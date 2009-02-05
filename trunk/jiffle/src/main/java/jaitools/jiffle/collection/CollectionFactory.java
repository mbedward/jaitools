/*
 * Copyright 2009 Michael Bedward
 * 
 * This file is part of jai-tools.

 * jai-tools is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the 
 * License, or (at your option) any later version.

 * jai-tools is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.

 * You should have received a copy of the GNU Lesser General Public 
 * License along with jai-tools.  If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package jaitools.jiffle.collection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Some convenience methods to create generic collections
 * following the DRY (Don't Repeat Yourself) principle.
 * Examples of use:
 * <code>
 * List<Integer> list1 = CollectionFactory.newList();
 * List<List<String>> list2 = CollectionFactory.newList();
 * </code>
 * 
 * @author Michael Bedward
 */
public class CollectionFactory {
    /**
     * Magic method to create a generic Map
     * <p>
     * Example of use:
     * <code>
     * Map<MyObject> foo = CollectionFactory.newMap();
     * </code>
     * 
     * @return a new Map<K,V> instance
     */
    public static <K, V> Map<K, V> newMap() {
        return new HashMap<K, V>();
    }

    /**
     * Magic methid to create a TreeMap
     * <p>
     * Example of use:
     * <code>
     * Map<MyObject> foo = CollectionFactory.newTreeMap();
     * </code>
     * 
     * @return a new TreeMap<K,V> instance
     */
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    /**
     * Magic method to create a generic ArrayList
     * <p>
     * Example of use:
     * <code>
     * List<MyObject> foo = CollectionFactory.newList();
     * </code>
     * 
     * @return a new ArrayList<T> instance
     */
    public static <T> List<T> newList() {
        return new ArrayList<T>();
    }
    
    /**
     * Magic method to create a generic Stack.
     * <p>
     * Example of use:
     * <code>
     * Stack<MyObject> foo = CollectionFactory.newStack();
     * </code>
     * 
     * @return a new Stack<T> instance
     */
    public static <T> Stack<T> newStack() {
        return new Stack<T>();
    }

    /** 
     * Magic method to create a generic Set
     * <p>
     * Example of use:
     * <code>
     * Set<MyObject> foo = CollectionFactory.newSet();
     * </code>
     * 
     * @return a new HashSet<T> instance
     */
    public static <T> Set<T> newSet() {
        return new HashSet<T>();
    }

    /** 
     * Magic method to create a generic Set
     * <p>
     * Example of use:
     * <code>
     * Set<MyObject> foo = CollectionFactory.newTreeSet();
     * </code>
     * 
     * @return a new Set<T> instance
     */
    public static <T> TreeSet<T> newTreeSet() {
        return new TreeSet<T>();
    }


}
