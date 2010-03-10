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

package jaitools.media.jai.maskedconvolve;

import com.sun.media.jai.opimage.RIFUtil;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;

/**
 * The image factory for the {@link MaskedConvolveOpImage} operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
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

