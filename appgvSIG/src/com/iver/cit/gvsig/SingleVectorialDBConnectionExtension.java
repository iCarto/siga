/* gvSIG. Sistema de Información Geográfica de la Generalitat Valenciana
 *
 * Copyright (C) 2006 Prodevelop and Generalitat Valenciana.
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
 *   Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ibáñez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *   +34 963862235
 *   gvsig@gva.es
 *   www.gvsig.gva.es
 *
 *    or
 *
 *   Prodevelop Integración de Tecnologías SL
 *   Conde Salvatierra de Álava , 34-10
 *   46004 Valencia
 *   Spain
 *
 *   +34 963 510 612
 *   +34 963 510 968
 *   gis@prodevelop.es
 *   http://www.prodevelop.es
 */
package com.iver.cit.gvsig;

import java.util.ArrayList;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.plugins.Extension;
import com.iver.cit.gvsig.fmap.drivers.DBException;
import com.iver.cit.gvsig.fmap.drivers.db.utils.ConnectionWithParams;
import com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager;
import com.iver.cit.gvsig.vectorialdb.ConnectionSettings;
import com.iver.utiles.NotExistInXMLEntity;
import com.iver.utiles.XMLEntity;
import com.prodevelop.cit.gvsig.vectorialdb.wizard.DBConnectionManagerDialog;

/**
 * This extension allows the user to access the single connection manager dialog
 *
 * @see com.iver.cit.gvsig.fmap.drivers.db.utils.SingleDBConnectionManager
 *
 * @author jldominguez
 *
 */
public class SingleVectorialDBConnectionExtension extends Extension {
    private static Logger logger = Logger.getLogger(SingleVectorialDBConnectionExtension.class.getName());

    /**
     * This method simply loads the connections stored in gvSIG's persistence file
     * as closed connections
     */
    @Override
    public void initialize() {
        loadPersistenceConnections();
        registerIcons();
    }

    private void registerIcons() {
        PluginServices.getIconTheme().register("conn-image",
                this.getClass().getClassLoader().getResource("images/conn.png"));
        PluginServices.getIconTheme().register("disconn-image",
                this.getClass().getClassLoader().getResource("images/disconn.png"));

    }

    @Override
    public void execute(String actionCommand) {
        DBConnectionManagerDialog dlg = new DBConnectionManagerDialog();
        dlg.showDialog();
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public boolean isVisible() {
        return true;
    }

    public static void saveAllToPersistence() {
        SingleVectorialDBConnectionExtension ext = new SingleVectorialDBConnectionExtension();
        XMLEntity xml = PluginServices.getPluginServices(ext).getPersistentXML();
        xml.remove("db-connections");

        ConnectionWithParams[] all = SingleDBConnectionManager.instance().getAllConnections();

        if (all == null) {
            PluginServices.getPluginServices(ext).setPersistentXML(xml);
            return;
        }

        for (int i = 0; i < all.length; i++) {
            addToPersistence(all[i], ext);
        }
    }

    private static void addToPersistence(ConnectionWithParams cwp, Extension ext) {
        if (cwp == null) {
            return;
        }

        ConnectionSettings _cs = new ConnectionSettings();

        _cs.setHost(cwp.getHost());
        _cs.setPort(cwp.getPort());
        _cs.setDb(cwp.getDb());
        _cs.setDriver(cwp.getDrvName());
        _cs.setUser(cwp.getUser());
        _cs.setName(cwp.getName());
        _cs.setSchema(cwp.getSchema());

        String newstr = _cs.toString();

        XMLEntity xml = PluginServices.getPluginServices(ext).getPersistentXML();

        String[] old = null;

        if (xml.contains("db-connections")) {
            old = xml.getStringArrayProperty("db-connections");
        }

        ArrayList<String> oldl = stringArrayToArrayList(old);
        oldl.add(newstr);

        String[] newarr = oldl.toArray(new String[0]);

        xml.remove("db-connections");
        xml.putProperty("db-connections", newarr);
        PluginServices.getPluginServices(ext).setPersistentXML(xml);
    }

    private static ArrayList<String> stringArrayToArrayList(String[] arr) {
        ArrayList<String> resp = new ArrayList<String>();

        if ((arr != null) && (arr.length > 0)) {
            for (int i = 0; i < arr.length; i++) {
                resp.add(arr[i]);
            }
        }

        return resp;
    }

    private void loadPersistenceConnections() {
        XMLEntity xml = PluginServices.getPluginServices(this).getPersistentXML();

        if (xml == null) {
            xml = new XMLEntity();
        }

        if (!xml.contains("db-connections")) {
            String[] servers = new String[0];
            xml.putProperty("db-connections", servers);
        }

        // add drivers to connection manager
        String[] servers = null;

        try {
            servers = xml.getStringArrayProperty("db-connections");
        } catch (NotExistInXMLEntity e) {
            System.err.println("Error while getting projects db-connections: " + e.getMessage());

            return;
        }

        for (int i = 0; i < servers.length; i++) {
            ConnectionSettings cs = new ConnectionSettings();
            boolean params_ok = true;
            try {
                cs.setFromString(servers[i]);

            } catch (Exception ex) {
                logger.error("Found misconfigured connection: " + servers[i]);
                params_ok = false;
            }
            if (params_ok) {
                addDisconnected(cs);
            }
        }

        PluginServices ps = PluginServices.getPluginServices("com.iver.cit.gvsig.jdbc_spatial");
        if (ps == null) {
            return;
        }
        XMLEntity xmlJDBC = ps.getPersistentXML();

        if (xmlJDBC == null || !xmlJDBC.contains("jdbc-connections")) {
            return;
        }

        // add drivers to connection manager
        String[] serversOld = null;

        try {
            serversOld = xmlJDBC.getStringArrayProperty("jdbc-connections");
        } catch (NotExistInXMLEntity e) {
            System.err.println("Error while getting projects jdbc-connections: " + e.getMessage());

            return;
        }

        for (int i = 0; i < serversOld.length; i++) {
            ConnectionSettings cs = new ConnectionSettings();
            boolean params_ok = true;
            try {
                cs.setFromString(serversOld[i]);
                cs.setDb(cs.getDb().toLowerCase());
            } catch (Exception ex) {
                logger.error("Found misconfigured connection: " + serversOld[i]);
                params_ok = false;
            }
            if (params_ok) {
                addDisconnected(cs);
            }
        }

    }

    private void addDisconnected(ConnectionSettings _cs) {
        try {
            SingleDBConnectionManager.instance().getConnection(_cs.getDriver(), _cs.getUser(), null, _cs.getName(),
                    _cs.getHost(), _cs.getPort(), _cs.getDb(), _cs.getSchema(), false);
        } catch (DBException e) {
            System.err.println("While getting connection: " + e.getMessage());
            showConnectionErrorMessage(e.getMessage());
        }
    }

    private void showConnectionErrorMessage(String _msg) {
        String msg = (_msg.length() > 300) ? "" : (": " + _msg);
        String title = PluginServices.getText(this, "connection_error");
        JOptionPane.showMessageDialog(null, title + msg, title, JOptionPane.ERROR_MESSAGE);
    }

    @Override
    public void terminate() {
        saveAllToPersistence();
        SingleDBConnectionManager.instance().closeAllBeforeTerminate();
    }
}