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
package jaitools.media.jai.kernel;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.MultiPoint;
import jaitools.CollectionFactory;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.PathIterator;
import java.awt.geom.Point2D;
import java.util.List;
import javax.media.jai.KernelJAI;

/**
 * A factory class with static methods to create a variety of
 * KernelJAI objects with specified geometries
 *
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class KernelFactory {

    /**
     * Meaning of the kernel values
     */
    public static enum ValueType {

        /**
         * Simple binary kernel with values of 1 or 0
         */
        BINARY,
        /**
         * The value of each kernel element is its distance to the
         * kernel's key element (the element that is placed over
         * image pixels during kernel operations).
         */
        DISTANCE,
        /**
         * The value of each kernel element is the inverse distance to the
         * kernel's key element (the element that is placed over
         * image pixels during kernel operations).
         */
        INVERSE_DISTANCE;
    };

    /**
     * Create a new KernelJAI object with a circular configuration.
     * Kernel elements within the circle will have value 1.0f; those
     * outside will have value 0.0f.
     * <p>
     * This is equivalent to, but faster than, calling...
     * <p>
     * {@code createCircle(radius, Kernel.ValueType.BINARY, 1.0f) }
     *
     * @param radius radius of the circle
     * @return a new instance of KernelJAI
     */
    public static KernelJAI createCircle(int radius) {
        if (radius <= 0) {
            throw new IllegalArgumentException(
                    "Invalid radius (" + radius + "); must be > 0");
        }

        KernelFactoryHelper kh = new KernelFactoryHelper();
        float[] weights = kh.makeCircle(radius);
        int w = 2*radius + 1;
        kh.rowFill(weights, w, w);
        return new KernelJAI(w, w, weights);
    }

    /**
     * Creates a new KernelJAI object with a circular configuration.
     * The kernel width is 2*radius + 1.
     * The kernel's key element is at position x=radius, y=radius
     *
     * @param radius the radius of the circle expressed in pixels
     *
     * @param type one of
     * {@linkplain ValueType#BINARY},
     * {@linkplain ValueType#DISTANCE} or
     * {@linkplain ValueType#INVERSE_DISTANCE}
     *
     * @param centreValue the value to assign to the kernel centre (key element)
     * 
     * @return a new instance of KernelJAI
     */
    public static KernelJAI createCircle(int radius, ValueType type, float centreValue) {

        if (radius <= 0) {
            throw new IllegalArgumentException(
                    "Invalid radius (" + radius + "); must be > 0");
        }

        KernelFactoryHelper kh = new KernelFactoryHelper();

        int width = 2 * radius + 1;
        float[] weights = new float[width * width];

        float r2 = radius * radius;
        int k0 = 0;
        int k1 = weights.length - 1;

        for (int y = radius; y > 0; y--) {
            int y2 = y * y;
            for (int x = -radius; x <= radius; x++, k0++, k1--) {
                float dist2 = x * x + y2;
                float value = 0f;

                if (kh.fcomp(r2, dist2) >= 0) {
                    if (type == ValueType.DISTANCE) {
                        value = (float) Math.sqrt(dist2);
                    } else if (type == ValueType.INVERSE_DISTANCE) {
                        value = 1.0f / (float) Math.sqrt(dist2);
                    } else {
                        value = 1.0f;
                    }

                    weights[k0] = weights[k1] = value;
                }
            }
        }

        for (int x = -radius; x <= radius; x++, k0++) {
            float value;
            if (x == 0) {
                value = centreValue;
            } else {
                if (type == ValueType.DISTANCE) {
                    value = (float) Math.sqrt(x * x);
                } else if (type == ValueType.INVERSE_DISTANCE) {
                    value = 1.0f / (float) Math.sqrt(x * x);
                } else {
                    value = 1.0f;
                }
            }
            weights[k0] = value;
        }

        return new KernelJAI(width, width, weights);
    }

    /**
     * Creates a new KernelJAI object with an annular configuration
     * (like a doughnut).
     *
     * The kernel width is 2*outerRadius + 1.
     * The kernel's key element is at position x=outerRadius, y=outerRadius
     *
     * <p>
     * An IllegalArgumentException will be thrown if:
     * <ul>
     * <li> The value of outerRadius not greater than 0
     * <li> The value of innerRadius is not less than outerRadius
     * </ul>
     *
     * Calling this method with innerRadius == 0 is equivalent to
     * calling {@linkplain #createCircle }
     *
     * @param outerRadius the radius of the circle expressed in pixels
     * @param innerRadius the radius of the 'hole' of the annulus
     *
     * @param type one of
     * {@linkplain ValueType#BINARY},
     * {@linkplain ValueType#DISTANCE} or
     * {@linkplain ValueType#INVERSE_DISTANCE}
     *
     * @param centreValue the value to assign to the kernel centre (key element)
     *
     * @return a new instance of KernelJAI
     *
     */
    public static KernelJAI createAnnulus(int outerRadius, int innerRadius, ValueType type, float centreValue) {

        if (innerRadius < 0) {
            throw new IllegalArgumentException(
                    "Invalid innerRadius (" + innerRadius + "); must be >= 0");
        }

        if (outerRadius <= innerRadius) {
            throw new IllegalArgumentException("outerRadius must be greater than innerRadius");
        }

        if (innerRadius == 0) {
            return createCircle(outerRadius, type, centreValue);
        }

        KernelFactoryHelper kh = new KernelFactoryHelper();

        int width = 2 * outerRadius + 1;
        float[] weights = new float[width * width];

        int outer2 = outerRadius * outerRadius;
        float inner2 = innerRadius * innerRadius;
        int k0 = 0;
        int k1 = weights.length - 1;

        for (int y = outerRadius; y > 0; y--) {
            int y2 = y * y;
            for (int x = -outerRadius; x <= outerRadius; x++, k0++, k1--) {
                float dist2 = x * x + y2;
                float value = 0f;

                if (kh.fcomp(dist2, outer2) <= 0 && kh.fcomp(dist2, inner2) > 0) {
                    if (type == ValueType.DISTANCE) {
                        value = (float) Math.sqrt(dist2);
                    } else if (type == ValueType.INVERSE_DISTANCE) {
                        value = 1.0f / (float) Math.sqrt(dist2);
                    } else {
                        value = 1.0f;
                    }

                    weights[k0] = weights[k1] = value;
                }
            }
        }

        for (int x = -outerRadius; x <= outerRadius; x++, k0++) {
            float value = 0f;
            if (x == 0) {
                value = centreValue;

            } else if (x < -innerRadius || x > innerRadius) {

                if (type == ValueType.DISTANCE) {
                    value = (float) Math.sqrt(x * x);
                } else if (type == ValueType.INVERSE_DISTANCE) {
                    value = 1.0f / (float) Math.sqrt(x * x);
                } else {
                    value = 1.0f;
                }
            }
            
            weights[k0] = value;
        }

        return new KernelJAI(width, width, weights);
    }


    /**
     * Creates a new KernelJAI object with a rectangular configuraton.
     * An IllegalArgumentException will be thrown if width or height are less than 1.
     * <p>
     * This is equivalent to calling...
     * <p>
     * {@code createRectangle(width, height, Kernel.ValueType.BINARY, width/2, height/2, 1.0f) }
     *
     * @param width rectangle width
     * @param height rectangle height
     *
     * @return a new instance of KernelJAI
     */
    public static KernelJAI createRectangle(int width, int height) {
        float [] weights = (new KernelFactoryHelper()).makeRect(width, height);
        return new KernelJAI(width, height, weights);
    }

    /**
     * Creates a new KernelJAI object with a rectangular configuration.
     * <p>
     * An IllegalArgumentException will be thrown if:
     * <ul>
     * <li> width or height are less than 1
     * <li> keyX is not in the range 0:width-1
     * <li> keyY is not in the range 0:height-1
     * </ul>
     *
     * @param width rectangle width
     * @param height rectangle height
     *
     * @param type one of
     * {@linkplain ValueType#BINARY},
     * {@linkplain ValueType#DISTANCE} or
     * {@linkplain ValueType#INVERSE_DISTANCE}
     *
     * @param keyX x position of the key element
     * @param keyY y position of the key element (y coords increase downwards)
     * @param keyValue value of the key element
     *
     * @return a new instance of KernelJAI
     *
     */
    public static KernelJAI createRectangle(
            int width, int height, ValueType type, int keyX, int keyY, float keyValue) {

        if (width < 1) {
            throw new IllegalArgumentException("width must be >= 1");
        }

        if (height < 1) {
            throw new IllegalArgumentException("height must be >= 1");
        }

        if (!(keyX >= 0 && keyX < width) || !(keyY >= 0 && keyY < height)) {
            throw new IllegalArgumentException("key element position " + keyX + "," + keyY +
                    " is outside rectangle bounds");
        }
        
        KernelFactoryHelper kh = new KernelFactoryHelper();
        float weights[];

        if (type == ValueType.BINARY) {
            weights = kh.makeRect(width, height);
            weights[keyX + keyY*width] = keyValue;
            return new KernelJAI(width, height, keyX, keyY, weights);
        }

        weights = new float[width*height];

        float dist;
        int k = 0;
        Point2D p = new Point(keyX, keyY);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++, k++) {
                dist = (float) p.distance(x, y);
                if (type == ValueType.DISTANCE) {
                    weights[k] = dist;
                } else {
                    weights[k] = 1.0f / dist;
                }
            }
        }

        weights[keyX + keyY*width] = keyValue;
        
        return new KernelJAI(width, height, keyX, keyY, weights);
    }

    /**
     * Create a new KernelJAI object by rasterizing a shape. The shape must be a closed
     * polygon. The rasterizing process checks whether the centre of each pixel is inside
     * the polygon.
     * <p>
     * This method can cope with arbitrary shape bounds, ie. there is no need to
     * set the bounding rectangle to have origin x=0, y=0. The values of keyX and keyY,
     * which specify the position of the kernel's key element, must be within the
     * bounds of the shape as passed to this method, but do not need to be inside the
     * shape itself.
     *
     * @param shape an object representing a closed polygon
     * @param transform an optional AffineTransform to relate shape coordinates to
     * kernel element coordinates. May be null. This is useful to scale and/or rotate
     * the shape.
     *
     * @param type one of
     * {@linkplain ValueType#BINARY},
     * {@linkplain ValueType#DISTANCE} or
     * {@linkplain ValueType#INVERSE_DISTANCE}
     *
     * @param keyX the x coord of the key element
     * @param keyY the y coord of the key element
     * @param keyValue the value of the key element
     *
     * @return a new instance of KernelJAI
     */
    public static KernelJAI createFromShape(Shape shape, AffineTransform transform, ValueType type, int keyX, int keyY, float keyValue) {
        /*
         * First we transform the shape to a JTS Polygon object
         * so we can take advantage of the JTS intersects method
         * and avoid rasterizing artefacts which can arise when
         * using Shape.contains
         */
        PathIterator iter = shape.getPathIterator(transform, 0.05);
        float[] buf = new float[6];
        List<Coordinate> coords = CollectionFactory.list();
        while (!iter.isDone()) {
            iter.currentSegment(buf);
            coords.add(new Coordinate(buf[0], buf[1]));
            iter.next();
        }

        GeometryFactory gf = new GeometryFactory();
        Coordinate[] coordsAr = coords.toArray(new Coordinate[coords.size()]);
        Geometry poly = gf.createPolygon(gf.createLinearRing(coordsAr), null);

        Envelope env = poly.getEnvelopeInternal();

        int left = (int) Math.floor(env.getMinX());
        int right = (int) Math.ceil(env.getMaxX());
        int top = (int) Math.ceil(env.getMaxY());
        int bottom = (int) Math.floor(env.getMinY());

        int width = right - left + 1;
        int height = top - bottom + 1;

        float[] weights = new float[width * height];
        int[] offset = new int[height];
        for (int i = 0, o=0; i < offset.length; i++, o+=width) offset[i] = o;

        coords.clear();
        double y = top;
        for (int iy = 0; iy < height; y--, iy++) {
            double x = left;
            for (int ix = 0; ix < width; x++, ix++) {
                coords.add(new Coordinate(x, y));
            }
        }

        /*
         * Now we buffer the polygon by a small amount to avoid
         * rejection points that lie exactly on the boundary
         * and the points that intersect with the poly
         */
        MultiPoint mp = gf.createMultiPoint(coords.toArray(new Coordinate[coords.size()]));
        Geometry inside = mp.intersection(poly.buffer(0.05, Math.max(width/2, 10)));

        /*
         * For each intersecting point we set a kernel element
         */
        int n = inside.getNumGeometries();
        for (int i = 0; i < n; i++) {
            Geometry g = inside.getGeometryN(i);
            Coordinate c = g.getCoordinate();
            int index = (int) c.x - left + offset[(int) c.y - bottom];

            if (type == ValueType.BINARY) {
                weights[index] = 1.0f;
            } else if (type == ValueType.DISTANCE) {
                weights[index] = (float) Point2D.distance(keyX, keyY, (int)c.x, (int)c.y);
            } else if (type == ValueType.INVERSE_DISTANCE) {
                weights[index] = 1.0f / (float) Point2D.distance(keyX, keyY, (int)c.x, (int)c.y);
            }
        }

        // set the key element to the requested value
        weights[keyX + offset[keyY]] = keyValue;

        return new KernelJAI(width, height, keyX, keyY, weights);
    }
}
