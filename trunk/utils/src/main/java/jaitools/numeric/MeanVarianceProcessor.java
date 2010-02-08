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
 * A Processor to calculate running mean and variance. The algorithm used is
 * that of Welford (1962) which was presented by Knuth:
 * <blockquote>
 * Donald E. Knuth (1998). The Art of Computer Programming, volume 2: Seminumerical Algorithms, 3rd edn., p. 232.
 * </blockquote>
 * The algorithm is described online at:
 * <blockquote>
 * http://en.wikipedia.org/wiki/Algorithms_for_calculating_variance#On-line_algorithm
 * </blockquote>
 * 
 * @see Statistic
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
class MeanVarianceProcessor extends AbstractProcessor {

    private static final Set<Statistic> SUPPORTED = new HashSet<Statistic>();
    static {
        SUPPORTED.add(Statistic.MEAN);
        SUPPORTED.add(Statistic.SDEV);
        SUPPORTED.add(Statistic.VARIANCE);
    };


    private Double mOld;
    private Double mNew;
    private Double s;

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
            if (getNumOffered() == 1) {
                mOld = mNew = sample;
                s = 0.0;
            } else {
                mNew = mOld + (sample - mOld) / getNumOffered();
                s = s + (sample - mOld) * (sample - mNew);
                mOld = mNew;
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

        final long n = getNumAccepted();
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
                throw new IllegalArgumentException(stat + " not supported by " + getClass().getName());
        }

        return Double.NaN;
    }
}
