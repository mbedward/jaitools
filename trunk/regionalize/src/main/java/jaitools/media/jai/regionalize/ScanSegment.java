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
 * Used by the FloodFiller and Region classes.
 * Describes a range of contiguous pixels on a single scan line
 * that form part of a region.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ScanSegment implements Comparable<ScanSegment> {

    int startX;
    int endX;
    int y;

    /**
     * Constructor
     */
    public ScanSegment(int startX, int endX, int y) {
        super();
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
