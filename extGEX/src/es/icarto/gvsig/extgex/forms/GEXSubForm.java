package es.icarto.gvsig.extgex.forms;

import es.icarto.gvsig.extgia.forms.GIASubForm;
 
/*
 * Est� aqu� �nicamente para que los getClass().getClassLoader().getResource cojan el paquete extgex y no otro
 */
@SuppressWarnings("serial")
public class GEXSubForm extends GIASubForm {

    public GEXSubForm() {
        super();
    }
    
    public GEXSubForm(String basicName) {
        super(basicName);
    }

}
