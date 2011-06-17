/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
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
                    {"DocURL", "http://code.google.com/p/jaitools/"},
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

