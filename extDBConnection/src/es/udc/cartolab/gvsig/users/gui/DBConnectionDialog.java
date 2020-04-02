/*
 * Copyright (c) 2010 - 2012. CartoLab. Fundaci�n de Intenier�a Civil de Galicia.
 *
 * This file is part of extDBConnection
 *
 * extDBConnection is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 *
 * extDBConnection is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with extDBConnection.
 * If not, see <http://www.gnu.org/licenses/>.
 */
package es.udc.cartolab.gvsig.users.gui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.InputStream;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.ProjectExtension;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.utiles.XMLEntity;
import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;
import com.jeta.forms.gui.form.GridView;

import es.udc.cartolab.gvsig.users.preferences.UsersPreferencePage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class DBConnectionDialog extends AbstractGVWindow {

    private static final String DATABASE_PROPERTY_NAME = "database";
    private static final String USER_PROPERTY_NAME = "user";
    private static final String PORT_PROPERTY_NAME = "port";
    private static final String HOST_PROPERTY_NAME = "host";
    private final static int INIT_MIN_HEIGHT = 185;
    private final static int INIT_MAX_HEIGHT = 285;

    private int minHeight;
    private int maxHeight;

    private JPanel centerPanel = null;

    private JCheckBox advCHB;
    private JTextField serverTF, userTF, passTF, dbTF, portTF;
    private JComponent advForm;
    private GridView extraInfo;

    public static final String ID_SERVERTF = "serverTF";
    public static final String ID_PORTTF = "portTF";
    public static final String ID_USERTF = "userTF";
    public static final String ID_PASSTF = "passTF"; // javax.swing.JPasswordField
    public static final String ID_DBTF = "dbTF";
    public static final String ID_ADVF = "advancedForm";
    public static final String ID_ADVCHB = "advancedCHB";
    public static final String ID_SERVERL = "serverLabel";
    public static final String ID_PORTL = "portLabel";
    public static final String ID_USERL = "userLabel";
    public static final String ID_PASSL = "passLabel";
    public static final String ID_DBL = "dbLabel";
    FormPanel form;
    private static final Logger logger = Logger.getLogger(DBConnectionDialog.class);

    public DBConnectionDialog() {
        super(325, INIT_MIN_HEIGHT);
        setTitle(PluginServices.getText(this, "Login"));
    }

    @Override
    protected JPanel getCenterPanel() {

        if (centerPanel == null) {
            centerPanel = new JPanel();

            InputStream resourceAsStream = this.getClass().getClassLoader()
                    .getResourceAsStream("forms/dbConnection.xml");

            try {
                form = new FormPanel(resourceAsStream);
            } catch (FormException e) {
                logger.error(e.getStackTrace(), e);
                return centerPanel;
            }
            centerPanel.add(form);

            serverTF = form.getTextField(ID_SERVERTF);
            portTF = form.getTextField(ID_PORTTF);
            userTF = form.getTextField(ID_USERTF);
            passTF = form.getTextField(ID_PASSTF);
            dbTF = form.getTextField(ID_DBTF);
            advForm = (JComponent) form.getComponentByName(ID_ADVF);
            advForm.setBorder(UIManager.getBorder("TitledBorder.border"));

            advCHB = form.getCheckBox(ID_ADVCHB);
            extraInfo = (GridView) form.getComponentByName("extra_info");
            form.getButton("server_central").addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    serverTF.setText("192.168.100.182");
                    portTF.setText("5432");
                    dbTF.setText("SIGA");
                }
            });
            form.getButton("server_seixurra").addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    serverTF.setText("192.168.0.30");
                    portTF.setText("5432");
                    dbTF.setText("SIGA");
                }
            });

            showAdvancedProperties(false);
            advCHB.addActionListener(this);

            initLogo(form);

            // localization
            form.getLabel(ID_SERVERL).setText(PluginServices.getText(this, "server"));
            form.getLabel(ID_PORTL).setText(PluginServices.getText(this, "portl"));
            form.getLabel(ID_USERL).setText(PluginServices.getText(this, "user_name"));
            form.getLabel(ID_PASSL).setText(PluginServices.getText(this, "user_pass"));
            form.getLabel(ID_DBL).setText(PluginServices.getText(this, "data_base"));

            advCHB.setText(PluginServices.getText(this, "advanced_options"));

            DBSession dbs = DBSession.getCurrentSession();
            if (dbs != null) {
                serverTF.setText(dbs.getServer());
                portTF.setText(Integer.toString(dbs.getPort()));
                userTF.setText(dbs.getUserName());
                dbTF.setText(dbs.getDatabase());
                advCHB.setSelected(false);
            } else {
                fillDialogFromPluginPersistence();
            }

            passTF.requestFocusInWindow();

        }
        return centerPanel;
    }

    private void initLogo(FormPanel form) {
        if (!UsersPreferencePage.LOGO.isEmpty()) {
            File logo = new File(UsersPreferencePage.LOGO);
            if (logo.isFile()) {
                ImageComponent image = (ImageComponent) form.getComponentByName("image");
                ImageIcon icon = new ImageIcon(logo.getAbsolutePath());
                image.setIcon(icon);
            }
        }
    }

    private void fillDialogFromPluginPersistence() {
        XMLEntity xml = PluginServices.getPluginServices(this).getPersistentXML();

        if (xml.contains(HOST_PROPERTY_NAME) && xml.contains(PORT_PROPERTY_NAME)
                && xml.contains(DATABASE_PROPERTY_NAME) && xml.contains(USER_PROPERTY_NAME)) {
            serverTF.setText(xml.getStringProperty(HOST_PROPERTY_NAME));
            portTF.setText(xml.getStringProperty(PORT_PROPERTY_NAME));
            dbTF.setText(xml.getStringProperty(DATABASE_PROPERTY_NAME));
            userTF.setText(xml.getStringProperty(USER_PROPERTY_NAME));
        } else {
            showAdvancedProperties(true);
            advCHB.setSelected(true);
        }
    }

    private void saveConfig(String host, String port, String database, String schema, String user) {
        // TODO: fpuga: If in the future we will want save more than one
        // configuration this approach is not valid. Whe should store each
        // connection in a different XMLEntity and in the main XMLEntity store a
        // "lastConnectionUsed" value

        XMLEntity xml = PluginServices.getPluginServices(this).getPersistentXML();
        xml.putProperty(HOST_PROPERTY_NAME, host);
        xml.putProperty(PORT_PROPERTY_NAME, port);
        xml.putProperty(DATABASE_PROPERTY_NAME, database);
        xml.putProperty(USER_PROPERTY_NAME, user);
        PluginServices.getMDIManager().restoreCursor();
        String title = " " + String.format(PluginServices.getText(this, "connectedTitle"), user, host);
        PluginServices.getMainFrame().setTitle(title);
    }

    @Override
    protected JPanel getNorthPanel() {
        if (headerImg != null) {
            maxHeight = INIT_MAX_HEIGHT + headerImg.getIconHeight();
            minHeight = INIT_MIN_HEIGHT + headerImg.getIconHeight();
        } else {
            maxHeight = INIT_MAX_HEIGHT;
            minHeight = INIT_MIN_HEIGHT;
        }
        return super.getNorthPanel();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        super.actionPerformed(e);
        if (e.getSource() == advCHB) {
            showAdvancedProperties(advCHB.isSelected());
        }
    }

    private void showAdvancedProperties(boolean show) {
        int height;
        if (show) {
            height = maxHeight;
        } else {
            height = minHeight;
        }
        setHeight(height);
        advForm.setVisible(show);
        extraInfo.setVisible(show);
    }

    private boolean activeSession() throws DBException {

        DBSession dbs = DBSession.getCurrentSession();
        if (dbs != null) {
            if (!dbs.askSave()) {
                return false;
            }
            dbs.close();

            ProjectExtension pExt = (ProjectExtension) PluginServices.getExtension(ProjectExtension.class);
            pExt.execute("NUEVO");
        }
        return true;

    }

    @Override
    protected void onOK() {

        try {

            if (!activeSession()) {
                return;
            }

            PluginServices.getMDIManager().setWaitCursor();

            String portS = portTF.getText().trim();
            int port = Integer.parseInt(portS);
            String server = serverTF.getText().trim();
            String username = userTF.getText().trim();
            String password = passTF.getText();
            String database = dbTF.getText();

            DBSession.createConnection(server, port, database, null, username, password);

            closeWindow();

            saveConfig(server, portS, database, null, username);
            PluginServices.getMainFrame().enableControls();
        } catch (DBException e1) {
            // Login error
            e1.printStackTrace();
            PluginServices.getMDIManager().restoreCursor();
            JOptionPane.showMessageDialog(this, PluginServices.getText(this, "databaseConnectionError"),
                    PluginServices.getText(this, "connectionError"), JOptionPane.ERROR_MESSAGE);

        } catch (NumberFormatException e2) {
            PluginServices.getMDIManager().restoreCursor();
            JOptionPane.showMessageDialog(this, PluginServices.getText(this, "portError"),
                    PluginServices.getText(this, "dataError"), JOptionPane.ERROR_MESSAGE);
        } finally {
            passTF.setText("");
        }
    }

    @Override
    protected Component getDefaultFocusComponent() {
        return passTF;
    }

}
