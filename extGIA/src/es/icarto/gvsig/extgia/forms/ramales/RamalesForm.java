package es.icarto.gvsig.extgia.forms.ramales;

import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.preferences.Elements;

@SuppressWarnings("serial")
public class RamalesForm extends AbstractFormWithLocationWidgets {

    public static final String[] colNames = { "gid", "ramal", "sentido",
	    "direccion", "longitud" };
    public static final String[] colAlias = { "ID Ramal", "Nombre Ramal",
	    "Sentido", "Dirección", "Longitud" };

    public RamalesForm(FLyrVect layer) {
	super(layer);
    }

    public static final String TABLENAME = "ramales";

    @Override
    public Elements getElement() {
	return Elements.Ramales;
    }

    @Override
    public String getElementID() {
	return "gid";
    }

    @Override
    public String getElementIDValue() {
	JTextField idWidget = (JTextField) getWidgets().get(getElementID());
	return idWidget.getText();
    }

    @Override
    public JTable getReconocimientosJTable() {
	return null;
    }

    @Override
    public JTable getTrabajosJTable() {
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

}
