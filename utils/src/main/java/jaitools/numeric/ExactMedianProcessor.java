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
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Processor for the exact median {@code Statistic.MEDIAN}.
 * <p>
 * <b>Note:</b> this processor stores sll accepted sample values in memory in order
 * to calculate the exact median. For very large data streams {@linkplain Statistic#APPROX_MEDIAN}
 * might be preferred.
 *
 * @see Statistic
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ExactMedianProcessor extends AbstractProcessor {

    private static final Set<Statistic> SUPPORTED = Collections.singleton(Statistic.MEDIAN);

    private List<Double> values = CollectionFactory.list();
    private boolean calculationRequired = true;
    private double median;

    /**
     * {@inheritDoc}
     */
    public Collection<Statistic> getSupported() {
        return Collections.unmodifiableCollection(SUPPORTED);
    }

    /**
     * {@inheritDoc}
     * The maximum number of sample values than can be stored is
     * {@code Integer.MAX_VALUE} (available memory permitting).
     * Once this limit is reached, subsequent values will be
     * rejected and a warning message will be logged.
     */
    @Override
    public final void offer(Double sample) {
        numOffered++ ;

        if (getNumAccepted() >= Integer.MAX_VALUE) {
            // only log a warning for the first extraneous value
            if (getNumOffered() == Integer.MAX_VALUE) {
                Logger.getLogger("jaitools.numeric").log(
                        Level.WARNING, "Too many values for exact median calculation");
            }
        }

        if (update(sample)) {
            numAccepted++ ;
        }
    }

    /**
     * {@inheritDoc}.
     */
    @Override
    protected boolean update(Double sample) {
        if (!isExcluded(sample)) {
            values.add(sample);
            calculationRequired = true;
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}.
     */
    public Double get(Statistic stat) {
        if (SUPPORTED.contains(stat)) {
            if (getNumAccepted() == 0) {
                return Double.NaN;
            }

            if (calculationRequired) {
                Collections.sort(values);

                int n0 = (int) getNumOffered() / 2;
                if (getNumOffered() % 2 == 1) {
                    median = values.get(n0);
                } else {
                    median = (values.get(n0) + values.get(n0 - 1)) / 2;
                }
                calculationRequired = false;
            }
            return median;
        }

        throw new IllegalArgumentException(stat + " not supported by " + getClass().getName());
    }
}
