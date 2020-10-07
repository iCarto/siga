package es.icarto.gvsig.extgia.batch.reconocimientos;

import es.icarto.gvsig.extgia.batch.BatchAbstractSubForm;
import es.icarto.gvsig.extgia.forms.estructuras.EstructurasCalculateResultadoInspeccion;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class BatchEstructurasReconocimientos extends BatchAbstractSubForm {

    public BatchEstructurasReconocimientos(String formFile, String dbTableName) {
    super(dbTableName);
    addCalculation(new EstructurasCalculateResultadoInspeccion(this));
    }

    @Override
    public String getLayerName() {
    return DBFieldNames.ESTRUCTURAS_LAYERNAME;
    }

    @Override
    public String getIdFieldName() {
    return DBFieldNames.ID_ESTRUCTURA;
    }

}
