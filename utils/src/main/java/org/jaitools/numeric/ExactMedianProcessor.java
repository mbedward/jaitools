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
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jaitools.CollectionFactory;


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
                Logger.getLogger("org.jaitools.numeric").log(
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
        if (isAccepted(sample)) {
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
                int n0 = (int) getNumAccepted() / 2;
                if (getNumAccepted() % 2 == 1) {
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
