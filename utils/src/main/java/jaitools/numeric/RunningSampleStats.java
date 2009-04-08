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
import java.util.EnumSet;
import java.util.Map;

/**
 * A class to calculate summary statistics for a sample of Double-valued
 * data that is received as a (potentially long) stream of values rather
 * than in a single batch.
 *
 * @author Michael Bedward
 */
public class RunningSampleStats {

    private Map<Statistic, StatBuffer> buffers;

    /**
     * Constructor
     */
    public RunningSampleStats() {
        buffers = CollectionFactory.newTreeMap();
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
        switch (stat) {
            case MAX:
            case MIN:
            case RANGE:
                createExtremaBuffer(stat);
                break;

            case MEDIAN:
                createMedianBuffer();
                break;

            case MEAN:
            case SDEV:
            case VARIANCE:
                createVarianceBuffer(stat);
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
        Statistic[] stats = new Statistic[buffers.size()];
        int k = 0;
        for (Statistic stat : buffers.keySet()) {
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
        StatBuffer buffer = buffers.get(stat);
        if (buffer == null) {
            throw new IllegalStateException(
                    "requesting a result for a statistic that hasn't been set: " + stat);
        }

        return buffer.get(stat);
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
        StatBuffer buffer = buffers.get(stat);
        if (buffer == null) {
            throw new IllegalArgumentException(
                    "requesting sample size for a statistic that is not set: " + stat);
        }

        return buffer.getCount();
    }

    /**
     * Add a sample value and update all currently set statistics.
     *
     * @param sample the new sample value
     */
    public void addSample(Double sample) {
        for (Statistic stat : buffers.keySet()) {
            buffers.get(stat).add(sample);
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
     * Initialize a buffer for the requested extremum statistic:
     * one of Statistic.MIN, Statistic.MAX or Statistic.RANGE
     */
    private void createExtremaBuffer(Statistic stat) {
        StatBuffer buffer = null;
        for (Statistic existing : EnumSet.of(Statistic.MAX, Statistic.MIN, Statistic.RANGE)) {
            if (stat != existing) {
                buffer = buffers.get(existing);
                if (buffer != null) {
                    break;
                }
            }
        }

        if (buffer == null) {
            buffer = new StatBufferExtrema();
        }

        buffers.put(stat, buffer);
    }

    /**
     * Create a StatBuffer object
     */
    private void createMedianBuffer() {
        buffers.put(Statistic.MEDIAN, new StatBufferMedian());
    }


    private void createVarianceBuffer(Statistic stat) {
        StatBuffer buffer = null;
        for (Statistic existing : EnumSet.of(Statistic.MEAN, Statistic.SDEV, Statistic.VARIANCE)) {
            if (stat != existing) {
                buffer = buffers.get(existing);
                if (buffer != null) {
                    break;
                }
            }
        }

        if (buffer == null) {
            buffer = new StatBufferMeanVariance();
        }

        buffers.put(stat, buffer);
    }

    private static abstract class StatBuffer {

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

    private static class StatBufferExtrema extends StatBuffer {

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
                            "Invalid argument for this buffer type: " + stat);
            }
        }
    }

    private static class StatBufferMedian extends StatBuffer {

        @Override
        void update(Double sample) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        Double get(Statistic stat) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }

    /**
     * A buffer to calculate running mean and variance. The algorithm used is that
     * that of Welford (1962) which was presented in Knuth's <i>The Art of Computer
     * Programming (3rd ed)</i> vol. 2, p. 232. Also described online at:
     * http://www.johndcook.com/standard_deviation.html
     */
    private static class StatBufferMeanVariance extends StatBuffer {

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
                            "Invalid argument for this buffer type: " + stat);
            }

            return Double.NaN;
        }
    }
}

