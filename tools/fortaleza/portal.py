#!/usr/bin/env python3
"""
El portal de la Sombra nuevo (parte 5e, respuestas 51-56 del plan): los bloques del marco y el remolino.

  piedra_sombra            piedra negra violacea labrada, tres variantes de runa (piedra_sombra_1..3) y, encima, sus
                           runas rojas que laten (piedra_sombra_runas_1..3, 16 fotogramas: se dibujan con luz propia)
  hueso_labrado(_top)      hueso tallado con anillos y estrias (el anillo de dentro del arco)
  hierro_negro             planchas de hierro negro con remaches
  gema_roja / gema_roja_luz  la gema en su engaste: apagada, y encendida (con luz propia) al abrirse el portal
  entity/remolino.png      el remolino rojo y negro del portal (256 x 256): un solo dibujo que el juego hace girar
  entity/nubes_remolino.png  las nubes que se arremolinan sobre el portal en el capitulo 6
  environment/luna_sangre.png  la luna de sangre (desde el rayo rojo)

Pixel art procedural, siempre igual (semilla fija). Uso: python3 tools/fortaleza/portal.py
"""
import json
import math
import os

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.normpath(os.path.join(AQUI, "..", ".."))
TEX = os.path.join(RAIZ, "src", "main", "resources", "assets", "darkcraft", "textures")
R = np.random.default_rng(5150)


def guardar(img, ruta, mcmeta=None):
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), "RGBA").save(ruta)
    if mcmeta:
        with open(ruta + ".mcmeta", "w") as f:
            json.dump(mcmeta, f)
    print("  ", os.path.relpath(ruta, RAIZ))


def mezcla(a, b, t):
    return np.array(a, float) * (1 - t) + np.array(b, float) * t


# ---------------------------------------------------------------- piedra de sombra

PIEDRA = [(18, 14, 24), (28, 22, 36), (40, 31, 50), (55, 44, 66)]
SURCO = (9, 6, 12)
RUNA = [(100, 8, 8), (170, 18, 14), (225, 36, 24), (250, 84, 56)]

# tres runas (de 16 x 16; '#' surco con luz, '.' nada) con el marco tallado aparte
RUNAS = [
    [
        "................",
        "................",
        ".......##.......",
        "....#..##..#....",
        ".....#.##.#.....",
        "......####......",
        ".......##.......",
        ".......##.......",
        ".......##.......",
        "......####......",
        ".....#.##.#.....",
        "....#..##..#....",
        ".......##.......",
        "................",
        "................",
        "................",
    ],
    [
        "................",
        "................",
        "....########....",
        "....#......#....",
        "....#.####.#....",
        "......#..#......",
        "......#..#......",
        "....###..###....",
        "......#..#......",
        "......#..#......",
        "....#.####.#....",
        "....#......#....",
        "....########....",
        "................",
        "................",
        "................",
    ],
    [
        "................",
        "................",
        "......#..#......",
        ".....#....#.....",
        "....#......#....",
        "....#..##..#....",
        ".....#.##.#.....",
        "......####......",
        ".......##.......",
        "......####......",
        ".....#.##.#.....",
        "....#..##..#....",
        "...#...##...#...",
        "................",
        "................",
        "................",
    ],
]


def piedra(k):
    img = np.zeros((16, 16, 4))
    rng = np.random.default_rng(300 + k)
    for y in range(16):
        for x in range(16):
            v = rng.uniform(0, 1)
            c = PIEDRA[1] if v < 0.55 else PIEDRA[2] if v < 0.85 else PIEDRA[0]
            # el marco tallado: el borde mas claro arriba a la izquierda, mas oscuro abajo a la derecha
            if x == 0 or y == 0:
                c = PIEDRA[3]
            elif x == 15 or y == 15:
                c = PIEDRA[0]
            elif x == 1 or y == 1 or x == 14 or y == 14:
                c = mezcla(c, SURCO, 0.5)
            img[y, x, :3] = c
            img[y, x, 3] = 255
    runa = RUNAS[k]
    for y in range(16):
        for x in range(16):
            if runa[y][x] == "#":
                img[y, x, :3] = SURCO
    return img


def runas(k):
    """16 fotogramas: la runa late (sube y baja), con una chispa que la recorre."""
    runa = RUNAS[k]
    celdas = [(x, y) for y in range(16) for x in range(16) if runa[y][x] == "#"]
    out = np.zeros((16 * 16, 16, 4))
    for f in range(16):
        latido = 0.5 + 0.5 * math.sin(2 * math.pi * (f / 16.0) + k * 1.3)
        # la chispa: recorre la runa de arriba abajo
        chispa = (f / 16.0) * 16
        for (x, y) in celdas:
            t = 0.35 + 0.65 * latido
            if abs(y - chispa) < 1.2:
                t = min(1.0, t + 0.35)
            idx = min(3, int(t * 3.999))
            c = RUNA[idx]
            out[f * 16 + y, x, :3] = c
            out[f * 16 + y, x, 3] = 255
    return out


# ---------------------------------------------------------------- hueso labrado

def hueso(v):
    osc = np.array([118, 104, 82])
    med = np.array([206, 196, 168])
    luz = np.array([240, 234, 214])
    v = max(0.0, min(1.0, v))
    if v < 0.5:
        return osc + (med - osc) * (v / 0.5)
    return med + (luz - med) * ((v - 0.5) / 0.5)


def hueso_lado():
    img = np.zeros((16, 16, 4))
    for y in range(16):
        for x in range(16):
            v = 0.62 + R.uniform(-0.06, 0.06)
            # estrias verticales y anillos tallados cada cuatro
            if x in (3, 8, 12):
                v -= 0.16
            if y % 8 in (0, 1):
                v = 0.4 if y % 8 == 0 else 0.82
            if x in (0, 15):
                v -= 0.1
            img[y, x, :3] = hueso(v)
            img[y, x, 3] = 255
    return img


def hueso_arriba():
    img = np.zeros((16, 16, 4))
    for y in range(16):
        for x in range(16):
            r = math.hypot(x - 7.5, y - 7.5)
            v = 0.66 + R.uniform(-0.05, 0.05)
            if 5.0 < r < 6.2 or 2.0 < r < 3.0:
                v = 0.42
            if r < 1.6:
                v = 0.3
            img[y, x, :3] = hueso(v)
            img[y, x, 3] = 255
    return img


# ---------------------------------------------------------------- hierro negro

def hierro():
    img = np.zeros((16, 16, 4))
    base = [(28, 30, 36), (38, 40, 47), (48, 51, 59), (70, 73, 82)]
    for y in range(16):
        for x in range(16):
            v = R.uniform(0, 1)
            c = base[1] if v < 0.6 else base[2] if v < 0.92 else base[0]
            # dos planchas (la junta en medio) con el canto claro arriba
            if y == 7:
                c = base[0]
            if y in (0, 8):
                c = base[3]
            if x in (0,) or y in (15,):
                c = base[0]
            img[y, x, :3] = c
            img[y, x, 3] = 255
            # algo de oxido rojo
            if R.uniform(0, 1) < 0.04:
                img[y, x, :3] = (74, 30, 22)
    # los remaches
    for (x, y) in ((2, 2), (13, 2), (2, 10), (13, 10), (7, 4), (7, 12)):
        img[y, x, :3] = (96, 99, 110)
        img[y + 1, x, :3] = (20, 21, 26)
    return img


# ---------------------------------------------------------------- la gema

def gema(luz):
    img = hierro()
    # el engaste: un rombo de 10 de alto en el centro, tallado en facetas
    for y in range(16):
        for x in range(16):
            dx, dy = abs(x - 7.5), abs(y - 7.5)
            d = dx + dy
            if d > 6.6:
                continue
            if d > 5.6:
                img[y, x, :3] = (14, 14, 18)
                continue
            # facetas: cuatro triangulos con luz distinta
            cara = (0 if x < 7.5 else 1) + (0 if y < 7.5 else 2)
            luzc = [0.9, 0.7, 0.55, 0.35][cara]
            if luz:
                c = mezcla((150, 20, 10), (255, 120, 60), luzc)
            else:
                c = mezcla((40, 6, 8), (110, 18, 20), luzc)
            img[y, x, :3] = c
    if luz:
        img[4, 6, :3] = (255, 230, 200)
        img[5, 6, :3] = (255, 200, 150)
    else:
        img[4, 6, :3] = (150, 60, 60)
    return img


def gema_brillo():
    """Solo lo que luce de la gema encendida (encima, con luz propia)."""
    img = gema(True)
    for y in range(16):
        for x in range(16):
            if abs(x - 7.5) + abs(y - 7.5) > 5.6:
                img[y, x, 3] = 0
    return img


# ---------------------------------------------------------------- el remolino

def remolino(n=256):
    """Brazos de espiral rojos y negros que se cierran hacia el centro; el juego gira el dibujo."""
    img = np.zeros((n, n, 4))
    c = (n - 1) / 2.0
    for y in range(n):
        for x in range(n):
            dx, dy = (x - c) / c, (y - c) / c
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            # tres brazos en espiral logaritmica + ruido fino
            s = math.sin(3 * a + 7.5 * math.log(r + 0.05) + 0.4 * math.sin(11 * a))
            s = (s + 1) / 2
            g = 0.5 + 0.5 * math.sin(23 * r + 5 * a)
            t = min(1.0, s * 0.85 + g * 0.15)
            # del negro al rojo, y del rojo a un naranja palido hacia el centro
            if t < 0.5:
                col = mezcla((6, 2, 6), (120, 10, 14), t / 0.5)
            else:
                col = mezcla((120, 10, 14), (240, 50, 30), (t - 0.5) / 0.5)
            centro = max(0.0, 1 - r / 0.22)
            col = mezcla(col, (255, 170, 120), centro * 0.85)
            img[y, x, :3] = col
            # (borde transparente: el juego lo usa con "clamp", y lo que cae fuera del circulo no se ve)
            img[y, x, 3] = 235 if r < 0.86 else 235 * max(0.0, 1 - (r - 0.86) / 0.1)
    return img


def nubes_remolino(n=256):
    """Las nubes que se arremolinan sobre el portal (capitulo 6): brazos de nube gris en espiral, mas densos hacia el
    centro, con el borde transparente (el juego lo usa con "clamp" y lo tine de rojo despues del rayo)."""
    img = np.zeros((n, n, 4))
    c = (n - 1) / 2.0
    ruido = R.random((32, 32))
    for y in range(n):
        for x in range(n):
            dx, dy = (x - c) / c, (y - c) / c
            r = math.hypot(dx, dy)
            a = math.atan2(dy, dx)
            brazo = (math.sin(4 * a + 9.0 * math.log(r + 0.04)) + 1) / 2
            # (ruido suave: interpolado entre los puntos de la rejilla)
            fy, fx = y / n * 31, x / n * 31
            iy, ix = int(fy), int(fx)
            ty, tx = fy - iy, fx - ix
            gr = (ruido[iy, ix] * (1 - tx) * (1 - ty) + ruido[iy, ix + 1] * tx * (1 - ty)
                  + ruido[iy + 1, ix] * (1 - tx) * ty + ruido[iy + 1, ix + 1] * tx * ty)
            dens = min(1.0, brazo * 0.8 + gr * 0.35 + max(0.0, 0.35 - r) * 1.6)
            gris = mezcla((60, 56, 62), (205, 200, 210), dens ** 1.2)
            img[y, x, :3] = gris
            borde = 1.0 if r < 0.7 else max(0.0, 1 - (r - 0.7) / 0.16)
            img[y, x, 3] = 255 * borde * (0.35 + 0.65 * dens)
    return img


def luna_sangre(n=64):
    """La luna de sangre: un disco rojo oscuro con mares y crateres (tapa la luna de siempre) y un halo rojo alrededor."""
    img = np.zeros((n, n, 4))
    c = (n - 1) / 2.0
    for y in range(n):
        for x in range(n):
            dx, dy = (x - c) / c, (y - c) / c
            r = math.hypot(dx, dy)
            if r < 0.78:
                mar = 0.5 + 0.5 * math.sin(dx * 7.1 + 1.3) * math.cos(dy * 5.3 - 0.4)
                crater = sum(max(0.0, 1 - math.hypot(dx - cx, dy - cy) / cr) for cx, cy, cr in
                             ((-0.3, -0.2, 0.18), (0.25, 0.3, 0.14), (0.1, -0.45, 0.1), (-0.4, 0.35, 0.12), (0.45, -0.1, 0.09)))
                t = min(1.0, 0.35 * mar + 0.6 * crater)
                col = mezcla((196, 34, 22), (108, 10, 12), t)
                # el borde, mas oscuro (como una esfera)
                col = mezcla(col, (70, 6, 8), max(0.0, (r - 0.55) / 0.23) ** 1.5)
                img[y, x, :3] = col
                img[y, x, 3] = 255
            else:
                halo = max(0.0, 1 - (r - 0.78) / 0.22)
                img[y, x, :3] = (200, 30, 20)
                img[y, x, 3] = 150 * halo ** 2
    return img


def main():
    b = os.path.join(TEX, "block")
    for k in range(3):
        guardar(piedra(k), os.path.join(b, f"piedra_sombra_{k + 1}.png"))
        guardar(runas(k), os.path.join(b, f"piedra_sombra_runas_{k + 1}.png"), {"animation": {"frametime": 3, "interpolate": True}})
    guardar(hueso_lado(), os.path.join(b, "hueso_labrado.png"))
    guardar(hueso_arriba(), os.path.join(b, "hueso_labrado_top.png"))
    guardar(hierro(), os.path.join(b, "hierro_negro.png"))
    guardar(gema(False), os.path.join(b, "gema_roja.png"))
    guardar(gema(True), os.path.join(b, "gema_roja_luz.png"))
    guardar(gema_brillo(), os.path.join(b, "gema_roja_brillo.png"))
    guardar(remolino(), os.path.join(TEX, "entity", "remolino.png"), {"texture": {"clamp": True}})
    guardar(nubes_remolino(), os.path.join(TEX, "entity", "nubes_remolino.png"), {"texture": {"clamp": True}})
    guardar(luna_sangre(), os.path.join(TEX, "environment", "luna_sangre.png"))


if __name__ == "__main__":
    main()
