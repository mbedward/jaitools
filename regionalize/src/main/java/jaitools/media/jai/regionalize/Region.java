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

import jaitools.utils.CollectionFactory;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * Data class for a single region extracted from the source image
 *
 * @author Michael Bedward
 */
class Region {
    private int id;
    private double value;
    private int minx, maxx, miny, maxy;

    private List<ScanSegment> segments;
    private Map<Integer, List<Integer>> index;

    /**
     * Constructor. Takes ownership of the list of segments.
     * @param id unique ID assigned to this region
     * @param value representative value of pixels in this region
     * @param segments list of line segments making up this region
     */
    Region(int id, double value, List<ScanSegment> segments) {
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

        addToIndex(segment, 0);

        if (segments.size() > 1) {
            ListIterator<ScanSegment> iter = segments.listIterator(1);
            int k = 1;
            while (iter.hasNext()) {
                segment = iter.next();
                maxy = segment.y;
                if (segment.startX < minx) minx = segment.startX;
                if (segment.endX > maxx) maxx = segment.endX;

                addToIndex(segment, k++);
            }
        }
    }

    /**
     * Check if this region contains the given pixel coords
     */
    boolean contains(int x, int y) {
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
