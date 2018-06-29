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

package org.jaitools.jts;

import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.LineString;
import org.locationtech.jts.geom.LinearRing;
import org.locationtech.jts.geom.Polygon;
import org.locationtech.jts.geom.PrecisionModel;

import org.jaitools.numeric.CompareOp;

/**
 * A helper class with methods to work with JTS geometry objects.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class Utils {

    private static GeometryFactory geomFactory;

    private Utils() {
    }
    
    /**
     * Gets the cached {@code GeometryFactory} instance.
     * 
     * @return the geometry factory
     */
    public static GeometryFactory getGeometryFactory() {
        if (geomFactory == null) {
            geomFactory = new GeometryFactory();
        }
        return geomFactory;
    }
    
    /**
     * Sets the precision to be used by the cached {@code GeometryFactory}.
     * For an explanation of the scale factor see {@link PrecisionModel}.
     * 
     * @param scale the desired precision expressed as a scale factor
     * 
     * @return the (possibly new) cached {@code GeometryFactory} instance
     */
    public static GeometryFactory setPrecision(double scale) {
        geomFactory = new GeometryFactory(new PrecisionModel(scale));
        return geomFactory;
    }

    /**
     * Removes collinear points from the provided linestring.
     * 
     * @param ls the {@link LineString} to be simplified.
     * @return a new version of the provided {@link LineString} with collinear points removed.
     */
    public static LineString removeCollinearVertices(final LineString ls) {
        if (ls == null) {
            throw new NullPointerException("The provided linestring is null");
        }
        final int N = ls.getNumPoints();
        final boolean isLinearRing = ls instanceof LinearRing;

        List<Coordinate> retain = new ArrayList<Coordinate>();
        retain.add(ls.getCoordinateN(0));

        int i0 = 0, i1 = 1, i2 = 2;
        Coordinate firstCoord = ls.getCoordinateN(i0);
        Coordinate midCoord;
        Coordinate lastCoord;
        while (i2 < N) {

            midCoord = ls.getCoordinateN(i1);
            lastCoord = ls.getCoordinateN(i2);

            // perform a redundancy check based on 
            double dx1 = midCoord.x - firstCoord.x;
            double dy1 = midCoord.y - firstCoord.y;
            double dx2 = lastCoord.x - midCoord.x;
            double dy2 = lastCoord.y - midCoord.y;

            boolean redundant = false;
            if (CompareOp.isZero(dx1)) {
                if (CompareOp.isZero(dx2) && Math.signum(dy1) == Math.signum(dy2)) {
                    redundant = true;
                }
            } else {
                if (!CompareOp.isZero(dx2)) {
                    if (CompareOp.aequal(dy1 / dx1, dy2 / dx2) && 
                            Math.signum(dx1) == Math.signum(dx2)) {
                        redundant = true;
                    }
                }
            }


            // Add only if not redundant
            if (!redundant) {
                // add midcoord and change head
                retain.add(midCoord);
                i0 = i1;
                firstCoord = ls.getCoordinateN(i0);
            }
            i1++;
            i2++;
        }
        retain.add(ls.getCoordinateN(N - 1));

        //
        // Return value
        //
        final int size = retain.size();
        // nothing changed?
        if (size == N) {
            // free everything and return original
            retain.clear();
            return ls;
        }
        return isLinearRing
                ? ls.getFactory().createLinearRing(retain.toArray(new Coordinate[size]))
                : ls.getFactory().createLineString(retain.toArray(new Coordinate[size]));
    }

    /**
     * Removes collinear vertices from the provided {@link Polygon}.
     * @param polygon the instance of a {@link Polygon} to remove collinear vertices from.
     * @return a new instance of the provided  {@link Polygon} without collinear vertices.
     */
    public static Polygon removeCollinearVertices(final Polygon polygon) {
        if (polygon == null) {
            throw new NullPointerException("The provided Polygon is null");
        }
        // reuse existing factory		
        final GeometryFactory gf = polygon.getFactory();

        // work on the exterior ring
        LineString exterior = polygon.getExteriorRing();
        LineString shell = removeCollinearVertices(exterior);
        if (shell == null || shell.isEmpty()) {
            return null;
        }

        // work on the holes
        List<LineString> holes = new ArrayList<LineString>();
        final int size = polygon.getNumInteriorRing();
        for (int i = 0; i < size; i++) {
            LineString hole = polygon.getInteriorRingN(i);
            hole = removeCollinearVertices(hole);
            if (hole != null && !hole.isEmpty()) {
                holes.add(hole);
            }
        }

        return gf.createPolygon((LinearRing) shell, (LinearRing[]) holes.toArray(new LinearRing[holes.size()]));
    }

    /**
     * Removes collinear vertices from the provided {@link Geometry}.
     * 
     * <p>
     * For the moment this implementation only accepts, {@link Polygon}, {@link LinearRing} and {@link LineString}.
     * It return <code>null</code> in case the geometry is not of these types. 
     * 
     * @todo implement submethods for {@link GeometryCollection} sublcaases.
     * @param g the instance of a {@link Geometry} to remove collinear vertices from.
     * @return a new instance of the provided  {@link Geometry} without collinear vertices.
     */
    public static Geometry removeCollinearVertices(final Geometry g) {
        if (g == null) {
            throw new NullPointerException("The provided Geometry is null");
        }
        if (g instanceof LineString) {
            return removeCollinearVertices((LineString) g);
        }
        if (g instanceof Polygon) {
            return removeCollinearVertices((Polygon) g);
        }

        throw new IllegalArgumentException("This method can work on LineString and Polygon.");
    }
}
