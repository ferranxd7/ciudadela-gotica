"""Texturas del ascensor de hueso de la fortaleza (placa cuadrada 96x96 arriba, canto en las filas 96..103)."""
import random, sys
from PIL import Image

random.seed(7)
W = 128
tex = Image.new('RGBA', (W, W), (0, 0, 0, 0))
glo = Image.new('RGBA', (W, W), (0, 0, 0, 255))
T = tex.load(); G = glo.load()

def hueso(x, y):
    n = random.randint(-9, 9)
    b = (214 + n, 203 + n, 172 + n)
    # vetas
    if (x * 7 + y * 3) % 23 == 0 and random.random() < 0.5: b = (190, 176, 146)
    return b + (255,)

S = 96
for y in range(S):
    for x in range(S):
        T[x, y] = hueso(x, y)
# marco de piedra negra (4) con un filete rojo oscuro por dentro
for y in range(S):
    for x in range(S):
        d = min(x, y, S - 1 - x, S - 1 - y)
        if d < 4: T[x, y] = (40 + random.randint(-4, 4), 34, 42, 255)
        elif d == 4: T[x, y] = (104, 22, 18, 255); G[x, y] = (70, 12, 8, 255)

# glifos de runas de 3x5
GL = ["101111010010010", "010111101010010", "100110101110100", "010011110010010", "110010010010011",
      "001010111010100", "010101010101101", "001010100010001", "010100010001010", "101111010111101"]

def runa(cx, cy, k, col, gcol):
    g = GL[k % len(GL)]
    for j in range(5):
        for i in range(3):
            if g[j * 3 + i] == '1':
                T[cx + i - 1, cy + j - 2] = col; G[cx + i - 1, cy + j - 2] = gcol

def cuadro(d, col, gcol):
    for t in range(d, S - d):
        for (x, y) in ((t, d), (t, S - 1 - d), (d, t), (S - 1 - d, t)):
            T[x, y] = col; G[x, y] = gcol

# dos cuadros de runas
cuadro(11, (92, 26, 20, 255), (190, 36, 24, 255))
cuadro(28, (92, 26, 20, 255), (150, 28, 18, 255))
k = 0
for t in range(19, S - 19, 10):
    for (x, y) in ((t, 19), (t, S - 20), (19, t), (S - 20, t)):
        runa(x, y, k, (110, 30, 22, 255), (255, 92, 52, 255)); k += 3
# costillas en diagonal (de las esquinas al cuadro de dentro)
for i in range(5, 29):
    for (x, y) in ((i, i), (S - 1 - i, i), (i, S - 1 - i), (S - 1 - i, S - 1 - i)):
        if T[x, y][0] > 150: T[x, y] = (150, 136, 108, 255)

# la calavera del centro
cx, cy = 48, 46
for y in range(cy - 14, cy + 16):
    for x in range(cx - 13, cx + 13):
        dx, dy = (x - cx + 0.5) / 12.5, (y - cy + 2) / 12.5
        dentro = dx * dx + dy * dy < 1 and y < cy + 7 or (abs(x - cx + 0.5) < 7.5 and cy + 6 <= y < cy + 15)
        if not dentro: continue
        T[x, y] = (232, 224, 200, 255)
# contorno
pts = [(x, y) for y in range(cy - 16, cy + 18) for x in range(cx - 15, cx + 15) if T[x, y][:3] == (232, 224, 200)]
ps = set(pts)
for (x, y) in pts:
    for (a, b) in ((1, 0), (-1, 0), (0, 1), (0, -1)):
        if (x + a, y + b) not in ps: T[x, y] = (70, 52, 40, 255); break
# cuencas, nariz y dientes
for (ex, ey) in ((cx - 6, cy), (cx + 5, cy)):
    for y in range(ey - 3, ey + 3):
        for x in range(ex - 3, ex + 3):
            if (x - ex + 0.5) ** 2 + (y - ey + 0.5) ** 2 <= 9:
                T[x, y] = (34, 10, 10, 255)
                G[x, y] = (255, 70, 40, 255) if (x - ex + 0.5) ** 2 + (y - ey + 0.5) ** 2 <= 3 else (150, 20, 12, 255)
for (x, y) in ((cx - 1, cy + 5), (cx, cy + 5), (cx - 1, cy + 6), (cx, cy + 6), (cx, cy + 4)):
    T[x, y] = (40, 16, 14, 255)
for x in range(cx - 6, cx + 6):
    if x % 2 == 0:
        for y in range(cy + 10, cy + 14): T[x, y] = (60, 40, 32, 255)

# el canto (filas 96..103): hueso con marcas
for y in range(96, 104):
    for x in range(W):
        T[x, y] = hueso(x, y) if 97 <= y <= 102 else (40, 34, 42, 255)
        if 97 <= y <= 102 and x % 16 == 8: T[x, y] = (104, 22, 18, 255); G[x, y] = (190, 36, 24, 255)
    if y in (96, 103):
        for x in range(W): G[x, y] = (60, 10, 6, 255)

out = sys.argv[1]
tex.save(out + '/ascensor_hueso.png')
glo.save(out + '/ascensor_hueso_brillo.png')
