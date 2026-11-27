#!/usr/bin/env python3
"""
Hedda, la madre de la Puerta Oeste (parte 7ah): su piel (128 x 64: lo de un jugador de brazos finos en la mitad izquierda y las
piezas de mas en la derecha) y su retrato para los dialogos (64 x 64), en `textures/entity/hedda.png` y
`textures/gui/retratos/hedda.png`.

- **Hedda**: vieja, la cara arrugada y cansada, mechones grises que asoman de un panuelo negro de luto anudado; el chal de lana
  parda con flecos sobre los hombros, el vestido de lana azul oscura hasta los pies con el delantal de lino gastado; zapatos
  negros; teje una bufanda roja (la labor y el ovillo, de la misma lana roja que la de su hijo) con dos agujas de madera,
  sentada en un taburete de madera vieja.

Las piezas de mas (en Java, `client/personajes/ViajeroRenderer.crearHedda`, mismas medidas):
    chal      (64, 0)   9 x 7 x 5    en el cuerpo (0,3 mas grande)
    faldon    (64, 12)  9 x 10 x 2   suelto: la falda que cae de las rodillas al suelo
    zapatos   (88, 12)  7 x 1 x 3    suelto, delante del faldon
    taburete  (64, 26)  9 x 2 x 8    suelto: el asiento
    pata      (98, 26)  1 x 6 x 1    suelto: las cuatro patas
    ovillo    (102, 26) 3 x 3 x 3    suelto, en el suelo a su derecha
    aguja     (114, 26) 1 x 1 x 6    en cada brazo
    labor     (64, 38)  4 x 5 x 1    en el brazo derecho: lo que lleva tejido

    python3 tools/personajes/hedda.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

PIEL = ["#4e3428", "#6e4c3c", "#8e6a56", "#aa866e", "#c4a088", "#d8b8a0"]
PELO = ["#5a5650", "#7e7a72", "#a29e96", "#c4c0b8", "#dedad2"]
PANUELO = ["#0c0a0e", "#151217", "#1e1a21", "#28232c", "#332d38"]
CHAL = ["#1c1714", "#28211c", "#352c25", "#433830", "#52453b", "#62543f"]
VESTIDO = ["#11131b", "#181b26", "#202433", "#2a2f41", "#353b50"]
DELANTAL = ["#3e3a34", "#544e46", "#6a6358", "#7e776a", "#92897a"]
MADERA = ["#24180e", "#352416", "#48321e", "#5c4128", "#6e5030"]
LANA = ["#3e0b0d", "#5e1214", "#82191b", "#a52626", "#c43a34", "#de5c4c"]
ZAPATO = ["#120c08", "#1e150e", "#2c2016"]
AGUJA = ["#7a5a30", "#a8844a", "#cca86a"]


def piel():
    P = Piel(ancho=128, alto=64, brazos=3)
    P.caja("chal", 64, 0, 9, 7, 5)
    P.caja("faldon", 64, 12, 9, 10, 2)
    P.caja("zapatos", 88, 12, 7, 1, 3)
    P.caja("taburete", 64, 26, 9, 2, 8)
    P.caja("pata", 98, 26, 1, 6, 1)
    P.caja("ovillo", 102, 26, 3, 3, 3)
    P.caja("aguja", 114, 26, 1, 1, 6)
    P.caja("labor", 64, 38, 4, 5, 1)
    s = 83
    vestido = tela(VESTIDO, s + 1, pliegue=3, grano=0.12)
    lana_chal = ruido(CHAL[1:5], s + 2, grano=0.45, base=1.6)
    cara_lado = ruido(PIEL[2:5], s + 3, grano=0.25, base=1.0)

    # ---- la cabeza: el panuelo negro sobre la frente, mechones grises a los lados, los ojos cansados, arrugas
    C = {"k": PANUELO[1], "K": PANUELO[3], "g": PELO[2], "G": PELO[3], "s": PIEL[3], "S": PIEL[4], "o": PIEL[2], "e": "#3a2822",
         "w": "#8e7e70", "n": PIEL[4], "N": PIEL[2], "b": PIEL[1], "r": "#5e302c", "a": PIEL[2]}
    P.sprite("cabeza", "frente", [
        "kKkKKkKk",
        "gkKkkKkg",
        "gsSssSsg",
        "aweSSewa",
        "gsSnnSsg",
        "asoSSosa",
        "asorrosa",
        "aaossoaa",
    ], C)

    def lado(x, y, w, h):
        if y <= 1:
            return PANUELO[2] if (x + y) % 3 else PANUELO[1]
        if x >= 5 and y <= 5:
            return PELO[2] if (x + y) % 2 else PELO[1]
        return cara_lado(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)
    P.pintar("cabeza", "espalda", lambda x, y, w, h: PANUELO[2] if (x * 3 + y) % 5 else PANUELO[3])
    P.pintar("cabeza", "arriba", lambda x, y, w, h: PANUELO[2] if (x + y) % 3 else PANUELO[3])
    P.pintar("cabeza", "abajo", lambda x, y, w, h: PIEL[2])

    # la capa de encima: el panuelo de luto que envuelve la cabeza (abierto en la cara) y el nudo detras
    def panuelo(cara):
        def f(x, y, w, h):
            if cara == "frente":
                if y == 0:
                    return PANUELO[3] if x % 2 else PANUELO[2]
                if x in (0, 7) and y <= 5:
                    return PANUELO[2]
                return None
            if cara in ("der", "izq"):
                return PANUELO[2] if y <= 5 or (cara == "der" and x < 3) or (cara == "izq" and x > 4) else None
            if cara == "espalda":
                if y >= 6 and x in (3, 4):
                    return PANUELO[3]  # el nudo
                return PANUELO[2] if (x + y) % 4 else PANUELO[1]
            if cara == "arriba":
                return PANUELO[2] if (x * 2 + y) % 5 else PANUELO[3]
            return None
        return f

    for cara in ("frente", "der", "izq", "espalda", "arriba", "abajo"):
        P.pintar("cabeza_f", cara, panuelo(cara))

    # ---- el cuerpo: el vestido de lana con el delantal de lino delante (de la cintura abajo)
    def cuerpo(cara):
        def f(x, y, w, h):
            if cara == "frente" and y >= 6 and 1 <= x <= 6:
                if y == 6:
                    return DELANTAL[1]
                return DELANTAL[2] if (x + y // 3) % 3 else DELANTAL[1]
            if cara == "frente" and y == 0 and x in (3, 4):
                return PIEL[3]  # el cuello
            return vestido(x, y, w, h)
        return f

    for cara in ("frente", "espalda", "der", "izq"):
        P.pintar("cuerpo", cara, cuerpo(cara))
    P.pintar("cuerpo", "arriba", lambda x, y, w, h: VESTIDO[2])
    P.pintar("cuerpo", "abajo", lambda x, y, w, h: VESTIDO[1])
    for cara in ("frente", "espalda", "der", "izq", "arriba", "abajo"):
        P.pintar("cuerpo_f", cara, lambda x, y, w, h: None)

    # el chal: lana parda tejida sobre los hombros, abierto delante (se ve el vestido) y con flecos abajo
    def chal(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return None
            if cara == "frente" and 3 <= x <= 5 and y >= 1 + abs(x - 4):
                return None  # el pico abierto del chal
            if cara != "arriba" and y == h - 1:
                return CHAL[2] if x % 2 == 0 else None  # los flecos
            c = lana_chal(x, y, w, h)
            if (x + y) % 4 == 0:
                c = mezcla(c, CHAL[0], 0.35)  # el punto de la lana
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("chal", cara, chal(cara))

    # ---- los brazos: mangas del vestido y las manos viejas
    def brazo(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return PIEL[2]
            if cara == "arriba":
                return VESTIDO[3]
            if y >= 10:
                return PIEL[3] if (x + y) % 3 else PIEL[2]
            if y == 9:
                return VESTIDO[1]
            return vestido(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("brazo_d", cara, brazo(cara))
        P.pintar("brazo_i", cara, brazo(cara))
        P.pintar("brazo_d_f", cara, lambda x, y, w, h: None)
        P.pintar("brazo_i_f", cara, lambda x, y, w, h: None)

    # ---- las piernas: la falda (sentada, lo de arriba es el regazo; el delantal por delante)
    def pierna(cara, derecha):
        def f(x, y, w, h):
            if cara == "abajo":
                return VESTIDO[0]
            if cara in ("frente", "arriba") and (x >= 1 if derecha else x <= 2):
                return DELANTAL[2] if (x + y // 3) % 3 else DELANTAL[1]
            return vestido(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("pierna_d", cara, pierna(cara, True))
        P.pintar("pierna_i", cara, pierna(cara, False))
        P.pintar("pierna_d_f", cara, lambda x, y, w, h: None)
        P.pintar("pierna_i_f", cara, lambda x, y, w, h: None)

    # el faldon: la falda que cae de las rodillas al suelo, con sus pliegues, el delantal y el bajo
    def faldon(cara):
        def f(x, y, w, h):
            if cara == "frente" and 1 <= x <= 7 and y <= 6:
                return DELANTAL[2] if (x + y // 2) % 3 else DELANTAL[1]
            if cara in ("frente", "der", "izq", "espalda") and y == h - 1:
                return VESTIDO[0]
            return vestido(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("faldon", cara, faldon(cara))
    P.todas("zapatos", lambda x, y, w, h: ZAPATO[2] if (x + y) % 3 == 0 else ZAPATO[1])

    # ---- el taburete de madera vieja (asiento y patas)
    P.todas("taburete", lambda x, y, w, h: MADERA[3] if (x * 3 + y) % 7 else MADERA[2] if y else MADERA[4])
    P.pintar("taburete", "abajo", lambda x, y, w, h: MADERA[1])
    P.todas("pata", lambda x, y, w, h: MADERA[2] if y % 3 else MADERA[1])

    # ---- la lana roja: el ovillo (vueltas de hebra), las agujas y la labor tejida
    P.todas("ovillo", lambda x, y, w, h: LANA[3] if (x + y) % 2 else LANA[4] if (x * y) % 3 else LANA[2])
    P.todas("aguja", lambda x, y, w, h: AGUJA[2] if x == 0 and y == 0 else AGUJA[1])

    def labor(x, y, w, h):
        if y == 0:
            return LANA[4]
        return LANA[3] if (x + y) % 2 == 0 else LANA[2] if y % 2 else LANA[4]

    P.todas("labor", labor)
    return P


# ---------------------------------------------------------------- el retrato

def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato():
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(89)
    grano = r.random((H, W))
    farol = ["#6a3a10", "#b0681a", "#e8a440", "#ffd888"]
    # ---- el fondo: el ocaso de la capital quemada, humo, y el farol a la izquierda que la alumbra
    for y in range(H):
        for x in range(W):
            t = y / H
            c = mezcla("#120a0e", "#2e1612", t * 0.9)
            d = math.hypot((x - 2) / 26, (y - 50) / 24)
            c = mezcla(c, farol[1], max(0.0, 1 - d) * 0.55)
            if grano[y, x] < 0.05:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    # la silueta de la Puerta Oeste al fondo, a la derecha
    for y in range(6, 40):
        for x in range(46, 64):
            if (x < 50 or x > 58 or y < 14) and not (x > 60 and y < 10):
                L.px(x, y, mezcla(L.leer(x, y), "#0a0608", 0.75))
    for x in range(50, 59):
        L.px(x, 14, "#0a0608")
    cx, cy = 30, 28
    # ---- los hombros: el chal de lana parda con flecos sobre el vestido azul oscuro
    for y in range(40, H):
        ancho = 21 + (y - 40) * 0.7
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            pico = abs(dx) < (y - 41) * 0.45  # el pico abierto del chal: se ve el vestido
            if pico:
                c = _tono(["#11131b", "#181b26", "#202433", "#2a2f41"], 2.4 - abs(dx) / 8)
            else:
                c = _tono(CHAL, 3.2 - abs(dx) / ancho * 1.8 + (grano[y, x] - 0.5) * 1.4)
                if (x + y) % 4 == 0:
                    c = mezcla(c, CHAL[0], 0.3)
            if dx < -ancho + 4:
                c = mezcla(c, farol[2], 0.25)
            L.px(x, y, c)
    # ---- la bufanda roja en las manos, abajo (la labor), con sus puntos
    for y in range(54, H):
        for x in range(18, 44):
            if _e(31, 62, 13, 6.5, x, y) <= 1:
                c = LANA[3] if (x + y) % 2 == 0 else LANA[2] if y % 2 else LANA[4]
                if x < 24:
                    c = mezcla(c, farol[3], 0.2)
                L.px(x, y, c)
    for (x, y) in ((22, 56), (23, 56), (24, 57), (40, 55), (41, 56), (42, 56)):
        L.px(x, y, AGUJA[1])
    # las manos, nudosas, sobre la labor
    for (hx, hy) in ((23, 59), (39, 59)):
        for y in range(hy - 2, hy + 3):
            for x in range(hx - 3, hx + 4):
                if _e(hx, hy, 2.6, 2.0, x, y) <= 1:
                    L.px(x, y, _tono(PIEL, 2.6 - (y - hy + 2) * 0.3 + (0.6 if hx < 30 else 0)))
    # ---- la cara: vieja, arrugada, los ojos cansados y mojados, alumbrada por el farol desde la izquierda
    rx, ry = 10.4, 12.6
    for y in range(10, 42):
        for x in range(W):
            e = _e(cx, cy, rx, ry, x, y)
            if e > 1:
                continue
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            t = 3.0 - dy / 14.0 * 0.3 - dx / 12.0 * 0.55 - max(0.0, e - 0.7) * 2.0
            c = _tono(PIEL, t)
            if dx < -rx + 4:
                c = mezcla(c, farol[2], 0.3)
            if grano[y, x] < 0.035:
                c = mezcla(c, PIEL[2], 0.4)  # manchas de la edad
            L.px(x, y, c)
    # las arrugas: la frente, las patas de gallo, los surcos de la boca
    for x in range(cx - 6, cx + 7, 2):
        L.px(x, 20, PIEL[2])
    for (x, y) in ((21, 26), (20, 27), (39, 26), (40, 27), (24, 33), (25, 34), (25, 35), (36, 33), (35, 34), (35, 35)):
        L.px(x, y, PIEL[1])
    # los ojos: los parpados caidos, la mirada baja, y una lagrima que brilla
    for (ex, lado) in ((25, -1), (35, 1)):
        for x in range(ex - 2, ex + 3):
            L.px(x, 25, PIEL[2])
        for x in range(ex - 2, ex + 2):
            L.px(x, 26, PIEL[1])
        L.px(ex - 1, 27, "#9a8c7c").px(ex, 27, "#24180f").px(ex + 1, 27, "#3a2a20")
        L.px(ex, 28, PIEL[2])
    L.px(24, 29, "#d8e4ee")  # la lagrima
    L.px(24, 30, "#b8c8d8")
    # la nariz y la boca apretada
    for y in range(26, 32):
        L.px(cx, y, PIEL[4]).px(cx + 1, y, PIEL[2])
    L.px(cx - 1, 31, PIEL[1]).px(cx + 2, 31, PIEL[1])
    for x in range(cx - 3, cx + 5):
        L.px(x, 35, "#6a3832" if abs(x - cx - 0.5) < 3 else PIEL[1])
    # ---- el pelo gris que asoma bajo el panuelo, a los lados de la frente
    for y in range(17, 30):
        for (x0, x1) in ((19, 22), (38, 41)):
            for x in range(x0, x1):
                if _e(cx, cy, rx + 0.8, ry, x, y) <= 1 and (x + y) % 3:
                    L.px(x, y, _tono(PELO, 3 - (y - 17) * 0.12 + (0.8 if x < 25 else 0)))
    # ---- el panuelo negro de luto, ceñido a la frente y que cae por los lados hasta los hombros
    for y in range(4, 48):
        for x in range(W):
            dentro = _e(cx, cy - 1, rx + 4.0, ry + 3.5, x, y) <= 1
            cara = _e(cx, cy + 2, rx - 1.0, ry - 2.0, x, y) <= 1 and y >= 18
            if not dentro or cara:
                continue
            if y > 40 and abs(x + 0.5 - cx) < 10:
                continue
            k = 2.6 - (y - 4) * 0.02 + (x - cx) * 0.03 + (grano[y, x] - 0.5) * 0.8
            c = _tono(PANUELO, k)
            if x < cx - 8:
                c = mezcla(c, farol[1], 0.18)
            L.px(x, y, c)
    # el borde del panuelo sobre la frente
    for x in range(cx - 9, cx + 10):
        if _e(cx, cy, rx, ry, x, 18) <= 1.05:
            L.px(x, 18, PANUELO[3])
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "hedda.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "hedda.png"))
    print("hedda: piel y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        p = piel()
        p.vista().save(os.path.join(d, "hedda_piel.png"))
        p.L.imagen().resize((128 * 4, 64 * 4), 0).save(os.path.join(d, "hedda_textura.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "hedda_retrato.png"))
        print("vistas en", d)
