# Plan de Implementación: Registro de Clientes y Proveedores

## 1. Información General y Alcance de la Historia de Usuario (HU)

### Definición de la HU
> **Como** visitante,  
> **quiero** registrarme como cliente o como proveedor,  
> **para** acceder a las funciones de la plataforma según mi rol.

### Reglas de Negocio y Seguridad
* **Política de Contraseñas (NIST SP 800-63B rev. 4):**
  * Longitud obligatoria de 15 a 64 caracteres inclusive (`15 <= longitud <= 64`).
  * Sin reglas de composición impuestas (no forzar mezclas de mayúsculas, minúsculas, números o caracteres especiales).
  * Validada contra una lista negra de contraseñas comunes o comprometidas.
  * No debe contener el correo del usuario (comparación insensible a mayúsculas/minúsculas).
  * No debe contener la palabra `"bookify"` (comparación insensible a mayúsculas/minúsculas).
* **Criptografía:**
  * Almacenada exclusivamente con hash **BCrypt** (cost factor 10 o 12).
  * La contraseña en texto plano o el hash nunca se devuelven en respuestas ni registros de log.
* **Correo Electrónico:**
  * Normalizado y persistido en minúsculas (`trim().toLowerCase()`).
  * Único sin distinción de mayúsculas y minúsculas (insensible a mayúsculas).
  * Ante colisión de correo, respuesta HTTP `409 Conflict` con el mensaje exacto: `"El correo ya está registrado"`.
* **Estado de la Cuenta:**
  * Se crea activa (`activo = true`, asociado al estado `'ACTIVO'`).
  * La verificación por correo queda explícitamente fuera del alcance de este sprint.
* **Roles Válidos:**
  * `CLIENTE` o `PROVEEDOR`.

---

## 2. Escenarios de Aceptación (Gherkin)

```gherkin
Scenario: Registrar una cuenta de cliente
  Given que no existe una cuenta con el correo "ana@mail.com"
  When envío nombre "Ana Gómez", correo "ana@mail.com", una contraseña válida y el rol "CLIENTE"
  Then el sistema responde con código 201
  And crea la cuenta con el rol "CLIENTE"
  And la respuesta incluye id, nombre, correo y rol, sin la contraseña

Scenario: Registrar una cuenta de proveedor
  Given que no existe una cuenta con el correo "carlos@barberia.com"
  When envío nombre, correo, una contraseña válida y el rol "PROVEEDOR"
  Then el sistema responde con código 201
  And crea la cuenta con el rol "PROVEEDOR"

Scenario: Rechazar un correo ya registrado
  Given que ya existe una cuenta con el correo "ana@mail.com"
  When intento registrarme con el correo "ANA@mail.com"
  Then el sistema responde con código 409
  And el mensaje "El correo ya está registrado"

Scenario Outline: Rechazar una contraseña que no cumple la política
  Given que no tengo una cuenta registrada
  When intento registrarme con la contraseña <contrasena>
  Then el sistema responde con código 400
  And el mensaje indica <motivo>

  Examples:
    | contrasena            | motivo                                   |
    | "corta123"            | "debe tener entre 15 y 64 caracteres"    |
    | una de 65 caracteres  | "debe tener entre 15 y 64 caracteres"    |
    | "contraseña123456"    | "es una contraseña común o comprometida" |
    | "ana@mail.com-2026"   | "no puede contener tu correo"            |
```

---

## 3. Análisis de Mapeo contra el Schema de Base de Datos Real

### Estructura de Tablas Involucradas

1. **`tbl_usuario`:**
   * `id`: `uuid NOT NULL DEFAULT gen_random_uuid()` (Primary Key).
   * `estado_usuario_id`: `integer NOT NULL` (Foreign Key a `tbl_estado_usuario`).
   * `nombres`: `varchar NOT NULL` $\rightarrow$ Primer nombre o componentes iniciales del nombre enviado.
   * `apellidos`: `varchar NOT NULL` $\rightarrow$ Apellidos resultantes de la división del nombre completo.
   * `correo`: `varchar NOT NULL` $\rightarrow$ Almacenado en minúsculas.
   * `password_hash`: `varchar NOT NULL` $\rightarrow$ Hash BCrypt generado.
   * `tipo_identificacion_id`, `numero_identificacion`, `telefono`: Campos `NULLABLE` (quedan en `null` durante el registro inicial).
   * `fecha_creacion`, `fecha_actualizacion`: Timestamps UTC con zona horaria.

2. **`tbl_estado_usuario`:**
   * Catálogo de estados con columnas `id`, `codigo`, `nombre`, `descripcion`.
   * Se consulta el registro con `codigo = 'ACTIVO'` para asignar el `estado_usuario_id` requerido.

3. **`tbl_rol`:**
   * Catálogo de roles con columnas `id`, `codigo`, `nombre`, `descripcion`.
   * Registros esperados: `codigo = 'CLIENTE'` y `codigo = 'PROVEEDOR'`.

4. **`tbl_usuario_rol`:**
   * Tabla intermedia M:N con columnas `usuario_id`, `rol_id`, `fecha_asignacion`.
   * Permite asociar al usuario recién creado con su rol respectivo.

5. **`tbl_reserva` y `tbl_proveedor`:**
   * `tbl_reserva.cliente_id` referencia directamente a `tbl_usuario(id)`. No existe tabla `tbl_cliente`, el cliente es un usuario con rol `CLIENTE`.
   * La tabla `tbl_proveedor` representa a la entidad comercial y sus sedes/servicios. La creación de la cuenta con rol `PROVEEDOR` establece la identidad del usuario en `tbl_usuario` y `tbl_usuario_rol`, dejando el alta de los datos comerciales para el módulo de onboarding respectivo.

---

## 4. Diseño de Arquitectura Hexagonal

```
src/main/java/udea/fabrica/bookify/
├── domain/                                  <-- Reglas de negocio puras (sin Spring)
│   ├── model/
│   │   ├── User.java                        <-- Modelo de dominio
│   │   └── UserRole.java                    <-- Enum: CLIENTE, PROVEEDOR
│   ├── port/
│   │   ├── in/
│   │   │   └── RegisterUserInputPort.java   <-- Puerto de entrada (Caso de uso)
│   │   └── out/
│   │       ├── UserRepositoryPort.java      <-- Guardar usuario y consultar unicidad de correo
│   │       ├── PasswordEncoderPort.java     <-- Cifrado seguro BCrypt
│   │       └── CommonPasswordPort.java      <-- Validación de contraseñas de lista negra
│   ├── service/
│   │   └── RegisterUserService.java         <-- Orquestación y reglas de negocio puras
│   └── exception/
│       ├── EmailAlreadyExistsException.java <-- Excepción para HTTP 409
│       └── InvalidPasswordException.java    <-- Excepción para HTTP 400
│
└── infrastructure/                          <-- Adaptadores y Frameworks
    ├── adapter/
    │   ├── input/rest/
    │   │   ├── controller/
    │   │   │   └── UserController.java      <-- POST /api/v1/auth/register
    │   │   ├── dto/
    │   │   │   ├── request/RegisterUserRequestDto.java
    │   │   │   └── response/UserResponseDto.java
    │   │   └── mapper/
    │   │       └── UserRestMapper.java
    │   │
    │   └── output/
    │       ├── persistence/
    │       │   ├── entity/
    │       │   │   ├── UserEntity.java              <-- @Table(name = "tbl_usuario")
    │       │   │   ├── EstadoUsuarioEntity.java     <-- @Table(name = "tbl_estado_usuario")
    │       │   │   ├── RolEntity.java               <-- @Table(name = "tbl_rol")
    │       │   │   └── UsuarioRolEntity.java        <-- @Table(name = "tbl_usuario_rol")
    │       │   ├── repository/
    │       │   │   ├── UserJpaRepository.java
    │       │   │   ├── EstadoUsuarioJpaRepository.java
    │       │   │   ├── RolJpaRepository.java
    │       │   │   └── UsuarioRolJpaRepository.java
    │       │   ├── mapper/
    │       │   │   └── UserEntityMapper.java
    │       │   └── adapter/
    │       │       └── UserPersistenceAdapter.java  <-- Implementa UserRepositoryPort (@Transactional)
    │       └── security/
    │           ├── BcryptPasswordEncoderAdapter.java    <-- Implementa PasswordEncoderPort
    │           └── ResourceCommonPasswordAdapter.java   <-- Implementa CommonPasswordPort
    │
    └── config/
        ├── BeanConfiguration.java                   <-- Registro del Service y Adaptadores como Beans
        └── GlobalExceptionHandler.java               <-- Captura de excepciones y mapeo de HTTP status
```

---

## 5. Fases de Implementación Paso a Paso

### Fase 1: Dependencias y Recursos
1. **Configuración de Gradle (`build.gradle`):**
   * Añadir dependencia ligera de hashing:
     ```groovy
     implementation 'org.springframework.security:spring-security-crypto'
     ```
     *(Evita incluir todo el filtro de seguridad de Spring Security para no bloquear endpoints no autenticados existentes).*
2. **Diccionario de Contraseñas Comunes:**
   * Crear archivo `src/main/resources/passwords/common-passwords.txt` con las contraseñas comunes más frecuentes (incluyendo `"contraseña123456"`, `"password123456"`, `"123456789012345"`, etc.).

### Fase 2: Capa de Dominio (Núcleo Puro)
1. **Crear `UserRole.java`:**
   * Enumeración: `CLIENTE`, `PROVEEDOR`.
2. **Crear `User.java`:**
   * Atributos: `UUID id`, `String nombres`, `String apellidos`, `String correo`, `String passwordHash`, `UserRole rol`, `boolean activo`, `OffsetDateTime fechaCreacion`, `OffsetDateTime fechaActualizacion`.
   * Método de conveniencia `getNombreCompleto()`: Retorna `(nombres + " " + apellidos).trim()`.
3. **Crear Excepciones de Dominio:**
   * `EmailAlreadyExistsException`: Mensaje `"El correo ya está registrado"`.
   * `InvalidPasswordException`: Mensaje con el motivo de falla.
4. **Definir Puertos:**
   * `RegisterUserInputPort`: `User register(String nombre, String correo, String contrasena, String rol)`.
   * `UserRepositoryPort`:
     * `boolean existsByEmail(String email);`
     * `User save(User user);`
   * `PasswordEncoderPort`: `String encode(String rawPassword);`
   * `CommonPasswordPort`: `boolean isCommon(String password);`
5. **Implementar `RegisterUserService.java`:**
   * **Paso 5.1:** Validar rol: si no es `CLIENTE` ni `PROVEEDOR`, lanzar `IllegalArgumentException("Rol no válido")`.
   * **Paso 5.2:** Validar longitud de contraseña: si longitud $< 15$ o $> 64$, lanzar `InvalidPasswordException("debe tener entre 15 y 64 caracteres")`.
   * **Paso 5.3:** Validar que la contraseña no contenga el correo (insensible a mayúsculas/minúsculas): si contiene el correo, lanzar `InvalidPasswordException("no puede contener tu correo")`.
   * **Paso 5.4:** Validar que la contraseña no contenga la palabra `"bookify"`: si la contiene, lanzar `InvalidPasswordException("no puede contener la palabra 'bookify'")`.
   * **Paso 5.5:** Validar lista negra: consultar `commonPasswordPort.isCommon(contrasena)`. Si coincide, lanzar `InvalidPasswordException("es una contraseña común o comprometida")`.
   * **Paso 5.6:** Normalizar correo: `String emailNormalizado = correo.trim().toLowerCase();`.
   * **Paso 5.7:** Validar unicidad: si `userRepositoryPort.existsByEmail(emailNormalizado)` es verdadero, lanzar `EmailAlreadyExistsException("El correo ya está registrado")`.
   * **Paso 5.8:** Separar nombres y apellidos:
     ```java
     String[] partes = nombre.trim().split("\\s+", 2);
     String nombres = partes[0];
     String apellidos = partes.length > 1 ? partes[1] : "";
     ```
   * **Paso 5.9:** Generar hash con `passwordEncoderPort.encode(contrasena)`.
   * **Paso 5.10:** Construir entidad de dominio con `activo = true` y guardar mediante `userRepositoryPort.save(...)`.

### Fase 3: Capa de Infraestructura - Persistencia
1. **Entidades JPA:**
   * `EstadoUsuarioEntity`: Mapeo de `tbl_estado_usuario` (`id`, `codigo`, `nombre`).
   * `RolEntity`: Mapeo de `tbl_rol` (`id`, `codigo`, `nombre`).
   * `UserEntity`: Mapeo de `tbl_usuario`:
     * `@Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;`
     * `@Column(name = "estado_usuario_id") private Integer estadoUsuarioId;`
     * `@Column(name = "nombres") private String nombres;`
     * `@Column(name = "apellidos") private String apellidos;`
     * `@Column(name = "correo") private String correo;`
     * `@Column(name = "password_hash") private String passwordHash;`
     * `@Column(name = "fecha_creacion") private OffsetDateTime fechaCreacion;`
     * `@Column(name = "fecha_actualizacion") private OffsetDateTime fechaActualizacion;`
   * `UsuarioRolEntity`: Mapeo de `tbl_usuario_rol` con `@EmbeddedId` (`usuario_id`, `rol_id`) y `@Column(name = "fecha_asignacion")`.
2. **Repositorios JPA:**
   * `UserJpaRepository`: `boolean existsByCorreoIgnoreCase(String correo);`
   * `EstadoUsuarioJpaRepository`: `Optional<EstadoUsuarioEntity> findByCodigo(String codigo);`
   * `RolJpaRepository`: `Optional<RolEntity> findByCodigo(String codigo);`
   * `UsuarioRolJpaRepository`: Interfaz Spring Data para `UsuarioRolEntity`.
3. **Adaptador de Persistencia (`UserPersistenceAdapter.java`):**
   * Anotado con `@Component` y `@Transactional`.
   * Resuelve `estadoUsuarioRepository.findByCodigo("ACTIVO")` para obtener `estado_usuario_id`.
   * Resuelve `rolRepository.findByCodigo(user.getRol().name())` para obtener `rol_id`.
   * Guarda `UserEntity` en `tbl_usuario`.
   * Guarda la relación en `tbl_usuario_rol`.
   * Retorna el modelo de dominio `User`.

### Fase 4: Capa de Infraestructura - Adaptadores de Seguridad
1. **`BcryptPasswordEncoderAdapter.java`:**
   * Implementa `PasswordEncoderPort` delegando a `BCryptPasswordEncoder`.
2. **`ResourceCommonPasswordAdapter.java`:**
   * Implementa `CommonPasswordPort`.
   * Carga el archivo `common-passwords.txt` en memoria en un `HashSet<String>` en minúsculas al iniciar la aplicación para consultas en tiempo $O(1)$.

### Fase 5: Capa de Infraestructura - Adaptadores REST
1. **DTOs:**
   * `RegisterUserRequestDto`:
     * `@NotBlank private String nombre;`
     * `@NotBlank @Email private String correo;`
     * `@NotBlank private String contrasena;`
     * `@NotBlank private String rol;`
   * `UserResponseDto`:
     * `UUID id;`
     * `String nombre;`
     * `String correo;`
     * `String rol;`
     *(Excluye cualquier campo de contraseña o hash).*
2. **Mapper REST (`UserRestMapper.java`):**
   * Mapea entre DTO y dominio, componiendo `nombre = (nombres + " " + apellidos).trim()`.
3. **Controlador REST (`UserController.java`):**
   * Anotado con `@RestController` y `@RequestMapping("/api/v1/auth")`.
   * Endpoint: `@PostMapping("/register")` que recibe `@Valid @RequestBody RegisterUserRequestDto`.
   * Retorna `ResponseEntity.status(HttpStatus.CREATED).body(responseDto)`.

### Fase 6: Configuración y Manejo de Excepciones
1. **`BeanConfiguration.java`:**
   * Registrar `@Bean public RegisterUserInputPort registerUserInputPort(...)` inyectando `userRepositoryPort`, `passwordEncoderPort` y `commonPasswordPort`.
2. **`GlobalExceptionHandler.java`:**
   * Handler para `EmailAlreadyExistsException`:
     * Retorna HTTP `409 CONFLICT`.
     * Respuesta: `Map.of("error", ex.getMessage(), "mensaje", ex.getMessage())`.
   * Handler para `InvalidPasswordException`:
     * Retorna HTTP `400 BAD REQUEST`.
     * Respuesta: `Map.of("error", ex.getMessage(), "motivo", ex.getMessage(), "mensaje", ex.getMessage())`.

---

## 6. Matriz de Cobertura de Escenarios Gherkin

| Escenario Gherkin | Petición / Datos | Código HTTP y Comportamiento | Validación Técnica |
| :--- | :--- | :--- | :--- |
| **Registrar cuenta de cliente** | `nombre`: "Ana Gómez"<br>`correo`: "ana@mail.com"<br>`contrasena`: (válida $\ge 15$ caracteres)<br>`rol`: "CLIENTE" | **201 Created**<br>Body: `{ id, nombre, correo, rol }` | Se crea registro en `tbl_usuario` (`nombres` = "Ana", `apellidos` = "Gómez", `estado_usuario_id` = ACTIVO) y en `tbl_usuario_rol` (`rol_id` = CLIENTE). La respuesta omite el hash. |
| **Registrar cuenta de proveedor** | `nombre`: "Carlos Barbería"<br>`correo`: "carlos@barberia.com"<br>`contrasena`: (válida $\ge 15$ caracteres)<br>`rol`: "PROVEEDOR" | **201 Created**<br>Body: `{ id, nombre, correo, rol }` | Se crea registro en `tbl_usuario` y en `tbl_usuario_rol` (`rol_id` = PROVEEDOR). |
| **Rechazar correo ya registrado** | `correo`: "ANA@mail.com"<br>(existe "ana@mail.com") | **409 Conflict**<br>Mensaje: `"El correo ya está registrado"` | `existsByCorreoIgnoreCase` detecta el correo; lanza `EmailAlreadyExistsException`. |
| **Contraseña corta (< 15)** | `contrasena`: "corta123" | **400 Bad Request**<br>Motivo: `"debe tener entre 15 y 64 caracteres"` | Validación de longitud menor a 15 caracteres. |
| **Contraseña larga (> 64)** | `contrasena`: (cadena de 65 caracteres) | **400 Bad Request**<br>Motivo: `"debe tener entre 15 y 64 caracteres"` | Validación de longitud mayor a 64 caracteres. |
| **Contraseña común** | `contrasena`: "contraseña123456" | **400 Bad Request**<br>Motivo: `"es una contraseña común o comprometida"` | Interceptada por `ResourceCommonPasswordAdapter` contra `common-passwords.txt`. |
| **Contraseña contiene correo** | `correo`: "ana@mail.com"<br>`contrasena`: "ana@mail.com-2026" | **400 Bad Request**<br>Motivo: `"no puede contener tu correo"` | `password.toLowerCase().contains(email.toLowerCase())`. |

---

## 7. Plan de Pruebas Automatizadas

1. **Pruebas Unitarias de Dominio (`RegisterUserServiceTest`):**
   * Cobertura del 100% de reglas de contraseña NIST (longitud mínima, longitud máxima, correo contenido, "bookify" contenido, contraseña común).
   * Verificación de la normalización del correo a minúsculas.
   * Verificación del rechazo por correo duplicado.
   * Verificación de llamada al puerto de cifrado BCrypt (la contraseña nunca llega en plano al repositorio).
2. **Pruebas del Adaptador de Seguridad (`ResourceCommonPasswordAdapterTest`):**
   * Validación de detección efectiva en la lista cargada desde recursos.
3. **Pruebas de Integración Web (`UserControllerTest` con `MockMvc`):**
   * Verificación de código de estado `201` y esquema JSON de respuesta sin campos de contraseña.
   * Verificación de código de estado `409` con cuerpo que contiene `"El correo ya está registrado"`.
   * Verificación de código de estado `400` con cuerpo que contiene los motivos respectivos.
4. **Pruebas de Persistencia (`UserPersistenceAdapterTest`):**
   * Inserción coordinada en `tbl_usuario` y `tbl_usuario_rol` bajo `@Transactional`.
