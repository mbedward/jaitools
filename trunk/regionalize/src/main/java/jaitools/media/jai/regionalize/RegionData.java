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

/**
 * Holds summary data for regions in an image generated with the
 * Regionalize operation.
 *
 * @see RegionalizeDescriptor
 * @author Michael Bedward
 */
public class RegionData {

    /**
     * Holds data for a single region
     */
    public static class RegionRec {
        private int id;
        private int numPixels;
        private double value;
        private int minx;
        private int maxx;
        private int miny;
        private int maxy;
        
        /**
         * Package-private constructor
         */
        RegionRec(Region region) {
            this.id = region.getID();
            this.value = region.getValue();
            this.minx = region.getMinX();
            this.miny = region.getMinY();
            this.maxx = region.getMaxX();
            this.maxy = region.getMaxY();
            this.numPixels = region.getNumPixels();
        }

        /**
         * Get the region ID
         */
        public int getId() {
            return id;
        }

        /**
         * Get the max x coordinate of pixels within this region
         */
        public int getMaxx() {
            return maxx;
        }

        /**
         * Get the max y coordinate of pixels within this region
         */
        public int getMaxy() {
            return maxy;
        }

        /**
         * Get the min x coordinate of pixels within this region
         */
        public int getMinx() {
            return minx;
        }

        /**
         * Get the min y coordinate of pixels within this region
         */
        public int getMiny() {
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
        public double getValue() {
            return value;
        }
    }

    private List<RegionRec> recs = CollectionFactory.newList();

    /**
     * Package private method to add data for a region
     */
    void addRegion(Region region) {
        recs.add(new RegionRec(region));
    }

    /**
     * Get the data for regions as an unmodifiable List of
     * {@linkplain RegionRec} objects.
     * @return
     */
    public List<RegionRec> getData() {
        return Collections.unmodifiableList(recs);
    }
}
