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

import jaitools.CollectionFactory;
import jaitools.numeric.NumberOperations;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.media.jai.iterator.RectIter;
import javax.media.jai.iterator.RectIterFactory;

/**
 *
 * @author michael
 */
public class ImageSetIter<K> {
    private final Map<K, Delegate> delegates;
    private final Rectangle bounds;
    private final Point pos;
    private boolean finished;

    ImageSetIter(ImageSet set, Rectangle bounds) {
        if (set == null) {
            throw new IllegalArgumentException("The ImageSet argument must not be null");
        }

        this.bounds = bounds;
        this.pos = new Point(bounds.x, bounds.y);
        this.finished = false;
        
        this.delegates = CollectionFactory.orderedMap();
        Set<K> keySet = set.keySet();
        for (K key : keySet) {
            Delegate d = new Delegate(
                    RectIterFactory.create(set.get(key), bounds),
                    set.getBounds(key),
                    set.getOutsideValue(key) );

            delegates.put(key, d);
        }
    }

    public Map<K, Integer> getSample() {
        return getSample(0);
    }
    
    public Map<K, Integer> getSample(int band) {
        Map<K, Integer> sample = CollectionFactory.map();
        
        for (Entry<K, Delegate> e : delegates.entrySet()) {
            sample.put(e.getKey(), e.getValue().getSample(band));
        }
        return sample;
    }

    public Map<K, Float> getSampleFloat() {
        return getSampleFloat(0);
    }
    
    public Map<K, Float> getSampleFloat(int band) {
        Map<K, Float> sample = CollectionFactory.map();
        
        for (Entry<K, Delegate> e : delegates.entrySet()) {
            sample.put(e.getKey(), e.getValue().getSampleFloat(band));
        }
        return sample;
    }

    public Map<K, Double> getSampleDouble() {
        return getSampleDouble(0);
    }
    
    public Map<K, Double> getSampleDouble(int band) {
        Map<K, Double> sample = CollectionFactory.map();
        
        for (Entry<K, Delegate> e : delegates.entrySet()) {
            sample.put(e.getKey(), e.getValue().getSampleDouble(band));
        }
        return sample;
    }

    public boolean hasNext() {
        return !finished;
    }

    public boolean next() {
        if (!finished) {
            pos.x = (pos.x + 1) % bounds.width;

            if (pos.x == bounds.x) {
                if (pos.y < bounds.y + bounds.height - 1) {
                    pos.y++;
                } else {
                    finished = true;
                }
            }

            if (!finished) {
                for (Delegate d : delegates.values()) {
                    d.setPosition(pos);
                }
            }
        }

        return !finished;
    }

    /**
     * This class wraps each of the RectIter objects used to iterate over
     * images in the set, and is responsible for positioning its iterator
     * and dealing with out-of-bounds requests.
     */
    static class Delegate {
        final RectIter iter;
        final Rectangle bounds;
        final Point iterPos;
        final Number outsideValue;
        private boolean inside;

        Delegate(RectIter iter, Rectangle bounds, Number outsideValue) {
            this.iter = iter;
            this.bounds = bounds;
            this.outsideValue = NumberOperations.copy(outsideValue);
            this.iterPos = new Point(bounds.x, bounds.y);
            this.inside = true;
        }

        RectIter getIter() {
            return iter;
        }

        void setPosition(Point pos) {
            inside = bounds.contains(pos);
            if (inside) {
                int dy = pos.y - iterPos.y;
                if (dy < 0) {
                    iter.startLines();
                    dy = pos.y - bounds.y;
                }
                
                while (dy > 0) {
                    iter.nextLineDone();
                    dy-- ;
                }

                int dx = pos.x - iterPos.x;
                if (dx < 0) {
                    iter.startPixels();
                    dx = pos.x - bounds.x;
                }

                while (dx > 0) {
                    iter.nextPixelDone();
                    dx-- ;
                }
                
                iterPos.setLocation(pos);
            }
        }

        private Integer getSample(int band) {
            if (inside) {
                return iter.getSample(band);
            } else {
                return outsideValue == null ? null : outsideValue.intValue();
            }
        }
        
        private Float getSampleFloat(int band) {
            if (inside) {
                return iter.getSampleFloat(band);
            } else {
                return outsideValue == null ? null : outsideValue.floatValue();
            }
        }

        private Double getSampleDouble(int band) {
            if (inside) {
                return iter.getSampleDouble(band);
            } else {
                return outsideValue == null ? null : outsideValue.doubleValue();
            }
        }
    }
    
}
