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

package jaitools.jiffle.runtime;

import org.junit.Test;

/**
 * Unit tests for stats functions
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class StatsFunctionsTest extends StatementsTestBase {

    @Test
    public void max2Arg() throws Exception {
        System.out.println("   max(D, D)");
        
        int z = WIDTH * WIDTH / 2;
        String script = String.format("init { z = %d; } dest = max(src, z);", z);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return Math.max(val, 50);
            }
        };
        
        testScript(script, e);
    }

    @Test
    public void min2Arg() throws Exception {
        System.out.println("   min(D, D)");
        
        int z = WIDTH * WIDTH / 2;
        String script = String.format("init { z = %d; } dest = min(src, z);", z);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return Math.min(val, 50);
            }
        };
        
        testScript(script, e);
    }

    @Test
    public void maxListArg() throws Exception {
        System.out.println("   max(List)");
        
        String script = "options { outside = 0; } \n"
                + "z = [ src[0,-1], src, src[0,1] ]; \n"
                + "dest = max(z);";
        
        Evaluator e = new Evaluator() {
            final int MAXY = WIDTH - 1;
            int x = 0;
            int y = 0;
            public double eval(double val) {
                double z = Math.min(MAXY, y + 1);
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return z;
            }
        };
        
        testScript(script, createRowValueImage(), e);
    }

    @Test
    public void minListArg() throws Exception {
        System.out.println("   min(List)");
        
        String script = "options { outside = 0; } \n"
                + "z = [ src[0,-1], src, src[0,1] ]; \n"
                + "dest = min(z);";
        
        Evaluator e = new Evaluator() {
            final int MAXY = WIDTH - 1;
            int x = 0;
            int y = 0;
            public double eval(double val) {
                double z = y == MAXY ? 0 : Math.max(0, y-1);
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return z;
            }
        };
        
        testScript(script, createRowValueImage(), e);
    }

}
