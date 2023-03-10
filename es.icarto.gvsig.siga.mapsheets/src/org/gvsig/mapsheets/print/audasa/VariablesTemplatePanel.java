package org.gvsig.mapsheets.print.audasa;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

@SuppressWarnings("serial")
public class VariablesTemplatePanel extends JPanel implements IWindow, ActionListener {

    private String template = null;
    private ArrayList<Component> componentsList = null;

    private final int width = 520;
    private int height = 320;
    private AudasaTemplate audasaTemplate = null;

    private WindowInfo windowsInfo = null;

    public VariablesTemplatePanel(String template) {
        super();
        this.setLayout(null);
        this.template = template;
        audasaTemplate = new AudasaTemplate();
        init();
    }

    private void init() {
        componentsList = new ArrayList<Component>();
        if (template.equals(AudasaPreferences.A3_DIMENSIONES)
                || template.equals(AudasaPreferences.A3_DIMENSIONES_LOCALIZADOR)) {
            componentsList = TemplatePanel.create(AudasaPreferences.A3_DIMENSIONES);
        } else if (template.equals(AudasaPreferences.A3_POLICIA_MARGENES)
                || template.equals(AudasaPreferences.A3_POLICIA_MARGENES_LEYENDA)
                || template.equals(AudasaPreferences.A4_POLICIA_MARGENES)
                || template.equals(AudasaPreferences.A4_POLICIA_MARGENES_LEYENDA)
                || template.equals(AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL)
                || template.equals(AudasaPreferences.A3_POLICIA_MARGENES_VERTICAL_LEYENDA)
                || template.equalsIgnoreCase(AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL)
                || template.equals(AudasaPreferences.A4_POLICIA_MARGENES_VERTICAL_LEYENDA)) {
            height = 200;
            componentsList = TemplatePanel.create(AudasaPreferences.A4_POLICIA_MARGENES);
        } else if (template.equals(AudasaPreferences.A4_CONSULTAS_VERTICAL)
                || template.equals(AudasaPreferences.A4_CONSULTAS_VERTICAL_LOCALIZADOR)) {
            height = 200;
            componentsList = TemplatePanel.create(AudasaPreferences.A4_CONSULTAS_VERTICAL);
        } else {
            componentsList = TemplatePanel.create(AudasaPreferences.A4_CONSULTAS);
        }
        for (Component c : componentsList) {
            this.add(c, null);
            if (c instanceof JButton) {
                ((JButton) c).addActionListener(this);
            }
        }
    }

    public AudasaTemplate getAudasaTemplateVariables() {
        return audasaTemplate;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Component c : componentsList) {
            if (c instanceof JTextField) {
                audasaTemplate.setProperty(c.getName(), ((JTextField) c).getText());
            }
        }
        PluginServices.getMDIManager().closeWindow(this);
    }

    @Override
    public WindowInfo getWindowInfo() {
        if (windowsInfo == null) {
            windowsInfo = new WindowInfo(WindowInfo.MODALDIALOG | WindowInfo.RESIZABLE);
            windowsInfo.setHeight(height);
            windowsInfo.setTitle("Variables del mapa");
            windowsInfo.setWidth(width);
        }
        return windowsInfo;
    }

    @Override
    public Object getWindowProfile() {
        return WindowInfo.DIALOG_PROFILE;
    }

}
