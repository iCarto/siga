package es.icarto.gvsig.extgex.locators;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.hardcode.driverManager.DriverLoadException;
import com.hardcode.gdbms.driver.exceptions.InitializeDriverException;
import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.data.DataSource;
import com.hardcode.gdbms.engine.data.DataSourceFactory;
import com.hardcode.gdbms.engine.instruction.EvaluationException;
import com.hardcode.gdbms.engine.instruction.SemanticException;
import com.hardcode.gdbms.engine.values.Value;
import com.hardcode.gdbms.parser.ParseException;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.core.IGeometry;
import com.iver.cit.gvsig.fmap.edition.EditableAdapter;
import com.iver.cit.gvsig.fmap.edition.EditionEvent;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.fmap.layers.ReadableVectorial;
import com.iver.cit.gvsig.fmap.layers.SelectableDataSource;

import es.icarto.gvsig.commons.gui.BasicAbstractWindow;
import es.icarto.gvsig.extgex.utils.retrievers.KeyValueRetriever;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.navtable.format.ValueFormatNT;

@SuppressWarnings("serial")
public class LocatorByPK extends BasicAbstractWindow implements ActionListener {

    private static final ValueFormatNT WRITER = new ValueFormatNT();

    private static final Logger logger = Logger.getLogger(LocatorByPK.class);

    private static final String TRAMO_FIELD = "tramo";
    private static final String PK_FIELD = "pks";

    public final String ID_TRAMO = "tramo";
    private JComboBox tramoCB;

    public final String ID_PKNUMBER = "pk_number";
    private JComboBox pkNumberCB;

    public final String ID_GOTOPK = "goToPKButton";
    private JButton zoomBt;

    private final FLyrVect pkLayer;

    public LocatorByPK(FLyrVect pkLayer) {
	super();
	this.pkLayer = pkLayer;
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
	this.setWindowTitle("Localizar PK");
	initWidgets();
    }

    @Override
    protected String getBasicName() {
	return "LocatorByPK";
    }

    public void initWidgets() {
	tramoCB = (JComboBox) formPanel.getComponentByName(ID_TRAMO);
	pkNumberCB = (JComboBox) formPanel.getComponentByName(ID_PKNUMBER);
	tramoCB.addActionListener(this);
	fillTramo();
	fillPK();
	zoomBt = (JButton) formPanel.getComponentByName(ID_GOTOPK);
	zoomBt.addActionListener(this);
    }

    private void fillTramo() {
	KeyValueRetriever kvPks = new KeyValueRetriever(pkLayer, TRAMO_FIELD,
		TRAMO_FIELD);
	kvPks.setOrderBy(TRAMO_FIELD);
	ArrayList<String> distinctValues = new ArrayList<String>();
	for (KeyValue kv : kvPks.getValues()) {
	    if (!distinctValues.contains(kv.getValue())) {
		distinctValues.add(kv.getValue());
	    }
	}
	for (String dkv : distinctValues) {
	    tramoCB.addItem(dkv);
	}
    }

    private void fillPK() {
	DataSourceFactory dsf;
	try {
	    dsf = pkLayer.getRecordset().getDataSourceFactory();
	    String sqlQuery = "select * from "
		    + pkLayer.getRecordset().getName() + " where tramo = "
		    + "'" + tramoCB.getSelectedItem().toString() + "'"
		    + " order by pks;";
	    DataSource ds = dsf.executeSQL(sqlQuery, EditionEvent.ALPHANUMERIC);
	    ds.setDataSourceFactory(dsf);
	    SelectableDataSource sds = new SelectableDataSource(ds);
	    EditableAdapter ea = new EditableAdapter();
	    ea.setOriginalDataSource(sds);
	    pkNumberCB.removeAllItems();
	    int pkIndex = ea.getRecordset().getFieldIndexByName(PK_FIELD);
	    for (int i = 0; i < ea.getRecordset().getRowCount(); i++) {
		pkNumberCB.addItem(ea.getRecordset().getFieldValue(i, pkIndex)
			.getStringValue(WRITER));
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

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == tramoCB) {
	    fillPK();
	}
	if (e.getSource() == zoomBt) {
	    String pkToFind = pkNumberCB.getSelectedItem().toString();
	    String tramo = tramoCB.getSelectedItem().toString();
	    if (pkToFind != null) {
		try {
		    SelectableDataSource pkRecordset = pkLayer.getRecordset();
		    int tramoIndex = pkRecordset
			    .getFieldIndexByName(TRAMO_FIELD);
		    int pkIndex = pkRecordset.getFieldIndexByName(PK_FIELD);
		    for (int i = 0; i < pkRecordset.getRowCount(); i++) {
			Value pkValue = pkRecordset.getFieldValue(i, pkIndex);
			String pkStringValue = pkValue.getStringValue(WRITER);
			Value tramoValue = pkRecordset.getFieldValue(i,
				tramoIndex);

			if ((pkStringValue.compareToIgnoreCase(pkToFind) == 0)
				&& (tramoValue.toString().compareToIgnoreCase(
					tramo) == 0)) {
			    zoom(pkLayer, i);
			    return;
			}
		    }
		} catch (ReadDriverException e1) {
		    e1.printStackTrace();
		}
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

}
