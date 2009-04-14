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

import jaitools.utils.CollectionFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * A class to calculate summary statistics for a sample of Double-valued
 * buffers that is received as a (potentially long) stream of values rather
 * than in a single batch.
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
 * <pre>{@code \u0000
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
 * }</pre>
 * 
 * @author Michael Bedward
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

        return proc.getCount();
    }

    /**
     * Add a sample value and update all currently set statistics.
     *
     * @param sample the new sample value
     */
    public void addSample(Double sample) {
        for (Processor proc : procTable.keySet()) {
            proc.add(sample);
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
     * Initialize a processor for the exact or approximate median
     */
    private void createMedianProcessor(Statistic stat) {
        Processor proc = null;
        switch (stat) {
            case MEDIAN:
                proc = new ExactMedianProcessor();
                procTable.put(proc, 1);
                procByStat.put(Statistic.MEDIAN, proc);
                break;

            case APPROX_MEDIAN:
                proc = new RemedianProcessor();
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


    /**
     * Base class for statistics processors. Sub-classes override the
     * update and get methods.
     */
    private static abstract class Processor {

        private long count = 0;

        long getCount() {
            return count;
        }

        final void add(Double sample) {
            count++;
            update(sample);
        }

        abstract void update(Double sample);

        abstract Double get(Statistic stat);
    }


    /**
     * Processor for extrema statistics
     */
    private static class ExtremaProcessor extends Processor {

        Double min;
        Double max;

        @Override
        void update(Double sample) {
            if (getCount() == 1) {
                min = max = sample;
            } else {
                if (sample > max) {
                    max = sample;
                }

                if (sample < min) {
                    min = sample;
                }
            }
        }

        @Override
        Double get(Statistic stat) {
            if (getCount() == 0) {
                return Double.NaN;
            }

            switch (stat) {
                case MAX:
                    return max;

                case MIN:
                    return min;

                case RANGE:
                    return max - min;

                default:
                    throw new IllegalArgumentException(
                            "Invalid argument for this processor type: " + stat);
            }
        }
    }


    /**
     * Processor for the exact median
     */
    private static class ExactMedianProcessor extends Processor {

        private List<Double> values = CollectionFactory.newList();
        private boolean calculationRequired = true;
        private double median;

        @Override
        void update(Double sample) {
            if (getCount() >= Integer.MAX_VALUE) {
                throw new IllegalStateException("Too many samples for exact median");
            }

            values.add(sample);
            calculationRequired = true;
        }

        @Override
        Double get(Statistic stat) {
            if (getCount() == 0) {
                return Double.NaN;
            }

            if (calculationRequired) {
                Collections.sort(values);
                int n0 = (int) getCount() / 2;
                if (getCount() % 2 == 1) {
                    median = values.get(n0);
                } else {
                    median = (values.get(n0) + values.get(n0-1)) / 2;
                }
                calculationRequired = false;
            }

            return median;
        }

    }

    /*
     * Processor for the remedian: a large dataset estiator
     * of the sample median
     */
    private static class RemedianProcessor extends Processor {

        // this must be an odd value
        private final int BASE = 21;
        private final int MEDIAN_POS = BASE / 2;

        private boolean needsCalculation = true;
        private double remedian;

        private class Buffer {
            double[] data = new double[BASE];
            int pos = 0;

            void add(double value) {
                data[pos++] = value;
            }

            boolean isFull() { return pos >= BASE; }
        }

        private List<Buffer> buffers;
        private Buffer buf0;

        private class WeightedSample implements Comparable<WeightedSample> {
            double value;
            long weight;

            public int compareTo(WeightedSample other) {
                return Double.compare(value, other.value);
            }
        }


        RemedianProcessor() {
            buffers = CollectionFactory.newList();
            buf0 = new Buffer();
            buffers.add(buf0);
        }


        @Override
        void update(Double sample) {
            if (buf0.isFull()) {
                cascade(0);
            }
            buf0.add(sample);

            needsCalculation = true;
        }

        /*
         * Calculate the median of the values in the full buffer at
         * the given level and store the result in the next
         * available position of the buffer at level+1, creating this
         * next buffer if necessary. If the next buffer is also full
         * it is cascaded with a recursive call.
         */
        private void cascade(int level) {
            Buffer buf = buffers.get(level);
            Arrays.sort(buf.data);
            double median = buf.data[MEDIAN_POS];

            Buffer nextBuf;
            if (level+1 < buffers.size()) {
                nextBuf = buffers.get(level+1);
            } else {
                nextBuf = new Buffer();
                buffers.add(nextBuf);
            }

            if (nextBuf.isFull()) {
                cascade(level+1);
            }

            buf.pos = 0;
            nextBuf.add(median);
        }

        /**
         * Calculate the remedian as the weighted median of the buffer values
         * where the weight for each value in buffer i is BASE^i, i = 0..numBuffers-1
         *
         * @param stat ignored
         * @return the value of the remedian
         */
        @Override
        Double get(Statistic stat) {
            if (getCount() == 0) {
                return Double.NaN;
            }

            if (needsCalculation) {
                List<WeightedSample> samples = CollectionFactory.newList();
                long weight = 1;
                for (Buffer buf : buffers) {
                    for (int i = 0; i < buf.pos; i++) {
                        WeightedSample datum = new WeightedSample();
                        datum.value = buf.data[i];
                        datum.weight = weight;
                        samples.add(datum);
                    }

                    weight = weight * BASE;
                }

                Collections.sort(samples);

                long nHalf = getCount() / 2;
                long n = 0;
                Iterator<WeightedSample> iter = samples.iterator();
                WeightedSample datum = null;
                
                while (n < nHalf) {
                    datum = iter.next();
                    n += datum.weight;
                }

                remedian = datum.value;
                needsCalculation = false;
            }

            return remedian;
        }
    }


    /**
     * A Processor to calculate running mean and variance. The algorithm used is that
     * that of Welford (1962) which was presented in Knuth's <i>The Art of Computer
     * Programming (3rd ed)</i> vol. 2, p. 232. Also described online at:
     * http://www.johndcook.com/standard_deviation.html
     */
    private static class MeanVarianceProcessor extends Processor {

        Double mOld, mNew;
        Double s;

        @Override
        void update(Double sample) {
            if (getCount() == 1) {
                mOld = mNew = sample;
                s = 0.0d;
            } else {
                mNew = mOld + (sample - mOld) / getCount();
                s = s + (sample - mOld) * (sample - mNew);
                mOld = mNew;
            }
        }

        @Override
        Double get(Statistic stat) {
            long n = getCount();

            switch (stat) {
                case MEAN:
                    if (n > 0) {
                        return mNew;
                    }
                    break;

                case SDEV:
                    if (n > 1) {
                        return Math.sqrt(s / (n - 1));
                    }
                    break;

                case VARIANCE:
                    if (n > 1) {
                        return s / (n - 1);
                    }
                    break;

                default:
                    throw new IllegalArgumentException(
                            "Invalid argument for this processor type: " + stat);
            }

            return Double.NaN;
        }
    }
}

