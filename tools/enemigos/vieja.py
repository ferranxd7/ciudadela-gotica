"""
La Vieja (parte 7r): la ballena que el viejo Brann lleva cuarenta anos arponeando. Hace mucho que no esta; a media vida de
Brann sale del suelo del Costillar como si fuera el mar (un cachalote fantasma, palido y medio transparente: el juego lo
dibuja a 3,1, unos diecisiete bloques) y embiste en linea recta. Al caer Brann sube al cielo y se va.

El cachalote: la cabeza enorme y cuadrada (un tercio del cuerpo) con la quijada estrecha y sus dientes, el ojo pequeno atras
de la cabeza, las aletas del pecho pequenas, la joroba del lomo, la cola que se estrecha y la aleta ancha. La piel arrugada en
surcos, cicatrices largas y blancas, percebes, y en el lomo las astas rotas de los arpones de Brann con sus cabos colgando.
Sin cinematica inversa: todo son giros (la cola sube y baja al nadar).
"""
from anim import Anim
from modelo import Box, Rig
from pinta import (claro, liso, mezcla, oscuro, por_cara, ruido, sombra)

NOMBRE = "vieja"
VARIANTES = ("vieja",)
OCULTAS = {0: []}
#: para las hojas de revision (en el juego la dibuja su propio dibujante, mucho mas grande)
ESCALA = 0.42
CENTRO_Y = 0.6

# ---------------------------------------------------------------- el modelo
# (pixeles; Y hacia abajo; la cabeza mira a -Z; el centro del cuerpo en la raiz)


def rig():
    r = Rig(NOMBRE, 256)
    r.pieza("raiz", "", (0, 24, 0))
    r.pieza("cuerpo", "raiz", (0, -12, 0), boxes=[
        Box(-9, -9, -14, 18, 18, 30, "cuerpo"),
        Box(-2.2, -11.4, 7, 4.4, 2.6, 7, "joroba"),
    ])
    # la cabeza: un cajon enorme, algo mas estrecho abajo (la frente, alta y roma)
    r.pieza("cabeza", "cuerpo", (0, 0, -14), boxes=[Box(-8.6, -9.6, -26, 17.2, 17, 26, "cabeza")])
    # la quijada, estrecha, bajo la cabeza (con los dientes arriba); se abre por detras
    r.pieza("mandibula", "cabeza", (0, 6.0, -5), boxes=[Box(-2.7, 0, -20.5, 5.4, 2.8, 21, "mandibula")])
    # las aletas del pecho
    r.pieza("aleta_d", "cuerpo", (-8.6, 5.5, -9), rot=(28, 0, 30), boxes=[Box(-1.4, 0, -2.6, 1.4, 10, 5.2, "aleta_pecho")])
    r.pieza("aleta_i", "cuerpo", (8.6, 5.5, -9), rot=(28, 0, -30), boxes=[Box(0, 0, -2.6, 1.4, 10, 5.2, "aleta_pecho")])
    # la cola en dos trozos y la aleta
    r.pieza("cola1", "cuerpo", (0, -0.8, 16), boxes=[Box(-7, -7, 0, 14, 14, 14, "cola1")])
    r.pieza("cola2", "cola1", (0, 0.4, 14), boxes=[Box(-4.5, -4.5, 0, 9, 9, 13, "cola2")])
    r.pieza("aleta", "cola2", (0, 0.4, 13), boxes=[Box(-15, -1, -1, 30, 2, 10, "aleta")])
    # los arpones de Brann clavados en el lomo (las astas rotas) y un cabo que cuelga
    r.pieza("arpon_1", "cuerpo", (3, -9, -2), rot=(-38, 0, 24), boxes=[Box(-0.5, -12, -0.5, 1, 12, 1, "asta_clavada")])
    r.pieza("arpon_2", "cuerpo", (-4, -9, 8), rot=(-24, 0, -30), boxes=[Box(-0.5, -9, -0.5, 1, 9, 1, "asta_clavada")])
    r.pieza("arpon_3", "cola1", (1.5, -7, 5), rot=(-46, 0, 10), boxes=[Box(-0.5, -7, -0.5, 1, 7, 1, "asta_clavada")])
    r.pieza("cabo", "arpon_1", (0, -11, 0), rot=(60, 0, 0), boxes=[Box(-0.3, 0, -0.3, 0.6, 13, 0.6, "cabo")])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas
# Palida (un gris azulado casi blanco), con los surcos de la piel mas oscuros, cicatrices blancas, percebes y el vientre
# mas claro; el ojo pequeno y la boca. En el juego va medio transparente y luce: es lo que queda de ella.

PIEL = ["#5d6d78", "#73848f", "#8a9ba6", "#a2b2bc", "#bccad2", "#d3dee4", "#e9f1f4"]
SURCO = "#4f5e69"
CICATRIZ = "#f4f9fb"
PERCEBE = ["#8b8a7a", "#a7a693", "#c3c1ab"]
BOCA = "#1b2228"
DIENTE = "#f6f4ea"
OJO, OJO_B = "#e8fbff", "#7fdcff"
ASTA = ["#4c3d2b", "#5f4c35", "#735d41"]
CABO = ["#5a4d36", "#74664a"]


_RAYAS = {}


def rayas(sem, cara, w, h):
    """Las cicatrices de una cara: unos pocos trazos cortos y torcidos (siempre los mismos)."""
    k = (sem, cara, w, h)
    if k not in _RAYAS:
        import numpy as np
        rng = np.random.default_rng(sem * 131 + len(cara) * 7 + w * 3 + h)
        px = set()
        for _ in range(max(1, (w * h) // 140)):
            x, y = float(rng.integers(0, w)), float(rng.integers(0, h))
            dx, dy = 1.0, float(rng.uniform(-0.8, 0.8))
            for _ in range(int(rng.integers(4, 9))):
                px.add((int(x), int(y)))
                x += dx
                y += dy
                dy += float(rng.uniform(-0.3, 0.3))
        _RAYAS[k] = px
    return _RAYAS[k]


def materiales(variante):
    piel = ruido(PIEL[2:6], 1201, grano=0.35, base=0.5)
    vientre = ruido(PIEL[4:7], 1202, grano=0.3, base=0.5)

    def surcos(cara, x, y, w, h, sem):
        c = (vientre if cara == "abajo" else piel)(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba"):
            # los surcos de la piel: rayas largas a lo largo, onduladas
            k = (y * 3 + (x // 5) + sem) % 7 if cara != "arriba" else (x * 3 + (y // 5) + sem) % 7
            if k == 0:
                c = mezcla(c, SURCO, 0.55)
            # cicatrices: unos pocos trazos blancos
            if (x, y) in rayas(sem, cara, w, h):
                return CICATRIZ
            # percebes en grupos
            if ((x // 3) * 7 + (y // 3) * 5 + sem) % 23 == 0 and (x + y) % 2 == 0:
                return PERCEBE[(x + y) % 3]
        if cara == "abajo" and (x + y * 3) % 11 == 0:
            return PIEL[5]
        return c

    def cuerpo(cara, x, y, w, h):
        return surcos(cara, x, y, w, h, 0)

    def cabeza(cara, x, y, w, h):
        c = surcos(cara, x, y, w, h, 3)
        if cara in ("der", "izq"):
            # el ojo pequeno, atras y abajo en el costado; la boca, una raya larga abajo
            d = (w - 1 - x) if cara == "der" else x
            if d >= w - 6 and d <= w - 5 and h - 7 <= y <= h - 6:
                return BOCA if d == w - 6 else OJO_B
            if y == h - 2 and d <= w - 8:
                return BOCA
        if cara == "frente":
            # la frente roma, con cicatrices de calamar (anillos)
            if (x * x + y * 3) % 29 == 0:
                return CICATRIZ
            return claro(c, 0.04)
        return c

    def mandibula(cara, x, y, w, h):
        c = vientre(cara, x, y, w, h)
        if cara == "arriba":
            # la fila de dientes a cada lado
            return DIENTE if (x in (0, w - 1) and y % 2 == 0) else BOCA
        return c

    def aleta_pecho(cara, x, y, w, h):
        return oscuro(piel(cara, x, y, w, h), 0.06 * (y / max(1, h - 1)))

    def cola(cara, x, y, w, h):
        return surcos(cara, x, y, w, h, 5)

    def aleta(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            # la aleta de la cola: ancha, con la muesca en el medio y los bordes rotos
            cx = abs(x - (w - 1) / 2.0)
            if cx < 1.0 and y >= h - 3:
                return None
            borde = h - 1 - int(2.4 * (cx / (w / 2.0)) ** 2)
            if y > borde:
                return None
            if y == borde and x % 4 == 0:
                return None
            return claro(c, 0.06) if cara == "abajo" else c
        return c

    def asta(cara, x, y, w, h):
        return ASTA[(x + y) % 3]

    def cabo(cara, x, y, w, h):
        return CABO[y % 2]

    return {
        "cuerpo": sombra(cuerpo, abajo=0.10, cantos=0.06),
        "joroba": sombra(cuerpo, abajo=0.10),
        "cabeza": sombra(cabeza, abajo=0.10, cantos=0.06),
        "mandibula": mandibula,
        "aleta_pecho": aleta_pecho,
        "cola1": sombra(cola, abajo=0.10), "cola2": sombra(cola, abajo=0.10),
        "aleta": aleta,
        "asta_clavada": asta, "cabo": cabo,
    }


def brillo(variante):
    """Lo que luce: el ojo y las cicatrices (una luz fria que le recorre la piel)."""
    def ojo(cara, x, y, w, h):
        if cara not in ("der", "izq"):
            return None
        d = (w - 1 - x) if cara == "der" else x
        if d == w - 5 and h - 7 <= y <= h - 6:
            return OJO
        return None

    def cicatrices(sem):
        def f(cara, x, y, w, h):
            if cara in ("der", "izq", "arriba") and (x, y) in rayas(sem, cara, w, h):
                return "#bff2ff"
            return None
        return f

    return {"cabeza": ojo, "cuerpo": cicatrices(0), "cola1": cicatrices(5), "cola2": cicatrices(5)}


# ---------------------------------------------------------------- las animaciones

def animaciones(r=None):
    r = r or rig()
    B, A = [], []

    def bucle(nombre, largo, poses):
        a = Anim(nombre, largo, bucle=True).poses(poses, r)
        B.append(a)
        return a

    # ---- nadar: la cola sube y baja (cada trozo un poco despues), el cuerpo se mece y las aletas reman
    pasos = []
    for k in range(9):
        t = k / 8.0
        import math
        f = math.sin(t * 2 * math.pi)
        f1 = math.sin(t * 2 * math.pi - 0.7)
        f2 = math.sin(t * 2 * math.pi - 1.4)
        pasos.append((2.4 * t, {
            "cuerpo": (2.0 * f, 0, 0),
            "cabeza": (-1.0 * f, 0, 0),
            "cola1": (7.0 * f, 0, 0),
            "cola2": (12.0 * f1, 0, 0),
            "aleta": (18.0 * f2, 0, 0),
            "aleta_d": (28 + 8 * f, 0, 30 + 6 * f1),
            "aleta_i": (28 + 8 * f, 0, -30 - 6 * f1),
            "mandibula": (5.0 + 2.0 * f, 0, 0),
            "cabo": (60 + 10 * f1, 0, 6 * f2),
        }))
    bucle("nadar", 2.4, pasos)
    # ---- la boca abierta (al salir y al embestir: se pone encima de nadar)
    bucle("boca", 2.0, [(0.0, {"mandibula": (24, 0, 0)}), (1.0, {"mandibula": (30, 0, 0)}), (2.0, {"mandibula": (24, 0, 0)})])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision

def variante_de(nombre):
    return 0


def reposo(v, anims):
    return anims["nadar"].muestra(0)


def base_de(nombre, anims):
    if nombre == "boca":
        return [anims["nadar"]]
    return []


TABLA_EXTRA = ()
