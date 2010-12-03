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
import java.util.SortedSet;
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

            if (!isOutside(val)) {
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
     * Examine the values in the 2x2 kernel and match to one of
     * the cases in the table below:
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
     * These patterns are those used in the GRASS raster to vector routine.
     * @param sample array of current data window values
     * @return integer id of the matching configuration
     */
    private int nbrConfig(double[] sample) {
        if (isDifferent(sample[TL], sample[TR])) { // 0, 4, 5, 6, 8, 9, 10-13
            if (isDifferent(sample[TL], sample[BL])) { // 4, 6, 8, 10-13
                if (isDifferent(sample[BL], sample[BR])) { // 8, 10-13
                    if (isDifferent(sample[TL], sample[BR])) { // 10, 13
                        if (isDifferent(sample[TR], sample[BL])) {
                            return 13;
                        } else {
                            return 10;
                        }
                    } else { // 8, 11, 12
                        if (isDifferent(sample[TR], sample[BL])) { // 8, 11
                            if (isDifferent(sample[TL], sample[BR])) {
                                return 8;
                            } else {
                                return 12;
                            }
                        } else {
                            return 12;
                        }
                    }
                } else { // 4, 6
                    if (isDifferent(sample[TR], sample[BR])) {
                        return 6;
                    } else {
                        return 4;
                    }
                }
            } else { // 0, 5, 9
                if (isDifferent(sample[BL], sample[BR])) { // 0, 9
                    if (isDifferent(sample[TR], sample[BR])) {
                        return 9;
                    } else {
                        return 0;
                    }
                } else {
                    return 5;
                }
            }
        } else { // 1, 2, 3, 7, 11
            if (isDifferent(sample[TL], sample[BL])) { // 2, 3, 7
                if (isDifferent(sample[BL], sample[BR])) { // 3, 7
                    if (isDifferent(sample[TR], sample[BR])) {
                        return 7;
                    } else {
                        return 3;
                    }
                } else {
                    return 2;
                }
            } else { // 1, 14
                if (isDifferent(sample[TR], sample[BR])) {
                    return 1;
                } else {
                    return 14;
                }
            }
        }
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
            if (!isDifferent(d, value)) {
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
     * @return true if the values are different; false otherwise
     */
    private boolean isDifferent(double a, double b) {
        if (Double.isNaN(a) ^ Double.isNaN(b)) {
            return true;
        } else if (Double.isNaN(a) && Double.isNaN(b)) {
            return false;
        }

        if (Math.abs(a - b) > EPSILON) {
            return true;
        } else {
            return false;
        }
    }


}
