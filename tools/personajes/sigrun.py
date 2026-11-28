#!/usr/bin/env python3
"""
Sigrun, la Guardiana del Paso (parte 7ap): su retrato para los dialogos (64 x 64), en `textures/gui/retratos/sigrun.png` (el
modelo y su textura salen de tools/enemigos/sigrun.py: es un enemigo con el que tambien se habla).

- El fondo: el Paso del Colmillo al anochecer, el cielo de pizarra azul, los picos nevados detras y la nieve que cae.
- Ella: la cara palida y curtida por el frio (las mejillas rojas, una cicatriz en la izquierda), los ojos azul claro que casi
  brillan; el yelmo abierto de acero con escarcha (el casquete con su punta, el nasal y las carrilleras); la trenza blanca que le
  cae por el hombro; el manto de piel de lobo con nieve encima y, debajo, el tabardo azul de noche con el colmillo blanco.

    python3 tools/personajes/sigrun.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, mezcla  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

PIEL = ["#5e463e", "#86665a", "#a8887a", "#c4a698", "#d8bfb2", "#e8d4c8"]
MEJILLA = "#c98a80"
OJO = ["#1c3a52", "#4c8ab4", "#8fd8ff", "#d8f4ff"]
PELO = ["#8e9298", "#b4b8be", "#d4d8dc", "#eceef0", "#ffffff"]
ACERO = ["#1e2328", "#343c44", "#4f5a64", "#6e7a86", "#96a2ae", "#c8d2da"]
ESCARCHA = "#e4f2fa"
LOBO = ["#25272a", "#36393d", "#4a4e54", "#62676e", "#7e848c", "#9aa0a8"]
TABARDO = ["#070c18", "#0e1730", "#16244a", "#20345e"]
CUERO = ["#2a1a10", "#4a2f1c", "#6e4a2e"]
CIELO = ["#0b0f18", "#141b28", "#1e2738", "#2a3446", "#3a4558"]


def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato():
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(717)
    grano = r.random((H, W))
    # ---- el fondo: el cielo de pizarra que se aclara hacia el horizonte y los picos nevados del paso
    for y in range(H):
        for x in range(W):
            t = y / H
            c = _tono(CIELO, 0.3 + t * 4.2)
            if grano[y, x] < 0.04:
                c = mezcla(c, "#000000", 0.25)
            L.px(x, y, c)
    # dos filas de picos: la de lejos, azulada, con nieve en las cumbres; la de cerca, oscura, con un filo de nieve
    def cresta(x, fase, alto, base):
        v = 0.0
        for k, (f, a) in enumerate(((0.09, 1.0), (0.23, 0.45), (0.51, 0.18))):
            v += a * abs(math.sin(x * f + fase * (k + 1)))
        return base - alto * v / 1.63
    for x in range(W):
        lejos = cresta(x, 1.3, 14, 36)
        cerca = cresta(x, 4.1, 9, 47)
        for y in range(int(lejos), H):
            d = y - lejos
            c = mezcla("#2c3646", "#3e4a5c", min(1.0, d / 10))
            if d < 3 + 3 * (lejos < 26):
                c = mezcla("#dbe6ef", "#aebccb", d / 6)
            L.px(x, y, c)
        for y in range(int(cerca), H):
            d = y - cerca
            L.px(x, y, "#e6eef4" if d < 1.5 else "#141a22" if d > 3 else "#5a6878")
    cx, cy = 31, 27
    # ---- los hombros: el manto de piel de lobo con nieve encima y el tabardo azul abajo, con el colmillo
    for y in range(41, H):
        ancho = 22 + (y - 41) * 0.75
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            pecho = abs(dx) < 9 + (y - 41) * 0.15 and y > 46
            if pecho:
                c = _tono(TABARDO, 2.4 - abs(dx) / 9 + (grano[y, x] - 0.5) * 0.8)
            else:
                # mechones de piel: rayas cortas que caen, mas claras arriba
                fleco = (x * 7 + (y // 2) * 3) % 5
                c = _tono(LOBO, 3.6 - (y - 41) * 0.12 - abs(dx) / ancho * 1.4 + (fleco - 2) * 0.35)
                if y < 44 + (abs(dx) // 6) % 2 and grano[y, x] < 0.7:
                    c = mezcla("#f4f8fb", "#ccd8e2", grano[y, x])  # nieve sobre los hombros
            L.px(x, y, c)
    # el borde de la coraza bajo el manto y el colmillo blanco en el pecho
    for x in range(cx - 9, cx + 10):
        L.px(x, 46, ACERO[3] if x % 3 else ACERO[4])
    fang = ["WWWWW", ".WWWW", ".WWWW", "..WWW", "..WW.", "...W."]
    for fy, fila in enumerate(fang):
        for fx, ch in enumerate(fila):
            if ch == "W":
                L.px(cx - 3 + fx, 52 + fy, "#ffffff" if fx < 2 else "#e3e8ee")
    # ---- la cara: palida y curtida, alumbrada de lado por el ultimo sol (desde la izquierda)
    rx, ry = 10.4, 12.6
    for y in range(11, 42):
        for x in range(W):
            e = _e(cx, cy, rx, ry, x, y)
            if e > 1:
                continue
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            t = 4.0 - dy / 14.0 * 0.4 + dx / 12.0 * 0.7 * -1 - max(0.0, e - 0.72) * 2.4
            c = _tono(PIEL, t)
            if dx > 4:
                c = mezcla(c, PIEL[2], 0.25)
            L.px(x, y, c)
    # las mejillas rojas del frio
    for (mx, my) in ((24, 31), (38, 31)):
        for y in range(my - 1, my + 2):
            for x in range(mx - 2, mx + 3):
                if _e(mx, my, 2.6, 1.6, x, y) <= 1:
                    L.px(x, y, mezcla(L.leer(x, y), MEJILLA, 0.45))
    # la cicatriz de la mejilla izquierda (la de ella: a la derecha del retrato)
    for (x, y) in ((37, 26), (37, 27), (38, 28), (38, 29), (39, 30)):
        L.px(x, y, mezcla(L.leer(x, y), "#e8d6cc", 0.8))
    # las cejas, la nariz (el nasal la tapa en parte), la boca apretada
    for x in range(22, 28):
        L.px(x, 23 + (1 if x < 24 else 0), "#8a7a70")
    for x in range(35, 41):
        L.px(x, 23 + (1 if x > 38 else 0), "#8a7a70")
    for (x, y) in ((32, 33), (33, 33), (30, 33)):
        L.px(x, y, PIEL[2])
    for x in range(27, 36):
        L.px(x, 37, "#8a5a54" if 28 <= x <= 34 else PIEL[2])
    L.px(31, 38, PIEL[2])
    # los ojos: azul claro, casi brillan
    for ex in (25, 37):
        for x in range(ex - 2, ex + 3):
            L.px(x, 26, PIEL[1])
        L.px(ex - 1, 27, "#f4f0ec")
        L.px(ex, 27, OJO[2])
        L.px(ex + 1, 27, OJO[1])
        L.px(ex - 1, 28, OJO[0])
        L.px(ex, 28, OJO[3])
        L.px(ex + 1, 28, "#f4f0ec")
        L.px(ex - 2, 27, PIEL[2])
        L.px(ex + 2, 28, PIEL[2])
    # ---- el yelmo abierto: el casquete hasta las cejas, la punta, el nasal y las carrilleras, con escarcha
    for y in range(2, 24):
        for x in range(W):
            casquete = _e(cx, cy - 4, rx + 2.4, ry + 1.2, x, y) <= 1 and y <= 22
            if not casquete:
                continue
            dx = x + 0.5 - cx
            c = _tono(ACERO, 3.6 - dx / 6.0 - (y - 4) * 0.05 + (grano[y, x] - 0.5) * 0.7)
            if grano[y, x] > 0.93:
                c = ESCARCHA
            L.px(x, y, c)
    # el borde del casquete (la banda con remaches) sobre las cejas
    for x in range(cx - 12, cx + 13):
        if _e(cx, cy - 4, rx + 2.4, ry + 1.2, x, 21) <= 1.04:
            L.px(x, 21, ACERO[1])
            L.px(x, 20, ACERO[4] if x % 4 else ACERO[5])
    # la punta, encima del casquete
    top = min(y for y in range(H) if _e(cx, cy - 4, rx + 2.4, ry + 1.2, cx, y) <= 1)
    for k in range(5):
        for x in range(cx - 2 + k // 2, cx + 3 - k // 2):
            L.px(x, top - k, ACERO[4] if x < cx + 1 else ACERO[2])
    # el nasal: una tira de acero de la banda a la punta de la nariz
    for y in range(21, 33):
        L.px(cx, y, ACERO[4])
        L.px(cx + 1, y, ACERO[2])
    L.px(cx, 33, ACERO[3])
    # las carrilleras: placas estrechas a los lados de la cara, de la sien a la mandibula
    for (lado, x0) in ((-1, cx - 12), (1, cx + 9)):
        for y in range(21, 36):
            for x in range(x0, x0 + 4):
                if _e(cx, cy + 1, rx + 2.2, ry + 0.4, x, y) > 1:
                    continue
                c = _tono(ACERO, (2.2 if lado > 0 else 3.8) + (grano[y, x] - 0.5) * 0.8 - (y - 21) * 0.05)
                if (x == x0 + 3 and lado < 0) or (x == x0 and lado > 0):
                    c = ACERO[1]
                L.px(x, y, c)
        L.px(x0 + (1 if lado < 0 else 2), 25, ACERO[5])
        L.px(x0 + (1 if lado < 0 else 2), 31, ACERO[5])
    # ---- la trenza blanca por el hombro derecho del retrato (el izquierdo de ella), con su lazo de cuero
    for k in range(26):
        y = 36 + k
        bx = cx + 12 + int(2.2 * math.sin(k * 0.35)) + k // 6
        for x in range(bx - 2, bx + 3):
            hilo = ((y + (x - bx) * 2) % 4)
            L.px(x, y, PELO[1 + hilo] if abs(x - bx) < 2 else PELO[0])
    for x in range(cx + 13, cx + 19):
        L.px(x, 56, CUERO[1])
        L.px(x, 57, CUERO[0])
    # mechones blancos que asoman del yelmo, por la sien
    for (x, y) in ((cx - 11, 23), (cx - 11, 24), (cx - 10, 25), (cx + 11, 23), (cx + 11, 24), (cx + 12, 25), (cx + 12, 26)):
        L.px(x, y, PELO[3])
    # ---- la nieve que cae, por encima de todo (menos de la cara)
    for _ in range(34):
        x, y = int(r.integers(0, W)), int(r.integers(0, H))
        if _e(cx, cy, rx + 1, ry + 1, x, y) <= 1:
            continue
        L.px(x, y, "#ffffff" if r.random() < 0.6 else "#c8d4de")
    return L


def guardar():
    retrato().guardar(os.path.join(ASSETS, "textures", "gui", "retratos", "sigrun.png"))
    print("sigrun: retrato")


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        retrato().imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, "sigrun_retrato.png"))
        print("vistas en", d)
