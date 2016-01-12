package es.icarto.gvsig.extgex.locators;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JOptionPane;

import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.gui.BasicAbstractWindow;
import es.icarto.gvsig.extgex.locators.actions.ZoomToHandler;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.extgex.utils.retrievers.KeyValueRetriever;
import es.icarto.gvsig.extgex.utils.retrievers.PositionRetriever;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.navtableforms.utils.TOCLayerManager;
import es.udc.cartolab.gvsig.elle.constants.IPositionRetriever;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

@SuppressWarnings("serial")
public class LocatorByMunicipio extends BasicAbstractWindow implements
IPositionRetriever, ActionListener {

    private JComboBox ayuntamiento;
    private JComboBox parroquia;
    private JButton zoomBt;

    public LocatorByMunicipio() {
	super();
	setWindowInfoProperties(WindowInfo.MODELESSDIALOG | WindowInfo.PALETTE);
	this.setWindowTitle("Localizar Municipio o Parroquia");
	initWidgets();
    }

    @Override
    protected String getBasicName() {
	return "LocatorByMunicipio";
    }

    public void initWidgets() {
	ayuntamiento = (JComboBox) formPanel.getComponentByName("ayuntamiento");
	parroquia = (JComboBox) formPanel.getComponentByName("parroquia");
	fillAyuntamiento();
	ayuntamiento.addActionListener(this);
	fillParroquia();
	zoomBt = (JButton) formPanel.getComponentByName("zoom");
	ZoomToHandler zoomToHandler = new ZoomToHandler(this, false);
	zoomBt.addActionListener(zoomToHandler);
    }

    private void fillParroquia() {
	ArrayList<KeyValue> fks = new ArrayList<KeyValue>();
	KeyValueRetriever kvParroquia;
	if (ayuntamiento.getSelectedItem() instanceof KeyValue) {
	    KeyValue concello = new KeyValue(DBNames.COD_CONCELLO,
		    ((KeyValue) ayuntamiento.getSelectedItem()).getKey());
	    fks.add(concello);
	    kvParroquia = new KeyValueRetriever(getParroquiaLayer(),
		    DBNames.COD_PARROQUIA, DBNames.NOME_PARROQUIA, fks);
	    kvParroquia.setOrderBy(DBNames.NOME_PARROQUIA);
	} else {
	    kvParroquia = new KeyValueRetriever(getParroquiaLayer(),
		    DBNames.COD_PARROQUIA, DBNames.NOME_PARROQUIA);
	    kvParroquia.setOrderBy(DBNames.NOME_PARROQUIA);
	}
	parroquia.removeAllItems();
	parroquia.addItem("");
	for (KeyValue kv : kvParroquia.getValues()) {
	    parroquia.addItem(kv);
	}
    }

    private void fillAyuntamiento() {
	KeyValueRetriever kvMunicipio = new KeyValueRetriever(
		getMunicipioLayer(), DBNames.COD_CONCELLO,
		DBNames.NOME_AYUNTAMIENTO);
	kvMunicipio.setOrderBy(DBNames.NOME_AYUNTAMIENTO);
	for (KeyValue kv : kvMunicipio.getValues()) {
	    ayuntamiento.addItem(kv);
	}
    }

    @Override
    public int getPosition() {
	String fieldName = null;
	String fieldValue = null;
	int position = AbstractNavTable.EMPTY_REGISTER;
	FLyrVect layer = getLayer();
	if (layer == null) {
	    JOptionPane.showMessageDialog(this,
		    "Es necesario seleccionar un municipio o parroquia.");
	    position = AbstractNavTable.EMPTY_REGISTER;

	} else if (layer.getName().equalsIgnoreCase(DBNames.LAYER_PARROQUIAS)) {
	    fieldValue = ((KeyValue) parroquia.getSelectedItem()).getKey();
	    fieldName = DBNames.COD_PARROQUIA;
	    PositionRetriever positionRetriever = new PositionRetriever(layer,
		    fieldName, fieldValue);
	    position = positionRetriever.getPosition();
	} else if (layer.getName().equalsIgnoreCase(DBNames.LAYER_MUNICIPIOS)) {
	    fieldName = DBNames.COD_CONCELLO;
	    fieldValue = ((KeyValue) ayuntamiento.getSelectedItem()).getKey();
	    PositionRetriever positionRetriever = new PositionRetriever(layer,
		    fieldName, fieldValue);
	    position = positionRetriever.getPosition();
	}
	return position;
    }

    public FLyrVect getMunicipioLayer() {
	TOCLayerManager toc = new TOCLayerManager();
	return toc.getLayerByName(DBNames.LAYER_MUNICIPIOS);
    }

    public FLyrVect getParroquiaLayer() {
	TOCLayerManager toc = new TOCLayerManager();
	return toc.getLayerByName(DBNames.LAYER_PARROQUIAS);
    }

    @Override
    public FLyrVect getLayer() {
	if (parroquia.getSelectedItem() instanceof KeyValue) {
	    return getParroquiaLayer();
	} else if (ayuntamiento.getSelectedItem() instanceof KeyValue) {
	    return getMunicipioLayer();
	} else {
	    return null;
	}
    }

    @Override
    public void actionPerformed(ActionEvent arg0) {
	fillParroquia();
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
