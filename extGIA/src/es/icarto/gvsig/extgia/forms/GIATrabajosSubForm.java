package es.icarto.gvsig.extgia.forms;

import static es.icarto.gvsig.extgia.preferences.DBFieldNames.ANCHO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.LONGITUD;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.MEDICION;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.MEDICION_ELEMENTO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.MEDICION_ULTIMO_TRABAJO;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.UNIDAD;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;

@SuppressWarnings("serial")
public abstract class GIATrabajosSubForm extends GIASubForm {

    private String orgLongitud = "";

    public GIATrabajosSubForm(String basicName) {
	super(basicName);

	updateAnchoWhenUnidadChanges();
	updateLongWhenUnidadChanges();

    }

    private void updateLongWhenUnidadChanges() {
	getFormPanel().getComboBox(UNIDAD).addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    getFormPanel().getTextField(LONGITUD).setText(orgLongitud);
		    getFormController().setValue(LONGITUD, orgLongitud);
		}
	    }
	});
    }

    private void updateAnchoWhenUnidadChanges() {
	getFormPanel().getComboBox(UNIDAD).addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent e) {
		if (e.getStateChange() == ItemEvent.SELECTED) {
		    getFormPanel().getTextField(ANCHO).setText("");
		    getFormController().setValue(ANCHO, "");
		}
	    }
	});
    }

    @Override
    public void actionCreateRecord() {
	super.actionCreateRecord();
	fillForeignValues();
	for (ForeignValue fv : getForeignValues()) {
	    if (fv.getComponent().equals(LONGITUD)) {
		orgLongitud = fv.getValue();
		break;
	    }
	}
	fillMedicionValue();
	validateForm();
	getWindowInfo().setTitle("Añadir Trabajo");
    }

    @Override
    public void actionUpdateRecord(long position) {
	super.actionUpdateRecord(position);
	fillForeignValues();
	for (ForeignValue fv : getForeignValues()) {
	    if (fv.getComponent().equals(LONGITUD)) {
		orgLongitud = fv.getValue();
		break;
	    }
	}
	getWindowInfo().setTitle("Editar Trabajo");
    }

    protected void fillForeignValues() {
	for (ForeignValue fv : getForeignValues()) {
	    String v = (fv.getValue() != null) ? fv.getValue() : "";
	    final String compName = fv.getComponent();
	    getFormPanel().getTextField(compName).setText(v);
	    getFormController().setValue(compName, v);
	}
    }

    protected void fillMedicionValue() {
	String medicionValue = "";

	for (ForeignValue fv : getForeignValues()) {
	    if (fv.getComponent().equals(MEDICION_ULTIMO_TRABAJO)
		    && fv.getValue() != null) {
		medicionValue = fv.getValue();
		break;
	    }
	    if (fv.getComponent().equals(MEDICION_ELEMENTO)
		    && fv.getValue() != null) {
		medicionValue = fv.getValue();
		medicionValue = fv.getValue();
	    }
	}
	getFormPanel().getTextField(MEDICION).setText(medicionValue);
	getFormController().setValue(MEDICION, medicionValue);
    }

    protected abstract ArrayList<ForeignValue> getForeignValues();
}
