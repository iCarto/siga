package es.icarto.gvsig.extgia.batch.elements;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

import es.icarto.gvsig.extgia.batch.BatchVegetationTrabajosAbstractSubForm;
import es.icarto.gvsig.extgia.forms.barrera_rigida.CalculateBarreraRigidaTrabajosMedicionUltimoTrabajo;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchBarreraRigidaTrabajos extends BatchVegetationTrabajosAbstractSubForm {

    public BatchBarreraRigidaTrabajos(String formFile, String dbTableName) {
	super(formFile, dbTableName);
    }

    @Override
    public String getLayerName() {
	return DBFieldNames.BARRERA_RIGIDA_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return DBFieldNames.ID_BARRERA_RIGIDA;
    }

    @Override
    public String getDbTableName() {
	return "barrera_rigida_trabajos";
    }

    @Override
    protected String getBasicName() {
	return "vegetation_trabajos";
    }

    @Override
    public String[] getColumnNames() {
	String[] columnNames = {"ID Barrera R�gida ",
		"Fecha",
		"Unidad",
		"Longitud",
		"Ancho",
		"Medici�n",
		"Medici�n �ltimo trabajo",
		"Observaciones"
	};
	return columnNames;
    }

    @Override
    public String[] getColumnDbNames() {
	String[] columnNames = {"id_barrera_rigida",
		"fecha",
		"unidad",
		"longitud",
		"ancho",
		"medicion",
		"medicion_ultimo_trabajo",
		"observaciones"
	};
	return columnNames;
    }

    @Override
    public Integer[] getColumnDbTypes() {
	Integer[] columnTypes = {Types.VARCHAR,
		Types.DATE,
		Types.VARCHAR,
		Types.INTEGER,
		Types.NUMERIC,
		Types.NUMERIC,
		Types.NUMERIC,
		Types.VARCHAR
	};
	return columnTypes;
    }

    @Override
    public void getForeignValues(HashMap<String, String> values, String idValue) {
	Map<String, String> primaryKey = new HashMap<String, String>();
	primaryKey.put(getIdFieldName(), idValue);

	values.put(DBFieldNames.MEDICION_ULTIMO_TRABAJO, new CalculateBarreraRigidaTrabajosMedicionUltimoTrabajo(
		primaryKey, values.get(DBFieldNames.UNIDAD)).getForeignValue().getValue());

    }

}
