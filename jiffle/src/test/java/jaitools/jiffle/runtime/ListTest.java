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
 * Tests for parsing list expressions.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ListTest extends StatementsTestBase {

    @Test
    public void createEmptyList() throws Exception {
        String script = "foo = []; dest = 42;" ;
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 42;
            }
        };
        testScript(script, e);
    }

    @Test
    public void createInitList() throws Exception {
        String script = "foo = [1, 2, 3]; dest = 42;" ;
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 42;
            }
        };
        testScript(script, e);
    }
    
    @Test
    public void passListAsFunctionArg() throws Exception {
        String script = "foo = [1, 3, 2]; dest = max(foo);" ;
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 3;
            }
        };
        testScript(script, e);
    }

}
