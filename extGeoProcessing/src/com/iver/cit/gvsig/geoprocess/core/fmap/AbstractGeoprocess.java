/*
 * Created on 16-feb-2006
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
* Revision 1.10  2007-03-06 16:47:58  caballero
* Exceptions
*
* Revision 1.9  2006/10/20 14:28:30  azabala
* changed visibility of createLayerFrom method to protected
*
* Revision 1.8  2006/09/15 10:42:54  caballero
* extensibilidad de documentos
*
* Revision 1.7  2006/08/29 07:56:30  cesar
* Rename the *View* family of classes to *Window* (ie: SingletonView to SingletonWindow, ViewInfo to WindowInfo, etc)
*
* Revision 1.6  2006/08/29 07:21:09  cesar
* Rename com.iver.cit.gvsig.fmap.Fmap class to com.iver.cit.gvsig.fmap.MapContext
*
* Revision 1.5  2006/06/29 07:33:57  fjp
* Cambios ISchemaManager y IFieldManager por terminar
*
* Revision 1.4  2006/06/20 18:19:43  azabala
* refactorización para que todos los nuevos geoprocesos cuelguen del paquete impl
*
* Revision 1.3  2006/06/12 19:15:38  azabala
* cambios para poder trabajar en geoprocessing con capas MULTI (dxf, jdbc, etc)
*
* Revision 1.2  2006/05/31 09:10:12  fjp
* Ubicación de IWriter
*
* Revision 1.1  2006/05/24 21:12:16  azabala
* primera version en cvs despues de refactoring orientado a crear un framework extensible de geoprocessing
*
* Revision 1.7  2006/05/01 19:19:10  azabala
* la capa resultado tiene como nombre ahora solo el nombre del fichero (no la ruta completa)
*
* Revision 1.6  2006/03/17 19:52:19  azabala
* *** empty log message ***
*
* Revision 1.5  2006/03/14 19:34:18  azabala
* *** empty log message ***
*
* Revision 1.4  2006/03/14 18:32:46  fjp
* Cambio con LayerDefinition para que sea compatible con la definición de tablas también.
*
* Revision 1.3  2006/03/07 21:01:33  azabala
* *** empty log message ***
*
* Revision 1.2  2006/03/06 19:48:39  azabala
* *** empty log message ***
*
* Revision 1.1  2006/02/17 16:04:28  azabala
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.geoprocess.core.fmap;

import java.io.File;
import java.util.Map;

import org.cresques.cts.IProjection;

import com.hardcode.gdbms.driver.exceptions.SchemaEditionException;
import com.iver.cit.gvsig.fmap.drivers.ILayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.SHPLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.edition.ISchemaManager;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.writers.shp.MultiShpWriter;
import com.iver.cit.gvsig.fmap.edition.writers.shp.ShpWriter;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
/**
 * Base class with all commong logic to geoprocesses
 * @author azabala
 *
 */
public abstract class AbstractGeoprocess implements IGeoprocess {
	/**
	 * Writes result features in a persistent
	 * data format.
	 */
	protected IWriter writer;
	/**
	 * Creates schema of result data format.
	 */
	protected ISchemaManager schemaManager;
	/**
	 * All geoprocesses at least work with one
	 * vectorial layer
	 */
	protected FLyrVect firstLayer;

	public abstract void setParameters(Map params) throws GeoprocessException;

	public abstract void checkPreconditions() throws GeoprocessException;

	public abstract void process() throws GeoprocessException;

	public  void cancel(){
		try {
			schemaManager.removeSchema("");
		} catch (SchemaEditionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}


	protected FLayer createLayerFrom(IWriter writer) throws GeoprocessException{
        SHPLayerDefinition tableDef = (SHPLayerDefinition) ((ShpWriter) writer)
                .getTableDefinition();
		FLyrVect solution = null;
		//Para evitar todos estos casts, hay que meter la
		//interfaz FileWriter
		String fileName = ((ShpWriter)writer).getShpPath();
		String layerName = null;
		int fileNameStart = fileName.lastIndexOf(File.separator) + 1;
		if(fileNameStart == -1)
			fileNameStart = 0;
		layerName = fileName.substring(fileNameStart, fileName.length() /*-1*/);
		File file = new File(fileName);
        IProjection proj = (tableDef.getProjection() != null) ? tableDef
                .getProjection() : firstLayer.getProjection();
		//TODO La proyeccion se deberia leer del WRITER
		try {
			IndexedShpDriver driver = new IndexedShpDriver();
			driver.open(file);
			driver.initialize();
			solution = (FLyrVect) LayerFactory.createLayer(layerName,
									driver,
									file,
									proj);
			return solution;
		} catch (Exception e) {
			throw new GeoprocessException("Problemas al cargar la capa resultado", e);
		}
	}

	/**
	 * Creates a new Layer with:
	 * a) the same projection than input layer.
	 * b) an adapter created to work with writer's persistent store
	 *
	 * @return FLyrVect with geoprocess result
	 */
	public FLayer getResult() throws GeoprocessException {
		if(! (writer instanceof MultiShpWriter)){
			return createLayerFrom(writer);
		}else{
			IWriter[] writers = ((MultiShpWriter)writer).getWriters();
			if(writers.length > 1){
				FLayers solution = new FLayers();//(map,null);
				solution.setMapContext(firstLayer.getMapContext());
				String name = ((MultiShpWriter)writer).getFileName();
				int fileNameStart = name.lastIndexOf(File.separator) + 1;
				if(fileNameStart == -1)
					fileNameStart = 0;
				name = name.substring(fileNameStart, name.length());
				solution.setName(name);
				for(int i = 0; i < writers.length; i++){
					solution.addLayer(createLayerFrom(writers[i]));
				}
				return solution;
			}else if(writers.length ==0){
				return null;
			}
			else {
				return createLayerFrom(writers[0]);
			}
		}
	}

	public void setResultLayerProperties(IWriter writer,
			ISchemaManager schemaManager) {
		this.writer = writer;
		this.schemaManager = schemaManager;
	}

	public abstract ILayerDefinition createLayerDefinition();

}

