package es.icarto.gvsig.extgia.forms.enlaces;

import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JTextField;

import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class EnlacesCalculateIDValue extends CalculateComponentValue {

    public EnlacesCalculateIDValue(AbstractForm form, Map<String, JComponent> allFormWidgets,
            String resultComponentName, String... operatorComponentsNames) {
        super(form, allFormWidgets, resultComponentName, operatorComponentsNames);
    }

    @Override
    public void setValue(boolean validate) {
        String baseContratistaValue = "";
        String tramoValue = "";
        String tipoViaValue = "";
        String municipioValue = "";

        JComboBox areaMantenimientoWidget = (JComboBox) operatorComponents.get(DBFieldNames.AREA_MANTENIMIENTO);
        String areaMantenimientoValue = ((KeyValue) areaMantenimientoWidget.getSelectedItem()).getKey();
        JComboBox baseContratistaWidget = (JComboBox) operatorComponents.get(DBFieldNames.BASE_CONTRATISTA);
        if (baseContratistaWidget.getItemCount() != 0) {
            baseContratistaValue = ((KeyValue) baseContratistaWidget.getSelectedItem()).getKey();
        }
        JComboBox tramoWidget = (JComboBox) operatorComponents.get(DBFieldNames.TRAMO);
        if (tramoWidget.getItemCount() != 0) {
            tramoValue = ((KeyValue) tramoWidget.getSelectedItem()).getKey();
        }
        JComboBox tipoViaWidget = (JComboBox) operatorComponents.get(DBFieldNames.TIPO_VIA);
        if (tipoViaWidget.getItemCount() != 0) {
            tipoViaValue = ((KeyValue) tipoViaWidget.getSelectedItem()).getKey();
        }
        JComboBox municipioWidget = (JComboBox) operatorComponents.get(DBFieldNames.MUNICIPIO);
        if (municipioWidget.getItemCount() != 0) {
            municipioValue = ((KeyValue) municipioWidget.getSelectedItem()).getKey();
        }
        JTextField pkWidget = (JTextField) operatorComponents.get(DBFieldNames.PK);

        String pkValueFormatted = getPkFormatted(pkWidget);

        if (pkWidget.getText().isEmpty() || pkValueFormatted.isEmpty()) {
            validate = false;
        }

        String enlaceID = "";
        if (validate) {

            enlaceID = areaMantenimientoValue + baseContratistaValue + tramoValue + tipoViaValue + municipioValue
                    + pkValueFormatted;
        }
        resultComponent.setText(enlaceID);
        form.getFormController().setValue(resultComponentName, enlaceID);
    }
}
