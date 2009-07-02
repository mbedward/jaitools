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
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import javax.media.jai.TileCache;

/**
 * @author Michael Bedward
 */
public class DiskBasedTileCache implements TileCache {

    public static final long DEFAULT_MEMORY_CAPACITY = 64L * 1024L * 1024L;
    private long memCapacity;

    // map of all tiles whether currently in memory or cached on disk
    private Map<Integer, DiskBasedTile> tiles;
    
    // set of those tiles currently resident in memory
    private Map<Integer, SoftReference<Raster>> residentTiles;

    // current (approximate) memory used for resident tiles
    private long curSize;
    

    public DiskBasedTileCache() {
        tiles = new HashMap<Integer, DiskBasedTile>();
        residentTiles = new HashMap<Integer, SoftReference<Raster>>();
        
        memCapacity = DEFAULT_MEMORY_CAPACITY;
        curSize = 0L;
    }

    public void add(RenderedImage owner, int tileX, int tileY, Raster data) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void add(RenderedImage owner,
                int tileX,
                int tileY,
                Raster data,
                Object tileCacheMetric) {

        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void remove(RenderedImage owner, int tileX, int tileY) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Raster getTile(RenderedImage owner, int tileX, int tileY) {
        Raster r = null;

        // is the tile resident ?
        int id = computeTileId(owner, tileX, tileY);
        if (residentTiles.containsKey(id)) {
            r = residentTiles.get(id).get();
            if (r == null) {
                // tile has been garbage collected
                residentTiles.remove(id);

            } else {
                return r;
            }
        }

        // tile needs to be loaded from disk and added
        // to the memory store
        DiskBasedTile tile = tiles.get(id);
        if (tile != null) {
            r = tile.getTile();
            makeResident(id, r);
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Raster[] getTiles(RenderedImage owner, Point[] tileIndices) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void flush() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void memoryControl() {
        throw new UnsupportedOperationException("Not supported yet.");
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

    public void setMemoryCapacity(long memoryCapacity) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public long getMemoryCapacity() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setMemoryThreshold(float memoryThreshold) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public float getMemoryThreshold() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setTileComparator(Comparator comparator) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Comparator getTileComparator() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Add a raster to those resident in memory
     */
    private void makeResident(int tileId, Raster r) {
        SampleModel sm = r.getSampleModel();
        long typeLen = DataBuffer.getDataTypeSize(sm.getTransferType());
        long size = (long)sm.getNumDataElements() * typeLen;

        if (size > memCapacity) {
            // @todo something better than this...
            throw new RuntimeException("tile size greater than memory capacity");
        }

        while (curSize + size > memCapacity) {
            removeResidentTile();
        }
    }

    /**
     * Remove the tile with the longest time since last access from memory
     */
    private void removeResidentTile() {

    }

    /**
     * Compute an integer ID for a tile based on its image coordinates
     * and the hash code of the parent image
     * 
     * @param owner the parent image
     * @param tileX the tile's x location
     * @param tileY the tile's y location
     * 
     * @return integer ID
     */
    private int computeTileId(RenderedImage owner, int tileX, int tileY) {
        return 31 * owner.hashCode() + tileX + tileY * owner.getNumXTiles();
    }
}
