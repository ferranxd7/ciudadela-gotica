"""
El perro de hueso de Valdrenn (parte 4b): los lebreles de la guardia del Rey, que siguieron cazando cuando ya no les
quedaba carne. Un esqueleto de perro flaco y largo, con jirones de pellejo oscuro pegados al lomo, el collar de hierro con
pinchos y la cadena rota, y dos brasas rojas en las cuencas. Van en jauria: rodean, muerden deprisa y saltan desde lejos
(el salto se ve venir: se agacha antes). Una sola variante.

Las patas no son las de una persona, pero se llaman igual (brazo / antebrazo / mano las de delante, muslo / pierna /
pata las de atras) para que el modelo del juego y las vistas las traten igual. Sin cinematica inversa: todo son giros.
"""
from anim import Anim, con, raiz
from modelo import Box, Rig, g
from pinta import (claro, delante, dibujo, liso, metal, oscuro, por_cara, ruido, sombra)

NOMBRE = "perro"
VARIANTES = ("hueso",)
ESCALA = 1.0
CENTRO_Y = 0.55

# ---------------------------------------------------------------- el modelo
# (pixeles; Y hacia abajo; el suelo en y = 24; el hocico mira a -Z; la derecha del perro es -X)


def rig():
    r = Rig(NOMBRE, 64)
    r.pieza("raiz", "", (0, 24, 0))
    # la cadera (atras, a 13 del suelo): de ella salen el lomo hacia delante, las patas de atras y el rabo
    r.pieza("cadera", "raiz", (0, -13, 6), boxes=[Box(-3, -2, -1.5, 6, 4, 6, "pelvis")])
    # el lomo: las costillas (con huecos), el espinazo por encima y el pellejo que le queda pegado
    r.pieza("torso", "cadera", (0, -0.5, -1), boxes=[
        Box(-3.5, -1.5, -13, 7, 8, 12, "costillas"),
        Box(-0.75, -2.6, -14, 1.5, 1.6, 16, "espinazo"),
        Box(-3.8, -2.0, -12.5, 7.6, 5, 10, "pellejo", grow=g(0.15)),
    ])
    # el cuello (sube hacia delante) y la cabeza (alargada: el craneo y el hocico), con la mandibula suelta
    r.pieza("cuello", "torso", (0, 1, -12), rot=(-38, 0, 0), boxes=[
        Box(-1.5, -1.5, -6, 3, 3, 6.5, "cuello"),
        Box(-2.3, -2.3, -2.2, 4.6, 4.6, 1.6, "collar"),
        Box(-0.4, -3.7, -1.8, 0.8, 1.5, 0.8, "pincho"),
        Box(-3.7, -0.4, -1.8, 1.5, 0.8, 0.8, "pincho_lado"),
        Box(2.2, -0.4, -1.8, 1.5, 0.8, 0.8, "pincho_lado"),
        Box(-0.5, 2.2, -1.6, 1, 2.6, 1, "cadena"),
    ])
    r.pieza("cabeza", "cuello", (0, 0, -5.5), rot=(38, 0, 0), boxes=[
        Box(-2.5, -2.8, -5, 5, 5, 5.5, "craneo"),
        Box(-1.6, -1.2, -10.5, 3.2, 2.6, 5.6, "hocico"),
        Box(-1.1, 1.3, -10.2, 0.6, 1.0, 0.6, "colmillo"),
        Box(0.5, 1.3, -10.2, 0.6, 1.0, 0.6, "colmillo"),
    ])
    r.pieza("mandibula", "cabeza", (0, 1.5, -4.5), rot=(6, 0, 0), boxes=[
        Box(-1.4, 0, -5.6, 2.8, 1.2, 6, "mandibula"),
        Box(-1.0, -0.8, -5.4, 0.5, 0.8, 0.5, "colmillo"),
        Box(0.5, -0.8, -5.4, 0.5, 0.8, 0.5, "colmillo"),
    ])
    # las patas de delante: del hombro (a 10 del suelo) al codo, a la mano y a la zarpa
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"brazo_{n}", "torso", (s * 2.6, 3.5, -10.5), boxes=[Box(-1.1, -1.5, -1.2, 2.2, 6, 2.4, f"brazo_{n}")])
        r.pieza(f"antebrazo_{n}", f"brazo_{n}", (0, 4.5, 0), boxes=[Box(-0.8, 0, -0.8, 1.6, 4.5, 1.6, f"antebrazo_{n}")])
        r.pieza(f"mano_{n}", f"antebrazo_{n}", (0, 4.5, 0), boxes=[Box(-1.2, 0, -2.4, 2.4, 1, 3.2, "zarpa")])
    # las de atras: el muslo hacia delante, la canilla hacia atras y el tarso de pie (el corvejon de los perros)
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"muslo_{n}", "cadera", (s * 2.5, 1.5, 3), rot=(-25, 0, 0), boxes=[Box(-1.3, -1.5, -1.4, 2.6, 6, 2.8, f"muslo_{n}")])
        r.pieza(f"pierna_{n}", f"muslo_{n}", (0, 4.5, 0), rot=(50, 0, 0), boxes=[Box(-0.9, 0, -0.9, 1.8, 4.5, 1.8, f"pierna_{n}")])
        r.pieza(f"pata_{n}", f"pierna_{n}", (0, 4.5, 0), rot=(-25, 0, 0), boxes=[
            Box(-0.8, 0, -0.8, 1.6, 2.6, 1.6, f"tarso_{n}"),
            Box(-1.2, 2.6, -2.6, 2.4, 1, 3.2, "zarpa"),
        ])
    # el rabo: dos trozos de vertebras, caido
    r.pieza("cola", "cadera", (0, -1, 4.5), rot=(-18, 0, 0), boxes=[Box(-0.55, -0.55, 0, 1.1, 1.1, 6, "cola")])
    r.pieza("cola2", "cola", (0, 0, 6), rot=(-16, 0, 0), boxes=[Box(-0.45, -0.45, 0, 0.9, 0.9, 5, "cola")])
    r.empaquetar()
    return r


OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

HUESO = ["#463f31", "#6a614d", "#8c8266", "#aa9f7c", "#c2b792", "#d6cdab"]
HUECO = "#110e0b"
OJO = "#ff3b22"
PELLEJO = ["#141110", "#1d1815", "#28211c", "#342b24", "#41362d"]
HIERRO = ["#22262a", "#3c4247", "#5d656b", "#868f95"]
OXIDO = ["#3a2416", "#5a3420"]


def materiales(variante):
    hueso = ruido(HUESO[2:6], 611, grano=0.45, base=0.55)
    hueso_b = ruido(HUESO[1:5], 612, grano=0.5, base=0.55)
    pellejo = ruido(PELLEJO, 621, grano=0.55, base=0.5)
    hierro = metal(HIERRO, 631, grano=0.25)

    def costillas(cara, x, y, w, h):
        # tiras de hueso de arriba abajo por los costados (una si y otra no, de delante a atras), el esternon por abajo
        if cara == "arriba":
            return None                                         # arriba va el espinazo
        if cara == "abajo":
            return hueso_b(cara, x, y, w, h) if x in (2, 3, 4) else None
        if cara in ("frente", "espalda"):
            if cara == "espalda":
                return None
            return hueso(cara, x, y, w, h) if (x in (0, w - 1) or y == 0) else None
        d = delante(cara, x, w)
        if d % 2 == 1 or y >= h - 1:
            return None
        if y >= h - 3 and d >= 8:
            return None                                         # las costillas de atras, mas cortas
        c = hueso(cara, x, y, w, h)
        return oscuro(c, 0.12 * (y / max(1, h - 1)))

    def espinazo(cara, x, y, w, h):
        c = hueso_b(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba") and (x + y) % 3 == 0:
            return oscuro(c, 0.25)                              # las juntas de las vertebras
        return c

    def pellejo_lomo(cara, x, y, w, h):
        # jirones: solo pegado por arriba y a trozos por los costados, con agujeros
        if cara in ("abajo", "frente", "espalda"):
            return None
        if cara == "arriba":
            if (x * 5 + y * 3) % 11 < 2:
                return None
            return pellejo(cara, x, y, w, h)
        d = delante(cara, x, w)
        largo = 1 + ((d * 7 + 3) % 5)                           # cuanto cuelga en cada columna
        if y > largo or (d * 3 + y * 5) % 13 == 0:
            return None
        return oscuro(pellejo(cara, x, y, w, h), 0.06 * y)

    def pelvis(cara, x, y, w, h):
        if cara in ("frente", "espalda") and 2 <= x <= 3 and y >= 1:
            return None
        return hueso_b(cara, x, y, w, h)

    def cuello(cara, x, y, w, h):
        c = hueso_b(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo") and (y if cara in ("der", "izq") else x) % 2 == 0:
            return oscuro(c, 0.3)
        return c

    def craneo(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara == "frente":
            # las cuencas (con la brasa) a los lados del hocico, la frente encima
            if y in (1, 2) and x in (0, 1, 3, 4):
                return HUECO
            return c
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if d <= 1 and y in (1, 2):
                return HUECO                                    # la cuenca vista de lado
            if y >= 3 and d >= 3:
                return oscuro(c, 0.2)                           # el hueco de la mejilla
        if cara == "abajo":
            return HUECO
        return c

    def hocico(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara == "frente":
            if y == 0 and x in (1, 2):
                return HUECO                                    # la nariz: dos agujeros
            if y == h - 1:
                return HUESO[5] if x % 2 == 0 else HUECO        # los dientes de arriba
            return c
        if cara in ("der", "izq") and y == h - 1:
            return HUESO[5] if x % 2 == 0 else HUECO            # los dientes por el costado
        if cara == "abajo":
            return HUECO
        return c

    def mandibula(cara, x, y, w, h):
        if cara == "arriba":
            return HUESO[5] if (x + y) % 2 == 0 else HUECO       # los dientes de abajo
        return oscuro(hueso(cara, x, y, w, h), 0.08)

    def colmillo(cara, x, y, w, h):
        return HUESO[5] if y == 0 else HUESO[4]

    def hueso_largo(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if y == 0 or y == h - 1:
            return claro(c, 0.08)                               # las articulaciones, mas gordas y claras
        return c

    def muslo(cara, x, y, w, h):
        # el muslo con algo de pellejo pegado por fuera
        c = hueso_largo(cara, x, y, w, h)
        if cara in ("der", "izq") and y <= 2 and (x + y) % 3 != 0:
            return pellejo(cara, x, y, w, h)
        return c

    def zarpa(cara, x, y, w, h):
        if cara == "frente" and y == h - 1:
            return HUESO[5] if x % 2 == 0 else HUECO            # las unas
        if cara == "abajo":
            return HUESO[1]
        return hueso_b(cara, x, y, w, h)

    def collar(cara, x, y, w, h):
        c = hierro(cara, x, y, w, h)
        if (x * 3 + y) % 7 == 0:
            return OXIDO[(x + y) % 2]
        return c

    def cola(cara, x, y, w, h):
        c = hueso_b(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo") and (x if cara in ("arriba", "abajo") else x) % 2 == 0:
            return oscuro(c, 0.28)
        return c

    return {
        "pelvis": sombra(pelvis, abajo=0.15), "costillas": costillas, "espinazo": espinazo, "pellejo": pellejo_lomo,
        "cuello": cuello, "collar": collar, "pincho": liso(HIERRO[3]), "pincho_lado": liso(HIERRO[3]),
        "cadena": por_cara(liso(HIERRO[1]), frente=dibujo(["a", "b", "a"], {"a": HIERRO[2], "b": HIERRO[0]})),
        "craneo": sombra(craneo, abajo=0.12), "hocico": sombra(hocico, abajo=0.1), "mandibula": mandibula, "colmillo": colmillo,
        "brazo_d": muslo, "brazo_i": muslo, "antebrazo_d": hueso_largo, "antebrazo_i": hueso_largo,
        "muslo_d": muslo, "muslo_i": muslo, "pierna_d": hueso_largo, "pierna_i": hueso_largo,
        "tarso_d": sombra(hueso_largo, abajo=0.1), "tarso_i": sombra(hueso_largo, abajo=0.1), "zarpa": zarpa,
        "cola": cola,
    }


def brillo(variante):
    """Lo que brilla: las brasas de las cuencas (de frente y de lado)."""
    C = {"R": OJO}
    frente = dibujo([".....", "R...R", "....."], C)
    lado_d = dibujo(["......", "R.....", "R....."], C)
    lado_i = dibujo(["......", ".....R", ".....R"], C)
    return {"craneo": por_cara(liso(None), frente=frente, der=lado_d, izq=lado_i)}


# ---------------------------------------------------------------- las animaciones
# Giros en grados sobre la pose de reposo. Patas: x NEGATIVO = hacia delante; las de atras doblan al reves.

BASE = dict(raiz=raiz(0.0), torso=(0, 0, 0), cuello=(0, 0, 0), cabeza=(0, 0, 0), mandibula=(0, 0, 0), cola=(0, 0, 0))
# de guardia: agachado por delante, la cabeza baja y adelantada, el rabo tieso, ensenando los dientes
LISTO = dict(raiz=raiz(-0.8), cadera=(4, 0, 0), torso=(8, 0, 0), cuello=(22, 0, 0), cabeza=(-20, 0, 0), mandibula=(12, 0, 0),
             brazo_d=(-18, 0, 0), antebrazo_d=(30, 0, 0), mano_d=(-12, 0, 0), brazo_i=(-10, 0, 0), antebrazo_i=(26, 0, 0), mano_i=(-16, 0, 0),
             muslo_d=(-8, 0, 0), pierna_d=(14, 0, 0), pata_d=(-6, 0, 0), muslo_i=(-14, 0, 0), pierna_i=(18, 0, 0), pata_i=(-4, 0, 0),
             cola=(14, 0, 0), cola2=(10, 0, 0))


def _paso(fd, fi, td, ti, lomo=0.0, sube=0.0, cabeza=0.0):
    """Una pose del trote: fd/fi = las de delante (der/izq), td/ti = las de atras; + = la pata hacia atras."""
    def delante_(a):
        # la pata que va atras estira el codo; la que viene, lo dobla para no arrastrar la zarpa
        return (a, 0, 0), (max(0.0, -a) * 1.2 + 6, 0, 0), (-max(0.0, -a) * 0.4, 0, 0)

    def atras_(a):
        return (a, 0, 0), (max(0.0, -a) * 0.9 + 4, 0, 0), (-max(0.0, -a) * 0.5, 0, 0)

    bd, ad, md = delante_(fd)
    bi, ai, mi = delante_(fi)
    sd, pd, ped = atras_(td)
    si, pi_, pei = atras_(ti)
    return {"brazo_d": bd, "antebrazo_d": ad, "mano_d": md, "brazo_i": bi, "antebrazo_i": ai, "mano_i": mi,
            "muslo_d": sd, "pierna_d": pd, "pata_d": ped, "muslo_i": si, "pierna_i": pi_, "pata_i": pei,
            "torso": (lomo, 0, 0), "cuello": (cabeza, 0, 0), "raiz": raiz(sube), "cola": (6 + lomo * 2, 0, 0)}


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

    # ---- quieto: respira (se le mueve el lomo), olisquea, la mandibula le cuelga y a ratos chasquea
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.0, con(BASE, torso=(-1.5, 0, 0), cuello=(6, -10, 0), cabeza=(-4, -8, 4), mandibula=(10, 0, 0), cola=(-4, 6, 0))),
        (1.6, con(BASE, torso=(-1.5, 0, 0), cuello=(8, -12, 0), cabeza=(-2, -10, 4), mandibula=(2, 0, 0), cola=(-4, 8, 0))),
        (2.4, con(BASE, torso=(0.5, 0, 0), cuello=(2, 8, 0), cabeza=(0, 12, -4), mandibula=(14, 0, 0), cola=(-2, -6, 0))),
        (3.2, con(BASE, torso=(-1, 0, 0), cuello=(0, 4, 0), cabeza=(2, 6, 0), mandibula=(4, 0, 0), cola=(-3, -4, 0))),
        (4.0, BASE),
    ])
    # ---- andar: un trote (las patas en diagonal a la vez); en el juego se acelera con lo que corre
    T = 1.0
    bucle("andar", T, [
        (0.00, _paso(-26, 22, 20, -24, lomo=1, sube=0.0, cabeza=-4)),
        (0.25, _paso(0, 0, 0, 0, lomo=-1, sube=0.7, cabeza=2)),
        (0.50, _paso(22, -26, -24, 20, lomo=1, sube=0.0, cabeza=-4)),
        (0.75, _paso(0, 0, 0, 0, lomo=-1, sube=0.7, cabeza=2)),
        (1.00, _paso(-26, 22, 20, -24, lomo=1, sube=0.0, cabeza=-4)),
    ])
    # ---- correr: al galope (las de delante juntas, luego las de atras; el lomo se encoge y se estira)
    bucle("correr", 0.6, [
        (0.00, con(_paso(-62, -52, 46, 54, lomo=-8, sube=1.8, cabeza=-12), cadera=(-8, 0, 0))),
        (0.15, con(_paso(-16, -8, -16, -6, lomo=5, sube=2.8, cabeza=-4), cadera=(5, 0, 0))),
        (0.30, con(_paso(48, 56, -62, -54, lomo=10, sube=0.4, cabeza=6), cadera=(12, 0, 0))),
        (0.45, con(_paso(10, 18, 10, 16, lomo=-2, sube=1.0, cabeza=-2), cadera=(-2, 0, 0))),
        (0.60, con(_paso(-62, -52, 46, 54, lomo=-8, sube=1.8, cabeza=-12), cadera=(-8, 0, 0))),
    ])
    # ---- de guardia: agachado, grune (el lomo y la cabeza tiemblan un poco)
    bucle("listo", 1.2, [
        (0.0, LISTO),
        (0.3, con(LISTO, cuello=(24, -3, 0), mandibula=(16, 0, 0))),
        (0.6, con(LISTO, cuello=(21, 2, 0), mandibula=(10, 0, 0), torso=(9, 0, 0))),
        (0.9, con(LISTO, cuello=(23, 3, 0), mandibula=(15, 0, 0))),
        (1.2, LISTO),
    ])
    # ---- echado (dormido): tumbado de pecho, la cabeza sobre las patas; respira
    ECHADO = dict(raiz=raiz(-7.6), cadera=(0, 0, 0), torso=(0, 0, 0), cuello=(34, 0, 0), cabeza=(-22, 10, 0), mandibula=(2, 0, 0),
                  brazo_d=(-82, 0, 0), antebrazo_d=(4, 0, 0), mano_d=(80, 0, 0), brazo_i=(-78, 0, 0), antebrazo_i=(2, 0, 0), mano_i=(76, 0, 0),
                  muslo_d=(-70, 0, 20), pierna_d=(140, 0, 0), pata_d=(-70, 0, 0), muslo_i=(-70, 0, -20), pierna_i=(140, 0, 0), pata_i=(-70, 0, 0),
                  cola=(-30, 40, 0), cola2=(0, 40, 0))
    bucle("echado", 3.0, [(0.0, ECHADO), (1.5, con(ECHADO, raiz=raiz(-7.3), torso=(-1.2, 0, 0))), (3.0, ECHADO)])
    # ---- tirado: un monton de huesos de lado (asi espera en las emboscadas)
    TIRADO = dict(raiz=raiz(3.2, x=0.0, rot=(0, 0, 84)), torso=(0, 0, 0), cuello=(30, 20, 0), cabeza=(-10, 30, 0), mandibula=(28, 0, 0),
                  brazo_d=(-30, 0, 10), antebrazo_d=(40, 0, 0), brazo_i=(10, 0, -10), antebrazo_i=(20, 0, 0),
                  muslo_d=(-10, 0, 6), pierna_d=(30, 0, 0), muslo_i=(20, 0, -6), pierna_i=(10, 0, 0), cola=(-10, 30, 0), cola2=(0, 30, 0))
    bucle("tumbado", 4.0, [(0.0, TIRADO), (4.0, TIRADO)])

    # ================================================================ acciones
    # ---- el mordisco: se echa atras un pelo, se lanza con la boca abierta y cierra (golpe a los 0,45 s)
    ABRE = con(LISTO, raiz=raiz(-1.2, 1.0), torso=(4, 0, 0), cuello=(10, 0, 0), cabeza=(-30, 0, 0), mandibula=(46, 0, 0),
               muslo_d=(-16, 0, 0), muslo_i=(-20, 0, 0), pierna_d=(24, 0, 0), pierna_i=(26, 0, 0))
    MUERDE = con(LISTO, raiz=raiz(-0.2, -4.0), torso=(-6, 0, 0), cuello=(-2, 0, 0), cabeza=(-6, 0, 0), mandibula=(0, 0, 0),
                 brazo_d=(-50, 0, 0), antebrazo_d=(20, 0, 0), brazo_i=(-44, 0, 0), antebrazo_i=(16, 0, 0),
                 muslo_d=(16, 0, 0), pierna_d=(4, 0, 0), muslo_i=(10, 0, 0), pierna_i=(6, 0, 0), cola=(26, 0, 0))
    accion("mordisco", 1.0, [
        (0.00, LISTO),
        (0.30, ABRE),
        (0.45, MUERDE),
        (0.55, con(MUERDE, cabeza=(-2, 14, 0), mandibula=(4, 0, 0))),
        (0.65, con(MUERDE, cabeza=(-2, -14, 0), mandibula=(4, 0, 0))),
        (1.00, LISTO),
    ], golpes=(0.45,))
    # ---- el salto: se agacha (se ve venir), salta con las patas por delante y muerde al caer (golpe a los 0,8 s)
    AGACHA = con(LISTO, raiz=raiz(-3.2, 1.5), cadera=(-6, 0, 0), torso=(4, 0, 0), cuello=(26, 0, 0), cabeza=(-24, 0, 0), mandibula=(20, 0, 0),
                 brazo_d=(-30, 0, 0), antebrazo_d=(60, 0, 0), mano_d=(-30, 0, 0), brazo_i=(-26, 0, 0), antebrazo_i=(58, 0, 0), mano_i=(-30, 0, 0),
                 muslo_d=(-40, 0, 0), pierna_d=(70, 0, 0), pata_d=(-30, 0, 0), muslo_i=(-44, 0, 0), pierna_i=(72, 0, 0), pata_i=(-28, 0, 0),
                 cola=(-10, 0, 0))
    VUELA = con(BASE, raiz=raiz(2.0), cadera=(-14, 0, 0), torso=(-8, 0, 0), cuello=(-10, 0, 0), cabeza=(-24, 0, 0), mandibula=(50, 0, 0),
                brazo_d=(-80, 0, 0), antebrazo_d=(10, 0, 0), brazo_i=(-74, 0, 0), antebrazo_i=(14, 0, 0),
                muslo_d=(50, 0, 0), pierna_d=(10, 0, 0), pata_d=(10, 0, 0), muslo_i=(56, 0, 0), pierna_i=(8, 0, 0), pata_i=(14, 0, 0),
                cola=(30, 0, 0), cola2=(10, 0, 0))
    CAE = con(MUERDE, raiz=raiz(-0.6, -2.0), torso=(-2, 0, 0), mandibula=(0, 0, 0))
    accion("salto", 1.5, [
        (0.00, LISTO),
        (0.45, AGACHA),
        (0.55, con(AGACHA, raiz=raiz(-3.4, 2.0))),
        (0.70, VUELA),
        (0.80, CAE),
        (0.95, con(CAE, cabeza=(-2, 12, 0))),
        (1.10, con(CAE, cabeza=(-2, -10, 0))),
        (1.50, LISTO),
    ], golpes=(0.8,))
    # ---- el aullido: alza la cabeza y aulla (llama a la jauria); no pega
    AULLA = con(BASE, raiz=raiz(0.4), cadera=(6, 0, 0), torso=(-14, 0, 0), cuello=(-40, 0, 0), cabeza=(-20, 0, 0), mandibula=(34, 0, 0),
                muslo_d=(-14, 0, 0), muslo_i=(-14, 0, 0), pierna_d=(24, 0, 0), pierna_i=(24, 0, 0), cola=(-20, 0, 0))
    accion("aullido", 2.0, [
        (0.00, LISTO),
        (0.35, AULLA),
        (1.20, con(AULLA, cabeza=(-24, 0, 0), mandibula=(40, 0, 0))),
        (1.60, con(AULLA, mandibula=(10, 0, 0))),
        (2.00, LISTO),
    ])
    # ---- aturdido (se le rompio la guardia): se le doblan las patas y sacude la cabeza
    accion("aturdido", 1.5, [
        (0.00, LISTO),
        (0.20, con(LISTO, raiz=raiz(-3.0, 2.0), torso=(-6, 0, 10), cuello=(30, 20, 0), mandibula=(30, 0, 0), brazo_d=(-30, 0, 0), antebrazo_d=(50, 0, 0))),
        (0.70, con(LISTO, raiz=raiz(-2.6, 1.6), torso=(-4, 0, -8), cuello=(28, -24, 0), cabeza=(-10, -20, 0), mandibula=(20, 0, 0))),
        (1.10, con(LISTO, raiz=raiz(-1.6, 0.8), cuello=(24, 10, 0), cabeza=(-14, 14, 0))),
        (1.50, LISTO),
    ])
    # ---- morir: las patas se le van, cae de lado y se queda hecho un monton de huesos
    accion("morir", 2.0, [
        (0.00, LISTO),
        (0.30, con(LISTO, raiz=raiz(-2.0), torso=(-10, 0, 0), cuello=(-20, 0, 0), cabeza=(-20, 0, 0), mandibula=(40, 0, 0))),
        (0.70, con(LISTO, raiz=raiz(-5.0, rot=(0, 0, 30)), cuello=(10, 10, 0), brazo_d=(-50, 0, 0), brazo_i=(-30, 0, 0), mandibula=(30, 0, 0))),
        (1.10, TIRADO),
        (2.00, TIRADO),
    ])
    # ---- levantarse (de entre los huesos: las emboscadas) y alzarse (de dormido)
    accion("levantarse", 1.4, [
        (0.00, TIRADO),
        (0.40, con(TIRADO, raiz=raiz(-8.0, rot=(0, 0, 60)), cuello=(20, 0, 0), cabeza=(-20, 0, 0))),
        (0.85, con(AGACHA, raiz=raiz(-4.0, 0.0))),
        (1.40, LISTO),
    ])
    accion("alzarse", 1.2, [
        (0.00, ECHADO),
        (0.40, con(ECHADO, cuello=(10, 0, 0), cabeza=(-20, 0, 0))),
        (0.80, con(AGACHA, raiz=raiz(-3.0, 0.0))),
        (1.20, LISTO),
    ])
    return B, A


# lo que ademas de las acciones necesita el servidor
TABLA_EXTRA = ()
