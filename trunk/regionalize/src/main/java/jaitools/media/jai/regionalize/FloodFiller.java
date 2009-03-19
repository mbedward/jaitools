/*
 * Copyright 2009 Michael Bedward
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

package jaitools.media.jai.regionalize;

/**
 * Implements a flood-fill algorithm.
 * 
 * Highly adapted from an algorithm published by J. Dunlap at
 * http://www.codeproject.com/KB/GDI-plus/queuelinearfloodfill.aspx
 * and subsequently ported to Java by Owen Kaluza. Any bugs are not
 * their fault.
 *
 * This version has been adapated to work with JAI iterators and has
 * the added option of including sub-regions that are only
 * diagonally connected.
 *
 * @author Michael Bedward
 */

import jaitools.utils.DoubleComparison;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Queue;
import java.util.LinkedList;
import java.util.List;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.WritableRandomIter;

class FloodFiller {

    static final int FILL_4WAY = 0;
    static final int FILL_8WAY = 1;

    private double fillValue;
    private double tolerance;
    private double startCellValue;
    private int band;

    private Queue<ScanSegment> pending;
    private List<ScanSegment> filled;
    private ScanSegment lastSegmentChecked;
    private int totalNumFilled;

    private int minSrcX;
    private int maxSrcX;
    private int minSrcY;
    private int maxSrcY;

    private RandomIter srcIter;
    private WritableRandomIter destIter;

    /**
     * Create a RegionFiller to work with the given source and destination rasters
     * @param src accessor for the source
     * @param dest accessor for the destination
     */
    FloodFiller(Raster src, WritableRaster dest) {

        srcIter = RandomIterFactory.create(src, null);
        destIter = RandomIterFactory.createWritable(dest, null);

        minSrcX = src.getMinX();
        maxSrcX = minSrcX + src.getWidth() - 1;
        minSrcY = src.getMinY();
        maxSrcY = minSrcY + src.getHeight() - 1;
    }


    /**
     * Fill the region connected to the specified pixel. A pixel belongs to
     * this region if a path can be formed from it to the starting pixel,
     * passing only through pixels with values {@code v} within the range
     * {@code v.start - tolerance <= v <= v.start + tolerance} by taking
     * unit steps vertically or horizontally
     *
     * @param x start pixel x coordinate
     * @param y start pixel y coordinate
     * @param value value to flood the region with
     * @param tolerance max absolute difference in value for a pixel to be
     * included in the region
     * @return the number of pixels filled
     */
    List<ScanSegment> floodFill(int band, int x, int y, double value, double tolerance) {
        this.band = band;
        this.fillValue = value;
        this.tolerance = tolerance;
        this.startCellValue = srcIter.getSampleDouble(x, y, band);

        pending = new LinkedList<ScanSegment>();
        filled = new ArrayList<ScanSegment>();

        totalNumFilled = 0;
        fillSegment(x, y);

        ScanSegment segment;
        ScanSegment newSegment;
        while ((segment = pending.poll()) != null) {
            
            if (segment.y > minSrcY) {
                int xi = segment.startX;
                while (xi <= segment.endX) {
                    newSegment = fillSegment(xi, segment.y - 1);
                    xi = newSegment != null ? newSegment.endX+1 : xi+1;
                }
            }
            if (segment.y < maxSrcY) {
                int xi = segment.startX; 
                while (xi <= segment.endX) {
                    newSegment = fillSegment(xi, segment.y + 1);
                    xi = newSegment != null ? newSegment.endX+1 : xi+1;
                }
            }
        }

        List<ScanSegment> newRef = filled;
        filled = null;

        return newRef;
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
     * @return the new ScanSegment created, or null if no pixels were filled
     */
    private ScanSegment fillSegment(int x, int y) {
        int numFilled = 0;
        int left = x, right = x + 1;
        ScanSegment segment = null;

        while (left >= minSrcX) {
            if (checkPixel(left, y) && !pixelDone(left, y)) {
                destIter.setSample(left, y, band, fillValue);
                numFilled++;
                left--;
            } else {
                break;
            }
        }
        left++;

        while (right <= maxSrcX) {
            if (checkPixel(right, y) && !pixelDone(right, y)) {
                destIter.setSample(right, y, band, fillValue);
                numFilled++;
                right++;
            } else {
                break;
            }
        }
        right--;

        if (numFilled > 0) {
            segment = new ScanSegment(left, right, y);
            filled.add(segment);
            pending.offer(segment);
        }

        totalNumFilled += numFilled;
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
        return DoubleComparison.dcomp(Math.abs(val - startCellValue), tolerance) <= 0;
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

        for (ScanSegment segment : filled) {
            if (segment.contains(x, y)) {
                lastSegmentChecked = segment;
                return true;
            }
        }

        lastSegmentChecked = null;
        return false;
    }
}