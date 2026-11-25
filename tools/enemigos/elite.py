"""
El capitan de elite de la guardia de Valdrenn (parte 4c): uno al mando de cada grupo grande. Mas alto que los suyos
(x1,15), armadura ennegrecida con filos de oro, el gran yelmo cerrado con penacho rojo, capa larga y un mandoble a dos
manos. Al verte grita y los de su grupo van a por ti (y pegan mas fuerte un rato); si cae, los suyos se desaniman.
Golpes: tajo de arriba abajo que hace temblar el suelo, barrido ancho, estocada y embestida con el hombro.

Toma lo comun de la guardia (`guardia.py`: la piel, la malla, el oxido, la cara) y le pone lo suyo.
"""
import guardia as G
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, metal, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "elite"
VARIANTES = ("mandoble",)
ESCALA = 1.15

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r)
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, "coraza", grow=g(0.55))])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2, 8, 7, 4, "faldon", grow=g(0.45))])
    for n in ("d", "i"):
        # hombreras grandes, de tres laminas
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[
            Box(-3.0, -3.4, -2.9, 6.0, 3.6, 5.8, f"hombrera_{n}"),
            Box(-3.2, -1.2, -3.1, 6.4, 1.6, 6.2, f"lamina_{n}"),
        ])
    # el gran yelmo: cerrado, con una corona de picos y el penacho rojo
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8.5, -4, 8, 8.5, 8, "yelmo", grow=g(0.65)),
        Box(-4.8, -10.2, -4.8, 9.6, 1.4, 9.6, "corona"),
    ])
    r.pieza("penacho", "yelmo", (0, -10.2, 0), boxes=[
        Box(-0.7, -2.8, -3.4, 1.4, 2.8, 8.2, "penacho"),
        Box(-0.7, -1.4, 4.8, 1.4, 7.5, 1.4, "penacho_cola"),
    ])
    # la capa larga, desde los hombros hasta las corvas
    r.pieza("capa", "torso", (0, -11.6, 2.7), rot=(6, 0, 0), boxes=[Box(-5, 0, 0, 10, 20, 1, "capa")])
    # el mandoble (a dos manos: la derecha junto a la guarda, la izquierda en el pomo)
    r.pieza("mandoble", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -2.5, 1.2, 1.2, 6, "puno"),
        Box(-1.2, -1.2, 3.5, 2.4, 2.4, 1.6, "pomo"),
        Box(-4.5, -0.8, -3.6, 9, 1.6, 1.4, "guarda"),
        Box(-0.6, -1.4, -19.5, 1.2, 2.8, 16, "hoja"),
        Box(-0.6, -0.8, -21.5, 1.2, 1.6, 2, "hoja_punta"),
    ])
    r.empaquetar()
    return r


OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

NEGRO_A = ["#0d0e10", "#16181b", "#202327", "#2c3035", "#3b4046", "#50565d"]
ORO = ["#4a3c14", "#6b5820", "#8f7a34", "#b09848", "#d0b65e"]
ROJO = ["#2a0507", "#3f080c", "#560c12", "#701219", "#8c1a22"]


def materiales(variante):
    base = G.materiales(0)
    placa = G._oxidado(NEGRO_A[1:6], 421, cuanto=53)
    placa_b = G._oxidado(NEGRO_A[0:5], 422, cuanto=61)
    capa_t = tela(ROJO, 431, pliegue=3, grano=0.12, luz=0.25)

    def coraza(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return placa(cara, x, y, w, h) if cara == "arriba" else None
        if y >= 10:
            return None
        c = placa(cara, x, y, w, h)
        if cara == "frente":
            # el peto negro con la corona de oro del Rey
            corona = ["K.KK.K", "KKKKKK", "KKKKKK", ".KKKK."]
            if 1 <= y <= 4 and 1 <= x <= 6 and corona[y - 1][x - 1] == "K":
                return ORO[3] if (x + y) % 3 else ORO[2]
            if x in (0, 7):
                return ORO[1]
        if y == 9:
            return ORO[1]
        return c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # las escarcelas de laminas (negras, el canto de abajo de oro) y, delante y detras, la tira roja
        if cara in ("frente", "espalda") and 2 <= x <= 5:
            largo = (6, 5, 6, 5)[x - 2]
            return None if y > largo else oscuro(capa_t(cara, x, y + 4, w, h + 4), 0.05 * y)
        if y == h - 1:
            return ORO[1]
        c = placa_b(cara, x, y, w, h)
        return oscuro(c, 0.3) if y % 2 == 1 else c

    malla = G._malla(331)

    def antebrazo(cara, x, y, w, h):
        # el brazal negro con su filo de oro y el guantelete
        if y >= h - 2:
            return oscuro(placa_b(cara, x, y, w, h), 0.1) if cara != "abajo" else NEGRO_A[0]
        if y <= 0:
            return malla(cara, x, y, w, h)
        if y == 1:
            return ORO[1] if (x + y) % 2 else ORO[2]
        return placa(cara, x, y, w, h)

    cuero = ruido(G.CUERO, 352, grano=0.4)

    def pierna(cara, x, y, w, h):
        # la rodillera (con su filo de oro), la greba negra y la bota
        if y == 0:
            return ORO[2] if cara == "frente" else claro(placa(cara, x, y, w, h), 0.08)
        if y >= h - 1:
            return G.CUERO[0] if cara != "abajo" else G.CUERO[1]
        if cara in ("frente", "der", "izq"):
            return placa_b(cara, x, y, w, h)
        return cuero(cara, x, y, w, h)

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return ORO[2]
        if cara == "arriba" and x in (0, w - 1) and y % 2 == 0:
            return oscuro(c, 0.25)
        return c

    def lamina(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return ORO[1] if y == h - 1 else placa_b(cara, x, y, w, h)

    def yelmo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara == "frente":
            if y in (3, 4) and 1 <= x <= 6 and not (x in (3, 4) and y == 4):
                return None if y == 4 else oscuro(c, 0.4)     # la rendija en cruz
            if x in (3, 4):
                return ORO[2]                                  # la nariz de oro
            if y >= 6 and (x + y) % 2 == 0:
                return NEGRO_A[0]                              # los respiraderos
        if cara in ("der", "izq") and y == 4 and delante(cara, x, w) <= 1:
            return None
        if y == h - 1:
            return ORO[1]
        return c

    def corona(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= w - 2 else ORO[3]
        return ORO[4] if x % 3 == 0 else ORO[2]

    def penacho(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara in ("der", "izq") and y == 0 and (x * 3) % 4 == 0:
            return None                                        # la crin, despeinada por arriba
        return ROJO[2 + ((x * 2 + y) % 3)] if cara != "frente" else ROJO[1]

    def penacho_cola(cara, x, y, w, h):
        if cara in ("arriba",):
            return ROJO[3]
        if y >= h - 1 and x % 2 == 0:
            return None
        return ROJO[1 + ((x + y * 2) % 3)]

    def capa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 5 + 2) % 3)
        if y > largo:
            return None
        c = capa_t(cara, x, y, w, h)
        if y == 0:
            return ORO[2]
        return oscuro(c, 0.03 * y / 3) if cara == "espalda" else oscuro(c, 0.25)

    hoja = G._oxidado(G.HIERRO[3:6], 441, cuanto=71)

    def hoja_m(cara, x, y, w, h):
        c = hoja(cara, x, y, w, h)
        if cara in ("der", "izq") and y in (0, h - 1):
            return claro(c, 0.2)                               # los filos
        if cara in ("der", "izq") and y == h // 2:
            return oscuro(c, 0.25)                             # la acanaladura
        return c

    m = dict(base)
    m.update({
        "coraza": sombra(coraza, abajo=0.14), "faldon": sombra(faldon, abajo=0.1),
        "hombrera_d": sombra(hombrera, abajo=0.1), "hombrera_i": sombra(hombrera, abajo=0.1),
        "lamina_d": lamina, "lamina_i": lamina,
        "yelmo": sombra(yelmo, abajo=0.12), "corona": corona, "penacho": penacho, "penacho_cola": penacho_cola, "capa": capa,
        "puno": ruido(G.CUERO, 451, grano=0.4), "pomo": liso(ORO[3]), "guarda": metal(ORO[1:5], 452),
        "hoja": hoja_m, "hoja_punta": hoja_m,
        "antebrazo_d": sombra(antebrazo, abajo=0.14), "antebrazo_i": sombra(antebrazo, abajo=0.14),
        "pierna_d": sombra(pierna, abajo=0.18), "pierna_i": sombra(pierna, abajo=0.18),
    })
    return m


def brillo(variante):
    """Lo que brilla: los ojos tras la rendija del yelmo."""
    C = {"R": G.OJO, "r": G.OJO_B}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "........",
        ".R....R.",
    ], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno derecho respecto a su hombro; _agarra = la izquierda en el puno, a tantos pixeles
# detras de la derecha (el mandoble se lleva a dos manos)

BASE = dict(raiz=raiz(-0.2), torso=(1, 0, 0), cabeza=(2, 0, 0),
            muslo_d=(0, 0, 5), pierna_d=(2, 0, 0), muslo_i=(0, 0, -5), pierna_i=(2, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# esperando: el mandoble de punta en el suelo delante de el, las dos manos en el pomo
ESPERA = dict(_mano_d=("h", 5.4, -0.9, 5.0), _arma=(0.0, -1.0, 0.04), _agarra=2.8)
# en guardia: a dos manos, la punta a la cara del otro
GUARDIA = dict(raiz=raiz(-1.2), torso=(8, 22, 0), cabeza=(-4, -20, 0),
               muslo_d=(12, 0, 6), pierna_d=(16, 0, 0), muslo_i=(-22, 0, -6), pierna_i=(24, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", 3.0, -3.6, 6.4), _arma=(-0.1, 0.45, 1), _agarra=3.2)


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

    # ---- quieto: firme, apenas respira; gira la cabeza de vez en cuando
    bucle("quieto", 6.0, [
        (0.0, BASE),
        (2.0, con(BASE, raiz=raiz(-0.4), torso=(2, 3, 0), cabeza=(3, -14, 0))),
        (4.0, con(BASE, raiz=raiz(-0.2), torso=(1, -3, 0), cabeza=(2, 14, 0))),
        (6.0, BASE),
    ])
    # ---- andar: pasos largos y pesados
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-28, 22, 6, 18, 4, 0.0), (-2, 2, 4, 40, 0, 0.8), (22, -28, 18, 6, -4, 0.0), (2, -2, 40, 4, 0, 0.8), (-28, 22, 6, 18, 4, 0.0))):
        pasos.append((1.4 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (3, ty, -ty * 0.3), "cabeza": (0, -ty * 0.7, ty * 0.3), "raiz": raiz(up),
                                    "capa": (16 + 6 * up, ty * 0.5, 0)}))
    bucle("andar", 1.4, pasos)
    # ---- como lleva el mandoble (se suma; solo los brazos): de punta en el suelo
    q = con(BASE, ESPERA)
    bucle("porte_mandoble", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    # ---- en guardia
    bucle("listo_mandoble", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-1.6), torso=(10, 20, 0))),
        (2.0, LISTO),
    ])
    # ---- velando: de rodilla, el mandoble clavado delante (ante el trono)
    VELA = dict(raiz=raiz(-6.4, 0.6), torso=(14, 0, 0), cabeza=(24, 0, 0),
                muslo_d=(-4, 0, 4), pierna_d=(98, 0, 0), muslo_i=(-86, 0, -6), pierna_i=(84, 0, 0),
                _mano_d=("h", 3.0, -1.8, 7.6), _arma=(0.0, -1.0, 0.05), _agarra=4)
    bucle("velar", 4.0, [(0.0, VELA), (2.0, con(VELA, torso=(17, 0, 0), cabeza=(28, 0, 0))), (4.0, VELA)])
    # ---- tumbado (no lo usa, pero el modelo lo pide como los demas)
    TUMBADO = dict(raiz=raiz(2.2, 14.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 34, 0),
                   muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                   _mano_d=("h", -3.0, -0.6, 7.5), _arma=(-0.25, 0.0, 1), brazo_i=(-22, 0, -30), antebrazo_i=(-70, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (4.0, TUMBADO)])

    # ================================================================ acciones
    L = con(BASE, LISTO)
    # ---- el tajo: lo alza muy arriba (se ve venir: casi un segundo) y lo descarga; tiembla el suelo
    ALZA = con(L, raiz=raiz(-0.4, 1.4), torso=(-10, 8, 0), cabeza=(-6, -6, 0),
               _mano_d=("h", 4.5, 8.0, -0.6), _arma=(0.0, 0.55, -0.85), _agarra=3.2)
    CLAVA = con(L, raiz=raiz(-3.2, -3.0), torso=(32, 4, 0), cabeza=(-14, -2, 0),
                muslo_i=(-48, 0, -6), pierna_i=(46, 0, 0), muslo_d=(24, 0, 6), pierna_d=(10, 0, 0),
                _mano_d=("h", 2.4, -6.4, 6.4), _arma=(0.0, -0.75, 0.66), _agarra=3.2)
    accion("tajo", 2.0, [
        (0.00, L),
        (0.55, ALZA),
        (0.85, con(ALZA, raiz=raiz(-0.2, 1.8), torso=(-14, 8, 0), _mano_d=("h", 4.2, 8.4, -1.4))),
        (0.89, con(L, raiz=raiz(-0.8, 0.4), torso=(-4, 6, 0), _mano_d=("h", 4.6, 7.6, 3.0), _arma=(0.0, 0.95, 0.3), _agarra=3.2)),
        (0.92, con(L, raiz=raiz(-1.8, -1.4), torso=(14, 5, 0), _mano_d=("h", 4.0, 2.0, 8.0), _arma=(0.0, 0.25, 1), _agarra=3.2)),
        (0.95, CLAVA),
        (1.40, con(CLAVA, torso=(30, 4, 0))),
        (2.00, L),
    ], golpes=(0.95,))
    # ---- el barrido: de su derecha a su izquierda, ancho y bajo (se esquiva rodando hacia atras o por debajo)
    ATRAS = con(L, raiz=raiz(-1.6, 1.0), torso=(10, 60, 0), cabeza=(-6, -50, 0),
                _mano_d=("h", -3.4, -2.0, 0.6), _arma=(-0.85, 0.1, -0.5), _agarra=3.2)
    CRUZA = con(L, raiz=raiz(-2.2, -1.6), torso=(14, -32, 0), cabeza=(-8, 28, 0),
                muslo_i=(-34, 0, -6), pierna_i=(30, 0, 0),
                _mano_d=("h", 7.4, -3.0, 2.6), _arma=(0.92, -0.1, -0.3), _agarra=3.2)
    accion("barrido", 1.8, [
        (0.00, L),
        (0.55, ATRAS),
        (0.68, con(ATRAS, torso=(10, 64, 0))),
        (0.80, con(L, raiz=raiz(-2.0, -0.6), torso=(12, 6, 0), _mano_d=("h", 0.0, -2.6, 9.0), _arma=(0.1, 0.0, 1), _agarra=3.2)),
        (0.92, CRUZA),
        (1.25, con(CRUZA, torso=(14, -36, 0))),
        (1.80, L),
    ], golpes=(0.80,))
    # ---- la estocada a dos manos: se echa atras y a fondo
    RECOGE = con(L, raiz=raiz(-1.2, 2.2), torso=(4, 34, 0), cabeza=(-2, -30, 0),
                 _mano_d=("h", 1.6, -3.4, -1.6), _arma=(0.0, 0.08, 1), _agarra=3.2)
    FONDO = con(L, raiz=raiz(-2.8, -6.0), torso=(22, 8, 0), cabeza=(-16, -6, 0),
                muslo_i=(-56, 0, -6), pierna_i=(50, 0, 0), muslo_d=(28, 0, 6), pierna_d=(8, 0, 0),
                _mano_d=("h", 2.2, -1.6, 9.4), _arma=(0.0, 0.0, 1), _agarra=3.2)
    accion("estocada", 1.4, [
        (0.00, L),
        (0.45, RECOGE),
        (0.58, con(RECOGE, raiz=raiz(-1.3, 2.6))),
        (0.66, FONDO),
        (0.95, con(FONDO, raiz=raiz(-2.6, -5.6))),
        (1.40, L),
    ], golpes=(0.66,))
    # ---- la embestida: agacha el hombro y carga (el juego lo lanza hacia delante; pega en el choque)
    AGACHA = con(L, raiz=raiz(-2.4, 1.0), torso=(30, 40, 0), cabeza=(-24, -34, 0),
                 muslo_i=(-40, 0, -6), pierna_i=(48, 0, 0), _mano_d=("h", 2.0, -6.0, 2.0), _arma=(0.1, -0.4, 0.9), _agarra=3.2)
    CARGA = con(AGACHA, raiz=raiz(-1.8, -3.0), torso=(26, 44, 0), muslo_d=(-30, 0, 6), pierna_d=(30, 0, 0),
                muslo_i=(20, 0, -6), pierna_i=(10, 0, 0))
    accion("embestida", 1.6, [
        (0.00, L),
        (0.40, AGACHA),
        (0.55, CARGA),
        (0.70, con(AGACHA, raiz=raiz(-1.8, -3.0), muslo_d=(24, 0, 6), muslo_i=(-30, 0, -6))),
        (0.90, CARGA),
        (1.15, con(L, raiz=raiz(-1.8, -2.0), torso=(18, 20, 0))),
        (1.60, L),
    ], golpes=(0.70,))
    # ---- el grito: alza el mandoble y llama a los suyos (pegan mas un rato)
    GRITA = con(L, raiz=raiz(0.4), torso=(-14, 0, 0), cabeza=(-26, 0, 0),
                _mano_d=("h", 0.0, 9.4, 1.6), _arma=(0.05, 1.0, 0.05), brazo_i=(-140, 0, -20), antebrazo_i=(-10, 0, 0))
    accion("grito", 1.8, [
        (0.00, L),
        (0.45, sin(GRITA, "_agarra")),
        (1.10, con(sin(GRITA, "_agarra"), raiz=raiz(0.6), cabeza=(-30, 0, 0))),
        (1.80, L),
    ])
    # ---- aturdido
    accion("aturdido", 1.6, [
        (0.00, L),
        (0.18, con(L, raiz=raiz(-2.2, 2.0), torso=(-16, 26, 0), cabeza=(-20, -10, 0), muslo_i=(-10, 0, -6))),
        (0.80, con(L, raiz=raiz(-2.6, 1.6), torso=(-10, 18, 0), cabeza=(-6, 10, 0))),
        (1.20, con(L, raiz=raiz(-1.8, 0.8), torso=(0, 20, 0), cabeza=(-4, -10, 0))),
        (1.60, L),
    ])
    # ---- morir: cae de rodillas apoyado en el mandoble y se desploma de lado
    accion("morir", 2.6, [
        (0.00, L),
        (0.50, con(VELA, raiz=raiz(-5.0, 0.6), torso=(24, 0, 0), cabeza=(36, 0, 0))),
        (1.30, con(VELA, raiz=raiz(-6.4, 0.6), torso=(30, 0, 10), cabeza=(44, 0, 0))),
        (2.00, con(TUMBADO, raiz=raiz(2.2, 10.0, rot=(80, 0, 20)))),
        (2.60, con(TUMBADO, raiz=raiz(2.2, 10.0, rot=(88, 0, 20)))),
    ])
    accion("levantarse", 1.6, [
        (0.00, TUMBADO),
        (0.60, con(VELA, raiz=raiz(-5.4, 1.0))),
        (1.10, con(L, raiz=raiz(-2.0, 0.4))),
        (1.60, L),
    ])
    accion("alzarse", 1.4, [
        (0.00, VELA),
        (0.60, con(VELA, raiz=raiz(-4.0, 0.6), torso=(10, 0, 0))),
        (1.40, L),
    ])
    return B, A


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_mandoble"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre in ("andar", "quieto"):
        return [anims["porte_mandoble"]]
    return []


TABLA_EXTRA = ()
