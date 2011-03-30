/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.swing;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;

import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;

/**
 * Extends the JAI widget DisplayJAI. Displays an image gets information
 * about the pixel location and value(s) under the mouse cursor to be
 * displayed by the owning frame (e.g. an ImageFrame object).
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
class ImagePane extends SimpleImagePane implements MouseListener, MouseMotionListener {

    private ImageFrame frame;

    private RenderedImage displayImage;
    private RenderedImage dataImage;
    private RandomIter dataImageIter;
    private boolean integralImageDataType;
    private final Rectangle imageBounds;

    private int[] intData;
    private double[] doubleData;
    
    private void setMouseListener() {
        addMouseListener(this);
        addMouseMotionListener(this);
    }

    
    /**
     * Constructor.
     *
     * @param frame an object such as an {@linkplain ImageFrame} that implments {@linkplain FrameWithStatusBar}
     * @param displayImg the image to display
     * @param dataImg the image containing data the will be shown in the owning frame's status bar
     * when the mouse is over the pane. If null, data is drawn from the displayImg. If non-null, this
     * image should have bounds equal to, or surrounding, those of the display image.
     */
    public ImagePane(ImageFrame frame, RenderedImage displayImg, RenderedImage dataImg) {
        setImage(displayImg);
        this.frame = frame;
        this.displayImage = displayImg;
        this.imageBounds = new Rectangle(displayImage.getMinX(), displayImage.getMinY(), 
                displayImage.getWidth(), displayImage.getHeight());
        
        this.dataImage = (dataImg == null ? displayImg : dataImg);

        switch (dataImage.getSampleModel().getDataType()) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                integralImageDataType = true;
                intData = new int[dataImage.getSampleModel().getNumBands()];
                break;

            case DataBuffer.TYPE_FLOAT:
            case DataBuffer.TYPE_DOUBLE:
                integralImageDataType = false;
                doubleData = new double[dataImage.getSampleModel().getNumBands()];
                break;
        }

        setMouseListener();
    }

    /**
     * If the mouse cursor is over the image, get the value of the image
     * pixel from band 0
     */
    @Override
    public void mouseMoved(MouseEvent ev) {
        if (dataImage != null) {
            Point imagePos = getImageCoords(ev.getPoint(), null);
            if (imageBounds.contains(imagePos)) {
                
                if (dataImageIter == null) {
                    dataImageIter = RandomIterFactory.create(dataImage, imageBounds);
                }

                if (integralImageDataType) {
                    dataImageIter.getPixel(imagePos.x, imagePos.y, intData);
                    frame.setCursorInfo(imagePos, intData);
                } else {
                    dataImageIter.getPixel(imagePos.x, imagePos.y, doubleData);
                    frame.setCursorInfo(imagePos, doubleData);
                }
            } else {
                frame.setStatusText("");
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent ev) {
        frame.setStatusText("");
    }

    /**
     * Empty method.
     * @param e the event
     */
    public void mouseClicked(MouseEvent e) {}

    /**
     * Empty method.
     * @param e the event
     */
    public void mousePressed(MouseEvent e) {}

    /**
     * Empty method.
     * @param e the event
     */
    public void mouseReleased(MouseEvent e) {}

    /**
     * Empty method.
     * @param e the event
     */
    public void mouseEntered(MouseEvent e) {}

    /**
     * Empty method.
     * @param e the event
     */
    public void mouseDragged(MouseEvent e) {}

}
