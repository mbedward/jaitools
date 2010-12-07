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
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import java.lang.ref.WeakReference;
import java.util.HashMap;
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
 */
public class PolygonSmoother {
    private GeometryFactory geomFactory;
    
    public PolygonSmoother() {
        this.geomFactory = new GeometryFactory();
    }

    public PolygonSmoother(GeometryFactory gf) {
        this.geomFactory = gf;
    }
    
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
    Map<Integer, WeakReference<InterpPoint[]>> lookup = 
            new HashMap<Integer, WeakReference<InterpPoint[]>>();

    /**
     * Get the interpolation parameters for a Bezier curve approximated
     * by the given number of vertices. 
     * 
     * @param npoints number of vertices
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
     * @param coords vertex coordinates
     * @param N number of coordinates in {@coords} to use
     * @param alpha tightness of fit
     * 
     * @return array of {@code Coordinates} for positions of control points
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
        int Nvertices = coords.length - 1;  // first coord == last coord
        
        Coordinate[][] controlPoints = getControlPoints(coords, Nvertices, alpha);
        
        Coordinate[] smoothCoords = new Coordinate[Nvertices * pointsPerSegment];
        for (int i = 0, k = 0; i < Nvertices; i++) {
            int next = (i + 1) % Nvertices;
            
            Coordinate[] segment = cubicBezier(
                    coords[i], coords[next],
                    controlPoints[i][1], controlPoints[next][0],
                    pointsPerSegment);
            
            for (int j = 0; j < pointsPerSegment; j++, k++) {
                smoothCoords[k] = segment[j];
            }
        }
        
        LinearRing shell = geomFactory.createLinearRing(smoothCoords);
        return geomFactory.createPolygon(shell, null);
    }
}
