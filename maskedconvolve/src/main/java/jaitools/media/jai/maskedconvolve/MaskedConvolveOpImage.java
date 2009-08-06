/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.media.jai.maskedconvolve;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Map;
import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

/**
 * An operator to perform masked convolution on a source image.
 * @see MaskedConvolveDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class MaskedConvolveOpImage extends AreaOpImage {

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
    
    /* Kernel variables. */
    private float[] kernelData;
    private int kernelW,  kernelH;
    private int kernelKeyX, kernelKeyY;
    
    /* ROI and options */
    private ROI roi;
    private boolean maskSrc;
    private boolean maskDest;


    /**
     * Constructor
     * @param source a RenderedImage.
     * @param extender a BorderExtender, or null.
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *        SampleModel, and ColorModel, or null.
     * @param kernel the convolution kernel
     * @param roi the ROI used to control masking; must contain the source image bounds
     * @param maskSrc if true, exclude masked pixels ({@code roi.contains == false}) from
     * convolution kernel calculation
     * @param maskDest if true, do not place kernel over masked pixels (dest will be 0)
     * @throws IllegalArgumentException if the roi's bounds do not contain the entire
     * source image
     * @see MaskedConvolveDescriptor
     */
    public MaskedConvolveOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            KernelJAI kernel,
            ROI roi,
            Boolean maskSrc,
            Boolean maskDest) {
        
        super(source,
                layout,
                config,
                true,
                extender,
                kernel.getLeftPadding(),
                kernel.getRightPadding(),
                kernel.getTopPadding(),
                kernel.getBottomPadding());

        Rectangle sourceBounds = new Rectangle(
                source.getMinX(), source.getMinY(), source.getWidth(), source.getHeight());
        
        if (!roi.getBounds().contains(sourceBounds)) {
            throw new IllegalArgumentException("The bounds of the ROI must contain the source image");
        }

        this.roi = roi;
        this.maskSrc = (maskSrc == null ? false : maskSrc.booleanValue());
        this.maskDest = (maskDest == null ? false : maskDest.booleanValue());

        kernelData = kernel.getKernelData();
        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();
    }

    /**
     * Performs convolution on a specified rectangle. 
     *
     * @param sources an array of source Rasters, guaranteed to provide all
     *        necessary source data for computing the output.
     * @param dest a WritableRaster tile containing the area to be computed.
     * @param destRect the rectangle within dest to be processed.
     */
    @Override
    protected void computeRect(Raster[] sources,
            WritableRaster dest,
            Rectangle destRect) {

        RasterFormatTag[] formatTags = getFormatTags();

        Raster source = sources[0];
        Rectangle srcRect = mapDestRect(destRect, 0);


        RasterAccessor srcAcc =
                new RasterAccessor(source, srcRect,
                formatTags[0], getSourceImage(0).getColorModel());
        
        RasterAccessor destAcc =
                new RasterAccessor(dest, destRect,
                formatTags[1], getColorModel());
        
        convolve(srcAcc, destAcc);
    }

    /**
     * Initialize common variables then delegate the convolution to
     * one of the data-type-specific methods
     * 
     * @param srcAcc source raster accessor
     * @param destAcc dest raster accessor
     */
    private void convolve(RasterAccessor srcAcc, RasterAccessor destAcc) {
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
                convolveAsByteData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_INT:
                convolveAsIntData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_SHORT:
                convolveAsShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_USHORT:
                convolveAsUShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_FLOAT:
                convolveAsFloatData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_DOUBLE:
                convolveAsDoubleData(srcAcc, destAcc);
                break;
        }

        if (destAcc.isDataCopy()) {
            destAcc.clampDataArrays();
            destAcc.copyDataToRaster();
        }
    }

    private void convolveAsByteData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        byte srcData[][] = srcAcc.getByteDataArrays();
        byte destData[][] = destAcc.getByteDataArrays();
        
        for (int k = 0; k < destBands; k++) {
            int destY = destAcc.getY();
            byte destBandData[] = destData[k];
            byte srcBandDat[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    int val = 0;
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        float f = 0.5F;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    f += ((int) srcBandDat[imageOffset] & 0xff) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        val = (int) f;
                        if (val < 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }
                    }
                    destBandData[dstPixelOffset] = (byte) val;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void convolveAsShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        short destData[][] = destAcc.getShortDataArrays();
        short srcData[][] = srcAcc.getShortDataArrays();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            short destBand[] = destData[k];
            short srcBand[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    int val = 0;
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        float f = 0.5F;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    f += srcBand[imageOffset] * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        val = (int) f;
                        if (val < Short.MIN_VALUE) {
                            val = Short.MIN_VALUE;
                        } else if (val > Short.MAX_VALUE) {
                            val = Short.MAX_VALUE;
                        }
                    }
                    destBand[dstPixelOffset] = (short) val;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void convolveAsUShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        short destData[][] = destAcc.getShortDataArrays();
        short srcData[][] = srcAcc.getShortDataArrays();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            short destBand[] = destData[k];
            short srcBand[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    int val = 0;
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        float f = 0.5F;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    f += (srcBand[imageOffset] & 0xffff) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                        val = (int) f;
                        if (val < 0) {
                            val = 0;
                        } else if (val > 0xffff) {
                            val = 0xffff;
                        }
                    }
                    destBand[dstPixelOffset] = (short) val;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void convolveAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        int destData[][] = destAcc.getIntDataArrays();
        int srcData[][] = srcAcc.getIntDataArrays();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            int destBand[] = destData[k];
            int srcBand[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    float f = 0.5F;
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                    }
                    destBand[dstPixelOffset] = (int) f;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void convolveAsFloatData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        float destData[][] = destAcc.getFloatDataArrays();
        float srcData[][] = srcAcc.getFloatDataArrays();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            float destBand[] = destData[k];
            float srcBand[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    float f = 0.0F;
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                    }
                    destBand[dstPixelOffset] = f;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void convolveAsDoubleData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        double destData[][] = destAcc.getDoubleDataArrays();
        double srcData[][] = srcAcc.getDoubleDataArrays();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            double destBand[] = destData[k];
            double srcBand[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    double f = 0.0D;
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                    }
                    destBand[dstPixelOffset] = f;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                dstScanlineOffset += dstScanlineStride;
                srcScanlineOffset += srcScanlineStride;
            }
        }
    }    
}

