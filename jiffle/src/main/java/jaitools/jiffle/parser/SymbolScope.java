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

import jaitools.CollectionFactory;
import java.util.Collections;
import java.util.List;

/**
 * Stores symbols in a Jiffle script at a single scope level. Used during
 * script compilation.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SymbolScope {

    private final String name;
    private final List<Symbol> symbols;

    /**
     * Creates a new scope.
     * 
     * @param name a scope label
     */
    public SymbolScope(String name) {
        this.name = name;
        this.symbols = CollectionFactory.list();
    }

    /**
     * Gets the scope label.
     * 
     * @return scope label
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the list of symbols in this scope. The list
     * is returned as an unmodifiable view.
     * 
     * @return list of symbols
     */
    public List<Symbol> getSymbols() {
        return Collections.unmodifiableList(symbols);
    }
    
    /**
     * Tests if this scope is empty.
     * 
     * @return {@code true} if there are no symbols; {@code false} otherwise
     */
    public boolean isEmpty() {
        return symbols.isEmpty();
    }
    
    /**
     * Gets the number of symbols in this scope.
     * 
     * @return number of symbols
     */
    public int size() {
        return symbols.size();
    }
    
    /**
     * Adds a symbol to this scope.
     * 
     * @param symbol the symbol
     * @throws IllegalArgumentException if {@code symbol} is {@code null}
     */
    public void add(Symbol symbol) {
        if (symbol == null) {
            throw new IllegalArgumentException("symbol must not be null");
        }
        symbols.add(symbol);
    }

    /**
     * Tests if this scope contains a symbol with the given name.
     * 
     * @param name symbol name
     * @return {@code true} if a symbol with this name is found; 
     *         {@code false} otherwise
     */
    public boolean hasSymbolNamed(String name) {
        return findSymbolNamed(name) != null;
    }
    
    /**
     * Gets the symbol with the given name if one exists.
     * 
     * @param name symbol name
     * @return the symbol or {@code null} if not match was found
     */
    public Symbol findSymbolNamed(String name) {
        for (Symbol s : symbols) {
            if (s.getName().equals(name)) {
                return s;
            }
        }
        return null;
    }
}
