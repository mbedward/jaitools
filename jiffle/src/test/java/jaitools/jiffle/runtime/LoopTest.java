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
    
    @Test
    public void untilLoopWithBlock() throws Exception {
        System.out.println("   until loop with block");
        String script = 
                  "n = 0; \n"
                + "i = 0; \n"
                + "until (i > x()) { n += i; i++ ; } \n"
                + "dest = n;" ;
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                int n = 0;
                for (int i = 0; i <= x; i++) n += i;
                x = (x + 1) % WIDTH;
                return n;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void whileLoopWithSimpleStatement() throws Exception {
        System.out.println("   while loop with simple statement");
        String script = 
                  "n = 0; \n"
                + "while (n < x()) n++; \n"
                + "dest = n;" ;
        
        Evaluator e = new Evaluator() {
            int x = 0;
            
            public double eval(double val) {
                int xx = x;
                x = (x + 1) % WIDTH;
                return xx;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void untilLoopWithSimpleStatement() throws Exception {
        System.out.println("   until loop with simple statement");
        String script = 
                  "n = 0; \n"
                + "until (n > x()) n++; \n"
                + "dest = n;" ;
        
        Evaluator e = new Evaluator() {
            int x = 0;
            
            public double eval(double val) {
                int xx = x;
                x = (x + 1) % WIDTH;
                return xx + 1;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void foreachLoopWithSimpleStatement() throws Exception {
        System.out.println("   foreach loop with simple statement");
        String script =
                  "z = 0;"
                + "foreach (i in {x(), y(), 3}) z += i;"
                + "dest = z;" ;
        
        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;

            public double eval(double val) {
                double z = x + y + 3;
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return z;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void foreachLoopWithBlock() throws Exception {
        System.out.println("   foreach loop with block");
        String script =
                  "z = 0;"
                + "foreach (i in {x(), y(), 3}) \n"
                + "{ \n"
                + "    temp = i * 2; \n"
                + "    z += temp; \n"
                + "} \n"
                + "dest = z;" ;
        
        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;

            public double eval(double val) {
                double z = 2*(x + y + 3);
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return z;
            }
        };
        
        testScript(script, e);
    }
}
