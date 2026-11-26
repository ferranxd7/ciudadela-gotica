"""Barras de vida de los dos guardianes de Valdrenn (parte 4d), de la Molinera de Velmor (parte 7f) y del Verdugo de la
Arboleda (parte 7h), pixel a pixel, con
la misma disposicion que la del Capitan (la dibuja client/enemigos/GuardianCliente):

  Ser Brandt, el Muro del Rey: un marco de hierro negro remachado con filos rojos, el paves con la corona del Rey en el
  centro y dos puas en los extremos; relleno de sangre oscura que corre; chispas de hierro al recibir dano.
  Vareth, el Guardian de Corteza: un marco de corteza con musgo y raices que cuelgan, el corazon verde latiendo en el
  centro entre dos ramas; relleno de savia verde que corre; hojas que caen al recibir dano.
  La Molinera de Velmor: una viga de madera vieja del molino con flejes de hierro y espigas secas por encima, un aspa rota
  en cada punta y la piedra de moler en el centro; relleno de brasas que corren (las de sus grietas); ceniza y harina que
  caen al recibir dano.

  python3 tools/hud/barra_guardianes.py [carpeta_de_vistas]

  El Verdugo de la Arboleda (parte 7h): la viga de la horca de roble oscuro con vueltas de soga, los postes con sus
  tornapuntas en las puntas y, en el centro, el lazo que cuelga con la capucha del verdugo y sus ojos encendidos dentro;
  relleno de una soga empapada de sangre que gira; hebras de canamo y hojas secas que caen al recibir dano.

  El Barquero de la Laguna Negra (parte 7j): el remo tumbado (la pertiga mojada con anillas de hierro, la pala en la punta
  derecha y el garfio en la izquierda), cadenitas con monedas viejas colgando y la linterna de almas en el centro; relleno
  de agua negra con ondas y destellos de almas; gotas, una moneda y chispas de almas que caen al recibir dano.

  Hrodric, el Abad de la Cumbre (parte 7l): el baculo tumbado (la vara de plata escarchada con nudos de oro, el regaton y la
  voluta de hielo con su cristal), carambanos colgando y en el centro la mitra de hielo con su banda de oro; relleno de hielo
  azul que corre; copos y esquirlas que caen al recibir dano.

  El Condestable del Juramento (parte 7n): el montante tumbado (la hoja negra con los filos al rojo, la cruz de oro, el puno
  y el pomo a la izquierda, la punta a la derecha), dos manos alzadas de oro y en el centro el gran yelmo cerrado con la
  rendija encendida y la cimera en ascuas; relleno de fuego que corre; brasas y chispas que suben al recibir dano.

Salen textures/gui/brandt_barra.png, corteza_barra.png, molinera_barra.png, verdugo_barra.png, barquero_barra.png,
abad_barra.png y condestable_barra.png (256x128):
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


# ---------------------------------------------------------------- la Molinera de Velmor

W_DD, W_D, W, W_L = hexc("120c08"), hexc("2a1d13"), hexc("45321f"), hexc("66503a")
F_D, F, F_L = hexc("26262b"), hexc("44444c"), hexc("6c6c76")
T_D, T, T_L = hexc("5e5136"), hexc("8a7a52"), hexc("b9a877")
P_DD, P_D, P, P_L = hexc("1e1d1c"), hexc("4a4846"), hexc("6f6c68"), hexc("98948e")
B_DD, B_D, B, B_L, B_H = hexc("3a0c04"), hexc("7a2008"), hexc("c0401a"), hexc("f07a2a"), hexc("ffc070")
H_ = hexc("dcd7c8")


def marco_molinera():
    rnd = random.Random(41)
    n = noise2(59)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la viga: vetas a lo largo, nudos y grietas
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.08, y * 0.9)
            c = W_D if v < 0.38 else W if v < 0.82 else W_L
            if (x * 7 + y * 3) % 53 == 0:
                c = W_DD
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, W_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # flejes de hierro con sus clavos
    for x in range(x0 + 14, x1 - 10, 26):
        if abs(x - FW // 2) < 18:
            continue
        for y in range(y0 + 1, y1):
            if FY0 - 1 <= y <= FY1:
                continue
            put(img, x, y, F)
            put(img, x + 1, y, F_D)
        put(img, x, y0 + 2, F_L)
        put(img, x, y1 - 2, F_L)
    # espigas secas que asoman por arriba (y alguna caida)
    for _ in range(26):
        xx = rnd.randint(x0 + 4, x1 - 4)
        if abs(xx - FW // 2) < 15:
            continue
        alto = rnd.randint(3, 7)
        inclina = rnd.choice((-1, 0, 0, 1))
        for i in range(alto):
            px = xx + (inclina if i > alto // 2 else 0)
            put(img, px, y0 - 1 - i, T_D if i < alto - 3 else T)
        cab = y0 - alto
        put(img, xx + inclina, cab, T_L)
        put(img, xx + inclina - 1, cab + 1, T)
        put(img, xx + inclina + 1, cab + 1, T)
        put(img, xx + inclina, cab - 1, T_L)
    # en las puntas: el extremo de un aspa (el armazon de listones), de pie sobre la viga
    for side in (0, 1):
        bx = 1 if side == 0 else FW - 5
        for y in range(y0 - 7, y1 + 6):
            for dx in range(4):
                borde = dx in (0, 3)
                liston = (y - y0) % 3 == 0
                if borde or liston:
                    put(img, bx + dx, y, W_L if borde and dx == 0 else W)
                elif y < y0 or y > y1:
                    put(img, bx + dx, y, (0, 0, 0, 0))
            put(img, bx - 1, y, OUT)
            put(img, bx + 4, y, OUT)
        for dx in range(-1, 5):
            put(img, bx + dx, y0 - 8, OUT)
            put(img, bx + dx, y1 + 6, OUT)
    # en el centro: la piedra de moler, con su ojo y sus surcos (encima de la viga, sin tapar el relleno)
    cx, cy = FW // 2, 8
    for dy in range(-10, 11):
        for dx in range(-10, 11):
            d = math.hypot(dx, dy)
            if d > 9.9 or cy + dy >= FY0 - 1:
                continue
            if d > 8.9 or cy + dy == FY0 - 2:
                put(img, cx + dx, cy + dy, OUT)
                continue
            a = math.atan2(dy, dx)
            surco = 3.6 < d < 8.2 and abs(((a + math.pi) * 10 / (2 * math.pi)) % 1 - 0.5) < 0.1
            c = P_L if dx + dy < -6 else P if dx + dy < 6 else P_D
            if surco:
                c = P_DD
            if n(dx * 0.4 + 30, dy * 0.4) > 0.8:
                c = mix(c, P_DD, 0.5)
            put(img, cx + dx, cy + dy, c)
    # el ojo de la piedra: un hueco con brasas dentro
    for dy in range(-3, 4):
        for dx in range(-3, 4):
            if abs(dx) == 3 and abs(dy) == 3:
                continue
            put(img, cx + dx, cy + dy, OUT if max(abs(dx), abs(dy)) == 3 else (B_H if dx == 0 and dy == 0 else B_L if abs(dx) + abs(dy) < 2 else B))
    # harina en el borde de abajo de la piedra
    for dx in range(-7, 8):
        if rnd.random() < 0.6:
            put(img, cx + dx, FY0 - 3, H_)
    return img


def relleno_molinera(k):
    n = noise2(127)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 6) * 0.09, y * 0.55)
            t = 0.2 + 0.55 * mid + 0.45 * (v - 0.5)
            c = mix(B_D, B, max(0.0, min(1.0, t * 1.4)))
            if t > 0.66:
                c = mix(c, B_L, min(1.0, (t - 0.66) * 3))
            if y == 0:
                c = mix(c, B_L, 0.35)
            if y == 6:
                c = mix(c, B_DD, 0.6)
            # grietas de ceniza que corren por las brasas
            if (x * 5 + y * 11 - k * 23) % 97 == 0:
                c = P_D
            elif (x * 3 + 7 * y - k * 19) % 89 == 0 and 1 <= y <= 5:
                c = B_H
            img.putpixel((x, y), c)
    return img


def ceniza():
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["..h", ".c.", "c.."], ["h.", "cc"], [".h.", "hcH", ".c."], ["H.", ".c", "c."])
    col = {"h": H_, "c": P_L, "H": B_L}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- el Verdugo de la Arboleda (parte 7h)

O_DD, O_D, O, O_L = hexc("150e09"), hexc("2c1f15"), hexc("45311f"), hexc("624a31")
S_DD, S_D, S, S_L = hexc("3e3020"), hexc("56442c"), hexc("876e46"), hexc("b39a6a")
C_DD, C_D, C_L = hexc("070606"), hexc("141212"), hexc("2a2624")
J_D, J, J_L, J_H = hexc("5a1806"), hexc("a03a0c"), hexc("e0701a"), hexc("ffd070")
X_DD, X_D, X, X_L = hexc("1e0404"), hexc("420a08"), hexc("6e1610"), hexc("9c2a1c")


def marco_verdugo():
    """La viga de la horca de roble oscuro, con vueltas de soga; en las puntas los postes con sus tornapuntas; en el centro
    el lazo que cuelga de la viga, y dentro la capucha del verdugo con sus ojos encendidos."""
    rnd = random.Random(73)
    n = noise2(61)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la viga: vetas a lo largo, nudos y alguna mancha oscura
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.07, y * 0.8)
            c = O_D if v < 0.36 else O if v < 0.8 else O_L
            if (x * 5 + y * 9) % 61 == 0:
                c = O_DD
            if n(x * 0.3 + 40, y * 0.3) > 0.86:
                c = mix(c, X_D, 0.55)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, O_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # vueltas de soga alrededor de la viga (en diagonal, como se enrollan)
    for x in range(x0 + 18, x1 - 14, 30):
        if abs(x - FW // 2) < 20:
            continue
        for y in range(y0 + 1, y1):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(3):
                put(img, x + k + (y % 2), y, S_L if k == 0 else S if k == 1 else S_D)
        put(img, x - 1, y0, S_D)
        put(img, x + 3, y1, S_D)
    # en las puntas: los postes de la horca (de arriba abajo) con la tornapunta hacia el centro
    for side in (0, 1):
        bx = 1 if side == 0 else FW - 6
        for y in range(y0 - 6, y1 + 7):
            for dx in range(5):
                c = O_L if dx == 1 else O if dx < 4 else O_D
                if (y * 3 + dx) % 13 == 0:
                    c = O_DD
                put(img, bx + dx, y, c)
            put(img, bx - 1, y, OUT)
            put(img, bx + 5, y, OUT)
        for dx in range(-1, 6):
            put(img, bx + dx, y0 - 7, OUT)
            put(img, bx + dx, y1 + 7, OUT)
        # la tornapunta: de lo alto del poste a la viga
        sx = 1 if side == 0 else -1
        x_ini = bx + (5 if side == 0 else -1)
        for i in range(7):
            xx = x_ini + sx * i
            yy = y0 - 6 + i
            if yy >= y0:
                break
            put(img, xx, yy, O_L)
            put(img, xx, yy + 1, O_D)
            put(img, xx, yy - 1, OUT)
    # unas hebras sueltas que cuelgan bajo la viga
    for _ in range(14):
        xx = rnd.randint(x0 + 8, x1 - 8)
        if abs(xx - FW // 2) < 16:
            continue
        for i in range(rnd.randint(1, 3)):
            put(img, xx, y1 + i, S_D if i else S)
    # en el centro: el lazo que cuelga de la viga, y dentro la capucha con los ojos
    cx, cy = FW // 2, 8
    # la soga de arriba (desde la viga) y el nudo
    for y in range(0, 3):
        put(img, cx - 1, y, OUT)
        put(img, cx, y, S)
        put(img, cx + 1, y, S_D)
        put(img, cx + 2, y, OUT)
    for dy in range(-10, 11):
        for dx in range(-10, 11):
            d = math.hypot(dx * 0.95, dy)
            if cy + dy >= FY0 - 1 or cy + dy < 2:
                continue
            if 7.0 <= d < 9.6:
                # el lazo: canamo trenzado (en diagonal)
                t = (dx + dy * 2) % 4
                c = S_L if t == 0 else S if t < 3 else S_D
                if d >= 9.0:
                    c = OUT
                put(img, cx + dx, cy + dy, c)
            elif d < 7.0:
                # la capucha (negra, con algo de brillo arriba a la izquierda) y su pico
                c = C_L if dx + dy < -7 else C_D if dx + dy < 4 else C_DD
                if d > 6.2:
                    c = OUT
                put(img, cx + dx, cy + dy, c)
    # los ojos encendidos
    for ex in (-3, 2):
        put(img, cx + ex, cy, J_L)
        put(img, cx + ex + 1, cy, J_H)
        put(img, cx + ex, cy + 1, J)
        put(img, cx + ex + 1, cy + 1, J_L)
        put(img, cx + ex - 1, cy, J_D)
    return img


def relleno_verdugo(k):
    """Una soga empapada de sangre que gira: franjas en diagonal que corren con el cuadro."""
    n = noise2(131)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            fase = (x + y * 2 - k * 2) % 8
            t = 0.25 + 0.5 * mid + 0.35 * (n((x - k * 3) * 0.12, y * 0.6) - 0.5)
            c = mix(X_D, X, max(0.0, min(1.0, t * 1.3)))
            if fase < 2:
                c = mix(c, X_L, 0.45 + 0.3 * mid)
            elif fase == 7:
                c = mix(c, X_DD, 0.5)
            if y == 0:
                c = mix(c, X_L, 0.3)
            if y == 6:
                c = mix(c, X_DD, 0.6)
            # hebras de canamo que asoman
            if (x * 7 + y * 13 - k * 17) % 83 == 0 and 1 <= y <= 5:
                c = S
            img.putpixel((x, y), c)
    return img


def fibras():
    """Lo que cae al recibir dano: hebras de canamo, una hoja seca y gotas oscuras."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = (["s..", ".s.", "..S"], ["S.", "s.", ".s"], [".h.", "hHh", ".h."], ["x.", "x.", ".X"])
    col = {"s": S, "S": S_L, "h": O_L, "H": O, "x": X, "X": X_L}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- el Barquero de la Laguna Negra (parte 7j)

W_DD, W_D, W, W_L = hexc("0e0b09"), hexc("1f1812"), hexc("352a1f"), hexc("4f4030")
I_DD, I_D, I, I_L = hexc("101013"), hexc("26262c"), hexc("3c3c45"), hexc("63636e")
A_D, A, A_L, A_H = hexc("0f4d5e"), hexc("2aa3b8"), hexc("7fe3f2"), hexc("e6fdff")
B_DD, B_D, B, B_L = hexc("030607"), hexc("0a1317"), hexc("122228"), hexc("1f3a44")
G_D, G, G_L = hexc("6b5420"), hexc("a07d28"), hexc("e0bf4a")


def marco_barquero():
    """El remo tumbado: la pertiga de madera mojada con anillas de hierro, la pala ancha en la punta derecha y el garfio de
    hierro en la izquierda; de la pertiga cuelgan cadenitas con monedas viejas (el peaje); en el centro, la linterna de
    almas con su asa, la reja de hierro y el fuego azul palido dentro."""
    rnd = random.Random(97)
    n = noise2(67)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la pertiga: vetas a lo largo, oscura y mojada (mas clara arriba, donde brilla el agua)
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.06, y * 0.9)
            c = W_D if v < 0.4 else W if v < 0.82 else W_L
            if y == y0 + 1:
                c = mix(c, B_L, 0.35)
            if (x * 7 + y * 5) % 67 == 0:
                c = W_DD
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, W_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # las anillas de hierro
    for x in range(x0 + 22, x1 - 20, 34):
        if abs(x - FW // 2) < 22:
            continue
        for y in range(y0, y1 + 1):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(3):
                put(img, x + k, y, I_L if k == 0 else I if k == 1 else I_D)
        put(img, x - 1, y0, OUT)
        put(img, x + 3, y1, OUT)
    # la pala (punta derecha): ancha, de arriba abajo, con su banda de hierro y el borde comido
    px0 = FW - 14
    for y in range(y0 - 7, y1 + 8):
        ancho = 12 - (abs(y - (y0 + y1) // 2) // 4)
        for dx in range(ancho):
            c = W if dx > 2 else W_L if dx == 1 else W_D
            if dx == 0:
                c = I_L
            if (y * 3 + dx * 5) % 17 == 0:
                c = W_DD
            put(img, px0 + dx, y, c)
        put(img, px0 - 1, y, OUT)
        put(img, px0 + ancho, y, OUT)
    for dx in range(-1, 13):
        put(img, px0 + dx, y0 - 8, OUT)
        put(img, px0 + dx, y1 + 8, OUT)
    # el garfio (punta izquierda): la punta hacia fuera y el gancho que sube y vuelve
    for x in range(0, 9):
        for y in (y0 + 3, y0 + 4):
            put(img, x, y, I_L if y == y0 + 3 else I_D)
        put(img, x, y0 + 2, OUT)
        put(img, x, y0 + 5, OUT)
    put(img, 0, y0 + 3, I_DD)
    for y in range(y0 - 6, y0 + 3):
        put(img, 5, y, I)
        put(img, 6, y, I_D)
        put(img, 4, y, OUT)
        put(img, 7, y, OUT)
    for x in range(6, 11):
        put(img, x, y0 - 6, I_L)
        put(img, x, y0 - 7, OUT)
        put(img, x, y0 - 5, I_D if x > 7 else I)
    put(img, 10, y0 - 4, I)
    put(img, 11, y0 - 5, OUT)
    # las cadenitas con monedas que cuelgan bajo la pertiga
    for xx in (x0 + 40, x0 + 64, x1 - 70, x1 - 46, x1 - 30):
        largo = rnd.randint(3, 5)
        for i in range(largo):
            put(img, xx, y1 + 1 + i, I if i % 2 == 0 else I_D)
        cy = y1 + 1 + largo
        for dx in (-1, 0, 1):
            for dy in (0, 1, 2):
                if abs(dx) + abs(dy - 1) <= 1:
                    put(img, xx + dx, cy + dy, G_L if dx < 0 or dy == 0 else G)
        put(img, xx + 1, cy + 2, G_D)
    # en el centro: la linterna de almas (el asa, la tapa, la reja y el fuego)
    cx = FW // 2
    # el asa (un aro) que asoma por encima de la pertiga
    for dx in range(-3, 4):
        put(img, cx + dx, 0, OUT)
    for y in range(1, 4):
        put(img, cx - 3, y, I)
        put(img, cx + 3, y, I_D)
        put(img, cx - 4, y, OUT)
        put(img, cx + 4, y, OUT)
    for dx in range(-2, 3):
        put(img, cx + dx, 1, I_L)
    # el cuerpo: de y 4 a y 17 (sobre la pertiga), 15 de ancho
    for y in range(4, 18):
        for dx in range(-7, 8):
            borde = abs(dx) == 7 or y in (4, 17)
            tapa = y in (5, 6, 15, 16)
            barra = dx in (-6, 0, 6)
            if borde:
                c = OUT
            elif tapa:
                c = I_L if y in (5, 15) else I_D
            elif barra:
                c = I if dx != 6 else I_D
            else:
                # el fuego de almas: blanco en el centro abajo, azul por los bordes
                d = math.hypot(dx * 0.9, (y - 12) * 0.8)
                c = A_H if d < 1.6 else A_L if d < 3.0 else A if d < 4.6 else A_D
            put(img, cx + dx, y, c)
    return img


def relleno_barquero(k):
    """Agua negra que corre, con ondas y destellos de almas azules que pasan."""
    n = noise2(151)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.2 + 0.45 * mid + 0.4 * (n((x - k * 4) * 0.09, y * 0.5) - 0.5)
            c = mix(B_D, B, max(0.0, min(1.0, t * 1.4)))
            onda = (x - k * 4 + int(3 * math.sin((x + y * 3) * 0.2))) % 23
            if onda in (0, 1) and 1 <= y <= 5:
                c = mix(c, B_L, 0.7)
            if y == 0:
                c = mix(c, B_L, 0.35)
            if y == 6:
                c = mix(c, B_DD, 0.6)
            # los destellos de almas
            if (x * 11 + y * 7 - k * 29) % 97 == 0 and 1 <= y <= 5:
                c = A_L
            elif (x * 11 + y * 7 - k * 29) % 97 == 1 and 1 <= y <= 5:
                c = A
            img.putpixel((x, y), c)
    return img


def gotas():
    """Lo que cae al recibir dano: gotas de agua negra, una moneda y una chispa de almas."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".b.", "bBb", ".b."], ["b.", "B.", "b."], [".g.", "gGg", ".g."], [".a.", "aAa", ".a."])
    col = {"b": B_L, "B": A_D, "g": G, "G": G_L, "a": A, "A": A_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- Hrodric, el Abad de la Cumbre (parte 7l)

P_DD, P_D, P, P_L, P_H = hexc("1b2026"), hexc("353e47"), hexc("5b6873"), hexc("95a3ad"), hexc("d6e0e6")
Q_DD, Q_D, Q, Q_L, Q_H = hexc("10283a"), hexc("2f5f7a"), hexc("4a86a3"), hexc("9bd2e6"), hexc("ecfbff")
O2_D, O2, O2_L = hexc("6e4a14"), hexc("b8862a"), hexc("e8c560")


def marco_abad():
    """El baculo tumbado: la vara de plata escarchada con nudos de oro, el regaton a la izquierda y a la derecha la voluta
    de hielo que sube y se enrosca con el cristal que luce; carambanos colgando de la vara; en el centro la mitra de hielo
    (las dos hojas en punta, la banda de oro, el cristal de la punta) y a los lados dos cruces pequenas."""
    rnd = random.Random(131)
    n = noise2(137)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la vara: plata con vetas a lo largo, luz arriba, escarcha a trozos
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.07, y * 0.8)
            c = P_D if v < 0.38 else P if v < 0.8 else P_L
            if y == y0 + 1:
                c = mix(c, P_H, 0.5)
            if y == y1 - 1:
                c = mix(c, P_DD, 0.5)
            if n(x * 0.3 + 40, y * 0.9) > 0.86:
                c = mix(c, Q_H, 0.6)
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, P_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # los nudos de oro (anillos) a lo largo
    for x in range(x0 + 26, x1 - 30, 38):
        if abs(x - FW // 2) < 26:
            continue
        for y in range(y0 - 1, y1 + 2):
            if FY0 - 1 <= y <= FY1:
                continue
            for k in range(4):
                put(img, x + k, y, O2_L if k == 0 else O2 if k < 3 else O2_D)
        for k in range(-1, 5):
            put(img, x + k, y0 - 2, OUT)
            put(img, x + k, y1 + 2, OUT)
    # el regaton (punta izquierda): una punta de plata
    for x in range(0, 6):
        for y in range(y0 + 1, y1):
            if abs(y - (y0 + y1) / 2) <= (x + 1) * 0.7:
                put(img, x, y, P_L if y < (y0 + y1) / 2 else P_D)
    # la voluta de hielo (punta derecha): sube por la derecha, se enrosca hacia dentro y luce el cristal en la curva
    vx = FW - 12
    for y in range(1, y1):
        for dx in range(3):
            put(img, vx + 6 + dx, y, Q_L if dx == 0 else Q if dx == 1 else Q_D)
        put(img, vx + 5, y, OUT)
        put(img, vx + 9, y, OUT)
    for x in range(vx - 2, vx + 9):
        for dy in range(3):
            put(img, x, 1 + dy, Q_L if dy == 0 else Q if dy == 1 else Q_D)
        put(img, x, 0, OUT)
        put(img, x, 4, OUT)
    for y in range(4, 11):
        for dx in range(3):
            put(img, vx - 2 + dx, y, Q_L if dx == 0 else Q if dx == 1 else Q_D)
        put(img, vx - 3, y, OUT)
    for x in range(vx - 2, vx + 2):
        put(img, x, 11, OUT)
    # el cristal dentro de la curva
    for y in range(5, 10):
        for x in range(vx + 1, vx + 5):
            d = abs(x - (vx + 2.5)) + abs(y - 7)
            if d <= 2.5:
                put(img, x, y, Q_H if d < 1.2 else Q_L)
    # los carambanos que cuelgan de la vara
    for xx in (x0 + 36, x0 + 50, x0 + 80, x1 - 86, x1 - 62, x1 - 44):
        largo = rnd.randint(3, 6)
        for i in range(largo):
            ancho = 1 if i >= largo - 2 else 2
            for dx in range(ancho):
                put(img, xx + dx, y1 + 1 + i, Q_H if dx == 0 and i < 2 else Q_L if i < largo - 1 else Q)
        put(img, xx - 1, y1 + 1, OUT)
    # dos cruces de oro pequenas sobre la vara, a los lados de la mitra
    for cx in (FW // 2 - 40, FW // 2 + 40):
        for y in range(y0 - 7, y0 + 1):
            put(img, cx, y, O2_L if y < y0 - 3 else O2)
            put(img, cx + 1, y, O2_D)
        for x in range(cx - 2, cx + 4):
            put(img, x, y0 - 5, O2_L)
        put(img, cx, y0 - 8, OUT)
    # en el centro: la mitra de hielo (de y 0 a 18), con su banda de oro y el cristal de la punta
    cx = FW // 2
    for y in range(0, 19):
        semi = 7 if y >= 7 else max(1, int((y + 1) * 1.0))
        for dx in range(-semi - 1, semi + 2):
            put(img, cx + dx, y, OUT)
    for y in range(1, 18):
        semi = 6 if y >= 7 else max(0, int(y * 0.95))
        for dx in range(-semi, semi + 1):
            c = Q_L if dx < -semi // 2 else Q if dx <= semi // 2 else Q_D
            if dx == 0 and y >= 4:
                c = mix(O2, Q_L, 0.35)          # la franja de oro de en medio, bajo el hielo
            if y >= 15:
                c = O2_L if (dx + y) % 2 else O2   # la banda
            put(img, cx + dx, y, c)
    for y in range(0, 4):
        put(img, cx, y, Q_H)
    put(img, cx - 2, 16, Q_H)
    put(img, cx + 2, 16, Q_H)
    return img


def relleno_abad(k):
    """Hielo azul que corre, con vetas claras y destellos blancos que pasan."""
    n = noise2(167)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            t = 0.25 + 0.45 * mid + 0.4 * (n((x - k * 3) * 0.08, y * 0.5) - 0.5)
            c = mix(Q_D, Q, max(0.0, min(1.0, t * 1.4)))
            veta = (x - k * 3 + int(2 * math.sin((x + y * 4) * 0.17))) % 19
            if veta in (0, 1) and 1 <= y <= 5:
                c = mix(c, Q_L, 0.65)
            if y == 0:
                c = mix(c, Q_H, 0.45)
            if y == 6:
                c = mix(c, Q_DD, 0.6)
            if (x * 13 + y * 5 - k * 31) % 89 == 0 and 1 <= y <= 5:
                c = Q_H
            img.putpixel((x, y), c)
    return img


def copos():
    """Lo que cae al recibir dano: copos de nieve y esquirlas de hielo."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".w.", "wWw", ".w."], ["w.w", ".W.", "w.w"], [".q", "qQ", "q."], ["q", "Q", "q"])
    col = {"w": Q_L, "W": Q_H, "q": Q, "Q": Q_H}
    for k, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, k * 8 + 2 + x, 2 + y, col[ch])
    return img


# ---------------------------------------------------------------- el Condestable del Juramento (parte 7n)

K_DD, K_D, K, K_L, K_H = hexc("08080a"), hexc("17161a"), hexc("2a282e"), hexc("47434c"), hexc("6e6874")
E_DD, E_D, E, E_L, E_H = hexc("3a0d06"), hexc("8a2208"), hexc("d24a14"), hexc("f88a2c"), hexc("ffe08a")
G_D, G, G_L = hexc("6e5622"), hexc("a8842e"), hexc("e2c464")
CR_D, CR = hexc("451210"), hexc("74221c")


def marco_condestable():
    """El montante tumbado: la hoja negra con los filos al rojo y rajas de brasa; a la izquierda la cruz de oro, el puno de
    cuero y el pomo; a la derecha la punta; dos manos alzadas de oro a los lados y en el centro el gran yelmo cerrado con la
    rendija encendida y su cimera de crines en ascuas."""
    rnd = random.Random(171)
    n = noise2(173)
    img = Image.new("RGBA", (FW, 40), (0, 0, 0, 0))
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    # la hoja: acero ennegrecido; los filos de arriba y de abajo al rojo; rajas de brasa
    for y in range(y0 + 1, y1):
        for x in range(x0 + 1, x1):
            v = n(x * 0.06, y * 0.7)
            c = K_D if v < 0.4 else K if v < 0.82 else K_L
            if y == y0 + 1:
                c = E_L if (x // 3) % 4 else E
            if y == y1 - 1:
                c = E if (x // 3) % 4 else E_D
            if n(x * 0.25 + 70, y * 0.8) > 0.88 and y0 + 1 < y < y1 - 1:
                c = E_D
            put(img, x, y, c)
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, K_DD)
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    # la punta (derecha): la hoja se estrecha
    for x in range(x1, FW):
        semi = (FW - x) * 0.45
        for y in range(y0 - 1, y1 + 2):
            m = (y0 + y1) / 2
            if abs(y - m) <= semi:
                put(img, x, y, E_L if abs(abs(y - m) - semi) < 1 else K)
    # la cruz de oro (izquierda), el puno y el pomo
    cx0 = x0 + 2
    for y in range(y0 - 7, y1 + 8):
        for dx in range(3):
            put(img, cx0 + dx, y, G_L if dx == 0 else G if dx == 1 else G_D)
        put(img, cx0 - 1, y, OUT)
        put(img, cx0 + 3, y, OUT)
    for dx in range(-1, 4):
        put(img, cx0 + dx, y0 - 8, OUT)
        put(img, cx0 + dx, y1 + 8, OUT)
    for x in range(0, cx0 - 1):
        for y in range(y0 + 2, y1 - 1):
            put(img, x, y, CR_D if (x + y) % 3 == 0 else hexc("2e2015"))
    for y in range(y0, y1 + 1):
        for x in range(0, 2):
            put(img, x, y, G_L if x == 0 else G)
    # dos manos alzadas de oro, a los lados del yelmo
    mano = ["#.#.#", "#.#.#", "#####", "#####", ".###.", "..#.."]
    for mx in (FW // 2 - 42, FW // 2 + 38):
        for yy, fila in enumerate(mano):
            for xx, ch in enumerate(fila):
                if ch == "#":
                    put(img, mx + xx, y0 - 6 + yy, G_L if yy < 2 else G if yy < 4 else G_D)
    # en el centro: el gran yelmo (de y 1 a 18) con la rendija encendida, la nervadura y la cimera
    cx = FW // 2
    for y in range(1, 19):
        semi = 7 if y >= 4 else 5 + (y - 1) // 2
        for dx in range(-semi - 1, semi + 2):
            put(img, cx + dx, y, OUT)
    for y in range(2, 18):
        semi = 6 if y >= 4 else 4 + (y - 2) // 2
        for dx in range(-semi, semi + 1):
            c = K_L if dx < -semi // 2 else K if dx <= semi // 2 else K_D
            if y == 9 and abs(dx) <= 5:
                c = OUT if abs(dx) in (0, 1) else (E_H if abs(dx) in (3, 4) else E_L)   # la rendija y los ojos
            if dx == 0 and y >= 10:
                c = K_H                                       # la nervadura
            if y in (12, 14) and abs(dx) in (2, 4):
                c = K_DD                                      # los respiraderos
            if y == 5:
                c = G if (dx + y) % 2 else G_L                # la banda de oro de la frente
            put(img, cx + dx, y, c)
    # la cimera: crines en ascuas que suben del yelmo
    for dx in range(-2, 3):
        alto = 3 + rnd.randint(0, 2) - abs(dx)
        for i in range(alto):
            put(img, cx + dx, 1 - i if 1 - i >= 0 else 0, E_H if i == alto - 1 else E_L)
    return img


def relleno_condestable(k):
    """Fuego que corre: brasas rojas y naranjas que se mueven, con lenguas amarillas que pasan."""
    n = noise2(181)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            mid = 1 - abs(y - 3) / 3.5
            v = n((x - k * 5) * 0.1, y * 0.6 - k * 0.3)
            t = 0.25 + 0.5 * mid + 0.5 * (v - 0.5)
            c = mix(E_D, E, max(0.0, min(1.0, t * 1.5)))
            if t > 0.62:
                c = mix(c, E_L, min(1.0, (t - 0.62) * 3))
            if t > 0.86:
                c = mix(c, E_H, min(1.0, (t - 0.86) * 4))
            if y == 0:
                c = mix(c, E_L, 0.4)
            if y == 6:
                c = mix(c, E_DD, 0.65)
            if (x * 7 + y * 13 - k * 29) % 101 == 0 and 1 <= y <= 5:
                c = E_H
            img.putpixel((x, y), c)
    return img


def brasas():
    """Lo que salta al recibir dano: brasas y chispas (suben, no caen: lo hace el cliente)."""
    img = Image.new("RGBA", (32, 8), (0, 0, 0, 0))
    formas = ([".e.", "eHe", ".e."], ["h.", ".E"], [".h", "hE", "e."], ["H", "e", "."])
    col = {"e": E_L, "E": E, "h": E_H, "H": E_H}
    for kk, f in enumerate(formas):
        for y, fila in enumerate(f):
            for x, ch in enumerate(fila):
                if ch != ".":
                    put(img, kk * 8 + 2 + x, 2 + y, col[ch])
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
        "molinera": atlas(marco_molinera(), relleno_molinera, hueco("0b0908", "1c1815"), rastro("f6f2e6", "d6cdb6"), ceniza()),
        "verdugo": atlas(marco_verdugo(), relleno_verdugo, hueco("0a0606", "1a1110"), rastro("f6ece2", "d8b9a6"), fibras()),
        "barquero": atlas(marco_barquero(), relleno_barquero, hueco("040607", "0f1a1e"), rastro("eefcff", "a9dbe6"), gotas()),
        "abad": atlas(marco_abad(), relleno_abad, hueco("05090c", "121c24"), rastro("f6fcff", "c4e2ef"), copos()),
        "condestable": atlas(marco_condestable(), relleno_condestable, hueco("0a0605", "1c1210"), rastro("fff2e0", "f0b88a"), brasas()),
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
