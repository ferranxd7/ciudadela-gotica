"""
Pell, el Bufon del Rey (parte 7y; respuesta 132 del plan: "jefes secretos tras muros ilusorios"): el bufon del ultimo rey de
Morvain. La noche que el cielo se puso rojo la corte huyo y el se quedo en su teatro, bajo el escenario, con sus mascaras; la
marca de la Sombra le volvio de ilusiones. Flaco y largo, el capirote de tres puntas con sus cascabeles, la mascara partida
(blanca y negra, la sonrisa pintada), la gorguera, el traje de rombos rojos y negros con filetes de oro, el faldon de picos y
los zapatos de punta enroscada. Una daga en cada mano; en los malabares, tres bolas de fuego que giran delante de el.
"""
from anim import Anim, con, raiz
from modelo import Box, Rig, g, humanoide
from pinta import (Textura, claro, delante, dibujo, espejado, liso, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "bufon"
VARIANTES = ("pell",)
#: las bolas de los malabares solo se ven en los malabares (las ensena el modelo del juego)
OCULTAS = {0: ["malabar"]}
ESCALA = 1.1
CENTRO_Y = 1.1

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(7, 11, 4), cabeza=(8, 8, 8), brazo=(3, 12, 3), pierna=(3, 13, 3))
    # la mascara, un poco por delante de la cara (ojos, nariz y la sonrisa pintada)
    r.pieza("mascara", "cabeza", (0, 0, 0), boxes=[Box(-4, -7.5, -4.6, 8, 6, 1, "mascara")])
    # el capirote: la banda de oro, la copa y tres puntas de dos trozos con un cascabel en la punta
    r.pieza("gorro", "cabeza", (0, -8, 0), boxes=[
        Box(-4.3, -1.0, -4.3, 8.6, 1.5, 8.6, "gorro_banda", grow=g(0.1)),
        Box(-3.5, -4.0, -3.5, 7, 3, 7, "gorro_copa"),
    ])
    for n, x, rot1, rot2, mat in (("d", -2.0, (0, 0, 50), (0, 0, 66), "punta_roja"), ("i", 2.0, (0, 0, -50), (0, 0, -66), "punta_roja"),
                                  ("c", 0.0, (-40, 0, 0), (-72, 0, 0), "punta_negra")):
        z = 1.0 if n == "c" else 0.0
        r.pieza(f"punta_{n}1", "gorro", (x, -3.5, z), rot=rot1, boxes=[Box(-1.5, -6, -1.5, 3, 6, 3, f"{mat}1", uv=f"{mat}1")])
        r.pieza(f"punta_{n}2", f"punta_{n}1", (0, -6, 0), rot=rot2, boxes=[
            Box(-1, -6, -1, 2, 6, 2, f"{mat}2", uv=f"{mat}2"),
            Box(-1.2, -8.2, -1.2, 2.4, 2.4, 2.4, "cascabel", uv="cascabel"),
        ])
    # la gorguera (al cuello) y el faldon de picos (a la cadera)
    r.pieza("gorguera", "torso", (0, -11, 0), boxes=[Box(-5, -1.2, -3, 10, 2, 6, "gorguera", grow=g(0.2))])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -1.5, -2.5, 8, 4.5, 5, "faldon", grow=g(0.25))])
    # los zapatos de punta enroscada (con su cascabelito)
    for n in ("d", "i"):
        r.pieza(f"zapato_{n}", f"pierna_{n}", (0, 6.0, -1.5), rot=(-28, 0, 0), boxes=[
            Box(-1, -1, -3.5, 2, 1.5, 3.5, "zapato_punta", uv="zapato_punta"),
            Box(-0.8, -2.4, -4.3, 1.6, 1.6, 1.6, "cascabel_pie", uv="cascabel_pie"),
        ])
    # ---- las dagas: la hoja es un plano de canto a lo largo de -Z de la mano
    for n in ("d", "i"):
        r.pieza(f"daga_{n}", f"mano_{n}", (0, 0, 0), boxes=[
            Box(-0.5, -0.5, -1.5, 1, 1, 3.5, "mango", uv="mango"),
            Box(-1.5, -0.5, -2.5, 3, 1, 1, "guarda", uv="guarda"),
            Box(0, -1, -8.5, 0, 2, 6, "hoja", uv="hoja"),
        ])
    # ---- los malabares: tres bolas de fuego en un aro delante del pecho (el aro gira en la animacion)
    r.pieza("malabar", "torso", (0, -6.5, -7), boxes=[
        Box(-1.25, -7.25, -1.25, 2.5, 2.5, 2.5, "bola", uv="bola"),
        Box(4.95, 2.75, -1.25, 2.5, 2.5, 2.5, "bola", uv="bola"),
        Box(-7.45, 2.75, -1.25, 2.5, 2.5, 2.5, "bola", uv="bola"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas

ROJO = ["#2a0a10", "#4a1018", "#6e1824", "#932333", "#b8324a"]
NEGRO = ["#07070a", "#0f0f15", "#181822", "#232330", "#2f2f40"]
ORO = ["#3d3010", "#5c491b", "#7f6728", "#a58736", "#c9a94b", "#e8cc6a"]
BLANCO = ["#8f8a7e", "#aba596", "#c7c0ae", "#dcd5c1", "#eee8d5"]
PIEL = ["#4b4651", "#5f5966", "#746d7c", "#8a8292"]
HIERRO = ["#181b1d", "#2a2f32", "#40474b", "#5b6468", "#7c878c", "#a3aeb2", "#cfd6da"]
PALO = ["#241b13", "#33271b", "#443424", "#56422e"]
OJO, OJO_B = "#f4ffc4", "#c9e86a"
FUEGO = ["#5a1606", "#a3300b", "#e0601a", "#ff9a32", "#ffd27a", "#fff4cf"]


def rombos(a, b, semilla, lado=3, filete=None):
    """El traje de arlequin: rombos de dos colores (a y b, listas de tonos) con un filete de oro en las aristas."""
    ta, tb = ruido(a, semilla, grano=0.25), ruido(b, semilla + 1, grano=0.25)

    def f(cara, x, y, w, h):
        u, v = (x + y) // lado, (x - y + 1000) // lado
        borde = (x + y) % lado == 0 or (x - y + 1000) % lado == 0
        if filete and borde and (x * 7 + y * 3) % 2 == 0:
            return filete
        return ta(cara, x, y, w, h) if (u + v) % 2 == 0 else tb(cara, x, y, w, h)
    return f


def _mascara():
    # mitad blanca (la derecha del bufon) y mitad negra; los ojos (agujeros), la sonrisa roja que cruza las dos y una lagrima
    C = {"W": BLANCO[4], "w": BLANCO[3], "v": BLANCO[2], "N": NEGRO[3], "n": NEGRO[2], "k": "#020203", "r": ROJO[4], "R": ROJO[3],
         "t": BLANCO[4], "o": ORO[4]}
    return dibujo([
        "wWWWNNNn",
        "WkkWNkkN",
        "WWWvnNNN",
        "WrWWNNtN",
        "vRrrrrRn",
        "wwWWNNnn",
    ], C)


def materiales(variante):
    cuero = ruido(NEGRO[0:4], 301, grano=0.3)
    piel = ruido(PIEL, 302, grano=0.4)
    roja = tela(ROJO, 303, pliegue=3, grano=0.12)
    negra = tela(NEGRO[0:5], 304, pliegue=3, grano=0.12)
    traje = rombos(ROJO[2:5], NEGRO[1:4], 311, lado=3, filete=ORO[3])

    def cabeza(cara, x, y, w, h):
        # una capucha negra ceñida bajo el capirote (la cara la tapa la mascara); por detras, el pelo gris que asoma
        c = negra(cara, x, y, w, h)
        if cara == "frente":
            return PIEL[1] if y >= 6 else NEGRO[2]
        if cara == "espalda" and y >= 6 and (x + y) % 3 == 0:
            return PIEL[3]
        return c

    def torso(cara, x, y, w, h):
        c = traje(cara, x, y, w, h)
        if y in (8, 9):                                  # el cinto de oro con su hebilla
            return ORO[2 + (x % 2)] if cara != "frente" or abs(x - w // 2) > 1 else ORO[5]
        if cara == "frente" and x == w // 2 and y < 8 and y % 3 == 1:
            return ORO[4]                                # los botones
        return c

    def brazo(lado):
        base = roja if lado == "d" else negra

        def f(cara, x, y, w, h):
            return base(cara, x, y, w, h)
        return f

    def antebrazo(lado):
        base = roja if lado == "d" else negra

        def f(cara, x, y, w, h):
            if y >= h - 2:
                return ORO[3 + (x + y) % 2]               # el puno de oro
            return base(cara, x, y, w, h)
        return f

    def pierna(lado):
        base = negra if lado == "d" else roja

        def f(cara, x, y, w, h):
            if y >= h - 2:
                return cuero(cara, x, y, w, h)            # el zapato
            return base(cara, x, y, w, h)
        return f

    def muslo(lado):
        base = negra if lado == "d" else roja
        return base

    def punta(tonos, sem):
        t = tela(tonos, sem, pliegue=2, grano=0.1)

        def f(cara, x, y, w, h):
            if y == h - 1:
                return ORO[3]
            return t(cara, x, y, w, h)
        return f

    def gorguera(cara, x, y, w, h):
        # pliegues blancos en zigzag con su sombra
        if cara in ("arriba", "abajo"):
            return BLANCO[3] if (x + y) % 2 == 0 else BLANCO[1]
        return BLANCO[4] if (x + (y % 2)) % 2 == 0 else BLANCO[2]

    def faldon(cara, x, y, w, h):
        # picos rojos y negros (lo de debajo de cada pico, transparente) con su filete de oro
        if cara in ("arriba", "abajo"):
            return NEGRO[2]
        k = (x // 4) % 2
        if y > h - 1 - (0 if x % 4 in (1, 2) else 1):
            return None                                   # entre pico y pico, nada
        if y == 0:
            return ORO[3]
        return (ROJO[3] if k == 0 else NEGRO[3]) if (y + x) % 5 else (ROJO[2] if k == 0 else NEGRO[2])

    def mascara(cara, x, y, w, h):
        if cara == "frente":
            return _mascara()(cara, x, y, w, h)
        return BLANCO[2] if cara in ("der", "arriba") else NEGRO[2]

    def hoja(cara, x, y, w, h):
        # la hoja de la daga de canto: el filo claro abajo, la punta a la izquierda
        if x == 0 and y == 0:
            return None
        return HIERRO[6] if y == h - 1 else HIERRO[4] if (x + y) % 3 else HIERRO[5]

    def bola(cara, x, y, w, h):
        return FUEGO[2] if (x + y) % 2 else FUEGO[3]

    return {
        "cabeza": sombra(cabeza, abajo=0.12),
        "torso": sombra(torso, abajo=0.12),
        "brazo_d": sombra(brazo("d"), abajo=0.08), "brazo_i": sombra(brazo("i"), abajo=0.08),
        "antebrazo_d": sombra(antebrazo("d"), abajo=0.12), "antebrazo_i": sombra(antebrazo("i"), abajo=0.12),
        "muslo_d": sombra(muslo("d"), abajo=0.08), "muslo_i": sombra(muslo("i"), abajo=0.08),
        "pierna_d": sombra(pierna("d"), abajo=0.18), "pierna_i": sombra(pierna("i"), abajo=0.18),
        "mascara": mascara,
        "gorro_banda": metal(ORO[1:6], 321), "gorro_copa": sombra(rombos(ROJO[2:5], NEGRO[1:4], 322, lado=2), abajo=0.1),
        "punta_roja1": punta(ROJO, 323), "punta_roja2": punta(ROJO, 324),
        "punta_negra1": punta(NEGRO[0:5], 325), "punta_negra2": punta(NEGRO[0:5], 326),
        "cascabel": metal(ORO[2:6], 327), "cascabel_pie": metal(ORO[2:6], 328),
        "gorguera": gorguera, "faldon": faldon,
        "zapato_punta": cuero,
        "mango": ruido(PALO, 331, grano=0.3), "guarda": metal(ORO[1:5], 332),
        "hoja": por_cara(liso(None), izq=hoja, der=espejado(hoja)),
        "bola": bola,
    }


def brillo(variante):
    """Lo que brilla: los ojos tras la mascara y las bolas de fuego de los malabares."""
    C = {"E": OJO, "e": OJO_B}
    ojos = dibujo([
        "........",
        ".Ee..eE.",
    ], C)

    def bola(cara, x, y, w, h):
        cx, cy = abs(x - (w - 1) / 2.0), abs(y - (h - 1) / 2.0)
        return FUEGO[5] if cx + cy < 0.8 else FUEGO[4] if cx + cy < 1.6 else FUEGO[3]

    return {"mascara": por_cara(liso(None), frente=ojos), "bola": bola}


# ---------------------------------------------------------------- las animaciones

# de pie, juguetón: el peso en una pierna, la cabeza ladeada y las dagas bajas, apuntando delante
BASE = dict(raiz=raiz(-0.3), torso=(4, -10, 0), cabeza=(6, 12, -10),
            muslo_d=(-8, 0, 6), pierna_d=(10, 0, 0), muslo_i=(6, 0, -8), pierna_i=(6, 0, 0),
            _mano_d=("h", -1.5, -6.5, 4.5), _arma=(-0.2, -0.4, 1),
            _mano_i=("h", 1.5, -6.5, 4.5), _arma_i=(0.2, -0.4, 1))
# en guardia: agachado, las rodillas abiertas y las dos dagas delante, en alto
LISTO = dict(raiz=raiz(-1.8), torso=(14, 0, 0), cabeza=(-10, 0, 0),
             muslo_d=(-30, 0, 12), pierna_d=(42, 0, 0), muslo_i=(-18, 0, -12), pierna_i=(32, 0, 0),
             _mano_d=("h", -0.5, -3.0, 7.5), _arma=(0.1, 0.35, 1),
             _mano_i=("h", 0.5, -2.2, 7.2), _arma_i=(-0.1, 0.45, 1))


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

    # ---- quieto: da saltitos sin moverse del sitio, ladeando la cabeza de un lado a otro
    bucle("quieto", 2.4, [
        (0.0, BASE),
        (0.6, con(BASE, raiz=raiz(0.5), muslo_d=(-14, 0, 6), pierna_d=(18, 0, 0), cabeza=(2, 4, 8),
                  _mano_d=("h", -2.0, -5.8, 5.0), _mano_i=("h", 2.0, -7.0, 4.0))),
        (1.2, con(BASE, raiz=raiz(-0.4), torso=(4, 10, 0), cabeza=(8, -12, 10))),
        (1.8, con(BASE, raiz=raiz(0.5), muslo_i=(-10, 0, -8), pierna_i=(16, 0, 0), cabeza=(2, -4, -8),
                  _mano_d=("h", -2.0, -7.0, 4.0), _mano_i=("h", 2.0, -5.8, 5.0))),
        (2.4, BASE),
    ])
    # ---- andar: a saltitos, las rodillas altas
    pasos = []
    for k, (md, mi, pd, pi_, tz, up) in enumerate((
            (-40, 16, 50, 8, 4, 0.0), (-10, 4, 30, 40, 0, 1.4), (16, -40, 8, 50, -4, 0.0), (4, -10, 40, 30, 0, 1.4), (-40, 16, 50, 8, 4, 0.0))):
        pasos.append((0.9 * k / 4, {"muslo_d": (md, 0, 4), "muslo_i": (mi, 0, -4), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (8, tz * 2.0, tz * 0.6), "cabeza": (-2, -tz * 1.5, -tz), "raiz": raiz(up - 0.4),
                                    "_mano_d": ("h", -2.0, -5.0 + tz * 0.3, 4.0 - tz * 0.4), "_arma": (-0.2, -0.2, 1),
                                    "_mano_i": ("h", 2.0, -5.0 - tz * 0.3, 4.0 + tz * 0.4), "_arma_i": (0.2, -0.2, 1)}))
    bucle("andar", 0.9, pasos)
    bucle("listo", 1.2, [
        (0.0, LISTO),
        (0.6, con(LISTO, raiz=raiz(-1.2), torso=(12, 6, 0), cabeza=(-8, -6, 4), _mano_d=("h", -0.8, -2.4, 7.8), _mano_i=("h", 0.8, -2.8, 7.0))),
        (1.2, LISTO),
    ])

    # ---- la reverencia (al empezar la pelea; el golpe = el aplauso de nadie): abre los brazos y se inclina hasta el suelo
    R_ABRE = dict(raiz=raiz(0.2), torso=(-6, 0, 0), cabeza=(-12, 0, 0),
                  muslo_d=(-4, 0, 4), pierna_d=(4, 0, 0), muslo_i=(4, 0, -4), pierna_i=(4, 0, 0),
                  _mano_d=("h", -8.2, 1.0, 2.0), _arma=(-1, 0.3, 0.1), _mano_i=("h", 8.2, 1.0, 2.0), _arma_i=(1, 0.3, 0.1))
    R_BAJA = dict(raiz=raiz(-1.6, 0.6), torso=(62, 0, 0), cabeza=(10, 0, 0),
                  muslo_d=(-34, 0, 4), pierna_d=(40, 0, 0), muslo_i=(14, 0, -4), pierna_i=(8, 0, 0),
                  _mano_d=("h", 2.0, -7.0, 3.6), _arma=(0.6, -0.5, -0.3), _mano_i=("h", 6.0, 3.4, -5.0), _arma_i=(0.5, 0.6, -0.6))
    accion("reverencia", 2.6, [
        (0.00, LISTO),
        (0.50, R_ABRE),
        (0.90, con(R_ABRE, cabeza=(-18, 0, 0))),
        (1.20, R_BAJA),
        (1.90, con(R_BAJA, torso=(66, 0, 0), cabeza=(14, 0, 0))),
        (2.60, LISTO),
    ], golpes=(1.20,))
    # ---- el tajo doble: las dos dagas atras y cruzan una despues de otra
    T_ATRAS = con(LISTO, raiz=raiz(-1.4, 1.0), torso=(4, 30, 0), cabeza=(-6, -26, 0),
                  _mano_d=("h", -6.6, -0.6, 4.4), _arma=(-1, 0.2, 0.3), _mano_i=("h", 7.0, -1.6, 3.0), _arma_i=(1, 0.3, 0.2))
    T_DER = con(LISTO, raiz=raiz(-2.0, -2.0), torso=(20, -34, 0), cabeza=(-14, 30, 0),
                muslo_d=(-44, 0, 12), pierna_d=(46, 0, 0),
                _mano_d=("h", 5.4, -2.6, 6.4), _arma=(1, -0.1, 0.4), _mano_i=("h", 6.6, -2.0, 1.0), _arma_i=(1, 0.2, -0.2))
    T_IZQ = con(LISTO, raiz=raiz(-2.2, -3.0), torso=(22, 32, 0), cabeza=(-16, -28, 0),
                muslo_d=(-48, 0, 12), pierna_d=(50, 0, 0),
                _mano_d=("h", -6.0, -3.0, 2.0), _arma=(-1, -0.1, 0.0), _mano_i=("h", -4.6, -2.4, 6.6), _arma_i=(-1, -0.1, 0.5))
    accion("tajo", 1.1, [
        (0.00, LISTO),
        (0.30, T_ATRAS),
        (0.45, T_DER),
        (0.60, con(T_DER, torso=(20, -24, 0), _mano_i=("h", 6.8, -0.8, 2.6))),
        (0.72, T_IZQ),
        (0.90, con(T_IZQ, torso=(20, 26, 0))),
        (1.10, LISTO),
    ], golpes=(0.45, 0.72))
    # ---- la pirueta: los brazos abiertos con las dagas y dos vueltas enteras sobre una pierna
    P_ABRE = con(LISTO, raiz=raiz(-0.6), torso=(6, 0, 0), cabeza=(-6, 0, 0),
                 muslo_d=(-10, 0, 6), pierna_d=(20, 0, 0), muslo_i=(-46, 0, -10), pierna_i=(80, 0, 0),
                 _mano_d=("h", -8.6, -2.0, 2.4), _arma=(-0.6, 0.0, 1), _mano_i=("h", 8.6, -2.0, 2.4), _arma_i=(0.6, 0.0, 1))
    accion("pirueta", 1.4, [
        (0.00, LISTO),
        (0.20, P_ABRE),
        (0.45, con(P_ABRE, raiz=raiz(-0.2, rot=(0, 180, 0)))),
        (0.70, con(P_ABRE, raiz=raiz(0.2, rot=(0, 360, 0)))),
        (0.95, con(P_ABRE, raiz=raiz(-0.2, rot=(0, 540, 0)))),
        (1.15, con(P_ABRE, raiz=raiz(-0.6, rot=(0, 720, 0)))),
        (1.40, con(LISTO, raiz=raiz(-1.8, rot=(0, 720, 0)))),
    ], golpes=(0.55, 0.85))
    # ---- los cuchillos: la derecha atras y tres latigazos del brazo (sale un cuchillo en cada golpe)
    C_ATRAS = con(LISTO, raiz=raiz(-1.2, 0.8), torso=(2, 24, 0), cabeza=(-8, -22, 0),
                  _mano_d=("h", -3.0, 5.4, -2.6), _arma=(-0.2, 0.9, -0.4), _mano_i=("h", 3.6, -4.0, 6.0))
    C_TIRA = con(LISTO, raiz=raiz(-1.8, -1.2), torso=(16, -22, 0), cabeza=(-14, 18, 0),
                 muslo_d=(-40, 0, 12), pierna_d=(44, 0, 0),
                 _mano_d=("h", 0.0, -1.0, 9.0), _arma=(0.0, -0.3, 1), _mano_i=("h", 4.4, -5.0, 3.0))
    accion("cuchillos", 1.3, [
        (0.00, LISTO),
        (0.35, C_ATRAS),
        (0.55, C_TIRA),
        (0.62, con(C_ATRAS, raiz=raiz(-1.4, 0.4))),
        (0.70, C_TIRA),
        (0.77, con(C_ATRAS, raiz=raiz(-1.4, 0.4))),
        (0.85, con(C_TIRA, torso=(18, -26, 0))),
        (1.30, LISTO),
    ], golpes=(0.55, 0.70, 0.85))
    # ---- los malabares: las tres bolas de fuego giran delante del pecho (el aro, dos vueltas) y las tira una a una
    M1 = con(LISTO, raiz=raiz(-0.8), torso=(4, 0, 0), cabeza=(-14, 0, 0),
             muslo_d=(-14, 0, 8), pierna_d=(20, 0, 0), muslo_i=(-8, 0, -8), pierna_i=(14, 0, 0),
             _mano_d=("h", -2.4, -2.0, 6.6), _arma=(-0.2, -0.8, 0.5), _mano_i=("h", 2.4, -4.4, 6.0), _arma_i=(0.2, -0.8, 0.5))
    M2 = con(M1, _mano_d=("h", -2.4, -4.4, 6.0), _mano_i=("h", 2.4, -2.0, 6.6))
    M_TIRA = con(LISTO, raiz=raiz(-1.6, -1.0), torso=(14, -18, 0), cabeza=(-12, 14, 0),
                 _mano_d=("h", -0.6, 0.6, 8.8), _arma=(0.0, 0.3, 1), _mano_i=("h", 3.0, -4.0, 5.0))
    M_TIRA_I = con(LISTO, raiz=raiz(-1.6, -1.0), torso=(14, 18, 0), cabeza=(-12, -14, 0),
                   _mano_i=("h", 0.6, 0.6, 8.8), _arma_i=(0.0, 0.3, 1), _mano_d=("h", -3.0, -4.0, 5.0))
    poses_m = [(0.00, LISTO)]
    t, k = 0.30, 0
    while t < 1.55:
        poses_m.append((round(t, 2), con(M1 if k % 2 == 0 else M2, malabar=(0, 0, (t - 0.3) / 1.3 * 720))))
        t += 0.20
        k += 1
    poses_m += [(1.60, con(M_TIRA, raiz=raiz(-0.6, 1.0), _mano_d=("h", -1.0, 5.0, 4.0), malabar=(0, 0, 720))),
                (1.70, con(M_TIRA, malabar=(0, 0, 720))), (1.90, con(M_TIRA_I, malabar=(0, 0, 720))),
                (2.10, con(M_TIRA, malabar=(0, 0, 720))), (2.60, con(LISTO, malabar=(0, 0, 720)))]
    accion("malabares", 2.6, poses_m, golpes=(1.70, 1.90, 2.10))
    # ---- el humo: se agacha y estrella la bomba contra el suelo (el golpe); se queda agachado mientras se deshace en humo
    H_AGACHA = con(LISTO, raiz=raiz(-3.4), torso=(28, 0, 0), cabeza=(-4, 0, 0),
                   muslo_d=(-60, 0, 14), pierna_d=(100, 0, 0), muslo_i=(-50, 0, -14), pierna_i=(96, 0, 0),
                   _mano_d=("h", -1.4, 4.4, 4.0), _arma=(-0.2, 0.8, 0.4), _mano_i=("h", 3.0, -5.0, 4.0))
    H_SUELO = con(H_AGACHA, raiz=raiz(-4.0), torso=(40, 0, 0), _mano_d=("h", -0.6, -8.4, 3.6), _arma=(0.0, -1, 0.2))
    accion("humo", 0.9, [
        (0.00, LISTO),
        (0.30, H_AGACHA),
        (0.40, H_SUELO),
        (0.90, con(H_SUELO, raiz=raiz(-4.4))),
    ], golpes=(0.40,))
    # ---- aparecer: sale del humo de un salto con los brazos abiertos ("¡ta-chan!")
    accion("aparece", 0.7, [
        (0.00, con(H_SUELO, raiz=raiz(-4.4))),
        (0.25, con(R_ABRE, raiz=raiz(1.6), muslo_d=(-20, 0, 8), pierna_d=(30, 0, 0))),
        (0.70, LISTO),
    ])
    # ---- la voltereta hacia atras (el juego le aparta; el modelo da la vuelta en el aire)
    accion("voltereta", 0.9, [
        (0.00, LISTO),
        (0.15, con(LISTO, raiz=raiz(-3.0), torso=(24, 0, 0), muslo_d=(-56, 0, 12), pierna_d=(90, 0, 0),
                   muslo_i=(-50, 0, -12), pierna_i=(88, 0, 0))),
        (0.45, con(LISTO, raiz=raiz(7.0, rot=(-180, 0, 0)), torso=(-10, 0, 0), muslo_d=(-80, 0, 8), pierna_d=(110, 0, 0),
                   muslo_i=(-80, 0, -8), pierna_i=(110, 0, 0), _mano_d=("h", -3.0, -2.0, 6.0), _mano_i=("h", 3.0, -2.0, 6.0))),
        (0.75, con(LISTO, raiz=raiz(1.0, rot=(-340, 0, 0)), muslo_d=(-40, 0, 12), pierna_d=(50, 0, 0))),
        (0.90, con(LISTO, raiz=raiz(-1.8, rot=(-360, 0, 0)))),
    ])
    # ---- la ilusion: abre los brazos, da una vuelta y se parte en tres (el golpe = salen las copias)
    accion("ilusion", 1.6, [
        (0.00, LISTO),
        (0.30, R_ABRE),
        (0.60, con(R_ABRE, raiz=raiz(0.8, rot=(0, 180, 0)), cabeza=(-24, 0, 0))),
        (0.90, con(R_ABRE, raiz=raiz(0.4, rot=(0, 360, 0)), cabeza=(-28, 0, 0), _mano_d=("h", -8.0, 3.0, 1.0), _mano_i=("h", 8.0, 3.0, 1.0))),
        (1.20, con(R_ABRE, raiz=raiz(0.0, rot=(0, 360, 0)), cabeza=(-20, 0, 0))),
        (1.60, con(LISTO, raiz=raiz(-1.8, rot=(0, 360, 0)))),
    ], golpes=(0.90,))
    # ---- la risa: una mano a la tripa, la otra senalando, la cabeza atras y sacudiendose
    RI = dict(raiz=raiz(-0.4), torso=(-14, 0, 0), cabeza=(-30, 0, 0),
              muslo_d=(-6, 0, 6), pierna_d=(8, 0, 0), muslo_i=(4, 0, -6), pierna_i=(6, 0, 0),
              _mano_d=("h", 1.0, -6.4, 3.4), _arma=(0.8, -0.5, 0.2), _mano_i=("h", 1.2, -0.4, 9.0), _arma_i=(0.0, 0.1, 1))
    poses_r = [(0.00, LISTO), (0.30, RI)]
    for k in range(1, 6):
        poses_r.append((0.30 + k * 0.22, con(RI, torso=(-14 + (6 if k % 2 else -2), 0, 0), cabeza=(-30 + (10 if k % 2 else -4), 0, 0),
                                             raiz=raiz(-0.4 + (0.4 if k % 2 else 0.0)))))
    poses_r.append((1.80, LISTO))
    accion("risa", 1.8, poses_r)
    # ---- aturdido: le han roto la guardia; trastabilla con los brazos sueltos y los cascabeles sonando
    AT = dict(raiz=raiz(-1.0, 2.0), torso=(-18, 12, 10), cabeza=(-18, -10, 16),
              muslo_d=(-14, 0, 10), pierna_d=(26, 0, 0), muslo_i=(16, 0, -10), pierna_i=(18, 0, 0),
              _mano_d=("h", -6.0, -5.0, -1.0), _arma=(-0.7, -0.6, 0.3), _mano_i=("h", 6.0, -4.0, 1.0), _arma_i=(0.7, -0.6, 0.3))
    accion("aturdido", 1.4, [
        (0.00, con(AT, raiz=raiz(-0.6, 0.6), torso=(4, 0, 0), cabeza=(0, 0, 4))),
        (0.22, AT),
        (0.65, con(AT, raiz=raiz(-1.4, 2.6), torso=(-12, -10, -10), cabeza=(-10, 10, -14))),
        (1.00, con(AT, raiz=raiz(-1.2, 2.2), torso=(-16, 8, 8), cabeza=(-16, -8, 12))),
        (1.40, LISTO),
    ])
    # ---- morir: trastabilla, cae sentado, deja caer las dagas a los lados y se tumba boca arriba
    SENTADO = dict(raiz=raiz(-11.0, 1.0), torso=(-16, 0, 0), cabeza=(14, 0, 10),
                   muslo_d=(-84, 0, 14), pierna_d=(20, 0, 0), muslo_i=(-80, 0, -14), pierna_i=(26, 0, 0),
                   _mano_d=("h", -5.0, -8.0, 1.0), _arma=(-0.6, -0.8, 0.1), _mano_i=("h", 5.0, -8.0, 1.0), _arma_i=(0.6, -0.8, 0.1))
    TUMBADO = dict(raiz=raiz(-11.6, 3.0, rot=(-84, 0, 0)), torso=(0, 0, 0), cabeza=(-10, 24, 0),
                   muslo_d=(-10, 0, 12), pierna_d=(14, 0, 0), muslo_i=(-6, 0, -16), pierna_i=(30, 0, 0),
                   _mano_d=("h", -8.0, 2.0, 0.0), _arma=(-1, 0.0, 0.2), _mano_i=("h", 7.0, 3.0, 1.0), _arma_i=(1, 0.2, 0.1))
    accion("morir", 4.0, [
        (0.00, con(LISTO, raiz=raiz(-1.0, 1.0), torso=(-10, 0, 8), cabeza=(-16, 0, 10))),
        (0.50, con(AT, raiz=raiz(-2.0, 1.6))),
        (1.20, SENTADO),
        (2.00, con(SENTADO, torso=(-8, 0, 0), cabeza=(24, 0, 14))),
        (2.90, TUMBADO),
        (4.00, TUMBADO),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    if nombre == "andar":
        return [anims["quieto"]]
    return []
