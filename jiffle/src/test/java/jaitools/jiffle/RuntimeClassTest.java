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

package jaitools.jiffle;

import java.util.Map;

import jaitools.CollectionFactory;
import jaitools.jiffle.runtime.JiffleDirectRuntime;
import jaitools.jiffle.runtime.JiffleIndirectRuntime;

import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for basic Jiffle object creation, setting attributes and compiling.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class RuntimeClassTest {
    
    private Jiffle jiffle;
    private Map<String, Jiffle.ImageRole> imageParams;
    
    @Before
    public void setup() {
        jiffle = new Jiffle();
        imageParams = CollectionFactory.map();
    }
    
    @Test
    public void getDirectRuntime() throws Exception {
        setupSingleDestScript();
        Object runtime = jiffle.getRuntimeInstance(Jiffle.EvaluationModel.DIRECT);
        assertTrue(runtime instanceof JiffleDirectRuntime);
    }

    @Test
    public void getIndirectRuntime() throws Exception {
        setupSingleDestScript();
        Object runtime = jiffle.getRuntimeInstance(Jiffle.EvaluationModel.INDIRECT);
        assertTrue(runtime instanceof JiffleIndirectRuntime);
    }

    @Test
    public void customDirectBaseClass() throws Exception {
        setupSingleDestScript();
        Object runtime = jiffle.getRuntimeInstance(MockDirectBaseClass.class);
        assertTrue(runtime instanceof MockDirectBaseClass);
    }    

    @Test
    public void customIndirectBaseClass() throws Exception {
        setupSingleDestScript();
        Object runtime = jiffle.getRuntimeInstance(MockIndirectBaseClass.class);
        assertTrue(runtime instanceof MockIndirectBaseClass);
    }
    
    @Test(expected=JiffleException.class)
    public void invalidBaseClass() throws Exception {
        class Foo extends NullRuntime { }
        
        setupSingleDestScript();
        Object runtime = jiffle.getRuntimeInstance(Foo.class);
    }

    private void setupSingleDestScript() throws JiffleException {
        String script = "dest = 42;";
        jiffle.setScript(script);
        
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle.setImageParams(imageParams);
        jiffle.compile();
    }
    
}
