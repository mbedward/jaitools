/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.imageutils;

import java.awt.Rectangle;
import java.awt.geom.Ellipse2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRenderedImage;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;

import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

import org.jaitools.numeric.CompareOp;


/**
 * A flood-filling algorithm to use with rasters.
 * <p>
 * The code is adapted from an algorithm published in C# by J. Dunlap at:
 * <pre>   http://www.codeproject.com/KB/GDI-plus/queuelinearfloodfill.aspx</pre>
 * which was subsequently ported to Java by Owen Kaluza. The JAITools implementation
 * is substantially different and any bugs should not be blamed on the above authors.
 * <p>
 * This version works with a source {@code RenderedImage} and a destination
 * {@code WritableRenderedImage}, both of which are accessed using JAI iterators.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class FloodFiller {

    private RandomIter srcIter;
    private WritableRenderedImage destImage;
    private Rectangle destBounds;

    private int fillValue;
    private double tolerance;
    private boolean diagonal;
    private double refValue;
    private int srcBand;
    private int destBand;
    private double fillRadius;
    private boolean usingRadius;
    private ROI roi;

    /**
     * Records a segment of contiguous pixels in a single row that will
     * become part of a filled region.
     */
    public static class ScanSegment implements Comparable<ScanSegment> {

        int startX;
        int endX;
        int y;

        /**
         * Creates a new segment.
         * 
         * @param startX start X ordinate
         * @param endX end X ordinate
         * @param y Y ordinate
         */
        public ScanSegment(int startX, int endX, int y) {
            this.startX = startX;
            this.endX = endX;
            this.y = y;
        }

        /**
         * Checks if the given location lies within this segment.
         * 
         * @param x location X ordinate
         * @param y location Y ordinate
         * 
         * @return the result as boolean
         */
        public boolean contains(int x, int y) {
            return this.y == y && startX <= x && endX >= x;
        }

        /**
         * Compares this segment to another. The comparison is first by
         * Y ordinate, then by left and right X ordinates.
         * 
         * @param other the other segment
         */
        public int compareTo(ScanSegment other) {
            if (y < other.y) {
                return -1;
            } else if (y > other.y) {
                return 1;
            } else if (startX < other.startX) {
                return -1;
            } else if (startX > other.startX) {
                return 1;
            } else if (endX < other.endX) {
                return -1;
            } else if (endX > other.endX) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    private Queue<ScanSegment> segmentsPending;
    private List<ScanSegment> segmentsFilled;
    private ScanSegment lastSegmentChecked;

    /**
     * Create a FloodFiller to work with the given source image
     *
     * @param sourceImage the source image
     * 
     * @param destImage the destination image
     *
     * @param sourceBand the source image band to be processed
     *
     * @param destBand the destination image band to write to
     * 
     * @param tolerance the maximum absolute difference in value for a pixel to be
     *        included in the region
     *
     * @param diagonal set to true to include sub-regions that are only connected
     *        diagonally; set to false to require orthogonal connections
     */
    public FloodFiller(
            RenderedImage sourceImage, int sourceBand,
            WritableRenderedImage destImage, int destBand,
            double tolerance, boolean diagonal) {

        this.srcBand = sourceBand;
        this.destImage = destImage;
        this.destBand = destBand;
        this.tolerance = tolerance;
        this.diagonal = diagonal;

        if (destImage instanceof PlanarImage) {
            this.destBounds = ((PlanarImage)destImage).getBounds();

        } else if (destImage instanceof BufferedImage) {
            BufferedImage bImg = (BufferedImage) destImage;
            this.destBounds = new Rectangle(
                    bImg.getMinX(),
                    bImg.getMinY(),
                    bImg.getWidth(),
                    bImg.getHeight());

        } else {
            // @todo I suppose we could try reflection instead of giving up here

            throw new IllegalArgumentException("regionImage arg must be a PlanarImage or a BufferedImage");
        }

        this.srcIter = RandomIterFactory.create(sourceImage, null);

        // Note: the destination iterator is created in the fillRadius
        // method to avoid tile ownership problems
    }

    /**
     * Fills the region connected to the specified start pixel.
     * A pixel belongs to this region if there is a path between it and the starting
     * pixel which passes only through pixels of value {@code v} within the range
     * {@code start_pixel_value - tolerance <= v <= start_pixel_value + tolerance}.
     *
     * @param x start pixel x coordinate
     *
     * @param y start pixel y coordinate
     *
     * @param fillValue the value to write to the destination image for this region
     *
     * @return a new {@linkplain FillResult}
     */
    public FillResult fill(int x, int y, int fillValue) {
        return fill(x, y, fillValue, srcIter.getSampleDouble(x, y, srcBand));
    }

    /**
     * Fills the region connected to the specified start pixel and lying within
     * {@code radius} pixels of the start pixel.
     * <p>
     * A pixel belongs to this region if there is a path between it and the starting
     * pixel which passes only through pixels of value {@code v} within the range
     * {@code start_pixel_value - tolerance <= v <= start_pixel_value + tolerance}.
     *
     * @param x start pixel x coordinate
     *
     * @param y start pixel y coordinate
     *
     * @param fillValue the value to write to the destination image for this region
     *
     * @param radius maximum distance (pixels) that a candidate pixel can be from
     *        the start pixel
     *
     * @return a new {@linkplain FillResult}
     */
    public FillResult fillRadius(int x, int y, int fillValue, double radius) {
        return fillRadius(x, y, fillValue, srcIter.getSampleDouble(x, y, srcBand), radius);
    }

    /**
     * Fills the region connected to the specified start pixel.
     * A pixel belongs to this region if there is a path between it and the starting
     * pixel which passes only through pixels of value {@code v} within the range
     * {@code refValue - tolerance <= v <= refValue + tolerance}.
     *
     * @param x start pixel x coordinate
     *
     * @param y start pixel y coordinate
     *
     * @param fillValue the value to write to the destination image for this region
     *
     * @param refValue the source image reference value for the region
     *
     * @return a new {@linkplain FillResult}
     */
    public FillResult fill(int x, int y, int fillValue, double refValue) {
        return fillRadius(x, y, fillValue, refValue, Double.NaN);
    }

    /**
     * Fills the region connected to the specified start pixel and lying within
     * {@code radius} pixels of the start pixel.
     * <p>
     * A pixel belongs to this region if there is a path between it and the starting
     * pixel which passes only through pixels of value {@code v} within the range
     * {@code refValue - tolerance <= v <= refValue + tolerance}.
     *
     * @param x start pixel x coordinate
     *
     * @param y start pixel y coordinate
     *
     * @param fillValue the value to write to the destination image for this region
     *
     * @param refValue the source image reference value for the region
     *
     * @param radius maximum distance (pixels) that a candidate pixel can be from
     *        the start pixel
     *
     * @return a new {@linkplain FillResult}
     */
    public FillResult fillRadius(int x, int y, int fillValue, double refValue, double radius) {

        this.fillValue = fillValue;
        this.refValue = refValue;
        this.fillRadius = radius;

        if (!Double.isNaN(radius)) {
            Ellipse2D shp = new Ellipse2D.Double(x-radius, y-radius, 2*radius, 2*radius);
            roi = new ROIShape(shp);
            roi = roi.intersect(new ROIShape(destBounds));

        } else {
            roi = new ROIShape(destBounds);
        }

        WritableRandomIter destIter = RandomIterFactory.createWritable(destImage, null);
        segmentsPending = new LinkedList<ScanSegment>();
        segmentsFilled = new ArrayList<ScanSegment>();

        fillSegment(x, y, destIter);

        ScanSegment segment;
        ScanSegment newSegment;
        while ((segment = segmentsPending.poll()) != null) {

            int startX, endX;
            if (diagonal) {
                startX = segment.startX-1;
                endX = segment.endX + 1;
            } else {
                startX = segment.startX;
                endX = segment.endX;
            }

            if (segment.y > destBounds.y) {
                int xi = startX;
                while (xi <= endX) {
                    newSegment = fillSegment(xi, segment.y - 1, destIter);
                    xi = newSegment != null ? newSegment.endX+1 : xi+1;
                }
            }

            if (segment.y < destBounds.y + destBounds.height - 1) {
                int xi = startX;
                while (xi <= endX) {
                    newSegment = fillSegment(xi, segment.y + 1, destIter);
                    xi = newSegment != null ? newSegment.endX+1 : xi+1;
                }
            }
        }

        destIter.done();
        return new FillResult(fillValue, refValue, segmentsFilled);
    }


    /**
     * Fills pixels that:
     * <ul>
     * <li>are on the same horizontal scan line as the start pixel
     * <li>have the same value (plus or minus tolerance) as the start pixel
     * <li>have no intervening pixels with other values between them and
     * the start pixel
     * </ul>
     * 
     * @param x start X ordinate
     * @param y start Y ordinate
     * 
     * @return one of FILL_NEW_SEGMENT, FILL_NONE or FILL_NEED_NEXT_RASTER
     */
    private ScanSegment fillSegment(int x, int y, WritableRandomIter destIter) {

        if (!roi.contains(x, y)) {
            return null;
        }

        boolean fill = false;
        ScanSegment segment = null;
        int left = x, right = x, xi = x;

        while (roi.contains(xi, y)) {
            if (checkPixel(xi, y)) {
                destIter.setSample(xi, y, destBand, fillValue);
                fill = true;
                left = xi;
                xi-- ;
            } else {
                break;
            }
        }

        if (!fill) {
            return null;
        }

        xi = x+1;
        while (roi.contains(xi, y)) {
            if (checkPixel(xi, y)) {
                destIter.setSample(xi, y, destBand, fillValue);
                right = xi;
                xi++ ;
            } else {
                break;
            }
        }

        segment = new ScanSegment(left, right, y);
        segmentsFilled.add(segment);
        segmentsPending.offer(segment);

        return segment;
    }


    /**
     * Tests if a pixel is a candidate to be filled.
     *
     * @param x X ordinate
     * @param y Y ordinate
     *
     * @return true if a fill candidate; false otherwise
     */
    private boolean checkPixel(int x, int y) {

        /*
         * First check if the pixel has already been filled
         */
        if (lastSegmentChecked != null) {
            if (lastSegmentChecked.contains(x, y)) {
                return false;
            }
        }

        for (ScanSegment segment : segmentsFilled) {
            if (segment.contains(x, y)) {
                lastSegmentChecked = segment;
                return false;
            }
        }

        lastSegmentChecked = null;

        /*
         * Now test if the pixel's value is within range
         * of the flood fill reference value
         */
        double val = srcIter.getSampleDouble(x, y, srcBand);
        return CompareOp.acompare(Math.abs(val - refValue), tolerance) <= 0;
    }

}
