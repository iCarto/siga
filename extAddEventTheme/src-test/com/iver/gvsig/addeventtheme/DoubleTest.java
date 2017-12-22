package com.iver.gvsig.addeventtheme;

import static org.junit.Assert.assertEquals;

import java.util.Locale;

import org.junit.Test;

public class DoubleTest {

    private final static double DELTA = 0.0001;

    @Test
    public void test() {
        assertEquals(new Double("123").doubleValue(), 123, DELTA);
        assertEquals(new Double("+123.45").doubleValue(), 123.45, DELTA);
        assertEquals(new Double("-123.45").doubleValue(), -123.45, DELTA);
        assertEquals(new Double("0.34f").doubleValue(), 0.34, DELTA);
        assertEquals(new Double("23e-5").doubleValue(), 0.00023, DELTA);
        // assertEquals(new Double("0x1").doubleValue(), 15, DELTA);
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidDoubleSpanishLocale() {
        Locale.setDefault(new Locale("es", "es"));
        new Double("123,45");
    }

    @Test(expected = NumberFormatException.class)
    public void testInvalidDoubleUSLocale() {
        Locale.setDefault(new Locale("en", "us"));
        new Double("123,45");
    }
}
