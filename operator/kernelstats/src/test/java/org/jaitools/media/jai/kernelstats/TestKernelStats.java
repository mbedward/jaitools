/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   
package org.jaitools.media.jai.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;

import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

import org.jaitools.numeric.Statistic;
import org.jaitools.numeric.SampleStats;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit test for the KernelStats operator
 *
 * @author Michael Bedward
 */
public class TestKernelStats {
    
    private static final double TOL = 1.0e-4;

    /**
     * In this test we provide the operator with a TYPE_INT test image
     * and request the statistics MIN, MAX, RANGE. The returned image
     * should be TYPE_INT.
     */
    @Test
    public void testIntReturnDataType() {
        System.out.println("   test int return type");
        int value = 1;
        int width = 10, height = 10;

        ParameterBlockJAI pb = new ParameterBlockJAI("kernelstats");
        pb.setSource("source0", getConstIntImage(value, width, height));

        Statistic[] stats = {
            Statistic.MAX,
            Statistic.MIN,
            Statistic.RANGE
        };
        pb.setParameter("stats", stats);

        KernelJAI kernel = new KernelJAI(3, 3, new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
        pb.setParameter("kernel", kernel);

        RenderedImage img = JAI.create("kernelstats", pb);
        assertTrue(img.getSampleModel().getDataType() == DataBuffer.TYPE_INT);
    }

    /**
     * In this test we provide the operator with a TYPE_INT test image
     * and request the statistics MIN, MAX, RANGE and MEAN. The returned image
     * should be TYPE_DOUBLE.
     */
    @Test
    public void testDoubleReturnDataType() {
        System.out.println("   test double return type");
        int value = 1;
        int width = 10, height = 10;

        ParameterBlockJAI pb = new ParameterBlockJAI("kernelstats");
        pb.setSource("source0", getConstIntImage(value, width, height));

        Statistic[] stats = {
            Statistic.MAX,
            Statistic.MIN,
            Statistic.RANGE,
            Statistic.MEAN
        };

        pb.setParameter("stats", stats);

        KernelJAI kernel = new KernelJAI(3, 3, new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
        pb.setParameter("kernel", kernel);

        RenderedImage img = JAI.create("kernelstats", pb);
        assertTrue(img.getSampleModel().getDataType() == DataBuffer.TYPE_DOUBLE);
    }

    /**
     * Test all statistics with a constant image
     */
    @Test
    public void testStatisticsWithConstImage() {
        for (Statistic stat : Statistic.values()) {
            if (stat != Statistic.APPROX_MEDIAN) {
                System.out.println("   test " + stat.toString() + " with const image");
                testWithConstImage(stat);
            }
        }
    }

    /**
     * Test all statistics with a random, TYPE_FLOAT image
     */
    @Test
    public void testStatisticsWithFloatImage() {
        RandomImageFunction fn = new RandomImageFunction(-5.0f, 5.0f);
        ParameterBlockJAI pb = new ParameterBlockJAI("imagefunction");
        pb.setParameter("width", 3);
        pb.setParameter("height", 3);
        pb.setParameter("function", fn);
        RenderedOp op = JAI.create("imagefunction", pb);

        RenderedImage testImg = op.getAsBufferedImage();

        for (Statistic stat : Statistic.values()) {
            if (stat != Statistic.APPROX_MEDIAN) {
                System.out.println("   test " + stat.toString() + " with float image");
                testWithFloatImage(stat, testImg);
            }
        }

    }

    /**
     * Helper function for testStatisticsWithConstImage method
     */
    private void testWithConstImage(Statistic stat) {
        int value = 1;
        int width = 10, height = 10;

        ParameterBlockJAI pb = new ParameterBlockJAI("kernelstats");
        pb.setSource("source0", getConstIntImage(value, width, height));
        pb.setParameter("stats", new Statistic[]{stat});

        KernelJAI kernel = new KernelJAI(3, 3, new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
        pb.setParameter("kernel", kernel);

        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER,
                BorderExtender.createInstance(BorderExtender.BORDER_REFLECT));

        RenderedImage img = JAI.create("kernelstats", pb, hints);

        int expValue = 0;
        switch (stat) {
            case MAX:
            case MEAN:
            case MEDIAN:
            case MIN:
                expValue = value;
                break;

            case RANGE:
            case SDEV:
            case VARIANCE:
                expValue = 0;
                break;

            case SUM:
                expValue = 9;
                break;
        }

        RectIter iter = RectIterFactory.create(img, null);
        do {
            do {
                assertEquals(expValue, iter.getSampleDouble(), TOL);
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());
    }

    /**
     * Helper function for testStatisticsWithFloatImage method
     */
    private void testWithFloatImage(Statistic stat, RenderedImage testImg) {
        /*
         * Note: using a larger comparison tolerance here to take into
         * account that we are working with a TYPE_FLOAT test image
         */
        final double tol = 1.0e-6d;

        ParameterBlockJAI pb = new ParameterBlockJAI("kernelstats");
        pb.setSource("source0", testImg);
        pb.setParameter("stats", new Statistic[]{stat});

        KernelJAI kernel = new KernelJAI(3, 3, new float[]{1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f, 1f});
        pb.setParameter("kernel", kernel);

        RenderingHints hints = new RenderingHints(JAI.KEY_BORDER_EXTENDER,
                BorderExtender.createInstance(BorderExtender.BORDER_ZERO));

        RenderedImage img = JAI.create("kernelstats", pb, hints);

        RectIter iter = RectIterFactory.create(img, null);
        RandomIter randIter = RandomIterFactory.create(testImg, null);
        Double[] nbrHoodValues = new Double[9];

        int x = img.getMinX();
        int y = img.getMinY();
        int maxx = img.getWidth() + x - 1;
        int maxy = img.getHeight() + y - 1;
        do {
            do {
                if (x > 0 && x < maxx && y > 0 && y < maxy) {
                    double result = iter.getSampleDouble();
                    int k = 0;
                    for (int ky = -1; ky <= 1; ky++) {
                        int ynbr = ky + y;
                        for (int kx = -1; kx <= 1; kx++, k++) {
                            int xnbr = kx + x;
                            if (ynbr < 0 || ynbr > maxy || xnbr < 0 || xnbr > maxx) {
                                nbrHoodValues[k] = 0d;
                            } else {
                                double val = randIter.getSampleDouble(xnbr, ynbr, 0);
                                nbrHoodValues[k] = val;
                            }
                        }
                    }

                    switch (stat) {
                        case MAX:
                            assertEquals(SampleStats.max(nbrHoodValues, true), result, TOL);
                            break;

                        case MEAN:
                            assertEquals(SampleStats.mean(nbrHoodValues, true), result, TOL);
                            break;

                        case MEDIAN:
                            assertEquals(SampleStats.median(nbrHoodValues, true), result, TOL);
                            break;

                        case MIN:
                            assertEquals(SampleStats.min(nbrHoodValues, true), result, TOL);
                            break;

                        case RANGE:
                            assertEquals(SampleStats.range(nbrHoodValues, true), result, TOL);
                            break;

                        case SDEV:
                            assertEquals(SampleStats.sdev(nbrHoodValues, true), result, TOL);
                            break;

                        case VARIANCE:
                            assertEquals(SampleStats.variance(nbrHoodValues, true), result, TOL);
                            break;
                    }
                }

                x++;

            } while (!iter.nextPixelDone());

            iter.startPixels();
            x = img.getMinX();
            y++;

        } while (!iter.nextLineDone());
    }

    /**
     * Helper function to create a constant image with the given value, width and height
     */
    private RenderedImage getConstIntImage(int value, int width, int height) {
        ParameterBlockJAI pb = new ParameterBlockJAI("constant");
        pb.setParameter("width", (float) width);
        pb.setParameter("height", (float) height);
        pb.setParameter("bandvalues", new Integer[]{value});
        return JAI.create("constant", pb);
    }
}

