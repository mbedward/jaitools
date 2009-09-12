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

import jaitools.numeric.DoubleComparison;
import jaitools.tilecache.DiskMemTileCache;
import jaitools.utils.CollectionFactory;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
 * <p>
 * To avoid region numbering artefacts on image tile boundaries this
 * operator imposes an order on tile computation (by column within row).
 * If an arbitrary tile is requested by the caller, the operator first
 * checks that all of the preceding tiles have been computed and cached,
 * processing any that have not. The operator creates its own
 * {@link ExecutorService} for sequential tile computations.
 * <p>
 * Each computed tile is cached using an instance of {@link DiskMemTileCache}.
 * The caller can provide this to the operator via {@code RenderingHints}, or set
 * it as the default {@code TileCache} using {@code JAI.getDefaultInstance().setTileCache()}.
 * Otherwise the operator will create a {@code DiskMemTileCache} object for itself.
 *
 *
 * @see RegionalizeDescriptor
 * @see RegionData
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class RegionalizeOpImage extends PointOpImage {

    private static final int NO_REGION = -1;

    private boolean singleBand;
    private boolean diagonal;
    private int band;
    private double tolerance;

    private FloodFiller filler;
    private Map<Integer, WorkingRegion> regions;
    int currentID;

    private ExecutorService executor;
    private final Object computeLock;
    private int nextTileX;
    private int nextTileY;


    private class ComputeTileTask implements Callable<Raster> {

        int tileX, tileY;

        ComputeTileTask(int tileX, int tileY) {
            this.tileX = tileX;
            this.tileY = tileY;
        }

        public Raster call() throws Exception {
            return computeTile(tileX, tileY);
        }

    }

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
        this.tolerance = tolerance;

        /*
         * @TODO remove later if we expand the operator to
         * deal with multiple bands
         */
        this.singleBand = true;

        this.diagonal = diagonal;

        filler = new FloodFiller(source, band, tolerance, diagonal);
        regions = CollectionFactory.newTreeMap();

        /*
         * We want to use DiskMemTileCache to cache all computed tiles. If the
         * client hasn't supplied its own DiskMemTileCache we set one here.
         */
        if (!(this.cache instanceof DiskMemTileCache)) {
            setTileCache(new DiskMemTileCache());
        }

        this.executor = Executors.newSingleThreadExecutor();
        this.computeLock = new Object();

        this.nextTileX = this.nextTileY = 0;
        this.currentID = 1;
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
     * Returns a tile of this image as a <code>Raster</code>.  If the
     * requested tile is completely outside of this image's bounds,
     * this method returns <code>null</code>.
     * <p>
     * The nature of the regionalizing algorithm means that to compute
     * <i>any</i> tile other than the first (top left) we must compute
     * <i>all</i> tiles to avoid region numbering artefacts across
     * tile boundaries.
     *
     * @param tileX  The X index of the tile.
     * @param tileY  The Y index of the tile.
     */
    @Override
    public Raster getTile(int tileX, int tileY) {
        Raster tile = null;

        if (tileX >= getMinTileX() && tileX <= getMaxTileX() &&
            tileY >= getMinTileY() && tileY <= getMaxTileY()) {

            tile = getTileFromCache(tileX, tileY);

            if (tile == null) {
                synchronized (computeLock) {
                    boolean done = false;
                    for (int y = nextTileY; !done && y < getNumYTiles(); y++) {
                        for (int x = nextTileX; !done && x < getNumXTiles(); x++) {
                            try {
                                tile = executor.submit(new ComputeTileTask(x, y)).get();
                                addTileToCache(x, y, tile);
                                
                                // for the first tile only we can stop here
                                if (tileX == 0 && tileY == 0) {
                                    nextTileX = 1;
                                    nextTileY = 0;
                                    done = true;
                                }

                            } catch (ExecutionException execEx) {
                                throw new IllegalStateException(execEx);

                            } catch (InterruptedException intEx) {
                                // @todo is this safe / sensible ?
                                nextTileX = x;
                                nextTileY = y;
                                return null;
                            }
                        }
                    }
                }
            }
        }

        return tile;
    }

    /**
     * Performs regionalization on a specified rectangle.
     *
     * @param sources an array of source {@code Rasters} - only one element
     *        is expected here
     *
     * @param dest a WritableRaster tile containing the area to be computed.
     *
     * @param destRect the rectangle within dest to be processed.
     */
    @Override
    protected void computeRect(PlanarImage[] sources, WritableRaster dest, Rectangle destRect) {

        final int ABOVE = 0, CURRENT = 1;

        int tileX = XToTileX(destRect.x);
        int tileY = YToTileY(destRect.y);

        filler.setDestination(dest, destRect);

        RectIter srcIter = RectIterFactory.create(sources[0], destRect);
        for (int i = 0; i < band; i++) {
            srcIter.nextBand();
        }

        Set<Integer> prevCheckedIDs = new HashSet<Integer>();

        /**
         * Processing loop
         */
        for (int destY = destRect.y, row = 0; row < destRect.height; destY++, row++) {
            for (int destX = destRect.x, col = 0; col < destRect.width; destX++, col++) {

                /*
                 * Check that the pixel has not yet been included in a region
                 * by a previous flood fill
                 */
                if (getRegionForPixel(destX, destY) == NO_REGION) {

                    int id;
                    int prevID = NO_REGION;
                    double srcVal = srcIter.getSampleDouble();
                    WorkingRegion region = null;
                    double refVal = Double.NaN;

                    /*
                     * If this is the top row of the destination, check for
                     * connection with regions above
                     */
                    if (row == 0 && tileY > getMinTileY()) {
                        id = getRegionForPixel(destX, destY - 1);
                        assert(id != NO_REGION);
                        refVal = regions.get(id).getValue();
                        if (DoubleComparison.dzero(srcVal - refVal, tolerance)) {
                            prevID = id;
                        } else {
                            prevCheckedIDs.add(id);
                        }

                        /*
                         * If the N neighbour's region didn't match and we are
                         * allowing diagonal connectedness, try the NE
                         * and NW neighbours
                         */
                        if (prevID == NO_REGION && diagonal) {
                            if (destX - 1 >= getMinX()) {
                                id = getRegionForPixel(destX-1, destY - 1);
                                assert(id != NO_REGION);
                                if (!prevCheckedIDs.contains(id)) {
                                    refVal = regions.get(id).getValue();
                                    if (DoubleComparison.dzero(srcVal - refVal, tolerance)) {
                                        prevID = id;
                                    } else {
                                        prevCheckedIDs.add(id);
                                    }
                                }
                            }

                            if (prevID == NO_REGION && destX + 1 <= getMaxX()) {
                                id = getRegionForPixel(destX+1, destY - 1);
                                assert(id != NO_REGION);
                                if (!prevCheckedIDs.contains(id)) {
                                    refVal = regions.get(id).getValue();
                                    if (DoubleComparison.dzero(srcVal - refVal, tolerance)) {
                                        prevID = id;
                                    } else {
                                        prevCheckedIDs.add(id);
                                    }
                                }
                            }
                        }
                    }

                    if (prevID == NO_REGION) {
                        /*
                         * If this is the left edge of the destination, check for
                         * connection with regions from the W and SW (if diagonal)
                         * neighbours
                         */
                        if (col == 0 && tileX > getMinTileX()) {
                            id = getRegionForPixel(destX-1, destY);
                            assert(id != NO_REGION);
                            if (!prevCheckedIDs.contains(id)) {
                                refVal = regions.get(id).getValue();
                                if (DoubleComparison.dzero(srcVal - refVal, tolerance)) {
                                    prevID = id;
                                } else {
                                    prevCheckedIDs.add(id);
                                }
                            }

                            if (prevID == NO_REGION && diagonal && destY+1 <= getMaxY()) {
                                id = getRegionForPixel(destX-1, destY);
                                assert(id != NO_REGION);
                                if (!prevCheckedIDs.contains(id)) {
                                    refVal = regions.get(id).getValue();
                                    if (DoubleComparison.dzero(srcVal - refVal, tolerance)) {
                                        prevID = id;
                                    }
                                }
                            }
                        }
                    }

                    /**
                     * If we found a matching region from a preceding tile
                     * expand it by flood-filling this tile and merging the
                     * previous and new working regions
                     */
                    if (prevID != NO_REGION) {
                        assert(!Double.isNaN(refVal));
                        region = filler.fill(destX, destY, prevID, refVal);
                        regions.get(prevID).expand(region);

                    } else {
                        /*
                         * Start a new region
                         */
                        region = filler.fill(destX, destY, currentID, srcVal);
                        regions.put(currentID, region);
                        currentID++ ;
                    }
                }

                srcIter.nextPixelDone();
            }

            srcIter.startPixels();
            srcIter.nextLineDone();
        }
    }

    /**
     * Get the ID of the region that contains the given pixel
     * position
     *
     * @return the id of the region that contains this pixel OR
     *         NO_REGION if the pixel hasn't been processed
     */
    private int getRegionForPixel(int x, int y) {
        for (WorkingRegion reg : regions.values()) {
            if (reg.contains(x, y)) {
                return reg.getID();
            }
        }

        return NO_REGION;
    }

    private RegionData getRegionData() {
        RegionData regionData = new RegionData();
        
        for (WorkingRegion r : regions.values()) {
            regionData.addRegion(r);
        }

        return regionData;
    }
}

