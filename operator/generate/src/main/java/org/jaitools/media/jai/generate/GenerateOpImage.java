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
    
    private final Generator generator;
    
    public GenerateOpImage(
            ImageLayout layout,
            Map config,
            Generator generator) {

        super(null, // source image vector arg not applicable
                layout, 
                config, 
                false  // cobble sources arg not applicable
                );

        this.generator = generator;
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
        final int destWidth = dest.getWidth();
        final int destHeight = dest.getHeight();
        final int numBands = dest.getNumBands();

        final int[] bandOffsets = dest.getBandOffsets();
        final int lineStride = dest.getScanlineStride();
        final int pixelStride = dest.getPixelStride();
        
        final byte[][] destData = dest.getByteDataArrays();

        int lineOffset = 0;
        for (int h = 0; h < destHeight; h++) {
            int y = dest.getY() + h;
            
            int pixelOffset = lineOffset;
            for (int w = 0; w < destWidth; w++) {
                int x = dest.getX() + w;
                
                Number[] values = generator.getValues(x, y);
                for (int b = 0; b < numBands; b++) {
                    int dataOffset = bandOffsets[b] + pixelOffset;
                    destData[b][dataOffset] = values[b].byteValue();
                }
                
                pixelOffset += pixelStride;
            }
            
            lineOffset += lineStride;
        }
    }

    private void generateAsIntData(RasterAccessor dest) {
        final int destWidth = dest.getWidth();
        final int destHeight = dest.getHeight();
        final int numBands = dest.getNumBands();

        final int[] bandOffsets = dest.getBandOffsets();
        final int lineStride = dest.getScanlineStride();
        final int pixelStride = dest.getPixelStride();
        
        final int[][] destData = dest.getIntDataArrays();

        int lineOffset = 0;
        for (int h = 0; h < destHeight; h++) {
            int y = dest.getY() + h;
            
            int pixelOffset = lineOffset;
            for (int w = 0; w < destWidth; w++) {
                int x = dest.getX() + w;
                
                Number[] values = generator.getValues(x, y);
                for (int b = 0; b < numBands; b++) {
                    int dataOffset = bandOffsets[b] + pixelOffset;
                    destData[b][dataOffset] = values[b].intValue();
                }
                
                pixelOffset += pixelStride;
            }
            
            lineOffset += lineStride;
        }
    }

    private void generateAsShortData(RasterAccessor dest) {
        final int destWidth = dest.getWidth();
        final int destHeight = dest.getHeight();
        final int numBands = dest.getNumBands();

        final int[] bandOffsets = dest.getBandOffsets();
        final int lineStride = dest.getScanlineStride();
        final int pixelStride = dest.getPixelStride();
        
        final short[][] destData = dest.getShortDataArrays();

        int lineOffset = 0;
        for (int h = 0; h < destHeight; h++) {
            int y = dest.getY() + h;
            
            int pixelOffset = lineOffset;
            for (int w = 0; w < destWidth; w++) {
                int x = dest.getX() + w;
                
                Number[] values = generator.getValues(x, y);
                for (int b = 0; b < numBands; b++) {
                    int dataOffset = bandOffsets[b] + pixelOffset;
                    destData[b][dataOffset] = values[b].shortValue();
                }
                
                pixelOffset += pixelStride;
            }
            
            lineOffset += lineStride;
        }
    }

    private void generateAsUShortData(RasterAccessor dest) {
        throw new UnsupportedOperationException("Not ready yet");
    }

    private void generateAsFloatData(RasterAccessor dest) {
        final int destWidth = dest.getWidth();
        final int destHeight = dest.getHeight();
        final int numBands = dest.getNumBands();

        final int[] bandOffsets = dest.getBandOffsets();
        final int lineStride = dest.getScanlineStride();
        final int pixelStride = dest.getPixelStride();
        
        final float[][] destData = dest.getFloatDataArrays();

        int lineOffset = 0;
        for (int h = 0; h < destHeight; h++) {
            int y = dest.getY() + h;
            
            int pixelOffset = lineOffset;
            for (int w = 0; w < destWidth; w++) {
                int x = dest.getX() + w;
                
                Number[] values = generator.getValues(x, y);
                for (int b = 0; b < numBands; b++) {
                    int dataOffset = bandOffsets[b] + pixelOffset;
                    destData[b][dataOffset] = values[b].floatValue();
                }
                
                pixelOffset += pixelStride;
            }
            
            lineOffset += lineStride;
        }
    }

    private void generateAsDoubleData(RasterAccessor dest) {
        final int destWidth = dest.getWidth();
        final int destHeight = dest.getHeight();
        final int numBands = dest.getNumBands();

        final int[] bandOffsets = dest.getBandOffsets();
        final int lineStride = dest.getScanlineStride();
        final int pixelStride = dest.getPixelStride();
        
        final double[][] destData = dest.getDoubleDataArrays();

        int lineOffset = 0;
        for (int h = 0; h < destHeight; h++) {
            int y = dest.getY() + h;
            
            int pixelOffset = lineOffset;
            for (int w = 0; w < destWidth; w++) {
                int x = dest.getX() + w;
                
                Number[] values = generator.getValues(x, y);
                for (int b = 0; b < numBands; b++) {
                    int dataOffset = bandOffsets[b] + pixelOffset;
                    destData[b][dataOffset] = values[b].doubleValue();
                }
                
                pixelOffset += pixelStride;
            }
            
            lineOffset += lineStride;
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
