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

import jaitools.CollectionFactory;
import jaitools.numeric.StreamingSampleStats;
import jaitools.numeric.Statistic;
import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * Holds the results of the ZonalStats operator.
 * An instance of this class is stored as a property of the destination
 * image.
 * <p>
 * Chaining methods are provided to select a subset of the results...
 * <pre><code>
 * ZonalStats allStats = ...
 *
 * // Get results for a given band
 * int bandIndex = ...
 * List<Result> bandResults = allStats.band(bandIndex).results();
 *
 *
 * // Get Statistic.MEAN values for the specified band and zone
 * List<Result> subsetResults = allStats.band(b).zone(z).statistic(Statistic.MEAN).results();
 *
 *
 * // Impress your friends with pretty printing !
 * Statistic[] statistics = {
 *           Statistic.MIN,
 *           Statistic.MAX,
 *           Statistic.MEDIAN,
 *           Statistic.APPROX_MEDIAN,
 *           Statistic.SDEV
 *       };
 *
 * System.out.println("                               exact    approx");
 * System.out.println(" band zone      min      max   median   median     sdev");
 * System.out.println("-----------------------------------------------------------");
 *
 * for (int b : allStats.getImageBands()) {
 *     for (int z : zs.getZones()) {
 *         System.out.printf(" %4d %4d", b, z);
 *         ZonalStats subset = zs.band(b).zone(z);
 *         for (Statistic s : statistics) {
 *             System.out.printf(" %8.4f", zoneSubset.statistic(s).results().get(0).getValue());
 *         }
 *         System.out.println();
 *     }
 * }
 *
 * </code></pre>
 * 
 * @see Result
 * @see ZonalStatsDescriptor
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStats {

    private List<Result> results;

    /**
     * Constructor. Package-private; called by ZonalStatsOpImage.
     */
    ZonalStats() {
        results = CollectionFactory.list();
    }

    /**
     * Copy constructor. Used by the chaining methods such as {@linkplain #band(int)}.
     *
     * @param src source object
     * @param band selected image band or {@code null} for all bands
     * @param zone selected zone or {@code null} for all zones
     * @param stat selected statistic or {@code null} for all statistics
     */
    private ZonalStats(ZonalStats src, Integer band, Integer zone, Statistic stat) {
        results = CollectionFactory.list();
        for (Result r : src.results) {
            if ((band == null || r.getImageBand() == band) &&
                (zone == null || r.getZone() == zone) &&
                (stat == null || r.getStatistic() == stat)) {
                results.add(r);
            }
        }
    }

    /**
     * Store the results for the given zone. Package-private method used by
     * {@code ZonalStatsOpImage}.
     */
    void setResults(int band, int zone, StreamingSampleStats stats) {
        for (Statistic s : stats.getStatistics()) {
            Result r = new Result(band, zone, s,
                    stats.getStatisticValue(s),
                    stats.getNumOffered(s),
                    stats.getNumAccepted(s));

            results.add(r);
        }
    }

    /**
     * Get the integer IDs read from the zone image. If a zone image
     * was not used all results are treated as being in zone 0.
     * <p>
     * Note that statistics will not necessarily have been calculated for
     * all zones.
     *
     * @return the sorted zone IDs
     */
    public SortedSet<Integer> getZones() {
        SortedSet<Integer> ids = CollectionFactory.sortedSet();

        for (Result r : results) {
            ids.add(r.getZone());
        }
        
        return ids;
    }

    /**
     * Get the subset of results for the given band.
     *
     * See the example of chaining this method in the class docs.
     *
     * @param b band index
     *
     * @return a new {@code ZonalStats} object containing results for the band
     *         (data are shared with the source object rather than copied)
     */
    public ZonalStats band(int b) {
        return new ZonalStats(this, b, null, null);
    }

    /**
     * Get the subset of results for the given zone.
     *
     * See the example of chaining this method in the class docs.
     *
     * @param z zone ID
     *
     * @return a new {@code ZonalStats} object containing results for the zone
     *         (data are shared with the source object rather than copied)
     */
    public ZonalStats zone(int z) {
        return new ZonalStats(this, null, z, null);
    }

    /**
     * Get the subset of results for the given {@code Statistic}.
     *
     * See the example of chaining this method in the class docs.
     *
     * @param s the statistic
     *
     * @return a new {@code ZonalStats} object containing results for the statistic
     *         (data are shared with the source object rather than copied)
     */
    public ZonalStats statistic(Statistic s) {
        return new ZonalStats(this, null, null, s);
    }

    /**
     * Returns the list of {@code Result} objects.
     *
     * @return the results
     * @see Result
     */
    public List<Result> results() {
        return Collections.unmodifiableList(results);
    }
}
