/*
 * Copyright 2010-2011 Michael Bedward
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
import javax.media.jai.ROIShape;

import com.vividsolutions.jts.awt.ShapeReader;
import com.vividsolutions.jts.awt.ShapeWriter;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.geom.prep.PreparedGeometry;
import com.vividsolutions.jts.geom.prep.PreparedGeometryFactory;
import com.vividsolutions.jts.geom.util.AffineTransformation;

/**
 * An {@code ROI} class that honours double precision coordinates when testing for inclusion.
 * 
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @version $Id$
 */
public class ROIGeometry extends ROI {
    
    private static boolean DEFAULT_ROIGEOMETRY_ANTIALISING = true;
    
    private static boolean DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION = true;
    
    private boolean useAntialiasing = DEFAULT_ROIGEOMETRY_ANTIALISING;
    
    private boolean useFixedPrecision = DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION;
    
    private static final long serialVersionUID = 1L;
    
    private static final AffineTransformation Y_INVERSION = new AffineTransformation(1, 0, 0, 0, -1, 0);
    
    private static final String UNSUPPORTED_ROI_TYPE = 
            "The argument be either an ROIGeometry or an ROIShape";

    /** The {@code Geometry} that defines the area of inclusion */
    private final PreparedGeometry theGeom;
    /** Caches the roi image */
    private PlanarImage roiImage;
    
    private final GeometryFactory geomFactory;
    
    private final static double tolerance = 1d;
    private final static PrecisionModel PRECISION = new PrecisionModel(tolerance);
    // read, remove excess ordinates, force precision and collect
    private final static GeometryFactory PRECISE_FACTORY = new GeometryFactory(PRECISION);

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
        this(geom, PixelCoordType.CORNER, DEFAULT_ROIGEOMETRY_ANTIALISING, DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION);
    }
    
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
     * @param useFixedPrecision whether to use fixedPrecision on ROIGeometry coordinates.
     * 
     * @throws IllegalArgumentException if {@code theGeom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, final boolean useFixedPrecision) {
        this(geom, PixelCoordType.CORNER, DEFAULT_ROIGEOMETRY_ANTIALISING, useFixedPrecision);
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
        this(geom, coordType, DEFAULT_ROIGEOMETRY_ANTIALISING, DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION);
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
     * @param antiAliasing whether to use antiAliasing hint when getting the geometry as an image.
     * 
     * @param useFixedPrecision whether to use fixedPrecision on ROIGeometry coordinates.
     * 
     * @throws IllegalArgumentException if {@code theGeom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, PixelCoordType coordType, final boolean antiAliasing, 
            final boolean useFixedPrecision) {
        if (geom == null) {
            throw new IllegalArgumentException("geom must not be null");
        }
        
        if (!(geom instanceof Polygon || geom instanceof MultiPolygon)) {
            throw new IllegalArgumentException("geom must be a Polygon, MultiPolygon or null");
        }
        this.useFixedPrecision = useFixedPrecision;
        Geometry cloned = null; 
        if (useFixedPrecision){
            geomFactory = PRECISE_FACTORY;
            cloned = geomFactory.createGeometry(geom);
            Coordinate[] coords = cloned.getCoordinates();
            for (Coordinate coord : coords) {
                Coordinate cc1 = coord;
                PRECISION.makePrecise(cc1);
            }
            cloned.normalize();
        } else {
            geomFactory = new GeometryFactory();
            cloned = (Geometry)geom.clone();
        }
        
        theGeom = PreparedGeometryFactory.prepare(cloned);
        
        testPointCS = new CoordinateSequence2D(1);
        testPoint = geomFactory.createPoint(testPointCS);

        testRectCS = new CoordinateSequence2D(5);
        testRect = geomFactory.createPolygon(geomFactory.createLinearRing(testRectCS), null);
        
        this.coordType = coordType;
        delta = coordType == PixelCoordType.CENTER ? 0.5 : 0.0;
    }

    /**
     * Returns a new instance which is the union of this ROI and {@code roi}. 
     * This is only possible if {@code roi} is an instance of ROIGeometry 
     * or {@link ROIShape}.
     * 
     * @param roi the ROI to add
     * @return the union as a new instance
     * @throws UnsupportedOperationException if {@code roi} is not an instance
     *         of ROIGeometry or {@link ROIShape}
     */
    @Override
    public ROI add(ROI roi) {
        final Geometry geom = getGeometry(roi);
        if (geom != null) {
            Geometry union = geom.union(theGeom.getGeometry());
//            Geometry fixed = PRECISE_FACTORY.createGeometry(union);
//            Coordinate[] coords = fixed.getCoordinates();
//            for (Coordinate coord : coords) {
//                Coordinate cc1 = coord;
//                PRECISION.makePrecise(cc1);
//            }
//            union = fixed;
            return new ROIGeometry(union);
        }
        throw new UnsupportedOperationException(UNSUPPORTED_ROI_TYPE);
    }

    /**
     * Tests if this ROI contains the given point.
     * 
     * @param p the point
     * 
     * @return {@code true} if the point is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(Point p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * Tests if this ROI contains the given point.
     * 
     * @param p the point
     * 
     * @return {@code true} if the point is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(Point2D p) {
        return contains(p.getX(), p.getY());
    }

    /**
     * Tests if this ROI contains the given image location.
     * 
     * @param x location X ordinate
     * @param y location Y ordinate
     * 
     * @return {@code true} if the location is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(int x, int y) {
        return contains((double)x, (double)y);
    }

    /**
     * Tests if this ROI contains the given image location.
     * 
     * @param x location X ordinate
     * @param y location Y ordinate
     * 
     * @return {@code true} if the location is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(double x, double y) {
        testPointCS.setX(0, x + delta);
        testPointCS.setY(0, y + delta);
        testPoint.geometryChanged();
        return theGeom.contains(testPoint);
    }

    /**
     * Tests if this ROI contains the given rectangle.
     * 
     * @param rect the rectangle
     * @return {@code true} if the rectangle is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(Rectangle rect) {
        return contains(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Tests if this ROI contains the given rectangle.
     * 
     * @param rect the rectangle
     * @return {@code true} if the rectangle is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(Rectangle2D rect) {
        return contains(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
    }

    /**
     * Tests if this ROI contains the given rectangle.
     * 
     * @param x rectangle origin X ordinate
     * @param y rectangle origin Y ordinate
     * @param w rectangle width
     * @param h rectangle height
     * 
     * @return {@code true} if the rectangle is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(int x, int y, int w, int h) {
        return contains((double)x, (double)y, (double)w, (double)h);
    }

    /**
     * Tests if this ROI contains the given rectangle.
     * 
     * @param x rectangle origin X ordinate
     * @param y rectangle origin Y ordinate
     * @param w rectangle width
     * @param h rectangle height
     * 
     * @return {@code true} if the rectangle is within this ROI; 
     *         {@code false} otherwise
     */
    @Override
    public boolean contains(double x, double y, double w, double h) {
        setTestRect(x, y, w, h);
        return theGeom.contains(testRect);
    }

    /**
     * Returns a new instance which is the exclusive OR of this ROI and {@code roi}. 
     * This is only possible if {@code roi} is an instance of ROIGeometry 
     * or {@link ROIShape}.
     * 
     * @param roi the ROI to add
     * @return the union as a new instance
     * @throws UnsupportedOperationException if {@code roi} is not an instance
     *         of ROIGeometry or {@link ROIShape}
     */
    @Override
    public ROI exclusiveOr(ROI roi) {
        final Geometry geom = getGeometry(roi);
        if (geom != null) {
            return new ROIGeometry(theGeom.getGeometry().symDifference(geom));
        }
        throw new UnsupportedOperationException(UNSUPPORTED_ROI_TYPE);
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    public int[][] getAsBitmask(int x, int y, int width, int height, int[][] mask) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Gets an image representation of this ROI using the {@code VectorBinarize}
     * operation. For an ROI with very large bounds but simple shape(s) the 
     * resulting image has a small memory footprint.
     * 
     * @return a new image representing this ROI
     * @see jaitools.media.jai.vectorbinarize.VectorBinarizeDescriptor
     */
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
            pb.setParameter("antiAliasing", useAntialiasing);
            roiImage = JAI.create("VectorBinarize", pb);
        }
        
        return roiImage;
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    public LinkedList getAsRectangleList(int x, int y, int width, int height) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    protected LinkedList getAsRectangleList(int x, int y, int width, int height, boolean mergeRectangles) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Gets a new {@link Shape} representing this ROI.
     * 
     * @return the shape
     */
    @Override
    public Shape getAsShape() {
        return new ShapeWriter().toShape(theGeom.getGeometry());
    }
    
    /**
     * Returns the ROI as a JTS {@code Geometry}.
     * 
     * @return the geometry
     */
    public Geometry getAsGeometry() {
        return theGeom.getGeometry();
    }

    /**
     * Gets the enclosing rectangle of this ROI.
     * 
     * @return a new rectangle
     */
    @Override
    public Rectangle getBounds() {
        Envelope env = theGeom.getGeometry().getEnvelopeInternal();
        return new Rectangle((int)env.getMinX(), (int)env.getMinY(), (int)env.getWidth(), (int)env.getHeight());
    }

    /**
     * Gets the enclosing double-precision rectangle of this ROI.
     * 
     * @return a new rectangle
     */
    @Override
    public Rectangle2D getBounds2D() {
        Envelope env = theGeom.getGeometry().getEnvelopeInternal();
        return new Rectangle2D.Double(env.getMinX(), env.getMinY(), env.getWidth(), env.getHeight());
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    public int getThreshold() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * Returns a new instance which is the intersection of this ROI and {@code roi}. 
     * This is only possible if {@code roi} is an instance of ROIGeometry 
     * or {@link ROIShape}.
     * 
     * @param roi the ROI to intersect with
     * @return the intersection as a new instance
     * @throws UnsupportedOperationException if {@code roi} is not an instance
     *         of ROIGeometry or {@link ROIShape}
     */
    @Override
    public ROI intersect(ROI roi) {
        final Geometry geom = getGeometry(roi);
        if (geom != null) {
            Geometry intersect = geom.intersection(theGeom.getGeometry());
//            Geometry fixed = PRECISE_FACTORY.createGeometry(intersect);
//            Coordinate[] coords = fixed.getCoordinates();
//            for (Coordinate coord : coords) {
//                Coordinate cc1 = coord;
//                PRECISION.makePrecise(cc1);
//            }
//            intersect = fixed;
            return new ROIGeometry(intersect);

        }
        throw new UnsupportedOperationException(UNSUPPORTED_ROI_TYPE);
    }
    
    /**
     * Gets a {@link Geometry} from an input {@link ROI}.
     * 
     * @param roi the ROI
     * @return a {@link Geometry} instance from the provided input;
     * null in case the input roi is neither a geometry, nor a shape. 
     */
    private Geometry getGeometry(ROI roi){
        if (roi instanceof ROIGeometry){
            return ((ROIGeometry) roi).getAsGeometry();
        } else if (roi instanceof ROIShape){
            final Shape shape = ((ROIShape) roi).getAsShape();
            final Geometry geom = ShapeReader.read(shape, 0, geomFactory);
            geom.apply(Y_INVERSION);
            return geom;
        }
        return null;
    }

    /**
     * Tests if the given rectangle intersects with this ROI.
     * 
     * @param rect the rectangle
     * @return {@code true} if there is an intersection; {@code false} otherwise
     */
    @Override
    public boolean intersects(Rectangle rect) {
        setTestRect(rect.x, rect.y, rect.width, rect.height);
        return theGeom.intersects(testRect);
    }

    /**
     * Tests if the given rectangle intersects with this ROI.
     * 
     * @param rect the rectangle
     * @return {@code true} if there is an intersection; {@code false} otherwise
     */
    @Override
    public boolean intersects(Rectangle2D rect) {
        setTestRect(rect.getMinX(), rect.getMinY(), rect.getWidth(), rect.getHeight());
        return theGeom.intersects(testRect);
    }

    /**
     * Tests if the given rectangle intersects with this ROI.
     * 
     * @param x rectangle origin X ordinate
     * @param y rectangle origin Y ordinate
     * @param w rectangle width
     * @param h rectangle height
     * @return {@code true} if there is an intersection; {@code false} otherwise
     */
    @Override
    public boolean intersects(int x, int y, int w, int h) {
        setTestRect(x, y, w, h);
        return theGeom.intersects(testRect);
    }

    /**
     * Tests if the given rectangle intersects with this ROI.
     * 
     * @param x rectangle origin X ordinate
     * @param y rectangle origin Y ordinate
     * @param w rectangle width
     * @param h rectangle height
     * @return {@code true} if there is an intersection; {@code false} otherwise
     */
    @Override
    public boolean intersects(double x, double y, double w, double h) {
        setTestRect(x, y, w, h);
        return theGeom.intersects(testRect);
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    public ROI performImageOp(RenderedImageFactory RIF, ParameterBlock paramBlock, int sourceIndex, RenderingHints renderHints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    public ROI performImageOp(String name, ParameterBlock paramBlock, int sourceIndex, RenderingHints renderHints) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /**
     * This method is not supported.
     * @throws UnsupportedOperationException if called
     */
    @Override
    public void setThreshold(int threshold) {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * Returns a new instance which is the difference of this ROI and {@code roi}. 
     * This is only possible if {@code roi} is an instance of ROIGeometry 
     * or {@link ROIShape}.
     * 
     * @param roi the ROI to add
     * @return the union as a new instance
     * @throws UnsupportedOperationException if {@code roi} is not an instance
     *         of ROIGeometry or {@link ROIShape}
     */
    @Override
    public ROI subtract(ROI roi) {
        final Geometry geom = getGeometry(roi);
        if (geom != null) {
            return new ROIGeometry(theGeom.getGeometry().difference(geom));
        }
        throw new UnsupportedOperationException(UNSUPPORTED_ROI_TYPE);
    }

    /**
     * Returns a new ROI created by applying the given transform to 
     * this ROI.
     * 
     * @param at the transform
     * @param interp ignored
     * 
     * @return the new ROI
     */
    @Override
    public ROI transform(AffineTransform at, Interpolation interp) {
        return transform(at);
    }

    /**
     * Returns a new ROI created by applying the given transform to 
     * this ROI.
     * 
     * @param at the transform
     * 
     * @return the new ROI
     */
    @Override
    public ROI transform(AffineTransform at) {
        Geometry cloned = (Geometry) theGeom.getGeometry().clone();
        cloned.apply(new AffineTransformation(at.getScaleX(), at.getShearX(), at.getTranslateX(), 
                at.getShearY(), at.getScaleY(), at.getTranslateY()));
        if (useFixedPrecision){
            Geometry fixed = PRECISE_FACTORY.createGeometry(cloned);
            Coordinate[] coords = fixed.getCoordinates();
            for (Coordinate coord : coords) {
                Coordinate precise = coord;
                PRECISION.makePrecise(precise);
            }
            cloned = fixed;
        }
        return new ROIGeometry(cloned);
    }

    /**
     * Helper function for contains and intersects methods.
     * 
     * @param x rectangle origin X ordinate
     * @param y rectangle origin Y ordinate
     * @param w rectangle width
     * @param h rectangle height
     */
    private void setTestRect(double x, double y, double w, double h) {
        testRectCS.setXY(0, x + delta, y + delta);
        testRectCS.setXY(1, x + delta, y + h - delta);
        testRectCS.setXY(2, x + w - delta, y + h - delta);
        testRectCS.setXY(3, x + w - delta, y + delta);
        testRectCS.setXY(4, x + delta, y + delta);
        testRect.geometryChanged();
    }

}
