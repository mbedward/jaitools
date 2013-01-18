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
package org.jaitools.media.jai.rangelookup;

import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.List;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.RasterFactory;

import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.util.JDKWorkarounds;

import org.jaitools.imageutils.ImageDataType;


/**
 * The image factory for the RangeLookup operation.
 *
 * @see RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
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

        final RenderedImage src = paramBlock.getRenderedSource(0);
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        
        final RangeLookupTable table =
                (RangeLookupTable) paramBlock.getObjectParameter(RangeLookupDescriptor.TABLE_ARG);

        /*
         * Default value may be null, indicating unmatched source values 
         * should be passed through.
         */
        final Number defaultValue = 
                (Number) paramBlock.getObjectParameter(RangeLookupDescriptor.DEFAULT_ARG);

        /*
         * Set the destination type based on the
         * type and range of lookup table return values.
         */
        final Class<? extends Number> destClazz;
        List<LookupItem> items = table.getItems();
        if (items.size() > 0) {
            destClazz = items.get(0).getValue().getClass();
        } else if (defaultValue != null) {
            destClazz = defaultValue.getClass();
        } else {
            // fall back to source value class
            int typeCode = paramBlock.getRenderedSource(0).getSampleModel().getDataType();
            ImageDataType dataType = ImageDataType.getForDataBufferType(typeCode);
            destClazz = dataType.getDataClass();
        }
        
        int dataType = -1;
        if (destClazz.equals(Short.class)) {

            // if the values are positive we should go with USHORT
            for (int i = items.size() - 1; i >= 0; i--) {
                if (items.get(i).getValue().shortValue() < 0) {
                    dataType = DataBuffer.TYPE_SHORT;
                    break;
                }
            }

            // No negative values so USHORT can be used
            if (dataType == -1) {
                dataType = DataBuffer.TYPE_USHORT;
            } 
            
        } else { // All data classes other than Short
            try {
                ImageDataType t = ImageDataType.getForClass(destClazz);
                dataType = t.getDataBufferType();

            } catch (IllegalArgumentException ex) {
                throw new IllegalArgumentException(
                        "Illegal destination class for this rangelookuptable:"
                        + destClazz.toString());
            }
        }

        final boolean isDataTypeChanged;
        if (src.getSampleModel().getDataType() != dataType) {
            isDataTypeChanged = true;
        } else {
            isDataTypeChanged = false;
        }

        if (isDataTypeChanged) {
            // Create or clone the ImageLayout.
            if (layout == null) {
                layout = new ImageLayout(src);
            } else {
                layout = (ImageLayout) layout.clone();
            }

            // Get prospective destination SampleModel.
            SampleModel sampleModel = layout.getSampleModel(src);

            // Create a new SampleModel 
            int tileWidth = layout.getTileWidth(src);
            int tileHeight = layout.getTileHeight(src);
            int numBands = src.getSampleModel().getNumBands();

            SampleModel csm =
                    RasterFactory.createComponentSampleModel(sampleModel,
                    dataType,
                    tileWidth,
                    tileHeight,
                    numBands);

            layout.setSampleModel(csm);

            // Check ColorModel.
            ColorModel colorModel = layout.getColorModel(null);
            if (colorModel != null
                    && !JDKWorkarounds.areCompatibleDataModels(layout.getSampleModel(null),
                    colorModel)) {
                // Clear the mask bit if incompatible.
                layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
            }
        }

        return new RangeLookupOpImage(src,
                renderHints,
                layout,
                table,
                defaultValue);
    }
}
