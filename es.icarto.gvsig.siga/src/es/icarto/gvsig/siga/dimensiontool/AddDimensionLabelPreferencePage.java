package es.icarto.gvsig.siga.dimensiontool;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.ImageIcon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import com.iver.andami.PluginServices;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.StoreException;

public class AddDimensionLabelPreferencePage extends AbstractPreferencePage {

    private static final long serialVersionUID = -6351405323528798919L;

    private JLabel jLabel = null;
    private JTextField jTxtDecimals = null;
    private final JCheckBox cbShowUnits = new JCheckBox();
    private JPanel jPanelNord = null;
    private JPanel jPanelSouth = null;
    private boolean changed = false;

    public AddDimensionLabelPreferencePage() {
        super();
        initialize();
    }

    private void initialize() {
        BorderLayout layout = new BorderLayout();
        layout.setHgap(20);

        this.setLayout(layout);

        jLabel = new JLabel();
        jLabel.setText(PluginServices.getText(this,
                "add_dimension_config_decimals") + ":");
        jLabel.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
        jLabel.setName("jLabel");
        jLabel.setBounds(new java.awt.Rectangle(15, 8, 150, 15));
        jLabel.setPreferredSize(new java.awt.Dimension(28, 20));
        jLabel.setHorizontalTextPosition(javax.swing.SwingConstants.LEFT);

        cbShowUnits.setText(PluginServices.getText(this,
                "add_dimension_config_show_units"));
        cbShowUnits.setBounds(new java.awt.Rectangle(15, 35, 150, 15));
        cbShowUnits.setSelected(true);

        this.setSize(new java.awt.Dimension(502, 488));
        this.setPreferredSize(this.getSize());
        // this.add(getJPanelNord(), BorderLayout.NORTH);
        this.add(getJPanelSouth(), BorderLayout.NORTH);
    }

    @Override
    public String getID() {
        return this.getClass().getName();
    }

    @Override
    public String getTitle() {
        return PluginServices.getText(this, "add_dimension_config_labeling");
    }

    @Override
    public JPanel getPanel() {
        return this;
    }

    @Override
    public void initializeValues() {
        jTxtDecimals.setText(new Integer(AddDimension.getNrDecimals())
                .toString());
        cbShowUnits.setSelected(AddDimension.getShowUnits());
    }

    @Override
    public void storeValues() throws StoreException {
        try {
            AddDimension
                    .setNrDecimals(Integer.parseInt(jTxtDecimals.getText()));

        } catch (Exception e) {
            throw new StoreException(PluginServices.getText(this,
                    "add_dimension_config_decimals_invalid"), e);
        }
        AddDimension.setShowUnits(cbShowUnits.isSelected());
    }

    @Override
    public void initializeDefaults() {
        getJTxtDecimals().setText("0");
        cbShowUnits.setSelected(true);
    }

    @Override
    public ImageIcon getIcon() {
        return null;
    }

    protected JTextField getJTxtDecimals() {
        if (jTxtDecimals == null) {
            jTxtDecimals = new JTextField();
            jTxtDecimals.setPreferredSize(new java.awt.Dimension(28, 20));
            jTxtDecimals.setName("jTxtTolerance");
            jTxtDecimals.setHorizontalAlignment(SwingConstants.RIGHT);
            jTxtDecimals.setBounds(new java.awt.Rectangle(145, 8, 39, 18));
            jTxtDecimals.addKeyListener(new KeyListener() {
                @Override
                public void keyPressed(KeyEvent e) {
                    changed = true;
                }

                @Override
                public void keyReleased(KeyEvent e) {
                    changed = true;
                }

                @Override
                public void keyTyped(KeyEvent e) {
                    changed = true;
                }
            });
        }
        return jTxtDecimals;
    }

    private JPanel getJPanelNord() {
        if (jPanelNord == null) {
            jPanelNord = new JPanel();
            jPanelNord.setLayout(null);
            jPanelNord
                    .setComponentOrientation(java.awt.ComponentOrientation.UNKNOWN);
            jPanelNord.setPreferredSize(new java.awt.Dimension(30, 30));
            jPanelNord.add(jLabel, null);
            jPanelNord.add(getJTxtDecimals(), null);
        }
        return jPanelNord;
    }

    private JPanel getJPanelSouth() {
        if (jPanelSouth == null) {
            jPanelSouth = new JPanel();
            jPanelSouth.setLayout(null);
            jPanelSouth.setPreferredSize(new java.awt.Dimension(30, 60));
            jPanelSouth.add(jLabel, null);
            jPanelSouth.add(getJTxtDecimals(), null);
            jPanelSouth.add(cbShowUnits, null);
        }
        return jPanelSouth;
    }

    @Override
    public boolean isValueChanged() {
        return changed;
    }

    @Override
    public void setChangesApplied() {
        changed = false;
    }

}
