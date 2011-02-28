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
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests for FunctionLookup.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class FunctionLookupTest {

    @Test
    public void proxyFunctions() throws Exception {
        System.out.println("   proxy functions");
        String[] names = { "x", "y", "width", "height", "size" };
        
        for (String name : names) {
            FunctionInfo info = FunctionLookup.getInfo(name, null);
            assertNotNull(info);
            assertTrue(info.isProxy());
        }
    }
    
    @Test
    public void oneAndTwoArgFunctions() throws Exception {
        System.out.println("   alternate one or two arg functions");
        String[] names = { "round", "log" };
        List<String> oneArg = Arrays.asList("D");
        List<String> twoArg = Arrays.asList("D", "D");
        
        for (String name : names) {
            assertNotNull(FunctionLookup.getInfo(name, oneArg));
            assertNotNull(FunctionLookup.getInfo(name, twoArg));
        }
    }
    
    @Test
    public void minAndMax() throws Exception {
        System.out.println("   alternate min and max functions");
        String[] names = { "min", "max" };
        List<String> oneArg = Arrays.asList("List");
        List<String> twoArg = Arrays.asList("D", "D");
        
        for (String name : names) {
            FunctionInfo info = FunctionLookup.getInfo(name, oneArg);
            assertNotNull(info);
            assertTrue(info.getRuntimeExpr().contains("_FN."));
            
            info = FunctionLookup.getInfo(name, twoArg);
            assertNotNull(info);
            assertTrue(info.getRuntimeExpr().contains("Math."));
        }
    }
}
