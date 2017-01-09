package es.icarto.gvsig.siga.models;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class InfoEmpresa {

    private DefaultTableModel result;

    public InfoEmpresa() {
	if (!DBSession.isActive()) {
	    result = new DefaultTableModel();
	}
	infoFromDB();
    }

    public void infoFromDB() {
	ConnectionWrapper con = new ConnectionWrapper(DBSession
		.getCurrentSession().getJavaConnection());
	String query = "SELECT distinct(tr.id), tr.item, ie.report_logo, ie.title, ie.subtitle FROM audasa_extgia_dominios.tramo tr LEFT OUTER JOIN audasa_aplicaciones.info_empresa as ie ON tr.empresa = ie.id";
	result = con.execute(query);
    }

    public String getTitle(Object tramo) {
	String itemTramo = getItemTramo(tramo);
	for (int i = 0; i < result.getRowCount(); i++) {
	    if (result.getValueAt(i, 1).equals(itemTramo)) {
		return result.getValueAt(i, 3).toString();
	    }
	}
	return PreferencesPage.APP_NAME;
    }

    public String getSubtitle(Object tramo) {
	String itemTramo = getItemTramo(tramo);
	for (int i = 0; i < result.getRowCount(); i++) {
	    if (result.getValueAt(i, 1).equals(itemTramo)) {
		return result.getValueAt(i, 4).toString();
	    }
	}
	return PreferencesPage.APP_DESC;
    }

    /**
     * Absolute path to the logo
     */
    public String getReportLogo(Object tramo) {
	String itemTramo = getItemTramo(tramo);
	for (int i = 0; i < result.getRowCount(); i++) {
	    if (result.getValueAt(i, 1).equals(itemTramo)) {
		final String logoName = result.getValueAt(i, 2).toString();
		return PreferencesPage.LOGO_PATH + logoName;
	    }
	}
	return PreferencesPage.SIGA_REPORT_LOGO;
    }

    private String getItemTramo(Object tramo) {
	if (tramo instanceof KeyValue) {
	    return ((KeyValue) tramo).getValue();
	}
	return (tramo == null) ? "" : tramo.toString();
    }

}
