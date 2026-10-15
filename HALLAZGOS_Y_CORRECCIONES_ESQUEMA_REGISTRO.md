# Reporte General de Hallazgos y Correcciones de Esquema de Base de Datos

Este documento consolida la auditoría técnica completa del mapeo entre el código fuente (Entidades JPA y consultas nativas SQL) y el esquema físico real de PostgreSQL en Supabase. Incluye tanto las correcciones aplicadas exitosamente para la Historia de Usuario de **Registro de Clientes y Proveedores**, como los hallazgos críticos detectados en los módulos de **Disponibilidad** e **Historial de Reservas**.

---

## 1. Convención General del Esquema PostgreSQL

A través de la inspección directa del catálogo `information_schema.columns` en Supabase, se constató el estándar arquitectónico de la base de datos:

> **Todas las tablas usan la convención `id_<nombre_tabla>` tanto para sus claves primarias (PK) como para sus claves foráneas (FK)** (por ejemplo: `id_usuario`, `id_disponibilidad`, `id_reserva`, `id_cliente`), y **no** usan `id` ni `nombre_id`.

Las discrepancias detectadas se originan cuando las entidades JPA o consultas nativas asumen la convención por defecto de Hibernate (`id` o `entidad_id`), lo que no genera errores de compilación ni en pruebas unitarias con Mocks, pero produce excepciones `column does not exist` en tiempo de ejecución.

---

## 2. Estado de Módulos y Entidades en el Proyecto

| Módulo / Historia de Usuario | Archivo / Componente | Estado frente a PostgreSQL |
|---|---|:---:|
| **HU Registro de Usuarios** | [UserEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/UserEntity.java)<br>[UsuarioRolEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/UsuarioRolEntity.java)<br>[RolEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/RolEntity.java)<br>[EstadoUsuarioEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/EstadoUsuarioEntity.java) | ✅ **Corregido y validado 100% en Postman** |
| **HU Búsqueda de Servicios** | [SearchServicesJpaRepository.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/repository/SearchServicesJpaRepository.java)<br>[TipoProveedorJpaRepository.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/repository/TipoProveedorJpaRepository.java) | ✅ **100% Correcto y alineado con la BD** |
| **HU Días No Laborables** | [AgendaServicioEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/AgendaServicioEntity.java)<br>[NonWorkingDayEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/NonWorkingDayEntity.java) | ✅ **Funcional** (contiene campos legacy en entidad) |
| **HU Disponibilidad** | [AvailabilityEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/AvailabilityEntity.java)<br>[EstadoDisponibilidadEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/EstadoDisponibilidadEntity.java) | ⚠️ **Requiere corrección de nombres de columna** |
| **HU Historial de Reservas** | [BookingHistoryJpaRepository.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/repository/BookingHistoryJpaRepository.java) | ⚠️ **Requiere corrección de JOINs en SQL nativo** |

---

## 3. Sección Aplicada: HU Registro de Usuarios (Validada)

### Resumen del Diagnóstico
Durante la validación inicial de `POST /api/v1/auth/register`, la consulta `existsByCorreoIgnoreCase` disparó:
```text
JDBC exception executing SQL [ERROR: column ue1_0.id does not exist Position: 8] 
[select ue1_0.id from tbl_usuario ue1_0 where upper(ue1_0.correo)=upper(?) fetch first ? rows only]
```

### Correcciones Aplicadas

1. **`UserEntity.java`:**
   * `@Id @GeneratedValue(strategy = GenerationType.UUID) @Column(name = "id_usuario") private UUID id;`
   * `@Column(name = "id_estado_usuario", nullable = false) private Integer estadoUsuarioId;`
   * `@Column(name = "id_tipo_identificacion") private Integer tipoIdentificacionId;`

2. **`UsuarioRolEntity.java` (en `UsuarioRolId`):**
   * `@Column(name = "id_usuario", nullable = false) private UUID usuarioId;`
   * `@Column(name = "id_rol", nullable = false) private Integer rolId;`

3. **`EstadoUsuarioEntity.java`:**
   * `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_estado_usuario") private Integer id;`

4. **`RolEntity.java`:**
   * `@Id @GeneratedValue(strategy = GenerationType.IDENTITY) @Column(name = "id_rol") private Integer id;`

**Resultado:** Las peticiones en Postman responden exitosamente `HTTP 201 Created` y los registros se crean en `tbl_usuario` y `tbl_usuario_rol`.

---

## 4. Hallazgos Pendientes por Corregir en Otros Módulos

### Hallazgo 1: `AvailabilityEntity.java` (`tbl_disponibilidad`)
* **Archivo:** [AvailabilityEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/AvailabilityEntity.java) (Líneas 16–25)
* **Diagnóstico:** Asume nombres de columnas por defecto (`id`, `agenda_servicio_id`, `estado_disponibilidad_id`). Al invocar `AvailabilityJpaRepository.save()`, Hibernate generará un `INSERT` con columnas inexistentes, arrojando error `column ... does not exist`.

#### Tabla Comparativa
| Atributo Java | Mapeo Actual en JPA | Columna Real en PostgreSQL | Estado |
|---|---|---|:---:|
| `private UUID id;` | `@Id` (asume `id`) | **`id_disponibilidad`** | ❌ Error |
| `private UUID agendaServicioId;` | `@Column(name = "agenda_servicio_id")` | **`id_agenda_servicio`** | ❌ Error |
| `private Integer estadoDisponibilidadId;` | `@Column(name = "estado_disponibilidad_id")` | **`id_estado_disponibilidad`** | ❌ Error |
| `private UUID creadoPorUsuarioId;` | `@Column(name = "creado_por_usuario_id")` | `creado_por_usuario_id` / `id_usuario_creador` | ℹ️ Ambos existen en la tabla |

#### Instrucción de Corrección
En `AvailabilityEntity.java`, actualizar las anotaciones de columnas:
```java
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id_disponibilidad") // <-- Corrección
    private UUID id;

    @Column(name = "id_agenda_servicio", nullable = false) // <-- Corrección
    private UUID agendaServicioId;

    @Column(name = "id_estado_disponibilidad", nullable = false) // <-- Corrección
    private Integer estadoDisponibilidadId;
```

---

### Hallazgo 2: `EstadoDisponibilidadEntity.java` (`tbl_estado_disponibilidad`)
* **Archivo:** [EstadoDisponibilidadEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/EstadoDisponibilidadEntity.java) (Línea 12)
* **Diagnóstico:** La entidad no declara el nombre de columna de la clave primaria. En PostgreSQL la columna es `id_estado_disponibilidad`. Al ejecutar `estadoDisponibilidadRepository.findByCodigo(...)` o cualquier consulta derivada, Hibernate proyectará `ede.id`, fallando en base de datos.

#### Tabla Comparativa
| Atributo Java | Mapeo Actual en JPA | Columna Real en PostgreSQL | Estado |
|---|---|---|:---:|
| `private Integer id;` | `@Id` (asume `id`) | **`id_estado_disponibilidad`** | ❌ Error |

#### Instrucción de Corrección
En `EstadoDisponibilidadEntity.java`:
```java
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_estado_disponibilidad") // <-- Corrección
    private Integer id;
```

---

### Hallazgo 3: `BookingHistoryJpaRepository.java` (Consulta SQL Nativa Rota)
* **Archivo:** [BookingHistoryJpaRepository.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/repository/BookingHistoryJpaRepository.java) (Líneas 13–29)
* **Diagnóstico:** La consulta SQL nativa `@Query(..., nativeQuery = true)` fue escrita usando nombres desactualizados (`id`, `tabla_id`, `r.cliente_id`). Al ejecutarse en vivo, fallará en prácticamente todos los `JOIN` y en el `WHERE`.

#### Comparativa de Cláusulas SQL

| Cláusula Actual en el Repositorio | Equivalente Correcto en PostgreSQL |
|---|---|
| `INNER JOIN tbl_estado_reserva er ON er.id = r.estado_reserva_id` | `INNER JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva` |
| `INNER JOIN tbl_ocupacion_disponibilidad od ON od.reserva_id = r.id` | `INNER JOIN tbl_ocupacion_disponibilidad od ON od.id_reserva = r.id_reserva` |
| `INNER JOIN tbl_disponibilidad d ON d.id = od.disponibilidad_id` | `INNER JOIN tbl_disponibilidad d ON d.id_disponibilidad = od.id_disponibilidad` |
| `INNER JOIN tbl_agenda_servicio ag ON ag.id = d.agenda_servicio_id` | `INNER JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio` |
| `INNER JOIN tbl_servicio_sede ss ON ss.id = ag.servicio_sede_id` | `INNER JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede` |
| `INNER JOIN tbl_servicio s ON s.id = ss.servicio_id` | `INNER JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio` |
| `INNER JOIN tbl_proveedor p ON p.id = s.proveedor_id` | `INNER JOIN tbl_proveedor p ON p.id_proveedor = s.id_proveedor` |
| `WHERE r.cliente_id = :clienteId` | `WHERE r.id_cliente = :clienteId` |

#### Instrucción de Corrección
En `BookingHistoryJpaRepository.java`, sustituir el bloque `@Query` por la siguiente versión alineada al esquema:

```java
    @Query(value = """
            SELECT
                s.nombre AS service,
                p.nombre_comercial AS provider,
                d.inicio_at AS fechaHora,
                er.codigo AS estado
            FROM tbl_reserva r
            INNER JOIN tbl_estado_reserva er ON er.id_estado_reserva = r.id_estado_reserva
            INNER JOIN tbl_ocupacion_disponibilidad od ON od.id_reserva = r.id_reserva
            INNER JOIN tbl_disponibilidad d ON d.id_disponibilidad = od.id_disponibilidad
            INNER JOIN tbl_agenda_servicio ag ON ag.id_agenda_servicio = d.id_agenda_servicio
            INNER JOIN tbl_servicio_sede ss ON ss.id_servicio_sede = ag.id_servicio_sede
            INNER JOIN tbl_servicio s ON s.id_servicio = ss.id_servicio
            INNER JOIN tbl_proveedor p ON p.id_proveedor = s.id_proveedor
            WHERE r.id_cliente = :clienteId
            ORDER BY d.inicio_at DESC
            """, nativeQuery = true)
    List<BookingHistoryProjection> findBookingHistoryByClientId(@Param("clienteId") UUID clienteId);
```

---

### Hallazgo 4: `NonWorkingDayEntity.java` (Campos Duplicados Legacy)
* **Archivo:** [NonWorkingDayEntity.java](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/src/main/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/entity/NonWorkingDayEntity.java) (Líneas 23–35)
* **Diagnóstico:** Esta entidad posee un mapeo doble para compatibilidad:
  * `id` mapeado a `id_dia_no_laborable` y `legacyId` mapeado a `id`.
  * `proveedorId` mapeado a `id_proveedor` y `legacyProveedorId` mapeado a `proveedor_id`.
* En la tabla física `tbl_dia_no_laborable` existen actualmente ambas columnas, por lo que **no falla en ejecución**, pero representa deuda técnica que debe ser limpiada cuando se homologue el esquema de base de datos.

---

## 5. Resumen de Impacto en Pruebas Automatizadas

Al igual que ocurrió con la HU de Registro, dado que los nombres de los atributos Java (`id`, `agendaServicioId`, `estadoDisponibilidadId`) y los getters/setters no cambian:
* **Ninguna prueba unitaria existente se verá afectada.**
* Las pruebas unitarias seguirán pasando al 100%.
* Al aplicar estas correcciones, los endpoints de creación de disponibilidad y consulta de historial de reservas funcionarán de forma transparente en Postman contra la base de datos real en Supabase.
