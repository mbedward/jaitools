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

import jaitools.numeric.NumberOperations;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import java.lang.ref.WeakReference;
import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 *
 * @author michael
 */
public class SimpleIter {

    private final WeakReference<RenderedImage> imageRef;
    private final int imageDataType;
    
    private final Rectangle iterBounds;
    private final Rectangle delegateBounds;
    private final Number outsideValue;
    
    private final RectIter delegateIter;

    private final Point mainPos;
    private final Point delegatePos;
    private boolean finished;

    /**
     * Creates a new iterator. The bounds are allowed to extend beyond the bounds
     * of the target image. When the iterator is positioned outside the image the
     * specified outside value will be returned.
     * 
     * @param image the target image
     * @param bounds bounds for the iterator; if {@code null} the bounds of the target
     *     image will be used
     * @param outsideValue value to return when the iterator is positioned beyond
     *     the bounds of the target image; may be {@code null} 
     */
    public SimpleIter(RenderedImage image, Rectangle bounds, Number outsideValue) {
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }

        imageRef = new WeakReference<RenderedImage>(image);
        imageDataType = image.getSampleModel().getDataType();
        
        Rectangle imageBounds = new Rectangle(image.getMinX(), image.getMinY(),
                image.getWidth(), image.getHeight());
        
        if (bounds == null) {
            iterBounds = imageBounds;
        } else {
            iterBounds = new Rectangle(bounds);
        }

        delegateBounds = imageBounds.intersection(iterBounds);
        if (delegateBounds.isEmpty()) {
            delegateIter = null;
            delegatePos = null;
            
        } else {
            delegateIter = RectIterFactory.create(image, delegateBounds);
            delegatePos = new Point(delegateBounds.x, delegateBounds.y);
        }

        mainPos = new Point(iterBounds.x, iterBounds.y);
        finished = false;
        
        this.outsideValue = NumberOperations.copy(outsideValue);
    }

    public boolean hasNext() {
        return !finished;
    }

    public boolean next() {
        if (!finished) {
            mainPos.x++ ;
            if (mainPos.x == iterBounds.x + iterBounds.width) {
                if (mainPos.y < iterBounds.y + iterBounds.height - 1) {
                    mainPos.x = iterBounds.x;
                    mainPos.y++;
                } else {
                    finished = true;
                }
            }

            if (!finished) {
                setDelegatePosition();
            }
        }

        return !finished;
    }

    public Rectangle getBounds() {
        return new Rectangle(iterBounds);
    }

    public Point getPos() {
        return finished ? null : new Point(mainPos);
    }

    public Number getSample() {
        return getSample(0);
    }

    public Number getSample(int band) {
        if (!finished) {
            RenderedImage image = imageRef.get();
            if (image == null) {
                throw new IllegalStateException("Target image has been deleted");
            }

            if (delegateBounds.contains(mainPos)) {
                switch (imageDataType) {
                    case DataBuffer.TYPE_DOUBLE:
                        return new Double(delegateIter.getSampleDouble(band));

                    case DataBuffer.TYPE_FLOAT:
                        return new Float(delegateIter.getSampleFloat(band));

                    default:
                        return Integer.valueOf(delegateIter.getSample(band));
                }

            } else {
                return outsideValue;
            }
        }

        return null;
    }

    /**
     * Sets the delegate iterator position. If {@code newPos} is outside
     * the target image bounds, the delegate iterator does not move.
     */
    private void setDelegatePosition() {
        boolean inside = delegateBounds.contains(mainPos);
        if (inside) {
            int dy = mainPos.y - delegatePos.y;
            if (dy < 0) {
                delegateIter.startLines();
                delegatePos.y = delegateBounds.y;
                dy = mainPos.y - delegateBounds.y;
            }

            while (dy > 0) {
                delegateIter.nextLineDone();
                delegatePos.y++ ;
                dy--;
            }

            int dx = mainPos.x - delegatePos.x;
            if (dx < 0) {
                delegateIter.startPixels();
                delegatePos.x = delegateBounds.x;
                dx = mainPos.x - delegateBounds.x;
            }

            while (dx > 0) {
                delegateIter.nextPixelDone();
                delegatePos.x++ ;
                dx--;
            }
        }
    }

}
