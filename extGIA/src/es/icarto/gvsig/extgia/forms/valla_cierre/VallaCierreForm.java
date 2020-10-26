package es.icarto.gvsig.extgia.forms.valla_cierre;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class VallaCierreForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "valla_cierre";
    public static final String LAYERNAME = "Valla_Cierre";
    public static final String PK = "gid";
    

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
	return PK;
    }

    @Override
    public String getElementIDValue() {
        JTextField vallaCierreIDWidget = (JTextField) getWidgets().get(PK);
        return vallaCierreIDWidget.getText();
    }
}
