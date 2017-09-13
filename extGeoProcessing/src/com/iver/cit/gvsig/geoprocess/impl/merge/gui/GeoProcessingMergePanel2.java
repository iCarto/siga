/*
 * Created on 08-ago-2006
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
 * Revision 1.4  2007-03-06 16:47:58  caballero
 * Exceptions
 *
 * Revision 1.3  2006/09/25 18:28:50  azabala
 * fixed bug: now, when some data file in directory to merge is wrong, it ignores it and reports user at the end of the data file searching task
 *
 * Revision 1.2  2006/09/15 10:42:54  caballero
 * extensibilidad de documentos
 *
 * Revision 1.1  2006/08/11 16:30:38  azabala
 * *** empty log message ***
 *
 *
 */
package com.iver.cit.gvsig.geoprocess.impl.merge.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Arrays;

import javax.swing.AbstractListModel;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.border.EtchedBorder;
import javax.swing.event.ListSelectionListener;

import org.cresques.cts.IProjection;
import org.gvsig.gui.beans.swing.GridBagLayoutPanel;
import org.gvsig.gui.beans.swing.JButton;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.drivers.VectorialFileDriver;
import com.iver.cit.gvsig.fmap.drivers.dgn.DgnMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.dxf.DXFMemoryDriver;
import com.iver.cit.gvsig.fmap.drivers.shp.IndexedShpDriver;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.geoprocess.core.gui.IGeoprocessPanel;
import com.iver.cit.gvsig.geoprocess.core.gui.IGeoprocessUserEntries;
import com.iver.cit.gvsig.project.documents.view.legend.CreateSpatialIndexMonitorableTask;
import com.iver.utiles.GenericFileFilter;
import com.iver.utiles.swing.threads.IMonitorableTask;

public class GeoProcessingMergePanel2 extends GridBagLayoutPanel implements
		MergePanelIF , IGeoprocessPanel, IGeoprocessUserEntries{

	// input layers of the active view's TOC
	private FLayers layers;

	// layers selected to merge
	private FLyrVect[] layersSelected;

	// layers not in the TOC but in the directory selected by user
	private FLyrVect[] aditionalLayers;

	private String[] inputLayersToAdd;

	// Additional selected layers in second list
	private FLyrVect[] aditionalLayersSelection;

	/*
	 * GUI CONTROLS
	 */
	private JList inputLayersList;

	private JComboBox fieldsJComboBox;

	private JLabel directoryLyrsLabel;

	private JList directoryLayerList;

	protected JTextField resultTf;


	/**
	 * Constructor
	 * @param layers
	 */
	public GeoProcessingMergePanel2(FLayers layers) {
		super();
		setFLayers(layers);
		initialize();
	}

	private void initialize() {
		Insets insets = new Insets(5, 5, 5, 5);
		String titleText = PluginServices.getText(this,
				"Juntar._Introduccion_de_datos")
				+ ":";
		addComponent(new JLabel(titleText), insets);

		String inputLayerText = PluginServices.getText(this,
				"Cobertura_de_entrada")
				+ ":";
		addComponent(new JLabel(inputLayerText), insets);

		JScrollPane inputLayersScrollPane = new JScrollPane();
		inputLayersList = new JList(getLayerNames());
		LayerSelectorListener layerSelectorListener = new LayerSelectorListener(
				layers);
		inputLayersList.addListSelectionListener(layerSelectorListener);
		inputLayersScrollPane.setViewportView(inputLayersList);
		inputLayersScrollPane.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));
//		If we dont do this, GridBagLayout wont resize well the scrollpane
		inputLayersScrollPane.setPreferredSize(new Dimension(520, 110));
		inputLayersScrollPane.setMinimumSize(new Dimension(520, 90));
		int numRows = 2;
		addComponent(inputLayersScrollPane,
								insets,
								numRows);

		String directoryLayersText = PluginServices.getText(this,
				"Capas_del_directorio")
				+ ":";
		directoryLyrsLabel = new JLabel(directoryLayersText);
		addComponent(directoryLyrsLabel, insets);

		JButton directoryButton = new JButton(PluginServices.getText(this,
				"Seleccionar_Directorio"));
		directoryButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				openDirectoryLayersDialog();
			}
		});
		addComponent(directoryButton, insets);

		JScrollPane directoryLayerScrollPane = new JScrollPane();
		directoryLayerList = new JList(getDirectoryLayerNames());
		AditionalLayerSelectorListener listener = new AditionalLayerSelectorListener(
				aditionalLayers);
		directoryLayerList.addListSelectionListener(listener);
		directoryLayerScrollPane.setViewportView(directoryLayerList);
		directoryLayerScrollPane.setBorder(BorderFactory
				.createEtchedBorder(EtchedBorder.RAISED));

		//If we dont do this, GridBagLayout wont resize well the scrollpane
		directoryLayerScrollPane.setPreferredSize(new Dimension(520, 110));
		directoryLayerScrollPane.setMinimumSize(new Dimension(520, 90));


		addComponent(directoryLayerScrollPane, insets,
				 numRows );


		JLabel resultSchemaLabel = new JLabel(PluginServices.
				getText(this, "Usar_los_campos_de_la_capa") + ":");

		fieldsJComboBox = new JComboBox();
		addComponent(resultSchemaLabel,
				fieldsJComboBox,
				GridBagConstraints.HORIZONTAL,
				insets);


		//FIXME Create an open result layer control
		JPanel aux = new JPanel(new BorderLayout());
		String resultLayerText = PluginServices.
				getText(this, "Cobertura_de_salida") + ":";
		resultTf  = new JTextField(30);
		JButton	openResultButton = new JButton();
		openResultButton.setText(PluginServices.getText(this, "Abrir"));
		openResultButton.addActionListener(new java.awt.event.ActionListener() {
				public void actionPerformed(java.awt.event.ActionEvent e) {
					openResultFile();
				}
			}
		);
        aux.add(resultTf, BorderLayout.WEST);
        aux.add(new JLabel(" "), BorderLayout.CENTER);
        aux.add(openResultButton, BorderLayout.EAST);
        addComponent(resultLayerText, aux, GridBagConstraints.HORIZONTAL, insets );
	}

	//FIXME Move this to an open result layer control

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
			resultTf.setText(
					jfc.getSelectedFile().getAbsolutePath());
		}

	}


	public void setFLayers(FLayers layers) {
		this.layers = layers;
	}

	public FLyrVect[] getSelectedLayers() {
		FLyrVect[] solution = null;
		String[] selectedNames = this.getLayersSelectedNames();
		if (selectedNames == null) {
			return null;
		}
		ArrayList loadedSelectedLayers = new ArrayList();
		for (int i = 0; i < selectedNames.length; i++) {
			String name = selectedNames[i];
			FLyrVect layer = (FLyrVect) layers.getLayer(name);
			if (layer == null) {
				// it is a directory layer
				for (int j = 0; j < aditionalLayers.length; j++) {
					FLyrVect aditional = aditionalLayers[j];
					if (name.equals(aditional.getName())) {
						layer = aditional;
					}//
				}// fir
			}// if
			if (layer != null)
				loadedSelectedLayers.add(layer);
		}// for
		solution = new FLyrVect[loadedSelectedLayers.size()];
		loadedSelectedLayers.toArray(solution);
		return solution;
	}

	public FLyrVect[] loadLayersInDirectory(File file, final String[] extensions) {
		if (file.isDirectory()) {
			inputLayersToAdd = file.list(new FilenameFilter() {
				public boolean accept(File dir, String name) {
					for (int i = 0; i < extensions.length; i++) {
						if (name.toLowerCase().endsWith(extensions[i]))
							return true;
					}// for
					return false;
				}// acept
			});
		} else {
			inputLayersToAdd = new String[] { file.getAbsolutePath() };
		}

		FLyrVect[] solution = null;
		ArrayList newLyrList = new ArrayList();
		if (inputLayersToAdd == null)
			return solution;

		// sin florituras. cogemos la primera que haya
		IProjection projection = layers.getLayer(0).getProjection();
		DgnMemoryDriver dgnDriver = null;
		IndexedShpDriver shpDriver = null;
		DXFMemoryDriver dxfDriver = null;
		VectorialFileDriver driver = null;
		ArrayList errors = new ArrayList();
		for (int i = 0; i < inputLayersToAdd.length; i++) {
			String fileName = inputLayersToAdd[i];
			String fullPath = file.getAbsolutePath() + "/" + fileName;
			File afile = new File(fullPath);
			try {
				if (fileName.endsWith("dxf") || fileName.endsWith("DXF")) {
					dxfDriver = new DXFMemoryDriver();
					dxfDriver.open(afile);
					dxfDriver.initialize();
					driver = dxfDriver;
				} else if (fileName.endsWith("shp") || fileName.endsWith("SHP")) {
					shpDriver = new IndexedShpDriver();
					shpDriver.open(afile);
					shpDriver.initialize();
					driver = shpDriver;
				} else if (fileName.endsWith("dgn") || fileName.endsWith("DGN")) {
					dgnDriver = new DgnMemoryDriver();
					dgnDriver.open(afile);
					dgnDriver.initialize();
					driver = dxfDriver;
				}
				newLyrList.add((FLyrVect) LayerFactory.
						createLayer(fileName,
									driver,
									afile,
									projection));
			} catch(Exception e){
				String errorText =  fullPath + "\n";
				errors.add(errorText);
				//we catch any possible (and not declared) exception, because we dont know
				//the correctness of file layers (for example, shp without dbf, etc)
				e.printStackTrace();
			}
		}//for
		if(errors.size() > 0){
			String errorMsg = PluginServices.getText(this, "Se_han_encontrado_capas_erroneas_en");
			errorMsg += " "+file.getAbsolutePath() + "\n";
			for(int i = 0; i < errors.size(); i++){
				errorMsg +=  (String) errors.get(i) + "\n";
			}
			JOptionPane.showMessageDialog(this, errorMsg);
		}
		solution = new FLyrVect[newLyrList.size()];
		newLyrList.toArray(solution);
		return solution;
	}

	public void addLayersToMergeList(FLyrVect[] layers) {
		// TODO Auto-generated method stub

	}

	public FLyrVect getSelectedSchema() {
		FLyrVect solution = null;
		String layerName = (String) fieldsJComboBox.getSelectedItem();
		if(layerName == null)
			return null;
		solution = (FLyrVect) layers.getLayer(layerName);
		if (solution == null){
			//see if it is an aditionallayer
			for(int i = 0; i < aditionalLayers.length; i++){
				FLyrVect layer = aditionalLayers[i];
				if(layer.getName() == layerName)
					solution = layer;
			}
		}
		return solution;
	}

	public void layersSelected() {
		 LayerSelectorListener layerSelectorListener = new LayerSelectorListener(layers);
		 inputLayersList.addListSelectionListener(layerSelectorListener);
         LayersListModel model = new LayersListModel(layers);
         inputLayersList.setModel(model);
         AditionalLayerSelectorListener aditionalListener = new
         	AditionalLayerSelectorListener(aditionalLayers);
         directoryLayerList.addListSelectionListener(aditionalListener);
         AditionalLayersListModel aditionalListModel =
        	 new AditionalLayersListModel(aditionalLayers);
         directoryLayerList.setModel(aditionalListModel);
	}

	public void openResultFileDialog() {
		openResultFile();
	}

	/**
	 * Runs when user pushs Additional Layer button
	 */
	public void openDirectoryLayersDialog() {
		JFileChooser jfc = new JFileChooser();
		jfc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		final String[] extensions = { "shp", "dxf", "dwg" };
		jfc.addChoosableFileFilter(new GenericFileFilter(extensions,
				PluginServices.getText(this, "Ficheros_de_cartografia")));
		if (jfc.showOpenDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
			File file = jfc.getSelectedFile();
			FLyrVect[] newlayers = loadLayersInDirectory(file, extensions);
			String directoryLayersText = PluginServices.getText(this,
					"Capas_del_directorio")
					+ ":";
			directoryLayersText += file.getAbsolutePath();
			directoryLyrsLabel.setText(directoryLayersText);

			this.aditionalLayers = newlayers;
		}
		layersSelected();
	}


	// mover a una utility class
	protected String[] getLayerNames() {
		String[] solution = null;
		int numLayers = layers.getLayersCount();
		if (layers != null && numLayers > 0) {
			ArrayList list = new ArrayList();
			for (int i = 0; i < numLayers; i++) {
				FLayer layer = layers.getLayer(i);
				if (layer instanceof FLyrVect)
					list.add(layer.getName());
				if (layer instanceof FLayers) {
					FLayers layers = (FLayers) layer;
					FLyrVect[] vectorials = getVectorialLayers(layers);
					for (int j = 0; j < vectorials.length; j++) {
						list.add(vectorials[j].getName());
					}
				}
			}// for
			solution = new String[list.size()];
			list.toArray(solution);
		}
		return solution;
	}

	// FIXME mover a una utility class
	protected FLyrVect[] getVectorialLayers(FLayers layers) {
		FLyrVect[] solution = null;
		ArrayList list = new ArrayList();
		int numLayers = layers.getLayersCount();
		for (int i = 0; i < numLayers; i++) {
			FLayer layer = layers.getLayer(i);
			if (layer instanceof FLyrVect)
				list.add(layer);
			else if (layer instanceof FLayers)
				list.addAll(Arrays.asList(getVectorialLayers((FLayers) layer)));
		}
		solution = new FLyrVect[list.size()];
		list.toArray(solution);
		return solution;
	}

	protected String[] getDirectoryLayerNames() {
		String[] solution = null;
		ArrayList layerNames = new ArrayList();
		if (aditionalLayers != null) {
			for (int i = 0; i < aditionalLayers.length; i++) {
				layerNames.add(aditionalLayers[i].getName());
			}// for
		}
		solution = new String[layerNames.size()];
		layerNames.toArray(solution);
		return solution;
	}

	private String[] getLayersSelectedNames() {
		if (layersSelected == null && aditionalLayersSelection == null)
			return null;
		ArrayList solution = new ArrayList();
		if (layersSelected != null) {
			for (int i = 0; i < layersSelected.length; i++) {
				solution.add(layersSelected[i].getName());
			}
		}
		if (aditionalLayersSelection != null) {
			for (int i = 0; i < aditionalLayersSelection.length; i++) {
				solution.add(aditionalLayersSelection[i].getName());
			}
		}
		String[] layersSelectedNames = new String[solution.size()];
		solution.toArray(layersSelectedNames);
		return layersSelectedNames;
	}

	/**
	 * Listens selection of layers in input layers list, and refresh selected
	 * layers list
	 *
	 * @author azabala
	 *
	 */
	private class LayerSelectorListener implements ListSelectionListener {
		private FLayers layers;

		public LayerSelectorListener(FLayers layers) {
			this.layers = layers;
		}

		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
			Object[] layersSelectedNames = inputLayersList.getSelectedValues();
			if (layersSelectedNames == null)
				return;
			layersSelected = new FLyrVect[layersSelectedNames.length];
			for (int i = 0; i < layersSelectedNames.length; i++) {
				String layerString = (String) layersSelectedNames[i];
				FLyrVect lyr = (FLyrVect) layers.getLayer(layerString);
				if (lyr != null)
					layersSelected[i] = lyr;
			}
			// refresh combo box of schema preserve of layers
			DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(
					getLayersSelectedNames());
			fieldsJComboBox.setModel(defaultModel);
		}// valueChanged
	}

	private class AditionalLayerSelectorListener implements
			ListSelectionListener {
		private FLyrVect[] aditionalLayers;

		public AditionalLayerSelectorListener(FLyrVect[] aditionalLayers) {
			this.aditionalLayers = aditionalLayers;
		}

		public void valueChanged(javax.swing.event.ListSelectionEvent e) {
			Object[] layersSelectedNames = directoryLayerList
					.getSelectedValues();
			if (layersSelectedNames == null)
				return;
			if (this.aditionalLayers == null)
				return;
			aditionalLayersSelection = new FLyrVect[layersSelectedNames.length];
			for (int i = 0; i < layersSelectedNames.length; i++) {
				String selection = (String) layersSelectedNames[i];
				for (int j = 0; j < this.aditionalLayers.length; j++) {
					if (this.aditionalLayers[j].getName().equals(selection)) {
						aditionalLayersSelection[i] = this.aditionalLayers[j];
						break;
					}// if
				}// for
			}// for
			// Any time user add a layer to aditional layers list,
			// we create a new comboboxmodel, to add this layers to the
			// schema presevation combo box
			DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(
					getLayersSelectedNames());
			fieldsJComboBox.setModel(defaultModel);
		}// valueChanged
	}

	/**
     * ListModel for input layer list (which has layers of the TOC)
     * @author azabala
     *
     */
    class LayersListModel extends AbstractListModel{
		private static final long serialVersionUID = 1L;
		private FLayers layers;
		LayersListModel(FLayers layers){
			this.layers = layers;
		}

		public int getSize() {
			return layers.getLayersCount();
		}

		public Object getElementAt(int arg0) {
			if(arg0 < layers.getLayersCount())
				return layers.getLayer(arg0).getName();
			else{
				if(aditionalLayers == null)
					return null;
				else{
					FLyrVect layer = aditionalLayers[arg0 - layers.getLayersCount()];
					if(layer == null)
						return "";
					else
						return layer.getName();
				}//else
			}//else
		}//getElementAt
    }

    class AditionalLayersListModel extends AbstractListModel{
		private static final long serialVersionUID = 6123487908892908100L;
		private FLyrVect[] layers;

		AditionalLayersListModel(FLyrVect[] layers){
			this.layers = layers;
		}
		public int getSize() {
			if(layers != null){
				return layers.length;
			}
			return 0;
		}

		public Object getElementAt(int position) {
			if(layers != null){
				return layers[position].getName();
			}
			return null;
		}
    }

    //FIXME Move to an abstract base class
    public File getOutputFile() throws FileNotFoundException{
		String fileName = resultTf.getText();
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

    //FIXME Move to an abstract base class
    /**
	 * Returns layers.
	 *
	 * @return
	 */
	public FLayers getFLayers() {
		return layers;
	}
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
			} catch (ReadDriverException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return null;
			}
		} else
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

	public FLyrVect getInputLayer() {
		return null;
	}

}
