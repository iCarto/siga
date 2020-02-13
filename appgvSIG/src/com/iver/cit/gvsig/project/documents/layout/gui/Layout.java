/*
 * Created on 20-feb-2004
 *
 */
/* gvSIG. Sistema de Informaci�n Geogr�fica de la Generalitat Valenciana
 *
 * Copyright (C) 2004 IVER T.I. and Generalitat Valenciana.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307,USA.
 *
 * For more information, contact:
 *
 *  Generalitat Valenciana
 *   Conselleria d'Infraestructures i Transport
 *   Av. Blasco Ib��ez, 50
 *   46010 VALENCIA
 *   SPAIN
 *
 *      +34 963862235
 *   gvsig@gva.es
 *      www.gvsig.gva.es
 *
 *    or
 *
 *   IVER T.I. S.A
 *   Salamanca 50
 *   46005 Valencia
 *   Spain
 *
 *   +34 963163400
 *   dac@iver.es
 */
package com.iver.cit.gvsig.project.documents.layout.gui;

import java.awt.Component;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.geom.AffineTransform;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;

import javax.print.Doc;
import javax.print.DocFlavor;
import javax.print.DocPrintJob;
import javax.print.PrintException;
import javax.print.PrintService;
import javax.print.PrintServiceLookup;
import javax.print.ServiceUI;
import javax.print.SimpleDoc;
import javax.print.attribute.PrintRequestAttributeSet;
import javax.print.event.PrintJobAdapter;
import javax.print.event.PrintJobEvent;
import javax.print.event.PrintJobListener;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.gvsig.gui.beans.swing.JFileChooser;

import com.iver.andami.PluginServices;
import com.iver.andami.messages.NotificationManager;
import com.iver.andami.ui.mdiManager.IWindowListener;
import com.iver.andami.ui.mdiManager.SingletonWindow;
import com.iver.andami.ui.mdiManager.WindowInfo;
import com.iver.cit.gvsig.fmap.ColorEvent;
import com.iver.cit.gvsig.fmap.ExtentEvent;
import com.iver.cit.gvsig.fmap.ProjectionEvent;
import com.iver.cit.gvsig.fmap.ViewPortListener;
import com.iver.cit.gvsig.fmap.edition.commands.CommandListener;
import com.iver.cit.gvsig.fmap.layers.XMLException;
import com.iver.cit.gvsig.project.Project;
import com.iver.cit.gvsig.project.documents.exceptions.OpenException;
import com.iver.cit.gvsig.project.documents.exceptions.SaveException;
import com.iver.cit.gvsig.project.documents.layout.Attributes;
import com.iver.cit.gvsig.project.documents.layout.LayoutContext;
import com.iver.cit.gvsig.project.documents.layout.LayoutControl;
import com.iver.cit.gvsig.project.documents.layout.ProjectMap;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrame;
import com.iver.cit.gvsig.project.documents.layout.fframes.FFrameGroup;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrame;
import com.iver.cit.gvsig.project.documents.layout.fframes.IFFrameViewDependence;
import com.iver.cit.gvsig.project.documents.layout.fframes.gui.dialogs.IFFrameDialog;
import com.iver.cit.gvsig.project.documents.layout.gui.dialogs.FConfigLayoutDialog;
import com.iver.utiles.GenericFileFilter;
import com.iver.utiles.XMLEntity;

/**
 * Graphic representation of the elements to print.
 *
 * @author Vicente Caballero Navarro
 */
public class Layout extends JPanel implements SingletonWindow, ViewPortListener,
        IWindowListener, CommandListener {
	public static final String PDF_AND_PS_FILECHOOSER = "PDF_AND_PS_FILECHOOSER";

	public static File defaultPDFFolderPath;

    private static Boolean defaultShowGrid = null;
    private static Boolean defaultAdjustToGrid = null;
    private static Boolean defaultShowRulers = null;
    private IFFrameDialog fframedialog = null;
    private MapProperties m_propertiesLayout = null;
    private PrintService[] m_cachePrintServices = null;
    private PrintService m_cachePrintService = null;
    private ProjectMap map = null;
    private Doc doc = null;
    private PrintRequestAttributeSet att = null;
    private WindowInfo m_viewInfo = null;
    private static boolean useDefaultJavaPrinting = true;

    /**
     * We use it when we are doing a layout and assigning tags.
     * It is put in debug when we do a VIEW_TAGS
     */
    private boolean bShowIconTag = false;
  	private LayoutControl layoutControl;
	private LayoutContext layoutContext;

    /**
     * Creates a new Layout object.
     */
    public Layout() {
    	layoutContext=new LayoutContext();
        layoutControl=new LayoutControl(this);
        layoutContext.updateFFrames();
        this.initComponents();
    }

    /**
	 * Inserts the ProjectMap of this Layout.
	 *
	 * @param m ProjectMap.
	 */
    public void setProjectMap(ProjectMap m) {
        map = m;
        this.setName(m.getName());
        map.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent evt) {
                if (evt.getPropertyName().equals("name")) {
                    PluginServices.getMDIManager().getWindowInfo(Layout.this)
                            .setTitle(
                                    PluginServices.getText(this, "Mapa")
                                            + " : "
                                            + (String) evt.getNewValue());
                }
            }
        });
    }






    /**
	 * Method to print the Layout without modify the Affinetransform.
	 *
	 * @param g2 Geaphics2D
	 */
    public void drawLayoutPrint(Graphics2D g2) {
        layoutControl.setCancelDrawing(false);

        setCursor(Cursor.getDefaultCursor());

        double scale = 0;
        scale = layoutControl.getRect().height / layoutContext.getAtributes().m_sizePaper.getAlto() * 1;
        AffineTransform escalado = new AffineTransform();
        AffineTransform translacion = new AffineTransform();
        translacion.setToTranslation(layoutControl.getRect().getMinX(), layoutControl.getRect().getMinY());
        escalado.setToScale(scale, scale);
        layoutControl.getAT().setToIdentity();
        layoutControl.getAT().concatenate(translacion);
        layoutControl.getAT().concatenate(escalado);
        layoutContext.getAtributes().setDistanceUnitX(layoutControl.getRect());
        layoutContext.getAtributes().setDistanceUnitY(layoutControl.getRect());
        IFFrame[] fframes=layoutContext.getFFrames();
        for (int i = 0; i < fframes.length; i++) {
//            	fframes[i].setPrintingProperties(this.att);
       	    fframes[i].print(g2, layoutControl.getAT(),null, layoutContext.getAtributes().getAttributes());
//        	    fframes[i].setPrintingProperties(null);
        }

        // TODO Esto es para ver el rect�ngulo que representa el folio en la
        // impresi�n.
        // g2.drawRect(0, 0, (int) rect.getWidth(), (int) rect.getHeight());
    }



    /**
	 * It initializes the components.
	 */
    private void initComponents() {
    	this.setLayout(new GridLayout(1,1));
        add(layoutControl);
    	layoutContext.getAtributes().setDistanceUnitX(layoutControl.getRect());
    	layoutContext.getAtributes().setDistanceUnitY(layoutControl.getRect());
        setDoubleBuffered(true);
    }

    /**
	 * Open the dialog of Layout properties.
	 *
	 * @param job PrinterJob
	 */
    public void showPagePropertiesWindow(PrinterJob job) {
        PageFormat pf1;

        pf1 = layoutContext.getAtributes().getPageFormat();
        pf1 = job.pageDialog(pf1);
        layoutContext.getAtributes().setPageFormat(pf1);
        layoutControl.refresh();
    }

    /**
	 * It obtains the rect�ngulo that represents the sheet with the characteristics
	 * that contains attributes and differentiating if is to visualize in screen or
	 * for print.
	 *
	 */
    public void obtainRect(boolean isPrint) {
    	layoutContext.getAtributes().obtainRect(isPrint,layoutControl.getRect(), getWidth(), getHeight());
//    	layoutContext.getAtributes().obtainRect(layoutControl.getRect());
    }

    /**
	 * It shows the dialog of configuration of the Layout.
	 */
    public void showFConfig() {
        FConfigLayoutDialog m_configLayout = new FConfigLayoutDialog(this);
        PluginServices.getMDIManager().addWindow(m_configLayout);
    }

    /**
	 * It shows the dialog of Layout�s properties.
	 */
    public boolean showFProperties() {
        if (map == null) {
            map = new ProjectMap();
            map.setModel(this);
            map.setName(getName());
        }

        m_propertiesLayout = new MapProperties(map);
        PluginServices.getMDIManager().addWindow(m_propertiesLayout);
        return m_propertiesLayout.isAccepted();
    }

    /**
	 * It shows the dialog of printing of the Layout.
	 *
	 * @param job PrinterJob
	 */
    public void showPrintDialog(PrinterJob job) {
        if (job != null) {
            job.printDialog();

            try {
                job.setPrintable((Printable) PluginServices
                        .getExtension(com.iver.cit.gvsig.Print.class));
                job.print();
            } catch (PrinterException e) {
                e.printStackTrace();
            }
        } else {
            // Actualizar attributes
            att = layoutContext.getAtributes().toPrintAttributes();

            // ------------------ The Printing things --------------------- //
            DocFlavor flavor = DocFlavor.SERVICE_FORMATTED.PRINTABLE;

            // returns the set of printers that support printing a specific
            // document type (such as GIF)
            // with a specific set of attributes (such as two sided).
            // PrintRequestAttributeSet pras = new
            // HashPrintRequestAttributeSet();
            // interestingly, the printer dialog's default behavior has changed
            // with the new API: by default the dialog is not shown.
            // So we must use the ServiceUI class to create a print dialog
            // returns the default print service.
            if (m_cachePrintServices == null || m_cachePrintServices.length == 0) {
                m_cachePrintServices = PrintServiceLookup.lookupPrintServices(
                        flavor, null);
            }

            PrintService defaultService = null;

            if (m_cachePrintService == null) {
                defaultService = PrintServiceLookup.lookupDefaultPrintService();
            }
            
            if(m_cachePrintService == null && defaultService == null && m_cachePrintServices.length > 0)
            	defaultService = m_cachePrintServices[0];

            if ((defaultService == null) && (m_cachePrintService == null)) {
                JOptionPane.showMessageDialog((Component) PluginServices
                        .getMainFrame(),PluginServices.getText(this,"ninguna_impresora_configurada"));

                return;
            }

            if (useDefaultJavaPrinting) {
            	try {
            		m_cachePrintService = ServiceUI.printDialog(null, 200, 200,
            				m_cachePrintServices, defaultService, flavor, att);
            	}
            	catch (RuntimeException ex) {
            		useDefaultJavaPrinting=false;
            		Logger logger = PluginServices.getLogger();
            		logger.error("Error showing print dialog", ex); 
            		
            		// workaround a problem with Java 1.5 with moder CUPS versions
            		// this try-catch block may be safely removed when we move to Java 1.6
            		logger.debug("Opening gvSIG's internal Java 1.7 CUPS printing dialog");
            		m_cachePrintService = backport1_7.javax.print.ServiceUI.printDialog(null, 200, 200,
            				m_cachePrintServices, defaultService, flavor, att);
            	}
            }
            else {
            	PluginServices.getLogger().debug("Opening gvSIG's internal Java 1.7 CUPS printing dialog");
            	m_cachePrintService = backport1_7.javax.print.ServiceUI.printDialog(null, 200, 200,
            			m_cachePrintServices, defaultService, flavor, att);
            }

            if (m_cachePrintService != null) {
                DocPrintJob jobNuevo = m_cachePrintService.createPrintJob();
                PrintJobListener pjlistener = new PrintJobAdapter() {
                    public void printDataTransferCompleted(PrintJobEvent e) {
                        layoutControl.fullRect();
                    }
                };

                jobNuevo.addPrintJobListener(pjlistener);

                doc = new SimpleDoc(PluginServices
                        .getExtension(com.iver.cit.gvsig.Print.class), flavor,
                        null);

                try {
                    jobNuevo.print(doc, att);

                    // m_attributes.
                } catch (PrintException pe) {
                    NotificationManager.addError(pe);
                }
            }
        }
    }

    /**
	 * The dialogs are created here each time that are needed.
	 *
	 * @param fframe
	 *            Rectangle that represents the place that occupied the element added.
	 *
	 * @return IFFrame Returns the FFrame added or null if the fframe has not been added.
	 */
    public IFFrame openFFrameDialog(IFFrame fframe) {
        fframedialog=fframe.getPropertyDialog();
        if (fframedialog != null) {
            fframedialog.setRectangle(fframe.getBoundingBox(layoutControl.getAT()));
            PluginServices.getMDIManager().addWindow(fframedialog);
        }

        return fframedialog.getFFrame();
    }

    /**
     * This method is used to get <strong>an initial</strong> ViewInfo object
     * for this Map. It is not intended to retrieve the ViewInfo object in a
     * later time. <strong>Use PluginServices.getMDIManager().getViewInfo(view)
     * to retrieve the ViewInfo object at any time after the creation of the
     * object.
     *
     * @see com.iver.mdiApp.ui.MDIManager.IWindow#getWindowInfo()
     */
    public WindowInfo getWindowInfo() {
        if (m_viewInfo == null) {
            m_viewInfo = new WindowInfo(WindowInfo.ICONIFIABLE | WindowInfo.RESIZABLE
                    | WindowInfo.MAXIMIZABLE);
            m_viewInfo.setWidth(500);
            m_viewInfo.setHeight(400);

            m_viewInfo.setTitle(PluginServices.getText(this, "Mapa") + " : "
                    + map.getName());
        }
        return m_viewInfo;
    }

    /**
	 * It returns an Object XMLEntity with the information the necessary attributes
	 * to be able later to create again the original object.
	 *
	 * @return XMLEntity.
	 *
	 * @throws XMLException
	 */
    public XMLEntity getXMLEntity() {
        XMLEntity xml = new XMLEntity();
        xml.putProperty("className", this.getClass().getName());
        xml.setName("layout");
        xml.putProperty("isCuadricula", layoutContext.isAdjustingToGrid());
//        xml.putProperty("m_name", this.getName());
        xml.putProperty("isEditable", layoutContext.isEditable());
        xml.putProperty("numBehind", layoutContext.numBehind);
        xml.putProperty("numBefore", layoutContext.numBefore);
        xml.addChild(layoutContext.getAtributes().getXMLEntity());
        IFFrame[] fframes=layoutContext.getFFrames();
        for (int i = 0; i < fframes.length; i++) {
            try {
                XMLEntity xmlAux = fframes[i].getXMLEntity();
                xml.addChild(xmlAux);
            } catch (SaveException e) {
                e.showError();
            }
        }
        return xml;
    }
    /**
     * Returns the LayoutContext.
     * @return LayoutContext.
     */
    public LayoutContext getLayoutContext() {
    	return layoutContext;
    }
    /**
     * Returns LayoutControl.
     * @return LayoutControl.
     */
    public LayoutControl getLayoutControl() {
    	return layoutControl;
    }
    /**
	 * It creates an Object of this class from the information of the XMLEntity.
	 *
	 * @param xml
	 *            XMLEntity
	 * @param p
	 *            Project.
	 *
	 * @return Object of this class.
	 * @throws OpenException
	 */
    public static Layout createLayout(XMLEntity xml, Project p)
            throws OpenException {
        Layout layout = new Layout();
        try {
            layout.layoutContext.setAdjustToGrid(xml.getBooleanProperty("isCuadricula"));
            //layout.setName(xml.getStringProperty("m_name"));
            layout.getLayoutContext().setAtributes(Attributes.createAtributes(xml.getChild(0)));
            if (xml.contains("isEditable")) {
                layout.layoutContext.setEditable(xml.getBooleanProperty("isEditable"));
            }
            if (xml.contains("numBehind")) {
                layout.layoutContext.numBehind = xml.getIntProperty("numBehind");
                layout.layoutContext.numBefore = xml.getIntProperty("numBefore");
            }
            //layout.layoutContext.getEFS().startComplexCommand();
//            for (int i = 1; i < xml.getChildrenCount(); i++) {
//                try {
//                    layout.layoutContext.addFFrame(FFrame.createFFrame(xml.getChild(i), p,
//                            layout), true, false);
//                } catch (OpenException e) {
//                    e.showError();
//                }
//            }

            for (int i = 1; i < xml.getChildrenCount(); i++) {
				try {
					IFFrame frame = FFrame.createFromXML(xml
							.getChild(i),p, layout);
					layout.layoutContext.addFFrame(frame,true,frame.getSelected()==IFFrame.RECT);
				} catch (OpenException e) {
					e.showError();
				}
			}

            //layout.layoutContext.getEFS().endComplexCommand(PluginServices.getText(layout,"Inicializando"));
            IFFrame[] fframes = layout.getLayoutContext().getAllFFrames();
            for (int i = 0; i < fframes.length; i++) {
                fframes[i].setLayout(layout);
                if (fframes[i] instanceof IFFrameViewDependence) {
                    try {
                	((IFFrameViewDependence) fframes[i])
                            .initDependence(fframes);
                    }catch (Exception e) {
						System.out.println("Fallo FFrameGroup");
					}
                    }
            }
            IFFrame[] fs = layout.getLayoutContext().getFFrames();
			for (int i = 0; i < fs.length; i++) {
				if (fs[i] instanceof FFrameGroup) {
					((IFFrameViewDependence) fs[i]).initDependence(fframes);
				}
			}
        } catch (Exception e) {
            throw new OpenException(e, layout.getClass().getName());
        }
        return layout;
    }

    /**
     * @see com.iver.mdiApp.ui.MDIManager.IWindow#windowActivated()
     */
    public void windowActivated() {
        //fullRect();
        layoutControl.refresh();
        PluginServices.getMainFrame().getStatusBar().setMessage("units",
                PluginServices.getText(this, layoutContext.getAtributes().getNameUnit()));
            // ensure requestFocus is enabled
        if(!layoutControl.isRequestFocusEnabled()) {
        	layoutControl.setRequestFocusEnabled(true);
       	}
        requestFocus();
        layoutControl.requestFocus();
    }

    /**
     * @see com.iver.mdiApp.ui.MDIManager.SingletonWindow#getWindowModel()
     */
    public Object getWindowModel() {
        return map;
    }
    /**
     * Returns ProjectMap, the model of Layout.
     * @return ProjectMap.
     */
    public ProjectMap getModel() {
    	return map;
    }
    /**
     * @see com.iver.cit.gvsig.fmap.ExtentListener#extentChanged(com.iver.cit.gvsig.fmap.ExtentEvent)
     */
    public void extentChanged(ExtentEvent e) {
    }

    /**
     * @see com.iver.andami.ui.mdiManager.IWindowListener#windowClosed()
     */
    public void windowClosed() {
        // /PluginServices.getMainFrame().getStatusBar().setMessage("1","");
    }

    /**
     * @see com.iver.cit.gvsig.fmap.ViewPortListener#backColorChanged(com.iver.cit.gvsig.fmap.ColorEvent)
     */
    public void backColorChanged(ColorEvent e) {
        // refresh();
    }

    /**
     * Opens a dialog where to pick a PDF-file to save the current Layout
     * suggesting a name for the file given by the first argument
     *
     * @param suggestedName
     */
    public void layoutToPDF(String suggestedName) {
        FileFilter pdfFilter = new GenericFileFilter("pdf", PluginServices
                .getText(this, "pdf"));

        JFileChooser jfc = new JFileChooser(PDF_AND_PS_FILECHOOSER, defaultPDFFolderPath);
        if (suggestedName != null)
            jfc.setSelectedFile(new File(suggestedName));
        jfc.addChoosableFileFilter(pdfFilter);
        jfc.setFileFilter(pdfFilter);

        if (jfc.showSaveDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            File faux = null;

            if (f.getName().endsWith(".pdf") || f.getName().endsWith(".PDF")) {
                faux = f;
            } else {
                faux = new File(f.getPath() + ".pdf");
            }

            layoutControl.getLayoutDraw().toPDF(faux);
        }
    }
    /**
     * Opens a dialog where to pick a PS-file to save the current Layout
     * suggesting a name for the file given by the first argument
     *
     * @param suggestedName
     */
    public void layoutToPS(String suggestedName) {
        FileFilter pdfFilter = new GenericFileFilter("ps", PluginServices
                .getText(this, "ps"));

        JFileChooser jfc = new JFileChooser(PDF_AND_PS_FILECHOOSER, defaultPDFFolderPath);
        if (suggestedName != null)
            jfc.setSelectedFile(new File(suggestedName));
        jfc.addChoosableFileFilter(pdfFilter);
        jfc.setFileFilter(pdfFilter);

        if (jfc.showSaveDialog((Component) PluginServices.getMainFrame()) == JFileChooser.APPROVE_OPTION) {
            File f = jfc.getSelectedFile();
            File faux = null;

            if (f.getName().endsWith(".ps") || f.getName().endsWith(".PS")) {
                faux = f;
            } else {
                faux = new File(f.getPath() + ".ps");
            }

            layoutControl.getLayoutDraw().toPS(faux);
        }
    }
    /**
	 * It opens a dialog to select pdf file where to save the Layout in this format.
	 */
    public void layoutToPDF() {
        layoutToPDF(null);
    }
    /**
	 * It opens a dialog to select ps file where to save the Layout in this format.
	 */
    public void layoutToPS() {
        layoutToPS(null);
    }
    /**
	 * @return Returns the bShowIconTag.
	 */
    public boolean isShowIconTag() {
        return bShowIconTag;
    }

    /**
     * @param modeDebug
     *            The bModeDebug to set.
     */
    public void setShowIconTag(boolean modeDebug) {
        bShowIconTag = modeDebug;
    }

    /**
	 * Repaint the Layout.
	 */
    public void commandRepaint() {
        this.layoutContext.updateFFrames();
        this.layoutControl.refresh();
    }
    /**
	 * Repaint the Layout.
	 */
    public void commandRefresh() {
        commandRepaint();

    }
    /**
	 * Event to change the projection.
	 */
    public void projectionChanged(ProjectionEvent e) {
        // TODO Auto-generated method stub

    }
    /**
	 * Returns if the grid sould be show.
	 * @return True if the grid sould be show.
	 */
    public static boolean getDefaultShowGrid() {
        if (defaultShowGrid == null) {
            XMLEntity xml = PluginServices.getPluginServices("com.iver.cit.gvsig").getPersistentXML();
            if (xml.contains("DefaultShowLayoutGrid")) {
                defaultShowGrid = new Boolean(xml.getBooleanProperty("DefaultShowLayoutGrid"));
            }
            else {
                // factory default is true
                defaultShowGrid = new Boolean(true);
            }
        }
        return defaultShowGrid.booleanValue();
    }
    /**
	 * Returns if the adjust to grid sould be actived.
	 * @return True if the adjust to grid sould be actived.
	 */
    public static boolean getDefaultAdjustToGrid() {
        if (defaultAdjustToGrid == null) {
            XMLEntity xml = PluginServices.getPluginServices("com.iver.cit.gvsig").getPersistentXML();
            if (xml.contains("DefaultEnableLayoutGrid")) {
                defaultAdjustToGrid = new Boolean(xml.getBooleanProperty("DefaultEnableLayoutGrid"));
            }
            else {
                // factory default is false
                defaultAdjustToGrid = new Boolean(false);
            }
        }
        return defaultAdjustToGrid.booleanValue();
    }
    /**
	 * Returns if the ruler sould be show.
	 * @return True if the ruler sould be show.
	 */
    public static boolean getDefaultShowRulers() {
        if (defaultShowRulers == null){
            XMLEntity xml = PluginServices.getPluginServices("com.iver.cit.gvsig").getPersistentXML();
            if (xml.contains("DefaultShowLayoutRules")) {
                defaultShowRulers = new Boolean(xml.getBooleanProperty("DefaultShowLayoutRules"));
            }
            else {
                // factory default is true
                defaultShowRulers = new Boolean(true);
            }
        }
        return defaultShowRulers.booleanValue();
    }
    /**
	 * Inserts if the grid sould be show.
	 * @param showGrid
	 */
    public static void setDefaultShowGrid(boolean showGrid) {
        defaultShowGrid = new Boolean(showGrid);
    }
    /**
	 * Inserts if the adjust togrid sould be actived.
	 * @param gridEnable
	 */
    public static void setDefaultAdjustToGrid(boolean gridEnabled) {
        defaultAdjustToGrid = new Boolean(gridEnabled);
    }
    /**
	 * Inserts if the ruler sould be show.
	 * @param showRuler
	 */
    public static void setDefaultShowRulers(boolean showRules) {
        defaultShowRulers  = new Boolean(showRules);
    }

	public Object getWindowProfile() {
		return WindowInfo.EDITOR_PROFILE;
	}
}
