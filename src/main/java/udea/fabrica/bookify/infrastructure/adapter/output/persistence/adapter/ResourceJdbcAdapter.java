package udea.fabrica.bookify.infrastructure.adapter.output.persistence.adapter;

import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import udea.fabrica.bookify.domain.exception.DomainConflictException;
import udea.fabrica.bookify.domain.model.Resource;
import udea.fabrica.bookify.domain.port.out.ResourceRepositoryPort;

@Repository
public class ResourceJdbcAdapter implements ResourceRepositoryPort {

    private static final String ACTIVE_STATUS = "ACTIVO";

    private final JdbcTemplate jdbc;

    public ResourceJdbcAdapter(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @Override
    public boolean existsByMerchantProfileIdAndNameIgnoreCase(UUID merchantProfileId, String name) {
        Boolean exists = jdbc.queryForObject(
                "SELECT EXISTS (SELECT 1 FROM tbl_recurso WHERE id_proveedor = ? AND LOWER(nombre) = LOWER(?))",
                Boolean.class, merchantProfileId, name);
        return Boolean.TRUE.equals(exists);
    }

    @Override
    public Resource save(Resource resource) {
        Integer typeId = findId(
                "SELECT id_tipo_recurso FROM tbl_tipo_recurso WHERE codigo = ?",
                resource.type(),
                "Tipo de recurso no configurado en la base de datos");
        Integer statusId = findId(
                "SELECT id_estado_recurso FROM tbl_estado_recurso WHERE codigo = ?",
                ACTIVE_STATUS,
                "Estado ACTIVO no configurado en la base de datos");

        UUID id = UUID.randomUUID();
        try {
            jdbc.update("""
                    INSERT INTO tbl_recurso
                        (id_recurso, id_proveedor, id_tipo_recurso, id_estado_recurso, nombre,
                         fecha_creacion, fecha_actualizacion)
                    VALUES (?, ?, ?, ?, ?, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
                    """,
                    id, resource.providerId(), typeId, statusId, resource.name());
        } catch (DataIntegrityViolationException ex) {
            throw new DomainConflictException("Ya existe un recurso con ese nombre en tu comercio");
        }

        return Resource.builder()
                .id(id)
                .providerId(resource.providerId())
                .type(resource.type())
                .name(resource.name())
                .status(ACTIVE_STATUS)
                .build();
    }

    private Integer findId(String sql, String code, String errorMessage) {
        List<Integer> ids = jdbc.queryForList(sql, Integer.class, code);
        if (ids.isEmpty()) {
            throw new IllegalStateException(errorMessage);
        }
        return ids.get(0);
    }
}