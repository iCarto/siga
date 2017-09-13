/*
 * Created on 01-mar-2006
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
 * $Id$
 * $Log$
 * Revision 1.3  2007-09-19 16:08:13  jaume
 * ReadExpansionFileException removed from this context
 *
 * Revision 1.2  2007/03/06 16:47:58  caballero
 * Exceptions
 *
 * Revision 1.1  2006/06/20 18:20:45  azabala
 * first version in cvs
 *
 * Revision 1.2  2006/06/02 18:21:28  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/05/24 21:09:47  azabala
 * primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
 *
 * Revision 1.4  2006/03/21 19:29:36  azabala
 * *** empty log message ***
 *
 * Revision 1.3  2006/03/14 18:32:46  fjp
 * Cambio con LayerDefinition para que sea compatible con la definición de tablas también.
 *
 * Revision 1.2  2006/03/07 21:01:33  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/06 19:48:39  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/03/05 19:59:47  azabala
 * *** empty log message ***
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.spatialjoin.fmap;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.ProcessVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StartVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.LayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.geoprocess.core.fmap.DefinitionUtils;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureFactory;
import com.iver.cit.gvsig.geoprocess.core.fmap.FeatureProcessor;
import com.iver.cit.gvsig.geoprocess.core.fmap.SummarizationFunction;
import com.iver.cit.gvsig.geoprocess.core.fmap.XTypes;

/**
 * This visitor implements Intersect Geometry Spatial Join.
 *
 * It is a particular case of 1-M relationship (a feature of layer A always be
 * related to M feature of layer B)
 * It allows to apply a sumarization function over numeric values of target
 * layer (sum, avg, min, max). If it doesnt find any feature of target layer
 * wich intersects with a given feature of source layer, these values will have
 * 0d value.
 *
 * @author azabala
 *
 */
public class IntersectSpatialJoinVisitor implements SpatialJoinVisitor {

	/**
	 * Needed to create layer definition
	 */
	private FLyrVect sourceLayer;
	/**
	 * Reads data of features for the source layer
	 */
	private SelectableDataSource sourceRecordset;

	/**
	 * Recordset of this layer
	 */
	private SelectableDataSource targetRecordset;

	/**
	 * Strategy to do querys against target Layer
	 */
	private Strategy strategy;

	/**
	 * Maps for each numerical field of target layer its sumarization functions
	 */
	private Map fields_sumarizeFunc;

	/**
	 * Visitor that finds features of target layer that intersects with a given
	 * feature of source layer
	 */
	private IntersectsFinderFeatureVisitor visitor;

	/**
	 * Processes results of dissolve operations (save them in a file, or cache
	 * them in memory, etc)
	 */
	private FeatureProcessor featureProcessor;

	private LayerDefinition resultLayerDefinition;

	private boolean onlySecondLyrSelection;

	public IntersectSpatialJoinVisitor(FLyrVect sourceLayer,
										FLyrVect targetLayer,
										Map fields_sumarizeFunct,
										FeatureProcessor processor) throws ReadDriverException {
		this.sourceLayer = sourceLayer;
		this.sourceRecordset = sourceLayer.getRecordset();
		this.featureProcessor = processor;
		this.fields_sumarizeFunc = fields_sumarizeFunct;
		this.targetRecordset = targetLayer.getRecordset();
		this.visitor = new IntersectsFinderFeatureVisitor(fields_sumarizeFunc);
	}

	public void visit(IGeometry g, int index) throws VisitorException, ProcessVisitorException {
		if(g == null)
			return;
		IFeature solution = null;
		visitor.setQueryGeometry(g.toJTSGeometry());
		try {
			if(onlySecondLyrSelection)
				visitor.setSelection(targetRecordset.getSelection());
			strategy.process(visitor, g.getBounds2D());
			solution = createFeature(g, index, visitor.getNumIntersections());
			featureProcessor.processFeature(solution);
			resetFunctions();
			visitor.clearIntersections();
		} catch (ReadDriverException e) {
			throw new ProcessVisitorException(targetRecordset.getName(),e,
					"Error al buscar las intersecciones de una geometria durante un spatial join");
		} 
	}

	public String getProcessDescription() {
		return "Spatial joining by intersects criteria";
	}

	public void resetFunctions(){
		Iterator fieldsIt = fields_sumarizeFunc.keySet().iterator();
		while (fieldsIt.hasNext()) {
			String field = (String) fieldsIt.next();
			SummarizationFunction[] functions =
				(SummarizationFunction[]) fields_sumarizeFunc.get(field);
			for (int i = 0; i < functions.length; i++) {
				functions[i].reset();
			}// for
		}// while
	}


	private IFeature createFeature(IGeometry g, int index, int numIntersections)
			throws ReadDriverException {
		IFeature solution = null;
		int numSourceFields = this.sourceRecordset.getFieldCount();
		ArrayList values = new ArrayList();
		for (int i = 0; i < numSourceFields; i++) {
			values.add(sourceRecordset.getFieldValue(index, i));
		}
		//target layer
		Iterator fieldsIt = fields_sumarizeFunc.keySet().iterator();
		while (fieldsIt.hasNext()) {
			String field = (String) fieldsIt.next();
			SummarizationFunction[] functions =
				(SummarizationFunction[]) fields_sumarizeFunc.get(field);
			for (int i = 0; i < functions.length; i++) {
				values.add(functions[i].getSumarizeValue());
			}// for
		}// while
		values.add(ValueFactory.createValue(numIntersections));
		Value[] attrs = new Value[values.size()];
		values.toArray(attrs);
		solution = FeatureFactory.createFeature(attrs, g);
		return solution;
	}

	public void stop(FLayer layer) throws StopVisitorException {
		featureProcessor.finish();

	}

	public boolean start(FLayer layer) throws StartVisitorException {
		this.featureProcessor.start();
		return true;
	}


	public ILayerDefinition getResultLayerDefinition(){
		if(this.resultLayerDefinition == null){
			ArrayList<FieldDescription> fields = new ArrayList<FieldDescription>();
			resultLayerDefinition = new SHPLayerDefinition();
			//result layer will be exactly similar to firstLayer with
			//new attributes
			try {
				resultLayerDefinition.setShapeType(sourceLayer.getShapeType());
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			//first layer attributes
			try {
			    for (FieldDescription fieldDesc : sourceRecordset.getFieldsDescription()) {
			        fields.add(fieldDesc);
			    }
			} catch (ReadDriverException e) {
			    e.printStackTrace();
			}

			//target layer attributes
			Iterator fieldsIt = fields_sumarizeFunc.keySet().iterator();
			FieldDescription fieldDesc;
			while(fieldsIt.hasNext()){
				String field = (String) fieldsIt.next();
				SummarizationFunction[] functions =
					(SummarizationFunction[]) fields_sumarizeFunc.get(field);
				for(int i = 0; i < functions.length; i++){
					fieldDesc = new FieldDescription();
//					to avoid truncation of field names (f.example shp)
					//we only catch three first letters
					String shortName = null;
					if(field.length() > 3)
						shortName = field.substring(0,3);
					else
						shortName = field;
					fieldDesc.setFieldName(
							shortName + "_" + functions[i].toString());
					fieldDesc.setFieldType(XTypes.DOUBLE);
					int fieldLenght = DefinitionUtils.getDataTypeLength(XTypes.DOUBLE);
					fieldDesc.setFieldLength(fieldLenght);
					fieldDesc.setFieldDecimalCount(DefinitionUtils.NUM_DECIMALS);
					fields.add(fieldDesc);
				}//for
			}//while

			//finally, we add to the result schema of M-N spatial join
			//the number of features intersected of layer b
			fieldDesc = new FieldDescription();
			fieldDesc.setFieldName("NUM_RELA");
			fieldDesc.setFieldType(XTypes.INTEGER);
			fieldDesc.setFieldLength(DefinitionUtils.
					getDataTypeLength(XTypes.INTEGER));
			fields.add(fieldDesc);

			FieldDescription[] fieldsDesc = null;
			if(fields.size() == 0){
				fieldsDesc = new FieldDescription[0];
			}else{
				fieldsDesc = new FieldDescription[fields.size()];
				fields.toArray(fieldsDesc);
			}
			resultLayerDefinition.setFieldsDesc(fieldsDesc);
		}//if result == null
		return resultLayerDefinition;
	}

	public void setFeatureProcessor(FeatureProcessor processor) {
		this.featureProcessor = processor;
	}

	public void setCancelableStrategy(Strategy secondLyrStrategy) {
		this.strategy = secondLyrStrategy;
	}

	public void setOnlySecondLyrSelection(boolean onlySecondLayerSelection) {
		this.onlySecondLyrSelection = onlySecondLayerSelection;
	}

}
