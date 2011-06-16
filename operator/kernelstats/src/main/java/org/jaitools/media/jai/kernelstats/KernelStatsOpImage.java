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

package org.jaitools.media.jai.kernelstats;

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

import org.jaitools.numeric.SampleStats;
import org.jaitools.numeric.Statistic;


/**
 * An operator to calculate neighbourhood statistics on a source image.
 *
 * @see KernelStatsDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class KernelStatsOpImage extends AreaOpImage {

    private int[] srcBandOffsets;
    private int srcPixelStride;
    private int srcScanlineStride;

    /* Destination image variables */
    private int destWidth;
    private int destHeight;
    private int destBands;
    private int[] destBandOffsets;
    private int destPixelStride;
    private int destScanlineStride;

    private int srcBand;

    /* Kernel variables. */
    private boolean[] inKernel;
    private int kernelN;
    private int kernelW;
    private int kernelH;
    private int kernelKeyX;
    private int kernelKeyY;

    /* Mask variables */
    private ROI roi;
    private boolean maskSrc;
    private boolean maskDest;

    private Statistic[] stats;
    private Double[] sampleData;
    private Calculator functionTable;
    private Number nilValue;


    /**
     * Constructor
     * @param source a RenderedImage.
     * @param extender a BorderExtender, or null.
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an optional ImageLayout object; if the layout specifies a SampleModel
     * and / or ColorModel that are not valid for the requested statistics (e.g. wrong number
     * of bands) these will be overridden.
     * @param band the source image band to process
     * @param kernel the convolution kernel
     * @param stats an array of Statistic constants naming the statistics required
     * @throws IllegalArgumentException if the roi's bounds do not contain the entire
     * source image
     * @see KernelStatsDescriptor
     * @see Statistic
     */
    public KernelStatsOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            Statistic[] stats,
            KernelJAI kernel,
            int band,
            ROI roi,
            boolean maskSrc,
            boolean maskDest,
            boolean ignoreNaN,
            Number nilValue) {

        super(source,
              layout,
              config,
              true,
              extender,
              kernel.getLeftPadding(),
              kernel.getRightPadding(),
              kernel.getTopPadding(),
              kernel.getBottomPadding());

        this.srcBand = band;

        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();

        /*
         * Convert the kernel data to boolean values such
         * that all non-zero values -> true; all zero
         * values -> false
         */
        final float FTOL = 1.0e-8f;
        inKernel = new boolean[kernelW * kernelH];
        float[] data = kernel.getKernelData();

        kernelN = 0;
        for (int i = 0; i < inKernel.length; i++) {
            if (Math.abs(data[i]) > FTOL) {
                inKernel[i] = true;
                kernelN++ ;
            } else {
                inKernel[i] = false;
            }
        }

        this.stats = stats;

        this.roi = roi;
        if (roi == null) {
            this.maskSrc = this.maskDest = false;

        } else {
            // check that the ROI contains the source image bounds
            Rectangle sourceBounds = new Rectangle(
                    source.getMinX(), source.getMinY(), source.getWidth(), source.getHeight());

            if (!roi.getBounds().contains(sourceBounds)) {
                throw new IllegalArgumentException("The bounds of the ROI must contain the source image");
            }

            this.maskSrc = maskSrc;
            this.maskDest = maskDest;
        }

        this.functionTable = new Calculator(ignoreNaN);

        this.nilValue = nilValue;

        this.sampleData = new Double[kernelN];
    }

    /**
     * Calculates neighbourhood statistics for a specified rectangle
     *
     * @param sources source rasters (only sources[0] is used here)
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

        destWidth = destAcc.getWidth();
        destHeight = destAcc.getHeight();
        destBands = destAcc.getNumBands();

        destBandOffsets = destAcc.getBandOffsets();
        destPixelStride = destAcc.getPixelStride();
        destScanlineStride = destAcc.getScanlineStride();

        srcBandOffsets = srcAcc.getBandOffsets();
        srcPixelStride = srcAcc.getPixelStride();
        srcScanlineStride = srcAcc.getScanlineStride();

        switch (destAcc.getDataType()) {
            case DataBuffer.TYPE_BYTE:
                calcByteData(srcAcc, destAcc);
                break;

            case DataBuffer.TYPE_SHORT:
                calcShortData(srcAcc, destAcc);
                break;

            case DataBuffer.TYPE_USHORT:
                calcUShortData(srcAcc, destAcc);
                break;

            case DataBuffer.TYPE_INT:
                calcIntData(srcAcc, destAcc);
                break;

            case DataBuffer.TYPE_FLOAT:
                calcFloatData(srcAcc, destAcc);
                break;

            case DataBuffer.TYPE_DOUBLE:
                calcDoubleData(srcAcc, destAcc);
                break;
        }

        if (destAcc.isDataCopy()) {
            destAcc.clampDataArrays();
            destAcc.copyDataToRaster();
        }
    }

    private void calcByteData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        byte srcData[][] = srcAcc.getByteDataArrays();
        byte destData[][] = destAcc.getByteDataArrays();

        int destY = destAcc.getY();
        int destX = destAcc.getX();

        byte srcBandData[] = srcData[srcBand];
        int srcScanlineOffset = srcBandOffsets[srcBand];

        int destLineDelta = 0;
        for (int j = 0; j < destHeight; j++, destY++) {
            int srcPixelOffset = srcScanlineOffset;

            int destPixelDelta = 0;
            for (int i = 0; i < destWidth; i++, destX++) {
                int numSamples = 0;
                if (!maskDest || roi.contains(destX, destY)) {
                    int srcY = destY - kernelKeyY;
                    int kernelVerticalOffset = 0;
                    int imageVerticalOffset = srcPixelOffset;

                    for (int u = 0; u < kernelH; u++, srcY++) {
                        int srcX = destX - kernelKeyX;
                        int imageOffset = imageVerticalOffset;

                        for (int v = 0; v < kernelW; v++, srcX++) {
                            if (!maskSrc || roi.contains(srcX, srcY)) {
                                if (inKernel[kernelVerticalOffset + v]) {
                                    sampleData[numSamples++] = (double) (srcBandData[imageOffset] & 0xff);
                                }
                            }
                            imageOffset += srcPixelStride;
                        }
                        kernelVerticalOffset += kernelW;
                        imageVerticalOffset += srcScanlineStride;
                    }
                }

                for (int band = 0; band < destBands; band++) {
                    byte destBandData[] = destData[band];
                    int dstPixelOffset = destBandOffsets[band] + destPixelDelta + destLineDelta;

                    int val = nilValue.byteValue();
                    if (numSamples > 0) {
                        double statValue = functionTable.call(stats[band], sampleData, numSamples);
                        if (!Double.isNaN(statValue)) {
                            val = (int) (statValue + 0.5);
                            if (val < 0) {
                                val = 0;
                            } else if (val > 255) {
                                val = 255;
                            }
                        }
                    }

                    destBandData[dstPixelOffset] = (byte) val;
                }

                srcPixelOffset += srcPixelStride;
                destPixelDelta += destPixelStride;
            }

            srcScanlineOffset += srcScanlineStride;
            destLineDelta += destScanlineStride;
        }
    }

    private void calcShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        short destData[][] = destAcc.getShortDataArrays();
        short srcData[][] = srcAcc.getShortDataArrays();

        int destY = destAcc.getY();
        int destX = destAcc.getX();

        short srcBandData[] = srcData[srcBand];
        int srcScanlineOffset = srcBandOffsets[srcBand];

        int destLineDelta = 0;
        for (int j = 0; j < destHeight; j++, destY++) {
            int srcPixelOffset = srcScanlineOffset;

            int destPixelDelta = 0;
            for (int i = 0; i < destWidth; i++, destX++) {
                int numSamples = 0;
                if (!maskDest || roi.contains(destX, destY)) {
                    int srcY = destY - kernelKeyY;
                    int kernelVerticalOffset = 0;
                    int imageVerticalOffset = srcPixelOffset;

                    for (int u = 0; u < kernelH; u++, srcY++) {
                        int srcX = destX - kernelKeyX;
                        int imageOffset = imageVerticalOffset;

                        for (int v = 0; v < kernelW; v++, srcX++) {
                            if (!maskSrc || roi.contains(srcX, srcY)) {
                                if (inKernel[kernelVerticalOffset + v]) {
                                    sampleData[numSamples++] = (double) srcBandData[imageOffset];
                                }
                            }
                            imageOffset += srcPixelStride;
                        }
                        kernelVerticalOffset += kernelW;
                        imageVerticalOffset += srcScanlineStride;
                    }
                }

                for (int band = 0; band < destBands; band++) {
                    short destBandData[] = destData[band];
                    int dstPixelOffset = destBandOffsets[band] + destPixelDelta + destLineDelta;

                    int val = nilValue.shortValue();
                    if (numSamples > 0) {
                        double statValue = functionTable.call(stats[band], sampleData, numSamples);
                        if (!Double.isNaN(statValue)) {
                            val = (int) (statValue + 0.5);
                            if (val < Short.MIN_VALUE) {
                                val = Short.MIN_VALUE;
                            } else if (val > Short.MAX_VALUE) {
                                val = Short.MAX_VALUE;
                            }
                        }
                    }

                    destBandData[dstPixelOffset] = (short) val;
                }

                srcPixelOffset += srcPixelStride;
                destPixelDelta += destPixelStride;
            }

            srcScanlineOffset += srcScanlineStride;
            destLineDelta += destScanlineStride;
        }
    }

    private void calcUShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        short destData[][] = destAcc.getShortDataArrays();
        short srcData[][] = srcAcc.getShortDataArrays();

        int destY = destAcc.getY();
        int destX = destAcc.getX();

        short srcBandData[] = srcData[srcBand];
        int srcScanlineOffset = srcBandOffsets[srcBand];

        int destLineDelta = 0;
        for (int j = 0; j < destHeight; j++, destY++) {
            int srcPixelOffset = srcScanlineOffset;

            int destPixelDelta = 0;
            for (int i = 0; i < destWidth; i++, destX++) {
                int numSamples = 0;
                if (!maskDest || roi.contains(destX, destY)) {
                    int srcY = destY - kernelKeyY;
                    int kernelVerticalOffset = 0;
                    int imageVerticalOffset = srcPixelOffset;

                    for (int u = 0; u < kernelH; u++, srcY++) {
                        int srcX = destX - kernelKeyX;
                        int imageOffset = imageVerticalOffset;

                        for (int v = 0; v < kernelW; v++, srcX++) {
                            if (!maskSrc || roi.contains(srcX, srcY)) {
                                if (inKernel[kernelVerticalOffset + v]) {
                                    sampleData[numSamples++] = (double) (srcBandData[imageOffset] & 0xffff);
                                }
                            }
                            imageOffset += srcPixelStride;
                        }
                        kernelVerticalOffset += kernelW;
                        imageVerticalOffset += srcScanlineStride;
                    }
                }

                for (int band = 0; band < destBands; band++) {
                    short destBandData[] = destData[band];
                    int dstPixelOffset = destBandOffsets[band] + destPixelDelta + destLineDelta;

                    int val = nilValue.shortValue();
                    if (numSamples > 0) {
                        double statValue = functionTable.call(stats[band], sampleData, numSamples);
                        if (!Double.isNaN(statValue)) {
                            val = (int) (statValue + 0.5);
                            if (val < 0) {
                                val = 0;
                            } else if (val > 0xffff) {
                                val = 0xffff;
                            }
                        }
                    }

                    destBandData[dstPixelOffset] = (short) val;
                }

                srcPixelOffset += srcPixelStride;
                destPixelDelta += destPixelStride;
            }

            srcScanlineOffset += srcScanlineStride;
            destLineDelta += destScanlineStride;
        }
    }

    private void calcIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        int destData[][] = destAcc.getIntDataArrays();
        int srcData[][] = srcAcc.getIntDataArrays();
        int destY = destAcc.getY();
        int destX = destAcc.getX();

        int srcBandData[] = srcData[srcBand];
        int srcScanlineOffset = srcBandOffsets[srcBand];

        int destLineDelta = 0;
        for (int j = 0; j < destHeight; j++, destY++) {
            int srcPixelOffset = srcScanlineOffset;

            int destPixelDelta = 0;
            for (int i = 0; i < destWidth; i++, destX++) {
                int numSamples = 0;
                if (!maskDest || roi.contains(destX, destY)) {
                    int srcY = destY - kernelKeyY;
                    int kernelVerticalOffset = 0;
                    int imageVerticalOffset = srcPixelOffset;

                    for (int u = 0; u < kernelH; u++, srcY++) {
                        int srcX = destX - kernelKeyX;
                        int imageOffset = imageVerticalOffset;

                        for (int v = 0; v < kernelW; v++, srcX++) {
                            if (!maskSrc || roi.contains(srcX, srcY)) {
                                if (inKernel[kernelVerticalOffset + v]) {
                                    sampleData[numSamples++] = (double) srcBandData[imageOffset];
                                }
                            }
                            imageOffset += srcPixelStride;
                        }
                        kernelVerticalOffset += kernelW;
                        imageVerticalOffset += srcScanlineStride;
                    }
                }

                for (int band = 0; band < destBands; band++) {
                    int destBandData[] = destData[band];
                    int dstPixelOffset = destBandOffsets[band] + destPixelDelta + destLineDelta;

                    int val = nilValue.intValue();
                    if (numSamples > 0) {
                        double statValue = functionTable.call(stats[band], sampleData, numSamples);
                        if (!Double.isNaN(statValue)) {
                            val = (int) (statValue + 0.5);
                        }
                    }

                    destBandData[dstPixelOffset] = val;
                }

                srcPixelOffset += srcPixelStride;
                destPixelDelta += destPixelStride;
            }

            srcScanlineOffset += srcScanlineStride;
            destLineDelta += destScanlineStride;
        }
    }

    private void calcFloatData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        float destData[][] = destAcc.getFloatDataArrays();
        float srcData[][] = srcAcc.getFloatDataArrays();
        int destY = destAcc.getY();
        int destX = destAcc.getX();

        float srcBandData[] = srcData[srcBand];
        int srcScanlineOffset = srcBandOffsets[srcBand];

        int destLineDelta = 0;
        for (int j = 0; j < destHeight; j++, destY++) {
            int srcPixelOffset = srcScanlineOffset;

            int destPixelDelta = 0;
            for (int i = 0; i < destWidth; i++, destX++) {
                int numSamples = 0;
                if (!maskDest || roi.contains(destX, destY)) {
                    int srcY = destY - kernelKeyY;
                    int kernelVerticalOffset = 0;
                    int imageVerticalOffset = srcPixelOffset;

                    for (int u = 0; u < kernelH; u++, srcY++) {
                        int srcX = destX - kernelKeyX;
                        int imageOffset = imageVerticalOffset;

                        for (int v = 0; v < kernelW; v++, srcX++) {
                            if (!maskSrc || roi.contains(srcX, srcY)) {
                                if (inKernel[kernelVerticalOffset + v]) {
                                    sampleData[numSamples++] = (double) srcBandData[imageOffset];
                                }
                            }
                            imageOffset += srcPixelStride;
                        }
                        kernelVerticalOffset += kernelW;
                        imageVerticalOffset += srcScanlineStride;
                    }
                }

                for (int band = 0; band < destBands; band++) {
                    float destBandData[] = destData[band];
                    int dstPixelOffset = destBandOffsets[band] + destPixelDelta + destLineDelta;

                    float val = nilValue.floatValue();
                    if (numSamples > 0) {
                        double statValue = functionTable.call(stats[band], sampleData, numSamples);
                        if (!Double.isNaN(statValue)) {
                            val = (float) statValue;
                        }
                    }

                    destBandData[dstPixelOffset] = val;
                }

                srcPixelOffset += srcPixelStride;
                destPixelDelta += destPixelStride;
            }

            srcScanlineOffset += srcScanlineStride;
            destLineDelta += destScanlineStride;
        }
    }

    private void calcDoubleData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        double destData[][] = destAcc.getDoubleDataArrays();
        double srcData[][] = srcAcc.getDoubleDataArrays();
        int destY = destAcc.getY();
        int destX = destAcc.getX();

        double srcBandData[] = srcData[srcBand];
        int srcScanlineOffset = srcBandOffsets[srcBand];

        int destLineDelta = 0;
        for (int j = 0; j < destHeight; j++, destY++) {
            int srcPixelOffset = srcScanlineOffset;

            int destPixelDelta = 0;
            for (int i = 0; i < destWidth; i++, destX++) {
                int numSamples = 0;
                if (!maskDest || roi.contains(destX, destY)) {
                    int srcY = destY - kernelKeyY;
                    int kernelVerticalOffset = 0;
                    int imageVerticalOffset = srcPixelOffset;

                    for (int u = 0; u < kernelH; u++, srcY++) {
                        int srcX = destX - kernelKeyX;
                        int imageOffset = imageVerticalOffset;

                        for (int v = 0; v < kernelW; v++, srcX++) {
                            if (!maskSrc || roi.contains(srcX, srcY)) {
                                if (inKernel[kernelVerticalOffset + v]) {
                                    sampleData[numSamples++] = srcBandData[imageOffset];
                                }
                            }
                            imageOffset += srcPixelStride;
                        }
                        kernelVerticalOffset += kernelW;
                        imageVerticalOffset += srcScanlineStride;
                    }
                }

                for (int band = 0; band < destBands; band++) {
                    double destBandData[] = destData[band];
                    int dstPixelOffset = destBandOffsets[band] + destPixelDelta + destLineDelta;

                    double val = nilValue.doubleValue();
                    if (numSamples > 0) {
                        double statValue = functionTable.call(stats[band], sampleData, numSamples);
                        if (!Double.isNaN(statValue)) {
                            val = statValue;
                        }
                    }

                    destBandData[dstPixelOffset] = val;
                }

                srcPixelOffset += srcPixelStride;
                destPixelDelta += destPixelStride;
            }

            srcScanlineOffset += srcScanlineStride;
            destLineDelta += destScanlineStride;
        }
    }


    /**
     * This class handles preparation of sample data, passing calculation tasks
     * to {@linkplain jaitools.utils.SampleStats} methods, and returning results
     */
    private static class Calculator {

        private boolean ignoreNaN;

        /**
         * Constructor
         * @param ignoreNaN specifies how to respond to NaN values
         */
        Calculator(boolean ignoreNaN) {
            this.ignoreNaN = ignoreNaN;
        }

        /**
         * Calculate the specified statistic on sample data
         * @param stat the {@linkplain Statistic} constant for the desired statistic
         * @param data the sample data
         * @param n number of elements to use from the sample data array
         * @return value of the statistic as a double (may be NaN)
         */
        public double call(Statistic stat, Double[] data, int n) {
            Double[] values = null;
            if (data.length == n) {
                values = data;
            } else {
                values = new Double[n];
                System.arraycopy(data, 0, values, 0, n);
            }

            switch (stat) {
                case MAX:
                    return SampleStats.max(values, ignoreNaN);

                case MEAN:
                    return SampleStats.mean(values, ignoreNaN);

                case MEDIAN:
                    return SampleStats.median(values, ignoreNaN);

                case MIN:
                    return SampleStats.min(values, ignoreNaN);

                case RANGE:
                    return SampleStats.range(values, ignoreNaN);

                case SDEV:
                    return SampleStats.sdev(values, ignoreNaN);
                
                case VARIANCE:
                    return SampleStats.variance(values, ignoreNaN);

                case SUM:
                    return SampleStats.sum(values, ignoreNaN);

                default:
                throw new IllegalArgumentException("Unrecognized KernelStatstic arg");
            }
        }
    }
}

