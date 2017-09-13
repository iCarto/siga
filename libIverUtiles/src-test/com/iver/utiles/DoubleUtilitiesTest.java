package com.iver.utiles;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class DoubleUtilitiesTest {
    @Test
    public void testDouble() {
	double numero = 1.36645345645;
	numero = DoubleUtilities.format(numero, ".".charAt(0), 4);
	assertEquals(numero, 1.3665, 0.00000000001);
    }
}
