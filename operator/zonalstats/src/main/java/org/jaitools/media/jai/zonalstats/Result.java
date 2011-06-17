/* 
 *  Copyright (c) 2009-2010, Michael Bedward. All rights reserved. 
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

package org.jaitools.media.jai.zonalstats;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;


/**
 * Holds the result for a a given combination of image band / zone / statistic.
 * See documentation for {@code ZonalStats} for examples of how to access 
 * results.
 * 
 * @see ZonalStats
 * @author Michael Bedward
 */
public class Result {

    private List<Range> ranges;
    private int imageBand;
    private int zone;
    private Statistic stat;
    private Double value;
    private long numOffered;
    private long numAccepted;
    private long numNaN;
    private long numNoData;

    /**
     * Create a new {@code Result} object. This is intended for use by the
     * {@code ZonalStats} class rather than client code.
     * 
     * @param imageBand data image band
     * @param zone integer identifier of the zone in the zone image; 0 should be passed
     *        when no zone image was used
     * @param stat the statistic to which this result pertains
     * @param ranges list of ranges (if any) used to filter data image values
     * @param value the calculated value of the statistic
     * @param numOffered number of data image values considered for inclusion
     * @param numAccepted number of data image values actually used for calculating
     *        this result
     * @param numNaN number of NaN values read from the data image
     * @param numNoData number of NoData values read from the data image
     */
    public Result(int imageBand, int zone, Statistic stat, List<Range> ranges, 
                  Double value, long numOffered, long numAccepted, long numNaN, 
                  long numNoData) {
        this.imageBand = imageBand;
        this.zone = zone;
        this.stat = stat;
        this.value = value;
        this.numOffered = numOffered;
        this.numAccepted = numAccepted;
        this.numNaN = numNaN;
        this.numNoData = numNoData;
        this.ranges = ranges;
    }

    /**
     * Get the ranges (if defined) that were used to filter data image
     * values for inclusion in the calculation of this result.
     * 
     * @return ranges used to filter data image values
     */
    public Collection<Range> getRanges() {
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
     * Get the integer ID of the image zone for which this result was
     * calculated. If no zone image was used this method will
     * return 0.
     * 
     * @return integer ID of the image zone.
     */
    public int getZone() {
        return zone;
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

    @Override
    public String toString() {
        String rangess = ranges != null && !ranges.isEmpty() ? ranges.toString() : "";
        return String.format("band %d zone %d %s: %.4f N=%d (%d - ND:%d - NaN:%d) %s",
                imageBand, zone, stat, value, numAccepted, numOffered, numNoData, numNaN, rangess);
    }


}
