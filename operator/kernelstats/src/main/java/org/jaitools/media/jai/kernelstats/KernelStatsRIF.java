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

package org.jaitools.media.jai.kernelstats;

import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;
import javax.media.jai.RasterFactory;

import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.util.ImageUtil;

import org.jaitools.numeric.Statistic;

/**
 * The image factory for the {@link KernelStatsOpImage} operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class KernelStatsRIF implements RenderedImageFactory {

    /** Constructor */
    public KernelStatsRIF() {
    }

    /**
     * Create a new instance of KernelStatsOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the following parameters:
     * "kernel", "stats", "roi", "masksrc", "maskdest", "ignoreNaN"
     *
     * @param renderHints useful to specify a {@linkplain javax.media.jai.BorderExtender}
     */
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {
        
        RenderedImage source = paramBlock.getRenderedSource(0);

        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        if (layout == null) layout = new ImageLayout();

        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);

        Statistic[] stats =
                (Statistic[]) paramBlock.getObjectParameter(KernelStatsDescriptor.STATS_ARG_INDEX);

        KernelJAI unRotatedKernel =
                (KernelJAI) paramBlock.getObjectParameter(KernelStatsDescriptor.KERNEL_ARG_INDEX);
        KernelJAI kernel = unRotatedKernel.getRotatedKernel();

        int band = paramBlock.getIntParameter(KernelStatsDescriptor.BAND_ARG_INDEX);

        SampleModel sm = layout.getSampleModel(null);
        if (sm == null || sm.getNumBands() != stats.length) {

            int dataType = source.getSampleModel().getDataType();
            if (dataType != DataBuffer.TYPE_FLOAT && dataType != DataBuffer.TYPE_DOUBLE) {
                for (Statistic stat : stats) {
                    if (!stat.supportsIntegralResult()) {
                        dataType = DataBuffer.TYPE_DOUBLE;
                        break;
                    }
                }
            }

            sm = RasterFactory.createComponentSampleModel(
                    source.getSampleModel(),
                    dataType,
                    source.getWidth(), source.getHeight(), stats.length);

            layout.setSampleModel(sm);
            if (layout.getColorModel(null) != null) {
                ColorModel cm = ImageUtil.getCompatibleColorModel(sm, renderHints);
                layout.setColorModel(cm);
            }
        }

        ROI roi = (ROI) paramBlock.getObjectParameter(KernelStatsDescriptor.ROI_ARG_INDEX);

        Boolean maskSrc =
                (Boolean) paramBlock.getObjectParameter(KernelStatsDescriptor.MASKSRC_ARG_INDEX);

        Boolean maskDest =
                (Boolean) paramBlock.getObjectParameter(KernelStatsDescriptor.MASKDEST_ARG_INDEX);

        Boolean ignoreNaN =
                (Boolean) paramBlock.getObjectParameter(KernelStatsDescriptor.NAN_ARG_INDEX);

        Number nilValue =
                (Number) paramBlock.getObjectParameter(KernelStatsDescriptor.NO_RESULT_VALUE_ARG_INDEX);

        return new KernelStatsOpImage(
                paramBlock.getRenderedSource(0),
                extender,
                renderHints,
                layout,
                stats,
                kernel,
                band,
                roi,
                maskSrc,
                maskDest,
                ignoreNaN,
                nilValue);
    }
}

