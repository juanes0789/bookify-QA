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
