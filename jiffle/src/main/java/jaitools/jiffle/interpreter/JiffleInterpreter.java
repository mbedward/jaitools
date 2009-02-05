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

import jaitools.jiffle.collection.CollectionFactory;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
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

    public void addListener(JiffleEventListener listener) {
        listeners.add(listener);
    }

    /**
     * Submit a compiled Jiffle to the interpreter
     * @param j the compiled jiffle
     * @return the job ID that can be used to query this job's status
     */
    public int submit(final Jiffle j) {
        if (threadPool == null) {
            threadPool = Executors.newCachedThreadPool();
        }

        int id = ++jobID;
        jobs.put(id, threadPool.submit(new JiffleRunnable(j)));
        return id;
    }

}
