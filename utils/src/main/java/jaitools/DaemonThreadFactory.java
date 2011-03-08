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

package jaitools;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Simple ThreadFactory implementation to supply daemon threads with 
 * specified priority. Used by JAI-tools classes that run polling services
 * on background threads to avoid blocking application exit.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class DaemonThreadFactory implements ThreadFactory {

    private static final String DEFAULT_ROOT_NAME = "daemon-";
    
    private final ReentrantLock lock = new ReentrantLock();
    private static final AtomicInteger threadCount = new AtomicInteger(0);
    private final int priority;
    private final String rootName;

    
    /**
     * Creates a new factory which will supply daemon threads having
     * normal priority.
     */
    public DaemonThreadFactory() {
        this(Thread.NORM_PRIORITY, DEFAULT_ROOT_NAME);
    }
    
    /**
     * Creates a new factory which will supply daemon threads to run
     * at the specified priority.
     * 
     * @param priority thread priority
     */
    public DaemonThreadFactory(int priority) {
        this(priority, DEFAULT_ROOT_NAME);
    }
    
    
    /**
     * Creates a new factory which will supply daemon threads to run
     * at the specified priority. Threads will be named {@code rootName-n}
     * where {@code n} is the count of threads produced by all instances
     * of this class.
     * 
     * @param priority thread priority
     * @param rootName root name to label threads
     */
    public DaemonThreadFactory(int priority, String rootName) {
        String s = rootName == null ? "" : rootName.trim();
        if (s.length() == 0) {
            this.rootName = DEFAULT_ROOT_NAME;
        } else if (s.endsWith("-")) {
            this.rootName = s;
        } else {
            this.rootName = s + "-";
        }
        
        this.priority = Math.min(Thread.MAX_PRIORITY, Math.max(Thread.MIN_PRIORITY, priority));
    }
    
    /**
     * Creates a new daemon thread with name and priority assigned 
     * as per the values supplied when creating this thread factory.
     * 
     * @param r target for the new thread
     * 
     * @return new thread
     */
    public Thread newThread(Runnable r) {
        lock.lock();
        try {
            Thread t = new Thread(r);
            t.setDaemon(true);
            t.setName("daemon-" + threadCount.getAndIncrement());
            t.setPriority(priority);
            return t;
        } finally {
            lock.unlock();
        }
    }

}
