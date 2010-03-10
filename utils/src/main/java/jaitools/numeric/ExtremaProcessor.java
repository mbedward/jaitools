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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Processor for extrema {@code Statistics}: MIN, MAX and RANGE.
 *
 * @see Statistic
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ExtremaProcessor extends AbstractProcessor {

    private static final Set<Statistic> SUPPORTED = new HashSet<Statistic>();
    static {
        SUPPORTED.add(Statistic.MAX);
        SUPPORTED.add(Statistic.MIN);
        SUPPORTED.add(Statistic.RANGE);
    };

    private double min;
    private double max;

    /**
     * {@inheritDoc}
     */
    public Collection<Statistic> getSupported() {
        return Collections.unmodifiableCollection(SUPPORTED);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected boolean update(Double sample) {
        if (!isExcluded(sample)) {
            if (getNumAccepted() == 0) {
                min = max = sample;

            } else {
                if (sample > max) {
                    max = sample;
                }
                if (sample < min) {
                    min = sample;
                }
            }
            return true;
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public Double get(Statistic stat) {
        if (getNumAccepted() == 0) {
            return Double.NaN;
        }

        switch (stat) {
            case MAX: return max;
            case MIN: return min;
            case RANGE: return max - min;
            
            default:
                throw new IllegalArgumentException(stat + " not supported by " + getClass().getName());
        }
    }

}
