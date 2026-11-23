#!/usr/bin/env python3
"""
Texturas del Espejo de la Sombra (el minimapa) y del libro de la M:
  espejo_marco.png    marco ovalado de obsidiana y plata, gemas rojas y la cresta con el ojo de la Sombra
  espejo_cristal.png  el cristal por encima del mapa: vineta negra y roja y un reflejo
  humo.png            bocanada de humo (blanca: se tine al dibujarla)
  libro.png           el libro abierto: tapas de cuero negro con costuras rojas y dos paginas de pergamino
  libro_pestanas.png  las cintas de las pestanas (normal y elegida)
  espejo_iconos.png   iconos de 16x16 (lugares, cofres, jefes, marcas)
Uso: python3 tools/espejo/texturas.py   (necesita numpy y Pillow)
"""
import math
import os

import numpy as np
from PIL import Image, ImageDraw, ImageFilter

AQUI = os.path.dirname(os.path.abspath(__file__))
SALIDA = os.path.join(AQUI, "..", "..", "src", "main", "resources", "assets", "darkcraft", "textures", "gui")
RNG = np.random.default_rng(1307)


def ruido(h, w, escalas=(4, 8, 16, 32), semilla=0):
    """Ruido fractal suave en [0, 1]."""
    r = np.random.default_rng(semilla)
    total = np.zeros((h, w))
    peso = 0
    for i, s in enumerate(escalas):
        gh, gw = max(2, h // s + 2), max(2, w // s + 2)
        g = r.random((gh, gw))
        img = Image.fromarray((g * 255).astype(np.uint8)).resize((w, h), Image.BICUBIC)
        a = 1.0 / (i + 1)
        total += np.asarray(img, dtype=float) / 255 * a
        peso += a
    total /= peso
    total -= total.min()
    return total / max(1e-6, total.max())


def guardar(arr, nombre):
    arr = np.clip(arr, 0, 255).astype(np.uint8)
    Image.fromarray(arr, "RGBA").save(os.path.join(SALIDA, nombre), optimize=True)
    print("ok", nombre, arr.shape[1], "x", arr.shape[0])


# ---------------------------------------------------------------------------------------------- espejo
# Lienzo del espejo (en pixeles de la textura). Java usa las mismas medidas (EspejoCliente.TEX_*).
TW, TH = 256, 352
CX, CY = 128, 196          # centro del cristal
AI, BI = 90, 128           # semiejes del cristal
AO, BO = 112, 150          # semiejes del borde de fuera del marco
SS = 4                     # supermuestreo


def elipse_t(x, y, a, b):
    return np.sqrt(((x - CX) / a) ** 2 + ((y - CY) / b) ** 2)


def marco():
    W, H = TW * SS, TH * SS
    yy, xx = np.mgrid[0:H, 0:W].astype(float)
    x, y = (xx + 0.5) / SS, (yy + 0.5) / SS
    ti = elipse_t(x, y, AI, BI)
    to = elipse_t(x, y, AO, BO)
    out = np.zeros((H, W, 4))
    # posicion dentro del aro: 0 en el cristal, 1 en el borde de fuera (aprox. por interpolacion de las dos elipses)
    banda = (ti >= 1.0) & (to <= 1.0)
    u = np.clip((ti - 1.0) / np.maximum(1e-6, (ti - 1.0) + (1.0 - to)), 0, 1)
    ang = np.arctan2((y - CY) / BI, (x - CX) / AI)
    n1 = np.asarray(Image.fromarray((ruido(TH, TW, (3, 6, 12), 5) * 255).astype(np.uint8)).resize((W, H), Image.BICUBIC), float) / 255
    # obsidiana: casi negra con brillo morado y luz desde arriba a la izquierda (bisel)
    bis = np.cos((u - 0.5) * math.pi)  # 1 en el centro de la banda
    luz = -np.sin(ang + math.pi * 0.75) * (u - 0.5) * 2  # el lado de fuera arriba-izq y el de dentro abajo-der brillan
    base = np.stack([9 + 9 * n1, 5 + 5 * n1, 12 + 12 * n1], -1)
    brillo = np.clip(0.3 * bis + 0.5 * luz, -0.3, 1)[..., None] * np.array([46, 30, 64])
    # brillos de cristal volcanico: rayas finas que cruzan la banda
    raya = np.exp(-((np.sin(ang * 5 + 1.1) - 0.93) / 0.03) ** 2) * (np.abs(u - 0.45) < 0.3)
    brillo = brillo + raya[..., None] * np.array([90, 70, 120])
    col = base + brillo
    # vetas rojas que brillan (grietas)
    vet = np.abs(np.sin(ang * 7 + n1 * 9 + u * 3)) < 0.035
    col = np.where((vet & (u > 0.25) & (u < 0.75))[..., None], np.array([178, 22, 26]) + n1[..., None] * 60, col)
    # lineas de plata: dentro, fuera y una fina por el medio
    def plata(mask, fuerza=1.0):
        nonlocal col
        sh = 0.65 + 0.35 * np.cos(ang * 2 + 0.9)  # reflejo que cambia alrededor
        p = np.stack([190 * sh + 40, 196 * sh + 40, 210 * sh + 40], -1) * fuerza
        col = np.where(mask[..., None], p, col)
    plata((u < 0.12) | ((u > 0.84) & (u < 0.94)))
    plata((np.abs(u - 0.5) < 0.025) & (np.abs(np.sin(ang * 24)) > 0.35), 0.8)
    # remaches de plata en la linea de fuera
    for k in range(40):
        a = k * 2 * math.pi / 40
        rx, ry = CX + math.cos(a) * (AO - 4.6), CY + math.sin(a) * (BO - 4.6)
        d = np.sqrt((x - rx) ** 2 + (y - ry) ** 2)
        m = d < 1.6
        col = np.where(m[..., None], np.array([235, 236, 240]) * (1.05 - d[..., None] / 3), col)
    alpha = np.where(banda, 255.0, 0.0)
    # borde negro fino por fuera
    borde = (to > 1.0) & (to < 1.0 + 1.6 / BO * 1.0)
    out[..., :3] = np.where(borde[..., None], 6, col)
    out[..., 3] = np.where(borde, 255, alpha)
    img = Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)

    def P(px, py):
        return (px * SS, py * SS)

    plata_c = (214, 218, 228, 255)
    plata_o = (120, 124, 138, 255)
    rojo = (176, 18, 24, 255)
    # volutas de plata en las diagonales
    for k in range(4):
        a0 = math.pi / 4 + k * math.pi / 2
        mx, my = CX + math.cos(a0) * (AI + AO) / 2, CY + math.sin(a0) * (BI + BO) / 2
        for s in (-1, 1):
            pts = []
            for i in range(40):
                t = i / 39
                rr = 7.5 * (1 - t * 0.85)
                aa = a0 + s * (0.2 + t * 4.2)
                pts.append(P(mx + math.cos(aa) * rr + math.cos(a0 + s * 1.57) * 3.5 * (1 - t),
                             my + math.sin(aa) * rr + math.sin(a0 + s * 1.57) * 3.5 * (1 - t)))
            d.line(pts, fill=plata_o, width=int(2.6 * SS), joint="curve")
            d.line(pts, fill=plata_c, width=int(1.4 * SS), joint="curve")
        d.ellipse([P(mx - 3, my - 3), P(mx + 3, my + 3)], fill=rojo, outline=plata_c, width=SS)
    # gemas rojas a los lados y abajo
    for gx, gy, rx, ry in ((CX - (AI + AO) / 2, CY, 5.5, 8), (CX + (AI + AO) / 2, CY, 5.5, 8), (CX, CY + (BI + BO) / 2, 8, 5.5)):
        d.ellipse([P(gx - rx - 2, gy - ry - 2), P(gx + rx + 2, gy + ry + 2)], fill=plata_o)
        d.ellipse([P(gx - rx - 1, gy - ry - 1), P(gx + rx + 1, gy + ry + 1)], fill=plata_c)
        d.ellipse([P(gx - rx, gy - ry), P(gx + rx, gy + ry)], fill=(110, 6, 12, 255))
        d.ellipse([P(gx - rx * 0.7, gy - ry * 0.7), P(gx + rx * 0.5, gy + ry * 0.5)], fill=(196, 22, 30, 255))
        d.ellipse([P(gx - rx * 0.5, gy - ry * 0.6), P(gx - rx * 0.1, gy - ry * 0.15)], fill=(255, 170, 160, 255))
    # colgante de abajo
    by = CY + BO
    d.polygon([P(CX - 9, by - 3), P(CX + 9, by - 3), P(CX, by + 11)], fill=plata_o)
    d.polygon([P(CX - 7, by - 2), P(CX + 7, by - 2), P(CX, by + 8.5)], fill=(20, 12, 28, 255))
    d.line([P(CX - 7, by - 2), P(CX, by + 8.5), P(CX + 7, by - 2)], fill=plata_c, width=SS)
    # cresta de arriba: arco ojival de obsidiana con borde de plata, cuernos y el ojo de la Sombra
    ty = CY - BO
    arco = []
    for i in range(41):
        t = i / 40
        a = math.pi * t
        arco.append((CX - 30 * math.cos(a), ty + 6 - 26 * math.sin(a) ** 0.8 - 8 * math.sin(a) ** 6))
    arco = [(CX - 34, ty + 9)] + arco + [(CX + 34, ty + 9)]
    d.polygon([P(px, py) for px, py in arco], fill=(16, 10, 24, 255), outline=plata_o)
    d.line([P(px, py) for px, py in arco], fill=plata_c, width=int(1.6 * SS), joint="curve")
    # punta
    d.polygon([P(CX - 3.5, ty - 26), P(CX + 3.5, ty - 26), P(CX, ty - 37)], fill=plata_c)
    # cuernos
    for s in (-1, 1):
        izq, der = [], []
        for i in range(31):
            t = i / 30
            px, py = CX + s * (27 + 17 * t), ty + 6 - 24 * t * t - 6 * t
            # grosor que se afila hacia la punta, perpendicular a la curva
            dxc, dyc = s * 17, -48 * t - 6
            L = (dxc * dxc + dyc * dyc) ** 0.5
            nx, ny = -dyc / L, dxc / L
            g = 3.6 * (1 - t) + 0.35
            izq.append((px + nx * g, py + ny * g))
            der.append((px - nx * g, py - ny * g))
        cuerno = izq + der[::-1]
        d.polygon([P(px, py) for px, py in cuerno], fill=plata_o)
        izq2 = [(px * 0.6 + qx * 0.4, py * 0.6 + qy * 0.4) for (px, py), (qx, qy) in zip(izq, der)]
        d.line([P(px, py) for px, py in izq2], fill=plata_c, width=int(1.3 * SS), joint="curve")
    # el ojo: almendra roja con pupila de rendija
    ex, ey = CX, ty - 8
    ojo = []
    for i in range(60):
        a = i / 60 * 2 * math.pi
        ojo.append(P(ex + 15 * math.cos(a), ey + 7.5 * math.sin(a) * (1 - 0.35 * abs(math.cos(a)) ** 3)))
    d.polygon(ojo, fill=(70, 2, 6, 255), outline=plata_c)
    iris = [P(ex + 6.5 * math.cos(i / 40 * 6.283), ey + 6.5 * math.sin(i / 40 * 6.283)) for i in range(40)]
    d.polygon(iris, fill=(210, 28, 34, 255))
    d.ellipse([P(ex - 4, ey - 4), P(ex + 4, ey + 4)], fill=(250, 90, 60, 255))
    d.polygon([P(ex, ey - 6.5), P(ex + 1.6, ey), P(ex, ey + 6.5), P(ex - 1.6, ey)], fill=(8, 0, 0, 255))
    d.ellipse([P(ex - 4.5, ey - 4), P(ex - 2, ey - 1.8)], fill=(255, 220, 210, 230))
    img = img.resize((TW, TH), Image.LANCZOS)
    # sombra suave alrededor (para que se lea sobre cualquier fondo)
    a = np.asarray(img).astype(float)
    sombra = Image.fromarray(a[..., 3].astype(np.uint8)).filter(ImageFilter.GaussianBlur(3.2))
    s = np.asarray(sombra, float) * 0.55
    res = np.zeros_like(a)
    fa = a[..., 3:] / 255
    res[..., :3] = a[..., :3] * fa
    res[..., 3] = a[..., 3] + s * (1 - a[..., 3] / 255)
    # el agujero del cristal sin sombra
    yy2, xx2 = np.mgrid[0:TH, 0:TW].astype(float)
    dentro = elipse_t(xx2 + 0.5, yy2 + 0.5, AI - 0.6, BI - 0.6) < 1
    res[..., 3] = np.where(dentro, 0, res[..., 3])
    fa2 = np.maximum(res[..., 3:], 1) / 255
    res[..., :3] = np.where(res[..., 3:] > 0, res[..., :3] / fa2, 0)
    guardar(res, "espejo_marco.png")


def cristal():
    yy, xx = np.mgrid[0:TH, 0:TW].astype(float)
    x, y = xx + 0.5, yy + 0.5
    t = elipse_t(x, y, AI + 0.5, BI + 0.5)
    out = np.zeros((TH, TW, 4))
    # vineta: negro rojizo hacia el borde
    v = np.clip((t - 0.5) / 0.5, 0, 1) ** 1.8
    n = ruido(TH, TW, (8, 16, 32), 11)
    out[..., 0] = 26 + 40 * n
    out[..., 1] = 2
    out[..., 2] = 6
    out[..., 3] = 235 * v
    # anillo rojo muy fino justo en el borde
    r = np.exp(-((t - 0.985) / 0.02) ** 2)
    out[..., 0] = out[..., 0] * (1 - r) + 150 * r
    out[..., 3] = np.maximum(out[..., 3], 160 * r)
    # reflejo diagonal
    u = ((x - CX) / AI + (y - CY) / BI * 0.8)
    refl = np.exp(-((u + 0.55) / 0.13) ** 2) * np.clip(1 - t, 0, 1) * 0.9
    punto = np.exp(-(((x - (CX - AI * 0.45)) / 10) ** 2 + ((y - (CY - BI * 0.55)) / 16) ** 2))
    blanco = np.clip(refl * 34 + punto * 52, 0, 255)
    a = out[..., 3]
    tot = a + blanco
    for c in range(3):
        out[..., c] = np.where(tot > 0, (out[..., c] * a + 235 * blanco) / np.maximum(tot, 1e-6), 0)
    out[..., 3] = np.clip(tot, 0, 255)
    out[..., 3] = np.where(t > 1.0, 0, out[..., 3])
    guardar(out, "espejo_cristal.png")


def humo():
    S = 64
    yy, xx = np.mgrid[0:S, 0:S].astype(float)
    d = np.sqrt((xx - S / 2 + 0.5) ** 2 + (yy - S / 2 + 0.5) ** 2) / (S / 2)
    n = ruido(S, S, (4, 8, 16), 23)
    a = np.clip(1 - d ** 1.6, 0, 1) * (0.45 + 0.55 * n)
    a = np.clip(a * 1.25, 0, 1) ** 1.3
    out = np.zeros((S, S, 4))
    out[..., :3] = 255
    out[..., 3] = a * 255
    guardar(out, "humo.png")


# ---------------------------------------------------------------------------------------------- libro
LW, LH = 512, 320


def libro():
    W, H = LW, LH
    yy, xx = np.mgrid[0:H, 0:W].astype(float)
    out = np.zeros((H, W, 4))
    # tapas de cuero negro (rectangulo con esquinas redondeadas)
    r = 14
    dx = np.maximum(np.maximum(r - xx, xx - (W - 1 - r)), 0)
    dy = np.maximum(np.maximum(r - yy, yy - (H - 1 - r)), 0)
    dentro = (dx ** 2 + dy ** 2) <= r * r
    n = ruido(H, W, (2, 4, 8, 24), 31)
    grano = ruido(H, W, (1, 2), 32)
    cuero = np.stack([22 + 22 * n + 10 * grano, 12 + 10 * n + 4 * grano, 14 + 12 * n + 4 * grano], -1)
    # borde de las tapas un poco mas claro (desgastado)
    bord = np.clip(1 - np.minimum(np.minimum(xx, W - 1 - xx), np.minimum(yy, H - 1 - yy)) / 10, 0, 1)
    cuero += bord[..., None] * np.array([26, 12, 12])
    out[..., :3] = cuero
    out[..., 3] = np.where(dentro, 255, 0)
    img = Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGBA")
    d = ImageDraw.Draw(img)
    # costura roja alrededor
    m = 7
    for (x0, y0, x1, y1) in ((m, m, W - m, m), (W - m, m, W - m, H - m), (W - m, H - m, m, H - m), (m, H - m, m, m)):
        L = math.hypot(x1 - x0, y1 - y0)
        n_p = int(L // 6)
        for i in range(n_p):
            a0, a1 = i / n_p, (i + 0.55) / n_p
            d.line([(x0 + (x1 - x0) * a0, y0 + (y1 - y0) * a0), (x0 + (x1 - x0) * a1, y0 + (y1 - y0) * a1)],
                   fill=(150, 20, 24, 255), width=1)
    # esquinas de plata
    for (cx, cy, sx, sy) in ((0, 0, 1, 1), (W - 1, 0, -1, 1), (0, H - 1, 1, -1), (W - 1, H - 1, -1, -1)):
        tri = [(cx, cy), (cx + sx * 30, cy), (cx, cy + sy * 30)]
        d.polygon(tri, fill=(132, 136, 150, 255))
        d.line([(cx + sx * 30, cy), (cx, cy + sy * 30)], fill=(220, 224, 234, 255), width=2)
        d.ellipse([cx + sx * 9 - 3, cy + sy * 9 - 3, cx + sx * 9 + 3, cy + sy * 9 + 3], fill=(230, 232, 240, 255))
    out = np.asarray(img).astype(float)
    # paginas de pergamino con el canto de las hojas a los lados
    pn = ruido(H, W, (3, 6, 12, 40), 41)
    manchas = ruido(H, W, (20, 40), 42)
    px0, px1, py0, py1, gut = 20, W - 20, 13, H - 13, W // 2
    for lado in (0, 1):
        x0 = px0 if lado == 0 else gut + 2
        x1 = gut - 2 if lado == 0 else px1
        m = (xx >= x0) & (xx < x1) & (yy >= py0) & (yy < py1)
        base = np.stack([232 - 26 * pn - 30 * manchas, 214 - 30 * pn - 34 * manchas, 170 - 36 * pn - 40 * manchas], -1)
        # bordes de la pagina mas oscuros (quemados)
        e = np.minimum(np.minimum(xx - x0, x1 - 1 - xx), np.minimum(yy - py0, py1 - 1 - yy))
        q = np.clip(1 - e / 9, 0, 1) ** 2
        base = base * (1 - 0.45 * q[..., None]) + np.array([70, 40, 20]) * 0.45 * q[..., None]
        # sombra del lomo
        g = np.abs(xx - gut) / 40
        sombra = np.clip(1 - g, 0, 1) ** 2.2
        base = base * (1 - 0.55 * sombra[..., None])
        out[..., :3] = np.where(m[..., None], base, out[..., :3])
        out[..., 3] = np.where(m, 255, out[..., 3])
        # canto de las hojas (rayas) por fuera
        cx0 = x0 - 5 if lado == 0 else x1
        canto = (xx >= cx0) & (xx < cx0 + 5) & (yy >= py0 + 2) & (yy < py1 - 2)
        rayas = 196 - 40 * ((yy.astype(int) % 2) == 0)
        out[..., 0] = np.where(canto, rayas, out[..., 0])
        out[..., 1] = np.where(canto, rayas * 0.88, out[..., 1])
        out[..., 2] = np.where(canto, rayas * 0.66, out[..., 2])
    # linea del lomo
    lomo = (np.abs(xx - gut) < 2) & (yy >= py0) & (yy < py1)
    out[..., :3] = np.where(lomo[..., None], np.array([60, 34, 22]), out[..., :3])
    guardar(out, "libro.png")


def pestanas():
    """Dos cintas de 72x22 (normal arriba, elegida abajo): rojo oscuro con borde de plata y punta en cola de golondrina."""
    W, H = 72, 44
    img = Image.new("RGBA", (W * SS, H * SS), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for fila, (c1, c2) in enumerate((((70, 10, 14), (110, 18, 24)), ((150, 18, 24), (205, 36, 40)))):
        y0 = fila * 22
        poly = [(0, y0 + 2), (W - 10, y0 + 2), (W - 1, y0 + 11), (W - 10, y0 + 20), (0, y0 + 20)]
        d.polygon([(x * SS, y * SS) for x, y in poly], fill=c1 + (255,))
        inner = [(0, y0 + 4), (W - 12, y0 + 4), (W - 4.5, y0 + 11), (W - 12, y0 + 18), (0, y0 + 18)]
        d.polygon([(x * SS, y * SS) for x, y in inner], fill=c2 + (255,))
        d.line([(x * SS, y * SS) for x, y in poly[:4] + [poly[4]]], fill=(200, 204, 216, 255), width=SS)
        # bordado
        for i in range(6, W - 14, 5):
            d.line([(i * SS, (y0 + 6) * SS), ((i + 2) * SS, (y0 + 6) * SS)], fill=(230, 180, 120, 200), width=SS // 2)
            d.line([(i * SS, (y0 + 16) * SS), ((i + 2) * SS, (y0 + 16) * SS)], fill=(230, 180, 120, 200), width=SS // 2)
    img = img.resize((W, H), Image.LANCZOS)
    img.save(os.path.join(SALIDA, "libro_pestanas.png"), optimize=True)
    print("ok libro_pestanas.png", W, "x", H)


if __name__ == "__main__":
    os.makedirs(SALIDA, exist_ok=True)
    marco()
    cristal()
    humo()
    libro()
    pestanas()
    import iconos
    iconos.main(SALIDA)
