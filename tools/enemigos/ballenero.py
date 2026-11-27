"""
Brann, el Ballenero (parte 7r): el jefe de la Costa de los Huesos. El viejo arponero del Puerto Varado; la noche del cielo rojo
el mar se retiro de golpe y el se quedo en tierra, en el Costillar de la Ballena, arponeando a la Vieja, una ballena que hace
mucho que no esta (libro 26: "el viejo Brann sigue arponeando una ballena que hace mucho que no esta"). Cuarenta anos detras de
ella. La sal se lo ha comido como a los demas, pero a el lo ha hecho grande: un gigante viejo (el juego lo dibuja a 1,55).

Calvo, con la piel blanca y agrietada de sal, una melena blanca por la nuca, la barba blanca hasta el pecho y los ojos con una
luz fria de agua; un chaqueton largo de hule oscuro con costras de sal sobre un jersey de lana, el cuello alto; una cuerda
enrollada en bandolera; una pata de palo de hueso de ballena en la derecha y una bota alta en la izquierda; un arpon de
repuesto a la espalda. En la derecha, el arpon (asta de fresno, la cana de hierro y la punta con sus dos barbas; el cabo atado
abajo); en la izquierda, el farol de aceite de ballena.

Sus golpes: el arponazo (lo echa atras por encima del hombro y lo clava a fondo), el tajo doble (barre con la punta de un lado
a otro y de vuelta), el arpon (lo lanza atado a su cabo; a quien pilla, lo arrastra: el tiron), el farol (lo lanza: el aceite
arde en el suelo), el salto (salta y lo clava en el suelo: la onda), el grito ("¡Alli resopla!") y llamar a la Vieja a media
vida (apunta con el arpon y alza el farol).
"""
from anim import Anim, con, raiz, resolver, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)

NOMBRE = "ballenero"
VARIANTES = ("brann",)
OCULTAS = {0: []}
#: un gigante: lo que le da su dibujante en el juego
ESCALA = 1.55
CENTRO_Y = 1.5

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(9, 12, 5), brazo=(4, 12, 4), pierna=(4, 12, 4))
    # la pata de palo: la pierna derecha (de la rodilla abajo) es la copa de cuero atada y el hueso de ballena con su contera
    r.por_nombre["pierna_d"].boxes = [
        Box(-2.2, 0, -2.2, 4.4, 1.8, 4.4, "copa_palo"),
        Box(-1.0, 1.8, -1.0, 2.0, 3.4, 2.0, "pata_palo"),
        Box(-0.7, 5.2, -0.7, 1.4, 0.8, 1.4, "contera"),
    ]
    # la bota alta de la izquierda
    r.pieza("bota_i", "pierna_i", (0, 1.0, 0), boxes=[Box(-2.3, 0, -2.3, 4.6, 5.0, 4.6, "bota")])
    # el chaqueton de hule (abierto por delante) con su cuello alto, y los faldones largos
    r.caja("torso", -4.5, -12, -2.5, 9, 12, 5, "chaqueton", grow=g(0.5))
    r.pieza("cuello_chaqueton", "torso", (0, -12, 0), boxes=[Box(-5.0, -2.0, -3.1, 10, 2.6, 6.2, "cuello_chaqueton")])
    r.pieza("faldon_tras", "cadera", (0, 0, 0), boxes=[Box(-5.0, -0.5, 1.9, 10, 10.5, 1, "faldon_tras")])
    for s, n in ((-1, "d"), (1, "i")):
        r.pieza(f"faldon_{n}", f"muslo_{n}", (0, 0, 0), boxes=[
            Box(-3.0 if s < 0 else 2.0, -0.5, -2.6, 1, 9.5, 5.2, f"faldon_lado_{n}"),
            Box(-2.4 if s < 0 else -0.6, -0.5, -3.1, 3, 8.5, 1, f"faldon_frente_{n}"),
        ])
    # la cuerda en bandolera (del hombro izquierdo a la cadera derecha) y el rollo que le cuelga de la cadera
    r.pieza("bandolera", "torso", (0, -6.2, 0), rot=(0, 0, -34), boxes=[Box(-0.9, -8.6, -3.3, 1.8, 17.2, 6.6, "bandolera")])
    r.pieza("rollo", "cadera", (-5.4, 1.2, 0.4), rot=(0, 90, 8), boxes=[Box(-2.6, -2.6, -0.9, 5.2, 5.2, 1.8, "rollo")])
    # los cristales de sal de los hombros y de la coronilla
    r.pieza("sal_hombros", "torso", (0, -12, 0), boxes=[
        Box(-6.4, -2.2, -1.2, 2.4, 2.6, 2.4, "cristal"),
        Box(-5.0, -3.0, 0.6, 1.4, 2.0, 1.4, "cristal"),
        Box(4.2, -1.8, -0.8, 2.2, 2.2, 2.0, "cristal"),
    ])
    r.pieza("sal_cabeza", "cabeza", (0, -8, 0), boxes=[
        Box(1.4, -1.4, -1.6, 1.6, 1.6, 1.6, "cristal"),
        Box(-2.8, -0.9, 1.0, 1.2, 1.2, 1.2, "cristal"),
    ])
    # la barba hasta el pecho, el bigote, y la melena por la nuca
    r.pieza("barba", "cabeza", (0, 0, 0), boxes=[
        Box(-3.6, -3.0, -4.7, 7.2, 9.6, 1.6, "barba"),
        Box(-4.1, -3.8, -4.5, 8.2, 1.6, 0.8, "bigote"),
    ])
    r.pieza("melena", "cabeza", (0, 0, 0), boxes=[Box(-4.5, -6.0, -0.4, 9.0, 8.4, 4.9, "melena")])
    # el arpon de repuesto a la espalda (asoma por encima del hombro derecho)
    r.pieza("arpon_espalda", "torso", (0.5, -5.5, 3.3), rot=(0, 0, 38), boxes=[
        Box(-0.5, -13.0, -0.5, 1, 22.0, 1, "asta_espalda"),
        Box(-0.35, -16.5, -0.35, 0.7, 3.5, 0.7, "cana_espalda"),
        Box(-1.3, -19.0, -0.4, 2.6, 2.5, 0.8, "punta_espalda"),
    ])
    # ---- el arpon (mano derecha): el asta va de la contera (+Z) a la cana (-Z) y la punta con sus dos barbas
    r.pieza("arpon", "mano_d", (0, 0, 0), boxes=[
        Box(-0.6, -0.6, -18.0, 1.2, 1.2, 28.0, "asta"),
        Box(-0.8, -0.8, -18.6, 1.6, 1.6, 1.4, "anilla"),
        Box(-0.45, -0.45, -24.0, 0.9, 0.9, 6.0, "cana"),
        Box(-1.5, -0.5, -27.6, 3.0, 1.0, 3.8, "punta"),
        Box(-2.7, -0.5, -25.4, 1.3, 1.0, 2.2, "barba_arpon", uv="barba_arpon"),
        Box(1.4, -0.5, -25.4, 1.3, 1.0, 2.2, "barba_arpon", uv="barba_arpon"),
        Box(-0.9, -0.9, 7.6, 1.8, 1.8, 2.6, "cabo_atado"),
    ])
    # ---- el farol de aceite de ballena (mano izquierda): cuelga a lo largo de -Z de la mano (en las poses, hacia abajo)
    r.pieza("farol", "mano_i", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2.0, 1, 1, 3, "asa"),
        Box(-1.9, -1.9, -3.2, 3.8, 3.8, 1.2, "farol_tapa"),
        Box(-2.2, -2.2, -8.4, 4.4, 4.4, 5.2, "farol"),
        Box(-1.5, -1.5, -9.4, 3.0, 3.0, 1.0, "farol_pie"),
    ])
    r.empaquetar()
    return r


# ---------------------------------------------------------------- las texturas
# La piel comida por la sal (gris blanquecina, agrietada); la barba y la melena blancas, con sal; el hule del chaqueton, de un
# verde aceituna casi negro con manchas y costras blancas; el jersey de lana azul de noche; el cuero de la copa y del cinto;
# el laton del farol; el hierro del arpon, oxidado; el asta de fresno, gastada; la pata de hueso de ballena.

PIEL = ["#4e4b46", "#67635c", "#827d74", "#9d978c", "#b7b1a5", "#d0cbbf", "#e6e2d8"]
BLANCO = ["#7f8786", "#9aa2a1", "#b7bebc", "#d2d8d6", "#e9edeb", "#f8faf9"]
SAL = ["#c9cfcd", "#dce2df", "#eef2ef", "#ffffff"]
GRIETA = "#3a3632"
CUENCA = "#0b1214"
HULE = ["#1d1d12", "#282818", "#34341f", "#424128", "#524f31", "#625e3a"]
LANA = ["#10151d", "#171e29", "#1f2836", "#283344", "#323f52"]
CUERO = ["#1b120d", "#291c14", "#38271b", "#493424", "#5b432f"]
BOTA = ["#141312", "#1d1b19", "#282522", "#35312c"]
LATON = ["#3d2f10", "#5f4a1a", "#836827", "#a88a38", "#c9aa52", "#e2c878"]
HIERRO = ["#2b2220", "#45302a", "#5f3d31", "#7a4c3a", "#93604a"]
HIERRO_GRIS = ["#24262a", "#363a40", "#4b5058", "#646a73", "#80878f"]
FRESNO = ["#3a2d1f", "#4c3c29", "#5f4c35", "#735d41", "#88714f", "#9d8560"]
CUERDA = ["#3e3424", "#54482f", "#6c5d3c", "#857451", "#9c8b66"]
HUESO = ["#77735f", "#918d78", "#aba792", "#c5c1ab", "#dcd8c2", "#eeeadb"]
OJO, OJO_B = "#e8fbff", "#9fe6ff"
LLAMA = ["#8a5a12", "#c98a26", "#eec14e", "#fde58e", "#fff7d6"]
CRISTAL_FAROL = ["#3c3a26", "#5a5636", "#7e784a"]


def _cara():
    C = {"S": PIEL[5], "s": PIEL[4], "o": PIEL[3], "O": PIEL[2], "K": CUENCA, "k": "#18262a", "E": OJO, "e": OJO_B,
         "g": GRIETA, "c": "#5e2b26", "W": BLANCO[4], "w": BLANCO[3], "b": BLANCO[2], "M": "#1c1715"}
    return dibujo([
        "osSSSSSo",
        "oSgsSsSo",
        "wWWsoWWw",
        "OKeKoKEO",
        "oKkoskKo",
        "OosggsoO",
        "oo.MM.oo",
        "........",
    ], C, resto=None)


def materiales(variante):
    piel = ruido(PIEL[2:6], 911, grano=0.45, base=0.5)
    blanco = ruido(BLANCO[1:6], 912, grano=0.5, base=0.55)
    hule = tela(HULE, 921, pliegue=4, grano=0.14, luz=0.2)
    lana = tela(LANA, 922, pliegue=2, grano=0.18)
    cuero = ruido(CUERO, 931, grano=0.4)
    bota = ruido(BOTA, 932, grano=0.35)
    hueso = ruido(HUESO[1:6], 941, grano=0.4, base=0.55)
    cuerda = ruido(CUERDA, 951, grano=0.5)
    cara = _cara()

    def costra(c, cara_, x, y, sem):
        # las manchas y costras de sal del hule
        k = (x * 7 + y * 11 + sem + {"frente": 0, "espalda": 5, "der": 9, "izq": 3, "arriba": 7, "abajo": 1}.get(cara_, 0)) % 23
        if k == 0:
            return SAL[1]
        if k == 1:
            return mezcla(c, SAL[0], 0.45)
        return c

    def cabeza(cara_, x, y, w, h):
        if cara_ == "frente":
            c = cara(cara_, x, y, w, h)
            return c if c is not None else piel(cara_, x, y, w, h)
        if cara_ == "arriba":
            # la calva, con alguna grieta y sal
            c = piel(cara_, x, y, w, h)
            if (x * 5 + y * 3) % 13 == 0:
                return GRIETA
            if (x + y * 7) % 11 == 0:
                return SAL[1]
            return claro(c, 0.06)
        c = piel(cara_, x, y, w, h)
        if cara_ in ("der", "izq"):
            d = delante(cara_, x, w)
            if y >= 5 and d <= 2:
                return blanco(cara_, x, y, w, h)              # la barba le sube por la quijada
            if y == 3 and d == 1:
                return PIEL[1]                                # la oreja
        if (x * 3 + y * 5) % 17 == 0:
            return GRIETA
        return c

    def barba(cara_, x, y, w, h):
        if cara_ != "frente":
            if cara_ in ("der", "izq", "abajo"):
                return oscuro(blanco(cara_, x, y, w, h), 0.18)
            return None
        # mechones largos: mas largos en el medio; la boca por arriba; dos trenzas con cuentas de hueso abajo
        largo = (6, 7, 8, 9, 9, 8, 7)[x % 7]
        if y > largo:
            return None
        if y == 0 and 2 <= x <= 4:
            return None                                       # la boca
        if y == largo and x in (2, 4):
            return HUESO[4]                                   # las cuentas de hueso
        if (x * 3 + y * 5) % 13 == 0:
            return SAL[2]
        t = (1, 3, 2, 4, 3, 2, 1)[x % 7]
        return BLANCO[max(0, t - (1 if y >= largo - 1 else 0) - (1 if (x + y) % 4 == 0 else 0))]

    def bigote(cara_, x, y, w, h):
        if cara_ != "frente":
            return BLANCO[2]
        return BLANCO[3 + ((x + y) % 2)] if not (3 <= x <= 4 and y == 1) else None

    def melena(cara_, x, y, w, h):
        if cara_ == "frente":
            return None
        if cara_ == "arriba":
            return None                                       # la calva sigue por arriba
        c = blanco(cara_, x, y, w, h)
        if cara_ in ("der", "izq"):
            d = delante(cara_, x, w)
            if d <= 1 and y < 4:
                return None                                   # deja ver la oreja y la sien
        # mechones que acaban en punta
        if y >= h - 2 and (x % 3) == 0:
            return None
        return oscuro(c, 0.08 * (y / max(1, h - 1)))

    def torso(cara_, x, y, w, h):
        # debajo del chaqueton: el jersey de lana con su cuello vuelto y el cinto de cuero con la hebilla de laton
        c = lana(cara_, x, y, w, h)
        if cara_ in ("frente", "espalda", "der", "izq"):
            if y in (9, 10):
                if cara_ == "frente" and x in (4, 5):
                    return LATON[4 if y == 9 else 2]
                return CUERO[2 if y == 9 else 1]
            if y <= 1:
                return LANA[3] if (x % 2 == 0) else LANA[2]  # el cuello vuelto
            if y % 3 == 0 and cara_ == "frente":
                return LANA[1]                                # los puntos del jersey
        return c

    def chaqueton(cara_, x, y, w, h):
        c = hule(cara_, x, y, w, h)
        if cara_ == "abajo":
            return None
        if cara_ == "frente":
            # abierto: se ve el jersey por el medio; las solapas, y botones de hueso
            ancho = 1 if y <= 2 else 2
            if abs(x - 4.0) < ancho:
                return None
            if abs(x - 4.0) < ancho + 1:
                return HULE[4] if y % 2 else HULE[3]
            if x in (1, 7) and y % 4 == 2 and y < 10:
                return HUESO[4]
        if cara_ == "espalda" and x in (4, 5) and y >= 8:
            return HULE[0]                                    # la abertura de atras
        return costra(c, cara_, x, y, 0)

    def cuello_chaqueton(cara_, x, y, w, h):
        if cara_ == "abajo":
            return None
        c = hule(cara_, x, y + 6, w, h + 6)
        if cara_ == "frente" and 3 <= x <= w - 4:
            return None                                       # abierto por delante (la barba asoma)
        if cara_ == "arriba" and 2 <= x <= w - 3 and 1 <= y <= h - 2:
            return None                                       # hueco del cuello
        return claro(c, 0.06) if y == 0 else c

    def faldon(jiron, semilla):
        def f(cara_, x, y, w, h):
            corte = jiron[(x + semilla) % len(jiron)]
            if cara_ in ("arriba", "abajo") or y > corte:
                return None
            c = hule(cara_, x, y + 4, w, h + 4)
            c = costra(c, cara_, x, y, semilla)
            if y >= corte - 1:
                return mezcla(c, SAL[0], 0.35)                 # el borde, blanco de sal
            return oscuro(c, 0.04 * y)
        return f

    def faldon_tras(cara_, x, y, w, h):
        if cara_ not in ("espalda", "frente", "der", "izq"):
            return None
        if cara_ == "espalda" and x in (4, 5) and y >= 2:
            return None                                       # la raja del faldon
        return faldon((10, 9, 10, 8, 10, 9, 10, 9, 8, 10), 0)(cara_, x, y, w, h)

    def brazo(cara_, x, y, w, h):
        c = hule(cara_, x, y, w, h)
        return costra(c, cara_, x, y, 4)

    def antebrazo(cara_, x, y, w, h):
        if y >= h - 1:
            return PIEL[2] if cara_ != "abajo" else PIEL[3]   # la mano
        if y >= h - 2:
            return piel(cara_, x, y, w, h)
        if y in (2, 3):
            return LANA[2] if y == 3 else HULE[4]            # la bocamanga vuelta y el puno del jersey
        return costra(hule(cara_, x, y, w, h), cara_, x, y, 6)

    def muslo(cara_, x, y, w, h):
        # el calzon de lona encerada, oscuro
        c = tela(["#1c1b17", "#26241f", "#302d27", "#3b3730"], 961, pliegue=3)(cara_, x, y, w, h)
        if (x * 5 + y * 7) % 19 == 0:
            return SAL[0]
        return c

    def pierna_i(cara_, x, y, w, h):
        return tela(["#1c1b17", "#26241f", "#302d27", "#3b3730"], 961, pliegue=3)(cara_, x, y, w, h)

    def bota_m(cara_, x, y, w, h):
        c = bota(cara_, x, y, w, h)
        if y == 0:
            return claro(c, 0.10)
        if y >= h - 1 and cara_ != "abajo":
            return oscuro(c, 0.25)
        if (x + y * 3) % 9 == 0:
            return mezcla(c, SAL[0], 0.5)
        return c

    def copa_palo(cara_, x, y, w, h):
        # la copa de cuero donde encaja el munon, con su correa
        c = cuero(cara_, x, y, w, h)
        if y == h - 1 and cara_ not in ("arriba", "abajo"):
            return CUERO[4]
        if x == w // 2 and cara_ in ("der", "izq"):
            return LATON[2]                                   # la hebilla
        return c

    def pata_palo(cara_, x, y, w, h):
        c = hueso(cara_, x, y, w, h)
        if (y + x) % 3 == 0:
            return oscuro(c, 0.12)                            # las vetas del hueso
        return c

    def bandolera(cara_, x, y, w, h):
        # la cuerda, enrollada en vueltas (rayas en diagonal)
        if cara_ in ("arriba", "abajo"):
            return cuerda(cara_, x, y, w, h)
        c = cuerda(cara_, x, y, w, h)
        return oscuro(c, 0.3) if (x + y) % 3 == 0 else (claro(c, 0.08) if (x + y) % 3 == 1 else c)

    def rollo(cara_, x, y, w, h):
        # un rollo de cuerda: un aro (el centro hueco) visto de frente
        if cara_ in ("frente", "espalda"):
            cx, cy = x - (w - 1) / 2.0, y - (h - 1) / 2.0
            r = (cx * cx + cy * cy) ** 0.5
            if r < 1.1 or r > 2.75:
                return None
            c = cuerda(cara_, x, y, w, h)
            return oscuro(c, 0.25) if (x + y) % 2 == 0 else c
        return cuerda(cara_, x, y, w, h) if (x + y) % 2 == 0 else CUERDA[1]

    def cristal(cara_, x, y, w, h):
        return SAL[(x + y + (1 if cara_ in ("arriba", "der") else 0)) % 4]

    def asta(cara_, x, y, w, h):
        c = madera(FRESNO, 971, "z")(cara_, x, y, w, h)
        # vueltas de cuerda donde se agarra y una marca de sal
        if cara_ in ("der", "izq", "arriba", "abajo"):
            k = x if cara_ in ("der", "izq") else y
            if 17 <= k <= 19 or 23 <= k <= 24:
                return CUERDA[2 + (k % 2)]
        return c

    def cana(cara_, x, y, w, h):
        return metal(HIERRO, 972)(cara_, x, y, w, h)

    def punta(cara_, x, y, w, h):
        # la hoja de la punta: hierro gris con el filo claro y oxido
        c = metal(HIERRO_GRIS, 973)(cara_, x, y, w, h)
        if (x + y) % 5 == 0:
            return HIERRO[2]
        return c

    def anilla(cara_, x, y, w, h):
        return metal(HIERRO, 974)(cara_, x, y, w, h)

    def cabo_atado(cara_, x, y, w, h):
        return cuerda(cara_, x, y, w, h) if (x + y) % 2 else oscuro(cuerda(cara_, x, y, w, h), 0.25)

    def farol(cara_, x, y, w, h):
        # la jaula de laton y, dentro, la llama del aceite (los cuatro costados; las tapas, de laton)
        if cara_ in ("frente", "espalda"):
            return LATON[2] if (x in (0, w - 1) or y in (0, h - 1)) else LATON[1]
        if x in (0, w - 1) or y in (0, h - 1):
            return LATON[3 + ((x + y) % 2)]
        cx, cy = abs(x - (w - 1) / 2.0), abs(y - (h - 1) / 2.0)
        return LLAMA[4] if cx + cy < 0.9 else LLAMA[3] if cx + cy < 1.7 else CRISTAL_FAROL[2]

    return {
        "cabeza": sombra(cabeza, abajo=0.12),
        "torso": sombra(torso),
        "chaqueton": sombra(chaqueton, abajo=0.16),
        "cuello_chaqueton": cuello_chaqueton,
        "faldon_tras": faldon_tras,
        "faldon_lado_d": faldon((9, 8, 9, 7, 9), 2), "faldon_lado_i": faldon((8, 9, 7, 9, 8), 4),
        "faldon_frente_d": faldon((8, 7, 8), 1), "faldon_frente_i": faldon((7, 8, 8), 3),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.14), "antebrazo_i": sombra(antebrazo, abajo=0.14),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_i": sombra(pierna_i, abajo=0.1), "bota": sombra(bota_m, abajo=0.2),
        "copa_palo": copa_palo, "pata_palo": sombra(pata_palo, abajo=0.15), "contera": metal(HIERRO_GRIS, 975),
        "bandolera": bandolera, "rollo": rollo, "cristal": cristal,
        "barba": barba, "bigote": bigote, "melena": sombra(melena, abajo=0.1),
        "asta_espalda": madera(FRESNO, 976, "y"), "cana_espalda": metal(HIERRO, 977), "punta_espalda": metal(HIERRO_GRIS, 978),
        "asta": asta, "anilla": anilla, "cana": cana, "punta": punta, "barba_arpon": metal(HIERRO_GRIS, 979),
        "cabo_atado": cabo_atado,
        "asa": metal(LATON[0:5], 981), "farol_tapa": metal(LATON[0:5], 982), "farol": farol, "farol_pie": metal(LATON[0:5], 983),
    }


def brillo(variante):
    """Lo que brilla: los ojos (luz fria de agua) y la llama del farol."""
    C = {"E": OJO, "e": OJO_B}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "..e...E.",
    ], C)

    def llama(cara, x, y, w, h):
        if cara in ("frente", "espalda") or x in (0, w - 1) or y in (0, h - 1):
            return None
        cx, cy = abs(x - (w - 1) / 2.0), abs(y - (h - 1) / 2.0)
        return LLAMA[4] if cx + cy < 0.9 else LLAMA[3] if cx + cy < 1.7 else None

    return {"cabeza": por_cara(liso(None), frente=ojos), "farol": llama}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 12) y
# _arma = hacia donde apunta la punta del arpon (su -Z); el farol cuelga hacia -Z de la mano.

ABAJO = (0.0, -1.0, 0.0)
# de pie: cargado de hombros, el arpon de pie como un baston (la punta arriba) y el farol bajo
BASE = dict(raiz=raiz(-0.3), torso=(8, 0, 2), cabeza=(4, 0, -2),
            muslo_d=(-4, 0, 4), pierna_d=(4, 0, 0), muslo_i=(2, 0, -4), pierna_i=(6, 0, 0),
            _mano_d=("h", -0.6, -6.0, 2.8), _arma=(-0.04, 1, 0.1),
            _mano_i=("h", 1.6, -8.6, 1.6), _arma_i=ABAJO)
# en guardia: de medio lado, el arpon bajo por delante como una lanza, el farol atras y alto
LISTO = dict(raiz=raiz(-1.2), torso=(12, -18, 0), cabeza=(-4, 16, 0),
             muslo_d=(-18, 0, 4), pierna_d=(10, 0, 0), muslo_i=(14, 0, -6), pierna_i=(14, 0, 0),
             _mano_d=("h", 0.6, -6.4, 5.2), _arma=(0.06, 0.12, 1),
             _mano_i=("h", 4.2, -3.0, -1.8), _arma_i=ABAJO)
# de vigia (la postura de rezar del juego): de rodilla en la izquierda, la pata de palo delante, el arpon de pie a su lado y
# el farol en el suelo; la cabeza alta, mirando al mar que no esta
VIGIA = dict(raiz=raiz(-9.0, 0.6), torso=(8, 0, 0), cabeza=(-10, 0, 0),
             faldon_d=(10, 0, 0), faldon_i=(-6, 0, 0),
             muslo_d=(-72, 0, 6), pierna_d=(70, 0, 0), muslo_i=(-6, 0, -5), pierna_i=(96, 0, 0),
             _mano_d=("h", -1.2, -2.6, 3.2), _arma=(-0.03, 1, 0.06),
             _mano_i=("h", 2.4, -3.2, 4.6), _arma_i=ABAJO)


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

    # ---- quieto: respira hondo, se apoya en el arpon y mira a un lado y a otro
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (1.6, con(BASE, raiz=raiz(-0.6), torso=(11, 3, 2), cabeza=(8, -10, -4), _mano_i=("h", 1.8, -8.4, 2.0))),
        (3.2, con(BASE, raiz=raiz(-0.2), torso=(7, -3, 3), cabeza=(2, 12, 0), _mano_i=("h", 1.4, -8.7, 1.2))),
        (5.0, BASE),
    ])
    # ---- andar: cojea; la pata de palo no se dobla (la rodilla derecha casi rigida) y el cuerpo cae sobre ella
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-20, 18, 2, 18, 5, 0.0), (-6, 4, 2, 38, 1, 0.9), (16, -22, 4, 8, -4, -0.6), (4, -4, 4, 30, 0, 0.5), (-20, 18, 2, 18, 5, 0.0))):
        pasos.append((1.5 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (6, ty, -ty * 0.8), "cabeza": (0, -ty * 0.6, ty * 0.5), "raiz": raiz(up),
                                    "faldon_tras": (abs(md) * 0.35, 0, 0)}))
    bucle("andar", 1.5, pasos)
    # ---- en guardia y de vigia
    l0 = fija(LISTO)
    l1 = fija(con(LISTO, raiz=raiz(-1.8), torso=(15, -21, 0), _mano_d=("h", 0.8, -6.6, 5.0), _mano_i=("h", 4.4, -2.6, -2.0)))
    bucle("listo", 1.8, [(0.0, l0), (0.9, l1), (1.8, l0)])
    v0 = fija(VIGIA)
    # (solo la cabeza: con el cuerpo quieto las manos no cambian y el farol no se columpia)
    v1 = dict(v0, cabeza=(-12, -12, 0))
    v2 = dict(v0, cabeza=(-8, 12, 0))
    bucle("vigia", 8.0, [(0.0, v0), (2.6, v1), (5.4, v2), (8.0, v0)])

    # ================================================================ acciones
    L = LISTO
    # ---- 1 arponazo: lo echa atras por encima del hombro (la punta delante) y lo clava a fondo, cargando el cuerpo
    AZ_ATRAS = con(L, raiz=raiz(-1.0, 2.4), torso=(-2, 30, 0), cabeza=(-6, -24, 0),
                   muslo_d=(-4, 0, 4), pierna_d=(8, 0, 0), muslo_i=(18, 0, -6), pierna_i=(16, 0, 0),
                   _mano_d=("h", -1.6, -4.6, -4.4), _arma=(0.05, 0.08, 1), _mano_i=("h", 3.0, -3.0, 4.6))
    AZ_FONDO = con(L, raiz=raiz(-3.2, -5.6), torso=(28, -36, 0), cabeza=(-22, 30, 0),
                   muslo_d=(24, 0, 4), pierna_d=(8, 0, 0), muslo_i=(-56, 0, -4), pierna_i=(50, 0, 0),
                   _mano_d=("h", 1.8, -1.0, 8.6), _arma=(0.0, 0.02, 1), _mano_i=("h", 4.4, -3.4, -3.6))
    accion("arponazo", 1.6, [
        (0.00, L),
        (0.50, AZ_ATRAS),
        (0.78, con(AZ_ATRAS, raiz=raiz(-1.0, 2.8), torso=(-4, 34, 0), _mano_d=("h", -1.6, -4.6, -5.0))),
        (0.82, con(AZ_ATRAS, raiz=raiz(-2.0, -1.2), torso=(12, 0, 0), _mano_d=("h", -0.8, -5.8, 2.0), _arma=(0.03, 0.05, 1))),
        (0.86, AZ_FONDO),
        (1.18, con(AZ_FONDO, torso=(26, -38, 0))),
        (1.60, L),
    ], golpes=(0.86,))
    # ---- 2 tajo doble: barre con la punta de su derecha a su izquierda y de vuelta (el filo de la hoja)
    FUERA = con(L, raiz=raiz(-1.0, 0.8), torso=(8, 42, 0), cabeza=(-4, -36, 0),
                muslo_d=(-12, 0, 4), pierna_d=(10, 0, 0), _mano_d=("h", -6.4, -0.6, 2.2), _arma=(-1, 0.12, 0.2),
                _mano_i=("h", 2.6, -3.6, 4.8))
    DENTRO = con(L, raiz=raiz(-2.0, -2.4), torso=(16, -46, 0), cabeza=(-8, 40, 0),
                 muslo_i=(-40, 0, -6), pierna_i=(36, 0, 0), _mano_d=("h", 6.8, -1.4, 4.2), _arma=(1, 0.05, 0.32),
                 _mano_i=("h", 4.4, -2.0, -3.4))
    MEDIO = con(L, raiz=raiz(-1.6, -1.2), torso=(14, 0, 0), cabeza=(-8, 0, 0), _mano_d=("h", 0.6, -1.0, 8.6), _arma=(0.0, 0.08, 1))
    accion("tajo", 2.2, [
        (0.00, L),
        (0.46, FUERA),
        (0.66, con(FUERA, torso=(8, 46, 0), cabeza=(-4, -40, 0))),
        (0.74, MEDIO),
        (0.84, DENTRO),
        (1.16, con(DENTRO, torso=(16, -52, 0), cabeza=(-8, 44, 0))),
        (1.30, con(MEDIO, raiz=raiz(-2.0, -3.6), muslo_i=(-46, 0, -6), pierna_i=(40, 0, 0))),
        (1.40, con(FUERA, raiz=raiz(-2.0, -4.6), torso=(12, 44, 0), muslo_i=(-46, 0, -6), pierna_i=(40, 0, 0))),
        (1.75, con(FUERA, raiz=raiz(-1.8, -4.4), torso=(10, 46, 0), muslo_i=(-42, 0, -6), pierna_i=(36, 0, 0))),
        (2.20, L),
    ], golpes=(0.74, 1.30))
    # ---- 3 arpon: lo alza por encima del hombro, se echa atras y lo lanza (el golpe = suelta); luego se queda con el cabo
    T_ATRAS = con(L, raiz=raiz(-0.8, 2.6), torso=(-8, 34, 0), cabeza=(-8, -28, 0),
                  muslo_d=(-2, 0, 4), pierna_d=(10, 0, 0), muslo_i=(22, 0, -6), pierna_i=(14, 0, 0),
                  _mano_d=("h", -2.6, 6.0, -3.4), _arma=(0.05, 0.38, 1), _mano_i=("h", 2.4, 1.0, 6.4))
    T_SUELTA = con(L, raiz=raiz(-2.4, -3.4), torso=(22, -30, 0), cabeza=(-14, 24, 0),
                   muslo_d=(18, 0, 4), pierna_d=(6, 0, 0), muslo_i=(-44, 0, -4), pierna_i=(40, 0, 0),
                   _mano_d=("h", 1.2, 3.2, 8.0), _arma=(0.0, 0.22, 1), _mano_i=("h", 4.4, -3.0, -4.6))
    T_CABO = con(L, raiz=raiz(-2.0, -2.6), torso=(18, -20, 0), cabeza=(-10, 16, 0),
                 muslo_i=(-36, 0, -4), pierna_i=(32, 0, 0), _mano_d=("h", 1.2, -3.0, 7.2), _arma=(0.0, -0.2, 1),
                 _mano_i=("h", 4.0, -2.4, -3.6))
    accion("arpon", 1.7, [
        (0.00, L),
        (0.20, con(L, torso=(6, 10, 0), _mano_d=("h", -3.6, -3.0, 5.6), _arma=(0.05, 0.2, 1))),
        (0.38, con(T_ATRAS, _mano_d=("h", -4.4, 3.0, 3.6))),
        (0.55, T_ATRAS),
        (0.88, con(T_ATRAS, raiz=raiz(-0.8, 3.0), torso=(-10, 38, 0), _mano_d=("h", -2.8, 6.2, -4.2))),
        (0.94, con(T_ATRAS, raiz=raiz(-1.6, 0.0), torso=(6, 6, 0), _mano_d=("h", -1.6, 6.4, 2.6), _arma=(0.02, 0.3, 1))),
        (1.00, T_SUELTA),
        (1.25, T_CABO),
        (1.70, T_CABO),
    ], golpes=(1.00,))
    # ---- 4 tiron: con el cabo en el puno, se echa atras y tira con todo el cuerpo (el golpe = el tiron)
    TI_TENSO = con(T_CABO, raiz=raiz(-1.6, -1.6), torso=(16, -18, 0))
    TI_TIRA = con(L, raiz=raiz(-1.4, 3.6), torso=(-6, 20, 0), cabeza=(-14, -14, 0),
                  muslo_d=(30, 0, 4), pierna_d=(10, 0, 0), muslo_i=(-10, 0, -6), pierna_i=(10, 0, 0),
                  _mano_d=("h", -2.0, -6.0, -2.0), _arma=(0.0, -0.3, 1), _mano_i=("h", 3.8, -4.0, 0.0))
    accion("tiron", 1.3, [
        (0.00, T_CABO),
        (0.25, TI_TENSO),
        (0.45, TI_TIRA),
        (0.80, con(TI_TIRA, torso=(-8, 22, 0))),
        (1.30, L),
    ], golpes=(0.45,))
    # ---- 5 farol: lo echa atras por abajo y lo lanza en alto por delante (el golpe = suelta); el arpon, bajo
    F_ATRAS = con(L, raiz=raiz(-1.6, 1.6), torso=(10, -36, 0), cabeza=(-6, 30, 0),
                  muslo_i=(20, 0, -6), pierna_i=(16, 0, 0), _mano_d=("h", -1.0, -7.0, 3.6), _arma=(0.0, -0.3, 1),
                  _mano_i=("h", 1.6, -6.4, -5.2), _arma_i=ABAJO)
    F_LANZA = con(L, raiz=raiz(-1.6, -2.6), torso=(6, 20, 0), cabeza=(-14, -16, 0),
                  muslo_i=(-36, 0, -6), pierna_i=(30, 0, 0), _mano_d=("h", -1.0, -7.0, 3.6), _arma=(0.0, -0.3, 1),
                  _mano_i=("h", 1.0, 4.4, 7.4), _arma_i=(0.0, 0.6, 1))
    accion("farol", 1.8, [
        (0.00, L),
        (0.52, F_ATRAS),
        (0.84, con(F_ATRAS, torso=(10, -40, 0), _mano_i=("h", 1.6, -6.6, -5.6))),
        (0.90, con(F_ATRAS, torso=(8, -10, 0), _mano_i=("h", 3.6, -3.0, 5.0), _arma_i=(0.0, -0.2, 1))),
        (0.96, F_LANZA),
        (1.25, con(F_LANZA, torso=(8, 24, 0), _mano_i=("h", 1.2, 3.0, 7.4))),
        (1.80, L),
    ], golpes=(0.96,))
    # ---- 6 grito: "¡Alli resopla!": alza el arpon al cielo con la punta arriba y echa la cabeza atras
    G_ALTO = con(L, raiz=raiz(-0.6, 0.4), torso=(-12, 6, 0), cabeza=(-30, -4, 0),
                 muslo_d=(-8, 0, 6), pierna_d=(6, 0, 0), muslo_i=(6, 0, -6), pierna_i=(8, 0, 0),
                 _mano_d=("h", -1.0, 8.6, 1.4), _arma=(-0.05, 1, 0.12), _mano_i=("h", 4.6, -0.6, 3.0))
    accion("grito", 2.3, [
        (0.00, L),
        (0.45, con(L, raiz=raiz(-2.4, 0.8), torso=(26, 0, 0), cabeza=(20, 0, 0), _mano_d=("h", 0.6, -6.8, 4.4))),
        (0.58, con(G_ALTO, raiz=raiz(-1.4, 0.6), torso=(6, 4, 0), cabeza=(-6, 0, 0), _mano_d=("h", -3.4, 2.0, 6.0), _arma=(-0.05, 0.6, 1))),
        (0.70, G_ALTO),
        (1.60, con(G_ALTO, raiz=raiz(-0.4, 0.4), torso=(-14, 4, 0), cabeza=(-34, 6, 0), _mano_d=("h", -1.2, 8.8, 1.0))),
        (2.30, L),
    ], golpes=(0.70,))
    # ---- 7 llamar: apunta con el arpon al horizonte (por donde sale la Vieja) y alza el farol (el golpe = la llama)
    LL_BAJO = con(L, raiz=raiz(-2.6, 0.6), torso=(24, -6, 0), cabeza=(16, 6, 0),
                  muslo_d=(-24, 0, 8), pierna_d=(30, 0, 0), muslo_i=(-18, 0, -8), pierna_i=(36, 0, 0),
                  _mano_d=("h", -0.4, -6.6, 4.0), _arma=(0.0, -0.4, 1), _mano_i=("h", 2.6, -6.0, 3.6))
    LL_ALTO = con(L, raiz=raiz(0.2, -1.2), torso=(-10, -4, 0), cabeza=(-14, 4, 0),
                  muslo_d=(14, 0, 6), pierna_d=(4, 0, 0), muslo_i=(-26, 0, -6), pierna_i=(16, 0, 0),
                  _mano_d=("h", 0.4, 5.2, 7.0), _arma=(0.02, 0.5, 1), _mano_i=("h", 2.4, 8.2, 2.4), _arma_i=(0.1, 0.55, 1))
    accion("llamar", 2.8, [
        (0.00, L),
        (0.60, LL_BAJO),
        (1.15, con(LL_BAJO, raiz=raiz(-3.0, 0.6), torso=(28, -6, 0), cabeza=(20, 6, 0))),
        (1.25, con(LL_BAJO, raiz=raiz(-1.4, 0.0), torso=(8, -6, 0), cabeza=(0, 6, 0), _mano_d=("h", -0.2, -1.0, 7.4), _arma=(0.0, 0.1, 1),
                   _mano_i=("h", 4.6, 1.0, 5.0), _arma_i=(0.1, 0.1, 1))),
        (1.35, LL_ALTO),
        (2.20, con(LL_ALTO, raiz=raiz(0.0, -1.2), torso=(-12, -4, 0), cabeza=(-18, 4, 0), _mano_d=("h", 0.4, 5.5, 6.8))),
        (2.80, L),
    ], golpes=(1.35,))
    # ---- 8 salto: se agacha, salta con el arpon en alto (el juego le da el impulso) y lo clava en el suelo al caer
    S_AGACHA = con(L, raiz=raiz(-4.4, 0.6), torso=(30, -10, 0), cabeza=(-16, 8, 0),
                   muslo_d=(-46, 0, 6), pierna_d=(58, 0, 0), muslo_i=(-36, 0, -6), pierna_i=(66, 0, 0),
                   _mano_d=("h", -0.4, -4.4, 3.0), _arma=(0.0, 0.5, 1), _mano_i=("h", 4.2, -4.6, 1.0))
    S_AIRE = con(L, raiz=raiz(0.6, 0.0), torso=(-14, 0, 0), cabeza=(-6, 0, 0),
                 muslo_d=(-30, 0, 6), pierna_d=(30, 0, 0), muslo_i=(-48, 0, -6), pierna_i=(60, 0, 0),
                 _mano_d=("h", -0.8, 8.2, -0.4), _arma=(0.0, 0.25, 1), _mano_i=("h", 4.4, 2.6, -1.4))
    S_CLAVA = con(L, raiz=raiz(-4.8, -2.0), torso=(44, 0, 0), cabeza=(-24, 0, 0),
                  muslo_d=(-40, 0, 6), pierna_d=(62, 0, 0), muslo_i=(-56, 0, -6), pierna_i=(80, 0, 0),
                  _mano_d=("h", 0.8, -3.0, 8.0), _arma=(0.0, -1, 0.3), _mano_i=("h", 4.6, -3.6, -2.0))
    accion("salto", 2.1, [
        (0.00, L),
        (0.45, S_AGACHA),
        (0.62, S_AIRE),
        (1.05, con(S_AIRE, torso=(-18, 0, 0), _mano_d=("h", -1.0, 8.6, -1.2))),
        (1.13, con(S_AIRE, torso=(10, 0, 0), _mano_d=("h", -0.6, 6.0, 5.6), _arma=(0.0, -0.2, 1))),
        (1.20, S_CLAVA),
        (1.62, con(S_CLAVA, torso=(40, 0, 0))),
        (2.10, L),
    ], golpes=(1.20,))
    # ---- 9 aturdido: le han roto la guardia; se tambalea apoyado en el arpon y el farol se columpia
    AT = dict(raiz=raiz(-1.4, 2.6), torso=(-18, 10, 8), cabeza=(-16, -6, 14),
              muslo_d=(-10, 0, 8), pierna_d=(10, 0, 0), muslo_i=(16, 0, -8), pierna_i=(18, 0, 0),
              _mano_d=("h", -4.6, -6.4, -0.8), _arma=(-0.6, -0.7, 0.4), _mano_i=("h", 5.2, -4.0, 2.0), _arma_i=(0.25, -1, 0.1))
    accion("aturdido", 1.4, [
        (0.00, con(AT, raiz=raiz(-0.8, 0.6), torso=(4, 0, 0), cabeza=(0, 0, 4))),
        (0.22, AT),
        (0.65, con(AT, raiz=raiz(-1.8, 3.2), torso=(-12, -8, -8), cabeza=(-10, 8, -10), _arma_i=(-0.25, -1, -0.1))),
        (1.00, con(AT, raiz=raiz(-1.6, 2.8), torso=(-14, 6, 6), cabeza=(-14, -6, 10))),
        (1.40, con(L, raiz=raiz(-1.2, 1.0))),
    ])
    # ---- 10 morir: se tambalea, cae de rodillas agarrado al arpon clavado, deja el farol en el suelo y baja la cabeza
    RODILLAS = dict(raiz=raiz(-9.4, 0.4), torso=(14, 0, 0), cabeza=(30, 0, 0),
                    faldon_d=(10, 0, 0), faldon_i=(-6, 0, 0),
                    muslo_d=(-70, 0, 8), pierna_d=(70, 0, 0), muslo_i=(-8, 0, -6), pierna_i=(96, 0, 0),
                    _mano_d=("h", -0.4, -1.2, 4.6), _arma=(-0.05, 1, 0.12), _mano_i=("h", 2.2, -3.4, 4.4), _arma_i=ABAJO)
    accion("morir", 3.6, [
        (0.00, con(L, raiz=raiz(-1.6, 1.6), torso=(-12, 0, 6), cabeza=(-16, 0, 8))),
        (0.55, con(L, raiz=raiz(-3.2, 1.2), torso=(18, 0, 0), cabeza=(22, 0, 0), muslo_d=(-24, 0, 6), pierna_d=(20, 0, 0),
                   muslo_i=(-18, 0, -6), pierna_i=(44, 0, 0), _mano_d=("h", -0.6, -4.0, 4.4), _arma=(-0.05, 1, 0.1),
                   _mano_i=("h", 3.4, -5.0, 2.0))),
        (1.10, RODILLAS),
        (1.90, con(RODILLAS, torso=(20, 0, 0), cabeza=(38, 0, 0), _mano_d=("h", -0.4, -1.8, 4.8))),
        (3.60, con(RODILLAS, torso=(24, 0, 0), cabeza=(44, 0, 0), _mano_d=("h", -0.4, -2.2, 5.0))),
    ])
    # ---- 11 levantarse: de vigia a en guardia (apoya la mano, se alza sobre la pata de palo y baja el arpon)
    MEDIO_L = con(L, raiz=raiz(-5.0, 0.4), torso=(30, -6, 0), cabeza=(-12, 6, 0),
                  muslo_d=(-60, 0, 6), pierna_d=(60, 0, 0), muslo_i=(-10, 0, -6), pierna_i=(70, 0, 0),
                  _mano_d=("h", -0.8, -3.6, 4.2), _arma=(0.0, 0.7, 1), _mano_i=("h", 3.2, -6.0, 2.4))
    accion("levantarse", 1.6, [
        (0.00, VIGIA),
        (0.40, con(VIGIA, torso=(20, 10, 0), cabeza=(-4, 20, 0))),
        (0.95, MEDIO_L),
        (1.25, con(L, raiz=raiz(-2.4, 0.2), torso=(20, -12, 0), _mano_i=("h", 4.0, -4.8, 0.6))),
        (1.60, L),
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
