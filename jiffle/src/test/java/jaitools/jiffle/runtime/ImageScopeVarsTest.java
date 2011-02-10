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

import static org.junit.Assert.*;
import org.junit.Test;

/**
 * Unit tests of scripts with image scope variables declared in an init block.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ImageScopeVarsTest extends StatementsTestBase {

    @Test
    public void increment() throws Exception {
        System.out.println("   incrementing value");

        String script = "init { n = 0; } dest = n++;" ;

        Evaluator e = new Evaluator() {
            double n = 0;

            public double eval(double val) {
                return n++ ;
            }
        };

        testScript(script, e);
    }

    @Test
    public void decrement() throws Exception {
        System.out.println("   decrementing value");

        String script = "init { n = 0; } dest = n--;" ;

        Evaluator e = new Evaluator() {
            double n = 0;

            public double eval(double val) {
                return n-- ;
            }
        };

        testScript(script, e);
    }

    @Test
    public void getter() throws Exception {
        System.out.println("   getting values from runtime instance");

        String script = String.format(
                "init { n = 0; } n += if (src < %s); dest = n;",
                NUM_PIXELS / 2);

        Evaluator e = new Evaluator() {
            double n = 0;

            public double eval(double val) {
                if (val < NUM_PIXELS / 2) {
                    n++ ;
                }
                return n;
            }
        };

        testScript(script, e);
        assertEquals(NUM_PIXELS / 2, runtimeInstance.getVar("n"), TOL);
    }

    @Test
    public void proxyFunctionInInitBlock() throws Exception {
        System.out.println("   using image info function in init block");

        String script = "init { n = width(); } dest = n;" ;

        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return WIDTH;
            }
        };

        testScript(script, e);
    }

    @Test
    public void ifWithinInitBlock() throws Exception {
        System.out.println("   using if expressions in init block");

        String script = "init { n = if (width() > 100, 2, 1); } dest = n;" ;
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return WIDTH > 100 ? 2 : 1;
            }
        };

        testScript(script, e);
    }
}
