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
 * An operator to calculate neighbourhood statistics on a source image.
 * @see KernelStatsDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 */
final class KernelStatsOpImage extends AreaOpImage {

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
    private float[] kernelData;
    private int kernelW;
    private int kernelH;
    private int kernelKeyX;
    private int kernelKeyY;

    /* Mask variables */
    private ROI roi;
    private boolean maskSrc;
    private boolean maskDest;

    private KernelStatistic[] stats;
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
     * @param stats an array of KernelStatistic constants naming the statistics required
     * @throws IllegalArgumentException if the roi's bounds do not contain the entire
     * source image
     * @see KernelStatsDescriptor
     * @see KernelStatistic
     */
    public KernelStatsOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            KernelStatistic[] stats,
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
        
        this.kernelData = kernel.getKernelData();
        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();

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

        this.sampleData = new Double[kernelData.length];
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
                                sampleData[numSamples++] = (double) ((srcBandData[imageOffset] & 0xff) * kernelData[kernelVerticalOffset + v]);
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
                                sampleData[numSamples++] = (double) (srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v]);
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
                                sampleData[numSamples++] = (double) ((srcBandData[imageOffset] & 0xffff) * kernelData[kernelVerticalOffset + v]);
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
                                sampleData[numSamples++] = (double) (srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v]);
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
                                sampleData[numSamples++] = (double) (srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v]);
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
                                sampleData[numSamples++] = srcBandData[imageOffset] * kernelData[kernelVerticalOffset + v];
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

            switch (stat) {
                case MAX:
                    return SummaryStats.max(values, ignoreNaN);

                case MEAN:
                    return SummaryStats.mean(values, ignoreNaN);

                case MEDIAN:
                    return SummaryStats.median(values, ignoreNaN);

                case MIN:
                    return SummaryStats.min(values, ignoreNaN);

                case RANGE:
                    return SummaryStats.range(values, ignoreNaN);

                case SDEV:
                    return SummaryStats.sdev(values, ignoreNaN);
                
                case VARIANCE:
                    return SummaryStats.variance(values, ignoreNaN);

                default:
                throw new IllegalArgumentException("Unrecognized KernelStatstic arg");
            }
        }
    }
}

