package es.icarto.gvsig.extgia.forms.centros_mando;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractButton;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.hardcode.gdbms.driver.exceptions.ReadDriverException;
import com.hardcode.gdbms.engine.values.Value;
import com.iver.andami.PluginServices;
import com.iver.andami.ui.mdiManager.IWindow;
import com.iver.cit.gvsig.fmap.MapContext;
import com.iver.cit.gvsig.fmap.layers.FBitSet;
import com.iver.cit.gvsig.fmap.layers.FLyrVect;
import com.iver.cit.gvsig.project.documents.view.gui.BaseView;

import es.icarto.gvsig.commons.gui.TOCLayerManager;
import es.icarto.gvsig.extgia.forms.AbstractFormWithLocationWidgets;
import es.icarto.gvsig.extgia.forms.farolas.FarolasForm;
import es.icarto.gvsig.extgia.preferences.Elements;
import es.udc.cartolab.gvsig.navtable.NavTable;

@SuppressWarnings("serial")
public class CentrosMandoForm extends AbstractFormWithLocationWidgets {

    public static final String TABLENAME = "centros_mando";

    public CentrosMandoForm(FLyrVect layer) {
        super(layer);
        viewAssociatedFarolasHandler();
    }

    private void viewAssociatedFarolasHandler() {
    JButton viewFarolasButton = (JButton) formBody.getComponentByName("ver_farolas");
    viewFarolasButton.addActionListener(new ActionListener() {

    @Override
    public void actionPerformed(ActionEvent e) {
    try {
        FLyrVect farolasLayer = null;
        IWindow iWindow = PluginServices.getMDIManager().getActiveWindow();
        
        if(iWindow instanceof BaseView){
        TOCLayerManager toc = new TOCLayerManager(((BaseView) iWindow).getMapControl());
        farolasLayer = toc.getVectorialLayerByName(FarolasForm.TABLENAME);
        }    

        Value[] centroMandoRowValues = layer.getSource().getRecordset().getRow(getPosition());
        String centroMandoID = centroMandoRowValues[0].toString();
        FBitSet farolasBitset = getRecordset().getSelection();
        farolasBitset.clear();
        for (int i=0; i<farolasLayer.getRecordset().getRowCount(); i++) {
            Value[] farolasRowValues = farolasLayer.getRecordset().getRow(i);
            String farolaCentroMando = farolasRowValues[1].toString();
            if (centroMandoID.compareToIgnoreCase(farolaCentroMando) == 0) {  
                farolasBitset.set(i);
            }
            
        }
        farolasLayer.getRecordset().setSelection(farolasBitset);
        
        MapContext mapa = farolasLayer.getMapContext();
        farolasLayer.setActive(true);
        layer.setActive(false);
        Rectangle2D rectangle = mapa.getSelectionBounds();
        if (rectangle.getWidth() < 200) {
            rectangle.setFrameFromCenter(rectangle.getCenterX(),
                rectangle.getCenterY(),
                rectangle.getCenterX() + 100,
                rectangle.getCenterY() + 100);
            }
            if (rectangle != null) {
            mapa.getViewPort()
                .setExtent(rectangle);
            }
            farolasLayer.setActive(false);
            layer.setActive(true);
    } catch (ReadDriverException e1) {
        e1.printStackTrace();
    }
    }
    });
    
    }

    @Override
    public Elements getElement() {
        return Elements.Centros_Mando;
    }

    @Override
    public String getElementID() {
        return "gid";
    }

    @Override
    public String getElementIDValue() {
        JTextField idWidget = (JTextField) getWidgets().get(getElementID());
        return idWidget.getText();
    }

    @Override
    public JTable getReconocimientosJTable() {
        return null;
    }

    @Override
    public JTable getTrabajosJTable() {
        return null;
    }

    @Override
    public String getBasicName() {
        return TABLENAME;
    }

    @Override
    protected boolean hasSentido() {
        return true;
    }

}