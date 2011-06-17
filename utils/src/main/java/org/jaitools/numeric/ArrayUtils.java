/* 
 *  Copyright (c) 2009-2010, Michael Bedward. All rights reserved. 
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

package org.jaitools.numeric;

import java.lang.reflect.Array;
import java.util.Arrays;

/**
 * Provides a number of array operations not present in the standard Java
 * {@linkplain Arrays} class.
 * <p>
 * Based on methods originally in the "BandCombine" operator ported from
 * the GeoTools library (<a href="http://geotools.org">http://geotools.org</a>)
 * and written by Martin Desruisseaux.
 * 
 * @deprecated This class will be removed in the next version of JAITools
 *
 * @author Michael Bedward
 * @author Martin Desruisseaux
 * @since 1.1
 * @version $Id$
 */
public class ArrayUtils {

    /**
     * Resize the given array, truncating or padding with zeroes as necessary.
     * If the length of the input array equals the requested length it will
     * be returned unchanged, otherwise a new array instance is created.
     *
     * @param array the array to resize
     * @param length requested length
     * @return a new array of the requested size
     */
    public static double[] resize(final double[] array, final int length) {
        return resize(array, length);
    }

    /**
     * Resize the given array, truncating or padding with zeroes as necessary.
     * If the length of the input array equals the requested length it will
     * be returned unchanged, otherwise a new array instance is created.
     *
     * @param array the array to resize
     * @param length requested length
     * @return a new array of the requested size
     */
    public static float[] resize(final float[] array, final int length) {
        return resize(array, length);
    }

    /**
     * Resize the given array, truncating or padding with zeroes as necessary.
     * If the length of the input array equals the requested length it will
     * be returned unchanged, otherwise a new array instance is created.
     *
     * @param array the array to resize
     * @param length requested length
     * @return a new array of the requested size
     */
    public static int[] resize(final int[] array, final int length) {
        return resize(array, length);
    }

    /**
     * Generic helper for the {@code resize} methods.
     *
     * @param array input array
     * @param newLength requested length
     * @return a new array of length {@code newLength} and with data from the input
     *         array truncated or padded as necessary; or the input array if its
     *         length is equal to {@code newLength}
     */
    private static <T> T doResize(final T array, final int newLength) {
        if (newLength <= 0) {
            throw new IllegalArgumentException("new array length must be > 0");
        }

        final int len = array == null ? 0 : Array.getLength(array);
        if (newLength != len) {
            T newArray = (T) Array.newInstance(array.getClass().getComponentType(), newLength);
            System.arraycopy(array, 0, newArray, 0, Math.min(len, newLength));
            return newArray;
        } else {
            return array;
        }
    }
}
