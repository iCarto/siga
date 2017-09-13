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
package com.iver.cit.gvsig.project.documents.view.legend.gui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;

import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JBlank;
import org.gvsig.gui.beans.swing.JIncrementalNumberField;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.cit.gvsig.fmap.core.SymbologyFactory;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.AttrInTableLabelingStrategy;
import com.iver.cit.gvsig.fmap.rendering.styling.labeling.ILabelingStrategy;
import com.iver.cit.gvsig.gui.JComboBoxUnits;
import com.iver.cit.gvsig.gui.panels.ColorChooserPanel;
import com.iver.cit.gvsig.gui.styling.JComboBoxUnitsReferenceSystem;
import com.iver.cit.gvsig.gui.utils.FontChooser;
import com.iver.utiles.swing.JComboBox;

public class AttrInTableLabeling extends JPanel implements  ILabelingStrategyPanel{
	private static final long serialVersionUID = 8229927418031917075L;
	private static final String NO_FIELD_ITEM = "-- " +
	PluginServices.getText(LabelingManager.class, "none") + " --";
	private String[] fieldNames;
	private String[] numericFieldNames;
	private String[] integerFieldNames;

	private JRadioButton rdBtnFixedHeight;
	private JRadioButton rdBtnHeightField;
	private JRadioButton rdBtnFixedColor;
	private JRadioButton rdBtnColorField;
	private JComboBox cmbTextField;
	private JComboBox cmbHeightField;
	private JComboBox cmbRotationField;
	private JComboBoxUnits cmbUnits;
	private JComboBoxUnitsReferenceSystem cmbReferenceSystem;
	private JTextField txtHeightField;
	private FLyrVect layer;

	private ColorChooserPanel colorChooser;
	private JComboBox cmbColorField;

	private JButton chooseFontBut;
	private Font labelFont = SymbologyFactory.DefaultTextFont;
	
	private JCheckBox chkWithHalo;
	private ColorChooserPanel colorHaloChooser;
	private JIncrementalNumberField txtHaloWidth;

	public AttrInTableLabeling() {
		initialize();
	}

	private void initialize() {
		setLayout(new FlowLayout(FlowLayout.LEFT, 10, 10));
		GridBagLayoutPanel panel = new GridBagLayoutPanel();

		GridBagLayoutPanel aux = new GridBagLayoutPanel();
		aux.addComponent(PluginServices.getText(this, "field_to_be_labeled") + ":", getCmbTextField());
		aux.addComponent(getRdBtnHeightField(), getCmbHeightField());
		aux.addComponent(getRdBtnFixedHeight(), getTxtHeightField());
		aux.addComponent(PluginServices.getText(this, "rotation_height") + ":", getCmbRotationField());
		aux.addComponent(PluginServices.getText(this, "units") + ":", getCmbUnits());
		aux.addComponent(PluginServices.getText(this,""),getCmbReferenceSystem());
		panel.add(aux);

		aux = new GridBagLayoutPanel();
		aux.addComponent(getChooseFontBut(),new JBlank(20,20));
		GridBagLayoutPanel aux2 = new GridBagLayoutPanel();
		aux2.setBorder(BorderFactory.createTitledBorder(null,PluginServices.getText(this,"color")));
		aux2.addComponent(getRdBtnFixedColor(),getColorChooser());
		aux2.addComponent(getRdBtnColorField(),getCmbColorField());
		aux2.addComponent(getCheckHalo(), getHaloColorChooser());
		aux2.addComponent(PluginServices.getText(null, "HaloWidth") + ":", getTxtHaloWidth());
		aux.addComponent(aux2);

		panel.add(new JBlank(20,20));
		panel.add(aux);


		add(panel);


		ButtonGroup group = new ButtonGroup();
		group.add(getRdBtnFixedHeight());
		group.add(getRdBtnHeightField());

		ButtonGroup colorGroup = new ButtonGroup();
		colorGroup.add(getRdBtnFixedColor());
		colorGroup.add(getRdBtnColorField());

//		getRdBtnHeightField().setEnabled(true);
	}


	private JIncrementalNumberField getTxtHaloWidth() {
		if (txtHaloWidth == null) {
			txtHaloWidth = new JIncrementalNumberField("3");
		}
		return txtHaloWidth;
	}

	private ColorChooserPanel getColorChooser() {
		if (colorChooser == null){
			colorChooser = new ColorChooserPanel(true);
		}
		return colorChooser;
	}

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

	private JButton getChooseFontBut() {
		if(chooseFontBut == null){
			chooseFontBut = new JButton(PluginServices.getText(this,"font"));
			chooseFontBut.setPreferredSize(new Dimension(80,10));
			chooseFontBut.addActionListener(new ActionListener(){

				public void actionPerformed(ActionEvent e) {
					Font newFont;

					newFont = FontChooser.showDialog("Choose Font", labelFont);
					if (newFont == null) {
						return;
					}

					labelFont = newFont;
				}

			});
		}
		return chooseFontBut;
	}

	private JRadioButton getRdBtnFixedHeight() {
		if (rdBtnFixedHeight == null) {
			rdBtnFixedHeight = new JRadioButton(PluginServices.getText(this, "fixed_height") + ":");
			rdBtnFixedHeight.setSelected(true);
			rdBtnFixedHeight.setName("RDFIXEDHEIGHT");
		}

		return rdBtnFixedHeight;
	}

	private JRadioButton getRdBtnHeightField() {
		if (rdBtnHeightField == null) {
			rdBtnHeightField = new JRadioButton(PluginServices.getText(this, "text_height_field") + ":");
			rdBtnHeightField.setSelected(false);
			rdBtnHeightField.setName("RDHEIGHTFIELD");
		}

		return rdBtnHeightField;
	}

	private JRadioButton getRdBtnFixedColor() {
		if (rdBtnFixedColor == null) {
			rdBtnFixedColor = new JRadioButton(PluginServices.getText(this, "fixed_color") + ":");
			rdBtnFixedColor.setSelected(true);
			rdBtnFixedColor.setName("RDFIXEDCOLOR");
		}

		return rdBtnFixedColor;
	}

	private JRadioButton getRdBtnColorField() {
		if (rdBtnColorField == null) {
			rdBtnColorField = new JRadioButton(PluginServices.getText(this, "color_field") + ":");
			rdBtnColorField.setSelected(false);
			rdBtnColorField.setName("RDCOLORFIELD");
		}

		return rdBtnColorField;
	}

	private JComboBoxUnits getCmbUnits() {
		if (cmbUnits == null) {
			cmbUnits = new JComboBoxUnits();
			cmbUnits.setName("CMBUNITS");
		}

		return cmbUnits;
	}

	private JComboBoxUnitsReferenceSystem getCmbReferenceSystem(){
		if(cmbReferenceSystem == null){
			cmbReferenceSystem = new JComboBoxUnitsReferenceSystem();
			cmbReferenceSystem.setName("CMBREFSYST");
		}
		return cmbReferenceSystem;
	}

	private JComboBox getCmbColorField() {
		if (cmbColorField == null) {
			cmbColorField = new JComboBox();
			cmbColorField.setName("CMBCOLOR");
		}

		return cmbColorField;
	}

	private void refreshControls() {
		// When the attributes are in the table -----
		//      field with the text
		refreshCmbTextField();

		//      field with the rotation
		refreshCmbRotationField();

		//      field with the text height or the text size
		refreshTextHeight();

		//		the text size unit name
		refreshCmbUnits();

		refreshCmbRefSystem();
		//		the font for the text
		refreshFont();
		//		the color for the font AND halo with and haloColor
		refreshColorFont();
	}

	private JComboBox getCmbRotationField() {
		if (cmbRotationField == null) {
			cmbRotationField = new JComboBox();
			cmbRotationField.setPreferredSize(new Dimension(200, 20));
			cmbRotationField.setName("CMBROTATIONFIELD");
		}
		return cmbRotationField;
	}

	private JComboBox getCmbHeightField() {
		if (cmbHeightField == null) {
			cmbHeightField = new JComboBox();
			cmbHeightField.setPreferredSize(new Dimension(200, 20));
			cmbHeightField.setName("CMBHEIGHTFIELD");
		}
		return cmbHeightField;
	}

	private JComboBox getCmbTextField() {
		if (cmbTextField == null) {
			cmbTextField = new JComboBox();
			cmbTextField.setPreferredSize(new Dimension(200, 20));
			cmbTextField.setName("CMBTEXTFIELD");
		}
		return cmbTextField;
	}


	private JTextField getTxtHeightField() {
		if (txtHeightField == null) {
			txtHeightField = new JTextField(10);
			txtHeightField.setText("10");
			txtHeightField.setName("TXTHEIGHTFIELD");
		}

		return txtHeightField;
	}

	public void actionPerformed(ActionEvent e) {

	}

	public ILabelingStrategy getLabelingStrategy() {
		// user selected to define each label attributes from values
		// contained in the table for each feature row.

		double fixedSize;
		try {
			fixedSize = Double.parseDouble(getTxtHeightField().getText());
		} catch (Exception e) {
			fixedSize = 10;
		}
		AttrInTableLabelingStrategy strategy = new AttrInTableLabelingStrategy();
		strategy.setLayer(layer);

		if(getCmbHeightField().getItemCount() > 0 && !rdBtnFixedHeight.isSelected())
			strategy.setHeightField(
				(String) getCmbHeightField().getSelectedItem());
		if(getCmbRotationField().getItemCount() > 0)
			if(!getCmbRotationField().getSelectedItem().equals(NO_FIELD_ITEM))
				strategy.setRotationField(
						(String) getCmbRotationField().getSelectedItem());
			else
				strategy.setRotationField(null);

		if(getCmbTextField().getItemCount() > 0)
			strategy.setTextField(
				(String) getCmbTextField().getSelectedItem());

		strategy.setUsesFixedSize(getRdBtnFixedHeight().isSelected());
		strategy.setFixedSize(fixedSize);

		if(getCmbUnits().getItemCount() > 0)
			strategy.setUnit(getCmbUnits().getSelectedUnitIndex());
		if(getCmbReferenceSystem().getItemCount() > 0)
			strategy.setReferenceSystem(getCmbReferenceSystem().getSelectedIndex());

		strategy.setUsesFixedColor(getRdBtnFixedColor().isSelected());
		strategy.setFixedColor(getColorChooser().getColor());

		if(getCmbColorField().getItemCount() > 0 && !rdBtnFixedColor.isSelected())
			strategy.setColorField((String) getCmbColorField().getSelectedItem());


		strategy.setFont(labelFont);
		strategy.setWithHalo(getCheckHalo().isSelected());
		if (strategy.isWithHalo()) {
			strategy.setHaloColor(getHaloColorChooser().getColor());
			strategy.setHaloWidth((float) getTxtHaloWidth().getDouble());
		}
		return strategy;
	}

	public void setModel(FLayer layer, ILabelingStrategy str) {
		this.layer = (FLyrVect) layer;
		// to allow the labeling of non-FLyrVect layers
		if (layer instanceof FLyrVect) {
			FLyrVect lv = (FLyrVect) layer;
			try {
				fieldNames = lv.getRecordset().getFieldNames();

				// detect the numeric fields
				ArrayList<String> l = new ArrayList<String>();
				ArrayList<String> lColors = new ArrayList<String>();

				for (int i = 0; i < fieldNames.length; i++) {
					switch (lv.getRecordset().getFieldType(i)) {
					case Types.DECIMAL:
					case Types.NUMERIC:
					case Types.FLOAT:
					case Types.REAL:
					case Types.DOUBLE:
						l.add(fieldNames[i]);
						break;
					case Types.INTEGER:
					case Types.SMALLINT:
					case Types.TINYINT:
					case Types.BIGINT:
						lColors.add(fieldNames[i]);
						l.add(fieldNames[i]);
						break;
					}
				}
				numericFieldNames = l.toArray(new String[l.size()]);
				integerFieldNames = lColors.toArray(new String[lColors.size()]);
			} catch (ReadDriverException e) {
				NotificationManager.addError(PluginServices.getText(this, "accessing_file_structure"), e);
			}


			refreshControls();
		}
	}


	private void refreshColorFont(){

		getCmbColorField().removeAllItems();

		boolean enabled = integerFieldNames.length>0;
//		getCmbColorField().setEnabled(enabled);
//		getRdBtnColorField().setEnabled(enabled);

		if (!enabled) {
			getRdBtnFixedColor().setSelected(true);
		}

		for (int i = 0; i < integerFieldNames.length; i++) {
			getCmbColorField().addItem(integerFieldNames[i]);
		}

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			try {

				getRdBtnFixedColor().setSelected(aux.usesFixedColor());
				getRdBtnColorField().setSelected(!aux.usesFixedColor());
				
				getCheckHalo().setSelected(aux.isWithHalo());
				if (aux.isWithHalo()) {
					getHaloColorChooser().setColor(aux.getHaloColor());
				}

				String item = aux.getColorField();
				getCmbColorField().setSelectedItem(item);
				getColorChooser().setColor(aux.getFixedColor());

			} catch (ReadDriverException e) {
				// should never happen
				NotificationManager.addWarning(PluginServices.getText(this, "could_not_restore_color_field"), e);
			}
		}
	}

	private void refreshFont(){

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			labelFont = aux.getFont();
		}
	}

	private void refreshCmbUnits() {

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			getCmbUnits().setSelectedUnitIndex(aux.getUnit());
		}
	}

	private void refreshCmbRefSystem() {

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			getCmbReferenceSystem().setSelectedIndex(aux.getReferenceSystem());
		}
	}

	private void refreshTextHeight() {
		getCmbHeightField().removeAllItems();

		boolean enabled = numericFieldNames.length>0;
//		getCmbHeightField().setEnabled(enabled);
//		getRdBtnHeightField().setEnabled(enabled);

		if (!enabled) {
			getRdBtnFixedHeight().setSelected(true);
		}

		for (int i = 0; i < numericFieldNames.length; i++) {
			getCmbHeightField().addItem(numericFieldNames[i]);
		}

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			try {
				getTxtHeightField().setText(String.valueOf(aux.getFixedSize()));
				getRdBtnFixedHeight().setSelected(aux.usesFixedSize());
				getRdBtnHeightField().setSelected(!aux.usesFixedSize());

				String item = aux.getHeightField();
				getCmbHeightField().setSelectedItem(item);

			} catch (ReadDriverException e) {
				// should never happen
				NotificationManager.addWarning(PluginServices.getText(this, "could_not_restore_text_height_field"), e);
			}
		}

	}

	private void refreshCmbRotationField() {
		getCmbRotationField().removeAllItems();
		getCmbRotationField().addItem(NO_FIELD_ITEM);
		for (int i = 0; i < numericFieldNames.length; i++) {
			getCmbRotationField().addItem(numericFieldNames[i]);
		}

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			try {
				String item = aux.getRotationField();
				getCmbRotationField().setSelectedItem(item != null? item : NO_FIELD_ITEM);
			} catch (ReadDriverException e) {
				// should never happen
				NotificationManager.addWarning(PluginServices.getText(this, "could_not_restore_rotation_field"), e);
			}
		}
	}

	private void refreshCmbTextField() {
		getCmbTextField().removeAllItems();
		for (int i = 0; i < fieldNames.length; i++) {
			getCmbTextField().addItem(fieldNames[i]);
		}

		if (layer.getLabelingStrategy() instanceof AttrInTableLabelingStrategy) {
			AttrInTableLabelingStrategy aux = (AttrInTableLabelingStrategy) layer.getLabelingStrategy();
			try {
				String item = aux.getTextField();
				getCmbTextField().setSelectedItem(item != null? item : NO_FIELD_ITEM);
			} catch (ReadDriverException e) {
				// should never happen
				NotificationManager.addWarning(PluginServices.getText(this, "could_not_restore_text_field"), e);
			}
		}
	}

	public String getLabelingStrategyName() {
		return PluginServices.getText(this, "label_attributes_defined_in_table");
	}

	public Class getLabelingStrategyClass() {
		return AttrInTableLabelingStrategy.class;
	}

	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
		getChooseFontBut().setEnabled(enabled);
		getCmbColorField().setEnabled(enabled);
		getCmbHeightField().setEnabled(enabled);
		getCmbReferenceSystem().setEnabled(enabled);
		getCmbRotationField().setEnabled(enabled);
		getCmbTextField().setEnabled(enabled);
		getCmbUnits().setEnabled(enabled);
		getColorChooser().setEnabled(enabled);
		getRdBtnColorField().setEnabled(enabled);
		getRdBtnFixedColor().setEnabled(enabled);
		getRdBtnFixedHeight().setEnabled(enabled);
		getRdBtnHeightField().setEnabled(enabled);
		getTxtHeightField().setEnabled(enabled);
		getCheckHalo().setEnabled(enabled);
		getHaloColorChooser().setEnabled(enabled);
		getTxtHaloWidth().setEnabled(enabled);

	}

}
