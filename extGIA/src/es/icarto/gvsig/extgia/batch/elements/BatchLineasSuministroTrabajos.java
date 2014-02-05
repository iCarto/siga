package es.icarto.gvsig.extgia.batch.elements;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchLineasSuministroTrabajos extends BatchAbstractSubForm {

    public BatchLineasSuministroTrabajos(String formFile, String dbTableName) {
	super(formFile, dbTableName);
	// TODO Auto-generated constructor stub
    }

    @Override
    public String getLayerName() {
	return DBFieldNames.LINEAS_SUMINISTRO_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return DBFieldNames.ID_LINEAS_SUMINISTRO;
    }

    @Override
    public String getXMLPath() {
	return this.getClass().getClassLoader()
		.getResource("rules/lineas_suministro_trabajos_metadata.xml")
		.getPath();
    }

}