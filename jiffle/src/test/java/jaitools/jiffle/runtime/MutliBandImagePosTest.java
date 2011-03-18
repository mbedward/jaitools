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

import java.util.Arrays;
import java.util.Map;

import javax.media.jai.TiledImage;

import static org.junit.Assert.*;
import org.junit.Ignore;
import org.junit.Test;

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;

/**
 * Unit tests for the source image pixel specifiers with multi-band images
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
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class MutliBandImagePosTest {
    
    private static final int WIDTH = 10;
    private static final int NUM_PIXELS = WIDTH * WIDTH;
    private static final double TOL = 1.0e-8;

    interface Evaluator {
        double eval(double[] values);
    }

    private Map<String, Jiffle.ImageRole> imageParams;

    @Test
    public void noBandSpecifier() throws Exception {
        System.out.println("   no band specifier");

        String script = "dest = src;" ;

        Evaluator e = new Evaluator() {
            public double eval(double[] values) {
                return values[0];
            }
        };

        testScript(script, 3, e);
    }

    @Test
    public void constantBandSpecifier() throws Exception {
        System.out.println("   constant band specifier");

        String script = "dest = src[1];" ;

        Evaluator e = new Evaluator() {
            public double eval(double[] values) {
                return values[1];
            }
        };

        testScript(script, 3, e);
    }

    @Test
    public void variableBandSpecifier() throws Exception {
        System.out.println("   variable band specifier");

        String script = "init { i = 0; } dest = src[i]; i = (i + 1)%3;" ;

        Evaluator e = new Evaluator() {
            int k = 0;

            public double eval(double[] values) {
                int kk = k;
                k = (k + 1) % 3;
                return values[kk];
            }
        };

        testScript(script, 3, e);
    }

    @Test
    public void multipleBands() throws Exception {
        System.out.println("   script specifying multiple image bands");

        String script = "dest = src[0] + src[1] + src[2];" ;

        Evaluator e = new Evaluator() {

            public double eval(double[] values) {
                double sum = 0;
                for (int i = 0; i < values.length; i++) {
                    sum += values[i];
                }
                return sum;
            }
        };

        testScript(script, 3, e);
    }

    @Test
    public void bandRelativePixel() throws Exception {
        System.out.println("   band plus relative pixel position");

        String script = "dest = con(x() > 0 && y() > 0, src[1][-1,-1], NULL);" ;

        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;
            double[] prevRow = new double[WIDTH];

            public double eval(double[] values) {
                double rtn = (x > 0 && y > 0 ? prevRow[x-1] : Double.NaN);
                prevRow[x] = values[1];
                if (++x == WIDTH) {
                    x = 0;
                    y++ ;
                }
                return rtn;
            }
        };

        testScript(script, 3, e);
    }


    @Test
    public void bandAbsolutePixel() throws Exception {
        System.out.println("   band plus absolute pixel position");

        String script = "dest = con(x() > 0 && y() > 0, src[1][$(x()-1), $(y()-1)], NULL);";

        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;
            double[] prevRow = new double[WIDTH];

            public double eval(double[] values) {
                double rtn = (x > 0 && y > 0 ? prevRow[x-1] : Double.NaN);
                prevRow[x] = values[1];
                if (++x == WIDTH) {
                    x = 0;
                    y++ ;
                }
                return rtn;
            }
        };

        testScript(script, 3, e);
    }

    @Ignore("todo: get the compiler to throw an exception on this coding mistake")
    @Test(expected=JiffleException.class)
    public void emptyBandSpecifier() throws Exception {
        System.out.println("   malformed band specifier");

        String script = "dest = src[];" ;
        testScript(script, 3, null);
    }

    private void testScript(String script, int numSrcBands, Evaluator evaluator) throws Exception {
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        imageParams.put("src", Jiffle.ImageRole.SOURCE);

        Jiffle jiffle = new Jiffle(script, imageParams);
        JiffleDirectRuntime runtime = jiffle.getRuntimeInstance();

        TiledImage srcImg = createSequenceImage(numSrcBands);
        runtime.setSourceImage("src", srcImg);

        TiledImage destImg = ImageUtils.createConstantImage(WIDTH, WIDTH, 0d);
        runtime.setDestinationImage("dest", destImg);

        runtime.evaluateAll(null);

        double[] values = new double[numSrcBands];
        for (int y = 0; y < WIDTH; y++) {
            for (int x = 0; x < WIDTH; x++) {
                for (int b = 0; b < numSrcBands; b++) {
                    values[b] = srcImg.getSampleDouble(x, y, b);
                }
                assertEquals(evaluator.eval(values), destImg.getSampleDouble(x, y, 0), TOL);
            }
        }
    }

    private double calculateValue(int x, int y, int band) {
        return band * NUM_PIXELS + y * WIDTH + x;
    }

    private TiledImage createSequenceImage(int numBands) {
        Double[] initVal = new Double[numBands];
        Arrays.fill(initVal, 0d);

        TiledImage img = ImageUtils.createConstantImage(WIDTH, WIDTH, initVal);
        for (int b = 0; b < numBands; b++) {
            for (int y = 0; y < WIDTH; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    img.setSample(x, y, 0, calculateValue(x, y, b));
                }
            }
        }
        return img;
    }
    
}
