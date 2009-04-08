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
 * A class to calculate summary statistics for a sample of double-valued
 * data that is received as a (potentially long) stream of values rather
 * than in a single batch.
 *
 * @author Michael Bedward
 */
public class RunningSampleStats {

    private Map<Statistic, StatBuffer> table;

    public RunningSampleStats() {
        table = CollectionFactory.newTreeMap();
    }

    public void setStatistics(Statistic[] stats) {
        for (Statistic stat : stats) {
            StatBuffer buffer = null;
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
    }

    public void addSample(double sample) {
        for (Statistic stat : table.keySet()) {
            table.get(stat).add(sample);
        }
    }

    private void createExtremaBuffer(Statistic stat) {
        StatBuffer buffer = null;
        for (Statistic existing : EnumSet.of(Statistic.MAX, Statistic.MIN, Statistic.RANGE)) {
            if (stat != existing) {
                buffer = table.get(existing);
                if (buffer != null) {
                    break;
                }
            }
        }

        if (buffer == null) {
            buffer = new StatBufferExtrema();
        }

        table.put(stat, buffer);
    }

    private void createMedianBuffer() {
        table.put(Statistic.MEDIAN, new StatBufferMedian());
    }

    private void createVarianceBuffer(Statistic stat) {
        StatBuffer buffer = null;
        for (Statistic existing : EnumSet.of(Statistic.MEAN, Statistic.SDEV, Statistic.VARIANCE)) {
            if (stat != existing) {
                buffer = table.get(existing);
                if (buffer != null) {
                    break;
                }
            }
        }

        if (buffer == null) {
            buffer = new StatBufferMeanVariance();
        }

        table.put(stat, buffer);
    }


    private static abstract class StatBuffer {

        protected long count = 0;

        abstract void add(double sample);

        abstract double get(Statistic stat);
    }

    private static class StatBufferExtrema extends StatBuffer {

        double min = Double.MAX_VALUE;
        double max = Double.MIN_VALUE;
        double range = 0d;

        @Override
        void add(double sample) {
            if (sample > max) {
                max = sample;
            }

            if (sample < min) {
                min = sample;
            }

            count++;
        }

        @Override
        double get(Statistic stat) {
            if (count == 0) {
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
        void add(double sample) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        double get(Statistic stat) {
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

        double mOld, mNew;
        double s;

        @Override
        void add(double sample) {
            count++;

            if (count == 1) {
                mOld = mNew = sample;
                s = 0.0d;
            } else {
                mNew = mOld + (sample - mOld) / count;
                s = s + (sample - mOld)*(sample - mNew);
                mOld = mNew;
            }
        }

        @Override
        double get(Statistic stat) {
            switch (stat) {
                case MEAN:
                    if (count > 0) {
                        return mNew;
                    }
                    break;

                case SDEV:
                    if (count > 1) {
                        return Math.sqrt(s / (count-1));
                    }
                    break;

                case VARIANCE:
                    if (count > 1) {
                        return s / (count-1);
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
