package es.icarto.gvsig.extgia.forms.valla_cierre;

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
public class VallaCierreForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "valla_cierre";

    JTextField vallaCierreIDWidget;
    CalculateComponentValue vallaCierreid;

    public VallaCierreForm(FLyrVect layer) {
	super(layer);

	// int[] trabajoColumnsSize = { 1, 30, 90, 70, 200 };
	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosVegetacionColNames, 
		DBFieldNames.trabajosVegetacionColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		VallaCierreReconocimientosSubForm.class));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (vallaCierreIDWidget.getText().isEmpty()) {
	    vallaCierreid = new VallaCierreCalculateIDValue(this,
		    getWidgetComponents(), DBFieldNames.ID_VALLA_CIERRE,
		    DBFieldNames.ID_VALLA_CIERRE);
	    vallaCierreid.setValue(true);
	}
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	vallaCierreIDWidget = (JTextField) widgets
		.get(DBFieldNames.ID_VALLA_CIERRE);

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
	return Elements.Valla_Cierre;
    }

    @Override
    protected boolean hasSentido() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String getElementID() {
	return "id_valla";
    }

    @Override
    public String getElementIDValue() {
	return vallaCierreIDWidget.getText();
    }
}
