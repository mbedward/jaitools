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

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Defines methods that must be implemented by statistics processors
 * working with {@code StreamingSampleStats}.
 *
 * @see Statistic
 * @see StreamingSampleStats
 *
 * @author Michael Bedward
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @version $Id$
 */
public interface Processor {

    /**
     * Gets the {@code Statistics} supported by this processor.
     *
     * @return supported {@code Statistics}
     */
    public Collection<Statistic> getSupported();

    /**
     * Sets a range of values to be considered as NoData. Processors
     * count the number of NoData values offered as samples but exclude them from
     * calculations.
     * <p>
     * NoData ranges take precedence over included / excluded data ranges.
     *
     * @param noData a range of values to be excluded
     */
    public void addNoDataRange(Range<Double> noData);

    /**
     * Convenience method to specify a single value to be considered as NoData.
     *
     * @param noData the value to be treated as NoData
     *
     * @see #addNoDataRange
     */
    public void addNoDataValue(Double noData);

    /**
     * Sets a range of values to exclude from or include in calculations.
     * Note that you can only add ranges of the same type to a Processor.
     *
     * @param range the range
     * @param rangeType the type of range
     * 
     * @throws IllegalArgumentException if {@code rangeType} is different to that
     *         of a previously added range.
     */
    public void addRange(Range<Double> range, final Range.Type rangeType);

    /**
     * Sets a range of values to exclude from or include in from 
     * calculations. Ie. if a sample value in this range is offered it will 
     * be ignored or accepted with the behavior depending on which
     * range type has been set previously.
     *
     * @param range the range
     */
    public void addRange(Range<Double> range);

    /**
     * Sets the type of the ranges to be added to the processor.
     * It is worth to point out that this method can be called only one time in case the rangesType
     * haven't been specified at construction time and no ranges have been added yet.
     *
     * @param rangeType the type of range.
     */
    public void setRangesType(Range.Type rangeType);

    /**
     * Gets the type of ranges being used by this processor.
     *
     * @return the rangesType of this processor
     */
    public Range.Type getRangesType();

    /**
     * Gets the ranges of sample values to be considered as NoData.
     *
     * @return the NoData {@code Ranges} or an empty list if no NoData are defined
     */
    public List<Range<Double>> getNoDataRanges();

    /**
     * Gets the {@code Ranges} of sample values excluded from/included in calculations.
     *
     * @return the {@code Ranges} or an empty list if no ranges are defined
     */
    public List<Range<Double>> getRanges();

    /**
     * Tests whether a sample value will be accepted for calculations by
     * the processor.
     *
     * @param sample the sample value
     *
     * @return true if the sample is accepted in compliance with the ranges settings.
     *         false otherwise
     */
    public boolean isAccepted(Double sample);

    /**
     * Offers a sample value to the processor.
     *
     * @param sample the sample value
     */
    public void offer(Double sample);

    /**
     * Gets the number of samples that have been offered to this processor.
     *
     * @return number of samples offered
     */
    public long getNumOffered();

    /**
     * Gets the number of samples that have been accepted by this processor
     * (ie. contributed to the calculations).
     *
     * @return number of samples used for calculations
     */
    public long getNumAccepted();

    /**
     * Gets the number of NaN samples passed to the processor
     *
     * @return number of NaN samples
     */
    public long getNumNaN();

    /**
     * Gets the number of NoData samples passed to the processor. This count
     * includes values in user-specified NoData ranges as well as Double.NaN values.
     *
     * @return number of NoData samples
     *
     * @see #addNoDataRange(org.jaitools.numeric.Range)
     * @see #addNoDataValue(java.lang.Double)
     */
    public long getNumNoData();

    /**
     * Gets the value of the statistic calculated by this processor.
     *
     * @param stat the specified statistic
     *
     * @return the value of the statistic if it has been calculated or
     *         Double.NaN otherwise
     *
     * @throws IllegalArgumentException if {@code stat} is not supported
     *         by this processor
     */
    public Double get(Statistic stat) throws IllegalArgumentException;

    /**
     * Gets the value of all statistics calculated by this processor.
     *
     * @return the calculated statistic(s)
     */
    public Map<Statistic, Double> get();
}
