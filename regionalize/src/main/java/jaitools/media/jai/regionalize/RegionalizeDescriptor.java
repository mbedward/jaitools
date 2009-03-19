/*
 * Copyright 2009 Michael Bedward
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

package jaitools.media.jai.regionalize;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Describes the "Regionalize" operation.
 *
 * NOT FUNCTIONAL YET
 * 
 * @author Michael Bedward
 */
public class RegionalizeDescriptor extends OperationDescriptorImpl {

    static final int BAND_ARG_INDEX = 0;
    static final int TOLERANCE_ARG_INDEX = 1;
    static final int DIAGONAL_ARG_INDEX = 2;

    private static final String[] paramNames =
        {"band", "tolerance", "diagonal"};

    private static final Class[] paramClasses =
        {Integer.class, Double.class, Boolean.class};

    private static final Object[] paramDefaults =
        {Integer.valueOf(0), Double.valueOf(0d), Boolean.TRUE};

    /** Constructor. */
    public RegionalizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Regionalize"},
                    {"LocalName", "Regionalize"},
                    {"Vendor", "jaitools"},
                    {"Description", "Identifies sufficiently uniform regions in a source image"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "0.0.1"},
                    {"arg0Desc", "band (int) - the band to regionalize"},
                    {"arg1Desc", "tolerance (double) - tolerance for pixel value comparison"},
                    {"arg2Desc", "diagonal (boolean) - true to include diagonal neighbours;" +
                             "false for only orthogonal neighbours"}
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
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("regionalize", params) }
     * @param source0 the image to be regionalized
     * @param band the band to process
     * @param tolerance tolerance for pixel value comparisons
     * @param diagonal true to include diagonal connections; false for only
     * orthogonal connections
     * @param hints rendering hints (may be null)
     * @return the RenderedOp destination
     * @throws IllegalArgumentException if any args are null
     */
    public static RenderedOp create(
            RenderedImage source0,
            int band,
            double tolerance,
            boolean diagonal,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("Regionalize",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[BAND_ARG_INDEX], band);
        pb.setParameter(paramNames[TOLERANCE_ARG_INDEX], tolerance);
        pb.setParameter(paramNames[DIAGONAL_ARG_INDEX], diagonal);

        return JAI.create("Regionalize", pb, hints);
    }
}

