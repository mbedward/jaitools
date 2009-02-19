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

package jaitools.jiffle.parser;

/**
 * Double value comparisons within a set tolerance
 * 
 * @author Michael Bedward
 */
public class DoubleComparison {

    private static final double TOL = 1.0e-8;

    public static boolean dzero(double x) {
        return Math.abs(x) < TOL;
    }

    public static int dcomp(double x1, double x2) {
        if (dzero(x1 - x2)) {
            return 0;
        } else {
            return Double.compare(x1, x2);
        }
    }
}
