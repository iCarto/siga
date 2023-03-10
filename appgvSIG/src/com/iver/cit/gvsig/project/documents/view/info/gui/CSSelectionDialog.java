/*
 * Created on 26-ene-2005
 */
package com.iver.cit.gvsig.project.documents.view.info.gui;

import org.cresques.cts.IProjection;
import org.cresques.ui.cts.CSSelectionDialogPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.gui.panels.crs.ISelectCrsPanel;

/**
 * @author Luis W. Sevilla (sevilla_lui@gva.es)
 */
public class CSSelectionDialog
	extends CSSelectionDialogPanel implements IWindow, ISelectCrsPanel {
	private boolean okPressed = false;
	private IProjection lastProj = null;

	/**
	 * 
	 */
	public CSSelectionDialog() {
		super();
		this.init();
	}
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void init() {
        this.setBounds(0, 0, 350, 260);
        getAcceptButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                PluginServices.getMDIManager().closeWindow(CSSelectionDialog.this);
                okPressed = true;
            }
        });
        getCancelButton().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                setProjection(lastProj);
                PluginServices.getMDIManager().closeWindow(CSSelectionDialog.this);
                okPressed = false;
            }
        });
	}
	/* (non-Javadoc)
	 * @see com.iver.andami.ui.mdiManager.View#getViewInfo()
	 */
	public WindowInfo getWindowInfo() {
		WindowInfo m_viewinfo=new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE);
   		m_viewinfo.setTitle(PluginServices.getText(this, "selecciona_sistema_de_referencia"));
		return m_viewinfo;
	}

	public boolean isOkPressed() { return okPressed; }
	
	/**
	 * @return
	 */
	public IProjection getProjection() {
		return getProjPanel().getProjection();
	}
	/**
	 * @param proj
	 */
	public void setProjection(IProjection proj) {
		lastProj = proj;
		getProjPanel().setProjection(proj);
	}
	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}
}
