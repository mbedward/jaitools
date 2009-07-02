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
import java.lang.ref.WeakReference;
import java.math.BigInteger;
import javax.media.jai.CachedTile;
import javax.media.jai.PlanarImage;

/**
 * Represents a tile cached on disk that may also be resident in memory
 *
 * @see DiskBasedTileCache
 */
final class DiskCachedTile implements CachedTile {

    private File file;
    private WeakReference<RenderedImage> owner;
    private int tileX;
    private int tileY;
    private Object tileCacheMetric;
    private long timeStamp;

    private Object key;
    private long memorySize;

    private int action = 0;


    DiskCachedTile(RenderedImage owner,
                  int tileX,
                  int tileY,
                  Raster tile,
                  Object tileCacheMetric,
                  File file) {

        if (owner == null || tile == null || file == null) {
            throw new IllegalArgumentException(
                    "All of owner, tile and file args must be non-null");
        }

        this.owner = new WeakReference(owner);
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileCacheMetric = tileCacheMetric;
        this.file = file;

        key = generateTileKey(owner, tileX, tileY);

        DataBuffer db = tile.getDataBuffer();
        memorySize = DataBuffer.getDataTypeSize(db.getDataType()) / 8L *
                     db.getSize() * db.getNumBanks();
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
               " key = " + ((key instanceof Long)? Long.toHexString(((Long)key).longValue()) : key.toString()) +
               " memorySize = " + Long.toString(memorySize) +
               " timeStamp = " + Long.toString(timeStamp);
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
     * Get the unique key for this tile. The key is a combination of
     * either the JAI-generated unique ID of the owning image or the image's
     * hash key if a unique ID was not available plus the tile index.
     * The returned object will be either Long or BigInteger.
     */
    public Object getTileKey() {
        return key;
    }

    /**
     * Generate a hash key for this tile. This uses the same technique as the
     * Sun memory cache implementation: putting the id of the owning image
     * into the upper bytes of a long or BigInteger value and the tile id into
     * the lower bytes.
     */
    private Object generateTileKey(RenderedImage owner,
                              int tileX,
                              int tileY) {

        if (key != null) {
            throw new IllegalStateException("trying to generate the tile key more than once");
        }

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
