/*
 * Copyright 2011 Michael Bedward
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

package jaitools.demo;

/**
 * Constants to identify Jiffle scripts used to create example
 * images for JAI-tools demo applications.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public enum ImageChoice {

    /**
     * Chessboard pattern with 0 and 1 values.
     */
    CHESSBOARD("chessboard", "result"), 
    /**
     * Complex interference pattern.
     */
    INTERFERENCE("interference", "result"), 
    /**
     * Binary image of the Mandelbrot set.
     */
    MANDELBROT("mandelbrot", "result"),
    /**
     * Concentric, sinusoidal ripples.
     */
    RIPPLES("ripple", "result"), 
    /**
     * Sort of a square circle thing.
     */
    SQUIRCLE("squircle", "result");

    private String name;
    private String destImageVarName;

    private ImageChoice(String name, String destImageVarName) {
        this.name = name;
        this.destImageVarName = destImageVarName;
    }
    
    @Override
    public String toString() {
        return name;
    }
    
    public String getDestImageVarName() {
        return destImageVarName;
    }
}
