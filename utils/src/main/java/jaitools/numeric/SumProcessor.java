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
import java.util.Set;

/**
 * Processor for SUM {@code Statistic}.
 *
 * @see Statistic
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class SumProcessor extends AbstractProcessor {

    private static final Set<Statistic> SUPPORTED = Collections.singleton(Statistic.SUM);

    Double sum = 0.0;

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
            sum += sample;
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
            case SUM:
                return sum;

            default:
                throw new IllegalArgumentException(stat + " not supported by " + getClass().getName());
        }
    }

}
