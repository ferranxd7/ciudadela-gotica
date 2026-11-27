#!/usr/bin/env python3
"""
Oswin, el peregrino ciego de la Encrucijada (parte 7ag), y los peregrinos muertos que echan a andar cuando le devuelven su
campana: sus pieles (128 x 64: lo de un jugador de brazos anchos en la mitad izquierda y las piezas de mas en la derecha) y el
retrato de Oswin para los dialogos (64 x 64), en `textures/entity/oswin.png`, `textures/entity/peregrino.png` y
`textures/gui/retratos/oswin.png`.

- **Oswin**: viejo y flaco, la barba blanca y larga, una venda de lino sobre los ojos (es ciego) anudada detras; el sombrero de
  peregrino de fieltro pardo, de copa baja y ala ancha, con la concha delante; el habito de lana parda hasta las espinillas, la
  esclavina de cuero con conchas cosidas, el cordon por cinturon, el zurron a la cadera, las piernas vendadas y sandalias; el
  bordon de madera con la calabaza colgada.
- **Los peregrinos muertos**: lo mismo en azul palido de muerto, las cuencas vacias con una luz fria y el habito hecho jirones.

Las piezas de mas (en Java, `client/personajes/ViajeroRenderer.crear`, mismas medidas):
    copa      (64, 0)   9 x 3 x 9    en la cabeza
    concha    (100, 0)  3 x 3 x 1    en la cabeza (delante de la copa)
    ala       (64, 12)  16 x 1 x 16  en la cabeza
    esclavina (64, 30)  8 x 5 x 4    en el cuerpo (0,6 mas grande)
    zurron    (88, 30)  2 x 3 x 3    en el cuerpo (a la cadera)
    calabaza  (100, 30) 2 x 3 x 2    en el bordon
    baston    (120, 30) 1 x 30 x 1   suelto, delante de el

    python3 tools/personajes/oswin.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

OSWIN = dict(
    piel=["#4a3428", "#664836", "#80604a", "#9a7a62", "#b2927a", "#c8aa92"],
    barba=["#5a5652", "#7e7a74", "#a29e96", "#c6c2ba", "#e2ded6", "#f4f0e8"],
    venda=["#7a7264", "#a09888", "#c4bca8", "#e0d8c4"],
    sombrero=["#22180f", "#2e2215", "#3c2d1c", "#4b3923", "#5c472c", "#6e5636"],
    concha=["#8a7a5c", "#b8a47e", "#e2d2aa", "#f6ecd0"],
    habito=["#1a1612", "#241e18", "#2e271f", "#3a3127", "#463c30", "#544838"],
    cuero=["#2a1a10", "#3a2616", "#4e341e", "#644428", "#7a5632"],
    cuerda=["#5a4a30", "#7e6a46", "#a28c60"],
    calabaza=["#6a4a18", "#94702a", "#bc9440", "#dcb860"],
    madera=["#2a1c10", "#3e2a18", "#543a22", "#6a4a2c", "#7e5a36"],
    venda_pierna=["#4a4236", "#625848", "#7a705c"],
    ojos=None, jirones=False, semilla=61,
)
PEREGRINO = dict(
    piel=["#3a4658", "#4e5e74", "#66788e", "#8496ac", "#a4b4c8", "#c4d2e2"],
    barba=["#4a5668", "#66748a", "#8494aa", "#a4b4c8", "#c4d2e2", "#e2ecf6"],
    venda=["#2a3446", "#3a465a", "#4c5a70", "#5e6c84"],
    sombrero=["#121a26", "#1a2432", "#222e3e", "#2c384a", "#364458", "#425268"],
    concha=["#5a6a80", "#7e90a8", "#a8bad0", "#d0e0f0"],
    habito=["#141c28", "#1c2634", "#243042", "#2e3a4e", "#38465c", "#44546c"],
    cuero=["#18202c", "#202a38", "#2a3646", "#344256", "#3e4e64"],
    cuerda=["#3a465a", "#4e5c72", "#66748c"],
    calabaza=["#3a465a", "#4e5c72", "#66748c", "#808ea6"],
    madera=["#1a2230", "#222c3c", "#2c3848", "#364456", "#405064"],
    venda_pierna=["#222c3a", "#2c3848", "#384658"],
    ojos=("#0a0e18", "#9fd6ff"), jirones=True, semilla=67,
)


def piel(cfg):
    P = Piel(ancho=128, alto=64, brazos=4)
    P.caja("copa", 64, 0, 9, 3, 9)
    P.caja("concha", 100, 0, 3, 3, 1)
    P.caja("ala", 64, 12, 16, 1, 16)
    P.caja("esclavina", 64, 30, 8, 5, 4)
    P.caja("zurron", 88, 30, 2, 3, 3)
    P.caja("calabaza", 100, 30, 2, 3, 2)
    P.caja("baston", 120, 30, 1, 30, 1)
    pi, ba, ve, so, co = cfg["piel"], cfg["barba"], cfg["venda"], cfg["sombrero"], cfg["concha"]
    ha, cu, cd, ma = cfg["habito"], cfg["cuero"], cfg["cuerda"], cfg["madera"]
    s = cfg["semilla"]
    r = np.random.default_rng(s)
    habito = tela(ha, s + 1, pliegue=3, grano=0.14)
    cara_lado = ruido(pi[2:5], s + 2, grano=0.25, base=1.0)

    # ---- la cabeza: la venda sobre los ojos, la nariz, el bigote y la barba blancos (los muertos: cuencas con luz fria)
    C = {"h": ba[3], "H": ba[4], "s": pi[3], "S": pi[4], "o": pi[2], "V": ve[3], "v": ve[1], "w": ve[2], "n": pi[4],
         "N": pi[2], "B": ba[4], "b": ba[2], "k": pi[0], "c": ba[5]}
    if cfg["ojos"]:
        C["V"], C["v"], C["w"] = pi[2], cfg["ojos"][0], cfg["ojos"][1]
        filas_ojos = ["oVvwVvwo", "oVvvVvvo"]
    else:
        filas_ojos = ["VVwVVwVV", "vVVvvVVv"]
    P.sprite("cabeza", "frente", [
        "hHhhhhHh",
        "osSSSSso",
        filas_ojos[0],
        filas_ojos[1],
        "osSnnSso",
        "bBBNNBBb",
        "bBBkkBBb",
        "bbBcBBbb",
    ], C)

    def lado(x, y, w, h):
        if y == 0:
            return ba[3] if x % 2 else ba[2]
        if y in (2, 3):
            # la venda que da la vuelta a la cabeza (los muertos: una cinta oscura)
            return ve[2] if (x + y) % 3 else ve[1]
        if y >= 5 and x >= 3:
            return ba[3] if (x + y) % 2 else ba[2]
        if (x, y) in ((4, 4),):
            return pi[2]
        return cara_lado(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)

    def nuca(x, y, w, h):
        if y in (2, 3):
            return ve[2] if (x + y) % 3 else ve[1]
        return ba[3] if (x + y) % 3 else ba[2]

    P.pintar("cabeza", "espalda", nuca)
    P.pintar("cabeza", "arriba", lambda x, y, w, h: ba[3] if (x + y) % 2 else ba[4])
    P.pintar("cabeza", "abajo", lambda x, y, w, h: ba[2])

    # la capa de encima de la cabeza: la barba larga que cae (delante), los dos cabos del nudo de la venda (detras)
    def barba_f(x, y, w, h):
        if y >= 5:
            largo = 7 if 2 <= x <= 5 else 6
            if y <= largo:
                return ba[4] if (x + y) % 2 else ba[3]
        return None

    P.pintar("cabeza_f", "frente", barba_f)

    def nudo(x, y, w, h):
        if x in (3, 4) and 2 <= y <= 6 - (x - 3):
            return ve[3] if y == 2 else ve[2]
        return None

    P.pintar("cabeza_f", "espalda", nudo)
    for cara in ("der", "izq", "arriba", "abajo"):
        P.pintar("cabeza_f", cara, lambda x, y, w, h: None)

    # ---- el sombrero: copa baja de fieltro con su cinta de cuero, ala ancha gastada en el borde, la concha delante
    def copa(cara):
        def f(x, y, w, h):
            if cara == "arriba":
                return so[3] if (x - 4) ** 2 + (y - 4) ** 2 < 6 else so[2]
            if cara == "abajo":
                return so[0]
            if y == 2:
                return cu[2] if x % 3 else cu[1]
            return so[3] if y == 0 else so[2] if (x + y) % 4 else so[1]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("copa", cara, copa(cara))

    def ala(cara):
        def f(x, y, w, h):
            if cara in ("arriba", "abajo"):
                borde = min(x, y, w - 1 - x, h - 1 - y)
                if cara == "abajo":
                    return so[0] if borde > 0 else so[1]
                if borde == 0:
                    return so[2] if (x + y) % 5 else so[1]
                if cfg["jirones"] and borde == 0 and (x * 7 + y) % 5 == 0:
                    return None
                return so[4] if borde == 1 else so[3] if (x * 3 + y) % 7 else so[4]
            return so[1] if (x % 3) else so[2]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("ala", cara, ala(cara))
    P.sprite("concha", "frente", ["cCc", "CcC", ".c."], {"c": co[2], "C": co[3]})
    for cara in ("arriba", "abajo", "der", "izq", "espalda"):
        P.pintar("concha", cara, lambda x, y, w, h: co[1])

    # ---- el cuerpo: el habito con el cordon por cinturon (los muertos: con jirones)
    def cuerpo(cara):
        def f(x, y, w, h):
            if y in (6, 7):
                if cara == "frente" and x in (2, 3) and y == 7:
                    return cd[2]
                return cd[1] if (x + y) % 2 else cd[0]
            if cara == "frente" and y > 7 and x in (2, 3) and y < 11:
                return cd[1] if y % 2 else cd[0]  # los cabos del cordon que cuelgan
            return habito(x, y, w, h)
        return f

    for cara in ("frente", "espalda", "der", "izq"):
        P.pintar("cuerpo", cara, cuerpo(cara))
    P.pintar("cuerpo", "arriba", lambda x, y, w, h: ha[2])
    P.pintar("cuerpo", "abajo", lambda x, y, w, h: ha[1])
    for cara in ("frente", "espalda", "der", "izq", "arriba", "abajo"):
        P.pintar("cuerpo_f", cara, lambda x, y, w, h: None)

    # la esclavina de cuero, con dos conchas cosidas delante
    esclavina = ruido(cu[1:5], s + 3, grano=0.3, base=1.6)

    def escl(cara):
        def f(x, y, w, h):
            if cara == "frente" and (x, y) in ((1, 2), (2, 2), (1, 3), (6, 2), (5, 2), (6, 3)):
                return co[2] if y == 2 else co[1]
            if cara in ("frente", "espalda", "der", "izq") and y == h - 1:
                if cfg["jirones"] and (x * 5) % 3 == 0:
                    return None
                return cu[0]
            return esclavina(x, y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("esclavina", cara, escl(cara))
    # el zurron de cuero con su solapa
    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("zurron", cara, lambda x, y, w, h: cu[3] if y == 0 else cu[2] if (x + y) % 3 else cu[1])

    # ---- los brazos: mangas anchas del habito y las manos huesudas
    def brazo(cara, derecho):
        def f(x, y, w, h):
            if cara == "abajo":
                return pi[2]
            if cara == "arriba":
                return ha[3]
            if y >= 10:
                return pi[3] if (x + y) % 3 else pi[2]
            if y == 9:
                return ha[1]  # el puno de la manga
            return habito(x + (0 if derecho else 2), y, w, h)
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("brazo_d", cara, brazo(cara, True))
        P.pintar("brazo_i", cara, brazo(cara, False))
        P.pintar("brazo_d_f", cara, lambda x, y, w, h: None)
        P.pintar("brazo_i_f", cara, lambda x, y, w, h: None)

    # ---- las piernas: el habito hasta las espinillas, las piernas vendadas y las sandalias
    vp = cfg["venda_pierna"]

    def pierna(cara):
        def f(x, y, w, h):
            if cara == "abajo":
                return cu[1]
            if cara == "arriba":
                return ha[2]
            if y <= 7:
                if cfg["jirones"] and y == 7 and (x + (0 if cara == "frente" else 1)) % 2 == 0:
                    return None
                return habito(x, y, w, h)
            if y == 11:
                return cu[2] if x % 2 else cu[1]
            return vp[(x + y) % 3]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("pierna_d", cara, pierna(cara))
        P.pintar("pierna_i", cara, pierna(cara))
        P.pintar("pierna_d_f", cara, lambda x, y, w, h: None)
        P.pintar("pierna_i_f", cara, lambda x, y, w, h: None)

    # ---- el bordon: madera con nudos, el agarradero de cuero donde van las manos y la calabaza
    def baston(cara):
        def f(x, y, w, h):
            if cara in ("arriba", "abajo"):
                return ma[1]
            if 15 <= y <= 18:
                return cu[2] if y % 2 else cu[1]
            if y in (4, 23):
                return ma[1]  # dos nudos
            return ma[3] if (y + (1 if cara in ("der", "espalda") else 0)) % 5 else ma[2]
        return f

    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("baston", cara, baston(cara))
    ca = cfg["calabaza"]
    for cara in ("arriba", "abajo", "der", "frente", "izq", "espalda"):
        P.pintar("calabaza", cara, lambda x, y, w, h: cu[1] if y == 0 else ca[3] if (x == 0 and y == 1) else ca[2] if y < 2 else ca[1])
    # (los muertos no llevan bordon: el modelo no lo dibuja)
    return P


# ---------------------------------------------------------------- el retrato

def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato():
    cfg = OSWIN
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(71)
    grano = r.random((H, W))
    pi, ba, ve, so, co = cfg["piel"], cfg["barba"], cfg["venda"], cfg["sombrero"], cfg["concha"]
    ha, cu, ma, ca = cfg["habito"], cfg["cuero"], cfg["madera"], cfg["calabaza"]
    llama = ["#2a4a6a", "#4a86b0", "#8cc6e6", "#d6f2ff"]
    # ---- el fondo: el ocaso de Morvain arriba y el fuego de almas del santuario abajo a la izquierda
    for y in range(H):
        for x in range(W):
            t = y / H
            c = mezcla("#0e0814", "#3a1a1c", t * 0.8)
            c = mezcla(c, "#6a3a1c", max(0.0, 0.5 - abs(y - 46) / 40) * 0.6)
            d = math.hypot((x - 0) / 30, (y - 64) / 26)
            c = mezcla(c, llama[1], max(0.0, 1 - d) * 0.5)
            if grano[y, x] < 0.06:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    for (x, y) in ((6, 8), (14, 3), (52, 6), (60, 18), (3, 26), (44, 4), (58, 34)):
        L.px(x, y, "#8a7a72")
    cx, cy = 31, 29
    # ---- el bordon a la derecha, con la calabaza colgada
    for y in range(10, H):
        for x in (53, 54):
            L.px(x, y, _tono(ma, 3 - (x - 53) * 1.2 + (1 if y % 7 == 0 else 0)))
    for y in range(18, 26):
        for x in range(54, 60):
            if _e(57, 22.5, 2.6, 3.8, x, y) <= 1:
                L.px(x, y, _tono(ca, 3 - (x - 55) * 0.6 - (y - 20) * 0.15))
    L.px(55, 17, cu[2]).px(55, 18, cu[1])
    # ---- los hombros: el habito y la esclavina de cuero con su concha
    for y in range(40, H):
        ancho = 22 + (y - 40) * 0.6
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            esclavina = y < 52 - abs(dx) * 0.12
            if esclavina:
                c = _tono(cu, 2.8 - abs(dx) / ancho * 1.4 + (grano[y, x] - 0.5) * 1.2)
            else:
                c = _tono(ha, 3 - abs(dx) / ancho * 1.5 - (0.8 if (x + y) % 5 == 0 else 0))
            if dx < -ancho + 3:
                c = mezcla(c, llama[2], 0.15)
            L.px(x, y, c)
    for (x, y, k) in ((16, 45, 3), (15, 46, 2), (16, 46, 3), (17, 46, 2), (16, 47, 1), (15, 47, 2), (17, 47, 2)):
        L.px(x, y, co[k])
    # la mano sobre el bordon
    for y in range(44, 50):
        for x in range(50, 57):
            if _e(53.5, 46.8, 3.6, 3.0, x, y) <= 1:
                L.px(x, y, _tono(pi, 3.2 - (y - 44) * 0.2 + (0.6 if x == 51 else 0)))
    # ---- la cabeza
    rx, ry = 10.2, 13.0
    for y in range(8, 44):
        for x in range(W):
            e = _e(cx, cy, rx, ry, x, y)
            if e > 1:
                continue
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            t = 3.0 - dy / 14.0 * 0.4 + dx / 12.0 * 0.4 - max(0.0, e - 0.7) * 2.0
            c = _tono(pi, t)
            if dx < -rx + 3:
                c = mezcla(c, llama[2], 0.3)
            if grano[y, x] < 0.08:
                c = mezcla(c, pi[1], 0.5)  # arrugas y manchas de la edad
            L.px(x, y, c)
    # las orejas
    for lado in (-1, 1):
        ox = cx + lado * (rx - 0.3)
        for y in range(cy - 3, cy + 5):
            for x in range(int(ox - 2), int(ox + 2)):
                if _e(ox, cy + 1, 2.0, 3.6, x, y) <= 1:
                    L.px(x, y, pi[3] if lado > 0 else mezcla(pi[3], llama[2], 0.35))
    # ---- la venda de lino sobre los ojos, anudada (los cabos cuelgan junto a la oreja de la derecha)
    for y in range(25, 29):
        for x in range(W):
            if _e(cx, cy, rx + 0.6, ry + 0.4, x, y) > 1:
                continue
            k = 2 - (1 if (y == 28 or (x * 2 + y) % 7 == 0) else 0) + (1 if y == 25 and x < cx + 4 else 0) - (1 if x > cx + 7 else 0)
            L.px(x, y, ve[max(0, min(3, k))])
    for (x, y) in ((41, 28), (42, 29), (41, 30), (42, 31), (43, 30)):
        L.px(x, y, ve[1] if (x + y) % 2 else ve[2])
    # una mancha vieja en la venda, sobre el ojo de la derecha
    for (x, y) in ((35, 26), (36, 26), (36, 27)):
        L.px(x, y, mezcla(ve[2], "#6a3a2a", 0.5))
    # los pomulos hundidos y la nariz larga
    for (x, y) in ((24, 30), (25, 31), (38, 30), (37, 31)):
        L.px(x, y, pi[1])
    for y in range(29, 34):
        L.px(cx, y, pi[4]).px(cx + 1, y, pi[2])
    L.px(cx - 1, 33, pi[1]).px(cx + 2, 33, pi[1])
    # ---- el bigote y la barba, blancos y largos, con sus mechones
    for y in range(34, H):
        ancho = 3.0 + (y - 34) * 0.6 if y < 45 else 9.6 - (y - 45) * 0.1
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho or (y > 54 and abs(dx) > ancho - (y - 54) * 0.6):
                continue
            k = 3.0 + (0.9 if (x + int(y * 0.45)) % 3 == 0 else 0) - (0.9 if (x * 2 + y) % 7 == 0 else 0) - abs(dx) / ancho * 1.3
            c = _tono(ba, k)
            if dx < -ancho + 2:
                c = mezcla(c, llama[2], 0.22)
            L.px(x, y, c)
    for x in range(cx - 3, cx + 5):
        L.px(x, 37, ba[1] if abs(x - cx - 0.5) < 2 else ba[2])  # la boca bajo el bigote
    # ---- el sombrero: el ala ancha que le da sombra a la frente, la copa baja con su cinta y la concha
    for y in range(4, 23):
        for x in range(W):
            ala = _e(cx, 17.5, 25.5, 5.0, x, y) <= 1
            copa = _e(cx, 12, 11.5, 8.5, x, y) <= 1 and y <= 17
            if not (ala or copa):
                continue
            if copa and y < 16:
                k = 3.4 - (y - 4) * 0.08 + (x - cx) * 0.05
                c = cu[2] if y == 14 else _tono(so, k)
            else:
                k = 2.8 - abs(x + 0.5 - cx) / 26 * 1.4 + (0.8 if y < 17 else -0.7)
                c = _tono(so, k)
                if _e(cx, 17.5, 25.5, 5.0, x, y) > 0.8:
                    c = _tono(so, k - 0.8)  # el borde del ala, mas oscuro y gastado
            L.px(x, y, c)
    # la sombra del ala sobre la frente
    for y in range(22, 25):
        for x in range(W):
            if _e(cx, cy, rx, ry, x, y) <= 1:
                L.px(x, y, mezcla(L.leer(x, y), "#120a08", 0.5 - (y - 22) * 0.12))
    for (x, y, k) in ((31, 9, 3), (30, 10, 2), (31, 10, 3), (32, 10, 2), (29, 11, 2), (30, 11, 3), (31, 11, 2), (32, 11, 3),
                      (33, 11, 2), (30, 12, 1), (31, 12, 2), (32, 12, 1), (31, 13, 1)):
        L.px(x, y, co[k])
    return L


def guardar():
    piel(OSWIN).guardar(os.path.join(ASSETS, "textures", "entity", "oswin.png"))
    piel(PEREGRINO).guardar(os.path.join(ASSETS, "textures", "entity", "peregrino.png"))
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "oswin.png"))
    print("oswin: pieles (oswin, peregrino) y retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        for nombre, cfg in (("oswin", OSWIN), ("peregrino", PEREGRINO)):
            p = piel(cfg)
            p.vista().save(os.path.join(d, nombre + "_piel.png"))
            p.L.imagen().resize((128 * 4, 64 * 4), 0).save(os.path.join(d, nombre + "_textura.png"))
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "oswin_retrato.png"))
        print("vistas en", d)
