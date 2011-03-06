/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package jaitools.jiffle.runtime;

/**
 *
 * @author michael
 */
class MockJiffleRuntime extends AbstractDirectRuntime {

    private final long pixelTime;

    public MockJiffleRuntime(int imageWidth, long pixelTime) {
        this.pixelTime = pixelTime;
        
        // super class fields
        this._width = imageWidth;
        this._height = 1;
        this._minx = 0;
        this._miny = 0;
    }


    @Override
    protected void initImageScopeVars() {
    }

    @Override
    protected void initOptionVars() {
    }

    /**
     * Pretends to process a pixel (very slowly).
     */
    public void evaluate(int x, int y) {
        try {
            Thread.sleep(pixelTime);

        } catch (InterruptedException ex) {
            throw new RuntimeException(ex);
        }
    }
}