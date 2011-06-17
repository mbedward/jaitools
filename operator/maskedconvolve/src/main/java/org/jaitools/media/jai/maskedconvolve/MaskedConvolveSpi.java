/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.maskedconvolve;

import java.awt.image.renderable.RenderedImageFactory;

import javax.media.jai.OperationDescriptor;
import javax.media.jai.OperationRegistry;
import javax.media.jai.OperationRegistrySpi;
import javax.media.jai.registry.RenderedRegistryMode;

/**
 * OperationRegistrySpi implementation to register the "MaskedConvolve"
 * operation and its associated image factories.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class MaskedConvolveSpi implements OperationRegistrySpi {

    /** The name of the product to which these operations belong. */
    private String productName = "org.jaitools.media.jai.maskedconvolve";
 
    /** Default constructor. */
    public MaskedConvolveSpi() {}

    /**
     * Registers the MaskedConvolve operation and its
     * associated image factories across all supported operation modes.
     *
     * @param registry The registry with which to register the operations
     * and their factories.
     */
    public void updateRegistry(OperationRegistry registry) {
        OperationDescriptor op = new MaskedConvolveDescriptor();
        registry.registerDescriptor(op);
        String descName = op.getName();
        
        RenderedImageFactory rif = new MaskedConvolveRIF();

        registry.registerFactory(RenderedRegistryMode.MODE_NAME,
                                 descName,
                                 productName,
                                 rif);

    }
}
