/*
 * Copyright 2009-2010 Michael Bedward
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

package jaitools.media.jai.zonalstats;

import jaitools.numeric.Range;
import jaitools.numeric.Statistic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

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
