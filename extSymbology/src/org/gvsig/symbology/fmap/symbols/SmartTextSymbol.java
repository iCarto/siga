/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2005-8 IVER T.I. and Generalitat Valenciana.
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
* $Id: SmartTextSymbol.java 13953 2007-09-21 12:26:04Z jaume $
* $Log$
* Revision 1.6  2007-09-21 12:25:32  jaume
* cancellation support extended down to the IGeometry and ISymbol level
*
* Revision 1.5  2007/08/16 06:55:19  jvidal
* javadoc updated
*
* Revision 1.4  2007/08/13 11:36:50  jvidal
* javadoc
*
* Revision 1.3  2007/03/28 16:48:14  jaume
* *** empty log message ***
*
* Revision 1.2  2007/03/21 17:36:22  jaume
* *** empty log message ***
*
* Revision 1.1  2007/03/09 11:20:56  jaume
* Advanced symbology (start committing)
*
* Revision 1.1.2.8  2007/02/21 07:34:09  jaume
* labeling starts working
*
* Revision 1.1.2.7  2007/02/16 10:54:12  jaume
* multilayer splitted to multilayerline, multilayermarker,and  multilayerfill
*
* Revision 1.1.2.6  2007/02/15 16:23:44  jaume
* *** empty log message ***
*
* Revision 1.1.2.5  2007/02/09 07:47:05  jaume
* Isymbol moved
*
* Revision 1.1.2.4  2007/02/08 07:36:38  jaume
* *** empty log message ***
*
* Revision 1.1.2.3  2007/02/06 16:54:36  jaume
* *** empty log message ***
*
* Revision 1.1.2.2  2007/02/06 16:47:35  jaume
* first steps, follows a line
*
* Revision 1.1.2.1  2007/02/02 16:21:24  jaume
* start commiting labeling stuff
*
* Revision 1.1  2007/01/24 17:58:22  jaume
* new features and architecture error fixes
*
*
*/
package org.gvsig.symbology.fmap.symbols;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.font.LineMetrics;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;

import javax.print.attribute.PrintRequestAttributeSet;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JComboBoxFonts;
import org.gvsig.gui.beans.swing.JIncrementalNumberField;
import org.gvsig.symbology.fmap.labeling.placements.PointPlacementConstraints;

import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.core.FPolygon2D;
import com.iver.cit.gvsig.fmap.core.FPolyline2D;
import com.iver.cit.gvsig.fmap.core.FShape;
import com.iver.cit.gvsig.fmap.core.GeneralPathX;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.core.TextPath;
import com.iver.cit.gvsig.fmap.core.symbols.IFillSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SymbolDrawingException;
import com.iver.cit.gvsig.fmap.core.v02.FConstant;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.IPlacementConstraints;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.JComboBox;
import com.iver.utiles.swing.threads.Cancellable;

/**
 * Class used to create symbols composed using a text defined by
 * the user.This text can be edited (changing the color, the font of the characters, and
 * the rotation of the text)and has the property that can follow a path.If this path
 * does not exist, the text is treated as a simpletextsymbol (when is drawn).
 * @author   jaume dominguez faus - jaume.dominguez@iver.es
 */
public class SmartTextSymbol extends SimpleTextSymbol implements ITextSymbol {

	private char[] charText;
//	Background: ITextBackground
//	Case
	private double characterSpacing;
	private double characterWidth;
//	Direction
	private IFillSymbol fillSymbol;
	private double flipAngle;
//	boolean kerning;
	private double leading;
//	Position: textPosition
	private Color ShadowColor;
	private double ShadowXOffset;
	private double ShadowYOffset;
//	TypeSetting: Boolean
	private double wordSpacing;
//	ISimpleTextSymbol : ITextSymbol
//	BreakCharacter: Long
//	Clip: Boolean
	private TextPath textPath;
	private double xOffset;
	private double yOffset;
	private double angle;
//	Color: IColor

//	HorizontalAlignment:
//	esriTextHorizontalAlignment
	private boolean rightToLeft;
	//	VerticalAlignment
	private double maskSize;
//	MaskStyle
	private  IFillSymbol maskSymbol;
	private double margin;
	private int alignment;
	private boolean kerning = false;
	private TextPath tp;
	private IPlacementConstraints constraints;

	public SmartTextSymbol(ITextSymbol sym, IPlacementConstraints constraints) {

		if(sym instanceof SimpleTextSymbol){
			SimpleTextSymbol mySym = (SimpleTextSymbol)sym;

			this.setAutoresizeEnabled(mySym.isAutoresizeEnabled());
			this.setDescription(mySym.getDescription());
			this.setFont(mySym.getFont());
			this.setFontSize(mySym.getFont().getSize());
			this.setIsShapeVisible(mySym.isShapeVisible());
			this.setReferenceSystem(mySym.getReferenceSystem());
			this.setRotation(mySym.getRotation());
			this.setText(mySym.getText());
			this.setTextColor(mySym.getTextColor());
			this.setUnit(mySym.getUnit());
			super.setText(this.getText());
			this.constraints = constraints;

			setCharacterSpacing(2); //???
			setWordSpacing(TextPath.DEFAULT_WORD_SPACING);
			boolean rtl = false; // right to left text
			if (constraints.isAtTheBeginingOfLine()) {
				if (rtl) {
					setAlignment(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_RIGHT);
				}
				else {
					setAlignment(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_LEFT);
				}
			}
			else if (constraints.isAtTheEndOfLine()) {
				if (rtl) {
					setAlignment(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_LEFT);
				}
				else {
					setAlignment(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_RIGHT);
				}
			}
			else { //constraints.isInTheMiddleOfLine() or constraints.isAtBestOfLine()
				setAlignment(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_CENTERED);
			}
			setKerning(false);
			setRightToLeft(rtl);
		}
	}
	public SmartTextSymbol() {
		PointPlacementConstraints pc = new PointPlacementConstraints();
		this.constraints = pc;
		
	}

	/**
	 * Draws the text according. If this symbol has the text path set, then
	 * it is used as the text line, otherwise shp <b>must be an FPoint2D</b>
	 * indicating the starting point of the text and then the text will
	 * be rendered from there and following the rotation previously set.
	 */
	@Override
	public void draw(Graphics2D g, AffineTransform affineTransform, FShape shp, Cancellable cancel) {
		if (!isShapeVisible()) return;

		setMargin(0);


		tp = new TextPath(g, shp, charText, getFont(),
				(float) characterSpacing, (float) characterWidth, kerning,
				(float) leading, alignment, (float) wordSpacing, (float) margin, rightToLeft);
		Font font = getFont();
		g.setFont(font);
		FontRenderContext frc = g.getFontRenderContext();
		LineMetrics lineMetrics = font.getLineMetrics(getText(), frc);
		
//		GlyphVector glyph =  font.layoutGlyphVector(frc, charText, 0, charText.length, Font.LAYOUT_NO_START_CONTEXT);
		double cons = 0;

		/* Repartimos el leading (espacio de separación entre lineas)
		 * arriba y abajo para que exista la misma separación entre la
		 * caja de la letra y la linea tanto si se dibuja por abajo como
		 * si se dibuja por arriba. 
		 */
		if(this.constraints.isAboveTheLine()) {
			cons = lineMetrics.getDescent()+lineMetrics.getLeading()/2;
		}
		else if (this.constraints.isBelowTheLine()) {
			cons = -(lineMetrics.getAscent()+lineMetrics.getLeading()/2);
		}
		/* Dibujamos la letra de tal manera que el centro de la caja de letra
		 * coincida con la linea
		 */
		else if(this.constraints.isOnTheLine()) {
//			cons = lineMetrics.getDescent()+(lineMetrics.getLeading()/2)-(lineMetrics.getHeight()/2);
			cons = lineMetrics.getDescent()+lineMetrics.getLeading()-(lineMetrics.getHeight()/2);
		}
		
		double[] coords = tp.nextPosForGlyph(0);
		Stroke haloStroke = new BasicStroke(getHaloWidth(), BasicStroke.CAP_ROUND,
                BasicStroke.JOIN_ROUND);

		for (int i = 0; i < tp.getGlyphCount(); i++) {
			coords = tp.nextPosForGlyph(i);
			if (coords[0] == TextPath.NO_POS || coords[1] == TextPath.NO_POS)
				continue;

			// move the label 'cons" units above/below the line
			double xOffset = cons * Math.sin(coords[2]);
			double yOffset = cons * Math.cos(coords[2]);

			g.translate(coords[0]+xOffset, coords[1]-yOffset);
			g.rotate(coords[2]);

			char[] aux = new char[1];
			aux[0] = charText[i];
			if (isDrawWithHalo()) {
				GlyphVector glyph = font.createGlyphVector(frc, aux);
				Shape outlineChar = glyph.getOutline();
				g.setStroke(haloStroke);
				g.setColor(getHaloColor());
				g.draw(outlineChar);
			}
			
			g.setColor(this.getTextColor());
			g.drawString(String.valueOf(charText[i]), 0, 0);
			g.rotate(-coords[2]);
			g.translate(-coords[0]-xOffset, -coords[1]+yOffset);
		}
	}

	@Override
	public void getPixExtentPlus(FShape shp, float[] distances, ViewPort viewPort, int dpi) {
		// TODO Implement it
		throw new Error("Not yet implemented!");

	}

	@Override
	public int getOnePointRgb() {
		return getTextColor().getRGB();
	}

	@Override
	public XMLEntity getXMLEntity() {
		XMLEntity xml = new XMLEntity();
		xml.putProperty("className", getClassName());
		xml.putProperty("desc", getDescription());
		xml.putProperty("isShapeVisible", isShapeVisible());
		return xml;
	}

	@Override
	public int getSymbolType() {
		return FShape.TEXT;
	}

	@Override
	public boolean isSuitableFor(IGeometry geom) {
		return (geom.getGeometryType()%FShape.Z) == FShape.LINE;
	}

	@Override
	public void drawInsideRectangle(Graphics2D g, AffineTransform scaleInstance, Rectangle r, PrintRequestAttributeSet properties) throws SymbolDrawingException {
		// let's take the bottom segment of the rectangle as the line

		GeneralPathX gpx = new GeneralPathX();
		gpx.moveTo(r.getX(), r.getY());
		gpx.lineTo(r.getX()+r.getWidth(), r.getY());
		if (properties==null)
			draw(g, scaleInstance, new FPolygon2D(gpx), null);
		else
			print(g, scaleInstance, new FPolygon2D(gpx), properties);

	}

	@Override
	public String getClassName() {
		return getClass().getName();
	}

	@Override
	public void setXMLEntity(XMLEntity xml) {
		setFont(new Font("Arial", Font.PLAIN, 18));
		setText("this is my TEST text that follows a line");
		setDescription(xml.getStringProperty("desc"));
		setIsShapeVisible(xml.getBooleanProperty("isShapeVisible"));

	}

	@Override
	public void setText(String text) {
		this.charText = text.toCharArray();
	}

	@Override
	public String getText() {
		return new String(charText);
	}

	public void setCharacterSpacing(double charSpacing) {
		this.characterSpacing = charSpacing;
	}

	public void setWordSpacing(double wordSpacing) {
		this.wordSpacing = wordSpacing;
	}

	public void setAlignment(int alignment) {
		this.alignment = alignment;
	}

	public void setKerning(boolean kerning) {
		this.kerning = kerning;
	}

	public void setMargin(double margin) {
		this.margin = margin;
	}

	public void setRightToLeft(boolean rightToLeft) {
		this.rightToLeft = rightToLeft;
	}

	public static void main(String[] args) {
		class Item {
			int value;
			String text;
			public Item(int value, String text) {
				this.value = value;
				this.text = text;
			}
			@Override
			public String toString() {
				return text;
			}

			public int getValue() {
				return value;
			}
		}

		final int initialWidth = 300;
		final int initialHeight = 300;

		final JTextField textField = new JTextField("write your text here");
		final JIncrementalNumberField textFontSize = new JIncrementalNumberField();
		textFontSize.setDouble(15);
		final JIncrementalNumberField textCharSpacing = new JIncrementalNumberField();
		textCharSpacing.setDouble(1);
		final JIncrementalNumberField textWordSpacing = new JIncrementalNumberField();
		textCharSpacing.setDouble(10);
		final JCheckBox chkKerning = new JCheckBox("Kerning:");
		final JCheckBox chkRightToLeft = new JCheckBox("Rigth to left:");
		final JComboBoxFonts cmbFonts = new JComboBoxFonts();

		ArrayList<Integer> alignments = new ArrayList<Integer>();
		final JComboBox cmbAlign = new JComboBox(new Item[] {
				new Item(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_LEFT, "LEFT"),
				new Item(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_RIGHT, "RIGHT"),
				new Item(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_CENTERED, "CENTER"),
				new Item(FConstant.SYMBOL_STYLE_TEXTALIGNMENT_JUSTIFY, "JUSTIFY"),
		});

		final JIncrementalNumberField txtMargin = new JIncrementalNumberField();
		txtMargin.setDouble(10);



		final JFrame f = new JFrame("test Smart Text Symbol");
		JPanel content = new JPanel(new BorderLayout(20,20));
		GridBagLayoutPanel controlPanel = new GridBagLayoutPanel();
		controlPanel.addComponent("Text: ", textField);
		controlPanel.addComponent("Font: ", cmbFonts);
		controlPanel.addComponent("Font size: ", textFontSize);
		controlPanel.addComponent("Char spacing: ", textCharSpacing);
		controlPanel.addComponent("Word spacing: ", textWordSpacing);
		controlPanel.addComponent("Alignment: ", cmbAlign);
		controlPanel.addComponent("Margin: ", txtMargin);
		controlPanel.addComponent(chkKerning);
		controlPanel.addComponent(chkRightToLeft);

		content.add(controlPanel, BorderLayout.NORTH);

		final JPanel canvas = new JPanel() {
			private static final long serialVersionUID = 623038680274774722L;

			@Override
			protected void paintComponent(Graphics g) {
				Graphics2D g2 = (Graphics2D) g;
				double width = getBounds().getWidth();
				double height= getBounds().getHeight();
				GeneralPathX gpx = new GeneralPathX();

				gpx.moveTo(50, 100);
				gpx.lineTo(100, 50);
				gpx.lineTo(150, 100);
				gpx.lineTo(250, 50);

				SmartTextSymbol sms = new SmartTextSymbol();
				FPolyline2D theLineShape = new FPolyline2D(gpx);
				SymbologyFactory.createDefaultLineSymbol().draw(g2, null, theLineShape, null);
				sms.setFont(new Font((String) cmbFonts.getSelectedItem(), Font.PLAIN, 10));
				sms.setFontSize(textFontSize.getDouble());
				sms.setText(textField.getText());
				sms.setCharacterSpacing(textCharSpacing.getDouble());
				sms.setWordSpacing(textWordSpacing.getDouble());
				sms.setAlignment(((Item) cmbAlign.getSelectedItem()).getValue());
				sms.setKerning(chkKerning.isSelected());
				sms.setRightToLeft(chkRightToLeft.isSelected());
				sms.setMargin(txtMargin.getDouble());
				sms.draw(g2, null, theLineShape, null);
			}
		};

		ActionListener action = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				f.repaint();
			}
		};
		textFontSize.addActionListener(action);
		textCharSpacing.addActionListener(action);
		textWordSpacing.addActionListener(action);
		cmbAlign.addActionListener(action);
		txtMargin.addActionListener(action);
		chkKerning.addActionListener(action);
		chkRightToLeft.addActionListener(action);
		cmbFonts.addActionListener(action);


		canvas.setPreferredSize(new Dimension(initialWidth, initialHeight));
		canvas.setSize(new Dimension(initialWidth, initialHeight));
		content.add(canvas, BorderLayout.CENTER);
		f.setContentPane(content);
		f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


		f.pack();
		f.setVisible(true);
	}

}
