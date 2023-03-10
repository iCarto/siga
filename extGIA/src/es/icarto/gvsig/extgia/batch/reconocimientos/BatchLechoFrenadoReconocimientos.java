package es.icarto.gvsig.extgia.batch.reconocimientos;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchLechoFrenadoReconocimientos extends BatchAbstractSubForm {

    public BatchLechoFrenadoReconocimientos(String formFile, String dbTableName) {
	super(dbTableName);
    }

    @Override
    public String getLayerName() {
	return DBFieldNames.LECHO_FRENADO_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return DBFieldNames.ID_LECHO_FRENADO;
    }
}
