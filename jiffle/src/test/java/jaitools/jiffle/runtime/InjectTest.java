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

import java.awt.image.RenderedImage;

import jaitools.jiffle.JiffleBuilder;
import java.awt.image.WritableRenderedImage;

import org.junit.Test;

/**
 * Tests for setting the value of image-scope variables at run-time.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class InjectTest extends StatementsTestBase {
    
    @Test
    public void varWithDefault() throws Exception {
        String script = 
                  "init { n = 0; } \n"
                + "dest = n;" ;

        testInject(script, 42.0);
    }
    
    @Test
    public void varWithNoDefault() throws Exception {
        String script = 
                  "init { n; } \n"
                + "dest = n;" ;

        testInject(script, 42.0);
    }

    @Test(expected=JiffleRuntimeException.class)
    public void neglectVarWithNoDefault() throws Exception {
        String script = 
                  "init { n; } \n"
                + "dest = n;" ;

        testInject(script, null);
    }

    private void testInject(String script, final Double value) throws Exception {
        JiffleBuilder builder = new JiffleBuilder();
        builder.script(script).dest("dest", WIDTH, WIDTH);
        
        RenderedImage destImg = builder.getImage("dest");
        JiffleDirectRuntime runtime = builder.getRuntime();
        
        runtime.setDestinationImage("dest", (WritableRenderedImage)destImg);
        if (value != null) {
            runtime.setVar("n", value);
        }
        runtime.evaluateAll(null);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return value;
            }
        };
        
        assertImage(null, destImg, e);
    }
    
    
}
