/*
 * Copyright 2010 Michael Bedward
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

package jaitools.media.jai;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.util.Vector;
import javax.media.jai.ImageLayout;
import javax.media.jai.OpImage;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;

/**
 * Abstract base class for operators that generate non-image attributes from a
 * source image while passing source image pixels directly to destination pixels.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL: $
 * @version $Id: $
 */
public abstract class AttributeOpImage extends OpImage {

    /**
     * An optional ROI to define the region over which to derive attributes.
     * If an ROI is not provided this will be set to an ROIShape having the
     * bounds of the source image.
     */
    protected ROI roi;
    
    /**
     * Flag indicating whether the ROI is simple (ROIShape) or not.
     */
    protected boolean isShapeROI;

    /**
     * Bounds of the source image as a convenience.
     */
    protected Rectangle srcBounds;


    /**
     * Constructor.
     *
     * @param source the source image from from which to derive attributes
     * @param roi an optional {@code ROI} to define sub-region(s) of the
     *        source image to examine.
     */
    public AttributeOpImage(RenderedImage source, ROI roi) {
        super(wrapSource(source),
              new ImageLayout(source),
              null,
              false);

        if (roi == null) {
            this.roi = new ROIShape(getSourceImage(0).getBounds());
            isShapeROI = true;
        } else {
            this.roi = roi;
            isShapeROI = roi instanceof ROIShape;
        }
    }
    
    /**
     * Helper function for constructor. Wraps the given image in a {@code Vector}
     * as required by {@link OpImage} constructor. Also checks for a null 
     * source image.
     * 
     * @param img the image to wrap
     * @return a new {@code Vector} containing the image
     */
    private static Vector wrapSource(RenderedImage img) {
        if (img == null) {
            throw new IllegalArgumentException("Source image must not be null");
        }
        
        Vector<RenderedImage> v = new Vector<RenderedImage>();
        v.add(img);
        return v;
    }

    /**
     * Always returns false since source and destination tiles are the same.
     */
    @Override
    public boolean computesUniqueTiles() {
        return false;
    }

    /**
     * Gets the requested image tile (which will be from the source image).
     * 
     * @param tileX  tile X index
     * @param tileY  tile Y index
     *
     * @return the requested tile
     */
    @Override
    public Raster getTile(int tileX, int tileY) {
        return this.getSourceImage(0).getTile(tileX, tileY);
    }

    /**
     * Handles a request to compute image data for a given tile by
     * simply passing a getTile request to the source image.
     * 
     * @param tileX  tile X index
     * @param tileY  tile Y index
     *
     * @return the requested tile
     */
    @Override
    public Raster computeTile(int tileX, int tileY) {
        return getTile(tileX, tileY);
    }

    /**
     * Gets the requested image tiles (which will be from the source image).
     *
     * @param tileIndices tile X and Y indices as {@code Points}
     *
     * @throws IllegalArgumentException  If {@code tileIndices} is {@code null}.
     */
    @Override
    public Raster[] getTiles(Point[] tileIndices) {
        if ( tileIndices == null ) {
            throw new IllegalArgumentException("tileIndices must not be null");
        }

        return getSourceImage(0).getTiles(tileIndices);
    }

    /**
     * Maps the source rectangle into destination space (which will be identical).
     *
     * @param sourceRect the {@code Rectangle} in source image coordinates
     * 
     * @param sourceIndex the index of the source image (must be 0 since there
     *        is only one source image)
     *
     * @return A new {@code Rectangle} with identical bounds to {@code sourceRect}
     *
     * @throws IllegalArgumentException if {@code sourceRect} is {@code null}
     * @throws IllegalArgumentException if {@code sourceIndex} is not 0
     */
    public Rectangle mapSourceRect(Rectangle sourceRect, int sourceIndex) {
        if ( sourceRect == null ) {
            throw new IllegalArgumentException("sourceRect must not be null");
        }

        if (sourceIndex != 0) {
            throw new IllegalArgumentException("sourceIndex arg must be 0");
        }
        
        return new Rectangle(sourceRect);
    }
    
    /**
     * Maps the destination rectangle into source image space (which will be identical).
     *
     * @param destRect the {@code Rectangle} in destination image coordinates
     * 
     * @param sourceIndex the index of the source image (must be 0 since there
     *        is only one source image)
     *
     * @return A new {@code Rectangle} with identical bounds to {@code destRect}
     *
     * @throws IllegalArgumentException if {@code destRect} is {@code null}
     * @throws IllegalArgumentException if {@code sourceIndex} is not 0
     */
    public Rectangle mapDestRect(Rectangle destRect, int sourceIndex) {
        if ( destRect == null ) {
            throw new IllegalArgumentException("destRect must not be null");
        }

        if (sourceIndex != 0) {
            throw new IllegalArgumentException("sourceIndex arg must be 0");
        }
        
        return new Rectangle(destRect);
    }

    /**
     * Retrieves an attribute by name. Calling this method will fire a request 
     * for it to be generated if it has not already been so, or if the sub-class
     * does not cache the attribute.
     * 
     * @param name the attribute name
     * 
     * @return the requested attribute or {@code null} if the name does not
     *         match any of the available attributes
     *
     * @throws IllegalArgumentException if {@code name} is {@code null}
     */
    public abstract Object getAttribute(String name);

    protected abstract String[] getAttributeNames();
    
}
