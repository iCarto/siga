/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.utiles.vectorUtilities;

import static org.junit.Assert.assertEquals;

import java.text.Collator;
import java.util.Locale;
import java.util.Vector;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.iver.utiles.StringComparator;

/**
 * @author Pablo Piqueras Bartolomé (p_queras@hotmail.com)
 * @author Francisco Puga <fpuga@cartolab.es>
 */
public class VectorUtilitiesTest {
    private static String obj;
    private static Vector<Object> v1;
    private static StringComparator stringComparator;

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
	stringComparator = new StringComparator();

	// Set spanish rules and with case sensitive
	Collator collator = Collator.getInstance(new Locale("es_ES"));
	stringComparator.setLocaleRules(stringComparator.new LocaleRules(true,
		collator));
	stringComparator.setCaseSensitive(false);
    }

    @Before
    public void setUp() throws Exception {
	v1 = new Vector<Object>();
    }

    @Test
    public void insertAtTheBeginning() {
	obj = new String("First");
	v1 = new Vector<Object>();
	VectorUtilities.addAlphabeticallyOrdered(v1, obj);
	assertEquals(obj, v1.get(0));
    }

    @Test
    public void insertAtTheBeginningWithComparator() {
	obj = new String("First");
	VectorUtilities.addAlphabeticallyOrdered(v1, obj, stringComparator);
	assertEquals(obj, v1.get(0));
    }

    @Test
    public void insertAtTheEnd() {
	obj = new String("ZZÑÑÇÇ");
	VectorUtilities.addAlphabeticallyOrdered(v1, obj);
	VectorUtilities.addAlphabeticallyOrdered(v1, "Z");
	VectorUtilities.addAlphabeticallyOrdered(v1, "A");
	assertEquals(obj, v1.lastElement());
    }

    @Test
    public void insertAtTheEndWithComparator() {
	obj = new String("ZZÑÑÇÇ");
	VectorUtilities.addAlphabeticallyOrdered(v1, obj, stringComparator);
	VectorUtilities.addAlphabeticallyOrdered(v1, "z", stringComparator);
	VectorUtilities.addAlphabeticallyOrdered(v1, "A", stringComparator);
	assertEquals(obj, v1.lastElement());
    }

}
