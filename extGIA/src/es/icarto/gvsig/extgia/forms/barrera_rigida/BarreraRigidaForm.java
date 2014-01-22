package es.icarto.gvsig.extgia.forms.barrera_rigida;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.JTextField;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgia.forms.utils.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.utils.CalculateComponentValue;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.utils.SqlUtils;
import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.listeners.DependentComboboxHandler;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class BarreraRigidaForm extends AbstractFormWithLocationWidgets {

    public static final String ABEILLE_FILENAME = "forms/barrera_rigida.xml";
    public static final String ABEILLE_RECONOCIMIENTOS_FILENAME = "forms/barrera_rigida_reconocimiento_estado.xml";
    public static final String ABEILLE_TRABAJOS_FILENAME = "forms/barrera_rigida_trabajos.xml";

    JTextField barreraRigidaIDWidget;
    CalculateComponentValue barreraRigidaid;
    CalculateComponentValue barreraRigidaCodigo;
    private JComboBox tipoVia;
    private DependentComboboxHandler direccionDomainHandler;

    AddReconocimientoListener addReconocimientoListener;
    EditReconocimientoListener editReconocimientoListener;
    AddTrabajoListener addTrabajoListener;
    EditTrabajoListener editTrabajoListener;
    DeleteReconocimientoListener deleteReconocimientoListener;
    DeleteTrabajoListener deleteTrabajoListener;

    public BarreraRigidaForm(FLyrVect layer) {
	super(layer);
	initListeners();
    }

    private void addNewButtonsToActionsToolBar() {
	super.addNewButtonsToActionsToolBar(DBFieldNames.Elements.Barrera_Rigida);
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();

	direccionDomainHandler.updateComboBoxValues();

	if (filesLinkButton == null) {
	    addNewButtonsToActionsToolBar();
	}

	// Embebed Tables
	int[] trabajoColumnsSize = {1, 30, 90, 70, 200};
	SqlUtils.createEmbebedTableFromDB(reconocimientoEstado,
		"audasa_extgia", getReconocimientosDBTableName(),
		DBFieldNames.reconocimientoEstadoFields, null, "id_barrera_rigida",
		barreraRigidaIDWidget.getText(), "n_inspeccion");
	SqlUtils.createEmbebedTableFromDB(trabajos,
		"audasa_extgia", getTrabajosDBTableName(),
		DBFieldNames.trabajoFields, trabajoColumnsSize, "id_barrera_rigida",
		barreraRigidaIDWidget.getText(), "id_trabajo");
	repaint();
    }

    protected void initListeners() {

	HashMap<String, JComponent> widgets = getWidgetComponents();

	barreraRigidaIDWidget = (JTextField) widgets.get(DBFieldNames.ID_BARRERA_RIGIDA);

	barreraRigidaid = new BarreraRigidaCalculateIDValue(this, getWidgetComponents(),
		DBFieldNames.ID_BARRERA_RIGIDA, DBFieldNames.NUMERO_BARRERA_RIGIDA,
		DBFieldNames.BASE_CONTRATISTA);
	barreraRigidaid.setListeners();

	barreraRigidaCodigo = new BarreraRigidaCalculateCodigo(this, getWidgetComponents(),
		DBFieldNames.CODIGO, DBFieldNames.TIPO, DBFieldNames.METODO_CONSTRUCTIVO,
		DBFieldNames.PERFIL);
	barreraRigidaCodigo.setListeners();

	JComboBox direccion = (JComboBox) getWidgetComponents().get(
		"direccion");
	tipoVia = (JComboBox) getWidgetComponents().get("tipo_via");
	direccionDomainHandler = new DependentComboboxHandler(this,
		tipoVia, direccion);
	tipoVia.addActionListener(direccionDomainHandler);

	addReconocimientoListener = new AddReconocimientoListener();
	addReconocimientoButton.addActionListener(addReconocimientoListener);
	editReconocimientoListener = new EditReconocimientoListener();
	editReconocimientoButton.addActionListener(editReconocimientoListener);
	addTrabajoListener = new AddTrabajoListener();
	addTrabajoButton.addActionListener(addTrabajoListener);
	editTrabajoListener = new EditTrabajoListener();
	editTrabajoButton.addActionListener(editTrabajoListener);
	deleteReconocimientoListener = new DeleteReconocimientoListener();
	deleteReconocimientoButton.addActionListener(deleteReconocimientoListener);
	deleteTrabajoListener = new DeleteTrabajoListener();
	deleteTrabajoButton.addActionListener(deleteTrabajoListener);
    }

    @Override
    protected void removeListeners() {
	tipoVia.removeActionListener(direccionDomainHandler);
	addReconocimientoButton.removeActionListener(addReconocimientoListener);
	editReconocimientoButton.removeActionListener(editReconocimientoListener);
	addTrabajoButton.removeActionListener(addTrabajoListener);
	editTrabajoButton.removeActionListener(editTrabajoListener);
	deleteReconocimientoButton.removeActionListener(deleteReconocimientoListener);
	deleteTrabajoButton.removeActionListener(deleteTrabajoListener);
	super.removeListeners();
    }

    @Override
    protected boolean validationHasErrors() {
	if (this.getFormController().getValuesChanged()
		.containsKey("id_barrera_rigida")) {
	    if (barreraRigidaIDWidget.getText() != "") {
		String query = "SELECT id_barrera_rigida FROM audasa_extgia.barrera_rigida "
			+ " WHERE id_barrera_rigida = '"
			+ barreraRigidaIDWidget.getText() + "';";
		PreparedStatement statement = null;
		Connection connection = DBSession.getCurrentSession()
			.getJavaConnection();
		try {
		    statement = connection.prepareStatement(query);
		    statement.execute();
		    ResultSet rs = statement.getResultSet();
		    if (rs.next()) {
			JOptionPane.showMessageDialog(null,
				"El ID est� en uso, por favor, escoja otro.",
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

    public class AddReconocimientoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    BarreraRigidaReconocimientosSubForm subForm =
		    new BarreraRigidaReconocimientosSubForm(
			    ABEILLE_RECONOCIMIENTOS_FILENAME,
			    getReconocimientosDBTableName(),
			    reconocimientoEstado,
			    "id_barrera_rigida",
			    barreraRigidaIDWidget.getText(),
			    null,
			    null,
			    false);
	    PluginServices.getMDIManager().addWindow(subForm);
	}
    }

    public class AddTrabajoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    BarreraRigidaTrabajosSubForm subForm = new BarreraRigidaTrabajosSubForm(
		    ABEILLE_TRABAJOS_FILENAME,
		    getTrabajosDBTableName(),
		    trabajos,
		    "id_barrera_rigida",
		    barreraRigidaIDWidget.getText(),
		    null,
		    null,
		    false);
	    PluginServices.getMDIManager().addWindow(subForm);
	}
    }

    public class EditReconocimientoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (reconocimientoEstado.getSelectedRowCount() != 0) {
		int row = reconocimientoEstado.getSelectedRow();
		BarreraRigidaReconocimientosSubForm subForm =
			new BarreraRigidaReconocimientosSubForm(
				ABEILLE_RECONOCIMIENTOS_FILENAME,
				getReconocimientosDBTableName(),
				reconocimientoEstado,
				"id_barrera_rigida",
				barreraRigidaIDWidget.getText(),
				"n_inspeccion",
				reconocimientoEstado.getValueAt(row, 0).toString(),
				true);
		PluginServices.getMDIManager().addWindow(subForm);
	    }else {
		JOptionPane.showMessageDialog(null,
			"Debe seleccionar una fila para editar los datos.",
			"Ninguna fila seleccionada",
			JOptionPane.INFORMATION_MESSAGE);
	    }
	}
    }

    public class EditTrabajoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (trabajos.getSelectedRowCount() != 0) {
		int row = trabajos.getSelectedRow();
		BarreraRigidaTrabajosSubForm subForm = new BarreraRigidaTrabajosSubForm(
			ABEILLE_TRABAJOS_FILENAME,
			getTrabajosDBTableName(),
			trabajos,
			"id_barrera_rigida",
			barreraRigidaIDWidget.getText(),
			"id_trabajo",
			trabajos.getValueAt(row, 0).toString(),
			true);
		PluginServices.getMDIManager().addWindow(subForm);
	    }else {
		JOptionPane.showMessageDialog(null,
			"Debe seleccionar una fila para editar los datos.",
			"Ninguna fila seleccionada",
			JOptionPane.INFORMATION_MESSAGE);
	    }
	}
    }

    public class DeleteReconocimientoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    deleteElement(reconocimientoEstado, getReconocimientosDBTableName(),
		    getReconocimientosIDField());
	}
    }

    public class DeleteTrabajoListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    deleteElement(trabajos, getTrabajosDBTableName(), getTrabajosIDField());
	}
    }

    @Override
    public String getFormBodyPath() {
	return ABEILLE_FILENAME;
    }

    @Override
    public Logger getLoggerName() {
	return Logger.getLogger(this.getClass().getName());
    }

    @Override
    public String getXMLPath() {
	return this.getClass().getClassLoader()
		.getResource("rules/barrera_rigida_metadata.xml")
		.getPath();
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
    public String getReconocimientosDBTableName() {
	return "barrera_rigida_reconocimientos";
    }

    @Override
    public String getTrabajosDBTableName() {
	return "barrera_rigida_trabajos";
    }

    @Override
    public boolean isSpecialCase() {
	// TODO Auto-generated method stub
	return false;
    }

    @Override
    protected String getBasicName() {
	return "Barrera R�gida";
    }

    @Override
    public String getElement() {
	return DBFieldNames.Elements.Barrera_Rigida.name();
    }

    @Override
    public String getReconocimientosFormFileName() {
	return ABEILLE_RECONOCIMIENTOS_FILENAME;
    }

    @Override
    public String getTrabajosFormFileName() {
	return ABEILLE_TRABAJOS_FILENAME;
    }

    @Override
    protected boolean hasSentido() {
	return true;
    }

    @Override
    public String getElementID() {
	return "id_barrera_rigida";
    }

    @Override
    public String getElementIDValue() {
	return barreraRigidaIDWidget.getText();
    }

    @Override
    public String getImagesDBTableName() {
	return "barrera_rigida_imagenes";
    }

}
