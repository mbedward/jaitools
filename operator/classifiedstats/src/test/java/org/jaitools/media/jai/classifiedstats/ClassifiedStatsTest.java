/* 
 *  Copyright (c) 2009, Daniele Romagnoli. All rights reserved. 
 *   
 *  Redistribution and use in source and binary forms, with or without modification, 
 *  are permitted provided that the following conditions are met: 
 *   
 *  - Redistributions of source code must retain the above copyright notice, this  
 *    list of conditions and the following disclaimer. 
 *   
 *  - Redistributions in binary form must reproduce the above copyright notice, this 
 *    list of conditions and the following disclaimer in the documentation and/or 
 *    other materials provided with the distribution.   
 *   
 *  THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND 
 *  ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED 
 *  WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE 
 *  DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR 
 *  ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES 
 *  (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; 
 *  LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON 
 *  ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT 
 *  (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS 
 *  SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE. 
 */   

package org.jaitools.media.jai.classifiedstats;

import java.awt.image.RenderedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;
import javax.media.jai.JAI;
import javax.media.jai.ParameterBlockJAI;
import javax.media.jai.RenderedOp;
import javax.media.jai.util.ImagingListener;

import org.apache.commons.collections.keyvalue.MultiKey;
import org.jaitools.CollectionFactory;
import org.jaitools.numeric.Range;
import org.jaitools.numeric.Statistic;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Unit tests for the ClassifiedStats operator
 *
 * @author Daniele Romagnoli, GeoSolutions SAS
 * @since 1.2
 */
public class ClassifiedStatsTest {

    private static final Logger LOGGER = Logger.getLogger("ClassifiedStatsTest");
    
    @BeforeClass
    public static void quiet() {
    	JAI jai = JAI.getDefaultInstance();
		final ImagingListener imagingListener = jai.getImagingListener();
    	if( imagingListener == null || imagingListener.getClass().getName().contains("ImagingListenerImpl")) {
    		jai.setImagingListener( new ImagingListener() {
				@Override
				public boolean errorOccurred(String message, Throwable thrown, Object where, boolean isRetryable)
						throws RuntimeException {
					if (message.contains("Continuing in pure Java mode")) {
						return false;
					}
					return imagingListener.errorOccurred(message, thrown, where, isRetryable);
				}    			
    		});
    	}
    }
    
    @Test
//    @Ignore
    public void testClassification() throws IOException {
        if (LOGGER.isLoggable(Level.INFO)) {
    		LOGGER.info("   test classification");
        }
        InputStream sample = null;
        InputStream classifier1 = null;
        InputStream classifierStripes = null;
        try {
            sample = ClassifiedStatsTest.class.getResourceAsStream("sample.tif");
            RenderedImage sampleImage = ImageIO.read(sample);
            classifier1 = ClassifiedStatsTest.class.getResourceAsStream("mask1.tif");
            RenderedImage classifierImage = ImageIO.read(classifier1);
            classifierStripes = ClassifiedStatsTest.class.getResourceAsStream("5stripes.tif");
            RenderedImage stripedImage = ImageIO.read(classifierStripes);
            ParameterBlockJAI pb = new ParameterBlockJAI("ClassifiedStats");
            pb.addSource(sampleImage);
            pb.setParameter("classifiers", new RenderedImage[]{stripedImage, classifierImage});
            
            pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX, Statistic.RANGE, Statistic.SUM});
            pb.setParameter("bands", new Integer[]{0});
    
            RenderedOp op = JAI.create("ClassifiedStats", pb);
            ClassifiedStats stats = (ClassifiedStats) op.getProperty(ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY);
    
            Map<MultiKey, List<Result>> results = stats.results().get(0);
            Set<MultiKey> multikeys = results.keySet();
            Iterator<MultiKey> it = multikeys.iterator();
            while (it.hasNext()) {
                MultiKey key = it.next(); 
                List<Result> rs = results.get(key);
                for (Result r: rs){
                    String s = r.toString();
                    if (LOGGER.isLoggable(Level.FINE)){
                        LOGGER.fine(s.toString());
                    }
                }
            }
            if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("Getting Max from the result coming from the 2nd stripe " +
                		"(The first classifier raster, with value = 1), " +
                		"\n and the second classifier raster with value = 50\n" + 
                		stats.band(0).statistic(Statistic.MAX).results().get(0)
                		    .get(new MultiKey(1,50)).get(0));
            }
        } finally {
            if (sample != null){
                try {
                    sample.close();
                } catch (Throwable t){
                    
                }
            }
            
            if (classifier1 != null){
                try {
                    classifier1.close();
                } catch (Throwable t){
                    
                }
            }
            
            if (classifierStripes != null){
                try {
                    classifierStripes.close();
                } catch (Throwable t){
                    
                }
            }
        }
    }
    
    
    @Test
//  @Ignore
  public void testClassificationWithPivot() throws IOException {
      if (LOGGER.isLoggable(Level.INFO)) {
              LOGGER.info("   test classification");
      }
      InputStream sample = null;
      InputStream classifier1 = null;
      InputStream classifierStripes = null;
      InputStream classifierLines = null;
      try {
          sample = ClassifiedStatsTest.class.getResourceAsStream("sample.tif");
          RenderedImage sampleImage = ImageIO.read(sample);
          classifier1 = ClassifiedStatsTest.class.getResourceAsStream("mask1.tif");
          RenderedImage classifierImage = ImageIO.read(classifier1);
          classifierStripes = ClassifiedStatsTest.class.getResourceAsStream("5stripes.tif");
          RenderedImage stripedImage = ImageIO.read(classifierStripes);
          classifierLines = ClassifiedStatsTest.class.getResourceAsStream("5lines.tif");
          RenderedImage linedImage = ImageIO.read(classifierLines);
          
          ParameterBlockJAI pb = new ParameterBlockJAI("ClassifiedStats");
          pb.addSource(sampleImage);
          pb.setParameter("classifiers", new RenderedImage[]{classifierImage});
          pb.setParameter("pivotClassifiers", new RenderedImage[]{stripedImage, linedImage});
          pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX, Statistic.RANGE, Statistic.SUM});
          pb.setParameter("bands", new Integer[]{0});
  
          RenderedOp op = JAI.create("ClassifiedStats", pb);
          ClassifiedStats stats = (ClassifiedStats) op.getProperty(ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY);
  
          List<Map<MultiKey, List<Result>>> results = stats.results();
          for (int i = 0; i < results.size(); i++){
              if (LOGGER.isLoggable(Level.FINE)){
                  LOGGER.fine("Stats for pivot " + i + ": " + 
                      (i == 0 ? "stripes [1-5, step1]": "verticalLines [51-255, step 51]"));
              }
              Map<MultiKey, List<Result>> result_i = results.get(i); 
              Set<MultiKey> multikeys = result_i.keySet();
              Iterator<MultiKey> it = multikeys.iterator();
              while (it.hasNext()) {
                  MultiKey key = it.next(); 
                  List<Result> rs = result_i.get(key);
                  for (Result r: rs){
                      String s = r.toString();
                      if (LOGGER.isLoggable(Level.FINE)){
                          LOGGER.fine(s.toString());
                      }
                  }
              }
          }
          
      } finally {
          if (sample != null){
              try {
                  sample.close();
              } catch (Throwable t){
                  
              }
          }
          
          if (classifier1 != null){
              try {
                  classifier1.close();
              } catch (Throwable t){
                  
              }
          }
          
          if (classifierStripes != null){
              try {
                  classifierStripes.close();
              } catch (Throwable t){
                  
              }
          }
          
          if (classifierLines!= null){
              try {
                  classifierLines.close();
              } catch (Throwable t){
                  
              }
          }
      }
  }
    
    @Test
//    @Ignore
    public void testClassificationWithLocalRanges() throws IOException {
        if (LOGGER.isLoggable(Level.INFO)) {
                LOGGER.info("   test classificationWithLocalRanges");
        }

        ParameterBlockJAI pb = new ParameterBlockJAI("ClassifiedStats");
        InputStream sample = ClassifiedStatsTest.class.getResourceAsStream("sample.tif");
        RenderedImage sampleImage = ImageIO.read(sample);
        InputStream classifierMask = ClassifiedStatsTest.class.getResourceAsStream("mask1.tif");
        RenderedImage classifierImage = ImageIO.read(classifierMask);
        InputStream classifierStripe = ClassifiedStatsTest.class.getResourceAsStream("5stripes.tif");
        RenderedImage stripedImage = ImageIO.read(classifierStripe);
        pb.addSource(sampleImage);
        pb.setParameter("stats", new Statistic[]{Statistic.MIN, Statistic.MAX, Statistic.RANGE, Statistic.SUM});
        pb.setParameter("bands", new Integer[]{0});
        pb.setParameter("classifiers", new RenderedImage[]{stripedImage, classifierImage});
        
        List<Range<Double>> ranges = CollectionFactory.list();
        ranges.add(Range.create(0d, true, 100d , true));
        ranges.add(Range.create(101d, true, 255d , true));
        pb.setParameter("ranges", ranges);
        pb.setParameter("rangesType", Range.Type.INCLUDE);
        
        pb.setParameter("rangeLocalStats", true);

        RenderedOp op = JAI.create("ClassifiedStats", pb);
        ClassifiedStats stats = (ClassifiedStats) op.getProperty(ClassifiedStatsDescriptor.CLASSIFIED_STATS_PROPERTY);

        Map<MultiKey, List<Result>> results = stats.results().get(0);
        Set<MultiKey> multikeys = results.keySet();
        Iterator<MultiKey> it = multikeys.iterator();
        while (it.hasNext()) {
            MultiKey key = it.next(); 
            List<Result> rs = results.get(key);
            for (Result r: rs){
                String s = r.toString();
                if (LOGGER.isLoggable(Level.FINE)){
                    LOGGER.fine(s.toString());
                }
            }
        }
        if (LOGGER.isLoggable(Level.INFO)){
            LOGGER.info("\nGetting Max from the result coming from the 2nd stripe " +
    		"(The first classifier raster, with value = 1), " +
                "\n and the second classifier raster with value = 50, " +
                "for the first and second range\n " +
                stats.band(0).statistic(Statistic.MAX).results().get(0).get(new MultiKey(1,50)).get(0) + "\n" +
                stats.band(0).statistic(Statistic.MAX).results().get(0).get(new MultiKey(1,50)).get(1));
        }
    }

}
