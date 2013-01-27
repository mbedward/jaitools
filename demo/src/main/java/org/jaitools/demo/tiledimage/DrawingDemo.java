/* 
 *  Copyright (c) 2009, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.demo.tiledimage;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.LinearGradientPaint;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.image.ColorModel;
import java.awt.image.SampleModel;

import org.jaitools.swing.ImageFrame;
import org.jaitools.tiledimage.DiskMemImage;


/**
 * Demonstrates drawing into a <code>DiskMemImage</code> using
 * Graphics2D methods. See comments in the source code for more
 * details.
 *
 * @see org.jaitools.tiledimage.DiskMemImage
 * @see org.jaitools.tiledimage.DiskMemImageGraphics
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class DrawingDemo {
    
    private final int TILE_SIZE = 128;
    private final int IMAGE_SIZE = 4 * TILE_SIZE;
        
    public static void main(String[] args) {
        DrawingDemo me = new DrawingDemo();
        me.basicDrawing();
        me.clippingDemo();
    }

    private void basicDrawing() {
        DiskMemImage image = createImage();
        
        /*
         * The createGraphics methods returns an instance of
         * DiskMemImageGraphics which provides a bridge to 
         * Graphics2D drawing methods
         */
        Graphics2D gr = image.createGraphics();
        gr.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        /*
         * Here we do some common operations to demonstrate
         * that they work
         */
        gr.setBackground(Color.ORANGE);
        gr.clearRect(0, 0, IMAGE_SIZE, IMAGE_SIZE);

        /*
         * Fill alternating tiles
         */
        gr.setColor(Color.BLUE);
        gr.setStroke(new BasicStroke(3.0f));
        for (int iy = 0, y = image.getMinY(); y < image.getMaxY(); iy++, y += TILE_SIZE) {
            for (int ix = 0, x = image.getMinX(); x < image.getMaxX(); ix++, x += TILE_SIZE) {
                if ((ix + iy) % 2 == 0) {
                    Rectangle r = new Rectangle(x, y, TILE_SIZE, TILE_SIZE);
                    gr.fill(r);
                }
            }
        }
        
        /*
         * Derive a child graphics object, apply a rotation to it
         * and draw some text.
         */
        Graphics2D child = (Graphics2D) gr.create();
        AffineTransform tr = AffineTransform.getRotateInstance(
                Math.PI / 4,
                IMAGE_SIZE / 2,
                IMAGE_SIZE / 2);
        
        child.setTransform(tr);
        
        child.setColor(Color.BLACK);
        Font font = gr.getFont();
        child.setFont(font.deriveFont(48f));
        child.drawString("Hello World !", IMAGE_SIZE / 6, IMAGE_SIZE / 2);
        
        ImageFrame.showImage(image, "Drawing demo");
    }
    
    private void clippingDemo() {
        DiskMemImage image = createImage();
        Graphics2D gr = image.createGraphics();
        
        /*
         * Apply a clip region to the graphics so that drawing only 
         * appears in the upper left quadrant
         */
        gr.clip(new Rectangle(0, 0, IMAGE_SIZE / 2, IMAGE_SIZE / 2));
        
        gr.setPaint(new LinearGradientPaint(
                new Point(0, 0), 
                new Point(IMAGE_SIZE, IMAGE_SIZE), 
                new float[] {0.0f, 0.5f},
                new Color[] {Color.BLACK, Color.WHITE}));
        
        gr.fill(image.getBounds());
        
        /*
         * Apply a transformation to the graphics which will interact
         * with the clip region.
         */
        gr.transform(AffineTransform.getRotateInstance(
                Math.PI / 4, IMAGE_SIZE / 2, IMAGE_SIZE / 2));
        
        gr.setPaint(Color.RED);
        gr.setStroke(new BasicStroke(5f));
        
        int x = IMAGE_SIZE / 2 - 100;
        int w = 100;
        gr.draw(new Rectangle(x, x, w, w));
        
        /*
         * Clear the clip region and redraw the rectangle with 
         * a dashed line
         */
        gr.clip(null);
        gr.setPaint(Color.BLACK);
        gr.setStroke(new BasicStroke(
                3f, 
                BasicStroke.CAP_BUTT, 
                BasicStroke.JOIN_MITER, 
                1f, 
                new float[]{5f, 5f}, 
                0f));
        
        gr.draw(new Rectangle(x, x, w, w));
        
        ImageFrame.showImage(image, "foo");
    }

    private DiskMemImage createImage() {
        ColorModel cm = ColorModel.getRGBdefault();
        SampleModel sm = cm.createCompatibleSampleModel(TILE_SIZE, TILE_SIZE);

        return new DiskMemImage(
                0, 0, 
                IMAGE_SIZE, IMAGE_SIZE, 
                0, 0, sm, cm);
    }
}
