package udea.fabrica.bookify.domain.model;

public class SearchServiceFilter {
    private String texto;
    private String categoria;

    public SearchServiceFilter(String texto, String categoria) {
        this.texto = texto;
        this.categoria = categoria;
    }

    public String getTexto() {
        return texto;
    }

    public String getCategoria() {
        return categoria;
    }
}
