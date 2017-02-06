package es.icarto.gvsig.extgia.forms.obras_desague;

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
public class ObrasDesagueForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "obras_desague";

    JTextField obraDesagueIDWidget;
    CalculateComponentValue obraDesagueid;

    public ObrasDesagueForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (obraDesagueIDWidget.getText().isEmpty()) {
	    obraDesagueid = new ObrasDesagueCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    obraDesagueid.setValue(true);
	}
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	obraDesagueIDWidget = (JTextField) widgets.get(getElementID());
    }

    @Override
    public Elements getElement() {
	return Elements.Obras_Desague;
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
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_obra_desague";
    }

    @Override
    public String getElementIDValue() {
	return obraDesagueIDWidget.getText();
    }
}
