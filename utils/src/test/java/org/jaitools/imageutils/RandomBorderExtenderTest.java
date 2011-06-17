/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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

package org.jaitools.imageutils;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;

import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;

import org.jaitools.numeric.CompareOp;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for RandomBorderExtender
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class RandomBorderExtenderTest {

    private static final int SOURCE_WIDTH = 10;
    private static final int BUFFER_WIDTH = 2;

    private final PlanarImage sourceImage;

    public RandomBorderExtenderTest() {
        sourceImage = ImageUtils.createConstantImage(SOURCE_WIDTH, SOURCE_WIDTH, 0);
    }

    @Test
    public void testExtendByte() {
        System.out.println("   image type BYTE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_BYTE);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), 0, 10);
    }

    @Test
    public void testExtendShort() {
        System.out.println("   image type SHORT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_SHORT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), -10, 10);
    }

    @Test
    public void testExtendUShort() {
        System.out.println("   image type USHORT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_USHORT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), 0, 10);
    }

    @Test
    public void testExtendInt() {
        System.out.println("   image type INT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_INT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsInt(raster, sourceImage.getBounds(), -10, 10);
    }

    @Test
    public void testExtendFloat() {
        System.out.println("   image type FLOAT");

        WritableRaster raster = createRaster(DataBuffer.TYPE_FLOAT);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsDouble(raster, sourceImage.getBounds(), -10, 10);
    }

    @Test
    public void testExtendDouble() {
        System.out.println("   image type DOUBLE");

        WritableRaster raster = createRaster(DataBuffer.TYPE_DOUBLE);

        RandomBorderExtender ex = new RandomBorderExtender(-10, 10);
        ex.extend(raster, sourceImage);

        checkResultAsDouble(raster, sourceImage.getBounds(), -10, 10);
    }

    private void checkResultAsInt(WritableRaster raster, Rectangle srcRectangle, int minValue, int maxValue) {
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!srcRectangle.contains(x, y)) {
                    int value = raster.getSample(x, y, 0);
                    assertTrue(value >= minValue && value < maxValue);
                }
            }
        }
    }

    private void checkResultAsDouble(WritableRaster raster, Rectangle srcRectangle, double minValue, double maxValue) {
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!srcRectangle.contains(x, y)) {
                    double value = raster.getSampleDouble(x, y, 0);
                    assertTrue(CompareOp.acompare(value, minValue) >= 0);
                    assertTrue(CompareOp.acompare(value, maxValue) < 0);
                }
            }
        }
    }

    private WritableRaster createRaster(int dataType) {
        return RasterFactory.createBandedRaster(
                dataType,
                SOURCE_WIDTH + 2 * BUFFER_WIDTH,
                SOURCE_WIDTH + 2 * BUFFER_WIDTH,
                1, new Point(-BUFFER_WIDTH, -BUFFER_WIDTH));
    }

}
