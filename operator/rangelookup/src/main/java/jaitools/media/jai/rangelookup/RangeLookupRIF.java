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
import com.sun.media.jai.util.JDKWorkarounds;

import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.sql.DatabaseMetaData;

import javax.media.jai.BorderExtender;
import javax.media.jai.ImageLayout;
import javax.media.jai.RasterFactory;

/**
 * The image factory for the RangeLookup operation.
 *
 * @see RangeLookupDescriptor
 * 
 * @author Michael Bedward
 * @since 1.0
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

        final RenderedImage src = paramBlock.getRenderedSource(0);
        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        final RangeLookupTable table =
                (RangeLookupTable) paramBlock.getObjectParameter(RangeLookupDescriptor.TABLE_ARG_INDEX);
        
        
        // try to set the right destination type rather than the one of the input image
        // Most part of the time we want to map float or double images
        // to classified byte image, therefore we can create an output image as smaller as 8 times the input image
        // if the set the right destination type
        final Class<? extends Number> destClazz;
        if(table.items.size()>0)
        	destClazz=((RangeLookupTable.Item)table.items.get(0)).destValue.getClass();
        else
        	destClazz=table.defaultValue.getClass();
        int dataType=-1;
        if(destClazz.equals(Byte.class))
        	dataType=DataBuffer.TYPE_BYTE;
        else if(destClazz.equals(Short.class)){
        	
        	// if the values are positive we should go with USHORT
        	for(int i=table.items.size()-1;i>=0;i--)
        		if(((RangeLookupTable.Item)table.items.get(i)).destValue.shortValue()<0){
        			dataType=DataBuffer.TYPE_SHORT;
        			break;
        		}
        		
        	// we did not find anyone what was negative
        	if(dataType==-1)
        		dataType=DataBuffer.TYPE_USHORT;
        	
        }
        else if(destClazz.equals(Integer.class)){
        	dataType=DataBuffer.TYPE_INT;
        }
        else if(destClazz.equals(Float.class))
        	dataType=DataBuffer.TYPE_FLOAT;
        else if(destClazz.equals(Double.class))
        	dataType=DataBuffer.TYPE_DOUBLE; 
        else
        	throw new IllegalArgumentException("Illegal destination class for this rangelookuptable:"+destClazz.toString());
        
        final boolean isDataTypeChanged;
        if(src.getSampleModel().getDataType()!=dataType)
        	isDataTypeChanged=true;
        else
        	isDataTypeChanged=false;
        
        if(isDataTypeChanged){
            // Create or clone the ImageLayout.
            if(layout == null) {
                layout = new ImageLayout(src);
            } else {
                layout = (ImageLayout)layout.clone();
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
            if(colorModel != null &&
               !JDKWorkarounds.areCompatibleDataModels(layout.getSampleModel(null),
                                                       colorModel)) {
                // Clear the mask bit if incompatible.
                layout.unsetValid(ImageLayout.COLOR_MODEL_MASK);
            }            
        }

        return new RangeLookupOpImage(src,
                renderHints,
                layout,
                table);
    }
}

