package org.gvsig.gpe.gui.dialogs;

import java.awt.Color;
import java.io.File;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.gpe.writer.GPEWriterHandler;
import org.gvsig.gui.beans.swing.JButton;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.FileUtils;
import com.iver.utiles.swing.JComboBox;

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
/* CVS MESSAGES:
 *
 * $Id$
 * $Log$
 *
 */
/**
 * @author Jorge Piera LLodrá (jorge.piera@iver.es)
 */
public class SelectVersionPanel extends JPanel{
	private static final String defaultDir = System.getProperty("user.home") +
		File.separatorChar;
	private static final String defaultFile = "output";
	private JPanel buttonsPanel;
	private JButton cancelButton;
	private JPanel componentsPanel;
	private JButton exportButton;
	private javax.swing.JButton fileButton;
	private JLabel fileLabel;
	private JTextField fileText;
	private JComboBox formatCombo;
	private JLabel formatLabel;
	private javax.swing.JButton schemaButton;
	private JCheckBox schemaCheck;
	private JLabel schemaLabel;
	private JTextField schemaText;
	private JComboBox writerCombo;
	private JLabel writerLabel;

	public SelectVersionPanel(boolean schemaEnabled){
		initComponents();
		initLabels(schemaEnabled);
		initCombos();
	}

	/**
	 * Initializes all the components
	 */
	private void initComponents() {
		setLayout(new java.awt.BorderLayout());
		add(getComponentsPanel(), java.awt.BorderLayout.CENTER);
		add(getButtonsPanel(), java.awt.BorderLayout.SOUTH);
	}             

	/**
	 * Initializes all the labels
	 */
	private void initLabels(boolean schemaEnabled){
		//Labels
		fileLabel.setText(PluginServices.getText(this, "gpe_select_file") + ":");
		formatLabel.setText(PluginServices.getText(this, "gpe_select_format") + ":");
		writerLabel.setText(PluginServices.getText(this, "gpe_select_writer") + ":");
		schemaLabel.setText(PluginServices.getText(this, "gpe_select_schema") + ":");
		schemaCheck.setText(PluginServices.getText(this, "gpe_create_default_schema"));
		//Combo colors
		formatCombo.setBackground(Color.WHITE);
		writerCombo.setBackground(Color.WHITE);
		//Buttons
		cancelButton.setText(PluginServices.getText(this, "cancel"));
		exportButton.setText(PluginServices.getText(this, "export"));
		//images
		fileButton.setText(null);
		schemaButton.setText(null);
		try{
			fileButton.setIcon(new ImageIcon(View.class.getClassLoader().getResource("images/open.png")));
			schemaButton.setIcon(new ImageIcon(View.class.getClassLoader().getResource("images/open.png")));
		}catch(NullPointerException exception){
			fileButton.setText("...");
			schemaButton.setText("...");
		}
		setSchemaEnabled(schemaEnabled);
	}

	/**
	 * removes all the items
	 */
	private void initCombos(){
		formatCombo.removeAllItems();
		writerCombo.removeAllItems();
	}

	/**
	 * removes all the items
	 */
	public void initializeSelection(){
		formatCombo.removeAllItems();
	}

	/**
	 * Sets the listener for the buttons
	 * @param listener
	 * The listener to set
	 */
	public void addListener(SelectVersionListener listener){
		cancelButton.setActionCommand(listener.CANCEL_BUTTON);
		cancelButton.addActionListener(listener);
		exportButton.setActionCommand(listener.EXPORT_BUTTON);
		exportButton.addActionListener(listener);
		writerCombo.setActionCommand(listener.WRITER_COMBO);
		writerCombo.addActionListener(listener);
		fileButton.setActionCommand(listener.FILE_BUTTON);
		fileButton.addActionListener(listener);
		schemaButton.setActionCommand(listener.SCHEMA_BUTTON);
		schemaButton.addActionListener(listener);	
		schemaCheck.addItemListener(listener);
	}	

	/**
	 * @return the selected writer
	 */
	public GPEWriterHandler getSelectedWriter(){
		if (writerCombo.getItemCount() > 0){
			return (GPEWriterHandler)writerCombo.getSelectedItem();
		}
		return null;
	}

	/**
	 * @return the selected writer
	 */
	public String getSelectedFile(){
		return fileText.getText();
	}
	
	/**
	 * @return the selected XML schema
	 */
	public String getSelectedXMLSchema(){
		return schemaText.getText();
	}

	/**
	 * @return the selected format
	 */
	public String getSelectedFormat(){
		if (getFormatCombo().getItemCount() > 0){
			return (String)formatCombo.getSelectedItem();
		}
		return null;
	}
	
	/**
	 * Set a XML schema
	 */
	public void setXMLSchema(String schema){
		schemaText.setText(schema);
	}
	
	/**
	 * Select a format
	 */
	public void setSelectedFormat(String format){
		formatCombo.setSelectedItem(format);
	}

	/**
	 * Sets a file
	 */
	public void setFile(String file){
		fileText.setText(file);
	}

	/**
	 * Adds a new writer
	 * @param writer
	 * The writer to add
	 */
	public void addWriter(GPEWriterHandler writer){
		writerCombo.addItem(writer);
	}

	/**
	 * Adds a new format
	 * @param format
	 * The format to add
	 */
	public void addFormat(String format){
		formatCombo.addItem(format);
	}
	
	/**
	 * @return If the XML schema has to be created
	 */
	public boolean isXMLSchemaCreated(){
		return schemaCheck.isSelected();
	}
	
	/**
	 * Enable or disable the XML schema generation
	 * @param isEnabled
	 * If is or not is enabled
	 */
	public void setSchemaEnabled(boolean isEnabled){
		getSchemaCheck().setEnabled(isEnabled);
		getSchemaCheck().setSelected(isEnabled);
		getSchemaButton().setEnabled(isEnabled);
		getSchemaText().setEnabled(isEnabled);
		if (isEnabled){
			getSchemaText().setText(getDefaultSchema());
		}
	}

	/**
	 * @return the buttonsPanel
	 */
	private JPanel getButtonsPanel() {
		if (buttonsPanel == null){
			buttonsPanel = new JPanel();
			buttonsPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.RIGHT));
			buttonsPanel.add(getCancelButton());
			buttonsPanel.add(getExportButton());
		}
		return buttonsPanel;
	}

	/**
	 * @return the cancelButton
	 */
	private JButton getCancelButton() {
		if (cancelButton == null){
			cancelButton = new JButton();
		}
		return cancelButton;
	}

	/**
	 * @return the componentsPanel
	 */
	private JPanel getComponentsPanel() {
		if (componentsPanel == null){
			componentsPanel = new JPanel();
			componentsPanel.setLayout(new java.awt.GridBagLayout());
			java.awt.GridBagConstraints gridBagConstraints;
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 10;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
			
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 7;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
			componentsPanel.add(getFormatLabel(), gridBagConstraints);
		
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 8;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
			componentsPanel.add(getFormatCombo(), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 9;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.insets = new java.awt.Insets(5, 2, 2, 2);
			
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 5;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
			componentsPanel.add(getWriterLabel(), gridBagConstraints);
			
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 6;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
			componentsPanel.add(getWriterCombo(), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
			componentsPanel.add(getFileLabel(), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
			componentsPanel.add(getFileText(), gridBagConstraints);
	
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 1;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
			componentsPanel.add(getFileButton(), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 2;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
			componentsPanel.add(getSchemaLabel(), gridBagConstraints);

			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 4;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
			componentsPanel.add(getSchemaText(), gridBagConstraints);
			
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 1;
			gridBagConstraints.gridy = 4;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 5, 2);
			componentsPanel.add(getSchemaButton(), gridBagConstraints);		
			
			gridBagConstraints = new java.awt.GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.gridy = 3;
			gridBagConstraints.gridwidth = 2;
			gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints.weightx = 1.0;
			gridBagConstraints.insets = new java.awt.Insets(2, 2, 2, 2);
			componentsPanel.add(getSchemaCheck(), gridBagConstraints);
		}
		return componentsPanel;
	}

	/**
	 * @return the exportButton
	 */
	private JButton getExportButton() {
		if (exportButton == null){
			exportButton = new JButton();
		}
		return exportButton;
	}

	/**
	 * @return the fileButton
	 */
	private javax.swing.JButton getFileButton() {
		if (fileButton == null){
			fileButton = new javax.swing.JButton();
			fileButton.setMaximumSize(new java.awt.Dimension(25, 25));
			fileButton.setMinimumSize(new java.awt.Dimension(25, 25));
			fileButton.setPreferredSize(new java.awt.Dimension(25, 25));
		}
		return fileButton;
	}

	/**
	 * @return the fileLabel
	 */
	private JLabel getFileLabel() {
		if (fileLabel == null){
			fileLabel = new JLabel();
		}
		return fileLabel;
	}

	/**
	 * @return the fileText
	 */
	private JTextField getFileText() {
		if (fileText == null){
			fileText = new JTextField();
			fileText.setText(getDefaultFileName());
		}
		return fileText;
	}

	/**
	 * @return the formatCombo
	 */
	private JComboBox getFormatCombo() {
		if (formatCombo == null){
			formatCombo = new JComboBox();
		}
		return formatCombo;
	}

	/**
	 * @return the formatLabel
	 */
	private JLabel getFormatLabel() {
		if (formatLabel == null){
			formatLabel = new JLabel();
		}
		return formatLabel;
	}

	/**
	 * @return the schemaButton
	 */
	private javax.swing.JButton getSchemaButton() {
		if (schemaButton == null){
			schemaButton = new javax.swing.JButton();
			schemaButton.setPreferredSize(new java.awt.Dimension(25, 25));
		}
		return schemaButton;
	}

	/**
	 * @return the schemaCheck
	 */
	private JCheckBox getSchemaCheck() {
		if (schemaCheck == null){
			schemaCheck = new JCheckBox();
			schemaCheck.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
			schemaCheck.setMargin(new java.awt.Insets(0, 0, 0, 0));
		}
		return schemaCheck;
	}

	/**
	 * @return the schemaLabel
	 */
	private JLabel getSchemaLabel() {
		if (schemaLabel == null){
			schemaLabel = new JLabel();
		}
		return schemaLabel;
	}

	/**
	 * @return the schemaText
	 */
	private JTextField getSchemaText() {
		if (schemaText == null){
			schemaText = new JTextField();
		}
		return schemaText;
	}
		
	/**
	 * @return the writerCombo
	 */
	private JComboBox getWriterCombo() {
		if (writerCombo == null){
			writerCombo = new JComboBox();
		}
		return writerCombo;
	}

	/**
	 * @return the writerLabel
	 */
	private JLabel getWriterLabel() {
		if (writerLabel == null){
			writerLabel = new JLabel();
		}
		return writerLabel;
	}

	/**
	 * @return the defaultFile
	 */
	public String getDefaultFileName() {
		if (getSelectedFormat() != null){
			return defaultDir + defaultFile + "." + getSelectedWriter().getFileExtension().toLowerCase();
		}else{
			return defaultDir + defaultFile;
		}
	}
	
	/**
	 * @return the defaultSchema
	 */
	private String getDefaultSchema() {
		String sFile = getSelectedFile();
		if (sFile.length() > 0){
			File file = new File(sFile);
			String extension = FileUtils.getFileExtension(file);
			sFile = sFile.substring(0, sFile.length() - extension.length());
			sFile = sFile + ".xsd";
			return sFile;
		}else{
			return defaultDir + defaultFile + ".xsd";
		}
	}
}
