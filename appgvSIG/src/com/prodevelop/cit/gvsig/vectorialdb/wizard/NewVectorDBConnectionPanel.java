package com.prodevelop.cit.gvsig.vectorialdb.wizard;


import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;


import org.apache.log4j.Logger;


import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.SingleVectorialDBConnectionExtension;
import com.iver.cit.gvsig.fmap.drivers.ConnectionJDBC;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.IVectorialDatabaseDriver;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.utiles.swing.JPasswordDlg;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.DBConnectionParamsDialog;

// import es.prodevelop.cit.gvsig.fmap.drivers.jdbc.oracle.OracleSpatialDriver;


import jwizardcomponent.JWizardComponents;
import jwizardcomponent.JWizardPanel;


public class NewVectorDBConnectionPanel extends JWizardPanel
implements ItemListener, PropertyChangeListener, ActionListener, KeyListener {
	

	private static int NEW_TABLE_NAME_INDEX = 0;
	private static Logger logger = Logger.getLogger(NewVectorDBConnectionPanel.class.getName());
	
	private String drvName = "";
	private int idMaxLen = 10;

	
	public NewVectorDBConnectionPanel(
			JWizardComponents wizardComponents,
			String drv_name,
			int id_max_len) {
		
		super(wizardComponents);
		drvName = drv_name;
		idMaxLen = id_max_len;
		wizardComponents.addPropertyChangeListener(this);
		initialize();
	}

	private JLabel tableNameLabel = null;
	private JTextField tableNameField = null;

	private JLabel chooseConnLabel = null;
	private JComboBox datasourceComboBox;
	private ConnectionWithParams theConnWithParams = null;
	private JButton dbButton;
    private JLabel schemaLB;
    private JComboBox schemaCB;

	/**
	 * This method initializes this
	 *
	 */
	private void initialize() {
		
		datasourceComboBox = getDatasourceComboBox();
		loadVectorialDBDatasourcesCombo();
		
		chooseConnLabel = new JLabel();
	chooseConnLabel.setText(PluginServices.getText(this,
		"choose_connection") + ":");
	chooseConnLabel.setBounds(new java.awt.Rectangle(15, 10, 300, 20));
		dbButton = getJdbcButton();
        
        this.setLayout(null);
        this.setSize(new java.awt.Dimension(358,263));
        
        this.add(getTableNameLabel(), null);
        this.add(getTableNameField(), null);
        
        this.add(chooseConnLabel, null);
        this.add(datasourceComboBox, null);
        this.add(dbButton, null);
        
	this.add(getSchemaLB(), null);
	this.add(getSchemaCB(), null);

    	this.updateFinishButton();
    	getTableNameField().setText(getNewTableName());
	}

    private JLabel getSchemaLB() {
	if (schemaLB == null) {
	schemaLB = new JLabel(PluginServices.getText(this, "schema") + ":");
	    schemaLB.setBounds(new java.awt.Rectangle(15, 70, 300, 20));
	}
	return schemaLB;
    }

    private JComboBox getSchemaCB() {
	if (schemaCB == null) {
	schemaCB = new JComboBox();
	    schemaCB.setBounds(new java.awt.Rectangle(15, 95, 300, 20));
	loadAvaliableSchemas();
	}
	return schemaCB;
    }

    private void loadAvaliableSchemas() {

	if ((theConnWithParams == null) || (!theConnWithParams.isConnected())) {
	    return;
	}

	try {
	    schemaCB.removeAllItems();
	    ResultSet rs = ((ConnectionJDBC) theConnWithParams.getConnection())
		    .getConnection().getMetaData().getSchemas();
	    while (rs.next()) {
		schemaCB.addItem(rs.getString("TABLE_SCHEM"));
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

	private String getNewTableName() {
		String resp = "GVSIG_" + NEW_TABLE_NAME_INDEX;
		NEW_TABLE_NAME_INDEX++;
		return resp;
	}

	private JTextField getTableNameField() {

		if (tableNameField == null) {
			tableNameField = new JTextField();
	    tableNameField.setBounds(new java.awt.Rectangle(15, 155, 300, 20));
			tableNameField.addKeyListener(this);
		}
		return tableNameField;

	}

	private JLabel getTableNameLabel() {
		
		if (tableNameLabel == null) {
			tableNameLabel = new JLabel();
			tableNameLabel.setText(PluginServices.getText(this, "Tabla") + ":");
	    tableNameLabel
.setBounds(new java.awt.Rectangle(15, 130, 300, 20));
		}
		return tableNameLabel;
	}

	public ConnectionWithParams getConnectionWithParams() {
		return theConnWithParams;
	}
	
	public String getTableName() {
		return getTableNameField().getText().toUpperCase();
	}

    private JComboBox getDatasourceComboBox() {
        if (datasourceComboBox == null) {
            datasourceComboBox = new JComboBox();
	    datasourceComboBox
		    .setBounds(new java.awt.Rectangle(15, 35, 300, 20));
            datasourceComboBox.addItemListener(this);
        }

        return datasourceComboBox;
    }	
    
    private void loadVectorialDBDatasourcesCombo() {
    	
        getDatasourceComboBox().removeAllItems();
        getDatasourceComboBox().addItem(new ConnectionWithParams());
        ConnectionWithParams[] conn =
        	SingleDBConnectionManager.instance().getAllConnections();

        if (conn == null) {
            return;
        }

        for (int i = 0; i < conn.length; i++) {
        	if (conn[i].getDrvName().compareToIgnoreCase(drvName) == 0) {
        		getDatasourceComboBox().addItem(conn[i]);
        	}
        }

    }

	public void itemStateChanged(ItemEvent e) {
		
		Object src = e.getSource();
		if (src == datasourceComboBox) {
			
            Object sel_obj = datasourceComboBox.getSelectedItem();

            if (sel_obj == null) {
            	getWizardComponents().getFinishButton().setEnabled(false);
            	theConnWithParams = null;
            	this.updateFinishButton();
                return;
            }

            if (!(sel_obj instanceof ConnectionWithParams)) {
            	getWizardComponents().getFinishButton().setEnabled(false);
            	theConnWithParams = null;
            	this.updateFinishButton();
                return;
            }

            ConnectionWithParams cwp = (ConnectionWithParams) sel_obj; 

            if (cwp.isNull()) {
            	getWizardComponents().getFinishButton().setEnabled(false);
            	theConnWithParams = null;
            	this.updateFinishButton();
                return;
            }

            if (!cwp.isConnected()) {
                if (!tryToConnect(cwp)) {
                	getWizardComponents().getFinishButton().setEnabled(false);
                	theConnWithParams = null;
                	datasourceComboBox.setSelectedIndex(0);
                	this.updateFinishButton();
                    return;
                }
            }

            theConnWithParams = cwp;
	    loadAvaliableSchemas();
        	this.updateFinishButton();
            datasourceComboBox.repaint();
		}
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
    
    private void showConnectionErrorMessage(String _msg) {
        String msg = (_msg.length() > 300) ? "" : (": " + _msg);
        String title = PluginServices.getText(this, "connection_error");
        JOptionPane.showMessageDialog(this, title + msg, title,
            JOptionPane.ERROR_MESSAGE);
    }

	public void propertyChange(PropertyChangeEvent evt) {
		
		if (evt.getPropertyName().compareToIgnoreCase(JWizardComponents.CURRENT_PANEL_PROPERTY) == 0) {
			
			if (evt.getNewValue() == this) {
				this.updateFinishButton();
			}
			
		}
		
	}    
	
	
    private ConnectionWithParams addNewConnection() {
        ConnectionWithParams resp = null;

	DBConnectionParamsDialog newco = new DBConnectionParamsDialog(
		new Class[] { IVectorialDatabaseDriver.class });
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
	    dbButton.setBounds(new java.awt.Rectangle(320, 32, 26, 21));
            String _file = createResourceUrl("images/jdbc.png").getFile();
            dbButton.setIcon(new ImageIcon(_file));
        }

        return dbButton;
    }
    
    private java.net.URL createResourceUrl(String path) {
        return getClass().getClassLoader().getResource(path);
    }

	public void actionPerformed(ActionEvent e) {
		
		Object src = e.getSource();
		
        if (src == dbButton) {
            ConnectionWithParams sel = addNewConnection();

            if (sel != null) {
                loadVectorialDBDatasourcesCombo();
                getDatasourceComboBox().setSelectedItem(sel);
            }
        }
		
	}

	public void keyPressed(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}

	public void keyReleased(KeyEvent e) {
		updateFinishButton();
	}

	public void keyTyped(KeyEvent e) {
	}    
	
	private void updateFinishButton() {
	if ((theConnWithParams != null) && (theConnWithParams.isConnected())
		&& (schemaCB.getSelectedItem() != null)
		&& (schemaCB.getSelectedItem().toString().length() > 0)) {
		String aux_table_name = getTableName(); 
		boolean active = (theConnWithParams != null) && validTableName(aux_table_name);
		getWizardComponents().getFinishButton().setEnabled(active);
	}
	}

	private boolean validTableName(String str) {

		int len = str.length();

		if ((len == 0) || (len > (idMaxLen - 3))) {
			return false;
		}
		
		if (!validInitialChar(str.substring(0, 1))) {
			return false;
		}
		
		String onechar = "";
		for (int i=1; i<len; i++) {
			onechar = str.substring(i, i+1);
			if (!validChar(onechar)) return false;
		}
		return true;
	}

	private static String[] VALID_CHAR = {
		"_", "A", "B", "C", "D", "E", "F", "G", "H", "I",
		"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
		"T", "U", "V", "W", "X", "Y", "Z", "1", "2", "3",
		"4", "5", "6", "7", "8", "9", "0"
		};
	
	private static String[] VALID_INITIAL_CHAR = {
		"_", "A", "B", "C", "D", "E", "F", "G", "H", "I",
		"J", "K", "L", "M", "N", "O", "P", "Q", "R", "S",
		"T", "U", "V", "W", "X", "Y", "Z"
		};
	
	
	private boolean validChar(String onechar) {
		int len = VALID_CHAR.length;
		for (int i=0; i<len; i++) {
			if (onechar.compareTo(VALID_CHAR[i]) == 0) return true;
		}
		return false;
	}

	private boolean validInitialChar(String onechar) {
		int len = VALID_INITIAL_CHAR.length;
		for (int i=0; i<len; i++) {
			if (onechar.compareTo(VALID_INITIAL_CHAR[i]) == 0) return true;
		}
		return false;
	}


}

// [eiel-gestion-conexiones]