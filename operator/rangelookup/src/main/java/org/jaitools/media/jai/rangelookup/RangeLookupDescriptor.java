/* 
 *  Copyright (c) 2009-2013, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.media.jai.rangelookup;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * Describes the "RangeLookup" operation.
 * <p>
 * This is a variation on the JAI Lookup operation.
 * It works with a {@linkplain RangeLookupTable} object in which each entry maps
 * a source image value range to a destination image value.
 * <p>
 * In the example below, double data values from a source image are mapped
 * to integer values in a destination image.
 * <pre><code>
 * RenderedImage srcImage = ...
 *
 * // RangeLookupTable is an immutable class. Use the associated Builder class
 * // to construct a new table. The type parameters define source data type 
 * // and destination type respectively
 * RangeLookupTable.Builder&lt;Double, Integer&gt; builder =
 *         new RangeLookupTable.Builder&lt;Double, Integer&gt;();
 *
 * // Map all source values less than zero to -1
 * Range&lt;Double&gt; r = Range.create(Double.NEGATIVE_INFINITY, false, 0.0, false);
 * builder.add(r, -1);
 *
 * // Map all source values from 0.0 (inclusive) to 1.0 (exclusive) to 1
 * r = Range.create(0.0, true, 1.0, false);
 * builder.add(r, 1);
 *
 * // Map all source values from 1.0 (inclusive) to 2.0 (exclusive) to 2
 * r = Range.create(1.0, true, 2.0, false);
 * builder.add(r, 2);
 * 
 * // Map all source values greater than or equal to 2.0 to 3
 * r = Range.create(2.0, true, Double.POSITIVE_INFINITY, false);
 * builder.add(r, 3);
 * 
 * // Create the lookup table and the JAI operation
 * RangeLookupTable&lt;Double, Integer&gt; table = builder.build();
 *
 * ParameterBlockJAI pb = new ParameterBlockJAI("rangelookup");
 * pb.setSource("source0", srcImage);
 * pb.setParameter("table", table);
 * RenderedImage destImage = JAI.create("rangelookup", pb);
 * </code></pre>
 * 
 * The example above uses a table with complete coverage of all source image
 * values. It is also allowed to have a table that only covers parts of the
 * source domain. In this case, a default destination value can be specified
 * via the "default" parameter to RangeLookup, and this will be returned for
 * all unmatched source values. If the "default" parameter is null (which is 
 * its default setting) unmatched source values will be passed through to the
 * destination image. Note that this may produce surprising results when 
 * converting a float or double source image to an integral destination image
 * due to value truncation and overflow.
 * 
 * <p>
 * <b>Parameters</b>
 * <table border="1">
 * <caption>Parameters</caption>
 * <tr>
 * <th>Name</th><th>Type</th><th>Description</th><th>Default value</th>
 * </tr>
 * <tr>
 * <td>table</td>
 * <td>RangeLookupTable</td>
 * <td>Table mapping source value ranges to destination values</td>
 * <td>NO DEFAULT</td>
 * </tr>
 * <tr>
 * <td>default</td>
 * <td>Number</td>
 * <td>Specifies the value to return for source values that do not map to any
 * ranges in the lookup table. If null, unmatched source values will be passed
 * through to the destination image.
 * </td>
 * <td>null (pass-through)</td>
 * </tr>
 * </table>
 *
 * @see org.jaitools.numeric.Range
 * @see RangeLookupTable
 * 
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions
 * 
 * @since 1.0
 */
public class RangeLookupDescriptor extends OperationDescriptorImpl {

    /** serialVersionUID */
    private static final long serialVersionUID = 6435703646431578734L;

    static final int TABLE_ARG = 0;
    static final int DEFAULT_ARG = 1;

    private static final String[] paramNames = {
        "table", 
        "default"
    };

    private static final Class<?>[] paramClasses = {
        RangeLookupTable.class, 
        Number.class
    };

    private static final Object[] paramDefaults = {
        NO_PARAMETER_DEFAULT,
        (Number) null
    };

    /** Constructor. */
    public RangeLookupDescriptor() {
        super(new String[][]{
                {"GlobalName", "RangeLookup"},
                {"LocalName", "RangeLookup"},
                {"Vendor", "org.jaitools.media.jai"},
                {"Description", "Maps source image value ranges to destination image values"},
                {"DocURL", "http://code.google.com/p/jaitools/"},
                {"Version", "1.0.0"},
        
                {"arg0Desc",
                 String.format("%s - table holding source value ranges mapped to "
                             + "destination values", paramNames[TABLE_ARG])},
                {"arg1Desc",
                 String.format("%s - value to use for unmatched source values "
                             + "(default: null to pass through source values)", paramNames[DEFAULT_ARG])},
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

