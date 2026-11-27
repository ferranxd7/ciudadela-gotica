"""
Anselmo, el Pontifice Sangrante (parte 7t): el jefe de la Tierra sin Fe. Era el obispo que guiaba a los peregrinos al
crater a pedir perdon; la noche del cielo rojo la Sombra les contesto, y el lo entendio como una orden: que dieran su
sangre. Desde entonces se llama a si mismo Pontifice y los lleva al noroeste, al Altar de la Sangre (libro 28: "se los
lleva al noroeste, al Altar de la Sangre"), donde reza de rodillas ante el altar profanado con el caliz lleno. El juego lo
dibuja a 1,45: un viejo altisimo y enjuto.

La cara chupada y cerulea, las cuencas hundidas con una luz roja y dos lagrimas de sangre que le bajan a la barba rala; la
tiara de tres coronas (la seda blanca manchada de sangre que le chorrea de arriba, las coronas de oro con sus flores, la bola
y la cruz) con las infulas rojas a la nuca; la muceta roja con el armino y el palio blanco con sus cruces negras, el broche de
oro; la capa pluvial carmesi con la orla de oro, rasgada por la espalda (asoma la espalda en carne viva, a latigazos); el alba
blanca hasta los pies empapada de sangre de abajo arriba, las mangas anchas forradas de rojo y las zapatillas rojas. En la
derecha la ferula, la cruz de tres travesanos en lo alto de la vara de oro; en la izquierda el caliz de oro lleno de sangre
(que se le derrama por el borde y luce un poco).

Sus golpes: el ferulazo (alza la ferula y la estrella delante), el barrido (a media altura), la aspersion (echa la sangre del
caliz en abanico delante: hace sangrar), la comunion (alza el caliz: la sangre de quien este sangrando cerca vuela a el y le
cura), el juicio (alza la ferula al cielo y la clava: brotan columnas de sangre bajo los pies de los marcados), el sermon (al
empezar) y la uncion a media vida (se vierte el caliz por la cabeza: desde entonces sangra y deja charcos, y sale la
procesion).
"""
from anim import Anim, con, raiz, resolver
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, liso, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "pontifice"
VARIANTES = ("anselmo",)
OCULTAS = {0: []}
#: alto y enjuto: lo que le da su dibujante en el juego
ESCALA = 1.45
CENTRO_Y = 1.6

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # alto y flaco: el torso estrecho, la cabeza estrecha, los brazos largos y delgados (la mano llega a unos 11 de su hombro)
    humanoide(r, torso=(8, 13, 4), cabeza=(7, 8, 7), brazo=(3, 14, 3), pierna=(4, 13, 4))
    # ---- la tiara: tres pisos de seda que se estrechan, la bola y la cruz arriba, y las tres coronas de oro (sobresalen)
    r.pieza("tiara", "cabeza", (0, -7.6, 0), boxes=[
        Box(-3.8, -3.2, -3.8, 7.6, 3.2, 7.6, "tiara_1"),
        Box(-3.4, -6.2, -3.4, 6.8, 3.0, 6.8, "tiara_2"),
        Box(-2.9, -8.9, -2.9, 5.8, 2.7, 5.8, "tiara_3"),
        Box(-2.1, -10.5, -2.1, 4.2, 1.6, 4.2, "tiara_4"),
        Box(-0.8, -11.7, -0.8, 1.6, 1.2, 1.6, "orbe"),
        Box(-0.35, -14.1, -0.35, 0.7, 2.4, 0.7, "cruz_tiara"),
        Box(-1.1, -13.5, -0.35, 2.2, 0.7, 0.7, "cruz_tiara", uv="cruz_tiara_b"),
        Box(-4.1, -1.3, -4.1, 8.2, 1.3, 8.2, "corona"),
        Box(-3.7, -4.4, -3.7, 7.4, 1.2, 7.4, "corona", uv="corona_2"),
        Box(-3.2, -7.2, -3.2, 6.4, 1.1, 6.4, "corona", uv="corona_3"),
    ])
    # las infulas (las dos cintas rojas que caen de la tiara por la nuca)
    r.pieza("infulas", "cabeza", (0, -7.4, 3.5), rot=(10, 0, 0), boxes=[
        Box(-2.6, 0, 0, 1.6, 9.5, 0.45, "infula"),
        Box(1.0, 0, 0, 1.6, 9.5, 0.45, "infula"),
    ])
    # la barba rala (cana, con la sangre de las lagrimas)
    r.pieza("barba", "cabeza", (0, -2.0, -3.5), rot=(-4, 0, 0), boxes=[Box(-2.6, 0, -0.8, 5.2, 4.6, 0.9, "barba")])
    # ---- la muceta (la esclavina roja con el armino abajo y el palio por encima), el broche y la tira del palio por delante
    r.pieza("muceta", "torso", (0, -13, 0), boxes=[Box(-5.0, -0.5, -2.8, 10.0, 6.0, 5.6, "muceta", grow=g(0.25))])
    r.pieza("broche", "torso", (0, -11.4, -3.15), boxes=[Box(-1.3, 0, -0.5, 2.6, 2.2, 0.6, "broche")])
    r.pieza("palio", "torso", (0, -9.2, -3.0), boxes=[Box(-0.8, 0, -0.35, 1.6, 8.6, 0.35, "palio")])
    # ---- la capa pluvial, de los hombros al suelo por detras (rasgada por el medio: se le ve la espalda)
    r.pieza("capa", "torso", (0, -12.6, 2.3), rot=(4, 0, 0), boxes=[Box(-5.2, 0, 0, 10.4, 25.4, 0.8, "capa")])
    # ---- el alba hasta los pies (cada pierna su faldon; el izquierdo un poco mas estrecho, que no se pisen) y las mangas anchas
    for n, a in (("d", 2.7), ("i", 2.65)):
        r.pieza(f"alba_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-a, -0.6, -a, 2 * a, 7.2, 2 * a, f"alba_{n}")])
        r.pieza(f"alba_bajo_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-a + 0.1, 0, -a + 0.1, 2 * a - 0.2, 5.9, 2 * a - 0.2, f"alba_bajo_{n}")])
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 1.0, 0), boxes=[Box(-2.3, 0, -2.3, 4.6, 5.0, 4.6, f"manga_{n}")])
    # ---- la ferula (mano derecha; con el brazo caido apunta a -Z hacia arriba): la vara de oro, el regaton, el nudo y la
    # cruz de tres travesanos (el de arriba el mas corto)
    r.pieza("ferula", "mano_d", (0, 0, 0), boxes=[
        Box(-0.7, -0.7, -21.0, 1.4, 1.4, 34.0, "vara"),
        Box(-0.95, -0.95, 13.0, 1.9, 1.9, 1.2, "regaton"),
        Box(-1.2, -1.2, -22.8, 2.4, 2.4, 1.8, "nudo"),
        Box(-0.65, -0.65, -35.0, 1.3, 1.3, 12.2, "cruz_ferula"),
        Box(-2.3, -0.65, -33.6, 4.6, 1.3, 1.1, "travesano_1"),
        Box(-3.3, -0.65, -31.0, 6.6, 1.3, 1.1, "travesano_2"),
        Box(-4.3, -0.65, -28.4, 8.6, 1.3, 1.1, "travesano_3"),
    ])
    # ---- el caliz (mano izquierda; derecho con _arma_i hacia arriba): el pie, el tallo en el puno, el nudo, la copa y la sangre
    r.pieza("caliz", "mano_i", (0, 0, 0), boxes=[
        Box(-1.5, -1.5, 0.4, 3.0, 3.0, 0.7, "caliz_pie"),
        Box(-0.5, -0.5, -1.4, 1.0, 1.0, 1.8, "caliz_tallo"),
        Box(-0.9, -0.9, -2.0, 1.8, 1.8, 0.6, "caliz_nudo"),
        Box(-1.8, -1.8, -4.8, 3.6, 3.6, 2.8, "caliz_copa"),
        Box(-1.5, -1.5, -4.95, 3.0, 3.0, 0.3, "caliz_sangre"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas
# La piel cerulea y chupada; la seda blanca (amarillenta de vieja) del alba y de la tiara; el carmesi de la muceta, la capa y
# las infulas; el oro de las coronas, el broche, la ferula y el caliz; la sangre (seca, oscura, y fresca, viva); la carne de
# la espalda.

PIEL = ["#3e3530", "#5a4d45", "#776a5f", "#968879", "#b3a593", "#cdc0ad"]
SEDA = ["#6d665a", "#89826f", "#a59d87", "#beb69f", "#d6cfb8", "#e9e3cf"]
ROJO = ["#2c0508", "#42080d", "#5c0d13", "#77131a", "#931c23", "#ad2830"]
SANGRE = ["#2a0607", "#45090c", "#620e12", "#801519", "#9e2026", "#b8323a"]
CARNE = ["#3d0a0c", "#5e1216", "#7e1c20", "#9b2a2c", "#b5413f"]
ORO = ["#4e3a12", "#73591c", "#9c7a26", "#c49d36", "#e2c25a", "#f4e08e"]
ARMINO = ["#c9c3b3", "#e3ddcd", "#f3efe4"]
NEGRO = "#141012"
CUENCA = "#0a0505"
OJO, OJO_B = "#ff4a2e", "#ffc2a6"
GEMA = ["#5a0710", "#8c0d1c", "#c41f33"]
ZAFIRO = ["#101a4a", "#1d2f7a"]
CANA = ["#8f8a80", "#aaa59a", "#c6c1b6", "#dedad0"]


def _cara():
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": CUENCA, "k": PIEL[0], "E": OJO, "R": SANGRE[3],
         "r": SANGRE[2], "M": "#1a0a0a"}
    return dibujo([
        "sSSSSSs",
        "ooSsSoo",
        "oKEsEKo",
        "okRsRko",
        "OoRsRoO",
        "OkRMRkO",
        "ooRMRoo",
        "oorrroo",
    ], C)


def _gota(x, sem, largo):
    """Lo largo del chorreon de sangre de la columna x (0 si no hay): para las cosas que chorrean de arriba abajo."""
    k = (x * 7 + sem * 13) % 11
    if k in (0, 4):
        return largo
    if k == 7:
        return max(1, largo // 2)
    return 0


def materiales(variante):
    piel = ruido(PIEL[1:5], 1201, grano=0.4, base=0.5)
    seda = tela(SEDA, 1211, pliegue=3, grano=0.12, luz=0.18)
    rojo = tela(ROJO, 1221, pliegue=3, grano=0.14)
    carne = ruido(CARNE, 1231, grano=0.5)
    cara = _cara()
    CI = {"frente": 0, "espalda": 5, "der": 9, "izq": 3, "arriba": 7, "abajo": 1}

    def empapada(c, cara_, x, y, h, sem, alto):
        """La sangre que sube del bajo: oscura y seca abajo, mas clara arriba, a lenguas."""
        k = (x * 5 + sem * 7 + CI.get(cara_, 0) * 3) % 7
        lim = h - 1 - alto - (k % 3) + (1 if k == 0 else 0)
        if y >= lim:
            t = (y - lim) / max(1.0, h - lim)
            return mezcla(SANGRE[2 + (x + y) % 2], SANGRE[0], min(1.0, t * 0.9))
        if y == lim - 1 and k in (1, 4):
            return mezcla(c, SANGRE[3], 0.6)
        return c

    def salpicada(c, cara_, x, y, sem, pct=9):
        k = (x * 11 + y * 7 + sem + CI.get(cara_, 0) * 5) % 37
        if k < pct * 37 // 100:
            return mezcla(c, SANGRE[3 + (k % 2)], 0.7)
        return c

    def cabeza(cara_, x, y, w, h):
        if cara_ == "frente":
            return cara(cara_, x, y, w, h)
        c = piel(cara_, x, y, w, h)
        if cara_ == "arriba":
            return oscuro(c, 0.1)
        if cara_ in ("der", "izq"):
            d = delante(cara_, x, w)
            if d >= 4 or y <= 1:
                return CANA[1 + (x + y) % 3]                   # el pelo cano por detras de las orejas
            if y == 3 and d == 3:
                return PIEL[1]                                 # la oreja
            if 3 <= y <= 5 and d == 1:
                return oscuro(c, 0.25)                         # la mejilla hundida
            if y >= 5 and d <= 1:
                return CANA[(x + y) % 3]                       # la barba por la quijada
            return c
        if cara_ == "espalda":
            # el pelo cano y ralo, largo hasta la nuca
            return CANA[(x * 3 + y) % 4] if (x + y * 3) % 5 else oscuro(CANA[1], 0.2)
        return c

    def barba(cara_, x, y, w, h):
        if cara_ != "frente":
            return oscuro(CANA[1], 0.2) if cara_ in ("der", "izq", "abajo") else None
        largo = (3, 4, 4, 3, 4, 3)[x % 6]
        if y > largo:
            return None
        if y == 0 and 1 <= x <= 3:
            return None                                       # la boca
        if x in (1, 3) and y <= 2:
            return SANGRE[3] if y < 2 else SANGRE[2]         # las lagrimas de sangre que bajan
        return CANA[(x * 2 + y) % 4] if (x + y) % 3 else CANA[0]

    def tiara(alto_gota, sem):
        def f(cara_, x, y, w, h):
            if cara_ == "abajo":
                return SEDA[1]
            c = seda(cara_, x, y, w, h)
            if cara_ == "arriba":
                return claro(c, 0.04)
            g_ = _gota(x, sem + CI.get(cara_, 0), alto_gota)
            if g_ and y < g_:
                return SANGRE[3] if y < g_ - 1 else SANGRE[4]
            # el bordado de oro (una cenefa de puntos)
            if y == h - 1 and x % 2 == 0:
                return ORO[3]
            return c
        return f

    def corona(cara_, x, y, w, h):
        if cara_ == "arriba":
            return ORO[3] if (x in (0, w - 1) or y in (0, h - 1)) else None
        if cara_ == "abajo":
            return ORO[1]
        # las flores de lis arriba (una si, una no), las piedras en medio
        if y == 0:
            return ORO[4] if x % 3 == 1 else None
        if y == h - 1:
            return ORO[1]
        if x % 4 == 2:
            return GEMA[2] if (x // 4) % 2 == 0 else ZAFIRO[1]
        return ORO[3] if (x + y) % 2 else ORO[2]

    def infula(cara_, x, y, w, h):
        c = rojo(cara_, x, y, w, h)
        if y >= h - 1:
            return ORO[3] if x % 2 == 0 else None             # el fleco de oro
        if cara_ in ("espalda", "frente") and y in (3, 4) and x in (0, 1):
            return ORO[4] if (y == 3 or x == 0) else ORO[2]  # la crucecita bordada
        return empapada(c, cara_, x, y, h, 3, 1)

    def muceta(cara_, x, y, w, h):
        c = rojo(cara_, x, y, w, h)
        if cara_ == "abajo":
            return None
        if cara_ == "arriba":
            # el palio: el aro de lana blanca con sus cruces negras, por encima de los hombros
            if 1 <= x <= w - 2 and 1 <= y <= h - 2 and not (2 <= x <= w - 3 and 2 <= y <= h - 3):
                return NEGRO if (x + y) % 4 == 0 else ARMINO[2]
            if 2 <= x <= w - 3 and 2 <= y <= h - 3:
                return None                                   # el hueco del cuello
            return c
        if y >= h - 1:
            return ARMINO[(x + y) % 2 + 1] if (x * 3 + y) % 5 else NEGRO   # el armino con sus colas negras
        if y <= 1:
            # el palio por el pecho y la espalda
            if cara_ in ("frente", "espalda"):
                return NEGRO if (x % 4 == 2 and y == 0) or (x % 4 in (1, 2, 3) and y == 1 and x % 4 == 2) else ARMINO[2 - y]
            return ARMINO[1]
        return salpicada(c, cara_, x, y, 5, 6)

    def broche(cara_, x, y, w, h):
        if cara_ != "frente":
            return ORO[2]
        if 1 <= x <= w - 2 and 1 <= y <= h - 2:
            return GEMA[2] if (x + y) % 2 else GEMA[1]
        return ORO[4] if (x + y) % 2 else ORO[3]

    def palio(cara_, x, y, w, h):
        if cara_ != "frente":
            return ARMINO[0]
        # cruces negras cada tres filas; la ultima, el remate de plomo
        if y == h - 1:
            return "#3a3a40"
        if y % 4 == 1:
            return NEGRO
        if y % 4 in (0, 2) and x == 0:
            return NEGRO
        return ARMINO[2] if (x + y) % 3 else ARMINO[1]

    def capa(cara_, x, y, w, h):
        if cara_ in ("arriba", "abajo"):
            return ROJO[1]
        c = rojo(cara_, x, y, w, h)
        if cara_ in ("espalda", "frente"):
            # rasgada por el medio de arriba a media espalda (asoma la espalda), y el bajo hecho jirones
            medio = abs(x - (w - 1) / 2.0)
            tajo = 1.6 if 4 <= y <= 13 else (0.8 if y in (3, 14, 15) else 0)
            if medio < tajo:
                return None
            jiron = (0, 1, 0, 2, 1, 0, 3, 1, 0, 2, 0)[x % 11]
            if y >= h - jiron:
                return None
            if x in (0, w - 1) and cara_ == "espalda":
                return ORO[3] if y % 3 else ORO[2]           # la orla de oro
            if cara_ == "espalda" and x in (1, w - 2):
                return ORO[1]
            if medio < tajo + 1:
                return mezcla(c, SANGRE[2], 0.6)              # los bordes del tajo, ensangrentados
            if cara_ == "frente":
                return ROJO[0] if (x + y) % 4 else ROJO[1]   # el forro
            return empapada(c, cara_, x, y, h, 9, 5)
        return c

    def torso(cara_, x, y, w, h):
        # el alba por delante y por los lados (salpicada de sangre en el pecho); la espalda, en carne viva a latigazos
        if cara_ == "espalda":
            c = carne(cara_, x, y, w, h)
            if (x + 2 * y) % 6 == 0 or (2 * x - y) % 7 == 0:
                return CARNE[4] if (x + y) % 2 else SANGRE[5]  # los verdugones abiertos
            if (x * 3 + y) % 5 == 0:
                return CARNE[0]
            return c
        c = seda(cara_, x, y, w, h)
        if cara_ == "frente":
            # el cingulo rojo en la cintura y la sangre que le ha caido del caliz por el pecho
            if y in (h - 3, h - 2):
                return ROJO[3] if y == h - 3 else ROJO[2]
            if 2 <= x <= 5 and 2 <= y <= 8 and (x * 3 + y * 5) % 7 < 3:
                return SANGRE[3 + (x + y) % 2]
            return salpicada(c, cara_, x, y, 11, 12)
        if cara_ in ("der", "izq") and delante(cara_, x, w) >= 3:
            return mezcla(c, CARNE[2], 0.55)                  # la carne viva asoma por los costados de la espalda
        return salpicada(c, cara_, x, y, 13, 8)

    def brazo(cara_, x, y, w, h):
        return salpicada(seda(cara_, x, y, w, h), cara_, x, y, 17, 10)

    def antebrazo_(derecho):
        def f(cara_, x, y, w, h):
            # el brazo flaco (lo tapa casi todo la manga) y la mano huesuda; en la derecha, el anillo del pescador
            c = piel(cara_, x, y, w, h)
            if y >= h - 2:
                if derecho and y == h - 2 and cara_ in ("frente", "izq" if derecho else "der") and x == 1:
                    return ORO[4]
                return mezcla(c, SANGRE[3], 0.35) if (x + y) % 3 == 0 else c
            if y < 3:
                return seda(cara_, x, y, w, h)
            return oscuro(c, 0.1) if (x + y) % 2 else c
        return f

    def muslo(cara_, x, y, w, h):
        return seda(cara_, x, y, w, h)

    def pierna(cara_, x, y, w, h):
        # las zapatillas rojas (lo unico que asoma del alba)
        if y >= h - 2 or cara_ == "abajo":
            return ROJO[4] if (x + y) % 3 else ORO[3] if y == h - 2 and x == 1 and cara_ == "frente" else ROJO[3]
        return seda(cara_, x, y, w, h)

    def alba(sem, alto):
        def f(cara_, x, y, w, h):
            if cara_ == "arriba":
                return None
            if cara_ == "abajo":
                return SANGRE[1]
            c = seda(cara_, x, y, w, h)
            return salpicada(empapada(c, cara_, x, y, h, sem, alto), cara_, x, y, sem, 10)
        return f

    def alba_bajo(sem):
        def f(cara_, x, y, w, h):
            if cara_ == "arriba":
                return None
            if cara_ == "abajo":
                return SANGRE[0]
            c = seda(cara_, x, y, w, h)
            # empapada casi entera: la sangre sube desde el suelo hasta la rodilla
            k = (x * 3 + sem + CI.get(cara_, 0)) % 5
            if y >= 1 + (k % 3):
                return mezcla(SANGRE[1 + (x + y) % 3], SANGRE[0], min(1.0, y / max(1.0, h)))
            return mezcla(c, SANGRE[3], 0.35)
        return f

    def manga(cara_, x, y, w, h):
        if cara_ == "arriba":
            return None
        if cara_ == "abajo":
            return ROJO[3] if not (1 <= x <= w - 2 and 1 <= y <= h - 2) else ROJO[1]   # el forro rojo por dentro
        c = seda(cara_, x, y, w, h)
        if y >= h - 1:
            return ORO[3] if x % 2 else ORO[2]                 # la cenefa de oro del borde
        return salpicada(empapada(c, cara_, x, y, h, 21, 1), cara_, x, y, 21, 12)

    def vara(cara_, x, y, w, h):
        c = metal(ORO[0:5], 1241)(cara_, x, y, w, h)
        # sangre seca a trozos por la vara, mas por arriba (donde chorrea de la cruz)
        if cara_ in ("der", "izq", "arriba", "abajo"):
            k = x if cara_ in ("der", "izq") else y
            if (k < 14 and (k * 5 + CI[cara_]) % 4 == 0) or (k * 7 + CI[cara_]) % 13 == 0:
                return SANGRE[2 + k % 2]
        return c

    def cruz_ferula(cara_, x, y, w, h):
        c = metal(ORO[1:6], 1242)(cara_, x, y, w, h)
        if (x * 3 + y * 5 + CI.get(cara_, 0)) % 9 == 0:
            return SANGRE[3]
        return c

    def travesano(cara_, x, y, w, h):
        c = metal(ORO[1:6], 1243)(cara_, x, y, w, h)
        # la sangre que gotea de cada travesano (por la cara de abajo cuando la cruz esta derecha)
        if cara_ in ("frente", "espalda") and x % 3 == 1:
            return SANGRE[3 + (x // 3) % 2]
        return c

    def caliz_copa(cara_, x, y, w, h):
        if cara_ == "arriba":
            return SANGRE[4]
        c = metal(ORO[1:6], 1251)(cara_, x, y, w, h)
        # la sangre que se derrama por el borde (chorreones desde arriba: con la copa derecha, la cara de -Z es la de arriba)
        if cara_ in ("der", "izq", "frente", "espalda"):
            g_ = _gota(x, 4 + CI.get(cara_, 0), 3)
            if g_ and y < g_:
                return SANGRE[4] if y == 0 else SANGRE[3]
            if y == h - 1:
                return ORO[4]                                  # el borde de la copa (con el caliz derecho, el de arriba)
        return c

    def caliz_sangre(cara_, x, y, w, h):
        return SANGRE[5] if (x + y) % 3 == 0 else SANGRE[4]

    return {
        "cabeza": sombra(cabeza, abajo=0.12),
        "barba": barba,
        "tiara_1": tiara(3, 1), "tiara_2": tiara(2, 2), "tiara_3": tiara(2, 3), "tiara_4": tiara(1, 4),
        "orbe": metal(ORO[1:6], 1261), "cruz_tiara": metal(ORO[2:6], 1262),
        "corona": corona,
        "infula": infula,
        "muceta": sombra(muceta, abajo=0.1),
        "broche": broche,
        "palio": palio,
        "capa": capa,
        "torso": sombra(torso),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo_(True), abajo=0.1), "antebrazo_i": sombra(antebrazo_(False), abajo=0.1),
        "muslo_d": muslo, "muslo_i": muslo,
        "pierna_d": sombra(pierna, abajo=0.1), "pierna_i": sombra(pierna, abajo=0.1),
        "alba_d": sombra(alba(1, 2), abajo=0.08), "alba_i": sombra(alba(2, 2), abajo=0.08),
        "alba_bajo_d": alba_bajo(3), "alba_bajo_i": alba_bajo(4),
        "manga_d": sombra(manga, abajo=0.1), "manga_i": sombra(manga, abajo=0.1),
        "vara": vara, "regaton": metal(ORO[0:4], 1244), "nudo": metal(ORO[1:6], 1245),
        "cruz_ferula": cruz_ferula,
        "travesano_1": travesano, "travesano_2": travesano, "travesano_3": travesano,
        "caliz_pie": metal(ORO[1:5], 1252), "caliz_tallo": metal(ORO[1:5], 1253), "caliz_nudo": metal(ORO[2:6], 1254),
        "caliz_copa": caliz_copa,
        "caliz_sangre": caliz_sangre,
    }


def brillo(variante):
    """Lo que luce: los ojos (rojos, como los de los penitentes) y, poco, la sangre del caliz."""
    C = {"E": OJO, "e": OJO_B}
    ojos = dibujo([
        ".......",
        ".......",
        "..E.E..",
    ], C)
    return {
        "cabeza": por_cara(liso(None), frente=ojos),
        "caliz_sangre": lambda cara, x, y, w, h: SANGRE[5] if (x + y) % 2 == 0 else None,
    }


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 14: llega
# a unos 11); _arma = hacia donde apunta la cruz de la ferula (su -Z); _arma_i = hacia donde apunta la boca del caliz.

ARRIBA = (0.0, 1.0, 0.0)
# de pie: alto, algo cargado de hombros pero la cabeza alta; la ferula de pie a su derecha y el caliz ante el pecho
BASE = dict(raiz=raiz(-0.2), torso=(6, 0, 0), cabeza=(-6, 0, 0), capa=(-4, 0, 0), infulas=(-8, 0, 0),
            muslo_d=(-2, 0, 2), pierna_d=(3, 0, 0), muslo_i=(1, 0, -2), pierna_i=(3, 0, 0),
            _mano_d=("h", -1.6, -9.0, 4.6), _arma=(0.0, 1.0, 0.04),
            _mano_i=("h", -3.8, -4.6, 6.6), _arma_i=ARRIBA)
# en guardia: de medio lado, la ferula cruzada delante (la cruz arriba y adelante), el caliz a su izquierda
LISTO = dict(raiz=raiz(-1.0), torso=(12, -12, 0), cabeza=(-8, 10, 0), capa=(-8, 0, 0), infulas=(-12, 0, 0),
             muslo_d=(-16, 0, 4), pierna_d=(14, 0, 0), muslo_i=(10, 0, -5), pierna_i=(12, 0, 0),
             _mano_d=("h", 1.8, -5.6, 7.6), _arma=(0.25, 0.75, 0.6),
             _mano_i=("h", 3.6, -5.4, 4.4), _arma_i=(0.1, 1.0, 0.0))
# de rodillas ante el altar: la ferula de pie a su derecha, el caliz alzado ante la cara, la cabeza baja (asi espera)
RODILLAS = dict(raiz=raiz(-6.4, 0.8), torso=(14, 0, 0), cabeza=(24, 0, 0), capa=(12, 0, 0), infulas=(8, 0, 0),
                muslo_d=(-6, 0, 4), pierna_d=(92, 0, 0), muslo_i=(-6, 0, -4), pierna_i=(92, 0, 0))
REZANDO = con(RODILLAS, _mano_d=("h", -1.4, -3.2, 5.8), _arma=(0.0, 1.0, 0.05),
              _mano_i=("h", -3.6, 2.0, 7.4), _arma_i=(0.0, 1.0, 0.1))


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

    # ---- quieto: respira despacio, alza un poco el caliz y mira por encima de todos
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (2.5, con(BASE, torso=(4, 0, 0), cabeza=(-10, 4, 0), raiz=raiz(0.0), _mano_i=("h", -3.6, -3.8, 6.8))),
        (5.0, BASE),
    ])
    # ---- andar: a paso de procesion, lento y solemne; la capa se mece
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate(((-20, 16, 6, 18, 4, 0.0), (-4, 2, 4, 30, 0, 0.6), (16, -20, 18, 6, -4, 0.0),
                                                   (2, -4, 30, 4, 0, 0.6), (-20, 16, 6, 18, 4, 0.0))):
        pasos.append((2.2 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, -ty * 0.3), "cabeza": (-6, -ty * 0.5, 0), "raiz": raiz(up),
                                    "capa": (-3 + abs(md) * 0.25, 0, 0), "infulas": (-8 + abs(md) * 0.2, 0, 0)}))
    bucle("andar", 2.2, pasos)
    l0 = fija(LISTO)
    l1 = fija(con(LISTO, raiz=raiz(-1.5), torso=(15, -14, 0), cabeza=(-10, 12, 2), _mano_i=("h", 3.8, -5.0, 4.2)))
    bucle("listo", 2.4, [(0.0, l0), (1.2, l1), (2.4, l0)])
    # ---- de rodillas, rezando: casi quieto (la cabeza baja un poco mas y vuelve)
    rz = fija(REZANDO)
    bucle("rezando", 6.0, [(0.0, rz), (3.0, dict(rz, cabeza=(30, 0, 0), torso=(16, 0, 0))), (6.0, rz)])

    # ================================================================ acciones
    L = LISTO

    # ---- 1 ferulazo: alza la ferula por encima de la cabeza (la cruz atras) y la estrella delante, la cruz contra el suelo
    FE_ALTO = con(L, raiz=raiz(0.3, 1.4), torso=(-12, 6, 0), cabeza=(-20, -4, 0), capa=(16, 0, 0), infulas=(14, 0, 0),
                  muslo_d=(8, 0, 4), pierna_d=(8, 0, 0), muslo_i=(-12, 0, -5), pierna_i=(10, 0, 0),
                  _mano_d=("h", 0.6, 10.4, 0.6), _arma=(0.05, 0.35, -0.94), _mano_i=("h", 4.2, -4.0, 2.6))
    FE_ABAJO = con(L, raiz=raiz(-3.6, -2.6), torso=(40, 0, 0), cabeza=(-24, 0, 0), capa=(-20, 0, 0), infulas=(-26, 0, 0),
                   muslo_d=(12, 0, 4), pierna_d=(26, 0, 0), muslo_i=(-46, 0, -5), pierna_i=(50, 0, 0),
                   _mano_d=("h", 1.2, -2.4, 10.2), _arma=(0.0, -0.55, 0.84), _mano_i=("h", 4.6, -2.0, -1.6))
    accion("ferulazo", 2.2, [
        (0.00, L),
        (0.72, FE_ALTO),
        (0.98, con(FE_ALTO, raiz=raiz(0.5, 1.7), torso=(-16, 6, 0), cabeza=(-24, -4, 0))),
        (1.06, con(L, raiz=raiz(-1.6, -0.8), torso=(14, 0, 0), cabeza=(-12, 0, 0), _mano_d=("h", 1.0, 6.0, 8.0), _arma=(0.0, 0.2, 1))),
        (1.14, FE_ABAJO),
        (1.65, con(FE_ABAJO, torso=(38, 0, 0))),
        (2.20, L),
    ], golpes=(1.13,))
    # ---- 2 barrido: echa la ferula atras a su derecha y barre a media altura de derecha a izquierda (el caliz, atras)
    BA_ATRAS = con(L, raiz=raiz(-1.0, 1.2), torso=(8, 52, 0), cabeza=(-6, -42, 0), muslo_d=(12, 0, 4), pierna_d=(18, 0, 0),
                   _mano_d=("h", -6.4, -1.2, -3.6), _arma=(-1, 0.2, -0.35), _mano_i=("h", 4.6, -4.4, 3.2))
    BA_DELANTE = con(L, raiz=raiz(-2.0, -1.4), torso=(16, -6, 0), cabeza=(-14, 4, 0), muslo_i=(-28, 0, -5), pierna_i=(28, 0, 0),
                     _mano_d=("h", 2.2, -1.2, 10.4), _arma=(0.1, 0.05, 1), _mano_i=("h", 4.8, -3.6, -2.4))
    BA_FIN = con(L, raiz=raiz(-1.8, -2.0), torso=(14, -54, 0), cabeza=(-10, 44, 0), muslo_i=(-32, 0, -5), pierna_i=(32, 0, 0),
                 _mano_d=("h", 9.0, -1.4, 5.6), _arma=(1, 0.05, 0.25), _mano_i=("h", 4.4, -3.0, -3.6))
    accion("barrido", 2.0, [
        (0.00, L),
        (0.62, BA_ATRAS),
        (0.88, con(BA_ATRAS, torso=(8, 58, 0), cabeza=(-6, -48, 0))),
        (0.97, BA_DELANTE),
        (1.06, BA_FIN),
        (1.45, con(BA_FIN, torso=(12, -58, 0))),
        (2.00, L),
    ], golpes=(0.99,))
    # ---- 3 aspersion: echa el caliz atras a su izquierda y lo barre por delante de izquierda a derecha, derramando la sangre
    AS_ATRAS = con(L, raiz=raiz(-1.2, 0.8), torso=(10, -40, 0), cabeza=(-8, 30, 0), muslo_i=(14, 0, -5), pierna_i=(14, 0, 0),
                   _mano_d=("h", -1.6, -8.8, 5.0), _arma=(0.0, 1.0, 0.1), _mano_i=("h", 7.4, -1.4, -3.8), _arma_i=(0.3, 1.0, -0.2))
    AS_DELANTE = con(L, raiz=raiz(-2.0, -1.6), torso=(18, 4, 0), cabeza=(-14, -4, 0), muslo_d=(-30, 0, 4), pierna_d=(30, 0, 0),
                     _mano_d=("h", -1.6, -8.8, 5.0), _arma=(0.0, 1.0, 0.1), _mano_i=("h", -1.0, 0.4, 10.2), _arma_i=(0.0, 0.2, 1))
    AS_FIN = con(L, raiz=raiz(-2.0, -1.8), torso=(16, 40, 0), cabeza=(-12, -32, 0), muslo_d=(-34, 0, 4), pierna_d=(34, 0, 0),
                 _mano_d=("h", -1.6, -8.8, 5.0), _arma=(0.0, 1.0, 0.1), _mano_i=("h", -8.6, 0.6, 5.8), _arma_i=(-1, 0.25, 0.3))
    accion("aspersion", 2.0, [
        (0.00, L),
        (0.55, AS_ATRAS),
        (0.80, con(AS_ATRAS, torso=(10, -46, 0), cabeza=(-8, 36, 0))),
        (0.90, AS_DELANTE),
        (1.05, AS_FIN),
        (1.40, con(AS_FIN, torso=(14, 44, 0))),
        (2.00, L),
    ], golpes=(0.90,))
    # ---- 4 comunion: alza el caliz al cielo con la izquierda (la ferula en alto en la derecha), la cabeza atras; aguanta
    # mientras la sangre vuela a el, y luego bebe
    CO_ALTO = con(BASE, raiz=raiz(0.2), torso=(-12, 0, 0), cabeza=(-34, 0, 0), capa=(14, 0, 0), infulas=(18, 0, 0),
                  _mano_d=("h", -3.0, 2.0, 5.4), _arma=(-0.1, 1.0, 0.1), _mano_i=("h", 0.6, 10.6, 2.0), _arma_i=ARRIBA)
    CO_BEBE = con(BASE, raiz=raiz(-0.4), torso=(-6, 0, 0), cabeza=(-30, 0, 0), capa=(8, 0, 0),
                  _mano_d=("h", -1.6, -9.0, 4.6), _arma=(0.0, 1.0, 0.04), _mano_i=("h", -4.4, 5.0, 5.2), _arma_i=(0.0, 0.2, -1))
    accion("comunion", 3.4, [
        (0.00, L),
        (0.45, con(BASE, torso=(16, 0, 0), cabeza=(6, 0, 0), _mano_i=("h", -2.6, -5.0, 6.0))),
        (0.85, CO_ALTO),
        (1.60, con(CO_ALTO, torso=(-14, 0, 3), cabeza=(-38, 0, 4))),
        (2.30, con(CO_ALTO, torso=(-12, 0, -3), cabeza=(-36, 0, -4))),
        (2.75, CO_BEBE),
        (3.00, con(CO_BEBE, cabeza=(-34, 0, 0))),
        (3.40, L),
    ], golpes=(0.85,))
    # ---- 5 juicio: alza la ferula al cielo, derecha (la izquierda abierta) y la clava de punta delante de el con todo su peso
    JU_ALTO = con(BASE, raiz=raiz(0.4), torso=(-10, 0, 0), cabeza=(-30, 0, 0), capa=(14, 0, 0), infulas=(18, 0, 0),
                  _mano_d=("h", -0.8, 10.4, 2.4), _arma=ARRIBA, _mano_i=("h", 5.4, 3.0, 4.6), _arma_i=(0.3, 1.0, 0.2))
    JU_CLAVA = con(BASE, raiz=raiz(-3.4, 0.4), torso=(30, 0, 0), cabeza=(-18, 0, 0), capa=(-14, 0, 0), infulas=(-18, 0, 0),
                   muslo_d=(-30, 0, 4), pierna_d=(46, 0, 0), muslo_i=(-8, 0, -4), pierna_i=(46, 0, 0),
                   _mano_d=("h", -0.4, -2.6, 9.0), _arma=ARRIBA, _mano_i=("h", 3.8, -4.4, 4.0))
    accion("juicio", 2.4, [
        (0.00, L),
        (0.70, JU_ALTO),
        (1.05, con(JU_ALTO, raiz=raiz(0.6), _mano_d=("h", -0.8, 10.8, 2.0))),
        (1.20, con(JU_ALTO, raiz=raiz(-1.4, 0.2), torso=(10, 0, 0), cabeza=(-20, 0, 0), _mano_d=("h", -0.6, 4.0, 6.8))),
        (1.30, JU_CLAVA),
        (1.85, con(JU_CLAVA, torso=(26, 0, 0))),
        (2.40, L),
    ], golpes=(1.30,))
    # ---- 6 sermon: abre los brazos (la ferula y el caliz en alto), la cabeza atras, y predica
    SE = con(BASE, raiz=raiz(-0.2, 0.6), torso=(-14, 0, 0), cabeza=(-28, 0, 0), capa=(20, 0, 0), infulas=(22, 0, 0),
             _mano_d=("h", -6.6, 7.0, 4.2), _arma=(-0.25, 1, 0.1), _mano_i=("h", 7.0, 6.4, 4.4), _arma_i=(0.2, 1, 0.1))
    accion("sermon", 2.6, [
        (0.00, L),
        (0.40, con(BASE, raiz=raiz(-1.6), torso=(24, 0, 0), cabeza=(14, 0, 0))),
        (0.80, SE),
        (1.35, con(SE, torso=(-16, 0, 4), cabeza=(-32, 0, 6))),
        (1.90, con(SE, torso=(-12, 0, -4), cabeza=(-26, 0, -6))),
        (2.60, L),
    ], golpes=(0.80,))
    # ---- 7 uncion: alza el caliz por encima de la cabeza y se lo vierte encima (la boca abajo); luego abre los brazos
    UN_ALZA = con(BASE, raiz=raiz(0.0), torso=(-6, 0, 0), cabeza=(-18, 0, 0), capa=(8, 0, 0),
                  _mano_d=("h", -3.0, -6.0, 6.4), _arma=(0.0, 1.0, 0.2), _mano_i=("h", -4.6, 9.2, 2.2), _arma_i=ARRIBA)
    UN_VIERTE = con(UN_ALZA, cabeza=(-30, 0, 0), torso=(-10, 0, 0), _mano_i=("h", -5.2, 9.0, 1.2), _arma_i=(-0.6, -0.8, 0.1))
    UN_ABRE = con(BASE, raiz=raiz(-0.6, 0.6), torso=(-16, 0, 0), cabeza=(-36, 0, 0), capa=(20, 0, 0), infulas=(24, 0, 0),
                  _mano_d=("h", -8.0, 4.2, 3.4), _arma=(-0.3, 1, 0.1), _mano_i=("h", 8.2, 4.0, 3.4), _arma_i=(0.3, 0.6, 0.6))
    accion("uncion", 3.0, [
        (0.00, L),
        (0.70, UN_ALZA),
        (1.15, UN_VIERTE),
        (1.60, con(UN_VIERTE, cabeza=(-34, 0, 4))),
        (2.10, UN_ABRE),
        (2.55, con(UN_ABRE, torso=(-18, 0, 0))),
        (3.00, L),
    ], golpes=(1.15,))
    # ---- 8 aturdido
    AT = con(L, raiz=raiz(-1.4, 2.0), torso=(-10, 0, 8), cabeza=(-20, 0, 14), capa=(14, 0, 0), muslo_d=(-8, 0, 6), pierna_d=(22, 0, 0),
             muslo_i=(12, 0, -6), pierna_i=(16, 0, 0), _mano_d=("h", -3.6, -8.4, 3.0), _arma=(-0.3, 0.6, 0.6),
             _mano_i=("h", 5.4, -3.4, 2.6), _arma_i=(0.6, 0.6, 0.2))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.22, AT),
        (0.66, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-1.8, 2.6))),
        (1.06, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.6, 2.2))),
        (1.50, con(L, raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir: suelta el aire, cae de rodillas con la ferula delante (se apoya en ella), el caliz se le cae y se derrama;
    # la cabeza baja: vencido
    VENCIDO = con(RODILLAS, raiz=raiz(-6.8, 0.6), torso=(28, 0, 4), cabeza=(40, 0, 8), capa=(22, 0, 0),
                  _mano_d=("h", -0.6, -4.4, 7.4), _arma=(0.0, 1.0, 0.25), _mano_i=("h", 2.2, -8.6, 4.4), _arma_i=(0.5, -0.4, 0.8))
    accion("morir", 4.0, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), capa=(12, 0, 0), _mano_i=("h", 2.0, -2.0, 7.0))),
        (0.80, con(REZANDO, raiz=raiz(-5.8, 0.9), torso=(4, 0, 0), cabeza=(-14, 0, 0), _mano_i=("h", 1.0, -3.0, 7.6))),
        (1.80, con(REZANDO, torso=(10, 0, 0), cabeza=(-6, 0, 0), _mano_i=("h", 2.0, -7.0, 5.4), _arma_i=(0.6, 0.2, 0.8))),
        (2.80, VENCIDO),
        (4.00, VENCIDO),
    ])
    # ---- 10 levantarse: estaba de rodillas rezando; se apoya en la ferula, se pone en pie y se pone en guardia
    accion("levantarse", 2.0, [
        (0.00, REZANDO),
        (0.65, con(REZANDO, torso=(6, 0, 0), cabeza=(-6, 0, 0), muslo_d=(-66, 0, 4), pierna_d=(76, 0, 0), raiz=raiz(-4.8, 0.6))),
        (1.30, con(BASE, raiz=raiz(-0.8), torso=(14, 0, 0), cabeza=(-6, 0, 0))),
        (2.00, L),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    if nombre == "andar":
        return [anims["quieto"]]
    return []


TABLA_EXTRA = ()
