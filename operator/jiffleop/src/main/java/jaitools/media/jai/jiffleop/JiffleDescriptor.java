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

package jaitools.media.jai.jiffleop;

import java.awt.Rectangle;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Jiffle operation.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
public class JiffleDescriptor extends OperationDescriptorImpl {
    
    static final int SCRIPT_ARG = 0;
    static final int DEST_NAME_ARG = 1;
    static final int DEST_BOUNDS_ARG = 2;

    private static final String[] paramNames = {
        "script",
        "destName",
        "destBounds"
    };

    private static final Class[] paramClasses = {
         String.class,
         String.class,
         Rectangle.class
    };

    private static final Object[] paramDefaults = {
         NO_PARAMETER_DEFAULT,
         "dest",
         (Rectangle)null
    };

    public JiffleDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Jiffle"},
                    {"LocalName", "Jiffle"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Execute a Jiffle script"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.2.0"},
                    {"arg0Desc", paramNames[0] + " (String):" +
                             "the Jiffle script"},
                    {"arg1Desc", paramNames[1] + " (String, default \"dest\"):" +
                             "the destination variable name"}

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
    public int getNumSources() {
        return 0;
    }

    
}
