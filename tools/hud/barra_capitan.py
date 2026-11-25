"""Barra de vida del Capitan de Brumavela (pixel a pixel): un liston de cubierta gastado, con dos cabos trenzados por
arriba y por abajo, herrajes en los extremos (dos unas de ancla), percebes, y en el centro su farol de animas colgando
de una argolla; relleno de bruma verdemar que corre (4 cuadros), hueco, rastro y las gotas que caen de la barra al
recibir dano.

  python3 tools/hud/barra_capitan.py [carpeta_de_vistas]

Sale textures/gui/capitan_barra.png (256x128):
  v 0..39   marco (el relleno va en x 8..247, y 19..25, como las demas)
  v 48..79  relleno, 4 cuadros de 8 px (240 x 7)
  v 80      hueco (240 x 7)        v 88  rastro del ultimo dano (240 x 7)
  v 104     gotas sueltas: 4 de 8 x 8 (u 0, 8, 16, 24)
"""
import math
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from barras import FW, FX0, FX1, FY0, FY1, OUT, GUI, gem, hexc, mix, noise2, put, rect  # noqa: E402

TABLA_DD, TABLA_D, TABLA, TABLA_L = hexc("14100c"), hexc("261d15"), hexc("3a2c20"), hexc("54402e")
CABO_D, CABO, CABO_L = hexc("5b4c33"), hexc("86724d"), hexc("b39c6d")
HIERRO_D, HIERRO, HIERRO_L = hexc("1c2023"), hexc("3a4247"), hexc("6b777d")
ORO_D, ORO, ORO_L = hexc("5c491b"), hexc("a58736"), hexc("d9bd62")
PERCEBE, PERCEBE_D = hexc("a9a895"), hexc("6f6e60")
ALGA_D, ALGA = hexc("1d3826"), hexc("386540")
MAR_DD, MAR_D, MAR, MAR_L, MAR_H = hexc("0f3b3c"), hexc("2c8f8a"), hexc("49c7bb"), hexc("9af0e4"), hexc("e8fffb")


def tabla_px(n, x, y):
    v = n(x * 0.16, y * 0.9) + (0.10 if y % 3 == 0 else 0.0)
    if x % 31 == 0:
        return TABLA_DD                       # la junta entre dos tablas
    if v < 0.30:
        return TABLA_DD
    if v < 0.52:
        return TABLA_D
    if v < 0.84:
        return TABLA
    return TABLA_L


def cabo(img, x0, x1, y):
    """Un cabo trenzado de dos pixeles de alto: las hebras suben en diagonal."""
    for x in range(x0, x1 + 1):
        k = x % 4
        put(img, x, y, (CABO_L, CABO, CABO_D, CABO)[k])
        put(img, x, y + 1, (CABO, CABO_D, CABO, CABO_L)[k])
        put(img, x, y - 1, OUT)
        put(img, x, y + 2, OUT)


def frame():
    rnd = random.Random(19)
    n = noise2(41)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    # el liston de cubierta alrededor del relleno, con contorno negro
    rect(img, x0, y0, x1, y1, OUT)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            put(img, x, y, tabla_px(n, x, y))
    # los dos cabos, por arriba y por abajo
    cabo(img, x0 + 1, x1 - 1, y0 + 1)
    cabo(img, x0 + 1, x1 - 1, y1 - 2)
    # filo oscuro pegado al relleno y hueco transparente
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, TABLA_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # clavos de hierro en el liston y percebes pegados por fuera
    for x in range(x0 + 9, x1 - 6, 31):
        if abs(x - FW // 2) > 14:
            put(img, x + 4, y0 + 4 - 1, HIERRO_L)
            put(img, x + 4, y1 - 4 + 1, HIERRO_L)
    for _ in range(26):
        xx = rnd.randint(x0 + 8, x1 - 8)
        if abs(xx - FW // 2) < 20:
            continue
        arriba = rnd.random() < 0.45
        yy = y0 - 1 if arriba else y1 + 1
        put(img, xx, yy, PERCEBE)
        put(img, xx + 1, yy, PERCEBE_D)
        if rnd.random() < 0.5:
            put(img, xx, yy + (-1 if arriba else 1), PERCEBE_D)
    # algas que cuelgan por debajo
    for _ in range(11):
        xx = rnd.randint(x0 + 10, x1 - 10)
        largo = rnd.randint(3, 6)
        for i in range(largo):
            put(img, xx + (1 if (i // 2) % 2 else 0), y1 + 1 + i, ALGA if i < largo - 1 else ALGA_D)
    # herrajes de los extremos: dos unas de ancla
    for side in (0, 1):
        s = 1 if side == 0 else -1
        cx = 5 if side == 0 else FW - 6
        cy = (FY0 + FY1) // 2
        for dy in range(-9, 10):
            for dx in range(-5, 6):
                # la cana (vertical) y los brazos curvos hacia fuera
                cana = abs(dx) <= 1 and abs(dy) <= 8
                brazo = 4.2 <= math.hypot(dx * 1.0 + s * 0.0, (dy - 2) * 0.75) <= 6.0 and dy >= 2
                if cana or brazo:
                    for ox, oy in ((0, 0), (1, 0), (-1, 0), (0, 1), (0, -1)):
                        if img.getpixel((min(FW - 1, max(0, cx + dx + ox)), min(39, max(0, cy + dy + oy))))[3] == 0:
                            put(img, cx + dx + ox, cy + dy + oy, OUT)
        for dy in range(-9, 10):
            for dx in range(-5, 6):
                cana = abs(dx) <= 1 and abs(dy) <= 8
                brazo = 4.2 <= math.hypot(dx * 1.0, (dy - 2) * 0.75) <= 6.0 and dy >= 2
                if cana or brazo:
                    put(img, cx + dx, cy + dy, HIERRO_L if dx < 0 else (HIERRO_D if dx > 0 else HIERRO))
        gem(img, cx, cy - 6, 1, MAR_DD, MAR, MAR_L)
    # a media barra, una argolla de hierro: ahi llama a su tripulacion
    xs = FX0 + (FX1 - FX0) // 2
    # (queda debajo del farol: solo se ve por abajo)
    rect(img, xs - 2, FY1 + 1, xs + 2, FY1 + 6, OUT)
    for y in range(FY1 + 2, FY1 + 6):
        for xx in (-1, 0, 1):
            put(img, xs + xx, y, (HIERRO_L, HIERRO, HIERRO_D)[xx + 1])
    put(img, xs, FY1 + 6, OUT)
    # en el centro: el farol de animas, colgando de su argolla
    cx, top = FW // 2, 1
    # la argolla
    for dy in range(0, 5):
        for dx in range(-2, 3):
            d = math.hypot(dx, dy - 2)
            if 1.2 <= d <= 2.4:
                put(img, cx + dx, top + dy, HIERRO_L if dx <= 0 else HIERRO)
    # la tapa
    rect(img, cx - 5, top + 4, cx + 5, top + 7, OUT)
    rect(img, cx - 4, top + 5, cx + 4, top + 6, HIERRO)
    for x in range(cx - 4, cx + 5):
        put(img, x, top + 5, HIERRO_L)
    # la jaula y la llama
    rect(img, cx - 6, top + 7, cx + 6, top + 17, OUT)
    rect(img, cx - 5, top + 8, cx + 5, top + 16, MAR_DD)
    for dy in range(8, 17):
        for dx in range(-4, 5):
            d = math.hypot(dx * 1.0, (dy - 12.5) * 0.85)
            if d < 1.6:
                c = MAR_H
            elif d < 2.8:
                c = MAR_L
            elif d < 4.0:
                c = MAR
            else:
                c = MAR_D
            put(img, cx + dx, top + dy, c)
    for dx in (-5, -2, 2, 5):
        for dy in range(8, 17):
            put(img, cx + dx, top + dy, HIERRO if abs(dx) == 5 else HIERRO_D)
    # el pie
    rect(img, cx - 5, top + 17, cx + 5, top + 19, OUT)
    rect(img, cx - 4, top + 17, cx + 4, top + 18, HIERRO)
    for x in range(cx - 4, cx + 5):
        put(img, x, top + 18, HIERRO_D)
    # dos remaches de oro gastado a cada lado del farol, sobre el cabo
    for sgn in (-1, 1):
        for i in (10, 16):
            put(img, cx + sgn * i, y0 + 1, ORO_L)
            put(img, cx + sgn * i, y0 + 2, ORO_D)
    return img


def relleno(k):
    """Bruma verdemar que corre hacia la derecha (4 cuadros), mas clara en el centro."""
    n = noise2(53)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            jiron = n((x - k * 7) * 0.09, y * 0.55)
            t = 0.22 + 0.5 * mid + 0.5 * (jiron - 0.5)
            c = mix(MAR_D, MAR, max(0.0, min(1.0, t * 1.5)))
            if t > 0.66:
                c = mix(c, MAR_L, min(1.0, (t - 0.66) * 3.2))
            if y == 0:
                c = mix(c, MAR_L, 0.45)
            if y == 6:
                c = mix(c, MAR_DD, 0.6)
            if (x * 2 + 5 * y - k * 19) % 76 == 0 and 1 <= y <= 5:
                c = MAR_H
            img.putpixel((x, y), c)
    return img


def hueco():
    n = noise2(61)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            v = n(x * 0.3, y * 0.5)
            img.putpixel((x, y), mix(hexc("05090b"), hexc("101a1d"), v))
    return img


def rastro():
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            img.putpixel((x, y), mix(hexc("f4fffd"), hexc("b9e8e0"), abs(y - 3) / 3))
    return img


def gotas():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (
        ["..h.", ".hL.", "hLLm", "hLmm", ".mm."],
        [".h.", "hLm", "hmm", ".m."],
        ["h.", "Lm", "m."],
        [".hh.", "hLLm", ".mm."],
    )
    col = {"h": MAR_H, "L": MAR_L, "m": MAR}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 1 + y, col[ch])
    return img


def barra_capitan():
    atlas = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    atlas.paste(frame(), (0, 0))
    for k in range(4):
        atlas.paste(relleno(k), (0, 48 + 8 * k))
    atlas.paste(hueco(), (0, 80))
    atlas.paste(rastro(), (0, 88))
    atlas.paste(gotas(), (0, 104))
    return atlas


if __name__ == "__main__":
    os.makedirs(GUI, exist_ok=True)
    b = barra_capitan()
    b.save(os.path.join(GUI, "capitan_barra.png"))
    if len(sys.argv) > 1:
        d = sys.argv[1]
        os.makedirs(d, exist_ok=True)
        # una vista con el relleno puesto al 62 %, sobre un fondo de juego
        v = Image.new("RGBA", (256, 48), (58, 66, 76, 255))
        v.paste(b.crop((0, 80, 240, 87)), (8, 19 + 4))
        v.paste(b.crop((0, 88, 170, 95)), (8, 19 + 4))
        v.paste(b.crop((0, 48, 149, 55)), (8, 19 + 4))
        marco = b.crop((0, 0, 256, 40))
        v.alpha_composite(marco, (0, 4))
        v.resize((256 * 5, 48 * 5), Image.NEAREST).save(os.path.join(d, "capitan_barra_vista.png"))
        b.resize((256 * 4, 128 * 4), Image.NEAREST).save(os.path.join(d, "capitan_barra_x4.png"))
    print("ok")
