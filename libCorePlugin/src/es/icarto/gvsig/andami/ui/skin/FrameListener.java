package es.icarto.gvsig.andami.ui.skin;

import java.beans.PropertyVetoException;
import java.util.HashMap;

import javax.swing.JInternalFrame;
import javax.swing.event.InternalFrameEvent;
import javax.swing.event.InternalFrameListener;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiFrame.MDIFrame;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public class FrameListener implements InternalFrameListener {

    private static final Logger logger = Logger.getLogger(FrameListener.class);
    private final WindowSupport fws;
    private final MDIFrame mainFrame;
    private final MyDesktopPane panel;

    public FrameListener(WindowSupport fws, MDIFrame mainFrame, MyDesktopPane panel) {
        this.fws = fws;
        this.mainFrame = mainFrame;
        this.panel = panel;
    }

    @Override
    public void internalFrameActivated(InternalFrameEvent e) {

        IWindow panel = fws.getWindow((JInternalFrame) e.getSource());

        WindowInfo wi = fws.getWindowInfo(panel);
        if (wi.isPalette()) {
            return;
        }

        fws.setActive(panel);

        JInternalFrame frame = fws.getJInternalFrame(panel);

        if (wi.isMaximizable()) {
            frame.setMaximizable(true);
        }
        if (!frame.isMaximizable() && frame.isMaximum()) {
            try {
                frame.setMaximum(false);
            } catch (PropertyVetoException e1) {
            }
        }
        mainFrame.enableControls();
        if (wi.getSelectedTools() == null) {
            // this is the first time this window is activated
            wi.setSelectedTools(new HashMap(mainFrame.getInitialSelectedTools()));
        }
        mainFrame.setSelectedTools(wi.getSelectedTools());
        callWindowActivated(panel);

    }

    @Override
    public void internalFrameClosed(InternalFrameEvent e) {
    }

    @Override
    public void internalFrameClosing(InternalFrameEvent e) {
        JInternalFrame c = (JInternalFrame) e.getSource();
        WindowInfo wi = fws.getWindowInfo(fws.getWindow(c));

        IWindow win = fws.getWindow(c);
        callWindowClosed(win);
        if (win instanceof SingletonWindow) {
            fws.closeWindowSingleton((SingletonWindow) win);
        }

        fws.closeWindow(win);

        panel.remove(c);

        fws.remove(win);

        if (!wi.isPalette()) {
            mainFrame.enableControls();
        }
        panel.repaint();

        // Para activar el JInternalFrame desde la que hemos
        // abierto la ventana que estamos cerrando
        IWindow lastWindow = fws.getActiveWindow();
        // La activamos
        if (lastWindow != null) {
            logger.debug(PluginServices.getText(this, "Devuelvo_el_foco_a_") + lastWindow.getWindowInfo().getTitle());
            JInternalFrame frame = fws.getJInternalFrame(lastWindow);
            try {
                frame.setSelected(true);
            } catch (PropertyVetoException e1) {
                logger.error(e1.getMessage(), e1);
            }
        }

    }

    @Override
    public void internalFrameDeactivated(InternalFrameEvent e) {
        JInternalFrame c = (JInternalFrame) e.getSource();
        IWindow win = fws.getWindow(c);
        if (win != null) {
            WindowInfo wi = fws.getWindowInfo(win);
            if (wi.isPalette()) {
                return;
            }

        }

    }

    @Override
    public void internalFrameDeiconified(InternalFrameEvent e) {
        mainFrame.enableControls();
    }

    @Override
    public void internalFrameIconified(InternalFrameEvent e) {
        mainFrame.enableControls();
    }

    @Override
    public void internalFrameOpened(InternalFrameEvent e) {
    }

    /**
     * If <code>window</code> implements IWindowListener, sent it the
     * windowActivated event.
     *
     * @param window
     *            The IWindow which has to be notified.
     */
    private void callWindowClosed(IWindow window) {
        if (window instanceof IWindowListener) {
            ((IWindowListener) window).windowClosed();
        }
    }

    /**
     * If <code>window</code> implements IWindowListener, sent it the
     * windowActivated event.
     *
     * @param window
     *            The IWindow which has to be notified.
     */
    private void callWindowActivated(IWindow window) {
        if (window instanceof IWindowListener) {
            ((IWindowListener) window).windowActivated();
        }
    }
}