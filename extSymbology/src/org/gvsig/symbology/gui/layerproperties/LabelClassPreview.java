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
package org.gvsig.symbology.gui.layerproperties;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.awt.geom.AffineTransform;

import javax.swing.JPanel;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.styles.ILabelStyle;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.LabelClass;

public class LabelClassPreview extends JPanel {
	private static final long serialVersionUID = 3788548343944220354L;
	private Dimension sz = new Dimension(100, 200);
	private LabelClass labelClass;

	@Override
	public Dimension getPreferredSize() {
		Dimension sz = labelClass == null ? this.sz : labelClass.getBounds().getSize();
		int w = sz.width;
		int h = sz.height;

		if (w>300) {
			w = 300;
		}

		if (h>200) {
			h = 200;
		}
		sz.setSize(w, h);
		return sz;
	}

	@Override
	protected void paintComponent(Graphics g) {
		if (labelClass == null)
			return;
		Rectangle r = getBounds();
		r.setLocation(0, 0);
		g.setColor(Color.white);
		((Graphics2D) g).fill(r);
		((Graphics2D) g).setStroke(new BasicStroke(2));
		g.setColor(Color.black);
		((Graphics2D) g).draw(r);
		ILabelStyle sty = labelClass.getLabelStyle();
		String cad;
		if (sty != null) {
			int fc = sty.getFieldCount();
			String[] texts = new String[fc];
			for (int i = 0; i < texts.length; i++) {
				texts[i] = PluginServices.getText(this, "text_field") + " "+(i+1);
			}
			labelClass.setTexts(texts);
		} else {
			labelClass.setTexts(new String[] {PluginServices.getText(this, "text_field")});
		}

//		try {
//			labelClass.drawInsideRectangle((Graphics2D) g, r);
			ITextSymbol sym = labelClass.getTextSymbol();
			Font myFont = new Font(sym.getFont().getName(),sym.getFont().getStyle(),35);
			AffineTransform saveAT = ((Graphics2D)g).getTransform();
			
//			AffineTransform tx = new AffineTransform();
//			tx.setToTranslation(0, -r.getCenterY());
			saveAT.translate(0, r.getCenterY());
			((Graphics2D) g).setTransform(saveAT);
			FontRenderContext frc = new FontRenderContext(
					saveAT, false, true);;
			g.setFont(myFont);
			String text = PluginServices.getText(this, "text_field");
			
			if (sym.isDrawWithHalo()) {
				BasicStroke haloStroke = new BasicStroke(sym.getHaloWidth());
				char[] charText = new char[text.length()];
				text.getChars(0, charText.length, charText, 0);
				GlyphVector glyph = myFont.layoutGlyphVector(frc, charText, 0, charText.length, Font.LAYOUT_NO_LIMIT_CONTEXT);
				g.setColor(sym.getHaloColor());
				((Graphics2D) g).setStroke(haloStroke);
				((Graphics2D) g).draw(glyph.getOutline());
			}
			g.setColor(sym.getTextColor());
			g.drawString(PluginServices.getText(this, "text_field"), 0, 0);
//		} catch (SymbolDrawingException e) {
//			SymbologyFactory
//					.getWarningSymbol(
//							PluginServices.getText(this,
//									"cant_draw_preview"),
//							SymbolDrawingException.STR_UNSUPPORTED_SET_OF_SETTINGS,
//							SymbolDrawingException.UNSUPPORTED_SET_OF_SETTINGS);
//		}
	}

	public void setLabelClass(LabelClass labelClass) {
		this.labelClass = labelClass;
	}
}