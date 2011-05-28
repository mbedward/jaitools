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
import java.awt.image.RenderedImage;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import jaitools.CollectionFactory;
import jaitools.numeric.NumberOperations;

/**
 *
 * @author michael
 */
public class ImageSet<K> {

    private final Map<K, Element> elements;


    /**
     * Creates a new image set which will contain the same keys, images and 
     * outside values as the input set. If the input set is {@code null} or
     * empty, an empty image set is created.
     * 
     * @param set the image set to copy
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
     * @param other the other image set
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
        return Collections.unmodifiableSet( elements.keySet() );
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
    public ImageSetIterator<K> getIterator() {
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
    public ImageSetIterator<K> getIterator(K referenceImageKey) {
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
     * @param referenceImageKey the key of the image to use as the reference
     *        for the iterator
     * 
     * @return the new iterator
     * 
     * @throws IllegalArgumentException if this image set is empty or if no 
     * image corresponds to the key value
     */
    public ImageSetIterator<K> getIterator(Rectangle bounds) {
        return new ImageSetIterator<K>(this, bounds);
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



    public static class Element {
        private final RenderedImage image;
        private final Number outsideValue;

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

}

