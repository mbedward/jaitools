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

/**
 * Jiffle parser and support classes. In addition to the hand-written classes
 * in this package, further parser classes are generated from the ANTLR grammars
 * that define the language syntax and steps in Abstract Syntax Tree (AST)
 * construction.
 * <p>
 * The Jiffle compiler works by first creating an AST from the input Jiffle
 * script. Next, a number of tree parsers check the AST for semantic errors.
 * This is followed by tree rewriting, in which the AST is re-structured to
 * make it more suitable for the final step: runtime class source code generation.
 * The resulting source code can be retrieved by the client and/or compiled into
 * executable bytecode by Jiffle's embedded Janino compiler.
 */

package jaitools.jiffle.parser;
