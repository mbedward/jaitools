/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Stack;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Convenience methods to create generic collections
 * following the DRY (Don't Repeat Yourself) principle.
 * Examples of use:
 * <pre><code>
 * // simple
 * List&lt;Integer&gt; list1 = CollectionFactory.list();
 * 
 * // nested
 * List&lt;List&lt;String&gt;&gt; list2 = CollectionFactory.list();
 * 
 * // a multi-map
 * Map&lt;String, List&lt;Integer&gt;&gt; multiMap = CollectionFactory.map();
 * </code></pre>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class CollectionFactory {
    /**
     * Returns a new {@code Map} object. The returned
     * object is a {@link HashMap}.
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;Foo, Bar&gt; myMap = CollectionFactory.map();
     * </code></pre>
     * 
     * @return a new Map&lt;K,V&gt; instance
     */
    public static <K, V> Map<K, V> map() {
        return new HashMap<K, V>();
    }

    /**
     * Returns a new {@code Map} object that maintains insertion
     * order. The returned object is a {@link LinkedHashMap}.
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;Foo, Bar&gt; myMap = CollectionFactory.orderedMap();
     * </code></pre>
     *
     * @return a new Map&lt;K,V&gt; instance
     */
    public static <K, V> Map<K, V> orderedMap() {
        return new LinkedHashMap<K, V>();
    }

    /**
     * Returns a new {@code SortedMap} object. Key type {@code K} must
     * implement {@linkplain Comparable}. The returned object is a
     * {@link TreeMap}.
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;Foo, Bar&gt; myMap = CollectionFactory.sortedMap();
     * </code></pre>
     * 
     * @return a new SortedMap&lt;K,V&gt; instance
     */
    public static <K, V> SortedMap<K, V> sortedMap() {
        return new TreeMap<K, V>();
    }
    
    /**
     * Returns a new {@code List} object. The returned object is
     * an {@link ArrayList}.
     * <p>
     * Example of use:
     * <pre><code>
     * List&lt;Foo&gt; myList = CollectionFactory.list();
     * </code></pre>
     * 
     * @return a new List&lt;T&gt; instance
     */
    public static <T> List<T> list() {
        return new ArrayList<T>();
    }
    
    /**
     * Returns a new {@code Stack} object.
     * <p>
     * Example of use:
     * <pre><code>
     * Stack&lt;Foo&gt; myStack = CollectionFactory.stack();
     * </code></pre>
     * 
     * @return a new Stack&lt;T&gt; instance
     */
    public static <T> Stack<T> stack() {
        return new Stack<T>();
    }

    /** 
     * Returns a new {@code Set} instance. The returned object is
     * a {@link HashSet}.
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;Foo&gt; mySet = CollectionFactory.set();
     * </code></pre>
     * 
     * @return a new HashSet&lt;T&gt; instance
     */
    public static <T> Set<T> set() {
        return new HashSet<T>();
    }

    /**
     * Returns a new {@code Set} instance that maintains
     * the insertion order of its elements.
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;Foo&gt; mySet = CollectionFactory.set();
     * </code></pre>
     *
     * @return a new LinkedHashSet&lt;T&gt; instance
     */
    public static <T> Set<T> orderedSet() {
        return new LinkedHashSet<T>();
    }

    /** 
     * Returns a new {@code SortedSet} instance. The type {@code T}
     * must implement {@linkplain Comparable}. The returned object
     * is a {@link TreeSet}.
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;MyObject&gt; foo = CollectionFactory.sortedSet();
     * </code></pre>
     * 
     * @return a new TreeSet&lt;T&gt; instance
     */
    public static <T> SortedSet<T> sortedSet() {
        return new TreeSet<T>();
    }


}
