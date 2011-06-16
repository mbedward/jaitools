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

package org.jaitools.media.jai.maskedconvolve;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;

import com.sun.media.jai.opimage.RIFUtil;


/**
 * The image factory for the {@link MaskedConvolveOpImage} operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class MaskedConvolveRIF implements RenderedImageFactory {

    /** Constructor */
    public MaskedConvolveRIF() {
    }

    /**
     * Create a new instance of MaskedConvolveOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameters
     * "kernel", "roi", "masksource" and "maskdest"
     *
     * @param renderHints useful to specify a {@linkplain javax.media.jai.BorderExtender}
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        // Get ImageLayout from renderHints if any.
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        // Get BorderExtender from renderHints if any.
        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);

        KernelJAI unRotatedKernel =
                (KernelJAI) paramBlock.getObjectParameter(MaskedConvolveDescriptor.KERNEL_ARG);
        KernelJAI kJAI = unRotatedKernel.getRotatedKernel();

        ROI roi = (ROI) paramBlock.getObjectParameter(MaskedConvolveDescriptor.ROI_ARG);
        
        Boolean maskSrc = 
                (Boolean) paramBlock.getObjectParameter(MaskedConvolveDescriptor.MASKSRC_ARG);
        
        Boolean maskDest = 
                (Boolean) paramBlock.getObjectParameter(MaskedConvolveDescriptor.MASKDEST_ARG);

        Number nilValue =
                (Number) paramBlock.getObjectParameter(MaskedConvolveDescriptor.NIL_VALUE_ARG);

        int minCells = paramBlock.getIntParameter(MaskedConvolveDescriptor.MIN_CELLS_ARG);
        
        return new MaskedConvolveOpImage(paramBlock.getRenderedSource(0),
                extender,
                renderHints,
                layout,
                kJAI,
                roi,
                maskSrc,
                maskDest,
                nilValue,
                minCells);
    }
}

