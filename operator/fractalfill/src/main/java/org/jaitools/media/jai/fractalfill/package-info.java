/* 
 *  Copyright (c) 2011, Michael Bedward. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
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

package org.jaitools.media.jai.fractalfill;
