/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
package com.iver.cit.gvsig.fmap.layers;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.AffineTransform;
import java.awt.geom.NoninvertibleTransformException;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.cresques.cts.IProjection;
import org.cresques.geo.ViewPortData;
import org.cresques.px.Extent;
import org.exolab.castor.xml.ValidationException;
import org.gvsig.fmap.raster.layers.FLyrRasterSE;
import org.gvsig.fmap.raster.layers.IRasterLayerActions;
import org.gvsig.fmap.raster.layers.IStatusRaster;
import org.gvsig.fmap.raster.layers.StatusLayerRaster;
import org.gvsig.raster.dataset.CompositeDataset;
import org.gvsig.raster.dataset.IBuffer;
import org.gvsig.raster.dataset.MosaicNotValidException;
import org.gvsig.raster.dataset.MultiRasterDataset;
import org.gvsig.raster.dataset.NotSupportedExtensionException;
import org.gvsig.raster.dataset.io.RasterDriverException;
import org.gvsig.raster.grid.GridPalette;
import org.gvsig.raster.grid.GridTransparency;
import org.gvsig.raster.grid.filter.FilterTypeException;
import org.gvsig.raster.grid.filter.RasterFilterList;
import org.gvsig.raster.grid.filter.RasterFilterListManager;
import org.gvsig.raster.grid.filter.bands.ColorTableFilter;
import org.gvsig.raster.grid.filter.bands.ColorTableListManager;
import org.gvsig.remoteClient.utils.Utilities;
import org.gvsig.remoteClient.wms.ICancellable;
import org.gvsig.remoteClient.wms.WMSStatus;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.exceptions.layers.ConnectionErrorLayerException;
import com.iver.cit.gvsig.exceptions.layers.LoadLayerException;
import com.iver.cit.gvsig.exceptions.layers.URLLayerException;
import com.iver.cit.gvsig.exceptions.layers.UnsupportedVersionLayerException;
import com.iver.cit.gvsig.fmap.ConnectionErrorExceptionType;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.WMSDriverExceptionType;
import com.iver.cit.gvsig.fmap.crs.CRSFactory;
import com.iver.cit.gvsig.fmap.drivers.DriverIOException;
import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriver;
import com.iver.cit.gvsig.fmap.drivers.wms.FMapWMSDriverFactory;
import com.iver.cit.gvsig.fmap.drivers.wms.WMSException;
import com.iver.cit.gvsig.fmap.layers.WMSLayerNode.FMapWMSStyle;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ComposedLayer;
import com.iver.cit.gvsig.fmap.layers.layerOperations.IHasImageLegend;
import com.iver.cit.gvsig.fmap.layers.layerOperations.StringXMLItem;
import com.iver.cit.gvsig.fmap.layers.layerOperations.XMLItem;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.XmlBuilder;
import com.iver.cit.gvsig.wmc.WebMapContextTags;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;



/**
* FMap's WMS Layer class.
*
* Las capas WMS son tileadas para descargarlas del servidor. Esto quiere decir que
* están formadas por multiples ficheros raster. Por esto la fuente de datos raster (IRasterDatasource)
* de la capa FLyrWMS es un objeto de tipo CompositeDataset. Este objeto está compuesto por un array
* bidimensional de MultiRasterDataset. Cada uno de los MultiRasterDataset corresponde con un tile
* salvado en disco. Estos MultiRasterDataset se crean cada vez que se repinta ya que en WMS a cada
* zoom varian los ficheros fuente. La secuencia de creación de un CompositeDataset sería la siguiente:
* <UL>
* <LI>Se hace una petición de dibujado por parte del usuario llamando al método draw de FLyrWMS</LI>
* <LI>Se tilea la petición</LI>
* <LI>Cada tile se dibuja abriendo una FLyerRaster para ese tile</LI>
* <LI>Si es el primer dibujado se guarda una referencia en la capa WMS a las propiedades de renderizado, orden de bandas,
* transparencia, filtros aplicados, ...</LI>
* <LI>Si no es el primer dibujado se asignan las propiedades de renderizado cuya referencia se guarda en la capa WMS</LI>
* <LI>Se guarda el MultiRasterDataset de cada tile</LI>
* <LI>Al acabar todos los tiles creamos un CompositeDataset con los MultiRasterDataset de todos los tiles</LI>
* <LI>Asignamos a la capa la referencia de las propiedades de renderizado que tenemos almacenadas. De esta forma si hay
* alguna modificación desde el cuadro de propiedades será efectiva sobre los tiles que se dibujan.</LI>
* </UL>
*
*
* @author Jaume Dominguez Faus
*
*/
public class FLyrWMS extends FLyrRasterSE implements IHasImageLegend{
	private boolean 					isPrinting = false;
	private boolean 					mustTileDraw = true;
	private boolean 					mustTilePrint = true;
	private final int 					maxTileDrawWidth = 1023;
	private final int 					maxTileDrawHeight = 1023;
	private final int 					maxTilePrintWidth = 1999;
	private final int 					maxTilePrintHeight = 1999;
	private final int					minTilePrintWidth = 12;
	private final int					minTilePrintHeight = 12;

	public URL 							host;
	public String 						m_Format;

	private String 						m_SRS;
	private String 						layerQuery;
	private String 						infoLayerQuery;
	private FMapWMSDriver 				wms;
	private WMSStatus 					wmsStatus = new WMSStatus();
	private Rectangle2D 				fullExtent;
	private boolean						wmsTransparency;
	private Vector 						styles;
	private Vector 						dimensions;
	private boolean						firstLoad = false;
	private Hashtable 					onlineResources = new Hashtable();
	private Dimension 					fixedSize;
	private boolean 					queryable = true;
	private VisualStatus				visualStatus = new VisualStatus();
	/**
	 * Lista de filtros aplicada en la renderización
	 */
	private RasterFilterList            filterList = null;
	private GridTransparency			transparency = null;
	private int[]                       renderBands = null;
	private FLyrRasterSE[]				layerRaster = null;
	private ArrayList                   filterArguments = null;

	private List						disableUpdateDrawVersion;
	private int                         lastNColumns = 0;
	private int                         lastNRows = 0;
	private boolean 					hasImageLegend = false;
	
	private int cachedAxisOrientation = WMSStatus.CRS_AXIS_OTHER_OR_UNKNOWN;

	/***
	 * WMS 1.3 standard defines a fixed pixel size of 0.28 mm for the server.
	 * As
	 *   1 inch = 25.4 mm
	 * then the server is supposed to have the following DPI:
	 *   25.4 / 0.28 = 90.714285714 dpi
	 */

	private final double WMS_DPI = 90.714285714;
	private String infoFormat;
   

	private class MyCancellable implements ICancellable
	{
		private Cancellable original;
		public MyCancellable(Cancellable cancelOriginal)
		{
			this.original = cancelOriginal;
		}
		public boolean isCanceled() {
			if (original == null) return false;
			return original.isCanceled();
		}
		public Object getID() {
			return this;
		}

	}

	public FLyrWMS(){
		super();
		this.updateDrawVersion();
	}

	public FLyrWMS(Map args) throws LoadLayerException{
		this.updateDrawVersion();
		FMapWMSDriver drv = null;
		String host = (String)args.get("host");
		String sLayer = (String)args.get("layer");
		Rectangle2D fullExtent = (Rectangle2D)args.get("FullExtent");
		String sSRS = (String)args.get("SRS");
		String sFormat = (String)args.get("Format");
		String[] sLayers = sLayer.split(",");

		try {
			this.setHost(new URL(host));
		} catch (MalformedURLException e) {
			//e.printStackTrace();
			throw new URLLayerException(getName(),e);
		}
			try {
				drv = this.getDriver();
			} catch (IllegalStateException e) {
				throw new LoadLayerException(getName(),e);
			} catch (ValidationException e) {
				throw new LoadLayerException(getName(),e);
			} catch (IOException e) {
				throw new ConnectionErrorLayerException(getName(),e);
			}
		if( sFormat == null || sSRS == null || fullExtent == null ) {
			if (!drv.connect(null))
				throw new ConnectionErrorLayerException(getName(),null);

			WMSLayerNode[] wmsNodeList = new WMSLayerNode[sLayers.length];
			for (int i=0; i<sLayers.length; i++) {
				wmsNodeList[i] = drv.getLayer(sLayers[i]);
			}

			if (wmsNodeList == null || wmsNodeList.length==0){
				throw new LoadLayerException(getName(),null);
			}
			if( sFormat == null ) {
				sFormat = this.getGreatFormat(drv.getFormats());
			}
			// SRS
			ArrayList allSrs = new ArrayList();
			for (int i=0; i<wmsNodeList.length; i++) {
				allSrs.addAll(wmsNodeList[i].getAllSrs());
			}
			boolean isSRSSupported = false;
			if( sSRS != null ) {
				for (int i=0; i<allSrs.size() ; i++){
					if (((String)allSrs.get(i)).compareTo(sSRS) == 0){
						isSRSSupported = true;
					}
				}
			}

			if(!isSRSSupported) {
				for (int i=0; i<allSrs.size() ; i++){
					if (((String)allSrs.get(i)).compareTo("EPSG:4326") == 0){
						sSRS = (String)allSrs.get(i);
					}
				}
				if (sSRS==null){
					sSRS = (String)allSrs.get(0);
				}
			}
			if( fullExtent == null ) {
				fullExtent = drv.getLayersExtent(sLayers,sSRS);
			}
		}


		this.setFullExtent(fullExtent);
		this.setFormat(sFormat);
		this.setLayerQuery(sLayer);
		this.setInfoLayerQuery("");
		this.setSRS(sSRS);
		this.setName(sLayer);
		this.setOnlineResources(drv.getOnlineResources());
		if (drv.getInfoFormats() != null) {
			if (drv.getInfoFormats().size() > 0)
				this.setInfoFormat((String) drv.getInfoFormats().get(0));
		}
		
		this.setDriver(drv);
		initServerScale();
		load();
	}

	private void setInfoFormat(String infoFormat) {
		this.infoFormat = infoFormat;
	}

	/**
	 * It choose the best format to load different maps if the server
	 * supports it. This format could be png, because it supports
	 * transparency.
	 * @param formats
	 * Arraywith all the formats supported by the server
	 * @return
	 */
	private String getGreatFormat(Vector formats){
			for (int i=0 ; i<formats.size() ; i++){
					String format = (String) formats.get(i);
				if (format.equals("image/png")
						|| format.equals("image/jpg")
						|| format.equals("image/jpeg")) {
					return format;
				}
			}

			return (String)formats.get(0);
	}

	/**
	 * Clase que contiene los datos de visualización de WMS.
	 * @author Nacho Brodin (brodin_ign@gva.es)
	 */
	private class VisualStatus{
		/**
		 * Ancho y alto de la imagen o del conjunto de tiles si los tiene. Coincide con
		 * el ancho y alto del viewPort
		 */
		private double						minX = 0D;
		private double						minY = 0D;
		private double						maxX = 0D;
		private double						maxY = 0D;
		/**
		 * Lista de nombre de fichero que componen toda la visualización.
		 */
		private String[]					fileNames = null;

		public Object clone() {
			VisualStatus s = new VisualStatus();
			s.maxX = maxX;
			s.maxY = maxY;
			s.minX = minX;
			s.minY = minY;
			s.fileNames = new String[fileNames.length];
			for (int i = 0; i < fileNames.length; i++)
				s.fileNames[i] = fileNames[i];
			return s;
		}
	}


	/**
	 * Devuelve el XMLEntity con la información necesaria para reproducir la
	 * capa.
	 *
	 * @return XMLEntity.
	 * @throws XMLException
	 */
	public XMLEntity getXMLEntity() throws XMLException {
		XMLEntity xml = super.getXMLEntityWithoutChecks();
		if (xml != null){

			// Full extent
			xml.putProperty("fullExtent", StringUtilities.rect2String(fullExtent));

			// Host
			xml.putProperty("host", host.toExternalForm());

			// Part of the query that is not the host, or the
			// layer names, or other not listed bellow
			xml.putProperty("infoLayerQuery", infoLayerQuery);

			// Part of the query containing the layer names
			xml.putProperty("layerQuery", layerQuery);

			// Format
			xml.putProperty("format", m_Format);

			// SRS
			xml.putProperty("srs", m_SRS);
			if (status!=null)
				status.getXMLEntity(xml, true, this);
			else{
				status = new StatusLayerRaster();
				status.getXMLEntity(xml, true, this);
			}

			// Transparency
			xml.putProperty("wms_transparency", wmsTransparency);

			// Styles
			if (styles!=null){
				String stylePr = "";
				for (int i = 0; i < styles.size(); i++) {
					stylePr += styles.get(i).toString();
					if (i<styles.size()-1)
						stylePr += ",";
				}
				if (stylePr.endsWith(","))
					stylePr += " ";
				xml.putProperty("styles", stylePr);
			}

			// Dimensions
			if (dimensions!=null){
				String dim = "";
				for (int i = 0; i < dimensions.size(); i++) {
					dim += (String) dimensions.get(i);
					if (i<dimensions.size()-1)
						dim += ",";
				}
				if (dim.endsWith(","))
					dim += " ";
				xml.putProperty("dimensions", dim);
			}

			// OnlineResources
			Iterator it = onlineResources.keySet().iterator();
			String strOnlines = "";
			while (it.hasNext()) {
				String key = (String) it.next();
				String value = (String) onlineResources.get(key);
				strOnlines += key+"~##SEP2##~"+value;
				if (it.hasNext())
					strOnlines += "~##SEP1##~";
			}
			xml.putProperty("onlineResources", strOnlines);

			// Queryable
			xml.putProperty("queryable", queryable);
			if(wms != null)
				xml.putProperty("hasImageLegend", wms.hasLegendGraphic());

			// fixedSize
			if (isSizeFixed()) {
				xml.putProperty("fixedSize", true);
				xml.putProperty("fixedWidth", fixedSize.width);
				xml.putProperty("fixedHeight", fixedSize.height);
			}
		}
		return xml;
	}

	/**
	 * A partir del XMLEntity reproduce la capa.
	 *
	 * @param xml XMLEntity
	 *
	 * @throws XMLException
	 * @throws DriverException
	 * @throws DriverIOException
	 */
	public void setXMLEntity03(XMLEntity xml)
		throws XMLException {
		super.setXMLEntity(xml);
		fullExtent = StringUtilities.string2Rect(xml.getStringProperty(
					"fullExtent"));

		try {
			host = new URL(xml.getStringProperty("host"));
		} catch (MalformedURLException e) {
			throw new XMLException(e);
		}

		infoLayerQuery = xml.getStringProperty("infoLayerQuery");
		layerQuery = xml.getStringProperty("layerQuery");
		m_Format = xml.getStringProperty("format");
		m_SRS = xml.getStringProperty("srs");
	}

	/**
	 * A partir del XMLEntity reproduce la capa.
	 *
	 * @param xml XMLEntity
	 *
	 * @throws XMLException
	 * @throws DriverException
	 * @throws DriverIOException
	 */
	public void setXMLEntity(XMLEntity xml)
		throws XMLException {
		for (int i = 0; i < xml.getPropertyCount(); i++) {
			String key = xml.getPropertyName(i);
			if(key.startsWith("raster.file")) {
				xml.putProperty(key, "");
			}
		}


		super.setXMLEntity(xml);
		fullExtent = StringUtilities.string2Rect(xml.getStringProperty(
					"fullExtent"));

		// Host
		try {
			host = new URL(xml.getStringProperty("host"));
		} catch (MalformedURLException e) {
			throw new XMLException(e);
		}

		// Part of the query that is not the host, or the
		// layer names, or other not listed bellow
		infoLayerQuery = xml.getStringProperty("infoLayerQuery");

		// Part of the query containing the layer names
		layerQuery = xml.getStringProperty("layerQuery");

		// Format
		m_Format = xml.getStringProperty("format");

		// SRS
		m_SRS = xml.getStringProperty("srs");

		String claseStr = StatusLayerRaster.defaultClass;
		if (xml.contains("raster.class")) {
			claseStr = xml.getStringProperty("raster.class");
		}

		// Transparency
				if (xml.contains("wms_transparency"))
						wmsTransparency = xml.getBooleanProperty("wms_transparency");

				// Styles
				if (xml.contains("styles")){
						styles = new Vector();
						String[] stl = xml.getStringProperty("styles").split(",");

						for (int i = 0; i < stl.length; i++) {
							if (stl[i].equals(" "))
								stl[i]="";
								styles.add(stl[i]);
						}
				}

				// Dimensions
				if (xml.contains("dimensions")){
						dimensions = new Vector();
						String[] dims = xml.getStringProperty("dimensions").split(",");
						for (int i = 0; i < dims.length; i++){
							if (dims[i].equals(" "))
								dims[i]="";

								dimensions.add(dims[i]);
						}
				}

				// OnlineResources
				if (xml.contains("onlineResources")) {
					String[] operations = xml.getStringProperty("onlineResources").split("~##SEP1##~");
					for (int i = 0; i < operations.length; i++) {
				String[] resources = operations[i].split("~##SEP2##~");
				if (resources.length==2 && resources[1]!="")
					onlineResources.put(resources[0], resources[1]);
			}
				}

				// Queryable
				queryable = true; // let's assume that the layer is queryable by default
				if (xml.contains("queryable"))
					queryable = xml.getBooleanProperty("queryable");

				hasImageLegend=false;
				if (xml.contains("hasImageLegend")){
					hasImageLegend=xml.getBooleanProperty("hasImageLegend");
				}

				// fixedSize
				if (xml.contains("fixedSize")) {
					fixedSize = new Dimension(xml.getIntProperty("fixedWidth"),
																xml.getIntProperty("fixedHeight"));
				}

		if(status!=null)
			status.setXMLEntity(xml, this);
		else{
			if(claseStr != null && !claseStr.equals("")){
				try{
					Class clase = LayerFactory.getLayerClassForLayerClassName(claseStr);
					Constructor constr = clase.getConstructor(null);
					status = (IStatusRaster)constr.newInstance(null);
					if(status != null) {
						((StatusLayerRaster)status).setNameClass(claseStr);
						status.setXMLEntity(xml, this);
						filterArguments = status.getFilterArguments();
						transparency = status.getTransparency();
						renderBands = status.getRenderBands();
					}
				}catch(ClassNotFoundException exc){
					exc.printStackTrace();
				}catch(InstantiationException exc){
					exc.printStackTrace();
				}catch(IllegalAccessException exc){
					exc.printStackTrace();
				}catch(NoSuchMethodException exc){
					exc.printStackTrace();
				}catch(InvocationTargetException exc){
					exc.printStackTrace();
				}
			}
		}
		firstLoad = true;
	}

	/**
	 * @throws ReadDriverException
	 * @throws LoadLayerException
	 * @see com.iver.cit.gvsig.fmap.layers.layerOperations.InfoByPoint#queryByPoint(com.iver.cit.gvsig.fmap.operations.QueriedPoint)
	 */
	public XMLItem[] getInfo(Point p, double tolerance, Cancellable cancellable) throws ReadDriverException {
		XMLItem[] item = new XMLItem[1];
		try {
			if (queryable) 	{
				//TODO
				// check if there are layers which are not queryable
				ViewPort viewPort = getMapContext().getViewPort();

				Point tiledPoint = new Point((int) p.getX() % maxTilePrintWidth, (int) p.getY() % maxTilePrintHeight);
				Rectangle rect = new Rectangle(0, 0, viewPort.getImageWidth() - 1, viewPort.getImageHeight() - 1);
				Tiling tiles = new Tiling(maxTilePrintWidth, maxTilePrintHeight, rect);
				tiles.setAffineTransform((AffineTransform) viewPort.getAffineTransform().clone());
				int nCols = tiles.getNumCols();

				int col = (int) p.getX() / maxTilePrintWidth;
				int row = (int) p.getY() / maxTilePrintHeight;
				int tileIndex = (row*nCols) + col;

				ViewPort vp = tiles.getTileViewPort(viewPort, tileIndex);
				wmsStatus.setExtent(vp.getAdjustedExtent());
				wmsStatus.setHeight(vp.getImageHeight());
				wmsStatus.setWidth(vp.getImageWidth());
				wmsStatus.setOnlineResource((String) onlineResources.get("GetFeatureInfo"));
				
				// We search for application string
				// TODO: Maybe the user wants to select the format	
				if (infoFormat == null) {
					try {
						infoFormat = "text/xml"; // bydefault
						if (getDriver().getInfoFormats().size() > 0) {
							for (int iFormat = 0; iFormat < getDriver().getInfoFormats().size(); iFormat++ ) {
								String auxFormat = (String) getDriver().getInfoFormats().get(iFormat);
								if (auxFormat.contains("vnd.ogc")) {
									infoFormat = auxFormat;
									break;
								}
							}
						}
					} catch (UnsupportedVersionLayerException e) {
						e.printStackTrace();
						throw new ReadDriverException(FMapWMSDriver.class.getName()+"::"+getName()+" - UnsupportedVersionLayerException", e);
					} catch (IllegalStateException e) {
						e.printStackTrace();
						throw new ReadDriverException(FMapWMSDriver.class.getName()+"::"+getName()+" - UnsupportedVersionLayerException", e);
					}
				}
				wmsStatus.setInfoFormat(infoFormat);


				wmsStatus.setFormat( m_Format );
				wmsStatus.setLayerNames(Utilities.createVector(layerQuery,","));
				wmsStatus.setSrs(m_SRS);
				wmsStatus.setCrsAxisOrder(cachedAxisOrientation);
				wmsStatus.setStyles(styles);
				wmsStatus.setDimensions(dimensions);
				wmsStatus.setTransparency(wmsTransparency);
				MyCancellable c = new MyCancellable(cancellable);
				try {
					item[0] = new StringXMLItem(new String(getDriver()
							.getFeatureInfo(wmsStatus, (int) tiledPoint.getX(), (int) tiledPoint.getY(), Integer.MAX_VALUE, c)),this);
				} catch (UnsupportedVersionLayerException e) {
					throw new ReadDriverException(FMapWMSDriver.class.getName()+"::"+getName()+" - UnsupportedVersionLayerException", e);
				} catch (IllegalStateException e) {
					throw new ReadDriverException(FMapWMSDriver.class.getName()+"::"+getName()+" - IllegalStateException", e);
				}
				return item;
			}
			else
			{
				JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(),this.getName() + " " +
						PluginServices.getText(this,"layer_not_queryable"));
				item[0] =  new StringXMLItem("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><info></info>",this);
				return item;
				//return null;
			}
		} catch (WMSException  e) {
			item[0] = new StringXMLItem("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?><exception>" +
			e.getMessage() + "</exception>", this);
			return item;
		} catch (ValidationException e) {
			throw new ReadDriverException(FMapWMSDriver.class.getName()+"::"+getName()+" - ValidationException", e);
		} catch (IOException e) {
			throw new ReadDriverException(FMapWMSDriver.class.getName()+"::"+getName()+" - IOException", e);
		} catch (NoninvertibleTransformException e) {
			NotificationManager.addError("NotinvertibleTransform", e);
		}
		return null;
	}

	/*
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#getFullExtent()
	 */
	public Rectangle2D getFullExtent() {
		return fullExtent;
	}

	/*
	 *
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#draw(java.awt.image.BufferedImage,
	 * 		java.awt.Graphics2D, com.iver.cit.gvsig.fmap.ViewPort,
	 * 		com.iver.cit.gvsig.fmap.operations.Cancellable)
	 */
	private int callCount; // mess code, represents the amount of times the methods drawFixedSize or drawTile where tried for an extent
	private static final int MAX_RETRY_TIMES = 5; // mess code, represents the max amount of retries allowed.
	public void draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
			Cancellable cancel,double scale) throws ReadDriverException {
		callCount = 0; // mess code
		lastNColumns = lastNRows = 0;

		enableStopped();

		closeAndFree();

		if (isWithinScale(scale)){
			Point2D p = viewPort.getOffset();
			// p will be (0, 0) when drawing a view or other when painting onto
			// the Layout.
			visualStatus.minX = viewPort.getAdjustedExtent().getMinX();
			visualStatus.minY = viewPort.getAdjustedExtent().getMinY();
			visualStatus.maxX = viewPort.getAdjustedExtent().getMaxX();
			visualStatus.maxY = viewPort.getAdjustedExtent().getMaxY();


			if (isSizeFixed()) {
				// This condition handles those situations in which the server can
				// only give static extent and resolution maps despite we need
				// a specific BBOX and pixel WIDTH and HEIGHT
				try {
					visualStatus.fileNames = new String[1];
					layerRaster = new FLyrRasterSE[1];
					drawFixedSize(g, viewPort, cancel, scale);
					if(layerRaster != null && layerRaster[0] != null) {
						dataset = layerRaster[0].getDataSource();
						initializeRasterLayer(null, new IBuffer[][]{{layerRaster[0].getRender().getLastRenderBuffer()}});
						getRender().setLastRenderBuffer(layerRaster[0].getRender().getLastRenderBuffer());
					}
				} catch (LoadLayerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
				}

			} else {
				if(mustTileDraw){
					Rectangle r = new Rectangle((int) p.getX(), (int) p.getY(), viewPort.getImageWidth(), viewPort.getImageHeight());
					Tiling tiles = new Tiling(maxTileDrawWidth, maxTileDrawHeight, r);
					tiles.setAffineTransform((AffineTransform) viewPort.getAffineTransform().clone());
					visualStatus.fileNames = new String[tiles.getNumTiles()];
					MultiRasterDataset[][] datasets = new MultiRasterDataset[tiles.getNumRows()][tiles.getNumCols()];
					IBuffer[][] buf = new IBuffer[tiles.getNumRows()][tiles.getNumCols()];
					layerRaster = new FLyrRasterSE[tiles.getNumTiles()];
					lastNColumns = tiles.getNumCols();
					lastNRows = tiles.getNumRows();
					for (int tileNr = 0; tileNr < tiles.getNumTiles(); tileNr++) {
						// drawing part
						try {
							ViewPort vp = tiles.getTileViewPort(viewPort, tileNr);
							boolean painted = drawTile(g, vp, cancel, tileNr, scale, tileNr);
							if(	layerRaster != null &&
									layerRaster[tileNr] != null &&
									painted) {
									datasets[(int)(tileNr / tiles.getNumCols())][tileNr % tiles.getNumCols()] = (MultiRasterDataset)layerRaster[tileNr].getDataSource().newDataset();
									buf[(int)(tileNr / tiles.getNumCols())][tileNr % tiles.getNumCols()] = layerRaster[tileNr].getRender().getLastRenderBuffer();
								}
						} catch (NoninvertibleTransformException e) {
							e.printStackTrace();
						} catch (LoadLayerException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					try {
						if(datasets != null && datasets[0][0] != null) {
							dataset = new CompositeDataset(datasets);
                                                        if (layerRaster != null && layerRaster[0] != null) {
                                                            initializeRasterLayer(datasets, buf);
                                                        }
							buf = null;
						}
					} catch (MosaicNotValidException e) {
						throw new ReadDriverException("No hay continuidad en el mosaico.", e);
					} catch (LoadLayerException e) {
						throw new ReadDriverException("Error inicializando la capa.", e);
					} catch (InterruptedException e) {
					}
				} else
					try {
						layerRaster = new FLyrRasterSE[1];
						visualStatus.fileNames = new String[1];
						drawTile(g, viewPort, cancel, 0, scale, 0);
						if(layerRaster != null && layerRaster[0] != null) {
							dataset = layerRaster[0].getDataSource();
							getRender().setLastRenderBuffer(layerRaster[0].getRender().getLastRenderBuffer());
							initializeRasterLayer(null, new IBuffer[][]{{layerRaster[0].getRender().getLastRenderBuffer()}});
						}
					} catch (LoadLayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InterruptedException e) {
					}
			}
		}
		disableStopped();

		/*Runtime r = Runtime.getRuntime();
		System.err.println("********************WMS**********************");
		System.err.println("Memoria Total: " + (r.totalMemory() / 1024) +"KB");
		System.err.println("Memoria Usada: " + ((r.totalMemory() - r.freeMemory()) / 1024) +"KB");
		System.err.println("Memoria Libre: " + (r.freeMemory() / 1024) +"KB");
		System.err.println("Memoria MaxMemory: " + (r.maxMemory() / 1024) +"KB");
		System.err.println("*********************************************");*/
	}

	/**
	 * Closes files and releases memory (pointers to null)
	 */
	synchronized private void closeAndFree() {
		int count = 0;
		while(readingData != null && readingData.compareTo(Thread.currentThread().toString()) != 0)
			try {
				Thread.sleep(100);
				count++;
				if (count==1000)
					readingData = null;
			} catch (InterruptedException e) {
			}

		if(dataset != null) {
			dataset.close();
			dataset = null;
		}

		//Cerramos el dataset asociado a la capa si está abierto.
		if(layerRaster != null) {
			for (int i = 0; i < layerRaster.length; i++) {
				if(layerRaster[i] != null) {
					layerRaster[i].setRemoveRasterFlag(true);
					layerRaster[i].getDataSource().close();
					System.out.println("CloseAndFree WMS" + this.getName());
					if (layerRaster[i] != null) {
						layerRaster[i].getRender().free();
						System.out.println("CloseAndFree WMS after render free" + this.getName());
						if(layerRaster[i].getBufferFactory() != null)
							layerRaster[i].getBufferFactory().free();
						layerRaster[i] = null;
						System.out.println("CloseAndFree WMS after bufferFactory free");
					}
				}
			}
		}
		if (getRender() != null)
			getRender().free();
		System.out.println("CloseAndFree WMS before System.gc");
		System.gc();
	}

	/**
	 * Acciones que se realizan después de asignar la fuente de datos a
	 * la capa raster.
	 *
	 * @throws LoadLayerException
	 * @throws InterruptedException
	 */
	private void initializeRasterLayer(MultiRasterDataset[][] datasets, IBuffer[][] buf) throws LoadLayerException, InterruptedException {
		if(this.filterList != null)
			getRender().setFilterList(filterList);
		if(this.transparency != null)
			getRender().setLastTransparency(transparency);
		if(this.renderBands != null)
			getRender().setRenderBands(renderBands);
		if(datasets != null) {
			String[][] names = new String[datasets.length][datasets[0].length];
			for (int i = 0; i < datasets.length; i++) {
				for (int j = 0; j < datasets[i].length; j++) {
					if(datasets[i][j] != null)
						names[i][j] = datasets[i][j].getDataset(0)[0].getFName();
				}
			}
			super.setLoadParams(names);
		}
		super.init();
		if(buf != null && buf[0][0] != null) {
			int drawablesBandCount = layerRaster[0].getDataSource().getBands().getDrawableBandsCount();
			IBuffer buff = null;
			if(dataset instanceof CompositeDataset)
				buff = ((CompositeDataset)dataset).generateBuffer(buf, drawablesBandCount);
			else
				buff = buf[0][0];

			if(getRender().getLastRenderBuffer() != null)
				getRender().getLastRenderBuffer().free();
			getRender().setLastRenderBuffer(buff);
		}
	}

    private void drawFixedSize(Graphics2D g, ViewPort vp, Cancellable cancel,
            double scale) throws ReadDriverException, LoadLayerException {
        drawFixedSize(g, vp, cancel, scale, false);
    }

    private void drawFixedSize(Graphics2D g, ViewPort vp, Cancellable cancel,
            double scale, boolean compress) throws ReadDriverException,
            LoadLayerException {
		callCount++; // mess code, it is not unusual a wms server to response an error which is completely
					 // temporal and the response is available if we retry requesting.
					 //


		// This is the extent that will be requested
		Rectangle2D bBox = getFullExtent();
		MyCancellable c = new MyCancellable(cancel);

		try {
			wmsStatus.setExtent( bBox );
			wmsStatus.setFormat( m_Format );
			wmsStatus.setHeight( fixedSize.height );
			wmsStatus.setWidth( fixedSize.width );
			wmsStatus.setLayerNames(Utilities.createVector(layerQuery,","));
			wmsStatus.setSrs(m_SRS);
			wmsStatus.setCrsAxisOrder(cachedAxisOrientation);
			wmsStatus.setStyles(styles);
			wmsStatus.setDimensions(dimensions);
			wmsStatus.setTransparency(wmsTransparency);
			wmsStatus.setOnlineResource((String) onlineResources.get("GetMap"));
			File f = getDriver().getMap(wmsStatus, c);
			if (f == null)
				return;
			String nameWorldFile = f.getPath() + getExtensionWorldFile();
			com.iver.andami.Utilities.createTemp(nameWorldFile, this.getDataWorldFile(bBox, fixedSize));

			IStatusRaster status = getStatus();
			if(status!=null && firstLoad){
				try {
					status.applyStatus(this);
				} catch (NotSupportedExtensionException e) {
					throw new ReadDriverException("", e);
				} catch (RasterDriverException e) {
					throw new ReadDriverException("", e);
				} catch (FilterTypeException e) {
					throw new ReadDriverException("", e);
				}
				firstLoad = false;
			}

			// And finally, obtain the extent intersecting the view and the BBox
			// to draw to.
			Rectangle2D extent = new Rectangle2D.Double();
			Rectangle2D.intersect(vp.getAdjustedExtent(), bBox, extent);

			ViewPortData vpData = new ViewPortData(
				vp.getProjection(), new Extent(extent), fixedSize );
			vpData.setMat(vp.getAffineTransform());

			String filePath = f.getAbsolutePath();
			visualStatus.fileNames[0] = filePath;

			try {
                rasterProcess(filePath, g, vp, scale, cancel, 0, compress);
				this.updateDrawVersion();
			} catch (FilterTypeException e) {
			}
		} catch (ValidationException e) {
			if (!c.isCanceled())
			{
				LoadLayerException exception = new LoadLayerException(getName(),e);
				throw exception;
			}
		} catch (IOException e) {
			if (!c.isCanceled())
				if (callCount<MAX_RETRY_TIMES) { // mess code
					NotificationManager.addWarning("\n[ FLyrWMS.drawFixedSize() ]  Failed in trying " + callCount + "/" + MAX_RETRY_TIMES + ")\n", null); // mess code
					// I'll try again requesting up to MAX_RETRY_TIMES times before throw an error // mess code
					// (this is mess code, should be replaced by a layer status handler which is scheduled for version > 1.0) // mess code
                    drawFixedSize(g, vp, cancel, scale, compress); // mess code
				} // mess code

				if (callCount == 1) { // mess code
					ConnectionErrorExceptionType type = new ConnectionErrorExceptionType();
					type.setLayerName(getName());
					try {
						type.setDriverName("WMS Driver");
					} catch (Exception e1) {
					}
					type.setHost(host);
					throw new ConnectionErrorLayerException(getName(),e);
				} // mess code

		} catch (WMSException e) {
			if (!c.isCanceled()) {
				if (callCount<MAX_RETRY_TIMES) { // mess code
					NotificationManager.addWarning("\n[ FLyrWMS.drawFixedSize() ]  Failed in trying " + callCount + "/" + MAX_RETRY_TIMES + ")\n", null); // mess code
					// I'll try again requesting up to MAX_RETRY_TIMES times before throw an error // mess code
					// (this is mess code, should be replaced by a layer status handler which is scheduled for version > 1.0) // mess code
                    drawFixedSize(g, vp, cancel, scale, compress); // mess code
				} // mess code
				if (callCount == 1) { // mess code
					if (!isPrinting)
						this.setVisible(false);
					throw new LoadLayerException(getName(),e);


				} // mess code
			}
		}catch (NullPointerException e) {
			this.setVisible(false);
		}
		callCount--; // mess code
	}

    private void rasterProcess(String filePath, Graphics2D g, ViewPort vp,
            double scale, Cancellable cancel, int nLyr)
            throws ReadDriverException, LoadLayerException, FilterTypeException {
        rasterProcess(filePath, g, vp, scale, cancel, nLyr, false);
    }

	/**
	 * Carga y dibuja el raster usando la librería
	 * @param filePath Ruta al fichero en disco
	 * @param g Graphics2D
	 * @param vp ViewPort
	 * @param scale Escala para el draw
	 * @param cancel Cancelación para el draw
	 * @throws ReadDriverException
	 * @throws LoadLayerException
	 */
    private void rasterProcess(String filePath, Graphics2D g, ViewPort vp,
            double scale, Cancellable cancel, int nLyr, boolean compress)
            throws ReadDriverException, LoadLayerException, FilterTypeException {
                closeAndFree();
	        //Cargamos el dataset con el raster de disco.
		layerRaster[nLyr] = FLyrRasterSE.createLayer("", filePath, vp.getProjection());
		//layerRaster[nLyr].getRender().setBufferFactory(layerRaster[nLyr].getBufferFactory());
		layerRaster[nLyr].setNoDataValue(getNoDataValue());
		layerRaster[nLyr].setNoDataType(getNoDataType());
//		layerRaster[nLyr].init();

		//Obtenemos la tabla de color del raster abierto ya que se le va a sustituir la lista
		//de filtros y el de tabla de color no queremos sustituirlo.
		RasterFilterList rasterFilterList = layerRaster[nLyr].getRender().getFilterList();
		ColorTableFilter ct = (ColorTableFilter)rasterFilterList.getFilterByBaseClass(ColorTableFilter.class);
		Object param = null;
		if(ct != null)
			param = ct.getParam("colorTable");

		//En caso de cargar un proyecto con XMLEntity se crean los filtros
		if(filterArguments != null) {
			RasterFilterList fl = new RasterFilterList();
			fl.addEnvParam("IStatistics", layerRaster[nLyr].getDataSource().getStatistics());
			fl.addEnvParam("MultiRasterDataset", layerRaster[nLyr].getDataSource());
			fl.setInitDataType(layerRaster[nLyr].getDataType()[0]);
			RasterFilterListManager filterListManager = new RasterFilterListManager(fl);
			filterListManager.createFilterListFromStrings(filterArguments);
			filterArguments = null;
			filterList = fl;
		}

		//Como el raster se carga a cada zoom el render se crea nuevamente y la lista de
		//filtros siempre estará vacia a cada visualización. Para evitarlo tenemos que
		//guardar la lista de filtro aplicada en la visualización anterior.
		if(this.filterList != null) {
			this.disableUpdateDrawVersion();
			//Si tenía tabla de color le asignamos la original
			if(param != null && param instanceof GridPalette) {
				this.filterList.remove(ColorTableFilter.class);
				RasterFilterListManager filterManager = new RasterFilterListManager(filterList);
				ColorTableListManager ctm = new ColorTableListManager(filterManager);
				ctm.addColorTableFilter((GridPalette)param);
				filterList.move(ColorTableFilter.class, 0);
				filterList.controlTypes();
			}
			layerRaster[nLyr].getRender().setFilterList(filterList);
			this.enableUpdateDrawVersion();
		}
		if(this.transparency != null)
			layerRaster[nLyr].getRender().setLastTransparency(transparency);
		if(this.renderBands != null)
			layerRaster[nLyr].getRender().setRenderBands(renderBands);

		//Dibujamos
		disableUpdateDrawVersion();
        layerRaster[nLyr].draw(null, g, vp, cancel, scale, compress);
		enableUpdateDrawVersion();

		//La primera vez asignamos la lista de filtros asociada al renderizador. Guardamos una referencia
		//en esta clase para que a cada zoom no se pierda.
		if (this.filterList == null)
			filterList = layerRaster[nLyr].getRender().getFilterList();
		if (this.transparency == null)
			transparency = layerRaster[nLyr].getRender().getLastTransparency();
		if (this.renderBands == null)
			renderBands = layerRaster[nLyr].getRender().getRenderBands();

		transparency.free();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.IRasterRendering#getRenderFilterList()
	 */
	public RasterFilterList getRenderFilterList(){
		return (filterList != null) ? filterList : getRender().getFilterList();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.raster.hierarchy.IRasterRendering#setRenderFilterList(org.gvsig.raster.grid.filter.RasterFilterList)
	 */
	public void setRenderFilterList(RasterFilterList filterList) {
		if (filterList == this.filterList){
			return;
		}
		this.filterList = filterList;
		this.updateDrawVersion();
		super.getRender().setFilterList(filterList);
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.IRasterRendering#getRenderTransparency()
	 */
	public GridTransparency getRenderTransparency() {
		return (transparency != null) ? transparency : getRender().getLastTransparency();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.raster.hierarchy.IRasterRendering#getRenderBands()
	 */
	public int[] getRenderBands() {
		return (renderBands != null) ? renderBands : getRender().getRenderBands();
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.raster.hierarchy.IRasterRendering#setRenderBands(int[])
	 */
	public void setRenderBands(int[] renderBands) {
		//TODO: Comprobar si hay cambios
		this.renderBands = renderBands;
		this.updateDrawVersion();
		getRender().setRenderBands(renderBands);
	}

    private boolean drawTile(Graphics2D g, ViewPort vp, Cancellable cancel,
            int tile, double scale, int nLyr) throws LoadLayerException,
            ReadDriverException {
        return drawTile(g, vp, cancel, tile, scale, nLyr, false);
    }

	/**
	 * This is the method used to draw a tile in a WMS mosaic layer.
	 * @throws LoadLayerException
	 * @throws ReadDriverException
	 * @return true when a tile has been painted
	 */
    private boolean drawTile(Graphics2D g, ViewPort vp, Cancellable cancel,
            int tile, double scale, int nLyr, boolean compress)
            throws LoadLayerException, ReadDriverException {
		callCount++;
		// Compute the query geometry
		// 1. Check if it is within borders
//		Rectangle2D extent = getFullExtent();
//				if ((vp.getAdjustedExtent().getMinX() > extent.getMaxX()) ||
//								(vp.getAdjustedExtent().getMinY() > extent.getMaxY()) ||
//								(vp.getAdjustedExtent().getMaxX() < extent.getMinX()) ||
//								(vp.getAdjustedExtent().getMaxY() < extent.getMinY())) {
//						return false;
//				}

				// 2. Compute extent to be requested.
//				Rectangle2D bBox = new Rectangle2D.Double();
//				Rectangle2D.intersect(vp.getAdjustedExtent(), extent, bBox); // FIXME: ESTO PROVOCA FALLOS SI EL EXTENT (fullextent) no es correcto

				Rectangle2D bBox = vp.getAdjustedExtent();
				
				// 3. Compute size in pixels
				double scalex = vp.getAffineTransform().getScaleX();
				double scaley = vp.getAffineTransform().getScaleY();
				int wImg = (int) Math.ceil(Math.abs(bBox.getWidth() * scalex) + 1);
				int hImg = (int) Math.ceil(Math.abs(bBox.getHeight() * scaley) + 1);

				Dimension sz = new Dimension(wImg, hImg);

				if ((wImg <= 0) || (hImg <= 0)) {
						return false;
				}
				MyCancellable c = new MyCancellable(cancel);

		try {
			sz = new Dimension(wImg, hImg);
			// Rectangle2D.intersect(vp.getAdjustedExtent(), extent, bBox);


			wmsStatus.setExtent( bBox );
			wmsStatus.setFormat(m_Format);
			wmsStatus.setHeight( hImg );
			wmsStatus.setWidth( wImg );
			wmsStatus.setLayerNames(Utilities.createVector(layerQuery,","));
			wmsStatus.setSrs(m_SRS);
			wmsStatus.setCrsAxisOrder(cachedAxisOrientation);
			wmsStatus.setStyles(styles);
			wmsStatus.setDimensions(dimensions);
			wmsStatus.setTransparency(wmsTransparency);
			wmsStatus.setOnlineResource((String) onlineResources.get("GetMap"));

			// begin patch; Avoid to request too small tiles.
			// This generally occurs when printing

			if (wImg < minTilePrintWidth) {
				double wScale = (double) minTilePrintWidth / wImg;
				wmsStatus.setWidth(minTilePrintWidth);
				Rectangle2D sExtent = wmsStatus.getExtent();
				Point2D initialPoint = new Point2D.Double(sExtent.getX(), sExtent.getY());
				sExtent.setRect(sExtent.getX()*wScale, sExtent.getY(), sExtent.getWidth()*wScale, sExtent.getHeight());
				if (!bBox.contains(initialPoint)) {
					sExtent.setRect(sExtent.getX() - initialPoint.getX(), sExtent.getY(), sExtent.getWidth(), sExtent.getHeight());
				}
			}

			if (hImg < minTilePrintHeight) {
				double hScale = (double) minTilePrintHeight / hImg;
				wmsStatus.setHeight(minTilePrintHeight);
				Rectangle2D sExtent = wmsStatus.getExtent();
				Point2D initialPoint = new Point2D.Double(sExtent.getX(), sExtent.getY());
				sExtent.setRect(sExtent.getX(), sExtent.getY()*hScale, sExtent.getWidth(), sExtent.getHeight()*hScale);
				if (!bBox.contains(initialPoint)) {
					sExtent.setRect(sExtent.getX(), sExtent.getY() - initialPoint.getY(), sExtent.getWidth(), sExtent.getHeight());
				}
			}

			// end patch
			File f = getDriver().getMap(wmsStatus, c);
			if (f == null)
				return false;
			String nameWordFile = f.getPath() + getExtensionWorldFile();
			com.iver.andami.Utilities.createTemp(nameWordFile, this.getDataWorldFile(bBox, sz));

			ViewPortData vpData = new ViewPortData(
				vp.getProjection(), new Extent(bBox), sz );
			vpData.setMat(vp.getAffineTransform());

			String filePath = f.getAbsolutePath();
			visualStatus.fileNames[tile] = filePath;
			try {
                rasterProcess(filePath, g, vp, scale, cancel, nLyr, compress);
//				this.updateDrawVersion();
			} catch (FilterTypeException e) {
			}

		} catch (ValidationException e) {
			if (!c.isCanceled())
			{
				LoadLayerException exception = new LoadLayerException(getName(),e);
				throw exception;
			}
		} catch (IOException e) {
			if (!c.isCanceled()){
				if (callCount<MAX_RETRY_TIMES) { // mess code
					NotificationManager.addWarning("\n[ FLyrWMS.drawFixedSize() ]  Failed in trying " + callCount + "/" + MAX_RETRY_TIMES + ")\n", null); // mess code
					// I'll try again requesting up to MAX_RETRY_TIMES times before throw an error // mess code
					// (this is mess code, should be replaced by a layer status handler which is scheduled for version > 1.0) // mess code

                    drawFixedSize(g, vp, cancel, scale, compress); // mess code
				} // mess code
			}
			if (callCount == 1) { // mess code
				throw new ConnectionErrorLayerException(getName(),e);
			}//if
		} catch (WMSException e) {
			if (!c.isCanceled()) {
				if (callCount<MAX_RETRY_TIMES) { // mess code
					Logger.getAnonymousLogger().warning("\n[ FLyrWMS.drawFixedSize() ]  Failed in trying " + callCount + "/" + MAX_RETRY_TIMES + ")\n"); // mess code
					// I'll try again requesting up to MAX_RETRY_TIMES times before throw an error // mess code
					// (this is mess code, should be replaced by a layer status handler which is scheduled for version > 1.0) // mess code
                    drawTile(g, vp, cancel, tile, scale, nLyr, compress);
				} // mess code
				if (callCount == 1) { // mess code
//		azabala			JOptionPane.showMessageDialog((Component)PluginServices.getMainFrame(), e.getMessage());
					WMSDriverExceptionType type = new WMSDriverExceptionType();
					type.setLayerName(getName());
					try {
						type.setDriverName("WMS Driver");
					} catch (Exception e1) {
					}
					type.setWcsStatus(this.wmsStatus);
					if (!isPrinting)
						this.setVisible(false);
					throw new LoadLayerException(getName(),e);


				} //if
			}//if
		}//catch
		callCount--;
		return true;
	}

   /**
    * Applies server min/max scale as layer min/max scale.
    * The purpose is to update the TOC icon according with
    * the WMS layer visibilitiy. 
    */
   private void initServerScale() {
       /**
        * We have to calculate the equivalent scale for our system, taking into account the
        * server DPI and our real DPI.
        */
       double minScale = getCorrectedServerMinScale();
       double maxScale = getCorrectedServerMaxScale();
       if (minScale>0) {
           setMinScale(minScale);
       }
       if (maxScale>0 &&
               maxScale>minScale) {
           setMaxScale(maxScale);
       }
   }
	
	/**
    * Calculates the equivalent MinScaleDenominator for our system, taking into account the
    * server DPI and our real DPI.
    * 
    * WMS 1.3 standard defines a fixed pixel size of 0.28 mm for the server,
    * which may not match our real settings, so this conversion is required
    * to properly use MinScaleDenominator and MaxScaleDenominator values.
    *
    * @return
    */
   public double getCorrectedServerMinScale() {
       return (getServerMinScale()*MapContext.getScreenDPI())/WMS_DPI;
   }
   
   /**
    * Calculates the equivalent MaxScaleDenominator for our system, taking into account the
    * server DPI and our real DPI.
    * 
    * WMS 1.3 standard defines a fixed pixel size of 0.28 mm for the server,
    * which may not match our real settings, so this conversion is required
    * to properly use MinScaleDenominator and MaxScaleDenominator values.
    *
    * @return
    */
   public double getCorrectedServerMaxScale() {
       return (getServerMaxScale()*MapContext.getScreenDPI())/WMS_DPI;
   }
   
   /**
    * Returns the value of MinScaleDenominator tag, if available,
    * or -1 otherwise.
    *  
    * @return
    */
   public double getServerMinScale() {
       String[] layers = getLayerNames();
       double minScale=Double.MAX_VALUE;
       for (int i=0; i<layers.length; i++) {
           WMSLayerNode layer = wms.getLayer(layers[i]);
           double lyrMinScale = layer.getScaleMin(); 
           if ((lyrMinScale > 0) && (lyrMinScale<minScale)) {
               minScale = lyrMinScale;
           }
       }
       if (minScale==Double.MAX_VALUE) {
           return -1;
       }
       else {
           return minScale;
       }
   }

   /**
    * Returns the value of MaxScaleDenominator tag, if available,
    * or -1 otherwise.
    *  
    * @return
    */
   public double getServerMaxScale() {
       String[] layers = getLayerNames();
       double maxScale=-1, lyrMaxScale;
       for (int i=0; i<layers.length; i++) {
           WMSLayerNode layer = wms.getLayer(layers[i]);
           lyrMaxScale = layer.getScaleMax();
           if (lyrMaxScale!=-1
                   && lyrMaxScale>maxScale) {
               maxScale = lyrMaxScale;
           }
           
       }
       return maxScale;
   }
   
   
   /**
	 * Obtiene la extensión del fichero de georreferenciación
	 * @return String con la extensión del fichero de georreferenciación dependiendo
	 * del valor del formato obtenido del servidor. Por defecto asignaremos un .wld
	 */
	private String getExtensionWorldFile(){
		String extWorldFile = ".wld";
			if(m_Format.equals("image/tif") || m_Format.equals("image/tiff"))
				extWorldFile = ".tfw";
			//En la versión 1.6 de gdal no soporta jpgw si el fichero de imagen no 
			//tiene extensión. Solo lo lee si es wld
			/*if(m_Format.equals("image/jpeg"))
				extWorldFile = ".jpgw";*/
			return extWorldFile;
	}

	/**
	 * Calcula el contenido del fichero de georreferenciación de una imagen.
	 * @param bBox Tamaño y posición de la imagen (en coordenadas de usuario)
	 * @param sz Tamaño de la imagen en pixeles.
	 * @return el 'WorldFile', como String.
	 * @throws IOException
	 */
	public String getDataWorldFile(Rectangle2D bBox, Dimension sz) throws IOException {
		StringBuffer data = new StringBuffer();
			data.append((bBox.getMaxX() - bBox.getMinX())/(sz.getWidth() - 1)+"\n");
			data.append("0.0\n");
			data.append("0.0\n");
			data.append("-"+(bBox.getMaxY() - bBox.getMinY())/(sz.getHeight() - 1)+"\n");
			data.append(""+bBox.getMinX()+"\n");
			data.append(""+bBox.getMaxY()+"\n");
			return data.toString();
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#print(java.awt.Graphics2D,
	 * 		com.iver.cit.gvsig.fmap.ViewPort,
	 * 		com.iver.cit.gvsig.fmap.operations.Cancellable)
	 */
	public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel, double scale, PrintRequestAttributeSet properties)
		throws ReadDriverException {

		closeAndFree();

		if (isVisible() && isWithinScale(scale)) {
			isPrinting = true;
			if (!mustTilePrint) {
                draw(null, g, viewPort, cancel, scale, true);
			} else {
				// Para no pedir imagenes demasiado grandes, vamos
				// a hacer lo mismo que hace EcwFile: chunkear.
				// Llamamos a drawView con cuadraditos mï¿½s pequeï¿½os
				// del BufferedImage ni caso, cuando se imprime viene con null

				Tiling tiles = new Tiling(maxTilePrintWidth, maxTilePrintHeight, g.getClipBounds());
				tiles.setAffineTransform((AffineTransform) viewPort.getAffineTransform().clone());
				visualStatus.fileNames = new String[tiles.getNumTiles()];
				layerRaster = new FLyrRasterSE[tiles.getNumTiles()];
				for (int tileNr=0; tileNr < tiles.getNumTiles(); tileNr++) {
					// Parte que dibuja
					try {
						ViewPort vp = tiles.getTileViewPort(viewPort, tileNr);
                        drawTile(g, vp, cancel, tileNr, scale, tileNr, true);
					} catch (NoninvertibleTransformException e) {
						e.printStackTrace();
					} catch (LoadLayerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			isPrinting = false;
		}
	}

	public void _print(Graphics2D g, ViewPort viewPort, Cancellable cancel,double scale)
		throws ReadDriverException {
		draw(null, g, viewPort, cancel,scale);
	}

	/**
	 * Devuelve el FMapWMSDriver.
	 *
	 * @return FMapWMSDriver
	 *
	 * @throws IllegalStateException
	 * @throws ValidationException
	 * @throws UnsupportedVersionLayerException
	 * @throws IOException
	 */
	public FMapWMSDriver getDriver()
		throws IllegalStateException, ValidationException,
			UnsupportedVersionLayerException, IOException {
		return FMapWMSDriverFactory.getFMapDriverForURL(host);
	}

	/**
	 * Devuelve el FMapWMSDriver.
	 *
	 * @return FMapWMSDriver
	 *
	 * @throws IllegalStateException
	 * @throws ValidationException
	 * @throws UnsupportedVersionLayerException
	 * @throws IOException
	 */
	public void setDriver(FMapWMSDriver drv) {
		//TODO: Comprobar cambio
		wms = drv;
		initServerScale();
		this.updateDrawVersion();
	}

	/**
	 * Devuelve el URL.
	 *
	 * @return URL.
	 */
	public URL getHost() {
		return host;
	}

	/**
	 * Inserta el URL.
	 *
	 * @param host URL.
	 */
	public void setHost(URL host) {
		if (this.host == host){
			return;
		}
		if (this.host != null && this.fullExtent.equals(host)){
			return;
		}

		this.host = host;
		this.updateDrawVersion();
	}

	/**
	 * Devuelve la información de la consulta.
	 *
	 * @return String.
	 */
	public String getInfoLayerQuery() {
		return infoLayerQuery;
	}

	/**
	 * Inserta la informaciï¿½n de la consulta.
	 *
	 * @param infoLayerQuery String.
	 */
	public void setInfoLayerQuery(String infoLayerQuery) {
		this.infoLayerQuery = infoLayerQuery;
	}

	/**
	 * Devuelve la consulta.
	 *
	 * @return String.
	 */
	public String getLayerQuery() {
		return layerQuery;
	}

	/**
	 * Inserta la consulta.
	 *
	 * @param layerQuery consulta.
	 */
	public void setLayerQuery(String layerQuery) {
		if (this.layerQuery == layerQuery){
			return;
		}
		if (this.layerQuery != null && this.layerQuery.equals(layerQuery)){
			return;
		}

		this.layerQuery = layerQuery;
       wmsStatus.setLayerNames(Utilities.createVector(layerQuery,","));
		this.updateDrawVersion();
	}

	/**
	 * Devuelve el formato.
	 *
	 * @return Formato.
	 */
	public String getFormat() {
		return m_Format;
	}

	/**
	 * Inserta el formato.
	 *
	 * @param format Formato.
	 */
	public void setFormat(String format) {
		if (this.m_Format == format){
			return;
		}
		if (this.m_Format != null && this.m_Format.equals(format)){
			return;
		}
		m_Format = format;
		this.updateDrawVersion();
	}

	/**
	 * Devuelve el SRS.
	 *
	 * @return SRS.
	 */
	public String getSRS() {
		return m_SRS;
	}

	/**
	 * Inserta el SRS.
	 *
	 * @param m_srs SRS.
	 */
	public void setSRS(String m_srs) {
		if (m_SRS == m_srs){
			return;
		}
		if (m_SRS != null && m_SRS.equals(m_srs)){
			return;
		}
		m_SRS = m_srs;
		this.updateDrawVersion();
		setProjection(CRSFactory.getCRS(getSRS()));
	}

	/**
	 * Inserta la extensiï¿½n total de la capa.
	 *
	 * @param fullExtent Rectï¿½ngulo.
	 */
	public void setFullExtent(Rectangle2D fullExtent) {
		if (this.fullExtent == fullExtent){
			return;
		}
		if (this.fullExtent != null && this.fullExtent.equals(fullExtent)){
			return;
		}

		this.fullExtent = fullExtent;
		this.updateDrawVersion();
	}

	public HashMap getProperties() {
		HashMap info = new HashMap();
				String[] layerNames = getLayerQuery().split(",");
				Vector layers = new Vector(layerNames.length);
				try {
						if(getDriver().connect(null)){
								for (int i = 0; i < layerNames.length; i++) {
										layers.add(i, getDriver().getLayer(layerNames[i]));
								}
								info.put("name", getName());
								info.put("selectedLayers", layers);
								info.put("host", getHost());
								info.put("srs", getSRS());
								info.put("format", getFormat());
								info.put("wmsTransparency", new Boolean(wmsTransparency));
								info.put("styles", styles);
								info.put("dimensions", dimensions);
								info.put("fixedSize", fixedSize);
								return info;
						}
				} catch (Exception e) {
						e.printStackTrace();
				}
				return null;
	}

	public double getMaxX() {
		return visualStatus.maxX;
	}

	public double getMaxY() {
		return visualStatus.maxY;
	}

	public double getMinX() {
		return visualStatus.minX;
	}

	public double getMinY() {
		return visualStatus.minY;
	}

  
   /**
    * Returns the names of the WMS layers that are loaded in this
    * gvSIG layer.
    * 
    * @return An array containing the names of the WMS layers
    */
   public String[] getLayerNames() {
       Vector namesList = wmsStatus.getLayerNames();
       String[] names = new String[namesList.size()];
       for (int i=0; i<names.length; i++) {
           Object o = namesList.get(i);
           if (o instanceof String) {
               names[i] = (String) o;
           }
           else { // this should not happen
               names[i] = o.toString();
           }
       }
       return names;
   }

	/**
	 * @return Returns the wmsTransparency.
	 */
	public boolean isWmsTransparent() {
		return wmsTransparency;
	}

	/**
	 * @param wmsTransparency The wmsTransparency to set.
	 */
	public void setWmsTransparency(boolean wmsTransparency) {
		if (this.wmsTransparency == wmsTransparency){
			return;
		}
		this.wmsTransparency = wmsTransparency;
		this.updateDrawVersion();
	}

	/**
	 * @param styles
	 */
	public void setStyles(Vector styles) {
		if (this.styles == styles){
			return;
		}
		if (this.styles != null && styles != null ){
			if (this.styles.containsAll(styles) && this.styles.size() ==styles.size()){
				return;
			}
		}
		this.styles = styles;
		this.updateDrawVersion();
	}

	/**
	 * Sets the dimension vector that is a list of key-value pairs containing
	 * the name of the dimension and the value for it
	 * @param dimensions
	 */
	public void setDimensions(Vector dimensions) {
		if (this.dimensions == dimensions){
			return;
		}
		if (this.dimensions != null && dimensions != null ){
			if (this.dimensions.containsAll(dimensions) && this.dimensions.size() ==dimensions.size()){
				return;
			}
		}
		this.dimensions = dimensions;
		this.updateDrawVersion();
	}

	/**
	 * Sets the set of URLs that should be accessed for each operation performed
	 * to the server.
	 *
	 * @param onlineResources
	 */
	public void setOnlineResources(Hashtable onlineResources) {
		if (this.onlineResources == onlineResources){
			return;
		}
		if (this.onlineResources != null && this.onlineResources.equals(onlineResources)){
			return;
		}

		this.onlineResources = onlineResources;
		this.updateDrawVersion();
	}

	/**
	 * Gets the URL that should be accessed for an operation performed
	 * to the server.
	 *
	 * @param onlineResources
	 */
	public String getOnlineResource(String operation) {
		return ((String) onlineResources.get(operation));
	}

	/**
	 * When a server is not fully featured and it only can serve constant map
	 * sizes this value must be set. It expresses the size in pixels (width, height)
	 * that the map will be requested.
	 * @param Dimension sz
	 */
	public void setFixedSize(Dimension sz) {
		if (this.fixedSize == sz){
			return;
		}
		if (this.fixedSize != null && this.fixedSize.equals(sz)){
			return;
		}
		fixedSize = sz;
		this.updateDrawVersion();
	}

	/**
	 * Tells whether if this layer must deal with the server with the constant-size
	 * limitations or not.
	 * @return boolean.
	 */
	private boolean isSizeFixed() {
		return fixedSize != null && fixedSize.getWidth() > 0 && fixedSize.getHeight() > 0;
	}

	/**
	 * If it is true, this layer accepts GetFeatureInfo operations. This WMS operations
	 * maps to FMap's infoByPoint(p) operation.
	 * @param b
	 */
	public void setQueryable(boolean b) {
		queryable = b;
	}

	/**
	 * Creates the part of a OGC's MapContext document that would describe this
	 * layer(s).
	 * @param version, The desired version of the resulting document. (1.1.0)
	 * @return String containing the xml.
	 * @throws UnsupportedVersionLayerException
	 */
	public String toMapContext(String mapContextVersion) {
		XmlBuilder xml = new XmlBuilder();
		FMapWMSDriver drv;
		try {
			drv = getDriver();
		} catch (Exception e) {
			return xml.toString();
		}
		String[] layerNames = getLayerQuery().split(",");
		String[] styleNames = (String[]) styles.toArray(new String[0]);
		for (int i = 0; i < layerNames.length; i++) {
			WMSLayerNode layer = drv.getLayer(layerNames[i]);
			HashMap xmlAttrs = new HashMap();

			// <Layer>
			xmlAttrs.put(WebMapContextTags.HIDDEN, !isVisible()+"");
			xmlAttrs.put(WebMapContextTags.QUERYABLE, queryable+"");
			xml.openTag(WebMapContextTags.LAYER, xmlAttrs);
			xmlAttrs.clear();
			if (mapContextVersion.equals("1.1.0") || mapContextVersion.equals("1.0.0")) {
				// <Server>
				xmlAttrs.put(WebMapContextTags.SERVICE, WebMapContextTags.WMS);
				xmlAttrs.put(WebMapContextTags.VERSION, drv.getVersion());
				xmlAttrs.put(WebMapContextTags.SERVER_TITLE, drv.getServiceTitle());
				xml.openTag(WebMapContextTags.SERVER, xmlAttrs);
				xmlAttrs.clear();

					// <OnlineResource>
					xmlAttrs.put(WebMapContextTags.XLINK_TYPE, "simple");
					xmlAttrs.put(WebMapContextTags.XLINK_HREF, getHost().toString());
					xml.writeTag(WebMapContextTags.ONLINE_RESOURCE, xmlAttrs);
					xmlAttrs.clear();
					// </OnlineResource>

				xml.closeTag();
				// </Server>

				// <Name>
				xml.writeTag(WebMapContextTags.NAME, layer.getName().trim());
				// </Name>

				// <Title>
				xml.writeTag(WebMapContextTags.TITLE, layer.getTitle().trim());
				//?xml.writeTag(WebMapContextTags.TITLE, getName().trim());
				// </Title>

				// <Abstract>
				if (layer.getAbstract() != null)
					xml.writeTag(WebMapContextTags.ABSTRACT, layer.getAbstract());
				// </Abstract>

				// <SRS> (a list of available SRS for the enclosing layer)
				String[] strings = (String[]) layer.getAllSrs().toArray(new String[0]);
				String mySRS = strings[0];
				for (int j = 1; j < strings.length; j++) {
					mySRS += ","+strings[j];
				}
				xml.writeTag(WebMapContextTags.SRS, mySRS);
				// </SRS>

				// <FormatList>
				xml.openTag(WebMapContextTags.FORMAT_LIST);
					strings = (String[]) drv.getFormats().toArray(new String[0]);
					for (int j = 0; j < strings.length; j++) {
										// <Format>
						String str = strings[j].trim();
						if (str.equals(getFormat()))
							xml.writeTag(WebMapContextTags.FORMAT, str, WebMapContextTags.CURRENT, "1");
						else
							xml.writeTag(WebMapContextTags.FORMAT, str);
										// </Format>
					}
				xml.closeTag();
				// </FormatList>

				// <StyleList>
				xml.openTag(WebMapContextTags.STYLE_LIST);

					if (layer.getStyles().size()>0) {
						for (int j = 0; j < layer.getStyles().size(); j++) {
							// <Style>
							FMapWMSStyle st = (FMapWMSStyle) layer.getStyles().get(j);
							if (st.name.equals(styleNames[i]))
								xmlAttrs.put(WebMapContextTags.CURRENT, "1");
							xml.openTag(WebMapContextTags.STYLE, xmlAttrs);
							xmlAttrs.clear();

								// <Name>
								xml.writeTag(WebMapContextTags.NAME, st.name);
								// </Name>

								// <Title>
								xml.writeTag(WebMapContextTags.TITLE, st.title);
								// </Title>

								// <LegendURL width="180" format="image/gif" height="50">
									// <OnlineResource xlink:type="simple" xlink:href="http://globe.digitalearth.gov/globe/en/icons/colorbars/NATIONAL.gif"/>
									// </OnlineResource>
								// </LegendURL>
							xml.closeTag();
							// </Style>

						}

					} else {
						// Create fake style (for compatibility issues)
						xmlAttrs.put(WebMapContextTags.CURRENT, "1");
						// <Style>
						xml.openTag(WebMapContextTags.STYLE, xmlAttrs);
							xmlAttrs.clear();
							// <Name>
							xml.writeTag(WebMapContextTags.NAME, "default");
							// </Name>

							// <Title>
							xml.writeTag(WebMapContextTags.TITLE, "default");
							// </Title>

//							// <LegendURL width="180" format="image/gif" height="50">
//							xmlAttrs.put(WebMapContextTags.WIDTH, "0");
//							xmlAttrs.put(WebMapContextTags.HEIGHT, "0");
//							xmlAttrs.put(WebMapContextTags.FORMAT.toLowerCase(), "image/gif");
//							xml.openTag(WebMapContextTags.LEGEND_URL, xmlAttrs);
//							xmlAttrs.clear();
//								// <OnlineResource xlink:type="simple" xlink:href="http://globe.digitalearth.gov/globe/en/icons/colorbars/NATIONAL.gif"/>
//								xmlAttrs.put(WebMapContextTags.XLINK_TYPE, "simple");
//								xmlAttrs.put(WebMapContextTags.XLINK_HREF, "http://globe.digitalearth.gov/globe/en/icons/colorbars/NATIONAL.gif");
//								xml.writeTag(WebMapContextTags.ONLINE_RESOURCE, xmlAttrs);
//								// </OnlineResource>
//						    // </LegendURL>
//							xml.closeTag();
						// </Style>
						xml.closeTag();
					}
				// </StyleList>
				xml.closeTag();
				if (mapContextVersion.compareTo("1.0.0") > 0) {
				// <DimensionList>
					xml.openTag(WebMapContextTags.DIMENSION_LIST);
					// <Dimension>
					// </Dimension>
					xml.closeTag();
				// </DimensionList>
				}
			} else {
				xml.writeTag("ERROR", PluginServices.getText(this, "unsupported_map_context_version"));
			}
			// </Layer>
			xml.closeTag();
		}
		return xml.getXML();
	}

	public ImageIcon getTocImageIcon() {
		return new ImageIcon(getClass().getResource("image/icon-layer-wms.png"));
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.RasterOperations#getTileSize()
	 */
	public int[] getTileSize() {
		int[] size = {maxTileDrawWidth, maxTileDrawHeight};
		return size;
	}

	/*
	 *  (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.RasterOperations#isTiled()
	 */
	public boolean isTiled() {
		return mustTileDraw;
	}

	public Image getImageLegend() {
		try {
			if (wms == null)
				wms = getDriver();
			if (wms.hasLegendUrl(wmsStatus,layerQuery) ||wms.hasLegendGraphic() || hasImageLegend) {
				wmsStatus.setOnlineResource((String) onlineResources
						.get("GetLegendGraphic"));
				String path = getPathImage();// File legend =
												// getDriver().getLegendGraphic(wmsStatus,
												// layerQuery, null);
				Image img = null;
				if ((path != null) && (path.length() > 0))
					img = new ImageIcon(path).getImage();
				return img;
			}
		} catch (Exception e) {
		}
		return null;
	}

	public String getPathImage() {
		try {
			File legend = getDriver().getLegendGraphic(wmsStatus, layerQuery, null);
			return legend.getAbsolutePath();
		}catch(Exception e){
			e.printStackTrace();
			return null;
		}
	}

	/* (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLayer#newComposedLayer()
	 */
	public ComposedLayer newComposedLayer() {
		Preferences prefs = Preferences.userRoot().node("gvsig.wms");

		/*
		 * from java.util.prefs import Preferences
		 * prefs = Preferences.userRoot().node("gvsig.wms")
		 * prefs.put("useComposedLayer","true")
		 */

		String str = prefs.get("useComposedLayer","false");
		Boolean useComposedLayer = Boolean.TRUE; // por defecto ya se usan
		try {
			useComposedLayer = Boolean.valueOf(str);
		} catch (Exception e) {

		}
		if (useComposedLayer.booleanValue()) {
			return new ComposedLayerWMS();
		} else {
			return null;
		}
	}

	/**
	 * @param styles
	 */
	public Vector getStyles() {
		return this.styles;
	}


	/*
	 * Checks if can make a single petition for the two layers to the server
	 * @see com.iver.cit.gvsig.fmap.layers.ComposedLayerWMS#canAdd(com.iver.cit.gvsig.fmap.layers.FLayer)
	 */
	boolean isComposedLayerCompatible(FLayer layer) {
		FLyrWMS aLayer;

		if (!(layer instanceof FLyrWMS)) {
			return false;
		}
		aLayer = (FLyrWMS)layer;
		if (!this.getHost().equals(aLayer.getHost())) {
			return false;
		}
		if (!this.getFormat().equals(aLayer.getFormat())) {
			return false;
		}
		if (!this.getSRS().equals(aLayer.getSRS())) {
			return false;
		}
		if (this.getInfoLayerQuery() != null) {
			if (!this.getInfoLayerQuery().equals(aLayer.getInfoLayerQuery())) {
				return false;
			}
		}else if (aLayer.getInfoLayerQuery() != null) {
			return false;
		}


		// isFixedSize es privado
		if ((this.fixedSize != null) &&
				(aLayer.fixedSize!= null)) {
			if (this.fixedSize.equals(aLayer.fixedSize)) {
				return false;
			}
		} else if ((this.fixedSize != null) != (aLayer.fixedSize != null)) {
			return false;
		}

		// time elevation (dimensions)
		if ((this.dimensions != null) &&
				(aLayer.dimensions != null)) {
			if (this.dimensions.size() != aLayer.dimensions.size()) {
				return false;
			} else {
				Iterator iter = this.dimensions.iterator();
				while (iter.hasNext()) {
					if (!aLayer.dimensions.contains(iter.next())) {
						return false;
					}
				}
			}

		} else if ((this.dimensions != null) != (aLayer.dimensions != null)) {
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.layers.FLyrRasterSE#isActionEnabled(int)
	 */
	public boolean isActionEnabled(int action) {
		switch (action) {
			case IRasterLayerActions.ZOOM_PIXEL_RESOLUTION:
			case IRasterLayerActions.FLYRASTER_BAR_TOOLS:
			case IRasterLayerActions.BANDS_FILE_LIST:
			case IRasterLayerActions.COLOR_TABLE:
			case IRasterLayerActions.GEOLOCATION:
			case IRasterLayerActions.PANSHARPENING:
			case IRasterLayerActions.SAVE_COLORINTERP:
				return false;
			case IRasterLayerActions.REMOTE_ACTIONS:
				return true;
		}

		return super.isActionEnabled(action);
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.layers.FLyrRasterSE#getLegend()
	 */
	public ILegend getLegend() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.IRasterOperations#getDatatype()
	 */
	public int[] getDataType(){
		try {
			return dataset.getDataType();
		} catch (NullPointerException e) {
			if(layerRaster != null && layerRaster[0] != null)
				return layerRaster[0].getDataType();
			else
				return new int[]{IBuffer.TYPE_UNDEFINED};
		}
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.layers.FLyrRasterSE#overviewsSupport()
	 */
	public boolean overviewsSupport() {
		return false;
	}

	protected void updateDrawVersion() {
		if (this.disableUpdateDrawVersion != null){

			Thread curThread = Thread.currentThread();

			Thread aThread;

			Iterator iter = this.disableUpdateDrawVersion.iterator();
			while (iter.hasNext()){
				aThread = (Thread) ((WeakReference)iter.next()).get();
				if (aThread == null){
					iter.remove();
				} else if(aThread.equals(curThread)){
					return;
				}
			}
		}
//		Exception ex = new Exception();
//		ex.printStackTrace();
		super.updateDrawVersion();
	}

	protected void disableUpdateDrawVersion(){
		if (this.disableUpdateDrawVersion == null){
			this.disableUpdateDrawVersion = new ArrayList();
		}
		this.disableUpdateDrawVersion.add(new WeakReference(Thread.currentThread()));
	}

	protected void enableUpdateDrawVersion(){
		if (this.disableUpdateDrawVersion == null){
			return;
		}

		Thread curThread = Thread.currentThread();

		Thread aThread;


		Iterator iter = this.disableUpdateDrawVersion.iterator();
		while (iter.hasNext()){
			aThread = (Thread) ((WeakReference)iter.next()).get();
			if (aThread == null){
				iter.remove();
			} else if(aThread.equals(curThread)){
				iter.remove();
				break;
			}
		}

	}
	
	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.fmap.layers.FLyrDefault#cloneLayer()
	 */
	public FLayer cloneLayer() throws Exception {
		FLyrWMS layer = new FLyrWMS();
		layer.setHost(this.getHost());
		layer.setName(this.getName());
		layer.setSRS(this.getSRS());
		layer.setFormat(this.getFormat());
		layer.setFullExtent(this.fullExtent);
		layer.setDriver(this.getDriver());
        layer.setStyles(getStyles());
        layer.setOnlineResources(this.onlineResources);
        layer.setFixedSize(this.fixedSize);
        layer.setQueryable(this.queryable);
        layer.setWmsTransparency(this.wmsTransparency);
        layer.setDimensions(this.dimensions);
        layer.setLayerQuery(this.layerQuery);
        layer.setInfoLayerQuery(this.infoLayerQuery);
        
		ArrayList filters = null;
		if(layer.getRender().getFilterList() == null) {
			layer.getRender().setFilterList(new RasterFilterList());
			filters = layer.getRender().getFilterList().getStatusCloned();
		}
		else
			filters = getRender().getFilterList().getStatusCloned();
		layer.getRender().getFilterList().setStatus(filters);
		
		return layer;
	}

	/*
	 * (non-Javadoc)
	 * @see org.gvsig.fmap.raster.layers.FLyrRasterSE#getFileLayer()
	 */
	public FLayer getFileLayer() {
		if(layerRaster != null && layerRaster[0] != null) {
			FLyrRasterSE ly = null;
			if(lastNColumns == 0 && lastNRows == 0) {
				try {
					ly = createLayer(layerRaster[0].getName(), layerRaster[0].getLoadParams(), layerRaster[0].getProjection());
				} catch (LoadLayerException e) {
					return null;
				}
			} else {

				String[][] s = new String[lastNRows][lastNColumns];

				for (int i = 0; i < s.length; i++) {
					for (int j = 0; j < s[0].length; j++) {
						s[i][j] = ((FLyrRasterSE)layerRaster[i]).getDataSource().getNameDatasetStringList(i, j)[0];
					}
				}
				try {
					ly = createLayer("preview", s, getProjection());
				} catch (LoadLayerException e) {
					return null;
				}
			}
			return ly;
		}
		return null;
	}

	/**
	 * Try to guess the axis orientation from the CRS WKT definition,
	 * as there is no good way to do this using libProjection API.
	 * 
	 * @return One of @link {@link WMSStatus#CRS_AXIS_EAST_NORTH},
	 * {@link WMSStatus#CRS_AXIS_NORTH_EAST}, {@link WMSStatus#CRS_AXIS_SOUTH_WEST},
	 * {@link WMSStatus#CRS_AXIS_WEST_SOUTH}, {@link WMSStatus#CRS_AXIS_OTHER_OR_UNKNOWN}.
	 */
	private int guessAxisOrientation() {
		if (this.getProjection()!=null) {
			String wkt = this.getProjection().getWKT();
			if (wkt!=null) {
				String wktup = wkt.toUpperCase();
				Pattern p = Pattern.compile(".*AXIS\\[(.*)\\]\\s*,\\s*AXIS\\[(.*)\\].*", Pattern.DOTALL);
				Matcher m = p.matcher(wkt);
				if (m.matches()) {
					String firstAxis = m.group(1);
					String secondAxis = m.group(2);
					if (firstAxis.contains("EAST")
							&& secondAxis.contains("NORTH")) {
						return WMSStatus.CRS_AXIS_EAST_NORTH;
					}
					else if (firstAxis.contains("NORTH")
							&& secondAxis.contains("EAST")) {
						return WMSStatus.CRS_AXIS_NORTH_EAST;
					}
					else if (firstAxis.contains("WEST")
							&& secondAxis.contains("SOUTH")) {
						return WMSStatus.CRS_AXIS_WEST_SOUTH;
					}
					else if (firstAxis.contains("SOUTH")
							&& secondAxis.contains("WEST")) {
						return WMSStatus.CRS_AXIS_SOUTH_WEST;
					}
				}
			}
		}
		return WMSStatus.CRS_AXIS_OTHER_OR_UNKNOWN;
	}
	
	public void setProjection(IProjection proj) {
		super.setProjection(proj);
		cachedAxisOrientation = guessAxisOrientation();
	}
}
