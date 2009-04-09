/*
 * Copyright 2009 Michael Bedward
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

import java.util.HashMap;
import java.util.Map;

/**
 * Enum constants for the statistics supported by the {@linkplain SampleStats} and
 * {@linkplain StreamingSampleStats} classes.
 * 
 * @author Michael Bedward
 */
public enum Statistic {

    /** Arithmetic mean */
    MEAN("mean", "arithmetic mean", false),

    /** Exact median */
    MEDIAN("median", "median sample value", false),

    /** Approximate median calculated with the 'remedian' algorithm
     * of Rousseeuw et al. This is only used with {@linkplain StreamingSampleStats}.
     */
    APPROX_MEDIAN("approx. median", "approximate median (remedian algorithm)", true),

    /** Minimum sample value */
    MIN("min", "minimum value", true),

    /** Maximum sample value */
    MAX("max", "maximum value", true),
            
    /** Range (maximum - minimum) */
    RANGE("range", "sample range", true),

    /** Sample standard deviation */
    SDEV("sdev", "sample standard deviation", false),

    /** Sample variance */
    VARIANCE("variance", "sample variance", false);

    private static Map<String, Statistic> lookup;
    static {
        lookup = new HashMap<String, Statistic>();
        for (Statistic stat : Statistic.values()) {
            lookup.put(stat.name, stat);
        }
    }

    private String name;
    private String desc;
    private boolean supportsIntResult;

    /**
     * Private constructor
     */
    private Statistic(String name, String desc, boolean supportsIntResult) {
        this.name = name;
        this.desc = desc;
        this.supportsIntResult = supportsIntResult;
    }

    /**
     * Returns the short name of this statistic
     */
    @Override
    public String toString() {
        return name;
    }

    /**
     * Returns a brief description of the statistic
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Query whether this statistic is can return an integral result
     * when working with integral sample data.
     */
    public boolean supportsIntegralResult() {
        return supportsIntResult;
    }

    /**
     * Get a Statistic constant by name. A case-insensitive
     * lookup is performed.
     * @param name the statistic name
     * @return a Statistic instance or null if the name was
     * not recognized
     */
    public static Statistic get(String name) {
        return lookup.get(name.toLowerCase());
    }
}
