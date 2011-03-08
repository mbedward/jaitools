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

import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.runner.notification.Failure;
import org.junit.runner.notification.RunListener;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;


/**
 * Used to drive {@link ExecutorSimpleTaskTest}. Set the field {@code N} to a 
 * high value to check for concurrency problems.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 *
 */
public class ExecutorTestRunner extends BlockJUnit4ClassRunner {
    
    final int N = 1;
    int numFailures;

    public ExecutorTestRunner(Class<?> klass) throws InitializationError {
        super(klass);
    }

    @Override
    public void run(RunNotifier notifier) {
        Logger logger = Logger.getLogger(JiffleExecutor.class.getName());
        logger.setLevel(Level.WARNING);
        
        notifier.addListener(new RunListener() {
            @Override
            public void testFailure(Failure failure) throws Exception {
                numFailures++ ;
            }
        });
        
        for (int i = 0; i < N; i++) {
            super.run(notifier);
        }
        
        System.out.printf("%d failures in %d runs\n", numFailures, N);
    }
    
    
}
