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

package org.jaitools.media.jai.zonalstats;

import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
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

import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.util.ImageUtil;

import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;

/**
 * The image factory for the {@link ZonalStatsOpImage} operation.
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @author Daniele Romagnoli, GeoSolutions S.A.S.
 * @since 1.0
 * @version $Id$
 */
public class ZonalStatsRIF implements RenderedImageFactory {

    /** Constructor */
    public ZonalStatsRIF() {
    }

    /**
     * Create a new instance of ZonalStatsOpImage in the rendered layer.
     *
     * @param paramBlock specifies the source image, the optional zone image,
     * and the following parameters: "stats", "band", "roi", "zoneTransform", "ranges",
     * "rangesType", "rangeLocalStats"
     *
     * @param renderHints optional RenderingHints object
     */
    public RenderedImage create(ParameterBlock paramBlock, RenderingHints renderHints) {

        RenderedImage dataImage = paramBlock.getRenderedSource(ZonalStatsDescriptor.DATA_IMAGE);
        RenderedImage zoneImage = null;

        if (paramBlock.getNumSources() == 2) {
            zoneImage = paramBlock.getRenderedSource(ZonalStatsDescriptor.ZONE_IMAGE);
        }

        ImageLayout layout = RIFUtil.getImageLayoutHint(renderHints);
        if (layout == null) layout = new ImageLayout();

        Statistic[] stats =
                (Statistic[]) paramBlock.getObjectParameter(ZonalStatsDescriptor.STATS_ARG);

        Integer[] bands = (Integer[]) paramBlock.getObjectParameter(ZonalStatsDescriptor.BAND_ARG);

        Object localStats = paramBlock.getObjectParameter(ZonalStatsDescriptor.RANGE_LOCAL_STATS_ARG);
        Boolean rangeLocalStats = localStats != null ? (Boolean) localStats : Boolean.FALSE;

        Object rng = paramBlock.getObjectParameter(ZonalStatsDescriptor.RANGES_ARG);
        Collection<Range<Double>> ranges = rng != null ? (Collection<Range<Double>>) rng : null;

        Object noDataRng = paramBlock.getObjectParameter(ZonalStatsDescriptor.NODATA_RANGES_ARG);
        Collection<Range<Double>> noDataRanges = noDataRng != null ? (Collection<Range<Double>>) noDataRng : null;

        Object rngType = paramBlock.getObjectParameter(ZonalStatsDescriptor.RANGES_TYPE_ARG);
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

        ROI roi = (ROI) paramBlock.getObjectParameter(ZonalStatsDescriptor.ROI_ARG);

        AffineTransform zoneTransform =
                (AffineTransform) paramBlock.getObjectParameter(ZonalStatsDescriptor.ZONE_TRANSFORM_ARG);

        return new ZonalStatsOpImage(
                dataImage, zoneImage,
                renderHints,
                layout,
                stats,
                bands,
                roi,
                zoneTransform,
                ranges,
                rangesType,
                rangeLocalStats,
                noDataRanges
                );
    }
}

