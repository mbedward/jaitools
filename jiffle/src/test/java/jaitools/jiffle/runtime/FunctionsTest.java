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

import javax.media.jai.TiledImage;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import jaitools.numeric.DoubleComparison;
import static jaitools.numeric.DoubleComparison.*;

import static org.junit.Assert.*;
import org.junit.Test;


/**
 * Unit tests for general functions
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class FunctionsTest extends StatementsTestBase {
    
    @Test(expected=JiffleException.class)
    public void undefinedFunctionName() throws Exception {
        System.out.println("   undefined function name");
        String script = "dest = foo(src);" ;
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                throw new UnsupportedOperationException("Should not be called");
            }
        };
        
        testScript(script, e);
    }
    
    @Test(expected=JiffleException.class)
    public void wrongNumArgs() throws Exception {
        System.out.println("   wrong number of args");
        String script = "dest = sqrt(src, 2, 3);" ;
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                throw new UnsupportedOperationException("Should not be called");
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void abs() throws Exception {
        String script = "dest = abs(src - 50);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return Math.abs(val - 50);
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void acos() throws Exception {
        String script = "dest = acos(x() / width());" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                double z = Math.acos((double)x / WIDTH);
                x = (x + 1) % WIDTH;
                return z;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void asin() throws Exception {
        String script = "dest = asin(x() / width());" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                double z = Math.asin((double)x / WIDTH);
                x = (x + 1) % WIDTH;
                return z;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void atan() throws Exception {
        String script = "dest = atan(x() / width());" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                double z = Math.atan((double)x / WIDTH);
                x = (x + 1) % WIDTH;
                return z;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void degToRad() throws Exception {
        String script = "dest = degToRad(src);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return Math.PI * val / 180;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void exp() throws Exception {
        String script = "dest = exp(src);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return Math.exp(val);
            }
        };
    }
    
    @Test
    public void floor() throws Exception {
        String script = "dest = floor(src / 10);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return Math.floor(val / 10);
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void isinf() throws Exception {
        String script = "dest = isinf(1 / x());" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                Double z = 1.0 / x;
                x = (x + 1) % WIDTH;
                return z.isInfinite() ? 1.0 : 0.0;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void isnan() throws Exception {
        String script = "dest = isnan(y() / x());" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;
            public double eval(double val) {
                Double z = (double)y / x;
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return z.isNaN() ? 1.0 : 0.0;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void isnull() throws Exception {
        String script = "dest = isnull(y() / x());" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;
            public double eval(double val) {
                Double z = (double)y / x;
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return z.isNaN() ? 1.0 : 0.0;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void log1Arg() throws Exception {
        String script = "dest = log(src + 1);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return Math.log(val + 1);
            }
        };
        
        testScript(script, e);
    }

    @Test
    public void log2Arg() throws Exception {
        String script = "dest = log(src + 1, 10);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return Math.log10(val + 1);
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void radToDeg() throws Exception {
        String script = "dest = radToDeg(src / 10);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return (val / 10) * 180 / Math.PI;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void rand() throws Exception {
        String script = "dest = src + rand(src);" ;
        System.out.println("   " + script);
        
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        imageParams.put("src", Jiffle.ImageRole.SOURCE);
        Jiffle jiffle = new Jiffle(script, imageParams);
        JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();
        
        TiledImage src = createRowValueImage();
        TiledImage dest = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        
        runtime.setSourceImage("src", src);
        runtime.setDestinationImage("dest", dest);
        runtime.evaluateAll(null);

        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                double val = src.getSample(x, y, 0);
                double z = dest.getSample(x, y, 0);
                assertTrue(dcomp(z, val) >= 0);
                assertTrue(dcomp(z, 2*val) <= 0);
            }
        }
    }
    
    @Test
    public void randInt() throws Exception {
        String script = "dest = src + randInt(src + 1);" ;
        System.out.println("   " + script);
        
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        imageParams.put("src", Jiffle.ImageRole.SOURCE);
        Jiffle jiffle = new Jiffle(script, imageParams);
        JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();
        
        TiledImage src = createRowValueImage();
        TiledImage dest = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        
        runtime.setSourceImage("src", src);
        runtime.setDestinationImage("dest", dest);
        runtime.evaluateAll(null);

        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                double val = src.getSample(x, y, 0);
                double z = dest.getSample(x, y, 0);
                assertEquals(Math.round(z), z, DoubleComparison.TOL);
                assertTrue(dcomp(z, val) >= 0);
                assertTrue(dcomp(z, 2*val + 1) <= 0);
            }
        }
    }
    
    @Test
    public void round1Arg() throws Exception {
        String script = "dest = round(src / (width() - 1));" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return Math.round(val / (WIDTH - 1));
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void round2Arg() throws Exception {
        String script = "dest = round(src / (width() - 1), 2);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                double z = val / (WIDTH - 1);
                return Math.round(z / 2) * 2;
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void sin() throws Exception {
        String script = "dest = sin(src);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return Math.sin(val);
            }
        };
        
        testScript(script, e);
    }

    @Test
    public void sqrt() throws Exception {
        String script = "dest = sqrt(src);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return Math.sqrt(val);
            }
        };
        
        testScript(script, e);
    }
    
    @Test
    public void tan() throws Exception {
        String script = "dest = tan(src);" ;
        System.out.println("   " + script);
        
        Evaluator e = new Evaluator() {

            public double eval(double val) {
                return Math.tan(val);
            }
        };
        
        testScript(script, e);
    }

}
