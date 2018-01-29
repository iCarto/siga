package es.icarto.gvsig.siga;

import java.awt.Component;
import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.AbstractButton;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.iver.andami.PluginServices;
import com.iver.andami.iconthemes.IIconTheme;
import com.iver.andami.ui.mdiFrame.MainFrame;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.MapControl;
import com.iver.cit.gvsig.fmap.layers.FLayer;
import com.iver.cit.gvsig.fmap.tools.Behavior.PolygonBehavior;

import es.icarto.gvsig.commons.AbstractExtension;
import es.icarto.gvsig.siga.printselected.PrintSelectedListener;
import es.icarto.gvsig.siga.printselected.Visibility;

public class PrintSelectedExtension extends AbstractExtension {

    private FLayer[] actives;

    // public visibility. static to be accessed from the listener. Awful. I know.
    public static final List<FLayer> clipedLayers = new ArrayList<FLayer>();
    // public visibility. static to be accessed from the listener. Awful. I know.
    public static final List<Visibility> dbLayers = new ArrayList<Visibility>();

    // tracks the last folder selected by the user in this session
    private File folder = new File(System.getProperty("user.home"));

    @Override
    public void execute(String actionCommand) {

        lazyInitialization();
        boolean toolInUse = getView().getMapControl().getCurrentTool().equals(id);

        if (!clipedLayers.isEmpty() || toolInUse) {
            revertExecute();
        } else {
            doExecute();
        }
    }

    private void lazyInitialization() {
        IIconTheme theme = PluginServices.getIconTheme();
        String iconName = id + ".revert";
        if (!theme.exists(id + ".revert")) {
            // lazy loading of revert button
            URL iconUrl = this.getClass().getClassLoader().getResource("images/" + iconName.toLowerCase() + ".png");
            PluginServices.getIconTheme().registerDefault(iconName, iconUrl);
            registerCursor();
        }
    }

    private void doExecute() {

        ArrayList<FLayer> layers = new ArrayList<FLayer>(Arrays.asList(actives));
        String tmpFolder = getFolder(folder);
        if (tmpFolder == null) {
            return;
        }
        if (!new File(tmpFolder).isDirectory()) {
            String title = "Error";
            String message = "El directorio debe existir";
            Component mainFrame = (Component) PluginServices.getMainFrame();
            JOptionPane.showMessageDialog(mainFrame, message, title, JOptionPane.ERROR_MESSAGE);
            return;
        }
        folder = new File(tmpFolder);
        setupTool(layers, tmpFolder);

        getView().getMapControl().setTool(id);

    }

    private void revertExecute() {
        // From EliminarCapaTocMenuEntry
        MapContext mapContext = getView().getMapControl().getMapContext();
        mapContext.beginAtomicEvent();
        for (FLayer l : clipedLayers) {
            l.getParentLayer().removeLayer(l);
        }
        clipedLayers.clear();

        for (Visibility v : dbLayers) {
            v.layer.setVisible(v.visible);
        }
        dbLayers.clear();
        cleanupTool();
        useExecuteBt();
        mapContext.endAtomicEvent();
        mapContext.invalidate();
        PluginServices.getMainFrame().enableControls();
    }

    private void useExecuteBt() {
        useIcon(id);
    }

    private void useRevertBt() {
        useIcon(id + ".revert");
    }

    private void useIcon(String iconName) {
        MainFrame f = PluginServices.getMainFrame();
        AbstractButton bt = (AbstractButton) f.getComponentByName(id);
        ImageIcon imageIcon = PluginServices.getIconTheme().get(iconName);
        bt.setIcon(imageIcon);
    }

    @Override
    public boolean isEnabled() {
        if (getView() == null) {
            return false;
        }

        boolean toolInUse = getView().getMapControl().getCurrentTool().equals(id);
        if (!clipedLayers.isEmpty() || toolInUse) {
            return true;
        }
        MapContext mapContext = getView().getMapControl().getMapContext();
        actives = mapContext.getLayers().getActives();
        return actives.length > 0;
    }

    private String getFolder(File folder) {
        JFileChooser fileChooser = new JFileChooser(folder);
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        Component mainFrame = (Component) PluginServices.getMainFrame();
        int showSaveDialog = fileChooser.showSaveDialog(mainFrame);
        if (showSaveDialog != JFileChooser.APPROVE_OPTION) {
            return null;
        }
        File newFile = fileChooser.getSelectedFile();
        String path = newFile.getAbsolutePath();
        if (!path.endsWith(File.separator)) {
            path = path + File.separator;
        }
        return path;
    }

    private void setupTool(List<FLayer> layers, String folder) {
        MapControl mapControl = getView().getMapControl();

        PrintSelectedListener listener = new PrintSelectedListener(mapControl, layers, folder);
        listener.setCursorImage(id + ".cursor");

        mapControl.addMapTool(id, new PolygonBehavior(listener));
        useRevertBt();
    }

    // public to be accessed from the listener. Awful. I know.
    public void cleanupTool() {
        MapControl mapControl = getView().getMapControl();
        mapControl.setTool("zoomIn");
        mapControl.getNamesMapTools().remove(id);
    }

}
