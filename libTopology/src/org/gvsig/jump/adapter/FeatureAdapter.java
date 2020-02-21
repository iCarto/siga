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

import org.gvsig.fmap.core.NewFConverter;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jump.feature.BasicFeature;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * Adapts a gvSIG's feature source (ReadableVectorial) to OpenJUMP Api,
 * so it can create OpenJUMP's features with gvSIG' drivers.
 * 
 * Based in OrbisCAD work, adapted to work with gvSIG' drivers architecture.
 * 
 * @author Alvaro Zabala
 *
 */
public class FeatureAdapter implements Feature, Comparable {

    private ReadableVectorial rv;

    private int index;

    public FeatureAdapter(ReadableVectorial rv, int index) {
        this.rv = rv;
        this.index = index;
    }

    @Override
    public int hashCode() {
        return index;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FeatureAdapter) {
            FeatureAdapter fa = (FeatureAdapter) obj;
            if ((fa.index == index)) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void setAttributes(Object[] attributes) {
    }

    public void setSchema(FeatureSchema schema) {
    }

    public int getID() {
        return index;
    }

    public void setAttribute(int attributeIndex, Object newAttribute) {
    }

    public void setAttribute(String attributeName, Object newAttribute) {
    }

    public void setGeometry(Geometry geometry) {
    }

    public Object getAttribute(int i) {
        try {
            return rv.getRecordset().getFieldValue(index, i);
        } catch (ReadDriverException e) {
        	 throw new RuntimeException(e);
		}
    }

    public Object getAttribute(String name) {
        try {
            return rv.getRecordset().getFieldValue(index, rv.getRecordset().getFieldIndexByName(name));
        } catch (ReadDriverException e) {
        	 throw new RuntimeException(e);
		}
    }

    public String getString(int attributeIndex) {
        return null;
    }

    public int getInteger(int attributeIndex) {
        return 0;
    }

    public double getDouble(int attributeIndex) {
        return 0;
    }

    public String getString(String attributeName) {
        return null;
    }

    public Geometry getGeometry() {
        try {
			return NewFConverter.toJtsGeometry(rv.getShape(index));
		} catch (ExpansionFileReadException e) {
			 throw new RuntimeException(e);
		} catch (ReadDriverException e) {
			 throw new RuntimeException(e);
		}
    }

    public FeatureSchema getSchema() {
        return new FeatureSchemaAdapter(rv);
    }

    public Object clone() {
        BasicFeature ret = new BasicFeature(getSchema());
        ret.setAttributes(getAttributes());

        return ret;
    }

    public Feature clone(boolean deep) {
        return (Feature) clone();
    }

    public Object[] getAttributes() {
        Object[] atts = new Object[getSchema().getAttributeCount()];
        for (int i = 0; i < atts.length; i++) {
            atts[i] = getAttribute(i);
        }

        return atts;
    }

    public int compareTo(Object o) {
        if (o instanceof FeatureAdapter) {
            FeatureAdapter fa = (FeatureAdapter) o;
            if (fa.rv != rv) {
                return -1;
            } else {
                return fa.index - index;
            }
        } else {
            return -1;
        }
    }

}
