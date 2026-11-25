"""
Vareth, el Guardian de Corteza (parte 4d): el escudero de Ser Gavran, que juro guardar la torre del arbol y al que el
arbol se trago. Un caballero enorme (x1,45) envuelto en corteza negra con musgo, raices colgando como un faldon, el
yelmo partido por dos ramas como cuernos, el corazon verde latiendo en el pecho y las manos convertidas en garras de
madera. Guarda la entrada de la torre del arbol (tras la puerta de la Llave del Arbol).
Golpes: zarpazo, doble zarpazo cruzado, golpe al suelo (las raices corren por el suelo hacia delante), espinas (abre
los brazos y suelta una rafaga de espinas delante) y, con media vida, las raices de alrededor (pisa y brotan a su
alrededor).
"""
import guardia as G
from anim import Anim, con, raiz
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, mezcla, oscuro, por_cara, ruido, sombra)

NOMBRE = "corteza"
VARIANTES = ("garras",)
ESCALA = 1.45

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(10, 12, 6), cabeza=(8, 8, 8), brazo=(5, 12, 5), pierna=(5, 12, 5))
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-5, -12, -3, 10, 12, 6, "coraza", grow=g(0.65))])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-5, -0.5, -3, 10, 9, 6, "faldon", grow=g(0.55))])
    for n, s in (("d", -1), ("i", 1)):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-3.6, -4.6, -3.4, 7.2, 5.2, 6.8, f"hombrera_{n}")])
        # una rama que sale de cada hombro
        r.pieza(f"rama_{n}", f"hombrera_{n}", (s * 1.5, -4.4, 1.0), rot=(-20, 0, s * 20), boxes=[Box(-0.8, -5, -0.8, 1.6, 5, 1.6, "rama")])
        # las garras: tres dedos de madera hacia delante
        for k, x in enumerate((-1.6, 0.0, 1.6)):
            r.pieza(f"dedo_{n}{k}", f"mano_{n}", (x, 0.5, -0.5), rot=(-10, 0, 0), boxes=[Box(-0.6, -0.6, -6.5, 1.2, 1.2, 7, "dedo")])
    # el yelmo cubierto de corteza y las dos ramas como cuernos
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[Box(-4, -8.4, -4, 8, 8.4, 8, "yelmo", grow=g(0.7))])
    for n, s in (("d", -1), ("i", 1)):
        r.pieza(f"cuerno_{n}", "yelmo", (s * 3.0, -8.4, 0.0), rot=(10, 0, s * 26), boxes=[
            Box(-0.8, -6.0, -0.8, 1.6, 6.0, 1.6, "rama"),
        ])
        r.pieza(f"cuerno2_{n}", f"cuerno_{n}", (0, -6.0, 0), rot=(0, 0, s * 30), boxes=[Box(-0.6, -4.0, -0.6, 1.2, 4.0, 1.2, "rama")])
    r.empaquetar()
    return r


OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

CORTEZA = ["#120d0a", "#1c1510", "#271d15", "#33271c", "#413224"]
MUSGO = ["#1c2a12", "#27391a", "#334a22"]
SAVIA = "#4fe07a"
SAVIA_B = "#b6ffc9"


def _corteza(semilla):
    """Corteza: vetas verticales oscuras, nudos y alguna mancha de musgo."""
    base = ruido(CORTEZA[1:5], semilla, grano=0.45, base=0.45)

    def f(cara, x, y, w, h):
        c = base(cara, x, y, w, h)
        k = (x * 13 + semilla) % 5
        if k == 0:
            c = oscuro(c, 0.3)                                   # la veta
        if (x * 7 + y * 11 + semilla) % 29 == 0:
            return MUSGO[(x + y) % 3]
        if cara == "arriba" and (x + y + semilla) % 3 == 0:
            return MUSGO[1]
        return c
    return f


def materiales(variante):
    base = G.materiales(0)
    cort = _corteza(611)
    cort_b = _corteza(612)
    oscura = ruido(CORTEZA[0:3], 613, grano=0.4)

    def coraza(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return cort(cara, x, y, w, h)
        c = cort(cara, x, y, w, h)
        if cara == "frente":
            # el hueco del pecho con el corazon (lo que brilla va en el brillo)
            dx, dy = x - 6, y - 4
            if dx * dx + dy * dy <= 2:
                return "#0f2a14"
            if dx * dx + dy * dy <= 5:
                return oscuro(c, 0.5)
        return c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # raices colgando, de largo desigual
        largo = h - 1 - ((x * 7 + (3 if cara in ("frente", "der") else 0)) % 4)
        if y > largo:
            return None
        c = oscura(cara, x, y, w, h)
        return claro(c, 0.08) if x % 2 == 0 else c

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return cort_b(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        c = cort(cara, x, y, w, h)
        return oscuro(c, 0.2) if y >= h - 1 else c

    def mano(cara, x, y, w, h):
        return oscura(cara, x, y, w, h)

    def dedo(cara, x, y, w, h):
        if cara == "frente":
            return CORTEZA[4]
        return CORTEZA[2] if (y + x) % 3 else CORTEZA[3]

    def rama(cara, x, y, w, h):
        c = CORTEZA[2 + ((y + x) % 3)]
        return MUSGO[1] if cara == "arriba" else c

    def yelmo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = cort(cara, x, y, w, h)
        if cara == "frente":
            # el hierro del yelmo asoma entre la corteza: la rendija y el refuerzo de la nariz
            if y == 4 and 1 <= x <= 6:
                return None
            if y in (3, 5) and 1 <= x <= 6:
                return mezcla(G.HIERRO[2], c, 0.4)
            if x in (3, 4) and y >= 5:
                return G.HIERRO[3]
        if cara in ("der", "izq") and y == 4 and delante(cara, x, w) <= 1:
            return None
        return c

    def pierna(cara, x, y, w, h):
        if y >= h - 1:
            return CORTEZA[0] if cara != "abajo" else CORTEZA[1]
        c = cort_b(cara, x, y, w, h)
        return oscuro(c, 0.25) if y % 4 == 0 else c

    m = dict(base)
    m.update({
        "torso": sombra(oscura), "coraza": sombra(coraza, abajo=0.14), "faldon": faldon,
        "hombrera_d": sombra(hombrera, abajo=0.1), "hombrera_i": sombra(hombrera, abajo=0.1),
        "brazo_d": sombra(cort, abajo=0.1), "brazo_i": sombra(cort, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12), "antebrazo_i": sombra(antebrazo, abajo=0.12),
        "muslo_d": sombra(cort_b, abajo=0.1), "muslo_i": sombra(cort_b, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.16), "pierna_i": sombra(pierna, abajo=0.16),
        "yelmo": sombra(yelmo, abajo=0.12), "rama": rama, "dedo": dedo,
    })
    return m


def brillo(variante):
    """Lo que brilla: los ojos verdes tras la rendija y el corazon del pecho."""
    C = {"R": SAVIA, "r": SAVIA_B}
    ojos = dibujo(["........", "........", "........", "........", ".R....R."], C)

    def corazon(cara, x, y, w, h):
        if cara != "frente":
            return None
        dx, dy = x - 6, y - 4
        d = dx * dx + dy * dy
        return SAVIA_B if d == 0 else SAVIA if d <= 2 else None
    return {"cabeza": por_cara(liso(None), frente=ojos), "coraza": corazon}


# ---------------------------------------------------------------- las animaciones

BASE = dict(raiz=raiz(-0.6), torso=(10, 0, 0), cabeza=(-4, 0, 0),
            muslo_d=(-6, 0, 8), pierna_d=(12, 0, 0), muslo_i=(-6, 0, -8), pierna_i=(12, 0, 0),
            brazo_d=(-10, 0, 14), antebrazo_d=(-20, 0, 0), brazo_i=(-10, 0, -14), antebrazo_i=(-20, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
LISTO = con(BASE, raiz=raiz(-1.6), torso=(18, 0, 0), cabeza=(-14, 0, 0),
            muslo_d=(-2, 0, 10), pierna_d=(22, 0, 0), muslo_i=(-26, 0, -10), pierna_i=(30, 0, 0),
            brazo_d=(-40, 0, 30), antebrazo_d=(-40, 0, 0), brazo_i=(-40, 0, -30), antebrazo_i=(-40, 0, 0))


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

    bucle("quieto", 5.0, [
        (0.0, BASE),
        (2.5, con(BASE, raiz=raiz(-1.0), torso=(13, 4, 0), cabeza=(-2, -10, 0), brazo_d=(-6, 0, 16), brazo_i=(-14, 0, -12))),
        (5.0, BASE),
    ])
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-26, 16, 8, 16, 6, 0.0), (-4, 0, 6, 34, 0, 0.7), (16, -26, 16, 8, -6, 0.0), (0, -4, 34, 6, 0, 0.7), (-26, 16, 8, 16, 6, 0.0))):
        pasos.append((1.7 * k / 4, {"muslo_d": (md, 0, 8), "muslo_i": (mi, 0, -8), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (12, ty, -ty * 0.4), "cabeza": (-4, -ty * 0.6, 0), "raiz": raiz(up - 0.6),
                                    "brazo_d": (-10 - ty * 3, 0, 14), "brazo_i": (-10 + ty * 3, 0, -14)}))
    bucle("andar", 1.7, pasos)
    bucle("porte_garras", 3.0, [(0.0, BASE), (3.0, BASE)], solo=BRAZOS)
    bucle("listo_garras", 2.0, [(0.0, LISTO), (1.0, con(LISTO, raiz=raiz(-2.0), torso=(21, 0, 0))), (2.0, LISTO)])
    # envuelto en raices, de rodillas, la cabeza baja (espera a que entre alguien)
    VELA = dict(raiz=raiz(-6.4, 0.6), torso=(30, 0, 0), cabeza=(30, 0, 0),
                muslo_d=(-4, 0, 6), pierna_d=(98, 0, 0), muslo_i=(-86, 0, -6), pierna_i=(84, 0, 0),
                brazo_d=(-20, 0, 10), antebrazo_d=(-30, 0, 0), brazo_i=(-20, 0, -10), antebrazo_i=(-30, 0, 0))
    bucle("velar", 4.0, [(0.0, VELA), (2.0, con(VELA, torso=(33, 0, 0))), (4.0, VELA)])
    TUMBADO = dict(raiz=raiz(2.6, 15.0, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 30, 0),
                   muslo_d=(4, 0, 8), pierna_d=(6, 0, 0), muslo_i=(-2, 0, -12), pierna_i=(22, 0, 0),
                   brazo_d=(-30, 0, 30), antebrazo_d=(-40, 0, 0), brazo_i=(-22, 0, -30), antebrazo_i=(-70, 0, 0))
    bucle("tumbado", 4.0, [(0.0, TUMBADO), (4.0, TUMBADO)])

    L = LISTO
    # ---- el zarpazo: la garra derecha atras y en arco de arriba abajo
    ALZA = con(L, torso=(4, 30, 0), cabeza=(-6, -24, 0), brazo_d=(-150, 0, 40), antebrazo_d=(-30, 0, 0))
    CORTA = con(L, raiz=raiz(-2.4, -2.0), torso=(28, -24, 0), cabeza=(-14, 20, 0), muslo_i=(-40, 0, -10), pierna_i=(36, 0, 0),
                brazo_d=(-20, 0, -10), antebrazo_d=(-10, 0, 0))
    accion("zarpazo", 1.4, [
        (0.00, L),
        (0.42, ALZA),
        (0.58, con(ALZA, brazo_d=(-160, 0, 44))),
        (0.66, CORTA),
        (0.95, con(CORTA, torso=(26, -28, 0))),
        (1.40, L),
    ], golpes=(0.66,))
    # ---- el doble: las dos garras abiertas y cruzadas delante
    ABRE = con(L, raiz=raiz(-0.8, 1.2), torso=(-6, 0, 0), cabeza=(-10, 0, 0),
               brazo_d=(-90, -40, 60), antebrazo_d=(-20, 0, 0), brazo_i=(-90, 40, -60), antebrazo_i=(-20, 0, 0))
    CRUZA = con(L, raiz=raiz(-2.6, -2.4), torso=(30, 0, 0), cabeza=(-16, 0, 0), muslo_i=(-40, 0, -10), pierna_i=(36, 0, 0),
                brazo_d=(-60, 40, -30), antebrazo_d=(-10, 0, 0), brazo_i=(-60, -40, 30), antebrazo_i=(-10, 0, 0))
    accion("doble", 1.7, [
        (0.00, L),
        (0.55, ABRE),
        (0.76, con(ABRE, brazo_d=(-96, -46, 64), brazo_i=(-96, 46, -64))),
        (0.84, CRUZA),
        (1.20, con(CRUZA, torso=(28, 0, 0))),
        (1.70, L),
    ], golpes=(0.84,))
    # ---- golpe al suelo: alza los dos puños y los clava (las raices corren hacia delante)
    ARRIBA = con(L, raiz=raiz(0.4, 1.0), torso=(-14, 0, 0), cabeza=(-16, 0, 0),
                 brazo_d=(-170, 0, 10), antebrazo_d=(-20, 0, 0), brazo_i=(-170, 0, -10), antebrazo_i=(-20, 0, 0))
    SUELO = con(L, raiz=raiz(-5.6, -2.0), torso=(56, 0, 0), cabeza=(-30, 0, 0),
                muslo_d=(-40, 0, 10), pierna_d=(80, 0, 0), muslo_i=(-50, 0, -10), pierna_i=(90, 0, 0),
                brazo_d=(-60, 0, 6), antebrazo_d=(-10, 0, 0), brazo_i=(-60, 0, -6), antebrazo_i=(-10, 0, 0))
    accion("golpe_suelo", 2.2, [
        (0.00, L),
        (0.65, ARRIBA),
        (1.00, con(ARRIBA, raiz=raiz(0.6, 1.2), torso=(-16, 0, 0))),
        (1.10, SUELO),
        (1.70, con(SUELO, torso=(54, 0, 0))),
        (2.20, L),
    ], golpes=(1.10,))
    # ---- espinas: se encoge y abre los brazos de golpe (rafaga de espinas delante)
    ENCOGE = con(L, raiz=raiz(-2.4, 1.0), torso=(40, 0, 0), cabeza=(10, 0, 0),
                 brazo_d=(-30, 0, -20), antebrazo_d=(-110, 0, 0), brazo_i=(-30, 0, 20), antebrazo_i=(-110, 0, 0))
    SUELTA = con(L, raiz=raiz(-0.4, -1.0), torso=(-18, 0, 0), cabeza=(-24, 0, 0),
                 brazo_d=(-80, 0, 80), antebrazo_d=(-10, 0, 0), brazo_i=(-80, 0, -80), antebrazo_i=(-10, 0, 0))
    accion("espinas", 1.9, [
        (0.00, L),
        (0.60, ENCOGE),
        (0.85, con(ENCOGE, torso=(44, 0, 0))),
        (0.95, SUELTA),
        (1.40, con(SUELTA, torso=(-16, 0, 0))),
        (1.90, L),
    ], golpes=(0.95,))
    # ---- las raices de alrededor (con media vida): pisa fuerte dos veces y ruge
    PISA = con(L, raiz=raiz(-2.6), torso=(30, 0, 0), muslo_d=(-70, 0, 10), pierna_d=(70, 0, 0))
    PISA2 = con(L, raiz=raiz(-3.2, -0.6), torso=(36, 0, 0), muslo_d=(0, 0, 10), pierna_d=(14, 0, 0))
    accion("raices", 2.4, [
        (0.00, L),
        (0.40, PISA),
        (0.60, PISA2),
        (0.95, con(PISA, muslo_i=(-70, 0, -10), pierna_i=(70, 0, 0), muslo_d=(-2, 0, 10), pierna_d=(22, 0, 0))),
        (1.15, PISA2),
        (1.50, con(L, torso=(-20, 0, 0), cabeza=(-30, 0, 0), brazo_d=(-120, 0, 50), brazo_i=(-120, 0, -50))),
        (2.40, L),
    ], golpes=(1.15,))
    # ---- el rugido: al empezar la pelea y a media vida
    RUGE = con(L, raiz=raiz(0.2), torso=(-20, 0, 0), cabeza=(-30, 0, 0),
               brazo_d=(-100, 0, 70), antebrazo_d=(-30, 0, 0), brazo_i=(-100, 0, -70), antebrazo_i=(-30, 0, 0))
    accion("rugido", 2.0, [
        (0.00, L),
        (0.45, RUGE),
        (1.30, con(RUGE, cabeza=(-34, 0, 0))),
        (2.00, L),
    ])
    accion("aturdido", 1.6, [
        (0.00, L),
        (0.20, con(L, raiz=raiz(-2.0, 2.2), torso=(-10, 20, 0), cabeza=(-20, -10, 0))),
        (0.90, con(L, raiz=raiz(-2.6, 1.8), torso=(-4, 12, 0), cabeza=(-8, 10, 0))),
        (1.60, L),
    ])
    accion("morir", 3.0, [
        (0.00, L),
        (0.70, con(VELA, raiz=raiz(-5.0, 0.6), torso=(26, 0, 0), cabeza=(-30, 0, 0), brazo_d=(-110, 0, 40), brazo_i=(-110, 0, -40))),
        (1.80, con(VELA, raiz=raiz(-6.4, 0.6), torso=(40, 0, 10), cabeza=(30, 0, 0))),
        (2.50, con(TUMBADO, raiz=raiz(2.6, 12.0, rot=(80, 0, -14)))),
        (3.00, con(TUMBADO, raiz=raiz(2.6, 12.0, rot=(88, 0, -14)))),
    ])
    accion("levantarse", 2.0, [
        (0.00, VELA),
        (0.80, con(VELA, raiz=raiz(-5.0, 0.4), torso=(20, 0, 0), cabeza=(-20, 0, 0))),
        (1.50, con(L, raiz=raiz(-2.4, 0.0))),
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
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_garras"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre in ("andar", "quieto"):
        return [anims["porte_garras"]]
    return []


TABLA_EXTRA = ()
