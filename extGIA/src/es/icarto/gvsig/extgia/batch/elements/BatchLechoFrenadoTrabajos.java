package es.icarto.gvsig.extgia.batch.elements;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchLechoFrenadoTrabajos extends BatchAbstractSubForm {

    public BatchLechoFrenadoTrabajos(String formFile, String dbTableName) {
	super(formFile, dbTableName);
	// TODO Auto-generated constructor stub
    }

    @Override
    public String getLayerName() {
	return DBFieldNames.LECHO_FRENADO_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return DBFieldNames.ID_LECHO_FRENADO;
    }

    @Override
    public String getXMLPath() {
	return this.getClass().getClassLoader()
		.getResource("rules/lecho_frenado_trabajos_metadata.xml")
		.getPath();
    }

}
