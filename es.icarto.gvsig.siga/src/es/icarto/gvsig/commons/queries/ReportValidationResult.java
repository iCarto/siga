package es.icarto.gvsig.commons.queries;

import es.icarto.gvsig.commons.utils.StrUtils;

public class ReportValidationResult {
    
    private final String msg;

    public ReportValidationResult(String msg) {
        this.msg = msg;
    }
    
    public boolean isError() {
        return !StrUtils.isEmptyString(msg);
    }
    
    public String getMsg( ) {
        return this.msg;
    }

}
