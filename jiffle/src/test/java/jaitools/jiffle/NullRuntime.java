/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle;

import jaitools.jiffle.runtime.JiffleRuntime;

/**
 *
 * @author michael
 */
public class NullRuntime implements JiffleRuntime {

    public <T extends Number> T getValue(String varName) {
        return null;
    }

}
