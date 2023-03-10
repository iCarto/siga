/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
*
* Copyright (C) 2007 IVER T.I. and Generalitat Valenciana.
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
package org.gvsig.rastertools.saveraster.operations;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.geom.AffineTransform;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import javax.swing.JOptionPane;

import org.gvsig.fmap.raster.layers.FLyrRasterSE;
import org.gvsig.raster.RasterLibrary;
import org.gvsig.raster.RasterProcess;
import org.gvsig.raster.dataset.GeoRasterWriter;
import org.gvsig.raster.dataset.IBuffer;
import org.gvsig.raster.dataset.IDataWriter;
import org.gvsig.raster.dataset.NotSupportedExtensionException;
import org.gvsig.raster.dataset.Params;
import org.gvsig.raster.dataset.io.RasterDriverException;
import org.gvsig.raster.datastruct.Extent;
import org.gvsig.raster.datastruct.ViewPortData;
import org.gvsig.raster.process.RasterTask;
import org.gvsig.raster.process.RasterTaskQueue;
import org.gvsig.raster.util.ExternalCancellable;
import org.gvsig.raster.util.RasterToolsUtil;
import org.gvsig.raster.util.RasterUtilities;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.fmap.ViewPort;
/**
 * Thread que se encarga de llamar a los writer para realizar la tarea de
 * salvado y/p compresión
 *
 * @author Nacho Brodin (nachobrodin@gmail.com)
 */
public class SaveRasterProcess extends RasterProcess {
	private ViewPort          viewPort          = null;
	private Dimension         dimension         = null;
	private RasterizerLayer   rasterizerLayer   = null;
	private String            fileName          = "";
	private Params            writerParams      = null;
	private CopyDataset       jp2Copy           = null;
	private boolean           supportImage      = false;
	private ArrayList<FLyrRasterSE>
	                          layers            = null;
	
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.rastertools.RasterProcess#init()
	 */
	@SuppressWarnings("unchecked")
	public void init() {
		viewPort = (ViewPort) getParam("viewport");
		dimension = (Dimension) getParam("dimension");
		rasterizerLayer = (RasterizerLayer) getParam("rasterizerlayer");
		fileName = getStringParam("filename");
		writerParams = (Params) getParam("writerparams");
		supportImage = getBooleanParam("remotelayers") && fileName.endsWith(".jp2");
		layers = (ArrayList<FLyrRasterSE>) getParam("layers");
	}

	
	/**
	 * Procesos de escritura de una porción de la vista.
	 */
	public void process() throws InterruptedException {
		RasterTask task = RasterTaskQueue.get(Thread.currentThread().toString());
		if(layers != null) {
			for (int i = 0; i < layers.size(); i++) 
				layers.get(i).setReadingData(Thread.currentThread().toString());
		}
		
		jp2Copy = null;
		long t2;
		long t1 = new java.util.Date().getTime();

		//Creamos el driver
		Extent ex = new Extent(viewPort.getAdjustedExtent());
		Dimension imgSz = viewPort.getImageSize();
		ViewPortData vpData = new ViewPortData(viewPort.getProjection(), ex, imgSz );
		AffineTransform at = new AffineTransform(vpData.getExtent().width() / imgSz.width,
												 0, 0,
												 -(vpData.getExtent().height() / imgSz.height),
												 vpData.getExtent().getULX(),
												 vpData.getExtent().getULY());

		String oldFileName = fileName;
		if(supportImage) {
			fileName = fileName.substring(0, Math.min(fileName.lastIndexOf(File.separator) + 1, fileName.length() - 1));
			fileName += RasterLibrary.usesOnlyLayerName() + ".tif";
			writerParams = getWriterParams(fileName);
		}
		
		//Ejecutamos el driver con los datos pasados
		try {
			write(fileName, at, writerParams, rasterizerLayer);

			if(task.getEvent() != null)
				task.manageEvent(task.getEvent());
			
			if(supportImage) {
				try {
					insertLineLog(RasterToolsUtil.getText(this, "saving_jp2"));
					jp2Copy = new CopyDataset(fileName, oldFileName, incrementableTask);
					jp2Copy.copy();
					new File(fileName).delete();
					new File(RasterUtilities.getRMFNameFromFileName(fileName)).delete();
				} catch (LoadLayerException e) {
				}
				jp2Copy = null;
			} 
			t2 = new Date().getTime();
			try {
				saveRasterFinalize(oldFileName, (t2 - t1));
			} catch(ArrayIndexOutOfBoundsException exc) {
				//Si la ventana se ha cerrado ya es porque ha sido cancelada por lo que
				//producirá esta excepción. En este caso no se lanza la ventana de información
				//de finalización.
			}

		} catch(IOException ev) {
			if (incrementableTask != null) {
				incrementableTask.processFinalize();
				incrementableTask = null;
			}
			RasterToolsUtil.messageBoxError("error_processing", this, ev);
			return;
		} catch(OutOfMemoryError ev) {
			if (incrementableTask != null) {
				incrementableTask.processFinalize();
				incrementableTask = null;
			}
			RasterToolsUtil.messageBoxError("memoria_excedida", this, new IOException("Out of memory en salvar a raster"));
		} finally {
			if(layers != null) {
				for (int i = 0; i < layers.size(); i++) 
					layers.get(i).setReadingData(null);
			}
		}
	}

	/**
	 * 
	 * @param name
	 * @param at
	 * @throws IOException
	 * @throws InterruptedException
	 */
	private void write(String name, AffineTransform at, Params writerParams, IDataWriter rasterizerLayer) throws IOException, InterruptedException {
		RasterTask task = RasterTaskQueue.get(Thread.currentThread().toString());
		GeoRasterWriter geoRasterWriter = null;
		try {
			//TODO: FUNCIONALIDAD: Poner los gerWriter con la proyección de la vista
			geoRasterWriter = GeoRasterWriter.getWriter(rasterizerLayer, name,
												3, at, dimension.width,
												dimension.height, IBuffer.TYPE_IMAGE, writerParams, null);
		} catch (NotSupportedExtensionException e) {
			RasterToolsUtil.messageBoxError("extension_no_soportada", (Component)PluginServices.getMainFrame(), e);
		} catch (RasterDriverException e) {
			RasterToolsUtil.messageBoxError("no_driver_escritura", (Component)PluginServices.getMainFrame(), e);
		}
		
		if(task.getEvent() != null)
			task.manageEvent(task.getEvent());
		
		geoRasterWriter.setCancellableRasterDriver(new ExternalCancellable(incrementableTask));
		geoRasterWriter.dataWrite();
		geoRasterWriter.writeClose();
	}
	
	/**
	 * Obtiene los parámetros del driver de escritura. Si el driver no se ha creado aún se obtienen
	 * unos parámetros con la inicialización por defecto. Si se ha creado ya y se han modificado se
	 * devuelven los parámetros con las modificaciones. Si se cambia de driver se devolverá un WriterParams
	 * como si fuera la primera vez que se abre.
	 * @param name Nombre del fichero sobre el que se salva.
	 * @return WriterParams
	 */
	private Params getWriterParams(String name) {
		GeoRasterWriter writer = null;
		String ext = RasterUtilities.getExtensionFromFileName(name);
		try {
			if(writer == null) //La primera vez que se obtiene el driver
				writer = GeoRasterWriter.getWriter(name);
			else {
				String newType = GeoRasterWriter.getDriverType(ext);
				String oldType = writer.getDriverName();
				if(!newType.equals(oldType))  //Cambio de driver después de haber seleccionado y modificado las propiedades de uno
					writer = GeoRasterWriter.getWriter(name);
			}

			if(writer == null)
				JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(), PluginServices.getText(this, "no_driver_escritura"));

			return writer.getParams();

		} catch (NotSupportedExtensionException e1) {
			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(), PluginServices.getText(this, "no_driver_escritura"));
			return null;
		} catch (RasterDriverException e1) {
			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(), PluginServices.getText(this, "no_driver_escritura"));
			return null;
		}
	}
	
	/**
	 * Acciones que se realizan al finalizar de salvar a raster.
	 * @param fileName Nombre del fichero
	 * @param milis Tiempo que ha tardado en ejecutarse
	 */
	private void saveRasterFinalize(String fileName, long milis) {
		if (incrementableTask != null)
			incrementableTask.hideWindow();
		externalActions.end(new Object[]{fileName, new Long(milis)});
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gui.beans.incrementabletask.IIncrementable#getPercent()
	 */
	public int getPercent() {
		if(jp2Copy != null)
			return jp2Copy.getPercent();
		else if(rasterizerLayer != null)
			return rasterizerLayer.getPercent();
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gui.beans.incrementabletask.IIncrementable#getTitle()
	 */
	public String getTitle() {
		if(rasterizerLayer != null)
			return rasterizerLayer.getTitle();
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.rastertools.RasterProcess#getLog()
	 */
	public String getLog() {
		if(rasterizerLayer != null)
			return rasterizerLayer.getLog();
		return null;
	}
	
	/*
	 * (non-Javadoc)
	 * @see org.gvsig.gui.beans.incrementabletask.IIncrementable#getLabel()
	 */
	public String getLabel() {
		if(rasterizerLayer != null)
			return rasterizerLayer.getLabel();
		return null;
	}
}