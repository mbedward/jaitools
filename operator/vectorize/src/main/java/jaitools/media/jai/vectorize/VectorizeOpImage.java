/*
 * Copyright 2010 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.media.jai.vectorize;

import com.vividsolutions.jts.algorithm.InteriorPointArea;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.operation.polygonize.Polygonizer;
import jaitools.media.jai.AttributeOpImage;
import java.awt.image.RenderedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/**
 * Vectorize regions of uniform value in an image
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class VectorizeOpImage extends AttributeOpImage {
    
    // positions in the 2x2 sample window
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    /*
     * Possible configurations of values in the 2x2 sample window: 
     *
     * <pre>
     *  0) AB   1) AA   2) AA   3) AA
     *     AB      AB      BB      BA
     *
     *  4) AB   5) AB   6) AB   7) AA
     *     BB      AA      CC      BC
     *
     *  8) AB   9) AB  10) AB  11) AB
     *     CB      AC      BC      CA
     * 
     * 12) AB  13) AB  14) AA
     *     BA      CD      AA
     * </pre>
     * 
     * These patterns are adapted from those used in the GRASS raster to 
     * vector routine.
     * 
     * The following map is a lookup table for the sample window pattern
     * where the key is constructed as follows (bit 6 is left-most):
     * bit 6 = TR != TL
     * bit 5 = BL != TL
     * bit 4 = BL != TR
     * bit 3 = BR != TL
     * bit 2 = BR != TR
     * bit 1 = BR != BL
     */
    private static final SortedMap<Integer, Integer> NBR_CONFIG_LOOKUP = new TreeMap<Integer, Integer>();
    static {
        NBR_CONFIG_LOOKUP.put( 0x2d, 0 );  // 101101
        NBR_CONFIG_LOOKUP.put( 0x07, 1 );  // 000111
        NBR_CONFIG_LOOKUP.put( 0x1e, 2 );  // 011110
        NBR_CONFIG_LOOKUP.put( 0x19, 3 );  // 011001
        NBR_CONFIG_LOOKUP.put( 0x34, 4 );  // 110100
        NBR_CONFIG_LOOKUP.put( 0x2a, 5 );  // 101010
        NBR_CONFIG_LOOKUP.put( 0x3e, 6 );  // 111110
        NBR_CONFIG_LOOKUP.put( 0x1f, 7 );  // 011111
        NBR_CONFIG_LOOKUP.put( 0x3d, 8 );  // 111101
        NBR_CONFIG_LOOKUP.put( 0x2f, 9 );  // 101111
        NBR_CONFIG_LOOKUP.put( 0x37, 10 ); // 110111
        NBR_CONFIG_LOOKUP.put( 0x3b, 11 ); // 111011
        NBR_CONFIG_LOOKUP.put( 0x33, 12 ); // 110011
        NBR_CONFIG_LOOKUP.put( 0x3f, 13 ); // 111111
        NBR_CONFIG_LOOKUP.put( 0x00, 14 ); // 000000
    }
    

    // Precision of comparison in the function different(a, b)
    // TODO: enable this to be set by the user
    private static final double EPSILON = 1.0e-8d;
    
    // Default values used for "inside" when not vectorizing
    // boundaries between adjacent inside regions
    private static final int INSIDE_FLAG_VALUE = 1;

    // Source image band being processed
    private final int band;

    // Set of values that indicate 'outside' or 'no data' areas in the raster
    private SortedSet<Double> outsideValues;
    
    // Flag indicating whether the boundaries between adjacent inside regions
    // should be vectorized
    private boolean insideEdges;

    // Proxy value used when inside edges are not being vectorized
    // (ie. insideEdges == false)
    private Double inside = null;

    /*
     * array of Coor objects that store end-points of vertical lines under construction
     */
    private Map<Integer, LineSegment> vertLines;

    /*
     * end-points of horizontal line under construction
     */
    private LineSegment horizLine;

    /*
     * collection of line strings on the boundary of raster regions
     */
    private List<LineString> lines;
    
    /*
     * Factory for construction of JTS Geometry objects
     */
    private GeometryFactory geomFactory;

    SoftReference<List<Geometry>> cachedVectors;
    

    /**
     * Constructor.
     * 
     * @param source the source image to be vectorized
     * 
     * @param roi an optional {@code ROI} defining the region to be vectorized
     * 
     * @param band the source image band to examine
     * 
     * @param outsideValues values representing "outside" areas (ie. regions that
     *        will not be vectorized); may be null or empty
     * 
     * @param insideEdges flag controlling whether boundaries between adjacent
     *        "inside" regions should be vectorized
     */
    public VectorizeOpImage(RenderedImage source,
            ROI roi,
            int band,
            List<Double> outsideValues,
            boolean insideEdges) {
            
        super(source, roi);
                
        this.band = band;
        
        this.outsideValues = new TreeSet<Double>();
        if (outsideValues == null || outsideValues.isEmpty()) {
            this.outsideValues.add(Double.NaN);
        } else {
            this.outsideValues.addAll(outsideValues);
        }
        
        this.insideEdges = insideEdges;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Geometry> getAttribute(String name) {
        if (cachedVectors == null || cachedVectors.get() == null) {
            synchronized(this) {
                cachedVectors = new SoftReference<List<Geometry>>(doVectorize());
            }
        }
        
        return cachedVectors.get();
    }
        
    /**
     * {@inheritDoc}
     */
    @Override
    protected String[] getAttributeNames() {
        return new String[] {VectorizeDescriptor.VECTOR_PROPERTY_NAME};
    }
    

    private List<Geometry> doVectorize() {
        geomFactory = new GeometryFactory();
        lines = new ArrayList<LineString>();
        vertLines = new HashMap<Integer, LineSegment>();
        
        vectorizeBoundaries();
        return assemblePolygons();
    }
        
    /**
     * Polygonize the boundary segments that have been collected by the 
     * vectorizing algorithm and, if the field {@code insideEdges} is TRUE,
     * assign the value of the source image band to each polygon's user data
     * field.
     */
    private List<Geometry> assemblePolygons() {

        List<Geometry> polygons = new ArrayList<Geometry>();
        RandomIter imgIter = RandomIterFactory.create(getSourceImage(0), null);

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        Collection<Geometry> rawPolys = polygonizer.getPolygons();

        int index = 0;
        for (Geometry geom : rawPolys) {
            Polygon poly = (Polygon) geom;
            InteriorPointArea ipa = new InteriorPointArea(poly);
            Coordinate c = ipa.getInteriorPoint();
            Point pt = geomFactory.createPoint(c);

            if (!poly.contains(pt)) {
                // try another method to generate an interior point
                boolean found = false;
                for (Coordinate ringC : poly.getExteriorRing().getCoordinates()) {
                    c.x = ringC.x + 0.5;
                    c.y = ringC.y;
                    pt = geomFactory.createPoint(c);
                    if (poly.contains(pt)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new IllegalStateException("Can't locate interior point for polygon");
                }
            }

            double val = imgIter.getSampleDouble((int) c.x, (int) c.y, band);

            if (roi.contains(c.x, c.y) && !isOutside(val)) {
                if (insideEdges) {
                    poly.setUserData(val);
                } else {
                    poly.setUserData(inside);
                }
                polygons.add(poly);
            }
        }
        
        return polygons;
    }



    /**
     * Vectorize the boundaries of regions of uniform value in the source image.
     */
    private void vectorizeBoundaries() {

        // array treated as a 2x2 matrix of double values used as a moving window
        double[] sample = new double[4];
        
        // array treated as a 2x2 matrix of boolean flags used to indicate which
        // sampling window pixels are within the source image and ROI
        boolean[] flag = new boolean[4];
        
        RandomIter imageIter = RandomIterFactory.create(getSourceImage(0), null);

        if (!insideEdges) {
            setInsideValue();
        }
        
        final Double OUT = outsideValues.first();
        
        // NOTE: the for-loop indices are set to emulate a one pixel width border
        // around the source image area
        for (int y = srcBounds.y - 1; y < srcBounds.y + srcBounds.height; y++) {
            sample[TR] = sample[BR] = OUT;
            flag[TR] = flag[BR] = false;
            
            boolean yFlag = srcBounds.contains(srcBounds.x, y);
            boolean yNextFlag = srcBounds.contains(srcBounds.x, y+1);

            for (int x = srcBounds.x - 1; x < srcBounds.x + srcBounds.width; x++) {
                sample[TL] = sample[TR];
                flag[TL] = flag[TR];
                sample[BL] = sample[BR];
                flag[BL] = flag[BR];
                
                flag[TR] = yFlag && srcBounds.contains(x+1, y) && roi.contains(x+1, y);
                flag[BR] = yNextFlag && srcBounds.contains(x+1, y+1) && roi.contains(x+1, y+1);

                sample[TR] = (flag[TR] ? imageIter.getSampleDouble(x + 1, y, band) : OUT);
                if (isOutside(sample[TR])) {
                    sample[TR] = OUT;
                } else if (!insideEdges) {
                    sample[TR] = inside;
                }

                sample[BR] = (flag[BR] ? imageIter.getSampleDouble(x + 1, y + 1, band) : OUT);
                if (isOutside(sample[BR])) {
                    sample[BR] = OUT;
                } else if (!insideEdges) {
                    sample[BR] = inside;
                }

                updateCoordList(x, y, sample);
            }
        }
    }
    

    /**
     * Sets the proxy value used for "inside" cells when inside edges
     * are not being vectorized.
     */
    private void setInsideValue() {
        Double maxFinite = null;

        for (Double d : outsideValues) {
            if (!(d.isInfinite() || d.isNaN())) {
                maxFinite = d;
            }
        }

        if (maxFinite != null) {
            inside = maxFinite + 1;
        } else {
            inside = (double) INSIDE_FLAG_VALUE;
        }
    }


    /**
     * This method controls the construction of line segments that border regions of uniform data
     * in the raster. See the {@linkplain #nbrConfig} method for more details.
     *
     * @param xpixel index of the image col in the top left cell of the 2x2 data window
     * @param ypixel index of the image row in the top left cell of the 2x2 data window
     * @param sample current sampling window data
     */
    private void updateCoordList(int xpixel, int ypixel, double[] sample) {
        LineSegment seg;
        int xvec = xpixel + 1;
        int yvec = ypixel + 1;

        int configIndex = nbrConfig(sample);
        switch (configIndex) {
            case 0:
                /*
                 * Vertical edge:
                 * 
                 *   AB
                 *   AB
                 * 
                 * No update required.
                 */
                break;

            case 1:
                /*
                 * Corner:
                 * 
                 *   AA
                 *   AB
                 * 
                 * Begin new horizontal.
                 * Begin new vertical.
                 */
                horizLine = new LineSegment();
                horizLine.p0.x = xvec;

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 2:
                /*
                 * Horizontal edge:
                 * 
                 *   AA
                 *   BB
                 * 
                 * No update required.
                 */
                break;

            case 3:
                /*
                 * Corner:
                 * 
                 *   AA
                 *   BA
                 * 
                 * End current horizontal. 
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);
                horizLine = null;

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 4:
                /*
                 * Corner:
                 * 
                 *   AB
                 *   BB
                 * 
                 * End current horizontal. 
                 * End current vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);
                horizLine = null;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);
                vertLines.remove(xvec);
                break;

            case 5:
                /*
                 * Corner:
                 * 
                 *   AB
                 *   AA
                 * 
                 * Begin new horizontal. 
                 * End current vertical.
                 */
                horizLine = new LineSegment();
                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);
                vertLines.remove(xvec);
                break;

            case 6:
                /*
                 * T-junction:
                 * 
                 *   AB
                 *   CC
                 * 
                 * End current horizontal. 
                 * Begin new horizontal. 
                 * End current vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);

                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);
                vertLines.remove(xvec);
                break;

            case 7:
                /*
                 * T-junction:
                 * 
                 *   AA
                 *   BC
                 * 
                 * End current horizontal. 
                 * Begin new horizontal. 
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);

                horizLine.p0.x = xvec;

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 8:
                /*
                 * T-junction:
                 * 
                 *   AB
                 *   CB
                 * 
                 * End current horizontal.
                 * End current vertical.
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);
                horizLine = null;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 9:
                /*
                 * T-junction:
                 * 
                 *   AB
                 *   AC
                 * 
                 * Begin new horizontal.
                 * End current vertical.
                 * Begin new vertical.
                 */
                horizLine = new LineSegment();
                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 10:
            case 11:
            case 12:
            case 13:
                /*
                 * Cross:
                 * 
                 *   AB  AB  AB  AB
                 *   BC  CA  BA  CD
                 * 
                 * End current horizontal.
                 * Begin new horizontal.
                 * End current vertical.
                 * Begin new vertical.
                 */
                horizLine.p1.x = xvec;
                addHorizLine(yvec);

                horizLine.p0.x = xvec;

                seg = vertLines.get(xvec);
                seg.p1.y = yvec;
                addVertLine(xvec);

                seg = new LineSegment();
                seg.p0.y = yvec;
                vertLines.put(xvec, seg);
                break;

            case 14:
                /*
                 * Uniform:
                 * 
                 *   AA
                 *   AA
                 * 
                 * No update required.
                 */
                break;
        }
    }

    /**
     * Examine the values in the 2x2 sample window and return
     * the integer id of the configuration (0 - 14) based on
     * the NBR_CONFIG_LOOKUP {@code Map}.
     * 
     * @param sample sample window values
     * @return configuration id
     */
    private int nbrConfig(double[] sample) {
        int flag = 0;
        
        flag |= (isDifferent(sample[TR], sample[TL]) << 5);
        
        flag |= (isDifferent(sample[BL], sample[TL]) << 4);
        flag |= (isDifferent(sample[BL], sample[TR]) << 3);
        
        flag |= (isDifferent(sample[BR], sample[TL]) << 2);
        flag |= (isDifferent(sample[BR], sample[TR]) << 1);
        flag |=  isDifferent(sample[BR], sample[BL]);
        
        return NBR_CONFIG_LOOKUP.get(flag);
    }

    /**
     * Create a LineString for a newly constructed horizontal border segment
     * @param y y ordinate of the line
     */
    private void addHorizLine(int y) {
        Coordinate[] coords = new Coordinate[] { 
            new Coordinate(horizLine.p0.x, y),
            new Coordinate(horizLine.p1.x, y) 
        };

        lines.add(geomFactory.createLineString(coords));
    }

    /**
     * Create a LineString for a newly constructed vertical border segment
     * @param x x ordinate of the line
     */
    private void addVertLine(int x) {
        
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(x, vertLines.get(x).p0.y),
            new Coordinate(x, vertLines.get(x).p1.y)
        };
        
        lines.add(geomFactory.createLineString(coords));
    }

    private boolean isOutside(double value) {
        for (Double d : outsideValues) {
            if (isDifferent(d, value) == 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Test if two double values are different. Uses an absolute tolerance and
     * checks for NaN values.
     *
     * @param a first value
     * @param b second value
     * @return 1 if the values are different; 0 otherwise
     */
    private int isDifferent(double a, double b) {
        if (Double.isNaN(a) ^ Double.isNaN(b)) {
            return 1;
        } else if (Double.isNaN(a) && Double.isNaN(b)) {
            return 0;
        }

        if (Math.abs(a - b) > EPSILON) {
            return 1;
        } else {
            return 0;
        }
    }


}
