"""Revisa sin abrir el juego lo que escribe `bloques.py`: que cada estado tenga sus modelos, que cada modelo tenga sus
texturas (todas definidas, de 64 de ancho y fotogramas de 64), que los elementos esten en rango (-16..32) con angulos
validos (-45, -22,5, 0, 22,5, 45) y que cada bloque tenga su botin. Uso: python3 tools/bloques/validar.py (desde la raiz)."""
import json, os, sys, zipfile
from PIL import Image
RAIZ = os.path.abspath(os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", ".."))
A = os.path.join(RAIZ, "src/main/resources/assets/darkcraft")
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import bloques as B
malos = []
vistos = set()
jar = zipfile.ZipFile("/tmp/cp/cpjars/client-extra.jar")
def leer(ref):
    ns, path = ref.split(":") if ":" in ref else ("minecraft", ref)
    if ns == "darkcraft":
        f = f"{A}/models/{path}.json"
        return json.load(open(f)) if os.path.exists(f) else None
    try: return json.loads(jar.read(f"assets/minecraft/models/{path}.json"))
    except KeyError: return None
def texs(ref):
    out = {}
    while ref and not ref.startswith("builtin"):
        m = leer(ref)
        if m is None: break
        for k, v in m.get("textures", {}).items(): out.setdefault(k, v)
        ref = m.get("parent")
    return out
def elementos(ref):
    while ref and not ref.startswith("builtin"):
        m = leer(ref)
        if m is None: return []
        if "elements" in m: return m["elements"]
        ref = m.get("parent")
    return []
def resolver(t, T, prof=0):
    while t.startswith("#") and prof < 10:
        t = T.get(t[1:], "?"); prof += 1
    return t
def textura(ref):
    ns, path = ref.split(":") if ":" in ref else ("minecraft", ref)
    if ns != "darkcraft": return
    f = f"{A}/textures/{path}.png"
    if not os.path.exists(f): malos.append("falta textura " + ref); return
    w, h = Image.open(f).size
    if w != 64 or h % 64: malos.append(f"textura {ref} {w}x{h}")
def rev(ref):
    """Un modelo usado por un estado: sus texturas (todas definidas) y sus elementos (en rango, angulos validos)."""
    if ref in vistos: return
    vistos.add(ref)
    if leer(ref) is None: malos.append("falta modelo " + ref); return
    T = texs(ref)
    for k, v in T.items():
        if not v.startswith("#"): textura(v)
    for el in elementos(ref):
        for c in ("from", "to"):
            if any(x < -16 or x > 32 for x in el[c]): malos.append(f"fuera de rango {ref} {el[c]}")
        r = el.get("rotation")
        if r and r["angle"] not in (-45, -22.5, 0, 22.5, 45): malos.append(f"angulo {ref} {r['angle']}")
        for cara, fc in el["faces"].items():
            t = resolver(fc["texture"], T)
            if t == "?" or t.startswith("#"): malos.append(f"textura sin definir {ref} {fc['texture']}")
n = 0
for m in B.MATERIALES:
    for f in m["formas"]:
        bid = m["id"] + ("" if f in B.SIN_SUFIJO else B.FORMAS[f])
        for b in [bid] + ([bid + "_pared"] if f == "t" else []):
            n += 1
            bs = json.load(open(f"{A}/blockstates/{b}.json"))
            refs = [e["model"] for v in bs.get("variants", {}).values() for e in (v if isinstance(v, list) else [v])] + \
                   [p["apply"]["model"] for p in bs.get("multipart", [])]
            for r in refs: rev(r)
            assert os.path.exists(os.path.join(RAIZ, f"src/main/resources/data/darkcraft/loot_tables/blocks/{b}.json")), b
        it = json.load(open(f"{A}/models/item/{bid}.json"))
        if "parent" in it and not it["parent"].endswith("generated"): rev(it["parent"])
        for k, v in it.get("textures", {}).items(): textura(v)
print(n, "bloques;", len(vistos), "modelos;", "problemas:", malos[:10], len(malos))
sys.exit(1 if malos else 0)
