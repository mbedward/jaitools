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

import java.util.Arrays;
import java.util.List;

/**
 * Holds information about a supported Jiffle script option.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
class OptionInfo {
    
    public static final String ANY_STRING = "ANY_STRING";
    public static final String ANY_NUMBER = "ANY_NUMBER";
    public static final String NULL_KEYWORD = "NULL_KEYWORD";

    private final String name;
    private final List<String> validValues;
    
    public OptionInfo(String name, String[] validValues) {
        this.name = name;
        this.validValues = Arrays.asList(validValues);
    }

    public String getName() {
        return name;
    }

    public boolean isValidValue(String value) {
        // Is it the null keyword ?
        if ("null".equalsIgnoreCase(value)) {
            return validValues.contains(NULL_KEYWORD);
        }
        
        // Is it a number ?
        boolean numeric = true;
        try {
            Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            numeric = false;
        }
        
        if (numeric) {
            return validValues.contains(ANY_NUMBER);
        }
        
        // Final test
        return validValues.contains(ANY_STRING);
    }
    
}
