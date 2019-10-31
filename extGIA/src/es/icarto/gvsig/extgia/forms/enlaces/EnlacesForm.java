package es.icarto.gvsig.extgia.forms.enlaces;

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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

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
public class EnlacesForm extends AbstractFormWithLocationWidgets {

    private static final Logger logger = Logger.getLogger(EnlacesForm.class);

    public static final String TABLENAME = "enlaces";
    public static final String TOCLAYERNAME = "Enlaces";

    private final VectorialTableHandler ramalesTableHandler;

    JTextField enlaceIDWidget;
    CalculateComponentValue enlaceid;

    public static String[] carreterasColNames = { "id_carretera_enlazada", "clave_carretera", "pk", "titular",
    "tipo_cruce" };

    public static String[] carreterasColAlias = { "ID Carretera", "Clave", "PK", "Titular", "Tipo Cruce" };

    public EnlacesForm(FLyrVect layer) {
        super(layer);

        addTableHandler(new GIAAlphanumericTableHandler(getReconocimientosDBTableName(), getWidgets(), getElementID(),
                DBFieldNames.reconocimientosWhitoutIndexColNames, DBFieldNames.reconocimientosWhitoutIndexColAlias,
                null, this));

        addTableHandler(new GIAAlphanumericTableHandler("enlaces_carreteras_enlazadas", getWidgets(), getElementID(),
                carreterasColNames, carreterasColAlias, null, this));

        ramalesTableHandler = new VectorialTableHandler(RamalesForm.TABLENAME, getWidgets(), new String[] { TRAMO,
            TIPO_VIA, NOMBRE_VIA }, RamalesForm.colNames, RamalesForm.colAlias);
        addTableHandler(ramalesTableHandler);
    }

    @Override
    protected void setListeners() {
        super.setListeners();
        Map<String, JComponent> widgets = getWidgets();

        enlaceIDWidget = (JTextField) widgets.get(DBFieldNames.ID_ENLACE);

        enlaceid = new EnlacesCalculateIDValue(this, getWidgetComponents(), DBFieldNames.ID_ENLACE,
                DBFieldNames.AREA_MANTENIMIENTO, DBFieldNames.BASE_CONTRATISTA, DBFieldNames.TRAMO,
                DBFieldNames.TIPO_VIA, DBFieldNames.MUNICIPIO, DBFieldNames.PK);
        enlaceid.setListeners();

        setIdWhenPkIsAutomaticallyUpdated();

    }

    /**
     * Workaround. When pk field is not typed id is not update. Maybe
     * Calculation class should use DocumentListener for textfield instead of
     * KeyListener
     */
    private void setIdWhenPkIsAutomaticallyUpdated() {

        JTextField textField = (JTextField) getFormBody().getComponentByName(DBFieldNames.PK);
        textField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                doIt();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                doIt();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                doIt();
            }

            private void doIt() {
                if (!isFillingValues()) {
                    enlaceid.setValue(true);
                }
            }
        });
    }

    @Override
    protected boolean validationHasErrors() {
        if (this.getFormController().getValuesChanged().containsKey("id_enlace")) {
            if (enlaceIDWidget.getText() != "") {
                String query = "SELECT id_enlace FROM audasa_extgia.enlaces " + " WHERE id_enlace = '"
                        + enlaceIDWidget.getText() + "';";
                PreparedStatement statement = null;
                Connection connection = DBSession.getCurrentSession().getJavaConnection();
                try {
                    statement = connection.prepareStatement(query);
                    statement.execute();
                    ResultSet rs = statement.getResultSet();
                    if (rs.next()) {
                        JOptionPane.showMessageDialog(null, "El ID está en uso, por favor, escoja otro.", "ID en uso",
                                JOptionPane.WARNING_MESSAGE);
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
        return Elements.Enlaces;
    }

    @Override
    protected boolean hasSentido() {
        return false;
    }

    @Override
    public String getElementID() {
        return "id_enlace";
    }

    @Override
    public String getElementIDValue() {
        return enlaceIDWidget.getText();
    }

}
