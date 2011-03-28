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

package jaitools.media.jai.jiffleop;

import com.sun.media.jai.opimage.RIFUtil;
import jaitools.CollectionFactory;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.Map;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.RasterFactory;

/**
 * The image factory for the "Jiffle" operation.
 *
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class JiffleRIF implements RenderedImageFactory {

    /** Constructor */
    public JiffleRIF() {
    }

    /**
     * Create a new instance of JiffleOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameters
     * WRITE ME
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        Map<String, RenderedImage> sourceImages = CollectionFactory.map();
        
        String script = (String) paramBlock.getObjectParameter(JiffleDescriptor.SCRIPT_ARG);
        String destVarName = (String) paramBlock.getObjectParameter(JiffleDescriptor.DEST_NAME_ARG);
        Rectangle destBounds = (Rectangle) paramBlock.getObjectParameter(JiffleDescriptor.DEST_BOUNDS_ARG);

        // Ignore any ImageLayout that was provided and create one here
        ImageLayout layout = new ImageLayout(destBounds.x, destBounds.y, destBounds.width, destBounds.height);

        Dimension defaultTileSize = JAI.getDefaultTileSize();
        SampleModel sm = RasterFactory.createPixelInterleavedSampleModel(
                DataBuffer.TYPE_DOUBLE, defaultTileSize.width, defaultTileSize.height, 1);
        layout.setSampleModel(sm);
        layout.setColorModel(PlanarImage.createColorModel(sm));
        
        return new JiffleOpImage(sourceImages, layout, renderHints, script, destVarName, destBounds);
    }
}

