import json, sys, urllib.request, urllib.error
from datetime import datetime, timezone, timedelta
B="https://bookify-pb92.onrender.com"; CL="Calidad-Sprint2-Clave-2026!"; TS=datetime.now(timezone.utc).strftime("%m%d%H%M%S")
def call(m,p,b=None,t=None):
    h={"Content-Type":"application/json"}; 
    if t: h["Authorization"]="Bearer "+t
    r=urllib.request.Request(B+p,data=json.dumps(b).encode() if b is not None else None,method=m,headers=h)
    try:
        with urllib.request.urlopen(r,timeout=90) as x: return x.status, json.loads(x.read() or b"null"), {k.lower():v for k,v in x.headers.items()}
    except urllib.error.HTTPError as e:
        raw=e.read(); 
        try: body=json.loads(raw)
        except Exception: body=raw[:200].decode()
        return e.code, body, {k.lower():v for k,v in e.headers.items()}
out={}
reg=lambda tag,rol: call("POST","/api/v1/auth/register",{"nombre":"Sonda "+tag,"correo":f"qa.s2.sonda.{tag}.{TS}@bookify-qa.test","contrasena":CL,"rol":rol})
log=lambda tag: call("POST","/api/v1/auth/login",{"correo":f"qa.s2.sonda.{tag}.{TS}@bookify-qa.test","password":CL})[1]["accessToken"]
c1=reg("c1","CLIENTE")[1]; c2=reg("c2","CLIENTE")[1]; reg("p","PROVEEDOR")
t1,t2,tp=log("c1"),log("c2"),log("p")
# HU-06 datos inválidos: ¿lista los campos?
out["hu06_rol_invalido"]=call("POST","/api/v1/auth/register",{"nombre":"X","correo":f"qa.s2.sonda.x.{TS}@bookify-qa.test","contrasena":CL,"rol":"ADMIN"})[:2]
out["hu06_nombre_vacio"]=call("POST","/api/v1/auth/register",{"nombre":"","correo":f"qa.s2.sonda.y.{TS}@bookify-qa.test","contrasena":CL,"rol":"CLIENTE"})[:2]
# HU-08 Retry-After sin distinguir mayúsculas
lm=f"qa.s2.sonda.lock.{TS}@bookify-qa.test"; reg("lock","CLIENTE")
for _ in range(4): call("POST","/api/v1/auth/login",{"correo":lm,"password":"Mala-Clave-Intento-00"})
s,b,h=call("POST","/api/v1/auth/login",{"correo":lm,"password":"Mala-Clave-Intento-05"})
out["hu08_quinto"]=(s,b,{"retry-after":h.get("retry-after")})
# Reservas con clientes válidos
t0=datetime(2027,3,2,13,0,tzinfo=timezone.utc)+timedelta(minutes=int(TS[-4:])%500)
d=call("POST","/api/v1/disponibilidades",{"agendaServicioId":"6f085c7d-a713-4657-9fbf-17c914609768","inicioAt":t0.isoformat(),"finAt":(t0+timedelta(hours=1)).isoformat()},tp)[1]
did=d.get("id")
s,b,_=call("POST","/api/v1/bookings",{"clienteId":c2["id"],"disponibilidadId":did},t1); out["reserva_a_nombre_de_otro"]=(s,b)
bid=b.get("id") if isinstance(b,dict) else None
s,b,_=call("POST","/api/v1/bookings",{"clienteId":c1["id"],"disponibilidadId":did},t1); out["doble_reserva"]=(s,b)
if bid:
    s,b,_=call("PATCH",f"/api/v1/bookings/{bid}/cancel",None,t1); out["cancelar_reserva_ajena"]=(s,b)
json.dump(out,open(sys.argv[1],"w"),ensure_ascii=False,indent=1,default=str)
for k,v in out.items(): print(k, json.dumps(v,ensure_ascii=False,default=str)[:260])
