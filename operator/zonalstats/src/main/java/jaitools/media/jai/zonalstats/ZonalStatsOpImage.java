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

import jaitools.CollectionFactory;
import jaitools.numeric.StreamingSampleStats;
import jaitools.numeric.Statistic;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.RenderedImage;
import java.util.Map;
import java.util.SortedSet;
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
 * An operator to calculate neighbourhood data on a source image.
 * @see ZonalStatsDescriptor Description of the algorithm and example
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class ZonalStatsOpImage extends NullOpImage {

    private int srcBand;

    private ROI roi;

    private Statistic[] stats;

    private RenderedImage dataImage;
    private Rectangle dataImageBounds;
    private RenderedImage zoneImage;
    private AffineTransform zoneTransform;
    private SortedSet<Integer> zones;

    /**
     * Constructor
     * @param dataImage a RenderedImage from which data values will be read
     * @param zoneImage a RenderedImage of integral data type defining the zones for which
     * to calculate summary data
     * @param config configurable attributes of the image (see {@link AreaOpImage})
     * @param layout an optional ImageLayout object; if the layout specifies a SampleModel
     * and / or ColorModel that are not valid for the requested data (e.g. wrong number
     * of bands) these will be overridden.
     * @param stats an array of Statistic constants naming the data required
     * @param band the data image band to process
     * @param roi an optional ROI for data image masking
     * @see ZonalStatsDescriptor
     * @see Statistic
     */
    public ZonalStatsOpImage(RenderedImage dataImage, RenderedImage zoneImage,
            Map config,
            ImageLayout layout,
            Statistic[] stats,
            int band,
            ROI roi,
            AffineTransform zoneTransform) {

        super(dataImage, layout, config, OpImage.OP_COMPUTE_BOUND);

        this.dataImage = dataImage;

        dataImageBounds = new Rectangle(
                dataImage.getMinX(), dataImage.getMinY(),
                dataImage.getWidth(), dataImage.getHeight());

        this.zoneImage = zoneImage;
        this.zoneTransform = zoneTransform;

        this.stats = stats;
        this.srcBand = band;

        this.roi = roi;

        if (roi != null) {
            // check that the ROI contains the data image bounds
            if (!roi.getBounds().contains(dataImageBounds)) {
                throw new IllegalArgumentException("The bounds of the ROI must contain the data image");
            }
        }
    }

    /**
     * Compiles the set of zone ID values from the zone image. Note, we are
     * assuming that the zone values are in band 0.
     * 
     * @param zoneImage the zone image
     */
    private void buildZoneList() {
        zones = CollectionFactory.newTreeSet();
        RectIter iter = RectIterFactory.create(zoneImage, null);
        do {
            do {
                zones.add(iter.getSample());
            } while (!iter.nextPixelDone());
            iter.startPixels();
        } while (!iter.nextLineDone());
    }

    @Override
    public Object getProperty(String name) {
        if (ZonalStatsDescriptor.ZONAL_STATS_PROPERTY.equalsIgnoreCase(name)) {
            return getZonalStats();
        } else {
            return super.getProperty(name);
        }
    }

    private ZonalStats getZonalStats() {
        buildZoneList();

        Map<Integer, StreamingSampleStats> results = CollectionFactory.newTreeMap();

        for (Integer zone : zones) {
            StreamingSampleStats sampleStats = new StreamingSampleStats();
            sampleStats.setStatistics(stats);
            results.put(zone, sampleStats);
        }

        RectIter dataIter = RectIterFactory.create(dataImage, null);
        if (zoneTransform == null) {  // Idenity transform assumed
            RectIter zoneIter = RectIterFactory.create(zoneImage, dataImageBounds);

            int y = dataImage.getMinY();
            do {
                int x = dataImage.getMinX();
                do {
                    if (roi == null || roi.contains(x, y)) {
                        double value = dataIter.getSampleDouble(srcBand);
                        int zone = zoneIter.getSample();
                        results.get(zone).addSample(value);
                    }
                    zoneIter.nextPixelDone(); // safe call
                    x++ ;
                } while (!dataIter.nextPixelDone());

                dataIter.startPixels();
                zoneIter.startPixels();
                zoneIter.nextLineDone(); // safe call
                y++ ;

            } while (!dataIter.nextLineDone());

        } else {
            RandomIter zoneIter = RandomIterFactory.create(zoneImage, dataImageBounds);
            Point2D.Double dataPos = new Point2D.Double();
            Point2D.Double zonePos = new Point2D.Double();
            dataPos.y = dataImage.getMinY();
            do {
                dataPos.x = dataImage.getMinX();
                do {
                    if (roi == null | roi.contains(dataPos)) {
                        double value = dataIter.getSampleDouble(srcBand);
                        zoneTransform.transform(dataPos, zonePos);
                        int zone = zoneIter.getSample((int)zonePos.x, (int)zonePos.y, 0);
                        results.get(zone).addSample(value);
                    }
                    dataPos.x++ ;
                } while (!dataIter.nextPixelDone());

                dataIter.startPixels();
                dataPos.y++ ;

            } while (!dataIter.nextLineDone());
        }

        ZonalStats zonalStats = new ZonalStats(stats, zones);

        for (Integer zone : zones) {
            zonalStats.setZoneResults(zone, results.get(zone));
        }

        return zonalStats;
    }

    @Override
    public Class getPropertyClass(String name) {
        return ZonalStats.class;
    }

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

        names[k] = "ZonalStats";
        return names;
    }


}

