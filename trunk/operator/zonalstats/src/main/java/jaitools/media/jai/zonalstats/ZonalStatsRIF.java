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

import com.sun.media.jai.opimage.RIFUtil;
import com.sun.media.jai.util.ImageUtil;

import jaitools.numeric.Range;
import jaitools.numeric.Statistic;
import java.awt.RenderingHints;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.List;

import javax.media.jai.ImageLayout;
import javax.media.jai.ROI;
import javax.media.jai.RasterFactory;

/**
 * The image factory for the {@link ZonalStatsOpImage} operation.
 *
 * @author Michael Bedward
 * @author Andrea Antonello
 * @since 1.0
 * @source $URL$
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
     * and the following parameters: "stats", "band", "roi", "zoneTransform"
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

        List<Range<Double>> rangesList = (List<Range<Double>>) paramBlock.getObjectParameter(ZonalStatsDescriptor.RANGE_ARG);

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
                (AffineTransform) paramBlock.getObjectParameter(ZonalStatsDescriptor.ZONE_TRANSFORM);

        return new ZonalStatsOpImage(
                dataImage, zoneImage,
                renderHints,
                layout,
                stats,
                bands,
                roi,
                zoneTransform,
                rangesList);
    }
}

