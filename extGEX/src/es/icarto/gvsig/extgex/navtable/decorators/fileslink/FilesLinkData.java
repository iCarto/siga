package es.icarto.gvsig.extgex.navtable.decorators.fileslink;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.extgex.forms.expropiations.ExpropiationsLayerResolver;
import es.icarto.gvsig.extgex.preferences.DBNames;

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

    public FLyrVect getLayer() {
        return layer;
    }

    public String getDirectoryLayerName() {
        return layer.getName();
    }

    public String getDirectoryFieldName() {
        if (ExpropiationsLayerResolver.isExpropiationLayer(layer)) {
            return DBNames.FIELD_IDFINCA_FINCAS;
        } else {
            return DBNames.FIELD_IDREVERSION_REVERSIONES;
        }
    }
}
