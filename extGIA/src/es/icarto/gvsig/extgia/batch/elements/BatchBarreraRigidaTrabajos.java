package es.icarto.gvsig.extgia.batch.elements;

import java.util.HashMap;
import java.util.Map;

import es.icarto.gvsig.extgia.batch.BatchVegetationTrabajosAbstractSubForm;
import es.icarto.gvsig.extgia.forms.barrera_rigida.CalculateBarreraRigidaTrabajosLongitud;
import es.icarto.gvsig.extgia.forms.barrera_rigida.CalculateBarreraRigidaTrabajosMedicionUltimoTrabajo;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class BatchBarreraRigidaTrabajos extends BatchVegetationTrabajosAbstractSubForm {

    public BatchBarreraRigidaTrabajos(Elements parentElement) {
	super(parentElement);
    }

    @Override
    public String[] getColumnNames() {
	String[] columnNames = {"ID Barrera R�gida",
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
    public void getForeignValues(HashMap<String, String> values, String idValue) {
	Map<String, String> primaryKey = new HashMap<String, String>();
	primaryKey.put(getIdFieldName(), idValue);

	values.put(DBFieldNames.LONGITUD, new CalculateBarreraRigidaTrabajosLongitud(
		primaryKey).getForeignValue().getValue());
	values.put(DBFieldNames.MEDICION_ULTIMO_TRABAJO, new CalculateBarreraRigidaTrabajosMedicionUltimoTrabajo(
		primaryKey, values.get(DBFieldNames.UNIDAD)).getForeignValue().getValue());

    }

}
