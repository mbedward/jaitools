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
     */
    public void addExcludedRange(Range<Double> exclude);

    /**
     * Retrieve the {@code Ranges} of sample values excluded from calculations.
     *
     * @return the excluded {@code Ranges} or an empty list if no exclusions
     *         are defined
     */
    public List<Range<Double>> getExcludedRanges();

    /**
     * Test whether a sample value will be excluded from calculations by
     * the processor.
     * 
     * @param sample the sample value
     * 
     * @return true if the sample lies within an excluded {@code Range} set 
     *         for this processor; false otherwise
     */
    public boolean isExcluded(Double sample);

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
