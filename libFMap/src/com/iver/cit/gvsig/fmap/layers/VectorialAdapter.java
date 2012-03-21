/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib��ez, 50
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
package com.iver.cit.gvsig.fmap.layers;

import java.awt.Image;
import java.awt.geom.Rectangle2D;

import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.AttrQueryFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.AttrQuerySelectionFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.DefaultFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.IndexedSptQueryFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.SpatialQueryFeatureIterator;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;

/**
 * Clase padre de los adaptadores de los drivers. De momento mantiene solo el
 * �ndice creado sobre la capa
 */
public abstract class VectorialAdapter implements ReadableVectorial {
	protected VectorialDriver driver;
	/**
	 * Spatial index of the driver's data source
	 * */
	protected ISpatialIndex spatialIndex;
	/**
	 * Projection of the data in the data source
	 */
	protected IProjection projection;

	/**
	 * Establece el driver sobre el que act�a el adaptador
	 *
	 * @param driver
	 */
	public void setDriver(VectorialDriver driver) {
		this.driver = driver;
	}

	/**
	 * Obtiene una referencia al objeto que implementa la interfaz vectorial con
	 * el fin de que las Strategy puedan optimizar en funci�n del driver.
	 *
	 * @return VectorialDriver
	 */
	public VectorialDriver getDriver() {
		return driver;
	}

	/**
	 * Devuelve el DataSource a pasrtir del nombre.
	 *
	 * @return DataSource.
	 *
	 * @throws ReadDriverException
	 */
	public abstract SelectableDataSource getRecordset()
			throws ReadDriverException;

	/**
	 * Por defecto devuelve null, y se le pone el icono por defecto. Si el
	 * driver reescribe este m�todo, se usar� este icono en el TOC.
	 *
	 * @return
	 */
	public Image getImageIcon() {
		return null;
	}

	public DriverAttributes getDriverAttributes() {
		if (driver != null)
			return driver.getDriverAttributes();
		return null;
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getShapeCount()
	 */
	public int getShapeCount() throws ReadDriverException {
		return getDriver().getShapeCount();
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getFullExtent()
	 */
	public Rectangle2D getFullExtent() throws ReadDriverException {
		try {
			Rectangle2D aux = getDriver().getFullExtent();
			// Para evitar que una nueva capa a�adida a una vista vac�a no
			// tenga un lienzo para pintar.
			if (aux == null)
				aux = new Rectangle2D.Double(1,1,10,10);
			return aux;
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getDriver().getName(),e);
		}
	}

	/**
	 * En la implementaci�n por defecto podemos hacer que cada feature tenga ID =
	 * numero de registro. En el DBAdapter podr�amos "overrride" este m�todo y
	 * poner como ID de la Feature el campo �nico escogido en la base de datos.
	 *
	 * @param numReg
	 * @return
	 */
	public IFeature getFeature(int numReg) throws ReadDriverException {
		IGeometry geom;
		IFeature feat = null;
		try {
			geom = getShape(numReg);
			DataSource rs = getRecordset();
			Value[] regAtt = new Value[rs.getFieldCount()];
			for (int fieldId = 0; fieldId < rs.getFieldCount(); fieldId++) {
				regAtt[fieldId] = rs.getFieldValue(numReg, fieldId);
			}

			feat = new DefaultFeature(geom, regAtt, numReg + "");
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getDriver().getName(),e);
		}
		return feat;
	}

	public IFeatureIterator getFeatureIterator() throws ReadDriverException{
		return new DefaultFeatureIterator(this, projection, null, null);
	}

	public IFeatureIterator getFeatureIterator(String[] fields, IProjection newProjection)
	throws ReadDriverException{
		return new DefaultFeatureIterator(this, projection, newProjection, fields);
	}

	/**
	* Return a feature iterator from a given sql statement.
	* <br>
	* In this case, the statement will have the "projection" operator
	* (select campo1, campo2, ...etc) and the "selection" operator (where ....)
	* @param sql statement which define a filter
	* @return feature iterator
	* */
	public IFeatureIterator getFeatureIterator(String sql,
								IProjection newProjection) throws ReadDriverException{

		return new AttrQueryFeatureIterator(this, projection, newProjection, sql);
	}


	/**
	* Makes an spatial query returning a feature iterator over the features which intersects
	* or are contained in the rectangle query. Applies a restriction to the alphanumeric fields
	* returned by the iterator.
	* @param rect
	* @param fields
	* @return
	 * @throws ReadDriverException
	*/
	public IFeatureIterator getFeatureIterator(Rectangle2D rect, String[] fields,
												IProjection newProjection,
												boolean fastIteration) throws ReadDriverException{
		if(spatialIndex != null){
			try {
				if(isSpatialIndexNecessary(rect)) {
					IndexedSptQueryFeatureIterator it = new IndexedSptQueryFeatureIterator(this, projection, newProjection, fields, rect, spatialIndex, fastIteration); 
					if (it == null) {
						setSpatialIndex(null);
					}
					else
						return it;
				}
			} catch (ExpansionFileReadException e) {
				e.printStackTrace();
				throw new ReadDriverException("Error al iterar la capa", e);
			}
			catch (Exception e) {
				e.printStackTrace();
				setSpatialIndex(null);
			}
			
		}//if
		return new SpatialQueryFeatureIterator(this, projection, newProjection, fields, rect, fastIteration);


	}


	/*
	 * this method is copied from ShpStrategy
	 * */
	/**
	 * Decides if for a given Rectangle2D extent, is worthy to use an spatial index
	 * (or a secuential scan)
	 * @param extent Rectangle2D used to filter features
	 * @return true if spatial index search is worthy, or false
	 */
	protected boolean isSpatialIndexNecessary(Rectangle2D extent) throws ReadDriverException, ExpansionFileReadException {
		Rectangle2D driverExtent = getFullExtent();
		double areaExtent = extent.getWidth() * extent.getHeight();
		double areaFullExtent = driverExtent.getWidth() *
			                         driverExtent.getHeight();
		return areaExtent < (areaFullExtent / 4.0);

	}

    public ISpatialIndex getSpatialIndex(){
    	return spatialIndex;

    }
    public void setSpatialIndex(ISpatialIndex spatialIndex){
    	this.spatialIndex = spatialIndex;
    }

    public void setProjection(IProjection projection){
    	this.projection = projection;
    }

    public IProjection getProjection(){
    	return projection;
    }

    public IFeatureIterator getFeatureIterator(String sql, IProjection newProjection, boolean withSelection) throws ReadDriverException {
		if (withSelection)
			return new AttrQuerySelectionFeatureIterator(this, projection, newProjection, sql);
		else
			return getFeatureIterator(sql,newProjection);
	}
	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getFeatureIterator(java.awt.geom.Rectangle2D,
	 *      java.lang.String) Lo sobreescribir�n los adapters para base de datos
	 *      espacial. Por defecto, suponemos un buen acceso aleatorio y usamos
	 *      getFeature(i)
	 */
	/* public IFeatureIterator getFeatureIterator(Rectangle2D r, String strEPSG)
			throws DriverException {
		try {
			return new RandomAccessFeatureIterator(driver, getRecordset(), r,
					strEPSG);
		} catch (DriverLoadException e) {
			throw new DriverException(e);
		}
	}

	public IFeatureIterator getFeatureIterator(String strEPSG)
			throws DriverException {
		try {
			return new RandomAccessFeatureIterator(driver, getRecordset(), strEPSG);
		} catch (DriverLoadException e) {
			throw new DriverException(e);
		}
	} */

}
