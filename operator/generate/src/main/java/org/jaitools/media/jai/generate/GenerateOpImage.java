/* 
 *  Copyright (c) 2013, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.generate;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.util.Map;

import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterAccessor;

/**
 *
 * @author Michael Bedward
 * @since 1.3
 */
public class GenerateOpImage extends OpImage {
    
    private final Generator[] generators;
    
    public GenerateOpImage(
            ImageLayout layout,
            Map config,
            Generator[] generators) {

        super(null, // source image vector arg not applicable
                layout, 
                config, 
                false  // cobble sources arg not applicable
                );

        this.generators = generators;
    }

    @Override
    public boolean computesUniqueTiles() {
        return true;
    }

    @Override
    protected void computeRect(PlanarImage[] sourcesIgnored, WritableRaster dest, Rectangle destRect) {
        RasterAccessor destAcc = new RasterAccessor(
                dest, destRect, getFormatTags()[0], getColorModel());
        
        switch (destAcc.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                generateAsByteData(destAcc);
                break;
            case DataBuffer.TYPE_INT:
                generateAsIntData(destAcc);
                break;
            case DataBuffer.TYPE_SHORT:
                generateAsShortData(destAcc);
                break;
            case DataBuffer.TYPE_USHORT:
                generateAsUShortData(destAcc);
                break;
            case DataBuffer.TYPE_FLOAT:
                generateAsFloatData(destAcc);
                break;
            case DataBuffer.TYPE_DOUBLE:
                generateAsDoubleData(destAcc);
                break;
        }

        destAcc.copyDataToRaster();
    }
    
    private void generateAsByteData(RasterAccessor dest) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int numBands = dest.getNumBands();

        int lineStride = dest.getScanlineStride();
        int pixelStride = dest.getPixelStride();
        int[] bandOffsets = dest.getBandOffsets();
        byte[][] destData = dest.getByteDataArrays();

        for (int b = 0; b < numBands; b++) {
            byte[] bandData = destData[b];
            int lineOffset = bandOffsets[b];
            Generator gen = generators[b];

            for (int h = 0; h < destHeight; h++) {
                int y = dest.getY() + h;
                int dstPixelOffset = lineOffset;
                lineOffset += lineStride;
                
                for (int w = 0; w < destWidth; w++) {
                    int x = dest.getX() + w;
		    bandData[dstPixelOffset] = gen.getValue(x, y).byteValue();
                    dstPixelOffset += pixelStride;
                }
            }
        }
    }

    private void generateAsIntData(RasterAccessor dest) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int numBands = dest.getNumBands();

        int lineStride = dest.getScanlineStride();
        int pixelStride = dest.getPixelStride();
        int[] bandOffsets = dest.getBandOffsets();
        int[][] destData = dest.getIntDataArrays();

        for (int b = 0; b < numBands; b++) {
            int[] bandData = destData[b];
            int lineOffset = bandOffsets[b];
            Generator gen = generators[b];

            for (int h = 0; h < destHeight; h++) {
                int y = dest.getY() + h;
                int dstPixelOffset = lineOffset;
                lineOffset += lineStride;
                
                for (int w = 0; w < destWidth; w++) {
                    int x = dest.getX() + w;
		    bandData[dstPixelOffset] = gen.getValue(x, y).intValue();
                    dstPixelOffset += pixelStride;
                }
            }
        }
    }

    private void generateAsShortData(RasterAccessor dest) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int numBands = dest.getNumBands();

        int lineStride = dest.getScanlineStride();
        int pixelStride = dest.getPixelStride();
        int[] bandOffsets = dest.getBandOffsets();
        short[][] destData = dest.getShortDataArrays();

        for (int b = 0; b < numBands; b++) {
            short[] bandData = destData[b];
            int lineOffset = bandOffsets[b];
            Generator gen = generators[b];

            for (int h = 0; h < destHeight; h++) {
                int y = dest.getY() + h;
                int dstPixelOffset = lineOffset;
                lineOffset += lineStride;
                
                for (int w = 0; w < destWidth; w++) {
                    int x = dest.getX() + w;
		    bandData[dstPixelOffset] = gen.getValue(x, y).shortValue();
                    dstPixelOffset += pixelStride;
                }
            }
        }
    }

    private void generateAsUShortData(RasterAccessor dest) {
        throw new UnsupportedOperationException("Not ready yet");
    }

    private void generateAsFloatData(RasterAccessor dest) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int numBands = dest.getNumBands();

        int lineStride = dest.getScanlineStride();
        int pixelStride = dest.getPixelStride();
        int[] bandOffsets = dest.getBandOffsets();
        float[][] destData = dest.getFloatDataArrays();

        for (int b = 0; b < numBands; b++) {
            float[] bandData = destData[b];
            int lineOffset = bandOffsets[b];
            Generator gen = generators[b];

            for (int h = 0; h < destHeight; h++) {
                int y = dest.getY() + h;
                int dstPixelOffset = lineOffset;
                lineOffset += lineStride;
                
                for (int w = 0; w < destWidth; w++) {
                    int x = dest.getX() + w;
		    bandData[dstPixelOffset] = gen.getValue(x, y).floatValue();
                    dstPixelOffset += pixelStride;
                }
            }
        }
    }

    private void generateAsDoubleData(RasterAccessor dest) {
        int destWidth = dest.getWidth();
        int destHeight = dest.getHeight();
        int numBands = dest.getNumBands();

        int lineStride = dest.getScanlineStride();
        int pixelStride = dest.getPixelStride();
        int[] bandOffsets = dest.getBandOffsets();
        double[][] destData = dest.getDoubleDataArrays();

        for (int b = 0; b < numBands; b++) {
            double[] bandData = destData[b];
            int lineOffset = bandOffsets[b];
            Generator gen = generators[b];

            for (int h = 0; h < destHeight; h++) {
                int y = dest.getY() + h;
                int dstPixelOffset = lineOffset;
                lineOffset += lineStride;
                
                for (int w = 0; w < destWidth; w++) {
                    int x = dest.getX() + w;
		    bandData[dstPixelOffset] = gen.getValue(x, y).doubleValue();
                    dstPixelOffset += pixelStride;
                }
            }
        }
    }

    @Override
    public Raster computeTile(int tileX, int tileY) {
        Point origin = new Point(tileXToX(tileX), tileYToY(tileY));
        WritableRaster dest = createWritableRaster(sampleModel, origin);

        Rectangle tileRect = new Rectangle(origin.x, origin.y,
                                       sampleModel.getWidth(),
                                       sampleModel.getHeight());
        
        Rectangle destRect = tileRect.intersection(getBounds());
        computeRect((PlanarImage[])null, dest, destRect);
        return dest;
    }

    @Override
    public Rectangle mapSourceRect(Rectangle rctngl, int i) {
        throw new UnsupportedOperationException("Generate operation does not have source images.");
    }

    @Override
    public Rectangle mapDestRect(Rectangle rctngl, int i) {
        throw new UnsupportedOperationException("Generate operation does not have source iamges.");
    }

}
