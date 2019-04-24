package es.udc.cartolab.gvsig.elle.gui.wizard.load;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.geom.Rectangle2D;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.RowFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import es.icarto.gvsig.commons.utils.CollUtils;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.elle.db.DBStructure;
import es.icarto.gvsig.siga.models.CurrentUser;
import es.icarto.gvsig.siga.models.MunicipioConstantes;
import es.udc.cartolab.gvsig.elle.constants.Constant;
import es.udc.cartolab.gvsig.elle.utils.ELLEMap;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class ConstantsPanel extends JPanel {

    private static final Logger logger = Logger.getLogger(ConstantsPanel.class);
    private JTable table;
    private MunicipioConstantes municipioConstantes;
    private CurrentUser user;

    private TableRowSorter<TableModel> sorter;

    public ConstantsPanel(boolean reload) {
        super(new BorderLayout());
        setUpUI();
        setUpData();
        if (reload) {
            preselectConstants();
        }
    }

    private void setUpUI() {
        setUpTable();
        setUpRadioButtonPanel();
    }

    private void setUpTable() {
        table = new JTable();
        table.getTableHeader().setReorderingAllowed(false);
        table.getTableHeader().setResizingAllowed(false);
        JPanel p = new JPanel(new BorderLayout(5, 5));
        p.add(table, BorderLayout.CENTER);

        this.add(new JScrollPane(p), BorderLayout.CENTER);
    }

    private void setUpRadioButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        ButtonGroup btGroup = new ButtonGroup();
        MyItemListener itemListener = new MyItemListener(btGroup);

        JRadioButton todos = new JRadioButton("Todos");
        todos.setActionCommand("Todos");
        panel.add(todos);
        btGroup.add(todos);

        JRadioButton ap = new JRadioButton("AP");
        ap.setActionCommand("AP");
        panel.add(ap);
        btGroup.add(ap);

        JRadioButton ag = new JRadioButton("AG");
        ag.setActionCommand("AG");
        panel.add(ag);
        btGroup.add(ag);
        btGroup.setSelected(todos.getModel(), true);

        todos.addItemListener(itemListener);
        ap.addItemListener(itemListener);
        ag.addItemListener(itemListener);

        this.add(panel, BorderLayout.NORTH);

    }

    private class MyItemListener implements ItemListener {

        private final ButtonGroup btGroup;

        public MyItemListener(ButtonGroup btGroup) {
            this.btGroup = btGroup;
        }

        private class MyRowFilter extends RowFilter<Object, Object> {
            private final int column;

            public MyRowFilter(int column) {
                this.column = column;
            }

            @Override
            public boolean include(javax.swing.RowFilter.Entry<? extends Object, ? extends Object> entry) {
                return !entry.getStringValue(column).isEmpty();
            }

        }

        @Override
        public void itemStateChanged(ItemEvent e) {
            String selected = btGroup.getSelection().getActionCommand();
            RowFilter<Object, Object> filter = null;

            if (selected.equalsIgnoreCase("TODOS")) {
                filter = null;
            }
            if (selected.equalsIgnoreCase("AP")) {
                filter = new MyRowFilter(3);
            } else if (selected.equalsIgnoreCase("AG")) {
                filter = new MyRowFilter(4);
            }

            sorter.setRowFilter(filter);
        }
    }

    private void setUpData() {
        user = new CurrentUser();
        municipioConstantes = new MunicipioConstantes(user);
        final TableModel valueListData = municipioConstantes.getAsTableModel();
        table.setModel(valueListData);
        table.removeColumn(table.getColumnModel().getColumn(0));
        table.removeColumn(table.getColumnModel().getColumn(2));
        table.removeColumn(table.getColumnModel().getColumn(2));
        // size is adjusted for windows L&F
        table.getColumn("Municipio").setPreferredWidth(150);
        table.getColumn("Municipio").setMaxWidth(180);
        table.getColumn("Municipio").setMinWidth(150);
        table.getColumn("Descripción").setMinWidth(480);

        table.getColumnModel().getColumn(0).setCellRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
                    boolean hasFocus, int row, int column) {
                super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                Font font = getFont().deriveFont(Font.BOLD);
                setFont(font);
                return this;
            }
        });

        sorter = new TableRowSorter<TableModel>(table.getModel());
        for (int i = 0; i < table.getModel().getColumnCount(); i++) {
            sorter.setSortable(i, false);
        }

        table.setRowSorter(sorter);
    }

    private void preselectConstants() {
        List<String> constants = ELLEMap.getConstantValuesSelected();
        if (constants.isEmpty()) {
            return;
        }
        for (int i = 0; i < table.getRowCount(); i++) {
            int modelIdx = table.convertRowIndexToModel(i);
            Object id = table.getModel().getValueAt(modelIdx, 0);
            if (constants.contains(id)) {
                table.getSelectionModel().addSelectionInterval(i, i);
            }
        }
    }

    private List<String> getSelectedIds() {
        List<String> values = new ArrayList<String>();
        int[] idx = table.getSelectedRows();
        for (int i : idx) {
            int modelIdx = table.convertRowIndexToModel(i);
            values.add(table.getModel().getValueAt(modelIdx, 0).toString());
        }
        return values;
    }

    private List<Field> getSelectedAsFields() {
        List<Field> values = new ArrayList<Field>();
        int[] idx = table.getSelectedRows();
        for (int i : idx) {
            int modelIdx = table.convertRowIndexToModel(i);
            String id = table.getModel().getValueAt(modelIdx, 0).toString();
            String name = table.getModel().getValueAt(modelIdx, 1).toString();
            values.add(new Field(id, name));
        }
        return values;
    }

    public String buildWhereAndSetConstants() {
        List<String> values = getSelectedIds();
        if (table.getModel().getRowCount() == values.size()) {
            values.clear();
        }
        // TODO: An index on selectedConstant field could speed up the queryS
        String where = "WHERE municipio IN (";
        if (!values.isEmpty()) {

            ELLEMap.setConstantValuesSelected(values);

            for (String s : values) {
                where += "'" + s + "', ";
            }
            where = where.substring(0, where.length() - 2) + ")";

        } else if (sorter.getRowFilter() != null) {
            ArrayList<String> newValues = new ArrayList<String>();
            for (int i = 0; i < table.getRowCount(); i++) {
                int modelIdx = table.convertRowIndexToModel(i);
                String s = table.getModel().getValueAt(modelIdx, 0).toString();
                newValues.add(s);
                where += "'" + s + "', ";
            }
            where = where.substring(0, where.length() - 2) + ")";
            ELLEMap.setConstantValuesSelected(newValues);
        } else if (!user.getArea().equalsIgnoreCase("ambas")) {
            ELLEMap.setConstantValuesSelected(new ArrayList<String>());

            Collection<String> councils = municipioConstantes.getAsIds();
            for (String s : councils) {
                where += "'" + s + "', ";
            }
            where = where.substring(0, where.length() - 2) + ")";

        } else {
            ELLEMap.setConstantValuesSelected(new ArrayList<String>());
            where = "";
        }

        return where;
    }

    public String buildWhereForLoc() {
        List<String> values = getSelectedIds();
        String where = "WHERE cdprov IN (";

        HashSet<String> set = new HashSet<String>();
        if (!values.isEmpty()) {

            for (String s : values) {
                set.add(s.substring(0, 2));
            }

            for (String s : set) {
                where += s + " ,";
            }

            where = where.substring(0, where.length() - 2) + ")";

        } else if (sorter.getRowFilter() != null) {
            for (int i = 0; i < table.getRowCount(); i++) {
                int modelIdx = table.convertRowIndexToModel(i);
                String s = table.getModel().getValueAt(modelIdx, 0).toString();
                set.add(s.substring(0, 2));
            }

            for (String s : set) {
                where += s + " ,";
            }

            where = where.substring(0, where.length() - 2) + ")";
        } else if (!user.getArea().equalsIgnoreCase("ambas")) {
            Collection<String> councils = municipioConstantes.getAsIds();

            for (String s : councils) {
                set.add(s.substring(0, 2));
            }

            for (String s : set) {
                where += s + " ,";
            }
            where = where.substring(0, where.length() - 2) + ")";

        } else {
            where = "";
        }

        return where;

    }

    public String getStatusBarMsg() {

        List<String> values = getSelectedIds();
        if (values.size() != 1) {
            if (user.getArea().equalsIgnoreCase("ambas")) {
                return "Municipio: " + "TODOS";

            } else if (user.getArea().equalsIgnoreCase("norte")) {
                return "Municipio: " + "Área Norte";

            } else {
                return "Municipio: " + "Área Sur";

            }
        } else {
            String msg = "Municipio: " + getSelectedAsFields().get(0);
            return msg;
        }
    }

    public Rectangle2D getZoomGeometry() {
        Constant constant = new Constant(getSelectedIds());
        return constant.getGeometry();

    }

    // TODO. Modificar tabla _constants porque sólo se la toca desde aqui
    private final static String CONSTANTS_TABLE_NAME = "_constants";
    private final static String CONSTANTS_AFFECTED_TABLE_NAME = "nombre_tabla";

    public Collection<String> getTablesAffectedByConstant() {
        try {
            String[][] table = DBSession.getCurrentSession().getTable(CONSTANTS_TABLE_NAME, DBStructure.SCHEMA_NAME,
                    new String[] { CONSTANTS_AFFECTED_TABLE_NAME }, null, null, false);
            return CollUtils.flat(table);
        } catch (SQLException e) {
            logger.error(e.getStackTrace(), e);
        }
        return Collections.emptyList();
    }

}
