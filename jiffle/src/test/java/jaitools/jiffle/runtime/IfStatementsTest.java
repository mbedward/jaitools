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
 * Unit tests for the various {@code if} fnctions and the ternary statement.
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class IfStatementsTest extends StatementsTestBase {

    @Test
    public void if1Arg() throws Exception {
        String src = "dest = if (src > 10);";
        
        testScript(src, new Evaluator() {

            public double eval(double val) {
                return val > 10 ? 1 : 0;
            }
        });
    }

    @Test
    public void if2Arg() throws Exception {
        String src = "dest = if (src > 10, 10);";
        
        testScript(src, new Evaluator() {

            public double eval(double val) {
                return val > 10 ? 10 : 0;
            }
        });
    }

    @Test
    public void if3Arg() throws Exception {
        String src = "dest = if (src > 10, src, 10);";
        
        testScript(src, new Evaluator() {

            public double eval(double val) {
                return val > 10 ? val : 10;
            }
        });
    }
    
    @Test
    public void if4Arg() throws Exception {
        String src = "dest = if (src - 10, src, 10, 0);";
        
        testScript(src, new Evaluator() {

            public double eval(double val) {
                double comp = val - 10;
                if (comp > 0) {
                    return val;
                } else if (comp == 0) {
                    return 10;
                } else {
                    return 0;
                }
            }
        });
    }
    
    @Test
    public void ternary() throws Exception {
        
        String src = String.format("dest = src <= %d ? -1 : 1;",
                NUM_PIXELS / 2);
        
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val <= NUM_PIXELS / 2 ? -1.0 : 1.0;
                    }
                });
    }
    
}
