#!/usr/bin/env python3
"""
Dibuja un volcado de Volcado.java (un plano del mod levantado sin abrir Minecraft): la planta desde arriba, vistas en
perspectiva ortografica desde las cuatro esquinas (o desde donde se pida), plantas por altura y cortes.

    python3 tools/vista3d/vista.py volcado.bin carpeta [opciones]

    --jar client-extra.jar        de donde salen los colores de los bloques (por defecto /tmp/cp/cpjars/client-extra.jar)
    --vistas "planta;so;ne"       que dibujar, separado por ";" (so = desde el suroeste mirando al noreste...); tambien
                                  "y=266" (planta a esa altura), "x=10" o "z=-4" (cortes) y "az,el" en grados ("200,25")
    --caja x0 y0 z0 x1 y1 z1      solo esa caja (coordenadas del mundo)
    --techo Y                     quita todo lo que haya por encima de Y (para ver interiores)
    --zoom n                      pixeles por bloque (por defecto 4)
    --nombre prefijo              prefijo de los ficheros (por defecto el del volcado)
    --sombras                     sombra del sol (mas lento)
"""
import gzip
import math
import os
import struct
import sys
import zipfile
from io import BytesIO

import numpy as np
from PIL import Image, ImageDraw

CESPED = (0x6B, 0x80, 0x5C)      # el verde apagado de la isla del pueblo
FOLLAJE = (0x5A, 0x6E, 0x4C)
AGUA = (0x3F, 0x76, 0xE4)

# formas: 0 entero, 1 media losa de abajo, 2 media losa de arriba, 3 fino abajo, 4 fino arriba, 5 ralo (plantas, vallas...),
# 6 cristal, 7 nube
ENTERO, ABAJO, ARRIBA, FINO_ABAJO, FINO_ARRIBA, RALO, CRISTAL, NUBE = range(8)

RALOS = ("grass", "tall_grass", "fern", "large_fern", "dead_bush", "cobweb", "torch", "lantern", "chain", "candle", "ladder", "vine",
         "rail", "button", "sign", "banner", "skull", "head", "flower_pot", "potted", "lever", "tripwire", "iron_bars", "pane", "fence",
         "wall", "sapling", "mushroom", "rose", "dandelion", "poppy", "tulip", "orchid", "allium", "bluet", "daisy", "cornflower",
         "lily", "wheat", "carrots", "potatoes", "beetroots", "sugar_cane", "bamboo", "roots", "dripstone", "rod", "bell", "campfire",
         "sweet_berry", "azalea", "door", "scaffolding", "string", "hanging", "lichen", "sprouts", "fungus", "amethyst_cluster",
         "brewing_stand", "cauldron", "anvil", "grindstone", "lectern", "hopper", "composter", "bed", "stonecutter", "end_rod")


def leer(ruta):
    with gzip.open(ruta, "rb") as f:
        d = f.read()
    o = 0
    magia, minx, miny, minz, sx, sy, sz, n = struct.unpack_from(">8i", d, o)
    o += 32
    assert magia == 0x56495354, "no es un volcado"
    paleta = []
    for _ in range(n):
        (l,) = struct.unpack_from(">H", d, o)
        o += 2
        paleta.append(d[o:o + l].decode())
        o += l
    datos = np.frombuffer(d, dtype=">u2", count=sx * sy * sz, offset=o).reshape(sx, sz, sy).astype(np.uint16)
    o += sx * sy * sz * 2
    marcas = {}
    if o + 4 <= len(d):
        (m,) = struct.unpack_from(">i", d, o)
        o += 4
        for _ in range(m):
            (l,) = struct.unpack_from(">H", d, o)
            o += 2
            nombre = d[o:o + l].decode()
            o += l
            marcas[nombre] = struct.unpack_from(">3i", d, o)
            o += 12
    return (minx, miny, minz), datos, paleta, marcas


class Colores:
    def __init__(self, jar):
        self.zip = zipfile.ZipFile(jar) if jar and os.path.exists(jar) else None
        self.nombres = set()
        if self.zip:
            for n in self.zip.namelist():
                if n.startswith("assets/minecraft/textures/block/") and n.endswith(".png"):
                    self.nombres.add(n[len("assets/minecraft/textures/block/"):-4])
        self.cache = {}

    def media(self, tex):
        if tex in self.cache:
            return self.cache[tex]
        c = None
        if self.zip and tex in self.nombres:
            im = Image.open(BytesIO(self.zip.read("assets/minecraft/textures/block/" + tex + ".png"))).convert("RGBA")
            a = np.asarray(im, dtype=np.float64)
            a = a[:a.shape[1]]  # el primer fotograma de las animadas
            w = a[..., 3] / 255.0
            if w.sum() > 0:
                c = tuple((a[..., :3] * w[..., None]).sum(axis=(0, 1)) / w.sum())
        self.cache[tex] = c
        return c

    def de(self, estado):
        """(r, g, b, forma, alfa) de un estado 'minecraft:spruce_slab[type=top,...]'."""
        nombre, _, props = estado.partition("[")
        props = dict(p.split("=") for p in props.rstrip("]").split(",") if "=" in p)
        esp, _, n = nombre.partition(":")
        forma, alfa = ENTERO, 1.0
        if esp == "darkcraft":
            if n == "nube":
                t = int(props.get("tono", 0))
                return (*[(244, 242, 251), (230, 228, 244), (211, 209, 233), (186, 185, 216)][t], NUBE, 0.82)
            if n == "nube_firme":
                return (233, 222, 203, ENTERO, 1.0)
            return (150, 60, 60, ENTERO, 1.0)
        if n.endswith("_slab"):
            forma = {"top": ARRIBA, "bottom": ABAJO}.get(props.get("type"), ENTERO)
        elif n.endswith("_trapdoor"):
            forma = RALO if props.get("open") == "true" else (FINO_ARRIBA if props.get("half") == "top" else FINO_ABAJO)
        elif n.endswith("_carpet") or n.endswith("pressure_plate") or n in ("snow", "moss_carpet", "lily_pad"):
            forma = FINO_ABAJO
        elif n.endswith("glass"):
            forma, alfa = CRISTAL, 0.35
        elif n == "water":
            return (*AGUA, CRISTAL, 0.6)
        elif any(r in n for r in RALOS) and not n.endswith(("_block", "_planks", "_bricks", "_log", "_wood", "_stairs")) \
                and n not in ("grass_block", "cobblestone_wall_x"):
            forma, alfa = RALO, 0.55
            if "leaves" in n:
                forma, alfa = ENTERO, 1.0
        # textura
        cands = [n + "_top", n, n + "_side", n + "_front", n + "_stage7", n + "_bottom"]
        if n in ("grass_block", "podzol", "mycelium", "dirt_path", "farmland"):
            cands = [n + "_top"] if n != "farmland" else ["farmland"]
        for suf in ("_stairs", "_slab", "_wall", "_fence_gate", "_fence", "_door", "_trapdoor", "_button", "_pressure_plate",
                    "_wall_sign", "_sign", "_carpet", "_pane", "_wood", "_hyphae"):
            if n.endswith(suf):
                b = n[:-len(suf)]
                extra = {"_door": [b + "_door_top"], "_trapdoor": [b + "_trapdoor"], "_carpet": [b + "_wool", b], "_wood": [b + "_log"],
                         "_hyphae": [b + "_stem"], "_pane": [b]}.get(suf, [])
                cands = extra + [b, b + "s", b + "_planks", b.replace("brick", "bricks"), b.replace("tile", "tiles"), b + "_top",
                                 b + "_block", "smooth_" + b] + cands
                break
        if n.startswith("stripped_") and n.endswith("_wood"):
            cands = [n[:-5] + "_log"] + cands
        if n.startswith("potted_"):
            cands = [n[7:]] + cands
        if n == "lantern" or n == "soul_lantern" or "torch" in n or "campfire" in n:
            alfa = 0.9
        c = None
        for t in cands:
            c = self.media(t)
            if c is not None:
                break
        if c is None:
            c = (128, 128, 128) if n != "air" else (0, 0, 0)
        if n in ("grass_block", "grass", "tall_grass", "fern", "large_fern"):
            c = tuple(c[i] * CESPED[i] / 150.0 for i in range(3))
        elif "leaves" in n or n == "vine":
            if n not in ("cherry_leaves", "azalea_leaves", "flowering_azalea_leaves"):
                c = tuple(c[i] * FOLLAJE[i] / 150.0 for i in range(3))
        return (*[min(255.0, v) for v in c], forma, alfa)


def tabla(paleta, colores):
    t = np.zeros((len(paleta), 5), dtype=np.float32)
    for i, p in enumerate(paleta):
        if p == "minecraft:air":
            t[i] = (0, 0, 0, ENTERO, 0)
        else:
            t[i] = colores.de(p)
    return t


def recortar(origen, datos, caja, techo):
    minx, miny, minz = origen
    if caja:
        x0, y0, z0, x1, y1, z1 = caja
        a = (max(0, x0 - minx), max(0, z0 - minz), max(0, y0 - miny))
        b = (min(datos.shape[0], x1 - minx + 1), min(datos.shape[1], z1 - minz + 1), min(datos.shape[2], y1 - miny + 1))
        datos = datos[a[0]:b[0], a[1]:b[1], a[2]:b[2]]
        origen = (minx + a[0], miny + a[2], minz + a[1])
    if techo is not None:
        k = techo - origen[1] + 1
        if 0 < k < datos.shape[2]:
            datos = datos[:, :, :k]
    return origen, datos


def planta(origen, datos, tab, zoom, marcas):
    """Desde arriba: el bloque mas alto de cada columna (sin contar nubes ni lo ralo si hay algo debajo), con relieve."""
    sx, sz, sy = datos.shape
    forma = tab[datos, 3]
    alfa = tab[datos, 4]
    solido = (alfa > 0) & (forma != NUBE)
    ys = np.arange(sy)[None, None, :]
    alto = np.where(solido, ys, -1).max(axis=2)
    hay = alto >= 0
    idx = np.take_along_axis(datos, np.maximum(alto, 0)[:, :, None], axis=2)[:, :, 0]
    col = tab[idx, :3].copy()
    # relieve: luz del noroeste
    h = alto.astype(np.float32)
    gx = np.zeros_like(h)
    gz = np.zeros_like(h)
    gx[1:, :] = h[1:, :] - h[:-1, :]
    gz[:, 1:] = h[:, 1:] - h[:, :-1]
    luz = np.clip(1.0 - 0.10 * np.clip(gx, -3, 3) - 0.10 * np.clip(gz, -3, 3), 0.55, 1.35)
    hm = h[hay]
    base = 0.72 + 0.28 * (h - hm.min()) / max(1.0, hm.max() - hm.min()) if hay.any() else 1.0
    col *= (luz * base)[:, :, None]
    # donde no hay nada: nube o cielo
    nube = ((forma == NUBE) & (alfa > 0)).any(axis=2)
    img = np.zeros((sx, sz, 3), dtype=np.float32)
    img[:] = (58, 74, 112)
    img[nube & ~hay] = (214, 212, 232)
    img[hay] = col[hay]
    im = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8).transpose(1, 0, 2), "RGB").resize((sx * zoom, sz * zoom), Image.NEAREST)
    d = ImageDraw.Draw(im)
    # rejilla cada 16 y ejes
    for x in range(origen[0] - origen[0] % 16 + 16, origen[0] + sx, 16):
        d.line([((x - origen[0]) * zoom, 0), ((x - origen[0]) * zoom, 6 if x % 64 else 14)], fill=(255, 255, 255))
        if x % 64 == 0:
            d.text(((x - origen[0]) * zoom + 2, 14), str(x), fill=(255, 255, 255))
    for z in range(origen[2] - origen[2] % 16 + 16, origen[2] + sz, 16):
        d.line([(0, (z - origen[2]) * zoom), (6 if z % 64 else 14, (z - origen[2]) * zoom)], fill=(255, 255, 255))
        if z % 64 == 0:
            d.text((16, (z - origen[2]) * zoom + 2), str(z), fill=(255, 255, 255))
    for nombre, (x, y, z) in marcas.items():
        px, pz = (x - origen[0] + 0.5) * zoom, (z - origen[2] + 0.5) * zoom
        if 0 <= px < im.width and 0 <= pz < im.height:
            d.ellipse([px - 3, pz - 3, px + 3, pz + 3], outline=(255, 40, 40), width=2)
            d.text((px + 5, pz - 5), nombre, fill=(255, 235, 120), stroke_width=1, stroke_fill=(0, 0, 0))
    return im


def nivel(origen, datos, tab, zoom, y, marcas):
    """Planta a una altura: lo que hay en Y (lo que se pisa en Y - 1 mas apagado)."""
    k = y - origen[1]
    sx, sz, sy = datos.shape
    if not 0 <= k < sy:
        return None
    img = np.zeros((sx, sz, 3), dtype=np.float32)
    img[:] = (24, 26, 34)
    if k > 0:
        aba = datos[:, :, k - 1]
        m = tab[aba, 4] > 0
        img[m] = tab[aba, :3][m] * 0.45
    cap = datos[:, :, k]
    m = tab[cap, 4] > 0
    f = tab[cap, 3]
    img[m] = tab[cap, :3][m] * np.where(f[m] == RALO, 0.85, 1.0)[:, None]
    if k + 1 < sy:
        arr = datos[:, :, k + 1]
        m2 = (tab[arr, 4] > 0) & m & (tab[arr, 3] != RALO)
        img[m2] *= 1.12  # los muros (siguen hacia arriba) algo mas claros
    im = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8).transpose(1, 0, 2), "RGB").resize((sx * zoom, sz * zoom), Image.NEAREST)
    d = ImageDraw.Draw(im)
    for nombre, (x, yy, z) in marcas.items():
        if abs(yy - y) > 3:
            continue
        px, pz = (x - origen[0] + 0.5) * zoom, (z - origen[2] + 0.5) * zoom
        d.ellipse([px - 3, pz - 3, px + 3, pz + 3], outline=(255, 40, 40), width=2)
        d.text((px + 5, pz - 5), nombre, fill=(255, 235, 120), stroke_width=1, stroke_fill=(0, 0, 0))
    return im


def corte(origen, datos, tab, zoom, eje, v):
    """Corte vertical por x = v o z = v."""
    sx, sz, sy = datos.shape
    if eje == "x":
        k = v - origen[0]
        if not 0 <= k < sx:
            return None
        cap = datos[k, :, :]
    else:
        k = v - origen[2]
        if not 0 <= k < sz:
            return None
        cap = datos[:, k, :]
    img = np.zeros((cap.shape[0], sy, 3), dtype=np.float32)
    img[:] = (58, 74, 112)
    m = tab[cap, 4] > 0
    img[m] = tab[cap, :3][m]
    im = Image.fromarray(np.clip(img, 0, 255).astype(np.uint8).transpose(1, 0, 2)[::-1], "RGB")
    return im.resize((cap.shape[0] * zoom, sy * zoom), Image.NEAREST)


def perspectiva(origen, datos, tab, zoom, az, el, sombras=False, paso=0.25):
    """
    Vista ortografica: la camara mira hacia (az, el) (az en grados: 0 = hacia el sur (+z), 90 = hacia el oeste (-x), como
    la guinada de Minecraft; el = cuanto mira hacia abajo). Rayos a paso fijo dentro de la caja, saltando de golpe las
    celdas de 4 x 4 x 4 vacias; las formas a medias (losas, trampillas) se comprueban con la parte de bloque en que cae
    cada muestra; lo ralo, el cristal y la nube dejan pasar parte.
    """
    sx, sz, sy = datos.shape
    a, e = math.radians(az), math.radians(el)
    d = np.array([-math.sin(a) * math.cos(e), -math.sin(e), math.cos(a) * math.cos(e)])  # x, y, z
    d = np.where(np.abs(d) < 1e-9, 1e-9, d)
    der = np.array([-math.cos(a), 0.0, -math.sin(a)])
    arr = np.cross(der, d)
    arr /= np.linalg.norm(arr)
    if arr[1] < 0:
        arr = -arr
    tam = np.array([sx, sy, sz], dtype=np.float64)
    esq = np.array([[x, y, z] for x in (0, sx) for y in (0, sy) for z in (0, sz)], dtype=np.float64)
    u, v, w = esq @ der, esq @ arr, esq @ d
    W, H = int(math.ceil((u.max() - u.min()) * zoom)) + 2, int(math.ceil((v.max() - v.min()) * zoom)) + 2
    jj, ii = np.meshgrid(np.arange(H), np.arange(W), indexing="ij")
    pu = u.min() + (ii.ravel() + 0.5) / zoom
    pv = v.max() - (jj.ravel() + 0.5) / zoom
    o = pu[:, None] * der[None, :] + pv[:, None] * arr[None, :] + (w.min() - 1.0) * d[None, :]
    n = o.shape[0]
    # por donde entra y sale cada rayo de la caja
    t1, t2 = (0.0 - o) / d[None, :], (tam[None, :] - o) / d[None, :]
    tent = np.minimum(t1, t2).max(axis=1)
    tsal = np.maximum(t1, t2).min(axis=1)
    # ocupacion gruesa (celdas de C bloques)
    C = 4
    gx, gy, gz = (sx + C - 1) // C, (sy + C - 1) // C, (sz + C - 1) // C
    lleno = np.zeros((gx * C, gz * C, gy * C), dtype=bool)
    lleno[:sx, :sz, :sy] = tab[datos, 4] > 0
    grueso = lleno.reshape(gx, C, gz, C, gy, C).any(axis=(1, 3, 5))
    color = np.zeros((n, 3), dtype=np.float32)
    resto = np.ones(n, dtype=np.float32)
    prof = np.full(n, np.inf, dtype=np.float32)
    activos = np.nonzero(tsal > np.maximum(tent, 0.0))[0]
    t = np.maximum(tent[activos], 0.0) + 1e-4
    fin = tsal[activos]
    ant = np.full((activos.size, 3), -99999, dtype=np.int32)
    eje_salto = np.ones(activos.size, dtype=np.int8)
    luz_cara = {0: (0.80, 0.80), 1: (0.52, 1.0), 2: (0.66, 0.66)}  # eje -> luz de la cara negativa y de la positiva
    pos_d = (d > 0)
    vueltas = 0
    while activos.size and vueltas < 6000:
        vueltas += 1
        p = o[activos] + t[:, None] * d[None, :]
        q = np.floor(p).astype(np.int32)
        np.clip(q[:, 0], 0, sx - 1, out=q[:, 0])
        np.clip(q[:, 1], 0, sy - 1, out=q[:, 1])
        np.clip(q[:, 2], 0, sz - 1, out=q[:, 2])
        cq = q // C
        vacio = ~grueso[cq[:, 0], cq[:, 2], cq[:, 1]]
        avance = np.full(activos.size, paso)
        if vacio.any():
            # hasta donde sale de la celda gruesa
            borde = (cq[vacio] + pos_d[None, :]) * C
            ts = (borde - p[vacio]) / d[None, :]
            k = ts.argmin(axis=1)
            avance[vacio] = np.maximum(ts[np.arange(k.size), k], 0.0) + 1e-3
            eje_salto[vacio] = k
        lleno_ = ~vacio
        if lleno_.any():
            ql = q[lleno_]
            idx = datos[ql[:, 0], ql[:, 2], ql[:, 1]]
            fila = tab[idx]
            alfa = fila[:, 4].copy()
            forma = fila[:, 3]
            fy = p[lleno_, 1] - ql[:, 1]
            alfa[(forma == ABAJO) & (fy > 0.5)] = 0
            alfa[(forma == ARRIBA) & (fy < 0.5)] = 0
            alfa[(forma == FINO_ABAJO) & (fy > 0.19)] = 0
            alfa[(forma == FINO_ARRIBA) & (fy < 0.81)] = 0
            al = ant[lleno_]
            valido = al[:, 0] > -9999
            cambio = (ql != al) & valido[:, None]
            eje = np.where(cambio[:, 1], 1, np.where(cambio[:, 0], 0, np.where(cambio[:, 2], 2, 1)))
            eje = np.where(valido, eje, eje_salto[lleno_])
            luz = np.ones(ql.shape[0], dtype=np.float32)
            for ex in (0, 1, 2):
                neg, pos = luz_cara[ex]
                luz[eje == ex] = pos if d[ex] < 0 else neg
            parcial = (forma == RALO) | (forma == CRISTAL) | (forma == NUBE)
            mismo = valido & ~cambio.any(axis=1)
            alfa[parcial & mismo] = 0
            toca = alfa > 0
            if toca.any():
                ef = np.where(parcial, alfa, 1.0).astype(np.float32)
                ef[~toca] = 0
                luz = np.where(forma == NUBE, 0.94 + 0.06 * luz, luz)
                ia = activos[lleno_]
                color[ia] += (resto[ia] * ef)[:, None] * (fila[:, :3] * luz[:, None])
                nueva = toca & (prof[ia] == np.inf) & (ef > 0.5)
                prof[ia[nueva]] = t[lleno_][nueva]
                resto[ia] *= (1.0 - ef)
        # el bloque anterior: solo vale si no se salto
        ant[lleno_] = q[lleno_]
        ant[vacio] = -99999
        t = t + avance
        sigue = (t < fin) & (resto[activos] > 0.03)
        if not sigue.all():
            activos, t, fin, ant, eje_salto = activos[sigue], t[sigue], fin[sigue], ant[sigue], eje_salto[sigue]
    # el cielo de fondo y un velo con la distancia (lo lejano mas palido)
    cielo = np.empty((n, 3), dtype=np.float32)
    g = (jj.ravel() / max(1, H - 1)).astype(np.float32)
    cielo[:, 0] = 96 + 60 * g
    cielo[:, 1] = 118 + 60 * g
    cielo[:, 2] = 170 + 50 * g
    hay = prof < np.inf
    if hay.any():
        pmin, pmax = prof[hay].min(), prof[hay].max()
        velo = np.clip((prof - pmin) / max(1.0, pmax - pmin), 0, 1) * 0.22
        velo[~hay] = 0
        color = color * (1 - velo[:, None]) + np.array([150, 165, 200], dtype=np.float32)[None, :] * (velo * (1 - resto))[:, None]
    color += resto[:, None] * cielo
    return Image.fromarray(np.clip(color, 0, 255).astype(np.uint8).reshape(H, W, 3), "RGB")


ESQUINAS = {"so": (225, 34), "se": (135, 34), "no": (315, 34), "ne": (45, 34), "s": (180, 30), "n": (0, 30), "e": (90, 30), "o": (270, 30)}


def main():
    if len(sys.argv) < 3:
        print(__doc__)
        return
    ruta, carpeta = sys.argv[1], sys.argv[2]
    arg = sys.argv[3:]

    def op(nombre, n=1, por=None):
        if nombre in arg:
            i = arg.index(nombre)
            return arg[i + 1] if n == 1 else arg[i + 1:i + 1 + n]
        return por

    jar = op("--jar", por="/tmp/cp/cpjars/client-extra.jar")
    vistas = op("--vistas", por="planta;so;ne").split(";")
    caja = op("--caja", 6)
    caja = [int(v) for v in caja] if caja else None
    techo = op("--techo")
    techo = int(techo) if techo is not None else None
    zoom = int(op("--zoom", por="4"))
    nombre = op("--nombre", por=os.path.splitext(os.path.basename(ruta))[0])
    os.makedirs(carpeta, exist_ok=True)
    origen, datos, paleta, marcas = leer(ruta)
    tab = tabla(paleta, Colores(jar))
    origen, datos = recortar(origen, datos, caja, techo)
    print("caja", origen, datos.shape, "estados", len(paleta), "marcas", len(marcas))
    for v in vistas:
        v = v.strip()
        if not v:
            continue
        if v == "planta":
            im = planta(origen, datos, tab, zoom, marcas)
        elif v.startswith("y="):
            im = nivel(origen, datos, tab, zoom, int(v[2:]), marcas)
        elif v.startswith("x=") or v.startswith("z="):
            im = corte(origen, datos, tab, zoom, v[0], int(v[2:]))
        else:
            az, el = ESQUINAS[v] if v in ESQUINAS else [float(t) for t in v.split(",")]
            im = perspectiva(origen, datos, tab, zoom, az, el, "--sombras" in arg)
        if im is None:
            print("  ", v, "queda fuera")
            continue
        sal = os.path.join(carpeta, nombre + "_" + v.replace("=", "").replace(",", "_").replace(".", "") + ".png")
        im.save(sal)
        print("  ", sal, im.size)


if __name__ == "__main__":
    main()
