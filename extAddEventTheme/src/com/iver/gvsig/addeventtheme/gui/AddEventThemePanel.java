/*
 * Created on 09-nov-2005
 *
 * gvSIG. Sistema de Informacin Geogrfica de la Generalitat Valenciana
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
 *   Av. Blasco Ibez, 50
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
package com.iver.gvsig.addeventtheme.gui;

import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cresques.cts.IProjection;
import org.gvsig.gui.beans.AcceptCancelPanel;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.SourceInfo;
import com.hardcode.gdbms.engine.data.file.FileSourceInfo;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.fmap.layers.layerOperations.AlphanumericData;
import com.iver.cit.gvsig.project.documents.table.ProjectTable;
import com.iver.gvsig.addeventtheme.AddEventThemListener;
import com.iver.gvsig.addeventtheme.AddEventThemeDriver;

/**
 * The AddEventThemePanel class creates a JPanel where the
 * user can input the name of the gvSIG Table from that the
 * plugin will create the new point Layer, and the fields
 * corresponding with the point coordinates.
 *
 * @author jmorell
 */
public class AddEventThemePanel extends JPanel implements IWindow {
    private static final long serialVersionUID = 1L;
    private WindowInfo viewInfo = null;
    private JLabel tableLabel = null;
    private JLabel xLabel = null;
    private JLabel yLabel = null;
    private JComboBox tableComboBox = null;
    private JComboBox xComboBox = null;
    private JComboBox yComboBox = null;
    //private JButton okButton = null;
    private AcceptCancelPanel acceptPanel = null;
    private MapContext mapContext;
    private ArrayList tableList;
    private String firstCoordinate;
    private String secondCoordinate;

    /**
     * This is the default constructor
     */
    public AddEventThemePanel(MapContext mapContext, ArrayList tableList) {
        super();
        this.mapContext = mapContext;
        this.tableList = tableList;
        initializeCoordinates();
        initialize();
    }

    private void initializeCoordinates() {
        IProjection proj = mapContext.getProjection();
        if (!proj.isProjected() || MapContext.getDistanceNames()[mapContext.getViewPort().getDistanceUnits()].equals("Grados")) {
            firstCoordinate = "Lon";
            secondCoordinate = "Lat";
        } else{
            firstCoordinate = "X";
            secondCoordinate = "Y";

        }
    }

    /**
     * This method initializes this
     *
     * @return void
     */
    private void initialize() {
        yLabel = new JLabel();
        yLabel.setBounds(6, 68, 90, 23);
        yLabel.setText(secondCoordinate + ":");
        xLabel = new JLabel();
        xLabel.setBounds(6, 37, 90, 23);
        xLabel.setText(firstCoordinate + ":");
        tableLabel = new JLabel();
        tableLabel.setBounds(6, 6, 90, 23);
        tableLabel.setText(PluginServices.getText(this,"Tabla") + ":");
        this.setLayout(null);
        this.setSize(400, 135);
        this.add(tableLabel, null);
        this.add(xLabel, null);
        this.add(yLabel, null);
        this.add(getTableComboBox(), null);
        this.add(getXComboBox(), null);
        this.add(getYComboBox(), null);
        this.add(getAcceptPanel(), null);
    }

    private String[] getTableNames() {
        String[] tableNames = new String[tableList.size()];
        for (int i=0;i<tableList.size();i++) {
            tableNames[i] = ((ProjectTable)tableList.get(i)).getName();
        }
        return tableNames;
    }
    private String[] getFieldNames() {
        String tableName = (String)tableComboBox.getSelectedItem();
        ProjectTable projectTable = getProjectTable(tableName);
        DataSource ds;
        ArrayList fieldName=new ArrayList();
        try {
            ds = projectTable.getModelo().getRecordset();
            for (int i = 0; i < ds.getFieldCount(); i++) {
            	// Podr�amos comprobar si es un campo num�rico pero entonces
            	// no va con el driver de csv => moraleja: cuando
            	// se cambia el driver de csv para que sepa de qu� campos
            	// hablamos, se habilita esta l�nea.
            	// if (ds.getFieldType(i)==Types.DOUBLE || ds.getFieldType(i)==Types.INTEGER || ds.getFieldType(i)==Types.VARCHAR)
            		fieldName.add(ds.getFieldName(i));
            }
        } catch (ReadDriverException e) {
            e.printStackTrace();
            NotificationManager.addError(e);
        }
        return (String[])fieldName.toArray(new String[0]);
    }

    private ProjectTable getProjectTable(String tableName) {
        ProjectTable projectTable = null;
        for (int i=0;i<tableList.size();i++) {
            if (((ProjectTable)tableList.get(i)).getName().equals(tableName)) {
                projectTable = (ProjectTable)tableList.get(i);
                break;
            }
        }
        return projectTable;
    }

    private void createNewLayerFromDataSource() {
        String tableName = (String)tableComboBox.getSelectedItem();
        ProjectTable projectTable = getProjectTable(tableName);
        try {
	        DataSource ds = projectTable.getModelo().getRecordset();
	        int xFieldIndex = 0;
	        int yFieldIndex = 0;

            xFieldIndex = ds.getFieldIndexByName(getXFieldName());
            yFieldIndex = ds.getFieldIndexByName(getYFieldName());
	        AddEventThemeDriver addEventThemeDriver = new AddEventThemeDriver();
	        addEventThemeDriver.setData(ds, xFieldIndex, yFieldIndex);
	        FLayer capa = null;
            capa = LayerFactory.createLayer(tableName, addEventThemeDriver, mapContext.getProjection());
	        capa.addLayerListener(new AddEventThemListener());
	        
	        //Guardamos el nombre del fichero asociado a la capa de eventos 
	        SourceInfo srcInfo = ds.getDataSourceFactory().getDriverInfo(tableName);
	        if(srcInfo instanceof FileSourceInfo) 
	        	capa.setProperty("DBFFile", ((FileSourceInfo)srcInfo).file);
	        
	        projectTable.setAssociatedTable((AlphanumericData)capa);
	        mapContext.getLayers().addLayer(capa);
        } catch (ReadDriverException e1) {
            NotificationManager.addError(e1);
        }

    }

    private String getXFieldName() {
        return ((String)xComboBox.getSelectedItem());
    }

    private String getYFieldName() {
        return ((String)yComboBox.getSelectedItem());
    }

    /* (non-Javadoc)
     * @see com.iver.andami.ui.mdiManager.View#getViewInfo()
     */
    public WindowInfo getWindowInfo() {
        // TODO Auto-generated method stub
        if (viewInfo == null) {
            viewInfo=new WindowInfo(WindowInfo.MODALDIALOG);
            viewInfo.setTitle(PluginServices.getText(this,"Anadir_capa_de_eventos"));
        }
        return viewInfo;
    }

    /**
     * This method initializes tableComboBox
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getTableComboBox() {
    	if (tableComboBox == null) {
    		tableComboBox = new JComboBox();
            DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getTableNames());
            tableComboBox.setModel(defaultModel);
    		tableComboBox.setBounds(92, 6, 290, 23);
    		tableComboBox.addItemListener(new java.awt.event.ItemListener() {
    			public void itemStateChanged(java.awt.event.ItemEvent e) {
    				System.out.println("itemStateChanged()"); // TODO Auto-generated Event stub itemStateChanged()
                    // Cambiar el estado del xComboBox
                    DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getFieldNames());
                    xComboBox.setModel(defaultModel);
                    // Cambiar el estado del yComboBox
                    defaultModel = new DefaultComboBoxModel(getFieldNames());
                    yComboBox.setModel(defaultModel);
    			}
    		});
    	}
    	return tableComboBox;
    }

    /**
     * This method initializes xComboBox
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getXComboBox() {
    	if (xComboBox == null) {
    		xComboBox = new JComboBox();
            DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getFieldNames());
            xComboBox.setModel(defaultModel);
    		xComboBox.setBounds(92, 37, 290, 23);
    	}
    	return xComboBox;
    }

    /**
     * This method initializes yComboBox
     *
     * @return javax.swing.JComboBox
     */
    private JComboBox getYComboBox() {
    	if (yComboBox == null) {
    		yComboBox = new JComboBox();
            DefaultComboBoxModel defaultModel = new DefaultComboBoxModel(getFieldNames());
            yComboBox.setModel(defaultModel);
    		yComboBox.setBounds(92, 68, 290, 23);
    	}
    	return yComboBox;
    }

    /**
     * This method initializes okButton
     *
     * @return javax.swing.JButton
     */
    private AcceptCancelPanel getAcceptPanel() {
    	if (acceptPanel == null) {
    		acceptPanel = new AcceptCancelPanel(
    				new java.awt.event.ActionListener() {
    	    			public void actionPerformed(java.awt.event.ActionEvent e) {
    	    				System.out.println("actionPerformed()"); // TODO Auto-generated Event stub actionPerformed()
    	                    createNewLayerFromDataSource();
    	                    // y sale.
    	                    if (PluginServices.getMainFrame() == null)
    	                        ((JDialog) (getParent().getParent().getParent().getParent())).dispose();
    	                    else
    	                        PluginServices.getMDIManager().closeWindow(AddEventThemePanel.this);
    	    			}
    	    		},
    				new java.awt.event.ActionListener() {
    	    			public void actionPerformed(java.awt.event.ActionEvent e) {
    	                    if (PluginServices.getMainFrame() == null)
    	                        ((JDialog) (getParent().getParent().getParent().getParent())).dispose();
    	                    else
    	                        PluginServices.getMDIManager().closeWindow(AddEventThemePanel.this);
    	    			}
    	    		}
    				);
    		acceptPanel.setBounds(0, 100, getWidth()-6, 30);
    	}
    	return acceptPanel;
    }

	public Object getWindowProfile() {
		// TODO Auto-generated method stub
		return WindowInfo.MODALDIALOG;
	}
}  //  @jve:decl-index=0:visual-constraint="10,10"
