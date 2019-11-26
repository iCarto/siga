package es.icarto.gvsig.siga.dimensiontool;

import java.awt.Dimension;
import java.util.Iterator;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.plugins.Extension;
import com.iver.andami.preferences.AbstractPreferencePage;
import com.iver.andami.preferences.GenericDlgPreferences;
import com.iver.cit.gvsig.CADExtension;
import com.iver.cit.gvsig.gui.preferences.EditionPreferencePage;
import com.iver.cit.gvsig.gui.preferences.SnapConfigPage;
import com.iver.cit.gvsig.project.documents.view.gui.View;
import com.iver.utiles.extensionPoints.ExtensionPoint;
import com.iver.utiles.extensionPoints.ExtensionPoints;
import com.iver.utiles.extensionPoints.ExtensionPointsSingleton;

public class AddDimensionConfigExtension extends Extension {

    @Override
    public void initialize() {
        CADExtension
                .addCADTool(AddDimensionCADTool.CAD_ID, new AddDimensionCADTool());
        ExtensionPoints extensionPoints = ExtensionPointsSingleton
                .getInstance();
        extensionPoints.add("dimension_add_properties_pages", "snapping",
                SnapConfigPage.class);
    }

    @Override
    public void execute(String actionCommand) {
        EditionPreferencePage pref = new AddDimensionPreferencePage();

        pref.setMapContext(((View) PluginServices.getMDIManager()
                .getActiveWindow()).getMapControl().getMapContext());
        // GridPage gridPage=new GridPage();
        // gridPage.setParentID(pref.getID());
        // FlatnessPage flatnessPage=new FlatnessPage();
        // flatnessPage.setParentID(pref.getID());

        GenericDlgPreferences dlg = new GenericDlgPreferences();
        dlg.addPreferencePage(pref);

        Dimension d = dlg.getSize();
        d.height = pref.getHeight() + 70;
        dlg.setSize(d);

        ExtensionPoints extensionPoints = ExtensionPointsSingleton
                .getInstance();
        ExtensionPoint extensionPoint = (ExtensionPoint) extensionPoints
                .get("dimension_add_properties_pages");
        Iterator iterator = extensionPoint.keySet().iterator();
        while (iterator.hasNext()) {
            try {
                AbstractPreferencePage app = (AbstractPreferencePage) extensionPoint
                        .create((String) iterator.next());
                app.setParentID(pref.getID());
                dlg.addPreferencePage(app);
            } catch (InstantiationException e) {
                NotificationManager.addError(e.getMessage(), e);
            } catch (IllegalAccessException e) {
                NotificationManager.addError(e.getMessage(), e);
            } catch (ClassCastException e) {
                NotificationManager.addError(e.getMessage(), e);
            }
        }

        // dlg.addPreferencePage(gridPage);
        // dlg.addPreferencePage(flatnessPage);
        // dlg.addPreferencePage(fieldExpresionPage);
        dlg.getWindowInfo().setTitle(
                PluginServices.getText(this, "add_dimension_config"));
        dlg.setActivePage(pref);
        PluginServices.getMDIManager().addWindow(dlg);
    }

    @Override
    public boolean isEnabled() {
        return (PluginServices.getMDIManager().getActiveWindow() instanceof View)
                && !AddDimension.isExtensionActive();
    }

    @Override
    public boolean isVisible() {
        return true;
    }

}
