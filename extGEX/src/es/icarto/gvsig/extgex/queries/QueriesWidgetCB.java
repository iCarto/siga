package es.icarto.gvsig.extgex.queries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.SQLException;
import java.util.List;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;

import org.apache.log4j.Logger;

import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.commons.queries.QueriesWidget;
import es.icarto.gvsig.extgex.forms.expropiations.ExpropiationsLayerResolver;
import es.icarto.gvsig.extgex.preferences.DBNames;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class QueriesWidgetCB implements QueriesWidget {

    private static final Logger logger = Logger.getLogger(QueriesWidgetCB.class);
    private final JComboBox widget;
    private final JButton customBt;
    private final JButton launchBt;

    public QueriesWidgetCB(FormPanel formPanel, String name) {
        widget = (JComboBox) formPanel.getComponentByName(name);
        customBt = (JButton) formPanel.getComponentByName(QueriesPanel.ID_CUSTOMQUERIES);
        launchBt = (JButton) formPanel.getComponentByName(QueriesPanel.ID_RUNQUERIES);

        initQueriesWidget(formPanel);
        fillQueriesWidget();
    }

    private void initQueriesWidget(final FormPanel formPanel) {
        widget.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (widget.getSelectedItem() == null) {
                    return;
                }

                String key = ((KeyValue) widget.getSelectedItem()).getKey();
                JComboBox tramoCB = (JComboBox) formPanel.getComponentByName("tramo");
                DefaultComboBoxModel model = (DefaultComboBoxModel) tramoCB.getModel();
                if (key.startsWith("custom")) {
                    customBt.setEnabled(true);
                    launchBt.setEnabled(false);

                    // Por defecto están en orden alfabético. Cuando la consulta es "Expropiaciones" quiero que salgan
                    // estos tramos pero al final del combo en un orden predefinido. Si no es Expropiaciones no
                    // quiero que salgan.
                    List<KeyValue> specialTramos = ExpropiationsLayerResolver.getTramosWithHardcodedOrder();
                    for (KeyValue kv : specialTramos) {
                        model.removeElement(kv);
                        model.addElement(kv);
                    }

                } else {
                    customBt.setEnabled(false);
                    launchBt.setEnabled(true);
                    List<KeyValue> specialTramos = ExpropiationsLayerResolver.getTramosWithHardcodedOrder();
                    for (KeyValue kv : specialTramos) {
                        model.removeElement(kv);
                    }
                }
            }
        });
    }

    private void fillQueriesWidget() {
        DBSession dbs = DBSession.getCurrentSession();
        try {
            widget.addItem(new KeyValue("custom-exp_finca", "Expropiaciones"));

            String[] orderBy = new String[1];
            orderBy[0] = DBNames.FIELD_CODIGO_QUERIES;
            String[][] tableContent = dbs.getTable(DBNames.TABLE_QUERIES, DBNames.SCHEMA_QUERIES, orderBy, false);
            for (int i = 0; i < tableContent.length; i++) {
                // Table Schema: 0-codigo, 1-consulta(SQL), 2-descripcion
                KeyValue kv = new KeyValue(tableContent[i][DBNames.INDEX_CODIGO_QUERIES],
                        tableContent[i][DBNames.INDEX_DESCRIPCION_QUERIES]);
                widget.addItem(kv);
            }

        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public String getQueryId() {
        Object value = widget.getSelectedItem() == null ? null : widget.getSelectedItem();
        return ((KeyValue) value).getKey();
    }

    @Override
    public boolean isQueryIdSelected(String id) {
        throw new AssertionError("Not implemented");
    }

}
