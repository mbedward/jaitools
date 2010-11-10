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

    public long getNumNaN() {
        return numNaN;
    }

    public long getNumNoData() {
        return numNoData;
    }

    public Result(int imageBand, int zone, Statistic stat, List<Range> ranges, Double value, long numOffered, long numAccepted, long numNaN, long numNoData) {
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

    public Collection<Range> getRanges() {
        return Collections.unmodifiableCollection(ranges);
    }

    public int getImageBand() {
        return imageBand;
    }

    public int getZone() {
        return zone;
    }

    public Statistic getStatistic() {
        return stat;
    }

    public Double getValue() {
        return value;
    }

    public long getNumAccepted() {
        return numAccepted;
    }

    public long getNumOffered() {
        return numOffered;
    }

    @Override
    public String toString() {
        String rangess = ranges != null && !ranges.isEmpty() ? ranges.toString() : "";
        return String.format("band %d zone %d %s: %.4f N=%d (%d - ND:%d - NaN:%d) %s",
                imageBand, zone, stat, value, numAccepted, numOffered, numNoData, numNaN, rangess);
    }


}
