package es.icarto.gvsig.extgex.forms;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;

@SuppressWarnings("serial")
public class FormExpropiationLine extends BasicAbstractForm {

    public static final String TOCNAME = "Linea_Expropiacion";
    public static final String TOCNAME_AMPLIACION = "Linea_Expropiacion_Ampliacion";
    public static final String TABLENAME = "linea_expropiacion";
    private Boolean ampliacion;

    public FormExpropiationLine(FLyrVect layer, Boolean ampliacion) {
        super(layer);
        this.ampliacion = ampliacion;
        if (this.ampliacion) {
            setTitle("Línea de expropiación - Ampliación");
        }
    }

    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    protected String getSchema() {
	return DBNames.SCHEMA_DATA;
    }
    
    @Override
    protected void fillSpecificValues() {
        super.fillSpecificValues();
        getFormController().setValue("ampliacion", ampliacion.toString());
    }

}
