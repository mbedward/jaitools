/*
 * Copyright 2011 Michael Bedward
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

package jaitools.media.jai.vmcafill;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterAccessor;
import javax.media.jai.RasterFormatTag;

import jaitools.numeric.CompareOp;


/**
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class VMCAFillOpImage extends AreaOpImage {

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

    /* Records which kernel cells have non-zero values */
    private boolean[] kernelActive;

    /* ROI and options */
    private ROI roi;

    private final ArrayList<Number> gapValues;

    public VMCAFillOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            KernelJAI kernel,
            ROI roi,
            Collection<Number> gapValues) {
        
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
            throw new IllegalArgumentException("The bounds of the ROI must overlap the source image");
        }

        this.roi = roi;

        kernelData = kernel.getKernelData();
        kernelActive = new boolean[kernelData.length];
        for (int i = 0; i < kernelData.length; i++) {
            kernelActive[i] = CompareOp.isZero(kernelData[i]);
        }

        kernelW = kernel.getWidth();
        kernelH = kernel.getHeight();
        kernelKeyX = kernel.getXOrigin();
        kernelKeyY = kernel.getYOrigin();

        this.gapValues = new ArrayList<Number>();
        this.gapValues.addAll(gapValues);
    }

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
                processAsByteData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_INT:
                processAsIntData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_SHORT:
                processAsShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_USHORT:
                processAsUShortData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_FLOAT:
                processAsFloatData(srcAcc, destAcc);
                break;
            case DataBuffer.TYPE_DOUBLE:
                processAsDoubleData(srcAcc, destAcc);
                break;
        }

        if (destAcc.isDataCopy()) {
            destAcc.clampDataArrays();
            destAcc.copyDataToRaster();
        }
    }

    private void processAsByteData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }

    private void processAsShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }

    private void processAsUShortData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }

    private void processAsIntData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }

    private void processAsFloatData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }

    private void processAsDoubleData(RasterAccessor srcAcc, RasterAccessor destAcc) {

    }    
}

