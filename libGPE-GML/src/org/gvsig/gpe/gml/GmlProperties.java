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
 
package org.gvsig.gpe.gml;

import java.util.Properties;

import org.gvsig.gpe.IGPEProperties;


/**
 * This class contains the properties for the GML
 * parsers and writers. This class has been registered using
 * the SPI (Service Provider Interface) and the values of their
 * properties can be configured using {@link GPEDefaults}.
 */
public class GmlProperties implements IGPEProperties{
	public Properties properties = null;
	
	public GmlProperties(){
		properties = new Properties();
		properties.put(DECIMAL, DECIMAL_VALUE);
		properties.put(COORDINATES_SEPARATOR, COORDINATES_SEPARATOR_VALUE);
		properties.put(TUPLES_SEPARATOR, TUPLES_SEPARATOR_VALUE);
		properties.put(DEFAULT_FEATURECOLLECTION, DEFAULT_FEATURECOLLECTION_VALUE);
		properties.put(DEFAULT_FEATURE, DEFAULT_FEATURE_VALUE);
		properties.put(SRS_BASED_ON_XML, SRS_BASED_ON_XML_VALUE);
	}
	
	/**
	 * This attribute is used to represent the decimal separator. 
	 * It separates the integral and  the fractional parts of a 
	 * decimal numeral.
	 */
	public static final String DECIMAL = "decimal";
	private static final String DECIMAL_VALUE = ".";
	
	/**
	 * It separates the different dimensions of each coordinate
	 * of a geometry (e.g. Uses ",": X1,Y1,Z1) 
	 */
	public static final String COORDINATES_SEPARATOR = "coordinatesSeparator";
	private static final String COORDINATES_SEPARATOR_VALUE = ",";
	
	/**
	 * It separates the different tuples of dimensions 
	 * (e.g. Uses blank space " ": X1,Y1,Z1 X2,Y2,Z2) 
	 */
	public static final String TUPLES_SEPARATOR = "tuplesSeparator";
	private static final String TUPLES_SEPARATOR_VALUE = " ";
	
	/**
	 * Default name for a collection of features. This name will be used
	 * only if there is not a name for a feature collection.
	 */
	public static final String DEFAULT_FEATURECOLLECTION= "featureCollection";
	private static final String DEFAULT_FEATURECOLLECTION_VALUE=  "FeatureCollection";
	
	/**
	 * Default name for a feature. It is only used if the
	 * feature to write doesn't has a name
	 */
	public static final String DEFAULT_FEATURE= "feature";
	private static final String DEFAULT_FEATURE_VALUE=  "Feature";
	
	/**
	 * IF the SRS are based on XML or if are based on EPSG code.
	 */
	public static final String SRS_BASED_ON_XML  = "srsBasedOnXml";
	private static final Boolean SRS_BASED_ON_XML_VALUE = new Boolean(true);
		
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gpe.IGPEProperties#getProperties()
	 */
	public Properties getProperties() {
		return properties;
	}

}

