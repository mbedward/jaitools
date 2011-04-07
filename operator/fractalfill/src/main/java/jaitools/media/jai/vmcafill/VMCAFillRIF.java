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

