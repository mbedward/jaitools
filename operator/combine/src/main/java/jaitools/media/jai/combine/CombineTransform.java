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


/**
 * Transforms the sample values for one pixel during a "{@link Combine Combine}" operation.
 * The method {@link #transformSamples} is invoked by {@link Combine#computeRect
 * Combine.computeRect(...)} just before the sample values are combined as
 *
 * <code>values[0]*row[0] + values[1]*row[1] + values[2]*row[2] + ... + row[sourceBands]</code>.
 *
 * This interface provides a hook where non-linear transformations can be performed before the
 * linear one. For example, the {@code transformSamples} method could substitutes some
 * values by their logarithm.
 *
 * @since 2.1
 * @version $Id$
 * @author Martin Desruisseaux (IRD)
 */
public interface CombineTransform {
    /**
     * Transforms the sample values for one pixel before the linear combinaison.
     *
     * @param values The sampel values to transformation.
     *               Transformation are performed in-place.
     */
    public abstract void transformSamples(final double[] values);

    /**
     * Returns {@code true} if the transformation performed by {@link #transformSamples}
     * do not depends on the ordering of samples in the {@code values} array. This method
     * can returns {@code true} if the {@code transformSamples(double[])} implementation
     * meet the following conditions:
     *
     * <ul>
     *   <li>The transformation is separable, i.e. the output value {@code values[i]} depends
     *       only on the input value {@code values[i]} for all <var>i</var>.</li>
     *   <li>The transformation do not depends on the value of the index <var>i</var>.
     * </ul>
     *
     * For example, the following implementations meets the above mentioned conditions:
     *
     * <blockquote><pre>
     * for (int i=0; i<values.length; i++) {
     *     values[i] = someFunction(values[i]);
     * }
     * </pre></blockquote>
     *
     * A {@code true} value will allows some optimisations inside the
     * {@link Combine#computeRect Combine.computeRect(...)} method. This method
     * may conservatly returns {@code false} if this information is unknow.
     */
    public abstract boolean isSeparable();
}
