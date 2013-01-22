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

package org.jaitools.media.jai.random;

import java.awt.image.DataBuffer;
import java.awt.image.renderable.ParameterBlock;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.registry.RenderedRegistryMode;

import org.jaitools.imageutils.ImageDataType;

/**
 *
 * @author Michael Bedward
 * @since 1.3
 */
public class RandomDescriptor extends OperationDescriptorImpl {
    
    public static final int WIDTH_ARG = 0;
    public static final int HEIGHT_ARG = 1;
    public static final int GENERATORS_ARG = 2;
    
    private static final String[] paramNames = {
        "width",
        "height",
        "generators"
    };
    
    private static final Class<?>[] paramClasses = {
        Number.class,
        Number.class,
        RandomGenerator[].class
    };
    
    private static final Object[] paramDefaults = {
        NO_PARAMETER_DEFAULT,
        NO_PARAMETER_DEFAULT,
        NO_PARAMETER_DEFAULT
    };


    public RandomDescriptor() {
        super(new String[][]{
                {"GlobalName", "Random"},
                {"LocalName", "Random"},
                {"Vendor", "org.jaitools.media.jai"},
                {"Description", "Generates random images"},
                {"DocURL", "http://jaitools.org/"},
                {"Version", "1.0.0"},
        
                {"arg0Desc",
                 String.format("Image width", paramNames[WIDTH_ARG])},
                
                {"arg1Desc",
                 String.format("Image height", paramNames[HEIGHT_ARG])},
                
                {"arg2Desc",
                 String.format("RandomGenerators (either a single object or an array)", 
                         paramNames[GENERATORS_ARG])},
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
         * Generators parameter must be an non-empty array of RandomGenerators.
         */
        obj = pb.getObjectParameter(GENERATORS_ARG);
        if (obj == null) {
            makeMsg(sb, GENERATORS_ARG, "must not be null");
            return false;
        }
        if (!(obj instanceof RandomGenerator ||
              obj instanceof RandomGenerator[])) {
            makeMsg(sb, GENERATORS_ARG, 
                    "must be either a RandomGenerator or an array of RandomGenerators");
            return false;
        }
        
        if (obj instanceof RandomGenerator[]) {
            RandomGenerator[] generators = (RandomGenerator[]) obj;
            if (generators.length < 1) {
                makeMsg(sb, GENERATORS_ARG, "array must have at least one element");
                return false;
            }
            
            ImageDataType firstType = null;
            for (RandomGenerator rg : generators) {
                if (rg == null) {
                    makeMsg(sb, GENERATORS_ARG, "array must not contain nulls");
                    return false;
                }
                
                ImageDataType type = rg.getDataType();
                if (firstType == null) {
                    firstType = type;
                } else {
                    if (type != firstType) {
                        makeMsg(sb, GENERATORS_ARG, "array contains generators of different data types");
                        return false;
                    }
                }
            }
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
