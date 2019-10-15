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

    public TaludesCalculateTaludIDValue(AbstractForm form, Map<String, JComponent> allFormWidgets,
            String resultComponentName, String... operatorComponentsNames) {
        super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
    }

    /**
     * (primera letra "Tipo de Talud")&("-")&("Número de Talud")&((Primera letra de
     * "Base decontratista")|("X" si es AG)) ; EJ: D-584N, D-584X
     *
     */
    @Override
    public void setValue(boolean validate) {
        JComboBox tipoWidget = (JComboBox) operatorComponents.get(DBFieldNames.TIPO_TALUD);
        JTextField numeroWidget = (JTextField) operatorComponents.get(DBFieldNames.NUMERO_TALUD);
        JComboBox baseContratistaWidget = (JComboBox) operatorComponents.get(DBFieldNames.BASE_CONTRATISTA);
        JComboBox tramoWidget = (JComboBox) operatorComponents.get(DBFieldNames.TRAMO);

        if (numeroWidget.getText().isEmpty()) {
            validate = false;
        }

        String id = "";
        if (validate) {
            boolean isAG = ((KeyValue) tramoWidget.getSelectedItem()).getValue().startsWith("AG");
            String lastLetter = ((KeyValue) baseContratistaWidget.getSelectedItem()).getValue().substring(0, 1);
            if (isAG) {
                lastLetter = "X";
            }

            String tipoValue = ((KeyValue) tipoWidget.getSelectedItem()).getValue().substring(0, 1);
            Integer numeroValue = Integer.valueOf(numeroWidget.getText());
            id = String.format("%s-%03d%s", tipoValue, numeroValue, lastLetter);
        }
        resultComponent.setText(id);
        form.getFormController().setValue(resultComponentName, id);

    }
}
