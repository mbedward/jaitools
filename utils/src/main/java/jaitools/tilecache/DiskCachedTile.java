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
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferDouble;
import java.awt.image.DataBufferFloat;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferShort;
import java.awt.image.DataBufferUShort;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.imageio.stream.ImageOutputStream;
import javax.media.jai.CachedTile;

/**
 * Represents a tile cached on disk that may also be resident in memory.
 * <p>
 * This class does not hold a reference to the associated raster. When an
 * instance is created, the raster is written to disk and may, at the discretion
 * of the controlling DiskMemTileCache, also remain resident in
 * memory.
 *
 *
 * @see DiskMemTileCache
 * @author Michael Bedward
 * @author Simone Giannecchini, GeoSolutions SAS
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public final class DiskCachedTile implements CachedTile {

    public enum TileAction {

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile has been added to the cache
         */
        ACTION_ADDED(0, "added to cache"),

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile has been added to the cache and immediately loaded
         * into memory
         */
        ACTION_ADDED_RESIDENT(1, "added to cache and placed into memory"),

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile becomes resident in memory
         */
        ACTION_RESIDENT(2, "placed into memory"),

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile is removed from memory
         */
        ACTION_NON_RESIDENT(3, "removed from memory"),

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile is removed from the cache entirely
         */
        ACTION_REMOVED(4, "removed from the cache"),

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile is accessed via the cache
         */
        ACTION_ACCESSED(5, "accessed"),

        /**
         * Value that will be returned by {@linkplain #getAction()} when
         * the tile's raster has been garbage collected
         */
        ACTION_GARBAGE_COLLECTED(6, "garbage collected");

        /**
         * Map for the reverse lookup facility
         */
        private static Map<Integer, TileAction> lookup;
        static {
            lookup = new HashMap<Integer, TileAction>();
            for (TileAction t : EnumSet.allOf(TileAction.class)) {
                lookup.put(t.getAction(), t);
            }
        }
        /** An int associated to this action.*/
        private final int action;
        /** Description of the action. */
        private final String desc;

        /**
         * Private constructor to have maximum control over the values we use for this action.
         * @param action an int associated to this action for interoperability with {@link CachedTile} interface.
         */
        private TileAction(final int action, final String desc) {
            this.action = action;
            this.desc = desc;
        }

        /**
         * Retrieves an int associated to this action for interoperability with {@link CachedTile} interface.
         * @return an int associated to this action for interoperability with {@link CachedTile} interface.
         */
        public int getAction() {
            return action;
        }

        /**
         * Retrieves a description of this action
         * @return a description of this action
         */
        public String getDescription() {
            return desc;
        }

        /**
         * Reverse lookup.
         * @param actionValue int value associated with an action
         * @return the corresponding TileAction
         */
        public static TileAction get(int value) {
            return lookup.get(value);
        }

        /**
         * The default action.
         * @return the default action.
         */
        public static TileAction getDefault() {
            return ACTION_ACCESSED;
        }
    }

    /**
     * The prefix used for temporary cache files
     * data
     */
    public static final String FILE_PREFIX = "tile";

    /**
     * The suffix used for temporary cache files
     */
    public static final String FILE_SUFFIX = ".tmp";

    /**
     * The folder used to store tiles that are being newly cached to disk.
     * If {@code null} (the default) the system's default folder is used.
     * It is safe (though not necessarily sensible) to change this property
     * after some tiles have already been cached to disk.
     */
    private static File cacheFolder = null;
    private static final Object folderLock = new Object();

    private Object id;
    private WeakReference<RenderedImage> owner;
    private int tileX;
    private int tileY;
    private Object tileCacheMetric;
    private long timeStamp;
    private int  numBanks;
    private int  dataLen;
    private long memorySize;
    private File file;
    private Point location;
    private boolean isWritable;

    private TileAction action =TileAction.getDefault();

    /**
     * Get the current cache folder. This is the folder in which
     * newly created temporary files holding tile data will be created.
     *
     * @return a new {@code File} object for the current cache folder
     *         or {@code null} if the default system folder is being
     *         used (ie. the folder corresponding to the {@code java.io.file}
     *         System property
     */
    public static File getCacheFolder() {
        File file = null;
        if (cacheFolder != null) {
            file = new File(cacheFolder.toURI());
        }

        return file;
    }

    /**
     * Set the current cache folder. This is the folder in which
     * newly created temporary files holding tile data will be created.
     * <p>
     * It is safe, though not necessarily sensible, to change the
     * cache folder while one or more caches are running.
     *
     * @param folder the new cache folder or {@code null} to use
     *        the System's default temporary file folder
     */
    public static void setCacheFolder(File folder) {
        synchronized(folderLock) {
            if (folder == null) {
                cacheFolder = null;
            } else {
                cacheFolder = new File(folder.toURI());
            }
        }
    }

    /**
     * Package-private constructor
     *
     * @param id the tile's unique identifier, allocated by the cache
     * @param owner the image to which this tile belongs
     * @param tileX the column index for this tile in the image's tile grid
     * @param tileY the row index for this tile in the image's tile grid
     * @param raster the image data for this tile
     * @param writeToFile if true, the tile's data will be cached to disk
     *        immediately; if false, disk caching is deferred
     * @param tileCacheMetric optional tile cache metric for use in scheduling
     *        (may be {@code null})
     *
     * @throws IOException if an attempt to write the tile to disk fails
     */
    DiskCachedTile(Object id,
                  RenderedImage owner,
                  int tileX,
                  int tileY,
                  Raster raster,
                  boolean writeToFile,
                  Object tileCacheMetric) throws IOException {

        if (owner == null || raster == null) {
            throw new IllegalArgumentException(
                    "All of owner, tile and file args must be non-null");
        }

        this.id = id;
        this.owner = new WeakReference<RenderedImage>(owner);
        this.tileX = tileX;
        this.tileY = tileY;
        this.tileCacheMetric = tileCacheMetric;
        this.location = raster.getBounds().getLocation();
        this.isWritable = (raster instanceof WritableRaster);

        DataBuffer db = raster.getDataBuffer();
        numBanks = db.getNumBanks();
        dataLen = db.getSize();
        db.getOffsets();
        memorySize = DataBuffer.getDataTypeSize(db.getDataType()) / 8L * dataLen * numBanks;

        if (writeToFile ) {
            writeData(raster);
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
     * Get the most recent action for this tile. More information can be
     * retrieved by getting the corresponding {@linkplain DiskCachedTile.TileAction}
     * constant as shown here:
     * <pre><code>
     * int code = tile.getAction();
     * TileAction action = TileAction.get(code);
     * System.out.println("tile action: " + action.getDescription());
     * </code></pre>
     */
    public int getAction() {
        return action.ordinal();
    }

    /**
     * Query if this tile has been cached to disk. This method is
     * a short-cut for: {@code getFile() != null}
     */
    public boolean cachedToDisk() {
        return file != null;
    }

    /**
     * Get this tile's disk cache file. If the tile has not
     * been cached to disk, return {@code null}.
     */
    public File getFile() {
        return file;
    }

    /**
     * Delete this tile's disk cache file
     */
    public void deleteDiskCopy() {
        if (file != null) {
            file.delete();
        }
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
     * Get this tile's location in its parent image in pixel coordinates
     * @return a new Point instance for the location
     */
    public Point getLocation() {
        return new Point(location);
    }

    /**
     * Return the X location (column) of this tile
     * @return tile X location
     */
    public int getTileX() {
        return tileX;
    }

    /**
     * Return the Y location (row) of this tile
     * @return tile Y location
     */
    public int getTileY() {
        return tileY;
    }

    /**
     * Query if this tile is writable
     */
    public boolean isWritable() {
        return isWritable;
    }

    /**
     * Package-private method called by the controlling {@linkplain DiskBasedTileCache}
     * object when the tile is added to, or removed from, the cache.
     */
    void setAction( TileAction action ) {
        this.action = action;
    }

    /**
     * Package-private method called by the controlling {@linkplain DiskBasedTileCache}
     * object when the tile is accessed
     */
    void setTileTimeStamp(long time) {
        this.timeStamp = time;
    }

    /**
     * Package-private method that reads data for the raster associated with this tile
     * from disk
     *
     * @return a new instance of Raster or WritableRaster
     */
    Raster readData() {
        ImageInputStream strm = null;
        DataBuffer dataBuf = null;
        RenderedImage img = owner.get();
        Raster raster = null;

        if (file != null && img != null) {
            try {
                strm = ImageIO.createImageInputStream(file);

                switch (img.getSampleModel().getDataType()) {
                    case DataBuffer.TYPE_BYTE: {
                        byte[][] bankData = new byte[numBanks][dataLen];
                        for (int i = 0; i < numBanks; i++) {
                            strm.read(bankData[i], 0, dataLen);
                        }
                        dataBuf = new DataBufferByte(bankData, dataLen);
                    }
                    break;

                    case DataBuffer.TYPE_DOUBLE: {
                        double[][] bankData = new double[numBanks][dataLen];
                        for (int i = 0; i < numBanks; i++) {
                            strm.readFully(bankData[i], 0, dataLen);
                        }
                        dataBuf = new DataBufferDouble(bankData, dataLen);
                    }
                    break;

                    case DataBuffer.TYPE_FLOAT: {
                        float[][] bankData = new float[numBanks][dataLen];
                        for (int i = 0; i < numBanks; i++) {
                            strm.readFully(bankData[i], 0, dataLen);
                        }
                        dataBuf = new DataBufferFloat(bankData, dataLen);
                    }
                    break;

                    case DataBuffer.TYPE_INT: {
                        int[][] bankData = new int[numBanks][dataLen];
                        for (int i = 0; i < numBanks; i++) {
                            strm.readFully(bankData[i], 0, dataLen);
                        }
                        dataBuf = new DataBufferInt(bankData, dataLen);
                    }
                    break;

                    case DataBuffer.TYPE_SHORT: {
                        short[][] bankData = new short[numBanks][dataLen];
                        for (int i = 0; i < numBanks; i++) {
                            strm.readFully(bankData[i], 0, dataLen);
                        }
                        dataBuf = new DataBufferShort(bankData, dataLen);
                    }
                    break;

                    case DataBuffer.TYPE_USHORT: {
                        short[][] bankData = new short[numBanks][dataLen];
                        for (int i = 0; i < numBanks; i++) {
                            strm.readFully(bankData[i], 0, dataLen);
                        }
                        dataBuf = new DataBufferUShort(bankData, dataLen);
                    }
                    break;

                    default:
                        throw new UnsupportedOperationException("Unsupported image data type");
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(DiskCachedTile.class.getName()).
                        log(Level.SEVERE, "Failed to read image tile data", ex);
                return null;

            } catch (IOException ex) {
                Logger.getLogger(DiskCachedTile.class.getName()).log(Level.SEVERE, "Failed to read image tile data", ex);
                return null;
            }
            finally{
            	if(strm!=null)
            		try{
            			strm.close();
            		}catch (Throwable e) {
						// chew me
					}
            }
        }

        if (dataBuf != null) {
            if (isWritable) {
                raster = Raster.createWritableRaster(img.getSampleModel(), dataBuf, location);
            } else {
                raster = Raster.createRaster(img.getSampleModel(), dataBuf, location);
            }
        }

        return raster;
    }


    /**
     * Write data for the raster associated with this tile to
     * disk. This may be called by <code>DiskMemTileCache</code>
     * as well as be the tile itself.
     */
    void writeData(Raster raster) throws IOException {
        ImageOutputStream strm = null;
        DataBuffer dataBuf = raster.getDataBuffer();

        if (file == null) {
            // first time this tile has been written to disk
            file = createFile();
        }

        try {
            strm = ImageIO.createImageOutputStream(file);

            switch (dataBuf.getDataType()) {
                case DataBuffer.TYPE_BYTE:
                     {
                        byte[] bankData;
                        for (int i = 0; i < numBanks; i++) {
                            bankData = ((DataBufferByte) dataBuf).getData(i);
                            strm.write(bankData);
                        }
                    }
                    break;

                case DataBuffer.TYPE_DOUBLE:
                     {
                        double[] bankData;
                        for (int i = 0; i < numBanks; i++) {
                            bankData = ((DataBufferDouble) dataBuf).getData(i);
                            strm.writeDoubles(bankData, 0, dataLen);
                        }
                    }
                    break;

                case DataBuffer.TYPE_FLOAT:
                     {
                        float[] bankData;
                        for (int i = 0; i < numBanks; i++) {
                            bankData = ((DataBufferFloat) dataBuf).getData(i);
                            strm.writeFloats(bankData, 0, dataLen);
                        }
                    }
                    break;

                case DataBuffer.TYPE_INT:
                     {
                        int[] bankData;
                        for (int i = 0; i < numBanks; i++) {
                            bankData = ((DataBufferInt) dataBuf).getData(i);
                            strm.writeInts(bankData, 0, dataLen);
                        }
                    }
                    break;

                case DataBuffer.TYPE_SHORT:
                     {
                        short[] bankData;
                        for (int i = 0; i < numBanks; i++) {
                            bankData = ((DataBufferShort) dataBuf).getData(i);
                            strm.writeShorts(bankData, 0, dataLen);
                        }
                    }
                    break;

                case DataBuffer.TYPE_USHORT:
                    {
                        short[] bankData;
                        for (int i = 0; i < numBanks; i++) {
                            bankData = ((DataBufferShort) dataBuf).getData(i);
                            strm.writeShorts(bankData, 0, dataLen);
                        }
                    }
                    break;

                default:
                    throw new UnsupportedOperationException("Unsupported image data type");
            }


        } catch (FileNotFoundException ex) {
            Logger.getLogger(DiskCachedTile.class.getName()).
                    log(Level.SEVERE, "Failed to write image tile data", ex);

        } catch (IOException ex) {
            Logger.getLogger(DiskCachedTile.class.getName()).log(Level.SEVERE, "Failed to write image tile data", ex);
        }
        finally{
        	if(strm!=null)
        		try{
        			strm.close();
        		}catch (Throwable e) {
					// chew me
				}
        }
    }

    /**
     * Create a file to cache the given tile on disk. Presently
     * this method does nothing more than delegate to File.createTempFile
     *
     * @throws java.io.IOException
     */
    private File createFile() throws IOException {
        synchronized(folderLock) {
            return File.createTempFile(FILE_PREFIX, FILE_SUFFIX, cacheFolder);
        }
    }

}
