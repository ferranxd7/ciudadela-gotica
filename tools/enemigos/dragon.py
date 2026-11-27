"""
Vorthak, el Dragon de Ceniza (parte 7z; respuesta 132 del plan: "especiales: dragon..."): el dragon viejo que guardaba los
pasos de las Montanas Nevadas para los reyes de Morvain. Cuando el cielo se volvio rojo se volvio loco: quemo la Atalaya del
Cuervo y a su guarnicion, se hizo un nido en una repisa de la montana con el oro de los que subian a matarlo y desde entonces
no deja que nadie suba.

Un dragon de cuatro patas y dos alas (unos 21 bloques de largo y otros tantos de alas abiertas, con la escala del juego):
- escamas de carbon con brillo de ceniza, la tripa en placas de color hueso sucio, grietas de brasa por los costados, el lomo,
  el cuello y la cola (en la capa de brillo), una fila de puas de hueso por el espinazo;
- la cabeza larga, con cejas de hueso, dos cuernos que van hacia atras (y dos mas pequenos en las mejillas), los ojos de oro
  fundido, los ollares y la boca con brasas (la garganta se ve al abrir la mandibula);
- las alas de murcielago en tres trozos (brazo, antebrazo y dedos, con su membrana cada uno; la de la punta, festoneada y con
  algun desgarron), la garra del pulgar en la muneca;
- la cola en seis trozos que adelgazan, con la punta en hoja (sus cantos son brasas).

En reposo (la pieza tal cual) tiene las alas abiertas; en tierra se pliegan (la muneca arriba, junto al hombro, y los dedos hacia
la cola). Las alas se ponen con `orientar` (hacia donde va cada hueso y hacia donde cae su membrana; los angulos los saca el).
Las patas, el cuello y la cola, con giros. Las piezas de las patas se llaman como las de una persona (brazo / antebrazo / mano
las de delante, muslo / pierna / pata las de atras).
"""
import math

import numpy as np

from anim import Anim, con, espejo, raiz
from modelo import Box, Rig, g
from pinta import (claro, delante, liso, mezcla, oscuro, ruido, sombra)

NOMBRE = "dragon"
VARIANTES = ("ceniza",)
ESCALA = 1.75
CENTRO_Y = 2.6
#: para las hojas de revision (generar.py): de lejos (es muy grande)
ZOOM_VARIANTES = 15
ZOOM_TIRA = 11
LADO_TIRA = 320

OCULTAS = {0: []}


def reposo(v, por_nombre):
    """Para la hoja de las variantes: de pie, con las alas plegadas."""
    return por_nombre["quieto"].muestra(0.0)


# ---------------------------------------------------------------- el modelo
# (pixeles; Y hacia abajo; el suelo en y = 24; el hocico mira a -Z; la derecha del dragon es -X)

def _espejar(boxes):
    """Las mismas cajas del otro lado (x cambiado de signo)."""
    return [Box(-(b.x + b.w), b.y, b.z, b.w, b.h, b.d, b.mat, b.grow, b.uv) for b in boxes]


def _puas(z0, z1, cada, y, alto, mat="puas", ancho=2.0):
    """Una fila de puas por el espinazo (de z0 a z1, una cada `cada`), con la base en y."""
    out = []
    z = z0
    k = 0
    while z <= z1 + 1e-6:
        h = alto * (0.75 if k % 2 else 1.0)
        out.append(Box(-ancho / 2, y - h, z, ancho, h, 2.4, mat, uv=f"{mat}_{round(h * 10)}"))
        z += cada
        k += 1
    return out


#: las piezas de las alas (de la raiz a la punta) y lo largo de cada hueso
ALA = ("ala", "ala2", "ala3")
LARGO_ALA = (28.0, 32.0, 24.0)
FONDO_ALA = (30.0, 32.0, 30.0)


def rig():
    r = Rig(NOMBRE, 256)
    r.pieza("raiz", "", (0, 24, 0))
    # ================================================================ el cuerpo
    # la cadera (detras, a 28 del suelo): de ella salen el lomo hacia delante, las patas de atras y la cola
    r.pieza("cadera", "raiz", (0, -28, 14), boxes=[
        Box(-10, -8, -7, 20, 17, 21, "cadera"),
    ] + _puas(-5, 11, 4.6, -8, 4.4))
    # el lomo: el pecho y la tripa (de la cadera hacia delante) con las puas del espinazo
    r.pieza("torso", "cadera", (0, -1, -6), boxes=[
        Box(-12, -9, -33, 24, 21, 34, "torso"),
    ] + _puas(-31, -3, 4.4, -9, 5.2))
    # el cuello, en tres trozos (sube hacia delante y se dobla hacia la cabeza)
    r.pieza("cuello1", "torso", (0, -3.5, -31), rot=(-38, 0, 0), boxes=[
        Box(-6.2, -6.2, -13, 12.4, 12.4, 14, "cuello1"),
    ] + _puas(-11, -2, 4.5, -6.2, 3.8))
    r.pieza("cuello2", "cuello1", (0, 0, -12), rot=(-14, 0, 0), boxes=[
        Box(-5.2, -5.2, -12, 10.4, 10.4, 13, "cuello2"),
    ] + _puas(-10, -1, 4.5, -5.2, 3.4))
    r.pieza("cuello3", "cuello2", (0, 0, -11), rot=(24, 0, 0), boxes=[
        Box(-4.5, -4.5, -11, 9, 9, 12, "cuello3"),
    ] + _puas(-9, -1, 4.0, -4.5, 3.0))
    # la cabeza: el craneo, el hocico largo, las cejas de hueso y las mandibulas
    r.pieza("cabeza", "cuello3", (0, 0, -10), rot=(28, 0, 0), boxes=[
        Box(-6.5, -6, -13, 13, 11, 14, "craneo"),
        Box(-5, -4.4, -27, 10, 7.6, 15, "hocico"),
        Box(-7.3, -7.6, -13.5, 4.6, 2.6, 7.5, "cejas", uv="cejas"),
        Box(2.7, -7.6, -13.5, 4.6, 2.6, 7.5, "cejas", uv="cejas"),
        Box(-1.0, -6.8, -26.6, 2.0, 1.6, 10, "cresta_hocico"),
    ])
    r.pieza("mandibula", "cabeza", (0, 3.2, -11.5), rot=(4, 0, 0), boxes=[
        Box(-4.6, 0, -15.4, 9.2, 3.4, 16.4, "mandibula"),
        Box(-5.2, 1.2, -2.0, 10.4, 2.8, 3.8, "mandibula_atras"),
    ])
    # los cuernos: los grandes van hacia atras y arriba (en dos trozos), los de las mejillas hacia atras y abajo
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"cuerno_{n}", "cabeza", (s * 4.6, -5.0, -2.5), rot=(24, s * 16, 0), boxes=[
            Box(-1.7, -1.7, 0, 3.4, 3.4, 13, "cuerno", uv="cuerno")])
        r.pieza(f"cuerno2_{n}", f"cuerno_{n}", (0, 0, 12.4), rot=(16, s * 6, 0), boxes=[
            Box(-1.1, -1.1, 0, 2.2, 2.2, 11, "cuerno2", uv="cuerno2")])
        r.pieza(f"cuerno_bajo_{n}", "cabeza", (s * 6.5, 1.4, -3.0), rot=(-8, s * 32, 0), boxes=[
            Box(-1.0, -1.0, 0, 2.0, 2.0, 8.5, "cuerno_bajo", uv="cuerno_bajo")])
    # ================================================================ las patas
    # las de delante: del hombro al codo, la mano y las garras
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"brazo_{n}", "torso", (s * 11.0, 3, -26), boxes=[Box(-4.0, -3.5, -4.6, 8.0, 16.5, 9.2, "brazo", uv="brazo")])
        r.pieza(f"antebrazo_{n}", f"brazo_{n}", (0, 12, 0), boxes=[Box(-3.2, 0, -3.2, 6.4, 12, 6.4, "antebrazo", uv="antebrazo")])
        r.pieza(f"mano_{n}", f"antebrazo_{n}", (0, 11, 0), boxes=[
            Box(-4.4, 0, -7.6, 8.8, 4, 10, "mano", uv="mano"),
            Box(-4.2, 2.2, -10.0, 1.7, 1.8, 2.8, "garra", uv="garra"),
            Box(-0.85, 2.2, -10.4, 1.7, 1.8, 3.2, "garra2", uv="garra2"),
            Box(2.5, 2.2, -10.0, 1.7, 1.8, 2.8, "garra", uv="garra"),
        ])
    # las de atras: el muslo hacia delante, la pierna hacia atras, el tarso y el pie con sus garras
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"muslo_{n}", "cadera", (s * 10.8, 1, 4), rot=(-30, 0, 0), boxes=[Box(-4.8, -4.5, -5.6, 9.6, 16.5, 11.2, "muslo", uv="muslo")])
        r.pieza(f"pierna_{n}", f"muslo_{n}", (0, 11, 0), rot=(62, 0, 0), boxes=[Box(-3.2, 0, -3.2, 6.4, 11, 6.4, "pierna", uv="pierna")])
        r.pieza(f"pata_{n}", f"pierna_{n}", (0, 10, 0), rot=(-32, 0, 0), boxes=[
            Box(-2.6, 0, -2.6, 5.2, 7.6, 5.2, "tarso", uv="tarso"),
            Box(-4.4, 6.6, -9.2, 8.8, 3, 11.5, "pie", uv="pie"),
            Box(-4.2, 7.8, -11.6, 1.7, 1.8, 2.8, "garra", uv="garra"),
            Box(-0.85, 7.8, -12.0, 1.7, 1.8, 3.2, "garra2", uv="garra2"),
            Box(2.5, 7.8, -11.6, 1.7, 1.8, 2.8, "garra", uv="garra"),
        ])
    # ================================================================ la cola (seis trozos y la punta en hoja)
    largos = (16, 15, 14, 13, 12, 12)
    gruesos = (12.6, 10.4, 8.6, 6.8, 5.2, 3.8)
    caidas = (-14, -8, -4, 5, 8, 7)
    padre, z = "cadera", 13.0
    for i in range(6):
        gr = gruesos[i]
        boxes = [Box(-gr / 2, -gr / 2, -1, gr, gr, largos[i] + 1, f"cola{i + 1}")]
        if i < 5:
            boxes += _puas(1.5, largos[i] - 3, 4.6, -gr / 2, 3.4 - i * 0.45, mat="puas_cola")
        r.pieza(f"cola{i + 1}", padre, (0, -3 if i == 0 else 0, z), rot=(caidas[i], 0, 0), boxes=boxes)
        padre, z = f"cola{i + 1}", largos[i]
    r.pieza("punta", "cola6", (0, 0, 12), boxes=[Box(-4.8, -0.7, -0.5, 9.6, 1.4, 11, "punta")])
    # ================================================================ las alas (abiertas en reposo)
    # cada trozo: el hueso a lo largo de -X (la derecha) y su membrana hacia atras (+Z), fina. La izquierda lleva sus propias
    # membranas (pintadas en espejo: en la textura, la x de una caja va siempre de la derecha del modelo a su izquierda)
    for s, n in ((-1, "d"), (1, "i")):
        hueso = [
            [Box(-LARGO_ALA[0], -2.4, -2.4, LARGO_ALA[0] + 1, 4.8, 4.8, "ala_hueso", uv="ala_hueso"),
             Box(-LARGO_ALA[0], -0.3, 2.0, LARGO_ALA[0], 0.6, FONDO_ALA[0], f"membrana1_{n}")],
            [Box(-LARGO_ALA[1], -1.7, -1.7, LARGO_ALA[1] + 0.5, 3.4, 3.4, "ala_hueso2", uv="ala_hueso2"),
             Box(-LARGO_ALA[1], -0.3, 1.3, LARGO_ALA[1], 0.6, FONDO_ALA[1], f"membrana2_{n}"),
             Box(-LARGO_ALA[1] - 1.0, -4.8, -1.5, 2.6, 3.6, 3.0, "garra_ala", uv="garra_ala")],
            [Box(-LARGO_ALA[2], -1.1, -1.1, LARGO_ALA[2] + 0.5, 2.2, 2.2, "ala_hueso3", uv="ala_hueso3"),
             Box(-LARGO_ALA[2], -0.3, 0.7, LARGO_ALA[2], 0.6, FONDO_ALA[2], f"membrana3_{n}")],
        ]
        if s > 0:
            hueso = [_espejar(h) for h in hueso]
        r.pieza(f"ala_{n}", "torso", (s * 9.0, -8.4, -21), boxes=hueso[0])
        r.pieza(f"ala2_{n}", f"ala_{n}", (s * LARGO_ALA[0], 0, 0), boxes=hueso[1])
        r.pieza(f"ala3_{n}", f"ala2_{n}", (s * LARGO_ALA[1], 0, 0), boxes=hueso[2])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas

ESCAMA = ["#0b0a0a", "#131111", "#1b1817", "#24201e", "#2e2926", "#3a3430", "#48413b"]
CENIZA = ["#4e4944", "#67615a", "#827a71", "#9d948a", "#b6ada2"]
VIENTRE = ["#2f2924", "#42382f", "#584a3d", "#6e5d4c", "#86725e"]
HUESO = ["#3a332b", "#5c5345", "#82765f", "#a89b80", "#c9bea3", "#e2d9c2"]
MEMBRANA = ["#0d0908", "#150e0c", "#1e1411", "#291b16", "#35231b", "#432c22"]
BRASA = ["#4a0e02", "#8a2204", "#d24c0c", "#ff8a26", "#ffc35a", "#fff0b0"]
BOCA = ["#1c0403", "#360806", "#550e09", "#78160d"]
DIENTE = ["#a89c84", "#d8cfba", "#efe8d4"]
OJO = ["#7a3a06", "#ff9a20", "#ffd24a", "#fff4b8"]
HUECO = "#050404"

KC = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def borde_ala(tipo, s):
    """Hasta donde llega la membrana (0 = el hueso, 1 = el fondo de la caja) a lo largo del trozo (s: 0 fuera, 1 la raiz)."""
    if tipo == 3:
        # la punta: el borde de atras baja de la muneca a la punta, con festones entre los dedos
        return 0.10 + 0.90 * s ** 1.1 - 0.10 * abs(math.sin(s * math.pi * 3.0)) * s
    if tipo == 2:
        # el antebrazo: festones entre los dedos (que acaban en el borde), mas hondo hacia el codo
        return 0.80 + 0.20 * s - 0.12 * abs(math.sin(s * math.pi * 2.0))
    # junto al cuerpo: casi entera, con una curva suave
    return 0.97 - 0.07 * abs(math.sin(s * math.pi * 1.0 + 0.4))


def dedos_ala(tipo):
    """Donde acaba cada dedo en el borde de atras (s) y de donde sale (la muneca: el extremo de fuera del trozo del
    antebrazo, s = 0, o la raiz del de los dedos, s = 1)."""
    if tipo == 3:
        return [(1.0, k) for k in (0.22, 0.50, 0.78)]
    if tipo == 2:
        return [(0.0, k) for k in (0.16, 0.42)]
    return []


def dedo_ala(tipo, dedo, q):
    """Por donde pasa (s) el dedo a la altura q (del hueso, 0, al borde): en linea de la muneca a su punta en el borde."""
    s0, sk = dedo
    bk = borde_ala(tipo, sk)
    if q > bk:
        return None
    return s0 + (sk - s0) * (q / bk)


def _grietas(semilla, n=7, largo=9):
    """Grietas de brasa: caminos al azar sobre una rejilla de 64 x 64 (se repite) que bajan haciendo zigzag."""
    rng = np.random.default_rng(semilla)
    m = np.zeros((64, 64), dtype=np.int8)
    for _ in range(n):
        x, y = int(rng.integers(0, 64)), int(rng.integers(0, 64))
        for k in range(largo + int(rng.integers(0, largo))):
            m[y % 64, x % 64] = 2 if k % 4 == 0 else 1
            d = int(rng.integers(0, 4))
            if d == 0:
                x += 1
            elif d == 1:
                x -= 1
            else:
                y += 1
    return m


def _escamas(tonos, semilla, fila=2, ancho=3, grano=0.4):
    """Escamas: filas de `fila` pixeles con la junta oscura, cada fila corrida media escama; grano encima."""
    base = ruido(tonos[1:], semilla, grano=grano, base=0.45)

    def f(cara, x, y, w, h):
        c = base(cara, x, y, w, h)
        a, b = (x, y)
        fila_n = b // fila
        if b % fila == fila - 1:
            return oscuro(c, 0.38)
        if (a + (fila_n % 2) * (ancho // 2 + 1)) % ancho == 0:
            return oscuro(c, 0.22)
        if b % fila == 0:
            return claro(c, 0.06)
        return c

    return f


def materiales(variante):
    escama = _escamas(ESCAMA, 901)
    escama_b = _escamas(ESCAMA[:6], 902, fila=2, ancho=2)
    ceniza = ruido(CENIZA, 903, grano=0.45, base=0.45)
    vientre = ruido(VIENTRE, 904, grano=0.35, base=0.5)
    hueso = ruido(HUESO[1:], 905, grano=0.35, base=0.55)
    memb = ruido(MEMBRANA, 906, grano=0.5, base=0.5)
    grieta = _grietas(907, n=9, largo=10)
    roto = np.random.default_rng(908).random((64, 64))

    def cuerpo(cara, x, y, w, h, tripa=2):
        """Escamas de carbon por arriba y los costados, la tripa en placas abajo; las grietas, quemadas."""
        if cara == "abajo":
            c = vientre(cara, x, y, w, h)
            if y % 3 == 2:
                return oscuro(c, 0.4)                       # las juntas de las placas de la tripa
            if x in (0, w - 1):
                return oscuro(c, 0.25)
            return claro(c, 0.05) if y % 3 == 0 else c
        if cara in ("der", "izq") and y >= h - tripa:
            c = vientre(cara, x, y, w, h)
            return oscuro(c, 0.35) if x % 3 == 2 else c
        c = escama(cara, x, y, w, h)
        if cara == "arriba" and abs(x - (w - 1) / 2) < 1:
            return mezcla(c, CENIZA[1], 0.35)              # el espinazo, mas claro
        if cara in ("der", "izq", "arriba") and grieta[(y + 9 * KC[cara]) % 64, (x + 17 * KC[cara]) % 64]:
            return ESCAMA[0]                                # la grieta (por la capa de brillo, la brasa)
        if cara in ("der", "izq") and y <= 1:
            return mezcla(c, CENIZA[0], 0.25)               # la ceniza que se le posa en el lomo
        return c

    def cadera(cara, x, y, w, h):
        return cuerpo(cara, x, y, w, h, 3)

    def torso(cara, x, y, w, h):
        return cuerpo(cara, x, y, w, h, 4)

    def cuello(cara, x, y, w, h):
        c = cuerpo(cara, x, y, w, h, 2)
        if cara == "abajo":
            return c
        return c

    def cola(cara, x, y, w, h):
        return cuerpo(cara, x, y, w, h, 1)

    def puas(cara, x, y, w, h):
        # hueso con la punta negra
        c = hueso(cara, x, y, w, h)
        if y == 0:
            return HUESO[0]
        if y == 1:
            return oscuro(c, 0.35)
        return c

    def craneo(cara, x, y, w, h):
        c = escama_b(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 1 <= d <= 3 and y in (2, 3):
                return HUECO                                # el ojo (le brilla)
            if d <= 4 and y == 1:
                return oscuro(c, 0.4)                       # la sombra de la ceja
            if y >= h - 2:
                return mezcla(c, VIENTRE[2], 0.5)
        if cara == "frente":
            return oscuro(c, 0.15)
        if cara == "abajo":
            return vientre(cara, x, y, w, h)
        return c

    def hocico(cara, x, y, w, h):
        c = escama_b(cara, x, y, w, h)
        if cara == "frente":
            if y in (1, 2) and x in (1, w - 2):
                return HUECO                                # los ollares
            if y == h - 1:
                return DIENTE[1] if x % 2 == 0 else BOCA[0]
            return c
        if cara in ("der", "izq"):
            if y == h - 1:
                return DIENTE[2] if x % 2 == 0 else BOCA[0]   # los dientes de arriba
            if y == h - 2 and x % 4 == 1:
                return DIENTE[0]
            if y >= h - 3:
                return mezcla(c, VIENTRE[1], 0.4)
        if cara == "abajo":
            # el paladar: la boca de dentro (con brasas, por la capa de brillo)
            if x in (0, w - 1):
                return DIENTE[1] if y % 2 == 0 else BOCA[1]
            return BOCA[1] if (x + y) % 3 else BOCA[2]
        return c

    def cejas(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara == "abajo":
            return HUECO
        if cara == "frente" and (x + y) % 3 == 0:
            return oscuro(c, 0.3)
        return oscuro(c, 0.15) if y == h - 1 else c

    def cresta_hocico(cara, x, y, w, h):
        return puas(cara, x, y, w, h)

    def mandibula(cara, x, y, w, h):
        c = escama_b(cara, x, y, w, h)
        if cara == "arriba":
            # la lengua y los dientes de abajo
            if x in (0, w - 1):
                return DIENTE[1] if y % 2 == 0 else BOCA[0]
            if abs(x - (w - 1) / 2) <= 1.5:
                return BOCA[3] if y % 2 else BOCA[2]
            return BOCA[1]
        if cara in ("der", "izq") and y == 0:
            return DIENTE[2] if x % 2 == 1 else BOCA[0]
        if cara == "abajo":
            return vientre(cara, x, y, w, h)
        return c

    def mandibula_atras(cara, x, y, w, h):
        return escama_b(cara, x, y, w, h)

    def cuerno(cara, x, y, w, h):
        # hueso claro que se oscurece hacia la punta (la cara de atras de la caja), con anillos
        c = hueso(cara, x, y, w, h)
        if cara == "espalda":
            return HUESO[1]
        if cara in ("der", "izq", "arriba", "abajo"):
            d = (w - 1 - x) if cara == "der" else x if cara == "izq" else (h - 1 - y)
            if (d + 1) % 3 == 0:
                return oscuro(c, 0.25)                      # los anillos del cuerno
        return c

    def cuerno2(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara == "espalda":
            return HUESO[0]
        if cara in ("der", "izq", "arriba", "abajo"):
            d = (w - 1 - x) if cara == "der" else x if cara == "izq" else (h - 1 - y)
            return mezcla(c, HUESO[0], min(1.0, d / 9.0))   # negra en la punta
        return c

    def brazo(cara, x, y, w, h):
        c = escama(cara, x, y, w, h)
        if cara == "frente" and y >= 3:
            return mezcla(c, VIENTRE[2], 0.4)
        return c

    def mano(cara, x, y, w, h):
        c = escama_b(cara, x, y, w, h)
        if cara == "abajo":
            return VIENTRE[0]
        return c

    def garra(cara, x, y, w, h):
        return HUESO[4] if cara in ("arriba", "frente") else HUESO[2]

    def tarso(cara, x, y, w, h):
        return escama_b(cara, x, y, w, h)

    def ala_hueso(cara, x, y, w, h):
        c = escama_b(cara, x, y, w, h)
        if cara == "abajo":
            return mezcla(c, MEMBRANA[4], 0.4)
        return c

    def membrana(tipo, izquierda=False):
        """La membrana de un trozo del ala (caras de arriba y abajo: x a lo largo del ala, y del borde de atras (0) al hueso
        (h - 1)). tipo 1: la de junto al cuerpo; 2: la de en medio; 3: la de la punta, festoneada. La izquierda, en espejo."""

        def f(cara, x, y, w, h):
            if cara not in ("arriba", "abajo"):
                return None                                 # una lamina: sin cantos
            # s: del extremo de fuera del trozo (0) a su raiz (1); q: del hueso (0) al borde de atras (1)
            s = (w - 1 - x if izquierda else x) / max(1, w - 1)
            q = 1.0 - y / max(1, h - 1)
            if q > borde_ala(tipo, s):
                return None
            # algun desgarron
            if roto[(y * 3 + KC[cara]) % 64, (int(s * 63) * 2 + tipo * 11) % 64] > 0.985 and q > 0.25:
                return None
            c = memb(cara, int(s * (w - 1)), y, w, h)
            # los dedos: rayas oscuras que salen de la muneca hacia el borde
            for k in dedos_ala(tipo):
                eje = dedo_ala(tipo, k, q)
                if eje is not None and abs(s - eje) < 1.3 / max(8, w):
                    return mezcla(c, ESCAMA[2], 0.75)
            # mas clara y rojiza hacia el borde (la luz la atraviesa), mas oscura junto al hueso
            c = mezcla(c, "#4a261c", 0.35 * q)
            if cara == "abajo":
                c = oscuro(c, 0.18)
            if q < 0.08:
                return oscuro(c, 0.3)
            return c

        return f

    def garra_ala(cara, x, y, w, h):
        return HUESO[4] if y <= 1 else HUESO[2]

    def punta(cara, x, y, w, h):
        # la hoja de la cola: un rombo (lo de fuera, transparente)
        if cara in ("arriba", "abajo"):
            cx = (w - 1) / 2.0
            d = abs(x - cx) / max(1.0, cx)
            yy = 1.0 - y / max(1, h - 1)               # 0 en la raiz (el frente de la caja esta abajo), 1 en la punta
            ancho = (yy / 0.35) if yy < 0.35 else (1.0 - (yy - 0.35) / 0.65)
            if d > ancho * 0.98:
                return None
            c = escama_b(cara, x, y, w, h)
            if d > ancho * 0.72:
                return BRASA[1]
            if abs(x - cx) < 0.6:
                return HUESO[2]
            return c
        if cara in ("der", "izq", "frente", "espalda"):
            return None
        return None

    return {
        "cadera": sombra(cadera, abajo=0.16), "torso": sombra(torso, abajo=0.18),
        "cuello1": sombra(cuello, abajo=0.12), "cuello2": sombra(cuello, abajo=0.12), "cuello3": sombra(cuello, abajo=0.12),
        "puas": puas, "puas_cola": puas,
        "craneo": sombra(craneo, abajo=0.1), "hocico": sombra(hocico, abajo=0.08), "cejas": cejas,
        "cresta_hocico": cresta_hocico, "mandibula": mandibula, "mandibula_atras": sombra(mandibula_atras, abajo=0.1),
        "cuerno": cuerno, "cuerno2": cuerno2, "cuerno_bajo": cuerno2,
        "brazo": sombra(brazo, abajo=0.12), "antebrazo": sombra(brazo, abajo=0.15), "mano": mano, "garra": garra,
        "muslo": sombra(cadera, abajo=0.15), "pierna": sombra(brazo, abajo=0.15), "tarso": tarso, "pie": mano,
        "cola1": sombra(cola, abajo=0.1), "cola2": sombra(cola, abajo=0.1), "cola3": sombra(cola, abajo=0.1),
        "cola4": sombra(cola, abajo=0.1), "cola5": sombra(cola, abajo=0.1), "cola6": sombra(cola, abajo=0.1),
        "punta": punta,
        "ala_hueso": ala_hueso, "ala_hueso2": ala_hueso, "ala_hueso3": ala_hueso,
        "membrana1_d": membrana(1), "membrana2_d": membrana(2), "membrana3_d": membrana(3),
        "membrana1_i": membrana(1, True), "membrana2_i": membrana(2, True), "membrana3_i": membrana(3, True),
        "garra_ala": garra_ala, "garra2": garra,
    }


def brillo(variante):
    """Lo que brilla: los ojos de oro fundido, los ollares, las brasas de la boca, las grietas de brasa del cuerpo, las venas de
    brasa del borde de las alas y el filo de la hoja de la cola."""
    grieta = _grietas(907, n=9, largo=10)
    venas = np.random.default_rng(909).random((64, 64))

    def grietas(cara, x, y, w, h, tripa=2):
        if cara in ("der", "izq") and y >= h - tripa:
            return None
        if cara in ("der", "izq", "arriba"):
            v = grieta[(y + 9 * KC[cara]) % 64, (x + 17 * KC[cara]) % 64]
            if v:
                return BRASA[3] if v == 2 else BRASA[2]
        return None

    def cuerpo_b(tripa):
        return lambda cara, x, y, w, h: grietas(cara, x, y, w, h, tripa)

    def ojos(cara, x, y, w, h):
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if 1 <= d <= 3 and y in (2, 3):
                return OJO[3] if (d == 2 and y == 2) else OJO[2] if y == 2 else OJO[1]
        return None

    def hocico_b(cara, x, y, w, h):
        if cara == "frente" and y in (1, 2) and x in (1, w - 2):
            return BRASA[3] if y == 2 else BRASA[2]
        if cara == "abajo" and 1 <= x <= w - 2 and (x * 3 + y) % 4 == 0:
            return BRASA[1]
        return None

    def mandibula_b(cara, x, y, w, h):
        if cara == "arriba" and 1 <= x <= w - 2 and (x + y * 2) % 5 == 0:
            return BRASA[1]
        return None

    def membrana_b(tipo, izquierda=False):
        def f(cara, x, y, w, h):
            if cara != "abajo" and cara != "arriba":
                return None
            s = (w - 1 - x if izquierda else x) / max(1, w - 1)
            q = 1.0 - y / max(1, h - 1)
            borde = borde_ala(tipo, s)
            if q > borde:
                return None
            # el borde de atras, una brasa apagada; y alguna vena que sube del borde
            if borde - q < 1.2 / max(6, h) and (x + y) % 3 != 0:
                return BRASA[1]
            if q > borde * 0.7 and venas[(int(s * 63) * 5) % 64, (tipo * 7) % 64] > 0.86 and (y % 2 == 0):
                return BRASA[0]
            return None

        return f

    def punta_b(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            cx = (w - 1) / 2.0
            d = abs(x - cx) / max(1.0, cx)
            yy = 1.0 - y / max(1, h - 1)
            ancho = (yy / 0.35) if yy < 0.35 else (1.0 - (yy - 0.35) / 0.65)
            if ancho * 0.72 < d <= ancho * 0.98:
                return BRASA[3] if (x + y) % 2 else BRASA[2]
        return None

    return {
        "cadera": cuerpo_b(3), "torso": cuerpo_b(4), "cuello1": cuerpo_b(2), "cuello2": cuerpo_b(2), "cuello3": cuerpo_b(2),
        "cola1": cuerpo_b(1), "cola2": cuerpo_b(1), "cola3": cuerpo_b(1), "cola4": cuerpo_b(1),
        "craneo": ojos, "hocico": hocico_b, "mandibula": mandibula_b,
        "membrana1_d": membrana_b(1), "membrana2_d": membrana_b(2), "membrana3_d": membrana_b(3),
        "membrana1_i": membrana_b(1, True), "membrana2_i": membrana_b(2, True), "membrana3_i": membrana_b(3, True),
        "punta": punta_b,
    }


# ---------------------------------------------------------------- las alas: angulos a partir de hacia donde va cada hueso

def _norm(v):
    v = np.asarray(v, dtype=float)
    return v / np.linalg.norm(v)


def _euler(m):
    """Los angulos (grados; el juego los pone Z, Y, X) de una matriz de giro."""
    b = math.asin(max(-1.0, min(1.0, -m[2, 0])))
    a = math.atan2(m[2, 1], m[2, 2])
    c = math.atan2(m[1, 0], m[0, 0])
    return (math.degrees(a), math.degrees(b), math.degrees(c))


def _matriz(dir_hueso, dir_fondo, lado):
    """El giro que lleva el hueso (local: -X a la derecha, +X a la izquierda) a dir_hueso y su membrana (local +Z) hacia dir_fondo."""
    ex = _norm(dir_hueso) * (-1.0 if lado == "d" else 1.0)
    f = np.asarray(dir_fondo, dtype=float)
    ez = _norm(f - np.dot(f, ex) * ex)
    ey = np.cross(ez, ex)
    return np.stack([ex, ey, ez], axis=1)


def ala(lado, *tramos):
    """
    La pose de un ala (en el sistema del torso): tramos = [(hueso, fondo)] del brazo, el antebrazo y los dedos, en direcciones
    con x hacia la izquierda del dragon, y hacia abajo y z hacia atras (las del modelo). Para el ala izquierda, las mismas
    direcciones con la x cambiada de signo (lo hace `alas`).
    """
    out = {}
    padre = np.eye(3)
    for k, (dh, df) in enumerate(tramos):
        mundo = _matriz(dh, df, lado)
        local = padre.T @ mundo
        out[f"{ALA[k]}_{lado}"] = tuple(round(v, 1) for v in _euler(local))
        padre = mundo
    return out


def alas(*tramos):
    """Las dos alas en la misma pose (la izquierda, en espejo)."""
    d = ala("d", *tramos)
    i = ala("i", *[((-h[0], h[1], h[2]), (-f[0], f[1], f[2])) for h, f in tramos])
    return {**d, **i}


# las poses de las alas (derecha; x negativa = hacia fuera por la derecha)
PLEGADAS = (((-0.42, -0.34, 0.84), (-0.30, 1.0, 0.10)),    # el brazo, hacia atras y un poco arriba
            ((-0.08, -0.26, -0.96), (-0.25, 1.0, -0.10)),  # el antebrazo, hacia delante: la muneca alta, junto al hombro
            ((-0.14, 0.26, 0.95), (-0.35, 1.0, 0.10)))     # los dedos, hacia la cola
MEDIO = (((-0.55, -0.62, 0.55), (0.0, 0.45, 1.0)),
         ((-0.30, -0.80, -0.50), (-0.2, 0.3, 1.0)),
         ((-0.50, 0.25, 0.83), (-0.3, 0.9, 0.3)))
ABIERTAS = (((-0.97, -0.20, 0.10), (0, 0, 1)),
            ((-0.96, -0.06, 0.26), (0, 0, 1)),
            ((-0.78, 0.06, 0.62), (0.3, 0.05, 1)))
ARRIBA = (((-0.62, -0.78, 0.10), (0, -0.1, 1)),
          ((-0.78, -0.50, 0.38), (0.1, 0.1, 1)),
          ((-0.70, 0.12, 0.70), (0.3, 0.3, 1)))
ABAJO = (((-0.80, 0.58, -0.10), (0, 0.15, 1)),
         ((-0.72, 0.62, 0.30), (0.1, 0.2, 1)),
         ((-0.45, 0.52, 0.72), (0.4, 0.4, 1)))
ALTAS = (((-0.40, -0.90, 0.17), (0, -0.2, 1)),
         ((-0.62, -0.72, 0.30), (0, -0.1, 1)),
         ((-0.75, -0.30, 0.59), (0.2, 0.1, 1)))
PLEGADAS_TODO = alas(*PLEGADAS)


def mezcla_alas(a, b, t):
    """Entre dos poses de alas (por direcciones: el ala no hace cosas raras por el camino)."""
    out = []
    for (ha, fa), (hb, fb) in zip(a, b):
        h = _norm(np.asarray(ha) * (1 - t) + np.asarray(hb) * t)
        f = np.asarray(fa) * (1 - t) + np.asarray(fb) * t
        out.append((tuple(h), tuple(f)))
    return tuple(out)


# ---------------------------------------------------------------- el cuello y la cola: giros repartidos

PESOS_CUELLO = (0.30, 0.28, 0.24, 0.18)


def cuello(x=0.0, y=0.0, z=0.0, pesos=PESOS_CUELLO):
    """El cuello y la cabeza: x positivo = bajar la cabeza (negativo, alzarla), y positivo = hacia su derecha."""
    return {n: (x * w, y * w, z * w) for n, w in zip(("cuello1", "cuello2", "cuello3", "cabeza"), pesos)}


def cola(x=0.0, y=0.0, ola=0.0, fase=0.0, olay=0.0):
    """La cola: x positivo = alzarla, y positivo = hacia su derecha; ola = una onda de lado a lado que corre hacia la punta."""
    out = {}
    for i in range(6):
        w = 0.10 + 0.04 * i
        out[f"cola{i + 1}"] = (x * w + olay * math.sin(fase - i * 0.9) * (0.5 + 0.1 * i),
                              y * w + ola * math.sin(fase - i * 0.9) * (0.5 + 0.12 * i), 0.0)
    return out


def boca(abierta):
    return {"mandibula": (abierta, 0, 0)}


# ---------------------------------------------------------------- las patas

def patas(fd=0.0, fi=0.0, td=0.0, ti=0.0, agacha=0.0):
    """Las patas: fd/fi las de delante (der/izq), td/ti las de atras; + = la pata hacia atras. agacha: dobla todas."""
    def delante_(a):
        return (a - agacha * 0.5, 0, 0), (max(0.0, -a) * 0.7 + agacha * 0.9, 0, 0), (-max(0.0, -a) * 0.4 - agacha * 0.4, 0, 0)

    def atras_(a):
        return (a - agacha * 0.9, 0, 0), (max(0.0, -a) * 0.6 + agacha * 1.3, 0, 0), (-max(0.0, -a) * 0.3 - agacha * 0.5, 0, 0)

    bd, ad, md = delante_(fd)
    bi, ai, mi = delante_(fi)
    sd, pd, ped = atras_(td)
    si, pi_, pei = atras_(ti)
    return {"brazo_d": bd, "antebrazo_d": ad, "mano_d": md, "brazo_i": bi, "antebrazo_i": ai, "mano_i": mi,
            "muslo_d": sd, "pierna_d": pd, "pata_d": ped, "muslo_i": si, "pierna_i": pi_, "pata_i": pei}


# las patas recogidas en vuelo: las de delante pegadas al pecho, las de atras estiradas hacia la cola
PATAS_VUELO = {"brazo_d": (55, 0, -8), "antebrazo_d": (-95, 0, 0), "mano_d": (40, 0, 0),
               "brazo_i": (55, 0, 8), "antebrazo_i": (-95, 0, 0), "mano_i": (40, 0, 0),
               "muslo_d": (70, 0, -6), "pierna_d": (-20, 0, 0), "pata_d": (35, 0, 0),
               "muslo_i": (70, 0, 6), "pierna_i": (-20, 0, 0), "pata_i": (35, 0, 0)}
# las patas colgando, listas para posarse
PATAS_POSARSE = {"brazo_d": (-25, 0, -4), "antebrazo_d": (20, 0, 0), "mano_d": (10, 0, 0),
                 "brazo_i": (-25, 0, 4), "antebrazo_i": (20, 0, 0), "mano_i": (10, 0, 0),
                 "muslo_d": (-10, 0, -4), "pierna_d": (-10, 0, 0), "pata_d": (20, 0, 0),
                 "muslo_i": (-10, 0, 4), "pierna_i": (-10, 0, 0), "pata_i": (20, 0, 0)}

# ---------------------------------------------------------------- las poses de tierra

BASE = con(PLEGADAS_TODO, raiz=raiz(0.0), torso=(0, 0, 0), **cuello(), **cola(), **boca(0))
# en guardia: la cabeza baja y adelantada, la boca entreabierta, las alas medio alzadas y la cola en alto
LISTO = con(BASE, alas(*mezcla_alas(PLEGADAS, MEDIO, 0.35)), raiz=raiz(-1.5), cadera=(-3, 0, 0), torso=(4, 0, 0),
            **cuello(16, 0, 0), **cola(8, 0), **boca(10), **patas(-6, -4, -4, -6, agacha=6))


def _tierra(**k):
    return con(BASE, **k)


def animaciones(r=None):
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses):
        a = Anim(nombre, largo, bucle=True).poses(poses, r)
        B.append(a)
        return a

    def accion(nombre, largo, poses, golpes=()):
        a = Anim(nombre, largo, golpes=golpes).poses(poses, r)
        A.append(a)
        return a

    # ================================================================ bucles de tierra
    # ---- quieto: respira hondo (el pecho sube), mira a un lado y a otro, la cola se mece
    def quieto(t):
        f = 2 * math.pi * t / 8.0
        return con(BASE, torso=(-1.2 * math.sin(2 * f), 0, 0), raiz=raiz(0.4 * math.sin(2 * f)),
                   **cuello(-4 + 5 * math.sin(f), 14 * math.sin(f + 0.6), 3 * math.sin(f)),
                   **cola(2, 6 * math.sin(f), ola=6, fase=2 * f), **boca(2 + 2 * math.sin(2 * f)))
    bucle("quieto", 8.0, [(k * 0.5, quieto(k * 0.5)) for k in range(17)])

    # ---- en guardia: agachado, la cabeza baja que se mece como una serpiente, la cola que azota
    def listo(t):
        f = 2 * math.pi * t / 4.0
        return con(LISTO, torso=(4 - 1.0 * math.sin(2 * f), 0, 0),
                   **cuello(16 + 4 * math.sin(2 * f), 10 * math.sin(f), 4 * math.sin(f)),
                   **cola(8, 0, ola=10, fase=2 * f), **boca(10 + 6 * max(0.0, math.sin(2 * f))))
    bucle("listo", 4.0, [(k * 0.25, listo(k * 0.25)) for k in range(17)])

    # ---- andar: las patas en diagonal; el cuerpo se balancea, el cuello cabecea y la cola ondula (un paso cada 2,4 s)
    T = 2.4

    def andar(t):
        f = 2 * math.pi * t / T
        a = math.sin(f)
        return con(LISTO, raiz=raiz(-1.0 + 0.8 * abs(math.cos(f))), torso=(3, 3 * a, 2 * a), cadera=(-2, -3 * a, -2 * a),
                   **patas(-24 * a, 24 * a, 22 * a, -22 * a, agacha=4),
                   **cuello(12 + 3 * math.cos(2 * f), -6 * a, 0), **cola(6, 8 * a, ola=8, fase=f), **boca(8))
    bucle("andar", T, [(k * T / 8, andar(k * T / 8)) for k in range(9)])

    # ---- dormido: echado sobre la tripa encima de su tesoro, el cuello enroscado a un lado con la cabeza en el suelo, la cola
    # rodeandole por el otro; respira despacio (las alas suben y bajan un poco)
    DORMIDO = con(BASE, raiz=raiz(-17.0), cadera=(2, 0, 0), torso=(0, 0, 0),
                  brazo_d=(-62, 0, -12), antebrazo_d=(8, 0, 0), mano_d=(70, 0, 0),
                  brazo_i=(-62, 0, 12), antebrazo_i=(8, 0, 0), mano_i=(70, 0, 0),
                  muslo_d=(-76, 0, -18), pierna_d=(130, 0, 0), pata_d=(-60, 0, 0),
                  muslo_i=(-76, 0, 18), pierna_i=(130, 0, 0), pata_i=(-60, 0, 0),
                  cuello1=(30, -30, 0), cuello2=(12, -34, 0), cuello3=(-12, -30, 0), cabeza=(-14, -20, 10), mandibula=(0, 0, 0),
                  cola1=(-6, 22, 0), cola2=(-2, 26, 0), cola3=(0, 28, 0), cola4=(2, 26, 0), cola5=(0, 22, 0), cola6=(0, 18, 0))

    def dormido(t):
        f = 2 * math.pi * t / 6.0
        return con(DORMIDO, alas(*mezcla_alas(PLEGADAS, MEDIO, 0.04 + 0.04 * math.sin(f))), torso=(-1.2 * math.sin(f), 0, 0),
                   raiz=raiz(-17.0 + 0.5 * math.sin(f)))
    bucle("dormido", 6.0, [(k * 0.75, dormido(k * 0.75)) for k in range(9)])

    # ================================================================ bucles de vuelo
    # el cuerpo tendido, las patas recogidas, la cola estirada hacia atras; el aleteo: arriba, abajo (con los dedos a la zaga)
    VUELO = con(PATAS_VUELO, raiz=raiz(0.0), torso=(0, 0, 0), cadera=(0, 0, 0), **cuello(-8, 0, 0), **cola(10, 0), **boca(4))
    AL = 1.4

    def vuelo(t):
        f = 2 * math.pi * t / AL
        # el ala: de arriba (f = 0) a abajo (f = pi) y vuelta; los dedos van un poco detras
        k1 = 0.5 - 0.5 * math.cos(f)
        k2 = 0.5 - 0.5 * math.cos(f - 0.5)
        k3 = 0.5 - 0.5 * math.cos(f - 1.0)
        tr = [mezcla_alas(ARRIBA, ABAJO, k)[i] for i, k in enumerate((k1, k2, k3))]
        return con(VUELO, alas(*tr), raiz=raiz(2.0 * math.cos(f)), torso=(2 * math.sin(f), 0, 0),
                   **cuello(-8 - 4 * math.cos(f), 0, 0), **cola(10 - 4 * math.cos(f + 0.6), 0, ola=4, fase=f, olay=3))
    bucle("vuelo", AL, [(k * AL / 8, vuelo(k * AL / 8)) for k in range(9)])

    # ---- planeo: las alas abiertas, quietas (solo se le mecen las puntas), la cola ondula
    def planeo(t):
        f = 2 * math.pi * t / 3.0
        tr = mezcla_alas(ABIERTAS, ARRIBA, 0.08 + 0.05 * math.sin(f))
        return con(VUELO, alas(*tr), raiz=raiz(0.6 * math.sin(f)), **cuello(-6, 4 * math.sin(f), 0),
                   **cola(10, 0, ola=6, fase=f))
    bucle("planeo", 3.0, [(k * 0.375, planeo(k * 0.375)) for k in range(9)])

    # ================================================================ acciones
    # ---- despierta (desde dormido): alza la cabeza, se levanta, abre las alas y ruge
    RUGE = con(BASE, alas(*ALTAS), raiz=raiz(1.5), cadera=(-6, 0, 0), torso=(-14, 0, 0),
               **cuello(-30, 0, 0), **cola(14, 0), **boca(42), **patas(-10, -10, -6, -6))
    accion("despierta", 3.2, [
        (0.00, dormido(0.0)),
        (0.60, con(DORMIDO, cuello1=(10, -20, 0), cuello2=(0, -18, 0), cuello3=(-14, -10, 0), cabeza=(-20, -4, 0), mandibula=(6, 0, 0))),
        (1.20, con(LISTO, alas(*MEDIO), raiz=raiz(-6.0), **cuello(0, 0, 0), **boca(14))),
        (1.80, con(RUGE, alas(*ARRIBA), raiz=raiz(0.5))),
        (2.10, RUGE),
        (2.50, con(RUGE, cuello1=(-12, 4, 0), cabeza=(-6, -6, 0), mandibula=(46, 0, 0))),
        (2.80, con(RUGE, cuello1=(-10, -4, 0), cabeza=(-8, 6, 0), mandibula=(40, 0, 0))),
        (3.20, LISTO),
    ])
    # ---- el rugido: se echa atras, abre las alas en alto y ruge sacudiendo la cabeza
    accion("rugido", 2.6, [
        (0.00, LISTO),
        (0.45, con(LISTO, alas(*MEDIO), raiz=raiz(-3.0), torso=(10, 0, 0), **cuello(26, 0, 0), **boca(2))),
        (0.85, RUGE),
        (1.25, con(RUGE, cuello1=(-12, 6, 0), cabeza=(-6, -8, 0), mandibula=(46, 0, 0))),
        (1.65, con(RUGE, cuello1=(-10, -6, 0), cabeza=(-8, 8, 0), mandibula=(44, 0, 0))),
        (2.05, con(RUGE, mandibula=(30, 0, 0))),
        (2.60, LISTO),
    ])
    # ---- el mordisco: echa el cuello atras con la boca abierta y lo lanza hacia delante (golpe a los 0,7 s)
    ATRAS = con(LISTO, raiz=raiz(0.5, 2.0), torso=(-4, 0, 0), **cuello(-24, 0, 0), **boca(38))
    MUERDE = con(LISTO, raiz=raiz(-2.5, -3.0), torso=(10, 0, 0), cadera=(-4, 0, 0), cuello1=(30, 0, 0), cuello2=(10, 0, 0), cuello3=(6, 0, 0), cabeza=(-6, 0, 0), **boca(4),
                 **patas(-20, -16, 10, 8, agacha=8))
    accion("mordisco", 1.4, [
        (0.00, LISTO),
        (0.45, ATRAS),
        (0.70, MUERDE),
        (0.85, con(MUERDE, cabeza=(-6, 12, 0), mandibula=(2, 0, 0))),
        (1.00, con(MUERDE, cabeza=(-6, -10, 0))),
        (1.40, LISTO),
    ], golpes=(0.70,))
    # ---- el zarpazo: alza la mano derecha y barre por delante, de derecha a izquierda (golpe a los 0,7 s)
    ALZA = con(LISTO, ala("d", *ALTAS), ala("i", *[((-h[0], h[1], h[2]), (-f[0], f[1], f[2])) for h, f in MEDIO]),
               torso=(-8, 12, 8), cadera=(-3, -6, 0), raiz=raiz(1.0),
               brazo_d=(-135, -10, -58), antebrazo_d=(-50, 0, 0), mano_d=(-40, 0, 0), **cuello(4, -20, 0), **boca(18))
    BARRE = con(LISTO, torso=(8, -18, 6), cadera=(-2, 8, 0), raiz=raiz(-2.0),
                brazo_d=(-55, 50, 30), antebrazo_d=(-10, 0, 0), mano_d=(20, 0, 0), **cuello(14, 16, 0), **boca(26))
    accion("zarpazo", 1.3, [
        (0.00, LISTO),
        (0.45, ALZA),
        (0.68, BARRE),
        (0.90, con(BARRE, brazo_d=(-30, 30, 10), antebrazo_d=(10, 0, 0))),
        (1.30, LISTO),
    ], golpes=(0.68,))
    # ---- el coletazo: se retuerce y la cola barre de un lado a otro por detras (golpe a los 0,95 s)
    ARMA_COLA = con(LISTO, torso=(2, -16, 0), cadera=(0, 18, 0), **cuello(10, -30, 0),
                    cola1=(6, 30, 0), cola2=(4, 26, 0), cola3=(2, 22, 0), cola4=(0, 18, 0), cola5=(0, 14, 0), cola6=(0, 10, 0))
    BARRE_COLA = con(LISTO, torso=(2, 18, 0), cadera=(0, -24, 0), **cuello(10, 34, 0),
                     cola1=(4, -36, 0), cola2=(2, -30, 0), cola3=(0, -26, 0), cola4=(0, -22, 0), cola5=(0, -18, 0), cola6=(0, -14, 0),
                     **patas(8, -8, -10, 10, agacha=6))
    accion("coletazo", 1.8, [
        (0.00, LISTO),
        (0.60, ARMA_COLA),
        (0.95, BARRE_COLA),
        (1.20, con(BARRE_COLA, cola4=(0, -30, 0), cola5=(0, -30, 0), cola6=(0, -26, 0))),
        (1.80, LISTO),
    ], golpes=(0.95,))
    # ---- el aletazo: abre las alas del todo y las baja de golpe hacia delante (el viento derriba; golpe a los 0,8 s)
    AL_ARRIBA = con(LISTO, alas(*ALTAS), raiz=raiz(1.0), torso=(-10, 0, 0), **cuello(-10, 0, 0), **boca(20))
    AL_ABAJO = con(LISTO, alas(*mezcla_alas(ABAJO, ABIERTAS, 0.2)), raiz=raiz(-3.0), torso=(10, 0, 0), **cuello(20, 0, 0),
                   **boca(30), **patas(-6, -6, 6, 6, agacha=10))
    accion("aletazo", 1.5, [
        (0.00, LISTO),
        (0.50, AL_ARRIBA),
        (0.80, AL_ABAJO),
        (1.05, con(AL_ABAJO, alas(*mezcla_alas(ABAJO, MEDIO, 0.4)))),
        (1.50, LISTO),
    ], golpes=(0.80,))
    # ---- el pisoton: se alza sobre las patas de atras y cae con las de delante (la onda; golpe a los 1,15 s)
    ALZADO = con(LISTO, alas(*ALTAS), raiz=raiz(3.0, 2.0), cadera=(-22, 0, 0), torso=(-16, 0, 0),
                 brazo_d=(-90, 0, -10), antebrazo_d=(-30, 0, 0), mano_d=(-20, 0, 0),
                 brazo_i=(-90, 0, 10), antebrazo_i=(-30, 0, 0), mano_i=(-20, 0, 0),
                 muslo_d=(18, 0, 0), pierna_d=(16, 0, 0), pata_d=(-12, 0, 0), muslo_i=(18, 0, 0), pierna_i=(16, 0, 0), pata_i=(-12, 0, 0),
                 **cuello(-30, 0, 0), **cola(22, 0), **boca(36))
    CAE = con(LISTO, alas(*mezcla_alas(MEDIO, ABAJO, 0.5)), raiz=raiz(-4.0, -2.0), cadera=(4, 0, 0), torso=(12, 0, 0),
              **patas(-30, -30, 4, 4, agacha=10), **cuello(30, 0, 0), **boca(24))
    accion("pisoton", 2.0, [
        (0.00, LISTO),
        (0.70, ALZADO),
        (0.95, con(ALZADO, raiz=raiz(4.0, 2.0))),
        (1.15, CAE),
        (1.40, con(CAE, raiz=raiz(-3.0, -2.0))),
        (2.00, LISTO),
    ], golpes=(1.15,))
    # ---- el aliento: coge aire (el pecho se hincha, la cabeza atras) y echa fuego barriendo de su derecha a su izquierda
    GIRO = 34.0

    def echa(ang, abre=40):
        return con(LISTO, raiz=raiz(-2.0), torso=(6, ang * 0.12, 0), **cuello(22, ang, 0), **boca(abre), **patas(-8, -8, 0, 0, agacha=8))
    accion("aliento", 3.8, [
        (0.00, LISTO),
        (0.55, con(LISTO, raiz=raiz(1.0), torso=(-10, 0, 0), **cuello(-22, GIRO * 0.6, 0), **boca(10))),
        (0.90, echa(GIRO, 34)),
        (1.00, echa(GIRO)),
        (1.60, echa(GIRO * 0.45)),
        (2.20, echa(-GIRO * 0.1)),
        (2.80, echa(-GIRO * 0.65)),
        (3.20, echa(-GIRO)),
        (3.40, echa(-GIRO * 0.9, 20)),
        (3.80, LISTO),
    ])
    # ---- despega: se agacha, salta con un aletazo y sube (vuela desde el tick 16)
    accion("despega", 2.0, [
        (0.00, LISTO),
        (0.50, con(LISTO, alas(*ALTAS), raiz=raiz(-6.0), **patas(-10, -10, 10, 10, agacha=26), torso=(6, 0, 0), **cuello(10, 0, 0))),
        (0.80, con(VUELO, alas(*ABAJO), raiz=raiz(4.0), **cuello(-14, 0, 0), **cola(-8, 0),
                   **patas(-30, -30, 30, 30))),
        (1.15, con(VUELO, alas(*ARRIBA), raiz=raiz(2.0), **cuello(-10, 0, 0), **cola(4, 0))),
        (1.50, con(VUELO, alas(*ABAJO), raiz=raiz(1.0))),
        (2.00, vuelo(0.0)),
    ])
    # ---- se posa: llega con las alas abiertas y las patas por delante, cae (la onda; golpe a los 0,25 s) y pliega las alas
    accion("aterriza", 1.6, [
        (0.00, con(VUELO, PATAS_POSARSE, alas(*ABIERTAS), raiz=raiz(6.0), torso=(-14, 0, 0), **cuello(-6, 0, 0))),
        (0.25, con(LISTO, alas(*mezcla_alas(ABAJO, ABIERTAS, 0.4)), raiz=raiz(-7.0), torso=(10, 0, 0),
                   **patas(-14, -14, 8, 8, agacha=22), **cuello(24, 0, 0), **boca(26))),
        (0.60, con(LISTO, alas(*MEDIO), raiz=raiz(-3.0), **cuello(8, 0, 0), **boca(30))),
        (1.10, con(LISTO, alas(*mezcla_alas(PLEGADAS, MEDIO, 0.6)))),
        (1.60, LISTO),
    ], golpes=(0.25,))
    # ---- el vuelo rasante echando fuego: el cuello hacia abajo y delante, la boca abierta (va sobre el bucle de vuelo; solo
    # cuello, cabeza y mandibula)
    RASA = con(cuello(36, 0, 0), boca(40))
    accion("rasante", 3.0, [
        (0.00, con(cuello(0, 0, 0), boca(0))),
        (0.50, con(cuello(26, 0, 0), boca(20))),
        (0.70, RASA),
        (1.60, con(RASA, cuello1=(10, 4, 0), cabeza=(6, -4, 0))),
        (2.50, RASA),
        (2.80, con(cuello(14, 0, 0), boca(10))),
        (3.00, con(cuello(0, 0, 0), boca(0))),
    ])
    # ---- escupe una bola de fuego (sobre el bucle que toque; golpe a los 0,55 s): atras y adelante con la boca abierta
    accion("escupe", 1.2, [
        (0.00, con(cuello(0, 0, 0), boca(0))),
        (0.35, con(cuello(-22, 0, 0), boca(14))),
        (0.55, con(cuello(20, 0, 0), boca(42))),
        (0.80, con(cuello(12, 0, 0), boca(20))),
        (1.20, con(cuello(0, 0, 0), boca(0))),
    ], golpes=(0.55,))
    # ---- el picado: las alas medio recogidas y echadas atras, el cuello estirado; al final, las patas por delante
    PICA = con(VUELO, alas(*mezcla_alas(ABIERTAS, PLEGADAS, 0.45)), torso=(4, 0, 0), **cuello(-4, 0, 0), **cola(14, 0), **boca(20))
    accion("picado", 2.4, [
        (0.00, vuelo(0.0)),
        (0.40, con(VUELO, alas(*ALTAS), **cuello(-16, 0, 0), **boca(30))),
        (0.80, PICA),
        (1.90, con(PICA, cuello1=(-6, 0, 0), mandibula=(36, 0, 0))),
        (2.40, con(PICA, PATAS_POSARSE, alas(*ABIERTAS), torso=(-10, 0, 0))),
    ])
    # ---- la lluvia de ascuas: se queda en el aire casi de pie, aletea fuerte y ruge al cielo
    CIELO = con(VUELO, PATAS_POSARSE, raiz=raiz(4.0), torso=(-38, 0, 0), cadera=(-8, 0, 0), **cuello(-44, 0, 0), **cola(-16, 0), **boca(46))
    accion("ascuas", 3.0, [
        (0.00, vuelo(0.0)),
        (0.40, con(CIELO, alas(*ALTAS), mandibula=(12, 0, 0))),
        (0.80, con(CIELO, alas(*ABAJO))),
        (1.20, con(CIELO, alas(*ALTAS))),
        (1.60, con(CIELO, alas(*ABAJO), cuello1=(-14, 6, 0), cabeza=(-10, -6, 0))),
        (2.00, con(CIELO, alas(*ALTAS), cuello1=(-14, -6, 0), cabeza=(-10, 6, 0))),
        (2.40, con(CIELO, alas(*ABAJO))),
        (3.00, vuelo(0.0)),
    ])
    # ---- aturdido: se le doblan las patas, cae de lado y sacude la cabeza; se levanta
    TIRADO = con(BASE, alas(*mezcla_alas(PLEGADAS, ABIERTAS, 0.35)), raiz=raiz(-14.0, 0, 0, rot=(0, 0, -20)),
                 torso=(4, 0, 6), **patas(-40, -50, -30, -40, agacha=20), **cuello(36, -20, 0), **cola(-8, 10), **boca(20))
    accion("aturdido", 3.0, [
        (0.00, LISTO),
        (0.40, con(TIRADO, raiz=raiz(-8.0, 0, 0, rot=(0, 0, -10)))),
        (0.80, TIRADO),
        (1.30, con(TIRADO, cabeza=(0, 16, 0), mandibula=(30, 0, 0))),
        (1.80, con(TIRADO, cabeza=(0, -14, 0), mandibula=(10, 0, 0))),
        (2.40, con(LISTO, raiz=raiz(-6.0), **cuello(28, 0, 0))),
        (3.00, LISTO),
    ])
    # ---- morir: se alza aleteando sin fuerza, se tambalea, cae sobre el pecho y se ladea; el cuello cae y las alas se abren por
    # el suelo; el ultimo aliento
    MUERTO = con(BASE, alas(*mezcla_alas(ABIERTAS, ABAJO, 0.35)), raiz=raiz(-18.0, 0, 0, rot=(0, 0, 18)), torso=(2, 0, 4), cadera=(0, 0, 0),
                 **patas(-60, -70, -50, -60, agacha=24), cuello1=(26, 20, 0), cuello2=(16, 22, 0), cuello3=(6, 16, 0), cabeza=(-8, 10, -16),
                 **cola(-6, -10), **boca(22))
    accion("morir", 5.0, [
        (0.00, LISTO),
        (0.50, con(RUGE, alas(*ARRIBA), raiz=raiz(2.0), **boca(40))),
        (1.00, con(RUGE, alas(*mezcla_alas(MEDIO, ABAJO, 0.5)), raiz=raiz(-1.0), torso=(-6, 8, 6), **cuello(-10, 14, 0), **boca(30))),
        (1.60, con(LISTO, alas(*MEDIO), raiz=raiz(-6.0), torso=(10, -6, -6), **cuello(30, -16, 0), **boca(20),
                   **patas(-30, -10, -10, -20, agacha=14))),
        (2.30, con(MUERTO, raiz=raiz(-12.0, 0, 0, rot=(0, 0, 8)), cuello1=(10, 10, 0), cabeza=(-14, 0, 0), mandibula=(30, 0, 0))),
        (2.90, MUERTO),
        (3.80, con(MUERTO, torso=(0, 0, 4), cabeza=(-6, 10, -16), mandibula=(14, 0, 0))),
        (5.00, con(MUERTO, mandibula=(6, 0, 0))),
    ])
    return B, A


# lo que ademas de las acciones necesita el servidor: el barrido del aliento (de su derecha a su izquierda, grados) y cuando echa
# fuego, y en que tick del despegue deja el suelo
TABLA_EXTRA = (
    "",
    "    /** El aliento: de que tick a que tick echa fuego y lo que gira la cabeza (de +GIRO, a su derecha, a -GIRO). */",
    "    public static final int ALIENTO_DESDE = 18, ALIENTO_HASTA = 66;",
    "    public static final float ALIENTO_GIRO = 34f;",
    "    /** El vuelo rasante: de que tick a que tick echa fuego. */",
    "    public static final int RASANTE_DESDE = 12, RASANTE_HASTA = 52;",
    "    /** El despegue: el tick en que deja el suelo. */",
    "    public static final int DESPEGA_SALTO = 15;",
    "    /** La lluvia de ascuas: el tick del rugido al cielo. */",
    "    public static final int ASCUAS_RUGIDO = 16;",
)


def base_de(nombre, por_nombre):
    """Para las hojas de revision: las acciones que van sobre el bucle de vuelo (solo mueven el cuello) se ven con el."""
    if nombre in ("rasante", "escupe"):
        return [por_nombre["vuelo"]]
    return []
