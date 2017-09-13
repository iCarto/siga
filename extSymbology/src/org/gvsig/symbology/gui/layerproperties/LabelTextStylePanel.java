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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JComboBoxFonts;
import org.gvsig.gui.beans.swing.JIncrementalNumberField;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.CartographicSupport;
import com.iver.cit.gvsig.fmap.core.symbols.ITextSymbol;
import com.iver.cit.gvsig.fmap.core.symbols.SimpleTextSymbol;
import com.iver.cit.gvsig.gui.JComboBoxUnits;
import com.iver.cit.gvsig.gui.panels.ColorChooserPanel;
import com.iver.cit.gvsig.gui.styling.JComboBoxUnitsReferenceSystem;

public class LabelTextStylePanel extends JPanel implements ActionListener{
	private static final long serialVersionUID = 8351591938535233138L;
	private ITextSymbol symbol;
	private JComboBoxFonts cmbFont;
	private ColorChooserPanel colorFont;
	private JRadioButton rdBtnTextHeight;
	private JRadioButton rdBtnFitOnTextArea;
	private JIncrementalNumberField incrTextSize;
	private JComboBoxUnits units;
	private JComboBoxUnitsReferenceSystem referenceSystem;

	private JCheckBox chkWithHalo;
	private ColorChooserPanel colorHaloChooser;
	private JIncrementalNumberField txtHaloWidth;

	private ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
	private boolean performAction = true;
	
	private JCheckBox getCheckHalo() {
		if (chkWithHalo == null) {
			chkWithHalo = new JCheckBox(PluginServices.getText(this, "chk_halo") + ":");
			chkWithHalo.setSelected(false);
			chkWithHalo.setName("CHECK_HALO");
		}
		return chkWithHalo;
	}
	private ColorChooserPanel getHaloColorChooser() {
		if (colorHaloChooser == null){
			colorHaloChooser = new ColorChooserPanel(true);
			colorHaloChooser.setColor(Color.WHITE);
		}
		return colorHaloChooser;
	}
	

	private JIncrementalNumberField getTxtHaloWidth() {
		if (txtHaloWidth == null) {
			txtHaloWidth = new JIncrementalNumberField("3");
		}
		return txtHaloWidth;
	}



	public LabelTextStylePanel() {
		setLayout(new BorderLayout(10, 2));
		JPanel aux = new JPanel();
		aux.setLayout(new BoxLayout(aux,BoxLayout.Y_AXIS));
		JPanel auxA = new JPanel();
		aux.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(this, "font")));
		auxA.add(new JLabel(PluginServices.getText(this, "font")));
		auxA.add(cmbFont = new JComboBoxFonts(),BorderLayout.NORTH);
		JPanel auxB = new JPanel();
		auxB.add(new JLabel(PluginServices.getText(this, "color")));
		auxB.add(colorFont = new ColorChooserPanel(true),BorderLayout.SOUTH);
		cmbFont.addActionListener(this);
		colorFont.addActionListener(this);
		aux.add(auxA);
		aux.add(auxB);
		add(aux, BorderLayout.WEST);

		aux = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 2));
		aux.setBorder(BorderFactory.createTitledBorder(PluginServices.getText(this, "format")));
		rdBtnTextHeight = new JRadioButton(PluginServices.getText(this, "fixed_text_size"));
		rdBtnFitOnTextArea = new JRadioButton(PluginServices.getText(this, "fit_on_text_area"));
		rdBtnFitOnTextArea.addActionListener(this);
		rdBtnTextHeight.addActionListener(this);
		ButtonGroup g = new ButtonGroup();
		g.add(rdBtnFitOnTextArea);
		g.add(rdBtnTextHeight);
		incrTextSize = new JIncrementalNumberField(
				"1",
				7,
				0,
				Double.POSITIVE_INFINITY,
				1);
		incrTextSize.addActionListener(this);
		units = new JComboBoxUnits();
		referenceSystem = new JComboBoxUnitsReferenceSystem();
		units.addActionListener(this);
		referenceSystem.addActionListener(this);

		GridBagLayoutPanel aux2 = new GridBagLayoutPanel();
		JPanel aux3 = new JPanel(new FlowLayout(FlowLayout.LEFT, 3, 0));
		aux3.add(rdBtnTextHeight);
		aux3.add(incrTextSize);

		aux3.add(units);
		aux3.add(referenceSystem);

		aux2.addComponent(aux3);
		GridBagLayoutPanel aux4 = new GridBagLayoutPanel();
		aux4.addComponent(rdBtnFitOnTextArea);
		
		// Para reflejar los cambios.
		getCheckHalo().addActionListener(this);
		getHaloColorChooser().addActionListener(this);
		getTxtHaloWidth().addActionListener(this);
		aux4.addComponent(getCheckHalo(), getHaloColorChooser());
		aux4.addComponent(PluginServices.getText(null, "HaloWidth") + ":", getTxtHaloWidth());
	
		aux2.addComponent(aux4);
		aux.add(aux2);

		add(aux, BorderLayout.CENTER);
	}



	public void setModel(ITextSymbol textSymbol, int unit, int referenceSystem) {
		incrTextSize.setDouble(textSymbol.getFont().getSize());
		rdBtnTextHeight.setSelected(!textSymbol.isAutoresizeEnabled());
		rdBtnFitOnTextArea.setSelected(textSymbol.isAutoresizeEnabled());
		getCheckHalo().setSelected(textSymbol.isDrawWithHalo());
		if (textSymbol.isDrawWithHalo()) {
			getHaloColorChooser().setColor(textSymbol.getHaloColor());
			getTxtHaloWidth().setDouble(textSymbol.getHaloWidth());
		}
		performAction = false;
		cmbFont.setSelectedItem(textSymbol.getFont().getName());
		colorFont.setColor(textSymbol.getTextColor());
		this.units.setSelectedUnitIndex(unit);
		this.referenceSystem.setSelectedIndex(referenceSystem);
		boolean enableSize = rdBtnTextHeight.isSelected();
		this.units.setEnabled(enableSize);
		this.referenceSystem.setEnabled(enableSize);
		this.incrTextSize.setEnabled(enableSize);
		performAction = true;
		this.symbol = textSymbol;
	}

	public ITextSymbol getTextSymbol() {
		symbol.setDrawWithHalo(chkWithHalo.isSelected());
		if (symbol.isDrawWithHalo()) {
			symbol.setHaloColor(colorHaloChooser.getColor());
			symbol.setHaloWidth((float) txtHaloWidth.getDouble());
		}

		return symbol;
	}


	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}

	public void actionPerformed(ActionEvent e) {
		if (performAction) {
			boolean enableSize = rdBtnTextHeight.isSelected();
			incrTextSize.setEnabled(enableSize);
			units.setEnabled(enableSize);
			referenceSystem.setEnabled(enableSize);

			if (symbol != null) {
				symbol.setAutoresizeEnabled(rdBtnFitOnTextArea.isSelected());
				symbol.setFont(
						new Font(
								(String) cmbFont.getSelectedItem(),
								Font.PLAIN,
								(int) incrTextSize.getDouble()));
				symbol.setTextColor(colorFont.getColor());
				symbol.setFontSize(incrTextSize.getDouble());
				if(symbol instanceof SimpleTextSymbol){
					SimpleTextSymbol myText = (SimpleTextSymbol) symbol;
					myText.setUnit(units.getSelectedUnitIndex());
					myText.setReferenceSystem(referenceSystem.getSelectedIndex());
					myText.setDrawWithHalo(chkWithHalo.isSelected());
					if (myText.isDrawWithHalo()) {
						myText.setHaloColor(colorHaloChooser.getColor());
						myText.setHaloWidth((float) txtHaloWidth.getDouble());
					}
				}
			}
			for (ActionListener l : listeners) {
				l.actionPerformed(new ActionEvent(this, 0, null));
			}
		}
	}

	public int getUnit() {
		return this.units.getSelectedUnitIndex();
	}

	public int getReferenceSystem() {
		return this.referenceSystem.getSelectedIndex();
	}
}
