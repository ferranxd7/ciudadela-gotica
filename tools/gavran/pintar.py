"""Pinta las texturas de Ser Gavran (procedural, con numpy).

Fase 1: armadura negra (acero viejo, biselado, remaches, grabados de ramas) por la que asoma la corteza, raices
enroscadas, musgo en la hombrera izquierda, capa y tabardo granates hechos jirones con ribete de oro, yelmo con
corona de espinas y astas de rama, ojos verdes, el corazon del arbol en el pecho y un mandoble negro con runas
verdes. Fase 2 (parte B2): la corteza y las espinas cubren casi toda la armadura y el corazon brilla mucho.

Por cada fase salen dos imagenes: la base (con transparencia: jirones, hojas) y la de brillo (lo que se ve en la
oscuridad: ojos, corazon, runas, esporas).

Orientacion de cada cara en su rectangulo (como Minecraft, vista desde fuera de la caja):
  front (-Z): izquierda = -X (derecha del caballero)   back (+Z): izquierda = +X
  right (-X): columna 0 = +Z (detras)                   left (+X): columna 0 = -Z (delante)
  top: fila 0 = +Z, ultima fila = -Z                    bottom: fila 0 = +Z
  En la hoja del mandoble (largo en -Z): en "right" la punta esta a la derecha, en "left" a la izquierda.
"""
import math

import numpy as np

from partes import PARTS, SCALE, TEX_W, TEX_H

S = SCALE

# ------------------------------------------------------------------ utilidades


def rng_for(*keys):
    h = 2166136261
    for k in keys:
        for ch in str(k):
            h = ((h ^ ord(ch)) * 16777619) & 0xFFFFFFFF
    return np.random.default_rng(h)


def vnoise(W, H, cx, cy, rng):
    """Ruido de valor suave, con celdas de cx x cy texeles (se pueden estirar)."""
    gw, gh = int(math.ceil(W / cx)) + 2, int(math.ceil(H / cy)) + 2
    grid = rng.random((gh, gw))
    ys = np.arange(H) / cy
    xs = np.arange(W) / cx
    y0 = np.floor(ys).astype(int)
    x0 = np.floor(xs).astype(int)
    fy = (ys - y0)[:, None]
    fx = (xs - x0)[None, :]
    fy = fy * fy * (3 - 2 * fy)
    fx = fx * fx * (3 - 2 * fx)
    a = grid[y0][:, x0]
    b = grid[y0][:, x0 + 1]
    c = grid[y0 + 1][:, x0]
    d = grid[y0 + 1][:, x0 + 1]
    return (a * (1 - fx) + b * fx) * (1 - fy) + (c * (1 - fx) + d * fx) * fy


def fbm(W, H, rng, cell=6.0, octaves=3, stretch=(1.0, 1.0)):
    out = np.zeros((H, W))
    amp, tot = 1.0, 0.0
    for _ in range(octaves):
        out += amp * vnoise(W, H, max(1.0, cell * stretch[0]), max(1.0, cell * stretch[1]), rng)
        tot += amp
        amp *= 0.5
        cell /= 2
    return out / tot


def col(*c):
    return np.array(c, dtype=float) / 255.0


def mix(a, b, t):
    t = np.clip(t, 0, 1)
    if np.ndim(t) == 2:
        t = t[..., None]
    return a * (1 - t) + b * t


def rect(img, x0, y0, x1, y1, c, alpha=1.0):
    H, W = img.shape[:2]
    x0, x1 = max(0, int(round(x0))), min(W, int(round(x1)))
    y0, y1 = max(0, int(round(y0))), min(H, int(round(y1)))
    if x1 > x0 and y1 > y0:
        img[y0:y1, x0:x1, :3] = img[y0:y1, x0:x1, :3] * (1 - alpha) + np.asarray(c)[:3] * alpha


def glow_at(gl, mask, c, a=1.0):
    """Pone brillo (color c * a) donde mask > 0 (mask 0..1)."""
    m = np.clip(mask, 0, 1) * a
    gl[..., :3] = np.maximum(gl[..., :3], np.asarray(c)[None, None, :3] * m[..., None])
    gl[..., 3] = np.maximum(gl[..., 3], (m > 0.04).astype(float))


def cracks(W, H, rng, n=7, width=1.0):
    """Mapa de grietas (Voronoi: cerca del borde entre celdas = 1)."""
    pts = rng.random((n, 2)) * [W, H]
    ys, xs = np.mgrid[0:H, 0:W]
    d = np.stack([np.hypot(xs - px, ys - py) for px, py in pts], axis=-1)
    d.sort(axis=-1)
    edge = d[..., 1] - d[..., 0]
    return np.clip(1 - edge / width, 0, 1)


def ellipse(W, H, cx, cy, rx, ry):
    ys, xs = np.mgrid[0:H, 0:W]
    return np.clip(1 - np.hypot((xs + 0.5 - cx) / max(rx, 1e-3), (ys + 0.5 - cy) / max(ry, 1e-3)), 0, 1)


def line(img, x0, y0, x1, y1, c, alpha=1.0, w=1):
    n = int(max(abs(x1 - x0), abs(y1 - y0)) * 1.5) + 1
    for i in range(n + 1):
        t = i / max(1, n)
        x = x0 + (x1 - x0) * t
        y = y0 + (y1 - y0) * t
        rect(img, x - w / 2, y - w / 2, x + w / 2, y + w / 2, c, alpha)


# ------------------------------------------------------------------ colores

STEEL = col(38, 42, 48)
STEEL_D = col(20, 22, 27)
STEEL_DD = col(10, 11, 14)
STEEL_L = col(78, 86, 96)
STEEL_E = col(118, 126, 138)
STEEL_S = col(168, 176, 188)
PATINA = col(38, 52, 44)

BARK = col(70, 53, 38)
BARK_D = col(44, 33, 24)
BARK_DD = col(22, 16, 11)
BARK_L = col(104, 82, 58)
LICHEN = col(118, 128, 96)

ROOT = col(112, 86, 60)
ROOT_D = col(76, 57, 39)
ROOT_DD = col(46, 34, 23)
ROOT_L = col(146, 118, 84)

MOSS = col(72, 104, 44)
MOSS_D = col(44, 70, 28)
MOSS_DD = col(28, 46, 20)
MOSS_L = col(110, 146, 60)

LEAF = col(64, 112, 44)
LEAF_D = col(40, 76, 30)
LEAF_L = col(98, 152, 62)
LEAF_V = col(132, 176, 84)

CLOTH = col(78, 18, 26)
CLOTH_D = col(48, 10, 16)
CLOTH_DD = col(28, 6, 10)
CLOTH_L = col(108, 30, 38)

GOLD = col(176, 132, 46)
GOLD_D = col(108, 76, 22)
GOLD_L = col(222, 182, 88)

LEATHER = col(56, 38, 27)
LEATHER_D = col(32, 22, 16)
LEATHER_L = col(82, 58, 40)

GREEN_GLOW = col(110, 255, 140)
GREEN_CORE = col(225, 255, 228)
GREEN_SOFT = col(60, 200, 90)
EYE = col(170, 255, 180)

# ------------------------------------------------------------------ materiales base


def acero(W, H, rng, rngs, liso=False):
    """Placa de acero negro viejo: degradado (arriba mas claro), ruido fino, aranazos y algo de patina."""
    n = fbm(W, H, rngs, 7 * S / 2, 3)
    fine = rngs.random((H, W))
    ys = np.linspace(0, 1, H)[:, None]
    t = 0.55 + 0.3 * (n - 0.5) + 0.05 * fine - 0.3 * ys
    img = mix(STEEL_D, STEEL, t)
    # brillo de metal: una banda clara y suave hacia arriba (placa abombada)
    sheen = np.exp(-((ys - 0.28) / 0.16) ** 2)
    img = mix(img, STEEL_L, sheen * (0.35 + 0.25 * n))
    img = mix(img, STEEL_E, np.clip((n - 0.8) * 3, 0, 0.35) * sheen)
    if not liso:
        # aranazos finos (claros) en diagonal, pocos
        for _ in range(int(W * H / (140 * S * S))):
            x0, y0 = rngs.uniform(0, W), rngs.uniform(0, H)
            ln = rngs.uniform(1.5, 4) * S
            a = rngs.uniform(-0.9, 0.9) + (0 if rngs.random() < 0.5 else math.pi)
            line(img, x0, y0, x0 + math.cos(a) * ln, y0 + math.sin(a) * ln * 0.5, STEEL_E, 0.25)
        # patina verdosa en los huecos (abajo)
        p = fbm(W, H, rngs, 5 * S / 2, 2)
        img = mix(img, PATINA, np.clip((p - 0.62) * 2.5, 0, 0.55) * np.clip(ys * 1.4, 0, 1))
    return img


def bisel(img, top=True, left=True, bottom=True, right=True, k=1.0):
    """Borde de placa: luz arriba e izquierda, sombra abajo y derecha (1 texel), y un hilo oscuro por fuera."""
    H, W = img.shape[:2]
    if top:
        img[0:1, :] = mix(img[0:1, :], STEEL_E, 0.7 * k)
    if left:
        img[:, 0:1] = mix(img[:, 0:1], STEEL_L, 0.45 * k)
    if bottom:
        img[H - 1:H, :] = mix(img[H - 1:H, :], STEEL_DD, 0.6 * k)
    if right:
        img[:, W - 1:W] = mix(img[:, W - 1:W], STEEL_D, 0.45 * k)
    return img


def remaches(img, gl, y, x0, x1, paso, rng=None):
    """Fila de remaches (2x2 texeles: luz arriba a la izquierda, sombra abajo a la derecha)."""
    H, W = img.shape[:2]
    x = x0
    while x <= x1 - 1:
        xi, yi = int(round(x)), int(round(y))
        if 0 <= yi < H - 1 and 0 <= xi < W - 1:
            img[yi + 1, xi + 1] = STEEL_DD
            img[yi, xi + 1] = STEEL_D
            img[yi + 1, xi] = STEEL_D
            img[yi, xi] = STEEL_S
        x += paso


def grabado(img, rngs, alpha=0.6, densidad=1.0, color=None):
    """Grabado de ramas y hojas en la placa (lineas oscuras finas con algun brillo): una enredadera."""
    H, W = img.shape[:2]
    c = STEEL_DD if color is None else color
    n = max(1, int(W / (9 * S) * densidad + 0.5))
    for k in range(n):
        x = (k + 0.5) * W / n + rngs.uniform(-1, 1) * S
        amp = rngs.uniform(1.0, 2.2) * S
        fr = rngs.uniform(0.18, 0.3) / S
        ph = rngs.uniform(0, 6.28)
        prev = None
        for y in range(H):
            xx = x + math.sin(y * fr + ph) * amp
            if prev is not None:
                line(img, prev[0], prev[1], xx, y, c, alpha)
            prev = (xx, y)
            # hojitas a los lados
            if y % int(5 * S) == int(2 * S):
                sgn = 1 if (y // int(5 * S)) % 2 == 0 else -1
                line(img, xx, y, xx + sgn * 2.2 * S, y - 1.4 * S, c, alpha * 0.9)
                line(img, xx + sgn * 2.2 * S, y - 1.4 * S, xx + sgn * 1.0 * S, y - 0.2 * S, c, alpha * 0.7)


def corteza(W, H, rngs, vertical=True, oscuro=0.0):
    """Corteza: fibras a lo largo, grietas profundas, crestas claras y liquen."""
    st = (0.35, 2.6) if vertical else (2.6, 0.35)
    n = fbm(W, H, rngs, 5 * S / 2, 3, stretch=st)
    f = fbm(W, H, rngs, 2 * S / 2, 2, stretch=st)
    img = mix(BARK_D, BARK, 0.35 + 0.65 * n)
    # grietas: donde el ruido estirado cruza un umbral
    g = np.abs(fbm(W, H, rngs, 4 * S / 2, 2, stretch=st) - 0.5)
    deep = np.clip(1 - g / 0.045, 0, 1)
    img = mix(img, BARK_DD, deep * 0.9)
    img = mix(img, BARK_L, np.clip((f - 0.66) * 3.5, 0, 0.8) * (1 - deep))
    # liquen y musgo en manchas
    m = fbm(W, H, rngs, 3 * S / 2, 2)
    img = mix(img, LICHEN, np.clip((m - 0.74) * 5, 0, 0.55))
    img = mix(img, MOSS_D, np.clip((m - 0.2) * -6, 0, 0.6))
    if oscuro:
        img = img * (1 - oscuro)
    return img


def raiz_tex(W, H, rngs, a_lo_largo_y=True):
    """Raiz: fibras retorcidas en diagonal, luz en el centro y sombra en los bordes (como un cilindro)."""
    ys, xs = np.mgrid[0:H, 0:W]
    if a_lo_largo_y:
        across = (xs + 0.5) / W
        along = ys
    else:
        across = (ys + 0.5) / H
        along = xs
    tw = rngs.uniform(0.25, 0.45)
    fib = np.sin((along * tw + across * W * 0.9 / S) * 1.6 + fbm(W, H, rngs, 3 * S / 2, 2) * 4)
    img = mix(ROOT_D, ROOT, 0.5 + 0.35 * fib)
    img = mix(img, ROOT_L, np.clip(fib - 0.6, 0, 1) * 1.2)
    shade = 1 - np.abs(across - 0.45) * 1.1
    img = img * np.clip(0.55 + 0.6 * shade, 0.4, 1.1)[..., None]
    # nudos
    for _ in range(max(1, int(W * H / (40 * S * S)))):
        cx, cy = rngs.uniform(0, W), rngs.uniform(0, H)
        e = ellipse(W, H, cx, cy, 1.0 * S, 1.0 * S)
        img = mix(img, ROOT_DD, np.clip(e * 3, 0, 1) * 0.8)
        img = mix(img, ROOT_L, np.clip(ellipse(W, H, cx - 0.4 * S, cy - 0.4 * S, 0.5 * S, 0.5 * S) * 3, 0, 1) * 0.5)
    return img


def musgo_tex(W, H, rngs, gl, esporas=True):
    n = fbm(W, H, rngs, 3 * S / 2, 3)
    fine = rngs.random((H, W))
    img = mix(MOSS_DD, MOSS, 0.3 + 0.6 * n + 0.2 * (fine - 0.5))
    img = mix(img, MOSS_L, np.clip((fine - 0.86) * 6, 0, 1) * (n > 0.45))
    img = mix(img, MOSS_DD, np.clip((0.12 - fine) * 8, 0, 1))
    if esporas:
        # esporas que brillan (puntitos verdes)
        m = (fine > 0.992) & (n > 0.5)
        img[m] = col(190, 255, 170)
        glow_at(gl, m.astype(float), col(150, 255, 150), 0.9)
    return img


def tela(W, H, rngs, oscura=False):
    """Pano granate: trama, pliegues verticales y manchas."""
    ys, xs = np.mgrid[0:H, 0:W]
    n = fbm(W, H, rngs, 5 * S / 2, 3)
    weave = ((xs + ys) % 2 == 0) * 0.06 - 0.03
    folds = np.sin(xs / W * math.pi * rngs.uniform(2.5, 4.5) + rngs.uniform(0, 6)) * 0.12
    t = 0.5 + 0.35 * (n - 0.5) + weave + folds
    img = mix(CLOTH_D, CLOTH, t)
    img = mix(img, CLOTH_L, np.clip(folds * 4 - 0.25, 0, 0.5))
    img = mix(img, CLOTH_DD, np.clip(-folds * 4 - 0.3, 0, 0.6))
    stain = fbm(W, H, rngs, 4 * S / 2, 2)
    img = mix(img, col(30, 14, 12), np.clip((stain - 0.7) * 3, 0, 0.6))
    if oscura:
        img = img * 0.78
    return img


def jirones(alpha, rngs, prof=3.0, abajo=True):
    """Borde de abajo hecho jirones (corta la transparencia) y algun agujero."""
    H, W = alpha.shape
    if not abajo:
        return
    base = H - 1
    x = 0
    while x < W:
        ancho = int(rngs.integers(max(2, int(0.8 * S)), max(3, int(2.6 * S))))
        prof_k = rngs.uniform(0.2, 1.0) * prof * S
        for i in range(ancho):
            xx = x + i
            if xx >= W:
                break
            t = abs(i - ancho / 2) / (ancho / 2 + 1e-6)
            cut = int(prof_k * (1 - t * t))
            if cut > 0:
                alpha[max(0, base - cut + 1):, xx] = 0
        x += ancho
    # agujeros
    for _ in range(int(W * H / (70 * S * S))):
        cx, cy = rngs.uniform(0, W), rngs.uniform(H * 0.4, H * 0.95)
        e = ellipse(W, H, cx, cy, rngs.uniform(0.4, 1.0) * S, rngs.uniform(0.6, 1.4) * S)
        alpha[e > 0.2] = 0


def ribete(img, rngs, lados=("top",), ancho=1.0):
    H, W = img.shape[:2]
    b = int(round(ancho * S))
    for lado in lados:
        if lado == "top":
            rect(img, 0, 0, W, b, GOLD)
            rect(img, 0, 0, W, 1, GOLD_L)
            rect(img, 0, b - 1, W, b, GOLD_D)
        elif lado == "left":
            rect(img, 0, 0, b, H, GOLD)
            rect(img, 0, 0, 1, H, GOLD_L)
        elif lado == "right":
            rect(img, W - b, 0, W, H, GOLD)
            rect(img, W - 1, 0, W, H, GOLD_D)
        elif lado == "bottom":
            rect(img, 0, H - b, W, H, GOLD)
            rect(img, 0, H - b, W, H - b + 1, GOLD_L)


def emblema_arbol(img, cx, cy, sc, color=GOLD, sombra=GOLD_D):
    """El Arbol de la Corona bordado: tronco, ramas y una corona encima (sc = medio ancho en texeles)."""
    # tronco
    rect(img, cx - 0.12 * sc, cy - 0.1 * sc, cx + 0.12 * sc, cy + 0.9 * sc, color)
    rect(img, cx + 0.06 * sc, cy - 0.1 * sc, cx + 0.12 * sc, cy + 0.9 * sc, sombra)
    # raices
    for d in (-1, 1):
        line(img, cx, cy + 0.85 * sc, cx + d * 0.45 * sc, cy + 1.05 * sc, color, 1, max(1, sc * 0.08))
    # ramas
    for d in (-1, 1):
        line(img, cx, cy + 0.2 * sc, cx + d * 0.55 * sc, cy - 0.25 * sc, color, 1, max(1, sc * 0.09))
        line(img, cx + d * 0.3 * sc, cy, cx + d * 0.75 * sc, cy + 0.05 * sc, color, 1, max(1, sc * 0.07))
        line(img, cx, cy - 0.05 * sc, cx + d * 0.25 * sc, cy - 0.55 * sc, color, 1, max(1, sc * 0.07))
    # copa: puntos
    for k in range(9):
        a = math.pi * (k / 8.0)
        rect(img, cx - math.cos(a) * 0.7 * sc - 0.07 * sc, cy - 0.35 * sc - math.sin(a) * 0.45 * sc - 0.07 * sc,
             cx - math.cos(a) * 0.7 * sc + 0.07 * sc, cy - 0.35 * sc - math.sin(a) * 0.45 * sc + 0.07 * sc, color)
    # corona
    y = cy - 0.95 * sc
    rect(img, cx - 0.35 * sc, y, cx + 0.35 * sc, y + 0.16 * sc, color)
    for d in (-0.32, 0, 0.32):
        rect(img, cx + d * sc - 0.05 * sc, y - 0.2 * sc, cx + d * sc + 0.05 * sc, y, color)


def hoja_forma(W, H, rngs):
    """Mascara (0/1) de una hoja vista desde arriba (ovalada, punta hacia -Z = abajo en la textura) y su color."""
    ys, xs = np.mgrid[0:H, 0:W]
    u = (xs + 0.5) / W * 2 - 1
    v = (ys + 0.5) / H * 2 - 1
    # forma de hoja: mas ancha arriba, punta abajo
    ancho = 0.95 * np.sqrt(np.clip(1 - v * v, 0, 1)) * (1 - 0.35 * np.clip(v, 0, 1))
    mask = (np.abs(u) < ancho) & (np.abs(v) < 0.98)
    img = mix(LEAF_D, LEAF, 0.4 + 0.4 * rngs.random((H, W)))
    img = mix(img, LEAF_L, np.clip(0.4 - np.abs(u) - v * 0.3, 0, 1))
    vena = np.abs(u) < 0.09
    img[vena] = LEAF_V
    return img, mask


# ------------------------------------------------------------------ pintor por material

# materiales que llevan bisel de placa
PLACAS = {"peto", "faldon_malla", "gorjal", "lama", "espaldar", "quijote", "quijote_i", "greba", "greba_i", "escarpe",
          "escarpe_punta", "rodillera", "rodillera_aleta", "brazal", "hombrera", "hombrera_corteza", "hombrera_tapa",
          "hombrera_tapa_corteza", "hombrera_lama", "hombrera_lama2", "gola", "avambrazo", "codera", "codera_aleta",
          "guantelete_puno", "guantelete", "nudillos", "escarcela", "escarcela_i", "escarcela_borde", "escarcela_lado",
          "yelmo", "visera", "cresta", "nasal", "cimera", "quilla", "peto_borde", "pomo", "guarda_centro", "gavilan"}

# cuanto asoma la corteza por la armadura (0 nada, 1 mucho) en la fase 1
CORTEZA_F1 = {
    "peto": 0.18, "hombrera_corteza": 0.55, "hombrera_tapa_corteza": 0.3, "quijote_i": 0.35, "greba_i": 0.45,
    "brazal": 0.12, "avambrazo": 0.18, "escarcela_i": 0.3, "faldon_malla": 0.12, "gorjal": 0.1, "yelmo": 0.08,
    "escarpe": 0.12, "espaldar": 0.2, "escarcela_lado": 0.15,
}


def corteza_asoma(img, gl, rngs, cuanto, W, H, phase):
    """Parches de corteza que rompen la armadura (borde oscuro de chapa rota alrededor)."""
    if cuanto <= 0:
        return
    m = fbm(W, H, rngs, 7 * S / 2, 3)
    thr = 1 - cuanto * 0.75
    if phase >= 2:
        thr -= 0.35
    patch = np.clip((m - thr) * 9, 0, 1)
    if patch.max() <= 0:
        return
    edge = np.clip(1 - np.abs(m - thr) * 28, 0, 1)
    bark = corteza(W, H, rngs, vertical=True)
    img[:] = mix(img, bark, patch)
    img[:] = mix(img, STEEL_DD, edge * 0.9)
    # chapa levantada: brillo justo fuera del borde
    lift = np.clip(1 - np.abs(m - (thr - 0.04)) * 30, 0, 1) * (m < thr)
    img[:] = mix(img, STEEL_E, lift * 0.35)


def paint(mat, face, W, H, phase, key):
    rng = rng_for(mat, face, key, phase)
    rngs = rng_for(mat, face, key)          # igual en todas las fases (formas)
    gl = np.zeros((H, W, 4))
    alpha = np.ones((H, W))

    # ---------------------------------------------------------------- placas de armadura
    if mat in PLACAS:
        img = acero(W, H, rng, rngs, liso=mat in ("pomo", "nudillos", "cimera"))
        if mat in ("peto", "hombrera", "hombrera_corteza", "espaldar", "quijote", "greba", "avambrazo", "yelmo") and \
                face in ("front", "left", "right", "back"):
            grabado(img, rngs, alpha=0.3, densidad=0.8 if mat != "yelmo" else 0.5)
        if mat in ("lama", "hombrera_lama", "hombrera_lama2", "escarcela_borde", "peto_borde", "nudillos") and face != "bottom":
            # filo dorado viejo en el borde de las lamas
            rect(img, 0, 0, W, max(1, int(0.5 * S)), GOLD_D, 0.85)
            rect(img, 0, 0, W, 1, GOLD, 0.7)
        if mat == "quilla":
            img = mix(img, STEEL_L, 0.25)
        if face in ("front", "back", "left", "right") and mat in ("greba", "greba_i", "avambrazo", "hombrera",
                                                                  "hombrera_corteza", "escarcela", "escarcela_i", "gorjal"):
            remaches(img, gl, 1.0 * S, 1.6 * S, W - 1.6 * S, 4.2 * S)
        if mat == "faldon_malla" and face in ("front", "back", "left", "right"):
            # malla de anillas bajo las lamas
            ys, xs = np.mgrid[0:H, 0:W]
            ring = ((xs // 2 + (ys // 2) % 2) % 2 == 0)
            img = mix(img, STEEL_DD, ring * 0.35)
        if mat == "peto" and face == "front":
            # pectorales marcados y el escudo del Arbol de la Corona en dorado viejo (lado derecho, el corazon esta
            # en el izquierdo, que en la textura queda a la derecha)
            for cx in (0.28, 0.72):
                e = ellipse(W, H, cx * W, 0.38 * H, 0.24 * W, 0.28 * H)
                img = mix(img, STEEL_L, np.clip(e * 2, 0, 1) * 0.18)
            emblema_arbol(img, 0.27 * W, 0.42 * H, 0.17 * W, color=GOLD_D, sombra=col(70, 48, 14))
        if mat == "yelmo":
            img = yelmo_cara(img, gl, face, W, H, rngs, phase)
        if mat == "guarda_centro" and face in ("front", "back", "left", "right", "top", "bottom"):
            # gema verde en el centro del gavilan
            e = ellipse(W, H, W / 2, H / 2, 0.36 * W, 0.36 * H)
            gem = np.clip(e * 3, 0, 1)
            img = mix(img, GOLD_D, np.clip(ellipse(W, H, W / 2, H / 2, 0.46 * W, 0.46 * H) * 3, 0, 1))
            img = mix(img, col(30, 140, 60), gem)
            img = mix(img, col(200, 255, 210), np.clip(ellipse(W, H, W * 0.4, H * 0.4, 0.12 * W, 0.12 * H) * 3, 0, 1))
            glow_at(gl, gem, GREEN_GLOW, 0.75)
        if mat == "pomo":
            rect(img, 0, H * 0.4, W, H * 0.6, GOLD_D, 0.9)
            rect(img, 0, H * 0.4, W, H * 0.45, GOLD, 0.8)
        if mat == "gavilan" and face in ("front", "back", "left", "right"):
            # incrustaciones de oro a lo largo
            rect(img, W * 0.5 - 0.25 * S, 0, W * 0.5 + 0.25 * S, H, GOLD_D, 0.8)
        corteza_asoma(img, gl, rngs, CORTEZA_F1.get(mat, 0.0), W, H, phase)
        if mat.endswith("_corteza") and face in ("top", "front", "left", "right", "back"):
            # hombrera izquierda casi tragada por la corteza y el musgo
            mm = fbm(W, H, rngs, 3 * S / 2, 2)
            img = mix(img, musgo_tex(W, H, rngs, gl, esporas=True), np.clip((mm - 0.55) * 4, 0, 1) * (1.0 if face == "top" else 0.55))
        # bisel de la placa (no en las caras de abajo, que casi no se ven)
        if face != "bottom" and mat not in ("pomo",):
            img = bisel(img, k=0.9)
        if face == "bottom":
            img = img * 0.7
        return img, gl, alpha

    # ---------------------------------------------------------------- corteza y raices
    if mat in ("corteza_pecho", "corteza_costado"):
        img = corteza(W, H, rngs, vertical=True)
        if mat == "corteza_pecho" and face == "front":
            # grietas que salen del corazon, con un hilo de luz verde
            c = cracks(W, H, rngs, n=6, width=0.9)
            hot = np.clip((c - 0.55) * 3, 0, 1) * np.clip(ellipse(W, H, 0.55 * W, 0.42 * H, 0.6 * W, 0.6 * H) * 2.2, 0, 1)
            img = mix(img, col(20, 60, 30), hot * 0.8)
            glow_at(gl, hot, GREEN_SOFT, 0.55 if phase == 1 else 1.0)
        img = img * (0.85 if face == "bottom" else 1.0)
        return img, gl, alpha

    if mat == "corazon":
        # el corazon del arbol: madera negra abierta con venas de luz verde
        img = corteza(W, H, rngs, vertical=True) * 0.45
        c = cracks(W, H, rngs, n=4, width=1.1)
        core = np.clip(ellipse(W, H, W / 2, H / 2, 0.45 * W, 0.48 * H) * 2.0, 0, 1)
        vein = np.clip((c - 0.35) * 2.5, 0, 1)
        lum = np.clip(vein * 0.8 + core * 0.9, 0, 1)
        img = mix(img, GREEN_GLOW, lum * 0.85)
        img = mix(img, GREEN_CORE, np.clip(core * 1.6 - 0.8, 0, 1))
        glow_at(gl, lum, GREEN_GLOW, 0.65 if phase == 1 else 1.0)
        glow_at(gl, np.clip(core * 1.6 - 0.8, 0, 1), GREEN_CORE, 0.8 if phase == 1 else 1.0)
        return img, gl, alpha

    if mat in ("raiz", "raiz_banda", "raiz_hoja", "rama", "raiz_corazon"):
        largo_y = not (mat in ("raiz_banda", "raiz_hoja") and face in ("front", "back", "left", "right"))
        img = raiz_tex(W, H, rngs, a_lo_largo_y=largo_y)
        if mat == "raiz_corazon" and face not in ("top", "bottom"):
            # savia que brilla: una vena verde que se apaga al alejarse del corazon (arriba de la pieza)
            ys = np.linspace(0, 1, H)[:, None] * np.ones((1, W))
            xs = np.arange(W)[None, :] * np.ones((H, 1))
            vena = (np.abs(xs - (W / 2 + np.sin(ys * 9) * W * 0.18)) < 0.6) * np.clip(1.15 - ys * 1.3, 0, 1)
            img = mix(img, GREEN_GLOW, vena * 0.75)
            glow_at(gl, vena, GREEN_SOFT, 0.7 if phase == 1 else 1.0)
        if mat == "rama":
            img = mix(img, corteza(W, H, rngs, vertical=True), 0.55)
            img = img * 0.85
        if mat == "raiz_banda" and face in ("top", "bottom"):
            # la banda es un anillo: por arriba y abajo solo se ve el borde (el centro es la pieza que envuelve)
            b = max(1, int(round(1.0 * S)))
            alpha[b:H - b, b:W - b] = 0
        if mat == "raiz_hoja" and face in ("top", "bottom"):
            b = max(1, int(round(0.8 * S)))
            alpha[b:H - b, b:W - b] = 0
        if face == "bottom":
            img = img * 0.8
        return img, gl, alpha

    if mat == "musgo":
        img = musgo_tex(W, H, rngs, gl)
        if face in ("front", "back", "left", "right"):
            # cuelga un poco por los lados (borde irregular)
            for x in range(W):
                cut = int(rngs.uniform(0.3, 1.0) * H)
                alpha[cut:, x] = 0 if rngs.random() < 0.6 else 1
            alpha[: max(1, H // 3), :] = 1
        return img, gl, alpha

    if mat == "hoja_arbol":
        img, mask = hoja_forma(W, H, rngs)
        alpha = mask.astype(float)
        if face == "bottom":
            img = img * 0.75
        if face not in ("top", "bottom"):
            alpha[:] = 0
        return img, gl, alpha

    if mat in ("corona_raiz",):
        img = raiz_tex(W, H, rngs, a_lo_largo_y=False)
        img = mix(img, corteza(W, H, rngs, vertical=False), 0.35)
        if face in ("top", "bottom"):
            b = max(1, int(round(1.0 * S)))
            alpha[b:H - b, b:W - b] = 0
        return img, gl, alpha

    if mat == "espina":
        ys = np.linspace(0, 1, H)[:, None] * np.ones((1, W))
        img = mix(col(40, 30, 22), col(96, 76, 52), 1 - ys)
        if face == "top":
            img = img * 1.2
        return img, gl, alpha

    if mat == "espina2":
        # espinas de la fase 2: corteza casi negra que se aclara hacia la punta, y la punta verde que brilla
        ys = np.linspace(0, 1, H)[:, None] * np.ones((1, W))
        img = mix(col(18, 13, 10), col(58, 42, 28), 1 - ys)
        img = mix(img, corteza(W, H, rngs, vertical=True), 0.25)
        punta = np.clip((0.3 - ys) / 0.3, 0, 1) if face != "bottom" else np.zeros_like(ys)
        if face == "top":
            punta = np.ones_like(ys)
        img = mix(img, col(90, 200, 110), punta * 0.6)
        glow_at(gl, punta, GREEN_GLOW, 0.75 if phase >= 2 else 0.0)
        return img, gl, alpha

    # ---------------------------------------------------------------- tela
    if mat in ("capa", "capa_bajo", "tabardo", "tabardo_tras", "capa_cuello"):
        img = tela(W, H, rngs, oscura=face in ("front",) and mat.startswith("capa"))
        if mat == "capa_cuello":
            # cuello de la capa: pano grueso con musgo y alguna hoja
            img = mix(img, CLOTH_D, 0.3)
            mm = fbm(W, H, rngs, 3 * S / 2, 2)
            img = mix(img, musgo_tex(W, H, rngs, gl, esporas=False), np.clip((mm - 0.6) * 4, 0, 1) * (1 if face == "top" else 0.5))
            ribete(img, rngs, ("bottom",), 0.6)
            return img, gl, alpha
        if face in ("front", "back"):
            if mat == "capa":
                ribete(img, rngs, ("left", "right"), 0.6)
            if mat == "capa_bajo":
                ribete(img, rngs, ("left", "right"), 0.6)
                jirones(alpha, rngs, prof=5.0)
                # hojas e hiedra que trepan por la capa
                for _ in range(int(W * H / (55 * S * S))):
                    cx, cy = rngs.uniform(0, W), rngs.uniform(H * 0.3, H * 0.95)
                    e = ellipse(W, H, cx, cy, rngs.uniform(0.6, 1.1) * S, rngs.uniform(0.4, 0.8) * S)
                    img = mix(img, mix(LEAF_D, LEAF, rngs.random()), np.clip(e * 4, 0, 1))
            if mat in ("tabardo", "tabardo_tras"):
                ribete(img, rngs, ("left", "right"), 0.6)
                jirones(alpha, rngs, prof=4.0)
                if face == "front" and mat == "tabardo":
                    emblema_arbol(img, W / 2, H * 0.3, W * 0.33)
        elif face in ("left", "right"):
            if mat in ("capa_bajo", "tabardo", "tabardo_tras"):
                jirones(alpha, rngs, prof=4.0)
            img = img * 0.9
        else:
            img = img * 0.8
        return img, gl, alpha

    # ---------------------------------------------------------------- cinturon y hebilla
    if mat == "cinturon":
        n = fbm(W, H, rngs, 3 * S / 2, 2)
        img = mix(LEATHER_D, LEATHER, 0.4 + 0.5 * n)
        rect(img, 0, 0.6 * S, W, 1.0 * S, LEATHER_L, 0.5)
        rect(img, 0, H - 1.0 * S, W, H - 0.6 * S, LEATHER_D, 0.8)
        for k in range(int(W / (3 * S))):
            x = (k + 0.5) * 3 * S
            rect(img, x - 0.4 * S, H / 2 - 0.4 * S, x + 0.4 * S, H / 2 + 0.4 * S, GOLD_D)
            rect(img, x - 0.4 * S, H / 2 - 0.4 * S, x, H / 2, GOLD)
        # una raiz que se enrosca en el cinturon
        if face in ("front", "left"):
            for x in range(W):
                y = H / 2 + math.sin(x / S * 0.8) * H * 0.3
                rect(img, x, y - 0.6 * S, x + 1, y + 0.6 * S, ROOT, 0.85)
        if face == "bottom":
            img = img * 0.7
        return img, gl, alpha

    if mat == "hebilla":
        img = mix(GOLD_D, GOLD, 0.5 + 0.4 * fbm(W, H, rngs, 2 * S / 2, 2))
        if face == "front":
            rect(img, 1, 1, W - 1, H - 1, GOLD_D)
            rect(img, 2, 2, W - 2, H - 2, col(40, 30, 20))
            emblema_arbol(img, W / 2, H * 0.55, H * 0.38, color=GOLD_L, sombra=GOLD)
        return img, gl, alpha

    # ---------------------------------------------------------------- el mandoble
    if mat == "empunadura":
        ys, xs = np.mgrid[0:H, 0:W]
        if face in ("left", "right", "top", "bottom"):
            k = xs if face in ("left", "right") else ys
            other = ys if face in ("left", "right") else xs
            stripe = ((k + other * 0.8) // max(1, S) % 3 == 0)
        else:
            stripe = np.zeros((H, W), bool)
        img = mix(LEATHER, LEATHER_L, fbm(W, H, rngs, 2, 2) * 0.6)
        img[stripe] = LEATHER_D
        return img, gl, alpha

    if mat in ("hoja", "punta", "punta2"):
        return hoja_espada(mat, face, W, H, rngs, phase, gl, alpha)

    # por si acaso: magenta para que se note
    return np.ones((H, W, 3)) * col(255, 0, 255), gl, alpha


def yelmo_cara(img, gl, face, W, H, rngs, phase):
    """Rendija de los ojos, respiraderos y los ojos verdes (solo en la cara de delante)."""
    if face != "front":
        if face in ("left", "right"):
            # bisagra del visor
            cx = W * 0.32 if face == "left" else W * 0.68
            e = ellipse(W, H, cx, H * 0.5, 1.2 * S, 1.2 * S)
            img = mix(img, STEEL_L, np.clip(e * 3, 0, 1) * 0.8)
            img = mix(img, STEEL_DD, np.clip(ellipse(W, H, cx + 0.3 * S, H * 0.5 + 0.3 * S, 0.5 * S, 0.5 * S) * 3, 0, 1))
        return img
    # la rendija esta justo bajo la visera: la visera ocupa y -9.8..-8.3 del yelmo (-15.5..0)
    y0 = (15.5 - 8.3) * S
    y1 = y0 + 1.6 * S
    rect(img, 0.6 * S, y0, W - 0.6 * S, y1, STEEL_DD)
    rect(img, 0.6 * S, y0, W - 0.6 * S, y0 + 1, col(4, 6, 5))
    # luz verde que se escapa de la rendija
    halo = np.zeros((H, W))
    for ex in (0.24, 0.76):
        cx = ex * W
        e = ellipse(W, H, cx, (y0 + y1) / 2, 1.9 * S, 0.95 * S)
        halo = np.maximum(halo, e)
        rect(img, cx - 0.9 * S, y0 + 0.25 * S, cx + 0.9 * S, y1 - 0.25 * S, EYE)
        rect(img, cx - 0.45 * S, y0 + 0.45 * S, cx + 0.45 * S, y1 - 0.45 * S, GREEN_CORE)
    eyes = np.zeros((H, W))
    for ex in (0.24, 0.76):
        cx = ex * W
        e = ellipse(W, H, cx, (y0 + y1) / 2, 0.95 * S, 0.55 * S)
        eyes = np.maximum(eyes, np.clip(e * 4, 0, 1))
    img = mix(img, col(30, 90, 45), np.clip(halo * 1.5, 0, 1) * 0.5 * (1 - eyes))
    glow_at(gl, np.clip(halo * 1.6, 0, 1), GREEN_SOFT, 0.55)
    glow_at(gl, eyes, EYE, 1.0)
    glow_at(gl, np.clip(eyes * 2 - 1, 0, 1), GREEN_CORE, 1.0)
    # respiraderos: dos columnas de ranuras a cada lado del nasal
    for sx in (-1, 1):
        for k in range(4):
            for j in range(2):
                x = W / 2 + sx * (2.2 + j * 1.3) * S
                y = y1 + (1.4 + k * 1.3) * S
                rect(img, x - 0.35 * S, y, x + 0.35 * S, y + 0.7 * S, STEEL_DD)
    return img


def hoja_espada(mat, face, W, H, rngs, phase, gl, alpha):
    """Hoja negra del mandoble con filos claros, acanaladura y runas verdes (en las dos caras anchas)."""
    if face in ("left", "right"):
        # columnas = largo de la hoja; en "right" la punta esta a la derecha, en "left" a la izquierda
        img = np.ones((H, W, 3)) * STEEL
        ys = np.linspace(-1, 1, H)[:, None] * np.ones((1, W))
        img = mix(STEEL_D, STEEL, 1 - np.abs(ys) * 0.5)
        img = mix(img, col(140, 148, 160), (np.abs(ys) > 0.72) * 0.85)          # filos
        img = mix(img, STEEL_S, (np.abs(ys) > 0.9) * 0.7)
        n = fbm(W, H, rngs, 4 * S / 2, 2)
        img = img * (0.9 + 0.14 * n)[..., None]
        if mat == "hoja":
            # acanaladura: centro mas oscuro, desde la guarda hasta el 85 % del largo
            xs = np.arange(W)
            u = xs / max(1, W - 1)                 # 0..1 a lo largo de la textura
            punta = u if face == "right" else 1 - u   # 0 en la guarda, 1 en la punta
            dentro = (punta < 0.86)[None, :] & (np.abs(ys) < 0.3)
            img[dentro] = mix(img[dentro], STEEL_DD, 0.8)
            # runas: glifos cada ~5 px dentro de la acanaladura
            paso = int(5 * S)
            runas = np.zeros((H, W))
            yc = H / 2
            for k, x in enumerate(range(int(3 * S), W - int(2 * S), paso)):
                p = (x / max(1, W - 1)) if face == "right" else 1 - x / max(1, W - 1)
                if p > 0.82:
                    continue
                r = rng_for("runa", k)
                for _ in range(3):
                    a0 = r.uniform(-1.2, 1.2)
                    x0 = x + r.uniform(-1.2, 1.2) * S
                    ln = r.uniform(1.0, 1.8) * S
                    for i in range(int(ln * 2) + 1):
                        t = i / max(1, int(ln * 2))
                        xx = int(x0 + math.cos(a0) * ln * (t - 0.5))
                        yy = int(yc + math.sin(a0) * ln * (t - 0.5) * 0.6)
                        if 0 <= xx < W and 0 <= yy < H:
                            runas[yy, xx] = 1
            img = mix(img, col(70, 220, 110), runas * 0.9)
            glow_at(gl, runas, GREEN_GLOW, 0.7 if phase == 1 else 1.0)
            # raices grabadas junto a la guarda
            base = (punta < 0.12)[None, :] & (np.abs(ys) < 0.7)
            img[base] = mix(img[base], ROOT_DD, 0.35)
        return img, gl, alpha
    if face in ("top", "bottom"):
        # los filos (vistos de canto)
        img = np.ones((H, W, 3)) * col(150, 158, 170)
        img = img * (0.9 + 0.1 * fbm(W, H, rngs, 2, 2))[..., None]
        return img, gl, alpha
    img = np.ones((H, W, 3)) * STEEL
    return img, gl, alpha


# ------------------------------------------------------------------ montar las texturas


def faces_of(b):
    """Rectangulos (en unidades de UV) de cada cara de la caja b, como los usa Minecraft."""
    u, v, w, h, d = b.u, b.v, b.w, b.h, b.d
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }


def build(phase, used_h):
    base = np.zeros((TEX_H * S, TEX_W * S, 4))
    glow = np.zeros((TEX_H * S, TEX_W * S, 4))
    done = set()
    for p in PARTS:
        for i, b in enumerate(p.boxes):
            key = b.uv or f"{p.name}#{i}"
            if key in done:
                continue
            done.add(key)
            for face, (fu, fv, fw, fh) in faces_of(b).items():
                W_, H_ = int(round(fw * S)), int(round(fh * S))
                if W_ <= 0 or H_ <= 0:
                    continue
                img, gl, al = paint(b.mat, face, W_, H_, phase, key)
                x0, y0 = int(round(fu * S)), int(round(fv * S))
                base[y0:y0 + H_, x0:x0 + W_, :3] = np.clip(img, 0, 1)
                base[y0:y0 + H_, x0:x0 + W_, 3] = al
                gl = np.clip(gl, 0, 1)
                gl[..., 3] *= al
                glow[y0:y0 + H_, x0:x0 + W_] = gl
    return base, glow
