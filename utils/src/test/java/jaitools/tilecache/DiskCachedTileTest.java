/*
 * Copyright 2009 Michael Bedward
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

package jaitools.tilecache;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.ComponentSampleModel;
import java.awt.image.DataBuffer;
import java.awt.image.Raster;
import java.awt.image.RenderedImage;
import java.awt.image.SampleModel;
import java.awt.image.WritableRaster;
import java.io.File;
import javax.media.jai.TiledImage;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for {@code DiskCachedTile}.
 *
 * @author Michael Bedward
 * @since 1.0
 * @source $URL$
 * @version $Id$
 */
public class DiskCachedTileTest {

    private static final int TILE_WIDTH = 64;

    private static final WritableRaster raster;
    private static final RenderedImage image;

    static {
        ColorModel cm = ColorModel.getRGBdefault();
        SampleModel sm = cm.createCompatibleSampleModel(TILE_WIDTH, TILE_WIDTH);

        raster = Raster.createWritableRaster(sm, new Point(0, 0));
        image = new BufferedImage(cm, raster, false, null);
    }


    @Test
    public void testGetCacheFolder() throws Exception {
        System.out.println("   getCacheFolder");

        assertNull(DiskCachedTile.getCacheFolder());
    }

    @Test
    public void testSetCacheFolder() {
        System.out.println("   setCacheFolder");
        
        File oldFolder = DiskCachedTile.getCacheFolder();
        
        File newFolder = new File("/foo/bar");
        DiskCachedTile.setCacheFolder(newFolder);

        assertEquals(newFolder, DiskCachedTile.getCacheFolder());

        DiskCachedTile.setCacheFolder(oldFolder);
    }

    @Test
    public void testGetTile() throws Exception {
        System.out.println("   getTile (testing for exception)");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        boolean gotException = false;
        try {
            Raster result = instance.getTile();
        } catch (UnsupportedOperationException uoe) {
            gotException = true;
        }

        assertTrue(gotException);
    }

    @Test
    public void testGetOwner() throws Exception {
        System.out.println("   getOwner");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        RenderedImage result = instance.getOwner();
        assertEquals(result, image);
    }

    @Test
    public void testGetTileTimeStamp() throws Exception {
        System.out.println("   getTileTimeStamp");

        long t0 = System.currentTimeMillis();

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        long t1 = System.currentTimeMillis();

        long t = instance.getTileTimeStamp();
        assertTrue(t >= t0 && t <= t1);
    }

    @Test
    public void testGetTileSize() throws Exception {
        System.out.println("   getTileSize");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        long dataTypeSize = DataBuffer.getDataTypeSize(raster.getSampleModel().getDataType()) / 8;
        long expResult = dataTypeSize * TILE_WIDTH * TILE_WIDTH;
        long result = instance.getTileSize();

        assertEquals(expResult, result);
    }

    /**
     * Test of cachedToDisk method, of class DiskCachedTile.
     */
    @Test
    public void testCachedToDisk() throws Exception {
        System.out.println("   cachedToDisk");

        DiskCachedTile instance1 = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        assertFalse(instance1.cachedToDisk());

        DiskCachedTile instance2 = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, true, null);

        assertTrue(instance2.cachedToDisk());

        instance1.writeData(raster);
        assertTrue(instance1.cachedToDisk());
    }

    @Test
    public void testDeleteDiskCopy() throws Exception {
        System.out.println("   deleteDiskCopy");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, true, null);

        File file = instance.getFile();
        assertTrue(file != null);
        assertTrue(file.exists());

        instance.deleteDiskCopy();
        assertFalse(file.exists());
    }

    @Test
    public void testGetTileId() throws Exception {
        System.out.println("   getTileId");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        Object expResult = Integer.valueOf(1234);
        Object result = instance.getTileId();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetLocation() throws Exception {
        System.out.println("   getLocation");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        Point result = instance.getLocation();
        assertTrue(result.x == 0 && result.y == 0);
    }

    @Test
    public void testGetTileX() throws Exception {
        System.out.println("   getTileX");

        Point p = new Point(1, 2);
        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, p.x, p.y, raster, false, null);

        int result = instance.getTileX();
        assertEquals(p.x, result);
    }

    @Test
    public void testGetTileY() throws Exception {
        System.out.println("   getTileY");

        Point p = new Point(1, 2);
        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, p.x, p.y, raster, false, null);

        int result = instance.getTileY();
        assertEquals(p.y, result);
    }

    @Test
    public void testIsWritable() throws Exception {
        System.out.println("   isWritable");

        DiskCachedTile instance = new DiskCachedTile(
                Integer.valueOf(1234), image, 0, 0, raster, false, null);

        assertTrue(instance.isWritable());
    }

    //@Ignore
    @Test
    public void testReadWrite() throws Exception {
        System.out.println("   writing and reading data");

        testReadWriteImageType(DataBuffer.TYPE_BYTE, "TYPE_BYTE");
        testReadWriteImageType(DataBuffer.TYPE_DOUBLE, "TYPE_DOUBLE");
        testReadWriteImageType(DataBuffer.TYPE_FLOAT, "TYPE_FLOAT");
        testReadWriteImageType(DataBuffer.TYPE_INT, "TYPE_INT");
        testReadWriteImageType(DataBuffer.TYPE_SHORT, "TYPE_SHORT");
        testReadWriteImageType(DataBuffer.TYPE_USHORT, "TYPE_USHORT");
    }

    private void testReadWriteImageType(int dataType, String typeName) throws Exception {
        System.out.println("      " + typeName);

        final int w = 10;
        SampleModel sm = new ComponentSampleModel(dataType, w, w, 1, w, new int[]{0});
        TiledImage img = new TiledImage(0, 0, w, w, 0, 0, sm, null);
        WritableRaster r = img.getWritableTile(0, 0);

        int k = 0;
        for (int y = 0; y < w; y++) {
            for (int x = 0; x < w; x++) {
                r.setSample(x, y, 0, k++);
            }
        }

        DiskCachedTile instance = new DiskCachedTile(0, img, 0, 0, r, true, null);
        Raster read = instance.readData();

        assertNotNull(read);
        assertTrue(read.getWidth() == w && read.getHeight() == w);
        assertTrue(read.getSampleModel().getDataType() == dataType);

        k = 0;
        for (int y = 0; y < w; y++) {
            for (int x = 0; x < w; x++) {
                assertEquals(k, read.getSample(x, y, 0));
                k++ ;
            }
        }
    }

}