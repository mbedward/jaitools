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

import org.jaitools.imageutils.ImageDataType;

/**
 * Implemented by data generating classes used with the "Generate" operation.
 * 
 * @see GenerateDescriptor
 *
 * @author Michael Bedward
 * @since 1.3
 */
public interface Generator {
    
    /**
     * Gets the image data type supported by this generator. Implementing
     * classes must ensure that values returned by {@linkplain #getValues(int, int)}
     * conform to this data type.
     * 
     * @return image data type
     */
    ImageDataType getDataType();
    
    /**
     * Gets the number of bands that this generator supports.
     * 
     * @return number of bands (must be greater than zero)
     */
    int getNumBands();
    
    /**
     * Gets band value(s) for the given image location.
     * 
     * @param imageX image X ordinate
     * @param imageY image Y ordinate
     * 
     * @return image values
     */
    Number[] getValues(int imageX, int imageY);
}
