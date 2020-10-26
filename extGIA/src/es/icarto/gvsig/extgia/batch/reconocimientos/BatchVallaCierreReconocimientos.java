package es.icarto.gvsig.extgia.batch.reconocimientos;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.forms.valla_cierre.VallaCierreCalculateIndiceEstado;
import es.icarto.gvsig.extgia.forms.valla_cierre.VallaCierreForm;

@SuppressWarnings("serial")
public class BatchVallaCierreReconocimientos extends BatchAbstractSubForm {

    public BatchVallaCierreReconocimientos(String formFile, String dbTableName) {
	super(dbTableName);
	addCalculation(new VallaCierreCalculateIndiceEstado(this));
    }

    @Override
    public String getLayerName() {
	return VallaCierreForm.LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
	return VallaCierreForm.PK;
    }
}
