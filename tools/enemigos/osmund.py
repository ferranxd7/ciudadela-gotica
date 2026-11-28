"""
Fray Osmund, el Sacristan del Osario de San Ulfo (parte 7at): el jefe del Relicario. Cuando la Peste Gris lleno el Camposanto de los
Pobres, el sacristan desenterro a los muertos y ordeno sus huesos en la cripta: cuarenta mil, contados uno a uno (libro perdido 37,
"El libro de los huesos"). Murio contando y le enterraron en el Relicario, a los pies de San Ulfo. La noche del cielo rojo se
levanto como los demas, pero no con lo suyo: su cuerpo es un mosaico de huesos de muchos (cada pieza de un tono), cosido con el
cordon de su habito; y a la espalda lleva la aureola que se hizo con los huesos que le sobraron, un aro de huesos con cinco
calaveras y cuatro velas encendidas. Sigue contando. En las cuencas, la lucecita azul de las almas; entre las costillas, otra.

Alto y flaco, encorvado (el juego lo dibuja a 1,5): la capucha calada, el capucho sobre los hombros, el habito pardo de lana basta
lleno de polvo de hueso y roto por el pecho (se ven las costillas), el escapulario, el cordon con sus tres nudos y un rosario de
vertebras con una calaverita; las mangas anchas y rotas, las manos de hueso. En la derecha el **incensario** (de bronce, con su
cadena: dentro arde polvo de hueso) y en la izquierda la **campanilla** de difuntos.

Sus golpes: el incensazo (lo alza por encima del hombro y lo estrella delante), el giro (lo voltea por encima de la cabeza y barre
todo a su alrededor), la corona (alza los brazos: las cinco calaveras de su aureola se sueltan y le orbitan, y luego vuelan), el muro
de huesos (se dobla y hunde las manos hacia el suelo: los huesos brotan en abanico), la campanilla (la toca tres veces: los muertos
se levantan de sus montones; es su rugido), las guirnaldas (alza los brazos a la cupula: caen guirnaldas de huesos), aturdido,
morir (de rodillas, el incensario en el suelo), levantarse (esperaba de rodillas, contando huesos) y alzarse (a media vida: los
muertos le sostienen en el aire; desde entonces flota sobre un haz de brazos de hueso).
"""
import math

from anim import Anim, con, raiz, resolver, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, mezcla, metal, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "osmund"
VARIANTES = ("osmund",)
#: el haz de brazos de hueso que le sostiene en el aire: solo a media vida (lo enseña el modelo del juego)
SOSTEN = ["sosten_a", "sosten_b", "sosten_c", "sosten_d", "sosten_e", "sosten_craneos"]
OCULTAS = {0: list(SOSTEN)}
#: alto y flaco: lo que le da su dibujante en el juego
ESCALA = 1.5
CENTRO_Y = 1.7

#: la aureola: su centro (respecto al torso), su radio, y donde van las calaveras y las velas (grados desde arriba)
AURA_C = (0.0, -17.0, 4.9)
AURA_R = 8.5
AURA_CALAVERAS = (0, 72, 144, 216, 288)
AURA_VELAS = (36, 108, 252, 324)

# ---------------------------------------------------------------- el modelo


def _aro(a):
    """El punto de la aureola a `a` grados desde arriba (en el plano de la espalda)."""
    r = math.radians(a)
    return (AURA_C[0] + AURA_R * math.sin(r), AURA_C[1] - AURA_R * math.cos(r), AURA_C[2])


def rig():
    r = Rig(NOMBRE, 128)
    # flaco y alto, encorvado (el brazo mide 15: la mano llega a unos 12 de su hombro)
    humanoide(r, torso=(8, 13, 5), cabeza=(7, 8, 7), brazo=(3, 15, 3), pierna=(3, 14, 3), hombro=0.4)
    # ---- la capucha calada (por delante, abierta: se ve la calavera) con su pico a la espalda; la mandibula que cuelga
    r.pieza("capucha", "cabeza", (0, 0, 0), boxes=[
        Box(-3.5, -8, -3.5, 7, 8, 7, "capucha", grow=g(0.65)),
        Box(-2.0, -6.4, 4.1, 4.0, 6.4, 1.4, "capucha_pico"),
    ])
    r.pieza("mandibula", "cabeza", (0, -1.2, -1.4), rot=(10, 0, 0), boxes=[Box(-2.5, 0, -2.2, 5.0, 2.0, 3.4, "mandibula")])
    # ---- el capucho sobre los hombros, el escapulario (solo a la espalda: por delante el habito esta roto) y el cordon con sus
    # nudos, el rosario de vertebras y su calaverita
    r.pieza("capucho", "torso", (0, -13, 0), boxes=[Box(-5.0, -0.8, -3.1, 10.0, 3.6, 6.2, "capucho", grow=g(0.2))])
    r.pieza("escapulario", "torso", (0, 0, 0), boxes=[Box(-2.0, -12.4, 2.55, 4.0, 17.0, 0.5, "escapulario")])
    r.pieza("cordon", "torso", (0, 0, 0), boxes=[
        Box(-4.3, -2.0, -2.8, 8.6, 1.3, 5.6, "cordon"),
        Box(1.5, -0.8, -3.15, 0.8, 9.5, 0.6, "cordon_cabo"),
        Box(-2.9, -0.8, -3.1, 0.7, 6.6, 0.6, "rosario"),
        Box(-3.4, 5.8, -3.5, 1.8, 1.8, 1.6, "rosario_calavera"),
    ])
    # ---- el habito hasta los pies: un faldon por pierna (mas ancho que ella; el izquierdo un pelo mas hondo, que no se pisen las
    # caras) y las mangas anchas y rotas
    for n, hondo in (("d", 0.0), ("i", 0.12)):
        r.pieza(f"falda_{n}", f"muslo_{n}", (0, 0, 0), boxes=[
            Box(-3.0, -0.8, -3.2 - hondo, 6.0, 8.0, 6.4 + 2 * hondo, "falda", uv="falda" if n == "d" else "falda_i")])
        r.pieza(f"bajo_{n}", f"pierna_{n}", (0, 0, 0), boxes=[
            Box(-3.1, 0, -3.3 - hondo, 6.2, 6.4, 6.6 + 2 * hondo, "bajo", uv="bajo" if n == "d" else "bajo_i")])
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 0.4, 0), boxes=[Box(-2.5, 0, -2.5, 5.0, 6.2, 5.0, "manga", uv="manga" if n == "d" else "manga_i")])
    # ---- la aureola de huesos a la espalda: diez huesos en aro, cinco calaveras (mirando adelante) y cuatro velas derechas
    for k in range(10):
        a = 18 + 36 * k
        x, y, z = _aro(a)
        r.pieza(f"aro_{k}", "torso", (x, y, z), rot=(0, 0, a - 90), boxes=[Box(-0.7, -2.9, -0.7, 1.4, 5.8, 1.4, "aro", uv="aro")])
    for k, a in enumerate(AURA_CALAVERAS):
        x, y, z = _aro(a)
        r.pieza(f"calavera_{k}", "torso", (x, y, z - 0.2), boxes=[Box(-1.5, -1.5, -1.5, 3.0, 3.0, 3.0, "calavera_aro", uv="calavera_aro")])
    for k, a in enumerate(AURA_VELAS):
        x, y, z = _aro(a)
        if abs(math.cos(math.radians(a))) < 0.5:
            x += 1.6 * math.copysign(1, math.sin(math.radians(a)))
        r.pieza(f"vela_{k}", "torso", (x, y - 0.6, z), boxes=[
            Box(-0.55, -2.8, -0.55, 1.1, 2.8, 1.1, "vela", uv="vela"),
            Box(-0.3, -3.9, -0.3, 0.6, 1.1, 0.6, "llama", uv="llama"),
        ])
    # ---- el incensario (mano derecha; apunta a -Z de la mano): la cadena, la tapa en punta, el cuerpo de bronce con sus
    # agujeros (dentro arde el polvo de hueso) y el pie
    r.pieza("incensario", "mano_d", (0, 0, 0), boxes=[
        Box(-0.35, -0.35, -12.0, 0.7, 0.7, 12.0, "cadena"),
        Box(-1.2, -1.2, -13.3, 2.4, 2.4, 1.3, "tapa"),
        Box(-1.9, -1.9, -17.5, 3.8, 3.8, 4.2, "incensario"),
        Box(-1.0, -1.0, -18.1, 2.0, 2.0, 0.6, "pie"),
    ])
    # ---- la campanilla de difuntos (mano izquierda): el mango, la campana y la boca
    r.pieza("campanilla", "mano_i", (0, 0, 0), boxes=[
        Box(-0.45, -0.45, -2.6, 0.9, 0.9, 2.6, "mango_campana"),
        Box(-1.4, -1.4, -5.6, 2.8, 2.8, 3.0, "campana"),
        Box(-1.75, -1.75, -6.2, 3.5, 3.5, 0.6, "boca_campana"),
    ])
    # ---- a media vida, lo que le sostiene en el aire: brazos de hueso que suben del suelo hasta sus pies (cuelgan de la raiz:
    # el juego lo levanta 1,5 bloques, 16 pixeles del modelo) y un monton de craneos abajo
    for n, (x, z, rx, rz, largo) in zip("abcde", ((-2.6, -1.2, 10, -12, 17.0), (2.6, -1.0, 8, 12, 17.0), (0.0, 2.4, -14, 0, 16.5),
                                                    (-1.4, 1.2, -6, -6, 16.0), (1.6, -2.4, 16, 6, 16.0))):
        r.pieza(f"sosten_{n}", "raiz", (x, 0.4, z), rot=(rx, 0, rz), boxes=[
            Box(-0.7, 0, -0.7, 1.4, largo, 1.4, "sosten", uv="sosten"),
            Box(-1.2, -0.4, -1.2, 2.4, 1.6, 2.4, "sosten_mano", uv="sosten_mano"),
        ])
    r.pieza("sosten_craneos", "raiz", (0, 15.0, 0), boxes=[
        Box(-3.5, -1.0, -3.5, 3.0, 3.0, 3.0, "craneo_sosten", uv="craneo_sosten"),
        Box(0.6, -0.6, -2.8, 3.0, 3.0, 3.0, "craneo_sosten", uv="craneo_sosten"),
        Box(-1.4, -1.6, 0.8, 3.0, 3.0, 3.0, "craneo_sosten", uv="craneo_sosten"),
        Box(-4.6, 1.0, -1.0, 9.2, 1.4, 4.4, "huesos_sosten"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas
# Los huesos de muchos muertos (cada pieza de un tono: el amarillento, el gris y el pardo), el habito pardo de lana basta lleno de
# polvo de hueso, el cordon, el bronce del incensario y de la campanilla, la cera y las llamas de las velas, las brasas del incensario
# y la lucecita azul de las almas.

HUESOS = (["#4a4334", "#6d6450", "#8f8569", "#ada27f", "#c4b994", "#d8cfae"],
          ["#3e3f3c", "#5d5f5a", "#7c7f78", "#9a9d95", "#b5b8af", "#cdd0c6"],
          ["#3d2f22", "#5a4733", "#7a6247", "#967c5c", "#ae9472", "#c6ad8a"])
HUECO = "#0b0908"
ALMA, ALMA_B, ALMA_C = "#4fb4f0", "#a6e2ff", "#2a6a9c"
HABITO = ["#120e0b", "#1b1510", "#251c15", "#30251b", "#3c2e22", "#4a3a2b"]
POLVO = ["#4e483d", "#625a4c", "#766d5c"]
CORDON = ["#3b3020", "#5a4a30", "#7a6644", "#94805a"]
BRONCE = ["#1f150a", "#3a2810", "#5a3e18", "#7c5822", "#a07632", "#c49a4e"]
BRASA = ["#4a1406", "#8a2a0a", "#d0521a", "#ff8a3a", "#ffc27a"]
CERA = ["#8c8166", "#b3a782", "#d6cba4", "#ece3c2"]
LLAMA = ["#5a2c06", "#a0560e", "#e09a2a", "#ffd36a", "#fff2c0"]

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _azar01(*v):
    h = 2166136261
    for n in v:
        h = ((h ^ (int(n) & 0xffffffff)) * 16777619) & 0xffffffff
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    h ^= h >> 15
    return h / 4294967296.0


def _tonos(pieza):
    """Los huesos de esa pieza: de uno de los tres muertos (cada una del suyo)."""
    k = (sum(ord(c) for c in pieza) * 7 + 2) % 3
    return k, HUESOS[k]


def materiales(variante):
    texturas = {}

    def hueso_de(pieza):
        if pieza not in texturas:
            k, tonos = _tonos(pieza)
            texturas[pieza] = (ruido(tonos[2:6], 9300 + k * 17 + len(pieza), grano=0.45, base=0.55), tonos)
        return texturas[pieza]

    habito_t = tela(HABITO[1:6], 9321, pliegue=3, grano=0.18, luz=0.12)
    bronce_t = metal(BRONCE[1:6], 9322)

    def polvo(c, cara, x, y, w, h, cuanto=0.2, sal=0):
        """El polvo de hueso que lo cubre (mas abajo, mas)."""
        kc = CARAS[cara]
        s0 = w * 31 + h * 17 + sal * 101
        g_ = 0.6 * _azar01(s0, kc, x // 2, y // 2) + 0.4 * _azar01(s0 + 3, kc, x, y)
        k = 0.6 * cuanto * (0.6 + 0.8 * y / max(1, h))
        if g_ < k * 0.3:
            return POLVO[1 + (x + y) % 2]
        if g_ < k:
            return mezcla(c, POLVO[0], 0.45)
        return c

    def costura(cara, x, y, w, h, sal):
        """Donde se juntan dos huesos de dos muertos: una raya quebrada oscura."""
        kc = CARAS[cara]
        a = (x * 5 + y * 3 + kc * 7 + sal) % 13
        return a == 0 and _azar01(sal, kc, x // 2, y // 2) < 0.7

    def cabeza(cara, x, y, w, h):
        # la calavera hecha de tres: la frente de uno, los pomulos de otro, los dientes de un tercero (las costuras entre medias)
        t, tonos = hueso_de("cabeza")
        t2, tonos2 = hueso_de("pomulo")
        if cara == "frente":
            C = {"F": tonos[4], "f": tonos[3], "c": tonos[2], "S": HUECO, "s": "#1a1512", "P": tonos2[4], "p": tonos2[3], "o": tonos2[2],
                 "N": HUECO, "D": HUESOS[2][5], "d": HUESOS[2][3], "-": "#2a2219"}
            v = dibujo([
                "cfFfFfc",
                "fF-FfFf",
                "pSSpSSp",
                "pSspsSo",
                "-pPNPp-",
                "opNNNpo",
                "DdDdDdD",
                "dDdDdDd",
            ], C)(cara, x, y, w, h)
            return v if v is not None else tonos[3]
        if cara == "abajo":
            return HUECO
        c = t(cara, x, y, w, h)
        if costura(cara, x, y, w, h, 3):
            return oscuro(c, 0.5)
        return c

    def mandibula(cara, x, y, w, h):
        t, tonos = hueso_de("mandibula")
        if cara == "arriba":
            return None
        if cara == "frente":
            if y == 0:
                return tonos[5] if x % 2 == 0 else HUECO      # los dientes de abajo (le faltan)
            return t(cara, x, y, w, h)
        return oscuro(t(cara, x, y, w, h), 0.12)

    def capucha(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "frente" and 1 <= y <= h - 1 and 1 <= x <= w - 2:
            return None                                       # la calavera al aire
        c = habito_t(cara, x, y, w, h)
        if cara == "frente":
            return oscuro(c, 0.35)                            # el borde, por dentro en sombra
        if cara in ("der", "izq") and delante(cara, x, w) <= 1:
            return oscuro(c, 0.3)
        return polvo(c, cara, x, y, w, h, 0.16, 1)

    def capucha_pico(cara, x, y, w, h):
        return polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.2, 2)

    def torso(cara, x, y, w, h):
        if cara == "abajo":
            return HABITO[1]
        if cara == "arriba":
            return HABITO[2]
        c = polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.2, 4)
        # el pecho roto: las costillas (de dos muertos), y entre ellas la lucecita de las almas
        if cara == "frente" and 1 <= x <= w - 2 and 2 <= y <= 8:
            borde = (x in (1, w - 2) and (y + x) % 3 == 0) or y in (2, 8) and (x * 3) % 4 == 0
            if borde:
                return oscuro(c, 0.4)                         # lo roto del habito
            if x in (w // 2 - 1, w // 2):
                return HUESOS[0][4] if y % 3 else HUESOS[0][3]    # el esternon
            if y % 2 == 1:
                return ALMA_C                                 # entre las costillas (luce)
            k = 1 if x < w // 2 else 2
            return HUESOS[k][4 if (x + y) % 3 else 3]
        if cara == "espalda" and 2 <= x <= w - 3 and 3 <= y <= 9 and (x * 7 + y * 3) % 17 == 0:
            return HUESOS[1][3]                               # asoma una vertebra por un roto
        if (x * 7 + y * 5 + CARAS[cara]) % 31 == 0:
            return oscuro(c, 0.4)                             # un remiendo
        return c

    def brazo(cara, x, y, w, h):
        c = polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.18, 5)
        if (x + y * 3 + CARAS[cara]) % 19 == 0:
            return oscuro(c, 0.35)
        return c

    def antebrazo(pieza):
        def f(cara, x, y, w, h):
            # los huesos del antebrazo (de otro muerto) y la mano: los dedos abajo
            t, tonos = hueso_de(pieza)
            if y >= h - 1:
                return tonos[1] if (x + CARAS[cara]) % 2 else tonos[3]
            c = t(cara, x, y, w, h)
            if y == h - 2:
                return claro(c, 0.08)                         # los nudillos
            return c
        return f

    def muslo(cara, x, y, w, h):
        return habito_t(cara, x, y, w, h)

    def pierna(pieza):
        def f(cara, x, y, w, h):
            # la tibia y el pie de hueso (asoman por debajo del habito)
            t, tonos = hueso_de(pieza)
            if y >= h - 1:
                return tonos[2] if (x + y) % 2 else tonos[1]
            return t(cara, x, y, w, h)
        return f

    def capucho(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.22, 6)
        if y == h - 1 and (x * 5 + CARAS[cara]) % 4 == 0:
            return None                                       # el borde deshilachado
        return c

    def escapulario(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if y > h - 2 - (x * 3) % 3:
            return None                                       # deshilachado
        c = oscuro(habito_t(cara, x, y, w, h), 0.15)
        if cara in ("frente", "espalda") and y in (3, 4) and x in (1, 2):
            return HUESOS[0][3]                               # la cruz de hueso cosida al pecho
        if cara in ("frente", "espalda") and y == 2 and x in (1, 2) or y == 5 and x in (1, 2) and cara == "frente":
            return HUESOS[0][2]
        return polvo(c, cara, x, y, w, h, 0.16, 7)

    def cordon(cara, x, y, w, h):
        return CORDON[(x + y) % 3 + 1] if (x + 2 * y) % 4 else CORDON[0]

    def cordon_cabo(cara, x, y, w, h):
        if y in (2, 5, 8):
            return CORDON[3]                                  # los tres nudos
        return CORDON[1 + (y % 2)]

    def rosario(cara, x, y, w, h):
        k, tonos = _tonos("rosario")
        return tonos[4] if y % 2 == 0 else CORDON[0]          # vertebras ensartadas

    def rosario_calavera(cara, x, y, w, h):
        t, tonos = hueso_de("rosario_calavera")
        if cara == "frente" and y == 0 and x in (0, 1):
            return HUECO
        return t(cara, x, y, w, h)

    def falda(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None if cara == "abajo" else HABITO[1]
        c = polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.26, 8)
        if (x * 3 + y * 7 + CARAS[cara]) % 29 == 0:
            return None                                       # un agujero
        return c

    def bajo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        corte = h - 1 - ((x * 5 + CARAS[cara] * 3) % 3)
        if y > corte:
            return None                                       # el bajo roto: asoman los huesos del pie
        c = polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.34, 9)
        return oscuro(c, 0.06 * y / max(1, h))

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        corte = h - 1 - ((x * 7 + CARAS[cara] * 2) % 3)
        if y > corte or (x * 5 + y * 3 + CARAS[cara]) % 23 == 0:
            return None                                       # rota por abajo y por algun sitio
        return polvo(habito_t(cara, x, y, w, h), cara, x, y, w, h, 0.22, 10)

    def aro(cara, x, y, w, h):
        # cada hueso del aro, de un muerto (por el sitio en la textura) y con sus cabezas mas claras
        k = (x // 2 + CARAS[cara]) % 3
        c = HUESOS[k][3 + (x + y) % 2]
        if y in (0, h - 1):
            return claro(c, 0.1)
        if (x + y * 3) % 7 == 0:
            return oscuro(c, 0.25)
        return c

    def calavera_aro(cara, x, y, w, h):
        t, tonos = hueso_de("calavera_aro")
        if cara == "frente":
            if y == 1 and x in (0, 2):
                return HUECO                                  # las cuencas
            if y == 2 and x == 1:
                return "#1a1512"
            return t(cara, x, y, w, h)
        if cara == "abajo":
            return tonos[1]
        return t(cara, x, y, w, h)

    def vela(cara, x, y, w, h):
        if cara == "arriba":
            return CERA[3]
        c = CERA[1 + (x + y) % 2]
        if y >= h - 1 and (x + CARAS[cara]) % 2 == 0:
            return CERA[3]                                    # la cera que chorrea
        return c

    def cadena(cara, x, y, w, h):
        return BRONCE[3] if (y // 1) % 2 else BRONCE[1]

    def incensario(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return bronce_t(cara, x, y, w, h)
        # el bronce labrado con sus agujeros (por los agujeros, las brasas)
        if 1 <= y <= h - 2 and (x + y) % 2 == 0 and 0 < x < w - 1:
            return BRASA[2] if (x * 3 + y) % 3 else BRASA[1]
        c = bronce_t(cara, x, y, w, h)
        if y in (0, h - 1):
            return claro(c, 0.12)
        return c

    def tapa(cara, x, y, w, h):
        return BRONCE[4] if (x + y) % 2 else BRONCE[3]

    def campana(cara, x, y, w, h):
        c = bronce_t(cara, x, y, w, h)
        if cara not in ("arriba", "abajo") and y == h - 1:
            return claro(c, 0.15)
        return c

    def boca_campana(cara, x, y, w, h):
        if cara == "abajo":
            return BRONCE[0] if 0 < x < w - 1 and 0 < y < h - 1 else BRONCE[3]
        return BRONCE[4]

    def sosten(cara, x, y, w, h):
        # un brazo de hueso largo (radio y cubito de otro muerto cada uno)
        k = (x + CARAS[cara] * 2) % 3
        c = HUESOS[k][3 if x % 2 == 0 else 2]
        if y % 6 == 0:
            return claro(c, 0.1)                              # el codo, la muneca
        if (x * 7 + y * 3 + CARAS[cara]) % 13 == 0:
            return oscuro(c, 0.3)
        return c

    def sosten_mano(cara, x, y, w, h):
        k, tonos = _tonos("sosten_mano")
        return tonos[4] if (x + y) % 2 else tonos[2]          # los dedos que le cogen los pies

    def craneo_sosten(cara, x, y, w, h):
        t, tonos = hueso_de("craneo_sosten")
        if cara == "frente" and y == 1 and x in (0, 2):
            return HUECO
        if cara == "frente" and y == 2 and x == 1:
            return HUECO
        return t(cara, x, y, w, h)

    def huesos_sosten(cara, x, y, w, h):
        k = (x // 2 + CARAS[cara]) % 3
        c = HUESOS[k][2 + (x + y) % 3]
        return oscuro(c, 0.2) if (x * 3 + y) % 5 == 0 else c

    mats = {
        "cabeza": sombra(cabeza, abajo=0.1), "mandibula": mandibula,
        "capucha": sombra(capucha, abajo=0.15), "capucha_pico": sombra(capucha_pico, abajo=0.2),
        "torso": sombra(torso), "capucho": sombra(capucho, abajo=0.1),
        "brazo_d": sombra(brazo, abajo=0.12), "brazo_i": sombra(brazo, abajo=0.12),
        "antebrazo_d": sombra(antebrazo("antebrazo_d"), abajo=0.1), "antebrazo_i": sombra(antebrazo("antebrazo_i"), abajo=0.1),
        "muslo_d": sombra(muslo), "muslo_i": sombra(muslo),
        "pierna_d": sombra(pierna("pierna_d"), abajo=0.1), "pierna_i": sombra(pierna("pierna_i"), abajo=0.1),
        "escapulario": escapulario, "cordon": cordon, "cordon_cabo": cordon_cabo, "rosario": rosario,
        "rosario_calavera": rosario_calavera,
        "falda": sombra(falda, abajo=0.1), "bajo": sombra(bajo, abajo=0.15), "manga": sombra(manga, abajo=0.12),
        "aro": aro, "calavera_aro": sombra(calavera_aro, abajo=0.15), "vela": vela,
        "llama": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else LLAMA[2],
        "cadena": cadena, "tapa": tapa, "incensario": sombra(incensario, abajo=0.12),
        "pie": lambda cara, x, y, w, h: BRONCE[2],
        "mango_campana": lambda cara, x, y, w, h: CORDON[1] if (x + y) % 2 else CORDON[2],
        "campana": sombra(campana, abajo=0.1), "boca_campana": boca_campana,
        "sosten": sombra(sosten, abajo=0.1), "sosten_mano": sosten_mano, "craneo_sosten": sombra(craneo_sosten, abajo=0.15),
        "huesos_sosten": huesos_sosten,
    }
    return mats


def brillo(variante):
    """Lo que luce: las almas de las cuencas y de entre las costillas, las llamas de las velas y las brasas del incensario."""
    C = {"R": ALMA, "r": ALMA_B, "c": ALMA_C}
    ojos = dibujo([
        ".......",
        ".......",
        ".rR.Rr.",
        "..c..c.",
    ], C)

    def costillas(cara, x, y, w, h):
        if cara == "frente" and 2 <= x <= w - 3 and 3 <= y <= 7 and y % 2 == 1 and x not in (w // 2 - 1, w // 2):
            return ALMA if (x + y) % 3 else ALMA_B
        return None

    def incensario(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if 1 <= y <= h - 2 and (x + y) % 2 == 0 and 0 < x < w - 1:
            return BRASA[3] if (x * 3 + y) % 3 else BRASA[2]
        return None

    def craneo(cara, x, y, w, h):
        return ALMA if cara == "frente" and y == 1 and x in (0, 2) else None

    return {
        "cabeza": por_cara(liso(None), frente=ojos),
        "torso": costillas,
        "llama": lambda cara, x, y, w, h: LLAMA[3 + (x + y) % 2] if cara != "abajo" else None,
        "incensario": incensario,
        "calavera_aro": craneo,
        "craneo_sosten": craneo,
    }


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos 12).
# _arma = hacia donde apunta la cadena del incensario (a -Z de la mano derecha); _arma_i = hacia donde apunta la campanilla.

# de pie: encorvado, la cabeza adelantada; las rodillas un poco dobladas
BASE = dict(raiz=raiz(-0.5), torso=(14, 0, 0), cabeza=(-6, 0, 0), mandibula=(0, 0, 0),
            muslo_d=(-6, 0, 3), pierna_d=(10, 0, 0), muslo_i=(-2, 0, -3), pierna_i=(8, 0, 0))
# lo que lleva: el incensario colgando por delante (la mano a la altura del pecho) y la campanilla colgando al costado
PORTE = dict(_mano_d=("h", 1.2, -5.0, 5.5), _arma=(0.05, -1.0, 0.12), _mano_i=("h", -0.6, -11.0, 2.4), _arma_i=(0.0, -1.0, 0.2))
# en guardia: mas abierto de piernas, el incensario a su derecha listo para voltearlo, la campanilla delante
GUARDIA = dict(raiz=raiz(-1.2), torso=(18, 8, 0), cabeza=(-14, -6, 0), mandibula=(6, 0, 0),
               muslo_d=(-12, 0, 6), pierna_d=(20, 0, 0), muslo_i=(0, 0, -6), pierna_i=(14, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", -2.4, -5.0, 3.6), _arma=(-0.45, -0.88, 0.15), _mano_i=("h", 1.4, -5.6, 6.4), _arma_i=(0.1, -1.0, 0.35))
# esperando (REZA): de rodillas, contando huesos; el incensario en el suelo, delante
RODILLAS = dict(raiz=raiz(-6.6, 0.6), torso=(30, 0, 0), cabeza=(24, 0, 0), muslo_d=(-10, 0, 4), pierna_d=(96, 0, 0), muslo_i=(-80, 0, -4),
                pierna_i=(80, 0, 0))
CUENTA = con(RODILLAS, _mano_d=("h", 0.0, -11.5, 6.0), _arma=(0.1, -0.08, 1.0))
# flotando (a media vida): derecho, el pecho fuera, la cabeza alta; las piernas cuelgan; los brazos abiertos
FLOTA = dict(raiz=raiz(0.0), torso=(-4, 0, 0), cabeza=(-12, 0, 0), mandibula=(8, 0, 0), muslo_d=(-8, 0, 4), pierna_d=(16, 0, 0),
             muslo_i=(-3, 0, -4), pierna_i=(20, 0, 0),
             _mano_d=("h", -5.0, -6.0, 4.0), _arma=(-0.35, -1.0, 0.1), _mano_i=("h", 5.0, -6.0, 4.0), _arma_i=(0.35, -1.0, 0.1))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")


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

    # ---- quieto: respira despacio, la mandibula se le mueve (cuenta por lo bajo); lo que lleva es el "porte", que se suma
    q0 = dict(BASE)
    q1 = con(BASE, torso=(18, 0, 0), cabeza=(-8, 0, 0), mandibula=(8, 0, 0), raiz=raiz(-0.8))
    bucle("quieto", 4.0, [(0.0, q0), (1.0, con(q0, mandibula=(7, 0, 0))), (2.0, q1), (3.0, con(q0, mandibula=(5, 0, 0))), (4.0, q0)])
    # ---- andar: arrastrando los pies (se suma al quieto y al porte, o a listo)
    pasos = []
    for k, (md, mi, pd, pi_, ty) in enumerate(((-18, 14, 10, 14, 4), (-4, 3, 6, 24, 0), (14, -18, 14, 10, -4), (3, -4, 24, 6, 0),
                                               (-18, 14, 10, 14, 4))):
        pasos.append((1.6 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, 0), "cabeza": (0, -ty * 0.5, 0), "raiz": raiz(0.4 if k % 2 else 0.0)}))
    bucle("andar", 1.6, pasos)
    # el porte: el incensario se mece un poco al colgar de la mano
    p0 = fija(con(BASE, PORTE))
    p1 = fija(con(BASE, PORTE, _arma=(0.18, -1.0, 0.22)))
    p2 = fija(con(BASE, PORTE, _arma=(-0.12, -1.0, 0.02)))
    bucle("porte", 3.0, [(0.0, p0), (1.0, p1), (2.0, p2), (3.0, p0)], solo=BRAZOS)
    # en guardia: el incensario va y viene a su derecha (como un peso de reloj)
    l0 = fija(LISTO)
    l1 = fija(con(LISTO, raiz=raiz(-1.8), torso=(LISTO["torso"][0] + 4, LISTO["torso"][1] - 3, 0), _arma=(-0.3, -0.9, 0.45)))
    l2 = fija(con(LISTO, _arma=(-0.55, -0.85, -0.15)))
    bucle("listo", 1.8, [(0.0, l0), (0.6, l1), (1.2, l2), (1.8, l0)])
    # ---- contando (REZA): de rodillas, coge un hueso del suelo con la izquierda, lo mira, asiente y lo deja (cada 2,4 s)
    c_coge = fija(con(CUENTA, _mano_i=("h", 1.0, -11.5, 7.0), _arma_i=(0.0, -0.6, 0.8)))
    c_mira = fija(con(CUENTA, cabeza=(10, 0, 0), _mano_i=("h", -1.0, -5.5, 7.0), _arma_i=(-0.5, 0.2, 0.84), mandibula=(8, 0, 0)))
    c_asiente = fija(con(CUENTA, cabeza=(22, 0, 0), _mano_i=("h", -1.0, -5.8, 7.0), _arma_i=(-0.5, 0.2, 0.84)))
    bucle("contando", 2.4, [(0.0, c_coge), (0.6, c_mira), (1.2, c_asiente), (1.6, c_mira), (2.4, c_coge)])
    # ---- flotando (a media vida): sube y baja despacio, los pies colgando
    f0 = fija(FLOTA)
    f1 = fija(con(FLOTA, raiz=raiz(0.8), torso=(-6, 0, 0), pierna_d=(22, 0, 0), pierna_i=(14, 0, 0), _arma=(-0.2, -1.0, 0.3),
                  _arma_i=(0.2, -1.0, 0.3)))
    bucle("flotando", 3.0, [(0.0, f0), (1.5, f1), (3.0, f0)])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 incensazo: alza el incensario por encima del hombro derecho (la cadena atras) y lo estrella delante, en el suelo
    IN_ALTO = con(L, raiz=raiz(-0.2, 0.8), torso=(-12, 18, 0), cabeza=(-20, -12, 0), muslo_i=(-14, 0, -6), pierna_i=(12, 0, 0),
                  _mano_d=("h", 0.6, 7.4, -1.6), _arma=(0.15, 0.62, -0.78))
    IN_BAJO = con(L, raiz=raiz(-3.4, -2.0), torso=(44, -12, 0), cabeza=(-30, 8, 0), muslo_d=(16, 0, 6), pierna_d=(30, 0, 0),
                  muslo_i=(-50, 0, -6), pierna_i=(52, 0, 0), _mano_d=("h", 2.6, -3.6, 9.4), _arma=(0.05, -0.62, 0.78))
    accion("incensazo", 1.9, [
        (0.00, L),
        (0.60, IN_ALTO),
        (0.95, con(IN_ALTO, raiz=raiz(0.2, 1.0), torso=(-16, 20, 0))),
        (1.06, con(L, raiz=raiz(-1.2, 0.0), torso=(14, 4, 0), cabeza=(-24, 0, 0), _mano_d=("h", 2.2, 3.4, 8.8), _arma=(0.05, 0.55, 0.83))),
        (1.15, IN_BAJO),
        (1.50, con(IN_BAJO, torso=(42, -12, 0))),
        (1.90, L),
    ], golpes=(1.15,))
    # ---- 2 giro: lo voltea por encima de la cabeza (una vuelta y cuarto) y lo baja barriendo a su alrededor
    G_ARRIBA = con(L, raiz=raiz(-0.4, 0.4), torso=(-6, 0, 0), cabeza=(-24, 0, 0), _mano_d=("h", 2.6, 9.4, 1.6))
    vueltas = [(0.00, L)]
    dirs = ((-1.0, 0.12, 0.1), (0.0, 0.12, 1.0), (1.0, 0.12, 0.0), (0.0, 0.12, -1.0), (-1.0, 0.12, 0.0))
    for k, d in enumerate(dirs):
        vueltas.append((0.30 + 0.13 * k, con(G_ARRIBA, torso=(-6, -14 + 7 * k, 0), _arma=d)))
    G_BARRE = con(L, raiz=raiz(-2.0, -0.6), torso=(24, -36, 0), cabeza=(-16, 26, 0), muslo_i=(-30, 0, -8), pierna_i=(28, 0, 0),
                  _mano_d=("h", 6.0, -1.0, 6.0), _arma=(1.0, -0.15, 0.25))
    vueltas += [
        (0.92, con(L, raiz=raiz(-1.2, 0.2), torso=(10, 30, 0), cabeza=(-14, -20, 0), _mano_d=("h", -3.6, 0.0, 5.0), _arma=(-1.0, -0.1, 0.2))),
        (1.00, con(L, raiz=raiz(-1.8), torso=(18, 0, 0), cabeza=(-16, 0, 0), _mano_d=("h", 2.0, -0.6, 8.0), _arma=(0.1, -0.12, 1.0))),
        (1.10, G_BARRE),
        (1.40, con(G_BARRE, torso=(22, -40, 0))),
        (1.80, L),
    ]
    accion("giro", 1.8, vueltas, golpes=(1.0,))
    # ---- 3 la corona: alza los brazos abiertos y echa la cabeza atras: las calaveras de su aureola se sueltan
    CO = con(BASE, raiz=raiz(0.4, 0.4), torso=(-14, 0, 0), cabeza=(-30, 0, 0), mandibula=(14, 0, 0),
             _mano_d=("h", -6.0, 7.6, 3.0), _arma=(-0.3, -1.0, 0.25), _mano_i=("h", 6.0, 7.6, 3.0), _arma_i=(0.3, 0.3, 0.9))
    accion("corona", 1.6, [
        (0.00, L),
        (0.45, con(CO, torso=(-4, 0, 0), cabeza=(-10, 0, 0), _mano_d=("h", -4.0, 2.0, 4.0), _mano_i=("h", 4.0, 2.0, 4.0))),
        (0.90, CO),
        (1.20, con(CO, torso=(-18, 0, 0), cabeza=(-34, 0, 0))),
        (1.60, L),
    ], golpes=(0.9,))
    # ---- 4 el muro de huesos: se dobla y hunde las dos manos hacia el suelo, delante (los huesos brotan en abanico)
    MU_ALTO = con(L, raiz=raiz(0.2, 0.8), torso=(-10, 0, 0), cabeza=(-22, 0, 0), _mano_d=("h", -1.0, 6.8, 4.0), _arma=(-0.2, -1.0, 0.3),
                  _mano_i=("h", 1.0, 6.8, 4.0), _arma_i=(0.1, 0.4, 0.9))
    MU_BAJO = con(L, raiz=raiz(-3.6, -1.0), torso=(56, 0, 0), cabeza=(-36, 0, 0), mandibula=(14, 0, 0), muslo_d=(-30, 0, 6), pierna_d=(46, 0, 0),
                  muslo_i=(-30, 0, -6), pierna_i=(46, 0, 0), _mano_d=("h", 2.0, -6.0, 9.6), _arma=(0.0, -0.25, 1.0),
                  _mano_i=("h", -2.0, -6.0, 9.6), _arma_i=(0.0, -1.0, 0.3))
    accion("muro", 2.0, [
        (0.00, L),
        (0.70, MU_ALTO),
        (1.05, con(MU_ALTO, raiz=raiz(0.4, 1.0), torso=(-14, 0, 0))),
        (1.20, MU_BAJO),
        (1.60, con(MU_BAJO, torso=(54, 0, 0))),
        (2.00, L),
    ], golpes=(1.2,))
    # ---- 5 la campanilla (su rugido): alza la izquierda y la toca tres veces; los muertos se levantan
    CA = con(BASE, raiz=raiz(-0.2, 0.4), torso=(-6, -10, 0), cabeza=(-24, 10, 0), mandibula=(12, 0, 0), _mano_d=("h", 1.2, -5.0, 5.5),
             _arma=(0.05, -1.0, 0.12), _mano_i=("h", 3.6, 8.4, 2.6), _arma_i=(0.2, 0.6, 0.78))
    toques = [(0.00, L), (0.40, CA)]
    for k in range(3):
        toques.append((0.55 + 0.4 * k, con(CA, _arma_i=(0.62, 0.35, 0.7))))
        toques.append((0.75 + 0.4 * k, con(CA, _arma_i=(-0.62, 0.35, 0.7))))
    toques += [(1.95, CA), (2.40, L)]
    accion("llamar", 2.4, toques)
    # ---- 6 las guirnaldas: alza los dos brazos a la cupula, mirando arriba, y los baja de golpe ("inclinad la cabeza")
    GU_ALTO = con(BASE, raiz=raiz(0.4, 0.6), torso=(-18, 0, 0), cabeza=(-36, 0, 0), mandibula=(16, 0, 0),
                  _mano_d=("h", -2.0, 9.6, 2.0), _arma=(-0.2, 0.9, 0.3), _mano_i=("h", 2.0, 9.6, 2.0), _arma_i=(0.2, 0.9, 0.3))
    GU_BAJO = con(L, raiz=raiz(-2.4, -0.6), torso=(40, 0, 0), cabeza=(-26, 0, 0), _mano_d=("h", 0.6, -4.0, 9.4), _arma=(0.0, -0.7, 0.7),
                  _mano_i=("h", -0.6, -4.0, 9.4), _arma_i=(0.0, -0.8, 0.6))
    accion("guirnaldas", 1.8, [
        (0.00, L),
        (0.55, GU_ALTO),
        (0.90, con(GU_ALTO, torso=(-22, 0, 0), cabeza=(-40, 0, 0))),
        (1.00, GU_BAJO),
        (1.40, con(GU_BAJO, torso=(38, 0, 0))),
        (1.80, L),
    ], golpes=(1.0,))
    # ---- 7 aturdido
    AT = dict(raiz=raiz(-1.2, 2.0), torso=(-12, 0, 8), cabeza=(-24, 0, 14), mandibula=(16, 0, 0), muslo_d=(-8, 0, 8), pierna_d=(22, 0, 0),
              muslo_i=(12, 0, -8), pierna_i=(16, 0, 0), _mano_d=("h", -3.0, -6.0, 3.0), _arma=(-0.4, -0.9, 0.2),
              _mano_i=("h", 4.0, -3.0, 3.0), _arma_i=(0.6, -0.6, 0.4))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-6, 6, -10), cabeza=(-18, -8, -16), raiz=raiz(-1.8, 2.6))),
        (1.05, con(AT, torso=(-10, -6, 8), cabeza=(-22, 8, 12), raiz=raiz(-1.6, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 8 morir: suelta el incensario (queda en el suelo delante), cae de rodillas y se dobla sobre si mismo
    CAE = con(RODILLAS, torso=(46, 0, 0), cabeza=(40, 0, 0), mandibula=(20, 0, 0), _mano_d=("h", 0.0, -11.0, 7.0), _arma=(0.1, -0.1, 1.0),
              _mano_i=("h", 0.0, -11.0, 5.0), _arma_i=(0.0, -0.5, 0.86))
    accion("morir", 4.0, [
        (0.00, con(BASE, PORTE, torso=(-10, 0, 6), cabeza=(-30, 0, 10), mandibula=(18, 0, 0))),
        (0.60, con(BASE, torso=(18, 0, 6), cabeza=(10, 0, 10), _mano_d=("h", 0.6, -7.0, 6.0), _arma=(0.1, -0.6, 0.8),
                   _mano_i=("h", 0.0, -9.0, 5.0), _arma_i=(0.0, -0.9, 0.4))),
        (1.30, CAE),
        (2.20, con(CAE, torso=(54, 0, 0), cabeza=(46, 0, 0))),
        (4.00, con(CAE, torso=(60, 0, 0), cabeza=(52, 0, 0))),
    ])
    # ---- 9 levantarse: deja de contar, se pone en pie y se vuelve hacia quien llega (el giro lo pone el servidor)
    accion("levantarse", 1.4, [
        (0.00, c_coge),
        (0.40, con(CUENTA, raiz=raiz(-5.0, 0.4), torso=(20, -10, 0), cabeza=(-10, -24, 0), _mano_i=("h", 0.0, -8.0, 5.0),
                   _arma_i=(0.0, -1.0, 0.2))),
        (0.90, con(L, raiz=raiz(-2.4), torso=(26, 4, 0), cabeza=(-20, 8, 6))),
        (1.40, L),
    ])
    # ---- 10 alzarse (a media vida): abre los brazos, echa la cabeza atras y los muertos le levantan; acaba flotando
    AL = con(FLOTA, torso=(-16, 0, 0), cabeza=(-32, 0, 0), mandibula=(18, 0, 0), _mano_d=("h", -7.0, 4.0, 2.0), _arma=(-0.5, -0.9, 0.1),
             _mano_i=("h", 7.0, 4.0, 2.0), _arma_i=(0.5, 0.6, 0.6))
    accion("alzarse", 2.6, [
        (0.00, L),
        (0.50, con(BASE, raiz=raiz(-2.0), torso=(30, 0, 0), cabeza=(10, 0, 0), _mano_d=("h", -3.0, -6.0, 5.0), _arma=(-0.2, -1.0, 0.2),
                   _mano_i=("h", 3.0, -6.0, 5.0), _arma_i=(0.2, -1.0, 0.2))),
        (1.30, AL),
        (2.00, con(AL, torso=(-20, 0, 0), cabeza=(-36, 0, 0))),
        (2.60, FLOTA),
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
