/* OrbisCAD. The Community cartography editor
 *
 * Copyright (C) 2005, 2006 OrbisCAD development team
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
 *  OrbisCAD development team
 *   elgallego@users.sourceforge.net
 */
package org.gvsig.jump.adapter;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * JUMP's FeatureSchema implementation based in gvSIG's ReadableVectorial classes.
 * 
 * This adapter is based in FeatureSchemaAdapter of OrbisCAD project, modified
 * to work with gvSIG's GDBMS version.
 * 
 * @author Alvaro Zabala
 *
 */
public class FeatureSchemaAdapter extends FeatureSchema {
	private static final long serialVersionUID = 2657075042299778228L;
	private ReadableVectorial rv;
	private SelectableDataSource ds;

    public FeatureSchemaAdapter(ReadableVectorial rv) {
        try {
        	this.rv = rv;
			this.ds = rv.getRecordset();
		} catch (ReadDriverException e) {
			throw new RuntimeException(e);
		}
    }
    
    public int getAttributeCount() {
        try {
            return ds.getFieldCount() + 1;
        }catch (ReadDriverException e) {
        	throw new RuntimeException(e);
		}
    }

    public String getAttributeName(int attributeIndex) {
    	if(attributeIndex == getAttributeCount() - 1)
    		return "GEOMETRY";
        try {
        	return ds.getFieldName(attributeIndex);
        }catch (ReadDriverException e) {
        	throw new RuntimeException(e);
		}
    }

    public int getAttributeIndex(String attributeName) {
    	if(attributeName == "GEOMETRY")
    		return getAttributeCount() - 1;
        	try {
				return ds.getFieldIndexByName(attributeName);
			} catch (ReadDriverException e) {
				throw new RuntimeException(e);
			}
    }

    public int getGeometryIndex() {
    	//If users try to access geometry by getAttribute, it will be
    	//after the last alphanumeric attribute
        return getAttributeCount() -1;
    }

    public ReadableVectorial getDs() {
        return rv;
    }
    
}
