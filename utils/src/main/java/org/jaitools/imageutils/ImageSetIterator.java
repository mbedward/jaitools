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

package org.jaitools.imageutils;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jaitools.CollectionFactory;


/**
 * An iterator to sample a stack of values from images in an {@link ImageSet}.
 * 
 * @param <K> the key type
 * 
 * @author Michael Bedward
 * @since 1.2
 * @version $Id: ROIGeometry.java 1816 2011-06-24 03:22:08Z michael.bedward $
 */
public class ImageSetIterator<K> {

    // It is more convenient to work with two lists 
    // than a map in this class
    private final List<K> keys;
    private final List<SimpleIterator> delegates;

    /**
     * Package-private constructor.
     * 
     * @param set the target image set
     * @param bounds the bounds for this iterator
     */
    ImageSetIterator(ImageSet set, Rectangle bounds) {
        if (set == null || set.isEmpty()) {
            throw new IllegalArgumentException("The ImageSet must not be null or empty");
        }

        this.keys = CollectionFactory.list();
        this.delegates = CollectionFactory.list();
        
        Set<K> keySet = set.keySet();
        for (K key : keySet) {
            keys.add(key);
            delegates.add(new SimpleIterator(set.get(key), bounds, set.getOutsideValue(key)));
        }
    }

    /**
     * Gets a value from the first band of each image in this set 
     * and returns them as a {@code Map} of key : value pairs. 
     * 
     * @return image (or outside) values
     */
    public Map<K, Number> getSample() {
        return getSample(0);
    }
    
    /**
     * Gets a value from the specified band of each image in this set
     * and returns them as a {@code Map} of key : value pairs. 
     * 
     * @param band the image band to sample
     * @return image (or outside) values
     */
    public Map<K, Number> getSample(int band) {
        Map<K, Number> sample = CollectionFactory.map();
        
        for (int i = 0; i < keys.size(); i++) {
            K key = keys.get(i);
            Number value = delegates.get(i).getSample(band);
            sample.put(key, value);
        }
        
        return sample;
    }

    /**
     * Tests if the iterator has any more positions to sample.
     * 
     * @return {@code true} if more samples are available; 
     *     {@code false} otherwise
     */
    public boolean hasNext() {
        return delegates.get(0).hasNext();
    }

    /**
     * Advances the iterator to the next position if possible.
     * 
     * @return {@code true} if the iterator was moved;
     *     {@code false} if it is at the end of its bounds
     */
    public boolean next() {
        if (hasNext()) {
            for (SimpleIterator iter : delegates) {
                iter.next();
            }
        
            return true;
        }
        
        return false;
    }

}
