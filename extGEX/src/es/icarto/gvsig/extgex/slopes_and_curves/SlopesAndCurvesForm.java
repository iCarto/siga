package es.icarto.gvsig.extgex.slopes_and_curves;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JRadioButton;
import javax.swing.JSpinner;
import javax.swing.JSpinner.NumberEditor;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableModel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.commons.db.ConnectionWrapper;
import es.icarto.gvsig.commons.gui.BasicAbstractWindow;
import es.icarto.gvsig.commons.referencing.GPoint;
import es.icarto.gvsig.extgex.utils.retrievers.KeyValueRetriever;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.elle.constants.ZoomTo;
import es.udc.cartolab.gvsig.users.utils.DBSession;

@SuppressWarnings("serial")
public class SlopesAndCurvesForm extends BasicAbstractWindow implements SingletonWindow {

    final class EmpresaRadioButton implements ItemListener {
        public final String ID_RADIO_AP = "ap";
        private JRadioButton radioAP;

        public final String ID_RADIO_AG = "ag";
        private JRadioButton radioAG;

        public ButtonGroup radioGroup;

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                fillTramo();
            }
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
        }

        protected Component getDefaultFocusComponent() {
            return radioAG;
        }

        public boolean isAP() {
            return radioAP.isSelected();
        }

        public boolean isAG() {
            return radioAG.isSelected();
        }

        public void onOpenDialog() {
            // Nothing to do here
        }
    }

    final class SentidoRadioButton implements ItemListener {

        private JRadioButton creciente;
        private JRadioButton decreciente;
        public ButtonGroup radioGroup;
        private static final int CRECIENTE = 1;
        private static final int DECRECIENTE = 2;

        @Override
        public void itemStateChanged(ItemEvent e) {
            if (e.getStateChange() == ItemEvent.SELECTED) {
                fillPK(true);
            }
        }

        public void initWidgets() {
            creciente = (JRadioButton) formPanel.getComponentByName("creciente");
            decreciente = (JRadioButton) formPanel.getComponentByName("decreciente");
            radioGroup = new ButtonGroup();
            radioGroup.add(creciente);
            radioGroup.add(decreciente);
            radioGroup.clearSelection();
            creciente.setSelected(true);
            decreciente.addItemListener(this);
            creciente.addItemListener(this);
        }

        public boolean isCreciente() {
            return creciente.isSelected();
        }

        public boolean isDecreciente() {
            return decreciente.isSelected();
        }

        public int getCode() {
            int code = isCreciente() ? CRECIENTE : DECRECIENTE;
            return code;
        }

        public void setEnabled(boolean b) {
            creciente.setEnabled(b);
            decreciente.setEnabled(b);
        }
    }

    final class PKSppiner {
        private JLabel minMaxPKLabel;
        private final String ID_PKNUMBER = "pk_number";
        private JSpinner pkNumberSpinner;

        public void initWidgets() {
            minMaxPKLabel = (JLabel) formPanel.getComponentByName("minMaxPKLabel");
            pkNumberSpinner = (JSpinner) formPanel.getComponentByName(ID_PKNUMBER);
            Float cero = new Float(0f);
            SpinnerNumberModel model = new SpinnerNumberModel(cero, cero, cero, new Float("0.01"));
            pkNumberSpinner.setModel(model);
            resetEditor();
            pkNumberSpinner.addChangeListener(new ChangeListener() {
                @Override
                public void stateChanged(ChangeEvent e) {
                    ((JSpinner.DefaultEditor) pkNumberSpinner.getEditor()).getTextField().selectAll();
                    fillData();
                }
            });

        }

        public void setValue(Number v) {
            pkNumberSpinner.setValue(v);
        }

        public void setToMinValue() {
            setValue(getMinimum());
        }

        public void setToMaxValue() {
            setValue(getMaximum());
        }

        public void setToRelative(float relative) {
            Float v = (Float) pkNumberSpinner.getValue();
            Float newV = v + relative;

            Float minimum = getMinimum();
            if (minimum.compareTo(newV) > 0) {
                newV = minimum;
            }

            Float maximum = getMaximum();
            if (maximum.compareTo(newV) < 0) {
                newV = maximum;
            }
            setValue(newV);
        }

        private Float getMaximum() {
            Float maximum = (Float) ((SpinnerNumberModel) pkNumberSpinner.getModel()).getMaximum();
            return maximum;
        }

        private Float getMinimum() {
            Float minimum = (Float) ((SpinnerNumberModel) pkNumberSpinner.getModel()).getMinimum();
            return minimum;
        }

        public void resetRange(Float min, Float max) {
            String label = String.format("%s - %s", SIGAFormatter.formatPkForDisplay(min),
                    SIGAFormatter.formatPkForDisplay(max));
            minMaxPKLabel.setText(label);
            SpinnerNumberModel model = new SpinnerNumberModel(min, min, max, new Float("0.01"));
            pkNumberSpinner.setModel(model);
            resetEditor();
        }

        public String getValue() {
            return pkNumberSpinner.getValue().toString();
        }

        public Float getFloatValue() {
            if (pkNumberSpinner.getValue() == null) {
                return -1f;
            }
            return (Float) pkNumberSpinner.getValue();
        }

        private void resetEditor() {
            NumberEditor editor = new JSpinner.NumberEditor(pkNumberSpinner, "0.000");
            Locale myLocale = new Locale("es", "ES");
            editor.getFormat().setDecimalFormatSymbols(new DecimalFormatSymbols(myLocale));
            editor.getFormat().setGroupingUsed(false);
            final JFormattedTextField textField = editor.getTextField();
            textField.addFocusListener(new FocusAdapter() {
                @Override
                public void focusGained(FocusEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            textField.selectAll();
                        }
                    });
                }

            });
            pkNumberSpinner.setEditor(editor);
        }

        public void setEnabled(boolean b) {
            pkNumberSpinner.setEnabled(b);
            if (!b) {
                pkSpinner.resetRange(0f, 0f);
            }
        }
    }

    final class ToolBar {
        private final JButton[] buttons = new JButton[6];

        public void initWidgets() {
            JButton start = (JButton) formPanel.getComponentByName("inicio_tramo_bt");
            start.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pkSpinner.setToMinValue();
                }
            });
            JButton end = (JButton) formPanel.getComponentByName("fin_tramo_bt");
            end.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pkSpinner.setToMaxValue();
                }
            });

            JButton backward50 = (JButton) formPanel.getComponentByName("retroceder_50_bt");
            backward50.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pkSpinner.setToRelative(-0.05f);
                }
            });
            JButton forward50 = (JButton) formPanel.getComponentByName("avanzar_50_bt");
            forward50.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pkSpinner.setToRelative(+0.05f);
                }
            });
            JButton backward10 = (JButton) formPanel.getComponentByName("retroceder_10_bt");
            backward10.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pkSpinner.setToRelative(-0.01f);
                }
            });
            JButton forward10 = (JButton) formPanel.getComponentByName("avanzar_10_bt");
            forward10.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    pkSpinner.setToRelative(+0.01f);
                }
            });
            buttons[0] = start;
            buttons[1] = end;
            buttons[2] = backward50;
            buttons[3] = backward10;
            buttons[4] = forward50;
            buttons[5] = forward10;

        }

        public void setEnabled(boolean b) {
            for (JButton bt : buttons) {
                bt.setEnabled(b);
            }
        }
    }

    public static final List<String> ORDERED_AP_ROADS = new ArrayList<String>(Arrays.asList("AP-9", "AP-9F", "AP-9M",
            "AP-9V", "PO-10", "PO-11", "AP-9_FS", "AP-9_SF", "AP-9_VB", "AP-9_BV", "AP-9_TB", "AP-9_BT"));

    public static final List<String> ORDERED_AG_ROADS = new ArrayList<String>(
            Arrays.asList("AG-55", "AG-57", "AG-57N"));

    private static final String TRAMO_FIELD = "tramo";

    public final String ID_TRAMO = "tramo";
    private JComboBox tramoCB;

    private final EjeCalibrado ejeCalibrado;
    private final EmpresaRadioButton empresaRadioButton;
    private final SentidoRadioButton sentidoRadioButton;
    private final PKSppiner pkSpinner;
    private final ToolBar toolbar;

    public SlopesAndCurvesForm() {
        super();
        setWindowInfoProperties(WindowInfo.PALETTE);
        this.setWindowTitle("SlopesAndCurvesForm");
        this.ejeCalibrado = new EjeCalibrado();
        this.empresaRadioButton = new EmpresaRadioButton();
        this.pkSpinner = new PKSppiner();
        this.sentidoRadioButton = new SentidoRadioButton();
        this.toolbar = new ToolBar();
        initWidgets();
    }

    public void initWidgets() {
        empresaRadioButton.initWidgets();
        sentidoRadioButton.initWidgets();
        toolbar.initWidgets();

        tramoCB = (JComboBox) formPanel.getComponentByName(ID_TRAMO);
        tramoCB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillPK(false);
            }
        });

        pkSpinner.initWidgets();

        fillTramo();

    }

    @Override
    public FormPanel getFormPanel() {
        FormPanel fp = super.getFormPanel();
        fp.setBorder(null);
        return fp;
    }

    @Override
    public void openDialog() {
        super.openDialog();
        empresaRadioButton.onOpenDialog();
        if (getDefaultButton() != null) {
            // Se puede meter esto en AbstractIWindow
            getRootPane().setDefaultButton(getDefaultButton());
        }
        fillData();
    }

    @Override
    protected String getBasicName() {
        return "SlopesAndCurvesForm";
    }

    private void fillTramo() {
        List<String> comboTramos = new ArrayList<String>();
        KeyValueRetriever kvPks = new SlopesAndCurvesKeyValueRetriever(TRAMO_FIELD);
        boolean onlyAG = empresaRadioButton.isAG();
        kvPks.setOrderBy(TRAMO_FIELD);
        ArrayList<String> distinctValues = new ArrayList<String>();
        for (KeyValue kv : kvPks.getValues()) {
            if (!distinctValues.contains(kv.getValue()) && (onlyAG == kv.getValue().toString().startsWith("AG-"))) {
                distinctValues.add(kv.getValue());
            }
        }
        List<String> orderedValues = onlyAG ? ORDERED_AG_ROADS : ORDERED_AP_ROADS;
        for (String dkv : orderedValues) {
            if (distinctValues.contains(dkv)) {
                comboTramos.add(dkv);
            }
        }
        for (String dkv : distinctValues) {
            if (!orderedValues.contains(dkv)) {
                comboTramos.add(dkv);
            }
        }
        tramoCB.removeAllItems();
        // Workaround, for no data
        if (comboTramos.isEmpty()) {
            sentidoRadioButton.setEnabled(false);
            tramoCB.setEnabled(false);
            pkSpinner.setEnabled(false);
            toolbar.setEnabled(false);
            return;
        } else {
            sentidoRadioButton.setEnabled(true);
            tramoCB.setEnabled(true);
            pkSpinner.setEnabled(true);
            toolbar.setEnabled(true);
        }

        for (String s : comboTramos) {
            tramoCB.addItem(s);
        }

        fillPK(false);
    }

    private void fillPK(boolean tryToKeepPreviousValue) {
        if (tramoCB.getSelectedItem() == null) {
            return;
        }
        Float prevValue = pkSpinner.getFloatValue();

        String tramo = tramoCB.getSelectedItem().toString();
        int sentido = sentidoRadioButton.getCode();
        Float[] minMaxPk = ejeCalibrado.getMinMaxPk(tramo, sentido);
        pkSpinner.resetRange(minMaxPk[0], minMaxPk[1]);

        if (tryToKeepPreviousValue) {
            Float min = minMaxPk[0];
            Float max = minMaxPk[1];
            float distMin = min - prevValue;
            float distMax = prevValue - max;
            if (distMin < 0 && distMax < 0) {
                pkSpinner.setValue(prevValue);
            } else {
                if (Math.abs(distMin) < Math.abs(distMax)) {
                    pkSpinner.setValue(min);
                } else {
                    pkSpinner.setValue(max);
                }
            }
        }

        fillData();
    }

    @Override
    protected JButton getDefaultButton() {
        return (JButton) formPanel.getComponentByName("avanzar_10_bt");
    }

    @Override
    protected Component getDefaultFocusComponent() {
        return empresaRadioButton.getDefaultFocusComponent();
    }

    @Override
    protected WindowInfo getWindowInfo(Component frame) {
        WindowInfo wInfo = super.getWindowInfo(frame);
        wInfo.setHeight(wInfo.getHeight() - 15);
        return wInfo;
    }

    @Override
    public Object getWindowModel() {
        return "SlopesAndCurvesForm";
    }

    private void fillData() {
        ConnectionWrapper c = new ConnectionWrapper(DBSession.getCurrentSession().getJavaConnection());
        String query = "SELECT giro, a, radio, pendiente, x, y FROM audasa_aplicaciones.get_slopes_and_curves(%s, %s, '%s', NULL) AS (giro text, a float, radio float, pendiente float, x float, y float);";
        // in_sentido integer, in_pk double precision, in_tramo text, id_tramo integer
        // OUT out_giro text, OUT out_a double precision, OUT out_radio double precision, OUT out_pendiente double
        // precision, OUT x double precision, OUT y double precision
        String sentido = sentidoRadioButton.isCreciente() ? "1" : "2";

        query = String.format(query, sentido, pkSpinner.getValue(), tramoCB.getSelectedItem().toString());
        DefaultTableModel execute = c.execute(query);
        myformat(execute, 0, "giro");
        myformat(execute, 1, "a");
        myformat(execute, 2, "radio");
        myformat(execute, 3, "pendiente");
        myformat(execute, 4, "x");
        myformat(execute, 5, "y");

        IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
        View view = (View) iWindow;
        ZoomTo z = new ZoomTo(view.getMapControl());
        Number x = (Number) execute.getValueAt(0, 4);
        Number y = (Number) execute.getValueAt(0, 5);
        z.zoom(new GPoint(view.getProjection(), x.doubleValue(), y.doubleValue()), true);
    }

    private void myformat(DefaultTableModel t, int pos, String name) {
        Object o = t.getValueAt(0, pos);
        String v = SIGAFormatter.formatValue(o);
        String vs = v.isEmpty() ? " - " : v;
        ((JTextField) formPanel.getComponentByName(name)).setText(vs);
    }
}
