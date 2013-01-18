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

import java.util.Comparator;

/**
 * Compares ranges by min end-point, then max end-point. This reduces the
 * detailed comparison available from {@linkplain RangeExtendedComparator} to
 * an integer result that is compatible with generic sorting methods such
 * as 
 * {@link java.util.Collections#sort(java.util.List, Comparator)}.
 * <p>
 * <table border="1">
 * <tr>
 * <th>Result</th><th>Meaning</th>
 * </tr>
 * <tr>
 * <td>negative</td>
 * <td>either r1 min is less than r2 min or r1 max is less than r2 max</td>
 * </tr>
 * <tr>
 * <td>zero</td>
 * <td>r1 and r2 are equal</td>
 * </tr>
 * <tr>
 * <td>positive</td>
 * <td>either r1 min is greater than r2 min or r1 max is greater than r2 max</td>
 * </tr>
 * </table>
 * 
 * @param <T> range value type
 *
 * @author michael
 */
public class RangeComparator<T extends Number & Comparable> implements Comparator<Range<T>> {
    private RangeExtendedComparator<T> delegate;

    /**
     * Creates a new comparator.
     */
    public RangeComparator() {
        this(null);
    }
    
    /**
     * Creates a new comparator that will use the supplied extended comparator.
     * 
     * @param ec extended comparator (may be null)
     */
    public RangeComparator(RangeExtendedComparator<T> ec) {
        this.delegate = ec == null ? new RangeExtendedComparator<T>() : ec;
    }

    /**
     * Compares two ranges.
     * Returns a negative value if r1 min is less than r2 min OR r1 max is less
     * than r2 max; a positive value if r1 min is greater than r2 min OR r2
     * max is greater than r2 max; or zero if the ranges are identical.
     * 
     * @param r1 first range
     * @param r2 second range
     * 
     * @returns an integer value indicating r1 compared to r2
     */
    public int compare(Range<T> r1, Range<T> r2) {
        RangeExtendedComparator.Result result = delegate.compare(r1, r2);
        switch (result.getAt(RangeExtendedComparator.MIN_MIN)) {
            case RangeExtendedComparator.LT:
                return -1;
            case RangeExtendedComparator.GT:
                return 1;
            default:
                switch (result.getAt(RangeExtendedComparator.MAX_MAX)) {
                    case RangeExtendedComparator.LT:
                        return -1;
                    case RangeExtendedComparator.GT:
                        return 1;
                    default:
                        return 0;
                }
        }
    }
    
}
