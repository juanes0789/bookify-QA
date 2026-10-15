# Plan de Correcciones y Finalización: Registro de Clientes y Proveedores

Este documento describe el plan de acción estructurado para resolver los conflictos, inconsistencias y elementos pendientes identificados durante la auditoría técnica de la Historia de Usuario (HU) de **Registro de Clientes y Proveedores** de Bookify, basada en [PLAN_IMPLEMENTACION_REGISTRO.md](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/PLAN_IMPLEMENTACION_REGISTRO.md).

---

## 1. Resumen Ejecutivo de Hallazgos

| # | Área | Conflicto / Ocurrencia | Severidad | Acción Requerida |
|---|---|---|:---:|---|
| **1** | Pruebas Web | Incompatibilidad de paquetes en `UserControllerTest` (Spring Boot 4.1.1 / Jackson 3) | 🔴 Alta | Migrar a `MockMvcStandalone` con JSON raw o actualizar imports de Jackson 3. |
| **2** | Pruebas Persistencia | Omisión de `UserPersistenceAdapterTest` definido en Sección 7 del plan original | 🟡 Media | Implementar prueba unitaria del adaptador con aislamiento de repositorios. |
| **3** | Resiliencia / Concurrencia | Condición de carrera en registro concurrente (`DataIntegrityViolationException`) | 🟡 Media | Agregar handler específico en `GlobalExceptionHandler` retornando HTTP 409. |
| **4** | Integridad de Datos | Nombres de una sola palabra generan `apellidos = ""` en `tbl_usuario` | 🔵 Baja | Definir validación de nombre completo o salvaguarda de formato en dominio. |
| **5** | Catálogos / Semillas | Falla de inserción si `tbl_estado_usuario` o `tbl_rol` no tienen registros previos | 🟡 Media | Crear script semilla de inicialización o manejo defensivo en infraestructura. |
| **6** | Configuración Gradle | Tarea `test` no hereda variables del archivo `.env` para pruebas de contexto | 🔵 Baja | Extender lectura de `.env` o definir `application-test.properties`. |

---

## 2. Plan Detallado de Acciones de Corrección

### Acción 1: Corrección y Desbloqueo de la Suite de Pruebas Web (`UserControllerTest`)
* **Archivo objetivo:** `src/test/java/udea/fabrica/bookify/infrastructure/adapter/input/rest/controller/UserControllerTest.java`
* **Diagnóstico técnico:**
  * El proyecto corre sobre **Spring Boot 4.1.1** (Spring 7.0.9).
  * `com.fasterxml.jackson.databind.ObjectMapper` no existe en el classpath porque Spring Boot 4 migró a **Jackson 3.x** (`tools.jackson.databind.ObjectMapper`).
  * La anotación `@WebMvcTest` no se encuentra en el subconjunto de autoconfiguración cargado por `spring-boot-starter-test`.
* **Solución técnica recomendada (Pruebas unitarias de controlador desacopladas):**
  1. Configurar `MockMvc` mediante `MockMvcBuilders.standaloneSetup(userController).setControllerAdvice(new GlobalExceptionHandler()).build()`.
  2. Sustituir la serialización dinámica de `ObjectMapper` por plantillas de bloques de texto Java (Text Blocks `"""..."""`) nativos, eliminando cualquier dependencia frágil de librerías de serialización en el test.
  3. Mantener todos los 7 escenarios de prueba (HTTP 201 cliente/proveedor, HTTP 409 colisión, HTTP 400 contraseñas NIST).
* **Beneficio:** Ejecución ultrarrápida (en milisegundos), sin necesidad de levantar contexto Spring ni lidiar con cambios de paquetes en Spring Boot 4.

---

### Acción 2: Implementación de la Prueba de Persistencia Faltante (`UserPersistenceAdapterTest`)
* **Archivo a crear:** `src/test/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/adapter/UserPersistenceAdapterTest.java`
* **Diagnóstico técnico:**
  * La Sección 7 del plan original estipuló `UserPersistenceAdapterTest` para verificar la inserción coordinada en `tbl_usuario` y `tbl_usuario_rol`.
* **Solución técnica recomendada:**
  1. Crear la suite unitaria aislando los 4 repositorios JPA (`UserJpaRepository`, `EstadoUsuarioJpaRepository`, `RolJpaRepository`, `UsuarioRolJpaRepository`) mediante fakes en memoria o mocks (`Mockito.mock`).
  2. **Caso 1:** Registro exitoso: verificar que se busca el estado `'ACTIVO'`, se busca el rol `'CLIENTE'`, se guarda `UserEntity` y se guarda `UsuarioRolEntity` con el UUID generado y la fecha de asignación.
  3. **Caso 2:** Confidencialidad: verificar que el objeto de dominio retornado tenga `passwordHash == null`.
  4. **Caso 3:** Excepción por estado inexistente: verificar que lance `IllegalStateException` si `'ACTIVO'` no existe.
  5. **Caso 4:** Excepción por rol inexistente: verificar que lance `IllegalStateException` si el rol solicitado no existe.

---

### Acción 3: Mitigación de Concurrencia y Violación de Unicidad (`DataIntegrityViolationException`)
* **Archivo objetivo:** `src/main/java/udea/fabrica/bookify/infrastructure/config/GlobalExceptionHandler.java`
* **Diagnóstico técnico:**
  * En `RegisterUserService.java`, la comprobación `existsByEmail(email)` previa al `save(user)` no es una transacción serializable atómica.
  * Si dos peticiones idénticas ingresan en paralelo, ambas pasan la validación y disparan un `INSERT`. La base de datos rechaza la segunda mediante la restricción única `tbl_usuario.correo`, arrojando `org.springframework.dao.DataIntegrityViolationException`.
  * Sin un manejador explícito, Spring Boot traduce esta excepción en **HTTP 500 Internal Server Error**.
* **Solución técnica recomendada:**
  1. Añadir `@ExceptionHandler(DataIntegrityViolationException.class)` en `GlobalExceptionHandler`.
  2. Responder con código HTTP **409 Conflict** y el cuerpo exacto requerido por la especificación:
     ```java
     @ExceptionHandler(DataIntegrityViolationException.class)
     public ResponseEntity<Map<String, String>> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
         return ResponseEntity.status(HttpStatus.CONFLICT)
                 .body(Map.of("error", "El correo ya está registrado", "mensaje", "El correo ya está registrado"));
     }
     ```

---

### Acción 4: Robustecimiento de Nombres y Apellidos en el Servicio de Dominio
* **Archivo objetivo:** `src/main/java/udea/fabrica/bookify/domain/service/RegisterUserService.java`
* **Diagnóstico técnico:**
  * La división actual es:
    ```java
    String[] partes = nombre.trim().split("\\s+", 2);
    String nombres = partes[0];
    String apellidos = partes.length > 1 ? partes[1] : "";
    ```
  * En PostgreSQL, la columna `tbl_usuario.apellidos` tiene restricción `NOT NULL`. Aunque `""` no viola `NOT NULL`, produce registros con apellido vacío si el visitante ingresa únicamente `"Carlos"`.
* **Solución técnica recomendada:**
  1. Validar que el nombre contenga al menos nombre y apellido (`nombre.trim().contains(" ")`), lanzando `IllegalArgumentException("Debe proporcionar al menos un nombre y un apellido")`.
  2. Alternativamente, si se permite registrar nombres de una sola palabra, asignar un valor por defecto o permitir que la cadena vacía sea la representación canónica aprobada, documentándolo en las reglas del negocio.

---

### Acción 5: Aseguramiento de Datos Semilla para Catálogos de Base de Datos
* **Archivos involucrados:** `src/main/resources/data.sql` o script de migración PostgreSQL.
* **Diagnóstico técnico:**
  * `UserPersistenceAdapter` depende de que existan registros con `codigo = 'ACTIVO'` en `tbl_estado_usuario` y `codigo = 'CLIENTE'` / `'PROVEEDOR'` en `tbl_rol`.
  * En una base de datos recién creada, el registro de usuarios fallará irremediablemente hasta que se inserten dichos registros.
* **Solución técnica recomendada:**
  1. Proveer un script idempotente de inicialización:
     ```sql
     INSERT INTO public.tbl_estado_usuario (codigo, nombre, descripcion)
     VALUES ('ACTIVO', 'Activo', 'Usuario activo en el sistema')
     ON CONFLICT (codigo) DO NOTHING;

     INSERT INTO public.tbl_rol (codigo, nombre, descripcion)
     VALUES 
       ('CLIENTE', 'Cliente', 'Usuario final que realiza reservas'),
       ('PROVEEDOR', 'Proveedor', 'Proveedor de servicios comerciales')
     ON CONFLICT (codigo) DO NOTHING;
     ```

---

### Acción 6: Homologación del Entorno de Ejecución para Gradle Test
* **Archivo objetivo:** `build.gradle` o `src/test/resources/application.properties`
* **Diagnóstico técnico:**
  * En `build.gradle`, el bloque que parsea `.env` solo está asociado a `bootRun`.
  * La tarea `./gradlew test` no carga el archivo `.env`, lo que causa que cualquier prueba que cargue el contexto de Spring falle por no encontrar `${SPRING_DATASOURCE_URL}`.
* **Solución técnica recomendada:**
  1. Extender en `build.gradle` la carga del archivo `.env` a la tarea `test`:
     ```groovy
     tasks.named('test') {
         useJUnitPlatform()
         if (file('.env').exists()) {
             file('.env').readLines().each { line ->
                 if (line && !line.startsWith('#') && line.contains('=')) {
                     def keyVal = line.split('=', 2)
                     environment keyVal[0].trim(), keyVal[1].trim()
                 }
             }
         }
     }
     ```
  2. O agregar `src/test/resources/application.properties` configurando datasource en memoria / perfiles de prueba para aislar las pruebas de variables del sistema.

---

## 3. Matriz de Ejecución y Orden de Implementación

Para asegurar que no se introduzcan regresiones, las correcciones deben ejecutarse en el siguiente orden secuencial:

```
[Acción 1: UserControllerTest] ──► [Acción 2: UserPersistenceAdapterTest]
               │
               ▼
[Acción 3: GlobalExceptionHandler (409 Concurrente)]
               │
               ▼
[Acción 4: Validación de Nombres y Apellidos]
               │
               ▼
[Acción 5 & 6: Configuración Gradle y Scripts Semilla]
               │
               ▼
[Verificación Final: ./gradlew test exitoso 100%]
```

---

## 4. Criterio de Éxito y Verificación

La implementación de la HU se considerará finalizada con éxito cuando:
1. `./gradlew test` compile sin errores y ejecute al 100% las 4 suites de pruebas:
   - `RegisterUserServiceTest` (11 pruebas pasadas)
   - `ResourceCommonPasswordAdapterTest` (3 pruebas pasadas)
   - `UserControllerTest` (7 pruebas pasadas)
   - `UserPersistenceAdapterTest` (4 pruebas pasadas)
2. Los 4 escenarios Gherkin de aceptacion respondan exactamente con los codigos HTTP (201, 400, 409) y mensajes estipulados en [PLAN_IMPLEMENTACION_REGISTRO.md](file:///c:/Users/Alejandro/Documents/Fabrica2026-II/bookify/PLAN_IMPLEMENTACION_REGISTRO.md).

---

## 5. Registro de Cambios Aplicados en Fase 7

Fecha de actualizacion: 2026-10-12.

Durante la fase 7 se aplicaron las correcciones funcionales y de pruebas indicadas en este plan:

| Accion | Estado | Archivos modificados / creados | Resultado |
|---|---|---|---|
| Accion 1 - Pruebas Web | Aplicada | `src/test/java/udea/fabrica/bookify/infrastructure/adapter/input/rest/controller/UserControllerTest.java` | Se reemplazo el uso de `@WebMvcTest` y `ObjectMapper` de Jackson 2 por una prueba desacoplada con `MockMvcBuilders.standaloneSetup(...)`, `GlobalExceptionHandler` real y mock manual de `RegisterUserInputPort`. |
| Accion 2 - Prueba Persistencia | Aplicada | `src/test/java/udea/fabrica/bookify/infrastructure/adapter/output/persistence/adapter/UserPersistenceAdapterTest.java` | Se agrego la suite unitaria faltante para validar guardado coordinado en `tbl_usuario` y `tbl_usuario_rol`, confidencialidad del `passwordHash`, y errores cuando faltan catalogos. |
| Accion 3 - Concurrencia / unicidad | Aplicada | `src/main/java/udea/fabrica/bookify/infrastructure/config/GlobalExceptionHandler.java` | Se agrego `@ExceptionHandler(DataIntegrityViolationException.class)` para responder `409 Conflict` con el mensaje `"El correo ya esta registrado"`. |
| Accion 4 - Nombre completo | Aplicada | `src/main/java/udea/fabrica/bookify/domain/service/RegisterUserService.java`, `src/test/java/udea/fabrica/bookify/domain/service/RegisterUserServiceTest.java` | Se rechazo el registro con nombre de una sola palabra y se actualizo la prueba de dominio asociada. |
| Accion 5 - Datos semilla | Aplicada | `src/main/resources/data.sql` | Se agregaron inserts idempotentes para `ACTIVO`, `CLIENTE` y `PROVEEDOR`. |
| Accion 6 - Gradle test | Aplicada | `build.gradle` | Se reutilizo la carga de `.env` para `bootRun` y `test`, evitando que las pruebas dependientes del contexto queden sin variables de entorno. |

### Detalle tecnico de los cambios

1. `UserControllerTest` ahora construye el controlador directamente:
   ```java
   mockMvc = MockMvcBuilders.standaloneSetup(new UserController(registerUserInputPort))
           .setControllerAdvice(new GlobalExceptionHandler())
           .build();
   ```
   Esto evita levantar el contexto completo de Spring y evita depender del paquete antiguo `com.fasterxml.jackson.databind.ObjectMapper`.

2. `GlobalExceptionHandler` ahora captura violaciones de integridad generadas por la base de datos. Esto cubre el caso concurrente donde dos solicitudes pasan la validacion `existsByEmail(...)` y la segunda falla al insertar por la restriccion unica del correo.

3. `RegisterUserService` valida que `nombre` tenga al menos dos partes separadas por espacio. Si no, lanza:
   ```text
   Debe proporcionar al menos un nombre y un apellido
   ```

4. `UserPersistenceAdapterTest` usa mocks de los repositorios JPA para validar el comportamiento del adaptador sin requerir base de datos real.

5. `data.sql` contiene semillas con `ON CONFLICT (codigo) DO NOTHING`, por lo que puede ejecutarse varias veces sin duplicar catalogos.

---

## 6. Consideraciones Pendientes para Revision Posterior

La verificacion automatizada completa quedo pendiente por condiciones del entorno local durante la ejecucion de Gradle:

1. `./gradlew.bat test` tardo demasiado iniciando procesos de Gradle/daemon.
2. Un intento posterior avanzo hasta compilacion, pero reporto errores anormales indicando que no encontraba paquetes internos que si existen en `src/main/java`.
3. Tambien se detecto bloqueo de Windows sobre:
   ```text
   build/reports/problems/problems-report.html
   ```
   Este bloqueo impidio ejecutar `clean` correctamente.
4. `git status` no pudo ejecutarse por configuracion de repositorio seguro:
   ```text
   detected dubious ownership in repository
   ```
   Para revisiones con Git, puede requerirse:
   ```bash
   git config --global --add safe.directory C:/Users/Alejandro/Documents/Fabrica2026-II/bookify
   ```

Antes de cerrar definitivamente la HU, se recomienda:

1. Cerrar cualquier visor, navegador, IDE o proceso que pueda tener abierto `build/reports/problems/problems-report.html`.
2. Detener daemons de Gradle:
   ```bash
   ./gradlew.bat --stop
   ```
3. Ejecutar una compilacion limpia:
   ```bash
   ./gradlew.bat clean test --no-daemon --console=plain
   ```
4. Si `clean` vuelve a fallar por bloqueo de archivos, reiniciar el equipo o cerrar procesos Java abiertos y repetir.
5. Revisar que `data.sql` sea compatible con la estrategia de inicializacion configurada en Spring Boot para el ambiente usado. En produccion, si se usa Flyway/Liquibase, migrar estas semillas a una migracion formal.
6. Confirmar con negocio que exigir nombre y apellido es la regla definitiva. Si se decide permitir nombres de una sola palabra, debe revertirse la validacion y documentarse el valor permitido para `apellidos`.

---

## 7. Paso a Paso para Probar la HU en Postman

### 7.1 Prerrequisitos

1. Tener PostgreSQL disponible y la base de datos configurada segun `application.properties` o `.env`.
2. Confirmar que existan los catalogos requeridos:
   - `tbl_estado_usuario.codigo = 'ACTIVO'`
   - `tbl_rol.codigo = 'CLIENTE'`
   - `tbl_rol.codigo = 'PROVEEDOR'`
3. Si la aplicacion ejecuta `data.sql`, estos catalogos deberian insertarse automaticamente.
4. Levantar la aplicacion:
   ```bash
   ./gradlew.bat bootRun
   ```
5. Usar como base URL:
   ```text
   http://localhost:8080
   ```

### 7.2 Configuracion en Postman

1. Crear una nueva request.
2. Metodo: `POST`.
3. URL:
   ```text
   http://localhost:8080/api/v1/auth/register
   ```
4. En `Headers`, agregar:
   ```text
   Content-Type: application/json
   ```
5. En `Body`, seleccionar `raw` y formato `JSON`.

### 7.3 Escenario 1 - Registrar Cliente Exitosamente

Body:

```json
{
  "nombre": "Ana Gomez",
  "correo": "ana@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "CLIENTE"
}
```

Resultado esperado:

```text
HTTP 201 Created
```

El body debe incluir `id`, `nombre`, `correo` y `rol`. No debe incluir `contrasena` ni `passwordHash`.

### 7.4 Escenario 2 - Registrar Proveedor Exitosamente

Body:

```json
{
  "nombre": "Carlos Barberia",
  "correo": "carlos@barberia.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "PROVEEDOR"
}
```

Resultado esperado:

```text
HTTP 201 Created
```

El campo `rol` de la respuesta debe ser `PROVEEDOR`.

### 7.5 Escenario 3 - Rechazar Correo Ya Registrado

Ejecutar nuevamente el registro de Ana, cambiando el correo a mayusculas para validar la comparacion insensible a mayusculas:

```json
{
  "nombre": "Ana Gomez",
  "correo": "ANA@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "CLIENTE"
}
```

Resultado esperado:

```text
HTTP 409 Conflict
```

Body esperado:

```json
{
  "error": "El correo ya esta registrado",
  "mensaje": "El correo ya esta registrado"
}
```

Nota: si el mensaje aparece con tilde en el codigo fuente o en la respuesta final, tambien es valido mientras coincida con la especificacion funcional del proyecto.

### 7.6 Escenario 4 - Rechazar Contrasena Corta

Body:

```json
{
  "nombre": "Laura Perez",
  "correo": "laura@mail.com",
  "contrasena": "corta123",
  "rol": "CLIENTE"
}
```

Resultado esperado:

```text
HTTP 400 Bad Request
```

El body debe contener `debe tener entre 15 y 64 caracteres`.

### 7.7 Escenario 5 - Rechazar Contrasena Comun

Body:

```json
{
  "nombre": "Mario Lopez",
  "correo": "mario@mail.com",
  "contrasena": "contrasena123456",
  "rol": "CLIENTE"
}
```

Resultado esperado:

```text
HTTP 400 Bad Request
```

El body debe contener `es una contrasena comun o comprometida`.

Nota: este escenario depende del contenido exacto de `src/main/resources/passwords/common-passwords.txt`. Si la lista contiene la variante con `ñ`, probar tambien:

```json
{
  "nombre": "Mario Lopez",
  "correo": "mario@mail.com",
  "contrasena": "contraseña123456",
  "rol": "CLIENTE"
}
```

### 7.8 Escenario 6 - Rechazar Contrasena que Contiene el Correo

Body:

```json
{
  "nombre": "Sofia Ramirez",
  "correo": "sofia@mail.com",
  "contrasena": "sofia@mail.com-2026",
  "rol": "CLIENTE"
}
```

Resultado esperado:

```text
HTTP 400 Bad Request
```

El body debe contener `no puede contener tu correo`.

### 7.9 Escenario 7 - Rechazar Nombre sin Apellido

Body:

```json
{
  "nombre": "Pedro",
  "correo": "pedro@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "CLIENTE"
}
```

Resultado esperado:

```text
HTTP 400 Bad Request
```

El body debe contener `Debe proporcionar al menos un nombre y un apellido`.

### 7.10 Escenario 8 - Rechazar Rol Invalido

Body:

```json
{
  "nombre": "Diana Ruiz",
  "correo": "diana@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "ADMIN"
}
```

Resultado esperado:

```text
HTTP 400 Bad Request
```

El body debe contener `Rol no valido`.

### 7.11 Verificacion en Base de Datos

Despues de un registro exitoso, validar:

```sql
SELECT id, nombres, apellidos, correo, password_hash, estado_usuario_id
FROM public.tbl_usuario
WHERE correo = 'ana@mail.com';
```

Criterios:

1. `correo` debe estar en minusculas.
2. `password_hash` debe existir y no debe ser la contrasena en texto plano.
3. `nombres` debe contener la primera parte del nombre.
4. `apellidos` debe contener el resto del nombre.

Validar relacion de rol:

```sql
SELECT ur.usuario_id, ur.rol_id, r.codigo
FROM public.tbl_usuario_rol ur
JOIN public.tbl_rol r ON r.id = ur.rol_id
JOIN public.tbl_usuario u ON u.id = ur.usuario_id
WHERE u.correo = 'ana@mail.com';
```

Criterio esperado:

```text
codigo = CLIENTE
```
