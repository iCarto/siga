package es.icarto.gvsig.extgia.forms.areas_mantenimiento;

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
import es.icarto.gvsig.extgia.forms.ramales.RamalesForm;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.gui.tables.handler.VectorialTableHandler;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class AreasMantenimientoForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "areas_mantenimiento";

    JTextField areaMantenimientoIDWidget;
    CalculateComponentValue areaMantenimientoid;

    public AreasMantenimientoForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new VectorialTableHandler(RamalesForm.TABLENAME,
		getWidgets(), new String[] { TRAMO, TIPO_VIA, NOMBRE_VIA },
		RamalesForm.colNames, RamalesForm.colAlias));
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	if (areaMantenimientoIDWidget.getText().isEmpty()) {
	    areaMantenimientoid = new AreasMantenimientoCalculateIDValue(this,
		    getWidgetComponents(), getElementID(), getElementID());
	    areaMantenimientoid.setValue(true);
	}
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey(getElementID())) {
	    if (areaMantenimientoIDWidget.getText() != "") {
		String query = "SELECT id_area_mantenimiento FROM audasa_extgia.areas_mantenimiento "
			+ " WHERE id_area_mantenimiento = '"
			+ areaMantenimientoIDWidget.getText() + "';";
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

	areaMantenimientoIDWidget = (JTextField) widgets.get(getElementID());
    }

    @Override
    public Elements getElement() {
	return Elements.Areas_Mantenimiento;
    }

    @Override
    public String getElementID() {
	return "id_area_mantenimiento";
    }

    @Override
    public String getElementIDValue() {
	return areaMantenimientoIDWidget.getText();
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
