package es.icarto.gvsig.extpm.forms;

import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableModel;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.gui.WidgetFactory;
import es.icarto.gvsig.extgex.forms.reversions.OpenWebAction;
import es.icarto.gvsig.extpm.forms.filesLink.NavTableComponentsFilesLinkButton;
import es.icarto.gvsig.navtableforms.BasicAbstractForm;
import es.icarto.gvsig.siga.SIGAConfigExtension;
import es.icarto.gvsig.siga.forms.reports.NavTableComponentsPrintButton;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class FormPM extends BasicAbstractForm {
    
    public static final String TOCNAME = "Policia_Margenes";
    public static final String SCHEMA = "audasa_pm";
    public static final String TABLENAME = "exp_pm";

    private static final String PM_FIELD_NUMEROPM = "exp_id";
    private static final String FINCAS_TABLENAME = "audasa_expropiaciones.exp_finca";
    private static final String FINCAS_FIELD_TRAMO = "tramo";
    private static final String FINCAS_FIELD_IDFINCA = "id_finca";
    private static final String TRAMOS_TABLENAME = "audasa_expropiaciones.tramos";
    private static final String TRAMOS_FIELD_ID = "id_tramo";
    private static final String FINCAS_PM_TABLENAME = "audasa_pm.fincas_pm";
    private static final String FINCAS_PM_FIELD_IDFINCA = "id_finca";
    private static final String FINCAS_PM_FIELD_NUMEROPM = "numero_pm";

    private JTextField numeroPM;
    private JTable fincasAfectadasTable;
    private JButton printReportB;
    private URL reportPath;

    private NavTableComponentsFilesLinkButton ntFilesLinkButton;
    private NavTableComponentsPrintButton ntPrintButton;
    private JButton openWebBt;
    private final JTextField tramoCB;
    private final JLabel empresaLb;
    private final JLabel concesionariaLb;
    private final InfoEmpresa infoEmpresa;

    public FormPM(FLyrVect layer) {
	super(layer);
	SIGAConfigExtension ext = (SIGAConfigExtension) PluginServices
		.getExtension(SIGAConfigExtension.class);
	infoEmpresa = ext.getInfoEmpresa();
	tramoCB = getFormPanel().getTextField("loc_tramo");
	empresaLb = getFormPanel().getLabel("etiqueta_empresa");
	concesionariaLb = getFormPanel().getLabel("etiqueta_concesion");
    }

    private void addNewButtonsToActionsToolBar() {
	reportPath = this.getClass().getClassLoader()
		.getResource("reports/pm_report.jasper");
	JPanel actionsToolBar = this.getActionsToolBar();
	ntFilesLinkButton = new NavTableComponentsFilesLinkButton();
	ntPrintButton = new NavTableComponentsPrintButton();
	JButton filesLinkB = ntFilesLinkButton.getFilesLinkButton(layer, this);

	printReportB = ntPrintButton.getPrintButton(reportPath.getPath(), SCHEMA + "." + TABLENAME,
		PM_FIELD_NUMEROPM, numeroPM.getText());
	printReportB.setName("printButton");

	if (openWebBt == null) {
	    openWebBt = new JButton(new OpenWebAction(this, "pm"));
	    actionsToolBar.add(openWebBt);
	}

	if (printReportB != null) {
	    for (int i = 0; i < this.getActionsToolBar().getComponents().length; i++) {
		if (getActionsToolBar().getComponents()[i].getName() != null) {
		    if (getActionsToolBar().getComponents()[i].getName()
			    .equalsIgnoreCase("printButton")) {
			this.getActionsToolBar().remove(
				getActionsToolBar().getComponents()[i]);
			actionsToolBar.add(printReportB);
			break;
		    }
		}
	    }
	    actionsToolBar.add(printReportB);
	}

	if (filesLinkB != null && ntFilesLinkButton == null) {
	    actionsToolBar.add(filesLinkB);
	}

	this.getActionsToolBar().remove(saveB);
	this.getActionsToolBar().remove(removeB);
	this.getActionsToolBar().remove(undoB);
	this.getActionsToolBar().remove(copyPreviousB);
	this.getActionsToolBar().remove(copySelectedB);
    }

    @Override
    protected void setListeners() {
	super.setListeners();

	Map<String, JComponent> widgets = getWidgets();

	numeroPM = (JTextField) widgets.get(PM_FIELD_NUMEROPM);

	fincasAfectadasTable = (JTable) formBody
		.getComponentByName("parcelas_afectadas_table");
    }

    @Override
    protected void fillSpecificValues() {
	super.fillSpecificValues();
	createFincasAfectadasTable();

	for (String finca : getFincasAfectadas()) {
	    String[] fincaValues = getFincaValuesFromID(finca);
	    ((DefaultTableModel) fincasAfectadasTable.getModel())
		    .addRow(fincaValues);
	}
	for (JComponent c : getWidgets().values()) {
	    WidgetFactory.disableComponent(c);
	}
	addNewButtonsToActionsToolBar();
	fillEmpresaLB();
    }
    
    private void fillEmpresaLB() {
	String tramo = tramoCB.getText();
	empresaLb.setText(infoEmpresa.getTitle(tramo));
	concesionariaLb.setText(infoEmpresa.getSubtitle(tramo));
    }

    private String[] getFincaValuesFromID(String idFinca) {
	PreparedStatement statement;
	String[] fincaValues = new String[1];
	try {
	    String query = "SELECT " + FINCAS_FIELD_IDFINCA
		    + " FROM " + FINCAS_TABLENAME + " a, "
		    + TRAMOS_TABLENAME + " b " + " WHERE "
		    + FINCAS_FIELD_TRAMO + " = "
		    + TRAMOS_FIELD_ID + " AND "
		    + FINCAS_FIELD_IDFINCA + "=" + "'" + idFinca
		    + "';";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();

	    while (rs.next()) {
		fincaValues[0] = rs.getString(1);
	    }
	    rs.close();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return fincaValues;
    }

    private void createFincasAfectadasTable() {
	DefaultTableModel model = new DefaultTableModel();
	model.addColumn("ID Finca");

	fincasAfectadasTable.setTableHeader(null);
	fincasAfectadasTable.setModel(model);
	fincasAfectadasTable.setEnabled(false);
    }

    private ArrayList<String> getFincasAfectadas() {
	ArrayList<String> fincasAfectadas = new ArrayList<String>();
	PreparedStatement statement;
	try {
	    String fincasQuery = "SELECT "
		    + FINCAS_PM_FIELD_IDFINCA + " FROM "
		    + FINCAS_PM_TABLENAME + " WHERE "
		    + FINCAS_PM_FIELD_NUMEROPM + " = '"
		    + numeroPM.getText() + "'";
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(fincasQuery);
	    statement.execute();
	    ResultSet fincasRs = statement.getResultSet();
	    while (fincasRs.next()) {
		fincasAfectadas.add(fincasRs.getString(1));
	    }
	    return fincasAfectadas;
	} catch (SQLException e) {
	    e.printStackTrace();
	    return new ArrayList<String>();
	}
    }

    @Override
    // As this is a non editable form we should never show the warning
    protected boolean showWarning() {
	return true;
    }

    @Override
    protected String getBasicName() {
	return TABLENAME;
    }

    @Override
    protected String getSchema() {
	return SCHEMA;
    }
}
