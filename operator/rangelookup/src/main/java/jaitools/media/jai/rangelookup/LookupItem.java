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
 * Used by {@link RangeLookupTable} to define a single lookup range and return value.
 * 
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions
 * @since 1.1
 * @version $Id$
 */
public class LookupItem<T extends Number & Comparable<? super T>, U extends Number & Comparable<? super U>> {
    
    /** Lookup range. Package-access field. */
    Range<T> range;
    
    /** Return value. Package-access field. */
    U value;

    /**
     * Creates a new instance.
     * 
     * @param range the lookup range
     * @param value the return value
     * @throws IllegalArgumentException if either arg is {@code null}
     */
    public LookupItem(Range<T> range, U value) {
        if (range == null || value == null) {
            throw new IllegalArgumentException("Both range and value must be non-null");
        }
        this.range = range;
        this.value = value;
    }
    
    /**
     * Gets a copy of this item's range. 
     * 
     * @return the range
     */
    public Range<T> getRange() {
        return new Range<T>(range);
    }
    
    /**
     * Gets this item's return value.
     * @return 
     */
    public U getValue() {
        return value;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((value == null) ? 0 : value.hashCode());
        result = prime * result + ((range == null) ? 0 : range.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        LookupItem other = (LookupItem) obj;
        if (value == null) {
            if (other.value != null) {
                return false;
            }
        } else if (!value.equals(other.value)) {
            return false;
        }
        if (range == null) {
            if (other.range != null) {
                return false;
            }
        } else if (!range.equals(other.range)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return range.toString() + " => " + value;
    }
    
}
