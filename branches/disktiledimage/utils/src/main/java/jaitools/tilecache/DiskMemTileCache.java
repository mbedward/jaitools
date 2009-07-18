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
import java.io.File;
import java.io.IOException;
import java.lang.ref.SoftReference;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.media.jai.CachedTile;
import javax.media.jai.PlanarImage;
import javax.media.jai.TileCache;

/**
 * This class implements JAI {@linkplain javax.media.jai.TileCache}. It stores
 * cached tiles on disk to allow applications to work with very large volumes of
 * tiled image data without being limited by available memory.
 * <p>
 * The cache can also provide memory storage for a subset of tiles to avoid
 * excessive disk activity when the same tiles are being repeatedly accessed.
 *
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 * @since 1.0
 * @version $Id$
 * 
 * @see DiskCachedTile
 * @see TileAccessTimeComparator
 */
public class DiskMemTileCache extends Observable implements TileCache {

    public static final long DEFAULT_MEMORY_CAPACITY = 64L * 1024L * 1024L;

    public static final float DEFAULT_MEMORY_THRESHOLD = 0.75F;

    /**
     * Rules used with the {@linkplain #makeResident} method
     */
    private enum ResidencyRule {
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
    public static final String KEY_INITIAL_MEMORY_CAPACITY = "memcapacity";

    /**
     * Key for the parameter controlling whether newly added tiles
     * automatically become resident in memory. The value must be
     * one of
     * {@linkplain #VALUE_NEW_TILES_RESIDENT_ALWAYS},
     * {@linkplain #VALUE_NEW_TILES_RESIDENT_TRY} or
     * {@linkplain #VALUE_NEW_TILES_RESIDENT_NEVER}
     */
    public static final String KEY_NEW_TILES_RESIDENT = "newtilesres";

    /**
     * Value option for {@linkplain #KEY_NEW_TILES_RESIDENT} indicating
     * that newly cached tiles should always become resident in memory
     * even if currently-resident tiles must be swapped out to make
     * space available.
     */
    public static final int VALUE_NEW_TILES_RESIDENT_ALWAYS = 1;

    /**
     * Value option for {@linkplain #KEY_NEW_TILES_RESIDENT} indicating
     * that newly cached tiles should become resident in memory only
     * if there is currently enough space available.
     * <b>This is the default behaviour</b>.
     */
    public static final int VALUE_NEW_TILES_RESIDENT_TRY    = 2;

    /**
     * Value option for {@linkplain #KEY_NEW_TILES_RESIDENT} indicating
     * that newly cached tiles do not automatically become resident in memory.
     */
    public static final int VALUE_NEW_TILES_RESIDENT_NEVER  = 3;


    private static Map<String, ParamDesc> paramDescriptors;
    static {
        ParamDesc desc;
        paramDescriptors = new HashMap<String, ParamDesc>();

        desc = new ParamDesc(KEY_INITIAL_MEMORY_CAPACITY, Number.class, DEFAULT_MEMORY_CAPACITY);
        paramDescriptors.put( desc.key, desc );

        desc = new ParamDesc(KEY_NEW_TILES_RESIDENT, Integer.class, VALUE_NEW_TILES_RESIDENT_TRY);
        paramDescriptors.put( desc.key, desc );
    }

    // maximum memory available for resident tiles
    private long memCapacity;

    // current memory used for resident tiles
    private long curMemory;

    /*
     * A value between 0.0 and 1.0 that may be used for memory control
     * if the param KEY_USE_MEMORY_THRESHOLD is TRUE.
     */
    private float memThreshold;

    /*
     * A flag value for whether newly cached tiles are placed into memory
     * always, when space is available, or never
     */
    private int newTilesResident;

    // map of all tiles whether currently in memory or cached on disk
    private Map<Object, DiskCachedTile> tiles;
    
    // set of those tiles currently resident in memory
    private Map<Object, SoftReference<Raster>> residentTiles;

    // A tile comparator used to determine priority of tiles in limited
    // memory storage
    private Comparator<CachedTile> comparator;

    /* List of tile references that is sorted into tile priority order when
     * required for memory swapping.
     *
     * Note: we use this in preference to a SortedSet or similar because of
     * the complications of using the remove(obj) method with a collection
     * that is using a comparator rather than equals().
     */
    private List<DiskCachedTile> sortedResidentTiles;

    // whether to send cache diagnostics to observers
    private boolean diagnosticsEnabled;


    /**
     * Constructor. Creates an instance of the cache with all parameters set
     * to their default values. Equivalent to <code>DiskMemTileCache(null)</code>.
     */
    public DiskMemTileCache() {
        this(null);
    }

    /**
     * Constructor.
     *
     * @param params an optional map of parameters (may be empty or null)
     */
    public DiskMemTileCache(Map<String, Object> params) {
        if (params == null) {
            params = Collections.emptyMap();
        }

        diagnosticsEnabled = false;
        tiles = new HashMap<Object, DiskCachedTile>();
        residentTiles = new HashMap<Object, SoftReference<Raster>>();
        curMemory = 0L;
        memThreshold = DEFAULT_MEMORY_THRESHOLD;

        Object o;
        ParamDesc desc;

        desc= paramDescriptors.get(KEY_INITIAL_MEMORY_CAPACITY);
        memCapacity = (Long)desc.defaultValue;
        o = params.get(desc.key);
        if (o != null) {
            if (desc.typeOK(o)) {
                memCapacity = ((Number)o).longValue();
            }
        }

        desc = paramDescriptors.get(KEY_NEW_TILES_RESIDENT);
        newTilesResident = (Integer)desc.defaultValue;
        o = params.get(desc.key);
        if (o != null) {
            if (desc.typeOK(o)) {
                int ival = (Integer)o;
                switch (ival) {
                    case VALUE_NEW_TILES_RESIDENT_ALWAYS:
                    case VALUE_NEW_TILES_RESIDENT_NEVER:
                    case VALUE_NEW_TILES_RESIDENT_TRY:
                        newTilesResident = ival;
                        break;

                    default:
                        throw new IllegalArgumentException(
                                "Unrecognized option for cache parameter: KEY_NEW_TILES_RESIDENT");
                }
            }
        }

        comparator = new TileAccessTimeComparator();
        sortedResidentTiles = new ArrayList<DiskCachedTile>();
    }

    public void add(RenderedImage owner, int tileX, int tileY, Raster data) {
        add(owner, tileX, tileY, data, null);
    }

    public synchronized void add(RenderedImage owner,
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

            boolean resident = false;
            switch (newTilesResident) {
                case VALUE_NEW_TILES_RESIDENT_ALWAYS:
                    resident = makeResident(tile, data, ResidencyRule.FORCE);
                    break;
                    
                case VALUE_NEW_TILES_RESIDENT_TRY:
                    resident = makeResident(tile, data, ResidencyRule.NO_SWAP);
                    break;
                    
                case VALUE_NEW_TILES_RESIDENT_NEVER:
                    // do nothing
                    break;
            }

            if (resident) {
                tile.setAction(DiskCachedTile.TileAction.ACTION_ADDED_RESIDENT);
            } else {
                tile.setAction(DiskCachedTile.TileAction.ACTION_ADDED);
            }

            tile.setTileTimeStamp(System.currentTimeMillis());

            if (diagnosticsEnabled) {
                setChanged();
                notifyObservers(tile);
            }

        } catch (IOException ex) {
            Logger.getLogger(DiskMemTileCache.class.getName())
                    .log(Level.SEVERE, "Unable to cache this tile on disk", ex);
        }
    }

    /**
     * Remove the specifed tile from the cache
     * @param owner the image that this tile belongs to
     * @param tileX the tile column
     * @param tileY the tile row
     */
    public synchronized void remove(RenderedImage owner, int tileX, int tileY) {
        Object key = getTileId(owner, tileX, tileY);
        DiskCachedTile tile = tiles.get(key);

        if (tile != null) {
            if (residentTiles.containsKey(key)) {
                removeResidentTile(key, false);
            }
        }

        tiles.remove(key);
        File f = tile.getFile();
        if (f != null && f.exists()) {
            f.delete();
        }

        tile.setAction(DiskCachedTile.TileAction.ACTION_REMOVED);
        if (diagnosticsEnabled) {
            setChanged();
            notifyObservers(tile);
        }
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
    public synchronized Raster getTile(RenderedImage owner, int tileX, int tileY) {
        Raster r = null;

        Object key = getTileId(owner, tileX, tileY);

        DiskCachedTile tile = tiles.get(key);
        if (tile != null) {

            // is the tile resident ?
            if (residentTiles.containsKey(key)) {
                r = residentTiles.get(key).get();
                if (r == null) {
                    // tile data has been garbage collected
                    // TODO: issue a warning ?

                    residentTiles.remove(key);
                    sortedResidentTiles.remove(tile);
                    curMemory -= tile.getTileSize();

                    tile.setAction(DiskCachedTile.TileAction.ACTION_GARBAGE_COLLECTED);
                    if (diagnosticsEnabled) {
                        setChanged();
                        notifyObservers(tile);
                    }
                }
            }

            if (r == null) {
                // tile needs to be read from disk
                r = tile.readData();
                if (makeResident(tile, r, ResidencyRule.FORCE)) {
                    tile.setAction(DiskCachedTile.TileAction.ACTION_RESIDENT);
                    if (diagnosticsEnabled) {
                        setChanged();
                        notifyObservers(tile);
                    }
                }
            }

            tile.setAction(DiskCachedTile.TileAction.ACTION_ACCESSED);
            tile.setTileTimeStamp(System.currentTimeMillis());

            if (diagnosticsEnabled) {
                setChanged();
                notifyObservers(tile);
            }
        }

        return r;
    }

    /**
     * Get all cached tiles associated with the specified image.
     * The tiles will be loaded into memory as space allows.
     * 
     * @param owner the image for which tiles are requested
     * @return an array of tile Rasters
     */
    public synchronized Raster[] getTiles(RenderedImage owner) {
        int minX = owner.getMinTileX();
        int minY = owner.getMinTileY();
        int numX = owner.getNumXTiles();
        int numY = owner.getNumYTiles();
        
        List<Object> keys = new ArrayList<Object>();
        for (int y = minY, ny=0; ny < numY; y++, ny++) {
            for (int x = minX, nx = 0; nx < numX; x++, nx++) {
                Object key = getTileId(owner, x, y);
                if (tiles.containsKey(key)) keys.add(key);
            }
        }

        Raster[] rasters = new Raster[keys.size()];
        int k = 0;
        for (Object key : keys) {
            DiskCachedTile tile = tiles.get(key);
            Raster r = null;
            if (residentTiles.containsKey(tile)) {
                r = residentTiles.get(tile).get();
            }

            if (r == null) {
                r = tile.readData();
                makeResident(tile, r, ResidencyRule.FORCE);
            }

            rasters[k++] = r;

            tile.setTileTimeStamp(System.currentTimeMillis());
            tile.setAction(DiskCachedTile.TileAction.ACTION_ACCESSED);
            if (diagnosticsEnabled) {
                setChanged();
                notifyObservers(tile);
            }
        }

        return rasters;
    }

    /**
     * Remove all tiles that belong to the specified image from the cache
     * @param owner the image owning the tiles to be removed
     */
    public void removeTiles(RenderedImage owner) {
        for (int y = owner.getMinTileY(), ny=0; ny < owner.getNumYTiles(); y++, ny++) {
            for (int x = owner.getMinTileX(), nx=0; nx < owner.getNumXTiles(); x++, nx++) {
                remove(owner, x, y);
            }
        }
    }

    /**
     * This method is not presently declared as synchronized because it simply calls
     * the <code>addTile</code> method repeatedly.
     *
     * @param owner the image that the tiles belong to
     * @param tileIndices an array of Points specifying the column-row coordinates
     * of each tile
     * @param tiles tile data in the form of Raster objects
     * @param tileCacheMetric optional metric (may be null)
     */
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

    /**
     * This method is not presently declared as synchronized because it simply calls
     * the <code>getTile</code> method repeatedly.
     *
     * @param owner the image that the tiles belong to
     * @param tileIndices an array of Points specifying the column-row coordinates
     * of each tile
     * @return data for the requested tiles as Raster objects
     */
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
     * Remove ALL tiles from the cache: all resident tiles will be
     * removed from memory and all files for disk-cached tiles will
     * be discarded.
     * <p>
     * The update action of each tile will be set to {@linkplain DiskCachedTile#ACTION_REMOVED}.
     */
    public synchronized void flush() {
        flushMemory();
        
        for (DiskCachedTile tile : tiles.values()) {
            tile.getFile().delete();
            tile.setAction(DiskCachedTile.TileAction.ACTION_REMOVED);
            if (diagnosticsEnabled) {
                setChanged();
                notifyObservers(tile);
            }
        }
        tiles.clear();
    }

    /**
     * Remove all resident tiles from memory. No rewriting of tile data
     * to disk is done.
     */
    public synchronized void flushMemory() {
        /*
         * @todo is this adequate to ensure garbage collection ?
         */
        for (SoftReference<Raster> wref : residentTiles.values()) {
            wref.clear();
        }

        residentTiles.clear();
        sortedResidentTiles.clear();
        curMemory = 0;
    }

    /**
     * Free memory for resident tiles so that the fraction of memory occupied is
     * no more than the current value of the mamory threshold. 
     *
     * @see DiskMemTileCache#setMemoryThreshold(float)
     */
    public synchronized void memoryControl() {
        long maxUsed = (long)(memThreshold * memCapacity);
        long toFree = curMemory - maxUsed;
        if (toFree > 0) {
            defaultMemoryControl( toFree );
        }
    }

    /**
     * Make the requested amount of memory cache available, removing
     * resident tiles as necessary
     *
     * @param memRequired memory requested (bytes)
     */
    private void defaultMemoryControl( long memRequired ) {
        if (memRequired > memCapacity) {
            // @todo something better than this...
            throw new RuntimeException("space required is greater than cache memory capacity");
        }

        /*
         * Remove one or more lowest priority tiles to free
         * space
         */
        Collections.sort(sortedResidentTiles, comparator);
        while (memCapacity - curMemory < memRequired) {
            Object key = sortedResidentTiles.get(sortedResidentTiles.size()-1).getTileId();
            removeResidentTile(key, true);
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

    /**
     * Reset the memory capacity of the cache. Setting capacity to 0 will
     * flush all resident tiles from memory. Setting a capcity less than the
     * current capacity may result in some memory-resident tiles being
     * removed from memory.
     *
     * @param newCapacity requested memory capacity for resident tiles
     */
    public synchronized void setMemoryCapacity(long newCapacity) {
        if (newCapacity < 0) {
            throw new IllegalArgumentException("memory capacity must be >= 0");
        }

        long oldCapacity = memCapacity;
        memCapacity = newCapacity;

        if (newCapacity == 0) {
            flushMemory();

        } else if (newCapacity < oldCapacity && curMemory > newCapacity) {
            /*
             * Note: we free memory here directly rather than using
             * memoryControl or defaultMemoryControl methods because
             * they will fail when memCapacity has been reduced
             */
            Collections.sort(sortedResidentTiles, comparator);
            while (curMemory > newCapacity) {
                Object key = sortedResidentTiles.get(sortedResidentTiles.size()-1).getTileId();
                removeResidentTile(key, true);
            }
        }
    }

    public long getMemoryCapacity() {
        return memCapacity;
    }

    /**
     * Sets the memoryThreshold value to a floating point number that ranges from
     * 0.0 to 1.0. When the cache memory is full, the memory usage will be reduced
     * to this fraction of the total cache memory capacity. For example, a value
     * of .75 will cause 25% of the memory to be cleared, while retaining 75%.
     *
     * @param memoryThreshold Retained fraction of memory
     * @throws IllegalArgumentException if the memoryThreshold is less than 0.0 or greater than 1.0
     */
    public void setMemoryThreshold(float newThreshold) {
        if (newThreshold < 0.0F) {
            memThreshold = 0.0F;
        } else if (newThreshold > 1.0F) {
            memThreshold = 1.0F;
        } else {
            memThreshold = newThreshold;
        }

        memoryControl();
    }

    /**
     * Returns the memory threshold, which is the fractional amount of cache memory
     * to retain during tile removal. This only applies if memory thresholding has
     * been enabled by passing the parameter {@linkplain #KEY_USE_MEMORY_THRESHOLD} to
     * the constructor with a value of <code>Boolean.TRUE</code>.
     *
     * @return the retained fraction of memory
     */
    public float getMemoryThreshold() {
        return memThreshold;
    }

    // TODO write me !
    public synchronized void setTileComparator(Comparator comp) {

        if ( comp == null ) {
            // switch to default comparator based on tile access time
            comparator = new TileAccessTimeComparator();
        } else {
            comparator = comp;
        }

        sortedResidentTiles = new ArrayList<DiskCachedTile>();
        for (Object key : residentTiles.keySet()) {
            sortedResidentTiles.addAll(tiles.values());
        }
        Collections.sort(sortedResidentTiles, comparator);
    }

    /**
     * Get the Comparator currently being used to set priority
     * for resident tiles
     * @return reference to the current Comparator
     */
    public Comparator getTileComparator() {
        return comparator;
    }

    /**
     * Get the total number of tiles currently in the cache
     * @return number of cached tiles
     */
    public int getNumTiles() {
        return tiles.size();
    }

    /**
     * Get the number of tiles currently residing in the
     * cache's memory storage
     * @return number of memory-resident tiles
     */
    public int getNumResidentTiles() {
        return residentTiles.size();
    }

    /**
     * Query whether a given tile is in this cache
     * 
     * @param owner the owning image
     * @param tileX tile column
     * @param tileY tile row
     * @return true if the cache contains the tile; false otherwise
     */
    public boolean containsTile(RenderedImage owner, int tileX, int tileY) {
        Object key = getTileId(owner, tileX, tileY);
        return tiles.containsKey(key);
    }

    /**
     * Query whether a given tile is in this cache's memory storage
     *
     * @param owner the owning image
     * @param tileX tile column
     * @param tileY tile row
     * @return true if the tile is in cache memory; false otherwise
     */
    public boolean containsResidentTile(RenderedImage owner, int tileX, int tileY) {
        Object key = getTileId(owner, tileX, tileY);
        return residentTiles.containsKey(key);
    }

    /**
     * Inform the cache that the tile's data have changed. The tile should
     * be resident in memory as the result of a previous <code>getTile</code>
     * request. If this is the case, the cache's disk copy of the tile's data
     * will be refreshed. If the tile is not resident in memory, for instance
     * because of memory swapping for other tile accesses, the disk copy
     * will not be refreshed and a <code>TileNotResidentException</code> is
     * thrown.
     *
     * @param owner the owning image
     * @param tileX tile column
     * @param tileY tile row
     * @throws TileNotResidentException if the tile is not resident
     */
    public void setTileChanged(RenderedImage owner, int tileX, int tileY)
            throws TileNotResidentException {

        Object tileId = getTileId(owner, tileX, tileY);
        SoftReference<Raster> ref = residentTiles.get(tileId);
        if (ref == null || ref.get() == null) {
            throw new TileNotResidentException(owner, tileX, tileY);
        }

        DiskCachedTile tile = tiles.get(tileId);
        tile.writeData(ref.get());
    }

    /**
     * Enable or disable the publishing of cache messages
     * to Observers
     *
     * @param state true to publish diagnostic messages; false to suppress them
     */
    public void setDiagnostics(boolean state) {
        diagnosticsEnabled = state;
    }

    /**
     * Accept a <code>DiskMemCacheVisitor</code> object and call its
     * <code>visit</code> method for each tile presently in the
     * cache.
     */
    public synchronized void accept(DiskMemTileCacheVisitor visitor) {
        for (Object key : tiles.keySet()) {
            visitor.visit(tiles.get(key), residentTiles.containsKey(key));
        }
    }

    /**
     * Add a raster to those resident in memory
     */
    private boolean makeResident(DiskCachedTile tile, Raster data, ResidencyRule rule) {
        if (tile.getTileSize() > memCapacity) {
            return false;
        }
        
        if (rule == ResidencyRule.NO_SWAP &&
                tile.getTileSize() > memCapacity - curMemory) {
            return false;
        }

        if (tile.getTileSize() > memCapacity - curMemory) {
            memoryControl();

            /*
             * It is possible that the threshold rule fails to
             * free enough memory for the tile
             */
            if (tile.getTileSize() > memCapacity - curMemory) {
                defaultMemoryControl(tile.getTileSize());
            }
        }
        
        residentTiles.put(tile.getTileId(), new SoftReference<Raster>(data));
        curMemory += tile.getTileSize();

        /*
         * We don't bother about sort order here. Instead, the list
         * will be sorted by tile priority when resident tiles are
         * being removed
         */
        sortedResidentTiles.add(tile);

        return true;
    }


    /**
     * Remove a tile from the cache's memory storage. This may be to free
     * space for other tiles, in which case <code>writeData</code> will be
     * set to true and, if the tile is writable, a request is made to write
     * its data to disk again. If the tile is being removed from the cache
     * entirely, this method will be called with <code>writeData</code> set
     * to false.
     *
     * @param tileId the tile's unique id
     * @param writeData if true, and the tile is writable, its data will be
     * written to disk again; otherwise no writing is done.
     */
    private void removeResidentTile(Object tileId, boolean writeData) {

        DiskCachedTile tile = tiles.get(tileId);
        SoftReference<Raster> ref = residentTiles.remove(tileId);
        sortedResidentTiles.remove(tile);
        curMemory -= tile.getTileSize();



        /**
         * If the tile is writable, ie. its data are represented
         * by a WritableRaster, we renew the file copy of the data
         * to preserve any changes
         *
         * TODO: consider how to detect when this is really necessary
         */
        if (writeData && tile.isWritable()) {
            Raster r = ref.get();
            if (r != null) {
                tile.writeData(r);
            }
        }

        tile.setAction(DiskCachedTile.TileAction.ACTION_NON_RESIDENT);
        if (diagnosticsEnabled) {
            setChanged();
            notifyObservers(tile);
        }
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
