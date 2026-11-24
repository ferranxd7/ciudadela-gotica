#!/usr/bin/env python3
"""
Las texturas de los menus del mod en pixel art (assets/darkcraft/textures/gui/menu.png) y sus sitios
(com/darkcraft/client/ui/MenuTex.java):

  panel_*      una losa de piedra oscura con doble marco de oro gastado y tachuelas en las esquinas, en nueve trozos
               (esquinas de 12, bordes y centro que se repiten)
  hueco_*      un rebaje oscuro dentro de la losa (para listas y numeros), en nueve trozos (esquinas de 4)
  fila_*       la barra que marca la fila elegida de una lista, en tres trozos
  flecha_*     flechas para subir y bajar un numero (normal, con el raton encima y apagada)
  at_*         los iconos de los ocho atributos
  frasco_*     los frascos de vida (rojo) y de mana (azul), llenos y vacios; tambien salen sueltos como texturas de objeto
  santuario    el icono del santuario (la hoguera de almas)

    python3 tools/ui/menu.py [--vista carpeta]
"""
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from arte import (ASSETS, AZUL, GUI, HUESO, ORO, PIEDRA, REPO, ROJO, TINTA, VERDE, Atlas, Lienzo, mezcla, piedra,  # noqa: E402
                  rgba)

# una paleta con letras para los sprites escritos como texto
PAL = {
    "k": TINTA,
    "r": ROJO[2], "R": ROJO[4], "O": ROJO[5], "Y": ROJO[6], "W": "#fff0e4",
    "b": AZUL[2], "B": AZUL[4], "C": AZUL[5], "D": AZUL[6],
    "g": VERDE[2], "G": VERDE[4], "L": VERDE[5], "M": VERDE[6],
    "q": ORO[2], "Q": ORO[4], "o": ORO[5], "y": ORO[6],
    "s": PIEDRA[5], "S": PIEDRA[7], "T": "#b8b2c4", "U": "#e6e2ee",
    "h": HUESO[2], "H": HUESO[4],
    "p": PIEDRA[2], "P": PIEDRA[3],
    "f": "#9a5a12", "F": "#f0a020", "e": "#ffe070", "E": "#fff8d8",
    "c": "#3aa0c8", "x": "#8fe4f4", "X": "#e8fcff",
}


# ---------------------------------------------------------------- la losa de los paneles
ESQ = 12


def losa():
    """Una losa de 12 + 32 + 12 de lado: de aqui se cortan los nueve trozos."""
    n = ESQ * 2 + 32
    L = Lienzo(n, n)
    base = piedra(n, n, semilla=51, base=2)
    L.a[:] = base.a
    r = np.random.default_rng(52)
    # alguna veta mas clara y alguna grieta
    for _ in range(26):
        x, y = int(r.integers(6, n - 6)), int(r.integers(6, n - 6))
        L.px(x, y, PIEDRA[3]).px(x + 1, y, PIEDRA[3])
    for i in range(n):
        for (d, arriba, abajo) in ((0, TINTA, TINTA), (1, ORO[4], ORO[1]), (2, ORO[3], ORO[2]), (3, PIEDRA[0], PIEDRA[0]),
                                   (4, ORO[2], ORO[1])):
            L.px(i, d, arriba).px(i, n - 1 - d, abajo)
            L.px(d, i, arriba).px(n - 1 - d, i, abajo)
    # las esquinas, bien rematadas (lo de arriba manda en la diagonal)
    for d in range(5):
        col_a = [TINTA, ORO[4], ORO[3], PIEDRA[0], ORO[2]][d]
        col_b = [TINTA, ORO[1], ORO[2], PIEDRA[0], ORO[1]][d]
        for i in range(d, n - d):
            L.px(i, d, col_a).px(d, i, col_a)
            L.px(i, n - 1 - d, col_b).px(n - 1 - d, i, col_b)
    # destellos en el filo de arriba
    for x in range(9, n - 9, 7):
        L.px(x, 1, ORO[5])
    # una sombra suave por dentro del marco
    for i in range(5, n - 5):
        L.px(i, 5, PIEDRA[1]).px(5, i, PIEDRA[1])
        L.px(i, n - 6, PIEDRA[3]).px(n - 6, i, PIEDRA[3])
    # tachuelas en las cuatro esquinas: un rombo de oro con una gema roja
    rombo = ["..kkk..", ".kqQqk.", "kqQyQqk", "kQyRyQk", "kqQyQqk", ".kqQqk.", "..kkk.."]
    for (x, y) in ((1, 1), (n - 8, 1), (1, n - 8), (n - 8, n - 8)):
        L.sprite(rombo, PAL, x, y)
    return L


def nueve(L, esq):
    """Corta un lienzo cuadrado en nueve trozos: (nombre, trozo)."""
    n = L.w
    m = n - 2 * esq
    cortes = {
        "ai": (0, 0, esq, esq), "a": (esq, 0, m, esq), "ad": (n - esq, 0, esq, esq),
        "i": (0, esq, esq, m), "c": (esq, esq, m, m), "d": (n - esq, esq, esq, m),
        "bi": (0, n - esq, esq, esq), "b": (esq, n - esq, m, esq), "bd": (n - esq, n - esq, esq, esq),
    }
    return [(k, L.recorte(*v)) for k, v in cortes.items()]


def hueco():
    """Un rebaje oscuro (4 + 16 + 4): sombra arriba y a la izquierda, luz abajo y a la derecha."""
    n = 24
    L = Lienzo(n, n, "#0a0810")
    r = np.random.default_rng(61)
    for _ in range(30):
        L.px(int(r.integers(2, n - 2)), int(r.integers(2, n - 2)), "#0e0c15")
    for i in range(n):
        L.px(i, 0, TINTA).px(0, i, TINTA)
        L.px(i, n - 1, PIEDRA[5]).px(n - 1, i, PIEDRA[5])
    for i in range(1, n - 1):
        L.px(i, 1, "#050408").px(1, i, "#050408")
        L.px(i, n - 2, PIEDRA[2]).px(n - 2, i, PIEDRA[2])
    L.px(0, n - 1, PIEDRA[3]).px(n - 1, 0, PIEDRA[3])
    return L


# ---------------------------------------------------------------- la fila elegida
ALTO_FILA = 18


def fila(parte):
    w = 10 if parte != "med" else 8
    L = Lienzo(w, ALTO_FILA)
    for x in range(w):
        xx = x if parte == "izq" else (w - 1 - x if parte == "der" else 99)
        k = 1.0 if parte == "med" else min(1.0, (xx + 1) / w)
        for y in range(ALTO_FILA):
            if y in (0, ALTO_FILA - 1):
                c = rgba(mezcla(ORO[1], ORO[4], k)[:3], int(255 * k))
            elif y in (1, ALTO_FILA - 2):
                c = rgba(ROJO[2], int(200 * k))
            else:
                t = 1 - abs(y - (ALTO_FILA - 1) / 2) / (ALTO_FILA / 2)
                c = rgba(mezcla(ROJO[0], ROJO[2], t * 0.9)[:3], int(215 * k))
            L.px(x, y, c)
    return L


# ---------------------------------------------------------------- flechas
def flecha(derecha, tono):
    """tono: 0 normal, 1 con el raton encima, 2 apagada."""
    filas = [
        "....k..",
        "...kXk.",
        "..kXxk.",
        ".kXxxk.",
        "kXxxxk.",
        ".kxxdk.",
        "..kxdk.",
        "...kdk.",
        "....k..",
    ]
    pal = [{"X": ORO[5], "x": ORO[4], "d": ORO[2]}, {"X": "#fff6d8", "x": ORO[6], "d": ORO[4]},
           {"X": PIEDRA[6], "x": PIEDRA[5], "d": PIEDRA[3]}][tono]
    pal["k"] = TINTA
    L = Lienzo(7, 9).sprite(filas, pal)
    return L.espejo_h() if derecha else L


# ---------------------------------------------------------------- los ocho atributos
ATRIBUTOS = {
    "vigor": [
        "................",
        "................",
        "...kkk....kkk...",
        "..kOOOk..kRRRk..",
        ".kOYYORkkRRRRRk.",
        ".kOYWORRRRRRRRk.",
        ".kROOORRRRRRRrk.",
        ".kRRRRRRRRRRRrk.",
        "..kRRRRRRRRRrk..",
        "...kRRRRRRRrk...",
        "....kRRRRRrk....",
        ".....kRRRrk.....",
        "......kRrk......",
        ".......kk.......",
        "................",
        "................",
    ],
    "mente": [
        "................",
        ".......kk.......",
        "......kDDk......",
        "......kCDk......",
        ".....kBCDBk.....",
        "..kkkkBCDCBkkk..",
        ".kCCBBCDXDCBBCk.",
        ".kBCBBCDXDCBBBk.",
        "..kkkkBCDCBkkk..",
        ".....kBCDBk.....",
        "......kBCk......",
        "......kBCk......",
        ".......kk.......",
        "................",
        "................",
        "................",
    ],
    "aguante": [
        "................",
        "..........kkk...",
        ".........kMMLk..",
        ".......kkMLLLk..",
        "......kMMLLGLk..",
        ".....kMLLLGGLk..",
        "....kMLLGGGGLk..",
        "....kLLGGLGGk...",
        "...kLLGGLGGGk...",
        "...kLGGLGGGgk...",
        "...kLGLGGGgk....",
        "....kLGGGgkk....",
        "...kgkggkk......",
        "..kgk.kk........",
        "..kk............",
        "................",
    ],
    "fuerza": [
        "................",
        "..kkkkkkkkk.....",
        ".kUTTTTTTTSk....",
        ".kTTTTTTTSSk....",
        ".kTSSSSSSSsk....",
        ".kSSSSSSSssk....",
        "..kkkkoQkkk.....",
        "......kQqk......",
        "......koQqk.....",
        ".......koQqk....",
        "........koQqk...",
        ".........koQqk..",
        "..........koQqk.",
        "...........kqqk.",
        "............kk..",
        "................",
    ],
    "destreza": [
        "................",
        "............kk..",
        "...........kUTk.",
        "..........kUTSk.",
        ".........kUTSk..",
        "........kUTSk...",
        ".......kUTSk....",
        "......kUTSk.....",
        ".kk..kUTSk......",
        ".kokkTTSk.......",
        "..koQTsk........",
        "...kQoQk........",
        "..kqQkoQk.......",
        ".kqQk.kkok......",
        ".kkk....kk......",
        "................",
    ],
    "inteligencia": [
        "................",
        ".......kk.......",
        "......kXXk......",
        ".....kXXxck.....",
        "....kXXxxcck....",
        "...kXXxxxcccK...",
        "...kXxxxccccK...",
        "...kxxxccccbK...",
        "....kxcccbbk....",
        "....kcccbbbk....",
        ".....kccbbk.....",
        ".....kcbbbk.....",
        "......kbbk......",
        "......kbbk......",
        ".......kk.......",
        "................",
    ],
    "fe": [
        "................",
        ".......kk.......",
        "......kFek......",
        ".....kFeEek.....",
        ".....kFeEEek....",
        ".....kfFeEek....",
        "......kfFek.....",
        ".......kkk......",
        ".....kkHHkk.....",
        ".....kHHHhk.....",
        ".....kHHHhk.....",
        ".....kHHhhk.....",
        ".....kHHhhk.....",
        "....kkhhhhkk....",
        "...koQQQQQqqk...",
        "....kkkkkkkk....",
    ],
    "arcano": [
        "................",
        "................",
        "......kkkk......",
        "...kkkrRRrkkk...",
        "..krRROOOORRrk..",
        ".krROOYkkYOORrk.",
        ".kRROYYkkYYORRk.",
        ".krROOYkkYOORrk.",
        "..krRROOOORRrk..",
        "...kkkrRRrkkk...",
        "......kkkk......",
        ".....kr..rk.....",
        "....kr....rk....",
        "....k......k....",
        "................",
        "................",
    ],
}


def atributo(nombre):
    pal = dict(PAL)
    pal["K"] = TINTA
    return Lienzo(16, 16).sprite(ATRIBUTOS[nombre], pal)


# ---------------------------------------------------------------- frascos
FRASCO = [
    "................",
    "......kkkk......",
    "......koQk......",
    ".....kkQqkk.....",
    ".....kUTTSk.....",
    "......kTSk......",
    ".....kkTSkk.....",
    "....kT1111Sk....",
    "...kT122222Sk...",
    "...k12322222k...",
    "..kT123222224k..",
    "..kT122222244k..",
    "..kS222222444k..",
    "...k22222444k...",
    "....kk4444kk....",
    "......kkkk......",
]


def frasco(color, lleno=True):
    pal = dict(PAL)
    if not lleno:
        pal.update({"1": PIEDRA[4], "2": PIEDRA[2], "3": PIEDRA[4], "4": PIEDRA[1]})
    elif color == "rojo":
        pal.update({"1": ROJO[6], "2": ROJO[4], "3": "#ffd8c8", "4": ROJO[2]})
    else:
        pal.update({"1": AZUL[6], "2": AZUL[4], "3": "#e0f0ff", "4": AZUL[2]})
    return Lienzo(16, 16).sprite(FRASCO, pal)


# ---------------------------------------------------------------- el santuario
SANTUARIO = [
    "................",
    ".......x........",
    "......xXx.......",
    "......xXXx......",
    ".....cxXXxc.....",
    ".....cxXxxc.....",
    "..h...cxxc...h..",
    "..hh.kcccck.hh..",
    "..kHhkkkkkkhHk..",
    "...kHhSSSShHk...",
    "....khsssshk....",
    ".....kssssk.....",
    ".....kPssPk.....",
    "....kPPssPPk....",
    "...kpPPPPPPpk...",
    "...kkkkkkkkkk...",
]


def santuario():
    return Lienzo(16, 16).sprite(SANTUARIO, PAL)


# ---------------------------------------------------------------- montar
def montar():
    A = Atlas(256, 128)
    for k, t in nueve(losa(), ESQ):
        A.poner("panel_" + k, t)
    for k, t in nueve(hueco(), 4):
        A.poner("hueco_" + k, t)
    for p in ("izq", "med", "der"):
        A.poner("fila_" + p, fila(p))
    for i, n in enumerate(("", "_luz", "_gris")):
        A.poner("flecha_izq" + n, flecha(False, i))
        A.poner("flecha_der" + n, flecha(True, i))
    for n in ATRIBUTOS:
        A.poner("at_" + n, atributo(n))
    for c in ("rojo", "azul"):
        nombre = "vida" if c == "rojo" else "mana"
        A.poner("frasco_" + nombre, frasco(c))
        A.poner("frasco_" + nombre + "_vacio", frasco(c, False))
    A.poner("santuario", santuario())
    A.L.guardar(os.path.join(GUI, "menu.png"))
    ruta = os.path.join(REPO, "src/main/java/com/darkcraft/client/ui/MenuTex.java")
    with open(ruta, "w", encoding="utf-8") as fh:
        fh.write(A.java("com.darkcraft.client.ui", "MenuTex", "textures/gui/menu.png", "Los trozos de la textura de los menus"))
    # los frascos, tambien como texturas de objeto
    for c, nombre in (("rojo", "frasco_vida"), ("azul", "frasco_mana")):
        frasco(c).guardar(os.path.join(ASSETS, "textures", "item", nombre + ".png"))
    print("menu.png", A.L.w, "x", A.L.h, "con", len(A.sitios), "trozos")
    return A


if __name__ == "__main__":
    A = montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        fondo = Lienzo(A.L.w, A.L.h, "#3a4a5c")
        fondo.pegar(A.L, 0, 0)
        fondo.imagen().resize((A.L.w * 5, A.L.h * 5), 0).save(os.path.join(d, "menu_atlas.png"))
        print("vista en", d)
