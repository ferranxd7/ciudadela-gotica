"""
El Condestable del Juramento (parte 7n): el jefe de la Capital en ruinas. Ser Halvard, el que mando cerrar las puertas la
noche del cielo rojo y se quedo de rodillas en medio del Patio del Juramento con el montante clavado delante. Ardio dentro
de su armadura y no acabo de apagarse: un caballero gigante (x2) con la placa ennegrecida y cuarteada (por las rajas, el
rojo de las brasas: capa de brillo), el gran yelmo cerrado con su cimera de crines quemadas (las puntas aun en ascuas) y la
rendija por la que se le ven los ojos; gola, hombreras de tres laminas, guanteletes, faldar, quijotes, grebas y escarpes; la
sobreveste roja del Juramento quemada por abajo con la mano alzada en oro, y la capa hecha jirones. A dos manos, el montante:
la hoja negra con los filos al rojo (lucen), la cruz y el pomo de oro gastado.

Una sola variante. Acciones: tajo (lo alza por encima del yelmo y lo descarga: el fuego corre por el suelo), barrido (de su
derecha a su izquierda, ancho), embestida (agacha el hombro y carga por el patio), llamarada (clava el montante: un anillo de
fuego revienta alrededor), alzar (el montante al cielo: la brea cae de los muros), grito (al empezar y a media vida, cuando
llama a la guardia con el cuerno), juramento (al caer la primera vez, hinca la rodilla y se alza otra vez con el montante en
alto: "juré no morir"), aturdido (se ha estrellado contra un muro), morir (de rodilla, apoyado en el montante, vencido) y
levantarse (esperaba de rodilla, el montante clavado delante y las manos en el pomo).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, dibujo, liso, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "condestable"
VARIANTES = ("condestable",)
ESCALA = 2.0
CENTRO_Y = 2.4

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # tan grande como el Abad, pero derecho y ancho de hombros (el brazo mide 16: la mano llega a unos 13 de su hombro)
    humanoide(r, torso=(11, 14, 6), cabeza=(8, 9, 8), brazo=(4, 16, 4), pierna=(5, 14, 5), hombro=0.6)
    # ---- el gran yelmo cerrado, su cimera de crines (de la frente a la nuca) y la gola
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4.6, -9.6, -4.6, 9.2, 10.0, 9.2, "yelmo"),
        Box(-0.7, -10.6, -4.8, 1.4, 1.2, 9.6, "cresta"),
    ])
    r.pieza("cimera", "yelmo", (0, -10.6, 0), boxes=[
        Box(-0.9, -3.4, -3.8, 1.8, 3.4, 7.2, "cimera"),
        Box(-0.9, -1.8, 3.4, 1.8, 7.0, 1.6, "cimera_cola"),
    ])
    r.pieza("gola", "torso", (0, -14, 0), boxes=[Box(-4.6, -1.6, -3.6, 9.2, 3.0, 7.2, "gola")])
    # ---- la coraza (peto y espaldar, sobre el torso), el faldar de lamas sobre la cadera
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-5.9, -13.4, -3.6, 11.8, 10.2, 7.2, "coraza")])
    r.pieza("faldar", "cadera", (0, 0, 0), boxes=[Box(-6.0, -3.4, -3.5, 12.0, 6.6, 7.0, "faldar")])
    # ---- la sobreveste del Juramento (delante y detras, del pecho a las rodillas) y la capa hecha jirones
    r.pieza("sobreveste", "torso", (0, -12.4, 0), boxes=[
        Box(-4.2, 0, -3.95, 8.4, 21.0, 0.4, "sobreveste"),
        Box(-4.2, 0, 3.55, 8.4, 21.0, 0.4, "sobreveste_espalda"),
    ])
    r.pieza("capa", "torso", (0, -13.8, 3.9), rot=(6, 0, 0), boxes=[Box(-6.6, 0, 0, 13.2, 27.0, 0.9, "capa")])
    # ---- las hombreras de tres laminas, los guanteletes, los quijotes, las grebas y los escarpes
    for n in ("d", "i"):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[
            Box(-3.2, -3.8, -3.2, 6.4, 4.0, 6.4, "hombrera"),
            Box(-3.4, -0.4, -3.4, 6.8, 1.8, 6.8, "lamina"),
            Box(-3.5, 1.2, -3.5, 7.0, 1.6, 7.0, "lamina"),
        ])
        r.pieza(f"guantelete_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-2.6, 3.0, -2.6, 5.2, 5.4, 5.2, "guantelete")])
        r.pieza(f"quijote_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.9, 0.6, -2.9, 5.8, 6.4, 5.8, "quijote")])
        r.pieza(f"greba_{n}", f"pierna_{n}", (0, 0, 0), boxes=[
            Box(-2.9, -0.4, -2.9, 5.8, 5.6, 5.8, "greba"),
            Box(-2.8, 5.0, -4.4, 5.6, 2.2, 7.2, "escarpe"),
        ])
    # ---- el montante (a dos manos: la derecha junto a la cruz, la izquierda mas atras en el puno; apunta a -Z)
    r.pieza("montante", "mano_d", (0, 0, 0), boxes=[
        Box(-0.8, -0.8, -2.2, 1.6, 1.6, 7.6, "puno"),
        Box(-1.4, -1.4, 5.4, 2.8, 2.8, 2.2, "pomo"),
        Box(-6.4, -1.0, -3.8, 12.8, 2.0, 1.6, "cruz"),
        Box(-0.9, -1.6, -6.4, 1.8, 3.2, 2.6, "recazo"),
        Box(-0.7, -2.2, -24.4, 1.4, 4.4, 18.0, "hoja"),
        Box(-0.7, -1.3, -27.0, 1.4, 2.6, 2.6, "punta"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {0: []}

#: lo que va ademas en CondestableAnims (la escala, para el dibujante y el servidor)
TABLA_EXTRA = (
    "",
    f"    public static final float ESCALA = {ESCALA}f;",
)

# ---------------------------------------------------------------- las texturas

CARBON = ["#0a0807", "#141110", "#1e1916", "#29221d", "#352c25", "#42382f"]
BRASA = ["#3a0d06", "#6a1a08", "#9c2c0c", "#d24a14", "#f57a24", "#ffb046", "#ffe08a"]
CENIZA = ["#4d4844", "#67615b", "#837c74", "#a29a91", "#c2bab0", "#ddd6cc"]
ACERO = ["#0c0c0e", "#161519", "#201f23", "#2b292e", "#38353a", "#47424a", "#5c5560"]
ROJO = ["#1e0808", "#2f0c0c", "#451210", "#5c1915", "#74221c", "#8c2c22"]
ORO = ["#4a3a16", "#6e5622", "#95772f", "#c09c42", "#e2c464"]
CUERO = ["#160f0a", "#22170f", "#2e2015", "#3b2a1c", "#4a3624"]

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


def _grietas(semilla, n=6, largo=(4, 9)):
    """Las rajas de la armadura: lineas finas que se tuercen y se ramifican (por donde se ven las brasas)."""
    import random
    r = random.Random(semilla)
    puntos = set()
    for _ in range(n):
        x, y = r.randrange(0, 16), r.randrange(0, 16)
        dx = r.choice((-1, 1))
        for _ in range(r.randrange(*largo)):
            puntos.add((x % 16, y % 16))
            if r.random() < 0.55:
                x += dx
            else:
                y += 1
            if r.random() < 0.15:
                dx = -dx
    return puntos


GRIETAS = _grietas(7301, n=5, largo=(4, 8))


def _grieta(cara, x, y, sal=0):
    k = CARAS[cara] + sal
    return ((x + 5 * k) % 16, (y + 3 * k) % 16) in GRIETAS


def materiales(variante):
    acero = metal(ACERO[1:6], 7311)
    acero_o = metal(ACERO[0:5], 7312)
    rojo = tela(ROJO, 7313, pliegue=3, grano=0.18, luz=0.18)
    capa_t = tela(ROJO[0:5], 7314, pliegue=5, grano=0.2, luz=0.15)
    carbon = ruido(CARBON[1:5], 7315, grano=0.55, base=0.5)
    cuero = ruido(CUERO[1:5], 7316, grano=0.5)

    def quemado(c, cara, x, y, w, h, cuanto=0.25, sal=0):
        """Hollin a manchas (mas cuanto mas abajo) y alguna mota de ceniza clara."""
        kc = CARAS[cara]
        s0 = w * 29 + h * 13 + sal * 97
        g_ = 0.6 * _azar01(s0, kc, x // 2, y // 2) + 0.4 * _azar01(s0 + 5, kc, x, y)
        p = cuanto * (0.7 + 0.6 * y / max(1, h - 1))
        if g_ < p * 0.45:
            return CARBON[1]
        if g_ < p:
            return mezcla(c, CARBON[1], 0.5)
        if g_ > 0.985:
            return CENIZA[3]
        return c

    def armadura(cara, x, y, w, h, sal, oscura=False):
        c = quemado((acero_o if oscura else acero)(cara, x, y, w, h), cara, x, y, w, h, 0.24, sal)
        if _grieta(cara, x, y, sal):
            return BRASA[1 + (x + y) % 2]               # una raja en la placa, al rojo (luce)
        if cara != "abajo" and (y == 0 or (cara in ("frente", "espalda") and x in (0, w - 1))):
            return claro(c, 0.14)                      # el canto
        return c

    def piel(cara, x, y, w, h):
        # lo que se ve de el entre las placas: carbon cuarteado con brasas
        c = carbon(cara, x, y, w, h)
        if _grieta(cara, x, y, 7):
            return BRASA[1 + (x + y) % 2]
        return c

    def cabeza(cara, x, y, w, h):
        return piel(cara, x, y, w, h)

    def yelmo(cara, x, y, w, h):
        if cara == "frente":
            # la rendija de la vista (por ella, los ojos), los respiraderos y la nervadura del frente
            if y == 4 and 1 <= x <= w - 2:
                return "#050303"
            if y == 5 and x in (2, w - 3):
                return "#050303"
            if y in (6, 7) and x % 2 == 0 and 1 <= x <= w - 2 and x != w // 2:
                return CARBON[0]
            if x == w // 2 and y >= 5:
                return claro(acero(cara, x, y, w, h), 0.18)
        if cara == "arriba":
            return armadura(cara, x, y, w, h, 2, True)
        c = armadura(cara, x, y, w, h, 2)
        # la banda de oro gastado en la frente
        if y == 2 and cara != "abajo":
            return ORO[1] if (x + y) % 3 else ORO[2]
        return c

    def cresta(cara, x, y, w, h):
        return armadura(cara, x, y, w, h, 3)

    def cimera(cara, x, y, w, h):
        # crines rojas quemadas: las puntas, aun en ascuas
        if cara == "abajo":
            return None
        if y <= 1 and (x + CARAS[cara]) % 3:
            return BRASA[2 + (x % 2)]
        return quemado(ROJO[2] if (x + y) % 2 else ROJO[1], cara, x, y, w, h, 0.45, 4)

    def gola(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 5)
        return oscuro(c, 0.2) if y % 2 else c

    def coraza(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 6)
        if cara == "frente":
            if x == w // 2 or x == w // 2 - 1:
                return claro(c, 0.1)                   # la arista del peto
            if y in (3, 7):
                return oscuro(c, 0.22)                 # las lamas
        return c

    def faldar(cara, x, y, w, h):
        if cara == "abajo":
            return ACERO[0]
        c = armadura(cara, x, y, w, h, 7)
        if y % 2 == 1:
            return oscuro(c, 0.24)
        if y == h - 1 and (x + CARAS[cara]) % 3 == 0:
            return BRASA[2]
        return c

    def sobreveste(cara, x, y, w, h, espalda=False):
        if cara not in ("frente", "espalda"):
            return ROJO[1]
        # el bajo roto y quemado
        corte = h - 1 - (0, 3, 1, 4, 2, 0, 3, 1)[x % 8]
        if y > corte:
            return None
        c = quemado(rojo(cara, x, y, w, h), cara, x, y, w, h, 0.22 + 0.025 * y, 8)
        if y >= corte - 1:
            return BRASA[2] if (x + y) % 2 else CARBON[1]   # el borde aun en ascuas
        if x in (0, w - 1):
            return ORO[1]                              # la orla
        if not espalda:
            # la mano alzada del Juramento, en oro gastado, en el pecho: tres dedos, la palma, el pulgar y la muneca
            M = ["#.#.#.",
                 "#.#.#.",
                 "#####.",
                 "######",
                 ".####.",
                 "..##..",
                 "..##.."]
            x0, y0 = (w - 6) // 2, 2
            if 0 <= x - x0 < 6 and 0 <= y - y0 < len(M) and M[y - y0][x - x0] == "#":
                fila = y - y0
                return ORO[3] if fila < 2 else ORO[2] if fila < 5 else ORO[1]
        return c

    def capa(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return ROJO[0]
        # jirones: el bajo deshilachado y agujeros quemados (con el borde en ascuas)
        corte = h - 1 - (0, 5, 2, 7, 1, 4, 8, 2, 0, 6, 3)[x % 11]
        if y > corte:
            return None
        if y > 6 and _azar01(911, x // 2, y // 2) < 0.06:
            return None
        if y >= corte - 1 and (x + y) % 2:
            return BRASA[2]
        c = quemado(capa_t(cara, x, y, w, h), cara, x, y, w, h, 0.3 + 0.015 * y, 9)
        return oscuro(c, 0.15) if cara == "frente" else c

    def hombrera(cara, x, y, w, h):
        return armadura(cara, x, y, w, h, 10)

    def lamina(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 11)
        if cara != "abajo" and y == h - 1:
            return ORO[1] if x % 2 else ORO[2]         # el filo de oro de cada lamina
        return c

    def guantelete(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 12, True)
        if y >= h - 2:
            return oscuro(c, 0.25)                     # los dedos
        return c

    def brazo(cara, x, y, w, h):
        # la cota de malla bajo las hombreras (oscura, a puntos)
        return ACERO[2] if (x + y) % 2 else ACERO[1]

    def antebrazo(cara, x, y, w, h):
        return armadura(cara, x, y, w, h, 13, True)

    def muslo(cara, x, y, w, h):
        return quemado(cuero(cara, x, y, w, h), cara, x, y, w, h, 0.3, 14)

    def pierna(cara, x, y, w, h):
        return armadura(cara, x, y, w, h, 15, True)

    def quijote(cara, x, y, w, h):
        return armadura(cara, x, y, w, h, 16)

    def greba(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 17)
        if cara == "frente" and x == w // 2:
            return claro(c, 0.12)
        return c

    def escarpe(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 18, True)
        return oscuro(c, 0.15) if y % 2 else c

    def puno(cara, x, y, w, h):
        return CUERO[1] if (x + y) % 2 else CUERO[2]

    def pomo(cara, x, y, w, h):
        return metal(ORO[0:4], 7321)(cara, x, y, w, h)

    def cruz(cara, x, y, w, h):
        c = metal(ORO[0:4], 7322)(cara, x, y, w, h)
        return quemado(c, cara, x, y, w, h, 0.3, 19)

    def hoja(cara, x, y, w, h):
        # la hoja negra; los filos (las caras de arriba y de abajo), al rojo: en la capa de brillo
        if cara in ("arriba", "abajo"):
            return BRASA[3] if (y // 3) % 3 else BRASA[2]
        if x in (0, w - 1):
            return BRASA[1]
        c = ACERO[2] if (x + y) % 3 else ACERO[3]
        if cara in ("der", "izq") and x == w // 2:
            return ACERO[1]                            # la acanaladura
        return c

    mats = {
        "cabeza": sombra(cabeza, abajo=0.15),
        "torso": sombra(lambda cara, x, y, w, h: armadura(cara, x, y, w, h, 20, True)),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12), "antebrazo_i": sombra(antebrazo, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.15), "pierna_i": sombra(pierna, abajo=0.15),
        "yelmo": sombra(yelmo, abajo=0.15),
        "cresta": cresta,
        "cimera": cimera,
        "cimera_cola": cimera,
        "gola": sombra(gola, abajo=0.1),
        "coraza": sombra(coraza, abajo=0.1),
        "faldar": sombra(faldar, abajo=0.1),
        "sobreveste": sobreveste,
        "sobreveste_espalda": lambda cara, x, y, w, h: sobreveste(cara, x, y, w, h, True),
        "capa": capa,
        "hombrera": sombra(hombrera, abajo=0.2),
        "lamina": lamina,
        "guantelete": sombra(guantelete, abajo=0.15),
        "quijote": sombra(quijote, abajo=0.15),
        "greba": sombra(greba, abajo=0.12),
        "escarpe": sombra(escarpe, abajo=0.1),
        "puno": puno,
        "pomo": pomo,
        "cruz": cruz,
        "recazo": metal(ACERO[1:5], 7323),
        "hoja": hoja,
        "punta": hoja,
    }
    return mats


def brillo(variante):
    """Lo que luce: los ojos por la rendija del yelmo, las rajas al rojo de la armadura, las puntas de la cimera, los bajos en
    ascuas de la sobreveste y de la capa, y los filos del montante."""

    def rajas(sal):
        def f(cara, x, y, w, h):
            if _grieta(cara, x, y, sal):
                return BRASA[3 + (x + y) % 3]
            return None
        return f

    def yelmo(cara, x, y, w, h):
        if cara == "frente":
            if y == 4 and x in (2, 3, w - 4, w - 3):
                return BRASA[5] if x in (2, w - 3) else BRASA[6]
            return None
        return rajas(2)(cara, x, y, w, h)

    def cimera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if y <= 1 and (x + CARAS[cara]) % 3:
            return BRASA[4 + (x % 2)]
        return None

    def sobreveste(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return None
        corte = h - 1 - (0, 3, 1, 4, 2, 0, 3, 1)[x % 8]
        if corte - 1 <= y <= corte and (x + y) % 2:
            return BRASA[4]
        return None

    def capa(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return None
        corte = h - 1 - (0, 5, 2, 7, 1, 4, 8, 2, 0, 6, 3)[x % 11]
        if corte - 1 <= y <= corte and (x + y) % 2:
            return BRASA[3]
        return None

    def hoja(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return BRASA[4] if (y // 3) % 3 else BRASA[5]
        if x in (0, w - 1):
            return BRASA[3]
        return BRASA[2] if (x + y) % 5 == 0 else None

    return {
        "yelmo": yelmo,
        "cimera": cimera, "cimera_cola": cimera,
        "coraza": rajas(6), "hombrera": rajas(10), "greba": rajas(17), "quijote": rajas(16), "faldar": rajas(7),
        "torso": rajas(20), "antebrazo_d": rajas(13), "antebrazo_i": rajas(13), "cabeza": rajas(7),
        "sobreveste": sobreveste, "sobreveste_espalda": sobreveste,
        "capa": capa,
        "hoja": hoja, "punta": hoja,
    }


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno derecho respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide
# 16: llega a unos 13); _arma = hacia donde apunta el montante; _agarra = la izquierda en el puno, a tantos pixeles detras
# de la derecha (lo lleva a dos manos). El porte lleva los brazos (el quieto no los toca): se suma al quieto.

# de pie: derecho, el pecho fuera, firme
BASE = dict(raiz=raiz(-0.2), torso=(3, 0, 0), cabeza=(4, 0, 0), muslo_d=(0, 0, 4), pierna_d=(2, 0, 0), muslo_i=(0, 0, -4),
            pierna_i=(2, 0, 0), capa=(-2, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# esperando de pie: el montante de punta en el suelo delante de el, las dos manos en el pomo
ESPERA = dict(_mano_d=("h", 6.2, -2.0, 8.6), _arma=(0.0, -1.0, 0.08), _agarra=3.4)
# en guardia: a dos manos, la punta alta hacia el otro
GUARDIA = dict(raiz=raiz(-1.6), torso=(10, 22, 0), cabeza=(-6, -20, 0), capa=(-6, 0, 0),
               muslo_d=(14, 0, 6), pierna_d=(18, 0, 0), muslo_i=(-24, 0, -6), pierna_i=(26, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", 6.2, -5.0, 8.2), _arma=(-0.1, 0.5, 1), _agarra=3.6)
# de rodilla (la derecha en el suelo, el pie izquierdo delante), el montante clavado delante y las dos manos en el pomo, la
# cabeza baja: asi espera en el estrado
RODILLA = dict(raiz=raiz(-8.0, 0.8), torso=(10, 0, 0), cabeza=(26, 0, 0), capa=(12, 0, 0),
               muslo_d=(-4, 0, 4), pierna_d=(96, 0, 0), muslo_i=(-84, 0, -6), pierna_i=(82, 0, 0))
VELA = con(RODILLA, _mano_d=("h", 7.0, 3.0, 8.4), _arma=(0.0, -1.0, 0.04), _agarra=2.8)


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

    # ---- quieto: respira despacio (las brasas laten: lo pone el cliente)
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (2.5, con(BASE, torso=(1, 0, 0), cabeza=(2, 4, 0), raiz=raiz(0.1), capa=(-3, 0, 0))),
        (5.0, BASE),
    ])
    # ---- andar: pasos largos y pesados; la capa se mece
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate(((-26, 20, 6, 18, 4, 0.0), (-4, 2, 4, 38, 0, 0.8), (20, -26, 18, 6, -4, 0.0),
                                                   (2, -4, 38, 4, 0, 0.8), (-26, 20, 6, 18, 4, 0.0))):
        pasos.append((1.8 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, -ty * 0.3), "cabeza": (2, -ty * 0.7, ty * 0.3), "raiz": raiz(up),
                                    "capa": (10 + 6 * up, ty * 0.5, 0)}))
    bucle("andar", 1.8, pasos)
    q = fija(con(BASE, ESPERA))
    bucle("porte_montante", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    l0 = fija(LISTO)
    bucle("listo_montante", 2.4, [(0.0, l0), (1.2, fija(con(LISTO, raiz=raiz(-2.0), torso=(14, 26, 0), cabeza=(-8, -22, 2)))), (2.4, l0)])
    # ---- de rodilla, esperando: casi quieto (la cabeza baja un poco mas y vuelve)
    vz = fija(VELA)
    bucle("rodilla", 6.0, [(0.0, vz), (3.0, con(vz, cabeza=(30, 0, 0), torso=(12, 0, 0))), (6.0, vz)])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 tajo: lo alza muy arriba con las dos manos (se ve venir: mas de un segundo) y lo descarga contra el suelo
    ALZA = con(L, raiz=raiz(-0.2, 1.6), torso=(-12, 8, 0), cabeza=(-14, -6, 0), capa=(16, 0, 0),
               muslo_d=(10, 0, 6), pierna_d=(10, 0, 0), muslo_i=(-16, 0, -6), pierna_i=(14, 0, 0),
               _mano_d=("h", 7.0, 10.2, -1.0), _arma=(0.0, 0.6, -0.8), _agarra=3.6)
    CLAVA = con(L, raiz=raiz(-4.6, -3.4), torso=(40, 4, 0), cabeza=(-24, -2, 0), capa=(-20, 0, 0),
                muslo_i=(-52, 0, -6), pierna_i=(52, 0, 0), muslo_d=(24, 0, 6), pierna_d=(14, 0, 0),
                _mano_d=("h", 6.4, -8.8, 8.6), _arma=(0.0, -0.5, 0.86), _agarra=3.6)
    accion("tajo", 2.5, [
        (0.00, L),
        (0.80, ALZA),
        (1.12, con(ALZA, raiz=raiz(0.0, 2.0), torso=(-16, 8, 0), _mano_d=("h", 7.0, 10.6, -2.0))),
        (1.17, con(L, raiz=raiz(-1.0, 0.4), torso=(-4, 6, 0), _mano_d=("h", 7.0, 9.6, 3.6), _arma=(0.0, 0.95, 0.3), _agarra=3.6)),
        (1.21, con(L, raiz=raiz(-2.4, -1.6), torso=(18, 5, 0), _mano_d=("h", 6.6, 2.6, 10.6), _arma=(0.0, 0.25, 1), _agarra=3.6)),
        (1.25, CLAVA),
        (1.85, con(CLAVA, torso=(38, 4, 0))),
        (2.50, L),
    ], golpes=(1.25,))
    # ---- 2 barrido: lo echa atras a su derecha y barre de derecha a izquierda, ancho y a media altura
    ATRAS = con(L, raiz=raiz(-1.8, 1.2), torso=(10, 62, 0), cabeza=(-6, -52, 0), capa=(8, 0, 0),
                _mano_d=("h", 4.0, -3.0, 6.0), _arma=(-0.85, 0.1, -0.5), _agarra=3.6)
    CRUZA = con(L, raiz=raiz(-2.6, -1.8), torso=(16, -36, 0), cabeza=(-10, 30, 0), capa=(-10, 0, 0),
                muslo_i=(-36, 0, -6), pierna_i=(32, 0, 0),
                _mano_d=("h", 11.0, -4.4, 1.0), _arma=(0.92, -0.1, -0.3), _agarra=3.4)
    accion("barrido", 2.0, [
        (0.00, L),
        (0.62, ATRAS),
        (0.82, con(ATRAS, torso=(10, 66, 0))),
        (0.94, con(L, raiz=raiz(-2.2, -0.8), torso=(14, 6, 0), _mano_d=("h", 7.0, -3.6, 7.8), _arma=(0.1, 0.0, 1), _agarra=3.2)),
        (1.04, CRUZA),
        (1.40, con(CRUZA, torso=(16, -40, 0))),
        (2.00, L),
    ], golpes=(0.94,))
    # ---- 3 embestida: agacha el hombro, el montante pegado al costado, y carga (el servidor lo lanza; pega en el choque)
    AGACHA = con(L, raiz=raiz(-3.0, 1.2), torso=(32, 40, 0), cabeza=(-26, -34, 0), capa=(4, 0, 0),
                 muslo_i=(-42, 0, -6), pierna_i=(50, 0, 0), _mano_d=("h", 5.0, -8.0, 4.0), _arma=(0.1, -0.4, 0.9), _agarra=3.6)
    CARGA_A = con(AGACHA, raiz=raiz(-2.2, -3.0), torso=(30, 44, 0), capa=(24, 0, 0), muslo_d=(-34, 0, 6), pierna_d=(34, 0, 0),
                  muslo_i=(22, 0, -6), pierna_i=(12, 0, 0))
    CARGA_B = con(AGACHA, raiz=raiz(-2.2, -3.0), torso=(30, 44, 0), capa=(28, 0, 0), muslo_d=(26, 0, 6), pierna_d=(12, 0, 0),
                  muslo_i=(-34, 0, -6), pierna_i=(34, 0, 0))
    accion("embestida", 2.4, [
        (0.00, L),
        (0.55, AGACHA),
        (0.70, CARGA_A),
        (0.90, CARGA_B),
        (1.10, CARGA_A),
        (1.30, CARGA_B),
        (1.50, CARGA_A),
        (1.85, con(L, raiz=raiz(-2.4, -2.0), torso=(20, 20, 0))),
        (2.40, L),
    ], golpes=(0.70,))
    # ---- 4 llamarada: alza el montante de punta y lo clava en el suelo delante: un anillo de fuego revienta alrededor
    LL_ALZA = con(BASE, raiz=raiz(0.2, 0.6), torso=(-8, 0, 0), cabeza=(-10, 0, 0), capa=(10, 0, 0),
                  _mano_d=("h", 6.8, 4.0, 7.0), _arma=(0.0, -1.0, 0.1), _agarra=3.0)
    LL_CLAVA = con(BASE, raiz=raiz(-5.2, 0.4), torso=(30, 0, 0), cabeza=(-6, 0, 0), capa=(-12, 0, 0),
                   muslo_d=(-40, 0, 6), pierna_d=(70, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(60, 0, 0),
                   _mano_d=("h", 6.6, -6.4, 9.0), _arma=(0.0, -1.0, 0.12), _agarra=3.0)
    accion("llamarada", 2.2, [
        (0.00, L),
        (0.55, LL_ALZA),
        (0.82, con(LL_ALZA, raiz=raiz(0.4, 0.6), _mano_d=("h", 6.8, 5.2, 6.6))),
        (0.92, LL_CLAVA),
        (1.50, con(LL_CLAVA, torso=(32, 0, 0), cabeza=(-10, 0, 0))),
        (2.20, L),
    ], golpes=(0.92,))
    # ---- 5 alzar: el montante al cielo con la derecha, la izquierda abierta: la brea cae de los muros
    AZ = con(BASE, raiz=raiz(0.4), torso=(-10, 0, 0), cabeza=(-34, 0, 0), capa=(14, 0, 0),
             _mano_d=("h", 1.6, 12.6, 1.6), _arma=(0.0, 1.0, 0.12),
             brazo_i=(-150, 0, -26), antebrazo_i=(-10, 0, 0))
    accion("alzar", 2.6, [
        (0.00, L),
        (0.45, con(BASE, torso=(16, 0, 0), _mano_d=("h", 3.0, -6.0, 8.0), _arma=(0.0, 0.6, 0.8), brazo_i=(-30, 0, -14),
                   antebrazo_i=(-40, 0, 0))),
        (0.95, AZ),
        (1.15, con(AZ, raiz=raiz(0.6), torso=(-14, 0, 0))),
        (1.70, con(AZ, torso=(-12, 0, 3))),
        (2.05, AZ),
        (2.60, L),
    ], golpes=(1.10,))
    # ---- 6 grito: abre los brazos (el montante en alto a su derecha), la cabeza atras
    GR = con(BASE, raiz=raiz(-0.6, 1.0), torso=(-16, 0, 0), cabeza=(-36, 0, 0), capa=(20, 0, 0),
             _mano_d=("h", -7.0, 9.0, 3.6), _arma=(-0.35, 1.0, 0.1),
             brazo_i=(-26, 0, -82), antebrazo_i=(-14, 0, 0))
    accion("grito", 2.6, [
        (0.00, L),
        (0.45, con(GR, torso=(24, 0, 0), cabeza=(10, 0, 0), raiz=raiz(-2.6))),
        (0.90, GR),
        (1.40, con(GR, torso=(-20, 0, 4), cabeza=(-40, 0, 6))),
        (1.95, con(GR, torso=(-14, 0, -4), cabeza=(-34, 0, -6))),
        (2.60, L),
    ])
    # ---- 7 juramento: le han vencido... hinca la rodilla, el montante clavado; tiembla; y se alza otra vez con el montante en
    # alto ("juré no morir"): el fuego revienta a su alrededor
    J_CAE = con(VELA, raiz=raiz(-7.6, 1.0), torso=(28, 0, 6), cabeza=(40, 0, 8), capa=(-8, 0, 0))
    J_SUBE = con(AZ, raiz=raiz(-0.6, 0.8), torso=(-14, 0, 0), cabeza=(-40, 0, 0))
    accion("juramento", 4.0, [
        (0.00, con(L, torso=(-6, 10, 8), cabeza=(-20, 0, 10))),
        (0.60, J_CAE),
        (1.40, con(J_CAE, torso=(30, 2, -6), cabeza=(42, 0, -8))),
        (2.10, con(J_CAE, torso=(26, -2, 6), cabeza=(36, 0, 6))),
        (2.60, con(VELA, cabeza=(-24, 0, 0), torso=(4, 0, 0))),
        (3.10, con(RODILLA, raiz=raiz(-4.0, 0.8), torso=(-6, 0, 0), cabeza=(-30, 0, 0), muslo_d=(-40, 0, 4), pierna_d=(70, 0, 0),
                   muslo_i=(-50, 0, -6), pierna_i=(40, 0, 0),
                   _mano_d=("h", 2.0, 10.0, 4.0), _arma=(0.0, 1.0, 0.2), brazo_i=(-120, 0, -30), antebrazo_i=(-10, 0, 0))),
        (3.25, J_SUBE),
        (3.60, con(J_SUBE, raiz=raiz(-0.2, 0.6))),
        (4.00, L),
    ], golpes=(3.25,))
    # ---- 8 aturdido: se ha estrellado (contra un muro): tambalea con el montante colgando
    AT = con(L, raiz=raiz(-2.0, 2.4), torso=(-12, 0, 8), cabeza=(-24, 0, 14), capa=(14, 0, 0), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0),
             muslo_i=(12, 0, -8), pierna_i=(18, 0, 0), _mano_d=("h", -2.0, -11.0, 3.0), _arma=(-0.2, -0.4, 1), brazo_i=(16, 0, -30),
             antebrazo_i=(-16, 0, 0))
    AT.pop("_agarra", None)
    accion("aturdido", 1.6, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.22, AT),
        (0.70, con(AT, torso=(-6, 6, -10), cabeza=(-18, -8, -16), raiz=raiz(-2.2, 3.0))),
        (1.12, con(AT, torso=(-10, -6, 8), cabeza=(-22, 8, 12), raiz=raiz(-2.0, 2.6))),
        (1.60, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir: suelta el montante a medias, hinca la rodilla, lo clava delante y se queda apoyado en el, la cabeza baja
    VENCIDO = con(VELA, raiz=raiz(-8.2, 0.6), torso=(30, 0, 4), cabeza=(44, 0, 8), capa=(-8, 0, 0),
                  _mano_d=("h", 7.0, 1.0, 9.0), _arma=(0.0, -1.0, 0.06), _agarra=2.6)
    accion("morir", 4.0, [
        (0.00, con(L, torso=(-8, 0, 6), cabeza=(-30, 0, 10), capa=(12, 0, 0))),
        (0.80, con(VELA, raiz=raiz(-6.6, 0.9), torso=(4, 0, 0), cabeza=(-14, 0, 0))),
        (1.80, con(VELA, torso=(12, 0, 0), cabeza=(-4, 0, 0))),
        (2.80, VENCIDO),
        (4.00, VENCIDO),
    ])
    # ---- 10 levantarse: estaba de rodilla con el montante clavado; se apoya en el, se pone en pie y lo arranca del suelo
    accion("levantarse", 2.2, [
        (0.00, VELA),
        (0.70, con(VELA, torso=(4, 0, 0), cabeza=(-8, 0, 0), muslo_d=(-60, 0, 4), pierna_d=(70, 0, 0), raiz=raiz(-5.6, 0.6))),
        (1.40, con(BASE, ESPERA, raiz=raiz(-1.0), torso=(10, 0, 0), cabeza=(-6, 0, 0))),
        (2.20, L),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_montante"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_montante"]]
    if nombre == "quieto":
        return [anims["porte_montante"]]
    return []
