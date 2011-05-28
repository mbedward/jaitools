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

package jaitools.imageutils;

import java.awt.Rectangle;
import java.util.List;
import java.util.Map;
import java.util.Set;

import jaitools.CollectionFactory;


/**
 *
 * @author michael
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
