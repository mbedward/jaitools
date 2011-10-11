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
import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jaitools.CollectionFactory;
import org.jaitools.imageutils.iterator.SimpleIterator;
import org.jaitools.numeric.NumberOperations;

/**
 *
 * @param <K> 
 * @author michael
 */
public class ImageSet<K> {

    private final Map<K, Element> elements;

    /**
     * Creates a new image set which will contain the same keys, images and 
     * outside values as the input set. If the input set is {@code null} or
     * empty, an empty image set is created.
     * 
     * @param <K> the key type
     * @param set the image set to copy
     * 
     * @return the new copy
     */
    public static <K> ImageSet<K> copy(ImageSet<K> set) {
        ImageSet<K> newSet = new ImageSet<K>();

        if (!(set == null || set.isEmpty())) {
            for (K key : set.keySet()) {
                RenderedImage image = set.get(key);
                Number outsideValue = set.getOutsideValue(key);
                newSet.add(key, image, outsideValue);
            }
        }

        return newSet;
    }

    /**
     * Creates a new, empty image set.
     */
    public ImageSet() {
        elements = CollectionFactory.orderedMap();
    }

    /**
     * Adds an image to this set to be associated with the given key.
     * 
     * @param key the key
     * @param image the image
     * @param outsideValue the value to be returned by iterators when
     *        positions beyond the bounds of this image (may be {@code null}
     * 
     * @throws IllegalArgumentException if either {@code key} or {@code image}
     *     is {@code null}
     */
    public void add(K key, RenderedImage image, Number outsideValue) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }

        elements.put(key, new Element(image, outsideValue));
    }

    /**
     * Gets the number of key:image pairs in this set. Note that
     * a single image may be mapped to more than one key.
     * 
     * @return the number of key:image pairs
     */
    public int size() {
        return elements.size();
    }

    /**
     * Tests if this image set is empty.
     * 
     * @return {@code true} if the set is empty
     */
    public boolean isEmpty() {
        return elements.isEmpty();
    }

    /**
     * Tests if this image set contains the given key.
     * 
     * @param key the key to search for
     * @return {@code true} if the key is found; {@code false} otherwise
     * @throws IllegalArgumentException if {@code key} is {@code null}
     */
    public boolean containsKey(K key) {
        return elements.containsKey(key);
    }

    /**
     * Tests if this image set contains the given image. Note
     * that the test is merely for reference equality.
     * 
     * @param image the image to search for
     * @return {@code true} is the image is in this set; {@code false} otherwise
     */
    public boolean containsImage(RenderedImage image) {
        for (Element e : elements.values()) {
            if (e.getImage() == image) {
                return true;
            }
        }
        return false;
    }

    /**
     * Retrieves an image from this set.
     * 
     * @param key the key associated with the image.
     * @return the image or {@code null} if the key could not be found
     */
    public RenderedImage get(K key) {
        return elements.get(key).getImage();
    }

    /**
     * Removes a key:image pair from this set.
     * 
     * @param key the key
     * @return the image associated with the key or {@code null} if
     *     the key could not be found
     */
    public RenderedImage remove(K key) {
        return elements.remove(key).getImage();
    }

    /**
     * Copies all key:image pairs from {@code otherSet} into this image set.
     * 
     * @param otherSet the image set to take contents from
     * 
     * @throws IllegalArgumentException if otherSet is {@code null}
     */
    public void putAll(ImageSet<? extends K> otherSet) {
        if (otherSet != this) {
            elements.putAll(otherSet.elements);
        }
    }

    /**
     * Removes all key:image pairs from this image set.
     */
    public void clear() {
        elements.clear();
    }

    /**
     * Retrieves a {@code Set} view of the keys in this image set.
     * Note that unlike Java collection classes, this method returns an
     * unmodifiable view of the keys.
     * 
     * @return keys contained in this image set
     */
    public Set<K> keySet() {
        return Collections.unmodifiableSet(elements.keySet());
    }

    /**
     * Retrieves a {@code Set} view of the keys in this image set.
     * Note that unlike Java collection classes, this method returns an
     * unmodifiable view of the keys.
     * 
     * @return keys contained in this image set
     */
    public Set<RenderedImage> values() {
        Set<RenderedImage> set = CollectionFactory.orderedSet();
        for (Element e : elements.values()) {
            set.add(e.getImage());
        }
        return Collections.unmodifiableSet(set);
    }

    /**
     * Gets a new iterator based on the bounds of the first image added
     * to this set or, if that has been removed, the image that has been
     * in this set for the longest period.
     * 
     * @return the new iterator
     * 
     * @throws IllegalArgumentException if this image set is empty
     */
    public Iterator<K> getIterator() {
        return getIterator(elements.keySet().iterator().next());
    }

    /**
     * Gets a new iterator based on the bounds of the image with the 
     * specified key value.
     * 
     * @param referenceImageKey the key of the image to use as the reference
     *        for the iterator
     * 
     * @return the new iterator
     * 
     * @throws IllegalArgumentException if this image set is empty or if no 
     * image corresponds to the key value
     */
    public Iterator<K> getIterator(K referenceImageKey) {
        if (elements.isEmpty()) {
            throw new IllegalArgumentException("This image set is empty");
        }

        Rectangle bounds = getBounds(referenceImageKey);
        return getIterator(bounds);
    }

    /**
     * Gets a new iterator based on the bounds of the image with the 
     * specified key value.
     * 
     * @param bounds iterator bounds
     * @return the new iterator
     * 
     * @throws IllegalArgumentException if this image set is empty or if no 
     * image corresponds to the key value
     */
    public Iterator<K> getIterator(Rectangle bounds) {
        return new Iterator<K>(this, bounds);
    }

    /**
     * Gets the bounds of the image associated with the given key.
     * 
     * @param key the image key
     * @return image bounds as a new Rectangle or {@code null} if the key
     *     could not be found
     */
    public Rectangle getBounds(K key) {
        Element e = elements.get(key);
        return e == null ? null : e.getBounds();
    }

    /**
     * Gets the enclosing bounds of all images in this set. This is 
     * the union of the individual image bounds. If the set is empty
     * an empty {@code Rectangle} will be returned.
     * 
     * @return enclosing bounds for this image set
     */
    public Rectangle getUnionBounds() {
        Rectangle r = new Rectangle();
        for (Element e : elements.values()) {
            r = r.union(e.getBounds());
        }
        return r;
    }

    /**
     * Gets the common bounds of images in this set This is the intersection
     * of the individual iamge bounds. An empty {@code Rectangle} will be
     * returned if ths set is empty, or if there is no area over which all
     * images overlap.
     * 
     * @return common bounds for this image set
     */
    public Rectangle getIntersectionBounds() {
        Rectangle r = null;
        for (Element e : elements.values()) {
            r = (r == null ? e.getBounds() : r.intersection(e.getBounds()));
            if (r.isEmpty()) {
                break;
            }
        }
        return r;
    }

    /**
     * Gets the value that an image set iterator will return for the
     * image associated with the given key when it is positioned 
     * outside the bounds of that image.
     * 
     * @param key the key
     * @return the value returned for out-of-bounds value requests
     *     (may be {@code null})
     */
    public Number getOutsideValue(K key) {
        assertKey(key);
        return elements.get(key).getOutsideValue();
    }

    /**
     * Tests that a given key is contained in this set and throws an exception if 
     * it is not.
     * 
     * @param key the key
     * @throws IllegalArgumentException if the key is not found
     */
    private void assertKey(K key) {
        if (!elements.containsKey(key)) {
            throw new IllegalArgumentException("The key does not match an image in this set");
        }
    }

    /**
     * An {@code ImageSet} element consisting of a {@code RenderedImage}
     * and an associated outside value.
     */
    public static class Element {

        private final RenderedImage image;
        private final Number outsideValue;

        /**
         * Creates a new element.
         * 
         * @param image the image
         * @param outsideValue the outside value
         */
        public Element(RenderedImage image, Number outsideValue) {
            this.image = image;
            this.outsideValue = NumberOperations.copy(outsideValue);
        }

        private RenderedImage getImage() {
            return image;
        }

        private Number getOutsideValue() {
            return NumberOperations.copy(outsideValue);
        }

        private Rectangle getBounds() {
            return new Rectangle(image.getMinX(), image.getMinY(),
                    image.getWidth(), image.getHeight());
        }
    }

    
    public static class Iterator<K> {

        // It is more convenient to work with two lists 
        // than a map in this class
        private final List<K> keys;
        private final List<SimpleIterator> delegates;

        /**
         * Private constructor.
         * 
         * @param set the target image set
         * @param bounds the bounds for this iterator
         */
        private Iterator(ImageSet set, Rectangle bounds) {
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
}
