package es.icarto.gvsig.extgia.forms.areas_descanso;

import es.icarto.gvsig.extgia.forms.utils.BasicAbstractSubForm;

@SuppressWarnings("serial")
public class AreasDescansoReconocimientosSubForm extends BasicAbstractSubForm {

    public static final String[] colNames = { "n_inspeccion", "nombre_revisor",
	    "fecha_inspeccion", "indice_estado" };
    public static final String[] colAlias = { "N� Inspecci�n", "Revisor",
	    "Fecha Inspecci�n", "�ndice Estado" };

    public AreasDescansoReconocimientosSubForm() {
	addCalculation(new AreasDescansoCalculateIndiceEstado(this));
    }

    @Override
    protected String getBasicName() {
	return "areas_descanso_reconocimientos";
    }

}
