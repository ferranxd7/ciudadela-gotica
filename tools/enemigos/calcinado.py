"""
Los Calcinados de la Capital en ruinas (parte 7m): la gente de la capital, que ardio la noche que el cielo se puso rojo y
no acabo de apagarse. La piel negra de carbon, cuarteada, y por las grietas el rojo de las brasas que siguen dentro (capa
de brillo); los ojos, dos ascuas. Huelen a humo y van dejando chispas. Tres:

  0 guardia: un soldado del Juramento con su armadura ennegrecida (yelmo cerrado, hombreras, peto, faldar, grebas y
    guanteletes), la sobreveste roja quemada con la mano alzada del Juramento en oro, un escudo de torre en la izquierda y
    en la derecha una espada con el filo al rojo (luce). Tajo (de arriba abajo: quema) y escudazo (embiste con el escudo:
    empuja y rompe la guardia); de frente para los golpes con el escudo.
  1 incendiario: un vecino con delantal de cuero, un trapo atado a la cara y un cinturon de cantaros de pez; en la derecha
    un cantaro con la mecha encendida (luce). Lo lanza en arco (donde revienta, el suelo arde un rato) y de cerca golpea con
    el (quema).
  2 ceniciento: un vecino consumido, en harapos, cubierto de ceniza; espera enterrado en la ceniza de los derrumbes
    (TUMBADO: se alza de golpe). Zarpazo y agarron (se te echa encima y te quema); al morir revienta en una nube de ceniza.

Los guardias y los incendiarios esperan agazapados junto a los rescoldos (postura REZA: se levantan al acercarte) o de pie.
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "calcinado"
VARIANTES = ("guardia", "incendiario", "ceniciento")
ESCALA = 1.05

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), cabeza=(7, 8, 7), brazo=(3, 14, 3), pierna=(4, 12, 4), hombro=0.6)
    # ---- 0, la guardia: la armadura ennegrecida, la sobreveste, el escudo de torre y la espada
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4.2, -8.6, -4.2, 8.4, 8.8, 8.4, "yelmo"),
        Box(-0.6, -10.2, -3.0, 1.2, 1.8, 6.6, "cimera"),
    ])
    for n in ("d", "i"):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-2.3, -2.8, -2.3, 4.6, 4.0, 4.6, "hombrera")])
        r.pieza(f"guantelete_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-1.8, 2.6, -1.8, 3.6, 4.4, 3.6, "guantelete")])
        r.pieza(f"greba_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-2.3, -0.2, -2.3, 4.6, 6.4, 4.6, "greba")])
    r.pieza("peto", "torso", (0, 0, 0), boxes=[Box(-4.4, -12.3, -2.9, 8.8, 9.2, 5.8, "peto")])
    r.pieza("faldar", "cadera", (0, 0, 0), boxes=[Box(-4.5, -3.2, -2.95, 9.0, 6.4, 5.9, "faldar")])
    r.pieza("sobreveste", "torso", (0, -11.6, 0), boxes=[
        Box(-3.0, 0, -3.15, 6.0, 15.5, 0.3, "sobreveste"),
        Box(-3.0, 0, 2.85, 6.0, 15.5, 0.3, "sobreveste_espalda"),
    ])
    # el escudo de torre, embrazado en la mano izquierda: con el antebrazo de punta adelante (como lo lleva siempre) su
    # plano (x, z de la mano) queda de pie delante del cuerpo; la cara de fuera es la de abajo de la caja
    r.pieza("escudo", "mano_i", (-1.2, 0.4, 0), boxes=[
        Box(-5.0, 0.0, -8.5, 10.0, 1.2, 17.0, "escudo"),
        Box(-1.0, -1.2, -1.2, 2.0, 1.2, 2.4, "escudo_asa"),
    ])
    r.pieza("espada", "mano_d", (0, 0, 0), boxes=[
        Box(-0.55, -0.9, -17.0, 1.1, 1.8, 14.8, "hoja"),
        Box(-2.8, -0.7, -2.6, 5.6, 1.4, 1.0, "guarda"),
        Box(-0.7, -0.7, -1.6, 1.4, 1.4, 3.4, "puno_espada"),
        Box(-1.0, -1.0, 1.8, 2.0, 2.0, 1.2, "pomo"),
    ])
    # ---- 1, el incendiario: el trapo de la cara, el delantal, el cinturon de cantaros y el cantaro con su mecha
    r.pieza("trapo", "cabeza", (0, 0, 0), boxes=[
        Box(-3.8, -4.4, -3.8, 7.6, 3.2, 7.6, "trapo"),
        Box(-3.8, -8.4, -3.8, 7.6, 1.6, 7.6, "pano_cabeza"),
    ])
    r.pieza("delantal", "torso", (0, -9.5, 0), boxes=[Box(-3.4, 0, -2.85, 6.8, 15.0, 0.4, "delantal")])
    r.pieza("cinturon", "torso", (0, -1.6, 0), boxes=[
        Box(-4.3, -0.4, -2.8, 8.6, 1.4, 5.6, "cinturon"),
        Box(-5.6, 0.8, -1.4, 2.2, 2.8, 2.6, "cantarillo"),
        Box(3.4, 0.8, -1.4, 2.2, 2.8, 2.6, "cantarillo"),
        Box(-1.3, 0.8, 2.6, 2.6, 2.8, 2.2, "cantarillo"),
    ])
    # el cantaro, de pie en la palma (lo lleva con el antebrazo de punta adelante: lo de arriba es -z de la mano), con la
    # boca arriba, la mecha y su llama
    r.pieza("cantaro", "mano_d", (0, 1.2, 0), boxes=[
        Box(-2.4, -2.4, -6.4, 4.8, 4.8, 4.8, "cantaro"),
        Box(-1.2, -1.2, -7.4, 2.4, 2.4, 1.0, "cantaro_boca"),
        Box(-0.4, -0.4, -9.2, 0.8, 0.8, 1.8, "mecha"),
    ])
    r.pieza("llama", "cantaro", (0, 0, -9.0), boxes=[Box(-1.4, -1.4, -3.6, 2.8, 2.8, 3.6, "llama")])
    # ---- 2, el ceniciento: los harapos y la ceniza que le cubre la cabeza y los hombros
    r.pieza("harapos", "torso", (0, 0, 0), boxes=[Box(-4.3, -12.2, -2.8, 8.6, 15.5, 5.6, "harapos")])
    r.pieza("ceniza_cabeza", "cabeza", (0, -8, 0), boxes=[Box(-3.8, -0.9, -3.8, 7.6, 1.6, 7.6, "ceniza_capa")])
    for n in ("d", "i"):
        r.pieza(f"ceniza_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-1.9, -2.6, -1.9, 3.8, 1.4, 3.8, "ceniza_capa")])
    r.empaquetar()
    return r


GUARDIA_P = ["yelmo", "hombrera_d", "hombrera_i", "guantelete_d", "guantelete_i", "greba_d", "greba_i", "peto", "faldar", "sobreveste",
             "escudo", "espada"]
INCENDIARIO_P = ["trapo", "delantal", "cinturon", "cantaro", "llama"]
CENICIENTO_P = ["harapos", "ceniza_cabeza", "ceniza_d", "ceniza_i"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: INCENDIARIO_P + CENICIENTO_P,
    1: GUARDIA_P + CENICIENTO_P,
    2: GUARDIA_P + INCENDIARIO_P,
}

# ---------------------------------------------------------------- las texturas

CARBON = ["#0a0807", "#141110", "#1e1916", "#29221d", "#352c25", "#42382f"]
BRASA = ["#3a0d06", "#6a1a08", "#9c2c0c", "#d24a14", "#f57a24", "#ffb046", "#ffe08a"]
CENIZA = ["#4d4844", "#67615b", "#837c74", "#a29a91", "#c2bab0", "#ddd6cc"]
ACERO = ["#0f0f11", "#1a191c", "#252428", "#322f33", "#413c40", "#524b4e", "#6a6164"]
ROJO = ["#1e0a0a", "#2e0f0f", "#421514", "#561c1a", "#6b2522"]
ORO = ["#4a3a16", "#6e5622", "#95772f", "#c09c42", "#e2c464"]
CUERO = ["#160f0a", "#22170f", "#2e2015", "#3b2a1c", "#4a3624"]
TRAPO = ["#2a2622", "#3a352f", "#4b453d", "#5d564c"]
BARRO = ["#3e2414", "#55321c", "#6c4126", "#835031", "#9a603b"]
MADERA = ["#1c140d", "#271c12", "#332518", "#40301f"]

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
    """Las grietas de la piel: lineas finas que se tuercen y se ramifican (por donde se ven las brasas)."""
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


GRIETAS = _grietas(7101)
GRIETAS_ARMADURA = _grietas(7103, n=4, largo=(3, 6))


def _grieta(cara, x, y, grietas=GRIETAS):
    k = CARAS[cara]
    return ((x + 5 * k) % 16, (y + 3 * k) % 16) in grietas


def materiales(variante):
    carbon = ruido(CARBON[1:5], 7111, grano=0.55, base=0.5)
    acero = metal(ACERO[1:6], 7112)
    rojo = tela(ROJO, 7113, pliegue=3, grano=0.2, luz=0.16)
    cuero = ruido(CUERO[1:5], 7114, grano=0.5)
    trapo_t = tela(TRAPO, 7115, pliegue=3, grano=0.25)
    barro = ruido(BARRO[0:4], 7116, grano=0.4)
    ceniza = ruido(CENIZA[1:6], 7117, grano=0.7, base=0.5)
    madera_t = madera(MADERA, 7118, "y")

    def quemado(c, cara, x, y, w, h, cuanto=0.25, sal=0):
        """Hollin a manchas (lo de mas abajo y lo de los bordes, mas negro) y alguna chispa de ceniza clara."""
        kc = CARAS[cara]
        s0 = w * 29 + h * 13 + sal * 97
        g_ = 0.6 * _azar01(s0, kc, x // 2, y // 2) + 0.4 * _azar01(s0 + 5, kc, x, y)
        if g_ < cuanto * 0.45:
            return CARBON[1]
        if g_ < cuanto:
            return mezcla(c, CARBON[1], 0.5)
        if g_ > 0.985:
            return CENIZA[3]
        return c

    def piel(cara, x, y, w, h):
        c = carbon(cara, x, y, w, h)
        if _grieta(cara, x, y):
            return BRASA[1 + (x + y) % 2]               # la grieta (por dentro, la brasa: capa de brillo)
        if (x * 5 + y * 3 + CARAS[cara]) % 17 == 0:
            return CENIZA[1]
        return c

    def cabeza(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if cara == "frente":
            C = {"K": "#050303", "k": CARBON[0], "o": CARBON[2], "c": CARBON[3], "r": BRASA[1], "R": BRASA[2]}
            v = dibujo([
                "occccco",
                "oKKcKKo",
                "oKRcRKo",
                "occkcco",
                "ocrrrco",
                "okKKKko",
                "oorkroo",
                "ooooooo",
            ], C)(cara, x, y, w, h)
            return v if v is not None else c
        if cara == "arriba":
            return CARBON[1] if (x + y) % 3 else BRASA[1]  # el craneo pelado, quemado
        return c

    def torso(cara, x, y, w, h):
        return piel(cara, x, y, w, h)

    def brazo(cara, x, y, w, h):
        return piel(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 1:
            return CARBON[0]                           # los dedos
        return piel(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        if variante == 2:
            return piel(cara, x, y, w, h)
        return quemado(cuero(cara, x, y, w, h), cara, x, y, w, h, 0.3, 1)

    def pierna(cara, x, y, w, h):
        if y >= h - 2:
            return CARBON[0] if variante == 2 else CUERO[0]
        return muslo(cara, x, y + 6, w, h)

    # ---- la guardia
    def armadura(cara, x, y, w, h, sal):
        c = quemado(acero(cara, x, y, w, h), cara, x, y, w, h, 0.22, sal)
        if _grieta(cara, x, y, GRIETAS_ARMADURA):
            return BRASA[1]                            # una raja en el metal, al rojo
        if y == 0 or (cara in ("frente", "espalda") and x in (0, w - 1)):
            return claro(c, 0.12)                      # el canto
        return c

    def yelmo(cara, x, y, w, h):
        if cara == "frente":
            # la rendija de la vista (por ella, los ojos) y los respiraderos
            if y == 3 and 1 <= x <= w - 2:
                return "#050303"
            if y in (5, 6) and x in (2, 4, 6) and w > 7:
                return CARBON[0]
            if x == w // 2 and y >= 4:
                return claro(acero(cara, x, y, w, h), 0.15)   # la nervadura del frente
        return armadura(cara, x, y, w, h, 2)

    def cimera(cara, x, y, w, h):
        return quemado(ROJO[2] if (x + y) % 2 else ROJO[1], cara, x, y, w, h, 0.5, 3)

    def peto(cara, x, y, w, h):
        c = armadura(cara, x, y, w, h, 4)
        if cara == "frente" and y in (3, 6):
            return oscuro(c, 0.25)                     # las lamas
        return c

    def faldar(cara, x, y, w, h):
        if cara == "abajo":
            return ACERO[0]
        c = armadura(cara, x, y, w, h, 5)
        if y % 2 == 1:
            return oscuro(c, 0.2)
        return c

    def sobreveste(cara, x, y, w, h, espalda=False):
        if cara not in ("frente", "espalda"):
            return ROJO[1]
        # el bajo roto y quemado
        corte = h - 1 - (0, 2, 1, 3, 0, 2)[x % 6]
        if y > corte:
            return None
        c = quemado(rojo(cara, x, y, w, h), cara, x, y, w, h, 0.3 + 0.03 * y, 6)
        if y >= corte - 1:
            return BRASA[1] if (x + y) % 2 else CARBON[1]   # el borde aun en ascuas
        if not espalda:
            # la mano alzada del Juramento, en oro gastado
            M = {(2, 2), (3, 2), (2, 3), (3, 3), (1, 3), (4, 3), (1, 4), (2, 4), (3, 4), (4, 4), (2, 5), (3, 5), (2, 6), (3, 6)}
            if (x, y) in M:
                return ORO[2] if (x + y) % 3 else ORO[1]
        return c

    def escudo(cara, x, y, w, h):
        if cara != "abajo":
            return quemado(madera_t(cara, x, y, w, h), cara, x, y, w, h, 0.4, 7)
        if x in (0, w - 1) or y in (0, h - 1):
            return armadura(cara, x, y, w, h, 8)        # el reborde de hierro
        c = quemado(rojo(cara, x, y, w, h), cara, x, y, w, h, 0.38, 9)
        # la mano alzada, grande (en esta cara la y va de abajo arriba)
        cx, cy = w // 2, h // 2 - 1
        dx, dy = x - cx, (h - 1 - y) - cy
        if -2 <= dx <= 1 and -4 <= dy <= -1 and not (dy == -4 and dx in (-2, 1)):
            return ORO[3] if (x + y) % 3 else ORO[2]    # los dedos
        if -3 <= dx <= 2 and 0 <= dy <= 2:
            return ORO[2] if (x + y) % 2 else ORO[1]    # la palma
        if -1 <= dx <= 0 and 3 <= dy <= 5:
            return ORO[1]                               # la muneca
        if _grieta(cara, x, y, GRIETAS_ARMADURA):
            return CARBON[1]
        return c

    def hoja(cara, x, y, w, h):
        # el acero ennegrecido; el filo, al rojo (capa de brillo)
        if cara in ("arriba", "abajo"):
            return BRASA[3] if y % 2 else BRASA[2]
        return ACERO[3] if (x + y) % 3 else ACERO[4]

    # ---- el incendiario
    def trapo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return quemado(trapo_t(cara, x, y, w, h), cara, x, y, w, h, 0.25, 10)

    def delantal(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return CUERO[1]
        if y >= h - 1 and x % 2:
            return None
        c = quemado(cuero(cara, x, y, w, h), cara, x, y, w, h, 0.25, 11)
        if (x * 3 + y * 7) % 13 == 0:
            return "#0c0a08"                           # una mancha de pez
        return c

    def cantaro(cara, x, y, w, h):
        c = barro(cara, x, y, w, h)
        if y == 1:
            return oscuro(c, 0.3)                      # la cuerda
        if (x + y) % 5 == 0:
            return "#0c0a08"                           # pez que chorrea
        return c

    # ---- el ceniciento
    def harapos(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        corte = h - 1 - (0, 3, 1, 4, 2, 0, 3, 1)[(x + CARAS[cara]) % 8]
        if y > corte:
            return None
        if (x * 3 + y * 5 + CARAS[cara]) % 9 == 0 and y > 3:
            return None                                # agujeros quemados
        return quemado(trapo_t(cara, x, y, w, h), cara, x, y, w, h, 0.45, 12)

    def ceniza_capa(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara != "arriba" and y >= 1 and (x + CARAS[cara]) % 3:
            return None
        return ceniza(cara, x, y, w, h)

    mats = {
        "cabeza": sombra(cabeza, abajo=0.15),
        "torso": sombra(torso),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.12), "antebrazo_i": sombra(antebrazo, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.15), "pierna_i": sombra(pierna, abajo=0.15),
        "yelmo": sombra(yelmo, abajo=0.15),
        "cimera": cimera,
        "hombrera": sombra(lambda cara, x, y, w, h: armadura(cara, x, y, w, h, 13), abajo=0.2),
        "guantelete": sombra(lambda cara, x, y, w, h: armadura(cara, x, y, w, h, 14), abajo=0.15),
        "greba": sombra(lambda cara, x, y, w, h: armadura(cara, x, y, w, h, 15), abajo=0.15),
        "peto": sombra(peto, abajo=0.1),
        "faldar": sombra(faldar, abajo=0.1),
        "sobreveste": sobreveste,
        "sobreveste_espalda": lambda cara, x, y, w, h: sobreveste(cara, x, y, w, h, True),
        "escudo": sombra(escudo, abajo=0.08),
        "escudo_asa": metal(ACERO[1:4], 7121),
        "hoja": hoja,
        "guarda": metal(ACERO[2:6], 7122),
        "puno_espada": lambda cara, x, y, w, h: CUERO[1] if y % 2 else CUERO[2],
        "pomo": metal(ACERO[2:6], 7123),
        "trapo": sombra(trapo, abajo=0.1),
        "pano_cabeza": sombra(trapo, abajo=0.1),
        "delantal": delantal,
        "cinturon": lambda cara, x, y, w, h: CUERO[2] if (x + y) % 4 else ORO[0],
        "cantarillo": sombra(cantaro, abajo=0.2),
        "cantaro": sombra(cantaro, abajo=0.2),
        "cantaro_boca": lambda cara, x, y, w, h: BARRO[1] if cara != "arriba" else "#0c0a08",
        "mecha": lambda cara, x, y, w, h: TRAPO[2] if y > 0 else BRASA[4],
        "llama": lambda cara, x, y, w, h: BRASA[4 + (x + y) % 3] if cara != "abajo" else BRASA[3],
        "harapos": harapos,
        "ceniza_capa": ceniza_capa,
    }
    return mats


def brillo(variante):
    """Lo que luce: las ascuas de los ojos y las brasas de las grietas; el filo de la espada; la mecha y su llama."""
    C = {"a": BRASA[3], "b": BRASA[5], "c": BRASA[6], "r": BRASA[2]}
    ojos = dibujo([
        ".......",
        ".......",
        "..b.b..",
        ".......",
        "..rrr..",
        ".......",
        "...r...",
    ], C)

    def grietas(cara, x, y, w, h):
        if _grieta(cara, x, y):
            return BRASA[3 + (x + y) % 3]
        return None

    def cara_viva(cara, x, y, w, h):
        if cara == "frente":
            return ojos(cara, x, y, w, h)
        return grietas(cara, x, y, w, h)

    b = {"torso": grietas, "brazo_d": grietas, "brazo_i": grietas, "antebrazo_d": grietas, "antebrazo_i": grietas}
    if variante == 0:
        # la guardia: los ojos por la rendija del yelmo, las rajas al rojo de la armadura y el filo de la espada
        b["yelmo"] = por_cara(lambda cara, x, y, w, h: None,
                              frente=lambda cara, x, y, w, h: BRASA[5] if y == 3 and x in (2, 3, 5, 6) else None)
        b["hoja"] = lambda cara, x, y, w, h: BRASA[4 + (y % 2)] if cara in ("arriba", "abajo") else (BRASA[3] if (x + y) % 4 == 0 else None)
        b["peto"] = lambda cara, x, y, w, h: BRASA[3] if _grieta(cara, x, y, GRIETAS_ARMADURA) else None
        b["sobreveste"] = lambda cara, x, y, w, h: (BRASA[4] if cara in ("frente", "espalda") and y >= h - 1 - (0, 2, 1, 3, 0, 2)[x % 6] - 1
                                                    and y <= h - 1 - (0, 2, 1, 3, 0, 2)[x % 6] and (x + y) % 2 else None)
    else:
        b["cabeza"] = cara_viva
        b["muslo_d"] = b["muslo_i"] = (grietas if variante == 2 else None)
        if variante == 2:
            b["pierna_d"] = b["pierna_i"] = grietas
    if variante == 1:
        b["mecha"] = lambda cara, x, y, w, h: BRASA[5] if y == 0 else None
        b["llama"] = lambda cara, x, y, w, h: BRASA[4 + (x + y) % 3] if cara != "abajo" else None
    if variante == 2:
        b["ceniza_capa"] = lambda cara, x, y, w, h: BRASA[4] if cara == "arriba" and (x * 7 + y * 3) % 11 == 0 else None
    return {k: v for k, v in b.items() if v is not None}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14: llega
# a unos 11). Las animaciones de "porte" llevan el brazo derecho (y la guardia, tambien el izquierdo con su escudo): se suman
# al quieto.

# de pie: encorvado, humeando, la cabeza un poco adelantada
BASE = dict(raiz=raiz(-0.3), torso=(10, 0, 2), cabeza=(2, 0, 4), muslo_d=(-4, 0, 3), pierna_d=(6, 0, 0), muslo_i=(2, 0, -3),
            pierna_i=(4, 0, 0), brazo_i=(-4, 0, -8), antebrazo_i=(-12, 0, 0))
# lo que lleva cada uno: la espada baja y el escudo delante; el cantaro en la mano; el ceniciento, los brazos colgando
PORTE_0 = dict(_mano_d=("h", -1.0, -9.5, 4.5), _arma=(0.05, -0.45, 0.9), brazo_i=(-30, 0, -6), antebrazo_i=(-50, 0, 0))
PORTE_1 = dict(brazo_d=(-12, 0, 10), antebrazo_d=(-72, 0, 0))
PORTE_2 = dict(brazo_d=(-8, 0, 8), antebrazo_d=(-10, 0, 0))
GUARDIA = dict(raiz=raiz(-1.2), torso=(18, 8, 0), cabeza=(-10, -6, 0), muslo_d=(-16, 0, 6), pierna_d=(26, 0, 0),
               muslo_i=(-4, 0, -6), pierna_i=(20, 0, 0), brazo_i=(-24, 0, -16), antebrazo_i=(-40, 0, 0))
# la guardia: el escudo alzado delante (el antebrazo izquierdo de punta arriba) y la espada lista por encima
CUBIERTO = dict(brazo_i=(-62, 0, -4), antebrazo_i=(-58, 0, 0))
LISTO_0 = con(GUARDIA, CUBIERTO, _mano_d=("h", -2.0, -3.0, 8.0), _arma=(0.1, 0.55, 0.83))
LISTO_1 = con(GUARDIA, brazo_d=(-30, 0, 22), antebrazo_d=(-56, 0, 0))
LISTO_2 = con(GUARDIA, raiz=raiz(-1.8), torso=(28, 0, 0), cabeza=(-20, 0, 0), brazo_d=(-50, 0, 16), antebrazo_d=(-30, 0, 0),
              brazo_i=(-50, 0, -16), antebrazo_i=(-30, 0, 0))
# agazapado junto a los rescoldos (REZA): en cuclillas, los brazos sobre las rodillas, la cabeza baja
AGAZAPADO = dict(raiz=raiz(-7.0, 0.6), torso=(34, 0, 0), cabeza=(14, 0, 0),
                 muslo_d=(-100, 0, 8), pierna_d=(120, 0, 0), muslo_i=(-96, 0, -8), pierna_i=(118, 0, 0),
                 brazo_d=(-40, 0, 6), antebrazo_d=(-30, 0, 0), brazo_i=(-40, 0, -6), antebrazo_i=(-30, 0, 0))
AGAZAPADOS = {
    0: con(AGAZAPADO, _mano_d=("h", 0.0, -7.0, 6.0), _arma=(0.0, -0.6, 0.8), brazo_i=(-56, 0, -4), antebrazo_i=(-30, 0, 0)),
    1: AGAZAPADO,
    2: AGAZAPADO,
}
# enterrado en la ceniza: de bruces, encogido, un brazo por delante
ENTERRADO = dict(raiz=raiz(2.4, -13.0, rot=(90, 0, 0)), torso=(0, 0, 0), cabeza=(-30, 10, 0),
                 muslo_d=(10, 0, 6), pierna_d=(30, 0, 0), muslo_i=(4, 0, -6), pierna_i=(16, 0, 0),
                 brazo_d=(-150, 0, 14), antebrazo_d=(-20, 0, 0), brazo_i=(-10, 0, -14), antebrazo_i=(-30, 0, 0))
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
        """La pose con sus objetivos ya resueltos: un bucle casi quieto no necesita un fotograma por tick."""
        return resolver(r, pose)

    # ---- quieto: respira humo (el pecho sube y baja despacio; de vez en cuando un tiron)
    bucle("quieto", 3.2, [
        (0.0, BASE),
        (1.2, con(BASE, torso=(7, 0, 2), cabeza=(-2, 0, 4), raiz=raiz(-0.1))),
        (2.0, con(BASE, torso=(12, 3, 1), cabeza=(6, -4, 5))),
        (2.3, con(BASE, torso=(11, -2, 3), cabeza=(4, 4, 3))),
        (3.2, BASE),
    ])
    # ---- andar: arrastrando los pies, pesado
    pasos = []
    for k, (md, mi, pd, pi_, ty) in enumerate(((-24, 20, 10, 16, 6), (-6, 4, 6, 30, 0), (20, -24, 16, 10, -6), (4, -6, 30, 6, 0), (-24, 20, 10, 16, 6))):
        pasos.append((1.2 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (4, ty, 0), "cabeza": (0, -ty * 0.5, 0), "raiz": raiz(0.6 if k % 2 else 0.0),
                                    "brazo_i": (-mi * 0.3, 0, 0)}))
    bucle("andar", 1.2, pasos)
    q0 = fija(con(BASE, PORTE_0))
    bucle("porte_guardia", 3.2, [(0.0, q0), (3.2, q0)], solo=BRAZOS)
    q1 = con(BASE, PORTE_1)
    bucle("porte_incendiario", 3.2, [(0.0, q1), (1.6, con(q1, antebrazo_d=(-78, 0, 0))), (3.2, q1)], solo=BRAZO_D)
    q2 = con(BASE, PORTE_2)
    bucle("porte_ceniciento", 3.2, [(0.0, q2), (1.6, con(q2, brazo_d=(-4, 0, 10))), (3.2, q2)], solo=BRAZO_D)
    l0 = fija(LISTO_0)
    bucle("listo_guardia", 2.0, [(0.0, l0), (1.0, fija(con(LISTO_0, raiz=raiz(-1.6), torso=(22, 6, 0)))), (2.0, l0)])
    bucle("listo_incendiario", 2.0, [(0.0, LISTO_1), (1.0, con(LISTO_1, raiz=raiz(-1.6), antebrazo_d=(-62, 0, 0))), (2.0, LISTO_1)])
    bucle("listo_ceniciento", 1.6, [(0.0, LISTO_2), (0.8, con(LISTO_2, raiz=raiz(-2.2), torso=(32, 4, 0), cabeza=(-24, -4, 0))), (1.6, LISTO_2)])
    # ---- agazapado junto a los rescoldos (casi quieto: la cabeza se mece)
    for v in range(3):
        ag = fija(AGAZAPADOS[v])
        bucle(f"agazapado_{VARIANTES[v]}", 5.0, [(0.0, ag), (2.5, con(ag, cabeza=(20, 6, 0))), (5.0, ag)])
    # ---- enterrado en la ceniza
    bucle("enterrado", 6.0, [(0.0, ENTERRADO), (6.0, ENTERRADO)])

    # ================================================================ acciones

    # ---- 1 tajo (la guardia): alza la espada por encima del hombro y la baja en diagonal (el escudo sigue delante)
    T_ALTO = con(sin(LISTO_0, "_mano_d"), raiz=raiz(-0.6, 1.0), torso=(4, 30, 0), cabeza=(-10, -20, 0),
                 _mano_d=("h", -3.5, 9.0, 1.5), _arma=(-0.3, 0.6, -0.75))
    T_ABAJO = con(sin(LISTO_0, "_mano_d"), raiz=raiz(-2.4, -1.8), torso=(30, -24, 0), cabeza=(-20, 16, 0), muslo_i=(-34, 0, -6),
                  pierna_i=(36, 0, 0), _mano_d=("h", 4.0, -5.0, 8.0), _arma=(0.45, -0.55, 0.7))
    accion("tajo", 1.6, [
        (0.00, LISTO_0),
        (0.55, T_ALTO),
        (0.80, con(T_ALTO, torso=(2, 34, 0))),
        (0.92, con(sin(LISTO_0, "_mano_d"), raiz=raiz(-1.4, 0.0), torso=(16, 6, 0), _mano_d=("h", 0.5, 3.0, 9.6), _arma=(0.15, 0.25, 0.95))),
        (1.00, T_ABAJO),
        (1.30, con(T_ABAJO, torso=(28, -26, 0))),
        (1.60, LISTO_0),
    ], golpes=(0.96,))
    # ---- 2 escudazo (la guardia): recoge el escudo al pecho y embiste con el
    E_RECOGE = con(LISTO_0, raiz=raiz(-1.0, 1.2), torso=(6, 20, 0), cabeza=(-6, -14, 0), brazo_i=(-50, 0, -20), antebrazo_i=(-80, 0, 0))
    E_EMBISTE = con(LISTO_0, raiz=raiz(-2.4, -3.0), torso=(30, -16, 0), cabeza=(-20, 10, 0), muslo_i=(-46, 0, -6), pierna_i=(40, 0, 0),
                    muslo_d=(10, 0, 6), pierna_d=(20, 0, 0), brazo_i=(-84, 0, 6), antebrazo_i=(-20, 0, 0))
    accion("escudazo", 1.3, [
        (0.00, LISTO_0),
        (0.45, E_RECOGE),
        (0.62, E_EMBISTE),
        (0.85, E_EMBISTE),
        (1.30, LISTO_0),
    ], golpes=(0.62,))
    # ---- 3 bloqueo (la guardia): para un golpe con el escudo (un respingo y vuelve a cubrirse)
    BL = con(LISTO_0, raiz=raiz(-1.6, 1.2), torso=(8, 12, 0), cabeza=(-4, -8, 0), brazo_i=(-70, 0, -2), antebrazo_i=(-64, 0, 0))
    accion("bloqueo", 0.6, [
        (0.00, LISTO_0),
        (0.10, BL),
        (0.30, con(BL, torso=(12, 10, 0))),
        (0.60, LISTO_0),
    ])
    # ---- 4 lanzar (el incendiario): echa atras el cantaro por encima del hombro y lo lanza en arco
    L_ATRAS = con(LISTO_1, raiz=raiz(-0.8, 1.6), torso=(-8, 34, 0), cabeza=(-14, -26, 0), brazo_i=(-60, 0, -30), antebrazo_i=(-20, 0, 0),
                  brazo_d=(-150, 0, 30), antebrazo_d=(-50, 0, 0))
    L_SUELTA = con(LISTO_1, raiz=raiz(-1.8, -1.8), torso=(26, -20, 0), cabeza=(-18, 14, 0), brazo_i=(-20, 0, -40), muslo_i=(-36, 0, -6),
                   pierna_i=(36, 0, 0), brazo_d=(-110, 0, 4), antebrazo_d=(-6, 0, 0))
    accion("lanzar", 1.5, [
        (0.00, LISTO_1),
        (0.55, L_ATRAS),
        (0.75, con(L_ATRAS, torso=(-10, 38, 0))),
        (0.85, L_SUELTA),
        (1.10, con(L_SUELTA, brazo_d=(-60, 0, 10), antebrazo_d=(-10, 0, 0))),
        (1.50, LISTO_1),
    ], golpes=(0.86,))
    # ---- 5 tea (el incendiario): de cerca, un golpe de lado con el cantaro encendido
    S_ATRAS = con(LISTO_1, raiz=raiz(-1.0, 1.0), torso=(10, 46, 0), cabeza=(-8, -36, 0), brazo_d=(-50, 0, 80), antebrazo_d=(-30, 0, 0))
    S_FIN = con(LISTO_1, raiz=raiz(-1.8, -1.4), torso=(18, -40, 0), cabeza=(-12, 30, 0), brazo_d=(-90, -20, -30), antebrazo_d=(-20, 0, 0))
    accion("tea", 1.1, [
        (0.00, LISTO_1),
        (0.40, S_ATRAS),
        (0.55, con(LISTO_1, raiz=raiz(-1.6, -0.6), torso=(16, 0, 0), brazo_d=(-95, 0, 20), antebrazo_d=(-10, 0, 0))),
        (0.65, S_FIN),
        (1.10, LISTO_1),
    ], golpes=(0.57,))
    # ---- 6 agarron (el ceniciento): se echa encima con los dos brazos por delante y aprieta
    G_SALTA = con(LISTO_2, raiz=raiz(-3.0, -5.0), torso=(52, 0, 0), cabeza=(-40, 0, 0), muslo_d=(30, 0, 6), pierna_d=(20, 0, 0),
                  muslo_i=(-50, 0, -6), pierna_i=(40, 0, 0), brazo_d=(-100, 0, 14), antebrazo_d=(-10, 0, 0), brazo_i=(-100, 0, -14),
                  antebrazo_i=(-10, 0, 0))
    G_APRIETA = con(G_SALTA, raiz=raiz(-2.4, -4.0), torso=(40, 0, 0), brazo_d=(-90, 0, -12), antebrazo_d=(-50, 0, 0), brazo_i=(-90, 0, 12),
                    antebrazo_i=(-50, 0, 0))
    accion("agarron", 1.8, [
        (0.00, LISTO_2),
        (0.45, con(LISTO_2, raiz=raiz(-3.4, 1.0), torso=(40, 0, 0), brazo_d=(-30, 0, 40), brazo_i=(-30, 0, -40))),
        (0.65, G_SALTA),
        (0.80, G_APRIETA),
        (1.30, con(G_APRIETA, torso=(44, 6, 0))),
        (1.80, LISTO_2),
    ], golpes=(0.72,))
    # ---- 7 zarpazo (el ceniciento)
    Z_ALTO = con(LISTO_2, raiz=raiz(-1.2, 0.8), torso=(14, 30, 0), cabeza=(-10, -20, 0), brazo_d=(-150, 0, 40), antebrazo_d=(-30, 0, 0))
    Z_BAJA = con(LISTO_2, raiz=raiz(-2.2, -1.4), torso=(36, -20, 0), cabeza=(-24, 14, 0), brazo_d=(-50, 0, -20), antebrazo_d=(-10, 0, 0))
    accion("zarpazo", 1.0, [
        (0.00, LISTO_2),
        (0.35, Z_ALTO),
        (0.48, Z_BAJA),
        (0.70, con(Z_BAJA, torso=(34, -24, 0))),
        (1.00, LISTO_2),
    ], golpes=(0.47,))
    # ---- 8 aturdido
    AT = dict(raiz=raiz(-1.4, 2.0), torso=(-10, 0, 8), cabeza=(-22, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0), muslo_i=(12, 0, -8),
              pierna_i=(18, 0, 0), brazo_d=(16, 0, 26), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -28), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.6))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir: se dobla, cae de rodillas y se derrumba de lado (el servidor lo deshace en ceniza y brasas)
    RODILLAS = dict(raiz=raiz(-6.0, 0.8), torso=(26, 0, 0), cabeza=(30, 0, 0), muslo_d=(-10, 0, 5), pierna_d=(96, 0, 0), muslo_i=(-8, 0, -5),
                    pierna_i=(96, 0, 0), brazo_d=(-10, 0, 14), antebrazo_d=(-10, 0, 0), brazo_i=(-10, 0, -14), antebrazo_i=(-10, 0, 0))
    CAIDO = dict(raiz=raiz(1.6, -6.0, rot=(0, 0, 84)), torso=(30, 0, 0), cabeza=(20, 0, 10), muslo_d=(-60, 0, 6), pierna_d=(80, 0, 0),
                 muslo_i=(-40, 0, -6), pierna_i=(70, 0, 0), brazo_d=(-30, 0, 30), antebrazo_d=(-40, 0, 0), brazo_i=(-60, 0, -20),
                 antebrazo_i=(-30, 0, 0))
    accion("morir", 2.2, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0))),
        (0.50, RODILLAS),
        (1.10, con(RODILLAS, torso=(40, 0, 10), cabeza=(40, 0, 10))),
        (1.60, CAIDO),
        (2.20, CAIDO),
    ])
    # ---- 10 levantarse: estaba agazapado junto a los rescoldos; se pone en pie de un tiron (chispas)
    accion("levantarse", 1.2, [
        (0.00, AGAZAPADO),
        (0.35, con(AGAZAPADO, raiz=raiz(-5.0, 0.4), torso=(20, 0, 0), cabeza=(-20, 0, 0))),
        (0.75, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0))),
        (1.20, GUARDIA),
    ])
    # ---- 11 alzarse: enterrado en la ceniza, se incorpora de golpe sacudiendose la ceniza
    accion("alzarse", 1.6, [
        (0.00, ENTERRADO),
        (0.40, con(ENTERRADO, raiz=raiz(2.0, -11.0, rot=(60, 0, 0)), brazo_d=(-90, 0, 30), brazo_i=(-90, 0, -30))),
        (0.85, con(AGAZAPADO, torso=(10, 0, 0), cabeza=(-20, 0, 0), brazo_d=(-20, 0, 50), brazo_i=(-20, 0, -50))),
        (1.25, con(GUARDIA, raiz=raiz(-3.0), muslo_d=(-50, 0, 6), pierna_d=(70, 0, 0))),
        (1.60, GUARDIA),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "incendiario" in nombre or nombre in ("lanzar", "tea"):
        return 1
    if "ceniciento" in nombre or nombre in ("agarron", "zarpazo", "alzarse", "enterrado"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_guardia"]]
    return []
