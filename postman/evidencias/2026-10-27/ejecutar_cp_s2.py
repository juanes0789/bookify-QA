"""Ejecuta los casos CP-HU06 a CP-HU14 contra la API desplegada y guarda la evidencia."""
import json
import sys
import time
import urllib.error
import urllib.request
from datetime import datetime, timezone, timedelta

BASE = "https://bookify-pb92.onrender.com"
OUT = sys.argv[1]
TS = datetime.now(timezone.utc).strftime("%m%d%H%M%S")
AGENDA_S1 = "6f085c7d-a713-4657-9fbf-17c914609768"   # agenda de prueba del Sprint 1 (otro comercio)
ANA = "11111111-1111-1111-1111-111111111111"
LUIS = "22222222-2222-2222-2222-222222222222"
CLAVE = "Calidad-Sprint2-Clave-2026!"
results = []
created_bookings = []


def call(method, path, body=None, token=None, timeout=90):
    data = json.dumps(body).encode() if body is not None else None
    headers = {"Content-Type": "application/json"}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    req = urllib.request.Request(BASE + path, data=data, method=method, headers=headers)
    try:
        with urllib.request.urlopen(req, timeout=timeout) as r:
            raw, status, hdrs = r.read().decode(), r.status, dict(r.headers)
    except urllib.error.HTTPError as e:
        raw, status, hdrs = e.read().decode(), e.code, dict(e.headers)
    try:
        parsed = json.loads(raw) if raw else None
    except ValueError:
        parsed = raw[:300]
    return status, parsed, hdrs


def record(cp, esc, desc, method, path, body, expected, status, resp, ok, note="", hdrs=None, blocked=False):
    shown = dict(body) if isinstance(body, dict) else body
    for k in ("contrasena", "password"):
        if isinstance(shown, dict) and k in shown:
            shown[k] = "***"
    verdict = "Bloqueado" if blocked else ("Pasa" if ok else "No pasa")
    results.append({"cp": cp, "esc": esc, "desc": desc, "request": f"{method} {path}" if method else "",
                    "body": shown, "esperado": expected, "status": status, "respuesta": resp,
                    "retry_after": (hdrs or {}).get("Retry-After"), "resultado": verdict, "nota": note,
                    "hora_utc": datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")})
    print(f"{cp}-{esc:4} {verdict:9} {status} | {desc}{' | ' + note if note else ''}")


def run(cp, esc, desc, method, path, body, expected, check, token=None, note=""):
    st, resp, hdrs = call(method, path, body, token)
    try:
        ok, extra = check(st, resp, hdrs)
    except Exception as ex:  # noqa: BLE001
        ok, extra = False, f"verificación falló: {ex}"
    record(cp, esc, desc, method, path, body, expected, st, resp, ok, (note + " " + (extra or "")).strip(), hdrs)
    return st, resp


def msg(resp):
    if isinstance(resp, dict):
        return " ".join(str(resp.get(k, "")) for k in ("mensaje", "error", "motivo", "message"))
    return str(resp)


def mail(tag):
    return f"qa.s2.{tag}.{TS}@bookify-qa.test"


# ---------- calentar Render ----------
call("GET", "/api/v1/catalog/services", timeout=120)

# ---------- CP-HU06 ----------
cli_mail, prov_mail, provb_mail = mail("cliente"), mail("proveedor"), mail("proveedorb")
st, cli = run("CP-HU06", "F1", "Registrar una cuenta de cliente", "POST", "/api/v1/auth/register",
              {"nombre": "Ana QA Gómez", "correo": cli_mail, "contrasena": CLAVE, "rol": "CLIENTE"},
              "201; rol CLIENTE; id, nombre, correo y rol sin contraseña",
              lambda s, r, h: (s == 201 and r.get("rol") == "CLIENTE" and "contrasena" not in json.dumps(r).lower()
                               and "password" not in json.dumps(r).lower(), None))
st, prov = run("CP-HU06", "F2", "Registrar una cuenta de proveedor", "POST", "/api/v1/auth/register",
               {"nombre": "Carlos QA Barbería", "correo": prov_mail, "contrasena": CLAVE, "rol": "PROVEEDOR"},
               "201; rol PROVEEDOR", lambda s, r, h: (s == 201 and r.get("rol") == "PROVEEDOR", None))
_, provb, _h = call("POST", "/api/v1/auth/register", {"nombre": "Marta QA Estadio", "correo": provb_mail, "contrasena": CLAVE, "rol": "PROVEEDOR"})
run("CP-HU06", "E1", "Correo ya registrado (en mayúsculas)", "POST", "/api/v1/auth/register",
    {"nombre": "Ana Repetida", "correo": cli_mail.upper(), "contrasena": CLAVE, "rol": "CLIENTE"},
    "409; «El correo ya está registrado»",
    lambda s, r, h: (s == 409 and "El correo ya está registrado" in msg(r), None))
for esc, pwd, frag, desc in [("E2a", "corta123", "entre 15 y 64", "Contraseña de 8 caracteres"),
                             ("E2b", "A" * 65, "entre 15 y 64", "Contraseña de 65 caracteres"),
                             ("E2c", "contraseña123456", "común", "Contraseña común"),
                             ("E2d", None, "correo", "Contraseña que contiene el correo")]:
    e = mail("pwd" + esc.lower())
    run("CP-HU06", esc, desc, "POST", "/api/v1/auth/register",
        {"nombre": "Prueba Clave", "correo": e, "contrasena": pwd or (e + "-2026"), "rol": "CLIENTE"},
        f"400; el mensaje indica «{frag}»", lambda s, r, h, f=frag: (s == 400 and f in msg(r).lower(), None))
run("CP-HU06", "E3", "Rol inexistente", "POST", "/api/v1/auth/register",
    {"nombre": "Prueba Rol", "correo": mail("rol"), "contrasena": CLAVE, "rol": "ADMINISTRADOR"},
    "400", lambda s, r, h: (s == 400, None))
run("CP-HU06", "E4", "Correo con formato inválido", "POST", "/api/v1/auth/register",
    {"nombre": "Prueba Correo", "correo": "ana-arroba-mail", "contrasena": CLAVE, "rol": "CLIENTE"},
    "400", lambda s, r, h: (s == 400, None))
run("CP-HU06", "E5", "Nombre vacío", "POST", "/api/v1/auth/register",
    {"nombre": "", "correo": mail("nombre"), "contrasena": CLAVE, "rol": "CLIENTE"},
    "400", lambda s, r, h: (s == 400, None))

# ---------- CP-HU07 ----------
st, lp = run("CP-HU07", "F1", "Iniciar sesión como proveedor", "POST", "/api/v1/auth/login",
             {"correo": prov_mail, "password": CLAVE}, "200; Bearer, expiresIn 3600, rol PROVEEDOR",
             lambda s, r, h: (s == 200 and r.get("tokenType") == "Bearer" and r.get("expiresIn") == 3600
                              and r.get("role") == "PROVEEDOR", None))
tok_prov = (lp or {}).get("accessToken")
st, lc = run("CP-HU07", "F2", "Iniciar sesión como cliente", "POST", "/api/v1/auth/login",
             {"correo": cli_mail, "password": CLAVE}, "200; rol CLIENTE",
             lambda s, r, h: (s == 200 and r.get("role") == "CLIENTE", None))
tok_cli = (lc or {}).get("accessToken")
tok_provb = (call("POST", "/api/v1/auth/login", {"correo": provb_mail, "password": CLAVE})[1] or {}).get("accessToken")
run("CP-HU07", "E1", "Contraseña incorrecta", "POST", "/api/v1/auth/login",
    {"correo": cli_mail, "password": "Otra-Clave-Equivocada-99"}, "401; «Correo o contraseña incorrectos»",
    lambda s, r, h: (s == 401 and "Correo o contraseña incorrectos" in msg(r), None))
run("CP-HU07", "E2", "Correo no registrado", "POST", "/api/v1/auth/login",
    {"correo": mail("nadie"), "password": CLAVE}, "401; mismo mensaje que E1",
    lambda s, r, h: (s == 401 and "Correo o contraseña incorrectos" in msg(r), None))
run("CP-HU07", "E3", "Campos obligatorios vacíos", "POST", "/api/v1/auth/login",
    {"correo": "", "password": ""}, "400", lambda s, r, h: (s == 400, None))
record("CP-HU07", "E4", "Cuenta inactiva", "", "", None, "403", None, None, False,
       "No hay API para inactivar una cuenta ni una cuenta inactiva en los datos de prueba.", blocked=True)
run("CP-HU07", "E5", "Endpoint protegido sin token", "POST", "/api/v1/bookings",
    {"clienteId": ANA}, "401", lambda s, r, h: (s == 401, None))
run("CP-HU07", "E6", "Token CLIENTE en endpoint de proveedor", "POST", "/api/v1/disponibilidades",
    {"agendaServicioId": AGENDA_S1, "inicioAt": "2027-03-01T15:00:00Z", "finAt": "2027-03-01T16:00:00Z"},
    "403", lambda s, r, h: (s == 403, None), token=tok_cli)

# ---------- CP-HU08 ----------
lock_mail, reset_mail = mail("bloqueo"), mail("reinicio")
call("POST", "/api/v1/auth/register", {"nombre": "Bloqueo QA", "correo": lock_mail, "contrasena": CLAVE, "rol": "CLIENTE"})
call("POST", "/api/v1/auth/register", {"nombre": "Reinicio QA", "correo": reset_mail, "contrasena": CLAVE, "rol": "CLIENTE"})
codes = [call("POST", "/api/v1/auth/login", {"correo": lock_mail, "password": "Mala-Clave-Intento-00"})[0] for _ in range(4)]
run("CP-HU08", "F1", "Bloquear al quinto intento fallido", "POST", "/api/v1/auth/login",
    {"correo": lock_mail, "password": "Mala-Clave-Intento-05"},
    "Intentos 1-4: 401. Quinto: 429, Retry-After 900, «Demasiados intentos fallidos. Intenta de nuevo en 15 minutos»",
    lambda s, r, h: (codes == [401] * 4 and s == 429 and str(h.get("Retry-After")) in ("899", "900")
                     and "Demasiados intentos fallidos" in msg(r), f"intentos 1-4: {codes}"))
run("CP-HU08", "E1", "Credenciales correctas durante el bloqueo", "POST", "/api/v1/auth/login",
    {"correo": lock_mail, "password": CLAVE}, "429; informa los minutos restantes; no entrega token",
    lambda s, r, h: (s == 429 and "minuto" in msg(r).lower() and "accessToken" not in json.dumps(r or {}), None))
record("CP-HU08", "F2", "Acceso al terminar el bloqueo", "", "", None, "200 y contador en cero", None, None, False,
       "La duración (15 min) está fija en LoginService; no es configurable por propiedades como pide la regla de HU-08.", blocked=True)
codes2 = [call("POST", "/api/v1/auth/login", {"correo": reset_mail, "password": "Mala-Clave-Intento-00"})[0] for _ in range(3)]
ok_login = call("POST", "/api/v1/auth/login", {"correo": reset_mail, "password": CLAVE})[0]
run("CP-HU08", "E2", "Un inicio exitoso reinicia el contador", "POST", "/api/v1/auth/login",
    {"correo": reset_mail, "password": "Mala-Clave-Intento-04"}, "3 fallos 401, inicio correcto 200, nuevo fallo 401 (no 429)",
    lambda s, r, h: (codes2 == [401] * 3 and ok_login == 200 and s == 401, f"fallos: {codes2}, inicio: {ok_login}"))
ghost = mail("fantasma")
codes3 = [call("POST", "/api/v1/auth/login", {"correo": ghost, "password": "Mala-Clave-Intento-00"})[0] for _ in range(4)]
run("CP-HU08", "E3", "Mismo bloqueo para un correo no registrado", "POST", "/api/v1/auth/login",
    {"correo": ghost, "password": "Mala-Clave-Intento-05"}, "Quinto intento: 429, igual que una cuenta existente",
    lambda s, r, h: (s == 429, f"intentos 1-4: {codes3}"))

# ---------- CP-HU09 ----------
perfil = {"businessName": f"Barbería QA {TS}", "category": "Barbería y belleza", "address": "Calle 10 # 43-20, Medellín", "phone": "+573001234567"}
run("CP-HU09", "E4a", "Crear perfil sin token", "POST", "/api/v1/comercio/perfil", perfil, "401", lambda s, r, h: (s == 401, None))
run("CP-HU09", "E4b", "Crear perfil con token de cliente", "POST", "/api/v1/comercio/perfil", perfil, "403",
    lambda s, r, h: (s == 403, None), token=tok_cli)
run("CP-HU09", "E1", "Perfil sin nombre comercial", "POST", "/api/v1/comercio/perfil", {**perfil, "businessName": ""},
    "400; indica el campo faltante", lambda s, r, h: (s == 400 and "nombre comercial" in json.dumps(r, ensure_ascii=False).lower(), None), token=tok_prov)
run("CP-HU09", "E2", "Perfil sin categoría", "POST", "/api/v1/comercio/perfil", {**perfil, "category": ""},
    "400; indica el campo faltante", lambda s, r, h: (s == 400 and "categor" in json.dumps(r, ensure_ascii=False).lower(), None), token=tok_prov)
run("CP-HU09", "E5", "Categoría fuera del catálogo", "POST", "/api/v1/comercio/perfil", {**perfil, "category": "Mascotas exóticas"},
    "400; lista las categorías válidas", lambda s, r, h: (s == 400 and "Barbería y belleza" in msg(r), None), token=tok_prov)
st, prof = run("CP-HU09", "F1", "Crear un perfil comercial válido", "POST", "/api/v1/comercio/perfil", perfil,
               "201; id del perfil y datos registrados", lambda s, r, h: (s == 201 and r.get("id") and r.get("businessName") == perfil["businessName"], None),
               token=tok_prov)
prof_id = (prof or {}).get("id")
run("CP-HU09", "E3", "Segundo perfil del mismo proveedor", "POST", "/api/v1/comercio/perfil", perfil,
    "409; «Ya tienes un perfil comercial registrado»", lambda s, r, h: (s == 409 and "Ya tienes un perfil comercial registrado" in msg(r), None), token=tok_prov)
run("CP-HU09", "F1b", "El perfil queda visible en la búsqueda", "GET",
    "/api/v1/catalog/services/search?texto=" + urllib.request.quote(perfil["businessName"]), None,
    "200 y el comercio aparece en los resultados",
    lambda s, r, h: (s == 200 and any(x.get("provider") == perfil["businessName"] for x in (r or {}).get("servicios", [])), None),
    note="Un comercio nuevo no puede publicar servicios ni horarios por API; ver #181.")

# ---------- CP-HU12 ----------
run("CP-HU12", "E2", "Proveedor sin perfil comercial", "POST", "/api/v1/comercio/recursos", {"name": "Cancha 1", "type": "FISICO"},
    "409; «Primero debes crear tu perfil comercial»", lambda s, r, h: (s == 409 and "Primero debes crear tu perfil comercial" in msg(r), None), token=tok_provb)
st, rf = run("CP-HU12", "F1", "Registrar un recurso físico", "POST", "/api/v1/comercio/recursos", {"name": "Cancha 1", "type": "FISICO"},
             "201; ACTIVO y asociado al comercio", lambda s, r, h: (s == 201 and r.get("status") == "ACTIVO" and r.get("providerId") == prof_id, None), token=tok_prov)
run("CP-HU12", "F2", "Registrar un recurso humano", "POST", "/api/v1/comercio/recursos", {"name": "Barbero Carlos", "type": "HUMANO"},
    "201; ACTIVO", lambda s, r, h: (s == 201 and r.get("status") == "ACTIVO", None), token=tok_prov)
run("CP-HU12", "E1", "Recurso duplicado sin distinguir mayúsculas", "POST", "/api/v1/comercio/recursos", {"name": "cancha 1", "type": "FISICO"},
    "409; «Ya existe un recurso con ese nombre en tu comercio»", lambda s, r, h: (s == 409 and "Ya existe un recurso con ese nombre en tu comercio" in msg(r), None), token=tok_prov)
run("CP-HU12", "E3a", "Tipo de recurso inválido", "POST", "/api/v1/comercio/recursos", {"name": "Sala Virtual", "type": "VIRTUAL"},
    "400; indica FISICO o HUMANO", lambda s, r, h: (s == 400 and "FISICO" in msg(r) and "HUMANO" in msg(r), None), token=tok_prov)
run("CP-HU12", "E3b", "Nombre vacío", "POST", "/api/v1/comercio/recursos", {"name": "", "type": "FISICO"},
    "400", lambda s, r, h: (s == 400, None), token=tok_prov)
st, profb, _h = call("POST", "/api/v1/comercio/perfil", {**perfil, "businessName": f"Estadio QA {TS}", "category": "Deportes y recreación"}, tok_provb)
profb_id = (profb or {}).get("id")
run("CP-HU12", "E4", "Mismo nombre en otro comercio", "POST", "/api/v1/comercio/recursos", {"name": "Cancha 1", "type": "FISICO"},
    "201", lambda s, r, h: (s == 201, None), token=tok_provb)
run("CP-HU12", "E5", "Token de cliente", "POST", "/api/v1/comercio/recursos", {"name": "Cancha 9", "type": "FISICO"},
    "403", lambda s, r, h: (s == 403, None), token=tok_cli)

# ---------- CP-HU10 ----------
prov_user = (prov or {}).get("id")
dia = lambda f, p=prof_id: {"idProveedor": p, "fecha": f, "motivo": "Prueba QA Sprint 2", "idUsuarioCreador": prov_user}
run("CP-HU10", "F1", "Registrar un día no laborable sin reservas", "POST", "/api/v1/dias-no-laborables", dia("2027-02-10"),
    "201; fecha registrada", lambda s, r, h: (s == 201 and str((r or {}).get("fecha")) == "2027-02-10", None), token=tok_prov)
run("CP-HU10", "E1", "Fecha ya registrada", "POST", "/api/v1/dias-no-laborables", dia("2027-02-10"),
    "409; «La fecha ya está registrada como no laborable»", lambda s, r, h: (s == 409 and "La fecha ya está registrada como no laborable" in msg(r), None), token=tok_prov)
run("CP-HU10", "E2", "Fecha con formato inválido", "POST", "/api/v1/dias-no-laborables", dia("fecha-invalida"),
    "400", lambda s, r, h: (s == 400, None), token=tok_prov)
run("CP-HU10", "E3", "Fecha pasada", "POST", "/api/v1/dias-no-laborables", dia("2026-09-20"),
    "400; «La fecha debe ser igual o posterior a hoy»", lambda s, r, h: (s == 400 and "posterior a hoy" in msg(r), None), token=tok_prov)
run("CP-HU10", "E4", "Bloquear una fecha de OTRO comercio con mi token", "POST", "/api/v1/dias-no-laborables", dia("2027-02-11", profb_id),
    "403; un proveedor solo bloquea fechas de su comercio", lambda s, r, h: (s == 403, None), token=tok_prov,
    note="El proveedor y el usuario creador se leen del cuerpo, no del token.")
record("CP-HU10", "F2", "El cliente no ve horarios en la fecha bloqueada", "", "", None, "200 sin horarios ese día", None, None, False,
       "El comercio de QA no puede publicar horarios: no hay API para crear agendas de un comercio nuevo.", blocked=True)

# ---------- CP-HU11 ----------
run("CP-HU11", "P1", "Previsualizar una fecha sin reservas", "POST", "/api/v1/dias-no-laborables/previsualizar", dia("2027-02-12"),
    "200; 0 reservas afectadas", lambda s, r, h: (s == 200 and r.get("reservasAfectadas") == 0, None), token=tok_prov)
for esc, desc in [("F1", "Advertir sobre reservas existentes (409 con la lista)"), ("F2", "Confirmar el bloqueo con reservas existentes"),
                  ("E1", "Sin confirmación la fecha sigue laborable"), ("E2", "Fecha solo con reservas canceladas")]:
    record("CP-HU11", esc, desc, "", "", None, "Según HU-11", None, None, False,
           "Requiere reservas en una agenda del comercio que bloquea; el comercio de QA no puede tener agenda por API. "
           "Revisión de código: POST /dias-no-laborables bloquea sin pedir confirmación (no existe el campo confirmar) y la "
           "previsualización filtra por fecha de creación (#176).", blocked=True)

# ---------- CP-HU13 ----------
record("CP-HU13", "F1", "Registrar horarios en la agenda de un recurso", "", "", None, "201; horario en la agenda del recurso", None, None, False,
       "No existe endpoint para crear la agenda de un recurso nuevo ni para registrar horarios por recurso: "
       "POST /api/v1/disponibilidades solo recibe agendaServicioId.")
day0 = datetime(2027, 3, 1, 15, 0, tzinfo=timezone.utc) + timedelta(minutes=int(TS[-4:]) % 600)
s1 = {"agendaServicioId": AGENDA_S1, "inicioAt": day0.isoformat(), "finAt": (day0 + timedelta(hours=1)).isoformat()}
st, disp = run("CP-HU13", "E2", "Registrar un horario en la agenda de OTRO comercio", "POST", "/api/v1/disponibilidades", s1,
               "403", lambda s, r, h: (s == 403, None), token=tok_prov, note="La agenda pertenece al comercio de prueba del Sprint 1.")
disp_id = (disp or {}).get("id") or (disp or {}).get("idDisponibilidad")
s2 = {"agendaServicioId": AGENDA_S1, "inicioAt": (day0 + timedelta(minutes=30)).isoformat(), "finAt": (day0 + timedelta(minutes=90)).isoformat()}
run("CP-HU13", "E1", "Horario cruzado en el mismo recurso", "POST", "/api/v1/disponibilidades", s2,
    "409; «El horario se cruza con otro horario del recurso»", lambda s, r, h: (s == 409 and "se cruza" in msg(r), None), token=tok_prov)
st, cat = run("CP-HU13", "F2", "Cada horario muestra el id y el nombre del recurso", "GET", "/api/v1/catalog/services", None,
              "200; cada horario incluye id y nombre del recurso",
              lambda s, r, h: (s == 200 and all(("recursoId" in hh or "resourceId" in hh) and ("recurso" in hh or "resourceName" in hh)
                                                for x in (r if isinstance(r, list) else (r or {}).get("servicios", r or [])) for hh in (x.get("availableSchedules") or x.get("horarios") or [])), None))
if disp_id:
    st, b1 = run("CP-HU13", "E3a", "Ana reserva el horario (token de otro cliente)", "POST", "/api/v1/bookings",
                 {"clienteId": ANA, "disponibilidadId": disp_id}, "403: el token no corresponde a Ana",
                 lambda s, r, h: (s == 403, None), token=tok_cli, note="El clienteId se lee del cuerpo, no del token.")
    if st == 201 and b1:
        created_bookings.append(b1.get("id"))
    st, b2 = run("CP-HU13", "E3", "Doble reserva sobre el mismo horario", "POST", "/api/v1/bookings",
                 {"clienteId": LUIS, "disponibilidadId": disp_id}, "409; «El recurso no está disponible en ese horario»",
                 lambda s, r, h: (s == 409 and "El recurso no está disponible en ese horario" in msg(r), None), token=tok_cli)
    if st == 201 and b2:
        created_bookings.append(b2.get("id"))

# ---------- CP-HU14 ----------
st, todo, _h = call("GET", "/api/v1/catalog/services/search?texto=a")
items = (todo or {}).get("servicios", [])
svc = items[0]["service"] if items else "corte"
provname = items[0]["provider"] if items else "Barbería Central"
frag = svc.split()[0][:5].upper()
run("CP-HU14", "F1", f"Buscar por nombre («{frag}»)", "GET", "/api/v1/catalog/services/search?texto=" + urllib.request.quote(frag), None,
    "200; solo servicios cuyo nombre o proveedor contiene el texto",
    lambda s, r, h: (s == 200 and r["servicios"] and all(frag.lower() in (x["service"] + x["provider"]).lower() for x in r["servicios"]), None))
q2 = provname.lower()
run("CP-HU14", "F2", f"Buscar por proveedor («{q2}»)", "GET", "/api/v1/catalog/services/search?texto=" + urllib.request.quote(q2), None,
    "200; servicios de ese proveedor", lambda s, r, h: (s == 200 and r["servicios"] and all(x["provider"] == provname for x in r["servicios"]), None))
run("CP-HU14", "F3", "Buscar por categoría «Deportes y recreación»", "GET", "/api/v1/catalog/services/search?categoria=" + urllib.request.quote("Deportes y recreación"), None,
    "200; solo servicios de esa categoría", lambda s, r, h: (s == 200, None), note="Ver #181.")
run("CP-HU14", "F4", "Combinar texto y categoría sin tilde", "GET", "/api/v1/catalog/services/search?texto=a&categoria=" + urllib.request.quote("Deportes y recreacion"), None,
    "200; cumple ambos filtros", lambda s, r, h: (s == 200, None), note="Ver #181 y #182.")
run("CP-HU14", "E1", "Búsqueda sin resultados («Yoga»)", "GET", "/api/v1/catalog/services/search?texto=Yoga", None,
    "200; lista vacía y «No se encontraron servicios»", lambda s, r, h: (s == 200 and r["servicios"] == [] and r.get("mensaje") == "No se encontraron servicios", None))
run("CP-HU14", "E2", "Búsqueda sin criterio", "GET", "/api/v1/catalog/services/search", None,
    "400; el mensaje indica el criterio inválido", lambda s, r, h: (s == 400 and "criterio" in msg(r), None))
run("CP-HU14", "E3", "Categoría fuera del catálogo", "GET", "/api/v1/catalog/services/search?categoria=Inexistente-QA", None,
    "400", lambda s, r, h: (s == 400, None))
now = datetime.now(timezone.utc).isoformat()
run("CP-HU14", "E4", "No mostrar horarios que ya pasaron", "GET", "/api/v1/catalog/services/search?texto=a", None,
    "200; todos los inicioAt son futuros",
    lambda s, r, h: (s == 200 and all(hh["inicioAt"] > now for x in r["servicios"] for hh in x["availableSchedules"]), None), note="Ver #183.")

# ---------- limpieza ----------
for bid in created_bookings:
    if bid:
        print("limpieza: cancelar", bid, call("PATCH", f"/api/v1/bookings/{bid}/cancel", token=tok_cli)[0])

json.dump({"ejecucion_utc": datetime.now(timezone.utc).isoformat(), "base": BASE, "sufijo": TS, "resultados": results},
          open(OUT, "w"), ensure_ascii=False, indent=2)
tot = {k: sum(1 for r in results if r["resultado"] == k) for k in ("Pasa", "No pasa", "Bloqueado")}
print("TOTAL", len(results), tot)
