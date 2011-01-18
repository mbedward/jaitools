/*
 * Copyright 2011 Michael Bedward
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

package jaitools.jiffle.parser;

import jaitools.CollectionFactory;
import java.util.Map;

/**
 * A lookup for named constants used by the Jiffle compiler.
 * <p>
 * The following constants are recognized:
 * <pre>
 * M_E     The base of natural logarithms (e)
 * M_PI    Pi
 * M_PI_2  Pi / 2
 * M_PI_4  Pi / 4
 * M_SQRT2 Squre root of 2
 * </pre>
 * In addition, any of the following can be used for {@code Double.NaN}
 * <pre>
 * M_NaN
 * M_NAN
 * NaN
 * NAN
 * </pre>
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ConstantLookup {

    private static final Map<String, Double> constants;
    static {
        constants = CollectionFactory.map();
        
        constants.put("M_E", Math.E);
        constants.put("M_PI", Math.PI);
        constants.put("M_PI_2", Math.PI / 2);
        constants.put("M_PI_4", Math.PI / 4);
        constants.put("M_SQRT2", Math.sqrt(2.0));
        
        // be generous with NaN names
        constants.put("M_NaN", Double.NaN);
        constants.put("M_NAN", Double.NaN);
        constants.put("NaN", Double.NaN);
        constants.put("NAN", Double.NaN);
    }
    
    /**
     * Checks if a constant is recognized by Jiffle.
     * 
     * @param name the name
     * 
     * @return {@code true} if the constant is recognized;
     *         {@code false} otherwise
     */
    public static boolean isDefined(String name) {
        return constants.containsKey(name);
    }

    /**
     * Gets the value of a named constant.
     * 
     * @param name the constant
     * 
     * @return the value
     * 
     * @throws IllegalArgumentException if {@code name} is not recognized
     */
    public static double getValue(String name) {
        Double value = constants.get(name);
        if (value == null) {
            throw new IllegalArgumentException("Unknown constant: " + name);
        }
        return value;
    }
 
}
