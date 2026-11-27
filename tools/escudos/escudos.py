#!/usr/bin/env python3
"""
Los escudos (parte 7ak, respuesta 109 del plan: "escudos pequeno, mediano y grande"): la Rodela de Hierro (pequena y redonda), el
Escudo de Caballero (mediano, de punta, con la corona de Valdrenn) y el Paves de la Guardia (grande, hasta el suelo). Cada uno es un
modelo de cajas como el escudo de Minecraft (una placa de un pixel de grueso por delante, en z -2..-1, y el asa por detras, en el
mismo sitio que la del juego: asi le valen las posturas de la mano y del bloqueo del escudo del juego). Sale:

  - la textura de cada escudo          textures/entity/escudos/<id>.png
  - el codigo Java de la geometria     client/escudos/<Nombre>Geometria.java (GENERADO)
  - los modelos de objeto              models/item/<id>.json y <id>_blocking.json (builtin/entity: lo dibuja EscudoRenderer)

y con --vista, una hoja con cada escudo de frente, de tres cuartos y por detras.

El frente de cada placa se pinta con coordenadas de todo el escudo (no de cada caja): asi el aro de la rodela, el filo del escudo
de caballero o las bandas del paves siguen la forma entera aunque esten hechos de varias cajas. Coordenadas de los modelos de
Minecraft (pixeles, Y hacia abajo, el frente a -Z); en el juego la textura sale derecha (el dibujante da la vuelta en Y y en Z).

    python3 tools/escudos/escudos.py [--vista carpeta]
"""
import json
import math
import os
import sys

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, os.path.join(AQUI, "..", "enemigos"))

import vista  # noqa: E402
from modelo import Box, Rig, java_geometria  # noqa: E402
from pinta import Textura, hexa, mezcla, tono  # noqa: E402

REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
RES = os.path.join(REPO, "src/main/resources/assets/darkcraft")
TEX_DIR = os.path.join(RES, "textures/entity/escudos")
MOD_DIR = os.path.join(RES, "models/item")
JAVA_DIR = os.path.join(REPO, "src/main/java/com/darkcraft/client/escudos")
HERRAMIENTA = "tools/escudos/escudos.py"

HIERRO = [hexa(c) for c in ("#1c1d22", "#2e3038", "#464954", "#62666f", "#868a94", "#b0b4bc", "#dadde2")]
ACERO = [hexa(c) for c in ("#22252c", "#3a3f4a", "#585e6c", "#7c8392", "#a4abb8", "#cdd2dc", "#eef0f4")]
MADERA = [hexa(c) for c in ("#2a1a10", "#3d2717", "#55361f", "#6e4728", "#885a33", "#a06d40")]
MADERA_OSC = [hexa(c) for c in ("#1e130c", "#2c1c11", "#3c2717", "#4e331e", "#614126")]
CUERO = [hexa(c) for c in ("#2a170d", "#402515", "#5a361f", "#744829", "#8c5a34")]
ROJO = [hexa(c) for c in ("#3a0a0e", "#5c1016", "#7e1820", "#9e222a", "#bc3036", "#d8484a")]
GRANATE = [hexa(c) for c in ("#26070a", "#3c0c10", "#561218", "#701a20", "#88242a")]
ORO = [hexa(c) for c in ("#5a3c10", "#8a6420", "#b88a34", "#dcb04e", "#f4d27a", "#fff0b8")]
NEGRO = [hexa(c) for c in ("#0e0d10", "#18171c", "#232228", "#302e36", "#3e3c46")]


def ruido(semilla):
    return np.random.default_rng(semilla).random((64, 64))


# ---------------------------------------------------------------- un escudo: cajas y como se pinta cada una

class Escudo:
    def __init__(self, ident, clase, ancho=64):
        self.id = ident
        self.clase = clase
        self.rig = Rig(ident, ancho)
        self.rig.pieza("escudo", "", (0, 0, 0))
        self.pintores = []    # (caja, pintor)
        self.placa = set()    # los pixeles (x, y) del frente de la placa (para saber el borde)

    def caja(self, x, y, z, w, h, d, pintor, mat="placa", placa=False):
        b = Box(x, y, z, w, h, d, f"{mat}_{len(self.pintores)}")
        self.rig.por_nombre["escudo"].boxes.append(b)
        self.pintores.append((b, pintor))
        if placa:
            for yy in range(int(round(y)), int(round(y + h))):
                for xx in range(int(round(x)), int(round(x + w))):
                    self.placa.add((xx, yy))
        return b

    def borde(self, x, y, n=1):
        """A cuantos pixeles (1..n) esta (x, y) del borde de la placa, o 0 si esta mas adentro."""
        for k in range(1, n + 1):
            for dx, dy in ((k, 0), (-k, 0), (0, k), (0, -k), (k, k), (-k, -k), (k, -k), (-k, k)):
                if (x + dx, y + dy) not in self.placa:
                    return k
        return 0

    def textura(self):
        self.rig.empaquetar()
        t = Textura(self.rig)
        for b, f in self.pintores:
            t.pintar_caja(b, f)
        return t


def placa(esc, frente, canto, espalda):
    """El pintor de una caja de la placa: el frente con coordenadas del escudo; los cantos y la espalda, los suyos."""
    def hacer(b):
        def f(cara, x, y, w, h):
            if cara == "frente":
                return frente(int(math.floor(b.x + x)), int(math.floor(b.y + y)))
            if cara == "espalda":
                return espalda(int(math.floor(b.x + w - 1 - x)), int(math.floor(b.y + y)))
            return canto(cara, x, y, w, h)
        return f
    return hacer


def losas(esc, filas, frente, canto, espalda):
    """La placa hecha de franjas: filas = [(y0, y1, x0, x1), ...] (sin solaparse: dos caras en el mismo plano parpadean),
    toda en z -2..-1."""
    pint = placa(esc, frente, canto, espalda)
    for y0, y1, x0, x1 in filas:
        b = esc.caja(x0, y0, -2, x1 - x0, y1 - y0, 1, None, placa=True)
        esc.pintores[-1] = (b, pint(b))


def asa(esc, tonos=CUERO, semilla=3):
    """El asa por detras (en el sitio del asa del escudo del juego), de cuero con su vuelta de cuerda."""
    r = ruido(semilla)

    def f(cara, x, y, w, h):
        v = 0.5 + (r[y % 64, (x * 3) % 64] - 0.5) * 0.3
        if y % 2 == 0:
            v -= 0.18
        if cara == "abajo":
            v -= 0.2
        return tono(tonos, v)

    esc.caja(-1, -3, -1, 2, 6, 6, f, mat="asa")


def madera_atras(tonos=MADERA_OSC, semilla=5):
    r = ruido(semilla)

    def f(x, y):
        v = 0.45 + (r[(y * 2) % 64, (x + 32) % 64] - 0.5) * 0.25
        if (x + 64) % 4 == 0:
            v -= 0.22
        return tono(tonos, v)
    return f


# ---------------------------------------------------------------- la rodela (pequena, redonda)

def rodela():
    esc = Escudo("rodela", "RodelaGeometria")
    r = ruido(11)
    filas = [(-6, -5, -2, 2), (-5, -4, -4, 4), (-4, -2, -5, 5), (-2, 2, -6, 6), (2, 4, -5, 5), (4, 5, -4, 4), (5, 6, -2, 2)]

    def frente(x, y):
        cx, cy = x + 0.5, y + 0.5
        rr = math.hypot(cx, cy)
        b = esc.borde(x, y, 1)
        if b == 1 or rr > 5.1:
            # el aro de hierro: claro arriba a la izquierda
            luz = 0.55 + 0.25 * (-cx - cy) / max(rr, 0.1) * 0.6
            return tono(HIERRO, luz + (r[y % 64, x % 64] - 0.5) * 0.08)
        # ocho clavos en un anillo
        for k in range(8):
            a = math.radians(k * 45 + 22.5)
            if math.hypot(cx - 3.6 * math.cos(a), cy - 3.6 * math.sin(a)) < 0.72:
                return tono(HIERRO, 0.86)
        # tablas de nogal en vertical
        tabla = int(math.floor((cx + 6) / 3))
        v = 0.5 + (0.12 if tabla % 2 else -0.04) + (r[(y * 3) % 64, (x + 7) % 64] - 0.5) * 0.22
        if (x + 6) % 3 == 0:
            v -= 0.25
        v += 0.1 * (-cx - cy) / 12.0
        return tono(MADERA, v)

    def canto(cara, x, y, w, h):
        return tono(HIERRO, 0.62 if cara in ("arriba", "der") else 0.38)

    losas(esc, filas, frente, canto, madera_atras())

    # el umbo: un cuenco de hierro en el centro, con su punta
    def umbo(cara, x, y, w, h):
        if cara == "frente":
            cx, cy = x + 0.5 - w / 2, y + 0.5 - h / 2
            return tono(HIERRO, 0.66 - 0.12 * (cx + cy) / max(w, 1) * 2 + (0.12 if (x, y) == (1, 1) else 0))
        return tono(HIERRO, 0.7 if cara in ("arriba", "der") else 0.42)

    esc.caja(-2, -2, -4, 4, 4, 2, umbo, mat="umbo")
    esc.caja(-1, -1, -5, 2, 2, 1, lambda cara, x, y, w, h: tono(HIERRO, 0.9 if cara == "frente" and (x, y) == (0, 0) else 0.72), mat="punta")
    asa(esc)
    return esc


# ---------------------------------------------------------------- el escudo de caballero (mediano, de punta)

CORONA = [
    "X..X..X",
    "X.XXX.X",
    "XXXXXXX",
    "XRXRXRX",
    "XXXXXXX",
]


def caballero():
    esc = Escudo("escudo_caballero", "EscudoCaballeroGeometria")
    r = ruido(23)
    filas = [(-11, 3, -6, 6), (3, 6, -5, 5), (6, 8, -4, 4), (8, 10, -3, 3), (10, 11, -2, 2), (11, 12, -1, 1)]
    cw = len(CORONA[0])

    def frente(x, y):
        b = esc.borde(x, y, 2)
        cx, cy = x + 0.5, y + 0.5
        if b == 1:
            # el filo de acero
            return tono(ACERO, 0.58 + 0.2 * (-cx - cy) / 18.0 + (0.12 if y == -11 else 0))
        if b == 2:
            return tono(GRANATE, 0.35)
        # la corona de Valdrenn en oro, arriba
        ox, oy = x + cw // 2, y + 7
        if 0 <= oy < len(CORONA) and 0 <= ox < cw:
            ch = CORONA[oy][ox]
            if ch == "X":
                return tono(ORO, 0.62 - oy * 0.05 + (0.18 if ox < cw // 2 and oy < 3 else 0))
            if ch == "R":
                return tono(ROJO, 0.95)
        # el campo rojo, liso, con la luz de arriba a la izquierda y un brillo a lo alto en el primer tercio (es curvo)
        v = 0.6 + 0.2 * (-cx - cy) / 22.0 + (r[(y + 11) % 64, (x + 6) % 64] - 0.5) * 0.05
        if x == -3:
            v += 0.12
        # un filo dorado bajo la corona y otro en la punta (lo que se pinto con el oro que sobro)
        if y == 1 and abs(cx) < 4.6:
            return tono(ORO, 0.48)
        return tono(ROJO, v)

    def canto(cara, x, y, w, h):
        return tono(ACERO, 0.66 if cara in ("arriba", "der") else 0.4)

    losas(esc, filas, frente, canto, madera_atras(semilla=7))
    asa(esc)
    return esc


# ---------------------------------------------------------------- el paves de la guardia (grande, hasta el suelo)

def paves():
    esc = Escudo("paves", "PavesGeometria")
    r = ruido(37)
    filas = [(-13, -12, -6, 6), (-12, 12, -7, 7), (12, 13, -6, 6)]
    bandas = (-9, -1, 7)

    def frente(x, y):
        b = esc.borde(x, y, 1)
        cx, cy = x + 0.5, y + 0.5
        if b == 1:
            return tono(HIERRO, 0.5 + 0.2 * (-cx - cy) / 24.0)
        for by in bandas:
            if by <= y < by + 2:
                # las bandas de hierro negro, con sus remaches
                if y == by and (x + 7) % 4 == 1:
                    return tono(ORO, 0.7)
                return tono(NEGRO, 0.55 if y == by else 0.3)
        # el campo granate, en tablas a lo alto, con la luz de arriba a la izquierda
        v = 0.55 + 0.16 * (-cx - cy) / 26.0 + (r[(y + 13) % 64, (x + 7) % 64] - 0.5) * 0.14
        if (x + 7) % 5 == 0:
            v -= 0.2
        return tono(GRANATE, v)

    def canto(cara, x, y, w, h):
        return tono(HIERRO, 0.6 if cara in ("arriba", "der") else 0.36)

    losas(esc, filas, frente, canto, madera_atras(semilla=9))

    # el lomo de hierro de arriba abajo y el pincho del pie (para clavarlo en el suelo)
    def lomo(cara, x, y, w, h):
        if cara == "frente":
            return tono(HIERRO, (0.78 if x == 0 else 0.5) + (0.08 if y % 8 == 0 else 0))
        return tono(HIERRO, 0.62 if cara in ("izq", "arriba") else 0.4)

    esc.caja(-1, -12, -3, 2, 24, 1, lomo, mat="lomo")
    esc.caja(-1, 13, -2, 2, 2, 1, lambda cara, x, y, w, h: tono(HIERRO, 0.7 - y * 0.25), mat="pincho")
    asa(esc)
    return esc


ESCUDOS = (rodela, caballero, paves)


# ---------------------------------------------------------------- los modelos de objeto (los del escudo del juego, a su tamano)

def modelos(ident, escala_gui, escala_mano):
    gui = {"rotation": [15, -25, -5], "translation": [2, 3, 0], "scale": [escala_gui] * 3}
    normal = {
        "parent": "builtin/entity",
        "gui_light": "front",
        "textures": {"particle": "block/dark_oak_planks"},
        "display": {
            "thirdperson_righthand": {"rotation": [0, 90, 0], "translation": [10, 6, -4], "scale": [1, 1, 1]},
            "thirdperson_lefthand": {"rotation": [0, 90, 0], "translation": [10, 6, 12], "scale": [1, 1, 1]},
            "firstperson_righthand": {"rotation": [0, 180, 5], "translation": [-10, 2, -10], "scale": [escala_mano] * 3},
            "firstperson_lefthand": {"rotation": [0, 180, 5], "translation": [10, 0, -10], "scale": [escala_mano] * 3},
            "gui": gui,
            "fixed": {"rotation": [0, 180, 0], "translation": [-4.5, 4.5, -5], "scale": [0.55, 0.55, 0.55]},
            "ground": {"rotation": [0, 0, 0], "translation": [2, 4, 2], "scale": [0.25, 0.25, 0.25]},
        },
        "overrides": [{"predicate": {"blocking": 1}, "model": f"darkcraft:item/{ident}_blocking"}],
    }
    cubierto = {
        "parent": "builtin/entity",
        "gui_light": "front",
        "textures": {"particle": "block/dark_oak_planks"},
        "display": {
            "thirdperson_righthand": {"rotation": [45, 135, 0], "translation": [3.51, 11, -2], "scale": [1, 1, 1]},
            "thirdperson_lefthand": {"rotation": [45, 135, 0], "translation": [13.51, 3, 5], "scale": [1, 1, 1]},
            "firstperson_righthand": {"rotation": [0, 180, -5], "translation": [-15, 5, -11], "scale": [escala_mano] * 3},
            "firstperson_lefthand": {"rotation": [0, 180, -5], "translation": [5, 5, -11], "scale": [escala_mano] * 3},
            "gui": gui,
        },
    }
    return normal, cubierto


ESCALAS = {"rodela": (0.8, 1.25), "escudo_caballero": (0.65, 1.25), "paves": (0.54, 1.12)}


def main():
    vista_dir = sys.argv[sys.argv.index("--vista") + 1] if "--vista" in sys.argv else None
    os.makedirs(TEX_DIR, exist_ok=True)
    os.makedirs(JAVA_DIR, exist_ok=True)
    hojas = []
    for hacer in ESCUDOS:
        esc = hacer()
        tex = esc.textura()
        tex.guardar(os.path.join(TEX_DIR, esc.id + ".png"))
        with open(os.path.join(JAVA_DIR, esc.clase + ".java"), "w") as f:
            f.write(java_geometria(esc.rig, "com.darkcraft.client.escudos", esc.clase, HERRAMIENTA))
        normal, cubierto = modelos(esc.id, *ESCALAS[esc.id])
        for nombre, m in ((esc.id, normal), (esc.id + "_blocking", cubierto)):
            with open(os.path.join(MOD_DIR, nombre + ".json"), "w") as f:
                json.dump(m, f, indent=2)
                f.write("\n")
        print(f"{esc.id}: {sum(1 for _ in esc.rig.cajas())} cajas, textura {esc.rig.tex_w} x {esc.rig.tex_h}")
        if vista_dir:
            imgs = []
            for yaw, pitch in ((180, 0), (215, -12), (0, 0), (270, 0)):
                im = vista.render(esc.rig, tex.a, yaw=yaw, pitch=pitch, size=(200, 260), zoom=260 / 2.2, centro_y=1.5, suelo=False)
                imgs.append(Image.fromarray(im))
            hoja = Image.new("RGB", (len(imgs) * 204, 264), (46, 44, 52))
            for i, im in enumerate(imgs):
                hoja.paste(im, (2 + i * 204, 2))
            hojas.append(hoja)
            tex.imagen().resize((esc.rig.tex_w * 4, esc.rig.tex_h * 4), Image.NEAREST).save(os.path.join(vista_dir, esc.id + "_textura.png"))
    if vista_dir:
        os.makedirs(vista_dir, exist_ok=True)
        todo = Image.new("RGB", (hojas[0].width, sum(h.height for h in hojas)), (46, 44, 52))
        y = 0
        for h in hojas:
            todo.paste(h, (0, y))
            y += h.height
        todo.save(os.path.join(vista_dir, "escudos.png"))


if __name__ == "__main__":
    main()
