package es.icarto.gvsig.extgia.consultas;

public enum QueryType {

    TYPE_NOT_SET(""),
    TRABAJOS("Listado de Trabajos"),
    RECONOCIMIENTOS("Listado de Reconocimientos de Estado"),
    TRABAJOS_VEGETACION("Listado de Trabajos"),
    TRABAJOS_FIRME("Listado de Trabajos"),
    CARACTERISTICAS("Listado de Caracter�sticas"),
    TRABAJOS_AGREGADOS("Listado de Trabajos");

    private final String title;

    QueryType(String title) {
	this.title = title;
    }
    
    public String title() {
	return title;
    }

}
