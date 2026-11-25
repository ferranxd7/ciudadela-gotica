"""
Ser Brandt, el Muro del Rey (parte 4d): el guardian del salon del trono. Un caballero enorme (x1,5: tres bloques) en
armadura de placas de hierro negro con filos rojos, el gran yelmo con una corona de puas, un pavés (escudo torre) con la
corona del Rey en el brazo izquierdo y un martillo de guerra en la mano derecha. Espera de rodillas ante las puertas del
trono, apoyado en el martillo; se levanta cuando entra alguien.
Golpes: martillazo (lo alza y lo descarga: una onda por el suelo), golpe de lado, escudazo, embestida con el escudo
delante, el muro (se cubre del todo un momento) y, con media vida, el terremoto (salta y cae con todo).
"""
import guardia as G
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "brandt"
VARIANTES = ("martillo",)
ESCALA = 1.5

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(10, 12, 6), cabeza=(8, 8, 8), brazo=(5, 12, 5), pierna=(5, 12, 5))
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-5, -12, -3, 10, 12, 6, "coraza", grow=g(0.6))])
    r.pieza("gorjal", "torso", (0, 0, 0), boxes=[Box(-4.5, -13.6, -3.6, 9, 2.6, 7.2, "gorjal")])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-5, -0.5, -3, 10, 7, 6, "faldon", grow=g(0.5))])
    for n in ("d", "i"):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[
            Box(-3.7, -4.4, -3.5, 7.4, 4.8, 7.0, f"hombrera_{n}"),
            Box(-3.9, -1.6, -3.7, 7.8, 1.8, 7.4, f"lamina_{n}"),
        ])
    # el gran yelmo, cerrado, con su corona de puas
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8.6, -4, 8, 8.6, 8, "yelmo", grow=g(0.7)),
        Box(-4.9, -10.6, -4.9, 9.8, 1.8, 9.8, "corona"),
    ])
    for i, (x, z) in enumerate(((-3.6, -4.2), (0, -4.6), (3.6, -4.2), (-4.2, 0), (4.2, 0))):
        r.pieza(f"pua_{i}", "yelmo", (x, -10.6, z), boxes=[Box(-0.6, -2.2, -0.6, 1.2, 2.2, 1.2, "pua")])
    # el pavés: alto y ancho, por fuera del antebrazo izquierdo
    r.pieza("escudo", "antebrazo_i", (0, 0, 0), boxes=[
        Box(-6.5, 5.0, -10.0, 13, 1.6, 19, "escudo"),
        Box(-2.0, 6.6, -2.0, 4, 1.0, 4, "umbo"),
    ])
    # el martillo de guerra (apunta hacia delante, -Z, con el brazo caido)
    r.pieza("martillo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.7, -0.7, -15.0, 1.4, 1.4, 21, "mango"),
        Box(-1.0, -1.0, 6.0, 2.0, 2.0, 1.6, "pomo"),
        Box(-2.6, -3.2, -19.8, 5.2, 6.4, 4.8, "cabeza_martillo"),
        Box(-0.8, -0.8, -22.2, 1.6, 1.6, 2.4, "punta"),
        Box(-0.7, -6.2, -18.4, 1.4, 3.0, 2.0, "pico"),
    ])
    r.empaquetar()
    return r


OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

NEGRO = ["#0f1012", "#18191c", "#222428", "#2e3136", "#3d4148", "#52575f"]
ROJO = ["#2b0606", "#420a0b", "#5c0f10", "#7a1517", "#981d1f"]
ORO = ["#4a3c14", "#6b5820", "#8f7a34", "#b09848", "#d0b65e"]


def materiales(variante):
    base = G.materiales(0)
    placa = G._oxidado(NEGRO[1:6], 511, cuanto=59)
    placa_b = G._oxidado(NEGRO[0:5], 512, cuanto=47)
    malla = G._malla(531)
    tela_r = tela(ROJO, 541, pliegue=3, grano=0.12, luz=0.22)
    cuero = ruido(G.CUERO, 551, grano=0.4)

    def coraza(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return placa(cara, x, y, w, h) if cara == "arriba" else None
        if y >= 11:
            return None
        c = placa(cara, x, y, w, h)
        if cara == "frente":
            # la corona del Rey en el peto, de rojo, y la arista del medio
            corona = ["R.RR.R", "RRRRRR", "RRRRRR"]
            if 2 <= y <= 4 and 2 <= x <= 7 and corona[y - 2][x - 2] == "R":
                return ROJO[3] if (x + y) % 2 else ROJO[4]
            if x in (4, 5) and y >= 6:
                return claro(c, 0.1)
        if y == 10:
            return ROJO[2]
        if x in (0, w - 1) and cara in ("frente", "espalda"):
            return oscuro(c, 0.2)
        return c

    def gorjal(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa_b(cara, x, y, w, h)
        return ROJO[2] if y == h - 1 and cara != "arriba" else c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara in ("frente", "espalda") and 3 <= x <= 6:
            largo = (6, 5, 6, 5)[x - 3]
            return None if y > largo else oscuro(tela_r(cara, x, y + 3, w, h + 3), 0.05 * y)
        if y == h - 1:
            return ROJO[1]
        c = placa_b(cara, x, y, w, h)
        return oscuro(c, 0.3) if y % 2 == 1 else c

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return ROJO[2]
        if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)):
            return oscuro(c, 0.25)
        return c

    def lamina(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return ROJO[1] if y == h - 1 else placa_b(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return oscuro(placa_b(cara, x, y, w, h), 0.1) if cara != "abajo" else NEGRO[0]
        if y <= 0:
            return malla(cara, x, y, w, h)
        return ROJO[2] if y == 1 else placa(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        if y == 0:
            return claro(placa(cara, x, y, w, h), 0.08)
        if y >= h - 1:
            return G.CUERO[0] if cara != "abajo" else G.CUERO[1]
        if cara in ("frente", "der", "izq"):
            return placa_b(cara, x, y, w, h)
        return cuero(cara, x, y, w, h)

    def yelmo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara == "frente":
            if y == 4 and 1 <= x <= 6:
                return None                                   # la rendija
            if y in (3, 5) and 1 <= x <= 6:
                return oscuro(c, 0.35)
            if x in (3, 4) and y >= 5:
                return claro(c, 0.08)
            if y >= 6 and (x + y) % 2 == 0 and x not in (3, 4):
                return NEGRO[0]                               # los respiraderos
        if cara in ("der", "izq") and y == 4 and delante(cara, x, w) <= 1:
            return None
        if y == h - 1:
            return oscuro(c, 0.3)
        return c

    def corona(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= h - 2 else NEGRO[3]
        return ROJO[3] if y == 0 else NEGRO[2 + (x % 2)]

    def pua(cara, x, y, w, h):
        return NEGRO[4] if cara == "arriba" else NEGRO[3]

    def escudo(cara, x, y, w, h):
        # el paves: la cara de fuera es la de "abajo" (la placa esta de cara hacia donde apunta el antebrazo); negro con
        # el cerco de hierro, la corona del Rey roja en medio y golpes viejos. x de 0 a 12 (ancho), y de 0 a 18 (alto)
        if cara != "abajo":
            return NEGRO[3] if cara != "arriba" else madera(G.PALO, 561, "z")(cara, x, y, w, h)
        if x in (0, w - 1) or y in (0, h - 1):
            return NEGRO[4] if (x + y) % 3 else ROJO[1]
        c = placa_b(cara, x, y, w, h)
        corona = [
            "..R..R..R..",
            "..RR.R.RR..",
            "..RRRRRRR..",
            "..RRRRRRR..",
            "...RRRRR...",
        ]
        fy = (h - 1 - y) - 4
        if 0 <= fy < len(corona) and 0 <= x - 1 < 11 and corona[fy][x - 1] == "R":
            return ROJO[3] if (x + y) % 2 else ROJO[4]
        if 10 <= (h - 1 - y) <= 11 and 3 <= x <= 9:
            return ROJO[2]
        if (x * 5 + y * 7) % 23 == 0:
            return oscuro(c, 0.35)
        return c

    def mango(cara, x, y, w, h):
        if cara in ("frente", "espalda"):
            return NEGRO[2]
        return G.CUERO[2] if (y // 2) % 2 == 0 and 14 <= y <= 24 else NEGRO[2 + (y % 2)]

    m = dict(base)
    m.update({
        "coraza": sombra(coraza, abajo=0.14), "gorjal": gorjal, "faldon": sombra(faldon, abajo=0.1),
        "hombrera_d": sombra(hombrera, abajo=0.1), "hombrera_i": sombra(hombrera, abajo=0.1),
        "lamina_d": lamina, "lamina_i": lamina,
        "antebrazo_d": sombra(antebrazo, abajo=0.14), "antebrazo_i": sombra(antebrazo, abajo=0.14),
        "pierna_d": sombra(pierna, abajo=0.18), "pierna_i": sombra(pierna, abajo=0.18),
        "yelmo": sombra(yelmo, abajo=0.12), "corona": corona, "pua": pua,
        "escudo": escudo, "umbo": metal(NEGRO[2:6], 562),
        "mango": mango, "pomo": metal(NEGRO[2:6], 563), "cabeza_martillo": sombra(metal(NEGRO[1:6], 564), abajo=0.12),
        "punta": metal(NEGRO[3:6], 565), "pico": metal(NEGRO[3:6], 566),
        "brazo_d": sombra(malla, abajo=0.1), "brazo_i": sombra(malla, abajo=0.1),
        "muslo_d": sombra(malla, abajo=0.1), "muslo_i": sombra(malla, abajo=0.1),
        "torso": sombra(malla),
    })
    return m


def brillo(variante):
    """Lo que brilla: los ojos tras la rendija."""
    C = {"R": G.OJO, "r": G.OJO_B}
    ojos = dibujo(["........", "........", "........", "........", ".R....R."], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones

BASE = dict(raiz=raiz(-0.2), torso=(2, 0, 0), cabeza=(3, 0, 0),
            muslo_d=(0, 0, 6), pierna_d=(2, 0, 0), muslo_i=(0, 0, -6), pierna_i=(2, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# el pavés delante, plantado; el martillo colgando a su lado
ESCUDO_FRENTE = dict(brazo_i=(-12, 0, -6), antebrazo_i=(-78, 0, 0), mano_i=(0, 0, 0))
PORTE = con(dict(_mano_d=("h", -1.6, -8.2, 2.2), _arma=(-0.12, -0.55, 0.82)), ESCUDO_FRENTE)
GUARDIA = dict(raiz=raiz(-1.2), torso=(8, 14, 0), cabeza=(-4, -12, 0),
               muslo_d=(12, 0, 6), pierna_d=(16, 0, 0), muslo_i=(-20, 0, -6), pierna_i=(22, 0, 0))
# en guardia: el pavés arriba (los ojos por encima), el martillo atras, listo
LISTO = con(GUARDIA, _mano_d=("h", -2.4, -2.0, -1.6), _arma=(-0.1, 0.9, 0.4),
            brazo_i=(-34, 0, -4), antebrazo_i=(-62, 0, 0), mano_i=(0, 0, 0))


def animaciones(r=None):
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

    bucle("quieto", 6.0, [
        (0.0, BASE),
        (3.0, con(BASE, raiz=raiz(-0.5), torso=(4, 2, 0), cabeza=(5, -8, 0))),
        (6.0, BASE),
    ])
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-24, 18, 6, 14, 3, 0.0), (-2, 2, 4, 32, 0, 0.6), (18, -24, 14, 6, -3, 0.0), (2, -2, 32, 4, 0, 0.6), (-24, 18, 6, 14, 3, 0.0))):
        pasos.append((1.6 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, -ty * 0.3), "cabeza": (0, -ty * 0.6, ty * 0.3), "raiz": raiz(up)}))
    bucle("andar", 1.6, pasos)
    q = con(BASE, PORTE)
    bucle("porte_martillo", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    bucle("listo_martillo", 2.0, [(0.0, LISTO), (1.0, con(LISTO, raiz=raiz(-1.6), torso=(10, 12, 0))), (2.0, LISTO)])
    # de rodillas ante las puertas del trono, las dos manos sobre el martillo de pie
    VELA = dict(raiz=raiz(-6.4, 0.6), torso=(12, 0, 0), cabeza=(22, 0, 0),
                muslo_d=(-4, 0, 5), pierna_d=(98, 0, 0), muslo_i=(-86, 0, -6), pierna_i=(84, 0, 0),
                _mano_d=("h", 4.0, -2.2, 7.0), _arma=(0.0, -1.0, 0.06), brazo_i=(-30, 0, -10), antebrazo_i=(-56, 0, 0), mano_i=(0, 0, 0))
    bucle("velar", 4.0, [(0.0, VELA), (2.0, con(VELA, torso=(15, 0, 0), cabeza=(26, 0, 0))), (4.0, VELA)])
    TUMBADO = dict(raiz=raiz(2.6, 16.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 30, 0),
                   muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                   _mano_d=("h", -3.0, -0.6, 7.5), _arma=(-0.25, 0.0, 1), brazo_i=(-22, 0, -30), antebrazo_i=(-70, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (4.0, TUMBADO)])

    L = con(BASE, LISTO)
    # ---- el martillazo: lo alza por encima del hombro (casi un segundo) y lo descarga delante
    ALZA = con(L, raiz=raiz(-0.4, 1.4), torso=(-12, 18, 0), cabeza=(-6, -12, 0),
               _mano_d=("h", 0.4, 8.6, -1.2), _arma=(0.0, 0.5, -0.86))
    BAJA = con(L, raiz=raiz(-3.4, -3.0), torso=(30, 6, 0), cabeza=(-14, -4, 0),
               muslo_i=(-46, 0, -6), pierna_i=(44, 0, 0), muslo_d=(22, 0, 6), pierna_d=(10, 0, 0),
               _mano_d=("h", 1.6, -6.0, 7.0), _arma=(0.0, -0.85, 0.52))
    accion("martillazo", 2.2, [
        (0.00, L),
        (0.60, ALZA),
        (0.95, con(ALZA, raiz=raiz(-0.2, 1.8), torso=(-15, 18, 0), _mano_d=("h", 0.2, 8.9, -2.0))),
        (1.00, con(L, raiz=raiz(-1.0, 0.0), torso=(-2, 12, 0), _mano_d=("h", 0.6, 7.2, 3.6), _arma=(0.0, 0.9, 0.4))),
        (1.05, BAJA),
        (1.55, con(BAJA, torso=(28, 6, 0))),
        (2.20, L),
    ], golpes=(1.05,))
    # ---- el golpe de lado: de su derecha a su izquierda, rapido
    ATRAS = con(L, raiz=raiz(-1.2, 1.0), torso=(8, 48, 0), cabeza=(-4, -40, 0),
                _mano_d=("h", -6.0, 0.0, -2.4), _arma=(-0.95, 0.2, -0.2))
    CRUZA = con(L, raiz=raiz(-1.8, -1.6), torso=(12, -30, 0), cabeza=(-6, 24, 0), muslo_i=(-30, 0, -6), pierna_i=(28, 0, 0),
                _mano_d=("h", 6.0, -1.6, 5.0), _arma=(0.9, 0.0, 0.4))
    accion("golpe", 1.3, [
        (0.00, L),
        (0.42, ATRAS),
        (0.54, con(ATRAS, torso=(8, 52, 0))),
        (0.62, con(L, raiz=raiz(-1.6, -0.8), torso=(10, 8, 0), _mano_d=("h", 0.0, -1.2, 8.6), _arma=(0.1, 0.0, 1))),
        (0.70, CRUZA),
        (0.95, con(CRUZA, torso=(12, -34, 0))),
        (1.30, L),
    ], golpes=(0.62,))
    # ---- el escudazo: recoge el pavés y lo estampa con un paso
    RECOGE = con(L, raiz=raiz(-1.6, 1.6), torso=(4, -10, 0), brazo_i=(-6, 0, -10), antebrazo_i=(-100, 0, 0))
    EMPUJA = con(L, raiz=raiz(-2.0, -4.2), torso=(16, 30, 0), cabeza=(-8, -26, 0), muslo_i=(-46, 0, -6), pierna_i=(40, 0, 0),
                 brazo_i=(-78, 0, -2), antebrazo_i=(-14, 0, 0))
    accion("escudazo", 1.3, [
        (0.00, L),
        (0.40, RECOGE),
        (0.52, con(RECOGE, raiz=raiz(-1.7, 1.9))),
        (0.58, EMPUJA),
        (0.85, con(EMPUJA, raiz=raiz(-2.0, -4.0))),
        (1.30, L),
    ], golpes=(0.58,))
    # ---- la embestida: el pavés delante, agachado, y a correr (el juego lo lanza); pega en el choque
    AGACHA = con(L, raiz=raiz(-2.6, 1.0), torso=(26, 20, 0), cabeza=(-20, -16, 0), muslo_i=(-36, 0, -6), pierna_i=(44, 0, 0),
                 brazo_i=(-50, 0, -4), antebrazo_i=(-40, 0, 0))
    CORRE = con(AGACHA, raiz=raiz(-2.0, -2.0), muslo_d=(-30, 0, 6), pierna_d=(30, 0, 0), muslo_i=(20, 0, -6), pierna_i=(10, 0, 0))
    accion("embestida", 2.0, [
        (0.00, L),
        (0.45, AGACHA),
        (0.60, CORRE),
        (0.75, con(AGACHA, raiz=raiz(-2.0, -2.0), muslo_d=(22, 0, 6), muslo_i=(-30, 0, -6))),
        (0.90, CORRE),
        (1.05, con(AGACHA, raiz=raiz(-2.0, -2.0), muslo_d=(22, 0, 6), muslo_i=(-30, 0, -6))),
        (1.15, con(CORRE, raiz=raiz(-2.4, -3.0))),
        (1.55, L),
        (2.00, L),
    ], golpes=(1.15,))
    # ---- el muro: se cubre entero tras el pavés y aguanta
    MURO = con(L, raiz=raiz(-2.4, 0.4), torso=(12, 30, 0), cabeza=(-6, -24, 0), muslo_i=(-30, 0, -6), pierna_i=(36, 0, 0),
               brazo_i=(-44, 8, -2), antebrazo_i=(-52, 0, 0))
    accion("muro", 1.8, [
        (0.00, L),
        (0.25, MURO),
        (1.45, con(MURO, raiz=raiz(-2.5, 0.2))),
        (1.80, L),
    ])
    # ---- el terremoto: salta y cae con el martillo a dos manos (la onda llega lejos)
    SALTA = con(L, raiz=raiz(5.0, 1.0), torso=(-16, 0, 0), cabeza=(-10, 0, 0), muslo_d=(-40, 0, 6), pierna_d=(60, 0, 0),
                muslo_i=(-30, 0, -6), pierna_i=(50, 0, 0), _mano_d=("h", 2.6, 9.0, 0.0), _arma=(0.0, 0.3, -0.95))
    CAE = con(L, raiz=raiz(-4.2, -2.0), torso=(38, 0, 0), cabeza=(-18, 0, 0), muslo_d=(-50, 0, 8), pierna_d=(90, 0, 0),
              muslo_i=(-60, 0, -8), pierna_i=(96, 0, 0), _mano_d=("h", 3.0, -6.4, 7.0), _arma=(0.0, -0.9, 0.4))
    accion("terremoto", 2.6, [
        (0.00, L),
        (0.50, con(L, raiz=raiz(-3.0, 0.0), torso=(20, 0, 0), muslo_d=(-30, 0, 6), pierna_d=(60, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(60, 0, 0))),
        (0.85, SALTA),
        (1.20, con(SALTA, raiz=raiz(4.0, 1.0), _mano_d=("h", 2.4, 9.2, 1.0))),
        (1.30, CAE),
        (1.90, con(CAE, raiz=raiz(-4.0, -2.0))),
        (2.60, L),
    ], golpes=(1.30,))
    # ---- el rugido: al empezar la pelea y a media vida (alza el martillo y golpea el pavés)
    RUGE = con(L, raiz=raiz(0.2), torso=(-16, 0, 0), cabeza=(-24, 0, 0), _mano_d=("h", 0.0, 9.0, 1.6), _arma=(0.05, 1.0, 0.05),
               brazo_i=(-20, 0, -30), antebrazo_i=(-50, 0, 0))
    accion("rugido", 2.0, [
        (0.00, L),
        (0.45, RUGE),
        (1.20, con(RUGE, raiz=raiz(0.4), cabeza=(-28, 0, 0))),
        (2.00, L),
    ])
    accion("aturdido", 1.6, [
        (0.00, L),
        (0.20, con(L, raiz=raiz(-2.2, 2.2), torso=(-16, 22, 0), cabeza=(-18, -8, 0))),
        (0.90, con(L, raiz=raiz(-2.6, 1.8), torso=(-10, 14, 0), cabeza=(-6, 10, 0))),
        (1.60, L),
    ])
    accion("morir", 3.0, [
        (0.00, L),
        (0.60, con(VELA, raiz=raiz(-5.0, 0.6), torso=(22, 0, 0), cabeza=(34, 0, 0))),
        (1.60, con(VELA, raiz=raiz(-6.4, 0.6), torso=(30, 0, 12), cabeza=(44, 0, 0))),
        (2.40, con(TUMBADO, raiz=raiz(2.6, 12.0, rot=(80, 0, 16)))),
        (3.00, con(TUMBADO, raiz=raiz(2.6, 12.0, rot=(88, 0, 16)))),
    ])
    accion("levantarse", 2.0, [
        (0.00, VELA),
        (0.70, con(VELA, raiz=raiz(-5.0, 0.4), torso=(26, 0, 0), cabeza=(-10, 0, 0))),
        (1.40, con(L, raiz=raiz(-2.4, 0.0), torso=(20, 0, 0))),
        (2.00, L),
    ])
    accion("alzarse", 1.6, [
        (0.00, TUMBADO),
        (0.80, con(VELA, raiz=raiz(-5.4, 0.6))),
        (1.60, L),
    ])
    return B, A


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_martillo"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre in ("andar", "quieto"):
        return [anims["porte_martillo"]]
    return []


TABLA_EXTRA = ()
