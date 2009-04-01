/*
 * Copyright 2009 Michael Bedward
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

package jaitools.media.jai.kernelstats;

import jaitools.utils.SummaryStats;
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
import org.junit.Test;
import static org.junit.Assert.*;
import static jaitools.utils.DoubleComparison.*;

/**
 * Unit test for the KernelStats operator
 *
 * @author Michael Bedward
 */
public class TestKernelStats {

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

        KernelStatistic[] stats = {
            KernelStatistic.MAX,
            KernelStatistic.MIN,
            KernelStatistic.RANGE
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

        KernelStatistic[] stats = {
            KernelStatistic.MAX,
            KernelStatistic.MIN,
            KernelStatistic.RANGE,
            KernelStatistic.MEAN
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
        for (KernelStatistic stat : KernelStatistic.values()) {
            System.out.println("   test " + stat.toString() + " with const image");
            testWithConstImage(stat);
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

        for (KernelStatistic stat : KernelStatistic.values()) {
            System.out.println("   test " + stat.toString() + " with float image");
            testWithFloatImage(stat, testImg);
        }

    }

    /**
     * Helper function for testStatisticsWithConstImage method
     */
    private void testWithConstImage(KernelStatistic stat) {
        int value = 1;
        int width = 10, height = 10;

        ParameterBlockJAI pb = new ParameterBlockJAI("kernelstats");
        pb.setSource("source0", getConstIntImage(value, width, height));
        pb.setParameter("stats", new KernelStatistic[]{stat});

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
        }

        RectIter iter = RectIterFactory.create(img, null);
        do {
            do {
                assertTrue(dcomp(iter.getSampleDouble(), expValue) == 0);
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());
    }


    /**
     * Helper function for testStatisticsWithFloatImage method
     */
    private void testWithFloatImage(KernelStatistic stat, RenderedImage testImg) {
        /*
         * Note: using a larger comparison tolerance here to take into
         * account that we are working with a TYPE_FLOAT test image
         */
        final double tol = 1.0e-6d;

        ParameterBlockJAI pb = new ParameterBlockJAI("kernelstats");
        pb.setSource("source0", testImg);
        pb.setParameter("stats", new KernelStatistic[]{stat});

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
                            assertTrue(dcomp(SummaryStats.max(nbrHoodValues, true), result, tol) == 0);
                            break;

                        case MEAN:
                            assertTrue(dcomp(SummaryStats.mean(nbrHoodValues, true), result, tol) == 0);
                            break;

                        case MEDIAN:
                            assertTrue(dcomp(SummaryStats.median(nbrHoodValues, true), result, tol) == 0);
                            break;

                        case MIN:
                            assertTrue(dcomp(SummaryStats.min(nbrHoodValues, true), result, tol) == 0);
                            break;

                        case RANGE:
                            assertTrue(dcomp(SummaryStats.range(nbrHoodValues, true), result, tol) == 0);
                            break;

                        case SDEV:
                            assertTrue(dcomp(SummaryStats.sdev(nbrHoodValues, true), result, tol) == 0);
                            break;

                        case VARIANCE:
                            assertTrue(dcomp(SummaryStats.variance(nbrHoodValues, true), result, tol) == 0);
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

