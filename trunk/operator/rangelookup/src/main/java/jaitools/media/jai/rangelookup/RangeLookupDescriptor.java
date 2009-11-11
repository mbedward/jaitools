/*
 * Copyright 2009 Michael Bedward
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

package jaitools.media.jai.rangelookup;

import java.awt.RenderingHints;
import java.awt.image.RenderedImage;
import javax.media.jai.JAI;
import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Describes the "RangeLookup" operation.
 * <p>
 * This is a variation on the JAI {@linkplain javax.media.jai.LookupDescriptor}.
 * It works with a {@linkplain RangeLookupTable} object in which each entry maps
 * a source image value range to a destination image value.
 * <p>
 * Example of use...
 * <pre><code>
 *
 * // Perform a lookup as follows:
 * //   Src Value     Dest Value
 * //     x < 5            1
 * //   5 <= x < 10        2
 * //  10 <= x <= 20       3
 * //  any other value    99
 * 
 * RenderedImage myIntImg = ...
 *
 * RangeLookupTable<Integer> table = new RangeLookupTable<Integer>(99);
 *
 * Range<Integer> r = new Range<Integer>(null, true, 5, false);  // x < 5
 * table.add(r, 1);
 *
 * r = new Range<Integer>(5, true, 10, false);  // 5 <= x < 10
 * table.add(r, 2);
 *
 * r = new Range<Integer>(10, true, 20, true);  // 10 <= x <= 20
 * table.add(r, 2);
 *
 * ParameterBlockJAI pb = new ParameterBlockJAI("rangelookup");
 * pb.setSource("source0", myIntImg);
 * pb.setParameter("table", table);
 * RenderedImage luImg = JAI.create("rangelookup", pb);
 * </code></pre>
 *
 * @see Range
 * @see RangeLookupTable
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
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
                    {"Version", "1.0.0"},
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

