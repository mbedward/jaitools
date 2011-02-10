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

import org.junit.Test;

import jaitools.imageutils.ImageUtils;

/**
 * Unit tests for the evaluation of expressions with Jiffle's image
 * info functions.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ProxyFunctionsTest extends StatementsTestBase {

    @Test
    public void x() throws Exception {
        System.out.println("   x()");

        String script = "dest = x();" ;

        Evaluator e = new Evaluator() {
            int x = 0;
            public double eval(double val) {
                double xx = x;
                x = (x + 1) % WIDTH;
                return xx;
            }
        };

        testScript(script, e);
    }

    @Test
    public void y() throws Exception {
        System.out.println("   y()");

        String script = "dest = y();" ;

        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;
            public double eval(double val) {
                double yy = y;
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                return yy;
            }
        };

        testScript(script, e);
    }

    @Test
    public void xmin() throws Exception {
        System.out.println("   xmin()");

        String script = "dest = xmin();" ;
        TiledImage srcImg = ImageUtils.createConstantImage(-5, 5, 10, 20, 0);

        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return -5;
            }
        };

        testScript(script, srcImg, e);
    }

    @Test
    public void ymin() throws Exception {
        System.out.println("   ymin()");

        String script = "dest = ymin();" ;
        TiledImage srcImg = ImageUtils.createConstantImage(-5, 5, 10, 20, 0);

        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 5;
            }
        };

        testScript(script, srcImg, e);
    }

    @Test
    public void width() throws Exception {
        System.out.println("   width()");

        String script = "dest = width();" ;

        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return WIDTH;
            }
        };

        testScript(script, e);
    }

    @Test
    public void height() throws Exception {
        System.out.println("   height()");

        String script = "dest = height();" ;

        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return WIDTH;
            }
        };

        testScript(script, e);
    }

}
