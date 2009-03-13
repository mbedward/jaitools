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

    static final int DIAGONAL_ARG_INDEX = 0;

    private static final String[] paramNames =
        {"diagonal"};

    private static final Class[] paramClasses =
        {Boolean.class};

    private static final Object[] paramDefaults =
        {Boolean.TRUE};

    /** Constructor. */
    public RegionalizeDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Regionalize"},
                    {"LocalName", "Regionalize"},
                    {"Vendor", "jaitools"},
                    {"Description", "Identifies sufficiently uniform regions in a source image"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "0.0.1"},
                    {"arg0Desc", "diagonal (boolean) - true to include diagonal neighbours;" +
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
     * @param source0 the image to be convolved
     * @param kernel convolution kernel
     * @param hints useful for specifying a border extender; may be null
     * @return the RenderedOp destination
     * @throws IllegalArgumentException if any args are null
     */
    public static RenderedOp create(
            RenderedImage source0,
            boolean diagonal,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("Regionalize",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[0], diagonal);

        return JAI.create("Regionalize", pb, hints);
    }
}

