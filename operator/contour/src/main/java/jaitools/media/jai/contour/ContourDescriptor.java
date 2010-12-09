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

import java.util.Collection;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ContourDescriptor extends OperationDescriptorImpl {
    public final static String CONTOUR_PROPERTY_NAME = "contours";
    
    static final int ROI_ARG = 0;
    static final int BAND_ARG = 1;
    static final int LEVELS_ARG = 2;
    static final int MERGE_TILES_ARG = 3;
    static final int SMOOTH_ARG = 4;

    private static final String[] paramNames = {
        "roi",
        "band",
        "levels",
        "mergeTiles",
        "smooth"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.ROI.class,
         Integer.class,
         Collection.class,
         Boolean.class,
         Boolean.class
    };

    private static final Object[] paramDefaults = {
         (ROI) null,
         Integer.valueOf(0),
         NO_PARAMETER_DEFAULT,
         Boolean.TRUE,
         Boolean.FALSE,
    };

    

    public ContourDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Contour"},
                    {"LocalName", "Contour"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Traces contours based on source image values"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.1.0"},
                    
                    {"arg0Desc", paramNames[0] + " an optional ROI"},
                    
                    {"arg1Desc", paramNames[1] + " (Integer, default=0) " +
                              "the source image band to process"},
                    
                    {"arg2Desc", paramNames[2] + " (Collection<? extends Number>, required) " +
                              "values for which to generate contours"},
                    
                    {"arg2Desc", paramNames[3] + " (Boolean, default=true) " +
                              "whether to merge contour lines across source image tile boundaries"},
                    
                    {"arg3Desc", paramNames[4] + " (Boolean, default=false) " +
                              "whether to smooth contour lines with Bezier interpolation"}
                },
                new String[]{RenderedRegistryMode.MODE_NAME},   // supported modes
                
                1,                                              // number of sources
                
                paramNames,
                paramClasses,
                paramDefaults,
                    
                null                                            // valid values (none defined)
                );    
    }

}
