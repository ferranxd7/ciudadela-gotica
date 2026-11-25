#!/usr/bin/env python3
"""
Maelis, la guia de los santuarios (parte 2e-1): su piel (64 x 64, modelo de jugador con brazos finos: capucha de pizarra
con ribete de oro, manto sobre los hombros, tunica azul pizarra con el vestido de lino a la vista, cordon de cuerda y un
colgante de fuego de almas) y su retrato para los dialogos (64 x 64: busto con la capucha, la cara serena con los ojos
entornados, el pelo plateado y la luz azul del santuario por debajo).

    python3 tools/personajes/guia.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, caras, mezcla, pintar, rgba, sprite, tela, vista  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

CAPUCHA = ["#0f1119", "#171b27", "#212636", "#2b3245", "#373f57", "#465170"]
TUNICA = ["#1c2132", "#252c41", "#303952", "#3c4764", "#4b5878", "#5d6b8f"]
LINO = ["#8c877a", "#a8a394", "#c2bdad", "#d9d5c6"]
ORO = ["#4a3312", "#6e4e1c", "#96702a", "#c0963c", "#e2bf62", "#f6e3a0"]
PIEL = ["#a8806c", "#c49c86", "#dab69f", "#ead0bd", "#f4e2d4"]
PELO = ["#5e5e74", "#85859c", "#adadc0", "#d2d2df", "#ececf4"]
CUERDA = ["#4e381f", "#76552f", "#9c7646"]
BOTA = ["#1a1310", "#2a1f18", "#3a2c22"]
IRIS, IRIS_OSC, BLANCO, PESTANA, LABIOS = "#7fb0ee", "#34508c", "#e9e6de", "#241c22", "#a8686a"
ALMA = ["#1f3f96", "#5a8ae6", "#a0c0f8", "#e8f2ff"]


# ---------------------------------------------------------------- la piel

def piel():
    L = Lienzo(64, 64)
    capucha = tela(CAPUCHA, 3, pliegue=3, grano=0.08)
    tunica = tela(TUNICA, 5, pliegue=3, grano=0.10)
    manto = tela(CAPUCHA[1:], 7, pliegue=2, grano=0.06)

    # --- cabeza: la cara (lo de fuera lo tapa la capucha) y el pelo plateado
    P = {"h": PELO[2], "H": PELO[3], "d": PELO[1], "s": PIEL[3], "S": PIEL[4], "o": PIEL[2], "O": PIEL[1],
         "L": PESTANA, "W": BLANCO, "I": IRIS, "m": LABIOS, "b": "#e0b4a8"}
    sprite(L, "cabeza", "frente", [
        "dHhHHhHd",
        "dhhSShhd",
        "dhssssSd",
        "dLLssLLd",
        "dWIsoIWd",
        "dbsssobd",
        "dosmmsod",
        "dOossoOd",
    ], P)
    pelo = tela(PELO[:4], 11, pliegue=2, luz_arriba=True, grano=0.12)
    for cara in ("der", "izq", "espalda", "arriba"):
        pintar(L, "cabeza", cara, pelo)
    pintar(L, "cabeza", "abajo", lambda x, y, w, h: PIEL[1])

    # --- la capucha (capa de encima de la cabeza): abierta por delante, ribete de oro en el borde
    def capucha_frente(x, y, w, h):
        if y == 0:
            return ORO[3] if 2 <= x <= 5 else CAPUCHA[3]
        if x in (0, w - 1):
            return CAPUCHA[2] if y < 6 else CAPUCHA[1]
        if y == 1 and x in (1, w - 2):
            return CAPUCHA[1]
        return None

    pintar(L, "cabeza_f", "frente", capucha_frente)

    def capucha_lado(junto_frente):
        def f(x, y, w, h):
            if x == junto_frente:
                return ORO[4] if y < 3 else ORO[3] if y < 6 else ORO[2]
            c = capucha(x, y, w, h)
            return mezcla(c, CAPUCHA[0], 0.25) if y >= h - 2 else c
        return f

    pintar(L, "cabeza_f", "der", capucha_lado(7))
    pintar(L, "cabeza_f", "izq", capucha_lado(0))

    def capucha_arriba(x, y, w, h):
        if y == h - 1:
            return ORO[4]
        return capucha(x, y, w, h)

    pintar(L, "cabeza_f", "arriba", capucha_arriba)

    def capucha_espalda(x, y, w, h):
        # costura en el centro y la llama de almas bordada en oro
        if (x, y) in ((3, 4), (4, 4), (3, 5), (4, 5), (4, 3), (3, 6), (4, 6)):
            return ORO[4] if (x, y) in ((4, 3), (3, 4)) else ORO[3]
        if x == 3 and y < 3:
            return CAPUCHA[1]
        return capucha(x, y, w, h)

    pintar(L, "cabeza_f", "espalda", capucha_espalda)
    pintar(L, "cabeza_f", "abajo", lambda x, y, w, h: None)

    # --- el cuerpo: tunica con el vestido de lino delante, cuello y botones de oro, colgante y cordon
    def cuerpo_frente(x, y, w, h):
        if y == 6:
            return CUERDA[2] if x % 3 == 0 else CUERDA[1]
        if y == 7 and x in (2, 5):
            return CUERDA[0]
        if y < 6:
            if x in (2, 5):
                return ORO[4] if y == 0 else ORO[3] if y < 4 else ORO[2]
            if x in (3, 4):
                if y == 3 and x == 3:
                    return ALMA[2]
                if y == 3 and x == 4:
                    return ALMA[1]
                if y == 2 and x == 3:
                    return ORO[4]
                return LINO[2] if y < 2 else LINO[1 + (x + y) % 2]
            return tunica(x, y, w, h)
        if x in (3, 4):
            return ORO[3] if x == 3 else ORO[2]
        return tunica(x, y, w, h)

    pintar(L, "cuerpo", "frente", cuerpo_frente)

    def cuerpo_lado(x, y, w, h):
        if y == 6:
            return CUERDA[1] if x % 2 else CUERDA[0]
        return tunica(x, y, w, h)

    for cara in ("der", "izq", "espalda"):
        pintar(L, "cuerpo", cara, cuerpo_lado)
    pintar(L, "cuerpo", "arriba", lambda x, y, w, h: TUNICA[3])
    pintar(L, "cuerpo", "abajo", lambda x, y, w, h: TUNICA[1])

    # el manto (capa de encima del cuerpo): sobre los hombros, mas largo por la espalda, con ribete de oro
    def manto_frente(x, y, w, h):
        if y < 3 and x in (0, 1, w - 2, w - 1):
            return manto(x, y, w, h)
        if y == 3 and x in (0, 1, w - 2, w - 1):
            return ORO[3] if x in (1, w - 2) else ORO[2]
        if y >= 9 and x in (0, w - 1):
            return mezcla(TUNICA[2], TUNICA[0], 0.3)
        return None

    pintar(L, "cuerpo_f", "frente", manto_frente)

    def manto_espalda(x, y, w, h):
        if y < 6:
            if (x, y) in ((3, 2), (4, 2), (3, 3), (4, 3), (4, 1)):
                return ALMA[1] if (x, y) == (4, 1) else ORO[3]
            return manto(x, y, w, h)
        if y == 6:
            return ORO[3] if x % 2 == 0 else ORO[2]
        return None

    pintar(L, "cuerpo_f", "espalda", manto_espalda)

    def manto_lado(x, y, w, h):
        if y < 4:
            return manto(x, y, w, h)
        if y == 4:
            return ORO[2]
        return None

    pintar(L, "cuerpo_f", "der", manto_lado)
    pintar(L, "cuerpo_f", "izq", manto_lado)
    pintar(L, "cuerpo_f", "arriba", lambda x, y, w, h: CAPUCHA[3])
    pintar(L, "cuerpo_f", "abajo", lambda x, y, w, h: None)

    # --- los brazos: manga de tunica, mano; por encima el manto en el hombro y el puno ancho con oro
    def brazo(x, y, w, h):
        if y >= 10:
            return PIEL[3] if y == 10 else PIEL[2]
        return tunica(x, y, w, h)

    def manga(x, y, w, h):
        if y < 3:
            return manto(x, y, w, h)
        if y == 3:
            return ORO[2]
        if 7 <= y <= 8:
            return mezcla(TUNICA[3], TUNICA[1], 0.2 * (x % 2))
        if y == 9:
            return ORO[4] if x % 2 == 0 else ORO[3]
        return None

    for b in ("brazo_d", "brazo_i"):
        for cara in ("der", "frente", "izq", "espalda"):
            pintar(L, b, cara, brazo)
            pintar(L, b + "_f", cara, manga)
        pintar(L, b, "arriba", lambda x, y, w, h: TUNICA[3])
        pintar(L, b, "abajo", lambda x, y, w, h: PIEL[2])
        pintar(L, b + "_f", "arriba", lambda x, y, w, h: CAPUCHA[3])
        pintar(L, b + "_f", "abajo", lambda x, y, w, h: None)

    # --- las piernas: la falda de la tunica (abierta en el centro, con ribete) y las botas
    def pierna(lado_centro):
        def f(x, y, w, h):
            if y >= 10:
                return BOTA[2] if y == 10 else BOTA[1]
            if x == lado_centro:
                return LINO[1]
            return tunica(x, y, w, h)
        return f

    def falda(lado_centro, frente):
        def f(x, y, w, h):
            if y >= 10:
                return None
            if y == 9:
                return ORO[3] if (x + y) % 2 == 0 else ORO[2]
            if frente and x == lado_centro:
                return ORO[3] if y < 9 else ORO[2]
            c = tunica(x + 1, y + 6, w, 16)
            return mezcla(c, TUNICA[0], 0.15 * (y / 9))
        return f

    for pie, centro in (("pierna_d", 3), ("pierna_i", 0)):
        pintar(L, pie, "frente", pierna(centro))
        for cara in ("der", "izq", "espalda"):
            pintar(L, pie, cara, pierna(-1))
        pintar(L, pie, "arriba", lambda x, y, w, h: TUNICA[2])
        pintar(L, pie, "abajo", lambda x, y, w, h: BOTA[0])
        pintar(L, pie + "_f", "frente", falda(centro, True))
        for cara in ("der", "izq", "espalda"):
            pintar(L, pie + "_f", cara, falda(-1, False))
        pintar(L, pie + "_f", "arriba", lambda x, y, w, h: None)
        pintar(L, pie + "_f", "abajo", lambda x, y, w, h: None)
    return L


# ---------------------------------------------------------------- el retrato

def _elipse(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def retrato():
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(21)
    ruido = r.random((H, W))
    # fondo: penumbra con el resplandor azul del santuario abajo a la derecha
    for y in range(H):
        for x in range(W):
            d = math.hypot((x - 46) / 40, (y - 70) / 34)
            t = max(0.0, 1 - d)
            c = mezcla("#07060b", "#13213f", t * 0.85)
            if ruido[y, x] < 0.08:
                c = mezcla(c, "#000000", 0.25)
            L.px(x, y, c)
    cx = 32
    # el manto y los hombros (de 44 hacia abajo): pliegues que caen desde el cuello
    for y in range(44, H):
        ancho = 17 + (y - 44) * 0.9
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) <= ancho:
                t = 3.2 - abs(dx) / ancho * 1.6
                pl = (abs(dx) * 0.9 + (y - 44) * 0.25) % 5
                if pl < 1:
                    t -= 0.9
                elif pl < 2:
                    t += 0.4
                c = CAPUCHA[int(np.clip(round(t), 0, 5))]
                # la luz azul del santuario por el borde derecho
                if dx > ancho - 1.5:
                    c = mezcla(c, ALMA[1], 0.22)
                L.px(x, y, c)
    # la tunica y el lino en el pecho (entre los bordes del manto)
    for y in range(51, H):
        media = 3.5 + (y - 51) * 0.3
        for x in range(W):
            dx = abs(x + 0.5 - cx)
            if dx <= media:
                L.px(x, y, LINO[2] if dx < media - 1.5 else LINO[1])
            elif dx <= media + 1.2:
                L.px(x, y, ORO[3] if (y % 2) else ORO[2])
    # el broche: oro con la gema de alma
    for y in range(52, 59):
        for x in range(cx - 3, cx + 3):
            if _elipse(cx, 55.5, 3.2, 3.2, x, y) <= 1:
                L.px(x, y, ORO[4] if _elipse(cx - 0.8, 54.8, 2.0, 2.0, x, y) <= 1 else ORO[2])
    for (x, y, c) in ((31, 54, ALMA[1]), (32, 54, ALMA[2]), (31, 55, ALMA[0]), (32, 55, ALMA[1]), (32, 53, ALMA[3])):
        L.px(x, y, c)
    # la capucha: forma de gota alrededor de la cabeza, abierta por delante
    for y in range(2, 52):
        for x in range(W):
            e = _elipse(cx, 27, 21.5, 26, x, y)
            if e <= 1:
                dentro = _elipse(cx, 30.5, 12.2, 17.5, x, y)
                if dentro <= 1 and y > 11:
                    continue
                # tono: mas claro arriba a la izquierda, pliegues verticales
                t = 3.1 - (y - 2) / 24.0 - max(0.0, e - 0.75) * 3
                pl = (abs(x + 0.5 - cx) * 0.8 + y * 0.12) % 6
                if pl < 1 and y > 12:
                    t -= 0.8
                elif pl < 2 and y > 12:
                    t += 0.35
                c = CAPUCHA[int(np.clip(round(t), 0, 5))]
                # la luz azul del santuario, solo en el borde derecho
                if x > cx + 17 and y > 16:
                    c = mezcla(c, ALMA[1], 0.18 + 0.14 * (y - 16) / 34)
                # ribete de oro en el borde de la abertura (hasta la barbilla)
                if 1 < dentro <= 1.3 and 9 < y < 41:
                    c = ORO[4] if y < 22 else ORO[3] if y < 33 else ORO[2]
                L.px(x, y, c)
    # sombra dentro de la capucha (hueco oscuro detras de la cara)
    for y in range(10, 48):
        for x in range(W):
            if _elipse(cx, 30.5, 12.2, 17.5, x, y) <= 1:
                L.px(x, y, mezcla("#06070c", "#111728", max(0, (y - 10) / 40)))
    # el pelo plateado: mechones a los lados de la cara que caen hasta los hombros
    for y in range(13, 50):
        for x in range(W):
            if _elipse(cx, 29, 11.4, 16, x, y) <= 1:
                dx = x + 0.5 - cx
                lado = abs(dx) > (6.4 if y < 22 or y > 36 else 7.1)
                if lado or y < 17:
                    k = 3 if (x + y // 3) % 3 else 2
                    if abs(dx) > 9.5:
                        k -= 1
                    if dx > 6 and y > 20:
                        k = min(4, k + 1)  # la luz azul en el pelo de la derecha
                    L.px(x, y, PELO[k])
    # la cara: ovalo palido con sombra a la izquierda y luz fria a la derecha
    for y in range(15, 45):
        for x in range(W):
            e = _elipse(cx + 0.3, 30, 7.2, 12.4, x, y)
            if e <= 1:
                dx = x + 0.5 - cx
                c = PIEL[3]
                if dx < -5.5:
                    c = PIEL[1]
                elif dx < -4.5:
                    c = mezcla(PIEL[1], PIEL[2], 0.6)
                elif dx < -3:
                    c = PIEL[2]
                if y > 40:
                    c = mezcla(c, PIEL[1], 0.35)
                if dx > 4.5:
                    c = mezcla(c, ALMA[2], 0.2)
                L.px(x, y, c)
    # el flequillo partido al medio
    for (x0, y0, largo, paso) in ((cx - 1, 15, 7, -1), (cx, 15, 7, 1)):
        for i in range(largo):
            x = x0 + paso * i
            for j in range(0, 4 - i // 2):
                L.px(x, y0 + j + i // 2, PELO[3] if j == 0 else PELO[2])
    # cejas finas, ojos entornados (parpado oscuro y una rendija de iris azul palido), nariz y boca
    CEJA = "#6c6478"
    for x, y in ((27, 25), (28, 24), (29, 24), (30, 25), (34, 25), (35, 24), (36, 24), (37, 25)):
        L.px(x, y, CEJA)
    for x in range(27, 31):
        L.px(x, 28, PESTANA)
    for x in range(34, 38):
        L.px(x, 28, PESTANA)
    L.px(26, 27, PESTANA).px(38, 27, PESTANA)
    for x, c in ((27, BLANCO), (28, IRIS_OSC), (29, IRIS), (30, BLANCO), (34, BLANCO), (35, IRIS), (36, IRIS_OSC), (37, BLANCO)):
        L.px(x, 29, c)
    L.px(28, 30, PIEL[2]).px(29, 30, PIEL[2]).px(35, 30, PIEL[2]).px(36, 30, PIEL[2])
    L.px(cx, 32, PIEL[2]).px(cx, 33, PIEL[2]).px(cx - 1, 34, PIEL[1]).px(cx, 34, PIEL[2])
    L.px(cx - 2, 37, mezcla(LABIOS, PIEL[2], 0.5)).px(cx - 1, 37, LABIOS).px(cx, 37, LABIOS).px(cx + 1, 37, mezcla(LABIOS, PIEL[3], 0.5))
    L.px(cx - 1, 38, mezcla(LABIOS, PIEL[3], 0.55)).px(cx, 38, mezcla(LABIOS, PIEL[3], 0.55))
    # un poco de color en las mejillas
    L.px(cx - 4, 32, mezcla(PIEL[3], "#d08a86", 0.35)).px(cx + 4, 32, mezcla(PIEL[3], "#d08a86", 0.3))
    # el cuello en sombra
    for y in range(43, 47):
        for x in range(cx - 3, cx + 3):
            L.px(x, y, PIEL[1] if x < cx else PIEL[2])
    # motas de luz de alma flotando
    for (x, y) in ((50, 47), (54, 40), (47, 56), (57, 52), (8, 58)):
        L.px(x, y, ALMA[2])
    L.px(51, 47, ALMA[1]).px(54, 41, ALMA[1])
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "guia.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "guia.png"))
    print("guia: piel y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        vista(piel()).save(os.path.join(d, "guia_piel.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "guia_retrato.png"))
        piel().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "guia_textura.png"))
        print("vistas en", d)
