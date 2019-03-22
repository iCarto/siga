package es.icarto.gvsig.extgex.locators;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.parser.ParseException;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.commons.gui.BasicAbstractWindow;
import es.icarto.gvsig.extgex.forms.expropiations.FormExpropiations;
import es.icarto.gvsig.extgex.locators.actions.FormOpener;
import es.icarto.gvsig.extgex.locators.actions.ZoomToHandler;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.queries.QueriesPanel;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.elle.constants.IPositionRetriever;
import es.udc.cartolab.gvsig.elle.utils.ELLEMap;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class LocatorByFinca extends BasicAbstractWindow implements
IPositionRetriever {

    private static final String TABLE_MUNICIPIO_TRAMOS = "municipio_tramos";
    private static final String FIELD_MUNICIPIO_MUNICIPIO_TRAMOS = "municipio";

    private static final Logger logger = Logger.getLogger(QueriesPanel.class);

    private static final String DEFAULT_FILTER = "--SELECCIONAR--";

    private JComboBox tramo;
    private JComboBox uc;
    private JComboBox ayuntamiento;
    private JComboBox parroquiaSubtramo;
    private JComboBox fincaSeccion;
    private JButton zoomBt;
    private JButton openForm;

    private final ZoomToHandler zoomToHandler;
    private final FormOpener formOpener;

    // Filters
    String tramoSelected = DEFAULT_FILTER;
    String ucSelected = null;
    String ayuntamientoSelected = null;
    String parroquiaSelected = null;

    // Listeners
    TramoListener tramoListener;
    UCListener ucListener;
    AyuntamientoListener ayuntamientoListener;
    ParroquiaListener parroquiaListener;
    FincasListener fincasListener;

    DBSession dbs;
    String[][] tramos;

    public LocatorByFinca() {
	super();
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
	this.setWindowTitle("Localizar Finca");
	dbs = DBSession.getCurrentSession();
	initWidgets();
	initListeners();
	final List<String> constants = ELLEMap.getConstantValuesSelected();
	try {
	    if (constants.isEmpty()) {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDTRAMO;
		String[][] tramos = dbs.getTable(DBNames.TABLE_TRAMOS,
			DBNames.SCHEMA_DATA, order, false);
		for (int i = 0; i < tramos.length; i++) {
		    tramo.addItem(tramos[i][1]);
		}
	    } else {
		Vector<String> tramos = getTramosFromMunicipios(constants);
		for (int i = 0; i < tramos.size(); i++) {
		    tramo.addItem(tramos.elementAt(i));
		}
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}

	zoomToHandler = new ZoomToHandler(this, true);
	zoomBt.addActionListener(zoomToHandler);

	formOpener = new FormOpener(this);
	openForm.addActionListener(formOpener);
    }

    @Override
    protected String getBasicName() {
	return "LocatorByFinca";
    }

    private Vector<String> getTramosFromMunicipios(List<String> municipios) {
	PreparedStatement statement;
	String query = null;
	try {
	    if (municipios.size() == 1) {
		query = "SELECT distinct " + DBNames.FIELD_NOMBRETRAMO_TRAMOS
			+ "," + "b.id_tramo " + "FROM "
			+ DBNames.EXPROPIATIONS_SCHEMA + "."
			+ DBNames.TABLE_TRAMOS + " a, "
			+ DBNames.EXPROPIATIONS_SCHEMA + "."
			+ TABLE_MUNICIPIO_TRAMOS + " b "
			+ "WHERE a.id_tramo = b.id_tramo and "
			+ FIELD_MUNICIPIO_MUNICIPIO_TRAMOS + " = " + "'"
			+ municipios.get(0) + "' order by b.id_tramo;";
	    } else {
		query = "SELECT distinct " + DBNames.FIELD_NOMBRETRAMO_TRAMOS
			+ "," + "b.id_tramo " + "FROM "
			+ DBNames.EXPROPIATIONS_SCHEMA + "."
			+ DBNames.TABLE_TRAMOS + " a, "
			+ DBNames.EXPROPIATIONS_SCHEMA + "."
			+ TABLE_MUNICIPIO_TRAMOS + " b "
			+ "WHERE a.id_tramo = b.id_tramo and "
			+ FIELD_MUNICIPIO_MUNICIPIO_TRAMOS + " in (";
		for (int i = 0; i < municipios.size(); i++) {
		    query = query + "'" + municipios.get(i) + "',";
		}
		query = query.substring(0, query.length() - 1)
			+ ") order by b.id_tramo;";
	    }

	    statement = dbs.getJavaConnection().prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    Vector<String> tramos = new Vector<String>();
	    while (rs.next()) {
		tramos.add(rs.getString(1));
	    }
	    rs.close();
	    return tramos;
	} catch (SQLException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    private void initWidgets() {
	tramo = (JComboBox) formPanel.getComponentByName("tramo");
	tramo.addItem(new String(DEFAULT_FILTER));
	tramo.setSelectedIndex(1);
	uc = (JComboBox) formPanel.getComponentByName("unidad_constructiva");
	ayuntamiento = (JComboBox) formPanel.getComponentByName("ayuntamiento");
	parroquiaSubtramo = (JComboBox) formPanel
		.getComponentByName("parroquia_subtramo");
	fincaSeccion = (JComboBox) formPanel
		.getComponentByName("finca_seccion");
	openForm = (JButton) formPanel.getComponentByName("openform");
	zoomBt = (JButton) formPanel.getComponentByName("zoom");
    }

    private void initListeners() {
	tramoListener = new TramoListener();
	tramo.addActionListener(tramoListener);
	ucListener = new UCListener();
	uc.addActionListener(ucListener);
	ayuntamientoListener = new AyuntamientoListener();
	ayuntamiento.addActionListener(ayuntamientoListener);
	parroquiaListener = new ParroquiaListener();
	parroquiaSubtramo.addActionListener(parroquiaListener);
    }

    public class TramoListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    tramoSelected = tramo.getSelectedItem().toString();
	    updateUCDependingOnTramo();

	}
    }

    public class UCListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    ucSelected = (String) uc.getSelectedItem();
	    if (ucSelected != null) {
		updateAyuntamientoDependingOnUC();
	    }
	}
    }

    public class AyuntamientoListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent e) {
	    ayuntamientoSelected = (String) ayuntamiento.getSelectedItem();
	    if (ayuntamientoSelected != null) {
		updateParroquiaDependingOnAyuntamientoAndUC();
		updateFincaDependingOnAyuntamientoAndUCAndTramo();
	    }
	}
    }

    public class ParroquiaListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    if (!parroquiaSubtramo.isEnabled()) {
		parroquiaSelected = null;
	    } else {
		parroquiaSelected = (String) parroquiaSubtramo
			.getSelectedItem();
		updateFincaDependingOnAyuntamientoAndUCAndTramo();
	    }
	}
    }

    public class FincasListener implements ActionListener {
	@Override
	public void actionPerformed(ActionEvent e) {
	    updateFincaDependingOnAyuntamientoAndUCAndTramo();
	}
    }

    private void updateUCDependingOnTramo() {

	if (tramoSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    uc.removeAllItems();
	    uc.addItem(new String(""));
	} else {
	    try {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDUC;
		String tramoSelectedId = getTramoId();
		String whereClause = DBNames.FIELD_IDTRAMO + " = " + "'"
			+ tramoSelectedId + "'";
		String[][] ucs = dbs.getTable(DBNames.TABLE_UC,
			DBNames.SCHEMA_DATA, whereClause, order, false);
		uc.removeAllItems();
		for (int i = 0; i < ucs.length; i++) {
		    uc.addItem(ucs[i][2]);
		}
	    } catch (SQLException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    private void updateAyuntamientoDependingOnUC() {

	if (tramoSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    ayuntamiento.removeAllItems();
	    ayuntamiento.addItem(new String(""));
	} else {
	    try {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDAYUNTAMIENTO;
		String ucSelectedId = getUcId();
		String whereClause = DBNames.FIELD_IDUC + " = " + "'"
			+ ucSelectedId + "'";
		String[][] ayuntamientos = dbs.getTable(
			DBNames.TABLE_AYUNTAMIENTOS, DBNames.SCHEMA_DATA,
			whereClause, order, false);
		ayuntamiento.removeAllItems();
		for (int i = 0; i < ayuntamientos.length; i++) {
		    ayuntamiento.addItem(ayuntamientos[i][2]);
		}
	    } catch (SQLException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    private void updateParroquiaDependingOnAyuntamientoAndUC() {

	if (ayuntamientoSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    parroquiaSubtramo.removeAllItems();
	    parroquiaSubtramo.setEnabled(false);
	} else {
	    try {
		String[] order = new String[1];
		order[0] = DBNames.FIELD_IDPARROQUIA;
		String ucSelectedId = getUcId();
		String ayuntamientoSelectedId = getAyuntamientoId();
		String whereClause = DBNames.FIELD_IDAYUNTAMIENTO + " = " + "'"
			+ ayuntamientoSelectedId + "'" + " and "
			+ DBNames.FIELD_IDUC + " = " + "'" + ucSelectedId + "'";
		String[][] parroquias = dbs.getTable(
			DBNames.TABLE_PARROQUIASSUBTRAMOS, DBNames.SCHEMA_DATA,
			whereClause, order, false);
		if (parroquias.length <= 0) {
		    parroquiaSubtramo.setEnabled(false);
		    parroquiaSelected = null;
		    parroquiaSubtramo.removeAllItems();
		} else if (parroquias.length == 1
			&& parroquias[0][0].compareToIgnoreCase("0") == 0) {
		    parroquiaSubtramo.setEnabled(false);
		    parroquiaSelected = null;
		    parroquiaSubtramo.removeAllItems();
		} else if (parroquias[0][0].compareToIgnoreCase("0") == 0) {
		    parroquiaSubtramo.setEnabled(true);
		    parroquiaSubtramo.removeAllItems();
		    for (int i = 1; i < parroquias.length; i++) {
			parroquiaSubtramo.addItem(parroquias[i][3]);
		    }
		} else {
		    parroquiaSubtramo.setEnabled(true);
		    parroquiaSubtramo.removeAllItems();
		    for (int i = 0; i < parroquias.length; i++) {
			parroquiaSubtramo.addItem(parroquias[i][3]);
		    }
		}
	    } catch (SQLException e) {
		logger.error(e.getMessage(), e);
	    }
	}
    }

    private void updateFincaDependingOnAyuntamientoAndUCAndTramo() {
	if (tramoSelected.compareToIgnoreCase(DEFAULT_FILTER) == 0) {
	    fincaSeccion.removeAllItems();
	    ayuntamiento.addItem(new String(""));
	} else {
	    String query = null;
	    PreparedStatement statement;
	    fincaSeccion.removeAllItems();
	    try {
		if ((parroquiaSelected != null)
			&& (!parroquiaSelected.equalsIgnoreCase(DEFAULT_FILTER))) {
		    query = "SELECT " + DBNames.FIELD_NUMEROFINCA_FINCAS + ", "
			    + DBNames.FIELD_SECCION_FINCAS + " FROM "
			    + DBNames.EXPROPIATIONS_SCHEMA + "."
			    + FormExpropiations.TABLENAME + " WHERE "
			    + DBNames.FIELD_TRAMO_FINCAS + " = " + "'"
			    + getTramoId() + "' AND " + DBNames.FIELD_UC_FINCAS
			    + " = " + "'" + getUcId() + "' AND "
			    + DBNames.FIELD_AYUNTAMIENTO_FINCAS + " = " + "'"
			    + getAyuntamientoId() + "' AND "
			    + DBNames.FIELD_PARROQUIASUBTRAMO_FINCAS + " = "
			    + "'" + getParroquiaId() + "' ORDER BY "
			    + DBNames.FIELD_NUMEROFINCA_FINCAS;
		} else {
		    query = "SELECT " + DBNames.FIELD_NUMEROFINCA_FINCAS + ", "
			    + DBNames.FIELD_SECCION_FINCAS + " FROM "
			    + DBNames.EXPROPIATIONS_SCHEMA + "."
			    + FormExpropiations.TABLENAME + " WHERE "
			    + DBNames.FIELD_TRAMO_FINCAS + " = " + "'"
			    + getTramoId() + "' AND " + DBNames.FIELD_UC_FINCAS
			    + " = " + "'" + getUcId() + "' AND "
			    + DBNames.FIELD_AYUNTAMIENTO_FINCAS + " = " + "'"
			    + getAyuntamientoId() + "' ORDER BY "
			    + DBNames.FIELD_NUMEROFINCA_FINCAS;
		    ;
		}
		statement = dbs.getJavaConnection().prepareStatement(query);
		statement.execute();
		ResultSet rs = statement.getResultSet();
		while (rs.next()) {
		    String value = rs
			    .getString(DBNames.FIELD_NUMEROFINCA_FINCAS)
			    + "-"
			    + rs.getString(DBNames.FIELD_SECCION_FINCAS);
		    fincaSeccion.addItem(value);
		}
		rs.close();
	    } catch (SQLException e) {
		logger.error(query, e);
		e.printStackTrace();
	    }
	}
    }

    private String getTramoId() throws SQLException {
	String whereSQL = DBNames.FIELD_NOMBRETRAMO_TRAMOS + " = " + "'"
		+ tramoSelected + "'";
	String tramoId = getIDFromCB(DBNames.FIELD_IDTRAMO,
		DBNames.TABLE_TRAMOS, whereSQL);
	return tramoId;
    }

    private String getUcId() throws SQLException {
	String whereSQL = DBNames.FIELD_NOMBREUC_UC + " = " + "'" + ucSelected
		+ "'";
	String uc = getIDFromCB(DBNames.FIELD_IDUC, DBNames.TABLE_UC, whereSQL);
	return uc;
    }

    private String getAyuntamientoId() throws SQLException {
	String whereSQL = DBNames.FIELD_IDUC + " = " + "'" + getUcId() + "'"
		+ " and " + DBNames.FIELD_NOMBREAYUNTAMIENTO_AYUNTAMIENTO
		+ " = " + "'" + ayuntamientoSelected + "'";
	String ayuntamiento = getIDFromCB(DBNames.FIELD_IDAYUNTAMIENTO,
		DBNames.TABLE_AYUNTAMIENTOS, whereSQL);
	return ayuntamiento;
    }

    private String getParroquiaId() throws SQLException {
	if (parroquiaSelected == null) {
	    return "0";
	} else {
	    String whereSQL = DBNames.FIELD_NOMBREPARROQUIA_PARROQUIASUBTRAMOS
		    + " = " + "'" + parroquiaSelected + "'";
	    String parroquia = getIDFromCB(DBNames.FIELD_IDPARROQUIA,
		    DBNames.TABLE_PARROQUIASSUBTRAMOS, whereSQL);
	    return parroquia;
	}
    }

    private String getIDFromCB(String fieldID, String tablename,
	    String whereClause) throws SQLException {
	Connection con = dbs.getJavaConnection();
	String query = "SELECT " + fieldID + " FROM " + DBNames.SCHEMA_DATA
		+ "." + tablename + " WHERE " + whereClause;
	Statement st = con.createStatement();
	ResultSet resultSet = st.executeQuery(query);
	resultSet.first();
	return resultSet.getString(1);
    }

    private String getFincaID() {
	try {
	    String numFinca = fincaSeccion.getSelectedItem().toString()
		    .split("-")[0];
	    String seccion = fincaSeccion.getSelectedItem().toString()
		    .split("-")[1];
	    return getTramoId() + getUcId() + getAyuntamientoId()
		    + getParroquiaId() + numFinca + seccion;

	} catch (SQLException e) {
	    e.printStackTrace();
	    return null;
	}
    }

    @Override
    public int getPosition() {
	try {
	    FLyrVect fincasLayer = getLayer();
	    SelectableDataSource rs = fincasLayer.getRecordset();
	    String expression = "select * from " + rs.getName()
		    + " where id_finca = '" + getFincaID() + "';";
	    DataSource rsFiltered = rs.getDataSourceFactory().executeSQL(
		    expression, DataSourceFactory.MANUAL_OPENING);
	    long[] result = rsFiltered.getWhereFilter();
            if (result.length > 0) {
                return (int) result[0];
            }
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	} catch (DriverLoadException e) {
	    e.printStackTrace();
	} catch (ParseException e) {
	    e.printStackTrace();
	} catch (SemanticException e) {
	    e.printStackTrace();
	} catch (EvaluationException e) {
	    e.printStackTrace();
	} catch (IOException e) {
	    e.printStackTrace();
	}
        return AbstractNavTable.EMPTY_REGISTER;
    }

    @Override
    public FLyrVect getLayer() {
        TOCLayerManager toc = new TOCLayerManager();
        return toc.getLayerByName(getLayerName());
    }


     private String getLayerName() {
        return isAmpliacion() ? FormExpropiations.TOCNAME_AMPLIACION : FormExpropiations.TOCNAME;
    }

    private boolean isAmpliacion() {
        try {
            String tramoID = getTramoId();
            if ( tramoID.equalsIgnoreCase("13") || tramoID.equalsIgnoreCase("14") ) {
                return true;
            }
        } catch (SQLException e) {
            logger.error(e.getStackTrace(), e);
        }

        return false;
    }

    @Override
    public void closeDialog() {
	tramo.removeActionListener(tramoListener);
	uc.removeActionListener(ucListener);
	ayuntamiento.removeActionListener(ayuntamientoListener);
	parroquiaSubtramo.removeActionListener(parroquiaListener);
	fincaSeccion.removeActionListener(fincasListener);
	zoomBt.removeActionListener(zoomToHandler);
	openForm.removeActionListener(formOpener);
	super.closeDialog();
    }

    @Override
    protected JButton getDefaultButton() {
	return zoomBt;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }

}