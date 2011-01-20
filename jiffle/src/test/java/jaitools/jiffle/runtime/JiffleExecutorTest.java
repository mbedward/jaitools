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

package jaitools.jiffle.runtime;

import java.util.Map;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;

import org.junit.Test;

/**
 * Unit tests for JiffleExecutor.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class JiffleExecutorTest {
    private Map<String, Jiffle.ImageRole> imageParams;

    @Test
    public void simpleJob() throws Exception {
        imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        Jiffle jiffle = new Jiffle("dest = 42;", imageParams);
        
        JiffleExecutor executor = new JiffleExecutor();
        executor.submit(jiffle);
    }
            
}
