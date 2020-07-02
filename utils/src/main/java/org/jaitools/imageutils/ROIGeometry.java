/* 
 *  Copyright (c) 2010-2011, Michael Bedward. All rights reserved. 
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

package org.jaitools.imageutils;

import org.jaitools.imageutils.shape.LiteShape;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.renderable.ParameterBlock;
import java.awt.image.renderable.RenderedImageFactory;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.media.jai.Interpolation;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.PlanarImage;
import javax.media.jai.ROI;
import javax.media.jai.ROIShape;

import org.jaitools.jts.CoordinateSequence2D;

import org.locationtech.jts.awt.ShapeReader;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryComponentFilter;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.MultiPolygon;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;
import org.locationtech.jts.geom.prep.PreparedGeometry;
import org.locationtech.jts.geom.prep.PreparedGeometryFactory;
import org.locationtech.jts.geom.util.AffineTransformation;


/**
 * An ROI class backed by a vector object providing precision and the ability 
 * to handle massive regions. It has a minimal memory footprint allowing it to 
 * be used with massive images.
 * <p>
 * JAI operations often involve converting ROI objects to images. This class 
 * implements its {@link #getAsImage()} method using the JAITools "VectorBinarize" 
 * operator to avoid exhausting available memory when dealing with ROIs that 
 * cover massive image areas.
 * <p>
 * Note that this class can be used to honour floating precision pixel coordinates
 * by setting the {@code useFixedPrecision} constructor argument to {@code false}.
 * The effect of the default fixed coordinate precision is to provide equivalent
 * behaviour to that of the standard {@code ROIShape}, where pixel coordinates
 * are treated as referring to the upper-left pixel corner.
 * 
 * @author Michael Bedward
 * @author Andrea Aime
 * @since 1.1
 * @version $Id$
 */
public class ROIGeometry extends ROI {
    
    private static final Logger LOGGER = Logger.getLogger(ROIGeometry.class.getName());
    
    /** 
     * Default setting for use of anti-aliasing when drawing the reference
     * {@code Geometry} during a {@link #getAsImage()} request.
     * The default value is {@code true} which provides behaviour corresponding
     * to that of the standard JAI {@code ROIShape} class.
     */
    public static final boolean DEFAULT_ROIGEOMETRY_ANTIALISING = true;
    
    /** 
     * Default setting for use of fixed precision ({@code true}).
     */
    public static final boolean DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION = false;
    
    private boolean useAntialiasing = DEFAULT_ROIGEOMETRY_ANTIALISING;
    
    private boolean useFixedPrecision = DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION;
    
    private static final long serialVersionUID = 1L;
    
    private static final AffineTransformation Y_INVERSION = new AffineTransformation(1, 0, 0, 0, -1, 0);
    
    private static final String UNSUPPORTED_ROI_TYPE = 
            "The argument be either an ROIGeometry or an ROIShape";

    /** The {@code Geometry} that defines the area of inclusion */
    private final PreparedGeometry theGeom;
    
    /** Thread safe cache for the roi image */
    private volatile PlanarImage roiImage;
    
    private final GeometryFactory geomFactory;
    
    private final static double tolerance = 1d;
    private final static PrecisionModel PRECISION = new PrecisionModel(tolerance);
    // read, remove excess ordinates, force precision and collect
    private final static GeometryFactory PRECISE_FACTORY = new GeometryFactory(PRECISION);
    
    private final static PrecisionModel FLOAT_PRECISION = new PrecisionModel(PrecisionModel.FLOATING_SINGLE);
    private final static GeometryFactory FLOAT_PRECISION_FACTORY = new GeometryFactory(FLOAT_PRECISION);

    private final CoordinateSequence2D testPointCS;
    private final org.locationtech.jts.geom.Point testPoint;
    
    private final CoordinateSequence2D testRectCS;
    private final Polygon testRect;
    
    private RenderingHints hints;

    /**
     * Constructor which takes a {@code Geometry} object to be used
     * as the reference against which to test inclusion of image coordinates.
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * The input geometry is copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     * 
     * @throws IllegalArgumentException if {@code geom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom) {
        this(geom, DEFAULT_ROIGEOMETRY_ANTIALISING, DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION);
    }
    
    /**
     * Constructor which takes a {@code Geometry} object and a {@code boolean}
     * value for whether to use fixed coordinate precision (equivalent to
     * working with integer pixel coordinates). 
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * The input geometry is copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     *        
     * @param useFixedPrecision whether to use fixed precision when comparing
     *        pixel coordinates to the reference geometry
     * 
     * @throws IllegalArgumentException if {@code geom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, final boolean useFixedPrecision) {
        this(geom, DEFAULT_ROIGEOMETRY_ANTIALISING, useFixedPrecision);
    }
    
    /**
     * Constructors a new ROIGeometry.
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * The input geometry is copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     * 
     * @param antiAliasing whether to use anti-aliasing when converting this
     *        ROI to an image
     * 
     * @param useFixedPrecision whether to use fixed precision when comparing
     *        pixel coordinates to the reference geometry
     * 
     * @throws IllegalArgumentException if {@code geom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, final boolean antiAliasing, final boolean useFixedPrecision) {
        this(geom, DEFAULT_ROIGEOMETRY_ANTIALISING, useFixedPrecision, null);
    }
    
    /**
     * Builds a new ROIGeometry.
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * The input geometry is copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     * 
     * @param hints The JAI hints to be used when generating the raster equivalent of this ROI       
     * 
     * @throws IllegalArgumentException if {@code geom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, final RenderingHints hints) {
        this(geom, DEFAULT_ROIGEOMETRY_ANTIALISING, DEFAULT_ROIGEOMETRY_USEFIXEDPRECISION, hints);
    }
    
    /**
     * Fully-specified constructor.
     * The argument {@code geom} must be either a {@code Polygon} or
     * {@code MultiPolygon}.
     * The input geometry is copied so subsequent changes to it will
     * not be reflected in the {@code ROIGeometry} object.
     * 
     * @param geom either a {@code Polygon} or {@code MultiPolygon} object
     *        defining the area(s) of inclusion.
     * 
     * @param antiAliasing whether to use anti-aliasing when converting this
     *        ROI to an image
     * 
     * @param useFixedPrecision whether to use fixed precision when comparing
     *        pixel coordinates to the reference geometry
     *        
     * @param hints The JAI hints to be used when generating the raster equivalent of this ROI       
     * 
     * @throws IllegalArgumentException if {@code geom} is {@code null} or
     *         not an instance of either {@code Polygon} or {@code MultiPolygon}
     */
    public ROIGeometry(Geometry geom, final boolean antiAliasing, 
            final boolean useFixedPrecision, final RenderingHints hints) {
        if (geom == null) {
            throw new IllegalArgumentException("geom must not be null");
        }
        
        if (!(geom instanceof Polygon || geom instanceof MultiPolygon)) {
            throw new IllegalArgumentException("geom must be a Polygon, MultiPolygon");
        }
        
        this.useFixedPrecision = useFixedPrecision;
        this.hints = hints;
        
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
            geomFactory = FLOAT_PRECISION_FACTORY;
            cloned = geomFactory.createGeometry(geom);
            Coordinate[] coords = cloned.getCoordinates();
            for (Coordinate coord : coords) {
                Coordinate cc1 = coord;
                FLOAT_PRECISION.makePrecise(cc1);
            }
            cloned.normalize();
        }
        
        theGeom = PreparedGeometryFactory.prepare(cloned);
        
        testPointCS = new CoordinateSequence2D(1);
        testPoint = geomFactory.createPoint(testPointCS);

        testRectCS = new CoordinateSequence2D(5);
        testRect = geomFactory.createPolygon(geomFactory.createLinearRing(testRectCS), null);
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
            return buildROIGeometry(union);
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
        testPointCS.setX(0, x);
        testPointCS.setY(0, y);
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
            return buildROIGeometry(theGeom.getGeometry().symDifference(geom));
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
     * see org.jaitools.media.jai.vectorbinarize.VectorBinarizeDescriptor
     */
    @Override
    public PlanarImage getAsImage() {
        if (roiImage == null) {
            synchronized (this) {
                // this synch idiom works only if roiImage is volatile, keep it as such
                if (roiImage == null) {
                    Envelope env = theGeom.getGeometry().getEnvelopeInternal();
                    int x = (int) Math.floor(env.getMinX());
                    int y = (int) Math.floor(env.getMinY());
                    int w = (int) Math.ceil(env.getMaxX()) - x;
                    int h = (int) Math.ceil(env.getMaxY()) - y;

                    ParameterBlockJAI pb = new ParameterBlockJAI("VectorBinarize");
                    pb.setParameter("minx", x);
                    pb.setParameter("miny", y);
                    pb.setParameter("width", w);
                    pb.setParameter("height", h);
                    pb.setParameter("geometry", theGeom);
                    pb.setParameter("antiAliasing", useAntialiasing);
                    roiImage = JAI.create("VectorBinarize", pb, hints);
                }
            }
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
        return new LiteShape(theGeom.getGeometry());
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
            return buildROIGeometry(intersect);

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

    /**
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
            Geometry difference = theGeom.getGeometry().difference(geom);
            return buildROIGeometry(difference);
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
        Geometry cloned = theGeom.getGeometry().copy();
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
        return buildROIGeometry(cloned);
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
        testRectCS.setXY(0, x, y);
        testRectCS.setXY(1, x, y + h);
        testRectCS.setXY(2, x + w, y + h);
        testRectCS.setXY(3, x + w, y);
        testRectCS.setXY(4, x, y);
        testRect.geometryChanged();
    }
    
    /**
     * Setup a ROIGeometry on top of a geometry.
     * It takes care of removing invalid polygon, opened line strings and so on
     * @param geometry the input geometry to be used as reference to create a new {@link ROIGeometry}
     * @return a ROI from the input geometry.
     */
    private ROI buildROIGeometry(Geometry geometry) {
        // cleanup the geometry, extract only the polygons, set oriented operations might
        // have returned a mix of points and lines in the resulting geometries
        final List<Polygon> polygons = new ArrayList<Polygon>();
        geometry.apply(new GeometryComponentFilter() {

            public void filter(Geometry geom) {
                if (geom instanceof Polygon) {
                    polygons.add((Polygon) geom);
                }
            }
        });

        // build a polygon or a multipolygon
        Geometry geom = null;
        if (polygons.size() == 0) {
            geom = geomFactory.createMultiPolygon(new Polygon[0]);
        } else if (polygons.size() == 1) {
            geom = polygons.get(0);
        } else {
            Polygon[] polygonArray = (Polygon[]) polygons.toArray(new Polygon[polygons.size()]);
            geom = geomFactory.createMultiPolygon(polygonArray);
        }

        return new ROIGeometry(geom, this.useAntialiasing, this.useFixedPrecision, this.hints);
    }

}
