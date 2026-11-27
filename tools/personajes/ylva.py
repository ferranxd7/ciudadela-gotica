#!/usr/bin/env python3
"""
Ylva, la hija del ballenero (parte 7an): su piel (128 x 64: lo de un jugador de brazos finos en la mitad izquierda y las piezas
de mas en la derecha), la de su brillo (lo que luce: el farol de fuego frio) y su retrato para los dialogos (64 x 64), en
`textures/entity/ylva.png`, `textures/entity/ylva_brillo.png` y `textures/gui/retratos/ylva.png`.

- **Ylva**: unos cuarenta anos, la cara curtida por el viento y con pecas, los ojos grises de agua, el pelo cobrizo recogido en
  una trenza que le cae por la espalda; el jersey de lana azul marino de los pescadores (con sus ochos), una esclavina corta de
  hule mostaza con costras de sal, pantalones de lana gris metidos en las botas altas de goma negra. Sentada en un noray de
  hierro oxidado al final del espigon del Puerto Varado, con el farol en el regazo: el fuego frio del faro, azul y blanco.

Las piezas de mas (en Java, `client/personajes/ViajeroRenderer.crearYlva`, mismas medidas):
    capa       (64, 0)   9 x 6 x 5    en el cuerpo (0,3 mas grande): la esclavina de hule
    trenza     (96, 0)   2 x 8 x 2    en la cabeza, a la espalda
    espinilla  (64, 12)  4 x 7 x 4    suelta, una por pierna: de la rodilla al suelo (pantalon y bota)
    tapa       (64, 24)  7 x 2 x 7    suelto: lo de arriba del noray (donde se sienta)
    poste      (92, 24)  5 x 6 x 5    suelto: el noray
    farol      (64, 36)  4 x 5 x 4    suelto, en el regazo: el farol (lo que luce, en el brillo)
    asa        (80, 36)  1 x 1 x 1    suelto: el asa del farol

    python3 tools/personajes/ylva.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

PIEL = ["#5a3a2c", "#7a5040", "#9a6c56", "#b88a70", "#d0a88a", "#e2c0a2"]
PECA = "#8e5a3e"
PELO = ["#3e180c", "#5e2812", "#843818", "#a84e22", "#c86a30", "#de8a48"]
JERSEY = ["#121824", "#1a2232", "#232e44", "#2d3a56", "#384868"]
HULE = ["#3e300e", "#5c4614", "#7c601c", "#9a7a26", "#b69432", "#ccac48"]
SAL = "#d8dcd6"
PANTALON = ["#18181a", "#222224", "#2c2c30", "#38383e"]
BOTA = ["#09090b", "#121216", "#1c1c22", "#28282f", "#3a3a44"]
HIERRO = ["#151210", "#221d1a", "#302824", "#3e342e", "#4e423a"]
OXIDO = ["#4a2412", "#6a341a", "#8a4824"]
LATON = ["#3a2a12", "#5e4420", "#8a6830", "#b08c44", "#d0ae62"]
LUZ = ["#1e5a78", "#4c9cc4", "#94d6f0", "#d6f6ff", "#ffffff"]


def _cuerpo_cajas(P):
    P.caja("capa", 64, 0, 9, 6, 5)
    P.caja("trenza", 96, 0, 2, 8, 2)
    P.caja("espinilla", 64, 12, 4, 7, 4)
    P.caja("tapa", 64, 24, 7, 2, 7)
    P.caja("poste", 92, 24, 5, 6, 5)
    P.caja("farol", 64, 36, 4, 5, 4)
    P.caja("asa", 80, 36, 1, 1, 1)


def _farol(cara, brillo):
    """El farol: el marco de laton arriba y abajo, los barrotes en las esquinas y el cristal con el fuego frio dentro."""
    def f(x, y, w, h):
        if cara in ("arriba", "abajo"):
            if brillo:
                return None
            return LATON[3] if (x + y) % 2 == 0 else LATON[2]
        if y == 0 or y == h - 1:
            return None if brillo else (LATON[3] if y == 0 else LATON[1])
        if x in (0, w - 1):
            return None if brillo else LATON[2]
        # el cristal: la llama azul en medio, mas clara abajo
        centro = y == h - 2
        if brillo:
            return LUZ[3] if centro else LUZ[2]
        return LUZ[3] if centro else LUZ[2] if y == 2 else LUZ[1]
    return f


def piel():
    P = Piel(ancho=128, alto=64, brazos=3)
    _cuerpo_cajas(P)
    s = 97
    jersey = tela(JERSEY, s + 1, pliegue=2, grano=0.14)
    hule = ruido(HULE[1:5], s + 2, grano=0.4, base=1.7)
    pantalon = tela(PANTALON, s + 3, pliegue=3, grano=0.12)
    cara_lado = ruido(PIEL[2:5], s + 4, grano=0.22, base=1.0)
    rr = np.random.default_rng(s)
    motas = rr.random((64, 128))

    # ---- la cabeza: el pelo cobrizo con raya, la frente al aire, las pecas, los ojos grises de agua
    C = {"h": PELO[2], "H": PELO[3], "k": PELO[1], "s": PIEL[3], "S": PIEL[4], "o": PIEL[2], "w": "#a8b0aa", "e": "#5a7268",
         "n": PIEL[4], "f": PECA, "r": "#8a4a40", "b": PIEL[1]}
    P.sprite("cabeza", "frente", [
        "kHhhHHhk",
        "hHsSShHh",
        "hsSSSSsh",
        "hweSSewh",
        "osfnnfso",
        "osSfSSso",
        "osSrrSso",
        "koossook",
    ], C)

    def lado(x, y, w, h):
        # el pelo detras y arriba; delante la mejilla y la oreja
        if y <= 1 or x >= 4:
            return PELO[3] if (x * 2 + y) % 5 else PELO[2]
        if (x, y) in ((2, 3), (2, 4)):
            return PIEL[2]  # la oreja
        return cara_lado(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)

    def nuca(x, y, w, h):
        if x in (3, 4):
            return PELO[4] if y % 2 == 0 else PELO[3]  # donde empieza la trenza
        return PELO[3] if (x + y) % 3 else PELO[2]

    P.pintar("cabeza", "espalda", nuca)
    P.pintar("cabeza", "arriba", lambda x, y, w, h: PELO[4] if x == 3 else PELO[3] if (x + y) % 3 else PELO[2])
    P.pintar("cabeza", "abajo", lambda x, y, w, h: PIEL[2])

    # la capa de encima: algunos mechones sueltos a los lados (el viento)
    def mechones(cara):
        def f(x, y, w, h):
            if cara == "frente" and y == 0 and x in (1, 6):
                return PELO[4]
            if cara == "izq" and y in (2, 3) and x == 0:
                return PELO[4]  # un mechon que se lleva el viento
            return None
        return f

    for cara in ("frente", "der", "izq", "espalda", "arriba", "abajo"):
        P.pintar("cabeza_f", cara, mechones(cara))

    # ---- el cuerpo: el jersey de lana azul marino con sus ochos, el cuello vuelto y el elastico de abajo
    def cuerpo(cara):
        def f(x, y, w, h):
            if y == 0:
                return JERSEY[3] if x % 2 else JERSEY[2]  # el cuello
            if y >= h - 2:
                return JERSEY[1] if x % 2 else JERSEY[2]  # el elastico
            c = jersey(x, y, w, h)
            if cara == "frente" and x in (2, 5) and 2 <= y <= 9:
                # los ochos trenzados de los jerseis de pescador
                c = JERSEY[4] if (y + (0 if x == 2 else 2)) % 4 < 2 else JERSEY[3]
            return c
        return f

    for cara in ("frente", "espalda", "der", "izq"):
        P.pintar("cuerpo", cara, cuerpo(cara))
    P.pintar("cuerpo", "arriba", lambda x, y, w, h: JERSEY[2])
    P.pintar("cuerpo", "abajo", lambda x, y, w, h: JERSEY[1])
    for cara in ("frente", "espalda", "der", "izq", "arriba", "abajo"):
        P.pintar("cuerpo_f", cara, lambda x, y, w, h: None)

    # la esclavina de hule mostaza: costuras, el borde mas oscuro y costras de sal
    def capa(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return None
            if cara == "frente" and x == 4 and y >= 1:
                return HULE[0]  # donde se cierra
            if cara == "frente" and x == 3 and y in (2, 4):
                return LATON[3]  # los botones
            c = hule(x, y, w, h)
            if cara != "arriba" and y == h - 1:
                c = HULE[1]
            if motas[(y * 5 + len(cara)) % 64, (x * 7 + 3) % 128] > 0.93:
                c = SAL
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("capa", cara, capa(cara))

    # ---- los brazos: las mangas del jersey (con el elastico en la muneca) y las manos
    def brazo(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return PIEL[2]
            if cara == "arriba":
                return JERSEY[3]
            if y >= 10:
                return PIEL[3] if (x + y) % 3 else PIEL[2]
            if y == 9:
                return JERSEY[1]
            return jersey(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("brazo_d", cara, brazo(cara))
        P.pintar("brazo_i", cara, brazo(cara))
        P.pintar("brazo_d_f", cara, lambda x, y, w, h: None)
        P.pintar("brazo_i_f", cara, lambda x, y, w, h: None)

    # ---- las piernas (sentada: los muslos): el pantalon de lana gris
    def pierna(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return PANTALON[0]
            return pantalon(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("pierna_d", cara, pierna(cara))
        P.pintar("pierna_i", cara, pierna(cara))
        P.pintar("pierna_d_f", cara, lambda x, y, w, h: None)
        P.pintar("pierna_i_f", cara, lambda x, y, w, h: None)

    # la espinilla: lo que asoma del pantalon y la bota alta de goma con la vuelta y la suela
    def espinilla(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                return PANTALON[2]
            if cara == "abajo":
                return BOTA[0]
            if y == 0:
                return pantalon(x, y, w, h)
            if y == 1:
                return BOTA[3] if x % 2 else BOTA[4]  # la vuelta de la bota
            if y == h - 1:
                return BOTA[0]
            c = BOTA[2] if (x + y) % 4 else BOTA[1]
            if motas[(y * 3 + 7) % 64, (x * 5 + len(cara) * 9) % 128] > 0.9:
                c = mezcla(c, SAL, 0.6)
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("espinilla", cara, espinilla(cara))

    # ---- la trenza cobriza, atada abajo con un cordel
    def trenza(x, y, w, h):
        if y == h - 2:
            return "#2a1a10"
        if y == h - 1:
            return PELO[3]
        return PELO[4] if (x + y) % 2 == 0 else PELO[2]

    P.todas("trenza", trenza)

    # ---- el noray de hierro oxidado
    def tapa(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                c = HIERRO[3] if (x + y) % 3 else HIERRO[4]
                if motas[(x * 3 + 11) % 64, (y * 7 + 5) % 128] > 0.82:
                    c = OXIDO[1]
                return c
            if cara == "abajo":
                return HIERRO[0]
            return HIERRO[2] if y == 0 else HIERRO[1]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("tapa", cara, tapa(cara))

    def poste(x, y, w, h):
        c = HIERRO[2] if (x + y // 2) % 3 else HIERRO[1]
        if motas[(y * 5 + 2) % 64, (x * 9 + 31) % 128] > 0.75 and y < h - 1:
            c = OXIDO[(x + y) % 3]  # los chorretones de oxido
        if y == h - 1:
            c = HIERRO[0]
        return c

    P.todas("poste", poste)

    # ---- el farol y su asa
    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("farol", cara, _farol(cara, False))
    P.todas("asa", lambda x, y, w, h: LATON[3])
    return P


def brillo():
    """Lo que luce (se dibuja aparte, con luz propia): solo el cristal del farol."""
    P = Piel(ancho=128, alto=64, brazos=3)
    _cuerpo_cajas(P)
    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("farol", cara, _farol(cara, True))
    return P


# ---------------------------------------------------------------- el retrato

def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato():
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(101)
    grano = r.random((H, W))
    # ---- el fondo: el ocaso sobre el puerto seco, el faro de hueso a la derecha con su fuego frio, y la luz del farol abajo
    for y in range(H):
        for x in range(W):
            t = y / H
            c = mezcla("#120e22", "#3a2a2a", t * 0.8)
            if y > 30:
                c = mezcla(c, "#5a3a22", (y - 30) / 34 * 0.35)
            d = math.hypot((x - 32) / 30, (y - 70) / 22)
            c = mezcla(c, LUZ[1], max(0.0, 1 - d) * 0.55)
            if grano[y, x] < 0.04:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    # el faro, a lo lejos a la derecha: la torre clara de hueso y su linterna azul
    for y in range(10, 44):
        for x in range(52, 61):
            ancho = 2.2 + (y - 10) * 0.05
            if abs(x + 0.5 - 56.5) <= ancho:
                c = "#4a4440" if (y // 4) % 2 else "#3a3532"
                if x + 0.5 > 56.5 + ancho - 1.2:
                    c = mezcla(c, "#000000", 0.4)
                L.px(x, y, mezcla(L.leer(x, y), c, 0.8))
    for y in range(5, 10):
        for x in range(54, 60):
            L.px(x, y, mezcla(L.leer(x, y), LUZ[3] if 6 <= y <= 8 and 55 <= x <= 58 else LUZ[1], 0.75))
    for x in range(53, 61):
        L.px(x, 4, mezcla(L.leer(x, 4), "#2a2624", 0.8))
    # el halo frio del faro
    for y in range(0, 18):
        for x in range(44, 64):
            d = math.hypot(x + 0.5 - 56.5, y + 0.5 - 7.5)
            if 3.5 < d < 9:
                L.px(x, y, mezcla(L.leer(x, y), LUZ[1], (1 - (d - 3.5) / 5.5) * 0.25))
    cx, cy = 30, 27
    # ---- los hombros: la esclavina de hule mostaza sobre el jersey azul, con sal
    for y in range(39, H):
        ancho = 20 + (y - 39) * 0.75
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            if abs(dx) < 3.2 and y < 46:
                c = _tono(JERSEY, 2.6 - (y - 39) * 0.1)  # el cuello del jersey
            else:
                c = _tono(HULE, 3.3 - abs(dx) / ancho * 1.6 + (grano[y, x] - 0.5) * 1.2)
                if grano[y, x] > 0.988:
                    c = mezcla(c, SAL, 0.7)  # costras de sal, pocas
            # la luz azul del farol desde abajo
            c = mezcla(c, LUZ[2], max(0.0, (y - 48) / 16) * 0.35)
            L.px(x, y, c)
    for y in (48, 53):
        L.px(cx - 1, y, LATON[4])
    # ---- la trenza cobriza que le cae por el hombro izquierdo (a la derecha del retrato)
    for y in range(30, 58):
        x = int(round(41 + (y - 30) * 0.12))
        for dx in range(-2, 2):
            k = (y + dx) % 3
            L.px(x + dx, y, PELO[4] if k == 0 else PELO[3] if k == 1 else PELO[2])
    for dx in range(-2, 2):
        L.px(44 + dx, 57, "#2a1a10")
    # ---- la cara: curtida, con pecas, alumbrada desde abajo por el fuego frio
    rx, ry = 9.8, 12.2
    for y in range(10, 41):
        for x in range(W):
            e = _e(cx, cy, rx, ry, x, y)
            if e > 1:
                continue
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            t = 3.0 - dy / 14.0 * 0.2 - dx / 12.0 * 0.4 - max(0.0, e - 0.7) * 2.0
            c = _tono(PIEL, t)
            # la luz fria desde abajo: la barbilla, la nariz y los pomulos azulados
            c = mezcla(c, LUZ[2], max(0.0, dy / ry) * 0.3)
            if grano[y, x] < 0.05 and 18 < y < 34:
                c = mezcla(c, PECA, 0.7)  # las pecas
            L.px(x, y, c)
    # los ojos grises de agua, cansados, mirando un poco arriba (al norte, a las costillas)
    for (ex, lado) in ((25, -1), (35, 1)):
        for x in range(ex - 2, ex + 3):
            L.px(x, 23, PIEL[2])
        L.px(ex - 1, 25, "#b8c4c0").px(ex, 25, "#4e6a62").px(ex + 1, 25, "#2a3a36")
        L.px(ex, 24, "#3a4a46")
        L.px(ex - 2, 26, PIEL[2]).px(ex + 2, 26, PIEL[2])
    # la nariz, la boca apretada y la arruga del ceno
    for y in range(25, 31):
        L.px(cx, y, PIEL[4]).px(cx + 1, y, PIEL[2])
    L.px(cx - 1, 30, PIEL[1]).px(cx + 2, 30, PIEL[1])
    for x in range(cx - 3, cx + 5):
        L.px(x, 34, "#7a3a34" if abs(x - cx - 0.5) < 3 else PIEL[2])
    L.px(cx, 20, PIEL[2]).px(cx + 1, 20, PIEL[2])
    # ---- el pelo cobrizo: la raya en medio, recogido detras, mechones que se lleva el viento
    for y in range(4, 34):
        for x in range(W):
            dentro = _e(cx, cy - 2, rx + 2.6, ry + 2.2, x, y) <= 1
            cara = _e(cx, cy + 2, rx - 0.6, ry - 1.5, x, y) <= 1 and y >= 17
            if not dentro or cara:
                continue
            dx = x + 0.5 - cx
            if abs(dx) < 0.8 and y < 14:
                c = PIEL[2]  # la raya
            else:
                k = 3.2 - (y - 4) * 0.03 - dx * 0.04 + (grano[y, x] - 0.5) * 1.2 + (0.6 if (x + y) % 4 == 0 else 0)
                c = _tono(PELO, k)
            L.px(x, y, c)
    # mechones sueltos sobre la frente y la mejilla
    for (x, y) in ((22, 17), (21, 18), (21, 19), (20, 20), (38, 16), (39, 17), (39, 18)):
        L.px(x, y, PELO[4])
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "ylva.png"))
    brillo().guardar(os.path.join(ASSETS, "textures", "entity", "ylva_brillo.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "ylva.png"))
    print("ylva: piel, brillo y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        p = piel()
        p.vista().save(os.path.join(d, "ylva_piel.png"))
        p.L.imagen().resize((128 * 4, 64 * 4), 0).save(os.path.join(d, "ylva_textura.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "ylva_retrato.png"))
        print("vistas en", d)
