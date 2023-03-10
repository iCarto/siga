/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
*
* Copyright (C) 2006 IVER T.I. and Generalitat Valenciana.
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
*   Av. Blasco Ib??ez, 50
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


package com.iver.cit.gvsig.drivers.dwg;

import java.util.Locale;

/**
* Bridge class to provide internationalization services to the library.
* It uses the gvsig-i18n library as a backend, and includes its
* necessary initialization.
* 
* @author Cesar Martinez Izquierdo
*
*/
public class Messages {
	/**
	 * Whether the class has been initialized
	 */
	private static boolean isInitialized = false;
	
	/**
	 * The name of the Java package containing this class
	 */
	private static final String packageName = Messages.class.getPackage().getName();
	
	/**
	 * Loads the translations in the dictionary. It initializes the backend
	 * gvsig-i18n library
	 *
	 */
	private static void init() {
		if (!org.gvsig.i18n.Messages.hasLocales()) {
			org.gvsig.i18n.Messages.addLocale(Locale.getDefault());
		}
		org.gvsig.i18n.Messages.addResourceFamily(packageName+".resources.translations.text", Messages.class.getClassLoader(), packageName);
	}
	
	/**
	 * Gets the translation associated with the provided translation key.
	 * 
	 * @param key The translation key which identifies the target text
	 * @return The translation associated with the provided translation key.
	 */
	public static String getText(String key) {
		if (isInitialized==false) {
			init();
			isInitialized = true;
		}
		return org.gvsig.i18n.Messages.getText(key, packageName);
	}

}
