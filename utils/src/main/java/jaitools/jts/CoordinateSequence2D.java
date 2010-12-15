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

    private double[] x;
    private double[] y;

    public CoordinateSequence2D(int n) {
        x = new double[n];
        y = new double[n];
    }

    public CoordinateSequence2D(double... xy) {
        if (xy != null) {
            if (xy.length % 2 != 0) {
                throw new IllegalArgumentException("xy must have an even number of values");
            }

            x = new double[xy.length / 2];
            y = new double[xy.length / 2];
            for (int i = 0, k = 0; k < xy.length; i++, k += 2) {
                x[i] = xy[k];
                y[i] = xy[k + 1];
            }
        }
    }

    public int getDimension() {
        return 2;
    }

    public Coordinate getCoordinate(int index) {
        return new Coordinate(x[index], y[index]);
    }

    public Coordinate getCoordinateCopy(int index) {
        return getCoordinate(index);
    }

    public void getCoordinate(int index, Coordinate coord) {
        coord.x = x[index];
        coord.y = y[index];
    }

    public double getX(int index) {
        return x[index];
    }

    public double getY(int index) {
        return y[index];
    }

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

    public int size() {
        return x.length;
    }

    public void setOrdinate(int index, int ordinateIndex, double value) {
        switch (ordinateIndex) {
            case 0:
                x[index] = value;
                break;

            case 1:
                y[index] = value;
                break;

            default:
                throw new IllegalArgumentException("invalid ordinate index: " + ordinateIndex);
        }
    }

    public Coordinate[] toCoordinateArray() {
        Coordinate[] coords = new Coordinate[x.length];

        for (int i = 0; i < x.length; i++) {
            coords[i] = new Coordinate(x[i], y[i]);
        }

        return coords;
    }

    public Envelope expandEnvelope(Envelope env) {
        Envelope exp = null;
        for (int i = 0; i < x.length; i++) {
            if (!env.contains(x[i], y[i])) {
                if (exp == null) {
                    exp = new Envelope(env);
                }
                exp.expandToInclude(x[i], y[i]);
            }
        }

        return (exp == null ? env : exp);
    }

    @Override
    public Object clone() {
        CoordinateSequence2D copy = new CoordinateSequence2D(x.length);
        for (int i = 0; i < x.length; i++) {
            copy.x[i] = x[i];
            copy.y[i] = y[i];
        }
        return copy;
    }
    
    public void setX(int index, double value) {
        setOrdinate(index, 0, value);
    }

    public void setY(int index, double value) {
        setOrdinate(index, 1, value);
    }
    
    public void setXY(int index, double x, double y) {
        setOrdinate(index, 0, x);
        setOrdinate(index, 1, y);
    }
}
