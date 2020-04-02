package es.icarto.gvsig.siga.models;

import javax.swing.table.DefaultTableModel;

import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public abstract class InfoEmpresa {

    protected DefaultTableModel result;

    public InfoEmpresa() {
        if (!DBSession.isActive()) {
            result = new DefaultTableModel();
        }
        infoFromDB();
    }

    public abstract void infoFromDB();

    public abstract int getFieldTramoIndex();

    public String getTitle(Object tramo) {
        String itemTramo = getItemTramo(tramo);
        for (int i = 0; i < result.getRowCount(); i++) {
            if (result.getValueAt(i, getFieldTramoIndex()).equals(itemTramo)) {
                return result.getValueAt(i, 3).toString();
            }
        }
        return PreferencesPage.APP_NAME;
    }

    public String getSubtitle(Object tramo) {
        String itemTramo = getItemTramo(tramo);
        for (int i = 0; i < result.getRowCount(); i++) {
            if (result.getValueAt(i, getFieldTramoIndex()).equals(itemTramo)) {
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
            if (result.getValueAt(i, getFieldTramoIndex()).equals(itemTramo)) {
                final String logoName = result.getValueAt(i, 2).toString();
                return PreferencesPage.LOGO_PATH + logoName;
            }
        }
        return PreferencesPage.SIGA_REPORT_LOGO;
    }

    protected String getItemTramo(Object tramo) {
        if (tramo instanceof KeyValue) {
            return ((KeyValue) tramo).getValue();
        }
        return (tramo == null) ? "" : tramo.toString();
    }

    public String getCompany(Object tramo) {
        String itemTramo = getItemTramo(tramo);
        for (int i = 0; i < result.getRowCount(); i++) {
            if (result.getValueAt(i, getFieldTramoIndex()).equals(itemTramo)) {
                return result.getValueAt(i, 5).toString();
            }
        }
        return null;
    }

    public String getReportName(Object tramo) {
        String itemTramo = getItemTramo(tramo);
        for (int i = 0; i < result.getRowCount(); i++) {
            if (result.getValueAt(i, getFieldTramoIndex()).equals(itemTramo)) {
                return result.getValueAt(i, 6).toString();
            }
        }
        return null;
    }
}
