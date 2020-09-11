package es.icarto.gvsig.extgia.forms.firme;

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
public class FirmeForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "firme";

    public static String[] firmeReconocimientoColNames = { "n_inspeccion",
	"tipo_inspeccion", "nombre_revisor", "aparato_medicion",
    "fecha_inspeccion" };
    public static String[] firmeReconocimientoColAlias = { "Nº Inspección",
	"Tipo", "Revisor", "Aparato", "Fecha Inspección" };

    JTextField firmeIDWidget;
    CalculateComponentValue firmeid;

    public static String[] firmeTrabajoColNames = { "id_trabajo",
	"fecha", "pk_inicial", "pk_final", "sentido",
    "descripcion" };

    public static String[] firmeTrabajoColAlias = { "ID", "Fecha",
	"PK inicio", "PK fin", "Sentido", "Descripción" };

    public FirmeForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		firmeReconocimientoColNames, firmeReconocimientoColAlias, null,
		this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		firmeTrabajoColNames, firmeTrabajoColAlias, new int[] { 1, 35,
		    1, 1, 30, 250 }, this));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();
	if (firmeIDWidget.getText().isEmpty()) {
	    firmeid = new FirmeCalculateIDValue(this, getWidgets(),
		    DBFieldNames.ID_FIRME, DBFieldNames.ID_FIRME);
	    firmeid.setValue(true);
	}
	repaint();
    }

    @Override
    protected void setListeners() {
	super.setListeners();

	Map<String, JComponent> widgets = getWidgets();

	firmeIDWidget = (JTextField) widgets.get(getElementID());
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
	return Elements.Firme;
    }

    @Override
    protected boolean hasSentido() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String getElementID() {
	return DBFieldNames.ID_FIRME;
    }

    @Override
    public String getElementIDValue() {
	return firmeIDWidget.getText();
    }

}
