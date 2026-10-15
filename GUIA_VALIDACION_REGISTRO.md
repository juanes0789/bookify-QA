# Guía de Pruebas y Validación: Registro de Clientes y Proveedores

Esta guía describe el procedimiento paso a paso para verificar el funcionamiento de la Historia de Usuario de **Registro de Clientes y Proveedores** (`POST /api/v1/auth/register`), incluyendo configuración previa, payloads de prueba, códigos de respuesta HTTP y consultas de verificación en base de datos.

---

## 1. Prerrequisitos de Ejecución

### 1.1 Base de Datos PostgreSQL
Asegúrate de tener una instancia de PostgreSQL en ejecución y que las credenciales en tu archivo `.env` o `src/main/resources/application.properties` sean correctas:
* `SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/bookify`
* `SPRING_DATASOURCE_USERNAME=tu_usuario`
* `SPRING_DATASOURCE_PASSWORD=tu_password`

### 1.2 Catálogos Semilla
La aplicación incluye el archivo `src/main/resources/data.sql` que inicializa los catálogos requeridos si las tablas existen. Si necesitas cargarlos manualmente, ejecuta en PostgreSQL:

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

### 1.3 Iniciar la Aplicación
En la raíz del proyecto, ejecuta en la terminal:

```bash
# En Windows:
./gradlew.bat bootRun

# En Linux/macOS:
./gradlew bootRun
```

La API estará disponible en:
```text
http://localhost:8080/api/v1/auth/register
```

---

## 2. Configuración en Postman / Thunder Client / Insomnia / cURL

* **Método:** `POST`
* **URL:** `http://localhost:8080/api/v1/auth/register`
* **Headers:**
  ```http
  Content-Type: application/json
  ```

---

## 3. Matriz de Casos de Prueba

### Caso 1: Registro Exitoso de Cliente (Rol `CLIENTE`)
* **Propósito:** Verificar que un visitante puede registrarse exitosamente como cliente con datos válidos.
* **Request Body:**
```json
{
  "nombre": "Ana Gómez",
  "correo": "ana@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `201 Created`
  * **Body:**
  ```json
  {
    "id": "<UUID-GENERADO>",
    "nombre": "Ana Gómez",
    "correo": "ana@mail.com",
    "rol": "CLIENTE"
  }
  ```
  > **Nota de Seguridad:** La respuesta **NO** debe contener los campos `contrasena` ni `passwordHash`.

---

### Caso 2: Registro Exitoso de Proveedor (Rol `PROVEEDOR`)
* **Propósito:** Verificar el registro exitoso de una cuenta de proveedor.
* **Request Body:**
```json
{
  "nombre": "Carlos Barbería",
  "correo": "carlos@barberia.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "PROVEEDOR"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `201 Created`
  * **Body:**
  ```json
  {
    "id": "<UUID-GENERADO>",
    "nombre": "Carlos Barbería",
    "correo": "carlos@barberia.com",
    "rol": "PROVEEDOR"
  }
  ```

---

### Caso 3: Rechazo por Correo Duplicado (Insensible a Mayúsculas)
* **Propósito:** Verificar que no se permita registrar un correo ya existente, incluso si cambia mayúsculas/minúsculas.
* **Precondición:** El usuario del Caso 1 (`ana@mail.com`) ya debe estar registrado.
* **Request Body:**
```json
{
  "nombre": "Ana Sofía Gómez",
  "correo": "ANA@MAIL.COM",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `409 Conflict`
  * **Body:**
  ```json
  {
    "error": "El correo ya está registrado",
    "mensaje": "El correo ya está registrado"
  }
  ```

---

### Caso 4: Rechazo por Contraseña Corta (< 15 Caracteres)
* **Propósito:** Validar la regla NIST SP 800-63B rev. 4 de longitud mínima (15 caracteres).
* **Request Body:**
```json
{
  "nombre": "Pedro Pérez",
  "correo": "pedro.perez@mail.com",
  "contrasena": "corta123",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "debe tener entre 15 y 64 caracteres",
    "motivo": "debe tener entre 15 y 64 caracteres",
    "mensaje": "debe tener entre 15 y 64 caracteres"
  }
  ```

---

### Caso 5: Rechazo por Contraseña Larga (> 64 Caracteres)
* **Propósito:** Validar la regla NIST de longitud máxima (64 caracteres).
* **Request Body:**
```json
{
  "nombre": "Pedro Pérez",
  "correo": "pedro.perez@mail.com",
  "contrasena": "PasswordExtremadamenteLargaSuperandoLosSesentaYCuatroCaracteresNIST1234",
  "rol": "CLIENTE"
}
```
*(Contraseña de 71 caracteres).*
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "debe tener entre 15 y 64 caracteres",
    "motivo": "debe tener entre 15 y 64 caracteres",
    "mensaje": "debe tener entre 15 y 64 caracteres"
  }
  ```

---

### Caso 6: Rechazo por Contraseña Común o Comprometida (Lista Negra)
* **Propósito:** Verificar el bloqueo de contraseñas vulnerables cargadas desde `common-passwords.txt`.
* **Request Body:**
```json
{
  "nombre": "Luis Suárez",
  "correo": "luis.suarez@mail.com",
  "contrasena": "contraseña123456",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "es una contraseña común o comprometida",
    "motivo": "es una contraseña común o comprometida",
    "mensaje": "es una contraseña común o comprometida"
  }
  ```

---

### Caso 7: Rechazo por Contraseña que Contiene el Correo Electrónico
* **Propósito:** Validar que la contraseña no incluya el correo del usuario (insensible a mayúsculas/minúsculas).
* **Request Body:**
```json
{
  "nombre": "Laura Ruiz",
  "correo": "laura.ruiz@mail.com",
  "contrasena": "laura.ruiz@mail.com-2026",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "no puede contener tu correo",
    "motivo": "no puede contener tu correo",
    "mensaje": "no puede contener tu correo"
  }
  ```

---

### Caso 8: Rechazo por Contraseña que Contiene la Palabra "bookify"
* **Propósito:** Validar que la contraseña no contenga el nombre de la plataforma.
* **Request Body:**
```json
{
  "nombre": "Diana Morales",
  "correo": "diana.morales@mail.com",
  "contrasena": "BookifySegura2026!",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "no puede contener la palabra 'bookify'",
    "motivo": "no puede contener la palabra 'bookify'",
    "mensaje": "no puede contener la palabra 'bookify'"
  }
  ```

---

### Caso 9: Rechazo por Nombre de una Sola Palabra
* **Propósito:** Validar que el usuario ingrese al menos un nombre y un apellido para satisfacer las columnas `nombres` y `apellidos` en base de datos.
* **Request Body:**
```json
{
  "nombre": "Diana",
  "correo": "diana2@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "CLIENTE"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "Debe proporcionar al menos un nombre y un apellido"
  }
  ```

---

### Caso 10: Rechazo por Rol Inválido
* **Propósito:** Validar que solo se permitan los roles `CLIENTE` o `PROVEEDOR`.
* **Request Body:**
```json
{
  "nombre": "Administrador General",
  "correo": "admin@mail.com",
  "contrasena": "MiContrasenaSegura2026!",
  "rol": "ADMIN"
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`
  * **Body:**
  ```json
  {
    "error": "Rol no válido"
  }
  ```

---

### Caso 11: Rechazo por Validación de Formato de Correo y Campos Vacíos
* **Propósito:** Validar la anotación `@Valid`, `@NotBlank` y `@Email` del DTO.
* **Request Body:**
```json
{
  "nombre": "",
  "correo": "correo-no-valido",
  "contrasena": "",
  "rol": ""
}
```
* **Respuesta Esperada:**
  * **Código HTTP:** `400 Bad Request`

---

## 4. Verificación Directa en Base de Datos PostgreSQL

Tras registrar exitosamente a los usuarios de los Casos 1 y 2, ejecuta las siguientes consultas en PostgreSQL para validar la integridad de los datos persistidos:

### 4.1 Verificar la Tabla `tbl_usuario`
```sql
SELECT 
    id_usuario, 
    nombres, 
    apellidos, 
    correo, 
    password_hash, 
    id_estado_usuario,
    fecha_creacion,
    fecha_actualizacion
FROM public.tbl_usuario
WHERE correo IN ('ana@mail.com', 'carlos@barberia.com');
```

**Criterios de Aceptación a verificar:**
1. **`correo`:** Debe estar normalizado en minúsculas (`ana@mail.com`).
2. **`password_hash`:** Debe comenzar con `$2a$` o `$2b$` (hash BCrypt válido) y nunca ser la contraseña en texto plano.
3. **`nombres` y `apellidos`:**
   * Para "Ana Gómez": `nombres = 'Ana'`, `apellidos = 'Gómez'`.
   * Para "Carlos Barbería": `nombres = 'Carlos'`, `apellidos = 'Barbería'`.
4. **`id_estado_usuario`:** Debe coincidir con el valor de `id_estado_usuario` del registro con código `'ACTIVO'` en `tbl_estado_usuario`.

---

### 4.2 Verificar la Relación de Roles en `tbl_usuario_rol`
```sql
SELECT 
    u.correo,
    r.codigo AS rol_codigo,
    r.nombre AS rol_nombre,
    ur.fecha_asignacion
FROM public.tbl_usuario_rol ur
JOIN public.tbl_usuario u ON u.id_usuario = ur.id_usuario
JOIN public.tbl_rol r ON r.id_rol = ur.id_rol
WHERE u.correo IN ('ana@mail.com', 'carlos@barberia.com');
```

**Criterios de Aceptación a verificar:**
* Para `ana@mail.com`, `rol_codigo` debe ser **`CLIENTE`**.
* Para `carlos@barberia.com`, `rol_codigo` debe ser **`PROVEEDOR`**.
* `fecha_asignacion` debe tener timestamp no nulo.

---

## 5. Ejecución Rápida de Pruebas Automatizadas

Si deseas ejecutar todas las pruebas unitarias y de integración asociadas a esta historia de usuario sin levantar el servidor manualmente, ejecuta:

```bash
# Solo pruebas asociadas a la HU de Registro (34 pruebas en total):
./gradlew.bat test --no-daemon --tests "*RegisterUser*" --tests "*UserPersistence*" --tests "*UserController*" --tests "*ResourceCommonPassword*"

# Toda la suite de pruebas del proyecto:
./gradlew.bat test --no-daemon
```
