package es.icarto.gvsig.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;

public class TestSIGAFormatter {

    @Test
    public void testFormatPkForDisplay() {
        assertEquals("1,000", SIGAFormatter.formatPkForDisplay("1"));
        assertEquals("2,780", SIGAFormatter.formatPkForDisplay("002.78"));
    }

    @Test
    public void testFormatPkForDouble() {
        fail("Not yet implemented");
    }

}
