/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
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
 */
package com.iver.cit.gvsig;

import java.awt.Component;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.cresques.cts.ICoordTrans;
import org.cresques.cts.IProjection;

import com.hardcode.driverManager.DriverManager;
import com.hardcode.driverManager.WriterManager;
import com.hardcode.gdbms.driver.exceptions.FileNotFoundDriverException;
import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.addlayer.AddLayerDialog;
import com.iver.cit.gvsig.addlayer.fileopen.FileOpenWizard;
import com.iver.cit.gvsig.addlayer.fileopen.solve.FileNotFoundSolve;
import com.iver.cit.gvsig.addlayer.fileopen.vectorial.VectorialFileOpen;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.ViewPort;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.gui.WizardPanel;
import com.iver.cit.gvsig.project.documents.ProjectDocument;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.iver.cit.gvsig.project.documents.view.gui.IView;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;


/**
 * Extensi�n que abre un di�logo para seleccionar la capa o capas que se quieren
 * a�adir a la vista.
 *
 * @author Fernando Gonz�lez Cort�s
 */
public class AddLayer extends Extension {
	public AddLayerDialog fopen = null;

	private static ArrayList wizardStack = null;

	static {
		AddLayer.wizardStack = new ArrayList();
		// A�adimos el panel al wizard de cargar capa. (Esto es temporal hasta que
    // el actual sea totalmente extensible)
		AddLayer.addWizard(FileOpenWizard.class);
	}

	public static void addWizard(Class wpClass) {
		AddLayer.wizardStack.add(wpClass);
	}

	public static WizardPanel getInstance(int i)
			throws IllegalArgumentException, SecurityException,
			InstantiationException, IllegalAccessException,
			InvocationTargetException, NoSuchMethodException {
		Class wpClass = (Class) AddLayer.wizardStack.get(i);
		Class[] args = {};
		Object[] params = {};
		WizardPanel wp = (WizardPanel) wpClass.getConstructor(args)
				.newInstance(params);

		wp.initWizard();

		return wp;
	}

	/**
	 * @see com.iver.mdiApp.plugins.IExtension#isVisible()
	 */
	public boolean isVisible() {
		com.iver.andami.ui.mdiManager.IWindow window = PluginServices.getMDIManager()
															 .getActiveWindow();

		if (window == null) {
			return false;
		}

		// Any view derived from BaseView should have AddLayer available

		IView view;
		try {
			view = (IView)window;
		}
		catch (ClassCastException e) {
		    return false;
		}

		if (view == null) {
		    return false;
		}

		BaseView baseView = (BaseView)view;
		return (baseView != null);
	}

	/**
	 * @see com.iver.andami.plugins.IExtension#postInitialize()
	 */
	public void postInitialize() {
		LayerFactory.initialize();
		DriverManager dm=LayerFactory.getDM();
		PluginServices.addLoaders(dm.getDriverClassLoaders());
		WriterManager wm=LayerFactory.getWM();
		PluginServices.addLoaders(wm.getWriterClassLoaders());

		ExtensionPoints extensionPoints = ExtensionPointsSingleton.getInstance();
		extensionPoints.add("FileExtendingOpenDialog", "FileOpenVectorial", VectorialFileOpen.class);
	}

	public static void checkProjection(FLayer lyr, ViewPort viewPort) {
		if (lyr instanceof FLayers){
			FLayers layers=(FLayers)lyr;
			for (int i=0;i<layers.getLayersCount();i++){
				checkProjection(layers.getLayer(i),viewPort);
			}
		}
		if (lyr instanceof FLyrVect) {
			FLyrVect lyrVect = (FLyrVect) lyr;
			IProjection proj = lyr.getProjection();
			// Comprobar que la projecci�n es la misma que la vista
			if (proj == null) {
				// SUPONEMOS que la capa est� en la proyecci�n que
				// estamos pidiendo (que ya es mucho suponer, ya).
				lyrVect.setProjection(viewPort.getProjection());
				return;
			}
			int option = JOptionPane.YES_OPTION;
			if (!viewPort.getProjection().getAbrev().equals(proj.getAbrev())) {
				option = JOptionPane.showConfirmDialog((Component)PluginServices.getMainFrame(), PluginServices
						.getText(AddLayer.class, "reproyectar_aviso")+"\n"+ PluginServices.getText(AddLayer.class,"Capa")+": "+lyrVect.getName(), PluginServices
						.getText(AddLayer.class, "reproyectar_pregunta"),
						JOptionPane.YES_NO_OPTION);

				if (option != JOptionPane.OK_OPTION) {
					return;
				}

				ICoordTrans ct = proj.getCT(viewPort.getProjection());
				lyrVect.setCoordTrans(ct);
				System.err.println("coordTrans = " + proj.getAbrev() + " "
						+ viewPort.getProjection().getAbrev());
			}
		}

	}

	/**
	 * @see com.iver.andami.plugins.IExtension#execute(java.lang.String)
	 */
	public void execute(String actionCommand) {
		// Project project = ((ProjectExtension)
		// PluginServices.getExtension(ProjectExtension.class)).getProject();
		BaseView theView = (BaseView) PluginServices.getMDIManager().getActiveWindow();
		MapControl mapControl=theView.getMapControl();
		this.addLayers(mapControl);
		mapControl.getMapContext().callLegendChanged();
		((ProjectDocument)theView.getModel()).setModified(true);
	}

	/**
	 * @see com.iver.andami.plugins.IExtension#isEnabled()
	 */
	public boolean isEnabled() {
		return true;
	}

    /**
     * Creates FOpenDialog, and adds file tab, and additional registered tabs
     *
     * @param mapControl
     *
     * @return FOpenDialog
     */
	private AddLayerDialog createFOpenDialog(MapControl mapControl) {
		fopen = new AddLayerDialog();

		// after that, all registerez tabs (wizardpanels implementations)
		for (int i = 0; i < wizardStack.size(); i++) {
			WizardPanel wp;
			try {
				wp = AddLayer.getInstance(i);
		wp.setMapCtrl(mapControl);
				fopen.addWizardTab(wp.getTabName(), wp);
			} catch (IllegalArgumentException e) {
				e.printStackTrace();
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (InstantiationException e) {
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				e.printStackTrace();
			}
		}// for
		return fopen;
	}

	/**
	 * Adds to mapcontrol all layers selected by user in the specified WizardPanel.
	 *
	 * @param mapControl
	 * 	MapControl on which we want to load user selected layers.
	 * @param wizardPanel
	 * 	WizardPanel where user selected the layers to load
	 * @return
	 */
	private boolean loadGenericWizardPanelLayers(MapControl mapControl, WizardPanel wp) {
		FLayer lyr = null;
		wp.setMapCtrl(mapControl);
		wp.execute();
		lyr = wp.getLayer();

		if((lyr != null) && !(lyr.isOk())){
			//if the layer is not okay (it has errors) process them
			processErrorsOfLayer(lyr, mapControl);
		}

		if (lyr != null) {
			lyr.setVisible(true);
			mapControl.getMapContext().beginAtomicEvent();
			checkProjection(lyr, mapControl.getViewPort());
			try {
				mapControl.getMapContext().getLayers().addLayer(lyr);
			} catch (CancelationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mapControl.getMapContext().endAtomicEvent();
			return true;
		}
		return false;
	}

	/**
	 * This method process the errors found in a layer
	 * @param lyr
	 * @param mapControl
	 */

	private void processErrorsOfLayer(FLayer lyr, MapControl mapControl){
//		List errors = lyr.getErrors();
//		wp.callError(null);
		mapControl.getMapContext().callNewErrorEvent(null);
	}

	/**
	 * Abre dialogo para a�adir capas y las a�ade en mapControl
	 *
	 * Devuelve true si se han a�adido capas.
	 */
	public boolean addLayers(MapControl mapControl) {
        AddLayerDialog.setLastProjection(mapControl.getProjection());
		// create and show the modal fopen dialog
	fopen = createFOpenDialog(mapControl);
		PluginServices.getMDIManager().addWindow(fopen);

		if (fopen.isAccepted()) {
			if (fopen.getSelectedTab() instanceof WizardPanel) {
				WizardPanel wp = (WizardPanel) fopen.getSelectedTab();
				return loadGenericWizardPanelLayers(mapControl, wp);
			} else {
				JOptionPane.showMessageDialog((Component) PluginServices
						.getMainFrame(), PluginServices.getText(this,"ninguna_capa_seleccionada"));
			}

		}
		return false;
	}

	/* (non-Javadoc)
	 * @see com.iver.andami.plugins.IExtension#initialize()
	 */
	public void initialize() {
		//Listener para resolver un FileDriverNotFoundException
		LayerFactory.addSolveErrorForLayer(FileNotFoundDriverException.class,new FileNotFoundSolve());

		PluginServices.getIconTheme().registerDefault(
				"layer-add",
				this.getClass().getClassLoader().getResource("images/addlayer.png")
			);

	}
}

// [eiel-gestion-conexiones]