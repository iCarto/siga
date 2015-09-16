/*
 * Copyright (c) 2010. CartoLab, Universidad de A Coru�a
 *
 * This file is part of ELLE
 *
 * ELLE is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * ELLE is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with ELLE.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.addlayer.AddLayerDialog;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.gui.panels.CRSSelectPanel;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.elle.db.DBStructure;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardComponent;
import es.udc.cartolab.gvsig.elle.gui.wizard.WizardException;
import es.udc.cartolab.gvsig.elle.utils.MapDAO;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class SigaLoadMapWizardComponent extends WizardComponent implements
	ActionListener {

    private static final Logger logger = Logger
	    .getLogger(SigaLoadMapWizardComponent.class);

    protected CRSSelectPanel crsPanel = null;
    protected JList mapList;
    private DBSession dbs;
    private JPanel listPanel;
    private JTextArea layerTextArea;
    private String[][] layers;

    public final static String PROPERTY_VEW = "view";
    public static final String PROPERTY_MAP_NAME = "property_map_name";

    public SigaLoadMapWizardComponent(Map<String, Object> properties) {
	super(properties);

	dbs = DBSession.getCurrentSession();

	setLayout(new BorderLayout());

	add(getListPanel(), BorderLayout.CENTER);
	add(getCRSPanel(), BorderLayout.SOUTH);
    }

    @Override
    public boolean canFinish() {
	return false;
    }

    @Override
    public boolean canNext() {
	if (mapList != null) {
	    return mapList.getSelectedIndices().length == 1;
	}
	return false;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	callStateChanged();
    }

    private JPanel getCRSPanel() {
	if (crsPanel == null) {
	    crsPanel = CRSSelectPanel.getPanel(AddLayerDialog
		    .getLastProjection());
	    crsPanel.addActionListener(new java.awt.event.ActionListener() {
		@Override
		public void actionPerformed(java.awt.event.ActionEvent e) {
		    if (crsPanel.isOkPressed()) {
			AddLayerDialog.setLastProjection(crsPanel.getCurProj());
		    }
		}
	    });
	}
	return crsPanel;
    }

    private JPanel getListPanel() {
	if (listPanel == null) {

	    listPanel = new JPanel();

	    try {
		FormPanel form = null;

		InputStream stream = getClass().getClassLoader()
			.getResourceAsStream("forms/loadMap.jfrm");
		form = new FormPanel(stream);
		form.setFocusTraversalPolicyProvider(true);

		listPanel.add(form);

		dbs = DBSession.getCurrentSession();

		if (dbs.tableExists(DBStructure.getSchema(),
			DBStructure.getMapTable())
			&& dbs.tableExists(DBStructure.getSchema(),
				DBStructure.getOverviewTable())) {

		    Vector<String> mapsToShow = new Vector<String>();

		    String[] maps = MapDAO.getInstance().getMaps();

		    // Show only maps that doesn't start like 'BDD_'
		    for (int i = 0; i < maps.length; i++) {
			if (!maps[i].startsWith("BDD_")) {
			    mapsToShow.add(maps[i]);
			}
		    }
		    Collections.sort(mapsToShow);
		    // layerList = form.getList("layerList");
		    mapList = form.getList("mapList");
		    mapList.setListData(mapsToShow);

		    layerTextArea = (JTextArea) form
			    .getComponentByName("layerTextArea");
		    layerTextArea.setEditable(false);

		    JLabel mapLabel = form.getLabel("mapLabel");
		    JLabel layerLabel = form.getLabel("layerLabel");

		    mapLabel.setText(PluginServices.getText(this, "map_load"));
		    layerLabel.setText(PluginServices.getText(this,
			    "layer_load"));

		    mapList.addListSelectionListener(new ListSelectionListener() {

			@Override
			public void valueChanged(ListSelectionEvent arg0) {
			    int[] selected = mapList.getSelectedIndices();
			    callStateChanged();

			    if (selected.length == 1) {
				String selectedValue = (String) mapList
					.getSelectedValues()[0];
				String where = String.format(
					"WHERE mapa = '%s'", selectedValue);
				try {
				    layers = dbs.getTable(
					    DBStructure.getMapTable(),
					    DBStructure.getSchema(), where,
					    new String[] { "posicion" }, true);
				    String layerText = "";
				    for (int i = 0; i < layers.length; i++) {
					layerText = layerText + layers[i][1]
						+ "\n";
				    }

				    layerTextArea.setText(layerText);

				} catch (SQLException e) {
				    // TODO Auto-generated catch block
				    JOptionPane.showMessageDialog(null,
					    "Error SQL: " + e.getMessage(),
					    "SQL Exception",
					    JOptionPane.ERROR_MESSAGE);
				    try {
					dbs = DBSession.reconnect();
				    } catch (DBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				    }
				}

			    } else {
				layerTextArea.setText("");
			    }

			}

		    });
		} else {
		    listPanel = new JPanel();
		    JLabel label = new JLabel(PluginServices.getText(this,
			    "no_map_table_on_schema"));
		    listPanel.add(label);
		}

	    } catch (SQLException e) {
		try {
		    dbs = DBSession.reconnect();
		} catch (DBException e1) {
		    // TODO Auto-generated catch block
		    e1.printStackTrace();
		}
		// exception
		e.printStackTrace();
	    } catch (FormException e2) {
		logger.error(e2.getStackTrace(), e2);
	    }
	}
	return listPanel;
    }

    @Override
    public String getWizardComponentName() {
	return "load_map_wizard_component";
    }

    public String getMapName() {
	return mapList.getSelectedValue().toString();
    }

    @Override
    public void showComponent() {
    }

    @Override
    public void finish() throws WizardException {
    }

    @Override
    public void setProperties() {
	properties.put(SigaLoadMapWizardComponent.PROPERTY_MAP_NAME,
		mapList.getSelectedValue());
    }
}
