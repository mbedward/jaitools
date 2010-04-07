/*
 * Copyright 2009-2010 Michael Bedward
 *
 * This file is part of jai-tools.
 *
 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

package jaitools.numeric;

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
 * @source $URL$
 * @version $Id$
 */
public interface Processor {

    /**
     * Get the {@code Statistics} supported by this processor.
     *
     * @return supported {@code Statistics}
     */
    public Collection<Statistic> getSupported();

    /**
     * Set a {@code Range} of values to exclude from calculations, ie. if a sample
     * value in this range is offered it will be ignored.
     *
     * @param exclude a range of values to exclude
     * @deprecated Please use {@link #addRange(Range, jaitools.numeric.Range.Type))}
     */
    public void addExcludedRange(Range<Double> exclude);

    /**
     * Set a {@code Range} of values to exclude from/include in calculations
     *
     * @param range a range of values to be checked
     * @param rangeType the type of range. Note that you can only add range of the same type.
     * Otherwise, you will get an {@link IllegalArgumentException}.
     */
    public void addRange(Range<Double> range, final Range.Type rangeType);

    /**
     * Set a {@code Range} of values to exclude/include from calculations, ie. if a sample
     * value in this range is offered it will be ignored/accepted. The behavior depends
     * on how the rangesType have been set.
     *
     * @param a range of values to be checked
     */
    public void addRange(Range<Double> range);

    /**
     * Set the {@code Range.Type} of the ranges to be added to the processor.
     * It is worth to point out that this method can be called only one time in case the rangesType
     * haven't been specified at construction time and no ranges have been added yet.
     *
     * @param rangeType the type of range.
     */
    public void setRangesType(Range.Type rangeType);

    /**
     * Get the type of {@code Ranges} being used by this processor.
     *
     * @return the rangesType of this processor
     */
    public Range.Type getRangesType ();

    /**
     * Retrieve the {@code Ranges} of sample values excluded from calculations.
     *
     * @return the excluded {@code Ranges} or an empty list if no exclusions
     *         are defined
     * @deprecated Please use {@link #getRanges()}
     */
    public List<Range<Double>> getExcludedRanges();

    /**
     * Retrieve the {@code Ranges} of sample values excluded from/included in calculations.
     *
     * @return the {@code Ranges} or an empty list if no ranges are defined
     */
    public List<Range<Double>> getRanges();

    /**
     * Test whether a sample value will be excluded from calculations by
     * the processor.
     *
     * @param sample the sample value
     *
     * @return true if the sample lies within an excluded {@code Range} set
     *         for this processor; false otherwise
     * @deprecated Please use {@link #isAccepted}
     */
    public boolean isExcluded(Double sample);

    /**
     * Test whether a sample value will be accepted for calculations by
     * the processor.
     *
     * @param sample the sample value
     *
     * @return true if the sample is accepted in compliance with the ranges settings.
     * 			false otherwise
     */
    public boolean isAccepted(Double sample);

    /**
     * Offer a sample value to the processor.
     *
     * @param sample the sample value
     */
    public void offer(Double sample);

    /**
     * Get the number of samples that have been offered to this processor.
     *
     * @return number of samples offered
     */
    public long getNumOffered();

    /**
     * Get the number of samples that have been accepted by this processor
     * (ie. contributed to the calculations).
     *
     * @return number of samples used for calculations
     */
    public long getNumAccepted();

    /**
     * Get the number of NaN samples passed to the processor
     *
     * @return number of NaN samples
     */
    public long getNumNaN();

    /**
     * Get the value of the statistic calculated by this processor.
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
     * Get the value of all statistics calculated by this processor.
     *
     * @return the calculated statistic(s)
     */
    public Map<Statistic, Double> get();

}
