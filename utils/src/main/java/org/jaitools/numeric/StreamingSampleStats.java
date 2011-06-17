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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaitools.CollectionFactory;


/**
 * A class to calculate summary statistics for a sample of Double-valued
 * buffers that is received as a (potentially long) stream of values rather
 * than in a single batch. Any Double.NaN values in the stream will be
 * ignored.
 * <p>
 * Two options are offered to calculate sample median. Where it is known a priori
 * that the data stream can be accomodated in memory, the exact median can be
 * requested with Statistic.MEDIAN. Where the length of the data stream is unknown,
 * or known to be too large to be held in memory, an approximate median can be
 * calculated using the 'remedian' estimator as described in:
 * <blockquote>
 * PJ Rousseeuw and GW Bassett (1990)
 * <i>The remedian: a robust averaging method for large data sets.</i>
 * Journal of the American Statistical Society 85:97-104
 * </blockquote>
 * This is requested with Statistic.APPROX_MEDIAN.
 * <p>
 * Note: the 'remedian' estimator performs badly with non-stationary data, e.g. a
 * data stream that is monotonically increasing will result in an estimate for the
 * median that is too high. If possible, it is best to de-trend or randomly order
 * the data prior to streaming it.
 * <p>
 * Example of use:
 * <pre><code>
 * StreamingSampleStats strmStats = new StreamingSampleStats();
 *
 * // set the statistics that will be calculated
 * Statistic[] stats = {
 *     Statistic.MEAN,
 *     Statistic.SDEV,
 *     Statistic.RANGE,
 *     Statistic.APPROX_MEDIAN
 * };
 * strmStats.setStatistics(stats);
 *
 * // some process that generates a long stream of data
 * while (somethingBigIsRunning) {
 *     double value = ...
 *     strmStats.offer(value);
 * }
 *
 * // report the results
 * for (Statistic s : stats) {
 *     System.out.println(String.format("%s: %.4f", s, strmStats.getStatisticValue(s)));
 * }
 *
 * </code></pre>
 *
 * @author Michael Bedward
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @version $Id$
 */
public class StreamingSampleStats {

    private static final Logger LOGGER = Logger.getLogger("org.jaitools.numeric");
    
    private ProcessorFactory factory = new ProcessorFactory();
    private List<Processor> processors;
    private List<Range<Double>> ranges;
    private List<Range<Double>> noDataRanges;
    private final Range.Type rangesType;

    /**
     * Creates a new sampler and sets the default range type to 
     * {@link Range.Type#EXCLUDE}.
     */
    public StreamingSampleStats() {
        this(Range.Type.EXCLUDE);
    }

    /**
     * Creates a new sampler with specified use of {@code Ranges}.
     *
     * @param rangesType either {@link Range.Type#INCLUDE} 
     *        or {@link Range.Type#EXCLUDE}
     */
    public StreamingSampleStats(Range.Type rangesType) {
        processors = CollectionFactory.list();
        ranges = CollectionFactory.list();
        noDataRanges = CollectionFactory.list();
        this.rangesType = rangesType;
    }

    /**
     * Adds a statistic to those calculated by this sampler.
     * If the same statistic was previously set then calling this method
     * has no effect.
     *
     * @param stat the statistic
     * @see Statistic
     */
    public void setStatistic(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            p = factory.getForStatistic(stat);

            if (p == null) {
                LOGGER.log(Level.SEVERE, "Unsupported Statistic: {0}", stat);
            } else {
                processors.add(p);

                // apply cached excluded ranges to the new processor
                for (Range<Double> range : ranges) {
                    p.addRange(range, rangesType);
                }

                for (Range<Double> nRange : noDataRanges) {
                    p.addNoDataRange(nRange);
                }
            }
        }
    }

    /**
     * Adds the given statistics to those that will be calculated by this sampler.
     *
     * @param stats the statistics
     * 
     * @see #setStatistic(Statistic)
     */
    public void setStatistics(Statistic[] stats) {
        for (Statistic stat : stats) {
            setStatistic(stat);
        }
    }

    /**
     * Tests whether the specified statistic is currently set. Note that
     * statistics can be set indirectly because of logical groupings. For
     * example, if {@code Statistic.MEAN} is set then {@code SDEV} and
     * {@code VARIANCE} will also be set as these three are calculated
     * together.  The same is true for {@code MIN}, {@code MAX} and {@code RANGE}.
     *
     * @param stat the statistic
     *
     * @return {@code true} if the statistic has been set; {@code false} otherwise.
     */
    public boolean isSet(Statistic stat) {
        return findProcessor(stat) != null;
    }

    /**
     * Adds a range of values to be considered as NoData and then to be excluded
     * from the calculation of <b>all</b> statistics. NoData ranges take precedence
     * over included / excluded data ranges.
     *
     * @param noData the range defining NoData values
     */
    public void addNoDataRange(Range<Double> noData) {
        noDataRanges.add(new Range<Double>(noData));

        for (Processor p : processors) {
            p.addNoDataRange(noData);
        }
    }

    /**
     * Adds a single value to be considered as NoData.
     *
     * @param noData the value to be treated as NoData
     *
     * @see #addNoDataRange(Range)
     */
    public void addNoDataValue(Double noData) {
        if (noData != null && !noData.isNaN()) {
            addNoDataRange(new Range<Double>(noData));
        }
    }

    /**
     * Adds a range of values to include in or exclude from the calculation 
     * of <b>all</b> statistics. If further statistics are set after calling
     * this method the range will be applied to them as well.
     *
     * @param range the range to include/exclude
     */
    public void addRange(Range<Double> range) {
        ranges.add(new Range<Double>(range));

        for (Processor p : processors) {
            p.addRange(range);
        }
    }

    /**
     * Adds a range of values to include in or exclude from the calculation 
     * of <b>all</b> statistics. If further statistics are set after calling
     * this method the range will be applied to them as well.
     *
     * @param range the range to include/exclude
     * @param rangesType one of {@link Range.Type#INCLUDE} or {@link Range.Type#EXCLUDE}
     */
    public void addRange(Range<Double> range, Range.Type rangesType) {
        for (Processor p : processors) {
            p.addRange(range, rangesType);
        }
        ranges.add(new Range<Double>(range));
    }

    /**
     * Gets the statistics that are currently set.
     *
     * @return the statistics
     */
    public Set<Statistic> getStatistics() {
        Set<Statistic> stats = CollectionFactory.orderedSet();
        for (Processor p : processors) {
            for (Statistic s : p.getSupported()) {
                stats.add(s);
            }
        }

        return stats;
    }

    /**
     * Gets the current value of a running statistic. If there have not
     * been enough samples provided to compute the statistic, Double.NaN
     * is returned.
     *
     * @param stat the statistic
     * @return the current value of the statistic
     *
     * @throws IllegalStateException if {@code stat} was not previously set
     */
    public Double getStatisticValue(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            throw new IllegalStateException(
                    "requesting a result for a statistic that hasn't been set: " + stat);
        }

        return p.get(stat);
    }

    /**
     * Gets the number of sample values that have been accepted for the
     * specified {@code Statistic}.
     * <p>
     * Note that different statistics might have been set at different
     * times in the sampling process.
     *
     * @param stat the statistic
     *
     * @return number of samples that have been accepted
     *
     * @throws IllegalArgumentException if the statistic hasn't been set
     */
    public long getNumAccepted(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            throw new IllegalArgumentException(
                    "requesting sample size for a statistic that is not set: " + stat);
        }

        return p.getNumAccepted();
    }

    /**
     * Gets the number of sample values that have been <b>offered</b> for the
     * specified {@code Statistic}. This might be higher than the value
     * returned by {@link #getNumAccepted} due to {@code nulls},
     * {@code Double.NaNs} and excluded values in the data stream.
     * <p>
     * Note that different statistics might have been set at different
     * times in the sampling process.
     *
     * @param stat the statistic
     *
     * @return number of samples that have been accepted
     *
     * @throws IllegalArgumentException if the statistic hasn't been set
     */
    public long getNumOffered(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            throw new IllegalArgumentException(
                    "requesting sample size for a statistic that is not set: " + stat);
        }

        return p.getNumOffered();
    }

    /**
     * Gets the number of NaN values that have been offered.
     * Note that different statistics might have been set at different
     * times in the sampling process.
     *
     * @param stat the statistic
     *
     * @return number of NaN samples offered
     *
     * @throws IllegalArgumentException if the statistic hasn't been set
     */
    public long getNumNaN(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            throw new IllegalArgumentException(
                    "requesting sample size for a statistic that is not set: " + stat);
        }

        return p.getNumNaN();
    }

    /**
     * Gets the number of NoData values (including NaN) that have been offered.
     * Note that different statistics might have been set at different
     * times in the sampling process.
     *
     * @param stat the statistic
     *
     * @return number of NoData samples offered
     *
     * @throws IllegalArgumentException if the statistic hasn't been set
     */
    public long getNumNoData(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            throw new IllegalArgumentException(
                    "requesting sample size for a statistic that is not set: " + stat);
        }

        return p.getNumNoData();
    }


    /**
     * Offers a sample value. Offered values are filtered through excluded ranges.
     * {@code Double.NaNs} and {@code nulls} are excluded by default.
     *
     * @param sample the sample value
     */
    public void offer(Double sample) {
        for (Processor p : processors) {
            p.offer(sample);
        }
    }

    /**
     * Offers an array of sample values.
     *
     * @param samples the sample values
     */
    public void offer(Double[] samples) {
        for (int i = 0; i < samples.length; i++) {
            offer(samples[i]);
        }
    }

    /**
     * Searches the list of {@code Processors} for one that supports
     * the given {@code Statistic}.
     *
     * @param stat the statistic
     *
     * @return the supporting {@code Processor} or null if one has not
     *         been set for the statistic
     */
    private Processor findProcessor(Statistic stat) {
        for (Processor p : processors) {
            if (p.getSupported().contains(stat)) {
                return p;
            }
        }

        return null;
    }

    /**
     * Gets the values of all statistics calculated by this sampler.
     * 
     * @return calculated values
     */
    public Map<Statistic, Double> getStatisticValues() {
        Map<Statistic, Double> results = CollectionFactory.orderedMap();

        for (Statistic s : getStatistics()) {
            results.put(s, getStatisticValue(s));
        }

        return results;
    }
}

