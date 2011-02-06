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

import jaitools.numeric.DoubleComparison;

/**
 * Unit tests for the evaluation of simple logical statements with a 
 * single source and destination image.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class LogicalStatementsTest extends StatementsTestBase {
    
    @Test
    public void logical1() throws Exception {
        
        String src = String.format("dest = src < %d || src > %d;",
                NUM_PIXELS / 4, 3 * NUM_PIXELS / 4);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val < NUM_PIXELS / 4 || val > 3 * NUM_PIXELS / 4 ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical2() throws Exception {
        
        String src = String.format("dest = src <= %d || src >= %d;",
                NUM_PIXELS / 4, 3 * NUM_PIXELS / 4);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val <= NUM_PIXELS / 4 || val >= 3 * NUM_PIXELS / 4 ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical3() throws Exception {
        
        String src = String.format("dest = src > %d && src < %d;",
                NUM_PIXELS / 4, 3 * NUM_PIXELS / 4);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val > NUM_PIXELS / 4 && val < 3 * NUM_PIXELS / 4 ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical4() throws Exception {
        
        String src = String.format("dest = src >= %d && src <= %d;",
                NUM_PIXELS / 4, 3 * NUM_PIXELS / 4);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val >= NUM_PIXELS / 4 && val <= 3 * NUM_PIXELS / 4 ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical5() throws Exception {
        
        String src = String.format("dest = src == %d;",
                NUM_PIXELS / 2);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return DoubleComparison.dequal(val, NUM_PIXELS / 2) ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical6() throws Exception {
        
        String src = String.format("dest = src != %d;",
                NUM_PIXELS / 2);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return !DoubleComparison.dequal(val, NUM_PIXELS / 2) ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical7() throws Exception {
        
        String src = "dest = !(src % 2);";
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return !DoubleComparison.dequal(val % 2, 1.0) ? 1.0 : 0.0;
                    }
                });
    }
    
    @Test
    public void logical8() throws Exception {
        
        String src = String.format("dest = src <= %d ^| src >= %d;",
                3 * NUM_PIXELS / 4, NUM_PIXELS / 4);
        System.out.println("   " + src);
        
        testScript(src,
                new Evaluator() {

                    public double eval(double val) {
                        return val <= 3 * NUM_PIXELS / 4 ^ val >= NUM_PIXELS / 4 ? 1.0 : 0.0;
                    }
                });
    }
    
}
