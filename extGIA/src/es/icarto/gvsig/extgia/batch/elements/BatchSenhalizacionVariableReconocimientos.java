package es.icarto.gvsig.extgia.batch.elements;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchSenhalizacionVariableReconocimientos extends
BatchAbstractSubForm {

    public BatchSenhalizacionVariableReconocimientos(String formFile,
	    String dbTableName) {
	super(formFile, dbTableName);
	// TODO Auto-generated constructor stub
    }

    @Override
    public String getLayerName() {
	return DBFieldNames.SENHALIZACION_VARIABLE_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return DBFieldNames.ID_SENHAL_VARIABLE;
    }

    @Override
    public String getXMLPath() {
	return this.getClass().getClassLoader()
		.getResource("rules/senhalizacion_variable_reconocimientos_metadata.xml")
		.getPath();
    }

}
