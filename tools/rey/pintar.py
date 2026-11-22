"""Pinta las texturas del rey (procedural, con numpy). Tres fases:
1 normal, 2 chaqueta chamuscada y espadas en llamas, 3 grietas de lava en la piel y espadas de lava.
Para cada fase salen dos imagenes: la base y la de brillo (solo lo que brilla en la oscuridad).
"""
import math

import numpy as np

from partes import PARTS, SCALE, TEX_W, TEX_H

# ------------------------------------------------------------------ utilidades


def rng_for(*keys):
    h = 2166136261
    for k in keys:
        for ch in str(k):
            h = ((h ^ ord(ch)) * 16777619) & 0xFFFFFFFF
    return np.random.default_rng(h)


def vnoise(W, H, cell, rng):
    """Ruido de valor suave de tamano H x W con celdas de 'cell' texeles."""
    gw, gh = int(math.ceil(W / cell)) + 2, int(math.ceil(H / cell)) + 2
    grid = rng.random((gh, gw))
    ys = np.arange(H) / cell
    xs = np.arange(W) / cell
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


def fbm(W, H, rng, base=6.0, octaves=3):
    out = np.zeros((H, W))
    amp, tot, cell = 1.0, 0.0, base
    for _ in range(octaves):
        out += amp * vnoise(W, H, max(1.0, cell), rng)
        tot += amp
        amp *= 0.5
        cell /= 2
    return out / tot


def col(*c):
    return np.array(c, dtype=float) / 255.0


def fill(W, H, c):
    return np.ones((H, W, 3)) * c


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


def glowrect(gl, x0, y0, x1, y1, c, a=1.0):
    H, W = gl.shape[:2]
    x0, x1 = max(0, int(round(x0))), min(W, int(round(x1)))
    y0, y1 = max(0, int(round(y0))), min(H, int(round(y1)))
    if x1 > x0 and y1 > y0:
        gl[y0:y1, x0:x1, :3] = np.maximum(gl[y0:y1, x0:x1, :3], np.asarray(c)[:3] * a)
        gl[y0:y1, x0:x1, 3] = 1.0


def cracks(W, H, rng, n=7, width=1.0):
    """Mapa de grietas (Voronoi: cerca del borde entre celdas = 1)."""
    pts = rng.random((n, 2)) * [W, H]
    ys, xs = np.mgrid[0:H, 0:W]
    d = np.stack([np.hypot(xs - px, ys - py) for px, py in pts], axis=-1)
    d.sort(axis=-1)
    edge = d[..., 1] - d[..., 0]
    return np.clip(1 - edge / width, 0, 1)


# ------------------------------------------------------------------ colores

SKIN = col(156, 162, 158)
SKIN_D = col(88, 95, 96)
HAIR = col(226, 224, 214)
HAIR_D = col(165, 162, 152)
HAIR_L = col(250, 249, 244)
VELVET = col(92, 10, 22)
VELVET_D = col(52, 4, 12)
VELVET_L = col(128, 22, 36)
GOLD = col(214, 160, 34)
GOLD_D = col(128, 86, 14)
GOLD_L = col(252, 214, 104)
BLACK = col(18, 15, 18)
LEATHER = col(30, 24, 26)
CLOTH = col(28, 25, 31)
ERMINE = col(238, 234, 224)
STEEL = col(196, 202, 212)
STEEL_D = col(118, 124, 136)
STEEL_L = col(244, 248, 255)
RUBY = col(176, 14, 38)
SAPPHIRE = col(30, 70, 176)
EMERALD = col(24, 140, 70)

EYE = {1: col(200, 228, 255), 2: col(255, 140, 30), 3: col(255, 236, 140)}
FIRE = col(255, 120, 20)
LAVA = col(255, 110, 10)
LAVA_HOT = col(255, 220, 90)


# ------------------------------------------------------------------ materiales
# Cada material pinta una cara: face in top/bottom/right/front/left/back, W x H texeles.
# Devuelve (rgb HxWx3, brillo HxWx4).


def skin(W, H, rng, phase, shade_bottom=0.25):
    n = fbm(W, H, rng, 5)
    fine = rng.random((H, W))
    img = mix(SKIN_D, SKIN, 0.45 + 0.4 * n + 0.15 * fine)
    # venas y manchas
    m = fbm(W, H, rng, 2.5)
    img = mix(img, col(110, 120, 128), np.clip((m - 0.7) * 3, 0, 0.5))
    ys = np.linspace(0, 1, H)[:, None]
    img = img * (1 - shade_bottom * ys[..., None] * 0.6)
    return img


def apply_phase_skin(img, gl, rng, phase, density=6):
    H, W = img.shape[:2]
    if phase >= 2:
        soot = fbm(W, H, rng, 4)
        img[:] = mix(img, col(40, 36, 36), np.clip((soot - 0.55) * 2.2, 0, 0.7))
    if phase == 3:
        c = cracks(W, H, rng, n=max(3, int(W * H / (density * density * SCALE * SCALE))), width=1.2 * SCALE / 2)
        dark = np.clip(c * 1.6, 0, 1)
        img[:] = mix(img, col(30, 22, 20), dark * 0.9)
        hot = np.clip((c - 0.45) * 2.2, 0, 1)
        img[:] = mix(img, LAVA, hot)
        gl[..., :3] = np.maximum(gl[..., :3], mix(np.zeros_like(img), mix(LAVA, LAVA_HOT, hot), hot))
        gl[..., 3] = np.maximum(gl[..., 3], (hot > 0.05).astype(float))


def hair(W, H, rng, phase, vertical=True, wav=0.0):
    xs = np.arange(W)
    strands = rng.random(W + 4)
    base = np.empty((H, W))
    for y in range(H):
        off = int(round(wav * math.sin(y / max(1, H) * 6.28 + 1.3)))
        base[y] = strands[np.clip(xs + off + 2, 0, W + 3)]
    n = fbm(W, H, rng, 3)
    t = 0.35 + 0.45 * base + 0.2 * n
    img = mix(HAIR_D, HAIR, t)
    hi = (base > 0.82) & (n > 0.4)
    img[hi] = HAIR_L
    return img


def singe(img, gl, rng, phase, from_bottom=True):
    H, W = img.shape[:2]
    if phase < 2:
        return
    ys = np.linspace(0, 1, H)[:, None] if from_bottom else np.linspace(1, 0, H)[:, None]
    n = fbm(W, H, rng, 3)
    burnt = np.clip((ys * 1.3 + n * 0.5 - (1.05 if phase == 2 else 0.85)) * 3, 0, 1)
    img[:] = mix(img, col(60, 52, 48), burnt * 0.85)
    emb = (rng.random((H, W)) < (0.02 if phase == 2 else 0.04)) & (burnt > 0.3)
    img[emb] = FIRE
    gl[emb, :3] = FIRE
    gl[emb, 3] = 1


def velvet(W, H, rng, phase):
    n = fbm(W, H, rng, 4)
    fine = rng.random((H, W))
    img = mix(VELVET_D, VELVET, 0.45 + 0.4 * n + 0.15 * fine)
    # damasco: motivos rombos mas oscuros
    ys, xs = np.mgrid[0:H, 0:W]
    s = 6 * SCALE
    m = (np.abs(((xs + (ys // s) * (s // 2)) % s) - s / 2) + np.abs((ys % s) - s / 2)) < s * 0.22
    img[m] = img[m] * 0.78
    return img


def gold_metal(W, H, rng, horizontal=True):
    n = fbm(W, H, rng, 3)
    if horizontal:
        g = np.linspace(0, 1, H)[:, None] * np.ones((1, W))
    else:
        g = np.linspace(0, 1, W)[None, :] * np.ones((H, 1))
    t = 0.75 - 0.5 * g + 0.25 * n
    img = mix(GOLD_D, GOLD, t)
    img = mix(img, GOLD_L, np.clip((t - 0.85) * 4, 0, 1))
    return img


def gold_line(img, x0, y0, x1, y1, rng=None):
    rect(img, x0, y0, x1, y1, GOLD)
    w = x1 - x0
    h = y1 - y0
    if w >= h:
        rect(img, x0, y0, x1, y0 + max(1, h * 0.35), GOLD_L)
        rect(img, x0, y1 - max(1, h * 0.3), x1, y1, GOLD_D)
    else:
        rect(img, x0, y0, x0 + max(1, w * 0.35), y1, GOLD_L)
        rect(img, x1 - max(1, w * 0.3), y0, x1, y1, GOLD_D)


def burn_cloth(img, gl, rng, phase, amount=1.0, low_bias=0.5):
    """Quemaduras de la chaqueta: fase 2 agujeros chamuscados con borde de brasa, mas abajo que arriba;
    fase 3 tela casi negra con costuras y grietas que brillan."""
    if phase < 2:
        return
    H, W = img.shape[:2]
    S = SCALE
    n = fbm(W, H, rng, 7 * S / 2, 3)
    ys = np.linspace(0, 1, H)[:, None]
    n = n + low_bias * 0.22 * (ys - 0.5)
    if phase == 2:
        thr = 0.70 - 0.05 * (amount - 1)
        hole = np.clip((n - thr) * 10, 0, 1)
        rim = np.clip(1 - np.abs(n - thr) * 30, 0, 1)
        scorch = np.clip((n - thr + 0.10) * 6, 0, 1)
        img[:] = mix(img, col(40, 14, 12), scorch * 0.75)
        img[:] = mix(img, col(12, 8, 8), hole)
        img[:] = mix(img, FIRE, rim * 0.85)
        glow = rim
    else:
        img[:] = mix(img, col(30, 10, 12), 0.72)
        c = cracks(W, H, rng, n=max(3, int(W * H / (9 * 9 * S * S))), width=0.8 * S / 2)
        hot = np.clip((c - 0.5) * 2.5, 0, 1) * (n > 0.42)
        img[:] = mix(img, mix(col(140, 30, 0), LAVA, hot), hot)
        glow = hot
    gl[..., :3] = np.maximum(gl[..., :3], FIRE * glow[..., None])
    gl[..., 3] = np.maximum(gl[..., 3], (glow > 0.12).astype(float))


# ------------------------------------------------------------------ pintores por material


# fase 4: el Rey transformado (sin chaqueta): torso y brazos de piel con musculos y grietas de lava
TORSO4 = ("chaqueta_pecho", "chaqueta_bajo", "armino", "manga", "manga_baja")


def ellipse_mask(W, H, cx, cy, rx, ry):
    ys, xs = np.mgrid[0:H, 0:W]
    return np.clip(1 - np.hypot((xs + 0.5 - cx) / max(rx, 1e-3), (ys + 0.5 - cy) / max(ry, 1e-3)), 0, 1)


def muscle(mat, face, W, H, key):
    rng = rng_for(mat, face, key, 4)
    gl = np.zeros((H, W, 4))
    img = skin(W, H, rng, 3, shade_bottom=0.15)
    img[:] = mix(img, col(84, 84, 86), 0.45)
    light, dark = col(170, 172, 168), col(26, 24, 26)
    if mat == "chaqueta_pecho" and face == "front":
        for cx in (0.29, 0.71):
            m = ellipse_mask(W, H, cx * W, 0.36 * H, 0.23 * W, 0.3 * H)
            img[:] = mix(img, light, np.clip(m * 1.4, 0, 1) * 0.5)
            edge = np.clip(ellipse_mask(W, H, cx * W, 0.38 * H, 0.25 * W, 0.33 * H) * 3, 0, 1) - np.clip(m * 3, 0, 1)
            img[:] = mix(img, dark, np.clip(edge, 0, 1) * 0.8)
        img[:, int(W * 0.49):int(W * 0.51) + 1] = mix(img[:, int(W * 0.49):int(W * 0.51) + 1], dark, 0.6)
        rect(img, 0.08 * W, 0.66 * H, 0.92 * W, 0.7 * H, dark, 0.45)
    elif mat == "chaqueta_bajo" and face == "front":
        # tableta: dos columnas y tres filas
        for cx in (0.3, 0.7):
            for cy in (0.2, 0.52, 0.84):
                m = ellipse_mask(W, H, cx * W, cy * H, 0.18 * W, 0.16 * H)
                img[:] = mix(img, light, np.clip(m * 2, 0, 1) * 0.45)
        img[:, int(W * 0.49):int(W * 0.51) + 1] = mix(img[:, int(W * 0.49):int(W * 0.51) + 1], dark, 0.65)
        for cy in (0.36, 0.68):
            rect(img, 0.12 * W, cy * H, 0.88 * W, cy * H + 1, dark, 0.5)
    elif face == "back" and mat in ("chaqueta_pecho", "chaqueta_bajo"):
        img[:, int(W * 0.49):int(W * 0.51) + 1] = mix(img[:, int(W * 0.49):int(W * 0.51) + 1], dark, 0.5)
        if mat == "chaqueta_pecho":
            for cx in (0.27, 0.73):
                m = ellipse_mask(W, H, cx * W, 0.35 * H, 0.2 * W, 0.28 * H)
                img[:] = mix(img, light, np.clip(m * 1.6, 0, 1) * 0.28)
    elif mat in ("manga", "manga_baja"):
        # biceps / antebrazo: luz en el centro de cada cara y sombra abajo, venas
        m = ellipse_mask(W, H, 0.5 * W, (0.4 if mat == "manga" else 0.5) * H, 0.45 * W, 0.42 * H)
        img[:] = mix(img, light, np.clip(m * 1.5, 0, 1) * 0.3)
        if mat == "manga_baja":
            for k in range(2):
                x = int(rng.uniform(0.2, 0.8) * W)
                for y in range(H):
                    xx = min(W - 1, max(0, x + int(np.sin(y * 0.7 + k) * 1.2)))
                    img[y, xx] = mix(img[y, xx], col(70, 78, 84), 0.5)
    elif mat == "armino":
        img[:] = mix(img, light, 0.12)
    # grietas de lava (mas en el pecho: el corazon del Rey arde)
    dens = 4 if mat in ("chaqueta_pecho", "chaqueta_bajo") else 5
    apply_phase_skin(img, gl, rng, 3, density=dens)
    if mat == "chaqueta_pecho" and face == "front":
        c = ellipse_mask(W, H, 0.69 * W, 0.4 * H, 0.1 * W, 0.12 * H)
        img[:] = mix(img, LAVA_HOT, np.clip(c * 2.5, 0, 1))
        gl[..., :3] = np.maximum(gl[..., :3], LAVA_HOT * np.clip(c * 2.5, 0, 1)[..., None])
        gl[..., 3] = np.maximum(gl[..., 3], (c > 0.02).astype(float))
    return img, gl


def paint(mat, face, W, H, phase, key):
    if phase == 4:
        if mat in TORSO4:
            return muscle(mat, face, W, H, key)
        phase = 3
    rng = rng_for(mat, face, key, phase if phase >= 2 else 1)
    rngs = rng_for(mat, face, key)  # igual en todas las fases (formas)
    gl = np.zeros((H, W, 4))
    S = SCALE

    if mat in ("cara", "nariz", "mano"):
        img = skin(W, H, rngs, phase)
        if mat == "cara":
            if face == "front":
                # arrugas en la frente
                for k, yy in enumerate((1.6, 2.2)):
                    rect(img, 1.8 * S, yy * S, 7.2 * S, yy * S + 1, SKIN_D * 0.85, 0.55)
                # sombra bajo las cejas y cuencas hundidas, separadas por el puente de la nariz
                rect(img, 1.0 * S, 3.0 * S, 8.0 * S, 3.5 * S, SKIN_D * 0.7, 0.7)
                for ex in (2.45, 6.55):
                    rect(img, (ex - 1.05) * S, 3.4 * S, (ex + 1.05) * S, 4.7 * S, col(24, 26, 30))
                    rect(img, (ex - 1.05) * S, 4.7 * S, (ex + 1.05) * S, 5.1 * S, SKIN_D * 0.75, 0.8)
                    rect(img, (ex - 0.5) * S, 3.75 * S, (ex + 0.5) * S, 4.35 * S, EYE[phase])
                    glowrect(gl, (ex - 0.5) * S, 3.75 * S, (ex + 0.5) * S, 4.35 * S, EYE[phase])
                    glowrect(gl, (ex - 0.8) * S, 3.55 * S, (ex + 0.8) * S, 4.55 * S, EYE[phase], 0.3)
                # pomulos marcados y mejillas hundidas
                rect(img, 0.9 * S, 5.0 * S, 2.4 * S, 5.4 * S, SKIN * 1.12, 0.7)
                rect(img, 6.6 * S, 5.0 * S, 8.1 * S, 5.4 * S, SKIN * 1.12, 0.7)
                rect(img, 0.7 * S, 5.4 * S, 2.2 * S, 6.7 * S, SKIN_D * 0.85, 0.55)
                rect(img, 6.8 * S, 5.4 * S, 8.3 * S, 6.7 * S, SKIN_D * 0.85, 0.55)
                # la parte baja la tapa la barba: pelo blanco
                k = int(6.6 * S)
                img[k:] = hair(W, H - k, rngs, phase)
                # patillas
                img[int(3.2 * S):, :int(0.9 * S)] = hair(int(0.9 * S), H - int(3.2 * S), rngs, phase)
                img[int(3.2 * S):, W - int(0.9 * S):] = hair(int(0.9 * S), H - int(3.2 * S), rngs, phase)
            elif face in ("left", "right"):
                # media cara de piel (delante) y pelo (detras)
                hr = hair(W, H, rngs, phase)
                front_cols = int(W * 0.45)
                if face == "right":
                    img[:, :W - front_cols] = hr[:, :W - front_cols]
                    img[int(5.5 * S):, W - front_cols:] = hr[int(5.5 * S):, W - front_cols:]
                    ex = W - front_cols
                else:
                    img[:, front_cols:] = hr[:, front_cols:]
                    img[int(5.5 * S):, :front_cols] = hr[int(5.5 * S):, :front_cols]
                    ex = front_cols
                # oreja
                rect(img, ex - 1.2 * S, 3.6 * S, ex + 0.2 * S, 6.0 * S, SKIN_D)
            elif face in ("back", "top"):
                img = hair(W, H, rngs, phase)
            else:
                img = hair(W, H, rngs, phase)
        if mat == "mano":
            if face == "bottom":
                img = img * 0.85
            if face == "front":
                for fx in range(1, 4):
                    rect(img, fx * S - 0.5, 0, fx * S + 0.5, H, SKIN_D, 0.6)
            if face in ("left", "right"):
                rect(img, 0, 1 * S, W, 1.6 * S, GOLD)  # anillo
        apply_phase_skin(img, gl, rng, phase)
        if mat == "cara" and face == "front":
            for ex in (2.45, 6.55):
                rect(img, (ex - 0.5) * S, 3.75 * S, (ex + 0.5) * S, 4.35 * S, EYE[phase])
        return img, gl

    if mat in ("pelo", "pelo_lado", "barba", "barba_punta", "bigote", "cejas"):
        wav = 1.0 if mat.startswith("barba") else 0.4
        img = hair(W, H, rngs, phase, wav=wav)
        if mat == "cejas" and face == "front":
            # ceño: la parte de dentro mas baja (sombra arriba en el centro)
            rect(img, W * 0.38, 0, W * 0.62, H * 0.5, HAIR_D * 0.7)
        if mat in ("barba", "barba_punta") and face == "front":
            # mechones con sombras
            ys = np.arange(H)[:, None]
            img *= (0.92 + 0.08 * np.sin(np.arange(W)[None, :] / S * 2.1 + ys / S * 0.7))[..., None]
        if mat == "barba_punta" and face == "front":
            # punta en forma de V
            for y in range(H):
                cut = int((y / H) * W * 0.32)
                img[y, :cut] *= 0.6
                img[y, W - cut:] *= 0.6
        if face == "bottom":
            img *= 0.8
        singe(img, gl, rng, phase, from_bottom=face not in ("top",))
        return img, gl

    if mat in ("corona", "pico", "pico_roto"):
        img = gold_metal(W, H, rngs)
        if mat == "corona":
            if face == "top":
                # terciopelo rojo dentro del aro, con cruz de oro
                inner = velvet(W, H, rngs, 1) * 0.9
                b = int(0.8 * S)
                img[b:H - b, b:W - b] = inner[b:H - b, b:W - b]
                rect(img, W / 2 - 0.4 * S, b, W / 2 + 0.4 * S, H - b, GOLD)
                rect(img, b, H / 2 - 0.4 * S, W - b, H / 2 + 0.4 * S, GOLD)
            elif face == "bottom":
                img = img * 0.5
            else:
                # banda grabada y piedras
                rect(img, 0, 0.3 * S, W, 0.7 * S, GOLD_L, 0.6)
                rect(img, 0, H - 0.7 * S, W, H - 0.3 * S, GOLD_D, 0.8)
                gems = [RUBY, SAPPHIRE, EMERALD, SAPPHIRE, RUBY]
                n = 3 if W <= 10 * S else 5
                for i in range(n):
                    cx = (i + 0.5) * W / n
                    c = gems[(i + {"front": 0, "left": 1, "right": 2, "back": 3}.get(face, 0)) % len(gems)]
                    rect(img, cx - 0.7 * S, H / 2 - 0.7 * S, cx + 0.7 * S, H / 2 + 0.7 * S, GOLD_D)
                    rect(img, cx - 0.5 * S, H / 2 - 0.5 * S, cx + 0.5 * S, H / 2 + 0.5 * S, c)
                    rect(img, cx - 0.45 * S, H / 2 - 0.45 * S, cx - 0.1 * S, H / 2 - 0.1 * S, np.minimum(c * 1.8 + 0.2, 1))
                    if phase == 3:
                        glowrect(gl, cx - 0.5 * S, H / 2 - 0.5 * S, cx + 0.5 * S, H / 2 + 0.5 * S, c * 0.8 + 0.2, 0.6)
                # una abolladura / grieta en el oro (corona rota)
                if face == "front":
                    for k in range(int(2.5 * S)):
                        x = int(W * 0.78 + k * 0.4)
                        y = int(H * 0.15 + k)
                        if 0 <= x < W and 0 <= y < H:
                            img[y, x] = GOLD_D * 0.5
        else:
            if face == "top":
                rect(img, 0, 0, W, H, col(240, 236, 220))  # perla en la punta
            if mat == "pico_roto" and face == "top":
                img = gold_metal(W, H, rngs) * 0.6  # punta rota, sin perla
        if phase == 3:
            hot = np.clip(fbm(W, H, rng, 3) - 0.62, 0, 1) * 3
            img = mix(img, LAVA, hot * 0.5)
        return img, gl

    if mat in ("chaqueta_pecho", "chaqueta_bajo", "manga", "manga_baja", "faldon_del", "faldon_tras", "faldon_lado"):
        img = velvet(W, H, rngs, phase)
        # ribetes dorados en los bordes
        b = 0.5 * S
        if mat == "chaqueta_pecho":
            if face == "front":
                cx = W / 2
                gold_line(img, cx - 0.5 * S, 0, cx + 0.5 * S, H)
                # alamares: cordones dorados horizontales con botones
                for k in range(4):
                    yy = (1.6 + k * 2.1) * S
                    gold_line(img, cx - 4.2 * S, yy, cx + 4.2 * S, yy + 0.6 * S)
                    for bx in (cx - 4.6 * S, cx + 4.6 * S):
                        rect(img, bx - 0.5 * S, yy - 0.3 * S, bx + 0.5 * S, yy + 0.9 * S, GOLD_L)
                gold_line(img, 0, 0, b, H)
                gold_line(img, W - b, 0, W, H)
                # bordado de hombros
                gold_line(img, 0, 0, W, b * 0.8)
            elif face == "back":
                # escudo bordado en la espalda: corona simple
                cx, cy = W / 2, H * 0.45
                gold_line(img, cx - 3 * S, cy, cx + 3 * S, cy + 0.6 * S)
                for k in (-2.4, -0.8, 0.8, 2.4):
                    rect(img, cx + k * S - 0.3 * S, cy - 1.6 * S, cx + k * S + 0.3 * S, cy, GOLD)
                gold_line(img, 0, 0, W, b * 0.8)
            elif face in ("left", "right"):
                gold_line(img, 0, 0, W, b * 0.8)
        elif mat == "chaqueta_bajo":
            if face == "front":
                cx = W / 2
                gold_line(img, cx - 0.5 * S, 0, cx + 0.5 * S, H)
                gold_line(img, cx - 3.2 * S, 1.5 * S, cx + 3.2 * S, 2.0 * S)
        elif mat in ("faldon_del", "faldon_tras", "faldon_lado"):
            # bajo bordado
            gold_line(img, 0, H - 1.0 * S, W, H - 0.3 * S)
            for k in range(int(W / (2 * S))):
                x = (k + 0.5) * 2 * S
                rect(img, x - 0.3 * S, H - 2.0 * S, x + 0.3 * S, H - 1.0 * S, GOLD)
            if mat == "faldon_del" and face == "front":
                gold_line(img, 0, 0, b, H)
                gold_line(img, W - b, 0, W, H)
        elif mat == "manga":
            gold_line(img, 0, 0, W, b * 0.8)
            if face in ("left", "right", "front", "back"):
                for k in range(2):
                    yy = (3 + k * 3) * S
                    rect(img, 0, yy, W, yy + 0.35 * S, GOLD_D, 0.8)
        elif mat == "manga_baja":
            pass
        burn_cloth(img, gl, rng, phase, amount=1.3 if mat.startswith("faldon") else 1.0)
        return img, gl

    if mat == "puno":
        img = gold_metal(W, H, rngs)
        # bordado: puntos rojos
        for k in range(int(W / S)):
            rect(img, (k + 0.3) * S, H / 2 - 0.3 * S, (k + 0.7) * S, H / 2 + 0.3 * S, RUBY)
        if phase >= 2:
            img = mix(img, col(70, 40, 20), fbm(W, H, rng, 3) * (0.4 if phase == 2 else 0.6))
        return img, gl

    if mat == "armino":
        n = fbm(W, H, rngs, 3)
        img = mix(col(196, 192, 182), ERMINE, 0.5 + 0.5 * n)
        # colitas negras
        r = rngs
        for k in range(int(W * H / (S * S * 6))):
            x = int(r.random() * W)
            y = int(r.random() * H)
            rect(img, x, y, x + max(1, S // 2), y + S * 1.2, BLACK)
        if face == "bottom":
            img *= 0.75
        singe(img, gl, rng, phase, from_bottom=False)
        return img, gl

    if mat == "cinturon":
        img = fill(W, H, LEATHER) * (0.8 + 0.3 * fbm(W, H, rngs, 3))[..., None]
        rect(img, 0, 0.2 * S, W, 0.5 * S, GOLD_D, 0.6)
        rect(img, 0, H - 0.5 * S, W, H - 0.2 * S, GOLD_D, 0.6)
        if face == "front":
            cx = W / 2
            rect(img, cx - 2 * S, 0.2 * S, cx + 2 * S, H - 0.2 * S, GOLD)
            rect(img, cx - 1.4 * S, 0.8 * S, cx + 1.4 * S, H - 0.8 * S, LEATHER)
            rect(img, cx - 0.4 * S, 1.0 * S, cx + 0.4 * S, H - 1.0 * S, RUBY)
        for k in range(int(W / (2 * S))):
            x = (k + 0.5) * 2 * S
            rect(img, x - 0.25 * S, H / 2 - 0.25 * S, x + 0.25 * S, H / 2 + 0.25 * S, GOLD_L)
        return img, gl

    if mat == "pantalon":
        n = fbm(W, H, rngs, 4)
        img = mix(CLOTH * 0.7, CLOTH * 1.3, n)
        for k in range(0, W, 3 * S):
            rect(img, k, 0, k + 1, H, CLOTH * 0.6, 0.6)
        if phase >= 2:
            burn_cloth(img, gl, rng, phase, amount=0.6)
        return img, gl

    if mat == "bota":
        n = fbm(W, H, rngs, 3)
        img = mix(BLACK * 0.8, LEATHER * 1.4, n)
        if face in ("front", "left", "right", "back"):
            gold_line(img, 0, 0, W, 0.7 * S)
            rect(img, 0, H - 0.8 * S, W, H, BLACK * 0.6)
            if face == "front":
                rect(img, W * 0.2, H * 0.45, W * 0.8, H * 0.55, LEATHER * 2.0, 0.5)
        if face == "bottom":
            img = fill(W, H, BLACK * 0.6)
        return img, gl

    # ---------------------------------------------------------------- espadas
    if mat == "hoja":
        if face in ("left", "right"):
            # cara ancha de la hoja: W = d (largo), H = h (ancho). Columnas: largo de la hoja.
            ys = np.linspace(-1, 1, H)[:, None]
            img = mix(STEEL_D, STEEL, 1 - np.abs(ys) * 0.6) * np.ones((1, W, 1))
            img = mix(img, STEEL_L, (np.abs(ys) > 0.75) * 0.8)
            # acanaladura central
            rect(img, 0, H / 2 - 0.15 * S, W * 0.75, H / 2 + 0.15 * S, STEEL_D * 0.8)
            n = fbm(W, H, rngs, 3)
            img = img * (0.92 + 0.08 * n)[..., None]
        else:
            img = fill(W, H, STEEL_L) * (0.9 + 0.1 * fbm(W, H, rngs, 2))[..., None]
        if phase == 2:
            # al rojo, con llamas
            L = np.linspace(0, 1, W)[None, :] if face in ("left", "right", "top", "bottom") else np.linspace(0, 1, H)[:, None]
            hotc = mix(col(255, 90, 10), col(255, 230, 120), (np.sin(L * 9) * 0.5 + 0.5) * 0.6 + 0.2)
            hotc = hotc * np.ones((H, W, 1)) if hotc.ndim == 3 else hotc
            img = mix(img, hotc, 0.85)
            gl[..., :3] = hotc
            gl[..., 3] = 1
        if phase == 3:
            crust = fbm(W, H, rng, 2.5, 3)
            c = cracks(W, H, rng, n=max(4, W // 3), width=0.9 * S / 2)
            img = mix(col(38, 28, 26), col(70, 46, 36), crust)
            hot = np.clip(c * 1.4 + (crust > 0.72) * 0.6, 0, 1)
            img = mix(img, LAVA, hot)
            gl[..., :3] = mix(col(120, 30, 0), LAVA_HOT, hot) * np.clip(hot + 0.25, 0, 1)[..., None]
            gl[..., 3] = 1
        return img, gl

    if mat == "punta":
        img = fill(W, H, STEEL_L)
        if phase == 2:
            img = fill(W, H, col(255, 220, 110))
            gl[...] = [1.0, 0.86, 0.43, 1]
        if phase == 3:
            img = fill(W, H, LAVA_HOT)
            gl[...] = [1.0, 0.86, 0.35, 1]
        return img, gl

    if mat in ("guarda", "pomo"):
        img = gold_metal(W, H, rngs, horizontal=face not in ("left", "right"))
        if mat == "pomo" and face in ("left", "right", "back"):
            rect(img, W * 0.25, H * 0.25, W * 0.75, H * 0.75, RUBY)
        if phase == 3:
            img = mix(img, col(60, 30, 20), 0.35)
        return img, gl

    if mat == "puno_espada":
        img = fill(W, H, LEATHER * 1.3)
        ys, xs = np.mgrid[0:H, 0:W]
        stripe = ((xs + ys) // max(1, S // 2)) % 3 == 0
        img[stripe] = LEATHER * 0.6
        return img, gl

    # por si acaso
    return fill(W, H, col(255, 0, 255)), gl


# ------------------------------------------------------------------ montar las texturas


def faces_of(b):
    """Rectangulos (en texeles) de cada cara de la caja b, como los usa Minecraft."""
    u, v, w, h, d = b.u, b.v, b.w, b.h, b.d
    S = SCALE
    return {
        "top": (u + d, v, w, d),
        "bottom": (u + d + w, v, w, d),
        "right": (u, v + d, d, h),
        "front": (u + d, v + d, w, h),
        "left": (u + d + w, v + d, d, h),
        "back": (u + 2 * d + w, v + d, w, h),
    }, S


def build(phase, used_h):
    S = SCALE
    H = used_h
    base = np.zeros((TEX_H * S, TEX_W * S, 4))
    glow = np.zeros((TEX_H * S, TEX_W * S, 4))
    done = set()
    for p in PARTS:
        for i, b in enumerate(p.boxes):
            key = b.uv or f"{p.name}#{i}"
            if key in done:
                continue
            done.add(key)
            fs, S = faces_of(b)
            for face, (fu, fv, fw, fh) in fs.items():
                W_, H_ = int(round(fw * S)), int(round(fh * S))
                if W_ <= 0 or H_ <= 0:
                    continue
                img, gl = paint(b.mat, face, W_, H_, phase, key)
                x0, y0 = int(round(fu * S)), int(round(fv * S))
                base[y0:y0 + H_, x0:x0 + W_, :3] = np.clip(img, 0, 1)
                base[y0:y0 + H_, x0:x0 + W_, 3] = 1
                glow[y0:y0 + H_, x0:x0 + W_] = np.clip(gl, 0, 1)
    return base, glow
