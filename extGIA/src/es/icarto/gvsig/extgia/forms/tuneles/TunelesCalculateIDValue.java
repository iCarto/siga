package es.icarto.gvsig.extgia.forms.tuneles;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JComponent;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.AbstractForm;

public class TunelesCalculateIDValue extends CalculateComponentValue {

	public TunelesCalculateIDValue(AbstractForm form,
			Map<String, JComponent> allFormWidgets, String resultComponentName,
			String... operatorComponentsNames) {
		super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
	}

	@Override
	public void setValue(boolean validate) {
	String tunelID = String.valueOf(SqlUtils.
		getNextIdOfSequence("audasa_extgia.tuneles_gid_seq"));
	resultComponent.setText(tunelID);
	form.getFormController().setValue(resultComponentName, tunelID);		
		
	}

}
