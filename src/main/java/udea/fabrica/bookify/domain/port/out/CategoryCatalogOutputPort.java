package udea.fabrica.bookify.domain.port.out;

public interface CategoryCatalogOutputPort {
    boolean existsCategoryByName(String categoryName);
}
