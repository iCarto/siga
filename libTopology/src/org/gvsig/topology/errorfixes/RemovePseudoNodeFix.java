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
package org.gvsig.topology.errorfixes;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.gvsig.exceptions.BaseException;
import org.gvsig.fmap.core.FGeometryUtil;
import org.gvsig.fmap.core.NewFConverter;
import org.gvsig.jts.JtsUtil;
import org.gvsig.topology.Messages;
import org.gvsig.topology.TopologyError;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.vividsolutions.jts.algorithms.SnapCGAlgorithms;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.operation.linemerge.LineMerger;
import com.vividsolutions.jts.precision.EnhancedPrecisionOp;

/**
 * Removes the pseudonode associated to a topology error by merging
 * two touching linestrings. 
 * 
 * It will preserve the alphanumeric attributes of the largest linestring.
 * @author Alvaro Zabala
 *
 */
public class RemovePseudoNodeFix extends AbstractTopologyErrorFix {

	
	
	public void fix(TopologyError error) throws BaseException {
		EditableAdapter[] adapters = prepareEdition(error);

		List<IFeature> features2merge = new ArrayList<IFeature>();
		
		IFeature firstFeature = error.getFeature1();
		features2merge.add(firstFeature);

		Geometry firstJtsGeo = NewFConverter.toJtsGeometry(firstFeature.getGeometry());
		LineString[] firstLines = JtsUtil.extractLineStrings(firstJtsGeo);
		
		
		FGeometry errorGeom = (FGeometry) error.getGeometry();
		FPoint2D pseudonode = (FPoint2D) errorGeom.getInternalShape();
		double x = pseudonode.getX();
		double y = pseudonode.getY();
		double clusterTolerance = error.getTopology().getClusterTolerance();
		
		Rectangle2D queryRect = FGeometryUtil.getSnapRectangle(x, y,
				clusterTolerance);
		
		FLyrVect originLyr = error.getOriginLayer();
		SelectableDataSource recordset = originLyr.getRecordset();
		int numberOfFields = recordset.getFieldCount();
		String[] fields = new String[numberOfFields];
		for (int i = 0; i < numberOfFields; i++) {
			fields[i] = recordset.getFieldName(i);
		}
		
		IFeatureIterator featureIterator = originLyr.getSource().
				getFeatureIterator(queryRect, fields, null, false);
		while (featureIterator.hasNext()) {
			IFeature candidate = featureIterator.next();
			if (candidate.getID().equals(firstFeature.getID()))
				continue;

			Geometry secondJtsGeo = NewFConverter.toJtsGeometry(candidate.getGeometry());
			LineString[] secondLines = JtsUtil.extractLineStrings(secondJtsGeo);

			lookingForNestedPrimitive: 
				for (int i = 0; i < firstLines.length; i++) {
				LineString linA = firstLines[i];
				Coordinate firstA = linA.getCoordinateN(0);
				Coordinate lastA = linA.getCoordinateN(linA.getNumPoints() - 1);
				
				for (int j = 0; j < secondLines.length; j++) {
					LineString linB = secondLines[j];
					
					Coordinate firstB = linB.getCoordinateN(0);
					Coordinate lastB = linB.getCoordinateN(linB.getNumPoints() - 1);
					
					//quizas deberia refinar esto, y comprobar que casan con el pseudonodo
					if (SnapCGAlgorithms.snapEquals2D(firstA, firstB,
							clusterTolerance)
							|| SnapCGAlgorithms.snapEquals2D(firstA, lastB,
									clusterTolerance)
							|| SnapCGAlgorithms.snapEquals2D(lastA, firstB,
									clusterTolerance)
							|| SnapCGAlgorithms.snapEquals2D(firstA, lastB,
									clusterTolerance)) {
						features2merge.add(candidate);
						break lookingForNestedPrimitive;
					}// if
				}// for j
			}// for i

		}// while

		// at this point, features2merge has all the features whose geometries
		// we must to union
		double maxLength = -1;
		Iterator<IFeature> featuresIterator = features2merge.iterator();
		Geometry newJtsGeometry = null;
		Value[] attributes = null;

		LineMerger merger = new LineMerger();
		List geometries = new ArrayList();
		while (featuresIterator.hasNext()) {
			IFeature feature = featuresIterator.next();
			Geometry jtsGeometry = NewFConverter.toJtsGeometry(feature.getGeometry());//FIXME Este paso se podria ahorrar si creamos un FixFeature que cachee los JTS
			double lenght = jtsGeometry.getLength();
			if (lenght > maxLength) {
				attributes = feature.getAttributes();
				maxLength = lenght;
			}

			//with the union of the linestrings, we node them
			if (newJtsGeometry == null)
				newJtsGeometry = jtsGeometry;
			else
				newJtsGeometry = EnhancedPrecisionOp.union(newJtsGeometry,
						jtsGeometry);
		}
		//at this point, newJtsGeometry is a GeometryCollection with fully noded linestrings
		LineString[] lineStrings = JtsUtil.extractLineStrings(newJtsGeometry);
		geometries.addAll(Arrays.asList(lineStrings));
		merger.add(geometries);
		
		Collection mergedLineStrings = merger.getMergedLineStrings();
		Geometry[] geoms = new Geometry[mergedLineStrings.size()];
		mergedLineStrings.toArray(geoms);
		if(geoms.length == 1)
			newJtsGeometry = geoms[0];
		else
			newJtsGeometry = JtsUtil.GEOMETRY_FACTORY.createGeometryCollection(geoms);
		
		IGeometry newIGeometry = NewFConverter.toFMap(newJtsGeometry);
		String newId = error.getOriginLayer().getSource().getShapeCount()+"";
		DefaultFeature newFeature = new DefaultFeature(newIGeometry, attributes, newId);

//		adapters[0].startComplexRow();
		
		adapters[0].doAddRow(newFeature, EditionEvent.GRAPHIC);
		//now we remove the merged features
		for (int i = 0; i < features2merge.size(); i++) {
			IFeature feature = features2merge.get(i);
		
//			adapters[0].removeRow(Integer.parseInt(feature.getID()),
//											getEditionDescription(), 
//											EditionEvent.ROW_EDITION);
			adapters[0].doRemoveRow(Integer.parseInt(feature.getID()), EditionEvent.ROW_EDITION);
		}
		//and created a new feature, with the attributes of the largest line
//		adapters[0].addRow(newFeature,getEditionDescription(), EditionEvent.GRAPHIC);
		
//		adapters[0].endComplexRow(getEditionDescription());
		error.getTopology().removeError(error);

	}

	public String getEditionDescription() {
		return Messages.getText("REMOVE_PSEUDONODE_FIX");
	}

	@Override
	public List<IFeature>[] fixAlgorithm(TopologyError error)
			throws BaseException {
		throw new NotImplementedException();
	}

}
