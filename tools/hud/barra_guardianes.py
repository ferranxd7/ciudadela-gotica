"""Barras de vida de los dos guardianes de Valdrenn (parte 4d), pixel a pixel, con la misma disposicion que la del
Capitan (la dibuja client/enemigos/GuardianCliente):

  Ser Brandt, el Muro del Rey: un marco de hierro negro remachado con filos rojos, el paves con la corona del Rey en el
  centro y dos puas en los extremos; relleno de sangre oscura que corre; chispas de hierro al recibir dano.
  Vareth, el Guardian de Corteza: un marco de corteza con musgo y raices que cuelgan, el corazon verde latiendo en el
  centro entre dos ramas; relleno de savia verde que corre; hojas que caen al recibir dano.

  python3 tools/hud/barra_guardianes.py [carpeta_de_vistas]

Salen textures/gui/brandt_barra.png y corteza_barra.png (256x128):
  v 0..39   marco (el relleno va en x 8..247, y 19..25)
  v 48..79  relleno, 4 cuadros de 8 px (240 x 7)
  v 80      hueco (240 x 7)        v 88  rastro del ultimo dano (240 x 7)
  v 104     lo que cae: 4 de 8 x 8 (u 0, 8, 16, 24)
"""
import math
import os
import random
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from barras import FW, FX0, FX1, FY0, FY1, OUT, GUI, gem, hexc, mix, noise2, put, rect  # noqa: E402

# ---------------------------------------------------------------- Ser Brandt

H_DD, H_D, H, H_L = hexc("0d0e10"), hexc("1d1f23"), hexc("34373d"), hexc("5d626b")
R_DD, R_D, R, R_L, R_H = hexc("2a0505"), hexc("5c0d0e"), hexc("8e1b1d"), hexc("c93a33"), hexc("ffb4a0")


def marco_brandt():
    n = noise2(71)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.2, y * 0.6)
            put(img, x, y, H_D if v < 0.4 else H if v < 0.85 else H_L)
    # filos rojos arriba y abajo
    for x in range(x0 + 1, x1):
        put(img, x, y0 + 1, R_D if x % 2 else R)
        put(img, x, y1 - 1, R_DD if x % 2 else R_D)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, H_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # remaches
    for x in range(x0 + 7, x1 - 4, 12):
        if abs(x - FW // 2) > 16:
            put(img, x, y0 + 2, H_L)
            put(img, x, y1 - 2, H_L)
    # puas en los extremos
    for side in (0, 1):
        s = -1 if side == 0 else 1
        bx = x0 - 1 if side == 0 else x1 + 1
        cy = (FY0 + FY1) // 2
        for i in range(7):
            for dy in range(-(6 - i) // 2, (6 - i) // 2 + 1):
                put(img, bx + s * i, cy + dy, OUT)
        for i in range(6):
            for dy in range(-(4 - i) // 2, (4 - i) // 2 + 1):
                put(img, bx + s * i, cy + dy, H_L if dy < 0 else H)
    # en el centro: el paves con la corona (acaba en punta justo encima del relleno)
    cx, top = FW // 2, 0
    for y in range(top, top + 18):
        w = 7 if y < top + 11 else 7 - (y - top - 10)
        if w < 0:
            continue
        for x in range(cx - w - 1, cx + w + 2):
            put(img, x, y, OUT if abs(x - cx) > w or y == top else (H_L if abs(x - cx) == w else H))
    corona = ["R.R.R", "RRRRR", "RRRRR", ".RRR."]
    for j, fila in enumerate(corona):
        for i, ch in enumerate(fila):
            if ch == "R":
                put(img, cx - 2 + i, top + 3 + j, R_L if j == 0 else R)
    for x in range(cx - 3, cx + 4):
        put(img, x, top + 9, R_D)
    return img


def relleno_brandt(k):
    n = noise2(83)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 6) * 0.08, y * 0.5)
            t = 0.2 + 0.55 * mid + 0.45 * (v - 0.5)
            c = mix(R_D, R, max(0.0, min(1.0, t * 1.4)))
            if t > 0.7:
                c = mix(c, R_L, min(1.0, (t - 0.7) * 3))
            if y == 0:
                c = mix(c, R_L, 0.4)
            if y == 6:
                c = mix(c, R_DD, 0.6)
            img.putpixel((x, y), c)
    return img


def chispas():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["..h", ".L.", "m.."], ["h..", ".L.", "..m"], [".h.", "hLh", ".m."], ["L", "m"])
    col = {"h": R_H, "L": R_L, "m": R}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Vareth

C_DD, C_D, C, C_L = hexc("0e0a07"), hexc("201810"), hexc("3a2b1c"), hexc("5a442c")
M_D, M, M_L = hexc("1f3214"), hexc("355a22"), hexc("5e8f3b")
S_DD, S_D, S, S_L, S_H = hexc("0b2a14"), hexc("1f7a3c"), hexc("3fc265"), hexc("8cf0a8"), hexc("e6fff0")


def marco_corteza():
    rnd = random.Random(29)
    n = noise2(97)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.35, y * 0.12)
            c = C_D if v < 0.35 else C if v < 0.8 else C_L
            if x % 7 == 0:
                c = C_DD                                      # las vetas
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, C_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # musgo por arriba
    for x in range(x0 + 2, x1 - 1):
        if n(x * 0.3, 7.7) > 0.45:
            put(img, x, y0, M if x % 3 else M_L)
            if n(x * 0.5, 3.1) > 0.7:
                put(img, x, y0 - 1, M_D)
    # raices que cuelgan por debajo
    for _ in range(14):
        xx = rnd.randint(x0 + 6, x1 - 6)
        if abs(xx - FW // 2) < 14:
            continue
        largo = rnd.randint(2, 7)
        for i in range(largo):
            put(img, xx + (1 if (i // 2) % 2 else 0), y1 + 1 + i, C if i < largo - 1 else C_DD)
    # en los extremos, dos nudos con un brote
    for side in (0, 1):
        cx = x0 + 1 if side == 0 else x1 - 1
        cy = (FY0 + FY1) // 2
        for dy in range(-5, 6):
            for dx in range(-4, 5):
                if math.hypot(dx, dy * 0.8) <= 4.3:
                    put(img, cx + dx, cy + dy, OUT if math.hypot(dx, dy * 0.8) > 3.4 else (C_L if dx + dy < 0 else C))
        gem(img, cx, cy, 1, S_DD, S, S_L)
    # en el centro: el corazon entre dos ramas
    cx, cy = FW // 2, 12
    for s in (-1, 1):
        for i in range(12):
            x = cx + s * (6 + i)
            y = cy - 2 - int(i * 0.45)
            put(img, x, y, C_L)
            put(img, x, y + 1, C)
            put(img, x, y - 1, OUT)
            put(img, x, y + 2, OUT)
        put(img, cx + s * 17, cy - 9, M_L)
        put(img, cx + s * 16, cy - 8, M)
    for dy in range(-9, 10):
        for dx in range(-8, 9):
            d = math.hypot(dx, dy * 1.1)
            if d <= 8.2:
                put(img, cx + dx, cy + dy, OUT if d > 7.0 else (C_L if dx + dy < -2 else C))
    for dy in range(-6, 7):
        for dx in range(-5, 6):
            d = math.hypot(dx, dy * 1.05)
            if d <= 5.2:
                c = S_H if d < 1.3 else S_L if d < 2.6 else S if d < 4.0 else S_D
                put(img, cx + dx, cy + dy, c)
    return img


def relleno_corteza(k):
    n = noise2(101)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 5) * 0.1, y * 0.6)
            t = 0.2 + 0.55 * mid + 0.45 * (v - 0.5)
            c = mix(S_D, S, max(0.0, min(1.0, t * 1.4)))
            if t > 0.68:
                c = mix(c, S_L, min(1.0, (t - 0.68) * 3))
            if y == 0:
                c = mix(c, S_L, 0.4)
            if y == 6:
                c = mix(c, S_DD, 0.6)
            if (x * 3 + 7 * y - k * 17) % 83 == 0 and 1 <= y <= 5:
                c = S_H
            img.putpixel((x, y), c)
    return img


def hojas():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["..L", ".Lm", "Lm.", "m.."], [".L.", "LmL", ".m."], ["LL.", ".mm"], ["L.", "Lm", ".m"])
    col = {"L": M_L, "m": M}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- comun


def hueco(a, b):
    n = noise2(113)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            img.putpixel((x, y), mix(hexc(a), hexc(b), n(x * 0.3, y * 0.5)))
    return img


def rastro(a, b):
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            img.putpixel((x, y), mix(hexc(a), hexc(b), abs(y - 3) / 3))
    return img


def atlas(marco, relleno, hue, ras, cae):
    a = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    a.paste(marco, (0, 0))
    for k in range(4):
        a.paste(relleno(k), (0, 48 + 8 * k))
    a.paste(hue, (0, 80))
    a.paste(ras, (0, 88))
    a.paste(cae, (0, 104))
    return a


if __name__ == "__main__":
    os.makedirs(GUI, exist_ok=True)
    barras = {
        "brandt": atlas(marco_brandt(), relleno_brandt, hueco("080809", "17181b"), rastro("fff2ea", "e8b0a0"), chispas()),
        "corteza": atlas(marco_corteza(), relleno_corteza, hueco("050a06", "121c14"), rastro("f2fff6", "b9f0c8"), hojas()),
    }
    for nombre, b in barras.items():
        b.save(os.path.join(GUI, f"{nombre}_barra.png"))
        if len(sys.argv) > 1:
            d = sys.argv[1]
            os.makedirs(d, exist_ok=True)
            v = Image.new("RGBA", (256, 48), (58, 66, 76, 255))
            v.paste(b.crop((0, 80, 240, 87)), (8, 19 + 4))
            v.paste(b.crop((0, 88, 170, 95)), (8, 19 + 4))
            v.paste(b.crop((0, 48, 149, 55)), (8, 19 + 4))
            v.alpha_composite(b.crop((0, 0, 256, 40)), (0, 4))
            v.resize((256 * 4, 48 * 4), Image.NEAREST).save(os.path.join(d, f"{nombre}_barra_vista.png"))
    print("ok")
