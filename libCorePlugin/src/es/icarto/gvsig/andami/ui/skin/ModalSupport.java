package es.icarto.gvsig.andami.ui.skin;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JDialog;

import com.iver.andami.ui.mdiManager.IFrameWindowSupport;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.IWindowListener;

/**
 * Support for operating with Modal Windows handled by Andami.
 *
 * Commonly modal windows are implemented with JDialogs.
 *
 * This class tracks the opened modal windows, and make the needed operation of clean up when closed
 *
 */
public class ModalSupport implements WindowListener {

    private final List<JDialog> dialogStack = new ArrayList<JDialog>(0);
    private final IFrameWindowSupport fws;

    public ModalSupport(IFrameWindowSupport fws) {
        this.fws = fws;
    }

    public void pushDialog(JDialog dlg) {
        dialogStack.add(dlg);
        dlg.addWindowListener(this);
    }

    // Este método está por compatibilidad con NewSkin
    public JDialog popDialog() {
        int dialogStackSize = dialogStack.size();
        if (dialogStackSize < 1) {
            return null;
        }
        return dialogStack.remove(dialogStackSize - 1);
    }

    // Este metodo está hecho porqué necesitamos identificar correctamente el diálogo que vamos a cerrar. Cuando
    // trabajamos con frames puede suceder que no el evento de modalWindowClosing de la ventana a cerrar a no se
    // corresponda con el del único modal añadido
    // Ver #903. Sucede con VariablesTemplatePanel
    private JDialog popDialog(IWindow iWindow) {
        Component dlg = fws.getFrame(iWindow);
        boolean removed = dialogStack.remove(dlg);
        if (!removed) {
            return null;
        }
        return (JDialog) dlg;
    }

    public void setWaitCursor() {
        for (JDialog dlg : dialogStack) {
            dlg.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            dlg.getGlassPane().setVisible(true);
        }
    }

    public void restoreCursor() {
        // TODO. Should we save the dialog cursors before set it to wait and the restore it?
        // Does it make sense at all
        for (JDialog dlg : dialogStack) {
            dlg.setCursor(null);
            dlg.getGlassPane().setVisible(false);
        }
    }

    public void modalWindowClosing(IWindow iWindow) {
        JDialog dlg = this.popDialog(iWindow);
        if (dlg == null) {
            return;
        }
        dlg.setVisible(false);

        callWindowClosed(iWindow);

        fws.closeWindow(iWindow);
    }

    @Override
    public void windowOpened(WindowEvent e) {
        // Nothing to do here now
    }

    @Override
    public void windowClosing(WindowEvent e) {
        IWindow iWindow = fws.getWindow((Component) e.getSource());
        modalWindowClosing(iWindow);
    }

    @Override
    public void windowClosed(WindowEvent e) {
        // Nothing to do here now
    }

    @Override
    public void windowIconified(WindowEvent e) {
        // Nothing to do here now
    }

    @Override
    public void windowDeiconified(WindowEvent e) {
        // Nothing to do here now
    }

    @Override
    public void windowActivated(WindowEvent e) {
        IWindow iWindow = fws.getWindow((Component) e.getSource());
        callWindowActivated(iWindow);
    }

    @Override
    public void windowDeactivated(WindowEvent e) {
        // Nothing to do here now
    }

    /**
     * If <code>window</code> implements IWindowListener, sent it the
     * windowActivated event.
     *
     * @param iWindow
     *            The IWindow which has to be notified.
     */
    private void callWindowActivated(IWindow iWindow) {
        if (iWindow instanceof IWindowListener) {
            ((IWindowListener) iWindow).windowActivated();
        }
    }

    /**
     * If <code>window</code> implements IWindowListener, sent it the
     * windowActivated event.
     *
     * @param iWindow
     *            The IWindow which has to be notified.
     */
    private void callWindowClosed(IWindow iWindow) {
        if (iWindow instanceof IWindowListener) {
            ((IWindowListener) iWindow).windowClosed();
        }
    }

    public void closeWindow(IWindow iWindow) {
        if (!iWindow.getWindowInfo().isModal()) {
            return;
        }

        modalWindowClosing(iWindow);
    }

}
