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
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for Bezier smoothing.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public abstract class AbstractSmoother {
    
    /**
     * Default smoothing control. Specifies no minimum 
     * vertex distance and a constant number of points
     * per smoothed segment.
     */
    protected SmootherControl DEFAULT_CONTROL = new SmootherControl() {
        public double getMinLength() {
            return 0.0;
        }
        
        public int getNumVertices(double length) {
            return 10;
        }
    };
    
    protected SmootherControl control;;
    
    protected final GeometryFactory geomFactory;

    /**
     * Class to hold interpolation parameters for a given point.
     */
    protected static final class InterpPoint {
        double[] t = new double[4];
        double tsum;
    }
    
    /**
     * Cache of previously calculated interpolation parameters
     */
    protected Map<Integer, WeakReference<InterpPoint[]>> lookup = 
            new HashMap<Integer, WeakReference<InterpPoint[]>>();

    /**
     * Constructor.
     * 
     * @param geomFactory an instance of {@code GeometryFactory} to use when
     *        creating smoothed {@code Geometry} objects
     * 
     * @throws IllegalArgumentException if {@code geomFactory} is {@code null}
     */
    public AbstractSmoother(GeometryFactory geomFactory) {
        if (geomFactory == null) {
            throw new IllegalArgumentException("geomFactory must not be null");
        }
        this.geomFactory = geomFactory;
        
        this.control = DEFAULT_CONTROL;
    }

    /**
     * Set a new {@code Control} object to customize smoothing
     * behaviour.
     * 
     * @param control the control to use for smoothing; if {@code null} the
     *        default control will be set
     */
    public void setControl(SmootherControl control) {
        this.control = control == null ? DEFAULT_CONTROL : control;
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
    protected Coordinate[] cubicBezier(final Coordinate start, final Coordinate end,
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
     * Get the interpolation parameters for a Bezier curve approximated
     * by the given number of vertices. 
     * 
     * @param npoints number of vertices
     * 
     * @return array of {@code InterpPoint} objects holding the parameter values
     */
    protected InterpPoint[] getInterpPoints(int npoints) {
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
    
}
