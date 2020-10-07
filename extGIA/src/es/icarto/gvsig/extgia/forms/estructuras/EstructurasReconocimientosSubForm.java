package es.icarto.gvsig.extgia.forms.estructuras;

import es.icarto.gvsig.extgia.forms.GIASubForm;

@SuppressWarnings("serial")
public class EstructurasReconocimientosSubForm extends GIASubForm {
    
    public EstructurasReconocimientosSubForm() {
    super("estructuras_reconocimientos");
    addCalculation(new EstructurasCalculateResultadoInspeccion(this));
    }

}
