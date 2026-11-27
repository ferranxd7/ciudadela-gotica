#!/usr/bin/env python3
"""
Corwen, el farolero de los Pilotes (parte 7ao): su piel (128 x 64: lo de un jugador de brazos anchos en la mitad izquierda y las
piezas de mas en la derecha), la de su brillo (lo que luce: el cristal de su farol encendido) y su retrato para los dialogos
(64 x 64), en `textures/entity/corwen.png`, `textures/entity/corwen_brillo.png` y `textures/gui/retratos/corwen.png`.

- **Corwen**: un viejo de mas de setenta anos, la cara curtida y arrugada, las cejas blancas y pobladas, los ojos de un azul
  lavado, la nariz grande y una barba blanca que le llega al pecho; el sombrero de hule ocre de los marineros (con su ala ancha y
  el faldon de atras), un chaqueton largo de hule verde negruzco con cazonetes de asta, el pantalon de lana parda y las botas
  llenas de barro. Sentado en una caja de pescado vieja al final de la pasarela del santuario, con su pertiga de farolero (una
  vara larga con el gancho de laton arriba y el farol colgado); el farol no luce hasta que se encienden las tres balizas.

Las piezas de mas (en Java, `client/personajes/ViajeroRenderer.crearCorwen`, mismas medidas):
    copa       (64, 0)   9 x 3 x 9    en la cabeza: la copa del sombrero de hule
    cogote     (100, 0)  9 x 3 x 1    en la cabeza: el faldon de atras del sombrero
    barba      (100, 4)  6 x 5 x 1    en la cabeza: la barba, delante de la cara (cae hasta el pecho)
    ala        (64, 12)  11 x 1 x 12  en la cabeza: el ala (mas larga por detras)
    abrigo     (64, 25)  9 x 12 x 5   en el cuerpo (0,25 mas grande): el chaqueton de hule
    espinilla  (92, 25)  4 x 9 x 4    suelta, una por pierna: de la rodilla al suelo (pantalon y bota)
    caja       (64, 42)  10 x 11 x 8  suelta: la caja de pescado en que se sienta
    farol      (100, 42) 4 x 5 x 4    en la pertiga (o en el suelo, si se queda): el farol (lo que luce, en el brillo)
    gancho     (100, 51) 1 x 1 x 4    en la pertiga: el gancho de laton de arriba
    asa        (110, 51) 1 x 2 x 1    en la pertiga: la anilla de la que cuelga el farol
    pertiga    (124, 0)  1 x 40 x 1   suelta: la vara del farolero

    python3 tools/personajes/corwen.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

PIEL = ["#4a3428", "#6a4a3a", "#8a6650", "#a6806a", "#bf9a82", "#d4b49c"]
BARBA = ["#5e5a56", "#7e7a74", "#a29e96", "#c4c0b8", "#dedad2", "#f4f2ec"]
HULE = ["#241c0a", "#3a2e10", "#524218", "#6a5622", "#826c2c", "#9a8238"]
ABRIGO = ["#0c100c", "#141a12", "#1c2418", "#252f20", "#303c2a", "#3e4c36"]
PANTALON = ["#1a130e", "#261c14", "#33261a", "#413220"]
BOTA = ["#090706", "#120e0b", "#1c1612", "#28201a"]
BARRO = ["#2a2014", "#3a2e1c", "#4a3c26"]
MADERA = ["#221c14", "#32291e", "#463a2a", "#5a4b36", "#6e5d44", "#827052"]
LATON = ["#3a2a12", "#5e4420", "#8a6830", "#b08c44", "#d0ae62"]
HIERRO = ["#121014", "#1e1a20", "#2c272e", "#3c363e", "#50484f"]
ASTA = ["#5a4a36", "#86745a"]
VIDRIO = ["#141210", "#1e1a16", "#2a241e"]
LUZ = ["#7a3008", "#c45a10", "#f09030", "#ffc860", "#fff0b0"]


def _cuerpo_cajas(P):
    P.caja("copa", 64, 0, 9, 3, 9)
    P.caja("cogote", 100, 0, 9, 3, 1)
    P.caja("barba", 100, 4, 6, 5, 1)
    P.caja("ala", 64, 12, 11, 1, 12)
    P.caja("abrigo", 64, 25, 9, 12, 5)
    P.caja("espinilla", 92, 25, 4, 9, 4)
    P.caja("caja", 64, 42, 10, 11, 8)
    P.caja("farol", 100, 42, 4, 5, 4)
    P.caja("gancho", 100, 51, 1, 1, 4)
    P.caja("asa", 110, 51, 1, 2, 1)
    P.caja("pertiga", 124, 0, 1, 40, 1)


def _farol(cara, brillo):
    """El farol: el marco de hierro arriba y abajo, los barrotes en las esquinas y el cristal (apagado: ahumado, con la mecha negra;
    en el brillo: la llama ambar)."""
    def f(x, y, w, h):
        if cara in ("arriba", "abajo"):
            if brillo:
                return None
            return HIERRO[3] if (x + y) % 2 == 0 else HIERRO[2]
        if y == 0 or y == h - 1:
            return None if brillo else (HIERRO[4] if y == 0 else HIERRO[1])
        if x in (0, w - 1):
            return None if brillo else LATON[2]
        centro = y == h - 2
        if brillo:
            return LUZ[4] if centro else LUZ[3] if y == h - 3 else LUZ[2]
        # apagado: el cristal ahumado y la mecha
        return VIDRIO[0] if centro else VIDRIO[2] if (x + y) % 2 else VIDRIO[1]
    return f


def piel():
    P = Piel(ancho=128, alto=64, brazos=4)
    _cuerpo_cajas(P)
    s = 131
    hule = ruido(HULE[1:5], s + 1, grano=0.42, base=1.6)
    abrigo = tela(ABRIGO, s + 2, pliegue=3, grano=0.16)
    pantalon = tela(PANTALON, s + 3, pliegue=3, grano=0.12)
    cara_lado = ruido(PIEL[2:5], s + 4, grano=0.24, base=1.0)
    barba = ruido(BARBA[2:6], s + 5, grano=0.5, base=1.6)
    madera = ruido(MADERA[2:6], s + 6, grano=0.55, base=1.4)
    rr = np.random.default_rng(s)
    motas = rr.random((64, 128))

    # ---- la cabeza: la frente arrugada, las cejas blancas y pobladas, los ojos de azul lavado, la nariz grande, el bigote
    C = {"g": BARBA[2], "G": BARBA[3], "s": PIEL[3], "S": PIEL[4], "o": PIEL[2], "r": PIEL[1], "w": "#c4c8c2", "e": "#5c7c8c",
         "W": BARBA[5], "n": PIEL[5], "N": PIEL[2], "m": BARBA[3], "M": BARBA[4]}
    P.sprite("cabeza", "frente", [
        "gsrSSrsg",
        "gsSSSSsg",
        "gWWsSWWg",
        "oweSSewo",
        "osSnNSso",
        "omMMMMmo",
        "GmMmmMmG",
        "gMMMMMMg",
    ], C)

    def lado(x, y, w, h):
        # el pelo gris detras; delante la mejilla, la oreja y la barba abajo
        if x >= 5 or y <= 0:
            return BARBA[2] if (x * 2 + y) % 5 else BARBA[1]
        if y >= 5:
            return barba(x, y, w, h)
        if (x, y) in ((3, 3), (3, 4)):
            return PIEL[2]  # la oreja
        return cara_lado(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)
    P.pintar("cabeza", "espalda", lambda x, y, w, h: BARBA[2] if (x + y) % 3 else BARBA[1])
    P.pintar("cabeza", "arriba", lambda x, y, w, h: BARBA[1])
    P.pintar("cabeza", "abajo", lambda x, y, w, h: BARBA[3])
    for cara in ("frente", "der", "izq", "espalda", "arriba", "abajo"):
        P.pintar("cabeza_f", cara, lambda x, y, w, h: None)

    # ---- el sombrero de hule: la copa con sus costuras, el ala (oscura por debajo) y el faldon de atras
    def copa(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return HULE[0]
            c = hule(x, y, w, h)
            if cara == "arriba":
                if x in (4,) or y in (4,):
                    c = HULE[1]  # las costuras en cruz
                return c
            if y == h - 1:
                return HULE[1]  # la cinta de abajo
            if motas[(y * 7 + len(cara)) % 64, (x * 5 + 9) % 128] > 0.9:
                c = mezcla(c, BARRO[1], 0.6)
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("copa", cara, copa(cara))

    def ala(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return HULE[0] if (x + y) % 3 else HULE[1]
            if cara == "arriba":
                borde = x in (0, w - 1) or y in (0, h - 1)
                c = HULE[2] if borde else hule(x, y, w, h)
                if motas[(x * 3 + 17) % 64, (y * 11 + 2) % 128] > 0.92:
                    c = mezcla(c, BARRO[2], 0.5)
                return c
            return HULE[1]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("ala", cara, ala(cara))
    P.todas("cogote", lambda x, y, w, h: HULE[1] if y == h - 1 else hule(x, y, w, h))

    # ---- la barba blanca, con sus ondas; el bigote arriba
    def barba_f(cara):
        def f(x, y, w, h):
            if cara == "frente":
                if y == 0:
                    return BARBA[4] if x in (1, 2, 3, 4) else BARBA[3]
                if y == h - 1:
                    return BARBA[3] if x in (2, 3) else None if x in (0, w - 1) else BARBA[2]
                k = (x + y) % 3
                return BARBA[5] if k == 0 else BARBA[4] if k == 1 else BARBA[3]
            if cara == "abajo":
                return BARBA[2]
            return BARBA[3] if (x + y) % 2 else BARBA[2]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("barba", cara, barba_f(cara))

    # ---- el cuerpo (lo tapa el chaqueton, pero por si acaso) y el chaqueton de hule con sus cazonetes
    def chaqueton(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                return ABRIGO[3]
            if cara == "abajo":
                return ABRIGO[1]
            c = abrigo(x, y, w, h)
            if y <= 1 and cara in ("frente", "der", "izq", "espalda"):
                c = ABRIGO[4] if (x + y) % 2 else ABRIGO[3]  # el cuello alzado
            if cara == "frente":
                mitad = w // 2
                if x == mitad and y >= 2:
                    c = ABRIGO[0]  # donde se cierra
                if x == mitad - 1 and y in (3, 6, 9):
                    c = ASTA[1]  # los cazonetes de asta
                elif x == mitad + 1 and y in (3, 6, 9):
                    c = ASTA[0]
                if y in (7, 8) and x in (1, 2, w - 3, w - 2):
                    c = ABRIGO[1] if y == 7 else ABRIGO[2]  # los bolsillos
            # el brillo del hule mojado y el barro de abajo
            if motas[(y * 5 + len(cara) * 3) % 64, (x * 7 + 1) % 128] > 0.93:
                c = ABRIGO[5]
            if y >= h - 2 and motas[(y * 3 + 4) % 64, (x * 9 + len(cara)) % 128] > 0.5:
                c = mezcla(c, BARRO[1], 0.6)
            return c
        return f

    for cara in ("frente", "espalda", "der", "izq", "arriba", "abajo"):
        P.pintar("cuerpo", cara, chaqueton(cara))
        P.pintar("abrigo", cara, chaqueton(cara))
        P.pintar("cuerpo_f", cara, lambda x, y, w, h: None)

    # ---- los brazos: las mangas del chaqueton, el puno vuelto y las manos viejas
    def brazo(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return PIEL[2]
            if cara == "arriba":
                return ABRIGO[3]
            if y >= 10:
                return PIEL[3] if (x + y) % 3 else PIEL[2]
            if y == 9:
                return ABRIGO[4] if x % 2 else ABRIGO[3]
            c = abrigo(x, y, w, h)
            if motas[(y * 9 + len(cara)) % 64, (x * 3 + 21) % 128] > 0.94:
                c = ABRIGO[5]
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("brazo_d", cara, brazo(cara))
        P.pintar("brazo_i", cara, brazo(cara))
        P.pintar("brazo_d_f", cara, lambda x, y, w, h: None)
        P.pintar("brazo_i_f", cara, lambda x, y, w, h: None)

    # ---- los muslos (sentado: la cara de delante de la pierna queda arriba; el faldon del chaqueton tapa la mitad de atras)
    def pierna(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return PANTALON[0]
            if cara == "frente" and y <= 3:
                return abrigo(x, y + 4, w, h)
            if cara in ("der", "izq") and y <= 2:
                return ABRIGO[2]
            return pantalon(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("pierna_d", cara, pierna(cara))
        P.pintar("pierna_i", cara, pierna(cara))
        P.pintar("pierna_d_f", cara, lambda x, y, w, h: None)
        P.pintar("pierna_i_f", cara, lambda x, y, w, h: None)

    # la espinilla: el pantalon y la bota alta de cuero, llena de barro
    def espinilla(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                return PANTALON[2]
            if cara == "abajo":
                return BOTA[0]
            if y <= 2:
                return pantalon(x, y, w, h)
            if y == 3:
                return BOTA[3] if x % 2 else BOTA[2]  # la vuelta de la bota
            if y == h - 1:
                return BOTA[0]
            c = BOTA[2] if (x + y) % 4 else BOTA[1]
            # el barro, mas abajo cuanto mas
            if motas[(y * 3 + 11) % 64, (x * 5 + len(cara) * 7) % 128] < (y - 3) / h * 1.1:
                c = BARRO[(x + y) % 3]
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("espinilla", cara, espinilla(cara))

    # ---- la caja de pescado: tablas con rendijas, los cantos mas oscuros, clavos y una marca medio borrada
    def caja(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return MADERA[0]
            if cara == "arriba":
                if y % 3 == 2:
                    return MADERA[0]  # las rendijas entre tablas
                c = madera(x, y, w, h)
                if x in (0, w - 1) and y % 3 == 1:
                    c = HIERRO[3]  # los clavos
                return c
            if x in (0, w - 1):
                return MADERA[1]  # los listones de las esquinas
            if y % 4 == 3:
                return MADERA[0]
            c = madera(x, y, w, h)
            if y % 4 == 0 and x in (1, w - 2):
                c = HIERRO[3]
            # una marca de pintura medio borrada (un pez) en el frente
            if cara == "frente" and (x, y) in ((3, 5), (4, 4), (5, 4), (6, 5), (5, 6), (4, 6), (7, 4), (7, 6)) \
                    and motas[y % 64, (x * 13) % 128] > 0.25:
                c = mezcla(c, "#8a8478", 0.55)
            if motas[(y * 7 + 3) % 64, (x * 11 + len(cara) * 5) % 128] > 0.9:
                c = mezcla(c, "#1a2418", 0.5)  # moho
            return c
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("caja", cara, caja(cara))

    # ---- la pertiga: la vara de fresno con su vena, la virola de laton arriba y el regaton de hierro abajo
    def pertiga(cara):
        def f(x, y, w, h):
            if cara in ("arriba", "abajo"):
                return LATON[3] if cara == "arriba" else HIERRO[2]
            if y <= 1:
                return LATON[3] if y == 0 else LATON[2]
            if y >= h - 2:
                return HIERRO[3]
            return MADERA[4] if (y // 3 + len(cara)) % 4 else MADERA[3]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("pertiga", cara, pertiga(cara))
    P.todas("gancho", lambda x, y, w, h: LATON[3] if (x + y) % 2 else LATON[2])
    P.todas("asa", lambda x, y, w, h: HIERRO[3] if y % 2 else HIERRO[4])
    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("farol", cara, _farol(cara, False))
    return P


def brillo():
    """Lo que luce (se dibuja aparte, con luz propia, solo con las balizas encendidas): el cristal del farol."""
    P = Piel(ancho=128, alto=64, brazos=4)
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
    r = np.random.default_rng(137)
    grano = r.random((H, W))
    # ---- el fondo: la niebla del pantano de noche, tres luces lejanas (las balizas) y el farol que alumbra desde la derecha
    for y in range(H):
        for x in range(W):
            t = y / H
            c = mezcla("#0e1210", "#26302a", t * 0.9)
            # bandas de niebla
            c = mezcla(c, "#46524a", max(0.0, 0.35 - abs(y - 30 - math.sin(x / 9) * 3) / 26) * 0.9)
            d = math.hypot((x - 66) / 26, (y - 14) / 22)
            c = mezcla(c, LUZ[2], max(0.0, 1 - d) * 0.35)
            if grano[y, x] < 0.04:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    # las tres balizas a lo lejos: puntitos ambar con su halo en la niebla
    for (bx, by) in ((5, 33), (13, 30), (50, 31)):
        for y in range(by - 4, by + 5):
            for x in range(bx - 4, bx + 5):
                if 0 <= x < W and 0 <= y < H:
                    d = math.hypot(x - bx, y - by)
                    if d < 4.5:
                        L.px(x, y, mezcla(L.leer(x, y), LUZ[2], (1 - d / 4.5) * 0.45))
        L.px(bx, by, LUZ[4])
        for y in range(by + 1, by + 7):
            L.px(bx, y, mezcla(L.leer(bx, y), "#0a0c0a", 0.55))  # el poste
    cx, cy = 29, 30
    # ---- los hombros: el chaqueton de hule verde negruzco, mojado, con el cuello alzado y un cazonete
    for y in range(42, H):
        ancho = 21 + (y - 42) * 0.8
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            c = _tono(ABRIGO, 3.2 - abs(dx) / ancho * 1.8 + (grano[y, x] - 0.5) * 1.3)
            if abs(dx) < 1.0 and y > 47:
                c = ABRIGO[0]
            if grano[y, x] > 0.975:
                c = ABRIGO[5]  # el brillo del hule mojado
            # la luz del farol desde la derecha
            c = mezcla(c, LUZ[2], max(0.0, dx / ancho) * 0.18)
            L.px(x, y, c)
    for y in (52, 58):
        L.px(cx - 2, y, ASTA[1]).px(cx - 1, y, ASTA[0])
    # ---- la cara: vieja, curtida, alumbrada por el farol desde la derecha
    rx, ry = 9.6, 11.6
    for y in range(12, 44):
        for x in range(W):
            e = _e(cx, cy, rx, ry, x, y)
            if e > 1:
                continue
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            t = 2.7 + dx / 12.0 * 0.9 - dy / 14.0 * 0.3 - max(0.0, e - 0.7) * 2.2
            c = _tono(PIEL, t)
            c = mezcla(c, LUZ[3], max(0.0, dx / rx) * 0.22)
            L.px(x, y, c)
    # las arrugas de la frente y de los ojos
    for x in range(cx - 6, cx + 7):
        if x % 3:
            L.px(x, 19, PIEL[1])
            L.px(x + 1, 21, PIEL[1])
    # las cejas blancas y pobladas
    for (ex, s) in ((24, -1), (34, 1)):
        for x in range(ex - 3, ex + 4):
            L.px(x, 23 - (1 if abs(x - ex) < 2 else 0), BARBA[5] if x % 2 else BARBA[4])
        L.px(ex + 4 * s, 24, BARBA[4])
    # los ojos de azul lavado, cansados
    for (ex, lado) in ((24, -1), (34, 1)):
        L.px(ex - 1, 26, "#b8bcb4").px(ex, 26, "#5c7c8c").px(ex + 1, 26, "#2a3a42")
        L.px(ex, 25, PIEL[1])
        L.px(ex - 2, 27, PIEL[1]).px(ex + 2, 27, PIEL[1])
    # la nariz grande: el caballete fino entre los ojos y la punta ancha y roja de viejo
    for y in range(26, 30):
        L.px(cx, y, PIEL[4]).px(cx + 1, y, PIEL[3])
    for y in range(30, 33):
        for x in range(cx - 1, cx + 3):
            L.px(x, y, mezcla(PIEL[4] if x <= cx + 1 else PIEL[3], "#b85a48", 0.25 if y < 32 else 0.1))
    L.px(cx, 30, PIEL[5]).px(cx - 2, 32, PIEL[1]).px(cx + 3, 32, PIEL[1]).px(cx - 1, 32, PIEL[1]).px(cx + 2, 32, PIEL[1])
    # ---- la barba blanca que le cae al pecho, con el bigote
    for y in range(33, 58):
        for x in range(W):
            dx = x + 0.5 - cx
            anchob = 9.5 - max(0, y - 46) * 0.45 + math.sin(y / 2.2) * 0.6
            if y < 36:
                anchob = 5.5 + (y - 33) * 1.2  # el bigote, que se abre hacia los lados
            if abs(dx) > anchob:
                continue
            k = 3.4 - (y - 33) * 0.035 + dx / 14 * 0.8 + (grano[y, x] - 0.5) * 1.4 + (0.6 if (x * 2 + y) % 5 == 0 else 0)
            L.px(x, y, _tono(BARBA, k))
    # la boca, una raya oscura bajo el bigote
    for x in range(cx - 2, cx + 4):
        L.px(x, 36, mezcla(BARBA[1], PIEL[0], 0.5))
    # ---- el sombrero de hule: la copa y el ala ancha, mas oscuros por la izquierda
    for y in range(4, 22):
        for x in range(W):
            dx = x + 0.5 - cx
            if y < 15:
                if abs(dx) > 9.5 - max(0, 8 - y) * 0.3:
                    continue
                k = 3.0 + dx / 12 * 1.1 + (grano[y, x] - 0.5) * 1.2
            else:
                # el ala: ancha, cae un poco por los lados
                if abs(dx) > 15.5 or y > 17 + abs(dx) * 0.25:
                    continue
                k = 2.3 + dx / 16 * 1.2 + (grano[y, x] - 0.5) * 0.9
                if y >= 17:
                    k -= 1.2  # el canto, mas oscuro
            c = _tono(HULE, k)
            if y == 14:
                c = HULE[1]  # la cinta
            L.px(x, y, c)
    # el borde del ala hace sombra en la frente
    for x in range(cx - 8, cx + 9):
        L.px(x, 18, mezcla(L.leer(x, 18), "#000000", 0.35))
    return L


def guardar():
    piel().guardar(os.path.join(ASSETS, "textures", "entity", "corwen.png"))
    brillo().guardar(os.path.join(ASSETS, "textures", "entity", "corwen_brillo.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "corwen.png"))
    print("corwen: piel, brillo y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        p = piel()
        p.vista().save(os.path.join(d, "corwen_vista.png"))
        p.L.imagen().resize((128 * 6, 64 * 6), 0).save(os.path.join(d, "corwen_piel.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "corwen_retrato.png"))
