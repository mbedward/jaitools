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

package jaitools.demo.tiledimage;

import jaitools.tiledimage.DiskMemImage;
import jaitools.utils.ImageFrame;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;

/**
 * Demonstrates drawing into a <code>DiskMemImage</code> using
 * Graphics2D methods. See comments in the source code for more
 * details.
 *
 * @see jaitools.tiledimage.DiskMemImage
 * @see jaitools.tiledimage.DiskMemImageGraphics
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class DrawingDemo {

    public static void main(String[] args) {
        DrawingDemo me = new DrawingDemo();
        me.demo();
    }

    private void demo() {
        /*
         * First we create a DiskMemImage with an RGB color model.
         * The image will be tiled: we specify a tile size of 128x128 pixels
         * when creating the sample model. The image size is 256x256 pixels
         * (2x2 tiles).
         */
        ColorModel cm = ColorModel.getRGBdefault();
        SampleModel sm = cm.createCompatibleSampleModel(128, 128);
        DiskMemImage img = new DiskMemImage(0, 0, 256, 256, 0, 0, sm, cm);

        /*
         * The createGraphics methods returns an instance of
         * jaitools.tiledimage.DiskMemImageGraphics which provides a
         * bridge to Graphics2D drawing methods
         */
        Graphics2D gr = img.createGraphics();

        /*
         * Here we do some common operations to demonstrate
         * that they work
         */
        gr.setBackground(Color.ORANGE);
        gr.clearRect(0, 0, 256, 256);

        Shape shp = new Rectangle(64, 64, 128, 128);
        gr.setColor(Color.BLUE);
        gr.setStroke(new BasicStroke(3.0f));
        gr.draw(shp);

        gr.setColor(Color.RED);
        gr.fillRect(96, 96, 64, 64);

        gr.setColor(Color.BLACK);
        Font font = gr.getFont();
        gr.setFont(font.deriveFont(24f));
        gr.drawString("Hello World !", 48, 32);

        /*
         * Draw lines on tile boundaries
         */
        gr.setStroke(new BasicStroke(1.0f));
        gr.setColor(Color.GRAY);
        gr.drawLine(128, 0, 128, 255);
        gr.drawLine(0, 128, 255, 128);

        /*
         * We display the results using jaitools.utils.ImageFrame widget
         */
        ImageFrame frame = new ImageFrame(img, "Image of 4 square tiles");
        frame.setVisible(true);
    }
}
