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

package jaitools.jiffle.parser;

/**
 * Used by Jiffle parsers to complain about unexpected input in a acript.
 *
 * @author Michael Bedward
 * @since 1.1
 * @source $URL: https://jai-tools.googlecode.com/svn/trunk/jiffle/src/test/java/jaitools/jiffle/parser/HeaderBlockParsingTest.java $
 * @version $Id: HeaderBlockParsingTest.java 1374 2011-02-09 00:33:31Z michael.bedward $
 */
public class UnexpectedInputException extends RuntimeException {
    public UnexpectedInputException(String msg) {
        super(msg);
    }
}

