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

package org.jaitools.media.jai.regionalize;

import java.awt.RenderingHints;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;

import com.sun.media.jai.opimage.RIFUtil;


/**
 * The image factory for the Regionalize operation.
 *
 * @see RegionalizeDescriptor
 * @see Region
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
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

        RenderedImage src = paramBlock.getRenderedSource(0);
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);

        if (layout == null) {
            layout = new ImageLayout();
        }

        int tileWidth = layout.getTileWidth(null);
        if (tileWidth == 0) {
            tileWidth = JAI.getDefaultTileSize().width;
        }

        int tileHeight = layout.getTileHeight(null);
        if (tileHeight == 0) {
            tileHeight = JAI.getDefaultTileSize().height;
        }

        SampleModel sm = new ComponentSampleModel(
                DataBuffer.TYPE_INT, 
                tileWidth,
                tileHeight,
                1, tileWidth,  // pixel stride and scan-line stride
                new int[]{0});  // band offset

        layout.setSampleModel(sm);

        int band = paramBlock.getIntParameter(RegionalizeDescriptor.BAND_ARG_INDEX);
        double tolerance = paramBlock.getDoubleParameter(RegionalizeDescriptor.TOLERANCE_ARG_INDEX);
        boolean diagonal = (Boolean) paramBlock.getObjectParameter(RegionalizeDescriptor.DIAGONAL_ARG_INDEX);

        return new RegionalizeOpImage(src,
                renderHints,
                layout,
                band,
                tolerance,
                diagonal);
    }
}

