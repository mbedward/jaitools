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

package jaitools.media.jai.zonalstats;

import jaitools.numeric.StreamingSampleStats;
import jaitools.numeric.Statistic;
import jaitools.utils.CollectionFactory;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author Michael Bedward
 */
public class ZonalStats {

    Map<Integer, Map<Statistic, Double>> data;

    /**
     * Constructor. Package-private
     * @param stats the array of data
     * @param zones a SortedSet of integer zone IDs
     */
    ZonalStats(Statistic[] stats, SortedSet<Integer> zones) {
        data = CollectionFactory.newTreeMap();

        for (Integer zone : zones) {
            for (Statistic stat : stats) {
                Map<Statistic, Double> zoneStats = CollectionFactory.newTreeMap();
                data.put(zone, zoneStats);
            }
        }
    }

    /**
     * Store the results for the given zone. Package-private method.
     */
    void setZoneResults(int zone, StreamingSampleStats streamStats) {
        Map<Statistic, Double> zoneStats = data.get(zone);
        for (Statistic stat : streamStats.getStatistics()) {
            zoneStats.put(stat, streamStats.getStatisticValue(stat));
        }
    }

    /**
     * Get a List of the integer zone IDs that were read from the
     * zone image
     * @return a new List<Integer> containing the IDs
     */
    public List<Integer> getZones() {
        List<Integer> ids = CollectionFactory.newList();
        ids.addAll(data.keySet());
        return ids;
    }

    /**
     * Get the statistics calculated for the specified zone
     * @param zone the integer ID of the zone. If zone does not
     * match any of the IDs stored, this method will return null.
     *
     * @return a Map<Statistic, Double> of Statistics and their values,
     * or null if no results are held for the zone
     */
    public Map<Statistic, Double> getZoneStats(int zone) {
        Map<Statistic, Double> zoneStats = data.get(zone);
        if (zoneStats == null) {
            return null;
        }

        Map<Statistic, Double> copy = CollectionFactory.newTreeMap();
        copy.putAll(data.get(zone));
        return copy;
    }
}
