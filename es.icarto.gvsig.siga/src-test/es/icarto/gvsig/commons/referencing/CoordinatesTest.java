package es.icarto.gvsig.commons.referencing;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import es.icarto.gvsig.commons.referencing.Coordinates;

//https://code.google.com/p/gps-coordinates-parser/
/**
 *
 * @author Richard Mihalovic
 */
public class CoordinatesTest {

    @Test
    public void testEmptyConstructor() {
	Coordinates c = new Coordinates();

	assertEquals(0., c.getLatitude(), 0);
	assertEquals(0., c.getLongitude(), 0);
    }

    @Test
    public void coordinatesConstructor() {
	Coordinates c = new Coordinates(48.16, 17.13);

	assertEquals(48.16, c.getLatitude(), 0);
	assertEquals(17.13, c.getLongitude(), 0);
    }

    @Test
    public void parsingSpacesTest() {
	// BRATISLAVA: 48° 9' 36"N, 17°  7'  48"E

	Coordinates gps = new Coordinates();
	assertTrue(gps.parse("48 9 36,17 7 48"));
	assertTrue(gps.parse("48 9 36, 17 7 48"));
	assertTrue(gps.parse("48 9 36 , 17 7 48"));
	assertTrue(gps.parse(" 48 9 36 , 17 7 48"));
	assertTrue(gps.parse("48 9 36 , 17 7 48 "));
	assertTrue(gps.parse("  48 9 36 , 17 7 48  "));
    }

    @Test
    public void parsingDDMMSS() {
	Coordinates gps = new Coordinates();
	Coordinates gpsTB = new Coordinates(48.16, 17.13); // Bratislava
	Coordinates gpsTS = new Coordinates(-33.87, 151.2); // Sidney

	// Bratislava
	assertTrue(gps.parse("48 9 36, 17 7 48"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N48 9 36, E17 7 48"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("+48 9 36, +17 7 48"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48 9 36N, 17 7 48E"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48 9 36 17 7 48"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N48 9 36 E17 7 48"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48 9 36N 17 7 48E"));
	assertTrue(gps.equals(gpsTB));

	// Sidney
	assertTrue(gps.parse("33 52 12S, 151 12 36E"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("S33 52 12, E151 12 36"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("-33 52 12, +151 12 36"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("-33 52 12, 151 12 36"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);
    }

    @Test
    public void parsingDDMM() {
	// Bratislava: N 48 09.600 E 17 07.800

	Coordinates gps = new Coordinates();

	// Bratislava
	Coordinates gpsTB = new Coordinates(48.16, 17.13); // Bratislava

	assertTrue(gps.parse("N 48 09.600 E 17 07.800"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N 48 09.600, E 17 07.800"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N48 09.600 E17 07.800"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N48 09.600, E17 07.800"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48 09.600N 17 07.800E"));
	assertTrue(gps.equals(gpsTB));

	// Sidney
	Coordinates gpsTS = new Coordinates(-33.87, 151.2); // Sidney

	assertTrue(gps.parse("S 33 52.200, E 151 12.600"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("S33 52.200, E151 12.600"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("33 52.200S, 151 12.600E"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("-33 52.200, +151 12.600"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	assertTrue(gps.parse("-33 52.200, 151 12.600"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);

	// England (user bug) N 54° 16.439 W 000° 55.827
	Coordinates gpsUser1 = new Coordinates(54.274, -0.9304);

	assertTrue(gps.parse("N 54 16.439, W 000 55.827"));
	assertEquals(gps.getLatitude(), gpsUser1.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsUser1.getLongitude(), 0.1);

	assertTrue(gps.parse("N 54 16.439 W 000 55.827"));
	assertEquals(gps.getLatitude(), gpsUser1.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsUser1.getLongitude(), 0.1);

	// Australia (user bug) 33S,151E
	Coordinates gpsUser2 = new Coordinates(-33.0, 151.0);

	assertTrue(gps.parse("33S,151E"));
	assertEquals(gps.getLatitude(), gpsUser2.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsUser2.getLongitude(), 0.1);

	assertTrue(gps.parse("33S 151E"));
	assertEquals(gps.getLatitude(), gpsUser2.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsUser2.getLongitude(), 0.1);
    }

    @Test
    public void parsingDD() {
	// Bratislava: 48.16N, 17.13E

	Coordinates gps = new Coordinates();
	Coordinates gpsTB = new Coordinates(48.16, 17.13); // Bratislava
	Coordinates gpsTS = new Coordinates(-33.87, 151.2); // Sidney

	assertTrue(gps.parse("48.16, 17.13"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48.16N, 17.13E"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N48.16, E17.13"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48.16 17.13"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("48.16N 17.13E"));
	assertTrue(gps.equals(gpsTB));

	assertTrue(gps.parse("N48.16 E17.13"));
	assertTrue(gps.equals(gpsTB));

	// Sidney
	assertTrue(gps.parse("-33.87 151.21"));
	assertEquals(gps.getLatitude(), gpsTS.getLatitude(), 0.1);
	assertEquals(gps.getLongitude(), gpsTS.getLongitude(), 0.1);
    }
}