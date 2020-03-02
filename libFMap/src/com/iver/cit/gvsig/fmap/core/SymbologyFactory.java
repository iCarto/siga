/* gvSIG. Geographic Information System of the Valencian Government
 *
 * Copyright (C) 2007-2008 Infrastructures and Transports Department
 * of the Valencian Government (CIT)
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
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA  02110-1301, USA.
 *
 */

/* CVS MESSAGES:
*
* $Id$
* $Log$
* Revision 1.11  2007-09-20 09:33:15  jaume
* Refactored: fixed name of IPersistAnce to IPersistence
*
* Revision 1.10  2007/09/17 14:16:11  jaume
* multilayer symbols sizing bug fixed
*
* Revision 1.9  2007/09/17 09:32:05  jaume
* view refresh frame rate now configurable
*
* Revision 1.8  2007/07/18 06:54:34  jaume
* continuing with cartographic support
*
* Revision 1.7  2007/04/26 11:41:00  jaume
* attempting to let defining size in world units
*
* Revision 1.6  2007/03/27 09:28:40  jaume
* *** empty log message ***
*
* Revision 1.5  2007/03/21 11:02:51  jaume
* javadoc
*
* Revision 1.4  2007/03/20 15:59:03  jaume
* improved factory toolkit
*
* Revision 1.3  2007/03/13 16:58:36  jaume
* Added QuantityByCategory (Multivariable legend) and some bugfixes in symbols
*
* Revision 1.2  2007/03/09 11:20:57  jaume
* Advanced symbology (start committing)
*
* Revision 1.1.2.2  2007/02/15 16:23:44  jaume
* *** empty log message ***
*
* Revision 1.1.2.1  2007/02/12 15:15:20  jaume
* refactored interval legend and added graduated symbol legend
*
* Revision 1.1.2.3  2007/02/09 07:47:05  jaume
* Isymbol moved
*
* Revision 1.1.2.2  2007/02/01 11:42:47  jaume
* *** empty log message ***
*
* Revision 1.1.2.1  2007/01/26 13:48:05  jaume
* patch for opening old projects
*
* Revision 1.1  2007/01/10 16:39:41  jaume
* ISymbol now belongs to com.iver.cit.gvsig.fmap.core.symbols package
*
* Revision 1.3  2006/11/06 16:06:52  jaume
* *** empty log message ***
*
* Revision 1.2  2006/11/06 07:33:54  jaume
* javadoc, source style
*
* Revision 1.1  2006/10/30 19:30:35  jaume
* *** empty log message ***
*
*
*/
package com.iver.cit.gvsig.fmap.core;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.geom.Ellipse2D;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

import com.iver.cit.gvsig.fmap.Messages;
import com.iver.cit.gvsig.fmap.core.styles.IStyle;
import com.iver.cit.gvsig.fmap.core.styles.SimpleLineStyle;
import com.iver.cit.gvsig.fmap.core.symbols.AbstractSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ILineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.IMultiLayerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiLayerFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiLayerLineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiLayerMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.MultiShapeSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleLineSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleMarkerSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.cit.gvsig.fmap.core.v02.FConstant;
import com.iver.cit.gvsig.fmap.core.v02.FSymbol;
import com.iver.utiles.FileUtils;
import com.iver.utiles.IPersistence;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * Factory for obtaining symbology of any kind from several sources like.
 * <ol>
 * 	<li>
 * 		<b>XMLEntity's</b> that, at least, contains a full class name
 * 			string property that defines which class handles such symbol.
 *  </li>
 * </ol>
 *
 * @author jaume dominguez faus - jaume.dominguez@iver.es
 */

public class SymbologyFactory {

	/**
 	* This Constants are only used in SymbologyPage (preferences page for symbology),
	 * do not use in other context.
	 */
	public static final Color FactoryDefaultSymbolColor = Color.GRAY;
	public static final Color FactoryDefaultFillSymbolColor = new Color(60, 235, 235);
	public static final Font FactoryDefaultTextFont =  new Font("Serif", Font.PLAIN, 8);

	public static final String FactorySymbolLibraryPath = FileUtils.getAppHomeDir() + "Symbols";
	public static final String FactoryStyleLibraryPath = FileUtils.getAppHomeDir() + "Styles";

	public static final Boolean FactoryDefaultAleatoryFillColor = false;

	public static Color DefaultSymbolColor = FactoryDefaultSymbolColor;
	public static Color DefaultFillSymbolColor = FactoryDefaultFillSymbolColor;
	public static Font DefaultTextFont = FactoryDefaultTextFont;
	public static String SymbolLibraryPath = FactorySymbolLibraryPath;
	public static String StyleLibraryPath = FactoryStyleLibraryPath;

	public static Boolean DefaultAleatoryFillColor = FactoryDefaultAleatoryFillColor;



	private static Logger logger = Logger.getLogger(SymbologyFactory.class.getName());
	private static WarningSymbol warningSymbol;

	/**
	 * Factory that allows to create <b>ISymbol</b>'s from an ISymbol xml
	 * descriptor. A barely specific XMLEntity object. The string passed in the
	 * second argument is the description text that will be used in case no description
	 * is supplied by the symbol's xml descriptor.
	 *
	 * @param xml, the symbol's xml descriptor
	 * @param defaultDescription, a human readable description string for the symbol.
	 * @return ISymbol
	 */
	public static ISymbol createSymbolFromXML(XMLEntity xml, String defaultDescription) {
		if (!xml.contains("desc")) {
			if (defaultDescription == null) defaultDescription = "";
			xml.putProperty("desc", defaultDescription);
		}
		return (ISymbol) createFromXML(xml);
	}

	/**
	 * Factory that allows to create <b>IStyle</b>'s from an <b>IStyle</b> xml descriptor. A barely
	 * specific <b>XMLEntity</b> object. The string passed in the second argument is the
	 * description text that will be used in case no description is supplied by the
	 * style's xml descriptor.
	 *
	 * @param xml, the style's xml descriptor
	 * @param defaultDescription, a human readable description string for the style
	 * @return IStyle
	 */
	public static IStyle createStyleFromXML(XMLEntity xml, String defaultDescription) {
		if (!xml.contains("desc")) {
			if (defaultDescription == null)
				defaultDescription = "";

			xml.putProperty("desc", defaultDescription);
		}
		return (IStyle) createFromXML(xml);
	}

	/**
	 * Creates an <b>Object</b> described by the <b>XMLEntity</b> xml, please reffer to the
	 * XMLEntity definition contract to know what is the format of the xml argument.
	 * The result of this method is an <b>Object</b> that you can cast to the type you were
	 * looking for by means of the xml entity.
	 *
	 * @param xml
	 * @return Object
	 */
	private static Object createFromXML(XMLEntity xml) {
		String className = null;
		try {
			className = xml.getStringProperty("className");
		} catch (NotExistInXMLEntity e) {
			logger.error("Class name not set.\n" +
						" Maybe you forgot to add the" +
						" putProperty(\"className\", yourClassName)" +
						" call in the getXMLEntity method of your class", e);
		}


		Class clazz = null;
		IPersistence obj = null;
		String s = className;

		try {
			clazz = Class.forName(className);

			if (xml.contains("desc")) {
				s += " \"" + xml.getStringProperty("desc") +"\"";
			}
			// TODO: Modify the patch the day we deprecate FSymbol
			// begin patch
			if (clazz.equals(FSymbol.class)){
				obj = deriveFSymbol(FSymbol.createFromXML(xml));
			} else {
			// end patch


				obj = (IPersistence) clazz.newInstance();
//				logger.info(Messages.getString("creating")+"....... "+s);
				try {
					obj.setXMLEntity(xml);
				} catch (NotExistInXMLEntity neiXML) {
					logger.error(Messages.getString("failed_creating_object")+": "+s);
					throw neiXML;
				}

			}

		} catch (InstantiationException e) {
			logger.error("Trying to instantiate an interface" +
						" or abstract class + "+className, e);
		} catch (IllegalAccessException e) {
			logger.error("IllegalAccessException: does your class have an" +
					" anonymous constructor?", e);
		} catch (ClassNotFoundException e) {
			logger.error("No class called " + className +
					" was found.\nCheck the following.\n<br>" +
					"\t- The fullname of the class you're looking " +
						"for matches the value in the className " +
						"property of the XMLEntity ("+className+").\n<br>" +
					"\t- The jar file containing your symbol class is in" +
						"the application classpath<br>", e);
		}
		return obj;
	}

	private static ISymbol deriveFSymbol(FSymbol fSymbol) {
		ISymbol derivedSymbol;

		int symbolType = fSymbol.getSymbolType();
		Color color = fSymbol.getColor();
		double size = fSymbol.getSize();
		if (symbolType == FShape.LINE) {
			ILineSymbol line = SymbologyFactory.createDefaultLineSymbol();
			line.setLineColor(color);

			SimpleLineStyle lineStyle = new SimpleLineStyle();
			lineStyle.setUnit(-1);//En FSymbol solo estaban contemplados metros en los simbolos puntuales
			lineStyle.setOffset(0);
			lineStyle.setReferenceSystem(CartographicSupport.PAPER);
			Stroke stroke = fSymbol.getStroke();
			BasicStroke bStroke = null;
			if(stroke instanceof BasicStroke){
				bStroke = (BasicStroke) stroke;
			}
			if(bStroke != null){
				lineStyle.setStroke(bStroke);
				lineStyle.setLineWidth(bStroke.getLineWidth());
			} else {
				lineStyle.setLineWidth((float) size);
			}

			line.setLineStyle(lineStyle);
			derivedSymbol = line;

		} else if (symbolType == FShape.POINT) {
			int unit = fSymbol.isSizeInPixels() ? -1 : 1; // only meters or pixels
			// were supported in
			// FSymbol
			int style = fSymbol.getStyle();
			IMarkerSymbol marker = new SimpleMarkerSymbol();
			SimpleMarkerSymbol sms = (SimpleMarkerSymbol) marker;
			if (style == FConstant.SYMBOL_STYLE_MARKER_CIRCLE) {
				sms.setStyle(SimpleMarkerSymbol.CIRCLE_STYLE);
			} else if (style == FConstant.SYMBOL_STYLE_MARKER_CROSS) {
				sms.setStyle(SimpleMarkerSymbol.CROSS_STYLE);
			} else if (style == FConstant.SYMBOL_STYLE_MARKER_SQUARE) {
				sms.setStyle(SimpleMarkerSymbol.SQUARE_STYLE);
			} else if (style == FConstant.SYMBOL_STYLE_MARKER_TRIANGLE) {
				sms.setStyle(SimpleMarkerSymbol.TRIANGLE_STYLE);
			} else if (style == FConstant.SYMBOL_STYLE_MARKER_IMAGEN) {
				//Como no tenemos en el core ningun simbolo de imagen devolvemos uno circular por defecto
				sms.setStyle(SimpleMarkerSymbol.CIRCLE_STYLE);
			}
			Color outlineColor = fSymbol.getOutlineColor();
			if (outlineColor != null) {
				sms.setOutlined(true);
				sms.setOutlineColor(outlineColor);
			}
			marker.setColor(color);
			marker.setSize(size);
			marker.setUnit(unit);
			marker.setRotation(fSymbol.getRotation());
			derivedSymbol = marker;

		} else if (symbolType == FShape.POLYGON) {
			IFillSymbol fill;
			int fSymbolStyle = fSymbol.getStyle();

			fill = SymbologyFactory.createDefaultFillSymbol();
			if (fSymbolStyle == FConstant.SYMBOL_STYLE_FILL_TRANSPARENT ||
					fSymbolStyle == FConstant.SYMBOL_STYLE_DGNSPECIAL) {
				color = null;
			}
			// De momento no tenemos tipos simbolos apropiados en el core de fmap para estos.

			fill.setUnit(-1);//En FSymbol solo estaban contemplados metros en los simbolos puntuales
			fill.setFillColor(color);

			if (fSymbol.isOutlined()) {
				// Let's create the outline line symbol
				fSymbol.setSymbolType(FShape.LINE);
				ILineSymbol outline = (ILineSymbol) deriveFSymbol(fSymbol);

				// restore the old value for symbol type (should be always
				// FShape.POLYGON)
				assert symbolType == FShape.POLYGON;
				fSymbol.setSymbolType(symbolType);
				outline.setLineColor(fSymbol.getOutlineColor());
				fill.setOutline(outline);
			}
			derivedSymbol = fill;
		} else if (symbolType == FShape.MULTI) {
			fSymbol.setSymbolType(FShape.LINE);
			ILineSymbol line = (ILineSymbol) deriveFSymbol(fSymbol);
			fSymbol.setSymbolType(FShape.POINT);
			IMarkerSymbol marker = (IMarkerSymbol) deriveFSymbol(fSymbol);
			fSymbol.setSymbolType(FShape.POLYGON);
			IFillSymbol fill = (IFillSymbol) deriveFSymbol(fSymbol);
			assert symbolType == FShape.MULTI;
			fSymbol.setSymbolType(symbolType);
			MultiShapeSymbol multiShapeSymbol = new MultiShapeSymbol();
			multiShapeSymbol.setMarkerSymbol(marker);
			multiShapeSymbol.setLineSymbol(line);
			multiShapeSymbol.setFillSymbol(fill);

			derivedSymbol = multiShapeSymbol;
		} else if (symbolType == FShape.TEXT || symbolType == FConstant.SYMBOL_TYPE_TEXT) {
			ITextSymbol textSym = SymbologyFactory.createDefaultTextSymbol();
			textSym.setTextColor(color);
			derivedSymbol = textSym;
		} else {
			throw new Error("FSymbol of type "+symbolType+" cannot be imported yet!");
		}

		// establish the general description;
		if (derivedSymbol instanceof AbstractSymbol) {
			AbstractSymbol symbol = (AbstractSymbol) derivedSymbol;
			symbol.setIsShapeVisible(fSymbol.isShapeVisible());
			symbol.setDescription(fSymbol.getDescription());
		}

		if (derivedSymbol instanceof MultiShapeSymbol ) {
			MultiShapeSymbol symbol = (MultiShapeSymbol) derivedSymbol;
			symbol.setDescription(fSymbol.getDescription());
		}

		return derivedSymbol;
	}

	/**
	 * Returns a new empty instance of a <b>IMultiLayer</b> that can be one of:
	 * <b>MultiLayerMarkerSymbol</b>, <b>MultiLayerLineSymbol</b>, or
	 * <b>MultiLayerFillSymbol</b> depending on the shape type passed.
	 *
	 * @param shapeType, one of FShape.POINT, FShape.LINE, or FShape.POLYGON
	 * @return IMultiLayerSymbol
	 */
	public static IMultiLayerSymbol createEmptyMultiLayerSymbol(int shapeType) {
		IMultiLayerSymbol mSym;
		switch (typeWithoutZM(shapeType)) {
		case FShape.POINT:
			mSym = new MultiLayerMarkerSymbol();
			break;
		case FShape.LINE:
			mSym = new MultiLayerLineSymbol();
			break;
		case FShape.POLYGON:
			mSym =new MultiLayerFillSymbol();
			break;
		default:
			throw new Error("Shape type not yet supported for multilayer symbols");
		}

		if (mSym instanceof CartographicSupport) {
			CartographicSupport cs = (CartographicSupport) mSym;
			cs.setUnit(CartographicSupportToolkit.DefaultMeasureUnit);
			cs.setReferenceSystem(CartographicSupportToolkit.DefaultReferenceSystem);
		}
		return mSym;
	}

	/**
	 * Returns a new instance of an <b>IMarkerSymbol</b>.
	 * @return IMarkerSymbol, the default symbol for markers
	 */
	public static IMarkerSymbol createDefaultMarkerSymbol() {
		SimpleMarkerSymbol sms = new SimpleMarkerSymbol();
		sms.setColor(DefaultSymbolColor);
		sms.setSize(4);
		toDefaultCartographicProperties(sms);
		return sms;
	}

	/**
	 * Returns a new instance of an <b>ILineSymbol</b>. A black line.
	 * @return ILineSymbol, the default symbol for lines.
	 */
	public static ILineSymbol createDefaultLineSymbol() {
		SimpleLineSymbol sls = new SimpleLineSymbol();
		sls.setLineColor(DefaultSymbolColor);
		sls.setLineWidth(1);
		toDefaultCartographicProperties(sls);
		return sls;
	}

	private static void toDefaultCartographicProperties(CartographicSupport cs) {
		cs.setUnit(CartographicSupportToolkit.DefaultMeasureUnit);
		cs.setReferenceSystem(CartographicSupportToolkit.DefaultReferenceSystem);
	}

	/**
	 * Returns a new instance of an <b>IFillSymbol</b>. Black outline,
	 * and transparent fill.
	 * @return IFillSymbol, the default symbol for polygons
	 */
	public static IFillSymbol createDefaultFillSymbol() {

		SimpleFillSymbol sfs = new SimpleFillSymbol();

		// Default symbol for polygons
		sfs.setOutline(createDefaultLineSymbol());
		sfs.setFillColor(DefaultFillSymbolColor); // transparent fill
		toDefaultCartographicProperties(sfs);
		return sfs;
    }

	/**
	 * Returns a new instance of an <b>ITextSymbol</b>.
	 * @return ITextSymbol, the default symbol for texts
	 */
	public static ITextSymbol createDefaultTextSymbol() {
		SimpleTextSymbol sts = new SimpleTextSymbol();

		sts.setFont(DefaultTextFont);
		sts.setTextColor(DefaultSymbolColor);
		toDefaultCartographicProperties(sts);

		return sts;
	}

	/**
	 * Creates a new instance of the default symbol whose type is defined
	 * by the parameter <b>shapeType</b>
	 * @param shapeType, one of FShape.POINT, FShape.LINE, FShape.POLYGON,
	 * FShape.MULTIPOINT, FShape.TEXT, or FShape.MULTI.
	 * @return ISymbol, the default symbol for the shape type defined by <b>shapeType</b>
	 */
	public static ISymbol createDefaultSymbolByShapeType(int shapeType) {
	    switch (typeWithoutZM(shapeType)) {
        case FShape.POINT:
            return createDefaultMarkerSymbol();
        case FShape.LINE:
            return createDefaultLineSymbol();
        case FShape.POLYGON:
            return createDefaultFillSymbol();
        case FShape.MULTIPOINT:
            return createDefaultMarkerSymbol();
        case FShape.TEXT:
            return createDefaultTextSymbol();
        case FShape.MULTI:
            return new MultiShapeSymbol();
        case FShape.NULL:
            return null;
        }
		throw new Error("shape type not yet supported");
	}
	
	private static int typeWithoutZM(int shapeType) {
        // return shapeType % FShape.Z; // esto es suficiente, pero para que quede más claro
        return shapeType % FShape.Z % FShape.M;
    }

	/**
	 * Creates a new instance of the default symbol whose type is defined
	 * by the parameter <b>shapeType</b> and uses the color defined by the
	 * parameter color.
	 * @param shapeType, one of FShape.POINT, FShape.LINE, FShape.POLYGON,
	 * @param color, the color to be applied to the new ISymbol.
	 *
	 * FShape.MULTIPOINT, FShape.TEXT, or FShape.MULTI.
	 * @return ISymbol, the default symbol for the shape type defined by <b>shapeType</b>
	 */
	public static ISymbol createDefaultSymbolByShapeType(int shapeType, Color color) {
		ISymbol sym = createDefaultSymbolByShapeType(shapeType);

		if (sym instanceof IMarkerSymbol) {
			((IMarkerSymbol) sym).setColor(color);
		}

		if (sym instanceof ILineSymbol) {
			((ILineSymbol) sym).setLineColor(color);
		}

		if (sym instanceof IFillSymbol) {
			((IFillSymbol) sym).setFillColor(color);
		}

		if (sym instanceof ITextSymbol) {
			((ITextSymbol) sym).setTextColor(color);
		}

		return sym;
	}


	public static ISymbol getWarningSymbol(String message, String symbolDesc, int symbolDrawExceptionType) {
		if (warningSymbol == null) {
			warningSymbol = new WarningSymbol();
		}
		warningSymbol.setDescription(symbolDesc);
		warningSymbol.setMessage(message);
		warningSymbol.setDrawExceptionType(symbolDrawExceptionType);
		return warningSymbol;
	}

	private static class WarningSymbol extends MultiShapeSymbol {
		private String desc;
		private String message;
		private int exceptionType;
		private SimpleTextSymbol text;

		public static void main(String[] args) {
			JFrame f = new JFrame();
			final ISymbol warning = SymbologyFactory.getWarningSymbol(SymbolDrawingException.STR_UNSUPPORTED_SET_OF_SETTINGS, "a description", SymbolDrawingException.UNSUPPORTED_SET_OF_SETTINGS);
			JPanel preview = new JPanel() {
				@Override
				protected void paintComponent(Graphics g) {
					// TODO Auto-generated method stub
					super.paintComponent(g);
					Graphics2D g2 = (Graphics2D)g;
					try {
						warning.drawInsideRectangle(g2, g2.getTransform(), getBounds(), null);
					} catch (SymbolDrawingException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			};
			f.setContentPane(preview);
			f.pack();
			f.setVisible(true);
		}

		public void draw(Graphics2D g, AffineTransform affineTransform,
				FShape shp, Cancellable cancel) {
			try {
				drawInsideRectangle(g, g.getTransform(), shp.getBounds(), null);
			} catch (SymbolDrawingException e) {
				// IMPOSSIBLE
			}
		}

		public void setDrawExceptionType(int symbolDrawExceptionType) {
			this.exceptionType = symbolDrawExceptionType;
		}

		public void setMessage(String message) {
			this.message = message;
		}

		public void drawInsideRectangle(Graphics2D g,
				AffineTransform scaleInstance, Rectangle r, PrintRequestAttributeSet properties)
				throws SymbolDrawingException {
			g.setClip(r);
			if (message == null) {
				message = "Symbol undrawable.\nPlease, check errors.";
			}


			String[] messageLines = message.split("\n");
			int strokeWidth = (int) (Math.min(r.width, r.height)*.1);

			if (strokeWidth == 0) strokeWidth = 1;

			g.setColor(Color.red);
			g.setStroke(new BasicStroke(strokeWidth, BasicStroke.CAP_BUTT, BasicStroke.JOIN_ROUND));
			int x = r.x+strokeWidth;
			int y = r.x+strokeWidth;
			int width = r.width-(strokeWidth+strokeWidth);
			int height = r.height-(strokeWidth+strokeWidth);

			double radius = Math.min(width, height)*.5;
			double centerX = r.getCenterX();
			double centerY = r.getCenterY();
			Ellipse2D circle = new Ellipse2D.Double(centerX - radius, centerY - radius, 2*radius, 2*radius);
			g.draw(circle);
			g.setClip(circle);
			double aux = Math.cos(Math.PI*0.25)*radius;
			g.drawLine((int) (centerX - aux), (int) (centerY - aux), (int) (centerX + aux), (int) (centerY + aux));
			int fontSize = 20;
			g.setFont(new Font("Arial", fontSize, Font.PLAIN));
			g.setColor(Color.black);
			g.setClip(null);

			if (text == null) {
				text = new SimpleTextSymbol();
				text.setAutoresizeEnabled(true);

			}

			double lineHeight = (r.getHeight()-6)/messageLines.length;
			Rectangle textRect = new Rectangle(
					(int) r.getMinX(),
					(int) r.getMinY()+6,
					(int) r.getWidth(),
					(int) lineHeight);
			for (int i =0; i < messageLines.length; i++) {
				text.setText(messageLines[i]);
				text.drawInsideRectangle(g, null, textRect, properties);
				textRect.setLocation((int) r.getX(), (int) (r.getY()+r.getHeight()));
			}
		}

		@Override
		public XMLEntity getXMLEntity() {
			XMLEntity xml = new XMLEntity();
			xml.putProperty("className", getClass().getName());
			xml.putProperty("desc", desc);
			xml.putProperty("exceptionType", exceptionType);
			xml.putProperty("message", message);
			return xml;
		}

		@Override
		public void setXMLEntity(XMLEntity xml) {
			setDescription(xml.getStringProperty("desc"));
			exceptionType = xml.getIntProperty("exceptionType");
			message = xml.getStringProperty("message");
		}

	}


}