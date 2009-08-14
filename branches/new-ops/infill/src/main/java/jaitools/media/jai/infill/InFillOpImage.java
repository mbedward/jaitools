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

package jaitools.media.jai.infill;

import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

/**
 * @author Michael Bedward
 * @since 1.0
 * @sourcecode $URL: https://jai-tools.googlecode.com/svn/branches/new-ops/infill/src/main/java/jaitools/media/jai/infill/InFillOpImage.java $
 * @version $Id: InFillOpImage.java 535M 2009-08-14 01:47:05Z (local) $
 */
class InFillOpImage extends AreaOpImage {

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

    public InFillOpImage(RenderedImage renderedSource,
            BorderExtender extender,
            RenderingHints renderHints,
            ImageLayout layout,
            ROI roi,
            KernelJAI kernel,
            float candidateRadius,
            float subsetProp,
            boolean doWeightedAv) {

        super(renderedSource,
              layout,
              renderHints,
              true,  // cobbleSources
              extender,
              kernel.getLeftPadding(), kernel.getRightPadding(),
              kernel.getTopPadding(), kernel.getBottomPadding());

        this.roi = roi;

        kernelData = kernel.getKernelData();
        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();
    }

    /**
     * Performs the infilling operation for a given portion (rectangle) of
     * the destination image
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

        process(srcAcc, destAcc);
    }

    /**
     * Initialize common variables then delegate the in-filling operation to
     * one of the data-type-specific methods
     *
     * @param srcAcc source raster accessor
     * @param destAcc dest raster accessor
     */
    private void process(RasterAccessor srcAcc, RasterAccessor destAcc) {
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
                // processAsByteData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_INT:
                processAsIntData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_SHORT:
                // processAsShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_USHORT:
                // processAsUShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_FLOAT:
                // processAsFloatData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_DOUBLE:
                // processAsDoubleData(srcAcc, destAcc);
                break;
        }

        if (destAcc.isDataCopy()) {
            destAcc.clampDataArrays();
            destAcc.copyDataToRaster();
        }
    }

    private void processAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }

    private void fooAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {
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
                    if (roi.contains(x, y)) {
                        int srcY = y - kernelKeyY;
                        int kernelVerticalOffset = 0;
                        int imageVerticalOffset = srcPixelOffset;
                        for (int u = 0; u < kernelH; u++, srcY++) {
                            int srcX = x - kernelKeyX;
                            int imageOffset = imageVerticalOffset;
                            for (int v = 0; v < kernelW; v++, srcX++) {
                                if (roi.contains(srcX, srcY)) {
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

}
