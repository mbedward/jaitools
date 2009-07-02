/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
 * Information associated with a cached tile.
 *
 * <p> This class is used by SunTileCache to create an object that
 * includes all the information associated with a tile, and is put
 * into the tile cache.
 *
 * <p> It also serves as a double linked list.
 *
 * @see SunTileCache
 *
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

    private DiskCachedTile previous;
    private DiskCachedTile next;

    private int action = 0;             // add, remove, update from tile cache


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

        key = hashKey(owner, tileX, tileY);

        DataBuffer db = tile.getDataBuffer();
        memorySize = DataBuffer.getDataTypeSize(db.getDataType()) / 8L *
                     db.getSize() * db.getNumBanks();
    }

    /**
     * Generate a hash key for this tile. This uses the same technique as the
     * Sun memory cache implementation: putting the id of the owning image
     * into the upper bytes of a long or BigInteger value and the tile id into
     * the lower bytes.
     */
    static Object hashKey(RenderedImage owner,
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

    public RenderedImage getOwner() {
        return owner.get();
    }

    public long getTileTimeStamp() {
        return timeStamp;
    }

    public Object getTileCacheMetric() {
        return tileCacheMetric;
    }

    public long getTileSize() {
        return memorySize;
    }

    public int getAction() {
        return action;
    }

    public File getFile() {
        return file;
    }
}
