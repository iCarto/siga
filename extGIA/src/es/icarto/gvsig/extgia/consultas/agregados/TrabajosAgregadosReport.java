package es.icarto.gvsig.extgia.consultas.agregados;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.Locale;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFReport;
import es.icarto.gvsig.extgia.consultas.QueryType;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class TrabajosAgregadosReport extends PDFReport {

    private TrabajosAgregadosReportQueries agregadosReportQueries = null;

    public TrabajosAgregadosReport(String[] element, String fileName,
	    ResultSet resultMap, ConsultasFilters<Field> filters,
	    QueryType reportType) {
	super(element, fileName, null, filters, reportType);
    }

    @Override
    protected Rectangle setPageSize() {
	return PageSize.A4;
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID Elemento", "Tramo", "Tipo Vía",
		"Nombre Vía", "PK Inicial", "PK Final", "Sentido",
		"Medición" };
	return columnNames;
    }

    @Override
    protected float[] getColumnsWidth(int columnCount) {
	float[] columnsWidth = new float[columnCount];

	columnsWidth[0] = 60f;
	columnsWidth[1] = 60f;
	columnsWidth[2] = 60f;
	columnsWidth[3] = 60f;
	columnsWidth[4] = 60f;
	columnsWidth[5] = 60f;
	columnsWidth[6] = 60f;
	columnsWidth[7] = 60f;

	return columnsWidth;
    }

    @Override
    protected PdfPTable writeColumnNames(Document document) {
	return null;
    }

    @Override
    protected void writeValues(Document document, DefaultTableModel tableModel,
	    PdfPTable table, QueryType reportType) {
	agregadosReportQueries = new TrabajosAgregadosReportQueries(
		getElementID(), getFilters());
	writeTable("Desbroce con retroaraña\n\n",
		agregadosReportQueries.getDesbroceRetroaranhaQuery(),
		agregadosReportQueries.getDesbroceRetroaranhaSumQuery());
	writeTable("\nDesbroce mecánico\n\n",
		agregadosReportQueries.getDesbroceMecanicoQuery(),
		agregadosReportQueries.getDesbroceMecanicoSumQuery());
	writeTable("\nTala y desbroce manual\n\n",
		agregadosReportQueries.getDesbroceManualQuery(),
		agregadosReportQueries.getDesbroceManualSumQuery());

	writeTotal(document, "TOTAL DESBROCES",
		agregadosReportQueries.getDesbroceTotalSumQuery());

	writeTable("\nSiega mecánica de isletas\n\n",
		agregadosReportQueries.getSiegaMecanicaIsletasQuery(),
		agregadosReportQueries.getSiegaMecanicaIsletasSumQuery());

	writeTable("\nSiega mecánica de medianas\n\n",
		agregadosReportQueries.getSiegaMecanicaMedianaQuery(),
		agregadosReportQueries.getSiegaMecanicaMedianaSumQuery());

	writeTable("\nSiega mecánica de medianas < 1,5 m\n\n",
		agregadosReportQueries.getSiegaMecanicaMediana1_5mQuery(),
		agregadosReportQueries.getSiegaMecanicaMediana1_5mSumQuery());

	writeTotal(document, "TOTAL SEGADO DE HIERBAS",
		agregadosReportQueries.getSiegaTotalSumQuery());

	writeTable("\nHerbicida\n\n",
		agregadosReportQueries.getHerbicidadQuery(),
		agregadosReportQueries.getHerbicidaSumQuery());

	writeTable(
		"\nEliminación veg. mediana de HG y transp. a vertedero\n\n",
		agregadosReportQueries.getVegetacionQuery(),
		agregadosReportQueries.getVegetacionSumQuery());
    }

    private void writeTotal(Document document, String title, String query) {
	ResultSet rs = getValuesFromQuery(query);
	try {
	    rs.next();
	    if (rs.getString(1) != null) {
		PdfPTable totalTable = new PdfPTable(getColumnNames().length);
		totalTable.setTotalWidth(document.getPageSize().getWidth()
			- document.leftMargin() - document.rightMargin());
		totalTable.setWidths(getColumnsWidth(getColumnNames().length));
		totalTable.setWidthPercentage(100);

		document.add(Chunk.NEWLINE);

		PdfPCell totalCell = new PdfPCell(new Paragraph(title,
			bodyBoldStyle));
		totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		totalCell.setBorder(Rectangle.NO_BORDER);
		totalTable.addCell(totalCell);

		for (int i = 0; i < 6; i++) {
		    PdfPCell cell = new PdfPCell();
		    cell.setBorder(Rectangle.NO_BORDER);
		    totalTable.addCell(cell);
		}

		rs.beforeFirst();
		while (rs.next()) {
		    NumberFormat nf = NumberFormat.getInstance(Locale
			    .getDefault());
		    PdfPCell medicionAudasaCell = new PdfPCell(new Paragraph(
			    nf.format(rs.getDouble(1)), cellBoldStyle));
		    medicionAudasaCell
			    .setHorizontalAlignment(Element.ALIGN_CENTER);
		    totalTable.addCell(medicionAudasaCell);
		}
		document.add(totalTable);
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	} catch (DocumentException e) {
	    e.printStackTrace();
	}
    }

    private void writeTable(String tableTittle, String contentQuery,
	    String totalQuery) {
	try {
	    ResultSet resultMap;

	    resultMap = getValuesFromQuery(contentQuery);
	    if (resultMap.next()) {
		document.add(new Paragraph(tableTittle, bodyBoldStyle));
		PdfPTable table = super.writeColumnNames(document);
		writeTableContent(table, resultMap);
		if (totalQuery != null) {
		    resultMap = getValuesFromQuery(totalQuery);
		    PdfPCell totalCell = new PdfPCell(new Paragraph("TOTAL",
			    bodyBoldStyle));
		    totalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		    table.addCell(totalCell);

		    for (int i = 0; i < 6; i++) {
			PdfPCell cell = new PdfPCell();
			table.addCell(cell);
		    }

		    while (resultMap.next()) {
			NumberFormat nf = NumberFormat.getInstance(Locale
				.getDefault());
			PdfPCell medicionAudasaCell = new PdfPCell(
				new Paragraph(
					nf.format(resultMap.getDouble(8)),
					cellBoldStyle));
			medicionAudasaCell
				.setHorizontalAlignment(Element.ALIGN_CENTER);
			table.addCell(medicionAudasaCell);
		    }
		    document.add(table);
		}
	    }
	} catch (DocumentException e) {
	    e.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}
    }

    private ResultSet getValuesFromQuery(String query) {
	PreparedStatement statement;
	try {
	    statement = DBSession.getCurrentSession().getJavaConnection()
		    .prepareStatement(query);
	    statement.execute();
	    ResultSet rs = statement.getResultSet();
	    return rs;
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return null;
    }

    private void writeTableContent(PdfPTable table, ResultSet resultMap)
	    throws DocumentException, SQLException {
	Paragraph value;
	resultMap.beforeFirst();
	while (resultMap.next()) {
	    for (int column = 1; column <= getColumnNames().length; column++) {
		if (resultMap.getString(column) != null) {
		    String valueFormatted = SIGAFormatter.writeDBValueFormatted(
			    resultMap, column);
		    value = new Paragraph(valueFormatted, cellBoldStyle);
		} else {
		    value = new Paragraph("");
		}
		PdfPCell valueCell = new PdfPCell(value);
		valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(valueCell);
	    }
	}
    }

    @Override
    protected boolean hasEmbebedTable() {
	return false;
    }

    @Override
    protected PdfPCell writeAditionalColumnName() {
	return null;
    }

    @Override
    protected PdfPCell writeAditionalColumnValues(String id) {
	return null;
    }

}
