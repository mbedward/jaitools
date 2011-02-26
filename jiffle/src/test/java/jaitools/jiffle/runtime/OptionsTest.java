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

import java.awt.image.Raster;
import java.awt.image.RenderedImage;

import javax.media.jai.TiledImage;

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.JiffleBuilder;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for script options.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class OptionsTest extends StatementsTestBase {
    
    private JiffleBuilder builder;
    
    @Before
    public void setup() {
        builder = new JiffleBuilder();
    }

    
    @Test
    public void outsideOptionWithNeighbourhoodRefs() throws Exception {
        System.out.println("   using outside option with neighbourhood refs");
        String script = 
                  "options {outside = 0;} \n"
                + "n = 0;"
                + "foreach(iy in -1:1) { \n"
                + "  foreach(ix in -1:1) { \n"
                + "    n += src[ix, iy]; \n"
                + "  } \n"
            + "} \n"
                + "dest = n;";
        
        Integer[] srcData = {
            0, 0, 0, 0,
            0, 1, 0, 0,
            0, 1, 1, 0,
            0, 0, 0, 0
        };
        
        TiledImage srcImg = ImageUtils.createImageFromArray(srcData, 4, 4);
        
        builder.script(script).source("src", srcImg).dest("dest", 4, 4).run();
        
        RenderedImage result = builder.getImage("dest");
        
        int[] expectedData = {
            1, 1, 1, 0,
            2, 3, 3, 1,
            2, 3, 3, 1,
            1, 2, 2, 1
        };
        
        int k = 0;
        Raster raster = result.getData();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                assertEquals(expectedData[k++], raster.getSample(x, y, 0));
            }
        }
    }

    @Test(expected=JiffleRuntimeException.class)
    public void readOutsideBoundsWithOptionNotSet() throws Exception {
        System.out.println("   reading outside image bounds with option not set");
        
        String script = "dest = src[$-1, 0];";
        TiledImage srcImg = ImageUtils.createConstantImage(4, 4, 0);
        
        builder.script(script).source("src", srcImg).dest("dest", 4, 4).run();
    }
    
    @Test
    public void outsideEqualsNull() throws Exception {
        System.out.println("   outside = null");
        assertOutsideEqualsValue("null", Double.NaN);
    }

    @Test
    public void outsideEqualsNaN() throws Exception {
        System.out.println("   outside = NaN");
        assertOutsideEqualsValue("NaN", Double.NaN);
    }

    @Test
    public void outsideEqualsNamedConstant() throws Exception {
        System.out.println("   outside = M_PI");
        assertOutsideEqualsValue("M_PI", Math.PI);
    }
    
    private void assertOutsideEqualsValue(String stringValue, final Double expectedValue) 
            throws Exception {
        String script = "options { outside = " + stringValue + "; } dest = src[-1, 0];" ;
        TiledImage srcImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 1);
        
        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                double z = x == 0 ? expectedValue : 1;
                x = (x + 1) % WIDTH;
                return z;
            }
        };
        
        testScript(script, srcImg, e);
    }
}
