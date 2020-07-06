package es.icarto.gvsig.andami.ui.skin;

import java.awt.Dimension;
import java.awt.Rectangle;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import com.iver.andami.plugins.PluginClassLoader;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiFrame.NoSuchMenuException;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowProperties;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public class WindowPropertyChangeListener implements PropertyChangeListener {

    private final MDIFrame mainFrame;
    private final Hashtable<WindowInfo, IWindow> infoView;

    private final IWindowProperties singletonProps;
    private final IWindowProperties windowProps;

    public WindowPropertyChangeListener(MDIFrame mainFrame, Hashtable<WindowInfo, IWindow> infoView,
            IWindowProperties singletonProps, IWindowProperties windowProps) {
        this.mainFrame = mainFrame;
        this.infoView = infoView;
        this.singletonProps = singletonProps;
        this.windowProps = windowProps;
    }

    @Override
    public void propertyChange(PropertyChangeEvent evt) {
        WindowInfo winInfo = (WindowInfo) evt.getSource();
        IWindow win = infoView.get(winInfo);
        IWindowProperties windowProperties;

        if (win instanceof SingletonWindow) {
            windowProperties = singletonProps;
            SingletonWindow sw = (SingletonWindow) win;

            if (evt.getPropertyName().equals("x")) {
                windowProperties.setX(sw, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("y")) {
                windowProperties.setY(sw, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("height")) {
                windowProperties.setHeight(sw, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("width")) {
                windowProperties.setWidth(sw, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("maximized")) {
                windowProperties.setMaximized(sw, ((Boolean) evt.getNewValue()).booleanValue());
            } else if (evt.getPropertyName().equals("normalBounds")) {
                windowProperties.setNormalBounds(sw, (Rectangle) evt.getNewValue());
            } else if (evt.getPropertyName().equals("minimumSize")) {
                windowProperties.setMinimumSize(sw, (Dimension) evt.getNewValue());
            } else if (evt.getPropertyName().equals("title")) {
                windowProperties.setTitle(sw, (String) evt.getNewValue());

                try {
                    mainFrame.changeMenuName(new String[] { "Ventana", (String) evt.getOldValue() },
                            (String) evt.getNewValue(), (PluginClassLoader) getClass().getClassLoader());
                } catch (NoSuchMenuException e) {
                    /*
                     * No se hace nada porque puede modificarse el título de
                     * una ventana antes de ser añadida a Andami
                     */
                }
            }
        } else {
            windowProperties = windowProps;
            if (evt.getPropertyName().equals("x")) {
                windowProperties.setX(win, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("y")) {
                windowProperties.setY(win, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("height")) {
                windowProperties.setHeight(win, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("width")) {
                windowProperties.setWidth(win, ((Integer) evt.getNewValue()).intValue());
            } else if (evt.getPropertyName().equals("minimumSize")) {
                windowProperties.setMinimumSize(win, (Dimension) evt.getNewValue());
            } else if (evt.getPropertyName().equals("title")) {
                windowProperties.setTitle(win, (String) evt.getNewValue());
                try {
                    mainFrame.changeMenuName(new String[] { "Ventana", (String) evt.getOldValue() },
                            (String) evt.getNewValue(), (PluginClassLoader) getClass().getClassLoader());
                } catch (NoSuchMenuException e) {
                    /*
                     * No se hace nada porque puede modificarse el título de
                     * una ventana antes de ser añadida a Andami
                     */
                }
            }
        }
    }
}