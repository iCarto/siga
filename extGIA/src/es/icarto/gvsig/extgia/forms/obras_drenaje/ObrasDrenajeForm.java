package es.icarto.gvsig.extgia.forms.obras_drenaje;

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
public class ObrasDrenajeForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "obras_drenaje";

    JTextField obraDrenajeIDWidget;
    CalculateComponentValue obraDrenajeid;
    CalculateComponentValue obraDrenajeCodigo;

    public ObrasDrenajeForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (obraDrenajeIDWidget.getText().isEmpty()) {
	    obraDrenajeid = new ObrasDrenajeCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    obraDrenajeid.setValue(true);
	}
	
	
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	obraDrenajeIDWidget = (JTextField) widgets.get(getElementID());
	obraDrenajeCodigo = new ObrasDrenajeCalculateCodigo(this, getWidgetComponents(),
            DBFieldNames.CODIGO, DBFieldNames.PK, DBFieldNames.MATERIAL, DBFieldNames.SECCION);
	obraDrenajeCodigo.setListeners();
    }

    @Override
    public Elements getElement() {
	return Elements.Obras_Drenaje;
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
	return "id_obra_drenaje";
    }

    @Override
    public String getElementIDValue() {
	return obraDrenajeIDWidget.getText();
    }
}
