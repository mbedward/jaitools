/* 
 *  Copyright (c) 2010, Michael Bedward. All rights reserved. 
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

package org.jaitools.jts;

/**
 * Defines methods to control the smoothing process.
 * {@code LineSmoother} has a default implementation
 * that specifies a constant number of vertices in smoothed
 * segments and no lower bound on the distance between
 * input vertices for smoothing.
 * <p>
 * To customize smoothing, pass your own implementation
 * to {@link LineSmoother#setControl(org.jaitools.jts.SmootherControl) }
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public interface SmootherControl {

    /**
     * Gets the minimum distance between input vertices
     * for the segment to be smoothed. Segments smaller
     * than this will be copied to the output unchanged.
     *
     * @return minimum segment length for smoothing
     */
    double getMinLength();

    /**
     * Given an input segment length, returns the number
     * of vertices to use for the smoothed segment. This
     * number includes the segment end-points.
     *
     * @param length input segment length
     *
     * @return number of vertices in the smoothed segment
     *         including the end-points
     */
    int getNumVertices(double length);
}
