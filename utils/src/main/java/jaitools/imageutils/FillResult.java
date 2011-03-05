/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.imageutils;

import jaitools.CollectionFactory;
import java.awt.Rectangle;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * This class is used by the {@code RegionalizeOpImage} and {@code FloodFiller}
 * to record data describing an image region that has just been flood-filled.
 * <p>
 * Although public, it is not intended for general use unless you are modifying
 * or sub-classing the flood fill classes.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class FillResult {
    private int id;
    private double value;
    private Rectangle bounds;

    private List<FloodFiller.ScanSegment> segments;
    private Map<Integer, List<Integer>> index;
    private int numPixels;

    /**
     * Constructor. Takes ownership of the list of segments.
     * @param id unique ID assigned to this region
     * @param value representative value of pixels in this region
     * @param segments list of line segments making up this region
     */
    public FillResult(int id, double value, List<FloodFiller.ScanSegment> segments) {
        this.id = id;
        this.value = value;
        this.segments = segments;
        this.index = CollectionFactory.map();

        if (segments.isEmpty()) {
            numPixels = 0;
            bounds = new Rectangle();

        } else {
            Collections.sort(segments);

            FloodFiller.ScanSegment segment = segments.get(0);
            int minx = segment.startX;
            int maxx = segment.endX;
            int miny = segment.y;
            int maxy = segment.y;

            numPixels = segment.endX - segment.startX + 1;
            addToIndex(segment, 0);

            if (segments.size() > 1) {
                ListIterator<FloodFiller.ScanSegment> iter = segments.listIterator(1);
                int k = 1;
                while (iter.hasNext()) {
                    segment = iter.next();
                    maxy = segment.y;
                    if (segment.startX < minx) {
                        minx = segment.startX;
                    }
                    if (segment.endX > maxx) {
                        maxx = segment.endX;
                    }

                    numPixels += (segment.endX - segment.startX + 1);
                    addToIndex(segment, k++);
                }
            }

            bounds = new Rectangle(minx, miny, maxx - minx + 1, maxy - miny + 1);
        }
    }

    /**
     * Checks if this region contains the given location.
     * 
     * @param x X ordinate
     * @param y Y ordinate
     * @return {@code true} if the region contains the location; 
     *         {@code false} otherwise
     */
    public boolean contains(int x, int y) {
        if (!bounds.contains(x, y)) {
            return false;
        }

        List<Integer> indices = index.get(y);
        for (Integer i : indices) {
            FloodFiller.ScanSegment segment = segments.get(i);
            if (segment.contains(x, y)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Merges the given region into this region.
     * <p>
     * 
     * At present, this method doesn't bother about merging scan segments,
     * it just adds the other region's segments and updates the index
     * and bounds as necessary.
     * 
     * @param other other region
     */
    public void expand(FillResult other) {
        bounds = bounds.union(other.bounds);

        for (FloodFiller.ScanSegment otherSeg : other.segments) {
            segments.add(otherSeg);
            addToIndex(otherSeg, segments.size()-1);
        }

        numPixels += other.numPixels;
    }

    /**
     * Gets the ID of this region.
     * 
     * @return integer ID
     */
    public int getID() {
        return id;
    }

    /**
     * Gets the bounds of this region.
     * 
     * @return a new rectangle
     */
    public Rectangle getBounds() {
        return new Rectangle(bounds);
    }

    /**
     * Gets the number of pixels within this region.
     * 
     * @return number of pixels
     */
    public int getNumPixels() {
        return numPixels;
    }

    /**
     * Gets the reference value of this region.
     * This is the value of the start pixel used in the regionalize
     * operation.
     * 
     * @return reference value
     */
    public double getValue() {
        return value;
    }

    /**
     * Adds a segment to the index. This is used to improve the performance
     * of the {@linkplain #contains(int, int) } method.
     * 
     * @param segment the segment to add
     * @param segmentListPos insertion position
     */
    private void addToIndex(FloodFiller.ScanSegment segment, int segmentListPos) {
        List<Integer> indices = index.get(segment.y);
        if (indices == null) {
            indices = CollectionFactory.list();
            index.put(segment.y, indices);
        }
        indices.add(segmentListPos);
    }

}
