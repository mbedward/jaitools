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
import java.util.EnumSet;
import java.util.Map;

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
 *     strmStats.addSample(value);
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
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class StreamingSampleStats {

    private Map<Statistic, Processor> procByStat;
    private Map<Processor, Integer> procTable;

    /**
     * Constructor
     */
    public StreamingSampleStats() {
        procByStat = CollectionFactory.newMap();
        procTable = CollectionFactory.newMap();
    }

    /**
     * Set a statistic to be calculated as sample values are added.
     * If the same statistic was previously set calling this method
     * has the effect of clearing any previous result. In addition,
     * because of the relationship between statistics in the calculations,
     * calling this method for one statistic in the following groups will
     * cause the other statistics in that group to be reset:
     * <ul>
     * <li>MIN, MAX, RANGE
     * <li>MEAN, SDEV, VARIANCE
     * </ul>
     *
     * @param stat the requested statistic
     * @see Statistic
     */
    public void setStatistic(Statistic stat) {
        Processor proc = procByStat.get(stat);
        if (proc != null) {
            // treat a duplicate request as resetting this
            // statistic
            procByStat.remove(stat);
            Integer count = procTable.get(proc);
            if (count == 1) {
                procTable.remove(proc);
            }
        }

        switch (stat) {
            case MAX:
            case MIN:
            case RANGE:
                createExtremaProcessor(stat);
                break;

            case MEDIAN:
            case APPROX_MEDIAN:
                createMedianProcessor(stat);
                break;

            case MEAN:
            case SDEV:
            case VARIANCE:
                createVarianceProcessor(stat);
                break;

            case SUM:
                createSumProcessor(stat);
                break;
        }
    }

    /**
     * Convenience method: sets the selected statistics to be calculated
     * as subsequent sample values are added.
     *
     * @param stats the requested statistics
     * @see #setStatistic(jaitools.numeric.Statistic)
     * @see Statistic
     */
    public void setStatistics(Statistic[] stats) {
        for (Statistic stat : stats) {
            setStatistic(stat);
        }
    }

    /**
     * Get the array of statistics currently set.
     * @return a new array of Statistic enum constants.
     */
    public Statistic[] getStatistics() {
        Statistic[] stats = new Statistic[procByStat.size()];
        int k = 0;
        for (Statistic stat : procByStat.keySet()) {
            stats[k++] = stat;
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
        Processor proc = procByStat.get(stat);
        if (proc == null) {
            throw new IllegalStateException(
                    "requesting a result for a statistic that hasn't been set: " + stat);
        }

        return proc.get(stat);
    }

    /**
     * Get the current sample size for the specified statistic.
     * Note that the sample size has to be retrieved with reference to a
     * statistic because there is no requirement for the statistics
     * having been set at the same time.
     *
     * @param stat the statistic that the sample size pertains to
     * @return number of samples added since the statistic was set
     * @throws IllegalArgumentException if the statistic hasn't been set
     */
    public long size(Statistic stat) {
        Processor proc = procByStat.get(stat);
        if (proc == null) {
            throw new IllegalArgumentException(
                    "requesting sample size for a statistic that is not set: " + stat);
        }

        return proc.getNumOffered();
    }

    /**
     * Add a sample value and update all currently set statistics.
     *
     * @param sample the new sample value
     */
    public void addSample(Double sample) {
        for (Processor proc : procTable.keySet()) {
            proc.offer(sample);
        }
    }

    /**
     * Convenience method: adds an array of new sample values and
     * updates all currently set statistics.
     *
     * @param samples the new sample values
     */
    public void addSamples(Double[] samples) {
        for (int i = 0; i < samples.length; i++) {
            addSample(samples[i]);
        }
    }

    /**
     * Initialize a processor for the requested extremum statistic:
     * one of Statistic.MIN, Statistic.MAX or Statistic.RANGE
     */
    private void createExtremaProcessor(Statistic stat) {
        Processor proc = null;
        Integer count = 0;

        for (Statistic related : EnumSet.of(Statistic.MAX, Statistic.MIN, Statistic.RANGE)) {
            proc = procByStat.get(related);
            if (proc != null) {
                count = procTable.get(proc);
                break;
            }
        }

        if (proc == null) {
            proc = new ExtremaProcessor();
        }

        procByStat.put(stat, proc);
        procTable.put(proc, count+1);
    }

    /**
     * Initialize a processor for the requested extremum statistic:
     * one of Statistic.MIN, Statistic.MAX or Statistic.RANGE
     */
    private void createSumProcessor(Statistic stat) {
        Processor proc = null;
        Integer count = 0;
        
        for (Statistic related : EnumSet.of(Statistic.SUM)) {
            proc = procByStat.get(related);
            if (proc != null) {
                count = procTable.get(proc);
                break;
            }
        }
        
        if (proc == null) {
            proc = new SumProcessor();
        }
        
        procByStat.put(stat, proc);
        procTable.put(proc, count+1);
    }

    /**
     * Initialize a processor for the exact or approximate median
     */
    private void createMedianProcessor(Statistic stat) {
        AbstractProcessor proc = null;
        switch (stat) {
            case MEDIAN:
                proc = new ExactMedianProcessor();
                procTable.put(proc, 1);
                procByStat.put(Statistic.MEDIAN, proc);
                break;

            case APPROX_MEDIAN:
                proc = new ApproxMedianProcessor();
                procTable.put(proc, 1);
                procByStat.put(Statistic.APPROX_MEDIAN, proc);
                break;
        }
    }

    /**
     * Initialize a processor for one of Statistic.MEAN, Statistic.VARIANCE
     * or Statistic.SDEV
     */
    private void createVarianceProcessor(Statistic stat) {
        Processor proc = null;
        Integer count = 0;

        for (Statistic related : EnumSet.of(Statistic.MEAN, Statistic.SDEV, Statistic.VARIANCE)) {
            proc = procByStat.get(related);
            if (proc != null) {
                count = procTable.get(proc);
                break;
            }
        }

        if (proc == null) {
            proc = new MeanVarianceProcessor();
        }

        procByStat.put(stat, proc);
        procTable.put(proc, count+1);
    }
}

