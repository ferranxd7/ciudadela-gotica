#!/usr/bin/env python3
"""
Los hechizos en pixel art (parte 2d-2): el BASTON (hechiceria, escala con Inteligencia), el SELLO (milagros, escala con
Fe) y los hechizos que se aprenden (pergaminos azules los de baston, plegarias de lacre rojo los de sello). Texturas de
objeto de 16 x 16 y sus modelos (el baston y el sello se llevan como herramientas, en diagonal).

    python3 tools/rol/hechizos.py [--vista carpeta]
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, ORO, TINTA, Lienzo, rgba  # noqa: E402

PAL = {
    "k": TINTA,
    "w": "#3a2414", "W": "#5e3a1e", "V": "#8a5a30",          # madera
    "q": ORO[2], "Q": ORO[4], "y": ORO[6],                   # oro
    "b": "#142866", "B": "#2f5cc6", "C": "#8fb8ff", "S": "#f0f6ff",  # azul (hechiceria)
    "r": "#5e0c13", "R": "#b81c26", "O": "#f4786a",          # rojo (milagros)
    "p": "#6e6650", "P": "#c4bb9e", "H": "#e2dcc4", "h": "#9c9378",  # pergamino
    "e": "#f2d640", "E": "#fffbd0", "f": "#e86418", "F": "#ffd060", "g": "#3c9a3a", "G": "#b0e89a",
    "v": "#46308e", "x": "#9c88e6",
}

BASTON = [
    "............kkk.",
    "...........kCSBk",
    "..........kqBCBk",
    ".........kQqkBk.",
    "........kWQk.k..",
    ".......kWVk.....",
    "......kWVk......",
    ".....kWVk.......",
    "....kWVk........",
    "...kQQk.........",
    "..kWVk..........",
    ".kWVk...........",
    ".kVk............",
    "..k.............",
    "................",
    "................",
]

SELLO = [
    ".......kkkk.....",
    ".....kkQyyQkk...",
    "....kQyQqqQyQk..",
    "....kyQRRRRQyk..",
    "...kQqRrOOrRqQk.",
    "...kyqROrrORqyk.",
    "...kyqROrrORqyk.",
    "...kQqRrOOrRqQk.",
    "....kyQRRRRQyk..",
    "....kQyQqqQyQk..",
    ".....kkQkkQkk...",
    "....kWVk..kk....",
    "...kWVk.........",
    "..kWVk..........",
    ".kVk............",
    "..k.............",
]

# los glifos de cada hechizo (7 x 7) sobre su pergamino
GLIFOS = {
    # hechiceria (baston)
    "guijarro": ("azul", [".......", "..BBB..", ".BCCCB.", ".BCSCB.", ".BCCCB.", "..BBB..", "......."]),
    "lanza": ("azul", ["......S", ".....SC", "....SC.", "...SC..", "..BC...", ".BB....", "B......"]),
    "esquirlas": ("azul", ["S...S..", ".C..C..", "..B..B.", "S...S..", ".C..C..", "..B..B.", "......."]),
    "espada": ("azul", ["...S...", "...S...", "...C...", "...C...", ".BBCBB.", "...B...", "...b..."]),
    # milagros (sello)
    "llama": ("rojo", ["...f...", "..fF...", "..fFf..", ".fFFFf.", ".fFEFf.", "..fFf..", "......."]),
    "rayo": ("rojo", ["....eE.", "...eE..", "..eEE..", ".eEEEe.", "...Ee..", "..Ee...", ".E....."]),
    "curar": ("rojo", ["...G...", "...G...", ".GGGGG.", "...G...", "...G...", "..ggg..", "......."]),
    "bendicion": ("rojo", ["E..y..E", ".E.y.E.", "..yyy..", "yyyEyyy", "..yyy..", ".E.y.E.", "E..y..E"]),
}


def pergamino(escuela):
    """Un pergamino enrollado arriba y abajo; los de milagro llevan un sello de lacre rojo."""
    L = Lienzo(16, 16)
    L.rect(3, 2, 10, 12, "#c4bb9e")
    for y in range(2, 14):
        L.px(3, y, "#9c9378").px(12, y, "#e2dcc4")
    # los rollos
    for y in (1, 14):
        L.rect(2, y, 12, 1, "#6e6650")
        L.px(2, y, "#3c3628").px(13, y, "#3c3628")
    L.rect(2, 2, 12, 1, "#9c9378")
    L.rect(2, 13, 12, 1, "#9c9378")
    if escuela == "azul":
        L.px(13, 1, "#2f5cc6").px(13, 14, "#2f5cc6")
    else:
        L.rect(10, 11, 3, 3, "#b81c26")
        L.px(11, 12, "#f4786a").px(10, 13, "#5e0c13")
    L.contorno(TINTA)
    return L


def hechizo(n):
    escuela, glifo = GLIFOS[n]
    L = pergamino(escuela)
    L.pegar(Lienzo(7, 7).sprite(glifo, PAL), 4, 4)
    return L


# el icono del efecto de la Bendicion (18 x 18, como los del juego): un sol dorado en un circulo suave
SOL = [
    "......E......",
    "..E...y...E..",
    "...E..y..E...",
    "....yyyyy....",
    "...yyFFFyy...",
    "...yFFEFFy...",
    "EyyyFEEEFyyyE",
    "...yFFEFFy...",
    "...yyFFFyy...",
    "....yyyyy....",
    "...E..y..E...",
    "..E...y...E..",
    "......E......",
]


def icono_bendicion():
    L = Lienzo(18, 18)
    for y in range(18):
        for x in range(18):
            d = math.hypot(x + 0.5 - 9, y + 0.5 - 9)
            if d <= 8.6:
                L.px(x, y, rgba("#f6e3a0", int(110 * max(0.0, 1 - d / 8.6) + 30)))
    L.pegar(Lienzo(13, 13).sprite(SOL, PAL), 2, 2)
    return L


def modelo(nombre, mano=False):
    ruta = os.path.join(ASSETS, "models", "item", nombre + ".json")
    with open(ruta, "w", encoding="utf-8") as f:
        json.dump({"parent": "item/handheld" if mano else "item/generated", "textures": {"layer0": "darkcraft:item/" + nombre}}, f)


def montar():
    hechos = []
    Lienzo(16, 16).sprite(BASTON, PAL).guardar(os.path.join(ASSETS, "textures", "item", "baston.png"))
    modelo("baston", True)
    Lienzo(16, 16).sprite(SELLO, PAL).guardar(os.path.join(ASSETS, "textures", "item", "sello.png"))
    modelo("sello", True)
    hechos += ["baston", "sello"]
    for n in GLIFOS:
        hechizo(n).guardar(os.path.join(ASSETS, "textures", "item", "hechizo_" + n + ".png"))
        modelo("hechizo_" + n)
        hechos.append("hechizo_" + n)
    icono_bendicion().guardar(os.path.join(ASSETS, "textures", "mob_effect", "bendicion.png"))
    print(len(hechos), "objetos:", " ".join(hechos), "+ icono del efecto bendicion")


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        todos = [Lienzo(16, 16).sprite(BASTON, PAL), Lienzo(16, 16).sprite(SELLO, PAL)] + [hechizo(n) for n in GLIFOS]
        hoja = Lienzo(20 * 6, 20 * 2, "#2a2430")
        for i, s in enumerate(todos):
            hoja.pegar(s, (i % 5) * 20 + 2, (i // 5) * 20 + 2)
        hoja.pegar(icono_bendicion(), 5 * 20 + 1, 1)
        hoja.imagen().resize((hoja.w * 6, hoja.h * 6), 0).save(os.path.join(d, "hechizos.png"))
        print("vista en", d)
