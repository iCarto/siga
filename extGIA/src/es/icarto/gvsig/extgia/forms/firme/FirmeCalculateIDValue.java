package es.icarto.gvsig.extgia.forms.firme;

import java.util.Map;

import javax.swing.JComponent;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.AbstractForm;

public class FirmeCalculateIDValue extends CalculateComponentValue {

    public FirmeCalculateIDValue(AbstractForm form,
	    Map<String, JComponent> allFormWidgets, String resultComponentName,
	    String... operatorComponentsNames) {
	super(form, allFormWidgets, resultComponentName,
		operatorComponentsNames);
    }

    @Override
    public void setValue(boolean validate) {
	String firmeID = String.valueOf(SqlUtils
		.getNextIdOfSequence("audasa_extgia.firme_id_firme_seq"));
	resultComponent.setText(firmeID);
	form.getFormController().setValue(resultComponentName, firmeID);
    }

}
