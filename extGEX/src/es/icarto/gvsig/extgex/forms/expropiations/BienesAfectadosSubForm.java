package es.icarto.gvsig.extgex.forms.expropiations;


import es.icarto.gvsig.extgex.forms.GEXSubForm;
@SuppressWarnings("serial")
public class BienesAfectadosSubForm extends GEXSubForm {
    
    public BienesAfectadosSubForm() {
        super("bienes_afectados");
    }
    
    @Override
    protected String getBasicName() {
        return "bienes_afectados";
    }
    
    @Override
    public void actionCreateRecord() {
        super.actionCreateRecord();
        getWindowInfo().setTitle("Añadir Bien");
    }
    
   @Override
   public void actionUpdateRecord(long position) {
       super.actionUpdateRecord(position);
       getWindowInfo().setTitle("Editar Bien");
   }

}
