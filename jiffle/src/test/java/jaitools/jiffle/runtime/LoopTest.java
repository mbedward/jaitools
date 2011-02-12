/*
 * Copyright 2009-2011 Michael Bedward
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
 * Unit tests for Jiffle's loop statements.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class LoopTest extends StatementsTestBase {

    @Test
    public void whileLoopWithBlock() throws Exception {
        System.out.println("   while loop with block");
        String script = 
                  "n = 0; \n"
                + "i = 0; \n"
                + "while (i < x()) { n += i; i++ ; } \n"
                + "dest = n;" ;
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                int n = 0;
                for (int i = 0; i < x; i++) n += i;
                x = (x + 1) % WIDTH;
                return n;
            }
        };
        
        testScript(script, e);
    }
}
