"""El pintor de los bloques vivos y de lo que se rompe (parte 8k), a 64 x 64 (los animados, tiras de fotogramas que se
repiten sin salto: todo se mueve un numero entero de vueltas en la animacion).

- raices(): un nudo de raices que se retuercen despacio sobre tierra oscura (16 fotogramas).
- ojos(): los ojos que se abren en la piedra: un dibujo con transparencia (solo los ojos) que va encima de la piedra y
  brilla (32 fotogramas: miran a un lado, al centro, al otro, parpadean).
- carne(): carne con venas que late (16 fotogramas).
- cristal(): los cristales de un racimo (con transparencia; brillan) y vetas(): las vetas de cristal de una roca.
- sangre(): manchas y regueros de sangre (con transparencia) para encima de la piedra; charco(): sangre que brilla y se
  mueve (8 fotogramas).
- barro_cocido() y urna(): los jarrones y la urna de huesos.
"""
import math

import numpy as np

import pintor as P
from pintor import N, ruido


def _c(rampa, l):
    return rampa.pintar(np.array([[min(1.0, max(0.0, l))]]), 0.0)[0, 0]


def raices(semilla, rampa, tierra, fotogramas=16, n=N):
    """Un nudo de raices (tubos con luz de arriba a la izquierda y sombra sobre la tierra) que se retuercen despacio."""
    rng = np.random.default_rng(semilla)
    base = tierra.pintar(0.22 + 0.16 * ruido(semilla + 1, base=8, octavas=3), 0.6).astype(float)
    tiras = []
    for k in range(10):
        tiras.append(dict(y0=rng.uniform(0, n), x0=rng.uniform(0, n), ang=rng.uniform(-0.6, 0.6) + (0 if k % 2 else math.pi / 2),
                          grueso=rng.uniform(1.6, 4.2), amp=rng.uniform(2, 5), olas=int(rng.integers(1, 3)),
                          vel=int(rng.integers(1, 3)), fase=rng.uniform(0, 6.28)))
    tira = np.zeros((fotogramas * n, n, 3))
    for t in range(fotogramas):
        f = t / fotogramas
        capa = np.zeros((n, n, 3))
        prof = np.full((n, n), -9.0)
        for r in tiras:
            ca, sa = math.cos(r["ang"]), math.sin(r["ang"])
            for s in np.linspace(0, n, n * 3):
                off = r["amp"] * math.sin(2 * math.pi * (r["olas"] * s / n + r["vel"] * f) + r["fase"])
                cx = r["x0"] + ca * s - sa * off
                cy = r["y0"] + sa * s + ca * off
                g = r["grueso"] * (0.65 + 0.35 * math.sin(2 * math.pi * s / n * 2 + r["fase"]))
                # la normal del tubo, girada hacia la luz (arriba a la izquierda)
                nx, ny = -sa, ca
                hacia_luz = -(nx + ny) / math.sqrt(2)
                for d in np.arange(-g, g + 0.01, 0.5):
                    x, y = int(cx + nx * d) % n, int(cy + ny * d) % n
                    q = d / g
                    luz = 0.42 + 0.38 * q * hacia_luz + 0.30 * math.sqrt(max(0.0, 1 - q * q)) - 0.25 * q * q \
                        + 0.06 * math.sin(s * 1.9 + r["fase"])
                    alto = math.sqrt(max(0.0, 1 - q * q)) + 0.01 * r["grueso"]
                    if alto > prof[y, x]:
                        prof[y, x] = alto
                        capa[y, x] = _c(rampa, luz)
        hay = prof > -9
        # la sombra de las raices sobre la tierra (abajo a la derecha) y el filo oscuro de cada raiz
        sombra = (np.roll(hay, (1, 1), (0, 1)) | np.roll(hay, (2, 2), (0, 1)) | np.roll(hay, (1, 0), (0, 1))) & ~hay
        img = base.copy()
        img[sombra] *= 0.55
        filo = hay & ~(np.roll(hay, 1, 0) & np.roll(hay, -1, 0) & np.roll(hay, 1, 1) & np.roll(hay, -1, 1))
        img[hay] = capa[hay]
        img[filo] *= 0.72
        tira[t * n:(t + 1) * n] = img
    return tira


def ojos(semilla, fotogramas=32, n=N):
    """Dos ojos en la piedra: rgba (lo de fuera de los ojos, transparente). Miran a la izquierda, al centro, a la derecha y
    parpadean (los parpados de piedra oscura los cierran)."""
    centros = [(18, 22, 9.0), (45, 41, 7.5)]
    tira = np.zeros((fotogramas * n, n, 4))
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    for t in range(fotogramas):
        img = np.zeros((n, n, 4))
        if t < 8:
            mira = -1.0
        elif t < 14:
            mira = 0.0
        elif t < 22:
            mira = 1.0
        else:
            mira = 0.0
        cierre = {24: 0.5, 25: 1.0, 26: 1.0, 27: 0.5}.get(t, 0.0)
        for (cx, cy, r) in centros:
            u, v = (xs - cx) / r, (ys - cy) / (r * 0.55)
            ojo = u * u + v * v < 1
            parpado = np.abs(v) > (1 - cierre) * np.sqrt(np.clip(1 - u * u, 0, 1)) - 1e-6
            blanco = ojo & ~parpado
            img[blanco] = (205, 190, 150, 255)
            img[blanco & (v > 0.4)] = (170, 150, 110, 255)
            px = cx + mira * r * 0.35
            ri = np.hypot(xs - px, ys - cy)
            iris = blanco & (ri < r * 0.42)
            img[iris] = (190, 30, 20, 255)
            img[iris & (ri < r * 0.32)] = (230, 60, 30, 255)
            img[blanco & (ri < r * 0.18)] = (10, 5, 5, 255)
            brillo = blanco & (np.hypot(xs - px + 1.2, ys - cy + 1.2) < 1.0)
            img[brillo] = (255, 240, 220, 255)
            # el borde del ojo (piel de piedra arrugada) y el parpado cerrado
            borde = (u * u + v * v < 1.35) & ~ojo
            img[borde] = (40, 32, 30, 255)
            img[ojo & parpado] = (52, 44, 40, 255)
        tira[t * n:(t + 1) * n] = img
    return tira


def carne(semilla, rampa, venas, fotogramas=16, n=N):
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    pts = rng.uniform(0, n, (18, 2))
    d1 = np.full((n, n), 1e9)
    d2 = np.full((n, n), 1e9)
    cel = np.zeros((n, n), dtype=int)
    for k, (px, py) in enumerate(pts):
        dx = np.abs(xs - px)
        dx = np.minimum(dx, n - dx)
        dy = np.abs(ys - py)
        dy = np.minimum(dy, n - dy)
        d = np.hypot(dx, dy)
        m = d < d1
        d2 = np.where(m, d1, np.minimum(d2, d))
        cel = np.where(m, k, cel)
        d1 = np.where(m, d, d1)
    borde = d2 - d1
    fases = rng.uniform(0, 2 * math.pi, len(pts))
    fino = ruido(semilla + 1, base=16, octavas=2)
    tira = np.zeros((fotogramas * n, n, 3))
    for t in range(fotogramas):
        late = np.sin(2 * math.pi * t / fotogramas + fases[cel])
        bulto = np.clip(1 - d1 / (9 + 1.5 * late), 0, 1)
        v = 0.35 + 0.35 * bulto + 0.08 * late * bulto + 0.06 * (fino - 0.5)
        v = v + 0.12 * np.clip(P.luz_de(bulto) * 4, -1, 1)
        img = rampa.pintar(np.clip(v, 0, 1), 0.6)
        vena = borde < 1.6
        img = np.where(vena[..., None], venas.pintar(np.clip(0.3 + 0.4 * (1.6 - borde) / 1.6 + 0.1 * late, 0, 1), 0.5), img)
        # brillo humedo
        humedo = (bulto > 0.75) & (P.luz_de(bulto) > 0.05)
        img = np.where(humedo[..., None], np.minimum(255, img * 1.35 + 25), img)
        tira[t * n:(t + 1) * n] = img
    return tira


def cristal(semilla, rampa, n=N):
    """Un racimo de cristales (para los planos de un racimo, que lo enseñan sin estirarlo, y para el objeto): prismas en
    punta, un poco inclinados, con tres caras (la de la izquierda a la luz, la de en medio, la de la derecha en sombra), las
    caras de la punta, un filo claro, un brillo por dentro hacia la punta y un borde oscuro que los separa; rgba."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    # (x de la base, alto, medio ancho, inclinacion): los grandes en el centro (detras), los chicos delante
    cristales = [(32, 60, 8.5, 0.0), (20, 46, 6.5, -0.22), (45, 50, 6.5, 0.2), (10, 30, 5.0, -0.35), (55, 32, 5.0, 0.33),
                 (27, 24, 4.5, -0.1), (39, 20, 4.0, 0.15), (3, 16, 3.5, -0.3), (61, 18, 3.5, 0.3)]
    for (bx, alto, w, a) in cristales:
        bx += rng.uniform(-1, 1)
        a += rng.uniform(-0.05, 0.05)
        ux, uy = math.sin(a), -math.cos(a)
        px_, py_ = math.cos(a), math.sin(a)
        dx, dy = xs - bx, ys - (n + 1)
        t = dx * ux + dy * uy
        s_ = dx * px_ + dy * py_
        punta = w * 1.7
        cuerpo = (t >= 0) & (t <= alto - punta) & (np.abs(s_) <= w)
        ancho_p = w * np.clip((alto - t) / punta, 0, 1)
        tip = (t > alto - punta) & (t <= alto) & (np.abs(s_) <= ancho_p)
        todo = cuerpo | tip
        q = s_ / w
        luz = np.where(q < -0.33, 0.78, np.where(q < 0.33, 0.55, 0.32))
        luz = np.where(tip, np.where(s_ < 0, 0.95, 0.45), luz)
        luz = luz + 0.12 * np.clip(t / alto, 0, 1) + 0.05 * np.sin(t * 0.35 + bx)
        # el filo a la luz (la arista de la izquierda) y la arista de la punta
        filo = cuerpo & (np.abs(q + 0.33) < 1.1 / w)
        luz = np.where(filo, 1.0, luz)
        arista = tip & (np.abs(s_) < 0.7)
        luz = np.where(arista, 0.85, luz)
        # una raja por dentro
        raja = cuerpo & (np.abs(t - alto * 0.45 - 0.6 * s_) < 0.5) & (q > -0.2) & (q < 0.7)
        luz = np.where(raja, luz + 0.25, luz)
        borde = todo & ~(np.roll(todo, 1, 0) & np.roll(todo, -1, 0) & np.roll(todo, 1, 1) & np.roll(todo, -1, 1))
        luz = np.where(borde, 0.12, luz)
        lv = np.clip(luz, 0, 1)
        col = rampa.pintar(lv, 0.5)
        img[todo, :3] = col[todo]
        img[todo, 3] = 255
    return img


def vetas(semilla, rampa, n=N):
    """Vetas de cristal (rgba): grietas que se ramifican con cristales en ellas."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    for _ in range(4):
        x, y = rng.uniform(0, n), rng.uniform(0, n)
        a = rng.uniform(0, 2 * math.pi)
        for paso in range(40):
            g = 1 + int(2 * abs(math.sin(paso * 0.3)))
            for dx in range(-g, g + 1):
                for dy in range(-g, g + 1):
                    if dx * dx + dy * dy <= g * g:
                        xi, yi = int(x + dx) % n, int(y + dy) % n
                        img[yi, xi] = np.concatenate([_c(rampa, 0.5 + 0.4 * (dx < 0) + 0.1 * (dy < 0)), [255]])
            x += math.cos(a) * 1.5
            y += math.sin(a) * 1.5
            a += rng.uniform(-0.5, 0.5)
    return img


def sangre(semilla, n=N):
    """Manchas de sangre y regueros que bajan (rgba), para encima de la piedra."""
    rng = np.random.default_rng(semilla)
    img = np.zeros((n, n, 4))
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    m = ruido(semilla, base=4, octavas=4) > 0.66
    for _ in range(7):
        x = rng.uniform(0, n)
        y0 = rng.uniform(0, n * 0.6)
        largo = rng.uniform(14, 40)
        ancho = rng.uniform(1.0, 2.2)
        dx = np.abs(xs - x)
        dx = np.minimum(dx, n - dx)
        m |= (dx < ancho * (1 - 0.5 * (ys - y0) / largo)) & (ys > y0) & (ys < y0 + largo)
        m |= np.hypot(xs - x, ys - y0 - largo) < ancho * 1.3
    r = 70 + 70 * ruido(semilla + 1, base=8, octavas=2)
    img[m] = np.stack([r[m], r[m] * 0.12, r[m] * 0.1, np.full(m.sum(), 235)], axis=1)
    return img


def charco(semilla, fotogramas=8, n=N):
    """Un charco de sangre (rgba): una mancha de borde irregular con gotas sueltas alrededor, el borde mas oscuro (ya seco) y
    por dentro un brillo humedo que se mueve despacio."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    r = np.hypot((xs - n / 2) / (n * 0.40), (ys - n / 2) / (n * 0.34))
    forma = 1.0 - r + 0.45 * (ruido(semilla, base=4, octavas=3) - 0.5)
    mancha = forma > 0.12
    for _ in range(7):
        a, d = rng.uniform(0, 2 * math.pi), rng.uniform(0.36, 0.46) * n
        cx, cy, rr = n / 2 + math.cos(a) * d, n / 2 + math.sin(a) * d * 0.85, rng.uniform(1.2, 2.6)
        mancha |= np.hypot(xs - cx, ys - cy) < rr
    dentro = forma - 0.12
    base = ruido(semilla + 2, base=8, octavas=2)
    tira = np.zeros((fotogramas * n, n, 4))
    for t in range(fotogramas):
        w = np.roll(ruido(semilla + 1, base=4, octavas=2), int(t * n / fotogramas), axis=1)
        brillo = np.clip((w - 0.72) / 0.12, 0, 1) * np.clip(dentro / 0.15, 0, 1)
        v = 0.38 + 0.22 * base + 0.5 * brillo - 0.3 * np.clip(1 - dentro / 0.08, 0, 1)
        img = np.zeros((n, n, 4))
        img[..., 0] = 40 + 150 * v
        img[..., 1] = 4 + 22 * v * v
        img[..., 2] = 6 + 20 * v * v
        img[..., 3] = np.where(mancha, 255, 0)
        tira[t * n:(t + 1) * n] = img
    return tira


def barro_cocido(semilla, rampa, banda, n=N):
    """El barro cocido de los jarrones: liso con una banda pintada de grecas (a media altura) y desconchones."""
    rng = np.random.default_rng(semilla)
    ys, xs = np.mgrid[0:n, 0:n] + 0.5
    v = 0.55 + 0.08 * (ruido(semilla, base=8, octavas=3) - 0.5) + 0.06 * np.cos(xs / n * 2 * math.pi * 4)
    img = rampa.pintar(np.clip(v, 0, 1), 0.6)
    b = (ys > 26) & (ys < 38)
    greca = b & ((((xs // 6) + (ys // 6)) % 2) == 0) & (ys > 28) & (ys < 36)
    img = np.where(b[..., None], banda.pintar(np.clip(v - 0.25, 0, 1), 0.5), img)
    img = np.where(greca[..., None], rampa.pintar(np.clip(v + 0.15, 0, 1), 0.5), img)
    for _ in range(4):
        cx, cy, r = rng.uniform(0, n), rng.uniform(0, n), rng.uniform(2, 4)
        d = np.hypot(xs - cx, ys - cy) < r
        img = np.where(d[..., None], rampa.pintar(np.clip(v + 0.2, 0, 1), 0.3), img)
    return img
