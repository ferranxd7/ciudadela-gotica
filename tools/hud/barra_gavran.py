"""Barra de vida de Ser Gavran (pixel a pixel): marco de corteza retorcida con espinas, hojas y un arbolito con el
corazon verde en el centro; relleno verde que brilla (4 cuadros), hueco, rastro, barra de postura y hojas sueltas
(las que caen de la barra al recibir dano).

  python3 tools/hud/barra_gavran.py [carpeta_de_vistas]

Sale textures/gui/gavran_barra.png (256x128):
  v 0..39   marco (el relleno va en x 8..247, y 19..25, como la del Rey)
  v 48..79  relleno verde, 4 cuadros de 8 px (240 x 7)
  v 80      hueco (240 x 7)        v 88  rastro del ultimo dano (240 x 7)
  v 96      postura: relleno (240 x 3)   v 100  postura: fondo (240 x 3)
  v 104     hojas sueltas: 4 de 8 x 8 (u 0, 8, 16, 24)
"""
import math
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from barras import FW, FX0, FX1, FY0, FY1, OUT, GUI, gem, hexc, mix, noise2, put, rect  # noqa: E402

BARK_DD, BARK_D, BARK, BARK_L = hexc("1a120c"), hexc("2e2116"), hexc("4a3624"), hexc("6e5236")
ROOT_D, ROOT, ROOT_L = hexc("5a4230"), hexc("806046"), hexc("a8865e")
THORN, THORN_L = hexc("241a12"), hexc("8c7a5a")
LEAF_D, LEAF, LEAF_L = hexc("2c5a22"), hexc("4a8a34"), hexc("7cc25a")
GLOW_D, GLOW, GLOW_L, GLOW_H = hexc("1f6a32"), hexc("3fbf5a"), hexc("8cff9c"), hexc("e6ffe8")
AMBER_D, AMBER, AMBER_L = hexc("7a4a0a"), hexc("d89a2a"), hexc("ffd87a")


def corteza_px(n, x, y):
    v = n(x * 0.5, y * 0.12)
    if v < 0.28:
        return BARK_DD
    if v < 0.45:
        return BARK_D
    if v < 0.78:
        return BARK
    return BARK_L


def hoja(img, cx, cy, ang, largo=4):
    """Hojita de 'largo' px que sale de (cx, cy) en direccion ang (radianes)."""
    dx, dy = math.cos(ang), math.sin(ang)
    for i in range(largo):
        w = 1 if 0 < i < largo - 1 else 0
        x, y = cx + dx * i, cy + dy * i
        for k in range(-w, w + 1):
            put(img, int(round(x - dy * k)), int(round(y + dx * k)), LEAF if k == 0 else (LEAF_L if k < 0 else LEAF_D))
    put(img, int(round(cx + dx * (largo - 1))), int(round(cy + dy * (largo - 1))), LEAF_L)


def espina(img, x, y, alto, arriba=True):
    s = -1 if arriba else 1
    for i in range(alto):
        put(img, x, y + s * i, THORN if i < alto - 1 else THORN_L)
        if i == 0:
            put(img, x - 1, y, THORN)
            put(img, x + 1, y, THORN)


def frame():
    rnd = random.Random(7)
    n = noise2(11)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    # banda de corteza alrededor del relleno, con contorno negro
    rect(img, x0, y0, x1, y1, OUT)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            put(img, x, y, corteza_px(n, x, y))
    # raices retorcidas por arriba y por abajo (dos hebras que se cruzan)
    for x in range(x0 + 1, x1):
        for k, (yb, fase) in enumerate(((y0 + 1.5, 0.0), (y0 + 1.5, math.pi), (y1 - 1.5, 0.7), (y1 - 1.5, math.pi + 0.7))):
            y = yb + math.sin(x * 0.21 + fase) * 1.2
            c = ROOT_L if math.cos(x * 0.21 + fase) > 0.3 else (ROOT if math.cos(x * 0.21 + fase) > -0.4 else ROOT_D)
            put(img, x, int(round(y)), c)
    # filo oscuro pegado al relleno y hueco transparente
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, BARK_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # espinas hacia arriba y hacia abajo, separadas de forma irregular
    x = x0 + 4
    while x < x1 - 3:
        if abs(x - FW // 2) > 16:
            espina(img, x, y0, rnd.randint(2, 4), True)
            espina(img, x + rnd.randint(-1, 1), y1, rnd.randint(2, 3), False)
        x += rnd.randint(7, 12)
    # hojitas que brotan del marco
    for _ in range(16):
        xx = rnd.randint(x0 + 6, x1 - 6)
        if abs(xx - FW // 2) < 18:
            continue
        if rnd.random() < 0.5:
            hoja(img, xx, y0, -math.pi / 2 + rnd.uniform(-0.9, 0.9), rnd.randint(3, 4))
        else:
            hoja(img, xx, y1, math.pi / 2 + rnd.uniform(-0.9, 0.9), rnd.randint(3, 4))
    # nudos de raiz en los extremos, con un ojo verde
    for side in (0, 1):
        cx = 4 if side == 0 else FW - 5
        cy = (FY0 + FY1) // 2
        for dy in range(-7, 8):
            for dx in range(-5, 6):
                d = math.hypot(dx * 1.1, dy * 0.9)
                if d <= 6.2:
                    put(img, cx + dx, cy + dy, OUT if d > 5.2 else corteza_px(n, cx + dx * 2, cy + dy))
        for a in range(0, 360, 20):
            r = 4.2 - a / 360 * 3
            put(img, cx + int(round(math.cos(math.radians(a)) * r)), cy + int(round(math.sin(math.radians(a)) * r)), ROOT_L)
        gem(img, cx, cy, 1, GLOW_D, GLOW, GLOW_L)
    # nudo de espinas a media barra: ahi empieza la segunda fase
    xs = FX0 + (FX1 - FX0) // 2
    rect(img, xs - 2, FY0 - 6, xs + 2, FY1 + 5, OUT)
    for y in range(FY0 - 5, FY1 + 5):
        for xx in (-1, 0, 1):
            put(img, xs + xx, y, (ROOT_L, ROOT, ROOT_D)[xx + 1])
    espina(img, xs, FY0 - 6, 3, True)
    espina(img, xs, FY1 + 5, 3, False)
    gem(img, xs, (FY0 + FY1) // 2 - 1, 2, GLOW_D, GLOW, GLOW_L)
    # en el centro: el corazon del arbol (gema verde) en un nudo de raices, con dos ramitas con hojas
    cx, cy = FW // 2, FY0 - 6
    for dy in range(-7, 8):
        for dx in range(-7, 8):
            d = math.hypot(dx, dy * 1.05)
            if d <= 6.6:
                put(img, cx + dx, cy + dy, OUT if d > 5.6 else corteza_px(n, cx + dx * 2, cy + dy * 2))
    for a in range(0, 540, 15):
        r = 5.0 - a / 540 * 3.2
        put(img, cx + int(round(math.cos(math.radians(a)) * r)), cy + int(round(math.sin(math.radians(a)) * r)), ROOT_L)
    gem(img, cx, cy, 2, GLOW_D, GLOW, GLOW_L)
    put(img, cx - 1, cy - 1, GLOW_H)
    for sgn in (-1, 1):
        px, py = cx + sgn * 5, cy - 3
        for i in range(10):
            x = px + sgn * i
            y = py - int(round(math.sin(i / 9 * math.pi) * 3.2)) - (i // 4)
            put(img, x, y - 1, OUT)
            put(img, x, y + 1, OUT)
            put(img, x, y, ROOT_L if i % 3 else ROOT)
            if i in (3, 6, 9):
                hoja(img, x, y - 1, -math.pi / 2 - sgn * 0.5, 4)
        espina(img, cx + sgn * 16, FY0 - 4, 4, True)
    return img


def relleno(k):
    """Relleno verde que brilla; la savia corre hacia la derecha (4 cuadros)."""
    n = noise2(23)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            vena = n((x - k * 6) * 0.12, y * 0.7)
            t = 0.25 + 0.55 * mid + 0.35 * (vena - 0.5)
            c = mix(GLOW_D, GLOW, max(0.0, min(1.0, t * 1.4)))
            if t > 0.72:
                c = mix(c, GLOW_L, min(1.0, (t - 0.72) * 3))
            if y == 0:
                c = mix(c, GLOW_L, 0.5)
            if y == 6:
                c = mix(c, GLOW_D, 0.6)
            # destellos que avanzan
            if (x + 3 * y - k * 15) % 60 == 0 and 1 <= y <= 5:
                c = GLOW_H
            img.putpixel((x, y), c)
    return img


def hueco():
    n = noise2(31)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            v = n(x * 0.3, y * 0.5)
            img.putpixel((x, y), mix(hexc("0c0906"), hexc("1e1610"), v))
    return img


def rastro():
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            img.putpixel((x, y), mix(hexc("e8f6b8"), hexc("b8d890"), abs(y - 3) / 3))
    return img


def postura(fondo=False):
    img = Image.new("RGBA", (240, 3))
    for x in range(240):
        for y in range(3):
            if fondo:
                c = hexc("140e08") if y != 1 else hexc("221810")
            else:
                c = (AMBER_L, AMBER, AMBER_D)[y]
            img.putpixel((x, y), c)
    return img


def hojas():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    for k in range(4):
        ang = -math.pi / 4 + k * 0.5
        hoja(img, k * 8 + 1, 6, ang, 6 + (k % 2))
    return img


def barra_gavran():
    atlas = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    atlas.paste(frame(), (0, 0))
    for k in range(4):
        atlas.paste(relleno(k), (0, 48 + 8 * k))
    atlas.paste(hueco(), (0, 80))
    atlas.paste(rastro(), (0, 88))
    atlas.paste(postura(), (0, 96))
    atlas.paste(postura(True), (0, 100))
    atlas.paste(hojas(), (0, 104))
    return atlas


if __name__ == "__main__":
    os.makedirs(GUI, exist_ok=True)
    b = barra_gavran()
    b.save(os.path.join(GUI, "gavran_barra.png"))
    if len(sys.argv) > 1:
        d = sys.argv[1]
        os.makedirs(d, exist_ok=True)
        b.resize((256 * 4, 128 * 4), Image.NEAREST).save(os.path.join(d, "gavran_barra_x4.png"))
    print("ok")
