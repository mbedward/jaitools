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

import java.util.Collections;
import java.util.List;
import java.util.Map;

import jaitools.CollectionFactory;

/**
 * A lookup service used by the Jiffle compiler when parsing script options.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class OptionLookup {

    private static final List<OptionInfo> options;
    private static final Map<String, String> activeRuntimeExpr;
    private static final List<String> names;
    
    static {
        options = CollectionFactory.list();
        names = CollectionFactory.list();
        activeRuntimeExpr = CollectionFactory.map();
        
        OptionInfo info;
        String name;
        
        name = "outside";
        
        info = new OptionInfo(name,
                new String[] { OptionInfo.ANY_NUMBER, OptionInfo.NULL_KEYWORD });
        
        options.add(info);
        names.add(name);
        
        activeRuntimeExpr.put(name, 
                "_outsideValueSet = true;\n"
                + "_outsideValue = _VALUE_;");
    }
    
    /**
     * Tests if an option name is defined.
     * 
     * @param optionName the name
     * @return {@code true} if the name is defined; {@code false} otherwise
     */
    public static boolean isDefined(String optionName) {
        try {
            getInfo(optionName);
            return true;
            
        } catch (UndefinedOptionException ex) {
            return false;
        }
    }
    
    /**
     * Tests if a value is valid for the given option.
     * @param optionName option name
     * @param value the value as a String
     * @return {@code true} if the value is valid; {@code false} otherwise
     * @throws UndefinedOptionException if the name is not recognized
     */
    public static boolean isValidValue(String optionName, String value) 
            throws UndefinedOptionException {
        
        return getInfo(optionName).isValidValue(value);
    }
    
    /**
     * Gets the names known to the lookup service.
     * 
     * @return option names as an unmodifiable list
     */
    public static Iterable<String> getNames() {
        return Collections.unmodifiableList(names);
    }
    
    /**
     * Gets the runtime source for the given option name:value pair.
     * 
     * @param name option name
     * @param value option value
     * 
     * @return the runtime source
     * @throws UndefinedOptionException if the name is not recognized
     */
    public static String getActiveRuntimExpr(String name, String value) 
            throws UndefinedOptionException {
        
        String key = name.toLowerCase();
        String expr = activeRuntimeExpr.get(key);
        if (expr == null) {
            throw new UndefinedOptionException(name);
        }
        return expr.replace("_VALUE_", value);
    }

    /**
     * Get the info for a given option.
     * @param optionName option name
     * @return option info
     * @throws UndefinedOptionException if the name is not recognized
     */
    private static OptionInfo getInfo(String optionName) throws UndefinedOptionException {
        for (OptionInfo info : options) {
            if (info.getName().equalsIgnoreCase(optionName)) {
                return info;
            }
        }
        
        throw new UndefinedOptionException(optionName);
    }

}
