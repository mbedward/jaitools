/*
 * Copyright 2009 Michael Bedward
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

package jaitools.swing;

import com.sun.media.jai.widget.DisplayJAI;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
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
 * @source $URL$
 * @version $Id$
 */
class ImagePane extends DisplayJAI {

    private FrameWithStatusBar frame;

    private RenderedImage displayImage;
    private Rectangle imageDisplayBounds;
    private Point imageOrigin;

    private RenderedImage dataImage;
    private RandomIter dataImageIter;

    private int[] intData;
    private double[] doubleData;

    private enum ImageDataType {
        INTEGRAL, FLOAT;
    }
    private ImageDataType imageDataType;


    /**
     * Constructor.
     *
     * @param frame an object such as an {@linkplain ImageFrame} that implments {@linkplain FrameWithStatusBar}
     * @param displayImg the image to display
     * @param dataImg the image containing data the will be shown in the owning frame's status bar
     * when the mouse is over the pane. If null, data is drawn from the displayImg. If non-null, this
     * image should have bounds equal to, or surrounding, those of the display image.
     */
    public ImagePane(FrameWithStatusBar frame, RenderedImage displayImg, RenderedImage dataImg) {
        super(displayImg);

        this.frame = frame;
        this.displayImage = displayImg;
        this.imageDisplayBounds = new Rectangle(0, 0, displayImage.getWidth(), displayImage.getHeight());
        this.imageOrigin = new Point(displayImage.getMinX(), displayImage.getMinY());

        this.dataImage = (dataImg == null ? displayImg : dataImg);
        this.dataImageIter = RandomIterFactory.create(dataImage, imageDisplayBounds);

        switch (dataImage.getSampleModel().getDataType()) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                imageDataType = ImageDataType.INTEGRAL;
                intData = new int[dataImage.getSampleModel().getNumBands()];
                break;

            case DataBuffer.TYPE_FLOAT:
            case DataBuffer.TYPE_DOUBLE:
                imageDataType = ImageDataType.FLOAT;
                doubleData = new double[dataImage.getSampleModel().getNumBands()];
                break;
        }

        addMouseListener(this);
        addMouseMotionListener(this);
    }

    /**
     * If the mouse cursor is over the image, get the value of the image
     * pixel from band 0
     */
    @Override
    public void mouseMoved(MouseEvent ev) {
        if (dataImage != null) {
            Point pos = ev.getPoint();
            if (imageDisplayBounds.contains(pos)) {
                StringBuilder sb = new StringBuilder();
                sb.append("x:");
                sb.append(pos.x);
                sb.append(" y:");
                sb.append(pos.y);
                sb.append(" band data:");

                if (imageDataType == ImageDataType.INTEGRAL) {
                    dataImageIter.getPixel(pos.x, pos.y, intData);
                    for (int i = 0; i < intData.length; i++) {
                        sb.append(" ");
                        sb.append(intData[i]);
                    }

                } else {
                    dataImageIter.getPixel(pos.x, pos.y, doubleData);
                    for (int i = 0; i < doubleData.length; i++) {
                        sb.append(String.format(" %.4f", doubleData[i]));
                    }
                }

                frame.setStatusText(sb.toString());
            }
        }
    }

    @Override
    public void mouseExited(MouseEvent ev) {
        frame.setStatusText("");
    }

}
