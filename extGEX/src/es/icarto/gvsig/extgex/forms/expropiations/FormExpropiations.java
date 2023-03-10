package es.icarto.gvsig.extgex.forms.expropiations;

import java.awt.event.ActionEvent;
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
import javax.swing.JLabel;
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
import com.iver.cit.gvsig.exceptions.visitors.StopWriterVisitorException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.jeta.forms.components.panel.FormPanel;
import com.jeta.forms.gui.common.FormException;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.commons.utils.StrUtils;
import es.icarto.gvsig.extgex.navtable.NavTableComponentsFactory;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.utils.retrievers.LocalizadorFormatter;
import es.icarto.gvsig.extgia.forms.GIAAlphanumericTableHandler;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.icarto.gvsig.navtableforms.gui.CustomTableModel;
import es.icarto.gvsig.navtableforms.gui.tables.AbstractSubForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.icarto.gvsig.siga.models.InfoEmpresaGEX;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class FormExpropiations extends BasicAbstractForm implements TableModelListener {

    private static final Logger logger = Logger.getLogger(AbstractForm.class);

    private static final String EXPROPIATIONS_AFECTADO_PM = "afectado_por_policia_margenes";

    public static final String TABLENAME = "exp_finca";
    public static final String TOCNAME = "Fincas";
    public static final String TOCNAME_AMPLIACION = "Fincas_Ampliacion";
    public static final String TOCNAME_AUTOESTRADAS = "Fincas_Autoestradas";
    public static final String PKFIELD = "id_finca";

    private static final String WIDGET_REVERSIONES = "tabla_reversiones_afectan";
    private static final String WIDGET_PM = "tabla_pm_afectan";

    public static final String[] bienesAfectadosColNames = { "superficie", "tipo" };
    public static final String[] bienesAfectadosColAlias = { "<html>Superficie (m<sup>2</sup>)</html>", "Tipo" };
    public static final int[] bienesAfectadosColWidths = { 100, 100 };

    private final JComboBox tramo;
    private final JComboBox uc;
    private final JComboBox ayuntamiento;
    private final JComboBox subtramo;
    private final JTextField finca;
    private final JTextField numFinca;
    private final JTextField seccion;

    private JTable reversiones;
    private JTable pm;

    private JComboBox afectado_pm;

    private UpdateNroFincaHandler updateNroFincaHandler;

    private FormReversionsLauncher formReversionsLauncher;

    private ArrayList<String> oldReversions;

    private final GIAAlphanumericTableHandler bienesExpropiadosTableHandler;
    private TableModelListener bienesExpropiadosTableModelListener;

    private GIAAlphanumericTableHandler procesosTableHandler;
    private TableModelListener procesosTableModelListener;

    private JLabel empresaLb;
    private JLabel concesionariaLb;

    private final InfoEmpresa infoEmpresa;

    public FormExpropiations(FLyrVect layer, IGeometry insertedGeom) {
        super(layer);
        ExpropiationsLayerResolver.removeNotAppropiateTramos(this.getOrmlite(), layer);

        AbstractSubForm bienesExpropiadosSubForm = new BienesAfectadosSubForm();
        bienesExpropiadosTableHandler = new GIAAlphanumericTableHandler("bienes_afectados", getWidgets(),
                getElementID(), bienesAfectadosColNames, bienesAfectadosColAlias, bienesAfectadosColWidths, this,
                bienesExpropiadosSubForm);
        addTableHandler(bienesExpropiadosTableHandler);

        setTitle(layer.getName());
        Map<String, JComponent> widgets = getWidgets();

        if (ExpropiationsLayerResolver.getBaseName(layer).equals(TABLENAME)) {
            addCalculation(new ImportePendienteTotalAutocalculado(this));
            addCalculation(new ImportePagadoTotalAutocalculado(this));
        } else {
            procesosTableHandler = new GIAAlphanumericTableHandler("procesos", widgets, getElementID(),
                    ProcesosSubForm.colNames, ProcesosSubForm.colAlias, ProcesosSubForm.colWidths, this,
                    ProcesosSubForm.class);
            addTableHandler(procesosTableHandler);
            // importe_pagado_total_autocalculado - Cuando es ampliaci?n/autoestradas Trigger + Listener
        }
        // superficie_expropiada_total_autocalculado - Trigger + Workaround con un listener a la tabla y un refresh

        addButtonsToActionsToolBar();

        tramo = (JComboBox) widgets.get(DBNames.FIELD_TRAMO_FINCAS);
        uc = (JComboBox) widgets.get(DBNames.FIELD_UC_FINCAS);
        ayuntamiento = (JComboBox) widgets.get(DBNames.FIELD_AYUNTAMIENTO_FINCAS);
        subtramo = (JComboBox) widgets.get(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS);
        numFinca = (JTextField) widgets.get(DBNames.FIELD_NUMEROFINCA_FINCAS);
        seccion = (JTextField) widgets.get(DBNames.FIELD_SECCION_FINCAS);
        finca = (JTextField) widgets.get(DBNames.FIELD_IDFINCA);

        addChained(DBNames.FIELD_UC_FINCAS, DBNames.FIELD_TRAMO_FINCAS);
        addChained(DBNames.FIELD_AYUNTAMIENTO_FINCAS, DBNames.FIELD_UC_FINCAS);
        addChained(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS, DBNames.FIELD_UC_FINCAS, DBNames.FIELD_AYUNTAMIENTO_FINCAS);

        infoEmpresa = new InfoEmpresaGEX();

    }

    @Override
    protected void initWidgets() {
        super.initWidgets();
        empresaLb = getFormPanel().getLabel("etiqueta_empresa");
        concesionariaLb = getFormPanel().getLabel("etiqueta_concesion");
    }

    private void addButtonsToActionsToolBar() {
        JPanel actionsToolBar = this.getActionsToolBar();
        NavTableComponentsFactory ntFactory = new NavTableComponentsFactory();
        JButton filesLinkB = ntFactory.getFilesLinkButton(layer, this);
        String reportName = ExpropiationsLayerResolver.getBaseName(layer);
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
        Map<String, JComponent> widgets = getWidgets();
        reversiones = (JTable) widgets.get(WIDGET_REVERSIONES);
        pm = (JTable) widgets.get(WIDGET_PM);

        afectado_pm = (JComboBox) widgets.get(EXPROPIATIONS_AFECTADO_PM);

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

        subtramo.removeActionListener(updateNroFincaHandler);
        numFinca.removeKeyListener(updateNroFincaHandler);
        seccion.removeKeyListener(updateNroFincaHandler);

        reversiones.removeMouseListener(formReversionsLauncher);
    }

    private void setIDFinca() {

        if ((tramo.getSelectedItem() instanceof KeyValue) && (uc.getSelectedItem() instanceof KeyValue)
                && (ayuntamiento.getSelectedItem() instanceof KeyValue)
                && (subtramo.getSelectedItem() instanceof KeyValue)) {
            // will update id_finca only when comboboxes have proper values
            String id_finca = LocalizadorFormatter.getTramo(((KeyValue) tramo.getSelectedItem()).getKey())
                    + LocalizadorFormatter.getUC(((KeyValue) uc.getSelectedItem()).getKey())
                    + LocalizadorFormatter.getAyuntamiento(((KeyValue) ayuntamiento.getSelectedItem()).getKey())
                    + LocalizadorFormatter.getSubtramo(((KeyValue) subtramo.getSelectedItem()).getKey())
                    + getStringNroFincaFormatted() + getStringSeccionFormatted();
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
            String formatted = LocalizadorFormatter.getNroFinca(values.get(DBNames.FIELD_NUMEROFINCA_FINCAS));
            numFinca.setText(formatted);
            getFormController().setValue(DBNames.FIELD_NUMEROFINCA_FINCAS, formatted);
            return formatted;
        } catch (NumberFormatException nfe) {
            numFinca.setText(LocalizadorFormatter.FINCA_DEFAULT_VALUE);
            getFormController().setValue(DBNames.FIELD_NUMEROFINCA_FINCAS, LocalizadorFormatter.FINCA_DEFAULT_VALUE);
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

        if (!ExpropiationsLayerResolver.getBaseName(layer).equals(TABLENAME)) {
            updateAutomaticFieldImporteWhenSubFormDataChanges_pre();
        }
        super.fillSpecificValues();

        updateJTables();

        updateAutomaticFieldSuperficieWhenSubFormDataChanges_post();
        if (!ExpropiationsLayerResolver.getBaseName(layer).equals(TABLENAME)) {
            updateAutomaticFieldImporteWhenSubFormDataChanges_post();
        }

        fillEmpresaLB();

    }

    /*
     * El campo superficie_expropiada_total_autocalculado debe actualizarse cuando cambien los
     * datos del subformulario/tabla (a?adir, editar o eliminar) Hay varias formas de intentar
     * implementar esto:
     * 
     * * Por ejemplo usar NOTIFY en un trigger de bd y en el formulario padre escuchar el canal con LISTEN
     * https://jdbc.postgresql.org/documentation/81/listennotify.html
     * https://tapoueh.org/blog/2018/07/postgresql-listen/notify/
     * 
     * * Trigger para calcular en bd e implementando windowClosed en el subformulario m?s o menos as?
     * el problema es que no funciona al borrar una fila porque el form no llega a abrirse
     * 
     * @Override
     * public void windowClosed() {
     * super.windowClosed();
     * 
     * No llamamos a parentForm.refresh porque ni el reloadRecordset
     * ni el refresGUI por si mismos hacen que se actualice el LayerController
     * de este modo refreshGUI lee los valores de un LayerController que no
     * est? actualizado pero onPositionChange si que lo actualiza
     * 
     * 
     * }
     * 
     * Aqu? lo implementamos como un listener sobre la propia tabla. Va en fillSpecificValues porqu?
     * el TableHandler puede acabar creando un nuevo modelo de datos en este m?todo y esta rara construcci?n
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

    private void updateAutomaticFieldImporteWhenSubFormDataChanges_pre() {
        if (procesosTableModelListener != null) {
            procesosTableHandler.getModel().removeTableModelListener(procesosTableModelListener);
        }
    }

    private void updateAutomaticFieldImporteWhenSubFormDataChanges_post() {
        procesosTableModelListener = new TableModelListener() {

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
        procesosTableHandler.getModel().addTableModelListener(procesosTableModelListener);

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
            String query = "SELECT " + DBNames.FIELD_IDREVERSION_FINCA_REVERSION + ", "
                    + DBNames.FIELD_SUPERFICIE_FINCA_REVERSION + ", " + DBNames.FIELD_IMPORTE_FINCA_REVERSION_EUROS
                    + ", " + DBNames.FIELD_IMPORTE_FINCA_REVERSION_PTAS + ", " + DBNames.FIELD_FECHA_FINCA_REVERSION
                    + " " + "FROM " + DBNames.SCHEMA_DATA + "." + DBNames.TABLE_FINCA_REVERSION + " " + "WHERE "
                    + DBNames.FIELD_IDEXPROPIACION_FINCA_REVERSION + " = '" + getIDFinca() + "';";
            statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query);
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
        columnasReversiones.add(new Field("exp_id", "<html>Reversi?n</html>"));
        columnasReversiones.add(new Field("superficie", "<html>Superficie (m<sup>2</sup>)</html>"));
        columnasReversiones.add(new Field("importe_euros", "<html>Importe (&euro;)</html>"));
        columnasReversiones.add(new Field("importe_ptas", "<html>Importe (Ptas)</html>"));
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
            String query = "SELECT " + DBNames.FIELD_NUMPM_FINCAS_PM + " " + "FROM " + DBNames.PM_SCHEMA + "."
                    + DBNames.TABLE_FINCAS_PM + " " + "WHERE " + DBNames.FIELD_IDFINCA_FINCAS_PM + " = '"
                    + getIDFinca() + "';";
            statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query);
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
        String name = ExpropiationsLayerResolver.getBaseName(this.layer);
        if (formBody == null) {
            InputStream stream = getClass().getClassLoader().getResourceAsStream("/forms/" + name + ".jfrm");
            if (stream == null) {
                stream = getClass().getClassLoader().getResourceAsStream("/forms/" + name + ".xml");
            }
            try {
                formBody = new FormPanel(stream);
            } catch (FormException e) {
                e.printStackTrace();
            }
        }
        return formBody;

    }

    private void fillEmpresaLB() {
        String tramoKey = ((KeyValue) tramo.getSelectedItem()).getKey();
        empresaLb.setText(infoEmpresa.getTitle(tramoKey));
        concesionariaLb.setText(infoEmpresa.getSubtitle(tramoKey));
    }

    @Override
    public boolean saveRecord() throws StopWriterVisitorException {

        /*
         * Workaround. Para forzar al usuario a escoger un tramo (si no no se rellena el LayerController se meten
         * valores void a todos los combos. Para parroquia, por alg?n bug probablemente en
         * FillHandler.setDomainValueSelected no se rellena bien el valor cuando est? a nulo
         */
        String subtramoV = getFormController().getValue(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS);
        if (StrUtils.isEmptyString(subtramoV)) {
            getFormController().setValue(DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS,
                    LocalizadorFormatter.SUBTRAMO_DEFAULT_VALUE);
        }
        return super.saveRecord();
    }
}
