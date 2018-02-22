package es.icarto.gvsig.extgia.forms.taludes;

import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class TaludesCalculateTaludIDValue extends CalculateComponentValue {

    public TaludesCalculateTaludIDValue(AbstractForm form,
	    Map<String, JComponent> allFormWidgets,
	    String resultComponentName, String... operatorComponentsNames) {
	super(form, allFormWidgets, resultComponentName,
		operatorComponentsNames);
    }

    /**
     * (primera letra "Tipo de Talud")&-&("Número de Talud")&(Primera letra de
     * "Base decontratista") ; EJ: D-584N
     * 
     * @param validate
     *            . True if the operatorComponents validate their checks
     * 
     */
    @Override
    public void setValue(boolean validate) {

	// TODO: Aplicar el formato adecuado a los valores base

	JComboBox tipoTaludWidget = (JComboBox) operatorComponents
		.get(DBFieldNames.TIPO_TALUD);
	JTextField numeroTaludWidget = (JTextField) operatorComponents
		.get(DBFieldNames.NUMERO_TALUD);
	JComboBox baseContratistaWidget = (JComboBox) operatorComponents
		.get(DBFieldNames.BASE_CONTRATISTA);
	JComboBox tramoWidget = (JComboBox) operatorComponents.get(DBFieldNames.TRAMO);

	if (numeroTaludWidget.getText().isEmpty()) {
	    validate = false;
	}

	String taludID = "";
	if (validate) {
	    
	    boolean isAG = ((KeyValue) tramoWidget.getSelectedItem()).getValue().startsWith("AG");
	    String lastLetter = ((KeyValue) baseContratistaWidget.getSelectedItem()).getValue().substring(0, 1);
	    if (isAG) {
	        lastLetter = "X";
	    }
	    
	    taludID = String.format("%s-%03d%s", ((KeyValue) tipoTaludWidget
		    .getSelectedItem()).getValue().substring(0, 1), Integer
		    .valueOf(numeroTaludWidget.getText()),
		    lastLetter);
	}
	resultComponent.setText(taludID);
	form.getFormController().setValue(resultComponentName, taludID);

    }
}
