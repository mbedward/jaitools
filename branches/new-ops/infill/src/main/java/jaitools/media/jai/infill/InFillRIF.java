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

package jaitools.media.jai.infill;

import com.sun.media.jai.opimage.RIFUtil;
import jaitools.media.jai.kernel.KernelFactory;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;

/**
 * The image factory for the {@linkplain InFillOpImage} operation.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL: https://jai-tools.googlecode.com/svn/branches/new-ops/infill/src/main/java/jaitools/media/jai/infill/InFillRIF.java $
 * @version $Id: InFillRIF.java 535 2009-08-13 13:05:00Z michael.bedward $
 */
public class InFillRIF implements RenderedImageFactory {

    /** Constructor */
    public InFillRIF() {
    }

    /**
     * Create a new instance of InFillOpImage in the rendered layer.
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

        ROI roi = (ROI) paramBlock.getObjectParameter(InFillDescriptor.ROI_ARG);

        float nbrRadius = paramBlock.getFloatParameter(InFillDescriptor.NBR_RADIUS_ARG);
        KernelJAI kernel = KernelFactory.createCircle((int) nbrRadius);

        float candidateRadius = paramBlock.getFloatParameter(InFillDescriptor.CANDIDATE_RADIUS_ARG);

        float subsetProp = paramBlock.getFloatParameter(InFillDescriptor.CANDIDATE_SUBSET_PROP_ARG);

        boolean doWeightedAv = (Boolean) paramBlock.getObjectParameter(InFillDescriptor.DO_WIEGHTED_AVERAGE_ARG);
        
        return new InFillOpImage(paramBlock.getRenderedSource(0),
                extender,
                renderHints,
                layout,
                roi,
                kernel,
                candidateRadius,
                subsetProp,
                doWeightedAv);
    }
}

