package es.icarto.gvsig.extgia.consultas.caracteristicas.elements;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.swing.table.DefaultTableModel;

import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;

import es.icarto.gvsig.commons.utils.Field;
import es.icarto.gvsig.extgia.consultas.ConsultasFilters;
import es.icarto.gvsig.extgia.consultas.PDFReport;
import es.icarto.gvsig.extgia.consultas.QueryType;
import es.udc.cartolab.gvsig.users.utils.DBSession;

public class AreasServicioCaracteristicasReport extends PDFReport {

    public AreasServicioCaracteristicasReport(String[] element,
	    String fileName, DefaultTableModel tableModel,
	    ConsultasFilters<Field> filters, QueryType tipo) {
	super(element, fileName, tableModel, filters, tipo);
    }

    @Override
    protected Rectangle setPageSize() {
	return PageSize.A4.rotate();
    }

    @Override
    protected String[] getColumnNames() {
	String[] columnNames = { "ID �rea", "Nombre", "Tramo", "PK",
		"Puesta en Servicio", "Superficie Total", "Riego", "Cafeter�a",
		"Aparcamiento", "Area Picnic", "Fuentes Potables",
		"Observaciones" };
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
	columnsWidth[8] = 70f;
	columnsWidth[9] = 60f;
	columnsWidth[10] = 60f;
	columnsWidth[11] = 90f;
	// aditional column
	columnsWidth[12] = 60f;

	return columnsWidth;
    }

    @Override
    protected void writeDatesRange(Document document,
	    ConsultasFilters<Field> filters) {

    }

    @Override
    protected boolean hasEmbebedTable() {
	return true;
    }

    @Override
    protected PdfPCell writeAditionalColumnName() {
	PdfPCell aditionalCell = new PdfPCell(new Paragraph("N� Ramales",
		bodyBoldStyle));
	aditionalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	return aditionalCell;
    }

    @Override
    protected PdfPCell writeAditionalColumnValues(String id) {
	try {
	    Statement st = DBSession.getCurrentSession().getJavaConnection()
		    .createStatement();
	    String query = "SELECT count(id_ramal) FROM audasa_extgia.areas_servicio_ramales"
		    + " WHERE id_area_servicio = '" + id + "';";
	    ResultSet rs = st.executeQuery(query);
	    rs.next();
	    PdfPCell aditionalCell = new PdfPCell(new Paragraph(
		    rs.getString(1), cellBoldStyle));
	    aditionalCell.setHorizontalAlignment(Element.ALIGN_CENTER);
	    return aditionalCell;
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	return new PdfPCell();
    }

}
