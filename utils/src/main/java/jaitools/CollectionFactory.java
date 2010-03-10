/*
 * Copyright 2009-2010 Michael Bedward
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
 * List&lt;Integer&gt; list1 = CollectionFactory.list();
 * List&lt;List&lt;String&gt;&gt; list2 = CollectionFactory.list();
 * </code></pre>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class CollectionFactory {
    /**
     * Create a new {@code Map} instance.
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;MyObject&gt; foo = CollectionFactory.map();
     * </code></pre>
     * 
     * @return a new Map&lt;K,V&gt; instance
     */
    public static <K, V> Map<K, V> map() {
        return new HashMap<K, V>();
    }

    /**
     * Create a new {@code Map} instance that maintains the
     * input order of its key:value pairs.
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;MyObject&gt; foo = CollectionFactory.orderedMap();
     * </code></pre>
     *
     * @return a new Map&lt;K,V&gt; instance
     */
    public static <K, V> Map<K, V> orderedMap() {
        return new LinkedHashMap<K, V>();
    }

    /**
     * Create a new {@code SortedMap} instance. Note that type
     * {@code T} must implement {@linkplain Comparable}.
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;MyObject&gt; foo = CollectionFactory.sortedMap();
     * </code></pre>
     * 
     * @return a new SortedMap&lt;K,V&gt; instance
     */
    public static <K, V> SortedMap<K, V> sortedMap() {
        return new TreeMap<K, V>();
    }

    /**
     * Create a new {@code List} instance.
     * <p>
     * Example of use:
     * <pre><code>
     * List&lt;MyObject&gt; foo = CollectionFactory.list();
     * </code></pre>
     * 
     * @return a new List&lt;T&gt; instance
     */
    public static <T> List<T> list() {
        return new ArrayList<T>();
    }
    
    /**
     * Create a new {@code Stack} instance.
     * <p>
     * Example of use:
     * <pre><code>
     * Stack&lt;MyObject&gt; foo = CollectionFactory.stack();
     * </code></pre>
     * 
     * @return a new Stack&lt;T&gt; instance
     */
    public static <T> Stack<T> stack() {
        return new Stack<T>();
    }

    /** 
     * Create a new {@code Set} instance.
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;MyObject&gt; foo = CollectionFactory.set();
     * </code></pre>
     * 
     * @return a new HashSet&lt;T&gt; instance
     */
    public static <T> Set<T> set() {
        return new HashSet<T>();
    }

    /**
     * Create a new {@code Set} instance that maintains
     * the input order of its elements.
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;MyObject&gt; foo = CollectionFactory.set();
     * </code></pre>
     *
     * @return a new LinkedHashSet&lt;T&gt; instance
     */
    public static <T> Set<T> orderedSet() {
        return new LinkedHashSet<T>();
    }

    /** 
     * Create a new {@code SortedSet} instance. Note that the type {@code T}
     * must implement {@linkplain Comparable}.
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;MyObject&gt; foo = CollectionFactory.sortedSet();
     * </code></pre>
     * 
     * @return a new Set&lt;T&gt; instance
     */
    public static <T> SortedSet<T> sortedSet() {
        return new TreeSet<T>();
    }


}
