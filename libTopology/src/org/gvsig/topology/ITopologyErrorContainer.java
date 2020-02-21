/*
 * Created on 07-sep-2007
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

import java.util.List;

import org.cresques.cts.IProjection;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.utiles.XMLEntity;


/**
 *All  classes that contains TopologyError must 
 *implement this interface.
 */
public interface ITopologyErrorContainer extends Cloneable{
 
	public static final int ONLY_ERRORS = 0;//TODO QUITAR
	 
	public static final int ONLY_EXCEPTIONS = 1;
	 
	public static final int BOTH_ERROR_EXCEPTIONS = 2;
	
	/**
	 * Adds a topology error to the container.
	 * @param topologyError
	 */
	public void addTopologyError(TopologyError topologyError);
	
	/**
	 * Returns the number of topology errors contained.
	 * @return
	 */
	public int getNumberOfErrors();
	
	/**
	 * Returns the number of errors that has been marked as exceptions
	 * @return
	 */
	public int getNumberOfExceptions();
	
	/**
	 * Clear all contained errors.
	 */
	public void clear();
	
	/**
	 * Returns the topology error at position index.
	 * @param index
	 * @return
	 */
	public TopologyError getTopologyError(int index);
	
	/**
	 *Returns the contained errors. In function
	 *of the specified params, returned errors 
	 *will be reprojected, filtered by rule, geometry type,
	 *etc.
	 */
	public List<TopologyError> getTopologyErrors(String ruleName, int shapeType, FLyrVect sourceLayer, IProjection desiredProjection, boolean includeExceptions);
	
	/**
	 * Returns the contained errors filtered by violated rule.
	 * @param ruleName
	 * @param desiredProjection
	 * @param includeExceptions
	 * @return
	 */
	public List<TopologyError> getTopologyErrorsByRule(String ruleName, IProjection desiredProjection, boolean includeExceptions);
	
	/**
	 * Returns the contained errors filtered by type of error geometry.
	 * @param shapeType
	 * @param desiredProjection
	 * @param includeExceptions
	 * @return
	 */
	public List<TopologyError> getTopologyErrorsByShapeType(int shapeType, IProjection desiredProjection, boolean includeExceptions);
	
	/**
	 * Returns the contained errors filtered by layer that causes the error.
	 * @param layer
	 * @param desiredProjection
	 * @param includeExceptions
	 * @return
	 */
	public List<TopologyError> getTopologyErrorsByLyr(FLyrVect layer, IProjection desiredProjection, boolean includeExceptions);
	
	/**
	 * Marks an error as an exception.
	 * 
	 * @param error to mark as an exception
	 */
	public void markAsTopologyException(TopologyError topologyError);
	
	/**
	 * Mark the given error (until now consideer as an exception) as "not tolered".
	 * 
	 * @param topologyError error to adds to the not tolered errors list.
	 */
	public void demoteToError(TopologyError topologyError);
	
	public void removeErrorsByLayer(FLyrVect layer);
	
	public void removeErrorsByRule(String ruleName);
	
	/**
	 * Removes the passed TopologyError
	 * (usually as result of a topology fix operation)
	 * @param topologyError
	 */
	public void removeError(TopologyError topologyError);
	/**
	 * Returns an unique identifier for the error in the container
	 * context.
	 * @return
	 */
	public  String getErrorFid();
	
	/**
	 * XML Castor's persistence method.
	 * @param xml
	 * @throws XMLException
	 */
	public void setXMLEntity(XMLEntity xml) throws XMLException;
	
	public XMLEntity getXMLEntity() throws XMLException;

	public Object clone();
	
	
	public FLyrVect getAsFMapLayer(String name, IProjection projection);
	
	public void setTopology(Topology topology);
	
	public Topology getTopology();
}
 
