package org.gvsig.gpe;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;

import org.gvsig.gpe.gui.dialogs.SelectVersionListener;
import org.gvsig.gpe.gui.dialogs.SelectVersionWindow;
import org.gvsig.gpe.writer.GPEWriterHandler;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.StopEditing;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayersIterator;
import com.iver.cit.gvsig.project.documents.view.gui.IView;

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
public class GPEWriterExtension extends Extension{
	private String writersFile = "gvSIG" + File.separatorChar + "extensiones" + File.separatorChar + 
	"org.gvsig.gpe" + File.separatorChar + "writer.properties";

	/*
	 * (non-Javadoc)
	 * @see com.iver.cit.gvsig.ExportTo#execute(java.lang.String)
	 */
	public void execute(String format) {
		ArrayList writers = null;
		boolean schemaEnabled = true;
		if (format.equals("GML")){
			writers =GPERegister.getWriterHandlerByFormat("text/xml; subtype=gml/3.1.2");
			ArrayList auxWriters = GPERegister.getWriterHandlerByFormat("text/xml; subtype=gml/2.1.2");
			for (int i=0 ; i<auxWriters.size() ; i++){
				writers.add(auxWriters.get(i));
			}
		}else if(format.equals("KML")){
			writers = GPERegister.getWriterHandlerByFormat("text/xml; subtype=kml/2.1");
			schemaEnabled = false;
		}
		//Creates the window
		SelectVersionWindow window = new SelectVersionWindow(schemaEnabled);
		
		for (int i=0 ; i<writers.size() ; i++){
			window.addWriter((GPEWriterHandler)writers.get(i));			
		}
		//Sets the listeners and shows the window
		SelectVersionListener listener = new SelectVersionListener(window);
		window.addListener(listener);
		window.setFile(window.getDefaultFileName());
		PluginServices.getMDIManager().addCentredWindow(window);
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#initialize()
	 */
	public void initialize() {
//		ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
//		extensionPoints.add("AplicationPreferences","GPEPreferencesPage", GPEPreferencesPage.class);
			
		loadWriters();
		StopEditing.addExportFormat("GML", this.getClass());
		StopEditing.addExportFormat("KML", this.getClass());
	}

	/**
	 * Load the GPE writers from a file
	 */
	private void loadWriters(){
		File file = new File(writersFile);
		if (!file.exists()){
			NotificationManager.addWarning("File not found",
					new FileNotFoundException());
			return;
		}
		try {
			GPERegister.addWritersFile(file);
		} catch (Exception e) {
			NotificationManager.addWarning("GPE writers file not found",
					new FileNotFoundException());
		}
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#isEnabled()
	 */
	public boolean isEnabled() {
		com.iver.andami.ui.mdiManager.IWindow window = PluginServices.getMDIManager()
		.getActiveWindow();

		if (window == null) {
			return false;
		}

		if (!(window instanceof IView)){
			return false;
		}

		IView view = (IView)window;
		
		FLayer[] acc = view.getMapControl().getMapContext().getLayers().getActives();
		return (acc != null && acc.length == 1 && acc[0] instanceof FLyrVect);
	}

	/*
	 * (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#isVisible()
	 */
	public boolean isVisible() {
		com.iver.andami.ui.mdiManager.IWindow window = PluginServices.getMDIManager()
		.getActiveWindow();

		if (window == null) {
			return false;
		}

		return (window instanceof IView);	
	}
}
