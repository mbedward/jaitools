/*
 * Copyright 2011 Michael Bedward
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

package jaitools.imageutils;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;

import javax.media.jai.PlanarImage;

/**
 * A random-access image iterator.
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id: $
 */
public class BoundedRandomIter {

    /**
     * Bounds within which this iterator will operate.
     */
    protected final Rectangle bounds;
    
    /**
     * Image data type.
     */
    protected final int dataType;
    
    /**
     * The image accessed by this iterator.
     */
    protected final PlanarImage image;
    
    /**
     * Whether the image is of integral data type.
     */
    protected final boolean integralType;
    
    /**
     * The X,Y coordinates of the last successful image access.
     */
    protected Point lastPos;
    

    /**
     * Creates a new instance. Package-private: to be called by {@link IterFactory}
     * methods.
     * 
     * @param image the image
     * @param bounds the iterator bounds or {@code null} for the image bounds
     * 
     * @throws IllegalArgumentException if {@code image} is {@code null}
     */
    BoundedRandomIter(RenderedImage image, Rectangle bounds) {
        if (image == null) {
            throw new IllegalArgumentException("Image must not be null");
        }
        
        this.image = PlanarImage.wrapRenderedImage(image);
        this.bounds = bounds == null ?
                new Rectangle(image.getMinX(), image.getMinY(),
                        image.getWidth(), image.getHeight())
                : bounds;
        
        this.dataType = image.getSampleModel().getDataType();
        switch (this.dataType) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_INT:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
                this.integralType = true;
                break;

            case DataBuffer.TYPE_DOUBLE:
            case DataBuffer.TYPE_FLOAT:
                this.integralType = false;
                break;
                
            default:  // just in case
                throw new UnsupportedOperationException("Unsupported image data type");
        }
    }

    /**
     * Gets an image value. If the location is outside this iterator's
     * bounds a {@code null} value is returned.
     * 
     * @param x X ordinate
     * @param y Y ordinate
     * @param band image band
     * 
     * @return the image value or {@code null} if the location is outside
     *         this iterator's bounds
     */
    public Number getSample(int x, int y, int band) {
        if (bounds.contains(x, y)) {
            setLastPos(x, y);
            return doGetValue(x, y, band);
            
        } else {
            clearLastPos();
            return null;
        }
    }

    /**
     * Gets the X,Y coordinates of the last sample request.
     * Returns {@code null} if no accesses have yet been made or if the 
     * last request was out of bounds.
     * 
     * @return last image access location or {@code null}
     */
    public Point getLastPos() {
        return lastPos == null ? null : new Point(lastPos);
    }

    
    /**
     * Helper method for {@link #getSample(int, int, int) }. Takes care of 
     * converting from primitive type to return type.
     * 
     * @param x X ordinate
     * @param y Y ordinate
     * @param band image band
     * 
     * @return the image value
     */
    protected Number doGetValue(int x, int y, int band) {
        int ivalue;
        double dvalue;
        int tileX = image.XToTileX(x);
        int tileY = image.YToTileY(y);
        if (integralType) {
            ivalue = image.getTile(tileX, tileY).getSample(x, y, band);
            return toReturnType(ivalue);
        } else {
            dvalue = image.getTile(tileX, tileY).getSampleDouble(x, y, band);
            return toReturnType(dvalue);
        }
    }

    /**
     * Converts an integer to the integral return type.
     * 
     * @param ivalue integer value
     * 
     * @return value as return type
     */
    protected Number toReturnType(int ivalue) {
        switch (dataType) {
            case DataBuffer.TYPE_BYTE:
                return Byte.valueOf((byte) (ivalue & 255));
            case DataBuffer.TYPE_INT:
                return Integer.valueOf(ivalue);
            case DataBuffer.TYPE_SHORT:
                return Short.valueOf((short) (ivalue & 65535));
            case DataBuffer.TYPE_USHORT:
                return Short.valueOf((short) (ivalue & 65535));
        }
        throw new IllegalStateException("Field dataType has invalid value: " + dataType);
    }

    /**
     * Converts a value to the floating-point return type.
     * 
     * @param dvalue double value
     * 
     * @return value as return type
     */
    protected Number toReturnType(double dvalue) {
        switch (dataType) {
            case DataBuffer.TYPE_DOUBLE:
                return new Double(dvalue);
            case DataBuffer.TYPE_FLOAT:
                return new Float(dvalue);
        }
        throw new IllegalStateException("Field dataType has invalid value: " + dataType);
    }

    /**
     * Records a sample location.
     * 
     * @param x X ordinate
     * @param y Y ordinate
     */
    protected void setLastPos(int x, int y) {
        if (lastPos == null) {
            lastPos = new Point(x, y);
        } else {
            lastPos.x = x; 
            lastPos.y = y;
        }
    }
    
    /**
     * Clears the stored sample location.
     */
    protected void clearLastPos() {
        lastPos = null;
    }
    
}
