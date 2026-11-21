"""Piezas del modelo de Corvin, el Rey que Espera (unos 3 bloques de alto).

Coordenadas de modelo de Minecraft: en pixeles (16 = 1 bloque), Y hacia ABAJO, el frente mira a -Z y
el lado derecho del rey es -X (como en los modelos de vanilla). Cada pieza tiene su pivote (relativo a
la pieza madre), una rotacion inicial en grados y sus cajas (relativas al pivote).

Cada caja lleva un "material" que dice como se pinta (ver pintar.py) y, si se quiere, una clave "uv"
para que dos cajas compartan la misma zona de la textura (las dos espadas, por ejemplo).
"""
from dataclasses import dataclass, field


@dataclass
class Box:
    x: float
    y: float
    z: float
    w: int
    h: int
    d: int
    mat: str
    grow: tuple = (0.0, 0.0, 0.0)
    uv: str = ""          # zona compartida
    u: int = 0            # se rellenan al empaquetar
    v: int = 0


@dataclass
class Part:
    name: str
    parent: str
    pivot: tuple
    rot: tuple = (0.0, 0.0, 0.0)
    boxes: list = field(default_factory=list)


def g(v):
    return (v, v, v)


PARTS = [
    Part("raiz", "", (0, 24, 0)),
    Part("cadera", "raiz", (0, -18, 0), boxes=[
        Box(-6.5, -1.5, -3.5, 13, 3, 7, "pantalon"),
    ]),
    # ---------------------------------------------------------------- piernas
    Part("muslo_d", "cadera", (-3.3, 0.5, 0), boxes=[Box(-3, 0, -3, 6, 9, 6, "pantalon", g(-0.05))]),
    Part("pierna_d", "muslo_d", (0, 9, 0), boxes=[
        Box(-3, 0, -3, 6, 9, 6, "pantalon", g(-0.25)),
        Box(-3.5, 4, -4.5, 7, 5, 8, "bota", g(-0.25)),
    ]),
    Part("muslo_i", "cadera", (3.3, 0.5, 0), boxes=[Box(-3, 0, -3, 6, 9, 6, "pantalon", g(-0.05))]),
    Part("pierna_i", "muslo_i", (0, 9, 0), boxes=[
        Box(-3, 0, -3, 6, 9, 6, "pantalon", g(-0.25)),
        Box(-3.5, 4, -4.5, 7, 5, 8, "bota", g(-0.25)),
    ]),
    # ---------------------------------------------------------------- faldones de la chaqueta
    Part("falda_d", "cadera", (-3.6, -0.5, -4.6), boxes=[Box(-3.4, 0, -0.5, 7, 12, 1, "faldon_del")]),
    Part("falda_i", "cadera", (3.6, -0.5, -4.6), boxes=[Box(-3.6, 0, -0.5, 7, 12, 1, "faldon_del")]),
    Part("falda_t", "cadera", (0, -0.5, 4.6), boxes=[Box(-7.5, 0, -0.5, 15, 13, 1, "faldon_tras")]),
    Part("falda_ld", "cadera", (-7.1, -0.5, 0), boxes=[Box(-0.5, 0, -4.5, 1, 12, 9, "faldon_lado")]),
    Part("falda_li", "cadera", (7.1, -0.5, 0), boxes=[Box(-0.5, 0, -4.5, 1, 12, 9, "faldon_lado")]),
    # ---------------------------------------------------------------- tronco
    Part("torso", "cadera", (0, 0, 0), boxes=[
        Box(-6.5, -7, -4, 13, 7, 8, "chaqueta_bajo"),
        Box(-7, -17, -4.5, 14, 10, 9, "chaqueta_pecho"),
        Box(-7, -2.5, -4.5, 14, 3, 9, "cinturon", g(0.15)),
        Box(-7.3, -18.4, -4.9, 15, 2, 10, "armino", (-0.15, 0, -0.1)),
    ]),
    # ---------------------------------------------------------------- cabeza
    Part("cabeza", "torso", (0, -18, -0.3), boxes=[
        Box(-4.5, -9, -4.5, 9, 9, 9, "cara"),
        Box(-1, -5, -5.5, 2, 3, 1, "nariz", g(-0.15)),
        Box(-4, -6.6, -5, 8, 1, 1, "cejas", (0, 0.05, -0.1)),
        Box(-3.5, -3.4, -5.3, 7, 1, 1, "bigote", (0, 0.1, -0.05)),
    ]),
    Part("barba", "cabeza", (0, -2.2, -4.6), boxes=[Box(-4, 0, -1.6, 8, 7, 2, "barba")]),
    Part("barba_punta", "barba", (0, 7, -0.6), boxes=[Box(-2.5, 0, -0.9, 5, 4, 2, "barba_punta", g(-0.1))]),
    Part("pelo", "cabeza", (0, -6, 4.5), boxes=[Box(-5, -3.5, -0.2, 10, 13, 2, "pelo", g(-0.2))]),
    Part("pelo_d", "cabeza", (-4.7, -6, 0.2), boxes=[Box(-0.8, 0, -2.5, 2, 9, 5, "pelo_lado", g(-0.2))]),
    Part("pelo_i", "cabeza", (4.7, -6, 0.2), boxes=[Box(-1.2, 0, -2.5, 2, 9, 5, "pelo_lado", g(-0.2))]),
    # ---------------------------------------------------------------- corona (cae al morir)
    Part("corona", "cabeza", (0, -9, 0), boxes=[
        Box(-5, -1.5, -5, 10, 3, 10, "corona", g(0.05)),
        Box(-1, -4.5, -5.4, 2, 3, 1, "pico", (-0.3, 0, -0.2)),
        Box(-5.4, -4.5, -1, 1, 3, 2, "pico", (-0.2, 0, -0.3)),
        Box(4.4, -4.5, -1, 1, 3, 2, "pico", (-0.2, 0, -0.3)),
        Box(-1, -4.5, 4.4, 2, 3, 1, "pico", (-0.3, 0, -0.2)),
        Box(-5.2, -3.5, -5.2, 1, 2, 1, "pico", (0, 0, 0)),
        Box(-5.2, -3.5, 4.2, 1, 2, 1, "pico", (0, 0, 0)),
        Box(4.2, -3.5, 4.2, 1, 2, 1, "pico", (0, 0, 0)),
    ]),
    # el pico roto, torcido
    Part("pico_roto", "corona", (4.6, -1.5, -4.6), rot=(-12, 0, 18), boxes=[Box(-0.5, -2, -0.5, 1, 2, 1, "pico_roto")]),
    # ---------------------------------------------------------------- brazos
    Part("brazo_d", "torso", (-8.6, -15.2, 0), boxes=[Box(-3.4, -1.8, -3, 6, 9, 6, "manga", g(-0.2))]),
    Part("antebrazo_d", "brazo_d", (-0.4, 7.2, 0), boxes=[
        Box(-2.5, 0, -2.5, 5, 8, 5, "manga_baja"),
        Box(-3, 0.5, -3, 6, 3, 6, "puno", g(-0.1)),
        Box(-2, 8, -2, 4, 3, 4, "mano"),
    ]),
    Part("espada_d", "antebrazo_d", (0, 9.6, -0.2), rot=(55, 0, 0), boxes=[
        Box(-0.5, -0.5, -1.5, 1, 1, 5, "puno_espada", g(0.12), uv="esp_grip"),
        Box(-1, -1, 3.5, 2, 2, 2, "pomo", g(-0.2), uv="esp_pomo"),
        Box(-1, -4.5, -2.6, 2, 9, 1, "guarda", (-0.25, 0, -0.1), uv="esp_guarda"),
        Box(-0.5, -1, -28.6, 1, 2, 26, "hoja", (-0.12, 0.1, 0), uv="esp_hoja"),
        Box(-0.5, -0.5, -29.6, 1, 1, 1, "punta", (-0.12, 0, 0), uv="esp_punta"),
    ]),
    Part("brazo_i", "torso", (8.6, -15.2, 0), boxes=[Box(-2.6, -1.8, -3, 6, 9, 6, "manga", g(-0.2))]),
    Part("antebrazo_i", "brazo_i", (0.4, 7.2, 0), boxes=[
        Box(-2.5, 0, -2.5, 5, 8, 5, "manga_baja"),
        Box(-3, 0.5, -3, 6, 3, 6, "puno", g(-0.1)),
        Box(-2, 8, -2, 4, 3, 4, "mano"),
    ]),
    Part("espada_i", "antebrazo_i", (0, 9.6, -0.2), rot=(55, 0, 0), boxes=[
        Box(-0.5, -0.5, -1.5, 1, 1, 5, "puno_espada", g(0.12), uv="esp_grip"),
        Box(-1, -1, 3.5, 2, 2, 2, "pomo", g(-0.2), uv="esp_pomo"),
        Box(-1, -4.5, -2.6, 2, 9, 1, "guarda", (-0.25, 0, -0.1), uv="esp_guarda"),
        Box(-0.5, -1, -28.6, 1, 2, 26, "hoja", (-0.12, 0.1, 0), uv="esp_hoja"),
        Box(-0.5, -0.5, -29.6, 1, 1, 1, "punta", (-0.12, 0, 0), uv="esp_punta"),
    ]),
]

BY_NAME = {p.name: p for p in PARTS}

# tamano de la textura en unidades de UV (el PNG va a doble resolucion)
TEX_W, TEX_H = 256, 256
SCALE = 2


def pack():
    """Coloca la zona de cada caja en la textura (estanterias), compartiendo las que tienen clave uv."""
    shared = {}
    items = []
    for p in PARTS:
        for b in p.boxes:
            if b.uv and b.uv in shared:
                continue
            items.append(b)
            if b.uv:
                shared[b.uv] = b
    items.sort(key=lambda b: -(b.d + b.h))
    x = y = 0
    row_h = 0
    for b in items:
        bw, bh = 2 * (b.d + b.w), b.d + b.h
        if x + bw > TEX_W:
            x = 0
            y += row_h + 1
            row_h = 0
        b.u, b.v = x, y
        x += bw + 1
        row_h = max(row_h, bh)
    if y + row_h > TEX_H:
        raise SystemExit(f"no cabe en la textura: {y + row_h} > {TEX_H}")
    for p in PARTS:
        for b in p.boxes:
            if b.uv:
                b.u, b.v = shared[b.uv].u, shared[b.uv].v
    return y + row_h
