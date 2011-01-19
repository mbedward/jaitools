/*
 * Copyright 2009-2011 Michael Bedward
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

package jaitools.jiffle.runtime;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * Executes Jiffle scripts on separate threads.
 * <p>
 * Example of use:
 * <pre><code>
 * class Foo {
 * 
 *     public Foo() {
 *         private JiffleExecutor jiffleExec = new JiffleExecutor();
 *         jiffleExec.addEventListener(new JiffleEventAdapter() {
 *             public void onCompletionEvent(JiffleCompletionEvent ev) {
 *                 onCompletion(ev);
 *             }
 * 
 *             public void onFailureEvent(JiffleFailureEvent ev) {
 *                 onFailure(ev);
 *             }
 *         });
 *     }
 * 
 *     public doFoo() {
 *         // get script and create compiled jiffle
 *         String script = ....
 *         Jiffle j = new Jiffle(script);
 *         if (j.isCompiled()) {
 *             // set input and output images etc., then...
 *             jiffleExec.submit(j);
 *         }
 *     }
 * 
 *     private void onCompletion(JiffleCompletionEvent ev) {
 *         RenderedImage img = ev.getJiffle().getImage("anImageName");
 * 
 *         // do something with your beautiful image...
 *     }
 * 
 *     private void onFailure(JiffleFailureEvent ev) {
 *         System.err.println("Bummer: script failed to run");
 *     }
 * }
 * 
 * </code></pre>
 * 
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class JiffleExecutor {
    
    private static ExecutorService threadPool;
    private static int jobID = 0;
    
    private Map<Integer, Future<?>> jobs; 
    private List<JiffleEventListener> listeners;
    
    /**
     * Creates an executor with default settings. A default executor sets 
     * no upper limit to the number of {@code Jiffle} runtime objects that
     * can execute concurrently. It uses a cached thread pool, creating new
     * threads when required but recycling existing threads when earlier
     * jobs have completed.
     */
    public JiffleExecutor() {
        threadPool = Executors.newCachedThreadPool();
        jobs = CollectionFactory.orderedMap();
        listeners = CollectionFactory.list();
    }
    
    
    /**
     * Creates an executor that can have, at most, {@code maxJobs} 
     * {@code Jiffle} objects running concurrently. If a larger number of
     * jobs is submitted, some will be placed in a queue to wait for 
     * executor threads to become available.
     * 
     * @param maxJobs the maximum number of 
     */
    public JiffleExecutor(int maxJobs) {
        threadPool = Executors.newFixedThreadPool(maxJobs);
        jobs = CollectionFactory.orderedMap();
        listeners = CollectionFactory.list();
    }

    /**
     * Adds an event listener.
     * 
     * @param listener the listener
     * 
     * @see {@link JiffleEvent}
     */
    public void addEventListener(JiffleEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Submits an {@code Jiffle} object for immediate execution. 
     * <p>
     * This method first checks that the {@code Jiffle} object is properly
     * compiled. It then retrieves a {@link JiffleRuntime} instance from
     * the object and executes it.
     * 
     * @param jiffle a properly compiled {@code Jiffle} object
     * 
     * @return the job ID that can be used to query progress
     * 
     * @throws JiffleExecutorException if the {@code Jiffle} object was not
     *         compiled correctly
     */
    public int submit(final Jiffle jiffle) throws JiffleExecutorException {
        if (!jiffle.isCompiled()) {
            throw new JiffleExecutorException("Jiffle object not compiled" + jiffle.getName());
        }
        
        JiffleRuntime runtime = jiffle.getRuntimeInstance();
        int id = ++jobID;
        jobs.put(id, threadPool.submit(new JiffleTask(id, this, runtime)));
        return id;
    }
    

    /*
    void onTaskStatusEvent(JiffleTask task) {
        if (task.isCompleted()) {
            fireCompletionEvent(task);
        } else {
            fireFailureEvent(task);
        }
    }
    
    void onTaskProgressEvent(JiffleTask task, float progress) {
        fireProgressEvent(task, progress);
    }
    
    private void fireCompletionEvent(JiffleTask task) {
        JiffleCompletionEvent ev = new JiffleCompletionEvent(task.getId(), task.getJiffle());
        for (JiffleEventListener el : listeners) {
            el.onCompletionEvent(ev);
        }
    }

    private void fireFailureEvent(JiffleTask task) {
        JiffleFailureEvent ev = new JiffleFailureEvent(task.getId(), task.getJiffle());
        for (JiffleEventListener el : listeners) {
            el.onFailureEvent(ev);
        }
    }
    
    private void fireProgressEvent(JiffleTask task, float progress) {
        JiffleProgressEvent ev = new JiffleProgressEvent(task.getId(), task.getJiffle(), progress);
        for (JiffleEventListener el : listeners) {
            el.onProgressEvent(ev);
        }
    }
     * 
     */
}
