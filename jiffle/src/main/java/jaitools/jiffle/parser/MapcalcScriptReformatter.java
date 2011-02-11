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

/**
 * Reformats r.mapcalc scripts to Jiffle format.
 * Newline delimited statements are translated to semicolon delimiters.
 * Continuation line endings ('\' + newline) are replaced by newline plus indent.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class MapcalcScriptReformatter {
    
    private static final String NewlinePlusIndent = "\n    ";

    /**
     * Reformat the input script.
     * 
     * @param script a script assumed to be in mapcalc format
     * 
     * @return reformatted script
     */
    public static String reformat(String script) {
        String s = script + "\n";
        
        // Replace all continuation line endings with a temp marker
        s = s.replaceAll("\\\\\\s*\n+", "__NL__");
        
        // Replace remaining newlines with semicolon plus newline
        s = s.replaceAll("\n+", ";\n");
             
        // Replace temp markers with newlines plus indent
        s = s.replaceAll("__NL__", NewlinePlusIndent);
        
        // Replace any contiguous semicolons with a single semicolon
        s = s.replaceAll("(;\\s*;)+", ";");
        
        return s;
    }
    
}
