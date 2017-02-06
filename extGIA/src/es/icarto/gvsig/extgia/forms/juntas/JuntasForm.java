package es.icarto.gvsig.extgia.forms.juntas;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class JuntasForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "juntas";

    JTextField juntaIDWidget;
    CalculateComponentValue juntaid;

    public JuntasForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		JuntasReconocimientosSubForm.class));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (juntaIDWidget.getText().isEmpty()) {
	    juntaid = new JuntasCalculateIDValue(this, getWidgetComponents(),
		    DBFieldNames.ID_JUNTA, DBFieldNames.ID_JUNTA);
	    juntaid.setValue(true);
	}
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();
	juntaIDWidget = (JTextField) widgets.get(DBFieldNames.ID_JUNTA);
    }

    @Override
    public JTable getReconocimientosJTable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public JTable getTrabajosJTable() {
	// TODO Auto-generated method stub
	return null;
    }

    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    public Elements getElement() {
	return Elements.Juntas;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_junta";
    }

    @Override
    public String getElementIDValue() {
	return juntaIDWidget.getText();
    }
}
