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

import static jaitools.numeric.DoubleComparison.dcomp;
import static jaitools.numeric.DoubleComparison.dequal;
import jaitools.jts.LineSmoother;
import jaitools.jts.SmootherControl;
import jaitools.jts.Utils;
import jaitools.media.jai.AttributeOpImage;

import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;

import com.vividsolutions.jts.geom.LineSegment;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;

/**
 * Generates contours for user-specified levels of values in the source image.
 * The contours are returned as a {@code Collection} of
 * {@link com.vividsolutions.jts.geom.LineString}s.
 * <p>
 * The interpolation algorithm used is that of Paul Bourke: originally published
 * in Byte magazine (1987) as the CONREC contouring subroutine written in
 * FORTRAN. The implementation here was adapted from Paul Bourke's C code for the
 * algorithm available at: 
 * <a href="http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/">
 * http://local.wasp.uwa.edu.au/~pbourke/papers/conrec/</a>
 * <p>
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourOpImage extends AttributeOpImage {
    
    private static final int TL = 0;
    private static final int TR = 1;
    private static final int BL = 2;
    private static final int BR = 3;
    
    /** The source image band to process */
    private int band;
    
    /** Values at which to generate contour intervals */
    private List<Double> contourLevels;
    
    /** 
     * Interval between contours. This is used if specific contour
     * levels are not requested. Contours will be generated such that
     * the value of each is an integer multiple of this value.
     */
    private Double contourInterval;
    
    /** Output contour lines */
    private SoftReference<List<LineString>> cachedContours;
    
    /** Whether to simplify contour lines by removing coincident vertices */
    private final boolean simplify;
    
    /** 
     * Whether to merge contour lines across tile boundaries
     * during the tracing process. This can be very slow for
     * dense contours and large images.
     */
    private boolean mergeTiles;
    
    /** Whether to apply Bezier smoothing to the contour lines */
    private final boolean smooth;

    /** 
     * Alpha parameter controlling Bezier smoothing
     * (see {@link LineSmoother})
     */
    private double smoothAlpha = 0.0;
    
    /**
     * Control object for Bezier smoothing. Note that length units here
     * are pixels.
     */
    private final SmootherControl smootherControl = new SmootherControl() {

        public double getMinLength() {
            return 0.1;
        }

        public int getNumVertices(double length) {
            return (int) Math.max(5, length * 10);
        }
    };
    

    /**
     * Constructor. Note that one of {@code levels} or {@code interval} must
     * be supplied. If both are supplied {@code interval} is ignored.
     * 
     * @param source the source image
     * 
     * @param roi an optional {@code ROI} to constrain the areas for which
     *        contours are generated
     * 
     * @param band the band of the source image to process
     * 
     * @param levels values for which to generate contours
     * 
     * @param interval interval between contour levels (ignored if {@code levels}
     *        is supplied)
     * 
     * @param simplify whether to simplify contour lines by removing
     *        colinear vertices
     * 
     * @param mergeTiles whether to merge contour lines across source
     *        image tile boundaries (can be very slow for dense contours)
     * 
     * @param smooth whether contour lines should be smoothed using
     *        Bezier interpolation
     */
    public ContourOpImage(RenderedImage source, 
            ROI roi, 
            int band,
            Collection<? extends Number> levels,
            Double interval,
            boolean simplify,
            boolean mergeTiles,
            boolean smooth) {
                
        super(source, roi);

        this.band = band;
        
        this.contourLevels = new ArrayList<Double>();
        if (levels != null && !levels.isEmpty()) {
            // Use specific levels
            for (Number z : levels) {
                this.contourLevels.add(z.doubleValue());
            }
            Collections.sort(contourLevels);
            
        } else if (interval != null && !interval.isNaN()) {
            // Use requested interval with levels 'discovered' as the
            // image is scanned
            this.contourInterval = interval;
        } else {
            throw new IllegalArgumentException("At least one of levels or interval must be supplied");
        }
        
        this.simplify = simplify;
        this.mergeTiles = mergeTiles;
        this.smooth = smooth;
        
        // SG made static
//        PrecisionModel pm = new PrecisionModel(100);
//        this.geomFactory = new GeometryFactory(pm);
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
        Map<Integer, List<LineString>> contours = new HashMap<Integer, List<LineString>>();
        LineMerger merger = null;
        List<LineString> tempLines = null;

        RenderedImage src = getSourceImage(0);
        int tileIndex = 0;
        
        final int minTileX= src.getMinTileX();
        final int minTileY= src.getMinTileY();
        final int numTileY= src.getNumYTiles();
        final int numTileX= src.getNumXTiles();
        final int maxTileX= minTileX+ numTileX;
        final int maxTileY= minTileY+numTileY;
        
        for (int tileY =minTileY, ny = 0; tileY <maxTileY; tileY++, ny++) {
            for (int tileX = minTileX, nx = 0; tileX < maxTileX; tileX++, nx++, tileIndex++) {
                
                Map<Integer, List<LineSegment>> segments = getContourSegments(tileX, tileY);

                int levelIndex = 0;
                for (Double levelValue : contourLevels) {
                    List<LineSegment> levelSegments = segments.get(levelIndex);

                    if (!(levelSegments == null || levelSegments.isEmpty())) {
                        tempLines = new ArrayList<LineString>();

                        for (int i = levelSegments.size() - 1; i >= 0; i--) {
                            LineSegment seg = levelSegments.remove(i);
                            // Skip over any degenerate segments which can be produced by
                            // the traceContours algorithm
                            if (!seg.p0.equals2D(seg.p1)) {
                                tempLines.add(seg.toGeometry(Utils.GEOMETRY_FACTORY));
                            }
                        }

                        merger = new LineMerger();
                        merger.add(tempLines);
                        Collection<LineString> tileContours = merger.getMergedLineStrings();
                        
                        //
                        // SIMPLIFY
                        //
                        if (simplify) {
                            List<LineString> simplifiedContours = new ArrayList<LineString>();
                            for (LineString tc : tileContours) {
                                simplifiedContours.add( Utils.removeCollinearVertices(tc) );
                            }
                            tileContours = simplifiedContours;
                        }

                        List<LineString> levelContours = contours.get(levelIndex);
                        
                        if (levelContours == null) {
                            levelContours = new ArrayList<LineString>();
                            contours.put(levelIndex, levelContours);
                        }
                        
                        //
                        // MERGE
                        //
                        if (levelContours.isEmpty() || !mergeTiles) {
                            levelContours.addAll(tileContours);
                        } else {
                            merger = new LineMerger();
                            merger.add(levelContours);
                            merger.add(tileContours);
                            levelContours.clear();
                            levelContours.addAll( merger.getMergedLineStrings() );
                        }
                    }
                    
                    levelIndex++ ;
                }
            }
        }

        /*
         * Assemble contours into a simple list and assign values 
         */
        List<LineString> mergedContourLines = new ArrayList<LineString>();

        int levelIndex = 0;
        for (Double levelValue : contourLevels) {
            List<LineString> levelContours = contours.remove(levelIndex);
            if (!(levelContours == null || levelContours.isEmpty())) {
                for (LineString line : levelContours) {
                    line.setUserData(levelValue);
                }
                mergedContourLines.addAll(levelContours);
            }
            levelIndex++ ;
        }
        
        /*
         * Bezier smoothing of contours
         */
        if (smooth) {
            LineSmoother smoother = new LineSmoother(Utils.GEOMETRY_FACTORY);
            smoother.setControl(smootherControl);
            
            final int N = mergedContourLines.size();
            for (int i = N - 1; i >= 0; i--) {
                LineString contour = mergedContourLines.remove(i);
                LineString smoothed = smoother.smooth(contour, smoothAlpha);
                mergedContourLines.add(smoothed);
            }
        }
        
        return mergedContourLines;
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
     *            vertex 4 +-------------------+ vertex 3
     *                     | \               / |
     *                     |   \    m=3    /   |
     *                     |     \       /     |
     *                     |       \   /       |
     *                     |  m=4    X   m=2   |   centre vertex is 0
     *                     |       /   \       |
     *                     |     /       \     |
     *                     |   /    m=1    \   |
     *                     | /               \ |
     *            vertex 1 +-------------------+ vertex 2
     * 
     * </pre>
     * Each triangle is then categorized on which of its vertices are below,
     * at or above the contour level being considered. Triangle vertices 
     * (m1, m2, m3) are indexed such that:
     * <ul>
     * <li> m1 is the square vertex with index == triangle index
     * <li> m2 is square vertex 0
     * <li> m3 is square vertex m+1 (or 1 when m == 4)
     * </ul>
     * The original CONREC algorithm produces some duplicate line segments
     * which is not a problem when only plotting contours. However, here we
     * try to avoid any duplication because this can confuse the merging of
     * line segments into JTS LineStrings later.
     * 
     * @param tileX tile X index
     * @param tileY tile Y index
     * 
     * @return the generated contour segments
     */
    private Map<Integer, List<LineSegment>> getContourSegments(int tileX, int tileY) {

        Map<Integer, List<LineSegment>> segments = new HashMap<Integer, List<LineSegment>>();

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
        
        final PlanarImage src = getSourceImage(0);
        Raster tile = src.getTile(tileX, tileY);
        Rectangle bounds = tile.getBounds().intersection(srcBounds);
        
        int maxx = bounds.x + bounds.width - 1;
        int maxy = bounds.y + bounds.height - 1;
        
        /*
         * If we are using contour interval rather than specified levels,
         * check that we have all required intervals for this tile.
         */
        if (contourInterval != null) {
            if (!checkContourLevels(tile, bounds)) {
                // tile had no non-missing values
                return segments;
            }
        }

        for (int y = bounds.y; y < maxy; y++) {
            sample[BR] = tile.getSampleDouble(bounds.x, y, band);
            sample[TR] = tile.getSampleDouble(bounds.x, y + 1, band);
            
            for (int x = bounds.x + 1; x <= maxx; x++) {
                sample[BL] = sample[BR];
                sample[BR] = tile.getSampleDouble(x, y, band);
                sample[TL] = sample[TR];
                sample[TR] = tile.getSampleDouble(x, y + 1, band);

                temp1 = Math.min(sample[BL], sample[TL]);
                temp2 = Math.min(sample[BR], sample[TR]);
                double dmin = Math.min(temp1, temp2);

                temp1 = Math.max(sample[BL], sample[TL]);
                temp2 = Math.max(sample[BR], sample[TR]);
                double dmax = Math.max(temp1, temp2);

                final int size=contourLevels.size();
                for (int levelIndex = 0; levelIndex < size; levelIndex++) {
                    double levelValue = contourLevels.get(levelIndex);
                    if (levelValue < dmin || levelValue > dmax) {
                        continue;
                    }

                    List<LineSegment> zlist = segments.get(levelIndex);
                    if (zlist == null) {
                        zlist = new ArrayList<LineSegment>();
                        segments.put(levelIndex, zlist);
                    }
                    
                    h[4] = sample[TL] - levelValue;
                    xh[4] = x - 1;
                    yh[4] = y + 1;
                    sh[4] = Double.compare(h[4], 0.0);

                    h[3] = sample[TR] - levelValue;
                    xh[3] = x;
                    yh[3] = y + 1;
                    sh[3] = Double.compare(h[3], 0.0);

                    h[2] = sample[BR] - levelValue;
                    xh[2] = x;
                    yh[2] = y;
                    sh[2] = Double.compare(h[2], 0.0);

                    h[1] = sample[BL] - levelValue;
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
                        m3 = m == 4 ? 1 : m + 1;

                        int config = configLookup[sh[m1] + 1][sh[m2] + 1][sh[m3] + 1];
                        if (config == 0) {
                            continue;
                        }

                        double x0 = 0.0, y0 = 0.0, x1 = 0.0, y1 = 0.0;
                        boolean addSegment = true;
                        switch (config) {
                            /* Line between vertices 1 and 2 */
                            case 1: 
                                x0 = xh[m1];
                                y0 = yh[m1];
                                x1 = xh[m2];
                                y1 = yh[m2];
                                break;

                            /* Line between vertices 2 and 3 */
                            case 2: 
                                x0 = xh[m2];
                                y0 = yh[m2];
                                x1 = xh[m3];
                                y1 = yh[m3];
                                break;

                            /* 
                             * Line between vertices 3 and 1.
                             * We only want to generate this segment
                             * for triangles m=2 and m=3, otherwise
                             * we will end up with duplicate segments.
                             */
                            case 3: 
                                if (m == 2 || m == 3) {
                                    x0 = xh[m3];
                                    y0 = yh[m3];
                                    x1 = xh[m1];
                                    y1 = yh[m1];
                                } else {
                                    addSegment = false;
                                }
                                break;

                            /* Line between vertex 1 and side 2-3 */
                            case 4: 
                                x0 = xh[m1];
                                y0 = yh[m1];
                                x1 = sect(m2, m3, h, xh);
                                y1 = sect(m2, m3, h, yh);
                                break;

                            /* Line between vertex 2 and side 3-1 */
                            case 5: 
                                x0 = xh[m2];
                                y0 = yh[m2];
                                x1 = sect(m3, m1, h, xh);
                                y1 = sect(m3, m1, h, yh);
                                break;

                            /* Line between vertex 3 and side 1-2 */
                            case 6: 
                                x0 = xh[m3];
                                y0 = yh[m3];
                                x1 = sect(m1, m2, h, xh);
                                y1 = sect(m1, m2, h, yh);
                                break;

                            /* Line between sides 1-2 and 2-3 */
                            case 7: 
                                x0 = sect(m1, m2, h, xh);
                                y0 = sect(m1, m2, h, yh);
                                x1 = sect(m2, m3, h, xh);
                                y1 = sect(m2, m3, h, yh);
                                break;

                            /* Line between sides 2-3 and 3-1 */
                            case 8: 
                                x0 = sect(m2, m3, h, xh);
                                y0 = sect(m2, m3, h, yh);
                                x1 = sect(m3, m1, h, xh);
                                y1 = sect(m3, m1, h, yh);
                                break;

                            /* Line between sides 3-1 and 1-2 */
                            case 9: 
                                x0 = sect(m3, m1, h, xh);
                                y0 = sect(m3, m1, h, yh);
                                x1 = sect(m1, m2, h, xh);
                                y1 = sect(m1, m2, h, yh);
                                break;
                        }

                        if (addSegment) {
                            zlist.add(new LineSegment(x0, y0, x1, y1));
                        }
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
    private static double sect(int p1, int p2, double[] h, double[] coord) {
        return (h[p2] * coord[p1] - h[p1] * coord[p2]) / (h[p2] - h[p1]);
    }
    
    /**
     * Scans the image tile and checks that required contour values are in the
     * {@code contourLevels} list. 
     * <p>
     * Note: this method is only called when contour levels are being set 
     * according to a specified interval rather than user-supplied levels.
     * 
     * @param tile the image tile
     * @param bounds bounds of data in the tile
     * 
     * @return true if the tile had non-NaN values, false otherwise.
     */
    private boolean checkContourLevels(Raster tile, Rectangle bounds) {
        double minVal = 0, maxVal = 0;
        boolean first = true;

        int maxy = bounds.y + bounds.height - 1;
        int maxx = bounds.x + bounds.width - 1;
        boolean hasNonNan = false;
        
        for (int y = bounds.y; y < maxy; y++) {
            for (int x = bounds.x + 1; x <= maxx; x++) {
                double val = tile.getSampleDouble(x, y, band);
                if (!Double.isNaN(val)) {
                    hasNonNan = true;
                    if (first) {
                        minVal = maxVal = val;
                        first = false;
                    } else if (val < minVal) {
                        minVal = val;
                    } else if (val > maxVal) {
                        maxVal = val;
                    }
                }
            }
        }
        
        if (!hasNonNan) return false;
        
        double z = Math.floor(minVal / contourInterval) * contourInterval;
        if (dcomp(z, minVal) < 0) z += contourInterval;
        
        boolean found;
        while (dcomp(z, maxVal) <= 0) {
            found = false;
            for (Double level : contourLevels) {
                if (dequal(z, level)) {
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                contourLevels.add(z);
            }
            
            z += contourInterval;
        }
        
        return true;
    }

}
