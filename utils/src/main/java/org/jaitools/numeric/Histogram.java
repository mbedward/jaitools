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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple numeric histogram class that uses the {@linkplain Range} class
 * to define bins. New bins can be defined even after data had already been
 * added to allow a histogram to adapt to the input data, however the new bins
 * must <strong>not</strong> overlap with existing bins.
 *
 * @param <T> the value type
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class Histogram<T extends Number & Comparable> {
    /** Constant indicating that a value does not fall in any bin */
    public static final int NO_BIN = -1;

    private List<Bin> bins;
    private boolean needsSort;

    /**
     * Creates a new histogram.
     */
    public Histogram() {
        bins = new ArrayList<Bin>();
    }

    /**
     * Adds a new bin to the histogram.
     *
     * @param range the data range for this bin.
     * @throws HistogramException if the new bin overlaps with an existing bin 
     */
    public void addBin(Range<T> range) throws HistogramException {
        for (Bin bin : bins) {
            if (range.intersects(bin.range)) {
                throw new HistogramException(range.toString() + " overlaps existing bin" + bin.toString());
            }
        }
        bins.add(new Bin(range));
        needsSort = true;
    }

    /**
     * Adds a value to the histogram.
     *
     * @param value the value
     *
     * @return the index of the bin that the value was allocated to or 
     *         {@link #NO_BIN}
     */
    public int addValue(T value) {
        int index = getBinForValue(value);
        if (index != NO_BIN) {
            bins.get(index).count++;
        }
        return index;
    }

    /**
     * Adds a list of values to the histogram. This will be faster than
     * adding values one at a time if the list is at least partially
     * sorted.
     *
     * @param values input values
     */
    public void addValues(List<T> values) {
        ensureBinsSorted();

        int curIndex = NO_BIN;
        Bin bin = null;

        for (T value : values) {
            int index = findBin(value);
            if (index != NO_BIN) {
                if (index != curIndex) {
                    bin = bins.get(index);
                    curIndex = index;
                }
                bin.count++ ;
            }
        }
    }

    /**
     * Gets the bin index for a value. The value is not added to the histogram.
     *
     * @param value the value
     *
     * @return the bin index or {@link #NO_BIN}
     */
    public int getBinForValue(T value) {
        ensureBinsSorted();
        return findBin(value);
    }

    /**
     * Gets bin indices for a list of input values. The values are not added
     * to the histogram.
     *
     * @param values input values
     *
     * @return a new {@code List} of bin indices
     */
    public List<Integer> getBinForValues(List<T> values) {
        List<Integer> indices = new ArrayList<Integer>();
        for (T value : values) {
            indices.add(getBinForValue(value));
        }
        return indices;
    }

    /**
     * Gets the number of bins.
     *
     * @return number of bins
     */
    public int size() {
        return bins.size();
    }

    /**
     * Gets the count of data items in each bin.
     *
     * @return the counts
     */
    public List<Integer> getCounts() {
        List<Integer> counts = new ArrayList<Integer>();
        for (Bin bin : bins) {
            counts.add(bin.count);
        }
        return counts;
    }

    /**
     * Finds the bin for the given value.
     *
     * @param value the value
     *
     * @return bin index or {@code NO_BIN}
     */
    private int findBin(T value) {
        int index = 0;
        for (Bin bin : bins) {
            if (bin.range.contains(value)) {
                return index;
            }
            index++ ;
        }
        return NO_BIN;
    }

    /**
     * Ensures that bins are sorted in ascending order.
     */
    private void ensureBinsSorted() {
        if (needsSort) {
            Collections.sort(bins, new BinComparator());
            needsSort = false;
        }
    }

    /**
     * A histogram bin.
     */
    private class Bin {
        Range<T> range;
        int count;

        public Bin(Range r) {
            this.range = new Range(r);
            this.count = 0;
        }
    }

    /**
     * A comparator used to sort bins into ascending order based on their
     * lower bound.
     */
    private class BinComparator implements Comparator<Bin> {
        private RangeExtendedComparator<T> rc = new RangeExtendedComparator<T>();

        public int compare(Bin b1, Bin b2) {
            RangeExtendedComparator.Result result = rc.compare(b1.range, b2.range);
            switch (result.getNotation().charAt(1)) {
                case '<': return -1;
                case '>': return 1;
                default: return 0;
            }
        }
    }
}
