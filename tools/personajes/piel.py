#!/usr/bin/env python3
"""
Utiles para pintar las pieles (64 x 64, como las de los jugadores, brazos finos) de los personajes del mod: donde cae
cada cara de cada caja en la textura, telas con pliegues, y una vista de frente y de espaldas para revisarlas sin abrir
el juego (la piel se dibuja con el modelo de jugador de Minecraft, `PlayerModel` con brazos de 3).
"""
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import Lienzo, mezcla, rgba  # noqa: E402

# (u, v, ancho, alto, fondo) de cada caja en la textura; las capas de encima ("_f") son 0,5 mas grandes en el juego
CAJAS = {
    "cabeza": (0, 0, 8, 8, 8), "cabeza_f": (32, 0, 8, 8, 8),
    "cuerpo": (16, 16, 8, 12, 4), "cuerpo_f": (16, 32, 8, 12, 4),
    "brazo_d": (40, 16, 3, 12, 4), "brazo_d_f": (40, 32, 3, 12, 4),
    "brazo_i": (32, 48, 3, 12, 4), "brazo_i_f": (48, 48, 3, 12, 4),
    "pierna_d": (0, 16, 4, 12, 4), "pierna_d_f": (0, 32, 4, 12, 4),
    "pierna_i": (16, 48, 4, 12, 4), "pierna_i_f": (0, 48, 4, 12, 4),
}
CARAS = ("arriba", "abajo", "der", "frente", "izq", "espalda")


def caras(caja):
    """Las caras de una caja: {nombre: (x, y, ancho, alto)} (der/izq = costado derecho/izquierdo del personaje)."""
    u, v, w, h, d = CAJAS[caja]
    return {
        "arriba": (u + d, v, w, d),
        "abajo": (u + d + w, v, w, d),
        "der": (u, v + d, d, h),
        "frente": (u + d, v + d, w, h),
        "izq": (u + d + w, v + d, d, h),
        "espalda": (u + 2 * d + w, v + d, w, h),
    }


def pintar(L, caja, cara, f):
    """Pinta una cara llamando a f(x, y, ancho, alto) -> color (o None = transparente) para cada pixel."""
    x0, y0, w, h = caras(caja)[cara]
    for y in range(h):
        for x in range(w):
            c = f(x, y, w, h)
            if c is None:
                L.borrar(x0 + x, y0 + y)
            else:
                L.px(x0 + x, y0 + y, c)


def sprite(L, caja, cara, filas, paleta):
    """Pinta una cara con un sprite escrito como texto ('.' = transparente)."""
    x0, y0, w, h = caras(caja)[cara]
    for y in range(h):
        for x in range(w):
            ch = filas[y][x] if y < len(filas) and x < len(filas[y]) else "."
            if ch == ".":
                L.borrar(x0 + x, y0 + y)
            else:
                L.px(x0 + x, y0 + y, paleta[ch])


def tela(tonos, semilla, pliegue=3, luz_arriba=True, grano=0.10):
    """Una tela con pliegues verticales: f(x, y, w, h) que da un tono de la lista (de oscuro a claro)."""
    r = np.random.default_rng(semilla)
    ruido = r.random((64, 64))

    def f(x, y, w, h):
        n = len(tonos)
        base = n / 2.0
        if luz_arriba:
            base += 0.8 * (1 - y / max(1, h - 1)) - 0.4
        # pliegues: una raya oscura cada `pliegue` pixeles, con su lado claro
        k = (x + (y // 5) % 2) % pliegue
        if k == 0:
            base -= 1.0
        elif k == 1:
            base += 0.5
        base += (ruido[y % 64, x % 64] - 0.5) * grano * n
        return tonos[int(np.clip(round(base), 0, n - 1))]

    return f


# ---------------------------------------------------------------- vista previa

def _cara(L, caja, cara):
    x, y, w, h = caras(caja)[cara]
    return L.recorte(x, y, w, h)


def vista(L, escala=8, fondo="#2a2430"):
    """Frente y espalda del personaje de pie (con las capas de encima), para revisarlo."""
    W, H = 16 * 2 + 12, 32 + 4
    out = Lienzo(W, H, fondo)

    def figura(ox, frente):
        lado = "frente" if frente else "espalda"
        # de frente: el brazo derecho del personaje sale a la izquierda de la imagen
        piezas = [
            ("cabeza", 4, 0), ("cuerpo", 4, 8),
            ("brazo_d" if frente else "brazo_i", 1, 8), ("brazo_i" if frente else "brazo_d", 12, 8),
            ("pierna_d" if frente else "pierna_i", 4, 20), ("pierna_i" if frente else "pierna_d", 8, 20),
        ]
        for nombre, x, y in piezas:
            out.pegar(_cara(L, nombre, lado), ox + x, y + 2)
        for nombre, x, y in piezas:
            out.pegar(_cara(L, nombre + "_f", lado), ox + x, y + 2)

    figura(2, True)
    figura(24, False)
    return out.imagen().resize((W * escala, H * escala), 0)


__all__ = ["CAJAS", "caras", "pintar", "sprite", "tela", "vista", "Lienzo", "mezcla", "rgba"]
