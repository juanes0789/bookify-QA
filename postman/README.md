# Casos de prueba en Postman

Colección con los casos de prueba CP-HU01 a CP-HU05 del Plan de Pruebas (PP-BOOKIFY-001), con el camino feliz y los caminos de excepción de cada historia.

## Importar

1. En Postman: **Import** → seleccionar los dos archivos de esta carpeta.
2. Seleccionar el entorno **Bookify QA** (esquina superior derecha).
3. Completar las variables del entorno:

| Variable | Valor |
|---|---|
| `baseUrl` | `https://bookify-pb92.onrender.com` o `http://localhost:8080` |
| `agendaServicioId` | Id de una agenda activa: `SELECT id FROM tbl_agenda_servicio WHERE activo = true LIMIT 1;` |
| `clienteAnaId` | Id de un cliente |
| `clienteLuisId` | Id de otro cliente distinto |
| `agendaInactivaId` | Opcional: id de una agenda inactiva (si está vacío, se omite CP-HU01-E4) |

## Estructura

Cada carpeta es un caso de prueba y se puede ejecutar sola. Cada petición lleva un identificador para trazarla con el documento del caso:

- `CP-HUxx-Pn` **Preparar**: crea los datos que el caso necesita. Los horarios se generan al azar para poder repetir la ejecución.
- `CP-HUxx-F1` **Camino feliz** del documento, con sus verificaciones.
- `CP-HUxx-E1` **Camino de excepción** del documento; `E2`, `E3`… son los demás criterios de rechazo.

Cada petición tiene en su pestaña **Docs** el resultado esperado, y en **Tests** las verificaciones automáticas.

## Ejecutar

- **Manual**: abrir la carpeta y enviar las peticiones en orden (**Send**), revisando la respuesta y la pestaña **Test Results**.
- **Completa**: clic derecho sobre la colección o una carpeta → **Run** → **Run Bookify…**. Al terminar, **Export Results** genera la evidencia del criterio CA-07.

Un test en rojo es un posible defecto: se registra como Bug en Azure Boards con la petición, la respuesta obtenida y la esperada.

## Modificar la colección

La colección se genera con `scripts/generar_coleccion.py`. Para cambiarla, edita el script y ejecuta `python3 postman/scripts/generar_coleccion.py`; así el JSON no se desordena con ediciones manuales.

---

# Sprint 2 · Casos CP-HU06 a CP-HU14

Archivos: `Bookify-CP-HU06-HU14.postman_collection.json` y `Bookify-QA-Sprint2.postman_environment.json`, generados por `scripts/generar_coleccion_sprint2.py`. Los casos están refinados contra el Gherkin final de cada HU en Azure Boards.

Esta colección está pensada para **ejecutarse a mano**, petición por petición, para entender el flujo:

- Los cuerpos JSON están escritos en claro; los datos que cambian entre corridas son variables del entorno.
- La pestaña **Docs** de cada petición explica el objetivo, la precondición, el resultado esperado según Azure, cómo verificarlo a mano y el estado conocido con su bug.
- La pestaña **Tests** solo compara el código y el mensaje, y guarda el id o el token que usa la siguiente petición. Se pueden borrar esos scripts y copiar los valores al entorno a mano.
- Donde hay que repetir una petición (los intentos fallidos de HU-08), hay peticiones numeradas o la descripción dice cuántas veces enviarla.
- Los escenarios que hoy no se pueden ejecutar están explicados en la descripción de su carpeta, con el bug que los bloquea.

## Antes de cada corrida

En el entorno **Bookify QA Sprint 2** cambia:

| Variable | Por qué |
|---|---|
| `sufijo` | Los correos y los nombres de los comercios lo llevan; deben ser nuevos en cada corrida. |
| `horarioInicio`, `horarioFin`, `horarioCruceInicio`, `horarioCruceFin` | HU-13 crea un horario en la agenda de prueba del Sprint 1; si se repite, responde 409 por cruce. |
| `fechaNoLaborable`, `fechaOtroComercio` | Solo si reutilizas el mismo `sufijo`. |

Ejecuta primero **00 · Despertar Render** y luego las carpetas en orden: HU-07 usa las cuentas de HU-06, HU-10 y HU-12 usan los comercios de HU-09, y HU-13 usa el cliente y el proveedor anteriores.

## Ejecutar todo con Newman

```bash
npx -y newman@6 run postman/Bookify-CP-HU06-HU14.postman_collection.json \
  -e postman/Bookify-QA-Sprint2.postman_environment.json \
  --env-var sufijo=1027b --timeout-request 90000
```

Los fallos esperados hoy son los de los bugs abiertos (#177, #181 a #183, #187 a #196); cada petición lo indica en **Docs**.
