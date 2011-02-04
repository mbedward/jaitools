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

import org.junit.Test;

/**
 * Tests basic parsing of scripts with and without an init block.
 * 
 * @author michael
 */
public class InitBlockParsingTest extends ParserTestBase {
    
    @Test
    public void noBlock() throws Exception {
        System.out.println("   script without init block");
        String script = "dest = 42;";
        getAST(script);
    }

    @Test
    public void emptyBlock() throws Exception {
        System.out.println("   script with empty init block");
        String script = 
                  "init { } \n"
                + "dest = 42;";
        
        getAST(script);
    }

    @Test
    public void simpleBlock() throws Exception {
        System.out.println("   script with simple init block");
        String script = 
                  "init { foo = 42; } \n"
                + "dest = 42;";
        
        getAST(script);
    }
    
    @Test(expected=JiffleParserException.class)
    public void blockInWrongPlace() throws Exception {
        System.out.println("   block in wrong part of script");
        String script = 
                  "dest = 42;"
                + "init { foo = 42; } \n";
        
        getAST(script);
    }
    
}
