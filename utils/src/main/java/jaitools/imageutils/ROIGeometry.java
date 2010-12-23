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

package jaitools.imageutils;

import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import jaitools.jts.CoordinateSequence2D;

import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.LinkedList;
import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;

/**
 * An {@code ROI} class that honours double precision coordinates when testing for inclusion.
 * 
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class ROIGeometry extends ROI {
    
    private static final long serialVersionUID = 1L;

    /** The {@code Geometry} that defines the area of inclusion */
    private final PreparedGeometry theGeom;
    /** Caches the roi image */
    private PlanarImage roiImage;
    
    private final GeometryFactory geomFactory;

    private final CoordinateSequence2D testPointCS;
    private final com.vividsolutions.jts.geom.Point testPoint;
    
    private final CoordinateSequence2D testRectCS;
    private final Polygon testRect;
    
    private final PixelCoordType coordType;
    private final double delta;
    

    /**
     * Constructor which takes a {@code Geometry} object to be used
     * as the reference against which to test inclusion of image coordinates.
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * <p>
     * Using this constructor will result in pixel inclusion being tested
     * with corner coordinates (equivalent to standard JAI pixel indexing).
     * <p>
     * Note: {@code geom} will be copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     * 
     * @throws IllegalArgumentException if {@code theGeom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom) {
        this(geom, PixelCoordType.CORNER);
    }
    
    /**
     * Constructor which takes a {@code Geometry} object to be used
     * as the reference against which to test inclusion of image coordinates.
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * <p>
     * Note: {@code geom} will be copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     * 
     * @param coordType type of pixel coordinates to use when testing for inclusion
     * 
     * @throws IllegalArgumentException if {@code theGeom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, PixelCoordType coordType) {
        if (geom == null) {
            throw new IllegalArgumentException("geom must not be null");
        }
        
        if (!(geom instanceof Polygon || geom instanceof MultiPolygon)) {
            throw new IllegalArgumentException("geom must be a Polygon, MultiPolygon or null");
        }
            
        geomFactory = new GeometryFactory();
        
        theGeom = PreparedGeometryFactory.prepare((Geometry)geom.clone());
        
        testPointCS = new CoordinateSequence2D(1);
        testPoint = geomFactory.createPoint(testPointCS);

        testRectCS = new CoordinateSequence2D(5);
        testRect = geomFactory.createPolygon(geomFactory.createLinearRing(testRectCS), null);
        
        this.coordType = coordType;
        delta = coordType == PixelCoordType.CENTER ? 0.5 : 0.0;
    }

    /**
     * Not yet implemented.
     */
    @Override
    public ROI add(ROI roi) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Test if 
     * @param p
     * @return 
     */
    @Override
    public boolean contains(Point p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    @Override
    public boolean contains(int x, int y) {
        return contains((double)x, (double)y);
    }

    @Override
    public boolean contains(double x, double y) {
        testPointCS.setX(0, x + delta);
        testPointCS.setY(0, y + delta);
        testPoint.geometryChanged();
        return theGeom.contains(testPoint);
    }

    @Override
    public boolean contains(Rectangle rect) {
        return contains(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public boolean contains(Rectangle2D rect) {
        return contains(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    @Override
    public boolean contains(int x, int y, int w, int h) {
        return contains((double)x, (double)y, (double)w, (double)h);
    }

    @Override
    public boolean contains(double x, double y, double w, double h) {
        setTestRect(x, y, w, h);
        return theGeom.contains(testRect);
    }

    @Override
    public ROI exclusiveOr(ROI roi) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public int[][] getAsBitmask(int x, int y, int width, int height, int[][] mask) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public PlanarImage getAsImage() {
        if(roiImage == null) {
            Envelope env = theGeom.getGeometry().getEnvelopeInternal();
            int x = (int) Math.floor(env.getMinX());
            int y = (int) Math.floor(env.getMinY());
            int w = (int) Math.ceil(env.getWidth());
            int h = (int) Math.ceil(env.getHeight());
            
            ParameterBlockJAI pb = new ParameterBlockJAI("VectorBinarize");
            pb.setParameter("minx", x);
            pb.setParameter("miny", y);
            pb.setParameter("width", w);
            pb.setParameter("height", h);
            pb.setParameter("geometry", theGeom);
            pb.setParameter("coordtype", coordType);
            roiImage = JAI.create("VectorBinarize", pb);
        }
        
        return roiImage;
    }

    @Override
    public LinkedList getAsRectangleList(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    protected LinkedList getAsRectangleList(int x, int y, int width, int height, boolean mergeRectangles) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Shape getAsShape() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Rectangle getBounds() {
        Envelope env = theGeom.getGeometry().getEnvelopeInternal();
        return new Rectangle((int)env.getMinX(), (int)env.getMinY(), (int)env.getWidth(), (int)env.getHeight());
    }

    @Override
    public Rectangle2D getBounds2D() {
        Envelope env = theGeom.getGeometry().getEnvelopeInternal();
        return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }

    @Override
    public int getThreshold() {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ROI intersect(ROI roi) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public boolean intersects(Rectangle rect) {
        setTestRect(rect.x, rect.y, rect.width, rect.height);
        return theGeom.intersects(testRect);
    }

    @Override
    public boolean intersects(Rectangle2D r) {
        setTestRect(r.getMinX(), r.getMinY(), r.getWidth(), r.getHeight());
        return theGeom.intersects(testRect);
    }

    @Override
    public boolean intersects(int x, int y, int w, int h) {
        setTestRect(x, y, w, h);
        return theGeom.intersects(testRect);
    }

    @Override
    public boolean intersects(double x, double y, double w, double h) {
        setTestRect(x, y, w, h);
        return theGeom.intersects(testRect);
    }

    @Override
    public ROI performImageOp(RenderedImageFactory RIF, ParameterBlock paramBlock, int sourceIndex, RenderingHints renderHints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ROI performImageOp(String name, ParameterBlock paramBlock, int sourceIndex, RenderingHints renderHints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public void setThreshold(int threshold) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ROI subtract(ROI roi) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ROI transform(AffineTransform at, Interpolation interp) {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public ROI transform(AffineTransform at) {
        throw new UnsupportedOperationException("Not implemented");
    }

    private void setTestRect(double x, double y, double w, double h) {
        testRectCS.setXY(0, x + delta, y + delta);
        testRectCS.setXY(1, x + delta, y + h - delta);
        testRectCS.setXY(2, x + w - delta, y + h - delta);
        testRectCS.setXY(3, x + w - delta, y + delta);
        testRectCS.setXY(4, x + delta, y + delta);
        testRect.geometryChanged();
    }

}
