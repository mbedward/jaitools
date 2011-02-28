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
import java.util.List;

/**
 * Used in the Jiffle tree parser grammars to track symbols defined
 * at different scopes in a script. The name of this class comes from
 * the ANTLR scope stack mechanism on which it is loosely based.
 * <p>
 * Jiffle's scoping rules are almost identical to Java. At the top level
 * of a script the accessible variables are those declared in an <b>init</b>
 * (image-scope variables) plus any declared in the script outside other blocks.
 * Within a block, the accessible variables are those at the top level plus 
 * any additional variables defined in the block itself. The latter go out of
 * scope when leaving the block. A foreach loop variable's scope is the 
 * statement or block associated with the loop.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class SymbolScopeStack {

    private final List<SymbolScope> scopes;

    /**
     * Creates a new scope stack.
     */
    public SymbolScopeStack() {
        scopes = CollectionFactory.list();
    }

    /**
     * Pushes a new scope level onto the stack. The level
     * will be given a default label.
     */
    public void addLevel() {
        addLevel("scope level " + scopes.size());
    }
    
    /**
     * Pushes a new scope level onto the stack.
     * 
     * @param label label for the new scope
     */
    public void addLevel(String label) {
        scopes.add( new SymbolScope(label) );
    }

    /**
     * Drops (ie. pops) the top level from the stack.
     * 
     * @return the level just dropped or {@code null} if the 
     *         stack was empty
     */
    public SymbolScope dropLevel() {
        if (!scopes.isEmpty()) {
            return scopes.remove(scopes.size() - 1);
        }
        return null;
    }
    
    /**
     * Adds a new symbol to the current scope level.
     * 
     * @param name symbol name
     * @param type symbol type
     * @param scopeType symbol scope 
     * @throws IllegalArgumentException if no scope levels have been added
     */
    public void addSymbol(String name, SymbolType type, ScopeType scopeType) {
        if (scopes.isEmpty()) {
            throw new IllegalStateException("Called addSymbol before adding any scope levels");
        }
        
        getTop().add(new Symbol(name, type, scopeType));
    }

    /**
     * Tests if there are any scope levels.
     * 
     * @return {@code true} if the stack is empty; {@code false} otherwise
     */
    public boolean isEmpty() {
        return scopes.isEmpty();
    }

    /**
     * Gets the number of scope levels.
     * 
     * @return number of levels
     */
    public int size() {
        return scopes.size();
    }

    /**
     * Tests if a symbol with the given name is defined within
     * the top scope or any enclosing scopes.
     * 
     * @param name symbol name
     * @return {@code true} if the symbol is found; {@code false} otherwise
     */
    public boolean isDefined(String name) {
        return findSymbol(name) != null;
    }
    
    /**
     * Tests if a symbol with the given name and type is defined within
     * the top scope or any enclosing scopes.
     * 
     * @param name symbol name
     * @param type symbol type
     * @return {@code true} if the symbol is found; {@code false} otherwise
     */
    public boolean isDefined(String name, SymbolType type) {
        Symbol symbol = findSymbol(name);
        if (symbol == null) {
            return false;
        }
        return symbol.getType() == type;
    }
    
    /**
     * Tests if a symbol with the given name and scope type is defined within
     * the top scope or any enclosing scopes.
     * 
     * @param name symbol name
     * @param type symbol scope type
     * @return {@code true} if the symbol is found; {@code false} otherwise
     */
    public boolean isDefined(String name, ScopeType scopeType) {
        Symbol symbol = findSymbol(name);
        if (symbol == null) {
            return false;
        }
        return symbol.getScopeType() == scopeType;
    }
    
    /**
     * Gets the first symbol found with the given name. The search begins
     * at the top scope and moves through any enclosing scopes.
     * 
     * @param name symbol name
     * @return the symbol or {@code null} if there was no match
     */
    public Symbol findSymbol(String name) {
        if (scopes.isEmpty()) {
            return null;
        }

        for (int i = scopes.size() - 1; i >= 0; i--) {
            Symbol s = scopes.get(i).findSymbolNamed(name);
            if (s != null) {
                return s;
            }
        }

        return null;
    }
    
    /**
     * Helper method: returns the top scope.
     */
    private SymbolScope getTop() {
        if (!scopes.isEmpty()) {
            return scopes.get( scopes.size() - 1 );
        }
        return null;
    }

}
