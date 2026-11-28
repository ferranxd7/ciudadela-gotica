"""
Los Insomnes del Fuerte de la Marca (parte 7aw): la guarnicion sin relevo. Cuando la Peste Gris bajo por las Llanuras Palidas, el
castellano de la Marca recibio la orden del rey: que nadie entrase en la Capital. Los refugiados acamparon al pie del fuerte esperando
que se abriera la puerta; no se abrio. La guarnicion esperaba el relevo, que nunca llego. La noche del cielo rojo se levantaron en
sus puestos y siguen de guardia: no duermen. Grises, chupados, con los ojos abiertos de par en par (rojos en el borde, una lucecita
en el centro: lucen) y las ojeras negras; el sobreveste ocre de la Marca con su torre negra. Tres:

  0 centinela: el capacete (el casco de ala ancha), el almofar de malla, el gambeson y el sobreveste; la lanza con su banderola
    y el cuerno de rebato colgado a la cadera. Estocada, barrido y el CUERNO: se lo lleva a la boca y toca a rebato (si se le pega a
    tiempo, se le corta). Espera de guardia (REZA): mira a un lado y a otro, sin parpadear.
  1 ballestero: el capacete, el jubon de cuero con malla en los hombros, el paves a la espalda (la torre pintada), el carcaj de
    virotes y el torno de armar al cinto; la ballesta de acero. Dispara el virote pesado (no lo para el escudo) y tarda en volver
    a armarla (la manivela); de cerca, culatazo. Espera de guardia o sentado (TUMBADO: en el suelo, con los ojos abiertos).
  2 escudero: mas grande (el dibujante lo agranda); el bacinete cerrado (por la rendija, los ojos), el peto y las hombreras, el
    ESCUDO DE TORRE en el brazo izquierdo (de frente no le entra casi nada) y la maza de pestanas. Mazazo, escudazo y carga con el
    escudo por delante. Espera de guardia (REZA), apoyado en el escudo.
"""
from anim import Anim, con, raiz, resolver, sin  # noqa: F401
from modelo import Box, Rig, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "insomne"
VARIANTES = ("centinela", "ballestero", "escudero")
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), cabeza=(8, 8, 8), brazo=(3, 13, 3), pierna=(4, 12, 4), hombro=0.6)
    # ---- lo de los tres: los faldones del sobreveste o del jubon y el cinto
    for n in ("d", "i"):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.35, -0.5, -2.5, 4.7, 6.4, 5.0, f"faldon_{n}")])
    r.pieza("cinto", "torso", (0, 0, 0), boxes=[Box(-4.35, -2.3, -2.85, 8.7, 1.5, 5.7, "cinto")])
    # ---- 0 y 1: el capacete y el almofar de malla (por los lados y la nuca: la cara se ve)
    r.pieza("capacete", "cabeza", (0, 0, 0), boxes=[
        Box(-6.2, -7.6, -6.2, 12.4, 0.8, 12.4, "ala_capacete"),
        Box(-4.6, -10.8, -4.6, 9.2, 3.6, 9.2, "copa_capacete"),
        Box(-0.5, -11.5, -4.7, 1.0, 1.0, 9.4, "cresta"),
    ])
    r.pieza("almofar", "cabeza", (0, 0, 0), boxes=[
        Box(-4.5, -7.2, -4.0, 0.6, 7.6, 8.5, "almofar_lado"),
        Box(3.9, -7.2, -4.0, 0.6, 7.6, 8.5, "almofar_lado"),
        Box(-4.5, -7.2, 3.9, 9.0, 7.6, 0.6, "almofar_nuca"),
        Box(-4.5, -1.2, -4.5, 9.0, 1.8, 1.4, "almofar_barba"),
    ])
    # ---- 0, el centinela: la lanza con su banderola y el cuerno (a la cadera; en la mano mientras toca)
    r.pieza("lanza", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -26.0, 1.2, 1.2, 38.0, "asta"),
        Box(-1.0, -0.35, -31.6, 2.0, 0.7, 5.6, "punta_lanza"),
        Box(-0.8, -0.8, -26.6, 1.6, 1.6, 1.0, "virola"),
        Box(-0.12, 0.6, -25.6, 0.24, 3.4, 4.6, "banderola"),
    ])
    r.pieza("cuerno", "torso", (0, 0, 0), boxes=[
        Box(4.35, -2.9, -2.9, 1.1, 1.1, 1.7, "cuerno_boquilla"),
        Box(4.15, -3.15, -1.3, 1.5, 1.6, 2.1, "cuerno_cuerpo"),
        Box(3.9, -3.45, 0.8, 2.0, 2.2, 1.7, "cuerno_pabellon"),
    ])
    r.pieza("cuerno_mano", "mano_i", (0, 0.4, 0), boxes=[
        Box(-0.5, -0.5, -1.2, 1.0, 1.0, 2.0, "cuerno_boquilla"),
        Box(-0.8, -0.8, -3.4, 1.6, 1.6, 2.2, "cuerno_cuerpo"),
        Box(-1.2, -1.2, -5.4, 2.4, 2.4, 2.0, "cuerno_pabellon"),
    ])
    # ---- 1, el ballestero: la ballesta (la cureña, el arco de acero, la cuerda y el virote armado), el paves a la espalda, el carcaj
    # y el torno de armar al cinto
    r.pieza("ballesta", "mano_d", (0, 0, 0), boxes=[
        Box(-0.9, -0.8, -14.0, 1.8, 2.2, 20.0, "curena"),
        Box(-9.0, -0.5, -14.6, 18.0, 1.0, 1.4, "arco_ballesta"),
        Box(-8.4, -0.15, -7.4, 16.8, 0.3, 0.3, "cuerda"),
        Box(-1.5, -0.9, -16.6, 3.0, 2.6, 0.8, "estribo"),
    ])
    r.pieza("virote_armado", "ballesta", (0, 0, 0), boxes=[Box(-0.3, -1.5, -15.2, 0.6, 0.7, 8.4, "virote")])
    r.pieza("paves", "torso", (0, -12.0, 2.6), rot=(-6, 0, 0), boxes=[
        Box(-5.2, -1.6, 0.0, 10.4, 15.4, 1.3, "paves"),
    ])
    r.pieza("carcaj", "torso", (0, 0, 0), boxes=[
        Box(-6.0, -7.0, 0.4, 2.0, 5.4, 2.2, "carcaj"),
        Box(-5.8, -8.4, 0.6, 1.6, 1.4, 1.8, "plumas"),
    ])
    r.pieza("torno", "torso", (0, 0, 0), boxes=[
        Box(3.9, -4.6, -1.4, 1.8, 2.6, 2.8, "torno"),
        Box(5.6, -4.0, -0.4, 0.8, 0.8, 1.9, "manivela"),
    ])
    # ---- 2, el escudero: el bacinete cerrado, el peto, las hombreras, el escudo de torre y la maza de pestanas
    r.pieza("bacinete", "cabeza", (0, 0, 0), boxes=[
        Box(-4.6, -8.8, -4.6, 9.2, 9.4, 9.2, "bacinete"),
        Box(-0.45, -9.5, -4.8, 0.9, 1.0, 9.6, "cresta"),
    ])
    r.pieza("peto", "torso", (0, 0, 0), boxes=[
        Box(-4.5, -12.3, -3.05, 9.0, 8.6, 6.1, "peto"),
        Box(-4.6, -4.1, -3.1, 9.2, 2.0, 6.2, "faja"),
    ])
    for n, s in (("d", -1), ("i", 1)):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-2.45, -2.6, -2.45, 4.9, 3.6, 4.9, "hombrera")])
        r.pieza(f"brazal_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-1.85, 0.6, -1.85, 3.7, 4.6, 3.7, "brazal")])
    r.pieza("escudo", "antebrazo_i", (0, 3.0, 0), boxes=[
        Box(-5.6, -9.5, -3.2, 12.0, 22.0, 1.4, "escudo"),
        Box(-0.7, -1.5, -1.9, 1.4, 3.0, 0.5, "abrazadera"),
    ])
    r.pieza("maza", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -11.6, 1.2, 1.2, 14.6, "mango_maza"),
        Box(-1.5, -1.5, -15.6, 3.0, 3.0, 4.2, "cabeza_maza"),
        Box(-2.5, -0.45, -15.3, 5.0, 0.9, 3.6, "pestana"),
        Box(-0.45, -2.5, -15.3, 0.9, 5.0, 3.6, "pestana"),
        Box(-0.4, -0.4, -17.0, 0.8, 0.8, 1.5, "pincho"),
    ])
    r.empaquetar()
    return r


CENTINELA_P = ["lanza", "cuerno", "cuerno_mano"]
BALLESTERO_P = ["ballesta", "virote_armado", "paves", "carcaj", "torno"]
ESCUDERO_P = ["bacinete", "peto", "hombrera_d", "hombrera_i", "brazal_d", "brazal_i", "escudo", "maza"]
#: lo que se esconde en cada variante (el cuerno de la mano solo sale mientras toca: lo pone el modelo de Java)
OCULTAS = {
    0: BALLESTERO_P + ESCUDERO_P + ["cuerno_mano"],
    1: CENTINELA_P + ESCUDERO_P,
    2: CENTINELA_P + BALLESTERO_P + ["capacete", "almofar"],
}

# ---------------------------------------------------------------- las texturas

PIEL = ["#2f302e", "#454744", "#5b5e5a", "#71746f", "#878a84", "#9ea19a"]   # gris, chupada, sin sangre
OJERA = ["#241b25", "#33263a", "#43324a"]
OJO, OJO_R, PUPILA, PUPILA_B = "#d7d0bd", "#8a1f1a", "#ffb347", "#ffe3a0"
BARBA = ["#262420", "#33302b", "#403c35"]
HIERRO = ["#16171b", "#23252a", "#31343a", "#42464d", "#575c64"]
MALLA = ["#1c1d21", "#2c2e33", "#3e4148", "#545861"]
OXIDO = ["#4a2512", "#6b3719", "#8c4c22"]
OCRE = ["#4d3f1c", "#695527", "#857034", "#a08a45", "#b8a25a"]                # el sobreveste de la Marca, desvaido
TORRE = "#141210"
GAMBESON = ["#3b3122", "#4c402d", "#5e5039", "#706046", "#827154"]
CUERO = ["#1d140d", "#2a1d13", "#3a2a1b", "#4c3725", "#5e4630"]
CALZA = ["#1c1a17", "#26231f", "#312d28", "#3c3731"]
BOTA = ["#100c09", "#1a140f", "#251d16"]
MADERA = ["#24180e", "#331f11", "#432a17", "#54361f", "#674529"]
MADERA_C = ["#4a3520", "#60462b", "#775837", "#8d6b45"]
LATON = ["#3f3014", "#634c20", "#8a6c33", "#ae8d49", "#cfae66"]
MARFIL = ["#6e634a", "#8f8263", "#ada07c", "#c8bb95", "#ddd2ae"]
ACERO = ["#26282d", "#393c42", "#51555c", "#6c7178", "#8a9098"]
SANGRE = ["#2a0a08", "#4a1410"]
CUERDA = ["#5a5040", "#7a6e58"]
PLUMA = ["#b9b3a3", "#8f8878"]

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def materiales(variante):
    piel_t = ruido(PIEL[1:], 7701, grano=0.42, base=0.55)
    hierro_t = metal(HIERRO, 7702)
    malla_t = ruido(MALLA, 7703, grano=0.6, base=0.5)
    ocre_t = tela(OCRE, 7704, pliegue=4, grano=0.16, luz=0.12)
    gambeson_t = tela(GAMBESON, 7705, pliegue=3, grano=0.12, luz=0.1)
    cuero_t = ruido(CUERO[1:], 7706, grano=0.38, base=0.5)
    calza_t = tela(CALZA, 7707, pliegue=3, grano=0.12, luz=0.08)
    madera_t = madera(MADERA, 7708, "z")
    madera_c = madera(MADERA_C, 7709, "y")
    acero_t = metal(ACERO, 7710)

    def oxidado(f, semilla, cuanto=0.1):
        r = __import__("numpy").random.default_rng(semilla).random((64, 64))

        def g(cara, x, y, w, h):
            c = f(cara, x, y, w, h)
            if c is None:
                return None
            k = r[(y + 5 * CARAS[cara]) % 64, (x + 9 * CARAS[cara]) % 64]
            if k < cuanto:
                return mezcla(c, OXIDO[int(k * 100) % 3], 0.6)        # el oxido, a manchas
            return c
        return g

    def malla(cara, x, y, w, h):
        c = malla_t(cara, x, y, w, h)
        return claro(c, 0.08) if (x + y) % 2 == 0 else oscuro(c, 0.12)    # los anillos

    # ---------------------------------------------------- la cabeza: la cara gris y los ojos abiertos de par en par
    def cabeza(cara, x, y, w, h):
        C = {"S": PIEL[3], "s": PIEL[2], "n": PIEL[1], "O": OJERA[1], "o": OJERA[0], "W": OJO, "R": OJO_R, "P": PUPILA,
             "B": BARBA[1], "b": BARBA[0], "M": "#140d0c", "k": PIEL[4]}
        if cara == "frente":
            if variante == 2:
                return PIEL[2]                                   # (la tapa el bacinete)
            return dibujo([
                "sSsssSss",
                "skssskss",
                "sOOssOOs",
                "RWPssPWR",
                "oRRssRRo",
                "sssnnsss",
                "bBMMMMBb",
                "bBBbbBBb",
            ], C)(cara, x, y, w, h)
        c = piel_t(cara, x, y, w, h)
        if cara in ("der", "izq") and y >= 6:
            return BARBA[(x + y) % 2]                            # la barba de dias
        if cara == "arriba" or (cara == "espalda" and y < 4):
            return BARBA[(x * 3 + y) % 3] if (x + y) % 2 else oscuro(c, 0.2)   # el pelo rapado
        return c

    # ---------------------------------------------------- el torso de cada uno
    def torso(cara, x, y, w, h):
        if variante == 0:
            # el sobreveste ocre de la Marca sobre el gambeson; en el pecho, la torre negra; la correa del cuerno en diagonal
            if cara in ("der", "izq") or (cara == "arriba" and (x < 1 or x > 6)):
                return gambeson_t(cara, x, y, w, h)
            c = ocre_t(cara, x, y, w, h)
            if cara == "frente":
                if (x - y) == 0 or (x - y) == 1:
                    return CUERO[3] if (x + y) % 3 else LATON[2]      # la correa del cuerno, del hombro derecho a la cadera
                if 2 <= x <= 5 and 2 <= y <= 7:
                    # la torre negra de la Marca
                    t = dibujo(["T.TT", "TTTT", ".TT.", ".TT.", ".TT.", "TTTT"], {"T": TORRE})(cara, x - 2, y - 2, 4, 6)
                    if t:
                        return t
            if cara == "espalda" and 3 <= x <= 4:
                return oscuro(c, 0.15)
            if y >= h - 2 and (x * 3 + CARAS[cara]) % 4 == 0:
                return oscuro(c, 0.3)                            # el bajo, desgarrado
            return c
        if variante == 1:
            # el jubon de cuero con malla en los hombros (por delante, los cordones)
            if y < 3:
                return malla(cara, x, y, w, h)
            c = cuero_t(cara, x, y, w, h)
            if cara == "frente" and x in (3, 4):
                return CUERDA[(y % 2)] if y % 2 == 0 else oscuro(c, 0.25)   # los cordones
            if cara == "espalda" and y > 3:
                return oscuro(c, 0.1)
            return c
        # el escudero: la cota de malla bajo el peto
        return malla(cara, x, y, w, h)

    # ---------------------------------------------------- brazos y piernas
    def brazo(pieza):
        def f(cara, x, y, w, h):
            if variante == 2:
                c = malla(cara, x, y, w, h)
                if pieza.startswith("antebrazo") and y >= h - 2:
                    return HIERRO[2] if (x + y) % 2 else HIERRO[3]         # los guanteletes
                return c
            c = gambeson_t(cara, x, y, w, h) if variante == 0 else malla(cara, x, y, w, h) if pieza.startswith("brazo") and y < 3 else cuero_t(cara, x, y, w, h)
            if variante == 0 and cara in ("frente", "espalda", "der", "izq") and y % 3 == 0:
                return oscuro(c, 0.18)                                   # el acolchado, a rayas
            if pieza.startswith("antebrazo") and 1 <= y <= 3:
                return CUERO[3] if (x + y) % 3 else LATON[1]             # el brazal de cuero
            if pieza.startswith("antebrazo") and y >= h - 2:
                return CUERO[2] if (x + y) % 2 else CUERO[1]             # los guantes
            return c
        return f

    def pierna(pieza):
        def f(cara, x, y, w, h):
            if variante == 2:
                if pieza.startswith("pierna"):
                    c = hierro_t(cara, x, y, w, h)
                    if y >= h - 1:
                        return BOTA[1]
                    return c                                             # las grebas
                return malla(cara, x, y, w, h)
            c = calza_t(cara, x, y, w, h)
            if pieza.startswith("pierna") and y >= h - 3:
                return BOTA[(x + y) % 3] if y < h - 1 else BOTA[0]       # las botas
            if pieza.startswith("pierna") and y == h - 4:
                return CUERO[3]                                          # la vuelta de la bota
            return c
        return f

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if variante == 0:
            c = ocre_t(cara, x, y, w, h)
            if y == h - 1 and (x + CARAS[cara]) % 2 == 0:
                return None                                              # el bajo, en jirones
            if y >= h - 2:
                return oscuro(c, 0.25)
            return c
        if variante == 1:
            c = cuero_t(cara, x, y, w, h)
            return oscuro(c, 0.2) if y >= h - 1 else c
        c = malla(cara, x, y, w, h)
        return oscuro(c, 0.15) if y >= h - 1 else c

    def cinto(cara, x, y, w, h):
        if cara == "frente" and x in (3, 4, 5):
            return LATON[3] if y == 0 else LATON[2]                      # la hebilla
        return "#2a1a10" if (x + y) % 3 else "#3a2616"

    # ---------------------------------------------------- el capacete y el almofar
    def ala_capacete(cara, x, y, w, h):
        c = oxidado(hierro_t, 7711, 0.12)(cara, x, y, w, h)
        if cara == "abajo":
            return oscuro(c, 0.35)
        if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)):
            return claro(c, 0.15)                                        # el canto doblado
        return c

    def copa_capacete(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = oxidado(hierro_t, 7712, 0.1)(cara, x, y, w, h)
        if cara in ("frente", "espalda", "der", "izq") and y == h - 1 and x % 3 == 1:
            return LATON[1]                                              # los remaches
        if cara == "frente" and (x == 2 and y == 1):
            return claro(c, 0.3)                                         # una abolladura que brilla
        return c

    def cresta(cara, x, y, w, h):
        return claro(hierro_t(cara, x, y, w, h), 0.1)

    def almofar(cara, x, y, w, h):
        c = malla(cara, x, y, w, h)
        if y >= h - 1:
            return oscuro(c, 0.2)
        return c

    # ---------------------------------------------------- la lanza y el cuerno
    def asta(cara, x, y, w, h):
        c = madera_t(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo"):
            k = x if cara in ("arriba", "abajo") else x
            if 22 <= k <= 26:
                return CUERO[3] if (k + y) % 2 else CUERO[2]             # el agarre de cuero
        return c

    def punta_lanza(cara, x, y, w, h):
        c = acero_t(cara, x, y, w, h)
        if cara in ("arriba", "abajo") and (x == 0 or x == w - 1):
            return claro(c, 0.25)                                        # el filo
        return c

    def virola(cara, x, y, w, h):
        return LATON[2] if (x + y) % 2 else LATON[1]

    def banderola(cara, x, y, w, h):
        # la banderola de la Marca: ocre con una franja negra, deshilachada
        if cara in ("arriba", "abajo"):
            return None
        if y == h - 1 and x % 2:
            return None
        if y in (1, 2):
            return TORRE
        return OCRE[3] if (x + y) % 3 else OCRE[2]

    def cuerno_boquilla(cara, x, y, w, h):
        return LATON[3] if (x + y) % 2 else LATON[2]

    def cuerno_cuerpo(cara, x, y, w, h):
        c = ruido(MARFIL, 7713, grano=0.3, base=0.5)(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo") and (x if cara in ("arriba", "abajo") else x) % 3 == 0:
            return oscuro(c, 0.2)                                        # las vetas del cuerno
        return c

    def cuerno_pabellon(cara, x, y, w, h):
        if cara == "espalda":
            return "#1a140e" if 0 < x < w - 1 and 0 < y < h - 1 else LATON[2]    # la boca del cuerno
        c = ruido(MARFIL, 7714, grano=0.3, base=0.55)(cara, x, y, w, h)
        return LATON[2] if (cara in ("der", "izq", "arriba", "abajo") and x == w - 1) else c

    # ---------------------------------------------------- la ballesta, el paves, el carcaj y el torno
    def curena(cara, x, y, w, h):
        c = madera(MADERA, 7715, "z")(cara, x, y, w, h)
        if cara in ("arriba",) and 7 <= y <= 9:
            return LATON[2]                                              # la nuez
        if cara in ("der", "izq") and 14 <= x <= 16:
            return HIERRO[2]                                             # el gatillo
        return c

    def arco_ballesta(cara, x, y, w, h):
        c = acero_t(cara, x, y, w, h)
        if x in (0, w - 1):
            return HIERRO[1]
        if w // 2 - 1 <= x <= w // 2:
            return CUERDA[0]                                             # las ataduras al centro
        return c

    def cuerda(cara, x, y, w, h):
        return CUERDA[(x // 2) % 2]

    def estribo(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if cara == "frente" and 0 < x < w - 1 and 0 < y < h - 1:
            return None                                                  # el aro abierto
        return c

    def virote(cara, x, y, w, h):
        if cara in ("der", "izq", "arriba", "abajo"):
            if x <= 1:
                return ACERO[3]                                          # la punta
            if x >= w - 2:
                return PLUMA[0] if (x + y) % 2 else PLUMA[1]             # las plumas
        return MADERA_C[2]

    def paves(cara, x, y, w, h):
        if cara == "espalda":
            # la cara de fuera: ocre con la torre negra y el borde de hierro
            if x in (0, w - 1) or y in (0, h - 1):
                return HIERRO[2] if (x + y) % 2 else HIERRO[1]
            t = dibujo([
                "..........",
                "..........",
                "...T.TT.T.",
                "...TTTTTT.",
                "....TTTT..",
                "....TTTT..",
                "....TTTT..",
                "....T..T..",
                "...TTTTTT.",
                "..........",
            ], {"T": TORRE})(cara, x, (y - 2) * 10 // max(1, h - 4) if 2 <= y < h - 2 else 0, 10, 10)
            if 2 <= y < h - 2 and t:
                return t
            c = ocre_t(cara, x, y, w, h)
            return oscuro(c, 0.15) if (x * 5 + y * 3) % 17 == 0 else c   # golpes y desconchones
        if cara == "frente":
            return madera_c(cara, x, y, w, h) if (x + y) % 5 else CUERO[2]    # por dentro, las tablas y las correas
        return HIERRO[1]

    def carcaj(cara, x, y, w, h):
        c = cuero_t(cara, x, y, w, h)
        return LATON[1] if y == 0 else c

    def plumas(cara, x, y, w, h):
        if cara == "abajo":
            return None
        return PLUMA[(x + y) % 2] if (x + y) % 3 else MADERA_C[1]

    def torno(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if cara in ("frente", "espalda") and (x + y) % 2 == 0:
            return oscuro(c, 0.25)                                       # los dientes del engranaje
        return c

    def manivela(cara, x, y, w, h):
        return MADERA_C[2] if x > 0 else HIERRO[2]

    # ---------------------------------------------------- el escudero: el bacinete, el peto, las hombreras, el escudo y la maza
    def bacinete(cara, x, y, w, h):
        c = oxidado(hierro_t, 7716, 0.08)(cara, x, y, w, h)
        if cara == "frente":
            if y in (3, 4) and 1 <= x <= 7:
                return "#0b0b0d"                                         # la rendija de la vista
            if y >= 6 and (x + y) % 2 == 0 and 2 <= x <= 6:
                return oscuro(c, 0.45)                                   # los agujeros de respirar
            if x == 4 and y < 3:
                return claro(c, 0.15)                                    # la arista
        if cara == "abajo":
            return None
        if cara in ("der", "izq") and y == 5 and x % 3 == 1:
            return LATON[2]                                              # los remaches del gozne
        return c

    def peto(cara, x, y, w, h):
        c = oxidado(hierro_t, 7717, 0.07)(cara, x, y, w, h)
        if cara == "frente":
            if x in (4, 5) and y < h - 1:
                return claro(c, 0.18)                                    # la arista del peto
            if 3 <= x <= 6 and 2 <= y <= 6:
                t = dibujo(["T.TT", "TTTT", ".TT.", ".TT.", "TTTT"], {"T": TORRE})(cara, x - 3, y - 2, 4, 5)
                if t:
                    return mezcla(t, c, 0.35)                            # la torre de la Marca, grabada y medio borrada
        if cara == "abajo" or cara == "arriba":
            return None if cara == "abajo" else oscuro(c, 0.1)
        return c

    def faja(cara, x, y, w, h):
        c = ocre_t(cara, x, y, w, h)
        return oscuro(c, 0.2) if y == h - 1 else c

    def hombrera(cara, x, y, w, h):
        c = oxidado(hierro_t, 7718, 0.08)(cara, x, y, w, h)
        if cara != "arriba" and y == h - 1:
            return claro(c, 0.15)                                        # el canto
        if cara == "abajo":
            return None
        return c

    def brazal(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if y in (0, h - 1):
            return claro(c, 0.12)
        if cara in ("arriba", "abajo"):
            return None
        return c

    def escudo(cara, x, y, w, h):
        if cara == "frente":
            # la cara de fuera del escudo de torre: ocre con la torre negra, borde de hierro con clavos
            if x in (0, w - 1) or y in (0, h - 1):
                return HIERRO[2] if (x + y) % 3 else LATON[1]
            if x in (1, w - 2) or y in (1, h - 2):
                return HIERRO[1]
            t = dibujo([
                "............",
                "............",
                "....T.TT.T..",
                "....TTTTTT..",
                ".....TTTT...",
                ".....TTTT...",
                ".....TTTT...",
                ".....TTTT...",
                ".....T..T...",
                "....TTTTTT..",
                "............",
            ], {"T": TORRE})(cara, x, (y - 2) * 11 // max(1, h - 4) if 2 <= y < h - 2 else 0, 12, 11)
            if 2 <= y < h - 2 and t:
                return t
            c = ocre_t(cara, x, y, w, h)
            if (x * 7 + y * 5) % 19 == 0:
                return oscuro(c, 0.3)                                    # tajos y desconchones
            if (x * 3 + y * 11) % 29 == 0:
                return SANGRE[1]
            return c
        if cara == "espalda":
            return madera_c(cara, x, y, w, h) if (y % 6) else CUERO[2]   # por dentro, las tablas y las correas
        return HIERRO[2] if (x + y) % 2 else HIERRO[1]                   # el canto, de hierro

    def abrazadera(cara, x, y, w, h):
        return CUERO[3] if (x + y) % 2 else CUERO[2]

    def mango_maza(cara, x, y, w, h):
        c = madera_t(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo") and x >= 10:
            return CUERO[2] if (x + y) % 2 else CUERO[3]                 # el puno
        return c

    def cabeza_maza(cara, x, y, w, h):
        return oxidado(hierro_t, 7719, 0.1)(cara, x, y, w, h)

    def pestana(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if x in (0, w - 1):
            return claro(c, 0.18)
        return c

    def pincho(cara, x, y, w, h):
        return ACERO[3]

    mats = {
        "cabeza": sombra(cabeza, abajo=0.12),
        "torso": sombra(torso, abajo=0.16),
        "brazo_d": sombra(brazo("brazo_d"), abajo=0.1), "brazo_i": sombra(brazo("brazo_i"), abajo=0.1),
        "antebrazo_d": sombra(brazo("antebrazo_d"), abajo=0.14), "antebrazo_i": sombra(brazo("antebrazo_i"), abajo=0.14),
        "muslo_d": sombra(pierna("muslo_d"), abajo=0.1), "muslo_i": sombra(pierna("muslo_i"), abajo=0.1),
        "pierna_d": sombra(pierna("pierna_d"), abajo=0.16), "pierna_i": sombra(pierna("pierna_i"), abajo=0.16),
        "faldon_d": sombra(faldon, abajo=0.12), "faldon_i": sombra(faldon, abajo=0.12), "cinto": cinto,
        "ala_capacete": ala_capacete, "copa_capacete": sombra(copa_capacete, abajo=0.1), "cresta": cresta,
        "almofar_lado": sombra(almofar, abajo=0.12), "almofar_nuca": sombra(almofar, abajo=0.12), "almofar_barba": almofar,
        "asta": asta, "punta_lanza": punta_lanza, "virola": virola, "banderola": banderola,
        "cuerno_boquilla": cuerno_boquilla, "cuerno_cuerpo": cuerno_cuerpo, "cuerno_pabellon": cuerno_pabellon,
        "curena": curena, "arco_ballesta": arco_ballesta, "cuerda": cuerda, "estribo": estribo, "virote": virote,
        "paves": sombra(paves, abajo=0.12), "carcaj": sombra(carcaj, abajo=0.12), "plumas": plumas, "torno": torno, "manivela": manivela,
        "bacinete": sombra(bacinete, abajo=0.1), "peto": sombra(peto, abajo=0.1), "faja": faja, "hombrera": sombra(hombrera, abajo=0.1),
        "brazal": brazal, "escudo": sombra(escudo, abajo=0.1), "abrazadera": abrazadera,
        "mango_maza": mango_maza, "cabeza_maza": cabeza_maza, "pestana": pestana, "pincho": pincho,
    }
    return mats


def brillo(variante):
    """Lo que luce: las pupilas de los ojos que no se cierran (al escudero, por la rendija del bacinete)."""
    b = {}
    if variante in (0, 1):
        b["cabeza"] = por_cara(liso(None), frente=dibujo([
            "........",
            "........",
            "........",
            "..P..P..",
        ], {"P": PUPILA_B}))
    if variante == 2:
        b["bacinete"] = por_cara(liso(None), frente=dibujo([
            "........",
            "........",
            "........",
            "..P..P..",
            "..p..p..",
        ], {"P": PUPILA_B, "p": PUPILA}))
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos 9,5).
# _arma = hacia donde apunta lo que lleva en la derecha (a -Z de la mano): la lanza, la ballesta o la maza.

# de pie, firmes pero agotados: los hombros caidos, la cabeza un poco adelantada
BASE = dict(raiz=raiz(0.0), torso=(4, 0, 0), cabeza=(-2, 0, 0),
            muslo_d=(0, 0, 2), pierna_d=(2, 0, 0), muslo_i=(0, 0, -2), pierna_i=(2, 0, 0),
            brazo_i=(-2, 0, -5), antebrazo_i=(-8, 0, 0), brazo_d=(-2, 0, 5), antebrazo_d=(-8, 0, 0))
PORTE = {
    # la lanza de pie, apoyada en el suelo a su derecha (la punta arriba)
    0: dict(_mano_d=("h", -0.6, -7.0, 2.6), _arma=(0.0, 0.96, -0.12), brazo_i=(-4, 0, -6), antebrazo_i=(-10, 0, 0)),
    # la ballesta cruzada por delante, la punta abajo a su izquierda
    1: dict(_mano_d=("h", 1.8, -6.6, 3.6), _arma=(0.55, -0.55, 0.62), brazo_i=(-40, 0, -6), antebrazo_i=(-58, 0, 0)),
    # el escudo a su izquierda (en el brazo), la maza colgando
    2: dict(_mano_d=("h", -0.4, -8.4, 2.0), _arma=(0.05, -0.7, 0.7), brazo_i=(-14, 0, -4), antebrazo_i=(-18, 0, 0)),
}
GUARDIA = dict(raiz=raiz(-0.8), torso=(12, 6, 0), cabeza=(-8, -6, 0),
               muslo_d=(-14, 0, 6), pierna_d=(22, 0, 0), muslo_i=(4, 0, -6), pierna_i=(14, 0, 0))
LISTO = {
    # la lanza baja, apuntando adelante, las dos manos
    0: con(GUARDIA, _mano_d=("h", 1.6, -4.0, 4.8), _arma=(0.12, 0.12, 0.98), brazo_i=(-56, 0, 18), antebrazo_i=(-34, 0, 0)),
    # la ballesta al pecho, la punta adelante y abajo
    1: con(GUARDIA, _mano_d=("h", 2.0, -3.6, 5.2), _arma=(0.1, -0.25, 0.96), brazo_i=(-62, 0, 14), antebrazo_i=(-40, 0, 0)),
    # el escudo por delante (el brazo izquierdo adelantado y hacia el centro), la maza alzada detras
    2: con(GUARDIA, torso=(10, 14, 0), cabeza=(-6, -14, 0), brazo_i=(-38, 0, 16), antebrazo_i=(-26, 0, 0),
           _mano_d=("h", -1.2, 6.4, -3.4), _arma=(0.1, 0.94, 0.3)),
}
# de guardia (REZA): firme, la cabeza se va a un lado y a otro sin prisa (los ojos no parpadean)
VIGILANDO = con(BASE, raiz=raiz(0.2), torso=(0, 0, 0), cabeza=(-4, 0, 0))
# sentado en el suelo (TUMBADO del ballestero): la espalda contra algo, las rodillas arriba, los ojos abiertos
SENTADO = dict(raiz=raiz(-9.6, 1.2), torso=(-8, 0, 0), cabeza=(10, 0, 0),
               muslo_d=(-80, 0, 8), pierna_d=(86, 0, 0), muslo_i=(-74, 0, -6), pierna_i=(80, 0, 0),
               brazo_d=(-30, 0, 18), antebrazo_d=(-50, 0, 0), brazo_i=(-36, 0, -16), antebrazo_i=(-40, 0, 0))
RODILLAS = dict(raiz=raiz(-6.0, 0.8), torso=(20, 0, 0), cabeza=(24, 0, 0), muslo_d=(-10, 0, 5), pierna_d=(96, 0, 0),
                muslo_i=(-8, 0, -5), pierna_i=(96, 0, 0), brazo_d=(-6, 0, 14), antebrazo_d=(-10, 0, 0), brazo_i=(-6, 0, -14),
                antebrazo_i=(-10, 0, 0))
# de costado en el suelo, como dormido (al morir: por fin duerme)
DORMIDO = dict(raiz=raiz(1.6, 0.0, 6.0, rot=(0, 0, 84)), torso=(10, 0, 0), cabeza=(14, 0, -10),
               muslo_d=(-30, 0, 4), pierna_d=(50, 0, 0), muslo_i=(-40, 0, -4), pierna_i=(60, 0, 0),
               brazo_d=(-40, 0, 10), antebrazo_d=(-30, 0, 0), brazo_i=(-20, 0, -20), antebrazo_i=(-40, 0, 0))
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

    # ---- quieto: respira hondo, cansado; de vez en cuando se le va la cabeza y la levanta de golpe (no se deja dormir)
    bucle("quieto", 4.4, [
        (0.0, BASE),
        (1.4, con(BASE, raiz=raiz(-0.3), torso=(7, 0, 0), cabeza=(6, 0, 0))),
        (2.4, con(BASE, raiz=raiz(-0.5), torso=(9, 0, 1), cabeza=(18, 0, 2))),
        (2.6, con(BASE, raiz=raiz(0.1), torso=(2, 0, 0), cabeza=(-8, 0, 0))),
        (3.4, con(BASE, torso=(4, -2, 0), cabeza=(-2, 6, 0))),
        (4.4, BASE),
    ])
    # ---- andar: el paso de un soldado agotado
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-22, 20, 6, 14, 4, 0.0), (-6, 4, 6, 28, 0, 0.5), (20, -22, 14, 6, -4, 0.0), (4, -6, 28, 6, 0, 0.5), (-22, 20, 6, 14, 4, 0.0))):
        pasos.append((1.1 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, -ty * 0.3), "cabeza": (-2, -ty * 0.8, ty * 0.3), "raiz": raiz(up)}))
    bucle("andar", 1.1, pasos)
    for v in range(3):
        q = fija(con(BASE, PORTE[v]))
        bucle(f"porte_{VARIANTES[v]}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
        l0 = fija(LISTO[v])
        l1 = fija(con(LISTO[v], raiz=raiz(LISTO[v]["raiz"]["pos"][1] - 0.6), torso=(LISTO[v]["torso"][0] + 3, LISTO[v]["torso"][1] - 3, 0)))
        bucle(f"listo_{VARIANTES[v]}", 1.8, [(0.0, l0), (0.9, l1), (1.8, l0)])
    # ---- de guardia (REZA): mira a un lado... al otro... de golpe hacia delante (el porte de cada uno lo pone el de Java)
    vi = [fija(con(VIGILANDO, cabeza=(c[0], c[1], 0), torso=(0, c[2], 0))) for c in
          ((-4, 0, 0), (-6, 38, 8), (-6, 40, 8), (-2, -34, -8), (-2, -36, -8), (-8, 0, 0))]
    bucle("vigilando", 6.0, [(0.0, vi[0]), (1.2, vi[1]), (2.2, vi[2]), (3.4, vi[3]), (4.4, vi[4]), (4.7, vi[5]), (6.0, vi[0])])
    # ---- sentado (TUMBADO): las rodillas arriba, la cabeza se le cae y la levanta, los ojos abiertos
    se = fija(SENTADO)
    bucle("sentado", 5.0, [(0.0, se), (2.6, fija(con(SENTADO, cabeza=(30, 0, 4)))), (2.8, fija(con(SENTADO, cabeza=(-6, 0, 0)))),
                           (5.0, se)])
    # ---- apoyado en el escudo (REZA del escudero): el escudo plantado delante, las dos manos encima
    AP = con(BASE, raiz=raiz(-0.4), torso=(14, 0, 0), cabeza=(-6, 0, 0), brazo_i=(-30, 0, 10), antebrazo_i=(-20, 0, 0),
             _mano_d=("h", 2.6, -4.6, 5.6), _arma=(0.1, -0.9, 0.3))
    ap0, ap1 = fija(AP), fija(con(AP, cabeza=(-6, 30, 0), torso=(14, 6, 0)))
    bucle("apoyado", 5.0, [(0.0, ap0), (2.0, ap1), (3.0, ap1), (5.0, ap0)])

    # ================================================================ acciones
    L0, L1, L2 = LISTO[0], LISTO[1], LISTO[2]

    # ---- 1 estocada (el centinela): echa atras la lanza y la lanza a fondo
    ES_ATRAS = con(L0, raiz=raiz(-0.6, 1.6), torso=(4, 26, 0), cabeza=(-6, -22, 0), _mano_d=("h", -1.0, -1.6, -1.6), _arma=(0.1, 0.08, 1.0),
                   brazo_i=(-50, 0, 20), antebrazo_i=(-44, 0, 0))
    ES_FONDO = con(L0, raiz=raiz(-2.4, -2.4), torso=(24, -10, 0), cabeza=(-18, 8, 0), muslo_d=(-46, 0, 6), pierna_d=(46, 0, 0),
                   muslo_i=(14, 0, -6), pierna_i=(20, 0, 0), _mano_d=("h", 3.0, -2.2, 9.0), _arma=(0.05, 0.04, 1.0),
                   brazo_i=(-80, 0, 10), antebrazo_i=(-10, 0, 0))
    accion("estocada", 1.2, [
        (0.00, L0),
        (0.40, ES_ATRAS),
        (0.54, con(ES_ATRAS, torso=(4, 30, 0))),
        (0.62, ES_FONDO),
        (0.86, con(ES_FONDO, torso=(22, -10, 0))),
        (1.20, L0),
    ], golpes=(0.62,))
    # ---- 2 barrido (el centinela): la lanza de derecha a izquierda, a la altura de las rodillas
    BA_D = con(L0, raiz=raiz(-1.0, 0.6), torso=(10, 46, 0), cabeza=(-6, -40, 0), _mano_d=("h", -2.4, -3.6, 3.0), _arma=(-1, -0.2, 0.2))
    BA_F = con(L0, raiz=raiz(-2.0, -0.4), torso=(16, 6, 0), cabeza=(-10, -6, 0), _mano_d=("h", 2.0, -4.6, 7.0), _arma=(0.1, -0.3, 1))
    BA_I = con(L0, raiz=raiz(-2.2, -1.0), torso=(18, -40, 0), cabeza=(-10, 34, 0), muslo_i=(-26, 0, -6), pierna_i=(24, 0, 0),
               _mano_d=("h", 5.4, -4.0, 4.0), _arma=(1, -0.25, 0.3))
    accion("barrido", 1.3, [
        (0.00, L0),
        (0.44, BA_D),
        (0.62, BA_F),
        (0.70, BA_I),
        (0.98, con(BA_I, torso=(18, -44, 0))),
        (1.30, L0),
    ], golpes=(0.66,))
    # ---- 3 cuerno (el centinela): se lleva el cuerno a la boca con la izquierda, se echa atras y toca; la lanza en la derecha
    CU_SUBE = con(L0, raiz=raiz(-0.4), torso=(-4, 0, 0), cabeza=(-10, 0, 0), _mano_d=("h", -0.6, -6.4, 2.6), _arma=(0.0, 0.96, -0.12),
                  _mano_i=("h", -2.4, 6.2, 4.6), _arma_i=(-0.2, 0.3, 0.93))
    CU_TOCA = con(CU_SUBE, raiz=raiz(0.2), torso=(-16, 0, 0), cabeza=(-30, 0, 0), _mano_i=("h", -2.6, 7.6, 3.4), _arma_i=(-0.15, 0.62, 0.77))
    accion("cuerno", 2.0, [
        (0.00, L0),
        (0.40, CU_SUBE),
        (0.60, CU_TOCA),
        (1.10, con(CU_TOCA, torso=(-18, 4, 0), cabeza=(-32, 4, 0))),
        (1.60, con(CU_TOCA, torso=(-14, -4, 0), cabeza=(-28, -4, 0))),
        (1.75, CU_SUBE),
        (2.00, L0),
    ], golpes=(1.6,))
    # ---- 4 disparo (el ballestero): se echa la ballesta a la cara, apunta y suelta (el culatazo del retroceso)
    DI_APUNTA = con(L1, raiz=raiz(-1.0), torso=(4, 20, 0), cabeza=(2, -18, 0), muslo_d=(-20, 0, 6), pierna_d=(22, 0, 0),
                    _mano_d=("h", 3.4, 2.4, 6.4), _arma=(0.12, 0.0, 0.99), brazo_i=(-92, 0, 22), antebrazo_i=(-16, 0, 0))
    DI_SUELTA = con(DI_APUNTA, raiz=raiz(-1.0, 0.8), torso=(-2, 22, 0), cabeza=(-4, -18, 0), _mano_d=("h", 3.2, 2.9, 5.2),
                    _arma=(0.12, 0.18, 0.98))
    accion("disparo", 1.0, [
        (0.00, L1),
        (0.30, DI_APUNTA),
        (0.62, DI_APUNTA),
        (0.66, DI_SUELTA),
        (0.82, con(DI_APUNTA, torso=(2, 20, 0))),
        (1.00, L1),
    ], golpes=(0.65,))
    # ---- 5 recarga (el ballestero): la ballesta de punta al suelo, el pie en el estribo y la manivela del torno
    RE_ABAJO = con(L1, raiz=raiz(-2.0, 1.0), torso=(40, 0, 0), cabeza=(10, 0, 0), muslo_d=(-40, 0, 6), pierna_d=(40, 0, 0),
                   _mano_d=("h", 0.8, -8.2, 6.0), _arma=(0.0, -0.98, 0.2), brazo_i=(-60, 0, 10), antebrazo_i=(-30, 0, 0))
    manivela = [con(RE_ABAJO, brazo_i=(-60 + d, 0, 10), antebrazo_i=(-30 - d, 0, 0)) for d in (0, 30, 50, 30)]
    accion("recarga", 2.4, [
        (0.00, L1),
        (0.35, RE_ABAJO),
        (0.60, manivela[1]),
        (0.80, manivela[2]),
        (1.00, manivela[3]),
        (1.20, manivela[0]),
        (1.40, manivela[1]),
        (1.60, manivela[2]),
        (1.80, manivela[3]),
        (2.00, RE_ABAJO),
        (2.40, L1),
    ], golpes=(2.2,))
    # ---- 6 culatazo (el ballestero): de cerca, la culata de la ballesta a la cara
    CA_ATRAS = con(L1, raiz=raiz(-0.6, 1.2), torso=(0, -30, 0), cabeza=(-6, 24, 0), _mano_d=("h", -1.6, -1.0, -1.0), _arma=(0.3, 0.6, -0.74))
    CA_DA = con(L1, raiz=raiz(-2.0, -1.6), torso=(22, 26, 0), cabeza=(-14, -20, 0), muslo_d=(-36, 0, 6), pierna_d=(36, 0, 0),
                _mano_d=("h", 3.2, 0.6, 7.0), _arma=(-0.2, 0.5, -0.84))
    accion("culatazo", 1.1, [
        (0.00, L1),
        (0.36, CA_ATRAS),
        (0.50, CA_DA),
        (0.76, con(CA_DA, torso=(20, 24, 0))),
        (1.10, L1),
    ], golpes=(0.52,))
    # ---- 7 mazazo (el escudero): la maza arriba por detras del hombro y abajo; el escudo a un lado
    MZ_ALTO = con(L2, raiz=raiz(0.0, 1.4), torso=(-12, 24, 0), cabeza=(-14, -14, 0), _mano_d=("h", -0.8, 7.6, -1.6), _arma=(0.05, 0.6, -0.8),
                  brazo_i=(-30, 0, 20), antebrazo_i=(-30, 0, 0))
    MZ_BAJO = con(L2, raiz=raiz(-2.6, -2.0), torso=(38, -6, 0), cabeza=(-24, 6, 0), muslo_d=(-42, 0, 4), pierna_d=(40, 0, 0),
                  _mano_d=("h", 2.2, -2.6, 7.4), _arma=(0.05, -0.62, 0.78), brazo_i=(-20, 0, 26), antebrazo_i=(-20, 0, 0))
    accion("mazazo", 1.5, [
        (0.00, L2),
        (0.55, MZ_ALTO),
        (0.74, con(MZ_ALTO, torso=(-14, 26, 0))),
        (0.82, con(L2, torso=(10, 10, 0), _mano_d=("h", 1.6, 3.6, 6.8), _arma=(0.05, 0.4, 0.9))),
        (0.86, MZ_BAJO),
        (1.10, con(MZ_BAJO, torso=(36, -6, 0))),
        (1.50, L2),
    ], golpes=(0.86,))
    # ---- 8 escudazo (el escudero): el escudo atras y adelante de golpe, con el hombro
    EZ_ATRAS = con(L2, raiz=raiz(-0.8, 1.4), torso=(6, -30, 0), cabeza=(-8, 26, 0), brazo_i=(-20, 0, 6), antebrazo_i=(-16, 0, 0))
    EZ_DA = con(L2, raiz=raiz(-2.2, -2.2), torso=(18, 30, 0), cabeza=(-14, -24, 0), muslo_i=(-40, 0, -6), pierna_i=(40, 0, 0),
                brazo_i=(-70, 0, 26), antebrazo_i=(-10, 0, 0))
    accion("escudazo", 1.1, [
        (0.00, L2),
        (0.36, EZ_ATRAS),
        (0.52, EZ_DA),
        (0.80, con(EZ_DA, torso=(16, 26, 0))),
        (1.10, L2),
    ], golpes=(0.54,))
    # ---- 9 carga (el escudero): se agacha tras el escudo y arranca a correr; el golpe al llegar
    CG_A = con(L2, raiz=raiz(-2.4, 1.0), torso=(32, 12, 0), cabeza=(-24, -12, 0), muslo_d=(-40, 0, 6), pierna_d=(50, 0, 0),
               muslo_i=(14, 0, -6), pierna_i=(26, 0, 0), brazo_i=(-56, 0, 22), antebrazo_i=(-14, 0, 0))
    CG_B = con(CG_A, muslo_d=(18, 0, 6), pierna_d=(26, 0, 0), muslo_i=(-46, 0, -6), pierna_i=(50, 0, 0), raiz=raiz(-1.8, 1.0))
    accion("carga", 1.8, [
        (0.00, L2),
        (0.30, CG_A),
        (0.45, CG_B),
        (0.60, CG_A),
        (0.75, CG_B),
        (0.90, CG_A),
        (1.05, CG_B),
        (1.12, con(CG_A, torso=(26, 20, 0), brazo_i=(-74, 0, 26), antebrazo_i=(-6, 0, 0))),
        (1.40, con(L2, raiz=raiz(-1.6, -0.6), torso=(20, 10, 0))),
        (1.80, L2),
    ], golpes=(1.1,))
    # ---- 10 bloquea (el escudero): le dan en el escudo: se le va el brazo atras y lo vuelve a poner
    accion("bloquea", 0.6, [
        (0.00, L2),
        (0.10, con(L2, raiz=raiz(-0.8, 1.2), torso=(4, 10, 0), brazo_i=(-26, 0, 12), antebrazo_i=(-12, 0, 0))),
        (0.60, L2),
    ])
    # ---- 11 aturdido: se tambalea
    AT = dict(raiz=raiz(-1.0, 2.0), torso=(-18, 12, 10), cabeza=(-22, -16, 18),
              muslo_d=(-14, 0, 10), pierna_d=(28, 0, 0), muslo_i=(18, 0, -10), pierna_i=(20, 0, 0),
              brazo_d=(16, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -30), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-6, 8, -12), cabeza=(-18, -10, -16), raiz=raiz(-1.8, 2.4))),
        (1.05, con(AT, torso=(-10, -8, 10), cabeza=(-20, 10, 14), raiz=raiz(-1.6, 2.0))),
        (1.50, con(AT, torso=(10, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.0, 0.6))),
    ])
    # ---- 12 morir: de rodillas, se le cierran los ojos (el brillo se apaga) y se tumba de costado: por fin duerme
    accion("morir", 2.2, [
        (0.00, con(BASE, torso=(-10, 0, 6), cabeza=(-26, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0))),
        (0.50, RODILLAS),
        (1.10, con(RODILLAS, torso=(30, 0, 10), cabeza=(30, 0, 14))),
        (1.60, DORMIDO),
        (2.20, con(DORMIDO, cabeza=(18, 0, -12))),
    ])
    # ---- 13 levantarse: estaba sentado en el suelo; se pone en pie (despacio: le pesa todo)
    accion("levantarse", 1.4, [
        (0.00, SENTADO),
        (0.40, con(SENTADO, raiz=raiz(-9.2, 0.6), torso=(30, 0, 0), cabeza=(-10, -16, 0))),
        (0.85, con(GUARDIA, raiz=raiz(-6.0, 0.4), torso=(40, 0, 0), muslo_d=(-90, 0, 6), pierna_d=(110, 0, 0), muslo_i=(-60, 0, -6),
                   pierna_i=(100, 0, 0))),
        (1.15, con(GUARDIA, raiz=raiz(-2.4), muslo_d=(-46, 0, 6), pierna_d=(64, 0, 0))),
        (1.40, GUARDIA),
    ])
    # ---- 14 volverse: estaba de guardia; se vuelve de golpe (el giro lo pone el servidor)
    accion("volverse", 0.9, [
        (0.00, VIGILANDO),
        (0.25, con(VIGILANDO, torso=(6, 30, 0), cabeza=(-8, -30, 0))),
        (0.55, GUARDIA),
        (0.90, GUARDIA),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "ballestero" in nombre or nombre in ("disparo", "recarga", "culatazo", "sentado", "levantarse"):
        return 1
    if "escudero" in nombre or nombre in ("mazazo", "escudazo", "carga", "bloquea", "apoyado"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_centinela"]]
    if nombre == "vigilando":
        return [anims["porte_centinela"]]
    return []
