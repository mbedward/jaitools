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

package jaitools.jiffle.runtime;

import java.awt.image.RenderedImage;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import jaitools.CollectionFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;

/**
 * Executes Jiffle scripts on separate threads.
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
    private final List<Integer> jobsDone;
    
    /** Thread pool used to execute Jiffle runtime objects */
    private final ExecutorService threadPool;
    
    /** Scheduled executor to run job polling */
    private final ScheduledExecutorService pollingExecutor;
    private ScheduledFuture<?> poll;

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
        pollingExecutor = Executors.newSingleThreadScheduledExecutor();
        pollingInterval = DEFAULT_POLLING_INTERVAL;
        jobs = new ConcurrentHashMap<Integer, Future<JiffleExecutorResult>>();
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
     * Removes an event listener.
     * 
     * @param listener the listener
     * 
     * @return {@code true} if the listener was removed;
     *         {@code false} if it was not registered with this executor
     */
    public boolean removeEventListener(JiffleEventListener listener) {
        return listeners.remove(listener);
    }
    
    /**
     * Checks if a particular listener is registered with this executor.
     * 
     * @param listener the listener
     * 
     * @return {@code true} if the listener has already been added;
     *         {@code false} otherwise
     */
    public boolean isListening(JiffleEventListener listener) {
        return listeners.contains(listener);
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
     * @param images source and destination images as a {@code Map} with
     *        keys being image variable names as used in the Jiffle script
     *        and image parameters
     * 
     * @param progressListener an optional progress listener (may be {@code null})
     * 
     * @return the job ID that can be used to query progress
     * 
     * @throws JiffleExecutorException if the {@code Jiffle} object was not
     *         compiled correctly
     */
    public int submit(Jiffle jiffle, 
            Map<String, RenderedImage> images,
            JiffleProgressListener progressListener)
            throws JiffleExecutorException {

        try {
            if (!jiffle.isCompiled()) {
                jiffle.compile();
            }
        } catch (JiffleException ex) {
            throw new JiffleExecutorException(ex);
        }
        
        int id = ++jobID;
        startPolling();
        jobs.put(id, threadPool.submit(
                new JiffleExecutorTask(id, jiffle, images, progressListener)));
        
        return id;
    }
    
    private void startPolling() {
        poll = pollingExecutor.scheduleAtFixedRate( new Runnable() {
                    public void run() {
                        pollJobs();
                    }
        }, pollingInterval, pollingInterval, TimeUnit.MILLISECONDS);
    }

    private void pollJobs() {
        synchronized (jobsDone) {
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

            if (jobs.isEmpty()) {
                poll.cancel(true);
            }
        }
    }

    private void fireCompletionEvent(JiffleExecutorResult result) {
        JiffleEvent ev = new JiffleEvent(result);
        for (JiffleEventListener el : listeners) {
            el.onCompletionEvent(ev);
        }
    }

    private void fireFailureEvent(JiffleExecutorResult result) {
        JiffleEvent ev = new JiffleEvent(result);
        for (JiffleEventListener el : listeners) {
            el.onFailureEvent(ev);
        }
    }
    
}
