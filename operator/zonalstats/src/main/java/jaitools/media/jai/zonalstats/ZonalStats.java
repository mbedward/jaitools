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

package jaitools.media.jai.zonalstats;

import jaitools.CollectionFactory;
import jaitools.numeric.Range;
import jaitools.numeric.Statistic;
import jaitools.numeric.StreamingSampleStats;

import java.util.Collections;
import java.util.List;
import java.util.SortedSet;

/**
 * Holds the results of the ZonalStats operator.
 * An instance of this class is stored as a property of the destination
 * image.
 * <p>
 * The result for each combination of data image band, zone image integer zone (if
 * provided) and requested statistic is stored as a {@code Result} object.
 * The most basic usage is to iterate through the results as follows...
 * <pre><code>
 * RenderedOp op = JAI.create("zonalstats", myParamBlock);
 * ZonalStats allStats = (ZonalStats) op.getProperty(ZonalStatsDescriptor.ZONAL_STATS_PROPERTY);
 * for (Result r : allStats.results()) {
 *     System.out.prinln(r);
 * }
 * </code></pre>
 * 
 * Alternatively, the attributes of {@code Result} objects can be retrieved selectively...
 * <pre><code>
 * ZonalStats allStats = ...
 * for (Result r : allStats.results()) {
 *     if (r.getStatistic() == Statistic.MEAN) {
 *         System.out.printf("%4d %4d %8.4f\n", 
 *             r.getImageBand(), r.getZone(), r.getValue());
 *     }
 * }
 * </code></pre>
 * 
 * For most uses it may be easier to use the chaining methods provided by {@code ZonalStats}
 * to select the subset of results required...
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
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
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
     * @param ranges selected ranges or {@code null} for all ranges
     */
    private ZonalStats(ZonalStats src, Integer band, Integer zone, Statistic stat, List<Range> ranges) {
        results = CollectionFactory.list();
        for (Result r : src.results) {
            if ((band == null || r.getImageBand() == band) &&
                (zone == null || r.getZone() == zone) &&
                (stat == null || r.getStatistic() == stat)) {
                if (ranges == null || ranges.isEmpty()) {
                    results.add(r);
                } else {
                    if (r.getRanges().containsAll(ranges)) {
                        results.add(r);
                    } else {
                        for (Range range : ranges) {
                            if (r.getRanges().contains(range)) {
                                results.add(r);
                            }
                        }
                    }
                }

            }
        }
    }

    /**
     * Store the results for the given zone. Package-private method used by
     * {@code ZonalStatsOpImage}.
     */
    void setResults(int band, int zone, StreamingSampleStats stats, List<Range> includedRanges) {
        for (Statistic s : stats.getStatistics()) {
            Result r = new Result(band, zone, s, includedRanges,
                    stats.getStatisticValue(s),
                    stats.getNumOffered(s),
                    stats.getNumAccepted(s),
                    stats.getNumNaN(s),
                    stats.getNumNoData(s));

            results.add(r);
        }
    }

    /**
     * Store the results for the given zone. Package-private method used by
     * {@code ZonalStatsOpImage}.
     */
    void setResults(int band, int zone, StreamingSampleStats stats) {
        setResults(band, zone, stats, null);
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
        return new ZonalStats(this, b, null, null, null);
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
        return new ZonalStats(this, null, z, null, null);
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
        return new ZonalStats(this, null, null, s, null);
    }

    /**
     * Get the subset of results for the given {@code Ranges}.
     *
     * @param ranges the Ranges
     *
     * @return a new {@code ZonalStats} object containing results for the ranges
     *         (data are shared with the source object rather than copied)
     */
    public ZonalStats ranges(List<Range> ranges) {
        return new ZonalStats(this, null, null, null, ranges);
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
