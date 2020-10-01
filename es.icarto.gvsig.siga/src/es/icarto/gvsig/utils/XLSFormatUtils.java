package es.icarto.gvsig.utils;

import java.util.Date;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Workbook;

import es.udc.cartolab.gvsig.navtable.format.DateFormatNT;
import es.udc.cartolab.gvsig.navtable.format.DoubleFormatNT;

public class XLSFormatUtils {
    
    private static final DataFormatter dataFormatter = new DataFormatter();
    private static final String PATTERN = DateFormatNT.getDateFormat()
            .toPattern();
    
    public XLSFormatUtils() {
	throw new AssertionError("Non instantiable class");
    }
    
    public static String getValueAsString(Cell cell) {
	if (cell == null) {
	    return "";
	}
	switch (cell.getCellType()) {
	case Cell.CELL_TYPE_STRING:
	    return cell.getRichStringCellValue().getString();
	case Cell.CELL_TYPE_NUMERIC:
	    if (DateUtil.isCellDateFormatted(cell)) {
		// Date date = cell.getDateCellValue();
		// return DateFormatNT.getDateFormat().format(date);
		return dataFormatter.formatCellValue(cell);
	    } else {
		double numericCellValue = cell.getNumericCellValue();
		return DoubleFormatNT.getEditingFormat().format(
			numericCellValue);
	    }
	case Cell.CELL_TYPE_BOOLEAN:
	    return cell.getBooleanCellValue() ? "Sí" : "No";
	case Cell.CELL_TYPE_FORMULA:
	    return cell.getCellFormula();
	case Cell.CELL_TYPE_BLANK:
	    return "";
	default:
	    return "";
	}
	}
	
	public static void copyCellValue(Workbook wb, Cell inCell, Cell outCell) {
	switch (inCell.getCellType()) {
    case Cell.CELL_TYPE_STRING:
        outCell.setCellValue(inCell.getStringCellValue());
        break;
    case Cell.CELL_TYPE_NUMERIC:
        if (DateUtil.isCellDateFormatted(inCell)) {
            
            CellStyle cellStyle = wb.createCellStyle();
            CreationHelper creationHelper = wb.getCreationHelper();
            cellStyle.setAlignment(CellStyle.ALIGN_RIGHT);
            cellStyle.setDataFormat(creationHelper.createDataFormat()
                .getFormat(PATTERN));
            wb.getSheetAt(0).setDefaultColumnStyle(outCell.getColumnIndex(), cellStyle);
            outCell.setCellValue(dataFormatter.formatCellValue(inCell));
        }else {
            outCell.setCellValue(inCell.getNumericCellValue());
        }
        break;
    case Cell.CELL_TYPE_BOOLEAN:
        outCell.setCellValue(inCell.getBooleanCellValue());
        break;
    case Cell.CELL_TYPE_FORMULA:
        outCell.setCellValue(inCell.getCellFormula());
        break;
    case Cell.CELL_TYPE_BLANK:
        break;
	}
    }

}
