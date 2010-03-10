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

package jaitools.media.jai.rangelookup;

import jaitools.numeric.Range;
import java.util.ArrayList;
import java.util.List;

/**
 * Holds a collection of source image value ranges and their corresponding
 * destination image values for the RangeLookup operation.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class RangeLookupTable<T extends Number & Comparable> {

    T defaultValue = null;

    private static class Item<T extends Number & Comparable> {
        Range<T> srcRange;
        T destValue;

        /**
         * Constructor
         * 
         * @param srcRange a {@linkplain jaitools.media.jai.rangelookup.Range} object defining
         * a range of source image values
         *
         * @param destValue the destination image value
         */
        public Item(Range<T> srcRange, T destValue) {
            this.srcRange = srcRange;
            this.destValue = destValue;
        }
    }

    List<Item<T>> items;

    /**
     * Constructor that provides for a default value which will be written
     * to the destination image if a source image value is not contained
     * in any of the ranges held by this table. The exepction is if the
     * defaultValue is set to null, in which case the table will have the
     * same behaviour as if it was created with the no-argument constructor.
     *
     * @param defaultValue the default destination value
     */
    public RangeLookupTable(T defaultValue) {
        items = new ArrayList<Item<T>>();
        this.defaultValue = defaultValue;
    }

    /**
     * Constructor with no default value. A table created with this
     * constructor will throw a IllegalStateException if it is
     * asked to lookup a source image value that is not contained
     * within any of the ranges that it holds.
     */
    public RangeLookupTable() {
        items = new ArrayList<Item<T>>();
    }

    /**
     * Add source image value range and the corresponding destination
     * image value
     * 
     * @param range the source image value range
     * @param destValue the destination image value
     */
    public void add(Range<T> range, T destValue) {
        if (range == null || destValue == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }

        items.add(new Item(range, destValue));
    }

    /**
     * Lookup a source image value and return the corresponding destination image value
     *
     * @param srcValue source image value
     * @return destination image value
     *
     * @throws IllegalStateException if the source image value is not contained in any
     * of the ranges held by this table and the table was created without a default
     * destination image value
     */
    public T getDestValue(T srcValue) {
        for (Item<T> item : items) {
            if (item.srcRange.contains(srcValue)) {
                return item.destValue;
            }
        }

        if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new IllegalStateException("value outside all ranges: " + srcValue);
        }
    }
}
