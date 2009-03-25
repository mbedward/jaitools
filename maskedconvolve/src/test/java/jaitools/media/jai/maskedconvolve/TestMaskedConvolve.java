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
package jaitools.media.jai.maskedconvolve;

import java.awt.Dimension;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.JAI;
import javax.media.jai.KernelJAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class TestMaskedConvolve {

    // round off tolerance for float comparisons
    private static final float FTOL = 1.0e-8f;
    private static RenderedImage theImage = null;

    /**
     * Test operator with no masking - should get the same results as
     * standard convolve operator
     */
    @Test
    public void testConvolve() {
        System.out.println("   testing symmetric kernel");
        RenderedImage testImg = getTestImage();

        KernelJAI kernel = new KernelJAI(3, 3,
                new float[]{
                    1f, 1f, 1f,
                    1f, 0f, 1f,
                    1f, 1f, 1f});

        compareStandardToMasked(testImg, kernel);
    }

    @Test
    public void testAsymmetricKernel() {
        System.out.println("   testing asymmetric kernel");
        RenderedImage testImg = getTestImage();
        KernelJAI kernel = new KernelJAI(3, 3, 0, 0, new float[]{
            0f,1f,1f,
            1f,1f,1f,
            1f,1f,1f
        });

        compareStandardToMasked(testImg, kernel);
    }

    /**
     * Run a comparison of results between standard convolution and
     * mssked convolution with masking disabled
     */
    private void compareStandardToMasked(RenderedImage testImg, KernelJAI kernel) {
        ParameterBlockJAI pb = new ParameterBlockJAI("convolve");
        pb.setSource("source0", testImg);
        pb.setParameter("kernel", kernel);
        RenderingHints hints = new RenderingHints(
                JAI.KEY_BORDER_EXTENDER,
                BorderExtender.createInstance(BorderExtender.BORDER_ZERO));
        RenderedOp stdConvImg = JAI.create("convolve", pb, hints);

        pb = new ParameterBlockJAI("maskedconvolve");
        pb.setSource("source0", testImg);
        pb.setParameter("kernel", kernel);
        ROI roi = new ROI(testImg, -1);
        pb.setParameter("roi", roi);
        RenderedOp maskedConvImg = JAI.create("maskedconvolve", pb, hints);

        RectIter stdIter = RectIterFactory.create(stdConvImg, null);
        RectIter maskedIter = RectIterFactory.create(maskedConvImg, null);

        boolean stdDone, maskedDone;
        do {
            do {
                float stdVal = stdIter.getSampleFloat();
                float maskedVal = maskedIter.getSampleFloat();
                assertTrue(Math.abs(stdVal - maskedVal) < FTOL);

                stdDone = stdIter.nextPixelDone();
                maskedDone = maskedIter.nextPixelDone();
            } while (!stdDone && !maskedDone);

            if (!(stdDone && maskedDone)) {
                // images out of sync
                fail("images out of sync");
            }

            stdIter.startPixels();
            maskedIter.startPixels();
            stdDone = stdIter.nextLineDone();
            maskedDone = maskedIter.nextLineDone();
        } while (!stdDone && !maskedDone);

        if (!(stdDone && maskedDone)) {
            // images out of sync
            fail("images out of sync");
        }
    }

    /**
     * Create a test image two tiles wide, one tile high, with edge
     * pixels having value 0 and inner pixels having value 1
     */
    private RenderedImage getTestImage() {
        if (theImage == null) {
            int tileW = 128;

            JAI.setDefaultTileSize(new Dimension(tileW, tileW));
            ParameterBlockJAI pb = new ParameterBlockJAI("constant");
            pb.setParameter("width", 2f * tileW);
            pb.setParameter("height", (float) tileW);
            pb.setParameter("bandvalues", new Integer[]{0});
            RenderedOp zeroImg = JAI.create("constant", pb);

            pb = new ParameterBlockJAI("constant");
            pb.setParameter("width", 2f * tileW - 2);
            pb.setParameter("height", (float) tileW - 2);
            pb.setParameter("bandvalues", new Integer[]{1});
            RenderedOp oneImg = JAI.create("constant", pb);

            pb = new ParameterBlockJAI("translate");
            pb.setSource("source0", oneImg);
            pb.setParameter("xtrans", 1.0f);
            pb.setParameter("ytrans", 1.0f);
            RenderedOp oneImgT = JAI.create("translate", pb);

            pb = new ParameterBlockJAI("overlay");
            pb.setSource("source0", zeroImg);
            pb.setSource("source1", oneImgT);
            theImage = JAI.create("overlay", pb);
        }

        return theImage;
    }
}
