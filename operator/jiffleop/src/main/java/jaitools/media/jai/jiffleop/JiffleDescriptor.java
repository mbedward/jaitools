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

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Jiffle operation.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id$
 */
class JiffleDescriptor extends OperationDescriptorImpl {

    private static final String[] paramNames = {
        "script"
    };

    private static final Class[] paramClasses = {
         String.class
    };

    private static final Object[] paramDefaults = {
         NO_PARAMETER_DEFAULT
    };

    public JiffleDescriptor() {
        super(new String[][]{
                    {"GlobalName", "Jiffle"},
                    {"LocalName", "Jifle"},
                    {"Vendor", "jaitools.media.jai"},
                    {"Description", "Execute a Jiffle script"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.2.0"},
                    {"arg0Desc", paramNames[0] + " (String):" +
                             "the Jiffle script"}

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
