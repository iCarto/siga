package org.gvsig.tableImport.addgeominfo.util;

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

/**
* <p>Extends {@link StringUtilities StringUtilities} for adding support to replace some problematic
*  letters to their most equivalent.</p>
* 
* @author Pablo Piqueras Bartolom� (pablo.piqueras@iver.es)
*/
public class StringUtilitiesExtended {
	/**
	 * <p>Replaces all letters with accent by their equivalent without it:
	 *  <ul>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>a</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>e</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>i</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>o</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>u</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>A</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>E</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>I</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>O</i></b></li>
	 *   <li><b>�</b>,<b>�</b>,<b>�</b>,<b>�</b> by <b><i>U</i></b></li>
	 *  </ul>
	 * </p>
	 * 
	 * @param s text to be formatted
	 * @return text formatted
	 */
	public static String replaceAllAccents(String s) {
		return s.replaceAll("[����]","a").replaceAll("[����]","e").replaceAll("[����]","i").
			replaceAll("[����]","o").replaceAll("[����]","u").
			replaceAll("[����]", "A").replaceAll("[����]", "E").replaceAll("[����]", "I").
			replaceAll("[����]", "O").replaceAll("[����]", "U");
	}
	
	/**
	 * <p>Replaces all cedilla letters by the letter c:
	 *  <ul>
	 *   <li><b>�</b> by <b><i>c</i></b></li>
	 *   <li><b>�</b> by <b><i>C</i></b></li>
	 *  </ul>
	 * </p>
	 * 
	 * @param s text to be formatted
	 * @return text formatted
	 */
	public static String replaceAllCedilla(String s) {
		return s.replaceAll("[�]", "c").replaceAll("[�]", "C");
	}
	
	/**
	 * <p>Replaces all n with tilde letters by n:
	 *  <ul>
	 *   <li><b>�</b> by <b><i>n</i></b></li>
	 *   <li><b>�</b> by <b><i>N</i></b></li>
	 *  </ul>
	 * </p>
	 * 
	 * @param s text to be formatted
	 * @return text formatted
	 */
	public static String replaceAllNWithTilde(String s) {
		return s.replaceAll("[�]", "n").replaceAll("[�]", "N");
	}
}
