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

package jaitools.media.jai.regionalize;

import jaitools.numeric.DoubleComparison;
import jaitools.tiledimage.DiskMemImage;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
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

    private static final int DEST_BAND = 0;

    private RandomIter srcIter;
    private WritableRandomIter destIter;
    private Rectangle destBounds;

    private int fillValue;
    private double tolerance;
    private boolean diagonal;
    private double refValue;
    private int band;

    private Queue<ScanSegment> segmentsPending;
    private List<ScanSegment> segmentsFilled;
    private ScanSegment lastSegmentChecked;

    /**
     * Create a FloodFiller to work with the given source image
     *
     * @param regionImage an instance of {@code DiskMemImage} provided by the client
     *        {@code RegionalizeOpImage} object
     *
     * @param src source image
     * 
     * @param band the soruce image band to be processed
     *
     * @param tolerance the maximum absolute difference in value for a pixel to be
     *        included in the region
     *
     * @param diagonal set to true to include sub-regions that are only connected
     *        diagonally; set to false to require orthogonal connections
     */
    public FloodFiller(DiskMemImage regionImage, RenderedImage src, int band, double tolerance, boolean diagonal) {

        this.band = band;
        this.tolerance = tolerance;
        this.diagonal = diagonal;
        this.destBounds = regionImage.getBounds();

        this.srcIter = RandomIterFactory.create(src, null);
        this.destIter = RandomIterFactory.createWritable(regionImage, null);
    }

    /**
     * Fill the region connected to the specified start pixel.
     * A pixel belongs to this region if there is a path between it and the starting
     * pixel which passes only through pixels of value {@code v} within the range
     * {@code v.ref - tolerance <= v <= v.ref + tolerance} where {@code v.ref} is the
     * reference value for the region
     *
     * @param x start pixel x coordinate
     * @param y start pixel y coordinate
     * @param fillValue the value to write to the destination image for this region
     * @param refValue the reference value for this region
     * @return a new {@linkplain FillResult}
     */
    public FillResult fill(int x, int y, int fillValue, double refValue) {

        this.fillValue = fillValue;
        this.refValue = refValue;

        segmentsPending = new LinkedList<ScanSegment>();
        segmentsFilled = new ArrayList<ScanSegment>();

        fillSegment(x, y);

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
                    newSegment = fillSegment(xi, segment.y - 1);
                    xi = newSegment != null ? newSegment.endX+1 : xi+1;
                }
            }
            
            if (segment.y < destBounds.y + destBounds.height - 1) {
                int xi = startX;
                while (xi <= endX) {
                    newSegment = fillSegment(xi, segment.y + 1);
                    xi = newSegment != null ? newSegment.endX+1 : xi+1;
                }
            }
        }

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
    private ScanSegment fillSegment(int x, int y) {

        // we rely on the y coord being checked prior to getting here
        int xo = x - destBounds.x;
        if (xo < 0 || xo >= destBounds.width) {
            return null;
        }

        boolean fill = false;
        ScanSegment segment = null;
        int left = x, right = x, xi = x;

        while (xi >= destBounds.x) {
            if (checkPixel(xi, y) && !pixelDone(xi, y)) {
                destIter.setSample(xi, y, DEST_BAND, fillValue);
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
        while (xi < destBounds.x + destBounds.width) {
            if (checkPixel(xi, y) && !pixelDone(xi, y)) {
                destIter.setSample(xi, y, DEST_BAND, fillValue);
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
     * Check if a pixel's value is within the fill tolerance
     * @param x pixel x
     * @param y pixel y
     * @return true if within tolerance; false otherwise
     */
    private boolean checkPixel(int x, int y) {
        double val = srcIter.getSampleDouble(x, y, band);
        return DoubleComparison.dcomp(Math.abs(val - refValue), tolerance) <= 0;
    }

    /**
     * Check if we have already processed the given pixel
     * @param x pixel x
     * @param y pixel y
     * @return true if already processed; false otherwise
     */
    private boolean pixelDone(int x, int y) {
        if (lastSegmentChecked != null) {
            if (lastSegmentChecked.contains(x, y)) {
                return true;
            }
        }

        for (ScanSegment segment : segmentsFilled) {
            if (segment.contains(x, y)) {
                lastSegmentChecked = segment;
                return true;
            }
        }

        lastSegmentChecked = null;
        return false;
    }

}