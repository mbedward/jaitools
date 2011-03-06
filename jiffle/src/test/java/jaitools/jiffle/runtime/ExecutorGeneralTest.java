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

import jaitools.CollectionFactory;
import jaitools.imageutils.ImageUtils;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import java.awt.image.RenderedImage;
import java.io.ByteArrayOutputStream;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.logging.StreamHandler;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;


/**
 * General tests for {@link JiffleExecutor}.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ExecutorGeneralTest {
    private static final int WIDTH = 100;
    private static final double TOL = 1.0e-8;
    
    private JiffleExecutor executor;
    private StreamHandler handler;
    private ByteArrayOutputStream out;

    @Before
    public void setup() {
        executor = new JiffleExecutor();
        
        Logger logger = Logger.getLogger(JiffleExecutor.class.getName());  
        Formatter formatter = new SimpleFormatter();  
        out = new ByteArrayOutputStream();  
        handler = new StreamHandler(out, formatter);  
        logger.addHandler(handler);  
        logger.setUseParentHandlers(false);
    }
    
    @After
    public void cleanup() {
        Logger logger = Logger.getLogger(JiffleExecutor.class.getName());
        logger.removeHandler(handler);
        logger.setUseParentHandlers(true);
        
        executor.shutdownAndWait(1, TimeUnit.SECONDS);
    }
    
    
    @Test
    public void okToSubmitUncompiledScript() throws Exception {
        System.out.println("   ok to submit uncompiled script");
        executor.submit(createJiffle("dest=42;", false), createImagesMap(), null);
    }
    
    @Test(expected=JiffleExecutorException.class)
    public void submitBadScript() throws Exception {
        System.out.println("   receive exception when script will not compile");
        Jiffle jiffle = createJiffle("dest = unknownvar;", false);
        executor.submit(jiffle, createImagesMap(), null);
    }
    
    @Test
    public void defaultPollingInterval() throws Exception {
        System.out.println("   default polling interval");
        assertEquals(JiffleExecutor.DEFAULT_POLLING_INTERVAL, executor.getPollingInterval());
    }

    @Test
    public void setPollingInterval() throws Exception {
        System.out.println("   set polling interval");
        
        executor.setPollingInterval(50);
        assertEquals(50L, executor.getPollingInterval());
    }
    
    @Test
    public void invalidPollingIntervalIgnored() throws Exception {
        System.out.println("   invalid polling interval is ignored");
        executor.setPollingInterval(-1);
        assertEquals(JiffleExecutor.DEFAULT_POLLING_INTERVAL, executor.getPollingInterval());
    }
    
    @Test
    public void invalidPollingIntervalWarning() throws Exception {
        System.out.println("   invalid polling interval warning received");
        assertWarningMessage(-1, "polling interval ignored");
    }
    
    @Test
    public void pollingIntervalAfterFirstTaskIgnored() throws Exception {
        System.out.println("   polling interval set after first task is ignored");
        
        executor.submit(createJiffle("dest=42;", true), createImagesMap(), null);
        executor.setPollingInterval(JiffleExecutor.DEFAULT_POLLING_INTERVAL * 2);
        
        // polling interval set after task should have been ignored
        assertEquals(JiffleExecutor.DEFAULT_POLLING_INTERVAL, executor.getPollingInterval());
    }
    
    @Test
    public void pollingIntervalAfterFirstTaskWarning() throws Exception {
        System.out.println("   polling interval set after first task warning received");
        
        executor.submit(createJiffle("dest=42;", true), createImagesMap(), null);
        assertWarningMessage(JiffleExecutor.DEFAULT_POLLING_INTERVAL * 2, 
                "polling interval ignored");
    }
    
    private void assertWarningMessage(long pollingInterval, String expectedMsg) {
        executor.setPollingInterval(pollingInterval);

        handler.flush();
        String logMsg = out.toString();

        assertNotNull(logMsg);
        assertTrue(logMsg.toLowerCase().contains(expectedMsg.toLowerCase()));
    }
    
    
    @Test
    public void addEventListener() throws Exception {
        System.out.println("   add event listener");
        
        JiffleEventListener listener = createListener();
        executor.addEventListener(listener);
        assertTrue(executor.isListening(listener));
    }
    
    @Test
    public void removeEventListener() throws Exception {
        System.out.println("   remove event listener");
        
        JiffleEventListener listener = createListener();
        executor.addEventListener(listener);
        
        executor.removeEventListener(listener);
        assertFalse(executor.isListening(listener));
    }
    
    @Test
    public void speculativeRemoveEventListener() throws Exception {
        System.out.println("   ok to speculatively call removeEventListener");
        executor.removeEventListener(createListener());
    }
    
    @Test
    public void taskCompletedOnShutdown() throws Exception {
        System.out.println("   task completed after shutdown request");
        WaitingListener listener = new WaitingListener();
        executor.addEventListener(listener);
        listener.setNumTasks(1);
        
        Map<String, RenderedImage> emptyMap = Collections.emptyMap();
        executor.submit(new MockJiffle(50, 10L), emptyMap, null);

        executor.shutdown();
        if (!listener.await(2, TimeUnit.SECONDS)) {
            fail("Listener time-out period elapsed");
        }
        
        List<JiffleExecutorResult> results = listener.getResults();
        assertEquals(1, results.size());
        assertTrue(results.get(0).isCompleted());
    }
    
    @Test
    public void taskDiscardedOnImmediateShutdown() throws Exception {
        System.out.println("   task discarded after shutdownNow request");
        
        WaitingListener listener = new WaitingListener();
        executor.addEventListener(listener);
        listener.setNumTasks(1);
        
        Map<String, RenderedImage> emptyMap = Collections.emptyMap();
        executor.submit(new MockJiffle(50, 10L), emptyMap, null);

        executor.shutdownNow();
        boolean receivedEvent = listener.await(1, TimeUnit.SECONDS);
        assertFalse(receivedEvent);
    }
    
    
    private Jiffle createJiffle(String script, boolean compile) throws Exception {
        Jiffle jiffle = new Jiffle();
        jiffle.setScript(script);
        
        Map<String, Jiffle.ImageRole> imageParams = CollectionFactory.map();
        imageParams.put("dest", Jiffle.ImageRole.DEST);
        
        jiffle.setImageParams(imageParams);
        if (compile ) {
            jiffle.compile();
        }
        
        return jiffle;
    }
    
    private Map<String, RenderedImage> createImagesMap() throws Exception {
        Map<String, RenderedImage> images = CollectionFactory.map();
        RenderedImage img = ImageUtils.createConstantImage(10, 10, 0d);
        images.put("dest", img);
        return images;
    }

    private JiffleEventListener createListener() {
        return new JiffleEventListener() {
            public void onCompletionEvent(JiffleEvent ev) {
            }

            public void onFailureEvent(JiffleEvent ev) {
            }
        };
    }
    
}
