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
package com.iver.cit.gvsig.project.documents.view;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.LayersIterator;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.gui.preferencespage.ViewPage;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.ProjectDocumentFactory;
import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.cit.gvsig.project.documents.table.ProjectTableFactory;
import com.iver.cit.gvsig.project.documents.view.gui.ViewProperties;
import com.iver.utiles.XMLEntity;


/**
 * Clase que representa una vista del proyecto
 *
 * @author Fernando González Cortés
 */
public class ProjectView extends ProjectViewBase {

	public XMLEntity getXMLEntity() throws SaveException {
		XMLEntity xml = super.getXMLEntity();
		try{
		int numViews=((Integer)ProjectDocument.NUMS.get(ProjectViewFactory.registerName)).intValue();

		xml.putProperty("numViews", numViews);
		
		xml.addChild(mapContext.getXMLEntity());

		if (mapOverViewContext != null) {
			if (mapOverViewContext.getViewPort() != null) {
				xml.putProperty("mapOverView", true);
				xml.addChild(mapOverViewContext.getXMLEntity());
			} else {
				xml.putProperty("mapOverView", false);
			}
		} else {
			xml.putProperty("mapOverView", false);
		}
		}catch (Exception e) {
			throw new SaveException(e,this.getClass().getName());
		}
		return xml;
	}


	/**
	 * @see com.iver.cit.gvsig.project.documents.ProjectDocument#setXMLEntity(com.iver.utiles.XMLEntity)
	 */
	public void setXMLEntity(XMLEntity xml)
		throws XMLException, ReadDriverException, OpenException {
		try{
			super.setXMLEntity(xml);
			int currentChild=0;
			int numViews = xml.getIntProperty("numViews");
			ProjectDocument.NUMS.put(ProjectViewFactory.registerName,new Integer(numViews));

			setMapContext(MapContext.createFromXML(xml.getChild(currentChild)));
			currentChild++;
			if (xml.getBooleanProperty("mapOverView")) {
				setMapOverViewContext(MapContext.createFromXML(xml.getChild(currentChild)));
				currentChild++;
			}
			
			// legacy hyperlink stuff
			if (xml.contains("m_selectedField")) {
				String selectedField, extension=null;
				int type=-1;
				selectedField = xml.getStringProperty("m_selectedField");
				if (xml.contains("m_typeLink")) {
					type = xml.getIntProperty("m_typeLink");					
				}
				if (xml.contains("m_extLink")) {
					extension = xml.getStringProperty("m_extLink");
				}
				applyHyperlinkToLayers(selectedField, type, extension);
			}
			
		showErrors();
		}catch (Exception e) {
			throw new OpenException(e,this.getClass().getName());
		}
	}

	/**
	 * <p>Legacy code to keep compatibility with old 1.1.x hyperlink.</p>
	 * 
	 * @param selectedField
	 * @param type
	 * @param extension
	 */
	private void applyHyperlinkToLayers(String selectedField, int type, String extension) {
		final String LAYERPROPERTYNAME = "org.gvsig.hyperlink.config";
		
		LayersIterator iterator = new LayersIterator(getMapContext().getLayers());
		while (iterator.hasNext()) {
			FLayer layer = iterator.nextLayer();
			// don't apply 1.1.x compatibility if a property from 1.9 alpha hyperlink is found
			String auxFieldName = (String) layer.getProperty("legacy.hyperlink.selectedField");
			
			if (auxFieldName == null && selectedField!=null) {
				layer.setProperty("legacy.hyperlink.selectedField", selectedField);
				layer.setProperty("legacy.hyperlink.type", new Integer(type));
				if (extension!=null) {
					layer.setProperty("legacy.hyperlink.extension", extension);
				}
			}
		}
		
	}


	public String getFrameName() {
		return PluginServices.getText(this,"Vista");
	}

	public IWindow createWindow() {
		com.iver.cit.gvsig.project.documents.view.gui.View view = new com.iver.cit.gvsig.project.documents.view.gui.View();
		if (windowData != null)
			view.setWindowData(windowData);
		view.initialize(this.getMapContext());
		view.setModel(this);
	XMLEntity xml = PluginServices.getPluginServices(this)
		.getPersistentXML();
	if (xml.contains(ViewPage.OPEN_VIEW_WINDOW_MAXIMIZED)) {
	    view.getWindowInfo()
		    .setMaximized(
			    xml.getBooleanProperty(ViewPage.OPEN_VIEW_WINDOW_MAXIMIZED));
	}
		callCreateWindow(view);
		return view;
	}

	public IWindow getProperties() {
		return new ViewProperties(this);
	}

	public void exportToXML(XMLEntity root, Project project) throws SaveException {
		XMLEntity viewsRoot = project.getExportXMLTypeRootNode(root,ProjectViewFactory.registerName);
		viewsRoot.addChild(this.getXMLEntity());
		this.exportToXMLLayerDependencies(this.getMapContext().getLayers(),root,project);
		if (this.getMapOverViewContext() != null) {
			this.exportToXMLLayerDependencies(this.getMapOverViewContext().getLayers(),root,project);
		}
	}

	private void exportToXMLLayerDependencies(FLayer layer, XMLEntity root,Project project)
		throws SaveException  {

		if (layer instanceof FLayers) {
			FLayers layers = (FLayers)layer;
			for (int i=0;i< layers.getLayersCount();i++) {
				this.exportToXMLLayerDependencies(layers.getLayer(i),root,project);
			}
		} else {
			if (layer instanceof AlphanumericData) {
				try {
					project.exportToXMLDataSource(root,((AlphanumericData)layer).getRecordset().getName());
				} catch (ReadDriverException e) {
					throw new SaveException(e,layer.getName());
				}

				ProjectTable pt = project.getTable((AlphanumericData) layer);
				if (pt != null) {
					pt.exportToXML(root,project);
				}
			}
		}
	}

	public void importFromXML(XMLEntity root, XMLEntity typeRoot, int elementIndex, Project project, boolean removeDocumentsFromRoot) throws XMLException, ReadDriverException, OpenException {
		XMLEntity element = typeRoot.getChild(elementIndex);
		this.setXMLEntity(element);
		project.addDocument(this);
		if (removeDocumentsFromRoot) {
			typeRoot.removeChild(elementIndex);
		}


		//Cargamos las tables vinculadas:

		//Recuperamos todos los nombres
		XMLEntity tablesRoot = project.getExportXMLTypeRootNode(root,ProjectTableFactory.registerName);
		XMLEntity child;
		// Lo hacemos en un map por si una vista se usa varias veces
		HashMap<String, String> tablesName = new HashMap<String, String>();
		Iterator<XMLEntity> iterTables = tablesRoot.findChildren("viewName",this.getName());
		while (iterTables.hasNext()){
			child = iterTables.next();
			tablesName.put(child.getStringProperty("name"),child.getStringProperty("name"));
		}


		XMLEntity tableXML;

		// Construimos un diccionario ordenado inversamente por el indice
		// del elemento (por si se van eliminando elementos al importar) y
		// como valor el nombre de la vista
		TreeMap tablesToImport = new TreeMap( new Comparator() {

			public int compare(Object o1, Object o2) {

				if (((Integer)o1).intValue() > ((Integer)o2).intValue()) {
					return -1; //o1 first
				} else if (((Integer)o1).intValue() < ((Integer)o2).intValue()){
					return 1; //o1 second
				}
				return 0;
			}

		});
		Iterator iterTablesName = tablesName.keySet().iterator();
		int tableIndex;
		String tableName;
		while (iterTablesName.hasNext()) {
			tableName = (String)iterTablesName.next();
			tableIndex = tablesRoot.firstIndexOfChild("name",tableName);
			tablesToImport.put(new Integer(tableIndex),tableName);
		}

		ProjectTable table;
		ProjectDocumentFactory tableFactory = project.getProjectDocumentFactory(ProjectTableFactory.registerName);

		Iterator iterTablesToImport = tablesToImport.entrySet().iterator();
		Entry entry;
		// Nos recorremos las vistas a importar
		while (iterTablesToImport.hasNext()) {
			entry = (Entry)iterTablesToImport.next();
			tableName = (String)entry.getValue();
			tableIndex = ((Integer)entry.getKey()).intValue();
			table = (ProjectTable)tableFactory.create(project);
			table.importFromXML(root,tablesRoot,tableIndex,project,removeDocumentsFromRoot);


		}

	}

}
