package es.icarto.gvsig.extgex.locators;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.LinearSearchListEditor;
import javax.swing.LinearSearchSpinnerListModel;
import javax.swing.SpinnerListModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.apache.log4j.Logger;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.engine.values.NumericValue;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.parser.ParseException;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.commons.gui.BasicAbstractWindow;
import es.icarto.gvsig.commons.gui.TOCLayerManager;
import es.icarto.gvsig.extgex.utils.retrievers.KeyValueRetriever;
import es.icarto.gvsig.extgia.forms.LaunchGIAForms;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class InventoryLocator extends BasicAbstractWindow implements
        ActionListener,
        ItemListener, SingletonWindow {

    private static final Logger logger = Logger.getLogger(InventoryLocator.class);

    private static final String TRAMO_FIELD = "tramo";
    private static final String PK_FIELD = "pks";

    private static final List<Elements> ELEMENTS_TO_IGNORE = Arrays.asList(
            Elements.Ramales, Elements.Competencias, Elements.Comunicaciones);

    private static final Map<String, String> TRAMOS_IDS = new HashMap<String, String>();

    private static final Map<String, Map<String, List<KeyValue>>> HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES = new HashMap<String, Map<String, List<KeyValue>>>();

    private static final Map<String, List<KeyValue>> HIERARCHY_TRAMOS_VIAS_TIPOS = new HashMap<String, List<KeyValue>>();

    public final String ID_COMBO_TIPO = "tipo_elemento";
    private JComboBox tipoElementoCB;

    public ButtonGroup radioGroup;

    public final String ID_RADIO_AP = "ap";
    private JRadioButton radioAP;

    public final String ID_RADIO_AG = "ag";
    private JRadioButton radioAG;

    public final String ID_TRAMO = "tramo";
    private JComboBox tramoCB;

    public final String ID_TIPO_VIA = "tipo_via";
    private JComboBox tipoViaCB;

    public final String ID_NOMBRE_VIA = "nombre_via";
    private JComboBox nombreViaCB;

    public final String ID_PKNUMBER = "pk_number";
    private JSpinner pkNumberSpinner;

    public final String ID_ELEMENTO = "elemento";
    private JComboBox elementoCB;

    private String currentPkValue = null;
    private String latestTypedPkValue = null;
    private boolean lastPkTypedInvalid = false;

    public final String ID_ZOOM = "zoomButton";
    private JButton zoomBt;

    public final String ID_FORM = "formButton";
    private JButton formBt;

    private final static String PK_LAYER_NAME = "pks";
    private final FLyrVect pkLayer;

    private final TOCLayerManager toc;

    static {
        String query = "SELECT DISTINCT a.id, a.item, b.id, b.item, c.id, c.item FROM audasa_extgia_dominios.tramo a "
                + "LEFT JOIN audasa_extgia_dominios.tipo_via b ON a.id = b.id_tramo "
                + "LEFT JOIN audasa_extgia_dominios.nombre_via c ON c.id_tv = b.id AND c.id_tramo = a.id "
                + "WHERE trim(a.item) != '' ORDER BY a.id, b.id, c.id;";

        try {
            ResultSet rs = DBSession.getCurrentSession().getJavaConnection()
                    .prepareStatement(query).executeQuery();
            while (rs.next()) {
                String tramoId = rs.getString(1);
                String tramo = rs.getString(2);
                String tipoViaId = rs.getString(3);
                String tipoVia = rs.getString(4);
                String nombreViaId = rs.getString(5);
                String nombreVia = rs.getString(6);
                if (tramo != null) {
                    if (!TRAMOS_IDS.containsKey(tramo)) {
                        TRAMOS_IDS.put(tramo, tramoId);
                    }
                    if (!HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.containsKey(tramo)) {
                        HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.put(tramo,
                                new HashMap<String, List<KeyValue>>());
                        HIERARCHY_TRAMOS_VIAS_TIPOS.put(tramo,
                                new ArrayList<KeyValue>());
                    }
                    if (tipoVia != null && tipoVia.trim().length() > 0) {
                        if (!HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo)
                                .containsKey(tipoViaId)) {
                            HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).put(
                                    tipoViaId,
                                    new ArrayList<KeyValue>());
                            HIERARCHY_TRAMOS_VIAS_TIPOS.get(tramo).add(
                                    new KeyValue(tipoViaId,
                                            tipoVia));
                        }
                        if (nombreVia != null && nombreVia.trim().length() > 0) {
                            KeyValue nombreViaItem = new KeyValue(nombreViaId,
                                    nombreVia);
                            if (!HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo)
                                    .get(tipoViaId)
                                    .contains(nombreViaItem)) {
                                HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo)
                                        .get(tipoViaId)
                                        .add(nombreViaItem);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            logger.error(e);
        }
    }

    public InventoryLocator(TOCLayerManager toc) {
	super();
        this.toc = toc;
        this.pkLayer = toc.getVectorialLayerByName(PK_LAYER_NAME);
        setWindowInfoProperties(WindowInfo.PALETTE);
        this.setWindowTitle("Localizar PK");
        initWidgets();
    }

    public static List<Elements> getValidElements(TOCLayerManager toc) {
        List<Elements> elements = new ArrayList<Elements>();
        for (Elements elemento : Elements.values()) {
            if (!ELEMENTS_TO_IGNORE.contains(elemento)
                    && toc.getVectorialLayerByName(elemento.layerName) != null) {
                elements.add(elemento);
            }
        }
        return elements;
    }

    @Override
    public FormPanel getFormPanel() {
        FormPanel fp = super.getFormPanel();
        fp.setBorder(null);
        return fp;
    }

    @Override
    public void openDialog() {
        setFocusCycleRoot(true);
        PluginServices.getMDIManager().addWindow(this);
        radioAP.requestFocusInWindow();
    }

    @Override
    protected String getBasicName() {
        return "InventoryLocator";
    }

    public void initWidgets() {
        zoomBt = (JButton) formPanel.getComponentByName(ID_ZOOM);
        formBt = (JButton) formPanel.getComponentByName(ID_FORM);

        tipoElementoCB = (JComboBox) formPanel
                .getComponentByName(ID_COMBO_TIPO);

        for (Elements elemento : getValidElements(toc)) {
            tipoElementoCB.addItem(new ElementKeyValue(elemento));
        }
        tipoElementoCB.addActionListener(this);

        radioAP = (JRadioButton) formPanel.getComponentByName(ID_RADIO_AP);
        radioAG = (JRadioButton) formPanel.getComponentByName(ID_RADIO_AG);
        radioGroup = new ButtonGroup();

        radioGroup.add(radioAP);
        radioGroup.add(radioAG);
        radioGroup.clearSelection();

        radioAP.setSelected(true);

        radioAG.addItemListener(this);
        radioAP.addItemListener(this);

        tramoCB = (JComboBox) formPanel.getComponentByName(ID_TRAMO);
        tipoViaCB = (JComboBox) formPanel.getComponentByName(ID_TIPO_VIA);
        nombreViaCB = (JComboBox) formPanel.getComponentByName(ID_NOMBRE_VIA);
        elementoCB = (JComboBox) formPanel.getComponentByName(ID_ELEMENTO);

        pkNumberSpinner = (JSpinner) formPanel.getComponentByName(ID_PKNUMBER);

        pkNumberSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentPkValue = pkNumberSpinner.getValue().toString();
                ((JSpinner.DefaultEditor) pkNumberSpinner.getEditor())
                        .getTextField().selectAll();
                fillElementosCB();
            }
        });

	tramoCB.addActionListener(this);
        tipoViaCB.addActionListener(this);
        nombreViaCB.addActionListener(this);

        if (((ElementKeyValue) tipoElementoCB.getSelectedItem()).getElemento().hasViaInfo) {
            tipoViaCB.setEnabled(true);
            nombreViaCB.setEnabled(true);
            pkNumberSpinner.setEnabled(false);
        } else {
            tipoViaCB.setEnabled(false);
            nombreViaCB.setEnabled(false);
            pkNumberSpinner.setEnabled(true);
        }

	fillTramo();
	zoomBt.addActionListener(this);
        formBt.addActionListener(this);
    }

    private void fillTramo() {
        tramoCB.removeAllItems();
        boolean onlyAG = radioAG.isSelected();
	KeyValueRetriever kvPks = new KeyValueRetriever(pkLayer, TRAMO_FIELD,
		TRAMO_FIELD);
	kvPks.setOrderBy(TRAMO_FIELD);
	ArrayList<String> distinctValues = new ArrayList<String>();
	for (KeyValue kv : kvPks.getValues()) {
            if (!distinctValues.contains(kv.getValue())
                    && (onlyAG == kv.getValue().toString().startsWith("AG-"))) {
		distinctValues.add(kv.getValue());
	    }
	}
        List<String> orderedValues = onlyAG ? LocatorByPK.ORDERED_AG_ROADS
                : LocatorByPK.ORDERED_AP_ROADS;
        for (String dkv : orderedValues) {
            if (distinctValues.contains(dkv)) {
                tramoCB.addItem(dkv);
            }
	}
        for (String dkv : distinctValues) {
            if (!orderedValues.contains(dkv)) {
                tramoCB.addItem(dkv);
            }
        }
        if (((ElementKeyValue) tipoElementoCB.getSelectedItem()).getElemento().hasViaInfo) {
            fillTipoVia();
        } else {
            fillPK();
        }
    }

    private void fillTipoVia() {
        String tramo = (String) tramoCB.getSelectedItem();
        tipoViaCB.removeAllItems();
        if (tramo != null && HIERARCHY_TRAMOS_VIAS_TIPOS.containsKey(tramo)) {
            Integer troncoIdx = null;
            for (KeyValue tipoVia : HIERARCHY_TRAMOS_VIAS_TIPOS.get(tramo)) {
                tipoViaCB.addItem(tipoVia);
                if (tipoVia.getValue().equals("Tronco")) {
                    troncoIdx = tipoViaCB.getItemCount() - 1;
                }
            }
            if (troncoIdx != null) {
                tipoViaCB.setSelectedIndex(troncoIdx);
            }
            tipoViaCB.setEnabled(true);
        } else {
            tipoViaCB.setEnabled(false);
        }
        tipoViaChanged();
    }

    private void tipoViaChanged() {
        KeyValue tipoVia = ((KeyValue) tipoViaCB.getSelectedItem());
        if (tipoVia != null) {
            if (tipoVia.getValue().equals("Tronco")) {
                nombreViaCB.removeAllItems();
                nombreViaCB.setEnabled(false);
                pkNumberSpinner.setEnabled(true);
                fillPK();
            } else {
                nombreViaCB.setEnabled(true);
                pkNumberSpinner.setEnabled(false);
                fillNombreVia();
            }
        }
    }

    private void fillNombreVia() {
        String tramo = (String) tramoCB.getSelectedItem();
        KeyValue tipoVia = ((KeyValue) tipoViaCB.getSelectedItem());
        nombreViaCB.removeAllItems();
        if (tipoVia != null
                && HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).containsKey(
                        tipoVia.getKey())) {
            for (KeyValue nombreVia : HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(
                    tramo).get(tipoVia.getKey())) {
                nombreViaCB.addItem(nombreVia);
            }
            nombreViaCB.setEnabled(true);
        } else {
            nombreViaCB.setEnabled(false);
        }
    }

    private void fillPK() {
        Object selectedTramo = tramoCB.getSelectedItem();
        if (selectedTramo != null) {
            DataSourceFactory dsf;
            try {
                dsf = pkLayer.getRecordset().getDataSourceFactory();
                String sqlQuery = "select * from "
                        + pkLayer.getRecordset().getName() + " where tramo = "
                        + "'" + selectedTramo.toString() + "'"
                        + " order by pks;";
                DataSource ds = dsf.executeSQL(sqlQuery,
                        EditionEvent.ALPHANUMERIC);
                ds.setDataSourceFactory(dsf);
                SelectableDataSource currentPks = new SelectableDataSource(ds);
                int pkIndex = currentPks.getFieldIndexByName(PK_FIELD);
                List<String> pks = new ArrayList<String>();
                for (int i = 0; i < currentPks.getRowCount(); i++) {
                    pks.add(currentPks.getFieldValue(i, pkIndex).toString()
                            .replace(".", ","));
                }
                SpinnerListModel model = new LinearSearchSpinnerListModel(pks);
                pkNumberSpinner.setModel(model);
                pkNumberSpinner.setEditor(new LinearSearchListEditor(
                        pkNumberSpinner, LocatorByPK.PK_REGEX));
                final JFormattedTextField textField = ((JSpinner.DefaultEditor) pkNumberSpinner
                        .getEditor()).getTextField();
                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(final KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (lastPkTypedInvalid) {
                                selectClosestPk();
                                latestTypedPkValue = null;
                            }
                            textField.selectAll();
                        }
                    }

                    @Override
                    public void keyPressed(KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            latestTypedPkValue = textField.getText();
                            currentPkValue = null;
                            lastPkTypedInvalid = false;
                        }
                    }
                });
                textField
                        .addPropertyChangeListener(new PropertyChangeListener() {
                            @Override
                            public void propertyChange(PropertyChangeEvent e) {
                                if ((e.getSource() instanceof JFormattedTextField)
                                        && "value".equals(e.getPropertyName())) {
                                    if (currentPkValue == null) {
                                        lastPkTypedInvalid = true;
                                    }
                                }
                            }
                        });
                textField.addFocusListener(new FocusListener() {
                    @Override
                    public void focusGained(FocusEvent e) {
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                textField.selectAll();
                            }
                        });
                    }

                    @Override
                    public void focusLost(FocusEvent e) {
                        // TODO Auto-generated method stub
                    }
                });
                currentPkValue = pkNumberSpinner.getValue().toString();
            } catch (ReadDriverException e) {
                logger.error(e.getStackTrace(), e);
            } catch (DriverLoadException e) {
                logger.error(e.getStackTrace(), e);
            } catch (ParseException e) {
                logger.error(e.getStackTrace(), e);
            } catch (SemanticException e) {
                logger.error(e.getStackTrace(), e);
            } catch (EvaluationException e) {
                logger.error(e.getStackTrace(), e);
            }
        }
    }

    private void fillElementosCB() {
        elementoCB.removeAllItems();
        elementoCB.addItem("-- Sin coincidencias --");
        elementoCB.setEnabled(false);
        zoomBt.setEnabled(false);
        formBt.setEnabled(false);
        Object selectedTramo = tramoCB.getSelectedItem();
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB
                .getSelectedItem());
        if (selectedTramo != null && elementoKV != null
                && TRAMOS_IDS.containsKey(selectedTramo.toString())) {
            Elements elemento = elementoKV.getElemento();
            String where = " where tramo = "
                    + TRAMOS_IDS.get(selectedTramo.toString());
            if (pkNumberSpinner.isEnabled()) {
                String pkValue = (String) pkNumberSpinner.getValue();
                if (pkValue == null) {
                    return;
                }
                List<Integer> ids = getElementIdsAtDistanceFromPks(
                        elemento.layerName, elemento.pk,
                        selectedTramo.toString(),
                        Double.parseDouble(pkValue.replace(",", ".")),
                        500.0);
                if (ids.size() == 0) {
                    return;
                }
                where += " and " + elemento.pk + " in (" + ids.get(0);
                for (int i = 1, len = ids.size(); i < len; i++) {
                    where += ", " + ids.get(i);
                }
                where += ")";
                if (elemento.hasViaInfo) {
                    KeyValue tipoVia = ((KeyValue) tipoViaCB.getSelectedItem());
                    if (tipoVia == null) {
                        return;
                    }
                    where += " and tipo_via = " + tipoVia.getKey();
                }
            } else {
                KeyValue tipoVia = ((KeyValue) tipoViaCB.getSelectedItem());
                KeyValue nombreVia = ((KeyValue) nombreViaCB.getSelectedItem());
                if (tipoVia == null || nombreVia == null) {
                    return;
                }
                where += " and tipo_via = " + tipoVia.getKey()
                        + " and nombre_via = '" + nombreVia.getKey() + "'";
            }
            DataSourceFactory dsf;
            try {
                FLyrVect layer = toc
                        .getVectorialLayerByName(elemento.layerName);
                dsf = layer.getRecordset().getDataSourceFactory();
                String sqlQuery = "select * from "
                        + layer.getRecordset().getName() + where + " order by "
                        + elemento.pk + ";";
                DataSource ds = dsf.executeSQL(sqlQuery,
                        EditionEvent.ALPHANUMERIC);
                ds.setDataSourceFactory(dsf);
                SelectableDataSource currentElementos = new SelectableDataSource(
                        ds);
                int pkIndex = currentElementos.getFieldIndexByName(elemento.pk);
                Integer descrIndex = elemento.descriptiveField == null ? null
                        : currentElementos
                                .getFieldIndexByName(elemento.descriptiveField);
                if (currentElementos.getRowCount() > 0) {
                    elementoCB.removeAllItems();
                    for (int i = 0; i < currentElementos.getRowCount(); i++) {
                        String key = currentElementos.getFieldValue(i, pkIndex)
                                .toString();
                        String descr = descrIndex != null ? currentElementos
                                .getFieldValue(i, descrIndex).toString()
                                + " - " + key : key;
                        elementoCB.addItem(new KeyValue(key, descr));
                    }
                    elementoCB.setEnabled(true);
                    zoomBt.setEnabled(true);
                    formBt.setEnabled(true);
                }
            } catch (ReadDriverException e) {
                logger.error(e.getStackTrace(), e);
            } catch (DriverLoadException e) {
                logger.error(e.getStackTrace(), e);
            } catch (ParseException e) {
                logger.error(e.getStackTrace(), e);
            } catch (SemanticException e) {
                logger.error(e.getStackTrace(), e);
            } catch (EvaluationException e) {
                logger.error(e.getStackTrace(), e);
            }
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == tipoElementoCB) {
            if (((ElementKeyValue) tipoElementoCB.getSelectedItem())
                    .getElemento().hasViaInfo) {
                tipoViaCB.setEnabled(true);
                nombreViaCB.setEnabled(true);
                fillTipoVia();
            } else {
                tipoViaCB.setEnabled(false);
                nombreViaCB.setEnabled(false);
                pkNumberSpinner.setEnabled(true);
                fillPK();
            }
        } else if (e.getSource() == tramoCB) {
            if (((ElementKeyValue) tipoElementoCB.getSelectedItem())
                    .getElemento().hasViaInfo) {
                fillTipoVia();
            } else {
                fillPK();
            }
        } else if (e.getSource() == tipoViaCB) {
            tipoViaChanged();
        } else if (e.getSource() == nombreViaCB) {
            fillElementosCB();
        } else if (e.getSource() == zoomBt) {
            zoomToSelectedElement();
        } else if (e.getSource() == formBt) {
            openFormOnSelectedElement();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            fillTramo();
        }
    }

    private void zoomToSelectedElement() {
        KeyValue idToFindKV = (KeyValue) this.elementoCB.getSelectedItem();
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB
                .getSelectedItem());
        if (idToFindKV != null && elementoKV != null) {
            String idToFind = idToFindKV.getKey();
            Elements elemento = elementoKV.getElemento();
            try {
                FLyrVect layer = toc
                        .getVectorialLayerByName(elemento.layerName);
                SelectableDataSource pkRecordset = layer.getRecordset();
                int idIndex = pkRecordset.getFieldIndexByName(elemento.pk);
                for (int i = 0; i < pkRecordset.getRowCount(); i++) {
                    if (idToFind.equals(pkRecordset.getFieldValue(i, idIndex)
                            .toString())) {
                        zoom(layer, i);
                        break;
                    }
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void openFormOnSelectedElement() {
        KeyValue idToFindKV = (KeyValue) this.elementoCB.getSelectedItem();
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB
                .getSelectedItem());
        if (idToFindKV != null && elementoKV != null) {
            String idToFind = idToFindKV.getKey();
            Elements elemento = elementoKV.getElemento();
            try {
                FLyrVect layer = toc
                        .getVectorialLayerByName(elemento.layerName);
                SelectableDataSource pkRecordset = layer.getRecordset();
                int idIndex = pkRecordset.getFieldIndexByName(elemento.pk);
                Integer position = null;
                for (int i = 0; i < pkRecordset.getRowCount(); i++) {
                    if (idToFind.equals(pkRecordset.getFieldValue(i, idIndex)
                            .toString())) {
                        position = i;
                        break;
                    }
                }
                if (position != null) {
                    AbstractForm form = LaunchGIAForms
                            .getFormDependingOfLayer(layer);
                    if (form != null) {
                        if (form.init()) {
                            PluginServices.getMDIManager().addWindow(form);
                        }
                        form.setPosition(position);
                    }
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void selectClosestPk() {
        if (latestTypedPkValue != null) {
            Double typedNr = null;
            try {
                typedNr = Double.parseDouble(latestTypedPkValue.replace(",",
                        "."));
            } catch (NumberFormatException e) {
                // We ignore the parsing error
            }
            if (typedNr == null) {
                return;
            }
            Integer closestPk = null;
            Double smallestDiff = null;
            try {
                String tramo = tramoCB.getSelectedItem().toString();
                SelectableDataSource pkRecordset = pkLayer.getRecordset();
                int tramoIndex = pkRecordset.getFieldIndexByName(TRAMO_FIELD);
                int pkIndex = pkRecordset.getFieldIndexByName(PK_FIELD);
                for (int i = 0; i < pkRecordset.getRowCount(); i++) {
                    Double pkValue = ((NumericValue) pkRecordset.getFieldValue(
                            i, pkIndex)).doubleValue();
                    Value tramoValue = pkRecordset.getFieldValue(i, tramoIndex);
                    if (tramoValue.toString().compareToIgnoreCase(tramo) == 0) {
                        double diff = Math.abs(typedNr - pkValue);
                        if (smallestDiff == null || diff < smallestDiff) {
                            smallestDiff = diff;
                            closestPk = i;
                        }
                    }
                }
                if (closestPk != null) {
                    pkNumberSpinner.setValue(((NumericValue) pkRecordset
                            .getFieldValue(closestPk, pkIndex)).toString()
                            .replace(".",
                            ","));
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void zoom(FLyrVect layer, int pos) {
	try {
	    Rectangle2D rectangle = null;
	    IGeometry g;
	    ReadableVectorial source = (layer).getSource();
	    source.start();
	    g = source.getShape(pos);
	    source.stop();
	    /*
	     * fix to avoid zoom problems when layer and view projections aren't
	     * the same.
	     */
	    if (layer.getCoordTrans() != null) {
		g.reProject(layer.getCoordTrans());
	    }
	    rectangle = g.getBounds2D();
	    if (rectangle.getWidth() < 200) {
		rectangle.setFrameFromCenter(rectangle.getCenterX(),
			rectangle.getCenterY(), rectangle.getCenterX() + 100,
			rectangle.getCenterY() + 100);
	    }
	    if (rectangle != null) {
		layer.getMapContext().getViewPort().setExtent(rectangle);
		layer.getMapContext().setScaleView(4000);
	    }
	} catch (InitializeDriverException e) {
	    e.printStackTrace();
	} catch (ReadDriverException e) {
	    e.printStackTrace();
	}
    }

    @Override
    protected JButton getDefaultButton() {
	return zoomBt;
    }

    @Override
    protected Component getDefaultFocusComponent() {
	return null;
    }

    @Override
    public Object getWindowModel() {
        return "PkLocator";
    }

    @Override
    protected WindowInfo getWindowInfo(Component frame) {
        WindowInfo wInfo = super.getWindowInfo(frame);
        wInfo.setHeight(wInfo.getHeight() - 15);
        return wInfo;
    }

    private List<Integer> getElementIdsAtDistanceFromPks(String elementsTable,
            String idField, String tramo, Double pk, Double distance) {
        String query = "SELECT DISTINCT "
                + idField
                + " FROM audasa_extgia."
                + elementsTable
                + " WHERE ST_DWithin(st_transform(the_geom, 4326)::geography, "
                + "(SELECT st_transform(the_geom, 4326)::geography FROM "
                + "audasa_elementos_autopista.pks WHERE pks = "
                + pk.toString() + " AND tramo = '" + tramo + "'), "
                + distance.toString() + ") AND tramo = "
                + TRAMOS_IDS.get(tramo) + ";";
        List<Integer> results = new ArrayList<Integer>();
        try {
            ResultSet rs = DBSession.getCurrentSession().getJavaConnection()
                    .prepareStatement(query).executeQuery();
            while (rs.next()) {
                results.add(rs.getInt(1));
            }
        } catch (SQLException e) {
            logger.error(e);
        }
        return results;
    }

    private class ElementKeyValue extends KeyValue {

        private final Elements elemento;

        public ElementKeyValue(Elements elemento) {
            super(elemento.layerName, elemento.stylizedName);
            this.elemento = elemento;
        }

        public Elements getElemento() {
            return this.elemento;
        }
    }

}
