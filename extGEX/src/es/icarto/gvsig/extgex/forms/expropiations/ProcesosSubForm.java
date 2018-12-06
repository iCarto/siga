package es.icarto.gvsig.extgex.forms.expropiations;

import es.icarto.gvsig.extgex.forms.GEXSubForm;

@SuppressWarnings("serial")
public class ProcesosSubForm extends GEXSubForm {
    
    public static final String[] colNames = { "fecha", "tipo", "importe", "estado"};
    public static final String[] colAlias = { "Fecha", "Tipo", "<html>Importe (&euro;)</html>", "Estado"};
    public static final int[] colWidths = { 100, 100, 100, 100 };
    
    public ProcesosSubForm() {
        super("procesos");
    }
    
    @Override
    protected String getBasicName() {
        return "procesos";
    }
    

}
