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

import jaitools.jiffle.JiffleBuilder;
import jaitools.jiffle.JiffleException;

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for the MapcalcScriptReformatter.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id: $
 */
public class MapcalcScriptReformatterTest {

    @Test
    public void newlinesAsStatementDelims() throws Exception {
        System.out.println("   statements delimited by newlines");
        String script = 
                  "foo = 0 \n"
                + "bar = foo + 1 \n"
                + "dest = bar \n";
        
        assertReformat(script, 3);
    }
    
    @Test
    public void optionalSemicolons() throws Exception {
        System.out.println("   some statements with semicolons");
        String script = 
                  "foo = 0 \n"
                + "bar = foo + 1; \n"
                + "dest = bar";
        
        assertReformat(script, 3);
    }
    
    @Test
    public void continuationLines() throws Exception {
        System.out.println("   mapcalc continuation lines");
        String script = 
                  "foo = 0 \n"
                + "bar = foo + 1 + 2 \\ \n"
                + "+ 3 + 4 \\ \n"
                + "+ 5\n"
                + "dest = bar";
        
        assertReformat(script, 5);
    }

    private void assertReformat(String script, int numLines) {
        String result = MapcalcScriptReformatter.reformat(script);
        
        String[] lines = result.split("\n");
        assertEquals(numLines, lines.length);
        
        // Check that the result is a runnable script
        JiffleBuilder jb = new JiffleBuilder();
        
        try {
            jb.script(result).dest("dest", 2, 2).getRuntime();
        } catch (JiffleException ex) {
            fail("Reformatted script could not be compiled: \n" + result);
        }
    }
    
}
