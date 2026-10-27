"""Genera la colección Postman de los casos CP-HU06 a CP-HU14 (Sprint 2) y su entorno.

La colección está pensada para ejecutarse a mano, petición por petición: los cuerpos van
escritos en claro, los datos que cambian entre corridas son variables del entorno y los
scripts solo verifican el resultado y guardan el id o token que usa la siguiente petición.

Uso: python3 postman/scripts/generar_coleccion_sprint2.py
"""
import json
import uuid
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
COLLECTION = ROOT / "Bookify-CP-HU06-HU14.postman_collection.json"
ENVIRONMENT = ROOT / "Bookify-QA-Sprint2.postman_environment.json"

MSG = "const b = pm.response.json();\nconst msg = [b.mensaje, b.error, b.motivo].filter(Boolean).join(' ');"


def tests(*lines):
    return {"listen": "test", "script": {"type": "text/javascript", "exec": "\n".join(lines).split("\n")}}


def status(code, cp):
    return f'pm.test("{cp} · responde {code}", () => pm.response.to.have.status({code}));'


def contains(text, cp):
    return MSG + f'\npm.test("{cp} · mensaje: {text}", () => pm.expect(msg).to.include("{text}"));'


def save(var, expr, cp):
    return (f"// Guarda {var} para las peticiones siguientes (también puedes copiarlo a mano al entorno).\n"
            f"if (pm.response.code < 300) {{ pm.environment.set(\"{var}\", {expr}); }}")


def req(name, method, path, desc, body=None, token=None, checks=()):
    headers = [{"key": "Content-Type", "value": "application/json"}] if body is not None else []
    if token:
        headers.append({"key": "Authorization", "value": f"Bearer {{{{{token}}}}}"})
    item = {"name": name,
            "request": {"method": method, "header": headers,
                        "url": {"raw": "{{baseUrl}}" + path, "host": ["{{baseUrl}}"],
                                "path": [p for p in path.split("?")[0].split("/") if p]},
                        "description": desc}}
    if "?" in path:
        item["request"]["url"]["query"] = [{"key": k, "value": v} for k, v in
                                           (q.split("=", 1) for q in path.split("?", 1)[1].split("&"))]
    if body is not None:
        item["request"]["body"] = {"mode": "raw", "raw": json.dumps(body, ensure_ascii=False, indent=2),
                                   "options": {"raw": {"language": "json"}}}
    if checks:
        item["event"] = [tests(*checks)]
    return item


def d(objetivo, esperado, verificar, precondicion="", estado=""):
    out = f"**Objetivo:** {objetivo}\n\n"
    if precondicion:
        out += f"**Precondición:** {precondicion}\n\n"
    out += f"**Resultado esperado (Gherkin de Azure):** {esperado}\n\n**Cómo verificarlo a mano:** {verificar}"
    if estado:
        out += f"\n\n**Estado conocido (27/10/2026):** {estado}"
    return out


def folder(name, desc, items):
    return {"name": name, "description": desc, "item": items}


CLIENTE = "qa.s2.cliente.{{sufijo}}@bookify-qa.test"
PROV_A = "qa.s2.proveedor.{{sufijo}}@bookify-qa.test"
PROV_B = "qa.s2.proveedorb.{{sufijo}}@bookify-qa.test"
PROV_C = "qa.s2.proveedorc.{{sufijo}}@bookify-qa.test"
CLIENTE_B = "qa.s2.clienteb.{{sufijo}}@bookify-qa.test"
BLOQUEO = "qa.s2.bloqueo.{{sufijo}}@bookify-qa.test"
REINICIO = "qa.s2.reinicio.{{sufijo}}@bookify-qa.test"
FANTASMA = "qa.s2.fantasma.{{sufijo}}@bookify-qa.test"
PERFIL_A = {"businessName": "Barbería QA {{sufijo}}", "category": "Barbería y belleza",
            "address": "Calle 10 # 43-20, Medellín", "phone": "+573001234567"}

hu06 = folder("CP-HU06 – Registrar una cuenta",
    "HU-06. Política de contraseñas: 15 a 64 caracteres, no común, sin el correo ni «bookify». "
    "Antes de cada corrida cambia la variable `sufijo` del entorno para que los correos sean nuevos.", [
    req("CP-HU06-F1 · Registrar una cuenta de cliente", "POST", "/api/v1/auth/register",
        d("Registrar un cliente con datos válidos.", "201; la respuesta trae id, nombre, correo y rol CLIENTE, sin la contraseña.",
          "Revisa el código 201 y que el JSON no tenga ningún campo de contraseña."),
        {"nombre": "Ana QA Gómez", "correo": CLIENTE, "contrasena": "{{clave}}", "rol": "CLIENTE"},
        checks=[status(201, "CP-HU06-F1"),
                'pm.test("CP-HU06-F1 · rol CLIENTE y sin contraseña", () => { const b = pm.response.json(); pm.expect(b.rol).to.eql("CLIENTE"); pm.expect(JSON.stringify(b).toLowerCase()).to.not.include("contrasena"); });',
                save("clienteId", "pm.response.json().id", "")]),
    req("CP-HU06-F2 · Registrar una cuenta de proveedor", "POST", "/api/v1/auth/register",
        d("Registrar el proveedor A, que se usa en HU-09 a HU-13.", "201; rol PROVEEDOR.", "Revisa el código y el rol."),
        {"nombre": "Carlos QA Barbería", "correo": PROV_A, "contrasena": "{{clave}}", "rol": "PROVEEDOR"},
        checks=[status(201, "CP-HU06-F2"), save("proveedorAUserId", "pm.response.json().id", "")]),
    req("CP-HU06-E1 · Correo ya registrado (en mayúsculas)", "POST", "/api/v1/auth/register",
        d("Comprobar que el correo es único sin distinguir mayúsculas.", "409; «El correo ya está registrado».",
          "Usa el correo del cliente de F1 en mayúsculas.", "Haber ejecutado F1."),
        {"nombre": "Ana Repetida", "correo": "QA.S2.CLIENTE.{{sufijo}}@BOOKIFY-QA.TEST", "contrasena": "{{clave}}", "rol": "CLIENTE"},
        checks=[status(409, "CP-HU06-E1"), contains("El correo ya está registrado", "CP-HU06-E1")]),
    req("CP-HU06-E2a · Contraseña de 8 caracteres", "POST", "/api/v1/auth/register",
        d("Rechazar una contraseña corta.", "400; el mensaje indica «debe tener entre 15 y 64 caracteres».", "Lee el mensaje de la respuesta."),
        {"nombre": "Prueba Clave", "correo": "qa.s2.clave1.{{sufijo}}@bookify-qa.test", "contrasena": "corta123", "rol": "CLIENTE"},
        checks=[status(400, "CP-HU06-E2a"), contains("entre 15 y 64", "CP-HU06-E2a")]),
    req("CP-HU06-E2b · Contraseña de 65 caracteres", "POST", "/api/v1/auth/register",
        d("Rechazar una contraseña larga.", "400; «debe tener entre 15 y 64 caracteres».", "Lee el mensaje de la respuesta."),
        {"nombre": "Prueba Clave", "correo": "qa.s2.clave2.{{sufijo}}@bookify-qa.test", "contrasena": "A" * 65, "rol": "CLIENTE"},
        checks=[status(400, "CP-HU06-E2b"), contains("entre 15 y 64", "CP-HU06-E2b")]),
    req("CP-HU06-E2c · Contraseña común", "POST", "/api/v1/auth/register",
        d("Rechazar una contraseña de la lista de contraseñas comunes.", "400; «es una contraseña común o comprometida».", "Lee el mensaje."),
        {"nombre": "Prueba Clave", "correo": "qa.s2.clave3.{{sufijo}}@bookify-qa.test", "contrasena": "contraseña123456", "rol": "CLIENTE"},
        checks=[status(400, "CP-HU06-E2c"), contains("común", "CP-HU06-E2c")]),
    req("CP-HU06-E2d · Contraseña que contiene el correo", "POST", "/api/v1/auth/register",
        d("Rechazar una contraseña que incluye el correo.", "400; «no puede contener tu correo».", "Lee el mensaje."),
        {"nombre": "Prueba Clave", "correo": "qa.s2.clave4.{{sufijo}}@bookify-qa.test", "contrasena": "qa.s2.clave4.{{sufijo}}@bookify-qa.test-2026", "rol": "CLIENTE"},
        checks=[status(400, "CP-HU06-E2d"), contains("correo", "CP-HU06-E2d")]),
    req("CP-HU06-E3 · Rol inexistente", "POST", "/api/v1/auth/register",
        d("Rechazar un rol distinto de CLIENTE y PROVEEDOR.", "400; indica el campo inválido.", "Lee el mensaje."),
        {"nombre": "Prueba Rol", "correo": "qa.s2.rol.{{sufijo}}@bookify-qa.test", "contrasena": "{{clave}}", "rol": "ADMINISTRADOR"},
        checks=[status(400, "CP-HU06-E3")]),
    req("CP-HU06-E4 · Correo con formato inválido", "POST", "/api/v1/auth/register",
        d("Rechazar un correo mal formado.", "400; la respuesta lista el campo inválido (correo).", "Busca en la respuesta el nombre del campo.",
          estado="No pasa: responde 400 genérico sin indicar el campo (bug #192)."),
        {"nombre": "Prueba Correo", "correo": "ana-arroba-mail", "contrasena": "{{clave}}", "rol": "CLIENTE"},
        checks=[status(400, "CP-HU06-E4"), 'pm.test("CP-HU06-E4 · indica el campo correo", () => pm.expect(pm.response.text().toLowerCase()).to.include("correo"));']),
    req("CP-HU06-E5 · Nombre vacío", "POST", "/api/v1/auth/register",
        d("Rechazar un nombre vacío.", "400; la respuesta lista el campo inválido (nombre).", "Busca en la respuesta el nombre del campo.",
          estado="No pasa: 400 genérico sin indicar el campo (bug #192)."),
        {"nombre": "", "correo": "qa.s2.nombre.{{sufijo}}@bookify-qa.test", "contrasena": "{{clave}}", "rol": "CLIENTE"},
        checks=[status(400, "CP-HU06-E5"), 'pm.test("CP-HU06-E5 · indica el campo nombre", () => pm.expect(pm.response.text().toLowerCase()).to.include("nombre"));']),
])

hu07 = folder("CP-HU07 – Iniciar sesión",
    "HU-07. El token es un JWT Bearer de 3600 s con el id y el rol. **Bloqueado (no está en la colección):** "
    "E4 cuenta inactiva, porque no hay API ni datos para inactivar una cuenta.", [
    req("CP-HU07-F1 · Iniciar sesión como proveedor", "POST", "/api/v1/auth/login",
        d("Obtener el token del proveedor A.", "200; tokenType Bearer, expiresIn 3600 y role PROVEEDOR.",
          "Copia accessToken; puedes pegarlo en jwt.io para ver el id y el rol."),
        {"correo": PROV_A, "password": "{{clave}}"},
        checks=[status(200, "CP-HU07-F1"),
                'pm.test("CP-HU07-F1 · Bearer, 3600 s y rol PROVEEDOR", () => { const b = pm.response.json(); pm.expect(b.tokenType).to.eql("Bearer"); pm.expect(b.expiresIn).to.eql(3600); pm.expect(b.role).to.eql("PROVEEDOR"); });',
                save("tokenProveedorA", "pm.response.json().accessToken", "")]),
    req("CP-HU07-F2 · Iniciar sesión como cliente", "POST", "/api/v1/auth/login",
        d("Obtener el token del cliente.", "200; role CLIENTE.", "Revisa role en la respuesta."),
        {"correo": CLIENTE, "password": "{{clave}}"},
        checks=[status(200, "CP-HU07-F2"), save("tokenCliente", "pm.response.json().accessToken", "")]),
    req("CP-HU07-E1 · Contraseña incorrecta", "POST", "/api/v1/auth/login",
        d("Rechazar una contraseña incorrecta.", "401; «Correo o contraseña incorrectos».", "Lee el cuerpo de la respuesta.",
          estado="No pasa: 401 sin mensaje, con {authenticated:false,...} (bug #193)."),
        {"correo": CLIENTE, "password": "Otra-Clave-Equivocada-99"},
        checks=[status(401, "CP-HU07-E1"), contains("Correo o contraseña incorrectos", "CP-HU07-E1")]),
    req("CP-HU07-E2 · Correo no registrado", "POST", "/api/v1/auth/login",
        d("No revelar si el correo existe.", "401; el mismo mensaje de E1.", "Compara el cuerpo con el de E1: deben ser iguales.",
          estado="No pasa: igual que E1 (bug #193)."),
        {"correo": "qa.s2.nadie.{{sufijo}}@bookify-qa.test", "password": "{{clave}}"},
        checks=[status(401, "CP-HU07-E2"), contains("Correo o contraseña incorrectos", "CP-HU07-E2")]),
    req("CP-HU07-E3 · Campos obligatorios vacíos", "POST", "/api/v1/auth/login",
        d("Rechazar correo y contraseña vacíos.", "400.", "Revisa el código."), {"correo": "", "password": ""},
        checks=[status(400, "CP-HU07-E3")]),
    req("CP-HU07-E5 · Endpoint protegido sin token", "POST", "/api/v1/bookings",
        d("Un endpoint protegido exige token.", "401.", "Envía la petición sin la cabecera Authorization."),
        {"clienteId": "{{clienteId}}"}, checks=[status(401, "CP-HU07-E5")]),
    req("CP-HU07-E6 · Token CLIENTE en endpoint de proveedor", "POST", "/api/v1/disponibilidades",
        d("Un rol sin permiso recibe 403.", "403.", "Usa el token del cliente en un endpoint de proveedor."),
        {"agendaServicioId": "{{agendaOtroComercio}}", "inicioAt": "{{horarioInicio}}", "finAt": "{{horarioFin}}"},
        token="tokenCliente", checks=[status(403, "CP-HU07-E6")]),
])

fail = lambda n: req(f"CP-HU08-P{n} · Intento fallido {n} de 4", "POST", "/api/v1/auth/login",
                     d(f"Acumular el intento fallido {n}.", "401.", "Revisa que responda 401; todavía no hay bloqueo."),
                     {"correo": BLOQUEO, "password": "Mala-Clave-Intento-00"}, checks=[status(401, f"CP-HU08-P{n}")])
hu08 = folder("CP-HU08 – Bloquear temporalmente el acceso",
    "HU-08. El bloqueo es por correo, exista o no la cuenta. Ejecuta las peticiones en orden. "
    "**Bloqueado (no está en la colección):** F2 acceso al terminar el bloqueo, porque los 15 minutos están fijos en el código "
    "y no son configurables (bug #177); para probarlo a mano espera 15 minutos y repite el inicio de sesión correcto.", [
    req("CP-HU08-P0 · Registrar la cuenta que se va a bloquear", "POST", "/api/v1/auth/register",
        d("Crear una cuenta dedicada para no bloquear las de otras carpetas.", "201.", "Revisa el código."),
        {"nombre": "Bloqueo QA", "correo": BLOQUEO, "contrasena": "{{clave}}", "rol": "CLIENTE"}, checks=[status(201, "CP-HU08-P0")]),
    fail(1), fail(2), fail(3), fail(4),
    req("CP-HU08-F1 · Quinto intento fallido: bloqueo", "POST", "/api/v1/auth/login",
        d("Bloquear al quinto intento fallido consecutivo.",
          "429, encabezado Retry-After en 900 segundos y «Demasiados intentos fallidos. Intenta de nuevo en 15 minutos».",
          "Revisa la pestaña Headers (Retry-After) y el cuerpo.", estado="No pasa por el mensaje: «La cuenta está bloqueada temporalmente.» (bug #177). Retry-After sí llega."),
        {"correo": BLOQUEO, "password": "Mala-Clave-Intento-05"},
        checks=[status(429, "CP-HU08-F1"), 'pm.test("CP-HU08-F1 · Retry-After presente", () => pm.response.to.have.header("Retry-After"));',
                contains("Demasiados intentos fallidos", "CP-HU08-F1")]),
    req("CP-HU08-E1 · Credenciales correctas durante el bloqueo", "POST", "/api/v1/auth/login",
        d("Rechazar el acceso durante el bloqueo aunque la contraseña sea correcta.", "429; informa los minutos restantes; sin token.",
          "Ejecútala justo después de F1.", estado="No pasa: no indica los minutos restantes (bug #177)."),
        {"correo": BLOQUEO, "password": "{{clave}}"}, checks=[status(429, "CP-HU08-E1"), contains("minutos", "CP-HU08-E1")]),
    req("CP-HU08-E2a · Registrar la cuenta del reinicio", "POST", "/api/v1/auth/register",
        d("Cuenta dedicada al escenario de reinicio del contador.", "201.", "Revisa el código."),
        {"nombre": "Reinicio QA", "correo": REINICIO, "contrasena": "{{clave}}", "rol": "CLIENTE"}, checks=[status(201, "CP-HU08-E2a")]),
    req("CP-HU08-E2b · Tres intentos fallidos (envíala 3 veces)", "POST", "/api/v1/auth/login",
        d("Acumular 3 intentos fallidos.", "401 cada vez.", "Pulsa Send tres veces."),
        {"correo": REINICIO, "password": "Mala-Clave-Intento-00"}, checks=[status(401, "CP-HU08-E2b")]),
    req("CP-HU08-E2c · Inicio de sesión correcto", "POST", "/api/v1/auth/login",
        d("Un inicio correcto reinicia el contador.", "200 y token.", "Revisa el código."),
        {"correo": REINICIO, "password": "{{clave}}"}, checks=[status(200, "CP-HU08-E2c")]),
    req("CP-HU08-E2d · Nuevo intento fallido: sin bloqueo", "POST", "/api/v1/auth/login",
        d("Confirmar que el contador volvió a cero.", "401 (no 429).", "Si respondiera 429, el contador no se reinició."),
        {"correo": REINICIO, "password": "Mala-Clave-Intento-04"}, checks=[status(401, "CP-HU08-E2d")]),
    req("CP-HU08-E3 · Correo no registrado (envíala 5 veces)", "POST", "/api/v1/auth/login",
        d("Aplicar el mismo bloqueo a un correo que no existe.", "Intentos 1 a 4: 401. Quinto: 429, igual que una cuenta existente.",
          "Pulsa Send cinco veces y compara el quinto código con el de F1."),
        {"correo": FANTASMA, "password": "Mala-Clave-Intento-00"},
        checks=['pm.test("CP-HU08-E3 · 401 antes del bloqueo o 429 al quinto", () => pm.expect(pm.response.code).to.be.oneOf([401, 429]));']),
])

hu09 = folder("CP-HU09 – Crear perfil comercial",
    "HU-09. Categorías válidas: Barbería y belleza, Deportes y recreación, Salud y bienestar, Educación, Otros. "
    "Al final se crea también el comercio B, que usan HU-10 (E4) y HU-12 (E4).", [
    req("CP-HU09-E4a · Crear perfil sin token", "POST", "/api/v1/comercio/perfil",
        d("Exigir autenticación.", "401.", "Envía sin Authorization."), PERFIL_A, checks=[status(401, "CP-HU09-E4a")]),
    req("CP-HU09-E4b · Crear perfil con token de cliente", "POST", "/api/v1/comercio/perfil",
        d("Solo un proveedor crea perfiles.", "403.", "Usa tokenCliente."), PERFIL_A, token="tokenCliente", checks=[status(403, "CP-HU09-E4b")]),
    req("CP-HU09-E1 · Perfil sin nombre comercial", "POST", "/api/v1/comercio/perfil",
        d("Rechazar datos obligatorios incompletos.", "400; lista el campo faltante.", "Busca «nombre comercial» en la respuesta.",
          estado="No pasa: 400 genérico sin el campo (bug #192)."),
        {**PERFIL_A, "businessName": ""}, token="tokenProveedorA",
        checks=[status(400, "CP-HU09-E1"), 'pm.test("CP-HU09-E1 · indica el campo", () => pm.expect(pm.response.text().toLowerCase()).to.include("nombre comercial"));']),
    req("CP-HU09-E2 · Perfil sin categoría", "POST", "/api/v1/comercio/perfil",
        d("Rechazar datos obligatorios incompletos.", "400; lista el campo faltante.", "Busca «categoría» en la respuesta.",
          estado="No pasa: 400 genérico sin el campo (bug #192)."),
        {**PERFIL_A, "category": ""}, token="tokenProveedorA",
        checks=[status(400, "CP-HU09-E2"), 'pm.test("CP-HU09-E2 · indica el campo", () => pm.expect(pm.response.text().toLowerCase()).to.include("categor"));']),
    req("CP-HU09-E5 · Categoría fuera del catálogo", "POST", "/api/v1/comercio/perfil",
        d("Rechazar una categoría que no está en el catálogo.", "400; lista las categorías válidas.", "Lee el mensaje."),
        {**PERFIL_A, "category": "Mascotas exóticas"}, token="tokenProveedorA",
        checks=[status(400, "CP-HU09-E5"), contains("Barbería y belleza", "CP-HU09-E5")]),
    req("CP-HU09-F1 · Crear un perfil comercial válido", "POST", "/api/v1/comercio/perfil",
        d("Crear el perfil del comercio A.", "201; id del perfil y los datos enviados.", "Copia el id: es el idProveedor de HU-10 y HU-11."),
        PERFIL_A, token="tokenProveedorA", checks=[status(201, "CP-HU09-F1"), save("perfilAId", "pm.response.json().id", "")]),
    req("CP-HU09-E3 · Segundo perfil del mismo proveedor", "POST", "/api/v1/comercio/perfil",
        d("Un proveedor tiene un solo perfil.", "409; «Ya tienes un perfil comercial registrado».", "Reenvía el mismo cuerpo de F1."),
        PERFIL_A, token="tokenProveedorA",
        checks=[status(409, "CP-HU09-E3"), contains("Ya tienes un perfil comercial registrado", "CP-HU09-E3")]),
    req("CP-HU09-F1b · El comercio aparece en la búsqueda", "GET", "/api/v1/catalog/services/search?texto=Barbería QA {{sufijo}}",
        d("Criterio de HU-09: la consulta de servicios muestra el comercio.", "200 y el comercio entre los resultados.",
          "Busca el nombre comercial de F1.", estado="No pasa: un comercio nuevo no puede publicar servicios ni horarios (bugs #190 y #181)."),
        checks=[status(200, "CP-HU09-F1b"),
                'pm.test("CP-HU09-F1b · el comercio aparece", () => pm.expect(pm.response.json().servicios.map(s => s.provider)).to.include("Barbería QA " + pm.environment.get("sufijo")));']),
    req("CP-HU09-P1 · Registrar el proveedor B", "POST", "/api/v1/auth/register",
        d("Preparar el comercio B.", "201.", "Revisa el código."),
        {"nombre": "Marta QA Estadio", "correo": PROV_B, "contrasena": "{{clave}}", "rol": "PROVEEDOR"}, checks=[status(201, "CP-HU09-P1")]),
    req("CP-HU09-P2 · Iniciar sesión como proveedor B", "POST", "/api/v1/auth/login",
        d("Token del comercio B.", "200.", "Copia accessToken."), {"correo": PROV_B, "password": "{{clave}}"},
        checks=[status(200, "CP-HU09-P2"), save("tokenProveedorB", "pm.response.json().accessToken", "")]),
    req("CP-HU09-P3 · Crear el perfil del comercio B", "POST", "/api/v1/comercio/perfil",
        d("Comercio B con otra categoría.", "201.", "Copia el id: lo usa HU-10 E4."),
        {**PERFIL_A, "businessName": "Estadio QA {{sufijo}}", "category": "Deportes y recreación"}, token="tokenProveedorB",
        checks=[status(201, "CP-HU09-P3"), save("perfilBId", "pm.response.json().id", "")]),
])

dia = lambda fecha, perfil="perfilAId": {"idProveedor": f"{{{{{perfil}}}}}", "fecha": fecha, "motivo": "Prueba QA Sprint 2", "idUsuarioCreador": "{{proveedorAUserId}}"}
hu10 = folder("CP-HU10 – Registrar feriados y días de descanso",
    "HU-10. Fechas en America/Bogota. El endpoint recibe idProveedor e idUsuarioCreador en el cuerpo. "
    "**Bloqueado (no está en la colección):** F2 «el cliente no ve horarios en la fecha bloqueada», porque el comercio de QA "
    "no puede publicar horarios (bug #190).", [
    req("CP-HU10-F1 · Registrar un día no laborable", "POST", "/api/v1/dias-no-laborables",
        d("Bloquear una fecha sin reservas en el comercio A.", "201 y la fecha registrada.", "Revisa fecha en la respuesta."),
        dia("{{fechaNoLaborable}}"), token="tokenProveedorA", checks=[status(201, "CP-HU10-F1")]),
    req("CP-HU10-E1 · Fecha ya registrada", "POST", "/api/v1/dias-no-laborables",
        d("No registrar dos veces la misma fecha.", "409; «La fecha ya está registrada como no laborable».", "Reenvía el cuerpo de F1."),
        dia("{{fechaNoLaborable}}"), token="tokenProveedorA",
        checks=[status(409, "CP-HU10-E1"), contains("La fecha ya está registrada como no laborable", "CP-HU10-E1")]),
    req("CP-HU10-E2 · Fecha con formato inválido", "POST", "/api/v1/dias-no-laborables",
        d("Rechazar un formato de fecha inválido.", "400.", "Revisa el código."), dia("fecha-invalida"), token="tokenProveedorA",
        checks=[status(400, "CP-HU10-E2")]),
    req("CP-HU10-E3 · Fecha pasada", "POST", "/api/v1/dias-no-laborables",
        d("Rechazar una fecha anterior a hoy.", "400; «La fecha debe ser igual o posterior a hoy».", "Lee el mensaje.",
          estado="No pasa: responde 500 (bug #194)."),
        dia("{{fechaPasada}}"), token="tokenProveedorA",
        checks=[status(400, "CP-HU10-E3"), contains("posterior a hoy", "CP-HU10-E3")]),
    req("CP-HU10-E4 · Bloquear una fecha de OTRO comercio", "POST", "/api/v1/dias-no-laborables",
        d("Un proveedor solo bloquea fechas de su comercio.", "403.",
          "Con el token del proveedor A envía el idProveedor del comercio B.",
          estado="No pasa: responde 201 y bloquea la fecha del comercio B (bug #188, seguridad)."),
        dia("{{fechaOtroComercio}}", "perfilBId"), token="tokenProveedorA", checks=[status(403, "CP-HU10-E4")]),
])

hu11 = folder("CP-HU11 – Gestionar reservas existentes al bloquear una fecha",
    "HU-11. **Bloqueados (no están en la colección):** F1 advertir sobre reservas existentes (409 con la lista), F2 confirmar el "
    "bloqueo, E1 sin confirmación la fecha sigue laborable y E2 fecha solo con reservas canceladas. Requieren reservas en una "
    "agenda del comercio que bloquea, y el comercio de QA no puede tener agenda por API (bug #190). Además, la confirmación no "
    "existe: el POST bloquea aunque haya reservas (bug #191), y la previsualización filtra por fecha de creación (bug #176).", [
    req("CP-HU11-P1 · Previsualizar una fecha sin reservas", "POST", "/api/v1/dias-no-laborables/previsualizar",
        d("Ver cuántas reservas afectaría bloquear una fecha.", "200 y reservasAfectadas = 0.", "Revisa reservasAfectadas e idsReservasAfectadas."),
        dia("{{fechaPrevisualizar}}"), token="tokenProveedorA",
        checks=[status(200, "CP-HU11-P1"), 'pm.test("CP-HU11-P1 · 0 reservas afectadas", () => pm.expect(pm.response.json().reservasAfectadas).to.eql(0));']),
])

hu12 = folder("CP-HU12 – Registrar un recurso",
    "HU-12. Nombre de 1 a 60 caracteres, único por comercio sin distinguir mayúsculas. Tipos: FISICO o HUMANO.", [
    req("CP-HU12-P1 · Registrar un proveedor sin perfil (C)", "POST", "/api/v1/auth/register",
        d("Preparar un proveedor que no tiene perfil comercial.", "201.", "Revisa el código."),
        {"nombre": "Pedro QA Sin Perfil", "correo": PROV_C, "contrasena": "{{clave}}", "rol": "PROVEEDOR"}, checks=[status(201, "CP-HU12-P1")]),
    req("CP-HU12-P2 · Iniciar sesión como proveedor C", "POST", "/api/v1/auth/login",
        d("Token del proveedor sin perfil.", "200.", "Copia accessToken."), {"correo": PROV_C, "password": "{{clave}}"},
        checks=[status(200, "CP-HU12-P2"), save("tokenProveedorC", "pm.response.json().accessToken", "")]),
    req("CP-HU12-E2 · Proveedor sin perfil comercial", "POST", "/api/v1/comercio/recursos",
        d("Exigir perfil comercial.", "409; «Primero debes crear tu perfil comercial».", "Usa tokenProveedorC."),
        {"name": "Cancha 1", "type": "FISICO"}, token="tokenProveedorC",
        checks=[status(409, "CP-HU12-E2"), contains("Primero debes crear tu perfil comercial", "CP-HU12-E2")]),
    req("CP-HU12-F1 · Registrar un recurso físico", "POST", "/api/v1/comercio/recursos",
        d("Registrar «Cancha 1» en el comercio A.", "201; status ACTIVO y providerId = perfilAId.", "Compara providerId con perfilAId."),
        {"name": "Cancha 1", "type": "FISICO"}, token="tokenProveedorA",
        checks=[status(201, "CP-HU12-F1"), 'pm.test("CP-HU12-F1 · ACTIVO en mi comercio", () => { const b = pm.response.json(); pm.expect(b.status).to.eql("ACTIVO"); pm.expect(b.providerId).to.eql(pm.environment.get("perfilAId")); });']),
    req("CP-HU12-F2 · Registrar un recurso humano", "POST", "/api/v1/comercio/recursos",
        d("Registrar «Barbero Carlos».", "201; ACTIVO.", "Revisa type y status."), {"name": "Barbero Carlos", "type": "HUMANO"},
        token="tokenProveedorA", checks=[status(201, "CP-HU12-F2")]),
    req("CP-HU12-E1 · Recurso duplicado sin distinguir mayúsculas", "POST", "/api/v1/comercio/recursos",
        d("El nombre es único por comercio.", "409; «Ya existe un recurso con ese nombre en tu comercio».", "Envía «cancha 1» en minúsculas."),
        {"name": "cancha 1", "type": "FISICO"}, token="tokenProveedorA",
        checks=[status(409, "CP-HU12-E1"), contains("Ya existe un recurso con ese nombre en tu comercio", "CP-HU12-E1")]),
    req("CP-HU12-E3a · Tipo de recurso inválido", "POST", "/api/v1/comercio/recursos",
        d("Solo FISICO o HUMANO.", "400; indica que el tipo debe ser FISICO o HUMANO.", "Lee el mensaje."),
        {"name": "Sala Virtual", "type": "VIRTUAL"}, token="tokenProveedorA", checks=[status(400, "CP-HU12-E3a"), contains("FISICO o HUMANO", "CP-HU12-E3a")]),
    req("CP-HU12-E3b · Nombre vacío", "POST", "/api/v1/comercio/recursos",
        d("Rechazar un nombre vacío.", "400.", "Revisa el código."), {"name": "", "type": "FISICO"}, token="tokenProveedorA",
        checks=[status(400, "CP-HU12-E3b")]),
    req("CP-HU12-E4 · Mismo nombre en otro comercio", "POST", "/api/v1/comercio/recursos",
        d("La unicidad es por comercio.", "201.", "Usa tokenProveedorB con el nombre «Cancha 1»."),
        {"name": "Cancha 1", "type": "FISICO"}, token="tokenProveedorB", checks=[status(201, "CP-HU12-E4")]),
    req("CP-HU12-E5 · Token de cliente", "POST", "/api/v1/comercio/recursos",
        d("Un cliente no registra recursos.", "403.", "Usa tokenCliente."), {"name": "Cancha 9", "type": "FISICO"}, token="tokenCliente",
        checks=[status(403, "CP-HU12-E5")]),
])

hu13 = folder("CP-HU13 – Gestionar la agenda de cada recurso",
    "HU-13. **Antes de cada corrida cambia `horarioInicio`, `horarioFin`, `horarioCruceInicio` y `horarioCruceFin`** (si repites "
    "el horario, E2 responderá 409 por cruce). **No ejecutable (no está en la colección):** F1 «registrar horarios en la agenda de "
    "un recurso», porque no existe un endpoint que reciba el recurso: POST /api/v1/disponibilidades solo acepta agendaServicioId (bug #190).", [
    req("CP-HU13-E2 · Registrar un horario en la agenda de OTRO comercio", "POST", "/api/v1/disponibilidades",
        d("Un proveedor solo publica horarios en su comercio.", "403.",
          "agendaOtroComercio es la agenda de prueba del Sprint 1, que pertenece a otro comercio.",
          estado="No pasa: responde 201 y crea el horario (bug #189, seguridad). La respuesta trae el id del horario, que usan E1 y E3."),
        {"agendaServicioId": "{{agendaOtroComercio}}", "inicioAt": "{{horarioInicio}}", "finAt": "{{horarioFin}}"}, token="tokenProveedorA",
        checks=[status(403, "CP-HU13-E2"), save("disponibilidadId", "pm.response.json().id", "")]),
    req("CP-HU13-E1 · Horario cruzado", "POST", "/api/v1/disponibilidades",
        d("No permitir horarios cruzados en la misma agenda.", "409; «El horario se cruza con otro horario del recurso».",
          "El horario empieza 30 minutos después del de E2.", "Haber ejecutado E2.",
          estado="No pasa por el mensaje: «El rango de horario ingresado se traslapa con un horario ya registrado.» (bug #196)."),
        {"agendaServicioId": "{{agendaOtroComercio}}", "inicioAt": "{{horarioCruceInicio}}", "finAt": "{{horarioCruceFin}}"}, token="tokenProveedorA",
        checks=[status(409, "CP-HU13-E1"), contains("se cruza", "CP-HU13-E1")]),
    req("CP-HU13-F2 · Cada horario muestra el id y el nombre del recurso", "GET", "/api/v1/catalog/services",
        d("El cliente elige el recurso al reservar.", "200; cada horario incluye el id y el nombre del recurso.",
          "Abre availableSchedules de cualquier servicio.", estado="No pasa: el catálogo no trae el recurso (bug #195)."),
        checks=[status(200, "CP-HU13-F2"),
                'pm.test("CP-HU13-F2 · cada horario trae el recurso", () => pm.response.json().forEach(s => s.availableSchedules.forEach(h => pm.expect(h).to.have.property("recursoId"))));']),
    req("CP-HU13-P1 · Registrar un segundo cliente", "POST", "/api/v1/auth/register",
        d("Preparar al cliente B para E3a.", "201.", "Copia el id."),
        {"nombre": "Luis QA Pérez", "correo": CLIENTE_B, "contrasena": "{{clave}}", "rol": "CLIENTE"},
        checks=[status(201, "CP-HU13-P1"), save("clienteBId", "pm.response.json().id", "")]),
    req("CP-HU13-E3a · Reservar a nombre de OTRO cliente", "POST", "/api/v1/bookings",
        d("Un cliente solo reserva a su nombre.", "403.", "Con tokenCliente envía el clienteId del cliente B.",
          "Haber ejecutado E2 (disponibilidadId).", estado="Bloqueado: crear reservas responde 500 en Render (bug #187)."),
        {"clienteId": "{{clienteBId}}", "disponibilidadId": "{{disponibilidadId}}"}, token="tokenCliente",
        checks=[status(403, "CP-HU13-E3a")]),
    req("CP-HU13-E3b · Reservar el horario a mi nombre", "POST", "/api/v1/bookings",
        d("Reserva válida del cliente A.", "201 y estado CONFIRMADA.", "Copia el id de la reserva.",
          estado="No pasa: responde 500 (bug #187)."),
        {"clienteId": "{{clienteId}}", "disponibilidadId": "{{disponibilidadId}}"}, token="tokenCliente",
        checks=[status(201, "CP-HU13-E3b"), save("reservaId", "pm.response.json().id", "")]),
    req("CP-HU13-E3 · Doble reserva sobre el mismo horario", "POST", "/api/v1/bookings",
        d("Impedir la doble reserva sobre el mismo recurso.", "409; «El recurso no está disponible en ese horario».",
          "Reenvía E3b con el mismo horario.", "E3b respondió 201.", estado="Bloqueado por el bug #187; ver también #186."),
        {"clienteId": "{{clienteId}}", "disponibilidadId": "{{disponibilidadId}}"}, token="tokenCliente",
        checks=[status(409, "CP-HU13-E3"), contains("El recurso no está disponible en ese horario", "CP-HU13-E3")]),
])

hu14 = folder("CP-HU14 – Buscar servicios por nombre o categoría",
    "HU-14. Endpoint público. La coincidencia es parcial y no distingue mayúsculas ni tildes. La categoría es la del perfil comercial (HU-09). "
    "Los valores de `textoBusqueda` y `proveedorBusqueda` dependen de los datos de Render: revisa el resultado de E4 (búsqueda amplia) y ajústalos.", [
    req("CP-HU14-F1 · Buscar por nombre", "GET", "/api/v1/catalog/services/search?texto={{textoBusqueda}}",
        d("Encontrar servicios cuyo nombre contiene el texto.", "200; solo servicios que coinciden, con proveedor, categoría y horarios con su recurso.",
          "Revisa que cada service o provider contenga el texto."),
        checks=[status(200, "CP-HU14-F1"), 'pm.test("CP-HU14-F1 · hay resultados", () => pm.expect(pm.response.json().servicios.length).to.be.above(0));']),
    req("CP-HU14-F2 · Buscar por proveedor, en minúsculas y sin tildes", "GET", "/api/v1/catalog/services/search?texto={{proveedorBusqueda}}",
        d("Encontrar los servicios de un proveedor.", "200; servicios de ese proveedor.", "Revisa provider en cada resultado."),
        checks=[status(200, "CP-HU14-F2")]),
    req("CP-HU14-F3 · Buscar por categoría «Deportes y recreación»", "GET", "/api/v1/catalog/services/search?categoria=Deportes y recreación",
        d("Filtrar por una categoría del catálogo de HU-09.", "200; solo servicios de esa categoría.", "Revisa category en cada resultado.",
          estado="No pasa: 400 «no existe en el catálogo» (bug #181)."), checks=[status(200, "CP-HU14-F3")]),
    req("CP-HU14-F4 · Texto y categoría sin tilde", "GET", "/api/v1/catalog/services/search?texto=futbol&categoria=Deportes y recreacion",
        d("Combinar filtros sin tildes.", "200; cumple ambos filtros.", "Revisa service y category.",
          estado="No pasa: 400 (bugs #181 y #182)."), checks=[status(200, "CP-HU14-F4")]),
    req("CP-HU14-E1 · Búsqueda sin resultados («Yoga»)", "GET", "/api/v1/catalog/services/search?texto=Yoga",
        d("Informar cuando no hay coincidencias.", "200; lista vacía y «No se encontraron servicios».", "Revisa servicios y mensaje."),
        checks=[status(200, "CP-HU14-E1"), 'pm.test("CP-HU14-E1 · lista vacía y mensaje", () => { const b = pm.response.json(); pm.expect(b.servicios).to.eql([]); pm.expect(b.mensaje).to.eql("No se encontraron servicios"); });']),
    req("CP-HU14-E2 · Búsqueda sin criterio", "GET", "/api/v1/catalog/services/search",
        d("Exigir al menos un criterio.", "400; el mensaje indica el criterio inválido.", "Lee el mensaje."),
        checks=[status(400, "CP-HU14-E2"), contains("criterio", "CP-HU14-E2")]),
    req("CP-HU14-E3 · Categoría fuera del catálogo", "GET", "/api/v1/catalog/services/search?categoria=Inexistente-QA",
        d("Rechazar una categoría inexistente.", "400.", "Lee el mensaje."), checks=[status(400, "CP-HU14-E3")]),
    req("CP-HU14-E4 · No mostrar horarios que ya pasaron", "GET", "/api/v1/catalog/services/search?texto=a",
        d("Solo se muestran horarios reservables.", "200; todos los inicioAt son posteriores a ahora.",
          "Compara cada inicioAt con la fecha y hora actuales.", estado="No pasa: aparecen horarios pasados (bug #183)."),
        checks=[status(200, "CP-HU14-E4"),
                'const ahora = new Date().toISOString();\npm.test("CP-HU14-E4 · todos los horarios son futuros", () => pm.response.json().servicios.forEach(s => s.availableSchedules.forEach(h => pm.expect(h.inicioAt > ahora, h.inicioAt).to.be.true)));']),
])

collection = {
    "info": {"_postman_id": str(uuid.uuid5(uuid.NAMESPACE_URL, "bookify-cp-hu06-hu14")),
             "name": "Bookify – Casos de prueba HU-06 a HU-14",
             "description": (
                 "Casos de prueba del Sprint 2 (CP-HU06 a CP-HU14), refinados contra el Gherkin final de Azure Boards. "
                 "Diseñados para ejecutarse a mano, en orden, carpeta por carpeta.\n\n"
                 "**Antes de cada corrida**, en el entorno «Bookify QA Sprint 2»: cambia `sufijo` (por ejemplo, la fecha y la hora: 1027a) y "
                 "los cuatro horarios de HU-13. Nada más.\n\n"
                 "**Cómo leer cada petición:** la pestaña Docs tiene el objetivo, la precondición, el resultado esperado según Azure, cómo "
                 "verificarlo a mano y el estado conocido con su bug. La pestaña Tests solo compara el código y el mensaje con lo esperado y "
                 "guarda el id o el token que usa la siguiente petición; puedes borrar esos scripts y copiar los valores al entorno a mano.\n\n"
                 "**Datos:** las cuentas usan correos @bookify-qa.test y los comercios llevan el sufijo en el nombre. HU-13 crea un horario de 2027 "
                 "en la agenda de prueba del Sprint 1. La API no permite borrar estos datos."),
             "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json"},
    "item": [folder("00 · Preparación", "Ejecútala primero: el servicio gratuito de Render se duerme y la primera petición puede tardar cerca de un minuto.", [
                 req("00 · Despertar Render", "GET", "/api/v1/catalog/services",
                     d("Despertar el servicio antes de la corrida.", "200 (puede tardar hasta un minuto la primera vez).",
                       "Si responde con error o tarda demasiado, espera unos segundos y vuelve a enviarla."),
                     checks=[status(200, "00")])]),
             hu06, hu07, hu08, hu09, hu10, hu11, hu12, hu13, hu14],
}

env_vars = [
    ("baseUrl", "https://bookify-pb92.onrender.com", "default"),
    ("sufijo", "1027a", "default"),
    ("clave", "Calidad-Sprint2-Clave-2026!", "secret"),
    ("agendaOtroComercio", "6f085c7d-a713-4657-9fbf-17c914609768", "default"),
    ("fechaNoLaborable", "2027-04-10", "default"),
    ("fechaOtroComercio", "2027-04-11", "default"),
    ("fechaPrevisualizar", "2027-04-12", "default"),
    ("fechaPasada", "2026-09-20", "default"),
    ("horarioInicio", "2027-04-05T15:00:00Z", "default"),
    ("horarioFin", "2027-04-05T16:00:00Z", "default"),
    ("horarioCruceInicio", "2027-04-05T15:30:00Z", "default"),
    ("horarioCruceFin", "2027-04-05T16:30:00Z", "default"),
    ("textoBusqueda", "futbol", "default"),
    ("proveedorBusqueda", "deportes hu", "default"),
] + [(k, "", "default") for k in ("clienteId", "clienteBId", "proveedorAUserId", "perfilAId", "perfilBId", "tokenProveedorA",
                                   "tokenProveedorB", "tokenProveedorC", "tokenCliente", "disponibilidadId", "reservaId")]
environment = {"id": str(uuid.uuid5(uuid.NAMESPACE_URL, "bookify-qa-sprint2-env")), "name": "Bookify QA Sprint 2",
               "values": [{"key": k, "value": v, "type": t, "enabled": True} for k, v, t in env_vars],
               "_postman_variable_scope": "environment"}

COLLECTION.write_text(json.dumps(collection, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
ENVIRONMENT.write_text(json.dumps(environment, ensure_ascii=False, indent=2) + "\n", encoding="utf-8")
total = sum(len(f["item"]) for f in collection["item"])
print(f"{COLLECTION.name}: {len(collection['item'])} carpetas, {total} peticiones")
print(f"{ENVIRONMENT.name}: {len(env_vars)} variables")
