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
    
    // positions in curData matrix just to avoid confusion
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;

    // these are used to identify the orientation of corner touches
    // between possibly separate polygons with the same value
    private static final int TL_BR = 4;
    private static final int TR_BL = 5;
    private static final int CROSS = 6;

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

    /*
     * list of corner touches between possibly separate polygons of
     * the same value. Each Coordinate has x:y = col:row and z set
     * to either TL_BR or TR_BL to indicate the orientation of the
     * corner touch.
     */
    List<Coordinate> cornerTouches;
    
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
        cornerTouches = new ArrayList<Coordinate>();
        
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

        Coordinate p = new Coordinate();
        RandomIter imgIter = RandomIterFactory.create(getSourceImage(0), null);

        Polygonizer polygonizer = new Polygonizer();
        polygonizer.add(lines);
        List<Geometry> polygons = new ArrayList<Geometry>();
        polygons.addAll( polygonizer.getPolygons() );

        int index = 0;
        for (Geometry geom : polygons) {
            Polygon poly = (Polygon) geom;
            InteriorPointArea ipa = new InteriorPointArea(poly);
            Coordinate c = ipa.getInteriorPoint();
            Point insidePt = geomFactory.createPoint(c);

            if (!poly.contains(insidePt)) {
                // try another method to generate an interior point
                boolean found = false;
                for (Coordinate ringC : poly.getExteriorRing().getCoordinates()) {
                    c.x = ringC.x + 0.5;
                    c.y = ringC.y;
                    insidePt = geomFactory.createPoint(c);
                    if (poly.contains(insidePt)) {
                        found = true;
                        break;
                    }
                }

                if (!found) {
                    throw new IllegalStateException("Can't locate interior point for polygon");
                }
            }

            p.setCoordinate(c);
            double val = imgIter.getSampleDouble((int) c.x, (int) c.y, band);

            if (!isOutside(val)) {
                if (insideEdges) {
                    geom.setUserData(val);
                } else {
                    geom.setUserData(inside);
                }
            }
        }
        
        return polygons;
    }



    /**
     * Vectorize the boundaries of regions of uniform value in the source image.
     */
    private void vectorizeBoundaries() {

        // a 2x2 matrix of double values used as a moving window
        double[] curData = new double[4];
        boolean[] flag = new boolean[4];
        RandomIter imageIter = RandomIterFactory.create(getSourceImage(0), null);

        if (!insideEdges) {
            setInsideValue();
        }
        
        final Double OUT = outsideValues.first();

        // NOTE: the for-loop indices are set to emulate a one pixel width border
        // around the source image area
        for (int y = srcBounds.y - 1; y < srcBounds.y + srcBounds.height; y++) {
            curData[TR] = curData[BR] = OUT;
            flag[TR] = flag[BR] = false;
            
            boolean yFlag = srcBounds.contains(srcBounds.x, y);
            boolean yNextFlag = srcBounds.contains(srcBounds.x, y+1);

            for (int x = srcBounds.x - 1; x < srcBounds.x + srcBounds.width; x++) {
                curData[TL] = curData[TR];
                flag[TL] = flag[TR];
                curData[BL] = curData[BR];
                flag[BL] = flag[BR];
                
                flag[TR] = yFlag && srcBounds.contains(x+1, y) && roi.contains(x+1, y);
                flag[BR] = yNextFlag && srcBounds.contains(x+1, y+1) && roi.contains(x+1, y+1);

                curData[TR] = (flag[TR] ? imageIter.getSampleDouble(x + 1, y, band) : OUT);
                if (isOutside(curData[TR])) {
                    curData[TR] = OUT;
                } else if (!insideEdges) {
                    curData[TR] = inside;
                }

                curData[BR] = (flag[BR] ? imageIter.getSampleDouble(x + 1, y + 1, band) : OUT);
                if (isOutside(curData[BR])) {
                    curData[BR] = OUT;
                } else if (!insideEdges) {
                    curData[BR] = inside;
                }

                updateCoordList(y, x, curData);
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
     * @param row index of the image row in the top left cell of the 2x2 data window
     * @param col index of the image col in the top left cell of the 2x2 data window
     * @param curData values in the current data window
     */
    private void updateCoordList(int row, int col, double[] curData) {
        LineSegment seg;

        switch (nbrConfig(curData)) {
        case 0:
            // vertical line continuing
            // nothing to do
            break;

        case 1:
            // bottom right corner
            // new horizontal and vertical lines
            horizLine = new LineSegment();
            horizLine.p0.x = col;

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 2:
            // horizontal line continuing
            // nothing to do
            break;

        case 3:
            // bottom left corner
            // end of horizontal line; start of new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);
            horizLine = null;

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 4:
            // top left corner
            // end of horizontal line; end of vertical line
            horizLine.p1.x = col;
            addHorizLine(row);
            horizLine = null;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);
            vertLines.remove(col);
            break;

        case 5:
            // top right corner
            // start horiztonal line; end vertical line
            horizLine = new LineSegment();
            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);
            vertLines.remove(col);
            break;

        case 6:
            // inverted T in upper half
            // end horiztonal line; start new horizontal line; end vertical line
            horizLine.p1.x = col;
            addHorizLine(row);

            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);
            vertLines.remove(col);
            break;

        case 7:
            // T in lower half
            // end horizontal line; start new horizontal line; start new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);

            horizLine.p0.x = col;

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 8:
            // T pointing left
            // end horizontal line; end vertical line; start new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);
            horizLine = null;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 9:
            // T pointing right
            // start new horizontal line; end vertical line; start new vertical line
            horizLine = new LineSegment();
            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);
            break;

        case 10:
            // cross
            // end horizontal line; start new horizontal line
            // end vertical line; start new vertical line
            horizLine.p1.x = col;
            addHorizLine(row);

            horizLine.p0.x = col;

            seg = vertLines.get(col);
            seg.p1.y = row;
            addVertLine(col);

            seg = new LineSegment();
            seg.p0.y = row;
            vertLines.put(col, seg);

            int z = -1;
            if (isDifferent(curData[TL], curData[BR])) {
                if (!isDifferent(curData[TR], curData[BL])) {
                    z = CROSS;
                }
            } else {
                if (isDifferent(curData[TR], curData[BL])) {
                    z = TL_BR;
                } else {
                    z = TR_BL;
                }
            }
            if (z != -1) {
                cornerTouches.add(new Coordinate(col, row, z));
            }
            break;

        case 11:
            // uniform
            // nothing to do
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
     *  8) AB   9) AB  10) AB  11) AA
     *     CB      AC      CD      AA
     * </pre>
     * These patterns are those used in the GRASS raster to vector routine.
     * @param curData array of current data window values
     * @return integer id of the matching configuration
     */
    private int nbrConfig(double[] curData) {
        if (isDifferent(curData[TL], curData[TR])) { // 0, 4, 5, 6, 8, 9, 10
            if (isDifferent(curData[TL], curData[BL])) { // 4, 6, 8, 10
                if (isDifferent(curData[BL], curData[BR])) { // 8, 10
                    if (isDifferent(curData[TR], curData[BR])) {
                        return 10;
                    } else {
                        return 8;
                    }
                } else { // 4, 6
                    if (isDifferent(curData[TR], curData[BR])) {
                        return 6;
                    } else {
                        return 4;
                    }
                }
            } else { // 0, 5, 9
                if (isDifferent(curData[BL], curData[BR])) { // 0, 9
                    if (isDifferent(curData[TR], curData[BR])) {
                        return 9;
                    } else {
                        return 0;
                    }
                } else {
                    return 5;
                }
            }
        } else { // 1, 2, 3, 7, 11
            if (isDifferent(curData[TL], curData[BL])) { // 2, 3, 7
                if (isDifferent(curData[BL], curData[BR])) { // 3, 7
                    if (isDifferent(curData[TR], curData[BR])) {
                        return 7;
                    } else {
                        return 3;
                    }
                } else {
                    return 2;
                }
            } else { // 1, 11
                if (isDifferent(curData[TR], curData[BR])) {
                    return 1;
                } else {
                    return 11;
                }
            }
        }
    }

    /**
     * Create a LineString for a newly constructed horizontal border segment
     * @param row index of the image row in the top left cell of the current data window
     */
    private void addHorizLine(int row) {
        Coordinate[] coords = new Coordinate[] { 
            new Coordinate(horizLine.p0.x, row),
            new Coordinate(horizLine.p1.x, row) 
        };

        lines.add(geomFactory.createLineString(coords));
    }

    /**
     * Create a LineString for a newly constructed vertical border segment
     * @param col index of the image column in the top-left cell of the current data window
     */
    private void addVertLine(int col) {
        
        Coordinate[] coords = new Coordinate[] {
            new Coordinate(col, vertLines.get(col).p0.y),
            new Coordinate(col, vertLines.get(col).p1.y)
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
