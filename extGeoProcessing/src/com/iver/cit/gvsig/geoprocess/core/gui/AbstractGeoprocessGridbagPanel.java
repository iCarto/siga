/*
 * Created on 30-jun-2006
 *
 * gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
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
 * Revision 1.3  2007-03-06 16:47:58  caballero
 * Exceptions
 *
 * Revision 1.2  2006/09/15 10:42:54  caballero
 * extensibilidad de documentos
 *
 * Revision 1.1  2006/08/11 16:11:38  azabala
 * first version in cvs
 *
 * Revision 1.2  2006/07/04 16:43:18  azabala
 * *** empty log message ***
 *
 * Revision 1.1  2006/07/03 20:28:20  azabala
 * *** empty log message ***
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.core.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.FileNotFoundException;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.gvsig.gui.beans.swing.GridBagLayoutPanel;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.layerOperations.SingleLayer;
import com.iver.cit.gvsig.geoprocess.core.fmap.FMapUtil;
import com.iver.cit.gvsig.project.documents.view.legend.CreateSpatialIndexMonitorableTask;
import com.iver.utiles.GenericFileFilter;
import com.iver.utiles.swing.threads.IMonitorableTask;

/**
 * Abstract base panel to easily build GeoprocessPanels
 * (View GUI component with which user could launch a given
 * geoprocess).
 *
 * All panels that extends this class will have a header
 * (textual label that describes the geoprocess).
 *
 * In the next row, they will have a layer combo box, where users could
 * select the input layer of the geoprocess (all geoprocesses at least
 * will work with an input layer), and a check box to specify working only
 * with input layer selection.
 *
 * Nex, each descendant panels must implement addSpecificDesign()
 * abstract method, to add specific components with which users could
 * introduce the information needed by the geoprocess.
 *
 * Finally, the last row has a panel to allow users to select where
 * to save the results of the geoprocess.
 *
 *
 *
 * @author azabala
 *
 */
public abstract class AbstractGeoprocessGridbagPanel 
	extends GridBagLayoutPanel 
	implements IGeoprocessPanel, IGeoprocessUserEntries{

	protected final int DEFAULT_FILL = GridBagConstraints.BOTH;

	/**
	 * textual description of the associated geoprocess
	 */
	protected String titleText;

	/**
	 * View's layers showed in TOC
	 */
	protected FLayers layers;

	/**
	 * Combo box to show layer names to user
	 */
	protected JComboBox layersComboBox;

	/**
	 * Check box to specify that geoprocess will only process
	 * input layer selection
	 */
	protected JCheckBox selectedOnlyCheckBox;

	/**
	 * Shows the number of selected features of input layer
	 */
	protected JLabel numSelectedLabel;

	/**
	 * Text field to show user the full path (or a representative string)
	 * of the result layer selection
	 */

	protected JTextField resultTf;

	/**
	 * Default constructor
	 *
	 */

	public AbstractGeoprocessGridbagPanel(FLayers layers, String titleText) {
		super();
		this.layers = layers;
		this.titleText = titleText;
		initialize();
	}

	protected void initialize(){
		Insets insets = new Insets(5, 5, 5, 5);
		addComponent(new JLabel(titleText), insets);
		JLabel firstLayerLab = new JLabel(PluginServices.
				getText(this, "Cobertura_de_entrada")+":");
		JComboBox layersComboBox = getLayersComboBox();
		addComponent(firstLayerLab, layersComboBox, GridBagConstraints.BOTH, insets);
		addComponent(getSelectedOnlyCheckBox(), GridBagConstraints.BOTH, insets);

		String numSelectedText = PluginServices.
			getText(this, "Numero_de_elementos_seleccionados") + ":";
		numSelectedLabel = new JLabel("00");
		addComponent(numSelectedText, numSelectedLabel, insets);

		addSpecificDesign();

		JPanel aux = new JPanel(new BorderLayout());
		String resultLayerText = PluginServices.
				getText(this, "Cobertura_de_salida") + ":";
		resultTf = getFileNameResultTextField();
		JButton openButton = getOpenResultButton();
        aux.add(resultTf, BorderLayout.WEST);
        aux.add(new JLabel(" "), BorderLayout.CENTER);
        aux.add(openButton, BorderLayout.EAST);
        addComponent(resultLayerText, aux, GridBagConstraints.HORIZONTAL, insets );
		setBounds(0, 0, 520, 410);
	}


	protected void initSelectedItemsJCheckBox() {
		String selectedLayer = (String) layersComboBox.getSelectedItem();
		FLyrVect inputLayer = (FLyrVect) layers.getLayer(selectedLayer);
		FBitSet fBitSet = null;
		try {
			fBitSet = inputLayer.getRecordset().getSelection();
		} catch (ReadDriverException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (fBitSet.cardinality() == 0) {
			selectedOnlyCheckBox.setEnabled(false);
			selectedOnlyCheckBox.setSelected(false);
		} else {
			selectedOnlyCheckBox.setEnabled(true);
			selectedOnlyCheckBox.setSelected(true);
		}
		selectedOnlyCheckBox.setSelected(false);

		updateNumSelectedFeaturesLabel();
	}


    protected void updateNumSelectedFeaturesLabel() {
        if (selectedOnlyCheckBox.isSelected()) {
            FLyrVect inputSelectable = (FLyrVect)(layers.getLayer((String)layersComboBox.getSelectedItem()));
            FBitSet fBitSet = null;
			try {
				fBitSet = inputSelectable.getRecordset().getSelection();
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			numSelectedLabel.setText(new Integer(fBitSet.cardinality()).toString());
        } else {
        	ReadableVectorial va = ((SingleLayer)(layers.
        			getLayer((String)layersComboBox.
        					getSelectedItem()))).
        							getSource();
            	try {
					numSelectedLabel.setText(new Integer(va.getShapeCount()).toString());
				} catch (ReadDriverException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        }
    }


	private JButton getOpenResultButton() {
		JButton	openResultButton = new JButton();
		openResultButton.setText(PluginServices.getText(this, "Abrir"));
		openResultButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openResultFile();
				}
			}
		);
		return openResultButton;
	}


	/**
	 * This method must be overwrited by all descendant classes to
	 * add specific desing of each Geoprocess Panel.
	 */
	protected abstract void addSpecificDesign();

	/**
	 * Returns the layer selected in layer combo box
	 *
	 * @return
	 */
	public FLyrVect getInputLayer() {
		FLyrVect solution = null;
		String selectedLayer = (String) layersComboBox.getSelectedItem();
		solution = (FLyrVect) layers.getLayer(selectedLayer);
		return solution;
	}

	/**
	 * Sets view's layers from the TOC
	 *
	 * @param layers
	 */
	public void setFLayers(FLayers layers) {
		this.layers = layers;
	}

	/**
	 * Returns layers.
	 *
	 * @return
	 */
	public FLayers getFLayers() {
		return layers;
	}

	
	

	/**
	 * Obtains layer names from FLayers and returns like an array of strings.
	 *
	 * @return
	 *
	 * TODO Llevar a una utility class
	 */
	
	/**
	 * Shows to the user a dialog error, whith the title and message body
	 * specified as parameters.
	 *
	 * @param message
	 *            body of the error message
	 * @param title
	 *            title of the error message
	 */
	public void error(String message, String title) {
		JOptionPane.showMessageDialog(this, message, title,
				JOptionPane.ERROR_MESSAGE);
	}

	/**
	 * Asks to the user for the creation of a spatial index for the specified
	 * layer. It will be of help for certain geoprocesses.
	 *
	 * FLyrVect default spatial index is MapServer quadtree, that hasnt the
	 * ability to do nearest neighbour searches.
	 *
	 * For those geoprocesses that needs it (NN searches) overwrite this method
	 * and use JSI RTree, or SIL RTree.
	 *
	 * It returns an IMonitorableTask, a task to build the spatial index in
	 * background.
	 *
	 * @param layer
	 * @return an ITask
	 */
	public IMonitorableTask askForSpatialIndexCreation(FLyrVect layer) {
		String title = PluginServices.getText(this, "Crear_Indice");
		String confirmDialogText = PluginServices.getText(this,
				"Crear_Indice_Pregunta_1")
				+ " "
				+ layer.getName()
				+ " "
				+ PluginServices.getText(this, "Crear_Indice_Pregunta_2");
		int option = JOptionPane.showConfirmDialog(this, confirmDialogText,
				title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);
		if (option == JOptionPane.YES_OPTION) {
			// Usually we want task like spatial index creation dont block
			// GUI responses. Now, we dont want to start geoprocess execution
			// until spatial index creation would finish, to have advantage
			// of the spatial index
			try {
				CreateSpatialIndexMonitorableTask task = new CreateSpatialIndexMonitorableTask(
						layer);
				return task;
			}catch (ReadDriverException e) {
				e.printStackTrace();
				return null;
			}
		}
		return null;

	}

	/**
	 * Confirm overwrite the output file if it allready exist.
	 *
	 *
	 * @param outputFile
	 * @return answer
	 */
	public boolean askForOverwriteOutputFile(File outputFile) {
		String title = PluginServices.getText(this, "Sobreescribir_fichero");
		String confirmDialogText = PluginServices.getText(this,
				"Sobreescribir_fichero_Pregunta_1")
				+ "\n'"
				+ outputFile.getAbsolutePath()
				+ "'\n"
				+ PluginServices.getText(this,
						"Sobreescribir_fichero_Pregunta_2");
		int option = JOptionPane.showConfirmDialog(this, confirmDialogText,
				title, JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
				null);
		if (option == JOptionPane.YES_OPTION) {
			return true;
		}
		return false;
	}


	/**
	 * Opens a dialog to select where (file, database, etc)
	 * to save the result layer.
	 *
	 */
	public void openResultFile() {
		JFileChooser jfc = new JFileChooser();
		jfc
				.addChoosableFileFilter(new GenericFileFilter("shp",
						PluginServices.getText(this, "Ficheros_SHP")));
		if (jfc.showSaveDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			if (!(file.getPath().endsWith(".shp") || file.getPath().endsWith(
					".SHP"))) {
				file = new File(file.getPath() + ".shp");
			}
		}// if
		if (jfc.getSelectedFile() != null) {
			getFileNameResultTextField().setText(
					jfc.getSelectedFile().getAbsolutePath());
		}

	}


	protected JTextField getFileNameResultTextField() {
		if(resultTf == null)
			resultTf = new JTextField(25);
		return resultTf;
	}

	public File getOutputFile() throws FileNotFoundException{
		String fileName = getFileNameResultTextField().getText();
		if(fileName.length() == 0){
			throw new FileNotFoundException("No se ha seleccionado ningun fichero de salida");
		}
		if(! fileName.endsWith(".shp")){
			if(! fileName.endsWith("."))
				fileName += ".";
			fileName += "shp";
		}
		return new File(fileName);
	}

	protected JComboBox getLayersComboBox() {
		if (layersComboBox == null) {
			layersComboBox = new JComboBox();
			DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(
					FMapUtil.getLayerNames(layers));
			layersComboBox.setModel(defaultModel);
			layersComboBox.setBounds(142, 63, 260, 21);
			layersComboBox.addItemListener(new java.awt.event.ItemListener() {
				public void itemStateChanged(ItemEvent e) {
					if (e.getStateChange() == ItemEvent.SELECTED) {
						initSelectedItemsJCheckBox();
						updateNumSelectedFeaturesLabel();
						processLayerComboBoxStateChange(e);
					}
				}// itemStateChange
			});
		}
		return layersComboBox;
	}

	protected JCheckBox getSelectedOnlyCheckBox() {
		if (selectedOnlyCheckBox == null) {
			selectedOnlyCheckBox = new JCheckBox();
			selectedOnlyCheckBox.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent arg0) {
					updateNumSelectedFeaturesLabel();
				}});
			selectedOnlyCheckBox.setText(PluginServices.getText(this, "Usar_solamente_los_elementos_seleccionados"));
		}
		return selectedOnlyCheckBox;
	}

	public boolean isFirstOnlySelected(){
		return getSelectedOnlyCheckBox().isSelected();
	}

	protected abstract void processLayerComboBoxStateChange(ItemEvent e);
}
