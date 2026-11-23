"""Dibuja el modelo de Gavran en Python, igual que lo coloca Minecraft (pivotes, rotaciones Z-Y-X,
cajas con UV de vanilla y recorte por transparencia), para ver poses y animaciones sin abrir el juego.
(Copia de tools/rey/vista.py con transparencia y con la escala de este modelo.)
"""
import math

import numpy as np

from partes import PARTS, BY_NAME, SCALE


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


def part_matrices(offsets=None):
    """Matriz (en bloques) de cada pieza. offsets: {pieza: {"rot": (x,y,z) grados, "pos": (x,y,z) px (y hacia arriba)}}"""
    offsets = offsets or {}
    mats = {}
    for p in PARTS:
        o = offsets.get(p.name, {})
        px, py, pz = p.pivot
        if "pos" in o:
            px += o["pos"][0]
            py -= o["pos"][1]
            pz += o["pos"][2]
        rxx, ryy, rzz = (p.rot[0] * D, p.rot[1] * D, p.rot[2] * D)
        if "rot" in o:
            rxx += o["rot"][0] * D
            ryy += o["rot"][1] * D
            rzz += o["rot"][2] * D
        local = tr(px / 16, py / 16, pz / 16) @ rz(rzz) @ ry(ryy) @ rx(rxx)
        if "scale" in o:
            s = o["scale"]
            local = local @ sc(s[0], s[1], s[2])
        mats[p.name] = (mats[p.parent] @ local) if p.parent else local
    return mats


FACES = {
    # nombre: (indices de vertices, (u1, v1, u2, v2) en funcion de u, v, w, h, d)
    "top": ([4, 3, 0, 1], lambda u, v, w, h, d: (u + d, v, u + d + w, v + d)),
    "bottom": ([2, 7, 6, 5], lambda u, v, w, h, d: (u + d + w, v + d, u + d + 2 * w, v)),
    "right": ([0, 3, 6, 7], lambda u, v, w, h, d: (u, v + d, u + d, v + d + h)),
    "front": ([1, 0, 7, 2], lambda u, v, w, h, d: (u + d, v + d, u + d + w, v + d + h)),
    "left": ([4, 1, 2, 5], lambda u, v, w, h, d: (u + d + w, v + d, u + 2 * d + w, v + d + h)),
    "back": ([3, 4, 5, 6], lambda u, v, w, h, d: (u + 2 * d + w, v + d, u + 2 * d + 2 * w, v + d + h)),
}


def corners(b):
    gx, gy, gz = b.grow
    x0, y0, z0 = b.x - gx, b.y - gy, b.z - gz
    x1, y1, z1 = b.x + b.w + gx, b.y + b.h + gy, b.z + b.d + gz
    # v7, v, v1, v2, v3, v4, v5, v6  ->  0..7 en este orden:
    # 0=(x0,y0,z0) 1=(x1,y0,z0) 2=(x1,y1,z0) 3=(x0,y0,z1) 4=(x1,y0,z1) 5=(x1,y1,z1) 6=(x0,y1,z1) 7=(x0,y1,z0)
    return np.array([
        [x0, y0, z0], [x1, y0, z0], [x1, y1, z0], [x0, y0, z1],
        [x1, y0, z1], [x1, y1, z1], [x0, y1, z1], [x0, y1, z0],
    ]) / 16.0


def quads(mats, hidden=()):
    out = []
    for p in PARTS:
        if p.name in hidden:
            continue
        m = mats[p.name]
        for b in p.boxes:
            c = corners(b)
            ch = np.c_[c, np.ones(8)] @ m.T
            for fname, (idx, uvf) in FACES.items():
                u1, v1, u2, v2 = uvf(b.u, b.v, b.w, b.h, b.d)
                # orden de UV de Minecraft: vert[0]->(u2,v1) [1]->(u1,v1) [2]->(u1,v2) [3]->(u2,v2)
                uvs = np.array([[u2, v1], [u1, v1], [u1, v2], [u2, v2]])
                out.append((ch[idx, :3], uvs, fname))
    return out


def render(tex, glow=None, offsets=None, yaw=200, pitch=-12, size=(360, 520), zoom=None, body_yaw=0.0,
           hidden=(), light=True, bg=(46, 44, 52), center_y=2.9):
    """Imagen RGB del rey. yaw/pitch: camara en grados. Devuelve array HxWx3 uint8."""
    W, H = size
    mats = part_matrices(offsets)
    # como LivingEntityRenderer: girar 180-yaw, voltear X e Y, bajar 1.501
    ent = ry((180 - body_yaw) * D) @ sc(-1, -1, 1) @ tr(0, -1.501, 0)
    cam = rx(-pitch * D) @ ry(-yaw * D)
    zoom = zoom or H / 7.4
    img = np.zeros((H, W, 3))
    img[:] = np.array(bg) / 255.0
    zbuf = np.full((H, W), -1e9)
    th, tw = tex.shape[:2]
    for pts, uvs, fname in quads(mats, hidden):
        wp = np.c_[pts, np.ones(4)] @ ent.T
        wp[:, 1] -= center_y
        cp = wp @ cam.T
        sx = W / 2 + cp[:, 0] * zoom
        sy = H / 2 - cp[:, 1] * zoom
        sz = cp[:, 2]
        # luz por cara (normal en mundo)
        e1 = wp[1, :3] - wp[0, :3]
        e2 = wp[3, :3] - wp[0, :3]
        n = np.cross(e1, e2)
        nn = np.linalg.norm(n)
        if nn < 1e-12:
            continue
        n /= nn
        shade = 1.0
        if light:
            shade = 0.55 + 0.45 * max(0.0, n @ np.array([0.3, 0.8, -0.5]) / np.linalg.norm([0.3, 0.8, -0.5]))
        for tri in ((0, 1, 2), (0, 2, 3)):
            raster(img, zbuf, sx[list(tri)], sy[list(tri)], sz[list(tri)], uvs[list(tri)], tex, glow, shade)
    return (np.clip(img, 0, 1) * 255).astype(np.uint8)


def raster(img, zbuf, xs, ys, zs, uvs, tex, glow, shade):
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
    inside = (a >= -1e-6) & (b >= -1e-6) & (c >= -1e-6)
    if not inside.any():
        return
    z = a * zs[0] + b * zs[1] + c * zs[2]
    u = a * uvs[0, 0] + b * uvs[1, 0] + c * uvs[2, 0]
    v = a * uvs[0, 1] + b * uvs[1, 1] + c * uvs[2, 1]
    th, tw = tex.shape[:2]
    tu = np.clip((u * SCALE).astype(int), 0, tw - 1)
    tv = np.clip((v * SCALE).astype(int), 0, th - 1)
    sub = zbuf[y0:y1 + 1, x0:x1 + 1]
    m = inside & (z > sub)
    if not m.any():
        return
    if tex.shape[2] == 4:
        a = tex[tv, tu, 3]
        m = m & (a > 0.5)
        if not m.any():
            return
    color = tex[tv[m], tu[m], :3] * shade
    if glow is not None:
        gcol = glow[tv[m], tu[m]]
        color = np.maximum(color, gcol[:, :3] * gcol[:, 3:4])
    sub[m] = z[m]
    reg = img[y0:y1 + 1, x0:x1 + 1]
    reg[m] = color
