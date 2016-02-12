package es.icarto.gvsig.extgia.forms.senhalizacion_vertical;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JComboBox;

import com.jeta.forms.components.image.ImageComponent;

import es.icarto.gvsig.navtableforms.IValidatableForm;
import es.icarto.gvsig.navtableforms.gui.images.ImageHandler;

public class SenhalesImageHandler implements ImageHandler, KeyListener,
ActionListener {

    private final String imgComp;
    private final IValidatableForm form;
    private final ImageComponent image;

    private final JComboBox tipo;
    private final JComboBox codigo;
    private final SenhalesAlgorithm alg;

    public SenhalesImageHandler(String imgComp, String tipoName,
	    String codigoName, String folderPath, IValidatableForm form) {
	this.imgComp = imgComp;
	this.alg = new SenhalesAlgorithm(folderPath, new Dimension(115, 115));
	this.form = form;
	this.image = (ImageComponent) form.getFormPanel().getComponentByName(
		imgComp);

	tipo = (JComboBox) form.getWidgets().get(tipoName);
	codigo = (JComboBox) form.getWidgets().get(codigoName);

    }

    @Override
    public void setListeners() {
	tipo.addActionListener(this);
	codigo.addActionListener(this);
    }

    @Override
    /**
     * The name of the components this handler is associated on, and commonly the name of the handler itself used in maps
     */
    public String getName() {
	return imgComp;
    }

    @Override
    public void removeListeners() {
	tipo.removeActionListener(this);
	codigo.removeActionListener(this);
    }

    private String getComboValue(JComboBox combo) {
	Object tmpValue = combo.getSelectedItem();
	return (tmpValue != null) ? tmpValue.toString().trim() : "";
    }

    @Override
    public void fillValues() {
	String tipoValue = getComboValue(tipo);
	String codigoValue = getComboValue(codigo);
	image.setIcon(alg.getIcon(tipoValue, codigoValue));
	image.repaint();
    }

    @Override
    public void fillEmptyValues() {
	fillValues();
    }

    private void delegate() {
	if (!form.isFillingValues()) {
	    fillValues();
	}
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	delegate();
    }

    @Override
    public void keyTyped(KeyEvent e) {
    }

    @Override
    public void keyPressed(KeyEvent e) {
    }

    @Override
    public void keyReleased(KeyEvent e) {
	delegate();
    }

}
