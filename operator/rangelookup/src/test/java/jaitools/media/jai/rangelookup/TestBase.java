/*
 * Copyright 2011 Michael Bedward
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

package jaitools.media.jai.rangelookup;

import jaitools.numeric.Range;

/**
 * Base class for unit tests.
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public abstract class TestBase {
    /**
     * Creates a lookup table.
     * @param breaks array of breakpoints for source image values
     * @param values array of lookup values for destination image value
     * 
     * @return the lookup table
     */
    protected <T extends Number & Comparable<? super T>, 
             U extends Number & Comparable<? super U>> 
            RangeLookupTable<T, U> createTable(T[] breaks, U[] values) {
        
        final int N = breaks.length;
        if (values.length != N + 1) {
            throw new IllegalArgumentException(
                    "values array length should be breaks array length + 1");
        }
        
        RangeLookupTable<T, U> table = new RangeLookupTable<T, U>();
        Range<T> r;
        
        r = Range.create(null, false, breaks[0], false);
        table.add(r, values[0]);
        
        for (int i = 1; i < N; i++) {
            r = Range.create(breaks[i-1], true, breaks[i], false);
            table.add(r, values[i]);
        }
        
        r = Range.create(breaks[N-1], true, null, false);
        table.add(r, values[N]);
        
        return table;
    }

}
