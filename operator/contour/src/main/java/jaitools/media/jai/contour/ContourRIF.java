/*
 * Copyright 2010 Michael Bedward
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

package jaitools.media.jai.contour;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import javax.media.jai.ROI;

/**
 * The image factory for the Contour operator.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourRIF implements RenderedImageFactory {

    public ContourRIF() {
    }

    /**
     * Creates a new instance of ContourOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image and the parameters
     *        "roi", "band", "outsideValues" and "insideEdges"
     *
     * @param renderHints rendering hints (ignored)
     */
    public RenderedImage create(ParameterBlock paramBlock,
            RenderingHints renderHints) {
        
        Object obj = null;
        
        ROI roi = (ROI) paramBlock.getObjectParameter(ContourDescriptor.ROI_ARG);
        int band = paramBlock.getIntParameter(ContourDescriptor.BAND_ARG);
        
        List<Double> contourLevels = null;
        Double interval = null;
        
        Collection levels = (Collection) paramBlock.getObjectParameter(ContourDescriptor.LEVELS_ARG);
        if (levels != null && !levels.isEmpty()) {
            contourLevels = new ArrayList<Double>();
            for (Object val : levels) {
                contourLevels.add(((Number)val).doubleValue());
            }
        } else {
            // No contour levels - use interval parameter
            obj = paramBlock.getObjectParameter(ContourDescriptor.INTERVAL_ARG);
            interval = ((Number)obj).doubleValue();
        }
        
        Collection noDataValues = (Collection) paramBlock.getObjectParameter(ContourDescriptor.NO_DATA_ARG);
        
        Boolean simplify = (Boolean) paramBlock.getObjectParameter(ContourDescriptor.SIMPLIFY_ARG);

        Boolean smooth = (Boolean) paramBlock.getObjectParameter(ContourDescriptor.SMOOTH_ARG);

        return new ContourOpImage(paramBlock.getRenderedSource(0), roi, band, contourLevels, interval, noDataValues, simplify, smooth);
    }
}
