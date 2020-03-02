package es.icarto.gvsig.extgex.forms.reversions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.RowSorter.SortKey;
import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.commons.gui.OkCancelPanel;
import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.icarto.gvsig.commons.queries.Utils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgex.forms.expropiations.AddFincaAction;
import es.icarto.gvsig.extgex.navtable.NavTableComponentsFactory;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.icarto.gvsig.navtableforms.gui.CustomTableModel;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.navtable.contextualmenu.ChooseSortFieldDialog;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class FormReversions extends BasicAbstractForm {

    private static final Logger logger = Logger.getLogger(FormReversions.class);

    public static final String TABLENAME = "exp_rv";
    public static final String TOCNAME = "Reversiones";

    private JTable fincasAfectadas;
    private JTextField expId;
    private FormExpropiationsLauncher expropiationsLauncher;

    private final static List<String> ignoreColumns = Arrays.asList(new String[] { "gid", "the_geom", "geom", "orden",
            "municipio", "id", "num_reversion" });

    public FormReversions(FLyrVect layer, IGeometry insertedGeom) {
        super(layer);
        addNewButtonsToActionsToolBar();
    }

    private void addNewButtonsToActionsToolBar() {
        JPanel actionsToolBar = this.getActionsToolBar();
        NavTableComponentsFactory ntFactory = new NavTableComponentsFactory();
        JButton filesLinkB = ntFactory.getFilesLinkButton(layer, this);
        if (filesLinkB != null) {
            actionsToolBar.add(filesLinkB);
        }
        actionsToolBar.add(new JButton(new OpenWebAction(this, "rv")));
        JButton addFincaBt = new JButton(new AddFincaAction(layer, this));
        addFincaBt.getAction().putValue(Action.SHORT_DESCRIPTION, "Añadir Reversión");
        actionsToolBar.add(addFincaBt);

        this.getActionsToolBar().remove(saveB);
        this.getActionsToolBar().remove(removeB);
        this.getActionsToolBar().remove(undoB);
        this.getActionsToolBar().remove(copyPreviousB);
        this.getActionsToolBar().remove(copySelectedB);
    }

    @Override
    protected void addSorterButton() {
        java.net.URL imgURL = getClass().getClassLoader().getResource("sort.png");
        JButton jButton = new JButton(new ImageIcon(imgURL));
        jButton.setToolTipText("Ordenar registros");

        jButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                URL resource = this.getClass().getClassLoader().getResource("columns.properties");
                List<Field> fields = Utils.getFields(resource.getPath(), getSchema(), getBasicName(), ignoreColumns);

                ChooseSortFieldDialog dialog = new ChooseSortFieldDialog(fields);

                if (dialog.open().equals(OkCancelPanel.OK_ACTION_COMMAND)) {
                    List<Field> sortedFields = dialog.getFields();
                    List<SortKey> sortKeys = new ArrayList<SortKey>();
                    SelectableDataSource sds = getRecordset();
                    for (Field field : sortedFields) {
                        try {
                            int fieldIdx = sds.getFieldIndexByName(field.getKey());
                            sortKeys.add(new SortKey(fieldIdx, field.getSortOrder()));
                        } catch (ReadDriverException e1) {
                            logger.error(e1.getStackTrace(), e1);
                        }
                    }
                    setSortKeys(sortKeys);
                }
            }
        });
        getActionsToolBar().add(jButton);
    }

    @Override
    protected void enableSaveButton(boolean bool) {
        if (!isChangedValues()) {
            saveB.setEnabled(false);
        } else {
            saveB.setEnabled(bool);
        }
    }

    @Override
    protected void setListeners() {
        super.setListeners();

        Map<String, JComponent> widgets = getWidgets();

        expropiationsLauncher = new FormExpropiationsLauncher();
        fincasAfectadas = (JTable) widgets.get("tabla_fincas_afectadas");
        fincasAfectadas.addMouseListener(expropiationsLauncher);

        expId = (JTextField) widgets.get("exp_id");
    }

    private String getExpId() {
        return expId.getText();
    }

    @Override
    protected void removeListeners() {
        super.removeListeners();
        fincasAfectadas.removeMouseListener(expropiationsLauncher);
    }

    @Override
    protected void fillSpecificValues() {
        for (JComponent c : getWidgets().values()) {
            if (c != fincasAfectadas) {
                WidgetFactory.disableComponent(c);
            }
        }
        updateJTableFincasAfectadas();
    }

    private void updateJTableFincasAfectadas() {

        DefaultTableModel tableModel = setTableHeader();

        Double totalSuperficie = 0.0;
        Double totalImporteEuros = 0.0;
        Double totalImportePtas = 0.0;

        try {

            fincasAfectadas.setModel(tableModel);
            // fincasAfectadas.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            fincasAfectadas.getColumnModel().getColumn(0).setPreferredWidth(80);
            fincasAfectadas.getColumnModel().getColumn(1).setPreferredWidth(185);
            fincasAfectadas.getColumnModel().getColumn(2).setPreferredWidth(100);
            fincasAfectadas.getColumnModel().getColumn(5).setPreferredWidth(60);

            String[] reversionData = new String[tableModel.getColumnCount()];
            ResultSet rs = getFincasByExpReversion();

            while (rs.next()) {
                reversionData[0] = SIGAFormatter.formatValue(rs.getObject(1));
                reversionData[1] = getExpedientesPMByFinca(rs.getString(1));
                reversionData[2] = SIGAFormatter.formatValue(rs.getObject(2));
                if (rs.getObject(2) != null) {
                    totalSuperficie += rs.getDouble(2);
                }
                reversionData[3] = SIGAFormatter.formatValue(rs.getObject(3));
                if (rs.getObject(3) != null) {
                    totalImporteEuros += rs.getDouble(3);
                }
                reversionData[4] = SIGAFormatter.formatValue(rs.getObject(4));
                if (rs.getObject(4) != null) {
                    totalImportePtas += rs.getDouble(4);
                }
                reversionData[5] = SIGAFormatter.formatValue(rs.getObject(5));

                tableModel.addRow(reversionData);
            }
            reversionData[0] = "<html><b>" + "TOTAL" + "</b></html>";
            reversionData[1] = "";
            reversionData[2] = "<html><b>" + SIGAFormatter.formatValue(totalSuperficie) + "</b></html>";
            reversionData[3] = "<html><b>" + SIGAFormatter.formatValue(totalImporteEuros) + "</b></html>";
            reversionData[4] = "<html><b>" + SIGAFormatter.formatValue(totalImportePtas) + "</b></html>";
            reversionData[5] = "";
            tableModel.addRow(reversionData);
            repaint();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private DefaultTableModel setTableHeader() {
        CustomTableModel tableModel = new CustomTableModel();
        ArrayList<Field> columnasFincas = new ArrayList<Field>();
        columnasFincas.add(new Field("id_finca", "<html>Finca</html>"));
        columnasFincas.add(new Field("expedientes_pm", "<html>Expedientes PM</html>"));
        columnasFincas.add(new Field("superficie", "<html>Superficie (m<sup>2</sup>)</html>"));
        columnasFincas.add(new Field("importe_euros", "<html>Importe (&euro;)</html>"));
        columnasFincas.add(new Field("importe_ptas", "<html>Importe (Pts)</html>"));
        columnasFincas.add(new Field("fecha_acta", "<html>Fecha</html>"));

        for (Field columnName : columnasFincas) {
            tableModel.addColumn(columnName);
        }
        return tableModel;
    }

    private ResultSet getFincasByExpReversion() throws SQLException {
        PreparedStatement statement;
        String query = "SELECT " + DBNames.FIELD_IDEXPROPIACION_FINCA_REVERSION + ", "
                + DBNames.FIELD_SUPERFICIE_FINCA_REVERSION + ", " + DBNames.FIELD_IMPORTE_FINCA_REVERSION_EUROS + ", "
                + DBNames.FIELD_IMPORTE_FINCA_REVERSION_PTAS + ", " + DBNames.FIELD_FECHA_FINCA_REVERSION + " "
                + "FROM " + DBNames.SCHEMA_DATA + "." + DBNames.TABLE_FINCA_REVERSION + " " + "WHERE "
                + DBNames.FIELD_IDREVERSION_FINCA_REVERSION + " = '" + getExpId() + "';";
        statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query);
        statement.execute();
        ResultSet rs = statement.getResultSet();
        return rs;
    }

    private String getExpedientesPMByFinca(String idFinca) {
        String expedientesPM = "";
        PreparedStatement statement;
        String query = "SELECT " + DBNames.FIELD_NUMPM_FINCAS_PM + " " + "FROM " + DBNames.PM_SCHEMA + "."
                + DBNames.TABLE_FINCAS_PM + " " + "WHERE " + DBNames.FIELD_IDFINCA_FINCAS_PM + " = '" + idFinca + "';";
        try {
            statement = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query);
            statement.execute();
            ResultSet rs = statement.getResultSet();
            while (rs.next()) {
                expedientesPM = expedientesPM + rs.getString(1) + "/";
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return expedientesPM;
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
    // As this is a non editable form we should never show the warning
    protected boolean showWarning() {
        return true;
    }

}
