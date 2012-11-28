package es.icarto.gvsig.extgia.forms.utils;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.swing.JComboBox;
import javax.swing.JComponent;

import org.apache.log4j.Logger;

import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.navtableforms.AbstractForm;
import es.icarto.gvsig.navtableforms.ormlite.domain.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public abstract class AbstractFormWithLocationWidgets extends AbstractForm {

    public static final String AREA_MANTENIMIENTO = "area_mantenimiento";
    private static final String BASE_CONTRATISTA = "base_contratista";
    private static final String TRAMO = "tramo";
    private static final String TIPO_VIA = "tipo_via";
    private static final String NOMBRE_VIA = "nombre_via";


    private JComboBox areaMantenimientoWidget;
    private JComboBox baseContratistaWidget;
    private JComboBox tramoWidget;
    private JComboBox tipoViaWidget;
    private JComboBox nombreViaWidget;

    private UpdateBaseContratistaListener updateBaseContratistaListener;
    private UpdateTramoListener updateTramoListener;
    private UpdateTipoViaListener updateTipoViaListener;
    private UpdateNombreViaListener updateNombreViaListener;

    private String areaMantenimiento;
    private String baseContratista;
    private String tramo ;
    private String tipoVia;
    private String nombreVia;

    public AbstractFormWithLocationWidgets (FLyrVect layer) {
	super(layer);
	initWidgets();
	setListeners();
    }

    @Override
    protected void setListeners() {
	super.setListeners();

	HashMap<String, JComponent> widgets = getWidgetComponents();

	areaMantenimientoWidget = (JComboBox) widgets.get(AREA_MANTENIMIENTO);
	baseContratistaWidget = (JComboBox) widgets.get(BASE_CONTRATISTA);
	tramoWidget = (JComboBox) widgets.get(TRAMO);
	tipoViaWidget = (JComboBox) widgets.get(TIPO_VIA);
	nombreViaWidget = (JComboBox) widgets.get(NOMBRE_VIA);

	updateBaseContratistaListener = new UpdateBaseContratistaListener();
	updateTramoListener = new UpdateTramoListener();
	updateTipoViaListener = new UpdateTipoViaListener();
	updateNombreViaListener = new UpdateNombreViaListener();

	areaMantenimientoWidget.addActionListener(updateBaseContratistaListener);
	baseContratistaWidget.addActionListener(updateTramoListener);
	tramoWidget.addActionListener(updateTipoViaListener);
	tipoViaWidget.addActionListener(updateNombreViaListener);
    }

    public class UpdateBaseContratistaListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (!isFillingValues()) {
		String id = ((KeyValue) areaMantenimientoWidget.getSelectedItem()).getKey();
		String getBaseContratistaQuery =
			"SELECT id, item FROM audasa_extgia_dominios.base_contratista" +
				" WHERE id_am = " + id + ";";
		baseContratistaWidget.removeAllItems();
		baseContratistaWidget.addItem("");
		for (KeyValue value: getKeyValueListFromSql(getBaseContratistaQuery)) {
		    baseContratistaWidget.addItem(value);
		}
	    }
	}

    }

    public class UpdateTramoListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (!isFillingValues() && baseContratistaWidget.getItemCount()!=0) {
		String id = ((KeyValue) baseContratistaWidget.getSelectedItem()).getKey();
		String getTramoQuery =
			"SELECT id, item FROM audasa_extgia_dominios.tramo" +
				" WHERE id_bc = " + id + ";";
		tramoWidget.removeAllItems();
		tramoWidget.addItem("");
		for (KeyValue value: getKeyValueListFromSql(getTramoQuery)) {
		    tramoWidget.addItem(value);
		}
	    }
	}

    }

    public class UpdateTipoViaListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (!isFillingValues() && tramoWidget.getItemCount()!=0) {
		String id = ((KeyValue) tramoWidget.getSelectedItem()).getKey();
		String getTipoViaQuery =
			"SELECT id, item  FROM audasa_extgia_dominios.tipo_via" +
				" WHERE id_tramo = " + id + ";";
		tipoViaWidget.removeAllItems();
		tipoViaWidget.addItem("");
		for (KeyValue value: getKeyValueListFromSql(getTipoViaQuery)) {
		    tipoViaWidget.addItem(value);
		}
	    }
	}

    }

    public class UpdateNombreViaListener implements ActionListener {

	@Override
	public void actionPerformed(ActionEvent arg0) {
	    if (!isFillingValues() && tipoViaWidget.getItemCount()!=0) {
		String id = ((KeyValue) tipoViaWidget.getSelectedItem()).getKey();
		String getNombreViaQuery =
			"SELECT id, item FROM audasa_extgia_dominios.nombre_via" +
				" WHERE id_tv = " + id + ";";
		nombreViaWidget.removeAllItems();
		nombreViaWidget.addItem("");
		for (KeyValue value: getKeyValueListFromSql(getNombreViaQuery)) {
		    nombreViaWidget.addItem(value);
		}
	    }
	}

    }

    public JComboBox getAreaMantenimientoWidget() {
	return areaMantenimientoWidget;
    }

    public JComboBox getBaseContratistaWidget() {
	return baseContratistaWidget;
    }

    public JComboBox getTramoWidget() {
	return tramoWidget;
    }

    public JComboBox getTipoViaWidget() {
	return tipoViaWidget;
    }

    public JComboBox getNombreViaWidget() {
	return nombreViaWidget;
    }

    private ArrayList<KeyValue> getKeyValueListFromSql(String query) {
	ArrayList<KeyValue> values = new ArrayList<KeyValue>();
	PreparedStatement statement = null;
	Connection connection = DBSession.getCurrentSession().getJavaConnection();
	try {
	    statement = connection.prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    statement = connection.prepareStatement(query);
	    while (rs.next()) {
		values.add(new KeyValue(rs.getString(1), rs.getString(2)));
	    }
	    return values;
	} catch (SQLException e) {
	    e.printStackTrace();
	    return new ArrayList<KeyValue>();
	}
    }

    @Override
    protected abstract void fillSpecificValues();

    @Override
    public abstract FormPanel getFormBody();

    @Override
    public abstract Logger getLoggerName();

    @Override
    public abstract String getXMLPath();
}
