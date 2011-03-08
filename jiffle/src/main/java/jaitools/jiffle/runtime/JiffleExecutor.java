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

import java.awt.image.RenderedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import jaitools.DaemonThreadFactory;
import jaitools.jiffle.Jiffle;
import jaitools.jiffle.JiffleException;


/**
 * A multi-threaded, event-driven executor service for Jiffle scripts. Jiffle
 * objects can be submitted to this class to execute rather than the client 
 * working with a JiffleRuntime instance directly. This is preferable
 * for computationally demanding tasks because the scripts run in a separate
 * thread to the client. For multiple tasks, the executor can be set up to
 * run all tasks concurrently in separate threads (if system resources permit),
 * or run up to N tasks concurrently. If necessary, a task will be held in a 
 * queue while waiting for a thread. Setting N to 1 gives the option of serial
 * execution.
 * <p>
 * The client can optionally follow progress during execution with a 
 * {@link jaitools.jiffle.runtime.JiffleProgressListener}. When the task is
 * finished its status and results can be retrieved via {@link JiffleEventListener}.
 * <p>
 * Example of use:
 * 
 * <pre><code>
 * // assuming the executor is a class field in this example
 * executor = new JiffleExecutor();
 *
 * executor.addEventListener(new JiffleEventListener() {
 *
 *     public void onCompletionEvent(JiffleEvent ev) {
 *         myCompletionMethod(ev);
 *     }
 *
 *     public void onFailureEvent(JiffleEvent ev) {
 *         myFailureMethod(ev);
 *     }
 * });
 * </code></pre>
 * 
 * Now we can build Jiffle objects and submit them to the executor as shown here:
 * 
 * <pre><code>
 * String script = "dest = src > 10 ? src : null;" ;
 *
 * Map&lt;String, Jiffle.ImageRole&gt; imageParams = CollectionFactory.map();
 * imageParams.put("src", Jiffle.ImageRole.SOURCE);
 * imageParams.put("dest", Jiffle.ImageRole.DEST);
 *
 * Jiffle jiffle = new Jiffle(script, imageParams);
 *
 * // Map with the source and destination images
 * RenderedImage sourceImg = ...
 * WritableRenderedImage destImg = ...
 * Map&lt;String, RenderedImage&gt; images = CollectionFactory.map();
 * images.put("src", sourceImg);
 * images.put("dest", destImg);
 *
 * // Submit the task to the executor
 * executor.submit(jiffle, images, new MyProgressListener());
 * </code></pre>
 * 
 * When the script has completed the event listener will be notified and
 * the results can be retrieved:
 * 
 * <pre><code>
 * private void myCompletionMethod(JiffleEvent ev) {
 *     // Get and display the result image
 *     JiffleExecutorResult result = ev.getResult();
 *     RenderedImage img = result.getImages().get("dest");
 *     ...
 * }
 *
 * private void myFailureMethod(JiffleEvent ev) {
 *     System.out.println("Bummer...");
 * }
 * </code></pre>
 * 
 * Once the application has finished with th executor it should call one of
 * the shutdown methods which terminate the task and polling threads.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */public class JiffleExecutor {
    
    private static final Logger LOGGER = Logger.getLogger(JiffleExecutor.class.getName());

    // Templates for log INFO messages
    private static final String TASK_SUBMITTED_MSG = "Task {0} submitted";
    private static final String TASK_SUCCESS_MSG = "Task {0} completed";
    private static final String TASK_FAILURE_MSG = "Task {0} failed";
    
    
    /** 
     * The default interval for polling tasks to check for
     * completion (20 mS)
     */
    public static final long DEFAULT_POLLING_INTERVAL = 20L;

    private long pollingInterval = DEFAULT_POLLING_INTERVAL;
    
    /* Provides unique job ID values across all executor instances. */
    private static final AtomicInteger jobID = new AtomicInteger(0);

    private final Object _lock = new Object();
    
    private final ExecutorService taskService;
    private final ScheduledExecutorService pollingService;
    private final ScheduledExecutorService shutdownService;
    private final ExecutorCompletionService<JiffleExecutorResult> completionService;
    
    private final List<JiffleEventListener> listeners;
    
    private final AtomicBoolean isPolling = new AtomicBoolean(false);
    private final AtomicInteger numTasksRunning = new AtomicInteger(0);
    
    /* Used by constructors when setting up the task service. */
    private static enum ThreadPoolType {
        CACHED,
        FIXED;
    }

    
    /**
     * Creates an executor with default settings. There is no upper limit 
     * on the number of concurrent tasks. A cached thread pool will be used
     * which recycles existing threads where possible.
     */
    public JiffleExecutor() {
        this(ThreadPoolType.CACHED, -1);
    }
    
    
    /**
     * Creates an executor that can have, at most,{@code maxTasks} 
     * running concurrently, with further tasks being placed in a queue.
     * 
     * @param maxTasks the maximum number of concurrent tasks
     */
    public JiffleExecutor(int maxTasks) {
        this(ThreadPoolType.FIXED, maxTasks);
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
                taskService = Executors.newCachedThreadPool();
                break;
                
            case FIXED:
                taskService = Executors.newFixedThreadPool(maxJobs);
                break;
                
            default:
                throw new IllegalArgumentException("Bad arg to private JiffleExecutor constructor");
        }
        
        completionService = new ExecutorCompletionService<JiffleExecutorResult>(taskService);
        
        pollingService = Executors.newSingleThreadScheduledExecutor(
                new DaemonThreadFactory(Thread.NORM_PRIORITY, "executor-poll"));
        
        shutdownService = Executors.newSingleThreadScheduledExecutor(
                new DaemonThreadFactory(Thread.NORM_PRIORITY, "executor-shutdown"));
        
        listeners = new ArrayList<JiffleEventListener>();
    }
    
    /**
     * Sets the polling interval for task completion. JiffleExecutor uses a 
     * separate thread to poll tasks for completion (either success
     * or failure) at a fixed interval. The interval can only be changed 
     * prior to submitting the first task. After that, any calls to this
     * method will result in a warning message being logged and the new
     * value being ignored.
     * 
     * @param millis interval between task polling in milliseconds; values
     *        less than 1 are ignored
     * 
     * @see #DEFAULT_POLLING_INTERVAL
     */
    public void setPollingInterval(long millis) {
        synchronized (_lock) {
            if (isPolling.get()) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING,
                            "Request to change polling interval ignored");
                }
            } else if (millis < 1) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.log(Level.WARNING, "Invalid polling interval ignored: {0}", millis);
                }
            } else {
                pollingInterval = millis;
            }
        }
    }
    
    /**
     * Gets the interval in milliseconds for polling task completion.
     * 
     * @return polling interval
     */
    public long getPollingInterval() {
        synchronized (_lock) {
            return pollingInterval;
        }
    }
            
    
    /**
     * Adds an event listener.
     * 
     * @param listener the listener
     * 
     * @see {@link JiffleEvent}
     */
    public void addEventListener(JiffleEventListener listener) {
        synchronized(_lock) {
            listeners.add(listener);
        }
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
        synchronized(_lock) {
            return listeners.remove(listener);
        }
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
        synchronized(_lock) {
            return listeners.contains(listener);
        }
    }
    
    /**
     * Submits an {@code Jiffle} object for execution. If the script is not
     * already compiled the executor will compile it. Depending on existing
     * tasks and the number of threads available to the executor there could
     * be a delay before the task starts. Clients can receive notification 
     * via an optional progress listener.
     * <p>
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

        synchronized(_lock) {
            if (taskService.isShutdown()) {
                throw new IllegalStateException("Submitting task after executor shutdown");
            }
            
            try {
                if (!jiffle.isCompiled()) {
                    jiffle.compile();
                }
            } catch (JiffleException ex) {
                throw new JiffleExecutorException(ex);
            }

            startPolling();
            
            int id = jobID.getAndIncrement();
            
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.log(Level.INFO, TASK_SUBMITTED_MSG, id);
            }

            numTasksRunning.incrementAndGet();
            completionService.submit( new JiffleExecutorTask(
                    this, id, jiffle, images, progressListener));

            return id;
        }
    }
    
    /**
     * Requests that the executor shutdown after completing any tasks
     * already submitted. Control returns immediately to the client.
     */
    public void shutdown() {
        synchronized(_lock) {
            taskService.shutdown();
            stopPolling(false);
        }
    }
    
    /**
     * Requests that the executor shutdown after completing any tasks
     * already submitted. Control returns to the calling thread after
     * the executor has shutdown or the time out period has elapsed, 
     * whichever comes first.
     * 
     * @param timeOut time-out period
     * @param unit time unit
     * 
     * @return {@code true} if the executor has shutdown; {@code false} if
     *         the time-out period elapsed or the thread was interrupted
     */
    public boolean shutdownAndWait(long timeOut, TimeUnit unit) {
        synchronized (_lock) {
            boolean success = false;
            taskService.shutdown();
            stopPolling(false);
            
            try {
                success = taskService.awaitTermination(timeOut, unit);
                
            } catch (InterruptedException ex) {
                throw new RuntimeException(ex);
            }

            return success;
        }
    }
    
    /**
     * Attempts to shutdown the executor immediately.
     */
    public void shutdownNow() {
        taskService.shutdownNow();
        stopPolling(true);
    }
        
    /**
     * Starts the polling service if it is not already running.
     */
    private void startPolling() {
        if (!isPolling.get()) {
            pollingService.scheduleWithFixedDelay(new PollingTask(),
                    pollingInterval, pollingInterval, TimeUnit.MILLISECONDS);
            isPolling.set(true);
        }
    }
    
    /**
     * Stops the polling service.
     * 
     * @param immediate whether to stop the service immediately or wait
     *        for any running tasks to complete
     */
    private void stopPolling(boolean immediate) {
        if (immediate) {
            pollingService.shutdown();
            return;
        }

        shutdownService.scheduleAtFixedRate(new Runnable() {
            public void run() {
                if (numTasksRunning.get() == 0) {
                    pollingService.shutdown();
                    shutdownService.shutdown();
                }
            }
        }, pollingInterval, pollingInterval, TimeUnit.MILLISECONDS);
    }
    
    private class PollingTask implements Runnable {
        public void run() {
            try {
                Future<JiffleExecutorResult> future = completionService.poll();
                if (future != null) {
                    JiffleExecutorResult result = future.get();
                    numTasksRunning.decrementAndGet();
                    
                    if (result.isCompleted()) {
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.log(Level.INFO, TASK_SUCCESS_MSG, result.getTaskID());
                        }
                        notifySuccess(result);
                        
                    } else {
                        if (LOGGER.isLoggable(Level.INFO)) {
                            LOGGER.log(Level.INFO, TASK_FAILURE_MSG, result.getTaskID());
                        }
                        notifyFailure(result);
                    }
                }

            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        private void notifySuccess(JiffleExecutorResult result) {
            for (JiffleEventListener listener : listeners) {
                listener.onCompletionEvent(new JiffleEvent(result));
            }
        }

        private void notifyFailure(JiffleExecutorResult result) {
            for (JiffleEventListener listener : listeners) {
                listener.onFailureEvent(new JiffleEvent(result));
            }
        }
    }
    
}
