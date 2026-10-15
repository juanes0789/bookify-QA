# Reporte de Implementacion: Busqueda de Servicios

## Resumen

Se implementaron las fases 1, 2, 3, 4 y 5 descritas en `PLAN_IMPLEMENTACION_BUSQUEDA_SERVICIOS.md` para la HU de busqueda publica de servicios por nombre, proveedor o categoria.

El endpoint agregado es:

```http
GET /api/v1/catalog/services/search
```

Permite consultar usando los parametros opcionales:

- `texto`
- `categoria`

La solicitud debe incluir al menos uno de los dos criterios.

## Alcance Implementado

### Fase 1: Dominio

Se agregaron las excepciones de dominio:

- `InvalidSearchCriteriaException`
- `CategoryNotFoundException`

Se agrego la utilidad:

- `TextNormalizer`

Esta utilidad normaliza texto removiendo tildes/diacriticos y convirtiendo a minusculas.

Se agregaron los modelos:

- `SearchServiceFilter`
- `AvailableScheduleWithResource`
- `ServiceSearchItem`
- `SearchServicesResult`

Se agregaron los puertos:

- `SearchServicesInputPort`
- `SearchServicesOutputPort`
- `CategoryCatalogOutputPort`

### Fase 2: Servicio de Dominio

Se agrego:

- `SearchServicesService`

Reglas implementadas:

- Rechaza busquedas sin `texto` ni `categoria`.
- Valida la existencia de la categoria contra el catalogo.
- Filtra por texto usando coincidencia parcial normalizada.
- Filtra por categoria usando comparacion normalizada.
- Excluye servicios sin horarios disponibles.
- Retorna lista vacia con mensaje `"No se encontraron servicios"` cuando no hay coincidencias.

### Fase 3: Persistencia

Se agregaron:

- `SearchServicesProjection`
- `SearchServicesJpaRepository`
- `TipoProveedorJpaRepository`
- `SearchServicesEntityMapper`
- `SearchServicesPersistenceAdapter`

La consulta nativa une servicios, proveedores, categorias, agendas, recursos y disponibilidades.

Filtros incluidos:

- Servicio activo.
- Proveedor activo.
- Agenda-servicio activa.
- Estado de disponibilidad `DISPONIBLE`.
- Exclusion de reservas confirmadas.
- Exclusion de dias no laborables del proveedor.
- Busqueda por texto contra nombre de servicio o nombre comercial del proveedor.
- Busqueda por categoria comercial.

### Fase 4: REST

Se agregaron los DTOs:

- `ScheduleWithResourceResponseDto`
- `SearchedServiceOfferResponseDto`
- `SearchServicesResponseDto`

Se agrego el mapper:

- `SearchServicesRestMapper`

Se agrego el controlador:

- `SearchServicesController`

Endpoint publico:

```http
GET /api/v1/catalog/services/search
```

Ejemplo de respuesta esperada con resultados:

```json
{
  "servicios": [
    {
      "serviceId": "uuid",
      "service": "Corte de cabello",
      "provider": "Barberia Central",
      "category": "Barberia y belleza",
      "availableSchedules": [
        {
          "availabilityId": "uuid",
          "inicioAt": "2026-10-13T15:00:00Z",
          "finAt": "2026-10-13T15:30:00Z",
          "recurso": "Barbero Carlos"
        }
      ]
    }
  ],
  "mensaje": null
}
```

Ejemplo sin resultados:

```json
{
  "servicios": [],
  "mensaje": "No se encontraron servicios"
}
```

### Fase 5: Configuracion y Excepciones

Se actualizo:

- `BeanConfiguration`
- `GlobalExceptionHandler`

Se registro el bean:

- `SearchServicesInputPort`

Se agrego manejo `400 Bad Request` para:

- `InvalidSearchCriteriaException`
- `CategoryNotFoundException`

La respuesta de error contiene:

```json
{
  "error": "mensaje del error",
  "mensaje": "mensaje del error"
}
```

## Falla Observada Durante Verificacion

Primero se ejecuto:

```powershell
.\gradlew.bat test
```

Esa ejecucion fallo durante `compileJava` con errores indicando que no encontraba clases existentes del proyecto, por ejemplo:

- `AvailabilityEntity`
- `RegisterUserInputPort`
- `GetServiceCatalogService`
- `AvailabilityOverlapException`

Esto era inconsistente, porque esos archivos si existen en el repositorio.

Ademas, Gradle reporto:

```text
java.nio.file.AccessDeniedException:
build/reports/problems/problems-report.html
```

Luego se repitio solo compilacion limpia de Java con:

```powershell
.\gradlew.bat compileJava --no-daemon --rerun-tasks
```

Resultado:

```text
BUILD SUCCESSFUL
```

Conclusion: la implementacion compila. La falla inicial parece estar relacionada con estado incremental de Gradle, daemon o permisos sobre archivos generados en `build/reports`, no con una clase faltante real.

## Verificacion Alcanzada

Verificacion ejecutada correctamente:

```powershell
.\gradlew.bat compileJava --no-daemon --rerun-tasks
```

Resultado:

```text
BUILD SUCCESSFUL
```

El propietario del entorno ejecuto nuevamente ` .\gradlew.bat test` despues de configurar Mockito con el mock maker de subclases y reporto que la suite paso. Esa ejecucion fue anterior a las pruebas especificas de busqueda agregadas en esta revision.

## Riesgos Tecnicos Pendientes

### Extension `unaccent`

La consulta nativa usa `unaccent(...)` en PostgreSQL para soportar busquedas insensibles a tildes.

La base de datos debe tener habilitada la extension:

```sql
CREATE EXTENSION IF NOT EXISTS unaccent;
```

Si la extension no esta habilitada, el endpoint puede fallar en runtime al ejecutar SQL.

### Confirmar Schema Real

Se actualizo `SearchServicesJpaRepository` para usar columnas identificadoras `id_<entidad>` y claves foraneas `id_<entidad_relacionada>`, incluyendo proveedor, tipo de proveedor, servicio-sede, agenda-servicio, agenda, recurso, disponibilidad, estados, ocupaciones y dias no laborables. La consulta filtra por la actividad del servicio, el proveedor y la relación agenda-servicio.

El repositorio no incluye el DDL de la base de datos, asi que los nombres de relaciones `tbl_agenda_recurso` y `tbl_ocupacion_disponibilidad` todavia deben contrastarse con el esquema desplegado antes de validar la consulta en runtime.

### Pruebas Automatizadas

Se agregaron pruebas unitarias para `SearchServicesService` y `TextNormalizer`, pruebas REST con `MockMvc` para `SearchServicesController` y una prueba del agrupamiento/mapeo de `SearchServicesEntityMapper` (14 casos en total). La suite completa se ejecuto y reporto `BUILD SUCCESSFUL` en 56 segundos; Gradle marco las tareas como `UP-TO-DATE`. Los resultados XML confirman 14 pruebas nuevas, todas sin fallos ni errores.

## Paso A Paso Para Ejecutar La HU

### 1. Compilar

```powershell
.\gradlew.bat compileJava --no-daemon --rerun-tasks
```

### 2. Habilitar `unaccent` en PostgreSQL

Ejecutar en la base de datos:

```sql
CREATE EXTENSION IF NOT EXISTS unaccent;
```

### 3. Ejecutar la aplicacion

```powershell
.\gradlew.bat bootRun
```

### 4. Buscar por texto de servicio

```http
GET http://localhost:8080/api/v1/catalog/services/search?texto=CORTE
```

### 5. Buscar por nombre comercial del proveedor

```http
GET http://localhost:8080/api/v1/catalog/services/search?texto=barberia%20central
```

### 6. Buscar por categoria

```http
GET http://localhost:8080/api/v1/catalog/services/search?categoria=Deportes%20y%20recreacion
```

### 7. Combinar texto y categoria

```http
GET http://localhost:8080/api/v1/catalog/services/search?texto=futbol&categoria=Deportes%20y%20recreacion
```

### 8. Validar busqueda invalida

```http
GET http://localhost:8080/api/v1/catalog/services/search
```

Respuesta esperada:

```http
400 Bad Request
```

Con cuerpo similar a:

```json
{
  "error": "Debe ingresar al menos un criterio de busqueda: texto o categoria",
  "mensaje": "Debe ingresar al menos un criterio de busqueda: texto o categoria"
}
```

### 9. Validar categoria inexistente

```http
GET http://localhost:8080/api/v1/catalog/services/search?categoria=Categoria%20Inexistente
```

Respuesta esperada:

```http
400 Bad Request
```

## Pendientes Del Ciclo

- Confirmar el DDL real de agenda/recurso/categoria/ocupaciones contra la consulta nativa actualizada.
- Habilitar `unaccent` en PostgreSQL antes de invocar busquedas, tanto para filtrar como validar categorias.
- Validar manualmente el endpoint con datos reales y una base de datos que tenga el schema actualizado.

En el repositorio solo se encontro `src/main/resources/data.sql`; no hay DDL para comprobar las columnas ni una base configurada con la que validar la consulta.
