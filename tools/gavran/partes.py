"""Piezas del modelo de Ser Gavran, el campeon del Rey (unos 5,5 bloques de alto: 88 pixeles).

Mismas reglas que el modelo del rey (tools/rey/partes.py): coordenadas de modelo de Minecraft en pixeles
(16 = 1 bloque), Y hacia ABAJO, el suelo en y=24, el frente mira a -Z y el lado derecho del caballero es -X.
Cada pieza tiene su pivote (relativo a la pieza madre), una rotacion inicial en grados y sus cajas.

Alturas sobre el suelo (h): cadera 38, hombros 66, cuello 72, yelmo hasta 87, astas del yelmo hasta ~100.
El mandoble mide unos 80 pixeles (5 bloques): empunadura para las dos manos, gavilan de raices y una hoja
negra con runas verdes que brillan.
"""
from dataclasses import dataclass, field


@dataclass
class Box:
    x: float
    y: float
    z: float
    w: float
    h: float
    d: float
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


def M(s, x):
    """x en el lado derecho (s=-1) o espejado al izquierdo (s=1). x se da para el lado IZQUIERDO."""
    return x if s > 0 else -x


def caja_lado(s, x, y, z, w, h, d, mat, grow=(0.0, 0.0, 0.0), uv=""):
    """Caja dada para el lado izquierdo (+X); en el derecho se refleja en X."""
    if s > 0:
        return Box(x, y, z, w, h, d, mat, grow, uv)
    return Box(-x - w, y, z, w, h, d, mat, grow, uv)


def cadena(nombre, madre, pivote, rot0, segmentos, grosor=1.5, mat="raiz", mat0=None):
    """Raiz hecha de trozos encadenados (cada uno gira un poco respecto al anterior): parece que se curva.
    segmentos: [(largo, (rx, ry, rz) relativo)]. Devuelve la lista de piezas."""
    out = []
    madre_k, piv, rot = madre, pivote, rot0
    for k, (largo, r) in enumerate(segmentos):
        nombre_k = f"{nombre}{k}"
        if k > 0:
            rot = r
        out.append(Part(nombre_k, madre_k, piv, rot=rot, boxes=[
            Box(-grosor / 2, -0.5, -grosor / 2, grosor, largo + 0.5, grosor, (mat0 or mat) if k == 0 else mat)]))
        madre_k, piv = nombre_k, (0, largo, 0)
        if k % 2 == 1:
            grosor = max(1.0, grosor - 0.5)
    return out


PARTS = [
    Part("raiz", "", (0, 24, 0)),
    # ------------------------------------------------------------------ cadera y faldar
    Part("cadera", "raiz", (0, -38, 0), boxes=[
        Box(-11.5, -3, -7.5, 23, 6, 15, "cinturon"),
        Box(-3.5, -2.5, -8.6, 7, 5, 1.5, "hebilla"),
    ]),
    Part("tabardo", "cadera", (0, 2.6, -8.1), boxes=[Box(-5.5, 0, -0.5, 11, 25, 1, "tabardo")]),
    Part("tabardo_t", "cadera", (0, 2.6, 8.1), boxes=[Box(-6, 0, -0.5, 12, 22, 1, "tabardo_tras")]),
]

# escarcelas: placas sobre el muslo (delante) y a los lados de la cadera
for s, n in ((-1, "d"), (1, "i")):
    PARTS += [
        Part(f"escarcela_{n}", "cadera", (M(s, 6.6), 2.2, -6.9), boxes=[
            caja_lado(s, -4.6, 0, -1.2, 9.5, 12, 2.5, "escarcela" if s < 0 else "escarcela_i"),
            caja_lado(s, -4.1, 11.2, -1.0, 8.5, 2.5, 2, "escarcela_borde"),
        ]),
        Part(f"escarcela_l{n}", "cadera", (M(s, 11.4), 1.6, 0.5), boxes=[
            caja_lado(s, -1.2, 0, -6, 2.5, 11, 12, "escarcela_lado"),
        ]),
    ]

# ------------------------------------------------------------------ piernas
for s, n in ((-1, "d"), (1, "i")):
    raices = s > 0   # la pierna izquierda la envuelven raices
    PARTS += [
        Part(f"muslo_{n}", "cadera", (M(s, 6), 1.5, 0), boxes=[
            caja_lado(s, -4.5, 0, -5, 9, 17, 10, "quijote" if s < 0 else "quijote_i"),
        ]),
        Part(f"pierna_{n}", "muslo_{n}".format(n=n), (0, 17, 0.5), boxes=[
            caja_lado(s, -4, 0, -4.5, 8, 16, 9, "greba" if s < 0 else "greba_i"),
            caja_lado(s, -4.5, 15.5, -8.5, 9, 4, 14, "escarpe"),
            caja_lado(s, -3, 16.5, -11.3, 6, 3, 3, "escarpe_punta"),
            # rodillera con aleta
            caja_lado(s, -4.75, -3.2, -6.6, 9.5, 6, 3, "rodillera"),
            caja_lado(s, 3.6, -2.4, -5.2, 2, 5, 5.5, "rodillera_aleta"),
        ]),
    ]
    if raices:
        # tres raices que se enroscan a la greba y bajan hasta el pie
        for k, (yy, ang) in enumerate(((3.5, 14), (8.5, -11), (12.8, 9))):
            PARTS.append(Part(f"raiz_pierna{k}", f"pierna_{n}", (0, yy, 0), rot=(ang, 0, ang * 0.6), boxes=[
                Box(-4.75, -0.75, -5.25, 9.5, 1.5, 10.5, "raiz_banda"),
            ]))
        PARTS.append(Part("raiz_pie", f"pierna_{n}", (3.6, 13, -2), rot=(-18, 0, -8), boxes=[
            Box(-1, 0, -1, 2, 8.5, 2, "raiz"),
        ]))

# ------------------------------------------------------------------ tronco
PARTS += [
    Part("torso", "cadera", (0, 0, 0), boxes=[
        Box(-10, -13.5, -6.5, 20, 11, 13, "faldon_malla"),
        Box(-13, -31, -8, 26, 19, 16, "peto"),
        Box(-1.25, -30.5, -8.9, 2.5, 17.5, 1, "quilla"),
        Box(-12.5, -31.6, -8.5, 25, 2.5, 17, "peto_borde"),
        Box(-7.5, -35.5, -6.25, 15, 5.5, 12.5, "gorjal"),
        # placas del vientre (lamas) un poco salidas
        Box(-10.5, -15, -7.3, 21, 3, 1, "lama"),
        Box(-10.25, -11.6, -7.1, 20.5, 3, 1, "lama"),
        Box(-9.75, -8.2, -6.9, 19.5, 3, 1, "lama"),
        # costra de corteza que rodea el corazon del arbol (lado izquierdo del pecho)
        Box(-0.6, -29, -8.9, 10.5, 13, 1.5, "corteza_pecho"),
        Box(9.2, -27, -6.5, 4.5, 10, 9, "corteza_costado"),
        Box(2.4, -25.4, -9.6, 4.5, 5.5, 1, "corazon"),
        # placa de la espalda con el escudo grabado (bajo la capa)
        Box(-8, -30, 7.6, 16, 14, 1, "espaldar"),
    ]),
    # (las raices que salen del corazon van mas abajo, con cadena())
    # capa (dos trozos para que ondee)
    Part("capa", "torso", (0, -31.6, 8.7), rot=(4, 0, 0), boxes=[
        Box(-13.5, 0, -0.5, 27, 22, 1, "capa"),
        Box(-14.5, -1.6, -2.2, 29, 3.5, 3.5, "capa_cuello"),
    ]),
    Part("capa2", "capa", (0, 22, 0), rot=(3, 0, 0), boxes=[Box(-14, 0, -0.5, 28, 27, 1, "capa_bajo")]),
]
# raices que salen del corazon del arbol y abrazan el tronco (pegadas al peto)
PARTS += cadena("raiz_pa", "torso", (4.0, -21.6, -8.75), (0, 0, 30), [(6, None), (5, (0, 0, 16)), (3.5, (0, 0, -8))],
                grosor=2.0, mat0="raiz_corazon")
PARTS += cadena("raiz_pb", "torso", (5.8, -24.8, -8.75), (0, 0, -148), [(5, None), (4.5, (0, 0, -14)), (4, (0, 0, -22))],
                grosor=1.5, mat0="raiz_corazon")
PARTS += cadena("raiz_pc", "torso", (6.6, -22.2, -8.75), (0, 0, -92), [(4, None), (3.5, (0, 0, 12)), (3, (0, 0, 20))],
                grosor=1.5, mat0="raiz_corazon")
PARTS += [
    # ------------------------------------------------------------------ cabeza: yelmo con corona de espinas y astas de rama
    Part("cabeza", "torso", (0, -34.5, -0.6), boxes=[
        Box(-6, -15.5, -6.5, 12, 15.5, 13, "yelmo"),
        Box(-6.5, -9.8, -7.3, 13, 1.5, 1, "visera"),
        Box(-1, -15, -7.3, 2, 5.5, 1, "cresta"),
        Box(-1, -8.2, -7.2, 2, 7.5, 1, "nasal"),
        Box(-0.75, -17, -5.5, 1.5, 2, 11, "cimera"),
    ]),
    Part("espinas", "cabeza", (0, -12.6, 0), boxes=[
        Box(-6.75, -1.25, -7.25, 13.5, 2.5, 14.5, "corona_raiz"),
        Box(-4.5, -4.2, -7.8, 1, 3, 1, "espina"),
        Box(3.5, -4.6, -7.8, 1, 3.5, 1, "espina"),
        Box(-0.5, -3.4, -7.9, 1, 2.5, 1, "espina"),
        Box(-7.3, -3.8, -3, 1, 3, 1, "espina"),
        Box(6.3, -4.0, 1.5, 1, 3, 1, "espina"),
        Box(-7.3, -3.4, 3.4, 1, 2.5, 1, "espina"),
        Box(6.3, -3.2, -4.4, 1, 2, 1, "espina"),
        Box(-2.4, -3.8, 6.7, 1, 3, 1, "espina"),
        Box(2.8, -3.0, 6.7, 1, 2, 1, "espina"),
    ]),
]

# astas: ramas que crecen del yelmo, con hojas en las puntas
for s, n in ((-1, "d"), (1, "i")):
    PARTS += [
        Part(f"rama_{n}", "cabeza", (M(s, 4.6), -13.5, 1.2), rot=(-14, 0, M(s, 34)), boxes=[
            Box(-1.5, -9, -1.5, 3, 9.5, 3, "rama"),
        ]),
        Part(f"rama_{n}2", f"rama_{n}", (0, -8.6, 0), rot=(-10, 0, M(s, -38)), boxes=[
            Box(-1, -8, -1, 2, 8.5, 2, "rama"),
        ]),
        Part(f"rama_{n}3", f"rama_{n}", (0, -4.5, 0), rot=(-48, 0, M(s, 30)), boxes=[
            Box(-0.75, -6, -0.75, 1.5, 6.5, 1.5, "rama"),
        ]),
        Part(f"rama_{n}4", f"rama_{n}2", (0, -7.6, 0), rot=(18, 0, M(s, 26)), boxes=[
            Box(-0.5, -4.5, -0.5, 1, 5, 1, "rama"),
        ]),
        Part(f"hoja_{n}1", f"rama_{n}4", (0, -4.2, 0), rot=(20, 30, 0), boxes=[Box(-2.5, 0, -2.5, 5, 0, 5, "hoja_arbol")]),
        Part(f"hoja_{n}2", f"rama_{n}3", (0, -5.6, 0), rot=(-30, -20, 10), boxes=[Box(-2, 0, -2, 4, 0, 4, "hoja_arbol")]),
        Part(f"hoja_{n}3", f"rama_{n}2", (0, -3.5, 0), rot=(60, 45, 0), boxes=[Box(-1.75, 0, -1.75, 3.5, 0, 3.5, "hoja_arbol")]),
    ]

# ------------------------------------------------------------------ brazos
for s, n in ((-1, "d"), (1, "i")):
    corteza = s > 0   # la hombrera izquierda esta cubierta de corteza y musgo
    PARTS += [
        Part(f"brazo_{n}", "torso", (M(s, 15.5), -27.5, 0), boxes=[
            caja_lado(s, -4, -2, -4.5, 8, 17, 9, "brazal"),
        ]),
        # la hombrera cuelga del torso (en el hombro) y en las animaciones sigue al brazo solo en parte
        Part(f"hombrera_{n}", "torso", (M(s, 15.5), -27.5, 0), boxes=[
            caja_lado(s, -5.4, -4.4, -9, 14, 7, 18, "hombrera_corteza" if corteza else "hombrera"),
            caja_lado(s, -4.0, -6.9, -7.5, 11.5, 2.5, 15, "hombrera_tapa_corteza" if corteza else "hombrera_tapa"),
            caja_lado(s, -3.6, 2.4, -8.5, 12.5, 4, 17, "hombrera_lama"),
            caja_lado(s, -2.8, 6.0, -8.25, 11, 3.5, 16.5, "hombrera_lama2"),
            caja_lado(s, -6.2, -10.2, -7, 2, 4.5, 14, "gola"),
        ]),
        Part(f"antebrazo_{n}", f"brazo_{n}", (0, 15, 0), boxes=[
            caja_lado(s, -3.75, 0, -4, 7.5, 13.5, 8, "avambrazo"),
            caja_lado(s, -4.25, -2.8, 1.4, 8.5, 5.5, 4.5, "codera"),
            caja_lado(s, 3.2, -2.4, -2.5, 2.5, 5, 5, "codera_aleta"),
            caja_lado(s, -5, 10, -5, 10, 3.5, 10, "guantelete_puno"),
            caja_lado(s, -4.25, 13, -4.25, 8.5, 7, 8.5, "guantelete"),
            caja_lado(s, -4.5, 14.2, -4.9, 9, 2, 1.5, "nudillos"),
        ]),
    ]
    if corteza:
        PARTS += [
            # musgo y corteza encima de la hombrera, y una raiz que baja por detras del brazo
            Part("musgo_hombro", f"hombrera_{n}", (0, 0, 0), boxes=[
                caja_lado(s, -4.6, -8.2, -7.25, 12, 2.5, 14.5, "musgo"),
            ]),
        ]
        PARTS += cadena("raiz_hombro", f"hombrera_{n}", (M(s, 6.5), 2, 6.5), (18, 0, M(s, -10)),
                        [(6, None), (5, (8, 0, M(s, 12))), (5, (-6, 0, M(s, -14))), (3.5, (10, 0, M(s, 10)))], grosor=2.0)
    else:
        # el brazo derecho: raices enroscadas en el avambrazo
        for k, (yy, ang) in enumerate(((2.5, 16), (6.8, -13))):
            PARTS.append(Part(f"raiz_brazo{k}", f"antebrazo_{n}", (0, yy, 0), rot=(ang * 0.5, 0, ang), boxes=[
                Box(-4.5, -0.75, -4.75, 9, 1.5, 9.5, "raiz_banda"),
            ]))

# ------------------------------------------------------------------ espinas de la fase 2 (solo se ven cuando el arbol le ha cambiado)
# piezas "espina2_*": el modelo las esconde en la fase 1 (GavranGeometria.ESPINAS2)
for s, n in ((-1, "d"), (1, "i")):
    # tres en la hombrera, hacia arriba y hacia fuera
    for k, (x, z, rx, rz, largo) in enumerate(((3.0, -4.0, -18, 28, 6.5), (6.0, 1.5, 10, 40, 5.5), (0.5, 5.0, 30, 18, 5.0))):
        PARTS.append(Part(f"espina2_h{n}{k}", f"hombrera_{n}", (M(s, x), -6.4, z), rot=(rx, 0, M(s, rz)), boxes=[
            Box(-1.0, -largo, -1.0, 2.0, largo, 2.0, "espina2"),
        ]))
    # dos en el avambrazo, hacia fuera
    for k, (yy, rx, largo) in enumerate(((3.5, -20, 4.0), (8.5, 15, 3.5))):
        PARTS.append(Part(f"espina2_a{n}{k}", f"antebrazo_{n}", (M(s, 3.7), yy, 0), rot=(rx, 0, M(s, 72)), boxes=[
            Box(-0.75, -largo, -0.75, 1.5, largo, 1.5, "espina2"),
        ]))
    # una en la rodilla, hacia delante
    PARTS.append(Part(f"espina2_r{n}", f"pierna_{n}", (M(s, 1.5), -1.5, -6.2), rot=(75, 0, M(s, 12)), boxes=[
        Box(-0.75, -4.0, -0.75, 1.5, 4.0, 1.5, "espina2"),
    ]))
# cuatro por la espalda, saliendo entre las placas (por encima de la capa)
for k, (x, y, rx, rz, largo) in enumerate(((-4.5, -29.5, -62, 12, 6.0), (4.5, -28.5, -58, -14, 6.5), (-2.0, -23.0, -70, 6, 5.0),
                                         (2.5, -19.0, -74, -8, 4.5))):
    PARTS.append(Part(f"espina2_e{k}", "torso", (x, y, 9.5), rot=(rx, 0, rz), boxes=[
        Box(-1.0, -largo, -1.0, 2.0, largo, 2.0, "espina2"),
    ]))

# ------------------------------------------------------------------ el mandoble (en la mano derecha; la izquierda lo agarra mas abajo)
PARTS += [
    Part("espada", "antebrazo_d", (0, 16.6, -0.4), rot=(62, 0, 0), boxes=[
        Box(-1.25, -1.25, -4.5, 2.5, 2.5, 19, "empunadura"),
        Box(-2.5, -2.5, 14.2, 5, 5, 5, "pomo"),
        Box(-2, -2, -9.4, 4, 4, 5, "guarda_centro"),
        Box(-1.5, -12.5, -8.0, 3, 25, 3, "gavilan"),
        Box(-1, -4, -56.4, 2, 8, 47.5, "hoja"),
        Box(-0.75, -2.75, -60.4, 1.5, 5.5, 4, "punta"),
        Box(-0.5, -1.5, -63.3, 1, 3, 3, "punta2"),
        Box(-1.75, -4.75, -14.5, 3.5, 9.5, 2, "raiz_hoja"),
        Box(-1.5, -4.5, -19.2, 3, 9, 1.5, "raiz_hoja"),
    ]),
    # puntas del gavilan: raices retorcidas que se curvan hacia la hoja
    Part("gavilan_a", "espada", (0, -12.2, -6.6), rot=(-28, 0, 0), boxes=[Box(-1.25, -5, -1.25, 2.5, 5.5, 2.5, "raiz")]),
    Part("gavilan_b", "espada", (0, 12.2, -6.6), rot=(28, 0, 0), boxes=[Box(-1.25, -0.4, -1.25, 2.5, 5.5, 2.5, "raiz")]),
]

BY_NAME = {p.name: p for p in PARTS}

# tamano de la textura en unidades de UV (el PNG va a doble resolucion)
TEX_W, TEX_H = 512, 512
SCALE = 2


def pack():
    """Coloca la zona de cada caja en la textura (estanterias), compartiendo las que tienen clave uv.
    Devuelve el alto usado. Las medidas con decimales ocupan su parte entera hacia arriba (como Minecraft)."""
    import math
    shared = {}
    items = []
    for p in PARTS:
        for b in p.boxes:
            if b.uv and b.uv in shared:
                continue
            items.append(b)
            if b.uv:
                shared[b.uv] = b
    items.sort(key=lambda b: -(math.ceil(b.d) + math.ceil(b.h)))
    x = y = 0
    row_h = 0
    for b in items:
        bw = 2 * (math.ceil(b.d) + math.ceil(b.w))
        bh = math.ceil(b.d) + math.ceil(b.h)
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
