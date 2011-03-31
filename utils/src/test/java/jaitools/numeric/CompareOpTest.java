/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.numeric;

import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author michael
 */
public class CompareOpTest {
    
    @Test
    public void doubleIsZero() {
        double x = CompareOp.DTOL / 2;
        
        assertTrue(CompareOp.isZero(x));
        assertTrue(CompareOp.isZero(-x));
        
        x = CompareOp.DTOL * 2;
        assertFalse(CompareOp.isZero(x));
    }

    @Test
    public void floatIsZero() {
        float x = CompareOp.FTOL / 2;
        
        assertTrue(CompareOp.isZero(x));
        assertTrue(CompareOp.isZero(-x));
        
        x = CompareOp.FTOL * 2;
        assertFalse(CompareOp.isZero(x));
    }

    @Test
    public void doubleIsZeroWithTol() {
        double tol = CompareOp.DTOL * 100;

        double x = tol / 2;
        assertTrue(CompareOp.isZero(x, tol));
        
        x = tol * 2;
        assertFalse(CompareOp.isZero(x, tol));
    }

    @Test
    public void floatIsZeroWithTol() {
        float tol = CompareOp.FTOL * 100;

        float x = tol / 2;
        assertTrue(CompareOp.isZero(x, tol));
        
        x = tol * 2;
        assertFalse(CompareOp.isZero(x, tol));
    }
    
    @Test
    public void acompareDoubles() {
        double x1 = 100;
        
        double x2 = x1 - CompareOp.DTOL / 10;
        assertEquals(0, CompareOp.acompare(x1, x2));
        
        x2 = x1 - CompareOp.DTOL * 10;
        assertTrue(CompareOp.acompare(x1, x2) > 0);
        
        x2 = x1 + CompareOp.DTOL * 10;
        assertTrue(CompareOp.acompare(x1, x2) < 0);
    }

    @Test
    public void acompareFloats() {
        float x1 = 100;
        
        float x2 = x1 - CompareOp.FTOL / 10;
        assertEquals(0, CompareOp.acompare(x1, x2));
        
        x2 = x1 - CompareOp.FTOL * 10;
        assertTrue(CompareOp.acompare(x1, x2) > 0);
        
        x2 = x1 + CompareOp.FTOL * 10;
        assertTrue(CompareOp.acompare(x1, x2) < 0);
    }

    @Test
    public void acompareDoublesWithTol() {
        double tol = 0.01d;
        double x1 = 100;
        
        double x2 = x1 - tol / 10;
        assertEquals(0, CompareOp.acompare(x1, x2, tol));
        
        x2 = x1 - tol * 10;
        assertTrue(CompareOp.acompare(x1, x2, tol) > 0);
        
        x2 = x1 + tol * 10;
        assertTrue(CompareOp.acompare(x1, x2, tol) < 0);
    }

    @Test
    public void acompareFloatsWithTol() {
        float tol = 0.01f;
        float x1 = 100;
        
        float x2 = x1 - tol / 10;
        assertEquals(0, CompareOp.acompare(x1, x2, tol));
        
        x2 = x1 - tol * 10;
        assertTrue(CompareOp.acompare(x1, x2, tol) > 0);
        
        x2 = x1 + tol * 10;
        assertTrue(CompareOp.acompare(x1, x2, tol) < 0);
    }

    @Test
    public void pcompareDoubles() {
        double ptol = 0.01d;
        double x1 = 10000;

        double x2 = 0.995 * x1;
        assertEquals(0, CompareOp.pcompare(x1, x2, ptol));
        
        x2 = 0.98 * x1;
        assertTrue(CompareOp.pcompare(x1, x2, ptol) > 0);
    }

    @Test
    public void pcompareFloats() {
        float ptol = 0.01f;
        float x1 = 10000;

        float x2 = 0.995f * x1;
        assertEquals(0, CompareOp.pcompare(x1, x2, ptol));
        
        x2 = 0.98f * x1;
        assertTrue(CompareOp.pcompare(x1, x2, ptol) > 0);
    }

    @Test
    public void aequalDoubles() {
        double x1 = 42;
        double x2 = x1 + CompareOp.DTOL * 10;
        assertFalse(CompareOp.aequal(x1, x2));
        
        x2 = x1 + CompareOp.DTOL / 10;
        assertTrue(CompareOp.aequal(x1, x2));
    }

    @Test
    public void aequalFloats() {
        float x1 = 42;
        float x2 = x1 + CompareOp.FTOL * 10;
        assertFalse(CompareOp.aequal(x1, x2));
        
        x2 = x1 + CompareOp.FTOL / 10;
        assertTrue(CompareOp.aequal(x1, x2));
    }

    @Test
    public void aequalDoublesWithTol() {
        double tol = 0.1d;
        double x1 = 42;
        double x2 = x1 + tol * 10;
        assertFalse(CompareOp.aequal(x1, x2, tol));
        
        x2 = x1 + tol / 10;
        assertTrue(CompareOp.aequal(x1, x2, tol));
    }

    @Test
    public void aequalFloatsWithTol() {
        float tol = 0.1f;
        float x1 = 42;
        float x2 = x1 + tol * 10;
        assertFalse(CompareOp.aequal(x1, x2, tol));
        
        x2 = x1 + tol / 10;
        assertTrue(CompareOp.aequal(x1, x2, tol));
    }

    @Test
    public void pequalDoubles() {
        double ptol = 0.01d;
        double x1 = 10000;
        
        double x2 = 0.985 * x1;
        assertFalse(CompareOp.pequal(x1, x2, ptol));
        
        x2 = 0.995 * x1;
        assertTrue(CompareOp.pequal(x1, x2, ptol));
    }

    @Test
    public void pequalFloats() {
        float ptol = 0.01f;
        float x1 = 10000;
        
        float x2 = 0.985f * x1;
        assertFalse(CompareOp.pequal(x1, x2, ptol));
        
        x2 = 0.995f * x1;
        assertTrue(CompareOp.pequal(x1, x2, ptol));
    }
    
}
