/*
 * Copyright 2010 Michael Bedward
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

package jaitools.lookup;

import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * @author Michael Bedward
 */
public class LookupTest {
    
    @After
    public void tearDown() {
        Lookup.clearCache();
    }

    @Test
    public void testLookup() {
        System.out.println("   testLookup");

        final Class clazz = jaitools.numeric.Processor.class;

        List<Class> providers = Lookup.getProviders(clazz.getName());
        assertFalse(providers.isEmpty());

        for (Class c : providers) {
            assertTrue(clazz.isAssignableFrom(c));
        }
    }

    @Test
    public void testLookupCache() {
        System.out.println("   testLookupCache");

        final Class clazz = jaitools.numeric.Processor.class;

        List<Class> providers = Lookup.getProviders(clazz.getName());
        Map<String, List<Class>> cachedProviders = Lookup.getCachedProviders();

        List<Class> cachedList = cachedProviders.get(clazz.getName());
        assertNotNull(cachedList);
        assertTrue(cachedList.containsAll(providers));
    }

}
