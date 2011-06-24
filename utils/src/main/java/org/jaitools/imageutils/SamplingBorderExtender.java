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

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.WritableRaster;
import java.util.Random;

import javax.media.jai.BorderExtender;
import javax.media.jai.PlanarImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/**
 * A BorderExtender that generates pixel values by randomly sampling
 * the source image within a threshold distance of each border pixel
 *
 * @see BorderExtender
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SamplingBorderExtender extends BorderExtender {

    private final int maxDistance;
    private final Random rand;

    /**
     * Creates a border extender that generate a value for each border pixel by
     * randomly sampling the area of the source image that lies within
     * {@code maxDistance} pixels of the border pixel.
     *
     * @param maxDistance the maximum distance from a border pixel of source
     *        image pixels that are sampled
     */
    public SamplingBorderExtender(int maxDistance) {
        this.maxDistance = maxDistance;
        this.rand = new Random();
    }

    /**
     * Creates the extended border.
     * 
     * @param raster the destination raster
     * @param sourceImage the source image
     */
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
        Rectangle samplingBounds = new Rectangle(0, 0, 2 * maxDistance + 1, 2 * maxDistance + 1);
        RandomIter srcIter = RandomIterFactory.create(sourceImage, bounds);

        int[] pixel = new int[raster.getNumBands()];
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!bounds.contains(x, y)) {
                    samplingBounds.setLocation(x - maxDistance, y - maxDistance);
                    Rectangle sourceSamplingBounds = samplingBounds.intersection(bounds);
                    int sx = rand.nextInt(sourceSamplingBounds.width) + sourceSamplingBounds.x;
                    int sy = rand.nextInt(sourceSamplingBounds.height) + sourceSamplingBounds.y;
                    srcIter.getPixel(sx, sy, pixel);
                    for (int b = 0; b < raster.getNumBands(); b++) {
                        raster.setSample(x, y, b, pixel[b] & 0xff);
                    }
                }
            }
        }
    }

    private void extendAsShort(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();
        Rectangle samplingBounds = new Rectangle(0, 0, 2 * maxDistance + 1, 2 * maxDistance + 1);
        RandomIter srcIter = RandomIterFactory.create(sourceImage, bounds);

        int[] pixel = new int[raster.getNumBands()];
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!bounds.contains(x, y)) {
                    samplingBounds.setLocation(x - maxDistance, y - maxDistance);
                    Rectangle sourceSamplingBounds = samplingBounds.intersection(bounds);
                    int sx = rand.nextInt(sourceSamplingBounds.width) + sourceSamplingBounds.x;
                    int sy = rand.nextInt(sourceSamplingBounds.height) + sourceSamplingBounds.y;
                    srcIter.getPixel(sx, sy, pixel);
                    for (int b = 0; b < raster.getNumBands(); b++) {
                        raster.setSample(x, y, b, clamp(pixel[b], Short.MIN_VALUE, Short.MAX_VALUE));
                    }
                }
            }
        }
    }

    private void extendAsUShort(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();
        Rectangle samplingBounds = new Rectangle(0, 0, 2 * maxDistance + 1, 2 * maxDistance + 1);
        RandomIter srcIter = RandomIterFactory.create(sourceImage, bounds);

        int[] pixel = new int[raster.getNumBands()];
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!bounds.contains(x, y)) {
                    samplingBounds.setLocation(x - maxDistance, y - maxDistance);
                    Rectangle sourceSamplingBounds = samplingBounds.intersection(bounds);
                    int sx = rand.nextInt(sourceSamplingBounds.width) + sourceSamplingBounds.x;
                    int sy = rand.nextInt(sourceSamplingBounds.height) + sourceSamplingBounds.y;
                    srcIter.getPixel(sx, sy, pixel);
                    for (int b = 0; b < raster.getNumBands(); b++) {
                        raster.setSample(x, y, b, pixel[b] & 0xffff);
                    }
                }
            }
        }
    }

    private void extendAsInt(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();
        Rectangle samplingBounds = new Rectangle(0, 0, 2 * maxDistance + 1, 2 * maxDistance + 1);
        RandomIter srcIter = RandomIterFactory.create(sourceImage, bounds);

        int[] pixel = new int[raster.getNumBands()];
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!bounds.contains(x, y)) {
                    samplingBounds.setLocation(x - maxDistance, y - maxDistance);
                    Rectangle sourceSamplingBounds = samplingBounds.intersection(bounds);
                    int sx = rand.nextInt(sourceSamplingBounds.width) + sourceSamplingBounds.x;
                    int sy = rand.nextInt(sourceSamplingBounds.height) + sourceSamplingBounds.y;
                    srcIter.getPixel(sx, sy, pixel);
                    raster.setPixel(x, y, pixel);
                }
            }
        }
    }

    private void extendAsFloat(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();
        Rectangle samplingBounds = new Rectangle(0, 0, 2 * maxDistance + 1, 2 * maxDistance + 1);
        RandomIter srcIter = RandomIterFactory.create(sourceImage, bounds);

        float[] pixel = new float[raster.getNumBands()];
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!bounds.contains(x, y)) {
                    samplingBounds.setLocation(x - maxDistance, y - maxDistance);
                    Rectangle sourceSamplingBounds = samplingBounds.intersection(bounds);
                    int sx = rand.nextInt(sourceSamplingBounds.width) + sourceSamplingBounds.x;
                    int sy = rand.nextInt(sourceSamplingBounds.height) + sourceSamplingBounds.y;
                    srcIter.getPixel(sx, sy, pixel);
                    raster.setPixel(x, y, pixel);
                }
            }
        }
    }

    private void extendAsDouble(WritableRaster raster, PlanarImage sourceImage) {
        Rectangle bounds = sourceImage.getBounds();
        Rectangle samplingBounds = new Rectangle(0, 0, 2 * maxDistance + 1, 2 * maxDistance + 1);
        RandomIter srcIter = RandomIterFactory.create(sourceImage, bounds);

        double[] pixel = new double[raster.getNumBands()];
        for (int y = raster.getMinY(), ny = 0; ny < raster.getHeight(); y++, ny++) {
            for (int x = raster.getMinX(), nx = 0; nx < raster.getWidth(); x++, nx++) {
                if (!bounds.contains(x, y)) {
                    samplingBounds.setLocation(x - maxDistance, y - maxDistance);
                    Rectangle sourceSamplingBounds = samplingBounds.intersection(bounds);
                    int sx = rand.nextInt(sourceSamplingBounds.width) + sourceSamplingBounds.x;
                    int sy = rand.nextInt(sourceSamplingBounds.height) + sourceSamplingBounds.y;
                    srcIter.getPixel(sx, sy, pixel);
                    raster.setPixel(x, y, pixel);
                }
            }
        }
    }

    private int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

}
