#!/usr/bin/env python3
"""
Las cinco piezas de La Garza Gris, el barco de Brumavela (parte 3b): la VELA, el TIMON, el MASTIL, el ANCLA y el
CORAZON que lo hace volar. No son objetos del inventario: se dibujan en el mundo (entidades item_display, de cara a quien
mira) y en los carteles. Texturas de 32 x 32 en pixel art y sus modelos de objeto.

    python3 tools/pueblo/piezas.py [--vista carpeta]
"""
import json
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import ASSETS, TINTA, Lienzo, rgba  # noqa: E402

N = 32
MADERA = ["#2a1a0e", "#3e2815", "#573a1f", "#74502c", "#93693c", "#b2854f", "#cfa56a"]
LONA = ["#4a4a55", "#62626e", "#7c7c88", "#9a9aa4", "#b8b8c0", "#d4d4da", "#ecebee"]
HIERRO = ["#14161c", "#22262f", "#343a46", "#4b5362", "#667082", "#8791a4", "#b0b9c8"]
LATON = ["#3a2a0c", "#6a4c16", "#96702a", "#c0963c", "#e2bf62", "#f6e3a0"]
CUERDA = ["#4a3a22", "#75603a", "#a08a58", "#c8b480"]
ALMA = ["#081428", "#0e2a55", "#165090", "#2484c8", "#56b8ea", "#a4e2fa", "#f0fcff"]


def tono(paleta, t):
    return paleta[int(np.clip(round(t), 0, len(paleta) - 1))]


def linea(L, x0, y0, x1, y1, c, grueso=1):
    """Una linea de pixeles (Bresenham), con el grosor hacia abajo y a la derecha."""
    dx, dy = abs(x1 - x0), abs(y1 - y0)
    sx, sy = (1 if x0 < x1 else -1), (1 if y0 < y1 else -1)
    e = dx - dy
    while True:
        for i in range(grueso):
            for j in range(grueso):
                L.px(x0 + i, y0 + j, c)
        if x0 == x1 and y0 == y1:
            break
        e2 = 2 * e
        if e2 > -dy:
            e -= dy
            x0 += sx
        if e2 < dx:
            e += dx
            y0 += sy


def vela():
    """La vela mayor: la lona gris hinchada colgando de su verga, con sus rizos y un remiendo."""
    L = Lienzo(N, N)
    r = np.random.default_rng(7)
    for y in range(7, 29):
        v = (y - 7) / 21.0
        # los costados se curvan hacia dentro y el pujamen (abajo) hace panza
        entra = 2.2 * math.sin(v * math.pi)
        x0, x1 = 5 + entra, 26 - entra
        for x in range(N):
            if x + 0.5 < x0 or x + 0.5 > x1:
                continue
            u = (x + 0.5 - x0) / (x1 - x0)
            abajo = 28 - 2.6 * math.sin(u * math.pi) if v > 0.8 else 99
            if y + 0.5 > abajo + 1.2:
                continue
            # los panos: franjas verticales, luz por la izquierda y la panza mas clara en el centro
            t = 4.3 - 2.6 * abs(u - 0.42) * 2 + 1.1 * math.sin(v * math.pi) - (0.9 if int(u * 5.0) != int((u + 0.03) * 5.0) else 0)
            t += (r.random() - 0.5) * 0.7
            L.px(x, y, tono(LONA, t))
    # los rizos: dos hileras de cabitos
    for yy in (13, 19):
        for x in range(8, 25, 3):
            if L.leer(x, yy)[3]:
                L.px(x, yy, LONA[1])
                L.px(x, yy + 1, LONA[0])
    # el remiendo
    for y in range(21, 25):
        for x in range(16, 21):
            if L.leer(x, y)[3]:
                L.px(x, y, tono(LONA, 2 + (x + y) % 2))
    L.marco(16, 21, 5, 4, LONA[1])
    # la verga y sus cabos
    for x in range(2, 30):
        L.px(x, 5, MADERA[5 if 4 < x < 27 else 3])
        L.px(x, 6, MADERA[3])
        L.px(x, 7, MADERA[1])
    for x in (2, 3, 28, 29):
        L.px(x, 5, MADERA[4])
    for x in (7, 15, 23):
        L.px(x, 4, CUERDA[2])
        L.px(x, 5, CUERDA[3])
        L.px(x, 6, CUERDA[1])
        L.px(x, 7, CUERDA[0])
    linea(L, 5, 8, 2, 14, CUERDA[2])
    linea(L, 26, 8, 29, 14, CUERDA[1])
    L.contorno(TINTA)
    return L


def timon():
    """La rueda del timon: el aro de madera con ocho cabillas y el cubo de laton."""
    L = Lienzo(N, N)
    cx, cy = 15.5, 15.5
    for y in range(N):
        for x in range(N):
            dx, dy = x - cx, y - cy
            d = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            luz = -dx * 0.12 - dy * 0.16
            # el aro
            if 8.6 <= d <= 11.4:
                t = 3.2 + luz + (1.0 if d < 9.6 else -0.6 if d > 10.6 else 0.3)
                L.px(x, y, tono(MADERA, t))
                continue
            # las ocho cabillas: radios que salen del cubo y asoman por fuera del aro
            k = round(a / (math.pi / 4))
            ar = k * math.pi / 4
            lejos = abs(-math.sin(ar) * dx + math.cos(ar) * dy)
            if lejos <= 0.95 and 3.0 <= d <= 14.6:
                if d > 11.4:
                    t = 4.4 + luz if d < 13.6 else 5.5
                else:
                    t = 2.4 + luz + (0.9 if lejos < 0.45 else 0)
                L.px(x, y, tono(MADERA, t))
                continue
            # el cubo
            if d <= 3.4:
                L.px(x, y, tono(LATON, 3.0 + luz * 3.2 - (1.6 if d > 2.5 else 0)))
    L.px(15, 15, LATON[5])
    L.px(16, 16, LATON[1])
    L.contorno(TINTA)
    return L


def mastil():
    """El palo mayor nuevo: un fuste de abeto en diagonal con sus zunchos de hierro, la cofa y un cabo enrollado."""
    L = Lienzo(N, N)
    # de abajo a la izquierda a arriba a la derecha, mas grueso por el pie
    for i in range(0, 27):
        u = i / 26.0
        x, y = 3 + i, 28 - i
        g = 4 if u < 0.3 else 3 if u < 0.75 else 2
        for k in range(g):
            # a lo ancho (perpendicular): arriba a la izquierda la luz
            t = 5.2 - k * 1.5 + (0.6 if (i % 5) == 2 else 0)
            L.px(x + k, y + k, tono(MADERA, t))
            L.px(x + k + 1, y + k, tono(MADERA, t - 0.8))
    # los zunchos
    for i in (4, 12, 20):
        x, y = 3 + i, 28 - i
        for k in range(5):
            L.px(x + k, y + k, HIERRO[5 if k == 0 else 3 if k < 3 else 2])
            L.px(x + k + 1, y + k, HIERRO[2])
    # la cofa, arriba
    for k in range(-2, 5):
        L.px(24 + k, 3 + k, MADERA[2 if k > 2 else 4])
        L.px(25 + k, 3 + k, MADERA[1])
    # el cabo enrollado al pie
    for a in range(0, 360, 18):
        r = 3.2
        x, y = 8.5 + math.cos(math.radians(a)) * r, 21.5 + math.sin(math.radians(a)) * r * 0.75
        if not L.leer(int(x), int(y))[3]:
            L.px(int(x), int(y), CUERDA[3 if a < 200 else 1])
    for a in range(0, 360, 30):
        x, y = 8.5 + math.cos(math.radians(a)) * 1.6, 21.5 + math.sin(math.radians(a)) * 1.2
        if not L.leer(int(x), int(y))[3]:
            L.px(int(x), int(y), CUERDA[2])
    L.contorno(TINTA)
    return L


def ancla():
    """El ancla: el arganeo, el cepo, la cana y los brazos con sus unas, de hierro viejo, y un trozo de cadena."""
    L = Lienzo(N, N)
    cx = 15.5
    for y in range(N):
        for x in range(N):
            dx = x - cx
            luz = -dx * 0.18 - (y - 16) * 0.05
            # el arganeo (la anilla de arriba)
            d = math.hypot(dx, y - 5.0)
            if 1.6 <= d <= 3.3:
                L.px(x, y, tono(HIERRO, 3.6 + luz * 1.4))
                continue
            # el cepo
            if 9 <= y <= 10 and abs(dx) <= 7.6:
                L.px(x, y, tono(MADERA, 4.2 - (y - 9) * 1.6 + luz * 0.5))
                continue
            # la cana
            if 7 <= y <= 26 and abs(dx) <= 1.5:
                L.px(x, y, tono(HIERRO, 4.0 - abs(dx + 0.4) * 1.2 + (0.6 if dx < 0 else 0)))
                continue
            # los brazos: un arco por debajo
            d = math.hypot(dx, (y - 17.5) * 1.05)
            if 8.4 <= d <= 10.8 and y >= 19:
                L.px(x, y, tono(HIERRO, 3.4 + luz * 1.2 + (0.8 if d < 9.3 else -0.5 if d > 10.1 else 0)))
    # las unas: dos triangulos en las puntas de los brazos
    for lado in (-1, 1):
        px = 15.5 + lado * 9.6
        for k in range(5):
            for j in range(-k, k + 1):
                x, y = int(round(px + lado * (1.5 - k * 0.6) + j * 0.55)), 17 + k
                if 0 <= x < N:
                    L.px(x, y, tono(HIERRO, 4.6 - k * 0.5 + (0.8 if lado * j < 0 else -0.4)))
    # los remaches del cepo y la punta de la cruz
    for x in (9, 22):
        L.px(x, 9, HIERRO[5])
    for y in (27, 28):
        for x in (15, 16):
            L.px(x, y, HIERRO[2 if y == 28 else 3])
    # la cadena
    for i, (x, y) in enumerate(((19, 3), (21, 2), (23, 3), (25, 5), (27, 6), (28, 8))):
        L.px(x, y, HIERRO[5 if i % 2 == 0 else 3])
        L.px(x + 1, y, HIERRO[3 if i % 2 == 0 else 2])
    L.contorno(TINTA)
    return L


def corazon():
    """El corazon del barco: una piedra de tormenta tallada, azul y encendida, en su engaste de hierro con tres garras."""
    L = Lienzo(N, N)
    cx, cy = 15.5, 15.0
    r = np.random.default_rng(3)
    for y in range(N):
        for x in range(N):
            # un corazon: dos lobulos arriba y la punta abajo
            u, v = (x - cx) / 9.6, (cy + 1.0 - y) / 9.6
            f = (u * u + v * v - 0.62) ** 3 - u * u * v * v * v * 1.5
            if f > 0:
                continue
            d = math.hypot(u, v - 0.1)
            # facetas: la luz dentro, arriba a la izquierda; el borde oscuro
            t = 5.4 - d * 3.6 + (-u * 1.1 + v * 0.9) + (r.random() - 0.5) * 0.5
            cara = (int((x + y) / 4) + int((x - y + 32) / 5)) % 3
            t += (0.7, 0.0, -0.6)[cara]
            L.px(x, y, tono(ALMA, t))
    # el brillo y las vetas de rayo
    for (x, y) in ((11, 9), (12, 9), (11, 10), (12, 10), (10, 11)):
        if L.leer(x, y)[3]:
            L.px(x, y, ALMA[6])
    for (x, y) in ((16, 12), (17, 13), (16, 14), (17, 15), (18, 16), (17, 17), (16, 18), (15, 19), (16, 20)):
        if L.leer(x, y)[3]:
            L.px(x, y, ALMA[5 if (x + y) % 2 else 6])
    # el engaste: un aro por debajo y tres garras
    for y in range(N):
        for x in range(N):
            dx, dy = x - cx, y - 21.5
            d = math.hypot(dx, dy * 1.9)
            if 9.0 <= d <= 11.0 and y >= 20:
                L.px(x, y, tono(HIERRO, 3.2 - dx * 0.16 + (0.8 if d < 9.8 else 0)))
    for (x0, y0, x1, y1) in ((6, 22, 8, 16), (25, 22, 23, 16), (15, 27, 15, 23)):
        linea(L, x0, y0, x1, y1, HIERRO[4], 2)
        L.px(x1, y1, HIERRO[6])
    L.contorno(TINTA)
    return L


PIEZAS = {"vela": vela, "timon": timon, "mastil": mastil, "ancla": ancla, "corazon": corazon}


def main():
    vista = sys.argv[sys.argv.index("--vista") + 1] if "--vista" in sys.argv else None
    hoja = Lienzo(N * 5 + 6, N + 2, "#3a3a44")
    for i, (nombre, f) in enumerate(PIEZAS.items()):
        L = f()
        L.guardar(os.path.join(ASSETS, "textures", "item", "pieza_" + nombre + ".png"))
        ruta = os.path.join(ASSETS, "models", "item", "pieza_" + nombre + ".json")
        with open(ruta, "w", encoding="utf-8") as fh:
            json.dump({"parent": "item/generated", "textures": {"layer0": "darkcraft:item/pieza_" + nombre}}, fh)
        hoja.pegar(L, 1 + i * (N + 1), 1)
    if vista:
        os.makedirs(vista, exist_ok=True)
        im = hoja.imagen()
        im.resize((im.width * 8, im.height * 8), 0).save(os.path.join(vista, "piezas.png"))
        print("vista en", os.path.join(vista, "piezas.png"))
    print("piezas:", ", ".join(PIEZAS))


if __name__ == "__main__":
    main()
