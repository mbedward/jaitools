/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle.runtime;

import jaitools.jiffle.Jiffle;
import jaitools.utils.ImageUtils;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import javax.media.jai.TiledImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import org.junit.Test;
import static org.junit.Assert.*;
import static jaitools.numeric.DoubleComparison.*;

/**
 * Unit tests for the JiffleRunner class
 * @author Michael Bedward
 */
public class JiffleRunnerTest {

    public JiffleRunnerTest() {
    }

    @Test
    public void testIsPositionalFunction() {
        System.out.println("   isPositionalFunction");
        String[] names = {"x", "y", "row", "col"};
        for (String name : names) {
            assertTrue(JiffleRunner.isPositionalFunction(name));
        }
        
        assertFalse(JiffleRunner.isPositionalFunction("foo"));
    }

    @Test
    public void testIsInfoFunction() {
        System.out.println("   isInfoFunction");
        String[] names = {"width", "height"};
        for (String name : names) {
            assertTrue(JiffleRunner.isInfoFunction(name));
        }
        
        assertFalse(JiffleRunner.isInfoFunction("foo"));
    }

    /**
     * Test direct running of a simple script that creates a constant
     * image and divides it by 2.0
     * 
     * @throws java.lang.Exception
     */
    @Test
    public void testRun() throws Exception {
        System.out.println("   run");
        
        double inValue = 10d;
        
        TiledImage inImg = ImageUtils.createDoubleImage(10, 10, new double[]{inValue});
        TiledImage outImg = ImageUtils.createDoubleImage(10, 10, 1);
        
        Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
        imgParams.put("out", outImg);
        imgParams.put("in", inImg);
        
        Jiffle jif = new Jiffle("out = in / 2.0;\n", imgParams);
        boolean b = false;
        if (jif.isCompiled()) {
            JiffleRunner runner = new JiffleRunner(jif);
            b = runner.run();
        }
        
        assertTrue(b);

        double expValue = inValue / 2;
        RectIter iter = RectIterFactory.create(outImg, null);
        do {
            do {
                assertTrue(dzero(iter.getSampleDouble() - expValue));
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());
    }

    /**
     * Tests for correct treatment of null (NaN) values in an arithmentic
     * expression.
     * <p>
     * Creates two input images, each of which has random pixel values. The
     * values are generated such that there is approximately equal frequency
     * of the four possible pair-wise combinations of null or non-null.
     * The non-null values in each image are uniformly selected from {-1, 0, 1}.
     *
     * <p>
     * An output image is calculated from the Jiffle expression
     * {@code out = in1 - in2 }
     *
     * <p>
     * The output image should be null wherever one or both input images were
     * null
     */
    @Test
    public void testNullImageValueSubtraction() throws Exception {
        System.out.println("   subtraction with null and non-null image values");

        int width = 100;

        TiledImage inImg1 = ImageUtils.createDoubleImage(width, width);
        TiledImage inImg2 = ImageUtils.createDoubleImage(width, width);

        /*
         * Randomly allocate the four possible combinations of
         * null and non-null values to the input images
         */
        Random rand = new Random();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                switch (rand.nextInt(4)) {
                    case 0:
                        inImg1.setSample(i, j, 0, Double.NaN);
                        inImg2.setSample(i, j, 0, Double.NaN);
                        break;

                    case 1:
                        inImg1.setSample(i, j, 0, Double.NaN);
                        inImg2.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        break;

                    case 2:
                        inImg1.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        inImg2.setSample(i, j, 0, Double.NaN);
                        break;

                    case 3:
                        inImg1.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        inImg2.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        break;
                }
            }
        }
        
        // out image initially filled with zeroes
        TiledImage outImg = ImageUtils.createDoubleImage(width, width);
        
        Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
        imgParams.put("in1", inImg1);
        imgParams.put("in2", inImg2);
        imgParams.put("out", outImg);
        
        Jiffle jiffle = new Jiffle("out = in1 - in2", imgParams);
        boolean b = false;
        if (jiffle.isCompiled()) {
            JiffleRunner runner = new JiffleRunner(jiffle);
            b = runner.run();
        }
        
        assertTrue(b);

        final boolean VERBOSE = false;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                boolean null1 = Double.isNaN(inImg1.getSampleDouble(i, j, 0));
                if (VERBOSE) {
                    if (!null1) {
                        System.out.print("  " + inImg1.getSampleDouble(i, j, 0));
                    } else {
                        System.out.print("  NULL");
                    }
                }

                boolean null2 = Double.isNaN(inImg2.getSampleDouble(i, j, 0));

                if (VERBOSE) {
                    if (!null2) {
                        System.out.print(" - " + inImg2.getSampleDouble(i, j, 0));
                    } else {
                        System.out.print(" - NULL");
                    }
                }

                boolean nullOut = Double.isNaN(outImg.getSampleDouble(i, j, 0));

                if (VERBOSE) {
                    if (!nullOut) {
                        System.out.println(" = " + outImg.getSampleDouble(i, j, 0));
                    } else {
                        System.out.println(" = NULL");
                    }
                }

                assertTrue(String.format("Failed for combination %s - %s",
                                         (null1 ? "NULL" : "NON-NULL"),
                                         (null2 ? "NULL" : "NON-NULL")),
                           nullOut == (null1 || null2));
            }
        }
    }

    /**
     * Tests for correct treatment of null (NaN) values in an arithmentic
     * expression within an if statement.
     * <p>
     * Creates two input images, each of which has random pixel values. The
     * values are generated such that there is approximately equal frequency
     * of the four possible pair-wise combinations of null or non-null.
     * The non-null values in each image are uniformly selected from {-1, 0, 1}.
     * 
     * <p>
     * An output image is calculated from the Jiffle expression
     * {@code out = if(in1 - in2, 1, 0, -1) }
     *
     * <p>
     * The output image should have null wherever one or both input images were
     * null; otherwise the output image should be equal to:
     * {@code Double.compare(in1, in2)}
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testNullSubtractionWithinIf() throws Exception {
        System.out.println("   subtraction with null and non-null values within if statements");

        int width = 100;

        TiledImage inImg1 = ImageUtils.createDoubleImage(width, width);
        TiledImage inImg2 = ImageUtils.createDoubleImage(width, width);

        /*
         * Randomly allocate the four possible combinations of
         * null and non-null values to the input images
         */
        Random rand = new Random();
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                switch (rand.nextInt(4)) {
                    case 0:
                        inImg1.setSample(i, j, 0, Double.NaN);
                        inImg2.setSample(i, j, 0, Double.NaN);
                        break;

                    case 1:
                        inImg1.setSample(i, j, 0, Double.NaN);
                        inImg2.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        break;

                    case 2:
                        inImg1.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        inImg2.setSample(i, j, 0, Double.NaN);
                        break;

                    case 3:
                        inImg1.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        inImg2.setSample(i, j, 0, (double)(rand.nextInt(3) - 1));
                        break;
                }
            }
        }

        // out image initially filled with zeroes
        TiledImage outImg = ImageUtils.createDoubleImage(width, width);

        Map<String, TiledImage> imgParams = new HashMap<String, TiledImage>();
        imgParams.put("in1", inImg1);
        imgParams.put("in2", inImg2);
        imgParams.put("out", outImg);

        Jiffle jiffle = new Jiffle("out = if (in1 - in2, 1, 0, -1)", imgParams);
        boolean b = false;
        if (jiffle.isCompiled()) {
            JiffleRunner runner = new JiffleRunner(jiffle);
            b = runner.run();
        }

        assertTrue(b);

        final boolean VERBOSE = false;

        for (int i = 0; i < width; i++) {
            for (int j = 0; j < width; j++) {
                double in1 = inImg1.getSampleDouble(i, j, 0);
                boolean null1 = Double.isNaN(in1);
                if (VERBOSE) {
                    if (!null1) {
                        System.out.print("  " + in1);
                    } else {
                        System.out.print("  NULL");
                    }
                }

                double in2 = inImg2.getSampleDouble(i, j, 0);
                boolean null2 = Double.isNaN(in2);

                if (VERBOSE) {
                    if (!null2) {
                        System.out.print(" - " + in2);
                    } else {
                        System.out.print(" - NULL");
                    }
                }

                double out = outImg.getSampleDouble(i, j, 0);
                boolean nullOut = Double.isNaN(out);

                if (VERBOSE) {
                    if (!nullOut) {
                        System.out.println(" = " + out);
                    } else {
                        System.out.println(" = NULL");
                    }
                }

                assertTrue(String.format("Failed for combination %s - %s",
                                         (null1 ? "NULL" : "NON-NULL"),
                                         (null2 ? "NULL" : "NON-NULL")),
                           nullOut == (null1 || null2));

                if (!nullOut) {
                    assertTrue(String.format("got %.1f - %.1f = %.1f", in1, in2, out),
                            out == dcomp(in1 - in2, 0d));
                }
            }
        }
    }
}