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

package jaitools.media.jai.rangelookup;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 *         *** THIS OPERATION IS NOT FUNCTIONAL YET ***
 *
 * Describes the "RangeLookup" operation.
 * <p>
 * This is a variation on the JAI {@linkplain javax.media.jai.LookupDescriptor}.
 * It works with a {@linkplain RangeLookupTable} object in which each entry maps
 * a source image value range to a destination image value.
 * 
 * @author Michael Bedward
 */
public class RangeLookupDescriptor extends OperationDescriptorImpl {

    static final int TABLE_ARG_INDEX = 0;

    private static final String[] paramNames =
        {"table"};

    private static final Class[] paramClasses =
        {RangeLookupTable.class};

    private static final Object[] paramDefaults =
        {NO_PARAMETER_DEFAULT};

    /** Constructor. */
    public RangeLookupDescriptor() {
        super(new String[][]{
                    {"GlobalName", "RangeLookup"},
                    {"LocalName", "RangeLookup"},
                    {"Vendor", "jaitools"},
                    {"Description", "Maps source image value ranges to destination image values"},
                    {"DocURL", "http://code.google.com/p/jai-tools/"},
                    {"Version", "1.0-SNAPSHOT"},
                    {"arg0Desc", "table (RangeLookupTable)"}
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
    public boolean arePropertiesSupported() {
        return true;
    }

    /**
     * Convenience method which constructs a {@link ParameterBlockJAI} and
     * invokes {@code JAI.create("RangeLookup", params) }.
     *
     * @param source0 the source image
     * @param table an instance of RangeLookupTable defining the mappings from
     * source image value ranges to destination image values
     * @param hints rendering hints (may be null)
     * @return the RenderedOp destination
     */
    public static RenderedOp create(
            RenderedImage source0,
            RangeLookupTable table,
            RenderingHints hints) {
        ParameterBlockJAI pb =
                new ParameterBlockJAI("RangeLookup",
                RenderedRegistryMode.MODE_NAME);

        pb.setSource("source0", source0);
        pb.setParameter(paramNames[TABLE_ARG_INDEX], table);

        return JAI.create("RangeLookup", pb, hints);
    }
}

