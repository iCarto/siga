package es.icarto.gvsig.extgia.forms.estructuras;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.images.AddImageListener;
import es.icarto.gvsig.extgia.forms.images.DeleteImageListener;
import es.icarto.gvsig.extgia.forms.images.SaveImageListener;

public class CodigoXopa {

    private final FormPanel formPanel;

    private final String deleteImageButtonName = "delete_image_button";

    protected ImageComponent imageComponent;
    protected JButton addImageButton;
    protected JButton deleteImageButton;
    protected JButton saveImageButton;

    protected AddImageListener addImageListener;
    protected DeleteImageListener deleteImageListener;
    protected SaveImageListener saveImageListener;

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
            etiquetaCodigo.setText("Código XOPA");
        } else {
            etiquetaCodigo.setText("Código SGPA");
        }

    }

    public void setForm(AbstractFormWithLocationWidgets abstractFormWithLocationWidgets) {
        this.form = abstractFormWithLocationWidgets;
    }

}
