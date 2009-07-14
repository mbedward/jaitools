/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package jaitools.tilecache;

import jaitools.utils.CollectionFactory;
import java.awt.RenderingHints;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import javax.media.jai.ImageLayout;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;

/**
 * Support class for unit tests in the jaitools.tilecache package
 *
 * @author Michael Bedward
 * @since 1.0
 * $Id$
 */
public class TileCacheTestHelper implements Observer {

    private static final int TILE_WIDTH = 128;
    private RenderingHints hints;

    private List<DiskCachedTile> tiles;
    private List<DiskCachedTile> residentTiles;

    /**
     * Constructor
     */
    TileCacheTestHelper() {
        tiles = CollectionFactory.newList();
        residentTiles = CollectionFactory.newList();

        ImageLayout layout = new ImageLayout();
        layout.setTileWidth(TILE_WIDTH);
        layout.setTileHeight(TILE_WIDTH);
        hints = new RenderingHints(JAI.KEY_IMAGE_LAYOUT, layout);
    }

    /**
     * Get the memory required to store a tile's data
     * @return tile data size in bytes
     */
    long getTileMemSize() {
        return (long)TILE_WIDTH * TILE_WIDTH * (Double.SIZE / 8);
    }

    /**
     * Begin observing a cache
     */
    void startObserving(DiskMemTileCache cache) {
        cache.addObserver(this);
        cache.setDiagnostics(true);
    }

    /**
     * Stop observing a cache
     */
    void stopObserving(DiskMemTileCache cache) {
        cache.deleteObserver(this);
        cache.setDiagnostics(false);
        tiles.clear();
        residentTiles.clear();
    }

    /**
     * Creates a simple JAI rendering chain for a single band image
     * that will require use of the cache
     *
     * @param numXTiles image width as number of tiles
     * @param numYTiles image height as number of tiles
     * @return a new RenderedOp instance
     */
    RenderedOp simpleJAIOp(int numXTiles, int numYTiles) {
        /*
         * First node in a simple rendering chain: create
         * a constant image, 3 tiles x 2 tiles
         */
        ParameterBlockJAI pb = new ParameterBlockJAI("Constant");
        pb.setParameter("bandValues", new Double[]{1.0d});
        pb.setParameter("width", (float)TILE_WIDTH * numXTiles);
        pb.setParameter("height", (float)TILE_WIDTH * numYTiles);
        RenderedOp op1 = JAI.create("constant", pb, hints);

        /*
         * Second node: multiply the constant image by a constant
         */
        pb = new ParameterBlockJAI("MultiplyConst");
        pb.setSource("source0", op1);
        pb.setParameter("constants", new double[]{2.0d});
        RenderedOp op2 = JAI.create("MultiplyConst", pb, hints);

        return op2;
    }

    Collection<DiskCachedTile> getTiles() {
        return Collections.unmodifiableCollection(tiles);
    }

    Collection<DiskCachedTile> getResidentTiles() {
        return Collections.unmodifiableCollection(residentTiles);
    }

    /**
     * Observer method to receive cache events
     * @param ocache the tile cache
     * @param otile a cached tile
     */
    public void update(Observable ocache, Object otile) {
        DiskCachedTile tile = (DiskCachedTile)otile;

        int actionValue = tile.getAction();
        switch (DiskCachedTile.TileAction.get(actionValue)) {
            case ACTION_ACCESSED:
                break;

            case ACTION_ADDED:
                tiles.add(tile);
                break;

            case ACTION_ADDED_RESIDENT:
                tiles.add(tile);
                residentTiles.add(tile);
                break;

            case ACTION_RESIDENT:
                residentTiles.add(tile);
                break;

            case ACTION_NON_RESIDENT:
                residentTiles.remove(tile);
                break;

            case ACTION_REMOVED:
                tiles.remove(tile);
                residentTiles.remove(tile);
                break;
        }
    }

}
