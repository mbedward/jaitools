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

import jaitools.numeric.Statistic;
import jaitools.utils.CollectionFactory;
import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.Map;
import java.util.SortedSet;
import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.NullOpImage;
import javax.media.jai.OpImage;
import javax.media.jai.ROI;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 * An operator to calculate neighbourhood statistics on a source image.
 * @see KernelStatsDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 */
final class ZonalStatsOpImage extends NullOpImage {

    private int srcBand;

    private ROI roi;

    private Statistic[] stats;
    private Number nilValue;

    private SortedSet<Integer> zones;

    /**
     * Constructor
     * @param dataImage a RenderedImage from which data values will be read
     * @param zoneImage a RenderedImage of integral data type defining the zones for which
     * to calculate summary statistics
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an optional ImageLayout object; if the layout specifies a SampleModel
     * and / or ColorModel that are not valid for the requested statistics (e.g. wrong number
     * of bands) these will be overridden.
     * @param stats an array of Statistic constants naming the statistics required
     * @param band the data image band to process
     * @param roi an optional ROI for data image masking
     * @param ignoreNaN boolean flag for whether to ignore NaN values in the data image
     * @param nilValue value to write to the destination image if no statistic can be calculated
     * @see ZonalStatsDescriptor
     * @see Statistic
     */
    public ZonalStatsOpImage(RenderedImage dataImage, RenderedImage zoneImage,
            Map config,
            ImageLayout layout,
            Statistic[] stats,
            int band,
            ROI roi,
            boolean ignoreNaN,
            Number nilValue) {

        super(dataImage, layout, config, OpImage.OP_COMPUTE_BOUND);

        this.srcBand = band;
        this.stats = stats;

        this.roi = roi;

        if (roi != null) {
            // check that the ROI contains the data image bounds
            Rectangle dataBounds = new Rectangle(
                    dataImage.getMinX(), dataImage.getMinY(), dataImage.getWidth(), dataImage.getHeight());

            if (!roi.getBounds().contains(dataBounds)) {
                throw new IllegalArgumentException("The bounds of the ROI must contain the data image");
            }
        }

        this.nilValue = nilValue;
    }

    /**
     * Compiles the set of zone ID values from the zone image. Note, we are
     * assuming that the zone values are in band 0.
     * 
     * @param zoneImage the zone image
     */
    private void buildZoneList(RenderedImage zoneImage) {
        zones = CollectionFactory.newTreeSet();
        RectIter iter = RectIterFactory.create(zoneImage, null);
        do {
            do {
                zones.add(iter.getSample());
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());
    }

}

