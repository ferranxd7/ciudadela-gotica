"""
Hakon, el Capataz de las Minas de Hierro Negro (parte 7ar): el jefe de la mina. Era el capataz del turno de noche; el rey queria
mas hierro negro para las cadenas y el abrio el nivel tres, donde la roca esta caliente y la veta sangra (libro 36, el libro de
turnos). Mando volar la pared tras la que se oian voces: detras habia mas veta, y algo que respiraba. La noche del cielo rojo la
montana temblo y se hundio la Galeria del Rey; el se quedo abajo, en la Caverna del Filon, y la veta se le metio dentro: le salen
cristales rojos del hombro y del brazo izquierdo y por la espalda, y un ojo le arde. Sigue picando la veta y llamando al turno con
el silbato.

Grande y ancho (el juego lo dibuja a 1,5): el casco de hierro del capataz con su candil (la llama roja), la barba espesa y gris
del polvo de hierro, el abrigo largo de cuero con el cuello alzado, el cinturon ancho con la hebilla de laton, el silbato
colgado del pecho, las llaves y el farol de la mina al costado izquierdo (luce rojo); el hombro, el brazo izquierdo y la espalda
cubiertos de cristales de la veta (lucen); las botas herradas. En la derecha el **pico del capataz**: un pico doble de hierro
negro con un mango largo y la veta roja incrustada en la cabeza.

Sus golpes: el picotazo (a dos manos, de arriba abajo; engancha el escudo), el barrido (abierto), la embestida (con el hombro de
cristal), la veta (clava el pico en el suelo: los cristales brotan en linea hacia su presa), los barrenos (tres cartuchos en
abanico, encendidos en su farol), el derrumbe (dos golpes al suelo: del techo caen piedras sobre cada uno), el silbato (llama al
turno: los enterrados salen de los escombros) y, a media vida, se clava la mano de cristal en el suelo y se bebe la veta.
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, madera, metal, mezcla, oscuro, ruido, sombra, tela)  # noqa: F401

NOMBRE = "capataz"
VARIANTES = ("hakon",)
OCULTAS = {0: []}
#: grande y ancho: lo que le da su dibujante en el juego
ESCALA = 1.5
CENTRO_Y = 1.6

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # ancho de hombros y de pecho, los brazos gruesos y largos (la mano llega a unos 13 de su hombro)
    humanoide(r, torso=(10, 13, 6), cabeza=(8, 8, 8), brazo=(4, 16, 4), pierna=(5, 12, 5), hombro=0.7)
    # ---- el casco de hierro con el ala, la cresta y el candil delante (la llama roja)
    r.pieza("casco", "cabeza", (0, 0, 0), boxes=[
        Box(-4.4, -9.6, -4.4, 8.8, 3.2, 8.8, "casco"),
        Box(-4.6, -6.6, -5.7, 9.2, 0.7, 1.6, "ala_casco"),
        Box(-0.6, -10.2, -4.2, 1.2, 0.8, 8.4, "cresta"),
        Box(-1.1, -9.4, -5.9, 2.2, 2.4, 1.6, "candil"),
        Box(-0.6, -10.9, -5.6, 1.2, 1.5, 1.0, "llama_candil"),
    ])
    # ---- la barba espesa hasta el pecho
    r.pieza("barba", "cabeza", (0, -3.4, -4.1), rot=(-6, 0, 0), boxes=[Box(-3.9, 0, -1.4, 7.8, 6.6, 1.6, "barba")])
    # ---- el abrigo: el cuello alzado, el faldon de atras hasta las rodillas y los dos delanteros (abiertos)
    r.pieza("cuello", "torso", (0, -13, 0), boxes=[Box(-5.4, -2.0, -3.4, 10.8, 3.2, 6.8, "cuello", grow=g(0.1))])
    r.pieza("faldon", "torso", (0, -1.2, 3.1), rot=(6, 0, 0), boxes=[Box(-5.3, 0, 0, 10.6, 11.0, 0.8, "faldon")])
    for n, s in (("d", -1), ("i", 1)):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.9, -0.8, -2.9, 5.8, 8.2, 5.8, f"faldon_{n}")])
    # ---- el cinturon ancho con la hebilla, el silbato colgado del pecho, las llaves y el farol al costado izquierdo
    r.pieza("cinturon", "torso", (0, 0, 0), boxes=[
        Box(-5.35, -2.2, -3.35, 10.7, 2.2, 6.7, "cinturon"),
        Box(-1.3, -2.5, -3.75, 2.6, 2.8, 0.6, "hebilla"),
    ])
    r.pieza("silbato", "torso", (0, -11.5, -3.2), boxes=[
        Box(-0.2, 0, -0.25, 0.4, 4.2, 0.3, "cadenita"),
        Box(-0.8, 4.0, -0.9, 1.6, 0.9, 1.4, "silbato"),
    ])
    r.pieza("llaves", "torso", (-5.4, -1.5, -1.0), boxes=[
        Box(-0.5, 0, -1.2, 0.5, 2.6, 2.4, "llaves"),
    ])
    r.pieza("farol", "torso", (6.2, -1.4, 0.4), rot=(0, 0, 6), boxes=[
        Box(-0.25, 0, -0.25, 0.5, 1.6, 0.5, "asa_farol"),
        Box(-1.5, 1.6, -1.5, 3.0, 0.7, 3.0, "tapa_farol"),
        Box(-1.3, 2.3, -1.3, 2.6, 3.0, 2.6, "farol"),
        Box(-1.5, 5.3, -1.5, 3.0, 0.6, 3.0, "tapa_farol", uv="base_farol"),
    ])
    # ---- la veta: cristales rojos que le salen del hombro izquierdo, del brazo y de la espalda (lucen)
    r.pieza("veta_hombro", "brazo_i", (1.2, -2.2, 0.2), rot=(0, 0, 24), boxes=[
        Box(-1.0, -5.6, -1.0, 2.0, 5.6, 2.0, "cristal"),
        Box(-0.7, -3.8, -2.6, 1.4, 3.8, 1.4, "cristal", uv="cristal_b"),
        Box(0.6, -3.0, 0.9, 1.4, 3.0, 1.4, "cristal", uv="cristal_c"),
    ])
    r.pieza("veta_hombro2", "brazo_i", (2.0, 0.8, -0.6), rot=(14, 0, 70), boxes=[Box(-0.8, -4.4, -0.8, 1.6, 4.4, 1.6, "cristal", uv="cristal_d")])
    r.pieza("veta_brazo", "antebrazo_i", (0, 0, 0), boxes=[
        Box(-2.4, 0.6, -2.4, 4.8, 5.4, 4.8, "costra"),
        Box(1.6, 1.4, -0.7, 1.8, 2.4, 1.4, "cristal", uv="cristal_e"),
        Box(-0.7, 2.6, -3.4, 1.4, 2.0, 1.6, "cristal", uv="cristal_f"),
    ])
    r.pieza("veta_espalda", "torso", (1.5, -9.0, 3.0), rot=(-34, 0, 18), boxes=[
        Box(-1.1, -6.4, -1.1, 2.2, 6.4, 2.2, "cristal", uv="cristal_g"),
    ])
    r.pieza("veta_espalda2", "torso", (3.6, -5.5, 3.0), rot=(-50, 0, 40), boxes=[
        Box(-0.9, -4.6, -0.9, 1.8, 4.6, 1.8, "cristal", uv="cristal_h"),
    ])
    r.pieza("veta_espalda3", "torso", (-1.8, -11.0, 3.0), rot=(-28, 0, -10), boxes=[
        Box(-0.7, -3.4, -0.7, 1.4, 3.4, 1.4, "cristal", uv="cristal_i"),
    ])
    # ---- el pico del capataz (mano derecha; con el brazo caido apunta a -Z hacia arriba): el mango largo con su puno de
    # cuero y el regaton, la cabeza de hierro negro (un pico doble, mas largo hacia delante) con la veta incrustada
    r.pieza("pico", "mano_d", (0, 0, 0), boxes=[
        Box(-0.9, -0.9, -25.0, 1.8, 1.8, 30.0, "mango"),
        Box(-1.1, -1.1, 4.6, 2.2, 2.2, 1.4, "regaton"),
        Box(-1.4, -1.4, -27.6, 2.8, 2.8, 3.4, "ojo_pico"),
        Box(-1.0, -13.0, -27.2, 2.0, 11.8, 2.6, "pico_hierro"),
        Box(-1.0, 1.2, -27.2, 2.0, 9.0, 2.6, "pico_hierro", uv="pico_hierro_b"),
        Box(-0.6, -15.4, -26.8, 1.2, 2.6, 1.8, "punta"),
        Box(-0.6, 10.0, -26.8, 1.2, 2.2, 1.8, "punta", uv="punta_b"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas
# La piel curtida y gris del polvo de hierro (y, por la izquierda, agrietada con la veta); el cuero del abrigo; la lana de la
# camisa; el hierro negro del casco y del pico; el laton del silbato, la hebilla y el farol; los cristales rojos de la veta.

PIEL = ["#1f1a17", "#2e2621", "#40362e", "#52473d", "#655a4d", "#776b5c"]
POLVO = ["#121214", "#1b1b1e", "#26262a", "#323236"]
BARBA = ["#2a2a2c", "#3a3a3d", "#4c4c50", "#616165", "#77777b"]
CUERO = ["#140d08", "#1f150d", "#2b1d12", "#382718", "#46321f", "#563f28"]
LANA = ["#1c1a1e", "#262329", "#312d35", "#3d3842"]
HIERRO = ["#09090b", "#111114", "#1a1a1e", "#242429", "#303036", "#3e3e46", "#505059"]
LATON = ["#3b2a0e", "#5a4318", "#7d5e22", "#a07c2f", "#c9a043", "#e6c66a"]
MADERA = ["#1e150e", "#2a1d13", "#382718", "#46331f", "#55402a"]
VETA = ["#2c0504", "#4e0a07", "#7a120b", "#a81c10", "#d6341a", "#ff6a32", "#ffb27a"]
LLAMA = ["#5a0c06", "#9a1a0c", "#d43818", "#ff6a32", "#ffb27a"]
OJO = "#ff5a2a"

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _azar01(*v):
    h = 2166136261
    for n in v:
        h = ((h ^ (int(n) & 0xffffffff)) * 16777619) & 0xffffffff
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    h ^= h >> 15
    return h / 4294967296.0


def _veteado(cara, x, y, w, h, sal, cuanto):
    """Si en ese pixel pasa una grieta de la veta (lineas quebradas en diagonal)."""
    kc = CARAS[cara]
    a = (x * 3 + y * 5 + kc * 7 + sal) % 17
    b = int(_azar01(sal, kc, x // 2, (y + x) // 3) * 100)
    return a < 2 and b < cuanto


def materiales(variante):
    piel_t = ruido(PIEL[2:6], 9101, grano=0.5, base=0.45)
    cuero_t = ruido(CUERO[1:6], 9102, grano=0.45, base=0.5)
    abrigo_t = tela(CUERO[1:6], 9103, pliegue=3, grano=0.25, luz=0.14)
    lana_t = tela(LANA, 9104, pliegue=2, grano=0.3, luz=0.1)
    hierro_t = metal(HIERRO[2:7], 9105)
    negro_t = metal(HIERRO[0:5], 9106)
    madera_t = madera(MADERA, 9107, "z")
    cristal_t = ruido(VETA[1:5], 9108, grano=0.55, base=0.45)

    def polvo(c, cara, x, y, w, h, cuanto=0.24, sal=0):
        kc = CARAS[cara]
        s0 = w * 29 + h * 13 + sal * 97
        g_ = 0.6 * _azar01(s0, kc, x // 2, y // 2) + 0.4 * _azar01(s0 + 5, kc, x, y)
        k = cuanto * (0.7 + 0.6 * y / max(1, h))
        if g_ < k * 0.45:
            return POLVO[0]
        if g_ < k:
            return mezcla(c, POLVO[1], 0.5)
        return c

    def piel(cara, x, y, w, h, sal=1, veta=0):
        c = polvo(piel_t(cara, x, y, w, h), cara, x, y, w, h, 0.26, sal)
        if veta and _veteado(cara, x, y, w, h, sal, veta):
            return VETA[3] if (x + y) % 2 else VETA[2]
        return c

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            # bajo el ala del casco: las cejas pobladas; el ojo derecho hundido, el izquierdo con la veta (luce); la nariz rota
            # y la barba que empieza en los pomulos
            C = {"P": PIEL[3], "p": PIEL[2], "s": PIEL[1], "K": "#0b0807", "E": OJO, "e": VETA[3], "B": BARBA[2], "b": BARBA[1],
                 "n": PIEL[4], "c": BARBA[3], "r": VETA[2]}
            v = dibujo([
                "pPPPPPPp",
                "cccPpccc",
                "sKKpnreE",
                "pspPnrsp",
                "BbpnnpbB",
                "BbbbbbbB",
                "bBbBbBbB",
                "BbBbBbBb",
            ], C)(cara, x, y, w, h)
            return v if v is not None else piel(cara, x, y, w, h, 2)
        if cara == "arriba":
            return PIEL[1]
        if cara in ("der", "izq"):
            d = delante(cara, x, w)
            if y >= 4 and d <= 3:
                return BARBA[(x + y) % 3 + 1]                  # la barba por las quijadas
            if y == 3 and d == 4:
                return PIEL[1]                                 # la oreja
            return piel(cara, x, y, w, h, 3, 40 if cara == "izq" else 0)
        if cara == "espalda":
            return BARBA[1] if y >= 5 else polvo(PIEL[2], cara, x, y, w, h, 0.4, 4)   # el pelo corto y la nuca
        return piel(cara, x, y, w, h, 2)

    def torso(cara, x, y, w, h):
        if cara == "abajo":
            return CUERO[1]
        # el abrigo de cuero abierto por delante: asoma la camisa de lana; los botones de laton
        if cara == "frente" and w // 2 - 2 <= x <= w // 2 + 1:
            c = polvo(lana_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 5)
            return c
        c = polvo(abrigo_t(cara, x, y, w, h), cara, x, y, w, h, 0.24, 6)
        if cara == "frente" and x in (w // 2 - 3, w // 2 + 2) and y in (3, 6, 9):
            return LATON[3]
        if cara == "frente" and x in (w // 2 - 3, w // 2 + 2):
            return oscuro(c, 0.3)                              # el canto de las solapas
        if cara == "espalda" and x >= w // 2 and _veteado(cara, x, y, w, h, 7, 55):
            return VETA[3]                                     # la veta rompe el abrigo por la espalda
        if (x * 7 + y * 3 + CARAS[cara]) % 29 == 0:
            return oscuro(c, 0.35)                             # un remiendo, una quemadura
        return c

    def brazo(cara, x, y, w, h, izq=False):
        c = polvo(abrigo_t(cara, x, y, w, h), cara, x, y, w, h, 0.22, 8 if izq else 9)
        if izq and _veteado(cara, x, y, w, h, 10, 70):
            return VETA[3] if (x + y) % 2 else VETA[2]
        return c

    def antebrazo(cara, x, y, w, h, izq=False):
        if y >= h - 1:
            return PIEL[1] if not izq else VETA[2]            # los dedos (la izquierda, de cristal)
        if y < 2:
            return CUERO[2]                                    # el puno de la manga
        if izq:
            return VETA[2] if (x + y) % 3 else VETA[3]
        return piel(cara, x, y, w, h, 11)

    def muslo(cara, x, y, w, h):
        return polvo(lana_t(cara, x, y + 2, w, h), cara, x, y, w, h, 0.3, 12)

    def pierna(cara, x, y, w, h):
        if y >= 2:
            # las botas herradas, altas
            c = polvo(cuero_t(cara, x, y, w, h), cara, x, y, w, h, 0.35, 13)
            if y == h - 1:
                return HIERRO[3] if (x % 2) else HIERRO[2]
            if y == 2:
                return oscuro(c, 0.3)
            return c
        return muslo(cara, x, y + 5, w, h)

    def casco(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = hierro_t(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return HIERRO[1]                                   # el borde
        if (x * 5 + y * 3) % 11 == 0:
            return HIERRO[0]                                   # abolladuras
        return polvo(c, cara, x, y, w, h, 0.18, 14)

    def candil(cara, x, y, w, h):
        if cara == "frente" and 0 < y < h - 1:
            return LLAMA[2] if (x + y) % 2 else LLAMA[3]
        return LATON[2 + (x + y) % 2] if cara != "abajo" else LATON[0]

    def barba(cara, x, y, w, h):
        if cara == "arriba":
            return None
        c = BARBA[(x * 3 + y) % 4 + 1] if (x + y * 2) % 5 else BARBA[0]
        if cara == "frente" and y == 0 and 2 <= x <= w - 3:
            return "#120b09"                                   # la boca, entre la barba
        if cara == "frente" and y >= h - 2 and x % 3 == 1:
            return None                                        # las puntas
        return polvo(c, cara, x, y, w, h, 0.3, 15)

    def cuello_m(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return polvo(abrigo_t(cara, x, y, w, h), cara, x, y, w, h, 0.2, 16)

    def faldon(cara, x, y, w, h):
        c = polvo(abrigo_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 17)
        if y >= h - 1 and x % 3 == 0:
            return None                                        # el bajo deshilachado
        return c

    def faldon_pierna(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # abierto por delante: solo los lados y la espalda
        if cara == "frente" and 1 <= x <= w - 2:
            return None
        c = polvo(abrigo_t(cara, x, y, w, h), cara, x, y, w, h, 0.3, 18)
        if y >= h - 1 and (x + CARAS[cara]) % 3 == 0:
            return None
        return c

    def cinturon(cara, x, y, w, h):
        c = cuero_t(cara, x, y, w, h)
        if y in (0, h - 1):
            return oscuro(c, 0.3)
        if (x + CARAS[cara]) % 6 == 0:
            return LATON[2]                                    # los remaches
        return c

    def hebilla(cara, x, y, w, h):
        if cara == "frente" and 0 < x < w - 1 and 0 < y < h - 1:
            return CUERO[2]
        return LATON[3 + (x + y) % 2]

    def cristal(cara, x, y, w, h):
        c = cristal_t(cara, x, y, w, h)
        if cara in ("der", "izq", "frente", "espalda") and x == w // 2:
            return VETA[4]                                     # la arista
        if y <= 1:
            return VETA[5] if cara != "abajo" else VETA[3]     # la punta, mas clara
        return c

    def costra(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = cristal_t(cara, x, y, w, h)
        if (x * 3 + y * 5 + CARAS[cara]) % 4 == 0:
            return None                                        # entre los cristales asoma el brazo
        return oscuro(c, 0.2) if (x + y) % 3 else c

    def mango(cara, x, y, w, h):
        if y >= h - 6:
            return CUERO[3] if y % 2 else CUERO[2]             # el puno de cuero
        c = madera_t(cara, x, y, w, h)
        if y % 7 == 0:
            return HIERRO[3]                                   # los aros de hierro del mango
        return polvo(c, cara, x, y, w, h, 0.18, 19)

    def pico_hierro(cara, x, y, w, h):
        c = negro_t(cara, x, y, w, h)
        if cara in ("der", "izq") and x == w // 2:
            return VETA[3]                                     # la veta incrustada a lo largo
        if cara in ("der", "izq") and (y == 0 or y == h - 1):
            return claro(c, 0.12)
        return c

    mats = {
        "cabeza": sombra(cabeza, abajo=0.12),
        "torso": sombra(torso),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(lambda c, x, y, w, h: brazo(c, x, y, w, h, True), abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12),
        "antebrazo_i": sombra(lambda c, x, y, w, h: antebrazo(c, x, y, w, h, True), abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.15), "pierna_i": sombra(pierna, abajo=0.15),
        "casco": sombra(casco, abajo=0.1),
        "ala_casco": lambda cara, x, y, w, h: HIERRO[2] if cara != "arriba" else HIERRO[3],
        "cresta": lambda cara, x, y, w, h: HIERRO[4] if (x + y) % 2 else HIERRO[3],
        "candil": candil,
        "llama_candil": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else LLAMA[2],
        "barba": sombra(barba, abajo=0.15),
        "cuello": sombra(cuello_m, abajo=0.1),
        "faldon": sombra(faldon, abajo=0.15),
        "faldon_d": sombra(faldon_pierna, abajo=0.12), "faldon_i": sombra(faldon_pierna, abajo=0.12),
        "cinturon": cinturon,
        "hebilla": hebilla,
        "cadenita": lambda cara, x, y, w, h: LATON[2] if y % 2 else LATON[1],
        "silbato": lambda cara, x, y, w, h: LATON[4] if (x + y) % 2 else LATON[3],
        "llaves": lambda cara, x, y, w, h: HIERRO[4] if (x + y) % 2 else HIERRO[3],
        "asa_farol": lambda cara, x, y, w, h: HIERRO[3],
        "tapa_farol": lambda cara, x, y, w, h: LATON[2] if cara != "abajo" else LATON[1],
        "farol": lambda cara, x, y, w, h: (LLAMA[3] if (x + y) % 2 else LLAMA[2]) if cara in ("frente", "der", "izq", "espalda")
        and 0 < x < w - 1 and 0 < y < h - 1 else LATON[1],
        "cristal": cristal,
        "costra": costra,
        "mango": mango,
        "regaton": lambda cara, x, y, w, h: HIERRO[3] if (x + y) % 2 else HIERRO[2],
        "ojo_pico": lambda cara, x, y, w, h: HIERRO[2] if (x + y) % 3 else LATON[1],
        "pico_hierro": sombra(pico_hierro, abajo=0.1),
        "punta": lambda cara, x, y, w, h: HIERRO[5] if (x + y) % 2 else HIERRO[4],
    }
    return mats


def brillo(variante):
    """Lo que luce: el ojo izquierdo, el candil, el farol, los cristales de la veta y la veta del pico."""
    def ojo(cara, x, y, w, h):
        if cara == "frente" and y == 2 and x in (5, 6, 7):
            return OJO if x == 7 else VETA[4]
        if cara == "frente" and y == 3 and x == 5:
            return VETA[3]
        return None

    def cristal(cara, x, y, w, h):
        if cara in ("der", "izq", "frente", "espalda") and x == w // 2:
            return VETA[5]
        if y <= 1 and cara != "abajo":
            return VETA[6]
        return VETA[3] if (x * 3 + y) % 4 == 0 else None

    def costra(cara, x, y, w, h):
        if cara in ("arriba", "abajo") or (x * 3 + y * 5 + CARAS[cara]) % 4 == 0:
            return None
        return VETA[4] if (x + y) % 3 == 0 else None

    def pico(cara, x, y, w, h):
        if cara in ("der", "izq") and x == w // 2:
            return VETA[4]
        return None

    return {
        "cabeza": ojo,
        "candil": lambda cara, x, y, w, h: (LLAMA[3] if (x + y) % 2 else LLAMA[2]) if cara == "frente" and 0 < y < h - 1 else None,
        "llama_candil": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else None,
        "farol": lambda cara, x, y, w, h: (LLAMA[3] if (x + y) % 2 else LLAMA[2]) if cara in ("frente", "der", "izq", "espalda")
        and 0 < x < w - 1 and 0 < y < h - 1 else None,
        "cristal": cristal,
        "costra": costra,
        "pico_hierro": pico,
        "antebrazo_i": lambda cara, x, y, w, h: VETA[4] if 2 <= y < h - 1 and (x + y) % 3 == 0 else (VETA[5] if y >= h - 1 else None),
        "brazo_i": lambda cara, x, y, w, h: VETA[4] if _veteado(cara, x, y, w, h, 10, 70) else None,
        "torso": lambda cara, x, y, w, h: VETA[4] if cara == "espalda" and x >= w // 2 and _veteado(cara, x, y, w, h, 7, 55) else None,
    }


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14: llega a
# unos 11). _arma = hacia donde apunta el pico (a -Z de la mano). _agarra: la izquierda en el mango, tantos pixeles por detras
# (+) o por delante (-): el capataz lo coge con las dos manos juntas cerca del regaton (la derecha delante).

# de pie: ancho, el pecho fuera, la cabeza un poco gacha; el pico al hombro derecho (la cabeza a la espalda)
BASE = dict(raiz=raiz(0.0), torso=(4, 0, 0), cabeza=(6, 0, 0), muslo_d=(-4, 0, 6), pierna_d=(6, 0, 0), muslo_i=(2, 0, -6),
            pierna_i=(4, 0, 0), brazo_i=(-6, 0, -14), antebrazo_i=(-16, 0, 0))
PORTE = dict(_mano_d=("h", -0.6, -2.4, 4.6), _arma=(0.1, 0.62, -0.78))
# en guardia: el pico a dos manos cruzado por delante, la cabeza arriba a su izquierda, las piernas abiertas
GUARDIA = dict(raiz=raiz(-1.4), torso=(18, 10, 0), cabeza=(-12, -8, 2), muslo_d=(-16, 0, 8), pierna_d=(26, 0, 0),
               muslo_i=(-2, 0, -8), pierna_i=(20, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", 2.0, -6.0, 6.0), _arma=(0.6, 0.6, 0.5), _agarra=4)
# picando la veta (REZA): de cara a la pared, el pico arriba y abajo (da en la pared en el segundo 1,2)
P_ALZA = con(BASE, raiz=raiz(-0.2, 0.8), torso=(-2, 4, 0), cabeza=(-16, 0, 0), muslo_i=(-16, 0, -6), pierna_i=(14, 0, 0),
             _mano_d=("h", 2.5, 7.0, 1.0), _arma=(0.05, 0.62, -0.78), _agarra=4)
P_DA = con(BASE, raiz=raiz(-1.8, -0.8), torso=(26, 0, 0), cabeza=(-12, 0, 0), muslo_i=(-26, 0, -6), pierna_i=(22, 0, 0),
           _mano_d=("h", 2.5, -2.6, 8.4), _arma=(0.05, 0.6, 0.8), _agarra=4)
# de rodillas (al morir y al beberse la veta)
RODILLAS = dict(raiz=raiz(-6.4, 0.8), torso=(24, 0, 0), cabeza=(26, 0, 0), muslo_d=(-10, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-80, 0, -6),
                pierna_i=(80, 0, 0))
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")
BRAZOS = BRAZO_D + ("brazo_i", "antebrazo_i", "mano_i")


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
        return resolver(r, pose)

    # ---- quieto: respira hondo (el pico al hombro es el "porte", que se suma; en guardia, "listo")
    q0 = dict(BASE)
    q1 = con(BASE, torso=(8, 0, 0), cabeza=(2, 0, 0), raiz=raiz(-0.3))
    bucle("quieto", 4.0, [(0.0, q0), (2.0, q1), (4.0, q0)])
    # ---- andar: pesado (se suma al quieto y al porte, o a listo)
    pasos = []
    for k, (md, mi, pd, pi_, ty) in enumerate(((-24, 20, 10, 16, 5), (-6, 4, 6, 30, 0), (20, -24, 16, 10, -5), (4, -6, 30, 6, 0),
                                               (-24, 20, 10, 16, 5))):
        pasos.append((1.4 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, 0), "cabeza": (2, -ty * 0.5, 0), "raiz": raiz(0.6 if k % 2 else 0.0)}))
    bucle("andar", 1.4, pasos)
    qp = fija(con(BASE, PORTE))
    bucle("porte", 3.0, [(0.0, qp), (3.0, qp)], solo=BRAZO_D)
    l0 = fija(LISTO)
    l1 = fija(con(LISTO, raiz=raiz(-2.0), torso=(LISTO["torso"][0] + 4, LISTO["torso"][1] - 3, 0)))
    bucle("listo", 1.8, [(0.0, l0), (0.9, l1), (1.8, l0)])
    # ---- picando la veta (REZA): el golpe da en la pared en el segundo 1,2
    bucle("picando", 1.8, [
        (0.00, P_DA),
        (0.40, con(P_DA, torso=(26, 0, 0))),
        (0.80, P_ALZA),
        (1.05, con(P_ALZA, torso=(-6, 4, 0))),
        (1.20, P_DA),
        (1.80, P_DA),
    ])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 picotazo: el pico a dos manos muy por encima de la cabeza, lo sostiene y lo clava delante
    PT_ALTO = con(L, raiz=raiz(0.0, 1.4), torso=(-16, 6, 0), cabeza=(-24, -4, 0), muslo_i=(-14, 0, -6), pierna_i=(12, 0, 0),
                  _mano_d=("h", 3.0, 7.5, 0.0), _arma=(0.05, 0.5, -0.86), _agarra=4)
    PT_BAJO = con(L, raiz=raiz(-4.0, -2.6), torso=(48, -4, 0), cabeza=(-30, 2, 0), muslo_d=(16, 0, 6), pierna_d=(32, 0, 0),
                  muslo_i=(-54, 0, -6), pierna_i=(56, 0, 0), _mano_d=("h", 3.4, -3.2, 8.0), _arma=(0.05, -0.72, 0.69), _agarra=4)
    accion("picotazo", 1.9, [
        (0.00, L),
        (0.65, PT_ALTO),
        (0.98, con(PT_ALTO, raiz=raiz(0.3, 1.6), torso=(-20, 6, 0))),
        (1.06, con(L, raiz=raiz(-1.4, 0.0), torso=(16, 2, 0), cabeza=(-26, 0, 0), _mano_d=("h", 3.2, 3.0, 8.4), _arma=(0.05, 0.4, 0.92),
                   _agarra=4)),
        (1.12, PT_BAJO),
        (1.45, con(PT_BAJO, torso=(46, -4, 0))),
        (1.90, L),
    ], golpes=(1.12,))
    # ---- 2 barrido: el pico de derecha a izquierda a la altura de la cintura, muy abierto
    BR_D = con(L, raiz=raiz(-1.4, 0.8), torso=(12, 56, 0), cabeza=(-4, -48, 0), _mano_d=("h", 0.6, -4.2, 5.0), _arma=(-1, 0.12, -0.12),
               _agarra=4)
    BR_F = con(L, raiz=raiz(-2.2, -0.6), torso=(18, 10, 0), cabeza=(-8, -10, 0), _mano_d=("h", 3.6, -4.8, 7.0), _arma=(0.2, 0.05, 1),
               _agarra=4)
    BR_I = con(L, raiz=raiz(-2.6, -1.8), torso=(22, -42, 0), cabeza=(-10, 34, 0), muslo_i=(-32, 0, -8), pierna_i=(30, 0, 0),
               _mano_d=("h", 6.0, -4.8, 4.6), _arma=(1, 0.05, 0.3), _agarra=4)
    accion("barrido", 1.5, [
        (0.00, L),
        (0.50, BR_D),
        (0.68, BR_F),
        (0.78, BR_I),
        (1.08, con(BR_I, torso=(22, -46, 0))),
        (1.50, L),
    ], golpes=(0.70,))
    # ---- 3 embestida: el hombro de cristal por delante; se lanza
    EM_CARGA = con(L, raiz=raiz(-3.0, 1.6), torso=(40, -32, 0), cabeza=(-30, 24, 0), muslo_d=(-40, 0, 8), pierna_d=(50, 0, 0),
                   muslo_i=(10, 0, -8), pierna_i=(30, 0, 0), brazo_i=(-30, 0, -10), antebrazo_i=(-60, 0, 0))
    EM_CHOCA = con(L, raiz=raiz(-2.4, -3.0), torso=(44, -36, 0), cabeza=(-34, 28, 0), muslo_d=(24, 0, 8), pierna_d=(20, 0, 0),
                   muslo_i=(-56, 0, -8), pierna_i=(44, 0, 0), brazo_i=(-40, 0, -6), antebrazo_i=(-70, 0, 0))
    accion("embestida", 1.5, [
        (0.00, L),
        (0.35, EM_CARGA),
        (0.60, EM_CHOCA),
        (0.90, con(EM_CHOCA, torso=(40, -32, 0))),
        (1.50, L),
    ], golpes=(0.60,))
    # ---- 4 la veta: alza el pico muy alto y lo clava en el suelo delante (los cristales brotan hacia su presa)
    VT_ALTO = con(L, raiz=raiz(0.4, 1.0), torso=(-22, 0, 0), cabeza=(-26, 0, 0), muslo_d=(-6, 0, 8), pierna_d=(8, 0, 0),
                  muslo_i=(-20, 0, -8), pierna_i=(16, 0, 0), _mano_d=("h", 3.4, 8.0, -0.6), _arma=(0.05, 0.3, -0.95), _agarra=4)
    VT_CLAVA = con(L, raiz=raiz(-5.0, -2.0), torso=(56, -4, 0), cabeza=(-30, 2, 0), muslo_d=(20, 0, 8), pierna_d=(40, 0, 0),
                   muslo_i=(-60, 0, -8), pierna_i=(64, 0, 0), _mano_d=("h", 3.2, -4.6, 6.8), _arma=(0.05, -0.86, 0.5), _agarra=4)
    accion("veta", 2.0, [
        (0.00, L),
        (0.75, VT_ALTO),
        (1.05, con(VT_ALTO, raiz=raiz(0.6, 1.2), torso=(-26, 0, 0))),
        (1.12, con(L, raiz=raiz(-1.6, 0.0), torso=(18, 2, 0), cabeza=(-26, 0, 0), _mano_d=("h", 3.2, 3.0, 8.2), _arma=(0.05, 0.3, 0.95),
                   _agarra=4)),
        (1.18, VT_CLAVA),
        (1.60, con(VT_CLAVA, torso=(54, -4, 0))),
        (2.00, L),
    ], golpes=(1.18,))
    # ---- 5 los barrenos: el pico abajo en la derecha; con la izquierda saca tres cartuchos del cinturon, los acerca al farol y
    # los lanza en abanico por encima del hombro
    BA_COGE = con(BASE, raiz=raiz(-1.0), torso=(10, -16, 0), cabeza=(14, 16, 0), _mano_d=("h", 0.4, -9.4, 2.6), _arma=(0.1, -0.5, 0.86),
                  brazo_i=(30, 0, -26), antebrazo_i=(-70, 0, 0))
    BA_ATRAS = con(BASE, raiz=raiz(-0.8, 1.6), torso=(-8, -34, 0), cabeza=(-14, 26, 0), _mano_d=("h", 0.4, -9.4, 2.6), _arma=(0.1, -0.5, 0.86),
                   brazo_i=(-150, 0, -30), antebrazo_i=(-50, 0, 0), muslo_d=(-14, 0, 8), pierna_d=(14, 0, 0))
    BA_SUELTA = con(BASE, raiz=raiz(-1.8, -1.8), torso=(26, 20, 0), cabeza=(-18, -14, 0), _mano_d=("h", 0.4, -9.4, 2.6), _arma=(0.1, -0.5, 0.86),
                    brazo_i=(-110, 0, -4), antebrazo_i=(-6, 0, 0), muslo_d=(-36, 0, 8), pierna_d=(36, 0, 0))
    accion("barrenos", 1.8, [
        (0.00, L),
        (0.35, BA_COGE),
        (0.60, con(BA_COGE, brazo_i=(10, 0, -30), antebrazo_i=(-90, 0, 0))),
        (0.85, BA_ATRAS),
        (1.00, con(BA_ATRAS, torso=(-10, -38, 0))),
        (1.08, BA_SUELTA),
        (1.40, con(BA_SUELTA, brazo_i=(-60, 0, -10), antebrazo_i=(-10, 0, 0))),
        (1.80, L),
    ], golpes=(1.08,))
    # ---- 6 el derrumbe: dos golpes con el pico al suelo, a su derecha y a su izquierda (la mina tiembla; del techo caen piedras)
    DR_A = con(L, raiz=raiz(-0.4, 1.0), torso=(-18, 30, 0), cabeza=(-20, -20, 0), _mano_d=("h", 2.0, 7.6, 0.6), _arma=(-0.3, 0.4, -0.86),
               _agarra=4)
    DR_B = con(L, raiz=raiz(-4.2, -1.0), torso=(50, 26, 0), cabeza=(-28, -18, 0), muslo_d=(-40, 0, 10), pierna_d=(56, 0, 0),
               _mano_d=("h", 0.0, -4.6, 6.4), _arma=(-0.3, -0.85, 0.42), _agarra=4)
    DR_C = con(L, raiz=raiz(-0.4, 1.0), torso=(-18, -26, 0), cabeza=(-20, 18, 0), _mano_d=("h", 4.6, 7.6, 0.6), _arma=(0.3, 0.4, -0.86),
               _agarra=4)
    DR_D = con(L, raiz=raiz(-4.2, -1.0), torso=(50, -24, 0), cabeza=(-28, 16, 0), muslo_i=(-40, 0, -10), pierna_i=(56, 0, 0),
               _mano_d=("h", 5.4, -4.6, 6.4), _arma=(0.4, -0.85, 0.34), _agarra=4)
    accion("derrumbe", 2.4, [
        (0.00, L),
        (0.50, DR_A),
        (0.80, DR_B),
        (1.10, con(DR_B, torso=(48, 26, 0))),
        (1.35, DR_C),
        (1.62, DR_D),
        (1.95, con(DR_D, torso=(48, -24, 0))),
        (2.40, L),
    ], golpes=(0.80, 1.62))
    # ---- 7 el silbato (su rugido): echa la cabeza atras, se lleva el silbato a la boca con la izquierda y alza el pico
    SB = con(BASE, raiz=raiz(-0.4, 0.6), torso=(-14, 0, 0), cabeza=(-30, 0, 0), _mano_d=("h", 1.6, 8.0, 2.6), _arma=(0.1, 0.9, -0.42),
             brazo_i=(-120, 0, 44), antebrazo_i=(-110, 0, 0))
    accion("silbato", 2.2, [
        (0.00, L),
        (0.40, con(SB, cabeza=(-6, 0, 0), torso=(-4, 0, 0))),
        (0.70, SB),
        (1.70, con(SB, torso=(-18, 0, 0), cabeza=(-34, 0, 0))),
        (2.20, L),
    ])
    # ---- 8 aturdido
    AT = dict(raiz=raiz(-1.4, 2.0), torso=(-10, 0, 8), cabeza=(-22, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(18, 0, 0), _mano_d=("h", -1.0, -8.0, 4.0), _arma=(0.2, -0.7, 0.68), brazo_i=(18, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir: el pico se le escurre (queda clavado delante) y cae de rodillas; se apoya en el mango y se queda asi
    CAE = con(RODILLAS, _mano_d=("h", 1.0, -1.0, 8.6), _arma=(0.0, -0.9, 0.42), brazo_i=(-56, 0, -10), antebrazo_i=(-50, 0, 0))
    accion("morir", 4.0, [
        (0.00, con(BASE, PORTE, torso=(-10, 0, 6), cabeza=(-30, 0, 10))),
        (0.60, con(BASE, torso=(14, 0, 6), cabeza=(10, 0, 10), _mano_d=("h", 0.6, -6.0, 7.0), _arma=(0.0, -0.9, 0.42))),
        (1.30, CAE),
        (2.20, con(CAE, torso=(36, 0, 0), cabeza=(44, 0, 0))),
        (4.00, con(CAE, torso=(40, 0, 0), cabeza=(52, 0, 0))),
    ])
    # ---- 10 volverse: deja de picar y se vuelve hacia quien llega (el giro lo pone el servidor)
    accion("volverse", 1.2, [
        (0.00, P_DA),
        (0.35, con(P_DA, raiz=raiz(-1.2, 0.0), torso=(16, -12, 0), cabeza=(-20, -30, 0))),
        (0.80, con(L, torso=(18, 4, 0), cabeza=(-24, 8, 6))),
        (1.20, L),
    ])
    # ---- 11 beberse la veta (a media vida): clava la mano de cristal en el suelo, de rodillas, y se levanta con la veta dentro
    BV = con(RODILLAS, _mano_d=("h", 0.6, -7.0, 4.0), _arma=(0.2, -0.6, 0.77), brazo_i=(-40, 0, -14), antebrazo_i=(-36, 0, 0),
             torso=(40, 0, 0), cabeza=(20, 0, 0))
    accion("absorber", 2.6, [
        (0.00, L),
        (0.50, BV),
        (1.60, con(BV, torso=(44, 0, 4), cabeza=(-10, 0, 0))),
        (2.10, con(GUARDIA, PORTE, raiz=raiz(-0.6, 0.4), torso=(-20, 0, 0), cabeza=(-30, 0, 0), brazo_i=(-160, 0, -20),
                   antebrazo_i=(-20, 0, 0))),
        (2.60, L),
    ])
    return B, A


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte"].muestra(0))


def base_de(nombre, anims):
    if nombre == "porte":
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte"]]
    return []
