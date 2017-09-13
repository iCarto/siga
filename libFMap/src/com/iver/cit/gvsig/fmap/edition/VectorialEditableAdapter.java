/*
 * Created on 12-ene-2006 by fjp
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
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
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
 * Revision 1.76  2007-09-19 16:25:04  jaume
 * ReadExpansionFileException removed from this context
 *
 * Revision 1.75  2007/07/25 06:52:08  caballero
 * reproject
 *
 * Revision 1.74  2007/06/22 10:52:30  caballero
 * start y stop
 *
 * Revision 1.73  2007/05/29 19:09:07  azabala
 * using of spatial index in spatial iteration now depends of the query area (to avoid using index in full scan queries)
 *
 * Revision 1.72  2007/05/07 09:02:47  caballero
 * editing memory
 *
 * Revision 1.71  2007/04/25 14:42:24  caballero
 * BoundShape
 *
 * Revision 1.70  2007/04/19 17:28:24  azabala
 * changes in ReadableVectorial interface (new iteration modes)
 *
 * Revision 1.69  2007/03/06 17:08:55  caballero
 * Exceptions
 *
 * Revision 1.68  2007/02/19 12:36:22  caballero
 * *** empty log message ***
 *
 * Revision 1.67  2007/02/13 11:31:08  caballero
 * optimizo cuando el cambio es alfanumérico
 *
 * Revision 1.66  2007/02/12 08:53:37  caballero
 * FieldExpresion
 *
 * Revision 1.65  2007/02/08 12:03:07  caballero
 * comentar println
 *
 * Revision 1.64  2006/09/21 17:32:11  azabala
 * *** empty log message ***
 *
 * Revision 1.62  2006/08/10 08:20:31  caballero
 * flatness
 *
 * Revision 1.61  2006/07/20 13:03:07  fjp
 * nuevos campos
 *
 * Revision 1.60  2006/07/12 11:48:41  fjp
 * Draft to add, remove and delete fields
 *
 * Revision 1.59  2006/07/12 10:34:52  fjp
 * Draft to add, remove and delete fields
 *
 * Revision 1.58  2006/07/06 08:31:29  fjp
 * Draft to add, remove and delete fields
 *
 * Revision 1.57  2006/06/22 11:38:12  caballero
 * solución error al borrar geometrías
 *
 * Revision 1.56  2006/06/16 10:44:01  fjp
 * Por mandato de Vicente
 *
 * Revision 1.54  2006/06/01 16:15:16  fjp
 * Escritura que permite crear drivers de manera más sencilla.
 *
 * Revision 1.53  2006/05/30 13:03:41  fjp
 * setFlatness solo se debe aplicar a bases de datos espaciales.
 *
 * Revision 1.52  2006/05/24 09:29:30  caballero
 * añadir flatness
 *
 * Revision 1.51  2006/05/16 16:07:19  fjp
 * snapping. Revisar
 *
 * Revision 1.50  2006/05/16 07:07:46  caballero
 * Modificar la geometría desde fuera
 *
 * Revision 1.49  2006/05/15 10:52:23  caballero
 * Saber si se realiza una operación desde la vista o desde la tabla.
 *
 * Revision 1.48  2006/05/09 15:58:37  caballero
 * Para el IGN
 *
 * Revision 1.47  2006/05/09 10:28:28  caballero
 * faltaba controlar undo y redo
 *
 * Revision 1.46  2006/04/27 06:44:56  caballero
 * Solución undo y redo de anotaciones
 *
 * Revision 1.45  2006/04/18 06:56:55  caballero
 * Cambiar VectorialAdapter por ReadableVectorial
 *
 * Revision 1.44  2006/04/12 17:13:39  fjp
 * *** empty log message ***
 *
 * Revision 1.43  2006/04/11 12:12:29  fjp
 * Con edición en PostGIS y guardando pseudo-arcos en los shapes.
 *
 * Revision 1.42  2006/04/11 06:53:20  fjp
 * Preparando el driver de escritura PostGIS
 *
 * Revision 1.41  2006/04/04 11:27:16  fjp
 * Consola escuchando bien y selección en tabla sincronizada cuando hay edición
 *
 * Revision 1.40  2006/04/03 11:04:48  caballero
 * Posibilidad de añadir una anotación
 *
 * Revision 1.39  2006/03/29 06:26:37  caballero
 * acelerar con una imagen las herramientas
 *
 * Revision 1.38  2006/03/23 16:20:52  fjp
 * Un fallo un tanto inverosimil con el mapOverview
 *
 * Revision 1.37  2006/03/23 10:08:11  caballero
 * calculo del fullExtent recorriendo todas las geometrías
 *
 * Revision 1.36  2006/03/22 11:46:29  caballero
 * Editar capa de anotaciones
 *
 * Revision 1.35  2006/02/28 18:15:22  fjp
 * Consola de CAD
 *
 * Revision 1.34  2006/02/24 11:30:32  fjp
 * FUNCIONA!!! (Creo)
 *
 * Revision 1.33  2006/02/24 07:57:58  fjp
 * FUNCIONA!!! (Creo)
 *
 * Revision 1.32  2006/02/23 17:55:45  fjp
 * Preparando para poder editar con el EditionManager
 *
 * Revision 1.31  2006/02/21 16:44:08  fjp
 * Preparando para poder editar con el EditionManager
 *
 * Revision 1.30  2006/02/20 18:14:59  fjp
 * Preparando para poder editar con el EditionManager
 *
 * Revision 1.29  2006/02/20 10:32:54  fjp
 * Preparando para poder editar con el EditionManager
 *
 * Revision 1.28  2006/02/17 13:40:03  fjp
 * Preparando para poder editar con el EditionManager
 *
 * Revision 1.27  2006/02/17 10:41:14  fjp
 * Evento de edición lanzado cuando una capa se pone en edición
 *
 * Revision 1.26  2006/02/17 08:21:19  fjp
 * *** empty log message ***
 *
 * Revision 1.25  2006/02/16 09:38:10  fjp
 * Preparando compatibilidad para bases de datos (y de paso, acelerando :-)
 *
 * Revision 1.24  2006/02/16 09:06:28  caballero
 * commandStack
 *
 * Revision 1.23  2006/02/15 18:16:02  fjp
 * POR TERMINAR
 *
 * Revision 1.22  2006/02/13 18:18:31  fjp
 * POR TERMINAR
 *
 * Revision 1.21  2006/02/10 13:28:23  caballero
 * poder cambiar la selección
 *
 * Revision 1.20  2006/02/09 13:11:54  caballero
 * *** empty log message ***
 *
 * Revision 1.19  2006/02/08 16:45:29  caballero
 * elimnar códio no usado
 *
 * Revision 1.18  2006/02/08 15:18:45  caballero
 * control de las rows eliminadas
 *
 * Revision 1.17  2006/02/07 10:18:44  caballero
 * Con BoundedShape
 *
 * Revision 1.16  2006/02/06 12:01:41  caballero
 * driver del ova
 *
 * Revision 1.15  2006/02/03 14:09:32  fjp
 * Preparando edición
 *
 * Revision 1.14  2006/02/03 12:16:33  fjp
 * Preparando edición
 *
 * Revision 1.13  2006/02/03 11:54:12  caballero
 * tablas con vectorialEditableAdapter en edición
 *
 * Revision 1.11  2006/01/31 08:10:05  caballero
 * cambio de feature a row
 *
 * Revision 1.10  2006/01/30 08:18:14  caballero
 * métodos para deshacer y rehacer
 *
 * Revision 1.9  2006/01/23 17:30:28  caballero
 * coger los datos del ova
 *
 * Revision 1.8  2006/01/23 16:16:16  caballero
 * getRowIndex
 *
 * Revision 1.7  2006/01/20 08:37:10  fjp
 * Preparando la edición
 *
 * Revision 1.6  2006/01/19 12:48:20  caballero
 * poder modificar su vectorial Adapter
 *
 * Revision 1.5  2006/01/19 09:28:11  fjp
 * Preparando la edición
 *
 * Revision 1.4  2006/01/17 10:24:02  fjp
 * Preparando edición
 *
 * Revision 1.3  2006/01/16 12:47:38  fjp
 * Preparando edición
 *
 * Revision 1.2  2006/01/16 11:23:00  fjp
 * Preparando edición
 *
 * Revision 1.1  2006/01/12 13:39:14  fjp
 * preaparar edicion
 *
 *
 */
package com.iver.cit.gvsig.fmap.edition;

import java.awt.Image;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;

import com.hardcode.driverManager.Driver;
import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.cit.gvsig.exceptions.commands.EditionCommandException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileWriteException;
import com.iver.cit.gvsig.exceptions.layers.CancelEditingLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.DefaultFeature;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.AttrQueryFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.AttrQuerySelectionFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.DefaultFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.SpatialQueryFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.QuadtreeJts;

/**
 * @author fjp
 *
 */
public class VectorialEditableAdapter extends EditableAdapter implements
		ReadableVectorial, BoundedShapes {
	protected ReadableVectorial ova;

	protected ISpatialIndex fmapSpatialIndex;

	protected Rectangle2D fullExtent;

	protected Image selectionImage;

	protected BufferedImage handlersImage;


	/*
	 * azo; ReadableVectorial implementations need a reference to
	 * IProjection (of the layer) and to ISpatialIndex (of the layer)
	 * to allow iteration with different criteria
	 * */
	protected IProjection projection;
//	protected ISpatialIndex fmapSpatialIndex;
	private ICoordTrans ct;

	//private double flatness=0.8;
	/*
	 * private class MyFeatureIterator implements IFeatureIterator { int numReg =
	 * 0; Rectangle2D rect; String epsg; IFeatureIterator origFeatIt; boolean
	 * bHasNext = true;
	 *
	 * public MyFeatureIterator(Rectangle2D r, String strEPSG) throws
	 * DriverException { rect = r; epsg = strEPSG; origFeatIt =
	 * ova.getFeatureIterator(r, epsg); } public boolean hasNext() throws
	 * DriverException { return bHasNext; }
	 *
	 * public IFeature next() throws DriverException { IFeature aux =
	 * origFeatIt.next(); return null; }
	 *
	 * public void closeIterator() throws DriverException {
	 *  }
	 *  }
	 */



	public VectorialEditableAdapter() {
		super();
	}

	public void setOriginalVectorialAdapter(ReadableVectorial rv) throws ReadDriverException {
		ova = rv;
		setOriginalDataSource(rv.getRecordset());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#start()
	 */
	public void start() throws ReadDriverException, InitializeDriverException {
		ova.start();
	}

	public IWriter getWriter() {
		if (ova.getDriver() instanceof IWriteable)
		{
			IWriter writer = ((IWriteable) ova.getDriver()).getWriter();
			if (writer instanceof ISpatialWriter)
				return writer;
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#stop()
	 */
	public void stop() throws ReadDriverException {
		ova.stop();
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getShape(int)
	 */
	public IGeometry getShape(int rowIndex) throws ReadDriverException, ExpansionFileReadException {
		// Si no está en el fichero de expansión
		int calculatedIndex = getCalculatedIndex(rowIndex);
		Integer integer = new Integer(calculatedIndex);
		if (!relations.containsKey(integer)) {
			// Si ha sido eliminada
			/*
			 * if (delRows.get(integer.intValue())) { return null; } else {
			 */
			return ova.getShape(calculatedIndex);
			// }
		}
		int num = ((Integer) relations.get(integer)).intValue();
		DefaultRowEdited feat;
		feat = (DefaultRowEdited) expansionFile.getRow(num);
		IGeometry geom=((IFeature) feat.getLinkedRow()).getGeometry();
		if (geom!=null)
			return geom.cloneGeometry();
		return geom;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getShapeType()
	 */
	public int getShapeType() throws ReadDriverException {
		return ova.getShapeType();
	}

	public ReadableVectorial getOriginalAdapter() {
		return ova;
	}

	public VectorialDriver getDriver() {
		return ova.getDriver();
	}

	public void setDriver(VectorialDriver driver) {
		this.ova.setDriver(driver);
	}

	public DriverAttributes getDriverAttributes() {
		return ova.getDriverAttributes();
	}

	/**
	 * DOCUMENT ME!
	 * @throws StartEditionLayerException
	 * @throws StartWriterVisitorException
	 *
	 * @throws EditionException
	 *             DOCUMENT ME!
	 */
	public void startEdition(int sourceType) throws StartWriterVisitorException {
		super.startEdition(sourceType);
		Driver drv = ova.getDriver();
		if (drv instanceof IWriteable)
		{
			setWriter(((IWriteable) drv).getWriter());
		}


		try {
			expansionFile.open();
			if (fmapSpatialIndex == null) {
				// TODO: Si la capa dispone de un índice espacial, hacer
				// algo aquí para que se use ese índice espacial.
//				index = new Quadtree();
				fmapSpatialIndex = new QuadtreeJts();

				for (int i = 0; i < ova.getShapeCount(); i++) {
					Rectangle2D r=null;
					if (ova.getDriver() instanceof BoundedShapes) {
						r = ((BoundedShapes) ova.getDriver()).getShapeBounds(i);
					} else {
						IGeometry g = null;
						g = ((DefaultFeature) ova.getFeature(i))
									.getGeometry();
						if (g == null) {
							continue;
						}

						r = g.getBounds2D();
					}
					if (ct != null) {
						r = ct.convert(r);
					}
//					Envelope e = new Envelope(r.getX(),
//							r.getX() + r.getWidth(), r.getY(), r.getY()
//									+ r.getHeight());
					fmapSpatialIndex.insert(r, new Integer(i));
					if (fullExtent == null) {
						fullExtent = r;
					} else {
						fullExtent = fullExtent.createUnion(r);
					}
				}
			}
		} catch (ReadDriverException e) {
			throw new StartWriterVisitorException(writer.getName(),e);
		}

//		System.err.println("Se han metido en el índice "
//				+ index.queryAll().size() + " geometrías");
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.edition.IEditableSource#getRow(int)
	 */
	public IRowEdited getRow(int index) throws ReadDriverException, ExpansionFileReadException {
		// FJP: Demasiados start - stop hacen que se abra miles de veces el fichero y salte un error. Es mejor
		// no abrir al leer una fila, y si se abre, que se haga en procesos de lectura y escritura en batch.
		// otra posibilidad es abrir y no cerrar. (De todas formas a los 5 segundos lo cierrra el AutomaticDataSource
		// Por cierto, al quitar esto también se gana velocidad.
//		start(); 
		int calculatedIndex = getCalculatedIndex(index);
		Integer integer = new Integer(calculatedIndex);
		// Si no está en el fichero de expansión
		DefaultRowEdited edRow = null;
		if (!relations.containsKey(integer)) {
			edRow = new DefaultRowEdited(ova.getFeature(calculatedIndex),
					IRowEdited.STATUS_ORIGINAL, index);
//			stop();
			return createExternalRow(edRow, 0);
		}
		int num = ((Integer) relations.get(integer)).intValue();
		IRowEdited aux = expansionFile.getRow(num);
		edRow = new DefaultRowEdited(aux.getLinkedRow().cloneRow(), aux
				.getStatus(), index);
//		stop();
		return edRow;
	}

	/**
	 * Elimina una geometria. Si es una geometría original de la capa en edición
	 * se marca como eliminada (haya sido modificada o no). Si es una geometría
	 * añadida posteriormente se invalida en el fichero de expansión, para que
	 * una futura compactación termine con ella.
	 *
	 * @param index
	 *            Índice de la geometría.
	 * @throws ReadDriverException
	 * @throws ExpansionFileReadException
	 *
	 * @throws DriverIOException
	 * @throws IOException
	 */
	public IRow doRemoveRow(int index, int sourceType) throws ReadDriverException, ExpansionFileReadException{
		boolean cancel = fireBeforeRemoveRow(index, sourceType);
		if (cancel)
			return null;
		// Llega el calculatedIndex
		Integer integer = new Integer(index);

		IFeature feat = null;
		delRows.set(index, true);
		// Si la geometría no ha sido modificada
		if (!relations.containsKey(integer)) {
			feat = (DefaultFeature) (ova.getFeature(index));
		} else {
			int num = ((Integer) relations.get(integer)).intValue();
			feat = (IFeature) expansionFile.getRow(num).getLinkedRow();
		}
		// Se actualiza el índice
		if (feat != null) {
		    IGeometry geom = feat.getGeometry();
		    if (geom != null) {
			Rectangle2D r = geom.getBounds2D();
			this.fmapSpatialIndex.delete(r, new Integer(index));
			isFullExtentDirty = true;
		    }
		}
		setSelection(new FBitSet());
		fireAfterRemoveRow(index, sourceType);
		return feat;
	}

	/**
	 * Si se intenta modificar una geometría original de la capa en edición se
	 * añade al fichero de expansión y se registra la posición en la que se
	 * añadió. Si se intenta modificar una geometria que se encuentra en el
	 * fichero de expansión (por ser nueva o original pero modificada) se invoca
	 * el método modifyGeometry y se actualiza el índice de la geometria en el
	 * fichero.
	 *
	 * @param calculatedIndex
	 *            DOCUMENT ME!
	 * @param feat
	 *            DOCUMENT ME!
	 *
	 * @return position inside ExpansionFile
	 * @throws ReadDriverException
	 * @throws ExpansionFileWriteException
	 * @throws ExpansionFileReadException
	 *
	 * @throws IOException
	 * @throws DriverIOException
	 */
	public int doModifyRow(int calculatedIndex, IRow feat, int sourceType) throws ReadDriverException, ExpansionFileWriteException, ExpansionFileReadException {
		boolean cancel = fireBeforeModifyRow(feat, calculatedIndex, sourceType);
		if (cancel)
			return -1;
		int posAnteriorInExpansionFile = -1;
		Integer integer = new Integer(calculatedIndex);

		IFeature featAnt = null;
//		System.err.println("Modifica una Row en la posición: "
//				+ calculatedIndex);
		// Si la geometría no ha sido modificada
		if (!relations.containsKey(integer)) {
			int newPosition = expansionFile.addRow(feat,
					IRowEdited.STATUS_MODIFIED, actualIndexFields);
			relations.put(integer, new Integer(newPosition));

			if (sourceType == EditionEvent.GRAPHIC) {
				// Se actualiza el índice espacial
				featAnt = (DefaultFeature) (ova.getFeature(calculatedIndex));
				IGeometry g = featAnt.getGeometry();
				Rectangle2D rAnt = g.getBounds2D();
				Rectangle2D r = ((IFeature) feat).getGeometry().getBounds2D();
				this.fmapSpatialIndex.delete(rAnt, new Integer(calculatedIndex));
				this.fmapSpatialIndex.insert(r, new Integer(calculatedIndex));
			}
		} else {
			// Obtenemos el índice en el fichero de expansión
			int num = ((Integer) relations.get(integer)).intValue();
			posAnteriorInExpansionFile=num;

			// Obtenemos la geometría para actualiza el índice
			// espacialposteriormente
			featAnt = (IFeature) expansionFile.getRow(num).getLinkedRow();

			/*
			 * Se modifica la geometría y nos guardamos el índice dentro del
			 * fichero de expansión en el que se encuentra la geometría
			 * modificada
			 */
			num = expansionFile.modifyRow(num, feat, actualIndexFields);

			/*
			 * Actualiza la relación del índice de la geometría al índice en el
			 * fichero de expansión.
			 */
			relations.put(integer, new Integer(num));
			if (sourceType == EditionEvent.GRAPHIC) {
				// Se modifica el índice espacial
				Rectangle2D rAnt = featAnt.getGeometry().getBounds2D();
				Rectangle2D r = ((IFeature) feat).getGeometry().getBounds2D();
				this.fmapSpatialIndex.delete(rAnt, new Integer(calculatedIndex));
				this.fmapSpatialIndex.insert(r, new Integer(calculatedIndex));
			}

		}
		isFullExtentDirty = true;
		fireAfterModifyRow(calculatedIndex, sourceType);
		return posAnteriorInExpansionFile;
	}

	/**
	 * Actualiza en el mapa de índices, la posición en la que estaba la
	 * geometría antes de ser modificada. Se marca como válida, en caso de que
	 * fuera una modificación de una geometría que estuviese en el fichero de
	 * expansión antes de ser modificada y se pone el puntero de escritura del
	 * expansion file a justo despues de la penultima geometría
	 *
	 * @param calculatedIndex
	 *            índice de la geometría que se quiere deshacer su modificación
	 * @param previousExpansionFileIndex
	 *            índice que tenía antes la geometría en el expansionFile. Si
	 *            vale -1 quiere decir que es una modificación de una geometría
	 *            original y por tanto no hay que actualizar el mapa de indices
	 *            sino eliminar su entrada.
	 * @throws IOException
	 * @throws DriverIOException
	 */
	public int undoModifyRow(int calculatedIndex,
			int previousExpansionFileIndex, int sourceType) throws EditionCommandException{
		try {
			// Llega el CalculatedIndex
			/*
			 * Si la acción de modificar se realizó sobre una geometría original
			 */
			if (previousExpansionFileIndex == -1) {

				// Se obtiene la geometría para actualizar el índice
				// IGeometry g = ((DefaultFeature)
				// getRow(calculatedIndex).getLinkedRow()).getGeometry();
				int inverse = getInversedIndex(calculatedIndex);
				DefaultFeature df;

				df = (DefaultFeature) getRow(inverse).getLinkedRow();

				boolean cancel = fireBeforeModifyRow(df, calculatedIndex,
						sourceType);
				if (cancel)
					return -1;
				IGeometry g = df.getGeometry();
				// IGeometry g = ova.getShape(calculatedIndex);
				Rectangle2D r = g.getBounds2D();

				// Se elimina de las relaciones y del fichero de expansión
				relations.remove(new Integer(calculatedIndex));
				expansionFile.deleteLastRow();
				if (sourceType == EditionEvent.GRAPHIC) {
					// Se actualizan los índices
					IGeometry gAnt = ova.getShape(calculatedIndex);
					/*
					 * IGeometry gAnt = ((DefaultFeature) getRow(calculatedIndex)
					 * .getLinkedRow()).getGeometry();
					 */
					Rectangle2D rAnt = gAnt.getBounds2D();
					this.fmapSpatialIndex.delete(r, new Integer(calculatedIndex));
					this.fmapSpatialIndex.insert(rAnt, new Integer(calculatedIndex));
				}
			} else {
				// Se obtiene la geometría para actualizar el índice
				IGeometry g = null;
				int inverse = getInversedIndex(calculatedIndex);
				DefaultFeature df = (DefaultFeature) getRow(inverse).getLinkedRow();
				boolean cancel = fireBeforeModifyRow(df, calculatedIndex,
						sourceType);
				if (cancel)
					return -1;
				if (sourceType == EditionEvent.GRAPHIC) {
					g = df.getGeometry();
					System.out.println("Actual: " + g.toString());

					Rectangle2D r = g.getBounds2D();
					this.fmapSpatialIndex.delete(r, new Integer(calculatedIndex));

					// Se recupera la geometría
					// expansionFile.validateRow(previousExpansionFileIndex);

					// Se actualizan los índices
					 g = ((IFeature)
					 (expansionFile.getRow(previousExpansionFileIndex).getLinkedRow())).getGeometry();
					// System.out.println("Anterior a la que volvemos : " +
					// g.toString());
//					g = ((DefaultFeature) getRow(inverse).getLinkedRow())
//					.getGeometry();
					r = g.getBounds2D();
					this.fmapSpatialIndex.insert(r, new Integer(calculatedIndex));
				}
				// Se actualiza la relación de índices
				// Integer integer = new Integer(geometryIndex);
				int numAnt=((Integer)relations.get(new Integer(calculatedIndex))).intValue();
				relations.put(new Integer(calculatedIndex), new Integer(
						previousExpansionFileIndex));
				return numAnt;

			}
		} catch (ReadDriverException e) {
			throw new EditionCommandException(writer.getName(),e);
		}
		//fireAfterModifyRow(calculatedIndex, sourceType);
		return -1;
	}

	/**
	 * Añade una geometria al fichero de expansión y guarda la correspondencia
	 * en la tabla relations.
	 *
	 * @param feat
	 *            geometría a guardar.
	 *
	 * @return calculatedIndex
	 * @throws ExpansionFileWriteException
	 * @throws DriverIOException
	 * @throws IOException
	 */
	public int doAddRow(IRow feat, int sourceType) throws ReadDriverException, ExpansionFileWriteException{
		int calculatedIndex = super.doAddRow(feat, sourceType);
		// Actualiza el índice espacial
		IGeometry g = ((IFeature) feat).getGeometry();
		Rectangle2D r = g.getBounds2D();
		fmapSpatialIndex.insert(r, new Integer(calculatedIndex));
		isFullExtentDirty = true;
		return calculatedIndex;
	}

	/**
	 * Se desmarca como invalidada en el fichero de expansion o como eliminada
	 * en el fichero original
	 *
	 * @param index
	 *            DOCUMENT ME!
	 * @throws IOException
	 * @throws DriverIOException
	 */
	public void undoRemoveRow(int index, int sourceType) throws EditionCommandException  {
		super.undoRemoveRow(index, sourceType);

		IGeometry g = null;
		try {
			g = ((IFeature) getRow(getInversedIndex(index)).getLinkedRow()).getGeometry();
		} catch (ReadDriverException e) {
			throw new EditionCommandException(writer.getName(),e);
		}

		Rectangle2D r = g.getBounds2D();
		this.fmapSpatialIndex.insert(r, new Integer(index));
	}

	/**
	 * Se elimina del final del fichero de expansión poniendo el puntero de
	 * escritura apuntando al final de la penúltima geometría. Deberá quitar la
	 * relación del mapa de relaciones
	 *
	 * @param fmapSpatialIndex
	 *            Índice de la geometría que se añadió
	 * @throws EditionCommandException
	 * @throws DriverIOException
	 * @throws IOException
	 */
	public void undoAddRow(int calculatedIndex, int sourceType) throws EditionCommandException{
		int inverse = getInversedIndex(calculatedIndex);
		IGeometry g;
		try {
			g = ((IFeature) getRow(inverse).getLinkedRow()).getGeometry();
			Rectangle2D r = g.getBounds2D();
			this.fmapSpatialIndex.delete(r, new Integer(calculatedIndex));

			super.undoAddRow(calculatedIndex, sourceType);
			setSelection(new FBitSet());
		} catch (ReadDriverException e) {
			throw new EditionCommandException(writer.getName(),e);
		}
	}

	/**
	 * Obtiene las geometrías que se encuentran en el rectángulo que se pasa
	 * como parámetro haciendo uso del índice espacial
	 *
	 * @param r
	 *            Rectángulo indicando la porción del espacio para el cual se
	 *            quiere saber los índices de las geometrías que se encuentran
	 *            dentro de él
	 *
	 * @return Array de índices para su uso con getGeometry, removeGeometry, ...
	 */
	/*
	 * public int[] getRowsIndexes_OL(Rectangle2D r) { Envelope e = new
	 * Envelope(r.getX(), r.getX() + r.getWidth(), r.getY(), r.getY() +
	 * r.getHeight()); List l = index.query(e); int[] indexes = new
	 * int[l.size()];
	 *
	 * for (int index = 0; index < l.size(); index++) { Integer i = (Integer)
	 * l.get(index); indexes[index] = getInversedIndex(i.intValue()); }
	 *
	 * return indexes; }
	 */
	/**
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getShapeCount()
	 */
	public int getShapeCount() throws ReadDriverException {
		return getRowCount();
	}

	/**
	 * @throws ExpansionFileReadException
	 * @see com.iver.cit.gvsig.fmap.layers.ReadableVectorial#getFullExtent()
	 */
	public Rectangle2D getFullExtent() throws ReadDriverException, ExpansionFileReadException {
		if (fullExtent == null) {
			fullExtent = ova.getFullExtent();
		}

		if (isFullExtentDirty) {
			fullExtent = reCalculateFullExtent();
			isFullExtentDirty = false;
		}
		return fullExtent;
	}

	/**
	 * Use it BEFORE writing to a file.
	 *
	 * @return real full extent.
	 * @throws ExpansionFileReadException
	 * @throws ReadDriverException
	 * @throws DriverIOException
	 */
	public Rectangle2D reCalculateFullExtent() throws ReadDriverException, ExpansionFileReadException  {
		int i=0;
		int count=getShapeCount();
		while (i<count && getShape(i) == null){
			i++;
		}
		if (i < count){
			fullExtent = getShape(i).getBounds2D();
			for (int j = i; j < count; j++) {
				IGeometry geom=getShape(i);
				if (geom==null)
					continue;
				if (fullExtent==null) {
					fullExtent=geom.getBounds2D();
					continue;
				}
				if (!(geom instanceof FNullGeometry)) {
					fullExtent.add(geom.getBounds2D());
				}
			}
		} else {
			fullExtent = ova.getFullExtent();
		}
		return fullExtent;
	}

	/**
	 * En la implementación por defecto podemos hacer que cada feature tenga ID =
	 * numero de registro. En el DBAdapter podríamos "overrride" este método y
	 * poner como ID de la Feature el campo único escogido en la base de datos.
	 *
	 * @param numReg
	 * @return
	 * @throws ExpansionFileReadException
	 */
	public IFeature getFeature(int numReg) throws ReadDriverException, ExpansionFileReadException {
		IGeometry geom;
		IFeature feat = null;
			geom = getShape(numReg);
			DataSource rs = getRecordset();
			Value[] regAtt = new Value[rs.getFieldCount()];
			for (int fieldId = 0; fieldId < rs.getFieldCount(); fieldId++) {
				regAtt[fieldId] = rs.getFieldValue(numReg, fieldId);
			}
			feat = new DefaultFeature(geom, regAtt, numReg + "");
		return feat;
	}

	public void stopEdition(IWriter writer, int sourceType) throws StopWriterVisitorException {
//		ISpatialWriter spatialWriter = (ISpatialWriter) writer;
//		spatialWriter.setFlatness(FConverter.flatness);
		super.stopEdition(writer, sourceType);
//		try {
//			ova.getDriver().reload();
//		} catch (ReloadDriverException e) {
//			throw new StopWriterVisitorException(writer.getName(),e);
//		}
//		fmapSpatialIndex=null;
		writer=null;
	}

	public Rectangle2D getShapeBounds(int index) throws ReadDriverException, ExpansionFileReadException {
		// Solo se utiliza cuando el driver es BoundedShapes
		// Si no está en el fichero de expansión
		Integer integer = new Integer(index);
		if (!relations.containsKey(integer)) {
			if (ova.getDriver() instanceof BoundedShapes) {
				BoundedShapes bs = (BoundedShapes) ova.getDriver();
				return bs.getShapeBounds(index);
			}
			return ova.getDriver().getShape(index).getBounds2D();

		}
		int num = ((Integer) relations.get(integer)).intValue();
		DefaultRowEdited feat;
		feat = (DefaultRowEdited) expansionFile.getRow(num);
		if (feat.getStatus() == IRowEdited.STATUS_DELETED)
			return null;
		IGeometry geom = ((IFeature) feat.getLinkedRow()).getGeometry();
		return geom.getBounds2D();// getGeometry();

	}

	public int getShapeType(int index) throws ReadDriverException {
		return ova.getShapeType();
	}

	/**
	 * Usar solo cuando estés seguro de que puedes gastar memoria. Nosotros lo
	 * usamos para las búsquedas por índice espacial con el handle. La idea es
	 * usarlo una vez, guardar las geometrías que necesitas en ese extent y
	 * trabajar con ellas hasta el siguiente cambio de extent.
	 *
	 * @param r
	 * @param strEPSG
	 * @return
	 * @throws ExpansionFileReadException
	 * @throws ReadDriverException
	 * @throws DriverException
	 */
	public IRowEdited[] getFeatures(Rectangle2D r, String strEPSG) throws ReadDriverException, ExpansionFileReadException{
		// En esta clase suponemos random access.
		// Luego tendremos otra clase que sea VectorialEditableDBAdapter
		// que reescribirá este método.
//		Envelope e = FConverter.convertRectangle2DtoEnvelope(r);
		long t1 = System.currentTimeMillis();
		List l = fmapSpatialIndex.query(r);
		long t2 = System.currentTimeMillis();
		IRowEdited[] feats = new IRowEdited[l.size()];
		for (int index = 0; index < l.size(); index++) {
			Integer i = (Integer) l.get(index);
			int inverse = getInversedIndex(i.intValue());
			feats[index] = getRow(inverse);
		}
		long t3 = System.currentTimeMillis();
		System.out.println("VectorialEditableAdapter.getFeatures: tSpatialIndex = " + (t2-t1) + " tFor=" + (t3-t2));
		return feats;
	}

//	public void setSpatialIndex(SpatialIndex spatialIndex) {
//		index = (Quadtree) spatialIndex;
//		QuadtreeJts fmapidx = new QuadtreeJts(index);
//		setSpatialIndex(fmapidx);
//
//	}

	public void setFullExtent(Rectangle2D fullExtent2) {
		fullExtent = fullExtent2;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @return DOCUMENT ME!
	 */
	public Image getSelectionImage() {
		return selectionImage;
	}

	public Image getHandlersImage() {
		return handlersImage;
	}

	/**
	 * DOCUMENT ME!
	 *
	 * @param i
	 *            DOCUMENT ME!
	 */
	public void setSelectionImage(Image i) {
		selectionImage = i;
	}

	public void setHandlersImage(BufferedImage handlersImage) {
		this.handlersImage = handlersImage;
	}

	public IFeatureIterator getFeatureIterator() throws ReadDriverException {
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
									IProjection newProjection, boolean fastIteration) throws ReadDriverException{
//		if(index == null){
			return new SpatialQueryFeatureIterator(this, projection, newProjection, fields, rect, fastIteration);
//		}else{
//			return new IndexedSptQueryFeatureIterator(this, projection, newProjection, fields, rect, fmapSpatialIndex, fastIteration);
//		}
	}

	public ISpatialIndex getSpatialIndex() {
		return fmapSpatialIndex;
	}

	public void setSpatialIndex(ISpatialIndex spatialIndex) {
//		this.fmapSpatialIndex = spatialIndex;
	}

	public void setProjection(IProjection projection) {
		this.projection = projection;
	}

	public IProjection getProjection() {
		return projection;
	}

	public void cancelEdition(int sourceType) throws CancelEditingLayerException {
		super.cancelEdition(sourceType);
		fmapSpatialIndex=null;
		setWriter(null);
		System.gc();
	}

	/**
	 * Inserta las coordenadas de transformación.
	 *
	 * @param ct Coordenadas de transformación.
	 */
	public void setCoordTrans(ICoordTrans ct) {
		this.ct=ct;
	}

	public IFeatureIterator getFeatureIterator(String sql, IProjection newProjection, boolean withSelection) throws ReadDriverException {
		if (withSelection)
			return new AttrQuerySelectionFeatureIterator(this, projection, newProjection, sql);
		else
			return getFeatureIterator(sql,newProjection);
	}

}
