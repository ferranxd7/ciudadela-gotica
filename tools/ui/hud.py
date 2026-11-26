#!/usr/bin/env python3
"""
Las texturas del HUD nuevo, en pixel art (assets/darkcraft/textures/gui/hud.png) y sus sitios en la textura
(com/darkcraft/client/ui/HudTex.java):

  emblema                     el medallon con el ojo de la Sombra, a la izquierda de las barras
  barra_*                     el marco de las barras (izquierda, tramo que se repite, punta) y su fondo
  vida / mana / estamina      los rellenos (tramo que se repite), el rastro del dano y el brillo que los recorre
  ecos_*                      la placa de los Ecos de la Sombra (abajo a la derecha) y su icono
  tecla_*                     una tecla (para [E], [ESPACIO]...) en tres trozos
  aviso_*                     el cartelito de "Pulsa [E]..." en tres trozos
  floritura                   el adorno de los carteles grandes (HAS MUERTO, JEFE ABATIDO...)
  cine_*                      la cenefa de las cinematicas (en vez de las bandas negras con la linea roja)
  brujula_*                   la brujula de arriba
  boton_* / boton_luz_*       un boton de piedra con filo de oro (normal y con el raton encima), en tres trozos
  marca_*                     las marcas de la brujula (el objetivo, la espada de luz y los santuarios de Morvain)
  banda                       la piedra casi negra de las bandas de las cinematicas (se repite)

    python3 tools/ui/hud.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import (ASSETS, AZUL, GUI, HUESO, ORO, PIEDRA, REPO, ROJO, TINTA, VERDE, Atlas, Lienzo, mezcla, piedra,  # noqa: E402
                  rgba)

R = np.random.default_rng(2411)


# ---------------------------------------------------------------- el emblema
def emblema():
    """Medallon de oro gastado con el ojo rojo de la Sombra (24x24)."""
    L = Lienzo(24, 24)
    c = 11.5
    luz = (-0.62, -0.78)
    for y in range(24):
        for x in range(24):
            dx, dy = x - c, y - c
            r = math.hypot(dx, dy)
            if r > 11.6:
                continue
            if r > 10.5:
                L.px(x, y, TINTA)
            elif r > 7.6:
                # aro de oro: mas claro hacia la luz, con el filo de dentro oscuro
                n = (dx / max(r, 1e-6)) * luz[0] + (dy / max(r, 1e-6)) * luz[1]
                i = 3 + int(round(n * 2.4))
                if r < 8.4:
                    i -= 2
                elif r > 9.9:
                    i -= 1
                L.px(x, y, ORO[max(0, min(6, i))])
            elif r > 6.9:
                L.px(x, y, ORO[0])
            else:
                L.px(x, y, PIEDRA[1] if (x + y) % 5 else PIEDRA[2])
    # el ojo: una almendra roja con la pupila rasgada
    for y in range(24):
        for x in range(24):
            dx, dy = x - c, y - c
            if abs(dx) > 5.6:
                continue
            alto = 3.3 * math.sqrt(max(0.0, 1 - (dx / 5.8) ** 2))
            if abs(dy) <= alto:
                d = math.hypot(dx / 5.8, dy / 3.3)
                col = ROJO[6] if d < 0.28 else ROJO[5] if d < 0.5 else ROJO[4] if d < 0.78 else ROJO[2]
                L.px(x, y, col)
            elif abs(dy) <= alto + 1 and abs(dx) <= 5.6:
                L.px(x, y, TINTA)
    for y in range(9, 15):
        L.px(11, y, TINTA).px(12, y, TINTA)
    L.px(11, 8, ROJO[1]).px(12, 15, ROJO[1])
    L.px(9, 10, "#ffd8c8")
    # cuatro tachuelas en el aro
    for (x, y) in [(11, 1), (12, 1), (11, 22), (12, 22), (1, 11), (1, 12), (22, 11), (22, 12)]:
        L.px(x, y, ORO[6] if y < 12 and x < 13 else ORO[4])
    return L


# ---------------------------------------------------------------- las barras
ALTO_BARRA = 9  # tinta, oro, cinco de relleno, oro, tinta


def barra_marco(w, punta=False, izq=False):
    L = Lienzo(w, ALTO_BARRA)
    for x in range(w):
        L.px(x, 0, TINTA).px(x, 8, TINTA)
        L.px(x, 1, ORO[4] if x % 7 == 3 else ORO[3])
        L.px(x, 7, ORO[1] if x % 7 != 5 else ORO[2])
    if izq:
        # arranque pegado al emblema: un taco de oro
        for y in range(1, 8):
            L.px(0, y, ORO[2]).px(1, y, ORO[4] if y < 4 else ORO[3])
        L.px(1, 1, ORO[5])
    if punta:
        # punta de flecha de oro al final
        L = Lienzo(w, ALTO_BARRA)
        filas = [
            "kkkk....",
            "oooOk...",
            "...oOk..",
            "....oOk.",
            ".....oYk",
            "....oOk.",
            "...odk..",
            "dddDk...",
            "kkkk....",
        ]
        pal = {"k": TINTA, "o": ORO[3], "O": ORO[4], "Y": ORO[6], "d": ORO[1], "D": ORO[2]}
        L.sprite(filas, pal)
    return L


def barra_fondo():
    """El hueco vacio de la barra (se repite): casi negro, un poco transparente."""
    L = Lienzo(8, 5)
    for y in range(5):
        for x in range(8):
            L.px(x, y, rgba("#0a080d", 214) if y else rgba("#050407", 230))
    return L


def relleno(ramp, semilla):
    """Tramo de relleno de 32x5 que se repite: brillo arriba, sombra abajo y alguna mota."""
    r = np.random.default_rng(semilla)
    L = Lienzo(32, 5)
    filas = [5, 4, 4, 3, 2]
    for y in range(5):
        for x in range(32):
            L.px(x, y, ramp[filas[y]])
    for _ in range(9):
        x, y = int(r.integers(0, 32)), int(r.integers(1, 4))
        L.px(x, y, ramp[filas[y] + (1 if r.random() < 0.5 else -1)])
    for x in range(0, 32, 8):
        L.px(x + int(r.integers(0, 6)), 0, ramp[6])
    return L


def rastro():
    L = Lienzo(8, 5)
    cols = ["#f6e7a8", "#ecd68a", "#e2c676", "#cfae5e", "#a88a44"]
    for y in range(5):
        for x in range(8):
            L.px(x, y, cols[y])
    return L


def brillo():
    """El destello que recorre la barra (se dibuja sumando): una cuna blanca que se apaga a los lados."""
    L = Lienzo(14, 5)
    for x in range(14):
        k = 1 - abs(x - 6.5) / 7.0
        for y in range(5):
            a = int(120 * k * k * (1.0 if y < 2 else 0.55))
            L.px(x, y, (255, 255, 255, a))
    return L


# ---------------------------------------------------------------- los Ecos de la Sombra
ECO = [
    "................",
    ".......kk.......",
    "......krrk......",
    "......krRk......",
    ".....krRRk.k....",
    "....kkRROrkrk...",
    "...krkROORkRk...",
    "...kRkROYORRk...",
    "...kRRROYYORk...",
    "...kRROYWYORk...",
    "...krROYYOORk...",
    "....krROOORk....",
    "....kkrRRRrk....",
    "......krrrk.....",
    ".......kkk......",
    "................",
]
ECO_PAL = {"k": TINTA, "r": ROJO[2], "R": ROJO[4], "O": ROJO[5], "Y": ROJO[6], "W": "#ffe6d4"}


def eco_icono(fase=0):
    """El eco: una llama de sombra roja (16x16); con la fase late el nucleo."""
    pal = dict(ECO_PAL)
    if fase == 1:
        pal["Y"], pal["W"] = "#ff9c88", "#fff4ea"
    elif fase == 2:
        pal["O"], pal["Y"], pal["W"] = ROJO[4], ROJO[5], ROJO[6]
    return Lienzo(16, 16).sprite(ECO, pal)


ALTO_PLACA = 22


def placa(w, parte):
    """La placa de los ecos: losa de piedra con doble filo de oro. parte: 'izq' (con el hueco del icono), 'med', 'der'."""
    L = Lienzo(w, ALTO_PLACA)
    base = piedra(w, ALTO_PLACA, semilla=7 + w, base=2)
    L.a[:] = base.a
    for x in range(w):
        L.px(x, 0, TINTA).px(x, ALTO_PLACA - 1, TINTA)
        L.px(x, 1, ORO[4] if x % 9 == 2 else ORO[3]).px(x, 2, ORO[1])
        L.px(x, ALTO_PLACA - 2, ORO[1] if x % 9 != 6 else ORO[2]).px(x, ALTO_PLACA - 3, PIEDRA[0])
        L.px(x, 3, PIEDRA[4])
    if parte == "izq":
        # extremo redondeado con el engaste del icono (un circulo de oro de 20 de diametro)
        L = Lienzo(w, ALTO_PLACA)
        c = 10.5
        for y in range(ALTO_PLACA):
            for x in range(w):
                dx, dy = x - c, y - c
                r = math.hypot(dx, dy)
                if x >= 11:
                    L.a[y, x] = base.a[y, x]
                    if y == 0 or y == ALTO_PLACA - 1:
                        L.px(x, y, TINTA)
                    elif y == 1:
                        L.px(x, y, ORO[3])
                    elif y == 2:
                        L.px(x, y, ORO[1])
                    elif y == 3:
                        L.px(x, y, PIEDRA[4])
                    elif y == ALTO_PLACA - 2:
                        L.px(x, y, ORO[1])
                    elif y == ALTO_PLACA - 3:
                        L.px(x, y, PIEDRA[0])
                if r <= 10.6:
                    if r > 9.6:
                        L.px(x, y, TINTA)
                    elif r > 7.7:
                        n = (dx * -0.62 + dy * -0.78) / max(r, 1e-6)
                        L.px(x, y, ORO[max(0, min(6, 3 + int(round(n * 2.3)) - (2 if r < 8.4 else 0)))])
                    elif r > 6.9:
                        L.px(x, y, ORO[0])
                    else:
                        L.px(x, y, PIEDRA[0] if (x * 3 + y) % 7 else PIEDRA[1])
    if parte == "der":
        # remate en punta con una gema roja
        L2 = Lienzo(w, ALTO_PLACA)
        for y in range(ALTO_PLACA):
            corte = abs(y - 10.5) * 0.62
            for x in range(w):
                if x <= w - 1 - corte:
                    L2.a[y, x] = L.a[y, x]
        L = L2
        for y in range(ALTO_PLACA):
            xs = [x for x in range(w) if L.a[y, x, 3] > 0]
            if xs:
                L.px(xs[-1], y, TINTA)
                if len(xs) > 1 and 0 < y < ALTO_PLACA - 1:
                    L.px(xs[-1] - 1, y, ORO[3] if y < 11 else ORO[1])
        gx = w - 9
        gema = ["..k..", ".kRk.", "kRYRk", ".kRk.", "..k.."]
        L.sprite(gema, {"k": TINTA, "R": ROJO[4], "Y": ROJO[6]}, gx, 9)
    return L


# ---------------------------------------------------------------- teclas y avisos
ALTO_TECLA = 13


def tecla(parte):
    """Una tecla de piedra con filo de oro (tres trozos de 4 px: el del medio se repite)."""
    filas = {
        "izq": [
            ".kkk",
            "kooo",
            "koLL",
            "koLp",
            "koLp",
            "koLp",
            "koLp",
            "koLp",
            "koLp",
            "kodd",
            "kDDD",
            "kDDD",
            ".kkk",
        ],
        "med": [
            "kkkk",
            "oooo",
            "LLLL",
            "pppp",
            "pppp",
            "pppp",
            "pppp",
            "pppp",
            "pppp",
            "dddd",
            "DDDD",
            "DDDD",
            "kkkk",
        ],
        "der": [
            "kkk.",
            "oook",
            "LLok",
            "pdok",
            "pdok",
            "pdok",
            "pdok",
            "pdok",
            "pdok",
            "ddok",
            "DDDk",
            "DDDk",
            "kkk.",
        ],
    }[parte]
    pal = {"k": TINTA, "o": ORO[3], "L": PIEDRA[6], "p": PIEDRA[4], "d": PIEDRA[2], "D": PIEDRA[1]}
    return Lienzo(4, ALTO_TECLA).sprite(filas, pal)


ALTO_AVISO = 19


def aviso(parte):
    """El cartelito de los avisos ('Pulsa [E] para...'): banda oscura con filos de oro y puntas."""
    w = 12 if parte != "med" else 8
    L = Lienzo(w, ALTO_AVISO)
    for y in range(ALTO_AVISO):
        for x in range(w):
            if parte == "med":
                dentro = True
            else:
                xx = x if parte == "izq" else w - 1 - x
                dentro = xx >= abs(y - 9) * 0.9 + 0.5
            if not dentro:
                continue
            if y == 0 or y == ALTO_AVISO - 1:
                col = rgba(TINTA, 235)
            elif y == 1:
                col = rgba(ORO[3])
            elif y == ALTO_AVISO - 2:
                col = rgba(ORO[1])
            else:
                col = rgba("#0b090f", 218) if (x + y) % 6 else rgba("#110e17", 218)
            L.px(x, y, col)
    if parte != "med":
        # el filo de la punta
        for y in range(ALTO_AVISO):
            xs = [x for x in range(w) if L.a[y, x, 3] > 0]
            if not xs:
                continue
            x = xs[0] if parte == "izq" else xs[-1]
            L.px(x, y, rgba(TINTA, 235))
            x2 = x + 1 if parte == "izq" else x - 1
            if 0 <= x2 < w and 0 < y < ALTO_AVISO - 1:
                L.px(x2, y, ORO[3] if y < 9 else ORO[1])
    return L


# ---------------------------------------------------------------- florituras de los carteles
def floritura():
    """Adorno horizontal de oro (72x11) para poner a los lados del texto de los carteles (este mira a la derecha)."""
    filas = [
        "........................................................................",
        ".................................................................o......",
        "................................................................oYo.....",
        "..............................................ooo.....oo.......oYWYo....",
        "o.o.o.oo.ooo.ooooooooooooooooooooooooooooooooOYYOoooooOYOoooooOYWRWYOooo",
        "......dd.ddd.ddddddddddddddddddddddddddddddddDOODdddddDODdddddDOYWYODddd",
        "..............................................ddd.....dd.......dOYOd....",
        "................................................................dOd.....",
        ".................................................................d......",
        "........................................................................",
        "........................................................................",
    ]
    pal = {"o": ORO[4], "O": ORO[5], "Y": ORO[6], "W": "#fff6d8", "d": ORO[1], "D": ORO[2], "R": ROJO[4]}
    return Lienzo(72, 11).sprite(filas, pal)


# ---------------------------------------------------------------- la cenefa de las cinematicas
ALTO_CENEFA = 9


def cenefa():
    """La cenefa (32x9, se repite): filo de piedra, oro con tachuelas y una linea roja; debajo, sombra en tramado."""
    L = Lienzo(32, ALTO_CENEFA)
    for x in range(32):
        L.px(x, 0, PIEDRA[1])
        L.px(x, 1, PIEDRA[3] if x % 4 else PIEDRA[4])
        L.px(x, 2, ORO[1])
        L.px(x, 3, ORO[3])
        L.px(x, 4, ORO[2])
        L.px(x, 5, ROJO[2])
        L.px(x, 6, TINTA)
        if (x + 0) % 2 == 0:
            L.px(x, 7, rgba(TINTA, 150))
        if (x + 1) % 4 == 0:
            L.px(x, 8, rgba(TINTA, 90))
    # una tachuela con forma de rombo cada 16 px y muescas entre medias
    for x0 in (7, 23):
        rombo = [".oYo.", "oYWYo", ".dOd."]
        L.sprite(rombo, {"o": ORO[4], "Y": ORO[6], "W": "#fff6d8", "d": ORO[1], "O": ORO[3]}, x0 - 2, 2)
    for x0 in (15, 31):
        L.px(x0, 3, ORO[5]).px(x0, 5, ROJO[4])
    return L


def cine_centro():
    """El adorno del centro de la cenefa (64x17): el ojo de la Sombra entre dos alas de oro."""
    filas = [
        "................................................................",
        "...........................kkkkkkkkkk...........................",
        "..................kkkkkkkkkoOOOYYOOOokkkkkkkkk..................",
        ".........kkkkkkkkkoooOOOOOOkkkkkkkkkkOOOOOOoookkkkkkkkk.........",
        "kkkkkkkkkoooOOOOOOOkkkkkkkkrrRRRRRRrrkkkkkkkkOOOOOOOoookkkkkkkkk",
        "oooooooooOOOkkkkkkkrrRRRROOOOkkkkOOOORRRRrrkkkkkkkOOOooooooooooo",
        "dddddddddkkk...kkrrRROOOOYYYYkWWkYYYYOOOORRrrkk...kkkddddddddddd",
        "kkkkkkkkk.....krRROOYYYYYYYYYkWWkYYYYYYYYYOORRrk.....kkkkkkkkkkk",
        "...............kkrRROOOOYYYYYkWWkYYYYYOOOORRrkk.................",
        ".................kkkrrRRRROOOOkkkkOOOORRRRrrkkk.................",
        "....................kkkkrrrRRRRRRRRRRrrrkkkk....................",
        "........................kkkkdddDDDDdddkkkk......................",
        "...........................kkdDDOODDdkk.........................",
        ".............................kdDOODdk...........................",
        "..............................kdDDdk............................",
        "...............................kddk.............................",
        "................................kk..............................",
    ]
    pal = {"k": TINTA, "o": ORO[3], "O": ORO[4], "Y": ROJO[5], "W": TINTA, "d": ORO[1], "D": ORO[2], "r": ROJO[2], "R": ROJO[4]}
    L = Lienzo(64, 17).sprite(filas, pal)
    # el brillo del ojo y las alas con un filo claro
    L.px(27, 6, "#ffd8c8")
    for x in list(range(12, 19)) + list(range(45, 52)):
        if L.leer(x, 4)[:3] == rgba(ORO[4])[:3]:
            L.px(x, 4, ORO[5])
    return L


# ---------------------------------------------------------------- la brujula
ALTO_BRUJULA = 13


def brujula(parte):
    """La banda de la brujula: oscura y medio transparente, con filos de oro que se apagan hacia las puntas."""
    w = 24 if parte != "med" else 8
    L = Lienzo(w, ALTO_BRUJULA)
    for x in range(w):
        k = 1.0 if parte == "med" else ((x + 1) / w if parte == "izq" else (w - x) / w)
        for y in range(ALTO_BRUJULA):
            if y == 0 or y == ALTO_BRUJULA - 1:
                c = rgba(mezcla(ORO[1], ORO[3], k)[:3], int(255 * k))
            else:
                c = rgba("#08070b", int(150 * k)) if (x + y) % 2 or k > 0.6 else rgba("#08070b", int(100 * k))
            L.px(x, y, c)
    return L


def brujula_aguja():
    filas = [
        "kkkkkkkkk",
        "kOYYYYYOk",
        ".kOYWYOk.",
        "..kOYOk..",
        "...kOk...",
        "....k....",
    ]
    return Lienzo(9, 6).sprite(filas, {"k": TINTA, "O": ORO[4], "Y": ORO[6], "W": "#fff6d8"})


# ---------------------------------------------------------------- botones (pantalla de muerte, menus)
ALTO_BOTON = 20


def boton(parte, luz=False):
    """Un boton de piedra con doble filo de oro (tres trozos; el del medio, de 16 px, se repite). luz: raton encima."""
    w = 16 if parte == "med" else 8
    L = Lienzo(w, ALTO_BOTON)
    cuerpo = piedra(64, ALTO_BOTON, semilla=31 if not luz else 32, base=3 if not luz else 4)
    oro_a, oro_b, oro_c = (ORO[5], ORO[6], ORO[3]) if luz else (ORO[3], ORO[4], ORO[1])
    for y in range(ALTO_BOTON):
        for x in range(w):
            xx = x if parte != "der" else w - 1 - x          # la derecha es la izquierda en espejo
            borde = xx if parte != "med" else 99
            # esquinas achaflanadas
            if parte != "med" and xx + min(y, ALTO_BOTON - 1 - y) < 2:
                continue
            if y == 0 or y == ALTO_BOTON - 1 or (parte != "med" and (xx == 0 or xx + min(y, ALTO_BOTON - 1 - y) == 2)):
                c = TINTA
            elif y == 1 or (borde == 1 and y < ALTO_BOTON // 2):
                c = oro_b if (x % 5 == 2 and y == 1) else oro_a
            elif y == ALTO_BOTON - 2 or borde == 1:
                c = oro_c
            elif y == 2 or borde == 2:
                c = PIEDRA[6] if luz else PIEDRA[5]
            elif y == ALTO_BOTON - 3:
                c = PIEDRA[1]
            else:
                c = tuple(int(v) for v in cuerpo.a[y, (x * 3 + (0 if parte == "izq" else 23 if parte == "med" else 47)) % 64])
                if luz and (x + y) % 3 == 0:
                    c = mezcla(c, ROJO[2], 0.35)
            L.px(x, y, c)
    if parte != "med":
        # una tachuela de oro en el extremo
        x0 = 4 if parte == "izq" else w - 5
        for (dx, dy, col) in ((0, -1, oro_b), (-1, 0, oro_a), (0, 0, ORO[6]), (1, 0, oro_a), (0, 1, oro_c)):
            L.px(x0 + dx, ALTO_BOTON // 2 + dy, col)
    return L


# ---------------------------------------------------------------- marcas de la brujula
def marca_objetivo():
    filas = [
        "...k...",
        "..kRk..",
        ".kRYRk.",
        "kRYWYRk",
        ".kRYRk.",
        "..kRk..",
        "...k...",
    ]
    return Lienzo(7, 7).sprite(filas, {"k": TINTA, "R": ROJO[4], "Y": ROJO[5], "W": "#ffd8c8"})


def marca_espada():
    """La espada de luz en pequeno (7x11): para la brujula."""
    filas = [
        "..kkk..",
        "..kWk..",
        ".kkOkk.",
        "kOOYOOk",
        ".kkWkk.",
        "..kWk..",
        "..kWk..",
        "..kYk..",
        "..kWk..",
        "..kYk..",
        "...k...",
    ]
    return Lienzo(7, 11).sprite(filas, {"k": TINTA, "W": "#fff6d8", "Y": ORO[6], "O": ORO[5]})


# ---------------------------------------------------------------- la piedra de las bandas de cine
def banda():
    """Piedra casi negra (32x16, se repite a lo ancho y a lo alto): negro con un grano que apenas se ve."""
    r = np.random.default_rng(77)
    L = Lienzo(32, 16, "#060509")
    for _ in range(46):
        L.px(int(r.integers(0, 32)), int(r.integers(0, 16)), "#0b0a10")
    for _ in range(14):
        L.px(int(r.integers(0, 32)), int(r.integers(0, 16)), "#030305")
    return L



# ---------------------------------------------------------------- montar la textura
# ---------------------------------------------------------------- la barra del corcel (parte 7b)
# niebla de almas: de casi negro azulado a casi blanco
NIEBLA = ["#0a1418", "#12262e", "#1e3c48", "#2e5a66", "#4a8890", "#86c4c8", "#d4f4f2"]

CORCEL_ICONO = [
    ".f.....f.",
    "fNf...fNf",
    "oHo...oHo",
    "oHo...oHo",
    "oho...oho",
    "oHHo.oHHo",
    ".oHHoHHo.",
    "..ohHho..",
    "...ooo...",
]


def corcel_icono():
    """Una herradura de hueso con fuego de almas en las puntas, junto a la barra de la vida del corcel."""
    L = Lienzo(9, 9)
    col = {"o": TINTA, "H": HUESO[4], "h": HUESO[2], "N": NIEBLA[6], "f": NIEBLA[4]}
    for y, fila in enumerate(CORCEL_ICONO):
        for x, ch in enumerate(fila):
            if ch in col:
                L.px(x, y, col[ch])
    return L


# ---------------------------------------------------------------- los santuarios de Morvain en la brujula (parte 7c)
SANTUARIO_MARCA = [
    "...f...",
    "..fFf..",
    ".fFWFf.",
    ".fFWFf.",
    "..fFf..",
    ".kPPPk.",
    "kPpPpPk",
    "kPPPPPk",
    ".kkkkk.",
]


def marca_santuario(encendido):
    """Un santuario en pequeno (7x9): el altar de piedra con su llama de almas (o, si aun no lo conoce, apagado)."""
    if encendido:
        col = {"f": NIEBLA[4], "F": NIEBLA[5], "W": NIEBLA[6], "k": TINTA, "P": PIEDRA[6], "p": PIEDRA[4]}
    else:
        col = {"f": "#3c3a42", "F": "#5c5862", "W": "#7c7882", "k": TINTA, "P": PIEDRA[5], "p": PIEDRA[3]}
    return Lienzo(7, 9).sprite(SANTUARIO_MARCA, col)


def montar():
    A = Atlas(256, 128)
    A.poner("emblema", emblema())
    A.poner("barra_izq", barra_marco(3, izq=True))
    A.poner("barra_med", barra_marco(14))
    A.poner("barra_punta", barra_marco(8, punta=True))
    A.poner("barra_fondo", barra_fondo())
    A.poner("vida", relleno(ROJO, 11))
    A.poner("mana", relleno(AZUL, 12))
    A.poner("estamina", relleno(VERDE, 13))
    A.poner("rastro", rastro())
    A.poner("brillo", brillo())
    for i in range(3):
        A.poner(f"eco_{i}", eco_icono(i))
    A.poner("placa_izq", placa(24, "izq"))
    A.poner("placa_med", placa(18, "med"))
    A.poner("placa_der", placa(16, "der"))
    for p in ("izq", "med", "der"):
        A.poner("tecla_" + p, tecla(p))
    for p in ("izq", "med", "der"):
        A.poner("aviso_" + p, aviso(p))
    A.poner("floritura", floritura())
    A.poner("cenefa", cenefa())
    A.poner("cine_centro", cine_centro())
    for p in ("izq", "med", "der"):
        A.poner("brujula_" + p, brujula(p))
    A.poner("brujula_aguja", brujula_aguja())
    A.poner("marca_objetivo", marca_objetivo())
    A.poner("marca_espada", marca_espada())
    for p in ("izq", "med", "der"):
        A.poner("boton_" + p, boton(p))
    for p in ("izq", "med", "der"):
        A.poner("boton_luz_" + p, boton(p, True))
    A.poner("banda", banda())
    A.poner("corcel", relleno(NIEBLA, 14))
    A.poner("corcel_icono", corcel_icono())
    A.poner("marca_santuario", marca_santuario(True))
    A.poner("marca_santuario_apagado", marca_santuario(False))
    A.L.guardar(os.path.join(GUI, "hud.png"))
    ruta = os.path.join(REPO, "src/main/java/com/darkcraft/client/ui/HudTex.java")
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    with open(ruta, "w", encoding="utf-8") as fh:
        fh.write(A.java("com.darkcraft.client.ui", "HudTex", "textures/gui/hud.png", "Los trozos de la textura del HUD nuevo"))
    print("hud.png", A.L.w, "x", A.L.h, "con", len(A.sitios), "trozos")
    return A


if __name__ == "__main__":
    A = montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        im = A.L.imagen()
        fondo = Lienzo(A.L.w, A.L.h, "#3a4a5c")
        fondo.pegar(A.L, 0, 0)
        fondo.imagen().resize((A.L.w * 5, A.L.h * 5), 0).save(os.path.join(d, "hud_atlas.png"))
        print("vista en", d)
