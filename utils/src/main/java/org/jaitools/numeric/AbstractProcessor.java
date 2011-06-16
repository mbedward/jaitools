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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.jaitools.CollectionFactory;
import org.jaitools.numeric.Range.Type;


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
