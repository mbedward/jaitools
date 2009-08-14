/*
 * Copyright 2009 Michael Bedward
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

package jaitools.media.jai.rangelookup;

import com.sun.media.jai.opimage.RIFUtil;
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;

/**
 * The image factory for the RangeLookup operation.
 *
 * @see RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class RangeLookupRIF implements RenderedImageFactory {

    /** Constructor */
    public RangeLookupRIF() {
    }

    /**
     * Create a new instance of RangeLookupOpImage in the rendered layer.
     *
     * @param paramBlock an instance of ParameterBlock
     * @param renderHints useful to specify a {@link BorderExtender} and
     * {@link ImageLayout}
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {

        RenderedImage src = paramBlock.getRenderedSource(0);

        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        RangeLookupTable table =
                (RangeLookupTable) paramBlock.getObjectParameter(RangeLookupDescriptor.TABLE_ARG_INDEX);

        return new RangeLookupOpImage(src,
                renderHints,
                layout,
                table);
    }
}

