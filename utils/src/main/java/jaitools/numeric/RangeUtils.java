/*
 * Copyright 2010 Michael Bedward
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

import java.util.ArrayList;
import java.util.List;

/**
 * Provides static helper methods to transform, sort and merge {@code Range} objects.
 *
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class RangeUtils {

    public static <T extends Number & Comparable> List<Range<T>> createComplement(Range<T> range) {
        List<Range<T>> complements = new ArrayList<Range<T>>();

        // special case: point range
        if (range.isPoint()) {
            if (range.isMinInf() || range.isMinNegInf()) {
                complements.add(Range.create((T)null, true, (T)null, true));
                
            } else {  // finite point
                complements.add(Range.create(null, true, range.getMin(), false));
                complements.add(Range.create(range.getMin(), false, null, true));
            }

        } else {  // interval range
            if (range.isMinClosed()) {
                complements.add(Range.create(null, true, range.getMin(), !range.isMinIncluded()));
            }
            if (range.isMaxClosed()) {
                complements.add(Range.create(range.getMax(), !range.isMaxIncluded(), null, true));
            }
        }

        return complements;
    }
}
