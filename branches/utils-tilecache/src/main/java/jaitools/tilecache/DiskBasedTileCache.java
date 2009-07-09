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

package jaitools.tilecache;

import java.awt.Point;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;

/**
 * @author Michael Bedward
 */
public class DiskBasedTileCache extends Observable implements TileCache {

    public static final long DEFAULT_MEMORY_CAPACITY = 64L * 1024L * 1024L;

    public static final float DEFAULT_MEMORY_THRESHOLD = 0.75F;

    /**
     * Hints used with the {@linkplain #makeResident} method
     */
    private enum ResidencyHint {
        /**
         * Only make a tile resident if there is enough free memory
         * without swapping any currently resident tiles to disk
         */
        NO_SWAP,

        /**
         * Force tile to be come resident, swapping other tiles to
         * disk as necessary
         */
        FORCE
    };

    // @todo use JAI ParameterList or some other ready-made class for this ?
    private static class ParamDesc {
        String key;
        Class<?> clazz;
        Object defaultValue;

        ParamDesc(String key, Class<?> clazz, Object defaultValue) {
            this.key = key; this.clazz = clazz; this.defaultValue = defaultValue;
        }

        boolean typeOK (Object value) {
            if (Number.class.isAssignableFrom(clazz)) {
                return Number.class.isAssignableFrom(value.getClass());

            } else {
                return clazz.isAssignableFrom(value.getClass());
            }
        }
    }

    /**
     * Key for the parameter controlling initial memory capacity of the
     * tile cache. This determines the maximum number of tiles that can
     * be resident concurrently. The value must be numeric and will be
     * treated as Long.
     */
    public static final String INITIAL_MEMORY_CAPACITY = "memcapacity";

    /**
     * Key for the parameter controlling whether newly added tiles become
     * resident in memory. The value must be Boolean.
     */
    public static final String MAKE_NEW_TILES_RESIDENT = "newtilesres";

    /**
     * Key for the parameter controlling whether the memory threshold value
     * is used when creating space for new resident tiles. If set to true
     * memory thresholding is used when the resident tile space becomes full
     * (see {@linkplain #setMemoryThreshold(float)} for more details).
     * If set to FALSE (the default) new tiles are added to resident space by minimal
     * clearing of currently resident tiles.
     */
    public static final String USE_MEMORY_THRESHOLD = "memthreshold";


    private static Map<String, ParamDesc> paramDescriptors;
    static {
        ParamDesc desc;
        paramDescriptors = new HashMap<String, ParamDesc>();

        desc = new ParamDesc(INITIAL_MEMORY_CAPACITY, Number.class, DEFAULT_MEMORY_CAPACITY);
        paramDescriptors.put( desc.key, desc );

        desc = new ParamDesc(MAKE_NEW_TILES_RESIDENT, Boolean.class, Boolean.TRUE);
        paramDescriptors.put( desc.key, desc );

        desc = new ParamDesc(USE_MEMORY_THRESHOLD, Boolean.class, Boolean.TRUE);
        paramDescriptors.put( desc.key, desc );
    }

    // maximum memory available for resident tiles
    private long memCapacity;

    // current memory used for resident tiles
    private long curMemory;

    /*
     * A value between 0.0 and 1.0 that may be used for memory control
     * if the param USE_MEMORY_THRESHOLD is TRUE.
     */
    private float memThreshold;

    /**
     * Flags whether the memory threshold is used when freeing resident tile
     * memory. If false, a strategy of freeing minimum memory is used.
     */
    private boolean useThreshold;

    private boolean newTilesResident;

    // map of all tiles whether currently in memory or cached on disk
    private Map<Object, DiskCachedTile> tiles;
    
    // set of those tiles currently resident in memory
    private Map<Object, SoftReference<Raster>> residentTiles;


    /**
     * Constructor.
     *
     * @param params an optional map of parameters (may be empty or null)
     */
    public DiskBasedTileCache(Map<String, Object> params) {
        if (params == null) {
            params = Collections.emptyMap();
        }

        tiles = new HashMap<Object, DiskCachedTile>();
        residentTiles = new HashMap<Object, SoftReference<Raster>>();
        curMemory = 0L;

        Object o;
        ParamDesc desc;

        desc= paramDescriptors.get(INITIAL_MEMORY_CAPACITY);
        memCapacity = (Long)desc.defaultValue;
        o = params.get(desc.key);
        if (o != null) {
            if (desc.typeOK(o)) {
                memCapacity = ((Number)o).longValue();
            }
        }

        desc = paramDescriptors.get(USE_MEMORY_THRESHOLD);
        useThreshold = (Boolean)desc.defaultValue;
        o = params.get(desc.key);
        if (o != null) {
            if (desc.typeOK(o)) {
                useThreshold = (Boolean)o;
            }
        }

        desc = paramDescriptors.get(MAKE_NEW_TILES_RESIDENT);
        newTilesResident = (Boolean)desc.defaultValue;
        o = params.get(desc.key);
        if (o != null) {
            if (desc.typeOK(o)) {
                newTilesResident = (Boolean)o;
            }
        }
    }

    public void add(RenderedImage owner, int tileX, int tileY, Raster data) {
        add(owner, tileX, tileY, data, null);
    }

    public void add(RenderedImage owner,
                int tileX,
                int tileY,
                Raster data,
                Object tileCacheMetric) {

        Object key = getTileId(owner, tileX, tileY);
        if (tiles.containsKey(key)) {
            // tile is already cached
            return;
        }

        try {
            DiskCachedTile tile = new DiskCachedTile(key, owner, tileX, tileY, data, tileCacheMetric);
            tiles.put(key, tile);

            if (newTilesResident) {
                if (makeResident(tile, data, ResidencyHint.NO_SWAP)) {
                    tile.setAction(DiskCachedTile.ACTION_ADDED_RESIDENT);
                } else {
                    tile.setAction(DiskCachedTile.ACTION_ADDED);
                }
            } else {
                tile.setAction(DiskCachedTile.ACTION_ADDED);
            }

            setChanged();
            this.notifyObservers(tile);

        } catch (IOException ex) {
            Logger.getLogger(DiskBasedTileCache.class.getName())
                    .log(Level.SEVERE, "Unable to cache this tile on disk", ex);
        }
    }

    public void remove(RenderedImage owner, int tileX, int tileY) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Get the specified tile from the cache, if present. If the tile is
     * cached but not resident in memory it will be read from the cache's
     * disk storage and made resident.
     *
     * @param owner the image that the tile belongs to
     * @param tileX the tile column
     * @param tileY the tile row
     * @return the requested tile or null if the tile was not cached
     */
    public Raster getTile(RenderedImage owner, int tileX, int tileY) {
        Raster r = null;

        Object key = getTileId(owner, tileX, tileY);

        DiskCachedTile tile = tiles.get(key);
        if (tile != null) {

            // is the tile resident ?
            if (residentTiles.containsKey(key)) {
                r = residentTiles.get(key).get();
                if (r == null) {
                    // tile has been garbage collected
                    residentTiles.remove(key);
                }
            }

            if (r == null) {
                // tile needs to be read from disk
                r = tile.readData();
                makeResident(tile, r, ResidencyHint.FORCE);
            }
        }

        return r;
    }

    public Raster[] getTiles(RenderedImage owner) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeTiles(RenderedImage owner) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void addTiles(RenderedImage owner,
                     Point[] tileIndices,
                     Raster[] tiles,
                     Object tileCacheMetric) {

        if (tileIndices.length != tiles.length) {
            throw new IllegalArgumentException("tileIndices and tiles args must be the same length");
        }

        for (int i = 0; i < tiles.length; i++) {
            add(owner, tileIndices[i].x, tileIndices[i].y, tiles[i], tileCacheMetric);
        }
    }

    public Raster[] getTiles(RenderedImage owner, Point[] tileIndices) {
        Raster[] r = null;

        if (tileIndices.length > 0) {
            r = new Raster[tileIndices.length];
            for (int i = 0; i < tileIndices.length; i++) {
                r[i] = getTile(owner, tileIndices[i].x, tileIndices[i].y);
            }
        }

        return r;
    }

    /**
     * Remove all tiles from the cache: all resident tiles will be
     * removed from memory and all files for disk-cached tiles will
     * be discarded.
     * <p>
     * The update action of each tile will be set to {@linkplain DiskCachedTile#ACTION_REMOVED}.
     */
    public void flush() {
        /*
         * @todo is this adequate to ensure garbage collection ?
         */
        for (SoftReference<Raster> wref : residentTiles.values()) {
            wref.clear();
        }
        residentTiles.clear();

        for (DiskCachedTile tile : tiles.values()) {
            tile.getSource().delete();
            setChanged();
            tile.setAction(DiskCachedTile.ACTION_REMOVED);
            notifyObservers(tile);
        }
        tiles.clear();
    }

    /**
     * Free memory for resident tiles so that the fraction of memory occupied is
     * no more than the current value of the mamory threshold. Does nothing if
     * memory thresholding has not been enabled.
     *
     * @see DiskBasedTileCache#USE_MEMORY_THRESHOLD
     * @see DiskBasedTileCache#setMemoryThreshold(float)
     * @see DiskBasedTileCache#getMemoryThreshold() 
     */
    public void memoryControl() {
        if (useThreshold) {
            long toFree = (long) (curMemory - memThreshold * memCapacity);
            if (toFree > 0) {
                freeMemory( toFree );
            }
        }
    }

    /**
     * Implemented as an empty method (it was deprecated as of JAI 1.1)
     * @param arg0
     * @deprecated
     */
    @Deprecated
    public void setTileCapacity(int arg0) {
    }

    /**
     * Implemented as a dummy method that always returns 0
     * (it was deprecated as of JAI 1.1)
     * @deprecated
     */
    @Deprecated
    public int getTileCapacity() {
        return 0;
    }

    public void setMemoryCapacity(long newCapacity) {
        if (newCapacity < memCapacity) {
            freeMemory(memCapacity - newCapacity);
        }

        memCapacity = newCapacity;
    }

    public long getMemoryCapacity() {
        return memCapacity;
    }

    /**
     * Sets the memoryThreshold value to a floating point number that ranges from
     * 0.0 to 1.0. When the cache memory is full, the memory usage will be reduced
     * to this fraction of the total cache memory capacity. For example, a value
     * of .75 will cause 25% of the memory to be cleared, while retaining 75%.
     * <p>
     * Memory thresholding is only used if the parameter {@linkplain #USE_MEMORY_THRESHOLD}
     * was passed to the cache constructor with a value of <code>Boolean.TRUE</code>.
     * Otherwise, the default strategy is to free the minimum amount of memory for
     * resident tiles sufficient to add the next tile.
     *
     * @param memoryThreshold Retained fraction of memory
     * @throws IllegalArgumentException if the memoryThreshold is less than 0.0 or greater than 1.0
     */
    public void setMemoryThreshold(float memoryThreshold) {
        memThreshold = memoryThreshold;
    }

    /**
     * Returns the memory threshold, which is the fractional amount of cache memory
     * to retain during tile removal. This only applies if memory thresholding has
     * been enabled by passing the parameter {@linkplain #USE_MEMORY_THRESHOLD} to
     * the constructor with a value of <code>Boolean.TRUE</code>.
     *
     * @return the retained fraction of memory
     */
    public float getMemoryThreshold() {
        return memThreshold;
    }

    public void setTileComparator(Comparator comparator) {
    }

    public Comparator getTileComparator() {
        return null;
    }

    /**
     * Add a raster to those resident in memory
     */
    private boolean makeResident(DiskCachedTile tile, Raster data, ResidencyHint hint) {
        if (hint == ResidencyHint.NO_SWAP &&
                tile.getTileSize() > memCapacity - curMemory) {
            return false;
        }

        if (useThreshold) {
            memoryControl();
        } else {
            freeMemory( tile.getTileSize() );
        }
        
        residentTiles.put(tile.getTileId(), new SoftReference<Raster>(data));
        setChanged();
        tile.setAction(DiskCachedTile.ACTION_RESIDENT);
        notifyObservers(tile);
        return true;
    }

    /**
     * Make the requested amount of memory cache available, removing
     * resident tiles as necessary
     * 
     * @param memRequired memory requested (bytes)
     */
    private void freeMemory( long memRequired ) {
        if (memRequired > memCapacity) {
            // @todo something better than this...
            throw new RuntimeException("space required is greater than cache memory capacity");
        }

        while (memCapacity - curMemory < memRequired) {
            removeNextTile();
        }
    }

    /**
     * Remove the tile with the lowest residency priority
     */
    private void removeNextTile() {
        // TODO: WRITE ME
    }

    /**
     * Generate a unique ID for this tile. This uses the same technique as the
     * Sun memory cache implementation: putting the id of the owning image
     * into the upper bytes of a long or BigInteger value and the tile index into
     * the lower bytes.
     * @param owner the owning image
     * @param tileX tile column
     * @param tileY tile row
     * @return the ID as an Object which will be an instance of either Long or BigInteger
     */
    private Object getTileId(RenderedImage owner,
                              int tileX,
                              int tileY) {

        long tileId = tileY * (long)owner.getNumXTiles() + tileX;

        BigInteger imageId = null;

        if (owner instanceof PlanarImage) {
            imageId = (BigInteger)((PlanarImage)owner).getImageID();
        }

        if (imageId != null) {
            byte[] buf = imageId.toByteArray();
            int length = buf.length;
            byte[] buf1 = new byte[buf.length + 8];
            System.arraycopy(buf, 0, buf1, 0, length);
            for (int i = 7, j = 0; i >= 0; i--, j += 8)
                buf1[length++] = (byte)(tileId >> j);
            return new BigInteger(buf1);

        } else {
            tileId &= 0x00000000ffffffffL;
            return new Long(((long)owner.hashCode() << 32) | tileId);
        }
    }

}
