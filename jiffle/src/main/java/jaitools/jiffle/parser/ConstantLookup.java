/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle.parser;

import jaitools.CollectionFactory;
import java.util.Map;

/**
 * A symbol table for user-defined variables and some pre-defined, named constants
 * (PI, E, NaN).
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ConstantLookup {

    private static final Map<String, Double> constants;
    static {
        constants = CollectionFactory.map();
        constants.put("PI", Math.PI);
        constants.put("E", Math.E);
        constants.put("NaN", Double.NaN);
    }
    
    public static boolean isDefined(String name) {
        return constants.containsKey(name);
    }

    public static double getValue(String name) {
        Double value = constants.get(name);
        return value == null ? Double.NaN : value.doubleValue();
    }
 
}
