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
package org.jaitools.imageutils;

import java.awt.image.DataBuffer;
import java.util.Map;
import org.jaitools.CollectionFactory;

/**
 * Associates {@linkplain DataBuffer} data type constants with 
 * Number classes.
 * 
 * @author michael
 */
public enum ImageDataType {

    BYTE("Byte", DataBuffer.TYPE_BYTE, Byte.class),
    SHORT("Short", DataBuffer.TYPE_SHORT, Short.class),
    USHORT("Unsigned short", DataBuffer.TYPE_USHORT, Short.class),
    INT("Integer", DataBuffer.TYPE_INT, Integer.class),
    FLOAT("Float", DataBuffer.TYPE_FLOAT, Float.class),
    DOUBLE("Double", DataBuffer.TYPE_DOUBLE, Double.class);
    
    private static final Map<Integer, ImageDataType> codeLookup = CollectionFactory.map();
    static {
        for (ImageDataType t : ImageDataType.values()) {
            codeLookup.put(t.dataBufferCode, t);
        }
    }
    
    private static final Map<Class<? extends Number>, ImageDataType> classLookup = 
            CollectionFactory.map();
    static {
        for (ImageDataType t : ImageDataType.values()) {
            // Leave USHORT out of the class lookup
            if (t != USHORT) {
                classLookup.put(t.clazz, t);
            }
        }
    }
    
    private final String displayName;
    private final int dataBufferCode;
    private final Class<? extends Number> clazz;
    
    private ImageDataType(String displayName, int dataBufferCode, Class<? extends Number> clazz) {
        this.displayName = displayName;
        this.dataBufferCode = dataBufferCode;
        this.clazz = clazz;
    }
    
    /**
     * Gets the DataBuffer integer constant for this data type.
     */
    public int getDataBufferType() {
        return dataBufferCode;
    }

    /**
     * Gets the class associated with this data type.
     */
    public Class<? extends Number> getDataClass() {
        return clazz;
    }
    
    /**
     * Matches a DataBuffer type constant.
     * 
     * @param typeCode the constant value to match
     * 
     * @return the matching ImageDataType
     * @throws IllegalArgumentException if no match exists
     */
    public static ImageDataType getForDataBufferType(int typeCode) {
        ImageDataType t = codeLookup.get(typeCode);
        if (t == null) {
            throw new IllegalArgumentException("No match for type code " + typeCode);
        }
        return t;
    }
    
    /**
     * Matches a data class.
     * 
     * @param clazz the data class to match
     * 
     * @return the matching ImageDataType
     * @throws IllegalArgumentException if the argument is null or if no match exists
     */
    public static ImageDataType getForClass(Class<? extends Number> clazz) {
        if (clazz == null) {
            throw new IllegalArgumentException("Null argument");
        }
        
        ImageDataType t = classLookup.get(clazz);
        if (t == null) {
            throw new IllegalArgumentException("No match for data class " + clazz.getName());
        }
        return t;
    }
    
    @Override
    public String toString() {
        return displayName;
    }
}
