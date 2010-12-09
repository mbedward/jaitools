/*
 * Copyright 2010 Michael Bedward
 * 
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */
package jaitools.media.jai.contour;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import jaitools.media.jai.AttributeOpImage;
import jaitools.numeric.DoubleComparison;
import java.awt.image.RenderedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.SortedSet;
import java.util.TreeSet;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/**
 * Generates contours for user-specified levels of values in the source image.
 * The contours are returned as a {@code Collection} of
 * {@link com.vividsolutions.jts.geom.LineString}s.
 * <p>
 * The interpolation algorithm used is that of Paul Bourke. Originally published
 * in Byte magazine (1987) as the CONREC contouring subroutine written in
 * FORTRAN. The implementation here was adapted from Paul Bourke's C code for the
 * algorithm available at: 
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/">
 * http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/</a>
 * <p>
 * Example of use:
 * <pre><code>
 * RenderedImage src = ...
 * ParameterBlockJAI pb = new ParameterBlockJAI("Contour");
 * pb.setSource("source0", src);
 * 
 * // specify values at which contours are to be traced
 * List&lt;Double&gt; levels = Arrays.asList(new double[]{14.0, 14.5, 15.0, 15.5, 16.0})
 * pb.setParameter("levels", levels)
 * 
 * RenderedOp dest = JAI.create("Contour", pb);
 * Collection<LineString> contours = (Collection<LineString>) 
 *         dest.getProperty(ContourDescriptor.CONTOUR_PROPERTY_NAME);
 *
 * for (LineString contour : contours) {
 *   // get this contour's value
 *   Double contourValue = (Double) contour.getUserData();
 *   ...
 * }
 * </code></pre>
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourOpImage extends AttributeOpImage {

    private static final double EPS = 1.0e-8d;
    
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;
    
    /** The source image band to process */
    private int band;
    
    /** Values at which to generate contour intervals */
    private SortedSet<Double> contourIntervals;
    
    /** Output contour lines */
    private SoftReference<List<LineString>> cachedContours;
    
    /** Geometry factory used to create LineStrings */
    private GeometryFactory geomFactory;
    
    /** Whether to smooth the contour lines */
    private final boolean smooth;


    /**
     * Constructor.
     * 
     * @param source the source image
     * 
     * @param roi an optional {@code ROI} to constrain the areas for which
     *        contours are generated
     * 
     * @param band the band of the source image to process
     * 
     * @param intervals values for which to generate contours
     * 
     * @param smooth whether contour lines should be smoothed using
     *        Bezier interpolation
     */
    public ContourOpImage(RenderedImage source, 
            ROI roi, 
            int band,
            Collection<? extends Number> intervals,
            boolean smooth) {
                
        super(source, roi);

        this.band = band;
        
        this.contourIntervals = new TreeSet<Double>();
        for (Number z : intervals) {
            this.contourIntervals.add(z.doubleValue());
        }
        
        this.smooth = smooth;
        
        this.geomFactory = new GeometryFactory();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected Object getAttribute(String name) {
        if (cachedContours == null || cachedContours.get() == null) {
            synchronized(this) {
                cachedContours = new SoftReference<List<LineString>>(createContours());
            }
        }
        
        return cachedContours.get();
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected String[] getAttributeNames() {
        return new String[]{ContourDescriptor.CONTOUR_PROPERTY_NAME};
    }

    /**
     * {@inheritDoc }
     */
    @Override
    protected Class<?> getAttributeClass(String name) {
        if (ContourDescriptor.CONTOUR_PROPERTY_NAME.equalsIgnoreCase(name)) {
            return List.class;
        }
        
        return super.getAttributeClass(name);
    }
    

    /**
     * Controls contour generation. Each tile of the source image is processed
     * in turn. The resulting contour lines are then merged.
     * 
     * @return generated contours
     */
    private List<LineString> createContours() {
        List<LineString> contours = new ArrayList<LineString>();
        LineMerger merger = null;
        List<LineString> tempLines = null;

        RenderedImage src = getSourceImage(0);
        int numTiles = 0;
        for (int tileY = src.getMinTileY(), ny = 0; ny < src.getNumYTiles(); tileY++, ny++) {
            for (int tileX = src.getMinTileX(), nx = 0; nx < src.getNumXTiles(); tileX++, nx++) {
                numTiles++;

                Map<Double, List<LineSegment>> segments = getContourSegments(tileX, tileY);

                for (Entry<Double, List<LineSegment>> e : segments.entrySet()) {
                    Double zvalue = e.getKey();
                    List<LineSegment> zlist = e.getValue();

                    if (zlist == null || !zlist.isEmpty()) {
                        tempLines = new ArrayList<LineString>();

                        for (int i = zlist.size() - 1; i >= 0; i--) {
                            LineSegment seg = zlist.remove(i);
                            // Skip over any degenerate segments which can be produced by
                            // the traceContours algorithm
                            if (!seg.p0.equals2D(seg.p1)) {
                                tempLines.add(seg.toGeometry(geomFactory));
                            }
                        }

                        merger = new LineMerger();
                        merger.add(tempLines);
                        Collection<LineString> mergedLines = merger.getMergedLineStrings();

                        for (LineString line : mergedLines) {
                            line.setUserData(zvalue);
                        }

                        contours.addAll(mergedLines);
                    }
                }
            }
        }

        // Finally, merge the lines from separate tiles
        if (numTiles > 1) {
            List<LineString> mergedContourLines = new ArrayList<LineString>();

            for (Double z : contourIntervals) {
                tempLines = new ArrayList<LineString>();
                
                for (int i = contours.size() - 1; i >= 0; i--) {
                    LineString line = contours.get(i);
                    if (DoubleComparison.dequal((Double) line.getUserData(), z)) {
                        contours.remove(i);
                    }
                    tempLines.add(line);
                }

                if (!tempLines.isEmpty()) {
                    merger = new LineMerger();
                    merger.add(tempLines);
                    Collection<LineString> mergedLines = merger.getMergedLineStrings();
                    
                    for (LineString line : mergedLines) {
                        line.setUserData(z);
                    }
                    
                    mergedContourLines.addAll(mergedLines);
                }
            }
            
            contours = mergedContourLines;
        }
        
        return contours;
    }

    
    /**
     * Create contour segments for the specified source image tile.
     * The algorithm used is CONREC, devised by Paul Bourke (see class notes).
     * <p>
     * The source image is scanned with a 2x2 sample window. The algorithm
     * then treats these values as corner vertex values for a square that is
     * sub-divided into four triangles. This results in an additional centre
     * vertex.
     * <p>
     * The following diagram, taken from the C implementation of CONREC,
     * shows how vertices and triangles are indexed:
     * <pre>
                  vertex 4 +-------------------+ vertex 3
                           | \               / |
                           |   \    m=3    /   |
                           |     \       /     |
                           |       \   /       |
                           |  m=2    X   m=2   |   centre vertex is 0
                           |       /   \       |
                           |     /       \     |
                           |   /    m=1    \   |
                           | /               \ |
                  vertex 1 +-------------------+ vertex 2
     * </pre>
     * 
     * @param tileX tile X index
     * @param tileY tile Y index
     * 
     * @return the generated contour segments
     */
    private Map<Double, List<LineSegment>> getContourSegments(int tileX, int tileY) {

        Map<Double, List<LineSegment>> segments = new HashMap<Double, List<LineSegment>>();

        double[] sample = new double[4];
        double[] h = new double[5];
        double[] xh = new double[5];
        double[] yh = new double[5];
        int[] sh = new int[5];
        double temp1, temp2;

        int[][][] configLookup = {
            {{0, 0, 8}, {0, 2, 5}, {7, 6, 9}},
            {{0, 3, 4}, {1, 3, 1}, {4, 3, 0}},
            {{9, 6, 7}, {5, 2, 0}, {8, 0, 0}}
        };


        RandomIter iter = RandomIterFactory.create(getSourceImage(0), srcBounds);

        for (int y = srcBounds.y, iy = 0; iy < srcBounds.height - 1; y++, iy++) {
            sample[BR] = iter.getSampleDouble(srcBounds.x, y, band);
            sample[TR] = iter.getSampleDouble(srcBounds.x, y + 1, band);

            for (int x = srcBounds.x + 1, ix = 1; ix < srcBounds.width; x++, ix++) {
                sample[BL] = sample[BR];
                sample[BR] = iter.getSampleDouble(x, y, band);
                sample[TL] = sample[TR];
                sample[TR] = iter.getSampleDouble(x, y + 1, band);

                temp1 = Math.min(sample[BL], sample[TL]);
                temp2 = Math.min(sample[BR], sample[TR]);
                double dmin = Math.min(temp1, temp2);

                temp1 = Math.max(sample[BL], sample[TL]);
                temp2 = Math.max(sample[BR], sample[TR]);
                double dmax = Math.max(temp1, temp2);

                if (dmax < contourIntervals.first() || dmin > contourIntervals.last()) {
                    continue;
                }

                for (Double z : contourIntervals) {
                    if (z < dmin || z > dmax) {
                        continue;
                    }

                    List<LineSegment> zlist = segments.get(z);
                    if (zlist == null) {
                        zlist = new ArrayList<LineSegment>();
                        segments.put(z, zlist);
                    }

                    h[4] = sample[TL] - z;
                    xh[4] = x - 1;
                    yh[4] = y + 1;
                    sh[4] = Double.compare(h[4], 0.0);

                    h[3] = sample[TR] - z;
                    xh[3] = x;
                    yh[3] = y + 1;
                    sh[3] = Double.compare(h[3], 0.0);

                    h[2] = sample[BR] - z;
                    xh[2] = x;
                    yh[2] = y;
                    sh[2] = Double.compare(h[2], 0.0);

                    h[1] = sample[BL] - z;
                    xh[1] = x - 1;
                    yh[1] = y;
                    sh[1] = Double.compare(h[1], 0.0);

                    h[0] = (h[1] + h[2] + h[3] + h[4]) / 4.0;
                    xh[0] = x - 0.5;
                    yh[0] = y + 0.5;
                    sh[0] = Double.compare(h[0], 0.0);

                    /* Scan each triangle in the box */
                    int m1, m2, m3;
                    for (int m = 1; m <= 4; m++) {
                        m1 = m;
                        m2 = 0;
                        if (m != 4) {
                            m3 = m + 1;
                        } else {
                            m3 = 1;
                        }

                        int config = configLookup[sh[m1] + 1][sh[m2] + 1][sh[m3] + 1];
                        if (config == 0) {
                            continue;
                        }

                        double x0 = 0.0, y0 = 0.0, x1 = 0.0, y1 = 0.0;
                        switch (config) {
                            case 1: /* Line between vertices 1 and 2 */
                                x0 = xh[m1];
                                y0 = yh[m1];
                                x1 = xh[m2];
                                y1 = yh[m2];
                                break;

                            case 2: /* Line between vertices 2 and 3 */
                                x0 = xh[m2];
                                y0 = yh[m2];
                                x1 = xh[m3];
                                y1 = yh[m3];
                                break;

                            case 3: /* Line between vertices 3 and 1 */
                                x0 = xh[m3];
                                y0 = yh[m3];
                                x1 = xh[m1];
                                y1 = yh[m1];
                                break;

                            case 4: /* Line between vertex 1 and side 2-3 */
                                x0 = xh[m1];
                                y0 = yh[m1];
                                x1 = sect(m2, m3, h, xh);
                                y1 = sect(m2, m3, h, yh);
                                break;

                            case 5: /* Line between vertex 2 and side 3-1 */
                                x0 = xh[m2];
                                y0 = yh[m2];
                                x1 = sect(m3, m1, h, xh);
                                y1 = sect(m3, m1, h, yh);
                                break;

                            case 6: /* Line between vertex 3 and side 1-2 */
                                x0 = xh[m3];
                                y0 = yh[m3];
                                x1 = sect(m3, m2, h, xh);
                                y1 = sect(m3, m2, h, yh);
                                break;

                            case 7: /* Line between sides 1-2 and 2-3 */
                                x0 = sect(m1, m2, h, xh);
                                y0 = sect(m1, m2, h, yh);
                                x1 = sect(m2, m3, h, xh);
                                y1 = sect(m2, m3, h, yh);
                                break;

                            case 8: /* Line between sides 2-3 and 3-1 */
                                x0 = sect(m2, m3, h, xh);
                                y0 = sect(m2, m3, h, yh);
                                x1 = sect(m3, m1, h, xh);
                                y1 = sect(m3, m1, h, yh);
                                break;

                            case 9: /* Line between sides 3-1 and 1-2 */
                                x0 = sect(m3, m1, h, xh);
                                y0 = sect(m3, m1, h, yh);
                                x1 = sect(m1, m2, h, xh);
                                y1 = sect(m1, m2, h, yh);
                                break;
                        }

                        zlist.add(new LineSegment(x0, y0, x1, y1));
                    }
                }
            }
        }

        return segments;
    }

    /**
     * Calculate an X or Y ordinate for a contour segment end-point
     * relative to the difference in value between two sampling positions.
     * 
     * @param p1 index of the first sampling position
     * @param p2 index of the second sampling position
     * @param h source image values at sampling positions
     * @param coord X or Y ordinates of the 4 corner sampling positions
     * 
     * @return the calculated X or Y ordinate
     */
    private double sect(int p1, int p2, double[] h, double[] coord) {
        return (h[p2] * coord[p1] - h[p1] * coord[p2]) / (h[p2] - h[p1]);
    }

}
