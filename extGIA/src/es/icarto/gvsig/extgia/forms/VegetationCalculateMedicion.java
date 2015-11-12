package es.icarto.gvsig.extgia.forms;

import static es.icarto.gvsig.extgia.preferences.DBFieldNames.ANCHO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.LONGITUD;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.MEDICION;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.UNIDAD;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.math.BigDecimal;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JTextField;

import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.calculation.Calculation;

public abstract class VegetationCalculateMedicion extends Calculation {

    UnidadListener unidadHandler;
    private final String idField;

    public VegetationCalculateMedicion(IValidatableForm form, String idField) {
	super(form);
	setCalculateOnlyWhenValid(false);
	this.idField = idField;
	if (!form.isFillingValues()) {
	    unidadHandler = new UnidadListener();
	    ((JComboBox) form.getWidgets().get(UNIDAD))
		    .addItemListener(unidadHandler);
	}
    }

    protected abstract ForeignValue getMedicionForeignValue();

    @Override
    protected String resultName() {
	return MEDICION;
    }

    @Override
    protected String[] operandNames() {
	return new String[] { LONGITUD, ANCHO };
    }

    @Override
    protected String calculate() {
	BigDecimal value = new BigDecimal(1);

	value = operandValue(LONGITUD).multiply(value);
	value = operandValue(ANCHO).multiply(value);

	return formatter.format(value);
    }

    protected HashMap<String, String> getForeignKey() {
	String primaryKeyValue = ((JTextField) form.getWidgets().get(idField))
		.getText();
	HashMap<String, String> foreignKey = new HashMap<String, String>();
	foreignKey.put(idField, primaryKeyValue);
	return foreignKey;
    }

    public class UnidadListener implements ItemListener {

	private void updateMedicionValue() {
	    ((JTextField) form.getWidgets().get(MEDICION))
	    .setText(getMedicionForeignValue().getValue());
	}

	private void updateMedicionLastJobValue() {
	    if (getMedicionForeignValue().getComponent().equalsIgnoreCase(
		    DBFieldNames.MEDICION_ULTIMO_TRABAJO)) {
		((JTextField) form.getWidgets().get(
			DBFieldNames.MEDICION_ULTIMO_TRABAJO))
			.setText(getMedicionForeignValue().getValue());
	    } else {
		((JTextField) form.getWidgets().get(
			DBFieldNames.MEDICION_ULTIMO_TRABAJO)).setText(null);
	    }
	}

	@Override
	public void itemStateChanged(ItemEvent e) {
	    if (form.isFillingValues()) {
		return;
	    }
	    if (e.getStateChange() == ItemEvent.SELECTED) {
		if ((e.getItem() == null)
			|| (e.getItem().toString().trim().isEmpty())) {
		    ((JTextField) form.getWidgets().get(MEDICION)).setText("");
		    ((JTextField) form.getWidgets().get(
			    DBFieldNames.MEDICION_ULTIMO_TRABAJO)).setText("");
		    return;
		}
		updateMedicionValue();
		updateMedicionLastJobValue();
	    }
	}
    }

}
