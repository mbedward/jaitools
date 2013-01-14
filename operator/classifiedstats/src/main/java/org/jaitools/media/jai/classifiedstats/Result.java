/* 
 *  Copyright (c) 2009-2011, Daniele Romagnoli. All rights reserved. 
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

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;


/**
 * Holds the result for a given combination of image band / classifier / statistic / ranges.
 * See documentation for {@code ClassifiedStats} for examples on how to access results.
 * 
 * @see ClassifiedStats
 * @author Daniele Romagnoli, GeoSolutions SAS
 * @author Michael Bedward
 */
public class Result {

    private MultiKey classifierKeys;
    private List<Range<Double>> ranges;
    private int imageBand;
    private Statistic stat;
    private Double value;
    private long numOffered;
    private long numAccepted;
    private long numNaN;
    private long numNoData;

    /**
     * Create a new {@code Result} object. This is intended for use by the
     * {@code ClassifiedStats} class rather than client code.
     * 
     * @param imageBand data image band
     * @param stat the statistic to which this result pertains
     * @param ranges list of ranges (if any) used to filter data image values
     * @param value the calculated value of the statistic
     * @param numOffered number of data image values considered for inclusion
     * @param numAccepted number of data image values actually used for calculating
     *        this result
     * @param numNaN number of NaN values read from the data image
     * @param numNoData number of NoData values read from the data image
     * @param classifierKeys the classifier keys (multikey) associated to this result
     */
    public Result(int imageBand, Statistic stat, List<Range<Double>> ranges, 
                  Double value, long numOffered, long numAccepted, long numNaN, 
                  long numNoData, MultiKey classifierKeys) {
        this.imageBand = imageBand;
        this.stat = stat;
        this.value = value;
        this.numOffered = numOffered;
        this.numAccepted = numAccepted;
        this.numNaN = numNaN;
        this.numNoData = numNoData;
        this.ranges = ranges;
        this.classifierKeys = classifierKeys;
    }

    /**
     * Get the ranges (if defined) that were used to filter data image
     * values for inclusion in the calculation of this result.
     * 
     * @return ranges used to filter data image values
     */
    public Collection<Range<Double>> getRanges() {
        return Collections.unmodifiableCollection(ranges);
    }

    /**
     * Get the index of the image band for which this result
     * was calculated.
     * 
     * @return image band index
     */
    public int getImageBand() {
        return imageBand;
    }

    /**
     * Get the statistic that this result pertains to.
     * 
     * @return the statistic
     */
    public Statistic getStatistic() {
        return stat;
    }

    /**
     * Get the calculated value of the statistic.
     * 
     * @return value of the statistic
     */
    public Double getValue() {
        return value;
    }

    /**
     * Get the number of values that were accepted, ie. sampled from the image
     * and included in the calculation of this result.
     * 
     * @return the number of values accepted
     */
    public long getNumAccepted() {
        return numAccepted;
    }

    /**
     * Get the number of values that were offered, ie. sampled from the image
     * and considered for inclusion in this result.
     * 
     * @return number of values offered
     */
    public long getNumOffered() {
        return numOffered;
    }

    /**
     * Get the number of NaN values that were sampled from the image when
     * calculating this result.
     * 
     * @return number of NaN values sampled
     */
    public long getNumNaN() {
        return numNaN;
    }

    /**
     * Get the number of NoData values that were sampled from the image when
     * calculating this result.
     * 
     * @return number of NoData values sampled
     */
    public long getNumNoData() {
        return numNoData;
    }

    /**
     * Get the classifier keys. The key order respects the order of the classifier layers.
     * Supposing you have set a classified stats with classifierImage1 and classifierImage2
     * the MultiKey [key1, key2] refers to the key associated with classifierImage1 and
     * classifierImage2 respectively.
     * 
     * @return the classifier keys
     */
    public MultiKey getClassifierKeys() {
        return classifierKeys;
    }

    @Override
    public String toString() {
        String rangess = ranges != null && !ranges.isEmpty() ? ranges.toString() : "";
        return String.format("band %d %s: %.4f Naccepted=%d (offered:%d - NoData:%d - NaN:%d) %s Classifier:%s",
                imageBand, stat, value, numAccepted, numOffered, numNoData, numNaN, rangess, classifierKeys);
    }


}
