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

package jaitools.imageutils;

import jaitools.numeric.NumberOperations;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.Random;
import javax.media.jai.BorderExtender;
import javax.media.jai.PlanarImage;

/**
 * A BorderExtender that generates uniform random pixel values in a
 * user-specified range
 *
 * @see BorderExtender
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class RandomBorderExtender extends BorderExtender {

    private final Number minValueN;
    private final Number maxValueN;
    private final Random rand;

    /**
     * Creates a border extender that will buffer an image with values uniformly
     * drawn from the range {@code minValue} (inclusive) to {@code maxValue} (exclusive).
     *
     * @param minValue lowest value that can be generated
     * @param maxValue highest value that can be generated
     *
     */
    public RandomBorderExtender(Number minValue, Number maxValue) {
        this.minValueN = NumberOperations.newInstance(minValue, minValue.getClass());
        this.maxValueN = NumberOperations.newInstance(maxValue, maxValue.getClass());
        this.rand = new Random();
    }

    @Override
    public void extend(WritableRaster raster, PlanarImage sourceImage) {

        int dataType = raster.getSampleModel().getDataType();
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                extendAsByte(raster, sourceImage);
                break;

            case DataBuffer.TYPE_SHORT:
                extendAsShort(raster, sourceImage);
                break;

            case DataBuffer.TYPE_USHORT:
                extendAsUShort(raster, sourceImage);
                break;

            case DataBuffer.TYPE_INT:
                extendAsInt(raster, sourceImage);
                break;

            case DataBuffer.TYPE_FLOAT:
                extendAsFloat(raster, sourceImage);
                break;

            case DataBuffer.TYPE_DOUBLE:
                extendAsDouble(raster, sourceImage);
                break;

            default:
                throw new UnsupportedOperationException("Unsupported data type");
        }
    }

    private void extendAsByte(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();

        int minValue = NumberOperations.intValue(minValueN);
        int maxValue = NumberOperations.intValue(maxValueN);
        int range = maxValue - minValue;

        for (int b = 0; b < raster.getNumBands(); b++) {
            for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
                for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                    if (!bounds.contains(x, y)) {
                        int value = (int) clamp(rand.nextInt(range) + minValue, 0, 255);
                        raster.setSample(x, y, b, value);
                    }
                }
            }
        }
    }

    private void extendAsShort(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();

        int minValue = NumberOperations.intValue(minValueN);
        int maxValue = NumberOperations.intValue(maxValueN);
        int range = maxValue - minValue;

        for (int b = 0; b < raster.getNumBands(); b++) {
            for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
                for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                    if (!bounds.contains(x, y)) {
                        int value = (int) clamp(rand.nextInt(range) + minValue,
                                Short.MIN_VALUE, Short.MAX_VALUE);
                        raster.setSample(x, y, b, value);
                    }
                }
            }
        }
    }

    private void extendAsUShort(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();

        int minValue = NumberOperations.intValue(minValueN);
        int maxValue = NumberOperations.intValue(maxValueN);
        int range = maxValue - minValue;

        for (int b = 0; b < raster.getNumBands(); b++) {
            for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
                for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                    if (!bounds.contains(x, y)) {
                        int value = (int) clamp(rand.nextInt(range) + minValue, 0, 0xffff);
                        raster.setSample(x, y, b, value);
                    }
                }
            }
        }
    }

    private void extendAsInt(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();

        int minValue = NumberOperations.intValue(minValueN);
        int maxValue = NumberOperations.intValue(maxValueN);
        int range = maxValue - minValue;

        for (int b = 0; b < raster.getNumBands(); b++) {
            for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
                for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                    if (!bounds.contains(x, y)) {
                        int value = (int) clamp(rand.nextInt(range) + minValue,
                                Integer.MIN_VALUE, Integer.MAX_VALUE);
                        raster.setSample(x, y, b, value);
                    }
                }
            }
        }
    }

    private void extendAsFloat(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();

        float minValue = NumberOperations.floatValue(minValueN);
        float maxValue = NumberOperations.floatValue(maxValueN);
        float range = maxValue - minValue;

        for (int b = 0; b < raster.getNumBands(); b++) {
            for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
                for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                    if (!bounds.contains(x, y)) {
                        float value = rand.nextFloat() * range + minValue;
                        raster.setSample(x, y, b, value);
                    }
                }
            }
        }
    }

    private void extendAsDouble(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();

        double minValue = NumberOperations.doubleValue(minValueN);
        double maxValue = NumberOperations.doubleValue(maxValueN);
        double range = maxValue - minValue;

        for (int b = 0; b < raster.getNumBands(); b++) {
            for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
                for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                    if (!bounds.contains(x, y)) {
                        double value = rand.nextDouble() * range + minValue;
                        raster.setSample(x, y, b, value);
                    }
                }
            }
        }
    }

    private long clamp(long value, long min, long max) {
        return Math.max(Math.min(value, max), min);
    }

}
