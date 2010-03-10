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

import jaitools.CollectionFactory;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Abstract base class for statistics processors used with {@code StreamingSampleStats}.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public abstract class AbstractProcessor implements Processor {

    protected long numOffered;
    protected long numAccepted;

    private List<Range<Double>> excludedRanges;

    /**
     * Default constructor.
     */
    public AbstractProcessor() {
        excludedRanges = CollectionFactory.list();
        numOffered = numAccepted = 0;
    }

    /**
     * {@inheritDoc}
     *
     * Adding a {@code Range} that overlaps with one or more existing
     * {@code Ranges} is permitted.
     */
    public void addExcludedRange(Range<Double> exclude) {
        if (exclude != null) {
            // copy the input Range defensively
            excludedRanges.add(new Range<Double>(exclude));
        }
    }

    /**
     * {@inheritDoc}
     */
    public List<Range<Double>> getExcludedRanges() {
        return Collections.unmodifiableList(excludedRanges);
    }

    /**
     * {@inheritDoc}.
     * Null and Double.NaN values are excluded by default.
     */
    public boolean isExcluded(Double sample) {
        if (sample == null || sample.isNaN()) {
            return true;
        }

        for (Range<Double> r : excludedRanges) {
            if (r.contains(sample)) {
                return true;
            }
        }

        return false;
    }

    /**
     * {@inheritDoc}
     */
    public final long getNumOffered() {
        return numOffered;
    }

    /**
     * {@inheritDoc}
     */
    public final long getNumAccepted() {
        return numAccepted;
    }

    /**
     * {@inheritDoc}
     */
    public void offer(Double sample) {
        numOffered++;
        if (update(sample)) {
            numAccepted++ ;
        }
    }

    /**
     * {@inheritDoc}
     */
    public Map<Statistic, Double> get() {
        Map<Statistic, Double> stats = new HashMap<Statistic, Double>();
        for (Statistic s : getSupported()) {
            stats.put(s, get(s));
        }
        return stats;
    }

    /**
     * Process a sample value that has been offered by the client.
     *
     * @param sample the sample value
     * 
     * @return true if the sample is accepted (ie. used for calculations);
     *         false otherwise
     */
    protected abstract boolean update(Double sample);

}
