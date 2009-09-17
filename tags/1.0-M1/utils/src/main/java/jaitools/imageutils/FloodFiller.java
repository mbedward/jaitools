/*
 * Copyright 2009 Michael Bedward
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

package jaitools.imageutils;

import jaitools.numeric.DoubleComparison;
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

/**
 * Implements a flood-fill algorithm to work with Rasters.
 * <p>
 * The code is (very highly) adapted from an algorithm published in C# by J. Dunlap at:
 * <pre>   http://www.codeproject.com/KB/GDI-plus/queuelinearfloodfill.aspx</pre>
 * which was subsequently ported to Java by Owen Kaluza.
 * <b>Any bugs in the present code are not their fault</b>.
 * <p>
 * This version works with source and destination data in the form of Raster objects
 * which are accessed using JAI iterators.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
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

    
    public static class ScanSegment implements Comparable<ScanSegment> {

        int startX;
        int endX;
        int y;

        /**
         * Constructor
         */
        public ScanSegment(int startX, int endX, int y) {
            this.startX = startX;
            this.endX = endX;
            this.y = y;
        }

        /**
         * Check if the given pixel location lies within this segment
         */
        public boolean contains(int x, int y) {
            return this.y == y && startX <= x && endX >= x;
        }

        /**
         * Compare to another segment. Comparison is done first by
         * y coord, then by left x coord, then by right x coord.
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
     * @param targetImage the image to which flood fill values are written
     *
     * @param sourceimage the source image - may be the same as the targetImage
     *
     * @param sourceBand the source image band to be processed
     *
     * @param tolerance the maximum absolute difference in value for a pixel to be
     *        included in the region
     *
     * @param diagonal set to true to include sub-regions that are only connected
     *        diagonally; set to false to require orthogonal connections
     */
    public FloodFiller(
            RenderedImage sourceImage, int sourceBand,
            WritableRenderedImage targetImage, int targetBand,
            double tolerance, boolean diagonal) {

        this.srcBand = sourceBand;
        this.destImage = targetImage;
        this.destBand = targetBand;
        this.tolerance = tolerance;
        this.diagonal = diagonal;

        if (targetImage instanceof PlanarImage) {
            this.destBounds = ((PlanarImage)targetImage).getBounds();

        } else if (targetImage instanceof BufferedImage) {
            BufferedImage bImg = (BufferedImage) targetImage;
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
     * Fill the region connected to the specified start pixel.
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
     * Fill the region connected to the specified start pixel and lying within
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
     * Fill the region connected to the specified start pixel.
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
     * Fill the region connected to the specified start pixel and lying within
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
     * Fill pixels that:
     * <ul>
     * <li>are on the same horizontal scan line as the start pixel
     * <li>have the same value (plus or minus tolerance) as the start pixel
     * <li>have no intervening pixels with other values between them and
     * the start pixel
     * </ul>
     * @param x start pixel x coord
     * @param y start pixel y coord
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
     * Test if a pixel is a candidate to be filled.
     *
     * @param x pixel x
     * @param y pixel y
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
        return DoubleComparison.dcomp(Math.abs(val - refValue), tolerance) <= 0;
    }

}