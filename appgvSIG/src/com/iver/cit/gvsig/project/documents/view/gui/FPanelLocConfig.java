/* gvSIG. Sistema de Informaci?n Geogr?fica de la Generalitat Valenciana
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
 *   Av. Blasco Ib??ez, 50
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
package com.iver.cit.gvsig.project.documents.view.gui;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.plaf.basic.BasicArrowButton;

import org.gvsig.gui.beans.swing.JButton;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.AddLayer;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.CancelationException;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.layerOperations.Classifiable;
import com.iver.cit.gvsig.project.documents.view.MapOverview;
import com.iver.cit.gvsig.project.documents.view.legend.gui.ThemeManagerWindow;

/**
 * @author FJP
 *
 * Dialog to config the locator
 */
public class FPanelLocConfig extends JPanel implements ActionListener,IWindow {

	private static final long serialVersionUID = -2271914332135260143L;
	private javax.swing.JLabel jLabel = null;
	private javax.swing.JList jList = null;
	private JButton jBtnAddLayer = null;
	private JButton jBtnRemoveLayer = null;
	private JButton jBtnEditLegend = null;
	private JButton jBtnCancel = null;

	private MapControl mapCtrl;
	private WindowInfo m_viewinfo = null;
	private JPanel pnlButtons = null;
	private BasicArrowButton jBtnUp;
	private BasicArrowButton jBtnDown;

	public FPanelLocConfig( MapControl mc) {
		super();
		mapCtrl = mc;
		initialize();
		refreshList();
		updateControls(null);
	}

	private void refreshList()
	{
		DefaultListModel lstModel = (DefaultListModel) getJList().getModel();
		lstModel.clear();
		for (int i=mapCtrl.getMapContext().getLayers().getLayersCount()-1; i >=0; i--)
		{
			FLayer lyr = mapCtrl.getMapContext().getLayers().getLayer(i);
			lstModel.addElement(lyr.getName());
		}
	}

	private  void initialize() {
		this.setLayout(null);
		this.setSize(555, 210);
		this.add(getJLabel(), null);
		this.add(getJList(), null);
		this.add(getJBtnUp(), null);
		this.add(getJBtnDown(), null);
		this.add(getJPanel(), null);
	}

	private javax.swing.JLabel getJLabel() {
		if (jLabel == null) {
			jLabel = new javax.swing.JLabel();
			jLabel.setText(PluginServices.getText(this,"Capas_del_localizador")+":");
			jLabel.setBounds(10, 15, 132, 25);
		}
		return jLabel;
	}


	public javax.swing.JList getJList() {
		if (jList == null) {
			jList = new javax.swing.JList(new DefaultListModel());
			jList.setBounds(10, 49, 357, 139);
			jList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
			jList.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			jList.addListSelectionListener(new javax.swing.event.ListSelectionListener() {

				public void valueChanged(javax.swing.event.ListSelectionEvent e) {
					updateControls(e);
				}
			});

		}
		return jList;
	}

	private void updateControls(javax.swing.event.ListSelectionEvent e)
	{
		DefaultListModel lstModel = (DefaultListModel) getJList().getModel();
		int selIndex = jList.getSelectedIndex();
		jBtnDown.setEnabled(false);
		jBtnUp.setEnabled(false);

		if (selIndex != -1)
		{
			if (lstModel.getSize() > 1)
			{
				if (selIndex < (lstModel.getSize()-1))
					jBtnDown.setEnabled(true);

				if (selIndex > 0)
					jBtnUp.setEnabled(true);
			}

		}


	}

	private BasicArrowButton getJBtnUp() {
		if (jBtnUp == null) {
			jBtnUp = new javax.swing.plaf.basic.BasicArrowButton(
					javax.swing.SwingConstants.NORTH);
			jBtnUp.setBounds(374, 49, 25, 23);
			jBtnUp.setToolTipText(PluginServices.getText(this,"Subir_capa"));
			jBtnUp.addActionListener(this);
			jBtnUp.setActionCommand("UP");

		}
		return jBtnUp;
	}


	private BasicArrowButton getJBtnDown() {
		if (jBtnDown == null) {
			jBtnDown = new javax.swing.plaf.basic.BasicArrowButton(
					javax.swing.SwingConstants.SOUTH);
			jBtnDown.setBounds(374, 164, 25, 23);
			jBtnDown.setToolTipText(PluginServices.getText(this,"Bajar_capa"));
			jBtnDown.setActionCommand("DOWN");
			jBtnDown.addActionListener(this);
		}
		return jBtnDown;
	}

	private JButton getJBtnAddLayer() {
		if (jBtnAddLayer == null) {
			jBtnAddLayer = new JButton();
			jBtnAddLayer.setText(PluginServices.getText(this,"Anadir_capa")+"...");
			jBtnAddLayer.addActionListener(this);
			jBtnAddLayer.setActionCommand("ADD_LAYER");
		}
		return jBtnAddLayer;
	}

	private JButton getJBtnRemoveLayer() {
		if (jBtnRemoveLayer == null) {
			jBtnRemoveLayer = new JButton();
			jBtnRemoveLayer.setText(PluginServices.getText(this,"Quitar_capa")+"...");
			jBtnRemoveLayer.addActionListener(this);
			jBtnRemoveLayer.setActionCommand("REMOVE_LAYER");

		}
		return jBtnRemoveLayer;
	}


	private JButton getJBtnEditLegend() {
		if (jBtnEditLegend == null) {
			jBtnEditLegend = new JButton();
			jBtnEditLegend.setText(PluginServices.getText(this,"Editar_leyenda")+"...");
			jBtnEditLegend.addActionListener(this);
			jBtnEditLegend.setActionCommand("EDIT_LEGEND");
		}
		return jBtnEditLegend;
	}

	private JButton getJBtnCancel() {
		if (jBtnCancel == null) {
			jBtnCancel = new JButton();
			jBtnCancel.setText(PluginServices.getText(this,"Cerrar"));
			jBtnCancel.setActionCommand("CANCEL");
			jBtnCancel.addActionListener(this);

		}
		return jBtnCancel;
	}


	public void actionPerformed(ActionEvent e)
	{
		DefaultListModel lstModel = (DefaultListModel) getJList().getModel();
		FLayers theLayers = mapCtrl.getMapContext().getLayers();
       // IProjection proj = null;

		int numLayers = theLayers.getLayersCount()-1;

		if (e.getActionCommand() == "CANCEL")
		{
			if (PluginServices.getMainFrame() != null)
			{
				PluginServices.getMDIManager().closeWindow(FPanelLocConfig.this);
			}
			else
			{
				((JDialog) (getParent().getParent().getParent().getParent())).dispose();
			}
		}
		if (e.getActionCommand() == "ADD_LAYER")
		{
            AddLayer addLayer = (AddLayer)PluginServices.getExtension( AddLayer.class);//new AddLayer();
            //addLayer.initialize();
            addLayer.addLayers(mapCtrl);
	        refreshList();
	        updateControls(null);
          
            if (mapCtrl instanceof MapOverview){
            	((MapOverview)mapCtrl).refreshExtent();
            }

 		}
		if (e.getActionCommand() == "REMOVE_LAYER")
		{
			if (jList.getSelectedIndex() != -1)
			{
				theLayers.removeLayer((String) lstModel.get(jList.getSelectedIndex()));
				lstModel.remove(jList.getSelectedIndex());
				///mapCtrl.drawMap();
				updateControls(null);
				  if (mapCtrl instanceof MapOverview){
                	((MapOverview)mapCtrl).refreshExtent();
                }
				PluginServices.getMainFrame().enableControls();
			}
		}
		if (e.getActionCommand() == "EDIT_LEGEND")
		{
			int idSelec = jList.getSelectedIndex();
			if (idSelec != -1)
			{
				FLayer lyr = theLayers.getLayer((String) lstModel.get(idSelec));
				if (lyr instanceof Classifiable)
				{
					ThemeManagerWindow m_LegendEditor = new ThemeManagerWindow(lyr/*, mapCtrl.getMapContext()*/);
					theLayers.setActive(false);
					lyr.setActive(true);
					if (PluginServices.getMainFrame() == null) {
						JDialog dlg = new JDialog();

						m_LegendEditor.setPreferredSize(m_LegendEditor.getSize());
						dlg.getContentPane().add(m_LegendEditor);
						dlg.setModal(true);
						dlg.pack();
						dlg.show();

					} else {
						PluginServices.getMDIManager().addWindow(m_LegendEditor);
					}
				}
				else
				{
					JOptionPane.showMessageDialog(null, PluginServices.getText(this,"Solo_para_capas_vectoriales")+".");
				}

			}

		}
		if (e.getActionCommand() == "UP")
		{
			int idSelec = jList.getSelectedIndex();
		    int fromIndex = idSelec;
		    int toIndex = idSelec-1;
		        FLayer aux = theLayers.getLayer((String) lstModel.get(fromIndex));
		        try {
					theLayers.moveTo(numLayers-fromIndex, numLayers-toIndex);
				} catch (CancelationException e1) {
					e1.printStackTrace();
				}

		        lstModel.remove(fromIndex);
		        lstModel.add(toIndex,aux.getName());

		        jList.setSelectedIndex(toIndex);

		        ///mapCtrl.drawMap();
		}
		if (e.getActionCommand() == "DOWN")
		{
			int idSelec = jList.getSelectedIndex();
		    int fromIndex = idSelec;
		    int toIndex = idSelec+1;
		        FLayer aux = theLayers.getLayer((String) lstModel.get(fromIndex));
		        try {
		        	theLayers.moveTo(numLayers-fromIndex, numLayers-toIndex);
				} catch (CancelationException e1) {
					e1.printStackTrace();
				}

		        lstModel.remove(fromIndex);
		        lstModel.add(toIndex,aux.getName());

		        jList.setSelectedIndex(toIndex);

		        ///mapCtrl.drawMap();
		}


	}

	public WindowInfo getWindowInfo() {
		if (m_viewinfo==null){ // PALETTE para que no afecte cuando se pide la ventana activa y salga la vista.
    			m_viewinfo=new WindowInfo(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
			m_viewinfo.setTitle(PluginServices.getText(this,"Configurar_localizador"));
			m_viewinfo.setWidth(this.getWidth()+8);
			m_viewinfo.setHeight(this.getHeight()+8);
		}
			return m_viewinfo;
		}

	public void viewActivated() {
	}


	private JPanel getJPanel() {
		if (pnlButtons == null) {
			pnlButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 5));
			pnlButtons.setBounds(new java.awt.Rectangle(0,200, this.getWidth(),37));
			pnlButtons.add(getJBtnAddLayer(), null);
			pnlButtons.add(getJBtnRemoveLayer(), null);
			pnlButtons.add(getJBtnEditLegend(), null);
			pnlButtons.add(getJBtnCancel(), null);
		}
		return pnlButtons;
	}


	public MapControl getMapCtrl() {
		return mapCtrl;
	}

	public Object getWindowProfile() {
		return WindowInfo.DIALOG_PROFILE;
	}

        }
