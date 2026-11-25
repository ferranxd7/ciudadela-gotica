"""
El espectro de la corte de Valdrenn (parte 4b): los nobles del Rey Corvin que no se fueron del castillo. Medio
transparentes (lo pone el dibujante del juego), sin piernas: el faldon acaba en jirones y flotan un palmo sobre el suelo.
Caras chupadas, cuencas hondas con una luz fria y la boca abierta. Dos variantes:

  0 cortesano: jubon, gorguera, gorra con pluma, media capa y un estoque fino. Estocada que llega lejos y tajo doble.
  1 dama: vestido de cuello alto, diadema y velo; garras largas. Zarpazo, y el lamento (un grito alrededor: frena y
    quita fuerzas; se ve venir: echa la cabeza atras y abre los brazos).

Los dos se desvanecen y aparecen al lado de su presa (la accion `desvanecerse`: el dibujante los apaga y los enciende).
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "espectro"
VARIANTES = ("estoque", "garras")
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 4), cabeza=(8, 8, 8), brazo=(3, 13, 3), pierna=(4, 12, 4), hombro=0.5)
    # el faldon (en dos trozos, para que ondee): de la cintura hasta un palmo del suelo, roto por abajo
    r.pieza("faldon", "cadera", (0, -0.5, 0), boxes=[Box(-4.6, 0, -2.6, 9.2, 7, 5.2, "faldon")])
    r.pieza("faldon_bajo", "faldon", (0, 6.5, 0), boxes=[Box(-5.6, 0, -3.3, 11.2, 7, 6.6, "faldon_bajo")])
    # las mangas anchas que cuelgan de los antebrazos
    for n in ("d", "i"):
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 2.5, 0), boxes=[Box(-2.3, 0, -2.3, 4.6, 4.2, 4.6, f"manga_{n}")])
    # ---- 0, el cortesano: gorguera, gorra con pluma, media capa y el estoque
    r.pieza("gorguera", "torso", (0, -12, 0), boxes=[Box(-5, -1.6, -3.6, 10, 2.2, 7.2, "gorguera")])
    r.pieza("gorra", "cabeza", (0, -8, 0), rot=(-4, 0, 6), boxes=[
        Box(-4.6, -1.6, -4.6, 9.2, 2, 9.2, "gorra"),
        Box(2.7, -2.9, -0.6, 0.8, 1.8, 7.2, "pluma"),
    ])
    r.pieza("capa", "torso", (2.0, -12, 2.4), rot=(6, 0, -4), boxes=[Box(-4.0, 0, 0, 6.5, 14, 1, "capa")])
    r.pieza("estoque", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -1.5, 1, 1, 3.5, "puno"),
        Box(-0.8, -0.8, 2.0, 1.6, 1.6, 1.2, "pomo"),
        Box(-1.8, -1.8, -2.6, 3.6, 3.6, 1.1, "taza"),
        Box(-2.6, -0.3, -2.3, 5.2, 0.6, 0.6, "gavilan"),
        Box(-0.3, -0.3, -18.5, 0.6, 0.6, 16, "hoja_fina"),
    ])
    # ---- 1, la dama: diadema con velo, cuello alto y garras
    r.pieza("toca", "cabeza", (0, -8, 0), boxes=[
        Box(-4.4, -0.6, -4.4, 8.8, 1.3, 8.8, "diadema"),
        Box(-4.3, -0.4, 3.9, 8.6, 15, 0.8, "velo"),
    ])
    r.pieza("cuello_alto", "torso", (0, -12, 0), boxes=[Box(-4.6, -2.4, -2.4, 9.2, 3, 4.8, "cuello_alto")])
    for n in ("d", "i"):
        r.pieza(f"garra_{n}", f"mano_{n}", (0, 0, 0), boxes=[Box(-1.4, -0.2, -1.4, 2.8, 4.6, 2.8, f"garras_{n}")])
    r.empaquetar()
    return r


PIERNAS = ["muslo_d", "muslo_i", "pierna_d", "pierna_i"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: PIERNAS + ["toca", "cuello_alto", "garra_d", "garra_i"],
    1: PIERNAS + ["gorguera", "gorra", "capa", "estoque"],
}

# ---------------------------------------------------------------- las texturas
# El juego los dibuja medio transparentes y algo azulados: aqui van con su color, de gris azulado a casi blanco.

ALMA = ["#1b2430", "#2a3747", "#3d4f63", "#56708a", "#7b95ad", "#a6bdd0", "#d4e3ee"]
TELA_A = ["#141c27", "#1e2a3a", "#2a3a4f", "#3a4e66", "#4e6680"]     # el jubon del cortesano
TELA_B = ["#1a1626", "#272137", "#352d4a", "#463c5f", "#5b4f78"]     # el vestido de la dama (algo violeta)
BLANCO = ["#8fa6b8", "#b4c8d6", "#d6e4ee", "#eef6fb"]
HUECO = "#06090e"
OJO = "#8ff0ff"
ORO = ["#5c5a3f", "#8a8758", "#b8b47a"]


def materiales(variante):
    alma = ruido(ALMA[3:7], 711, grano=0.4, base=0.55)
    jubon = tela(TELA_A, 721, pliegue=3, grano=0.12)
    vestido = tela(TELA_B, 722, pliegue=4, grano=0.12, luz=0.3)
    ropa = jubon if variante == 0 else vestido
    blanco = ruido(BLANCO, 731, grano=0.35)

    def cabeza(cara, x, y, w, h):
        c = alma(cara, x, y, w, h)
        if cara == "frente":
            C = {"K": HUECO, "s": ALMA[2], "o": ALMA[3], "b": "#0b0f16", "c": ALMA[5]}
            cara_ = dibujo([
                "occccco.",
                "oscccsco",
                "osKccKso",
                "oKKccKKo",
                "osscccso",
                "oscbbcso",
                "ocsbbsco",
                "oocsscoo",
            ], C)
            v = cara_(cara, x, y, w, h)
            return v if v is not None else c
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if d <= 2 and y in (4, 5, 6):
                return oscuro(c, 0.35)                          # la mejilla hundida
        if cara == "arriba" or (cara in ("der", "izq", "espalda") and y <= 2):
            # el pelo, lacio y ralo (el cortesano lo lleva corto; la dama, recogido bajo el velo)
            return oscuro(mezcla(c, ALMA[1], 0.6), 0.1 * ((x + y) % 3))
        return c

    def torso(cara, x, y, w, h):
        c = ropa(cara, x, y, w, h)
        if cara == "frente":
            if variante == 0:
                if x in (3, 4):
                    return ORO[1] if y % 2 == 0 else oscuro(c, 0.1)     # los botones del jubon
                if y >= 9:
                    return oscuro(c, 0.25)                               # el cinto
            else:
                # el corpino: un peto en pico con lazada
                if abs(x - 3.5) < (12 - y) * 0.35 and y <= 9:
                    return claro(c, 0.18) if (x + y) % 2 == 0 else c
                if y >= 10:
                    return oscuro(c, 0.2)
        if cara == "abajo":
            return None
        return c

    def brazo(cara, x, y, w, h):
        c = ropa(cara, x, y, w, h)
        if variante == 0 and cara in ("frente", "der", "izq", "espalda") and y <= 3:
            return claro(c, 0.12) if (x + y) % 2 == 0 else oscuro(c, 0.1)   # la manga acuchillada
        return c

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return alma(cara, x, y, w, h)                       # la muneca, al aire
        return ropa(cara, x, y, w, h)

    def manga(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return None                                         # hueca: por dentro sale la mano
        # el borde de abajo, deshilachado
        if y == h - 1 and (x * 5 + 1) % 3 == 0:
            return None
        c = ropa(cara, x, y, w, h)
        return claro(c, 0.1) if y == h - 2 else c

    def faldon(cara, x, y, w, h):
        if cara == "arriba":
            return None
        if cara == "abajo":
            return None
        c = ropa(cara, x, y, w, h)
        return oscuro(c, 0.05 * y / max(1, h - 1))

    def faldon_bajo(cara, x, y, w, h):
        # se deshace en jirones: cada columna acaba a una altura, y cada vez mas oscuro y con huecos
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 3) % 4) - (1 if cara in ("der", "izq") else 0)
        if y > largo:
            return None
        if y >= h - 3 and (x * 3 + y) % 4 == 0:
            return None
        c = ropa(cara, x, y, w, h)
        return oscuro(c, 0.08 + 0.1 * y / max(1, h - 1))

    def gorguera(cara, x, y, w, h):
        # los pliegues de la gorguera: blanco y gris a rayas
        c = blanco(cara, x, y, w, h)
        if cara in ("frente", "espalda", "der", "izq"):
            return c if x % 2 == 0 else oscuro(c, 0.25)
        if cara in ("arriba", "abajo"):
            return c if (x + y) % 2 == 0 else oscuro(c, 0.2)
        return c

    def gorra(cara, x, y, w, h):
        c = ruido(TELA_A[:4], 741, grano=0.3)(cara, x, y, w, h)
        if cara != "arriba" and cara != "abajo" and y == h - 1:
            return ORO[0]
        return c

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 5 + 2) % 3)
        if y > largo:
            return None
        return oscuro(jubon(cara, x, y, w, h), 0.05 * (y / 3))

    def velo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 2) % 4)
        if y > largo or (x * 5 + y * 3) % 17 == 0:
            return None
        return mezcla(blanco(cara, x, y, w, h), ALMA[3], 0.3 + 0.03 * y)

    def diadema(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= w - 2 else ORO[1]
        if cara == "frente" and x in (3, 4):
            return "#9fd7e8"                                    # una piedra palida
        return ORO[2] if (x % 3 == 0) else ORO[1]

    def cuello_alto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = blanco(cara, x, y, w, h)
        return c if y > 0 else claro(c, 0.15)

    def garras(cara, x, y, w, h):
        c = alma(cara, x, y, w, h)
        if y >= h - 2 and cara in ("frente", "der", "izq", "espalda"):
            return oscuro(c, 0.45) if x % 2 == 0 else None          # las unas, finas y separadas
        if cara == "abajo":
            return None
        return c

    return {
        "cabeza": sombra(cabeza, abajo=0.12), "torso": sombra(torso, abajo=0.2),
        "brazo_d": brazo, "brazo_i": brazo, "antebrazo_d": antebrazo, "antebrazo_i": antebrazo,
        "muslo_d": liso(ALMA[2]), "muslo_i": liso(ALMA[2]), "pierna_d": liso(ALMA[2]), "pierna_i": liso(ALMA[2]),
        "manga_d": manga, "manga_i": manga, "faldon": faldon, "faldon_bajo": faldon_bajo,
        "gorguera": gorguera, "gorra": gorra, "pluma": liso(BLANCO[2]), "capa": capa,
        "puno": liso(TELA_A[2]), "pomo": liso(ORO[1]), "taza": sombra(liso(BLANCO[0]), abajo=0.2), "gavilan": liso(BLANCO[1]),
        "hoja_fina": por_cara(liso(BLANCO[2]), arriba=liso(BLANCO[3])),
        "diadema": diadema, "velo": velo, "cuello_alto": cuello_alto, "garras_d": garras, "garras_i": garras,
    }


def brillo(variante):
    """Lo que brilla: la luz fria de las cuencas, y el filo del estoque."""
    C = {"R": OJO}
    ojos = dibujo([
        "........",
        "........",
        "..R..R..",
        "........",
    ], C)
    b = {"cabeza": por_cara(liso(None), frente=ojos)}
    if variante == 0:
        b["hoja_fina"] = lambda cara, x, y, w, h: "#9ad9ee" if (y + x) % 5 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide ~10)

ARRIBA = 3.2   # lo que flota sobre el suelo (pixeles)
BASE = dict(raiz=raiz(ARRIBA), torso=(4, 0, 0), cabeza=(6, 0, 0), faldon=(4, 0, 0), faldon_bajo=(6, 0, 0),
            brazo_d=(-6, 0, 4), antebrazo_d=(-10, 0, 0), brazo_i=(-6, 0, -4), antebrazo_i=(-10, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# como lleva el cortesano el estoque cuando no pasa nada: la punta abajo y adelante; la otra mano a la espalda
PORTE_E = dict(_mano_d=("h", -1.0, -8.6, 2.6), _arma=(0.05, -0.75, 0.66), brazo_i=(16, 0, -10), antebrazo_i=(-70, 0, 0))
# la dama: las manos juntas por delante
PORTE_G = dict(_mano_d=("h", 3.4, -7.4, 4.0), _mano_i=("h", -3.4, -7.4, 4.0))
# en guardia el cortesano: de perfil (el hombro derecho delante), el estoque a la altura del pecho, apuntando
LISTO_E = dict(raiz=raiz(ARRIBA + 0.4), torso=(6, -34, 0), cabeza=(-4, 30, 0), faldon=(6, 0, 0), faldon_bajo=(10, 0, 0),
               _mano_d=("h", 1.0, -1.6, 8.6), _arma=(0.25, 0.1, 1), brazo_i=(30, 0, -20), antebrazo_i=(-60, 0, 0))
# la dama: inclinada, las garras abiertas por delante
LISTO_G = dict(raiz=raiz(ARRIBA + 0.8), torso=(16, 0, 0), cabeza=(-8, 0, 0), faldon=(8, 0, 0), faldon_bajo=(12, 0, 0),
               _mano_d=("h", 2.0, -1.0, 8.0), _mano_i=("h", -2.0, -1.4, 7.6))


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

    # ---- flotar (quieto): sube y baja despacio, el faldon ondea, la cabeza se ladea
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.0, con(BASE, raiz=raiz(ARRIBA + 0.8), torso=(3, 4, 2), cabeza=(4, -14, 6), faldon=(-2, 0, 3), faldon_bajo=(10, 0, -4))),
        (2.0, con(BASE, raiz=raiz(ARRIBA + 1.3), torso=(5, 0, 0), cabeza=(8, 0, 0), faldon=(6, 0, 0), faldon_bajo=(2, 0, 0))),
        (3.0, con(BASE, raiz=raiz(ARRIBA + 0.6), torso=(3, -4, -2), cabeza=(4, 14, -6), faldon=(-2, 0, -3), faldon_bajo=(10, 0, 4))),
        (4.0, BASE),
    ])
    # ---- andar: se desliza inclinado, el faldon y las mangas por detras
    DESLIZA = con(BASE, raiz=raiz(ARRIBA + 1.2), torso=(14, 0, 0), cabeza=(-8, 0, 0), faldon=(22, 0, 0), faldon_bajo=(18, 0, 0),
                  brazo_d=(22, 0, 8), antebrazo_d=(-6, 0, 0), brazo_i=(22, 0, -8), antebrazo_i=(-6, 0, 0))
    bucle("andar", 1.6, [
        (0.0, DESLIZA),
        (0.4, con(DESLIZA, raiz=raiz(ARRIBA + 1.6), faldon=(26, 0, 4), faldon_bajo=(24, 0, -6))),
        (0.8, con(DESLIZA, raiz=raiz(ARRIBA + 1.0), faldon=(20, 0, 0), faldon_bajo=(14, 0, 0))),
        (1.2, con(DESLIZA, raiz=raiz(ARRIBA + 1.6), faldon=(26, 0, -4), faldon_bajo=(24, 0, 6))),
        (1.6, DESLIZA),
    ])
    # ---- como lleva cada uno las manos (se suma; solo los brazos)
    for nombre, p in (("porte_estoque", PORTE_E), ("porte_garras", PORTE_G)):
        q = con(BASE, p)
        bucle(nombre, 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    # ---- en guardia
    for nombre, p in (("listo_estoque", LISTO_E), ("listo_garras", LISTO_G)):
        bucle(nombre, 2.0, [
            (0.0, p),
            (1.0, con(p, raiz=raiz(p["raiz"]["pos"][1] + 0.9), torso=(p["torso"][0] + 3, p["torso"][1], 0))),
            (2.0, p),
        ])
    # ---- velar: casi en el suelo, doblado sobre si mismo, las manos juntas (rezando ante un altar, un trono vacio)
    VELA = dict(raiz=raiz(0.6), torso=(34, 0, 0), cabeza=(34, 0, 0), faldon=(-30, 0, 0), faldon_bajo=(-40, 0, 0),
                _mano_d=("h", 3.6, -4.2, 6.4), _arma=(0.0, -1.0, 0.1), _mano_i=("h", -3.6, -4.4, 6.2))
    bucle("velar", 4.0, [
        (0.0, VELA),
        (2.0, con(VELA, raiz=raiz(1.0), torso=(37, 0, 0), cabeza=(38, 0, 0))),
        (4.0, VELA),
    ])
    # ---- tumbado: acurrucado en el suelo, la cara entre los brazos (asi espera en las emboscadas)
    ACURRUCA = dict(raiz=raiz(-6.0, -2.0), torso=(70, 0, 0), cabeza=(40, 0, 10), faldon=(-60, 0, 0), faldon_bajo=(-50, 0, 0),
                    _mano_d=("h", 3.0, -1.0, 6.0), _arma=(-0.3, -0.6, 0.7), _mano_i=("h", -3.0, -0.5, 6.0))
    bucle("tumbado", 4.0, [(0.0, ACURRUCA), (2.0, con(ACURRUCA, torso=(72, 0, 0))), (4.0, ACURRUCA)])

    # ================================================================ acciones
    E, G = con(BASE, LISTO_E), con(BASE, LISTO_G)
    # ---- el cortesano: la estocada (recoge el brazo, y a fondo deslizandose: llega lejos)
    RECOGE = con(E, raiz=raiz(ARRIBA + 1.4, 2.0), torso=(2, -44, 0), cabeza=(-2, 40, 0),
                 _mano_d=("h", -0.5, -0.6, 2.0), _arma=(0.1, 0.05, 1))
    FONDO = con(E, raiz=raiz(ARRIBA - 0.4, -6.0), torso=(18, -28, 0), cabeza=(-12, 24, 0), faldon=(26, 0, 0), faldon_bajo=(30, 0, 0),
                _mano_d=("h", 0.8, -0.2, 10.0), _arma=(0.05, 0.0, 1), brazo_i=(40, 0, -30))
    accion("estocada", 1.3, [
        (0.00, E),
        (0.40, RECOGE),
        (0.52, con(RECOGE, raiz=raiz(ARRIBA + 1.6, 2.4))),
        (0.60, FONDO),
        (0.85, con(FONDO, raiz=raiz(ARRIBA - 0.2, -5.6))),
        (1.30, E),
    ], golpes=(0.60,))
    # ---- el tajo doble: de su derecha a su izquierda y de vuelta, rapidos
    IDA = con(E, torso=(8, -60, 0), cabeza=(-6, 50, 0), _mano_d=("h", -6.4, 0.4, 3.0), _arma=(-0.9, 0.15, 0.4))
    VUELTA = con(E, torso=(10, 10, 0), cabeza=(-8, -8, 0), _mano_d=("h", 6.0, -0.6, 5.8), _arma=(0.95, 0.05, 0.3))
    accion("tajo_doble", 1.6, [
        (0.00, E),
        (0.32, IDA),
        (0.45, VUELTA),
        (0.62, con(VUELTA, torso=(10, 16, 0))),
        (0.80, IDA),
        (0.95, con(VUELTA, raiz=raiz(ARRIBA, -2.0))),
        (1.15, con(VUELTA, raiz=raiz(ARRIBA, -2.0), torso=(10, 20, 0))),
        (1.60, E),
    ], golpes=(0.45, 0.95))
    # ---- la dama: el zarpazo (las dos garras de arriba abajo, cruzadas)
    ALZA = con(G, raiz=raiz(ARRIBA + 2.0, 1.0), torso=(-6, 0, 0), cabeza=(-14, 0, 0),
               _mano_d=("h", -1.0, 7.0, 2.0), _mano_i=("h", 1.0, 7.0, 2.0))
    BAJA = con(G, raiz=raiz(ARRIBA - 0.6, -3.0), torso=(28, 0, 0), cabeza=(-4, 0, 0), faldon=(14, 0, 0),
               _mano_d=("h", 4.0, -7.0, 5.0), _mano_i=("h", -4.0, -7.0, 5.0))
    accion("zarpazo", 1.1, [
        (0.00, G),
        (0.32, ALZA),
        (0.40, con(ALZA, raiz=raiz(ARRIBA + 2.3, 1.2))),
        (0.48, BAJA),
        (0.70, con(BAJA, torso=(30, 0, 0))),
        (1.10, G),
    ], golpes=(0.48,))
    # ---- el lamento: echa la cabeza atras, abre los brazos y grita (el grito sale a los 0,9 s)
    TOMA = con(G, raiz=raiz(ARRIBA + 1.0), torso=(24, 0, 0), cabeza=(30, 0, 0),
               _mano_d=("h", 2.0, -3.0, 3.0), _mano_i=("h", -2.0, -3.0, 3.0))
    GRITA = con(G, raiz=raiz(ARRIBA + 3.6), torso=(-22, 0, 0), cabeza=(-36, 0, 0), faldon=(-10, 0, 0), faldon_bajo=(-8, 0, 0),
                _mano_d=("h", -8.0, 3.0, 2.0), _mano_i=("h", 8.0, 3.0, 2.0))
    accion("lamento", 2.0, [
        (0.00, G),
        (0.50, TOMA),
        (0.80, con(TOMA, raiz=raiz(ARRIBA + 0.6), torso=(28, 0, 0))),
        (0.90, GRITA),
        (1.40, con(GRITA, raiz=raiz(ARRIBA + 4.0), cabeza=(-40, 6, 0))),
        (2.00, G),
    ], golpes=(0.90,))
    # ---- desvanecerse: se encoge y se apaga (el juego lo lleva a otro sitio a los 0,6 s) y se abre al aparecer
    ENCOGE = con(BASE, raiz=raiz(ARRIBA - 1.0), torso=(30, 0, 0), cabeza=(30, 0, 0), faldon=(-10, 0, 0),
                 _mano_d=("h", 3.0, -2.0, 4.0), _mano_i=("h", -3.0, -2.0, 4.0))
    ABRE = con(BASE, raiz=raiz(ARRIBA + 2.0), torso=(-10, 0, 0), cabeza=(-12, 0, 0),
               _mano_d=("h", -7.0, -1.0, 4.0), _mano_i=("h", 7.0, -1.0, 4.0))
    accion("desvanecerse", 1.3, [
        (0.00, BASE),
        (0.40, ENCOGE),
        (0.60, con(ENCOGE, raiz=raiz(ARRIBA - 1.4), torso=(36, 0, 0))),
        (0.75, ABRE),
        (1.30, BASE),
    ])
    # ---- aturdido: se le va el cuerpo atras y tiembla
    accion("aturdido", 1.4, [
        (0.00, BASE),
        (0.15, con(BASE, raiz=raiz(ARRIBA + 1.5, 2.0), torso=(-18, 10, 0), cabeza=(-20, -14, 0), brazo_d=(30, 0, 30), brazo_i=(30, 0, -30))),
        (0.60, con(BASE, raiz=raiz(ARRIBA + 1.0, 1.5), torso=(-12, -8, 0), cabeza=(-10, 16, 0), brazo_d=(20, 0, 20), brazo_i=(20, 0, -20))),
        (1.00, con(BASE, raiz=raiz(ARRIBA + 0.6, 0.6), torso=(-4, 4, 0), cabeza=(0, -6, 0))),
        (1.40, BASE),
    ])
    # ---- morir: se arquea, alza los brazos y sube deshaciendose (el juego lo va apagando)
    accion("morir", 2.2, [
        (0.00, BASE),
        (0.40, con(BASE, raiz=raiz(ARRIBA + 1.0), torso=(-16, 0, 0), cabeza=(-30, 0, 0),
                   _mano_d=("h", -5.0, 4.0, 3.0), _mano_i=("h", 5.0, 4.0, 3.0))),
        (1.20, con(BASE, raiz=raiz(ARRIBA + 6.0), torso=(-24, 0, 0), cabeza=(-40, 0, 0), faldon=(-20, 0, 0), faldon_bajo=(-20, 0, 0),
                   _mano_d=("h", -3.0, 8.6, 1.0), _mano_i=("h", 3.0, 8.6, 1.0))),
        (2.20, con(BASE, raiz=raiz(ARRIBA + 12.0), torso=(-30, 0, 0), cabeza=(-44, 0, 0), faldon=(-30, 0, 0), faldon_bajo=(-30, 0, 0),
                   _mano_d=("h", -2.0, 9.0, 0.5), _mano_i=("h", 2.0, 9.0, 0.5))),
    ])
    # ---- levantarse (de acurrucado) y alzarse (de velar)
    accion("levantarse", 1.5, [
        (0.00, ACURRUCA),
        (0.50, con(ACURRUCA, raiz=raiz(-3.0, -1.0), torso=(50, 0, 0), cabeza=(10, 0, 0))),
        (1.00, con(BASE, raiz=raiz(ARRIBA + 1.5), torso=(-6, 0, 0), cabeza=(-14, 0, 0),
                   _mano_d=("h", -6.0, 0.0, 4.0), _mano_i=("h", 6.0, 0.0, 4.0))),
        (1.50, BASE),
    ])
    accion("alzarse", 1.2, [
        (0.00, VELA),
        (0.50, con(VELA, raiz=raiz(1.6), torso=(20, 0, 0), cabeza=(10, 0, 0))),
        (1.20, BASE),
    ])
    return B, A


def variante_de(nombre):
    if nombre in ("estocada", "tajo_doble") or nombre.endswith("estoque"):
        return 0
    if nombre in ("zarpazo", "lamento") or nombre.endswith("garras"):
        return 1
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["porte_estoque"]]
    if nombre == "quieto":
        return [anims["porte_estoque"]]
    return []


TABLA_EXTRA = ()
