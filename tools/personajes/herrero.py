#!/usr/bin/env python3
"""
Dorvan, el herrero de Valdrenn (parte 2e-2): su piel (64 x 64, modelo de jugador con brazos anchos: calvo y con hollin,
barba gris oscura con una trenza, camisa de lana con las mangas remangadas, delantal de cuero chamuscado con correas y
bolsillo, un guante grueso en la izquierda, pantalon oscuro y botas) y su retrato para los dialogos (64 x 64: busto con
la luz naranja de la forja por debajo, la cabeza calva, las cejas pobladas, los ojos hundidos y la barba trenzada).

    python3 tools/personajes/herrero.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

PIEL = ["#4a2c1e", "#653d29", "#7f5036", "#986446", "#b07a58", "#c6926c"]
BARBA = ["#141212", "#221f1e", "#322e2c", "#46413d", "#5e5852", "#7a736b"]
CAMISA = ["#211d1a", "#2d2823", "#3a332c", "#483f36", "#574c41"]
CUERO = ["#1e130c", "#2e1d11", "#422a18", "#5a3a21", "#734c2c", "#8c5e38"]
HIERRO = ["#2a2a30", "#45454e", "#666672", "#8e8e9a", "#b8b8c4"]
PANTALON = ["#16161c", "#1f1f27", "#292933", "#34343f"]
BOTA = ["#120d0a", "#1e1611", "#2c2018", "#3a2b20"]
BRASA = ["#5e1806", "#a8320c", "#e86418", "#ffa83a", "#ffe08a"]
OJO_B, OJO_I, LABIO, CICATRIZ = "#b9ada0", "#3a4a5a", "#5a3020", "#c4846a"


# ---------------------------------------------------------------- la piel

def piel():
    P = Piel(brazos=4)
    camisa = tela(CAMISA, 3, pliegue=3, grano=0.10)
    cuero = ruido(CUERO[2:5], 5, grano=0.32, base=1.2)
    pantalon = tela(PANTALON, 7, pliegue=4, grano=0.10)
    cabeza = ruido(PIEL[2:6], 9, grano=0.30, base=1.4)

    # --- la cabeza: calva, cejas pobladas, ojos hundidos, nariz ancha y barba
    C = {"S": PIEL[5], "s": PIEL[4], "o": PIEL[3], "O": PIEL[2], "B": BARBA[1], "W": OJO_B, "I": OJO_I, "n": PIEL[5],
         "b": BARBA[2], "M": BARBA[3], "m": BARBA[0], "r": CICATRIZ, "h": "#3a2a22"}
    P.sprite("cabeza", "frente", [
        "osSSSSso",
        "ossSSsro",
        "oossssro",
        "OBBooBBO",
        "OWIsnIWO",
        "bOosnoOb",
        "bbMMMMbb",
        "bbbmmbbb",
    ], C)

    def lado(x, y, w, h):
        # la oreja (en el medio del costado) y la patilla de barba hacia delante
        if y >= 4 and x >= 5:
            return BARBA[2] if y > 4 else BARBA[1]
        if y in (3, 4) and x in (3, 4):
            return PIEL[2] if (x, y) == (3, 4) else PIEL[3]
        if y >= 6:
            return BARBA[1] if x >= 3 else PIEL[2]
        return cabeza(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)
    P.pintar("cabeza", "espalda", lambda x, y, w, h: mezcla(cabeza(x, y, w, h), PIEL[1], 0.15 + 0.06 * y))
    P.pintar("cabeza", "arriba", lambda x, y, w, h: PIEL[5] if (x, y) in ((3, 3), (4, 3), (3, 4)) else cabeza(x, y, w, h))
    P.pintar("cabeza", "abajo", lambda x, y, w, h: BARBA[1] if y < 5 else PIEL[1])

    # la barba (capa de encima): el bulto de la barba, la trenza con su anilla de hierro y las patillas
    def barba_frente(x, y, w, h):
        if y < 5:
            return None
        if y == 5:
            return BARBA[2] if x in (0, 7) else None
        if x in (3, 4):
            return HIERRO[3] if y == 7 and x == 3 else HIERRO[2] if y == 7 else BARBA[4] if (x + y) % 2 == 0 else BARBA[3]
        return BARBA[3] if (x + y) % 3 == 0 else BARBA[2]

    P.pintar("cabeza_f", "frente", barba_frente)

    def barba_lado(junto_frente):
        def f(x, y, w, h):
            d = abs(x - junto_frente)
            if y >= 5 and d <= 2:
                return BARBA[2] if d < 2 else BARBA[1]
            if y >= 6 and d <= 4:
                return BARBA[1]
            return None
        return f

    P.pintar("cabeza_f", "der", barba_lado(7))
    P.pintar("cabeza_f", "izq", barba_lado(0))
    P.pintar("cabeza_f", "abajo", lambda x, y, w, h: BARBA[1] if y < 4 else None)
    for cara in ("arriba", "espalda"):
        P.pintar("cabeza_f", cara, lambda x, y, w, h: None)

    # --- el cuerpo: la camisa (con el pecho a la vista en el cuello) y el delantal de cuero por delante
    def cuerpo_frente(x, y, w, h):
        if y < 2 and x in (3, 4):
            return PIEL[3] if y == 0 else PIEL[2]
        if y == 0 and x in (2, 5):
            return CAMISA[3]
        return camisa(x, y, w, h)

    P.pintar("cuerpo", "frente", cuerpo_frente)

    def cuerpo_espalda(x, y, w, h):
        # las correas del delantal cruzadas en la espalda y el lazo en el cinto
        if y < 7 and (x == 1 + y or x == 6 - y):
            return CUERO[3] if y % 2 == 0 else CUERO[2]
        if y == 7:
            return CUERO[2] if x not in (3, 4) else CUERO[4]
        if y == 8 and x in (2, 5):
            return CUERO[3]
        return camisa(x, y, w, h)

    P.pintar("cuerpo", "espalda", cuerpo_espalda)
    for cara in ("der", "izq"):
        P.pintar("cuerpo", cara, lambda x, y, w, h: CUERO[2] if y == 7 else camisa(x, y, w, h))
    P.pintar("cuerpo", "arriba", lambda x, y, w, h: CAMISA[3])
    P.pintar("cuerpo", "abajo", lambda x, y, w, h: PANTALON[1])

    # el delantal (capa de encima): peto con correas, bolsillo con unas tenazas, quemaduras y el cinto con hebilla
    QUEMADO = {(5, 9), (2, 4)}

    def delantal(x, y, w, h):
        if y < 2:
            return CUERO[4] if x in (1, 6) else None
        if y == 2:
            return CUERO[3] if 1 <= x <= 6 else None
        if y == 7:
            if x in (3, 4):
                return HIERRO[3] if x == 3 else HIERRO[2]
            return CUERO[1]
        if 8 <= y <= 10 and 1 <= x <= 3:
            # el bolsillo, con el mango de las tenazas asomando
            if y == 8:
                return HIERRO[2] if x == 2 else CUERO[4]
            return CUERO[3] if x != 3 else CUERO[2]
        if (x, y) in QUEMADO:
            return CUERO[0]
        if x in (0, w - 1):
            return mezcla(cuero(x, y, w, h), CUERO[0], 0.35)
        return cuero(x, y, w, h)

    P.pintar("cuerpo_f", "frente", delantal)

    def delantal_lado(x, y, w, h):
        if y == 7:
            return CUERO[1]
        if y < 2 and x in (1, 2):
            return CUERO[4] if y == 0 else None
        return None

    P.pintar("cuerpo_f", "der", delantal_lado)
    P.pintar("cuerpo_f", "izq", delantal_lado)
    P.pintar("cuerpo_f", "arriba", lambda x, y, w, h: CUERO[4] if x in (1, 6) else None)
    for cara in ("espalda", "abajo"):
        P.pintar("cuerpo_f", cara, lambda x, y, w, h: None)

    # --- los brazos: manga remangada por encima del codo, antebrazo desnudo; la izquierda con el guante de cuero
    def brazo(guante):
        def f(x, y, w, h):
            if y < 5:
                return camisa(x, y + 2, w, h)
            if y == 5:
                return CAMISA[4] if (x + y) % 2 else CAMISA[3]
            if guante and y >= 8:
                return CUERO[4] if y == 8 else CUERO[2] if y < 11 else CUERO[1]
            if y >= 10:
                return PIEL[4] if y == 10 else PIEL[3]
            # el antebrazo: mas oscuro por el lado de fuera, una vena de luz por delante
            return PIEL[2] if x == 0 else PIEL[4] if (x == 1 and y in (6, 7)) else PIEL[3]
        return f

    def manga(guante):
        def f(x, y, w, h):
            if y in (4, 5):
                return CAMISA[4] if y == 4 else CAMISA[3]
            if guante and y == 8:
                return CUERO[3]
            return None
        return f

    for b, g in (("brazo_d", False), ("brazo_i", True)):
        for cara in ("der", "frente", "izq", "espalda"):
            P.pintar(b, cara, brazo(g))
            P.pintar(b + "_f", cara, manga(g))
        P.pintar(b, "arriba", lambda x, y, w, h: CAMISA[3])
        P.pintar(b, "abajo", lambda x, y, w, h, g=g: CUERO[1] if g else PIEL[2])
        P.pintar(b + "_f", "arriba", lambda x, y, w, h: None)
        P.pintar(b + "_f", "abajo", lambda x, y, w, h: None)

    # --- las piernas: pantalon oscuro y botas; por encima, el faldon del delantal hasta la rodilla
    def pierna(x, y, w, h):
        if y >= 8:
            if y == 8:
                return BOTA[3]
            if y == 10 and x == 1:
                return HIERRO[2]
            return BOTA[2] if y < 11 else BOTA[1]
        return pantalon(x, y, w, h)

    def faldon(centro):
        def f(x, y, w, h):
            if y > 5:
                return None
            if y == 5:
                return CUERO[1] if (x + y) % 2 else CUERO[2]
            if x == centro:
                return CUERO[2]
            if (x, y) in ((1, 2), (2, 4)):
                return CUERO[0]
            return cuero(x + 2, y + 9, 8, 16)
        return f

    for pie, centro in (("pierna_d", 3), ("pierna_i", 0)):
        for cara in ("der", "frente", "izq", "espalda"):
            P.pintar(pie, cara, pierna)
        P.pintar(pie, "arriba", lambda x, y, w, h: PANTALON[2])
        P.pintar(pie, "abajo", lambda x, y, w, h: BOTA[0])
        P.pintar(pie + "_f", "frente", faldon(centro))
        for cara in ("der", "izq", "espalda", "arriba", "abajo"):
            P.pintar(pie + "_f", cara, lambda x, y, w, h: None)
    return P


# ---------------------------------------------------------------- el retrato

def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato():
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(31)
    grano = r.random((H, W))
    # fondo: la herreria a oscuras, con el resplandor de la forja abajo a la izquierda
    for y in range(H):
        for x in range(W):
            d = math.hypot((x - 2) / 44, (y - 70) / 40)
            t = max(0.0, 1 - d)
            c = mezcla("#070505", "#3a1406", t * 0.9)
            if grano[y, x] < 0.07:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    cx = 32
    # los hombros y el pecho: camisa oscura, correas de cuero del delantal
    for y in range(42, H):
        ancho = 22 + (y - 42) * 0.75
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            t = 2.6 - abs(dx) / ancho * 1.3 + (0.4 if dx < 0 else 0)
            pl = (abs(dx) * 0.7 + y * 0.3) % 6
            if pl < 1:
                t -= 0.8
            c = _tono(CAMISA, t)
            # la luz de la forja por la izquierda
            if dx < -ancho + 4:
                c = mezcla(c, BRASA[2], 0.35)
            L.px(x, y, c)
    # las correas del delantal sobre los hombros
    for y in range(44, H):
        for lado in (-1, 1):
            xc = cx + lado * (12 - (y - 44) * 0.18)
            for x in range(int(xc - 2.5), int(xc + 3)):
                t = 3.4 - abs(x + 0.5 - xc) * 0.9
                c = _tono(CUERO, t)
                if lado < 0:
                    c = mezcla(c, BRASA[3], 0.18)
                L.px(x, y, c)
            if y == 56:
                for x in range(int(xc - 2), int(xc + 2)):
                    L.px(x, y, HIERRO[3] if x == int(xc - 1) else HIERRO[2])
    # el cuello, ancho
    for y in range(38, 47):
        for x in range(cx - 7, cx + 7):
            L.px(x, y, PIEL[2] if x < cx - 3 else PIEL[1])
    # la barba: cae desde las mejillas hasta el pecho, ancha arriba y en punta hacia la trenza; mechones que bajan
    for y in range(26, 60):
        for x in range(W):
            dx = x + 0.5 - cx
            if y < 40:
                ancho = 10.8 - max(0, 30 - y) * 0.5
            else:
                ancho = 10.8 - (y - 40) * 0.42
            if abs(dx) > ancho or ancho <= 0:
                continue
            # los mechones convergen hacia la trenza al bajar
            conv = dx * (1 - max(0, y - 34) / 34.0)
            m = int(math.floor(conv * 0.9 + 0.35 * math.sin(y * 0.5) + 100)) % 3
            k = 2.3 + (1.1 if m == 0 else -0.7 if m == 2 else 0)
            k -= max(0, y - 50) * 0.06
            k += 0.5 if abs(dx) < 2.5 and y < 40 else 0
            c = _tono(BARBA, k)
            if dx < -ancho + 2.5:
                c = mezcla(c, BRASA[3], 0.25)
            L.px(x, y, c)
    # la trenza en el centro, con sus anillas de hierro
    for y in range(46, 63):
        for x in range(cx - 2, cx + 2):
            k = 3 if (x + y) % 2 == 0 else 2
            L.px(x, y, BARBA[k])
    for yy in (52, 58):
        for x in range(cx - 3, cx + 3):
            L.px(x, yy, HIERRO[4] if x == cx - 2 else HIERRO[2] if x == cx + 2 else HIERRO[3])
    # la cabeza calva: un ovalo grande con la luz de arriba (fria) y la de la forja por la izquierda (naranja)
    for y in range(4, 42):
        for x in range(W):
            e = _e(cx, 23, 11.6, 16.2, x, y)
            if e > 1:
                continue
            dx = x + 0.5 - cx
            dy = y + 0.5 - 23
            # el craneo: la luz de arriba a la derecha
            t = 3.0 - dy / 14.0 * 0.9 + dx / 12.0 * 0.4 - max(0.0, e - 0.7) * 2.2
            c = _tono(PIEL, t)
            if dx < -8.5:
                c = mezcla(c, BRASA[3], 0.42)
            elif dx < -6.5:
                c = mezcla(c, BRASA[2], 0.16)
            if grano[y, x] < 0.10 and y < 18:
                c = mezcla(c, "#2a1810", 0.25)  # hollin
            L.px(x, y, c)
    # brillo de la calva
    for (x, y) in ((34, 8), (35, 8), (36, 9), (33, 8)):
        L.px(x, y, mezcla(PIEL[5], "#fff0dc", 0.45))
    # las orejas
    for lado in (-1, 1):
        ox = cx + lado * 11
        for y in range(21, 29):
            for x in range(ox - 2, ox + 2):
                if _e(ox, 25, 2.2, 4.2, x, y) <= 1:
                    c = PIEL[3] if lado > 0 else mezcla(PIEL[3], BRASA[3], 0.45)
                    if abs(x + 0.5 - ox) < 0.8 and 23 <= y <= 26:
                        c = PIEL[1]
                    L.px(x, y, c)
    # la frente arrugada y la cicatriz que cruza la ceja izquierda (la de su derecha)
    for x in range(cx - 6, cx + 6):
        if x % 3 != 0:
            L.px(x, 14, mezcla(PIEL[3], PIEL[1], 0.35))
    for i, (x, y) in enumerate(((cx + 6, 13), (cx + 6, 14), (cx + 5, 15), (cx + 5, 16), (cx + 4, 19), (cx + 4, 20))):
        L.px(x, y, CICATRIZ)
    # las cejas pobladas
    for lado in (-1, 1):
        for i in range(7):
            x = cx + lado * (2 + i)
            y = 17 + (1 if i in (0, 6) else 0)
            L.px(x, y, BARBA[2] if i % 2 else BARBA[1])
            L.px(x, y + 1, BARBA[1])
    # los ojos hundidos: la sombra de las cuencas, el ojo y un punto de luz de la forja
    for lado in (-1, 1):
        ox = cx + lado * 5
        for y in range(19, 23):
            for x in range(ox - 3, ox + 3):
                L.px(x, y, mezcla(PIEL[1], "#1a0e08", 0.35))
        L.px(ox - 1, 20, OJO_B).px(ox, 20, OJO_I).px(ox + 1, 20, OJO_B if lado > 0 else mezcla(OJO_B, PIEL[1], 0.5))
        L.px(ox - 1 if lado < 0 else ox, 21, mezcla(OJO_I, "#000000", 0.4))
        L.px(ox if lado > 0 else ox - 1, 20, mezcla(OJO_I, BRASA[3], 0.35))
        L.px(ox, 20, BRASA[4] if lado < 0 else "#d8e2ee")
    # la nariz ancha, un poco torcida
    for y in range(21, 30):
        for x in range(cx - 2, cx + 3):
            w = 1.5 + (y - 21) * 0.25
            if abs(x + 0.5 - (cx + 0.6 + (y - 21) * 0.08)) <= w:
                L.px(x, y, PIEL[4] if x >= cx else PIEL[3])
    L.px(cx - 2, 29, PIEL[1]).px(cx + 2, 29, PIEL[1]).px(cx - 1, 30, PIEL[1]).px(cx + 1, 30, PIEL[1])
    L.px(cx + 1, 23, PIEL[5]).px(cx + 1, 24, PIEL[5])
    # las mejillas (lo que deja ver la barba) y el bigote
    for y in range(27, 32):
        for lado in (-1, 1):
            for i in range(2):
                x = cx + lado * (6 + i)
                L.px(x, y, mezcla(PIEL[3], "#a85a42", 0.18) if y < 30 else BARBA[2])
    for x in range(cx - 7, cx + 8):
        dx = abs(x + 0.5 - cx)
        y0 = 31 + (1 if dx > 4 else 0) + (1 if dx > 6 else 0)
        for y in range(y0, y0 + 2):
            L.px(x, y, BARBA[4] if (x + y) % 3 == 0 else BARBA[3])
    for x in range(cx - 2, cx + 2):
        L.px(x, 34, LABIO if 0 < x - (cx - 2) < 3 else BARBA[1])
    # brasas flotando
    for (x, y, c) in ((6, 40, BRASA[3]), (9, 52, BRASA[4]), (4, 30, BRASA[2]), (12, 60, BRASA[3]), (56, 49, BRASA[1]), (2, 48, BRASA[2])):
        L.px(x, y, c)
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "herrero.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "herrero.png"))
    print("herrero: piel y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        p = piel()
        p.vista().save(os.path.join(d, "herrero_piel.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "herrero_retrato.png"))
        p.L.imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "herrero_textura.png"))
        print("vistas en", d)
