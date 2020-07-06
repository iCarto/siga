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

public class ExternalSupport implements WindowListener {

    private final List<JDialog> dialogStack = new ArrayList<JDialog>(0);
    private final IFrameWindowSupport fws;

    public ExternalSupport(IFrameWindowSupport fws) {
        this.fws = fws;
    }

    public void pushDialog(JDialog dlg) {
        dialogStack.add(dlg);
        dlg.addWindowListener(this);
    }

    public JDialog popDialog(JDialog dlg) {
        if (dialogStack.contains(dlg)) {
            dialogStack.remove(dlg);
            return dlg;
        }
        return null;
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

    @Override
    public void windowOpened(WindowEvent e) {
        // Nothing to do here now
    }

    @Override
    public void windowClosing(WindowEvent e) {
        JDialog dlg = (JDialog) e.getSource();
        IWindow iWindow = fws.getWindow(dlg);
        closeWindow(iWindow);
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
        if (iWindow == null) {
            return;
        }
        Component frame = fws.getFrame(iWindow);
        if ((frame == null) || !(frame instanceof JDialog)) {
            return;
        }
        popDialog((JDialog) frame);
        frame.setVisible(false);
        callWindowClosed(iWindow);
        fws.closeWindow(iWindow);
    }
}
