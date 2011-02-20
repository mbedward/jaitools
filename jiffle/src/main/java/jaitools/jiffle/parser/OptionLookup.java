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
    private static final Map<String, String> inactiveRuntimeExpr;
    private static final List<String> names;
    
    static {
        options = CollectionFactory.list();
        names = CollectionFactory.list();
        activeRuntimeExpr = CollectionFactory.map();
        inactiveRuntimeExpr = CollectionFactory.map();
        
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
        
        inactiveRuntimeExpr.put(name, "_outsideValueSet = false;");
    }
    
    public static boolean isDefined(String optionName) {
        try {
            getInfo(optionName);
            return true;
            
        } catch (UndefinedOptionException ex) {
            return false;
        }
    }
    
    public static boolean isValidValue(String optionName, String value) 
            throws UndefinedOptionException {
        
        return getInfo(optionName).isValidValue(value);
    }
    
    public static Iterable<String> getNames() {
        return Collections.unmodifiableList(names);
    }
    
    private static OptionInfo getInfo(String optionName) throws UndefinedOptionException {
        for (OptionInfo info : options) {
            if (info.getName().equalsIgnoreCase(optionName)) {
                return info;
            }
        }
        
        throw new UndefinedOptionException(optionName);
    }

    public static String getActiveRuntimExpr(String name, String value) {
        String key = name.toLowerCase();
        String expr = activeRuntimeExpr.get(key);
        if (expr == null) {
            throw new IllegalArgumentException("Option name not recognized: " + name);
        }
        return expr.replace("_VALUE_", value);
    }

    public static String getDefaultRuntimeExpr(String name) {
        String key = name.toLowerCase();
        String expr = inactiveRuntimeExpr.get(key);
        if (expr == null) {
            throw new IllegalArgumentException("Option name not recognized: " + name);
        }
        return expr;
    }

}
