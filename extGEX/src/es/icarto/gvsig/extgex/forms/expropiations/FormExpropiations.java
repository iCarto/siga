package es.icarto.gvsig.extgex.forms.expropiations;

import java.awt.event.ActionEvent;

import static es.icarto.gvsig.extgex.forms.expropiations.ImportePagadoTotalAutocalculado.FINCAS_IMPORTE_PAGADO_TOTAL_AUTOCALCULADO; 
import static es.icarto.gvsig.extgex.forms.expropiations.ImportePendienteTotalAutocalculado.FINCAS_IMPORTE_PENDIENTE_TOTAL_AUTOCALCULADO;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.InputStream;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.engine.values.ValueFactory;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgex.navtable.NavTableComponentsFactory;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.utils.retrievers.LocalizadorFormatter;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.icarto.gvsig.navtableforms.gui.CustomTableModel;
import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalidator.listeners.DependentComboboxHandler;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class FormExpropiations extends BasicAbstractForm implements TableModelListener {
    

    private static final Logger logger = Logger.getLogger(AbstractForm.class);


    private static final String EXPROPIATIONS_AFECTADO_PM = "afectado_por_policia_margenes";

    public static final String TABLENAME = "exp_finca";
    public static final String TOCNAME = "Fincas";
    public static final String TOCNAME_AMPLIACION = "Fincas_Ampliacion";
    public static final String PKFIELD = "id_finca";

    private static final String WIDGET_REVERSIONES = "tabla_reversiones_afectan";
    private static final String WIDGET_PM = "tabla_pm_afectan";
    
    public static final String[] bienesAfectadosColNames = { "superficie", "tipo"};
    public static final String[] bienesAfectadosColAlias = { "<html>Superficie (m<sup>2</sup>)</html>", "Tipo"};
    public static final int[] bienesAfectadosColWidths = { 100, 100 };

    private JComboBox tramo;
    private JComboBox uc;
    private JComboBox ayuntamiento;
    private JComboBox subtramo;
    private JTextField finca;
    private JTextField numFinca;
    private JTextField seccion;

    private JTable reversiones;
    private JTable pm;

    private JComboBox afectado_pm;

    private DependentComboboxHandler ayuntamientoDomainHandler;
    private DependentComboboxHandler subtramoDomainHandler;
    private UpdateNroFincaHandler updateNroFincaHandler;

    private FormReversionsLauncher formReversionsLauncher;

    private ArrayList<String> oldReversions;

    private GIAAlphanumericTableHandler bienesExpropiadosTableHandler;

    private TableModelListener bienesExpropiadosTableModelListener;

    public FormExpropiations(FLyrVect layer, IGeometry insertedGeom) {
	super(layer);
	
	AbstractSubForm bienesExpropiadosSubForm = new BienesAfectadosSubForm();
	bienesExpropiadosTableHandler = new GIAAlphanumericTableHandler("bienes_afectados", getWidgets(), getElementID(), bienesAfectadosColNames, bienesAfectadosColAlias, bienesAfectadosColWidths, this, bienesExpropiadosSubForm);
	addTableHandler(bienesExpropiadosTableHandler);

	
	if (isAmpliacion()) {
	    addTableHandler(new GIAAlphanumericTableHandler("procesos", getWidgets(), getElementID(), ProcesosSubForm.colNames, ProcesosSubForm.colAlias, ProcesosSubForm.colWidths, this, ProcesosSubForm.class));
	    
	} else {
	    addCalculation(new ImportePendienteTotalAutocalculado(this));
        addCalculation(new ImportePagadoTotalAutocalculado(this));
	}
	
	// superficie_expropiada_total_autocalculado - Trigger + Workaround con un listener a la tabla y un refresh
	
    
	addButtonsToActionsToolBar();


	addChained(DBNames.FIELD_UC_FINCAS, DBNames.FIELD_TRAMO_FINCAS);
	initTooltips();
    }
    
    private boolean isAmpliacion() {
        return this.layer.getName().equalsIgnoreCase(TOCNAME_AMPLIACION);
    }

    private void initTooltips() {
	getFormPanel().getTextField(FINCAS_IMPORTE_PAGADO_TOTAL_AUTOCALCULADO).setToolTipText("<html>Depósito previo imp. consignado <br> + Depósito previo imp. indemnización <br> + Depósito previo imp. pagado <br> + Mútuo acuerdo importe <br> + Anticipo importe <br> + Mútuo acuerdo parcial importe <br> + Límite acuerdo importe <br> + Imp. pagos varios <br> + Otros pagos imp. indemnización <br> +  Imp. justiprecio <br> - Depósito previo imp. levantamiento</html>");
	getFormPanel().getTextField(FINCAS_IMPORTE_PENDIENTE_TOTAL_AUTOCALCULADO).setToolTipText("Imp. terrenos pendiente + Imp. mejoras pendiente");
    }

    private void addButtonsToActionsToolBar() {
	JPanel actionsToolBar = this.getActionsToolBar();
	NavTableComponentsFactory ntFactory = new NavTableComponentsFactory();
	JButton filesLinkB = ntFactory.getFilesLinkButton(layer, this);
	String reportName = isAmpliacion() ? "exp_finca_ampliacion.jasper" : "exp_finca";
	JButton printReportB = ntFactory.getPrintButton(layer, this, reportName);
	if ((filesLinkB != null) && (printReportB != null)) {
	    actionsToolBar.add(filesLinkB);
	    actionsToolBar.add(printReportB);
	}
	actionsToolBar.add(new JButton(new AddFincaAction(layer, this)));
    }

    @Override
    protected String getPrimaryKeyValue() {
	return getFormController().getValue(PKFIELD);
    }
    
    public String getElementID() {
        return PKFIELD;
    };

    @Override
    protected void setListeners() {
	super.setListeners();

	// RETRIEVE WIDGETS
	Map<String, JComponent> widgets = getWidgets();

	tramo = (JComboBox) widgets.get(DBNames.FIELD_TRAMO_FINCAS);
	uc = (JComboBox) widgets.get(DBNames.FIELD_UC_FINCAS);
	ayuntamiento = (JComboBox) widgets
		.get(DBNames.FIELD_AYUNTAMIENTO_FINCAS);
	subtramo = (JComboBox) widgets
		.get(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS);

	numFinca = (JTextField) widgets.get(DBNames.FIELD_NUMEROFINCA_FINCAS);
	seccion = (JTextField) widgets.get(DBNames.FIELD_SECCION_FINCAS);

	finca = (JTextField) widgets.get(DBNames.FIELD_IDFINCA);

	reversiones = (JTable) widgets.get(WIDGET_REVERSIONES);
	pm = (JTable) widgets.get(WIDGET_PM);

	afectado_pm = (JComboBox) widgets.get(EXPROPIATIONS_AFECTADO_PM);

	ayuntamientoDomainHandler = new DependentComboboxHandler(this, uc,
		ayuntamiento);
	uc.addActionListener(ayuntamientoDomainHandler);

	ArrayList<JComponent> parentComponents = new ArrayList<JComponent>();
	parentComponents.add(uc);
	parentComponents.add(ayuntamiento);
	subtramoDomainHandler = new DependentComboboxHandler(this,
		parentComponents, subtramo);
	ayuntamiento.addActionListener(subtramoDomainHandler);

	updateNroFincaHandler = new UpdateNroFincaHandler();
	subtramo.addActionListener(updateNroFincaHandler);
	numFinca.addKeyListener(updateNroFincaHandler);
	seccion.addKeyListener(updateNroFincaHandler);

	formReversionsLauncher = new FormReversionsLauncher(this);
	reversiones.addMouseListener(formReversionsLauncher);

    }

    @Override
    protected void removeListeners() {
	super.removeListeners();

	uc.removeActionListener(ayuntamientoDomainHandler);
	ayuntamiento.removeActionListener(subtramoDomainHandler);
	subtramo.removeActionListener(updateNroFincaHandler);
	numFinca.removeKeyListener(updateNroFincaHandler);
	seccion.removeKeyListener(updateNroFincaHandler);

	reversiones.removeMouseListener(formReversionsLauncher);
    }

    private void setIDFinca() {
	if ((tramo.getSelectedItem() instanceof KeyValue)
		&& (uc.getSelectedItem() instanceof KeyValue)
		&& (ayuntamiento.getSelectedItem() instanceof KeyValue)
		&& (subtramo.getSelectedItem() instanceof KeyValue)) {
	    // will update id_finca only when comboboxes have proper values
	    String id_finca = LocalizadorFormatter.getTramo(((KeyValue) tramo
		    .getSelectedItem()).getKey())
		    + LocalizadorFormatter.getUC(((KeyValue) uc
			    .getSelectedItem()).getKey())
		    + LocalizadorFormatter
			    .getAyuntamiento(((KeyValue) ayuntamiento
				    .getSelectedItem()).getKey())
		    + LocalizadorFormatter.getSubtramo(((KeyValue) subtramo
			    .getSelectedItem()).getKey())
		    + getStringNroFincaFormatted()
		    + getStringSeccionFormatted();
	    finca.setText(id_finca);
	    getFormController().setValue(DBNames.FIELD_IDFINCA, id_finca);
	}
    }

    public String getIDFinca() {
	return finca.getText();
    }

    private String getStringNroFincaFormatted() {
	HashMap<String, String> values = getFormController().getValuesChanged();
	try {
	    String formatted = LocalizadorFormatter.getNroFinca(values
		    .get(DBNames.FIELD_NUMEROFINCA_FINCAS));
	    numFinca.setText(formatted);
	    getFormController().setValue(DBNames.FIELD_NUMEROFINCA_FINCAS,
		    formatted);
	    return formatted;
	} catch (NumberFormatException nfe) {
	    numFinca.setText(LocalizadorFormatter.FINCA_DEFAULT_VALUE);
	    getFormController().setValue(DBNames.FIELD_NUMEROFINCA_FINCAS,
		    LocalizadorFormatter.FINCA_DEFAULT_VALUE);
	    return LocalizadorFormatter.FINCA_DEFAULT_VALUE;
	}
    }

    private String getStringSeccionFormatted() {
	HashMap<String, String> values = getFormController().getValuesChanged();
	String formatted = values.get(DBNames.FIELD_SECCION_FINCAS);
	seccion.setText(formatted);
	getFormController().setValue(DBNames.FIELD_SECCION_FINCAS, formatted);
	return formatted;
    }

    public class UpdateNroFincaHandler implements KeyListener, ActionListener {

	@Override
	public void keyTyped(KeyEvent e) {
	}

	@Override
	public void keyPressed(KeyEvent e) {
	}

	@Override
	public void keyReleased(KeyEvent e) {
	    if (!isFillingValues()) {
		setIDFinca();
	    }
	}

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (!isFillingValues()) {
		setIDFinca();
	    }
	}
    }

    @Override
    protected void fillSpecificValues() {
    updateAutomaticFieldSuperficieWhenSubFormDataChanges_pre();
	super.fillSpecificValues();
	ayuntamientoDomainHandler.updateComboBoxValues();
	subtramoDomainHandler.updateComboBoxValues();
	updateJTables();
	
	updateAutomaticFieldSuperficieWhenSubFormDataChanges_post();
	
    }

    
    
    /*
     * El campo superficie_expropiada_total_autocalculado debe actualizarse cuando cambien los
     * datos del subformulario/tabla (añadir, editar o eliminar) Hay varias formas de intentar
     * implementar esto:
     * 
     *  * Por ejemplo usar NOTIFY en un trigger de bd y en el formulario padre escuchar el canal con LISTEN
     *  https://jdbc.postgresql.org/documentation/81/listennotify.html
     *  https://tapoueh.org/blog/2018/07/postgresql-listen/notify/
     *  
     *  * Trigger para calcular en bd e implementando windowClosed en el subformulario más o menos así
     *  el problema es que no funciona al borrar una fila porque el form no llega a abrirse
     *  @Override
        public void windowClosed() {
            super.windowClosed();
            
            No llamamos a parentForm.refresh porque ni el reloadRecordset
            ni el refresGUI por si mismos hacen que se actualice el LayerController
            de este modo refreshGUI lee los valores de un LayerController que no 
            está actualizado pero onPositionChange si que lo actualiza
              
            
        }
     *  
     * Aquí lo implementamos como un listener sobre la propia tabla. Va en fillSpecificValues porqué 
     * el TableHandler puede acabar creando un nuevo modelo de datos en este método y esta rara construcción
     * es para tratar de asegurarnos que no quedan listeners pendientes al cambiar de registros y que siempre 
     * se setea el listener 
     */
    private void updateAutomaticFieldSuperficieWhenSubFormDataChanges_pre() {
        if (bienesExpropiadosTableModelListener != null) {
            bienesExpropiadosTableHandler.getModel().removeTableModelListener(bienesExpropiadosTableModelListener);     
        }
    }
    
    private void updateAutomaticFieldSuperficieWhenSubFormDataChanges_post() {
        bienesExpropiadosTableModelListener = new TableModelListener() {
            
            @Override
            public void tableChanged(TableModelEvent e) {
                try {
                    FormExpropiations.this.reloadRecordset();
                } catch (ReadDriverException ex) {
                    logger.error(ex.getStackTrace(), ex);
                }
                FormExpropiations.this.onPositionChange(null);    
            }
        };
        bienesExpropiadosTableHandler.getModel().addTableModelListener(bienesExpropiadosTableModelListener);    
    }

    private void updateJTables() {
	oldReversions = new ArrayList<String>();

	updateReversionsTable();
	updatePMTable();

    }


    private void updateReversionsTable() {
	DefaultTableModel tableModel = setTableHeader();
	try {
	    reversiones.setModel(tableModel);
	    Value[] reversionData = new Value[tableModel.getColumnCount()];
	    PreparedStatement statement;
	    String query = "SELECT "
		    + DBNames.FIELD_IDREVERSION_FINCA_REVERSION + ", "
		    + DBNames.FIELD_SUPERFICIE_FINCA_REVERSION + ", "
		    + DBNames.FIELD_IMPORTE_FINCA_REVERSION_EUROS + ", "
		    + DBNames.FIELD_IMPORTE_FINCA_REVERSION_PTAS + ", "
		    + DBNames.FIELD_FECHA_FINCA_REVERSION + " " + "FROM "
		    + DBNames.SCHEMA_DATA + "." + DBNames.TABLE_FINCA_REVERSION
		    + " " + "WHERE "
		    + DBNames.FIELD_IDEXPROPIACION_FINCA_REVERSION + " = '"
		    + getIDFinca() + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    while (rs.next()) {
		reversionData[0] = ValueFactory.createValue(rs.getString(1));
		if (rs.getObject(2) != null) {
		    String v = SIGAFormatter.formatValue(rs.getDouble(2));
		    reversionData[1] = ValueFactory.createValue(v);
		} else {
		    reversionData[1] = ValueFactory.createNullValue();
		}
		if (rs.getObject(3) != null) {
		    String v = SIGAFormatter.formatValue(rs.getDouble(3));
		    reversionData[2] = ValueFactory.createValue(v);
		} else {
		    reversionData[2] = ValueFactory.createNullValue();
		}
		if (rs.getObject(4) != null) {
		    reversionData[3] = ValueFactory.createValue(rs.getInt(4));
		} else {
		    reversionData[3] = ValueFactory.createNullValue();
		}
		if (rs.getObject(5) != null) {
		    String v = SIGAFormatter.formatValue(rs.getDate(5));
		    reversionData[4] = ValueFactory.createValue(v);
		} else {
		    reversionData[4] = ValueFactory.createNullValue();
		}
		tableModel.addRow(reversionData);
		// Save current Fincas in order to remove them
		// from database when there is some change in the table model
		oldReversions.add(rs.getString(1));
	    }
	    repaint();
	    tableModel.addTableModelListener(this);
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    private DefaultTableModel setTableHeader() {
	CustomTableModel tableModel = new CustomTableModel();
	List<Field> columnasReversiones = new ArrayList<Field>();
	columnasReversiones.add(new Field("exp_id", "<html>Reversión</html>"));
	columnasReversiones.add(new Field("superficie",
		"<html>Superficie (m<sup>2</sup>)</html>"));
	columnasReversiones.add(new Field("importe_euros",
		"<html>Importe (&euro;)</html>"));
	columnasReversiones.add(new Field("importe_ptas",
		"<html>Importe (Ptas)</html>"));
	columnasReversiones.add(new Field("fecha_acta", "<html>Fecha</html>"));

	for (Field columnName : columnasReversiones) {
	    tableModel.addColumn(columnName);
	}

	return tableModel;
    }


    public void updatePMTable() {
	ArrayList<String> columnasPM = new ArrayList<String>();
	columnasPM.add(DBNames.FIELD_NUMPM_FINCAS_PM);

	try {
	    DefaultTableModel tableModel;
	    tableModel = new DefaultTableModel();
	    for (String columnName : columnasPM) {
		tableModel.addColumn(columnName);
	    }
	    pm.setModel(tableModel);
	    pm.setEnabled(false);
	    Value[] pmData = new Value[3];
	    PreparedStatement statement;
	    String query = "SELECT " + DBNames.FIELD_NUMPM_FINCAS_PM + " "
		    + "FROM " + DBNames.PM_SCHEMA + "."
		    + DBNames.TABLE_FINCAS_PM + " " + "WHERE "
		    + DBNames.FIELD_IDFINCA_FINCAS_PM + " = '" + getIDFinca()
		    + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    if (rs.next()) {
		if (afectado_pm.getItemCount() > 1) {
		    afectado_pm.setSelectedIndex(1);
		}
	    }
	    rs.beforeFirst();
	    while (rs.next()) {
		pmData[0] = ValueFactory.createValue(rs.getString(1));
		tableModel.addRow(pmData);
	    }
	    repaint();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }


    public void internalFrameDeactivated(InternalFrameEvent e) {
	updateJTables();
    }


    @Override
    public String getBasicName() {
	return TABLENAME;
    }

    @Override
    protected String getSchema() {
	return DBNames.SCHEMA_DATA;
    }

    @Override
    public void tableChanged(TableModelEvent e) {
	super.setChangedValues(true);
	super.saveB.setEnabled(true);
    }
    
    @Override
    public FormPanel getFormBody() {
        String name = isAmpliacion() ? getBasicName() + "_ampliacion" : getBasicName();
        if (formBody == null) {
            InputStream stream = getClass().getClassLoader()
                .getResourceAsStream("/forms/" + name + ".jfrm");
            if (stream == null) {
            stream = getClass().getClassLoader().getResourceAsStream(
                "/forms/" + name + ".xml");
            }
            try {
            formBody = new FormPanel(stream);
            } catch (FormException e) {
            e.printStackTrace();
            }
        }
        return formBody;
    
    }

}
