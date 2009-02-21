/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.jiffle.util;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 *
 * @author Michael Bedward and Murray Ellis
 */
public class SummaryStats {
    
    /**
     * Return the maximum of a sample of values. Double.NaN
     * elements are ignored.
     * @param values  sample data
     * @return max value or Double.NaN if the sample is empty
     */
    public static double max(Double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return values[0];
        }
        
        SortedSet<Double> set = CollectionFactory.newTreeSet();
        set.addAll(Arrays.asList(values));
        set.remove(Double.NaN);
        return set.last();
    }

    /**
     * Return the minimum of a sample of values. Double.NaN elements
     * are ignored.
     * @param values  sample data
     * @return min value or Double.NaN if the sample is empty
     */
    public static double min(Double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return values[0];
        }
        
        SortedSet<Double> set = CollectionFactory.newTreeSet();
        set.addAll(Arrays.asList(values));
        set.remove(Double.NaN);
        return set.first();
    }

    /**
     * Calculate the median of a sample of values. For a sample with an odd
     * number of elements the median is the mid-point value of the 
     * sorted sample. For an even number of elements it is the mean of
     * the two values on either side of the mid-point. Double.NaN
     * elements are ignored.
     * 
     * @param values sample data (need not be pre-sorted)
     * @return median value or Double.NaN if the sample is empty
     */
    @SuppressWarnings("empty-statement")
    public static double median(Double[] values) {
        if (values == null) {
            return Double.NaN;
        }
        
        List<Double> list = CollectionFactory.newList();
        list.addAll(Arrays.asList(values));
        while (list.remove(Double.NaN)) /* deliberately empty */ ;
        
        if (list.isEmpty()) {
            return Double.NaN;
        } else if (list.size() == 1) {
            return list.get(0);
        } else if (list.size() == 2) {
            return (list.get(0) + list.get(1)) / 2;
        }
        
        Collections.sort(list);
        
        int midHi = list.size() / 2;
        int midLo = midHi - 1;
        boolean even = list.size() % 2 == 0;

        Double result = 0.0d;
        int k = 0;
        for (Double val : values) {
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
     * @return calculated mode or Double.NaN if the sample is empty
     */
    @SuppressWarnings("empty-statement")
    public static double mode(Double[] values) {
        if (values == null) {
            return Double.NaN;
        }
        
        List<Double> list = CollectionFactory.newList();
        list.addAll(Arrays.asList(values));
        while (list.remove(Double.NaN)) /* deliberately empty */ ;
        
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
     * @param values input values
     * @return the range or Double.NaN if the set is empty
     */
    public static double range(Double[] values) {
        if (values == null || values.length == 0) {
            return Double.NaN;
        } else if (values.length == 1) {
            return 0d;
        }
        
        SortedSet<Double> set = CollectionFactory.newTreeSet();
        set.addAll(Arrays.asList(values));
        set.remove(Double.NaN);
        return set.last() - set.first();
    }

}
