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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gvsig.jts.JtsUtil;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jump.feature.Feature;
import com.vividsolutions.jump.feature.FeatureCollection;
import com.vividsolutions.jump.feature.FeatureSchema;

/**
 * OpenJUMP's FeatureCollection implementation based in gvSIG's driver architecture.
 * 
 * Based in OrbisCAD project's work, adapted to work with gvSIG drivers.
 * @author Alvaro Zabala
 *
 */
public class FeatureCollectionAdapter implements FeatureCollection {

    private ReadableVectorial rv;

    public FeatureCollectionAdapter(ReadableVectorial rv) {
        this.rv = rv;
    }
    
    public FeatureSchema getFeatureSchema() {
        return new FeatureSchemaAdapter(rv);
    }

    public Envelope getEnvelope() {
       try {
    	   return JtsUtil.rectangle2dToEnvelope(rv.getFullExtent());
       } catch (ExpansionFileReadException e) {
    	   throw new RuntimeException(e);
       } catch (ReadDriverException e) {
			throw new RuntimeException(e);
       }
    }

    public int size() {
        try {
            return (int) rv.getShapeCount();
        } catch (ReadDriverException e) {
        	  throw new RuntimeException(e);
		}
    }

    public boolean isEmpty() {
        // TODO Auto-generated method stub
        return false;
    }

    public List getFeatures() {
        return new FeatureListAdapter(rv);
    }

    public Iterator iterator() {
        return new FeatureIterator(rv);
    }

    public List query(Envelope envelope) {
        // TODO Auto-generated method stub
        return null;
    }

    public void add(Feature feature) {
        // TODO Auto-generated method stub
        
    }

    public void addAll(Collection features) {
        // TODO Auto-generated method stub
        
    }

    public void removeAll(Collection features) {
        // TODO Auto-generated method stub
        
    }

    public void remove(Feature feature) {
        // TODO Auto-generated method stub
        
    }

    public void clear() {
        // TODO Auto-generated method stub
        
    }

    public Collection remove(Envelope env) {
        // TODO Auto-generated method stub
        return null;
    }

}
