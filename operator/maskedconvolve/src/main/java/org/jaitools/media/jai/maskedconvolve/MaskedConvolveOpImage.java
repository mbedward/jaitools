/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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
package org.jaitools.media.jai.maskedconvolve;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.lang.reflect.Constructor;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

import org.jaitools.CollectionFactory;
import org.jaitools.numeric.CompareOp;
import org.jaitools.numeric.Range;

/**
 * An operator to perform masked convolution on a source image.
 * @see MaskedConvolveDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 * @since 1.0
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
    private final float[] kernelData;
    private final int kernelW, kernelH;
    private final int kernelKeyX, kernelKeyY;

    /* Records which kernel cells have non-zero values */
    private final boolean[] kernelActive;
    /**
     * The tolerance used when marking kernel cells as active
     * (ie. having a non-zero value that contributes to the
     * convolution result) or inactive. Cells with an absolute
     * value less than this tolerance will be treated as
     * inactive (zero).
     */
    public final static float KERNEL_TOL = 1.0e-6F;

    /* ROI and options */
    private final ROI roi;
    private final boolean maskSrc;
    private final boolean maskDest;

    /* Flags whether NO_DATA values are defined */
    private final boolean noDataDefined;
    /* Whether any NO_DATA values in a pixel neighbourhood prevent convolution */
    private final boolean strictNodata;
    /* List of Numbers to treat as NO_DATA */
    private final List<Double> noDataNumbers;
    /* List of Ranges to treat as NO_DATA */
    private final List<Range<Double>> noDataRanges;
    /*
     * The value to write to the destination when there is no
     * convolution result
     */
    private final Number nilValueNumber;

    /*
     * The minimum number of non-zero kernel cells that must overlap
     * unmasked source image cells for a convolution result to be written
     * to the destination image
     */
    private final int minKernelCells;

    /**
     * Creates a new instance.
     * 
     * @param source the source image to convolve
     * 
     * @param extender an optional {@code BorderExtender}, or {@code null}
     * 
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * 
     * @param layout an optional {@code ImageLayout} specifying destination image
     *     parameters, or {@code null}
     * 
     * @param kernel the convolution kernel
     * 
     * @param roi the ROI used to control masking; must contain the source image bounds
     * 
     * @param maskSrc if true, exclude masked pixels ({@code roi.contains == false}) from
     *    convolution kernel calculation
     * 
     * @param maskDest if true, do not place kernel over masked pixels (dest will be 0)
     * 
     * @param nilValue value to write to the destination image for pixels where
     *     there is no convolution result
     *
     * @param minCells the minimum number of non-zero kernel cells that be positioned over
     *     unmasked source image cells for convolution to be performed for the target cell
     * 
     * @param noDataValues option {@code Collection} of values and/or {@code Ranges} to
     *     treat as NO_DATA
     * 
     * @param strictNodata if {@code true} no convolution is performed for pixels with any
     *     NO_DATA values in their neighbourhood
     * 
     * @throws IllegalArgumentException if the roi's bounds do not contain the entire
     * source image
     */
    public MaskedConvolveOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            KernelJAI kernel,
            ROI roi,
            Boolean maskSrc,
            Boolean maskDest,
            Number nilValue,
            int minCells,
            Collection<Object> noDataValues,
            Boolean strictNodata) {

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
        this.maskSrc = maskSrc.booleanValue();
        this.maskDest = maskDest.booleanValue();

        /*
         * We defensivley copy the Number object, but have to
         * resort to reflection to do so :-(
         */
        try {
            Constructor<? extends Number> ctor = nilValue.getClass().getConstructor(String.class);
            this.nilValueNumber = ctor.newInstance(nilValue.toString());
        } catch (Exception ex) {
            throw new IllegalStateException("Problem copying nilValue arg", ex);
        }

        kernelData = kernel.getKernelData();
        kernelActive = new boolean[kernelData.length];
        for (int i = 0; i < kernelData.length; i++) {
            if (Math.abs(kernelData[i]) > KERNEL_TOL) {
                kernelActive[i] = true;
            } else {
                kernelActive[i] = false;
            }
        }

        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();

        minKernelCells = minCells;

        if (noDataValues != null && !noDataValues.isEmpty()) {
            noDataDefined = true;
            this.strictNodata = strictNodata;
            noDataNumbers = CollectionFactory.list();
            noDataRanges = CollectionFactory.list();

            for (Object oelem : noDataValues) {
                if (oelem instanceof Number) {
                    double dz = ((Number) oelem).doubleValue();
                    this.noDataNumbers.add(dz);

                } else if (oelem instanceof Range) {
                    Range r = (Range) oelem;
                    Double min = r.getMin().doubleValue();
                    Double max = r.getMax().doubleValue();
                    Range<Double> rd = new Range<Double>(
                            min, r.isMinIncluded(), max, r.isMaxIncluded());
                    this.noDataRanges.add(rd);

                } else {
                    // This should have been picked up by validateParameters
                    // method in ContourDescriptor, but just in case...
                    throw new IllegalArgumentException(
                            "only Number and Range elements are permitted in the "
                            + "noDataValues Collection");
                }
            }
        } else {
            noDataDefined = false;
            this.strictNodata = false;
            noDataNumbers = null;
            noDataRanges = null;
        }
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

    /**
     * Tests if a value should be treated as NODATA.
     * Values that are NaN, infinite or equal to Double.MAX_VALUE
     * are always treated as NODATA.
     * 
     * @param value the value to test
     * 
     * @return {@code true} if a NODATA value; {@code false} otherwise
     */
    private boolean isNoData(double value) {
        if (noDataDefined) {
            for (Double d : noDataNumbers) {
                if (CompareOp.aequal(value, d)) {
                    return true;
                }
            }

            for (Range r : noDataRanges) {
                if (r.contains(value)) {
                    return true;
                }
            }
        }

        return false;
    }

    private void convolveAsByteData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        byte srcData[][] = srcAcc.getByteDataArrays();
        byte destData[][] = destAcc.getByteDataArrays();

        int nilValue = nilValueNumber.intValue();

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
                    int val = 0;
                    int count = 0;
                    float convSum = 0.5f;
                    boolean hasResult = true;
                    
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        
                        for (int u = 0; hasResult && u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; hasResult && v < kernelW; v++, srcX++) {
                                if (kernelActive[kernelOffset + v]) {
                                    if (!maskSrc || roi.contains(srcX, srcY)) {
                                        float fval = ((int) srcBandData[imageOffset] & 0xff);
                                        if (isNoData(fval)) {
                                            if (strictNodata) {
                                                // no convolution performed
                                                hasResult = false;
                                            }
                                        } else {
                                            convSum += fval * kernelData[kernelOffset + v];
                                            count++;
                                        }
                                    }
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                        
                    } else { // outside destination mask
                        hasResult = false;
                    }
                    
                    
                    if (hasResult && count >= minKernelCells) {
                        val = (int) convSum;
                    } else {
                        val = nilValue;
                    }

                    if (val < 0) {
                        val = 0;
                    } else if (val > 255) {
                        val = 255;
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

        int nilValue = nilValueNumber.intValue();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            short destBandData[] = destData[k];
            short srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    int val = 0;
                    int count = 0;
                    float convSum = 0.5F;
                    boolean hasResult = true;
                    
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        
                        for (int u = 0; hasResult && u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; hasResult && v < kernelW; v++, srcX++) {
                                if (kernelActive[kernelOffset + v]) {
                                    if (!maskSrc || roi.contains(srcX, srcY)) {
                                        float fval = srcBandData[imageOffset];
                                        if (isNoData(fval)) {
                                            if (strictNodata) {
                                                // no convolution performed
                                                hasResult = false;
                                            }
                                        } else {
                                            convSum += fval * kernelData[kernelOffset + v];
                                            count++;
                                        }
                                    }
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                    } else { // outside destination mask
                        hasResult = false;
                    }

                    if (hasResult && count >= minKernelCells) {
                        val = (int) convSum;
                    } else {
                        val = nilValue;
                    }

                    if (val < Short.MIN_VALUE) {
                        val = Short.MIN_VALUE;
                    } else if (val > Short.MAX_VALUE) {
                        val = Short.MAX_VALUE;
                    }

                    destBandData[dstPixelOffset] = (short) val;
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

        int nilValue = nilValueNumber.intValue();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            short destBandData[] = destData[k];
            short srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    int val = 0;
                    int count = 0;
                    float convSum = 0.5F;
                    boolean hasResult = true;
                    
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        
                        for (int u = 0; hasResult && u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; hasResult && v < kernelW; v++, srcX++) {
                                if (kernelActive[kernelOffset + v]) {
                                    if (!maskSrc || roi.contains(srcX, srcY)) {
                                        float fval = (srcBandData[imageOffset] & 0xffff);
                                        if (isNoData(fval)) {
                                            if (strictNodata) {
                                                // no convolution performed
                                                hasResult = false;
                                            }
                                        } else {
                                            convSum += fval * kernelData[kernelOffset + v];
                                            count++;
                                        }
                                    }
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                    } else { // outside destination mask
                        hasResult = false;
                    }

                    if (hasResult && count >= minKernelCells) {
                        val = (int) convSum;
                    } else {
                        val = nilValue;
                    }

                    if (val < 0) {
                        val = 0;
                    } else if (val > 0xffff) {
                        val = 0xffff;
                    }

                    destBandData[dstPixelOffset] = (short) val;
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

        int nilValue = nilValueNumber.intValue();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            int destBandData[] = destData[k];
            int srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    float convSum = 0.5F;
                    int count = 0;
                    boolean hasResult = true;

                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        
                        for (int u = 0; hasResult && u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; hasResult && v < kernelW; v++, srcX++) {
                                if (kernelActive[kernelOffset + v]) {
                                    if (!maskSrc || roi.contains(srcX, srcY)) {
                                        float fval = (srcBandData[imageOffset]);
                                        if (isNoData(fval)) {
                                            if (strictNodata) {
                                                // no convolution performed
                                                hasResult = false;
                                            }
                                        } else {
                                            convSum += fval * kernelData[kernelOffset + v];
                                            count++;
                                        }
                                    }
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                    } else { // outside destination mask
                        hasResult = false;
                    }

                    if (hasResult && count >= minKernelCells) {
                        destBandData[dstPixelOffset] = (int) convSum;
                    } else {
                        destBandData[dstPixelOffset] = nilValue;
                    }

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

        float nilValue = nilValueNumber.floatValue();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            float destBandData[] = destData[k];
            float srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    float convSum = 0.0F;
                    int count = 0;
                    boolean hasResult = true;
                    
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        
                        for (int u = 0; hasResult && u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; hasResult && v < kernelW; v++, srcX++) {
                                if (kernelActive[kernelOffset + v]) {
                                    if (!maskSrc || roi.contains(srcX, srcY)) {
                                        float fval = (srcBandData[imageOffset]);
                                        if (isNoData(fval)) {
                                            if (strictNodata) {
                                                // no convolution performed
                                                hasResult = false;
                                            }
                                        } else {
                                            convSum += fval * kernelData[kernelOffset + v];
                                            count++;
                                        }
                                    }
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                        
                    } else { // outside destination mask
                        hasResult = false;
                    }

                    if (hasResult && count >= minKernelCells) {
                        destBandData[dstPixelOffset] = convSum;
                    } else {
                        destBandData[dstPixelOffset] = nilValue;
                    }

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

        double nilValue = nilValueNumber.doubleValue();

        for (int k = 0; k < destBands; k++) {
            int y = destAcc.getY();
            double destBandData[] = destData[k];
            double srcBandData[] = srcData[k];
            int srcScanlineOffset = srcBandOffsets[k];
            int dstScanlineOffset = dstBandOffsets[k];
            for (int j = 0; j < destHeight; j++, y++) {
                int x = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, x++) {
                    double convSum = 0.0D;
                    int count = 0;
                    boolean hasResult = true;
                    
                    if (!maskDest || roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; hasResult && u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; hasResult && v < kernelW; v++, srcX++) {
                                if (kernelActive[kernelOffset + v]) {
                                    if (!maskSrc || roi.contains(srcX, srcY)) {
                                        double dval = (srcBandData[imageOffset]);
                                        if (isNoData(dval)) {
                                            if (strictNodata) {
                                                // no convolution performed
                                                hasResult = false;
                                            }
                                        } else {
                                            convSum += dval * kernelData[kernelOffset + v];
                                            count++;
                                        }
                                    }
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }
                        
                    } else { // outside destination mask
                        hasResult = false;
                    }

                    if (hasResult && count >= minKernelCells) {
                        destBandData[dstPixelOffset] = convSum;
                    } else {
                        destBandData[dstPixelOffset] = nilValue;
                    }

                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                dstScanlineOffset += dstScanlineStride;
                srcScanlineOffset += srcScanlineStride;
            }
        }
    }
}
