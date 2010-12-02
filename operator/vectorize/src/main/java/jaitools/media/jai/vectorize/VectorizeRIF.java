/*
 * Copyright 2010 Michael Bedward
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

package jaitools.media.jai.vectorize;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.media.jai.ROI;

/**
 * The image factory for the Vectorize operator.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class VectorizeRIF implements RenderedImageFactory {

    /** Constructor */
    public VectorizeRIF() {
    }

    /**
     * Creates a new instance of VectorizeOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameters
     *        "roi", "band", "outsideValues" and "insideEdges"
     *
     * @param renderHints rendering hints (ignored)
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        ROI roi = (ROI) paramBlock.getObjectParameter(VectorizeDescriptor.ROI_ARG);
        int band = paramBlock.getIntParameter(VectorizeDescriptor.BAND_ARG);
        
        List<Double> outsideValues = null;
        Object obj = paramBlock.getObjectParameter(VectorizeDescriptor.OUTSIDE_VALUES_ARG);
        if (obj != null) {
            outsideValues = new ArrayList<Double>();
            Collection coll = (Collection) obj;
            for (Object val : coll) {
                outsideValues.add(((Number)val).doubleValue());
            }
        }
        
        Boolean insideEdges = (Boolean) paramBlock.getObjectParameter(VectorizeDescriptor.INSIDE_EDGES_ARG);

        return new VectorizeOpImage(paramBlock.getRenderedSource(0), roi, band, outsideValues, insideEdges);
    }
}

