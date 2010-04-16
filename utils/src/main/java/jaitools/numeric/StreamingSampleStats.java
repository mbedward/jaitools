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

import jaitools.CollectionFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

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
 * @source $URL$
 * @version $Id$
 */
public class StreamingSampleStats {

    private static final Logger LOGGER = Logger.getLogger("jaitools.numeric");
    private ProcessorFactory factory = new ProcessorFactory();
    private List<Processor> processors;
    private List<Range<Double>> ranges;
    private List<Range<Double>> noDataRanges;
    private final Range.Type rangesType;

    /**
     * Default constructor
     */
    public StreamingSampleStats() {
        this(Range.Type.EXCLUDE);
    }

    /**
     * Create a new instance with specified use of {@code Ranges}.
     *
     * @param rangesType normally either {@linkplain Range.Type#INCLUDE} to indicate that
     *        {@code Ranges} will define values to operate on, or {@linkplain Range.Type#EXCLUDE}
     *        when {@code Ranges} will define values to exclude from operations.
     */
    public StreamingSampleStats(Range.Type rangesType) {
        processors = CollectionFactory.list();
        ranges = CollectionFactory.list();
        noDataRanges = CollectionFactory.list();
        this.rangesType = rangesType;
    }

    /**
     * Set a statistic to be calculated as sample values are added.
     * If the same statistic was previously set then calling this method
     * has no effect.
     *
     * @param stat the requested statistic
     * @see Statistic
     */
    public void setStatistic(Statistic stat) {
        Processor p = findProcessor(stat);
        if (p == null) {
            p = factory.getForStatistic(stat);

            if (p == null) {
                LOGGER.severe("Unsupported Statistic: " + stat);
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
     * Convenience method: sets the specified statistics.
     *
     * @param stats the statistics
     * @see #setStatistic(Statistic)
     */
    public void setStatistics(Statistic[] stats) {
        for (Statistic stat : stats) {
            setStatistic(stat);
        }
    }

    /**
     * Query whether the specified statistic is currently set. Note that
     * statistics can be set indirectly because of logical groupings. For
     * example, if {@code Statistic.MEAN} is set then {@code SDEV} and
     * {@code VARIANCE} will also be set as these three are calculated
     * together.  The same is true for {@code MIN}, {@code MAX} and {@code RANGE}.
     *
     * @param stat the statistic
     *
     * @return true if the statistic has been set; false otherwise.
     */
    public boolean isSet(Statistic stat) {
        return findProcessor(stat) != null;
    }

    /**
     * Add a range of values to exclude from the calculation of <b>all</b>
     * statistics. If further statistics are set after calling this method
     * the excluded range will be applied to them as well.
     *
     * @param exclude the {@code Range} to exclude
     *
     * @deprecated Please use {@link #addRange(Range)}
     */
    public void addExcludedRange(Range<Double> exclude) {
        ranges.add(new Range<Double>(exclude));

        for (Processor p : processors) {
            p.addExcludedRange(exclude);
        }
    }
    
    /**
     * Add a range of values to be considered as NoData and then to be excluded 
     * from the calculation of <b>all</b> statistics. NoData ranges take precedence
     * over included / excluded data ranges.
     *
     * @param noData the {@code Range} containing NoData values
     */
    public void addNoDataRange(Range<Double> noData) {
        noDataRanges.add(new Range<Double>(noData));

        for (Processor p : processors) {
            p.addNoDataRange(noData);
        }
    }

    /**
     * Convenience method for specifying a single value to be considered as NoData.
     *
     * @param noData the value to be treated as NoData
     *
     * @see #addNoDataRange(jaitools.numeric.Range)
     */
    public void addNoDataValue(Double noData) {
        if (noData != null && !noData.isNaN()) {
            addNoDataRange(new Range<Double>(noData));
        }
    }

    /**
     * Add a range of values to include/exclude from the calculation of <b>all</b>
     * statistics. If further statistics are set after calling this method
     * the range will be applied to them as well.
     *
     * @param range the {@code Range} to include/exclude
     */
    public void addRange(Range<Double> range) {
        ranges.add(new Range<Double>(range));

        for (Processor p : processors) {
            p.addRange(range);
        }
    }

    /**
     * Add a range of values to exclude/include from the calculation of <b>all</b>
     * statistics. If further statistics are set after calling this method
     * the range will be applied to them as well.
     *
     * @param range the {@code Range} to include/exclude
     * @param rangesType one of {@linkplain Range.Type#INCLUDE} or {@linkplain Range.Type#EXCLUDE}
     */
    public void addRange(Range<Double> range, Range.Type rangesType) {
        for (Processor p : processors) {
            p.addRange(range, rangesType);
        }
        ranges.add(new Range<Double>(range));
    }

    /**
     * Get the statistics that are currently set.
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
     * Get the (current) value of a running statistic. If there have not
     * been enough samples provided to compute the statistic, Double.NaN
     * is returned.
     *
     * @param stat
     * @return the (current) value of the statistic
     *
     * @throws IllegalStateException if stat was not previously set
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
     * Get the number of sample values that have been accepted for the
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
     * Get the number of sample values that have been <b>offered</b> for the
     * specified {@code Statistic}. This might be higher than the value
     * returned by {@linkplain #getNumAccepted} due to {@code nulls},
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
     * Get the number of sample values that are NaN
     * Note that different statistics might have been set at different
     * times in the sampling process.
     *
     * @param stat the statistic
     *
     * @return number of NaN samples received
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
     * Get the number of sample values that are noData (including NaN).
     * Note that different statistics might have been set at different
     * times in the sampling process.
     *
     * @param stat the statistic
     *
     * @return number of NoData samples received
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
     * Offer a sample value. Offered values are filtered through excluded ranges.
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
     * Convenience method: adds an array of new sample values and
     * updates all currently set statistics.
     *
     * @param samples the sample values
     */
    public void offer(Double[] samples) {
        for (int i = 0; i < samples.length; i++) {
            offer(samples[i]);
        }
    }

    /**
     * Search the list of {@code Processors} for one that supports
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

    public Map<Statistic, Double> getStatisticValues() {
        Map<Statistic, Double> results = CollectionFactory.orderedMap();

        for (Statistic s : getStatistics()) {
            results.put(s, getStatisticValue(s));
        }

        return results;
    }
}

