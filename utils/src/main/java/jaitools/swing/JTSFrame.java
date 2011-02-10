/*
 * Copyright 2010 Michael Bedward
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

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Polygon;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JFrame;
import javax.swing.JPanel;

/**
 * A simple Swing widget to display JTS objects.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JTSFrame extends JFrame {

    private static final int MARGIN = 2;
    private Canvas canvas;

    public JTSFrame(String title) throws HeadlessException {
        super(title);
        initComponents();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    public void addGeometry(Geometry p, Color col) {
        canvas.elements.add(new Element(p, col));
    }
    
    public void addCoordinate(Coordinate c, Color col) {
        canvas.elements.add(new Element(c, col));
    }

    private void initComponents() {
        canvas = new Canvas();
        getContentPane().add(canvas);
    }

    private static class Element {

        Object geom;
        Color color;

        public Element(Object g, Color c) {
            geom = g;
            color = c;
        }
    }

    private static class Canvas extends JPanel {

        AffineTransform tr;
        List<Element> elements = new ArrayList<Element>();
        
        private static final int POINT_RADIUS = 4;

        @Override
        protected void paintComponent(Graphics g) {
            if (!elements.isEmpty()) {
                setTransform();
                Graphics2D g2 = (Graphics2D) g;
                g2.setStroke(new BasicStroke(2.0f));
                Coordinate[] coords;
                for (Element e : elements) {
                    g2.setColor(e.color);
                    if (e.geom instanceof Polygon) {
                        Polygon poly = (Polygon) e.geom;
                        coords = poly.getExteriorRing().getCoordinates();
                        draw(g2, coords);
                        for (int i = 0; i < poly.getNumInteriorRing(); i++) {
                            coords = poly.getInteriorRingN(i).getCoordinates();
                            draw(g2, coords);
                        }
                    } else if (e.geom instanceof Geometry) {
                        coords = ((Geometry)e.geom).getCoordinates();
                        draw(g2, coords);
                    } else if (e.geom instanceof Coordinate) {
                        draw(g2, (Coordinate)e.geom);
                    }
                }
            }
        }

        private void draw(Graphics2D g2, Coordinate[] coords) {
            for (int i = 1; i < coords.length; i++) {
                Point2D p0 = new Point2D.Double(coords[i - 1].x, coords[i - 1].y);
                tr.transform(p0, p0);
                Point2D p1 = new Point2D.Double(coords[i].x, coords[i].y);
                tr.transform(p1, p1);
                g2.drawLine((int) p0.getX(), (int) p0.getY(), (int) p1.getX(), (int) p1.getY());
            }
        }
        
        private void draw(Graphics2D g2, Coordinate coord) {
            Point2D p = new Point2D.Double(coord.x, coord.y);
            tr.transform(p, p);
            g2.fillOval((int)p.getX() - POINT_RADIUS, (int)p.getY() - POINT_RADIUS, 
                    2 * POINT_RADIUS, 2 * POINT_RADIUS );
        }

        private void setTransform() {
            Envelope env = new Envelope();
            for (int i = 0; i < elements.size(); i++) {
                Object obj = elements.get(i).geom;
                if (obj instanceof Geometry) {
                    Geometry g = (Geometry) obj;
                    env.expandToInclude(g.getEnvelopeInternal());
                } else if (obj instanceof Coordinate) {
                    Coordinate c = (Coordinate) obj;
                    env.expandToInclude(c);
                }
            }

            Rectangle visRect = getVisibleRect();
            Rectangle drawingRect = new Rectangle(
                    visRect.x + MARGIN, visRect.y + MARGIN, visRect.width - 2 * MARGIN, visRect.height - 2 * MARGIN);

            double scale = Math.min(drawingRect.getWidth() / env.getWidth(), drawingRect.getHeight() / env.getHeight());
            double xoff = MARGIN - scale * env.getMinX();
            double yoff = MARGIN + env.getMaxY() * scale;
            tr = new AffineTransform(scale, 0, 0, -scale, xoff, yoff);
        }
    }
}
