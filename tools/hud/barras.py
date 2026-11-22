"""Pinta (pixel a pixel, estilo Minecraft) la barra de vida del Rey y la barra de energia de sus espadas.

  python3 tools/hud/barras.py [carpeta_de_vistas]

Salen:
  textures/gui/rey_barra.png  (256x128): marco con corona y gemas + rellenos (sangre, fuego x4, lava x4, vacio, rastro)
  textures/gui/energia.png    (128x128): espada vacia + fuego en el canal x4 + runas encendidas + brillo de la hoja
"""
import math, os, random, sys
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
GUI = os.path.join(AQUI, "..", "..", "src", "main", "resources", "assets", "ciudadela", "textures", "gui")

def hexc(s, a=255):
    s = s.lstrip("#")
    return (int(s[0:2], 16), int(s[2:4], 16), int(s[4:6], 16), a)

def mix(c1, c2, t):
    return tuple(int(round(c1[i] + (c2[i] - c1[i]) * t)) for i in range(4))

def noise2(seed):
    rnd = random.Random(seed)
    g = {}
    def val(x, y):
        k = (x, y)
        if k not in g: g[k] = rnd.random()
        return g[k]
    def smooth(x, y):
        x0, y0 = math.floor(x), math.floor(y)
        fx, fy = x - x0, y - y0
        fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
        a, b = val(x0, y0), val(x0 + 1, y0)
        c, d = val(x0, y0 + 1), val(x0 + 1, y0 + 1)
        return (a + (b - a) * fx) * (1 - fy) + (c + (d - c) * fx) * fy
    return smooth

# ------------------------------------------------------------------ barra del Rey
FW, FX0, FX1, FY0, FY1 = 256, 8, 248, 19, 26          # relleno: x 8..247, y 19..25 (240 x 7)
OUT = hexc("0d0908")
IRON_D, IRON, IRON_L = hexc("2b2622"), hexc("4a423b"), hexc("736659")
GOLD_D, GOLD, GOLD_L, GOLD_H = hexc("6e4a14"), hexc("b8862a"), hexc("e3b545"), hexc("fff0a0")
RED_D, RED, RED_L = hexc("5a0a0c"), hexc("a3161a"), hexc("e8484a")
BLUE, BLUE_L = hexc("1d3f9a"), hexc("6f9bff")

def put(img, x, y, c):
    if 0 <= x < img.width and 0 <= y < img.height and c[3] > 0:
        img.putpixel((x, y), c)

def rect(img, x0, y0, x1, y1, c):
    for y in range(y0, y1 + 1):
        for x in range(x0, x1 + 1):
            if c[3] == 0:
                if 0 <= x < img.width and 0 <= y < img.height: img.putpixel((x, y), c)
            else: put(img, x, y, c)

def gem(img, cx, cy, h, col_d, col, col_l):
    """Gema en rombo de alto 2h+1, con brillo arriba a la izquierda y contorno oscuro."""
    for dy in range(-h - 1, h + 2):
        w = h + 1 - abs(dy)
        for dx in range(-w, w + 1):
            put(img, cx + dx, cy + dy, OUT)
    for dy in range(-h, h + 1):
        w = h - abs(dy)
        for dx in range(-w, w + 1):
            c = col
            if dx + dy < -h // 2: c = col_l
            elif dx + dy > h // 2: c = col_d
            put(img, cx + dx, cy + dy, c)
    put(img, cx - 1, cy - h + 1 if h > 1 else cy, hexc("ffffff"))

def frame():
    img = Image.new("RGBA", (FW, 32), (0, 0, 0, 0))
    # placa de hierro alrededor del relleno (3 px), contorno negro
    x0, x1, y0, y1 = FX0 - 4, FX1 + 3, FY0 - 4, FY1 + 3
    rect(img, x0, y0, x1, y1, OUT)
    rect(img, x0 + 1, y0 + 1, x1 - 1, y1 - 1, IRON)
    for x in range(x0 + 1, x1):
        put(img, x, y0 + 1, IRON_L)            # luz arriba
        put(img, x, y1 - 1, IRON_D)            # sombra abajo
        if x % 6 == 0:                          # remaches
            put(img, x, y0 + 2, IRON_L); put(img, x, y1 - 2, IRON_D)
    # filete dorado pegado al relleno
    rect(img, FX0 - 1, FY0 - 1, FX1, FY1, GOLD_D)
    for x in range(FX0 - 1, FX1 + 1):
        put(img, x, FY0 - 1, GOLD_L)
    # hueco del relleno transparente
    rect(img, FX0, FY0, FX1 - 1, FY1 - 1, (0, 0, 0, 0))
    for y in range(FY0, FY1): put(img, FX0 - 1, y, GOLD); put(img, FX1, y, GOLD_D)
    # remates de los extremos: punta de lanza de hierro con gema roja
    for side in (0, 1):
        cx = 3 if side == 0 else FW - 4
        cy = (FY0 + FY1) // 2
        for dy in range(-8, 9):
            w = max(0, 4 - abs(dy) // 2)
            for dx in range(-w, w + 1):
                put(img, cx + dx, cy + dy, OUT)
        for dy in range(-7, 8):
            w = max(0, 3 - abs(dy) // 2)
            for dx in range(-w, w + 1):
                c = IRON_L if dx < 0 else (IRON_D if dx > 0 else IRON)
                put(img, cx + dx, cy + dy, c)
        gem(img, cx, cy, 2, RED_D, RED, RED_L)
    # separadores de fase: barra dorada con gema roja
    for k in (1, 2):
        xs = FX0 + (FX1 - FX0) * k // 3
        rect(img, xs - 2, FY0 - 6, xs + 2, FY1 + 5, OUT)
        rect(img, xs - 1, FY0 - 5, xs + 1, FY1 + 4, GOLD)
        for y in range(FY0 - 5, FY1 + 5): put(img, xs - 1, y, GOLD_L); put(img, xs + 1, y, GOLD_D)
        gem(img, xs, (FY0 + FY1) // 2 - 1, 3, RED_D, RED, RED_L)
    # corona en el centro, apoyada en el borde de arriba
    cx, base = FW // 2, FY0 - 4
    # aro
    rect(img, cx - 12, base - 5, cx + 12, base + 1, OUT)
    rect(img, cx - 11, base - 4, cx + 11, base, GOLD)
    for x in range(cx - 11, cx + 12):
        put(img, x, base - 4, GOLD_L); put(img, x, base, GOLD_D)
    # cinco puntas con bolitas
    for i, dx in enumerate((-10, -5, 0, 5, 10)):
        hgt = 9 if dx == 0 else (7 if abs(dx) == 5 else 5)
        for y in range(base - 5 - hgt, base - 4):
            w = 1 if y > base - 5 - hgt + 2 else 0
            for xx in range(-w - 1, w + 2): put(img, cx + dx + xx, y, OUT)
        for y in range(base - 4 - hgt, base - 4):
            w = 1 if y > base - 4 - hgt + 2 else 0
            for xx in range(-w, w + 1):
                put(img, cx + dx + xx, y, GOLD_L if xx < 0 else (GOLD_D if xx > 0 else GOLD))
        top = base - 5 - hgt
        rect(img, cx + dx - 1, top - 2, cx + dx + 1, top, OUT)
        put(img, cx + dx, top - 1, GOLD_H)
    # gemas del aro: roja en medio, azules a los lados
    gem(img, cx, base - 2, 1, RED_D, RED, RED_L)
    gem(img, cx - 7, base - 2, 1, hexc("0e1f4d"), BLUE, BLUE_L)
    gem(img, cx + 7, base - 2, 1, hexc("0e1f4d"), BLUE, BLUE_L)
    return img

def relleno_sangre():
    n = noise2(3)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            t = y / 6.0
            c = mix(hexc("d63a34"), hexc("6b0b0e"), t)
            v = n(x / 5.0, y / 2.0) - 0.5
            c = mix(c, hexc("3a0406") if v < 0 else hexc("ff7060"), abs(v) * 0.45)
            if y == 0: c = mix(c, hexc("ff9a8a"), 0.55)
            if y == 6: c = mix(c, hexc("2a0204"), 0.5)
            img.putpixel((x, y), c)
    return img

def relleno_fuego(k):
    n = noise2(11)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            # lenguas de fuego que suben: el ruido se desplaza hacia arriba en cada fotograma
            v = n(x / 3.5, (y + k * 1.75) / 1.6) * 0.7 + n(x / 9.0 + 7, (y + k * 1.75) / 4.0) * 0.3
            h = 1 - y / 6.0
            heat = max(0.0, min(1.0, v * 1.25 - 0.15 + h * 0.45))
            if heat > 0.8: c = mix(hexc("ffd24a"), hexc("fff6c0"), (heat - 0.8) / 0.2)
            elif heat > 0.5: c = mix(hexc("f07014"), hexc("ffd24a"), (heat - 0.5) / 0.3)
            elif heat > 0.25: c = mix(hexc("a8260a"), hexc("f07014"), (heat - 0.25) / 0.25)
            else: c = mix(hexc("4a0804"), hexc("a8260a"), heat / 0.25)
            if y == 0: c = mix(c, hexc("fff0b0"), 0.35)
            img.putpixel((x, y), c)
    return img

def grietas():
    """Mapa de grietas (Voronoi: bordes entre celdas) para la lava."""
    rnd = random.Random(5)
    pts = [(rnd.uniform(0, 240), rnd.uniform(-2, 9)) for _ in range(46)]
    out = [[0.0] * 7 for _ in range(240)]
    for x in range(240):
        for y in range(7):
            ds = sorted(math.hypot((x - px) * 0.8, (y - py) * 1.6) for px, py in pts)
            out[x][y] = ds[1] - ds[0]
    return out

GR = None
def relleno_lava(k):
    global GR
    if GR is None: GR = grietas()
    n = noise2(19)
    img = Image.new("RGBA", (240, 7))
    pulso = 0.5 + 0.5 * math.sin(k * math.pi / 2)
    for x in range(240):
        for y in range(7):
            edge = GR[x][y]
            v = n(x / 6.0 + k * 0.6, y / 3.0)
            if edge < 1.1:
                glow = 1 - edge / 1.1
                c = mix(hexc("e0480c"), hexc("ffe27a"), glow * (0.6 + 0.4 * pulso))
            else:
                c = mix(hexc("2e0905"), hexc("5a1408"), v)
                if y == 0: c = mix(c, hexc("8a2a10"), 0.5)
            img.putpixel((x, y), c)
    return img

def relleno_vacio():
    n = noise2(23)
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            c = mix(hexc("140808"), hexc("231312"), n(x / 4.0, y / 2.0))
            if y == 0: c = mix(c, hexc("050202"), 0.6)
            img.putpixel((x, y), c)
    return img

def relleno_rastro():
    img = Image.new("RGBA", (240, 7))
    for x in range(240):
        for y in range(7):
            c = mix(hexc("f3d98a"), hexc("b88a3a"), y / 6.0)
            img.putpixel((x, y), c)
    return img

def barra_rey():
    atlas = Image.new("RGBA", (256, 128), (0, 0, 0, 0))
    atlas.paste(frame(), (0, 0))
    atlas.paste(relleno_sangre(), (0, 32))
    for k in range(4):
        atlas.paste(relleno_fuego(k), (0, 40 + 8 * k))
        atlas.paste(relleno_lava(k), (0, 72 + 8 * k))
    atlas.paste(relleno_vacio(), (0, 104))
    atlas.paste(relleno_rastro(), (0, 112))
    return atlas

# ------------------------------------------------------------------ energia: una espada que se llena de fuego
EW = 96
CANAL = (31, 86, 6, 9)   # x0, x1, y0, y1 del canal de la hoja

def espada():
    img = Image.new("RGBA", (EW, 16), (0, 0, 0, 0))
    # pomo
    rect(img, 0, 5, 4, 10, OUT)
    rect(img, 1, 6, 3, 9, GOLD)
    put(img, 1, 6, GOLD_H); put(img, 3, 9, GOLD_D)
    # empunadura de cuero con vueltas
    rect(img, 4, 6, 19, 9, OUT)
    for x in range(5, 19):
        for y in range(7, 9):
            c = hexc("5a3218") if (x + y) % 3 else hexc("3a1e0e")
            put(img, x, y, c)
        put(img, x, 7, hexc("7a4a28") if x % 3 == 0 else img.getpixel((x, 7)))
    # guarda dorada con gema
    rect(img, 20, 1, 27, 14, OUT)
    rect(img, 21, 2, 26, 13, GOLD)
    for y in range(2, 14): put(img, 21, y, GOLD_L); put(img, 26, y, GOLD_D)
    put(img, 21, 2, GOLD_H)
    gem(img, 23, 7, 2, RED_D, RED, RED_L)
    # hoja de acero que se estrecha hasta la punta
    for x in range(28, EW):
        t = (x - 28) / (EW - 1 - 28)
        half = 4 if x < 84 else max(0, int(round(4 * (1 - (x - 84) / (EW - 1 - 84)))))
        cy = 7.5
        top, bot = int(math.floor(cy - half)), int(math.ceil(cy + half))
        if half <= 0:
            put(img, x, 7, OUT); put(img, x, 8, OUT)
            continue
        for y in range(top - 1, bot + 1): put(img, x, y, OUT)
        for y in range(top, bot):
            c = hexc("c9ced6") if y < cy else hexc("8f959e")
            if y == top: c = hexc("eef1f5")
            if y == bot - 1: c = hexc("5d626a")
            put(img, x, y, c)
    # canal oscuro donde entra el fuego
    x0, x1, y0, y1 = CANAL
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            put(img, x, y, hexc("2a2c31") if y in (y0, y1) else hexc("1b1c20"))
    # runas grabadas (35 y 80 de energia)
    for frac in (0.35, 0.8):
        rx = x0 + int(round((x1 - x0) * frac))
        for (dx, dy) in ((0, -2), (0, 2), (-1, -3), (1, 3)):
            put(img, rx + dx, 7 + dy if dy < 0 else 8 + dy, hexc("4a3a20"))
    return img

def fuego_canal(k):
    n = noise2(31)
    img = Image.new("RGBA", (EW, 16), (0, 0, 0, 0))
    x0, x1, y0, y1 = CANAL
    for x in range(x0, x1 + 1):
        for y in range(y0, y1 + 1):
            v = n(x / 3.0 - k * 1.3, (y + k * 0.8) / 1.2)
            heat = max(0.0, min(1.0, v * 1.2 + (0.25 if y in (y0 + 1, y1 - 1) else 0.05)))
            if heat > 0.75: c = mix(hexc("ffd24a"), hexc("fff8d0"), (heat - 0.75) / 0.25)
            elif heat > 0.4: c = mix(hexc("f06a12"), hexc("ffd24a"), (heat - 0.4) / 0.35)
            else: c = mix(hexc("9a1e08"), hexc("f06a12"), heat / 0.4)
            img.putpixel((x, y), c)
    return img

def runas_encendidas():
    img = Image.new("RGBA", (EW, 16), (0, 0, 0, 0))
    x0, x1, _, _ = CANAL
    for frac in (0.35, 0.8):
        rx = x0 + int(round((x1 - x0) * frac))
        for (dx, dy) in ((0, -2), (0, 2), (-1, -3), (1, 3)):
            put(img, rx + dx, 7 + dy if dy < 0 else 8 + dy, hexc("ffe27a"))
    return img

def brillo_hoja():
    """Contorno brillante de la hoja (se dibuja con parpadeo cuando la energia esta llena)."""
    base = espada()
    img = Image.new("RGBA", (EW, 16), (0, 0, 0, 0))
    for x in range(28, EW):
        for y in range(16):
            if base.getpixel((x, y))[3] == 0:
                near = any(0 <= x + dx < EW and 0 <= y + dy < 16 and base.getpixel((x + dx, y + dy))[3] > 0 and x + dx >= 28
                           for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)))
                if near: img.putpixel((x, y), hexc("ff9a2a", 200))
    return img

def energia():
    atlas = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    atlas.paste(espada(), (0, 0))
    for k in range(4): atlas.paste(fuego_canal(k), (0, 16 + 16 * k))
    atlas.paste(runas_encendidas(), (0, 80))
    atlas.paste(brillo_hoja(), (0, 96))
    return atlas

if __name__ == "__main__":
    os.makedirs(GUI, exist_ok=True)
    b = barra_rey(); b.save(os.path.join(GUI, "rey_barra.png"))
    e = energia(); e.save(os.path.join(GUI, "energia.png"))
    if len(sys.argv) > 1:
        d = sys.argv[1]; os.makedirs(d, exist_ok=True)
        b.resize((256 * 4, 128 * 4), Image.NEAREST).save(os.path.join(d, "rey_barra_x4.png"))
        e.resize((128 * 6, 128 * 6), Image.NEAREST).save(os.path.join(d, "energia_x6.png"))
    print("ok")
