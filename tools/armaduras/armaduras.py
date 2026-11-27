#!/usr/bin/env python3
"""
Las armaduras (parte 7ai): conjuntos con modelo propio, como los de Elden Ring (yelmo, peto, guantes y grebas). Cada
conjunto es un modelo con las siete piezas del jugador de Minecraft (head, hat, body, right_arm, left_arm, right_leg,
left_leg, en sus sitios de siempre) y encima sus cajas, cada una de una de las cuatro piezas de la armadura. Sale:

  - la textura de cada conjunto        textures/models/armor/<conjunto>.png
  - el icono de cada pieza (32 x 32)  textures/item/<conjunto>_<pieza>.png  y  models/item/<conjunto>_<pieza>.json
  - el codigo Java de la geometria     client/armadura/ArmaduraGeometria.java (un LayerDefinition por conjunto y pieza)

y con --vistas, hojas para revisarlo todo sin abrir el juego (el conjunto puesto en un maniqui con la piel de Steve, de
frente, de espaldas, de lado y andando; los iconos en grande; y todos juntos).

    python3 tools/armaduras/armaduras.py [--vistas carpeta] [--solo caballero]

Los guantes van en el hueco de los PIES de Minecraft (no hay hueco de manos): su modelo pone las cajas en los brazos, y
las grebas llevan tambien las botas. Coordenadas como los modelos de Minecraft (pixeles, Y hacia abajo, el frente a -Z,
el lado derecho del personaje a -X); cada caja en el espacio de su pieza (la cabeza con el cuello en 0; el brazo derecho
colgado de (-5, 2, 0), de -2 a 10 en Y; la pierna derecha de (-1.9, 12, 0), de 0 a 12).

Necesita numpy y Pillow (solo para generar; el juego no).
"""
import argparse
import json
import math
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(AQUI, "..", "enemigos"))

import vista  # noqa: E402
from modelo import Box, Rig, fmt  # noqa: E402
from pinta import Textura, claro, delante, hexa, mezcla, oscuro, tono  # noqa: E402

REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
RES = os.path.join(REPO, "src/main/resources/assets/darkcraft")
TEX_DIR = os.path.join(RES, "textures/models/armor")
ICO_DIR = os.path.join(RES, "textures/item")
MOD_DIR = os.path.join(RES, "models/item")
JAVA = os.path.join(REPO, "src/main/java/com/darkcraft/client/armadura/ArmaduraGeometria.java")
HERRAMIENTA = "tools/armaduras/armaduras.py"
STEVE = "/tmp/cp/cpjars/client-extra.jar"

PIEZAS = ("yelmo", "peto", "guantes", "grebas")
#: las siete piezas del modelo del jugador y donde cuelgan
PARTES = (("head", (0, 0, 0)), ("hat", (0, 0, 0)), ("body", (0, 0, 0)), ("right_arm", (-5, 2, 0)), ("left_arm", (5, 2, 0)),
          ("right_leg", (-1.9, 12, 0)), ("left_leg", (1.9, 12, 0)))
LADOS = (("right", -1), ("left", 1))


def esp(x0, w):
    """La x de una caja del lado derecho, llevada al izquierdo (espejo en X)."""
    return -(x0 + w)


# ---------------------------------------------------------------- el conjunto: un Rig con las cajas de cada pieza

class Conjunto:
    def __init__(self, nombre, ancho=128):
        self.nombre = nombre
        self.rig = Rig(nombre, ancho)
        for n, p in PARTES:
            self.rig.pieza(n, "", p)
        self.de = {}           # id(caja) -> pieza
        self.parte_pieza = {}  # pieza hija -> pieza de la armadura (las piezas con giro)
        self.mats = {}

    def caja(self, pieza, parte, x, y, z, w, h, d, mat, grow=0.0, uv=""):
        if pieza not in PIEZAS:
            raise SystemExit(f"{self.nombre}: pieza desconocida {pieza}")
        for v in (w, h, d):
            if abs(v - round(v)) > 1e-9:
                raise SystemExit(f"{self.nombre}/{mat}: medidas enteras (la textura de Minecraft va por pixeles enteros): {w} {h} {d}")
        gr = (grow, grow, grow) if isinstance(grow, (int, float)) else tuple(grow)
        b = Box(x, y, z, w, h, d, mat, grow=gr, uv=uv)
        self.rig.por_nombre[parte].boxes.append(b)
        self.de[id(b)] = pieza
        return b

    def lados(self, pieza, base, x, y, z, w, h, d, mat, grow=0.0, uv=""):
        """La misma caja en los dos brazos o las dos piernas (base = "arm" o "leg"; x, w de la del lado derecho)."""
        self.caja(pieza, f"right_{base}", x, y, z, w, h, d, f"{mat}_d", grow, uv)
        self.caja(pieza, f"left_{base}", esp(x, w), y, z, w, h, d, f"{mat}_i", grow, uv)

    def hija(self, pieza, nombre, madre, pivote, rot=(0.0, 0.0, 0.0)):
        """Una pieza con giro colgada de otra (cuernos, plumas...)."""
        self.rig.pieza(nombre, madre, pivote, rot)
        self.parte_pieza[nombre] = pieza
        return nombre

    def cajas_de(self, pieza):
        return [(p, b) for p, b in self.rig.cajas() if self.de[id(b)] == pieza]


# ---------------------------------------------------------------- pintar

def ruido_t(semilla):
    return np.random.default_rng(semilla).random((128, 128))


class Pintor:
    """Ayudas para pintar una cara: f(cara, x, y, w, h) -> color o None."""

    @staticmethod
    def placa(tonos, semilla, grano=0.05, canto=1.0, rayas=0, veta=True):
        """
        Acero en pixel art: cada cara con su luz de arriba a la izquierda (el canto de arriba claro, el de la derecha y el
        de abajo oscuros, como los contornos de la armadura de hierro de Minecraft), un brillo a lo alto en el primer
        tercio (lo curvo) y muy poco grano.
        """
        r = ruido_t(semilla)
        k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

        def f(cara, x, y, w, h):
            n = (r[(y + 9 * k[cara]) % 128, (x + 17 * k[cara]) % 128] - 0.5) * grano
            if cara == "arriba":
                v = 0.78 - 0.18 * abs(y - (h - 1) / 2) / max(1, h / 2) + n
                if x == w - 1 or y == h - 1:
                    v -= 0.2 * canto
                return tono(tonos, v)
            if cara == "abajo":
                return tono(tonos, 0.12 + n)
            v = 0.56 + 0.14 * (0.5 - y / max(1, h - 1)) + n
            if y == 0:
                v += 0.2 * canto
            if x == 0:
                v += 0.1 * canto
            if x == w - 1:
                v -= 0.22 * canto
            if y == h - 1:
                v -= 0.28 * canto
            if veta and w >= 4 and x == max(1, w // 3) and 0 < y < h - 1:
                v += 0.14
            if rayas and (x * 3 + y * 5 + semilla) % rayas == 0 and 0 < x < w - 1 and 0 < y < h - 1:
                v += 0.1
            return tono(tonos, v)

        return f

    @staticmethod
    def malla(tonos, semilla=0):
        """Cota de malla: anillas a tresbolillo."""
        def f(cara, x, y, w, h):
            v = 0.62 if (x + y) % 2 == 0 else 0.3
            if (x * 5 + y * 3 + semilla) % 11 == 0:
                v += 0.2
            if cara == "abajo":
                v -= 0.25
            return tono(tonos, v - 0.25 * (y / max(1, h - 1)) ** 2)
        return f

    @staticmethod
    def cuero(tonos, semilla, grano=0.38):
        r = ruido_t(semilla)
        k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

        def f(cara, x, y, w, h):
            v = 0.5 + (r[(y + 7 * k[cara]) % 128, (x + 13 * k[cara]) % 128] - 0.5) * grano + 0.12 * (0.5 - y / max(1, h - 1))
            if cara == "abajo":
                v -= 0.25
            return tono(tonos, v)
        return f

    @staticmethod
    def tela(tonos, semilla, pliegue=3, grano=0.12):
        r = ruido_t(semilla)
        k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

        def f(cara, x, y, w, h):
            v = 0.55 + 0.18 * (0.5 - y / max(1, h - 1))
            if cara in ("arriba", "abajo"):
                v += 0.1 if cara == "arriba" else -0.25
            else:
                q = (x + (y // 4) % 2 + k[cara]) % pliegue
                v += -0.17 if q == 0 else (0.08 if q == 1 else 0)
            v += (r[(y + 5 * k[cara]) % 128, (x + 11 * k[cara]) % 128] - 0.5) * grano
            return tono(tonos, v)
        return f

    @staticmethod
    def pelo(tonos, semilla, largo=3):
        """Piel de animal: mechones a lo alto, con las puntas claras."""
        r = ruido_t(semilla)

        def f(cara, x, y, w, h):
            col = r[3, (x * 7 + 3) % 128]
            fase = int(col * largo)
            q = (y + fase) % largo
            v = 0.35 + 0.35 * (q / max(1, largo - 1)) + (r[(y * 3 + x) % 128, (x * 5 + 7) % 128] - 0.5) * 0.3
            if cara == "abajo":
                v -= 0.2
            return tono(tonos, v)
        return f

    @staticmethod
    def liso(c):
        return lambda cara, x, y, w, h: c

    @staticmethod
    def nada():
        return lambda cara, x, y, w, h: None


def borde(f, color, caras=("frente", "espalda", "der", "izq"), arriba=1, abajo=1, lados=0):
    """Un ribete (de `color`) en los cantos de arriba/abajo (y de los lados) de las caras dadas."""
    def g(cara, x, y, w, h):
        if cara in caras and (y < arriba or y >= h - abajo or x < lados or x >= w - lados):
            return color(cara, x, y, w, h) if callable(color) else color
        return f(cara, x, y, w, h)
    return g


def sombra(f, abajo=0.2, cantos=0.08):
    """Oscurece hacia abajo y en los cantos de cada cara (volumen)."""
    def g(cara, x, y, w, h):
        c = f(cara, x, y, w, h)
        if c is None:
            return None
        c = hexa(c)
        if cara == "abajo":
            return oscuro(c, abajo + 0.1)
        if cara == "arriba":
            return claro(c, 0.05)
        t = abajo * (y / max(1, h - 1)) ** 1.6
        if x == 0 or x == w - 1:
            t += cantos
        return oscuro(c, min(0.8, t))
    return g


def caras(base, **otras):
    """Otro pintor en las caras nombradas (frente=, espalda=, der=, izq=, arriba=, abajo=, lados=, tapas=)."""
    m = {}
    for k, v in otras.items():
        if k == "lados":
            m["der"] = m["izq"] = v
        elif k == "tapas":
            m["arriba"] = m["abajo"] = v
        else:
            m[k] = v
    return lambda cara, x, y, w, h: m.get(cara, base)(cara, x, y, w, h)


def dibujo(filas, paleta, resto=None):
    """Una cara dibujada con letras ('.' = lo de `resto` o transparente; ' ' = transparente)."""
    def f(cara, x, y, w, h):
        ch = filas[y][x] if y < len(filas) and x < len(filas[y]) else "."
        if ch == ".":
            return resto(cara, x, y, w, h) if resto else None
        if ch == " ":
            return None
        return paleta[ch]
    return f


# ---------------------------------------------------------------- los conjuntos

CONJUNTOS = {}


def conjunto(fn):
    CONJUNTOS[fn.__name__] = fn
    return fn


ACERO = ["#121417", "#1f2327", "#30363c", "#475057", "#636d75", "#87919a", "#b6bec4"]
ACERO_NEGRO = ["#0a0a0d", "#131317", "#1c1c22", "#28282f", "#36363f", "#4a4a55", "#66667a"]
ORO = ["#4a3a12", "#6c561e", "#917630", "#b69444", "#d6b25c"]
PLATA = ["#4d5058", "#6b6f78", "#8c9099", "#adb1b9", "#cfd2d8"]
CUERO_OSCURO = ["#120d0a", "#1d1510", "#291e16", "#36281d", "#443326"]
CUERO = ["#2a1d13", "#3a2819", "#4c3521", "#5f442b", "#735435"]
MALLA = ["#1f2226", "#2f3439", "#454c53", "#5f676f", "#7c858d"]
ROJO = ["#300a0c", "#4a1015", "#64161d", "#7f1d26", "#9b2730", "#b8343c"]
PURPURA = ["#1a0c22", "#271233", "#361946", "#46205a", "#57296e"]
HUESO = ["#6d6350", "#8a7f67", "#a89c80", "#c4b99c", "#ddd3b8"]


def _remaches(f, color, cada=3, caras_=("frente", "espalda", "der", "izq"), fila=0, desde=1):
    """Remaches en la fila `fila` (contada desde arriba) cada `cada` pixeles."""
    def g(cara, x, y, w, h):
        if cara in caras_ and y == fila and (x - desde) % cada == 0 and 0 < x < w - 1:
            return color
        return f(cara, x, y, w, h)
    return g


@conjunto
def caballero(paleta="caballero"):
    """
    El caballero de Valdrenn: yelmo cerrado con rendija y penacho rojo, coraza con el tabardo rojo y su corona de oro,
    hombreras de dos lamas, guanteletes con su puno y grebas con rodillera, escarcela de lamas y punteras.
    """
    oscura = paleta == "confesor"
    A = ACERO_NEGRO if oscura else ACERO
    TAB = PURPURA if oscura else ROJO
    BORDE = ORO
    c = Conjunto(paleta)
    # ---- yelmo
    c.caja("yelmo", "head", -4, -8, -4, 8, 8, 8, "yelmo", 0.7)
    c.caja("yelmo", "head", -4, -1, -4, 8, 1, 8, "gola", 1.05)
    if not oscura:
        c.caja("yelmo", "head", -1, -11, -3, 2, 3, 7, "penacho")
        c.caja("yelmo", "head", -1, -10, 4, 2, 5, 1, "penacho_cola")
    else:
        # el confesor: la capucha sobre el yelmo y la esclavina por los hombros (van con el yelmo)
        c.caja("yelmo", "head", -4, -8, -4, 8, 8, 8, "capucha", 1.0)
        c.caja("yelmo", "body", -4, -1, -2, 8, 3, 4, "esclavina", 1.1)
        p = c.hija("yelmo", "capucha_pico", "head", (0, -7.5, 4.6), (28, 0, 0))
        c.caja("yelmo", p, -2, 0, 0, 4, 5, 1, "capucha_pico")
    # ---- peto
    c.caja("peto", "body", -4, 0, -2, 8, 12, 4, "coraza", 0.6)
    c.caja("peto", "body", -3, 1, -3, 6, 9, 1, "tabardo", (0.05, 0.05, 0.0))
    for lado, s in LADOS:
        x0 = -3.6 if s < 0 else esp(-3.6, 5)
        c.caja("peto", f"{lado}_arm", x0, -3, -2.5, 5, 3, 5, f"hombrera_{lado}", 0.25)
        x1 = -3.6 if s < 0 else esp(-3.6, 5)
        c.caja("peto", f"{lado}_arm", x1, 0, -2.5, 5, 2, 5, f"hombrera2_{lado}", 0.05)
        c.caja("peto", f"{lado}_arm", -3 if s < 0 else -1, -2, -2, 4, 7, 4, f"brazal_{lado}", 0.45)
    # ---- guantes (en el hueco de los pies)
    for lado, s in LADOS:
        c.caja("guantes", f"{lado}_arm", -3 if s < 0 else -1, 4, -2, 4, 6, 4, f"guantelete_{lado}", 0.62)
        c.caja("guantes", f"{lado}_arm", -3 if s < 0 else -1, 4, -2, 4, 1, 4, f"puno_{lado}", 0.95)
        c.caja("guantes", f"{lado}_arm", 0 if s < 0 else -1, 7, -3, 1, 2, 1, f"pulgar_{lado}", 0.3)
    # ---- grebas
    c.caja("grebas", "body", -4, 10, -2, 8, 3, 4, "escarcela", 0.82)
    for lado, s in LADOS:
        c.caja("grebas", f"{lado}_leg", -2, 0, -2, 4, 6, 4, f"quijote_{lado}", 0.5)
        c.caja("grebas", f"{lado}_leg", -1, 5, -3, 2, 2, 1, f"rodillera_{lado}", 0.25)
        c.caja("grebas", f"{lado}_leg", -2, 6, -2, 4, 6, 4, f"greba_{lado}", 0.56)
        c.caja("grebas", f"{lado}_leg", -2, 11, -3, 4, 1, 1, f"puntera_{lado}", 0.3)

    # ---- pintar
    pl = Pintor.placa(A[1:7], 11, rayas=19)
    pl2 = Pintor.placa(A[0:6], 12)
    tab = Pintor.tela(TAB, 13)

    def yelmo(cara, x, y, w, h):
        col = pl(cara, x, y, w, h)
        if cara == "frente":
            if y == 3 and 1 <= x <= 6:
                return "#060608"                        # la rendija
            if y == 2 and 1 <= x <= 6:
                return tono(A, 0.82)                    # el canto de encima de la rendija
            if y == 4 and 1 <= x <= 6:
                return tono(A, 0.25)
            if x in (3, 4) and y != 3:
                return tono(A, 0.75 if x == 3 else 0.5)   # la arista del frente
            if y >= 5 and x in (1, 6) and y % 2 == 1:
                return "#08080a"                        # respiraderos
        if cara in ("der", "izq") and y == 3 and 2 <= delante(cara, x, w) <= 3:
            return "#060608"
        if cara != "abajo" and y == h - 1:
            return tono(BORDE, 0.7) if (x % 3 == 1) else tono(A, 0.18)
        if oscura and cara in ("frente", "der", "izq", "espalda") and y == 1:
            return tono(BORDE, 0.4)
        return col

    def gola(cara, x, y, w, h):
        if cara == "arriba":
            return None
        return pl2(cara, x, y, w, h)

    def penacho(cara, x, y, w, h):
        r = (x * 7 + y * 13) % 5
        v = 0.75 - 0.18 * y / max(1, h - 1) + (0.1 if r == 0 else -0.06 if r == 3 else 0)
        return tono(ROJO, v)

    def capucha(cara, x, y, w, h):
        # la capucha del confesor: tela oscura; delante, la cara del yelmo al aire
        if cara == "frente" and 1 <= x <= 6 and y >= 1:
            return None
        if cara == "abajo":
            return None
        t = Pintor.tela(["#0b0a0e", "#141219", "#1d1a24", "#28232f", "#332d3c"], 15, pliegue=2)(cara, x, y, w, h)
        if cara == "frente" and (x in (0, 7) or y == 0):
            return tono(["#0b0a0e", "#141219", "#1d1a24", "#28232f", "#332d3c"], 0.15)
        return t

    def esclavina(cara, x, y, w, h):
        if cara == "abajo":
            return None
        t = Pintor.tela(["#0b0a0e", "#141219", "#1d1a24", "#28232f", "#332d3c"], 16, pliegue=2)(cara, x, y, w, h)
        if y == h - 1 and cara != "arriba":
            return tono(BORDE, 0.45) if x % 3 != 1 else None
        return t

    def coraza(cara, x, y, w, h):
        col = pl(cara, x, y, w, h)
        if cara == "arriba":
            return None                                  # el cuello: no tapa la barbilla
        if cara in ("frente", "espalda"):
            if y == 0:
                return tono(BORDE, 0.5)
            if cara == "espalda" and 1 <= x <= 6 and 1 <= y <= 9:
                v = tab(cara, x, y, w, h)
                return tono(BORDE, 0.45) if x in (1, 6) else v
            if y in (10, 11):
                return tono(A, 0.35 if y == 10 else 0.2)
        if cara in ("der", "izq") and y in (4, 8):
            return tono(A, 0.25)                        # las lamas del costado
        return col

    corona = ["..O..O..", ".OOOOOO.", ".OGOOGO."]

    def tabardo(cara, x, y, w, h):
        if cara != "frente":
            return None if cara in ("arriba", "abajo", "espalda") else tono(TAB, 0.3)
        v = tab(cara, x, y, w, h)
        if x in (0, w - 1) or y == h - 1:
            return tono(BORDE, 0.55 if (x + y) % 2 else 0.4)
        if 1 <= y <= 3:
            ch = corona[y - 1][x + 1] if x + 1 < 8 else "."
            if ch == "O":
                return tono(ORO, 0.85) if not oscura else tono(PLATA, 0.8)
            if ch == "G":
                return "#b81b22" if not oscura else "#7f2fae"
        if y >= h - 3 and x in (2, 3):
            return tono(TAB, 0.2)
        return v

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return tono(A, 0.12)
        col = pl(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return tono(A, 0.22)
        if cara != "arriba" and y == 1 and x % 2 == 1 and 0 < x < w - 1:
            return tono(BORDE, 0.75)                    # los remaches de oro
        return col

    def hombrera2(cara, x, y, w, h):
        if cara in ("abajo", "arriba"):
            return tono(A, 0.2) if cara == "abajo" else None
        return pl2(cara, x, y, w, h) if y < h - 1 else tono(A, 0.2)

    def brazal(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if y == h - 1:
            return tono(A, 0.75)                         # el codal
        return pl2(cara, x, y, w, h)

    def guantelete(cara, x, y, w, h):
        if cara == "arriba":
            return None
        col = pl(cara, x, y, w, h)
        if y >= 4:
            # los dedos: lamas
            return tono(A, 0.6 if (y + x // 2) % 2 == 0 else 0.35) if cara != "abajo" else tono(A, 0.2)
        if y == 3:
            return tono(A, 0.2)
        return col

    def puno(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if x % 3 == 1:
            return tono(BORDE, 0.7) if not oscura else tono(A, 0.85)
        return tono(A, 0.62 if x % 3 == 0 else 0.4)

    def escarcela(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if cara == "arriba" else tono(A, 0.15)
        col = pl2(cara, x, y, w, h)
        if y == 0:
            return tono(CUERO, 0.6) if not (cara == "frente" and x in (3, 4)) else tono(ORO, 0.8)
        if y == h - 1:
            return tono(A, 0.3)
        return col

    def quijote(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        return pl2(cara, x, y, w, h)

    def rodillera(cara, x, y, w, h):
        return tono(A, 0.85) if cara == "frente" and (x + y) % 3 == 0 else pl(cara, x, y, w, h)

    def greba(cara, x, y, w, h):
        col = pl(cara, x, y, w, h)
        if cara == "arriba":
            return None
        if cara == "frente" and x in (1, 2) and y < 4:
            return tono(A, 0.8 if x == 1 else 0.6)      # la arista de la espinillera
        if y >= 4:
            return tono(A, 0.45 if y == 4 else 0.35) if cara != "abajo" else tono(A, 0.1)
        return col

    def puntera(cara, x, y, w, h):
        return pl2(cara, x, y, w, h)

    m = {"yelmo": yelmo, "gola": gola, "penacho": penacho, "penacho_cola": penacho, "capucha": capucha,
         "esclavina": esclavina, "capucha_pico": capucha, "coraza": sombra(coraza, 0.15), "tabardo": tabardo,
         "escarcela": escarcela}
    for lado, _ in LADOS:
        m.update({f"hombrera_{lado}": hombrera, f"hombrera2_{lado}": hombrera2, f"brazal_{lado}": brazal,
                  f"guantelete_{lado}": guantelete, f"puno_{lado}": puno, f"quijote_{lado}": quijote,
                  f"pulgar_{lado}": lambda cara, x, y, w, h: tono(A, 0.62 if cara in ("frente", "arriba") else 0.35),
                  f"rodillera_{lado}": rodillera, f"greba_{lado}": greba, f"puntera_{lado}": puntera})
    c.mats = m
    return c


@conjunto
def confesor():
    """El confesor: la misma armadura de placas, ennegrecida, con el tabardo morado, la corona de plata y la capucha."""
    return caballero("confesor")


CUERO_PARDO = ["#20160e", "#2f2116", "#40301f", "#54402a", "#6a5236", "#806544"]
ACOLCHADO = {"vagabundo": ["#2a1f15", "#3b2c1e", "#4e3b28", "#634b33", "#7a5d40"],
             "guerrero": ["#0d1626", "#152138", "#1e2e4d", "#283c63", "#344c7a"]}
CALZAS = {"vagabundo": ["#1a1c14", "#25281c", "#323526", "#404432", "#4f543e"],
          "guerrero": ["#15161b", "#1f2027", "#2a2c35", "#363945", "#444858"]}
LATON = ["#4c3814", "#6e5320", "#937233", "#b8924a", "#d8b468"]


@conjunto
def vagabundo(paleta="vagabundo"):
    """
    El vagabundo: cofia de malla con el casco de piezas (cono, aros de laton y nasal), cota de malla sobre el gambeson
    acolchado con su faldon, guantes de cuero con placas y calzas con botas. El guerrero, lo mismo en azul, sin cono ni
    nasal y con una banda de tela atada.
    """
    guerrero = paleta == "guerrero"
    G = ACOLCHADO[paleta]
    c = Conjunto(paleta)
    # ---- yelmo
    c.caja("yelmo", "head", -4, -8, -4, 8, 8, 8, "cofia", 0.62)
    c.caja("yelmo", "body", -4, -1, -2, 8, 2, 4, "camal", 0.9)
    if not guerrero:
        c.caja("yelmo", "head", -4, -8, -4, 8, 3, 8, "casco", 0.82)
        c.caja("yelmo", "head", -3, -10, -3, 6, 2, 6, "cono", 0.25)
        c.caja("yelmo", "head", -1, -11, -1, 2, 1, 2, "punta", 0.1)
        c.caja("yelmo", "head", -0.5, -5.2, -5.15, 1, 3, 1, "nasal")
    else:
        c.caja("yelmo", "head", -4, -8, -4, 8, 3, 8, "banda", 0.85)
        p = c.hija("yelmo", "banda_cola", "head", (1.5, -5.6, 4.85), (18, 0, -8))
        c.caja("yelmo", p, -1, 0, 0, 2, 5, 1, "banda_cola")
    # ---- peto
    c.caja("peto", "body", -4, 0, -2, 8, 12, 4, "jubon", 0.55)
    c.caja("peto", "body", -4, 11, -2, 8, 4, 4, "faldon", 0.72)
    c.lados("peto", "arm", -3, -2, -2, 4, 7, 4, "manga", 0.47)
    # ---- guantes
    c.lados("guantes", "arm", -3, 5, -2, 4, 5, 4, "guante", 0.56)
    c.lados("guantes", "arm", -3, 5, -2, 4, 1, 4, "vuelta_guante", 0.82)
    # ---- grebas
    c.lados("grebas", "leg", -2, 0, -2, 4, 7, 4, "calza", 0.42)
    c.lados("grebas", "leg", -2, 7, -2, 4, 5, 4, "bota", 0.58)
    c.lados("grebas", "leg", -2, 7, -2, 4, 1, 4, "vuelta_bota", 0.84)
    c.caja("grebas", "body", -4, 10, -2, 8, 1, 4, "cinto", 0.86)

    malla = Pintor.malla(MALLA, 3)
    acol = Pintor.tela(G, 21, pliegue=2)
    cuero = Pintor.cuero(CUERO_PARDO, 22)
    pl = Pintor.placa(ACERO[1:7], 23)
    calza = Pintor.tela(CALZAS[paleta], 24, pliegue=3)

    def cofia(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente" and 1 <= x <= 6 and 2 <= y <= 7:
            return None                                  # la cara, al aire
        return malla(cara, x, y, w, h)

    def camal(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara in ("frente", "espalda", "der", "izq") and y == h - 1 and x % 2 == 1:
            return None                                  # el borde de anillas
        return malla(cara, x, y, w, h)

    def casco(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return tono(LATON, 0.7) if x in (3, 4) or y in (3, 4) else pl(cara, x, y, w, h)
        if y == h - 1:
            return tono(LATON, 0.55 if x % 2 == 0 else 0.75)   # el aro de abajo
        if x in (3, 4):
            return tono(LATON, 0.6 if x == 3 else 0.45)        # las bandas
        return pl(cara, x, y, w, h)

    def cono(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara != "arriba" and x in (2, 3):
            return tono(LATON, 0.6 if x == 2 else 0.45)
        return pl(cara, x, y, w, h)

    def banda(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return tono(G, 0.6) if (x + y) % 3 else tono(G, 0.4)
        v = acol(cara, x, y, w, h)
        if y == 1:
            return tono(G, 0.25)
        if cara == "espalda" and x in (4, 5) and y >= 1:
            return tono(G, 0.3)                          # el nudo
        return v

    def jubon(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return tono(G, 0.2)
        if y <= 7:
            m = malla(cara, x, y, w, h)
            if y == 7 and x % 2 == 1:
                return tono(G, 0.45)                     # el canto de la cota
            return m
        if y == 8:
            return tono(CUERO_PARDO, 0.35) if not (cara == "frente" and x in (3, 4)) else tono(LATON, 0.8)
        v = acol(cara, x, y, w, h)
        return tono(G, 0.3) if (x + y) % 4 == 0 else v  # el pespunte del acolchado

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if cara == "arriba" else tono(G, 0.15)
        v = acol(cara, x, y, w, h)
        if cara in ("frente", "espalda") and x in (3, 4):
            return tono(G, 0.12)                         # la abertura
        if y == h - 1:
            return tono(G, 0.25)
        return tono(G, 0.3) if (x + y) % 4 == 0 else v

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if cara == "abajo" else malla(cara, x, y, w, h)
        if y >= h - 2:
            return acol(cara, x, y, w, h)                # el acolchado asoma por debajo
        return malla(cara, x, y, w, h)

    def guante(cara, x, y, w, h):
        if cara == "arriba":
            return None
        v = cuero(cara, x, y, w, h)
        fuera = (cara == "der")                          # el dorso de la mano derecha mira afuera
        if fuera and 1 <= y <= 2 and 0 < x < w - 1:
            return pl(cara, x, y, w, h)
        if y == h - 1:
            return tono(CUERO_PARDO, 0.2)
        return v

    def guante_i(cara, x, y, w, h):
        return guante("der" if cara == "izq" else ("izq" if cara == "der" else cara), x, y, w, h)

    def vuelta(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return tono(CUERO_PARDO, 0.62 if x % 2 == 0 else 0.5)

    def calza_f(cara, x, y, w, h):
        if cara == "arriba":
            return None
        return calza(cara, x, y, w, h)

    def bota(cara, x, y, w, h):
        if cara == "arriba":
            return None
        v = cuero(cara, x, y, w, h)
        if y == h - 1:
            return tono(CUERO_PARDO, 0.12)               # la suela
        if cara == "frente" and y == h - 2:
            return tono(CUERO_PARDO, 0.7)                # la puntera gastada
        return v

    def cinto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente" and x in (2, 3):
            return tono(LATON, 0.8 if x == 2 else 0.6)
        if cara == "frente" and x == 6:
            return tono(CUERO_PARDO, 0.75)               # la bolsa
        return tono(CUERO_PARDO, 0.4 if x % 3 else 0.3)

    m = {"cofia": cofia, "camal": camal, "casco": casco, "cono": cono, "punta": pl, "nasal": pl, "banda": banda,
         "banda_cola": lambda cara, x, y, w, h: acol(cara, x, y, w, h) if y < h - 1 else (tono(G, 0.3) if x == 0 else None),
         "jubon": sombra(jubon, 0.12), "faldon": faldon, "cinto": cinto}
    for n in ("d", "i"):
        m.update({f"manga_{n}": manga, f"guante_{n}": guante if n == "d" else guante_i, f"vuelta_guante_{n}": vuelta,
                  f"calza_{n}": calza_f, f"bota_{n}": bota, f"vuelta_bota_{n}": vuelta})
    c.mats = m
    return c


@conjunto
def guerrero():
    return vagabundo("guerrero")


PIEL_OSO = ["#1b130d", "#2a1e14", "#3a2a1c", "#4c3826", "#5f4831", "#74593d"]
HIERRO_VIEJO = ["#18181a", "#252527", "#353538", "#48484b", "#5e5e61", "#77777a"]


@conjunto
def barbaro():
    """
    El barbaro: casquete de hierro con borde de piel y dos cuernos curvos, chaleco de cuero abierto (se ve el pecho del
    jugador) con manto de piel sobre los hombros y una hombrera de hueso, brazales con tachuelas y perneras de piel atadas
    con correas, con taparrabos y cinto.
    """
    c = Conjunto("barbaro")
    # ---- yelmo
    c.caja("yelmo", "head", -4, -8, -4, 8, 2, 8, "casquete", 0.78)
    c.caja("yelmo", "head", -4, -6, -4, 8, 1, 8, "borde_piel", (1.0, 0.35, 1.0))
    for lado, s in LADOS:
        a = c.hija("yelmo", f"cuerno1_{lado}", "head", (4.7 * s, -7.0, 0.5), (0, 0, -32 * s))
        c.caja("yelmo", a, -3 if s < 0 else 0, -1, -1, 3, 2, 2, f"cuerno1_{lado}")
        b = c.hija("yelmo", f"cuerno2_{lado}", a, (3 * s, 0, 0), (0, 0, -38 * s))
        c.caja("yelmo", b, -3 if s < 0 else 0, -0.5, -0.5, 3, 1, 1, f"cuerno2_{lado}")
        t = c.hija("yelmo", f"cuerno3_{lado}", b, (-3 if s < 0 else 3, 0, 0), (0, 0, -40 * s))
        c.caja("yelmo", t, -2 if s < 0 else 0, -0.5, -0.5, 2, 1, 1, f"cuerno3_{lado}")
    # ---- peto
    c.caja("peto", "body", -4, 0, -2, 8, 12, 4, "chaleco", 0.5)
    c.caja("peto", "body", -5, -1, -3, 10, 3, 6, "manto", 0.15)
    c.caja("peto", "right_arm", -3.8, -3.2, -2.6, 5, 3, 5, "hombrera_hueso", 0.1)
    p = c.hija("peto", "colmillo", "right_arm", (-3.6, -3.0, -1.0), (0, 0, 35))
    c.caja("peto", p, -3, -0.5, -0.5, 3, 1, 1, "colmillo")
    c.caja("peto", "left_arm", esp(-3.6, 5), -2.8, -2.5, 5, 2, 5, "hombro_piel", 0.15)
    # ---- guantes
    c.lados("guantes", "arm", -3, 5, -2, 4, 4, 4, "brazal", 0.55)
    c.lados("guantes", "arm", -3, 5, -2, 4, 1, 4, "puno_piel", 0.88)
    # ---- grebas
    c.lados("grebas", "leg", -2, 0, -2, 4, 12, 4, "pernera", 0.45)
    c.lados("grebas", "leg", -2, 8, -2, 4, 2, 4, "bota_piel", 0.78)
    c.caja("grebas", "body", -4, 10, -2, 8, 1, 4, "cinto", 0.75)
    c.caja("grebas", "body", -2, 11, -3.6, 4, 5, 1, "taparrabos")

    pelo = Pintor.pelo(PIEL_OSO, 31, 3)
    cuero = Pintor.cuero(CUERO_PARDO, 32)
    hierro = Pintor.placa(HIERRO_VIEJO, 33, grano=0.12)
    hueso = Pintor.placa(HUESO, 34, grano=0.1, veta=False)

    def casquete(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return tono(HIERRO_VIEJO, 0.8) if x in (3, 4) else hierro(cara, x, y, w, h)
        if x in (3, 4):
            return tono(HIERRO_VIEJO, 0.75 if x == 3 else 0.55)
        return hierro(cara, x, y, w, h)

    def borde_piel(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return pelo(cara, x, y, w, h) if (x in (0, w - 1) or y in (0, h - 1)) else None
        return tono(PIEL_OSO, 0.45 + 0.3 * ((x * 5) % 3) / 2)

    def cuerno(n):
        def f(cara, x, y, w, h):
            base = 0.35 + 0.18 * n
            v = base + (0.1 if cara in ("arriba", "frente") else -0.08) + (0.08 if x % 2 == 0 else 0)
            return tono(HUESO, min(0.95, v))
        return f

    def chaleco(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return tono(CUERO_PARDO, 0.2)
        if cara == "frente":
            if 3 <= x <= 4 and 1 <= y <= 9:
                return None                              # abierto: el pecho al aire
            if x in (2, 5):
                return pelo(cara, x, y, w, h)            # el borde de piel
            if (x + y) % 5 == 0:
                return tono(CUERO_PARDO, 0.25)
        if cara in ("frente", "espalda") and y == 9:
            return tono(CUERO_PARDO, 0.2)
        return cuero(cara, x, y, w, h)

    def manto(cara, x, y, w, h):
        if cara == "abajo":
            # por debajo: el hueco de los hombros y el cuello
            return None
        if cara in ("frente", "espalda", "der", "izq") and y == h - 1 and (x * 7) % 3 == 0:
            return None                                  # el borde deshilachado
        return pelo(cara, x, y, w, h)

    def hombrera_hueso(cara, x, y, w, h):
        if cara == "abajo":
            return None
        v = hueso(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return tono(HUESO, 0.2)
        if cara in ("der", "frente") and (x + 2 * y) % 5 == 0:
            return tono(HUESO, 0.25)                     # grietas
        return v

    def hombro_piel(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return pelo(cara, x, y, w, h)

    def brazal(cara, x, y, w, h):
        if cara == "arriba":
            return None
        v = cuero(cara, x, y, w, h)
        if y in (1, 3) and x % 2 == 1:
            return tono(HIERRO_VIEJO, 0.8)               # tachuelas
        if y == h - 1:
            return tono(CUERO_PARDO, 0.15)
        return v

    def puno_piel(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return tono(PIEL_OSO, 0.55 if (x * 3) % 2 == 0 else 0.38)

    def pernera(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return tono(CUERO_PARDO, 0.12)
        if (y + (x if cara in ("frente", "der") else -x)) % 4 == 0:
            return tono(CUERO_PARDO, 0.58)               # las correas cruzadas
        if y >= 11:
            return tono(CUERO_PARDO, 0.2)
        return pelo(cara, x, y, w, h) if y < 8 else cuero(cara, x, y, w, h)

    def bota_piel(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return pelo(cara, x, y, w, h) if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)) else None
        return pelo(cara, x, y, w, h)

    def cinto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente" and x in (3, 4):
            return tono(HUESO, 0.8 if x == 3 else 0.6)  # la hebilla de hueso
        return tono(CUERO_PARDO, 0.32 if x % 2 else 0.4)

    def taparrabos(cara, x, y, w, h):
        if cara in ("arriba",):
            return None
        if cara in ("frente", "espalda") and y == h - 1 and x % 2 == 1:
            return None
        return pelo(cara, x, y, w, h) if cara in ("frente", "espalda") else tono(PIEL_OSO, 0.3)

    m = {"casquete": casquete, "borde_piel": borde_piel, "chaleco": sombra(chaleco, 0.12), "manto": manto,
         "hombrera_hueso": hombrera_hueso, "colmillo": cuerno(2), "hombro_piel": hombro_piel, "cinto": cinto,
         "taparrabos": taparrabos}
    for lado, _ in LADOS:
        for n in (1, 2, 3):
            m[f"cuerno{n}_{lado}"] = cuerno(n)
    for n in ("d", "i"):
        m.update({f"brazal_{n}": brazal, f"puno_piel_{n}": puno_piel, f"pernera_{n}": pernera, f"bota_piel_{n}": bota_piel})
    c.mats = m
    return c


NEGRO_CUERO = ["#09080a", "#121014", "#1b181d", "#252128", "#302b34", "#3d3742"]
TELA_NEGRA = ["#0b0b0d", "#141417", "#1d1d21", "#27272c", "#323238"]


@conjunto
def bandido():
    """
    El bandido: capucha negra con la mascara hasta la nariz (solo se le ven los ojos), jubon de cuero negro con tachuelas
    y la bandolera de cuchillos, capa corta, brazales de cuero con correas (los dedos al aire) y calzas con botas altas.
    """
    c = Conjunto("bandido")
    c.caja("yelmo", "head", -4, -8, -4, 8, 8, 8, "capucha", 0.66)
    p = c.hija("yelmo", "capucha_cola", "head", (0, -7.6, 4.62), (32, 0, 0))
    c.caja("yelmo", p, -2, 0, 0, 4, 4, 1, "capucha_cola")
    c.caja("peto", "body", -4, 0, -2, 8, 12, 4, "jubon", 0.5)
    c.caja("peto", "body", -4, 0, 2.6, 8, 10, 1, "capa")
    c.lados("peto", "arm", -3, -2, -2, 4, 6, 4, "manga", 0.42)
    c.lados("guantes", "arm", -3, 4, -2, 4, 6, 4, "brazal", 0.56)
    c.lados("grebas", "leg", -2, 0, -2, 4, 8, 4, "calza", 0.38)
    c.lados("grebas", "leg", -2, 7, -2, 4, 5, 4, "bota", 0.56)
    c.caja("grebas", "body", -4, 10, -2, 8, 1, 4, "cinto", 0.8)
    c.caja("grebas", "body", 1.5, 10.5, -3.5, 2, 2, 1, "bolsa")

    cuero = Pintor.cuero(NEGRO_CUERO, 41, grano=0.3)
    tela = Pintor.tela(TELA_NEGRA, 42, pliegue=2)

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente":
            if y in (3, 4) and 1 <= x <= 6:
                return None if y == 4 or x in (2, 5) else tono(TELA_NEGRA, 0.15)   # los ojos, al aire
            if y <= 1:
                return tono(TELA_NEGRA, 0.2 if y == 1 else 0.45)                     # el borde de la capucha
            if y >= 5:
                return tono(TELA_NEGRA, 0.38 if (x + y) % 3 else 0.3)                # la mascara
        return tela(cara, x, y, w, h)

    def jubon(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return tono(NEGRO_CUERO, 0.15)
        v = cuero(cara, x, y, w, h)
        # la bandolera: del hombro derecho a la cadera izquierda (delante) y al reves detras
        if cara in ("frente", "espalda"):
            xb = x if cara == "frente" else w - 1 - x
            if abs(xb - (1 + y * 6 / 10)) < 0.9 and y <= 10:
                if y in (3, 5, 7):
                    return tono(PLATA, 0.75)             # los mangos de los cuchillos
                return tono(CUERO_PARDO, 0.45)
        if (x * 3 + y * 5) % 9 == 0:
            return tono(NEGRO_CUERO, 0.9)                # tachuelas
        return v

    def capa(cara, x, y, w, h):
        if cara in ("abajo",):
            return None
        if cara in ("espalda", "frente", "der", "izq") and y == h - 1 and x % 2 == 0:
            return None
        return tela(cara, x, y, w, h)

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if cara == "abajo" else cuero(cara, x, y, w, h)
        return cuero(cara, x, y, w, h)

    def brazal(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return None                                  # los dedos, al aire
        if y == h - 1 and x % 2 == 0:
            return None
        if y in (1, 3):
            return tono(CUERO_PARDO, 0.4)                # las correas
        if y in (1, 3) and x == 1:
            return tono(PLATA, 0.7)
        return cuero(cara, x, y, w, h)

    def calza(cara, x, y, w, h):
        if cara == "arriba":
            return None
        return tela(cara, x, y, w, h)

    def bota(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if y == h - 1:
            return tono(NEGRO_CUERO, 0.05)
        if y == 0:
            return tono(NEGRO_CUERO, 0.6)
        if cara == "frente" and y == 2 and x in (1, 2):
            return tono(PLATA, 0.6)                      # la hebilla
        return cuero(cara, x, y, w, h)

    def cinto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara == "frente" and x in (3, 4):
            return tono(PLATA, 0.7 if x == 3 else 0.5)
        return tono(CUERO_PARDO, 0.3)

    m = {"capucha": capucha, "capucha_cola": lambda cara, x, y, w, h: tela(cara, x, y, w, h), "jubon": sombra(jubon, 0.1),
         "capa": capa, "cinto": cinto, "bolsa": lambda cara, x, y, w, h: tono(CUERO_PARDO, 0.5 if cara == "frente" else 0.3)}
    for n in ("d", "i"):
        m.update({f"manga_{n}": manga, f"brazal_{n}": brazal, f"calza_{n}": calza, f"bota_{n}": bota})
    c.mats = m
    return c


TUNICA = {"hechicero": ["#0a0f22", "#111a35", "#19254a", "#223260", "#2d4078", "#3b5192"],
          "profeta": ["#240d08", "#36150d", "#4a1f13", "#5e2a1a", "#733621", "#88452b"],
          "prisionero": ["#1c1a16", "#29261f", "#36322a", "#443f35", "#534d41", "#635c4e"]}
FILO = {"hechicero": PLATA, "profeta": ["#3a2a16", "#5a4426", "#7a5f39", "#9b7c4f", "#bb9a66"], "prisionero": HIERRO_VIEJO}
VENDA = ["#5f584a", "#7b7360", "#968d77", "#b0a78f", "#c9c1a8"]
PLUMAS = ["#0b0a0b", "#151315", "#211d1f", "#2e292b", "#3d3639"]


def _tunica(nombre):
    """
    Las tunicas: el hechicero (sombrero de punta con su banda de plata, tunica azul de cuello alto, mangas anchas, guantes
    finos y la falda abierta), el profeta (la venda en los ojos, el habito rojizo con el manto de plumas negras, las vendas
    y las cuentas) y el prisionero (la jaula de hierro en la cabeza con su candado, los harapos y los grilletes).
    """
    T = TUNICA[nombre]
    F = FILO[nombre]
    c = Conjunto(nombre)
    if nombre == "hechicero":
        c.caja("yelmo", "head", -4, -8, -4, 8, 2, 8, "copa", 0.75)
        c.caja("yelmo", "head", -6, -6.4, -6, 12, 1, 12, "ala")
        c.caja("yelmo", "head", -3, -11, -3, 6, 3, 6, "cono1", 0.3)
        a = c.hija("yelmo", "cono2", "head", (0, -11, 0), (22, 0, 4))
        c.caja("yelmo", a, -2, -3, -2, 4, 3, 4, "cono2", 0.2)
        c.caja("yelmo", a, -1, -6, -1, 2, 3, 2, "cono3", 0.12)
        b = c.hija("yelmo", "cono4", a, (0, -6, 0), (30, 0, 0))
        c.caja("yelmo", b, -0.5, -2, -0.5, 1, 2, 1, "cono4")
    elif nombre == "profeta":
        c.caja("yelmo", "head", -4, -5, -4, 8, 2, 8, "venda", 0.64)
        a = c.hija("yelmo", "nudo", "head", (0, -4.2, 4.64), (10, 0, 0))
        c.caja("yelmo", a, -1, -1, 0, 2, 2, 1, "nudo")
        c.caja("yelmo", a, -2, 0.5, 0.5, 1, 4, 0, "cinta_d")
        c.caja("yelmo", a, 1, 0.5, 0.5, 1, 5, 0, "cinta_i")
    else:
        c.caja("yelmo", "head", -4, -8, -4, 8, 8, 8, "jaula", 1.05)
        c.caja("yelmo", "head", -4, -1, -4, 8, 1, 8, "collar", (1.35, 0.35, 1.35))
        c.caja("yelmo", "head", -1, -1.6, -6, 2, 2, 1, "candado")
        c.caja("yelmo", "head", -1, -10, -0.5, 2, 1, 1, "argolla")
    # ---- peto
    c.caja("peto", "body", -4, 0, -2, 8, 12, 4, "tunica", 0.58 if nombre != "prisionero" else 0.45)
    if nombre == "hechicero":
        c.caja("peto", "body", -4, -1, -2, 8, 1, 4, "cuello", 0.9)
        c.lados("peto", "arm", -3, -2, -2, 4, 8, 4, "manga", 0.55)
        c.lados("peto", "arm", -3, 5, -2, 4, 3, 4, "puno", 0.95)
    elif nombre == "profeta":
        c.caja("peto", "body", -5, -1, -3, 10, 4, 6, "plumas", 0.1)
        c.lados("peto", "arm", -3, -2, -2, 4, 9, 4, "manga", 0.52)
    else:
        c.lados("peto", "arm", -3, -2, -2, 4, 5, 4, "manga", 0.4)
    # ---- guantes
    if nombre == "hechicero":
        c.lados("guantes", "arm", -3, 7, -2, 4, 3, 4, "guante", 0.4)
    elif nombre == "profeta":
        c.lados("guantes", "arm", -3, 6, -2, 4, 4, 4, "vendas", 0.5)
        c.lados("guantes", "arm", -3, 6, -2, 4, 1, 4, "cuentas", 0.78)
    else:
        c.lados("guantes", "arm", -3, 7, -2, 4, 2, 4, "grillete", 0.75)
    # ---- grebas
    if nombre == "hechicero":
        c.lados("grebas", "leg", -2, 0, -2, 4, 11, 4, "falda", 0.72)
        c.lados("grebas", "leg", -2, 11, -2, 4, 1, 4, "zapato", 0.55)
        c.caja("grebas", "body", -4, 10, -2, 8, 1, 4, "faja", 0.8)
        c.caja("grebas", "body", -1, 10, -3.6, 2, 6, 1, "faja_cuelga")
    elif nombre == "profeta":
        c.lados("grebas", "leg", -2, 0, -2, 4, 10, 4, "falda", 0.7)
        c.lados("grebas", "leg", -2, 10, -2, 4, 2, 4, "vendas_pie", 0.5)
        c.caja("grebas", "body", -4, 9, -2, 8, 1, 4, "cuerda", 0.75)
        c.caja("grebas", "body", 1, 10, -3.4, 1, 4, 1, "cuerda_cuelga")
    else:
        c.lados("grebas", "leg", -2, 0, -2, 4, 9, 4, "falda", 0.36)
        c.lados("grebas", "leg", -2, 9, -2, 4, 3, 4, "vendas_pie", 0.45)

    tela = Pintor.tela(T, 51, pliegue=3)
    plata = Pintor.placa(F, 52, grano=0.06)
    hierro = Pintor.placa(HIERRO_VIEJO, 53, grano=0.1)
    vend = Pintor.tela(VENDA, 54, pliegue=2)
    plumas_p = Pintor.pelo(PLUMAS, 55, 2)

    def raido(f, fila_abajo=True):
        """Lo de abajo deshilachado (huecos en la ultima fila)."""
        def g(cara, x, y, w, h):
            if fila_abajo and cara in ("frente", "espalda", "der", "izq") and y == h - 1 and (x * 5 + 1) % 3 == 0:
                return None
            return f(cara, x, y, w, h)
        return g

    # -- el sombrero
    def copa(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara != "arriba" and y == h - 1:
            return tono(F, 0.75 if x % 3 else 0.5)      # la banda de plata
        if cara == "frente" and y == 0 and x in (3, 4):
            return "#7fd3ff"                             # la piedra de brillo
        return tela(cara, x, y, w, h)

    def ala(cara, x, y, w, h):
        if cara == "abajo":
            return tono(T, 0.15)
        if cara == "arriba":
            if 2 <= x <= w - 3 and 2 <= y <= h - 3:
                return tono(T, 0.25)                     # bajo la copa
            return tono(T, 0.55 if (x + y) % 2 else 0.45)
        return tono(T, 0.35)

    def cono(cara, x, y, w, h):
        if cara == "abajo":
            return None
        v = tela(cara, x, y, w, h)
        if cara == "frente" and (x * 7 + y * 3) % 11 == 0:
            return tono(F, 0.9)                          # estrellitas de plata
        return v

    # -- la venda del profeta
    def venda(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        v = vend(cara, x, y, w, h)
        if cara == "frente" and y == 1 and x in (1, 2, 5, 6):
            return "#5a1410" if x in (2, 5) else "#3d0d0a"   # sangre seca en los ojos
        return v

    # -- la jaula
    def jaula(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            if x in (3, 4) or y in (3, 4) or x in (0, w - 1) or y in (0, h - 1):
                return hierro(cara, x, y, w, h)
            return None
        if y in (0, h - 1) or y == 3:
            return hierro(cara, x, y, w, h)              # los aros
        if x % 2 == 0:
            return tono(HIERRO_VIEJO, 0.6 if x % 4 == 0 else 0.45)   # los barrotes
        return None

    def collar(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            if x in (0, w - 1) or y in (0, h - 1):
                return hierro(cara, x, y, w, h)
            return None
        return tono(HIERRO_VIEJO, 0.5 if x % 2 else 0.35)

    def candado(cara, x, y, w, h):
        return tono(LATON, 0.6 if (x + y) % 2 == 0 else 0.4) if cara != "frente" or not (x == 0 and y == 1) else "#0a0a0a"

    # -- la tunica
    def tunica(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return tono(T, 0.12)
        v = tela(cara, x, y, w, h)
        if nombre == "hechicero":
            if cara == "frente" and x in (3, 4):
                return tono(F, 0.7 if x == 3 else 0.5)   # el filo de plata
            if y == 10:
                return tono(["#2a0f33", "#3b1648", "#4d1f5e", "#612a75", "#76368d"], 0.55)   # el fajin
        elif nombre == "profeta":
            if y == 8:
                return tono(F, 0.55 if x % 2 else 0.4)   # la cuerda
            if y == h - 1 and x % 3 == 0:
                return None
        else:
            if (x * 13 + y * 7) % 17 == 0:
                return tono(T, 0.12)                     # agujeros y remiendos
            if (x * 5 + y * 11) % 19 == 0:
                return tono(["#2c2a22", "#3c382e", "#4a463a"], 0.5)
            if y == h - 1 and x % 2 == 0:
                return None
        return v

    def cuello(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return tono(F, 0.6) if x in (0, w - 1) or y in (0, h - 1) else tono(T, 0.2)
        if y == 0:
            return tono(F, 0.75)
        return tela(cara, x, y, w, h)

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if cara == "abajo" else tela(cara, x, y, w, h)
        if nombre == "profeta" and y == h - 1 and (x + 1) % 2 == 0:
            return None
        if nombre == "prisionero" and y == h - 1 and x % 2 == 1:
            return None
        return tela(cara, x, y, w, h)

    def puno(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return tono(T, 0.3)
        if y == h - 1:
            return tono(F, 0.7 if x % 2 else 0.5)
        return tela(cara, x, y, w, h)

    def guante(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "frente" and y == 1 and x == 1:
            return "#7fd3ff"                             # el anillo con su piedra
        return tono(TELA_NEGRA, 0.5 + (0.12 if (x + y) % 2 else 0))

    def vendas(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if (x + y) % 3 == 0:
            return tono(VENDA, 0.3)
        return vend(cara, x, y, w, h)

    def cuentas(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return tono(HUESO, 0.8) if x % 2 == 0 else "#3a1712"

    def grillete(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara == "der" and x == 1 and y == 0:
            return tono(HIERRO_VIEJO, 0.95)
        return hierro(cara, x, y, w, h)

    def plumas(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara != "arriba" and y == h - 1 and (x * 3) % 2 == 0:
            return None
        v = plumas_p(cara, x, y, w, h)
        if cara != "arriba" and (x + y * 2) % 5 == 0:
            return tono(VENDA, 0.35)                     # el canon claro de alguna pluma
        return v

    def falda(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return tono(T, 0.12)
        v = tela(cara, x, y, w, h)
        if nombre == "hechicero" and y == h - 1:
            return tono(F, 0.6 if x % 2 else 0.45)
        if nombre != "hechicero" and y == h - 1 and (x * 7 + 2) % 3 == 0:
            return None
        if nombre == "prisionero" and (x * 13 + y * 7) % 17 == 0:
            return tono(T, 0.12)
        return v

    def zapato(cara, x, y, w, h):
        return tono(TELA_NEGRA, 0.35 if cara in ("frente", "arriba") else 0.2)

    def faja(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return tono(["#2a0f33", "#3b1648", "#4d1f5e", "#612a75", "#76368d"], 0.6 if x % 3 else 0.45)

    def cuerda(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return tono(F, 0.62 if (x + y) % 2 else 0.42)

    m = {"copa": copa, "ala": ala, "cono1": cono, "cono2": cono, "cono3": cono, "cono4": cono, "venda": venda,
         "nudo": venda, "cinta_d": vend, "cinta_i": vend, "jaula": jaula, "collar": collar, "candado": candado,
         "argolla": hierro, "tunica": sombra(tunica, 0.15), "cuello": cuello, "plumas": plumas, "faja": faja,
         "faja_cuelga": lambda cara, x, y, w, h: tono(["#2a0f33", "#3b1648", "#4d1f5e", "#612a75", "#76368d"], 0.5 - 0.06 * y)
         if not (y == h - 1 and x == 1) else None,
         "cuerda": cuerda, "cuerda_cuelga": cuerda}
    for n in ("d", "i"):
        m.update({f"manga_{n}": manga, f"puno_{n}": puno, f"guante_{n}": guante, f"vendas_{n}": vendas,
                  f"cuentas_{n}": cuentas, f"grillete_{n}": grillete, f"falda_{n}": raido(falda, False),
                  f"zapato_{n}": zapato, f"vendas_pie_{n}": vendas})
    c.mats = m
    return c


@conjunto
def hechicero():
    return _tunica("hechicero")


@conjunto
def profeta():
    return _tunica("profeta")


@conjunto
def prisionero():
    return _tunica("prisionero")


# ---------------------------------------------------------------- generar

def textura(c):
    c.rig.empaquetar()
    t = Textura(c.rig)
    hechos = set()
    for _, b in c.rig.cajas():
        clave = b.uv or id(b)
        if clave in hechos:
            continue
        hechos.add(clave)
        f = c.mats.get(b.mat)
        if f is None:
            raise SystemExit(f"{c.nombre}: falta el pintor de {b.mat}")
        t.pintar_caja(b, f)
    return t


def java(conjuntos):
    out = ["package com.darkcraft.client.armadura;\n",
           "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;\n",
           f"/** GENERADO por {HERRAMIENTA}: no tocar a mano. La geometria de cada pieza de cada conjunto de armadura. */",
           "public final class ArmaduraGeometria {",
           "    private ArmaduraGeometria() {}\n",
           "    /** Los conjuntos que tienen geometria (en el orden de la herramienta). */",
           "    public static final String[] CONJUNTOS = {" + ", ".join(f'"{c.nombre}"' for c in conjuntos) + "};\n",
           "    /** La geometria de la pieza (0 yelmo, 1 peto, 2 guantes, 3 grebas) del conjunto, o null si no hay. */",
           "    public static LayerDefinition crear(String conjunto, int pieza) {",
           "        return switch (conjunto + \":\" + pieza) {"]
    for c in conjuntos:
        for i, p in enumerate(PIEZAS):
            if c.cajas_de(p):
                out.append(f"            case \"{c.nombre}:{i}\" -> {c.nombre}_{p}();")
    out.append("            default -> null;")
    out.append("        };")
    out.append("    }")
    for c in conjuntos:
        for p in PIEZAS:
            if not c.cajas_de(p):
                continue
            out.append("")
            out.append(f"    private static LayerDefinition {c.nombre}_{p}() {{")
            out.append("        MeshDefinition mesh = new MeshDefinition();")
            out.append("        PartDefinition root = mesh.getRoot();")
            var = {"": "root"}
            for parte in c.rig.partes:
                es_base = parte.name in dict(PARTES)
                if not es_base and c.parte_pieza.get(parte.name) != p:
                    continue
                v = "p_" + parte.name
                var[parte.name] = v
                cb = "CubeListBuilder.create()"
                for b in parte.boxes:
                    if c.de[id(b)] != p:
                        continue
                    gx, gy, gz = b.grow
                    cb += f".texOffs({b.u}, {b.v})"
                    cb += (f".addBox({fmt(b.x)}, {fmt(b.y)}, {fmt(b.z)}, {fmt(b.w)}, {fmt(b.h)}, {fmt(b.d)}, "
                           f"new CubeDeformation({fmt(gx)}, {fmt(gy)}, {fmt(gz)}))")
                rx, ry, rz = (math.radians(a) for a in parte.rot)
                px, py, pz = parte.pivot
                if rx == ry == rz == 0:
                    pose = f"PartPose.offset({fmt(px)}, {fmt(py)}, {fmt(pz)})"
                else:
                    pose = f"PartPose.offsetAndRotation({fmt(px)}, {fmt(py)}, {fmt(pz)}, {fmt(rx)}, {fmt(ry)}, {fmt(rz)})"
                madre = var.get(parte.parent, "root")
                out.append(f"        PartDefinition {v} = {madre}.addOrReplaceChild(\"{parte.name}\", {cb}, {pose});")
            out.append(f"        return LayerDefinition.create(mesh, {c.rig.tex_w}, {c.rig.tex_h});")
            out.append("    }")
    out.append("}")
    return "\n".join(out) + "\n"


# ---------------------------------------------------------------- dibujar (vistas e iconos)

def maniqui():
    """El jugador de Minecraft con la piel de Steve (para ver la armadura puesta)."""
    r = Rig("maniqui", 64)
    for n, p in PARTES:
        r.pieza(n, "", p)
    capas = [("head", (-4, -8, -4, 8, 8, 8), (0, 0), 0), ("hat", (-4, -8, -4, 8, 8, 8), (32, 0), 0.5),
             ("body", (-4, 0, -2, 8, 12, 4), (16, 16), 0), ("body", (-4, 0, -2, 8, 12, 4), (16, 32), 0.25),
             ("right_arm", (-3, -2, -2, 4, 12, 4), (40, 16), 0), ("right_arm", (-3, -2, -2, 4, 12, 4), (40, 32), 0.25),
             ("left_arm", (-1, -2, -2, 4, 12, 4), (32, 48), 0), ("left_arm", (-1, -2, -2, 4, 12, 4), (48, 48), 0.25),
             ("right_leg", (-2, 0, -2, 4, 12, 4), (0, 16), 0), ("right_leg", (-2, 0, -2, 4, 12, 4), (0, 32), 0.25),
             ("left_leg", (-2, 0, -2, 4, 12, 4), (16, 48), 0), ("left_leg", (-2, 0, -2, 4, 12, 4), (0, 48), 0.25)]
    for parte, (x, y, z, w, h, d), (u, v), gr in capas:
        b = Box(x, y, z, w, h, d, "piel", grow=(gr, gr, gr))
        b.u, b.v = u, v
        r.por_nombre[parte].boxes.append(b)
    r.tex_h = 64
    import zipfile
    import io
    with zipfile.ZipFile(STEVE) as z:
        im = Image.open(io.BytesIO(z.read("assets/minecraft/textures/entity/player/wide/steve.png"))).convert("RGBA")
    return r, np.asarray(im).astype(np.float64) / 255.0


def render_varios(capas, offsets=None, yaw=200, pitch=-10, size=(240, 320), zoom=None, fondo=(46, 44, 52), giro=0.0,
                  centro_y=1.0, solo=None, luz_cam=True):
    """Como vista.render, pero con varios modelos a la vez (el maniqui y la armadura) y su z comun.
    solo: {id(caja), ...} para dibujar solo esas cajas (iconos)."""
    W, H = size
    D = math.pi / 180
    ent = vista.ry((180 - giro) * D) @ vista.sc(-1, -1, 1) @ vista.tr(0, -1.501, 0)
    cam = vista.rx(-pitch * D) @ vista.ry(-yaw * D)
    zoom = zoom or H / 2.9
    img = np.zeros((H, W, 3))
    img[:] = np.array(fondo) / 255.0
    alfa = np.zeros((H, W))
    zbuf = np.full((H, W), -1e9)
    luz = np.linalg.inv(cam[:3, :3]) @ np.array([-0.35, 0.62, 0.70])
    luz = luz / np.linalg.norm(luz)
    for rig, tex in capas:
        mats = vista.matrices(rig, offsets)
        for p in rig.partes:
            m = mats[p.name]
            for b in p.boxes:
                if solo is not None and id(b) not in solo:
                    continue
                cr = vista.corners(b)
                ch = np.c_[cr, np.ones(8)] @ m.T
                w, h, d = math.ceil(b.w), math.ceil(b.h), math.ceil(b.d)
                for fname, (idx, uvf) in vista.FACES.items():
                    u1, v1, u2, v2 = uvf(b.u, b.v, w, h, d)
                    uvs = np.array([[u2, v1], [u1, v1], [u1, v2], [u2, v2]])
                    pts = ch[idx, :3]
                    wp = np.c_[pts, np.ones(4)] @ ent.T
                    wp[:, 1] -= centro_y
                    cp = wp @ cam.T
                    sx = W / 2 + cp[:, 0] * zoom
                    sy = H / 2 - cp[:, 1] * zoom
                    sz = cp[:, 2]
                    e1 = wp[1, :3] - wp[0, :3]
                    e2 = wp[3, :3] - wp[0, :3]
                    n = np.cross(e1, e2)
                    nn = np.linalg.norm(n)
                    if nn < 1e-12:
                        continue
                    n /= nn
                    sombra_ = 0.55 + 0.45 * abs(float(n @ luz))
                    for tri in ((0, 1, 2), (0, 2, 3)):
                        antes = zbuf.copy()
                        vista.raster(img, zbuf, sx[list(tri)], sy[list(tri)], sz[list(tri)], uvs[list(tri)], tex, None, sombra_, rig.escala)
                        alfa[zbuf != antes] = 1.0
    return (np.clip(img, 0, 1) * 255).astype(np.uint8), alfa


def icono(c, tex, pieza, lado=32):
    """El icono de una pieza: sus cajas vistas de tres cuartos, encajadas en `lado` x `lado`, con contorno oscuro."""
    solo = {id(b) for _, b in c.cajas_de(pieza)}
    # las piezas hijas no estan en cajas_de por nombre de parte; cajas_de mira las cajas, que es lo que cuenta
    poses = {
        "yelmo": dict(yaw=40, pitch=-16, offsets=None),
        "peto": dict(yaw=25, pitch=-8, offsets={"right_arm": {"rot": (0, 0, 10)}, "left_arm": {"rot": (0, 0, -10)}}),
        # un guantelete solo (el derecho), en diagonal
        "guantes": dict(yaw=30, pitch=-14, offsets={"right_arm": {"rot": (-20, 0, -42)}}),
        "grebas": dict(yaw=15, pitch=-6, offsets={"right_leg": {"rot": (0, 0, -7), "pos": (-2.0, 0, 0)},
                                                  "left_leg": {"rot": (0, 0, 7), "pos": (2.0, 0, 0)}}),
    }[pieza]
    if pieza == "guantes":
        derecho = {id(b) for p, b in c.cajas_de(pieza) if p.name == "right_arm" or c.rig.camino(p.name)[0] == "right_arm"}
        solo = derecho or solo
    grande = 256
    img, alfa = render_varios([(c.rig, tex.a)], poses["offsets"], yaw=poses["yaw"], pitch=poses["pitch"], size=(grande, grande),
                              zoom=grande / 1.2, fondo=(0, 0, 0), solo=solo, centro_y={"yelmo": 1.95, "peto": 1.1,
                                                                                         "guantes": 1.1, "grebas": 0.45}[pieza])
    ys, xs = np.nonzero(alfa)
    if len(xs) == 0:
        return Image.new("RGBA", (lado, lado))
    x0, x1, y0, y1 = xs.min(), xs.max(), ys.min(), ys.max()
    ext = max(x1 - x0 + 1, y1 - y0 + 1)
    util = lado - 4
    k = util / ext
    out = Image.new("RGBA", (lado, lado), (0, 0, 0, 0))
    ow = max(1, int(round((x1 - x0 + 1) * k)))
    oh = max(1, int(round((y1 - y0 + 1) * k)))
    recorte = Image.fromarray(np.dstack([img, (alfa * 255).astype(np.uint8)])[y0:y1 + 1, x0:x1 + 1], "RGBA")
    pequeno = recorte.resize((ow, oh), Image.NEAREST)
    out.paste(pequeno, ((lado - ow) // 2, (lado - oh) // 2))
    a = np.asarray(out).copy()
    op = a[:, :, 3] > 0
    # contorno de un pixel, oscuro
    cont = np.zeros_like(op)
    for dy, dx in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        cont |= np.roll(np.roll(op, dy, 0), dx, 1)
    cont &= ~op
    a[cont] = (14, 11, 10, 255)
    return Image.fromarray(a, "RGBA")


def vistas(c, tex, carpeta):
    os.makedirs(carpeta, exist_ok=True)
    man, piel = maniqui()
    capas = [(man, piel), (c.rig, tex.a)]
    andar = {"right_arm": {"rot": (-35, 0, 0)}, "left_arm": {"rot": (35, 0, 0)}, "right_leg": {"rot": (30, 0, 0)},
             "left_leg": {"rot": (-30, 0, 0)}}
    tiles = []
    for yaw, off in ((20, None), (200, None), (290, None), (45, andar)):
        img, _ = render_varios(capas, off, yaw=yaw, pitch=-8, size=(240, 330))
        tiles.append(img)
    hoja = Image.fromarray(np.concatenate(tiles, axis=1))
    # los iconos en grande, debajo
    fila = Image.new("RGB", (hoja.width, 150), (46, 44, 52))
    for i, p in enumerate(PIEZAS):
        ic = icono(c, tex, p).resize((128, 128), Image.NEAREST)
        fila.paste(ic, (10 + i * 150, 11), ic)
    total = Image.new("RGB", (hoja.width, hoja.height + 150))
    total.paste(hoja, (0, 0))
    total.paste(fila, (0, hoja.height))
    d = ImageDraw.Draw(total)
    d.text((6, 4), c.nombre, fill=(235, 220, 160))
    total.save(os.path.join(carpeta, f"{c.nombre}.png"))
    Image.fromarray((np.clip(tex.a, 0, 1) * 255).astype(np.uint8), "RGBA").resize((tex.a.shape[1] * 4, tex.a.shape[0] * 4),
                                                                                   Image.NEAREST).save(os.path.join(carpeta, f"{c.nombre}_textura.png"))
    return total


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--vistas")
    ap.add_argument("--solo")
    a = ap.parse_args()
    nombres = [a.solo] if a.solo else list(CONJUNTOS)
    hechos = []
    for n in CONJUNTOS:
        c = CONJUNTOS[n]()
        t = textura(c)
        hechos.append(c)
        if n not in nombres:
            continue
        os.makedirs(TEX_DIR, exist_ok=True)
        t.guardar(os.path.join(TEX_DIR, f"{n}.png"))
        for p in PIEZAS:
            if not c.cajas_de(p):
                continue
            icono(c, t, p).save(os.path.join(ICO_DIR, f"{n}_{p}.png"))
            with open(os.path.join(MOD_DIR, f"{n}_{p}.json"), "w") as f:
                json.dump({"parent": "minecraft:item/generated", "textures": {"layer0": f"darkcraft:item/{n}_{p}"}}, f, indent=2)
                f.write("\n")
        if a.vistas:
            vistas(c, t, a.vistas)
        print(f"{n}: {sum(1 for _ in c.rig.cajas())} cajas, textura {c.rig.tex_w} x {c.rig.tex_h}")
    os.makedirs(os.path.dirname(JAVA), exist_ok=True)
    with open(JAVA, "w") as f:
        f.write(java(hechos))
    if a.vistas and not a.solo:
        ims = [Image.open(os.path.join(a.vistas, f"{c.nombre}.png")) for c in hechos]
        w = max(i.width for i in ims)
        todo = Image.new("RGB", (w, sum(i.height for i in ims)))
        y = 0
        for i in ims:
            todo.paste(i, (0, y))
            y += i.height
        todo.save(os.path.join(a.vistas, "todos.png"))


if __name__ == "__main__":
    main()
