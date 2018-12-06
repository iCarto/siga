package es.icarto.gvsig.extgex.navtable.decorators.fileslink;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.siga.PreferencesPage;

public class FilesLinkData {

    private FLyrVect layer = null;

    public FilesLinkData(FLyrVect layer) {
	this.layer = layer;
    }

    public SelectableDataSource getRecordset() {
	try {
	    return layer.getRecordset();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    public String getBaseDirectory() {
	return PreferencesPage.getBaseDirectory();
    }

    public String getDirectoryLayerName() {
	return layer.getName();
    }

    public String getDirectoryFieldName() {
	if (layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME) || layer.getName().equalsIgnoreCase(FormExpropiations.TOCNAME_AMPLIACION)) {
	    return DBNames.FIELD_IDFINCA_FINCAS;
	}else {
	    return DBNames.FIELD_IDREVERSION_REVERSIONES;
	}
    }
}
