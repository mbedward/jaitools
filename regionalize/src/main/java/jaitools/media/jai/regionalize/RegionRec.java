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
 * Holds summary data for a single region
 */
public class RegionRec {

    private int id;
    private int numPixels;
    private double value;
    private int minx;
    private int maxx;
    private int miny;
    private int maxy;

    /**
     * Constructor is package-private.
     */
    RegionRec(RegionZZZ region) {
        this.id = region.getID();
        this.value = region.getValue();
        this.minx = region.getMinX();
        this.miny = region.getMinY();
        this.maxx = region.getMaxX();
        this.maxy = region.getMaxY();
        this.numPixels = region.getNumPixels();
    }

    /**
     * Get the unique integer ID of this region
     */
    public int getId() {
        return id;
    }

    /**
     * Get the max x coordinate of pixels within this region
     */
    public int getMaxX() {
        return maxx;
    }

    /**
     * Get the max y coordinate of pixels within this region
     */
    public int getMaxY() {
        return maxy;
    }

    /**
     * Get the min x coordinate of pixels within this region
     */
    public int getMinX() {
        return minx;
    }

    /**
     * Get the min y coordinate of pixels within this region
     */
    public int getMinY() {
        return miny;
    }

    /**
     * Get the number of pixels within this region
     */
    public int getNumPixels() {
        return numPixels;
    }

    /**
     * Get the reference value for this region as a double.
     * This is the value of the first pixel that was identified
     * within the region.
     */
    public double getRefValue() {
        return value;
    }
}
