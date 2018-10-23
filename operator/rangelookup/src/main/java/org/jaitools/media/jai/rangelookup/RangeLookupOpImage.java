/* 
 *  Copyright (c) 2009-2013, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.rangelookup;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;

import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

import org.jaitools.numeric.Range;


/**
 * This is a variation on the JAI {@linkplain javax.media.jai.operator.LookupDescriptor}.
 * It works with a {@linkplain RangeLookupTable} object in which each entry maps
 * a source image value range to a destination image value.
 *
 * @see RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions
 * @since 1.0
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class RangeLookupOpImage extends PointOpImage {

    private final RangeLookupTable table;
    private final Number defaultValue;
    private final boolean hasDefault;

     /**
     * Constructor
     * @param source a RenderedImage.
     * @param config configurable attributes of the image
     * 
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *     SampleModel, and ColorModel, or null.
     * 
     * @param table an instance of RangeLookupTable that defines the mappings from source
     *     value ranges to destination values
     * 
     * @param defaultValue either a value to use for all unmatched source values
     *     or null to indicate that unmatched values should pass-through to the
     *     destination
     * 
     * @see RangeLookupDescriptor
     */
    public RangeLookupOpImage(RenderedImage source,
            Map config,
            ImageLayout layout,
            RangeLookupTable table,
            Number defaultValue) {

        super(source, layout, config, true);

        this.table = table;
        this.defaultValue = defaultValue;
        this.hasDefault = defaultValue != null;
    }

    /**
     * Do lookups for the specified destination rectangle
     *
     * @param sources an array of source Rasters
     * @param dest a WritableRaster tile containing the area to be computed.
     * @param destRect the rectangle within dest to be processed.
     */
    @Override
    protected void computeRect(Raster[] sources, WritableRaster dest, Rectangle destRect) {
        RasterFormatTag[] formatTags = getFormatTags();

        Raster source = sources[0];
        Rectangle srcRect = mapDestRect(destRect, 0);


        RasterAccessor srcAcc =
                new RasterAccessor(source, srcRect,
                formatTags[0], getSourceImage(0).getColorModel());

        RasterAccessor destAcc =
                new RasterAccessor(dest, destRect,
                formatTags[1], getColorModel());

        doLookup(srcAcc, destAcc);
    }

    private void doLookup(RasterAccessor srcAcc, RasterAccessor destAcc) {

        switch (destAcc.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                lookupAsByteData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_INT:
                lookupAsIntData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_SHORT:
                lookupAsShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_USHORT:
                lookupAsUShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_FLOAT:
                lookupAsFloatData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_DOUBLE:
                lookupAsDoubleData(srcAcc, destAcc);
                break;
        }

        if (destAcc.isDataCopy()) {
            destAcc.clampDataArrays();
            destAcc.copyDataToRaster();
        }
        
    }

    private void lookupAsByteData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        byte srcData[][] = srcAcc.getByteDataArrays();
        byte destData[][] = destAcc.getByteDataArrays();

        int destWidth = destAcc.getWidth();
        int destHeight = destAcc.getHeight();
        int destBands = destAcc.getNumBands();

        int[] dstBandOffsets = destAcc.getBandOffsets();
        int dstPixelStride = destAcc.getPixelStride();
        int dstScanlineStride = destAcc.getScanlineStride();

        int[] srcBandOffsets = srcAcc.getBandOffsets();
        int srcPixelStride = srcAcc.getPixelStride();
        int srcScanlineStride = srcAcc.getScanlineStride();

        Range lastRange = null;
        
        byte typedDefaultValue = hasDefault ? defaultValue.byteValue() : Byte.MIN_VALUE;
        byte destinationValue = typedDefaultValue;
        
        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            byte destBandData[] = destData[k];
            byte srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    // input value
                    byte val = (byte) (srcBandData[srcPixelOffset] & 0xff);

                    // === destination value
                    if (lastRange == null || !lastRange.contains(val)) {
                        // nullify the current rane
                        lastRange = null;

                        // get a new one if the value falls within some
                        LookupItem item = table.getLookupItem(val);
                        if (item != null) {
                            lastRange = item.getRange();
                            destinationValue = item.getValue().byteValue();
                        } else {
                            // no match: set destination to default value (if defined)
                            // or source value
                            destinationValue = hasDefault ? typedDefaultValue : val;
                        }
                    }
                    destBandData[dstPixelOffset] = destinationValue;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void lookupAsShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        short srcData[][] = srcAcc.getShortDataArrays();
        short destData[][] = destAcc.getShortDataArrays();
        int destWidth = destAcc.getWidth();
        int destHeight = destAcc.getHeight();
        int destBands = destAcc.getNumBands();

        int[] dstBandOffsets = destAcc.getBandOffsets();
        int dstPixelStride = destAcc.getPixelStride();
        int dstScanlineStride = destAcc.getScanlineStride();

        int[] srcBandOffsets = srcAcc.getBandOffsets();
        int srcPixelStride = srcAcc.getPixelStride();
        int srcScanlineStride = srcAcc.getScanlineStride();    
        
        Range lastRange = null;
        
        short typedDefaultValue = hasDefault ? defaultValue.shortValue() : Short.MIN_VALUE;
        short destinationValue = typedDefaultValue;
        
        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            short destBandData[] = destData[k];
            short srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    short val = srcBandData[srcPixelOffset];
                    // === destination value
                    if (lastRange == null || !lastRange.contains(val)) {
                        // nullify the current rane
                        lastRange = null;

                        // get a new one if the value falls within some
                        LookupItem item = table.getLookupItem(val);
                        if (item != null) {
                            lastRange = item.getRange();
                            destinationValue = item.getValue().shortValue();
                        } else {
                            // no match: set destination to default value (if defined)
                            // or source value
                            destinationValue = hasDefault ? typedDefaultValue : val;
                        }
                    }
                    destBandData[dstPixelOffset] = destinationValue;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void lookupAsUShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        short srcData[][] = srcAcc.getShortDataArrays();
        short destData[][] = destAcc.getShortDataArrays();
        int destWidth = destAcc.getWidth();
        int destHeight = destAcc.getHeight();
        int destBands = destAcc.getNumBands();

        int[] dstBandOffsets = destAcc.getBandOffsets();
        int dstPixelStride = destAcc.getPixelStride();
        int dstScanlineStride = destAcc.getScanlineStride();

        int[] srcBandOffsets = srcAcc.getBandOffsets();
        int srcPixelStride = srcAcc.getPixelStride();
        int srcScanlineStride = srcAcc.getScanlineStride();        

        Range lastRange = null;
        
        short typedDefaultValue = hasDefault ? defaultValue.shortValue() : 0;
        short destinationValue = typedDefaultValue;

        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            short destBandData[] = destData[k];
            short srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    int val = srcBandData[srcPixelOffset]  & 0xffff;
                    
                    // === destination value
                    if(lastRange==null|| !lastRange.contains(val)){
                        // nullify the current range
                        lastRange=null;
                        
                        // get a new one if the value falls within some
                        LookupItem item = table.getLookupItem(val);

                        if (item != null) {
                            lastRange = item.getRange();
                            destinationValue = item.getValue().shortValue();
                        } else {
                            // no match: set destination to default value (if defined)
                            // or source value
                            destinationValue = hasDefault ? typedDefaultValue : (short) val;
                        }
                    }                    
                    destBandData[dstPixelOffset] = destinationValue;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void lookupAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        int srcData[][] = srcAcc.getIntDataArrays();
        int destData[][] = destAcc.getIntDataArrays();
        int destWidth = destAcc.getWidth();
        int destHeight = destAcc.getHeight();
        int destBands = destAcc.getNumBands();

        int[] dstBandOffsets = destAcc.getBandOffsets();
        int dstPixelStride = destAcc.getPixelStride();
        int dstScanlineStride = destAcc.getScanlineStride();

        int[] srcBandOffsets = srcAcc.getBandOffsets();
        int srcPixelStride = srcAcc.getPixelStride();
        int srcScanlineStride = srcAcc.getScanlineStride();        

        Range lastRange = null;
        
        int typedDefaultValue = hasDefault ? defaultValue.intValue() : Integer.MIN_VALUE;
        int destinationValue = typedDefaultValue;

        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            int destBandData[] = destData[k];
            int srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    int val = srcBandData[srcPixelOffset];
                    // === destination value
                    if (lastRange == null || !lastRange.contains(val)) {
                        // nullify the current range
                        lastRange = null;

                        // get a new one if the value falls within some
                        LookupItem item = table.getLookupItem(val);

                        if (item != null) {
                            lastRange = item.getRange();
                            destinationValue = item.getValue().intValue();
                        } else {
                            // no match: set destination to default value (if defined)
                            // or source value
                            destinationValue = hasDefault ? typedDefaultValue : val;
                        }
                    }
                    destBandData[dstPixelOffset] = destinationValue;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void lookupAsFloatData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        float srcData[][] = srcAcc.getFloatDataArrays();
        float destData[][] = destAcc.getFloatDataArrays();
        int destWidth = destAcc.getWidth();
        int destHeight = destAcc.getHeight();
        int destBands = destAcc.getNumBands();

        int[] dstBandOffsets = destAcc.getBandOffsets();
        int dstPixelStride = destAcc.getPixelStride();
        int dstScanlineStride = destAcc.getScanlineStride();

        int[] srcBandOffsets = srcAcc.getBandOffsets();
        int srcPixelStride = srcAcc.getPixelStride();
        int srcScanlineStride = srcAcc.getScanlineStride();        

        Range lastRange = null;
        
        float typedDefaultValue = hasDefault ? defaultValue.floatValue() : Float.NaN;
        float destinationValue = typedDefaultValue;

        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            float destBandData[] = destData[k];
            float srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    float val = srcBandData[srcPixelOffset];
                    // === destination value
                    if (lastRange == null || !lastRange.contains(val)) {
                        // nullify the current range
                        lastRange = null;

                        // get a new one if the value falls within some
                        LookupItem item = table.getLookupItem(val);
                        if (item != null) {
                            lastRange = item.getRange();
                            destinationValue = item.getValue().floatValue();
                        } else {
                            // no match: set destination to default value (if defined)
                            // or source value
                            destinationValue = hasDefault ? typedDefaultValue : val;
                        }
                    }
                    destBandData[dstPixelOffset] = destinationValue;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void lookupAsDoubleData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        double srcData[][] = srcAcc.getDoubleDataArrays();
        double destData[][] = destAcc.getDoubleDataArrays();

        int destWidth = destAcc.getWidth();
        int destHeight = destAcc.getHeight();
        int destBands = destAcc.getNumBands();

        int[] dstBandOffsets = destAcc.getBandOffsets();
        int dstPixelStride = destAcc.getPixelStride();
        int dstScanlineStride = destAcc.getScanlineStride();

        int[] srcBandOffsets = srcAcc.getBandOffsets();
        int srcPixelStride = srcAcc.getPixelStride();
        int srcScanlineStride = srcAcc.getScanlineStride();

        Range lastRange = null;
        
        double typedDefaultValue = hasDefault ? defaultValue.doubleValue() : Double.NaN;
        double destinationValue = typedDefaultValue;

        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            double destBandData[] = destData[k];
            double srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    double val = srcBandData[srcPixelOffset];
                    // === destination value
                    if (lastRange == null || !lastRange.contains(val)) {
                        // nullify the current range
                        lastRange = null;

                        // get a new one if the value falls within some
                        LookupItem item = table.getLookupItem(val);
                        if (item != null) {
                            lastRange = item.getRange();
                            destinationValue = item.getValue().doubleValue();
                        } else {
                            // no match: set destination to default value (if defined)
                            // or source value
                            destinationValue = hasDefault ? typedDefaultValue : val;
                        }
                    }
                    destBandData[dstPixelOffset] = destinationValue;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }
}
