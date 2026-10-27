# Evidencia de ejecución · CP-HU06 a CP-HU14 (27/10/2026)

Ejecución de los casos del Sprint 2 contra `https://bookify-pb92.onrender.com` (main en `fed878d`), con cuentas de prueba `@bookify-qa.test` creadas en la misma corrida. Los tokens JWT de las respuestas se reemplazaron por `<jwt-de-prueba-redactado>`.

| Archivo | Contenido |
|---|---|
| `ejecucion_cp_hu06_hu14.json` | Los 64 escenarios: petición, resultado esperado según Azure, código, respuesta, veredicto (37 pasan, 18 no pasan, 9 bloqueados) y hora UTC |
| `sonda_verificacion.json` | Verificaciones adicionales antes de registrar bugs: cabecera `Retry-After`, cuerpo de las validaciones y reservas con clientes válidos (#187) |
| `newman_corrida_validacion_coleccion.json` | Reporte de Newman de la colección `Bookify-CP-HU06-HU14` (corrida de validación) |
| `ejecutar_cp_s2.py` y `sonda_verificacion.py` | Scripts para repetir la ejecución: `python3 ejecutar_cp_s2.py salida.json` |

Los defectos que salieron están en Azure Boards (#187 a #196, bajo el Feature #73). El detalle por escenario está en `docs/qa/Casos_Prueba_Bookify_CP-HU06_a_CP-HU14_v2.1.docx`.
