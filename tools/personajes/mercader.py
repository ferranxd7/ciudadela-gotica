#!/usr/bin/env python3
"""
Kerrick, el mercader ambulante (parte 2e-2): su piel (128 x 64: lo de un jugador con brazos finos en la mitad izquierda y,
en la derecha, las piezas de mas: el sombrero de ala ancha, la mochila enorme con la manta enrollada encima, la olla y
el farol colgando) y su retrato para los dialogos (64 x 64: el sombrero con su pluma, la sonrisa con un diente de oro,
el bigote fino, el panuelo mostaza y la luz amarilla de su farol).

Las piezas de mas (en Java, `client/personajes/MercaderRenderer.crear`, mismas medidas):
    mochila  (64, 0)  9 x 11 x 5  en el cuerpo
    manta    (64, 16) 11 x 3 x 3  en el cuerpo, encima de la mochila
    olla     (96, 0)  3 x 3 x 3   en el cuerpo, colgando a la izquierda de la mochila
    farol    (96, 8)  2 x 3 x 2   en el cuerpo, colgando a la derecha
    copa     (64, 24) 9 x 3 x 9   en la cabeza
    ala      (64, 40) 14 x 1 x 14 en la cabeza

    python3 tools/personajes/mercader.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

PIEL = ["#5a3a26", "#76503a", "#92684c", "#ac8060", "#c49a76", "#d8b292"]
PELO = ["#1c130d", "#2a1c13", "#3a281b", "#4e3624"]
SOMBRERO = ["#1e1c14", "#2a271b", "#383423", "#47422c", "#575136", "#6a6342"]
CINTA = ["#4a0e10", "#6e1618", "#922226"]
PLUMA = ["#8c8a80", "#b8b6aa", "#e2e0d4"]
ABRIGO = ["#122422", "#1a322e", "#22413b", "#2c524a", "#38645a", "#467868"]
REMIENDO = ["#3a2616", "#54381f", "#6e4a2a"]
PANUELO = ["#5a4010", "#7e5c18", "#a27a22", "#c49a32", "#dcb84e"]
PANTALON = ["#2a2016", "#382b1e", "#463627", "#544232"]
BOTA = ["#141010", "#221a16", "#30251e", "#3e3026"]
LONA = ["#4a4030", "#5e523d", "#74674d", "#8a7c5e", "#a09270", "#b6a882"]
CUERO = ["#24170e", "#3a2616", "#54381f", "#6e4a2a", "#8a5e36"]
MANTA = ["#3a1210", "#541c16", "#6e2a1e", "#8a3a26", "#a44e30"]
HIERRO = ["#202024", "#34343a", "#4c4c56", "#6c6c78", "#9898a4"]
LATON = ["#4a3410", "#7a5a1c", "#a88030", "#d4ac4c", "#f0d27a"]
LUZ = ["#a86a10", "#e8a830", "#ffd866", "#fff2b0"]
ORO = "#e2bf62"
DIENTE, LABIO = "#e8e0cc", "#6a3a2a"


def piel():
    P = Piel(ancho=128, alto=64, brazos=3)
    P.caja("mochila", 64, 0, 9, 11, 5)
    P.caja("manta", 64, 16, 11, 3, 3)
    P.caja("olla", 96, 0, 3, 3, 3)
    P.caja("farol", 96, 8, 2, 3, 2)
    P.caja("copa", 64, 24, 9, 3, 9)
    P.caja("ala", 64, 40, 14, 1, 14)
    abrigo = tela(ABRIGO, 13, pliegue=3, grano=0.12)
    pantalon = tela(PANTALON, 17, pliegue=4, grano=0.10)
    lona = tela(LONA, 19, pliegue=4, grano=0.14)
    cara_lado = ruido(PIEL[2:5], 23, grano=0.25, base=1.2)

    # --- la cabeza: cara flaca, nariz larga, bigote fino y la sonrisa con un diente de oro
    C = {"h": PELO[2], "H": PELO[3], "d": PELO[1], "s": PIEL[4], "S": PIEL[5], "o": PIEL[3], "O": PIEL[2], "P": PELO[1],
         "W": "#d8d0c0", "I": "#3a5a2e", "n": PIEL[5], "m": PELO[2], "t": DIENTE, "g": ORO, "b": PIEL[2], "k": PELO[1]}
    P.sprite("cabeza", "frente", [
        "dhhHHhhd",
        "dsssSssd",
        "oPPssPPo",
        "OWIsnIWO",
        "Oosnnsoo",
        "bmmssmmb",
        "bstgttsb",
        "kbossobk",
    ], C)

    def lado(x, y, w, h):
        if y < 3 or (y < 6 and x <= 2):
            return PELO[2] if (x + y) % 2 else PELO[1]
        if (x, y) in ((4, 4), (4, 5)):
            return ORO if y == 5 else PIEL[2]  # la oreja con el aro de oro
        return cara_lado(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)
    P.pintar("cabeza", "espalda", lambda x, y, w, h: PELO[2] if (x + y) % 3 else PELO[1])
    P.pintar("cabeza", "arriba", lambda x, y, w, h: PELO[2])
    P.pintar("cabeza", "abajo", lambda x, y, w, h: PIEL[1])
    # la capa de encima de la cabeza: el pelo que asoma bajo el ala, por los lados y la nuca
    def greñas(x, y, w, h):
        # mechones de distinto largo que asoman bajo el ala
        largo = 2 + (x * 5 + 1) % 3
        if 1 <= y <= largo:
            return PELO[3] if y == 1 else PELO[2] if y < largo else PELO[1]
        return None

    for cara in ("der", "izq", "espalda"):
        P.pintar("cabeza_f", cara, greñas)
    for cara in ("frente", "arriba", "abajo"):
        P.pintar("cabeza_f", cara, lambda x, y, w, h: None)

    # --- el sombrero: copa con cinta roja y la pluma; ala ancha algo gastada en el borde
    def copa(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                return SOMBRERO[3] if (x - 4) ** 2 + (y - 4) ** 2 < 5 else SOMBRERO[2]
            if cara == "abajo":
                return SOMBRERO[0]
            if y == 2:
                return CINTA[2] if x % 4 else CINTA[1]
            if cara == "der" and y < 2 and x in (5, 6):
                return PLUMA[2] if y == 0 else PLUMA[1]
            return SOMBRERO[3] if y == 0 else SOMBRERO[2]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("copa", cara, copa(cara))

    def ala(cara):
        def f(x, y, w, h):
            if cara in ("arriba", "abajo"):
                borde = min(x, y, w - 1 - x, h - 1 - y)
                if cara == "abajo":
                    return SOMBRERO[0] if borde > 0 else SOMBRERO[1]
                if borde == 0:
                    return SOMBRERO[2] if (x + y) % 5 else SOMBRERO[1]
                return SOMBRERO[4] if borde == 1 else SOMBRERO[3] if (x * 3 + y) % 7 else SOMBRERO[4]
            return SOMBRERO[1]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("ala", cara, ala(cara))

    # --- el cuerpo: abrigo verde gastado con remiendos, panuelo mostaza al cuello, correas de la mochila y cinto
    def cuerpo_frente(x, y, w, h):
        if y < 2:
            return PANUELO[3] if (x + y) % 3 else PANUELO[2]
        if y == 2 and 2 <= x <= 5:
            return PANUELO[1] if x in (3, 4) else PANUELO[2]
        if x in (1, 6) and y < 8:
            return CUERO[3] if y % 3 else HIERRO[3] if y == 3 else CUERO[2]
        if y == 8:
            return CUERO[1] if x not in (3, 4) else LATON[3]
        if y >= 9 and x in (5, 6):
            return CUERO[3] if y == 9 else CUERO[2]  # la bolsa del cinto
        if 4 <= x <= 5 and 4 <= y <= 6:
            return REMIENDO[1] if (x + y) % 2 else REMIENDO[2]
        if x in (3, 4) and y >= 3:
            return ABRIGO[1]
        return abrigo(x, y, w, h)

    P.pintar("cuerpo", "frente", cuerpo_frente)

    def cuerpo_espalda(x, y, w, h):
        if y < 2:
            return PANUELO[2]
        if x in (1, 6):
            return CUERO[2]
        if y == 8:
            return CUERO[1]
        return abrigo(x, y, w, h)

    P.pintar("cuerpo", "espalda", cuerpo_espalda)
    for cara in ("der", "izq"):
        P.pintar("cuerpo", cara, lambda x, y, w, h: PANUELO[2] if y < 2 else CUERO[1] if y == 8 else abrigo(x, y, w, h))
    P.pintar("cuerpo", "arriba", lambda x, y, w, h: PANUELO[3])
    P.pintar("cuerpo", "abajo", lambda x, y, w, h: PANTALON[1])

    # capa de encima del cuerpo: el panuelo abultado y los faldones del abrigo
    def panuelo(x, y, w, h):
        if y < 2:
            return PANUELO[4] if (x + y) % 3 == 0 else PANUELO[3]
        if y == 2 and x in (2, 3):
            return PANUELO[2]
        if y >= 9 and x not in (3, 4):
            return mezcla(abrigo(x, y, w, h), ABRIGO[0], 0.2)
        return None

    P.pintar("cuerpo_f", "frente", panuelo)
    for cara in ("der", "izq", "espalda"):
        P.pintar("cuerpo_f", cara, lambda x, y, w, h: PANUELO[3] if y < 2 else (abrigo(x, y, w, h) if y >= 9 else None))
    P.pintar("cuerpo_f", "arriba", lambda x, y, w, h: PANUELO[3])
    P.pintar("cuerpo_f", "abajo", lambda x, y, w, h: None)

    # --- los brazos: mangas del abrigo con un remiendo en el codo, guantes sin dedos
    def brazo(x, y, w, h):
        if y >= 9:
            if y == 9:
                return CUERO[3]
            return PIEL[3] if y == 11 else CUERO[2]
        if 5 <= y <= 6 and x == 1:
            return REMIENDO[2]
        return abrigo(x, y, w, h)

    def puño(x, y, w, h):
        if y == 8:
            return ABRIGO[4] if x % 2 else ABRIGO[3]
        return None

    for b in ("brazo_d", "brazo_i"):
        for cara in ("der", "frente", "izq", "espalda"):
            P.pintar(b, cara, brazo)
            P.pintar(b + "_f", cara, puño)
        P.pintar(b, "arriba", lambda x, y, w, h: ABRIGO[3])
        P.pintar(b, "abajo", lambda x, y, w, h: PIEL[2])
        P.pintar(b + "_f", "arriba", lambda x, y, w, h: None)
        P.pintar(b + "_f", "abajo", lambda x, y, w, h: None)

    # --- las piernas: pantalon pardo y botas altas con correa
    def pierna(x, y, w, h):
        if y >= 6:
            if y == 6:
                return BOTA[3]
            if y == 8:
                return CUERO[2] if x != 1 else LATON[2]
            return BOTA[2] if y < 11 else BOTA[1]
        return pantalon(x, y, w, h)

    for pie in ("pierna_d", "pierna_i"):
        for cara in ("der", "frente", "izq", "espalda"):
            P.pintar(pie, cara, pierna)
        P.pintar(pie, "arriba", lambda x, y, w, h: PANTALON[2])
        P.pintar(pie, "abajo", lambda x, y, w, h: BOTA[0])
        for cara in ("der", "frente", "izq", "espalda", "arriba", "abajo"):
            P.pintar(pie + "_f", cara, lambda x, y, w, h: BOTA[3] if y == 6 and cara != "arriba" and cara != "abajo" else None)

    # --- la mochila: lona con costuras, correas de cuero con hebillas y un bolsillo; cachivaches colgando
    def mochila(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                return LONA[3] if (x + y) % 2 else LONA[2]
            if cara == "abajo":
                return LONA[1]
            if cara == "frente":  # la cara que da a la espalda del mercader (no se ve)
                return LONA[1]
            if cara == "espalda":
                # la cara de atras: tapa con dos correas y hebillas, bolsillo abajo
                if y < 3:
                    return LONA[4] if y == 0 else LONA[3]
                if y == 3:
                    return LONA[1]
                if x in (2, 6):
                    return HIERRO[4] if y == 5 else CUERO[3] if y % 2 else CUERO[2]
                if 7 <= y <= 9 and 3 <= x <= 5:
                    return LONA[4] if y == 7 else LONA[2]
                return lona(x, y, w, h)
            # los lados: costura y una cuerda
            if x == w // 2 and y > 1:
                return LONA[1]
            if y == 6:
                return CUERO[2]
            return lona(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("mochila", cara, mochila(cara))

    def manta(cara):
        def f(x, y, w, h):
            if cara in ("der", "izq"):
                # el rollo visto de punta: la espiral
                d = abs(x - 1) + abs(y - 1)
                return MANTA[4] if d == 0 else MANTA[1] if d == 1 else MANTA[3]
            if x in (2, w - 3):
                return CUERO[2]
            return MANTA[3] if (x + y) % 3 else MANTA[2]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("manta", cara, manta(cara))
    P.todas("olla", lambda x, y, w, h: HIERRO[2] if y == 0 else HIERRO[1] if (x + y) % 2 else HIERRO[0])
    P.pintar("olla", "arriba", lambda x, y, w, h: HIERRO[3] if (x, y) != (1, 1) else HIERRO[1])

    def farol(cara):
        def f(x, y, w, h):
            if cara in ("arriba", "abajo"):
                return LATON[2]
            if y == 0 or y == h - 1:
                return LATON[3] if y == 0 else LATON[1]
            return LUZ[2] if x % 2 == 0 else LUZ[3]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("farol", cara, farol(cara))
    return P


# ---------------------------------------------------------------- el retrato

def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato():
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(41)
    grano = r.random((H, W))
    cx = 31
    # fondo: noche azul a la izquierda y la luz amarilla del farol a la derecha
    for y in range(H):
        for x in range(W):
            d = math.hypot((x - 66) / 40, (y - 44) / 36)
            t = max(0.0, 1 - d)
            c = mezcla("#06070d", "#3a2a0c", t * 0.95)
            if grano[y, x] < 0.06:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    # lo alto de la mochila, asomando por detras del hombro derecho (el de la luz)
    for y in range(30, 44):
        for x in range(42, 60):
            if (x - 42) + (43 - y) * 0.3 < 1 or (y - 30) ** 2 + (x - 59) ** 2 < 4:
                continue
            t = 2.4 + (x - 42) / 14.0 + (0.8 if y == 30 else 0) - (1 if x == 50 else 0)
            c = _tono(LONA, t)
            L.px(x, y, mezcla(c, LUZ[1], 0.18))
        L.px(50, y, CUERO[2])
    # los hombros: el abrigo verde con las correas de la mochila
    for y in range(41, H):
        ancho = 19 + (y - 41) * 0.8
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            t = 2.7 - abs(dx) / ancho * 1.2 + (0.6 if dx > 0 else -0.2)
            if (abs(dx) * 0.8 + y * 0.25) % 6 < 1:
                t -= 0.8
            c = _tono(ABRIGO, t)
            if dx > ancho - 4:
                c = mezcla(c, LUZ[2], 0.30)
            L.px(x, y, c)
    for lado in (-1, 1):
        for y in range(41, H):
            xc = cx + lado * (11 + (y - 41) * 0.12)
            for x in range(int(xc - 1.5), int(xc + 2)):
                L.px(x, y, CUERO[3] if x == int(xc - 1) or lado > 0 else CUERO[2])
            if y == 54:
                L.px(int(xc - 1), y, LATON[3]).px(int(xc), y, LATON[2])
    # el remiendo en el hombro izquierdo
    for y in range(52, 59):
        for x in range(cx - 21, cx - 15):
            if 0 <= x < W:
                L.px(x, y, REMIENDO[2] if (x + y) % 2 else REMIENDO[1])
    # el panuelo mostaza al cuello, con el nudo
    for y in range(40, 51):
        for x in range(W):
            dx = x + 0.5 - cx
            ancho = 8.5 - abs(y - 45) * 0.25
            if abs(dx) <= ancho:
                t = 2.6 + (0.8 if (x + y // 2) % 4 == 0 else 0) + dx / 9.0
                c = _tono(PANUELO, t)
                L.px(x, y, c)
    for y in range(48, 56):
        for x in range(cx - 1, cx + 3):
            if abs(x - (cx + 1) - (y - 48) * 0.2) < 1.6:
                L.px(x, y, PANUELO[3] if y < 51 else PANUELO[2])
    # el cuello y la cara flaca
    for y in range(36, 42):
        for x in range(cx - 4, cx + 5):
            L.px(x, y, PIEL[2] if x < cx else PIEL[3])
    for y in range(13, 41):
        for x in range(W):
            e = _e(cx + 0.5, 26.5, 9.0, 13.2, x, y)
            if e > 1:
                continue
            dx = x + 0.5 - cx
            t = 3.1 + dx / 16.0 - max(0.0, e - 0.75) * 2
            if y > 34:
                t -= 0.5
            c = _tono(PIEL, t)
            if dx > 6:
                c = mezcla(c, LUZ[2], 0.25)
            L.px(x, y, c)
    # la barba de tres dias en la barbilla y las mejillas hundidas
    for y in range(30, 40):
        for x in range(W):
            e = _e(cx + 0.5, 26.5, 9.0, 13.2, x, y)
            if e <= 1 and e > 0.55 and grano[y, x] < 0.55 and y > 31:
                L.px(x, y, mezcla(L.leer(x, y), PELO[1], 0.45))
    # el pelo que asoma bajo el ala por los lados
    for lado in (-1, 1):
        for y in range(16, 30):
            largo = 3 if y < 25 else 2 if y < 28 else 1
            for k in range(largo):
                x = cx + lado * (9 - k) + (0 if lado > 0 else 1)
                L.px(x, y, PELO[1] if k == 0 else PELO[2] if (y + k) % 4 else PELO[3])
    # la oreja derecha (a la izquierda del retrato no se ve) con el aro de oro
    for y in range(23, 30):
        for x in range(cx + 8, cx + 11):
            if _e(cx + 9.5, 26.5, 1.8, 3.6, x, y) <= 1:
                L.px(x, y, mezcla(PIEL[3], LUZ[2], 0.3))
    L.px(cx + 9, 30, LATON[4]).px(cx + 10, 31, LATON[3]).px(cx + 9, 32, LATON[3]).px(cx + 8, 31, LATON[2])
    # el ala del sombrero (sombra encima de los ojos) y la copa con la cinta y la pluma
    for y in range(0, 22):
        for x in range(W):
            # la copa
            if 3 <= y <= 15 and abs(x + 0.5 - cx) <= 9.5 - max(0, 6 - y) * 0.6:
                t = 3.0 + (x - cx) / 12.0 - (0.8 if (x - cx) % 5 == 0 else 0)
                c = _tono(SOMBRERO, t)
                if 12 <= y <= 14:
                    c = _tono(CINTA, 2 - (y - 12) * 0.5 + (0.4 if x % 4 == 0 else 0))
                L.px(x, y, c)
    for y in range(15, 21):
        for x in range(W):
            dx = x + 0.5 - cx
            # el ala vista un poco desde abajo: una elipse aplastada y caida en las puntas
            yc = 17.2 + (abs(dx) / 22.0) ** 2 * 2.5
            if abs(dx) <= 25 and abs(y + 0.5 - yc) <= 1.7 - (abs(dx) / 25) ** 4:
                t = 3.4 - (y + 0.5 - yc) * 0.8 + dx / 30.0
                c = _tono(SOMBRERO, t)
                if dx > 16:
                    c = mezcla(c, LUZ[2], 0.15)
                L.px(x, y, c)
    # la pluma, hacia atras a la derecha
    for i in range(14):
        x = cx + 7 + i
        y = 12 - int(i * 0.75)
        L.px(x, y, PLUMA[2] if i % 3 else PLUMA[1])
        if i > 2:
            L.px(x, y + 1, PLUMA[0] if i % 2 else PLUMA[1])
    # la sombra del ala sobre la frente
    for y in range(20, 24):
        for x in range(cx - 8, cx + 10):
            if _e(cx + 0.5, 26.5, 9.0, 13.2, x, y) <= 1:
                L.px(x, y, mezcla(L.leer(x, y), "#1a1008", 0.45 - (y - 20) * 0.1))
    # los ojos entornados de risa (con arruguitas) y las cejas arqueadas
    for lado in (-1, 1):
        ox = cx + lado * 4 + (1 if lado > 0 else 0)
        for i in range(-2, 2):
            L.px(ox + i, 24 + (1 if i in (-2, 1) else 0), PELO[1])
        L.px(ox - 1, 26, "#2a1a12").px(ox, 26, "#2a1a12")
        L.px(ox - 1 if lado < 0 else ox, 27, "#e8e2c8" if lado > 0 else "#c8c0a8")
        L.px(ox if lado < 0 else ox - 1, 27, "#3a5a2e")
        L.px(ox + 2 * lado, 27, mezcla(PIEL[2], PIEL[1], 0.5)).px(ox + 2 * lado, 28, mezcla(PIEL[2], PIEL[1], 0.5))
    # la nariz larga y un poco ganchuda
    for y in range(25, 34):
        x0 = cx + (1 if y > 29 else 0)
        L.px(x0, y, PIEL[4] if y < 32 else PIEL[3])
        L.px(x0 + 1, y, PIEL[3] if y < 32 else PIEL[2])
    L.px(cx - 1, 33, PIEL[1]).px(cx + 2, 33, PIEL[1]).px(cx, 34, PIEL[1])
    # el bigote fino con las puntas hacia arriba y la sonrisa ancha con el diente de oro
    for i, (x, y) in enumerate(((cx - 5, 34), (cx - 4, 35), (cx - 3, 35), (cx - 2, 35), (cx - 1, 35), (cx, 35), (cx + 1, 35),
                                (cx + 2, 35), (cx + 3, 35), (cx + 4, 35), (cx + 5, 35), (cx + 6, 34))):
        L.px(x, y, PELO[2] if i % 3 else PELO[1])
    for x in range(cx - 3, cx + 5):
        L.px(x, 36, LABIO)
        L.px(x, 37, DIENTE if x != cx + 2 else ORO)
    L.px(cx - 3, 37, LABIO).px(cx + 4, 37, LABIO)
    for x in range(cx - 2, cx + 4):
        L.px(x, 38, LABIO)
    L.px(cx - 4, 36, PIEL[1]).px(cx + 5, 36, PIEL[1])
    # el farol colgando de la mochila, a la derecha: laton y cristal encendido
    for y in range(44, 56):
        for x in range(54, 61):
            if y in (44, 45) and x in (57,):
                L.px(x, y, LATON[1])
            elif y == 46 or y == 55:
                if 55 <= x <= 59:
                    L.px(x, y, LATON[3] if y == 46 else LATON[1])
            elif 47 <= y <= 54 and 55 <= x <= 59:
                borde = x in (55, 59)
                L.px(x, y, LATON[2] if borde else LUZ[3] if 49 <= y <= 52 and x in (57,) else LUZ[2])
    for y in range(42, 58):
        for x in range(50, 64):
            d = math.hypot(x - 57, y - 50.5)
            if 4 < d < 8 and grano[y, x] < 0.35:
                L.px(x, y, mezcla(L.leer(x, y), LUZ[2], 0.25 * (8 - d) / 4))
    # motas de luz del farol
    for (x, y) in ((58, 30), (61, 40), (55, 22), (60, 50)):
        L.px(x, y, LUZ[3])
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "mercader.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "mercader.png"))
    print("mercader: piel y retrato")


def _vista_mochila(P):
    """En la vista de espaldas, la mochila, la manta, la olla y el farol encima (aproximado)."""
    def extra(out, ox, frente):
        if frente:
            # de frente: el ala del sombrero por encima de la cabeza y los cachivaches por los lados
            for x in range(-3, 11):
                out.px(ox + 4 + x, 2 - 1, P.L.leer(64 + 14 + max(0, min(13, x + 3)), 40 + 14))
            return
        for nombre, x, y in (("mochila", 3, 9), ("manta", 2, 7)):
            out.pegar(P.recorte(nombre, "espalda"), ox + x + 1, y + 2)
        out.pegar(P.recorte("olla", "espalda"), ox + 13, 17)
        out.pegar(P.recorte("farol", "espalda"), ox + 1, 16)
        for x in range(-3, 11):
            out.px(ox + 4 + x, 1, P.L.leer(64 + 14 + max(0, min(13, x + 3)), 40 + 14))
    return extra


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        p = piel()
        p.vista(extra=_vista_mochila(p)).save(os.path.join(d, "mercader_piel.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "mercader_retrato.png"))
        p.L.imagen().resize((128 * 4, 64 * 4), 0).save(os.path.join(d, "mercader_textura.png"))
        print("vistas en", d)
