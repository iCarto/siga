/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.addlayer.fileopen.vectorial;

import java.awt.geom.Rectangle2D;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.exolab.castor.xml.MarshalException;
import org.exolab.castor.xml.ValidationException;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.AddLayer;
import com.iver.cit.gvsig.addlayer.fileopen.AbstractFileOpen;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.drivers.gvl.FMapGVLDriver;
import com.iver.cit.gvsig.fmap.drivers.legend.LegendDriverException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelingFactory;
import com.iver.utiles.XMLEntity;
/**
 * Clase que indicará que ficheros puede tratar al panel de apertura de ficheros
 *
 * @version 04/09/2007
 * @author BorSanZa - Borja Sánchez Zamorano (borja.sanchez@iver.es)
 */
public class VectorialFileOpen extends AbstractFileOpen{
    
    
    private static final Logger logger = Logger.getLogger(VectorialFileOpen.class);

	/**
	 * Constructor de FileOpenRaster
	 */
	public VectorialFileOpen() {
		TreeSet filters = new TreeSet(new Comparator() {
			public int compare(Object o1, Object o2) {
				VectorialFileFilter dff1 = (VectorialFileFilter) o1;
				VectorialFileFilter dff2 = (VectorialFileFilter) o2;

				return dff1.driver.getName().compareTo(dff2.driver.getName());
			}
		});

		Class[] driverClasses = new Class[] { VectorialFileDriver.class };
		String[] driverNames = LayerFactory.getDM().getDriverNames();
		VectorialFileFilter auxF;
		try {
			for (int i = 0; i < driverNames.length; i++) {
				System.err.println("DRIVER " + i + " : " + driverNames[i]);
				boolean is = false;
				for (int j = 0; j < driverClasses.length; j++) {
					if (i == 0)
						System.err.println("DRIVER CLASS " + j + " : " + driverClasses[j].toString());
					if (LayerFactory.getDM().isA(driverNames[i], driverClasses[j]))
						is = true;
				}
				if (is) {
					auxF = new VectorialFileFilter(driverNames[i]);
					filters.add(auxF);
				}
			}
			Iterator i = filters.iterator();
			while (i.hasNext()) {
				VectorialFileFilter element = (VectorialFileFilter) i.next();
				getFileFilter().add(element);
			}
		} catch (DriverLoadException e1) {
			NotificationManager.addError("No se pudo acceder a los drivers", e1);
		}

	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.raster.gui.wizards.IFileOpen#execute(java.io.File[])
	 */
	public Rectangle2D createLayer(File file, MapControl mapControl, String driverName, IProjection proj) {
		FLayer lyr = null;
		Driver driver = null;

		// all catched errors will be saved here, to show user at the end of the method
		ArrayList errors = new ArrayList();

		// try to load the drivers referenced by the file dialog
		try {
			driver = LayerFactory.getDM().getDriver(driverName);
		} catch (DriverLoadException e) {
			errors.add(e);
		}

		// Envelope de cada fichero seleccionado por el usuario
		String layerName = file.getName();

		// Show file extension when adding layers on ToC
		XMLEntity xml = PluginServices.getPluginServices("com.iver.cit.gvsig").getPersistentXML();
		
		boolean showFileExtension;
		
		if (!xml.contains("ShowFileExtensions")) {
			//not show by def
			showFileExtension = false;
		} else {
			showFileExtension = xml.getBooleanProperty("ShowFileExtensions");
		}
		
		int dot_index = layerName.lastIndexOf(".");
		if (!showFileExtension && dot_index > 0) {
			layerName = layerName.substring(0, dot_index);
		}

		
		try {

			if (driver instanceof VectorialFileDriver) {
				lyr = LayerFactory.createLayer(layerName, (VectorialFileDriver) driver, file, proj);
			}

			if (lyr != null) {
				AddLayer.checkProjection(lyr, mapControl.getViewPort());
				mapControl.getMapContext().getLayers().addLayer(lyr);

				String path = file.getAbsolutePath();
				// last index + 1 because FmapGVLDriver.FILE_EXTENSION
				// does not contain the dot
				FMapGVLDriver legendDriver = new FMapGVLDriver();
				File legendFile = new File(path.substring(0,
						path.lastIndexOf('.') + 1)
						+ legendDriver.getFileExtension());

				if (legendFile.exists() && lyr instanceof FLyrVect) {
					FLyrVect lyrVect = (FLyrVect) lyr;
					Hashtable<FLayer, IVectorLegend> map = legendDriver.read(
							mapControl
							.getMapContext().getLayers(), lyr, legendFile);
					if (map.containsKey(lyr)) {
						lyrVect.setLegend(map.get(lyr));
					}
				}
				
				setLabel(path, lyr);
				return lyr.getFullExtent();
			}
		} catch (ReadDriverException e) {
			errors.add(e);
		} catch (LegendDriverException e) {
			errors.add(e);
		} catch (LegendLayerException e) {
			errors.add(e);
		}
		return null;
	}
	
	private void setLabel(String path, FLayer lyr) {
	    File legendFile = new File(path.substring(0, path.lastIndexOf('.') + 1) + "gvlabel");

        if (legendFile.exists() && lyr instanceof FLyrVect) {
            FLyrVect lyrVect = (FLyrVect) lyr;
            String label = readFile(legendFile);
            setLabel(label, lyrVect);
        }
    }

    private void setLabel(String label, FLyrVect layer) {
	    if ((label != null) && (label.length() > 0)) {
	        try {
	        XMLEntity parse = XMLEntity.parse(label);
	        ILabelingStrategy strategy = LabelingFactory
	            .createStrategyFromXML(parse, layer);
	        layer.setLabelingStrategy(strategy);
	        layer.setIsLabeled(true);

	        } catch (MarshalException e) {
	        logger.error(e.getStackTrace(), e);
	        } catch (ValidationException e) {
	        logger.error(e.getStackTrace(), e);
	        } catch (ReadDriverException e) {
	        logger.error(e.getStackTrace(), e);
	        }
	    }
	 }
    
    private String readFile(File file) {
        BufferedReader br = null;
        String fileAsString = null;
        try {
            br = new BufferedReader(new FileReader(file));
            StringBuilder sb = new StringBuilder();

            String line = br.readLine();
            while (line != null) {
              sb.append(line).append("\n");
              line = br.readLine();
            }

            fileAsString = sb.toString();
        } catch (Exception e) {
            logger.error(e.getStackTrace(), e);
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e1) {
                    logger.error(e1.getStackTrace(), e1);
                }
            }
        }
        
        return fileAsString;
    }
}