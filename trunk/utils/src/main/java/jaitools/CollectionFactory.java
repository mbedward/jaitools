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

package jaitools;

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
 * Convenience methods to create generic collections
 * following the DRY (Don't Repeat Yourself) principle.
 * Examples of use:
 * <pre><code>
 * List&lt;Integer&gt; list1 = CollectionFactory.newList();
 * List&lt;List&lt;String&gt;&gt; list2 = CollectionFactory.newList();
 * </code></pre>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class CollectionFactory {
    /**
     * Magic method to create a generic {@code Map}
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;MyObject&gt; foo = CollectionFactory.newMap();
     * </code></pre>
     * 
     * @return a new Map&lt;K,V&gt; instance
     */
    public static <K, V> Map<K, V> newMap() {
        return new HashMap<K, V>();
    }

    /**
     * Magic methid to create a TreeMap
     * <p>
     * Example of use:
     * <pre><code>
     * Map&lt;MyObject&gt; foo = CollectionFactory.newTreeMap();
     * </code></pre>
     * 
     * @return a new TreeMap&lt;K,V&gt; instance
     */
    public static <K, V> TreeMap<K, V> newTreeMap() {
        return new TreeMap<K, V>();
    }

    /**
     * Magic method to create a generic ArrayList
     * <p>
     * Example of use:
     * <pre><code>
     * List&lt;MyObject&gt; foo = CollectionFactory.newList();
     * </code></pre>
     * 
     * @return a new ArrayList&lt;T&gt; instance
     */
    public static <T> List<T> newList() {
        return new ArrayList<T>();
    }
    
    /**
     * Magic method to create a generic Stack.
     * <p>
     * Example of use:
     * <pre><code>
     * Stack&lt;MyObject&gt; foo = CollectionFactory.newStack();
     * </code></pre>
     * 
     * @return a new &lt;T&gt; instance
     */
    public static <T> Stack<T> newStack() {
        return new Stack<T>();
    }

    /** 
     * Magic method to create a generic Set
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;MyObject&gt; foo = CollectionFactory.newSet();
     * </code></pre>
     * 
     * @return a new HashSet&lt;T&gt; instance
     */
    public static <T> Set<T> newSet() {
        return new HashSet<T>();
    }

    /** 
     * Magic method to create a generic {@code TreeSet}
     * <p>
     * Example of use:
     * <pre><code>
     * Set&lt;MyObject&gt; foo = CollectionFactory.newTreeSet();
     * </code></pre>
     * 
     * @return a new Set&lt;T&gt; instance
     */
    public static <T> TreeSet<T> newTreeSet() {
        return new TreeSet<T>();
    }


}
