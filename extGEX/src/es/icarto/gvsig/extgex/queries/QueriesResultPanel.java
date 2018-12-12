/*
 * Copyright (c) 2010. Cartolab (Universidade da Coruña)
 * Copyright (c) 2014. iCarto
 * 
 * This program is free software: you can redistribute it and/or modify it under the terms
 * of the GNU General Public License as published by the Free Software Foundation, either
 * version 3 of the License, or any later version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License along with EIEL Validation
 * If not, see <http://www.gnu.org/licenses/>.
 */

package es.icarto.gvsig.extgex.queries;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import net.miginfocom.swing.MigLayout;
import es.icarto.gvsig.commons.gui.gvWindow;
import es.icarto.gvsig.commons.queries.QueryFiltersI;

@SuppressWarnings("serial")
public class QueriesResultPanel extends gvWindow implements ActionListener {

    private final JEditorPane resultTA;
    private final JButton exportB;
    private final JComboBox fileTypeCB;
    String[] fileFormats = { QueriesOuputWidget.HTML, QueriesOuputWidget.RTF,
	    QueriesOuputWidget.PDF, QueriesOuputWidget.CSV };
    private final QueryFiltersI filters;
    private final ResultTableModel table;

    public QueriesResultPanel(ResultTableModel result, QueryFiltersI filters) {
	super(800, 500, false);
	this.table = result;
	this.filters = filters;
	setTitle("Resultado de la consulta");
	MigLayout layout = new MigLayout("inset 0, align center", "[grow]",
		"[grow][]");

	setLayout(layout);

	resultTA = new JEditorPane();
	resultTA.setEditable(false);
	resultTA.setContentType("text/html");
	JScrollPane scrollPane = new JScrollPane(resultTA);
	resultTA.setText(showResultsAsHTML());
	JPanel panel = new JPanel();
	exportB = new JButton("Exportar");
	exportB.addActionListener(this);
	panel.add(exportB);
	fileTypeCB = new JComboBox(fileFormats);
	panel.add(fileTypeCB);

	add(scrollPane, "growx, growy, wrap");
	add(panel, "shrink, align right");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	if (e.getSource() == exportB) {
	    String sel = fileTypeCB.getSelectedItem() == null ? "" : fileTypeCB
		    .getSelectedItem().toString();
	    new QueriesOuputWidget().to(sel, table, filters);
	}
    }

    private String showResultsAsHTML() {
	StringBuffer sf = new StringBuffer();

	sf.append("<h3 style=\"color: blue\">" + table.getCode() + "  -  "
		+ table.getDescription() + "</h3>");

	sf.append("<p>" + table.getQueryTables() + "</p>");
	sf.append(table.getHTML());

	sf.append("</h2>");
	sf.append("<hr>");

	return sf.toString();
    }

}