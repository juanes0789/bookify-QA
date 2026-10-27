# Bitácora de QA · Detección y gestión de defectos · Sprint 2

**Proyecto:** Bookify, plataforma de reservas de servicios (caso 14) · **Equipo:** EAP09, CodeF@ctory UdeA
**Rol:** Ingeniería de calidad (QA) · **Última actualización:** 27/10/2026

Esta bitácora registra qué defectos encontramos, cómo los detectamos y cómo los gestionamos hasta su cierre. Se actualiza a medida que avanza el trabajo.

---

## 1. Resumen (26 y 27/10/2026)

| Indicador | Valor |
|---|---|
| Defectos nuevos detectados | **10**: 3 registrados, corregidos y cerrados (#178 a #180) · 1 de seguimiento corregido (S5976) · 6 registrados para desarrollo (#181 a #186) |
| Defectos abiertos que volvimos a probar | **10** (#75 a #79 y #173 a #177) |
| Resultado del retest | 3 corregidos · 2 corregidos en parte · 5 siguen abiertos (comentado en cada bug el 27/10) |
| Quality Gate de SonarCloud | Rojo en `7de6686` → **verde en `463d4e4`** («Green (was Red)», 27/10/2026) |
| Vulnerabilidades / bugs en Sonar | 1 / 0 → **0 / 0** |
| Pruebas unitarias | 206 → 223 en `main` → **224** con el PR de seguimiento (0 fallos) |
| Cobertura en SonarCloud | 88,2 % → **95,2 %** |
| Deuda técnica en SonarCloud | 245 min → **190 min** |

Hallazgos nuevos por técnica de detección:

| Técnica | Hallazgos |
|---|---|
| Análisis estático (SonarCloud) | 3 (#178, #179 y el seguimiento S5976) |
| Ejecución de pruebas y análisis de cobertura (JaCoCo) | 1 (#180) |
| Pruebas exploratorias contra el ambiente desplegado (Render) | 2 (#181, #183) |
| Revisión de código contra los criterios de aceptación (Gherkin) | 4 (#182, #184, #185, #186) |

---

## 2. Cómo trabajamos

### 2.1 Fuentes de detección

1. **Análisis estático:** SonarCloud (proyecto `juanes0789_bookify-QA`) corre en el pipeline de Azure DevOps con cada integración a `main`. Lo consultamos por la interfaz y por la API pública, lo que permite repetir cualquier consulta con un `curl`.
2. **Pruebas unitarias y cobertura:** `./gradlew clean test jacocoTestReport`. Comparamos el reporte de JaCoCo por clase entre commits para detectar regresiones de cobertura.
3. **Revisión de código contra criterios de aceptación:** contrastamos la implementación con las reglas y los escenarios Given-When-Then de cada HU en Azure Boards.
4. **Pruebas exploratorias en el ambiente desplegado:** peticiones a `https://bookify-pb92.onrender.com`. En esta jornada fueron solo GET públicos, que no modifican datos.
5. **Retest de defectos abiertos:** después de cada integración de desarrollo, volvemos a verificar los bugs que siguen en *New*.

### 2.2 Registro en Azure Boards

Cada defecto se registra como **Bug** con la misma plantilla:

- **Origen:** técnica, commit y fecha de detección.
- **Ubicación:** archivo, línea y fragmento de código.
- **Precondiciones.**
- **Pasos para reproducir:** comandos copiables.
- **Resultado esperado**, citando el criterio de aceptación o la regla de la rúbrica.
- **Resultado obtenido**, con la respuesta real.
- **Clasificación PACS §8:** severidad y prioridad justificadas, aunque difieran de la severidad que asigna Sonar.
- **Corrección propuesta o aplicada.**
- **Verificación.**
- **Evidencia adjunta:** respuestas JSON y reportes.

**Trazabilidad:** cada bug tiene como padre el Feature **#73 · F7. Calidad y estabilización**. Se relaciona con la HU o el bug que lo originó, y enlaza el commit que lo introdujo, el commit que lo corrige y el pull request.

**Flujo de estados:**

| Estado | Cuándo |
|---|---|
| New | Recién registrado |
| Active | En corrección |
| Resolved (Fixed) | Corrección implementada y verificada localmente |
| Closed (Fixed and verified) | Corrección integrada a `main` y confirmada por el pipeline o SonarCloud |

---

## 3. Bitácora cronológica (26/10/2026)

1. **Revisión del estado de `main` en el fork `bookify-QA`.** Hay 6 commits de desarrollo posteriores al PR #10 (el que había dejado los Quality Gates en verde el 17/10). Corrimos la suite y JaCoCo: 206 pruebas en verde, pero la cobertura bajó a 88,2 % y aparecieron 4 clases en 0 %.
2. **Contraste de la guía Postman de búsqueda (HU-14) con el código.** La prueba que filtra por `Deportes y recreacion` (sin tilde) respondería 400: la validación de la categoría distingue tildes, cosa que la regla de HU-14 prohíbe (→ #182).
3. **Revisión de SonarCloud.** El análisis del commit `7de6686` dejó el Quality Gate en **ERROR** (`new_security_rating` = D) y 10 hallazgos por encima de Minor. Analizamos si la vulnerabilidad crítica `java:S4502` era real (→ #178).
4. **Análisis de causa raíz.** El commit `9938d0f` reescribió `SecurityConfig` y borró las supresiones justificadas del PR #10. También eliminó dos clases de prueba sin reemplazarlas.
5. **Corrección por parte de QA.** Rama `fix/quality-gates-sprint2`, commit `440c4d5`, con 4 correcciones de Sonar y 17 pruebas nuevas con el patrón AAA.
6. **Registro en Azure** de los bugs #178, #179 y #180 con evidencia adjunta, y paso a *Resolved*.
7. **Apertura del PR [#11](https://github.com/manuelsepulvedag/bookify/pull/11)**, enlazado en ambos sentidos con los bugs.
8. **Retest de los 10 defectos abiertos** contra el código actual y el ambiente desplegado (sección 5).
9. **Pruebas exploratorias de HU-14 en Render.** Ninguna de las 5 categorías de HU-09 funciona en la búsqueda (→ #181), y la búsqueda devuelve horarios que ya pasaron (→ #183).

**27/10/2026**

10. **Integración del PR #11** en `main` (merge `463d4e4`, 00:03 UTC). El pipeline de Azure corrió el análisis de SonarCloud a las 00:07 UTC: **Quality Gate OK, «Green (was Red)»**. Los 10 issues quedaron en CLOSED / FIXED.
11. **Cierre en Azure** de #178, #179 y #180 (*Closed · Verified*), con las respuestas del nuevo análisis adjuntas.
12. **Hallazgo de seguimiento.** El mismo análisis reportó `java:S5976` (Major) en `GlobalExceptionHandlerTest`, introducido por las pruebas que QA agregó en #180: tres pruebas con la misma estructura. Lo corregimos con una `@ParameterizedTest` de 4 casos, que además cubre una llave foránea que no estaba probada (rama `fix/sonar-s5976-parametrizada`, commit `389f000`). Lección: el propio análisis estático también revisa el trabajo de QA.
13. **Registro en Azure** de los 5 hallazgos nuevos: #181 a #185 (*New*, padre #73, relacionados con sus HU), con la evidencia de Render adjunta en #181 y #183.
14. **Retest comentado** en los 10 bugs abiertos (#75 a #79 y #173 a #177), con el veredicto y la evidencia de cada uno. Los estados no se cambiaron: eso se acuerda con desarrollo.
15. **Bug #186, encontrado al preparar la demo en el IDE.** `BookingServiceHu13Test` verifica que, sin recurso libre, se lance `ResourceUnavailableException`, pero esa excepción no tiene manejador en `GlobalExceptionHandler`: la API respondería 500 y el criterio de HU-13 pide 409 con «El recurso no está disponible en ese horario». Registrado como #186 (3 - Medium, relacionado con HU-13 y #172). Lección: una prueba unitaria en verde verifica el dominio, no el contrato HTTP.
16. **Material para la review de calidad:** presentación de 12 diapositivas con guion por expositor en las notas, y `Guia_Demo_IDE_Pruebas_Sprint2.md` con cuatro pruebas explicadas línea por línea y las preguntas probables.

---

## 4. Defectos registrados y corregidos por QA

### Bug #178 · Vulnerabilidad crítica `java:S4502` (CSRF) en `SecurityConfig` · 2 - High · P1 · *Closed*

- **Detección:** SonarCloud, commit `7de6686`. El Quality Gate quedó en ERROR y se incumplían 2 de los 5 criterios de Calidad Interna de la rúbrica: "Minor o inferior" y "cero vulnerabilidades críticas".
- **Análisis:** en este contexto es un falso positivo. La API es sin estado (`SessionCreationPolicy.STATELESS`) y lee el JWT solo de la cabecera `Authorization: Bearer`, sin cookies, así que un ataque CSRF no es posible. Aun así hay que resolverlo formalmente: el Quality Gate y la rúbrica cuentan el hallazgo.
- **Causa raíz:** regresión. El commit `9938d0f` borró `@SuppressWarnings({"java:S112", "java:S4502"})`. Además, el perfil "Sonar way comprehensive" convirtió el antiguo *security hotspot* en vulnerabilidad.
- **Reproducción:**
  ```bash
  curl -s "https://sonarcloud.io/api/qualitygates/project_status?projectKey=juanes0789_bookify-QA"
  curl -s "https://sonarcloud.io/api/issues/search?componentKeys=juanes0789_bookify-QA&rules=java:S4502,java:S112&resolved=false"
  ```
- **Corrección:** se restaura la supresión con su justificación técnica en el código (commit `440c4d5`).

### Bug #179 · Hallazgos Critical y Major de mantenibilidad (`java:S1192`, `java:S5778` ×7) · 3 - Medium · P2 · *Closed*

- **Detección:** SonarCloud. Hay un literal repetido 4 veces en `GlobalExceptionHandler` (entró en `deb2c9e`) y 7 lambdas de `assertThatThrownBy` con dos invocaciones en las pruebas de HU-09 y HU-12 (entraron en `8cefaf2`).
- **Corrección:** constante `EMAIL_TAKEN_MESSAGE`; en las pruebas, el request se arma en la sección *Arrange*.

### Bug #180 · Adaptadores JDBC y controladores de HU-09/HU-12 sin pruebas (0 %) · 3 - Medium · P2 · *Closed*

- **Detección:** comparación de JaCoCo por clase. `MerchantProfileJdbcAdapter`, `ResourceJdbcAdapter`, `MerchantProfileController` y `ResourceController` estaban en 0 %.
- **Causa raíz:** el commit `9938d0f` cambió los adaptadores JPA por adaptadores JDBC y borró sus pruebas.
- **Corrección:** 17 pruebas nuevas con AAA, que incluyen casos de error: tipo o estado sin configurar, nombre duplicado, `Retry-After` en 0 y violación de llave foránea.

### Verificación en SonarCloud (análisis de `463d4e4`, 27/10/2026)

| Métrica (SonarCloud) | `7de6686` (antes) | `463d4e4` (después) |
|---|---|---|
| Quality Gate | ERROR (`new_security_rating` = D) | **OK** |
| Vulnerabilidades | 1 (Critical) | **0** |
| Bugs | 0 | 0 |
| Hallazgos por encima de Minor | 2 Critical + 8 Major | 1 Major (S5976, de seguimiento; corregido en `389f000`) |
| Cobertura (líneas + ramas) | 88,2 % | **95,2 %** |
| Líneas / ramas | 90,8 % / 74,8 % | 97,9 % / 81,0 % |
| Pruebas ejecutadas | 205 | 222 |
| Deuda técnica | 245 min | 190 min |
| Complejidad ciclomática máxima por archivo | 28 (`BookingService`) | 28 (`BookingService`) |

### Hallazgo de seguimiento · `java:S5976` (Major) en `GlobalExceptionHandlerTest`

- **Detección:** SonarCloud, en el análisis que verificó el cierre de #178 a #180.
- **Causa:** tres pruebas de `handleDataIntegrityViolation` con la misma estructura, dos de ellas agregadas por QA en #180.
- **Corrección:** una sola `@ParameterizedTest` con 4 casos (rama `fix/sonar-s5976-parametrizada`, commit `389f000`, pendiente de PR). Queda registrado como seguimiento en el historial de #180.
- **Resultado local:** 224 pruebas, 0 fallos; la cobertura de ramas sube a 81,4 %.

---

## 5. Retest de defectos abiertos

| Bug | HU | Resultado del retest (26/10) | Evidencia |
|---|---|---|---|
| #173 · Crear perfil comercial siempre falla | HU-09 | ✅ **Corregido en código** | `9938d0f`: el rol pasa a ser `String` y se compara con `equalsIgnoreCase`. Falta la verificación funcional. |
| #174 · Registrar recurso siempre falla | HU-12 | ✅ **Corregido en código** | `9938d0f`: `type` es `String`, validado contra `FISICO`/`HUMANO`. Falta la verificación funcional. |
| #175 · Sin 403 por rol y token exigido en catálogo | HU-07 | ✅ **Corregido y verificado en Render** | Catálogo sin token → 200; búsqueda sin token → 200/400; `POST /api/v1/comercio/perfil` sin token → 401. Las reglas `hasAuthority` las cubre `SecurityConfigTest`. |
| #176 · La previsualización busca por fecha de creación | HU-11 | ⚠️ **Corregido en parte** | La zona horaria ya es `America/Bogota`, pero sigue filtrando por `r.fecha_reserva`, que `BookingService` llena con el momento de crear la reserva (`b.setFechaReserva(now)`). No filtra por estado y solo retorna los ids. |
| #177 · El bloqueo responde 401 en vez de 429 | HU-08 | ⚠️ **Corregido en parte** | Ya responde 429 con `Retry-After`, pero el mensaje es "La cuenta está bloqueada temporalmente." y no el del criterio ("Demasiados intentos fallidos…"). El cuerpo no trae `mensaje`, y los valores 5 intentos y 15 min siguen fijos en el código. |
| #75 · Las canceladas desaparecen del historial | HU-05 | ❌ **Sigue abierto** | El historial hace `INNER JOIN tbl_ocupacion_disponibilidad`, y esa fila se borra al cancelar. |
| #76 · Cancelar dos veces responde 404 | HU-04 | ❌ **Sigue abierto** | `BookingService.cancel` busca la ocupación (línea 58) antes de revisar si la reserva ya estaba cancelada (línea 62). |
| #77 · "?" en lugar de tildes | HU-04 | ❌ **Sigue abierto** | `BookingService.java` sigue en ASCII con `inici?` y `est?`. |
| #78 · Disponibilidad inexistente responde 409 | HU-03 | ❌ **Sigue abierto** | Lanza `DoubleBookingException` → 409. |
| #79 · Cliente inexistente responde 500 | HU-03 | ❌ **Sigue abierto** | No se valida que el cliente exista. Ver #184: con el manejador actual, la respuesta probablemente cambió a un 409 engañoso. Falta confirmarlo en Render. |

---

## 6. Hallazgos nuevos registrados para desarrollo (27/10/2026)

### Bug #181 · La búsqueda por categoría rechaza todas las categorías de HU-09 · 2 - High · P1 · *New*

- **Técnica:** prueba exploratoria en Render + revisión de código.
- **Regla incumplida:** HU-14 dice "La categoría es la del perfil comercial (HU-09)". Falla el escenario "Buscar servicios por categoría" ("Deportes y recreación" debe devolver 200), y también el criterio de HU-09 "la consulta de servicios muestra «Barbería Central»".
- **Reproducción** (GET público, no modifica datos):
  ```bash
  curl -s -G "https://bookify-pb92.onrender.com/api/v1/catalog/services/search" --data-urlencode "categoria=Deportes y recreación"
  ```
- **Resultado obtenido (26/10/2026):** las 5 categorías del catálogo de HU-09 (Barbería y belleza, Deportes y recreación, Salud y bienestar, Educación y Otros) responden **400** con "La categoría ingresada '…' no existe en el catálogo". La única categoría que funciona es `Empresa`.
- **Causa raíz:** dos catálogos distintos. HU-09 guarda la categoría en `tbl_categoria_comercial` (`p.id_categoria_comercial`), en `MerchantProfileJdbcAdapter`. HU-14 la valida contra `tbl_tipo_proveedor` y une con `INNER JOIN tbl_tipo_proveedor ON p.id_tipo_proveedor`, en `SearchServicesJpaRepository` y `TipoProveedorJpaRepository`. Por eso, un comercio creado con HU-09, que no tiene `id_tipo_proveedor`, **nunca aparece en la búsqueda**, ni siquiera buscando por texto.

### Bug #182 · La validación de la categoría distingue tildes · 3 - Medium · P2 · *New*

- **Técnica:** contraste de la guía Postman de HU-14 con el código.
- **Regla incumplida:** HU-14 dice "no distingue mayúsculas ni tildes", y el escenario "Combinar texto y categoría" usa la categoría con tilde.
- **Causa:** `SearchServicesPersistenceAdapter.existsCategoryByName` usa `existsByNombreIgnoreCase`, que ignora mayúsculas (verificado en Render: `empresa` funciona) pero no tildes. La consulta posterior sí usa `unaccent`. Aun después de corregir #181, `categoria=Deportes y recreacion` respondería 400.

### Bug #183 · Se muestran y se pueden reservar horarios que ya pasaron · 2 - High · P1 · *New*

- **Técnica:** prueba exploratoria en Render + revisión de código + evidencia del Sprint 1.
- **Regla incumplida:** HU-02 y HU-14 dicen que solo se muestran "los horarios disponibles para reservar".
- **Reproducción:**
  ```bash
  curl -s -G "https://bookify-pb92.onrender.com/api/v1/catalog/services/search" --data-urlencode "categoria=Empresa"
  ```
- **Resultado obtenido (26/10/2026):** devuelve el horario `7def517e-…` con `inicioAt` = `2026-10-22T03:26:18Z`, cuatro días antes de la consulta.
- **Causa:** ninguna consulta de catálogo ni de búsqueda filtra `inicio_at > now()`, y `BookingService.create` no rechaza horarios pasados. La colección Postman del Sprint 1 ya lo evidenciaba sin notarlo: el paso de preparación `CP-HU04-P4 · Ana reserva el horario pasado` reserva con éxito un horario pasado.

### Bug #184 · Cualquier violación de integridad no reconocida responde "El correo ya está registrado" · 3 - Medium · P2 · *New*

- **Técnica:** revisión de código, reproducible con una prueba unitaria.
- **Causa:** el último `return` de `GlobalExceptionHandler.handleDataIntegrityViolation` responde 409 "El correo ya está registrado" a cualquier `DataIntegrityViolationException` que no coincida con las restricciones conocidas. Por ejemplo, una llave foránea de reserva con un cliente inexistente (escenario del bug #79) recibiría un mensaje que no tiene nada que ver con el error.
- **Esperado:** un mensaje genérico de conflicto de datos, o el manejo específico de cada restricción.

### Bug #185 · `UserPersistenceAdapter.findByEmail` devuelve siempre el rol CLIENTE y activo · 4 - Low · P3 · *New*

- **Técnica:** análisis de cobertura (las líneas sin cubrir llevaron a revisar el método) + revisión de código.
- **Causa:** el método construye el `User` con `UserRole.CLIENTE` y `activo = true` fijos, sin importar lo que haya en la base. Hoy nadie lo llama, pero si se reutiliza, un proveedor quedaría como cliente. QA no le escribió pruebas a propósito, para no validar un comportamiento incorrecto. Se propone eliminarlo o corregirlo.

### Bug #186 · La doble reserva sobre el mismo recurso responde 500 en lugar de 409 · 3 - Medium · P2 · *New*

- **Técnica:** revisión de código al preparar la demo de pruebas unitarias.
- **Regla incumplida:** HU-13, escenario «Impedir la doble reserva sobre el mismo recurso»: 409 y «El recurso no está disponible en ese horario».
- **Causa:** `BookingService.java:47` lanza `ResourceUnavailableException` («No hay recursos disponibles para el horario solicitado.»), que extiende `RuntimeException` y no tiene `@ExceptionHandler`. La doble reserva sí se impide, pero el cliente recibe un error interno con otro mensaje.
- **Reproducción:** `grep -rn "ResourceUnavailable" src/main` muestra solo la clase y el `throw`.

---

## 7. Observaciones de calidad (no son defectos)

- **Patrón AAA:** 138 de las 206 pruebas originales marcan `// Arrange / Act / Assert`. Hay 14 archivos sin marcas, entre ellos `RegisterUserServiceTest` (17 pruebas), `SearchServicesServiceTest`, `UserControllerTest` y `BookingServiceTest`, este último con formato compactado. Todas las pruebas nuevas de QA usan AAA.
- **`BookifyApplicationTests` deshabilitada:** la prueba de arranque del contexto está marcada con `@Disabled` porque requiere la base de datos. El pipeline no verifica que la aplicación arranque.
- **`MerchantProfileJdbcAdapter.findOrCreateCategory`** genera el id con `MAX(id) + 1` sin bloqueo, lo que permite una condición de carrera si dos proveedores crean categorías a la vez. El catálogo de categorías está fijo en el dominio (`ALLOWED_CATEGORIES`) y duplicado en la base (relacionado con #181).
- **Hallazgos Minor e Info en Sonar:** quedan 59. El grupo más relevante es `java:S8688` (13 usos de `now()` sin zona horaria explícita), dado que las reglas de HU-10 exigen `America/Bogota`.

---

## 8. Lecciones y prácticas propuestas para el Sprint 3

1. **Las regresiones de calidad entraron con refactors de desarrollo.** `9938d0f` borró supresiones justificadas y pruebas sin reemplazarlas. Proponemos que el pipeline analice cada PR con SonarCloud **antes** de integrar, y que el Quality Gate bloquee la integración si la cobertura del código nuevo baja.
2. **El retest es parte del trabajo de QA.** De 10 bugs abiertos, 3 ya estaban corregidos y nadie lo había verificado, y 2 solo en parte. Proponemos volver a probar después de cada integración.
3. **Los defectos de integración entre HU no se ven en las pruebas unitarias.** #181 solo apareció al probar el sistema desplegado de punta a punta. Proponemos pruebas funcionales que recorran el flujo completo: crear el perfil (HU-09), registrar un recurso (HU-12), publicar un horario (HU-01) y buscarlo (HU-14).
4. **Los criterios de aceptación son el oráculo de QA.** Los hallazgos #181, #182 y #183 se confirmaron citando reglas explícitas de las HU, lo que evita discusiones sobre si algo es o no un defecto.

---

## 9. Pendientes

- [x] Integrar el PR #11 y confirmar en SonarCloud el Quality Gate en verde → #178, #179 y #180 en *Closed* (27/10/2026).
- [ ] Abrir e integrar el PR de `fix/sonar-s5976-parametrizada` y confirmar 0 hallazgos por encima de Minor.
- [x] Registrar N1 a N5 en Azure con la plantilla y su evidencia → #181 a #185 (27/10/2026).
- [x] Comentar el resultado del retest en los bugs #75 a #79 y #173 a #177 (27/10/2026).
- [ ] Acordar con desarrollo el paso de #173, #174 y #175 a *Resolved* y la verificación funcional en Render de #173 y #174.
- [ ] Volver a probar #181 a #186 cuando desarrollo los corrija.
- [x] Ejecutar los casos de prueba del Sprint 2 y registrar sus defectos (27/10/2026: #187 a #196).
- [ ] Volver a ejecutar los 9 bloqueados cuando se corrijan #187 y #190.
- [ ] Confirmar en Render el comportamiento actual de #79 (requiere token de cliente).
- [ ] Actualizar las cifras del guion de la Review (cobertura, número de pruebas y hallazgos) con el análisis final de SonarCloud.

---

## Anexo · Evidencias

| Archivo | Contenido |
|---|---|
| `sonar-quality-gate-main-7de6686.json` | Quality Gate en ERROR antes de la corrección |
| `sonar-issues-critical-major-main-7de6686.json` | Los 10 issues por encima de Minor |
| `sonar-measures-main-7de6686.json` | Métricas de SonarCloud antes de la corrección |
| `jacoco-antes-7de6686.txt` / `jacoco-despues-440c4d5.txt` | Resumen de JUnit y JaCoCo de cada commit |
| `sonar-quality-gate-main-463d4e4.json` | Quality Gate en OK después de la corrección |
| `sonar-issues-cerrados-463d4e4.json` | Los 10 issues en CLOSED / FIXED |
| `sonar-measures-main-463d4e4.json` | Métricas de SonarCloud después de la corrección |

Los archivos están adjuntos en los bugs #178, #179 y #180 de Azure Boards.

## 10. Ejecución de los casos de prueba CP-HU06 a CP-HU14 (27/10/2026)

- **Qué se ejecutó:** los casos diseñados por el equipo, refinados contra el Gherkin final de Azure Boards (endpoints reales, códigos y mensajes de los criterios) y ampliados con escenarios de autorización y de horarios pasados. Ejecución automatizada contra Render, con cuentas `@bookify-qa.test` creadas en la misma corrida y fechas de 2027.
- **Resultado:** 64 escenarios · **37 pasan · 18 no pasan · 9 bloqueados**. Formato lleno en `Casos_Prueba_Bookify_CP-HU06_a_CP-HU14_v2.1.docx`; evidencia en `Evidencias_CP_Sprint2/` (JSON con petición, respuesta, código y hora UTC de cada escenario, más el script para repetirla).
- **Triage antes de registrar:** se revisó cada «No pasa». Dos resultados eran de la verificación y no del producto (la cabecera `Retry-After` sí llega) y se corrigieron; una sonda adicional confirmó con clientes válidos que crear una reserva responde 500.
- **Defectos nuevos (10):** #187 crear reserva responde 500 en Render (**Critical**); #188 bloquear fechas de otro comercio (**High**, seguridad); #189 horarios en la agenda de otro comercio (**High**, seguridad); #190 no hay forma de crear agenda/horarios para un recurso nuevo (**High**); #191 la confirmación de HU-11 no existe: bloquea aunque haya reservas (**High**); #192 validaciones sin lista de campos; #193 login fallido sin el mensaje del criterio; #194 fecha pasada responde 500; #195 el catálogo no muestra el recurso de cada horario; #196 mensaje de horario cruzado distinto.
- **Confirmados en ejecución:** #177 (HU-08, mensaje del 429), #181 y #182 (categorías de la búsqueda), #183 (horarios pasados).
- **Bloqueados (9):** cuenta inactiva (sin datos), fin del bloqueo (la duración no es configurable, #177), HU-10 F2 y los cuatro de HU-11 (requieren agenda en el comercio de QA, #190), y la doble reserva y la reserva a nombre de otro cliente (bloqueadas por #187).
