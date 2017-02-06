package es.icarto.gvsig.extgia.consultas;

import java.awt.Font;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;

import javax.swing.table.DefaultTableModel;

import org.apache.log4j.Logger;

import com.iver.andami.PluginServices;
import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.style.RtfParagraphStyle;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.siga.SIGAConfigExtension;
import es.icarto.gvsig.siga.models.InfoEmpresa;
import es.icarto.gvsig.utils.SIGAFormatter;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;

public abstract class PDFReport {

    private static final Logger logger = Logger.getLogger(PDFReport.class);

    protected final com.lowagie.text.Font cellBoldStyle = FontFactory.getFont(
	    "arial", 6, Font.BOLD);
    protected final com.lowagie.text.Font bodyBoldStyle = FontFactory.getFont(
	    "arial", 8, Font.BOLD);

    private final static DateFormat dateFormatter = DateFormatNT
	    .getDateFormat();

    boolean isFirstPage = true;

    protected Document document;
    private final String elementID;

    protected final ConsultasFilters<Field> filters;
    protected final QueryType reportType;

    public PDFReport(String[] element, String fileName,
	    DefaultTableModel table, ConsultasFilters<Field> filters,
	    QueryType reportType) {
	this.filters = filters;
	this.elementID = element[0];
	this.reportType = reportType;
	writePdfReport(element[1], fileName, table, filters, reportType);
    }

    protected String getTitle() {
	return reportType.title();
    }

    protected Rectangle setPageSize() {
	return PageSize.A4.rotate();
    }

    protected abstract String[] getColumnNames();

    protected abstract float[] getColumnsWidth(int columnCount);

    protected abstract boolean hasEmbebedTable();

    protected abstract PdfPCell writeAditionalColumnName();

    protected abstract PdfPCell writeAditionalColumnValues(String id);

    private Image getHeaderImage() {

	SIGAConfigExtension ext = (SIGAConfigExtension) PluginServices
		.getExtension(SIGAConfigExtension.class);
	InfoEmpresa infoEmpresa = ext.getInfoEmpresa();
	String logoPath = infoEmpresa.getReportLogo(filters.getTramo());

	Image image = null;
	try {
	    image = Image.getInstance(logoPath);
	    image.scaleToFit(150, 40);
	    image.setAlignment(Chunk.ALIGN_RIGHT);
	} catch (IOException e) {
	    logger.error(e.getMessage(), e);
	} catch (BadElementException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return image;
    }

    private void writeFilters(Document document, ConsultasFilters<Field> filters) {
	try {
	    Paragraph amP = null;
	    if (filters.getArea() == null) {
		amP = new Paragraph("Área Mantenimiento: -", bodyBoldStyle);
	    } else {
		amP = new Paragraph("Área Mantenimiento: "
			+ filters.getArea().getValue(), bodyBoldStyle);
	    }
	    document.add(amP);

	    Paragraph bcP = null;
	    if (filters.getBaseContratista() == null) {
		bcP = new Paragraph("Base Contratista: -", bodyBoldStyle);
	    } else {
		bcP = new Paragraph("Base Contratista: "
			+ filters.getBaseContratista().getValue(),
			bodyBoldStyle);
	    }
	    document.add(bcP);

	    Paragraph tramoP = null;
	    if (filters.getTramo() == null) {
		tramoP = new Paragraph("Tramo: -", bodyBoldStyle);
	    } else {
		tramoP = new Paragraph("Tramo: "
			+ filters.getTramo().getValue(), bodyBoldStyle);
	    }
	    document.add(tramoP);
	    writeDatesRange(document, filters);
	    document.add(Chunk.NEWLINE);
	} catch (DocumentException e) {
	    e.printStackTrace();
	}
    }

    protected void writeDatesRange(Document document,
	    ConsultasFilters<Field> filters) throws DocumentException {
	Paragraph mesP = new Paragraph("Desde: "
		+ filters.getFechaInicioFormatted(), bodyBoldStyle);
	document.add(mesP);
	Paragraph anhoP = new Paragraph("Hasta: "
		+ filters.getFechaFinFormatted(), bodyBoldStyle);
	document.add(anhoP);
    }

    private void writeTitleAndSubtitle(Document document, String title,
	    String subtitle) {

	Paragraph titleP = new Paragraph(title,
		RtfParagraphStyle.STYLE_HEADING_1);
	titleP.setAlignment(Paragraph.ALIGN_CENTER);
	try {
	    document.add(titleP);
	} catch (DocumentException e) {
	    e.printStackTrace();
	}

	Paragraph subtitleP = new Paragraph(subtitle,
		RtfParagraphStyle.STYLE_HEADING_2);
	subtitleP.setAlignment(Paragraph.ALIGN_CENTER);
	try {
	    document.add(subtitleP);
	} catch (DocumentException e) {
	    e.printStackTrace();
	}
    }

    private String getDateFormated() {
	Date d = Calendar.getInstance().getTime();
	String date = dateFormatter.format(d);
	return date;
    }

    private void writeDate(Document document) {
	Paragraph dateP = new Paragraph(getDateFormated(), bodyBoldStyle);
	dateP.setAlignment(Paragraph.ALIGN_CENTER);
	try {
	    document.add(dateP);
	} catch (DocumentException e) {
	    e.printStackTrace();
	}
    }

    protected void writeNumberOfRows(Document document, int numRows) {
	Paragraph numRowsP = new Paragraph("Número de registros: " + numRows,
		bodyBoldStyle);
	try {
	    document.add(numRowsP);
	} catch (DocumentException e) {
	    e.printStackTrace();
	}
    }

    private void writePdfReportContent(Document document, String element,
	    DefaultTableModel tableModel, ConsultasFilters<Field> filters,
	    QueryType reportType) {
	try {
	    // Header
	    Image image = getHeaderImage();
	    document.add(image);

	    // Footer
	    Phrase footerPhrase = new Phrase("Página: ", bodyBoldStyle);
	    HeaderFooter footer = new HeaderFooter(footerPhrase, true);
	    footer.setBorder(Rectangle.NO_BORDER);
	    document.setFooter(footer);

	    // Write title,subtitle and date report
	    String title = getTitle();
	    String subtitle = element;
	    writeTitleAndSubtitle(document, title, subtitle);
	    writeDate(document);
	    document.add(Chunk.NEWLINE);

	    // write filters
	    writeFilters(document, filters);

	    // Column names
	    PdfPTable table = writeColumnNames(document);

	    isFirstPage = false;

	    // Values
	    writeValues(document, tableModel, table, reportType);

	    // Close file
	    document.close();

	} catch (DocumentException e) {
	    e.printStackTrace();
	} catch (SQLException e) {
	    e.printStackTrace();
	}

    }

    protected void writeValues(Document document, DefaultTableModel tableModel,
	    PdfPTable table, QueryType reportType) throws SQLException,
	    DocumentException {
	Paragraph paragraph;
	int startColumn;
	int endColumn;
	if (reportType != QueryType.CARACTERISTICAS) {
	    startColumn = 0;
	    endColumn = getColumnNames().length - 1;
	} else {
	    startColumn = 1;
	    endColumn = getColumnNames().length;
	}

	for (int row = 0; row < tableModel.getRowCount(); row++) {
	    for (int column = startColumn; column <= endColumn; column++) {
		Object value = tableModel.getValueAt(row, column);
		paragraph = new Paragraph(SIGAFormatter.formatValue(value),
			cellBoldStyle);
		PdfPCell valueCell = new PdfPCell(paragraph);
		valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(valueCell);
	    }
	    if (hasEmbebedTable()) {
		table.addCell(writeAditionalColumnValues(tableModel.getValueAt(
			row, 1).toString()));
	    }
	}

	document.add(table);
	document.add(Chunk.NEWLINE);
	writeNumberOfRows(document, tableModel.getRowCount());
    }

    protected PdfPTable writeColumnNames(Document document)
	    throws DocumentException {
	PdfPTable table = getColumnNames(document);
	return table;
    }

    private PdfPTable getColumnNames(Document document)
	    throws DocumentException {
	int numColumns;
	if (hasEmbebedTable()) {
	    numColumns = getColumnNames().length + 1;
	} else {
	    numColumns = getColumnNames().length;
	}
	PdfPTable table = new PdfPTable(numColumns);
	table.setTotalWidth(document.getPageSize().getWidth()
		- document.leftMargin() - document.rightMargin());
	table.setWidths(getColumnsWidth(numColumns));
	table.setWidthPercentage(100);
	for (int i = 0; i < getColumnNames().length; i++) {
	    Paragraph column = new Paragraph(getColumnNames()[i], bodyBoldStyle);
	    PdfPCell columnCell = new PdfPCell(column);
	    columnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    table.addCell(columnCell);
	}
	if (hasEmbebedTable()) {
	    table.addCell(writeAditionalColumnName());
	}
	return table;
    }

    public void writePdfReport(String element, String fileName,
	    DefaultTableModel table, ConsultasFilters<Field> filters,
	    QueryType reportType) {
	document = new Document(setPageSize());
	try {
	    PdfWriter writer = PdfWriter.getInstance(document,
		    new FileOutputStream(fileName));
	    writer.setPageEvent(new MyPageEvent());
	    document.open();

	    writePdfReportContent(document, element, table, filters, reportType);

	    document.close();

	} catch (FileNotFoundException e) {
	    e.printStackTrace();
	} catch (DocumentException e) {
	    e.printStackTrace();
	}
    }

    public class MyPageEvent extends PdfPageEventHelper {

	@Override
	public void onStartPage(PdfWriter pdfWriter, Document document) {
	    if (!isFirstPage && getColumnNames() != null) {
		try {
		    document.add(Chunk.NEWLINE);
		    PdfPTable table = getColumnNames(document);
		    document.add(table);
		} catch (DocumentException e1) {
		    e1.printStackTrace();
		}
	    }
	}

	@Override
	public void onEndPage(PdfWriter pdfWriter, Document document) {

	}
    }

    protected ConsultasFilters<Field> getFilters() {
	return filters;
    }

    protected String getElementID() {
	return elementID;
    }

}
