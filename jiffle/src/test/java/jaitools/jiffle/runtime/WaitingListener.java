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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;

/**
 * An event listener with a wait function to allow unit tests to work with
 * JiffleExecutor tasks.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @source $URL$
 * @version $Id$
 */
public class WaitingListener implements JiffleEventListener {
    
    private CountDownLatch latch;

    private final Map<Integer, JiffleExecutorResult> results = 
            new ConcurrentHashMap<Integer, JiffleExecutorResult>();
    
    /**
     * Sets the number of job completions and/or failures to wait for.
     * 
     * @param n number of jobs
     */
    public synchronized void setNumJobs(int n) {
        if (latch != null && latch.getCount() > 0) {
            throw new IllegalStateException("Method called during wait period");
        }

        latch = new CountDownLatch(n);
    }
    
    /**
     * Waits for jobs to finish.
     */
    public void await() {
        try {
            latch.await();
        } catch (InterruptedException ignored) {}
    }
    
    public synchronized JiffleExecutorResult getResult(int jobID) {
        return results.get(jobID);
    }

    public void onCompletionEvent(JiffleEvent ev) {
        latch.countDown();
        JiffleExecutorResult result = ev.getResult();
        results.put(result.getJobID(), result);
    }

    public void onFailureEvent(JiffleEvent ev) {
        latch.countDown();
        JiffleExecutorResult result = ev.getResult();
        results.put(result.getJobID(), result);
    }

    public void onProgressEvent(float progress) {
    }

}
