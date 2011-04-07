/*
 * Copyright 2011 Michael Bedward
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

package jaitools.media.jai.vmcafill;

import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.Collections;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class VMCAFillDescriptor extends OperationDescriptorImpl {

    static final int KERNEL_ARG = 0;
    static final int ROI_ARG = 1;
    static final int GAP_VALUES_ARG = 2;

    private static final String[] paramNames = {
        "kernel",
        "roi",
        "gapvalues"
    };

    private static final Class[] paramClasses = {
         javax.media.jai.KernelJAI.class,
         javax.media.jai.ROI.class,
         Collection.class
    };

    private static final Object[] paramDefaults = {
         NO_PARAMETER_DEFAULT,
         (ROI) null,
         Collections.singleton(Double.NaN)
    };

    /** Constructor. */
    public VMCAFillDescriptor() {
        super(new String[][]{
                    {"GlobalName", "VMCAFill"},
                    {"LocalName", "VMCAFill"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Voter Model Cellular Automaton: a fractal gap filling algorithm"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0.0"},
                    {"arg0Desc", paramNames[0] + " - a JAI Kernel object"},
                    {"arg1Desc", paramNames[1] + " - an ROI object"},
                    {"arg2Desc", paramNames[2] + " (Collection<Number>): " +
                             "image values to treat as gaps"}

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

        return ok;
    }

}

