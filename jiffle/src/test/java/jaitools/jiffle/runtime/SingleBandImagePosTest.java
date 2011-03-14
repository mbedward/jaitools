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
 * Unit tests for the source image pixel specifiers.
 * <p>
 * Source image position can be specified as:
 * <pre>
 *     imageName[ b ][ xref, yref ]
 *
 * where:
 *     b is an expression for band number;
 *
 *     xref and yref are either expressions for absolute X and Y
 *     ordinates (if prefixed by '$' symbol) or offsets relative
 *     to current evaluation pixel (no prefix).
 * </pre>
 * The unit tests here work with a single band image
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SingleBandImagePosTest extends StatementsTestBase {
    
    @Test
    public void relativeReferences() throws Exception {
        System.out.println("   relative pixel position");
        
        String src = "dest = con(x() > 0, src[-1, 0], NULL);";
        
        testScript(src, new Evaluator() {
            double lastVal;
            int x;

            public double eval(double val) {
                double outVal;
                if (x > 0) {
                    outVal = lastVal;
                } else {
                    outVal = Double.NaN;
                }
                
                x = (x + 1) % WIDTH;
                lastVal = val;
                return outVal;
            }
        });
    }
    
    @Test
    public void absoluteReferences() throws Exception {
        System.out.println("   absolute pixel position");
        
        String src = "dest = con(x() > 5 && y() > 5, src[$5, $5], NULL);";
        
        testScript(src, new Evaluator() {
            double val55;
            int x;
            int y;

            public double eval(double val) {
                if (x == 5 && y == 5) {
                    val55 = val;
                }
                
                double outVal = Double.NaN;
                if (x > 5 && y > 5) {
                    outVal = val55;
                } 
                
                x++ ;
                if (x == WIDTH) {
                    x = 0;
                    y++ ;
                }
                
                return outVal;
            }
        });
    }
    
}
