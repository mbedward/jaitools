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

import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.JiffleBuilder;
import java.awt.image.RenderedImage;
import javax.media.jai.TiledImage;
import org.junit.Test;

/**
 * Unit tests for {@code con} functions.
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ConStatementsTest extends StatementsTestBase {

    @Test
    public void con1Arg() throws Exception {
        String script = "dest = con(src > 10);";
        System.out.println("   " + script);
        
        testScript(script, new Evaluator() {

            public double eval(double val) {
                return val > 10 ? 1 : 0;
            }
        });
    }

    @Test
    public void con2Arg() throws Exception {
        String script = "dest = con(src > 10, 10);";
        System.out.println("   " + script);
        
        testScript(script, new Evaluator() {

            public double eval(double val) {
                return val > 10 ? 10 : 0;
            }
        });
    }

    @Test
    public void con3Arg() throws Exception {
        String script = "dest = con(src > 10, src, 10);";
        System.out.println("   " + script);
        
        testScript(script, new Evaluator() {

            public double eval(double val) {
                return val > 10 ? val : 10;
            }
        });
    }
    
    @Test
    public void con4Arg() throws Exception {
        String script = "dest = con(src - 10, src, 10, 0);";
        System.out.println("   " + script);
        
        testScript(script, new Evaluator() {

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
    public void nestedCon() throws Exception {
        String script = "dest = con(src1, con(src1 > src2, 1, null), null);" ;
        
        System.out.println("   " + script);
        
        final double threshold = WIDTH * WIDTH / 2;
        TiledImage src1 = createSequenceImage();
        TiledImage src2 = ImageUtils.createConstantImage(WIDTH, WIDTH, threshold);
        
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return val > threshold ? 1 : Double.NaN;
            }
        };
                
        JiffleBuilder builder = new JiffleBuilder();
        builder.script(script).source("src1", src1).source("src2", src2);
        builder.dest("dest", WIDTH, WIDTH);
        RenderedImage dest = builder.run().getImage("dest");
        
        assertImage(src1, dest, e);
    }
    
}
