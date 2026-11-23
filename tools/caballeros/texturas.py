#!/usr/bin/env python3
"""
Texturas de los caballeros zombi (64x64, la disposicion de HumanoidModel: brazos y piernas iguales, en espejo) y de
la alabarda (16x16). Pixel art pintado con numpy: piel podrida verdosa, casco de hierro oxidado con la visera en T y
ojos rojos, peto con el tabardo del color de cada tipo y la corona de Valdrenn, cota de malla y grebas.
Uso: python3 tools/caballeros/texturas.py
"""
import os

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
RAIZ = os.path.join(AQUI, "..", "..", "src", "main", "resources", "assets", "darkcraft", "textures")
R = np.random.default_rng(77)


def col(h):
    return np.array([(h >> 16) & 255, (h >> 8) & 255, h & 255, 255], dtype=float)


class Lienzo:
    def __init__(self, w=64, h=64):
        self.a = np.zeros((h, w, 4))

    def rect(self, x, y, w, h, c, ruido=10, semilla=None):
        rr = np.random.default_rng(semilla if semilla is not None else (x * 131 + y * 17 + w))
        n = rr.integers(-ruido, ruido + 1, size=(h, w, 1)) if ruido else 0
        blk = np.broadcast_to(c, (h, w, 4)).copy()
        blk[..., :3] = np.clip(blk[..., :3] + n, 0, 255)
        self.a[y:y + h, x:x + w] = blk

    def px(self, x, y, c):
        self.a[y, x] = c

    def guardar(self, ruta):
        os.makedirs(os.path.dirname(ruta), exist_ok=True)
        Image.fromarray(np.clip(self.a, 0, 255).astype(np.uint8), "RGBA").save(ruta, optimize=True)
        print("ok", os.path.relpath(ruta, RAIZ))


PIEL, PIEL2, PIEL3 = col(0x5E7A52), col(0x4A6342), col(0x3A4E34)
HIERRO, HIERRO2, HIERRO3, OXIDO = col(0x6E6E74), col(0x55555C), col(0x3C3C42), col(0x7A4A2A)
MALLA, MALLA2 = col(0x8A8A90), col(0x4E4E54)
NEGRO, ROJO_OJO, ORO = col(0x120A0C), col(0xFF3A22), col(0xC89A2A)


def caja(L, ox, oy, w, h, d, caras):
    """Rellena las seis caras de una caja de la piel (posicion de la textura ox, oy; medidas w, h, d)."""
    # arriba, abajo, derecha, delante, izquierda, detras
    pos = {
        "arriba": (ox + d, oy, w, d), "abajo": (ox + d + w, oy, w, d),
        "der": (ox, oy + d, d, h), "frente": (ox + d, oy + d, w, h),
        "izq": (ox + d + w, oy + d, d, h), "atras": (ox + d + w + d, oy + d, w, h),
    }
    for k, f in caras.items():
        x, y, ww, hh = pos[k]
        f(L, x, y, ww, hh)


def malla(L, x, y, w, h):
    for j in range(h):
        for i in range(w):
            L.px(x + i, y + j, MALLA if (i + j) % 2 == 0 else MALLA2)


def placa(c1, c2, remaches=True):
    def f(L, x, y, w, h):
        L.rect(x, y, w, h, c1, 9)
        L.rect(x, y + h - 1, w, 1, c2, 4)
        if w > 2:
            L.rect(x, y, 1, h, c2, 4)
        # oxido aqui y alla
        rr = np.random.default_rng(x * 7 + y)
        for _ in range(max(1, w * h // 9)):
            L.px(x + rr.integers(0, w), y + rr.integers(0, h), OXIDO * 0.9 + HIERRO * 0.1)
        if remaches and w >= 4 and h >= 4:
            L.px(x + 1, y + 1, col(0xA8A8AE))
            L.px(x + w - 2, y + 1, col(0xA8A8AE))
    return f


def piel(L, x, y, w, h):
    L.rect(x, y, w, h, PIEL, 12)
    rr = np.random.default_rng(x * 13 + y * 3)
    for _ in range(w * h // 6):
        L.px(x + rr.integers(0, w), y + rr.integers(0, h), PIEL3 if rr.random() < 0.5 else PIEL2)


def cara(L, x, y, w, h):
    piel(L, x, y, w, h)
    # cuencas oscuras y ojos rojos
    L.rect(x + 1, y + 3, 2, 2, NEGRO, 0)
    L.rect(x + 5, y + 3, 2, 2, NEGRO, 0)
    L.px(x + 2, y + 4, ROJO_OJO)
    L.px(x + 5, y + 4, ROJO_OJO)
    # nariz podrida y boca
    L.px(x + 3, y + 5, PIEL3)
    L.px(x + 4, y + 5, PIEL3)
    L.rect(x + 2, y + 6, 4, 1, NEGRO, 0)
    L.px(x + 3, y + 7, PIEL3)


def casco(L, x, y, w, h, frente=False):
    L.rect(x, y, w, h, HIERRO2, 8)
    L.rect(x, y, w, 1, HIERRO, 6)
    if frente:
        # visera en T: hueco (transparente) para los ojos y la boca
        L.a[y + 3:y + 5, x + 1:x + 7] = 0
        L.a[y + 5:y + 7, x + 3:x + 5] = 0
        L.rect(x, y + 2, w, 1, HIERRO3, 2)
        L.px(x + 1, y + 1, col(0xB0B0B6))
        L.px(x + 6, y + 1, col(0xB0B0B6))
        L.rect(x + 3, y, 2, 3, HIERRO3, 2)  # cresta del casco
    else:
        rr = np.random.default_rng(x + y * 5)
        for _ in range(4):
            L.px(x + rr.integers(0, w), y + rr.integers(0, h), OXIDO)


def textura(nombre, tabardo, emblema=ORO):
    L = Lienzo()
    # cabeza (0,0) 8x8x8
    caja(L, 0, 0, 8, 8, 8, {"arriba": piel, "abajo": piel, "der": piel, "frente": cara, "izq": piel, "atras": piel})
    # casco (capa de encima, 32,0)
    caja(L, 32, 0, 8, 8, 8, {"arriba": lambda L, x, y, w, h: casco(L, x, y, w, h), "abajo": lambda L, x, y, w, h: None,
                              "der": lambda L, x, y, w, h: casco(L, x, y, w, h), "frente": lambda L, x, y, w, h: casco(L, x, y, w, h, True),
                              "izq": lambda L, x, y, w, h: casco(L, x, y, w, h), "atras": lambda L, x, y, w, h: casco(L, x, y, w, h)})
    # el borde de abajo del casco no tapa la cara por los lados
    # cuerpo (16,16) 8x12x4: peto con el tabardo delante
    def peto(L, x, y, w, h):
        placa(HIERRO, HIERRO3, False)(L, x, y, w, h)
        L.rect(x + 2, y + 1, 4, h - 1, tabardo, 10)
        L.rect(x + 2, y + h - 1, 4, 1, tabardo * 0.7, 4)
        # corona del emblema
        L.px(x + 3, y + 3, emblema)
        L.px(x + 4, y + 3, emblema)
        L.px(x + 2, y + 2, emblema)
        L.px(x + 5, y + 2, emblema)
        L.px(x + 3, y + 2, emblema * 0.8)
        L.px(x + 4, y + 2, emblema * 0.8)
        # cinturon
        L.rect(x, y + 7, w, 1, col(0x3A2414), 4)
        L.px(x + 4, y + 7, ORO)

    def espalda(L, x, y, w, h):
        placa(HIERRO, HIERRO3)(L, x, y, w, h)
        L.rect(x, y + 7, w, 1, col(0x3A2414), 4)

    caja(L, 16, 16, 8, 12, 4, {"arriba": placa(HIERRO, HIERRO3, False), "abajo": malla, "der": malla, "frente": peto, "izq": malla,
                                "atras": espalda})

    # brazo (40,16) 4x12x4: hombrera arriba, malla y la mano podrida abajo
    def brazo(L, x, y, w, h):
        malla(L, x, y, w, h)
        L.rect(x, y, w, 3, HIERRO, 8)
        L.rect(x, y + 3, w, 1, HIERRO3, 3)
        L.rect(x, y + h - 3, w, 3, PIEL, 10)
        L.px(x + 1, y + h - 2, PIEL3)

    caja(L, 40, 16, 4, 12, 4, {"arriba": placa(HIERRO, HIERRO3, False), "abajo": piel, "der": brazo, "frente": brazo, "izq": brazo,
                                "atras": brazo})

    # pierna (0,16) 4x12x4: grebas
    def pierna(L, x, y, w, h):
        malla(L, x, y, w, 5)
        L.rect(x, y + 5, w, h - 5, HIERRO2, 8)
        L.rect(x, y + 5, w, 1, HIERRO, 4)
        L.rect(x, y + h - 2, w, 2, HIERRO3, 4)
        L.px(x + 1, y + 7, OXIDO)

    caja(L, 0, 16, 4, 12, 4, {"arriba": malla, "abajo": placa(HIERRO3, HIERRO3, False), "der": pierna, "frente": pierna, "izq": pierna,
                               "atras": pierna})
    L.guardar(os.path.join(RAIZ, "entity", "caballero", nombre + ".png"))


def alabarda():
    """16x16: el asta en diagonal y la cabeza (hacha, pica y gancho) arriba a la derecha."""
    L = Lienzo(16, 16)
    madera, madera2 = col(0x6A4426), col(0x4A2E18)
    for i in range(13):
        x, y = 1 + i, 14 - i
        L.px(x, y, madera if i % 3 else madera2)
        if i < 12:
            L.px(x + 1, y, madera2)
    # hoja del hacha
    hoja, hoja2, filo = col(0x9A9AA2), col(0x6A6A72), col(0xE0E0E8)
    for (x, y) in [(9, 3), (10, 3), (9, 4), (10, 4), (8, 4), (8, 5), (9, 5), (7, 5), (8, 6), (7, 6), (6, 6)]:
        L.px(x, y, hoja)
    for (x, y) in [(6, 7), (7, 7), (5, 6), (6, 5), (7, 4), (8, 3)]:
        L.px(x, y, filo)
    for (x, y) in [(10, 5), (9, 6), (11, 4)]:
        L.px(x, y, hoja2)
    # pica
    for (x, y) in [(13, 1), (14, 0), (12, 2)]:
        L.px(x, y, filo)
    # gancho
    for (x, y) in [(13, 4), (14, 4), (14, 5)]:
        L.px(x, y, hoja2)
    L.guardar(os.path.join(RAIZ, "item", "alabarda.png"))


if __name__ == "__main__":
    textura("espada", col(0x8E1418))
    textura("alabarda", col(0x4A1C5C), col(0xB8B8C8))
    textura("ballesta", col(0x2E4A2C), col(0xC89A2A))
    alabarda()
