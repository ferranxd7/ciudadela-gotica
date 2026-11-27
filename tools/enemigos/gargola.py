"""
Las gargolas de la ciudadela (parte 7aa): los guardianes de piedra de la Sombra que esperan posados en la cadena del sur y en
la isla; cuando sube alguien con la marca se les resquebraja la piedra, se les encienden los ojos y bajan volando a por el.

Una bestia de piedra encorvada (unos dos bloques de pie) con alas de murcielago:
- piedra gris de granito con grietas, liquen verde palido en las juntas y la piedra mas oscura y lisa por donde la gasto la
  lluvia; las grietas, por dentro, rojas (en la capa de brillo, solo despierta: posada es una estatua);
- la cabeza de perro con hocico corto, cejas de piedra, dos cuernos que se curvan hacia atras y dos orejas en punta; la
  mandibula con colmillos; los ojos rojos (brillo);
- los brazos largos con manos de tres garras, las piernas de garra (el tobillo alto) con tres dedos, una fila de puas de
  piedra por el espinazo;
- las alas de murcielago en tres trozos (como las del dragon, mas cortas), la cola en tres trozos con la punta en pala.

Las alas se ponen con las mismas cuentas que las del dragon (`dragon.ala`, `alas`, `mezcla_alas`: hacia donde va cada hueso y
hacia donde cae su membrana, en el sistema del torso). De pie el torso esta casi derecho (el sistema del torso: x a la
izquierda, y hacia abajo, z hacia atras); en vuelo va tendido: las poses de vuelo del dragon se giran al torso con `girar`.
"""
import math

import numpy as np

from anim import Anim, con, raiz
from dragon import ALA, mezcla_alas, _matriz, _euler, _norm
from modelo import Box, Rig
from pinta import claro, delante, mezcla, oscuro, ruido, sombra

NOMBRE = "gargola"
VARIANTES = ("piedra",)
ESCALA = 1.05
CENTRO_Y = 1.2

OCULTAS = {0: []}


def reposo(v, por_nombre):
    """Para la hoja de las variantes: de pie, con las alas plegadas."""
    return por_nombre["quieto"].muestra(0.0)


# ---------------------------------------------------------------- el modelo
# (pixeles; Y hacia abajo; el suelo en y = 24; mira a -Z; la derecha de la gargola es -X)

def _espejar(boxes):
    return [Box(-(b.x + b.w), b.y, b.z, b.w, b.h, b.d, b.mat, b.grow, b.uv) for b in boxes]


#: lo largo de cada hueso del ala (de la raiz a la punta) y lo hondo de su membrana
LARGO_ALA = (9.0, 10.0, 9.0)
FONDO_ALA = (9.0, 10.0, 10.0)


def rig():
    r = Rig(NOMBRE, 128)
    r.pieza("raiz", "", (0, 24, 0))
    # la cadera (a 13 del suelo); el torso, derecho en reposo (en las poses se encorva hacia delante)
    r.pieza("cadera", "raiz", (0, -13, 1), boxes=[Box(-4.5, -3.5, -3, 9, 6, 6, "cadera")])
    puas = [Box(-0.8, -12.6 + k * 2.7, 3.0, 1.6, 1.6, 1.8 + (k % 2) * 0.6, "pua", uv="pua") for k in range(4)]
    r.pieza("torso", "cadera", (0, -3, 0), boxes=[
        Box(-5.5, -12, -3.5, 11, 12, 7, "torso"),
        Box(-4.0, -8.5, -4.2, 8, 7, 1.2, "pecho"),
    ] + puas)
    # la cabeza: el craneo, el hocico, las cejas y las orejas en punta; la mandibula con sus colmillos; los cuernos
    r.pieza("cabeza", "torso", (0, -12, -1), boxes=[
        Box(-3.5, -6, -4, 7, 6, 6.5, "craneo"),
        Box(-2.5, -3.4, -7.4, 5, 3.4, 3.6, "hocico"),
        Box(-3.8, -6.5, -4.4, 7.6, 1.5, 2.2, "cejas"),
        Box(-4.8, -7.6, -0.6, 1.2, 3.4, 1.6, "oreja", uv="oreja"),
        Box(3.6, -7.6, -0.6, 1.2, 3.4, 1.6, "oreja", uv="oreja"),
    ])
    r.pieza("mandibula", "cabeza", (0, -0.6, -2.6), boxes=[
        Box(-2.3, 0, -4.6, 4.6, 1.8, 4.8, "mandibula"),
        Box(-2.1, -1.4, -4.4, 0.8, 1.4, 0.8, "colmillo", uv="colmillo"),
        Box(1.3, -1.4, -4.4, 0.8, 1.4, 0.8, "colmillo", uv="colmillo"),
    ])
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"cuerno_{n}", "cabeza", (s * 2.4, -5.6, -1.2), rot=(48, s * 22, 0), boxes=[
            Box(-0.9, -0.9, 0, 1.8, 1.8, 4.6, "cuerno", uv="cuerno")])
        r.pieza(f"cuerno2_{n}", f"cuerno_{n}", (0, 0, 4.2), rot=(42, s * 8, 0), boxes=[
            Box(-0.6, -0.6, 0, 1.2, 1.2, 3.8, "cuerno2", uv="cuerno2")])
    # los brazos (largos) con las manos de tres garras
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"brazo_{n}", "torso", (s * 6.6, -10.6, 0), boxes=[Box(-1.8, -1.2, -1.8, 3.6, 8, 3.6, "brazo", uv="brazo")])
        r.pieza(f"antebrazo_{n}", f"brazo_{n}", (0, 6.8, 0), boxes=[Box(-1.5, 0, -1.5, 3, 7, 3, "antebrazo", uv="antebrazo")])
        r.pieza(f"mano_{n}", f"antebrazo_{n}", (0, 6.6, 0), boxes=[
            Box(-1.8, 0, -1.8, 3.6, 2.4, 3.6, "mano", uv="mano"),
            Box(-1.7, 2.0, -2.0, 0.9, 2.6, 0.9, "garra", uv="garra"),
            Box(-0.45, 2.0, -2.2, 0.9, 2.9, 0.9, "garra2", uv="garra2"),
            Box(0.8, 2.0, -2.0, 0.9, 2.6, 0.9, "garra", uv="garra"),
        ])
    # las piernas de garra: el muslo hacia delante, la pierna hacia atras, el tarso y el pie
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"muslo_{n}", "cadera", (s * 2.9, 1.5, 0), boxes=[Box(-1.9, -1, -2, 3.8, 7, 4, "muslo", uv="muslo")])
        r.pieza(f"pierna_{n}", f"muslo_{n}", (0, 6, 0), boxes=[Box(-1.4, 0, -1.4, 2.8, 6, 2.8, "pierna", uv="pierna")])
        r.pieza(f"pata_{n}", f"pierna_{n}", (0, 5.6, 0), boxes=[
            Box(-1.3, 0, -1.3, 2.6, 4.4, 2.6, "tarso", uv="tarso"),
            Box(-1.9, 3.6, -3.8, 3.8, 1.3, 4.8, "pie", uv="pie"),
            Box(-1.8, 3.9, -4.9, 0.9, 1.0, 1.4, "garra_pie", uv="garra_pie"),
            Box(-0.45, 3.9, -5.1, 0.9, 1.0, 1.6, "garra_pie", uv="garra_pie2"),
            Box(0.9, 3.9, -4.9, 0.9, 1.0, 1.4, "garra_pie", uv="garra_pie"),
        ])
    # la cola en tres trozos y la punta en pala
    r.pieza("cola1", "cadera", (0, 1.2, 2.6), rot=(-20, 0, 0), boxes=[Box(-1.4, -1.4, -0.5, 2.8, 2.8, 6.5, "cola1")])
    r.pieza("cola2", "cola1", (0, 0, 5.6), rot=(-10, 0, 0), boxes=[Box(-1.1, -1.1, -0.5, 2.2, 2.2, 6.5, "cola2")])
    r.pieza("cola3", "cola2", (0, 0, 5.6), rot=(10, 0, 0), boxes=[Box(-0.8, -0.8, -0.5, 1.6, 1.6, 6.0, "cola3")])
    r.pieza("punta", "cola3", (0, 0, 5.2), boxes=[Box(-2.4, -0.4, -0.4, 4.8, 0.8, 4.6, "punta")])
    # las alas (abiertas en reposo): el hueso a lo largo de -X (la derecha) y su membrana hacia atras (+Z), fina
    for s, n in ((-1, "d"), (1, "i")):
        hueso = [
            [Box(-LARGO_ALA[0], -1.2, -1.2, LARGO_ALA[0] + 0.6, 2.4, 2.4, "ala_hueso", uv="ala_hueso"),
             Box(-LARGO_ALA[0], -0.25, 1.0, LARGO_ALA[0], 0.5, FONDO_ALA[0], f"membrana1_{n}")],
            [Box(-LARGO_ALA[1], -0.9, -0.9, LARGO_ALA[1] + 0.4, 1.8, 1.8, "ala_hueso2", uv="ala_hueso2"),
             Box(-LARGO_ALA[1], -0.25, 0.7, LARGO_ALA[1], 0.5, FONDO_ALA[1], f"membrana2_{n}"),
             Box(-LARGO_ALA[1] - 0.6, -2.6, -0.8, 1.4, 2.0, 1.6, "garra_ala", uv="garra_ala")],
            [Box(-LARGO_ALA[2], -0.6, -0.6, LARGO_ALA[2] + 0.4, 1.2, 1.2, "ala_hueso3", uv="ala_hueso3"),
             Box(-LARGO_ALA[2], -0.25, 0.4, LARGO_ALA[2], 0.5, FONDO_ALA[2], f"membrana3_{n}")],
        ]
        if s > 0:
            hueso = [_espejar(h) for h in hueso]
        r.pieza(f"ala_{n}", "torso", (s * 3.6, -10.4, 2.8), boxes=hueso[0])
        r.pieza(f"ala2_{n}", f"ala_{n}", (s * LARGO_ALA[0], 0, 0), boxes=hueso[1])
        r.pieza(f"ala3_{n}", f"ala2_{n}", (s * LARGO_ALA[1], 0, 0), boxes=hueso[2])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas

PIEDRA = ["#2b2a2c", "#39383a", "#474648", "#555457", "#636265", "#727174", "#838285"]
LISA = ["#3c3b3e", "#4a494c", "#58575a", "#666568"]
LIQUEN = ["#4f5a3e", "#66714d", "#7f8a5f"]
GRIETA = "#161516"
BRASA = ["#3a0605", "#6a0d08", "#a8180c", "#e8381c", "#ff7a4a", "#ffc0a0"]
OJO = ["#5a0503", "#c4180c", "#ff4a2a", "#ffd2c0"]
BOCA = ["#1a0f0f", "#2c1818", "#422424"]
DIENTE = ["#8f8a80", "#b8b2a6", "#d8d2c4"]
HUECO = "#0b0a0b"

KC = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _grietas(semilla, n=8, largo=8):
    """Grietas: caminos al azar sobre una rejilla de 64 x 64 (se repite) que bajan haciendo zigzag (2 = un nudo de la grieta)."""
    rng = np.random.default_rng(semilla)
    m = np.zeros((64, 64), dtype=np.int8)
    for _ in range(n):
        x, y = int(rng.integers(0, 64)), int(rng.integers(0, 64))
        for k in range(largo + int(rng.integers(0, largo))):
            m[y % 64, x % 64] = 2 if k % 5 == 0 else 1
            d = int(rng.integers(0, 4))
            if d == 0:
                x += 1
            elif d == 1:
                x -= 1
            else:
                y += 1
    return m


GRIETAS = _grietas(1701, n=10, largo=9)


def materiales(variante):
    piedra = ruido(PIEDRA[1:], 1702, grano=0.5, base=0.5)
    lisa = ruido(LISA, 1703, grano=0.3, base=0.5)
    liquen = np.random.default_rng(1704).random((64, 64))
    memb = ruido(["#2c2b2e", "#353437", "#3e3d40", "#48474a"], 1705, grano=0.45, base=0.5)
    roto = np.random.default_rng(1706).random((64, 64))

    def roca(cara, x, y, w, h, lisa_abajo=True):
        """Granito con grietas oscuras y liquen en las juntas; por abajo, mas liso y oscuro (donde no le da la lluvia)."""
        if cara == "abajo" and lisa_abajo:
            return oscuro(lisa(cara, x, y, w, h), 0.12)
        g = GRIETAS[(y + 7 * KC[cara]) % 64, (x + 13 * KC[cara]) % 64]
        if g:
            return GRIETA
        c = piedra(cara, x, y, w, h)
        # el liquen: manchas palidas por arriba y por los cantos de arriba
        q = liquen[(y * 2 + KC[cara] * 5) % 64, (x * 3 + KC[cara] * 11) % 64]
        if (cara == "arriba" or y <= 1) and q > 0.86:
            return mezcla(c, LIQUEN[int(q * 31) % 3], 0.65)
        return c

    def torso(cara, x, y, w, h):
        c = roca(cara, x, y, w, h)
        if cara == "frente" and 2 <= y <= h - 3 and (y % 3 == 0) and 1 <= x <= w - 2:
            return oscuro(c, 0.22)                           # las costillas marcadas
        return c

    def pecho(cara, x, y, w, h):
        c = lisa(cara, x, y, w, h)
        return oscuro(c, 0.25) if y % 2 == 1 else c

    def pua(cara, x, y, w, h):
        c = piedra(cara, x, y, w, h)
        return oscuro(c, 0.35) if y == 0 else claro(c, 0.05)

    def craneo(cara, x, y, w, h):
        c = roca(cara, x, y, w, h)
        if cara == "frente":
            if y in (2, 3) and x in (1, 2, w - 3, w - 2):
                return HUECO                                 # las cuencas (le brillan los ojos)
            if y == 1:
                return oscuro(c, 0.35)                       # la sombra de la ceja
        return c

    def hocico(cara, x, y, w, h):
        c = roca(cara, x, y, w, h)
        if cara == "frente":
            if y == 1 and x in (1, w - 2):
                return HUECO                                 # los ollares
            if y == h - 1:
                return DIENTE[1] if x % 2 == 0 else BOCA[0]
        if cara in ("der", "izq") and y == h - 1:
            return DIENTE[2] if x % 2 == 0 else BOCA[0]
        if cara == "abajo":
            return BOCA[1] if (x + y) % 3 else BOCA[2]
        return c

    def cejas(cara, x, y, w, h):
        c = piedra(cara, x, y, w, h)
        if cara == "abajo":
            return HUECO
        return claro(c, 0.08) if cara == "arriba" else c

    def oreja(cara, x, y, w, h):
        c = piedra(cara, x, y, w, h)
        if cara == "frente" and 0 < x < w - 1 and y > 0:
            return oscuro(c, 0.4)
        return c

    def mandibula(cara, x, y, w, h):
        c = roca(cara, x, y, w, h)
        if cara == "arriba":
            if x in (0, w - 1):
                return DIENTE[1] if y % 2 == 0 else BOCA[0]
            return BOCA[2] if abs(x - (w - 1) / 2) <= 1 else BOCA[1]
        if cara in ("der", "izq", "frente") and y == 0:
            return DIENTE[2] if x % 2 == 1 else BOCA[0]
        return c

    def colmillo(cara, x, y, w, h):
        return DIENTE[2] if y == 0 else DIENTE[1]

    def cuerno(cara, x, y, w, h):
        c = piedra(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo"):
            d = (w - 1 - x) if cara == "der" else x if cara == "izq" else (h - 1 - y)
            if (d + 1) % 3 == 0:
                return oscuro(c, 0.3)                        # los anillos del cuerno
        return c

    def cuerno2(cara, x, y, w, h):
        c = piedra(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo"):
            d = (w - 1 - x) if cara == "der" else x if cara == "izq" else (h - 1 - y)
            return mezcla(c, PIEDRA[0], min(1.0, d / 6.0))   # negra en la punta
        return c

    def brazo(cara, x, y, w, h):
        return roca(cara, x, y, w, h)

    def mano(cara, x, y, w, h):
        c = roca(cara, x, y, w, h)
        return oscuro(c, 0.2) if cara == "abajo" else c

    def garra(cara, x, y, w, h):
        return PIEDRA[0] if y >= h - 2 else PIEDRA[1]

    def ala_hueso(cara, x, y, w, h):
        c = piedra(cara, x, y, w, h)
        return oscuro(c, 0.15) if cara == "abajo" else c

    def membrana(tipo, izquierda=False):
        """La membrana de un trozo del ala (caras de arriba y abajo): x a lo largo del ala, y del borde de atras (0) al hueso."""
        from dragon import borde_ala, dedos_ala, dedo_ala

        def f(cara, x, y, w, h):
            if cara not in ("arriba", "abajo"):
                return None
            s = (w - 1 - x if izquierda else x) / max(1, w - 1)
            q = 1.0 - y / max(1, h - 1)
            if q > borde_ala(tipo, s):
                return None
            if roto[(y * 3 + KC[cara]) % 64, (int(s * 63) * 2 + tipo * 11) % 64] > 0.975 and q > 0.25:
                return None
            c = memb(cara, int(s * (w - 1)), y, w, h)
            for k in dedos_ala(tipo):
                eje = dedo_ala(tipo, k, q)
                if eje is not None and abs(s - eje) < 1.3 / max(6, w):
                    return mezcla(c, PIEDRA[3], 0.7)          # los dedos de piedra
            # vetas de la piedra en la membrana (es piedra fina, como pizarra)
            if GRIETAS[(y * 2 + tipo * 9) % 64, (x * 2 + KC[cara]) % 64]:
                return oscuro(c, 0.3)
            if cara == "abajo":
                c = oscuro(c, 0.15)
            if q < 0.1:
                return oscuro(c, 0.3)
            return c

        return f

    def garra_ala(cara, x, y, w, h):
        return PIEDRA[5] if y <= 1 else PIEDRA[2]

    def punta(cara, x, y, w, h):
        # la pala de la cola: un rombo (lo de fuera, transparente)
        if cara in ("arriba", "abajo"):
            cx = (w - 1) / 2.0
            d = abs(x - cx) / max(1.0, cx)
            yy = 1.0 - y / max(1, h - 1)
            ancho = (yy / 0.45) if yy < 0.45 else (1.0 - (yy - 0.45) / 0.55)
            if d > ancho * 0.98:
                return None
            c = piedra(cara, x, y, w, h)
            return oscuro(c, 0.3) if d > ancho * 0.7 else c
        return None

    cuerpo = sombra(roca, abajo=0.16)
    return {
        "cadera": cuerpo, "torso": sombra(torso, abajo=0.14), "pecho": pecho, "pua": pua,
        "craneo": sombra(craneo, abajo=0.1), "hocico": hocico, "cejas": cejas, "oreja": oreja,
        "mandibula": mandibula, "colmillo": colmillo, "cuerno": cuerno, "cuerno2": cuerno2,
        "brazo": sombra(brazo, abajo=0.15), "antebrazo": sombra(brazo, abajo=0.18), "mano": mano, "garra": garra, "garra2": garra,
        "muslo": sombra(brazo, abajo=0.14), "pierna": sombra(brazo, abajo=0.16), "tarso": sombra(brazo, abajo=0.16),
        "pie": mano, "garra_pie": garra,
        "cola1": cuerpo, "cola2": cuerpo, "cola3": cuerpo, "punta": punta,
        "ala_hueso": ala_hueso, "ala_hueso2": ala_hueso, "ala_hueso3": ala_hueso, "garra_ala": garra_ala,
        "membrana1_d": membrana(1), "membrana2_d": membrana(2), "membrana3_d": membrana(3),
        "membrana1_i": membrana(1, True), "membrana2_i": membrana(2, True), "membrana3_i": membrana(3, True),
    }


def brillo(variante):
    """Lo que brilla (solo despierta): los ojos rojos, los ollares, las grietas del cuerpo por dentro y el fondo de la boca."""

    def grietas(cara, x, y, w, h):
        if cara == "abajo":
            return None
        g = GRIETAS[(y + 7 * KC[cara]) % 64, (x + 13 * KC[cara]) % 64]
        if g:
            return BRASA[3] if g == 2 else BRASA[2]
        return None

    def ojos(cara, x, y, w, h):
        if cara == "frente":
            if y in (2, 3) and x in (1, 2, w - 3, w - 2):
                return OJO[3] if (y == 2 and x in (2, w - 3)) else OJO[2] if y == 2 else OJO[1]
        return grietas(cara, x, y, w, h)

    def hocico_b(cara, x, y, w, h):
        if cara == "frente" and y == 1 and x in (1, w - 2):
            return BRASA[2]
        if cara == "abajo" and 1 <= x <= w - 2 and (x + y) % 3 == 0:
            return BRASA[1]
        return None

    def boca_b(cara, x, y, w, h):
        if cara == "arriba" and 1 <= x <= w - 2 and (x * 2 + y) % 4 == 0:
            return BRASA[1]
        return None

    return {
        "cadera": grietas, "torso": grietas, "craneo": ojos, "hocico": hocico_b, "mandibula": boca_b,
        "brazo": grietas, "antebrazo": grietas, "muslo": grietas, "pierna": grietas, "tarso": grietas,
        "cola1": grietas, "cola2": grietas,
    }


# ---------------------------------------------------------------- las alas

def girar(v, cabeceo):
    """Una direccion del sistema del dragon (tendido) al del torso de la gargola echado hacia delante `cabeceo` grados."""
    p = math.radians(cabeceo)
    x, y, z = v
    return (x, y * math.cos(p) + z * math.sin(p), -y * math.sin(p) + z * math.cos(p))


def ala(lado, *tramos):
    """Como `dragon.ala`, con estos nombres de piezas (ala, ala2, ala3)."""
    out = {}
    padre = np.eye(3)
    for k, (dh, df) in enumerate(tramos):
        mundo = _matriz(dh, df, lado)
        local = padre.T @ mundo
        out[f"{ALA[k]}_{lado}"] = tuple(round(v, 1) for v in _euler(local))
        padre = mundo
    return out


def alas(*tramos, cabeceo=0.0):
    """Las dos alas en la misma pose (la izquierda, en espejo); con cabeceo, las direcciones del dragon giradas al torso."""
    tr = [(girar(h, cabeceo), girar(f, cabeceo)) for h, f in tramos] if cabeceo else list(tramos)
    d = ala("d", *tr)
    i = ala("i", *[((-h[0], h[1], h[2]), (-f[0], f[1], f[2])) for h, f in tr])
    return {**d, **i}


# las poses de las alas de pie (derecha; el torso casi derecho: -y arriba, +z atras): plegadas como una capa por la espalda,
# entreabiertas (en guardia) y abiertas en alto (al gritar)
PLEGADAS = (((-0.40, -0.70, 0.60), (0.30, 0.20, 1.0)),     # el brazo: hacia arriba, fuera y atras
            ((-0.05, 0.98, 0.20), (0.40, 0.0, 1.0)),      # el antebrazo: baja por la espalda
            ((0.10, 0.95, 0.30), (0.40, 0.0, 1.0)))       # los dedos: hacia el suelo
ENTREABIERTAS = (((-0.70, -0.62, 0.35), (0.1, 0.2, 1.0)),
                 ((-0.45, 0.80, 0.40), (0.2, 0.1, 1.0)),
                 ((-0.25, 0.92, 0.30), (0.3, 0.1, 1.0)))
EN_ALTO = (((-0.55, -0.82, 0.15), (0, 0.1, 1)),
           ((-0.80, -0.45, 0.40), (0.1, 0.1, 1)),
           ((-0.70, 0.20, 0.68), (0.3, 0.3, 1)))
# las de vuelo, en el sistema del dragon (cuerpo tendido: -y arriba, +z atras); se giran al torso con el cabeceo del vuelo
V_ARRIBA = (((-0.62, -0.78, 0.10), (0, -0.1, 1)),
            ((-0.78, -0.50, 0.38), (0.1, 0.1, 1)),
            ((-0.70, 0.12, 0.70), (0.3, 0.3, 1)))
V_ABAJO = (((-0.80, 0.58, -0.10), (0, 0.15, 1)),
           ((-0.72, 0.62, 0.30), (0.1, 0.2, 1)),
           ((-0.45, 0.52, 0.72), (0.4, 0.4, 1)))
V_ABIERTAS = (((-0.97, -0.20, 0.10), (0, 0, 1)),
              ((-0.96, -0.06, 0.26), (0, 0, 1)),
              ((-0.78, 0.06, 0.62), (0.3, 0.05, 1)))
V_RECOGIDAS = (((-0.55, -0.30, 0.78), (0.2, 0.3, 1)),
               ((-0.20, -0.10, 0.97), (0.3, 0.3, 1)),
               ((0.05, 0.15, 0.98), (0.4, 0.4, 1)))
#: lo echado hacia delante que va el torso en vuelo (grados)
CABECEO_VUELO = 70.0
CABECEO_CERNE = 25.0


# ---------------------------------------------------------------- las poses

def patas(d=0.0, i=0.0, agacha=0.0):
    """Las piernas de garra: d / i hacia atras (+) o delante (-); agacha dobla las dos (muslo adelante, pierna atras)."""
    def una(a):
        return (-30 + a - agacha * 0.9, 0, 0), (55 + agacha * 1.2 + max(0.0, -a) * 0.4, 0, 0), (-25 - agacha * 0.4, 0, 0)

    md, pd, td = una(d)
    mi, pi_, ti = una(i)
    return {"muslo_d": md, "pierna_d": pd, "pata_d": td, "muslo_i": mi, "pierna_i": pi_, "pata_i": ti}


def brazos(d=(0, 0, 0), i=(0, 0, 0), codo_d=0.0, codo_i=0.0):
    return {"brazo_d": d, "brazo_i": (i[0], -i[1], -i[2]), "antebrazo_d": (-codo_d, 0, 0), "antebrazo_i": (-codo_i, 0, 0)}


def cola(x=0.0, y=0.0, ola=0.0, fase=0.0):
    out = {}
    for k in range(3):
        out[f"cola{k + 1}"] = (x * (0.3 + 0.1 * k), y * (0.3 + 0.1 * k) + ola * math.sin(fase - k * 0.9), 0.0)
    return out


def cabeza(x=0.0, y=0.0, boca=0.0):
    return {"cabeza": (x, y, 0.0), "mandibula": (boca, 0.0, 0.0)}


# de pie: encorvada, la cabeza adelantada, los brazos colgando delante, las piernas dobladas
BASE = con(alas(*PLEGADAS), raiz=raiz(-2.0), cadera=(10, 0, 0), torso=(22, 0, 0), **cabeza(-26, 0, 4),
           **brazos((-18, 0, 8), (-18, 0, 8), 24, 24), **patas(agacha=10), **cola(-8, 0))
LISTO = con(BASE, alas(*ENTREABIERTAS), raiz=raiz(-3.0), torso=(30, 0, 0), **cabeza(-30, 0, 12),
            **brazos((-40, -10, 22), (-40, -10, 22), 40, 40), **patas(agacha=18), **cola(6, 0))
# la estatua: agachada sobre el pedestal, las alas envolviendola, las manos en las rodillas, la cabeza baja
ESTATUA = con(alas(*mezcla_alas(PLEGADAS, ENTREABIERTAS, 0.15)), raiz=raiz(-6.0), cadera=(32, 0, 0), torso=(26, 0, 0),
              **cabeza(4, 0, 0), **brazos((-48, 0, 12), (-48, 0, 12), 70, 70), **patas(agacha=46), **cola(-30, 22))
# en vuelo: tendida, las piernas recogidas atras, la cola estirada
VUELO = con({}, raiz=raiz(0.0), cadera=(0, 0, 0), torso=(CABECEO_VUELO, 0, 0), **cabeza(-62, 0, 6),
            **brazos((-70, 0, 14), (-70, 0, 14), 30, 30), muslo_d=(60, 0, 0), pierna_d=(40, 0, 0), pata_d=(30, 0, 0),
            muslo_i=(60, 0, 0), pierna_i=(40, 0, 0), pata_i=(30, 0, 0), **cola(70, 0))
# cerniendose: casi derecha, las piernas colgando con las garras abiertas
CERNE = con({}, raiz=raiz(0.0), cadera=(0, 0, 0), torso=(CABECEO_CERNE, 0, 0), **cabeza(-20, 0, 10),
            **brazos((-30, 0, 18), (-30, 0, 18), 40, 40), **patas(d=-12, i=8, agacha=4), **cola(18, 0))


def animaciones(r=None):
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses):
        a = Anim(nombre, largo, bucle=True).poses(poses, r)
        B.append(a)
        return a

    def accion(nombre, largo, poses, golpes=()):
        a = Anim(nombre, largo, golpes=golpes).poses(poses, r)
        A.append(a)
        return a

    # ================================================================ bucles
    # ---- posada: la estatua, quieta del todo
    bucle("posada", 4.0, [(0.0, ESTATUA), (2.0, ESTATUA), (4.0, ESTATUA)])

    # ---- quieto: respira, mira a un lado y a otro, la cola se mece
    def quieto(t):
        f = 2 * math.pi * t / 4.0
        return con(BASE, torso=(22 - 2 * math.sin(2 * f), 0, 0), **cabeza(-26 + 4 * math.sin(f), 18 * math.sin(f), 4 + 3 * math.sin(2 * f)),
                   **cola(-8, 0, ola=8, fase=f))
    bucle("quieto", 4.0, [(k * 0.5, quieto(k * 0.5)) for k in range(9)])

    # ---- en guardia: agachada, las alas entreabiertas que tiemblan, la cola que azota
    def listo(t):
        f = 2 * math.pi * t / 2.0
        return con(LISTO, alas(*mezcla_alas(ENTREABIERTAS, EN_ALTO, 0.12 + 0.08 * math.sin(f))),
                   **cabeza(-30 + 4 * math.sin(f), 8 * math.sin(f / 2), 12 + 6 * max(0.0, math.sin(f))),
                   **cola(6, 0, ola=14, fase=2 * f))
    bucle("listo", 2.0, [(k * 0.25, listo(k * 0.25)) for k in range(9)])

    # ---- andar: a saltitos encorvada, apoyandose a veces en los nudillos
    T = 1.2

    def andar(t):
        f = 2 * math.pi * t / T
        a = math.sin(f)
        return con(LISTO, raiz=raiz(-3.0 + 1.2 * abs(math.cos(f))), torso=(34, 6 * a, 4 * a),
                   **patas(d=-26 * a, i=26 * a, agacha=14), **brazos((-40 + 22 * a, -10, 20), (-40 - 22 * a, -10, 20), 40, 40),
                   **cabeza(-34, -6 * a, 10), **cola(4, 10 * a, ola=8, fase=f))
    bucle("andar", T, [(k * T / 8, andar(k * T / 8)) for k in range(9)])

    # ---- vuelo: tendida, aleteo fuerte (los dedos van detras)
    AL = 0.7

    def vuelo(t):
        f = 2 * math.pi * t / AL
        k1 = 0.5 - 0.5 * math.cos(f)
        k2 = 0.5 - 0.5 * math.cos(f - 0.5)
        k3 = 0.5 - 0.5 * math.cos(f - 1.0)
        tr = [mezcla_alas(V_ARRIBA, V_ABAJO, k)[i] for i, k in enumerate((k1, k2, k3))]
        return con(VUELO, alas(*tr, cabeceo=CABECEO_VUELO), raiz=raiz(1.4 * math.cos(f)), **cola(70 - 6 * math.cos(f + 0.6), 0, ola=6, fase=f))
    bucle("vuelo", AL, [(k * AL / 8, vuelo(k * AL / 8)) for k in range(9)])

    # ---- cerniendose: casi derecha, aletea fuerte para quedarse en el sitio
    CE = 0.6

    def cerne(t):
        f = 2 * math.pi * t / CE
        k1 = 0.5 - 0.5 * math.cos(f)
        k2 = 0.5 - 0.5 * math.cos(f - 0.4)
        k3 = 0.5 - 0.5 * math.cos(f - 0.8)
        tr = [mezcla_alas(V_ARRIBA, V_ABAJO, k)[i] for i, k in enumerate((k1, k2, k3))]
        return con(CERNE, alas(*tr, cabeceo=CABECEO_CERNE), raiz=raiz(1.6 * math.cos(f)), **cola(18, 0, ola=10, fase=f))
    bucle("cerne", CE, [(k * CE / 8, cerne(k * CE / 8)) for k in range(9)])

    # ================================================================ acciones
    GRITA = con(LISTO, alas(*EN_ALTO), raiz=raiz(-1.0), torso=(-6, 0, 0), **cabeza(-40, 0, 40),
                **brazos((-60, -40, 50), (-60, -40, 50), 30, 30), **cola(24, 0))
    # ---- despierta: la estatua se agrieta (temblando), alza la cabeza, abre las alas y grita
    accion("despierta", 2.2, [
        (0.00, ESTATUA),
        (0.35, con(ESTATUA, torso=(26, 3, 0), cabeza=(6, 4, 0))),
        (0.55, con(ESTATUA, torso=(26, -3, 0), cabeza=(2, -4, 0))),
        (0.80, con(ESTATUA, torso=(20, 0, 0), **cabeza(-20, 0, 8))),
        (1.15, con(LISTO, alas(*ENTREABIERTAS), raiz=raiz(-5.0), **patas(agacha=30))),
        (1.45, GRITA),
        (1.80, con(GRITA, cabeza=(-36, 12, 0), mandibula=(42, 0, 0))),
        (2.20, LISTO),
    ])
    # ---- el grito (en tierra o al empezar a pelear): las alas en alto y la cabeza atras
    accion("grito", 1.4, [
        (0.00, LISTO),
        (0.35, con(LISTO, raiz=raiz(-4.0), torso=(40, 0, 0), **cabeza(-10, 0, 4))),
        (0.60, GRITA),
        (1.00, con(GRITA, cabeza=(-36, -10, 0), mandibula=(38, 0, 0))),
        (1.40, LISTO),
    ])
    # ---- el zarpazo: dos garrazos, derecha e izquierda (golpes a los 0,45 y 0,85 s)
    DER_ARRIBA = con(LISTO, torso=(20, 22, 6), brazo_d=(-150, -10, -40), antebrazo_d=(-50, 0, 0), **cabeza(-30, -18, 20))
    DER_BARRE = con(LISTO, torso=(38, -20, -4), brazo_d=(-50, 40, 40), antebrazo_d=(-10, 0, 0), **cabeza(-34, 16, 24))
    IZQ_ARRIBA = con(DER_BARRE, brazo_i=(-150, 10, 40), antebrazo_i=(-50, 0, 0))
    IZQ_BARRE = con(LISTO, torso=(38, 20, 4), brazo_d=(-40, 0, 20), brazo_i=(-50, -40, -40), antebrazo_i=(-10, 0, 0), **cabeza(-34, -16, 24))
    accion("zarpazo", 1.3, [
        (0.00, LISTO),
        (0.30, DER_ARRIBA),
        (0.45, DER_BARRE),
        (0.68, IZQ_ARRIBA),
        (0.85, IZQ_BARRE),
        (1.30, LISTO),
    ], golpes=(0.45, 0.85))
    # ---- el coletazo: se da la vuelta y barre con la cola (golpe a los 0,6 s)
    accion("coletazo", 1.2, [
        (0.00, LISTO),
        (0.35, con(LISTO, raiz=raiz(-3.0, rot=(0, -60, 0)), **cola(10, 40), **cabeza(-30, 30, 10))),
        (0.60, con(LISTO, raiz=raiz(-3.0, rot=(0, 140, 0)), **cola(14, -50), **cabeza(-30, -20, 16))),
        (0.80, con(LISTO, raiz=raiz(-3.0, rot=(0, 220, 0)), **cola(10, -30))),
        (1.20, con(LISTO, raiz=raiz(-3.0, rot=(0, 360, 0)))),
    ], golpes=(0.60,))
    # ---- despega: se agacha y salta con un aletazo (deja el suelo en el tick 9)
    accion("despega", 0.9, [
        (0.00, LISTO),
        (0.35, con(LISTO, alas(*EN_ALTO), raiz=raiz(-6.0), **patas(agacha=44))),
        (0.50, con(CERNE, alas(*V_ABAJO, cabeceo=CABECEO_CERNE), raiz=raiz(2.0))),
        (0.70, con(CERNE, alas(*V_ARRIBA, cabeceo=CABECEO_CERNE))),
        (0.90, cerne(0.0)),
    ])
    # ---- se posa: llega cerniendose con las garras por delante, cae (golpe a los 0,2 s: el ruido) y pliega las alas
    accion("aterriza", 0.8, [
        (0.00, con(CERNE, alas(*V_ABIERTAS, cabeceo=CABECEO_CERNE), **patas(d=-30, i=-30))),
        (0.20, con(LISTO, alas(*EN_ALTO), raiz=raiz(-6.0), **patas(agacha=40))),
        (0.50, con(LISTO, alas(*ENTREABIERTAS), raiz=raiz(-4.0))),
        (0.80, LISTO),
    ], golpes=(0.20,))
    # ---- el picado (va sobre el vuelo): se prepara cerniendose con las alas en alto y se lanza con ellas recogidas y las garras
    # por delante
    PICA = con(VUELO, alas(*V_RECOGIDAS, cabeceo=CABECEO_VUELO), **brazos((-100, 0, 10), (-100, 0, 10), 10, 10),
               muslo_d=(-40, 0, 0), pierna_d=(30, 0, 0), muslo_i=(-40, 0, 0), pierna_i=(30, 0, 0), **cabeza(-70, 0, 30))
    accion("picado", 1.6, [
        (0.00, cerne(0.0)),
        (0.30, con(CERNE, alas(*V_ARRIBA, cabeceo=CABECEO_CERNE), **cabeza(-30, 0, 34))),
        (0.50, con(CERNE, alas(*EN_ALTO), raiz=raiz(1.0), **cabeza(-34, 0, 40))),
        (0.70, PICA),
        (1.40, PICA),
        (1.60, vuelo(0.0)),
    ])
    # ---- las garras en el aire: cerniendose, se echa atras y lanza las patas por delante (golpe a los 0,55 s)
    accion("garras", 1.0, [
        (0.00, cerne(0.0)),
        (0.35, con(CERNE, alas(*V_ARRIBA, cabeceo=CABECEO_CERNE), torso=(5, 0, 0), **patas(d=-50, i=-50, agacha=20), **cabeza(-20, 0, 30))),
        (0.55, con(CERNE, alas(*V_ABAJO, cabeceo=CABECEO_CERNE), torso=(35, 0, 0), muslo_d=(-90, 0, 0), pierna_d=(10, 0, 0), pata_d=(0, 0, 0),
                   muslo_i=(-90, 0, 0), pierna_i=(10, 0, 0), pata_i=(0, 0, 0), **brazos((-90, 20, 30), (-90, 20, 30), 10, 10), **cabeza(-40, 0, 36))),
        (0.75, con(CERNE, alas(*V_ARRIBA, cabeceo=CABECEO_CERNE))),
        (1.00, cerne(0.0)),
    ], golpes=(0.55,))
    # ---- aturdida: se encoge, sacude la cabeza
    accion("aturdido", 1.5, [
        (0.00, LISTO),
        (0.25, con(BASE, alas(*PLEGADAS), raiz=raiz(-6.0), torso=(50, 0, 20), **cabeza(20, 20, 20), **patas(agacha=40))),
        (0.70, con(BASE, alas(*PLEGADAS), raiz=raiz(-6.0), torso=(50, 0, -16), **cabeza(20, -20, 10), **patas(agacha=40))),
        (1.10, con(LISTO, raiz=raiz(-5.0))),
        (1.50, LISTO),
    ])
    # ---- morir: se le parte la piedra, cae de rodillas con las alas caidas y se queda hecha una estatua rota
    ROTA = con(ESTATUA, alas(*mezcla_alas(PLEGADAS, ENTREABIERTAS, 0.6)), raiz=raiz(-8.0, rot=(0, 0, 14)), torso=(48, 0, 10),
               **cabeza(40, 10, 20), **brazos((-10, 0, 30), (-10, 0, 30), 10, 10), **cola(-40, 30))
    accion("morir", 2.0, [
        (0.00, LISTO),
        (0.30, con(GRITA, mandibula=(50, 0, 0))),
        (0.70, con(LISTO, raiz=raiz(-6.0), torso=(44, 10, 10), **cabeza(-10, 20, 30))),
        (1.20, ROTA),
        (2.00, ROTA),
    ])
    return B, A


# lo que ademas de las acciones necesita el servidor
TABLA_EXTRA = (
    "",
    "    /** El despegue: el tick en que deja el suelo. */",
    "    public static final int DESPEGA_SALTO = 9;",
    "    /** El picado: de que tick a que tick se lanza (lo de antes es prepararse). */",
    "    public static final int PICADO_DESDE = 12, PICADO_HASTA = 30;",
)


def base_de(nombre, por_nombre):
    """Para las hojas de revision: el picado y las garras van sobre el vuelo / cernerse."""
    return []
