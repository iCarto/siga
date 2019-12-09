package es.icarto.gvsig.extgia.forms.estructuras;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;

public class CodigoXopa {

    private final FormPanel formPanel;
    private AbstractFormWithLocationWidgets form;

    private final JLabel etiquetaCodigo;
    private final JComboBox tramoCB;

    private ActionListener listener;

    public CodigoXopa(FormPanel formPanel, JComboBox tramoCB) {
        this.formPanel = formPanel;
        this.etiquetaCodigo = (JLabel) this.formPanel.getComponentByName("etiqueta_codigo");
        this.tramoCB = tramoCB;
    }

    public void setListeners() {
        this.listener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                fillSpecificValues();
            }
        };
        tramoCB.addActionListener(this.listener);
    }

    public void removeListeners() {
        tramoCB.removeActionListener(this.listener);
    }

    public void fillSpecificValues() {
        Object selectedItem = tramoCB.getSelectedItem();
        if (selectedItem == null) {
            return;
        }
        String selectedTramo = selectedItem.toString();
        if (selectedTramo.startsWith("AG")) {
            etiquetaCodigo.setText("Código XOPA:");
        } else {
            etiquetaCodigo.setText("Código SGPA:");
        }

    }

    public void setForm(AbstractFormWithLocationWidgets abstractFormWithLocationWidgets) {
        this.form = abstractFormWithLocationWidgets;
    }

}
