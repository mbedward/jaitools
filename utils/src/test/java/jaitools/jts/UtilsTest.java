/**
 * 
 */
package jaitools.jts;

import org.junit.Assert;
import org.junit.Test;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.io.ParseException;
import com.vividsolutions.jts.io.WKTReader;

/**
 * Tests for the JTS utility class.
 * 
 * @author Simone Giannecchini, GeoSolutions SAS
 *
 */
public class UtilsTest extends Assert {

	private final static String LINESTRING= "LINESTRING(0 0, 1 0, 2 0, 5 0, 5 1, 5 2, 5 5, 6 5, 6 8, 6 9, 7 0)";
	private final static String EXPECTED_LINESTRING= "LINESTRING(0 0, 5 0, 5 5, 6 5, 6 9, 7 0)";
	
	private final static String POLYGON="POLYGON((1 1, 2 1, 3 1, 4 1, 5 1, 5 2, 5 3, 5 4, 5 5, 4 5, 3 5, 2 5, 1 5, 1 4, 1 3, 1 2, 1 1),(2 2,2 3,3 3,3 2,2 2))";
	private final static String EXPECTED_POLYGON="POLYGON((1 1,5 1,5 5,1 5,1 1),(2 2,2 3,3 3,3 2,2 2))";
	
	private final static String RING="LINEARRING(1 1, 2 1, 3 1, 4 1, 5 1, 5 2, 5 3, 5 4, 5 5, 4 5, 3 5, 2 5, 1 5, 1 4, 1 3, 1 2, 1 1)";
	private final static String EXPECTED_RING="LINEARRING(1 1,5 1,5 5,1 5,1 1)";
	
	@Test
	public void testLineString() throws ParseException {
		final LineString ls= (LineString) new WKTReader().read(LINESTRING);
		
		// simplify
		final LineString simplifiedLS= Utils.removeCollinearVertices(ls);
		
		// check expected
		assertTrue(((LineString) new WKTReader().read(EXPECTED_LINESTRING)).equalsExact(simplifiedLS));
	}

	@Test
	public void testLinePolygon() throws ParseException {
		final Polygon poly= (Polygon) new WKTReader().read(POLYGON);
		
		// simplify
		final Polygon simplifiedPoly= Utils.removeCollinearVertices(poly);
		
		// check expected
		assertTrue(((Polygon) new WKTReader().read(EXPECTED_POLYGON)).equalsExact(simplifiedPoly));
	}
	
	@Test
	public void testLineRing() throws ParseException {
		final LinearRing ring= (LinearRing) new WKTReader().read(RING);
		
		// simplify
		final LinearRing simplifiedring= (LinearRing) Utils.removeCollinearVertices(ring);
		
		// check expected
		assertTrue(((LinearRing) new WKTReader().read(EXPECTED_RING)).equalsExact(simplifiedring));
	}
	
	@Test
	public void testGeometry() throws ParseException {
		final Geometry ring= new WKTReader().read(RING);
		
		// simplify
		final LinearRing simplifiedring=  (LinearRing) Utils.removeCollinearVertices(ring);
		
		// check expected
		assertTrue(((LinearRing) new WKTReader().read(EXPECTED_RING)).equalsExact(simplifiedring));
		
		
		final Geometry poly=  new WKTReader().read(POLYGON);
		
		// simplify
		final Polygon simplifiedPoly= (Polygon) Utils.removeCollinearVertices(poly);
		
		// check expected
		assertTrue(((Polygon) new WKTReader().read(EXPECTED_POLYGON)).equalsExact(simplifiedPoly));
		
		
		final Geometry ls=  new WKTReader().read(LINESTRING);
		
		// simplify
		final LineString simplifiedLS= (LineString) Utils.removeCollinearVertices(ls);
		
		// check expected
		assertTrue(((LineString) new WKTReader().read(EXPECTED_LINESTRING)).equalsExact(simplifiedLS));
		
	}
}
