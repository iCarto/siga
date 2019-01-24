package org.gvsig.mapsheets.print
.audasa;

import java.awt.Component;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;

public class TemplatePanel {

    public static final String CONTINUAR_BUTTON = "Continuar";

    private static final int WIDTH_TITLE = 160;
    private static final int HEIGHT_TXT = 21;
    private static final int WIDTH_TXT = 300;
    private static final int HEIGHT_TITLE = 21;

    private static final int OFFSET_TOP = 8;
    private static final int MARGIN_TOP = 15 + OFFSET_TOP;
    private static final int MARGIN_LEFT_TITLE = 21;
    private static final int MARGIN_LEFT_TXT = MARGIN_LEFT_TITLE + WIDTH_TITLE;

    private static ArrayList<Component> componentsList;

    public static ArrayList<Component> create(String template) {
	if(template.equals(AudasaPreferences.A3_DIMENSIONES)){
	    return getComponentsForTemplateDimensiones();
	} else if (template.equals(AudasaPreferences.A4_POLICIA_MARGENES)) {
	    return getComponentsForTemplatePM();
	} else if (template.equals(AudasaPreferences.A4_CONSULTAS_VERTICAL)) {
	    return getComponentsForTemplateA4VerticalConsultas();
	} else {
	    return getComponentsForTemplateConsultas();
	}
    }

    private static ArrayList<Component> getComponentsForTemplateConsultas() {
	componentsList = new ArrayList<Component>();

	JLabel ingenieroDirectorTitle = new JLabel("Ingeniero director:");
	ingenieroDirectorTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*1, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField ingenieroDirectorText = new JTextField();
	ingenieroDirectorText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*1, WIDTH_TXT, HEIGHT_TXT));
	ingenieroDirectorText.setName(AudasaPreferences.INGENIERO_DIRECTOR);
	componentsList.add(ingenieroDirectorTitle);
	componentsList.add(ingenieroDirectorText);

	JLabel provinciaTitle = new JLabel("Provincia:");
	provinciaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*2, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField provinciaText = new JTextField();
	provinciaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*2, WIDTH_TXT, HEIGHT_TXT));
	provinciaText.setName(AudasaPreferences.PROVINCIA);
	componentsList.add(provinciaTitle);
	componentsList.add(provinciaText);

	JLabel municipioTitle = new JLabel("Municipio:");
	municipioTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*3, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField municipioText = new JTextField();
	municipioText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*3, WIDTH_TXT, HEIGHT_TXT));
	municipioText.setName(AudasaPreferences.MUNICIPIO);
	componentsList.add(municipioTitle);
	componentsList.add(municipioText);

	JLabel proyectoTitle = new JLabel("Proyecto:");
	proyectoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*4, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField proyectoText = new JTextField();
	proyectoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*4, WIDTH_TXT, HEIGHT_TXT));
	proyectoText.setName(AudasaPreferences.PROYECTO);
	componentsList.add(proyectoTitle);
	componentsList.add(proyectoText);

	JLabel dibujoTitle = new JLabel("Dibujo:");
	dibujoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*5, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField dibujoText = new JTextField();
	dibujoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*5, WIDTH_TXT, HEIGHT_TXT));
	dibujoText.setName(AudasaPreferences.DIBUJO);
	componentsList.add(dibujoTitle);
	componentsList.add(dibujoText);

	JLabel consultaTitle = new JLabel("Ref. Consulta:");
	consultaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*6, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField consultaText = new JTextField();
	consultaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*6, WIDTH_TXT, HEIGHT_TXT));
	consultaText.setName(AudasaPreferences.CONSULTA);
	componentsList.add(consultaTitle);
	componentsList.add(consultaText);

	JLabel fechaTitle = new JLabel("Fecha:");
	fechaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*7, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField fechaText = new JTextField();
	fechaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*7, WIDTH_TXT, HEIGHT_TXT));
	fechaText.setName(AudasaPreferences.FECHA);
	componentsList.add(fechaTitle);
	componentsList.add(fechaText);

	JLabel tituloEstudioTitle = new JLabel("Título del estudio:");
	tituloEstudioTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*8, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField tituloEstudioText = new JTextField();
	tituloEstudioText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*8, WIDTH_TXT, HEIGHT_TXT));
	tituloEstudioText.setName(AudasaPreferences.TITULO_ESTUDIO);
	componentsList.add(tituloEstudioTitle);
	componentsList.add(tituloEstudioText);

	JLabel tituloPlanoTitle = new JLabel("Título del plano:");
	tituloPlanoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*9, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField tituloPlanoText = new JTextField();
	tituloPlanoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*9, WIDTH_TXT, HEIGHT_TXT));
	tituloPlanoText.setName(AudasaPreferences.TITULO_PLANO);
	componentsList.add(tituloPlanoTitle);
	componentsList.add(tituloPlanoText);

	JLabel tramoTitle = new JLabel("Tramo:");
	tramoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*10, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField tramoText = new JTextField();
	tramoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*10, WIDTH_TXT, HEIGHT_TXT));
	tramoText.setName(AudasaPreferences.TRAMO);
	componentsList.add(tramoTitle);
	componentsList.add(tramoText);

	JLabel numeroPlanoTitle = new JLabel("Nº del plano:");
	numeroPlanoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*11, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField numeroPlanoText = new JTextField();
	numeroPlanoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*11, WIDTH_TXT, HEIGHT_TXT));
	numeroPlanoText.setName(AudasaPreferences.NUMERO_PLANO);
	componentsList.add(numeroPlanoTitle);
	componentsList.add(numeroPlanoText);

	JButton continuarButton = new JButton(CONTINUAR_BUTTON);
	continuarButton.setName(CONTINUAR_BUTTON);
	continuarButton.setMnemonic(KeyEvent.VK_C);
	continuarButton.setBounds(new Rectangle(
		MARGIN_LEFT_TXT+(WIDTH_TXT/2), MARGIN_TOP*(12+1), WIDTH_TXT/2, 26));
	componentsList.add(continuarButton);

	return componentsList;
    }
    
    private static ArrayList<Component> getComponentsForTemplateA4VerticalConsultas() {
    	componentsList = new ArrayList<Component>();

    	JLabel ingenieroDirectorTitle = new JLabel("Ingeniero director:");
    	ingenieroDirectorTitle.setBounds(new Rectangle(
    		MARGIN_LEFT_TITLE, MARGIN_TOP*1, WIDTH_TITLE, HEIGHT_TITLE));
    	JTextField ingenieroDirectorText = new JTextField();
    	ingenieroDirectorText.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT, MARGIN_TOP*1, WIDTH_TXT, HEIGHT_TXT));
    	ingenieroDirectorText.setName(AudasaPreferences.INGENIERO_DIRECTOR);
    	componentsList.add(ingenieroDirectorTitle);
    	componentsList.add(ingenieroDirectorText);

    	JLabel provinciaTitle = new JLabel("Provincia:");
    	provinciaTitle.setBounds(new Rectangle(
    		MARGIN_LEFT_TITLE, MARGIN_TOP*2, WIDTH_TITLE, HEIGHT_TITLE));
    	JTextField provinciaText = new JTextField();
    	provinciaText.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT, MARGIN_TOP*2, WIDTH_TXT, HEIGHT_TXT));
    	provinciaText.setName(AudasaPreferences.PROVINCIA);
    	componentsList.add(provinciaTitle);
    	componentsList.add(provinciaText);

    	JLabel municipioTitle = new JLabel("Municipio:");
    	municipioTitle.setBounds(new Rectangle(
    		MARGIN_LEFT_TITLE, MARGIN_TOP*3, WIDTH_TITLE, HEIGHT_TITLE));
    	JTextField municipioText = new JTextField();
    	municipioText.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT, MARGIN_TOP*3, WIDTH_TXT, HEIGHT_TXT));
    	municipioText.setName(AudasaPreferences.MUNICIPIO);
    	componentsList.add(municipioTitle);
    	componentsList.add(municipioText);

    	JLabel consultaTitle = new JLabel("Ref. Consulta:");
    	consultaTitle.setBounds(new Rectangle(
    		MARGIN_LEFT_TITLE, MARGIN_TOP*4, WIDTH_TITLE, HEIGHT_TITLE));
    	JTextField consultaText = new JTextField();
    	consultaText.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT, MARGIN_TOP*4, WIDTH_TXT, HEIGHT_TXT));
    	consultaText.setName(AudasaPreferences.CONSULTA);
    	componentsList.add(consultaTitle);
    	componentsList.add(consultaText);

    	JLabel fechaTitle = new JLabel("Fecha:");
    	fechaTitle.setBounds(new Rectangle(
    		MARGIN_LEFT_TITLE, MARGIN_TOP*5, WIDTH_TITLE, HEIGHT_TITLE));
    	JTextField fechaText = new JTextField();
    	fechaText.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT, MARGIN_TOP*5, WIDTH_TXT, HEIGHT_TXT));
    	fechaText.setName(AudasaPreferences.FECHA);
    	componentsList.add(fechaTitle);
    	componentsList.add(fechaText);

    	JLabel tituloPlanoTitle = new JLabel("Título del plano:");
    	tituloPlanoTitle.setBounds(new Rectangle(
    		MARGIN_LEFT_TITLE, MARGIN_TOP*6, WIDTH_TITLE, HEIGHT_TITLE));
    	JTextField tituloPlanoText = new JTextField();
    	tituloPlanoText.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT, MARGIN_TOP*6, WIDTH_TXT, HEIGHT_TXT));
    	tituloPlanoText.setName(AudasaPreferences.TITULO_PLANO);
    	componentsList.add(tituloPlanoTitle);
    	componentsList.add(tituloPlanoText);

    	JButton continuarButton = new JButton(CONTINUAR_BUTTON);
    	continuarButton.setName(CONTINUAR_BUTTON);
    	continuarButton.setMnemonic(KeyEvent.VK_C);
    	continuarButton.setBounds(new Rectangle(
    		MARGIN_LEFT_TXT+(WIDTH_TXT/2), MARGIN_TOP*(7+1), WIDTH_TXT/2, 26));
    	componentsList.add(continuarButton);

    	return componentsList;
        }

    private static ArrayList<Component> getComponentsForTemplateDimensiones() {
	componentsList = new ArrayList<Component>();

	JLabel ingenieroDirectorTitle = new JLabel("Ingeniero director:");
	ingenieroDirectorTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*1, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField ingenieroDirectorText = new JTextField();
	ingenieroDirectorText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*1, WIDTH_TXT, HEIGHT_TXT));
	ingenieroDirectorText.setName(AudasaPreferences.INGENIERO_DIRECTOR);
	componentsList.add(ingenieroDirectorTitle);
	componentsList.add(ingenieroDirectorText);

	JLabel consultoraTitle = new JLabel("Consultora:");
	consultoraTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*2, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField consultoraText = new JTextField();
	consultoraText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*2, WIDTH_TXT, HEIGHT_TXT));
	consultoraText.setName(AudasaPreferences.CONSULTORA);
	componentsList.add(consultoraTitle);
	componentsList.add(consultoraText);

	JLabel ingenieroAutorTitle = new JLabel("Ingeniero autor:");
	ingenieroAutorTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*3, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField ingenieroAutorText = new JTextField();
	ingenieroAutorText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*3, WIDTH_TXT, HEIGHT_TXT));
	ingenieroAutorText.setName(AudasaPreferences.INGENIERO_AUTOR);
	componentsList.add(ingenieroAutorTitle);
	componentsList.add(ingenieroAutorText);

	JLabel proyectoTitle = new JLabel("Proyecto:");
	proyectoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*4, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField proyectoText = new JTextField();
	proyectoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*4, WIDTH_TXT, HEIGHT_TXT));
	proyectoText.setName(AudasaPreferences.PROYECTO);
	componentsList.add(proyectoTitle);
	componentsList.add(proyectoText);

	JLabel dibujoTitle = new JLabel("Dibujo:");
	dibujoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*5, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField dibujoText = new JTextField();
	dibujoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*5, WIDTH_TXT, HEIGHT_TXT));
	dibujoText.setName(AudasaPreferences.DIBUJO);
	componentsList.add(dibujoTitle);
	componentsList.add(dibujoText);

	JLabel claveTitle = new JLabel("Clave:");
	claveTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*6, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField claveText = new JTextField();
	claveText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*6, WIDTH_TXT, HEIGHT_TXT));
	claveText.setName(AudasaPreferences.CLAVE);
	componentsList.add(claveTitle);
	componentsList.add(claveText);

	JLabel fechaTitle = new JLabel("Fecha:");
	fechaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*7, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField fechaText = new JTextField();
	fechaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*7, WIDTH_TXT, HEIGHT_TXT));
	fechaText.setName(AudasaPreferences.FECHA);
	componentsList.add(fechaTitle);
	componentsList.add(fechaText);

	JLabel sustituyeATitle = new JLabel("Sustituye a:");
	sustituyeATitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*8, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField sustituyeAText = new JTextField();
	sustituyeAText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*8, WIDTH_TXT, HEIGHT_TXT));
	sustituyeAText.setName(AudasaPreferences.SUSTITUYE_A);
	componentsList.add(sustituyeATitle);
	componentsList.add(sustituyeAText);

	JLabel sustituidoPorTitle = new JLabel("Sustituido por:");
	sustituidoPorTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*9, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField sustituidoPorText = new JTextField();
	sustituidoPorText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*9, WIDTH_TXT, HEIGHT_TXT));
	sustituidoPorText.setName(AudasaPreferences.SUSTITUIDO_POR);
	componentsList.add(sustituidoPorTitle);
	componentsList.add(sustituidoPorText);

	JLabel tramoTitle = new JLabel("Tramo:");
	tramoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*10, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField tramoText = new JTextField();
	tramoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*10, WIDTH_TXT, HEIGHT_TXT));
	tramoText.setName(AudasaPreferences.TRAMO);
	componentsList.add(tramoTitle);
	componentsList.add(tramoText);

	JLabel numeroPlanoTitle = new JLabel("Nº del plano:");
	numeroPlanoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*11, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField numeroPlanoText = new JTextField();
	numeroPlanoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*11, WIDTH_TXT, HEIGHT_TXT));
	numeroPlanoText.setName(AudasaPreferences.NUMERO_PLANO);
	componentsList.add(numeroPlanoTitle);
	componentsList.add(numeroPlanoText);

	JButton continuarButton = new JButton(CONTINUAR_BUTTON);
	continuarButton.setName(CONTINUAR_BUTTON);
	continuarButton.setMnemonic(KeyEvent.VK_C);
	continuarButton.setBounds(new Rectangle(
		MARGIN_LEFT_TXT+(WIDTH_TXT/2)+10, MARGIN_TOP*(12+1), (WIDTH_TXT/2)-5, 26));
	componentsList.add(continuarButton);

	return componentsList;
    }

    private static ArrayList<Component> getComponentsForTemplatePM() {
	componentsList = new ArrayList<Component>();

	JLabel proyectoTitle = new JLabel("Proyecto:");
	proyectoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*1, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField proyectoText = new JTextField();
	proyectoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*1, WIDTH_TXT, HEIGHT_TXT));
	proyectoText.setName(AudasaPreferences.PROYECTO);
	componentsList.add(proyectoTitle);
	componentsList.add(proyectoText);

	JLabel provinciaTitle = new JLabel("Provincia:");
	provinciaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*2, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField provinciaText = new JTextField();
	provinciaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*2, WIDTH_TXT, HEIGHT_TXT));
	provinciaText.setName(AudasaPreferences.PROVINCIA);
	componentsList.add(provinciaTitle);
	componentsList.add(provinciaText);

	JLabel municipioTitle = new JLabel("Municipio:");
	municipioTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*3, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField municipioText = new JTextField();
	municipioText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*3, WIDTH_TXT, HEIGHT_TXT));
	municipioText.setName(AudasaPreferences.MUNICIPIO);
	componentsList.add(municipioTitle);
	componentsList.add(municipioText);

	JLabel consultaTitle = new JLabel("Nº Referencia:");
	consultaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*4, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField consultaText = new JTextField();
	consultaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*4, WIDTH_TXT, HEIGHT_TXT));
	consultaText.setName(AudasaPreferences.NUMERO_REFERENCIA);
	componentsList.add(consultaTitle);
	componentsList.add(consultaText);

	JLabel fechaTitle = new JLabel("Fecha:");
	fechaTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*5, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField fechaText = new JTextField();
	fechaText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*5, WIDTH_TXT, HEIGHT_TXT));
	fechaText.setName(AudasaPreferences.FECHA);
	componentsList.add(fechaTitle);
	componentsList.add(fechaText);

	JLabel tituloPlanoTitle = new JLabel("Título del plano:");
	tituloPlanoTitle.setBounds(new Rectangle(
		MARGIN_LEFT_TITLE, MARGIN_TOP*6, WIDTH_TITLE, HEIGHT_TITLE));
	JTextField tituloPlanoText = new JTextField();
	tituloPlanoText.setBounds(new Rectangle(
		MARGIN_LEFT_TXT, MARGIN_TOP*6, WIDTH_TXT, HEIGHT_TXT));
	tituloPlanoText.setName(AudasaPreferences.TITULO_PLANO);
	componentsList.add(tituloPlanoTitle);
	componentsList.add(tituloPlanoText);

	JButton continuarButton = new JButton(CONTINUAR_BUTTON);
	continuarButton.setName(CONTINUAR_BUTTON);
	continuarButton.setMnemonic(KeyEvent.VK_C);
	continuarButton.setBounds(new Rectangle(
		MARGIN_LEFT_TXT+(WIDTH_TXT/2), MARGIN_TOP*(7+1), WIDTH_TXT/2, 26));
	componentsList.add(continuarButton);

	return componentsList;
    }

}
