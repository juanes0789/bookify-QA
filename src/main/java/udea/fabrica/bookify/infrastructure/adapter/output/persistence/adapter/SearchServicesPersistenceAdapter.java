package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import org.springframework.stereotype.Component;
import udea.fabrica.bookify.domain.model.ServiceSearchItem;
import udea.fabrica.bookify.domain.port.out.CategoryCatalogOutputPort;
import udea.fabrica.bookify.domain.port.out.SearchServicesOutputPort;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper.SearchServicesEntityMapper;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.SearchServicesJpaRepository;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository.TipoProveedorJpaRepository;

import java.util.List;

@Component
public class SearchServicesPersistenceAdapter implements SearchServicesOutputPort, CategoryCatalogOutputPort {

    private final SearchServicesJpaRepository searchServicesJpaRepository;
    private final TipoProveedorJpaRepository tipoProveedorJpaRepository;

    public SearchServicesPersistenceAdapter(SearchServicesJpaRepository searchServicesJpaRepository,
                                            TipoProveedorJpaRepository tipoProveedorJpaRepository) {
        this.searchServicesJpaRepository = searchServicesJpaRepository;
        this.tipoProveedorJpaRepository = tipoProveedorJpaRepository;
    }

    @Override
    public List<ServiceSearchItem> findAvailableServices(String texto, String categoria) {
        return SearchServicesEntityMapper.toDomain(
                searchServicesJpaRepository.findAvailableServices(texto, categoria)
        );
    }

    @Override
    public boolean existsCategoryByName(String categoryName) {
        return tipoProveedorJpaRepository.existsByNombreIgnoreCase(categoryName);
    }
}
