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

/**
 * Defines a range of numeric values to be used with a {@linkplain RangeLookupTable}
 * object.  This class has much less functionality than JAI's
 * {@linkplain javax.media.jai.util.Range} but has the convenience of generic
 * typing.
 *
 * @author Michael Bedward
 */
public class Range<T extends Number & Comparable> {

    private T minValue;
    private boolean minIncluded;
    private boolean isOpenLower;

    private T maxValue;
    private boolean maxIncluded;
    private boolean isOpenUpper;

    /**
     * Constructor for a range with non-zero width (may be infinite)
     *
     * @param minValue the lower bound; passing null for this parameter
     * means an open lower bound; for Float or Double types, the
     * relevant NEGATIVE_INFINITY value can also be used.
     *
     * @param minIncluded true if the lower bound is to be included in the
     * range; false to exclude the lower bound; overridden to be true if the
     * lower bound is open
     *
     * @param maxValue the upper bound; passing null for this parameter
     * means an open upper bound; for Float or Double types, the
     * relevant NEGATIVE_INFINITY value can also be used.
     *
     * @param maxIncluded true if the upper bound is to be included in the
     * range; false to exclude the upper bound; overridden to be true if the
     * upper bound is open
     */
    public Range(T minValue, boolean minIncluded, T maxValue, boolean maxIncluded) {

        if (minValue != null && maxValue != null &&
            Double.compare(minValue.doubleValue(), maxValue.doubleValue()) < 0) {
            throw new IllegalArgumentException("minValue greater than maxValue");
        }


        if (minValue == null ||
            (minValue instanceof Double && Double.isInfinite(minValue.doubleValue())) ||
            (minValue instanceof Float && Float.isInfinite(minValue.floatValue()))) {
            this.minValue = null;
            this.isOpenLower = true;
            this.minIncluded = true;
        } else {
            this.minValue = minValue;
            this.isOpenLower = false;
            this.minIncluded = minIncluded;
        }

        if (maxValue == null ||
            (maxValue instanceof Double && Double.isInfinite(maxValue.doubleValue())) ||
            (maxValue instanceof Float && Float.isInfinite(maxValue.floatValue()))) {
            this.maxValue = null;
            this.isOpenUpper = true;
            this.maxIncluded = true;
        } else {
            this.maxValue = maxValue;
            this.isOpenUpper = false;
            this.maxIncluded = maxIncluded;
        }
    }

    /**
     * Constructor for a range that is just a single value
     *
     * @param value the value
     */
    public Range(T value) {
        this.maxValue = this.maxValue = value;
        this.maxIncluded = this.maxIncluded = true;
    }

    /**
     * Query whether this range contains the given value
     * @param val the value
     * @return true if contained within this range; false otherwise
     */
    public boolean contains(T val) {
        int compLower, compUpper;

        if (isOpenLower) {
            if (isOpenUpper) {
                return true;
            }

            compLower = 1;

        } else {
            compLower = val.compareTo(minValue);
        }

        if (minIncluded) {
            if (compLower < 0) return false;
        } else {
            if (compLower <= 0) return false;
        }

        if (isOpenUpper) {
            compUpper = -1;
        } else {
            compUpper = val.compareTo(maxValue);
        }

        if (maxIncluded) {
            if (compUpper > 0) return false;
        } else {
            if (compUpper >= 0) return false;
        }

        return true;
    }

}
