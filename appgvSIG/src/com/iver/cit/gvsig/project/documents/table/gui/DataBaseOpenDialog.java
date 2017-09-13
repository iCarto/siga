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
package com.iver.cit.gvsig.project.documents.table.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.geom.Rectangle2D;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;

import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.DefaultListSelectionModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListModel;
import javax.swing.ListSelectionModel;
import javax.swing.border.TitledBorder;
import javax.swing.event.ListSelectionEvent;

import org.apache.log4j.Logger;
import org.cresques.cts.IProjection;
import org.gvsig.gui.beans.swing.JButton;

import com.hardcode.driverManager.Driver;
import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.engine.data.driver.AlphanumericDBDriver;
import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.SingleVectorialDBConnectionExtension;
import com.iver.cit.gvsig.addlayer.AddLayerDialog;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.core.ICanReproject;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.DBLayerDefinition;
import com.iver.cit.gvsig.fmap.drivers.FieldDescription;
import com.iver.cit.gvsig.fmap.drivers.IConnection;
import com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver;
import com.iver.cit.gvsig.fmap.drivers.IVectorialJDBCDriver;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.layers.FLayers;
import com.iver.cit.gvsig.fmap.layers.LayerFactory;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;
import com.iver.utiles.swing.JPasswordDlg;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.AvailableTablesCheckBoxList;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.DBConnectionParamsDialog;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.TablesListItem;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.TablesListItemSimple;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.UserSelectedFieldsPanel;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.UserTableSettingsPanel;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.WizardVectorialDB;
/**
 * DOCUMENT ME!
 *
 * @author Fernando González Cortés
 * @author jldominguez
 */
public class DataBaseOpenDialog extends JPanel implements ActionListener {
	
	private AddLayerDialog parent_dialog = null;
	
	private int _width = 500;
	private int _height = 480;
	private int _margin = 6;
	private int _conn_height = 55;
	private int _conn_button_height = 21;
	private int _conn_button_width = 26;
	
	
	
    /**
     * This is the default constructor
     */
    public DataBaseOpenDialog(AddLayerDialog dlg) {
        super();
        parent_dialog = dlg;
        initialize();
    }

    /**
     * This is the default constructor
     */
    public DataBaseOpenDialog() {
        super();
        initialize();
    }

	private static Logger logger = Logger.getLogger(WizardVectorialDB.class.getName());
	protected IConnection conex = null;
	private ConnectionWithParams selectedDataSource = null;
	private JPanel connPanel = null;
	private JPanel tablesPanel = null;
	private JScrollPane tablesScrollPane = null;
	private JScrollPane _fieldsScrollPane = null;
	
	private AvailableTablesList tablesList = null;
	private JList _fieldsList = null;
	
	private JComboBox datasourceComboBox = null;
	// private UserTableSettingsPanel settingsPanel = null;
	private JPanel fieldsPanel = null;
	// private UserTableSettingsPanel emptySettingsPanel = null;
	// private UserSelectedFieldsPanel emptyFieldsPanel = null;
	private JButton dbButton = null;
	// private BaseView view = null;
	

	private void initialize() {
		setLayout(null);
		this.setSize(_width, _height);

		add(getConnectionPanel(), null);
		loadDBDatasourcesCombo();
		getDatasourceComboBox().addActionListener(this);
		add(getTablesPanel(), null);
		add(getFieldsPanel(), null);
	}

	private JPanel getFieldsPanel() {

		if (fieldsPanel == null) {
			fieldsPanel = new JPanel();
			fieldsPanel.setLayout(new BorderLayout());
			fieldsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, PluginServices.getText(this, "choose_table"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			fieldsPanel.setBounds(new java.awt.Rectangle(
					(_width-3*_margin)/2+2*_margin,
					_conn_height+2*_margin,
					(_width-3*_margin)/2,
					_height-_conn_height-3*_margin));
			fieldsPanel.add(getFieldsScrollPane(), java.awt.BorderLayout.CENTER);
		}

		return fieldsPanel;
	}




	private JScrollPane getFieldsScrollPane() {
		if (_fieldsScrollPane == null) {
			_fieldsScrollPane = new JScrollPane();
		}

		return _fieldsScrollPane;
	}

	protected void loadDBDatasourcesCombo() {
		getDatasourceComboBox().removeAllItems();

		getDatasourceComboBox().addItem(new ConnectionWithParams());

		ConnectionWithParams[] conn = SingleDBConnectionManager.instance()
		.getAllConnections();

		if (conn == null) {
			return;
		}

		Driver _drv = null;
		for (int i = 0; i < conn.length; i++) {
			
			_drv = SingleDBConnectionManager.getInstanceFromName(conn[i].getDrvName());
			if (_drv != null && _drv instanceof AlphanumericDBDriver) {
				getDatasourceComboBox().addItem(conn[i]);
			}
		}
	}

	public void saveConns() {
		SingleVectorialDBConnectionExtension.saveAllToPersistence();
	}

	protected String getSelectedTable() {
		return null;
	}

	/**
	 * This method initializes namePanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getConnectionPanel() {
		if (connPanel == null) {
			connPanel = new JPanel();
			connPanel.setLayout(null);
			connPanel.setBounds(new java.awt.Rectangle(_margin, _margin, _width-2*_margin, _conn_height));
			connPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, PluginServices.getText(this, "choose_connection"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			connPanel.add(getDatasourceComboBox(), null);
			connPanel.add(getJdbcButton(), null);
		}

		return connPanel;
	}

	/**
	 * This method initializes tablesPanel
	 *
	 * @return javax.swing.JPanel
	 */
	private JPanel getTablesPanel() {
		if (tablesPanel == null) {
			tablesPanel = new JPanel();
			tablesPanel.setLayout(new BorderLayout());
			tablesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder(
					null, PluginServices.getText(this, "choose_table"),
					javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
					javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
			tablesPanel.setBounds(new java.awt.Rectangle(_margin, _conn_height+2*_margin, (_width-3*_margin)/2, _height-3*_margin-_conn_height));
			tablesPanel.add(getTablesScrollPane(), java.awt.BorderLayout.CENTER);
		}

		return tablesPanel;
	}

	/**
	 * This method initializes tablesScrollPane
	 *
	 * @return javax.swing.JScrollPane
	 */
	private JScrollPane getTablesScrollPane() {
		if (tablesScrollPane == null) {
			tablesScrollPane = new JScrollPane();
		}

		return tablesScrollPane;
	}

	/**
	 * This method initializes tablesList
	 *
	 * @return javax.swing.JList
	 */
	private AvailableTablesList getTablesList() {
		if (tablesList == null) {
			tablesList = new AvailableTablesList(this);
			DefaultListModel lmodel = new DefaultListModel();
			tablesList.setModel(lmodel);
			// tablesList.addListSelectionListener(this);
		}

		return tablesList;
	}

	/**
	 * This method initializes jComboBox
	 *
	 * @return javax.swing.JComboBox
	 */
	protected JComboBox getDatasourceComboBox() {
		if (datasourceComboBox == null) {
			datasourceComboBox = new JComboBox();
			datasourceComboBox.setBounds(new java.awt.Rectangle(
					_margin,
					3*_margin,
					_width-5*_margin-_conn_button_width, _conn_button_height));
		}

		return datasourceComboBox;
	}

	public void actionPerformed(ActionEvent arg0) {
		Object src = arg0.getSource();

		if (src == datasourceComboBox) {
			Object sel_obj = datasourceComboBox.getSelectedItem();

			if (sel_obj == null) {
				return;
			}

			if (!(sel_obj instanceof ConnectionWithParams)) {
				return;
			}

			selectedDataSource = (ConnectionWithParams) sel_obj;
			setEmptyPanels();

			if (selectedDataSource.isNull()) {
				// empty conn selected
				try {
					updateTableList(selectedDataSource);
					checkFinishable();
				} catch (SQLException e) { }
				return;
			}
			
			if (!selectedDataSource.isConnected()) {
				if (!tryToConnect(selectedDataSource)) {
					datasourceComboBox.setSelectedIndex(0);

					return;
				}
			}
			
			try {
				updateTableList(selectedDataSource);
				checkFinishable();
			} catch (SQLException e) {
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "Error_getting_table_names") + ": " + e.getMessage(),
						PluginServices.getText(this, "Error"),
						JOptionPane.ERROR_MESSAGE);
				setEmptyPanels();
				return;
			}


			if (tablesList.getModel().getSize() == 0) {
				JOptionPane.showMessageDialog(
						this,
						PluginServices.getText(this, "No_tables_listed_maybe_schema_case_issue"),
						PluginServices.getText(this, "Error"),
						JOptionPane.WARNING_MESSAGE);
				return;
			}

					
			return;
		}

		if (src == dbButton) {
			ConnectionWithParams sel = addNewConnection();

			if (sel != null) {
				loadDBDatasourcesCombo();
				getDatasourceComboBox().setSelectedItem(sel);
			}
		}
	}

	private void setEmptyPanels() {
		((DefaultListModel) getTablesList().getModel()).clear();
		getTablesScrollPane().updateUI();
		
		((TitledBorder) getFieldsPanel().getBorder()).setTitle(
				PluginServices.getText(this, "Fields_for_table") + ": -");
		((DefaultListModel) getFieldsList().getModel()).clear();
		getFieldsScrollPane().updateUI();
		
		checkFinishable();
	}

	private boolean tryToConnect(ConnectionWithParams _cwp) {
		JPasswordDlg dlg = new JPasswordDlg();
		dlg.setLocationRelativeTo((Component)PluginServices.getMainFrame());
		String strMessage = PluginServices.getText(this, "conectar_jdbc");
		String strPassword = PluginServices.getText(this, "password");
		dlg.setMessage(strMessage + " [" + _cwp.getDrvName() + ", " +
				_cwp.getHost() + ", " + _cwp.getPort() + ", " + _cwp.getDb() +
				", " + _cwp.getUser() + "]. " + strPassword + "?");

		dlg.setVisible(true);

		String clave = dlg.getPassword();

		if (clave == null) {
			return false;
		}

		try {
			_cwp.connect(clave);
		}
		catch (DBException e) {
			showConnectionErrorMessage(e.getMessage());

			return false;
		}

		return true;
	}

	private void updateTableList(ConnectionWithParams src) throws SQLException {
		if (src.isNull()) {
			getTablesList().setModel(new DefaultListModel());
			getTablesScrollPane().setViewportView(tablesList);
			getTablesScrollPane().updateUI();

			return;
		}

		conex = src.getConnection();
		ConnectionJDBC jdbc_conn = null;
		DefaultListModel lmodel = new DefaultListModel();

		if (conex instanceof ConnectionJDBC) {
			jdbc_conn = (ConnectionJDBC) conex; 
		} else {
			getTablesList().setModel(lmodel);
			getTablesScrollPane().setViewportView(tablesList);
			getTablesScrollPane().updateUI();
			throw new SQLException("Unknown connection type: " + conex.getClass().getName());
		}
		
		// FJP: We don't want system tables, but linked tables yes
		String[] ret_types = {"TABLE", "VIEW", "SYNONYM" /* odbc */ }; // SYSTEM
		ResultSet rs = null;

		try {
			
//		    DatabaseMetaData dmd = jdbc_conn.getConnection().getMetaData();
//		    ResultSet rs2 = dmd.getTables(null, null, "%", null);
//		    while (rs2.next())
//		        System.out.println(rs2.getString(3) + " " + rs2.getString(4));
//		    rs2 = dmd.getTables(null, null, null, null);
//		    int colcnt = rs2.getMetaData().getColumnCount();
//		    while (rs2.next()) {
//		    	for (int i=0; i<colcnt; i++) {
//		    		System.out.println(rs2.getMetaData().getColumnName(i+1) + " : " + rs2.getString(i+1));
//		    	}
//		    }
			
			rs = jdbc_conn.getConnection().getMetaData().getTables(
					null,
					src.getSchema().length() == 0 ? null : src.getSchema(),
					null,
					ret_types);
		} catch (SQLException e1) {
			throw e1;
		}
		
		String _n = "";
		String _t = "";
		String _s = "";
		
		while (rs.next()) {
			_s = rs.getString(2);
			_n = rs.getString(3);
			_n = (_s == null || _s.length()==0) ? _n : _s + "." + _n; 
			_t = rs.getString(4);
			lmodel.addElement( new TablesListItemSimple(_n, _t, src.getConnection()) );
		}
		rs.close();
		
		getTablesList().setModel(lmodel);
		getTablesScrollPane().setViewportView(tablesList);
		getTablesScrollPane().updateUI();
		
		
	}

	private boolean validFormSettings() {
		
		int count = tablesList.getModel().getSize();
		for (int i = 0; i < count; i++) {
			TablesListItemSimple item = (TablesListItemSimple) tablesList.getModel().getElementAt(i);
			if (item.isSelected()) {
				return true;
			}
		}
		return false;
	}

	public void checkFinishable() {
		boolean finishable = validFormSettings();
		parent_dialog.getJPanel().getOkButton().setEnabled(finishable);
	}

	/**
	 * This method initializes jdbcButton
	 *
	 * @return javax.swing.JButton
	 */
	 private JButton getJdbcButton() {
		if (dbButton == null) {
			dbButton = new JButton();
			dbButton.addActionListener(this);
			dbButton.setToolTipText(PluginServices.getText(this,
					"add_connection"));
			dbButton.setBounds(new java.awt.Rectangle(
					_width-3*_margin-_conn_button_width,
					3*_margin,
					_conn_button_width, _conn_button_height));

			String _file = createResourceUrl("images/jdbc.png").getFile();
			dbButton.setIcon(new ImageIcon(_file));
		}

		return dbButton;
	 }

	 private ConnectionWithParams addNewConnection() {
		 ConnectionWithParams resp = null;

	DBConnectionParamsDialog newco = new DBConnectionParamsDialog(
		new Class[] { AlphanumericDBDriver.class });
		 newco.showDialog();

		 if (newco.isOkPressed()) {
			 String _drvname = newco.getConnectionDriverName();
			 String _host = newco.getConnectionServerUrl();
			 String _port = newco.getConnectionPort();
			 String _dbname = newco.getConnectionDBName();
			 String _user = newco.getConnectionUser();
			 String _pw = newco.getConnectionPassword();
			 String _sche = newco.getConnectionSchema();
			 String _conn_usr_name = newco.getConnectionName();

			 boolean hasToBeCon = newco.hasToBeConnected();

			 try {
				 resp = SingleDBConnectionManager.instance()
				 .getConnection(_drvname,
						 _user, _pw, _conn_usr_name, _host, _port, _dbname, _sche,
						 hasToBeCon);
			 }
			 catch (DBException e) {
				 showConnectionErrorMessage(e.getMessage());

				 return null;
			 }
			 SingleVectorialDBConnectionExtension.saveAllToPersistence();
			 return resp;
		 }
		 else {
			 return null;
		 }
	 }

	 private void showConnectionErrorMessage(String _msg) {
		 String msg = (_msg.length() > 300) ? "" : (": " + _msg);
		 String title = PluginServices.getText(this, "connection_error");
		 JOptionPane.showMessageDialog(this, title + msg, title,
				 JOptionPane.ERROR_MESSAGE);
	 }

	 private java.net.URL createResourceUrl(String path) {
		 return getClass().getClassLoader().getResource(path);
	 }

	public String getDriverName() {
		if (selectedDataSource == null) {
			logger.error("CWP is NULL (?)");
			return null;
		} else {
			return selectedDataSource.getDrvName();
		}
	}

	public String getPort() {
		if (selectedDataSource == null) {
			logger.error("CWP is NULL (?)");
			return null;
		} else {
			return selectedDataSource.getPort();
		}
	}

	public String getHost() {
		if (selectedDataSource == null) {
			logger.error("CWP is NULL (?)");
			return null;
		} else {
			return selectedDataSource.getHost();
		}
	}

	public String getDataBase() {
		if (selectedDataSource == null) {
			logger.error("CWP is NULL (?)");
			return null;
		} else {
			return selectedDataSource.getDb();
		}
	}

	public String getUser() {
		if (selectedDataSource == null) {
			logger.error("CWP is NULL (?)");
			return null;
		} else {
			return selectedDataSource.getUser();
		}
	}

	public String getPassword() {
		if (selectedDataSource == null) {
			logger.error("CWP is NULL (?)");
			return null;
		} else {
			return selectedDataSource.getPw();
		}
	}

	public String getTable() {
		ListModel lm = getTablesList().getModel();
		int sz = lm.getSize();
		TablesListItemSimple item = null;
		for (int i=0; i<sz; i++) {
			if (lm.getElementAt(i) instanceof TablesListItemSimple) {
				item = (TablesListItemSimple) lm.getElementAt(i);
				if (item.isSelected()) {
					return quoteTableName(item.getTableName());
				}
			}
		}
		logger.error("NO check box selected in table list (?)");
		return "";
	}

	public boolean setActingTable(TablesListItemSimple acttab) {

		String drvn = selectedDataSource.getDrvName();
		IConnection iconn = selectedDataSource.getConnection();
		
		Driver drv = SingleDBConnectionManager.getInstanceFromName(drvn);
		String tn = "";
		if (drv instanceof AlphanumericDBDriver && iconn instanceof ConnectionJDBC) {
			ConnectionJDBC conn = (ConnectionJDBC) iconn;
			AlphanumericDBDriver alpha_drv = (AlphanumericDBDriver) drv;
			
			tn = quoteTableName(acttab.getTableName());

			String _sql = "SELECT * FROM " + tn + " WHERE (0=1)";
			FieldDescription[] fd = null;
			try {
				alpha_drv.open(conn.getConnection(), _sql);
				fd = alpha_drv.getTableDefinition().getFieldsDesc();
			} catch (Exception ex) {
				logger.error("While getting fields: " + ex.getMessage());
				JOptionPane.showMessageDialog(this,
						PluginServices.getText(this, "Error_while_getting_table_fields") + ": " + ex.getMessage(),
						PluginServices.getText(this, "Error"),
						JOptionPane.ERROR_MESSAGE);
				return false;
			}
			
			int fld_cnt = fd.length;
			((DefaultListModel) getFieldsList().getModel()).clear();
			String ft = "Other";
			for (int i=0; i<fld_cnt; i++) {
				String fn = fd[i].getFieldName();
				
				try {
					ft = typeToString(fd[i].getFieldType());
				} catch (Exception ex) {
					ft = "Other";
				}
				
				((DefaultListModel) getFieldsList().getModel()).addElement(
						new FieldListItem(fn,ft));
			}
			this.getFieldsScrollPane().setViewportView(getFieldsList());
			getFieldsScrollPane().updateUI();
			
			((TitledBorder) getFieldsPanel().getBorder()).setTitle(
					PluginServices.getText(this, "Fields_for_table") + ": " + tn);
			
			try {
				alpha_drv.close();
			} catch (Exception ex) {
				logger.error("Unreported error while closing temp driver: " + ex.getMessage());
			}
			
			alpha_drv = null;
			return true;
		} else {
			logger.error("Unexpacted driver or conn class, drv : " + drv.getClass().getName());
			logger.error("Unexpacted driver or conn class, conn: " + iconn.getClass().getName());
			return false;
		}
		
	}
	
		
	public static String typeToString(int sqlType) {
		switch (sqlType) {
		case Types.BIGINT:
		case Types.INTEGER:
		case Types.SMALLINT:
		case Types.TINYINT:
			return "Integer";

		case Types.NUMERIC:
			return "Numeric";

		case Types.BIT:
		case Types.BOOLEAN:
			return "Boolean";

		case Types.CHAR:
		case Types.VARCHAR:
		case Types.LONGVARCHAR:
			return "String";

		case Types.DATE:
			return "Date";

		case Types.FLOAT:
		case Types.DOUBLE:
		case Types.DECIMAL:
		case Types.REAL:
			return "Double";

		case Types.BINARY:
		case Types.VARBINARY:
		case Types.LONGVARBINARY:
			return "Binary";

		case Types.TIMESTAMP:
			return "Timestamp";

		case Types.TIME:
			return "Time";

		case Types.OTHER:
		default:
			throw new RuntimeException("Type not recognized: " + sqlType);
		}
	}

	private JList getFieldsList() {
		if (_fieldsList == null) {
			_fieldsList = new JList();
			_fieldsList.setSelectionModel(new NoSelectionModel());
			DefaultListModel lmodel = new DefaultListModel();
			_fieldsList.setModel(lmodel);
		}
		return _fieldsList;
	}
	
	private class FieldListItem extends JLabel {
		
		private String name = "";
		private String type = "";
		
		public FieldListItem(String _n, String _t) {
			name = _n;
			type = _t;
		}
		
		public String toString() {
			if (name == null || name.length() == 0) {
				return "[-]";
			} else {
				return (type == null || type.length() == 0) ? name : (name+" [" + type + "]");
			}
		}
		
	}
	
	private class NoSelectionModel extends DefaultListSelectionModel {
		public NoSelectionModel() {
		}
		public void addSelectionInterval(int a, int b) {  }
		public boolean isSelectedIndex(int a) { return false; }
		public boolean isSelectionEmpty() { return true; }
	}

	public void clearFieldsList() {
		((DefaultListModel) getFieldsList().getModel()).clear();
		getFieldsScrollPane().updateUI();
	}

	
	public static String quoteTableName(String _tn) {

		String[] pointspl = _tn.split("\\.");
		String resp = "";
		for (int i=0; i<pointspl.length; i++) {
			if (i!=0) {
				resp = resp + ".";
			}
			resp = resp + "\"" + pointspl[i] + "\"";
		}
		return resp;
	}
    
    

} 


// [eiel-gestion-conexiones]