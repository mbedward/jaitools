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

import java.util.List;

import org.jaitools.Factory;
import org.jaitools.lookup.Lookup;


/**
 * A (very) simple factory class used by {@code StreamingSampleStats} to create
 * {@code Processors} which handle the calculation of various statistics based on
 * values in a data stream.
 *
 * @see Processor
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class ProcessorFactory extends Factory {

    private static final String spiName = 
            ProcessorFactory.class.getPackage().getName() + ".Processor";

    static {
        addSpi(spiName);
    }

    /**
     * Return a new instance of a {@code Processor} that claims to support
     * the given {@code Statistic}.
     * 
     * @param stat the statistic
     * 
     * @return a new instance of the first {@code Processor} class found that supports
     *         {@code stat}; or null if no class was found
     */
    public Processor getForStatistic(Statistic stat) {
        Processor instance = null;

        List<Class> providers = Lookup.getProviders(spiName);
        for (Class provider : providers) {
            try {
                Processor p = (Processor) provider.newInstance();
                if (p.getSupported().contains(stat)) {
                    instance = p;
                    break;
                }

            } catch (IllegalAccessException ex) {

            } catch (InstantiationException ex) {

            }
        }

        return instance;
    }

}
