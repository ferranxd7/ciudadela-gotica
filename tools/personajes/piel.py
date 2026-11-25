#!/usr/bin/env python3
"""
Utiles para pintar las pieles de los personajes del mod (como las de los jugadores: 64 x 64, o 128 x 64 si llevan piezas
de mas, como la mochila del mercader, que van en la mitad derecha): donde cae cada cara de cada caja en la textura,
telas con pliegues, y una vista de frente y de espaldas para revisarlas sin abrir el juego. Se dibujan con el modelo de
jugador de Minecraft (`PlayerModel`, brazos de 3 o de 4).
"""
import os
import sys

import numpy as np

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "ui"))
from arte import Lienzo, mezcla, rgba  # noqa: E402


def cajas(brazos=3):
    """(u, v, ancho, alto, fondo) de cada caja; las capas de encima ("_f") son 0,5 mas grandes en el juego."""
    return {
        "cabeza": (0, 0, 8, 8, 8), "cabeza_f": (32, 0, 8, 8, 8),
        "cuerpo": (16, 16, 8, 12, 4), "cuerpo_f": (16, 32, 8, 12, 4),
        "brazo_d": (40, 16, brazos, 12, 4), "brazo_d_f": (40, 32, brazos, 12, 4),
        "brazo_i": (32, 48, brazos, 12, 4), "brazo_i_f": (48, 48, brazos, 12, 4),
        "pierna_d": (0, 16, 4, 12, 4), "pierna_d_f": (0, 32, 4, 12, 4),
        "pierna_i": (16, 48, 4, 12, 4), "pierna_i_f": (0, 48, 4, 12, 4),
    }


CAJAS = cajas(3)
CARAS = ("arriba", "abajo", "der", "frente", "izq", "espalda")


def caras_de(u, v, w, h, d):
    """Las caras de una caja: {nombre: (x, y, ancho, alto)} (der/izq = costado derecho/izquierdo del personaje)."""
    return {
        "arriba": (u + d, v, w, d),
        "abajo": (u + d + w, v, w, d),
        "der": (u, v + d, d, h),
        "frente": (u + d, v + d, w, h),
        "izq": (u + d + w, v + d, d, h),
        "espalda": (u + 2 * d + w, v + d, w, h),
    }


class Piel:
    """Una piel: el lienzo y sus cajas (las del jugador y las que se anadan)."""

    def __init__(self, ancho=64, alto=64, brazos=3):
        self.L = Lienzo(ancho, alto)
        self.cajas = cajas(brazos)
        self.brazos = brazos

    def caja(self, nombre, u, v, w, h, d):
        self.cajas[nombre] = (u, v, w, h, d)

    def caras(self, caja):
        return caras_de(*self.cajas[caja])

    def pintar(self, caja, cara, f):
        """Pinta una cara llamando a f(x, y, ancho, alto) -> color (o None = transparente) para cada pixel."""
        x0, y0, w, h = self.caras(caja)[cara]
        for y in range(h):
            for x in range(w):
                c = f(x, y, w, h)
                if c is None:
                    self.L.borrar(x0 + x, y0 + y)
                else:
                    self.L.px(x0 + x, y0 + y, c)

    def todas(self, caja, f, caras=CARAS):
        for c in caras:
            self.pintar(caja, c, f)

    def sprite(self, caja, cara, filas, paleta):
        """Pinta una cara con un sprite escrito como texto ('.' = transparente)."""
        x0, y0, w, h = self.caras(caja)[cara]
        for y in range(h):
            for x in range(w):
                ch = filas[y][x] if y < len(filas) and x < len(filas[y]) else "."
                if ch == ".":
                    self.L.borrar(x0 + x, y0 + y)
                else:
                    self.L.px(x0 + x, y0 + y, paleta[ch])

    def recorte(self, caja, cara):
        x, y, w, h = self.caras(caja)[cara]
        return self.L.recorte(x, y, w, h)

    def guardar(self, ruta):
        self.L.guardar(ruta)

    def vista(self, escala=8, fondo="#2a2430", extra=None):
        """Frente y espalda del personaje de pie (con las capas de encima), para revisarlo. extra(out, ox, frente)."""
        b = self.brazos
        W, H = 16 * 2 + 14, 32 + 4
        out = Lienzo(W, H, fondo)

        def figura(ox, frente):
            lado = "frente" if frente else "espalda"
            piezas = [
                ("cabeza", 4, 0), ("cuerpo", 4, 8),
                ("brazo_d" if frente else "brazo_i", 4 - b, 8), ("brazo_i" if frente else "brazo_d", 12, 8),
                ("pierna_d" if frente else "pierna_i", 4, 20), ("pierna_i" if frente else "pierna_d", 8, 20),
            ]
            for nombre, x, y in piezas:
                out.pegar(self.recorte(nombre, lado), ox + x, y + 2)
            for nombre, x, y in piezas:
                out.pegar(self.recorte(nombre + "_f", lado), ox + x, y + 2)
            if extra:
                extra(out, ox, frente)

        figura(3, True)
        figura(26, False)
        return out.imagen().resize((W * escala, H * escala), 0)


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


def ruido(tonos, semilla, grano=0.35, base=None):
    """Un color con grano (piel, cuero): f(x, y, w, h)."""
    r = np.random.default_rng(semilla)
    rr = r.random((64, 64))

    def f(x, y, w, h):
        n = len(tonos)
        b = (n - 1) / 2.0 if base is None else base
        return tonos[int(np.clip(round(b + (rr[y % 64, x % 64] - 0.5) * grano * n), 0, n - 1))]

    return f


# ---------------------------------------------------------------- compatibilidad con la guia (piel de 64 x 64, brazos de 3)

_GUIA = None


def _piel_guia(L):
    global _GUIA
    if _GUIA is None or _GUIA.L is not L:
        _GUIA = Piel()
        _GUIA.L = L
    return _GUIA


def caras(caja):
    return caras_de(*CAJAS[caja])


def pintar(L, caja, cara, f):
    _piel_guia(L).pintar(caja, cara, f)


def sprite(L, caja, cara, filas, paleta):
    _piel_guia(L).sprite(caja, cara, filas, paleta)


def vista(L, escala=8, fondo="#2a2430"):
    return _piel_guia(L).vista(escala, fondo)


__all__ = ["Piel", "CAJAS", "cajas", "caras", "caras_de", "pintar", "sprite", "tela", "ruido", "vista", "Lienzo", "mezcla", "rgba"]
