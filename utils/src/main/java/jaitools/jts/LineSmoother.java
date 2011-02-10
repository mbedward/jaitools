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
import java.util.ArrayList;
import java.util.List;


/**
 * Line smoothing by interpolation with cubic Bazier curves.
 * This code adapts an algorithm which was devised by Maxim Shemanarev
 * for polygons and described by him at:
 * <a href="http://www.antigrain.com/research/bezier_interpolation/index.html">
 * http://www.antigrain.com/research/bezier_interpolation/index.html</a>.
 * <p>
 * To use the same algorithm for {@code LineStrings} we add a dummy vertex to
 * either end, calculate control points for just the real vertices.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class LineSmoother extends AbstractSmoother {

    /**
     * Default constructor.
     */
    public LineSmoother() {
        super(new GeometryFactory());
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
        super(geomFactory);
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
    private Coordinate[][] getControlPoints(Coordinate[] coords, double alpha) {
        if (alpha < 0.0 || alpha > 1.0) {
            throw new IllegalArgumentException("alpha must be a value between 0 and 1 inclusive");
        }

        final int N = coords.length;
        Coordinate[][] ctrl = new Coordinate[N][2];

        Coordinate[] v = new Coordinate[3];

        Coordinate[] mid = new Coordinate[2];
        mid[0] = new Coordinate();
        mid[1] = new Coordinate();

        Coordinate anchor = new Coordinate();
        double[] vdist = new double[2];
        double mdist;

        // Start with dummy coordinate preceding first real coordinate
        v[1] = new Coordinate(
                2 * coords[0].x - coords[1].x, 2 * coords[0].y - coords[1].y);
        v[2] = coords[0];
        
        // Dummy coordinate for end of line
        Coordinate vN = new Coordinate(
                2 * coords[N-1].x - coords[N-2].x, 
                2 * coords[N-1].y - coords[N-2].y);
        
        mid[1].x = (v[1].x + v[2].x) / 2.0;
        mid[1].y = (v[1].y + v[2].y) / 2.0;
        vdist[1] = v[1].distance(v[2]);

        for (int i = 0; i < N; i++) {
            v[0] = v[1];
            v[1] = v[2];
            v[2] = (i < N - 1 ? coords[i + 1] : vN);

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
        
        Coordinate[][] controlPoints = getControlPoints(coords, alpha);
        
        final int N = coords.length;
        List<Coordinate> smoothCoords = new ArrayList<Coordinate>();
        double dist;
        for (int i = 0; i < N - 1; i++) {
            dist = coords[i].distance(coords[i+1]);
            if (dist < control.getMinLength()) {
                // segment too short - just copy input coordinate
                smoothCoords.add(new Coordinate(coords[i]));
                
            } else {
                int smoothN = control.getNumVertices(dist);
                Coordinate[] segment = cubicBezier(
                        coords[i], coords[i+1],
                        controlPoints[i][1], controlPoints[i+1][0],
                        smoothN);
            
                int copyN = i < N - 1 ? segment.length - 1 : segment.length;
                for (int k = 0; k < copyN; k++) {
                    smoothCoords.add(segment[k]);
                }
            }
        }
        smoothCoords.add(coords[N - 1]);

        return geomFactory.createLineString(smoothCoords.toArray(new Coordinate[0]));
    }
    
}
