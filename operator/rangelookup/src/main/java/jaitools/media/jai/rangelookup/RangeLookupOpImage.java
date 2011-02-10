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

package jaitools.media.jai.rangelookup;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

/**
 * This is a variation on the JAI {@linkplain javax.media.jai.LookupDescriptor}.
 * It works with a {@linkplain RangeLookupTable} object in which each entry maps
 * a source image value range to a destination image value.
 *
 * @see RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class RangeLookupOpImage extends PointOpImage {

    private RangeLookupTable table;

     /* Source image variables */
    private int[] srcBandOffsets;
    private int srcPixelStride;
    private int srcScanlineStride;

    /* Destination image variables */
    private int destWidth;
    private int destHeight;
    private int destBands;
    private int[] dstBandOffsets;
    private int dstPixelStride;
    private int dstScanlineStride;




    /**
     * Constructor
     * @param source a RenderedImage.
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *        SampleModel, and ColorModel, or null.
     * @param table an instance of RangeLookupTable that defines the mappings from source
     * image value ranges to destination image values
     * 
     * @see RangeLookupDescriptor
     */
    public RangeLookupOpImage(RenderedImage source,
            Map config,
            ImageLayout layout,
            RangeLookupTable table) {

        super(source, layout, config, true);

        this.table = table;
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
        destWidth = destAcc.getWidth();
        destHeight = destAcc.getHeight();
        destBands = destAcc.getNumBands();

        dstBandOffsets = destAcc.getBandOffsets();
        dstPixelStride = destAcc.getPixelStride();
        dstScanlineStride = destAcc.getScanlineStride();

        srcBandOffsets = srcAcc.getBandOffsets();
        srcPixelStride = srcAcc.getPixelStride();
        srcScanlineStride = srcAcc.getScanlineStride();

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
                    byte val = (byte) (srcBandData[srcPixelOffset] & 0xff);
                    destBandData[dstPixelOffset] = table.getDestValue(val).byteValue();
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
                    destBandData[dstPixelOffset] = table.getDestValue(val).shortValue();
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void lookupAsUShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void lookupAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {
        int srcData[][] = srcAcc.getIntDataArrays();
        int destData[][] = destAcc.getIntDataArrays();

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
                    destBandData[dstPixelOffset] = table.getDestValue(val).intValue();
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
                    destBandData[dstPixelOffset] = table.getDestValue(val).floatValue();
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
                    destBandData[dstPixelOffset] = table.getDestValue(val).doubleValue();
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }
}

