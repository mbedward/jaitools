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

import jaitools.numeric.Statistic;

/**
 * @author Michael Bedward
 */
public class Result {
    private int imageBand;
    private int zone;
    private Statistic stat;
    private Double value;
    private long numOffered;
    private long numAccepted;

    public Result(int imageBand, int zone, Statistic stat, Double value, long numOffered, long numAccepted) {
        this.imageBand = imageBand;
        this.zone = zone;
        this.stat = stat;
        this.value = value;
        this.numOffered = numOffered;
        this.numAccepted = numAccepted;
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
        return String.format("band %d zone %d %s: %.4f n=%d (%d)",
                imageBand, zone, stat, value, numAccepted, numOffered);
    }


}
