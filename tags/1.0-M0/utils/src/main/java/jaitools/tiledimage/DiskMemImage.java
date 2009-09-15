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

package jaitools.tiledimage;

import jaitools.tilecache.DiskMemTileCache;
import jaitools.utils.CollectionFactory;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;

/**
 * A tiled image class similar to JAI's standard {@code TiledImage} that uses a
 * {@code DiskMemTileCache} to manage its data. All images tiles are cached on disk
 * with a subset (most recently used) also being cached in memory for faster access.
 * This allows very large images to be handled that would otherwise exhaust
 * the system's memory, albeit at the cost of disk I/O leading to slower tile access.
 *
 * @see DiskMemTileCache
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class DiskMemImage
        extends PlanarImage
        implements WritableRenderedImage {
    
    private DiskMemTileCache tileCache;
    private Rectangle tileGrid;
    private boolean[][] tileInUse;
    private int numTilesInUse;

    private long tileMemorySize;

    private Set<TileObserver> tileObservers;

    /**
     * Minimal constructor. This will set default values for the image's
     * min x and y coordinates (0), x and y tile offsets (0) and <code>ColorModel</code>
     * (<code>null</code>)
     *
     * @param width image width
     * @param height image height
     * @param tileSampleModel a <code>SampleModel</code> specifying the dimensions
     * data type etc. for image tiles
     */
    public DiskMemImage(
            int width, int height,
            SampleModel tileSampleModel) {

        this(0, 0,           // minX, minY
             width, height,
             0, 0,           // tileGridXOffset, tileGridYOffset
             tileSampleModel,
             null            // ColorModel
             );
    }


    /**
     * Constructor. This will set default values for the image's
     * min x and y coordinates (0), x and y tile offsets (0)
     *
     * @param width image width
     * @param height image height
     * @param tileSampleModel a <code>SampleModel</code> specifying the dimensions
     * data type etc. for image tiles
     */
    public DiskMemImage(
            int width, int height,
            SampleModel tileSampleModel,
            ColorModel colorModel) {

        this(0, 0,           // minX, minY
             width, height,
             0, 0,           // tileGridXOffset, tileGridYOffset
             tileSampleModel,
             colorModel
             );
    }


    /**
     * Constructor. This will set default values for the image's
     * x and y tile offsets (0) and <code>ColorModel</code>
     * (<code>null</code>)
     *
     * @param minX x coordinate of the upper-left image pixel
     * @param minY y coordinate of the upper-left image pixel
     * @param width image width
     * @param height image height
     * @param tileSampleModel a <code>SampleModel</code> specifying the dimensions
     * data type etc. for image tiles
     */
    public DiskMemImage(
            int minX, int minY,
            int width, int height,
            SampleModel tileSampleModel) {

        this(minX, minY,
             width, height,
             0, 0,           // tileGridXOffset, tileGridYOffset
             tileSampleModel,
             null            // ColorModel
             );
    }


    /**
     * Constructor. Sets the image's <code>ColorModel</code> to <code>null</code>
     * 
     * @param minX x coordinate of the upper-left image pixel
     * @param minY y coordinate of the upper-left image pixel
     * @param width image width
     * @param height image height
     * data type etc. for image tiles
     * @param tileGridXOffset x coordinate of the upper-left pixel of the upper-left tile
     * @param tileGridYOffset y coordinate of the upper-left pixel of the upper-left tile
     * @param tileSampleModel a <code>SampleModel</code> specifying the dimensions
     */
    public DiskMemImage(
            int minX, int minY,
            int width, int height,
            int tileGridXOffset, int tileGridYOffset,
            SampleModel tileSampleModel) {

        this(minX, minY,
             width, height,
             tileGridXOffset, tileGridYOffset,
             tileSampleModel,
             null            // ColorModel
             );
    }

    /**
     * Fully specified constructor
     *
     * @param minX x coordinate of the upper-left image pixel
     * @param minY y coordinate of the upper-left image pixel
     * @param width image width
     * @param height image height
     * data type etc. for image tiles
     * @param tileGridXOffset x coordinate of the upper-left pixel of the upper-left tile
     * @param tileGridYOffset y coordinate of the upper-left pixel of the upper-left tile
     * @param tileSampleModel a <code>SampleModel</code> specifying the dimensions
     * @param colorModel a <code>ColorModel</code> to use with the image (may be null)
     */
    public DiskMemImage(
            int minX, int minY,
            int width, int height,
            int tileGridXOffset, int tileGridYOffset,
            SampleModel tileSampleModel, ColorModel colorModel) {
        
        super(new ImageLayout(
                minX, minY,
                width, height,
                tileGridXOffset, tileGridYOffset,
                tileSampleModel.getWidth(), tileSampleModel.getHeight(),
                tileSampleModel, colorModel),

                null, null);  // sources, properties

        tileGrid = new Rectangle(
                getMinTileX(),
                getMinTileY(),
                getMaxTileX() - getMinTileX() + 1,
                getMaxTileY() - getMinTileY() + 1);

        tileInUse = new boolean[tileGrid.width][tileGrid.height];
        numTilesInUse = 0;

        tileCache = new DiskMemTileCache();

        DataBuffer db = tileSampleModel.createDataBuffer();
        tileMemorySize = DataBuffer.getDataTypeSize(db.getDataType()) / 8L *
                db.getSize() * db.getNumBanks();

        tileObservers = CollectionFactory.newSet();
    }

    /**
     * Retrieve a tile for reading. Any changes to the tile's data
     * will not be preserved by the cache.
     *
     * @param tileX the tile's column in the tile grid
     * @param tileY the tile's row in the tile grid
     * @return the tile data for reading
     */
    @Override
    public Raster getTile(int tileX, int tileY) {
        Raster r = null;
        if (tileGrid.contains(tileX, tileY)) {
            r = tileCache.getTile(this, tileX, tileY);
            if (r == null) {
                r = createTile(tileX, tileY);
                tileCache.add(this, tileX, tileY, r);
            }
        }

        return r;
    }

    public void addTileObserver(TileObserver to) {
        tileObservers.add(to);
    }

    public void removeTileObserver(TileObserver to) {
        tileObservers.remove(to);
    }

    /**
     * Check-out a tile for writing. The tile will be unavailable to other
     * callers through this method until it is released via
     * {@linkplain #releaseWritableTile(int, int)}. If this method is
     * called for the same time prior to the tile being released
     * it returns <code>null</code> and a warning message is logged.
     *
     * @param tileX the tile's column in the tile grid
     * @param tileY the tile's row in the tile grid
     * @return the tile data for writing, or <code>null</code> if the tile
     * is already checked-out
     */
    public WritableRaster getWritableTile(int tileX, int tileY) {
        WritableRaster r = null;
        if (tileGrid.contains(tileX, tileY)) {
            if (tileInUse[tileX - tileGrid.x][tileY - tileGrid.y]) {
                // TODO: throw an exception here ?
                Logger.getLogger(DiskMemImage.class.getName()).log(Level.WARNING,
                        String.format("Attempting to get tile %d,%d for writing while it is already checked-out",
                        tileX, tileY));
                return null;
            }

            r = (WritableRaster) tileCache.getTile(this, tileX, tileY);
            if (r == null) {
                r = createTile(tileX, tileY);
                tileCache.add(this, tileX, tileY, r);
            }

            tileInUse[tileX - tileGrid.x][tileY - tileGrid.y] = true;
            numTilesInUse++ ;

            for (TileObserver obs : tileObservers) {
                obs.tileUpdate(this, tileX, tileY, true);
            }
        }
        return r;
    }

    /**
     * Release a tile after writing to it. The cache's disk copy of
     * the tile's data will be refreshed.
     * <p>
     * If the cache no longer has the tile in its memory storage, e.g.
     * because of memory swapping for other tile accesses, the cache
     * will be unable to refresh the tile's data on disk. In this case
     * a warning message is logged.
     * <p>
     * If the tile was not previously checked-out via
     * {@linkplain #getWritableTile(int, int)} a warning message is
     * logged.
     *
     * @param tileX the tile's column in the tile grid
     * @param tileY the tile's row in the tile grid
     */
    public void releaseWritableTile(int tileX, int tileY) {
        if (tileGrid.contains(tileX, tileY)) {
            if (tileInUse[tileX - tileGrid.x][tileY - tileGrid.y]) {
                tileInUse[tileX - tileGrid.x][tileY - tileGrid.y] = false;
                numTilesInUse--;

                /*
                 * TODO: Consider skipping this step. It is mostly here as a
                 * precaution against the cached tile being garbage collected
                 * if the system runs very low on memory.
                 */
                try {
                    tileCache.setTileChanged(this, tileX, tileY);

                } catch (Exception ex) {
                    Logger.getLogger(DiskMemImage.class.getName()).
                            log(Level.SEVERE, null, ex);
                }

                for (TileObserver obs : tileObservers) {
                    obs.tileUpdate(this, tileX, tileY, false);
                }

            } else {
                Logger.getLogger(DiskMemImage.class.getName()).
                        log(Level.WARNING, "Attempting to release a tile that was not checked-out");
            }
        }
    }

    /**
     * Query if a tile is currently checked-out for writing (via
     * a call to {@linkplain #getWritableTile(int, int)}.
     *
     * @param tileX the tile's column in the tile grid
     * @param tileY the tile's row in the tile grid
     * @return true if the tile is currently checked-out for
     * writing; false otherwise.
     */
    public boolean isTileWritable(int tileX, int tileY) {
        return tileInUse[tileX - tileGrid.x][tileY - tileGrid.y];
    }

    /**
     * Returns the indices (tile grid col,row) as <code>Point</code>s of
     * those tiles that are currently checked out for writing.
     *
     * @return array of tile indices or null if no tiles are checked-out
     */
    public Point[] getWritableTileIndices() {
        Point[] indices = null;

        if (numTilesInUse > 0) {
            indices = new Point[numTilesInUse];
            int k = 0;
            for (int y = tileGrid.y, ny = 0; ny < tileGrid.height; y++, ny++) {
                for (int x = tileGrid.x, nx = 0; nx < tileGrid.width; x++, nx++) {
                    if (tileInUse[nx][ny]) {
                        indices[k++] = new Point(x, y);
                    }
                }
            }
        }

        return indices;
    }

    /**
     * Query if any tiles are currently checked out for writing
     * @return true if any tiles are currently checked out for writing; false otherwise
     */
    public boolean hasTileWriters() {
        return numTilesInUse > 0;
    }


    public void setData(Raster data) {
        Rectangle rBounds = data.getBounds();
        Rectangle common = rBounds.intersection(getBounds());
        if (common.isEmpty()) {
            return;
        }

        int minTileX = XToTileX(common.x);
        int maxTileX = XToTileX(common.x + common.width - 1);
        int minTileY = YToTileY(common.y);
        int maxTileY = YToTileY(common.y + common.height - 1);

        for (int y = minTileY; y <= maxTileY; y++) {
            for (int x = minTileX; x <= maxTileX; x++) {
                WritableRaster tile = getWritableTile(x, y);
                Rectangle tileOverlap = tile.getBounds().intersection(common);

                Raster dataChild = data.createChild(
                        tileOverlap.x, tileOverlap.y,
                        tileOverlap.width, tileOverlap.height,
                        tileOverlap.x, tileOverlap.y,
                        null);

                WritableRaster tChild = tile.createWritableChild(
                        tileOverlap.x, tileOverlap.y,
                        tileOverlap.width, tileOverlap.height,
                        tileOverlap.x, tileOverlap.y,
                        null);

                tChild.setRect(dataChild);
            }
        }
    }

    /**
     * Create a Graphics2D object for drawing operations with this image.
     * The graphics object will be an instance of <code>DiskMemImageGraphics</code>.
     * <p>
     * Note that only images of integral data type support graphics operations.
     *
     * @return a new Graphics2D object
     * @throws UnsupportedOperationException if the image is not of integral data type
     * @see DiskMemImageGraphics
     */
    public Graphics2D createGraphics() {
        int dataType = getSampleModel().getDataType();
        if (dataType == DataBuffer.TYPE_BYTE ||
            dataType == DataBuffer.TYPE_INT ||
            dataType == DataBuffer.TYPE_SHORT ||
            dataType == DataBuffer.TYPE_USHORT)
        {
            return new DiskMemImageGraphics(this);

        } else {
            throw new UnsupportedOperationException("Image must have an integral data type");
        }
    }

    /**
     * Returns the maximum amount of memory, in bytes, that this
     * image will use for in-memory storage of its data. Since
     * this class uses a dedicated <code>DiskMemTileCache</code>
     * object, all image data are cached on disk during the lifetime
     * of the image, with tiles being loaded into memory as required.
     */
    public long getMemoryCapacity() {
        return tileCache.getMemoryCapacity();
    }

    /**
     * Set the maximum amount of memory, in bytes, that this image
     * will use for in-memory storage of its tiles. All tiles are
     * also cached on disk during the lifetime of the image.
     *
     * @param memCapacity maximum memory capacity for image tiles (in bytes)
     */
    public void setMemoryCapacity(long memCapacity) {
        tileCache.setMemoryCapacity(memCapacity);
    }

    /**
     * Returns the amount of memory (bytes) required to store a single image
     * tile's data
     *
     * @return tile memory size in bytes
     */
    public long getTileMemorySize() {
        return tileMemorySize;
    }

    /**
     * Retrieve a reference to the <code>DiskMemTileCache</code> instance
     * that is being used by this image. This method is intended for client
     * code that wishes to query cache state or receive cache diagnostic
     * messages (via the <code>Observer</code> interface). It is probably <b>not</b>
     * a good idea to manipulate the cache state directly !
     *
     * @return a live reference to the cache being used exclusively by this image
     * @see DiskMemTileCache
     */
    public DiskMemTileCache getTileCache() {
        return tileCache;
    }

    private WritableRaster createTile(int tileX, int tileY) {
        assert(tileCache.getTile(this, tileX, tileY) == null);

        Point location = new Point(tileXToX(tileX), tileYToY(tileY));
        return createWritableRaster(getSampleModel(), location);
    }

}
