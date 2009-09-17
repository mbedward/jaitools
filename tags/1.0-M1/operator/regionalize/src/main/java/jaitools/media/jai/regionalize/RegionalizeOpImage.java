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

import jaitools.CollectionFactory;
import jaitools.imageutils.FillResult;
import jaitools.imageutils.FloodFiller;
import jaitools.tilecache.DiskMemTileCache;
import jaitools.tiledimage.DiskMemImage;

import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.media.jai.AreaOpImage;
import javax.media.jai.ImageLayout;
import javax.media.jai.PointOpImage;
import javax.media.jai.TileCache;

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

    /**
     * Destingation value indicating that the pixel does not
     * belong to a region
     */
    public static final int NO_REGION = 0;

    private boolean singleBand;
    private boolean diagonal;
    private int band;
    private double tolerance;

    private final DiskMemImage regionImage;
    private FloodFiller filler;
    private Map<Integer, Region> regions;
    int currentID;

    private final ExecutorService executor;
    private final Object getTileLock = new Object();
    private final Object computeTileLock = new Object();

    private boolean[] tileComputed;


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

        if (getSampleModel().getDataType() != DataBuffer.TYPE_INT) {
            throw new IllegalStateException("destination sample model must be TYPE_INT");
        }

        this.band = band;
        this.tolerance = tolerance;

        /*
         * @TODO remove later if we expand the operator to
         * deal with multiple bands
         */
        this.singleBand = true;

        this.diagonal = diagonal;

        /*
         * Any tile cache provided by the caller is ignored
         */
        regionImage = new DiskMemImage(getWidth(), getHeight(), getSampleModel());
        setTileCache( regionImage.getTileCache() );

        filler = new FloodFiller(source, band, regionImage, 0, tolerance, diagonal);
        regions = CollectionFactory.newTreeMap();

        this.executor = Executors.newSingleThreadExecutor();

        tileComputed = new boolean[getNumXTiles() * getNumYTiles()];
        Arrays.fill(tileComputed, false);  // paranoia

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
            List<Region> regionData = CollectionFactory.newList();
            regionData.addAll(regions.values());
            return regionData;

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
        props.put(RegionalizeDescriptor.REGION_DATA_PROPERTY, "dynamic");
        return props;
    }

    /**
     * For internal use
     */
    @Override
    public Class<?> getPropertyClass(String name) {
        if (RegionalizeDescriptor.REGION_DATA_PROPERTY.equalsIgnoreCase(name)) {
            return List.class;

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

            if (tileComputed[getTileIndex(tileX, tileY)]) {
                tile = regionImage.getTile(tileX, tileY);
                
            } else {
                synchronized (getTileLock) {
                    try {
                        tile = executor.submit(new ComputeTileTask(tileX, tileY)).get();

                    } catch (ExecutionException execEx) {
                        throw new IllegalStateException(execEx);

                    } catch (InterruptedException intEx) {
                        // @todo is this safe ?
                        return null;
                    }
                }
            }
        }

        return tile;
    }

    @Override
    public Raster computeTile(int tileX, int tileY) {
        Rectangle destRect = getTileRect(tileX, tileY);

        synchronized (computeTileLock) {
            for (int destY = destRect.y, row = 0; row < destRect.height; destY++, row++) {
                for (int destX = destRect.x, col = 0; col < destRect.width; destX++, col++) {

                    if (getRegionForPixel(destX, destY) == NO_REGION) {

                        FillResult fill = filler.fill(destX, destY, currentID);
                        regions.put(currentID, new Region(fill));
                        currentID++;
                    }
                }
            }
            
            tileComputed[getTileIndex(tileX, tileY)] = true;
        }

        return regionImage.getTile(tileX, tileY);
    }



    /**
     * Convenience method to calculate a single value tile coordinate
     * @param tileX tile X coordinate
     * @param tileY tile Y coordinate
     * @return single integer coordinate used to index fields in this class
     */
    private int getTileIndex(int tileX, int tileY) {
        return (tileY - getMinTileY()) * getNumXTiles() + (tileX - getMinTileX());
    }

    /**
     * This method is overridden to prevent it being used because the
     * {@code RegionalizeOpImage} object should be soley responsible for
     * creating tiles and caching them. If invoked an
     * {@linkplain UnsupportedOperationException} will be thrown.
     */
    @Override
    protected void addTileToCache(int tileX, int tileY, Raster tile) {
        throw new UnsupportedOperationException("this method should not be called !");
    }

    /**
     * This method is overridden to ensure that the cache is always addressed
     * through the {@code DiskMemImage} being used by this operator, otherwise
     * tile IDs calculated by the cache will vary with the perceived owner
     * (the image or the operator) of the tile.
     *
     * @param tileX tile X coordinate
     * @param tileY tile Y coordinate
     * @return the requested tile
     */
    @Override
    protected Raster getTileFromCache(int tileX, int tileY) {
        return regionImage.getTile(tileX, tileY);
    }


    /**
     * Set the tile cache. The supplied cache must be an instance of
     * {@code DiskMemTileCache}.
     *
     * @param cache an instance of DiskMemTileCache
     * @throws IllegalArgumentException if cache is null or not an instance
     *         of {@code DiskMemTileCache}
     */
    @Override
    public void setTileCache(TileCache cache) {
        if (cache != null && cache instanceof DiskMemTileCache) {
            super.setTileCache(cache);
        } else {
            throw new IllegalArgumentException("cache must be an instance of DiskMemTileCache");
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
        int tileX = XToTileX(x);
        int tileY = YToTileY(y);
        Raster tile = regionImage.getTile(tileX, tileY);
        assert(tile != null);

        return tile.getSample(x, y, 0);
    }

}

