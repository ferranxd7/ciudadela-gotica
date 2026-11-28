"""Ver bloques del mod sin abrir el juego: un dibujante por software de los modelos de bloque (JSON de Minecraft) con sus
texturas, en una escena de bloques puestos con sus propiedades (lee los estados: `variants` y `multipart`).

Uso desde Python:
    import ver_modelo as V
    esc = V.Escena()
    esc.poner((0, 0, 0), "darkcraft:rama_roble", {"up": "true", "north": "true", ...})
    esc.dibujar("salida.png", ancho=900, alto=700)

Los modelos de Minecraft (padres como `block/cube_all`, `block/cross`, `block/stairs`...) se leen de
/tmp/cp/cpjars/client-extra.jar (solo para dibujar: no se copia nada). La primera variante de cada estado (sin azar).
"""
import json
import math
import os
import zipfile

import numpy as np
from PIL import Image

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
ASSETS = os.path.join(RAIZ, "src", "main", "resources", "assets")
JAR = "/tmp/cp/cpjars/client-extra.jar"
_jar = None


def _leer(ns, ruta):
    """Un fichero de assets: del mod (si es darkcraft) o del jar de Minecraft."""
    global _jar
    if ns == "darkcraft":
        with open(os.path.join(ASSETS, ns, ruta), "rb") as f:
            return f.read()
    if _jar is None:
        _jar = zipfile.ZipFile(JAR)
    return _jar.read(f"assets/{ns}/{ruta}")


def _ref(r):
    return r.split(":", 1) if ":" in r else ("minecraft", r)


_modelos = {}
_texturas = {}


def modelo(ref):
    """El modelo con sus padres resueltos: (elementos, texturas)."""
    if ref in _modelos:
        return _modelos[ref]
    ns, ruta = _ref(ref)
    m = json.loads(_leer(ns, f"models/{ruta}.json"))
    elementos, texturas = None, {}
    if "parent" in m and not m["parent"].endswith("builtin/generated"):
        elementos, texturas = modelo(m["parent"])
        texturas = dict(texturas)
    texturas.update(m.get("textures", {}))
    if "elements" in m:
        elementos = m["elements"]
    _modelos[ref] = (elementos or [], texturas)
    return _modelos[ref]


def textura(ref, texs):
    while ref.startswith("#"):
        ref = texs.get(ref[1:], "minecraft:missingno")
    if ref in _texturas:
        return _texturas[ref]
    ns, ruta = _ref(ref)
    try:
        im = Image.open(__import__("io").BytesIO(_leer(ns, f"textures/{ruta}.png"))).convert("RGBA")
        a = np.asarray(im, dtype=float) / 255.0
        if a.shape[0] > a.shape[1]:   # animada: el primer fotograma
            a = a[:a.shape[1]]
    except Exception:
        a = np.zeros((16, 16, 4))
        a[..., 0] = a[..., 2] = 1
        a[..., 3] = 1
    _texturas[ref] = a
    return a


def estado(ref_bloque, props):
    """Los modelos (ref, x, y, uvlock) de un bloque con esas propiedades."""
    ns, nombre = _ref(ref_bloque)
    bs = json.loads(_leer(ns, f"blockstates/{nombre}.json"))
    out = []

    def toma(v):
        v = v[0] if isinstance(v, list) else v
        out.append((v["model"], v.get("x", 0), v.get("y", 0), v.get("uvlock", False)))

    def cumple(cond):
        if "OR" in cond:
            return any(cumple(c) for c in cond["OR"])
        for k, val in cond.items():
            if str(props.get(k, "")) not in str(val).split("|"):
                return False
        return True
    if "variants" in bs:
        for clave, v in bs["variants"].items():
            if clave == "" or all(props.get(p.split("=")[0]) == p.split("=")[1] for p in clave.split(",")):
                toma(v)
                break
    else:
        for parte in bs["multipart"]:
            if "when" not in parte or cumple(parte["when"]):
                toma(parte["apply"])
    return out


# ------------------------------------------------------------------ geometria

_CARAS = {
    # cara: (esquinas en funcion de (x0,y0,z0,x1,y1,z1), en el orden de los uv: arriba-izq, arriba-der, abajo-der, abajo-izq)
    "down": lambda a, b: [(a[0], a[1], b[2]), (b[0], a[1], b[2]), (b[0], a[1], a[2]), (a[0], a[1], a[2])],
    "up": lambda a, b: [(a[0], b[1], a[2]), (b[0], b[1], a[2]), (b[0], b[1], b[2]), (a[0], b[1], b[2])],
    "north": lambda a, b: [(b[0], b[1], a[2]), (a[0], b[1], a[2]), (a[0], a[1], a[2]), (b[0], a[1], a[2])],
    "south": lambda a, b: [(a[0], b[1], b[2]), (b[0], b[1], b[2]), (b[0], a[1], b[2]), (a[0], a[1], b[2])],
    "west": lambda a, b: [(a[0], b[1], a[2]), (a[0], b[1], b[2]), (a[0], a[1], b[2]), (a[0], a[1], a[2])],
    "east": lambda a, b: [(b[0], b[1], b[2]), (b[0], b[1], a[2]), (b[0], a[1], a[2]), (b[0], a[1], b[2])],
}
_LUZ = {"up": 1.0, "down": 0.5, "north": 0.8, "south": 0.8, "east": 0.6, "west": 0.6}


def _uv_defecto(cara, a, b):
    if cara == "down":
        return [a[0], 16 - b[2], b[0], 16 - a[2]]
    if cara == "up":
        return [a[0], a[2], b[0], b[2]]
    if cara == "north":
        return [16 - b[0], 16 - b[1], 16 - a[0], 16 - a[1]]
    if cara == "south":
        return [a[0], 16 - b[1], b[0], 16 - a[1]]
    if cara == "west":
        return [a[2], 16 - b[1], b[2], 16 - a[1]]
    return [16 - b[2], 16 - b[1], 16 - a[2], 16 - a[1]]


def _rot(p, eje, ang, centro):
    c, s = math.cos(math.radians(ang)), math.sin(math.radians(ang))
    x, y, z = p[0] - centro[0], p[1] - centro[1], p[2] - centro[2]
    if eje == "x":
        y, z = y * c - z * s, y * s + z * c
    elif eje == "y":
        x, z = x * c + z * s, -x * s + z * c
    else:
        x, y = x * c - y * s, x * s + y * c
    return (x + centro[0], y + centro[1], z + centro[2])


class Escena:
    def __init__(self):
        self.quads = []   # (puntos 3D en bloques, uvs, textura, luz)

    def poner(self, pos, bloque, props=None):
        for (ref, rx, ry, uvlock) in estado(bloque, props or {}):
            self.modelo(pos, ref, rx, ry)

    def modelo(self, pos, ref, rx=0, ry=0):
        elementos, texs = modelo(ref)
        for el in elementos:
            a, b = el["from"], el["to"]
            for cara, f in el.get("faces", {}).items():
                pts = _CARAS[cara](a, b)
                uv = f.get("uv", _uv_defecto(cara, a, b))
                esquinas_uv = [(uv[0], uv[1]), (uv[2], uv[1]), (uv[2], uv[3]), (uv[0], uv[3])]
                r = f.get("rotation", 0) // 90
                if r:
                    # el giro de la textura en la cara (en el sentido de las agujas del reloj)
                    esquinas_uv = esquinas_uv[-r:] + esquinas_uv[:-r]
                if "rotation" in el:
                    ro = el["rotation"]
                    pts = [_rot(p, ro["axis"], ro["angle"], ro["origin"]) for p in pts]
                # el giro del estado (primero x, luego y, alrededor del centro del bloque; y gira al reves)
                pts = [_rot(p, "x", -rx, (8, 8, 8)) for p in pts]
                pts = [_rot(p, "y", -ry, (8, 8, 8)) for p in pts]
                pts = [(pos[0] + p[0] / 16, pos[1] + p[1] / 16, pos[2] + p[2] / 16) for p in pts]
                self.quads.append((pts, esquinas_uv, textura(f["texture"], texs), _LUZ[cara] if el.get("shade", True) else 1.0))

    def dibujar(self, salida, ancho=900, alto=700, giro=35, inclina=30, fondo=(70, 92, 120), escala=None):
        """Proyeccion ortografica: mirando desde el sureste (`giro` grados desde el sur hacia el este) y desde arriba."""
        g, t = math.radians(giro), math.radians(inclina)

        def proy(p):
            x, y, z = p
            u = x * math.cos(g) - z * math.sin(g)
            w = x * math.sin(g) + z * math.cos(g)
            v = -y * math.cos(t) + w * math.sin(t)
            d = w * math.cos(t) + y * math.sin(t)
            return u, v, d
        todos = [proy(p) for q in self.quads for p in q[0]]
        us = [a[0] for a in todos]
        vs = [a[1] for a in todos]
        if escala is None:
            escala = min((ancho - 40) / (max(us) - min(us) + 1e-6), (alto - 40) / (max(vs) - min(vs) + 1e-6))
        u0 = (ancho - escala * (max(us) - min(us))) / 2 - escala * min(us)
        v0 = (alto - escala * (max(vs) - min(vs))) / 2 - escala * min(vs)
        img = np.zeros((alto, ancho, 3))
        img[:] = np.array(fondo) / 255.0
        zb = np.full((alto, ancho), -1e9)
        for pts, uvs, tex, luz in self.quads:
            P = [proy(p) for p in pts]
            S = [(u0 + escala * a[0], v0 + escala * a[1], a[2]) for a in P]
            for tri in ((0, 1, 2), (0, 2, 3)):
                self._tri(img, zb, [S[i] for i in tri], [uvs[i] for i in tri], tex, luz)
        Image.fromarray((np.clip(img, 0, 1) * 255).astype(np.uint8)).save(salida)

    @staticmethod
    def _tri(img, zb, S, UV, tex, luz):
        (x0, y0, d0), (x1, y1, d1), (x2, y2, d2) = S
        minx, maxx = int(max(0, math.floor(min(x0, x1, x2)))), int(min(img.shape[1] - 1, math.ceil(max(x0, x1, x2))))
        miny, maxy = int(max(0, math.floor(min(y0, y1, y2)))), int(min(img.shape[0] - 1, math.ceil(max(y0, y1, y2))))
        if minx > maxx or miny > maxy:
            return
        den = (y1 - y2) * (x0 - x2) + (x2 - x1) * (y0 - y2)
        if abs(den) < 1e-9:
            return
        ys, xs = np.mgrid[miny:maxy + 1, minx:maxx + 1] + 0.5
        a = ((y1 - y2) * (xs - x2) + (x2 - x1) * (ys - y2)) / den
        b = ((y2 - y0) * (xs - x2) + (x0 - x2) * (ys - y2)) / den
        c = 1 - a - b
        dentro = (a >= -1e-6) & (b >= -1e-6) & (c >= -1e-6)
        if not dentro.any():
            return
        d = a * d0 + b * d1 + c * d2
        u = a * UV[0][0] + b * UV[1][0] + c * UV[2][0]
        v = a * UV[0][1] + b * UV[1][1] + c * UV[2][1]
        h, w = tex.shape[:2]
        ti = np.clip((v / 16 * h).astype(int), 0, h - 1)
        tj = np.clip((u / 16 * w).astype(int), 0, w - 1)
        col = tex[ti, tj]
        ver = dentro & (col[..., 3] > 0.1) & (d > zb[miny:maxy + 1, minx:maxx + 1])
        zona = img[miny:maxy + 1, minx:maxx + 1]
        zona[ver] = col[ver][:, :3] * luz
        zb[miny:maxy + 1, minx:maxx + 1][ver] = d[ver]
