/*
 * Copyright 2009-2011 Michael Bedward
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

import java.util.List;

import jaitools.CollectionFactory;
import jaitools.numeric.Range;
import jaitools.numeric.RangeUtils;

/**
 * Holds a collection of source image value ranges and their corresponding
 * destination image values for the RangeLookup operation.
 * 
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions
 * @since 1.0
 * @version $Id$
 */
public class RangeLookupTable<T extends Number & Comparable<? super T>, U extends Number & Comparable<? super U>> {

    /** Value returned when lookup value is outside all ranges. */
    private U defaultValue = null;
    
    /** Whether to allow overlapping lookup ranges to be added to the table. */
    private boolean overlapAllowed;

    static class Item<T extends Number & Comparable<? super T>, U extends Number & Comparable<? super U>> {

        Range<T> srcRange;
        U destValue;

        /**
         * Constructor
         * 
         * @param srcRange a {@linkplain jaitools.media.jai.rangelookup.Range} object defining
         * a range of source image values
         *
         * @param destValue the destination image value
         */
        public Item(Range<T> srcRange, U destValue) {
            this.srcRange = srcRange;
            this.destValue = destValue;
        }

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((destValue == null) ? 0 : destValue.hashCode());
            result = prime * result
                    + ((srcRange == null) ? 0 : srcRange.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            Item other = (Item) obj;
            if (destValue == null) {
                if (other.destValue != null) {
                    return false;
                }
            } else if (!destValue.equals(other.destValue)) {
                return false;
            }
            if (srcRange == null) {
                if (other.srcRange != null) {
                    return false;
                }
            } else if (!srcRange.equals(other.srcRange)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "Item<range=" + srcRange + " value=" + destValue + ">";
        }
    }
    List<Item<T, U>> items;

    /**
     * Creates a new table with no default value. The table 
     * will throw an IllegalArgumentException if a lookup value cannot
     * be matched. It will allow ranges to be added that overlap existing 
     * ranges.
     */
    public RangeLookupTable() {
        this(null);
    }

    /**
     * Creates a new table with a default value. The table will allow
     * ranges to be added that overlap existing ranges.
     * <p>
     * If {@code defaultValue} is not {@code null} it will be returned when a
     * lookup value cannot be matched; otherwise an unmatched value results in
     * an {@code IllegalArgumentException}.
     * 
     * @param defaultValue the default destination value or {@code null} to
     *        disable the default
     */
    public RangeLookupTable(U defaultValue) {
        this(defaultValue, true);
    }
    
    /**
     * Creates a new table with specified default value and overlapping range
     * behaviour. 
     * <p>
     * If {@code defaultValue} is not {@code null} it will be returned when a
     * lookup value cannot be matched; otherwise an unmatched value results in
     * an {@code IllegalArgumentException}.
     * <p>
     * If {@code overlap} is {@code true}, adding a new lookup range that overlaps
     * existing ranges will result in the new range being reduced to its
     * non-overlapping intervals, if any; if {@code false} adding an overlapping
     * range will result in an {@code IllegalArgumentException}.
     * 
     * @param defaultValue the default destination value or {@code null} to
     *        disable the default
     * 
     * @param overlap whether to allow overlapping ranges to be added to the table
     */
    public RangeLookupTable(U defaultValue, boolean overlap) {
        items = CollectionFactory.list();
        this.defaultValue = defaultValue;
        this.overlapAllowed = overlap;
    }

    /**
     * Sets whether the table should allow a range to be added that overlaps
     * with one or more ranges in the table.
     * 
     * If allowed, a new range that overlaps one or more existing 
     * ranges will be truncated or split into non-overlapping intervals.
     * For example, if the lookup [5, 10] => 1 is already present in the table
     * and a new range [0, 20] => 2 is added, then the following lookups
     * will result:
     * <pre>
     *     [0, 5) => 2
     *     [5, 10] => 1
     *     (10, 20] => 2
     * </pre>
     * Where a new range is completely overlapped by existing ranges it will
     * be ignored.
     * <p>
     * If overlapping ranges are not allowed the table will throw an
     * {@code IllegalArgumentException} when one is detected.
     * <p>
     * 
     * Note that it is possible to end up with unintended <i>leaks</i> in the
     * lookup table. If the first range in the above example had been
     * (5, 10] rather than [5, 10] then the table would have been:
     * <pre>
     *     [0, 5) => 2
     *     (5, 10] => 1
     *     (10, 20] => 2
     * </pre>
     * In this case the value 5 will not match any range.
     * 
     * @param overlap whether to allow overlapping ranges to be added to the table
     */
    public void setOverlapAllowed(boolean b) {
        overlapAllowed = b;
    }
    
    /**
     * Checks whether it is allowable to add a range that overlaps with ranges
     * already in the table.
     * 
     * @return {@code true} if adding an overlapping range is allowed; 
     *         {@code false} otherwise
     */
    public boolean getOverlapAllowed() {
        return overlapAllowed;
    }

    /**
     * Gets the default value which is returned when a lookup
     * value is outside all ranges in the table.
     * 
     * @return the default value or {@code null} if none is set
     */
    public U getDefaultValue() {
        return defaultValue;
    }
    
    /**
     * Sets the default value to return when a lookup value is
     * outside all ranges in the table. Setting the value to 
     * {@code null} disables the default and causes the table to 
     * throw an exception when a lookup value cannot be matched.
     * 
     * @param value the default value or {@code null} to disable
     *        the default
     */
    public void setDefaultValue(U value) {
        defaultValue = value;
    }

    /**
     * Add source image value range and the corresponding destination
     * image value
     * 
     * @param range the source image value range
     * @param destValue the destination image value
     */
    public void add(Range<T> range, U destValue) {
        if (range == null || destValue == null) {
            throw new IllegalArgumentException("arguments must not be null");
        }
        
        // Check for overlap with existing ranges
        for (Item item : items) {
            if (range.intersects(item.srcRange)) {
                if (!overlapAllowed) {
                    throw new IllegalArgumentException(
                            "New range " + range + " overlaps existing lookup " + item);
                }
                
                List<Range<T>> diffs = RangeUtils.subtract(item.srcRange, range);
                for (Range<T> diff : diffs) {
                    add(diff, destValue);
                }
                return;
            }
        }

        items.add(new Item<T, U>(range, destValue));
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
    public U getDestValue(T srcValue) {
        for (Item<T, U> item : items) {
            if (item.srcRange.contains(srcValue)) {
                return item.destValue;
            }
        }

        if (defaultValue != null) {
            return defaultValue;
        } else {
            throw new IllegalArgumentException("Value cannot be matched: " + srcValue);
        }
    }

    @SuppressWarnings("rawtypes")
    @Override
    public String toString() {
        final StringBuilder buff = new StringBuilder("RangeLookupTable [defaultValue=" + defaultValue + ", items=");
        for (Item item : items) {
            buff.append("{").append(item.toString()).append("},");
        }
        buff.subSequence(0, buff.length() - 1);
        buff.append("]");
        return buff.toString();
    }
}
