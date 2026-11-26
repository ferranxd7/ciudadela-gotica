"""
Los Helados de las Montanas Nevadas (parte 7k): los monjes y peregrinos del Monasterio de la Helada que se quedaron
helados rezando cuando el cielo se puso rojo; la Sombra los desperto dentro del hielo. Piel azul gris con escarcha, la barba
y las cejas blancas de hielo, los ojos de un azul blanco que luce (capa de brillo), carambanos colgando de los brazos y la
ropa tiesa de hielo. Tres:

  0 monje: habito con capucha y escapulario, y en la derecha un incensario de hierro en su cadena del que sale un vaho que
    congela (luce). Incensario (un golpe en arco) y aliento (echa delante un soplo que congela).
  1 penitente: el torso al aire con cadenas cruzadas, cristales de hielo que le crecen en la espalda y un martillo de hielo
    a dos manos. Martillazo (de arriba abajo: brotan puas de hielo delante) y barrido.
  2 peregrino: capa de viaje, sombrero de ala ancha con escarcha y un baston con un cristal de hielo en la punta (luce).
    Bastonazo (de punta) y esquirlas (alza el baston y lanza tres esquirlas de hielo).

Esperan de rodillas helados (rezando, postura REZA: al acercarte rompen el hielo y se levantan) o enterrados en la nieve
(TUMBADO: se alzan de golpe).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "helado"
VARIANTES = ("monje", "penitente", "peregrino")
ESCALA = 1.1

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), cabeza=(7, 8, 7), brazo=(3, 14, 3), pierna=(4, 12, 4), hombro=0.6)
    # los carambanos que cuelgan de los antebrazos y de la barbilla (barba de hielo)
    for n in ("d", "i"):
        r.pieza(f"carambano_{n}", f"antebrazo_{n}", (0, 3, 0), boxes=[
            Box(-1.4, 0, 1.6, 0.8, 5, 0, "carambano"),
            Box(0.4, 0, 1.6, 0.8, 3.5, 0, "carambano"),
        ])
    r.pieza("barba", "cabeza", (0, 0, -3.6), boxes=[Box(-2.5, -1.5, -0.2, 5, 5.5, 0.8, "barba")])
    # ---- 0, el monje: la capucha, el escapulario y los faldones del habito; el incensario en su cadena
    r.pieza("capucha", "cabeza", (0, -8, 0), boxes=[
        Box(-4.0, -0.8, -4.2, 8.0, 1.0, 8.6, "capucha_arriba"),
        Box(-4.0, -0.8, -4.2, 0.8, 8.6, 8.6, "capucha_lado"),
        Box(3.2, -0.8, -4.2, 0.8, 8.6, 8.6, "capucha_lado"),
        Box(-4.0, -0.8, 3.6, 8.0, 9.6, 0.8, "capucha_nuca"),
    ])
    r.pieza("escapulario", "torso", (0, -12, 0), boxes=[
        Box(-2.5, 0, -2.9, 5, 14, 0.4, "escapulario"),
        Box(-2.5, 0, 2.5, 5, 14, 0.4, "escapulario"),
    ])
    for n in ("d", "i"):
        r.pieza(f"habito_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.7, -0.6, -2.7, 5.4, 7.6, 5.4, f"habito_{n}")])
        r.pieza(f"habito_bajo_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-2.6, 0, -2.6, 5.2, 6.4, 5.2, f"habito_bajo_{n}")])
    # el incensario cuelga de la mano en su cadena (la pieza gira en la mano: asi se voltea)
    r.pieza("incensario", "mano_d", (0, 0, 0), boxes=[
        Box(-0.4, 0, -0.4, 0.8, 5.0, 0.8, "cadena"),
        Box(-1.0, 4.5, -1.0, 2.0, 0.6, 2.0, "incensario_tapa"),
        Box(-1.8, 5.0, -1.8, 3.6, 3.2, 3.6, "incensario_bola"),
        Box(-1.0, 8.2, -1.0, 2.0, 0.8, 2.0, "incensario_tapa"),
    ])
    # ---- 1, el penitente: las cadenas cruzadas, los cristales de la espalda y el martillo de hielo
    r.pieza("cadenas", "torso", (0, 0, 0), boxes=[Box(-4.4, -2.2, -2.9, 8.8, 1.2, 5.8, "cadenas")])
    for n, a in (("a", 38), ("b", -38)):
        r.pieza(f"cadena_{n}", "torso", (0, -6.5, 0), rot=(0, 0, a), boxes=[Box(-0.6, -6.4, -2.9, 1.2, 12.8, 5.8, "cadenas")])
    r.pieza("cristales", "torso", (0, -9, 2.5), boxes=[
        Box(-3.5, -4.0, 0, 2.0, 5.0, 2.0, "cristal"),
        Box(-0.5, -5.5, 0.4, 2.2, 6.5, 2.2, "cristal"),
        Box(2.0, -3.0, 0, 1.8, 4.0, 1.8, "cristal"),
    ])
    r.pieza("martillo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.8, -0.8, -20, 1.6, 1.6, 25, "mango"),
        Box(-3.0, -3.5, -26, 6.0, 7.0, 6.5, "hielo_martillo"),
        Box(-1.0, -1.0, 5, 2.0, 2.0, 1.5, "mango_pomo"),
    ])
    # ---- 2, el peregrino: el sombrero de ala ancha, la capa de viaje y el baston con su cristal
    r.pieza("sombrero", "cabeza", (0, -8, 0), boxes=[
        Box(-6.0, -0.6, -6.0, 12.0, 1.0, 12.0, "ala"),
        Box(-3.8, -4.2, -3.8, 7.6, 3.8, 7.6, "copa"),
    ])
    r.pieza("capa_viaje", "torso", (0, -12, 2.7), rot=(4, 0, 0), boxes=[Box(-5.0, 0, 0, 10.0, 19, 0.8, "capa_viaje")])
    r.pieza("baston", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -22, 1.2, 1.2, 36, "vara"),
        Box(-1.6, -1.6, -26, 3.2, 3.2, 4.2, "cristal_baston"),
    ])
    r.empaquetar()
    return r


MONJE = ["capucha", "escapulario", "habito_d", "habito_i", "habito_bajo_d", "habito_bajo_i", "incensario"]
PENITENTE = ["cadenas", "cadena_a", "cadena_b", "cristales", "martillo"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: PENITENTE + ["sombrero", "capa_viaje", "baston"],
    1: MONJE + ["sombrero", "capa_viaje", "baston"],
    2: PENITENTE + ["escapulario", "incensario", "capucha"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#27323a", "#36444f", "#4a5c69", "#617786", "#7d93a1", "#9fb3bf"]
ESCARCHA = ["#b7c9d3", "#d3e1e8", "#eef6fa", "#ffffff"]
HIELO = ["#3d6f8a", "#5a92ad", "#7fb6cd", "#a9d6e6", "#d6f0f8"]
HABITO = ["#1b1f25", "#252a32", "#30363f", "#3c434d", "#4a525d"]
LANA = ["#2d2a26", "#3a3631", "#48433d", "#57524a", "#68625a"]
CUERO = ["#191411", "#241d18", "#30271f", "#3d3228"]
HIERRO = ["#151518", "#24242a", "#36363e", "#4b4b55", "#63636e", "#80808b"]
MADERA = ["#2a1f15", "#38291c", "#473524", "#57412c"]
OJO = ["#3fa9d8", "#8fe0ff", "#e6fbff"]
VAHO = ["#5fbfe0", "#a6e6f7", "#e9fbff"]


CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _azar01(*v):
    """Un numero de 0 a 1 que solo depende de v (sin el hash de Python, que cambia de una ejecucion a otra)."""
    h = 2166136261
    for n in v:
        h = ((h ^ (int(n) & 0xffffffff)) * 16777619) & 0xffffffff
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    h ^= h >> 15
    return h / 4294967296.0


def materiales(variante):
    piel = ruido(PIEL[1:5], 1011, grano=0.45, base=0.55)
    habito_t = tela(HABITO, 1012, pliegue=4, grano=0.18, luz=0.2)
    lana_t = tela(LANA, 1013, pliegue=4, grano=0.2, luz=0.2)
    cuero_t = ruido(CUERO, 1014, grano=0.5)
    ropa_t = habito_t if variante == 0 else lana_t

    def helado(c, cara, x, y, w, h, cuanto=0.18, sal=0):
        """Escarcha a motas sueltas y a grumos (mas arriba y en la cara de arriba): las claras sobre el color, alguna blanca."""
        kc = CARAS[cara]
        s0 = w * 31 + h * 17 + sal * 101
        g = 0.55 * _azar01(s0, kc, x // 2, y // 2) + 0.45 * _azar01(s0 + 7, kc, x, y)
        alto = 1.0 - y / max(1, h - 1)
        p = 0.55 if cara == "arriba" else cuanto * (0.45 + alto)
        if g < p * 0.5:
            return ESCARCHA[1 + int(_azar01(s0 + 3, kc, x, y) * 2.99)]
        if g < p:
            return mezcla(c, ESCARCHA[0], 0.45)
        return c

    def cabeza(cara, x, y, w, h):
        c = helado(piel(cara, x, y, w, h), cara, x, y, w, h, 0.10, 1)
        if cara == "frente":
            C = {"K": "#0b1116", "s": PIEL[1], "o": PIEL[2], "c": PIEL[3], "W": ESCARCHA[2], "w": ESCARCHA[1]}
            v = dibujo([
                "wWWwWWw",
                "occccco",
                "oKKcKKo",
                "ocKcKco",
                "oscccso",
                "ossKsso",
                "oosssoo",
                "oooooo.",
            ], C)(cara, x, y, w, h)
            return v if v is not None else c
        return c

    def torso(cara, x, y, w, h):
        if variante == 1:
            # el penitente: el torso al aire, con las costillas marcadas y escarcha
            c = helado(piel(cara, x, y, w, h), cara, x, y, w, h, 0.2)
            if cara == "frente" and y % 3 == 1 and 1 <= x <= w - 2 and y < 8:
                return oscuro(c, 0.25)
            return c
        t = habito_t if variante == 0 else lana_t
        return helado(t(cara, x, y, w, h), cara, x, y, w, h)

    def brazo(cara, x, y, w, h):
        if variante == 1:
            return helado(piel(cara, x, y, w, h), cara, x, y, w, h, 0.2)
        t = habito_t if variante == 0 else lana_t
        return helado(t(cara, x, y, w, h), cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 3 or variante == 1:
            return helado(piel(cara, x, y, w, h), cara, x, y, w, h, 0.25)
        return brazo(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        if variante == 1:
            return helado(cuero_t(cara, x, y, w, h), cara, x, y, w, h)
        t = habito_t if variante == 0 else lana_t
        return helado(t(cara, x, y, w, h), cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        if y >= h - 2:
            return CUERO[1]
        return muslo(cara, x, y + 6, w, h)

    def carambano(cara, x, y, w, h):
        if y >= h - 1 and x % 2:
            return None
        return HIELO[4 - min(4, y)] if y < 5 else HIELO[0]

    def barba(cara, x, y, w, h):
        if y >= h - 1 and x % 2 == 0:
            return None
        return ESCARCHA[(x + y) % 3] if y < 3 else HIELO[3 + (x % 2)]

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return helado(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.25)

    def escapulario(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return HABITO[1]
        if y >= h - 1 and x % 2:
            return None
        c = tela(["#2a1416", "#3a1b1e", "#4d2428", "#5e2d31"], 1015, pliegue=3)(cara, x, y, w, h)
        if y == 2 and 1 <= x <= w - 2:
            return "#8a7a3a"        # el bordado de la cruz
        if x == w // 2 and 1 <= y <= 4:
            return "#8a7a3a"
        return helado(c, cara, x, y, w, h)

    def habito(cara, x, y, w, h):
        return helado(ropa_t(cara, x, y, w, h), cara, x, y, w, h)

    def habito_bajo(cara, x, y, w, h):
        if cara == "abajo":
            return HABITO[0]
        if y >= h - 1 and (x + len(cara)) % 3 == 0:
            return None
        c = ropa_t(cara, x, y + 7, w, h)
        if y >= h - 2:
            return ESCARCHA[(x + y) % 2]      # el borde tieso de escarcha
        return helado(c, cara, x, y, w, h)

    def cadenas(cara, x, y, w, h):
        return HIERRO[4] if (x + y) % 2 == 0 else HIERRO[2]

    def cristal(cara, x, y, w, h):
        return HIELO[min(4, 1 + (x + y * 2) % 4)] if cara != "abajo" else HIELO[0]

    def ala(cara, x, y, w, h):
        return helado(LANA[2] if cara == "arriba" else LANA[1], cara, x, y, w, h, 0.2, 2)

    def copa(cara, x, y, w, h):
        c = lana_t(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return "#4d2428"                          # la cinta
        return helado(c, cara, x, y, w, h, 0.12, 3)

    def capa_viaje(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return LANA[1]
        if y >= h - 2 and (x * 3 + y) % 4 == 0:
            return None
        return helado(lana_t(cara, x, y, w, h), cara, x, y, w, h, 0.25)

    mats = {
        "cabeza": cabeza,
        "torso": sombra(torso),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12), "antebrazo_i": sombra(antebrazo, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.15), "pierna_i": sombra(pierna, abajo=0.15),
        "carambano": carambano,
        "barba": barba,
        "capucha_arriba": sombra(capucha, abajo=0.1),
        "capucha_lado": sombra(capucha, abajo=0.15),
        "capucha_nuca": sombra(capucha, abajo=0.2),
        "escapulario": escapulario,
        "habito_d": sombra(habito, abajo=0.12), "habito_i": sombra(habito, abajo=0.12),
        "habito_bajo_d": sombra(habito_bajo, abajo=0.05), "habito_bajo_i": sombra(habito_bajo, abajo=0.05),
        "cadena": lambda cara, x, y, w, h: HIERRO[3] if y % 2 else HIERRO[1],
        "incensario_tapa": metal(HIERRO[1:5], 1021),
        "incensario_bola": metal(HIERRO[0:4], 1022),
        "cadenas": cadenas,
        "cristal": cristal,
        "mango": madera(MADERA, 1023, "z"),
        "mango_pomo": metal(HIERRO[1:4], 1024),
        "hielo_martillo": lambda cara, x, y, w, h: HIELO[1 + (x * 3 + y * 5) % 4] if (x + y) % 5 else HIELO[0],
        "ala": ala,
        "copa": copa,
        "capa_viaje": capa_viaje,
        "vara": madera(MADERA, 1025, "z"),
        "cristal_baston": lambda cara, x, y, w, h: HIELO[2 + (x + y) % 3],
    }
    return mats


def brillo(variante):
    """Lo que luce: los ojos; el vaho del incensario del monje y el cristal del baston del peregrino."""
    C = {"a": OJO[0], "b": OJO[1], "c": OJO[2]}
    ojos = dibujo([
        ".......",
        ".......",
        ".cb.bc.",
        "..a.a..",
    ], C)
    b = {"cabeza": por_cara(liso(None), frente=ojos)}
    if variante == 0:
        b["incensario_bola"] = lambda cara, x, y, w, h: VAHO[(x + y) % 3] if (x + y) % 2 == 0 and 0 < y < h - 1 else None
    if variante == 2:
        b["cristal_baston"] = lambda cara, x, y, w, h: VAHO[1 + (x + y) % 2]
    if variante == 1:
        b["cristal"] = lambda cara, x, y, w, h: VAHO[0] if (x * 3 + y) % 4 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14: llega
# a unos 11). Las animaciones de "porte" llevan el brazo derecho entero (el quieto no lo toca): se suman al quieto.

# de pie: tieso, encogido de frio (el brazo derecho lo pone el porte de cada uno)
BASE = dict(raiz=raiz(-0.2), torso=(8, 0, 0), cabeza=(6, 0, 0), muslo_d=(-3, 0, 3), pierna_d=(4, 0, 0), muslo_i=(2, 0, -3),
            pierna_i=(3, 0, 0), brazo_i=(-6, 0, -8), antebrazo_i=(-16, 0, 0))
# lo que lleva cada uno: el incensario colgando a su lado, el martillo al hombro, el baston de pie (apoyado en el suelo)
PORTE_0 = dict(brazo_d=(-14, 0, 10), antebrazo_d=(-12, 0, 0), incensario=(18, 0, -10))
PORTE_1 = dict(_mano_d=("h", -1.0, 1.0, 4.0), _arma=(0.05, 0.6, -0.8))
PORTE_2 = dict(_mano_d=("h", -1.5, -7.5, 4.0), _arma=(0.0, 1.0, 0.08))
GUARDIA = dict(raiz=raiz(-1.2), torso=(20, 10, 0), cabeza=(-12, -8, 0), muslo_d=(-18, 0, 6), pierna_d=(28, 0, 0),
               muslo_i=(-6, 0, -6), pierna_i=(22, 0, 0), brazo_i=(-30, 0, -20), antebrazo_i=(-50, 0, 0))
LISTO_0 = con(GUARDIA, brazo_d=(-40, 0, 20), antebrazo_d=(-30, 0, 0), incensario=(50, 0, -20))
LISTO_1 = con(GUARDIA, _mano_d=("h", 3.0, -7.0, 5.0), _arma=(0.25, 0.45, 0.85), _agarra=-4)
LISTO_2 = con(GUARDIA, _mano_d=("h", 1.0, -6.0, 7.0), _arma=(0.1, 0.5, 0.86), _agarra=-6)
# de rodillas, helado rezando: la mano izquierda al pecho, la cabeza baja (la derecha, con lo suyo: REZANDO de cada uno)
RODILLAS = dict(raiz=raiz(-6.0, 0.8), torso=(18, 0, 0), cabeza=(30, 0, 0),
                muslo_d=(-10, 0, 5), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -5), pierna_i=(96, 0, 0),
                brazo_d=(-20, 0, 10), antebrazo_d=(-10, 0, 0), incensario=(12, 0, -10),
                brazo_i=(-50, 0, -30), antebrazo_i=(-95, 0, 0))
REZANDO = {
    0: RODILLAS,
    1: con(RODILLAS, _mano_d=("h", -1.0, 1.0, 4.0), _arma=(0.05, 0.6, -0.8)),
    2: con(RODILLAS, _mano_d=("h", -2.0, -2.5, 5.0), _arma=(0.0, 1.0, 0.05)),
}
# enterrado en la nieve: boca arriba, rigido, los brazos a los lados
ENTERRADO = dict(raiz=raiz(2.6, -14.0, rot=(-90, 0, 0)), torso=(0, 0, 0), cabeza=(-6, 0, 0),
                 muslo_d=(0, 0, 4), pierna_d=(4, 0, 0), muslo_i=(0, 0, -4), pierna_i=(4, 0, 0),
                 brazo_d=(0, 0, 10), antebrazo_d=(-6, 0, 0), brazo_i=(0, 0, -10), antebrazo_i=(-6, 0, 0))
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")


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

    # ---- quieto: tirita (pequenas sacudidas)
    bucle("quieto", 3.0, [
        (0.0, BASE),
        (0.6, con(BASE, torso=(9, 1, 1), cabeza=(7, 2, 0))),
        (1.2, con(BASE, torso=(8, -1, -1), cabeza=(5, -2, 1))),
        (1.5, BASE),
        (2.4, con(BASE, torso=(6, 0, 0), cabeza=(2, 0, 0), raiz=raiz(0.0))),
        (3.0, BASE),
    ])
    # ---- andar: pasos cortos y rigidos (las rodillas casi sin doblar)
    pasos = []
    for k, (md, mi, pd, pi_, ty) in enumerate(((-22, 18, 6, 14, 5), (-4, 2, 4, 26, 0), (18, -22, 14, 6, -5), (2, -4, 26, 4, 0), (-22, 18, 6, 14, 5))):
        pasos.append((1.3 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (2, ty, 0), "cabeza": (0, -ty * 0.5, 0), "raiz": raiz(0.6 if k % 2 else 0.0),
                                    "brazo_i": (-mi * 0.4, 0, 0)}))
    bucle("andar", 1.3, pasos)
    q0 = con(BASE, PORTE_0)
    bucle("porte_monje", 3.0, [(0.0, q0), (1.5, con(q0, incensario=(24, 0, -6))), (3.0, q0)], solo=BRAZO_D + ("incensario",))
    q1 = fija(con(BASE, PORTE_1))
    bucle("porte_penitente", 3.0, [(0.0, q1), (3.0, q1)], solo=BRAZO_D)
    q2 = fija(con(BASE, PORTE_2))
    bucle("porte_peregrino", 3.0, [(0.0, q2), (3.0, q2)], solo=BRAZO_D)
    bucle("listo_monje", 2.0, [(0.0, LISTO_0), (1.0, con(LISTO_0, incensario=(40, 0, -10), raiz=raiz(-1.5))), (2.0, LISTO_0)])
    l1 = fija(LISTO_1)
    bucle("listo_penitente", 2.0, [(0.0, l1), (1.0, fija(con(LISTO_1, raiz=raiz(-1.6), torso=(24, 8, 0)))), (2.0, l1)])
    l2 = fija(LISTO_2)
    bucle("listo_peregrino", 2.0, [(0.0, l2), (1.0, fija(con(LISTO_2, raiz=raiz(-1.5), torso=(22, 12, 0)))), (2.0, l2)])
    # ---- helado de rodillas, rezando (casi quieto: la cabeza baja un poco mas y vuelve)
    for v in range(3):
        rz = fija(REZANDO[v])
        bucle(f"rezando_{VARIANTES[v]}", 6.0, [(0.0, rz), (3.0, con(rz, cabeza=(33, 0, 0))), (6.0, rz)])
    # ---- enterrado en la nieve
    bucle("enterrado", 6.0, [(0.0, ENTERRADO), (6.0, ENTERRADO)])

    # ================================================================ acciones

    # ---- 1 incensario (el monje): lo voltea de su derecha a su izquierda por delante, en arco
    I_ATRAS = con(LISTO_0, raiz=raiz(-1.0, 1.0), torso=(10, 50, 0), cabeza=(-8, -40, 0), brazo_d=(-20, 0, 75), antebrazo_d=(-15, 0, 0),
                  incensario=(10, 0, -20))
    I_DELANTE = con(LISTO_0, raiz=raiz(-1.8, -1.2), torso=(18, -10, 0), cabeza=(-14, 6, 0), brazo_d=(-95, 0, 5), antebrazo_d=(-5, 0, 0),
                    incensario=(0, 0, 0))
    I_FIN = con(LISTO_0, raiz=raiz(-1.6, -1.6), torso=(16, -50, 0), cabeza=(-10, 40, 0), brazo_d=(-80, -30, -40), antebrazo_d=(-10, 0, 0),
                incensario=(20, 0, 30))
    accion("incensario", 1.6, [
        (0.00, LISTO_0),
        (0.50, I_ATRAS),
        (0.72, con(I_ATRAS, torso=(10, 56, 0))),
        (0.80, I_DELANTE),
        (0.90, I_FIN),
        (1.20, con(I_FIN, torso=(14, -54, 0))),
        (1.60, LISTO_0),
    ], golpes=(0.82,))
    # ---- 2 aliento (el monje): se echa atras tomando aire y sopla delante (la cabeza adelantada, los brazos abiertos)
    A_TOMA = con(LISTO_0, raiz=raiz(-0.4, 1.4), torso=(-12, 0, 0), cabeza=(-30, 0, 0), brazo_d=(-20, 0, 40), antebrazo_d=(-10, 0, 0),
                 brazo_i=(-20, 0, -40), incensario=(40, 0, -40))
    A_SOPLA = con(LISTO_0, raiz=raiz(-1.6, -1.8), torso=(34, 0, 0), cabeza=(-24, 0, 0), brazo_d=(-30, 0, 50), antebrazo_d=(-20, 0, 0),
                  brazo_i=(-30, 0, -50), antebrazo_i=(-20, 0, 0), incensario=(16, 0, -50))
    accion("aliento", 2.2, [
        (0.00, LISTO_0),
        (0.60, A_TOMA),
        (0.85, A_SOPLA),
        (1.40, con(A_SOPLA, torso=(36, 3, 0), cabeza=(-26, -4, 0))),
        (1.70, con(A_SOPLA, torso=(32, -3, 0), cabeza=(-22, 4, 0))),
        (2.20, LISTO_0),
    ], golpes=(0.85,))
    # ---- 3 martillazo (el penitente): lo alza por encima de la cabeza y lo descarga delante (brotan puas de hielo)
    M_ALTO = con(sin(LISTO_1, "_agarra"), raiz=raiz(0.0, 1.6), torso=(-14, 0, 0), cabeza=(-20, 0, 0), muslo_d=(12, 0, 6), pierna_d=(10, 0, 0),
                 muslo_i=(-14, 0, -6), pierna_i=(10, 0, 0), _mano_d=("h", 2.0, 9.6, 0.0), _arma=(0.0, 0.55, -0.85), _agarra=3)
    M_ABAJO = con(sin(LISTO_1, "_agarra"), raiz=raiz(-3.8, -3.2), torso=(50, 0, 0), cabeza=(-30, 0, 0), muslo_d=(18, 0, 6), pierna_d=(34, 0, 0),
                  muslo_i=(-54, 0, -6), pierna_i=(58, 0, 0), _mano_d=("h", 2.5, -4.5, 8.8), _arma=(0.0, -0.8, 0.6), _agarra=3)
    accion("martillazo", 2.2, [
        (0.00, LISTO_1),
        (0.75, M_ALTO),
        (1.00, con(M_ALTO, torso=(-18, 0, 0))),
        (1.08, con(sin(LISTO_1, "_agarra"), raiz=raiz(-1.6, -1.0), torso=(18, 0, 0), _mano_d=("h", 2.5, 4.5, 8.8), _arma=(0.0, 0.3, 1),
                   _agarra=3)),
        (1.15, M_ABAJO),
        (1.70, con(M_ABAJO, torso=(48, 0, 0))),
        (2.20, LISTO_1),
    ], golpes=(1.14,))
    # ---- 4 barrido (el penitente): lo echa atras a su derecha y barre a media altura
    B_ATRAS = con(sin(LISTO_1, "_agarra"), raiz=raiz(-1.0, 1.2), torso=(8, 58, 0), cabeza=(-6, -48, 0), brazo_i=(-60, 0, -30), antebrazo_i=(-30, 0, 0),
                  _mano_d=("h", -5.0, -2.0, -3.0), _arma=(-1, 0.1, -0.3))
    B_DELANTE = con(sin(LISTO_1, "_agarra"), raiz=raiz(-2.0, -1.6), torso=(18, -6, 0), cabeza=(-14, 4, 0),
                    _mano_d=("h", 2.0, -1.0, 9.8), _arma=(0.1, 0.0, 1), _agarra=3)
    B_FIN = con(sin(LISTO_1, "_agarra"), raiz=raiz(-1.8, -2.0), torso=(16, -58, 0), cabeza=(-10, 46, 0), brazo_i=(-20, 0, -60),
                _mano_d=("h", 8.0, -1.0, 6.0), _arma=(1, 0.0, 0.2))
    accion("barrido", 1.8, [
        (0.00, LISTO_1),
        (0.58, B_ATRAS),
        (0.82, con(B_ATRAS, torso=(8, 64, 0))),
        (0.92, B_DELANTE),
        (1.00, B_FIN),
        (1.35, con(B_FIN, torso=(14, -62, 0))),
        (1.80, LISTO_1),
    ], golpes=(0.94,))
    # ---- 5 bastonazo (el peregrino): recoge el baston y lo clava de punta delante
    S_ATRAS = con(sin(LISTO_2, "_agarra"), raiz=raiz(-1.0, 1.4), torso=(6, 28, 0), cabeza=(-8, -20, 0),
                  _mano_d=("h", -2.0, 1.0, -4.0), _arma=(0.1, 0.1, 1), _agarra=-6)
    S_LANZA = con(sin(LISTO_2, "_agarra"), raiz=raiz(-2.2, -2.4), torso=(28, -12, 0), cabeza=(-22, 8, 0), muslo_i=(-40, 0, -6), pierna_i=(44, 0, 0),
                  _mano_d=("h", 0.5, 0.5, 10.2), _arma=(0.04, 0.0, 1), _agarra=5)
    accion("bastonazo", 1.4, [
        (0.00, LISTO_2),
        (0.45, S_ATRAS),
        (0.62, S_LANZA),
        (0.90, S_LANZA),
        (1.40, LISTO_2),
    ], golpes=(0.62,))
    # ---- 6 esquirlas (el peregrino): alza el baston por encima de la cabeza y lo baja apuntando: salen tres esquirlas
    E_ALZA = con(sin(LISTO_2, "_agarra"), raiz=raiz(-0.2, 0.8), torso=(-10, 0, 0), cabeza=(-24, 0, 0), brazo_i=(-30, 0, -40),
                 _mano_d=("h", -1.0, 10.2, 1.0), _arma=(0.0, 1, 0.1))
    E_APUNTA = con(sin(LISTO_2, "_agarra"), raiz=raiz(-1.4, -1.0), torso=(18, 0, 0), cabeza=(-14, 0, 0), brazo_i=(-30, 0, -30),
                   _mano_d=("h", 0.0, 2.0, 9.9), _arma=(0.0, 0.05, 1))
    accion("esquirlas", 2.0, [
        (0.00, LISTO_2),
        (0.55, E_ALZA),
        (0.85, con(E_ALZA, torso=(-14, 0, 0))),
        (0.95, E_APUNTA),
        (1.40, E_APUNTA),
        (2.00, LISTO_2),
    ], golpes=(0.95,))
    # ---- 7 aturdido
    AT = dict(raiz=raiz(-1.4, 2.0), torso=(-10, 0, 8), cabeza=(-22, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(18, 0, 0), brazo_d=(16, 0, 26), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 8 morir: se tambalea, cae de rodillas y se queda helado, vencido hacia delante (el servidor lo rompe en esquirlas)
    VENCIDO = con(RODILLAS, torso=(46, 0, 6), cabeza=(40, 0, 10), brazo_d=(-6, 0, 12), antebrazo_d=(-4, 0, 0), brazo_i=(-8, 0, -12),
                  antebrazo_i=(-6, 0, 0), incensario=(0, 0, -12))
    accion("morir", 2.4, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0))),
        (0.55, con(RODILLAS, torso=(4, 0, 0), cabeza=(-10, 0, 0))),
        (1.20, VENCIDO),
        (2.40, con(VENCIDO, torso=(48, 0, 6))),
    ])
    # ---- 9 levantarse: estaba de rodillas helado; rompe el hielo (una sacudida) y se pone en pie
    accion("levantarse", 1.6, [
        (0.00, RODILLAS),
        (0.40, con(RODILLAS, torso=(10, 0, 0), cabeza=(-20, 0, 0), brazo_d=(-30, 0, 60), brazo_i=(-30, 0, -60), antebrazo_d=(-20, 0, 0),
                   antebrazo_i=(-20, 0, 0))),
        (0.55, con(RODILLAS, torso=(6, 0, 0), cabeza=(-24, 0, 0), brazo_d=(-10, 0, 80), brazo_i=(-10, 0, -80), antebrazo_d=(-10, 0, 0),
                   antebrazo_i=(-10, 0, 0))),
        (1.00, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0), brazo_d=(-30, 0, 20), antebrazo_d=(-30, 0, 0))),
        (1.60, con(GUARDIA, brazo_d=(-30, 0, 16), antebrazo_d=(-30, 0, 0))),
    ])
    # ---- 10 alzarse: enterrado en la nieve, se incorpora de golpe
    accion("alzarse", 1.6, [
        (0.00, ENTERRADO),
        (0.45, con(ENTERRADO, raiz=raiz(3.0, -10.0, rot=(-50, 0, 0)), brazo_d=(-30, 0, 30), brazo_i=(-30, 0, -30))),
        (0.85, con(RODILLAS, torso=(-10, 0, 0), cabeza=(-20, 0, 0))),
        (1.25, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0), brazo_d=(-30, 0, 20), antebrazo_d=(-30, 0, 0))),
        (1.60, con(GUARDIA, brazo_d=(-30, 0, 16), antebrazo_d=(-30, 0, 0))),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "penitente" in nombre or nombre in ("martillazo", "barrido"):
        return 1
    if "peregrino" in nombre or nombre in ("bastonazo", "esquirlas"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_monje"]]
    return []
