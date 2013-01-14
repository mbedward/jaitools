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
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.renderable.ParameterBlock;
import java.util.Collection;
import java.util.List;

import javax.media.jai.OperationDescriptorImpl;
import javax.media.jai.ROI;
import javax.media.jai.registry.RenderedRegistryMode;

import org.jaitools.numeric.Range;
import org.jaitools.numeric.RangeComparator;
import org.jaitools.numeric.RangeUtils;
import org.jaitools.numeric.Statistic;

/**
 * Calculates a number of classified summary statistics, for the whole data image. 
 * Optionally, an ROI can be provided to constrain which areas of the data image
 * will be sampled for calculation of statistics.
 * <p>
 * Use of this operator is similar to the standard JAI statistics operators such as
 * {@link javax.media.jai.operator.HistogramDescriptor} where the source image is
 * simply passed through to the destination image and the results of the operation are
 * retrieved as a property. For this operator the property name can be reliably
 * referred to via the {@link #CLASSIFIED_STATS_PROPERTY} constant.
 * <p>
 * The operator uses the {@link org.jaitools.numeric.StreamingSampleStats} class for its
 * calculations, allowing it to handle very large images for statistics other than
 * {@link org.jaitools.numeric.Statistic#MEDIAN}, for which the
 * {@link org.jaitools.numeric.Statistic#APPROX_MEDIAN} alternative is provided.
 * <p>
 * Note that the source name for this operator are "dataImage" 
 * 
 * <p>
 * The range of data image values that contribute to the analysis can be constrained
 * in two ways: with the "ranges" parameter and the "noDataRanges" parameter.
 * Each of these parameters take a {@code Collection} of {@link Range} objects.
 * <p>
 * The "ranges" parameter allows you to define values to include or exclude from 
 * the calculations, the choice being specified by the associated "rangesType" parameter.
 * If "rangesType" is {@code Range.Type#INCLUDE} you can also request that 
 * statistics be calculated separately for each range of values by setting the
 * "rangeLocalStats" parameter to {@code Boolean.TRUE}.
 * <p>
 * The "noDataRanges" parameter allows you to define values to treat as NODATA. 
 * As well as being excluded from calculations of statistics, the frequency of 
 * NODATA values is tracked by the operator and can be retrieved from the results.
 * <p>
 * The "classifiers" parameter allows you to define the classifiers used on stats computation.
 * As an instance, suppose you have classifiers like [age, job].
 * Where age has byte values like [18,19,20,...,80]
 * job has byte values like [0,1,2,3,4,5,6,...,20] // 0: employer, 1: consultant, ..., 20: professional dancer
 * Statistic computations made on pixels which belong to the same age value and job value will 
 * be grouped together. So you will potentially have in output statistics for the group of pixels
 * related to the set of pixels classifier belonging:
 *   
 * age = 18 & job = 0 
 * age = 18 & job = 1
 * age = 18 & job = 2
 * ....
 * age = 19 & job = 0 
 * age = 19 & job = 1
 * age = 19 & job = 2
 * ...
 * 
 * A MultiKey object will be used to represent these tuples of key.
 * Only the tuples found during the computation will be reported as output results.
 * As an instance, in case there isn't any pixel belonging to age = 70 & job = 20 (professional dancer),
 * the output won't contain a <80,20> tuple.
 * <p>
 * The optional "noDataClassifiers" parameter allows you to define a specific value to treat as NODATA 
 * for each classifier image. When specifying the noDataClassifierRanges array entries, make sure to 
 * use the same order you have used to specify the "classifiers" parameter. The first no data in the
 * array will be used for the first classifier, the second no data in the array will be used for the
 * second classifier, and so on... use Double.NaN in case you won't (or you don't know) specify a noData 
 * for a specific entry.   
 * Note that although classifier are always of integer types, we use double to allow specifying NaN 
 * as "unknown/unspecified" element.
 * <p>
 * Optionally, you can also specify "pivotClassifiers" (when more then one is needed).
 * Pivots are classifiers which should be used to form different groups containing 
 * the same common classifiers and only a distinct (pivot) classifier.
 * As an instance, suppose you have classifiers like [age, job, sex] and classifiers like
 * [country, district, town]. Then you would like to do 3 classified stats like this:<BR>
 * - country, age, job, sex<BR>
 * - district, age, job, sex<BR>
 * - town, age, job, sex<BR>   
 * 
 * You can specify a "pivotClassifiers" parameter with [country, district, town] and the standard
 * "classifiers" parameter with [age, job, sex]. Optionally you can also specify noData for pivot 
 * classifiers (make sure to respect the proper order: first NoData of the array refers to the first
 * pivot classifier, second NoData of the array refers to the second pivot classifier,...)
 * <p>
 * The "noDataPivotClassifiers" parameter allows you to define a specific value to treat as NODATA for 
 * each pivot classifier image. The same remarks made on "noDataClassifiers" apply to this paramter.
 * <p>
 * 
 * 
 * Example of use...
 * <pre><code>
 * RenderedImage myData = ...
 * RenderedImage myClassifierImage0 = ...
 * RenderedImage myClassifierImage1 = ...
 * RenderedImage myClassifierImage2 = ...
 * RenderedImage pivotClassifierImage0 = ...
 * RenderedImage pivotClassifierImage1 = ...
 * 
 *
 * ParameterBlockJAI pb = new ParameterBlockJAI("ClassifiedStats");
 * pb.setSource("dataImage", myData);
 *
 * Statistic[] stats = {
 *     Statistic.MIN,
 *     Statistic.MAX,
 *     Statistic.MEAN,
 *     Statistic.APPROX_MEDIAN,
 *     Statistic.SDEV
 * };
 *
 * pb.setParameter("stats", stats);
 * pb.setParameter("classifiers", new RenderedImage[]{myClassifierImage0, myClassifierImage1, myClassifierImage2});
 * pb.setParameter("pivotClassifiers", new RenderedImage[]{pivotClassifierImage0, pivotClassifierImage1});
 * pb.setParameter("noDataClassifiers", new Double[] {0d, -1d, -9999d});
   pb.setParameter("noDataPivotClassifiers", new Double[] {-32768d, Double.NaN});
 * RenderedOp op = JAI.create("ClassifiedStats", pb);
 *
 * ClassifiedStats stats = (ClassifiedStats) op.getProperty(ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY);
 *
 * // print results to console
 * Map<MultiKey, List<Result>> results = stats.results().get(0);
 * Set<MultiKey> multik = results.keySet();
 * Iterator<MultiKey> it = multik.iterator();
 * while (it.hasNext()) {
 *      MultiKey key = it.next(); 
 *      List<Result> rs = results.get(key);
 *      for (Result r: rs){
 *              System.out.println(r.toString());
 *      }
 * }
 * </code></pre>
 * 
 * The {@code ClassifiedStats} object returned by the {@code getProperty} method above allows
 * you to examine results by image band, classifier key, pivot, statistic, range.
 * As output, you will get the List of results where the i-th element is related to the 
 * classified stats computed on top of the i-th pivot element
 *  
 * <pre><code>
 * List<Map<MultiKey, List<Result>>> results = stats.results();
 * Map<MultiKey, List<Result>> pivot0Results = results.get(0);
 * Map<MultiKey, List<Result>> pivot1Results = results.get(1);
 * Map<MultiKey, List<Result>> pivot2Results = results.get(2);
 * </code></pre>
 *
 * <b>Parameters</b>
 * <table border="1">
 * <tr>
 * <th>Name</th><th>Type</th><th>Description</th><th>Default value</th>
 * </tr>
 * <tr>
 * <td>classifiers</td><td>RenderedImage[]</td>
 * <td>classifier images to be used in computations</td>
 * <td>NO DEFAULT although this parameter is mandatory</td>
 * </tr>
 * <tr>
 * <td>pivotClassifiers</td><td>RenderedImage[]</td>
 * <td>optional pivot classifier images to be used in computations.
 * Elements of this array are used to form group with the standard 
 * classifiers. As an instance, suppose the classifiers are [classifier1, 
 * classifier2] and the pivot classifiers are [pivot1, pivot2], then the 
 * stats will be computed on classifiers [pivot1, classifier1, classifier2] 
 * and [pivot2, classifier1, classifier2]. </td>
 * <td>null</td>
 * </tr>
 *
 * <tr>
 * <td>stats</td><td>Statistic[]</td><td>Statistics to calculate</td><td>NO DEFAULT</td>
 * </tr>
 * <tr>
 * <td>bands</td><td>Integer[]</td><td>Image bands to sample</td><td>{0}</td>
 * </tr>
 * <tr>
 * <td>roi</td><td>ROI</td><td>An optional ROI to constrain sampling</td><td>null</td>
 * </tr>
 * <tr>
 * <td>ranges</td><td>Collection&lt;Range></td><td>Ranges of values to include or exclude</td><td>null (include all data values)</td>
 * </tr>
 * <tr>
 * <td>rangesType</td><td>Range.Type</td>
 * <td>How to treat values supplied via the "ranges" parameter</td>
 * <td>Range.Type.INCLUDE</td>
 * </tr>
 * <tr>
 * <td>rangeLocalStats</td><td>Boolean</td>
 * <td>If Ranges are supplied via the "ranges" parameter, whether to calculate
 * statistics for each of them separately</td>
 * <td>Boolean.FALSE</td>
 * </tr>
 * <tr>
 * <td>noDataRanges</td><td>Collection&lt;Range></td>
 * <td>Ranges of values to treat specifically as NODATA
 * </td><td>null (no NODATA values defined)</td>
 * </tr>
 * <tr>
 * <td>noDataClassifiers</td><td>Double[]</td>
 * <td>NoData specific for image classifiers. The order of the noData elements of the array
 * shall respect the order of the elements within the image classifiers array. 
 * NoData are specified as Double although they refer to classifiers images which are of 
 * integer types. Using a Double allows to specifiy NaN in case some specific noData entries
 * aren't unavailable for some classifier images. As an instance 
 * [-9999, Double.NaN, -32768, 0, Double.NaN] in case there isn't any noData for classifierImage 
 * 1 and 4 (starting from index 0).
 * </td><td>null (no NODATA values defined)</td>
 * </tr>
 * <tr>
 * <td>noDataPivotClassifiers</td><td>Double[]</td>
 * <td>NoData specific for pivot image classifiers. The order of the noData elements of the array
 * shall respect the order of the elements within the pivot image classifiers array. 
 * NoData are specified as Double although they refer to pivot classifiers images which are of 
 * integer types. Using a Double allows to specifiy NaN in case some specific noData entries
 * aren't unavailable for some pivot classifier images. As an instance 
 * [-9999, Double.NaN, -32768, 0, Double.NaN] in case there isn't any noData for pivotClassifierImage 
 * 1 and 4 (starting from index 0).
 * </td><td>null (no NODATA values defined)</td>
 * </tr>

 * </table>
 *
 * @see Result
 * @see Statistic
 * @see org.jaitools.numeric.StreamingSampleStats
 * @see ClassifiedStats
 *
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.2
 */
public class ClassifiedStatsDescriptor extends OperationDescriptorImpl {

    private static final long serialVersionUID = -526208282980300507L;

    /** Property name used to retrieve the results */
    public static final String CLASSIFIED_STATS_PROPERTY = "ClassifiedStatsProperty";

    static final int DATA_IMAGE = 0;
    static final int CLASSIFIER_IMAGE = 1;

    private static final String[] srcImageNames = {"dataImage"};

    private static final Class<?>[][] srcImageClasses = {{RenderedImage.class}};

    static final int CLASSIFIER_ARG = 0;
    static final int PIVOT_CLASSIFIER_ARG = 1;
    static final int STATS_ARG = 2;
    static final int BAND_ARG = 3;
    static final int ROI_ARG = 4;
    static final int RANGES_ARG = 5;
    static final int RANGES_TYPE_ARG = 6;
    static final int RANGE_LOCAL_STATS_ARG = 7;
    static final int NODATA_RANGES_ARG = 8;
    static final int NODATA_CLASSIFIER_ARG = 9;
    static final int NODATA_PIVOT_CLASSIFIER_ARG = 10;

    private static final String[] paramNames = {
        "classifiers",
        "pivotClassifiers",
        "stats", 
        "bands", 
        "roi", 
        "ranges", 
        "rangesType", 
        "rangeLocalStats", 
        "noDataRanges",
        "noDataClassifiers",
        "noDataPivotClassifiers"
        
    };

    private static final Class<?>[] paramClasses = {
        RenderedImage[].class,
        RenderedImage[].class,
        Statistic[].class, 
        Integer[].class,
        javax.media.jai.ROI.class, 
        Collection.class, 
        Range.Type.class, 
        Boolean.class, 
        Collection.class,
        Double[].class,
        Double[].class
    };

    @SuppressWarnings("rawtypes")
    private static final Object[] paramDefaults = {
        null,
        null,
        NO_PARAMETER_DEFAULT,
        new Integer[]{Integer.valueOf(0)}, 
        (ROI) null,  
        (Collection) null, 
        Range.Type.UNDEFINED, 
        Boolean.FALSE, 
        (Collection) null,
        null, null
    };
    

    /** Constructor. */
    public ClassifiedStatsDescriptor() {
        super(
            new String[][] {
                { "GlobalName", "ClassifiedStats" },
                { "LocalName", "ClassifiedStats" },
                { "Vendor", "org.jaitools.media.jai" },
                { "Description", "Calculate neighbourhood statistics" },
                { "DocURL", "http://code.google.com/p/jaitools/" },
                { "Version", "1.2.0" },
                {
                    "arg0Desc",
                    String.format(
                        "%s - an array of RenderedImage representing the classifier "
                        + "input images", paramNames[CLASSIFIER_ARG]) },
                {
                    "arg1Desc",
                    String.format(
                        "%s - an array of RenderedImage representing the pivot classifier "
                        + "input images", paramNames[PIVOT_CLASSIFIER_ARG]) },
                {
                    "arg2Desc",
                    String.format(
                        "%s - an array of Statistic constants specifying the "
                        + "statistics required", paramNames[STATS_ARG]) },

                {
                    "arg3Desc",
                    String.format(
                        "%s (default %s) - the bands of the data image to process",
                        paramNames[BAND_ARG], paramDefaults[BAND_ARG]) },

                {
                    "arg4Desc",
                    String.format(
                        "%s (default ) - an optional ROI for masking the data image",
                        paramNames[ROI_ARG], paramDefaults[ROI_ARG]) },

                {
                    "arg5Desc",
                    String.format("%s (default %s) - an optional Collection of Ranges "
                        + "that define dataImage values to include or exclude",
                        paramNames[RANGES_ARG], paramDefaults[RANGES_ARG]) },

                {
                    "arg6Desc",
                    String.format(
                        "%s (default %s) - whether to include or exclude provided ranges",
                        paramNames[RANGES_TYPE_ARG], paramDefaults[RANGES_TYPE_ARG]) },

                {
                    "arg7Desc",
                    String.format(
                        "%s (default %s) - whether to calculate statistics separately "
                                + "for ranges (when provided)",
                        paramNames[RANGE_LOCAL_STATS_ARG],
                        paramDefaults[RANGE_LOCAL_STATS_ARG]) },
                {
                    "arg8Desc",
                    String.format("%s (default %s) - an optional Collection of Ranges "
                        + "defining values to treat as NODATA",
                        paramNames[NODATA_RANGES_ARG],
                        paramDefaults[NODATA_RANGES_ARG]) },
                {
                    "arg9Desc",
                    String.format(
                        "%s (default %s) - an optional Array of values "
                                + "defining values to treat as NODATA from the classifier raster inputs\n"
                                + "the i-th element of the array refers to the i-th classifier raster source",
                        paramNames[NODATA_CLASSIFIER_ARG],
                        paramDefaults[NODATA_CLASSIFIER_ARG]) },
                {
                    "arg10Desc",
                    String.format(
                        "%s (default %s) - an optional Array of values "
                                + "defining values to treat as NODATA from the pivot classifier raster inputs\n"
                                + "the i-th element of the array refers to the i-th pivot classifier raster source",
                        paramNames[NODATA_PIVOT_CLASSIFIER_ARG],
                        paramDefaults[NODATA_PIVOT_CLASSIFIER_ARG]) },

            },

            new String[] { RenderedRegistryMode.MODE_NAME }, 
            srcImageNames, srcImageClasses,
            paramNames, paramClasses, paramDefaults,
            null // valid values (none defined)
        );
    }

    /**
     * Validates supplied parameters.
     * 
     * @param modeName the rendering mode
     * @param pb the parameter block
     * @param msg a {@code StringBuffer} to receive error messages
     * 
     * @return {@code true} if parameters are valid; {@code false} otherwise
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    @Override
    public boolean validateArguments( String modeName, ParameterBlock pb, StringBuffer msg ) {
        
        final int imageSources = pb.getNumSources();
        if (imageSources == 0) {
            msg.append("ClassifiedStats operator takes 1 source image");
            return false;
        }
        
        // CHECKING CLASSIFIER IMAGES 
        Object renderedObjects = pb.getObjectParameter(CLASSIFIER_ARG);
        RenderedImage[] classifierImages = null;
        if (!(renderedObjects instanceof RenderedImage[])) {
            msg.append(paramNames[CLASSIFIER_ARG]).append(" arg has to be of type RenderedImage[]");
            return false;
        } else {
            classifierImages = (RenderedImage[]) renderedObjects;
        }
        
        // CHECKING PIVOT CLASSIFIER IMAGES
        Object pivotObjects = pb.getObjectParameter(PIVOT_CLASSIFIER_ARG);
        RenderedImage[] pivotClassifierImages = null;
        if (pivotObjects != null && !(pivotObjects instanceof RenderedImage[])) {
            msg.append(paramNames[PIVOT_CLASSIFIER_ARG]).append(" arg has to be of type RenderedImage[]");
            return false;
        } else {
            pivotClassifierImages = (RenderedImage[]) pivotObjects;
        }

        // CHECKING RANGES
        Object rangeObject = pb.getObjectParameter(RANGES_ARG);
        if (rangeObject != null) {
            boolean ok = true;
            if (rangeObject instanceof Collection) {
                Collection coll = (Collection) rangeObject;
                if (!coll.isEmpty()) {
                    Object range = coll.iterator().next();
                    if (!(range instanceof Range)) {
                        msg.append(paramNames[RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
                        ok = false;

                    } else {
                        List sortedRanges = RangeUtils.sort(coll);
                        final int elements = sortedRanges.size();
                        if (elements > 1) {
                            RangeComparator rc = new RangeComparator();
                            List<Range> rr = (List<Range>) sortedRanges;
                            for (int i = 0; i < elements - 1; i++) {
                                Range r1 = rr.get(i);
                                Range r2 = rr.get(i + 1);
                                RangeComparator.Result result = rc.compare(r1, r2);
                                if (RangeComparator.isIntersection(result)) {
                                    ok = false;
                                    msg.append(paramNames[RANGES_ARG]).append(" arg can't contain intersecting ranges");
                                    break;
                                }
                            }
                        }
                    }
                }

            } else if (rangeObject != null) {
                ok = false;
                msg.append(paramNames[RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
            }
            if (!ok) {
                return false;
            }
        }

        // CHECKING NoData RANGES
        Object noDataRangeObject = pb.getObjectParameter(NODATA_RANGES_ARG);
        if (noDataRangeObject != null) {
            boolean ok = true;
            if (noDataRangeObject instanceof List) {
                Object range = ((List) noDataRangeObject).get(0);
                if (!(range instanceof Range)) {
                        msg.append(paramNames[NODATA_RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
                    ok = false;
                }
            } else if (noDataRangeObject != null) {
                ok = false;
                msg.append(paramNames[NODATA_RANGES_ARG]).append(" arg has to be of type List<Range<Double>>");
            }
            if (!ok) {
                return false;
            }
        }

        // CHECKING RANGES TYPE
        Object rangesType = pb.getObjectParameter(RANGES_TYPE_ARG);
        if (rangesType != null && rangesType instanceof Range.Type) {
            Range.Type rt = (Range.Type) rangesType;
            if (rangeObject != null && rt == Range.Type.UNDEFINED) {
                msg.append(paramNames[RANGES_TYPE_ARG]).append(" arg has to be of Type.EXCLUDED or Type.INCLUDED when specifying a Ranges List");
                return false;
            }
        }

        // CHECKING BANDS
        Object bandsObject = pb.getObjectParameter(BAND_ARG);
        Integer[] bands = null;
        if (!(bandsObject instanceof Integer[])) {
            msg.append(paramNames[BAND_ARG]).append(" arg has to be of type Integer[]");
            return false;
        } else {
            bands = (Integer[]) bandsObject;
        }

        // CHECKING DATA IMAGE
        RenderedImage dataImg = pb.getRenderedSource(DATA_IMAGE);
        final int imageBands = dataImg.getSampleModel().getNumBands();
        for( Integer band : bands ) {
            if (band < 0 || band >= imageBands) {
                msg.append("band index out of bounds for source image: ").append(band);
                return false;
            }
        }

        Rectangle dataBounds = new Rectangle(
                dataImg.getMinX(), dataImg.getMinY(),
                dataImg.getWidth(), dataImg.getHeight());

        // CHECKING ROI
        Object roiObject = pb.getObjectParameter(ROI_ARG);
        if (roiObject != null) {
            if (!(roiObject instanceof ROI)) {
                msg.append("The supplied ROI is not a supported class");
                return false;
            }
            if (!((ROI)roiObject).intersects(dataBounds)) {
                msg.append("The supplied ROI does not intersect the source image");
                return false;
            }
        }

        // CHECKING CLASSIFIER IMAGES 
        int numClassifiers = classifierImages != null ? classifierImages.length : 0;
        if (numClassifiers != 0){
            for (int i = 0; i < numClassifiers; i++){
                RenderedImage classifierImage = classifierImages[i];
                final Rectangle classifierBounds = new Rectangle(
                        classifierImage.getMinX(), classifierImage.getMinY(), 
                        classifierImage.getWidth(), classifierImage.getHeight());
                int dataType = classifierImage.getSampleModel().getDataType();
                boolean integralType = false;
                if (dataType == DataBuffer.TYPE_BYTE || dataType == DataBuffer.TYPE_INT
                        || dataType == DataBuffer.TYPE_SHORT || dataType == DataBuffer.TYPE_USHORT) {
                    integralType = true;
                }
    
                if (!integralType) {
                    msg.append("The classifier image must be an integral data type");
                    return false;
                }
                
                if (classifierBounds.width != dataBounds.width || classifierBounds.height != dataBounds.height
                        || classifierBounds.x != dataBounds.x || classifierBounds.y != dataBounds.y){
                    msg.append("Data image bounds and classifier raster bounds should match:\n "
                            + "Data Image: " + dataBounds.toString() + "\n Classifier Image[" + i + "]: " 
                            + classifierBounds.toString());
           
                    return false;
                }
            }
        } else {
            msg.append("At least one Classifier input image should be specified");
            return false;
        }
        
        
        // CHECKING PIVOT CLASSIFIER IMAGES 
        int numPivotClassifier = pivotClassifierImages != null ? pivotClassifierImages.length : 0;
        if (numPivotClassifier != 0){
            for (int i = 0; i < numPivotClassifier; i++){
                RenderedImage classifierImage = pivotClassifierImages[i];
                final Rectangle classifierBounds = new Rectangle(
                        classifierImage.getMinX(), classifierImage.getMinY(), 
                        classifierImage.getWidth(), classifierImage.getHeight());
                int dataType = classifierImage.getSampleModel().getDataType();
                boolean integralType = false;
                if (dataType == DataBuffer.TYPE_BYTE || dataType == DataBuffer.TYPE_INT
                        || dataType == DataBuffer.TYPE_SHORT || dataType == DataBuffer.TYPE_USHORT) {
                    integralType = true;
                }
    
                if (!integralType) {
                    msg.append("The pivot classifier image must be an integral data type");
                    return false;
                }
                
                if (classifierBounds.width != dataBounds.width || classifierBounds.height != dataBounds.height
                        || classifierBounds.x != dataBounds.x || classifierBounds.y != dataBounds.y){
                    msg.append("Data image bounds and pivot classifier raster bounds should match:\n "
                            + "Data Image: " + dataBounds.toString() + "\n Pivot Classifier Image[" + i + "]: " 
                            + classifierBounds.toString());
           
                    return false;
                }
            }
        } 
        
        // CHECKING NODATA FOR CLASSIFIERS 
        Object noDataClassifierArg = pb.getObjectParameter(NODATA_CLASSIFIER_ARG);
        Double[] noDataClassifier = null;
        if (noDataClassifierArg != null){
            if (!(noDataClassifierArg instanceof Double[])) {
            msg.append(paramNames[NODATA_CLASSIFIER_ARG]).append(" arg has to be of type Double[]");
            return false;
            } else {
                noDataClassifier = (Double[]) noDataClassifierArg;
                if (noDataClassifier.length != numClassifiers){
                    msg.append(paramNames[NODATA_CLASSIFIER_ARG]).append(" arg has to have the same " +
                        "number of elements of " + paramNames[CLASSIFIER_ARG] + " = " + numClassifiers + 
                        "whilst its actual size is " + noDataClassifier.length);
                }
            }
        }
        
        // CHECKING NODATA FOR PIVOT CLASSIFIERS
        Object noDataPivotClassifierArg = pb.getObjectParameter(NODATA_PIVOT_CLASSIFIER_ARG);
        Double[] noDataPivotClassifier = null;
        if (noDataPivotClassifierArg != null){
            if (!(noDataPivotClassifierArg instanceof Double[])) {
            msg.append(paramNames[NODATA_PIVOT_CLASSIFIER_ARG]).append(" arg has to be of type Double[]");
            return false;
            } else {
                noDataPivotClassifier = (Double[]) noDataPivotClassifierArg;
                if (noDataPivotClassifier.length != numPivotClassifier){
                    msg.append(paramNames[NODATA_PIVOT_CLASSIFIER_ARG]).append(" arg has to have the same " +
                        "number of elements of " + paramNames[PIVOT_CLASSIFIER_ARG] + " = " 
                        + numPivotClassifier + "whilst its actual size is " 
                        + noDataPivotClassifier.length);
                }
            }
        }

        return true;
    }
}
