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
/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package jaitools.media.jai.combine;

// J2SE dependencies
import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.List;
import java.util.Vector;

import javax.media.jai.CRIFImpl;


/**
 * The image factory for the {@link CombineOpImage} operation.
 *
 * @since 2.1
 * @source $URL$
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux (IRD)
 */
public class CombineCRIF extends CRIFImpl {
    /**
     * Constructs a default factory.
     */
    public CombineCRIF() {
    }

    /**
     * Creates a {@link RenderedImage} representing the results of an imaging
     * operation for a given {@link ParameterBlock} and {@link RenderingHints}.
     */
    public RenderedImage create(final ParameterBlock param,
                                final RenderingHints hints)
    {
        final Vector<Object>      sources =                    param.getSources();
        final double[][]          matrix = (double[][])       param.getObjectParameter(0);
        final CombineTransform transform = (CombineTransform) param.getObjectParameter(1);
        return transform==null && isDyadic(sources, matrix) ?
               new CombineOpImage.Dyadic(sources, matrix, hints)   :
               new CombineOpImage       (sources, matrix, transform, hints);
    }

    /**
     * Returns {@code true} if the combine operation could be done through
     * the optimized {@code CombineOpImage.Dyadic} class.
     */
    private static boolean isDyadic(final List<Object> sources, final double[][] matrix) {
        if (sources.size() != 2) {
            return false;
        }
        final RenderedImage src0 = (RenderedImage) sources.get(0);
        final RenderedImage src1 = (RenderedImage) sources.get(1);
        final int numBands0 = src0.getSampleModel().getNumBands();
        final int numBands1 = src1.getSampleModel().getNumBands();
        final int numBands  = matrix.length;
        if (numBands!=numBands0 || numBands!=numBands1) {
            return false;
        }
        for (int i=0; i<numBands; i++) {
            final double[] row = matrix[i];
            for (int j=numBands0+numBands1; --j>=0;) {
                if (j!=i && j!=i+numBands0 && row[j]!=0) {
                    return false;
                }
            }
        }
        return true;
    }
}
