package es.icarto.gvsig.extgia.forms;

import java.sql.Connection;
import java.sql.SQLException;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.commons.image.ImageWindow;
import es.icarto.gvsig.extgia.forms.images.ImagesDAO;
import es.icarto.gvsig.extgia.preferences.DBFieldNames;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.icarto.gvsig.siga.PreferencesPage;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class ImagesInView {

    private static final Logger logger = Logger.getLogger(ImagesInView.class);
    private Elements element;

    public ImagesInView(FLyrVect layer, int pos) {
        for (Elements e : Elements.values()) {
            if (e.layerName.equals(layer.getName())) {
                element = e;
                break;
            }
        }
        String table = element.imagenesTableName;
        

        // TODO: Probably all insert image methods should be in core gvSIG classes, now that 
        // the drivers are updated
        Connection connection = DBSession.getCurrentSession().getJavaConnection();
        ImagesDAO dao = new ImagesDAO(connection, DBFieldNames.GIA_SCHEMA, table, element.pk);

        AbstractFormWithLocationWidgets form = (AbstractFormWithLocationWidgets) LaunchGIAForms
                .getFormDependingOfLayer(layer);
        form.init();
        form.setPosition(pos);

        String pkValue = form.getPrimaryKeyValue();
        String title = layer.getName() + ": " + pkValue;
        title = title.intern();
        try {
            ImageIcon imageIcon = dao.getImageIconFromDb(pkValue);
            if (imageIcon == null) {
                imageIcon = getUnavailableImageIcon();
            }

            ImageWindow panel = new ImageWindow(title, imageIcon);
            panel.setUniqueID(title);
            PluginServices.getMDIManager().addWindow(panel);

        } catch (SQLException e1) {
            logger.error(e1.getStackTrace(), e1);
        }
    }

    private ImageIcon getUnavailableImageIcon() {
        return new ImageIcon(PreferencesPage.IMG_UNAVAILABLE);
    }
}
