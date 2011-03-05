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
package jaitools.numeric;

import jaitools.CollectionFactory;
import jaitools.numeric.Range.Type;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for statistics processors used with {@code StreamingSampleStats}.
 *
 * @author Michael Bedward
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 *
 * @since 1.0
 * @version $Id$
 */
public abstract class AbstractProcessor implements Processor {

    /** Number of samples offered. */
    protected long numOffered;
    
    /** Number of samples accepted. */
    protected long numAccepted;
    
    /** Number of NaN samples offered. */
    protected long numNaN;

    /** Number of NODATA samples offered. */
    protected long numNoData;

    /* Ranges of data values to include / exclude from calculations */
    private List<Range<Double>> ranges;

    /* Stores how ranges are to be interpreted: inclusion or exclusion */
    private Range.Type rangesType;

    /* Ranges of data values to treat as NoData and exclude from calculations */
    private List<Range<Double>> noDataRanges;


    /**
     * Default constructor.
     */
    public AbstractProcessor() {
        ranges = CollectionFactory.list();
        noDataRanges = CollectionFactory.list();
        rangesType = Range.Type.UNDEFINED;
        numOffered = numAccepted = numNaN = numNoData = 0;
    }

    /**
     * Creates a new processor with specified use of {@code Ranges}.
     *
     * @param rangesType normally either {@linkplain Range.Type#INCLUDE} to indicate that
     *        {@code Ranges} will define values to operate on, or {@linkplain Range.Type#EXCLUDE}
     *        when {@code Ranges} will define values to exclude from operations.
     */
    public AbstractProcessor(final Range.Type rangesType) {
        ranges = CollectionFactory.list();
        noDataRanges = CollectionFactory.list();
        this.rangesType = rangesType;
        numOffered = numAccepted = numNaN = numNoData = 0;
    }

    /**
     * {@inheritDoc}
     */
    public void addNoDataRange(Range<Double> noData) {
        if (noData != null) {
            // copy the input Range defensively
            noDataRanges.add(new Range<Double>(noData));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addNoDataValue(Double noData) {
        if (noData != null && !noData.isNaN()) {
            noDataRanges.add(new Range<Double>(noData));
        }
    }


    /**
     * {@inheritDoc}
     */
    public final long getNumOffered() {
        return numOffered;
    }

    /**
     * {@inheritDoc}
     */
    public final long getNumAccepted() {
        return numAccepted;
    }

    /**
     * {@inheritDoc}
     */
    public long getNumNaN() {
        return numNaN;
    }

    /**
     * {@inheritDoc}
     */
    public void offer(Double sample) {
        numOffered++;
        if (update(sample)) {
            numAccepted++;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<Statistic, Double> get() {
        Map<Statistic, Double> stats = CollectionFactory.map();
        for (Statistic s : getSupported()) {
            stats.put(s, get(s));
        }
        return stats;
    }

    /**
     * Processes a sample value.
     * 
     * @param sample the sample value
     *
     * @return {@code true} if the sample is accepted;
     *         {@code false} otherwise
     */
    protected abstract boolean update(Double sample);

    /**
     * {@inheritDoc}
     */
    public void addRange(Range<Double> range) {
        if (range != null) {
            if (this.rangesType == Range.Type.UNDEFINED) {
                this.rangesType = Range.Type.EXCLUDE;
            }
            // copy the input Range defensively
            ranges.add(new Range<Double>(range));
        }
    }

    /**
     * {@inheritDoc}
     */
    public void addRange(Range<Double> range, Range.Type rangesType) {
        if (range != null) {
            if (this.rangesType == Range.Type.UNDEFINED) {
                this.rangesType = rangesType;
            } else {
                if (this.rangesType != rangesType) {
                    throw new IllegalArgumentException("The provided rangesType is not compatible with the processors rangesType");
                }
            }
            // copy the input Range defensively
            ranges.add(new Range<Double>(range));
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Range<Double>> getRanges() {
        return Collections.unmodifiableList(ranges);
    }

    /**
     * {@inheritDoc}
     */
    public List<Range<Double>> getNoDataRanges() {
        return Collections.unmodifiableList(noDataRanges);
    }

    /**
     * {@inheritDoc}
     */
    public void setRangesType(final Range.Type rangesType) {
        if (this.rangesType != Range.Type.UNDEFINED) {
            throw new UnsupportedOperationException("Cannot change RangesType once already defined");
        }
        this.rangesType = rangesType;
    }

    /**
     * {@inheritDoc}
     */
    public final Range.Type getRangesType() {
        return rangesType;
    }

    /**
     * {@inheritDoc}
     */
    public boolean isAccepted(Double sample) {
        if (sample == null) {
            return false;
        }
        if (sample.isNaN()) {
            numNaN++;
            numNoData++;
            return false;
        }

        if (noDataRanges != null){
            for (Range<Double> r : noDataRanges) {
                if (r.contains(sample)) {
                    numNoData++;
                    return false;
                }
            }
        }

        if (ranges == null || ranges.isEmpty()) {
            return true;
        }

        boolean isAccepted = rangesType == Type.INCLUDE ? false : true;
        for (Range<Double> r : ranges) {
            switch (rangesType) {
                case EXCLUDE:
                    isAccepted &= !r.contains(sample);
                    break;
                case INCLUDE:
                    isAccepted |= r.contains(sample);
                    break;
            }
        }
        return isAccepted;
    }

    /**
     * {@inheritDoc}
     */
    public long getNumNoData() {
        return numNoData;
    }

}
