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

package jaitools.media.jai.kernelstats;

import java.util.HashMap;
import java.util.Map;

/**
 * Constants for the statistics supported by {@linkplain KernelStatsOpImage}
 * 
 * @author Michael Bedward
 */
public enum KernelStatistic {

    /** Arithmetic mean */
    MEAN("mean", "arithmetic mean", false),

    /** Median sample value */
    MEDIAN("median", "median sample value", false),

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

    private static Map<String, KernelStatistic> lookup;
    static {
        lookup = new HashMap<String, KernelStatistic>();
        for (KernelStatistic stat : KernelStatistic.values()) {
            lookup.put(stat.name, stat);
        }
    }

    private String name;
    private String desc;
    private boolean supportsIntResult;

    /**
     * Private constructor
     */
    private KernelStatistic(String name, String desc, boolean supportsIntResult) {
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
     * Get a KernelStatistic constant by name. A case-insensitive
     * lookup is performed.
     * @param name the statistic name
     * @return a KernelStatistic instance or null if the name was
     * not recognized
     */
    public static KernelStatistic get(String name) {
        return lookup.get(name.toLowerCase());
    }
}
