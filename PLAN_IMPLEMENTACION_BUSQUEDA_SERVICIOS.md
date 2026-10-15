# Plan de Implementación: Búsqueda de Servicios por Nombre o Categoría

Este documento define la especificación técnica y el plan de implementación para la Historia de Usuario (HU) de **Búsqueda de Servicios por Nombre o Categoría** en Bookify, asegurando el cumplimiento estricto de la Arquitectura Hexagonal y la integración con las reglas de negocio de catálogo (HU-02), perfil comercial (HU-09) y días no laborables (HU-10).

---

## 1. Información General y Alcance de la Historia de Usuario (HU)

### Definición de la HU
> **Como** cliente,  
> **quiero** buscar servicios por nombre o categoría,  
> **para** encontrar rápidamente lo que necesito sin revisar toda la oferta.

### Reglas de Negocio
1. **Coincidencia Parcial e Insensible a Mayúsculas y Tildes (Accent & Case Insensitive):**
   * La búsqueda debe ignorar tildes, diacríticos y diferencias entre mayúsculas y minúsculas (ej. el texto `"futbol"` debe coincidir con `"Fútbol 5"`, `"CORTE"` con `"Corte de cabello"`, `"barberia central"` con `"Barbería Central"`).
   * La búsqueda por texto evalúa coincidencia parcial en:
     * El nombre del servicio (`tbl_servicio.nombre`).
     * El nombre comercial del proveedor (`tbl_proveedor.nombre_comercial`).
2. **Filtro por Categoría Comercial (HU-09):**
   * La categoría corresponde al catálogo de perfiles comerciales (`tbl_tipo_proveedor.nombre`, ej. `"Barbería y belleza"`, `"Deportes y recreación"`).
   * Si se especifica una categoría que no existe en el catálogo registrado, el sistema debe rechazar la solicitud con código HTTP **`400 Bad Request`** indicando que la categoría es inválida.
3. **Validación de Criterios Obligatorios:**
   * La solicitud debe contener al menos un criterio de búsqueda: `texto` o `categoria`.
   * Si la búsqueda no incluye ni texto ni categoría (ambos ausentes o en blanco), responder con código HTTP **`400 Bad Request`** y un mensaje explicativo.
4. **Disponibilidad Activa y Exclusión de Días No Laborables (HU-02 & HU-10):**
   * Solo se retornan servicios que cuenten con **al menos un horario disponible** (`availableSchedules.size() > 0`).
   * Se excluyen automáticamente:
     * Horarios con reservas confirmadas (`tbl_ocupacion_disponibilidad` + `tbl_reserva.estado = 'CONFIRMADA'`).
     * Horarios pertenecientes a fechas marcadas como **días no laborables** para el comercio/proveedor (`tbl_dia_no_laborable`).
     * Servicios o agendas marcados como inactivos (`s.activo = false` o `ag.activo = false`).
5. **Estructura Enriquecida del Resultado:**
   * Cada servicio devuelto debe incluir:
     * `serviceId` y `service`: Identificador y nombre del servicio.
     * `provider`: Nombre comercial del comercio/proveedor.
     * `category`: Categoría comercial del proveedor.
     * `availableSchedules`: Lista de horarios disponibles, donde cada horario incluye obligatoriamente su **recurso asignado** (`recurso`, ej. `"Barbero Carlos"`).
6. **Respuesta ante Búsqueda sin Coincidencias:**
   * Si ningún servicio cumple los criterios pero la búsqueda fue válida, el sistema responde con código HTTP **`200 OK`**, una lista vacía de servicios y el mensaje: `"No se encontraron servicios"`.
7. **Seguridad y Acceso:**
   * Es un endpoint **público**: no exige token ni cabeceras de autenticación (`Authorization`).

---

## 2. Escenarios de Aceptación (Gherkin)

```gherkin
Scenario: Buscar servicios por nombre
  Given que existen los servicios publicados "Corte de cabello" y "Corte de barba"
  When busco el texto "CORTE"
  Then el sistema responde con código 200
  And devuelve solo los servicios cuyo nombre contiene "corte"
  And cada resultado incluye el servicio, el proveedor, la categoría y los horarios disponibles con su recurso

Scenario: Buscar servicios por proveedor
  Given que el proveedor "Barbería Central" tiene servicios publicados
  When busco el texto "barberia central"
  Then el sistema responde con código 200
  And devuelve los servicios de "Barbería Central"

Scenario: Buscar servicios por categoría
  Given que "Barbería Central" tiene la categoría "Barbería y belleza"
  And "Complejo El Estadio" tiene la categoría "Deportes y recreación"
  When filtro por la categoría "Deportes y recreación"
  Then el sistema responde con código 200
  And devuelve solo los servicios de "Complejo El Estadio"

Scenario: Combinar texto y categoría
  Given que existen servicios con "fútbol" en el nombre en comercios de varias categorías
  When busco el texto "futbol" en la categoría "Deportes y recreación"
  Then el sistema responde con código 200
  And devuelve solo los servicios que cumplen ambas condiciones

Scenario: Búsqueda sin resultados
  Given que ningún servicio ni proveedor coincide con "Yoga"
  When busco el texto "Yoga"
  Then el sistema responde con código 200 y una lista vacía
  And el mensaje "No se encontraron servicios"

Scenario: Rechazar una búsqueda inválida
  When busco sin texto ni categoría, o con una categoría fuera del catálogo
  Then el sistema responde con código 400
  And el mensaje indica el criterio inválido
```

---

## 3. Análisis de Mapeo contra el Schema de Base de Datos Real

### Estructura de Tablas Involucradas

1. **`tbl_servicio`:**
   * `id`: UUID (Primary Key).
   * `proveedor_id`: UUID (Foreign Key a `tbl_proveedor`).
   * `nombre`: varchar NOT NULL.
   * `activo`: boolean NOT NULL (debe ser `true`).

2. **`tbl_proveedor` y `tbl_tipo_proveedor` (Categoría HU-09):**
   * `tbl_proveedor.id`: UUID.
   * `tbl_proveedor.tipo_proveedor_id`: integer (Foreign Key a `tbl_tipo_proveedor.id`).
   * `tbl_proveedor.nombre_comercial`: varchar NOT NULL.
   * `tbl_proveedor.activo`: boolean NOT NULL (debe ser `true`).
   * `tbl_tipo_proveedor.id`: integer (Primary Key).
   * `tbl_tipo_proveedor.codigo`: varchar UNIQUE.
   * `tbl_tipo_proveedor.nombre`: varchar UNIQUE $\rightarrow$ **Categoría comercial** (ej. `"Barbería y belleza"`, `"Deportes y recreación"`).

3. **`tbl_recurso` y `tbl_agenda_recurso` (Recurso asignado):**
   * `tbl_recurso.id`: UUID (Primary Key).
   * `tbl_recurso.nombre`: varchar NOT NULL $\rightarrow$ Nombre del recurso (ej. `"Barbero Carlos"`).
   * `tbl_agenda_recurso.agenda_id`: UUID.
   * `tbl_agenda_recurso.recurso_id`: UUID $\rightarrow$ Vincula la agenda con el recurso específico.

4. **Cadena de Disponibilidad (`tbl_disponibilidad`, `tbl_agenda_servicio`, `tbl_servicio_sede`):**
   * `tbl_servicio_sede`: Vincula `servicio_id` con `sede_id`.
   * `tbl_agenda_servicio`: Vincula `agenda_id` con `servicio_sede_id`.
   * `tbl_disponibilidad`: Registra los bloques de tiempo (`inicio_at`, `fin_at`, `estado_disponibilidad_id`).

5. **Exclusión de Ocupaciones y Días No Laborables (HU-10):**
   * `tbl_ocupacion_disponibilidad` + `tbl_reserva`: Filtra horarios con reservas en estado `'CONFIRMAD%'`.
   * `tbl_dia_no_laborable`: Filtra horarios cuya fecha coincida con un día no laborable del proveedor en zona `America/Bogota`.

---

## 4. Diseño en Arquitectura Hexagonal

```
src/main/java/udea/fabrica/bookify/
├── domain/                                         <-- Núcleo Puro (Reglas de Negocio)
│   ├── model/
│   │   ├── SearchServiceFilter.java                <-- Criterios de búsqueda validados (texto, categoria)
│   │   ├── ServiceSearchItem.java                  <-- Servicio agrupado con proveedor y categoría
│   │   ├── AvailableScheduleWithResource.java      <-- Horario con id, inicioAt, finAt y recurso
│   │   └── SearchServicesResult.java               <-- Resultado: lista de servicios + mensaje
│   ├── port/
│   │   ├── in/
│   │   │   └── SearchServicesInputPort.java        <-- Caso de uso: buscar servicios
│   │   └── out/
│   │       ├── SearchServicesOutputPort.java       <-- Consulta enriquecida en persistencia
│   │       └── CategoryCatalogOutputPort.java      <-- Validación de categorías contra el catálogo
│   ├── service/
│   │   └── SearchServicesService.java              <-- Reglas de normalización, validaciones y agrupación
│   ├── util/
│   │   └── TextNormalizer.java                     <-- Normalización pura: remueve tildes y pasa a minúsculas
│   └── exception/
│       ├── InvalidSearchCriteriaException.java      <-- HTTP 400 (búsqueda sin parámetros)
│       └── CategoryNotFoundException.java          <-- HTTP 400 (categoría fuera del catálogo)
│
└── infrastructure/                                 <-- Adaptadores y Frameworks
    ├── adapter/
    │   ├── input/rest/
    │   │   ├── controller/
    │   │   │   └── SearchServicesController.java   <-- GET /api/v1/catalog/services/search
    │   │   ├── dto/
    │   │   │   └── response/
    │   │   │       ├── SearchServicesResponseDto.java          <-- Payload con mensaje y servicios
    │   │   │       ├── SearchedServiceOfferResponseDto.java    <-- Servicio con categoría
    │   │   │       └── ScheduleWithResourceResponseDto.java    <-- Horario con recurso
    │   │   └── mapper/
    │   │       └── SearchServicesRestMapper.java
    │   └── output/persistence/
    │       ├── repository/
    │       │   ├── SearchServicesProjection.java   <-- Proyección SQL nativa enriquecida
    │       │   ├── TipoProveedorJpaRepository.java  <-- Validación de categorías existentes
    │       │   └── SearchServicesJpaRepository.java<-- Consulta SQL optimizada con UNACCENT y filtros
    │       ├── mapper/
    │       │   └── SearchServicesEntityMapper.java
    │       └── adapter/
    │           └── SearchServicesPersistenceAdapter.java <-- Implementa los puertos out de persistencia
    └── config/
        ├── BeanConfiguration.java                  <-- Registro del Service como Bean de Spring
        └── GlobalExceptionHandler.java              <-- Mapeo de HTTP 400 con mensaje del criterio inválido
```

---

## 5. Algoritmo de Normalización y Búsqueda

### Manejo Insensible a Tildes y Mayúsculas (Accent & Case Insensitive)
Para garantizar que `"futbol"` encuentre `"Fútbol 5"` y `"barberia"` encuentre `"Barbería Central"`:

1. **En la Capa de Dominio (`TextNormalizer.java`):**
   ```java
   public final class TextNormalizer {
       private TextNormalizer() {}

       public static String normalize(String text) {
           if (text == null) return null;
           String nfd = Normalizer.normalize(text.trim(), Normalizer.Form.NFD);
           return nfd.replaceAll("\\p{M}", "").toLowerCase();
       }
   }
   ```
2. **En la Capa de Base de Datos (PostgreSQL):**
   * En la consulta nativa SQL se aplica `unaccent(lower(campo)) LIKE unaccent(lower('%texto%'))`, asegurando compatibilidad directa y rendimiento en base de datos. Si la extensión no estuviese habilitada, la proyección recupera los registros activos y el servicio de dominio ejecuta el filtrado final mediante `TextNormalizer`.

---

## 6. Fases de Implementación Paso a Paso

### Fase 1: Capa de Dominio (Modelos, Excepciones y Puertos)
1. **Crear Excepciones:**
   * `InvalidSearchCriteriaException`: Con mensaje `"Debe ingresar al menos un criterio de búsqueda: texto o categoría"`.
   * `CategoryNotFoundException`: Con mensaje `"La categoría ingresada no existe en el catálogo"`.
2. **Crear Utilidad de Dominio `TextNormalizer`:**
   * Métodos `normalize(String)` y `containsIgnoreCaseAndAccents(String source, String target)`.
3. **Crear Modelos de Dominio:**
   * `SearchServiceFilter`: Encapsula `String texto` y `String categoria`.
   * `AvailableScheduleWithResource`: `UUID availabilityId`, `OffsetDateTime inicioAt`, `OffsetDateTime finAt`, `String recurso`.
   * `ServiceSearchItem`: `UUID serviceId`, `String service`, `String provider`, `String category`, `List<AvailableScheduleWithResource> availableSchedules`.
   * `SearchServicesResult`: `List<ServiceSearchItem> servicios`, `String mensaje`.
4. **Definir Puertos:**
   * `SearchServicesInputPort`: `SearchServicesResult search(String texto, String categoria);`
   * `CategoryCatalogOutputPort`: `boolean existsCategoryByName(String categoryName);`
   * `SearchServicesOutputPort`: Consulta de disponibilidades enriquecidas (con nombre de servicio, proveedor, categoría comercial y nombre del recurso).

### Fase 2: Lógica del Servicio de Dominio (`SearchServicesService`)
1. **Paso 2.1: Validar Criterios de Búsqueda:**
   * Si tanto `texto` como `categoria` son nulos o vacíos tras `trim()`:
     Lanzar `InvalidSearchCriteriaException("Debe ingresar al menos un criterio de búsqueda: texto o categoría")`.
2. **Paso 2.2: Validar Existencia de la Categoría:**
   * Si `categoria != null && !categoria.isBlank()`:
     Verificar contra `categoryCatalogOutputPort.existsCategoryByName(categoria)`.
     Si no existe: lanzar `CategoryNotFoundException("La categoría ingresada '" + categoria + "' no existe en el catálogo")`.
3. **Paso 2.3: Consulta y Filtrado:**
   * Obtener ofertas disponibles desde `searchServicesOutputPort`.
   * Filtrar por texto (coincidencia parcial normalizada en servicio o proveedor).
   * Filtrar por categoría (coincidencia exacta o normalizada con la categoría del proveedor).
   * Excluir servicios sin horarios disponibles (`availableSchedules.isEmpty()`).
4. **Paso 2.4: Armar Resultado:**
   * Si la lista resultante está vacía: retornar `SearchServicesResult(List.of(), "No se encontraron servicios")`.
   * Si hay resultados: retornar `SearchServicesResult(servicios, null)`.

### Fase 3: Capa de Infraestructura - Persistencia
1. **Proyección SQL `SearchServicesProjection.java`:**
   ```java
   public interface SearchServicesProjection {
       UUID getServiceId();
       String getService();
       String getProvider();
       String getCategory();
       UUID getAvailabilityId();
       Instant getInicioAt();
       Instant getFinAt();
       String getResourceName();
   }
   ```
2. **Repositorio `SearchServicesJpaRepository.java` (Consulta Nativa Enriquecida):**
   * Realiza `JOIN` entre:
     * `tbl_servicio s`
     * `tbl_proveedor p`
     * `tbl_tipo_proveedor tp` (Categoría)
     * `tbl_servicio_sede ss`
     * `tbl_agenda_servicio ag`
     * `tbl_agenda a`
     * `tbl_agenda_recurso ar`
     * `tbl_recurso r` (Nombre del profesional/recurso)
     * `tbl_disponibilidad d`
     * `tbl_estado_disponibilidad ed`
   * Aplica filtros obligatorios:
     * `s.activo = true AND p.activo = true AND ag.activo = true`
     * `UPPER(ed.codigo) = 'DISPONIBLE'`
     * `NOT EXISTS (reserva confirmada en tbl_ocupacion_disponibilidad)`
     * `NOT EXISTS (día no laborable en tbl_dia_no_laborable)`
3. **Repositorio `TipoProveedorJpaRepository.java`:**
   * Método: `boolean existsByNombreIgnoreCase(String nombre);`
4. **Adaptador de Persistencia `SearchServicesPersistenceAdapter.java`:**
   * Implementa `SearchServicesOutputPort` y `CategoryCatalogOutputPort`.

### Fase 4: Adaptadores REST y Controladores
1. **DTOs de Respuesta:**
   * `ScheduleWithResourceResponseDto`: `UUID availabilityId`, `OffsetDateTime inicioAt`, `OffsetDateTime finAt`, `String recurso`.
   * `SearchedServiceOfferResponseDto`: `UUID serviceId`, `String service`, `String provider`, `String category`, `List<ScheduleWithResourceResponseDto> availableSchedules`.
   * `SearchServicesResponseDto`:
     ```json
     {
       "servicios": [ ... ],
       "mensaje": "No se encontraron servicios" // Presente o null según el caso
     }
     ```
2. **Controlador REST (`SearchServicesController.java`):**
   * Anotado con `@RestController` y `@RequestMapping("/api/v1/catalog/services")`.
   * Endpoint público:
     ```java
     @GetMapping("/search")
     public ResponseEntity<SearchServicesResponseDto> searchServices(
             @RequestParam(required = false) String texto,
             @RequestParam(required = false) String categoria)
     ```
   * Retorna HTTP **`200 OK`** con el cuerpo `SearchServicesResponseDto`.

### Fase 5: Configuración y Manejo de Excepciones
1. **Actualizar `GlobalExceptionHandler.java`:**
   * Manejador para `InvalidSearchCriteriaException` y `CategoryNotFoundException`:
     * Retorna HTTP **`400 BAD REQUEST`**.
     * Respuesta: `Map.of("error", ex.getMessage(), "mensaje", ex.getMessage())`.
2. **Actualizar `BeanConfiguration.java`:**
   * Registrar `@Bean public SearchServicesInputPort searchServicesInputPort(...)`.

---

## 7. Matriz de Cobertura de Escenarios Gherkin

| Escenario Gherkin | Parámetros de Búsqueda | Código HTTP | Contenido de la Respuesta |
| :--- | :--- | :---: | :--- |
| **Buscar servicios por nombre** | `texto`: "CORTE" | **200 OK** | Solo servicios cuyo nombre contiene "corte". Incluye `service`, `provider`, `category` y `availableSchedules` con campo `recurso`. |
| **Buscar servicios por proveedor** | `texto`: "barberia central" | **200 OK** | Devuelve los servicios de "Barbería Central". |
| **Buscar servicios por categoría** | `categoria`: "Deportes y recreación" | **200 OK** | Devuelve solo servicios de proveedores con categoría "Deportes y recreación" ("Complejo El Estadio"). |
| **Combinar texto y categoría** | `texto`: "futbol", `categoria`: "Deportes y recreación" | **200 OK** | Devuelve solo servicios que cumplen ambas condiciones ("futbol" + "Deportes y recreación"). |
| **Búsqueda sin resultados** | `texto`: "Yoga" (sin coincidencias) | **200 OK** | `servicios`: `[]`, `mensaje`: `"No se encontraron servicios"`. |
| **Rechazar búsqueda sin parámetros** | Sin `texto` ni `categoria` | **400 Bad Request** | Mensaje indicando criterio inválido. |
| **Rechazar categoría fuera de catálogo** | `categoria`: "Categoría Inexistente" | **400 Bad Request** | Mensaje indicando categoría inválida fuera del catálogo. |

---

## 8. Plan de Pruebas Automatizadas

1. **Pruebas Unitarias de Dominio (`SearchServicesServiceTest`):**
   * Coincidencia parcial insensible a mayúsculas y acentos: `"futbol"` coincide con `"Fútbol 5"`.
   * Coincidencia por nombre de proveedor: `"barberia central"` coincide con `"Barbería Central"`.
   * Filtro exclusivo por categoría válida.
   * Filtro combinado por texto y categoría simultáneos.
   * Búsqueda válida sin coincidencias: verifica retorno de lista vacía y mensaje `"No se encontraron servicios"`.
   * Exclusión de servicios cuyos horarios disponibles fueron filtrados por ocupación o día no laborable.
   * Rechazo con `InvalidSearchCriteriaException` ante texto y categoría vacíos.
   * Rechazo con `CategoryNotFoundException` ante categoría fuera del catálogo.
2. **Pruebas Unitarias de Utilidad (`TextNormalizerTest`):**
   * Verificación de remoción de tildes (á, é, í, ó, ú, ü, ñ se preserva como n o n tildada según estándar).
3. **Pruebas de Integración Web (`SearchServicesControllerTest` con `MockMvcStandalone`):**
   * Verificación de código `200` y estructura JSON con `service`, `provider`, `category` y `availableSchedules` (con `recurso`).
   * Verificación de código `200` con `servicios: []` y `mensaje: "No se encontraron servicios"`.
   * Verificación de código `400` ante query params vacíos (`GET /api/v1/catalog/services/search`).
   * Verificación de código `400` ante categoría inexistente (`GET /api/v1/catalog/services/search?categoria=Invalida`).
   * Verificación de endpoint público (acceso directo sin token).
4. **Pruebas de Persistencia (`SearchServicesPersistenceAdapterTest`):**
   * Verificación de mapeo de proyección con categoría y recurso vinculado.
