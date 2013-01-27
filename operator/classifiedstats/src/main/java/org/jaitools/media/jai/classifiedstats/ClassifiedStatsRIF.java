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

import java.awt.RenderingHints;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.Collection;

import javax.media.jai.ImageLayout;
import javax.media.jai.ROI;
import javax.media.jai.RasterFactory;

import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;

import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.util.ImageUtil;

/**
 * The image factory for the {@link ClassifiedStatsOpImage} operation.
 *
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.2
 */
public class ClassifiedStatsRIF implements RenderedImageFactory {

    /** Constructor */
    public ClassifiedStatsRIF() {
    }

    /**
     * Create a new instance of ClassifiedStatsOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image,
     * and the following parameters: "stats", "band", "roi", "ranges",
     * "rangesType", "rangeLocalStats"
     *
     * @param renderHints optional RenderingHints object
     */
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {

        RenderedImage dataImage = paramBlock.getRenderedSource(ClassifiedStatsDescriptor.DATA_IMAGE);
        RenderedImage[] classifierImages = null;
        RenderedImage[] pivotClassifierImages = null;

        classifierImages = (RenderedImage[]) paramBlock.getObjectParameter(ClassifiedStatsDescriptor.CLASSIFIER_ARG);
        pivotClassifierImages = (RenderedImage[]) paramBlock.getObjectParameter(ClassifiedStatsDescriptor.PIVOT_CLASSIFIER_ARG);

        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        if (layout == null) layout = new ImageLayout();

        Statistic[] stats =
                (Statistic[]) paramBlock.getObjectParameter(ClassifiedStatsDescriptor.STATS_ARG);

        Integer[] bands = (Integer[]) paramBlock.getObjectParameter(ClassifiedStatsDescriptor.BAND_ARG);

        Object localStats = paramBlock.getObjectParameter(ClassifiedStatsDescriptor.RANGE_LOCAL_STATS_ARG);
        Boolean rangeLocalStats = localStats != null ? (Boolean) localStats : Boolean.FALSE;

        Object rng = paramBlock.getObjectParameter(ClassifiedStatsDescriptor.RANGES_ARG);
        Collection<Range<Double>> ranges = rng != null ? (Collection<Range<Double>>) rng : null;

        Object noDataRng = paramBlock.getObjectParameter(ClassifiedStatsDescriptor.NODATA_RANGES_ARG);
        Collection<Range<Double>> noDataRanges = noDataRng != null ? (Collection<Range<Double>>) noDataRng : null;

        Object rngType = paramBlock.getObjectParameter(ClassifiedStatsDescriptor.RANGES_TYPE_ARG);
        Range.Type rangesType = rngType != null ? (Range.Type) rngType : rng != null ? Range.Type.EXCLUDE : Range.Type.UNDEFINED;

        SampleModel sm = layout.getSampleModel(null);
        if (sm == null || sm.getNumBands() != stats.length) {

            int dataType = dataImage.getSampleModel().getDataType();
            if (dataType != DataBuffer.TYPE_FLOAT && dataType != DataBuffer.TYPE_DOUBLE) {
                for (Statistic stat : stats) {
                    if (!stat.supportsIntegralResult()) {
                        dataType = DataBuffer.TYPE_DOUBLE;
                        break;
                    }
                }
            }

            sm = RasterFactory.createComponentSampleModel(
                    dataImage.getSampleModel(),
                    dataType,
                    dataImage.getWidth(), dataImage.getHeight(), stats.length);

            layout.setSampleModel(sm);
            if (layout.getColorModel(null) != null) {
                ColorModel cm = ImageUtil.getCompatibleColorModel(sm, renderHints);
                layout.setColorModel(cm);
            }
        }
        
        Double[] noDataClassifiers = (Double[]) paramBlock.getObjectParameter(
                ClassifiedStatsDescriptor.NODATA_CLASSIFIER_ARG);
        
        Double[] noDataPivotClassifiers = (Double[]) paramBlock.getObjectParameter(
                ClassifiedStatsDescriptor.NODATA_PIVOT_CLASSIFIER_ARG);

        ROI roi = (ROI) paramBlock.getObjectParameter(ClassifiedStatsDescriptor.ROI_ARG);

        return new ClassifiedStatsOpImage(
                dataImage, 
                classifierImages, 
                pivotClassifierImages,
                renderHints,
                layout,
                stats,
                bands,
                roi,
                ranges,
                rangesType,
                rangeLocalStats,
                noDataRanges,
                noDataClassifiers, 
                noDataPivotClassifiers
                );
    }
}

