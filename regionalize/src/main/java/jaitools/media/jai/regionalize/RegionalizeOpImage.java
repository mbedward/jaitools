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
package jaitools.media.jai.regionalize;

import jaitools.utils.CollectionFactory;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.List;
import java.util.Map;
import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 * An operator to perform masked convolution on a source image.
 * @see RegionalizeDescriptor Description of the algorithm and example
 * 
 * NOT FUNCTIONAL YET
 *
 * @author Michael Bedward
 */
final class RegionalizeOpImage extends PointOpImage {

    private boolean singleBand;
    private int band;
    private List<Region> regions;
    private double tolerance;

    /**
     * Constructor
     * @param source a RenderedImage.
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an ImageLayout optionally containing the tile grid layout,
     *        SampleModel, and ColorModel, or null.
     * @param band the band to process
     * @param tolerance max absolute difference in value between the starting pixel for
     * a region and any pixel added to that region
     * @param diagonal true to include sub-regions with only diagonal connectedness;
     * false to require orthogonal connectedness
     * 
     * @see RegionalizeDescriptor
     */
    public RegionalizeOpImage(RenderedImage source,
            Map config,
            ImageLayout layout,
            int band,
            double tolerance,
            boolean diagonal) {

        super(source, layout, config, true);

        // @TODO this is just for testing
        singleBand = true;
        this.band = band;
        this.tolerance = tolerance;

        regions = CollectionFactory.newList();
    }

    /**
     * Performs regionalization on a specified rectangle.
     *
     * @param sources an array of source Rasters, guaranteed to provide all
     *        necessary source data for computing the output.
     * @param dest a WritableRaster tile containing the area to be computed.
     * @param destRect the rectangle within dest to be processed.
     */
    @Override
    protected void computeRect(Raster[] sources,
            WritableRaster dest,
            Rectangle destRect) {

        Raster src = sources[0];

        RectIter iter = RectIterFactory.create(src, destRect);
        FloodFiller ff = new FloodFiller(src, dest);

        if (singleBand) {
            for (int i = band; i > 0; i--) {
                iter.nextBand();
            }
        } else {
            band = 0;
        }

        int x = src.getMinX();
        int y = src.getMinY();
        do {
            do {
                do {
                    if (!pixelDone(x, y)) {
                        double value = iter.getSampleDouble();
                        int id = regions.size() + 1;
                        List<ScanSegment> segments = ff.floodFill(band, x, y, value, tolerance);
                        regions.add(new Region(id, value, segments));
                    }
                    x++;

                } while (!iter.nextPixelDone());
                iter.startPixels();
                x = src.getMinX();
                y++;

            } while (!iter.nextLineDone());
            iter.startLines();
            y = src.getMinY();
            band++;

        } while (!(singleBand || iter.nextBandDone()));
    }

    /**
     * Check if the given pixel has already been processed by
     * seeing if it is contained within the regions collected
     * so far
     */
    private boolean pixelDone(int x, int y) {
        for (Region reg : regions) {
            if (reg.contains(x, y)) {
                return true;
            }
        }

        return false;
    }
}

