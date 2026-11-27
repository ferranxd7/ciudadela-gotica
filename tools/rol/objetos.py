#!/usr/bin/env python3
"""
Los objetos del rol en pixel art (parte 2d): los FRUTOS (dan resistencias o habilidades un rato) y los TALISMANES
(se equipan en sus huecos del menu). Cada uno: textura de objeto de 16 x 16 (potencia de dos), su modelo de objeto y,
los frutos, el icono de su efecto (18 x 18, como los del juego) en textures/mob_effect.

    python3 tools/rol/objetos.py [--vista carpeta]
"""
import json
import math
import os
import sys

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, HUESO, ORO, PIEDRA, ROJO, TINTA, Lienzo, mezcla, rgba  # noqa: E402

LUZ = (-0.55, -0.75)  # la luz llega de arriba a la izquierda


def rampa(cs, t):
    """Un color de la rampa (de oscuro a claro) para t en 0..1."""
    t = max(0.0, min(1.0, t))
    i = min(len(cs) - 1, int(round(t * (len(cs) - 1))))
    return cs[i]


def bola(L, cx, cy, rx, ry, cs, brillo=True, forma=None):
    """Un cuerpo redondeado (elipse, o la forma que se pase) sombreado con la rampa cs y un pixel de brillo."""
    pts = []
    for y in range(L.h):
        for x in range(L.w):
            dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
            dentro = forma(dx, dy) if forma else dx * dx + dy * dy <= 1.0
            if not dentro:
                continue
            nz = math.sqrt(max(0.0, 1 - min(1.0, dx * dx + dy * dy)))
            luz = -(dx * LUZ[0] + dy * LUZ[1]) * 0.55 + nz * 0.55
            L.px(x, y, rampa(cs, 0.15 + luz * 0.85))
            pts.append((x, y))
    if brillo and pts:
        bx = int(cx - rx * 0.45)
        by = int(cy - ry * 0.45)
        L.px(bx, by, cs[-1])
    return pts


def contorno(L, c=TINTA):
    return L.contorno(c)


# ---------------------------------------------------------------- frutos

FRUTOS = {
    # nombre: (rampa de colores del cuerpo, color del efecto)
    "brasa": (["#3a0a06", "#7a1608", "#b8320c", "#e86418", "#ffa83a", "#ffe08a"], "#e8641a"),
    "escarcha": (["#16324a", "#2e5a7c", "#4a8ab0", "#7cc0de", "#b8e6f6", "#f0fcff"], "#8fd0ee"),
    "tormenta": (["#3a2a06", "#6e5210", "#a8861c", "#dcbc30", "#f6e46a", "#fffbd0"], "#f2d640"),
    "lunar": (["#140c30", "#2a1a5e", "#46308e", "#6a52c0", "#9c88e6", "#dcd2ff"], "#7a62d0"),
    "ceniza": (["#1e2018", "#363a2c", "#545a44", "#767e60", "#9ca684", "#cfd6b8"], "#808a66"),
    "hueso": (HUESO, "#e2dcc4"),
    "sombra": (["#0a0406", "#1e070c", "#3a0c14", "#5e121c", "#8a1c26", "#c83a3a"], "#6a1020"),
    "pluma": (["#2a4a58", "#4a7a8a", "#78aab6", "#a8d4dc", "#d4eef0", "#ffffff"], "#bfe8ee"),
}


def fruto(n):
    cs, _ = FRUTOS[n]
    L = Lienzo(16, 16)
    if n == "brasa":
        # una granada: redonda, con corona arriba y grietas de brasa
        bola(L, 8, 9.5, 5.6, 5.4, cs)
        for x, y in ((6, 9), (7, 10), (9, 8), (10, 11), (8, 12)):
            L.px(x, y, "#ffd060")
        L.px(7, 3, cs[2]).px(8, 3, cs[3]).px(9, 3, cs[2]).px(8, 2, cs[1]).px(7, 4, cs[3]).px(9, 4, cs[3])
    elif n == "escarcha":
        # una pera helada con cristales de hielo
        bola(L, 8, 10, 5.0, 4.8, cs)
        bola(L, 8, 5.5, 3.0, 3.0, cs, brillo=False)
        for x, y in ((5, 9), (10, 7), (11, 11), (6, 12)):
            L.px(x, y, "#ffffff")
        L.px(8, 1, "#4a3a20").px(8, 2, "#6e5430").px(9, 2, "#2e6a3a").px(10, 1, "#3c9a3a")
    elif n == "tormenta":
        # una carambola: estrella de cinco puntas gorda, con chispas
        def estrella(dx, dy):
            a = math.atan2(dy, dx)
            r = math.hypot(dx, dy)
            return r <= 0.68 + 0.32 * (0.5 + 0.5 * math.cos(5 * (a + math.pi / 2)))
        bola(L, 8, 8.5, 6.4, 6.4, cs, forma=estrella)
        L.px(2, 3, "#fffbd0").px(13, 2, "#fffbd0").px(14, 12, "#f6e46a")
    elif n == "lunar":
        # una media luna gruesa
        def luna(dx, dy):
            return dx * dx + dy * dy <= 1.0 and (dx - 0.55) ** 2 + (dy + 0.15) ** 2 > 0.55
        bola(L, 8, 8.5, 6.2, 6.2, cs, forma=luna)
        L.px(12, 4, "#dcd2ff").px(13, 9, "#9c88e6")
    elif n == "ceniza":
        # un higo arrugado con motas de ceniza
        bola(L, 8, 10, 5.2, 5.0, cs)
        bola(L, 8, 5, 2.2, 2.6, cs, brillo=False)
        for x, y in ((6, 10), (9, 12), (10, 9), (7, 13), (5, 8)):
            L.px(x, y, cs[1])
        L.px(8, 2, "#2a2418")
    elif n == "hueso":
        # una calabacita de hueso
        bola(L, 8, 11, 5.0, 4.0, cs)
        bola(L, 8, 5.5, 3.0, 3.2, cs, brillo=False)
        L.px(7, 12, cs[1]).px(9, 10, cs[1]).px(8, 1, "#4a3a20").px(8, 2, "#6e5430")
    elif n == "sombra":
        # una ciruela casi negra con venas rojas
        bola(L, 8, 9.5, 5.5, 5.6, cs)
        for x, y in ((6, 8), (7, 9), (7, 10), (8, 11), (10, 9), (10, 10)):
            L.px(x, y, "#e0303a")
        L.px(8, 3, "#2a1a10").px(9, 2, "#3a2414")
    elif n == "pluma":
        # una baya blanca con dos alitas de pluma (cae despacio quien se la come)
        ala = Lienzo(6, 5).sprite([
            "WWC...",
            "CWWCc.",
            ".cCWCc",
            "...cCc",
            ".....c",
        ], {"c": "#78aab6", "C": "#d4eef0", "W": "#ffffff"})
        L.pegar(ala, 0, 6)
        L.pegar(ala.espejo_h(), 10, 6)
        bola(L, 8, 10.5, 4.0, 4.2, cs)
        L.px(8, 5, "#4a7a8a").px(8, 6, "#78aab6")
    if n not in ("escarcha", "hueso", "ceniza", "sombra", "pluma", "tormenta", "lunar"):
        pass
    # hojita (salvo los que no la llevan)
    if n in ("brasa", "ceniza", "sombra"):
        L.px(10, 3, "#2e6a3a").px(11, 3, "#3c9a3a").px(11, 2, "#6cc45c").px(10, 4, "#27702a")
    contorno(L)
    return L


def icono_efecto(n):
    """El icono del efecto (18 x 18): el fruto en un circulo suave de su color."""
    _, col = FRUTOS[n]
    L = Lienzo(18, 18)
    for y in range(18):
        for x in range(18):
            d = math.hypot(x + 0.5 - 9, y + 0.5 - 9)
            if d <= 8.6:
                a = int(110 * max(0.0, 1 - d / 8.6) + 30)
                L.px(x, y, rgba(col, a))
    L.pegar(fruto(n), 1, 1)
    return L


# ---------------------------------------------------------------- talismanes

METAL = {
    "oro": ORO[1:],
    "plata": ["#2a2c34", "#4a4e5a", "#7a808e", "#aab0bc", "#d6dae2", "#f6f8fc"],
    "hierro": ["#0e0d12", "#1d1b24", "#2e2b38", "#46424f", "#625d6c", "#8a8494"],
    "bronce": ["#2a1608", "#4e2a10", "#7a4618", "#a8682a", "#d0924a", "#f0c48a"],
    "hueso": HUESO,
}

PAL = {
    "k": TINTA, "r": ROJO[3], "R": ROJO[5], "W": "#fff0e4", "o": ORO[4], "y": ORO[6], "q": ORO[2],
    "b": "#2f5cc6", "B": "#8fb8ff", "g": "#27702a", "G": "#6cc45c", "s": "#d6dae2", "S": "#ffffff",
    "h": HUESO[3], "H": HUESO[5], "n": PIEDRA[1], "f": "#e86418", "F": "#ffd060", "c": "#7cc0de", "C": "#f0fcff",
    "e": "#f2d640", "E": "#fffbd0", "p": "#5e121c", "P": "#c83a3a", "t": "#7a4618", "T": "#d0924a",
}

# talisman: (forma de la placa, metal, emblema de 7 x 7 escrito como texto)
TALISMANES = {
    "vigor": ("redondo", "oro", [".......", "..rRr..", ".rRRRr.", ".rRWRr.", ".rRRRr.", "..rRr..", "......."]),
    "aliento": ("concha", "bronce", ["..gGg..", ".gGgGg.", "gGgGgGg", "gGgGgGg", ".gGgGg.", "..g.g..", "......."]),
    "mente": ("redondo", "plata", [".......", "..bBb..", ".bBBBb.", ".bBSBb.", ".bBBBb.", "..bBb..", "......."]),
    "porteador": ("cuadrado", "bronce", ["h.....h", "hh...hh", ".hHHHh.", ".hHkHh.", "..hHh..", "..hhh..", "......."]),
    "brasa": ("escudo", "oro", ["...f...", "..fFf..", ".fFFf..", ".fFFFf.", "fFFyFFf", ".fFFFf.", "..fff.."]),
    "escarcha": ("redondo", "plata", ["c..c..c", ".c.c.c.", "..cCc..", "cccCccc", "..cCc..", ".c.c.c.", "c..c..c"]),
    "trueno": ("escudo", "oro", ["....eE.", "...eE..", "..eEE..", ".eEEEe.", "...Ee..", "..Ee...", ".Ee...."]),
    "sombra": ("redondo", "hierro", [".......", "..rrr..", ".rPPPr.", "rPkkkPr", ".rPPPr.", "..rrr..", "......."]),
    "hueso": ("escudo", "hierro", [".......", "H.....H", "hHHHHHh", "..hhh..", "hHHHHHh", "H.....H", "......."]),
    "codicia": ("redondo", "oro", ["...y...", "..yoy..", ".y.o.y.", "y..o..y", "...o...", "..yoy..", "......."]),
    "garra": ("escudo", "hierro", ["s..s..s", "s..s..s", "S..S..S", ".S.S.S.", ".s.s.s.", "..sss..", "......."]),
    "furia": ("cuadrado", "hierro", ["R.....R", "RR...RR", ".RRRRR.", ".RkRkR.", ".RRRRR.", "..RkR..", "...R..."]),
    # el Rosario del peregrino (parte 7ag, de Oswin): cuentas de hueso en su vuelta y la crucecita
    "peregrino": ("redondo", "bronce", ["..hHh..", ".h...h.", ".h...h.", "..hHh..", "...H...", "..HHH..", "...H..."]),
    # la Bendicion de Hedda (parte 7ah): un corazon de lana roja tejida sobre plata
    "madre": ("redondo", "plata", [".......", ".rR.Rr.", "rRPRPRr", "rPRPRPr", ".rPRPr.", "..rPr..", "...r..."]),
    # la Bufanda de Tam (parte 7ah): la bufanda roja anudada, con sus dos cabos, sobre bronce
    "bufanda": ("cuadrado", "bronce", [".......", "rRrRrRr", "PrRrRrP", "..rPr..", ".rP.Pr.", ".Rr.rR.", ".r...r."]),
}


def placa(forma):
    if forma == "redondo":
        return lambda dx, dy: dx * dx + dy * dy <= 1.0
    if forma == "cuadrado":
        return lambda dx, dy: abs(dx) <= 0.86 and abs(dy) <= 0.86 and abs(dx) + abs(dy) <= 1.45
    if forma == "escudo":
        return lambda dx, dy: dy >= -0.92 and abs(dx) <= 0.86 and (dy <= 0.2 or abs(dx) <= 0.86 * (1 - (dy - 0.2) / 0.85))
    if forma == "concha":
        return lambda dx, dy: dx * dx + (dy * 1.15) ** 2 <= 1.0
    raise ValueError(forma)


def talisman(n):
    forma, metal, emblema = TALISMANES[n]
    cs = METAL[metal]
    L = Lienzo(16, 16)
    # la cadena: una anilla arriba y dos eslabones a los lados
    for x, y in ((7, 0), (8, 0), (6, 1), (9, 1), (6, 2), (9, 2), (7, 3), (8, 3)):
        L.px(x, y, ORO[3] if metal != "hierro" else "#5a5266")
    L.px(5, 1, ORO[2]).px(10, 1, ORO[2])
    bola(L, 8, 9.5, 6.3, 6.0, cs, brillo=False, forma=placa(forma))
    # el canto de la placa, un tono mas oscuro (que se vea el relieve)
    dentro = L.a[..., 3] > 0
    for y in range(16):
        for x in range(16):
            if dentro[y, x] and y >= 4:
                vecinos = sum(1 for ox, oy in ((1, 0), (-1, 0), (0, 1), (0, -1))
                              if not (0 <= x + ox < 16 and 0 <= y + oy < 16 and dentro[y + oy, x + ox]))
                if vecinos:
                    L.px(x, y, cs[1])
    em = Lienzo(7, 7).sprite(emblema, PAL)
    L.pegar(em, 5, 6)
    contorno(L)
    return L


# ---------------------------------------------------------------- escribir

def modelo(nombre):
    ruta = os.path.join(ASSETS, "models", "item", nombre + ".json")
    with open(ruta, "w", encoding="utf-8") as f:
        json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/" + nombre}}, f)


def montar():
    hechos = []
    for n in FRUTOS:
        fruto(n).guardar(os.path.join(ASSETS, "textures", "item", "fruto_" + n + ".png"))
        modelo("fruto_" + n)
        icono_efecto(n).guardar(os.path.join(ASSETS, "textures", "mob_effect", "fruto_" + n + ".png"))
        hechos.append("fruto_" + n)
    for n in TALISMANES:
        talisman(n).guardar(os.path.join(ASSETS, "textures", "item", "talisman_" + n + ".png"))
        modelo("talisman_" + n)
        hechos.append("talisman_" + n)
    print(len(hechos), "objetos:", " ".join(hechos))


if __name__ == "__main__":
    montar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        todos = [fruto(n) for n in FRUTOS] + [icono_efecto(n) for n in FRUTOS] + [talisman(n) for n in TALISMANES]
        hoja = Lienzo(20 * 8, 20 * 4, "#2a2430")
        for i, s in enumerate(todos):
            hoja.pegar(s, (i % 8) * 20 + 2, (i // 8) * 20 + 1)
        hoja.imagen().resize((hoja.w * 6, hoja.h * 6), 0).save(os.path.join(d, "objetos_rol.png"))
        print("vista en", d)
