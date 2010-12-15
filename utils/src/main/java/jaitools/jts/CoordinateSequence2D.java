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
import com.vividsolutions.jts.geom.CoordinateSequence;
import com.vividsolutions.jts.geom.Envelope;

/**
 * A lightweight implementation of JTS {@code CoordinateSequence} for 2D points.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class CoordinateSequence2D implements CoordinateSequence {

    private final double[] x;
    private final double[] y;
    private double minx, miny, maxx, maxy;

    /**
     * Creates a new {@code CoordinateSequence2D} object with the given 
     * size.
     * 
     * @param n capacity (number of coordinates)
     */
    public CoordinateSequence2D(int n) {
        x = new double[n];
        y = new double[n];
    }

    /**
     * Creates a new {@code CoordinateSequence2D} object from a sequence of
     * (x,y) pairs.
     * <pre><code>
     * // Example: create an object with 3 coordinates specified
     * // as xy pairs
     * CoordinateSequence cs = new CoordinateSequence2D(1.1, 1.2, 2.1, 2.2, 3.1, 3.2);
     * </code></pre>
     * @param xy x and y values ordered as {@code x0, y0, x1, y1...}; 
     *        if {@code null} an empty object is created
     * 
     * @throws IllegalArgumentException if the number of values in {@code xy} is
     *         greater than 0 but not even
     */
    public CoordinateSequence2D(double... xy) {
        if (xy == null) {
            x = new double[0];
            y = new double[0];
        } else {
            if (xy.length % 2 != 0) {
                throw new IllegalArgumentException("xy must have an even number of values");
            }

            x = new double[xy.length / 2];
            y = new double[xy.length / 2];
            minx = maxx = xy[0];
            miny = maxy = xy[1];
            
            for (int i = 0, k = 0; k < xy.length; i++, k += 2) {
                x[i] = xy[k];
                y[i] = xy[k + 1];
                
                if (x[i] < minx) {
                    minx = x[i];
                } else if (x[i] > maxx) {
                    maxx = x[i];
                }
                
                if (y[i] < miny) {
                    miny = y[i];
                } else if (y[i] > maxy) {
                    maxy = y[i];
                }
            }
        }
    }

    /**
     * Gets the dimension of points stored by this {@code CoordinateSequence2D}.
     * 
     * @return always returns 2
     */
    public int getDimension() {
        return 2;
    }

    /**
     * Gets coordinate values at the specified index.
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @return a new {@code Coordinate} object
     */
    public Coordinate getCoordinate(int index) {
        return new Coordinate(x[index], y[index]);
    }

    /**
     * Equivalent to {@link #getCoordinate(int)}.
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @return a new {@code Coordinate} object
     */
    public Coordinate getCoordinateCopy(int index) {
        return getCoordinate(index);
    }

    /**
     * Copies the requested coordinate in the sequence to the supplied
     * {@code Coordinate} object. 
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @param coord the destination object; ff {@code null} a new
     *        {@code Coordinate} will e created.
     */
    public void getCoordinate(int index, Coordinate coord) {
        if (coord == null) {
            coord = new Coordinate();
        }
        
        coord.x = x[index];
        coord.y = y[index];
    }

    /**
     * {@inheritDoc}
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     */
    public double getX(int index) {
        return x[index];
    }

    /**
     * {@inheritDoc}
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     */
    public double getY(int index) {
        return y[index];
    }

    /**
     * Returns the ordinate of a coordinate in this sequence.
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @param ordinateIndex 0 for the X ordinate or 1 for the Y ordinate
     * 
     * @return the ordinate value
     * 
     * @throws IllegalArgumentException if {@code ordinateIndex} is not
     *         either 0 or 1
     */
    public double getOrdinate(int index, int ordinateIndex) {
        switch (ordinateIndex) {
            case 0:
                return x[index];

            case 1:
                return y[index];

            default:
                throw new IllegalArgumentException("invalid ordinate index: " + ordinateIndex);
        }
    }

    /**
     * {@inheritDoc}
     */
    public int size() {
        return x.length;
    }

    /**
     * Sets the ordinate of a coordinate in this sequence.
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @param ordinateIndex 0 for the X ordinate or 1 for the Y ordinate
     * 
     * @param value the new ordinate value
     * 
     * @throws IllegalArgumentException if {@code ordinateIndex} is not
     *         either 0 or 1
     */
    public void setOrdinate(int index, int ordinateIndex, double value) {
        switch (ordinateIndex) {
            case 0:
                x[index] = value;
                if (value < minx) {
                    minx = value;
                } else if (value > maxx) {
                    maxx = value;
                }
                break;

            case 1:
                y[index] = value;
                if (value < miny) {
                    miny = value;
                } else if (value > maxy) {
                    maxy = value;
                }
                break;

            default:
                throw new IllegalArgumentException("invalid ordinate index: " + ordinateIndex);
        }
    }

    /**
     * Sets the X ordinate of the point at the given index.
     * Equivalent to {@link #setOrdinate}(index, 0, value).
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @param value the new value
     */
    public void setX(int index, double value) {
        setOrdinate(index, 0, value);
    }

    /**
     * Sets the Y ordinate of the point at the given index.
     * Equivalent to {@link #setOrdinate}(index, 1, value).
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @param value the new value
     */
    public void setY(int index, double value) {
        setOrdinate(index, 1, value);
    }
    
    /**
     * Sets the coordinate at the given index.
     * 
     * @param index an index &ge;0 and &lt; {@link #size()}
     * 
     * @param x the new X ordinate value
     * @param y the new Y ordinate value
     */
    public void setXY(int index, double x, double y) {
        setOrdinate(index, 0, x);
        setOrdinate(index, 1, y);
    }
    /**
     * Returns an array of new {@code Coordinate} objects for the point values
     * in this sequence. 
     * 
     * @return array of coordinates
     */
    public Coordinate[] toCoordinateArray() {
        Coordinate[] coords = new Coordinate[x.length];

        for (int i = 0; i < x.length; i++) {
            coords[i] = new Coordinate(x[i], y[i]);
        }

        return coords;
    }

    /**
     * Returns an envelope which contains {@code env} and all points
     * in this sequence. If {@code env} contains all points it is 
     * returned; otherwise a new {@code Envelope} is created based on
     * {@code env} and then expanded as necessary before being returned.
     * 
     * @param env the test envelope
     * 
     * @return an envelope that includes both {@code env} and all points
     *         in this sequence
     */
    public Envelope expandEnvelope(Envelope env) {
        Envelope exp = null;
        
        if (!env.contains(minx, miny)) {
            if (exp == null) exp = new Envelope(env);
            exp.expandToInclude(minx, miny);
        }

        if (!env.contains(maxx, maxy)) {
            if (exp == null) exp = new Envelope(env);
            exp.expandToInclude(maxx, maxy);
        }

        return (exp == null ? env : exp);
    }

    /**
     * Creates a deep copy of this sequence.
     * 
     * @return a new sequence with values 
     */
    @Override
    public Object clone() {
        CoordinateSequence2D copy = new CoordinateSequence2D(x.length);
        for (int i = 0; i < x.length; i++) {
            copy.x[i] = x[i];
            copy.y[i] = y[i];
        }
        copy.minx = minx;
        copy.miny = miny;
        copy.maxx = maxx;
        copy.maxy = maxy;
        
        return copy;
    }
    
}
