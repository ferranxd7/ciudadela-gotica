#!/usr/bin/env python3
"""
Los martillos (parte 7aj): textures/item/maza.png, martillo_guerra.png y gran_martillo.png (32 x 32); y el Pico del Capataz
(parte 7ar, pico_capataz.png). Dibujados en
diagonal como las armas del juego (el mango abajo a la izquierda, la cabeza arriba a la derecha): cada pixel se mira en
las coordenadas del arma (u a lo largo del mango, desde el pomo; v de lado) y se le da el material de lo que cae ahi;
luego el contorno de tinta. La luz viene de arriba a la izquierda (v negativa, mas clara).

    python3 tools/rol/martillos.py [--vista carpeta]
"""
import math
import os
import sys

from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
ITEM = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/item")

TINTA = (22, 18, 24, 255)
ACERO = [(46, 50, 58), (74, 80, 90), (110, 116, 128), (150, 156, 168), (196, 200, 210), (232, 234, 240)]
HIERRO = [(34, 34, 38), (54, 54, 60), (80, 80, 88), (108, 108, 116), (140, 140, 148)]
MADERA = [(44, 28, 16), (66, 43, 24), (92, 62, 34), (120, 84, 46), (146, 106, 60)]
CUERO = [(36, 22, 14), (58, 36, 22), (86, 56, 34), (110, 74, 44)]
LATON = [(92, 66, 22), (140, 104, 38), (188, 148, 60), (224, 190, 98)]
PIEDRA = [(48, 46, 50), (70, 68, 72), (96, 93, 98), (124, 120, 126), (152, 148, 152)]
RUNA = (196, 62, 44)


def tono(lista, v):
    v = max(0.0, min(0.999, v))
    return lista[int(v * len(lista))] + (255,)


def dibujar(material, angulo=45.0, origen=(4.5, 27.5)):
    """material(u, v) -> color o None, en las coordenadas del arma (u desde el pomo hacia la cabeza, v de lado)."""
    im = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    a = math.radians(angulo)
    du = (math.cos(a), -math.sin(a))        # hacia arriba a la derecha
    dv = (math.sin(a), math.cos(a))         # perpendicular (hacia abajo a la derecha)
    for y in range(32):
        for x in range(32):
            px, py = x + 0.5 - origen[0], y + 0.5 - origen[1]
            u = px * du[0] + py * du[1]
            v = px * dv[0] + py * dv[1]
            c = material(u, v)
            if c is not None:
                im.putpixel((x, y), c)
    # contorno de tinta alrededor de lo pintado
    op = [[im.getpixel((x, y))[3] > 0 for x in range(32)] for y in range(32)]
    for y in range(32):
        for x in range(32):
            if op[y][x]:
                continue
            if any(0 <= x + dx < 32 and 0 <= y + dy < 32 and op[y + dy][x + dx] for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                im.putpixel((x, y), TINTA)
    return im


def mango(u, v, largo, ancho=0.9, cuero_desde=None, cuero_hasta=None):
    """El mango de madera (con la empunadura de cuero entre dos u)."""
    if 0 <= u <= largo and abs(v) <= ancho:
        luz = 0.62 - v * 0.35
        if cuero_desde is not None and cuero_desde <= u <= cuero_hasta:
            return tono(CUERO, luz - (0.18 if int(u * 1.6) % 2 == 0 else 0.0))
        return tono(MADERA, luz + (0.08 if int(u * 2.2) % 3 == 0 else 0.0))
    return None


def maza(u, v):
    """La maza de clerigo: cabeza de acero con seis aletas, cuello de laton, mango de madera con cuero, pomo."""
    # la cabeza: un nucleo y las aletas
    if 21.5 <= u <= 29.5:
        nucleo = abs(v) <= 2.0
        aleta = abs(v) <= 4.0 and (int(u - 21.5) % 3 != 2)
        if nucleo or aleta:
            luz = 0.62 - v * 0.12 + (0.18 if abs(v) > 2.6 else 0.0)
            if abs(v) > 3.4:
                luz -= 0.25
            return tono(ACERO, luz)
    if 29.5 < u <= 31.3 and abs(v) <= 1.2:
        return tono(ACERO, 0.75 - v * 0.15)               # la punta
    if 19.8 <= u < 21.5 and abs(v) <= 1.6:
        return tono(LATON, 0.65 - v * 0.15)               # el cuello
    if -1.6 <= u < 0.4 and abs(v) <= 1.5:
        return tono(LATON, 0.6 - v * 0.15)                # el pomo
    return mango(u, v, 20, 0.95, 1.0, 7.5)


def martillo_guerra(u, v):
    """El martillo de guerra: cabeza cuadrada a un lado y pico al otro, sobre un mango largo con anillas de hierro."""
    if 22.5 <= u <= 27.5:
        # la cabeza atraviesa el mango: martillo hacia v negativa, pico hacia v positiva
        if -6.0 <= v <= -1.0:
            luz = 0.66 + (0.18 if u < 23.6 else 0.0) - (0.25 if v < -5.0 else 0.0)
            return tono(ACERO, luz)
        if -1.0 < v <= 1.0:
            return tono(ACERO, 0.5)
        if 1.0 < v <= 6.5 - abs(u - 25.0) * 0.9:
            return tono(ACERO, 0.55 - (v - 1) * 0.07)
    if 27.5 < u <= 30.0 and abs(v) <= 1.0:
        return tono(ACERO, 0.7)                            # la punta de arriba
    if u in (8.0,) or (8.0 <= u <= 9.0 and abs(v) <= 1.25) or (15.0 <= u <= 16.0 and abs(v) <= 1.25):
        return tono(HIERRO, 0.7 - v * 0.2)                # las anillas
    if -1.4 <= u < 0.4 and abs(v) <= 1.4:
        return tono(HIERRO, 0.6 - v * 0.2)                # el pomo
    return mango(u, v, 22.5, 0.95, 0.8, 6.5)


def gran_martillo(u, v):
    """El gran martillo de piedra: un bloque tallado con bandas de hierro y una runa, sobre un mango grueso."""
    if 19.0 <= u <= 29.5 and abs(v) <= 6.0:
        borde = abs(v) > 5.0 or u < 20.0 or u > 28.5
        if 22.0 <= u <= 22.9 or 25.6 <= u <= 26.5:
            return tono(HIERRO, 0.62 - v * 0.03)          # las bandas de hierro
        if abs(v) <= 1.0 and 23.4 <= u <= 25.2:
            return RUNA + (255,)                          # la runa roja
        luz = 0.58 - v * 0.05 + (0.15 if u > 28.5 else 0.0) - (0.2 if borde and v > 0 else 0.0)
        ruido = ((int(u * 3.1) * 7 + int(v * 2.3) * 13) % 9) / 40.0
        return tono(PIEDRA, luz + ruido - 0.1)
    if 29.5 < u <= 30.5 and abs(v) <= 4.5:
        return tono(PIEDRA, 0.72)
    if -1.8 <= u < 0.6 and abs(v) <= 1.8:
        return tono(HIERRO, 0.55 - v * 0.2)
    return mango(u, v, 19.0, 1.35, 1.0, 9.0)


NEGRO = [(16, 15, 18), (28, 27, 32), (44, 42, 50), (64, 62, 72), (92, 90, 102)]
VETA = [(96, 16, 12), (150, 30, 18), (204, 54, 30), (240, 110, 64), (255, 182, 130)]


def pico_capataz(u, v):
    """El Pico del Capataz (parte 7ar): un pico doble de hierro negro, curvado hacia el mango en las puntas, con la veta roja
    incrustada a lo largo; el ojo de hierro donde entra el mango, el mango largo con aros de hierro, el puno de cuero y el regaton."""
    av = abs(v)
    if av <= 10.0:
        centro = 24.2 - 0.045 * v * v                     # las puntas se curvan hacia el pomo
        grueso = 2.4 - 0.2 * av                            # y se afinan
        if av <= 1.7 and 21.6 <= u <= 26.8:
            return tono(HIERRO, 0.62 - v * 0.08 + (0.15 if u > 26.0 else 0.0))   # el ojo
        if grueso > 0.35 and abs(u - centro) <= grueso:
            if 2.2 <= av <= 8.2 and abs(u - centro) <= 0.55:
                return tono(VETA, 0.55 + 0.35 * (1 - av / 8.2) + (0.1 if int(av * 2) % 3 == 0 else 0.0))   # la veta incrustada
            luz = 0.5 - (u - centro) * 0.12 - v * 0.02 + (0.22 if u - centro > grueso - 0.8 else 0.0)
            return tono(NEGRO, luz)
    if 26.8 < u <= 28.2 and abs(v) <= 0.9:
        return tono(HIERRO, 0.7)                           # la punta del ojo
    if (9.0 <= u <= 9.9 or 15.5 <= u <= 16.4) and abs(v) <= 1.2:
        return tono(HIERRO, 0.66 - v * 0.2)               # los aros del mango
    if -1.6 <= u < 0.5 and abs(v) <= 1.3:
        return tono(HIERRO, 0.55 - v * 0.2)               # el regaton
    return mango(u, v, 21.6, 1.0, 0.6, 6.8)


ARMAS = {"maza": (maza, 45.0, (4.5, 27.5)), "martillo_guerra": (martillo_guerra, 45.0, (4.0, 28.0)),
         "gran_martillo": (gran_martillo, 45.0, (4.3, 27.7)), "pico_capataz": (pico_capataz, 45.0, (4.4, 27.6))}


def main():
    vista = sys.argv[sys.argv.index("--vista") + 1] if "--vista" in sys.argv else None
    hoja = Image.new("RGBA", (len(ARMAS) * 136, 136), (44, 40, 48, 255))
    for i, (nombre, (f, ang, org)) in enumerate(ARMAS.items()):
        im = dibujar(f, ang, org)
        im.save(os.path.join(ITEM, nombre + ".png"))
        hoja.paste(im.resize((128, 128), Image.NEAREST), (4 + i * 136, 4), im.resize((128, 128), Image.NEAREST))
    if vista:
        os.makedirs(vista, exist_ok=True)
        hoja.save(os.path.join(vista, "martillos.png"))
    print("martillos:", ", ".join(ARMAS))


if __name__ == "__main__":
    main()
