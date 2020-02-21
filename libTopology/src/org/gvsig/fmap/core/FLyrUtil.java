/*
 * Created on 10-abr-2006
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
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
/* CVS MESSAGES:
*
* $Id: 
* $Log: 
*/
package org.gvsig.fmap.core;

import java.util.ArrayList;
import java.util.List;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayersIterator;
import com.iver.cit.gvsig.fmap.spatialindex.INearestNeighbourFinder;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.RTreeJsi;

/**
 * Utility methods to work with FMap layers
 * @author Alvaro Zabala
 *
 */
public class FLyrUtil {
	/**
	 * From a given FLyrVect it returns an INearestNeighbourFinder
	 * spatial index.
	 * 
	 * @param lyr
	 * @return
	 */
	public static INearestNeighbourFinder getNearestNeighbourFinder(FLyrVect lyr) throws ReadDriverException{
		ISpatialIndex spatialIndex = lyr.getISpatialIndex();
		if( spatialIndex instanceof INearestNeighbourFinder){
			return (INearestNeighbourFinder) spatialIndex;
		}
		
		RTreeJsi newSptIdx = new RTreeJsi();
		newSptIdx.create();
		IFeatureIterator iterator = lyr.getSource().getFeatureIterator();
		while(iterator.hasNext()){
			IFeature feature = iterator.next();
			IGeometry geometry = feature.getGeometry();
			if(geometry != null)
				newSptIdx.insert(geometry.getBounds2D(), Integer.parseInt(feature.getID()));
		}
		return newSptIdx;	
	}
	
	public static  List<FLyrVect> getActiveVectorialLyrs(MapContext mapContext){
		List<FLyrVect> activeVectorialLyrs = new ArrayList<FLyrVect>();
		LayersIterator it = new LayersIterator(mapContext.getLayers());
		while (it.hasNext())
		{
			FLayer aux = (FLayer) it.next();
			if (!aux.isActive())
				continue;

			if(aux instanceof FLyrVect)
			{
				activeVectorialLyrs.add((FLyrVect)aux);
			}//if
		}//while
		return activeVectorialLyrs;
	}
	
	public static List<FLyrVect> getVectorialLayers(MapContext mapContext){
		List<FLyrVect> activeVectorialLyrs = new ArrayList<FLyrVect>();
		LayersIterator it = new LayersIterator(mapContext.getLayers());
		while (it.hasNext())
		{
			FLayer aux = (FLayer) it.next();
			if(aux instanceof FLyrVect)
			{
				activeVectorialLyrs.add((FLyrVect)aux);
			}//if
		}//while
		return activeVectorialLyrs;
	}
	
	public static List<FLyrVect> getLayersOfType(MapContext mapContext, int shapeType){
		List<FLyrVect> activeVectorialLyrs = new ArrayList<FLyrVect>();
		LayersIterator it = new LayersIterator(mapContext.getLayers());
		while (it.hasNext())
		{
			FLayer aux = (FLayer) it.next();
			if(aux instanceof FLyrVect)
			{
				FLyrVect aux2 = (FLyrVect)aux;
				try {
					if(aux2.getShapeType() == shapeType)
						activeVectorialLyrs.add((FLyrVect)aux);
				} catch (ReadDriverException e) {
					continue;
				}
			}//if
		}//while
		return activeVectorialLyrs;
	}

}
