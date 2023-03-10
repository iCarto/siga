/* gvSIG. Geographic Information System of the Valencian Government
*
* Copyright (C) 2007-2008 Infrastructures and Transports Department
* of the Valencian Government (CIT)
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
* Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, 
* MA  02110-1301, USA.
* 
*/

/*
* AUTHORS (In addition to CIT):
* 2008 Iver T.I.  {{Task}}
*/
 
package org.gvsig.gpe;

import java.util.Properties;

/**
 * This class contains the generic properties for all the 
 * GPE parsers and writers. This class has been registered using
 * the SPI (Service Provider Interface) and the values of their
 * properties can be configured using {@link GPEDefaults}.
 */
public class GPEProperties implements IGPEProperties{
	public static Properties properties = null;
	/**
	 * Number of decimal digits that both the parser and the writer
	 * have to manage.
	 */
	public static final String DECIMAL_DIGITS = "decimalDigits";
	private static final Integer DECIMAL_DIGITS_VALUE = new Integer(20);	
	
	static{
		properties = new Properties();
		properties.put(DECIMAL_DIGITS, DECIMAL_DIGITS_VALUE);		
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.IGPEProperties#getProperties()
	 */
	public Properties getProperties() {
		return properties;
	}

}


