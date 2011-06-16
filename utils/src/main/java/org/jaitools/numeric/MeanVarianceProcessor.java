/* 
 *  Copyright (c) 2009-2010, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.numeric;

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
 * @version $Id$
 */
public class MeanVarianceProcessor extends AbstractProcessor {

    private static final Set<Statistic> SUPPORTED = new HashSet<Statistic>();
    static {
        SUPPORTED.add(Statistic.MEAN);
        SUPPORTED.add(Statistic.SDEV);
        SUPPORTED.add(Statistic.VARIANCE);
    };


    private double mOld;
    private double mNew;
    private double s;

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
    	if (isAccepted(sample)) {
            if (getNumAccepted() == 0) {  // first value
                mOld = mNew = sample;
                s = 0.0;
            } else {
                mNew = mOld + (sample - mOld) / (getNumAccepted() + 1);
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
