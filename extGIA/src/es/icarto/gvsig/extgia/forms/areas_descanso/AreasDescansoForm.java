package es.icarto.gvsig.extgia.forms.areas_descanso;

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

import org.apache.log4j.Logger;

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
public class AreasDescansoForm extends AbstractFormWithLocationWidgets {

    private static final Logger logger = Logger
	    .getLogger(AreasDescansoForm.class);

    public static final String TABLENAME = "areas_descanso";

    JTextField areaDescansoIDWidget;
    CalculateComponentValue areaDescansoid;

    public AreasDescansoForm(FLyrVect layer) {
	super(layer);

	addTableHandler(new GIAAlphanumericTableHandler(
		getTrabajosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.trabajosColNames, DBFieldNames.trabajosColAlias,
		DBFieldNames.trabajosColWidths, this));

	addTableHandler(new GIAAlphanumericTableHandler(
		getReconocimientosDBTableName(), getWidgets(), getElementID(),
		DBFieldNames.reconocimientosColNames,
		DBFieldNames.reconocimientosColAlias, null, this,
		AreasDescansoReconocimientosSubForm.class));

	addTableHandler(new VectorialTableHandler(RamalesForm.TABLENAME,
		getWidgets(), new String[] { TRAMO, TIPO_VIA, NOMBRE_VIA },
		RamalesForm.colNames, RamalesForm.colAlias));
    }

    @Override
    protected void setListeners() {
	super.setListeners();
	Map<String, JComponent> widgets = getWidgets();

	areaDescansoIDWidget = (JTextField) widgets.get(getElementID());

	areaDescansoid = new AreasDescansoCalculateIDValue(this, getWidgets(),
		getElementID(), DBFieldNames.AREA_MANTENIMIENTO,
		DBFieldNames.BASE_CONTRATISTA, DBFieldNames.TRAMO,
		DBFieldNames.TIPO_VIA, DBFieldNames.MUNICIPIO,
		DBFieldNames.SENTIDO);
	areaDescansoid.setListeners();
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("id_area_descanso")) {
	    if (areaDescansoIDWidget.getText() != "") {
		String query = "SELECT id_area_descanso FROM audasa_extgia.areas_descanso "
			+ " WHERE id_area_descanso = '"
			+ areaDescansoIDWidget.getText() + "';";
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
		    logger.error(e.getStackTrace(), e);
		}
	    }
	}
	return super.validationHasErrors();
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
    public Elements getElement() {
	return Elements.Areas_Descanso;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_area_descanso";
    }

    @Override
    public String getElementIDValue() {
	return areaDescansoIDWidget.getText();
    }

}
