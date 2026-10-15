# Guía Postman: HU Búsqueda de Servicios

Esta guía prueba el endpoint público `GET /api/v1/catalog/services/search` por nombre de servicio, nombre comercial del proveedor y categoría. Incluye los resultados esperados, verificaciones Postman y el conjunto de datos que debe existir en la base de pruebas.

## 1. Requisitos y configuración

- Aplicación Bookify levantada y conectada a una base PostgreSQL de pruebas.
- La extensión `unaccent` habilitada en esa base:

```sql
CREATE EXTENSION IF NOT EXISTS unaccent;
```

- Postman con un Environment activo. Define estas variables:

| Variable | Valor |
|---|---|
| `baseUrl` | `http://localhost:8080` (ajusta host o puerto si aplica) |
| `textoSinResultados` | `HU-BUSQUEDA-SIN-RESULTADOS-20261015` |
| `categoriaInexistente` | `HU-CATEGORIA-INEXISTENTE-20261015` |

El endpoint es público; no agregues el header `Authorization`.

## 2. Datos de prueba requeridos

Para que los resultados sean deterministas, prepara el conjunto siguiente en una **base aislada de pruebas**. Usa los nombres y categorías indicados exactamente. Cada servicio positivo debe tener al menos una disponibilidad futura con estado `DISPONIBLE` y un recurso asignado a su agenda.

| Proveedor activo | Categoría comercial | Servicios activos con disponibilidad libre |
|---|---|---|
| `HU Barbería Central` | `Barbería y belleza` | `HU Corte de cabello`, `HU Corte de barba`, `HU Fútbol recreativo` |
| `HU Complejo El Estadio` | `Deportes y recreación` | `HU Fútbol 5`, `HU Fútbol infantil` |

Para verificar que se excluyen horarios, prepara además estos casos para el proveedor `HU Complejo El Estadio`:

- Una disponibilidad con estado `DISPONIBLE`, pero vinculada a una reserva `CONFIRMADA`.
- Una disponibilidad `DISPONIBLE` en una fecha que esté registrada como día no laborable del proveedor.
- Un servicio inactivo con disponibilidad `DISPONIBLE`.
- Una relación agenda-servicio inactiva con disponibilidad `DISPONIBLE`.

Asigna recursos con nombres distinguibles, por ejemplo `HU Barbero Carlos` y `HU Cancha 1`. Para las solicitudes de búsqueda normal, crea disponibilidades libres que no estén ocupadas y no caigan en días no laborables.

**Importante:** el repositorio no contiene el DDL completo ni todos los campos obligatorios de estas tablas. Por eso no incluyo `INSERT` inventados que podrían fallar o escribir datos incorrectos. Prepara estas filas con las migraciones/fixtures de tu base de pruebas o con el mecanismo de carga que use el proyecto. No cargues el dataset en producción.

### Consultar ofertas disponibles y copiar datos existentes

Si ya hay datos adecuados en la base, ejecuta esta consulta en Supabase SQL Editor o en tu cliente PostgreSQL para inspeccionar nombres, categorías, horarios y recursos disponibles. Usa los IDs reales devueltos; la consulta aplica las relaciones y nombres de columnas usados actualmente por el repositorio de búsqueda.

```sql
SELECT
    s.id_servicio,
    s.nombre AS servicio,
    p.id_proveedor,
    p.nombre_comercial AS proveedor,
    tp.nombre AS categoria,
    ag.id_agenda_servicio,
    a.id_agenda,
    d.id_disponibilidad,
    d.inicio_at,
    d.fin_at,
    r.nombre AS recurso,
    ed.codigo AS estado_disponibilidad
FROM public.tbl_servicio s
JOIN public.tbl_proveedor p
  ON p.id_proveedor = s.id_proveedor
JOIN public.tbl_tipo_proveedor tp
  ON tp.id_tipo_proveedor = p.id_tipo_proveedor
JOIN public.tbl_servicio_sede ss
  ON ss.id_servicio = s.id_servicio
JOIN public.tbl_agenda_servicio ag
  ON ag.id_servicio_sede = ss.id_servicio_sede
JOIN public.tbl_agenda a
  ON a.id_agenda = ag.id_agenda
JOIN public.tbl_agenda_recurso ar
  ON ar.id_agenda = a.id_agenda
JOIN public.tbl_recurso r
  ON r.id_recurso = ar.id_recurso
JOIN public.tbl_disponibilidad d
  ON d.id_agenda_servicio = ag.id_agenda_servicio
JOIN public.tbl_estado_disponibilidad ed
  ON ed.id_estado_disponibilidad = d.id_estado_disponibilidad
WHERE s.activo = true
  AND p.activo = true
  AND ag.activo = true
  AND upper(ed.codigo) = 'DISPONIBLE'
  AND NOT EXISTS (
      SELECT 1
      FROM public.tbl_ocupacion_disponibilidad od
      JOIN public.tbl_reserva res
        ON res.id_reserva = od.id_reserva
      JOIN public.tbl_estado_reserva er
        ON er.id_estado_reserva = res.id_estado_reserva
      WHERE od.id_disponibilidad = d.id_disponibilidad
        AND upper(er.codigo) LIKE 'CONFIRMAD%'
  )
  AND NOT EXISTS (
      SELECT 1
      FROM public.tbl_dia_no_laborable dnl
      WHERE dnl.id_proveedor = p.id_proveedor
        AND dnl.fecha = CAST(d.inicio_at AT TIME ZONE 'America/Bogota' AS date)
  )
ORDER BY tp.nombre, p.nombre_comercial, s.nombre, d.inicio_at
LIMIT 100;
```

Si esta consulta falla por nombres de columnas, confirma el DDL actual antes de continuar: el repositorio no contiene el esquema completo y esta consulta replica los nombres que usa la implementación.

## 3. Crear una colección y enviar solicitudes

Usa **Params** en Postman para los query params. Todas las solicitudes usan:

```http
GET {{baseUrl}}/api/v1/catalog/services/search
```

No es necesario enviar body ni headers de autenticación.

### Prueba 1: búsqueda parcial por nombre de servicio

En **Params** agrega:

| Key | Value |
|---|---|
| `texto` | `CORTE` |

**Esperado:** `200 OK`. En `servicios` deben aparecer `HU Corte de cabello` y `HU Corte de barba`, cada uno con `serviceId`, proveedor, categoría y al menos un `availableSchedules` con `availabilityId`, `inicioAt`, `finAt` y `recurso`. No debe aparecer `HU Fútbol 5`.

Prueba opcional en la pestaña **Scripts → Post-response**:

```javascript
pm.test("Responde 200 y encuentra los servicios Corte", () => {
  pm.response.to.have.status(200);
  const body = pm.response.json();
  const nombres = body.servicios.map(item => item.service);
  pm.expect(nombres).to.include("HU Corte de cabello");
  pm.expect(nombres).to.include("HU Corte de barba");
  pm.expect(nombres).to.not.include("HU Fútbol 5");
  body.servicios.forEach(item => {
    pm.expect(item.serviceId).to.be.a("string");
    pm.expect(item.provider).to.be.a("string");
    pm.expect(item.category).to.be.a("string");
    pm.expect(item.availableSchedules).to.be.an("array").that.is.not.empty;
    item.availableSchedules.forEach(horario => {
      pm.expect(horario.availabilityId).to.be.a("string");
      pm.expect(horario.inicioAt).to.be.a("string");
      pm.expect(horario.finAt).to.be.a("string");
      pm.expect(horario.recurso).to.be.a("string").and.not.empty;
    });
  });
});
```

### Prueba 2: búsqueda por nombre comercial, sin tildes

En **Params**:

| Key | Value |
|---|---|
| `texto` | `barberia central` |

**Esperado:** `200 OK`. Devuelve los servicios disponibles del proveedor `HU Barbería Central`. Cada oferta debe conservar el nombre comercial con tilde en la respuesta. No deben aparecer ofertas de `HU Complejo El Estadio`.

### Prueba 3: búsqueda por categoría

En **Params**:

| Key | Value |
|---|---|
| `categoria` | `Deportes y recreación` |

**Esperado:** `200 OK`. Devuelve servicios activos y disponibles cuya categoría sea `Deportes y recreación`, incluyendo `HU Fútbol 5` y `HU Fútbol infantil`. No devuelve servicios de `Barbería y belleza`.

### Prueba 4: combinar texto y categoría

En **Params**:

| Key | Value |
|---|---|
| `texto` | `futbol` |
| `categoria` | `Deportes y recreacion` |

**Esperado:** `200 OK`. Devuelve `HU Fútbol 5` y `HU Fútbol infantil` del proveedor `HU Complejo El Estadio`; no devuelve `HU Fútbol recreativo` de `HU Barbería Central`. La respuesta debe cumplir ambos filtros simultáneamente.

### Prueba 5: búsqueda válida sin coincidencias

En **Params**:

| Key | Value |
|---|---|
| `texto` | `{{textoSinResultados}}` |

**Esperado:** `200 OK` con el cuerpo:

```json
{
  "servicios": [],
  "mensaje": "No se encontraron servicios"
}
```

Usa un texto exclusivo que no aparezca en ningún nombre de servicio ni proveedor de la base.

### Prueba 6: rechazar solicitud sin criterios

Envía la URL sin Params:

```http
GET {{baseUrl}}/api/v1/catalog/services/search
```

**Esperado:** `400 Bad Request`. El cuerpo contiene el mensaje:

```json
{
  "error": "Debe ingresar al menos un criterio de búsqueda: texto o categoría",
  "mensaje": "Debe ingresar al menos un criterio de búsqueda: texto o categoría"
}
```

También prueba `texto` con espacios y `categoria` vacía; debe responder `400` como si no se hubiera enviado ningún criterio.

### Prueba 7: rechazar una categoría inexistente

En **Params**:

| Key | Value |
|---|---|
| `categoria` | `{{categoriaInexistente}}` |

**Esperado:** `400 Bad Request`. El cuerpo contiene `error` y `mensaje`, indicando que la categoría enviada no existe en el catálogo. No se devuelve una lista de servicios.

### Prueba 8: excluir horarios ocupados, no laborables e inactivos

Repite la prueba 3 (`categoria=Deportes y recreación`) después de preparar los casos negativos de la sección 2.

**Esperado:** `200 OK`. Ninguna respuesta debe incluir:

- La disponibilidad asociada a una reserva confirmada.
- Disponibilidades cuya fecha sea no laborable para el proveedor, interpretada en `America/Bogota`.
- Ofertas de servicios, proveedores o relaciones agenda-servicio inactivas.

Un servicio que solo tenga horarios excluidos no debe aparecer en `servicios`. Si tiene otros horarios válidos, puede aparecer, pero su lista `availableSchedules` no debe incluir los horarios excluidos.

## 4. Forma general de la respuesta exitosa

```json
{
  "servicios": [
    {
      "serviceId": "UUID del servicio",
      "service": "HU Corte de cabello",
      "provider": "HU Barbería Central",
      "category": "Barbería y belleza",
      "availableSchedules": [
        {
          "availabilityId": "UUID de disponibilidad",
          "inicioAt": "2026-10-20T15:00:00Z",
          "finAt": "2026-10-20T15:30:00Z",
          "recurso": "HU Barbero Carlos"
        }
      ]
    }
  ],
  "mensaje": null
}
```

Los UUID y horarios reales dependen de los registros creados en tu base. Los nombres de ejemplo corresponden al conjunto de prueba definido en esta guía.

## 5. Resultado esperado por prueba

| Prueba | Código | Resultado principal |
|---|---:|---|
| Texto `CORTE` | 200 | Dos servicios Corte; incluye proveedor, categoría, horario y recurso |
| Texto `barberia central` | 200 | Ofertas del proveedor Barbería Central; búsqueda ignora tilde y mayúsculas |
| Categoría `Deportes y recreación` | 200 | Solo ofertas activas disponibles de esa categoría |
| `futbol` + categoría Deportes | 200 | Solo servicios que cumplen ambos filtros |
| Texto exclusivo sin coincidencias | 200 | `servicios: []`, mensaje `No se encontraron servicios` |
| Sin query params o criterios en blanco | 400 | Error de criterio obligatorio |
| Categoría fuera del catálogo | 400 | Error que identifica la categoría inexistente |
| Horarios ocupados/no laborables/inactivos | 200 | Esos horarios/ofertas quedan excluidos |

## 6. Observaciones

- Si cualquier solicitud válida falla con error SQL que mencione `unaccent`, habilita la extensión en la base de pruebas y vuelve a intentar.
- Si falla por una columna o relación no encontrada, contrasta `SearchServicesJpaRepository` y las consultas de esta guía con el DDL desplegado. El proyecto no incluye el DDL completo.
- No uses los datos del ejemplo como prueba exacta si compartes una base con otros datos: crea el conjunto en una base aislada para que las listas esperadas sean deterministas.
