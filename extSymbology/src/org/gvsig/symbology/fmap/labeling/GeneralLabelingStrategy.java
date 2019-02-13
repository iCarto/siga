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
 * $Id: GeneralLabelingStrategy.java 13749 2007-09-17 14:16:11Z jaume $
 * $Log$
 * Revision 1.2  2007-09-17 14:16:11  jaume
 * multilayer symbols sizing bug fixed
 *
 * Revision 1.1  2007/05/22 12:17:41  jaume
 * *** empty log message ***
 *
 * Revision 1.1  2007/05/22 10:05:31  jaume
 * *** empty log message ***
 *
 * Revision 1.10  2007/05/17 09:32:06  jaume
 * *** empty log message ***
 *
 * Revision 1.9  2007/05/09 11:04:58  jaume
 * refactored legend hierarchy
 *
 * Revision 1.8  2007/04/13 11:59:30  jaume
 * *** empty log message ***
 *
 * Revision 1.7  2007/04/12 14:28:43  jaume
 * basic labeling support for lines
 *
 * Revision 1.6  2007/04/11 16:01:08  jaume
 * maybe a label placer refactor
 *
 * Revision 1.5  2007/04/10 16:34:01  jaume
 * towards a styled labeling
 *
 * Revision 1.4  2007/04/02 16:34:56  jaume
 * Styled labeling (start commiting)
 *
 * Revision 1.3  2007/03/28 16:48:01  jaume
 * *** empty log message ***
 *
 * Revision 1.2  2007/03/26 14:40:38  jaume
 * added print method (BUT UNIMPLEMENTED)
 *
 * Revision 1.1  2007/03/20 16:16:20  jaume
 * refactored to use ISymbol instead of FSymbol
 *
 * Revision 1.2  2007/03/09 11:20:57  jaume
 * Advanced symbology (start committing)
 *
 * Revision 1.1  2007/03/09 08:33:43  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.5  2007/02/21 07:34:08  jaume
 * labeling starts working
 *
 * Revision 1.1.2.4  2007/02/15 16:23:44  jaume
 * *** empty log message ***
 *
 * Revision 1.1.2.3  2007/02/09 07:47:05  jaume
 * Isymbol moved
 *
 * Revision 1.1.2.2  2007/02/02 16:21:24  jaume
 * start commiting labeling stuff
 *
 * Revision 1.1.2.1  2007/02/01 17:46:49  jaume
 * *** empty log message ***
 *
 *
 */
package org.gvsig.symbology.fmap.labeling;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;

import org.apache.log4j.Logger;
import org.gvsig.symbology.fmap.labeling.parse.LabelExpressionParser;
import org.gvsig.symbology.fmap.labeling.parse.ParseException;
import org.gvsig.symbology.fmap.labeling.placements.ILabelPlacement;
import org.gvsig.symbology.fmap.labeling.placements.LinePlacementConstraints;
import org.gvsig.symbology.fmap.labeling.placements.MultiShapePlacementConstraints;
import org.gvsig.symbology.fmap.labeling.placements.PointPlacementConstraints;
import org.gvsig.symbology.fmap.labeling.placements.PolygonPlacementConstraints;
import org.gvsig.symbology.fmap.labeling.placements.RemoveDuplicatesComparator;
import org.gvsig.symbology.fmap.rendering.filter.operations.Expression;
import org.gvsig.symbology.fmap.rendering.filter.operations.ExpressionException;
import org.gvsig.symbology.fmap.symbols.SmartTextSymbol;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.CartographicSupportToolkit;
import com.iver.cit.gvsig.fmap.core.FNullGeometry;
import com.iver.cit.gvsig.fmap.core.FPoint2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IFeature;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ShapeFactory;
import com.iver.cit.gvsig.fmap.drivers.IFeatureIterator;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingMethod;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.IPlacementConstraints;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.IZoomConstraints;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelLocationMetrics;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelingFactory;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;
import com.vividsolutions.jts.geom.TopologyException;

/**
 *
 * GeneralLabelingStrategy.java
 *
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es Jan 4, 2008
 *
 */
public class GeneralLabelingStrategy implements ILabelingStrategy, Cloneable,CartographicSupport {
	public static IPlacementConstraints DefaultPointPlacementConstraints = new PointPlacementConstraints();
	public static IPlacementConstraints DefaultLinePlacementConstraints = new LinePlacementConstraints();
	public static IPlacementConstraints DefaultPolygonPlacementConstraints = new PolygonPlacementConstraints();
	private static String[] NO_TEXT = {PluginServices.getText(null, "text_field")};
	private static MultiShapePlacementConstraints DefaultMultiShapePlacementConstratints = new MultiShapePlacementConstraints();
	private ILabelingMethod method;
	private IPlacementConstraints placementConstraints;
	protected FLyrVect layer;
	private IZoomConstraints zoomConstraints;
	private boolean allowOverlapping;
	private long parseTime;
	private int unit;
	private int referenceSystem;
	private double sizeAfter;
	private boolean printMode = false; /* indicate whether output is for a print product (PDF, PS, ...) */

    private static Logger logger = Logger
            .getLogger(GeneralLabelingStrategy.class.getName());

	public void setLayer(FLayer layer) throws ReadDriverException {
		FLyrVect l = (FLyrVect) layer;
		this.layer = l;
	}

	public ILabelingMethod getLabelingMethod() {
		return method;
	}

	public void setLabelingMethod(ILabelingMethod method) {
		this.method = method;
	}

	private class GeometryItem{
		public IGeometry geom = null;
		public int weigh = 0;
		public double savedPerimeter;

		public GeometryItem(IGeometry geom, int weigh){
			this.geom = geom;
			this.weigh = weigh;
			this.savedPerimeter = 0;
		}
	}

    public void draw(BufferedImage mapImage, Graphics2D mapGraphics,
            ViewPort viewPort, Cancellable cancel, double dpi)
            throws ReadDriverException {
        draw(mapImage, mapGraphics, viewPort, cancel, dpi, false);
    }

	public void draw(BufferedImage mapImage, Graphics2D mapGraphics,
            ViewPort viewPort, Cancellable cancel, double dpi,
            boolean checkIntersection) throws ReadDriverException {
		int x = (int)viewPort.getOffset().getX();
		int y = (int)viewPort.getOffset().getY();
//		boolean bVisualFXEnabled = false; // if true, the user can see how the labeling is drawing up

		//offsets for page generation (PDF, PS, direct printing)
		int print_offset_x = x;
		int print_offset_y = y;
		if (printMode) {
			//for printing, we never offset the labels themselves
			x = 0;
			y = 0;
			printMode = false;
		}

		TreeMap<String[], GeometryItem> labelsToPlace = null;
		parseTime =0;
//		long t1 = System.currentTimeMillis();
		String[] usedFields = getUsedFields();

		int notPlacedCount = 0;
		int placedCount = 0;

		/*
		 * Get the label placement solvers according the user's settings
		 */
		ILabelPlacement placement = PlacementManager.getPlacement(getPlacementConstraints(), layer.getShapeType());


		BufferedImage targetBI;
		Graphics2D targetGr;


		/*
		 * get an ordered set of the LabelClasses up on the
		 * label priority
		 */
		LabelClass[] lcs = method.getLabelClasses();
		TreeSet<LabelClass> ts = new TreeSet<LabelClass>(new LabelClassComparatorByPriority());

		for (int i = 0; i < lcs.length; i++) ts.add(lcs[i]);

		if (ts.size()==0) return;

		/*
		 * now we have an ordered set, it is only need to give a pass
		 * for each label class to render by priorities.
		 *
		 * If no priorities were defined, the following loop only executes
		 * once
		 */
		for (LabelClass lc : ts) {
			IFeatureIterator it =  method.getFeatureIteratorByLabelClass(layer, lc, viewPort, usedFields);

			// duplicates treatment stuff
			/* handle the duplicates mode */
			int duplicateMode = getDuplicateLabelsMode();
			if (duplicateMode == IPlacementConstraints.REMOVE_DUPLICATE_LABELS) {
				// we need to register the labels already placed

				labelsToPlace = new TreeMap<String[], GeometryItem>(new RemoveDuplicatesComparator());
			}

			boolean bLabelsReallocatable = !isAllowingOverlap();

			BufferedImage overlapDetectImage = null;
			Graphics2D overlapDetectGraphics = null;
			if (bLabelsReallocatable) {
                                int width, height;
                                if (mapImage != null) {
                                    width = mapImage.getWidth() + print_offset_x;
                                    height = mapImage.getHeight() + print_offset_y;
                                } else {
                                    width = viewPort.getImageWidth() + print_offset_x;
                                    height = viewPort.getImageHeight() + print_offset_y;
                                }
                                if (width < 0) {
                                    width = 1;
                                }
                                if (height < 0) {
                                    height = 1;
                                }
                                overlapDetectImage = new BufferedImage(width, height,
                                        BufferedImage.TYPE_INT_ARGB);
				overlapDetectGraphics = overlapDetectImage.createGraphics();
				overlapDetectGraphics.setRenderingHints(mapGraphics.getRenderingHints());
			}
			if (bLabelsReallocatable) {
				targetBI = overlapDetectImage;
				targetGr = overlapDetectGraphics;
				targetGr.translate(-x, -y);
			} else {
				targetBI = mapImage;
				targetGr = mapGraphics;
			}

			while ( !cancel.isCanceled() && it.hasNext()) {
				IFeature feat = it.next();
				IGeometry geom = feat.getGeometry();
				if (geom==null || geom instanceof FNullGeometry) // we don't need to label null geometries
					continue;

                // In order to make files smaller, we won't print
                // labels for geometries that don't seem to be in
                // the view
                Rectangle2D extent = viewPort.getExtent();
                try {
                if (checkIntersection
                        && !geom.fastIntersects(extent.getX(), extent.getY(),
                                extent.getWidth(), extent.getHeight())) {
                    continue;
                }
                } catch (TopologyException e) {
                    logger.warn(
                            "Some error happened while trying to cut a polygon with the view extent before printing its label (layer '"
                                    + this.layer.getName()
                                    + "' / feature id "
                                    + feat.getID()
                                    + "). The label will be drawn in any case. ",
                            e);
                }

				if (!setupLabel(feat, lc, cancel, usedFields, viewPort, dpi, duplicateMode)){ //, placedLabels)){
					continue;
				}

				String[] texts = lc.getTexts();
//				System.out.println(texts[0]);
				if (duplicateMode == IPlacementConstraints.REMOVE_DUPLICATE_LABELS) {
					// check if this text (so label) is already present in the map

					GeometryItem item = labelsToPlace.get(texts);
					if (item == null){
						item = new GeometryItem(geom, 0);
						labelsToPlace.put(texts, item);
					}
					if (item.geom != null){
						notPlacedCount++;
						if(geom.getGeometryType() != FShape.POINT){
							// FJP: Cambiamos la unión por una comprobación de longitud, por ejemplo.
							// La geometría con mayor longitud del bounding box será la que etiquetamos.
							// Será inexacto, pero más rápido. Solo lo queremos para saber qué entidad etiquetamos
							// El problema con la unión es que para líneas va muy mal (no sabes lo que te va
							// a etiquetar, y para polígonos será muy lenta. De todas formas, habría que evitar
							// la conversión al JTS.
//							Geometry jtsGeom = item.geom.toJTSGeometry().union(geom.toJTSGeometry());
//							if (jtsGeom instanceof LineString) {
//								CoordinateSequence cs = ((LineString) (jtsGeom)).getCoordinateSequence();
//								CoordinateSequences.reverse(cs);
//								jtsGeom = new LineString(cs, null);
//							}
//							item.geom = FConverter.jts_to_igeometry(jtsGeom);

							Rectangle2D auxBox = geom.getBounds2D();
							double perimeterAux = 2*auxBox.getWidth() + 2*auxBox.getHeight();
							if (perimeterAux > item.savedPerimeter) {
								item.geom = geom; //FConverter.jts_to_igeometry(jtsGeom);
								item.savedPerimeter = perimeterAux;
							}
						} else {
							int weigh = item.weigh;
							FPoint2D pointFromLabel = (FPoint2D)item.geom.getInternalShape();
							item.geom = ShapeFactory.createPoint2D(
									(pointFromLabel.getX()*weigh + ((FPoint2D)geom.getInternalShape()).getX())/(weigh+1),
									(pointFromLabel.getY()*weigh + ((FPoint2D)geom.getInternalShape()).getY())/(weigh+1));
						}
					} else {
						item.geom = geom;
					}
					item.weigh++;
				} else {
					// Check if size is a pixel
					if (isOnePoint(viewPort, geom)) {
						continue;
					}
//					lc.toCartographicSize(viewPort, dpi, null);

					drawLabelInGeom(targetBI, targetGr, lc, placement, viewPort, geom, cancel, dpi, bLabelsReallocatable);
					placedCount++;
				}
			}
			if (duplicateMode == IPlacementConstraints.REMOVE_DUPLICATE_LABELS) {
				Iterator<String[]> textsIt = labelsToPlace.keySet().iterator();
				while ( !cancel.isCanceled() && textsIt.hasNext()) {
					notPlacedCount++;
					String[] texts = textsIt.next();

					GeometryItem item = labelsToPlace.get(texts);
					if(item != null){
						lc.setTexts(texts);
						// Check if size is a pixel
						if (isOnePoint(viewPort, item.geom)) {
							continue;
						}

//						lc.toCartographicSize(viewPort, dpi, null);
						drawLabelInGeom(targetBI, targetGr, lc, placement, viewPort, item.geom, cancel, dpi, bLabelsReallocatable);
					}
				}
			}

			if (bLabelsReallocatable) {
				targetGr.translate(x, y);
				if (mapImage!=null)
					((Graphics2D)mapImage.getGraphics()).drawImage(overlapDetectImage, null, null);
				else
					mapGraphics.drawImage(overlapDetectImage, null, null);
			}


		}
//		double totalTime = (System.currentTimeMillis()-t1);
//
//		int total = placedCount+notPlacedCount;
//
//		if (total>0)
//		Logger.getLogger(getClass()).info("Labeled layer '"+layer.getName()+
//		"' "+totalTime/1000D+" seconds. "+placedCount+"/"+total+
//		" labels placed ("+NumberFormat.getInstance().
//		format(100*placedCount/(double) total)+"%)");
//
//		if (cancel.isCanceled()) {
//			Logger.getLogger(getClass()).info("Layer labeling canceled: '"+
//					layer.getName()+"'");
//		} else {
//			Logger.getLogger(getClass()).info("Total labels parse time = "+
//					parseTime+" ("+NumberFormat.getInstance().
//					format(parseTime*100/totalTime)+"%)");
//		}

	}
	private void drawLabelInGeom(BufferedImage targetBI, Graphics2D targetGr, LabelClass lc,
			ILabelPlacement placement, ViewPort viewPort, IGeometry geom, Cancellable cancel,
			double dpi, boolean bLabelsReallocatable){

		lc.toCartographicSize(viewPort, dpi, null);

		ArrayList<LabelLocationMetrics> llm = null;

		llm = placement.guess(
				lc,
				geom,
				getPlacementConstraints(),
				0,
				cancel,viewPort);

		setReferenceSystem(lc.getReferenceSystem());
		setUnit(lc.getUnit());

		/*
		 *  Esto provoca errores en el calculo del tamaño de la LabelClass
		 * así que se ha diferido el calculo del tamaño con que debería
		 * dibujarse el texto justo hasta el momento en que se dibuja éste,
		 * en el metodo draw de la LabelClass.
		 *
		 * FIXME: Mantengo el código viejo comentarizado para enfatizar
		 * este comentario. Eliminar cuando se asuma que es correcto el cambio.
		 *
		 */

//		double sizeBefore = lc.getTextSymbol().getFont().getSize();

//		sizeAfter = CartographicSupportToolkit.getCartographicLength(this,
//				sizeBefore,
//				viewPort,
//				MapContext.getScreenDPI());


		/*
		 * search if there is room left by the previous and
		 * with more priority labels, then check the current
		 * level
		 */
//		if (
		lookupAndPlaceLabel(targetBI, targetGr, llm,
				placement, lc, geom, viewPort, cancel,	bLabelsReallocatable);  //{

//			lc.getTextSymbol().setFontSize(sizeBefore);
//		}
//		lc.toCartographicSize(viewPort, dpi, null);

	}

	private int getDuplicateLabelsMode() {
		if (getPlacementConstraints() == null) {
			return IPlacementConstraints.DefaultDuplicateLabelsMode;
		}
		return getPlacementConstraints().getDuplicateLabelsMode();
	}

	private boolean lookupAndPlaceLabel(BufferedImage bi, Graphics2D g,
			ArrayList<LabelLocationMetrics> llm, ILabelPlacement placement,
			LabelClass lc, IGeometry geom,	ViewPort viewPort,
			Cancellable cancel, boolean bLabelsReallocatable) {
		int i;

		for (i = 0; !cancel.isCanceled() && i < llm.size(); i++) {
			LabelLocationMetrics labelMetrics = llm.get(i);

			IPlacementConstraints pc = getPlacementConstraints();
			if(pc instanceof MultiShapePlacementConstraints){
				MultiShapePlacementConstraints mpc = (MultiShapePlacementConstraints)pc;
				int shapeType = geom.getGeometryType();
				switch (shapeType % FShape.Z) {
				case FShape.POINT:
					pc = mpc.getPointConstraints();
					break;
				case FShape.LINE:
					pc = mpc.getLineConstraints();
					break;
				case FShape.POLYGON:
					pc = mpc.getPolygonConstraints();
					break;
				}
			}

			/*
			 * Ver comentario en el metodo drawLabelInGeom
			 */
//			lc.getTextSymbol().setFontSize(sizeAfter);// * FConstant.FONT_HEIGHT_SCALE_FACTOR);
			if (bLabelsReallocatable) {
				if (!isOverlapping(bi, lc.getShape(labelMetrics))) {

					if(!pc.isFollowingLine()){
						lc.draw(g, labelMetrics, (FShape) geom.getInternalShape());
					}
					else{
						SmartTextSymbolLabelClass smsLc = new SmartTextSymbolLabelClass();
						SmartTextSymbol sms = new SmartTextSymbol(lc.getTextSymbol(),pc);

						double sizeBefore = lc.getTextSymbol().getFont().getSize();
						double sizeAfter = CartographicSupportToolkit.getCartographicLength(this,
								sizeBefore,
								viewPort,
								MapContext.getScreenDPI());
						sms.setFontSize(sizeAfter);

						smsLc.setTextSymbol(sms);
						geom.transform(viewPort.getAffineTransform());
						smsLc.draw(g, null, (FShape) geom.getInternalShape());
						sms.setFontSize(sizeBefore);

					}
					return true;
				}
			} else {
				if(!pc.isFollowingLine()){
					lc.draw(g, labelMetrics, null);
				}
				else{
					SmartTextSymbolLabelClass smsLc = new SmartTextSymbolLabelClass();
					SmartTextSymbol sms = new SmartTextSymbol(lc.getTextSymbol(),pc);

					double sizeBefore = lc.getTextSymbol().getFont().getSize();
					double sizeAfter = CartographicSupportToolkit.getCartographicLength(this,
							sizeBefore,
							viewPort,
							MapContext.getScreenDPI());
					sms.setFontSize(sizeAfter);

					smsLc.setTextSymbol(sms);
					geom.transform(viewPort.getAffineTransform());
					smsLc.draw(g, null, (FShape) geom.getInternalShape());

					sms.setFontSize(sizeBefore);
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Divide una cadena de caracteres por el caracter dos puntos siempre que no esté entre comillas.
	 *
	 * @param str
	 *            Cadena de caracteres
	 *
	 * @return String[]
	 *
	 */
	private String[] divideExpression(String str){
		ArrayList<String> r = new ArrayList<String>();
		boolean inQuotationMarks = false;
		int lastIndex = 0;
		for(int i=0; i<str.length(); i++){
			if(str.substring(i, i+1).compareTo("\"")==0){
				inQuotationMarks = !inQuotationMarks;
				continue;
			}
			if(str.substring(i, i+1).compareTo(":")==0 && !inQuotationMarks){
				if(lastIndex < i){
					r.add(str.substring(lastIndex, i));
				}
				lastIndex = i+1;
			}
		}
		if(lastIndex < str.length()-1){
			r.add(str.substring(lastIndex));
		}
		String[] result = new String[r.size()];
		r.toArray(result);
		return result;
	}

	/**
	 * Compute the texts to show in the label and store them in LabelClass.
	 */
	@SuppressWarnings("unchecked")
	private boolean setupLabel(IFeature feat, LabelClass lc,
			Cancellable cancel, String[] usedFields, ViewPort viewPort,
			double dpi, int duplicateMode){//, TreeSet<?> placedLabels) {

//		Value[] vv = feat.getAttributes();
		String expr = lc.getStringLabelExpression();

		long pt1 = System.currentTimeMillis();
		String[] texts = NO_TEXT;
		ArrayList<String> preTexts = new ArrayList<String>();
//		String[] texts = {PluginServices.getText(this, "text_field")};
		try {

			for (int i = 0; !cancel.isCanceled() && i < usedFields.length; i++) {
				try {
					int index = layer.getSource().getRecordset().getFieldIndexByName(usedFields[i]);
					if(index != -1)
						symbol_table.put(usedFields[i], feat.getAttribute(index));
				} catch (ReadDriverException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			if (expr != null) {

				if(expr.equals("") || expr.equals(LabelExpressionParser.tokenFor(LabelExpressionParser.EOEXPR)))
					expr = texts[0];

				/*
				 * FIXME: Esto es un parche.
				 * Parece que el LabelExpresionParser no es capaz de
				 * de dividir convenientemente una expression compuesta por varias expresiones
				 * separadas por ":".
				 * Habría que arreglar (con tiempo) el evaluador, pero,
				 * de momento, rodeamos el problema.
				 */
				String[] multiexpr = divideExpression(expr);
				for(int i=0; i<multiexpr.length; i++){
					expr = multiexpr[i];
					if(!expr.endsWith(";")) expr += ";";
					// parse (if it hasn't been parsed yet) and evaluate the expression
					Expression evaluator = getEvaluator(expr);
					Object labelContents = evaluator.evaluate();
					if (labelContents!=null) {
						if (String[].class.equals(labelContents.getClass())) {
							for(int j=0; j<((String[]) labelContents).length;j++){
								preTexts.add(((String[])labelContents)[j]);
							}
						} else {
							preTexts.add(labelContents.toString());
						}
					} else {
					    preTexts.add("");
					}
				}
				texts = new String[preTexts.size()];
				preTexts.toArray(texts);
				parseTime += System.currentTimeMillis()-pt1;
			}
			lc.setTexts(texts);

		} catch (ExpressionException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	private Hashtable<String, Value> symbol_table = new Hashtable<String, Value>();
	private Hashtable<String, Expression> evaluators = new Hashtable<String, Expression>();

	private Expression getEvaluator(String strExpr) {
		Expression expr = evaluators.get(strExpr);
		if (expr == null) {
			LabelExpressionParser p = new LabelExpressionParser(
					new StringReader(strExpr),symbol_table);

			try {
				p.LabelExpression();
			} catch (ParseException e) {
				e.printStackTrace();
			}
			expr = (Expression) p.getStack().pop();
			evaluators.put(strExpr, expr);
		}
		return expr;
	}

	private boolean isOverlapping(BufferedImage bi, FShape labelShape) {
		if (labelShape==null)
			return false;
		Rectangle2D rPixels = labelShape.getBounds2D();
		for (int i= (int) rPixels.getX(); i<=rPixels.getMaxX(); i++){
			for (int j= (int) rPixels.getY(); j<=rPixels.getMaxY(); j++){

				if (!labelShape.contains(i, j) // contains seems to don't detect points placed in the rectangle boundaries
						&& !labelShape.intersects(i, j, i, j)) {
					continue;
				}

				if (i<0 || j<0) {
					continue;
				}

				if (bi.getWidth()<i+1 || bi.getHeight()<j+1) {
					continue;
				}

				if (bi.getRGB(i,j)!=0){
					return true;
				}
			}
		}
		return false;
	}

	private boolean isOnePoint(ViewPort viewPort, IGeometry geom) {
		boolean onePoint = false;
		int shapeType = geom.getGeometryType();
		if ((shapeType % FShape.Z)!=FShape.POINT && (shapeType % FShape.Z) !=FShape.MULTIPOINT) {

			Rectangle2D geomBounds = geom.getBounds2D();
//			ICoordTrans ct = layer.getCoordTrans();
//
//			if (ct!=null) {
//				geomBounds = ct.convert(geomBounds);
//			}

			double dist1Pixel = viewPort.getDist1pixel();
			onePoint = (geomBounds.getWidth() <= dist1Pixel
					&& geomBounds.getHeight() <= dist1Pixel);
		}
		return onePoint;
	}

	public String getClassName() {
		return getClass().getName();
	}

	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("labelingStrategy", "labelingStrategy");
		xml.putProperty("className", getClassName());
		xml.putProperty("allowsOverlapping", allowOverlapping);
//		xml.putProperty("minScaleView", minScaleView);
//		xml.putProperty("maxScaleView", maxScaleView);

		if (method!=null) {
			XMLEntity methodEntity = method.getXMLEntity();
			methodEntity.putProperty("id", "LabelingMethod");
			xml.addChild(methodEntity);
		}

		if (placementConstraints != null) {
			XMLEntity pcEntity = placementConstraints.getXMLEntity();
			pcEntity.putProperty("id", "PlacementConstraints");
			xml.addChild(pcEntity);
		}

		if (zoomConstraints != null) {
			XMLEntity zcEntity = zoomConstraints.getXMLEntity();
			zcEntity.putProperty("id", "ZoomConstraints");
			xml.addChild(zcEntity);
		}
		return xml;
	}

	public void setXMLEntity(XMLEntity xml) {
		XMLEntity aux = xml.firstChild("id", "LabelingMethod");

		// overlapping mode
		if (xml.contains("allowsOverlapping")) {
			allowOverlapping = xml.getBooleanProperty("allowsOverlapping");
		}


		if (aux != null) {
			method = LabelingFactory.createMethodFromXML(aux);
		}

		aux = xml.firstChild("id", "PlacementConstraints");
		if (aux != null) {
			placementConstraints = LabelingFactory.createPlacementConstraintsFromXML(aux);
		}

		aux = xml.firstChild("id", "ZoomConstraints");
		if (aux != null) {
			zoomConstraints = LabelingFactory.createZoomConstraintsFromXML(aux);
		}
	}

	public boolean isAllowingOverlap() {
		return allowOverlapping;
	}

	public void setAllowOverlapping(boolean allowOverlapping) {
		this.allowOverlapping = allowOverlapping;
	}

	public IPlacementConstraints getPlacementConstraints() {
		if (placementConstraints != null)
			return placementConstraints;

		try {
			switch (layer.getShapeType() % FShape.Z) {
			case FShape.POINT:
			case FShape.MULTIPOINT:
				return DefaultPointPlacementConstraints;
			case FShape.LINE:
				return DefaultLinePlacementConstraints;
			case FShape.POLYGON:
				return DefaultPolygonPlacementConstraints;
			case FShape.MULTI:
				DefaultMultiShapePlacementConstratints.setPointConstraints(DefaultPointPlacementConstraints);
				DefaultMultiShapePlacementConstratints.setLineConstraints(DefaultLinePlacementConstraints);
				DefaultMultiShapePlacementConstratints.setPolygonConstraints(DefaultPolygonPlacementConstraints);
				return DefaultMultiShapePlacementConstratints;
			}

		} catch (ReadDriverException e) {

		}
		return null;
	}

	public void setPlacementConstraints(IPlacementConstraints constraints) {
		this.placementConstraints = constraints;
	}

	public IZoomConstraints getZoomConstraints() {
		return zoomConstraints;
	}

	public void setZoomConstraints(IZoomConstraints constraints) {
		this.zoomConstraints = constraints;
	}

	public void print(Graphics2D g, ViewPort viewPort, Cancellable cancel, PrintRequestAttributeSet properties) throws ReadDriverException {
		double dpi = 100;

		PrintQuality resolution=(PrintQuality)properties.get(PrintQuality.class);
		if (resolution.equals(PrintQuality.NORMAL)){
			dpi = 300;
		} else if (resolution.equals(PrintQuality.HIGH)){
			dpi = 600;
		} else if (resolution.equals(PrintQuality.DRAFT)){
			dpi = 72;
		}

		viewPort.setOffset(new Point2D.Double(0,0));

		/* signal printing output */
		printMode = true;

        draw(null, g, viewPort, cancel, dpi, true);
	}

	public String[] getUsedFields() {
		LabelClass[] lcs = method.getLabelClasses();
		ArrayList<String> fieldNames = new ArrayList<String>();
		for (int i = 0; i < lcs.length; i++) {
			if(lcs[i].getLabelExpressions() != null){
				for (int j = 0; j < lcs[i].getLabelExpressions().length; j++) {
					String expr = lcs[i].getLabelExpressions()[j];
					int start;
					while (expr != null &&
							(start = expr.indexOf("[")) != -1) {
						int end = expr.indexOf("]");
						String field = expr.substring(start+1, end).trim();
						if (!fieldNames.contains(field))
							fieldNames.add(field);
						expr = expr.substring(end+1, expr.length());
					}
				}
			}
		}
		return fieldNames.toArray(new String[fieldNames.size()]);
	}


	public boolean shouldDrawLabels(double scale) {
		double minScaleView = -1;
		double maxScaleView = -1;

		if (zoomConstraints != null) {
			minScaleView = zoomConstraints.getMinScale();
			maxScaleView = zoomConstraints.getMaxScale();
		}

		if (minScaleView == -1 && maxScaleView == -1) {
			// parameters not set, so the layer decides.
			return layer.isWithinScale(scale);
		}

		if (minScaleView >= scale) {
			return (maxScaleView != -1) ? maxScaleView <= scale : true;
		}

		return false;
	}

	public void setUnit(int unitIndex) {
		unit = unitIndex;

	}

	public int getUnit() {
		return unit;
	}

	public int getReferenceSystem() {
		return referenceSystem;
	}

	public void setReferenceSystem(int referenceSystem) {
		this.referenceSystem = referenceSystem;
	}

	public double toCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		// TODO Auto-generated method stub
		return 0;
	}

	public void setCartographicSize(double cartographicSize, FShape shp) {
		// TODO Auto-generated method stub

	}

	public double getCartographicSize(ViewPort viewPort, double dpi, FShape shp) {
		// TODO Auto-generated method stub
		return 0;
	}

}
