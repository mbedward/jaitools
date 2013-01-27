/* 
 *  Copyright (c) 2010-2013, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   
package org.jaitools.numeric;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Provides static helper methods to transform, sort and merge {@code Range} objects.
 *
 * @author Michael Bedward
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @version $Id$
 */
public class RangeUtils {


    /**
     * Creates the complement of a {@code Range}. This is equivalent to subtracting the
     * input from the infinite interval.
     * <p>
     * If the input is a finite interval or point, the result will be a list of
     * two {@code Ranges}. For example: the complement of [-5, 5) is made up of
     * (-Inf, -5) and [5, Inf).
     * <p>
     * If the input is an interval open at one end, the result list will contain
     * a single {@code Range}. For example: the complement of (-Inf, 5) is [5, Inf).
     * <p>
     * If the input is a point at positive or negative infinity its complement is,
     * by convention, (-Inf, Inf).
     * <p>
     * If the input is the infinite interval (-Inf, Inf) the result list will be
     * empty.
     *
     * @param <T> the value type
     * @param range input range
     * 
     * @return a list of 0, 1 or 2 {@code Ranges} which form the complement
     *
     * @see #createComplement(java.util.Collection)
     */
    public static <T extends Number & Comparable> List<Range<T>> createComplement(Range<T> range) {
        return subtract(range, new Range<T>(null, false, null, false));
    }

    /**
     * Creates the complement of the given list of {@code Ranges}. This method first
     * calls {@linkplain #simplify} on the inputs and then subtracts each of the
     * resulting {@code Ranges} from the whole number line.
     *
     * @param <T> value type
     * @param ranges input ranges
     *
     * @return a list of {@code Ranges} which form the complement (may be empty)
     *
     * @see #createComplement(Range)
     */
    public static <T extends Number & Comparable> List<Range<T>> createComplement(Collection<Range<T>> ranges) {
        List<Range<T>> inputs = simplify(ranges);
        List<Range<T>> complements = new ArrayList<Range<T>>();

        /*
         * Start with the whole number line, then subtract each of
         * the input ranges from it
         */
        complements.add(new Range<T>(null, true, null, true));

        for (int i = 0; i < inputs.size(); i++) {
            boolean changed = false;
            Range<T> rin = inputs.get(i);
            for (int j = 0; j < complements.size() && !changed; j++) {
                Range<T> rc = complements.get(j);
                List<Range<T>> diff = subtract(rin, rc);
                final int ndiff = diff.size();
                switch (ndiff) {
                    case 0:
                        complements.remove(j);
                        changed = true;
                        break;

                    case 1:
                        if (!diff.get(0).equals(rc)) {
                            complements.remove(j);
                            complements.add(diff.get(0));
                            changed = true;
                        }
                        break;

                    case 2:
                        complements.remove(j);
                        complements.addAll(diff);
                        changed = true;
                        break;
                }

            }
        }

        return complements;
    }

    /**
     * Sorts a collection of ranges into ascending order of min value, then max value.
     * Returns a new List of sorted ranges, leaving the input collection unmodified.
     *
     * @param <T> the value type
     * @param ranges the ranges to sort
     *
     * @return sorted ranges as a {@code List}
     */
    public static <T extends Number & Comparable> List<Range<T>> sort(Collection<Range<T>> ranges) {
        List<Range<T>> copy = new ArrayList<Range<T>>(ranges);
        Collections.sort(copy, new RangeComparator(new RangeExtendedComparator<T>()));
        return copy;
    }

    /**
     * Sorts a list of ranges into ascending order of min value, then max value.
     *
     * @param <T> the value type
     * @param ranges the ranges to sort
     */
    public static <T extends Number & Comparable> void sortInPlace(List<Range<T>> ranges) {
        Collections.sort(ranges, new RangeComparator(new RangeExtendedComparator<T>()));
    }

    /**
     * Simplifies a collection of ranges by merging those which overlap.
     *
     * @param <T> value type
     * @param ranges input ranges to simplify
     *
     * @return simplified ranges sorted by min, then max end-points
     */
    public static <T extends Number & Comparable> List<Range<T>> simplify(Collection<Range<T>> ranges) {
        List<Range<T>> inputs = new ArrayList<Range<T>>(ranges);
        RangeExtendedComparator<T> comparator = new RangeExtendedComparator<T>();

        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < inputs.size()-1 && !changed; i++) {
                Range<T> r1 = inputs.get(i);
                for (int j = i+1; j < inputs.size() && !changed; j++) {
                    Range<T> r2 = inputs.get(j);
                    RangeExtendedComparator.Result result = comparator.compare(r1, r2);
                    if (RangeExtendedComparator.isIntersection(result)) {
                        switch (result) {
                            case EEEE:  // r1 and r2 are equal points
                            case EEGG:  // r2 is a point at min of r1
                            case LEEG:  // equal intervals
                            case LEGG:  // r1 contains r2
                            case LLEE:  // r2 is a point at max of r1
                            case LLEG:  // r1 contains r2
                            case LLGG:  // r1 contains r2
                                inputs.remove(j);
                                break;

                            case EGEG:  // r1 is a point at max of r2
                            case LELE:  // r1 is a point at min of r2
                            case LELG:  // r1 is contained in r2
                            case LGEG:  // r1 is contained in r2
                            case LGLG:  // r1 is contained in r2
                                inputs.remove(i);
                                break;

                            case EGGG:  // r1 extends from max of r2
                            case LGGG:  // r1 starts within and extends beyond r2
                                inputs.remove(j);
                                inputs.remove(i);
                                inputs.add(0, new Range<T>(r2.getMin(), r2.isMinIncluded(), r1.getMax(), r1.isMaxIncluded()));
                                break;

                            case LLLE:  // r1 extends to min of r2
                            case LLLG:  // r1 extends into r2
                                inputs.remove(j);
                                inputs.remove(i);
                                inputs.add(0, new Range<T>(r1.getMin(), r1.isMinIncluded(), r2.getMax(), r2.isMaxIncluded()));
                                break;
                        }
                        changed = true;
                    }
                }
            }

        } while (changed);

        /*
         * Next, look for any pairs of the form [A, B) [B, C] that can be joined as [A, C]
         */
        Collections.sort(inputs, new RangeComparator(comparator));
        do {
            changed = false;
            for (int i = 0; i < inputs.size() - 1 && !changed; i++) {
                Range<T> r1 = inputs.get(i);
                if (r1.isMaxClosed()) {
                    for (int j = i + 1; j < inputs.size() && !changed; j++) {
                        Range<T> r2 = inputs.get(j);
                        if (r2.isMinClosed()) {
                            if (r1.getMax().compareTo(r2.getMin()) == 0) {
                                inputs.remove(j);
                                inputs.remove(i);
                                inputs.add(i, new Range<T>(r1.getMin(), r1.isMinIncluded(), r2.getMax(), r2.isMaxIncluded()));
                                changed = true;
                            }
                        }
                    }
                }
            }
        } while (changed);

        return inputs;
    }

    /**
     * Gets the intersection of two ranges.
     *
     * @param <T> value type
     * @param r1 first range
     * @param r2 second range
     *
     * @return the intersection as a new range; or {@code null} if there was no intersection
     */
    public static <T extends Number & Comparable> Range<T> intersection(Range<T> r1, Range<T> r2) {
        if (r1.isPoint()) {
            if (r2.isPoint()) {
                if (r1.equals(r2)) {
                    return new Range<T>(r1);
                } else {
                    return null;
                }

            } else {  // r2 is an interval
                if ((r1.isMinInf() && r2.isMaxOpen()) ||
                        (r1.isMinNegInf() && r2.isMinOpen()) ||
                        r2.contains(r1.getMin())) {
                    return new Range<T>(r1);
                } else {
                    return null;
                }
            }
        } else if (r2.isPoint()) {  // r1 is an interval
            if ((r2.isMinInf() && r1.isMaxOpen()) ||
                    (r2.isMinNegInf() && r1.isMinOpen()) ||
                    r1.contains(r2.getMin())) {
                return new Range<T>(r2);
            } else {
                return null;
            }
        }

        /*
         * From here, we are comparing two interval ranges
         */
        RangeExtendedComparator<T> rc = new RangeExtendedComparator<T>();
        RangeExtendedComparator.Result result = rc.compare(r1, r2);
        if (RangeExtendedComparator.isIntersection(result)) {
            T min;
            boolean minIncluded;
            switch (result.getAt(RangeExtendedComparator.MIN_MIN)) {
                case RangeExtendedComparator.LT:
                    min = r2.getMin();
                    minIncluded = r2.isMinIncluded();
                    break;

                case RangeExtendedComparator.GT:
                    min = r1.getMin();
                    minIncluded = r1.isMinIncluded();
                    break;

                default:
                    min = r1.getMin();
                    minIncluded = r1.isMinIncluded() || r2.isMinIncluded();
                    break;
            }

            T max;
            boolean maxIncluded;
            switch (result.getAt(RangeExtendedComparator.MAX_MAX)) {
                case RangeExtendedComparator.LT:
                    max = r1.getMax();
                    maxIncluded = r1.isMaxIncluded();
                    break;

                case RangeExtendedComparator.GT:
                    max = r2.getMax();
                    maxIncluded = r2.isMaxIncluded();
                    break;

                default:
                    max = r1.getMax();
                    maxIncluded = r1.isMaxIncluded() || r2.isMaxIncluded();
                    break;
            }

            return new Range<T>(min, minIncluded, max, maxIncluded);
        }

        return null;
    }

    /**
     * Subtracts the first range from the second. If the two inputs do not intersect
     * the result will be equal to the second. If the two inputs are equal, or the first
     * input encloses the second, the result will be an empty list. If the first input
     * is strictly contained within the second the result will be two ranges.
     * 
     * @param <T> value type
     * @param r1 the first range
     * @param r2 the second range
     * 
     * @return 0, 1 or 2 ranges representing the result of {@code r2 - r1}
     */
    public static <T extends Number & Comparable> List<Range<T>> subtract(Range<T> r1, Range<T> r2) {
        List<Range<T>> difference = new ArrayList<Range<T>>();
        /*
         * Check for equality between inputs
         */
        if (r1.equals(r2)) {
            return difference;  // empty list
        }

        Range<T> common = intersection(r1, r2);
        
        /*
         * Check for no overlap between inputs
         */
        if (common == null) {
            difference.add( new Range<T>(r2) );
            return difference;
        }

        /*
         * Check if r1 enclosed r2
         */
        if (common.equals(r2)) {
            return difference;  // empty list
        }

        RangeExtendedComparator<T> rc = new RangeExtendedComparator<T>();
        RangeExtendedComparator.Result result = rc.compare(common, r2);

        int minComp = result.getAt(RangeExtendedComparator.MIN_MIN);
        int maxComp = result.getAt(RangeExtendedComparator.MAX_MAX);

        if (minComp == RangeExtendedComparator.EQ) {
            difference.add(new Range<T>(common.getMax(), !common.isMaxIncluded(), r2.getMax(), r2.isMaxIncluded()));
        } else {  // minComp == GT
            if (maxComp == RangeExtendedComparator.EQ) {
                difference.add(new Range<T>(r2.getMin(), r2.isMinIncluded(), common.getMin(), !common.isMinIncluded()));
            } else {
                // common lies within r2
                difference.add(new Range<T>(r2.getMin(), r2.isMinIncluded(), common.getMin(), !common.isMinIncluded()));
                difference.add(new Range<T>(common.getMax(), !common.isMaxIncluded(), r2.getMax(), r2.isMaxIncluded()));
            }
        }

        return difference;
    }
}


