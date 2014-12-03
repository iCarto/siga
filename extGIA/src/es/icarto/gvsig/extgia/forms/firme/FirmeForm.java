package es.icarto.gvsig.extgia.forms.firme;

import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.images.ShowImageAction;
import es.icarto.gvsig.extgia.forms.utils.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.utils.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.utils.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;

@SuppressWarnings("serial")
public class FirmeForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "firme";

    public static String[] firmeReconocimientoColNames = { "n_inspeccion",
	    "tipo_inspeccion", "nombre_revisor", "aparato_medicion",
	    "fecha_inspeccion" };
    public static String[] firmeReconocimientoColAlias = { "N� Inspecci�n",
	    "Tipo", "Revisor", "Aparato", "Fecha Inspecci�n" };

    JTextField firmeIDWidget;
    CalculateComponentValue firmeid;

    public static String[] firmeTrabajoColNames = { "id_trabajo",
	    "fecha_certificado", "pk_inicial", "pk_final", "sentido",
	    "descripcion" };

    public static String[] firmeTrabajoColAlias = { "ID", "Fecha cert",
	    "PK inicio", "PK fin", "Sentido", "Descripci�n" };

    public FirmeForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgetComponents(),
		getElementID(), firmeReconocimientoColNames,
		firmeReconocimientoColAlias, null, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgetComponents(),
		getElementID(), firmeTrabajoColNames, firmeTrabajoColAlias,
		new int[] { 1, 35, 1, 1, 30, 250 }, this));
    }

    private void addNewButtonsToActionsToolBar() {
	super.addNewButtonsToActionsToolBar(DBFieldNames.Elements.Firme);
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();
	if (firmeIDWidget.getText().isEmpty()) {
	    firmeid = new FirmeCalculateIDValue(this, getWidgets(),
		    DBFieldNames.ID_FIRME, DBFieldNames.ID_FIRME);
	    firmeid.setValue(true);
	}

	if (filesLinkButton == null) {
	    addNewButtonsToActionsToolBar();
	}

	if (addImageListener != null) {
	    addImageListener.setPkValue(getElementIDValue());
	}

	if (deleteImageListener != null) {
	    deleteImageListener.setPkValue(getElementIDValue());
	}

	// Element image
	new ShowImageAction(imageComponent, addImageButton,
		getImagesDBTableName(), getElementID(), getElementIDValue());

	repaint();
    }

    @Override
    protected void setListeners() {
	super.setListeners();

	Map<String, JComponent> widgets = getWidgets();

	firmeIDWidget = (JTextField) widgets.get(DBFieldNames.ID_FIRME);
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
    protected String getBasicName() {
	return TABLENAME;
    }

    @Override
    public String getElement() {
	return DBFieldNames.Elements.Firme.name();
    }

    @Override
    protected boolean hasSentido() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    public String getElementID() {
	return "id_firme";
    }

    @Override
    public String getElementIDValue() {
	return firmeIDWidget.getText();
    }

    @Override
    public String getImagesDBTableName() {
	return "firme_imagenes";
    }

}
