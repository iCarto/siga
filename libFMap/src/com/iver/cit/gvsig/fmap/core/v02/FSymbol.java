/*
 * Created on 19-feb-2004
 *
 * To change the template for this generated file go to
 * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
 */
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
package com.iver.cit.gvsig.fmap.core.v02;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Paint;
import java.awt.Rectangle;
import java.awt.Stroke;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.StringTokenizer;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.attribute.standard.PrintQuality;
import javax.swing.ImageIcon;

import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.ISLDCompatible;
import com.iver.cit.gvsig.fmap.core.SLDTags;
import com.iver.cit.gvsig.fmap.core.SLDUtils;
import com.iver.cit.gvsig.fmap.core.symbols.ISymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.cit.gvsig.fmap.rendering.XmlBuilder;
import com.iver.utiles.StringUtilities;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.threads.Cancellable;


/**
 * S�mbolo utilizado para guardar las caracter�sticas que se deben de aplicar a
 * los Shapes a dibujar.
 *
 * @author Vicente Caballero Navarro
 * @deprecated (jaume)
 */
public class FSymbol implements ISymbol, ISLDCompatible{
	private static BufferedImage img = new BufferedImage(1, 1,
			BufferedImage.TYPE_INT_ARGB);
	private static Rectangle rect = new Rectangle(0, 0, 1, 1);
	private int m_symbolType;
	private int m_Style;
	private boolean m_useOutline;
	private Color m_Color;
	private Color m_outlineColor;
	private Font m_Font;
	private Color m_FontColor;
	private float m_FontSize;
	private int rgb;
	private ImageObserver imgObserver;

	/**
	 * Si <code>m_bUseFontSize</code> viene a false entonces m_FontSize viene
	 * en unidades de mapa (metros)
	 */
	private boolean m_bUseFontSizeInPixels;

	/**
	 * <code>m_bDrawShape</code> indica si queremos dibujar el shape de fondo.
	 * Es �til cuando est�s etiquetando y no quieres que se dibuje el s�mbolo
	 * que te sirve de base para etiquetar.
	 */
	private boolean m_bDrawShape = true;
	private int m_Size;
	private Image m_Icon;
	private URI m_IconURI;
	private int m_Rotation;
	private Paint m_Fill;
	public String m_LinePattern = "0"; // Solo para poder mostrarlo cuando vamos a seleccionar un s�mbolo

	// En realidad lo podemos ver de BasicStroke, pero....
	// ya veremos si luego lo quitamos.
	private Stroke m_Stroke;

	//private float m_stroke=0;
	// public int m_Transparency; // Ya la lleva dentro del Color
	private boolean m_bUseSize; // Si est� a true, m_Size viene en coordenadas de mundo real.
	private int m_AlingVert;
	private int m_AlingHoriz;
	private String m_Descrip;
	public Color m_BackColor;
	public Paint m_BackFill;
	private int resolutionPrinting=300;

    /**
     * Converts the comma-delimited string into a List of trimmed strings.
     *
     * @param linePattern a String with comma-delimited values
     * @param lineWidth DOCUMENT ME!
     *
     * @return a List of the Strings that were delimited by commas
     *
     * @throws IllegalArgumentException DOCUMENT ME!
     */
    public static float[] toArray(String linePattern, float lineWidth) {
        StringTokenizer st = new StringTokenizer(linePattern, ",");
        int numTokens = st.countTokens();

        float[] array = new float[numTokens];

        for (int i = 0; i < numTokens; i++) {
            String string = st.nextToken();
            array[i] = Float.parseFloat(string) * lineWidth;

            if (array[i] <= 0) {
                return null;
            }
        }

        return array;
    }

	/**
	 * Crea un nuevo FSymbol.
	 * @deprecated use SymbologyFactory.createDefaultSymbol() instead
	 */
//    FSymbol() {
	private FSymbol() {
	}

	/**
	 * Creates a new FSymbol object.
	 *
	 * @param tipoSymbol Tipo de s�mbolo.
	 * @param c Color.
	 * @deprecated use SymbologyFactory.createDefaultSymbol(shapeType, color) instead
	 */
//	public FSymbol(int tipoSymbol, Color c) {
	private FSymbol(int tipoSymbol, Color c) {
		createSymbol(tipoSymbol, c);
	}

	/**
	 * Crea un nuevo FSymbol.
	 *
	 * @param tipoSymbol Tipo de S�mbolo.
	 * 			case FConstant.SYMBOL_TYPE_POINT:
			case FConstant.SYMBOL_TYPE_POINTZ:
			case FConstant.SYMBOL_TYPE_MULTIPOINT:
				m_bUseSize = true; // Esto es lo primero que hay que hacer siempre

				// para evitar un StackOverflow
				m_useOutline = false;
				setStyle(FConstant.SYMBOL_STYLE_MARKER_SQUARE);
				setSize(5); //pixels

				break;

			case FConstant.SYMBOL_TYPE_LINE:
			case FConstant.SYMBOL_TYPE_POLYLINEZ:
			case FConstant.SYMBOL_TYPE_POLYGONZ:
				setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_LINE_SOLID);

				break;

			case FConstant.SYMBOL_TYPE_FILL:
			    setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_FILL_SOLID);

				break;
			case FShape.MULTI:
				m_bUseSize = true;
			    setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_FILL_SOLID);

				// setStyle(FConstant.SYMBOL_STYLE_MARKER_SQUARE);
				setSize(5); //pixels
				break;

			case FConstant.SYMBOL_TYPE_TEXT:
				setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_TEXT_NORMAL);
				setFont(new Font("Dialog",Font.PLAIN,12));
				break;

	 */
	public FSymbol(int tipoSymbol) {
//	private FSymbol(int tipoSymbol) {
		int numreg = (int) (Math.random() * 100);
		Color colorAleatorio = new Color(((numreg * numreg) + 100) % 255,
				(numreg + ((3 * numreg) + 100)) % 255, numreg % 255);

		createSymbol(tipoSymbol, colorAleatorio);
	}

	/**
	 * A partir de un s�mbolo devuelve otro similar pero con el color de
	 * selecci�n.
	 *
	 * @param sym S�mbolo a modificar.
	 *
	 * @return S�mbolo modificado.
	 */
	public static FSymbol getSymbolForSelection(FSymbol sym) {
		FSymbol selecSymbol = sym.fastCloneSymbol();
		selecSymbol.setColor(MapContext.getSelectionColor());

		selecSymbol.setFill(null);
		// 050215, jmorell: Si en los drivers cambiamos el estilo, aqu� tenemos que
		// actualizar los cambios. SYMBOL_STYLE_MARKER_SQUARE --> SYMBOL_STYLE_DGNSPECIAL.
		if ((selecSymbol.getStyle() == FConstant.SYMBOL_STYLE_FILL_TRANSPARENT)
		        || (selecSymbol.getStyle() == FConstant.SYMBOL_STYLE_DGNSPECIAL))
		    selecSymbol.setStyle(FConstant.SYMBOL_STYLE_FILL_SOLID);


		else if (selecSymbol.getStyle() == FConstant.SYMBOL_STYLE_TEXT_BOLD ||
				selecSymbol.getStyle() == FConstant.SYMBOL_STYLE_TEXT_BOLDCURSIVE ||
				selecSymbol.getStyle() == FConstant.SYMBOL_STYLE_TEXT_CURSIVE ||
				selecSymbol.getStyle() == FConstant.SYMBOL_STYLE_TEXT_NORMAL){
			selecSymbol.setFontColor(MapContext.getSelectionColor());
		}
		selecSymbol.rgb = MapContext.getSelectionColor().getRGB();

		return selecSymbol;
	}

	/**
	 * Clona el s�mbolo actual.
	 *
	 * @return Nuevo s�mbolo clonado.
	 */
	public FSymbol cloneSymbol() {
		return createFromXML(getXMLEntity());
	}

	/**
	 * Se usa para el s�mbolo de selecci�n. Es una forma
	 * r�pida de clonar un s�mbolo, sin hacerlo via XML.
	 * Vicente, no lo borres!!!
	 * @return
	 */
	public FSymbol fastCloneSymbol()
	{
		FSymbol nS = new FSymbol();


		nS.m_symbolType = m_symbolType;
		nS.m_Style = m_Style;
		nS.m_useOutline = m_useOutline;
		nS.m_Color = m_Color;
		nS.m_outlineColor = m_outlineColor;
		nS.m_Font = m_Font;
		nS.m_FontColor = m_FontColor;
		nS.m_FontSize = m_FontSize;
		nS.m_bUseFontSizeInPixels = m_bUseFontSizeInPixels;
		nS.m_bDrawShape = m_bDrawShape;
		nS.m_Size = m_Size;
		nS.m_Icon = m_Icon;
		nS.m_IconURI = m_IconURI;
		nS.m_Rotation = m_Rotation;
		nS.m_Fill = m_Fill;
		nS.m_Stroke = m_Stroke;
		// nS.m_Transparency =m_Transparency ;
		nS.m_bUseSize = m_bUseSize;
		nS.m_AlingVert = m_AlingVert;
		nS.m_AlingHoriz = m_AlingHoriz;
		nS.m_Descrip = m_Descrip;
		nS.m_BackColor = m_BackColor;
		nS.m_BackFill = m_BackFill;

		nS.m_LinePattern = m_LinePattern;

		return nS;
	}


	/**
	 * Crea un s�mbolo a partir del tipo y el color.
	 *
	 * @param tipoSymbol Tipo de s�mbolo.
	 * @param c Color del s�mbolo a crear.
	 */
	private void createSymbol(int tipoSymbol, Color c) {
		// OJO: HE HECHO COINCIDIR LOS TIPOS DE SIMBOLO
		//FConstant.SYMBOL_TYPE_POINT, LINE Y FILL CON
		// FShape.POINT, LINE, POLYGON. EL .MULTI SE REFIERE
		// A MULTIPLES TIPO DENTRO DEL SHAPE, AS� QUE SER� UN
		// MULTISIMBOLO
		// Tipo de simbolo
		m_symbolType = tipoSymbol; // Para no recalcular el pixel, no usamos los set

		// Ponemos un estilo por defecto
		m_useOutline = true;
		m_Color = c;
		m_Stroke = null;
		m_Fill = null;

		m_FontColor = Color.BLACK;
		m_FontSize = 10;
		m_bUseFontSizeInPixels = true;

		m_Size = 2;

		switch (getSymbolType()) {
			case FConstant.SYMBOL_TYPE_POINT:
			case FConstant.SYMBOL_TYPE_POINTZ:
			case FConstant.SYMBOL_TYPE_MULTIPOINT:
				m_bUseSize = true; // Esto es lo primero que hay que hacer siempre

				// para evitar un StackOverflow
				m_useOutline = false;
				setStyle(FConstant.SYMBOL_STYLE_MARKER_SQUARE);
				setSize(5); //pixels

				break;

			case FConstant.SYMBOL_TYPE_LINE:
			case FConstant.SYMBOL_TYPE_POLYLINEZ:
			case FConstant.SYMBOL_TYPE_POLYGONZ:
				setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_LINE_SOLID);

				break;

			case FConstant.SYMBOL_TYPE_FILL:
			    setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_FILL_SOLID);

				break;
			case FShape.MULTI:
				m_bUseSize = true;
			    setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_FILL_SOLID);

				// setStyle(FConstant.SYMBOL_STYLE_MARKER_SQUARE);
				setSize(5); //pixels
				break;

			case FConstant.SYMBOL_TYPE_TEXT:
				setStroke(new BasicStroke());
				setStyle(FConstant.SYMBOL_STYLE_TEXT_NORMAL);
				setFont(new Font("Dialog",Font.PLAIN,12));
				break;
		}

		m_outlineColor = c.darker();

		calculateRgb();
	}

	/**
	 * Calcula el RGB del s�mbolo.
	 */
	public void calculateRgb() {
		// Recalculamos el RGB
		Graphics2D g2 = img.createGraphics();

		FGraphicUtilities.DrawSymbol(g2, g2.getTransform(), rect, this);
		rgb = img.getRGB(0, 0);
	}

	/**
	 * Devuelve el rgb del s�mbolo.
	 *
	 * @return rgb del s�mbolo.
	 */
	public int getOnePointRgb() {
		return rgb;
	}

	/**
	 * @see com.iver.cit.gvsig.gui.layout.fframes.IFFrame#getXMLEntity()
	 */
	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className",this.getClass().getName());
		xml.putProperty("m_symbolType", getSymbolType());
		xml.putProperty("m_Style", getStyle());
		xml.putProperty("m_useOutline", isOutlined());

		if (getColor() != null) {
			xml.putProperty("m_Color", StringUtilities.color2String(getColor()));
		}

		if (getOutlineColor() != null) {
			xml.putProperty("m_outlineColor",
				StringUtilities.color2String(getOutlineColor()));
		}

		if (getFont() != null) {
			xml.putProperty("fontname", getFont().getName());
			xml.putProperty("fontstyle", getFont().getStyle());

			xml.putProperty("m_FontSize", getFontSize());
			xml.putProperty("m_FontColor",
				StringUtilities.color2String(getFontColor()));

		}
		xml.putProperty("m_bUseFontSize", isFontSizeInPixels());
		xml.putProperty("m_bDrawShape", isShapeVisible());
		xml.putProperty("m_Size", getSize());

		//xml.putProperty("m_Icon",m_Icon.);
		xml.putProperty("m_Rotation", getRotation());

		if (getFill() instanceof Color) {
			xml.putProperty("m_Fill",
				StringUtilities.color2String((Color) getFill()));
		}
		else
			if (getFill() != null)
			{
			    xml.putProperty("m_Fill", "WithFill");
			}


		xml.putProperty("m_LinePattern", m_LinePattern);

		//Ancho del stroke en float
		if (getStroke() != null) {
			xml.putProperty("m_stroke",
				((BasicStroke) getStroke()).getLineWidth());
		} else {
			xml.putProperty("m_stroke", 0f);
		}

		xml.putProperty("m_bUseSize", isSizeInPixels());
		xml.putProperty("m_AlingVert", getAlingVert());
		xml.putProperty("m_AlingHoriz", getAlingHoriz());
		xml.putProperty("m_Descrip", getDescription());

		if (m_BackColor != null) {
			xml.putProperty("m_BackColor",
				StringUtilities.color2String(m_BackColor));
		}

		if (m_BackFill instanceof Color) {
			xml.putProperty("m_BackFill",
				StringUtilities.color2String((Color) m_BackFill));
		}

		xml.putProperty("rgb", rgb);

		if (m_Icon != null)
		{
		    xml.putProperty("m_IconURI", m_IconURI);
		}

		return xml;
	}


	/**
	 * Crea el s�mbolo a partir del xml.
	 *
	 * @param xml xml que contiene la informaci�n para crear el s�mbolo.
	 *
	 * @return S�mbolo creado a partir del XML.
	 */
	public static FSymbol createFromXML(XMLEntity xml) {
		FSymbol symbol = new FSymbol();
		symbol.setSymbolType(xml.getIntProperty("m_symbolType"));
		symbol.setStyle(xml.getIntProperty("m_Style"));
		// System.out.println("createFromXML: m_Style=" + xml.getIntProperty("m_Style"));

		symbol.setOutlined(xml.getBooleanProperty("m_useOutline"));

		if (xml.contains("m_Color")) {
			symbol.setColor(StringUtilities.string2Color(xml.getStringProperty(
						"m_Color")));
		}

		if (xml.contains("m_outlineColor")) {
			symbol.setOutlineColor(StringUtilities.string2Color(
					xml.getStringProperty("m_outlineColor")));
		}

		if (xml.contains("fontname")) {
			symbol.setFont(new Font(xml.getStringProperty("fontname"),
					xml.getIntProperty("fontstyle"),
					(int) xml.getFloatProperty("m_FontSize")));

			symbol.setFontColor(StringUtilities.string2Color(
					xml.getStringProperty("m_FontColor")));
			symbol.setFontSize(xml.getFloatProperty("m_FontSize"));

		}
		symbol.setFontSizeInPixels(xml.getBooleanProperty("m_bUseFontSize"));
		symbol.setShapeVisible(xml.getBooleanProperty("m_bDrawShape"));
		symbol.setSize(xml.getIntProperty("m_Size"));

		//xml.putProperty("m_Icon",m_Icon.);
		symbol.setRotation(xml.getIntProperty("m_Rotation"));

		if (xml.contains("m_Fill")) {
		    // TODO: Si es un Fill de tipo imagen, deber�amos recuperar la imagen.
		    String strFill = xml.getStringProperty("m_Fill");
		    if (strFill.compareTo("WithFill") == 0)
		        symbol.setFill(FSymbolFactory.createPatternFill(symbol.getStyle(), symbol.getColor()));
		    else
		        symbol.setFill(StringUtilities.string2Color(strFill));
		}


		symbol.m_LinePattern = xml.getStringProperty("m_LinePattern");

		//Ancho del stroke en float
        float lineWidth = xml.getFloatProperty("m_stroke");
        if (symbol.m_LinePattern.compareTo("0") == 0)
        {
            symbol.setStroke(new BasicStroke(lineWidth));
        }
        else
        {
            symbol.setStroke(new BasicStroke(lineWidth, BasicStroke.CAP_ROUND,
                    BasicStroke.JOIN_BEVEL, 1.0f,
                        toArray(symbol.m_LinePattern, lineWidth), 0));
        }

		symbol.setSizeInPixels(xml.getBooleanProperty("m_bUseSize"));
		symbol.setAlingVert(xml.getIntProperty("m_AlingVert"));
		symbol.setAlingHoriz(xml.getIntProperty("m_AlingHoriz"));
		symbol.setDescription(xml.getStringProperty("m_Descrip"));

		if (xml.contains("m_BackColor")) {
			symbol.m_BackColor = StringUtilities.string2Color(xml.getStringProperty(
						"m_BackColor"));
		}

		if (xml.contains("m_BackFill")) {
			symbol.m_BackFill = StringUtilities.string2Color(xml.getStringProperty(
						"m_BackFill"));
		}

		symbol.rgb = xml.getIntProperty("rgb");

		if (xml.contains("m_IconURI")) {
		    try {
                symbol.setIconURI(new URI(xml.getStringProperty("m_IconURI")));
            } catch (URISyntaxException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
		}

		return symbol;
	}

	/**
	 * Introduce el estilo del s�mbolo.
	 *
	 * @param m_Style The m_Style to set.
	 */
	public void setStyle(int m_Style) {
		this.m_Style = m_Style;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el estilo del s�mbolo.
	 *
	 * @return Returns the m_Style.
	 */
	public int getStyle() {
		return m_Style;
	}

	/**
	 * Introduce el tipo de s�mbolo.
	 *
	 * @param m_symbolType The m_symbolType to set.
	 */
	public void setSymbolType(int m_symbolType) {
		this.m_symbolType = m_symbolType;
	}

	/**
	 * Devuelve el tipo de s�mbolo.
	 *
	 * @return Returns the m_symbolType.
	 */
	public int getSymbolType() {
		return m_symbolType;
	}

	/**
	 * Introduce si el s�mbolo contiene linea de brode o no.
	 *
	 * @param m_useOutline The m_useOutline to set.
	 */
	public void setOutlined(boolean m_useOutline) {
		this.m_useOutline = m_useOutline;

		//		 calculateRgb();
	}

	/**
	 * Devuelve si el s�mbolo contiene o no linea de borde.
	 *
	 * @return Returns the m_useOutline.
	 */
	public boolean isOutlined() {
		return m_useOutline;
	}

	/**
	 * Introduce el color del s�mbolo.
	 *
	 * @param m_Color The m_Color to set.
	 */
	public void setColor(Color m_Color) {
		this.m_Color = m_Color;
		calculateRgb();
	}

	/**
	 * Devuelve el color del s�mbolo.
	 *
	 * @return Returns the m_Color.
	 */
	public Color getColor() {
		return m_Color;
	}

	/**
	 * Introduce el color de la l�nea de borde.
	 *
	 * @param m_outlineColor The m_outlineColor to set.
	 */
	public void setOutlineColor(Color m_outlineColor) {
		this.m_outlineColor = m_outlineColor;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el color de la l�nea de borde.
	 *
	 * @return Returns the m_outlineColor.
	 */
	public Color getOutlineColor() {
		return m_outlineColor;
	}

	/**
	 * Introduce el Font del s�mbolo.
	 *
	 * @param m_Font The m_Font to set.
	 */
	public void setFont(Font m_Font) {
		this.m_Font = m_Font;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el Font del s�mbolo.
	 *
	 * @return Returns the m_Font.
	 */
	public Font getFont() {
		return m_Font;
	}

	/**
	 * Introduce el color de la fuente.
	 *
	 * @param m_FontColor The m_FontColor to set.
	 */
	public void setFontColor(Color m_FontColor) {
		this.m_FontColor = m_FontColor;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el color de la fuente.
	 *
	 * @return Returns the m_FontColor.
	 */
	public Color getFontColor() {
		return m_FontColor;
	}

	/**
	 * Introduce si se usa el tama�o de la fuente en pixels.
	 *
	 * @param m_bUseFontSize The m_bUseFontSize to set.
	 */
	public void setFontSizeInPixels(boolean m_bUseFontSize) {
		this.m_bUseFontSizeInPixels = m_bUseFontSize;

		// calculateRgb();
	}

	/**
	 * Devuelve true si el tama�o de la fuente esta seleccionado en pixels.
	 *
	 * @return Returns the m_bUseFontSize.
	 */
	public boolean isFontSizeInPixels() {
		return m_bUseFontSizeInPixels;
	}

	/**
	 * Introduce si el shape e visible o no lo es.
	 *
	 * @param m_bDrawShape The m_bDrawShape to set.
	 */
	public void setShapeVisible(boolean m_bDrawShape) {
		this.m_bDrawShape = m_bDrawShape;

		//		 calculateRgb();
	}

	/**
	 * Devuelve true si el shape es visible.
	 *
	 * @return Returns the m_bDrawShape.
	 */
	public boolean isShapeVisible() {
		return m_bDrawShape;
	}

	/**
	 * Introduce el tama�o del s�mbolo.
	 *
	 * @param m_Size The m_Size to set.
	 */
	public void setSize(int m_Size) {
		this.m_Size = m_Size;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el tama�o del s�mbolo.
	 *
	 * @return Returns the m_Size.
	 */
	public int getSize() {
		return m_Size;
	}

	/**
	 * Introduce la imagen que hace de icono.
	 *
	 * @param m_Icon The m_Icon to set.
	 */
	public void setIcon(Image m_Icon) {
		this.m_Icon = m_Icon;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el icono.
	 *
	 * @return Returns the m_Icon.
	 */
	public Image getIcon() {
		return m_Icon;
	}

	/**
	 * Introduce la rotaci�n.
	 *
	 * @param m_Rotation The m_Rotation to set.
	 */
	public void setRotation(int m_Rotation) {
		this.m_Rotation = m_Rotation;

		//		 calculateRgb();
	}

	/**
	 * Devuelve la rotaci�n.
	 *
	 * @return Returns the m_Rotation.
	 */
	public int getRotation() {
		return m_Rotation;
	}

	/**
	 * Introduce el relleno.
	 *
	 * @param m_Fill The m_Fill to set.
	 */
	public void setFill(Paint m_Fill) {
		this.m_Fill = m_Fill;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el relleno.
	 *
	 * @return Returns the m_Fill.
	 */
	public Paint getFill() {
		return m_Fill;
	}

	/**
	 * Introduce el Stroke.
	 *
	 * @param m_Stroke The m_Stroke to set.
	 */
	public void setStroke(Stroke m_Stroke) {
		this.m_Stroke = m_Stroke;

		//		 calculateRgb();
	}

	/**
	 * Devuelve el Stroke.
	 *
	 * @return Returns the m_Stroke.
	 */
	public Stroke getStroke() {
		return m_Stroke;
	}

	/**
	 * Introduce si el tama�o del simbolo est� en pixels.
	 *
	 * @param m_bUseSize The m_bUseSize to set.
	 */
	public void setSizeInPixels(boolean m_bUseSize) {
		this.m_bUseSize = m_bUseSize;

		//		 calculateRgb();
	}

	/**
	 * Devuelve si el tama�o del s�mbolo est� en pixels.
	 *
	 * @return Returns the m_bUseSize.
	 */
	public boolean isSizeInPixels() {
		return m_bUseSize;
	}

	/**
	 * Introduce la descripci�n del s�mbolo.
	 *
	 * @param m_Descrip The m_Descrip to set.
	 */
	public void setDescription(String m_Descrip) {
		this.m_Descrip = m_Descrip;
	}

	/**
	 * Devuelve la descripci�n del s�mbolo.
	 *
	 * @return Returns the m_Descrip.
	 */
	public String getDescription() {
		return m_Descrip != null ? m_Descrip : "Default";
	}

	/**
	 * Introduce la alineaci�n en vertical.
	 *
	 * @param m_AlingVert The m_AlingVert to set.
	 */
	public void setAlingVert(int m_AlingVert) {
		this.m_AlingVert = m_AlingVert;

		//		 calculateRgb();
	}

	/**
	 * Devuelve la alineaci�n en vertical.
	 *
	 * @return Returns the m_AlingVert.
	 */
	public int getAlingVert() {
		return m_AlingVert;
	}

	/**
	 * Introduce la alineaci�n en horizontal.
	 *
	 * @param m_AlingHoriz The m_AlingHoriz to set.
	 */
	public void setAlingHoriz(int m_AlingHoriz) {
		this.m_AlingHoriz = m_AlingHoriz;

		// calculateRgb();
	}

	/**
	 * Devuelve la alineaci�n en horizontal.
	 *
	 * @return Returns the m_AlingHoriz.
	 */
	public int getAlingHoriz() {
		return m_AlingHoriz;
	}

	
	/**
	 * Introduce el tama�o de la fuente.
	 *
	 * @param m_FontSize The m_FontSize to set.
	 */
	public void setFontSize(float m_FontSize) {
		this.m_FontSize = m_FontSize;
	}

	/**
	 * Devuelve el tama�o de la fuente.
	 *
	 * @return Returns the m_FontSize.
	 */
	public float getFontSize() {
		return m_FontSize;
	}
    public URI getIconURI() {
        return m_IconURI;
    }
    public void setIconURI(URI iconURI) {
        m_IconURI = iconURI;
        ImageIcon prov;
        try {
            prov = new ImageIcon(iconURI.toURL());
            m_Icon = prov.getImage();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }
    //methods to be ISLDCompatible
    /**
     * converts FSymbol to Geotools symbol.
     */
    public String toSLD ()
    {
    	XmlBuilder xmlBuilder = new XmlBuilder();

    	try
    	{
   		switch (this.getSymbolType())
    		{

			case FConstant.SYMBOL_TYPE_POINT:
				xmlBuilder.openTag(SLDTags.POINTSYMBOLIZER);
				xmlBuilder.openTag(SLDTags.GRAPHIC);
				xmlBuilder.openTag(SLDTags.MARK);

				if (this.getStyle() == FConstant.SYMBOL_STYLE_MARKER_CIRCLE ){
					xmlBuilder.writeTag(SLDTags.WELLKNOWNNAME,"circle");
				}else if (this.getStyle() == FConstant.SYMBOL_STYLE_MARKER_SQUARE ){
					xmlBuilder.writeTag(SLDTags.WELLKNOWNNAME,"square");
				}else if (this.getStyle() == FConstant.SYMBOL_STYLE_MARKER_TRIANGLE ){
					xmlBuilder.writeTag(SLDTags.WELLKNOWNNAME,"triangle");
				}else if (this.getStyle() == FConstant.SYMBOL_STYLE_MARKER_CROSS ){
					xmlBuilder.writeTag(SLDTags.WELLKNOWNNAME,"cross");
				}

				if(this.m_Color != null){
					xmlBuilder.openTag(SLDTags.FILL);
					xmlBuilder.writeTag(SLDTags.CSSPARAMETER,
										SLDUtils.convertColorToHexString(this.m_Color),
										SLDTags.NAME_ATTR,
										SLDTags.FILL_ATTR);
					xmlBuilder.closeTag();
				}
				xmlBuilder.closeTag();

				//boolean bAux2 = this.isSizeInPixels();
				int alturaMetros = this.getSize();
				xmlBuilder.writeTag(SLDTags.SIZE,""+alturaMetros);
				xmlBuilder.closeTag();
				xmlBuilder.closeTag();

				//TODO: Ver como a�adir un texto a cada feature de una capa de puntos...
				if (this.getFont() != null) {
					//boolean bAux = this.isFontSizeInPixels();
				}
				break;

			case FConstant.SYMBOL_TYPE_LINE:
				xmlBuilder.openTag(SLDTags.LINESYMBOLIZER);
//				Add geometry tag with the column that gives the geometric infrmation
//				Maybe this is not necessary
//				sld.append(SLDTags.OT_GEOMETRY);
//				sld.append(SLDTags.CT_GEOMETRY);

				if(this.m_Color != null){
					xmlBuilder.openTag(SLDTags.STROKE);
					xmlBuilder.writeTag(SLDTags.CSSPARAMETER
										,SLDUtils.convertColorToHexString(this.m_Color)
										,SLDTags.NAME,SLDTags.STROKE_ATTR);

					if (getStroke() != null) {
						xmlBuilder.writeTag(SLDTags.CSSPARAMETER
								,Float.toString(((BasicStroke) getStroke()).getLineWidth())
								,SLDTags.NAME,SLDTags.STROKE_WIDTH_ATTR);
					}
					//TODO: Add to the SLD all the line styles
//					if (this.getStyle() ==  FConstant.SYMBOL_STYLE_LINE_DASH
//				    <CssParameter name="stroke">#FFFF00</CssParameter>
//				    <CssParameter name="stroke-opacity">1.0</CssParameter>
//				    <CssParameter name="stroke-width">6.0</CssParameter>
//				    <CssParameter name="stroke-dasharray">1</CssParameter>

					xmlBuilder.closeTag();
				}
				xmlBuilder.closeTag();
				break;

			case FConstant.SYMBOL_TYPE_FILL:
				xmlBuilder.openTag(SLDTags.POLYGONSYMBOLIZER);
				if(this.m_Color != null){
					xmlBuilder.openTag(SLDTags.FILL);
					xmlBuilder.writeTag(SLDTags.CSSPARAMETER,
										SLDUtils.convertColorToHexString(this.m_Color),
										SLDTags.NAME_ATTR,
										SLDTags.FILL_ATTR);
					xmlBuilder.closeTag();
				}
				if (this.m_outlineColor != null){
					xmlBuilder.openTag(SLDTags.STROKE);
					xmlBuilder.writeTag(SLDTags.CSSPARAMETER,
							SLDUtils.convertColorToHexString(this.m_outlineColor),
							SLDTags.NAME_ATTR,
							SLDTags.STROKE_ATTR);
					if (getStroke() != null) {
						xmlBuilder.writeTag(SLDTags.CSSPARAMETER
								,Float.toString(((BasicStroke) getStroke()).getLineWidth())
								,SLDTags.NAME,SLDTags.STROKE_WIDTH_ATTR);
					}
					xmlBuilder.closeTag();
				}
//				TODO: Fill opacity and other graphic features
				xmlBuilder.closeTag();
				break;

			case FShape.MULTI:
				if (this.getFont() != null) {
					// Para no tener que clonarlo si viene en unidades de mapa
					boolean bAux = this.isFontSizeInPixels();
					this.setFontSizeInPixels(true);
					//FGraphicUtilities.DrawLabel(g2, mT, shp, symbol,new FLabel("Abcd"));
					this.setFontSizeInPixels(bAux);
				}
				break;
		}
  		return xmlBuilder.getXML();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }

    /**
     * creates an FSymbol from an SLD
     */
    public String fromSLD (String sld)
    {
    	//TODO: This function can be implemented later...
    	StringBuffer sb = new StringBuffer();
    	try
    	{

    		return sb.toString();
    	}
    	catch(Exception e)
    	{
    		e.printStackTrace();
    		return null;
    	}
    }

	/**
	 * @return Returns the imgObserver.
	 */
	public ImageObserver getImgObserver() {
		return imgObserver;
	}

	/**
	 * @param imgObserver The imgObserver to set.
	 */
	public void setImgObserver(ImageObserver imgObserver) {
		this.imgObserver = imgObserver;
	}

	public ISymbol getSymbolForSelection() {
		return getSymbolForSelection(this);
	}

	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp, Cancellable cancel) {
		FGraphicUtilities.DrawShape(g, affineTransform, shp, this);
	}

	public void getPixExtentPlus(FShape shp, float[] distances, ViewPort viewPort, int dpi) {
		
	}

	public boolean isSuitableFor(IGeometry geom) {
		return true;
	}

	public void drawInsideRectangle(Graphics2D g2, AffineTransform scaleInstance, Rectangle r, PrintRequestAttributeSet properties) throws SymbolDrawingException {
		FGraphicUtilities.DrawSymbol(g2, scaleInstance, r, this);
	}

	public String getClassName() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setXMLEntity(XMLEntity xml) {
		// TODO Auto-generated method stub

	}

	public void print(Graphics2D g, AffineTransform at, FShape shape, PrintRequestAttributeSet properties){
		int strokeValue=0;
		BasicStroke stroke=(BasicStroke)this.getStroke();
		if (stroke != null && stroke.getLineWidth()!=0) {
			strokeValue=(int)stroke.getLineWidth();
			double d = strokeValue;
			PrintQuality pq = (PrintQuality) properties.get(PrintQuality.class);
			if (pq.equals(PrintQuality.NORMAL)){
				d *= (double) 300/72;
			}else if (pq.equals(PrintQuality.HIGH)){
				d *= (double) 600/72;
			}else if (pq.equals(PrintQuality.DRAFT)){
				//	d *= 72/72; (which is the same than doing nothing)
			}
			this.setStroke(new BasicStroke((int)d,BasicStroke.CAP_ROUND,BasicStroke.JOIN_ROUND));
		}
		draw(g, at, shape, null);
		this.setStroke(new BasicStroke(strokeValue));
	}

}
