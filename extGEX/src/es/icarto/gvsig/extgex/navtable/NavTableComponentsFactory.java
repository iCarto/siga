package es.icarto.gvsig.extgex.navtable;

import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;

import com.iver.andami.PluginServices;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;

import es.icarto.gvsig.extgex.navtable.decorators.fileslink.FilesLinkData;
import es.icarto.gvsig.extgex.navtable.decorators.fileslink.FilesLinkObserver;
import es.icarto.gvsig.extgex.navtable.decorators.printreports.PrintReportsObserver;
import es.udc.cartolab.gvsig.navtable.AbstractNavTable;

public class NavTableComponentsFactory {

    public JButton createButton(String toolTipText, ImageIcon icon,
	    ActionListener listener) {
	JButton but = new JButton(icon);
	but.setToolTipText(toolTipText);
	but.addActionListener(listener);
	return but;
    }

    public JButton getFilesLinkButton(FLyrVect layer, AbstractNavTable dialog) {
	NavTableComponentsFactory ntFactory = new NavTableComponentsFactory();
	FilesLinkData filesLinkData = new FilesLinkData(layer);
	JButton filesLinkB = ntFactory.createButton(PluginServices.getText(
		this, "filesLinkTooltip"), getIcon("/fileslink.png"),
		new FilesLinkObserver(dialog, filesLinkData));
	return filesLinkB;
    }

    public JButton getPrintButton(FLyrVect layer, AbstractNavTable dialog, String reportName) {
	NavTableComponentsFactory ntFactory = new NavTableComponentsFactory();
	JButton printReportB = ntFactory.createButton(PluginServices.getText(
		this, "printReportsToolTip"), getIcon("/print-report.png"),
		new PrintReportsObserver(layer, dialog, reportName));
	return printReportB;
    }

    public ImageIcon getIcon(String iconName) {
	java.net.URL imgURL = getClass().getResource(iconName);
	if (imgURL == null) {
	    imgURL = AbstractNavTable.class.getResource(iconName);
	}

	ImageIcon icon = new ImageIcon(imgURL);
	return icon;
    }

}
