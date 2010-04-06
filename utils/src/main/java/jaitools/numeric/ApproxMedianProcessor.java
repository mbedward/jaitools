/*
 * Copyright 2009-2010 Michael Bedward
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;


/**
 * Processor for the approximate median {@code Statistic.APPROX_MEDIAN}.
 *
 * Calculation of an exact median is only possible by storing all sample
 * values. For very large data streams, this processor will calculate an
 * approximate median using the <i>remedian</i> estimator:
 * <blockquote>
 * PJ Rousseeuw and GW Bassett (1990)
 * <i>The remedian: a robust averaging method for large data sets.</i>
 * Journal of the American Statistical Society 85:97-104
 * </blockquote>
 * <p>
 * The remedian estimator performs badly with non-stationary data, e.g. a
 * data stream that is monotonically increasing will result in an over-estimate.
 * If possible (which it probably isn't), it will help to de-trend or randomly
 * order the data prior to streaming it.
 *
 * @see Statistic
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ApproxMedianProcessor extends AbstractProcessor {

    private static final Set<Statistic> SUPPORTED = Collections.singleton(Statistic.APPROX_MEDIAN);

    // this must be an odd value
    private static final int BASE = 21;

    private static final int MEDIAN_POS = BASE / 2;

    private boolean needsCalculation = true;
    private double remedian;

    private static class Buffer {

        double[] data = new double[BASE];
        int pos = 0;

        void add(double value) {
            data[pos++] = value;
        }

        boolean isFull() {
            return pos >= BASE;
        }
    }

    private List<Buffer> buffers;
    private Buffer buf0;

    private static class WeightedSample implements Comparable<WeightedSample> {

        double value;
        long weight;

        public int compareTo(WeightedSample other) {
            return Double.compare(value, other.value);
        }
    }

    /**
     * Default constructor.
     */
    public ApproxMedianProcessor() {
        buffers = CollectionFactory.list();
        buf0 = new Buffer();
        buffers.add(buf0);
    }

    /**
     * {@inheritDoc}
     */
    public Collection<Statistic> getSupported() {
        return SUPPORTED;
    }

    /**
     * {@inheritDoc}
     */
    protected boolean update(Double sample) {
    	if (isAccepted(sample)) {
            if (buf0.isFull()) {
                cascade(0);
            }
            buf0.add(sample);
            needsCalculation = true;
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Double get(Statistic stat) {
        if (SUPPORTED.contains(stat)) {
            if (getNumAccepted() == 0) {
                return Double.NaN;
            }
            if (getNumAccepted() == 1) {
                return buf0.data[0];
            }

            if (needsCalculation) {
                /*
                 * Calculate the remedian as the weighted median of the buffer values
                 * where the weight for each value in buffer i is BASE^i, i = 0..numBuffers-1
                 */
                List<WeightedSample> samples = CollectionFactory.list();
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
                long nHalf = getNumAccepted() / 2;
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

        throw new IllegalArgumentException(stat + " not supported by " + getClass().getName());
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
        if (level + 1 < buffers.size()) {
            nextBuf = buffers.get(level + 1);
        } else {
            nextBuf = new Buffer();
            buffers.add(nextBuf);
        }
        if (nextBuf.isFull()) {
            cascade(level + 1);
        }
        buf.pos = 0;
        nextBuf.add(median);
    }

}
