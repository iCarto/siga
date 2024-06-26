/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
*
* Copyright (C) 2005 IVER T.I. and Generalitat Valenciana.
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
*/
package org.gvsig.raster.util.process;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.gvsig.fmap.raster.grid.roi.VectorialROI;
import org.gvsig.fmap.raster.layers.FLyrRasterSE;
import org.gvsig.fmap.raster.layers.IRasterLayerActions;
import org.gvsig.raster.Configuration;
import org.gvsig.raster.RasterProcess;
import org.gvsig.raster.buffer.BufferFactory;
import org.gvsig.raster.buffer.BufferInterpolation;
import org.gvsig.raster.buffer.RasterBuffer;
import org.gvsig.raster.buffer.WriterBufferServer;
import org.gvsig.raster.dataset.GeoRasterWriter;
import org.gvsig.raster.dataset.IBuffer;
import org.gvsig.raster.dataset.IRasterDataSource;
import org.gvsig.raster.dataset.InvalidSetViewException;
import org.gvsig.raster.dataset.NotSupportedExtensionException;
import org.gvsig.raster.dataset.Params;
import org.gvsig.raster.dataset.RasterDataset;
import org.gvsig.raster.dataset.io.RasterDriverException;
import org.gvsig.raster.dataset.properties.DatasetColorInterpretation;
import org.gvsig.raster.dataset.serializer.RmfSerializerException;
import org.gvsig.raster.datastruct.ColorTable;
import org.gvsig.raster.datastruct.NoData;
import org.gvsig.raster.grid.GridPalette;
import org.gvsig.raster.grid.filter.RasterFilterList;
import org.gvsig.raster.grid.filter.bands.ColorTableFilter;
import org.gvsig.raster.util.ExternalCancellable;
import org.gvsig.raster.util.RasterNotLoadException;
import org.gvsig.raster.util.RasterToolsUtil;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.view.ProjectViewBase;
/**
 * <code>ClippingProcess</code> es un proceso que usa un <code>Thread</code>
 * para aplicar un recorte a una capa y guardarlo en disco. Muestra una barra
 * de incremento informativa.
 *
 * @version 24/04/2007
 * @author BorSanZa - Borja S�nchez Zamorano (borja.sanchez@iver.es)
 */
public class ClippingProcess extends RasterProcess {
	private String                        fileName            = "";
	private WriterBufferServer            writerBufferServer  = null;
	private FLyrRasterSE                  rasterSE            = null;
	private AffineTransform               affineTransform     = new AffineTransform();
	private boolean                       oneLayerPerBand     = false;
	private int[]                         drawableBands       = { 0, 1, 2 };
	private int[]                         pValues             = null;
	private GeoRasterWriter               grw                 = null;
	private int                           interpolationMethod = BufferInterpolation.INTERPOLATION_Undefined;
	private String                        viewName            = "";
	private Params                        params              = null;
	private DatasetColorInterpretation    colorInterp         = null;
	private ArrayList                     selectedRois        = null;

	private double[]                      wcValues             = null;
	
	/**
	 * Variables de la resoluci�n de salida
	 */
	private int                           resolutionWidth     = 0;
	private int                           resolutionHeight    = 0;
	
	private IBuffer                       buffer              = null;
	private boolean                       showEndDialog       = true;
	private ExternalCancellable           cancellableObj      = null;

	/**
	 * Par�metros obligatorios al proceso:
	 * <UL>
	 * <LI>filename: Nombre del fichero de salida</LI>
	 * <LI>datawriter: Escritor de datos</LI>
	 * <LI>viewname: Nombre de la vista sobre la que se carga la capa al acabar el proceso</LI>
	 * <LI>pixelcoordinates: Coordenadas pixel del recorte (ulx, uly, lrx, lry)</LI>
	 * <LI>layer: Capa de entrada para el recorte</LI>
	 * <LI>drawablebands: Bandas de entrada</LI>
	 * <LI>onelayerperband: booleano que informa de si escribimos una banda por fichero de salida o todas en un fichero</LI>
	 * <LI>interpolationmethod: M�todo de interpolaci�n.</LI>
	 * <LI>affinetransform: Transformaci�n que informa al dataset de salida de su referencia geografica</LI>
	 * <LI>resolution: Ancho y alto de la capa de salida</LI>
	 * </UL> 
	 */
	public void init() {
		fileName = getStringParam("filename");
		writerBufferServer = (WriterBufferServer) getParam("datawriter");
		viewName = getStringParam("viewname");
		pValues = getIntArrayParam("pixelcoordinates");
		wcValues = getDoubleArrayParam("realcoordinates");
		rasterSE = getLayerParam("layer");
		drawableBands = getIntArrayParam("drawablebands");
		oneLayerPerBand = getBooleanParam("onelayerperband");
		interpolationMethod = getIntParam("interpolationmethod");
		affineTransform = (AffineTransform)getParam("affinetransform");
		colorInterp = (DatasetColorInterpretation)getParam("colorInterpretation");
		selectedRois = (ArrayList)getParam("selectedrois");
		if(getIntArrayParam("resolution") != null) {
			resolutionWidth = getIntArrayParam("resolution")[0];
			resolutionHeight = getIntArrayParam("resolution")[1];
		}
		params = (Params) getParam("driverparams");
		showEndDialog = getBooleanParam("showenddialog");
		Object obj = taskParams.get("cancellable");
		if(obj != null)
			cancellableObj = (ExternalCancellable)obj;
	}

	/**
	 * Salva la tabla de color al fichero rmf.
	 * @param fName
	 * @throws IOException
	 */
	private void saveToRmf(String fileName) {
		RasterDataset rds = null;
		int limitNumberOfRequests = 20;
		while (rds == null && limitNumberOfRequests > 0) {
			try {
				rds = rasterSE.getDataSource().getDataset(0)[0];
			} catch (IndexOutOfBoundsException e) {
				//En ocasiones, sobre todo con servicios remotos al pedir un datasource da una excepci�n de este tipo
				//se supone que es porque hay un refresco en el mismo momento de la petici�n por lo que como es m�s lento de
				//gestionar pilla a la capa sin datasources asociados ya que est� reasignandolo. Si volvemos a pedirlo debe
				//haberlo cargado ya.
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
				}
			}
			limitNumberOfRequests--;
		}
		
		if (rds == null) {
			//RasterToolsUtil.messageBoxError("error_load_layer", this, new Exception("Error writing RMF. limitNumberOfRequests=" + limitNumberOfRequests));
			return;
		}

		RasterFilterList rasterFilterList = rasterSE.getRenderFilterList();

		// Guardamos en el RMF el valor NoData
		if (Configuration.getValue("nodata_transparency_enabled", Boolean.FALSE).booleanValue()) {
			try {
				RasterDataset.saveObjectToRmfFile(fileName, NoData.class, new NoData(rasterSE.getNoDataValue(), rasterSE.getNoDataType(), rasterSE.getDataType()[0]));
			} catch (RmfSerializerException e) {
				RasterToolsUtil.messageBoxError("error_salvando_rmf", this, e);
			}
		}

		// Guardamos en el RMF la tabla de color
		ColorTableFilter colorTableFilter = (ColorTableFilter) rasterFilterList.getByName(ColorTableFilter.names[0]);
		if (colorTableFilter != null) {
			GridPalette gridPalette = new GridPalette((ColorTable) colorTableFilter.getColorTable().clone());
			try {
				RasterDataset.saveObjectToRmfFile(fileName, ColorTable.class, (ColorTable) gridPalette);
			} catch (RmfSerializerException e) {
				RasterToolsUtil.messageBoxError("error_salvando_rmf", this, e);
			}
		}
	}

	/**
	 * Tarea de recorte
	 */
	public void process() throws InterruptedException {
		IRasterDataSource dsetCopy = null;
		if(rasterSE != null)
			rasterSE.setReadingData(Thread.currentThread().toString());
		
		try {
			long t2;
			long t1 = new java.util.Date().getTime();

			insertLineLog(RasterToolsUtil.getText(this, "leyendo_raster"));

			dsetCopy = rasterSE.getDataSource().newDataset();
			BufferFactory bufferFactory = new BufferFactory(dsetCopy);
			bufferFactory.setDrawableBands(drawableBands);
	
			if(interpolationMethod != BufferInterpolation.INTERPOLATION_Undefined) {
				try {
					if(pValues != null) {
						if (RasterBuffer.isBufferTooBig(new double[] { pValues[0], pValues[3], pValues[2], pValues[1] }, drawableBands.length))
							bufferFactory.setReadOnly(true);
						bufferFactory.setAreaOfInterest(pValues[0], pValues[3], pValues[2] - pValues[0], pValues[1] - pValues[3]);
					} else if(wcValues != null) {
						//if (RasterBuffer.isBufferTooBig(new double[] { wcValues[0], wcValues[3], wcValues[2], wcValues[1] }, rasterSE.getCellSize(), drawableBands.length))
						if(!rasterSE.isActionEnabled(IRasterLayerActions.REMOTE_ACTIONS))
							bufferFactory.setReadOnly(true);
						bufferFactory.setAreaOfInterest(wcValues[0], wcValues[1], Math.abs(wcValues[0] - wcValues[2]), Math.abs(wcValues[1] - wcValues[3]));
					}
				} catch (InvalidSetViewException e) {
					RasterToolsUtil.messageBoxError("No se ha podido asignar la vista al inicial el proceso de recorte.", this, e);
				}
				buffer = bufferFactory.getRasterBuf();

				insertLineLog(RasterToolsUtil.getText(this, "interpolando"));
				
				if(buffer != null)
					buffer = ((RasterBuffer) buffer).getAdjustedWindow(resolutionWidth, resolutionHeight, interpolationMethod);
				else {
					RasterToolsUtil.messageBoxError("El proceso de recorte ha fallado porque el buffer no conten�a datos.", this, new NullPointerException());
					return;
				}
			} else {
				try {
					if (RasterBuffer.isBufferTooBig(new double[] { 0, 0, resolutionWidth, resolutionHeight }, drawableBands.length))
						bufferFactory.setReadOnly(true);
					if(pValues != null) 
						bufferFactory.setAreaOfInterest(pValues[0], pValues[3], Math.abs(pValues[2] - pValues[0]) + 1, Math.abs(pValues[1] - pValues[3]) + 1, resolutionWidth, resolutionHeight);
					else if(wcValues != null) 
						bufferFactory.setAreaOfInterest(wcValues[0], wcValues[1], wcValues[2], wcValues[3], resolutionWidth, resolutionHeight);
					buffer = bufferFactory.getRasterBuf();
					if(buffer == null) {
						RasterToolsUtil.messageBoxError("El proceso de recorte ha fallado porque el buffer no conten�a datos.", this, new NullPointerException());
						return;
					}
				} catch (InvalidSetViewException e) {
					RasterToolsUtil.messageBoxError("No se ha podido asignar la vista al inicial el proceso de recorte.", this, e);
				}
			}
			//TODO: FUNCIONALIDAD: Poner los getWriter con la proyecci�n del fichero fuente
			
			
			if ((selectedRois != null) && (!bufferFactory.isReadOnly())) {
				if (selectedRois.size() > 0) {
					int despX = 0;
					int despY = 0;
					if (pValues != null) {
						despX = pValues[0];
						despY = pValues[1];
					} else if (wcValues != null) {
						despX = (int)dsetCopy.worldToRaster(new Point2D.Double(wcValues[0], wcValues[1])).getX();
						despY = (int)dsetCopy.worldToRaster(new Point2D.Double(wcValues[0], wcValues[1])).getY();
					}
					drawOnlyROIs(buffer, selectedRois, despX, despY);
				}
			}
			
			insertLineLog(RasterToolsUtil.getText(this, "salvando_imagen"));

			String finalFileName = "";
			if (oneLayerPerBand) {
				long[] milis = new long[drawableBands.length];
				String[] fileNames = new String[drawableBands.length];
				for (int i = 0; i < drawableBands.length; i++) {
					fileNames[i] = fileName + "_B" + drawableBands[i] + ".tif";
					writerBufferServer.setBuffer(buffer, i);
					Params p = null;
					if (params == null)
						p = GeoRasterWriter.getWriter(fileNames[i]).getParams();
					else
						p = params;
					grw = GeoRasterWriter.getWriter(writerBufferServer, fileNames[i], 1,
							affineTransform, buffer.getWidth(), buffer.getHeight(),
							buffer.getDataType(), p, null);
					grw.setColorBandsInterpretation(new String[]{DatasetColorInterpretation.GRAY_BAND});
					grw.setWkt(dsetCopy.getWktProjection());
					grw.setCancellableRasterDriver(cancellableObj);
					grw.dataWrite();
					grw.writeClose();
					saveToRmf(fileNames[i]);
					t2 = new java.util.Date().getTime();
					milis[i] = (t2 - t1);
					t1 = new java.util.Date().getTime();
				}
				if (incrementableTask != null) {
					incrementableTask.processFinalize();
					incrementableTask = null;
				}
				if(viewName != null) {
					if (RasterToolsUtil.messageBoxYesOrNot("cargar_toc", this)) {
						try {
							for (int i = 0; i < drawableBands.length; i++) {
								FLayer lyr = RasterToolsUtil.loadLayer(viewName, fileNames[i], null);
								if(lyr != null && lyr instanceof FLyrRasterSE)
									((FLyrRasterSE)lyr).setRois(rasterSE.getRois());
							}
						} catch (RasterNotLoadException e) {
							RasterToolsUtil.messageBoxError("error_load_layer", this, e);
						}
					}
				}
				for (int i = 0; i < drawableBands.length; i++) {
					if (externalActions != null)
						externalActions.end(new Object[] { fileName + "_B" + drawableBands[i] + ".tif", new Long(milis[i]) });
				}
			} else {
				if (isUsingFile(fileName)){
					incrementableTask.hideWindow();
					RasterToolsUtil.messageBoxError("error_opened_file", this);
					return;
				}
				File f = new File(fileName);
				if (f.exists()){
					f.delete();
				}
				f = null;
				writerBufferServer.setBuffer(buffer, -1);
				if (params == null) {
					finalFileName = fileName + ".tif";
					params = GeoRasterWriter.getWriter(finalFileName).getParams();
				} else
					finalFileName = fileName;
				grw = GeoRasterWriter.getWriter(writerBufferServer, finalFileName,
						buffer.getBandCount(), affineTransform, buffer.getWidth(),
						buffer.getHeight(), buffer.getDataType(), params, null);
				if(colorInterp != null)
					grw.setColorBandsInterpretation(colorInterp.getValues());
				grw.setWkt(dsetCopy.getWktProjection());
				grw.setCancellableRasterDriver(cancellableObj);
				grw.dataWrite();
				grw.writeClose();
				saveToRmf(finalFileName);
				t2 = new java.util.Date().getTime();
				if (incrementableTask != null) {
					incrementableTask.processFinalize();
					incrementableTask = null;
				}
				//Damos tiempo a parar el Thread del incrementable para que no se cuelgue la ventana
				//El tiempo es como m�nimo el de un bucle del run de la tarea incrementable
				Thread.sleep(600);
				cutFinalize(finalFileName, (t2 - t1));
			}

		} catch (NotSupportedExtensionException e) {
			RasterToolsUtil.messageBoxError("error_not_suported_extension", this, e);
		} catch (RasterDriverException e) {
			RasterToolsUtil.messageBoxError("error_writer", this, e);
		} catch (IOException e) {
			RasterToolsUtil.messageBoxError("error_georasterwriter", this, e);
		} finally {
			if(rasterSE != null)
				rasterSE.setReadingData(null);
			if (dsetCopy != null)
				dsetCopy.close();
			buffer = null;
		}
	}
	
	/**
	 * Acciones para poner a NoData los pixels que est�n fuera de las
	 * regiones de inter�s seleccionadas.
	 * @param buffer
	 */
	private void drawOnlyROIs(IBuffer buffer, ArrayList rois, int despX, int despY){
		for (int i = 0 ; i<buffer.getWidth() ; i++){
			for (int j = 0 ; j<buffer.getHeight() ; j++){
				boolean  inside = false;
				for (int k = 0 ; k<rois.size() ; k++){
					VectorialROI roi = (VectorialROI)rois.get(k);
					//TODO: Hacer la comprobacion por coordenadas del mundo en lugar de coordenadas pixel.
					if (roi.isInGrid(i + despX, j + despY)){
						inside = true;
					}
				}
				if (!inside){
					for (int l = 0 ; l<buffer.getBandCount() ; l++){
						try{
							if (buffer.getDataType() == RasterBuffer.TYPE_BYTE){
								buffer.setElem(j, i, l, buffer.getByteNoDataValue());
							} else if (buffer.getDataType() == RasterBuffer.TYPE_DOUBLE){
								buffer.setElem(j, i, l, buffer.getFloatNoDataValue());
							} else if (buffer.getDataType() == RasterBuffer.TYPE_FLOAT){
								buffer.setElem(j, i, l, buffer.getFloatNoDataValue());
							} else if (buffer.getDataType() == RasterBuffer.TYPE_INT){
								buffer.setElem(j, i, l, buffer.getIntNoDataValue());
							} else if (buffer.getDataType() == RasterBuffer.TYPE_SHORT){
								buffer.setElem(j, i, l, buffer.getShortNoDataValue());
							}
						}catch (InterruptedException ex){

						}
					}
				}
			}
		}
	}
	
	/**
	 * Returns true if there is a layer in the current project using the file named with
	 * fileName.
	 * @param fileName
	 * @return
	 */
	private boolean isUsingFile(String fileName){
		ProjectExtension ext = (ProjectExtension)PluginServices.getExtension(ProjectExtension.class);
		ArrayList<ProjectDocument> docs = ext.getProject().getDocuments();
		
		for (int i = 0 ; i<docs.size() ; i++){
			if (docs.get(i) instanceof ProjectViewBase){
				FLayers lyrs = ((ProjectViewBase)docs.get(i)).getMapContext().getLayers();
				for (int j = 0 ; j<lyrs.getLayersCount() ; j++){
					if (lyrs.getLayer(j) instanceof FLyrRasterSE){
						FLyrRasterSE lyr = (FLyrRasterSE)lyrs.getLayer(j);
						if (lyr.getDataSource() != null ){							
							if (lyr.getDataSource().getDataset(0)[0] != null ){
								if (lyr.getDataSource().getDataset(0)[0].getFName().equals(fileName)){
									return true;
								}
							}
						}					
					}
				}
			}
		}
		return false;
	}


	/**
	 * Acciones que se realizan al finalizar de crear los recortes de imagen.
	 * Este m�todo es llamado por el thread TailRasterProcess al finalizar.
	 */
	private void cutFinalize(String fileName, long milis) {
		if (!new File(fileName).exists())
			return;

		if(viewName != null) {
			if (RasterToolsUtil.messageBoxYesOrNot("cargar_toc", this)) {

				try {
					FLayer lyr = RasterToolsUtil.loadLayer(viewName, fileName, null);
					if(lyr != null && lyr instanceof FLyrRasterSE)
						((FLyrRasterSE)lyr).setRois(rasterSE.getRois());
				} catch (RasterNotLoadException e) {
					RasterToolsUtil.messageBoxError("error_load_layer", this, e);
				}
			}
		}

		if (showEndDialog && externalActions != null)
			externalActions.end(new Object[]{fileName, new Long(milis)});
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gui.beans.incrementabletask.IIncrementable#getPercent()
	 */
	public int getPercent() {
		if (buffer != null) {
			BufferInterpolation interpolation = ((RasterBuffer) buffer).getLastInterpolation();
			
			if (interpolation != null)
				if ((interpolation.getPercent() > 0) && (interpolation.getPercent() < 99))
					return interpolation.getPercent();
		}
		
		return (writerBufferServer != null) ? writerBufferServer.getPercent() : 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gui.beans.incrementabletask.IIncrementable#getTitle()
	 */
	public String getTitle() {
		return RasterToolsUtil.getText(this, "incremento_recorte");
	}
}