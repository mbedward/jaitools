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

package jaitools.demo;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.geom.AffineTransform;
import java.awt.image.RenderedImage;
import javax.swing.JPanel;


/**
 * A very basic Swing widget to display a {@code RenderedImage}.
 * Used by JAI-tools demo applications.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class SimpleImagePane extends JPanel {

    RenderedImage image;
    AffineTransform transform;
    private final Object lock = new Object();

    
    public SimpleImagePane() {
        transform = new AffineTransform();
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent ce) {
                setTransform();
            }
        });
    }
    
    public void setImage(RenderedImage image) {
        this.image = image;
        setTransform();
        repaint();
    }
    
    public void clear() {
        image = null;
        repaint();
    }
    
    public void resetTransform() {
        setTransform();
    }
    
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        synchronized(lock) {
            if (image != null) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.drawRenderedImage(image, transform);
            }
        }
    }
    
    private void setTransform() {
        synchronized(lock) {
            if (image != null) {
                Rectangle r = getVisibleRect();
                double xscale = r.getWidth() / image.getWidth();
                double yscale = r.getHeight() / image.getHeight();
                double scale = Math.min(xscale, yscale);

                transform.setToScale(scale, scale);
            }
        }
    }
    
}