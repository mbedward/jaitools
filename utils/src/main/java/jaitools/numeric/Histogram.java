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

package jaitools.numeric;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A simple numeric histogram class that uses the {@linkplain Range} class
 * to define bins. New bins can be defined even after data had already been
 * added to allow a histrogram to adapt to the input data, however the new bins
 * must not overlap with existing bins.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class Histogram<T extends Number & Comparable> {
    /** Constant indicating that a value does not fall in any bin */
    public static final int NO_BIN = -1;

    private List<Bin> bins;
    private boolean needsSort;

    /**
     * Create a new instance.
     */
    public Histogram() {
        bins = new ArrayList<Bin>();
    }

    /**
     * Add a new bin.
     *
     * @param r the data range for this bin.
     *
     * @throws HistogramException if the new bin overlaps any existing bins
     */
    public void addBin(Range<T> r) throws HistogramException {
        for (Bin bin : bins) {
            if (r.intersects(bin.range)) {
                throw new HistogramException(r.toString() + " overlaps existing bin" + bin.toString());
            }
        }
        bins.add(new Bin(r));
        needsSort = true;
    }

    /**
     * Add a value to the histogram.
     *
     * @param value the value
     *
     * @return the index of the bin that the value was allocated to or {@linkplain #NO_BIN}
     */
    public int addValue(T value) {
        int index = getBinForValue(value);
        if (index != NO_BIN) {
            bins.get(index).count++;
        }
        return index;
    }

    /**
     * Add a list of values to the histogram. This will be faster than
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
     * Get the index of the bin that the given value would fall into if
     * added to the histogram.
     *
     * @param value the value
     *
     * @return the bin index or {@linkplain #NO_BIN}
     */
    public int getBinForValue(T value) {
        ensureBinsSorted();
        return findBin(value);
    }

    /**
     * Convenience method to get bin indices for a list of input values.
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
     * Get the number of bins
     *
     * @return number of bins
     */
    public int size() {
        return bins.size();
    }

    /**
     * Return the counts of data items per bin.
     *
     * @return counts as a new {@code List}
     */
    public List<Integer> getCounts() {
        List<Integer> counts = new ArrayList<Integer>();
        for (Bin bin : bins) {
            counts.add(bin.count);
        }
        return counts;
    }

    /**
     * Find the bin for the given value.
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
     * Ensure that bins are sorted in ascending order.
     */
    private void ensureBinsSorted() {
        if (needsSort) {
            Collections.sort(bins, new BinComparator());
            needsSort = false;
        }
    }

    /**
     * A bin, made up of a defining {@code Range} and a data count
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
        private RangeComparator<T> rc = new RangeComparator<T>();

        public int compare(Bin b1, Bin b2) {
            RangeComparator.Result result = rc.compare(b1.range, b2.range);
            switch (result.getNotation().charAt(1)) {
                case '<': return -1;
                case '>': return 1;
                default: return 0;
            }
        }
    }
}
