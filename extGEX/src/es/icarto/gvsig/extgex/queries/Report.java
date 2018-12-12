package es.icarto.gvsig.extgex.queries;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.text.DateFormat;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.lowagie.text.BadElementException;
import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.FontFactory;
import com.lowagie.text.HeaderFooter;
import com.lowagie.text.Image;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.Table;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfPageEventHelper;
import com.lowagie.text.pdf.PdfTemplate;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.rtf.RtfWriter2;
import com.lowagie.text.rtf.document.RtfDocumentSettings;
import com.lowagie.text.rtf.style.RtfParagraphStyle;
import com.lowagie.text.rtf.table.RtfCell;

import es.icarto.gvsig.commons.queries.QueryFiltersI;
import es.icarto.gvsig.commons.utils.Field;
import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;
import es.udc.cartolab.gvsig.navtable.format.DoubleFormatNT;
import es.udc.cartolab.gvsig.navtable.format.IntegerFormatNT;

public class Report {

    private static final Logger logger = Logger.getLogger(Report.class);

    protected static final int RTF = 0;
    protected static final int PDF = 1;

    private final Font cellBoldStyle = FontFactory.getFont("arial", 6,
	    Font.BOLD);
    private final Font bodyBoldStyle = FontFactory.getFont("arial", 8,
	    Font.BOLD);

    private static final float TITULAR_COLUMN_WIDTH = 200f;
    private static final float PAGOS_COLUMN_WIDTH = 50f;

    private static DateFormat dateFormatter = DateFormatNT.getDateFormat();
    private static NumberFormat doubleFormatter = DoubleFormatNT
	    .getDisplayingFormat();
    private static NumberFormat intFormatter = IntegerFormatNT
	    .getDisplayingFormat();

    private String[] tableHeader;
    private boolean startNewReport;
    private final ResultTableModel resultsMap;

    public Report(int reportType, String fileName, ResultTableModel resultsMap,
	    QueryFiltersI filters) {
	this.resultsMap = resultsMap;
	if (reportType == RTF) {
	    writeRtfReport(fileName, resultsMap, filters);
	}
	if (reportType == PDF) {
	    writePdfReport(fileName, resultsMap, filters);
	}
    }

    private Image getHeaderImage() {
	Image image = null;
	try {
	    image = Image
		    .getInstance("gvSIG/extensiones/es.icarto.gvsig.extgex/images/logo_audasa.gif");
	    image.scalePercent((float) 15.00);
	    image.setAbsolutePosition(0, 0);
	    image.setAlignment(Chunk.ALIGN_RIGHT);
	} catch (BadElementException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (MalformedURLException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (IOException e) {
	    logger.error(e.getStackTrace(), e);
	}
	return image;
    }

    private void writeFilters(Document document, QueryFiltersI filters) {
        Collection<Field> location = filters.getLocation();
        try {
            for (Field f : location) {
                Paragraph p = new Paragraph(f.getLongName() + f.getValue(), bodyBoldStyle);
                    document.add(p);
            }
            document.add(Chunk.NEWLINE);
        } catch (DocumentException e) {
            logger.error(e.getStackTrace(), e);
        }
    }

    private String writeFiltersInHeader(QueryFiltersI filters) {
        String s = "\n";
        for (Field f : filters.getLocation()) {
            s = s + f.getLongName() + f.getValue() + " ";
        }
        return s;
    }

    private void writeTitleAndSubtitle(Document document, String title,
	    String subtitle) {

	Paragraph titleP = new Paragraph(title,
		RtfParagraphStyle.STYLE_HEADING_1);
	titleP.setAlignment(Paragraph.ALIGN_CENTER);
	try {
	    document.add(titleP);
	} catch (DocumentException e) {
	    logger.error(e.getStackTrace(), e);
	}

	Paragraph subtitleP = new Paragraph(subtitle,
		RtfParagraphStyle.STYLE_HEADING_2);
	subtitleP.setAlignment(Paragraph.ALIGN_CENTER);
	try {
	    document.add(subtitleP);
	} catch (DocumentException e) {
	    logger.error(e.getStackTrace(), e);
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
	    logger.error(e.getStackTrace(), e);
	}
    }

    private void writeNumberOfFincas(Document document, int numFincas) {
	Paragraph numFincasP = new Paragraph("Número de fincas: " + numFincas,
		bodyBoldStyle);
	try {
	    document.add(numFincasP);
	} catch (DocumentException e) {
	    logger.error(e.getStackTrace(), e);
	}
    }

    private float[] getColumnsWidth(PdfPTable table, int columnCount,
	    float specialWith) {
	if (resultsMap.getQueryFilters().getFields() != null) {
	    float[] columnsWidth = new float[columnCount];
	    float columnWidh = table.getTotalWidth() / columnCount;
	    Arrays.fill(columnsWidth, columnWidh);
	    return columnsWidth;
	}
	float[] columnsWidth = new float[columnCount];
	for (int i = 0; i < columnCount; i++) {
	    if (i == 1) {
		columnsWidth[i] = specialWith;
	    } else {
		columnsWidth[i] = (table.getTotalWidth() - specialWith)
			/ (columnCount - 1);
	    }
	}
	return columnsWidth;
    }

    private void writeRtfReportContent(Document document,
	    ResultTableModel result, QueryFiltersI filters) {
	try {
	    // Header
	    Image image = getHeaderImage();
	    document.add(image);

	    // Write title,subtitle and date report
	    String title = result.getTitle();
	    String subtitle = result.getSubtitle();
	    writeTitleAndSubtitle(document, title, subtitle);
	    writeDate(document);
	    document.add(Chunk.NEWLINE);

	    // write filters
	    writeFilters(document, filters);

	    int columnCount = result.getColumnCount();
	    Table table = new Table(columnCount);

	    // Column names
	    for (int i = 0; i < columnCount; i++) {
		Paragraph column = new Paragraph(result.getColumnName(i),
			bodyBoldStyle);
		RtfCell columnCell = new RtfCell(column);
		columnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(columnCell);
	    }

	    // Values
	    Paragraph value;
	    for (int row = 0; row < result.getRowCount(); row++) {
		for (int column = 0; column < columnCount; column++) {
		    if (result.getValueAt(row, column) != null) {
			value = new Paragraph(result.getValueAt(row, column)
				.toString(), cellBoldStyle);
		    } else {
			value = new Paragraph("");
		    }
		    RtfCell valueCell = new RtfCell(value);
		    valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		    table.addCell(valueCell);
		}
	    }
	    document.add(table);
	    document.add(Chunk.NEWLINE);
	    writeNumberOfFincas(document, result.getRowCount());
	    document.newPage();
	    document.add(image);

	    // Close file
	    document.close();

	} catch (DocumentException e) {
	    logger.error(e.getStackTrace(), e);
	}

    }

    private void writePdfReportContent(PdfWriter writer, Document document,
	    ResultTableModel result, QueryFiltersI filters) {
	try {
	    boolean isFirstPage = true;

	    document.setPageCount(1);
	    startNewReport = true;
	    if (!isFirstPage) {
		document.setHeader(null);
		document.setFooter(null);
		document.newPage();

	    }
	    // Write title,subtitle and date report
	    String title = result.getTitle();
	    String subtitle = result.getSubtitle();
	    writeTitleAndSubtitle(document, title, subtitle);

	    // Header
	    Image image = getHeaderImage();

	    PdfContentByte cbhead = writer.getDirectContent();
	    PdfTemplate tp = cbhead.createTemplate(image.getWidth(),
		    image.getHeight());
	    tp.addImage(image);

	    cbhead.addTemplate(tp, 520, 775);

	    Phrase headPhrase = new Phrase(title + " - " + getDateFormated()
		    + writeFiltersInHeader(filters), bodyBoldStyle);
	    Phrase footerPhrase = new Phrase("Página: ", bodyBoldStyle);

	    HeaderFooter header = new HeaderFooter(headPhrase, false);
	    HeaderFooter footer = new HeaderFooter(footerPhrase, true);
	    footer.setBorder(Rectangle.NO_BORDER);

	    document.setHeader(header);
	    document.setFooter(footer);

	    writeDate(document);
	    document.add(Chunk.NEWLINE);

	    // write filters
	    writeFilters(document, filters);

	    int columnCount = result.getColumnCount();
	    PdfPTable table = new PdfPTable(columnCount);
	    table.setTotalWidth(document.getPageSize().getWidth()
		    - document.leftMargin() - document.rightMargin());
	    table.setLockedWidth(true);
	    if (!title.equalsIgnoreCase("listado de pagos")) {
		table.setWidths(getColumnsWidth(table, columnCount,
			TITULAR_COLUMN_WIDTH));
	    } else {
		table.setWidths(getColumnsWidth(table, columnCount,
			PAGOS_COLUMN_WIDTH));
	    }

	    String[] headerCells = new String[columnCount];

	    // Column names
	    for (int i = 0; i < columnCount; i++) {
		String columnName = result.getColumnName(i);
		if (result.getQueryFilters().getFields() != null) {
		    List<Field> list = result.getQueryFilters().getFields();
		    columnName = list.get(i).getLongName();
		}
		Paragraph column = new Paragraph(columnName, bodyBoldStyle);
		PdfPCell columnCell = new PdfPCell(column);
		columnCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		table.addCell(columnCell);
		headerCells[i] = columnName;
	    }

	    startNewReport = false;
	    tableHeader = headerCells;

	    // Values
	    Paragraph value;
	    String valueFormatted;
	    for (int row = 0; row < result.getRowCount(); row++) {
		for (int column = 0; column < columnCount; column++) {
		    if (result.getValueAt(row, column) != null) {
			if (result.getValueAt(row, column).getClass().getName()
				.equalsIgnoreCase("java.lang.Integer")) {
			    valueFormatted = intFormatter.format(result
				    .getValueAt(row, column));
			    value = new Paragraph(valueFormatted, cellBoldStyle);
			} else if (result.getValueAt(row, column).getClass()
				.getName().equalsIgnoreCase("java.lang.Double")) {
			    valueFormatted = doubleFormatter.format(result
				    .getValueAt(row, column));
			    value = new Paragraph(valueFormatted, cellBoldStyle);
			} else if (result.getValueAt(row, column).getClass()
				.getName().equalsIgnoreCase("java.sql.Date")) {
			    valueFormatted = dateFormatter.format(result
				    .getValueAt(row, column));
			    value = new Paragraph(valueFormatted, cellBoldStyle);
			} else {
			    value = new Paragraph(result
				    .getValueAt(row, column).toString(),
				    cellBoldStyle);
			}
		    } else {
			value = new Paragraph("");
		    }
		    PdfPCell valueCell = new PdfPCell(value);
		    valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
		    table.addCell(valueCell);
		}
	    }
	    // table.setHorizontalAlignment(Element.ALIGN_LEFT);
	    document.add(table);
	    writeNumberOfFincas(document, result.getRowCount());
	    isFirstPage = false;

	    // Close file
	    document.close();

	} catch (DocumentException e) {
	    logger.error(e.getStackTrace(), e);
	}
    }

    public void writeRtfReport(String fileName, ResultTableModel resultsMap,
	    QueryFiltersI filters) {
	Document document = new Document();
	document.setPageSize(PageSize.A4.rotate());
	RtfWriter2 writer;
	try {
	    // Open RTF file and prepare it to write on
	    writer = RtfWriter2.getInstance(document, new FileOutputStream(
		    fileName));
	    document.open();
	    RtfDocumentSettings settings = writer.getDocumentSettings();
	    settings.setOutputTableRowDefinitionAfter(true);

	    // Write report into document
	    writeRtfReportContent(document, resultsMap, filters);

	    // Close file
	    document.close();

	} catch (FileNotFoundException e) {
	    logger.error(e.getStackTrace(), e);
	}
    }

    public void writePdfReport(String fileName, ResultTableModel resultsMap,
	    QueryFiltersI filters) {
	Document document = new Document();
	document.setPageSize(PageSize.A4.rotate());
	try {
	    PdfWriter writer = PdfWriter.getInstance(document,
		    new FileOutputStream(fileName));
	    writer.setPageEvent(new MyPageEvent(writer, document, resultsMap));
	    document.open();

	    // Write report into document
	    writePdfReportContent(writer, document, resultsMap, filters);
	    // Close file
	    document.close();

	} catch (FileNotFoundException e) {
	    logger.error(e.getStackTrace(), e);
	} catch (DocumentException e) {
	    logger.error(e.getStackTrace(), e);
	}
    }

    public class MyPageEvent extends PdfPageEventHelper {
	private final ResultTableModel resultMap;

	public MyPageEvent(PdfWriter pdfWriter, Document document,
		ResultTableModel resultsMap) {
	    this.resultMap = resultsMap;
	}

	@Override
	public void onStartPage(PdfWriter pdfWriter, Document document) {
	    try {
		if (!startNewReport) {
		    document.add(Chunk.NEWLINE);
		    if (tableHeader != null) {
			PdfPTable table = new PdfPTable(tableHeader.length);
			table.setTotalWidth(document.getPageSize().getWidth()
				- document.leftMargin()
				- document.rightMargin());
			table.setLockedWidth(true);

			if (!resultMap.getTitle().equalsIgnoreCase(
				"listado de pagos")) {
			    table.setWidths(getColumnsWidth(table,
				    tableHeader.length, TITULAR_COLUMN_WIDTH));
			} else {
			    table.setWidths(getColumnsWidth(table,
				    tableHeader.length, PAGOS_COLUMN_WIDTH));
			}

			for (int i = 0; i < tableHeader.length; i++) {
			    Paragraph column = new Paragraph(tableHeader[i],
				    bodyBoldStyle);
			    PdfPCell columnCell = new PdfPCell(column);
			    columnCell
				    .setHorizontalAlignment(Element.ALIGN_CENTER);
			    table.addCell(columnCell);
			}
			document.add(table);
		    }
		}
	    } catch (DocumentException e1) {
		logger.error(e1.getMessage(), e1);
	    }
	}
    }

    public void onEndPage(PdfWriter pdfWriter, Document document) {
	// you do what you want here
    }
}
