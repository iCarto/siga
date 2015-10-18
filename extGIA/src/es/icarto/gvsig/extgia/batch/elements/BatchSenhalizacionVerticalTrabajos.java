package es.icarto.gvsig.extgia.batch.elements;

import java.sql.Types;
import java.util.HashMap;

import es.icarto.gvsig.extgia.batch.BatchVegetationTrabajosAbstractSubForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchSenhalizacionVerticalTrabajos extends BatchVegetationTrabajosAbstractSubForm {

    public BatchSenhalizacionVerticalTrabajos(String formFile,
	    String dbTableName) {
	super(formFile, dbTableName);
    }

    @Override
    public String getLayerName() {
	return DBFieldNames.SENHALIZACION_VERTICAL_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return DBFieldNames.ID_ELEMENTO_SENHALIZACION;
    }

    @Override
    public String getDbTableName() {
	return "senhalizacion_vertical_trabajos";
    }

    @Override
    protected String getBasicName() {
	return "batch_senhalizacion_vertical_trabajos";
    }

    @Override
    public String[] getColumnNames() {
	String[] columnNames = {"ID Se�alizaci�n",
		"Fecha",
		"Unidad",
		"Medici�n",
		"Observaciones"
	};
	return columnNames;
    }

    @Override
    public String[] getColumnDbNames() {
	String[] columnNames = {"id_elemento_senhalizacion",
		"fecha",
		"unidad",
		"medicion",
		"observaciones"
	};
	return columnNames;
    }

    @Override
    public void getForeignValues(HashMap<String, String> values, String idValue) {
	// TODO Auto-generated method stub

    }

}
