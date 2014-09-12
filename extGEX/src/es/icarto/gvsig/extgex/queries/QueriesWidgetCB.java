package es.icarto.gvsig.extgex.queries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;

import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.commons.queries.QueriesWidget;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class QueriesWidgetCB implements QueriesWidget {

    private static final Logger logger = Logger
	    .getLogger(QueriesWidgetCB.class);
    private final JComboBox widget;
    private final JButton customBt;
    private final JButton launchBt;

    public QueriesWidgetCB(FormPanel formPanel, String name) {
	widget = (JComboBox) formPanel.getComponentByName(name);
	customBt = (JButton) formPanel
		.getComponentByName(QueriesPanel.ID_CUSTOMQUERIES);
	launchBt = (JButton) formPanel
		.getComponentByName(QueriesPanel.ID_RUNQUERIES);
	initQueriesWidget();
	fillQueriesWidget();
    }

    private void initQueriesWidget() {
	widget.addActionListener(new ActionListener() {

	    @Override
	    public void actionPerformed(ActionEvent e) {
		if (widget.getSelectedItem() != null) {
		    String key = ((KeyValue) widget.getSelectedItem()).getKey();

		    if (key.startsWith("custom")) {
			customBt.setEnabled(true);
			launchBt.setEnabled(false);
		    } else {
			customBt.setEnabled(false);
			launchBt.setEnabled(true);
		    }
		}
	    }
	});
    }

    private void fillQueriesWidget() {
	DBSession dbs = DBSession.getCurrentSession();
	try {
	    String[] orderBy = new String[1];
	    orderBy[0] = DBNames.FIELD_CODIGO_QUERIES;
	    String[][] tableContent = dbs.getTable(DBNames.TABLE_QUERIES,
		    DBNames.SCHEMA_QUERIES, orderBy, false);
	    for (int i = 0; i < tableContent.length; i++) {
		// Table Schema: 0-codigo, 1-consulta(SQL), 2-descripcion
		KeyValue kv = new KeyValue(
			tableContent[i][DBNames.INDEX_CODIGO_QUERIES],
			tableContent[i][DBNames.INDEX_DESCRIPCION_QUERIES]);
		widget.addItem(kv);
	    }
	    widget.addItem(new KeyValue("custom-exp_finca", "Expropiaciones"));
	    widget.addItem(new KeyValue("custom-exp_rv", "Reversiones"));
	} catch (SQLException e) {
	    logger.error(e.getMessage(), e);
	}
    }

    @Override
    public String getQueryId() {
	Object value = widget.getSelectedItem() == null ? null : widget
		.getSelectedItem();
	return ((KeyValue) value).getKey();
    }

    @Override
    public boolean isQueryIdSelected(String id) {
	throw new AssertionError("Not implemented");
    }

}
