/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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

import java.util.Collection;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

import org.jaitools.CollectionFactory;

/**
 * A comparator for {@link Range} objects capable of discerning the 18 different possible
 * interval comparisons as described by:
 * <blockquote>
 * Brian Hayes (2003) A lucid interval. <br>
 * American Scientist 91(6):484-488. <p>
 *
 * Available at: http://www.cs.utep.edu/interval-comp/hayes.pdf
 * </blockquote>
 *
 * Constants describing each of these possible comparisons are defined in the
 * {@link RangeComparator.Result} class.
 * <p>
 * Normally, client code will not need to use the methods defined in this class
 * directly but will work through the {@link Range} class.
 *
 * @param <T> the numeric type
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RangeExtendedComparator<T extends Number & Comparable> {

    /** Constant for "less than". */
    public static final int LT = -1;

    /** Constant for "equal to". */
    public static final int EQ = 0;

    /** Constant for "greater than". */
    public static final int GT = 1;

    private static final int UNDEFINED = LT - 1;

    /**
     * Index of {@link Result} element for the comparison of the min value
     * of the first Range with the max value of the second range.
     */
    public static final int MIN_MAX = 0;

    /**
     * Index of {@link Result} element for the comparison of the min value
     * of the first Range with the min value of the second range.
     */
    public static final int MIN_MIN = 1;

    /**
     * Index of {@link Result} element for the comparison of the max value
     * of the first Range with the max value of the second range.
     */
    public static final int MAX_MAX = 2;

    /**
     * Index of {@link Result} element for the comparison of the max value
     * of the first Range with the min value of the second range.
     */
    public static final int MAX_MIN = 3;

    /**
     * Constants defining the names, notation and descriptions for the 18 different
     * possible interval comparisons as described by:
     * <blockquote>
     * Brian Hayes (2003) A lucid interval. <br>
     * American Scientist 91(6):484-488. <p>
     *
     * Available at: http://www.cs.utep.edu/interval-comp/hayes.pdf
     * </blockquote>
     *
     * The meaning of each of the four characters in the notation describing the
     * comparison between interval X and interval Y is as follows:
     * <ol type="1">
     * <li> Min X compared to Max Y
     * <li> Min X compared to Min Y
     * <li> Max X compared to Max Y
     * <li> Max X compared to Min Y
     * </ol>
     * So, for instance {@code X <<=> Y} means interval X starts below and extends to
     * the maximum of interval Y.
     */
    public enum Result {
        /** (Hayes notation {@code <<<<}) Interval X is entirely less than interval Y
         */
        LLLL("<<<<", "is entirely less than"),

        /** (Hayes notation {@code <<<=}) Interval X extends to minimum of interval Y
         */
        LLLE("<<<=", "extends to min of"),

        /** (Hayes notation {@code <<<>}) Interval X starts below and extends within interval Y
         */
        LLLG("<<<>", "starts below and extends within"),

        /** (Hayes notation {@code <<=>}) Interval X starts below and extends to maximum of interval Y
         *  Notation: {@code <<=>}
         */
        LLEG("<<=>", "starts below and extends to max of"),

        /** (Hayes notation {@code <=>>}) Interval X starts with and extends beyond interval Y. <br>
         */
        LEGG("<=>>", "starts with and extends beyond"),

        /** (Hayes notation {@code <<==}) Interval X starts below and has maximum at point location Y. <br>
         */
        LLEE("<<==", "starts below and has max at point location of"),

        /** (Hayes notation {@code ==>>}) Interval X extends from point location Y. <br>
         */
        EEGG("==>>", "extends from point location of"),

        /** (Hayes notation {@code <==>}) Interval X is exactly equal to finite interval Y. <br>
         */
        LEEG("<==>", "is exactly equal to finite interval"),

        /** (Hayes notation {@code ====}) Point X is exactly equal to point Y. <br>
         *  Notation: {@code ====}
         */
        EEEE("====", "is exactly equal to point"),

        /** (Hayes notation {@code <<>>}) Interval X strictly encloses interval Y
         */
        LLGG("<<>>", "strictly encloses"),

        /** (Hayes notation {@code <><>}) Interval X is enclosed by interval Y
         */
        LGLG("<><>", "is strictly enclosed by"),

        /** (Hayes notation {@code <>>>}) Interval X starts within and extends beyond interval Y
         */
        LGGG("<>>>", "starts within and extends beyond"),

        /** (Hayes notation {@code <>=>}) Interval X starts within and extends to maximum of interval Y
         */
        LGEG("<>=>", "starts within and extends to max of"),

        /** (Hayes notation {@code <=<>}) Interval X starts with and ends within interval Y
         */
        LELG("<=<>", "starts with and ends within"),

        /** (Hayes notation {@code =>=>}) X is a point at maximum of interval Y
         */
        EGEG("=>=>", "is a point at max of"),

        /** (Hayes notation {@code <=<=}) X is a point at minimum of interval Y
         */
        LELE("<=<=", "is a point at min of"),

        /** (Hayes notation {@code =>>>}) Interval X extends from maximum of interval Y
         */
        EGGG("=>>>", "extends from max of"),

        /** (Hayes notation {@code >>>>}) Interval X is entirely greater than interval Y
         */
        GGGG(">>>>", "is entirely greater than");

        private static final Map<String, Result> lookup = CollectionFactory.map();
        static {
            for (Result t : EnumSet.allOf(Result.class)) {
                lookup.put(t.name(), t);
            }
        }

        private String notation;
        private String desc;
        private int[] flags;

        /**
         * Private constructor
         * @param notation Haye's notation for the comparison
         * @param desc brief description
         */
        private Result(String notation, String desc) {
            this.notation = notation;
            this.desc = desc;

            flags = new int[notation.length()];
            final String symbols = "<=>";
            for (int i = 0; i < notation.length(); i++) {
                flags[i] = symbols.indexOf(notation.charAt(i)) - 1;
            }
        }

        /**
         * Gets the notation form of this comparison. For example,
         * for Result.LLEG the notation form is "<<=>".
         * 
         * @return the Hayes notation
         */
        public String getNotation() {
            return notation;
        }

        /**
         * Returns a description for this comparison type. The description is
         * worded so that it makes grammatical sense when placed between two
         * Range object names. For example...
         * <pre><code>
         * Range&lt;Integer> r1 = ...
         * Range&lt;Integer> r2 = ...
         * RangeComparator.Result comp = r1.compareTo(r2);
         * System.out.println(String.format("Range r1 %s Range r2", comp.getDesc()));
         * </code></pre>
         * 
         * @return the description
         */
        public String getDesc() {
            return desc;
        }

        /**
         * Gets the result element at the given index. The index can be
         * specified as an int from 0 to 3 or one of the constants:
         * {@link RangeComparator#MAX_MAX},
         * {@link RangeComparator#MAX_MIN},
         * {@link RangeComparator#MIN_MAX},
         * {@link RangeComparator#MIN_MIN}.
         *
         * @param pos index of the element to retrieve
         *
         * @return one of {@link RangeComparator#LT}, {@link RangeComparator#GT},
         *         or {@link RangeComparator#EQ}
         */
        public int getAt(int pos) {
            return flags[pos];
        }

        /**
         * Partial matching: returns a List of Types that have the given
         * end-point comparison flag at the specified position
         * @param op one of the RangeComparator constants: LT, EQ, GT
         * @param pos flag position 0-3
         * @return the List of matching Types (may be empty)
         * @see #match(int, int, java.util.Collection)
         */
        public List<Result> match(int op, int pos) {
            return match(op, pos, EnumSet.allOf(Result.class));
        }

        /**
         * Partial matching: returns a List of those Types in the provided Collection
         * that have the given end-point comparison flag at the specified position.
         * Repeated calls of this method can be used for incremental matching;
         * for example...
         * <pre><code>
         * // first call uses two-arg method to select from all Types that have
         * // the given flag at pos 0 (Types with names L***)
         * List&lt;Result> types = match(RangeComparator.LT, 0);
         *
         * // now winnow down to those Types that match LL*G
         * types = match(RangeComparator.LT, 1, types);
         * types = match(RangeComparator.GT, 3, types);
         *
         * for (Result t : types) {
         *     System.out.println(t.toString() + ": " + t.getDesc());
         * }
         * </code></pre>
         *
         * @param op one of the RangeComparator constants: LT, EQ, GT
         * @param pos flag position 0-3
         * @param typesToSearch 
         * @return the List of matching Types (may be empty)
         */
        public List<Result> match(int op, int pos, Collection<Result> typesToSearch) {
            if (op < LT || op > GT) {
                throw new IllegalArgumentException("op must be one of LT, EQ or GT");
            }

            if (pos < 0 || pos >= 4) {
                throw new IllegalArgumentException("pos must be in the range [0, NOTATION_LENGTH)");
            }

            List<Result> types = CollectionFactory.list();

            for (Result t : typesToSearch) {
                if (t.flags[pos] == op) {
                    types.add(t);
                }
            }

            return types;
        }

        /**
         * Get the Result that matches the given array of comparison flags.
         * @param compFlags 
         * @return the Result that matches the given array of comparison flags or
         *         {@code null} if there is no match
         */
        public static Result get(int[] compFlags) {
            char[] name = new char[4];

            for (int i = 0; i < 4; i++) {
                switch(compFlags[i]) {
                    case LT:
                        name[i] = 'L';
                        break;

                    case EQ:
                        name[i] = 'E';
                        break;

                    case GT:
                        name[i] = 'G';
                        break;

                    default:
                        throw new IllegalArgumentException("compOps must only contain LT, EQ or GT");
                }
            }

            return lookup.get(new String(name));
        }

    }

    /**
     * Tests if a Result value describes an intersection between two ranges.
     * @param r the Result value
     * @return true if r represents an intersection; false otherwise
     */
    public static boolean isIntersection(Result r) {
        return !(r == Result.LLLL || r == Result.GGGG);
    }

    /**
     * Compares two Range objects and return the {@link RangeComparator.Result}
     * that describes the relationship between them from the point of view of the first Range
     *
     * @param r1 the first Range
     * @param r2 the second Range
     * 
     * @return a {@link RangeComparator.Result} constant
     */
    public Result compare(Range<T> r1, Range<T> r2) {

        /*
         * If either of the ranges is a point interval then
         * we delegate to pointCompare just to keep the code in
         * this method to a manageable amount
         */
        if (r1.isPoint() || r2.isPoint()) {
            return pointCompare(r1, r2);
        }

        int[] compFlags = new int[4];

        /* 
         * Comparison 1: r1 min compared to r2 max
         */
        compFlags[0] = UNDEFINED;
        if (r1.isMinClosed()) {
            if (r2.isMaxClosed()) {
                int flag = validFlag(r1.getMin().compareTo(r2.getMax()));
                if (flag == EQ) {
                    if (r1.isMinIncluded()) {
                        if (r2.isMaxIncluded()) {
                            compFlags[0] = EQ;
                        } else {
                            compFlags[0] = GT;
                        }
                    } else {
                        compFlags[0] = GT;
                    }
                } else {
                    compFlags[0] = flag;
                }
            } else {
                compFlags[0] = LT;
            }
        } else { // r1 min is open
            compFlags[0] = LT;
        }
        assert (compFlags[0] != UNDEFINED);

        /*
         * Comparison 2: r1 min compared to r2 min
         */
        compFlags[1] = UNDEFINED;
        if (r1.isMinClosed()) {
            if (r2.isMinClosed()) {
                int flag = validFlag(r1.getMin().compareTo(r2.getMin()));
                if (flag == EQ) {
                    if (r1.isMinIncluded()) {
                        if (r2.isMinIncluded()) {
                            compFlags[1] = EQ;
                        } else {
                            compFlags[1] = LT;
                        }
                    } else {
                        if (r2.isMinIncluded()) {
                            compFlags[1] = GT;
                        } else {
                            compFlags[1] = EQ;
                        }
                    }
                } else {
                    compFlags[1] = flag;
                }
            } else {
                compFlags[1] = GT;
            }
        } else {
            if (r2.isMinClosed()) {
                compFlags[1] = LT;
            } else {
                compFlags[1] = EQ;
            }
        }
        assert (compFlags[1] != UNDEFINED);

        /*
         * Comparison 3: r1 max compared to r2 max
         */
        compFlags[2] = UNDEFINED;
        if (r1.isMaxClosed()) {
            if (r2.isMaxClosed()) {
                int flag = validFlag(r1.getMax().compareTo(r2.getMax()));
                if (flag == EQ) {
                    if (r1.isMaxIncluded()) {
                        if (r2.isMaxIncluded()) {
                            compFlags[2] = EQ;
                        } else {
                            compFlags[2] = GT;
                        }
                    } else {
                        if (r2.isMaxIncluded()) {
                            compFlags[2] = LT;
                        } else {
                            compFlags[2] = EQ;
                        }
                    }
                } else {
                    compFlags[2] = flag;
                }
            } else {
                compFlags[2] = LT;
            }
        } else {
            if (r2.isMaxClosed()) {
                compFlags[2] = GT;
            } else {
                compFlags[2] = EQ;
            }
        }
        assert (compFlags[2] != UNDEFINED);

        /*
         * Comparison 4: r1 max compared to r2 min
         */
        compFlags[3] = UNDEFINED;
        if (r1.isMaxClosed()) {
            if (r2.isMinClosed()) {
                int flag = validFlag(r1.getMax().compareTo(r2.getMin()));
                if (flag == EQ) {
                    if (r1.isMaxIncluded()) {
                        if (r2.isMinIncluded()) {
                            compFlags[3] = EQ;
                        } else {
                            compFlags[3] = LT;
                        }
                    } else {
                        compFlags[3] = LT;
                    }
                } else {
                    compFlags[3] = flag;
                }
            } else {
                compFlags[3] = GT;
            }
        } else {
            compFlags[3] = GT;
        }
        assert (compFlags[3] != UNDEFINED);

        return Result.get(compFlags);
    }


    /**
     * Helper for {@link #compare(Range, Range) } used
     * when one or both of the intervals are degenerate (point) intervals.
     *
     * @param r1 first interval
     * @param r2 second interval
     *
     * @return a {@link RangeComparator.Result} constant
     */
    private Result pointCompare(Range<T> r1, Range<T> r2) {
        int[] compFlags = new int[4];

        if (r1.isPoint() && r2.isPoint()) {
            // this will cover both finite and infinite locations
            if (r1.isMinNegInf()) {
                if (r2.isMinNegInf()) {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = EQ;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = LT;
                }
            } else if (r1.isMinInf()) {
                if (r2.isMinInf()) {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = EQ;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = GT;
                }
            } else {
                if (r2.isMinNegInf()) {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = GT;
                } else if (r2.isMinInf()) {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = LT;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] =
                            validFlag(r1.getMin().compareTo(r2.getMin()));
                }
            }
        } else if (r1.isPoint()) {  // r2 is a proper interval
            if (r1.isMinNegInf()) {
                if (r2.isMinNegInf()) {
                    compFlags[0] = LT;
                    compFlags[1] = EQ;
                    compFlags[2] = LT;
                    compFlags[3] = EQ;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = LT;
                }
            } else if (r1.isMinInf()) {
                if (r2.isMaxInf()) {
                    compFlags[0] = EQ;
                    compFlags[1] = GT;
                    compFlags[2] = EQ;
                    compFlags[3] = GT;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = GT;
                }
            } else {
                if (r2.isMaxInf()) {
                    compFlags[0] = compFlags[2] = LT;
                } else {
                    int flag = validFlag(r1.getMin().compareTo(r2.getMax()));
                    if (flag == EQ) {
                        if (r2.isMaxIncluded()) {
                            compFlags[0] = compFlags[2] = EQ;
                        } else {
                            compFlags[0] = compFlags[2] = GT;
                        }
                    } else {
                        compFlags[0] = compFlags[2] = flag;
                    }
                }
                
                if (r2.isMinNegInf()) {
                    compFlags[1] = compFlags[3] = GT;
                } else {
                    int flag = validFlag(r1.getMin().compareTo(r2.getMin()));
                    if (flag == EQ) {
                        if (r2.isMinIncluded()) {
                            compFlags[1] = compFlags[3] = EQ;
                        } else {
                            compFlags[1] = compFlags[3] = LT;
                        }
                    } else {
                        compFlags[1] = compFlags[3] = flag;
                    }
                }
            }
        } else if (r2.isPoint()) {  // r1 is a proper interval
            if (r2.isMinNegInf()) {
                if (r1.isMinNegInf()) {
                    compFlags[0] = EQ;
                    compFlags[1] = EQ;
                    compFlags[2] = GT;
                    compFlags[3] = GT;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = GT;
                }
            } else if (r2.isMinInf()) {
                if (r1.isMaxInf()) {
                    compFlags[0] = LT;
                    compFlags[1] = LT;
                    compFlags[2] = EQ;
                    compFlags[3] = EQ;
                } else {
                    compFlags[0] = compFlags[1] = compFlags[2] = compFlags[3] = LT;
                }
            } else {
                if (r1.isMinNegInf()) {
                    compFlags[0] = compFlags[1] = LT;
                } else {
                    int flag = validFlag(r1.getMin().compareTo(r2.getMin()));
                    if (flag == EQ) {
                        if (r1.isMinIncluded()) {
                            compFlags[0] = compFlags[1] = EQ;
                        } else {
                            compFlags[0] = compFlags[1] = GT;
                        }
                    } else {
                        compFlags[0] = compFlags[1] = flag;
                    }
                }

                if (r1.isMaxInf()) {
                    compFlags[2] = compFlags[3] = GT;
                } else {
                    int flag = validFlag(r1.getMax().compareTo(r2.getMin()));
                    if (flag == EQ) {
                        if (r1.isMaxIncluded()) {
                            compFlags[2] = compFlags[3] = EQ;
                        } else {
                            compFlags[2] = compFlags[3] = LT;
                        }
                    } else {
                        compFlags[2] = compFlags[3] = flag;
                    }
                }
            }
        }

        return Result.get(compFlags);
    }

    private int validFlag(int flag) {
        if (flag < 0) return LT;
        if (flag > 0) return GT;
        return EQ;
    }

}

