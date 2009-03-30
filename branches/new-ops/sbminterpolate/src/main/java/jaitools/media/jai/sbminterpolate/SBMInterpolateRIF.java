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

package jaitools.media.jai.sbminterpolate;

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
 * THIS IS NOT READY FOR USE YET !!!
 *
 * The image factory for the SBMInterpolate operation.
 *
 * @see SBMInterpolateDescriptor
 * @see RegionData
 * 
 * @author Michael Bedward
 */
public class SBMInterpolateRIF implements RenderedImageFactory {

    /** Constructor */
    public SBMInterpolateRIF() {
    }

    /**
     * Create a new instance of SBMInterpolateOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameter
     * "kernel"
     * @param renderHints useful to specify a {@link BorderExtender} and
     * {@link ImageLayout}
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {

        RenderedImage src = paramBlock.getRenderedSource(0);
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        BorderExtender extender = RIFUtil.getBorderExtenderHint(renderHints);
        if (extender == null) {
            extender = BorderExtender.createInstance(BorderExtender.BORDER_ZERO);
        }

        ROI roi = (ROI) paramBlock.getObjectParameter(SBMInterpolateDescriptor.ROI_ARG_INDEX);
        if (roi == null) {
            throw new IllegalArgumentException("the roi argument can't be null");
        }

        KernelJAI kernel =
                (KernelJAI) paramBlock.getObjectParameter(SBMInterpolateDescriptor.KERNEL_ARG_INDEX);

        Integer avNumSamples = (Integer) paramBlock.getObjectParameter(
                SBMInterpolateDescriptor.SAMPLES_ARG_INDEX);

        return new SBMInterpolateOpImage(src,
                extender,
                renderHints,
                layout,
                roi,
                kernel,
                avNumSamples);
    }
}

