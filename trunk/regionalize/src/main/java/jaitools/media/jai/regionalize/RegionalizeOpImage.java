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
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;
import javax.media.jai.PointOpImage;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 * An operator to identify regions of uniform value, within
 * a user-specified tolerance, in the source image. Produces a
 * destination image of these regions where pixel values are equal
 * to region ID.
 *
 * @see RegionalizeDescriptor
 * @see RegionData
 * 
 * @author Michael Bedward
 */
final class RegionalizeOpImage extends PointOpImage {

    private boolean singleBand;
    private int band;

    private RectIter srcIter;
    private int srcX;
    private int srcY;

    private FloodFiller filler;
    private List<WorkingRegion> regions;

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

        super(source, layout, config, false);

        this.band = band;

        /*
         * @TODO remove later if we expand the operator to
         * deal with multiple bands
         */
        singleBand = true;

        filler = new FloodFiller(this, source, band, tolerance, diagonal);

        regions = CollectionFactory.newList();
    }

    /**
     * Get a property associated with this operator. Use this
     * to retrieve the {@linkplain RegionData} object with the
     * property name {@linkplain RegionalizeDescriptor#REGION_DATA_PROPERTY}
     *
     * @param name property name
     * @return the matching object or null if there was no match
     */
    @Override
    public Object getProperty(String name) {
        if (RegionalizeDescriptor.REGION_DATA_PROPERTY.equalsIgnoreCase(name)) {
            return getRegionData();
        } else {
            return super.getProperty(name);
        }
    }

    /**
     * Get the properties for this operator. These will
     * include the {@linkplain RegionData} object
     */
    @Override
    public Hashtable getProperties() {
        Hashtable props = super.getProperties();
        if (props == null) {
            props = new Hashtable();
        }
        props.put(RegionalizeDescriptor.REGION_DATA_PROPERTY, getRegionData());
        return props;
    }

    /**
     * For internal use
     */
    @Override
    public Class<?> getPropertyClass(String name) {
        if (RegionalizeDescriptor.REGION_DATA_PROPERTY.equalsIgnoreCase(name)) {
            return RegionData.class;
        } else {
            return super.getPropertyClass(name);
        }
    }

    /**
     * For internal use
     */
    @Override
    public String[] getPropertyNames() {
        String[] superNames = super.getPropertyNames();
        int len = superNames != null ? superNames.length + 1 : 1;
        String[] names = new String[len];

        int k = 0;
        if (len > 1) {
            for (String name : superNames) {
                names[k++] = name;
            }
        }
        names[k] = RegionalizeDescriptor.REGION_DATA_PROPERTY;

        return names;
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
    protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {

        srcIter = RectIterFactory.create(sources[0], destRect);
        if (singleBand) {
            for (int i = band; i > 0; i--) {
                srcIter.nextBand();
            }
        }

        srcX = destRect.x;
        srcY = destRect.y;

        filler.setDestination(dest, destRect);
        List<WorkingRegion> carryOverRegions = filler.getCarryOverRegions();
        for (WorkingRegion cor : carryOverRegions) {
            boolean found = false;
            for (WorkingRegion r : regions) {
                if (r.getID() == cor.getID()) {
                    r.expand(cor);
                    found = true;
                    break;
                }
            }
            if (!found) {
                throw new IllegalStateException("carry-over region does not match any existing region");
            }
        }

        do {
            do {
                if (!pixelDone(srcX, srcY)) {
                    double value = srcIter.getSampleDouble();
                    int id = regions.size() + 1;
                    WorkingRegion r = filler.fill(srcX, srcY, id, value);
                    regions.add(r);
                }
                srcX++;

            } while (!srcIter.nextPixelDone());
            srcIter.startPixels();
            srcX = destRect.x;
            srcY++;

        } while (!srcIter.nextLineDone());
    }

    /**
     * Check if the given pixel has already been processed by
     * seeing if it is contained within the regions collected
     * so far
     */
    private boolean pixelDone(int x, int y) {
        for (WorkingRegion reg : regions) {
            if (reg.contains(x, y)) {
                return true;
            }
        }

        return false;
    }

    private RegionData getRegionData() {
        RegionData regionData = new RegionData();
        
        for (WorkingRegion r : regions) {
            regionData.addRegion(r);
        }

        return regionData;
    }
}

