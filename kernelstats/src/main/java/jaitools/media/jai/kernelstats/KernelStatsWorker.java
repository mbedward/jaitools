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
package jaitools.media.jai.kernelstats;

import jaitools.utils.SummaryStats;
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
 * A helper class for {@linkplain KernelStatsOpImage} that calculates
 * a single output image.
 *
 * @author Michael Bedward
 */
class KernelStatsWorker extends AreaOpImage {
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
    private int kernelW;
    private int kernelH;
    private int kernelKeyX;
    private int kernelKeyY;

    /* Mask variables */
    private ROI roi;
    private boolean maskSrc;
    private boolean maskDest;
    private KernelStatistic stat;
    private Double[] sampleData;
    private Calculator functionTable;

    /**
     * Constructor
     *
     * @param source a RenderedImage.
     * @param extender a BorderExtender, or null.
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *        SampleModel, and ColorModel, or null.
     * @param kernel the convolution kernel
     * @param stat the statistic to calculate

     * @see KernelStatsDescriptor
     */
    KernelStatsWorker(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            KernelJAI kernel,
            KernelStatistic stat,
            ROI roi,
            boolean maskSrc,
            boolean maskDest,
            boolean ignoreNaN) {

        super(source,
                layout,
                config,
                true,
                extender,
                kernel.getLeftPadding(),
                kernel.getRightPadding(),
                kernel.getTopPadding(),
                kernel.getBottomPadding());

        this.kernelData = kernel.getKernelData();
        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();

        this.sampleData = new Double[kernelData.length];

        this.stat = stat;

        this.roi = roi;
        this.maskSrc = maskSrc;
        this.maskDest = maskDest;

        this.functionTable = new Calculator(ignoreNaN);
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

        calculateStat(srcAcc, destAcc);
    }

    /**
     * Initialize common variables then delegate the convolution to
     * one of the data-type-specific methods
     *
     * @param srcAcc source raster accessor
     * @param destAcc dest raster accessor
     */
    private void calculateStat(RasterAccessor srcAcc, RasterAccessor destAcc) {
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
        int val = 0;

        for (int b = 0; b < destBands; b++) {
            int destY = destAcc.getY();
            byte destBandData[] = destData[b];
            byte srcBandData[] = srcData[b];
            int srcScanlineOffset = srcBandOffsets[b];
            int dstScanlineOffset = dstBandOffsets[b];

            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        int numSamples = 0;

                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;

                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    sampleData[numSamples++] = (double)((srcBandData[imageOffset] & 0xff) * kernelData[kernelVerticalOffset + v]);
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        double statValue = functionTable.call(stat, sampleData, numSamples);
                        if (Double.isNaN(statValue)) {
                            val = 0;
                        } else {
                            val = (int) (statValue + 0.5);
                        }

                        if (val < 0) {
                            val = 0;
                        } else if (val > 255) {
                            val = 255;
                        }

                    } else { // no calculation for this destination pixel
                        val = 0;
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

    private void calcShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        short destData[][] = destAcc.getShortDataArrays();
        short srcData[][] = srcAcc.getShortDataArrays();
        int val = 0;

        for (int b = 0; b < destBands; b++) {
            int destY = destAcc.getY();
            short destBandData[] = destData[b];
            short srcBandData[] = srcData[b];
            int srcScanlineOffset = srcBandOffsets[b];
            int dstScanlineOffset = dstBandOffsets[b];

            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        int numSamples = 0;

                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    sampleData[numSamples++] = (double)(srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v]);
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        double statValue = functionTable.call(stat, sampleData, numSamples);
                        if (Double.isNaN(statValue)) {
                            val = 0;
                        } else {
                            val = (int) (statValue + 0.5);
                        }

                        if (val < Short.MIN_VALUE) {
                            val = Short.MIN_VALUE;
                        } else if (val > Short.MAX_VALUE) {
                            val = Short.MAX_VALUE;
                        }

                    } else {  // no calculation for this destination pixel
                        val = 0;
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

    private void calcUShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        short destData[][] = destAcc.getShortDataArrays();
        short srcData[][] = srcAcc.getShortDataArrays();
        int val = 0;

        for (int b = 0; b < destBands; b++) {
            int destY = destAcc.getY();
            short destBandData[] = destData[b];
            short srcBandData[] = srcData[b];
            int srcScanlineOffset = srcBandOffsets[b];
            int dstScanlineOffset = dstBandOffsets[b];

            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        int numSamples = 0;

                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    sampleData[numSamples++] = (double)((srcBandData[imageOffset] & 0xffff) * kernelData[kernelVerticalOffset + v]);
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        double statValue = functionTable.call(stat, sampleData, numSamples);
                        if (Double.isNaN(statValue)) {
                            val = 0;
                        } else {
                            val = (int) (statValue + 0.5);
                        }

                        if (val < 0) {
                            val = 0;
                        } else if (val > 0xffff) {
                            val = 0xffff;
                        }

                    } else {  // no calculation for this destination pixel
                        val = 0;
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

    private void calcIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        int destData[][] = destAcc.getIntDataArrays();
        int srcData[][] = srcAcc.getIntDataArrays();
        int val = 0;

        for (int b = 0; b < destBands; b++) {
            int destY = destAcc.getY();
            int destBandData[] = destData[b];
            int srcBandData[] = srcData[b];
            int srcScanlineOffset = srcBandOffsets[b];
            int dstScanlineOffset = dstBandOffsets[b];

            for (int j = 0; j < destHeight; j++, destY++) {
                int destX = destAcc.getX();
                int srcPixelOffset = srcScanlineOffset;
                int dstPixelOffset = dstScanlineOffset;

                for (int i = 0; i < destWidth; i++, destX++) {
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        int numSamples = 0;

                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    sampleData[numSamples++] = (double)(srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v]);
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        double statValue = functionTable.call(stat, sampleData, numSamples);
                        if (Double.isNaN(statValue)) {
                            val = 0;
                        } else {
                            val = (int) (statValue + 0.5);
                        }

                    } else {  // no calculation for this destination pixel
                        val = 0;
                    }

                    destBandData[dstPixelOffset] = val;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void calcFloatData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        float destData[][] = destAcc.getFloatDataArrays();
        float srcData[][] = srcAcc.getFloatDataArrays();
        float val = 0f;

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
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        int numSamples = 0;

                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    sampleData[numSamples++] = (double)(srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v]);
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        double statValue = functionTable.call(stat, sampleData, numSamples);
                        if (Double.isNaN(statValue)) {
                            val = Float.NaN;
                        } else {
                            val = (float) statValue;
                        }

                    } else { // no calculation for this destination pixel
                        val = Float.NaN;
                    }

                    destBandData[dstPixelOffset] = val;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                srcScanlineOffset += srcScanlineStride;
                dstScanlineOffset += dstScanlineStride;
            }
        }
    }

    private void calcDoubleData(RasterAccessor srcAcc, RasterAccessor destAcc) {

        double destData[][] = destAcc.getDoubleDataArrays();
        double srcData[][] = srcAcc.getDoubleDataArrays();
        double val;

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
                    if (!maskDest || roi.contains(destX, destY)) {
                        int srcY = destY - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        int numSamples = 0;

                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = destX - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (!maskSrc || roi.contains(srcX, srcY)) {
                                    sampleData[numSamples++] = srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v];
                                }
                                imageOffset += srcPixelStride;
                            }
                            kernelVerticalOffset += kernelW;
                            imageVerticalOffset += srcScanlineStride;
                        }

                        double statValue = functionTable.call(stat, sampleData, numSamples);
                        if (Double.isNaN(statValue)) {
                            val = Double.NaN;
                        } else {
                            val = statValue;
                        }

                    } else { // no calculation for this destination pixel
                        val = Double.NaN;
                    }

                    destBandData[dstPixelOffset] = val;
                    srcPixelOffset += srcPixelStride;
                    dstPixelOffset += dstPixelStride;
                }
                dstScanlineOffset += dstScanlineStride;
                srcScanlineOffset += srcScanlineStride;
            }
        }
    }

    /**
     * This class handles preparation of sample data, passing calculation tasks
     * to {@linkplain jaitools.utils.SummaryStats} methods, and returning results
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
         * @param stat the {@linkplain KernelStatistic} constant for the desired statistic
         * @param data the sample data
         * @param n number of elements to use from the sample data array
         * @return value of the statistic as a double (may be NaN)
         */
        public double call(KernelStatistic stat, Double[] data, int n) {
            Double[] values = null;
            if (data.length == n) {
                values = data;
            } else {
                values = new Double[n];
                System.arraycopy(data, 0, values, 0, n);
            }

            if (stat == KernelStatistic.MAX) {
                return SummaryStats.max(values, ignoreNaN);

            } else if (stat == KernelStatistic.MEAN) {
                return SummaryStats.mean(values, ignoreNaN);

            } else if (stat == KernelStatistic.MEDIAN) {
                return SummaryStats.median(values, ignoreNaN);

            } else if (stat == KernelStatistic.MIN) {
                return SummaryStats.min(values, ignoreNaN);

            } else if (stat == KernelStatistic.RANGE) {
                return SummaryStats.range(values, ignoreNaN);

            } else if (stat == KernelStatistic.SDEV) {
                return SummaryStats.sdev(values, ignoreNaN);

            } else {
                throw new IllegalArgumentException("Unrecognized KernelStatstic arg");
            }
        }
    }
}
