package es.icarto.gvsig.extgia.forms.obras_paso;

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
public class ObrasPasoForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "obras_paso";

    JTextField obraPasoIDWidget;
    CalculateComponentValue obraPasoid;

    public ObrasPasoForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (obraPasoIDWidget.getText().isEmpty()) {
	    obraPasoid = new ObrasPasoCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    obraPasoid.setValue(true);
	}

	repaint();
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	obraPasoIDWidget = (JTextField) widgets.get(getElementID());
    }

    @Override
    public Elements getElement() {
	return Elements.Obras_Paso;
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
	return "id_obra_paso";
    }

    @Override
    public String getElementIDValue() {
	return obraPasoIDWidget.getText();
    }

}
