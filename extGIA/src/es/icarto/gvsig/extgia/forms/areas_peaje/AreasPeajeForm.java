package es.icarto.gvsig.extgia.forms.areas_peaje;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.CalculateComponentValue;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class AreasPeajeForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "areas_peaje";

    JTextField areaPeajeIDWidget;
    CalculateComponentValue areaPeajeid;

    public static String[] viasColNames = { "id_via", "via", "via_tipo",
	    "reversible", "cabinas" };

    public static String[] viasColAlias = { "ID", "Nª Vía", "Tipo Vía",
	    "Reversible", "Nº Cabinas" };

    public AreasPeajeForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		new int[] { 1, 30, 90, 70, 200 }, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosWhitoutIndexColNames,
		DBFieldNames.reconocimientosWhitoutIndexColAlias, null, this));

	addTableHandler(new GIAAlphanumericTableHandler("areas_peaje_vias",
		getWidgets(), getElementID(), viasColNames, viasColAlias, null,
		this));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (areaPeajeIDWidget.getText().isEmpty()) {
	    areaPeajeid = new AreasPeajeCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    areaPeajeid.setValue(true);
	}
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey(getElementID())) {
	    if (areaPeajeIDWidget.getText() != "") {
		String query = "SELECT id_area_peaje FROM audasa_extgia.areas_peaje "
			+ " WHERE id_area_peaje = '"
			+ areaPeajeIDWidget.getText() + "';";
		PreparedStatement statement = null;
		Connection connection = DBSession.getCurrentSession()
			.getJavaConnection();
		try {
		    statement = connection.prepareStatement(query);
		    statement.execute();
		    ResultSet rs = statement.getResultSet();
		    if (rs.next()) {
			JOptionPane.showMessageDialog(null,
				"El ID está en uso, por favor, escoja otro.",
				"ID en uso", JOptionPane.WARNING_MESSAGE);
			return true;
		    }
		} catch (SQLException e) {
		    e.printStackTrace();
		}
	    }
	}
	return super.validationHasErrors();
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	areaPeajeIDWidget = (JTextField) widgets.get(getElementID());
    }

    @Override
    public Elements getElement() {
	return Elements.Areas_Peaje;
    }

    @Override
    public String getElementID() {
	return "id_area_peaje";
    }

    @Override
    public String getElementIDValue() {
	return areaPeajeIDWidget.getText();
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

}
