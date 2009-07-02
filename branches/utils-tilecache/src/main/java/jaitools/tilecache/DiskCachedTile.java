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

import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import javax.media.jai.CachedTile;

/**
 * Represents a tile cached on disk that may also be resident in memory.
 * <p>
 * This class does not hold a reference to the associated raster. When an
 * instance is created, the raster is written to disk and may, at the discretion
 * of the controlling {@linkplain DiskBasedTileCache}, also remain resident in
 * memory.
 *
 * @see DiskBasedTileCache
 */
final class DiskCachedTile implements CachedTile {

    public static final String FILE_PREFIX = "tile";
    public static final String FILE_SUFFIX = ".tmp";

    private Object id;
    private WeakReference<RenderedImage> owner;
    private int tileX;
    private int tileY;
    private Object tileCacheMetric;
    private long timeStamp;
    private long memorySize;
    private File file;

    private int action = 0;


    /**
     * Create a file to cache the given tile on disk. Presently
     * this method does nothing more than delegate to File.createTempFile
     *
     * @throws java.io.IOException
     */
    public static File createFile(DiskCachedTile tile) throws IOException {
        return File.createTempFile(FILE_PREFIX, FILE_SUFFIX);
    }


    DiskCachedTile(Object id,
                  RenderedImage owner,
                  int tileX,
                  int tileY,
                  Raster tile,
                  Object tileCacheMetric) throws IOException {

        if (owner == null || tile == null || file == null) {
            throw new IllegalArgumentException(
                    "All of owner, tile and file args must be non-null");
        }

        this.id = id;
        this.owner = new WeakReference(owner);
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileCacheMetric = tileCacheMetric;

        DataBuffer db = tile.getDataBuffer();
        memorySize = DataBuffer.getDataTypeSize(db.getDataType()) / 8L *
                     db.getSize() * db.getNumBanks();

        file = createFile(this);
        writeData();
    }

    /**
     * Returns a string representation of this cached tile
     */
    @Override
    public String toString() {
        RenderedImage o = getOwner();
        String ostring = o == null ? "null" : o.toString();

        Raster t = getTile();
        String tstring = t == null ? "null" : t.toString();

        return getClass().getName() + "@" + Integer.toHexString(hashCode()) +
               ": owner = " + ostring +
               " tileX = " + Integer.toString(tileX) +
               " tileY = " + Integer.toString(tileY) +
               " tile = " + tstring +
               " id = " + ((id instanceof Long)? Long.toHexString(((Long)id).longValue()) : id.toString()) +
               " memorySize = " + Long.toString(memorySize) +
               " timeStamp = " + Long.toString(timeStamp) +
               " file = " + file.getPath();
    }

    /**
     * <b>Do not use this method.</b>
     * <p>
     * It is implemented to satisfy the CAchedTile interface but calling it will
     * provoke an UnsupportedOperationException.
     * <p>
     * To get a tile, use {@linkplain DiskBasedTileCache#getTile(java.awt.image.RenderedImage, int, int) }
     */
    public Raster getTile() {
        throw new UnsupportedOperationException("Can't get a tile directly from a DiskCachedTile object");
    }

    /**
     * Get the image that owns this tile
     */
    public RenderedImage getOwner() {
        return owner.get();
    }

    /**
     * Get the last time of access for this tile
     */
    public long getTileTimeStamp() {
        return timeStamp;
    }


    public Object getTileCacheMetric() {
        return tileCacheMetric;
    }

    /**
     * Get the tile size in bytes. This is actually the
     * size of the raster associated with this cached tile instance.
     */
    public long getTileSize() {
        return memorySize;
    }

    /**
     * Not used at present - returns 0
     */
    public int getAction() {
        return action;
    }

    /**
     * Get the file used to cache this tile on disk.
     * @todo Perhaps we should be using a URL ?
     */
    public File getSource() {
        return file;
    }

    /**
     * Get the unique ID for this tile. The ID is a combination of
     * either the JAI-generated unique ID of the owning image or the image's
     * hash key if a unique ID was not available plus the tile index.
     * The returned object will be either Long or BigInteger.
     */
    public Object getTileId() {
        return id;
    }

    /**
     * Write data for the raster associated with this tile to
     * disk
     */
    private void writeData() {
        // TODO Write me !
    }
}
