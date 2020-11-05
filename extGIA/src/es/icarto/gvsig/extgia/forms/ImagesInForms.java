package es.icarto.gvsig.extgia.forms;

import java.sql.Connection;

import javax.swing.JButton;

import com.jeta.forms.components.image.ImageComponent;
import com.jeta.forms.components.panel.FormPanel;

import es.icarto.gvsig.extgia.forms.images.AddImageListener;
import es.icarto.gvsig.extgia.forms.images.DBFacade;
import es.icarto.gvsig.extgia.forms.images.DeleteImageListener;
import es.icarto.gvsig.extgia.forms.images.ImagesDAO;
import es.icarto.gvsig.extgia.forms.images.SaveImageListener;
import es.icarto.gvsig.extgia.forms.images.ShowImageAction;
import es.icarto.gvsig.siga.models.InfoEmpresa;

public class ImagesInForms {

    private final FormPanel formPanel;

    private String deleteImageButtonName = "delete_image_button";

    protected ImageComponent imageComponent;
    protected JButton addImageButton;
    protected JButton deleteImageButton;
    protected JButton saveImageButton;

    protected AddImageListener addImageListener;
    protected DeleteImageListener deleteImageListener;
    protected SaveImageListener saveImageListener;

    private final ImagesDAO dao;

    public ImagesInForms(FormPanel formPanel, String schema, String tablename, String fk) {
        this.formPanel = formPanel;
        Connection connection = DBFacade.getConnection();
        this.dao = new ImagesDAO(connection, schema, tablename, fk);
    }

    /**
     * Set to null to avoid deletion
     */
    public void setDeleteImageButtonName(String deleteImageButtonName) {
        this.deleteImageButtonName = deleteImageButtonName;
    }

    public void setListeners() {
        imageComponent = (ImageComponent) formPanel.getComponentByName("element_image");
        addImageButton = (JButton) formPanel.getComponentByName("add_image_button");

        saveImageButton = (JButton) formPanel.getComponentByName("save_image_button");
        if ((saveImageButton != null && saveImageListener == null)) {
            saveImageListener = new SaveImageListener(imageComponent, addImageButton, dao);
            saveImageButton.addActionListener(saveImageListener);
        }

        if ((deleteImageButtonName != null) && (deleteImageListener == null)) {
            deleteImageButton = (JButton) formPanel.getComponentByName(deleteImageButtonName);
            deleteImageListener = new DeleteImageListener(imageComponent, addImageButton, dao, saveImageButton);
            deleteImageButton.addActionListener(deleteImageListener);
        }

        if (addImageListener == null) {
            addImageListener = new AddImageListener(imageComponent, addImageButton, dao, saveImageButton,
                    deleteImageButton);
            addImageButton.addActionListener(addImageListener);
        }

    }

    public void removeListeners() {
        addImageButton.removeActionListener(addImageListener);
        if (deleteImageButton != null) {
            deleteImageButton.removeActionListener(deleteImageListener);
        }

        if (saveImageButton != null) {
            saveImageButton.removeActionListener(saveImageListener);
        }
    }

    public void fillSpecificValues(String fkValue, String destFolder) {
        if (addImageListener != null) {
            addImageListener.setPkValue(fkValue);
            addImageListener.setDestFolder(destFolder);
        }

        if (deleteImageListener != null) {
            deleteImageListener.setPkValue(fkValue);
        }

        if (saveImageListener != null) {
            saveImageListener.setPkValue(fkValue);
        }

        // Element image
        ShowImageAction showImage = new ShowImageAction(imageComponent, addImageButton, dao, fkValue);
        showImage.resetEnability(saveImageButton, deleteImageButton);

    }

}
