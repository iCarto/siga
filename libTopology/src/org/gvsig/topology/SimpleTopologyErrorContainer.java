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
package org.gvsig.topology;

import java.awt.Color;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;

import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.drivers.TopologyErrorMemoryDriver;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.layers.FLayerGenericVectorial;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.VectorialUniqueValueLegend;
import com.iver.utiles.XMLEntity;

/**
 * 
 * Simple implementation of TopologyErrorContainer.
 * 
 * 
 * TODO Ver si la cadena de FIDs de errores topologicos que han sido marcados
 * como excepciones debe estar en Topology o en ErrorContainer
 * 
 * @author Alvaro Zabala
 * 
 */
public class SimpleTopologyErrorContainer implements ITopologyErrorContainer, Cloneable {

	private List<TopologyError> topologyErrors;
	
	private Topology topology;
	
	/**
	 * Number of errors that has been marked as exceptions
	 */
	int numberOfExceptions = 0;
	
	
	public SimpleTopologyErrorContainer(){
		topologyErrors = new ArrayList<TopologyError>();
	}
	
	public void setTopology(Topology topology){
		this.topology = topology;
	}
	
	public Topology getTopology(){
		return this.topology;
	}
	
	public Object clone(){
		SimpleTopologyErrorContainer newContainer = new SimpleTopologyErrorContainer();
		for(int i = 0; i < topologyErrors.size(); i++){
			newContainer.addTopologyError(topologyErrors.get(i));
		}
		newContainer.setTopology(this.topology);
		return newContainer;
	}

	public void addTopologyError(TopologyError topologyError) {
		topologyErrors.add(topologyError);
	}

	public void demoteToError(TopologyError topologyError) {
		if(!topologyError.isException())
			return;
		topologyError.setException(false);
		numberOfExceptions--;
	}

	public String getErrorFid() {
		return topologyErrors.size()+"";
	}

	public int getNumberOfErrors() {
		return topologyErrors.size();
	}

	public TopologyError getTopologyError(int index) {
		return topologyErrors.get(index);
	}

	//FIXME Redesign getTopologyErrorsByLyr to reduce code duplication
	public List<TopologyError> getTopologyErrors(String ruleName,
			int shapeType, FLyrVect sourceLayer, IProjection desiredProjection,
			boolean includeExceptions) {
		
		
		List<TopologyError> solution = new ArrayList<TopologyError>();
		Iterator<TopologyError> iterator = topologyErrors.iterator();
		while (iterator.hasNext()) {
			TopologyError error = iterator.next();

			// includeExceptions filter
			if (!includeExceptions) {
				if (error.isException())
					continue;
			}// if

			// shapeType filter
			if (error.getShapeType() != shapeType)
				continue;

			// rule name filter
			ITopologyRule rule = error.getViolatedRule();
			if (!rule.getName().equalsIgnoreCase(ruleName))
				continue;

			// source layer or destination layer filter
			IProjection errorProjection = null;
			FLyrVect originLayer = error.getOriginLayer();
			if (!(originLayer != null && originLayer.equals(sourceLayer))) {
				FLyrVect destinationLayer = error.getDestinationLayer();
				if (!(destinationLayer != null && destinationLayer
						.equals(sourceLayer))) {
					continue;
				} else {
					errorProjection = destinationLayer.getSource()
							.getProjection();
				}
			} else {
				errorProjection = originLayer.getSource().getProjection();
			}

			// reprojection
			if (errorProjection != null
					&& desiredProjection != null
					&& !(errorProjection.getAbrev()
							.equalsIgnoreCase(desiredProjection.getAbrev()))) {
				ICoordTrans trans = errorProjection.getCT(desiredProjection);
				error.getGeometry().reProject(trans);
			}

			solution.add(error);
		}// while

		return solution;
	}

	public List<TopologyError> getTopologyErrorsByLyr(FLyrVect layer,
			IProjection desiredProjection, boolean includeExceptions) {
		List<TopologyError> solution = new ArrayList<TopologyError>();
		Iterator<TopologyError> iterator = topologyErrors.iterator();
		while (iterator.hasNext()) {
			TopologyError error = iterator.next();

			// includeExceptions filter
			if (!includeExceptions) {
				if (error.isException())
					continue;
			}// if

			IProjection errorProjection = null;
			FLyrVect originLayer = error.getOriginLayer();
			FLyrVect destinationLayer = null;
			if (originLayer == null) {
				destinationLayer = error.getDestinationLayer();
				if (destinationLayer != null) {
					errorProjection = destinationLayer.getSource().getProjection();
				}
			} else {
				errorProjection = originLayer.getSource().getProjection();
			}

			// reprojection
			if (errorProjection != null
					&& desiredProjection != null
					&& !(errorProjection.getAbrev()
							.equalsIgnoreCase(desiredProjection.getAbrev()))) {
				ICoordTrans trans = errorProjection.getCT(desiredProjection);
				error.getGeometry().reProject(trans);
			}
			if(layer == originLayer || layer == destinationLayer)
				solution.add(error);
		}// while

		return solution;
	}

	public List<TopologyError> getTopologyErrorsByRule(String ruleName,
			IProjection desiredProjection, boolean includeExceptions) {
		List<TopologyError> solution = new ArrayList<TopologyError>();
		Iterator<TopologyError> iterator = topologyErrors.iterator();
		while (iterator.hasNext()) {
			TopologyError error = iterator.next();

			// includeExceptions filter
			if (!includeExceptions) {
				if (error.isException())
					continue;
			}// if

			
			// rule name filter
			ITopologyRule rule = error.getViolatedRule();
			if (!rule.getName().equalsIgnoreCase(ruleName))
				continue;

			IProjection errorProjection = null;
			FLyrVect originLayer = error.getOriginLayer();
			if (originLayer == null) {
				FLyrVect destinationLayer = error.getDestinationLayer();
				if (destinationLayer != null) {
					errorProjection = destinationLayer.getSource().getProjection();
				}
			} else {
				errorProjection = originLayer.getSource().getProjection();
			}

			// reprojection
			if (errorProjection != null
					&& desiredProjection != null
					&& !(errorProjection.getAbrev()
							.equalsIgnoreCase(desiredProjection.getAbrev()))) {
				ICoordTrans trans = errorProjection.getCT(desiredProjection);
				error.getGeometry().reProject(trans);
			}

			solution.add(error);
		}// while

		return solution;
	}

	public List<TopologyError> getTopologyErrorsByShapeType(int shapeType,
			IProjection desiredProjection, boolean includeExceptions) {
		List<TopologyError> solution = new ArrayList<TopologyError>();
		Iterator<TopologyError> iterator = topologyErrors.iterator();
		while (iterator.hasNext()) {
			TopologyError error = iterator.next();

			// includeExceptions filter
			if (!includeExceptions) {
				if (error.isException())
					continue;
			}// if

			// shapeType filter
			if (error.getShapeType() != shapeType)
				continue;
			
			IProjection errorProjection = null;
			FLyrVect originLayer = error.getOriginLayer();
			if (originLayer == null) {
				FLyrVect destinationLayer = error.getDestinationLayer();
				if (destinationLayer != null) {
					errorProjection = destinationLayer.getSource().getProjection();
				}
			} else {
				errorProjection = originLayer.getSource().getProjection();
			}

			// reprojection
			if (errorProjection != null
					&& desiredProjection != null
					&& !(errorProjection.getAbrev()
							.equalsIgnoreCase(desiredProjection.getAbrev()))) {
				ICoordTrans trans = errorProjection.getCT(desiredProjection);
				error.getGeometry().reProject(trans);
			}
			solution.add(error);
		}// while

		return solution;
	}

	public void markAsTopologyException(TopologyError topologyError) {
		if(topologyError.isException())
			return;
		
		topologyError.setException(true);
		numberOfExceptions++;
		
	}
	
	public void clear() {
		topologyErrors.clear();
	}

	
	// IMPLEMENTATION OF IPERSISTENCE
	public String getClassName() {
		return this.getClass().getName();
	}

	/*
	 * Como este ErrorContainer trabaja con todo en memoria, se pueden persistir
	 * los errores topologicos.
	 * 
	 * En el caso de error containers basados en drivers, se persistiría la información
	 * necesaria para 
	 * volver a cargar el driver (ruta del shp, tabla de bbdd, etc.)
	 */
	public XMLEntity getXMLEntity() {
		XMLEntity solution = new XMLEntity();
		solution.putProperty("className", getClassName());
		solution.putProperty("numberOfExceptions", numberOfExceptions);
		solution.putProperty("numberOfErrors", topologyErrors.size());
		for(int i = 0; i < topologyErrors.size(); i++){
			TopologyError error = topologyErrors.get(i);
			XMLEntity entity = error.getXMLEntity();
			solution.addChild(entity);
		}
		return solution;
	}

	public void setXMLEntity(XMLEntity xml) {
		if(xml.contains("numberOfExceptions")){
			this.numberOfExceptions = xml.getIntProperty("numberOfExceptions");
		}
		
		if(xml.contains("numberOfErrors")){
			int numberOfErrors = xml.getIntProperty("numberOfErrors");
			for(int i = 0; i < numberOfErrors; i++){
				XMLEntity errorXML = xml.getChild(i);
				TopologyError error = new TopologyError(topology);
				error.setXMLEntity(errorXML);
				topologyErrors.add(error);
			}//for
		}//if

	}

	public int getNumberOfExceptions() {
		return this.numberOfExceptions;
	}

	public void removeErrorsByLayer(FLyrVect layer) {
		Iterator<TopologyError> iterator = topologyErrors.iterator();
		while(iterator.hasNext()){
			TopologyError error = iterator.next();
			FLyrVect originLyr = error.getOriginLayer();
			FLyrVect destinationLyr = error.getDestinationLayer();
			if(originLyr == layer || destinationLyr == layer){
				iterator.remove();
			}
		}
	}

	public void removeErrorsByRule(String ruleName) {
		Iterator<TopologyError> iterator = topologyErrors.iterator();
		while(iterator.hasNext()){
			TopologyError error = iterator.next();
			ITopologyRule violatedRule = error.getViolatedRule();
			if(violatedRule.getName().equalsIgnoreCase(ruleName)){
				iterator.remove();
			}
		}
	}
	
	/**
	 * Returns a representation of the topology errors contained in as a fmap
	 * layer.
	 * 
	 * @param name name of the layer
	 * @projection projection of the layer
	 */
	public FLyrVect getAsFMapLayer(String name, IProjection projection) {
		FLayerGenericVectorial solution = new
		 	FLayerGenericVectorial();
		solution.setName(name);
		solution.setProjection(projection);
		solution.setDriver(new TopologyErrorMemoryDriver(name, this));
		try {
			solution.load();
			VectorialUniqueValueLegend defaultLegend = 
				LegendFactory.createVectorialUniqueValueLegend(FShape.MULTI);
			defaultLegend.setClassifyingFieldNames(new String[] {TopologyErrorMemoryDriver.LEGEND_FIELD});
			defaultLegend.setClassifyingFieldTypes(new int[]{Types.VARCHAR});
			defaultLegend.setDefaultSymbol(SymbologyFactory.
					createDefaultSymbolByShapeType(FShape.MULTI, Color.BLACK));
			
			
			Map<ITopologyRule, ITopologyRule> violatedRules =
				new HashMap<ITopologyRule, ITopologyRule>();
			
			int numberOfErrors = topology.getNumberOfErrors();
			for(int i = 0; i < numberOfErrors; i++){
				TopologyError error = topology.getTopologyError(i);
				ITopologyRule violatedRule = error.getViolatedRule();
				if(violatedRules.get(violatedRule) != null){
					continue;
				}else{
					ISymbol symbol = violatedRule.getErrorSymbol();
					defaultLegend.addSymbol(ValueFactory.createValue(violatedRule.getName()), symbol);
				}
			}
			
			
			/*
			Try to avoid legend entries for rules that hasnt been violated.
			List<ITopologyRule> rules = topology.getAllRules();
			//Now we are going to set a symbol for each kind of topology rule
			ISymbol theSymbol = null;
			for(int i = 0; i < rules.size(); i++){
				ITopologyRule rule = rules.get(i);
				
				theSymbol = rule.getErrorSymbol();
				defaultLegend.addSymbol(ValueFactory.createValue(rule.getName()), theSymbol);	
			}//for
			*/
			
			
			
			
			solution.setLegend(defaultLegend);
			
		} catch (LoadLayerException e) {
			e.printStackTrace();
		}
		
		
		return solution;
	}

	public void removeError(TopologyError topologyError) {
		//TODO register listeners to notify this
		this.topologyErrors.remove(topologyError);
	}

}
