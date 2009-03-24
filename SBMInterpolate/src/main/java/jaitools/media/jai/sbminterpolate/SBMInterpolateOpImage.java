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

package jaitools.media.jai.sbminterpolate;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.LinkedList;
import java.util.Map;
import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

/**
 * THIS IS NOT READY FOR USE YET !!!
 *
 * An operator to fill areas of missing data in a source image using
 * the fractal interpolation method described in:
 * <blockquote>
 * J. C. Sprott, J. Bolliger, and D. J. Mladenoff (2002)<br>
 * Self-organized Criticality in Forest-landscape Evolution<br>
 * Phys. Lett. A 297, 267-271
 * </blockquote>
 * and
 * <blockquote>
 * J. C. Sprott (2004)<br>
 * A Method For Approximating Missing Data in Spatial Patterns<br>
 * Comput. & Graphics 28, 113-117
 * </blockquote>
 *
 * @see SBMInterpolateDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 */
final class SBMInterpolateOpImage extends AreaOpImage {

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
    private KernelJAI kernel;
    private float[] kernelData;
    private int kw,  kh;
    
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
     * @param roi the ROI used to control masking; must contain the source image bounds
     * @param kernel the convolution kernel
     *
     * @throws IllegalArgumentException if the roi's bounds do not intersect with
     * those of the source image
     *
     * @see SBMInterpolateDescriptor
     */
    public SBMInterpolateOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            ROI roi,
            KernelJAI kernel) {
        
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
        
        if (!roi.getBounds().intersects(sourceBounds)) {
            throw new IllegalArgumentException("The bounds of the ROI must intersect with the source image");
        }

        this.kernel = kernel;
        this.roi = roi;
        kw = kernel.getWidth();
        kh = kernel.getHeight();
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

        LinkedList<Rectangle> roiRects = roi.getAsRectangleList(srcRect.x, srcRect.y, srcRect.width, srcRect.height);
        
        interpolate(srcAcc, destAcc, roiRects);
    }

    /**
     * Initialize common variables then delegate the interpolation to
     * one of the data-type-specific methods
     * 
     * @param srcAcc source raster accessor
     * @param destAcc dest raster accessor
     * @param rects rectangles within which areas of missing data lie
     */
    private void interpolate(RasterAccessor srcAcc, RasterAccessor destAcc, 
            LinkedList<Rectangle> rects) {

        destWidth = destAcc.getWidth();
        destHeight = destAcc.getHeight();
        destBands = destAcc.getNumBands();

        kernelData = kernel.getKernelData();

        dstBandOffsets = destAcc.getBandOffsets();
        dstPixelStride = destAcc.getPixelStride();
        dstScanlineStride = destAcc.getScanlineStride();

        srcBandOffsets = srcAcc.getBandOffsets();
        srcPixelStride = srcAcc.getPixelStride();
        srcScanlineStride = srcAcc.getScanlineStride();

        switch (destAcc.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                interpolateAsByteData(srcAcc, destAcc, rects);
                break;
            case DataBuffer.TYPE_INT:
                interpolateAsIntData(srcAcc, destAcc, rects);
                break;
            case DataBuffer.TYPE_SHORT:
                interpolateAsShortData(srcAcc, destAcc, rects);
                break;
            case DataBuffer.TYPE_USHORT:
                interpolateAsUShortData(srcAcc, destAcc, rects);
                break;
            case DataBuffer.TYPE_FLOAT:
                interpolateAsFloatData(srcAcc, destAcc, rects);
                break;
            case DataBuffer.TYPE_DOUBLE:
                interpolateAsDoubleData(srcAcc, destAcc, rects);
                break;
        }

        if (destAcc.isDataCopy()) {
            destAcc.clampDataArrays();
            destAcc.copyDataToRaster();
        }
    }

    private void interpolateAsByteData(RasterAccessor srcAcc, RasterAccessor destAcc,
            LinkedList<Rectangle> rects) {

        byte srcData[][] = srcAcc.getByteDataArrays();
        byte destData[][] = destAcc.getByteDataArrays();
        
        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            byte destBandData[] = destData[k];
            byte srcBandDat[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    int val = 0;
                    if (!maskDest || roi.contains(x, y)) {
                        int ky = y - kh / 2;
                        float f = 0.5F;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kh; u++, ky++) {
                            int kx = x - kw / 2;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kw; v++, kx++) {
                                if (!maskSrc || roi.contains(kx, ky)) {
                                    f += ((int) srcBandDat[imageOffset] & 0xff) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kw;
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

    private void interpolateAsShortData(RasterAccessor srcAcc, RasterAccessor destAcc,
            LinkedList<Rectangle> rects) {

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
                        int ky = y - kh / 2;
                        float f = 0.5F;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kh; u++, ky++) {
                            int kx = x - kw / 2;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kw; v++, kx++) {
                                if (!maskSrc || roi.contains(kx, ky)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kw;
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

    private void interpolateAsUShortData(RasterAccessor srcAcc, RasterAccessor destAcc,
            LinkedList<Rectangle> rects) {

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
                        int ky = y - kh / 2;
                        float f = 0.5F;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kh; u++, ky++) {
                            int kx = x - kw / 2;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kw; v++, kx++) {
                                if (!maskSrc || roi.contains(kx, ky)) {
                                    f += (srcBand[imageOffset] & 0xffff) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kw;
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

    private void interpolateAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc,
            LinkedList<Rectangle> rects) {

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
                        int ky = y - kh / 2;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kh; u++, ky++) {
                            int kx = x - kw / 2;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kw; v++, kx++) {
                                if (!maskSrc || roi.contains(kx, ky)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kw;
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

    private void interpolateAsFloatData(RasterAccessor srcAcc, RasterAccessor destAcc,
            LinkedList<Rectangle> rects) {

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
                        int ky = y - kh / 2;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kh; u++, ky++) {
                            int kx = x - kw / 2;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kw; v++, kx++) {
                                if (!maskSrc || roi.contains(kx, ky)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kw;
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

    private void interpolateAsDoubleData(RasterAccessor srcAcc, RasterAccessor destAcc,
            LinkedList<Rectangle> rects) {

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
                        int ky = y - kh / 2;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kh; u++, ky++) {
                            int kx = x - kw / 2;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kw; v++, kx++) {
                                if (!maskSrc || roi.contains(kx, ky)) {
                                    f += (srcBand[imageOffset]) * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kw;
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

