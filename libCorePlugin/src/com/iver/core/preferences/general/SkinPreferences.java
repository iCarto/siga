package com.iver.core.preferences.general;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import com.iver.andami.Launcher;
import com.iver.andami.PluginServices;
import com.iver.andami.plugins.config.generate.PluginConfig;
import com.iver.andami.plugins.config.generate.SkinExtension;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.StoreException;
import com.iver.utiles.XMLEntity;
import com.iver.utiles.swing.JComboBox;

@SuppressWarnings("serial")
public class SkinPreferences extends AbstractPreferencePage {

    private final String id;
    private final ImageIcon icon;
    private Vector<String> listSkinsPlugins;
    private JComboBox comboBox;
    private String skinName = Launcher.DEFAULT_SKIN;

    public SkinPreferences() {
        super();
        id = this.getClass().getName();
        setParentID(GeneralPage.id);
        icon = PluginServices.getIconTheme().get("gnome-settings-theme");
    }

    @Override
    public void setChangesApplied() {
    }

    @Override
    public void storeValues() throws StoreException {
        PluginServices ps = PluginServices.getPluginServices("com.iver.core");
        XMLEntity xml = ps.getPersistentXML();
        xml.putProperty("Skin-Selected", skinName);
    }

    @Override
    public String getID() {
        return id;
    }

    @Override
    public ImageIcon getIcon() {
        return icon;
    }

    @Override
    public JPanel getPanel() {

        if (comboBox == null) {
            comboBox = getComboBox();
            addComponent(new JLabel(PluginServices.getText(this, "skin_label")));
            addComponent(comboBox);
        }

        return this;
    }

    private JComboBox getComboBox() {
        comboBox = new JComboBox(listSkinsPlugins);

        comboBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JComboBox cb = (JComboBox) e.getSource();
                String newSkinName = (String) cb.getSelectedItem();
                if (newSkinName != null) {
                    if (!newSkinName.equals(skinName)) {
                        skinName = newSkinName;
                        JOptionPane.showMessageDialog(null, PluginServices.getText(this, "skin_message"));
                    }
                }
            }

        });

        comboBox.setSelectedItem(skinName);
        return comboBox;
    }

    @Override
    public String getTitle() {
        return PluginServices.getText(this, "skin");
    }

    @Override
    public void initializeDefaults() {
    }

    @Override
    public void initializeValues() {

        listSkinsPlugins = new Vector<String>();

        HashMap pluginsConfig = Launcher.getPluginConfig();
        Iterator i = pluginsConfig.keySet().iterator();

        while (i.hasNext()) {
            String name = (String) i.next();
            PluginConfig pc = (PluginConfig) pluginsConfig.get(name);

            if (pc.getExtensions().getSkinExtension() != null) {
                SkinExtension[] se = pc.getExtensions().getSkinExtension();
                for (int j = 0; j < se.length; j++) {

                    listSkinsPlugins.add(se[j].getClassName());
                    System.out.println("plugin de skin + name");
                }
            }
        }

        PluginServices ps = PluginServices.getPluginServices("com.iver.core");
        XMLEntity xml = ps.getPersistentXML();
        if (xml.contains("Skin-Selected")) {
            skinName = xml.getStringProperty("Skin-Selected");
        }

    }

    @Override
    public boolean isValueChanged() {
        return true;
    }

}
