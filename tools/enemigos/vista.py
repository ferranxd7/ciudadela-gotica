"""
Dibuja un modelo de tools/enemigos en Python igual que lo coloca Minecraft (pivotes, rotaciones Z-Y-X, cajas con el
reparto de textura del juego y recorte por transparencia), para ver poses y animaciones sin abrir el juego. Es el
dibujante de tools/gavran/vista.py, pero para cualquier Rig.
"""
import math

import numpy as np


def rx(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[1, 0, 0, 0], [0, c, -s, 0], [0, s, c, 0], [0, 0, 0, 1]])


def ry(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, 0, s, 0], [0, 1, 0, 0], [-s, 0, c, 0], [0, 0, 0, 1]])


def rz(a):
    c, s = math.cos(a), math.sin(a)
    return np.array([[c, -s, 0, 0], [s, c, 0, 0], [0, 0, 1, 0], [0, 0, 0, 1]])


def tr(x, y, z):
    m = np.eye(4)
    m[:3, 3] = [x, y, z]
    return m


def sc(x, y, z):
    return np.diag([x, y, z, 1.0])


D = math.pi / 180


def matrices(rig, offsets=None):
    """Matriz (en bloques) de cada pieza. offsets: {pieza: {"rot": (x, y, z) grados, "pos": (x, y arriba, z) px}} o (x, y, z)."""
    offsets = offsets or {}
    mats = {}
    for p in rig.partes:
        o = offsets.get(p.name, {})
        if not isinstance(o, dict):
            o = {"rot": o}
        px, py, pz = p.pivot
        if "pos" in o:
            px += o["pos"][0]
            py -= o["pos"][1]
            pz += o["pos"][2]
        a, b, c = (p.rot[0] * D, p.rot[1] * D, p.rot[2] * D)
        if "rot" in o:
            a += o["rot"][0] * D
            b += o["rot"][1] * D
            c += o["rot"][2] * D
        local = tr(px / 16, py / 16, pz / 16) @ rz(c) @ ry(b) @ rx(a)
        if "scale" in o:
            s = o["scale"]
            local = local @ sc(s[0], s[1], s[2])
        mats[p.name] = (mats[p.parent] @ local) if p.parent else local
    return mats


FACES = {
    # nombre: (indices de vertices, (u1, v1, u2, v2) en funcion de u, v, w, h, d)
    "arriba": ([4, 3, 0, 1], lambda u, v, w, h, d: (u + d, v, u + d + w, v + d)),
    "abajo": ([2, 7, 6, 5], lambda u, v, w, h, d: (u + d + w, v + d, u + d + 2 * w, v)),
    "der": ([0, 3, 6, 7], lambda u, v, w, h, d: (u, v + d, u + d, v + d + h)),
    "frente": ([1, 0, 7, 2], lambda u, v, w, h, d: (u + d, v + d, u + d + w, v + d + h)),
    "izq": ([4, 1, 2, 5], lambda u, v, w, h, d: (u + d + w, v + d, u + 2 * d + w, v + d + h)),
    "espalda": ([3, 4, 5, 6], lambda u, v, w, h, d: (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)),
}


def corners(b):
    gx, gy, gz = b.grow
    x0, y0, z0 = b.x - gx, b.y - gy, b.z - gz
    x1, y1, z1 = b.x + b.w + gx, b.y + b.h + gy, b.z + b.d + gz
    return np.array([
        [x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y0, z1],
        [x1, y0, z1], [x1, y1, z1], [x0, y1, z1], [x0, y1, z0],
    ]) / 16.0


def quads(rig, mats, ocultas=()):
    out = []
    oc = set(ocultas)
    # una pieza oculta esconde tambien a sus hijas
    for p in rig.partes:
        if p.parent in oc:
            oc.add(p.name)
    for p in rig.partes:
        if p.name in oc:
            continue
        m = mats[p.name]
        for b in p.boxes:
            c = corners(b)
            ch = np.c_[c, np.ones(8)] @ m.T
            w, h, d = math.ceil(b.w), math.ceil(b.h), math.ceil(b.d)
            for fname, (idx, uvf) in FACES.items():
                u1, v1, u2, v2 = uvf(b.u, b.v, w, h, d)
                uvs = np.array([[u2, v1], [u1, v1], [u1, v2], [u2, v2]])
                out.append((ch[idx, :3], uvs))
    return out


def render(rig, tex, glow=None, offsets=None, yaw=200, pitch=-10, size=(240, 320), zoom=None, giro=0.0, ocultas=(),
           fondo=(46, 44, 52), centro_y=1.0, escala_modelo=1.0, suelo=True):
    """Imagen RGB. tex: array HxWx4 en 0..1. yaw / pitch: la camara, en grados. giro: hacia donde mira el cuerpo."""
    W, H = size
    mats = matrices(rig, offsets)
    # como LivingEntityRenderer: girar 180 - giro, voltear X e Y y bajar 1.501
    ent = ry((180 - giro) * D) @ sc(-escala_modelo, -escala_modelo, escala_modelo) @ tr(0, -1.501, 0)
    cam = rx(-pitch * D) @ ry(-yaw * D)
    zoom = zoom or H / 2.9
    img = np.zeros((H, W, 3))
    img[:] = np.array(fondo) / 255.0
    zbuf = np.full((H, W), -1e9)
    # la luz viene de la camara, de arriba y un poco de lado (lo que se mira esta siempre iluminado)
    luz = np.linalg.inv(cam[:3, :3]) @ np.array([-0.35, 0.62, 0.70])
    luz = luz / np.linalg.norm(luz)
    if suelo:
        # una sombra de suelo para ver donde pisa
        for a in range(0, 360, 6):
            for r in (0.15, 0.3, 0.45):
                p = np.array([math.cos(a * D) * r, -centro_y, math.sin(a * D) * r, 1.0]) @ cam.T
                x, y = int(W / 2 + p[0] * zoom), int(H / 2 - p[1] * zoom)
                if 0 <= x < W and 0 <= y < H:
                    img[y, x] = img[y, x] * 0.75
    for pts, uvs in quads(rig, mats, ocultas):
        wp = np.c_[pts, np.ones(4)] @ ent.T
        wp[:, 1] -= centro_y
        cp = wp @ cam.T
        sx = W / 2 + cp[:, 0] * zoom
        sy = H / 2 - cp[:, 1] * zoom
        sz = cp[:, 2]
        e1 = wp[1, :3] - wp[0, :3]
        e2 = wp[3, :3] - wp[0, :3]
        n = np.cross(e1, e2)
        nn = np.linalg.norm(n)
        if nn < 1e-12:
            continue
        n /= nn
        sombra = 0.52 + 0.48 * max(0.0, n @ luz)
        for tri in ((0, 1, 2), (0, 2, 3)):
            raster(img, zbuf, sx[list(tri)], sy[list(tri)], sz[list(tri)], uvs[list(tri)], tex, glow, sombra, rig.escala)
    return (np.clip(img, 0, 1) * 255).astype(np.uint8)


def raster(img, zbuf, xs, ys, zs, uvs, tex, glow, sombra, escala):
    H, W = zbuf.shape
    x0, x1 = int(max(0, math.floor(xs.min()))), int(min(W - 1, math.ceil(xs.max())))
    y0, y1 = int(max(0, math.floor(ys.min()))), int(min(H - 1, math.ceil(ys.max())))
    if x1 < x0 or y1 < y0:
        return
    den = (ys[1] - ys[2]) * (xs[0] - xs[2]) + (xs[2] - xs[1]) * (ys[0] - ys[2])
    if abs(den) < 1e-9:
        return
    gy, gx = np.mgrid[y0:y1 + 1, x0:x1 + 1]
    px, py = gx + 0.5, gy + 0.5
    a = ((ys[1] - ys[2]) * (px - xs[2]) + (xs[2] - xs[1]) * (py - ys[2])) / den
    b = ((ys[2] - ys[0]) * (px - xs[2]) + (xs[0] - xs[2]) * (py - ys[2])) / den
    c = 1 - a - b
    dentro = (a >= -1e-6) & (b >= -1e-6) & (c >= -1e-6)
    if not dentro.any():
        return
    z = a * zs[0] + b * zs[1] + c * zs[2]
    u = a * uvs[0, 0] + b * uvs[1, 0] + c * uvs[2, 0]
    v = a * uvs[0, 1] + b * uvs[1, 1] + c * uvs[2, 1]
    th, tw = tex.shape[:2]
    tu = np.clip((u * escala).astype(int), 0, tw - 1)
    tv = np.clip((v * escala).astype(int), 0, th - 1)
    sub = zbuf[y0:y1 + 1, x0:x1 + 1]
    m = dentro & (z > sub)
    if not m.any():
        return
    al = tex[tv, tu, 3]
    m = m & (al > 0.5)
    if not m.any():
        return
    color = tex[tv[m], tu[m], :3] * sombra
    if glow is not None:
        gcol = glow[tv[m], tu[m]]
        color = np.maximum(color, gcol[:, :3] * gcol[:, 3:4])
    sub[m] = z[m]
    reg = img[y0:y1 + 1, x0:x1 + 1]
    reg[m] = color


def tira(rig, tex, poses, glow=None, yaw=205, pitch=-10, size=(200, 280), etiquetas=None, **kw):
    """Varias poses en fila (una imagen): para ver una animacion fotograma a fotograma."""
    from PIL import Image, ImageDraw
    tiles = [render(rig, tex, glow, o, yaw=yaw, pitch=pitch, size=size, **kw) for o in poses]
    im = Image.fromarray(np.concatenate(tiles, axis=1))
    if etiquetas:
        d = ImageDraw.Draw(im)
        for i, t in enumerate(etiquetas):
            d.text((i * size[0] + 4, 3), str(t), fill=(235, 220, 160))
    return im
