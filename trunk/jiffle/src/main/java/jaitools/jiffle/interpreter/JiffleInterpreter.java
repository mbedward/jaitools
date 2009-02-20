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

package jaitools.jiffle.interpreter;

import jaitools.jiffle.util.CollectionFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * This class runs compiled scripts in Jiffle objects (referred to as jiffles).
 * <p>
 * When a jiffle is submitted to the interpreter it is allocated a 
 * job ID (an integer, unique across all interpreter instances). A thread
 * is then created for running the jiffle.  The status of the executing
 * jiffle is communicated via {@link JiffleEvent} objects.
 * <p>
 * Example of use:
 * <pre>{@code \u0000
 * class Foo {
 * 
 *     public Foo() {
 *         private JiffleInterpreter interp = new JiffleInterpreter();
 *         interp.addEventListener(new JiffleEventAdapter() {
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
 *             interp.submit(j);
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
 * }</pre>
 * 
 * @author Michael Bedward
 */
public class JiffleInterpreter {
    
    private static ExecutorService threadPool;
    private static int jobID = 0;
    
    private Map<Integer, Future<?>> jobs; 
    private List<JiffleEventListener> listeners;
    
    /**
     * Constructor
     */
    public JiffleInterpreter() {
        jobs = CollectionFactory.newMap();
        listeners = CollectionFactory.newList();
    }

    /**
     * Add a listener for interpreter events
     * @param listener the listening object
     * @see {@link JiffleEvent}
     */
    public void addEventListener(JiffleEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Submit a compiled Jiffle to the interpreter
     * @param j the compiled jiffle
     * @return the job ID that can be used to query this job's status
     */
    public int submit(final Jiffle j) throws JiffleInterpreterException {
        if (threadPool == null) {
            threadPool = Executors.newCachedThreadPool();
        }

        int id = ++jobID;
        jobs.put(id, threadPool.submit(new JiffleTask(id, this, j)));
        return id;
    }

    /**
     * Package private method to receive events from running tasks
     * @param task the task sending the event
     */
    void onTaskEvent(JiffleTask task) {
        if (task.isCompleted()) {
            fireCompletionEvent(task);
        } else {
            fireFailureEvent(task);
        }
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
}
