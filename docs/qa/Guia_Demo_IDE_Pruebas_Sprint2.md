# Guía de demo en el IDE · Pruebas unitarias del Sprint 2

**Para:** el equipo de calidad (Sebastián, Juan David, María Paula y Juan Esteban) · **Presentación:** diapositivas 9 a 11 · **Fecha:** 27/10/2026

El objetivo es que, si el profesor pide ver el código, cualquiera de los cuatro pueda abrir la prueba y explicar qué hace cada línea y **por qué**, con argumentos del dominio de Bookify. Las respuestas cortas de la sección 7 sirven para que haga pocas preguntas.

---

## 0. Antes de la review

- [ ] Abrir el proyecto en el IDE en la rama `main` actualizada (`git pull`).
- [ ] Correr `./gradlew test` una vez antes de entrar, para que en vivo tarde unos 4 segundos y muestre 224 pruebas sin fallos (223 se ejecutan y 1 está deshabilitada).
- [ ] Dejar abiertos en pestañas, en este orden:
  1. `src/test/java/udea/fabrica/bookify/domain/service/BookingServiceHu13Test.java`
  2. `src/test/java/udea/fabrica/bookify/infrastructure/config/SecurityConfigTest.java`
  3. `src/test/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/adapter/ResourceJdbcAdapterTest.java`
  4. `src/test/java/udea/fabrica/bookify/infrastructure/config/GlobalExceptionHandlerTest.java`
  5. `src/main/java/udea/fabrica/bookify/infrastructure/config/SecurityConfig.java` (línea de la supresión de CSRF)
- [ ] Otras pestañas: SonarCloud (`juanes0789_bookify-QA`), Azure Boards con los bugs #178, #181, #183 y #186, y la HU-14 con su Gherkin.
- [ ] Despertar Render 10 minutos antes: `curl -s https://bookify-pb92.onrender.com/api/v1/catalog/services`.

---

## 1. Seis ideas que todos deben poder decir en una frase

| Idea | Frase |
|---|---|
| **AAA** | *Arrange* prepara los datos y los dobles, *Act* ejecuta **una** acción, *Assert* verifica el resultado y lo que no debió pasar. |
| **Por qué no hay base de datos** | La arquitectura es hexagonal: el dominio solo conoce **puertos** (interfaces). En la prueba reemplazamos los puertos con dobles de Mockito, así que probamos la regla sin infraestructura. |
| **`when(...).thenReturn(...)`** | Le dice al doble qué responder. Es la parte del *Arrange* que simula la base de datos. |
| **`verify(..., never())`** | Comprueba que algo **no** ocurrió. En reservas es tan importante como el resultado: que no quede una reserva a medias. |
| **`assertThatThrownBy`** | La acción va dentro de la verificación, por eso *Act* y *Assert* van juntos. La lambda debe tener una sola llamada (regla `java:S5778`). |
| **Cobertura** | JaCoCo mide líneas (97,9 %). SonarCloud combina líneas y ramas: (1223 + 196) / (1249 + 242) = 95,2 %. Cobertura alta no es ausencia de defectos. |

---

## 2. `BookingServiceHu13Test` · la regla de HU-13

**Dominio:** desde HU-13, la agenda pertenece al **recurso** (un barbero, una cancha), no al servicio. Reservar es: encontrar un recurso libre para el proveedor en ese horario, guardar la reserva como CONFIRMADA y ligarla al recurso. Si no hay recurso libre, no se reserva. Es la regla crítica de Bookify: **un horario se reserva una sola vez**.

```java
@ExtendWith(MockitoExtension.class)
class BookingServiceHu13Test {
    @Mock private BookingRepositoryPort bookings;
    @Mock private AvailabilityRepositoryPort availability;
    @Mock private ResourceAvailabilityPort resources;
    @InjectMocks private BookingService service;
```

- `@ExtendWith(MockitoExtension.class)`: activa Mockito en JUnit 5; crea los dobles antes de cada prueba.
- Los tres `@Mock` son los **puertos de salida** que usa `BookingService`: reservas, disponibilidades y recursos. No hay JPA ni base de datos.
- `@InjectMocks`: crea el `BookingService` **real** y le inyecta los tres dobles por constructor. Lo que se prueba es el servicio de dominio de verdad.

### Prueba 1 · camino feliz: se asigna el recurso

```java
@Test
void shouldAssignAvailableResource_whenCreatingResourceScheduledBooking() {
    // Arrange
    Booking booking = resourceBooking();
    UUID resourceId = UUID.randomUUID();
    UUID bookingId = UUID.randomUUID();
    when(resources.findAvailableResource(booking.getProviderId(), booking.getStartAt(), booking.getEndAt())).thenReturn(Optional.of(resourceId));
    when(bookings.findReservationStatusIdByCode("CONFIRMADA")).thenReturn(10);
    when(bookings.save(any())).thenAnswer(invocation -> { Booking saved = invocation.getArgument(0); saved.setId(bookingId); return saved; });

    // Act
    Booking saved = service.create(booking);

    // Assert
    assertThat(saved.getResourceId()).isEqualTo(resourceId);
    assertThat(saved.getEstadoReservaId()).isEqualTo(10);
    verify(resources).assignResourceToBooking(bookingId, resourceId);
    verify(bookings).save(booking);
    verifyNoInteractions(availability);
}
```

| Línea | Qué hace | Por qué (dominio) |
|---|---|---|
| `resourceBooking()` | Arma una reserva de 1 hora que empieza dentro de 1 hora, con cliente, proveedor, servicio, inicio y fin. | Con proveedor, servicio, inicio y fin, `BookingService` toma el flujo de HU-13 (`usesResourceScheduling`). Ese flujo solo valida que el fin sea posterior al inicio; **no** valida que el horario sea futuro (es parte del bug #183). |
| `findAvailableResource(...)` → `Optional.of(resourceId)` | Simula que el proveedor tiene un recurso libre en ese horario. | La disponibilidad ahora se calcula por recurso. |
| `findReservationStatusIdByCode("CONFIRMADA")` → `10` | Simula el catálogo de estados. | Los estados viven en una tabla (`tbl_estado_reserva`); el servicio busca el id por código, no lo tiene fijo. |
| `save(any())` con `thenAnswer` | Simula que la base asigna el id al guardar. | La prueba necesita ese id para verificar la asignación del recurso a **esa** reserva. |
| `service.create(booking)` | La única acción (*Act*). | Es el caso de uso de HU-03 y HU-13. |
| `assertThat(saved.getResourceId())` | La reserva quedó con el recurso encontrado. | El cliente reserva un recurso concreto (objetivo del sprint). |
| `getEstadoReservaId() == 10` | Quedó CONFIRMADA. | Criterio «Crear una reserva correctamente». |
| `verify(resources).assignResourceToBooking(bookingId, resourceId)` | Se escribió la relación reserva–recurso. | **Esta línea detectó el bug #172**: un cambio había borrado esa llamada, y sin la relación el recurso seguía apareciendo libre (doble reserva). |
| `verifyNoInteractions(availability)` | No se tocó el flujo viejo de disponibilidades. | Confirma que se tomó el flujo de HU-13 y no el del Sprint 1. |

### Prueba 2 · excepcional: no hay recurso libre

```java
@Test
void shouldNotSaveBooking_whenNoResourceIsAvailable() {
    // Arrange
    Booking booking = resourceBooking();
    when(resources.findAvailableResource(any(), any(), any())).thenReturn(Optional.empty());

    // Act / Assert
    assertThatThrownBy(() -> service.create(booking)).isInstanceOf(ResourceUnavailableException.class);
    verify(bookings, never()).save(any());
    verify(resources, never()).assignResourceToBooking(any(), any());
}
```

| Línea | Qué hace | Por qué (dominio) |
|---|---|---|
| `findAvailableResource(any(), any(), any())` → `Optional.empty()` | Para cualquier proveedor y horario, no hay recurso libre. | Es el escenario «Impedir la doble reserva sobre el mismo recurso»: Ana ya reservó a Barbero Carlos a las 10:00. |
| `assertThatThrownBy(...).isInstanceOf(ResourceUnavailableException.class)` | Crear la reserva lanza la excepción de dominio. | El dominio rechaza la doble reserva. |
| `verify(bookings, never()).save(any())` | **Nunca** se guardó la reserva. | No puede quedar una reserva sin recurso. |
| `verify(resources, never()).assignResourceToBooking(...)` | **Nunca** se asignó un recurso. | No puede quedar el recurso marcado como ocupado por una reserva que no existe. |

**El límite de esta prueba (bug #186), contarlo si preguntan «¿y qué recibe el cliente?»:** la prueba verifica el dominio, no el contrato HTTP. El criterio de HU-13 pide **409** con el mensaje «El recurso no está disponible en ese horario». `ResourceUnavailableException` no tiene manejador en `GlobalExceptionHandler`, así que la API respondería **500**, y además el mensaje de la excepción es otro. Lo encontramos preparando esta demo y está registrado como #186, con la corrección propuesta: mapearla a 409 y agregar una prueba del manejador.

---

## 3. `SecurityConfigTest` · roles de HU-07

**Dominio:** HU-07 define qué endpoints son públicos (catálogo y búsqueda) y qué rol necesita cada uno: `/api/v1/comercio/**` es de PROVEEDOR y `/api/v1/reservas/**` es de CLIENTE. Un rol sin permiso recibe **403**; sin token, **401**.

Lo especial de esta clase es que **no usa dobles para la seguridad**. En `setUp()` levanta un contexto mínimo de Spring con el `SecurityConfig` real, el `JwtService` real y un controlador de prueba, sin base de datos. Así se prueba la **cadena real de filtros** de Spring Security, no una simulación.

```java
@Test
void shouldReturn403_whenClienteCallsProviderEndpoint() throws Exception {
    // Arrange
    String token = jwtService.generateToken(UUID.randomUUID(), "ana@mail.com", UserRole.CLIENTE);
    var request = get(PROVIDER_ENDPOINT).header("Authorization", "Bearer " + token);

    // Act / Assert
    mockMvc.perform(request).andExpect(status().isForbidden());
}
```

| Línea | Qué hace | Por qué (dominio) |
|---|---|---|
| `jwtService.generateToken(..., UserRole.CLIENTE)` | Genera un JWT **real**, firmado, con el rol CLIENTE en el claim `role`. | El token es igual al que entrega `/api/v1/auth/login`; si cambia la firma o el claim, la prueba falla. |
| `get(PROVIDER_ENDPOINT)` | Petición a `/api/v1/comercio/perfil`. | Es un endpoint de proveedor (crear perfil comercial, HU-09). |
| `.header("Authorization", "Bearer " + token)` | Envía el token en la cabecera. | Bookify es sin estado: el token viaja en la cabecera, no en una cookie (por eso CSRF no aplica, sección 6). |
| `status().isForbidden()` | Espera **403**. | Criterio «Rechazar una petición con un rol sin permiso». Antes respondía 200 a cualquier autenticado (bug #175, ya corregido y verificado en Render). |

La prueba siguiente, `shouldAllowProviderEndpoint_whenRoleIsProveedor`, es el espejo: mismo endpoint con rol PROVEEDOR → 200. Juntas prueban la regla en las dos direcciones.

---

## 4. `ResourceJdbcAdapterTest` · recurso duplicado de HU-12

**Dominio:** HU-12 dice que el nombre de un recurso es único **dentro del comercio**: si ya existe «Cancha 1», registrar «cancha 1» en el mismo comercio responde 409 «Ya existe un recurso con ese nombre en tu comercio». La base de datos tiene una restricción única; el adaptador traduce esa violación a una excepción de dominio.

```java
@Test
void shouldThrowConflict_whenInsertViolatesUniqueName() {
    // Arrange
    Resource resource = resource(UUID.randomUUID());
    stubTypeAndStatus(List.of(2), List.of(1));
    when(jdbc.update(contains("INSERT INTO tbl_recurso"), any(), any(), any(), any(), any()))
            .thenThrow(new DataIntegrityViolationException("duplicate key"));

    // Act + Assert
    assertThatThrownBy(() -> adapter.save(resource))
            .isInstanceOf(DomainConflictException.class)
            .hasMessage("Ya existe un recurso con ese nombre en tu comercio");
}
```

| Línea | Qué hace | Por qué (dominio y técnica) |
|---|---|---|
| `resource(UUID.randomUUID())` | Un recurso FISICO «Cancha 1» de un proveedor cualquiera. | Los tipos válidos son FISICO y HUMANO (HU-12). |
| `stubTypeAndStatus(List.of(2), List.of(1))` | Simula que el tipo FISICO existe (id 2) y el estado ACTIVO existe (id 1). | El adaptador busca esos ids en sus catálogos antes de insertar; si faltan, falla con otro error (lo cubren otras dos pruebas). |
| `jdbc.update(contains("INSERT INTO tbl_recurso"), any() ×5)` | Identifica el `INSERT` por su SQL y sus 5 parámetros (id, proveedor, tipo, estado, nombre). | `update` recibe *varargs*: con matchers se pone uno por argumento. `contains` evita copiar todo el SQL en la prueba. |
| `.thenThrow(new DataIntegrityViolationException(...))` | Simula que PostgreSQL rechaza el `INSERT` por la restricción única. | Es lo que hace Spring cuando la base devuelve una violación de integridad. |
| `isInstanceOf(DomainConflictException.class)` | El adaptador la traduce a una excepción de **dominio**. | El dominio no debe conocer excepciones de Spring/JDBC (arquitectura hexagonal). |
| `hasMessage("Ya existe un recurso con ese nombre en tu comercio")` | Verifica el mensaje **exacto**. | Es el mensaje del criterio de HU-12. `DomainConflictException` se traduce a 409 en `GlobalExceptionHandler`. |

---

## 5. `GlobalExceptionHandlerTest` · prueba parametrizada

**Dominio:** cuando la base rechaza una operación por una restricción, el manejador global decide el código y el mensaje según **qué restricción** falló. Por ejemplo, HU-10: registrar dos veces el mismo día no laborable → 409 «La fecha ya está registrada como no laborable».

> Esta versión entró a `main` con el PR #12 (commit `389f000`). Antes eran tres pruebas separadas con la misma estructura: eso es justo lo que SonarCloud marcó como `java:S5976`.

```java
@ParameterizedTest(name = "{0} -> {1}")
@CsvSource(delimiter = '|', textBlock = """
        duplicate key value violates unique constraint "uq_dia_no_laborable_proveedor_fecha"    | 409 | La fecha ya está registrada como no laborable
        insert violates foreign key constraint "tbl_dia_no_laborable_proveedor_id_fkey"         | 400 | El proveedor o usuario especificado no existe
        insert violates foreign key constraint "tbl_dia_no_laborable_creado_por_usuario_id_fkey" | 400 | El proveedor o usuario especificado no existe
        duplicate key value violates unique constraint "uq_usuario_correo"                      | 409 | El correo ya está registrado
        """)
void shouldMapDataIntegrityViolationByConstraint(String causeMessage, int expectedStatus, String expectedMessage) {
    // Arrange
    GlobalExceptionHandler handler = new GlobalExceptionHandler();
    var violation = new DataIntegrityViolationException("could not execute statement",
            new RuntimeException(causeMessage));

    // Act
    var response = handler.handleDataIntegrityViolation(violation);

    // Assert
    assertThat(response.getStatusCode().value()).isEqualTo(expectedStatus);
    assertThat(response.getBody()).containsEntry("mensaje", expectedMessage);
}
```

| Parte | Qué hace | Por qué |
|---|---|---|
| `@ParameterizedTest` + `@CsvSource` | Ejecuta la misma prueba 4 veces, una por fila. Cada fila es: mensaje de PostgreSQL, código esperado y mensaje esperado. | Las 4 pruebas tenían la misma estructura; repetirlas era deuda técnica (Sonar `java:S5976`). Agregar una restricción nueva es agregar una fila. |
| Fila 1 · `uq_dia_no_laborable_proveedor_fecha` → 409 | Fecha duplicada. | Criterio «Rechazar una fecha ya registrada» de HU-10. |
| Filas 2 y 3 · `..._fkey` → 400 | Proveedor o usuario que no existe. | Es un error de los datos enviados (400), no un conflicto. La fila 3 no estaba cubierta antes. |
| Fila 4 · `uq_usuario_correo` → 409 | Correo ya registrado. | Criterio «Rechazar un correo ya registrado» de HU-06. |
| `new RuntimeException(causeMessage)` como causa | Simula la excepción de PostgreSQL dentro de la de Spring. | El manejador lee `getMostSpecificCause()`, es decir, el mensaje real de la base. |

**Si preguntan por el caso por defecto:** cualquier violación que no coincida con estas restricciones responde 409 «El correo ya está registrado», aunque no tenga que ver con el correo. Lo registramos como **bug #184**; por eso esa rama no se agregó como fila: sería validar un comportamiento incorrecto.

---

## 6. `SecurityConfig` · por qué CSRF está desactivado (no es una prueba, pero lo preguntan)

```java
// java:S112: HttpSecurity#build() declara Exception, así que el método no puede declarar otra más específica.
// java:S4502: la API es sin estado y recibe el JWT en la cabecera Authorization, sin cookies de sesión;
// el navegador no adjunta esa cabecera por sí solo, por lo que CSRF no aplica.
@Bean
@SuppressWarnings({"java:S112", "java:S4502"})
public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService jwtService)
```

**En una frase:** un ataque CSRF necesita que el navegador mande la credencial solo, como pasa con una cookie de sesión. Bookify no tiene sesión (`SessionCreationPolicy.STATELESS`) y el token va en la cabecera `Authorization`, que el navegador no agrega por su cuenta. Por eso es seguro desactivar CSRF, y la supresión queda escrita en el código con ese argumento. Si algún día el token se guarda en una cookie, hay que reactivarlo.

---

## 7. Preguntas probables

| Pregunta | Respuesta corta |
|---|---|
| ¿Por qué las pruebas no usan base de datos? | Arquitectura hexagonal: el dominio depende de puertos y los reemplazamos con dobles de Mockito. Las pruebas que necesitan la base (integración) son el siguiente paso del plan. |
| ¿Por qué *Act* y *Assert* van juntos a veces? | Cuando la acción lanza una excepción, `assertThatThrownBy` la ejecuta dentro de la verificación. |
| ¿Qué es `verify(..., never())`? | Comprueba que algo no pasó: en reservas, que no quede una reserva o un recurso a medias. |
| ¿Las pruebas encontraron defectos? | Sí: `BookingServiceHu13Test` detectó el #172 (faltaba ligar la reserva al recurso). Y al revisarlas encontramos su límite: el #186. |
| ¿Por qué SonarCloud y JaCoCo dan cifras distintas? | JaCoCo mide líneas (97,9 %); SonarCloud combina líneas y ramas (95,2 %). |
| ¿Qué queda sin cubrir? | El arranque de Spring (necesita la base) y `findByEmail`, que no probamos a propósito (bug #185). |
| ¿Por qué hay una prueba deshabilitada? | `BookifyApplicationTests` levanta toda la aplicación y necesita la base de datos real. |
| ¿Por qué una prueba parametrizada? | Cuatro casos con la misma estructura; SonarCloud lo marcó como `java:S5976`. Ahora cada restricción es una fila. |
| ¿Por qué `contains(...)` en los mocks de JDBC? | Para identificar cada consulta por su tabla sin copiar todo el SQL; la prueba no se rompe si cambia un espacio del SQL. |
| ¿Cómo saben que el CSRF desactivado es seguro? | Sección 6: API sin estado, token en cabecera, sin cookies. |

---

## 8. Antes de abrir otros archivos

14 de los archivos de prueba de `main` siguen la lógica AAA pero **no marcan** los bloques `// Arrange / Act / Assert`; los más visibles son `RegisterUserServiceTest`, `SearchServicesServiceTest`, `UserControllerTest` y `BookingServiceTest` (este último, además, con el formato compactado). Si el profesor pide «otra prueba al azar», mejor proponer una de las cuatro de esta guía o cualquiera de las clases de Sprint 2 que sí las marcan, por ejemplo `MerchantProfileUseCaseImplTest`, `ResourceUseCaseImplTest`, `NonWorkingDayControllerTest` o `MerchantProfileJdbcAdapterTest`.
