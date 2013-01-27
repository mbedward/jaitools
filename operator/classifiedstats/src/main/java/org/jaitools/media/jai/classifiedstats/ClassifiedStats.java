/* 
 *  Copyright (c) 2009-2010, Daniele Romagnoli. All rights reserved. 
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

package org.jaitools.media.jai.classifiedstats;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.jaitools.CollectionFactory;
import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;
import org.jaitools.numeric.StreamingSampleStats;


/**
 * Holds the results of the ClassifiedStats operator.
 * An instance of this class is stored as a property of the destination
 * image.
 * <p>
 *
 * @see Result
 * @see ClassifiedStatsDescriptor
 *
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.2
 */
public class ClassifiedStats {

    /**
     * List of Map of results.
     *  - Elements of the list represent results by pivots. (no pivot means list made of a single element).
     *    Each element of the list is a map of grouped results (<MultiKey, List<Result>>)
     *  
     *  see {@link ClassifiedStatsDescriptor} for more info about the concept of Pivot classifier.
     */
    private List<Map<MultiKey, List<Result>>> results;
    
    /**
     * Constructor. Package-private; called by ClassifiedStatsOpImage.
     */
    ClassifiedStats() {
        Map<MultiKey, List<Result>> map = new HashMap<MultiKey, List<Result>>();
        results = new ArrayList<Map<MultiKey,List<Result>>>();
        results.add(map);
    }

    /**
     * Copy constructor. Used by the chaining methods such as {@linkplain #band(int)}.
     *
     * @param src source object
     * @param band selected image band or {@code null} for all bands
     * @param pivot selected pivot index or {@code null} for using the first element
     * @param stat selected statistic or {@code null} for all statistics
     * @param ranges selected ranges or {@code null} for all ranges
     */
    private ClassifiedStats(ClassifiedStats src, Integer band, Integer pivot, Statistic stat, List<Range<Double>> ranges) {
        this();
        Map<MultiKey, List<Result>> group = null;
        
        // Results are firstly grouped by pivot index
        // When classifying without pivot we use the first element of the list (at index 0)
        int pivotIndex = pivot == null ? 0 : pivot; 
        if (pivotIndex < results.size()){
            // Get the results group for the specified pivot index
            group = results.get(pivotIndex);
            if (group == null){
                // In case we haven't a group yet, for that pivot, add it
                group = new HashMap<MultiKey, List<Result>>();
                results.add(pivotIndex, group);
            }
        } else {
            // The pivot index is greater than the current results list size.
            // add a new group
            group = new HashMap<MultiKey, List<Result>>();
            results.add(group);
        }
        
        // Get the keySet related to that pivot
        Set<MultiKey> ks = src.results.get(pivotIndex).keySet();
        
        // iterate over the keys
        Iterator<MultiKey> it = ks.iterator();
        while (it.hasNext()){
            MultiKey mk = it.next();

            // iterate over the results for each key
            List<Result> rs = src.results.get(pivotIndex).get(mk);
            List<Result> rsCopy = CollectionFactory.list();
            for (Result r: rs){
                if ((band == null || r.getImageBand() == band) &&
                    (stat == null || r.getStatistic() == stat)) {
                    if (ranges == null || ranges.isEmpty()) {
                        rsCopy.add(r);
                    } else {
                        if (r.getRanges().containsAll(ranges)) {
                            rsCopy.add(r);
                        } else {
                            for (Range<Double> range : ranges) {
                                if (r.getRanges().contains(range)) {
                                    rsCopy.add(r);
                                }
                            }
                        }
                    }
                }
            }
            group.put(mk, rsCopy);
        }
    }

    /**
     * Store the results for the given band, pivotIndex, classificationKey, ranges from the provided stats
     *  
     * Package-private method used by {@code ClassifiedStatsOpImage}.
     *  
     * @param band selected image band 
     * @param pivotIndex selected pivot index 
     * @param classificationKey the keys referring to the results to be set
     * @param stats input streamingSampleStats to be queried to populate results
     * @param ranges selected ranges 
     */
    void setResults(final int band, final int pivotIndex, final MultiKey classificationKey, 
            final StreamingSampleStats stats, final List<Range<Double>> ranges) {

        //First preliminary check on an already populated group of results for that pivot
        Map<MultiKey, List<Result>> group = null;
        if (pivotIndex < results.size()){
            group = results.get(pivotIndex);
            if (group == null){
                group = new HashMap<MultiKey, List<Result>>();
                results.add(pivotIndex, group);
            }
        } else {
            group = new HashMap<MultiKey, List<Result>>();
            results.add(group);
        }
        List<Result> rs = group.get(classificationKey);
        if (rs == null) {
            rs = CollectionFactory.list();    
        }
        
        //Populate the results list by scanning for statistics.
        for (Statistic s : stats.getStatistics()) {
            Result r = new Result(band, s, ranges,
                    stats.getStatisticValue(s),
                    stats.getNumOffered(s),
                    stats.getNumAccepted(s),
                    stats.getNumNaN(s),
                    stats.getNumNoData(s), classificationKey);
            rs.add(r);
        }
        group.put(classificationKey, rs);
    }

    /**
     * Store the results for the given band, pivotIndex, classificationKey, ranges from the provided stats
     *  
     * Package-private method used by {@code ClassifiedStatsOpImage}.
     * 
     * @param band selected image band 
     * @param pivotIndex selected pivot index 
     * @param classificationKey the keys referring to the results to be set
     * @param stats input streamingSampleStats to be queried to populate results
     */
    void setResults(final int band, final int pivotIndex, 
            final MultiKey classifierKey, final StreamingSampleStats stats) {
        setResults(band, pivotIndex, classifierKey, stats, null);
    }


    /**
     * Get the subset of results for the given band.
     *
     * See the example of chaining this method in the class docs.
     *
     * @param b band index
     *
     * @return a new {@code ClassifiedStats} object containing results for the band
     *         (data are shared with the source object rather than copied)
     */
    public ClassifiedStats band(int b) {
        return new ClassifiedStats(this, b, 0, null, null);
    }
    
    /**
     * Get the subset of results for the given group.
     *
     * See the example of chaining this method in the class docs.
     *
     * @param g group (pivot) index
     *
     * @return a new {@code ClassifiedStats} object containing results for the group
     *         (data are shared with the source object rather than copied)
     */
    public ClassifiedStats group(int g) {
        return new ClassifiedStats(this, null, g, null, null);
    }

    /**
     * Get the subset of results for the given {@code Statistic}.
     *
     * See the example of chaining this method in the class docs.
     *
     * @param s the statistic
     *
     * @return a new {@code ClassifiedStats} object containing results for the statistic
     *         (data are shared with the source object rather than copied)
     */
    public ClassifiedStats statistic(Statistic s) {
        return new ClassifiedStats(this, null, null, s, null);
    }

    /**
     * Get the subset of results for the given {@code Ranges}.
     *
     * @param ranges the Ranges
     *
     * @return a new {@code ClassifiedStats} object containing results for the ranges
     *         (data are shared with the source object rather than copied)
     */
    public ClassifiedStats ranges(List<Range<Double>> ranges) {
        return new ClassifiedStats(this, null, null, null, ranges);
    }

    /**
     * Returns the {@code Result} objects as a List<Map<MultiKey, List<Result>>> 
     * The keys are multiKey setup on top of the classifier pixel values. For each of them,
     * a List of {@code Result}s is provided. In case of classified stats against local ranges,
     * the list will contain the Result for each range.
     * The outer list allows to group results by pivot. In case no pivot classifiers 
     * have been specified, the list will be a singleton and user should always get element 0. 
     * 
     * @return the results
     * @see Result
     */
    public List<Map<MultiKey, List<Result>>> results() {
        return Collections.unmodifiableList(results);
    }
}
