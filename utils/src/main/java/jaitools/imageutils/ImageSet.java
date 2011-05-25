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
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import jaitools.CollectionFactory;
import jaitools.numeric.NumberOperations;

/**
 *
 * @author michael
 */
public class ImageSet<K> {

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
        
    }

    Map<K, Element> images = CollectionFactory.orderedMap();

    public void add(K key, RenderedImage image) {
        add(key, image, null);
    }

    public void add(K key, RenderedImage image, Number outsideValue) {
        if (key == null) {
            throw new IllegalArgumentException("key must not be null");
        }
        if (image == null) {
            throw new IllegalArgumentException("image must not be null");
        }
        
        images.put(key, new Element(image, outsideValue));
    }

    public int getNumImages() {
        return images.size();
    }

    public Set<K> keySet() {
        return images.keySet();
    }

    public RenderedImage get(K key) {
        return images.get(key).getImage();
    }
    
    public RenderedImage getByIndex(int index) {
        if (index < 0 || index >= getNumImages()) {
            throw new IllegalArgumentException("index out of range: " + index);
        }
        
        Iterator<Element> iterator = images.values().iterator();
        while (index > 0) {
            iterator.next();
        }
        return iterator.next().getImage();
    }

    /**
     * Creates a new iterator based on the bounds of the first image added
     * to this set or, if that has been removed, the image that has been
     * in this set for the longest period.
     * 
     * @return the new iterator
     * 
     * @throws IllegalArgumentException if this image set is empty
     */
    public ImageSetIter getIterator() {
        return getIterator(images.keySet().iterator().next());
    }

    /**
     * Creates a new iterator based on the bounds of the image with the 
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
    public ImageSetIter getIterator(K referenceImageKey) {
        if (images.isEmpty()) {
            throw new IllegalArgumentException("This image set is empty");
        }
        
        Rectangle bounds = getBounds(referenceImageKey);
        return getIterator(bounds);
    }

    /**
     * Creates a new iterator based on the bounds of the image with the 
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
    public ImageSetIter getIterator(Rectangle bounds) {
        return new ImageSetIter<K>(this, bounds);
    }

    /**
     * Gets the bounds of the image with the given key.
     * 
     * @param imageKey the image key
     * 
     * @return image bounds as a new Rectangle
     * 
     * @throws IllegalArgumentException if no image corresponds to the key value
     */
    public Rectangle getBounds(K imageKey) {
        checkKey(imageKey);
        RenderedImage image = images.get(imageKey).getImage();
        return new Rectangle(image.getMinX(), image.getMinY(), image.getWidth(), image.getHeight());
    }

    public Number getOutsideValue(K imageKey) {
        checkKey(imageKey);
        return images.get(imageKey).getOutsideValue();
    }

    private void checkKey(K key) {
        if (!images.containsKey(key)) {
            throw new IllegalArgumentException("The key does not match an image in this set");
        }
    }
}

