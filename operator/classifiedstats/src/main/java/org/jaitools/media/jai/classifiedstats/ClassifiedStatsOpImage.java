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

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.jaitools.CollectionFactory;
import org.jaitools.numeric.Range;
import org.jaitools.numeric.Range.Type;
import org.jaitools.numeric.RangeUtils;
import org.jaitools.numeric.Statistic;
import org.jaitools.numeric.StreamingSampleStats;

/**
 * Calculates image classified summary statistics for a data image.
 * 
 * @see ClassifiedStatsDescriptor for Description of the algorithm and example
 * 
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.2
 */
public class ClassifiedStatsOpImage extends NullOpImage {

    /**
     * A simple object holding classifier properties:
     * - a RandomIterator attached to the classifier Image
     * - a boolean stating whether we need to check for noData on it
     * - a noData value in case we need to check for it. In case the previous boolean
     * is set to false, the noData value will be ignored. 
     * 
     * @author Daniele Romagnoli, GeoSolutions SAS
     */
    private class ClassifierObject {
        /**
         * @param classifierIter
         * @param checkForNoData
         * @param noData
         */
        public ClassifierObject(RandomIter classifierIter, boolean checkForNoData, int noData) {
            this.classifierIter = classifierIter;
            this.checkForNoData = checkForNoData;
            this.noData = noData;
        }
        
        RandomIter classifierIter;
        boolean checkForNoData;
        int noData; //Classifiers are ALWAYS of integer type
    }
    
    private final Integer[] srcBands;

    private final ROI roi;

    /** 
     * Statistics to be computed
     */
    private final Statistic[] stats;

    /** 
     * basic image boundary properties allowing tiled computations 
     */
    private int imageWidth;
    private int imageHeigth;
    private int imageMinY;
    private int imageMinX;
    private int imageMaxX;
    private int imageMaxY;
    private int imageMinTileX;
    private int imageMinTileY;
    private int imageMaxTileY;
    private int imageMaxTileX;
    private int imageTileHeight;
    private int imageTileWidth;
    private final Rectangle dataImageBounds;

    private final RenderedImage dataImage;
    
    /**
     * Images used for classifications 
     */
    private final RenderedImage[] classifierImages;
    
    /**
     * Pivot Images used for classifications 
     */
    private final RenderedImage[] pivotClassifierImages;

    /**
     * Optional array to specify which value of each classifer should be considered as NoData
     * and then filter out from the computation, the pixels at those coordinates.
     * 
     * Note that although classifier are always of integer types, we use double to allow specifying
     * NaN as "unknown/unspecified" element 
     */
    private double[] noDataForClassifierImages;
    
    /**
     * Optional array to specify which value of each pivotClassifier should be considered as NoData
     * and then filter out from the computation, the pixels at those coordinates
     * 
     * Note that although pivot classifier are always of integer types, we use double to allow 
     * specifying NaN as "unknown/unspecified" element
     */
    private double[] noDataForPivotClassifierImages;
    
    /**
     * Optional ranges to exclude/include values from/in statistics computations
     */
    private final List<Range<Double>> ranges;

    /**
     * Optional ranges to specify which values should be considered as NoData
     * and then excluded from computations
     */
    private final List<Range<Double>> noDataRanges;

    /** Compute separated statistics on ranges if true */
    private final boolean rangeLocalStats;

    /**
     * Define whether provided ranges of values need to be included or excluded
     * from statistics computations
     */
    private Range.Type rangesType;

    /**
     * Constructor.
     * 
     * @param dataImage
     *            a {@code RenderedImage} from which data values will be read.
     * 
     * @param classifierImages
     *            a {@code RenderedImage}'s array of integral data type that
     *            defines the classification for which to calculate summary
     *            data.
     * @param pivotClassifierImages
     *            an optional {@code RenderedImage}'s array of integral data type that
     *            defines the pivot classification for which to calculate summary
     *            data. Elements of this array are used to form group with the standard 
     *            classifiers. As an instance, suppose the classifiers are [classifier1,
     *            classifier2] and the pivot classifiers are [pivot1, pivot2], then the
     *            stats will be computed on classifiers [pivot1, classifier1, classifier2]
     *            and [pivot2, classifier1, classifier2]. 
     * @param config
     *            configurable attributes of the image (see {@link AreaOpImage}
     *            ).
     * 
     * @param layout
     *            an optional {@code ImageLayout} object.
     * 
     * @param stats
     *            an array of {@code Statistic} constants specifying the data
     *            required.
     * 
     * @param bands
     *            the data image band to process.
     * 
     * @param roi
     *            an optional {@code ROI} for data image masking.
     * 
     * @param ranges
     *            an optional list of {@link Range} objects defining values to
     *            include or exclude (depending on {@code rangesType} from the
     *            calculations; may be {@code null} or empty
     * 
     * @param rangesType
     *            specifies whether the {@code ranges} argument defines values
     *            to include or exclude
     * 
     * @param rangeLocalStats
     *            if {@code true}, the statistics should be computed for ranges,
     *            separately.
     * 
     * @param noDataRanges
     *            an optional list of {@link Range} objects defining values to
     *            treat as NODATA
     * @param noDataClassifiers
     *            an optional array of Doubles defining values to
     *            treat as NODATA for the related classifierImage. Note that 
     *            classifier images will always leverage on integer types 
     *            (BYTE, INTEGER, SHORT, ...). Such noData are specified
     *            as Double to allow the users to provide NaN in case a NoData
     *            is unavailable for a specific classifierImage.
     * @param noDataPivotClassifiers
     *            an optional array of Doubles defining values to
     *            treat as NODATA for the related pivotClassifierImage. Note that 
     *            classifier images will always leverage on integer types 
     *            (BYTE, INTEGER, SHORT, ...). Such noData are specified
     *            as Double to allow the users to provide NaN in case a NoData
     *            is unavailable for a specific pivotClassifierImage.
     * 
     * @see ClassifiedStatsDescriptor
     * @see Statistic
     */
    public ClassifiedStatsOpImage(
            final RenderedImage dataImage, 
            final RenderedImage[] classifierImages,
            final RenderedImage[] pivotClassifierImages,
            final Map<?, ?> config, 
            final ImageLayout layout, 
            final Statistic[] stats, 
            final Integer[] bands, 
            final ROI roi,
            final Collection<Range<Double>> ranges, 
            final Range.Type rangesType, 
            final boolean rangeLocalStats,
            final Collection<Range<Double>> noDataRanges, 
            final Double[] noDataClassifiers,
            final Double[] noDataPivotClassifiers
            ) {

        super(dataImage, layout, config, OpImage.OP_COMPUTE_BOUND);

        this.dataImage = dataImage;
        this.classifierImages = classifierImages;
        this.pivotClassifierImages = pivotClassifierImages;
        
        // Setting imagesParameters
        this.imageWidth = dataImage.getWidth();
        this.imageHeigth = dataImage.getHeight();
        this.imageTileWidth = Math.min(dataImage.getTileWidth(), imageWidth);
        this.imageTileHeight = Math.min(dataImage.getTileHeight(), imageHeigth);
        this.imageMinY = dataImage.getMinY();
        this.imageMinX = dataImage.getMinX();
        this.imageMaxX = imageMinX + imageWidth - 1;
        this.imageMaxY = imageMinY + imageHeigth - 1;
        this.imageMinTileX = dataImage.getMinTileX();
        this.imageMinTileY = dataImage.getMinTileY();
        this.imageMaxTileX = imageMinTileX + dataImage.getNumXTiles();
        this.imageMaxTileY = imageMinTileY + dataImage.getNumYTiles();
        dataImageBounds = new Rectangle(imageMinX, imageMinY, imageWidth, imageHeigth);

        this.stats = new Statistic[stats.length];
        System.arraycopy(stats, 0, this.stats, 0, stats.length);

        this.srcBands = new Integer[bands.length];
        System.arraycopy(bands, 0, this.srcBands, 0, bands.length);

        this.roi = roi;

        // --------------------------------------------
        //           Ranges initialization
        // --------------------------------------------
        this.rangeLocalStats = rangeLocalStats;
        this.ranges = CollectionFactory.list();
        this.rangesType = rangesType;
        if (ranges != null && !ranges.isEmpty()) {

            // copy the ranges defensively
            for (Range<Double> r : ranges) {
                this.ranges.add(new Range<Double>(r));
            }
        }

        // --------------------------------------------
        //           NoData initialization
        // --------------------------------------------
        this.noDataRanges = CollectionFactory.list();
        if (noDataRanges != null && !noDataRanges.isEmpty()) {

            // copy the ranges defensively
            for (Range<Double> r : noDataRanges) {
                this.noDataRanges.add(new Range<Double>(r));
            }
        }
        if (noDataClassifiers != null){
            this.noDataForClassifierImages = new double[noDataClassifiers.length];
            for (int i = 0; i < noDataClassifiers.length; i++){
                this.noDataForClassifierImages[i] = noDataClassifiers[i];
            }
        }
        
        if (noDataPivotClassifiers!= null){
            this.noDataForPivotClassifierImages = new double[noDataPivotClassifiers.length];
            for (int i = 0; i < noDataPivotClassifiers.length; i++){
                this.noDataForPivotClassifierImages[i] = noDataPivotClassifiers[i];
            }
        }
    }

    /**
     * Delegates calculation of statistics to either
     * {@linkplain #compileRangeStatistics()} or
     * {@linkplain #compileClassifiedStatistics()}.
     * 
     * @return the results as a new instance of {@code ClassifiedStats}
     */
    synchronized ClassifiedStats compileStatistics() {
        ClassifiedStats classifiedStats = null;

        // --------------------------------
        //
        // Init classifiers and iterators
        //
        // --------------------------------
        final RandomIter dataIter = RandomIterFactory.create(dataImage, dataImageBounds);
        final int numClassifiers = classifierImages.length;
        final int numPivotClassifiers = pivotClassifierImages != null ? pivotClassifierImages.length : 0;
        
        // Standard Classifiers
        final ClassifierObject[] classifiers = new ClassifierObject[numClassifiers];
        for (int i = 0; i < numClassifiers; i++) {
            final RandomIter classifierIter = RandomIterFactory.create(classifierImages[i], dataImageBounds);
            final boolean checkForNoData = (noDataForClassifierImages != null && !Double.isNaN(noDataForClassifierImages[i])) ?
                    true : false; 
            final int noDataClassifierValue = checkForNoData ? (int)noDataForClassifierImages[i] : 0;
            classifiers[i] = new ClassifierObject(classifierIter, checkForNoData, noDataClassifierValue);
        }
        
        //Pivot Classifiers
        final ClassifierObject[] pivotClassifiers = numPivotClassifiers > 0 ? new ClassifierObject[numPivotClassifiers] : null;
        for (int i = 0; i < numPivotClassifiers; i++) {
            final RandomIter classifierIter = RandomIterFactory.create(pivotClassifierImages[i], dataImageBounds);
            final boolean checkForNoData = (noDataForPivotClassifierImages != null && !Double.isNaN(noDataForPivotClassifierImages[i])) ?
                    true : false; 
            final int noDataClassifierValue = checkForNoData ? (int)noDataForPivotClassifierImages[i] : 0;
            pivotClassifiers[i] = new ClassifierObject(classifierIter, checkForNoData, noDataClassifierValue);
        }
        
        // --------------------------------
        //
        // Compute statistics
        //
        // --------------------------------
        if (!rangeLocalStats) {
            classifiedStats = compileClassifiedStatistics(dataIter, classifiers, pivotClassifiers);
        } else {
            classifiedStats = compileLocalRangeStatistics(dataIter, classifiers);
        }
        
        // --------------------------------
        // 
        // Closing/disposing the iterators
        // 
        // --------------------------------
        dataIter.done();
        for (int i = 0; i < numClassifiers; i++) {
            classifiers[i].classifierIter.done();
        }
        for (int i = 0; i < numPivotClassifiers; i++) {
            pivotClassifiers[i].classifierIter.done();
        }

        return classifiedStats;
    }

    /**
     * Called by {@link #compileClassifiedStatistics} to lazily create a
     * {@link StreamingSampleStats} object for each classifier. The new object
     * is added to the provided {@code resultsPerBand} {@code Map}.
     * 
     * @param resultsPerBand
     *            {@code Map} of results by classifier
     * @param classifierKey
     *          the classifier key referring to this statistic
     * @param rangesType
     *          the range type
     * @param ranges
     *          a List of Range to be added to these stats.
     *          
     * 
     * @return a new {@code StreamingSampleStats} object
     */
    protected StreamingSampleStats setupStats(Map<MultiKey, StreamingSampleStats> resultsPerBand,
            MultiKey classifierKey, Range.Type rangesType, List<Range<Double>> ranges) {
        StreamingSampleStats sampleStats = new StreamingSampleStats(rangesType);
        for (Range<Double> r : ranges) {
            sampleStats.addRange(r);
        }
        for (Range<Double> r : noDataRanges) {
            sampleStats.addNoDataRange(r);
        }
        sampleStats.setStatistics(stats);
        resultsPerBand.put(classifierKey, sampleStats);
        return sampleStats;
    }

    /**
     * Used to calculate statistics against classifier rasters.
     * @param dataIter 
     *          the input image data iterator
     * @param classifiers 
     *          the classifiers objects for the classified stat 
     * 
     * @return the results as a {@code ClassifiedStats} instance
     */
    private ClassifiedStats compileClassifiedStatistics(
            final RandomIter dataIter,
            ClassifierObject[] classifiers,
            ClassifierObject[] pivotClassifiers) {
        ClassifiedStats classifiedStats = new ClassifiedStats();
        Map<Integer, List<Map<MultiKey, StreamingSampleStats>>> results = CollectionFactory.sortedMap();
        final int numPivots = pivotClassifiers != null ? pivotClassifiers.length : 0;
        for (Integer srcBand : srcBands) {
            // If pivots are present, grouping the results by pivot
            if (numPivots > 0){
                List<Map<MultiKey, StreamingSampleStats>> pivotLists = 
                    new ArrayList<Map<MultiKey,StreamingSampleStats>>(numPivots);
                for (int i = 0; i < numPivots; i++){
                    Map<MultiKey, StreamingSampleStats> resultsPerBand = new HashMap<MultiKey, StreamingSampleStats>();
                    pivotLists.add(resultsPerBand);
                }
                results.put(srcBand, pivotLists);                
            } else {
                // No pivots at all, results as singleton list
                Map<MultiKey, StreamingSampleStats> resultsPerBand = new HashMap<MultiKey, StreamingSampleStats>();
                List<Map<MultiKey, StreamingSampleStats>> singleElement = Collections.singletonList(resultsPerBand);
                results.put(srcBand, singleElement);   
            }
        }
        
        // Init iterations parameters
        Type localRangeType = Range.Type.EXCLUDE;
        List<Range<Double>> localRanges = ranges;
        
        // Computing statistics
        computeStatsOnTiles(dataIter, classifiers, pivotClassifiers, localRangeType, localRanges, results);
        
        // Setting results
        for (Integer band : srcBands) {
            int numElements = numPivots > 0 ? numPivots : 1;
            List<Map<MultiKey, StreamingSampleStats>> resultList = results.get(band);
            for (int i = 0; i < numElements; i++){
                Map<MultiKey, StreamingSampleStats> resultMap = resultList.get(i);
                Set<MultiKey> classifierSetForBand = resultMap.keySet();
                for (MultiKey classifier : classifierSetForBand) {
                    classifiedStats.setResults(band, i, classifier, resultMap.get(classifier));
                }
            }
        }
        return classifiedStats;
    }

    /**
     * Used to calculate statistics when range local statistics are required.
     * @param dataIter 
     *          the input image data iterator
     * @param classifiers 
     *          the classifiers objects for the classified stat 
     * 
     * @return the results as a {@code ClassifiedStats} instance
     */
    private ClassifiedStats compileLocalRangeStatistics(
            final RandomIter dataIter,
            final ClassifierObject[] classifiers) {
        ClassifiedStats classifiedStats = new ClassifiedStats();
        List<Range<Double>> rangesList = null;
        switch (rangesType) {
        case EXCLUDE:
            List<Range<Double>> inRanges = RangeUtils.createComplement(RangeUtils.sort(ranges));
            rangesList = CollectionFactory.list();
            rangesList.addAll(inRanges);
            break;
        case INCLUDE:
            rangesList = CollectionFactory.list();
            rangesList.addAll(ranges);
            break;
        case UNDEFINED:
            throw new UnsupportedOperationException(
                    "Unable to compute range local statistics on UNDEFINED ranges type");
        }

        Type localRangeType = rangesType;

        // Iterate
        for (Range<Double> range : rangesList) {
            Map<Integer, List<Map<MultiKey, StreamingSampleStats>>> results = CollectionFactory.sortedMap();
            for (int index = 0; index < srcBands.length; index++) {
                Map<MultiKey, StreamingSampleStats> resultsPerBand = 
                    new HashMap<MultiKey, StreamingSampleStats>();
                results.put(index, Collections.singletonList(resultsPerBand));

            }
            final List<Range<Double>> localRanges = Collections.singletonList(range);
            
            // Loop over the tiles
            computeStatsOnTiles(dataIter, classifiers, null, localRangeType, localRanges, results);

            // Setting results
            final Set<MultiKey> classifKeys = new HashSet<MultiKey>();
            for (Integer band : srcBands) {
                Set<MultiKey> classifierSetForBand = results.get(band).get(0).keySet();
                classifKeys.addAll(classifierSetForBand);
            }

            for (int index = 0; index < srcBands.length; index++) {
                for (MultiKey classifier : classifKeys) {
                    //TODO: FIX THIS TO DEAL WITH GROUPS?
                    classifiedStats.setResults(srcBands[index], 0, classifier,
                            results.get(index).get(0).get(classifier), localRanges);
                }
            }
        }
        return classifiedStats;
    }

    /**
     * @param dataIter
     *            an iterator related to the data input.
     * 
     * @param classifiers
     *            a {@code ClassifierObject}'s array of integral data type that
     *            defines the classification for which to calculate summary
     *            data.
     * @param pivotClassifiers
     *            a {@code ClassifierObject}'s array of integral data type that
     *            defines the pivot classification for which to calculate summary
     *            data. Elements of this array are used to form group with the standard 
     *            classifiers. As an instance, suppose the classifiers are [classifier1,
     *            classifier2] and the pivot classifiers are [pivot1, pivot2], then the
     *            stats will be computed on classifiers [pivot1, classifier1, classifier2]
     *            and [pivot2, classifier1, classifier2]. It could be null when no pivot
     *            are used. 
     * @param ranges
     *            an optional list of {@link Range} objects defining values to
     *            include or exclude (depending on {@code rangesType} from the
     *            calculations; may be {@code null} or empty
     * 
     * @param rangesType
     *            specifies whether the {@code ranges} argument defines values
     *            to include or exclude
     * 
     */
    private void computeStatsOnTiles( 
            final RandomIter dataIter,
            final ClassifierObject[] classifiers,
            final ClassifierObject[] pivotClassifiers, 
            final Type rangesType, List<Range<Double>> ranges, 
            Map<Integer, List<Map<MultiKey, StreamingSampleStats>>> results
            ) {
        
        // Initialization
        final int numClassifiers = classifiers.length;
        final int numPivotClassifiers = pivotClassifiers != null ? pivotClassifiers.length : 0;
        final double[] sampleValues = new double[dataImage.getSampleModel().getNumBands()];
        final int pivotClassifiersIncrement = numPivotClassifiers > 0 ? 1 : 0;
        final Integer[] keys = new Integer[numClassifiers + pivotClassifiersIncrement];
        final Integer[] pivotKeys = new Integer[numPivotClassifiers];
        
        // Loop over tiles
        for (int tileY = imageMinTileY; tileY <= imageMaxTileY; tileY++) {
            for (int tileX = imageMinTileX; tileX <= imageMaxTileX; tileX++) {
                for (int tRow = 0; tRow < imageTileHeight; tRow++) {
                    int row = tileY * imageTileHeight + tRow;
                    if (row >= imageMinY && row <= imageMaxY) {
                        for (int tCol = 0; tCol < imageTileWidth; tCol++) {
                            int col = tileX * imageTileWidth + tCol;
                            if (col >= imageMinX && col <= imageMaxX) {
                                if (roi == null || roi.contains(col, row)) {
                                    // Check for noData on classifier Images:
                                    // in case a classifier will refer to a noData pixel skip the stat computation for it.
                                    boolean skipStats = false;
                                    for (int i = 0; i < numClassifiers; i++) {
                                        keys[i+pivotClassifiersIncrement] = classifiers[i].classifierIter.getSample(col, row, 0);
                                        if (classifiers[i].checkForNoData){
                                            skipStats = skipStats || (keys[i+pivotClassifiersIncrement] == classifiers[i].noData);
                                        }
                                    }
                                    for (int i = 0; i < numPivotClassifiers; i++) {
                                        pivotKeys[i] = pivotClassifiers[i].classifierIter.getSample(col, row, 0);
                                        if (pivotClassifiers[i].checkForNoData){
                                            skipStats = skipStats || (pivotKeys[i] == pivotClassifiers[i].noData);
                                        }
                                    }
                                    
                                    if (skipStats){
                                        continue;
                                    }
                                    
                                    //Offer values to statistics operations                                    
                                    for (Integer band : srcBands) {
                                        sampleValues[band] = dataIter.getSampleDouble(col, row, band);
                                        List<Map<MultiKey, StreamingSampleStats>> resultPerBand = results.get(band);
                                        boolean goOn = true;
                                        int i = 0;
                                        while (goOn){
                                            Map<MultiKey, StreamingSampleStats> keyedElement = resultPerBand.get(i);
                                            if (numPivotClassifiers > 0){
                                                keys[0] = pivotKeys[i];
                                                if (i == numPivotClassifiers - 1){
                                                    goOn = false;
                                                }
                                            } else { 
                                                goOn = false;
                                            }
                                            MultiKey mk = createMultiKey(keys);
                                            StreamingSampleStats sss = keyedElement.get(mk);
                                            if (sss == null) {
                                                sss = setupStats(keyedElement, mk, rangesType, ranges);
                                            }
                                            sss.offer(sampleValues[band]);
                                            i++;
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Create a {@link MultiKey} object on top of the specified Keys array.
     * Use the multi input value when possible (to avoid cloning).
     * 
     * @param keys
     * @return a {@link MultiKey} instance set on top of the keys.
     */
    private final static MultiKey createMultiKey(Integer[] keys) {
        final int nKeys = keys.length;
        switch (nKeys){
        case 2:
            return new MultiKey(keys[0], keys[1]);
        case 3:
            return new MultiKey(keys[0], keys[1], keys[2]);
        case 4:
            return new MultiKey(keys[0], keys[1], keys[2], keys[3]);
        case 5:
            return new MultiKey(keys[0], keys[1], keys[2], keys[3], keys[4]);
        default:
            // This constructor will copy keys whilst the previous ones 
            // with specific number of keys (one by one) won't do the copy
            return new MultiKey(keys);
        }
    }

    /**
     * Get the specified property.
     * <p>
     * Use this method to retrieve the calculated statistics as a map of
     * {@code ClassifiedStats} per band by setting {@code name} to
     * {@linkplain ClassifiedStatsDescriptor#CLASSIFIED_STATS_PROPERTY}.
     * 
     * @param name
     *            property name
     * 
     * @return the requested property
     */
    @Override
    public Object getProperty(String name) {
        if (ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY.equalsIgnoreCase(name)) {
            return compileStatistics();
        } else {
            return super.getProperty(name);
        }
    }

    /**
     * Get the class of the given property. For
     * {@linkplain ClassifiedStatsDescriptor#CLASSIFIED_STATS_PROPERTY} this
     * will return {@code Map.class}.
     * 
     * @param name
     *            property name
     * 
     * @return the property class
     */
    @Override
    public Class<?> getPropertyClass(String name) {
        if (ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY.equalsIgnoreCase(name)) {
            return Map.class;
        } else {
            return super.getPropertyClass(name);
        }
    }

    /**
     * Get all property names
     * 
     * @return property names as an array of Strings
     */
    @Override
    public String[] getPropertyNames() {
        String[] names;
        int k = 0;

        String[] superNames = super.getPropertyNames();
        if (superNames != null) {
            names = new String[superNames.length + 1];
            for (String name : super.getPropertyNames()) {
                names[k++] = name;
            }
        } else {
            names = new String[1];
        }

        names[k] = ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY;
        return names;
    }

}
