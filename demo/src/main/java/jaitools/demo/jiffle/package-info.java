/*
 * Copyright 2009-2010 Michael Bedward
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

/**
 * Example applications for the Jiffle scripting language. 
 * <p>
 * To see the various example scripts in action run the
 * {@code JiffleDemo} program. It displays a script in a
 * text window and the image produced by it in an adjacent
 * window.
 * <p>
 * {@code JiffleBuilderDemo} shows how to compile and run scripts
 * with {@code JiffleBuilder}. This is the easiest way to get
 * started.
 * <p>
 * {@code DirectRuntimeDemo} demonstrates creating a Jiffle object
 * directly, compiling a script, retrieving a runtime object and
 * the using this to process images.
 * <p>
 * {@code JiffleExecutorDemo} shows how to run Jiffle objects using
 * a multi-threaded, event-driven executor service. This is especially
 * useful for computationally intensive tasks.
 * <p>
 * {@code ProgressListenerDemo} shows how to use a progress listener
 * with {@code JiffleExecutor} when running long tasks.
 * <p>
 * {@code GameOfLife} is a more advanced example which uses Jiffle
 * runtime objects to drive a simulation where the output image of each
 * step becomes the input image of the next step.
 */

package jaitools.demo.jiffle;
