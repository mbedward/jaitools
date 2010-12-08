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

package jaitools.media.jai.vectorize;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.Collections;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.ROI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Vectorize regions of uniform data within a source image.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class VectorizeDescriptor extends OperationDescriptorImpl {
    
    /**
     * Name used to access the vectorized region boundaries as
     * a destination image property.
     */
    public static final String VECTOR_PROPERTY_NAME = "vectors";

    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int OUTSIDE_VALUES_ARG = 2;
    static final int INSIDE_EDGES_ARG = 3;

    private static final String[] paramNames = {
        "roi",
        "band",
        "outsideValues",
        "insideEdges"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Boolean.class
    };

    private static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         Collections.EMPTY_LIST,
         Boolean.TRUE,
    };

    /** Constructor. */
    public VectorizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Vectorize"},
                    {"LocalName", "Vectorize"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Vecotirze boundaries of regions of uniform value"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " an optional ROI"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default=0) " +
                              "the source image band to process"},
                    
                    {"arg2Desc", paramNames[2] + " (Collection, default=null) " +
                              "optionsl set of values to treat as outside"},
                    
                    {"arg3Desc", paramNames[3] + " (Boolean, default=true) " +
                              "whether to vectorize boundaries between adjacent" +
                              "regions with non-outside values"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );
    }

    /**
     * Constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("Vectorize", params) }.
     * <p>
     * 
     * @param source0 the source image
     *
     * @param roi an optional {@link ROI} defining the bounds of the area to
     *        be vectorized
     * 
     * @param band source image band to process
     * 
     * @param outsideValues an optional collection of image values to treat as
     *        "outside" (ie. not data regions); may be {@code null} or empty
     * 
     * @param insideEdges whether to vectorize boundaries between adjacent
     *        data (ie. non-outside) regions
     *
     * @param hints rendering hints (ignored)
     *
     * @return the RenderedOp destination
     * 
     * @throws IllegalArgumentException if any args are null
     */
    public static RenderedOp create(
            RenderedImage source0,
            ROI roi,
            int band,
            Collection<Number> outsideValues,
            Boolean insideEdges,
            RenderingHints hints) {
                
        ParameterBlockJAI pb =
                new ParameterBlockJAI("Vectorize",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[ROI_ARG], roi);
        pb.setParameter(paramNames[BAND_ARG], band);
        pb.setParameter(paramNames[OUTSIDE_VALUES_ARG], outsideValues);
        pb.setParameter(paramNames[INSIDE_EDGES_ARG], insideEdges);

        return JAI.create("Vectorize", pb, hints);
    }

    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {

        boolean ok = super.validateParameters(modeName, pb, msg);
        if (ok) {
            // TODO: any checking required ?
        }
        return ok;
    }

}

