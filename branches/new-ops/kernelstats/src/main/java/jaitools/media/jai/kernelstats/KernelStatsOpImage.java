/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.media.jai.kernelstats;

import java.awt.Rectangle;
import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.Map;
import javax.media.jai.AreaOpImage;
import javax.media.jai.BorderExtender;
import javax.media.jai.CollectionImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.KernelJAI;
import javax.media.jai.ROI;

/**
 * An operator to calculate neighbourhood statistics on a source image.
 * @see KernelStatsDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 */
final class KernelStatsOpImage extends CollectionImage {

    /**
     * Constructor
     * @param source a RenderedImage.
     * @param extender a BorderExtender, or null.
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *        SampleModel, and ColorModel, or null.
     * @param kernel the convolution kernel
     * @param stats an array of KernelStatistic constants naming the statistics required
     * @throws IllegalArgumentException if the roi's bounds do not contain the entire
     * source image
     * @see KernelStatsDescriptor
     * @see KernelStatistic
     */
    public KernelStatsOpImage(RenderedImage source,
            BorderExtender extender,
            Map config,
            ImageLayout layout,
            KernelJAI kernel,
            KernelStatistic[] stats,
            ROI roi,
            boolean maskSrc,
            boolean maskDest,
            boolean ignoreNaN) {

        if (roi == null) {
            maskSrc = maskDest = false;

        } else {
            // check that the ROI contains the source image bounds
            Rectangle sourceBounds = new Rectangle(
                    source.getMinX(), source.getMinY(), source.getWidth(), source.getHeight());

            if (!roi.getBounds().contains(sourceBounds)) {
                throw new IllegalArgumentException("The bounds of the ROI must contain the source image");
            }
        }

        for (KernelStatistic ks : stats) {
            System.out.println("adding " + ks.toString());
            RenderedImage image = new KernelStatsWorker(source, extender, config, layout,
                    kernel, ks, roi, maskSrc, maskDest, ignoreNaN);

            if (imageCollection == null) {
                imageCollection = new ArrayList<RenderedImage>();
            }
            imageCollection.add(image);
        }
    }
}

