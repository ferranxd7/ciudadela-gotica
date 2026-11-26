"""
El corcel de hueso y niebla (parte 7b, respuesta 129 del plan): la montura de Morvain, que acude al silbato que da la
guia. Un caballo de hueso palido (algo azulado), con las costillas al aire, una silla de cuero negro con remaches de oro
gastado y una manta roja; la crin y la cola son niebla de almas (solo brillan: capa de brillo, sin textura debajo), los
ojos dos llamitas azules y los cascos de hierro negro con fuego azul por abajo. Corre, salta dos veces, se encabrita al
llegar y, si lo hieren mucho, se deshace en niebla.

Las patas se llaman como las del perro (brazo / antebrazo / mano las de delante, muslo / pierna / pata las de atras).
Sin cinematica inversa: todo son giros.
"""
import numpy as np

from anim import Anim, con, raiz
from modelo import Box, Rig, g
from pinta import (claro, delante, dibujo, liso, metal, oscuro, por_cara, ruido, sombra)

NOMBRE = "corcel"
VARIANTES = ("hueso",)
ESCALA = 1.0
CENTRO_Y = 0.95

# ---------------------------------------------------------------- el modelo
# (pixeles; Y hacia abajo; el suelo en y = 24; el hocico mira a -Z; la derecha del caballo es -X)


def rig():
    r = Rig(NOMBRE, 128)
    r.pieza("raiz", "", (0, 24, 0))
    # la cadera (atras, a 20 del suelo): de ella salen el lomo hacia delante, las patas de atras y la cola
    r.pieza("cadera", "raiz", (0, -20, 9), boxes=[Box(-4.5, -2.5, -2, 9, 5, 7.5, "pelvis")])
    # el lomo: las costillas (con huecos), el espinazo por encima, la silla y la manta
    r.pieza("torso", "cadera", (0, -0.5, -2), boxes=[
        Box(-5, -2.5, -19, 10, 10, 18, "costillas"),
        Box(-1, -3.6, -21, 2, 2, 23, "espinazo"),
        Box(-5.3, -3.2, -12.5, 10.6, 6.5, 6, "manta", grow=g(0.1)),
        Box(-4.6, -4.9, -13, 9.2, 2, 7, "silla"),
        Box(-1.1, -6.4, -13.4, 2.2, 1.8, 1.6, "perilla"),
    ])
    # el cuello (sube hacia delante) con la crin de niebla encima, y la cabeza (larga, mirando algo abajo)
    r.pieza("cuello", "torso", (0, -1, -18), rot=(-52, 0, 0), boxes=[
        Box(-2, -2, -11, 4, 4.5, 12, "cuello"),
        Box(-0.7, -6.2, -11.5, 1.4, 4.4, 12.5, "crin"),
    ])
    r.pieza("cabeza", "cuello", (0, 0.2, -10.5), rot=(94, 0, 0), boxes=[
        Box(-2.6, -3, -5.5, 5.2, 5.5, 6, "craneo"),
        Box(-1.9, -2.3, -12.5, 3.8, 4.1, 7.2, "hocico"),
        Box(-2.2, -5.2, -1.6, 1.2, 2.6, 1.1, "oreja"),
        Box(1.0, -5.2, -1.6, 1.2, 2.6, 1.1, "oreja"),
        Box(-2.1, -1.0, -9.6, 4.2, 1.2, 1.2, "brida", grow=g(0.05)),
        Box(-0.6, -5.6, -3.0, 1.2, 3.2, 3.6, "copete"),
    ])
    r.pieza("mandibula", "cabeza", (0, 1.6, -5), rot=(4, 0, 0), boxes=[Box(-1.6, 0, -7.2, 3.2, 1.4, 7.6, "mandibula")])
    # las patas de delante: del hombro (a 14,5 del suelo) a la rodilla, la cana y el casco
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"brazo_{n}", "torso", (s * 3.4, 6, -16), boxes=[Box(-1.5, -2, -1.6, 3, 8.5, 3.2, f"brazo_{n}")])
        r.pieza(f"antebrazo_{n}", f"brazo_{n}", (0, 6.5, 0), boxes=[Box(-1, 0, -1, 2, 6, 2, f"antebrazo_{n}")])
        r.pieza(f"mano_{n}", f"antebrazo_{n}", (0, 6, 0), boxes=[
            Box(-0.9, 0, -0.9, 1.8, 1.2, 1.8, "menudillo"),
            Box(-1.6, 1.2, -1.9, 3.2, 1.3, 3.6, "casco"),
        ])
    # las de atras: el muslo hacia delante, la canilla hacia atras y el corvejon con el casco
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"muslo_{n}", "cadera", (s * 3.6, 1.5, 2.5), rot=(-20, 0, 0), boxes=[Box(-1.8, -2, -2, 3.6, 8.5, 4, f"muslo_{n}")])
        r.pieza(f"pierna_{n}", f"muslo_{n}", (0, 6.5, 0), rot=(45, 0, 0), boxes=[Box(-1.1, 0, -1.1, 2.2, 7, 2.2, f"pierna_{n}")])
        r.pieza(f"pata_{n}", f"pierna_{n}", (0, 7, 0), rot=(-25, 0, 0), boxes=[
            Box(-0.9, 0, -0.9, 1.8, 3.4, 1.8, f"tarso_{n}"),
            Box(-1.6, 3.4, -1.9, 3.2, 1.3, 3.6, "casco"),
        ])
    # la cola: el hueso corto y la niebla que cuelga
    r.pieza("cola", "cadera", (0, -2, 5), rot=(-35, 0, 0), boxes=[
        Box(-0.8, -0.8, 0, 1.6, 1.6, 4, "cola_hueso"),
        Box(-1.6, -1.6, 2.5, 3.2, 3.2, 6, "cola_niebla"),
    ])
    r.pieza("cola2", "cola", (0, 0, 8), rot=(-25, 0, 0), boxes=[Box(-1.3, -1.3, 0, 2.6, 2.6, 7, "cola_niebla2")])
    r.empaquetar()
    return r


OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

HUESO = ["#3e4048", "#5e6270", "#82879a", "#a3a8b8", "#c3c7d3", "#dfe2ea"]
HUECO = "#0b0c12"
CUERO = ["#141214", "#1e1a1c", "#2a2426", "#383032"]
ORO = ["#5a4214", "#86662a", "#b08a3c", "#d2b062"]
ROJO = ["#2e0a0c", "#481214", "#621a1c", "#7e2424"]
HIERRO = ["#16181c", "#262a30", "#3c4248", "#5a6268"]
ALMA = ["#3a7c9a", "#66b4d2", "#9ad8ee", "#d4f4ff"]


def materiales(variante):
    hueso = ruido(HUESO[2:6], 711, grano=0.4, base=0.55)
    hueso_b = ruido(HUESO[1:5], 712, grano=0.45, base=0.55)
    cuero = ruido(CUERO, 721, grano=0.5, base=0.5)
    manta_c = ruido(ROJO, 722, grano=0.45, base=0.55)
    hierro = metal(HIERRO, 731, grano=0.25)

    def costillas(cara, x, y, w, h):
        # las costillas: tiras de hueso de arriba abajo por los costados (una si y otra no), el esternon por abajo
        if cara == "arriba":
            return None
        if cara == "abajo":
            return hueso_b(cara, x, y, w, h) if x in (3, 4, 5, 6) else None
        if cara == "espalda":
            return None
        if cara == "frente":
            return hueso(cara, x, y, w, h) if (x in (0, w - 1) or y == 0 or y == h - 1) else None
        d = delante(cara, x, w)
        if d % 2 == 1 or y >= h - 1:
            return None
        if y >= h - 3 and d >= 12:
            return None                                         # las de atras, mas cortas
        c = hueso(cara, x, y, w, h)
        return oscuro(c, 0.14 * (y / max(1, h - 1)))

    def espinazo(cara, x, y, w, h):
        c = hueso_b(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba") and (x + y) % 3 == 0:
            return oscuro(c, 0.28)                              # las juntas de las vertebras
        return c

    def manta(cara, x, y, w, h):
        # la manta roja con una franja de oro abajo; por arriba no se ve (la tapa la silla)
        if cara in ("arriba", "frente", "espalda", "abajo"):
            return None
        if y == h - 1:
            return ORO[1] if x % 2 == 0 else ORO[2]
        if y == h - 2:
            return ROJO[0]
        c = manta_c(cara, x, y, w, h)
        if (x + 2 * y) % 7 == 0:
            return oscuro(c, 0.15)
        return c

    def silla(cara, x, y, w, h):
        c = cuero(cara, x, y, w, h)
        if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)):
            return ORO[1]                                       # el ribete
        if cara in ("der", "izq") and y == 0 and x % 2 == 0:
            return ORO[2]                                       # los remaches
        return c

    def pelvis(cara, x, y, w, h):
        if cara in ("frente", "espalda") and 3 <= x <= 5 and y >= 1:
            return None
        return hueso_b(cara, x, y, w, h)

    def cuello(cara, x, y, w, h):
        c = hueso_b(cara, x, y, w, h)
        if cara in ("der", "izq", "abajo") and (y if cara in ("der", "izq") else x) % 2 == 0:
            return oscuro(c, 0.3)
        return c

    def craneo(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if d <= 2 and y in (1, 2):
                return HUECO                                    # la cuenca, vista de lado
            if y >= 4 and d >= 2:
                return oscuro(c, 0.22)
        if cara == "abajo":
            return HUECO
        return c

    def hocico(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara == "frente":
            if y == 1 and x in (1, 2):
                return HUECO                                    # los agujeros de la nariz
            if y == h - 1:
                return HUESO[5] if x % 2 == 0 else HUECO        # los dientes
            return c
        if cara in ("der", "izq") and y == h - 1:
            return HUESO[5] if x % 2 == 0 else HUECO
        if cara in ("der", "izq") and y == 1 and delante(cara, x, w) >= 2:
            return oscuro(c, 0.25)                              # la hendidura del hueso nasal
        if cara == "abajo":
            return HUECO
        return c

    def mandibula(cara, x, y, w, h):
        if cara == "arriba":
            return HUESO[5] if (x + y) % 2 == 0 else HUECO
        return oscuro(hueso(cara, x, y, w, h), 0.08)

    def oreja(cara, x, y, w, h):
        return oscuro(hueso_b(cara, x, y, w, h), 0.1 if y > 0 else 0.0)

    def brida(cara, x, y, w, h):
        if cara in ("der", "izq") and x == w // 2:
            return ORO[2]                                       # la anilla
        return CUERO[1] if (x + y) % 2 == 0 else CUERO[2]

    def hueso_largo(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if y == 0 or y == h - 1:
            return claro(c, 0.08)                               # las articulaciones, mas gordas y claras
        return c

    def casco(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if cara == "abajo":
            return HIERRO[0]
        if y == h - 1 and cara != "arriba":
            return HIERRO[1]
        return c

    return {
        "pelvis": sombra(pelvis, abajo=0.15), "costillas": costillas, "espinazo": espinazo, "manta": manta, "silla": silla,
        "perilla": liso(ORO[2]), "cuello": cuello, "crin": liso(None), "craneo": sombra(craneo, abajo=0.12),
        "hocico": sombra(hocico, abajo=0.1), "oreja": oreja, "brida": brida, "copete": liso(None), "mandibula": mandibula,
        "brazo_d": hueso_largo, "brazo_i": hueso_largo, "antebrazo_d": hueso_largo, "antebrazo_i": hueso_largo,
        "menudillo": sombra(hueso_b, abajo=0.1), "casco": casco,
        "muslo_d": hueso_largo, "muslo_i": hueso_largo, "pierna_d": hueso_largo, "pierna_i": hueso_largo,
        "tarso_d": sombra(hueso_largo, abajo=0.1), "tarso_i": sombra(hueso_largo, abajo=0.1),
        "cola_hueso": hueso_b, "cola_niebla": liso(None), "cola_niebla2": liso(None),
    }


def brillo(variante):
    """Lo que brilla: los ojos (llamitas azules), la crin, el copete y la cola de niebla, y el fuego de los cascos."""
    C = {"A": ALMA[3], "a": ALMA[2]}

    def niebla(semilla, densa=0.62):
        azar = np.random.default_rng(semilla).random((64, 64))
        kc = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

        def f(cara, x, y, w, h):
            t = azar[(y + 7 * kc[cara]) % 64, (x + 13 * kc[cara]) % 64]
            # jirones: a ratos hueco, mas claro arriba
            if (x * 7 + y * 3 + semilla) % 5 == 0 or t > densa + 0.25 * (y / max(1, h - 1)):
                return None
            k = min(3, max(0, int(3.4 - 3.2 * y / max(1, h - 1) + (t - 0.5) * 1.6)))
            return ALMA[k]

        return f

    ojo_lado = lambda cara, x, y, w, h: ALMA[3] if (delante(cara, x, w) == 1 and y == 1) else (ALMA[1] if (delante(cara, x, w) in (0, 2) and y == 1) else None)  # noqa: E731

    def fuego(cara, x, y, w, h):
        if cara == "abajo":
            return ALMA[2] if (x + y) % 2 == 0 else ALMA[1]
        if cara in ("der", "izq", "frente", "espalda") and y == h - 1:
            return ALMA[3] if x % 2 == 0 else ALMA[2]
        return None

    return {"craneo": por_cara(liso(None), der=ojo_lado, izq=ojo_lado), "crin": niebla(5), "copete": niebla(9),
            "cola_niebla": niebla(13), "cola_niebla2": niebla(17, densa=0.5), "casco": fuego}


# ---------------------------------------------------------------- las animaciones
# Giros en grados sobre la pose de reposo. Patas: x NEGATIVO = hacia delante; las de atras doblan al reves.

BASE = dict(raiz=raiz(0.0), torso=(0, 0, 0), cuello=(0, 0, 0), cabeza=(0, 0, 0), mandibula=(0, 0, 0), cola=(0, 0, 0), cola2=(0, 0, 0))


def _patas(fd, fi, td, ti, lomo=0.0, sube=0.0, cuello=0.0, cabeza=0.0, cola=0.0):
    """Una pose del paso: fd/fi = las de delante (der/izq), td/ti = las de atras; + = la pata hacia atras."""
    def delante_(a):
        # la que viene adelante dobla la rodilla para no arrastrar el casco
        return (a, 0, 0), (max(0.0, -a) * 1.1 + 4, 0, 0), (-max(0.0, -a) * 0.5, 0, 0)

    def atras_(a):
        return (a, 0, 0), (max(0.0, -a) * 0.9 + 3, 0, 0), (-max(0.0, -a) * 0.6, 0, 0)

    bd, ad, md = delante_(fd)
    bi, ai, mi = delante_(fi)
    sd, pd, ped = atras_(td)
    si, pi_, pei = atras_(ti)
    return {"brazo_d": bd, "antebrazo_d": ad, "mano_d": md, "brazo_i": bi, "antebrazo_i": ai, "mano_i": mi,
            "muslo_d": sd, "pierna_d": pd, "pata_d": ped, "muslo_i": si, "pierna_i": pi_, "pata_i": pei,
            "torso": (lomo, 0, 0), "cuello": (cuello, 0, 0), "cabeza": (cabeza, 0, 0), "raiz": raiz(sube),
            "cola": (cola, 0, 0), "cola2": (cola * 0.6, 0, 0)}


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

    # ---- quieto: respira, sacude la cabeza, mueve la cola y a ratos escarba con una mano
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (1.0, con(BASE, torso=(-1, 0, 0), cuello=(-4, 6, 0), cabeza=(2, 6, 4), mandibula=(6, 0, 0), cola=(-6, 10, 0), cola2=(-4, 12, 0))),
        (1.5, con(BASE, torso=(-1, 0, 0), cuello=(-6, -8, 0), cabeza=(-4, -10, -6), mandibula=(2, 0, 0), cola=(-6, -8, 0), cola2=(-4, -10, 0))),
        (2.2, con(BASE, cuello=(2, -4, 0), cabeza=(4, -2, 0), cola=(-2, -4, 0))),
        (3.0, con(BASE, cuello=(4, 0, 0), cabeza=(6, 0, 0), brazo_d=(-30, 0, 0), antebrazo_d=(50, 0, 0), mano_d=(-10, 0, 0), cola=(-4, 6, 0))),
        (3.4, con(BASE, cuello=(4, 0, 0), cabeza=(6, 0, 0), brazo_d=(-8, 0, 0), antebrazo_d=(14, 0, 0), cola=(-4, 8, 0))),
        (3.8, con(BASE, cuello=(4, 0, 0), cabeza=(6, 0, 0), brazo_d=(-28, 0, 0), antebrazo_d=(48, 0, 0), mano_d=(-10, 0, 0))),
        (4.3, con(BASE, cuello=(2, 2, 0), cabeza=(2, 2, 0))),
        (5.0, BASE),
    ])
    # ---- andar: el paso de cuatro tiempos (mano izq, pie der, mano der, pie izq); la cabeza cabecea
    bucle("andar", 1.4, [
        (0.00, _patas(-24, 16, 14, -20, lomo=0.5, sube=0.0, cuello=4, cabeza=-2, cola=-4)),
        (0.35, _patas(-6, 22, -20, 8, lomo=-0.5, sube=0.4, cuello=-2, cabeza=2, cola=-2)),
        (0.70, _patas(16, -24, -20, 14, lomo=0.5, sube=0.0, cuello=4, cabeza=-2, cola=-4)),
        (1.05, _patas(22, -6, 8, -20, lomo=-0.5, sube=0.4, cuello=-2, cabeza=2, cola=-2)),
        (1.40, _patas(-24, 16, 14, -20, lomo=0.5, sube=0.0, cuello=4, cabeza=-2, cola=-4)),
    ])
    # ---- correr: al galope (las de atras empujan juntas, las de delante se estiran; el lomo se encoge y se estira)
    bucle("correr", 0.7, [
        (0.000, con(_patas(-58, -46, 40, 50, lomo=-6, sube=1.6, cuello=8, cabeza=-6, cola=14), cadera=(-6, 0, 0))),
        (0.175, con(_patas(-12, -4, -14, -4, lomo=4, sube=3.0, cuello=-4, cabeza=4, cola=24), cadera=(4, 0, 0))),
        (0.350, con(_patas(44, 52, -56, -48, lomo=8, sube=0.4, cuello=-10, cabeza=6, cola=30), cadera=(10, 0, 0))),
        (0.525, con(_patas(12, 20, 8, 16, lomo=-2, sube=1.0, cuello=2, cabeza=-2, cola=18), cadera=(-2, 0, 0))),
        (0.700, con(_patas(-58, -46, 40, 50, lomo=-6, sube=1.6, cuello=8, cabeza=-6, cola=14), cadera=(-6, 0, 0))),
    ])
    # ---- en el aire (saltando): las manos recogidas, las patas de atras estiradas, el cuello adelante
    SALTO = con(_patas(-70, -64, 46, 52, lomo=-4, sube=0.0, cuello=12, cabeza=-10, cola=36),
                antebrazo_d=(96, 0, 0), antebrazo_i=(92, 0, 0), mano_d=(-30, 0, 0), mano_i=(-30, 0, 0), cadera=(-6, 0, 0))
    bucle("salto", 1.0, [(0.0, SALTO), (0.5, con(SALTO, cola=(40, 6, 0), cola2=(30, 8, 0))), (1.0, SALTO)])

    # ================================================================ acciones
    # ---- encabritarse (al llegar con el silbato): se alza sobre las patas de atras, manotea y vuelve a caer
    ALZADO = dict(raiz=raiz(1.0, 6.0, rot=(-48, 0, 0)), cadera=(0, 0, 0), torso=(0, 0, 0), cuello=(-20, 0, 0), cabeza=(-10, 0, 0),
                  mandibula=(24, 0, 0), brazo_d=(-70, 0, 0), antebrazo_d=(100, 0, 0), mano_d=(-20, 0, 0),
                  brazo_i=(-40, 0, 0), antebrazo_i=(80, 0, 0), mano_i=(-10, 0, 0),
                  muslo_d=(30, 0, 0), pierna_d=(10, 0, 0), pata_d=(10, 0, 0), muslo_i=(26, 0, 0), pierna_i=(14, 0, 0), pata_i=(8, 0, 0),
                  cola=(40, 0, 0), cola2=(30, 0, 0))
    accion("encabritarse", 1.8, [
        (0.00, BASE),
        (0.40, ALZADO),
        (0.65, con(ALZADO, brazo_d=(-40, 0, 0), antebrazo_d=(80, 0, 0), brazo_i=(-74, 0, 0), antebrazo_i=(104, 0, 0), mandibula=(30, 0, 0))),
        (0.90, con(ALZADO, brazo_d=(-72, 0, 0), antebrazo_d=(100, 0, 0), brazo_i=(-42, 0, 0), antebrazo_i=(82, 0, 0), cuello=(-26, 0, 0))),
        (1.25, con(BASE, raiz=raiz(-0.6), cuello=(10, 0, 0), cabeza=(6, 0, 0), brazo_d=(-10, 0, 0), brazo_i=(-14, 0, 0))),
        (1.80, BASE),
    ])
    # ---- deshacerse (herido de muerte o despedido): se le doblan las patas, se tumba y se hunde en la niebla
    TUMBADO = dict(raiz=raiz(-13.0), cadera=(0, 0, 0), torso=(0, 0, 0), cuello=(34, 0, 0), cabeza=(-30, 10, 0), mandibula=(14, 0, 0),
                   brazo_d=(-80, 0, 0), antebrazo_d=(150, 0, 0), mano_d=(-60, 0, 0), brazo_i=(-80, 0, 0), antebrazo_i=(150, 0, 0), mano_i=(-60, 0, 0),
                   muslo_d=(-70, 0, 12), pierna_d=(150, 0, 0), pata_d=(-70, 0, 0), muslo_i=(-70, 0, -12), pierna_i=(150, 0, 0), pata_i=(-70, 0, 0),
                   cola=(-20, 30, 0), cola2=(0, 20, 0))
    accion("deshacerse", 1.6, [
        (0.00, BASE),
        (0.30, con(BASE, raiz=raiz(-2.0), cuello=(-24, 0, 0), cabeza=(-14, 0, 0), mandibula=(30, 0, 0))),
        (0.80, con(TUMBADO, raiz=raiz(-8.0), cuello=(10, 0, 0))),
        (1.20, TUMBADO),
        (1.60, con(TUMBADO, raiz=raiz(-15.0))),
    ])
    return B, A


# lo que ademas de las acciones necesita el servidor
TABLA_EXTRA = ()
