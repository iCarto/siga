package es.icarto.gvsig.extgia.forms.estructuras;

import java.util.HashMap;

import javax.swing.JComponent;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.AbstractForm;

public class EstructurasCalculateIDValue extends CalculateComponentValue {

    public EstructurasCalculateIDValue(AbstractForm form,
	    HashMap<String, JComponent> allFormWidgets,
	    String resultComponentName, String... operatorComponentsNames) {
	super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
	// TODO Auto-generated constructor stub
    }

    @Override
    public void setValue(boolean validate) {
	String estructuraID = String.valueOf(SqlUtils.
		getNextIdOfSequence("audasa_extgia.estructuras_id_estructura_seq"));
	resultComponent.setText(estructuraID);
	form.getFormController().setValue(resultComponentName, estructuraID);

    }

}
