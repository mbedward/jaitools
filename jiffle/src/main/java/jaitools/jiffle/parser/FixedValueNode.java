/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.parser;

import org.antlr.runtime.CommonToken;
import org.antlr.runtime.tree.CommonTree;
import org.antlr.runtime.tree.Tree;

/**
 *
 * @author Michael Bedward and Murray Ellis
 */
public class FixedValueNode extends CommonTree {
    private double value;
    
    public FixedValueNode(int tokType, double value) { 
        super(new CommonToken(tokType));
        this.value = value; 
    }
    
    public FixedValueNode(int tokType, String text) {
        this(tokType, Double.valueOf(text));
    }
    
    public double getValue() { return value; }
    
    @Override
    public FixedValueNode dupNode() {
        return new FixedValueNode(getType(), value);
    }    
    
    @Override
    public String toString() {
        return "FIX<" + String.valueOf(value) + ">";
    }
}
