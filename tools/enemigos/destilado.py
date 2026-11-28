"""
Los Destilados del Lazareto de Maese Galen (parte 7au): cuando la Peste Gris llego al Pantano Podrido, Maese Galen, el fisico,
levanto su lazareto en el islote de la laguna y busco el elixir que la curase. Lo que destilo no curaba: les cambiaba. A los
enfermos se les volvio la piel gris y les salieron bubas de cristal verde; a sus mancebos, que bebian lo mismo para no
enfermar, el elixir les pego la mascara a la cara; y a los que Galen uso de cuba viva para su destilado, la cuba a la espalda.
Tres:

  0 mancebo: el abrigo encerado de los fisicos de la peste, el sombrero de ala ancha y la mascara de pico con sus dos lentes
    de cristal verde (lucen); en la derecha la mano de mortero (de piedra) y en la izquierda un frasco. Lanza frascos en arco
    (de fuego, de acido y uno verde que cura a los suyos) y de cerca pega con el mortero (de arriba abajo y de lado). Espera
    inclinado sobre su mesa, moliendo (REZA).
  1 enfermo: la piel gris, la bata sucia, las vendas y las bubas de cristal verde (lucen). Zarpazo, se abalanza y escupe lo
    que lleva dentro. Espera sentado en el suelo, encogido (REZA), o tendido boca arriba como un enfermo mas (TUMBADO).
  2 cuba: un enfermo grande (el dibujante lo agranda) con una cuba de cristal llena de destilado a la espalda (luce), los
    tubos que le entran por los hombros, el mandil de cuero, unos guantes enormes y las gafas de cristal verde. Punetazo,
    embestida y derrame (se inclina y la cuba le vierte el acido por delante). Espera de pie, jadeando (REZA).
"""
from anim import Anim, con, raiz, resolver, sin  # noqa: F401
from modelo import Box, Rig, humanoide
from pinta import (claro, delante, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "destilado"
VARIANTES = ("mancebo", "enfermo", "cuba")
ESCALA = 1.0

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 5), cabeza=(8, 8, 8), brazo=(3, 13, 3), pierna=(4, 12, 4), hombro=0.6)
    # ---- 0, el mancebo: el sombrero, la mascara de pico y sus lentes, la capelina, el cinto de los frascos, el zurron, los
    # faldones del abrigo, la mano de mortero y el frasco de la izquierda
    r.pieza("sombrero", "cabeza", (0, 0, 0), boxes=[
        Box(-6.5, -7.7, -6.5, 13.0, 0.8, 13.0, "ala_sombrero"),
        Box(-4.3, -11.4, -4.3, 8.6, 3.8, 8.6, "copa_sombrero"),
        Box(-4.45, -8.5, -4.45, 8.9, 0.9, 8.9, "cinta_sombrero"),
    ])
    r.pieza("pico", "cabeza", (0, -3.0, -4.0), rot=(16, 0, 0), boxes=[
        Box(-1.6, -1.6, -6.4, 3.2, 3.0, 6.6, "pico"),
        Box(-0.9, -0.9, -9.2, 1.8, 2.0, 2.9, "pico_punta"),
    ])
    r.pieza("lentes", "cabeza", (0, 0, 0), boxes=[
        Box(-3.5, -6.0, -4.5, 2.5, 2.5, 0.6, "lente"),
        Box(1.0, -6.0, -4.5, 2.5, 2.5, 0.6, "lente"),
    ])
    r.pieza("capelina", "torso", (0, 0, 0), boxes=[Box(-4.9, -12.5, -3.1, 9.8, 4.6, 6.2, "capelina")])
    r.pieza("cinturon", "torso", (0, 0, 0), boxes=[
        Box(-4.4, -2.2, -2.9, 8.8, 1.4, 5.8, "cinturon"),
        Box(-3.4, -1.6, -3.7, 1.2, 2.2, 1.2, "frasquito_rojo"),
        Box(-1.7, -1.6, -3.7, 1.2, 2.2, 1.2, "frasquito_verde"),
        Box(2.3, -1.6, -3.7, 1.2, 2.2, 1.2, "frasquito_ambar"),
    ])
    r.pieza("zurron", "torso", (0, 0, 0), boxes=[Box(3.7, -3.8, -1.6, 2.2, 3.4, 3.2, "zurron")])
    for n in ("d", "i"):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.4, -0.6, -2.6, 4.8, 10.4, 5.2, "faldon_abrigo")])
    r.pieza("mortero", "mano_d", (0, 0, 0), boxes=[
        Box(-0.7, -0.7, -9.4, 1.4, 1.4, 11.0, "mortero_mango"),
        Box(-1.7, -1.7, -13.6, 3.4, 3.4, 4.6, "mortero_cabeza"),
    ])
    r.pieza("frasco_mano", "mano_i", (0, 0.6, 0), boxes=[
        Box(-1.5, -1.5, -3.2, 3.0, 3.0, 3.0, "frasco_cuerpo"),
        Box(-0.6, -0.6, -4.6, 1.2, 1.2, 1.5, "frasco_cuello"),
        Box(-0.75, -0.75, -5.2, 1.5, 1.5, 0.6, "frasco_tapon"),
    ])
    # ---- 1, el enfermo: los faldones de la bata, las vendas, las bubas de cristal y las unas
    for n in ("d", "i"):
        r.pieza(f"faldon_bata_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.3, -0.5, -2.45, 4.6, 6.6, 4.9, "faldon_bata")])
    r.pieza("venda_cabeza", "cabeza", (0, 0, 0), boxes=[
        Box(-4.3, -7.7, -4.3, 8.6, 1.7, 8.6, "venda_cabeza"),
        Box(1.6, -7.0, 4.2, 1.6, 4.4, 0.5, "venda_cabo"),
    ])
    r.pieza("venda_brazo", "antebrazo_d", (0, 0, 0), boxes=[Box(-1.8, 0.4, -1.8, 3.6, 4.6, 3.6, "venda_brazo")])
    r.pieza("bubas", "torso", (0, 0, 0), boxes=[
        Box(-3.3, -9.8, -3.3, 2.0, 2.0, 0.9, "buba"),
        Box(1.3, -5.6, -3.2, 1.6, 1.6, 0.8, "buba"),
        Box(-1.0, -8.6, 2.4, 2.2, 2.2, 1.0, "buba"),
        Box(1.9, -11.0, 2.4, 1.6, 1.6, 0.8, "buba"),
        Box(3.9, -3.4, -0.8, 0.8, 1.6, 1.6, "buba"),
    ])
    r.pieza("bubas_cabeza", "cabeza", (0, 0, 0), boxes=[
        Box(1.8, -2.6, -4.6, 1.4, 1.4, 0.7, "buba"),
        Box(-4.6, -5.0, -1.2, 0.7, 1.6, 1.6, "buba"),
    ])
    r.pieza("bubas_brazo", "brazo_i", (0, 0, 0), boxes=[Box(1.2, 0.4, -1.0, 0.8, 1.8, 1.8, "buba")])
    r.pieza("bubas_pierna", "muslo_d", (0, 0, 0), boxes=[Box(-1.0, 2.2, -2.7, 1.8, 1.8, 0.8, "buba")])
    for n in ("d", "i"):
        r.pieza(f"garras_{n}", f"mano_{n}", (0, 0, 0), boxes=[
            Box(-1.4, 0.4, -1.9, 0.5, 2.4, 0.5, "una"),
            Box(-0.25, 0.6, -1.9, 0.5, 2.6, 0.5, "una"),
            Box(0.9, 0.4, -1.9, 0.5, 2.4, 0.5, "una"),
        ])
    # ---- 2, la cuba: la cuba de cristal con su destilado, los tubos, el mandil, las gafas y los guantes
    r.pieza("cuba", "torso", (0, -12, 2.5), boxes=[
        Box(-4.6, -3.4, 0.0, 9.2, 14.0, 7.0, "cuba_cristal"),
        Box(-5.0, -3.9, -0.4, 10.0, 1.2, 7.8, "cuba_aro"),
        Box(-5.0, 9.8, -0.4, 10.0, 1.2, 7.8, "cuba_aro"),
        Box(-3.6, -4.7, 1.0, 7.2, 0.9, 5.0, "cuba_tapa"),
        Box(-0.8, -6.2, 2.7, 1.6, 1.6, 1.6, "cuba_valvula"),
    ])
    # el destilado de dentro, aparte: al reventar la cuba (al morir) el modelo lo esconde
    r.pieza("liquido", "cuba", (0, 0, 0), boxes=[Box(-4.0, -0.4, 0.6, 8.0, 10.4, 5.8, "cuba_liquido")])
    r.pieza("tubos", "torso", (0, 0, 0), boxes=[
        Box(-3.9, -15.6, -1.3, 1.0, 1.0, 4.4, "tubo"),
        Box(-3.9, -15.0, -1.5, 1.0, 3.8, 1.0, "tubo"),
        Box(2.9, -15.6, -1.3, 1.0, 1.0, 4.4, "tubo"),
        Box(2.9, -15.0, -1.5, 1.0, 3.8, 1.0, "tubo"),
    ])
    r.pieza("mandil", "torso", (0, 0, 0), boxes=[Box(-4.2, -10.4, -2.95, 8.4, 10.8, 0.5, "mandil")])
    for n in ("d", "i"):
        r.pieza(f"mandil_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.1, 0.0, -2.6, 4.2, 8.6, 0.5, "mandil_bajo")])
        r.pieza(f"guante_{n}", f"antebrazo_{n}", (0, 0, 0), boxes=[Box(-2.0, 2.2, -2.0, 4.0, 4.8, 4.0, "guante")])
    r.pieza("gafas", "cabeza", (0, 0, 0), boxes=[
        Box(-4.25, -6.2, -4.25, 8.5, 1.4, 8.5, "gafas_correa"),
        Box(-3.5, -6.7, -4.75, 2.7, 2.4, 0.6, "gafa"),
        Box(0.8, -6.7, -4.75, 2.7, 2.4, 0.6, "gafa"),
    ])
    r.empaquetar()
    return r


MANCEBO_P = ["sombrero", "pico", "lentes", "capelina", "cinturon", "zurron", "faldon_d", "faldon_i", "mortero", "frasco_mano"]
ENFERMO_P = ["faldon_bata_d", "faldon_bata_i", "venda_cabeza", "venda_brazo", "bubas", "bubas_cabeza", "bubas_brazo", "bubas_pierna",
             "garras_d", "garras_i"]
CUBA_P = ["cuba", "tubos", "mandil", "mandil_d", "mandil_i", "guante_d", "guante_i", "gafas"]
#: lo que se esconde en cada variante
OCULTAS = {
    0: ENFERMO_P + CUBA_P,
    1: MANCEBO_P + CUBA_P,
    2: MANCEBO_P + ENFERMO_P,
}

# ---------------------------------------------------------------- las texturas

CUERO = ["#120d0a", "#1c1510", "#271d16", "#33271d", "#403125"]          # el abrigo encerado, casi negro
CUERO_M = ["#2a1d13", "#3a2a1b", "#4c3725", "#5e4630", "#70573c"]        # la mascara: cuero curtido mas claro
SOMBRERO = ["#0c0b0a", "#151311", "#1f1c19", "#2a2622", "#35302b"]
LATON = ["#3f3014", "#634c20", "#8a6c33", "#ae8d49", "#cfae66"]
PANO = ["#1a1916", "#25231f", "#302e29", "#3c3a34"]                     # los calzones
BOTA = ["#0f0c0a", "#1a1511", "#251e18"]
VERDE, VERDE_B = "#5cff86", "#c8ffd6"                                    # el cristal verde que luce
PIEL = ["#353b38", "#4a514d", "#5f6762", "#757c76", "#8b918a", "#a2a79f"]  # la piel gris de los enfermos
PIEL_C = ["#3b4136", "#50574a", "#666d5e", "#7c8373", "#929886", "#a9ae9a"]  # la de la cuba, gris verdosa
VENA = "#3d5a2e"
VENDA = ["#5f5a48", "#7d7862", "#9b967d", "#b8b398", "#d0cbb0"]
BATA = ["#34332d", "#45443c", "#57554c", "#6a685d", "#7d7a6e"]
MANCHA = ["#4d5a19", "#677822", "#83962f"]
BUBA = ["#1b5229", "#2a7d40", "#3fac5b", "#72e08a", "#c0ffd0"]
LIQUIDO = ["#173f16", "#246b20", "#3b9a31", "#62c94d", "#a6f28c"]
HIERRO = ["#14151a", "#202228", "#2d3037", "#3c4048", "#4f545d"]
COBRE = ["#3d1f12", "#5e321c", "#82482a", "#a6603a", "#c47b4e"]
PIEDRA = ["#33312e", "#47443f", "#5c5852", "#726d66", "#89837b"]
MADERA = ["#1e150e", "#2a1d13", "#382718", "#46331f", "#55402a"]
CORCHO = ["#5c4127", "#7a5835", "#966e44"]
FUEGO = ["#5a2306", "#9c460c", "#de7a1c", "#ffb347", "#ffe2a0"]
SANGRE = ["#2a0a08", "#4a1410"]

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def materiales(variante):
    cuero_t = tela(CUERO, 9301, pliegue=4, grano=0.14, luz=0.12)
    mascara_t = ruido(CUERO_M[1:], 9302, grano=0.4, base=0.55)
    sombrero_t = tela(SOMBRERO, 9303, pliegue=5, grano=0.12, luz=0.1)
    piel_t = ruido(PIEL[1:], 9304, grano=0.4, base=0.55) if variante != 2 else ruido(PIEL_C[1:], 9305, grano=0.4, base=0.55)
    pieles = PIEL if variante != 2 else PIEL_C
    venda_t = tela(VENDA, 9306, pliegue=2, grano=0.18, luz=0.08)
    bata_t = tela(BATA, 9307, pliegue=3, grano=0.16, luz=0.14)
    pano_t = tela(PANO, 9308, pliegue=3, grano=0.12, luz=0.1)
    hierro_t = metal(HIERRO, 9309)
    cobre_t = metal(COBRE, 9310)
    piedra_t = ruido(PIEDRA, 9311, grano=0.5, base=0.5)
    madera_t = madera(MADERA, 9312, "z")
    mandil_t = ruido(CUERO_M[:4], 9313, grano=0.35, base=0.45)

    def manchado(f, semilla, cuanto=0.12):
        r = __import__("numpy").random.default_rng(semilla).random((64, 64))

        def g(cara, x, y, w, h):
            c = f(cara, x, y, w, h)
            if c is None:
                return None
            k = r[(y + 5 * CARAS[cara]) % 64, (x + 9 * CARAS[cara]) % 64]
            if k < cuanto:
                return mezcla(c, MANCHA[int(k * 100) % 3], 0.65)       # lo que supura
            return c
        return g

    # ---------------------------------------------------- la cabeza de cada uno
    def cabeza(cara, x, y, w, h):
        if variante == 0:
            # la mascara de cuero cosida (las lentes y el pico van aparte; arriba, el sombrero)
            c = mascara_t(cara, x, y, w, h)
            if cara == "frente" and x in (3, 4) and y >= 6:
                return oscuro(c, 0.3)                             # la costura de la barbilla
            if cara in ("der", "izq") and y in (3, 4) and delante(cara, x, w) == 6:
                return LATON[2]                                  # la hebilla de la correa
            if cara in ("der", "izq") and y == 3:
                return CUERO[2]                                  # la correa que la sujeta
            if cara == "espalda" and y == 3:
                return CUERO[2]
            if cara == "espalda" and y >= 4:
                return CUERO[1 + (x + y) % 2]                    # la capucha del abrigo por detras
            if (x * 3 + y * 5 + CARAS[cara]) % 11 == 0:
                return oscuro(c, 0.25)                           # las puntadas
            return c
        if variante == 1:
            C = {"S": PIEL[4], "s": PIEL[3], "K": "#0c0f0d", "k": PIEL[1], "g": "#7f9a86", "N": PIEL[2], "M": "#120c0b",
                 "m": PIEL[2], "v": VENA}
            if cara == "frente":
                return dibujo([
                    "sSsssSss",
                    "sssSssss",
                    "ssvsssSs",
                    "skKssKks",
                    "sKgssgKs",
                    "sssNNsss",
                    "sMMMMMMs",
                    "ssmmmmss",
                ], C)(cara, x, y, w, h)
            c = piel_t(cara, x, y, w, h)
            if cara == "arriba" and (x * 5 + y * 3) % 7 == 0:
                return PIEL[1]                                   # el pelo que le queda, a mechones
            if cara == "espalda" and y < 3 and (x + y) % 3 == 0:
                return PIEL[1]
            if (x * 7 + y * 3 + CARAS[cara]) % 13 == 0:
                return VENA
            return c
        # la cuba: la cabeza rapada con la cicatriz cosida y la boca cosida (las gafas, aparte)
        C = {"O": PIEL_C[2], "o": PIEL_C[3], "x": "#1d1a14", "N": PIEL_C[1], "M": "#140f0c", "S": PIEL_C[4]}
        if cara == "frente":
            return dibujo([
                "OoooSooO",
                "oxoxoxoo",
                "oooooooo",
                "oooooooo",
                "oNooooNo",
                "oooNNooo",
                "oMxMxMxo",
                "OoooooOO",
            ], C)(cara, x, y, w, h)
        c = piel_t(cara, x, y, w, h)
        if cara == "arriba" and y == 4:
            return "#1d1a14" if x % 2 else PIEL_C[1]            # la cicatriz de la coronilla, cosida
        if cara in ("der", "izq") and y == 1 and x % 2 == 0:
            return "#1d1a14"
        return c

    # ---------------------------------------------------- el torso
    def torso(cara, x, y, w, h):
        if variante == 0:
            c = cuero_t(cara, x, y, w, h)
            if cara == "frente":
                if x == 3 and y % 3 == 1:
                    return LATON[3]                              # los botones
                if x in (3, 4):
                    return oscuro(c, 0.15)                       # la abotonadura
                if y < 3 and x in (1, 2, 5, 6):
                    return claro(c, 0.06)                        # las solapas
            if cara == "espalda" and x == 4:
                return oscuro(c, 0.2)                            # la costura
            return c
        if variante == 1:
            if cara == "frente" and y == 0 and 2 <= x <= 5:
                return piel_t(cara, x, y, w, h)                  # el escote de la bata
            if cara == "arriba" and 2 <= x <= 5:
                return piel_t(cara, x, y, w, h)
            c = bata_t(cara, x, y, w, h)
            if cara == "espalda" and x in (3, 4) and y > 1:
                return piel_t(cara, x, y, w, h) if (x + y) % 4 else oscuro(VENDA[1], 0.2)   # abierta por detras, las cintas
            return c
        # la cuba: el pecho desnudo, cosido; los tubos le entran por arriba
        c = piel_t(cara, x, y, w, h)
        if cara == "frente" and (x - y) % 5 == 0 and 2 <= y <= 9:
            return "#1d1a14" if (x + y) % 2 else PIEL_C[1]      # una costura en diagonal
        if cara == "frente" and y <= 1 and x in (0, 7):
            return LIQUIDO[1]                                    # donde le entran los tubos
        if cara == "espalda" and y >= 1:
            return HIERRO[1] if (x + y) % 3 else HIERRO[2]       # las correas de la cuba
        return c

    # ---------------------------------------------------- brazos y piernas
    def brazo(pieza):
        def f(cara, x, y, w, h):
            if variante == 0:
                c = cuero_t(cara, x, y, w, h)
                if pieza.startswith("antebrazo") and y >= h - 2:
                    return CUERO_M[1] if (x + y) % 2 else CUERO_M[0]     # los guantes
                if pieza.startswith("antebrazo") and y == h - 3:
                    return oscuro(c, 0.25)                               # el puno
                return c
            c = piel_t(cara, x, y, w, h)
            if pieza.startswith("antebrazo") and y >= h - 1:
                return oscuro(c, 0.35)                                   # los dedos
            if variante == 1 and pieza == "brazo_d" and y < 2:
                return bata_t(cara, x, y, w, h)                          # las mangas cortas de la bata
            if variante == 1 and pieza == "brazo_i" and y < 2:
                return bata_t(cara, x, y, w, h)
            if (x * 5 + y * 3 + CARAS[cara]) % 11 == 0:
                return VENA if variante == 1 else oscuro(c, 0.2)
            return c
        return f

    def pierna(pieza):
        def f(cara, x, y, w, h):
            if variante == 1:
                c = piel_t(cara, x, y, w, h)
                if pieza.startswith("pierna") and y >= h - 1:
                    return oscuro(c, 0.45)                               # los pies descalzos, sucios
                if (x * 3 + y * 7 + CARAS[cara]) % 13 == 0:
                    return VENA
                return c
            c = pano_t(cara, x, y, w, h) if variante == 0 else mezcla(pano_t(cara, x, y, w, h), CUERO_M[2], 0.35)
            if pieza.startswith("pierna") and y >= h - 3:
                return BOTA[(x + y) % 3] if y < h - 1 else BOTA[0]       # las botas
            return c
        return f

    # ---------------------------------------------------- el mancebo
    def ala(cara, x, y, w, h):
        c = sombrero_t(cara, x, y, w, h)
        if cara == "abajo":
            return oscuro(c, 0.3)
        if cara == "arriba" and (x in (0, w - 1) or y in (0, h - 1)):
            return oscuro(c, 0.2)
        return c

    def copa(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = sombrero_t(cara, x, y, w, h)
        if cara == "arriba":
            return claro(c, 0.04)
        return c

    def cinta(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return LATON[2] if cara == "frente" and x in (4, 5) else "#2a1712"

    def pico(cara, x, y, w, h):
        c = mascara_t(cara, x, y, w, h)
        if cara == "arriba" and x == w // 2:
            return oscuro(c, 0.3)                                # la costura del pico
        if cara == "abajo":
            return oscuro(c, 0.25)
        if cara in ("der", "izq") and y == h - 1 and x % 2 == 0:
            return CUERO_M[0]                                    # los agujeros de respirar
        return c

    def lente(cara, x, y, w, h):
        if cara == "frente":
            if 0 < x < w - 1 and 0 < y < h - 1:
                return VERDE
            return LATON[3] if (x + y) % 2 else LATON[2]         # el aro de laton
        return LATON[1]

    def capelina(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = cuero_t(cara, x, y, w, h)
        if cara == "arriba" and 3 <= x <= 6 and 2 <= y <= 4:
            return None                                          # el hueco del cuello
        if y == h - 1 and (x * 3 + CARAS[cara]) % 4 == 0:
            return oscuro(c, 0.3)
        return claro(c, 0.05) if y == 0 else c

    def cinturon(cara, x, y, w, h):
        if cara == "frente" and x in (3, 4):
            return LATON[3] if y == 0 else LATON[2]              # la hebilla
        return "#2a1a10" if (x + y) % 3 else "#3a2616"

    def frasquito(liquido):
        def f(cara, x, y, w, h):
            if cara == "arriba":
                return CORCHO[1]
            if y == 0:
                return "#a8c4b4"                                 # el cuello de cristal
            return liquido if (x + y) % 3 else claro(liquido, 0.3)
        return f

    def zurron(cara, x, y, w, h):
        c = mandil_t(cara, x, y, w, h)
        if cara == "frente" and y == 0:
            return oscuro(c, 0.3)                                # la solapa
        if cara == "frente" and y == 1 and x == 1:
            return LATON[2]
        return c

    def faldon_abrigo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = cuero_t(cara, x, y, w, h)
        if cara == "frente" and x == w - 1 and y < h - 1:
            return oscuro(c, 0.2)                                # la abertura del abrigo
        if y == h - 1:
            return oscuro(c, 0.25)                               # el bajo, rozado
        if (x * 5 + y * 3 + CARAS[cara]) % 23 == 0:
            return claro(c, 0.12)                                # la cera que brilla
        return oscuro(c, 0.03 * (y / 3))

    def mortero_mango(cara, x, y, w, h):
        c = madera_t(cara, x, y, w, h)
        if cara in ("der", "izq", "arriba", "abajo") and (x if cara in ("arriba", "abajo") else y) % 5 == 0:
            return "#2a1a10"                                     # el cuero del puno
        return c

    def mortero_cabeza(cara, x, y, w, h):
        c = piedra_t(cara, x, y, w, h)
        if cara == "frente" or cara == "abajo":
            return mezcla(c, LIQUIDO[2], 0.25)                   # la punta, verde de lo que muele
        return c

    def frasco_cuerpo(cara, x, y, w, h):
        if cara == "arriba":
            return "#b9d7c6"
        if (x == 0 and y == 1) or (x == 1 and y == 0):
            return FUEGO[4]                                      # el brillo del cristal
        return FUEGO[2 + (x + y) % 2]

    def frasco_cuello(cara, x, y, w, h):
        return "#a8c4b4" if (x + y) % 2 else "#8aa898"

    def frasco_tapon(cara, x, y, w, h):
        return CORCHO[(x + y) % 3]

    # ---------------------------------------------------- el enfermo
    def faldon_bata(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        corte = h - 1 - ((x * 5 + CARAS[cara] * 3) % 3)
        if y > corte or (x * 3 + y * 7 + CARAS[cara]) % 19 == 0:
            return None                                          # deshilachada
        return bata_t(cara, x, y, w, h)

    def venda(cara, x, y, w, h):
        c = venda_t(cara, x, y, w, h)
        if (x + 2 * y) % 5 == 0:
            return oscuro(c, 0.2)                                # las vueltas de la venda
        if (x * 3 + y + CARAS[cara]) % 9 == 0:
            return mezcla(c, MANCHA[1], 0.5)                     # lo que traspasa
        if (x * 7 + y * 5 + CARAS[cara]) % 29 == 0:
            return mezcla(c, SANGRE[1], 0.5)
        return c

    def venda_cabeza(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba" and 1 < x < w - 2 and 1 < y < h - 2:
            return None
        return venda(cara, x, y, w, h)

    def buba(cara, x, y, w, h):
        if (x + y) % 3 == 0:
            return BUBA[4]                                       # el brillo del cristal
        return BUBA[2 + (x * 3 + y) % 2]

    def una(cara, x, y, w, h):
        return "#1a1712" if y < h - 1 else "#3a352b"

    # ---------------------------------------------------- la cuba
    def cuba_cristal(cara, x, y, w, h):
        # el cristal: casi todo transparente (se ve el destilado de dentro); los montantes de hierro en las esquinas y un reflejo
        if cara in ("arriba", "abajo"):
            return None
        if x == 0 or x == w - 1:
            return HIERRO[2] if y % 4 else HIERRO[3]             # los montantes, con sus remaches
        if x == 2 and 1 < y < h - 3:
            return "#cfe8d8"                                     # el reflejo del cristal
        if x == 3 and 2 < y < h - 6 and y % 2:
            return "#a9d2bb"
        if y in (h // 2,) and cara in ("frente", "espalda"):
            return HIERRO[1]                                     # el aro de en medio
        return None

    def cuba_liquido(cara, x, y, w, h):
        k = 4 if y < 2 else (3 if y < 5 else 2)
        if y >= h - 2:
            k = 1
        if (x * 7 + y * 3 + CARAS[cara]) % 11 == 0:
            return LIQUIDO[min(4, k + 1)]                        # las burbujas
        if (x * 5 + y * 13 + CARAS[cara]) % 17 == 0:
            return "#3a2e20"                                     # lo que flota dentro (no mires)
        return LIQUIDO[k]

    def cuba_aro(cara, x, y, w, h):
        return hierro_t(cara, x, y, w, h) if (x + y) % 5 else HIERRO[4]

    def cuba_tapa(cara, x, y, w, h):
        return cobre_t(cara, x, y, w, h)

    def cuba_valvula(cara, x, y, w, h):
        return COBRE[3] if (x + y) % 2 else COBRE[2]

    def tubo(cara, x, y, w, h):
        if (x + y) % 3 == 0:
            return LIQUIDO[3]                                    # el destilado que corre por dentro
        return COBRE[2 + (x + y) % 2]

    def mandil(cara, x, y, w, h):
        c = mandil_t(cara, x, y, w, h)
        if cara == "frente" and (x * 3 + y * 5) % 13 == 0:
            return mezcla(c, LIQUIDO[1], 0.6)                    # quemaduras de acido
        if cara == "frente" and y == 0:
            return "#1a120b"                                     # el borde de arriba
        return c

    def mandil_bajo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = mandil_t(cara, x, y, w, h)
        if y == h - 1 and x % 2:
            return None
        if (x * 3 + y * 5) % 11 == 0:
            return mezcla(c, LIQUIDO[1], 0.6)
        return c

    def guante(cara, x, y, w, h):
        c = mezcla(mandil_t(cara, x, y, w, h), "#000000", 0.25)
        if y == 0:
            return oscuro(c, 0.3)                                # el puno vuelto
        if y >= h - 1:
            return oscuro(c, 0.45)
        return c

    def gafas_correa(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        return "#1e1610" if (x + y) % 3 else "#2c2118"

    def gafa(cara, x, y, w, h):
        if cara == "frente":
            if 0 < x < w - 1 and 0 < y < h - 1:
                return VERDE
            return COBRE[3]
        return COBRE[2]

    mats = {
        "cabeza": sombra(manchado(cabeza, 9321, 0.06) if variante else cabeza, abajo=0.14),
        "torso": sombra(manchado(torso, 9322, 0.1 if variante == 1 else 0.04), abajo=0.16),
        "brazo_d": sombra(brazo("brazo_d"), abajo=0.1), "brazo_i": sombra(brazo("brazo_i"), abajo=0.1),
        "antebrazo_d": sombra(brazo("antebrazo_d"), abajo=0.14), "antebrazo_i": sombra(brazo("antebrazo_i"), abajo=0.14),
        "muslo_d": sombra(pierna("muslo_d"), abajo=0.1), "muslo_i": sombra(pierna("muslo_i"), abajo=0.1),
        "pierna_d": sombra(pierna("pierna_d"), abajo=0.16), "pierna_i": sombra(pierna("pierna_i"), abajo=0.16),
        "ala_sombrero": ala, "copa_sombrero": sombra(copa, abajo=0.1), "cinta_sombrero": cinta,
        "pico": sombra(pico, abajo=0.12), "pico_punta": sombra(pico, abajo=0.12), "lente": lente,
        "capelina": sombra(capelina, abajo=0.14), "cinturon": cinturon,
        "frasquito_rojo": frasquito("#b8321c"), "frasquito_verde": frasquito(LIQUIDO[3]), "frasquito_ambar": frasquito("#d88a1e"),
        "zurron": sombra(zurron, abajo=0.15), "faldon_abrigo": sombra(faldon_abrigo, abajo=0.1),
        "mortero_mango": mortero_mango, "mortero_cabeza": sombra(mortero_cabeza, abajo=0.12),
        "frasco_cuerpo": frasco_cuerpo, "frasco_cuello": frasco_cuello, "frasco_tapon": frasco_tapon,
        "faldon_bata": sombra(manchado(faldon_bata, 9323, 0.12), abajo=0.12),
        "venda_cabeza": venda_cabeza, "venda_cabo": venda, "venda_brazo": sombra(venda, abajo=0.12),
        "buba": buba, "una": una,
        "cuba_cristal": cuba_cristal, "cuba_liquido": cuba_liquido, "cuba_aro": cuba_aro, "cuba_tapa": cuba_tapa,
        "cuba_valvula": cuba_valvula, "tubo": tubo,
        "mandil": sombra(mandil, abajo=0.12), "mandil_bajo": sombra(mandil_bajo, abajo=0.12), "guante": sombra(guante, abajo=0.12),
        "gafas_correa": gafas_correa, "gafa": gafa,
    }
    return mats


def brillo(variante):
    """Lo que luce: el cristal verde (las lentes del mancebo, las bubas del enfermo, la cuba y las gafas), los frascos."""
    b = {}
    if variante == 0:
        b["lente"] = lambda cara, x, y, w, h: (VERDE_B if (x + y) % 3 == 0 else VERDE) if cara == "frente" and 0 < x < w - 1 and 0 < y < h - 1 else None
        b["frasco_cuerpo"] = lambda cara, x, y, w, h: FUEGO[3] if cara != "arriba" and (x + y) % 2 == 0 else None
        b["frasquito_verde"] = lambda cara, x, y, w, h: LIQUIDO[3] if y > 0 and cara != "arriba" else None
    if variante == 1:
        b["buba"] = lambda cara, x, y, w, h: BUBA[3] if (x + y) % 2 == 0 else None
        C = {"g": "#8affa8"}
        b["cabeza"] = por_cara(liso(None), frente=dibujo([
            "........",
            "........",
            "........",
            "........",
            "..g..g..",
        ], C))
    if variante == 2:
        b["cuba_liquido"] = lambda cara, x, y, w, h: LIQUIDO[4] if y < 3 or (x * 7 + y * 3 + CARAS[cara]) % 11 == 0 else LIQUIDO[3]
        b["tubo"] = lambda cara, x, y, w, h: LIQUIDO[3] if (x + y) % 3 == 0 else None
        b["gafa"] = lambda cara, x, y, w, h: VERDE if cara == "frente" and 0 < x < w - 1 and 0 < y < h - 1 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos 9,5).
# _arma = hacia donde apunta lo que lleva en la derecha (a -Z de la mano): la mano de mortero.

BASE = dict(raiz=raiz(-0.3), torso=(10, 0, 2), cabeza=(6, -4, 4),
            muslo_d=(-2, 0, 3), pierna_d=(4, 0, 0), muslo_i=(2, 0, -3), pierna_i=(6, 0, 0),
            brazo_i=(-4, 0, -6), antebrazo_i=(-12, 0, 0), brazo_d=(-4, 0, 6), antebrazo_d=(-10, 0, 0))
PORTE = {
    # el mortero colgando en la derecha; el frasco en la izquierda, delante del pecho
    0: dict(_mano_d=("h", -0.4, -8.2, 2.4), _arma=(0.05, -0.78, 0.62), brazo_i=(-34, 0, -8), antebrazo_i=(-62, 0, 0)),
    # los brazos colgando por delante, las garras sueltas
    1: dict(brazo_d=(-26, 0, 6), antebrazo_d=(-28, 0, 0), brazo_i=(-32, 0, -6), antebrazo_i=(-22, 0, 0)),
    # los brazos pesados, un poco abiertos (los guantes casi le llegan a las rodillas)
    2: dict(brazo_d=(-6, 0, 14), antebrazo_d=(-16, 0, 0), brazo_i=(-6, 0, -14), antebrazo_i=(-16, 0, 0)),
}
GUARDIA = dict(raiz=raiz(-1.0), torso=(16, 8, 0), cabeza=(-8, -8, 4),
               muslo_d=(-12, 0, 6), pierna_d=(22, 0, 0), muslo_i=(-2, 0, -6), pierna_i=(16, 0, 0))
LISTO = {
    0: con(GUARDIA, _mano_d=("h", 2.2, -4.4, 5.4), _arma=(0.3, 0.78, 0.55), brazo_i=(-72, 0, -26), antebrazo_i=(-48, 0, 0)),
    1: con(GUARDIA, raiz=raiz(-2.0), torso=(30, 6, 0), cabeza=(-24, -6, 0), brazo_d=(-70, 0, 20), antebrazo_d=(-30, 0, 0),
           brazo_i=(-74, 0, -18), antebrazo_i=(-26, 0, 0)),
    2: con(GUARDIA, torso=(20, 12, 0), cabeza=(-12, -10, 0), brazo_d=(-56, 0, 12), antebrazo_d=(-64, 0, 0),
           brazo_i=(-50, 0, -14), antebrazo_i=(-70, 0, 0)),
}
# inclinado sobre su mesa, moliendo (REZA del mancebo): el mortero apunta abajo, a la escudilla que sostiene la izquierda
MOLIENDO = dict(raiz=raiz(-0.6), torso=(34, 0, 0), cabeza=(16, 0, 0),
                muslo_d=(-6, 0, 3), pierna_d=(8, 0, 0), muslo_i=(-2, 0, -3), pierna_i=(6, 0, 0),
                _mano_d=("h", 3.4, -6.4, 6.4), _arma=(0.1, -0.96, 0.2), brazo_i=(-64, 0, -4), antebrazo_i=(-46, 0, 0))
# sentado en el suelo, abrazado a las rodillas (REZA del enfermo)
ENCOGIDO = dict(raiz=raiz(-9.5, 1.0), torso=(30, 0, 0), cabeza=(36, 0, 0),
                muslo_d=(-120, 0, 6), pierna_d=(144, 0, 0), muslo_i=(-118, 0, -6), pierna_i=(142, 0, 0),
                brazo_d=(-72, 0, 18), antebrazo_d=(-64, 0, 0), brazo_i=(-72, 0, -18), antebrazo_i=(-64, 0, 0))
# de pie, encorvado, jadeando (REZA de la cuba)
JADEANDO = con(BASE, raiz=raiz(-1.2), torso=(26, 0, 0), cabeza=(-14, 0, 0), brazo_d=(-10, 0, 16), antebrazo_d=(-22, 0, 0),
               brazo_i=(-10, 0, -16), antebrazo_i=(-22, 0, 0))
# tendido boca arriba (TUMBADO), una mano en el vientre y la cabeza ladeada
TENDIDO = dict(raiz=raiz(2.6, -13.0, rot=(-90, 0, 0)), torso=(0, 0, 0), cabeza=(-8, 34, 0),
               muslo_d=(-4, 0, 6), pierna_d=(8, 0, 0), muslo_i=(-8, 0, -6), pierna_i=(14, 0, 0),
               brazo_d=(4, 0, 26), antebrazo_d=(-10, 0, 0), brazo_i=(-34, 0, -10), antebrazo_i=(-72, 0, 0))
# de bruces (al morir)
BRUCES = dict(raiz=raiz(2.0, 12.8, rot=(88, 0, 0)), torso=(0, 0, 0), cabeza=(-30, 30, 0),
              muslo_d=(-4, 0, 6), pierna_d=(10, 0, 0), muslo_i=(2, 0, -6), pierna_i=(18, 0, 0),
              brazo_d=(-150, 0, 20), antebrazo_d=(-10, 0, 0), brazo_i=(-20, 0, -20), antebrazo_i=(-30, 0, 0))
RODILLAS = dict(raiz=raiz(-6.0, 0.8), torso=(26, 0, 0), cabeza=(30, 0, 0), muslo_d=(-10, 0, 5), pierna_d=(96, 0, 0),
                muslo_i=(-8, 0, -5), pierna_i=(96, 0, 0), brazo_d=(-10, 0, 14), antebrazo_d=(-10, 0, 0), brazo_i=(-10, 0, -14),
                antebrazo_i=(-10, 0, 0))
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

    # ---- quieto: respira con la boca abierta, se le va la cabeza a un lado; de vez en cuando un temblor
    bucle("quieto", 4.0, [
        (0.0, BASE),
        (1.2, con(BASE, raiz=raiz(-0.6), torso=(13, 3, 2), cabeza=(10, -14, 6))),
        (2.2, con(BASE, torso=(9, 0, 1), cabeza=(4, 2, 0))),
        (2.45, con(BASE, torso=(10, -3, 3), cabeza=(8, 16, -6))),
        (2.6, con(BASE, torso=(9, 2, 1), cabeza=(6, 10, -2))),
        (3.4, con(BASE, raiz=raiz(-0.5), torso=(12, -2, 0), cabeza=(10, 6, -4))),
        (4.0, BASE),
    ])
    # ---- andar: arrastrando los pies, encorvado
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-24, 20, 8, 16, 6, 0.0), (-6, 4, 6, 30, 0, 0.6), (20, -24, 16, 8, -6, 0.0), (4, -6, 30, 6, 0, 0.6), (-24, 20, 8, 16, 6, 0.0))):
        pasos.append((1.2 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (12, ty, -ty * 0.4), "cabeza": (4, -ty * 1.1, ty * 0.5), "raiz": raiz(up)}))
    bucle("andar", 1.2, pasos)
    for v in range(3):
        q = fija(con(BASE, PORTE[v]))
        bucle(f"porte_{VARIANTES[v]}", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
        l0 = fija(LISTO[v])
        l1 = fija(con(LISTO[v], raiz=raiz(LISTO[v]["raiz"]["pos"][1] - 0.8), torso=(LISTO[v]["torso"][0] + 4, LISTO[v]["torso"][1] - 3, 0)))
        bucle(f"listo_{VARIANTES[v]}", 1.7, [(0.0, l0), (0.85, l1), (1.7, l0)])
    # ---- moliendo (el mancebo): la mano de mortero da vueltas en la escudilla
    mo = [fija(con(MOLIENDO, _mano_d=("h",) + p)) for p in ((3.0, -6.4, 6.8), (3.8, -6.7, 5.9), (4.5, -6.4, 6.5), (3.8, -6.1, 7.2))]
    bucle("moliendo", 2.0, [(0.0, mo[0]), (0.5, mo[1]), (1.0, mo[2]), (1.5, mo[3]), (2.0, mo[0])])
    # ---- encogido (el enfermo): se mece abrazado a las rodillas
    en = fija(ENCOGIDO)
    bucle("encogido", 3.2, [(0.0, en), (1.6, fija(con(ENCOGIDO, torso=(38, 0, 0), cabeza=(44, 0, 0), raiz=raiz(-9.5, 1.6)))), (3.2, en)])
    # ---- jadeando (la cuba): el pecho le sube y le baja
    ja = fija(JADEANDO)
    bucle("jadeando", 1.6, [(0.0, ja), (0.7, fija(con(JADEANDO, raiz=raiz(-0.8), torso=(21, 0, 0), cabeza=(-20, 0, 0)))), (1.6, ja)])
    # ---- tendido (TUMBADO): respira despacio; un temblor de la mano
    te = fija(TENDIDO)
    bucle("tendido", 5.0, [(0.0, te), (2.4, fija(con(TENDIDO, torso=(-3, 0, 0), brazo_i=(-38, 0, -10)))), (3.6, te),
                           (3.8, fija(con(TENDIDO, antebrazo_d=(-30, 0, 0)))), (4.0, te), (5.0, te)])

    # ================================================================ acciones
    L0, L1, L2 = LISTO[0], LISTO[1], LISTO[2]
    M0 = dict(_mano_d=("h", 1.4, -6.2, 3.4), _arma=(0.2, -0.6, 0.7))       # el mortero abajo mientras lanza

    # ---- 1 frasco (el mancebo): echa atras el frasco con la izquierda por encima del hombro y lo lanza en arco
    FR_ATRAS = con(L0, M0, raiz=raiz(-0.8, 1.4), torso=(-6, -30, 0), cabeza=(-14, 24, 0), brazo_i=(-150, 0, -30), antebrazo_i=(-50, 0, 0))
    FR_SUELTA = con(L0, M0, raiz=raiz(-1.8, -1.6), torso=(24, 22, 0), cabeza=(-16, -16, 0), muslo_d=(-34, 0, 6), pierna_d=(34, 0, 0),
                    brazo_i=(-110, 0, -4), antebrazo_i=(-6, 0, 0))
    accion("frasco", 1.4, [
        (0.00, L0),
        (0.50, FR_ATRAS),
        (0.70, con(FR_ATRAS, torso=(-8, -34, 0))),
        (0.82, FR_SUELTA),
        (1.05, con(FR_SUELTA, brazo_i=(-60, 0, -10), antebrazo_i=(-10, 0, 0))),
        (1.40, L0),
    ], golpes=(0.83,))
    # ---- 2 mortero (el mancebo): alza la mano de mortero y la deja caer de arriba abajo
    MO_ALTO = con(L0, raiz=raiz(0.0, 1.0), torso=(-10, 16, 0), cabeza=(-18, -10, 0), _mano_d=("h", 0.6, 7.2, 1.0), _arma=(0.05, 0.5, -0.86))
    MO_BAJO = con(L0, raiz=raiz(-2.4, -1.8), torso=(36, -8, 0), cabeza=(-24, 6, 0), muslo_d=(-38, 0, 4), pierna_d=(38, 0, 0),
                  _mano_d=("h", 2.6, -2.6, 7.6), _arma=(0.05, -0.6, 0.8))
    accion("mortero", 1.3, [
        (0.00, L0),
        (0.45, MO_ALTO),
        (0.62, con(MO_ALTO, torso=(-12, 18, 0))),
        (0.72, con(L0, torso=(10, 4, 0), _mano_d=("h", 2.0, 3.4, 7.4), _arma=(0.05, 0.4, 0.9))),
        (0.78, MO_BAJO),
        (1.00, con(MO_BAJO, torso=(34, -8, 0))),
        (1.30, L0),
    ], golpes=(0.78,))
    # ---- 3 mortero_barrido (el mancebo): de derecha a izquierda, a la altura de la cara
    MB_D = con(L0, raiz=raiz(-1.0, 0.6), torso=(10, 50, 0), cabeza=(-4, -44, 0), _mano_d=("h", -1.0, -2.4, 4.0), _arma=(-1, 0.2, -0.2))
    MB_F = con(L0, raiz=raiz(-1.8, -0.6), torso=(14, 8, 0), cabeza=(-8, -8, 0), _mano_d=("h", 2.6, -3.0, 7.4), _arma=(0.2, 0.15, 1))
    MB_I = con(L0, raiz=raiz(-2.0, -1.4), torso=(18, -40, 0), cabeza=(-10, 34, 0), muslo_i=(-28, 0, -6), pierna_i=(26, 0, 0),
               _mano_d=("h", 5.6, -3.4, 4.4), _arma=(1, 0.15, 0.3))
    accion("mortero_barrido", 1.1, [
        (0.00, L0),
        (0.36, MB_D),
        (0.50, MB_F),
        (0.58, MB_I),
        (0.82, con(MB_I, torso=(18, -44, 0))),
        (1.10, L0),
    ], golpes=(0.52,))
    # ---- 4 zarpazo (el enfermo): alza la derecha y la baja en diagonal, las unas por delante
    ZP_ALTO = con(L1, raiz=raiz(-1.0, 0.8), torso=(10, 34, 0), cabeza=(-12, -24, 0), brazo_d=(-170, 0, 30), antebrazo_d=(-30, 0, 0))
    ZP_BAJO = con(L1, raiz=raiz(-2.8, -1.6), torso=(40, -24, 0), cabeza=(-28, 16, 0), muslo_d=(-40, 0, 6), pierna_d=(40, 0, 0),
                  brazo_d=(-40, 0, -20), antebrazo_d=(-10, 0, 0))
    accion("zarpazo", 1.0, [
        (0.00, L1),
        (0.34, ZP_ALTO),
        (0.46, con(ZP_ALTO, torso=(8, 38, 0))),
        (0.55, ZP_BAJO),
        (0.78, con(ZP_BAJO, torso=(38, -26, 0))),
        (1.00, L1),
    ], golpes=(0.55,))
    # ---- 5 abalanzarse (el enfermo): se agacha y salta con las dos garras por delante
    AB_AGACHA = con(L1, raiz=raiz(-4.0, 1.0), torso=(44, 0, 0), cabeza=(-34, 0, 0), muslo_d=(-56, 0, 8), pierna_d=(84, 0, 0),
                    muslo_i=(-30, 0, -8), pierna_i=(70, 0, 0), brazo_d=(-20, 0, 30), antebrazo_d=(-40, 0, 0), brazo_i=(-20, 0, -30),
                    antebrazo_i=(-40, 0, 0))
    AB_VUELA = con(L1, raiz=raiz(1.0, -2.0), torso=(56, 0, 0), cabeza=(-44, 0, 0), muslo_d=(30, 0, 6), pierna_d=(30, 0, 0),
                   muslo_i=(20, 0, -6), pierna_i=(40, 0, 0), brazo_d=(-160, 0, 16), antebrazo_d=(-10, 0, 0), brazo_i=(-160, 0, -16),
                   antebrazo_i=(-10, 0, 0))
    AB_CAE = con(L1, raiz=raiz(-3.4, -2.4), torso=(50, 0, 0), cabeza=(-36, 0, 0), muslo_d=(-40, 0, 6), pierna_d=(70, 0, 0),
                 muslo_i=(-6, 0, -6), pierna_i=(50, 0, 0), brazo_d=(-80, 0, 10), antebrazo_d=(-10, 0, 0), brazo_i=(-80, 0, -10),
                 antebrazo_i=(-10, 0, 0))
    accion("abalanzarse", 1.5, [
        (0.00, L1),
        (0.40, AB_AGACHA),
        (0.55, con(AB_AGACHA, raiz=raiz(-4.4, 1.2))),
        (0.70, AB_VUELA),
        (0.90, AB_CAE),
        (1.15, con(AB_CAE, torso=(46, 0, 0))),
        (1.50, L1),
    ], golpes=(0.90,))
    # ---- 6 escupitajo (el enfermo): echa atras la cabeza y escupe lo que lleva dentro
    ES_ATRAS = con(L1, raiz=raiz(-0.6, 1.2), torso=(-14, 0, 0), cabeza=(-40, 0, 0), brazo_d=(-30, 0, 30), brazo_i=(-30, 0, -30))
    ES_SUELTA = con(L1, raiz=raiz(-2.0, -1.0), torso=(40, 0, 0), cabeza=(-6, 0, 0), brazo_d=(-50, 0, 20), brazo_i=(-50, 0, -20))
    accion("escupitajo", 1.3, [
        (0.00, L1),
        (0.45, ES_ATRAS),
        (0.66, con(ES_ATRAS, cabeza=(-46, 0, 0))),
        (0.76, ES_SUELTA),
        (1.00, con(ES_SUELTA, torso=(36, 0, 0))),
        (1.30, L1),
    ], golpes=(0.76,))
    # ---- 7 punetazo (la cuba): el brazo derecho atras y el gancho
    PU_ATRAS = con(L2, raiz=raiz(-1.0, 1.0), torso=(14, 44, 0), cabeza=(-10, -36, 0), brazo_d=(-30, 0, 70), antebrazo_d=(-80, 0, 0))
    PU_DA = con(L2, raiz=raiz(-2.2, -1.6), torso=(26, -30, 0), cabeza=(-18, 24, 0), muslo_d=(-36, 0, 6), pierna_d=(36, 0, 0),
                brazo_d=(-96, 0, -10), antebrazo_d=(-10, 0, 0))
    accion("punetazo", 1.3, [
        (0.00, L2),
        (0.45, PU_ATRAS),
        (0.62, con(PU_ATRAS, torso=(12, 48, 0))),
        (0.74, PU_DA),
        (0.98, con(PU_DA, torso=(24, -32, 0))),
        (1.30, L2),
    ], golpes=(0.74,))
    # ---- 8 embestida (la cuba): baja el hombro y se lanza
    EM_A = con(L2, raiz=raiz(-2.6, 1.0), torso=(44, 30, 0), cabeza=(-30, -24, 0), muslo_d=(-40, 0, 6), pierna_d=(50, 0, 0),
               muslo_i=(10, 0, -6), pierna_i=(30, 0, 0), brazo_d=(10, 0, 20), antebrazo_d=(-50, 0, 0), brazo_i=(-40, 0, -20),
               antebrazo_i=(-60, 0, 0))
    EM_B = con(EM_A, muslo_d=(20, 0, 6), pierna_d=(30, 0, 0), muslo_i=(-50, 0, -6), pierna_i=(50, 0, 0), raiz=raiz(-2.0, 1.0))
    accion("embestida", 1.6, [
        (0.00, L2),
        (0.25, EM_A),
        (0.40, EM_B),
        (0.55, EM_A),
        (0.70, EM_B),
        (0.85, EM_A),
        (1.05, con(L2, raiz=raiz(-2.0, -1.0), torso=(30, 10, 0))),
        (1.60, L2),
    ], golpes=(0.85,))
    # ---- 9 derrame (la cuba): se inclina mucho y la cuba se le vuelca por encima de los hombros: el acido cae por delante
    DE_INCLINA = con(L2, raiz=raiz(-3.0, 1.6), torso=(70, 0, 0), cabeza=(-50, 0, 0), muslo_d=(-30, 0, 6), pierna_d=(40, 0, 0),
                     muslo_i=(-20, 0, -6), pierna_i=(36, 0, 0), brazo_d=(-30, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(-30, 0, -30),
                     antebrazo_i=(-20, 0, 0), cuba=(30, 0, 0))
    accion("derrame", 2.0, [
        (0.00, L2),
        (0.60, DE_INCLINA),
        (0.90, con(DE_INCLINA, cuba=(42, 0, 0))),
        (1.40, con(DE_INCLINA, torso=(66, 0, 0), cuba=(36, 0, 0))),
        (2.00, L2),
    ], golpes=(0.90,))
    # ---- 10 aturdido: se tambalea
    AT = dict(raiz=raiz(-1.2, 2.0), torso=(-20, 12, 10), cabeza=(-24, -16, 20),
              muslo_d=(-14, 0, 10), pierna_d=(30, 0, 0), muslo_i=(18, 0, -10), pierna_i=(20, 0, 0),
              brazo_d=(16, 0, 30), antebrazo_d=(-20, 0, 0), brazo_i=(18, 0, -30), antebrazo_i=(-16, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-6, 8, -12), cabeza=(-18, -10, -18), raiz=raiz(-2.0, 2.6))),
        (1.05, con(AT, torso=(-10, -8, 10), cabeza=(-22, 10, 14), raiz=raiz(-1.8, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 11 morir: de rodillas y de bruces (a la cuba se le revienta: lo pone el servidor)
    accion("morir", 1.8, [
        (0.00, con(BASE, torso=(-10, 0, 6), cabeza=(-30, 0, 10), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0))),
        (0.45, RODILLAS),
        (1.00, BRUCES),
        (1.80, con(BRUCES, cabeza=(-34, 34, 0))),
    ])
    # ---- 12 levantarse: estaba sentado en el suelo (el enfermo); se pone en pie
    accion("levantarse", 1.3, [
        (0.00, ENCOGIDO),
        (0.35, con(ENCOGIDO, raiz=raiz(-9.0, 1.0), torso=(40, 0, 0), cabeza=(-10, -20, 0))),
        (0.75, con(GUARDIA, raiz=raiz(-6.0, 0.4), torso=(40, 0, 0), muslo_d=(-90, 0, 6), pierna_d=(110, 0, 0), muslo_i=(-60, 0, -6),
                   pierna_i=(100, 0, 0))),
        (1.05, con(GUARDIA, raiz=raiz(-2.4), muslo_d=(-46, 0, 6), pierna_d=(64, 0, 0))),
        (1.30, L1),
    ])
    # ---- 13 volverse: estaba inclinado moliendo o jadeando; se endereza y se vuelve (el giro lo pone el servidor)
    accion("volverse", 0.9, [
        (0.00, con(BASE, torso=(30, 0, 0), cabeza=(10, 0, 0))),
        (0.30, con(BASE, torso=(20, 30, 0), cabeza=(-6, -30, 0))),
        (0.60, GUARDIA),
        (0.90, GUARDIA),
    ])
    # ---- 14 alzarse: estaba tendido boca arriba; se sienta, se pone de rodillas y en pie
    accion("alzarse", 2.0, [
        (0.00, TENDIDO),
        (0.45, con(TENDIDO, raiz=raiz(2.6, -12.0, rot=(-62, 0, 0)), cabeza=(12, 0, 0), brazo_d=(30, 0, 30), brazo_i=(30, 0, -30))),
        (0.85, dict(raiz=raiz(-9.4, 1.0), torso=(22, 0, 0), cabeza=(10, 0, 0), muslo_d=(-80, 0, 8), pierna_d=(40, 0, 0),
                    muslo_i=(-86, 0, -8), pierna_i=(90, 0, 0), brazo_d=(10, 0, 30), antebrazo_d=(-10, 0, 0), brazo_i=(-20, 0, -20),
                    antebrazo_i=(-40, 0, 0))),
        (1.30, con(RODILLAS, torso=(36, 0, 0), cabeza=(-20, 0, 0))),
        (1.70, con(GUARDIA, raiz=raiz(-2.6), muslo_d=(-46, 0, 6), pierna_d=(64, 0, 0))),
        (2.00, L1),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    if "enfermo" in nombre or nombre in ("zarpazo", "abalanzarse", "escupitajo", "encogido", "levantarse", "alzarse", "tendido"):
        return 1
    if "cuba" in nombre or nombre in ("punetazo", "embestida", "derrame", "jadeando"):
        return 2
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims[f"porte_{VARIANTES[v]}"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_mancebo"]]
    return []
