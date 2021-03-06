/* 
 *  Copyright (c) 2013, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.generate;

import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 *
 * @author Michael Bedward
 * @since 1.3
 */
public class GenerateDescriptor extends OperationDescriptorImpl {
    
    public static final int WIDTH_ARG = 0;
    public static final int HEIGHT_ARG = 1;
    public static final int GENERATOR_ARG = 2;
    
    private static final String[] paramNames = {
        "width",
        "height",
        "generator"
    };
    
    private static final Class<?>[] paramClasses = {
        Number.class,
        Number.class,
        Generator.class
    };
    
    private static final Object[] paramDefaults = {
        NO_PARAMETER_DEFAULT,
        NO_PARAMETER_DEFAULT,
        NO_PARAMETER_DEFAULT
    };


    public GenerateDescriptor() {
        super(new String[][]{
                {"GlobalName", "Generate"},
                {"LocalName", "Generate"},
                {"Vendor", "org.jaitools.media.jai"},
                {"Description", "Generates images with random or systematic data"},
                {"DocURL", "http://jaitools.org/"},
                {"Version", "1.0.0"},
        
                {"arg0Desc",
                 String.format("Image width", paramNames[WIDTH_ARG])},
                
                {"arg1Desc",
                 String.format("Image height", paramNames[HEIGHT_ARG])},
                
                {"arg2Desc",
                 String.format("Generator instance to provide image data", 
                         paramNames[GENERATOR_ARG])},
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
    public boolean validateArguments(String modeName, ParameterBlock pb, StringBuffer sb) {
        Object obj;
        
        if (!isValidWidthOrHeight(pb.getObjectParameter(WIDTH_ARG), WIDTH_ARG, sb)) {
            return false;
        }
        
        if (!isValidWidthOrHeight(pb.getObjectParameter(HEIGHT_ARG), HEIGHT_ARG, sb)) {
            return false;
        }
        
        /*
         * Generators parameter must be an non-empty array of Generators.
         */
        obj = pb.getObjectParameter(GENERATOR_ARG);
        if (obj == null) {
            makeMsg(sb, GENERATOR_ARG, "must not be null");
            return false;
        }
        if (!(obj instanceof Generator)) {
            makeMsg(sb, GENERATOR_ARG, "must be an instance of Generator");
            return false;
        }
        
        return true;
    }
    
    
    private boolean isValidWidthOrHeight(Object obj, int argIndex, StringBuffer sb) {
        if (obj == null) {
            makeMsg(sb, argIndex, "must not be null");
            return false;
        }
        if (!(obj instanceof Number)) {
            makeMsg(sb, argIndex, "must be a Number");
            return false;
        }
        if (((Number) obj).intValue() < 1) {
            makeMsg(sb, argIndex, "must be greater than or equal to 1");
            return false;
        }
        
        return true;
    }
    
    private void makeMsg(StringBuffer sb, int argIndex, String msg) {
        sb.append(paramNames[argIndex]).append(' ').append(msg);
    }
}
