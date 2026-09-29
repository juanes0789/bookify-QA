"""Genera la colección Postman de los casos de prueba CP-HU01..CP-HU05 de Bookify."""
import json, pathlib, uuid

OUT = pathlib.Path(__file__).resolve().parent.parent
OUT.mkdir(exist_ok=True)


def js(code):
    return [l for l in code.strip("\n").split("\n")]


def slot(var, past=False):
    """Pre-request: genera un horario aleatorio de 30 min para no chocar con ejecuciones anteriores."""
    sign = "-" if past else "+"
    return f"""
// Horario aleatorio de 1 hora ({'en el pasado' if past else 'en el futuro'}) para no chocar con ejecuciones anteriores
const minutos = 60 * 24 * (2 + Math.floor(Math.random() * 720)) + 5 * Math.floor(Math.random() * 288);
const inicio = new Date(Date.now() {sign} minutos * 60000);
inicio.setUTCMinutes(Math.floor(inicio.getUTCMinutes() / 5) * 5, 0, 0);
const fin = new Date(inicio.getTime() + 60 * 60000);
pm.collectionVariables.set("{var}_inicio", inicio.toISOString());
pm.collectionVariables.set("{var}_fin", fin.toISOString());
"""


PARSE = """
let body = {};
try { body = pm.response.json(); } catch (e) { body = {}; }
"""

CATALOG = PARSE + """
const servicios = Array.isArray(body) ? body : [];
const horarios = servicios.flatMap(s => (s.availableSchedules || []).map(h => Object.assign({}, h, { service: s.service, provider: s.provider })));
const buscar = id => horarios.find(h => h.availabilityId === pm.collectionVariables.get(id));
"""


def status(code, text):
    return f'pm.test("Responde {code} {text}", () => pm.response.to.have.status({code}));\n'


def error_includes(fragment):
    return f'pm.test("El mensaje de error menciona \\"{fragment}\\"", () => pm.expect(body.error || "").to.include("{fragment}"));\n'


def req(name, method, path, description, body=None, pre=None, test=None, headers=None):
    parts = path.strip("/").split("/")
    hdr = [{"key": "Content-Type", "value": "application/json"}] if body is not None else []
    hdr += [{"key": k, "value": v} for k, v in (headers or {}).items()]
    r = {
        "method": method,
        "header": hdr,
        "url": {"raw": "{{baseUrl}}/" + "/".join(parts), "host": ["{{baseUrl}}"], "path": parts},
        "description": description.strip(),
    }
    if body is not None:
        r["body"] = {"mode": "raw", "raw": json.dumps(body, indent=2, ensure_ascii=False),
                     "options": {"raw": {"language": "json"}}}
    ev = []
    if pre:
        ev.append({"listen": "prerequest", "script": {"type": "text/javascript", "exec": js(pre)}})
    if test:
        ev.append({"listen": "test", "script": {"type": "text/javascript", "exec": js(test)}})
    return {"name": name, "event": ev, "request": r}


def create_availability(name, var, desc, past=False, save_as=None):
    """Petición de preparación: crea una disponibilidad y guarda su id."""
    return req(
        name, "POST", "api/v1/disponibilidades", desc,
        body={"agendaServicioId": "{{agendaServicioId}}", "inicioAt": "{{%s_inicio}}" % var, "finAt": "{{%s_fin}}" % var},
        pre=slot(var, past),
        test=PARSE + status(201, "Created") + f'pm.collectionVariables.set("{save_as or var + "_disponibilidadId"}", body.id);\n',
    )


def create_booking(name, cliente, disp_var, desc, save_as=None):
    return req(
        name, "POST", "api/v1/bookings", desc,
        body={"clienteId": "{{%s}}" % cliente, "disponibilidadId": "{{%s}}" % disp_var, "notas": "Prueba Postman"},
        test=PARSE + status(201, "Created") + (f'pm.collectionVariables.set("{save_as}", body.id);\n' if save_as else ""),
    )


HISTORY = PARSE + """
const reservas = body.reservas || [];
// fecha y hora del historial vienen en UTC a partir del inicio del horario
const clave = v => { const iso = pm.collectionVariables.get(v + "_inicio"); return iso ? iso.slice(0, 10) + " " + iso.slice(11, 16) : ""; };
const reservaDe = v => reservas.find(r => r.fecha + " " + String(r.hora).slice(0, 5) === clave(v));
"""


def history(name, cliente, desc, test):
    return req(name, "GET", "api/v1/reservas/historial", desc,
               headers={"X-Cliente-Id": "{{%s}}" % cliente}, test=HISTORY + status(200, "OK") + test)


def estado_en_historial(name, cliente, var, estado, desc):
    return history(name, cliente, desc, f"""
pm.test("La reserva aparece en el historial", () => pm.expect(reservaDe("{var}"), "no se encontró la reserva").to.exist);
pm.test("La reserva está en estado {estado}", () => pm.expect(String((reservaDe("{var}") || {{}}).estado).toUpperCase()).to.include("{estado[:-1]}"));
""")


def catalog(name, desc, test):
    return req(name, "GET", "api/v1/catalog/services", desc, test=CATALOG + status(200, "OK") + test)


SKIP_IF_NO_INACTIVE = """
// Opcional: solo se ejecuta si el entorno tiene una agenda inactiva configurada
if (!pm.environment.get("agendaInactivaId")) { pm.execution.skipRequest(); }
"""

# ---------------------------------------------------------------- CP-HU01
hu01 = {
    "name": "CP-HU01 – Registrar disponibilidad",
    "description": """HU-01. Verifica que se registre un horario disponible para una agenda activa y que se rechacen los horarios cruzados, los rangos inválidos y las agendas inexistentes o inactivas.

**No verificable por API:** la fecha de actualización (la respuesta no la retorna; revisar en BD) y la ausencia del estado DISPONIBLE en la base de datos (requiere alterar datos maestros).""",
    "item": [
        req("CP-HU01-F1 · Registrar una disponibilidad válida", "POST", "api/v1/disponibilidades",
            """
**Camino feliz.** Registra un horario de 1 hora para una agenda de servicio activa.

**Resultado esperado:** 201 Created; la disponibilidad queda registrada, asociada a la agenda y con fecha de creación.
""",
            body={"agendaServicioId": "{{agendaServicioId}}", "inicioAt": "{{hu01_inicio}}", "finAt": "{{hu01_fin}}"},
            pre=slot("hu01"),
            test=PARSE + status(201, "Created") + """
pm.test("Retorna el id de la disponibilidad", () => pm.expect(body.id).to.be.a("string").and.not.empty);
pm.test("Queda asociada a la agenda enviada", () => pm.expect(body.agendaServicioId).to.eql(pm.variables.get("agendaServicioId")));
pm.test("Conserva el rango de fecha y hora enviado", () => {
    pm.expect(new Date(body.inicioAt).getTime()).to.eql(new Date(pm.collectionVariables.get("hu01_inicio")).getTime());
    pm.expect(new Date(body.finAt).getTime()).to.eql(new Date(pm.collectionVariables.get("hu01_fin")).getTime());
});
pm.test("Registra la fecha de creación", () => pm.expect(body.fechaCreacion).to.be.a("string").and.not.empty);
pm.collectionVariables.set("hu01_disponibilidadId", body.id);
"""),
        catalog("CP-HU01-F1 · Verificar que el horario queda DISPONIBLE y reservable",
                """
**Camino feliz (verificación).** El catálogo solo lista horarios en estado DISPONIBLE.

**Resultado esperado:** el horario registrado aparece en el catálogo.
""", """
pm.test("El horario registrado aparece como reservable", () => pm.expect(buscar("hu01_disponibilidadId"), "no se encontró el horario en el catálogo").to.exist);
"""),
        req("CP-HU01-E1 · Rechazar un horario cruzado", "POST", "api/v1/disponibilidades",
            """
**Camino de excepción del documento.** Registra en la misma agenda un horario que empieza 30 minutos después del creado en F1 (equivale a 10:00–11:00 vs 10:30–11:30).

**Resultado esperado:** 409 Conflict; el mensaje informa que el rango se traslapa; no se crea la disponibilidad.
""",
            body={"agendaServicioId": "{{agendaServicioId}}", "inicioAt": "{{hu01_cruce_inicio}}", "finAt": "{{hu01_cruce_fin}}"},
            pre="""
// Rango que se cruza con el creado en F1, como en el documento: 10:00-11:00 vs 10:30-11:30
const inicio = new Date(new Date(pm.collectionVariables.get("hu01_inicio")).getTime() + 30 * 60000);
const fin = new Date(inicio.getTime() + 60 * 60000);
pm.collectionVariables.set("hu01_cruce_inicio", inicio.toISOString());
pm.collectionVariables.set("hu01_cruce_fin", fin.toISOString());
""",
            test=PARSE + status(409, "Conflict") + error_includes("traslapa") + """
pm.test("No retorna una disponibilidad creada", () => pm.expect(body.id).to.be.undefined);
"""),
        req("CP-HU01-E2 · Rechazar un rango inválido (inicio ≥ fin)", "POST", "api/v1/disponibilidades",
            """
**Criterio de rechazo:** la fecha/hora inicial es igual o posterior a la final.

**Resultado esperado:** 400 Bad Request y no se crea la disponibilidad.
""",
            body={"agendaServicioId": "{{agendaServicioId}}", "inicioAt": "{{hu01_invalido}}", "finAt": "{{hu01_invalido}}"},
            pre="""
const t = new Date(Date.now() + 90 * 86400000); t.setUTCSeconds(0, 0);
pm.collectionVariables.set("hu01_invalido", t.toISOString());
""",
            test=PARSE + status(400, "Bad Request") + error_includes("anterior")),
        req("CP-HU01-E3 · Rechazar una agenda inexistente", "POST", "api/v1/disponibilidades",
            """
**Criterio de rechazo:** la agenda de servicio no existe.

**Resultado esperado:** 400 Bad Request; el mensaje indica que la agenda no existe o no está activa.
""",
            body={"agendaServicioId": "{{$guid}}", "inicioAt": "{{hu01_inicio}}", "finAt": "{{hu01_fin}}"},
            test=PARSE + status(400, "Bad Request") + error_includes("agenda")),
        req("CP-HU01-E4 · Rechazar una agenda inactiva (opcional)", "POST", "api/v1/disponibilidades",
            """
**Criterio de rechazo:** la agenda de servicio no está activa. Se omite si el entorno no tiene `agendaInactivaId`.

**Resultado esperado:** 400 Bad Request; el mensaje indica que la agenda no existe o no está activa.
""",
            body={"agendaServicioId": "{{agendaInactivaId}}", "inicioAt": "{{hu01_inicio}}", "finAt": "{{hu01_fin}}"},
            pre=SKIP_IF_NO_INACTIVE,
            test=PARSE + status(400, "Bad Request") + error_includes("agenda")),
    ],
}

# ---------------------------------------------------------------- CP-HU02
hu02 = {
    "name": "CP-HU02 – Consultar servicios y horarios disponibles",
    "description": "HU-02. Verifica que el catálogo muestre servicio, proveedor y horarios disponibles, asociados al servicio correcto, y que oculte los horarios reservados y los servicios sin horarios.",
    "item": [
        create_availability("CP-HU02-P1 · Preparar: crear un horario libre", "hu02_libre",
                            "Crea un horario que debe aparecer en el catálogo."),
        create_availability("CP-HU02-P2 · Preparar: crear un horario que se va a reservar", "hu02_ocupada",
                            "Crea un horario que se reservará en P3."),
        create_booking("CP-HU02-P3 · Preparar: reservar el horario P2", "clienteAnaId", "hu02_ocupada_disponibilidadId",
                       "Reserva el horario P2 para que deje de estar disponible.", save_as="hu02_reservaId"),
        catalog("CP-HU02-F1 · Consultar el catálogo de servicios",
                """
**Camino feliz.**

**Resultado esperado:** 200 OK; cada servicio muestra nombre, proveedor y horarios disponibles; el horario libre (P1) aparece asociado a un único servicio y proveedor.
""", """
pm.test("Retorna al menos un servicio", () => pm.expect(servicios).to.be.an("array").that.is.not.empty);
pm.test("Cada servicio tiene nombre, proveedor y horarios", () => servicios.forEach(s => {
    pm.expect(s.service, "nombre del servicio").to.be.a("string").and.not.empty;
    pm.expect(s.provider, "proveedor").to.be.a("string").and.not.empty;
    pm.expect(s.availableSchedules, "horarios").to.be.an("array");
}));
pm.test("El horario libre aparece en el catálogo", () => pm.expect(buscar("hu02_libre_disponibilidadId"), "no se encontró el horario libre").to.exist);
pm.test("Cada horario está asociado a un único servicio", () => {
    const ids = horarios.map(h => h.availabilityId);
    pm.expect(new Set(ids).size, "hay horarios repetidos en varios servicios").to.eql(ids.length);
});
pm.test("Cada horario tiene inicio anterior al fin", () => horarios.forEach(h => pm.expect(new Date(h.inicioAt) < new Date(h.finAt), h.availabilityId).to.be.true));
"""),
        catalog("CP-HU02-E1 · Un horario reservado no aparece como disponible",
                """
**Camino de excepción.**

**Resultado esperado:** el horario reservado en P3 no aparece y ningún servicio se muestra sin horarios disponibles.
""", """
pm.test("El horario reservado no aparece como disponible", () => pm.expect(buscar("hu02_ocupada_disponibilidadId")).to.be.undefined);
pm.test("Ningún servicio aparece sin horarios disponibles", () => servicios.forEach(s => pm.expect(s.availableSchedules, s.service).to.not.be.empty));
"""),
    ],
}

# ---------------------------------------------------------------- CP-HU03
hu03 = {
    "name": "CP-HU03 – Crear una reserva",
    "description": "HU-03. Verifica que un cliente reserve un horario libre (reserva CONFIRMADA y horario OCUPADO) y que se impidan la doble reserva y las solicitudes incompletas o sobre horarios inexistentes.",
    "item": [
        create_availability("CP-HU03-P1 · Preparar: crear un horario libre", "hu03",
                            "Crea el horario que se va a reservar."),
        req("CP-HU03-F1 · Ana reserva el horario", "POST", "api/v1/bookings",
            """
**Camino feliz.**

**Resultado esperado:** 201 Created; la reserva pertenece a Ana y al horario elegido, y registra la fecha de reserva.
""",
            body={"clienteId": "{{clienteAnaId}}", "disponibilidadId": "{{hu03_disponibilidadId}}", "notas": "Prueba Postman"},
            test=PARSE + status(201, "Created") + """
pm.test("Retorna el id de la reserva", () => pm.expect(body.id).to.be.a("string").and.not.empty);
pm.test("La reserva pertenece a Ana y al horario elegido", () => {
    pm.expect(body.clienteId).to.eql(pm.variables.get("clienteAnaId"));
    pm.expect(body.disponibilidadId).to.eql(pm.collectionVariables.get("hu03_disponibilidadId"));
});
pm.test("Registra la fecha de la reserva", () => pm.expect(body.fechaReserva).to.be.a("string").and.not.empty);
pm.collectionVariables.set("hu03_reservaId", body.id);
pm.collectionVariables.set("estadoConfirmadaId", body.estadoReservaId);
"""),
        estado_en_historial("CP-HU03-F1 · Verificar que la reserva queda CONFIRMADA", "clienteAnaId", "hu03", "CONFIRMADA",
                            "**Camino feliz (verificación).** Consulta el historial de Ana para leer el estado de la reserva por su nombre."),
        catalog("CP-HU03-F1 · Verificar que el horario queda OCUPADO",
                "**Camino feliz (verificación).**\n\n**Resultado esperado:** el horario ya no aparece en el catálogo.", """
pm.test("El horario ya no aparece como disponible", () => pm.expect(buscar("hu03_disponibilidadId")).to.be.undefined);
"""),
        req("CP-HU03-E1 · Luis intenta reservar el mismo horario", "POST", "api/v1/bookings",
            """
**Camino de excepción del documento (doble reserva).**

**Resultado esperado:** 409 Conflict; se informa que la disponibilidad ya fue reservada.
""",
            body={"clienteId": "{{clienteLuisId}}", "disponibilidadId": "{{hu03_disponibilidadId}}", "notas": "Prueba Postman"},
            test=PARSE + status(409, "Conflict") + error_includes("ya fue reservada")),
        history("CP-HU03-E1 · Verificar que no se creó una segunda reserva", "clienteLuisId",
                "**Camino de excepción (verificación).**\n\n**Resultado esperado:** Luis no tiene ninguna reserva sobre ese horario.", """
pm.test("Luis no tiene reserva sobre el horario", () => pm.expect(reservaDe("hu03")).to.be.undefined);
"""),
        catalog("CP-HU03-E1 · Verificar que el horario continúa ocupado",
                "**Camino de excepción (verificación).**\n\n**Resultado esperado:** el horario sigue sin aparecer en el catálogo.", """
pm.test("El horario continúa ocupado", () => pm.expect(buscar("hu03_disponibilidadId")).to.be.undefined);
"""),
        req("CP-HU03-E2 · Rechazar una reserva sin cliente", "POST", "api/v1/bookings",
            "**Criterio de rechazo:** no se proporciona el cliente.\n\n**Resultado esperado:** 400 Bad Request.",
            body={"disponibilidadId": "{{hu03_disponibilidadId}}"},
            test=status(400, "Bad Request")),
        req("CP-HU03-E3 · Rechazar una reserva sin disponibilidad", "POST", "api/v1/bookings",
            "**Criterio de rechazo:** no se proporciona la disponibilidad.\n\n**Resultado esperado:** 400 Bad Request.",
            body={"clienteId": "{{clienteAnaId}}"},
            test=status(400, "Bad Request")),
        req("CP-HU03-E4 · Rechazar un horario inexistente", "POST", "api/v1/bookings",
            """
**Criterio de rechazo:** la disponibilidad no existe.

**Resultado esperado:** 404 Not Found; el mensaje indica que la disponibilidad no existe y no se crea la reserva.
""",
            body={"clienteId": "{{clienteAnaId}}", "disponibilidadId": "{{$guid}}"},
            test=PARSE + status(404, "Not Found") + error_includes("no existe")),
        create_availability("CP-HU03-P2 · Preparar: crear un horario libre para E5", "hu03_e5",
                            "Crea un horario libre para intentar reservarlo con un cliente que no existe."),
        req("CP-HU03-E5 · Rechazar una reserva de un cliente inexistente", "POST", "api/v1/bookings",
            """
**Criterio de rechazo:** el cliente no existe.

**Resultado esperado:** 404 Not Found; el mensaje indica que el cliente no existe y no se crea la reserva.
""",
            body={"clienteId": "{{$guid}}", "disponibilidadId": "{{hu03_e5_disponibilidadId}}", "notas": "Prueba Postman"},
            test=PARSE + status(404, "Not Found") + """
pm.test("No responde con un error interno (500)", () => pm.expect(pm.response.code).to.be.below(500));
"""),
    ],
}

# ---------------------------------------------------------------- CP-HU04
hu04 = {
    "name": "CP-HU04 – Cancelar una reserva",
    "description": """HU-04. Verifica que una reserva futura se cancele y libere el horario, y que se rechace cancelar reservas pasadas, ya canceladas o inexistentes.

**No verificable por API:** el registro en el historial de estados (revisar `tbl_historial_estado_reserva`) y los casos de reserva sin disponibilidad asociada o con disponibilidad inexistente (requieren datos inconsistentes en BD).""",
    "item": [
        create_availability("CP-HU04-P1 · Preparar: crear un horario futuro", "hu04",
                            "Crea un horario futuro que se reservará y luego se cancelará."),
        create_booking("CP-HU04-P2 · Preparar: Ana reserva el horario futuro", "clienteAnaId", "hu04_disponibilidadId",
                       "Crea la reserva confirmada que se va a cancelar.", save_as="hu04_reservaId"),
        req("CP-HU04-F1 · Cancelar una reserva futura", "PATCH", "api/v1/bookings/{{hu04_reservaId}}/cancel",
            "**Camino feliz.**\n\n**Resultado esperado:** 200 OK y la reserva cambia de estado.",
            test=PARSE + status(200, "OK") + """
pm.test("Retorna la reserva cancelada", () => pm.expect(body.id).to.eql(pm.collectionVariables.get("hu04_reservaId")));
pm.test("El estado cambió respecto al de la reserva confirmada", () => {
    const confirmada = pm.collectionVariables.get("estadoConfirmadaId");
    if (confirmada) pm.expect(String(body.estadoReservaId)).to.not.eql(String(confirmada));
    else pm.expect(body.estadoReservaId).to.be.a("number");
});
"""),
        catalog("CP-HU04-F1 · Verificar que el horario vuelve a estar DISPONIBLE",
                "**Camino feliz (verificación).**\n\n**Resultado esperado:** el horario liberado aparece de nuevo en el catálogo.", """
pm.test("El horario liberado aparece de nuevo como disponible", () => pm.expect(buscar("hu04_disponibilidadId"), "el horario no se liberó").to.exist);
"""),
        req("CP-HU04-F1 · Verificar que el horario liberado se puede reservar de nuevo", "POST", "api/v1/bookings",
            "**Camino feliz (verificación).** Luis reserva el horario que Ana canceló.\n\n**Resultado esperado:** 201 Created.",
            body={"clienteId": "{{clienteLuisId}}", "disponibilidadId": "{{hu04_disponibilidadId}}", "notas": "Prueba Postman"},
            test=PARSE + status(201, "Created") + 'pm.collectionVariables.set("hu04_luis_reservaId", body.id);\n'),
        create_availability("CP-HU04-P3 · Preparar: crear un horario en el pasado", "hu04_pasado",
                            "Crea un horario que ya pasó (la API lo permite) para probar la regla de cancelación.",
                            past=True),
        create_booking("CP-HU04-P4 · Preparar: Ana reserva el horario pasado", "clienteAnaId", "hu04_pasado_disponibilidadId",
                       "Crea una reserva confirmada sobre el horario pasado.", save_as="hu04_pasado_reservaId"),
        req("CP-HU04-E1 · Rechazar la cancelación de una reserva pasada", "PATCH",
            "api/v1/bookings/{{hu04_pasado_reservaId}}/cancel",
            """
**Camino de excepción del documento.**

**Resultado esperado:** 400 Bad Request con el mensaje "No se puede cancelar una reserva cuyo horario ya inició."
""",
            test=PARSE + status(400, "Bad Request") + error_includes("ya inició")),
        estado_en_historial("CP-HU04-E1 · Verificar que la reserva conserva su estado", "clienteAnaId", "hu04_pasado", "CONFIRMADA",
                            "**Camino de excepción (verificación).**\n\n**Resultado esperado:** la reserva sigue CONFIRMADA."),
        catalog("CP-HU04-E1 · Verificar que el horario no se liberó",
                "**Camino de excepción (verificación).**\n\n**Resultado esperado:** el horario sigue sin aparecer en el catálogo.", """
pm.test("El horario no fue liberado", () => pm.expect(buscar("hu04_pasado_disponibilidadId")).to.be.undefined);
"""),
        req("CP-HU04-E2 · Rechazar la cancelación de una reserva ya cancelada", "PATCH", "api/v1/bookings/{{hu04_reservaId}}/cancel",
            "**Criterio de rechazo:** la reserva ya se encuentra cancelada.\n\n**Resultado esperado:** 400 Bad Request con el mensaje \"La reserva ya está cancelada.\"",
            test=PARSE + status(400, "Bad Request") + error_includes("ya está cancelada")),
        req("CP-HU04-E3 · Rechazar la cancelación de una reserva inexistente", "PATCH", "api/v1/bookings/{{$guid}}/cancel",
            "**Criterio de rechazo:** la reserva no existe.\n\n**Resultado esperado:** 404 Not Found.",
            test=PARSE + status(404, "Not Found") + error_includes("No existe")),
    ],
}

# ---------------------------------------------------------------- CP-HU05
hu05 = {
    "name": "CP-HU05 – Consultar historial de reservas",
    "description": "HU-05. Verifica que el cliente vea solo sus reservas, ordenadas de la más reciente a la más antigua y con toda su información, y que un historial vacío muestre el mensaje correspondiente.",
    "item": [
        create_availability("CP-HU05-P1 · Preparar: crear horario 1 para Ana", "hu05_ana1", "Primer horario de Ana."),
        create_booking("CP-HU05-P2 · Preparar: Ana reserva el horario 1", "clienteAnaId", "hu05_ana1_disponibilidadId", "Reserva 1 de Ana.", save_as="hu05_ana1_reservaId"),
        create_availability("CP-HU05-P3 · Preparar: crear horario 2 para Ana", "hu05_ana2", "Segundo horario de Ana (otra fecha)."),
        create_booking("CP-HU05-P4 · Preparar: Ana reserva el horario 2", "clienteAnaId", "hu05_ana2_disponibilidadId", "Reserva 2 de Ana.", save_as="hu05_ana2_reservaId"),
        create_availability("CP-HU05-P5 · Preparar: crear horario para Luis", "hu05_luis", "Horario que reservará otro cliente."),
        create_booking("CP-HU05-P6 · Preparar: Luis reserva su horario", "clienteLuisId", "hu05_luis_disponibilidadId", "Reserva de Luis.", save_as="hu05_luis_reservaId"),
        history("CP-HU05-F1 · Ana consulta su historial", "clienteAnaId",
                """
**Camino feliz.** El cliente se identifica con el encabezado `X-Cliente-Id`.

**Resultado esperado:** 200 OK; solo reservas de Ana, ordenadas de la más reciente a la más antigua, cada una con servicio, proveedor, fecha, hora y estado.
""", """
pm.test("Retorna reservas", () => pm.expect(reservas).to.be.an("array").that.is.not.empty);
pm.test("Cada reserva muestra servicio, proveedor, fecha, hora y estado", () => reservas.forEach(r => {
    ["servicio", "proveedor", "fecha", "hora", "estado"].forEach(k => pm.expect(r[k], k).to.exist.and.not.be.empty);
}));
pm.test("Incluye las dos reservas creadas para Ana", () => { pm.expect(reservaDe("hu05_ana1"), "reserva 1").to.exist; pm.expect(reservaDe("hu05_ana2"), "reserva 2").to.exist; });
pm.test("No incluye la reserva de Luis", () => pm.expect(reservaDe("hu05_luis")).to.be.undefined);
pm.test("Está ordenado de la más reciente a la más antigua", () => {
    const fechas = reservas.map(r => r.fecha + " " + r.hora);
    pm.expect(fechas).to.eql([...fechas].sort().reverse());
});
pm.test("No muestra mensaje de historial vacío", () => pm.expect(body.message).to.not.exist);
"""),
        create_availability("CP-HU05-P7 · Preparar: crear horario 3 para Ana", "hu05_ana3", "Horario de una reserva que Ana cancelará."),
        create_booking("CP-HU05-P8 · Preparar: Ana reserva el horario 3", "clienteAnaId", "hu05_ana3_disponibilidadId",
                       "Reserva 3 de Ana.", save_as="hu05_ana3_reservaId"),
        req("CP-HU05-P9 · Preparar: Ana cancela la reserva 3", "PATCH", "api/v1/bookings/{{hu05_ana3_reservaId}}/cancel",
            "Cancela la reserva 3 para verificar que el historial incluye reservas canceladas.",
            test=status(200, "OK")),
        estado_en_historial("CP-HU05-F2 · El historial incluye las reservas canceladas", "clienteAnaId", "hu05_ana3", "CANCELADA",
                            """
**Camino feliz adicional.** La HU-05 indica que el cliente hace seguimiento a sus citas "realizadas, canceladas o pendientes"; este escenario no está en el documento CP-HU05.

**Resultado esperado:** la reserva cancelada aparece en el historial con estado CANCELADA.
"""),
        req("CP-HU05-E1 · Cliente sin reservas", "GET", "api/v1/reservas/historial",
            """
**Camino de excepción del documento.** Usa un cliente nuevo (id aleatorio) que no tiene reservas.

**Resultado esperado:** 200 OK, lista vacía y el mensaje "No tienes reservas registradas".
""",
            headers={"X-Cliente-Id": "{{$guid}}"},
            test=PARSE + status(200, "OK") + """
pm.test("La lista de reservas está vacía", () => pm.expect(body.reservas).to.be.an("array").that.is.empty);
pm.test("Muestra el mensaje \\"No tienes reservas registradas\\"", () => pm.expect(body.message).to.eql("No tienes reservas registradas"));
"""),
        req("CP-HU05-E2 · Consultar sin identificar al cliente", "GET", "api/v1/reservas/historial",
            "**Criterio de rechazo:** no se proporciona el identificador del cliente.\n\n**Resultado esperado:** 400 Bad Request.",
            test=status(400, "Bad Request")),
    ],
}

# ---------------------------------------------------------------- Limpieza
LIMPIAR = [("hu02_reservaId", "CP-HU02"), ("hu03_reservaId", "CP-HU03"), ("hu04_luis_reservaId", "CP-HU04"),
           ("hu05_ana1_reservaId", "CP-HU05, reserva 1 de Ana"), ("hu05_ana2_reservaId", "CP-HU05, reserva 2 de Ana"),
           ("hu05_luis_reservaId", "CP-HU05, reserva de Luis")]
limpieza = {
    "name": "Limpieza – cancelar las reservas futuras de la corrida",
    "description": ("Cancela las reservas futuras que crearon los pasos de preparación, para que el catálogo quede con los horarios libres. "
                    "Se omite cada petición si su caso no se ejecutó. Los horarios creados no se pueden borrar (la API no tiene ese endpoint) y "
                    "la reserva sobre el horario pasado de CP-HU04 no se puede cancelar por diseño."),
    "item": [req(f"Limpieza · Cancelar la reserva de {caso}", "PATCH", "api/v1/bookings/{{%s}}/cancel" % var,
                 "No es un escenario de prueba: deja los datos como estaban.",
                 pre=f'if (!pm.collectionVariables.get("{var}")) {{ pm.execution.skipRequest(); }}',
                 test=f'pm.test("La reserva de prueba quedó cancelada", () => pm.expect(pm.response.code).to.be.oneOf([200, 400]));\n'
                      f'pm.collectionVariables.unset("{var}");')
             for var, caso in LIMPIAR],
}

CLIENTES = """
// Clientes de prueba: se generan una sola vez; no hay que configurarlos en el entorno
["clienteAnaId", "clienteLuisId"].forEach(k => {
    if (!pm.collectionVariables.get(k)) pm.collectionVariables.set(k, pm.variables.replaceIn("{{$guid}}"));
});
"""

collection = {
    "info": {
        "_postman_id": str(uuid.uuid5(uuid.NAMESPACE_URL, "bookify-cp-hu")),
        "name": "Bookify – Casos de prueba HU-01 a HU-05",
        "description": """Casos de prueba manuales CP-HU01 a CP-HU05 del Plan de Pruebas PP-BOOKIFY-001.

**Antes de ejecutar**, selecciona el entorno *Bookify QA*. Ya trae:
- `baseUrl`: la API en Render (cámbiala a http://localhost:8080 para correr en local).
- `agendaServicioId`: una agenda de servicio **activa**.
- `agendaInactivaId` (opcional): id de una agenda inactiva, para CP-HU01-E4; si está vacío, E4 se omite.

- `clienteAnaId` y `clienteLuisId`: dos clientes de prueba que existen en la base (la API exige que el cliente exista).

No hay que cambiar nada entre ejecuciones: cada corrida crea horarios con fechas aleatorias. La carpeta **Limpieza**, al final, cancela las reservas futuras que creó la corrida.

Cada petición se identifica como `CP-HUxx-Pn` (preparación), `CP-HUxx-F1` (camino feliz) o `CP-HUxx-En` (excepciones y criterios de rechazo).

Cada carpeta es independiente: los pasos **Preparar** crean los datos que necesita el caso, con horarios aleatorios para poder repetir la ejecución. 

Para la evidencia (CA-07), ejecuta la colección con el *Collection Runner* y exporta los resultados.""",
        "schema": "https://schema.getpostman.com/json/collection/v2.1.0/collection.json",
    },
    "item": [hu01, hu02, hu03, hu04, hu05, limpieza],
    "variable": [],
}

environment = {
    "id": str(uuid.uuid5(uuid.NAMESPACE_URL, "bookify-qa-env")),
    "name": "Bookify QA",
    "values": [
        {"key": "baseUrl", "value": "https://bookify-pb92.onrender.com", "type": "default", "enabled": True},
        {"key": "agendaServicioId", "value": "6f085c7d-a713-4657-9fbf-17c914609768", "type": "default", "enabled": True},
        {"key": "clienteAnaId", "value": "11111111-1111-1111-1111-111111111111", "type": "default", "enabled": True},
        {"key": "clienteLuisId", "value": "22222222-2222-2222-2222-222222222222", "type": "default", "enabled": True},
        {"key": "agendaInactivaId", "value": "", "type": "default", "enabled": True},
    ],
    "_postman_variable_scope": "environment",
}

(OUT / "Bookify-CP-HU.postman_collection.json").write_text(json.dumps(collection, indent=2, ensure_ascii=False) + "\n")
(OUT / "Bookify-QA.postman_environment.json").write_text(json.dumps(environment, indent=2, ensure_ascii=False) + "\n")
n = sum(len(f["item"]) for f in collection["item"])
print("requests:", n)
