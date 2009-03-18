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

package jaitools.media.jai.regionalize;

import com.sun.media.jai.opimage.RIFUtil;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;

/**
 * The image factory for the {@link RegionalizeOpImage} operation.
 *
 * NOT FUNCTIONAL YET
 *
 * @author Michael Bedward
 */
public class RegionalizeRIF implements RenderedImageFactory {

    /** Constructor */
    public RegionalizeRIF() {
    }

    /**
     * Create a new instance of RegionalizeOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameter
     * "kernel"
     * @param renderHints useful to specify a {@link BorderExtender} and
     * {@link ImageLayout}
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        
        int band = paramBlock.getIntParameter(RegionalizeDescriptor.BAND_ARG_INDEX);
        double tolerance = paramBlock.getDoubleParameter(RegionalizeDescriptor.TOLERANCE_ARG_INDEX);
        boolean diagonal = (Boolean) paramBlock.getObjectParameter(RegionalizeDescriptor.DIAGONAL_ARG_INDEX);

        return new RegionalizeOpImage(paramBlock.getRenderedSource(0),
                renderHints,
                layout,
                band,
                tolerance,
                diagonal);
    }
}

