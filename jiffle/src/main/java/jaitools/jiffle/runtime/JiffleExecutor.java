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
import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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
    
    private static enum ThreadPoolType {
        CACHED,
        FIXED;
    }
    
    /** 
     * The default interval (milliseconds) for polling the 
     * result of a rendering task 
     */
    public static final long DEFAULT_POLLING_INTERVAL = 20L;

    private long pollingInterval;
    private List<Integer> jobsDone;
    
    /** Thread pool used to execute Jiffle runtime objects */
    private final ExecutorService threadPool;
    
    /** Scheduled executor to run job polling */
    private final ScheduledExecutorService watchExecutor;

    private static int jobID = 0;
    private Map<Integer, Future<JiffleExecutorResult>> jobs; 
    private List<JiffleEventListener> listeners;
    
    
    /**
     * Creates an executor with default settings. A default executor sets 
     * no upper limit to the number of {@code Jiffle} runtime objects that
     * can execute concurrently. It uses a cached thread pool, creating new
     * threads when required but recycling existing threads when earlier
     * jobs have completed.
     */
    public JiffleExecutor() {
        this(ThreadPoolType.CACHED, -1);
    }
    
    
    /**
     * Creates an executor that can have, at most,{@code maxJobs} 
     * {@code Jiffle} objects running concurrently. If a larger number of
     * jobs is submitted, some will be placed in a queue to wait for 
     * executor threads to become available.
     * 
     * @param maxJobs the maximum number of 
     */
    public JiffleExecutor(int maxJobs) {
        this(ThreadPoolType.FIXED, maxJobs);
    }
    
    /**
     * Private constructor for common setup.
     * 
     * @param type type of thread pool to use
     * 
     * @param maxJobs maximum number of concurrent jobs (ignored if
     *        {@code type} is not {@code FIXED}
     */
    private JiffleExecutor(ThreadPoolType type, int maxJobs) {
        switch (type) {
            case CACHED:
                threadPool = Executors.newCachedThreadPool();
                break;
                
            case FIXED:
                threadPool = Executors.newFixedThreadPool(maxJobs);
                break;
                
            default:
                throw new IllegalArgumentException("Bad arg to private JiffleExecutor constructor");
        }
        watchExecutor = Executors.newSingleThreadScheduledExecutor();
        pollingInterval = DEFAULT_POLLING_INTERVAL;
        jobs = CollectionFactory.orderedMap();
        listeners = CollectionFactory.list();
        jobsDone = CollectionFactory.list();
    }
    
    /**
     * Sets the interval between polling the executing jobs.
     *
     * @param interval interval in milliseconds 
     *        (values {@code <=} 0 are ignored)
     */
    public void setPollingInterval(long interval) {
        if (interval > 0) {
            pollingInterval = interval;
        }
    }

    /**
     * Gets the interval between polling the executing jobs.
     *
     * @return interval in milliseconds
     */
    public long getPollingInterval() {
        return pollingInterval;
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
    public int submit(final Jiffle jiffle, Map<String, RenderedImage> images)
            throws JiffleExecutorException {

        if (!jiffle.isCompiled()) {
            throw new JiffleExecutorException("Jiffle object not compiled" + jiffle.getName());
        }
        
        int id = ++jobID;
        startPolling();
        jobs.put(id, threadPool.submit(new JiffleExecutorTask(id, this, jiffle, images)));
        return id;
    }
    
    private void startPolling() {
        watchExecutor.scheduleAtFixedRate( new Runnable() {
                    public void run() {
                        pollJobs();
                    }
        }, pollingInterval, pollingInterval, TimeUnit.MILLISECONDS);
    }
    
    private void pollJobs() {
        jobsDone.clear();
        
        for (Integer id : jobs.keySet()) {
            if (jobs.get(id).isDone()) {
                jobsDone.add(id);
            }
        }

        for (Integer id : jobsDone) {
            Future<JiffleExecutorResult> future = jobs.remove(id);
            try {
                JiffleExecutorResult result = future.get();
                switch (result.getStatus()) {
                    case COMPLETED:
                        fireCompletionEvent(result);
                        break;
                        
                    case FAILED:
                        fireFailureEvent(result);
                        break;
                }
                
            } catch (Exception ex) {
                throw new IllegalStateException("When getting job result", ex);
            }
        }
    }

    private void fireCompletionEvent(JiffleExecutorResult result) {
        JiffleCompletionEvent ev = new JiffleCompletionEvent(result);
        for (JiffleEventListener el : listeners) {
            el.onCompletionEvent(ev);
        }
    }

    private void fireFailureEvent(JiffleExecutorResult result) {
        JiffleFailureEvent ev = new JiffleFailureEvent(result);
        for (JiffleEventListener el : listeners) {
            el.onFailureEvent(ev);
        }
    }
    
    /*
    void onTaskStatusEvent(JiffleExecutorTask task) {
        if (task.isCompleted()) {
            fireCompletionEvent(task);
        } else {
            fireFailureEvent(task);
        }
    }
    
    void onTaskProgressEvent(JiffleExecutorTask task, float progress) {
        fireProgressEvent(task, progress);
    }
    */
    /*
    private void fireProgressEvent(JiffleExecutorTask task, float progress) {
        JiffleProgressEvent ev = new JiffleProgressEvent(task.getId(), task.getJiffle(), progress);
        for (JiffleEventListener el : listeners) {
            el.onProgressEvent(ev);
        }
    }
     * 
     */
}
