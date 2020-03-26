package es.icarto.gvsig.siga.extractvertexestool;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import org.gvsig.gui.beans.AcceptCancelPanel;

import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.WindowInfo;

import es.icarto.gvsig.commons.gui.AbstractIWindow;

public class ExtractVertexesPaneContainer extends AbstractIWindow {
    private static final long serialVersionUID = 1L;
    private ExtractVertexesGeoprocess controller;
    private final AcceptCancelPanel buttonPanel;

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
        super(new BorderLayout());
        setWindowTitle(PluginServices.getText(this, "extract_vertexes_title"));
        setWindowInfoProperties(WindowInfo.MODALDIALOG);
        this.add(mainPanel, BorderLayout.NORTH);
        buttonPanel = new AcceptCancelPanel(okActionListener, cancelActionListener);
        this.add(buttonPanel, BorderLayout.SOUTH);
    }

    public void setCommand(ExtractVertexesGeoprocess controller) {
        this.controller = controller;
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

    @Override
    protected JButton getDefaultButton() {
        return buttonPanel.getOkButton();
    }

    @Override
    protected Component getDefaultFocusComponent() {
        return null;
    }

}
