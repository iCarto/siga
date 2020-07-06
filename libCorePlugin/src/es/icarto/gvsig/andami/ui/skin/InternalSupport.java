package es.icarto.gvsig.andami.ui.skin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;

import javax.swing.JInternalFrame;
import javax.swing.JLayeredPane;
import javax.swing.JPopupMenu;
import javax.swing.WindowConstants;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public class InternalSupport {

    private static final Logger logger = Logger.getLogger(InternalSupport.class);

    private final WindowSupport fws;
    private final MDIFrame mainFrame;
    private final MyDesktopPane panel;

    public InternalSupport(WindowSupport fws, MyDesktopPane panel) {
        this.fws = fws;
        this.mainFrame = (MDIFrame) PluginServices.getMainFrame();
        this.panel = panel;
    }

    public IWindow addWindow(IWindow iWindow) {
        // Esta llamada a getWindowInfo tiene que estar antes de updateFrameProperties. En una ventana que se setea como
        // Maximizada esa información estará en WindowsInfo, pero no en el JComponent. En updateWindowsFrame se
        // actualiza el WindowsInfo con la información del JInternalFrame (recién creado) y no maximizado porqué no se
        // puede hacer hasta que se añade al panel y por tanto se pierde esa información
        WindowInfo wi = fws.getWindowInfo(iWindow);
        JInternalFrame wnd = fws.getJInternalFrame(iWindow);

        if (iWindow instanceof SingletonWindow) {
            SingletonWindow sv = (SingletonWindow) iWindow;
            fws.openSingletonWindow(sv, wnd);
        }

        wnd.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        wnd.addInternalFrameListener(new FrameListener(fws, mainFrame, panel));

        if (wi.isModeless() || wi.isPalette()) {
            panel.add(wnd, JLayeredPane.PALETTE_LAYER);
            if (wi.isPalette()) {
                wnd.setFocusable(false);
            }
        } else {
            // Que no engañe lo de isModal y isModeless. No son excluyentes. Un IWindow puede tener a false tanto modal
            // como modeless por ejemplo View o Table, y entrarán aquí, a pesar de que en apariencia todos los
            // JInternalFrame son no modales
            panel.add(wnd);
        }

        updateFrameProperties(wnd, wi);
        activateWindow(wnd);
        try {
            wnd.setMaximum(wi.isMaximized());
        } catch (Exception ex) {
            logger.warn("Error: ", ex);
        }

        fws.add(iWindow, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                IWindow v = fws.getWindowById(Integer.parseInt(e.getActionCommand()));
                JInternalFrame f = fws.getJInternalFrame(v);
                activateWindow(f);
            }
        });
        return iWindow;
    }

    private void updateFrameProperties(JInternalFrame frame, WindowInfo wi) {
        int height, width;
        if (wi.isMaximized()) {
            if (wi.getNormalWidth() != -1) {
                width = wi.getNormalWidth();
            } else {
                width = frame.getNormalBounds().width;
            }
            if (wi.getNormalHeight() != -1) {
                height = wi.getNormalHeight();
            } else {
                height = frame.getNormalBounds().height;
            }

            frame.setSize(width, height);
            frame.setLocation(wi.getNormalX(), wi.getNormalY());
        } else {
            if (wi.getWidth() != -1) {
                width = wi.getWidth();
            } else {
                width = frame.getWidth();
            }
            if (wi.getHeight() != -1) {
                height = wi.getHeight();
            } else {
                height = frame.getHeight();
            }
            frame.setSize(width, height);
            frame.setLocation(wi.getX(), wi.getY());
        }
        frame.setTitle(wi.getTitle());
        frame.setVisible(wi.isVisible());
        frame.setResizable(wi.isResizable());
        frame.setIconifiable(wi.isIconifiable());
        frame.setMaximizable(wi.isMaximizable());
        try {
            frame.setMaximum(wi.isMaximized());
        } catch (PropertyVetoException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void activateWindow(JInternalFrame frame) {
        JPopupMenu.setDefaultLightWeightPopupEnabled(false);
        try {
            frame.moveToFront();
            logger.debug("Activando " + frame.getTitle());
            frame.setSelected(true);
            frame.setIcon(false);
        } catch (PropertyVetoException e) {
            logger.error(e);
        }
    }
}
