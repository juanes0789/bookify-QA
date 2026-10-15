package udea.fabrica.bookify.infrastructure.adapter.output.persistence.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "tbl_dia_no_laborable",
    uniqueConstraints = @UniqueConstraint(
        name = "uq_dia_no_laborable_proveedor_fecha",
        columnNames = {"id_proveedor", "fecha"}
    )
)
@Getter
@Setter
public class NonWorkingDayEntity {

    @Id
    @Column(name = "id_dia_no_laborable")
    private UUID id;

    @Column(name = "id", nullable = false)
    private UUID legacyId;

    @Column(name = "id_proveedor", nullable = false)
    private UUID proveedorId;

    @Column(name = "proveedor_id", nullable = false)
    private UUID legacyProveedorId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha;

    @Column(name = "motivo", length = 100)
    private String motivo;

    @Column(name = "fecha_creacion", nullable = false, updatable = false)
    private OffsetDateTime fechaCreacion;
}
