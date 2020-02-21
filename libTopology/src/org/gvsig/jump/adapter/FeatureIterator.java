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

import java.util.Iterator;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.vividsolutions.jump.feature.Feature;

/**
 * Iterator implementation that can returns OpenJUMP's feature implementations
 * using gvSIG's driver architecture.
 * 
 * Based in the work of OrbisCAD project, adapted to work with
 * gvSIG's driver architecture.
 * @author Alvaro Zabala
 *
 */
public class FeatureIterator implements Iterator<Feature> {

    private ReadableVectorial rv;
    private int index = 0;

    public FeatureIterator(ReadableVectorial rv) {
        this.rv = rv;
    }
    
    public boolean hasNext() {
        try {
            return index < rv.getShapeCount();
        }  catch (ReadDriverException e) {
        	throw new RuntimeException(e);
		}
    }

    public Feature next() {
        Feature f = new FeatureAdapter(rv, index);
        index++;
        
        return f;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }

}
