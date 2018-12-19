package es.icarto.gvsig.extgex.forms.expropiations;

import es.icarto.gvsig.extgex.forms.GEXSubForm;

@SuppressWarnings("serial")
public class ProcesosSubForm extends GEXSubForm {
    
    public static final String[] colNames = { "fecha", "tipo", "importe", "estado", "observaciones"};
    public static final String[] colAlias = { "Fecha", "Tipo", "<html>Importe (&euro;)</html>", "Estado", "Observaciones"};
    public static final int[] colWidths = { 80, 90, 80, 90, 95 };
    
    public ProcesosSubForm() {
        super("procesos");
    }
    
    @Override
    protected String getBasicName() {
        return "procesos";
    }
    
    @Override
    public void actionCreateRecord() {
        super.actionCreateRecord();
        getWindowInfo().setTitle("Añadir Proceso");
    }
    
   @Override
   public void actionUpdateRecord(long position) {
       super.actionUpdateRecord(position);
       getWindowInfo().setTitle("Editar Proceso");
   }

}
