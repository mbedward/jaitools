/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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

