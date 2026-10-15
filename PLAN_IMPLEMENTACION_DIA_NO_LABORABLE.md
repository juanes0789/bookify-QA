# Plan de Implementación: Registro y Gestión de Días No Laborables (Comercio / Proveedor)

## 1. Información General y Alcance de la Historia de Usuario (HU)

### Definición de la HU
> **Como** proveedor,  
> **quiero** marcar una fecha como día no laborable para todo mi comercio,  
> **para** evitar que los clientes realicen nuevas reservas ese día.

### Reglas de Negocio
1. **Alcance Global del Comercio:**
   * El día no laborable aplica a **todo el comercio (proveedor)**: cierra la agenda de todos sus recursos asociados (recursos/profesionales/servicios del proveedor).
2. **Ocultamiento de Horarios (No eliminación destructiva):**
   * Los horarios existentes en esa fecha **se ocultan, no se borran**. 
   * En la consulta de servicios disponibles (catálogo para clientes), no se debe devolver ningún horario de ninguno de los recursos del proveedor para la fecha marcada como no laborable.
3. **Zona Horaria y Validación Temporal:**
   * Todas las fechas se interpretan y calculan en la zona horaria **`America/Bogota`** (`ZoneId.of("America/Bogota")`).
   * **Rechazo de fechas pasadas:** La fecha ingresada debe ser estrictamente igual o posterior a la fecha actual ("hoy" en hora de Colombia). Ante fechas anteriores, responder con código HTTP **`400 Bad Request`** y el mensaje exacto: `"La fecha debe ser igual o posterior a hoy"`.
4. **Motivo:**
   * El motivo es opcional.
   * Longitud máxima permitida: **100 caracteres**. Si excede 100 caracteres, rechazar con HTTP **`400 Bad Request`**.
5. **Unicidad del Día No Laborable:**
   * No se puede registrar la misma fecha dos veces para el mismo comercio/proveedor. Ante duplicidad, responder con código HTTP **`409 Conflict`** y el mensaje exacto: `"La fecha ya está registrada como no laborable"`.
6. **Protección contra Creación de Horarios:**
   * Al intentar registrar un horario disponible (`POST /api/v1/disponibilidades`), si la fecha cae en un día no laborable para el comercio asociado a la agenda/recurso, el sistema responde con código HTTP **`409 Conflict`** y el mensaje exacto: `"La fecha está marcada como no laborable"`.
7. **Protección contra Creación de Reservas:**
   * Al intentar realizar una reserva sobre un horario previamente registrado en esa fecha, el sistema responde con código HTTP **`409 Conflict`** y el mensaje exacto: `"La fecha está marcada como no laborable"`.

---

## 2. Escenarios de Aceptación (Gherkin)

```gherkin
Scenario: Registrar un día no laborable sin reservas
  Given que mis recursos tienen horarios disponibles el "25/12/2026"
  And no tengo reservas confirmadas en esa fecha
  When registro el "25/12/2026" como día no laborable con el motivo "Navidad"
  Then el sistema responde con código 201
  And la consulta de servicios no devuelve horarios de ninguno de mis recursos para esa fecha

Scenario: Rechazar horarios nuevos en un día no laborable
  Given que el "25/12/2026" está registrado como día no laborable
  When registro un horario disponible para el recurso "Barbero Carlos" en esa fecha
  Then el sistema responde con código 409
  And el mensaje "La fecha está marcada como no laborable"

Scenario: Rechazar reservas nuevas en un día no laborable
  Given que el "25/12/2026" está registrado como día no laborable
  And existe un horario de "Barbero Carlos" registrado antes para esa fecha
  When un cliente intenta reservar ese horario
  Then el sistema responde con código 409
  And el mensaje "La fecha está marcada como no laborable"

Scenario: Rechazar una fecha pasada
  Given que hoy es "29/09/2026"
  When registro el "20/09/2026" como día no laborable
  Then el sistema responde con código 400
  And el mensaje "La fecha debe ser igual o posterior a hoy"

Scenario: Rechazar una fecha ya registrada
  Given que el "25/12/2026" ya está registrado como día no laborable
  When lo registro de nuevo
  Then el sistema responde con código 409
  And el mensaje "La fecha ya está registrada como no laborable"
```

---

## 3. Análisis del Estado Actual del Proyecto y Mapeo contra BD

### Estado Actual del Código Base
1. **Catálogo de Servicios (`GetServiceCatalogService`, `AvailabilityJpaRepository.findAvailableServiceCatalog`):**
   * Actualmente consulta `tbl_servicio`, `tbl_proveedor`, `tbl_servicio_sede`, `tbl_agenda_servicio`, `tbl_disponibilidad` y filtra por reservas confirmadas.
   * **Requisito HU:** Filtrar con `AND NOT EXISTS (SELECT 1 FROM tbl_dia_no_laborable dnl WHERE dnl.id_proveedor = p.id_proveedor AND dnl.fecha = CAST(d.inicio_at AT TIME ZONE 'America/Bogota' AS date))` para ocultar los horarios sin borrarlos.
2. **Creación de Disponibilidad (`CreateAvailabilityService`):**
   * Valida solapamiento y que la agenda esté activa.
   * **Requisito HU:** Debe inyectar la validación contra días no laborables del comercio al que pertenece la agenda de servicio. Si coincide, lanzar excepción de negocio mapeada a HTTP 409: `"La fecha está marcada como no laborable"`.
3. **Módulo de Reservas (`BookingService` o use cases futuros):**
   * Al procesar la reserva de una disponibilidad, debe verificar que la fecha no corresponda a un día no laborable del proveedor asociado, respondiendo con HTTP 409: `"La fecha está marcada como no laborable"`.

### Estructura de Tablas en Base de Datos
* **`tbl_proveedor`:** Entidad comercial (ID UUID, `nombre_comercial`, etc.).
* **`tbl_dia_no_laborable` (tabla ya existente en Supabase):**
  * `id_dia_no_laborable`: `uuid NOT NULL DEFAULT gen_random_uuid()` (Primary Key, convención reciente de nombres).
  * `id_proveedor`: `uuid NOT NULL` (Foreign Key a `tbl_proveedor(id_proveedor)`).
  * `fecha`: `date NOT NULL`.
  * `motivo`: `varchar(100) NULL`.
  * `fecha_creacion`: `timestamp with time zone NOT NULL DEFAULT now()`.
  * `CONSTRAINT uq_dia_no_laborable_proveedor_fecha UNIQUE (id_proveedor, fecha)`.
  * Supabase conserva también las columnas antiguas `id` y `proveedor_id`, `NOT NULL` y sin default. El trigger `fn_validar_dia_no_laborable` no las sincroniza; la aplicación escribe ambas parejas de IDs.
* **Relaciones para validación de horarios:**
  * Relación confirmada en Supabase: `tbl_agenda_servicio.id_agenda_servicio` → `tbl_servicio_sede.id_servicio_sede` → `tbl_servicio.id_servicio`; de allí se obtiene `tbl_servicio.id_proveedor`.
  * Con `agendaServicioId` se resuelve `id_proveedor` para consultar `tbl_dia_no_laborable`.

---

## 4. Diseño en Arquitectura Hexagonal

```
src/main/java/udea/fabrica/bookify/
├── domain/                                  <-- Núcleo Puro (sin dependencias de frameworks)
│   ├── model/
│   │   └── NonWorkingDay.java               <-- Entidad del dominio (id, proveedorId, fecha, motivo, fechaCreacion)
│   ├── port/
│   │   ├── in/
│   │   │   ├── RegisterNonWorkingDayInputPort.java   <-- Caso de uso: Registrar día no laborable
│   │   │   └── ValidateNonWorkingDayInputPort.java   <-- Caso de uso / consulta: Verificar fecha no laborable
│   │   └── out/
│   │       └── NonWorkingDayOutputPort.java          <-- Puerto salida: guardar, verificar duplicidad, buscar por proveedor y fecha
│   ├── service/
│   │   └── NonWorkingDayService.java        <-- Implementación de reglas (Zona horaria Bogota, validación de fechas, etc.)
│   └── exception/
│       ├── NonWorkingDayConflictException.java       <-- HTTP 409 ("La fecha ya está registrada como no laborable" o "La fecha está marcada como no laborable")
│       └── PastDateException.java                   <-- HTTP 400 ("La fecha debe ser igual o posterior a hoy")
│
└── infrastructure/                          <-- Adaptadores Frameworks y Persistencia
    ├── adapter/
    │   ├── input/rest/
    │   │   ├── controller/
    │   │   │   └── NonWorkingDayController.java      <-- POST /api/v1/proveedores/{proveedorId}/dias-no-laborables
    │   │   ├── dto/
    │   │   │   ├── request/RegisterNonWorkingDayRequestDto.java
    │   │   │   └── response/NonWorkingDayResponseDto.java
    │   │   └── mapper/
    │   │       └── NonWorkingDayRestMapper.java
    │   └── output/persistence/
    │       ├── entity/
    │       │   └── NonWorkingDayEntity.java          <-- Mapeo @Entity @Table(name = "tbl_dia_no_laborable")
    │       ├── repository/
    │       │   └── NonWorkingDayJpaRepository.java   <-- Spring Data JPA
    │       ├── mapper/
    │       │   └── NonWorkingDayEntityMapper.java
    │       └── adapter/
    │           └── NonWorkingDayPersistenceAdapter.java <-- Implementa NonWorkingDayOutputPort
    └── config/
        ├── BeanConfiguration.java                   <-- Registro del Bean del servicio
        └── GlobalExceptionHandler.java               <-- Handlers HTTP 400 y 409
```

---

## 5. Fases de Implementación Paso a Paso

### Fase 1: Capa de Dominio (Modelos, Excepciones y Puertos)
1. **Crear Excepciones de Dominio:**
   * `NonWorkingDayConflictException`: Excepción de conflicto de negocio (para HTTP 409).
   * `PastDateException`: Excepción para fecha menor a hoy en Colombia (para HTTP 400).
2. **Crear Modelo de Dominio `NonWorkingDay.java`:**
   * Atributos: `UUID id`, `UUID proveedorId`, `LocalDate fecha`, `String motivo`, `OffsetDateTime fechaCreacion`.
3. **Definir Puertos:**
   * `RegisterNonWorkingDayInputPort`:
     ```java
     NonWorkingDay register(UUID proveedorId, LocalDate fecha, String motivo);
     ```
   * `NonWorkingDayOutputPort`:
     ```java
     NonWorkingDay save(NonWorkingDay nonWorkingDay);
     boolean existsByProveedorIdAndFecha(UUID proveedorId, LocalDate fecha);
     boolean isNonWorkingDay(UUID proveedorId, LocalDate fecha);
     UUID findProviderIdByAgendaServicioId(UUID agendaServicioId);
     ```

### Fase 2: Lógica de Dominio y Servicios
1. **Implementar `NonWorkingDayService.java`:**
   * Zona horaria constante: `ZoneId BOGOTA_ZONE = ZoneId.of("America/Bogota");`.
   * **Regla 1 (Validar motivo):** Si `motivo != null && motivo.trim().length() > 100`, lanzar `IllegalArgumentException("El motivo no puede exceder 100 caracteres")`.
   * **Regla 2 (Validar fecha no pasada):**
     * Obtener hoy en Colombia: `LocalDate hoy = LocalDate.now(BOGOTA_ZONE);`.
     * Si `fecha.isBefore(hoy)`, lanzar `PastDateException("La fecha debe ser igual o posterior a hoy")`.
   * **Regla 3 (Validar unicidad):**
     * Consultar `nonWorkingDayOutputPort.existsByProveedorIdAndFecha(proveedorId, fecha)`.
     * Si ya existe, lanzar `NonWorkingDayConflictException("La fecha ya está registrada como no laborable")`.
   * **Regla 4 (Guardar):**
     * Construir entidad `NonWorkingDay` con `fechaCreacion = OffsetDateTime.now(BOGOTA_ZONE)` y llamar a `save`.

2. **Integrar Protección en Creación de Horarios (`CreateAvailabilityService.java`):**
   * Inyectar `NonWorkingDayOutputPort` (o puerto de validación).
   * Antes de validar solapamiento, determinar el `proveedorId` de la agenda mediante `findProviderIdByAgendaServicioId(availability.getAgendaServicioId())`.
   * Convertir `availability.getInicioAt()` a `LocalDate` en zona Bogotá:
     ```java
     LocalDate fechaInicio = availability.getInicioAt().atZoneSameInstant(ZoneId.of("America/Bogota")).toLocalDate();
     ```
   * Si `nonWorkingDayOutputPort.isNonWorkingDay(proveedorId, fechaInicio)` es verdadero:
     Lanzar `NonWorkingDayConflictException("La fecha está marcada como no laborable")`.

### Fase 3: Capa de Persistencia e Infraestructura
1. **Entidad JPA `NonWorkingDayEntity.java`:**
   * `@Entity @Table(name = "tbl_dia_no_laborable", uniqueConstraints = @UniqueConstraint(name = "uq_dia_no_laborable_proveedor_fecha", columnNames = {"id_proveedor", "fecha"}))`.
   * Mapear PK `id_dia_no_laborable`, FK `id_proveedor`, `fecha`, `motivo` y `fecha_creacion`. Por compatibilidad, escribir también `id` con el UUID de la PK y `proveedor_id` con el UUID del proveedor.
2. **Repositorio `NonWorkingDayJpaRepository.java`:**
   * `boolean existsByProveedorIdAndFecha(UUID proveedorId, LocalDate fecha);`
   * Query para resolver el proveedor desde una agenda:
     ```java
     @Query("""
         SELECT ss.proveedorId FROM AgendaServicioEntity ag
         ...
     """)
     UUID findProviderIdByAgendaServicioId(UUID agendaServicioId);
     ```
3. **Actualización de Consulta en `AvailabilityJpaRepository.java`:**
   * En la consulta nativa `findAvailableServiceCatalog()`:
     Agregar condición de exclusión para ocultar sin borrar:
     ```sql
     AND NOT EXISTS (
         SELECT 1
         FROM tbl_dia_no_laborable dnl
         WHERE dnl.id_proveedor = p.id_proveedor
           AND dnl.fecha = CAST(d.inicio_at AT TIME ZONE 'America/Bogota' AS date)
     )
     ```
4. **Adaptador `NonWorkingDayPersistenceAdapter.java`:**
   * Implementa `NonWorkingDayOutputPort` delegando a las entidades y repositorios JPA.

### Fase 4: Adaptadores REST y Controladores
1. **DTOs:**
   * `RegisterNonWorkingDayRequestDto`:
     * `@NotNull @JsonFormat(pattern = "dd/MM/yyyy") private LocalDate fecha;` (soporte flexible para `dd/MM/yyyy` y formato ISO `yyyy-MM-dd`).
     * `@Size(max = 100, message = "El motivo no puede exceder 100 caracteres") private String motivo;`
   * `NonWorkingDayResponseDto`:
     * `UUID id`, `UUID proveedorId`, `LocalDate fecha`, `String motivo`, `OffsetDateTime fechaCreacion`.
2. **Controlador `NonWorkingDayController.java`:**
   * `@RestController @RequestMapping("/api/v1/proveedores/{proveedorId}/dias-no-laborables")`.
   * Endpoint `@PostMapping`: Recibe `@PathVariable UUID proveedorId` y `@Valid @RequestBody RegisterNonWorkingDayRequestDto`.
   * Retorna `ResponseEntity.status(HttpStatus.CREATED).body(responseDto)`.

### Fase 5: Manejo Global de Excepciones y Configuración
1. **Actualizar `GlobalExceptionHandler.java`:**
   * Captura de `PastDateException`: HTTP `400 Bad Request` con `Map.of("error", ex.getMessage(), "mensaje", ex.getMessage())`.
   * Captura de `NonWorkingDayConflictException`: HTTP `409 Conflict` con `Map.of("error", ex.getMessage(), "mensaje", ex.getMessage())`.
2. **Actualizar `BeanConfiguration.java`:**
   * Registrar `@Bean RegisterNonWorkingDayInputPort` inyectando `NonWorkingDayOutputPort`.

---

## 6. Plan de Pruebas Automatizadas

### 1. Pruebas Unitarias de Dominio (`NonWorkingDayServiceTest`)
* **Test 1:** Registro exitoso de fecha futura con motivo $\rightarrow$ Retorna objeto persistido con datos correctos.
* **Test 2:** Registro exitoso con motivo opcional nulo o vacío.
* **Test 3:** Rechazo de fecha anterior a hoy en zona `America/Bogota` $\rightarrow$ Lanza `PastDateException` con mensaje `"La fecha debe ser igual o posterior a hoy"`.
* **Test 4:** Rechazo de fecha hoy $\rightarrow$ Debe ser permitida ("igual o posterior a hoy").
* **Test 5:** Rechazo de fecha ya registrada para el comercio $\rightarrow$ Lanza `NonWorkingDayConflictException` con mensaje `"La fecha ya está registrada como no laborable"`.
* **Test 6:** Rechazo de motivo mayor a 100 caracteres $\rightarrow$ Lanza `IllegalArgumentException`.

### 2. Pruebas de Dominio en Creación de Horarios (`CreateAvailabilityServiceTest`)
* **Test:** Intentar crear un horario en una fecha marcada como día no laborable del proveedor $\rightarrow$ Lanza `NonWorkingDayConflictException` con mensaje `"La fecha está marcada como no laborable"`.

### 3. Pruebas de Catálogo / Ocultamiento (`ServiceCatalogPersistenceAdapterTest` o JPA)
* **Test:** Los horarios de la fecha no laborable no aparecen en el catálogo de servicios disponibles (permanecen en base de datos pero no se listan).

### 4. Pruebas de Integración Web (`NonWorkingDayControllerTest`)
* Pruebas aisladas del controlador con `MockMvcStandalone`:
  * `POST 201 Created` ante datos válidos.
  * `POST 400 Bad Request` ante fecha pasada con mensaje `"La fecha debe ser igual o posterior a hoy"`.
  * `POST 409 Conflict` ante fecha duplicada con mensaje `"La fecha ya está registrada como no laborable"`.

---

## 7. Matriz de Cobertura de Escenarios Gherkin

| Escenario Gherkin | Petición / Condición | Código HTTP | Mensaje / Validación Esperada |
| :--- | :--- | :---: | :--- |
| **Registrar día no laborable sin reservas** | `fecha`: "25/12/2026", `motivo`: "Navidad" | **201 Created** | Se registra en `tbl_dia_no_laborable`. El catálogo no devuelve horarios para ese proveedor en dicha fecha. |
| **Rechazar horarios nuevos en día no laborable** | `POST /api/v1/disponibilidades` para el "25/12/2026" | **409 Conflict** | `"La fecha está marcada como no laborable"` |
| **Rechazar reservas nuevas en día no laborable** | Intento de reserva de cliente en fecha no laborable | **409 Conflict** | `"La fecha está marcada como no laborable"` |
| **Rechazar fecha pasada** | `fecha`: "14/10/2026" siendo hoy "15/10/2026" | **400 Bad Request** | `"La fecha debe ser igual o posterior a hoy"` |
| **Rechazar fecha ya registrada** | Registro duplicado para el mismo proveedor en "25/12/2026" | **409 Conflict** | `"La fecha ya está registrada como no laborable"` |

## 8. Ejecución manual de aceptación

La guía paso a paso de Postman, con variables, IDs reales a consultar, cuerpos JSON y respuestas esperadas, está en [GUIA_POSTMAN_DIA_NO_LABORABLE.md](./GUIA_POSTMAN_DIA_NO_LABORABLE.md).

La guía verifica registro `201`, duplicado `409`, fecha pasada `400`, motivo demasiado largo `400`, bloqueo de disponibilidad `409`, ocultamiento en catálogo y consistencia de las columnas nuevas/antiguas en Supabase. El escenario de crear reserva queda pendiente porque ese endpoint/caso de uso aún no existe.



**Nota de ejecución:** Durante la prueba Postman se corrigió `AgendaServicioEntity` para mapear su PK a `tbl_agenda_servicio.id_agenda_servicio`. Reinicia la aplicación antes de repetir la prueba 6.
