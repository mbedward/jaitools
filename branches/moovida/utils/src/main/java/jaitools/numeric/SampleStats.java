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

import jaitools.CollectionFactory;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * A collection of static methods to calculate summary statistics for
 * a sample of double-valued data. This class is used by both Jiffle
 * and the KernelStats operator.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class SampleStats {
    
    /**
     * Return the maximum of a sample of values.
     *
     * @param values  sample data
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return max value or Double.NaN if the sample is empty
     */
    public static double max(Double[] values, boolean ignoreNaN) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return values[0];
        }
        
        SortedSet<Double> set = CollectionFactory.newTreeSet();
        set.addAll(Arrays.asList(values));
        if (ignoreNaN) set.remove(Double.NaN);
        return set.last();
    }

    /**
     * Return the mean of a sample of values.
     *
     * @param values  sample data
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return mean value or Double.NaN if the sample is empty
     */
    public static double mean(Double[] values, boolean ignoreNaN) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return values[0];
        }

        double sum = 0.0d;
        int n = 0;
        for (Double val : values) {
            if (val.isNaN()) {
                if (!ignoreNaN) return Double.NaN;
            } else {
                sum += val;
                n++ ;
            }
        }

        return sum / n;
    }

    /**
     * Return the minimum of a sample of values.
     *
     * @param values  sample data
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return min value or Double.NaN if the sample is empty
     */
    public static double min(Double[] values, boolean ignoreNaN) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return values[0];
        }
        
        SortedSet<Double> set = CollectionFactory.newTreeSet();
        set.addAll(Arrays.asList(values));
        if (ignoreNaN) set.remove(Double.NaN);
        return set.first();
    }

    /**
     * Calculate the median of a sample of values. For a sample with an odd
     * number of elements the median is the mid-point value of the 
     * sorted sample. For an even number of elements it is the mean of
     * the two values on either side of the mid-point. 
     * 
     * @param values sample data (need not be pre-sorted)
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return median value or Double.NaN if the sample is empty
     */
    @SuppressWarnings("empty-statement")
    public static double median(Double[] values, boolean ignoreNaN) {
        if (values == null) {
            return Double.NaN;
        }
        
        List<Double> nonNaNValues = CollectionFactory.newList();
        nonNaNValues.addAll(Arrays.asList(values));
        if (ignoreNaN) {
            while (nonNaNValues.remove(Double.NaN)) /* deliberately empty */ ;
        }
        
        if (nonNaNValues.isEmpty()) {
            return Double.NaN;
        } else if (nonNaNValues.size() == 1) {
            return nonNaNValues.get(0);
        } else if (nonNaNValues.size() == 2) {
            return (nonNaNValues.get(0) + nonNaNValues.get(1)) / 2;
        }
        
        Collections.sort(nonNaNValues);
        
        int midHi = nonNaNValues.size() / 2;
        int midLo = midHi - 1;
        boolean even = nonNaNValues.size() % 2 == 0;

        Double result = 0.0d;
        int k = 0;
        for (Double val : nonNaNValues) {
            if (k == midHi) {
                if (!even) {
                    return val;
                } else {
                    result += val;
                    return result / 2;
                }
            } else if (even && k == midLo) {
                result += val;
            }
            k++ ;
        }
        
        return 0;  // to suppress compiler warning
    }
    
    /**
     * Calculate the emperical mode (highest frequency value) of a sample of values.
     * Double.NaN values are ignored. If more than one data value occurs with
     * maximum frequency the following tie-break rules are used:
     * <ul>
     * <li> for an odd number of tied values, return their median
     * <li> for an even number of tied values, return the value below
     *      the mid-point of the sorted list of tied values
     * </ul>
     * This ensures that the calculated mode occurs in the sample data.
     * Whether or not the mode is meaningful for the sample is up to the user !
     * 
     * @param values sample data
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return calculated mode or Double.NaN if the sample is empty
     */
    @SuppressWarnings("empty-statement")
    public static double mode(Double[] values, boolean ignoreNaN) {
        if (values == null) {
            return Double.NaN;
        }
        
        List<Double> list = CollectionFactory.newList();
        list.addAll(Arrays.asList(values));
        if (ignoreNaN) {
            while (list.remove(Double.NaN)) /* deliberately empty */ ;
        }
        
        if (list.isEmpty()) {
            return Double.NaN;
        } else if (list.size() == 1) {
            return list.get(0);
        }
        
        Collections.sort(list);
        
        List<Double> uniqueValues = CollectionFactory.newList();
        List<Integer> freq = CollectionFactory.newList();
        
        Double curVal = list.get(0);
        int curFreq = 1;
        int maxFreq = 1;
        
        for (int i = 1; i < list.size(); i++) {
            if (DoubleComparison.dcomp(curVal, list.get(i)) == 0) {
                curFreq++ ;
            } else {
                uniqueValues.add(curVal);
                freq.add(curFreq);
                curVal = list.get(i);
                if (curFreq > maxFreq) maxFreq = curFreq;
                curFreq = 1;
            }
        }
        uniqueValues.add(curVal);
        freq.add(curFreq);
        if (curFreq > maxFreq) maxFreq = curFreq;
        
        List<Integer> maxFreqIndices = CollectionFactory.newList();
        int k = 0;
        for (Integer f : freq) {
            if (f == maxFreq) {
                maxFreqIndices.add(k);
            }
            k++ ;
        }
        
        if (maxFreqIndices.size() == 1) {
            return uniqueValues.get(maxFreqIndices.get(0));
        }

        boolean even = maxFreqIndices.size() % 2 == 0;
        int i = maxFreqIndices.size() / 2;
        if (even) i-- ;
        return uniqueValues.get(maxFreqIndices.get(i));
    }

    /**
     * Return the range (max - min) of a set of values
     *
     * @param values input values
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return the range or Double.NaN if the set is empty
     */
    public static double range(Double[] values, boolean ignoreNaN) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return 0d;
        }
        
        SortedSet<Double> set = CollectionFactory.newTreeSet();
        set.addAll(Arrays.asList(values));
        if (ignoreNaN) set.remove(Double.NaN);
        return set.last() - set.first();
    }

    /**
     * Calculate sample variance using the running sample algorithm
     * of Welford (1962) described by Knuth in <i>The Art of Computer
     * Programming (3rd ed)</i> Vol.2 p.232
     */
    public static double variance(Double[] values, boolean ignoreNaN) {
        if (values.length < 2) {
            return Double.NaN;
        }

        double mNew, mOld = 0.0d, s = 0.0d;

        int n = 0;
        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) {
                if (!ignoreNaN) {
                    return Double.NaN;
                }
                
            } else {
                n++;
                if (n == 1) {
                    mNew = mOld = values[i];
                } else {
                    mNew = mOld + (values[i] - mOld) / n;
                    s = s + (values[i] - mOld) * (values[i] - mNew);
                    mOld = mNew;
                }
            }
        }

        if (n > 1) {
            return s / (n - 1);
        } else if (n == 1) {
            return 0.0d;
        } else {
            return Double.NaN;
        }
    }

    /**
     * Calculate sample standard deviation. This is a convenience
     * method that calls {@linkplain #variance(java.lang.Double[], boolean) }
     * and returns the square-root of the result
     *
     * @param values sample values
     * @param ignoreNaN specifies whether to ignore NaN values
     * @return sample standard deviation as a double
     */
    public static double sdev(Double[] values, boolean ignoreNaN) {
        double var = variance(values, ignoreNaN);
        return (Double.isNaN(var) ? Double.NaN : Math.sqrt(var));
    }

    /**
     * Calculate the sum of the values passed.
     */
    public static double sum(Double[] values, boolean ignoreNaN) {
        double sum = 0.0d;

        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) {
                if (!ignoreNaN) {
                    return Double.NaN;
                }
            } else {
              sum = sum + values[i];  
            }
        }
        
        return sum;
    }

    /**
     * Calculate the active values passed (not NaN).
     */
    public static double activecells(Double[] values, boolean ignoreNaN) {
        if (!ignoreNaN) {
            return values.length;
        }
        double count = 0;
        for (int i = 0; i < values.length; i++) {
            if (!Double.isNaN(values[i])) {
                count++;  
            }
        }
        
        return count;
    }
}
