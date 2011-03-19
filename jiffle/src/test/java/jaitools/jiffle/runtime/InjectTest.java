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

import java.awt.image.WritableRenderedImage;
import java.util.Map;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;

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
        System.out.println("   inject value for var with default");
        String script = 
                  "init { n = 0; } \n"
                + "dest = n;" ;

        testScriptWithValue(script, 42.0);
    }
    
    @Test
    public void varWithNoDefault() throws Exception {
        System.out.println("   inject value for var with no default");
        String script = 
                  "init { n; } \n"
                + "dest = n;" ;

        testScriptWithValue(script, 42.0);
    }

    @Test(expected=JiffleRuntimeException.class)
    public void neglectVarWithNoDefault() throws Exception {
        System.out.println("   unset var with no default gives exception");
        String script = 
                  "init { n; } \n"
                + "dest = n;" ;

        testScriptWithValue(script, null);
    }
    
    @Test
    public void injectThenDefault() throws Exception {
        System.out.println("   run with injected value then default value");
        String script = 
                  "init { n = 42; } \n"
                + "dest = n;" ;
        
        JiffleDirectRuntime runtime = getRuntime(script);
        WritableRenderedImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        runtime.setDestinationImage("dest", destImg);

        runtime.setVar("n", -1.0);
        runtime.evaluateAll(null);
        assertImage(null, destImg, new Evaluator() {
            public double eval(double val) {
                return -1.0;
            }
        });
        
        // set var back to default value (42)
        runtime.setVar("n", null);
        runtime.evaluateAll(null);
        assertImage(null, destImg, new Evaluator() {
            public double eval(double val) {
                return 42.0;
            }
        });
        
    }
    
    @Test
    public void repeatedSetting() throws Exception {
        System.out.println("   repeated setting of var");
        String script = 
                  "init { n; } \n"
                + "dest = n;" ;
        
        JiffleDirectRuntime runtime = getRuntime(script);
        for (int i = -5; i <= 5; i++) {
            testInject(runtime, Double.valueOf(i));
        }
    }
    
    
    private void testScriptWithValue(String script, final Double value) throws Exception {
        JiffleDirectRuntime runtime = getRuntime(script);
        testInject(runtime, value);
    }
            

    private JiffleDirectRuntime getRuntime(String script) throws Exception {
        Jiffle jiffle = new Jiffle();
        jiffle.setScript(script);
        
        Map<String, Jiffle.ImageRole> params = CollectionFactory.map();
        params.put("dest", Jiffle.ImageRole.DEST);
        jiffle.setImageParams(params);
        jiffle.compile();
        
        return jiffle.getRuntimeInstance();
    }
    
    private void testInject(JiffleDirectRuntime runtime, final Double value) throws Exception {
        WritableRenderedImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        runtime.setDestinationImage("dest", destImg);
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
