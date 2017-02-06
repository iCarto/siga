package es.icarto.gvsig.extgia.forms.areas_servicio;

import static es.icarto.gvsig.extgia.preferences.DBFieldNames.NOMBRE_VIA;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.TIPO_VIA;
import static es.icarto.gvsig.extgia.preferences.DBFieldNames.TRAMO;

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
import es.icarto.gvsig.extgia.forms.ramales.RamalesForm;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.gui.tables.handler.VectorialTableHandler;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class AreasServicioForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "areas_servicio";

    JTextField areaServicioIDWidget;
    CalculateComponentValue areaServicioid;

    public AreasServicioForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new VectorialTableHandler(RamalesForm.TABLENAME,
		getWidgets(), new String[] { TRAMO, TIPO_VIA, NOMBRE_VIA },
		RamalesForm.colNames, RamalesForm.colAlias));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		AreasServicioReconocimientosSubForm.class));
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	areaServicioIDWidget = (JTextField) widgets
		.get(DBFieldNames.ID_AREA_SERVICIO);

	areaServicioid = new AreasServicioCalculateIDValue(this,
		getWidgetComponents(), DBFieldNames.ID_AREA_SERVICIO,
		DBFieldNames.AREA_MANTENIMIENTO, DBFieldNames.BASE_CONTRATISTA,
		DBFieldNames.TRAMO, DBFieldNames.TIPO_VIA,
		DBFieldNames.MUNICIPIO, DBFieldNames.SENTIDO);
	areaServicioid.setListeners();
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("id_area_servicio")) {
	    if (areaServicioIDWidget.getText() != "") {
		String query = "SELECT id_area_servicio FROM audasa_extgia.areas_servicio "
			+ " WHERE id_area_servicio = '"
			+ areaServicioIDWidget.getText() + "';";
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
	return Elements.Areas_Servicio;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_area_servicio";
    }

    @Override
    public String getElementIDValue() {
	return areaServicioIDWidget.getText();
    }

}
