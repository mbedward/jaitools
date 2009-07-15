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
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.ColorModel;
import java.awt.image.Raster;
import java.awt.image.SampleModel;
import java.awt.image.TileObserver;
import java.awt.image.WritableRaster;
import java.awt.image.WritableRenderedImage;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.media.jai.ImageLayout;
import javax.media.jai.PlanarImage;

/**
 * A tiled image class
 * @author Michael Bedward
 */
public class DiskMemTiledImage 
        extends PlanarImage
        implements WritableRenderedImage, PropertyChangeListener {
    
    private DiskMemTileCache tileCache;
    private Rectangle tileGrid;
    private boolean[][] tileInUse;


    /**
     * Minimal onstructor. This will set default values for the image's
     * min x and y coordinates (0), x and y tile offsets (0) and <code>ColorModel</code>
     * (<code>null</code>)
     *
     * @param width image width
     * @param height image height
     * @param tileSampleModel a <code>SampleModel</code> specifying the dimensions
     * data type etc. for image tiles
     */
    public DiskMemTiledImage(
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
    public DiskMemTiledImage(
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
    public DiskMemTiledImage(
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
    public DiskMemTiledImage(
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

        tileCache = new DiskMemTileCache();
    }

    /**
     * Retrieve a tile for reading
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
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void removeTileObserver(TileObserver to) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public WritableRaster getWritableTile(int tileX, int tileY) {
        WritableRaster r = null;
        if (tileGrid.contains(tileX, tileY)) {
            if (tileInUse[tileX - tileGrid.x][tileY - tileGrid.y]) {
                // TODO: throw an exception here ?
                return null;
            }

            r = (WritableRaster) tileCache.getTile(this, tileX, tileY);
            if (r == null) {
                r = createTile(tileX, tileY);
                tileCache.add(this, tileX, tileY, r);
            }

            tileInUse[tileX - tileGrid.x][tileY - tileGrid.y] = true;
        }
        return r;
    }

    public void releaseWritableTile(int tileX, int tileY) {
        if (tileGrid.contains(tileX, tileY)) {
            tileInUse[tileX - tileGrid.x][tileY - tileGrid.y] = false;
        }
    }

    public boolean isTileWritable(int tileX, int tileY) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public Point[] getWritableTileIndices() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public boolean hasTileWriters() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void setData(Raster r) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    public void propertyChange(PropertyChangeEvent evt) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private WritableRaster createTile(int tileX, int tileY) {
        assert(tileCache.getTile(this, tileX, tileY) == null);

        Point location = new Point(tileXToX(tileX), tileYToY(tileY));
        return createWritableRaster(getSampleModel(), location);
    }
}
