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

package jaitools.utils;

import com.sun.media.jai.widget.DisplayJAI;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.image.DataBuffer;
import java.awt.image.RenderedImage;
import javax.media.jai.iterator.RandomIter;
import javax.media.jai.iterator.RandomIterFactory;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JTextField;


/**
 * Simple display widget
 * 
 * @author Michael Bedward
 */
public class ImageFrame extends JFrame {

    private JTextField statusBar;

    /**
     * Constructor. Sets the default close operation to
     * EXIT_ON_CLOSE but this can be changed by the client code
     * after construction.
     */
    public ImageFrame() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    /**
     * Displays an image in a scrolling panel
     * 
     * @param img image to be displayed
     * @param title title for the frame
     */
    public void displayImage(RenderedImage img, String title) {

        setTitle(title);

        ImagePane pane = new ImagePane(this, img);
        getContentPane().add(pane, BorderLayout.CENTER);

        statusBar = new JTextField();
        statusBar.setEditable(false);
        statusBar.setMinimumSize(new Dimension(100, 30));
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());

        getContentPane().add(statusBar, BorderLayout.SOUTH);

        setSize(500, 500);
        pack();

        setLocationByPlatform(true);
        setVisible(true);
    }

    void setStatusText(String s) {
        statusBar.setText(s);
    }

}
class ImagePane extends DisplayJAI {
    private ImageFrame frame;
    private RenderedImage image;
    private Rectangle imageDisplayBounds;
    private Point imageOrigin;
    private RandomIter imageIter;

    private int[] intData;
    private double[] doubleData;

    private enum ImageDataType {
        INTEGRAL, FLOAT;
    }
    private ImageDataType imageDataType;


    ImagePane(ImageFrame frame, RenderedImage image) {
        super(image);

        this.frame = frame;
        this.image = image;
        this.imageDisplayBounds = new Rectangle(0, 0, image.getWidth(), image.getHeight());
        this.imageOrigin = new Point(image.getMinX(), image.getMinY());
        this.imageIter = RandomIterFactory.create(image, imageDisplayBounds);

        switch (image.getSampleModel().getDataType()) {
            case DataBuffer.TYPE_BYTE:
            case DataBuffer.TYPE_SHORT:
            case DataBuffer.TYPE_USHORT:
            case DataBuffer.TYPE_INT:
                imageDataType = ImageDataType.INTEGRAL;
                intData = new int[image.getSampleModel().getNumBands()];
                break;

            case DataBuffer.TYPE_FLOAT:
            case DataBuffer.TYPE_DOUBLE:
                imageDataType = ImageDataType.FLOAT;
                doubleData = new double[image.getSampleModel().getNumBands()];
                break;
        }

        addMouseMotionListener(this);
    }

    /**
     * If the mouse cursor is over the image, get the value of the image
     * pixel from band 0
     */
    @Override
    public void mouseMoved(MouseEvent ev) {
        if (image != null) {
            Point pos = ev.getPoint();
            if (imageDisplayBounds.contains(pos)) {
                StringBuilder sb = new StringBuilder();
                sb.append("x:");
                sb.append(pos.x);
                sb.append(" y:");
                sb.append(pos.y);
                sb.append(" band data:");

                if (imageDataType == ImageDataType.INTEGRAL) {
                    imageIter.getPixel(pos.x, pos.y, intData);
                    for (int i = 0; i < intData.length; i++) {
                        sb.append(" ");
                        sb.append(intData[i]);
                    }

                } else {
                    imageIter.getPixel(pos.x, pos.y, doubleData);
                    for (int i = 0; i < doubleData.length; i++) {
                        sb.append(String.format(" %.4f", doubleData[i]));
                    }
                }

                frame.setStatusText(sb.toString());
            }
        }
    }

}
