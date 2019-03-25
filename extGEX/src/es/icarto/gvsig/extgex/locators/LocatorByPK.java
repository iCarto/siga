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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
import es.icarto.gvsig.extgex.utils.retrievers.KeyValueRetriever;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

@SuppressWarnings("serial")
public class LocatorByPK extends BasicAbstractWindow implements ActionListener,
        ItemListener, SingletonWindow {

    private static final Logger logger = Logger.getLogger(LocatorByPK.class);

    public static final List<String> ORDERED_AP_ROADS = new ArrayList<String>(
            Arrays.asList("AP-9", "AP-9F", "AP-9M", "AP-9V", "PO-10", "PO-11",
                    "AP-9_FS", "AP-9_SF", "AP-9_VB", "AP-9_BV", "AP-9_TB",
                    "AP-9_BT"));

    public static final List<String> ORDERED_AG_ROADS = new ArrayList<String>(
            Arrays.asList("AG-55", "AG-57", "AG-57N"));

    public static final String PK_REGEX = "\\d++,?\\d*";

    private static final String TRAMO_FIELD = "tramo";
    private static final String PK_FIELD = "pks";

    public ButtonGroup radioGroup;

    public final String ID_RADIO_AP = "ap";
    private JRadioButton radioAP;

    public final String ID_RADIO_AG = "ag";
    private JRadioButton radioAG;

    public final String ID_TRAMO = "tramo";
    private JComboBox tramoCB;

    public final String ID_PKNUMBER = "pk_number";
    private JSpinner pkNumberSpinner;

    private String currentPkValue = null;
    private String latestTypedPkValue = null;
    private boolean lastPkTypedInvalid = false;

    public final String ID_GOTOPK = "goToPKButton";
    private JButton zoomBt;

    private final FLyrVect pkLayer;

    public LocatorByPK(FLyrVect pkLayer) {
	super();
	this.pkLayer = pkLayer;
        setWindowInfoProperties(WindowInfo.PALETTE);
        this.setWindowTitle("Localizar PK");
	initWidgets();
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
	return "LocatorByPK";
    }

    public void initWidgets() {
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
        pkNumberSpinner = (JSpinner) formPanel
                .getComponentByName(ID_PKNUMBER);

        pkNumberSpinner.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                currentPkValue = pkNumberSpinner.getValue().toString();
                ((JSpinner.DefaultEditor) pkNumberSpinner.getEditor())
                        .getTextField().selectAll();
            }
        });

	tramoCB.addActionListener(this);
	fillTramo();
	zoomBt = (JButton) formPanel.getComponentByName(ID_GOTOPK);
	zoomBt.addActionListener(this);
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
        List<String> orderedValues = onlyAG ? ORDERED_AG_ROADS
                : ORDERED_AP_ROADS;
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
        fillPK();
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
                        pkNumberSpinner, PK_REGEX));
                final JFormattedTextField textField = ((JSpinner.DefaultEditor) pkNumberSpinner
                        .getEditor()).getTextField();
                textField.addKeyListener(new KeyAdapter() {
                    @Override
                    public void keyReleased(final KeyEvent e) {
                        if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                            if (!lastPkTypedInvalid) {
                                zoomToSelectedPk();
                            } else {
                                zoomToClosestPk();
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

    @Override
    public void actionPerformed(ActionEvent e) {
        if (e.getSource() == tramoCB) {
	    fillPK();
        } else if (e.getSource() == zoomBt) {
            zoomToSelectedPk();
        }
    }

    @Override
    public void itemStateChanged(ItemEvent e) {
        if (e.getStateChange() == ItemEvent.SELECTED) {
            fillTramo();
        }
    }

    private void zoomToSelectedPk() {
        Double pkToFind = Double.parseDouble(pkNumberSpinner.getValue()
                .toString().replace(",", "."));
        if (pkToFind != null) {
            try {
                String tramo = tramoCB.getSelectedItem().toString();
                SelectableDataSource pkRecordset = pkLayer.getRecordset();
                int tramoIndex = pkRecordset.getFieldIndexByName(TRAMO_FIELD);
                int pkIndex = pkRecordset.getFieldIndexByName(PK_FIELD);
                for (int i = 0; i < pkRecordset.getRowCount(); i++) {
                    Double pkValue = ((NumericValue) pkRecordset.getFieldValue(
                            i, pkIndex)).doubleValue();
                    Value tramoValue = pkRecordset.getFieldValue(i, tramoIndex);
                    if (tramoValue.toString().compareToIgnoreCase(tramo) == 0
                            && pkValue.equals(pkToFind)) {
                        zoom(pkLayer, i);
                        break;
                    }
                }
                return;
            } catch (ReadDriverException e1) {
                e1.printStackTrace();
            }
        }
    }

    private void zoomToClosestPk() {
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
                    zoom(pkLayer, closestPk);
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

}
