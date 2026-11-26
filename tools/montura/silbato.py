#!/usr/bin/env python3
"""
El silbato del corcel (parte 7b): un silbato de hueso curvo, como un cuerno pequeno, con tres agujeros, una boquilla
atada con cuero negro, el cordon y una voluta de niebla azul que sale de la boca. 32 x 32, pixel art con contorno.

    python3 tools/montura/silbato.py [--vista carpeta]
"""
import os
import sys

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.normpath(os.path.join(AQUI, "..", ".."))
SALIDA = os.path.join(RAIZ, "src", "main", "resources", "assets", "darkcraft", "textures", "item", "silbato_corcel.png")
N = 32

HUESO = [(92, 80, 62), (138, 124, 98), (184, 172, 142), (216, 206, 178), (240, 234, 214), (252, 250, 238)]
CONTORNO = (46, 36, 30)
CUERO = [(24, 20, 22), (48, 40, 40), (74, 62, 58)]
CORDON = [(70, 22, 24), (118, 36, 36), (156, 58, 52)]
NIEBLA = [(120, 200, 214), (168, 232, 236), (228, 252, 252)]


def bezier(t, p0, p1, p2):
    return (1 - t) ** 2 * p0 + 2 * (1 - t) * t * p1 + t * t * p2


def curva(n=400):
    p0, p1, p2 = np.array([7.0, 25.0]), np.array([9.0, 12.0]), np.array([24.5, 7.5])
    ts = np.linspace(0, 1, n)
    pts = np.array([bezier(t, p0, p1, p2) for t in ts])
    return ts, pts


def dibujar():
    img = np.zeros((N, N, 4))
    ts, pts = curva()
    # radio: fino en la boquilla, ancho en la boca
    radio = 1.5 + 2.3 * ts ** 1.6
    tubo = np.zeros((N, N), bool)
    luz = np.zeros((N, N))
    tt = np.zeros((N, N))
    for y in range(N):
        for x in range(N):
            c = np.array([x + 0.5, y + 0.5])
            d = np.hypot(pts[:, 0] - c[0], pts[:, 1] - c[1]) - radio
            i = int(np.argmin(d))
            if d[i] <= 0:
                tubo[y, x] = True
                tt[y, x] = ts[i]
                # normal: del eje al pixel; la luz viene de arriba a la izquierda
                v = c - pts[i]
                ln = np.hypot(*v) + 1e-6
                nx, ny = v / ln
                prof = ln / radio[i]
                luz[y, x] = 0.55 + 0.45 * (-0.6 * nx - 0.8 * ny) * min(1, prof * 1.4) - 0.15 * prof ** 3
    for y in range(N):
        for x in range(N):
            if not tubo[y, x]:
                continue
            k = int(np.clip(round(luz[y, x] * 5), 0, 5))
            col = HUESO[k]
            # vetas del hueso
            if (x * 5 + y * 3) % 17 == 0 and 1 < k < 5:
                col = HUESO[k + 1]
            img[y, x, :3] = col
            img[y, x, 3] = 255
    # la boca (hueca, oscura) en el extremo ancho
    bx, by = pts[-1]
    for y in range(N):
        for x in range(N):
            if tubo[y, x] and np.hypot(x + 0.5 - bx - 0.6, y + 0.5 - by + 0.2) < 2.1:
                img[y, x, :3] = (40, 30, 34) if np.hypot(x + 0.5 - bx - 0.9, y + 0.5 - by) < 1.4 else HUESO[1]
    # tres agujeros a lo largo del tubo, en la cara de arriba
    for t in (0.42, 0.58, 0.73):
        i = int(t * (len(ts) - 1))
        p = pts[i]
        d = pts[min(i + 4, len(pts) - 1)] - pts[max(i - 4, 0)]
        nrm = np.array([d[1], -d[0]]) / np.hypot(*d)
        if nrm[1] > 0:
            nrm = -nrm
        q = p + nrm * radio[i] * 0.35
        x, y = int(q[0]), int(q[1])
        img[y, x, :3] = (36, 28, 30)
        img[y, x, 3] = 255
        if 0 <= y + 1 < N and tubo[y + 1, x]:
            img[y + 1, x, :3] = HUESO[1]
    # la boquilla atada con cuero negro (dos vueltas)
    for t0 in (0.17, 0.29):
        for y in range(N):
            for x in range(N):
                if tubo[y, x] and abs(tt[y, x] - t0) < 0.042:
                    k = int(np.clip(round(luz[y, x] * 2), 0, 2))
                    img[y, x, :3] = CUERO[k]
    # el cordon: cuelga de la atadura y da una vuelta hacia abajo a la derecha
    cordon = [(11, 21), (12, 22), (13, 23), (14, 24), (15, 25), (16, 25), (17, 26), (18, 26), (19, 26), (20, 25), (21, 24),
              (21, 23), (20, 22), (19, 22)]
    for i, (x, y) in enumerate(cordon):
        if img[y, x, 3] == 0:
            img[y, x, :3] = CORDON[1 if i % 3 else 2]
            img[y, x, 3] = 255
    # contorno
    lleno = img[:, :, 3] > 0
    for y in range(N):
        for x in range(N):
            if lleno[y, x]:
                continue
            vec = [(x + dx, y + dy) for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))]
            if any(0 <= a < N and 0 <= b < N and lleno[b, a] for a, b in vec):
                img[y, x, :3] = CONTORNO
                img[y, x, 3] = 255
    # la voluta de niebla que sale de la boca (sin contorno, medio transparente)
    voluta = [(28, 6, 2, 235), (29, 5, 2, 220), (29, 4, 1, 200), (30, 3, 1, 180), (30, 2, 0, 150), (29, 1, 0, 120), (28, 1, 1, 130),
              (27, 2, 1, 150), (27, 3, 2, 170), (31, 4, 0, 110), (30, 6, 1, 160), (31, 7, 0, 110), (28, 7, 1, 190), (26, 1, 0, 90),
              (25, 2, 0, 70), (31, 1, 0, 70)]
    for x, y, k, a in voluta:
        if 0 <= x < N and 0 <= y < N and img[y, x, 3] < 255:
            img[y, x, :3] = NIEBLA[k]
            img[y, x, 3] = a
    return img


def main():
    img = dibujar()
    os.makedirs(os.path.dirname(SALIDA), exist_ok=True)
    Image.fromarray(img.astype(np.uint8), "RGBA").save(SALIDA)
    print("  ", os.path.relpath(SALIDA, RAIZ))
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        im = Image.open(SALIDA)
        fondo = Image.new("RGBA", (N, N), (58, 74, 92, 255))
        fondo.alpha_composite(im)
        fondo.resize((N * 10, N * 10), Image.NEAREST).save(os.path.join(d, "silbato.png"))


if __name__ == "__main__":
    main()
