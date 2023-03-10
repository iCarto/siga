/*
 * Created on 28-jul-2005
 *
 * gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
package com.iver.andami;

import java.util.Enumeration;
import java.util.ResourceBundle;

/**
 * @author fjp
 *
 * Esta clase intenta crear un locale con lo que le pasas.
 * Si no puede, mira si le has pasado algo que conoce, por ejemplo
 * va para valenciano, y hace lo que puede poniendo un lenguaje
 * para esto (por ejemplo, usar un locale "ca_ES"), pero se guarda
 * que en realidad es "va".
 */
public class SpecialResourceBundle extends ResourceBundle {
    private static final String BUNDLE_NAME = "com.iver.andami.text";
    SpecialResourceBundle(String strLocale)
    {
    }
    public Enumeration getKeys() {
        // TODO Auto-generated method stub
        return null;
    }
    protected Object handleGetObject(String key) {
        // TODO Auto-generated method stub
        return null;
    }
}
