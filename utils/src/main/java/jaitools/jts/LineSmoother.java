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

package jaitools.jts;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Polygon smoothing by interpolation with cubic Bazier curves.
 * This code implements an algorithm which was devised by Maxim Shemanarev
 * and described by him at:
 * <a href="http://www.antigrain.com/research/bezier_interpolation/index.html">
 * http://www.antigrain.com/research/bezier_interpolation/index.html</a>.
 * 
 * Note: the code here is <b>not</b> that written by Maxim to accompany his
 * algorithm description. Rather, it is an original implementation and any
 * errors are my fault.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class LineSmoother {

    /**
     * Defines methods to control the smoothing process.
     * {@code LineSmoother} has a default implementation
     * that specifies a constant number of vertices in smoothed
     * segments and no lower bound on the distance between
     * input vertices for smoothing.
     * <p>
     * To customize smoothing, pass your own implementation
     * to {@link LineSmoother#setControl(jaitools.jts.LineSmoother.Control) }.
     */
    public static interface Control {
        /**
         * Gets the minimum distance between input vertices
         * for the segment to be smoothed. Segments smaller
         * than this will be copied to the output unchanged.
         * 
         * @return minimum segment length for smoothing
         */
        double getMinLength();
        
        /**
         * Given an input segment length, returns the number
         * of vertices to use for the smoothed segment. This 
         * number includes the segment end-points.
         * 
         * @param length input segment length
         * 
         * @return number of vertices in the smoothed segment
         *         including the end-points
         */
        int getNumVertices(double length);
    }
    
    /**
     * Default smoothing control. Specifies no minimum 
     * vertex distance and a constant number of points
     * per smoothed segment.
     */
    private Control DEFAULT_CONTROL = new Control() {
        public double getMinLength() {
            return 0.0;
        }
        
        public int getNumVertices(double length) {
            return 10;
        }
    };
    
    private Control control = DEFAULT_CONTROL;
    
    private GeometryFactory geomFactory;
    

    /**
     * Class to hold interpolation parameters for a given point.
     */
    private static final class InterpPoint {
        double[] t = new double[4];
        double tsum;
    }
    
    /**
     * Cache of previously calculated interpolation parameters
     */
    private Map<Integer, WeakReference<InterpPoint[]>> lookup = 
            new HashMap<Integer, WeakReference<InterpPoint[]>>();

    /**
     * Default constructor.
     */
    public LineSmoother() {
    }

    /**
     * Constructor.
     * 
     * @param geomFactory an instance of {@code GeometryFactory} to use when
     *        creating smoothed {@code Geometry} objects
     * 
     * @throws IllegalArgumentException if {@code geomFactory} is {@code null}
     */
    public LineSmoother(GeometryFactory geomFactory) {
        if (geomFactory == null) {
            throw new IllegalArgumentException("geomFactory must not be null");
        }
        this.geomFactory = geomFactory;
    }
    
    /**
     * Set a new {@code Control} object to customize smoothing
     * behaviour.
     * 
     * @param control the control to use for smoothing
     * 
     * @throws IllegalArgumentException if {@code control} is {@code null}
     */
    public void setControl(Control control) {
        if (control == null) {
            throw new IllegalArgumentException("control must not be null");
        }
        
        this.control = control;
    }

    /**
     * Get the interpolation parameters for a Bezier curve approximated
     * by the given number of vertices. 
     * 
     * @param npoints number of vertices
     * 
     * @return array of {@code InterpPoint} objects holding the parameter values
     */
    private InterpPoint[] getInterpPoints(int npoints) {
        WeakReference<InterpPoint[]> ref = lookup.get(npoints);
        InterpPoint[] ip = null;
        if (ref != null) ip = ref.get();
        
        if (ip == null) {
            ip = new InterpPoint[npoints];
        
            for (int i = 0; i < npoints; i++) {
                double t = (double) i / (npoints - 1);
                double tc = 1.0 - t;

                ip[i] = new InterpPoint();
                ip[i].t[0] = tc*tc*tc;
                ip[i].t[1] = 3.0*tc*tc*t;
                ip[i].t[2] = 3.0*tc*t*t;
                ip[i].t[3] = t*t*t;
                ip[i].tsum = ip[i].t[0] + ip[i].t[1] + ip[i].t[2] + ip[i].t[3];
            }
            
            lookup.put(npoints, new WeakReference<InterpPoint[]>(ip));
        }
        
        return ip;
    }
    
    /**
     * Calculates a pair of Bezier control points for each vertex in an
     * array of {@code Coordinates}.
     * 
     * @param coords input vertices
     * @param N number of coordinates in {@coords} to use
     * @param alpha tightness of fit
     * 
     * @return 2D array of {@code Coordinates} for positions of each pair of
     *         control points per input vertex
     */
    private Coordinate[][] getControlPoints(Coordinate[] coords, int N, double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("alpha must be a value between 0 and 1 inclusive");
        }

        Coordinate[][] ctrl = new Coordinate[N][2];

        Coordinate[] v = new Coordinate[3];

        Coordinate[] mid = new Coordinate[2];
        mid[0] = new Coordinate();
        mid[1] = new Coordinate();

        Coordinate anchor = new Coordinate();
        double[] vdist = new double[2];
        double mdist;

        v[1] = coords[N - 1];
        v[2] = coords[0];
        mid[1].x = (v[1].x + v[2].x) / 2.0;
        mid[1].y = (v[1].y + v[2].y) / 2.0;
        vdist[1] = v[1].distance(v[2]);

        for (int i = 0; i < N; i++) {
            v[0] = v[1];
            v[1] = v[2];
            v[2] = coords[(i + 1) % N];

            mid[0].x = mid[1].x;
            mid[0].y = mid[1].y;
            mid[1].x = (v[1].x + v[2].x) / 2.0;
            mid[1].y = (v[1].y + v[2].y) / 2.0;

            vdist[0] = vdist[1];
            vdist[1] = v[1].distance(v[2]);

            double p = vdist[0] / (vdist[0] + vdist[1]);
            anchor.x = mid[0].x + p * (mid[1].x - mid[0].x);
            anchor.y = mid[0].y + p * (mid[1].y - mid[0].y);

            double xdelta = anchor.x - v[1].x;
            double ydelta = anchor.y - v[1].y;

            ctrl[i][0] = new Coordinate(
                    alpha*(v[1].x - mid[0].x + xdelta) + mid[0].x - xdelta,
                    alpha*(v[1].y - mid[0].y + ydelta) + mid[0].y - ydelta);

            ctrl[i][1] = new Coordinate(
                    alpha*(v[1].x - mid[1].x + xdelta) + mid[1].x - xdelta,
                    alpha*(v[1].y - mid[1].y + ydelta) + mid[1].y - ydelta);
        }

        return ctrl;
    }
    
    
    /**
     * Calculates vertices along a cubic Bazier curve given start point, end point
     * and two control points.
     * 
     * @param start start position
     * @param end end position
     * @param ctrl1 first control point
     * @param ctrl2 second control point
     * @param nv number of vertices including the start and end points
     * 
     * @return vertices along the Bezier curve
     */
    private Coordinate[] cubicBezier(final Coordinate start, final Coordinate end,
            final Coordinate ctrl1, final Coordinate ctrl2, final int nv) {
                
        final Coordinate[] curve = new Coordinate[nv];
        
        final Coordinate[] buf = new Coordinate[3];
        for (int i = 0; i < buf.length; i++) {
            buf[i] = new Coordinate();
        }
        
        curve[0] = new Coordinate(start);
        curve[nv - 1] = new Coordinate(end);
        InterpPoint[] ip = getInterpPoints(nv);

        for (int i = 1; i < nv-1; i++) {
            Coordinate c = new Coordinate();

            c.x = ip[i].t[0]*start.x + ip[i].t[1]*ctrl1.x + ip[i].t[2]*ctrl2.x + ip[i].t[3]*end.x;
            c.x /= ip[i].tsum;
            c.y = ip[i].t[0]*start.y + ip[i].t[1]*ctrl1.y + ip[i].t[2]*ctrl2.y + ip[i].t[3]*end.y;
            c.y /= ip[i].tsum;

            curve[i] = c;
        }

        return curve;
    }
    
    /**
     * Helper method for the public {@code smooth} methods. Smooths
     * segments defined by vertices in {@code coords}.
     * 
     * @param coords input vertices
     * @param N number of input vertices to consider
     * @param controlPoints array of control points where first dimension
     *        is same length as {@code coords} and second dimension is length 2
     * 
     * @return vertices of smoothed segments
     */
    private Coordinate[] getSmoothCoordinates(Coordinate[] coords, 
            int N, 
            Coordinate[][] controlPoints) {
                
        List<Coordinate> smoothCoords = new ArrayList<Coordinate>();
        double dist;
        for (int i = 0; i < N; i++) {
            int next = (i + 1) % N;
            
            dist = coords[i].distance(coords[next]);
            if (dist < control.getMinLength()) {
                // segment too short - just copy input coordinate
                smoothCoords.add(new Coordinate(coords[i]));
                
            } else {
                int smoothN = control.getNumVertices(dist);
                Coordinate[] segment = cubicBezier(
                        coords[i], coords[next],
                        controlPoints[i][1], controlPoints[next][0],
                        smoothN);
            
                int copyN = i < N - 1 ? segment.length - 1 : segment.length;
                for (int k = 0; k < copyN; k++) {
                    smoothCoords.add(segment[k]);
                }
            }
        }

        return smoothCoords.toArray(new Coordinate[0]);
    }
    
    /**
     * Creates a new {@code Polygon} whose exterior shell is a smoothed
     * version of the input {@code Polygon}.
     * <p>
     * Note: this method presently ignores holes.
     * 
     * @param p the input {@code Polygon}
     * 
     * @param alpha a value between 0 and 1 (inclusive) specifying the tightness
     *        of fit of the smoothed boundary (0 is loose)
     * 
     * @param pointsPerSegment the number of vertices to use to represent each
     *        Bezier curve derived from an edge of the input {@code Polygon}
     * 
     * @return the smoothed {@code Polygon}
     */
    public Polygon smooth(Polygon p, double alpha, int pointsPerSegment) {
        Coordinate[] coords = p.getExteriorRing().getCoordinates();
        final int N = coords.length - 1;  // first coord == last coord
        
        Coordinate[][] controlPoints = getControlPoints(coords, N, alpha);
        Coordinate[] smoothCoords = getSmoothCoordinates(coords, N, controlPoints);
        
        LinearRing shell = geomFactory.createLinearRing(smoothCoords);
        return geomFactory.createPolygon(shell, null);
    }
    
    /**
     * Creates a new {@code LineString} which is a smoothed version of 
     * the input {@code LineString}.
     * 
     * @param ls the input {@code LineString}
     * 
     * @param alpha a value between 0 and 1 (inclusive) specifying the tightness
     *        of fit of the smoothed boundary (0 is loose)
     * 
     * @return the smoothed {@code LineString}
     */
    public LineString smooth(LineString ls, double alpha) {
        Coordinate[] coords = ls.getCoordinates();
        
        Coordinate[][] controlPoints = getControlPoints(coords, coords.length, alpha);
        Coordinate[] smoothCoords = getSmoothCoordinates(coords, coords.length, controlPoints);
        
        return geomFactory.createLineString(smoothCoords);
    }
    
    public Coordinate[] smooth(Coordinate[] coords, double alpha, int pointsPerSegment) {
        Coordinate[][] controlPoints = getControlPoints(coords, coords.length, alpha);
        return getSmoothCoordinates(coords, coords.length, controlPoints);
    }
}
