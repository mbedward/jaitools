/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
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
/*
 *    GeoTools - The Open Source Java GIS Toolkit
 *    http://geotools.org
 * 
 *    (C) 2003-2008, Open Source Geospatial Foundation (OSGeo)
 *
 *    This library is free software; you can redistribute it and/or
 *    modify it under the terms of the GNU Lesser General Public
 *    License as published by the Free Software Foundation;
 *    version 2.1 of the License.
 *
 *    This library is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *    Lesser General Public License for more details.
 */
package jaitools.media.jai.combine;

// J2SE dependencies
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;



/**
 * The operation descriptor for the {@link Combine} operation. While this descriptor declares
 * to support 0 {@link RenderedImage} sources, an arbitrary amount of sources can really be
 * specified. The "0" should be understood as the <em>minimal</em> number of sources required.
 *
 * @since 2.1
 * @version $Id$
 * @author Remi Eve
 * @author Martin Desruisseaux (IRD)
 */
public class CombineDescriptor extends OperationDescriptorImpl {

    private static final long serialVersionUID = 4398739920268305751L;

    /**
     * The operation name, which is {@value}.
     */
    public static final String OPERATION_NAME = "Combine";

    /**
     * Constructs the descriptor.
     */
    public CombineDescriptor() {
        super(new String[][]{{"GlobalName",  OPERATION_NAME},
                             {"LocalName",   OPERATION_NAME},
                             {"Vendor",      "jaitools.media.jai"},
                             {"Description", "Combine rendered images using a linear relation."},
                             {"DocURL",      "http://www.geotools.org/"}, 
                             {"Version",     "1.0"},
                             {"arg0Desc",    "The coefficients for linear combinaison as a matrix."},
                             {"arg1Desc",    "An optional transform to apply on sample values "+
                                             "before the linear combinaison."}},
              new String[]   {RenderedRegistryMode.MODE_NAME}, 0,        // Supported modes
              new String[]   {"matrix", "transform"},                    // Parameter names
              new Class []   {double[][].class, CombineTransform.class}, // Parameter classes
              new Object[]   {NO_PARAMETER_DEFAULT, null},               // Default value
              null                                                       // Valid parameter values
        );
    }

    /**
     * Returns {@code true} if this operation supports the specified mode, and
     * is capable of handling the given input source(s) for the specified mode. 
     *
     * @param modeName The mode name (usually "Rendered").
     * @param param The parameter block for the operation to performs.
     * @param message A buffer for formatting an error message if any.
     */
    @Override
    protected boolean validateSources(final String      modeName,
                                      final ParameterBlock param,
                                      final StringBuffer message)
    {
        if (super.validateSources(modeName, param, message)) {
            for (int i=param.getNumSources(); --i>=0;) {
                final Object source = param.getSource(i);
                if (!(source instanceof RenderedImage)) {
                    message.append("Wrong class for source:"+source.getClass().toString());
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * Returns {@code true} if the parameters are valids. This implementation check
     * that the number of bands in the source src1 is equals to the number of bands of 
     * source src2.
     *
     * @param modeName The mode name (usually "Rendered").
     * @param param The parameter block for the operation to performs.
     * @param message A buffer for formatting an error message if any.
     */
    @Override
    protected boolean validateParameters(final String      modeName,
                                         final ParameterBlock param,
                                         final StringBuffer message)
    {
        if (!super.validateParameters(modeName, param, message))  {
            return false;
        }
        final double[][] matrix = (double[][]) param.getObjectParameter(0);
        int numSamples = 1; // Begin at '1' for the offset value.
        for (int i=param.getNumSources(); --i>=0;) {
            numSamples += ((RenderedImage) param.getSource(i)).getSampleModel().getNumBands();
        }
        for (int i=0; i<matrix.length; i++) {
            if (matrix[i].length != numSamples) {
                message.append("Wrong number of samples provided:"+numSamples);
                return false;
            }
        }
        return true;
    }
}
