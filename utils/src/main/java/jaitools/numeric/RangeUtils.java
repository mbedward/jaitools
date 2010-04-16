/*
 * Copyright 2010 Michael Bedward
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
 * @version $Id: RangeUtils.java 1160 2010-04-07 08:45:17Z michael.bedward $
 */
public class RangeUtils {

    private static class RangeSortComparator<T extends Number & Comparable> implements Comparator<Range<T>> {
        private RangeComparator<T> rc;

        public RangeSortComparator(RangeComparator<T> rc) {
            this.rc = rc;
        }

        public int compare(Range<T> r1, Range<T> r2) {
            RangeComparator.Result result = rc.compare(r1, r2);
            switch (result.getAt(RangeComparator.MIN_MIN)) {
                case RangeComparator.LT:
                    return -1;

                case RangeComparator.GT:
                    return 1;

                default:
                    switch (result.getAt(RangeComparator.MAX_MAX)) {
                        case RangeComparator.LT:
                            return -1;

                        case RangeComparator.GT:
                            return 1;

                        default:
                            return 0;
                    }
            }
        }

    }


    /**
     * Create the complement of a {@code Range}. This is equivalent to subtracting the
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
     * Create the complement of the given list of {@code Ranges}. This method first
     * calls {@linkplain #simplify} on the inputs and then subtracts each of the
     * resulting {@code Ranges} from the whole number line.
     *
     * @param ranges input ranges
     *
     * @return a list of {@code Ranges} which form the complement (may be empty)
     *
     * @see #createComplement(jaitools.numeric.Range)
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
     * Sort a collection of {@code Ranges} in ascending order of min value, then max value.
     *
     * @param ranges the {@code Ranges} to sort
     *
     * @return sorted {@code Ranges}
     */
    public static <T extends Number & Comparable> List<Range<T>> sort(Collection<Range<T>> ranges) {
        List<Range<T>> inputs = new ArrayList<Range<T>>(ranges);
        Collections.sort(inputs, new RangeSortComparator(new RangeComparator<T>()));
        return inputs;
    }

    /**
     * Takes a collection of ranges and returns a simplified collection by merging ranges
     * that overlap.
     *
     * @param ranges input ranges to simplify
     *
     * @return simplified ranges sorted by min, then max end-points
     */
    public static <T extends Number & Comparable> List<Range<T>> simplify(Collection<Range<T>> ranges) {
        List<Range<T>> inputs = new ArrayList<Range<T>>(ranges);
        RangeComparator<T> comparator = new RangeComparator<T>();

        boolean changed;
        do {
            changed = false;
            for (int i = 0; i < inputs.size()-1 && !changed; i++) {
                Range<T> r1 = inputs.get(i);
                for (int j = i+1; j < inputs.size() && !changed; j++) {
                    Range<T> r2 = inputs.get(j);
                    RangeComparator.Result result = comparator.compare(r1, r2);
                    if (RangeComparator.isIntersection(result)) {
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
        Collections.sort(inputs, new RangeSortComparator(comparator));
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
     * Return the intersection of the two ranges.
     *
     * @param r1 first range
     * @param r2 second range
     *
     * @return a new {@code Range} representing the intersection or null if the inputs
     *         do not intersect
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
        RangeComparator<T> rc = new RangeComparator<T>();
        RangeComparator.Result result = rc.compare(r1, r2);
        if (RangeComparator.isIntersection(result)) {
            T min;
            boolean minIncluded;
            switch (result.getAt(RangeComparator.MIN_MIN)) {
                case RangeComparator.LT:
                    min = r2.getMin();
                    minIncluded = r2.isMinIncluded();
                    break;

                case RangeComparator.GT:
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
            switch (result.getAt(RangeComparator.MAX_MAX)) {
                case RangeComparator.LT:
                    max = r1.getMax();
                    maxIncluded = r1.isMaxIncluded();
                    break;

                case RangeComparator.GT:
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
     * Subtract the first {@code Range} from the second. If the two inputs do not intersect
     * the result will be equal to the second. If the two inputs are equal, or the first
     * input encloses the second, the result will be an empty list. If the first input
     * is strictly contained within the second the result will be two {@code Ranges}.
     *
     * @param r1 the first range
     * @param r2 the second range
     *
     * @return 0, 1 or 2 {@code Ranges} representing the result of {@code r2 - r1}
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

        RangeComparator<T> rc = new RangeComparator<T>();
        RangeComparator.Result result = rc.compare(common, r2);

        int minComp = result.getAt(RangeComparator.MIN_MIN);
        int maxComp = result.getAt(RangeComparator.MAX_MAX);

        if (minComp == RangeComparator.EQ) {
            difference.add(new Range<T>(common.getMax(), !common.isMaxIncluded(), r2.getMax(), r2.isMaxIncluded()));
        } else {  // minComp == GT
            if (maxComp == RangeComparator.EQ) {
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


