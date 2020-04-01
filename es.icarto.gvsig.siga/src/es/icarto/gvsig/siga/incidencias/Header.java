package es.icarto.gvsig.siga.incidencias;

import java.util.HashMap;
import java.util.Map;

public enum Header {

    PK("P.K.", 10),
    SENTIDO("Sentido", 15),
    FECHA("Fecha", 15),
    HORA("Hora", 10),
    MOTIVO("Motivo", 25),
    DURACION("Duración", 25),
    ASISTENCIAS_MOVILIZADAS("Asistencias Movilizadas", 250),
    CLIMA("Clima", 25),
    CAUSAS("Causas", 100),
    N_VEHICULOS("nº Vehículos", 10),
    TOTAL_VEHICULOS("Total Vehículos", 10),
    HERIDOS("Heridos", 10),
    HERIDOS_LEVES("Heridos Leves", 10),
    HERIDOS_GRAVES("Heridos Graves", 10),
    MUERTOS("Muertos", 10),
    ASIST_SANITARIA("Asistencia Sanitaria", 5),
    INFORME_ARENA("Informe Arena", 5),
    INFORME_GCT("Informe GCT", 5);

    private static final Map<String, Header> stringToEnum = new HashMap<String, Header>();

    static {
        for (Header op : values()) {
            stringToEnum.put(op.toString().toUpperCase(), op);
        }
    }

    private final String text;
    private int idx = -1;
    private final int fieldLength;

    private Header(String text, int fieldLength) {
        this.text = text;
        this.fieldLength = fieldLength;
    }

    public void setIdx(int idx) {
        this.idx = idx;
    }

    public int getIdx() {
        return idx;
    }

    public int getFieldLength() {
        return this.fieldLength;
    }

    @Override
    public String toString() {
        return this.text;
    }

    /**
     * Returns Header for string, or null if string is invalid
     *
     */
    public static Header fromString(String text) {
        return stringToEnum.get(text.toUpperCase());
    }

}
