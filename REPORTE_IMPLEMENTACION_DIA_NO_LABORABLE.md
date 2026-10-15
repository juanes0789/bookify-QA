# Reporte actualizado: Días No Laborables (HU-10)

**Actualizado:** 2026-10-15  
**Plan:** [PLAN_IMPLEMENTACION_DIA_NO_LABORABLE.md](./PLAN_IMPLEMENTACION_DIA_NO_LABORABLE.md)  
**Guía Postman:** [GUIA_POSTMAN_DIA_NO_LABORABLE.md](./GUIA_POSTMAN_DIA_NO_LABORABLE.md)

## Estado

La HU está implementada para registrar días no laborables, bloquear nuevas disponibilidades y ocultar horarios existentes en los catálogos. No existe todavía un caso de uso para crear reservas; la validación de reservas queda pendiente hasta que ese flujo exista.

## Esquema confirmado en Supabase

La tabla ya existía; no se creó ni ejecutó migración. La consulta a `information_schema` y restricciones confirmó:

- PK `id_dia_no_laborable`, `uuid NOT NULL DEFAULT gen_random_uuid()`.
- FK `id_proveedor` hacia `tbl_proveedor(id_proveedor)`.
- `fecha` obligatoria, `motivo` opcional con máximo 100 caracteres y `fecha_creacion` con default `now()`.
- Restricción única `uq_dia_no_laborable_proveedor_fecha` sobre `(id_proveedor, fecha)`.
- Permanecen las columnas antiguas `id` y `proveedor_id`; ambas son `NOT NULL` y no tienen default.
- El trigger `fn_validar_dia_no_laborable` solo valida que `fecha` no sea pasada en Bogotá y normaliza `motivo`; no copia valores a las columnas antiguas.

Para evitar fallos de inserción, `NonWorkingDayEntityMapper` escribe el mismo UUID en `id_dia_no_laborable` e `id`, y el mismo UUID de proveedor en `id_proveedor` y `proveedor_id`. La entidad y el handler de conflictos usan el nombre real de la restricción única.

## Implementación y cobertura

- Registro: `POST /api/v1/proveedores/{proveedorId}/dias-no-laborables`; responde `201`.
- Fecha anterior a hoy en `America/Bogota`: `400`, mensaje `La fecha debe ser igual o posterior a hoy`.
- Duplicado por proveedor y fecha: `409`, mensaje `La fecha ya está registrada como no laborable`.
- Motivo de más de 100 caracteres: `400`.
- Creación de disponibilidad para un día bloqueado: `409`, mensaje `La fecha está marcada como no laborable`.
- Los catálogos filtran horarios por `id_proveedor` y fecha Bogota sin borrar disponibilidades.
- La función de reservas sigue pendiente porque actualmente solo está implementado el historial.

## Verificación ejecutada

- `./gradlew compileJava`: `BUILD SUCCESSFUL` (ejecutado por el usuario).
- `./gradlew test --tests "udea.fabrica.bookify.infrastructure.adapter.output.persistence.mapper.NonWorkingDayEntityMapperTest"`: `BUILD SUCCESSFUL` (ejecutado por el usuario).
- El usuario reportó también que las pruebas del proyecto pasaron.
- La prueba del mapper verifica que se llenen ambas parejas de columnas. Aún falta hacer el smoke test de inserción real contra Supabase desde Postman; los pasos y datos están en la guía enlazada arriba.

## Pendiente

1. Ejecutar la guía Postman con un `proveedorId` y `agendaServicioId` reales del entorno.
2. Implementar el bloqueo de reservas cuando exista el endpoint/caso de uso que las crea.


### Corrección de mapeo descubierta durante la prueba Postman

La respuesta `500` de la prueba 6 mostró que `AgendaServicioEntity` consultaba `tbl_agenda_servicio.id`, columna inexistente en Supabase. Se corrigió el `@Column` de su PK a `id_agenda_servicio`. Reiniciar la aplicación antes de repetir la prueba.

## Resultados manuales de Postman reportados (2026-10-15)

- Prueba 6: `POST /api/v1/disponibilidades` en una fecha bloqueada respondió `409 Conflict`, como se esperaba.
- Prueba 7: al bloquear el día que contenía una disponibilidad previa, la búsqueda devolvió `servicios: []`.
- Se verificó en Supabase que la disponibilidad previa continúa existiendo; el catálogo la oculta sin eliminarla.

Con esto quedan validados manualmente el bloqueo de nuevas disponibilidades y el ocultamiento no destructivo. La HU se puede cerrar para estos flujos si el equipo acepta dejar fuera la protección al crear reservas hasta que exista ese caso de uso.
