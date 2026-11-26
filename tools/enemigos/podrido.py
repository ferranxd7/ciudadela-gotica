"""
El podrido del Pantano Podrido (parte 7i): los que se ahogaron en el pantano y la Sombra hincho de agua negra. Hinchado,
la piel gris verdosa con venas oscuras y sanguijuelas, el barro hasta la cintura, algas colgando de la cabeza y los
hombros, la boca abierta que chorrea agua negra y los ojos de un amarillo enfermo. Esperan hundidos en el agua, como un
tronco mas (solo asoma la espalda), y se alzan al acercarse. Tres:
  0  el podrido   (las manos desnudas: zarpazo, mordisco a la carrera y el vomito de agua negra, que envenena)
  1  el fatuo     (lleva en la mano izquierda un fuego verde de pantano: golpe con el y lo lanza; donde cae, gas que envenena)
  2  el hinchado  (la panza enorme a punto de reventar: se deja caer encima de quien tiene delante y eructa una nube de gas;
                   al morir revienta)
"""
from anim import Anim, con, raiz, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide
from pinta import (brillo_de, claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido,  # noqa: F401
                   sombra, tela)

NOMBRE = "podrido"
VARIANTES = ("podrido", "fatuo", "hinchado")
ESCALA = 1.12

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # hinchado: el torso ancho y hondo, piernas cortas, brazos largos
    humanoide(r, torso=(9, 12, 6), cabeza=(7, 7, 7), brazo=(3, 15, 3), pierna=(4, 11, 4))
    # la barriga hinchada (0 y 1), que cuelga por delante
    r.pieza("barriga", "torso", (0, -5, -3), boxes=[Box(-4, -3.5, -2.5, 8, 7, 3, "barriga", grow=g(0.15))])
    # la panza del hinchado (2): enorme, por delante y a los lados
    r.pieza("panza", "torso", (0, -5, -1), boxes=[Box(-5.5, -5.5, -6.5, 11, 10, 9, "panza")])
    # el barro y los harapos de la cintura a las rodillas
    r.pieza("harapo", "cadera", (0, 0, 0), boxes=[Box(-5, -0.5, -3.5, 10, 7, 7, "harapo", grow=g(0.3))])
    # las algas: cuelgan de la cabeza (por detras y los lados) y de los hombros
    r.pieza("algas_cabeza", "cabeza", (0, -7, 0), boxes=[
        Box(-3.6, -0.3, 3.7, 7.2, 7, 0, "algas"),
        Box(3.7, -0.3, -2.0, 0, 6, 5.5, "algas"),
        Box(-3.7, -0.3, -2.0, 0, 6, 5.5, "algas"),
    ])
    for lado, s in (("d", -1), ("i", 1)):
        r.pieza(f"algas_{lado}", f"brazo_{lado}", (0, -2, 0), boxes=[Box(-1.8, -0.4, -1.8, 3.6, 5, 3.6, "algas_hombro", grow=g(0.2))])
    # el fuego fatuo (1): una llama verde en la palma de la mano izquierda
    r.pieza("fatuo", "mano_i", (0, 0.5, -1.5), boxes=[Box(-1.5, -1.5, -3.0, 3, 3, 3, "fatuo")])
    r.pieza("fatuo_halo", "fatuo", (0, 0, -1.5), boxes=[Box(-2.2, -2.2, -2.2, 4.4, 4.4, 4.4, "fatuo_halo")])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {
    0: ["panza", "fatuo", "fatuo_halo"],
    1: ["panza"],
    2: ["barriga", "fatuo", "fatuo_halo"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#2a332c", "#3d4a3f", "#536253", "#6b7b69", "#869483", "#a2ad9c"]
VENA = ["#2d2638", "#3c3149"]
MANCHA = ["#323b2a", "#414c35", "#55553a"]
BARRO = ["#15110c", "#1f1912", "#2a2218", "#362c20", "#433728"]
HARAPO = ["#262318", "#33301f", "#423e29", "#524c33", "#625b3d"]
ALGA = ["#1c3014", "#28441b", "#365a22", "#477229", "#5b8a33"]
SANGUIJUELA = ["#120d10", "#21171d", "#33232b"]
OJO = ["#5a5010", "#9a8a1a", "#d8c440", "#fff27a"]
FATUO = ["#1a5a20", "#2e9a3a", "#5ad860", "#b0ff9a", "#eaffd8"]
#: la raja de la panza del hinchado (cara de delante, 11 x 10)
GRIETA_PANZA = {(5, 2), (5, 3), (4, 4), (5, 5), (6, 5), (6, 6), (5, 7), (6, 8)}


def _venas(semilla):
    """Las venas: lineas finas que bajan y se ramifican (como las grietas del marchito)."""
    import random
    r = random.Random(semilla)
    puntos = set()
    for _ in range(5):
        x, y = r.randrange(0, 16), r.randrange(0, 16)
        for _ in range(r.randrange(5, 10)):
            puntos.add((x % 16, y % 16))
            x += r.choice((-1, 0, 0, 1))
            y += 1
    return puntos


def _cara(variante):
    C = {"S": PIEL[5], "s": PIEL[4], "o": PIEL[3], "O": PIEL[2], "K": "#0b0d08", "k": "#1a2016", "M": "#050404", "m": "#141010",
         "V": VENA[1], "A": ALGA[2], "a": ALGA[3], "N": "#0e1210"}
    filas = [
        "AaOsOaA",
        "OsSsSsO",
        "oKKsKKo",
        "SkKoKkS",
        "SVsosVS",
        "omMMMmo",
        "OoMNMoO",
    ]
    return dibujo(filas, C)


def materiales(variante):
    piel_b = ruido(PIEL[2:6], 37, grano=0.28, base=0.5)
    barro_b = ruido(BARRO[0:5], 41, grano=0.7, base=0.45)
    harapo = tela(HARAPO, 43, pliegue=3)
    alga = ruido(ALGA[0:4], 47, grano=0.8, base=0.5)
    import numpy as np
    bajo = np.random.default_rng(59).random((24, 24))
    venas = _venas(61)
    k = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}

    def barro(cara, x, y, w, h):
        c = barro_b(cara, x, y, w, h)
        if (x * 7 + y * 3 + k[cara] * 5) % 23 == 0:
            return "#5e4c36"                             # un brillo de barro mojado
        return c

    def piel(cara, x, y, w, h):
        c = piel_b(cara, x, y, w, h)
        # manchas grandes de podredumbre
        m = bajo[(y // 3 + 5 * k[cara]) % 24, (x // 3 + 7 * k[cara]) % 24]
        if m > 0.78:
            c = MANCHA[1] if m > 0.9 else mezcla(c, MANCHA[0], 0.55)
        # venas oscuras que bajan ramificandose
        if ((x + 3 * k[cara]) % 16, (y + 5 * k[cara]) % 16) in venas:
            return VENA[(x + y) % 2]
        return c

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara(variante)(cara, x, y, w, h)
        c = piel(cara, x, y, w, h)
        if cara == "arriba" and (x * 3 + y) % 4 == 0:
            return ALGA[1]                               # el pelo de algas
        return c

    def torso(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if y >= h - 3:
            return barro(cara, x, y, w, h)               # el barro de la cintura
        if cara == "frente" and y <= 1 and abs(x - w / 2 + 0.5) <= 2:
            return oscuro(c, 0.2)
        if (x * 7 + y * 3 + len(cara)) % 19 == 0:
            return SANGUIJUELA[1]                        # una sanguijuela
        return c

    def barriga(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if cara == "frente" and (x - w / 2) ** 2 / 9 + (y - h / 2) ** 2 / 9 < 1:
            return claro(c, 0.08)                        # la piel tirante y palida
        if y >= h - 1:
            return barro(cara, x, y, w, h)
        return c

    def panza(cara, x, y, w, h):
        c = piel_b(cara, x, y, w, h)
        if cara == "frente" and (x, y) in GRIETA_PANZA:
            return "#0d1a0b"                             # la raja (por dentro brilla el gas)
        if cara in ("frente", "der", "izq"):
            # la piel tirante a punto de reventar: palida y amarillenta, con ampollas
            c = mezcla(c, "#b8b48a", 0.25)
            if (x * 3 + y * 5 + k[cara]) % 11 == 0:
                return "#9fb04a"
            if (x * 3 + y * 5 + k[cara]) % 11 == 1:
                return "#6f7d33"
            if ((x + 3 * k[cara]) % 16, (y + 5 * k[cara]) % 16) in venas:
                return VENA[0]
        if y >= h - 2:
            return barro(cara, x, y, w, h)
        return c

    def harapo_f(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        corte = (5, 7, 6, 4, 7, 5, 6, 7, 4, 6)[(x * 3 + {"frente": 0, "espalda": 4, "der": 2, "izq": 7}.get(cara, 1)) % 10]
        if y > corte:
            return None
        if y >= 2:
            return barro(cara, x, y, w, h)               # empapado de barro
        return harapo(cara, x, y, w, h)

    def brazo(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if (x + y * 3 + len(cara)) % 11 == 0:
            return SANGUIJUELA[0]
        return c

    def antebrazo(cara, x, y, w, h):
        if y >= h - 1:
            return PIEL[1]                               # los dedos
        c = piel(cara, x, y, w, h)
        if (x * 2 + y + len(cara)) % 13 == 0:
            return SANGUIJUELA[1]
        return c

    def muslo(cara, x, y, w, h):
        return barro(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        if y >= h - 1:
            return BARRO[0]
        return oscuro(barro(cara, x, y, w, h), 0.1)

    def algas(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # tiras que cuelgan de largos distintos
        largo = (6, 4, 5, 3, 6, 2, 5, 4)[(x * 3 + {"frente": 0, "espalda": 2, "der": 5, "izq": 1}.get(cara, 0)) % 8]
        if y >= largo:
            return None
        return alga(cara, x, y, w, h)

    def algas_hombro(cara, x, y, w, h):
        if cara == "abajo":
            return None
        largo = (4, 2, 5, 3, 4)[(x + {"frente": 0, "espalda": 2, "der": 3, "izq": 1}.get(cara, 4)) % 5]
        if cara != "arriba" and y >= largo:
            return None
        return alga(cara, x, y, w, h)

    mats = {
        "cabeza": sombra(cabeza, abajo=0.15),
        "torso": sombra(torso),
        "barriga": sombra(barriga, abajo=0.2),
        "panza": sombra(panza, abajo=0.25),
        "harapo": harapo_f,
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.15), "antebrazo_i": sombra(antebrazo, abajo=0.15),
        "muslo_d": muslo, "muslo_i": muslo,
        "pierna_d": sombra(pierna, abajo=0.2), "pierna_i": sombra(pierna, abajo=0.2),
        "algas": algas,
        "algas_hombro": algas_hombro,
        "fatuo": ruido(FATUO[1:4], 53, grano=0.8, base=0.6),
        "fatuo_halo": por_cara(lambda cara, x, y, w, h: FATUO[2] if (x + y) % 3 == 0 else None),
    }
    return mats


def brillo(variante):
    """Lo que brilla: los ojos amarillos y, en el del fuego fatuo, la llama verde."""
    C = {"Y": OJO[3], "y": OJO[2]}
    ojos = dibujo([
        ".......",
        ".......",
        ".yY.Yy.",
        "..y.y..",
    ], C)
    b = {"cabeza": por_cara(liso(None), frente=ojos)}
    if variante == 2:
        b["panza"] = por_cara(liso(None), frente=lambda cara, x, y, w, h: "#7cff6a" if (x, y) in GRIETA_PANZA else None)
    if variante == 1:
        b["fatuo"] = ruido(FATUO[2:5], 53, grano=0.8, base=0.6)
        b["fatuo_halo"] = por_cara(lambda cara, x, y, w, h: FATUO[3] if (x + y) % 3 == 0 else None)
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 15)

# de pie: encorvado y pesado, la cabeza adelantada, los brazos colgando delante de la barriga
BASE = dict(raiz=raiz(-0.5), torso=(14, 0, 3), cabeza=(-4, 0, 6),
            muslo_d=(-6, 0, 6), pierna_d=(8, 0, 0), muslo_i=(2, 0, -6), pierna_i=(6, 0, 0),
            brazo_d=(-10, 0, 10), antebrazo_d=(-14, 0, 0), brazo_i=(-8, 0, -10), antebrazo_i=(-16, 0, 0), harapo=(6, 0, 0))
# el del fuego fatuo: la llama delante, en la palma abierta, a la altura del pecho
FATUO_PORTE = dict(brazo_i=(-52, 0, -10), antebrazo_i=(-58, 0, 0), mano_i=(10, 0, 0))
# en guardia: agachado, las manos abiertas delante
LISTO = dict(raiz=raiz(-1.4), torso=(28, 0, 0), cabeza=(-18, 0, 0), harapo=(14, 0, 0),
             muslo_d=(-24, 0, 8), pierna_d=(38, 0, 0), muslo_i=(-10, 0, -8), pierna_i=(30, 0, 0),
             brazo_d=(-60, -8, 24), antebrazo_d=(-36, 0, 0), brazo_i=(-56, 8, -24), antebrazo_i=(-40, 0, 0))
# flotando boca abajo en el agua (el dibujante lo sube hasta que la espalda asoma): los brazos y las piernas cuelgan
HUNDIDO = dict(raiz=raiz(3.2, 15.0, rot=(90, 0, 0)), torso=(0, 0, 0), cabeza=(20, 0, 15),
               muslo_d=(-40, 0, 10), pierna_d=(30, 0, 0), muslo_i=(-30, 0, -10), pierna_i=(24, 0, 0),
               brazo_d=(-120, 0, 40), antebrazo_d=(-20, 0, 0), brazo_i=(-116, 0, -36), antebrazo_i=(-24, 0, 0), harapo=(0, 0, 0))
# tendido boca abajo en el barro (si no hay agua): los brazos por delante de la cabeza
TENDIDO = dict(raiz=raiz(3.2, 15.0, rot=(90, 0, 0)), torso=(0, 0, 0), cabeza=(-6, 0, 18),
               muslo_d=(0, 0, 6), pierna_d=(6, 0, 0), muslo_i=(0, 0, -6), pierna_i=(4, 0, 0),
               brazo_d=(-172, 0, 34), antebrazo_d=(-6, 0, 0), brazo_i=(-170, 0, -30), antebrazo_i=(-8, 0, 0), harapo=(0, 0, 0))
# de rodillas en el barro, encorvado sobre si mismo
RODILLAS = dict(raiz=raiz(-6.8, 1.0), torso=(30, 0, 4), cabeza=(26, 0, 8),
                muslo_d=(-12, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -6), pierna_i=(94, 0, 0),
                brazo_d=(-20, 0, 14), antebrazo_d=(-30, 0, 0), brazo_i=(-24, 0, -14), antebrazo_i=(-28, 0, 0), harapo=(-30, 0, 0))
BRAZO_I = ("brazo_i", "antebrazo_i", "mano_i")


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

    # ---- quieto: respira con dificultad, la barriga sube y baja, se balancea
    bucle("quieto", 3.6, [
        (0.0, BASE),
        (1.8, con(BASE, torso=(18, 0, 6), cabeza=(-8, 6, 10), raiz=raiz(-0.8))),
        (3.6, BASE),
    ])
    # ---- andar: arrastra los pies, balanceandose
    pasos = []
    for k, (md, mi, pd, pi_, tz) in enumerate(((-22, 16, 10, 22, 8), (-4, 2, 8, 34, 0), (16, -22, 22, 10, -8), (2, -4, 34, 8, 0),
                                                 (-22, 16, 10, 22, 8))):
        pasos.append((1.8 * k / 4, {"muslo_d": (md, 0, 6), "muslo_i": (mi, 0, -6), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (16, 0, tz), "cabeza": (-6, 0, -tz * 0.6), "raiz": raiz(-0.5 + (0.6 if k % 2 else 0)),
                                    "brazo_d": (-10 - mi * 0.4, 0, 10), "brazo_i": (-8 - md * 0.4, 0, -10)}))
    bucle("andar", 1.8, pasos)
    bucle("listo_podrido", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-1.8), torso=(32, 0, 3), cabeza=(-22, 4, 0))),
        (2.0, LISTO),
    ])
    q = con(BASE, FATUO_PORTE)
    bucle("porte_fatuo", 2.4, [(0.0, q), (1.2, con(q, mano_i=(16, 0, 6))), (2.4, q)], solo=BRAZO_I)
    bucle("hundido", 5.0, [
        (0.0, HUNDIDO),
        (2.5, con(HUNDIDO, raiz=raiz(3.6, 15.0, rot=(88, 0, 3)), cabeza=(24, 0, 12), brazo_d=(-114, 0, 44), brazo_i=(-122, 0, -32))),
        (5.0, HUNDIDO),
    ])
    bucle("tendido", 5.0, [
        (0.0, TENDIDO),
        (2.5, con(TENDIDO, cabeza=(-8, 0, 16), torso=(2, 0, 0))),
        (5.0, TENDIDO),
    ])
    bucle("rodillas", 4.0, [
        (0.0, RODILLAS),
        (2.0, con(RODILLAS, torso=(34, 0, -4), cabeza=(30, 0, -6))),
        (4.0, RODILLAS),
    ])

    # ================================================================ acciones
    G = LISTO

    # ---- 1 zarpazo: echa atras la derecha y barre de arriba abajo
    Z_ATRAS = con(G, torso=(12, 30, 0), cabeza=(-10, -24, 0), brazo_d=(-150, 0, 40), antebrazo_d=(-50, 0, 0))
    Z_DA = con(G, raiz=raiz(-2.0, -1.4), torso=(40, -20, 0), cabeza=(-24, 16, 0), brazo_d=(-50, 0, -20), antebrazo_d=(-10, 0, 0),
               muslo_i=(-34, 0, -8), pierna_i=(40, 0, 0))
    accion("zarpazo", 1.3, [
        (0.00, G),
        (0.42, Z_ATRAS),
        (0.55, Z_DA),
        (0.85, con(Z_DA, torso=(36, -24, 0))),
        (1.30, G),
    ], golpes=(0.54,))
    # ---- 2 mordisco: se agacha y se lanza con la boca abierta, las manos por delante
    M_AGACHA = con(G, raiz=raiz(-3.0), torso=(40, 0, 0), cabeza=(-30, 0, 0), muslo_d=(-50, 0, 8), pierna_d=(70, 0, 0),
                   muslo_i=(-46, 0, -8), pierna_i=(66, 0, 0))
    M_LANZA = con(G, raiz=raiz(-1.0, -3.5), torso=(56, 0, 0), cabeza=(-44, 0, 0), brazo_d=(-100, -10, 10), antebrazo_d=(-10, 0, 0),
                  brazo_i=(-100, 10, -10), antebrazo_i=(-10, 0, 0), muslo_d=(30, 0, 8), pierna_d=(20, 0, 0), muslo_i=(-50, 0, -8),
                  pierna_i=(40, 0, 0))
    accion("mordisco", 1.5, [
        (0.00, G),
        (0.45, M_AGACHA),
        (0.68, M_LANZA),
        (0.95, con(M_LANZA, cabeza=(-30, 0, 0))),
        (1.50, G),
    ], golpes=(0.70,))
    # ---- 3 vomito: se echa atras, hincha el pecho y vomita agua negra delante (de 0,75 a 1,2 s)
    V_ATRAS = con(G, raiz=raiz(-0.6, 1.2), torso=(-10, 0, 0), cabeza=(-36, 0, 0), brazo_d=(-30, 0, 30), brazo_i=(-30, 0, -30))
    V_ECHA = con(G, raiz=raiz(-1.6, -1.0), torso=(44, 0, 0), cabeza=(-6, 0, 0), brazo_d=(-10, 0, 40), antebrazo_d=(-30, 0, 0),
                 brazo_i=(-10, 0, -40), antebrazo_i=(-30, 0, 0))
    accion("vomito", 1.9, [
        (0.00, G),
        (0.55, V_ATRAS),
        (0.75, V_ECHA),
        (1.00, con(V_ECHA, cabeza=(0, 6, 0))),
        (1.20, con(V_ECHA, cabeza=(-4, -6, 0))),
        (1.90, G),
    ], golpes=(0.76,))
    # ---- 4 golpe de fatuo: barre con la mano de la llama
    F_ATRAS = con(G, torso=(12, -30, 0), cabeza=(-10, 24, 0), brazo_i=(-140, 0, -40), antebrazo_i=(-40, 0, 0))
    F_DA = con(G, raiz=raiz(-2.0, -1.4), torso=(40, 20, 0), cabeza=(-24, -16, 0), brazo_i=(-50, 0, 20), antebrazo_i=(-10, 0, 0),
               muslo_d=(-34, 0, 8), pierna_d=(40, 0, 0))
    accion("golpe_fatuo", 1.3, [
        (0.00, con(G, FATUO_PORTE)),
        (0.42, F_ATRAS),
        (0.55, F_DA),
        (0.85, con(F_DA, torso=(36, 24, 0))),
        (1.30, con(G, FATUO_PORTE)),
    ], golpes=(0.54,))
    # ---- 5 lanzar el fatuo: lo levanta por encima del hombro y lo lanza (sale en 0,8 s)
    L_ALTO = con(G, raiz=raiz(-0.8, 1.0), torso=(-6, -20, 0), cabeza=(-24, 14, 0), brazo_i=(-170, 0, -20), antebrazo_i=(-30, 0, 0))
    L_LANZA = con(G, raiz=raiz(-1.8, -1.2), torso=(38, 14, 0), cabeza=(-16, -10, 0), brazo_i=(-80, 0, 10), antebrazo_i=(0, 0, 0),
                  muslo_d=(-30, 0, 8), pierna_d=(36, 0, 0))
    accion("lanzar_fatuo", 1.7, [
        (0.00, con(G, FATUO_PORTE)),
        (0.60, L_ALTO),
        (0.80, L_LANZA),
        (1.10, con(L_LANZA, torso=(34, 10, 0))),
        (1.70, con(G, FATUO_PORTE)),
    ], golpes=(0.80,))
    # ---- 6 aplastar (el hinchado): se alza de puntillas y se deja caer de panza encima
    A_ALZA = con(G, raiz=raiz(1.2, 1.0), torso=(-10, 0, 0), cabeza=(-24, 0, 0), brazo_d=(-170, 0, 20), antebrazo_d=(-10, 0, 0),
                 brazo_i=(-170, 0, -20), antebrazo_i=(-10, 0, 0), muslo_d=(0, 0, 8), pierna_d=(4, 0, 0), muslo_i=(0, 0, -8), pierna_i=(4, 0, 0))
    A_CAE = con(G, raiz=raiz(2.0, -2.0, rot=(70, 0, 0)), torso=(10, 0, 0), cabeza=(-40, 0, 0), brazo_d=(-150, 0, 50), antebrazo_d=(-10, 0, 0),
                brazo_i=(-150, 0, -50), antebrazo_i=(-10, 0, 0), muslo_d=(-10, 0, 8), pierna_d=(20, 0, 0), muslo_i=(-10, 0, -8), pierna_i=(20, 0, 0))
    accion("aplastar", 2.4, [
        (0.00, G),
        (0.55, A_ALZA),
        (0.80, con(A_ALZA, raiz=raiz(1.6, 0.0))),
        (1.00, A_CAE),
        (1.55, A_CAE),
        (2.05, con(G, raiz=raiz(-3.0, -1.0), torso=(50, 0, 0))),
        (2.40, G),
    ], golpes=(0.98,))
    # ---- 7 eructo (el hinchado): se echa atras, la panza se hincha y suelta una nube de gas alrededor
    E_ATRAS = con(G, raiz=raiz(-0.4, 1.6), torso=(-20, 0, 0), cabeza=(-40, 0, 0), brazo_d=(-20, 0, 50), antebrazo_d=(-20, 0, 0),
                  brazo_i=(-20, 0, -50), antebrazo_i=(-20, 0, 0))
    accion("eructo", 2.0, [
        (0.00, G),
        (0.60, E_ATRAS),
        (0.85, con(E_ATRAS, cabeza=(-50, 0, 0), torso=(-24, 0, 0))),
        (0.95, con(G, torso=(30, 0, 0), cabeza=(-10, 0, 0))),
        (1.40, con(G, torso=(26, 0, 0), cabeza=(-14, 0, 0))),
        (2.00, G),
    ], golpes=(0.95,))
    # ---- 8 alzarse: estaba hundido en el agua; se levanta chorreando
    accion("alzarse", 1.8, [
        (0.00, HUNDIDO),
        (0.50, con(HUNDIDO, raiz=raiz(4.0, 11.0, rot=(62, 0, 0)), brazo_d=(-70, 0, 20), brazo_i=(-70, 0, -20))),
        (1.00, con(RODILLAS, torso=(40, 0, 0))),
        (1.80, G),
    ])
    # ---- 9 levantarse: estaba de rodillas en el barro
    accion("levantarse", 1.2, [
        (0.00, RODILLAS),
        (0.60, con(RODILLAS, raiz=raiz(-4.0, 0.5), muslo_d=(-40, 0, 6), pierna_d=(60, 0, 0))),
        (1.20, G),
    ])
    # ---- 10 aturdido
    AT = dict(raiz=raiz(-1.2, 1.8), torso=(-12, 0, 10), cabeza=(-24, 0, 16), muslo_d=(-8, 0, 8), pierna_d=(20, 0, 0),
              muslo_i=(10, 0, -8), pierna_i=(16, 0, 0), brazo_d=(20, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(20, 0, -30),
              antebrazo_i=(-20, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0))),
        (0.25, AT),
        (0.70, con(AT, torso=(-6, 8, -10), cabeza=(-18, -10, -16))),
        (1.10, con(AT, torso=(-8, -8, 10), cabeza=(-20, 8, 14))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(2, 0, 0))),
    ])
    # ---- 11 morir: cae de rodillas y se desploma de bruces (en el agua)
    accion("morir", 2.4, [
        (0.00, con(BASE, torso=(-6, 0, 8), cabeza=(-24, 0, 10))),
        (0.60, RODILLAS),
        (1.40, con(TENDIDO, raiz=raiz(4.0, 12.0, rot=(78, 0, 8)))),
        (2.40, con(TENDIDO, raiz=raiz(3.2, 15.0, rot=(90, 0, 10)))),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "fatuo" in nombre:
        return 1
    if nombre in ("aplastar", "eructo"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    if v == 1:
        return anim.sumar(anims["quieto"].muestra(0), anims["porte_fatuo"].muestra(0))
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    return []
