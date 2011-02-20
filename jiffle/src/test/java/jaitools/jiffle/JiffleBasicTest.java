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

import java.io.File;
import java.net.URL;
import java.util.Map;

import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import jaitools.CollectionFactory;
import jaitools.jiffle.runtime.JiffleRuntime;

/**
 * Unit tests for basic Jiffle object creation, setting attributes and compiling.
 * 
 * @author Michael Bedward
 * @since 1.0
 * @version $Id$
 */
public class JiffleBasicTest {
    
    private Jiffle jiffle;
    private Map<String, Jiffle.ImageRole> imageParams;
    
    @Before
    public void setup() {
        jiffle = new Jiffle();
        imageParams = CollectionFactory.map();
    }
    
    @Test
    public void blankInstance() {
        System.out.println("   creating an empty Jiffle object");
        
        assertEquals("", jiffle.getScript());
        assertTrue(jiffle.getImageParams().isEmpty());
        assertFalse(jiffle.isCompiled());
    }
    
    @Test
    public void setScript() throws Exception {
        System.out.println("   set and get the script");
        
        String script = "dest = 42;";
        jiffle.setScript(script);
        
        String result = jiffle.getScript();
        assertTrue(result.contains(script));
    }
    
    @Test
    public void setImageParams() {
        System.out.println("   set and get image params");
        
        imageParams.put("src1", Jiffle.ImageRole.SOURCE);
        imageParams.put("src2", Jiffle.ImageRole.SOURCE);
        imageParams.put("dest1", Jiffle.ImageRole.DEST);
        imageParams.put("dest2", Jiffle.ImageRole.DEST);
        jiffle.setImageParams(imageParams);
        
        Map<String, Jiffle.ImageRole> result = jiffle.getImageParams();
        assertEquals(imageParams.size(), result.size());
        for (String name : imageParams.keySet()) {
            assertTrue(imageParams.get(name).equals(result.get(name)));
        }
    }
    
    @Test(expected=UnsupportedOperationException.class)
    public void tryToModifyImageParams() {
        System.out.println("   trying to modify map returned by getImageParams");
        
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle.setImageParams(imageParams);
        
        Map<String, Jiffle.ImageRole> unmodifiableMap = jiffle.getImageParams();
        
        // this should provoke an exception
        unmodifiableMap.clear();
    }

    @Test
    public void resetScript() throws Exception {
        System.out.println("   resetScript");
        
        String script1 = "dest = 42;";
        String script2 = "dest = foo;";
        
        jiffle.setScript(script1);
        jiffle.setScript(script2);
        
        String result = jiffle.getScript();
        assertFalse(result.contains(script1));
        assertTrue(result.contains(script2));
    }
    
    @Test
    public void compileValidScript() throws Exception {
        System.out.println("   compile valid script");
        
        String script = "dest = 42;";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        jiffle.setScript(script);
        jiffle.setImageParams(imageParams);
        jiffle.compile();
        
        assertTrue(jiffle.isCompiled());
    }

    @Test(expected=JiffleException.class)
    public void compileInvalidScriptThrowsException() throws Exception {
        System.out.println("   compile invalid script and get exception");
        
        // script with an uninitialized variable
        String script = "dest = x;";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        jiffle.setScript(script);
        jiffle.setImageParams(imageParams);
        jiffle.compile();
    }

    @Test
    public void compileInvalidScriptAndCheckStatus() throws Exception {
        System.out.println("   compile invalid script and check status");
        
        // script with an uninitialized variable
        String script = "dest = x;";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        jiffle.setScript(script);
        jiffle.setImageParams(imageParams);
        
        try {
            jiffle.compile();
        } catch (JiffleException ignored) {}
        
        assertFalse(jiffle.isCompiled());
    }
    
    @Test(expected=JiffleException.class)
    public void compileWithNoScript() throws Exception {
        System.out.println("   compile with no script set");
        
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle.setImageParams(imageParams);
        jiffle.compile();
    }

    @Test(expected=JiffleException.class)
    public void compileScriptWithoutImageParams() throws Exception {
        System.out.println("   compile script with missing image params");
        
        String script = "dest = 42;";
        
        jiffle.setScript(script);
        jiffle.compile();
    }
    
    @Test
    public void setName() {
        System.out.println("   set name");
        
        String name = "foo";
        jiffle.setName(name);
        assertEquals(name, jiffle.getName());
    }
    
    @Test
    public void scriptWithParamsConstructor() throws Exception {
        System.out.println("   Jiffle(script, imageParams)");
        
        String script = "dest = 42;";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle = new Jiffle(script, imageParams);
        
        assertTrue(jiffle.isCompiled());
    }
    
    @Test(expected=JiffleException.class)
    public void passingEmptyScriptToConstructor() throws Exception {
        System.out.println("   Jiffle(script, imageParams) with empty script");
        
        String script = "";
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle = new Jiffle(script, imageParams);
    }
    
    
    @Test
    public void fileWithParamsConstructor() throws Exception {
        System.out.println("   Jiffle(scriptFile, imageParams)");
        
        URL url = JiffleBasicTest.class.getResource("constant.jfl");
        File file = new File(url.toURI());
        
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle = new Jiffle(file, imageParams);
        
        assertTrue(jiffle.isCompiled());
    }
    
    @Test(expected=JiffleException.class)
    public void getRuntimeBeforeCompiling() throws Exception {
        System.out.println("   getRuntimeInstance before compiling");
        
        String script = "dest = 42;";
        jiffle.setScript(script);
        
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        jiffle.setImageParams(imageParams);
        
        JiffleRuntime runtime = jiffle.getRuntimeInstance();
    }    
}
