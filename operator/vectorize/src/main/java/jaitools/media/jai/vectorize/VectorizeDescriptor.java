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

import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.Collections;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Vectorize regions of uniform data within a source image.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class VectorizeDescriptor extends OperationDescriptorImpl {
    
    /**
     * Name used to access the vectorized region boundaries as
     * a destination image property.
     */
    public static final String VECTOR_PROPERTY_NAME = "vectors";
    
    /**
     * Filter small polygons by merging each with its largest (area) neighbour.
     * This is the default.
     */
    public static final int FILTER_MERGE_LARGEST = 0;
    /**
     * Filter small polygons by merging each with a randomly chosen neighbour.
     */
    public static final int FILTER_MERGE_RANDOM = 1;
    /**
     * Filter small polygons by simple deletion.
     */
    public static final int FILTER_DELETE = 2;

    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int OUTSIDE_VALUES_ARG = 2;
    static final int INSIDE_EDGES_ARG = 3;
    static final int REMOVE_COLLINEAR_ARG = 4;
    static final int FILTER_SMALL_POLYS_ARG = 5;
    static final int FILTER_METHOD_ARG = 6;

    private static final String[] paramNames = {
        "roi",
        "band",
        "outsideValues",
        "insideEdges",
        "removeCollinear",
        "filterThreshold",
        "filterMethod"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Boolean.class,
         Boolean.class,
         Double.class,
         Integer.class
    };

    private static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         Collections.EMPTY_LIST,
         Boolean.TRUE,
         Boolean.TRUE,
         Double.valueOf(0.0),
         FILTER_MERGE_LARGEST
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
                              "optional set of values to treat as outside"},
                    
                    {"arg3Desc", paramNames[3] + " (Boolean, default=true) " +
                              "whether to vectorize boundaries between adjacent" +
                              "regions with non-outside values"},
                    {"arg4Desc", paramNames[4] + " (Boolean, default=false) " +
                              "whether to reduce collinear points in the resulting polygons"},
                    {"arg5Desc", paramNames[5] + " (Double, default=0) " +
                              "area (fractional pixels) below which polygons will be filtered"},
                    {"arg6Desc", paramNames[6] + " (Integer, default=FILTER_MERGE_LARGEST) " +
                              "filter method to use for polygons smaller than threshold area"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );
    }

    @Override
    protected boolean validateParameters(String modeName, ParameterBlock pb, StringBuffer msg) {

        boolean ok = super.validateParameters(modeName, pb, msg);
        if (ok) {
            int filterMethod = pb.getIntParameter(FILTER_METHOD_ARG);
            if ( !(filterMethod == FILTER_MERGE_LARGEST ||
                   filterMethod == FILTER_MERGE_RANDOM ||
                   filterMethod == FILTER_DELETE) ) {
                ok = false;
                msg.append("Invalid filter method: ").append(filterMethod);
            }
        }
        return ok;
    }

}

