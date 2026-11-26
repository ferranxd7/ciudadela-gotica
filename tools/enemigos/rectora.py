"""
Ysolde, la Rectora de la Academia (parte 7p): el jefe de la Academia Espectral. La noche del cielo rojo subio al
Observatorio a leer el fuego que caia ("y el cielo la lee a ella", dice Maese Orlen en el libro 24) y alli se quedo,
flotando ante el gran telescopio con la mano alzada hacia las estrellas. Un espectro alto como los eruditos (medio
transparente: lo pone el dibujante), sin piernas: el vestido azul de noche con estrellas bordadas acaba en jirones y flota
un palmo sobre el suelo. El cuello alto de hechicera, el manto largo con las constelaciones por fuera (lucen), la melena
blanca, la diadema con su gema y, detras de la cabeza, la aureola de estrellas (un astrolabio de luz); al cinto, un
astrolabio pequeno. En la derecha, el baculo de plata rematado con la estrella de cristal entre dos puntas; la izquierda,
libre: con ella llama a las estrellas (le lucen los dedos).

Una sola variante (escala 1,6). Acciones: baculo (lo echa atras y barre de su derecha a su izquierda),
orbes (carga la izquierda y suelta tres orbes en abanico), estrellas (el baculo y la mano al cielo y los baja de golpe: la
lluvia de estrellas), constelacion (dibuja con la punta del baculo tres estrellas en el aire y las clava en el suelo),
parpadeo (se apaga y aparece en otro sitio), grito (al empezar), alzarse (a media vida: sube con los brazos abiertos y
llama a sus maestros), aturdido, morir (se hunde, el baculo cae a su lado, la cabeza baja: el cielo deja de leerla) y
levantarse (estaba leyendo el cielo y baja la cabeza hacia quien ha entrado).
"""
import math

from anim import Anim, con, raiz, resolver
from modelo import Box, Rig, humanoide
from pinta import (claro, delante, dibujo, liso, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "rectora"
VARIANTES = ("rectora",)
ESCALA = 1.6
CENTRO_Y = 1.6

# ---------------------------------------------------------------- el modelo

#: las estrellas de la aureola (detras de la cabeza): (angulo en grados, lado de la caja)
AUREOLA = [(a, 1.8 if a % 90 == 0 else 1.0) for a in range(0, 360, 30)]
R_AUREOLA = 6.4


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 4), cabeza=(8, 8, 8), brazo=(3, 14, 3), pierna=(4, 12, 4), hombro=0.5)
    # ---- el vestido (en dos trozos, para que ondee): de la cintura a un palmo del suelo, en jirones; el cinto con su astrolabio
    r.pieza("vestido", "cadera", (0, -0.5, 0), boxes=[Box(-4.6, 0, -2.6, 9.2, 7, 5.2, "vestido")])
    r.pieza("vestido_bajo", "vestido", (0, 6.5, 0), boxes=[Box(-6.2, 0, -3.6, 12.4, 8.2, 7.2, "vestido_bajo")])
    r.pieza("cinto", "torso", (0, -1.6, 0), boxes=[Box(-4.3, -0.6, -2.3, 8.6, 1.1, 4.6, "cinto"),
                                                    Box(2.4, 0.5, -2.9, 2.4, 2.4, 0.5, "astrolabio")])
    # ---- las mangas acampanadas, el cuello alto de hechicera y el manto largo hasta el suelo
    for n in ("d", "i"):
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 2.0, 0), boxes=[Box(-2.6, 0, -2.6, 5.2, 5.6, 5.2, f"manga_{n}")])
    r.pieza("cuello", "torso", (0, -12, 1.8), rot=(-12, 0, 0), boxes=[Box(-5.2, -8.0, 0, 10.4, 8.4, 0.7, "cuello")])
    r.pieza("capa", "torso", (0, -11.6, 2.6), rot=(6, 0, 0), boxes=[Box(-5.4, 0, 0, 10.8, 24.5, 0.8, "capa")])
    # ---- la melena por la espalda, la diadema con su gema y la aureola de estrellas detras de la cabeza
    r.pieza("pelo", "cabeza", (0, -7.6, 3.2), boxes=[Box(-4.3, 0, 0, 8.6, 14, 1.3, "pelo")])
    r.pieza("diadema", "cabeza", (0, 0, 0), boxes=[Box(-4.3, -6.9, -4.3, 8.6, 0.9, 8.6, "diadema"),
                                                    Box(-0.7, -7.2, -4.7, 1.4, 1.4, 0.5, "gema")])
    estrellas = []
    for a, lado in AUREOLA:
        x, y = R_AUREOLA * math.cos(math.radians(a)), R_AUREOLA * math.sin(math.radians(a))
        estrellas.append(Box(x - lado / 2, y - lado / 2, 0, lado, lado, 0.5, "aureola"))
    r.pieza("aureola", "cabeza", (0, -4.5, 5.4), boxes=estrellas)
    # ---- el baculo de plata (a lo largo de -Z, como un arma: la estrella arriba cuando apunta al cielo; abajo llega al suelo)
    r.pieza("baculo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.45, -0.45, -22.0, 0.9, 0.9, 36.0, "baculo"),
        Box(-2.4, -0.4, -22.8, 4.8, 0.8, 0.8, "plata"),
        Box(-2.4, -0.4, -26.6, 0.8, 0.8, 3.8, "plata"),
        Box(1.6, -0.4, -26.6, 0.8, 0.8, 3.8, "plata"),
        Box(-1.1, -1.1, -25.8, 2.2, 2.2, 2.2, "estrella"),
        Box(-0.4, -0.4, -28.6, 0.8, 0.8, 7.8, "rayo"),
        Box(-0.4, -2.9, -25.1, 0.8, 5.8, 0.8, "rayo"),
        Box(-1.7, -0.4, -25.1, 3.4, 0.8, 0.8, "rayo"),
    ])
    r.empaquetar()
    return r


PIERNAS = ["muslo_d", "muslo_i", "pierna_d", "pierna_i"]
#: lo que se esconde (no tiene piernas: el vestido acaba en jirones)
OCULTAS = {0: PIERNAS}

#: lo que va ademas en RectoraAnims (la escala, para el dibujante y el servidor)
TABLA_EXTRA = (
    "",
    f"    public static final float ESCALA = {ESCALA}f;",
)

# ---------------------------------------------------------------- las texturas
# El juego la dibuja medio transparente y algo fria: aqui va con su color.

ALMA = ["#1c1b30", "#2b2944", "#3e3b60", "#5a5684", "#807cab", "#aca8d0", "#dcd9f2"]
NOCHE = ["#080a1a", "#0d1128", "#141a3a", "#1c254e", "#263266", "#31407e"]     # el vestido, azul de noche
MANTO = ["#0b0716", "#130c24", "#1c1236", "#27194a", "#33215f"]                 # el manto, morado oscuro
ORO = ["#5a4a22", "#857033", "#b39a4a", "#dcc472", "#f2e2a0"]
PLATA = ["#3a3f52", "#5a6178", "#7f88a2", "#aab3cc", "#d4dbec"]
PELO = ["#8f8aa8", "#b4afcc", "#d2cee6", "#ece9f8"]
HUECO = "#07061a"
OJO = "#e4ecff"
LUZ = ["#9fb8ff", "#c4d4ff", "#eef3ff"]                                          # la luz de las estrellas
RUNA = "#c3a6ff"


def _azar01(*v):
    """Un numero de 0 a 1 que solo depende de v (sin el hash de Python, que cambia de una ejecucion a otra)."""
    h = 2166136261
    for n in v:
        h = ((h ^ (int(n) & 0xffffffff)) * 16777619) & 0xffffffff
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    h ^= h >> 15
    return h / 4294967296.0


CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _estrella(cara, x, y, semilla, cuantas=0.07):
    """Si en ese pixel hay una estrella bordada (salpicadas, nunca dos juntas)."""
    k = CARAS[cara]
    if (x + y) % 2:
        return False
    return _azar01(x, y, k, semilla) < cuantas


# las constelaciones del manto (por fuera, en la espalda): estrellas unidas por hilos de oro
CONSTELACIONES = [
    [(2, 3), (4, 5), (7, 4), (8, 7)],
    [(1, 11), (3, 13), (2, 16), (5, 17), (6, 14)],
    [(6, 20), (8, 22), (7, 25), (4, 23)],
    [(3, 7), (2, 9)],
]


def _segmento(px, py, a, b):
    (x0, y0), (x1, y1) = a, b
    dx, dy = x1 - x0, y1 - y0
    L = dx * dx + dy * dy
    t = 0 if L == 0 else max(0.0, min(1.0, ((px - x0) * dx + (py - y0) * dy) / L))
    qx, qy = x0 + t * dx, y0 + t * dy
    return (px - qx) ** 2 + (py - qy) ** 2


def _en_constelacion(x, y):
    """0 nada, 1 hilo, 2 estrella."""
    for c in CONSTELACIONES:
        if (x, y) in c:
            return 2
    for c in CONSTELACIONES:
        for a, b in zip(c, c[1:]):
            if _segmento(x, y, a, b) < 0.26:
                return 1
    return 0


def materiales(variante):
    alma = ruido(ALMA[3:7], 911, grano=0.4, base=0.55)
    noche = tela(NOCHE, 921, pliegue=4, grano=0.12, luz=0.28)
    manto = tela(MANTO, 922, pliegue=3, grano=0.12, luz=0.2)
    oro = ruido(ORO[1:4], 931, grano=0.3)
    plata = ruido(PLATA[1:5], 932, grano=0.25)

    def cabeza(cara, x, y, w, h):
        c = alma(cara, x, y, w, h)
        if cara == "frente":
            C = {"K": HUECO, "s": ALMA[3], "o": ALMA[4], "b": "#120f26", "c": ALMA[5], "g": ALMA[6], "p": PELO[2]}
            filas = [
                "pppppppp",
                "ppccccpp",
                "pcsccscp",
                "psKccKsp",
                "pcsgcscp",
                "pcccccop",
                "pocbbcop",
                "poocccop",
            ]
            v = dibujo(filas, C)(cara, x, y, w, h)
            return v if v is not None else c
        if cara == "arriba" or cara in ("der", "izq", "espalda"):
            # la melena blanca: lo de arriba, los lados (salvo la cara) y la nuca
            if cara in ("der", "izq") and delante(cara, x, w) <= 1 and y >= 2:
                return oscuro(c, 0.25)
            return PELO[(x * 3 + y) % 4] if (x + y) % 5 else PELO[0]
        return c

    def torso(cara, x, y, w, h):
        c = noche(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if cara == "frente":
            # el corpino: el escote en pico con el ribete de oro y una estrella de plata en el pecho
            if y <= 4 and abs(x + 0.5 - w / 2) < 2.6 - y * 0.5:
                return alma(cara, x, y, w, h)
            if y <= 5 and abs(abs(x + 0.5 - w / 2) - (2.6 - y * 0.5)) < 0.6:
                return ORO[3]
            if (x, y) in ((3, 7), (4, 7), (3, 8), (4, 8)):
                return PLATA[4] if (x + y) % 2 else PLATA[3]
        if _estrella(cara, x, y, 941):
            return PLATA[3]
        return c

    def brazo(cara, x, y, w, h):
        c = noche(cara, x, y, w, h)
        return PLATA[3] if _estrella(cara, x, y, 942, 0.05) else c

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return alma(cara, x, y, w, h)                                    # la muneca y la mano, al aire
        return brazo(cara, x, y, w, h)

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None                                                      # hueca: por dentro sale la mano
        if y == h - 1 and (x * 5 + 2) % 3 == 0:
            return None
        c = noche(cara, x, y, w, h)
        if y >= h - 2:
            return ORO[3] if (x + y) % 2 else ORO[2]                         # el ribete de oro
        if _estrella(cara, x, y, 943, 0.08):
            return PLATA[3]
        return c

    def vestido(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = noche(cara, x, y, w, h)
        if _estrella(cara, x, y, 944, 0.08):
            return PLATA[3] if (x + y) % 4 else ORO[3]
        return oscuro(c, 0.04 * y / max(1, h - 1))

    def vestido_bajo(cara, x, y, w, h):
        # se deshace en jirones: cada columna acaba a una altura, cada vez mas oscura y con huecos
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 2) % 4) - (1 if cara in ("der", "izq") else 0)
        if y > largo:
            return None
        if y >= h - 3 and (x * 3 + y) % 4 == 0:
            return None
        if y == 0:
            return ORO[2]                                                    # la cenefa de oro
        c = noche(cara, x, y, w, h)
        if _estrella(cara, x, y, 945, 0.08):
            return PLATA[3]
        return oscuro(c, 0.06 + 0.12 * y / max(1, h - 1))

    def cinto(cara, x, y, w, h):
        return ORO[2] if (x + y) % 3 else ORO[1]

    def astrolabio(cara, x, y, w, h):
        if cara in ("frente", "espalda"):
            d = ((x + 0.5 - w / 2) ** 2 + (y + 0.5 - h / 2) ** 2) ** 0.5
            return ORO[4] if d > 0.9 else ORO[2]
        return ORO[1]

    def cuello(cara, x, y, w, h):
        # el cuello alto: negro azulado por fuera con varillas de oro; por dentro (el frente) el forro con estrellas
        if cara == "abajo":
            return None
        if cara == "frente":
            if y == 0 or x in (0, w - 1):
                return ORO[3]
            return PLATA[3] if _estrella(cara, x, y, 946, 0.1) else NOCHE[3]
        if x % 3 == 1:
            return ORO[2]
        return NOCHE[1]

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return NOCHE[1]
        # los bajos en jirones
        corte = h - 1 - (0, 3, 1, 5, 2, 0, 4, 1, 3, 0, 2)[x % 11]
        if y > corte:
            return None
        if cara == "espalda":
            # por fuera: las constelaciones bordadas en oro
            k = _en_constelacion(x, y)
            if k == 2:
                return ORO[4]
            if k == 1:
                return ORO[2]
            c = manto(cara, x, y, w, h)
            if _estrella(cara, x, y, 947, 0.05):
                return PLATA[2]
            return c
        if cara == "frente":
            # por dentro: el forro azul de noche
            return oscuro(noche(cara, x, y, w, h), 0.2)
        return ORO[2] if y % 4 == 0 else MANTO[3]

    def pelo(cara, x, y, w, h):
        if cara == "arriba":
            return PELO[1]
        # las puntas desiguales y algo mas oscuras
        largo = h - 1 - ((x * 5 + 1) % 4)
        if y > largo:
            return None
        return oscuro(PELO[(x + y // 2) % 4], 0.06 * y / max(1, h - 1))

    def diadema(cara, x, y, w, h):
        return oro(cara, x, y, w, h) if (x + y) % 2 else ORO[3]

    def gema(cara, x, y, w, h):
        return "#8fb2ff"

    def aureola(cara, x, y, w, h):
        return LUZ[1] if (x + y) % 2 == 0 else LUZ[0]

    def baculo(cara, x, y, w, h):
        # plata con anillos de oro cada tanto
        if cara not in ("arriba", "abajo") and y % 9 == 0:
            return ORO[3]
        return plata(cara, x, y, w, h)

    def estrella(cara, x, y, w, h):
        return LUZ[2] if (x + y) % 2 == 0 else LUZ[1]

    def rayo(cara, x, y, w, h):
        return LUZ[1]

    return {
        "cabeza": sombra(cabeza, abajo=0.1), "torso": sombra(torso, abajo=0.18),
        "brazo_d": brazo, "brazo_i": brazo, "antebrazo_d": antebrazo, "antebrazo_i": antebrazo,
        "muslo_d": liso(ALMA[2]), "muslo_i": liso(ALMA[2]), "pierna_d": liso(ALMA[2]), "pierna_i": liso(ALMA[2]),
        "manga_d": manga, "manga_i": manga, "vestido": vestido, "vestido_bajo": vestido_bajo,
        "cinto": cinto, "astrolabio": astrolabio, "cuello": cuello, "capa": capa, "pelo": pelo,
        "diadema": diadema, "gema": gema, "aureola": aureola,
        "baculo": baculo, "plata": plata, "estrella": estrella, "rayo": rayo,
    }


def brillo(variante):
    """Lo que luce: los ojos, la gema de la diadema, la aureola, las estrellas bordadas del vestido y del manto, las
    constelaciones de la espalda, los dedos de la izquierda y la estrella del baculo."""
    ojos = dibujo([
        "........",
        "........",
        "........",
        "..R..R..",
        "........",
    ], {"R": OJO})

    def estrellas(semilla, cuantas):
        def f(cara, x, y, w, h):
            return LUZ[1] if _estrella(cara, x, y, semilla, cuantas) and (x * 7 + y) % 3 == 0 else None
        return f

    def capa(cara, x, y, w, h):
        if cara != "espalda":
            return None
        corte = h - 1 - (0, 3, 1, 5, 2, 0, 4, 1, 3, 0, 2)[x % 11]
        if y > corte:
            return None
        k = _en_constelacion(x, y)
        if k == 2:
            return LUZ[2]
        if k == 1:
            return "#7d8fd8"
        return None

    def mano(cara, x, y, w, h):
        # los dedos de la izquierda: la luz con la que llama a las estrellas
        return LUZ[0] if y >= h - 1 and (x + CARAS[cara]) % 2 == 0 else None

    def cuello(cara, x, y, w, h):
        return LUZ[0] if cara == "frente" and _estrella(cara, x, y, 946, 0.1) else None

    return {
        "cabeza": por_cara(liso(None), frente=ojos),
        "gema": liso("#b9d0ff"),
        "aureola": lambda cara, x, y, w, h: LUZ[2] if (x + y) % 2 == 0 else LUZ[1],
        "torso": estrellas(941, 0.07), "vestido": estrellas(944, 0.08), "vestido_bajo": estrellas(945, 0.08),
        "manga_d": estrellas(943, 0.08), "manga_i": estrellas(943, 0.08),
        "capa": capa, "cuello": cuello, "antebrazo_i": mano,
        "estrella": lambda cara, x, y, w, h: LUZ[2],
        "rayo": lambda cara, x, y, w, h: LUZ[1] if (x + y) % 2 == 0 else LUZ[0],
        "astrolabio": lambda cara, x, y, w, h: "#d8c47a" if cara == "frente" and (x + y) % 2 == 0 else None,
    }


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14: llega
# a unos 11); _arma = hacia donde apunta el baculo (la estrella); _agarra = la izquierda en el asta, tantos pixeles por
# debajo de la derecha (a dos manos). El porte lleva los brazos (el quieto no los toca): se suma al quieto.

ARRIBA = 3.2   # lo que flota sobre el suelo (pixeles)
BASE = dict(raiz=raiz(ARRIBA), torso=(2, 0, 0), cabeza=(3, 0, 0), vestido=(3, 0, 0), vestido_bajo=(5, 0, 0), capa=(5, 0, 0),
            brazo_d=(-4, 0, 6), antebrazo_d=(-12, 0, 0), brazo_i=(-4, 0, -6), antebrazo_i=(-12, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# el baculo derecho a su lado (la estrella arriba), la izquierda abierta a la altura de la cintura
PORTE = dict(_mano_d=("h", -1.2, -6.4, 3.4), _arma=(0.0, 1.0, 0.12), _mano_i=("h", 2.2, -7.4, 3.4))
# en guardia: el baculo terciado hacia su presa, la izquierda alzada delante, la palma hacia fuera
GUARDIA = dict(raiz=raiz(ARRIBA + 0.8), torso=(6, -12, 0), cabeza=(-4, 10, 0), vestido=(6, 0, 0), vestido_bajo=(10, 0, 0), capa=(10, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", 0.4, -3.0, 8.4), _arma=(0.0, 0.7, 0.7), _mano_i=("h", 3.0, -1.4, 7.6))
# leyendo el cielo (asi espera, ante el telescopio): la cabeza atras, la izquierda alzada hacia las estrellas, el baculo derecho
CIELO = dict(raiz=raiz(ARRIBA + 1.2), torso=(-8, 0, 0), cabeza=(-34, 0, 0), vestido=(-4, 0, 0), vestido_bajo=(-2, 0, 0),
             capa=(-2, 0, 0), _mano_d=("h", -1.4, -5.8, 3.8), _arma=(0.0, 1.0, 0.1), _mano_i=("h", 2.6, 8.4, 4.4))


def animaciones(r=None):
    """(bucles, acciones): los bucles por nombre y las acciones en el orden de sus numeros."""
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses, **kw):
        a = Anim(nombre, largo, bucle=True).poses(poses, r, **kw)
        B.append(a)
        return a

    def accion(nombre, largo, poses, golpes=()):
        a = Anim(nombre, largo, golpes=golpes).poses(poses, r)
        A.append(a)
        return a

    def fija(pose):
        """La pose con sus objetivos ya resueltos: un bucle casi quieto no necesita un fotograma por tick."""
        return resolver(r, pose)

    # ---- flotar (quieta): sube y baja despacio, el vestido y el manto ondean, la cabeza se ladea
    bucle("quieto", 4.4, [
        (0.0, BASE),
        (1.1, con(BASE, raiz=raiz(ARRIBA + 0.9), torso=(3, 4, 2), cabeza=(4, -10, 5), vestido=(-2, 0, 3), vestido_bajo=(10, 0, -4),
                  capa=(9, 0, 2))),
        (2.2, con(BASE, raiz=raiz(ARRIBA + 1.4), torso=(4, 0, 0), cabeza=(8, 0, 0), vestido=(6, 0, 0), vestido_bajo=(2, 0, 0), capa=(3, 0, 0))),
        (3.3, con(BASE, raiz=raiz(ARRIBA + 0.7), torso=(3, -4, -2), cabeza=(4, 10, -5), vestido=(-2, 0, -3), vestido_bajo=(10, 0, 4),
                  capa=(9, 0, -2))),
        (4.4, BASE),
    ])
    # ---- andar: se desliza inclinada, el vestido y el manto por detras
    DESLIZA = con(BASE, raiz=raiz(ARRIBA + 1.4), torso=(12, 0, 0), cabeza=(-6, 0, 0), vestido=(20, 0, 0), vestido_bajo=(16, 0, 0),
                  capa=(28, 0, 0))
    bucle("andar", 1.8, [
        (0.0, DESLIZA),
        (0.45, con(DESLIZA, raiz=raiz(ARRIBA + 1.8), vestido=(24, 0, 4), vestido_bajo=(22, 0, -6), capa=(34, 0, 3))),
        (0.9, con(DESLIZA, raiz=raiz(ARRIBA + 1.2), vestido=(18, 0, 0), vestido_bajo=(12, 0, 0), capa=(26, 0, 0))),
        (1.35, con(DESLIZA, raiz=raiz(ARRIBA + 1.8), vestido=(24, 0, -4), vestido_bajo=(22, 0, 6), capa=(34, 0, -3))),
        (1.8, DESLIZA),
    ])
    # ---- como lleva el baculo (se suma; solo los brazos) y en guardia
    q = fija(con(BASE, PORTE))
    bucle("porte_baculo", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    l0 = fija(LISTO)
    bucle("listo_baculo", 2.2, [
        (0.0, l0),
        (1.1, fija(con(LISTO, raiz=raiz(ARRIBA + 1.6), torso=(8, -14, 0), _mano_i=("h", 3.2, -0.6, 7.8)))),
        (2.2, l0),
    ])
    # ---- leyendo el cielo (la postura de rezar del juego): la mano alzada sigue a las estrellas, despacio
    c0 = fija(CIELO)
    bucle("cielo", 6.0, [
        (0.0, c0),
        (2.0, fija(con(CIELO, raiz=raiz(ARRIBA + 1.8), cabeza=(-38, 8, 0), _mano_i=("h", 4.4, 8.0, 3.6)))),
        (4.0, fija(con(CIELO, raiz=raiz(ARRIBA + 1.0), cabeza=(-32, -6, 0), _mano_i=("h", 1.2, 8.8, 4.8)))),
        (6.0, c0),
    ])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 baculo: lo echa atras a su derecha y barre de derecha a izquierda (la estrella delante); la izquierda equilibra
    ATRAS = con(L, raiz=raiz(ARRIBA + 1.0, 1.2), torso=(8, 46, 0), cabeza=(-6, -38, 0), capa=(14, 0, 0),
                _mano_d=("h", -4.0, -2.0, 4.0), _arma=(-0.85, 0.15, -0.45), _mano_i=("h", 3.6, -1.6, 6.4))
    CRUZA = con(L, raiz=raiz(ARRIBA + 0.2, -1.8), torso=(14, -40, 0), cabeza=(-10, 34, 0), capa=(4, 0, 0), vestido_bajo=(18, 0, 0),
                _mano_d=("h", 6.0, -2.6, 6.4), _arma=(0.92, 0.05, -0.3), _mano_i=("h", 5.4, -5.6, 1.4))
    accion("baculo", 1.6, [
        (0.00, L),
        (0.45, ATRAS),
        (0.66, con(ATRAS, torso=(8, 50, 0))),
        (0.80, con(L, raiz=raiz(ARRIBA + 0.6, -0.8), torso=(12, 4, 0), _mano_d=("h", 1.4, -2.4, 8.6), _arma=(0.1, 0.05, 1),
                   _mano_i=("h", 4.4, -4.0, 4.6))),
        (0.92, CRUZA),
        (1.20, con(CRUZA, torso=(14, -44, 0))),
        (1.60, L),
    ], golpes=(0.80,))
    # ---- 2 orbes: alza la izquierda abierta (la luz se junta en la palma) y la empuja hacia su presa: tres orbes en abanico
    CARGA = con(L, raiz=raiz(ARRIBA + 1.8), torso=(-8, 16, 0), cabeza=(-12, -10, 0), capa=(14, 0, 0),
                _mano_d=("h", -1.4, -5.6, 4.0), _arma=(0.0, 1.0, 0.15), _mano_i=("h", 3.6, 3.4, 4.6))
    EMPUJA = con(L, raiz=raiz(ARRIBA + 0.8, -2.0), torso=(10, -14, 0), cabeza=(-6, 12, 0), vestido=(10, 0, 0), vestido_bajo=(16, 0, 0),
                 capa=(18, 0, 0), _mano_d=("h", -1.4, -5.6, 4.0), _arma=(0.0, 1.0, 0.15), _mano_i=("h", 2.0, -0.6, 9.6))
    accion("orbes", 1.8, [
        (0.00, L),
        (0.50, CARGA),
        (0.80, con(CARGA, raiz=raiz(ARRIBA + 2.2), torso=(-10, 18, 0), _mano_i=("h", 3.8, 4.0, 4.0))),
        (0.95, EMPUJA),
        (1.25, con(EMPUJA, raiz=raiz(ARRIBA + 0.6, -1.6))),
        (1.80, L),
    ], golpes=(0.95,))
    # ---- 3 estrellas: el baculo y la mano al cielo (se ve venir: un segundo) y los baja de golpe: caen las estrellas
    INVOCA = con(BASE, raiz=raiz(ARRIBA + 3.0), torso=(-14, 0, 0), cabeza=(-32, 0, 0), vestido=(-8, 0, 0), vestido_bajo=(-4, 0, 0),
                 capa=(22, 0, 0), _mano_d=("h", -0.6, 9.2, 2.0), _arma=(0.0, 1.0, -0.15), _mano_i=("h", 1.4, 9.4, 2.4))
    BAJA = con(L, raiz=raiz(ARRIBA + 0.4, -1.4), torso=(18, 0, 0), cabeza=(4, 0, 0), vestido=(12, 0, 0), vestido_bajo=(18, 0, 0),
               capa=(6, 0, 0), _mano_d=("h", 0.8, -1.4, 9.0), _arma=(0.0, -0.3, 1.0), _mano_i=("h", 2.6, -2.4, 8.4))
    accion("estrellas", 2.6, [
        (0.00, L),
        (0.55, con(INVOCA, raiz=raiz(ARRIBA + 1.8))),
        (1.05, INVOCA),
        (1.20, con(INVOCA, raiz=raiz(ARRIBA + 3.4), torso=(-16, 0, 0), cabeza=(-36, 0, 0))),
        (1.30, BAJA),
        (1.90, con(BAJA, torso=(20, 0, 0))),
        (2.60, L),
    ], golpes=(1.30,))
    # ---- 4 constelacion: dibuja con la punta del baculo tres estrellas en el aire y lo clava apuntando al suelo
    P1 = con(L, torso=(4, 16, 0), cabeza=(-8, -12, 0), _mano_d=("h", -3.6, 2.0, 8.4), _arma=(-0.3, 0.6, 0.8))
    P2 = con(L, torso=(2, 0, 0), cabeza=(-12, 0, 0), _mano_d=("h", 1.0, 4.6, 8.2), _arma=(0.0, 0.8, 0.6))
    P3 = con(L, torso=(4, -16, 0), cabeza=(-8, 12, 0), _mano_d=("h", 4.6, 1.2, 8.0), _arma=(0.35, 0.6, 0.75))
    APUNTA = con(L, raiz=raiz(ARRIBA + 1.0, -0.8), torso=(24, 0, 0), cabeza=(16, 0, 0), capa=(2, 0, 0),
                 _mano_d=("h", 0.6, -4.6, 8.4), _arma=(0.0, -0.85, 0.5), _mano_i=("h", 3.4, -3.0, 7.0))
    accion("constelacion", 2.4, [
        (0.00, L),
        (0.35, P1),
        (0.60, P2),
        (0.85, P3),
        (1.00, APUNTA),
        (1.50, con(APUNTA, torso=(26, 0, 0))),
        (2.40, L),
    ], golpes=(1.00,))
    # ---- 5 parpadeo: se encoge y se apaga (el juego la lleva a otro sitio a los 0,5 s) y se abre al aparecer
    ENCOGE = con(BASE, raiz=raiz(ARRIBA - 1.0), torso=(28, 0, 0), cabeza=(28, 0, 0), vestido=(-10, 0, 0), capa=(30, 0, 0),
                 _mano_d=("h", -1.2, -5.0, 4.6), _arma=(0.0, 1.0, 0.25), _mano_i=("h", -2.0, -3.0, 5.0))
    ABRE = con(BASE, raiz=raiz(ARRIBA + 2.4), torso=(-10, 0, 0), cabeza=(-12, 0, 0), capa=(-6, 0, 0),
               _mano_d=("h", -4.0, -3.0, 4.6), _arma=(-0.2, 1.0, 0.1), _mano_i=("h", 6.0, -1.0, 4.0))
    accion("parpadeo", 1.2, [
        (0.00, L),
        (0.30, ENCOGE),
        (0.50, con(ENCOGE, raiz=raiz(ARRIBA - 1.4), torso=(34, 0, 0))),
        (0.65, ABRE),
        (1.20, L),
    ])
    # ---- 6 grito: abre los brazos (el baculo en alto a su derecha), la cabeza atras: grita
    GR = con(BASE, raiz=raiz(ARRIBA + 2.0, 0.8), torso=(-16, 0, 0), cabeza=(-36, 0, 0), capa=(26, 0, 0), vestido_bajo=(-6, 0, 0),
             _mano_d=("h", -6.4, 7.6, 3.0), _arma=(-0.35, 1.0, 0.1), brazo_i=(-30, 0, -84), antebrazo_i=(-12, 0, 0))
    accion("grito", 2.4, [
        (0.00, L),
        (0.40, con(GR, torso=(18, 0, 0), cabeza=(10, 0, 0), raiz=raiz(ARRIBA + 0.4))),
        (0.80, GR),
        (1.30, con(GR, torso=(-20, 0, 4), cabeza=(-40, 0, 6))),
        (1.85, con(GR, torso=(-14, 0, -4), cabeza=(-34, 0, -6))),
        (2.40, L),
    ])
    # ---- 7 alzarse: a media vida sube, los brazos abiertos y el baculo al cielo ("¡maestros!"), y vuelve a bajar
    ALTO = con(GR, raiz=raiz(ARRIBA + 9.0, 0.4), torso=(-12, 0, 0), cabeza=(-24, 0, 0), capa=(34, 0, 0), vestido=(-10, 0, 0),
               vestido_bajo=(-14, 0, 0), _mano_d=("h", -3.0, 9.6, 2.4), _arma=(-0.1, 1.0, 0.05), brazo_i=(-20, 0, -96), antebrazo_i=(-8, 0, 0))
    accion("alzarse", 3.0, [
        (0.00, L),
        (0.50, con(GR, raiz=raiz(ARRIBA + 2.4), torso=(10, 0, 0), cabeza=(6, 0, 0))),
        (1.20, ALTO),
        (1.50, con(ALTO, raiz=raiz(ARRIBA + 10.0, 0.4), torso=(-16, 0, 0), cabeza=(-30, 0, 0))),
        (2.20, con(ALTO, raiz=raiz(ARRIBA + 8.0, 0.4))),
        (3.00, L),
    ], golpes=(1.50,))
    # ---- 8 aturdido: se le va el cuerpo atras y tiembla, el baculo colgando
    AT = con(L, raiz=raiz(ARRIBA + 1.5, 2.0), torso=(-18, 10, 0), cabeza=(-20, -14, 0), capa=(20, 0, 0),
             _mano_d=("h", -3.0, -8.0, 3.0), _arma=(-0.2, 0.9, 0.3), brazo_i=(30, 0, -30), antebrazo_i=(-20, 0, 0))
    AT.pop("_mano_i", None)
    accion("aturdido", 1.4, [
        (0.00, L),
        (0.18, AT),
        (0.60, con(AT, raiz=raiz(ARRIBA + 1.0, 1.5), torso=(-12, -8, 0), cabeza=(-10, 16, 0))),
        (1.00, con(AT, raiz=raiz(ARRIBA + 0.6, 0.6), torso=(-4, 4, 0), cabeza=(0, -6, 0))),
        (1.40, L),
    ])
    # ---- 9 morir: el baculo se le cae a un lado, se lleva la izquierda al pecho y se hunde (el vestido se arruga), la cabeza
    # baja; al final mira arriba una vez mas (el juego la va deshaciendo en estrellas)
    HUNDE = con(BASE, raiz=raiz(ARRIBA - 7.0, 0.8), torso=(26, 0, 6), cabeza=(36, 0, 8), vestido=(-40, 0, 0), vestido_bajo=(-70, 0, 0),
                capa=(-10, 0, 0), _mano_d=("h", -6.0, -8.0, 2.0), _arma=(-0.7, 0.5, 0.4), _mano_i=("h", -2.0, -2.6, 4.4))
    accion("morir", 4.0, [
        (0.00, con(L, torso=(-8, 0, 6), cabeza=(-30, 0, 10), capa=(14, 0, 0))),
        (0.80, con(BASE, raiz=raiz(ARRIBA - 2.0, 0.6), torso=(10, 0, 0), cabeza=(-10, 0, 0), _mano_d=("h", -4.0, -7.0, 3.0),
                   _arma=(-0.4, 0.8, 0.3), _mano_i=("h", -2.0, -2.0, 4.6))),
        (1.80, HUNDE),
        (2.80, con(HUNDE, torso=(28, 0, 4), cabeza=(40, 0, 6))),
        (3.40, con(HUNDE, torso=(10, 0, 0), cabeza=(-30, 0, 0))),
        (4.00, con(HUNDE, torso=(8, 0, 0), cabeza=(-34, 0, 0))),
    ])
    # ---- 10 levantarse: estaba leyendo el cielo: baja la cabeza y la mano y se vuelve hacia quien ha entrado
    accion("levantarse", 1.6, [
        (0.00, CIELO),
        (0.60, con(CIELO, cabeza=(-6, 0, 0), torso=(0, 0, 0), _mano_i=("h", 3.0, 2.0, 6.0))),
        (1.10, con(L, raiz=raiz(ARRIBA + 1.6), torso=(0, -8, 0), cabeza=(-10, 6, 0))),
        (1.60, L),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_baculo"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["porte_baculo"]]
    if nombre == "quieto":
        return [anims["porte_baculo"]]
    return []
