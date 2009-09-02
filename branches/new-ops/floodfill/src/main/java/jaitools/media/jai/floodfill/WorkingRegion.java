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

package jaitools.media.jai.floodfill;

import jaitools.utils.CollectionFactory;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Holds data for a single region sa it is extracted from the source image.
 * The data may come in several batches depending on whether the region
 * crosses image tile boundaries.
 * <p>
 * Note: this class is used internally by the operator (specifically by
 * {@code RegionalizeOpImage} and {@code FloodFiller}) and is not intended
 * for use by client code.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL: https://jai-tools.googlecode.com/svn/trunk/operator/regionalize/src/main/java/jaitools/media/jai/regionalize/WorkingRegion.java $
 * @version $Id: WorkingRegion.java 543 2009-08-27 06:14:09Z michael.bedward $
 */
public class WorkingRegion {
    private int id;
    private double value;
    private int minx, maxx, miny, maxy;

    private List<ScanSegment> segments;
    private Map<Integer, List<Integer>> index;
    private int numPixels;

    /**
     * Constructor. Takes ownership of the list of segments.
     * @param id unique ID assigned to this region
     * @param value representative value of pixels in this region
     * @param segments list of line segments making up this region
     */
    public WorkingRegion(int id, double value, List<ScanSegment> segments) {
        this.id = id;
        this.value = value;
        this.segments = segments;
        this.index = CollectionFactory.newMap();
        
        Collections.sort(segments);

        ScanSegment segment = segments.get(0);
        minx = segment.startX;
        maxx = segment.endX;
        miny = segment.y;
        maxy = segment.y;

        numPixels = segment.endX - segment.startX + 1;
        addToIndex(segment, 0);

        if (segments.size() > 1) {
            ListIterator<ScanSegment> iter = segments.listIterator(1);
            int k = 1;
            while (iter.hasNext()) {
                segment = iter.next();
                maxy = segment.y;
                if (segment.startX < minx) minx = segment.startX;
                if (segment.endX > maxx) maxx = segment.endX;

                numPixels += (segment.endX - segment.startX + 1);
                addToIndex(segment, k++);
            }
        }
    }

    /**
     * Check if this region contains the given pixel coords
     */
    public boolean contains(int x, int y) {
        if (x < minx || x > maxx || y < miny || y > maxy) {
            return false;
        }

        List<Integer> indices = index.get(y);
        for (Integer i : indices) {
            ScanSegment segment = segments.get(i);
            if (segment.contains(x, y)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Package-private method. Merge the given region into this region.
     * At present, this method doesn't bother about merging scan segments,
     * it just addes the other region's segments and updates the index
     * and bounds as necessary.
     */
    public void expand(WorkingRegion cor) {
        for (ScanSegment otherSeg : cor.segments) {
            if (otherSeg.startX < minx) minx = otherSeg.startX;
            if (otherSeg.endX > maxx) maxx = otherSeg.endX;
            if (otherSeg.y < miny) {
                miny = otherSeg.y;
            } else if (otherSeg.y > maxy) {
                maxy = otherSeg.y;
            }

            segments.add(otherSeg);
            addToIndex(otherSeg, segments.size()-1);
        }
    }

    /**
     * Package-private method. Get the ID of this region.
     */
    public int getID() {
        return id;
    }

    /**
     * Package-private method. Get the max x coordinate within this region.
     */
    public int getMaxX() {
        return maxx;
    }

    /**
     * Package-private method. Get the max y coordinate of this region.
     */
    public int getMaxY() {
        return maxy;
    }

    /**
     * Package-private method. Get the min x coordinate of this region.
     */
    public int getMinX() {
        return minx;
    }

    /**
     * Package-private method. Get the min y coordinate of this region.
     */
    public int getMinY() {
        return miny;
    }

    /**
     * Package-private method. Get the number of pixels within this region.
     */
    public int getNumPixels() {
        return numPixels;
    }

    /**
     * Package-private method. Get the reference value of this region.
     * This is the value of the start pixel used in the regionalize
     * operation.
     */
    public double getValue() {
        return value;
    }

    /**
     * Add a segment to the index. This is to improve the performance
     * of the {@linkplain #contains(int, int) } method
     */
    private void addToIndex(ScanSegment segment, int segmentListPos) {
        List<Integer> indices = index.get(segment.y);
        if (indices == null) {
            indices = CollectionFactory.newList();
            index.put(segment.y, indices);
        }
        indices.add(segmentListPos);
    }

}
