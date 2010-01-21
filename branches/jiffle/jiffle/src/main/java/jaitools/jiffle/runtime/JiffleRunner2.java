/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.jiffle.runtime;

import jaitools.jiffle.Jiffle;
import java.awt.Rectangle;

/**
 * @author Michael Bedward
 */
public class JiffleRunner2 extends JiffleRunner implements IJiffleRunner {

    public JiffleRunner2(Jiffle jiffle) throws JiffleInterpreterException {
        super(jiffle);
    }

    public boolean run(int tileX, int tileY) {
        return true;
    }

    public boolean run(Rectangle bounds) {
        return true;
    }

}
