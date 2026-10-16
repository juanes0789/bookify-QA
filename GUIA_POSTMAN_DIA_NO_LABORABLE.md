# Guía Postman: HU Días No Laborables

## 1. Requisitos

- Aplicación Bookify levantada y conectada a Supabase.
- Postman con un Environment activo.
- Un proveedor activo y una agenda de servicio activa vinculada a ese proveedor.
- Base URL local: `http://localhost:8080` (cámbiala si usas otro host/puerto; no agregues `/` al final).

Crea estas variables de Postman:

| Variable | Valor |
|---|---|
| `baseUrl` | `http://localhost:8080` (sin `/` al final) |
| `proveedorId` | UUID real de un proveedor en Supabase |
| `agendaServicioId` | UUID real de una agenda activa del mismo proveedor |

No uses UUID ficticios: el proveedor y la agenda deben existir y estar relacionados. Para obtener una pareja válida desde el SQL Editor de Supabase, ejecuta:

```sql
SELECT ag.id_agenda_servicio, s.id_proveedor,
       p.nombre_comercial, s.nombre AS servicio
FROM public.tbl_agenda_servicio ag
JOIN public.tbl_servicio_sede ss
  ON ss.id_servicio_sede = ag.id_servicio_sede
JOIN public.tbl_servicio s
  ON s.id_servicio = ss.id_servicio
JOIN public.tbl_proveedor p
  ON p.id_proveedor = s.id_proveedor
WHERE ag.activo = true
  AND s.activo = true
  AND p.activo = true
LIMIT 20;
```

Copia `id_proveedor` a `proveedorId` y el `id_agenda_servicio` de esa misma fila a `agendaServicioId`.

## 2. Registrar un día no laborable (201)

En Postman crea una request:

- Método: `POST`
- URL: `{{baseUrl}}/api/v1/proveedores/{{proveedorId}}/dias-no-laborables`
- Header: `Content-Type: application/json`
- Body → raw → JSON:

```json
{
  "fecha": "25/12/2026",
  "motivo": "Navidad - prueba HU-10"
}
```

El formato de fecha de la request es `dd/MM/yyyy`. La fecha `25/12/2026` es futura al momento de esta guía (14/10/2026). Si ejecutas la guía después de esa fecha, reemplázala por otra fecha futura en todos los pasos.

Resultado esperado: **201 Created** y un JSON con `id`, `proveedorId`, `fecha`, `motivo` y `fechaCreacion`. Guarda el valor `id` de la respuesta si quieres verificarlo en SQL.

### Verificar la fila en Supabase

Sustituye `<id-de-respuesta>` por el `id` devuelto por la API:

```sql
SELECT id_dia_no_laborable, id_proveedor, fecha,
       id, proveedor_id, motivo, fecha_creacion
FROM public.tbl_dia_no_laborable
WHERE id_dia_no_laborable = '<id-de-respuesta>'::uuid;
```

Debe existir una fila y cumplir `id_dia_no_laborable = id` e `id_proveedor = proveedor_id`.

## 3. Intentar registrar el mismo día (409)

Repite exactamente la request del paso 2 con el mismo proveedor y fecha.

Resultado esperado: **409 Conflict** y mensaje `La fecha ya está registrada como no laborable`.

## 4. Rechazar una fecha pasada (400)

Usa la misma URL y reemplaza el body por:

```json
{
  "fecha": "14/10/2026",
  "motivo": "Fecha pasada - prueba HU-10"
}
```

Resultado esperado: **400 Bad Request** y mensaje `La fecha debe ser igual o posterior a hoy` (considerando que hoy en Bogotá es 15/10/2026).

## 5. Rechazar un motivo de más de 100 caracteres (400)

Usa una fecha futura que no esté ya registrada para ese proveedor. Envía un motivo de 101 caracteres (en Postman puedes pegar 101 letras `A`):

```json
{
  "fecha": "26/12/2026",
  "motivo": "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA"
}
```

Resultado esperado: **400 Bad Request**. No se debe crear una fila para esa fecha.

## 6. Rechazar disponibilidad nueva en la fecha bloqueada (409)

Crea una request:

- Método: `POST`
- URL: `{{baseUrl}}/api/v1/disponibilidades` (debe resolverse a `http://localhost:8080/api/v1/disponibilidades`, con una sola `/` antes de `api`)
- Header: `Content-Type: application/json`
- Body → raw → JSON:

```json
{
  "agendaServicioId": "{{agendaServicioId}}",
  "inicioAt": "2026-12-25T10:00:00-05:00",
  "finAt": "2026-12-25T10:30:00-05:00"
}
```

Resultado esperado: **409 Conflict** y mensaje `La fecha está marcada como no laborable`. Usa una agenda activa que pertenezca al `proveedorId` del paso 2; la disponibilidad no debe insertarse.

## 7. Comprobar que el catálogo oculte los horarios de esa fecha

Crea una request:

- Método: `GET`
- URL: `{{baseUrl}}/api/v1/catalog/services/search`

Puedes añadir `texto` y/o `categoria` como Query Params para limitar resultados. Resultado esperado: **200 OK**. En `servicios[].availableSchedules[]`, no debe aparecer disponibilidad cuyo `inicioAt` corresponda al 25/12/2026 para ese proveedor. Los horarios existentes se conservan en la base de datos; el catálogo solo los omite.

También puedes consultar el catálogo general con `GET {{baseUrl}}/api/v1/catalog/services`.

## 8. Limpieza y repetición

La API no expone un endpoint para borrar días no laborables. Para repetir el flujo, usa otra fecha futura que todavía no esté registrada para el proveedor. No elimines filas manualmente en el entorno compartido.

## Fuera de esta HU implementada

No hay endpoint/caso de uso para crear reservas en el estado actual del proyecto, así que no es posible probar aún el rechazo de una reserva en Postman. Ese escenario se podrá agregar cuando exista el flujo de creación de reservas.

## 9. Solución de errores de URL

Si la respuesta incluye `path: "//api/v1/disponibilidades"` o `No static resource`, Postman está enviando una doble barra al inicio de la ruta. Edita el Environment y deja `baseUrl` sin barra final, por ejemplo `http://localhost:8080`; la URL de la request debe ser `{{baseUrl}}/api/v1/disponibilidades`. No uses `http://localhost:8080//api/...` ni `{{baseUrl}}//api/...`.

### Error 500: `column ase1_0.id does not exist`

Este error indicaba que `AgendaServicioEntity` generaba una consulta usando una columna `id` inexistente. La PK real de `tbl_agenda_servicio` es `id_agenda_servicio`; el mapeo ya fue corregido. Reinicia la aplicación para cargar la clase actualizada y vuelve a ejecutar la prueba 6 con la URL sin doble slash.

### Comprobación concluyente del ocultamiento (paso 7)

Primero comprueba en Supabase que realmente había una disponibilidad del proveedor para la fecha bloqueada. Reemplaza `<proveedorId>` por el mismo UUID usado en Postman:

```sql
SELECT
    p.nombre_comercial,
    s.nombre AS servicio,
    d.id_disponibilidad,
    d.inicio_at,
    (d.inicio_at AT TIME ZONE 'America/Bogota')::date AS fecha_bogota,
    ed.codigo AS estado
FROM public.tbl_disponibilidad d
JOIN public.tbl_agenda_servicio ag
  ON ag.id_agenda_servicio = d.id_agenda_servicio
JOIN public.tbl_servicio_sede ss
  ON ss.id_servicio_sede = ag.id_servicio_sede
JOIN public.tbl_servicio s
  ON s.id_servicio = ss.id_servicio
JOIN public.tbl_proveedor p
  ON p.id_proveedor = s.id_proveedor
JOIN public.tbl_estado_disponibilidad ed
  ON ed.id_estado_disponibilidad = d.id_estado_disponibilidad
WHERE p.id_proveedor = '<proveedorId>'::uuid
  AND (d.inicio_at AT TIME ZONE 'America/Bogota')::date = DATE '2026-12-25';
```

Si la consulta devuelve una fila, guarda su `id_disponibilidad` y confirma que el proveedor es el mismo del paso 2. En Postman vuelve a enviar `GET {{baseUrl}}/api/v1/catalog/services/search` y busca ese UUID en todos los elementos `servicios[].availableSchedules[].availabilityId`. La prueba pasa si esa disponibilidad existe en Supabase pero su UUID no aparece en la respuesta del catálogo. Que la respuesta contenga otros horarios, por ejemplo de otra fecha, es normal.

Si la consulta no devuelve filas, la salida actual del catálogo no demuestra el ocultamiento: no había una disponibilidad que ocultar para esa fecha. Para comprobarlo con otro día, primero crea un horario para una fecha futura libre y verifica que aparece en el catálogo; después registra ese día como no laborable y repite el GET. El mismo `availabilityId` debe dejar de aparecer, mientras la fila sigue en Supabase.
