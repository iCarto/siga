package es.icarto.gvsig.commons.queries;

import javax.swing.table.DefaultTableModel;

import com.iver.andami.PluginServices;

public class ReportValidation {

    // excel libs have a maximum number of rows allowed.
    // Also too many rows can provoke memory errors creating the pdf
    private static final int MAX_ROWS_ALLOWED = 65000;

    public ReportValidationResult afterGetResults(DefaultTableModel table) {
        String msg = "";
        final int rowCount = table.getRowCount();
        
        if (rowCount == 0) {
            msg = PluginServices.getText(this, "queryWithoutResults_msg");
        }

        if (rowCount > MAX_ROWS_ALLOWED) {
            msg = PluginServices.getText(this, "queryWithTooMuchResults_msg");
        }
        
        return new ReportValidationResult(msg);
    }

}
