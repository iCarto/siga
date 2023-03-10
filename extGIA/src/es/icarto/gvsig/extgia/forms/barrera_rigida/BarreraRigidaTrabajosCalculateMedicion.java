package es.icarto.gvsig.extgia.forms.barrera_rigida;

import javax.swing.JComboBox;

import es.icarto.gvsig.extgia.forms.CalculateDBForeignValueLastJob;
import es.icarto.gvsig.extgia.forms.ForeignValue;
import es.icarto.gvsig.extgia.forms.VegetationCalculateMedicion;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.IValidatableForm;

public class BarreraRigidaTrabajosCalculateMedicion extends
VegetationCalculateMedicion {

    public BarreraRigidaTrabajosCalculateMedicion(IValidatableForm form) {
	super(form, DBFieldNames.ID_BARRERA_RIGIDA);
    }

    @Override
    protected ForeignValue getMedicionForeignValue() {
	String unidad = ((JComboBox) form.getWidgets().get(DBFieldNames.UNIDAD))
		.getSelectedItem().toString();
	ForeignValue medicionUltimoTrabajo = new CalculateDBForeignValueLastJob(
		unidad, getForeignKey(), DBFieldNames.MEDICION_ULTIMO_TRABAJO,
		DBFieldNames.BARRERA_RIGIDA_TRABAJOS_DBTABLENAME,
		DBFieldNames.ID_BARRERA_RIGIDA).getForeignValue();

	if (medicionUltimoTrabajo != null) {
	    return medicionUltimoTrabajo;
	}
	return null;
    }

}
