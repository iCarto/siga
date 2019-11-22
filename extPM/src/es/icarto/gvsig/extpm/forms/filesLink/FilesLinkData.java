package es.icarto.gvsig.extpm.forms.filesLink;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

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

    public String getDirectoryLayerName() {
	return layer.getName();
    }

    public String getDirectoryFieldName() {
	return "exp_id";

    }
}
