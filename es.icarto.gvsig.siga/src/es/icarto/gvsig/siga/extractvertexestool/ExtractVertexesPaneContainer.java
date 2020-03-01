package es.icarto.gvsig.siga.extractvertexestool;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import org.gvsig.gui.beans.AcceptCancelPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;

public class ExtractVertexesPaneContainer extends JPanel implements IWindow {
    private static final long serialVersionUID = 1L;
    private AcceptCancelPanel buttonPanel = null;
    private JPanel mainPanel = new JPanel();
    private WindowInfo viewInfo = null;
    private ExtractVertexesGeoprocess controller;

    private final ActionListener okActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            if (controller.launchGeoprocess()) {
                cancel();
            }
        }
    };

    private final ActionListener cancelActionListener = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent arg0) {
            cancel();
        }
    };

    public ExtractVertexesPaneContainer(JPanel mainPanel) {
        super();
        this.mainPanel = mainPanel;
        initialize();
    }

    @Override
    public WindowInfo getWindowInfo() {
        if (viewInfo == null) {
            viewInfo = new WindowInfo(WindowInfo.MODALDIALOG);
            viewInfo.setTitle(PluginServices.getText(this, "extract_vertexes_title"));
            viewInfo.setWidth(435);
            viewInfo.setHeight(135);
        }
        return viewInfo;
    }

    @Override
    public Object getWindowProfile() {
        return WindowInfo.DIALOG_PROFILE;
    }

    public void setCommand(ExtractVertexesGeoprocess controller) {
        this.controller = controller;
    }

    private void initialize() {
        this.setLayout(new BorderLayout());
        // this.setSize(new java.awt.Dimension(570, 460));
        this.add(getMainPanel(), java.awt.BorderLayout.NORTH);
        this.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);

        this.validate();
    }

    private JPanel getMainPanel() {
        return mainPanel;
    }

    private AcceptCancelPanel getButtonPanel() {
        if (buttonPanel == null) {
            buttonPanel = new AcceptCancelPanel();
            buttonPanel.setOkButtonActionListener(okActionListener);
            buttonPanel.setCancelButtonActionListener(cancelActionListener);
        }
        return buttonPanel;
    }

    public void cancel() {
        if (PluginServices.getMainFrame() == null) {
            Container container = getParent();
            Container parentOfContainer = null;
            while (!(container instanceof Window)) {
                parentOfContainer = container.getParent();
                container = parentOfContainer;
            }
            ((Window) container).dispose();
        } else {
            PluginServices.getMDIManager().closeWindow(ExtractVertexesPaneContainer.this);
        }
    }

}
