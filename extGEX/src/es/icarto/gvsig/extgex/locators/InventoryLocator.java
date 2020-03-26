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
import java.util.Collections;
import java.util.Comparator;
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
import com.iver.cit.gvsig.fmap.layers.FBitSet;
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
public class InventoryLocator extends BasicAbstractWindow implements ActionListener, ItemListener, SingletonWindow {

    private static final Logger logger = Logger.getLogger(InventoryLocator.class);

    private static final String ID_ALL_ELEMENTS = "-1";

    private static final String TRAMO_FIELD = "tramo";
    private static final String PK_FIELD = "pks";

    private static final String TRONCO_VALUE = "Tronco";

    private static final List<Elements> ELEMENTS_TO_IGNORE = Arrays.asList(Elements.Ramales, Elements.Competencias);

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
            ResultSet rs = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query).executeQuery();
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
                        HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.put(tramo, new HashMap<String, List<KeyValue>>());
                        HIERARCHY_TRAMOS_VIAS_TIPOS.put(tramo, new ArrayList<KeyValue>());
                    }
                    if (tipoVia != null && tipoVia.trim().length() > 0) {
                        if (!HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).containsKey(tipoViaId)) {
                            HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).put(tipoViaId, new ArrayList<KeyValue>());
                            HIERARCHY_TRAMOS_VIAS_TIPOS.get(tramo).add(new KeyValue(tipoViaId, tipoVia));
                        }
                        if (nombreVia != null && nombreVia.trim().length() > 0) {
                            KeyValue nombreViaItem = new KeyValue(nombreViaId, nombreVia);
                            if (!HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).get(tipoViaId).contains(nombreViaItem)) {
                                HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).get(tipoViaId).add(nombreViaItem);
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
        this.setWindowTitle("Localizar elementos de inventario");
        initWidgets();
    }

    public static List<Elements> getValidElements(TOCLayerManager toc) {
        List<Elements> elements = new ArrayList<Elements>();
        for (Elements elemento : Elements.values()) {
            if (!ELEMENTS_TO_IGNORE.contains(elemento) && toc.getVectorialLayerByName(elemento.layerName) != null) {
                elements.add(elemento);
            }
        }
        Comparator<Elements> foo = new Comparator<Elements>() {
            @Override
            public int compare(Elements o1, Elements o2) {
                return o1.name().compareTo(o2.name());
            }
        };
        Collections.sort(elements, foo);
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

        tipoElementoCB = (JComboBox) formPanel.getComponentByName(ID_COMBO_TIPO);

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
                ((JSpinner.DefaultEditor) pkNumberSpinner.getEditor()).getTextField().selectAll();
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
        KeyValueRetriever kvPks = new KeyValueRetriever(pkLayer, TRAMO_FIELD, TRAMO_FIELD);
        kvPks.setOrderBy(TRAMO_FIELD);
        ArrayList<String> distinctValues = new ArrayList<String>();
        for (KeyValue kv : kvPks.getValues()) {
            if (!distinctValues.contains(kv.getValue()) && (onlyAG == kv.getValue().toString().startsWith("AG-"))) {
                distinctValues.add(kv.getValue());
            }
        }
        List<String> orderedValues = onlyAG ? LocatorByPK.ORDERED_AG_ROADS : LocatorByPK.ORDERED_AP_ROADS;
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
                if (tipoVia.getValue().equals(TRONCO_VALUE)) {
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
            // If road type is "Tronco", a PK must be selected. Otherwise, road
            // name must be selected.
            if (tipoVia.getValue().equals(TRONCO_VALUE)) {
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
        if (tipoVia != null && HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).containsKey(tipoVia.getKey())) {
            for (KeyValue nombreVia : HIERARCHY_TRAMOS_VIAS_TIPOS_NOMBRES.get(tramo).get(tipoVia.getKey())) {
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
                String sqlQuery = "select * from " + pkLayer.getRecordset().getName() + " where tramo = " + "'"
                        + selectedTramo.toString() + "'" + " order by pks;";
                DataSource ds = dsf.executeSQL(sqlQuery, EditionEvent.ALPHANUMERIC);
                ds.setDataSourceFactory(dsf);
                SelectableDataSource currentPks = new SelectableDataSource(ds);
                int pkIndex = currentPks.getFieldIndexByName(PK_FIELD);
                List<String> pks = new ArrayList<String>();
                for (int i = 0; i < currentPks.getRowCount(); i++) {
                    pks.add(currentPks.getFieldValue(i, pkIndex).toString().replace(".", ","));
                }
                SpinnerListModel model = new LinearSearchSpinnerListModel(pks);
                pkNumberSpinner.setModel(model);
                pkNumberSpinner.setEditor(new LinearSearchListEditor(pkNumberSpinner, LocatorByPK.PK_REGEX));
                final JFormattedTextField textField = ((JSpinner.DefaultEditor) pkNumberSpinner.getEditor())
                        .getTextField();
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
                textField.addPropertyChangeListener(new PropertyChangeListener() {
                    @Override
                    public void propertyChange(PropertyChangeEvent e) {
                        if ((e.getSource() instanceof JFormattedTextField) && "value".equals(e.getPropertyName())) {
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
        fillElementosCB();
    }

    private void fillElementosCB() {
        elementoCB.removeAllItems();
        elementoCB.addItem("-- Sin coincidencias --");
        elementoCB.setEnabled(false);
        zoomBt.setEnabled(false);
        formBt.setEnabled(false);
        Object selectedTramo = tramoCB.getSelectedItem();
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (selectedTramo != null && elementoKV != null && TRAMOS_IDS.containsKey(selectedTramo.toString())) {
            try {
                Elements elemento = elementoKV.getElemento();
                String where = " where tramo = " + TRAMOS_IDS.get(selectedTramo.toString());
                FLyrVect layer = toc.getVectorialLayerByName(elemento.layerName);
                SelectableDataSource sds = layer.getRecordset();
                if (pkNumberSpinner.isEnabled()) {
                    // If a PK was selected, then we must first filter elements by
                    // their proximity to the PK.
                    String pkValue = (String) pkNumberSpinner.getValue();
                    if (pkValue == null) {
                        return;
                    }
                    List<String> ids = getElementIdsAtDistanceFromPks(elemento.layerName, elemento.pk,
                            selectedTramo.toString(), Double.parseDouble(pkValue.replace(",", ".")), 500.0);
                    if (ids.size() == 0) {
                        return;
                    }
                    int fieldType = sds.getFieldType(sds.getFieldIndexByName(elemento.pk));
                    boolean isPkText = fieldType == java.sql.Types.VARCHAR || fieldType == java.sql.Types.NVARCHAR
                            || fieldType == java.sql.Types.LONGVARCHAR || fieldType == java.sql.Types.LONGNVARCHAR;
                    where += " and " + elemento.pk + " in (" + (isPkText ? "'" : "") + ids.get(0);
                    for (int i = 1, len = ids.size(); i < len; i++) {
                        where += (isPkText ? "'" : "") + ", " + (isPkText ? "'" : "") + ids.get(i);
                    }
                    where += (isPkText ? "'" : "") + ")";
                } else {
                    // If no PK was selected, elements are filtered by road type and
                    // name.
                    KeyValue tipoVia = ((KeyValue) tipoViaCB.getSelectedItem());
                    KeyValue nombreVia = ((KeyValue) nombreViaCB.getSelectedItem());
                    if (tipoVia == null || nombreVia == null) {
                        return;
                    }
                    where += " and tipo_via = " + tipoVia.getKey() + " and nombre_via = '" + nombreVia.getKey() + "'";
                }
                DataSourceFactory dsf;
                dsf = sds.getDataSourceFactory();
                String sqlQuery = "select * from " + layer.getRecordset().getName() + where + " order by "
                        + elemento.pk + ";";
                DataSource ds = dsf.executeSQL(sqlQuery, EditionEvent.ALPHANUMERIC);
                ds.setDataSourceFactory(dsf);
                SelectableDataSource currentElementos = new SelectableDataSource(ds);
                int pkIndex = currentElementos.getFieldIndexByName(elemento.pk);
                Integer descrIndex = elemento.descriptiveField == null ? null : currentElementos
                        .getFieldIndexByName(elemento.descriptiveField);
                if (currentElementos.getRowCount() > 0) {
                    elementoCB.removeAllItems();
                    // If there is more than one element, we add first a generic
                    // one for zooming / opening form on all of them.
                    if (currentElementos.getRowCount() > 1) {
                        elementoCB.addItem(new KeyValue(ID_ALL_ELEMENTS, "-- Todas las coincidencias ("
                                + new Long(currentElementos.getRowCount()).toString() + ") --"));
                    }
                    for (int i = 0; i < currentElementos.getRowCount(); i++) {
                        String key = currentElementos.getFieldValue(i, pkIndex).toString();
                        String descr = descrIndex != null ? currentElementos.getFieldValue(i, descrIndex).toString()
                                + " || " + key : key;
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
            if (((ElementKeyValue) tipoElementoCB.getSelectedItem()).getElemento().hasViaInfo) {
                // If previously selected element didn't have road info, then we
                // must fill those combos. Otherwise, we simply reload the
                // resulting elements.
                if (!tipoViaCB.isEnabled()) {
                    tipoViaCB.setEnabled(true);
                    fillTipoVia();
                } else {
                    fillElementosCB();
                }
            } else {
                boolean reloadAllCombos = false;
                // If previously selected element had road info, then we try to
                // revert to "Tronco" road type. If the selected road doesn't
                // have that road type, then we reload all combos.
                if (tipoViaCB.isEnabled()) {
                    Integer troncoIdx = null;
                    for (int i = 0, len = tipoViaCB.getItemCount(); i < len; i++) {
                        if (((KeyValue) tipoViaCB.getItemAt(i)).getValue().equals(TRONCO_VALUE)) {
                            troncoIdx = i;
                            break;
                        }
                    }
                    tipoViaCB.setEnabled(false);
                    nombreViaCB.setEnabled(false);
                    if (troncoIdx != null) {
                        tipoViaCB.setSelectedIndex(troncoIdx);
                    } else {
                        // If the selected tramo has no "Tronco" type, we reload
                        // all combos as a workaround.
                        fillTramo();
                        reloadAllCombos = true;
                    }
                }
                if (!reloadAllCombos) {
                    // If we haven't reloaded all combos and previously selected
                    // road type wasn't "Tronco", we reload the PK spinner.
                    // Otherwise, we simply reload the resulting elements combo.
                    if (!pkNumberSpinner.isEnabled()) {
                        pkNumberSpinner.setEnabled(true);
                        fillPK();
                    } else {
                        fillElementosCB();
                    }
                }
            }
        } else if (e.getSource() == tramoCB) {
            if (((ElementKeyValue) tipoElementoCB.getSelectedItem()).getElemento().hasViaInfo) {
                fillTipoVia();
            } else {
                fillPK();
            }
        } else if (e.getSource() == tipoViaCB) {
            tipoViaChanged();
        } else if (e.getSource() == nombreViaCB) {
            fillElementosCB();
        } else if (e.getSource() == zoomBt) {
            zoom();
        } else if (e.getSource() == formBt) {
            openForm();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            fillTramo();
        }
    }

    private void zoom() {
        KeyValue idToFindKV = (KeyValue) elementoCB.getSelectedItem();
        if (idToFindKV != null) {
            if (idToFindKV.getKey().equals(ID_ALL_ELEMENTS)) {
                zoomToAllElements();
            } else {
                zoomToElement();
            }
        }
    }

    private void zoomToElement() {
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (elementoKV != null) {
            selectElement();
            zoomToSelection(toc.getVectorialLayerByName(elementoKV.getElemento().layerName));
        }
    }

    private void selectElement() {
        KeyValue idToFindKV = (KeyValue) elementoCB.getSelectedItem();
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (idToFindKV != null && elementoKV != null) {
            String idToFind = idToFindKV.getKey();
            Elements elemento = elementoKV.getElemento();
            try {
                FLyrVect layer = toc.getVectorialLayerByName(elemento.layerName);
                SelectableDataSource recordset = layer.getRecordset();
                int idIndex = recordset.getFieldIndexByName(elemento.pk);
                Integer idx = null;
                for (int i = 0; i < recordset.getRowCount(); i++) {
                    if (idToFind.equals(recordset.getFieldValue(i, idIndex).toString())) {
                        idx = i;
                        break;
                    }
                }
                if (idx != null) {
                    FBitSet selection = new FBitSet();
                    selection.set(idx);
                    recordset.clearSelection();
                    recordset.setSelection(selection);
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void zoomToAllElements() {
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (elementoKV != null && elementoCB.getItemCount() > 1) {
            selectAllElements();
            zoomToSelection(toc.getVectorialLayerByName(elementoKV.getElemento().layerName));
        }
    }

    private void selectAllElements() {
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (elementoKV != null && elementoCB.getItemCount() > 1) {
            List<String> idsToFind = new ArrayList<String>();
            for (int i = 1, len = elementoCB.getItemCount(); i < len; i++) {
                idsToFind.add(((KeyValue) elementoCB.getItemAt(i)).getKey());
            }

            Elements elemento = elementoKV.getElemento();
            FLyrVect layer = toc.getVectorialLayerByName(elemento.layerName);
            try {
                SelectableDataSource recordset = layer.getRecordset();
                int idIndex = recordset.getFieldIndexByName(elemento.pk);
                List<Integer> idxs = new ArrayList<Integer>();
                for (int i = 0; i < recordset.getRowCount(); i++) {
                    if (idsToFind.contains(recordset.getFieldValue(i, idIndex).toString())) {
                        idxs.add(i);
                    }
                }
                FBitSet selection = new FBitSet();
                for (int i = 0; i < idxs.size(); i++) {
                    selection.set(idxs.get(i));
                }
                recordset.clearSelection();
                recordset.setSelection(selection);
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    private void openForm() {
        KeyValue idToFindKV = (KeyValue) elementoCB.getSelectedItem();
        if (idToFindKV != null) {
            if (idToFindKV.getKey().equals(ID_ALL_ELEMENTS)) {
                openFormAllElements();
            } else {
                openFormElement();
            }
        }
    }

    private void openFormAllElements() {
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (elementoKV != null && elementoCB.getItemCount() > 1) {
            selectAllElements();
            openForm(true);
        }
    }

    private void openFormElement() {
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (elementoKV != null) {
            selectElement();
            openForm(false);
            zoomToSelection(toc.getVectorialLayerByName(elementoKV.getElemento().layerName));
        }
    }

    private void openForm(boolean onlySelected) {
        KeyValue idToFindKV = (KeyValue) this.elementoCB.getSelectedItem();
        ElementKeyValue elementoKV = ((ElementKeyValue) tipoElementoCB.getSelectedItem());
        if (idToFindKV != null && elementoKV != null) {
            String idToFind = idToFindKV.getKey();
            Elements elemento = elementoKV.getElemento();
            try {
                FLyrVect layer = toc.getVectorialLayerByName(elemento.layerName);
                SelectableDataSource pkRecordset = layer.getRecordset();
                Integer position = null;
                if (!onlySelected) {
                    int idIndex = pkRecordset.getFieldIndexByName(elemento.pk);
                    for (int i = 0; i < pkRecordset.getRowCount(); i++) {
                        if (idToFind.equals(pkRecordset.getFieldValue(i, idIndex).toString())) {
                            position = i;
                            break;
                        }
                    }
                }
                if (position != null || onlySelected) {
                    AbstractForm form = LaunchGIAForms.getExistingOrNewFormDependingOfLayer(layer);
                    if (form != null) {
                        if (!form.isShowing() && form.init()) {
                            PluginServices.getMDIManager().addWindow(form);
                        }
                        form.setOnlySelected(onlySelected);
                        form.setAlwaysZoom(true);
                        if (position != null) {
                            form.setPosition(position);
                        }
                    }
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
        }
    }

    private static void zoomToSelection(FLyrVect layer) {
        try {
            Rectangle2D rectangleAll = null;
            FBitSet selection = layer.getSelectionSupport().getSelection();
            ReadableVectorial source = (layer).getSource();
            source.start();
            for (int i = 0, len = source.getShapeCount(); i < len; i++) {
                if (selection.get(i)) {
                    IGeometry g = source.getShape(i);
                    /*
                     * fix to avoid zoom problems when layer and view
                     * projections aren't the same.
                     */
                    if (layer.getCoordTrans() != null) {
                        g.reProject(layer.getCoordTrans());
                    }
                    Rectangle2D rectangle = g.getBounds2D();
                    if (rectangle.getWidth() < 200) {
                        rectangle.setFrameFromCenter(rectangle.getCenterX(), rectangle.getCenterY(),
                                rectangle.getCenterX() + 100, rectangle.getCenterY() + 100);
                    }
                    if (rectangleAll == null) {
                        rectangleAll = rectangle;
                    } else {
                        rectangleAll.add(rectangle);
                    }
                }
            }
            source.stop();
            if (rectangleAll != null) {
                layer.getMapContext().getViewPort().setExtent(rectangleAll);
                layer.getMapContext().setScaleView(4000);
            }
        } catch (InitializeDriverException e) {
            e.printStackTrace();
        } catch (ReadDriverException e) {
            e.printStackTrace();
        }
    }

    private void selectClosestPk() {
        if (latestTypedPkValue != null) {
            Double typedNr = null;
            try {
                typedNr = Double.parseDouble(latestTypedPkValue.replace(",", "."));
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
                    Double pkValue = ((NumericValue) pkRecordset.getFieldValue(i, pkIndex)).doubleValue();
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
                    pkNumberSpinner.setValue(((NumericValue) pkRecordset.getFieldValue(closestPk, pkIndex)).toString()
                            .replace(".", ","));
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
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
        return this.getClass().getName();
    }

    @Override
    protected WindowInfo getWindowInfo(Component frame) {
        WindowInfo wInfo = super.getWindowInfo(frame);
        wInfo.setHeight(wInfo.getHeight() - 15);
        return wInfo;
    }

    private List<String> getElementIdsAtDistanceFromPks(String elementsTable, String idField, String tramo, Double pk,
            Double distance) {
        String query = "SELECT DISTINCT " + idField + " FROM audasa_extgia." + elementsTable
                + " WHERE ST_DWithin(st_transform(the_geom, 4326)::geography, "
                + "(SELECT st_transform(the_geom, 4326)::geography FROM "
                + "audasa_elementos_autopista.pks WHERE pks = " + pk.toString() + " AND tramo = '" + tramo + "'), "
                + distance.toString() + ") AND tramo = " + TRAMOS_IDS.get(tramo) + ";";
        List<String> results = new ArrayList<String>();
        try {
            ResultSet rs = DBSession.getCurrentSession().getJavaConnection().prepareStatement(query).executeQuery();
            while (rs.next()) {
                results.add(rs.getString(1));
            }
        } catch (SQLException e) {
            logger.error("Query failed: `" + query + "´", e);
        }
        return results;
    }

    private class ElementKeyValue extends KeyValue {

        private final Elements elemento;

        public ElementKeyValue(Elements elemento) {
            super(elemento.layerName, elemento.getStylizedName());
            this.elemento = elemento;
        }

        public Elements getElemento() {
            return this.elemento;
        }
    }

}
