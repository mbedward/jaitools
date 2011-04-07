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

/**
 * The VMCAFill image operation fills areas of an image by iteratively
 * sampling surrounding values using a Voter Model Cellular Automaton. 
 * It is based on the observation that many natural patterns display fractal 
 * or approximately fractal properties, and so gaps in images of natural patterns 
 * can effectively be filled by using fractal algorithms. The objective is a fill 
 * pattern that displays acceptable statistical resemblance to adjacent image 
 * areas. 
 * <p>
 * Sprott demonstrated that these algorithms are poor at filling gaps in 
 * non-fractal images, ie. those with strongly linear or otherwise regular
 * geometric features. 
 * <p>
 * The VMCA approach was extended slghtly by Anderson <i>et al.</i> who looked
 * at its application to satellite data. They took into account edge cases and
 * allowed for variable kernel size and random deviations in interpolated values
 * when working with continuous data.
 * <p>
 * References
 * <blockquote>
 * Sprott, J.C. 2004. A method for approximating missing data in spatial patterns. 
 * Computers and Graphics 28, 113-117.
 * </blockquote>
 * <blockquote>
 * Sharolyn Anderson, Mark A. Fonstad and Claudio Delrieux. Satellite image 
 * restoration using the VMCA model. <br>
 * (accessed from http://citeseerx.ist.psu.edu/viewdoc/summary?doi=10.1.1.94.4358)
 * </blockquote>
 * 
 */

package jaitools.media.jai.vmcafill;
