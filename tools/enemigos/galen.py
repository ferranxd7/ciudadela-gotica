"""
Maese Galen, el fisico del Lazareto (parte 7av): el jefe de la Sala del Elixir. Cuando la Peste Gris llego al Pantano Podrido, el
fisico al que la Academia echo por abrir a los muertos se llevo a los enfermos a su torre de la laguna y busco el elixir que los
curase. Lo que destilo no curaba: les cambiaba (los Destilados, parte 7au). La noche del cielo rojo hirvieron los alambiques, y el
con ellos: se conecto a su gran alambique y sigue destilando, esperando la ultima gota.

Alto, flaco y encorvado (el juego lo dibuja a 1,35): la cabeza calva con mechones blancos y lacios en la nuca, la cara chupada y
gris verdosa, la nariz larga y unas gafas redondas de cristal verde (lucen); el cuello alto del abrigo encerado de los fisicos de la
peste, abierto por delante sobre el mandil de cuero manchado; la mascara de pico colgando del cuello, sobre el pecho (ya no la
necesita); el cinto de los frascos; a la espalda, **su alambique**: una redoma de cristal en un bastidor de cobre, llena del elixir
verde (luce), con dos tubos que le entran en el cuello por los lados. En la derecha la **jeringa** enorme de laton y cristal (el
elixir dentro, luce; la aguja larga); en la izquierda el **bisturi**.

Dos variantes: 0, como esta; 1, despues de beberse el elixir a media vida: se ha arrancado los tubos (le cuelgan los cabos) y el
cristal le ha brotado por la espalda, los hombros, el brazo y la cabeza (lucen), la piel mas verde y con venas de cristal.

Sus golpes: la jeringa (la echa atras y la clava a fondo: envenena y le chupa la sangre), el bisturi (dos tajos con la izquierda),
los frascos (coge del cinto y lanza: llueven), el vapor (gira la llave de su alambique y se dobla: el vapor sale del suelo donde
estan los demas), el estallido (a media vida: se encoge y el cristal le revienta por fuera), prepararse (su rugido: alza la
jeringa, le da dos golpecitos con el bisturi y suelta un chorrito), beber (a media vida: se arranca los tubos, coge el frasco
grande del cinto y se lo bebe; se retuerce), volverse (esperaba de espaldas, destilando junto al gran alambique), aturdido y morir
(de rodillas, la mano tendida hacia el gran alambique).
"""
from anim import Anim, con, raiz, resolver, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "galen"
VARIANTES = ("galen", "bebido")
ESCALA = 1.35
CENTRO_Y = 1.5

#: el cristal que le brota al beberse el elixir y los cabos de los tubos arrancados (solo en la variante 1); los tubos enteros (solo
#: en la 0); el frasco grande que se bebe (lo ensena el modelo del juego mientras bebe)
CRISTAL = ["cristal_espalda", "cristal_hombro_d", "cristal_hombro_i", "cristal_brazo", "cristal_cabeza", "tubos_rotos"]
OCULTAS = {0: CRISTAL + ["frasco_grande"], 1: ["tubos", "frasco_grande"]}

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # flaco y alto (el brazo mide 14: la mano llega a unos 12 de su hombro)
    humanoide(r, torso=(8, 12, 5), cabeza=(7, 8, 7), brazo=(3, 14, 3), pierna=(3, 14, 3), hombro=0.5)
    # ---- la cabeza: los mechones blancos de la nuca y los lados, la nariz larga y las gafas redondas
    r.pieza("pelo", "cabeza", (0, 0, 0), boxes=[
        Box(-3.5, -6.6, -0.6, 7.0, 6.2, 4.1, "pelo", grow=g(0.35)),
        Box(-3.0, -1.0, 3.5, 6.0, 2.6, 0.6, "pelo_cola"),
    ])
    r.pieza("nariz", "cabeza", (0, 0, 0), boxes=[Box(-0.5, -4.4, -4.3, 1.0, 2.4, 0.9, "nariz")])
    r.pieza("gafas", "cabeza", (0, 0, 0), boxes=[
        Box(-3.2, -5.5, -3.95, 2.6, 2.2, 0.45, "lente"),
        Box(0.6, -5.5, -3.95, 2.6, 2.2, 0.45, "lente"),
        Box(-0.6, -5.0, -3.9, 1.2, 0.4, 0.35, "gafas_puente"),
        Box(-3.75, -5.0, -3.6, 0.35, 0.4, 3.4, "gafas_patilla"),
        Box(3.4, -5.0, -3.6, 0.35, 0.4, 3.4, "gafas_patilla"),
    ])
    # ---- el cuello alto del abrigo, el mandil, las correas del alambique, el cinto con sus frascos y la mascara de pico colgando
    r.pieza("cuello", "torso", (0, 0, 0), boxes=[Box(-4.3, -14.2, -2.8, 8.6, 2.8, 5.6, "cuello", grow=g(0.15))])
    r.pieza("mandil", "torso", (0, 0, 0), boxes=[Box(-3.6, -9.8, -2.95, 7.2, 10.0, 0.5, "mandil")])
    r.pieza("correas", "torso", (0, 0, 0), boxes=[
        Box(-3.6, -12.45, -2.85, 1.1, 1.0, 5.7, "correa"),
        Box(2.5, -12.45, -2.85, 1.1, 1.0, 5.7, "correa"),
        Box(-3.6, -12.0, -3.05, 1.1, 6.6, 0.4, "correa"),
        Box(2.5, -12.0, -3.05, 1.1, 6.6, 0.4, "correa"),
    ])
    r.pieza("cinto", "torso", (0, 0, 0), boxes=[
        Box(-4.4, -2.1, -2.9, 8.8, 1.3, 5.8, "cinto"),
        Box(1.2, -1.4, -3.8, 1.2, 2.4, 1.2, "vial_rojo"),
        Box(2.6, -1.4, -3.8, 1.2, 2.4, 1.2, "vial_verde"),
        Box(-3.8, -1.4, -3.8, 1.2, 2.4, 1.2, "vial_ambar"),
        Box(3.9, -2.6, -1.8, 1.4, 3.4, 3.6, "zurron"),
    ])
    r.pieza("mascara", "torso", (0, -8.6, -3.0), rot=(-12, 0, 0), boxes=[
        Box(-2.1, -1.8, -1.1, 4.2, 3.4, 1.1, "mascara"),
        Box(-0.9, 1.2, -1.9, 1.8, 5.2, 1.5, "mascara_pico"),
        Box(-1.9, -1.2, -1.35, 1.4, 1.4, 0.3, "mascara_lente"),
        Box(0.5, -1.2, -1.35, 1.4, 1.4, 0.3, "mascara_lente"),
    ])
    r.pieza("cordel", "torso", (0, 0, 0), boxes=[
        Box(-2.4, -12.2, -2.85, 0.4, 3.6, 0.3, "cordel"),
        Box(2.0, -12.2, -2.85, 0.4, 3.6, 0.3, "cordel"),
    ])
    # ---- el abrigo encerado hasta media pierna (abierto por delante: el mandil y las piernas) y las mangas anchas
    for n in ("d", "i"):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[
            Box(-2.5, -0.8, -1.35, 5.0, 8.2, 3.95, "faldon" if n == "d" else "faldon_i", uv="faldon" if n == "d" else "faldon_i")])
        r.pieza(f"bajo_{n}", f"pierna_{n}", (0, 0, 0), boxes=[
            Box(-2.6, 0.0, -1.3, 5.2, 4.6, 4.0, "bajo" if n == "d" else "bajo_i", uv="bajo" if n == "d" else "bajo_i")])
        r.pieza(f"mandil_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-1.9, 0.0, -2.1, 3.8, 8.6, 0.5, "mandil_" + n, uv="mandil_" + n)])
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 0.2, 0), boxes=[Box(-2.2, 0, -2.2, 4.4, 4.2, 4.4, "manga_" + n, uv="manga_" + n)])
    # ---- su alambique a la espalda: el bastidor de cobre, la redoma (casi todo transparente: se ve el elixir), los aros, el cuello
    # y la llave; el elixir de dentro aparte (luce)
    r.pieza("alambique", "torso", (0, -6.6, 2.5), boxes=[
        Box(-3.6, -5.2, 0.0, 7.2, 10.2, 0.9, "alambique_placa"),
        Box(-3.0, -4.4, 0.9, 6.0, 7.6, 5.0, "alambique_cristal"),
        Box(-3.3, -4.8, 0.7, 6.6, 0.8, 5.4, "alambique_aro"),
        Box(-3.3, 2.8, 0.7, 6.6, 0.8, 5.4, "alambique_aro"),
        Box(-1.0, -7.8, 2.4, 2.0, 3.4, 2.0, "alambique_cuello"),
        Box(-1.6, -8.8, 1.9, 3.2, 1.0, 3.0, "alambique_tapa"),
        Box(2.0, -7.2, 2.9, 2.4, 0.9, 0.9, "alambique_llave"),
    ])
    r.pieza("elixir", "alambique", (0, 0, 0), boxes=[Box(-2.6, -2.6, 1.3, 5.2, 5.6, 4.2, "elixir")])
    # los tubos (de la tapa, por los lados de la cabeza, le entran en el cuello) y, arrancados, sus cabos
    r.pieza("tubos", "torso", (0, 0, 0), boxes=[
        Box(3.65, -16.0, -0.2, 0.9, 0.9, 6.3, "tubo"),
        Box(3.65, -16.0, -0.6, 0.9, 3.6, 0.9, "tubo"),
        Box(-4.55, -16.0, -0.2, 0.9, 0.9, 6.3, "tubo"),
        Box(-4.55, -16.0, -0.6, 0.9, 3.6, 0.9, "tubo"),
        Box(-1.95, -16.0, 5.2, 3.9, 0.9, 0.9, "tubo"),
    ])
    r.pieza("tubos_rotos", "torso", (0, 0, 0), boxes=[
        Box(1.3, -15.4, 4.9, 0.9, 4.6, 0.9, "tubo_roto"),
        Box(-2.2, -15.4, 5.3, 0.9, 3.2, 0.9, "tubo_roto"),
        Box(3.55, -13.6, -0.6, 0.9, 1.4, 0.9, "tubo_cabo"),
        Box(-4.45, -13.6, -0.6, 0.9, 1.4, 0.9, "tubo_cabo"),
    ])
    # ---- el cristal que le brota (variante 1): por la espalda (entre el alambique y los hombros), los hombros, el brazo y la cabeza
    r.pieza("cristal_espalda", "torso", (0, 0, 0), boxes=[
        Box(-4.4, -11.8, 1.4, 1.6, 2.6, 1.6, "cristal"),
        Box(3.0, -10.0, 1.8, 1.4, 2.2, 1.4, "cristal"),
        Box(-4.6, -6.0, 0.8, 1.2, 1.8, 1.2, "cristal"),
        Box(-1.0, -1.6, 2.4, 1.6, 1.4, 1.4, "cristal"),
    ])
    r.pieza("cristal_hombro_d", "brazo_d", (0, 0, 0), boxes=[
        Box(-2.6, -1.6, -0.8, 1.4, 2.8, 1.6, "cristal"),
        Box(-2.0, 1.6, 0.6, 1.0, 1.8, 1.0, "cristal"),
    ])
    r.pieza("cristal_hombro_i", "brazo_i", (0, 0, 0), boxes=[
        Box(1.2, -1.8, -0.6, 1.6, 3.0, 1.6, "cristal"),
        Box(1.0, 2.0, -1.4, 1.0, 1.4, 1.0, "cristal"),
    ])
    r.pieza("cristal_brazo", "antebrazo_i", (0, 0, 0), boxes=[Box(1.0, 0.8, -0.4, 1.4, 2.2, 1.2, "cristal")])
    r.pieza("cristal_cabeza", "cabeza", (0, 0, 0), boxes=[
        Box(1.6, -9.6, 0.2, 1.4, 2.0, 1.4, "cristal"),
        Box(-3.0, -9.0, 1.4, 1.2, 1.4, 1.2, "cristal"),
        Box(-4.1, -3.4, -1.6, 0.8, 1.6, 1.2, "cristal"),
    ])
    # ---- la jeringa (mano derecha; apunta a -Z): el disco del embolo, el vastago, las aletas, el cristal con el elixir, la punta de
    # laton y la aguja
    r.pieza("jeringa", "mano_d", (0, 0, 0), boxes=[
        Box(-1.2, -1.2, 3.2, 2.4, 2.4, 0.6, "jeringa_disco"),
        Box(-0.4, -0.4, 0.4, 0.8, 0.8, 2.8, "jeringa_vastago"),
        Box(-2.0, -0.5, -0.4, 4.0, 1.0, 0.8, "jeringa_aletas"),
        Box(-1.3, -1.3, -9.4, 2.6, 2.6, 9.0, "jeringa_cristal"),
        Box(-1.5, -1.5, -10.4, 3.0, 3.0, 1.0, "jeringa_punta"),
        Box(-0.25, -0.25, -16.4, 0.5, 0.5, 6.0, "aguja"),
    ])
    r.pieza("jeringa_elixir", "jeringa", (0, 0, 0), boxes=[Box(-1.0, -1.0, -9.0, 2.0, 2.0, 6.6, "jeringa_elixir")])
    # ---- el bisturi (mano izquierda): el mango de hueso y la hoja larga
    r.pieza("bisturi", "mano_i", (0, 0, 0), boxes=[
        Box(-0.4, -0.4, -3.2, 0.8, 0.8, 3.6, "bisturi_mango"),
        Box(-0.15, -0.75, -7.8, 0.3, 1.5, 4.6, "bisturi_hoja"),
    ])
    # ---- el frasco grande que se bebe (mano izquierda; solo mientras bebe)
    r.pieza("frasco_grande", "mano_i", (0, 0.4, 0), boxes=[
        Box(-1.7, -1.7, -3.8, 3.4, 3.4, 3.8, "frasco_grande"),
        Box(-0.7, -0.7, -5.3, 1.4, 1.4, 1.6, "frasco_grande_cuello"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas

PIEL = (["#2e3430", "#444e47", "#5b675e", "#748077", "#8d998e", "#a8b2a5"],     # gris verdosa, chupada
        ["#25362b", "#375140", "#4b6c55", "#62886b", "#7ca383", "#99bf9c"])     # bebido: mas verde
PELO = ["#7d7a72", "#a29f95", "#c4c1b6", "#e2dfd4"]
ABRIGO = ["#0f0b08", "#18120e", "#221a14", "#2d231b", "#3a2e23"]
MANDIL = ["#24180f", "#342316", "#46301e", "#5a3f28", "#6d4e33"]
MASCARA = ["#2a1d13", "#3a2a1b", "#4c3725", "#5e4630", "#70573c"]
LATON = ["#3f3014", "#634c20", "#8a6c33", "#ae8d49", "#cfae66"]
COBRE = ["#3a1c10", "#5e2e18", "#8a4624", "#b05f34", "#cf7f4c"]
HIERRO = ["#26282c", "#43474e", "#6b7079", "#9aa0aa", "#cdd2da"]
HUESO = ["#7a705a", "#a0957a", "#c2b897", "#ddd4b4"]
ELIXIR = ["#0f3d1e", "#1a6a32", "#2a9a48", "#46c866", "#7ff09a"]
VERDE, VERDE_B = "#5cff86", "#c8ffd6"
VIDRIO, VIDRIO_B = "#a9d2bb", "#d6efe0"
PANO = ["#141311", "#1d1b18", "#27241f", "#322e28"]
BOTA = ["#0e0b09", "#191410", "#241d17"]

CARAS = {"arriba": 1, "abajo": 2, "der": 3, "frente": 4, "izq": 5, "espalda": 6}


def _azar01(*v):
    h = 2166136261
    for n in v:
        h = ((h ^ (int(n) & 0xffffffff)) * 16777619) & 0xffffffff
    h ^= h >> 13
    h = (h * 0x5bd1e995) & 0xffffffff
    h ^= h >> 15
    return h / 4294967296.0


def materiales(variante):
    bebido = variante == 1
    pieles = PIEL[1 if bebido else 0]
    piel_t = ruido(pieles[1:], 9401 + variante, grano=0.4, base=0.55)
    abrigo_t = tela(ABRIGO[1:], 9402, pliegue=3, grano=0.16, luz=0.12)
    mandil_t = ruido(MANDIL[1:], 9403, grano=0.35, base=0.5)
    laton_t = metal(LATON[1:], 9404)
    cobre_t = metal(COBRE[1:], 9405)
    hierro_t = metal(HIERRO[1:], 9406)
    pano_t = tela(PANO, 9407, pliegue=2, grano=0.2, luz=0.1)

    def vena(c, cara, x, y, w, h, sal):
        """Bebido, venas de cristal verde por la piel (finas, quebradas)."""
        if not bebido:
            return c
        k = (x * 3 + y * 5 + CARAS[cara] * 7 + sal) % 17
        if k == 0 or (k == 9 and _azar01(sal, x, y) < 0.5):
            return mezcla(c, ELIXIR[3], 0.55)
        return c

    def mancha(c, cara, x, y, w, h, sal):
        """Lo que se le ha derramado encima: alguna mancha verde y alguna oscura."""
        a = _azar01(sal, CARAS[cara], x // 2, y // 2)
        if a < 0.025:
            return mezcla(c, ELIXIR[2], 0.45)
        if a < 0.055:
            return oscuro(c, 0.3)
        return c

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            S, s, k = pieles[4], pieles[3], pieles[1]
            C = {"W": PELO[3], "w": PELO[2], "H": claro(pieles[4], 0.08), "S": S, "s": s, "D": PELO[1], "K": "#0d100e",
                 "k": k, "N": pieles[3], "C": pieles[2], "c": pieles[1], "L": "#3a2422", "m": "#140c0b", "b": PELO[2],
                 "G": ELIXIR[3]}
            filas = [
                "WHHHHHW",
                "wSSsSSw",
                "sDDsDDs",
                "sKksKKs",
                "sssNsss",
                "cCsNsCc",
                "sLmmmLs",
                "bsSbSsb",
            ]
            if bebido:
                filas[5] = "cGsNsCG"
                filas[1] = "wSGsSSw"
            v = dibujo(filas, C)(cara, x, y, w, h)
            return v if v is not None else S
        if cara == "arriba":
            c = piel_t(cara, x, y, w, h)
            return PELO[1] if (x in (0, w - 1) or y >= h - 2) and (x + y) % 2 == 0 else vena(claro(c, 0.06), cara, x, y, w, h, 3)
        if cara == "abajo":
            return pieles[1]
        c = piel_t(cara, x, y, w, h)
        if cara in ("der", "izq") and y in (3, 4) and delante(cara, x, w) == 2:
            return pieles[1]                                     # la sien hundida
        return vena(c, cara, x, y, w, h, 4)

    def pelo(cara, x, y, w, h):
        if cara in ("abajo", "frente"):
            return None
        if cara == "arriba":
            return None if (x + y) % 3 else PELO[2]               # calvo por arriba: algun pelo suelto
        # los mechones lacios: rayas verticales, mas ralos arriba
        if y < 2 and (x + CARAS[cara]) % 2 == 0:
            return None
        if (x * 5 + CARAS[cara]) % 7 == 0:
            return None
        return PELO[1 + (x + y // 3) % 3]

    def pelo_cola(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if y >= h - 1 and x % 2:
            return None
        return PELO[2 if (x + y) % 2 else 1]

    def nariz(cara, x, y, w, h):
        c = pieles[3] if cara != "abajo" else pieles[1]
        if cara == "frente" and y == h - 1:
            return pieles[2]
        return vena(c, cara, x, y, w, h, 5)

    def lente(cara, x, y, w, h):
        if cara == "frente" and 0 < x < w - 1 and 0 < y < h - 1:
            return VERDE if (x + y) % 3 else VERDE_B
        return LATON[2]

    def torso(cara, x, y, w, h):
        if cara == "abajo":
            return ABRIGO[1]
        if cara == "arriba":
            return ABRIGO[2]
        c = abrigo_t(cara, x, y, w, h)
        if cara == "frente":
            # el abrigo abierto: por el medio, la camisa vieja (gris) y el mandil la tapa casi entera
            if 2 <= x <= w - 3:
                return mancha(pano_t(cara, x, y, w, h), cara, x, y, w, h, 6)
            if x in (1, w - 2):
                return claro(c, 0.08)                            # el borde del abrigo
        if cara == "espalda" and y % 4 == 0:
            return oscuro(c, 0.2)
        return mancha(c, cara, x, y, w, h, 7)

    def brazo(cara, x, y, w, h):
        c = abrigo_t(cara, x, y, w, h)
        if (x + y * 3 + CARAS[cara]) % 19 == 0:
            return oscuro(c, 0.3)
        return mancha(c, cara, x, y, w, h, 8)

    def antebrazo(cara, x, y, w, h):
        # la manga del abrigo hasta la muneca y la mano gris (los dedos largos)
        if y >= h - 3:
            c = piel_t(cara, x, y, w, h)
            if y == h - 1:
                return pieles[2] if (x + CARAS[cara]) % 2 else pieles[3]
            return vena(c, cara, x, y, w, h, 9)
        return abrigo_t(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return pano_t(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        # el calzon y la bota alta (de abajo a la rodilla)
        if y >= h - 5:
            c = BOTA[(x + y) % 3]
            return claro(c, 0.1) if y == h - 5 else c
        return pano_t(cara, x, y, w, h)

    def cuello(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= h - 2 else ABRIGO[3]
        c = abrigo_t(cara, x, y, w, h)
        if cara == "frente" and w // 2 - 1 <= x <= w // 2:
            return None                                          # abierto por delante
        return claro(c, 0.1) if y == 0 else c

    def mandil(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return MANDIL[1]
        c = mandil_t(cara, x, y, w, h)
        if cara == "frente":
            if y == 0 or x in (0, w - 1):
                return oscuro(c, 0.25)                           # la costura
            # lo derramado: un chorreon verde y las de sangre seca
            if 3 <= x <= 4 and y >= 4 and (y + x) % 3:
                return mezcla(c, ELIXIR[2], 0.6)
            if (x * 7 + y * 3) % 13 == 0:
                return "#3a1612"
        return mancha(c, cara, x, y, w, h, 10)

    def mandil_bajo(n):
        def f(cara, x, y, w, h):
            if cara in ("arriba", "abajo"):
                return MANDIL[1]
            if y >= h - 1 and (x + (1 if n == "d" else 0)) % 2:
                return None                                      # el bajo roto
            c = mandil_t(cara, x, y, w, h)
            if cara == "frente" and (x * 5 + y * 3 + (2 if n == "d" else 0)) % 11 == 0:
                return mezcla(c, ELIXIR[2], 0.5)
            return mancha(c, cara, x, y, w, h, 11 if n == "d" else 12)
        return f

    def correa(cara, x, y, w, h):
        return MASCARA[2] if (x + y) % 3 else MASCARA[1]

    def cinto(cara, x, y, w, h):
        if cara == "frente" and w // 2 - 1 <= x <= w // 2:
            return LATON[3] if y == 1 else LATON[2]              # la hebilla
        return MASCARA[1 + (x + y) % 2]

    def vial(color):
        def f(cara, x, y, w, h):
            if y == 0:
                return "#6b4a2a"                                 # el tapon de corcho
            if cara == "arriba":
                return "#6b4a2a"
            return claro(color, 0.15) if x == 0 else color
        return f

    def zurron(cara, x, y, w, h):
        c = MASCARA[1 + (x * 3 + y) % 3]
        return oscuro(c, 0.3) if y == 0 else c

    def mascara(cara, x, y, w, h):
        c = MASCARA[2 + (x + y) % 2]
        if cara == "frente" and y == h - 1:
            return oscuro(c, 0.25)
        return c

    def mascara_pico(cara, x, y, w, h):
        c = MASCARA[3 if x % 2 else 2]
        if (y * 3 + x) % 5 == 0:
            return oscuro(c, 0.3)                                # las costuras del pico
        return c

    def mascara_lente(cara, x, y, w, h):
        return "#2f5a3e" if cara == "frente" else LATON[1]       # el cristal apagado (ya no luce)

    def cordel(cara, x, y, w, h):
        return MASCARA[1] if y % 2 else MASCARA[0]

    def faldon(n):
        def f(cara, x, y, w, h):
            if cara in ("abajo", "frente"):
                return None                                      # abierto por delante
            if cara == "arriba":
                return ABRIGO[2]
            c = abrigo_t(cara, x, y, w, h)
            if cara in ("der", "izq") and delante(cara, x, w) == 0:
                return claro(c, 0.08)                            # el borde de delante
            return mancha(c, cara, x, y, w, h, 13 if n == "d" else 14)
        return f

    def bajo(n):
        def f(cara, x, y, w, h):
            if cara in ("abajo", "frente", "arriba"):
                return None
            corte = h - 1 - ((x * 5 + CARAS[cara] * 3 + (1 if n == "d" else 0)) % 2)
            if y > corte:
                return None                                      # el bajo gastado
            c = abrigo_t(cara, x, y, w, h)
            return oscuro(mancha(c, cara, x, y, w, h, 15), 0.05 * y)
        return f

    def manga(n):
        def f(cara, x, y, w, h):
            if cara in ("arriba", "abajo"):
                return None
            c = abrigo_t(cara, x, y, w, h)
            if y == h - 1:
                return claro(c, 0.1)                             # el puno de la manga
            return mancha(c, cara, x, y, w, h, 16 if n == "d" else 17)
        return f

    def alambique_placa(cara, x, y, w, h):
        c = cobre_t(cara, x, y, w, h)
        if (x in (0, w - 1) or y in (0, h - 1)) and (x + y) % 2 == 0:
            return COBRE[4]                                      # los remaches
        return c

    def alambique_cristal(cara, x, y, w, h):
        # casi todo transparente (se ve el elixir); los montantes y un reflejo
        if cara in ("arriba", "abajo"):
            return None
        if x == 0 or x == w - 1:
            return COBRE[2] if y % 3 else COBRE[3]
        if x == 1 and 1 < y < h - 2:
            return VIDRIO_B
        if x == 2 and 2 < y < h - 3 and y % 2:
            return VIDRIO
        return None

    def alambique_aro(cara, x, y, w, h):
        return cobre_t(cara, x, y, w, h) if (x + y) % 5 else COBRE[4]

    def alambique_cuello(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return COBRE[2]
        return VIDRIO if x == 0 else ELIXIR[2] if y > 0 else VIDRIO_B

    def alambique_tapa(cara, x, y, w, h):
        return cobre_t(cara, x, y, w, h)

    def alambique_llave(cara, x, y, w, h):
        return LATON[3] if x == w - 1 else LATON[2]

    def elixir(cara, x, y, w, h):
        k = 4 if y < 1 else (3 if y < 3 else 2)
        if (x * 7 + y * 3 + CARAS[cara]) % 9 == 0:
            return ELIXIR[min(4, k + 1)]                         # las burbujas
        return ELIXIR[k]

    def tubo(cara, x, y, w, h):
        c = "#3b2e24" if (x + y) % 4 else "#56463a"              # goma vieja
        return c

    def tubo_roto(cara, x, y, w, h):
        if y >= h - 1:
            return ELIXIR[2]                                     # gotea
        return "#3b2e24" if (x + y) % 3 else "#56463a"

    def tubo_cabo(cara, x, y, w, h):
        return "#5a1a14" if y == h - 1 else "#3b2e24"            # donde estaba metido

    def cristal(cara, x, y, w, h):
        if y == 0 or (x + y) % 3 == 0:
            return VERDE_B if (x + y) % 2 else ELIXIR[4]
        return ELIXIR[3] if (x * 3 + y) % 2 else ELIXIR[2]

    def jeringa_disco(cara, x, y, w, h):
        return laton_t(cara, x, y, w, h)

    def jeringa_vastago(cara, x, y, w, h):
        return HIERRO[3] if x % 2 else HIERRO[2]

    def jeringa_aletas(cara, x, y, w, h):
        return LATON[3] if x in (0, w - 1) else LATON[2]

    def jeringa_cristal(cara, x, y, w, h):
        # el cristal (se ve el elixir) con los montantes de laton a lo largo y las rayas de medida; los extremos, de laton
        if cara in ("frente", "espalda"):
            return LATON[2]
        if cara in ("der", "izq"):
            borde, largo, ancho = y in (0, h - 1), x, y
        else:
            borde, largo, ancho = x in (0, w - 1), y, x
        if borde:
            return LATON[2] if largo % 3 else LATON[3]
        if largo % 2 == 0 and ancho == 1:
            return "#e6f0e8"                                     # las rayas de medida
        return None

    def jeringa_elixir(cara, x, y, w, h):
        return ELIXIR[3] if (x + y) % 3 else ELIXIR[4]

    def jeringa_punta(cara, x, y, w, h):
        return laton_t(cara, x, y, w, h)

    def aguja(cara, x, y, w, h):
        return HIERRO[4] if (x + y) % 2 else HIERRO[3]

    def bisturi_mango(cara, x, y, w, h):
        return HUESO[1 + (x + y) % 3]

    def bisturi_hoja(cara, x, y, w, h):
        c = hierro_t(cara, x, y, w, h)
        if cara in ("der", "izq") and y == h - 1:
            return HIERRO[4]                                     # el filo
        if (x + y * 3) % 7 == 0:
            return "#5a1a14"                                     # sangre seca
        return c

    def frasco_grande(cara, x, y, w, h):
        if cara == "arriba":
            return VIDRIO
        if x == 0:
            return VIDRIO_B
        return ELIXIR[3] if y > 0 else ELIXIR[4]

    def frasco_grande_cuello(cara, x, y, w, h):
        return "#6b4a2a" if y == 0 else VIDRIO

    mats = {
        "cabeza": sombra(cabeza, abajo=0.08), "pelo": pelo, "pelo_cola": pelo_cola, "nariz": nariz,
        "lente": lente, "gafas_puente": liso(LATON[2]), "gafas_patilla": liso(LATON[1]),
        "torso": sombra(torso), "brazo_d": sombra(brazo, abajo=0.12), "brazo_i": sombra(brazo, abajo=0.12),
        "antebrazo_d": sombra(antebrazo, abajo=0.1), "antebrazo_i": sombra(antebrazo, abajo=0.1),
        "muslo_d": sombra(muslo), "muslo_i": sombra(muslo), "pierna_d": sombra(pierna, abajo=0.1), "pierna_i": sombra(pierna, abajo=0.1),
        "cuello": sombra(cuello, abajo=0.1), "mandil": sombra(mandil, abajo=0.1), "correa": correa, "cinto": cinto,
        "vial_rojo": vial("#8a2018"), "vial_verde": vial(ELIXIR[3]), "vial_ambar": vial("#b07a1e"), "zurron": sombra(zurron, abajo=0.1),
        "mascara": sombra(mascara, abajo=0.12), "mascara_pico": sombra(mascara_pico, abajo=0.12), "mascara_lente": mascara_lente,
        "cordel": cordel,
        "faldon": sombra(faldon("d"), abajo=0.1), "faldon_i": sombra(faldon("i"), abajo=0.1),
        "bajo": sombra(bajo("d"), abajo=0.12), "bajo_i": sombra(bajo("i"), abajo=0.12),
        "mandil_d": sombra(mandil_bajo("d"), abajo=0.1), "mandil_i": sombra(mandil_bajo("i"), abajo=0.1),
        "manga_d": sombra(manga("d"), abajo=0.1), "manga_i": sombra(manga("i"), abajo=0.1),
        "alambique_placa": sombra(alambique_placa, abajo=0.1), "alambique_cristal": alambique_cristal, "alambique_aro": alambique_aro,
        "alambique_cuello": alambique_cuello, "alambique_tapa": alambique_tapa, "alambique_llave": alambique_llave, "elixir": elixir,
        "tubo": tubo, "tubo_roto": tubo_roto, "tubo_cabo": tubo_cabo, "cristal": cristal,
        "jeringa_disco": jeringa_disco, "jeringa_vastago": jeringa_vastago, "jeringa_aletas": jeringa_aletas,
        "jeringa_cristal": jeringa_cristal, "jeringa_elixir": jeringa_elixir, "jeringa_punta": jeringa_punta, "aguja": aguja,
        "bisturi_mango": bisturi_mango, "bisturi_hoja": bisturi_hoja,
        "frasco_grande": frasco_grande, "frasco_grande_cuello": frasco_grande_cuello,
    }
    return mats


def brillo(variante):
    """Lo que luce: las gafas, el elixir (del alambique, de la jeringa y del frasco) y, bebido, el cristal y las venas."""
    bebido = variante == 1

    def lente(cara, x, y, w, h):
        if cara == "frente" and 0 < x < w - 1 and 0 < y < h - 1:
            return VERDE if (x + y) % 3 else VERDE_B
        return None

    def elixir(cara, x, y, w, h):
        return ELIXIR[4] if (x * 7 + y * 3) % 9 == 0 else ELIXIR[3]

    def cara(cara_, x, y, w, h):
        if not bebido or cara_ != "frente":
            return None
        return ELIXIR[3] if (y == 5 and x in (1, 6)) or (y == 1 and x == 2) else None

    b = {
        "lente": lente,
        "elixir": elixir,
        "jeringa_elixir": lambda c, x, y, w, h: ELIXIR[3] if (x + y) % 3 else ELIXIR[4],
        "alambique_cuello": lambda c, x, y, w, h: ELIXIR[3] if c not in ("arriba", "abajo") and x != 0 and y > 0 else None,
        "frasco_grande": lambda c, x, y, w, h: ELIXIR[3] if c != "arriba" and x != 0 else None,
        "vial_verde": lambda c, x, y, w, h: ELIXIR[3] if y > 0 and c != "arriba" else None,
        "tubo_roto": lambda c, x, y, w, h: ELIXIR[3] if y >= h - 1 else None,
    }
    if bebido:
        b["cristal"] = lambda c, x, y, w, h: VERDE_B if (x + y) % 2 else VERDE
        b["cabeza"] = cara
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo llega a unos 12).
# _arma = hacia donde apunta la aguja de la jeringa (a -Z de la mano derecha); _arma_i = hacia donde apunta la hoja del bisturi.

# de pie: encorvado, la cabeza adelantada (mira por encima de las gafas)
BASE = dict(raiz=raiz(-0.4), torso=(12, 0, 0), cabeza=(-8, 0, 0), muslo_d=(-4, 0, 3), pierna_d=(8, 0, 0), muslo_i=(-1, 0, -3),
            pierna_i=(6, 0, 0))
# lo que lleva: la jeringa colgando a su derecha, la aguja abajo y adelante; el bisturi en la izquierda, junto al muslo
PORTE = dict(_mano_d=("h", 0.8, -9.6, 3.0), _arma=(0.1, -0.8, 0.6), _mano_i=("h", -0.6, -10.0, 2.6), _arma_i=(0.0, -0.9, 0.45))
# en guardia: agachado, la jeringa por delante como un estoque y el bisturi alzado
GUARDIA = dict(raiz=raiz(-1.4), torso=(20, 10, 0), cabeza=(-16, -8, 0), muslo_d=(-14, 0, 6), pierna_d=(22, 0, 0), muslo_i=(2, 0, -6),
               pierna_i=(16, 0, 0))
LISTO = con(GUARDIA, _mano_d=("h", -1.2, -3.6, 7.6), _arma=(0.05, 0.05, 1.0), _mano_i=("h", 2.0, -2.6, 6.0), _arma_i=(0.25, 0.55, 0.8))
# destilando (REZA): de cara al gran alambique, la jeringa alzada ante las gafas, mirandola al trasluz
DE = dict(raiz=raiz(-0.2), torso=(6, 0, 0), cabeza=(-10, 0, 0), muslo_d=(-2, 0, 3), pierna_d=(4, 0, 0), muslo_i=(0, 0, -3),
          pierna_i=(4, 0, 0))
DE_ALTA = con(DE, cabeza=(-16, -6, 0), _mano_d=("h", 3.0, 2.0, 6.6), _arma=(0.0, 1.0, 0.1), _mano_i=("h", -0.6, -9.4, 3.4),
              _arma_i=(0.0, -0.9, 0.4))
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

    # ---- quieto: respira despacio, con un temblor de la cabeza (mira a un lado y a otro por encima de las gafas)
    q0 = dict(BASE)
    q1 = con(BASE, torso=(15, 0, 0), cabeza=(-10, 6, 0), raiz=raiz(-0.7))
    q2 = con(BASE, torso=(13, 0, 0), cabeza=(-6, -6, 0))
    bucle("quieto", 4.0, [(0.0, q0), (1.3, q1), (2.6, q2), (4.0, q0)])
    # ---- andar: a pasos largos y rigidos (se suma al quieto y al porte, o a listo)
    pasos = []
    for k, (md, mi, pd, pi_, ty) in enumerate(((-20, 16, 8, 12, 5), (-4, 3, 6, 22, 0), (16, -20, 12, 8, -5), (3, -4, 22, 6, 0),
                                               (-20, 16, 8, 12, 5))):
        pasos.append((1.4 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (3, ty, 0), "cabeza": (0, -ty * 0.5, 0), "raiz": raiz(0.3 if k % 2 else 0.0)}))
    bucle("andar", 1.4, pasos)
    # el porte: la jeringa se mece un poco
    p0 = fija(con(BASE, PORTE))
    p1 = fija(con(BASE, PORTE, _arma=(0.2, -0.75, 0.62)))
    p2 = fija(con(BASE, PORTE, _arma=(-0.05, -0.85, 0.5)))
    bucle("porte", 3.0, [(0.0, p0), (1.0, p1), (2.0, p2), (3.0, p0)], solo=BRAZOS)
    # en guardia: la aguja busca, el bisturi tiembla
    l0 = fija(LISTO)
    l1 = fija(con(LISTO, raiz=raiz(-1.8), torso=(LISTO["torso"][0] + 3, LISTO["torso"][1] - 4, 0), _arma=(0.15, 0.1, 0.98),
                  _arma_i=(0.35, 0.6, 0.7)))
    l2 = fija(con(LISTO, _arma=(-0.12, -0.02, 0.99), _mano_i=("h", 2.4, -2.0, 6.4)))
    bucle("listo", 1.6, [(0.0, l0), (0.55, l1), (1.1, l2), (1.6, l0)])
    # ---- destilando (REZA): alza la jeringa al trasluz, la mira, suelta una gota y la baja al gran alambique (cada 4 s)
    d_alta = fija(DE_ALTA)
    d_mira = fija(con(DE_ALTA, cabeza=(-20, 8, 0), _mano_d=("h", 2.4, 2.6, 6.2)))
    d_gota = fija(con(DE_ALTA, cabeza=(-14, 0, 0), _mano_d=("h", 2.6, 2.2, 7.0), _arma=(0.0, 0.96, 0.25)))
    d_baja = fija(con(DE, torso=(22, 0, 0), cabeza=(6, 0, 0), _mano_d=("h", 0.4, -3.6, 9.6), _arma=(0.0, -0.7, 0.72),
                      _mano_i=("h", -1.6, -6.0, 8.0), _arma_i=(0.0, -0.8, 0.6)))
    bucle("destilando", 4.0, [(0.0, d_alta), (0.8, d_mira), (1.6, d_gota), (2.4, d_baja), (3.3, d_baja), (4.0, d_alta)])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 la jeringa: la echa atras junto a la oreja (la aguja adelante) y se lanza a fondo
    J_ATRAS = con(L, raiz=raiz(-1.0, 1.4), torso=(6, 30, 0), cabeza=(-14, -22, 0), muslo_d=(6, 0, 6), pierna_d=(14, 0, 0),
                  muslo_i=(-20, 0, -6), pierna_i=(16, 0, 0), _mano_d=("h", 0.6, 1.2, -1.6), _arma=(0.05, 0.1, 0.99),
                  _mano_i=("h", 3.4, -2.0, 6.4), _arma_i=(0.3, 0.6, 0.75))
    J_FONDO = con(L, raiz=raiz(-2.6, -3.4), torso=(30, -24, 0), cabeza=(-30, 18, 0), muslo_d=(-46, 0, 6), pierna_d=(40, 0, 0),
                  muslo_i=(20, 0, -6), pierna_i=(8, 0, 0), _mano_d=("h", -1.4, -1.6, 11.4), _arma=(0.02, -0.06, 1.0),
                  _mano_i=("h", 4.0, -6.0, 1.0), _arma_i=(0.2, -0.8, 0.5))
    accion("jeringa", 1.7, [
        (0.00, L),
        (0.55, J_ATRAS),
        (0.92, con(J_ATRAS, raiz=raiz(-1.2, 1.8), torso=(4, 34, 0))),
        (1.05, J_FONDO),
        (1.40, con(J_FONDO, _mano_d=("h", -1.2, -1.8, 11.0))),
        (1.70, L),
    ], golpes=(1.05,))
    # ---- 2 el bisturi: dos tajos con la izquierda (de su izquierda a su derecha, y de vuelta)
    B_ARMA = con(L, raiz=raiz(-1.2), torso=(14, -30, 0), cabeza=(-16, 18, 0), _mano_i=("h", 7.0, 5.6, 2.4), _arma_i=(0.55, 0.8, -0.2),
                 _mano_d=("h", -0.4, -8.0, 4.0), _arma=(0.05, -0.6, 0.8))
    B_UNO = con(L, raiz=raiz(-1.8, -0.8), torso=(24, 30, 0), cabeza=(-22, -18, 0), _mano_i=("h", -6.4, -2.6, 8.4),
                _arma_i=(-0.85, -0.35, 0.4), _mano_d=("h", -0.4, -8.0, 4.0), _arma=(0.05, -0.6, 0.8))
    B_VUELTA = con(L, raiz=raiz(-1.6, -1.2), torso=(22, -28, 0), cabeza=(-20, 16, 0), _mano_i=("h", 7.6, 0.6, 7.2),
                   _arma_i=(0.9, 0.1, 0.4), _mano_d=("h", -0.4, -8.0, 4.0), _arma=(0.05, -0.6, 0.8))
    accion("bisturi", 1.6, [
        (0.00, L),
        (0.40, B_ARMA),
        (0.55, B_UNO),
        (0.78, con(B_UNO, torso=(26, 30, 0), _mano_i=("h", -4.0, -2.2, 6.4), _arma_i=(-0.6, 0.6, 0.5))),
        (0.95, B_VUELTA),
        (1.25, con(B_VUELTA, torso=(20, -24, 0))),
        (1.60, L),
    ], golpes=(0.55, 0.95))
    # ---- 3 los frascos: baja la izquierda al cinto, la echa atras por encima del hombro y lanza (los frascos salen en el golpe)
    F_COGE = con(L, raiz=raiz(-1.6), torso=(26, 10, 0), cabeza=(-10, -6, 0), _mano_i=("h", 0.4, -11.4, 2.6), _arma_i=(0.0, -1.0, 0.2))
    F_ATRAS = con(L, raiz=raiz(-0.6, 1.0), torso=(-6, -22, 0), cabeza=(-24, 16, 0), muslo_i=(-16, 0, -6), pierna_i=(12, 0, 0),
                  _mano_i=("h", 1.0, 7.6, -3.6), _arma_i=(0.0, 0.7, -0.7))
    F_LANZA = con(L, raiz=raiz(-1.8, -1.6), torso=(30, 18, 0), cabeza=(-28, -12, 0), muslo_d=(-26, 0, 6), pierna_d=(24, 0, 0),
                  _mano_i=("h", -1.0, 2.0, 11.0), _arma_i=(0.0, 0.3, 0.95))
    accion("frascos", 2.0, [
        (0.00, L),
        (0.45, F_COGE),
        (0.85, F_ATRAS),
        (1.00, F_LANZA),
        (1.35, con(F_LANZA, torso=(34, 20, 0), _mano_i=("h", -1.4, -2.0, 10.0))),
        (2.00, L),
    ], golpes=(1.0,))
    # ---- 4 el vapor: lleva la izquierda atras por encima del hombro y gira la llave de su alambique; se dobla y alza la jeringa
    V_LLAVE = con(BASE, raiz=raiz(-0.6), torso=(4, -14, 0), cabeza=(-6, 20, 0), _mano_i=("h", -2.6, 8.4, -2.0), _arma_i=(-0.3, 0.9, -0.3),
                  _mano_d=("h", 1.0, -7.0, 5.0), _arma=(0.1, -0.5, 0.86))
    V_GIRA = con(V_LLAVE, _mano_i=("h", -2.0, 8.8, -2.6), _arma_i=(0.5, 0.8, -0.3), cabeza=(-10, 24, 0))
    V_SUELTA = con(L, raiz=raiz(-2.4, -0.6), torso=(36, 0, 0), cabeza=(-30, 0, 0), _mano_d=("h", -2.0, 5.0, 8.0), _arma=(0.0, 0.8, 0.6),
                   _mano_i=("h", 4.6, -6.0, 6.0), _arma_i=(0.4, -0.7, 0.6))
    accion("vapor", 1.8, [
        (0.00, L),
        (0.35, V_LLAVE),
        (0.60, V_GIRA),
        (0.85, V_SUELTA),
        (1.30, con(V_SUELTA, torso=(32, 0, 0), cabeza=(-34, 0, 0))),
        (1.80, L),
    ], golpes=(0.85,))
    # ---- 5 el estallido (bebido): se encoge, los brazos cruzados al pecho, y se abre de golpe (el cristal le revienta por fuera)
    E_ENCOGE = con(BASE, raiz=raiz(-3.2), torso=(40, 0, 0), cabeza=(30, 0, 0), muslo_d=(-30, 0, 6), pierna_d=(46, 0, 0), muslo_i=(-30, 0, -6),
                   pierna_i=(46, 0, 0), _mano_d=("h", 4.6, -2.0, 4.0), _arma=(0.6, -0.6, 0.5), _mano_i=("h", -4.6, -2.0, 4.0),
                   _arma_i=(-0.6, -0.6, 0.5))
    E_ABRE = con(BASE, raiz=raiz(0.4), torso=(-18, 0, 0), cabeza=(-34, 0, 0), muslo_d=(-4, 0, 10), pierna_d=(6, 0, 0), muslo_i=(-4, 0, -10),
                 pierna_i=(6, 0, 0), _mano_d=("h", -9.0, 3.0, 3.0), _arma=(-0.8, 0.5, 0.3), _mano_i=("h", 9.0, 3.0, 3.0), _arma_i=(0.8, 0.5, 0.3))
    accion("estallido", 1.9, [
        (0.00, L),
        (0.55, E_ENCOGE),
        (1.10, con(E_ENCOGE, raiz=raiz(-3.6), torso=(46, 0, 0))),
        (1.25, E_ABRE),
        (1.55, con(E_ABRE, torso=(-20, 0, 0))),
        (1.90, L),
    ], golpes=(1.25,))
    # ---- 6 prepararse (su rugido): alza la jeringa ante las gafas, le da dos golpecitos con el bisturi y suelta un chorrito
    P_ALTA = con(BASE, raiz=raiz(-0.3), torso=(4, 0, 0), cabeza=(-20, -6, 0), _mano_d=("h", 3.4, 3.0, 6.4), _arma=(0.0, 1.0, 0.05))
    P_TOCA = con(P_ALTA, _mano_i=("h", -2.0, 2.6, 7.0), _arma_i=(0.55, 0.3, 0.75))
    P_APARTA = con(P_ALTA, _mano_i=("h", -3.0, 1.0, 6.0), _arma_i=(0.2, 0.5, 0.8))
    accion("preparar", 2.0, [
        (0.00, L),
        (0.40, con(P_ALTA, _mano_i=("h", -0.6, -9.0, 3.0), _arma_i=(0.0, -0.9, 0.4))),
        (0.70, P_APARTA),
        (0.80, P_TOCA),
        (0.90, P_APARTA),
        (1.00, P_TOCA),
        (1.10, P_APARTA),
        (1.45, con(P_ALTA, cabeza=(-24, -10, 0), _mano_d=("h", 3.0, 3.6, 6.0), _mano_i=("h", -0.6, -9.0, 3.0), _arma_i=(0.0, -0.9, 0.4))),
        (2.00, L),
    ], golpes=(1.45,))
    # ---- 7 beber (a media vida): se arranca los tubos del cuello (la izquierda), coge el frasco grande del cinto y se lo bebe con la
    # cabeza echada atras; lo tira y se retuerce
    T_COGE = con(BASE, raiz=raiz(-0.4), torso=(4, 10, 0), cabeza=(-6, 0, 18), _mano_i=("h", -2.4, 7.6, 1.2), _arma_i=(0.0, 1.0, 0.0),
                 _mano_d=("h", 1.4, -8.0, 4.0), _arma=(0.1, -0.7, 0.7))
    T_TIRA = con(BASE, raiz=raiz(-1.0), torso=(18, -20, 0), cabeza=(-12, 14, -10), _mano_i=("h", 7.0, 2.0, 5.0), _arma_i=(0.8, -0.2, 0.5),
                 _mano_d=("h", 1.4, -8.0, 4.0), _arma=(0.1, -0.7, 0.7))
    BE_COGE = con(BASE, raiz=raiz(-1.6), torso=(26, 0, 0), cabeza=(0, 0, 0), _mano_i=("h", 0.4, -11.2, 2.8), _arma_i=(0.0, -1.0, 0.2),
                  _mano_d=("h", 1.4, -8.0, 4.0), _arma=(0.1, -0.7, 0.7))
    BE_BEBE = con(BASE, raiz=raiz(0.2), torso=(-14, 0, 0), cabeza=(-40, 0, 0), _mano_i=("h", -2.2, 7.6, 3.4), _arma_i=(-0.1, 0.9, -0.4),
                  _mano_d=("h", 1.0, -9.0, 3.0), _arma=(0.1, -0.8, 0.6))
    BE_RETUERCE = con(BASE, raiz=raiz(-2.6, 0.4), torso=(44, 14, 10), cabeza=(26, -10, 16), muslo_d=(-24, 0, 8), pierna_d=(34, 0, 0),
                      muslo_i=(-6, 0, -8), pierna_i=(28, 0, 0), _mano_d=("h", 3.0, -6.0, 5.0), _arma=(0.3, -0.8, 0.5),
                      _mano_i=("h", -3.0, 0.0, 6.0), _arma_i=(-0.4, 0.2, 0.9))
    accion("beber", 4.6, [
        (0.00, L),
        (0.35, T_COGE),
        (0.60, con(T_COGE, cabeza=(-8, 6, 22))),
        (0.80, T_TIRA),
        (1.20, BE_COGE),
        (1.70, con(BE_BEBE, cabeza=(-20, 0, 0), _mano_i=("h", -1.6, 6.4, 5.4), _arma_i=(0.0, 0.7, 0.7))),
        (2.10, BE_BEBE),
        (3.40, con(BE_BEBE, cabeza=(-46, 0, 0), torso=(-18, 0, 0))),
        (3.70, con(BASE, raiz=raiz(-0.4), torso=(10, -20, 0), cabeza=(-10, 16, 0), _mano_i=("h", 7.4, 0.0, 4.4), _arma_i=(0.9, -0.3, 0.3),
                   _mano_d=("h", 1.0, -9.0, 3.0), _arma=(0.1, -0.8, 0.6))),
        (4.00, BE_RETUERCE),
        (4.30, con(BE_RETUERCE, torso=(40, -14, -12), cabeza=(22, 12, -16))),
        (4.60, L),
    ])
    # ---- 8 volverse (esperaba destilando junto al gran alambique): baja la jeringa y se pone en guardia (el giro lo pone el servidor)
    accion("volverse", 1.3, [
        (0.00, d_alta),
        (0.35, con(DE_ALTA, cabeza=(-6, -20, 0), torso=(8, -14, 0))),
        (0.80, con(L, raiz=raiz(-0.8), torso=(14, 4, 0), cabeza=(-12, 6, 0))),
        (1.30, L),
    ])
    # ---- 9 aturdido
    AT = dict(raiz=raiz(-1.0, 2.0), torso=(-10, 0, 8), cabeza=(-22, 0, 14), muslo_d=(-8, 0, 8), pierna_d=(20, 0, 0),
              muslo_i=(12, 0, -8), pierna_i=(16, 0, 0), _mano_d=("h", -3.0, -6.0, 3.0), _arma=(-0.4, -0.85, 0.3),
              _mano_i=("h", 4.0, -3.0, 3.0), _arma_i=(0.6, -0.6, 0.4))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-1.6, 2.6))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.4, 2.2))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 10 morir: se tambalea, cae de rodillas tendiendo la mano hacia el gran alambique y se dobla
    RODILLAS = dict(raiz=raiz(-6.4, 0.6), torso=(24, 0, 0), cabeza=(10, 0, 0), muslo_d=(-10, 0, 4), pierna_d=(96, 0, 0),
                    muslo_i=(-80, 0, -4), pierna_i=(80, 0, 0))
    TIENDE = con(RODILLAS, torso=(18, 0, 0), cabeza=(-16, 0, 0), _mano_i=("h", -1.0, 1.0, 11.0), _arma_i=(0.0, 0.2, 0.98),
                 _mano_d=("h", 0.6, -11.0, 4.0), _arma=(0.1, -0.3, 0.95))
    CAE = con(RODILLAS, torso=(58, 0, 0), cabeza=(40, 0, 0), _mano_i=("h", -1.0, -9.0, 8.0), _arma_i=(0.0, -0.4, 0.9),
              _mano_d=("h", 0.6, -11.0, 5.0), _arma=(0.1, -0.2, 0.97))
    accion("morir", 4.5, [
        (0.00, con(BASE, PORTE, torso=(-12, 0, 6), cabeza=(-30, 0, 10))),
        (0.55, con(BASE, raiz=raiz(-1.6, 1.0), torso=(20, 0, -8), cabeza=(8, 0, -12), _mano_d=("h", 0.6, -9.0, 5.0),
                   _arma=(0.1, -0.6, 0.8), _mano_i=("h", 0.0, -6.0, 6.0), _arma_i=(0.0, -0.8, 0.5))),
        (1.25, TIENDE),
        (2.30, con(TIENDE, _mano_i=("h", -1.0, 2.0, 11.4))),
        (3.20, CAE),
        (4.50, con(CAE, torso=(64, 0, 0), cabeza=(46, 0, 0))),
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
