/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2007 IVER T.I. and Generalitat Valenciana.
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
 */
package org.gvsig.raster.dataset.io.rmf;

import junit.framework.Test;
import junit.framework.TestSuite;

public class AllTestsRmf {

	public static Test suite() {
		TestSuite suite = new TestSuite("Test for org.gvsig.raster.dataset.io.rmf");
		//$JUnit-BEGIN$
		suite.addTestSuite(TestRmfWrite.class);
		suite.addTestSuite(TestRmfRead.class);
		//$JUnit-END$
		return suite;
	}

}
