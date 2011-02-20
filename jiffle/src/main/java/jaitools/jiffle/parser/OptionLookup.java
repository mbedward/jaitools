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
import java.util.List;

/**
 * A lookup service used by the Jiffle compiler when parsing script options.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class OptionLookup {

    private static final List<OptionInfo> options;
    
    static {
        options = CollectionFactory.list();
        
        OptionInfo info;
        
        info = new OptionInfo("outside", 
                new String[] { OptionInfo.ANY_NUMBER, OptionInfo.NULL_KEYWORD } );
        options.add(info);
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
    
    private static OptionInfo getInfo(String optionName) throws UndefinedOptionException {
        for (OptionInfo info : options) {
            if (info.getName().equalsIgnoreCase(optionName)) {
                return info;
            }
        }
        
        throw new UndefinedOptionException(optionName);
    }
    
}
