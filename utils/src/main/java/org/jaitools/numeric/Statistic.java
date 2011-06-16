/* 
 *  Copyright (c) 2009-2011, Michael Bedward. All rights reserved. 
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

import java.util.HashMap;
import java.util.Map;

/**
 * Constants for the statistics supported by the {@linkplain SampleStats} and
 * {@linkplain StreamingSampleStats} classes.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
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

    /** Sum of valid values */
    SUM("sum", "sum of valid values", true),
    
    /** Sample variance */
    VARIANCE("variance", "sample variance", false);

    private static final Map<String, Statistic> lookup;
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
     * @return 
     */
    public String getDescription() {
        return desc;
    }

    /**
     * Tests if this statistic can return an integral result
     * when working with integral sample data.
     * 
     * @return {@code true} if an integral result can be returned;
     *         {@code false} otherwise
     */
    public boolean supportsIntegralResult() {
        return supportsIntResult;
    }

    /**
     * Gets a Statistic constant by name (case-insensitive).
     * 
     * @param name the statistic name
     * @return a Statistic instance or null if the name was
     *         was not matched
     */
    public static Statistic get(String name) {
        return lookup.get(name.toLowerCase());
    }
}
