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
package com.iver.cit.gvsig.fmap.operations.strategies;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.BitSet;
import java.util.List;

import javax.print.attribute.PrintRequestAttributeSet;

import org.apache.log4j.Logger;
import org.cresques.cts.ICoordTrans;
import org.geotools.resources.geometry.XRectangle2D;
import org.gvsig.tools.OsUtils;

import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.exceptions.expansionfile.ExpansionFileReadException;
import com.iver.cit.gvsig.exceptions.visitors.VisitorException;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.drivers.BoundedShapes;
import com.iver.cit.gvsig.fmap.drivers.DriverAttributes;
import com.iver.cit.gvsig.fmap.drivers.VectorialDriver;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.iver.cit.gvsig.fmap.layers.SpatialCache;
import com.iver.cit.gvsig.fmap.layers.layerOperations.ClassifiableVectorial;
import com.iver.cit.gvsig.fmap.layers.layerOperations.Selectable;
import com.iver.cit.gvsig.fmap.rendering.IClassifiedLegend;
import com.iver.cit.gvsig.fmap.rendering.IVectorLegend;
import com.iver.cit.gvsig.fmap.spatialindex.ISpatialIndex;
import com.iver.utiles.swing.threads.Cancellable;
import com.iver.utiles.swing.threads.CancellableMonitorable;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.IntersectionMatrix;

/**
 * Esta clase definirá las operaciones de la interfaz FLyrVect de la manera más
 * óptima para los ficheros shp.
 */
public class ShpStrategy extends DefaultStrategy {
	private static Logger logger = Logger
			.getLogger(ShpStrategy.class.getName());

	/**
	 * Crea una ShpStrategy.
	 *
	 * @param capa
	 */
	public ShpStrategy(FLayer capa) {
		super(capa);
	}

	/**
	 * @see com.iver.cit.gvsig.fmap.operations.LayerOperations#draw(java.awt.image.BufferedImage,
	 *      ISymbol, FShape)
	 */
	public void draw(BufferedImage image, Graphics2D g, ViewPort viewPort,
			Cancellable cancel) throws ReadDriverException {
		try {
			FLyrVect lyr = (FLyrVect) getCapa();
			ReadableVectorial adapter = lyr.getSource();
			if (adapter.getShapeCount() <= 0) {
//				logger.info("Layer:" + getCapa().getName() + " sin registros");
				return;
			}

			Selectable selectable = lyr.getRecordset();
			ICoordTrans ct = lyr.getCoordTrans();
			FBitSet bitSet = selectable.getSelection();
			BoundedShapes shapeBounds;
			if (adapter instanceof BoundedShapes)
				shapeBounds = (BoundedShapes) adapter;
			else
				shapeBounds = (BoundedShapes) adapter.getDriver();
			// VectorialFileDriver driver = (VectorialFileDriver)
			// adapter.getDriver();
			// logger.debug("adapter.start() -> Layer:" + getCapa().getName());
			adapter.start();
			IGeometry geom;
//			if (adapter.getShapeCount() > 0) {
//				geom = adapter.getShape(0);
//			}
			IVectorLegend l = (IVectorLegend) ((ClassifiableVectorial) lyr)
					.getLegend();

			if (l instanceof IClassifiedLegend) {
				IClassifiedLegend clsfLegend = (IClassifiedLegend) l;
				ISymbol[] symbs = clsfLegend.getSymbols();
				// double rSym = 0;
				// double maxRSym = -1;

				for (int i = 0; i < symbs.length; i++) {
					// TODO: REVISAR LOS SIMBOLOS Y SUS TAMAÑOS

					/*
					 * Style2D pointSymbol = symbs[i].getPointStyle2D(); if
					 * (pointSymbol instanceof MarkStyle2D) { MarkStyle2D mrk2D =
					 * (MarkStyle2D) pointSymbol; rSym =
					 * viewPort.toMapDistance(mrk2D.getSize()); if (maxRSym <
					 * rSym) maxRSym = rSym; }
					 */
				}
			}

			Rectangle2D extent = viewPort.getAdjustedExtent();
			// AffineTransform at = viewPort.getAffineTransform();

			int sc;

			Rectangle2D bounds;

			long t1 = System.currentTimeMillis();
			// logger.debug("getCapa().getRecordset().start()");
			lyr.getRecordset().start();

			// TODO: A revisar si es o no conveniente este sistema
			// de comunicación con los drivers.
			DriverAttributes attr = adapter.getDriverAttributes();
			boolean bMustClone = false;
			if (attr != null) {
				if (attr.isLoadedInMemory()) {
					bMustClone = attr.isLoadedInMemory();
				}
			}

			List lstIndexes = null;

			// If area of needed extent is less than fullExtent / 4,
			// it will be worthy to use SpatialIndex.
			// Otherwhise, we will not use it.
			boolean bUseSpatialIndex = false;
			sc = adapter.getShapeCount();
			// if (lyr.getSpatialIndex() != null) AZABALA
			// long t11 = System.currentTimeMillis();
			ISpatialIndex isi=lyr.getISpatialIndex();
			if (isi != null) {
				if (isSpatialIndexNecessary(extent)) {
					lstIndexes = isi.query(extent);
					// If the layer is reprojected, spatial index was created
					// in its own projection, so we must to apply an inverse
					// transform
					if (ct != null) {
						Rectangle2D newExtent = ct.getInverted()
								.convert(extent);
						// Rectangle2D newExtent = ct.convert(extent);
						// lstIndexes = lyr.getISpatialIndex().query(extent);
						lstIndexes = isi.query(newExtent);
					} else {
						lstIndexes = isi.query(extent);
					}
					sc = lstIndexes.size();
					bUseSpatialIndex = true;
				}// if
			}// if
			/*
			 * long t12 = System.currentTimeMillis(); System.out.println("Tiempo
			 * en mirar el índice espacial y recuperar los índices:" +
			 * (t12-t11)); System.out.println("Numero de índices:" + sc);
			 */

			// SpatialCache cache = lyr.createSpatialCache();
			lyr.getSpatialCache().clearAll();
			SpatialCache cache = lyr.getSpatialCache();
			int i;

			//En OS X con renderer Quartz (JRE<6), mezclar setRGB con dibujado geometrico en mismo BufferedImage
			//provoca ralentizaci—n brutal. Lo evitamos separando los setRGB en otro BufferedImage y juntandolos luego.
			boolean MAC_OS_X = OsUtils.isMac();
			BufferedImage auxBI = null;
			if (MAC_OS_X) {
				auxBI = new BufferedImage(image.getWidth(), image.getHeight(), image.getType());
			}

			double dist1pixel=viewPort.getDist1pixel();
			for (int aux = 0; aux < sc; aux++) {

				// Salimos si alguien cancela
				if (cancel != null) {
					// azabala (por si acaso, al arreglar bug de process)
					if (cancel.isCanceled()) {
						break;
					}
				}
				if (bUseSpatialIndex) {
					Integer idRec = (Integer) lstIndexes.get(aux);
					i = idRec.intValue();
				} else {
					i = aux;
				}


				if (getCapa().isEditing())
					bounds=adapter.getShape(i).getBounds2D();
				else
					bounds = shapeBounds.getShapeBounds(i);

				if (bounds==null)
					continue;

				if (ct != null) {
					bounds = ct.convert(bounds);
				}


				if (XRectangle2D.intersectInclusive(extent, bounds)) {
					ISymbol symbol = l.getSymbol(i);

					if (symbol == null)
						continue;

					if (bitSet.get(i)) {
						symbol = symbol.getSymbolForSelection();
					}

					boolean bPoint = ((shapeBounds.getShapeType(i) == FShape.POINT) ||
													(shapeBounds.getShapeType(i) == (FShape.POINT | FShape.Z)));

					boolean draw = bPoint		|| ((bounds.getHeight() > dist1pixel) || (bounds
							.getWidth() > dist1pixel));
					if (draw) {
						geom = adapter.getShape(i);

						// PRUEBA DE VELOCIDAD
						// geom = ShapeFactory.createPolygon2D(new
						// GeneralPathX(bounds));


						//JMVIVO: OJO, No colnamos siempre porque
						// el FGeometry.drawInt (a diferencia del
						// FGeometry.draw) clona siempre la geometria
						// antes de pintarla (para transforma a enteros)
						if (ct != null) {
							if (bMustClone)
								geom = geom.cloneGeometry();
							geom.reProject(ct);
						}
						if (lyr.isSpatialCacheEnabled()) {
							if (cache.getMaxFeatures() >= cache.size()) {
								// Ya reproyectado todo
								cache.insert(bounds, geom);
							}
						}


						// FJP: CAMBIO: Sabemos que vamos a dibujar sobre una
						// imagen, con coordenadas enteras, así
						// que lo tenemos en cuenta.
						// ANTES: geom.draw(g, viewPort, symbol);
						// AHORA:
						geom.drawInts(g, viewPort, symbol, null);
						// geom.draw(g, viewPort, symbol);
						/*
						 * if (lyr.isEditing()) { if (bitSet.get(i)) { Handler[]
						 * handlers = geom.getHandlers(IGeometry.SELECTHANDLER);
						 * FGraphicUtilities.DrawHandlers(g,
						 * viewPort.getAffineTransform(), handlers); } }
						 */

					} else {

						Point2D.Double pOrig = new Point2D.Double(bounds
								.getMinX(), bounds.getMinY());
						Point2D pDest, pDest2;

						pDest = viewPort.getAffineTransform().transform(pOrig,
								null);
						pDest2 = g.getTransform().transform(pDest, null);

						int pixX = (int) pDest2.getX();
						int pixY = (int) pDest2.getY();
						if (symbol == null)
							continue;
						if ((pixX > 0) && (pixX < image.getWidth())) {
							if ((pixY > 0) && (pixY < image.getHeight())) {
								if (MAC_OS_X) {
									auxBI.setRGB(pixX, pixY, symbol.getOnePointRgb());
								} else {
									image.setRGB(pixX, pixY, symbol.getOnePointRgb());
								}
							}
						}
					}
				}
			}

			if (MAC_OS_X) {
				g.drawImage(auxBI,0,0,null);
			}

			// logger.debug("getCapa().getRecordset().stop()");
			((FLyrVect) getCapa()).getSource().getRecordset().stop();

			long t2 = System.currentTimeMillis();
			// logger.debug("adapter.stop()");
			adapter.stop();

			// System.out.println(t2 - t1);
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		} catch (InitializeDriverException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		}
	}

	/**
	 * Método utilizado para dibujar sobre el graphics que se pasa como
	 * parámetro, pensado para utilizarse para imprimir.
	 *
	 * @param g
	 *            Graphics2D
	 * @param viewPort
	 *            ViewPort.
	 * @param cancel
	 */
	public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel, PrintRequestAttributeSet printProperties)
			throws ReadDriverException {
		// super.draw(null, g, viewPort, cancel); // Quiero ejecutar el draw del
		// padre, que es el que va sin acelaración!!
		try {
			FLyrVect lyr = (FLyrVect) getCapa();
			ReadableVectorial adapter = lyr.getSource();
			int shapeCount=adapter.getShapeCount();
			if (shapeCount <= 0) {
//				logger.info("Layer:" + getCapa().getName() + " sin registros");
				return;
			}


			SelectableDataSource selectable = lyr.getRecordset();
			ICoordTrans ct = lyr.getCoordTrans();
			BitSet bitSet = selectable.getSelection();
			//BoundedShapes shapeBounds = (BoundedShapes) adapter.getDriver();
			VectorialDriver driver = adapter
					.getDriver();
			adapter.start();
			IGeometry geom;

//			if (shapeCount > 0) {
//				geom = adapter.getShape(0);
//			}
			IVectorLegend l = (IVectorLegend) ((ClassifiableVectorial) lyr)
					.getLegend();

			Rectangle2D extent = viewPort.getAdjustedExtent();

			int sc;

			Rectangle2D bounds;

			sc = adapter.getShapeCount();

			selectable.start();

			// TODO: A revisar si es o no conveniente este sistema
			// de comunicación con los drivers.
			DriverAttributes attr = adapter.getDriverAttributes();
			boolean bMustClone = false;
			if (attr != null) {
				if (attr.isLoadedInMemory()) {
					bMustClone = attr.isLoadedInMemory();
				}
			}

			List lstIndexes = null;
			ISpatialIndex isi=lyr.getISpatialIndex();
			boolean bUseSpatialIndex = false;
			if (isi != null) {
				if (isSpatialIndexNecessary(extent)) {
					lstIndexes = isi.query(extent);
					if (ct != null) {
						Rectangle2D newExtent = ct.getInverted()
								.convert(extent);
						lstIndexes = isi.query(newExtent);
					} else {
						lstIndexes = isi.query(extent);
					}
					sc = lstIndexes.size();
					bUseSpatialIndex = true;
				}// if
			}// if

			int i;
			for (int aux = 0; aux < sc; aux++) {
				if (bUseSpatialIndex) {
					Integer idRec = (Integer) lstIndexes.get(aux);
					i = idRec.intValue();
				} else {
					i = aux;
				}

				bounds = ((BoundedShapes)driver).getShapeBounds(i);

				if (ct != null) {
					bounds = ct.convert(bounds);
				}

				if (XRectangle2D.intersectInclusive(extent, bounds)) {
					ISymbol symbol = l.getSymbol(i);
					if (symbol == null)
						continue;
					if (bitSet.get(i)) {
						symbol = symbol.getSymbolForSelection();
					}

					//boolean bPoint = (shapeBounds.getShapeType(i) == FShape.POINT);

					geom = driver.getShape(i);

					// PRUEBA DE VELOCIDAD
					// geom = ShapeFactory.createPolygon2D(new
					// GeneralPathX(bounds));

					// JMVIVO: Clonamos siempre que sea necesario y no
					// solo si hay que reproyectar. Porque el FGeometry.draw
					// va a aplicar la transformacion sobre la geometria original
						if (bMustClone)
							geom = geom.cloneGeometry();

					if (ct != null) {
						geom.reProject(ct);
					}

					geom.draw(g, viewPort, symbol, cancel);

				}
			}

			selectable.stop();

			adapter.stop();

		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		} catch (InitializeDriverException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		}


	}

	public FBitSet queryByShape(IGeometry g, int relationship)
			throws ReadDriverException, VisitorException {
		return queryByShape(g, relationship, null);
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.operations.strategies.Strategy#queryByShape(com.iver.cit.gvsig.fmap.core.IGeometry,
	 *      int)
	 */
	public FBitSet queryByShape(IGeometry g, int relationship,
			CancellableMonitorable cancel) throws ReadDriverException, VisitorException {
		// Si hay un índice espacial, lo usamos para hacer el query.
		FLyrVect lyr = (FLyrVect) capa;
		// if (lyr.getSpatialIndex() == null) AZABALA
		if (lyr.getISpatialIndex() == null)
			return super.queryByShape(g, relationship, null);

		ReadableVectorial va = lyr.getSource();
		ICoordTrans ct = lyr.getCoordTrans();
		Rectangle2D bounds = g.getBounds2D();
		List lstRecs = lyr.getISpatialIndex().query(bounds);
		Integer idRec;
		FBitSet bitset = new FBitSet();
		Geometry jtsShape = g.toJTSGeometry();
		IntersectionMatrix m;
		int index;
		try {
			va.start();

			for (int i = 0; i < lstRecs.size(); i++) {
				if (cancel != null) {
					cancel.reportStep();
					if (cancel.isCanceled()) {
						break;
					}
				}
				idRec = (Integer) lstRecs.get(i);
				index = idRec.intValue();
				IGeometry geom = va.getShape(index);
				if(geom == null)
					continue;
				if (ct != null) {
					geom.reProject(ct);
				}
				Geometry jtsGeom = geom.toJTSGeometry();
				switch (relationship) {
				case CONTAINS:
					m = jtsShape.relate(jtsGeom);
					if (m.isContains()) {
						bitset.set(index, true);
					}
					break;

				case CROSSES:
					m = jtsShape.relate(jtsGeom);
					if (m.isCrosses(jtsGeom.getDimension(), jtsShape
							.getDimension())) {
						bitset.set(index, true);
					}
					break;

				case DISJOINT:
					// TODO: CREO QUE EL DISJOINT NO SE PUEDE METER AQUI
					m = jtsShape.relate(jtsGeom);
					if (m.isDisjoint()) {
						bitset.set(index, true);
					}
					break;

				case EQUALS:
					m = jtsShape.relate(jtsGeom);
					if (m.isEquals(jtsGeom.getDimension(), jtsShape
							.getDimension())) {
						bitset.set(index, true);
					}
					break;

				case INTERSECTS:
					m = jtsShape.relate(jtsGeom);
					if (m.isIntersects()) {
						bitset.set(index, true);
					}
					break;

				case OVERLAPS:
					m = jtsShape.relate(jtsGeom);
					if (m.isOverlaps(jtsGeom.getDimension(), jtsShape
							.getDimension())) {
						bitset.set(index, true);
					}

					break;

				case TOUCHES:
					m = jtsShape.relate(jtsGeom);
					if (m.isTouches(jtsGeom.getDimension(), jtsShape
							.getDimension())) {
						bitset.set(index, true);
					}

					break;

				case WITHIN:
					m = jtsShape.relate(jtsGeom);
					if (m.isWithin()) {
						bitset.set(index, true);
					}

					break;
				}
			}
			va.stop();
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		} catch (InitializeDriverException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		}
		long t2 = System.currentTimeMillis();
//		logger.info("queryByShape optimizado sobre la capa " + lyr.getName()
//				+ ". " + (t2 - t1) + " mseg.");
		return bitset;
	}

	public FBitSet queryByRect(Rectangle2D rect, CancellableMonitorable cancel)
			throws ReadDriverException, VisitorException {
		// Si hay un índice espacial, lo usamos para hacer el query.
		FLyrVect lyr = (FLyrVect) capa;
		if (lyr.getISpatialIndex() == null)
			return super.queryByRect(rect, cancel);

		ReadableVectorial va = lyr.getSource();
		ICoordTrans ct = lyr.getCoordTrans();
		Rectangle2D bounds = rect;
		List lstRecs = lyr.getISpatialIndex().query(bounds);
		Integer idRec;
		FBitSet bitset = new FBitSet();
		int index;
		try {
			va.start();
			DriverAttributes attr = va.getDriverAttributes();
			boolean bMustClone = false;
			if (attr != null) {
				if (attr.isLoadedInMemory()) {
					bMustClone = attr.isLoadedInMemory();
				}
			}

			for (int i = 0; i < lstRecs.size(); i++) {
				if (cancel != null) {
					cancel.reportStep();
					if (cancel.isCanceled()) {
						va.stop();
						return bitset;
					}
				}
				idRec = (Integer) lstRecs.get(i);
				index = idRec.intValue();
				IGeometry geom = va.getShape(index);
				if(geom == null)//azabala
					continue;
				if (ct != null) {
					if (bMustClone)
						geom = geom.cloneGeometry();
					geom.reProject(ct);
				}
				if (geom.intersects(rect))
					bitset.set(index, true);
			}
			va.stop();
		} catch (ExpansionFileReadException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		} catch (InitializeDriverException e) {
			throw new ReadDriverException(getCapa().getName(),e);
		}
		return bitset;

	}

	public void process(FeatureVisitor visitor, Rectangle2D rectangle)
			throws ReadDriverException, ExpansionFileReadException, VisitorException {
		process(visitor, rectangle, null);
	}

	/**
	 * Processes (by calling visitor.process() method) only those features of
	 * the vectorial layer associated which intersects given rectangle2d.
	 * @throws ExpansionFileReadException
	 *
	 */

	public void process(FeatureVisitor visitor, Rectangle2D rectangle,
			CancellableMonitorable cancel) throws ReadDriverException, ExpansionFileReadException, VisitorException {
		if (visitor.start(capa)) {
			FLyrVect lyr = (FLyrVect) capa;
			// if we dont have spatial index or...
			if (lyr.getISpatialIndex() == null) {
				super.process(visitor, rectangle, cancel);
				return;
			}
			// if spatial index is not worthy
				if (!isSpatialIndexNecessary(rectangle)) {
					super.process(visitor, rectangle, cancel);
					return;
				}

			ReadableVectorial va = lyr.getSource();
			ICoordTrans ct = lyr.getCoordTrans();
			Rectangle2D bounds = rectangle;
			List lstRecs = lyr.getISpatialIndex().query(bounds);
			Integer idRec;
			int index;
				try {
					va.start();
				}catch (InitializeDriverException e) {
					throw new ReadDriverException(getCapa().getName(),e);
				}
				DriverAttributes attr = va.getDriverAttributes();
				boolean bMustClone = false;
				if (attr != null) {
					if (attr.isLoadedInMemory()) {
						bMustClone = attr.isLoadedInMemory();
					}
				}

				for (int i = 0; i < lstRecs.size(); i++) {
					if (cancel != null) {
						cancel.reportStep();
					}
					if (verifyCancelation(cancel, va, visitor))
						return;
					idRec = (Integer) lstRecs.get(i);
					index = idRec.intValue();
					IGeometry geom = va.getShape(index);
					if(geom == null)//azabala
						continue;
					if (ct != null) {
						if (bMustClone)
							geom = geom.cloneGeometry();
						geom.reProject(ct);
					}
					if (geom.intersects(rectangle))
						visitor.visit(geom, index);
				}// for
				va.stop();

		}// if visitor.start
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see com.iver.cit.gvsig.fmap.operations.strategies.Strategy#queryByPoint(java.awt.geom.Point2D,
	 *      double)
	 */
	public FBitSet queryByPoint(Point2D p, double tolerance,
			CancellableMonitorable cancel) throws ReadDriverException, VisitorException {
		// TODO: OJO!!!!. Está implementado como un rectangulo.
		// Lo correcto debería ser calculando las distancias reales
		// es decir, con un círculo.
		Rectangle2D recPoint = new Rectangle2D.Double(p.getX()
				- (tolerance / 2), p.getY() - (tolerance / 2), tolerance,
				tolerance);
		return queryByRect(recPoint, cancel);
	}
}
