/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
package org.gvsig.symbology.gui.layerproperties;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;

import javax.swing.JPanel;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.core.styles.ILabelStyle;
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
			Font myFont = new Font(labelClass.getTextSymbol().getFont().getName(),labelClass.getTextSymbol().getFont().getStyle(),35);
			g.setFont(myFont);
			g.drawString(PluginServices.getText(this, "text_field"), 0, (int)r.getCenterY());
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