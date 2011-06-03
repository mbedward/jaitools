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

import com.sun.media.jai.opimage.RIFUtil;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.Collection;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;

/**
 *
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class VMCAFillRIF implements RenderedImageFactory {

    /** Constructor */
    public VMCAFillRIF() {
    }

    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);

        KernelJAI unRotatedKernel =
                (KernelJAI) paramBlock.getObjectParameter(VMCAFillDescriptor.KERNEL_ARG);
        KernelJAI kJAI = unRotatedKernel.getRotatedKernel();

        ROI roi = (ROI) paramBlock.getObjectParameter(VMCAFillDescriptor.ROI_ARG);
        
        Collection<Number> gapValues = 
                (Collection<Number>) paramBlock.getObjectParameter(VMCAFillDescriptor.GAP_VALUES_ARG);
        
        
        return new VMCAFillOpImage(paramBlock.getRenderedSource(0),
                extender,
                renderHints,
                layout,
                kJAI,
                roi,
                gapValues);
    }
}

