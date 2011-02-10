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

import jaitools.Factory;
import jaitools.lookup.Lookup;
import java.util.List;

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

    static {
        addSpi("jaitools.numeric.Processor");
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

        List<Class> providers = Lookup.getProviders("jaitools.numeric.Processor");
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
