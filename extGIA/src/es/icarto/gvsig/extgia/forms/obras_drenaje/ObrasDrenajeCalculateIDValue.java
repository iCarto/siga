package es.icarto.gvsig.extgia.forms.obras_drenaje;

import java.util.HashMap;

import javax.swing.JComponent;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.AbstractForm;

public class ObrasDrenajeCalculateIDValue extends CalculateComponentValue {

    public ObrasDrenajeCalculateIDValue(AbstractForm form,
	    HashMap<String, JComponent> allFormWidgets,
	    String resultComponentName, String... operatorComponentsNames) {
	super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void setValue(boolean validate) {
	String obraDrenajeID = String.valueOf(SqlUtils.
		getNextIdOfSequence("audasa_extgia.obras_drenaje_id_obra_drenaje_seq"));
	resultComponent.setText(obraDrenajeID);
	form.getFormController().setValue(resultComponentName, obraDrenajeID);

    }

}
