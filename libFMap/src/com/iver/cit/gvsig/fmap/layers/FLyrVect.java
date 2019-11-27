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

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;

import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.gvsig.tools.file.PathGenerator;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.data.NoSuchTableException;
import com.hardcode.gdbms.engine.data.driver.DriverException;
import com.hardcode.gdbms.engine.instruction.FieldNotFoundException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.layers.LegendLayerException;
import com.iver.cit.gvsig.exceptions.layers.ReloadLayerException;
import com.iver.cit.gvsig.exceptions.layers.ReprojectLayerException;
import com.iver.cit.gvsig.exceptions.layers.StartEditionLayerException;
import com.iver.cit.gvsig.exceptions.visitors.StartWriterVisitorException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ILabelable;
import com.iver.cit.gvsig.fmap.core.IRow;
import com.iver.cit.gvsig.fmap.core.symbols.IMultiLayerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.core.v02.FConverter;
import com.iver.cit.gvsig.fmap.core.v02.FSymbol;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.drivers.WithDefaultLegend;
import com.iver.cit.gvsig.fmap.drivers.featureiterators.JoinFeatureIterator;
import com.iver.cit.gvsig.fmap.edition.AfterFieldEditEvent;
import com.iver.cit.gvsig.fmap.edition.AfterRowEditEvent;
import com.iver.cit.gvsig.fmap.edition.AnnotationEditableAdapter;
import com.iver.cit.gvsig.fmap.edition.BeforeFieldEditEvent;
import com.iver.cit.gvsig.fmap.edition.BeforeRowEditEvent;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.edition.IEditionListener;
import com.iver.cit.gvsig.fmap.edition.ISpatialWriter;
import com.iver.cit.gvsig.fmap.edition.IWriteable;
import com.iver.cit.gvsig.fmap.edition.IWriter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableAdapter;
import com.iver.cit.gvsig.fmap.edition.VectorialEditableDBAdapter;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ClassifiableVectorial;
import com.iver.cit.gvsig.fmap.layers.layerOperations.InfoByPoint;
import com.iver.cit.gvsig.fmap.layers.layerOperations.RandomVectorialData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.SingleLayer;
import com.iver.cit.gvsig.fmap.layers.layerOperations.VectorialData;
import com.iver.cit.gvsig.fmap.layers.layerOperations.VectorialXMLItem;
import com.iver.cit.gvsig.fmap.layers.layerOperations.XMLItem;
import com.iver.cit.gvsig.fmap.operations.strategies.FeatureVisitor;
import com.iver.cit.gvsig.fmap.operations.strategies.Strategy;
import com.iver.cit.gvsig.fmap.operations.strategies.StrategyManager;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.ILegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.rendering.LegendClearEvent;
import com.iver.cit.gvsig.fmap.rendering.LegendContentsChangedListener;
import com.iver.cit.gvsig.fmap.rendering.LegendFactory;
import com.iver.cit.gvsig.fmap.rendering.SingleSymbolLegend;
import com.iver.cit.gvsig.fmap.rendering.SymbolLegendEvent;
import com.iver.cit.gvsig.fmap.rendering.ZSort;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.AttrInTableLabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelingFactory;
import com.iver.cit.gvsig.fmap.spatialindex.IPersistentSpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;
import com.iver.cit.gvsig.fmap.spatialindex.QuadtreeGt2;
import com.iver.cit.gvsig.fmap.spatialindex.QuadtreeJts;
import com.iver.cit.gvsig.fmap.spatialindex.SpatialIndexException;
import com.iver.utiles.FileUtils;
import com.iver.utiles.IPersistence;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.PostProcessSupport;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;
import com.iver.utiles.swing.threads.CancellableMonitorable;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.TopologyException;

/**
 * Capa b�sica Vectorial.
 *
 * @author Fernando Gonz�lez Cort�s
 */

public class FLyrVect extends FLyrDefault implements ILabelable,
        ClassifiableVectorial, SingleLayer, VectorialData, RandomVectorialData,
        AlphanumericData, InfoByPoint, SelectionListener, IEditionListener, LegendContentsChangedListener {

	private static Logger logger = Logger.getLogger(FLyrVect.class.getName());

    /** Leyenda de la capa vectorial */
    private IVectorLegend legend;
    private int typeShape = -1;
    private ReadableVectorial source;
    private SelectableDataSource sds;
    private SpatialCache spatialCache = new SpatialCache();
    private boolean spatialCacheEnabled = false;

    /**
     * An implementation of gvSIG spatial index
     */
    protected ISpatialIndex spatialIndex = null;
    private boolean bHasJoin = false;
    private XMLEntity orgXMLEntity = null;
    private XMLEntity loadSelection = null;
    private IVectorLegend loadLegend = null;

    //Lo a�ado. Caracter�sticas de HyperEnlace (LINK)
    private FLyrVectLinkProperties linkProperties=new FLyrVectLinkProperties();
    //private ArrayList linkProperties=null;
    private boolean waitTodraw=false;
    private static PathGenerator pathGenerator=PathGenerator.getInstance();

    public boolean isWaitTodraw() {
		return waitTodraw;
	}

	public void setWaitTodraw(boolean waitTodraw) {
		this.waitTodraw = waitTodraw;
	}
    /**
     * Devuelve el VectorialAdapater de la capa.
     *
     * @return VectorialAdapter.
     */
    public ReadableVectorial getSource() {
        if (!this.isAvailable()) return null;
        return source;
    }

    /**
     * If we use a persistent spatial index associated with this layer, and the
     * index is not intrisic to the layer (for example spatial databases) this
     * method looks for existent spatial index, and loads it.
     *
     */
    private void loadSpatialIndex() {
        //FIXME: Al abrir el indice en fichero...
        //�C�mo lo liberamos? un metodo Layer.shutdown()


        ReadableVectorial source = getSource();
        //REVISAR QUE PASA CON LOS DRIVERS DXF, DGN, etc.
        //PUES SON VECTORIALFILEADAPTER
        if (!(source instanceof VectorialFileAdapter)) {
            // we are not interested in db adapters
            return;
        }
        VectorialDriver driver = source.getDriver();
        if (!(driver instanceof BoundedShapes)) {
            // we dont spatially index layers that are not bounded
            return;
        }
        File file = ((VectorialFileAdapter) source).getFile();
        String fileName = file.getAbsolutePath();
        File sptFile = new File(fileName + ".qix");
        if (!sptFile.exists() || (!(sptFile.length() > 0))) {
            // before to exit, look for it in temp path
            String tempPath = System.getProperty("java.io.tmpdir");
            fileName = tempPath + File.separator + sptFile.getName();
            sptFile = new File(fileName);
            // it doesnt exists, must to create
            if (!sptFile.exists() || (!(sptFile.length() > 0))) {
                return;
            }// if
        }// if

        try {
            source.start();
            spatialIndex = new QuadtreeGt2(FileUtils.getFileWithoutExtension(sptFile),
                    "NM", source.getFullExtent(), source.getShapeCount(), false);
            source.setSpatialIndex(spatialIndex);
        } catch (SpatialIndexException e) {
            spatialIndex = null;
            e.printStackTrace();
            return;
        } catch (ReadDriverException e) {
            spatialIndex = null;
            e.printStackTrace();
            return;
        }

    }

    /**
     * Checks if it has associated an external spatial index
     * (an spatial index file).
     *
     * It looks for it in main file path, or in temp system path.
     * If main file is rivers.shp, it looks for a file called
     * rivers.shp.qix.

     * @return
     */
    public boolean isExternallySpatiallyIndexed() {
        /*
         * FIXME (AZABALA): Independizar del tipo de fichero de �ndice
          * con el que se trabaje (ahora mismo considera la extension .qix,
         * pero esto depender� del tipo de �ndice)
         * */
        ReadableVectorial source = getSource();
        if (!(source instanceof VectorialFileAdapter)) {
            // we are not interested in db adapters.
            // think in non spatial dbs, like HSQLDB
            return false;
        }
        File file = ((VectorialFileAdapter) source).getFile();
        String fileName = file.getAbsolutePath();
        File sptFile = new File(fileName + ".qix");
        if (!sptFile.exists() || (!(sptFile.length() > 0))) {
            // before to exit, look for it in temp path
            // it doesnt exists, must to create
            String tempPath = System.getProperty("java.io.tmpdir");
            fileName = tempPath + File.separator + sptFile.getName();
            sptFile = new File(fileName);
            if (!sptFile.exists() || (!(sptFile.length() > 0))) {
                return false;
            }// if
        }// if
        return true;
    }

    /**
     * Inserta el VectorialAdapter a la capa.
     *
     * @param va
     *            VectorialAdapter.
     */
    public void setSource(ReadableVectorial rv) {
        source = rv;
        // azabala: we check if this layer could have a file spatial index
        // and load it if it exists
        loadSpatialIndex();
    }

    public Rectangle2D getFullExtent() throws ReadDriverException, ExpansionFileReadException {
            Rectangle2D rAux;
            source.start();
            rAux = (Rectangle2D)source.getFullExtent().clone();
            source.stop();

            // Si existe reproyecci�n, reproyectar el extent
            if (!(this.getProjection()!=null &&
            		this.getMapContext().getProjection()!=null &&
            		this.getProjection().getAbrev().equals(this.getMapContext().getProjection().getAbrev()))){
            	ICoordTrans ct = getCoordTrans();
            	try{
            		if (ct != null) {
            			Point2D pt1 = new Point2D.Double(rAux.getMinX(), rAux.getMinY());
            			Point2D pt2 = new Point2D.Double(rAux.getMaxX(), rAux.getMaxY());
            			pt1 = ct.convert(pt1, null);
            			pt2 = ct.convert(pt2, null);
            			rAux = new Rectangle2D.Double();
            			rAux.setFrameFromDiagonal(pt1, pt2);
            		}
            	}catch (IllegalStateException e) {
            		this.setAvailable(false);
            		this.addError(new ReprojectLayerException(getName(), e));
            	}
            }
            //Esto es para cuando se crea una capa nueva con el fullExtent de ancho y alto 0.
            if (rAux.getWidth()==0 && rAux.getHeight()==0) {
                rAux=new Rectangle2D.Double(0,0,100,100);
            }

            return rAux;
    }

    /**
     * Draws using IFeatureIterator. This method will replace the old draw(...) one.
     * @autor jaume dominguez faus - jaume.dominguez@iver.es
     * @param image
     * @param g
     * @param viewPort
     * @param cancel
     * @param scale
     * @throws ReadDriverException
     */
    private void _draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
    		Cancellable cancel, double scale) throws ReadDriverException {
    	boolean bDrawShapes = true;
    	if (legend instanceof SingleSymbolLegend) {
    		bDrawShapes = legend.getDefaultSymbol().isShapeVisible();
    	}
    	Point2D offset = viewPort.getOffset();
    	double dpi = MapContext.getScreenDPI();



    	if (bDrawShapes) {
    		boolean cacheFeatures = isSpatialCacheEnabled();
    		SpatialCache cache = null;
    		if (cacheFeatures) {
    			getSpatialCache().clearAll();
    			cache = getSpatialCache();
    		}

    		try {
    			ArrayList<String> fieldList = new ArrayList<String>();

    			// fields from legend
    			String[] aux = null;

    			if (legend instanceof IClassifiedVectorLegend) {
    				aux = ((IClassifiedVectorLegend) legend).getClassifyingFieldNames();
    				if (aux!=null) {
    					for (int i = 0; i < aux.length; i++) {
    						// check fields exists
    						if (sds.getFieldIndexByName(aux[i]) == -1) {
    							logger.warn("Error en leyenda de " + getName() +
    									". El campo " + aux[i] + " no est�.");
    							legend = LegendFactory.createSingleSymbolLegend(getShapeType());
    							break;
    						}
    						fieldList.add(aux[i]);
    					}
    				}
    			}
    			// Get the iterator over the visible features
    			IFeatureIterator it = null;
    			if (isJoined()) {
    				it = new JoinFeatureIterator(this, viewPort,
    						fieldList.toArray(new String[fieldList.size()]));
    			}
    			else {
    				ReadableVectorial rv=getSource();
//    				rv.start();
    				it = rv.getFeatureIterator(
    					viewPort.getAdjustedExtent(),
    					fieldList.toArray(new String[fieldList.size()]),
    					viewPort.getProjection(),
    					true);
//    				rv.stop();
    			}

				ZSort zSort = ((IVectorLegend) getLegend()).getZSort();

    			boolean bSymbolLevelError = false;

    			// if layer has map levels it will use a ZSort
    			boolean useZSort = zSort != null && zSort.isUsingZSort();

    			// -- visual FX stuff
    			long time = System.currentTimeMillis();
    			BufferedImage virtualBim;
    			Graphics2D virtualGraphics;

    			// render temporary map each screenRefreshRate milliseconds;
    			int screenRefreshDelay = (int) ((1D/MapControl.getDrawFrameRate())*3*1000);
    			BufferedImage[] imageLevels = null;
    			Graphics2D[] graphics = null;
    			if (useZSort) {
    				imageLevels = new BufferedImage[zSort.getLevelCount()];
    				graphics = new Graphics2D[imageLevels.length];
    				for (int i = 0; !cancel.isCanceled() && i < imageLevels.length; i++) {
    					imageLevels[i] = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
    					graphics[i] = imageLevels[i].createGraphics();
    					graphics[i].setTransform(g.getTransform());
    					graphics[i].setRenderingHints(g.getRenderingHints());
    				}
    			}
    			// -- end visual FX stuff

    			boolean isInMemory = false;
    			if (getSource().getDriverAttributes() != null){
    				isInMemory = getSource().getDriverAttributes().isLoadedInMemory();
    			}
    			SelectionSupport selectionSupport=getSelectionSupport();
    			// Iteration over each feature
    			while ( !cancel.isCanceled() && it.hasNext()) {
    				IFeature feat = it.next();
    				IGeometry geom = null;

    				if (isInMemory){
    					geom = feat.getGeometry().cloneGeometry();
    				}else{
    					geom = feat.getGeometry();
    				}

    				if (cacheFeatures) {
    					if (cache.getMaxFeatures() >= cache.size()) {
    						// already reprojected
    						cache.insert(geom.getBounds2D(), geom);
    					}
    				}

    				// retrieve the symbol associated to such feature
    				ISymbol sym = legend.getSymbolByFeature(feat);

    				if (sym == null) continue;

    				//C�digo para poder acceder a los �ndices para ver si est� seleccionado un Feature
    				ReadableVectorial rv=getSource();
    				int selectionIndex=-1;
    				if (rv instanceof ISpatialDB){
    					selectionIndex = ((ISpatialDB)rv).getRowIndexByFID(feat);
    				}else{
    					selectionIndex = Integer.parseInt(feat.getID());
    				}
    				if (selectionIndex!=-1) {
    					if (selectionSupport.isSelected(selectionIndex)) {
    						sym = sym.getSymbolForSelection();
    					}
    				}

    				// Check if this symbol is sized with CartographicSupport
    				CartographicSupport csSym = null;
    				int symbolType = sym.getSymbolType();
    				boolean bDrawCartographicSupport = false;

    				if (   symbolType == FShape.POINT
    						|| symbolType == FShape.LINE
    						|| sym instanceof CartographicSupport) {

    					// patch
    					if (!sym.getClass().equals(FSymbol.class)) {
    						csSym = (CartographicSupport) sym;
    						bDrawCartographicSupport = (csSym.getUnit() != -1);
    					}
    				}

    				int x = -1;
    				int y = -1;
    				int[] xyCoords = new int[2];

    				// Check if size is a pixel
    				boolean onePoint = bDrawCartographicSupport ?
    						isOnePoint(g.getTransform(), viewPort, MapContext.getScreenDPI(), csSym, geom, xyCoords) :
    							isOnePoint(g.getTransform(), viewPort, geom, xyCoords);

    						// Avoid out of bounds exceptions
    						if (onePoint) {
    							x = xyCoords[0];
    							y = xyCoords[1];
    							if (x<0 || y<0 || x>= viewPort.getImageWidth() || y>=viewPort.getImageHeight()) continue;
    						}

    						if (useZSort) {
    							// Check if this symbol is a multilayer
								int[] symLevels = zSort.getLevels(sym);
    							if (sym instanceof IMultiLayerSymbol) {
    								// if so, treat each of its layers as a single symbol
    								// in its corresponding map level
    								IMultiLayerSymbol mlSym = (IMultiLayerSymbol) sym;
    								for (int i = 0; !cancel.isCanceled() && i < mlSym.getLayerCount(); i++) {
    									ISymbol mySym = mlSym.getLayer(i);
        								int symbolLevel = 0;
        								if (symLevels != null) {
        									symbolLevel = symLevels[i];
        								} else {
    										/* an error occured when managing symbol levels.
    										 * some of the legend changed events regarding the
    										 * symbols did not finish satisfactory and the legend
    										 * is now inconsistent. For this drawing, it will finish
    										 * as it was at the bottom (level 0) but, when done, the
    										 * ZSort will be reset to avoid app crashes. This is
    										 * a bug that has to be fixed.
    										 */
    										bSymbolLevelError = true;
    									}

    									if (onePoint) {
    										if (x<0 || y<0 || x>= imageLevels[symbolLevel].getWidth() || y>=imageLevels[symbolLevel].getHeight()) continue;
    										imageLevels[symbolLevel].setRGB(x, y, mySym.getOnePointRgb());
    									} else {
    										if (!bDrawCartographicSupport) {
    											geom.drawInts(graphics[symbolLevel], viewPort, mySym, cancel);
    										} else {
    											geom.drawInts(graphics[symbolLevel], viewPort, dpi, (CartographicSupport) mySym, cancel);
    										}
    									}
    								}
    							} else {
    								// else, just draw the symbol in its level
    								int symbolLevel = 0;
    								if (symLevels != null) {

    									symbolLevel=symLevels[0];
    								} else {
    									/* If symLevels == null
    									 * an error occured when managing symbol levels.
    									 * some of the legend changed events regarding the
    									 * symbols did not finish satisfactory and the legend
    									 * is now inconsistent. For this drawing, it will finish
    									 * as it was at the bottom (level 0). This is
    									 * a bug that has to be fixed.
    									 */
//    									bSymbolLevelError = true;
    								}

    								if (!bDrawCartographicSupport) {
    									geom.drawInts(graphics[symbolLevel], viewPort, sym, cancel);
    								} else {
    									geom.drawInts(graphics[symbolLevel], viewPort, dpi, csSym, cancel);
    								}
    							}

    							// -- visual FX stuff
    							// Cuando el offset!=0 se est� dibujando sobre el Layout y por tanto no tiene que ejecutar el siguiente c�digo.
    							if (offset.getX()==0 && offset.getY()==0)
    								if ((System.currentTimeMillis() - time) > screenRefreshDelay) {
    									virtualBim = new BufferedImage(image.getWidth(),image.getHeight(),BufferedImage.TYPE_INT_ARGB);
    									virtualGraphics = virtualBim.createGraphics();
    									virtualGraphics.drawImage(image,0,0, null);
    									for (int i = 0; !cancel.isCanceled() && i < imageLevels.length; i++) {
    										virtualGraphics.drawImage(imageLevels[i],0,0, null);
    									}
    									g.clearRect(0, 0, image.getWidth(), image.getHeight());
    									g.drawImage(virtualBim, 0, 0, null);
    									time = System.currentTimeMillis();
    								}
    							// -- end visual FX stuff

    						} else {
    							// no ZSort, so there is only a map level, symbols are
    							// just drawn.
    							if (onePoint) {
    								if (x<0 || y<0 || x>= image.getWidth() || y>=image.getHeight()) continue;
    								image.setRGB(x, y, sym.getOnePointRgb());
    							} else {
                                                                if (!bDrawCartographicSupport) {
                                                                    geom.drawInts(g, viewPort, sym, cancel);
                                                                } else {
                                                                    geom.drawInts(g, viewPort, dpi, csSym,
                                                                            cancel);
                                                                }
                                                        }
    						}
    			}

    			if (useZSort) {
    				g.drawImage(image, (int)offset.getX(), (int)offset.getY(), null);
    				g.translate(offset.getX(), offset.getY());
    				for (int i = 0; !cancel.isCanceled() && i < imageLevels.length; i++) {
    					g.drawImage(imageLevels[i],0,0, null);
    					imageLevels[i] = null;
    					graphics[i] = null;
    				}
    				g.translate(-offset.getX(), -offset.getY());
    				imageLevels = null;
    				graphics = null;
    			}
    			it.closeIterator();

    			if (bSymbolLevelError) {
    				((IVectorLegend) getLegend()).setZSort(null);
    			}

    		} catch (ReadDriverException e) {
    			this.setVisible(false);
    			this.setActive(false);
    			throw e;
    		}


    	}
    }

   	public void draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
            Cancellable cancel, double scale) throws ReadDriverException {
   		if (isWaitTodraw()) {
			return;
		}
   		if (getStrategy() != null) {
   			getStrategy().draw(image, g, viewPort, cancel);
   		}
   		else {
   			_draw(image, g, viewPort, cancel, scale);
   		}
    }

    public void _print(Graphics2D g, ViewPort viewPort, Cancellable cancel,
    		double scale, PrintRequestAttributeSet properties, boolean highlight) throws ReadDriverException {
    	boolean bDrawShapes = true;
        boolean cutGeom = true;
    	if (legend instanceof SingleSymbolLegend) {
    		bDrawShapes = legend.getDefaultSymbol().isShapeVisible();
    	}


    	if (bDrawShapes) {

    		try {
    			double dpi = 72;

    			PrintQuality resolution=(PrintQuality)properties.get(PrintQuality.class);
    			if (resolution.equals(PrintQuality.NORMAL)){
    				dpi = 300;
    			} else if (resolution.equals(PrintQuality.HIGH)){
    				dpi = 600;
    			} else if (resolution.equals(PrintQuality.DRAFT)){
    				dpi = 72;
    			}
    			ArrayList<String> fieldList = new ArrayList<String>();
    			String[] aux;

    			// fields from legend
    			if (legend instanceof IClassifiedVectorLegend) {
    				aux = ((IClassifiedVectorLegend) legend).getClassifyingFieldNames();
    				for (int i = 0; i < aux.length; i++) {
    					fieldList.add(aux[i]);
    				}
    			}
    			//
    			//    			// fields from labeling
    			//    			if (isLabeled()) {
    			//    				aux = getLabelingStrategy().getUsedFields();
    			//    				for (int i = 0; i < aux.length; i++) {
    			//    					fieldList.add(aux[i]);
    			//    				}
    			//    			}

    			ZSort zSort = ((IVectorLegend) getLegend()).getZSort();

    			// if layer has map levels it will use a ZSort
    			boolean useZSort = zSort != null && zSort.isUsingZSort();


    			int mapLevelCount = (useZSort) ? zSort.getLevelCount() : 1;
    			for (int mapPass = 0; mapPass < mapLevelCount; mapPass++) {
    				// Get the iterator over the visible features
    				//    				IFeatureIterator it = getSource().getFeatureIterator(
    				//    						viewPort.getAdjustedExtent(),
    				//    						fieldList.toArray(new String[fieldList.size()]),
    				//    						viewPort.getProjection(),
    				//    						true);
    				IFeatureIterator it = null;
    				if (isJoined()) {
    					it = new JoinFeatureIterator(this, viewPort,
    							fieldList.toArray(new String[fieldList.size()]));
    				}
    				else {
    					it = getSource().getFeatureIterator(
    							viewPort.getAdjustedExtent(),
    							fieldList.toArray(new String[fieldList.size()]),
    							viewPort.getProjection(),
    							true);
    				}

    				// Iteration over each feature
    				while ( !cancel.isCanceled() && it.hasNext()) {
    					IFeature feat = it.next();
    					IGeometry geom = feat.getGeometry();

    					// retreive the symbol associated to such feature
    					ISymbol sym = legend.getSymbolByFeature(feat);
    					if (sym == null) {
    						continue;
    					}

    					SelectionSupport selectionSupport=getSelectionSupport();

    					if (highlight) {
	    					//C�digo para poder acceder a los �ndices para ver si est� seleccionado un Feature
	    					ReadableVectorial rv=getSource();
	    					int selectionIndex=-1;
	    					if (rv instanceof ISpatialDB){
	        					selectionIndex = ((ISpatialDB)rv).getRowIndexByFID(feat);
	        				} else {
	        					selectionIndex = Integer.parseInt(feat.getID());
	        				}
	    					if (selectionIndex!=-1) {
	        					if (selectionSupport.isSelected(selectionIndex)) {
	        						sym = sym.getSymbolForSelection();
	        					}
	        				}
    					}

    					if (useZSort) {
    						int[] symLevels = zSort.getLevels(sym);
    						if(symLevels != null){
    							// Check if this symbol is a multilayer
    							if (sym instanceof IMultiLayerSymbol) {
    								// if so, get the layer corresponding to the current
    								// level. If none, continue to next iteration
    								IMultiLayerSymbol mlSym = (IMultiLayerSymbol) sym;
    								for (int i = 0; i < mlSym.getLayerCount(); i++) {
    									ISymbol mySym = mlSym.getLayer(i);
    									if (symLevels[i] == mapPass) {
    										sym = mySym;
    										break;
    									}
    									System.out.println("avoided layer "+i+"of symbol '"+mlSym.getDescription()+"' (pass "+mapPass+")");
    								}
    							} else {
    								// else, just draw the symbol in its level
    								if (symLevels[0] != mapPass) {
    									System.out.println("avoided single layer symbol '"+sym.getDescription()+"' (pass "+mapPass+")");
    									continue;
    								}
    							}
    						}
    					}

    					// Check if this symbol is sized with CartographicSupport
    					CartographicSupport csSym = null;
    					int symbolType = sym.getSymbolType();

                        if (sym instanceof SimpleLineSymbol) {
                            if (((SimpleLineSymbol) sym).getLineStyle()
                                    .getArrowDecorator() != null) {
                                // Lines with decorators should not be cut
                                // because the decorators would be drawn in the
                                // wrong places
                                cutGeom = false;
                                if (!((SimpleLineSymbol) sym).getLineStyle()
                                        .getArrowDecorator()
                                        .isScaleArrow()) {
                                    // Hack for increasing non-scaled arrow
                                    // marker size, which usually looks smaller
                                    // when printing
                                    SimpleLineSymbol lineSym = new SimpleLineSymbol();
                                    lineSym.setXMLEntity(sym.getXMLEntity());
                                    lineSym.getLineStyle()
                                            .getArrowDecorator()
                                            .getMarker()
                                            .setSize(
                                                    lineSym.getLineStyle()
                                                            .getArrowDecorator()
                                                            .getMarker()
                                                            .getSize() * 3);
                                    csSym = lineSym;
                                }
                            }
                        } else if (symbolType == FShape.POINT
                                || symbolType == FShape.LINE
                                || sym instanceof CartographicSupport) {

                            csSym = (CartographicSupport) sym;
                        }

    					//System.err.println("passada "+mapPass+" pinte s�mboll "+sym.getDescription());

                        // We check if the geometry seems to intersect with the
                        // view extent
                        Rectangle2D extent = viewPort.getExtent();
                        IGeometry geomToPrint = null;
                        if (cutGeom) {

                            try {
                                if (geom.fastIntersects(extent.getX(),
                                        extent.getY(), extent.getWidth(),
                                        extent.getHeight())) {
                                    // If it does, then we create a rectangle
                                    // based on
                                    // the view extent and cut the geometries by
                                    // it
                                    // before drawing them
                                    GeometryFactory geomF = new GeometryFactory();
                                    Geometry intersection = geom
                                            .toJTSGeometry()
                                            .intersection(
                                                    new Polygon(
                                                            geomF.createLinearRing(new Coordinate[] {
                                                                    new Coordinate(
                                                                            extent.getMinX(),
                                                                            extent.getMaxY()),
                                                                    new Coordinate(
                                                                            extent.getMaxX(),
                                                                            extent.getMaxY()),
                                                                    new Coordinate(
                                                                            extent.getMaxX(),
                                                                            extent.getMinY()),
                                                                    new Coordinate(
                                                                            extent.getMinX(),
                                                                            extent.getMinY()),
                                                                    new Coordinate(
                                                                            extent.getMinX(),
                                                                            extent.getMaxY()) }),
                                                            null, geomF));
                                    if (!intersection.isEmpty()) {
                                        geomToPrint = FConverter
                                                .jts_to_igeometry(intersection);
                                    }
                                }
                            } catch (TopologyException e) {
                                logger.warn(
                                        "Some error happened while trying to cut a polygon with the view extent before printing (layer '"
                                                + this.getName()
                                                + "' / feature id "
                                                + feat.getID()
                                                + "). The whole polygon will be drawn. ",
                                        e);
                                geomToPrint = geom;
                            }
                        } else {
                            geomToPrint = geom;
                        }
                        if (geomToPrint != null) {
                            if (csSym == null) {
                                geomToPrint.drawInts(g, viewPort, sym, null);
                            } else {
                                geomToPrint.drawInts(g, viewPort, dpi, csSym,
                                        cancel);
                            }
                        }

    				}
    				it.closeIterator();
    			}
    		} catch (ReadDriverException e) {
    			this.setVisible(false);
    			this.setActive(false);
    			throw e;
    		}
    	}
    }


    public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel,
            double scale, PrintRequestAttributeSet properties) throws ReadDriverException {
    	print(g, viewPort, cancel, scale, properties, false);
    }

    public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel,
            double scale, PrintRequestAttributeSet properties, boolean highlight) throws ReadDriverException {
    	if (isVisible() && isWithinScale(scale)) {
    		_print(g, viewPort, cancel, scale, properties, highlight);
    	}
    }

    public void deleteSpatialIndex() {
        //must we delete possible spatial indexes files?
        spatialIndex = null;
    }

   /**
    * <p>
    * Creates an spatial index associated to this layer.
    * The spatial index will used
    * the native projection of the layer, so if the layer is reprojected, it will
    * be ignored.
    * </p>
    * @param cancelMonitor instance of CancellableMonitorable that allows
    * to monitor progress of spatial index creation, and cancel the process
    */
    public void createSpatialIndex(CancellableMonitorable cancelMonitor){
         // FJP: ESTO HABR� QUE CAMBIARLO. PARA LAS CAPAS SECUENCIALES, TENDREMOS
        // QUE ACCEDER CON UN WHILE NEXT. (O mejorar lo de los FeatureVisitor
        // para que acepten recorrer sin geometria, solo con rectangulos.

        //If this vectorial layer is based in a spatial database, the spatial
        //index is already implicit. We only will index file drivers
        ReadableVectorial va = getSource();
        //We must think in non spatial databases, like HSQLDB
        if(!(va instanceof VectorialFileAdapter)){
            return;
        }
        if (!(va.getDriver() instanceof BoundedShapes)) {
            return;
        }
        File file = ((VectorialFileAdapter) va).getFile();
        String fileName = file.getAbsolutePath();
        ISpatialIndex localCopy = null;
        try {
            va.start();
            localCopy = new QuadtreeGt2(fileName, "NM", va.getFullExtent(),
                    va.getShapeCount(), true);

        } catch (SpatialIndexException e1) {
            // Probably we dont have writing permissions
            String directoryName = System.getProperty("java.io.tmpdir");
            File newFile = new File(directoryName +
                    File.separator +
                    file.getName());
            String newFileName = newFile.getName();
            try {
                localCopy = new QuadtreeGt2(newFileName, "NM", va.getFullExtent(),
                        va.getShapeCount(), true);
            } catch (SpatialIndexException e) {
                // if we cant build a file based spatial index, we'll build
                // a pure memory spatial index
                localCopy = new QuadtreeJts();
            } catch (ReadDriverException e) {
                localCopy = new QuadtreeJts();
            }

        } catch(Exception e){
            e.printStackTrace();
        }//try
        BoundedShapes shapeBounds = (BoundedShapes) va.getDriver();
        try {
            for (int i=0; i < va.getShapeCount(); i++)
            {
                if(cancelMonitor != null){
                    if(cancelMonitor.isCanceled())
                        return;
                    cancelMonitor.reportStep();
                }
                Rectangle2D r = shapeBounds.getShapeBounds(i);
                if(r != null)
                    localCopy.insert(r, i);
            } // for
            va.stop();
            if(localCopy instanceof IPersistentSpatialIndex)
                ((IPersistentSpatialIndex) localCopy).flush();
            spatialIndex = localCopy;
            //vectorial adapter needs a reference to the spatial index, to solve
            //request for feature iteration based in spatial queries
            source.setSpatialIndex(spatialIndex);
        } catch (ReadDriverException e) {
            e.printStackTrace();
        }
    }

    public void createSpatialIndex() {
        createSpatialIndex(null);
    }

    public void process(FeatureVisitor visitor, FBitSet subset)
            throws ReadDriverException, ExpansionFileReadException, VisitorException {
        Strategy s = StrategyManager.getStrategy(this);
        s.process(visitor, subset);
    }

    public void process(FeatureVisitor visitor) throws ReadDriverException, VisitorException {
        Strategy s = StrategyManager.getStrategy(this);
        s.process(visitor);
    }

    public void process(FeatureVisitor visitor, Rectangle2D rect)
            throws ReadDriverException, ExpansionFileReadException, VisitorException {
        Strategy s = StrategyManager.getStrategy(this);
        s.process(visitor, rect);
    }

    public FBitSet queryByRect(Rectangle2D rect) throws ReadDriverException, VisitorException {
        Strategy s = StrategyManager.getStrategy(this);

        return s.queryByRect(rect);
    }

    public FBitSet queryByPoint(Point2D p, double tolerance)
            throws ReadDriverException, VisitorException {
        Strategy s = StrategyManager.getStrategy(this);
        return s.queryByPoint(p, tolerance);
    }

    public FBitSet queryByShape(IGeometry g, int relationship)
            throws ReadDriverException, VisitorException {
        Strategy s = StrategyManager.getStrategy(this);
        return s.queryByShape(g, relationship);
    }

    public XMLItem[] getInfo(Point p, double tolerance, Cancellable cancel) throws ReadDriverException, VisitorException {
        Point2D pReal = this.getMapContext().getViewPort().toMapPoint(p);
        FBitSet bs = queryByPoint(pReal, tolerance);
        VectorialXMLItem[] item = new VectorialXMLItem[1];
        item[0] = new VectorialXMLItem(bs, this);

        return item;
    }

    public void setLegend(IVectorLegend r) throws LegendLayerException {
    	if (this.legend == r){
    		return;
    	}
		if (this.legend != null && this.legend.equals(r)){
			return;
		}
        IVectorLegend oldLegend = legend;

        /*
         * Parche para discriminar las leyendas clasificadas cuyos campos de
         * clasificaci�n no est�n en la fuente de la capa.
         *
         * Esto puede ocurrir porque en versiones anteriores se admit�an
         * leyendas clasificadas en capas que se han unido a una tabla
         * por campos que pertenec�an a la tabla y no s�lo a la capa.
         *
         */
//		if(r instanceof IClassifiedVectorLegend){
//			IClassifiedVectorLegend classifiedLegend = (IClassifiedVectorLegend)r;
//			String[] fieldNames = classifiedLegend.getClassifyingFieldNames();
//
//			for (int i = 0; i < fieldNames.length; i++) {
//				try {
//					if(this.getRecordset().getFieldIndexByName(fieldNames[i]) == -1){
////					if(this.getSource().getRecordset().getFieldIndexByName(fieldNames[i]) == -1){
//						logger.warn("Some fields of the classification of the legend doesn't belong with the source of the layer.");
//						if (this.legend == null){
//							r = LegendFactory.createSingleSymbolLegend(this.getShapeType());
//						} else {
//							return;
//						}
//					}
//				} catch (ReadDriverException e1) {
//					throw new LegendLayerException(getName(),e1);
//				}
//			}
//		}
		/* Fin del parche */

		legend = r;
        try {
            legend.setDataSource(getRecordset());
        } catch (FieldNotFoundException e1) {
            throw new LegendLayerException(getName(),e1);
        } catch (ReadDriverException e1) {
            throw new LegendLayerException(getName(),e1);
        } finally{
        	this.updateDrawVersion();
        }
        if (oldLegend != null){
        	oldLegend.removeLegendListener(this);
        }
        if (legend != null){
        	legend.addLegendListener(this);
        }
        LegendChangedEvent e = LegendChangedEvent.createLegendChangedEvent(
                oldLegend, legend);
        e.setLayer(this);
        callLegendChanged(e);
    }

    /**
     * Devuelve la Leyenda de la capa.
     *
     * @return Leyenda.
     */
    public ILegend getLegend() {
        return legend;
    }

    /**
     * Devuelve el tipo de shape que contiene la capa.
     *
     * @return tipo de shape.
     *
     * @throws DriverException
     */
    public int getShapeType() throws ReadDriverException {
        if (typeShape == -1) {
            getSource().start();
            typeShape = getSource().getShapeType();
            getSource().stop();
        }

        return typeShape;
    }

    public XMLEntity getXMLEntity() throws XMLException {
        if (!this.isAvailable() && this.orgXMLEntity != null) {
            return this.orgXMLEntity;
        }
        XMLEntity xml = super.getXMLEntity();
        if (getLegend()!=null)
            xml.addChild(getLegend().getXMLEntity());
        try {
            if (getRecordset()!=null)
                xml.addChild(getRecordset().getSelectionSupport().getXMLEntity());
        } catch (ReadDriverException e1) {
            e1.printStackTrace();
            throw new XMLException(e1);
        }
        // Repongo el mismo ReadableVectorial m�s abajo para cuando se guarda el proyecto.
        ReadableVectorial rv=getSource();
        xml.putProperty("type", "vectorial");
        if (source instanceof VectorialEditableAdapter) {
            setSource(((VectorialEditableAdapter) source).getOriginalAdapter());
        }
        if (source instanceof VectorialFileAdapter) {
            xml.putProperty("type", "vectorial");
            xml.putProperty("absolutePath",((VectorialFileAdapter) source)
                    .getFile().getAbsolutePath());
            xml.putProperty("file", pathGenerator.getPath(((VectorialFileAdapter) source)
                    .getFile().getAbsolutePath()));
            try {
                xml.putProperty("recordset-name", source.getRecordset()
                        .getName());
            } catch (ReadDriverException e) {
                throw new XMLException(e);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
        } else if (source instanceof VectorialDBAdapter) {
            xml.putProperty("type", "vectorial");

            IVectorialDatabaseDriver dbDriver = (IVectorialDatabaseDriver) source
                    .getDriver();

            // Guardamos el nombre del driver para poder recuperarlo
            // con el DriverManager de Fernando.
            xml.putProperty("db", dbDriver.getName());
            try {
                xml.putProperty("recordset-name", source.getRecordset()
                        .getName());
            } catch (ReadDriverException e) {
                throw new XMLException(e);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            xml.addChild(dbDriver.getXMLEntity()); // Tercer child. Antes hemos
                                                    // metido la leyenda y el
                                                    // selection support
        } else if (source instanceof VectorialAdapter) {
            // Se supone que hemos hecho algo gen�rico.
            xml.putProperty("type", "vectorial");

            VectorialDriver driver = source.getDriver();

            // Guardamos el nombre del driver para poder recuperarlo
            // con el DriverManager de Fernando.
            xml.putProperty("other", driver.getName());
            // try {
            try {
                xml.putProperty("recordset-name", source.getRecordset()
                        .getName());
            } catch (ReadDriverException e) {
                throw new XMLException(e);
            } catch (RuntimeException e) {
                e.printStackTrace();
            }
            if (driver instanceof IPersistence) {
                // xml.putProperty("className", driver.getClass().getName());
            	IPersistence persist = (IPersistence) driver;
                xml.addChild(persist.getXMLEntity()); // Tercer child. Antes
                                                        // hemos metido la
                                                        // leyenda y el
                                                        // selection support
            }
        }
        if (rv!=null)
            setSource(rv);
        xml.putProperty("driverName", source.getDriver().getName());
        if (bHasJoin)
            xml.putProperty("hasJoin", "true");

        // properties from ILabelable
        xml.putProperty("isLabeled", isLabeled);
        if (strategy != null) {
            XMLEntity strategyXML = strategy.getXMLEntity();
            strategyXML.putProperty("Strategy", strategy.getClassName());
            xml.addChild(strategy.getXMLEntity());
        }
        xml.addChild(getLinkProperties().getXMLEntity());
        return xml;
    }

    /*
     * @see com.iver.cit.gvsig.fmap.layers.FLyrDefault#setXMLEntity(com.iver.utiles.XMLEntity)
     */
    public void setXMLEntity(XMLEntity xml) throws XMLException {
        try {
    		super.setXMLEntity(xml);
    		XMLEntity legendXML = xml.getChild(0);
    		IVectorLegend leg = LegendFactory.createFromXML(legendXML);

    		/*
    		 * Parche para detectar cuando, por algun problema de persistencia
    		 * respecto a versiones anteriores, la leyenda que se ha cargado
    		 * no se corresponde con el tipo de shape de la capa.
    		 */
    		int legShapeType = leg.getShapeType();
    		if (legShapeType != 0 && legShapeType != this.getShapeType()){
    			leg = LegendFactory.createSingleSymbolLegend(this.getShapeType());
				logger.warn("Legend shape type and layer shape type does not match.");
    		}
    		/* Fin del parche */

    		try {
    			getRecordset().getSelectionSupport().setXMLEntity(xml.getChild(1));
    			// JMVIVO: Esto sirve para algo????
    			/*
    			 *  Jaume: si, para restaurar el selectable datasource cuando se
    			 *  clona la capa, cuando se carga de un proyecto. Si no esta ya
    			 *  no se puede ni hacer consultas sql, ni hacer selecciones,
    			 *  ni usar la mayor parte de las herramientas.
    			 *
    			 *  Lo vuelvo a poner.
    			 */

    			String recordsetName = xml.getStringProperty("recordset-name");

    			LayerFactory.getDataSourceFactory().changeDataSourceName(
    					getSource().getRecordset().getName(), recordsetName);
    			SelectableDataSource sds = new SelectableDataSource(LayerFactory
    					.getDataSourceFactory().createRandomDataSource(
    							recordsetName, DataSourceFactory.AUTOMATIC_OPENING));

    		} catch (NoSuchTableException e1) {
    			this.setAvailable(false);
    			throw new XMLException(e1);
    		} catch (ReadDriverException e1) {
    			this.setAvailable(false);
    			throw new XMLException(e1);
    		}
    		// Si tiene una uni�n, lo marcamos para que no se cree la leyenda hasta
    		// el final
    		// de la lectura del proyecto
    		if (xml.contains("hasJoin")) {
    			setIsJoined(true);
    			PostProcessSupport.addToPostProcess(this, "setLegend", leg, 1);
    		} else {
    			try {
    				setLegend(leg);
    			} catch (LegendLayerException e) {
    				throw new XMLException(e);
    			}
    		}

    		//Por compatibilidad con proyectos anteriores a la 1.0
    		boolean containsIsLabeled = xml.contains("isLabeled");
    		if (containsIsLabeled){
    			isLabeled = xml.getBooleanProperty("isLabeled");
    		}
    		// set properties for ILabelable
    		XMLEntity labelingXML = xml.firstChild("labelingStrategy", "labelingStrategy");
    		if (labelingXML!= null) {
    			if(!containsIsLabeled){
    				isLabeled = true;
    			}
    			try {
    				ILabelingStrategy labeling = LabelingFactory.createStrategyFromXML(labelingXML, this);
    				if (isJoined()) {
    					PostProcessSupport.addToPostProcess(this, "setLabelingStrategy", labeling, 1);
    				}
    				else
    					this.setLabelingStrategy(labeling);

    			} catch (NotExistInXMLEntity neXMLEX) {
    				// no strategy was set, just continue;
    				logger.warn("Reached what should be unreachable code");
    			}
    		} else if (legendXML.contains("labelFieldName")|| legendXML.contains("labelfield")) {
    			/* (jaume) begin patch;
        		 * for backward compatibility purposes. Since gvSIG v1.1 labeling is
        		 * no longer managed by the Legend but by the ILabelingStrategy. The
        		 * following allows restoring older projects' labelings.
        		 */
    			String labelTextField =	null;
    			if (legendXML.contains("labelFieldName")){
    				labelTextField = legendXML.getStringProperty("labelFieldName");
        			if (labelTextField != null) {
        				AttrInTableLabelingStrategy labeling = new AttrInTableLabelingStrategy();
        				labeling.setLayer(this);
						int unit = 1;
						boolean useFixedSize = false;
    					String labelFieldHeight = legendXML.getStringProperty("labelHeightFieldName");
    					labeling.setTextField(labelTextField);
    					if(labelFieldHeight!=null){
    						labeling.setHeightField(labelFieldHeight);
    					} else {
    						double size = -1;
    						for(int i=0; i<legendXML.getChildrenCount();i++){
    							XMLEntity xmlChild = legendXML.getChild(i);
    							if(xmlChild.contains("m_FontSize")){
    								double childFontSize =  xmlChild.getDoubleProperty("m_FontSize");
    								if(size<0){
    									size = childFontSize;
    									useFixedSize = true;
    								} else {
    									useFixedSize = useFixedSize && (size==childFontSize);
    								}
    								if(xmlChild.contains("m_bUseFontSize")){
    									if(xmlChild.getBooleanProperty("m_bUseFontSize")){
    										unit = -1;
    									} else {
    										unit = 1;
    									}
    								}
    							}
    						}
    						labeling.setFixedSize(size/1.4);//Factor de correcci�n que se aplicaba antes en el etiquetado
    					}
						labeling.setUsesFixedSize(useFixedSize);
						labeling.setUnit(unit);
        				labeling.setRotationField(legendXML.getStringProperty("labelRotationFieldName"));
        				if (isJoined()) {
        					PostProcessSupport.addToPostProcess(this, "setLabelingStrategy", labeling, 1);
        				}
        				else
        					this.setLabelingStrategy(labeling);
        				this.setIsLabeled(true);
        			}
    			}else{
    				labelTextField = legendXML.getStringProperty("labelfield");
    				if (labelTextField != null) {
    					AttrInTableLabelingStrategy labeling = new AttrInTableLabelingStrategy();
    					labeling.setLayer(this);
						int unit = 1;
						boolean useFixedSize = false;
    					String labelFieldHeight = legendXML.getStringProperty("labelFieldHeight");
    					labeling.setTextField(labelTextField);
    					if(labelFieldHeight!=null){
    						labeling.setHeightField(labelFieldHeight);
    					} else {
    						double size = -1;
    						for(int i=0; i<legendXML.getChildrenCount();i++){
    							XMLEntity xmlChild = legendXML.getChild(i);
    							if(xmlChild.contains("m_FontSize")){
    								double childFontSize =  xmlChild.getDoubleProperty("m_FontSize");
    								if(size<0){
    									size = childFontSize;
    									useFixedSize = true;
    								} else {
    									useFixedSize = useFixedSize && (size==childFontSize);
    								}
    								if(xmlChild.contains("m_bUseFontSize")){
    									if(xmlChild.getBooleanProperty("m_bUseFontSize")){
    										unit = -1;
    									} else {
    										unit = 1;
    									}
    								}
    							}
    						}
    						labeling.setFixedSize(size/1.4);//Factor de correcci�n que se aplicaba antes en el etiquetado
    					}
						labeling.setUsesFixedSize(useFixedSize);
						labeling.setUnit(unit);
    					labeling.setRotationField(legendXML.getStringProperty("labelFieldRotation"));
        				if (isJoined()) {
        					PostProcessSupport.addToPostProcess(this, "setLabelingStrategy", labeling, 1);
        				}
        				else
        					this.setLabelingStrategy(labeling);
    					this.setIsLabeled(true);
    				}
    			}

        	}else if(!containsIsLabeled){
    				isLabeled = false;
    		}

    		// compatibility with hyperlink from 1.9 alpha version... do we really need to be compatible with alpha versions??
    		XMLEntity xmlLinkProperties=xml.firstChild("typeChild", "linkProperties");
    		if (xmlLinkProperties != null){
    			try {
    				String fieldName=xmlLinkProperties.getStringProperty("fieldName");
    				xmlLinkProperties.remove("fieldName");
    				String extName = xmlLinkProperties.getStringProperty("extName");
    				xmlLinkProperties.remove("extName");
    				int typeLink = xmlLinkProperties.getIntProperty("typeLink");
    				xmlLinkProperties.remove("typeLink");
    				if (fieldName!=null) {
    					setProperty("legacy.hyperlink.selectedField", fieldName);
    					setProperty("legacy.hyperlink.type", new Integer(typeLink));
    					if (extName!=null) {
    						setProperty("legacy.hyperlink.extension", extName);
    					}
    				}
    			}
    			catch (NotExistInXMLEntity ex) {
    				logger.warn("Error getting old hyperlink configuration", ex);
    			}
    		}

    	} catch (XMLException e) {
    		this.setAvailable(false);
    		this.orgXMLEntity = xml;
    	} catch (Exception e) {
    		e.printStackTrace();
    		this.setAvailable(false);
    		this.orgXMLEntity = xml;

    	}


    }

    public void setXMLEntityNew(XMLEntity xml) throws XMLException {
        try {
            super.setXMLEntity(xml);

            XMLEntity legendXML = xml.getChild(0);
            IVectorLegend leg = LegendFactory.createFromXML(legendXML);
            /* (jaume) begin patch;
             * for backward compatibility purposes. Since gvSIG v1.1 labeling is
             * no longer managed by the Legend but by the ILabelingStrategy. The
             * following allows restoring older projects' labelings.
             */
            if (legendXML.contains("labelFieldHeight")) {
                AttrInTableLabelingStrategy labeling = new AttrInTableLabelingStrategy();
                labeling.setLayer(this);
                labeling.setTextField(legendXML.getStringProperty("labelFieldHeight"));
                labeling.setRotationField(legendXML.getStringProperty("labelFieldRotation"));
                this.setLabelingStrategy(labeling);
                this.setIsLabeled(true);
              }
            /* end patch */
            try {
                getRecordset().getSelectionSupport().setXMLEntity(xml.getChild(1));

                this.setLoadSelection(xml.getChild(1));
            } catch (ReadDriverException e1) {
                this.setAvailable(false);
                throw new XMLException(e1);
            }
            // Si tiene una uni�n, lo marcamos para que no se cree la leyenda hasta
            // el final
            // de la lectura del proyecto
            if (xml.contains("hasJoin")) {
                setIsJoined(true);
                PostProcessSupport.addToPostProcess(this, "setLegend", leg, 1);
            } else {
                this.setLoadLegend(leg);
            }

        } catch (XMLException e) {
            this.setAvailable(false);
            this.orgXMLEntity = xml;
        } catch (Exception e) {
            this.setAvailable(false);
            this.orgXMLEntity = xml;
        }


    }


    /**
     * Sobreimplementaci�n del m�todo toString para que las bases de datos
     * identifiquen la capa.
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        /*
         * Se usa internamente para que la parte de datos identifique de forma
         * un�voca las tablas
         */
        String ret = super.toString();

        return "layer" + ret.substring(ret.indexOf('@') + 1);
    }

    public boolean isJoined() {
        return bHasJoin;
    }

    /**
     * Returns if a layer is spatially indexed
     *
     * @return if this layer has the ability to proces spatial queries without
     *         secuential scans.
     */
    public boolean isSpatiallyIndexed() {
        ReadableVectorial source = getSource();
        if (source instanceof ISpatialDB)
            return true;

//FIXME azabala
/*
 * Esto es muy dudoso, y puede cambiar.
 * Estoy diciendo que las que no son fichero o no son
 * BoundedShapes estan indexadas. Esto es mentira, pero
 * as� quien pregunte no querr� generar el indice.
 * Esta por ver si interesa generar el indice para capas
 * HSQLDB, WFS, etc.
 */
        if(!(source instanceof VectorialFileAdapter)){
            return true;
        }
        if (!(source.getDriver() instanceof BoundedShapes)) {
            return true;
        }

        if (getISpatialIndex() != null)
            return true;
        return false;
    }

    public void setIsJoined(boolean hasJoin) {
        bHasJoin = hasJoin;
    }

    /**
     * @return Returns the spatialIndex.
     */
    public ISpatialIndex getISpatialIndex() {
        return spatialIndex;
    }
    /**
     * Sets the spatial index. This could be useful if, for some
     * reasons, you want to work with a distinct spatial index
     * (for example, a spatial index which could makes nearest
     * neighbour querys)
     * @param spatialIndex
     */
    public void setISpatialIndex(ISpatialIndex spatialIndex){
        this.spatialIndex = spatialIndex;
    }

    public SelectableDataSource getRecordset() throws ReadDriverException {
        if (!this.isAvailable()) return null;
        if (sds == null) {

                SelectableDataSource ds = source.getRecordset();

                if (ds == null) {
                    return null;
                }

                sds = ds;
                getSelectionSupport().addSelectionListener(this);

        }
        return sds;
    }

    public void setEditing(boolean b) throws StartEditionLayerException {
        super.setEditing(b);
        try {
            if (b) {
                VectorialEditableAdapter vea = null;
                // TODO: Qu� pasa si hay m�s tipos de adapters?
                // FJP: Se podr�a pasar como argumento el
                // VectorialEditableAdapter
                // que se quiera usar para evitar meter c�digo aqu� de este
                // estilo.
                if (getSource() instanceof VectorialDBAdapter) {
                    vea = new VectorialEditableDBAdapter();
                } else if (this instanceof FLyrAnnotation) {
                    vea = new AnnotationEditableAdapter(
                            (FLyrAnnotation) this);
                } else {
                    vea = new VectorialEditableAdapter();
                }
                vea.addEditionListener(this);
                vea.setOriginalVectorialAdapter(getSource());
//				azo: implementations of readablevectorial need
                //references of projection and spatial index
                vea.setProjection(getProjection());
                vea.setSpatialIndex(spatialIndex);


                // /vea.setSpatialIndex(getSpatialIndex());
                // /vea.setFullExtent(getFullExtent());
                vea.setCoordTrans(getCoordTrans());
                vea.startEdition(EditionEvent.GRAPHIC);
                setSource(vea);
                getRecordset().setSelectionSupport(
                        vea.getOriginalAdapter().getRecordset()
                                .getSelectionSupport());

            } else {
                VectorialEditableAdapter vea = (VectorialEditableAdapter) getSource();
                vea.removeEditionListener(this);
                setSource(vea.getOriginalAdapter());
            }
            // Si tenemos una leyenda, hay que pegarle el cambiazo a su
            // recordset
            setRecordset(getSource().getRecordset());
            if (getLegend() instanceof IVectorLegend) {
                IVectorLegend ley = (IVectorLegend) getLegend();
                ley.setDataSource(getSource().getRecordset());
                // Esto lo pongo para evitar que al dibujar sobre un
                // dxf, dwg, o dgn no veamos nada. Es debido al checkbox
                // de la leyenda de textos "dibujar solo textos".
//jaume
//				if (!(getSource().getDriver() instanceof IndexedShpDriver)){
//					FSymbol symbol=new FSymbol(getShapeType());
//					symbol.setFontSizeInPixels(false);
//					symbol.setFont(new Font("SansSerif", Font.PLAIN, 9));
//					Color color=symbol.getColor();
//					int alpha=symbol.getColor().getAlpha();
//					if (alpha>250) {
//						symbol.setColor(new Color(color.getRed(),color.getGreen(),color.getBlue(),100));
//					}
//					ley.setDefaultSymbol(symbol);
//				}
//jaume//
                ley.useDefaultSymbol(true);
            }
        } catch (ReadDriverException e) {
            throw new StartEditionLayerException(getName(),e);
        } catch (FieldNotFoundException e) {
            throw new StartEditionLayerException(getName(),e);
        } catch (StartWriterVisitorException e) {
            throw new StartEditionLayerException(getName(),e);
        }

        setSpatialCacheEnabled(b);
        callEditionChanged(LayerEvent
                .createEditionChangedEvent(this, "edition"));

    }

    /**
     * Para cuando haces una uni�n, sustituyes el recorset por el nuevo. De esta
     * forma, podr�s poner leyendas basadas en el nuevo recordset
     *
     * @param newSds
     */
    public void setRecordset(SelectableDataSource newSds) {
    	// TODO: Deberiamos hacer comprobaciones del cambio
        sds = newSds;
		getSelectionSupport().addSelectionListener(this);
		this.updateDrawVersion();
    }

    public void clearSpatialCache()
    {
        spatialCache.clearAll();
    }

    public boolean isSpatialCacheEnabled() {
        return spatialCacheEnabled;
    }

    public void setSpatialCacheEnabled(boolean spatialCacheEnabled) {
        this.spatialCacheEnabled = spatialCacheEnabled;
    }

    public SpatialCache getSpatialCache() {
        return spatialCache;
    }

    /**
     * Siempre es un numero mayor de 1000
     * @param maxFeatures
     */
    public void setMaxFeaturesInEditionCache(int maxFeatures) {
        if (maxFeatures > spatialCache.maxFeatures)
            spatialCache.setMaxFeatures(maxFeatures);

    }

    /**
     * This method returns a boolean that is used by the FPopMenu
     * to make visible the properties menu or not. It is visible by
     * default, and if a later don't have to show this menu only
     * has to override this method.
     * @return
     * If the properties menu is visible (or not)
     */
    public boolean isPropertiesMenuVisible(){
        return true;
    }

    public void reload() throws ReloadLayerException {
    	if(this.isEditing()){
            throw new ReloadLayerException(getName());
    	}
        this.setAvailable(true);
        super.reload();
        this.updateDrawVersion();
        try {
            this.source.getDriver().reload();
            if (this.getLegend() == null) {
                if (this.getRecordset().getDriver() instanceof WithDefaultLegend) {
                    WithDefaultLegend aux = (WithDefaultLegend) this.getRecordset().getDriver();
                    this.setLegend((IVectorLegend) aux.getDefaultLegend());
                    this.setLabelingStrategy(aux.getDefaultLabelingStrategy());
                } else {
                    this.setLegend(LegendFactory.createSingleSymbolLegend(
                            this.getShapeType()));
                }
            }

        } catch (LegendLayerException e) {
            this.setAvailable(false);
            throw new ReloadLayerException(getName(),e);
        } catch (ReadDriverException e) {
            this.setAvailable(false);
            throw new ReloadLayerException(getName(),e);
        }

    }

    protected void setLoadSelection(XMLEntity xml) {
        this.loadSelection = xml;
    }

    protected void setLoadLegend(IVectorLegend legend) {
        this.loadLegend = legend;
    }

    protected void putLoadSelection() throws XMLException {
        if (this.loadSelection == null) return;
        try {
            this.getRecordset().getSelectionSupport().setXMLEntity(this.loadSelection);
        } catch (ReadDriverException e) {
            throw new XMLException(e);
        }
        this.loadSelection = null;

    }
    protected void putLoadLegend() throws LegendLayerException {
        if (this.loadLegend == null) return;
        this.setLegend(this.loadLegend);
        this.loadLegend = null;
    }

    protected void cleanLoadOptions() {
        this.loadLegend = null;
        this.loadSelection = null;
    }

    public boolean isWritable() {
        VectorialDriver drv = getSource().getDriver();
        if (!drv.isWritable())
            return false;
        if (drv instanceof IWriteable)
        {
            IWriter writer = ((IWriteable)drv).getWriter();
            if (writer != null)
            {
                if (writer instanceof ISpatialWriter)
                    return true;
            }
        }
        return false;

    }

    public FLayer cloneLayer() throws Exception {
        FLyrVect clonedLayer = new FLyrVect();
        clonedLayer.setSource(getSource());
        if (isJoined()) {
			clonedLayer.setIsJoined(true);
			clonedLayer.setRecordset(getRecordset());
		}
        clonedLayer.setVisible(isVisible());
        clonedLayer.setISpatialIndex(getISpatialIndex());
        clonedLayer.setName(getName());
        clonedLayer.setCoordTrans(getCoordTrans());

        clonedLayer.setLegend((IVectorLegend)getLegend().cloneLegend());

        clonedLayer.setIsLabeled(isLabeled());
        ILabelingStrategy labelingStrategy=getLabelingStrategy();
        if (labelingStrategy!=null)
        	clonedLayer.setLabelingStrategy(labelingStrategy);

        return clonedLayer;
    }

    public SelectionSupport getSelectionSupport() {
		try {
			return getRecordset().getSelectionSupport();
		} catch (ReadDriverException e) {
			e.printStackTrace();
		}
		return null;
	}

    protected boolean isOnePoint(AffineTransform graphicsTransform, ViewPort viewPort, double dpi, CartographicSupport csSym, IGeometry geom, int[] xyCoords) {
    	return isOnePoint(graphicsTransform, viewPort, geom, xyCoords) && csSym.getCartographicSize(viewPort, dpi, (FShape)geom.getInternalShape()) <= 1;
    }

    protected boolean isOnePoint(AffineTransform graphicsTransform, ViewPort viewPort, IGeometry geom, int[] xyCoords) {
    	boolean onePoint = false;
    	int type=geom.getGeometryType() % FShape.Z;
    	if (type!=FShape.POINT && type!=FShape.MULTIPOINT && type!=FShape.NULL) {

			Rectangle2D geomBounds = geom.getBounds2D();

		//	ICoordTrans ct = getCoordTrans();

			// Se supone que la geometria ya esta
			// repoyectada y no hay que hacer
			// ninguna transformacion
//			if (ct!=null) {
////				geomBounds = ct.getInverted().convert(geomBounds);
//				geomBounds = ct.convert(geomBounds);
//			}

			double dist1Pixel = viewPort.getDist1pixel();

			onePoint = (geomBounds.getWidth()  <= dist1Pixel
					 && geomBounds.getHeight() <= dist1Pixel);

			if (onePoint) {
				// avoid out of range exceptions
				FPoint2D p = new FPoint2D(geomBounds.getMinX(), geomBounds.getMinY());
				p.transform(viewPort.getAffineTransform());
				p.transform(graphicsTransform);
				xyCoords[0] = (int) p.getX();
				xyCoords[1] = (int) p.getY();

			}

		}
    	return onePoint;
    }
    /*
     * jaume. Stuff from ILabeled.
     */
    private boolean isLabeled;
    private ILabelingStrategy strategy;

    public boolean isLabeled() {
        return isLabeled;
    }

    public void setIsLabeled(boolean isLabeled) {
        this.isLabeled = isLabeled;
    }

    public ILabelingStrategy getLabelingStrategy() {
        return strategy;
    }

    public void setLabelingStrategy(ILabelingStrategy strategy) {
        this.strategy = strategy;
        try {
			strategy.setLayer(this);
		} catch (ReadDriverException e) {
			e.printStackTrace();
		}
    }

    public void drawLabels(BufferedImage image, Graphics2D g, ViewPort viewPort,
    		Cancellable cancel, double scale, double dpi) throws ReadDriverException {
        if (strategy!=null && isWithinScale(scale)) {
            strategy.draw(image, g, viewPort, cancel, dpi);
        }
    }
    public void printLabels(Graphics2D g, ViewPort viewPort,
    		Cancellable cancel, double scale, PrintRequestAttributeSet properties) throws ReadDriverException {
        if (strategy!=null) {
            strategy.print(g, viewPort, cancel, properties);
        }
    }


    //M�todos para el uso de HyperLinks en capas FLyerVect

    /**
     * Return true, because a Vectorial Layer supports HyperLink
     */
    public boolean allowLinks()
    {
    	return true;
    }

    /**
	 * Returns an instance of AbstractLinkProperties that contains the information
	 * of the HyperLink
	 * @return Abstra
	 */
    public AbstractLinkProperties getLinkProperties()
    {
    	return linkProperties;
    }

    /**
	 * Provides an array with URIs. Returns one URI by geometry that includes the point
	 * in its own geometry limits with a allowed tolerance.
	 * @param layer, the layer
	 * @param point, the point to check that is contained or not in the geometries in the layer
	 * @param tolerance, the tolerance allowed. Allowed margin of error to detect if the  point
	 * 		is contained in some geometries of the layer
	 * @return
	 */
    public URI[] getLink(Point2D point, double tolerance)
    {
    	//return linkProperties.getLink(this)
    	return linkProperties.getLink(this,point,tolerance);
    }

	public void selectionChanged(SelectionEvent e) {
		this.updateDrawVersion();
	}

	public void afterFieldEditEvent(AfterFieldEditEvent e) {
		this.updateDrawVersion();
	}

	public void afterRowEditEvent(IRow feat, AfterRowEditEvent e) {
		this.updateDrawVersion();

	}

	public void beforeFieldEditEvent(BeforeFieldEditEvent e) {

	}

	public void beforeRowEditEvent(IRow feat, BeforeRowEditEvent e) {

	}

	public void processEvent(EditionEvent e) {
		if (e.getChangeType()== e.ROW_EDITION){
			this.updateDrawVersion();
		}

	}

	public void legendCleared(LegendClearEvent event) {
		this.updateDrawVersion();
        LegendChangedEvent e = LegendChangedEvent.createLegendChangedEvent(
                legend, legend);
        this.callLegendChanged(e);
	}

	public boolean symbolChanged(SymbolLegendEvent e) {
		this.updateDrawVersion();
        LegendChangedEvent event = LegendChangedEvent.createLegendChangedEvent(
                legend, legend);
        this.callLegendChanged(event);
        return true;
	}
	public String getTypeStringVectorLayer() throws ReadDriverException {
		String typeString="";
		int typeShape=this.getShapeType();
		if (FShape.MULTI==typeShape){
			ReadableVectorial rv=this.getSource();
			int i=0;
			boolean isCorrect=false;
			while(rv.getShapeCount()>i && !isCorrect){
				IGeometry geom=rv.getShape(i);
				if (geom==null){
					i++;
					continue;
				}
				isCorrect=true;
				if ((geom.getGeometryType() & FShape.Z) == FShape.Z){
					typeString="Geometries3D";
				}else{
					typeString="Geometries2D";
 				}
 			}
		}else{
			ReadableVectorial rv=this.getSource();
			int i=0;
			boolean isCorrect=false;
			while(rv.getShapeCount()>i && !isCorrect){
				IGeometry geom=rv.getShape(i);
				if (geom==null){
					i++;
					continue;
				}
				isCorrect=true;
				int type=geom.getGeometryType();
				if (FShape.POINT == type){
					typeString="Point2D";
				} else if (FShape.LINE == type){
					typeString="Line2D";
				} else if (FShape.POLYGON == type){
					typeString="Polygon2D";
				} else if (FShape.MULTIPOINT == type){
					typeString="MultiPint2D";
				} else if ((FShape.POINT | FShape.Z)  == type ){
					typeString="Point3D";
				} else if ((FShape.LINE | FShape.Z)  == type ){
					typeString="Line3D";
				} else if ((FShape.POLYGON | FShape.Z)  == type ){
					typeString="Polygon3D";
				} else if ((FShape.MULTIPOINT | FShape.Z)  == type ){
					typeString="MultiPoint3D";
				} else if ((FShape.POINT | FShape.M)  == type ){
					typeString="PointM";
				} else if ((FShape.LINE | FShape.M)  == type ){
					typeString="LineM";
				} else if ((FShape.POLYGON | FShape.M)  == type ){
					typeString="PolygonM";
				} else if ((FShape.MULTIPOINT | FShape.M)  == type ){
					typeString="MultiPointM";
				} else if ((FShape.MULTI | FShape.M)  == type ){
					typeString="M";
				}

			}
			return typeString;
		}
		return "";
	}
	public int getTypeIntVectorLayer() throws ReadDriverException {
		int typeInt=0;
		int typeShape=this.getShapeType();
		if (FShape.MULTI==typeShape){
			ReadableVectorial rv=this.getSource();
			int i=0;
			boolean isCorrect=false;
			while(rv.getShapeCount()>i && !isCorrect){
				IGeometry geom=rv.getShape(i);
				if (geom==null){
					i++;
					continue;
				}
				isCorrect=true;
				if ((geom.getGeometryType() & FShape.Z) == FShape.Z){
					typeInt=FShape.MULTI | FShape.Z;
				}else{
					typeInt=FShape.MULTI;
				}
			}
		}else{
			ReadableVectorial rv=this.getSource();
			int i=0;
			boolean isCorrect=false;
			while(rv.getShapeCount()>i && !isCorrect){
				IGeometry geom=rv.getShape(i);
				if (geom==null){
					i++;
					continue;
				}
				isCorrect=true;
				int type=geom.getGeometryType();
				typeInt=type;
			}
			return typeInt;
		}
		return typeInt;
	}
 }