package udea.fabrica.bookify.infrastructure.adapter.output.persistence.repository;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity.AvailabilityEntity;

import java.util.UUID;

public interface TipoProveedorJpaRepository extends Repository<AvailabilityEntity, UUID> {

    @Query(value = """
            SELECT EXISTS (
                SELECT 1
                FROM tbl_tipo_proveedor tp
                WHERE unaccent(lower(tp.nombre)) = unaccent(lower(:nombre))
            )
            """, nativeQuery = true)
    boolean existsByNombreIgnoreCase(@Param("nombre") String nombre);
}
