"""Texturas del bloque de las estacas de raiz de Gavran (darkcraft:raiz_gavran).

Solo se ve en las entidades block_display de sus ataques (estiradas: finas y altas), asi que el lado lleva la veta
en vertical: corteza casi negra con surcos, nudos y dos venas de savia verde que brillan; la punta, anillos oscuros
con el corazon verde.

    python3 tools/gavran/raiz_bloque.py
"""
import os

import numpy as np
from PIL import Image

N = 16
SALIDA = os.path.join(os.path.dirname(__file__), '..', '..', 'src', 'main', 'resources', 'assets', 'darkcraft', 'textures', 'block')

CORTEZA = np.array([[22, 15, 11], [34, 23, 16], [46, 31, 21], [60, 41, 27], [74, 52, 34]], float)
SURCO = np.array([12, 8, 7], float)
VENA = np.array([104, 236, 120], float)
VENA_BORDE = np.array([28, 120, 52], float)


def ruido(rng, w, h, escala):
    """Ruido suave (cuadricula de valores aleatorios interpolada), que se repite en los bordes."""
    gw, gh = max(1, w // escala), max(1, h // escala)
    g = rng.random((gh, gw))
    ys, xs = np.mgrid[0:h, 0:w]
    fx, fy = xs / w * gw, ys / h * gh
    x0, y0 = np.floor(fx).astype(int), np.floor(fy).astype(int)
    tx, ty = fx - x0, fy - y0
    tx, ty = tx * tx * (3 - 2 * tx), ty * ty * (3 - 2 * ty)
    x1, y1 = (x0 + 1) % gw, (y0 + 1) % gh
    x0, y0 = x0 % gw, y0 % gh
    a = g[y0, x0] * (1 - tx) + g[y0, x1] * tx
    b = g[y1, x0] * (1 - tx) + g[y1, x1] * tx
    return a * (1 - ty) + b * ty


def lado(rng):
    # veta vertical: ruido muy estirado en y, mas un poco de ruido fino
    cols = rng.random(N)
    cols = np.convolve(np.r_[cols[-1:], cols, cols[:1]], [0.25, 0.5, 0.25], 'valid')
    v = cols[None, :].repeat(N, 0) * 0.7 + ruido(rng, N, N, 4) * 0.3
    v += (rng.random((N, N)) - 0.5) * 0.12
    idx = np.clip((v * len(CORTEZA)).astype(int), 0, len(CORTEZA) - 1)
    img = CORTEZA[idx].copy()
    # surcos verticales (dos o tres columnas oscuras que serpentean un poco)
    for x0 in (2, 7, 12):
        x = x0 + rng.integers(-1, 2)
        for y in range(N):
            if rng.random() < 0.82:
                img[y, x % N] = SURCO * (0.8 + 0.4 * rng.random())
            if rng.random() < 0.3:
                x += rng.integers(-1, 2)
    # nudo
    ny, nx = rng.integers(3, 13), rng.integers(3, 13)
    for dy in range(-1, 2):
        for dx in range(-1, 2):
            img[(ny + dy) % N, (nx + dx) % N] = CORTEZA[0] if dx == 0 and dy == 0 else CORTEZA[1]
    # venas de savia verde que suben (una entera y otra a trozos), con borde mas oscuro
    for x0, prob in ((4, 0.62), (11, 0.22)):
        x = x0
        for y in range(N):
            if rng.random() < prob:
                if rng.random() < 0.5:
                    img[y, (x - 1) % N] = img[y, (x - 1) % N] * 0.6 + VENA_BORDE * 0.4
                img[y, x % N] = VENA * (0.75 + 0.25 * rng.random())
            if rng.random() < 0.35:
                x += rng.choice([-1, 1])
    # luces en los bordes de la corteza (relieve)
    for y in range(N):
        for x in range(N):
            if img[y, x].sum() < SURCO.sum() * 1.3 and img[y, (x + 1) % N].sum() > 100 and img[y, (x + 1) % N][1] < 120:
                img[y, (x + 1) % N] = np.minimum(255, img[y, (x + 1) % N] * 1.25)
    return img


def punta(rng):
    img = np.zeros((N, N, 3))
    c = (N - 1) / 2
    for y in range(N):
        for x in range(N):
            r = np.hypot(x - c, y - c) + (rng.random() - 0.5) * 0.6
            if r < 1.3:
                img[y, x] = VENA
            elif r < 2.3:
                img[y, x] = VENA_BORDE
            elif r > 6.6:
                img[y, x] = CORTEZA[1] * (0.8 + 0.3 * rng.random())
            else:
                anillo = int(r * 1.4) % 2
                img[y, x] = CORTEZA[2 + anillo] * (0.9 + 0.15 * rng.random())
    # grietas que salen del centro
    for ang in rng.random(3) * np.pi * 2:
        for k in range(3, 8):
            x, y = int(round(c + np.cos(ang) * k)), int(round(c + np.sin(ang) * k))
            img[y % N, x % N] = SURCO
    return img


def main():
    rng = np.random.default_rng(7)
    os.makedirs(SALIDA, exist_ok=True)
    for nombre, img in (('raiz_gavran', lado(rng)), ('raiz_gavran_punta', punta(rng))):
        Image.fromarray(np.clip(img, 0, 255).astype(np.uint8), 'RGB').save(os.path.join(SALIDA, nombre + '.png'))
        print('escrito', nombre)


if __name__ == '__main__':
    main()
