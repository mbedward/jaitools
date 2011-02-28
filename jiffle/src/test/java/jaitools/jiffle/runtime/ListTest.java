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

import javax.media.jai.TiledImage;
import org.junit.Test;

/**
 * Tests for parsing list expressions.
 * 
 * @author Michael Bedward
 * @since 1.1
 * @version $Id$
 */
public class ListTest extends StatementsTestBase {

    @Test
    public void createEmptyList() throws Exception {
        System.out.println("   create empty list with []");
        String script = "foo = []; dest = 42;" ;
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 42;
            }
        };
        testScript(script, e);
    }

    @Test
    public void createInitList() throws Exception {
        System.out.println("   create list with initial values");
        String script = "foo = [1, 2, 3]; dest = 42;" ;
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 42;
            }
        };
        testScript(script, e);
    }
    
    @Test
    public void passListAsFunctionArg() throws Exception {
        System.out.println("   pass list as function arg");
        String script = "foo = [1, 3, 2]; dest = max(foo);" ;
        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return 3;
            }
        };
        testScript(script, e);
    }

    @Test
    public void appendWithOperator() throws Exception {
        System.out.println("   append to list with << operator in loop");
        String script = "options { outside=0; } \n"
                + "values = []; \n"
                + "foreach (dy in -1:1) { \n"
                + "  foreach (dx in -1:1) { \n"
                + "    values << src[dx, dy]; \n"
                + "  } \n"
                + "} \n"
                + "dest = sum(values);";
        
        assertListAppend(script);
    }
    
    @Test
    public void appendWithConcat() throws Exception {
        System.out.println("   append to list with concat in loop");
        String script = "options { outside=0; } \n"
                + "values = []; \n"
                + "foreach (dy in -1:1) { \n"
                + "  foreach (dx in -1:1) { \n"
                + "    values = concat(values, src[dx, dy]); \n"
                + "  } \n"
                + "} \n"
                + "dest = sum(values);";
        
        assertListAppend(script);
    }

    @Test
    public void prependWithConcat() throws Exception {
        System.out.println("   prepend to list with concat in loop");
        String script = "options { outside=0; } \n"
                + "values = []; \n"
                + "foreach (dy in -1:1) { \n"
                + "  foreach (dx in -1:1) { \n"
                + "    values = concat(src[dx, dy], values); \n"
                + "  } \n"
                + "} \n"
                + "dest = sum(values);";
        
        assertListAppend(script);
    }
    
    @Test
    public void concatToNewVar() throws Exception {
        System.out.println("   assign concat return to new var");
        String script = 
                  "foo = [1, 2, 3]; \n"
                + "bar = concat(foo, 4); \n"
                + "dest = src + sum(bar);" ;

        Evaluator e = new Evaluator() {
            public double eval(double val) {
                return val + 10;
            }
        };
        
        testScript(script, e);
    }

    private void assertListAppend(String script) throws Exception {
        TiledImage srcImg = createRowValueImage();
        
        Evaluator e = new Evaluator() {
            int x = 0;
            int y = 0;
            final int MAX = WIDTH - 1;
            
            public double eval(double val) {
                int sum = 0;
                int n = x == 0 || x == MAX ? 2 : 3;
                if (y > 0) sum += n*(y-1);
                sum += n*y;
                if (y < MAX) sum += n*(y+1);
                
                x = (x + 1) % WIDTH;
                if (x == 0) y++ ;
                
                return sum;
            }
        };
        
        testScript(script, srcImg, e);
    }

}
