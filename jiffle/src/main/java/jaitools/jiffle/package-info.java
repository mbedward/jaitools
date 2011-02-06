/*
 * Copyright 2009-2011 Michael Bedward
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

/**
 * Jiffle scripting language.
 * <p>
 * Jiffle is a language for raster algebra. Using it you can create images from
 * mathematical and logical expressions which may, in turn, use data from other
 * images. The main intention of Jiffle is to free you from having to write lots
 * of Java and JAI boiler-plate code. Jiffle also allows much more flexibility
 * that is possible with JAI's ImageFunction.
 * <p>
 * Jiffle is a superset of the r.mapcalc language used for raster algebra in
 * GRASS GIS. Although there are minor differences between the two languages,
 * including the form of variable names allowable, many r.mapcalc scripts should
 * run under Jiffle with little or no changes required.
 * <p>
 * Jiffle adds additional features to those offered in r.mapcalc including more
 * statistical functions, choice of scope for variables, and syntax to access
 * specified bands and pixels in input images.
 * <p>
 * Jiffle scripts are compiled to bytecode. The compiler first
 * translates the script into Java source which is then passed to Jiffle's
 * embedded Janino compiler to produce executable bytecode in memory. The
 * resulting run-time instance can then be used by client code as a normal
 * Java object.
 */

package jaitools.jiffle;
