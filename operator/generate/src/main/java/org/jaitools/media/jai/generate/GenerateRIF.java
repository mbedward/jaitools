/* 
 *  Copyright (c) 2013, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.generate;

import com.sun.media.jai.opimage.RIFUtil;
import java.awt.RenderingHints;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.RasterFactory;
import org.jaitools.imageutils.ImageDataType;

/**
 *
 * @author Michael Bedward
 * @since 1.3
 */
public class GenerateRIF implements RenderedImageFactory {

    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {
        Number width = (Number) paramBlock.getObjectParameter(GenerateDescriptor.WIDTH_ARG);
        Number height = (Number) paramBlock.getObjectParameter(GenerateDescriptor.HEIGHT_ARG);
        
        Object obj = paramBlock.getObjectParameter(GenerateDescriptor.GENERATORS_ARG);
        Generator[] generators;
        if (obj instanceof Generator) {
            generators = new Generator[] { (Generator) obj };
        } else {
            generators = (Generator[]) obj;
        }
        
        ImageDataType dataType = generators[0].getDataType();
        
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        if (layout == null) layout = new ImageLayout();
        
        layout.setWidth(width.intValue());
        layout.setHeight(height.intValue());
        
        SampleModel userSM = layout.getSampleModel(null);
        if (userSM == null || userSM.getNumBands() != generators.length) {
            int tileWidth = layout.getTileWidth(null);
            if (tileWidth == 0) {
                tileWidth = JAI.getDefaultTileSize().width;
            }
            
            int tileHeight = layout.getTileHeight(null);
            if (tileHeight == 0) {
                tileHeight = JAI.getDefaultTileSize().height;
            }
            
            int numBands = generators.length;

            SampleModel workingSM = RasterFactory.createBandedSampleModel(
                    dataType.getDataBufferType(), tileWidth, tileHeight, numBands);

            layout.setSampleModel(workingSM);
        }
        
        return new GenerateOpImage(layout, renderHints, generators);
    }
    
    private SampleModel makeSampleModel(int tileWidth, int tileHeight,
                                               Number[] bandValues) {
        int numBands = bandValues.length;
        int dataType;

        if (bandValues instanceof Byte[]) {
            dataType = DataBuffer.TYPE_BYTE;
        } else if (bandValues instanceof Short[]) {
            /* If all band values are positive, use UShort, else use Short. */
            dataType = DataBuffer.TYPE_USHORT;

            Short[] shortValues = (Short[])bandValues;
            for (int i = 0; i < numBands; i++) {
                if (shortValues[i].shortValue() < 0) {
                    dataType = DataBuffer.TYPE_SHORT;
                    break;
                }
            }
        } else if (bandValues instanceof Integer[]) {
            dataType = DataBuffer.TYPE_INT;
        } else if (bandValues instanceof Float[]) {
            dataType = DataBuffer.TYPE_FLOAT;
        } else if (bandValues instanceof Double[]) {
            dataType = DataBuffer.TYPE_DOUBLE;
        } else {
            dataType = DataBuffer.TYPE_UNDEFINED;
        }

        return RasterFactory.createPixelInterleavedSampleModel(
                             dataType, tileWidth, tileHeight, numBands);
        
    }
}
