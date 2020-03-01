package es.icarto.gvsig.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

public class TestSIGAFormatter {
    private static final double DELTA = 1e-15;

    private SIGAFormatter formatter;

    @Before
    public void before() {
        Locale.setDefault(new Locale("es"));
        formatter = new SIGAFormatter();
    }

    @Test
    public void testFormatPkForDisplay() {
        assertEquals("1,000", SIGAFormatter.formatPkForDisplay("1"));
        assertEquals("2,780", SIGAFormatter.formatPkForDisplay("002.78"));
    }

    @Test
    public void testToNumber() {
        assertNull(formatter.toNumber(null));
        assertNull(formatter.toNumber(""));
        assertNull(formatter.toNumber(" "));
        assertEquals(1, formatter.toNumber("1").doubleValue(), DELTA);
        assertNull("Forzando locale es el separador de decimal es la ,", formatter.toNumber("1.5"));
        assertEquals(1.4, formatter.toNumber("1,4"));
        assertEquals(1234.56, formatter.toNumber("1234,56"));
        assertNull(formatter.toNumber("1.234,56"));
        assertEquals(0.4, formatter.toNumber(" 0,4 "));
        assertNull(formatter.toNumber("- 0,4 "));
        assertEquals(-0.4, formatter.toNumber(" -0,4 "));
    }

    @Test
    public void demonstrateSomeNotParseableValues() {

        DecimalFormat doubleFormatOnDisplay = (DecimalFormat) NumberFormat.getNumberInstance(Locale.getDefault());
        final String pattern = "0.##########";
        // Display a maximum of 10 decimals
        doubleFormatOnDisplay.applyPattern(pattern);
        try {
            System.out.println(doubleFormatOnDisplay.parse(null));
            fail("Parsear null debería lanzar un NullPointerException");
        } catch (NullPointerException e) {

        } catch (ParseException e) {
            fail("No debería llegar hasta aquí");
        }
        try {
            System.out.println(doubleFormatOnDisplay.parse(""));
            fail("Parsear un string vacio debería lanzar un ParseExpection: Unparseable number");
        } catch (ParseException e) {

        }
        try {
            System.out.println(doubleFormatOnDisplay.parse(" "));
            fail("Parsear un espacio debería lanzar un ParseExpection: Unparseable number");
        } catch (ParseException e) {

        }

    }

    @Test
    public void testFormatPkForDouble() {
        fail("Not yet implemented");
    }

}
