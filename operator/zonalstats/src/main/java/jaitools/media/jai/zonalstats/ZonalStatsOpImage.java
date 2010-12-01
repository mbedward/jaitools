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
import jaitools.numeric.Range;
import jaitools.numeric.RangeUtils;
import jaitools.numeric.Statistic;
import jaitools.numeric.StreamingSampleStats;

import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 * Calculates image summary statistics for a data image within zones defined by
 * a integral valued zone image. If a zone image is not provided all data image
 * pixels are treated as being in the same zone (zone 0).
 *
 * @see ZonalStatsDescriptor Description of the algorithm and example
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStatsOpImage extends NullOpImage {

    private final Integer[] srcBands;

    private final ROI roi;

    private final Statistic[] stats;

    private final RenderedImage dataImage;
    private final Rectangle dataImageBounds;
    private final RenderedImage zoneImage;
    private final AffineTransform zoneTransform;
    
    /** Optional ranges to exclude/include values from/in statistics computations */
    private final List<Range<Double>> ranges;
    
    /** Optional ranges to specify which values should be considered as NoData and then excluded from computations */
    private final List<Range<Double>> noDataRanges;
    
    /** Compute separated statistics on ranges if true */
    private final boolean rangeLocalStats;
    
    /** Define whether provided ranges of values need to be included or excluded 
     * from statistics computations */
    private Range.Type rangesType;

    private SortedSet<Integer> zones;


    /**
     * Constructor.
     *
     * @param dataImage a {@code RenderedImage} from which data values will be read.
     *
     * @param zoneImage an optional {@code RenderedImage} of integral data type that defines
     *        the zones for which to calculate summary data.
     * 
     * @param config configurable attributes of the image (see {@link AreaOpImage}).
     *
     * @param layout an optional {@code ImageLayout} object.
     *
     * @param stats an array of {@code Statistic} constants specifying the data required.
     *
     * @param bands the data image band to process.
     *
     * @param roi an optional {@code ROI} for data image masking.
     *
     * @param zoneTransform
     * 
     * @param excludedRanges a {@link List} of {@link Range}s, that will be filtered out
     *        of the process. This means that values inside the supplied ranges 
     *        will not be considered as valid and discarded.
     * 
     * @see ZonalStatsDescriptor
     * @see Statistic
     */
    public ZonalStatsOpImage(RenderedImage dataImage, RenderedImage zoneImage, 
            Map<?, ?> config,
            ImageLayout layout,
            Statistic[] stats, 
            Integer[] bands,
            ROI roi,
            AffineTransform zoneTransform,
            List<Range<Double>> excludedRanges) {
    	this (dataImage, zoneImage, config, layout, stats, bands, roi, zoneTransform, excludedRanges, Range.Type.EXCLUDE, false, null);
    }
    
    /**
     * Constructor.
     *
     * @param dataImage a {@code RenderedImage} from which data values will be read.
     *
     * @param zoneImage an optional {@code RenderedImage} of integral data type that defines
     *        the zones for which to calculate summary data.
     * 
     * @param config configurable attributes of the image (see {@link AreaOpImage}).
     *
     * @param layout an optional {@code ImageLayout} object.
     *
     * @param stats an array of {@code Statistic} constants specifying the data required.
     *
     * @param bands the data image band to process.
     *
     * @param roi an optional {@code ROI} for data image masking.
     *
     * @param zoneTransform
     * 
     * @param ranges a {@link List} of {@link Range}s, that will be filtered out/in
     *        of the process. This means that values inside the supplied ranges 
     *        will be considered as invalid/valid and discarded/accepted.
     *        
     * @param rangesType specify if the provided ranges argument should be considered 
     * 		  as Included or Excluded. See {@link Range.Type}.
     * 
     * @param rangeLocalStats if {@code true}, the statistics should be computed for ranges, 
     * 		  separately. 
     *        
     * 
     * @see ZonalStatsDescriptor
     * @see Statistic
     */
    public ZonalStatsOpImage(RenderedImage dataImage, RenderedImage zoneImage, 
            Map<?, ?> config,
            ImageLayout layout,
            Statistic[] stats, 
            Integer[] bands,
            ROI roi,
            AffineTransform zoneTransform,
            List<Range<Double>> ranges,
            Range.Type rangesType,
            final boolean rangeLocalStats,
            List<Range<Double>> noDataRanges) {

        super(dataImage, layout, config, OpImage.OP_COMPUTE_BOUND);

        this.dataImage = dataImage;
        this.zoneImage = zoneImage;

        dataImageBounds = new Rectangle(
        		dataImage.getMinX(), dataImage.getMinY(),
        		dataImage.getWidth(), dataImage.getHeight());

        this.stats = stats;
        this.srcBands = bands;
        this.roi = roi;
        this.zoneTransform = zoneTransform;
        this.rangeLocalStats = rangeLocalStats;
        this.ranges = CollectionFactory.list();
        this.rangesType = rangesType;
        if (ranges != null && !ranges.isEmpty()) {
        	
            // copy the ranges defensively
            for (Range<Double> r : ranges) {
                this.ranges.add(new Range<Double>(r));
            }
        }
        
        this.noDataRanges = CollectionFactory.list();
        if (noDataRanges != null && !noDataRanges.isEmpty()) {
            
            // copy the ranges defensively
            for (Range<Double> r : noDataRanges) {
                this.noDataRanges.add(new Range<Double>(r));
            }
        }
    }

    /**
     * Compiles the set of zone ID values from the zone image. Note, we are
     * assuming that the zone values are in band 0.
     * <p>
     * If a zone image wasn't provided we treat all data image pixels as
     * belonging to zone 0.
     */
    private void buildZoneList() {
        zones = CollectionFactory.sortedSet();
        if (zoneImage != null) {

            if (zoneTransform == null) { // Optimize by only examining zones within bounds
                RectIter iter = RectIterFactory.create(zoneImage, dataImageBounds);
                do {
                    do {
                        zones.add(iter.getSample());
                    } while (!iter.nextPixelDone());
                    iter.startPixels();
                } while (!iter.nextLineDone());


            } else { // examine the whole zone image -- TODO: optimize also this case
            RectIter iter = RectIterFactory.create(zoneImage, null);
            do {
                do {
                    zones.add(iter.getSample());
                } while (!iter.nextPixelDone());
                iter.startPixels();
            } while (!iter.nextLineDone());
            }
        } else {
            zones.add(0);
        }
    }


    /**
     * Delegates calculation of statistics to either {@linkplain #compileZonalStatistics()}
     * or {@linkplain #compileUnzonedStatistics()}.
     *
     * @return the results as a new instance of {@code ZonalStats}
     */
    private synchronized ZonalStats compileStatistics() {
        if (zoneImage != null) {
            return compileZonalStatistics();
        } else {
        	if (!rangeLocalStats)
        		return compileUnzonedStatistics();
        	else
        		return compileRangeStatistics();
        }
    }

    /**
     * Called by {@link #compileZonalStatistics()} to lazily create a
     * {@link StreamingSampleStats} object for each zone as it is encountered
     * in the zone image. The new object is added to the provided {@code resultsPerBand}
     * {@code Map}.
     * 
     * @param resultsPerBand {@code Map} of results by zone id
     * @param zone integer zone id
     * 
     * @return a new {@code StreamingSampleStats} object
     */
    protected StreamingSampleStats setupZoneStats(Map<Integer, StreamingSampleStats> resultsPerBand, Integer zone) {
        StreamingSampleStats sampleStats = new StreamingSampleStats(Range.Type.EXCLUDE);
        for (Range<Double> r : ranges) {
            sampleStats.addRange(r);
        }
        for (Range<Double> r : noDataRanges) {
            sampleStats.addNoDataRange(r);
        }
        sampleStats.setStatistics(stats);
        resultsPerBand.put(zone, sampleStats);
        return sampleStats;
    }

    /**
     * Used to calculate statistics when a zone image was provided.
     *
     * @return the results as a {@code ZonalStats} instance
     */
    private ZonalStats compileZonalStatistics() {

        Map<Integer, Map<Integer, StreamingSampleStats>> results = CollectionFactory.sortedMap();
        for (Integer srcBand : srcBands) {
            Map<Integer, StreamingSampleStats> resultsPerBand = CollectionFactory.sortedMap();
            results.put(srcBand, resultsPerBand);
        }

        final double[] sampleValues = new double[dataImage.getSampleModel().getNumBands()];
        RectIter dataIter = RectIterFactory.create(dataImage, null);
        if (zoneTransform == null) { // Identity transform assumed
            RectIter zoneIter = RectIterFactory.create(zoneImage, dataImageBounds);

            int y = dataImage.getMinY();
            do {
                int x = dataImage.getMinX();
                do {
                    if (roi == null || roi.contains(x, y)) {
                        dataIter.getPixel(sampleValues);
                        for (Integer band : srcBands) {
                            Map<Integer, StreamingSampleStats> resultPerBand = results.get(band);

                            int zone = zoneIter.getSample();
                            StreamingSampleStats sss = resultPerBand.get(zone);
                            if (sss == null) // init the zoned stats lazily
                            {
                                sss = setupZoneStats(resultPerBand, zone);
                            }
                            sss.offer(sampleValues[band]);
                        }
                    }
                    zoneIter.nextPixelDone(); // safe call
                    x++;
                } while (!dataIter.nextPixelDone());

                dataIter.startPixels();
                zoneIter.startPixels();
                zoneIter.nextLineDone(); // safe call
                y++;

            } while (!dataIter.nextLineDone());

        } else {
            final RandomIter zoneIter = RandomIterFactory.create(zoneImage, dataImageBounds);
            final Point2D.Double dataPos = new Point2D.Double();
            final Point2D.Double zonePos = new Point2D.Double();
            dataPos.y = dataImage.getMinY();
            do {
                dataPos.x = dataImage.getMinX();
                do {
                    if (roi == null | roi.contains(dataPos)) {
                        dataIter.getPixel(sampleValues);
                        zoneTransform.transform(dataPos, zonePos);

                        for (Integer band : srcBands) {
                            Map<Integer, StreamingSampleStats> resultPerBand = results.get(band);

                            int zone = zoneIter.getSample((int) zonePos.x, (int) zonePos.y, 0);

                            StreamingSampleStats sss = resultPerBand.get(zone);
                            if (sss == null) // init the zoned stats lazily
                            {
                                sss = setupZoneStats(resultPerBand, zone);
                            }
                            sss.offer(sampleValues[band]);
                        }
                    }
                    dataPos.x++;
                } while (!dataIter.nextPixelDone());

                dataIter.startPixels();
                dataPos.y++;

            } while (!dataIter.nextLineDone());
        }


        // collect all found zones
        Set<Integer> zonesFound = new TreeSet<Integer>();
        for (Integer band : srcBands) {
            Set<Integer> zoneSetForBand = results.get(band).keySet();
            zonesFound.addAll(zoneSetForBand);
        }

        // set the results
        ZonalStats zs = new ZonalStats();
        for (Integer band : srcBands) {
            for (Integer zone : zonesFound) {
                zs.setResults(band, zone, results.get(band).get(zone));
            }
        }

        return zs;
    }

    /**
     * Used to calculate statistics when no zone image was provided.
     *
     * @return the results as a {@code ZonalStats} instance
     */
    private ZonalStats compileUnzonedStatistics() {
        buildZoneList();
        Integer zoneID = zones.first();
        
        // create the stats
        final StreamingSampleStats sampleStatsPerBand[] = new StreamingSampleStats[srcBands.length];
        for (int index = 0; index < srcBands.length; index++) {
            final StreamingSampleStats sampleStats = new StreamingSampleStats(rangesType);
            for (Range<Double> r : ranges) {
                sampleStats.addRange(r);
            }
            for (Range<Double> r : noDataRanges) {
                sampleStats.addNoDataRange(r);
            }
            sampleStats.setStatistics(stats);
            sampleStatsPerBand[index] = sampleStats;
        }

        final double[] sampleValues = new double[dataImage.getSampleModel().getNumBands()];
        RectIter dataIter = RectIterFactory.create(dataImage, null);
        int y = dataImage.getMinY();
        do {
            int x = dataImage.getMinX();
            do {
                if (roi == null || roi.contains(x, y)) {
                    dataIter.getPixel(sampleValues);
                    for (int index = 0; index < srcBands.length; index++) {
                        final double value = sampleValues[srcBands[index]];
                        sampleStatsPerBand[index].offer(value);
                    }
                }
                x++;
            } while (!dataIter.nextPixelDone() );

            dataIter.startPixels();
            y++;

        } while (!dataIter.nextLineDone() );

        // get the results
        final ZonalStats zs = new ZonalStats();
        for (int index = 0; index < srcBands.length; index++) {
            final StreamingSampleStats sampleStats = sampleStatsPerBand[index];
            List<Range> inclRanges = null;
            if (ranges != null && !ranges.isEmpty()){
            	switch(rangesType){
            		case INCLUDE:
            			inclRanges = CollectionFactory.list();
            			inclRanges.addAll(ranges);
            			break;
            		case EXCLUDE:
            			inclRanges = CollectionFactory.list();
            			List<Range<Double>> incRanges = RangeUtils.createComplement(RangeUtils.sort(ranges));
            			inclRanges.addAll(incRanges);
            			break;
            	}
            }
            zs.setResults(srcBands[index], zoneID, sampleStats, inclRanges);
        }
        return zs;
    }
    
    /**
     * Used to calculate statistics when range local statistics are required.
     *
     * @return the results as a {@code ZonalStats} instance
     */
    private ZonalStats compileRangeStatistics() {
        buildZoneList();
        final Integer zoneID = zones.first();
        final ZonalStats zs = new ZonalStats();
        List<Range> localRanges = null;
        switch (rangesType){
        	case EXCLUDE:
        		List<Range<Double>> inRanges = RangeUtils.createComplement(RangeUtils.sort(ranges));
        		localRanges = CollectionFactory.list();
        		localRanges.addAll(inRanges);
        		break;
        	case INCLUDE:
        		localRanges = CollectionFactory.list();
        		localRanges.addAll(ranges);
        		break;
        	case UNDEFINED:
        		throw new UnsupportedOperationException("Unable to compute range local statistics on UNDEFINED ranges type");
        }

        for (Range<Double> range: localRanges){
        	
	        // create the stats
	        final StreamingSampleStats sampleStatsPerBand[] = new StreamingSampleStats[srcBands.length];
	        for (int index = 0; index < srcBands.length; index++) {
	            final StreamingSampleStats sampleStats = new StreamingSampleStats(rangesType);
                    sampleStats.addRange(range);
                    for (Range<Double> noDataRange: noDataRanges){
                        sampleStats.addNoDataRange(noDataRange);
                    }
	            sampleStats.setStatistics(stats);
	            sampleStatsPerBand[index] = sampleStats;
	        }
	
	        final double[] sampleValues = new double[dataImage.getSampleModel().getNumBands()];
	        RectIter dataIter = RectIterFactory.create(dataImage, null);
	        int y = dataImage.getMinY();
	        do {
	            int x = dataImage.getMinX();
	            do {
	                if (roi == null || roi.contains(x, y)) {
	                    dataIter.getPixel(sampleValues);
	                    for (int index = 0; index < srcBands.length; index++) {
	                        final double value = sampleValues[srcBands[index]];
	                        sampleStatsPerBand[index].offer(value);
	                    }
	                }
	                x++;
	            } while (!dataIter.nextPixelDone() );
	
	            dataIter.startPixels();
	            y++;
	
	        } while( !dataIter.nextLineDone() );
	
	        // get the results
	        for (int index = 0; index < srcBands.length; index++) {
	            StreamingSampleStats sampleStats = sampleStatsPerBand[index];
	            List<Range> resultRanges = CollectionFactory.list();
	            resultRanges.add(range);
	            zs.setResults(srcBands[index], zoneID, sampleStats, resultRanges);
	        }
    	}
       
        return zs;
    }

    /**
     * Get the specified property.
     * <p>
     * Use this method to retrieve the calculated statistics as a map of {@code ZonalStats} per band
     * by setting {@code name} to {@linkplain ZonalStatsDescriptor#ZONAL_STATS_PROPERTY}.
     *
     * @param name property name
     *
     * @return the requested property
     */
    @Override
    public Object getProperty( String name ) {
        if (ZonalStatsDescriptor.ZONAL_STATS_PROPERTY.equalsIgnoreCase(name)) {
            return compileStatistics();
        } else {
            return super.getProperty(name);
        }
    }

    /**
     * Get the class of the given property. For
     * {@linkplain ZonalStatsDescriptor#ZONAL_STATS_PROPERTY} this will return
     * {@code Map.class}.
     *
     * @param name property name
     *
     * @return the property class
     */
    @Override
    public Class<?> getPropertyClass( String name ) {
        if (ZonalStatsDescriptor.ZONAL_STATS_PROPERTY.equalsIgnoreCase(name)) {
            return Map.class;
        } else {
            return super.getPropertyClass(name);
        }
    }

    /**
     * Get all property names
     * @return property names as an array of Strings
     */
    @Override
    public String[] getPropertyNames() {
        String[] names;
        int k = 0;

        String[] superNames = super.getPropertyNames();
        if (superNames != null) {
            names = new String[superNames.length + 1];
            for( String name : super.getPropertyNames() ) {
                names[k++] = name;
            }
        } else {
            names = new String[1];
        }

        names[k] = ZonalStatsDescriptor.ZONAL_STATS_PROPERTY;
        return names;
    }

}
