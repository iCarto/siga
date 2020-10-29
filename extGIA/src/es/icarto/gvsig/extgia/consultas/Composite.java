package es.icarto.gvsig.extgia.consultas;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;

import es.icarto.gvsig.commons.queries.Component;
import es.icarto.gvsig.commons.queries.FinalActions;
import es.icarto.gvsig.commons.queries.ReportValidationResult;
import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.navtableforms.ormlite.domainvalues.KeyValue;

public class Composite implements Component {
    private final List<Component> childs = new ArrayList<Component>();
    private final ConsultasFilters<Field> consultasFilters;
    private final KeyValue tipoConsulta;
    private final boolean pdf;

    private File outputPath;

    public Composite(ConsultasFilters<Field> consultasFilters,
	    KeyValue tipoConsulta, boolean pdf) {
	this.consultasFilters = consultasFilters;
	this.tipoConsulta = tipoConsulta;
	this.pdf = pdf;
    }

    @Override
    public boolean setOutputPath(File path) {
	if (path == null) {
	    JFileChooser fileChooser = new JFileChooser();
	    fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
	    fileChooser.showSaveDialog(null);
	    outputPath = fileChooser.getSelectedFile();
	    if (outputPath == null) {
		return false;
	    }
	} else {
	    outputPath = path;
	}

	for (Component c : childs) {
	    c.setOutputPath(outputPath);
	}
	return outputPath != null;
    }

    @Override
    public void generateReportFile() {
	for (Component child : childs) {
	    child.generateReportFile();
	}
    }

    @Override
    public void finalActions() {
	FinalActions finalActions = new FinalActions(outputPath, new ReportValidationResult(""));
	finalActions.openFolder();
    }

    public void add(List<String[]> list) {
	for (String[] e : list) {
	    Leaf leaf = new Leaf(e, consultasFilters, tipoConsulta, pdf);
	    childs.add(leaf);
	}
    }
}
