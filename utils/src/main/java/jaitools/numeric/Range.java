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
package jaitools.numeric;


/**
 * Yet another numeric interval class. It is used with the {@code RangeLookup} operator but
 * is also available for general use and does not depend on JAI classes.
 * <p>
 * {@code Range} is a generic class that can be used all {@code Number} types. A range
 * is defined by its endpoints. If the min (lower bound) end-point is the same as the
 * max (upper-bound) end-point then the range represents a point, also referred to as
 * a degenerate interval.
 * <p>
 * An end-point can be positioned at a finite value, in which case it is said to be closed,
 * or at positive or negative infinity, in which case it is said to be open. For finite
 * end-points, a range can include or exclude the end-point. Included end-points correspond
 * to square-brackets in interval notation, while excluded end-points correspond to round
 * brackets.
 * <p>
 * The following rules apply to Float and Double NaN values...
 * <ul>
 * <li> Float and Double NaN are considered equal
 * <li> Point intervals can have value NaN
 * <li> Proper intervals treat NaN at their lower end-point as negative infinity and at
 *      their upper end-point as positive infinity
 * <li> A test value of NaN can be equal to a point interval
 * <li> A test value of NaN will always be treated as outside a proper interval
 * </ul>
 *
 * @see RangeComparator
 * @see jaitools.media.jai.rangelookup.RangeLookupDescriptor
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Range<T extends Number & Comparable> {

    /**
     * This enum is used by jai-tools statistics classes to specify
     * whether a {@code Range} parameter defines included or excluded
     * values. It is defined within the {@code Range} class for
     * convenience.
     * <p>
     * It provides the constants INCLUDE, EXCLUDE and UNDEFINED.
     */
    public static enum Type {
        /** The range defines values to include in an operation */
        INCLUDE,

        /** The range defines values to exclude in an operation */
        EXCLUDE,

        /** The application of the range is undefined */
        UNDEFINED
    }

    /**
     * Integer flag value for negative infinity. Can be used as an
     * argument to the {@linkplain #Range} constructor for point
     * (degenerate) intervals.
     */
    public static final int NEG_INF = -1;

    /**
     * Integer flag value for positive infinity. Can be used as an
     * argument to the {@linkplain #Range} constructor for point
     * (degenerate) intervals.
     */
    public static final int INF = 1;

    /**
     * Integer flag to indicate a finite argument. For internal use
     * and by RangeUtils.
     */
    static final int FINITE = 0;

    private static final int NAN = -9999;

    private T minValue;
    private boolean minIncluded;
    private boolean minOpen;
    private int minType;
    private T maxValue;
    private boolean maxIncluded;
    private boolean maxOpen;
    private int maxType;
    private boolean isPoint;


    /**
     * Static create method. This just relieves the tedium of having to specify
     * the type parameter on both sides of the creation expression. So instead
     * of this...
     * <pre><code>
     *
     *    Range&lt;Integer> r = new Range&lt;Integer>(10, false, 20, true);
     * </code></pre>
     * you can do this...
     * <pre><code>
     *
     *    Range&lt;Integer> r = Range.create(10, false, 20, true);
     * </code></pre>
     * which is infinitesimally better.
     *
     * @param minValue the lower bound; passing null for this parameter
     * means an open lower bound; for Float or Double types, the
     * relevant NEGATIVE_INFINITY value can also be used.
     *
     * @param minIncluded true if the lower bound is to be included in the
     * range; false to exclude the lower bound; overridden to be false if the
     * lower bound is open
     *
     * @param maxValue the upper bound; passing null for this parameter
     * means an open upper bound; for Float or Double types, the
     * relevant NEGATIVE_INFINITY value can also be used.
     *
     * @param maxIncluded true if the upper bound is to be included in the
     * range; false to exclude the upper bound; overridden to be false if the
     * upper bound is open
     *
     * @return a new Range object with the specified type and bounds
     */
    public static <U extends Number & Comparable> Range<U> create(U minValue, boolean minIncluded, U maxValue, boolean maxIncluded) {
        return new Range<U>(minValue, minIncluded, maxValue, maxIncluded);
    }

    /**
     * Create a Range object that is a point (degenerate) interval with its (equal)
     * min and max end-points set to value. If value is null, for a point at infinity,
     * the optional inf argument must be provided and be one of Range.INF or Range.NEG_INF.
     *
     * @param value the value to set for both min and max end-points
     * @param inf either Range.INF or Range.NEG_INF (ignored if present when value is non-null)
     */
    public static <U extends Number & Comparable> Range<U> create(U value, int... inf) {
        return new Range<U>(value, inf);
    }

    /**
     * Constructor for a range with non-zero width (may be infinite)
     *
     * @param minValue the lower bound; passing null for this parameter
     * means an open lower bound; for Float or Double types, the
     * relevant NEGATIVE_INFINITY value can also be used.
     *
     * @param minIncluded true if the lower bound is to be included in the
     * range; false to exclude the lower bound; overridden to be false if the
     * lower bound is open
     *
     * @param maxValue the upper bound; passing null for this parameter
     * means an open upper bound; for Float or Double types, the
     * relevant NEGATIVE_INFINITY value can also be used.
     *
     * @param maxIncluded true if the upper bound is to be included in the
     * range; false to exclude the upper bound; overridden to be false if the
     * upper bound is open
     */
    public Range(T minValue, boolean minIncluded, T maxValue, boolean maxIncluded) {

        if (minValue != null && maxValue != null && minValue.compareTo(maxValue) > 0) {
            throw new IllegalArgumentException("minValue greater than maxValue");
        }

        minType = FINITE;

        if (minValue == null) {
            // assume negative infinity
            minType = NEG_INF;

        } else if (minValue instanceof Double) {
            if (Double.isInfinite(minValue.doubleValue())) {
                if (Double.compare(minValue.doubleValue(), Double.POSITIVE_INFINITY) == 0) {
                    minType = INF;
                } else {
                    minType = NEG_INF;
                }
            } else if (Double.isNaN(minValue.doubleValue())) {
                // assume negative infinity
                minType = NEG_INF;
            }

        } else if (minValue instanceof Float) {
            if (Float.isInfinite(minValue.floatValue())) {
                if (Float.compare(minValue.floatValue(), Float.POSITIVE_INFINITY) == 0) {
                    minType = INF;
                } else {
                    minType = NEG_INF;
                }
            } else if (Float.isNaN(minValue.floatValue())) {
                // assume negative infinity
                minType = NEG_INF;
            }
        }

        if (minType == FINITE) {
            this.minValue = minValue;
            this.minOpen = false;
            this.minIncluded = minIncluded;
        } else {
            this.minValue = null;
            this.minOpen = true;
            this.minIncluded = false;
        }

        maxType = FINITE;

        if (maxValue == null) {
            // assume positive infinity
            maxType = INF;

        } else if (maxValue instanceof Double) {
            if (Double.isInfinite(maxValue.doubleValue())) {
                if (Double.compare(maxValue.doubleValue(), Double.POSITIVE_INFINITY) == 0) {
                    maxType = INF;
                } else {
                    maxType = NEG_INF;
                }
            } else if (Double.isNaN(maxValue.doubleValue())) {
                // assume positive infinity
                maxType = INF;
            }
        } else if (maxValue instanceof Float) {
            if (Float.isInfinite(maxValue.floatValue())) {
                if (Float.compare(maxValue.floatValue(), Float.POSITIVE_INFINITY) == 0) {
                    maxType = INF;
                } else {
                    maxType = NEG_INF;
                }
            } else if (Float.isNaN(maxValue.floatValue())) {
                // assume positive infinity
                maxType = INF;
            }
        }

        if (maxType == FINITE) {
            this.maxValue = maxValue;
            this.maxOpen = false;
            this.maxIncluded = maxIncluded;
        } else {
            this.maxValue = null;
            this.maxOpen = true;
            this.maxIncluded = false;
        }

        /*
         * Check if we have a point range trying to sneak in via
         * the interval constructor
         */
        if (minType == FINITE && maxType == FINITE) {
            if (minValue == maxValue) {
                if (minIncluded && maxIncluded) {
                    isPoint = true;
                } else {
                    throw new IllegalArgumentException(
                            "point range created with the interval constructor must have " +
                            "min and max endpoints included");
                }
            }

        } else if (minType == maxType) { // both at +ve or -ve Inf
            isPoint = true;

        } else if (minType == INF &&  maxType == NEG_INF) { // Inf endpoints wrong way round
            throw new IllegalArgumentException(
                    "invalid to have min endpoint at Inf  and max endpoint at Neg Inf");
        }

    }


    /**
     * Constructor for a point (degenerate) range.
     * <p>
     * Where value is finite the following apply:
     * <ul>
     * <li> min value is equal to max value
     * <li> upper and lower bounds are treated as closed
     * <li> upper and lower bounds are treated as included
     * </ul>
     *
     * If the type parameter T is Float or Double then value can be
     * set to the relevant POSITIVE_INFINITY or NEGATIVE_INFINITY. For
     * other types such as Integer which lack infinity flag values a
     * point range at infinity can be created as in this example...
     * <pre><code>
     *
     *    Range&lt;Integer> rInf = new Range&lt;Integer>(null, Range.INF);
     *    Range&lt;Integer> rNegInf = new Range&lt;Integer>(null, Range.NEG_INF);
     *
     *    // or with the static create method...
     *    Range&lt;Integer> rInf2 = Range.create(null, Range.INF);
     * </code></pre>
     * <p>
     * For a point interval at positive or negative infinity the following apply:
     * <ul>
     * <li> getting the min or max value of the range will return null
     * <li> the upper and lower bounds are treated as open
     * <li> the upper and lower bounds are treated as not included (ie. an
     *      infinite point does not contain itself !)
     * </ul>
     *
     * @param value the value to set for both min and max end-points
     * @param inf either Range.INF or Range.NEG_INF (ignored if present when value is non-null)
     */
    public Range(T value, int... inf) {
        isPoint = true;
        int bound = FINITE;

        if (value == null) {
            if (inf == null || inf.length < 1 || !(inf[0] == INF || inf[0] == NEG_INF)) {
                throw new IllegalArgumentException("one of BOUND_INF or BOUND_NEG_INF must be provided with a null value");
            }

            bound = inf[0];

        } else if (value instanceof Double) {
            if (Double.isInfinite(value.doubleValue())) {
                if (Double.compare(value.doubleValue(), Double.POSITIVE_INFINITY) == 0) {
                    bound = INF;
                } else {
                    bound = NEG_INF;
                }
            } else if (Double.isNaN(value.doubleValue())) {
                bound = NAN;
            }
        } else if (value instanceof Float) {
            if (Float.isInfinite(value.floatValue())) {
                if (Float.compare(value.floatValue(), Float.POSITIVE_INFINITY) == 0) {
                    bound = INF;
                } else {
                    bound = NEG_INF;
                }
            } else if (Float.isNaN(value.floatValue())) {
                bound = NAN;
            }
        }

        if (bound == FINITE) {
            this.maxValue = this.minValue = value;
            this.maxType = this.minType = FINITE;
            this.maxOpen = this.minOpen = false;
            this.maxIncluded = this.minIncluded = true;

        } else {  // point at +ve or -ve infinity
            this.maxValue = this.minValue = null;
            this.maxType = this.minType = bound;
            this.minOpen = this.maxOpen = true;
            /*
             * While it seems strange to have a point that doesn't
             * include itself, setting minIncluded and maxIncluded to
             * true would be inconsistent with treatment for proper
             * intervals (and perhaps illogical)
             */
            this.maxIncluded = this.minIncluded = false;
        }
    }

    /**
     * Copy constructor.
     */
    public Range(Range<T> other) {
        this.minValue = other.minValue;
        this.minIncluded = other.minIncluded;
        this.minOpen = other.minOpen;
        this.minType = other.minType;
        this.maxValue = other.maxValue;
        this.maxIncluded = other.maxIncluded;
        this.maxOpen = other.maxOpen;
        this.maxType = other.maxType;
        this.isPoint = other.isPoint;
    }

    /**
     * Check if this range is a point (degenerate) interval
     */
    public boolean isPoint() {
        return isPoint;
    }

    /**
     * Get the minimum value for this range. The minimum value is
     * not necessarily included in the range (see {@linkplain #isMinIncluded()}).
     * <p>
     * <b>Caution:</b> If the range is lower-open
     * (ie. {@linkplain #isMinOpen()} returns true) this method will
     * return null.
     *
     * @return the minimum value or null if this range is lower-open
     */
    public T getMin() {
        return minValue;
    }

    /**
     * Query if the minimum value is positive infinity. If so, the range
     * must be a point (degenerate) interval.
     */
    public boolean isMinInf() {
        return minType == INF;
    }

    /**
     * Query if the minimum value is negative infinity. This is equivalent
     * to {@linkplain #isMinOpen()}
     */
    public boolean isMinNegInf() {
        return minType == NEG_INF;
    }

    /**
     * Get the maximum value for this range. The maximum value is
     * not necessarily included in the range (see {@linkplain #isMaxIncluded()}).
     * <p>
     * <b>Caution:</b> If the range is upper-open
     * (ie. {@linkplain #isMaxOpen()} returns true) this method will
     * return null.
     *
     * @return the maximum value or null if this range is upper-open
     */
    public T getMax() {
        return maxValue;
    }

    /**
     * Query if the maximum value is positive infinity. This is equivalent
     * to {@linkplain #isMaxOpen()}
     */
    public boolean isMaxInf() {
        return maxType == INF;
    }

    /**
     * Query if the maximum value is negative infinity. If so, the range
     * must be a point (degenerate) interval.
     */
    public boolean isMaxNegInf() {
        return maxType == NEG_INF;
    }

    /**
     * Query if the minimum range value is included in the range.
     * This will return false if the range has an unbounded
     * minimum.
     * @see #isMinOpen()
     * @see #isMinClosed()
     */
    public boolean isMinIncluded() {
        return minIncluded;
    }

    /**
     * Query if the maximum range value is included in the range.
     * This will return false if the range has an unbounded
     * maximum.
     * @see #isMaxOpen
     * @see #isMaxClosed
     */
    public boolean isMaxIncluded() {
        return maxIncluded;
    }

    /**
     * Return true if this range has an unbounded minimum value, ie.
     * the range extends to negative infinity.
     * @see #isMinIncluded()
     */
    public boolean isMinOpen() {
        return minOpen;
    }

    /**
     * Return true if this range has a bounded minimum value.
     * @see #isMinIncluded()
     */
    public boolean isMinClosed() {
        return !minOpen;
    }

    /**
     * Return true if this range has unbounded upper end, ie the
     * range extends to positive infinity.
     * @see #isMaxIncluded()
     */
    public boolean isMaxOpen() {
        return maxOpen;
    }

    /**
     * Return true if this range has a bounded maximum value.
     * @see #isMaxIncluded()
     */
    public boolean isMaxClosed() {
        return !maxOpen;
    }

    /**
     * Query if this range contains the specified, non-null value
     */
    public boolean contains(T value) {
        if (value == null) {
            throw new UnsupportedOperationException("null values are not supported");
        }

        if (isPoint) {
            if (minType == FINITE) {
                return NumberOperations.compare(minValue, value) == 0;

            } else if (minType == NAN) {
                if (value instanceof Double) {
                    return Double.isNaN(value.doubleValue());
                } else {
                    return Float.isNaN(value.floatValue());
                }

            } else {
                return false;
            }

        } else {
            // NaN values are always outside a proper interval
            if ((value instanceof Double && Double.isNaN(value.doubleValue())) ||
                (value instanceof Float && Float.isNaN(value.floatValue()))) {
                return false;
            }
            
            int comp;
            if (minValue != null) {
                comp = NumberOperations.compare(value, minValue);
                if (comp < 0 || (!minIncluded && comp == 0)) {
                    return false;
                }
            }

            if (maxValue != null) {
                comp = NumberOperations.compare(value, maxValue);
                if (comp > 0 || (!maxIncluded && comp == 0)) {
                    return false;
                }
            }
            
            return true;
        }
    }

    /**
     * Query if this range intersects another range. The two ranges intersect if
     * there is at least one value, x, for which contains(x) returns true
     * for both ranges
     *
     * @param other the range to check for intersection
     * @return true if this range intersects with other; false otherwise
     */
    public boolean intersects(Range<T> other) {
        RangeComparator.Result comp = this.compareTo(other);
        return RangeComparator.isIntersection(comp);
    }

    /**
     * Test for equality.
     *
     * @param obj the other {@code Range} object
     *
     * @return true if this instance has the same end point values and type
     *         (included / excluded) as the other instance
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }

        final Range<T> other = (Range<T>) obj;

        // need this to guard against incorrect matching of infinite points
        // with the whole number line
        if (this.isPoint != other.isPoint) {
            return false;
        }
        if (this.minValue != other.minValue && (this.minValue == null || !this.minValue.equals(other.minValue))) {
            return false;
        }
        if (this.minIncluded != other.minIncluded) {
            return false;
        }
        if (this.maxValue != other.maxValue && (this.maxValue == null || !this.maxValue.equals(other.maxValue))) {
            return false;
        }
        if (this.maxIncluded != other.maxIncluded) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 11 * hash + (this.minValue != null ? this.minValue.hashCode() : 0);
        hash = 11 * hash + (this.minIncluded ? 1 : 0);
        hash = 11 * hash + (this.maxValue != null ? this.maxValue.hashCode() : 0);
        hash = 11 * hash + (this.maxIncluded ? 1 : 0);
        return hash;
    }

    /**
     * Returns a string representation of this {@code Range}.
     * <p>
     * Examples of output:
     * <blockquote>
     * Range[10, 20] <br>
     * Range[-Inf, 42)
     * </blockquote>
     *
     * @return this range as a String
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Range");

        if (isMinClosed()) {
            sb.append(isMinIncluded() ? '[' : '(');
            sb.append(getMin());
        } else {
            if (isPoint()) {
                sb.append(isMinInf() ? "(Inf" : "(-Inf");
            } else {
                sb.append("(-Inf");
            }
        }

        if (isPoint()) {
            sb.append(isMinIncluded() ? ']' : ')');
        } else {
            sb.append(", ");
            if (isMaxClosed()) {
                sb.append(getMax());
                sb.append(isMaxIncluded() ? ']' : ')');
            } else {
                sb.append("Inf)");
            }
        }

        return sb.toString();
    }

    /**
     * Compare this Range to another Range.  Range comparisons are more involved
     * than comparisons between point values.  There are 18 distinct comparison
     * results as described by Hayes (2003). See {@linkplain RangeComparator} for
     * more details.
     *
     * @param other the other Range
     * @return a RangeComparator.Result enum identifying the relationship from the
     * point of view of this Range
     */
    RangeComparator.Result compareTo(Range<T> other) {
        RangeComparator<T> rc = new RangeComparator<T>();
        return rc.compare(this, other);
    }
}



