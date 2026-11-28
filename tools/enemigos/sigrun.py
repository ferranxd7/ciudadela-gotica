"""
Sigrun, la Guardiana del Paso (parte 7ap): la ultima de los nueve que el capitan de la Capital mando a guardar el Paso del
Colmillo la noche en que ardio la ciudad, para que la gente pudiera huir al monasterio. Juro que nadie pasaria mientras ella
siguiera en pie, y sigue en pie: el juramento la tiene ahi desde hace una vida, sin envejecer, con la escarcha en la armadura.

Una caballera alta (escala 1,05): el yelmo abierto de casquetes con su nasal y sus carrilleras, la cara palida con una
cicatriz y los ojos de azul de hielo (lucen), la trenza blanca que le cae por la espalda, el manto de piel de lobo con nieve
sobre los hombros, la coraza de acero con el tabardo azul de la guardia del Paso (un colmillo blanco en el pecho), la capa azul
rota hasta las rodillas, la cota de malla, los brazales y las grebas; en la derecha su espada larga y en el brazo izquierdo el
escudo de lagrima con el colmillo blanco.

Bucles: quieto (con la espada clavada en la nieve, la derecha en el puno: asi guarda el paso), porte (la espada colgando, para
andar), listo (en guardia, de perfil tras el escudo), andar, rodillas (rendida: de rodillas, apoyada en la espada clavada, la
cabeza gacha) y caida (tendida de lado en la nieve).

Acciones: desenvainar (arranca la espada de la nieve, la alza y se pone en guardia: empieza el duelo), tajo, combo (tres
golpes), escudazo, estocada (se lanza de lejos), bloquea, giro y salto (desde la media vida: la vuelta entera con la espada
tendida y el salto que cae con la espada en la nieve), grito (a media vida golpea el escudo con la espada: "Bien... ¡otra
vez!"), aturdido, rendirse (se tambalea y cae de rodillas), levantarse (de rodillas a en pie, apoyandose en la espada), saludo
(la empunadura al pecho y la cabeza baja: se despide), caer (de rodillas se vence de lado en la nieve) y plantar (clava la
espada en la nieve y se apoya: vuelve a guardar el paso).
"""
from anim import Anim, con, raiz, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide
from pinta import (claro, delante, dibujo, espejado, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "sigrun"
VARIANTES = ("sigrun",)
ESCALA = 1.05

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(8, 12, 4), cabeza=(8, 8, 8), brazo=(3, 12, 3), pierna=(4, 12, 4), hombro=0.6)
    # la coraza sobre la cota; el faldon (malla y el tabardo por delante y por detras) bajo el cinto
    r.pieza("coraza", "torso", (0, 0, 0), boxes=[Box(-4, -12, -2, 8, 12, 4, "coraza", grow=g(0.5))])
    r.pieza("faldon", "cadera", (0, 0, 0), boxes=[Box(-4, -0.5, -2, 8, 7, 4, "faldon", grow=g(0.42))])
    # el manto de piel de lobo sobre los hombros (con nieve encima) y la capa rota por la espalda
    r.pieza("manto", "torso", (0, 0, 0), boxes=[Box(-5.0, -12.9, -2.9, 10, 3.2, 5.8, "manto")])
    r.pieza("capa", "torso", (0, -10.4, 2.5), rot=(7, 0, 0), boxes=[Box(-4.6, 0, 0, 9.2, 18, 0.8, "capa")])
    for n in ("d", "i"):
        r.pieza(f"hombrera_{n}", f"brazo_{n}", (0, 0, 0), boxes=[Box(-2.3, -2.8, -2.3, 4.6, 3.0, 4.6, f"hombrera_{n}")])
    # ---- el yelmo abierto: el casquete, la punta, el nasal y las carrilleras
    r.pieza("yelmo", "cabeza", (0, 0, 0), boxes=[
        Box(-4, -8, -4, 8, 3.5, 8, "yelmo", grow=g(0.5)),
        Box(-1.0, -9.6, -1.0, 2, 1.4, 2, "punta"),
        Box(-0.5, -4.5, -4.7, 1, 2.6, 0.5, "nasal"),
        Box(-4.65, -4.5, -2.6, 0.5, 3.2, 3.8, "carrillera_d"),
        Box(4.15, -4.5, -2.6, 0.5, 3.2, 3.8, "carrillera_i"),
    ])
    # ---- la trenza blanca por la espalda (dos trozos y el lazo de cuero)
    r.pieza("trenza", "cabeza", (0, -2.6, 4.2), rot=(-8, 0, 0), boxes=[Box(-0.8, 0, -0.8, 1.6, 6, 1.6, "trenza")])
    r.pieza("trenza_b", "trenza", (0, 6, 0), rot=(-4, 0, 0), boxes=[Box(-0.65, 0, -0.65, 1.3, 3.5, 1.3, "trenza_b"),
                                                                 Box(-0.8, 3.5, -0.8, 1.6, 0.8, 1.6, "lazo")])
    # ---- la espada larga (apunta hacia delante, -Z, con el brazo caido): el puno, el pomo, la guarda, la hoja y la punta
    r.pieza("espada", "mano_d", (0, 0, 0), boxes=[
        Box(-0.5, -0.5, -2.0, 1, 1, 4, "puno"),
        Box(-1, -1, 2.0, 2, 2, 1.5, "pomo"),
        Box(-3.5, -0.5, -3.0, 7, 1, 1, "guarda"),
        Box(-0.5, -1, -20.0, 1, 2, 17, "hoja"),
        Box(-0.5, -0.5, -21.5, 1, 1, 1.5, "hoja_punta"),
    ])
    # ---- el escudo de lagrima en el antebrazo izquierdo (por fuera) y su umbo
    r.pieza("escudo", "antebrazo_i", (0, 0, 0), boxes=[
        Box(1.6, -3.6, -5.0, 1, 13, 10, "escudo"),
        Box(2.6, 0.6, -1.0, 0.6, 2, 2, "umbo"),
    ])
    r.empaquetar()
    return r


#: una sola: no se esconde nada
OCULTAS = {0: []}

# ---------------------------------------------------------------- las texturas

PIEL = ["#5e5a5c", "#7a7472", "#988f8a", "#b4aaa2", "#cdc3ba", "#e2dad2"]
OJO, OJO_B = "#8fd8ff", "#e4f8ff"
PELO = ["#7e858c", "#9aa1a7", "#b7bdc2", "#d2d6da", "#e8eaec", "#f6f7f8"]
ACERO = ["#1a1e23", "#2b3138", "#424a53", "#5d6771", "#7f8a95", "#a9b3bc"]
ESCARCHA = ["#bcd6e4", "#d9ecf5", "#f2fbff"]
MALLA = ["#202429", "#30363d", "#454c54", "#5d656e"]
TABARDO = ["#0b1322", "#121e36", "#1a2a4a", "#24395f", "#2f4877"]
LOBO = ["#3a3632", "#524d46", "#6b655c", "#857f74", "#a19b90", "#bcb6ab"]
NIEVE = "#f4f8fc"
CUERO = ["#17110c", "#241a12", "#33251a", "#443223"]
TABLA = ["#2a1e14", "#3a2a1c", "#4c3826", "#5e4630"]
BLANCO = "#e8edf2"
CICATRIZ = "#cfa9a0"

#: el colmillo blanco del tabardo y del escudo (4 x 5)
COLMILLO = [
    "WWWW",
    ".WWW",
    ".WWW",
    "..WW",
    "..W.",
]


def _acero(tonos, semilla, cuanto=31):
    """Acero con alguna mota de escarcha (una cada `cuanto` pixeles, mas o menos; mas arriba que abajo)."""
    base = metal(tonos, semilla)

    def f(cara, x, y, w, h):
        c = base(cara, x, y, w, h)
        k = (x * 7 + y * 11 + semilla * 3 + {"frente": 0, "espalda": 5, "der": 9, "izq": 3, "arriba": 2, "abajo": 6}.get(cara, 1)) % cuanto
        if cara == "arriba" and k < 6:
            return mezcla(c, ESCARCHA[1], 0.7)
        if k == 0 and y < h * 0.6:
            return mezcla(c, ESCARCHA[2], 0.55)
        if k in (1, 9) and y < h * 0.5:
            return mezcla(c, ESCARCHA[0], 0.35)
        return c
    return f


def _malla(semilla):
    """Cota de malla: anillas a tresbolillo, con poco contraste."""
    def f(cara, x, y, w, h):
        c = mezcla(MALLA[1], MALLA[2], 0.75 if (x + y) % 2 == 0 else 0.25)
        if (x * 5 + y * 3 + semilla) % 13 == 0:
            c = mezcla(c, MALLA[3], 0.5)
        return c
    return f


def _lobo(semilla):
    """La piel de lobo: mechones a lo alto (rayas claras y oscuras), mas oscura abajo."""
    r = ruido(LOBO, semilla, grano=0.5, base=0.55)

    def f(cara, x, y, w, h):
        c = r(cara, x, y, w, h)
        q = (x * 3 + (y // 2)) % 5
        if q == 0:
            c = oscuro(c, 0.18)
        elif q == 2:
            c = claro(c, 0.12)
        return oscuro(c, 0.25 * y / max(1, h - 1))
    return f


def _cara():
    C = {"S": PIEL[4], "s": PIEL[3], "o": PIEL[2], "O": PIEL[1], "K": "#1d1a1c", "B": OJO, "b": "#4a6a7c", "M": "#a88a82",
         "n": PIEL[5], "c": CICATRIZ, "H": PELO[3], "h": PELO[2]}
    return dibujo([
        "HhSSSShH",
        "hSSSSSSh",
        "sSnnSnnS",
        "sSssSScs",
        "sKBnsBKs",
        "sSSnSScs",
        "oSSMMSSo",
        "osSSSSso",
    ], C)


def materiales(variante):
    piel = ruido(PIEL[2:6], 611, grano=0.4, base=0.55)
    placa = _acero(ACERO[1:6], 621)
    placa_b = _acero(ACERO[0:5], 622, cuanto=23)
    malla = _malla(631)
    tab = tela(TABARDO, 641, pliegue=3, grano=0.14)
    lobo = _lobo(651)
    cuero = ruido(CUERO, 661, grano=0.4)
    pelo = ruido(PELO[1:6], 671, grano=0.5, base=0.6)

    def cabeza(cara, x, y, w, h):
        if cara == "frente":
            return _cara()(cara, x, y, w, h)
        if cara in ("espalda", "arriba"):
            # el pelo blanco, recogido (de aqui sale la trenza)
            c = pelo(cara, x, y, w, h)
            return oscuro(c, 0.12) if (x + y) % 3 == 0 else c
        # los costados: la oreja y la mejilla delante, el pelo detras
        d = delante(cara, x, w)
        if d <= 3 and y >= 3:
            c = piel(cara, x, y, w, h)
            return PIEL[1] if (d == 3 and y in (4, 5)) else c
        return pelo(cara, x, y, w, h)

    def torso(cara, x, y, w, h):
        if y == 10:
            return CUERO[1] if not (cara == "frente" and x in (3, 4)) else ACERO[5]
        return malla(cara, x, y, w, h)

    def coraza(cara, x, y, w, h):
        # el peto hasta la cintura (debajo se ve la malla y el cinto); el tabardo azul por delante y por detras con el colmillo
        if cara in ("arriba", "abajo"):
            return placa(cara, x, y, w, h) if cara == "arriba" else None
        if y >= 9:
            return None
        if cara in ("frente", "espalda"):
            if 1 <= x <= 6:
                c = tab(cara, x, y, w, h)
                if cara == "frente" and 3 <= y <= 7 and 2 <= x <= 5 and COLMILLO[y - 3][x - 2] == "W":
                    return mezcla(BLANCO, ESCARCHA[0], 0.25 if y > 5 else 0.0)
                if x in (1, 6):
                    return oscuro(c, 0.2)
                return c
        c = placa(cara, x, y, w, h)
        return oscuro(c, 0.25) if y == 8 else c

    def faldon(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if cara in ("frente", "espalda"):
            if 1 <= x <= 6:
                largo = (5, 6, 7, 6, 7, 5)[x - 1] if cara == "frente" else (6, 7, 6, 7, 5, 6)[x - 1]
                if y > largo:
                    return None
                c = tab(cara, x, y + 9, w, h + 9)
                return oscuro(c, 0.05 * y + (0.15 if x in (1, 6) else 0))
            return malla(cara, x, y, w, h) if y <= 5 else None
        return malla(cara, x, y, w, h) if y <= 5 else (None if (x + y) % 2 else malla(cara, x, y, w, h))

    def manto(cara, x, y, w, h):
        if cara == "abajo":
            return LOBO[0]
        if cara == "arriba":
            # nieve sobre los hombros, con algun mechon que asoma
            return NIEVE if (x * 5 + y * 3) % 7 else LOBO[4]
        c = lobo(cara, x, y, w, h)
        if y == 0 and (x * 3) % 4:
            return NIEVE
        if y == h - 1 and (x * 7) % 3 == 0:
            return None                                       # el borde, deshilachado
        return c

    def capa(cara, x, y, w, h):
        # la capa azul, raida, rota por abajo; por dentro mas oscura; escarcha en el borde de arriba
        largo = h - 1 - ((x * 7 + 3) % 5 if cara in ("frente", "espalda") else 0)
        if y > largo:
            return None
        c = tela(TABARDO, 681, pliegue=2, grano=0.18)(cara, x, y, w, h)
        if cara == "frente":
            c = oscuro(c, 0.35)
        if y <= 1 and (x % 3) == 0:
            c = mezcla(c, NIEVE, 0.6)
        if (x * 11 + y * 5) % 23 == 0:
            return None                                       # agujeros
        return c

    def brazo(cara, x, y, w, h):
        return malla(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        if y >= h - 2:
            return oscuro(placa_b(cara, x, y, w, h), 0.1) if cara != "abajo" else ACERO[1]
        if y <= 0:
            return malla(cara, x, y, w, h)
        c = placa(cara, x, y, w, h)
        return oscuro(c, 0.2) if y in (1, h - 3) else c

    def hombrera(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara == "arriba":
            return mezcla(c, NIEVE, 0.5) if (x + y) % 3 else c
        if y >= h - 1:
            return oscuro(c, 0.3)
        return c

    def muslo(cara, x, y, w, h):
        return malla(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        # la rodillera, la greba y la bota con su vuelta de piel
        if y == 0:
            return claro(placa(cara, x, y, w, h), 0.1)
        if y == h - 3:
            return LOBO[3] if (x % 2) else LOBO[2]
        if y >= h - 2:
            return CUERO[0] if cara != "abajo" else CUERO[1]
        if cara in ("frente", "der", "izq"):
            return placa_b(cara, x, y, w, h)
        return cuero(cara, x, y, w, h)

    def yelmo(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = placa(cara, x, y, w, h)
        if cara == "arriba":
            # los casquetes: dos franjas de remaches en cruz
            if x in (3, 4) or y in (3, 4):
                return oscuro(c, 0.25)
            return mezcla(c, ESCARCHA[1], 0.35) if (x + y) % 4 == 0 else c
        if y == h - 1:
            return oscuro(c, 0.35) if x % 2 else ACERO[5]  # la banda de abajo con sus remaches
        if x in (3, 4) and cara in ("frente", "espalda"):
            return oscuro(c, 0.22)
        return c

    def punta(cara, x, y, w, h):
        return claro(placa(cara, x, y, w, h), 0.1)

    def nasal(cara, x, y, w, h):
        return ACERO[4] if cara == "frente" and y < h - 1 else ACERO[3]

    def carrillera(cara, x, y, w, h):
        c = placa(cara, x, y, w, h)
        if cara in ("der", "izq") and y == 1 and x in (1, w - 2):
            return ACERO[5]                                   # un remache
        return c

    def trenza(cara, x, y, w, h):
        # la trenza: tres mechones que se cruzan (rayas en diagonal)
        q = (x + y) % 3
        return PELO[4] if q == 0 else (PELO[2] if q == 1 else PELO[3])

    hoja = _acero(ACERO[2:6], 701, cuanto=17)

    def hoja_f(cara, x, y, w, h):
        c = hoja(cara, x, y, w, h)
        if cara in ("der", "izq"):
            # el vaceo por el medio (a lo largo) y el filo claro con escarcha
            if y == 0 or y == h - 1:
                return mezcla(ACERO[5], ESCARCHA[2], 0.4)
            return oscuro(c, 0.12) if y == h // 2 else c
        return c

    def escudo(cara, x, y, w, h):
        # de lagrima: recto por arriba, en punta por abajo; el campo azul con el colmillo blanco, el cerco de acero
        if cara in ("arriba", "abajo", "frente", "espalda"):
            return ACERO[2]
        cx = abs(x - (w - 1) / 2.0)
        if y >= 6 and cx > (h - y) * 0.82 - 0.4:
            return None
        if x in (0, w - 1) or y == 0 or (y >= 6 and cx > (h - y) * 0.82 - 1.5):
            return ACERO[4] if (x + y) % 5 else mezcla(ACERO[4], ESCARCHA[2], 0.6)
        if cara == "der":
            return madera(TABLA, 711, "y")(cara, x, y, w, h)   # el reves, de tabla, con la correa de cuero
        c = tab(cara, x, y, w, h)
        fila = y - 3
        col = (x - 3) if cara == "izq" else (w - 1 - x - 3)
        if 0 <= fila < len(COLMILLO) and 0 <= col < 4 and COLMILLO[fila][col] == "W":
            return mezcla(BLANCO, ESCARCHA[0], 0.2 if fila > 2 else 0.0)
        if (x * 5 + y * 7) % 23 == 0:
            return oscuro(c, 0.3)                             # tajos viejos
        if y == 1 and x % 2:
            return mezcla(c, NIEVE, 0.6)                      # escarcha en el borde de arriba
        return c

    return {
        "cabeza": sombra(cabeza, abajo=0.12),
        "torso": sombra(torso), "coraza": sombra(coraza, abajo=0.16), "faldon": faldon,
        "manto": manto, "capa": sombra(capa, abajo=0.18),
        "brazo_d": sombra(brazo, abajo=0.1), "brazo_i": sombra(brazo, abajo=0.1),
        "antebrazo_d": sombra(antebrazo, abajo=0.14), "antebrazo_i": sombra(antebrazo, abajo=0.14),
        "hombrera_d": sombra(hombrera, abajo=0.12), "hombrera_i": sombra(hombrera, abajo=0.12),
        "muslo_d": sombra(muslo, abajo=0.1), "muslo_i": sombra(muslo, abajo=0.1),
        "pierna_d": sombra(pierna, abajo=0.18), "pierna_i": sombra(pierna, abajo=0.18),
        "yelmo": sombra(yelmo, abajo=0.1), "punta": punta, "nasal": nasal,
        "carrillera_d": sombra(carrillera, abajo=0.1), "carrillera_i": sombra(carrillera, abajo=0.1),
        "trenza": trenza, "trenza_b": trenza, "lazo": cuero,
        "puno": ruido(CUERO, 721, grano=0.4), "pomo": metal(ACERO[2:6], 722), "guarda": metal(ACERO[1:5], 723),
        "hoja": hoja_f, "hoja_punta": hoja,
        "escudo": escudo, "umbo": metal(ACERO[2:6], 724),
    }


def brillo(variante):
    """Lo que luce: los ojos de azul de hielo."""
    C = {"B": OJO, "K": "#3a5f78"}
    ojos = dibujo([
        "........",
        "........",
        "........",
        "........",
        ".KB..BK.",
    ], C)
    return {"cabeza": por_cara(liso(None), frente=ojos)}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 9)
# y _arma = hacia donde apunta lo que lleva.

BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
# de pie guardando el paso: firme, los pies algo abiertos
BASE = dict(raiz=raiz(-0.2), torso=(2, 0, 0), cabeza=(5, 0, 0),
            muslo_d=(0, 0, 4), pierna_d=(2, 0, 0), muslo_i=(0, 0, -4), pierna_i=(2, 0, 0))
# la espada clavada delante en la nieve, la derecha en el puno; el escudo colgando del brazo izquierdo, junto a la pierna
CLAVADA = dict(_mano_d=("h", 3.4, -3.8, 4.6), _arma=(0.0, -1.0, 0.06), brazo_i=(-4, 0, -9), antebrazo_i=(-14, 0, 0))
QUIETO = con(BASE, CLAVADA)
# la espada colgando de la derecha (para andar), el escudo igual
PORTE = dict(_mano_d=("h", -1.0, -8.4, 1.4), _arma=(-0.05, -0.8, 0.6), brazo_i=(-6, 0, -8), antebrazo_i=(-20, 0, 0))
# en guardia: de perfil tras el escudo (el hombro izquierdo delante), la espada atras, a la altura de la cadera
GUARDIA = dict(raiz=raiz(-1.0), torso=(8, 0, 0), cabeza=(-4, 0, 0),
               muslo_d=(10, 0, 5), pierna_d=(14, 0, 0), muslo_i=(-20, 0, -5), pierna_i=(22, 0, 0))
LISTO = con(GUARDIA, torso=(8, 24, 0), cabeza=(-4, -22, 0),
            _mano_d=("h", -1.0, -5.6, 2.6), _arma=(0.1, 0.35, 1),
            _mano_i=("h", -3.6, -2.6, 6.2), _arma_i=(-0.2, 1, 0.2))
# rendida: la rodilla derecha en la nieve, la izquierda delante, la espada clavada delante de ella con la derecha en el puno, a la
# altura de la cara, y la frente casi en la empunadura
RODILLAS = dict(raiz=raiz(-6.4, 0.6), torso=(14, 4, 0), cabeza=(30, -4, 0),
                muslo_d=(-4, 0, 6), pierna_d=(96, 0, 0), muslo_i=(-84, 0, -6), pierna_i=(84, 0, 0),
                _mano_d=("h", 2.4, 2.4, 6.0), _arma=(0.0, -1.0, -0.04), brazo_i=(-30, 0, -10), antebrazo_i=(-40, 0, 0))
# tendida boca arriba en la nieve, como en una tumba: la derecha en el pecho con la espada encima, hacia los pies
CAIDA = dict(raiz=raiz(2.6, 0.0, rot=(-86, 0, 0)), torso=(0, 0, 0), cabeza=(8, -10, 0),
             muslo_d=(0, 0, 3), pierna_d=(4, 0, 0), muslo_i=(0, 0, -3), pierna_i=(6, 0, 0),
             _mano_d=("h", 5.0, 3.2, 3.6), _arma=(0.0, -0.08, 1), brazo_i=(0, 0, -12), antebrazo_i=(-6, 0, 0))


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

    # ---- quieto: guarda el paso con la espada clavada; respira, mira a un lado y a otro
    bucle("quieto", 6.0, [
        (0.0, QUIETO),
        (1.5, con(QUIETO, raiz=raiz(-0.4), torso=(3, 2, 0), cabeza=(6, -14, 0))),
        (3.0, con(QUIETO, raiz=raiz(-0.1), torso=(1, 0, 0), cabeza=(3, 0, 0))),
        (4.5, con(QUIETO, raiz=raiz(-0.4), torso=(3, -2, 0), cabeza=(6, 14, 0))),
        (6.0, QUIETO),
    ])
    # ---- la espada colgando (para andar; se suma a andar)
    q = con(BASE, PORTE)
    bucle("porte", 3.0, [(0.0, q), (3.0, q)], solo=BRAZOS)
    # ---- andar: pasos firmes
    pasos = []
    for k, (md, mi, pd, pi_, ty, up) in enumerate((
            (-26, 20, 6, 16, 4, 0.0), (-2, 2, 4, 34, 0, 0.6), (20, -26, 16, 6, -4, 0.0), (2, -2, 34, 4, 0, 0.6), (-26, 20, 6, 16, 4, 0.0))):
        pasos.append((1.2 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (3, ty, -ty * 0.3), "cabeza": (0, -ty * 0.7, ty * 0.3), "raiz": raiz(up),
                                    "capa": (6 + abs(ty) * 1.5, 0, 0), "trenza": (4, 0, -ty)}))
    bucle("andar", 1.2, pasos)
    # ---- en guardia, tras el escudo
    bucle("listo", 2.0, [
        (0.0, LISTO),
        (1.0, con(LISTO, raiz=raiz(-1.5), torso=(10, 22, 0))),
        (2.0, LISTO),
    ])
    # ---- rendida: de rodillas, apoyada en la espada; respira hondo
    bucle("rodillas", 4.0, [
        (0.0, RODILLAS),
        (1.2, con(RODILLAS, raiz=raiz(-6.6, 0.4), torso=(22, 6, 0), cabeza=(34, -4, 0))),
        (2.0, con(RODILLAS, raiz=raiz(-6.3, 0.6), torso=(16, 6, 0), cabeza=(28, -6, 0))),
        (3.2, con(RODILLAS, raiz=raiz(-6.6, 0.4), torso=(21, 6, 0), cabeza=(33, -4, 0))),
        (4.0, RODILLAS),
    ])
    # ---- tendida en la nieve (no se mueve)
    bucle("caida", 4.0, [(0.0, CAIDA), (4.0, CAIDA)])

    # ================================================================ acciones
    E = LISTO
    # ---- desenvainar: arranca la espada de la nieve, la alza al cielo y baja a la guardia (empieza el duelo)
    TIRA = con(QUIETO, raiz=raiz(-0.9), torso=(10, -6, 0), cabeza=(8, 0, 0),
               _mano_d=("h", 3.0, -2.2, 4.2), _arma=(0.0, -1.0, 0.1), brazo_i=(-10, 0, -14), antebrazo_i=(-30, 0, 0))
    SACA = con(BASE, raiz=raiz(-0.6), torso=(-2, 26, 0), cabeza=(-6, -16, 0),
               _mano_d=("h", -3.0, 1.5, 3.0), _arma=(-0.4, 1.0, 0.25), brazo_i=(-16, 0, -16), antebrazo_i=(-40, 0, 0))
    ALZA = con(BASE, raiz=raiz(0.2), torso=(-6, 6, 0), cabeza=(-18, 0, 0),
               _mano_d=("h", 1.2, 8.2, 1.6), _arma=(0.0, 1.0, -0.08), brazo_i=(-30, 0, -24), antebrazo_i=(-56, 0, 0))
    accion("desenvainar", 2.2, [
        (0.00, QUIETO),
        (0.35, TIRA),
        (0.60, con(TIRA, raiz=raiz(-0.7), torso=(6, -2, 0), _mano_d=("h", 2.6, -0.4, 4.0))),
        (0.85, SACA),
        (1.20, ALZA),
        (1.45, con(ALZA, cabeza=(-22, 0, 0))),
        (1.80, con(E, raiz=raiz(-0.8), torso=(6, 18, 0))),
        (2.20, E),
    ])
    # ---- el tajo de arriba abajo, cruzado (la levanta sobre el hombro; se ve venir)
    ARRIBA = con(E, raiz=raiz(-0.6, 1.2), torso=(-6, 40, 0), cabeza=(0, -34, 0),
                 _mano_d=("h", -2.0, 6.6, -1.0), _arma=(-0.2, 0.8, -0.55), _mano_i=("h", -3.0, -2.0, 6.8))
    CORTE = con(E, raiz=raiz(-1.6, -1.6), torso=(14, -8, 0), cabeza=(-8, 6, 0),
                _mano_d=("h", 1.0, -1.0, 8.2), _arma=(0.25, 0.1, 1), _mano_i=("h", -1.0, -3.0, 5.0))
    ABAJO_E = con(E, raiz=raiz(-2.4, -2.8), torso=(26, -34, 0), cabeza=(-16, 28, 0),
                  muslo_i=(-40, 0, -5), pierna_i=(36, 0, 0),
                  _mano_d=("h", 5.0, -6.0, 4.5), _arma=(0.6, -0.6, 0.5), _mano_i=("h", 2.0, -4.0, 2.0))
    accion("tajo", 1.4, [
        (0.00, E),
        (0.45, ARRIBA),
        (0.66, con(ARRIBA, raiz=raiz(-0.4, 1.5), torso=(-9, 44, 0), _mano_d=("h", -2.0, 6.9, -1.6))),
        (0.74, CORTE),
        (0.84, ABAJO_E),
        (1.08, con(ABAJO_E, torso=(24, -36, 0))),
        (1.40, E),
    ], golpes=(0.74,))
    # ---- la tanda: de fuera adentro, de vuelta, y una estocada a fondo
    FUERA = con(E, raiz=raiz(-1.0, 0.6), torso=(6, 42, 0), cabeza=(-4, -36, 0),
                _mano_d=("h", -6.2, -0.5, 1.5), _arma=(-1, 0.2, 0.1), _mano_i=("h", -3.0, -2.4, 6.6))
    PASA = con(E, raiz=raiz(-1.4, -1.0), torso=(10, 4, 0), cabeza=(-6, -2, 0),
               _mano_d=("h", 0.5, -0.8, 8.4), _arma=(0.0, 0.05, 1), _mano_i=("h", -0.5, -3.2, 4.6))
    DENTRO = con(E, raiz=raiz(-1.8, -1.6), torso=(12, -36, 0), cabeza=(-8, 30, 0),
                 muslo_i=(-34, 0, -5), pierna_i=(30, 0, 0),
                 _mano_d=("h", 6.0, -1.5, 4.6), _arma=(1, 0.05, 0.3), _mano_i=("h", 3.0, -3.5, 1.0))
    ATRAS_E = con(E, raiz=raiz(-1.6, -1.2), torso=(6, 34, 0), cabeza=(-4, -30, 0),
                  muslo_i=(-30, 0, -5), pierna_i=(28, 0, 0),
                  _mano_d=("h", -1.0, -4.8, -4.0), _arma=(0.05, 0.05, 1), _mano_i=("h", -3.0, -2.4, 6.8))
    FONDO_E = con(E, raiz=raiz(-2.6, -5.0), torso=(22, -38, 0), cabeza=(-16, 32, 0),
                  muslo_i=(-52, 0, -5), pierna_i=(46, 0, 0), muslo_d=(26, 0, 5), pierna_d=(8, 0, 0),
                  _mano_d=("h", 2.6, -1.6, 8.6), _arma=(0.0, 0.0, 1), _mano_i=("h", 3.5, -4.0, -1.0))
    accion("combo", 2.3, [
        (0.00, E),
        (0.40, FUERA),
        (0.56, con(FUERA, torso=(6, 46, 0), cabeza=(-4, -40, 0))),
        (0.64, PASA),
        (0.74, DENTRO),
        (0.98, con(DENTRO, torso=(12, -40, 0), cabeza=(-8, 34, 0))),
        (1.16, con(PASA, raiz=raiz(-1.8, -2.6), muslo_i=(-38, 0, -5), pierna_i=(34, 0, 0))),
        (1.25, con(FUERA, raiz=raiz(-1.8, -3.0), muslo_i=(-38, 0, -5), pierna_i=(34, 0, 0))),
        (1.46, con(ATRAS_E, raiz=raiz(-1.6, -2.6))),
        (1.68, con(ATRAS_E, raiz=raiz(-1.6, -2.2), torso=(4, 38, 0), _mano_d=("h", -1.0, -4.8, -4.8))),
        (1.76, FONDO_E),
        (2.00, con(FONDO_E, torso=(20, -36, 0))),
        (2.30, E),
    ], golpes=(0.64, 1.16, 1.76))
    # ---- el escudazo: recoge el escudo y lo estampa con un paso (empuja)
    RECOGE = con(E, raiz=raiz(-1.4, 1.4), torso=(4, -8, 0), cabeza=(0, 6, 0),
                 _mano_i=("h", 1.0, -3.0, 1.5), _mano_d=("h", -1.5, -6.0, 1.0), _arma=(0.1, -0.2, 1))
    EMPUJA = con(E, raiz=raiz(-1.8, -3.6), torso=(16, 46, 0), cabeza=(-8, -40, 0),
                 muslo_i=(-46, 0, -5), pierna_i=(40, 0, 0),
                 _mano_i=("h", -2.0, -1.0, 8.6), _mano_d=("h", -2.0, -6.0, -2.0), _arma=(0.0, -0.3, 1))
    accion("escudazo", 1.2, [
        (0.00, E),
        (0.36, RECOGE),
        (0.50, con(RECOGE, raiz=raiz(-1.5, 1.7))),
        (0.56, EMPUJA),
        (0.80, con(EMPUJA, raiz=raiz(-1.8, -3.4))),
        (1.20, E),
    ], golpes=(0.56,))
    # ---- la estocada de lejos: se echa atras y se lanza a fondo (cruza tres o cuatro bloques)
    ATRAS_L = con(E, raiz=raiz(-2.2, 2.0), torso=(4, 40, 0), cabeza=(-2, -36, 0),
                  muslo_d=(20, 0, 5), pierna_d=(30, 0, 0), muslo_i=(-24, 0, -5), pierna_i=(26, 0, 0),
                  _mano_d=("h", -1.4, -3.4, -5.0), _arma=(0.05, 0.1, 1), _mano_i=("h", -3.0, -2.0, 6.8))
    accion("estocada", 1.6, [
        (0.00, E),
        (0.50, ATRAS_L),
        (0.82, con(ATRAS_L, raiz=raiz(-2.4, 2.4), torso=(2, 44, 0))),
        (0.92, FONDO_E),
        (1.20, con(FONDO_E, torso=(20, -36, 0))),
        (1.60, E),
    ], golpes=(0.92,))
    # ---- le han dado en el escudo: lo aguanta
    accion("bloquea", 0.5, [
        (0.00, E),
        (0.08, con(E, raiz=raiz(-1.6, 1.2), torso=(2, 30, 0), _mano_i=("h", -3.2, -1.6, 7.4))),
        (0.25, con(E, raiz=raiz(-1.2, 0.6), torso=(6, 26, 0))),
        (0.50, E),
    ])
    # ---- el giro (desde la media vida): la vuelta entera con la espada tendida a su derecha
    TENDIDA = dict(_mano_d=("h", -6.4, -1.2, 2.6), _arma=(-1, -0.05, 0.25), _mano_i=("h", 1.0, -3.2, 3.5), _arma_i=(0.2, 1, 0.1))
    ARMA_G = con(GUARDIA, raiz=raiz(-1.6, 0.6), torso=(10, 46, 0), cabeza=(-6, -40, 0),
                 _mano_d=("h", 1.0, -2.0, -3.0), _arma=(0.6, 0.1, -0.8), _mano_i=("h", -2.0, -3.0, 5.5), _arma_i=(-0.2, 1, 0.2))
    VUELTA = con(GUARDIA, raiz=raiz(-1.8), torso=(12, 0, 0), cabeza=(-6, 0, 0), muslo_i=(-30, 0, -5), pierna_i=(28, 0, 0))
    accion("giro", 1.8, [
        (0.00, E),
        (0.50, ARMA_G),
        (0.66, con(VUELTA, TENDIDA, raiz=raiz(-1.8, rot=(0, -90, 0)))),
        (0.82, con(VUELTA, TENDIDA, raiz=raiz(-2.0, rot=(0, -200, 0)))),
        (0.98, con(VUELTA, TENDIDA, raiz=raiz(-1.8, rot=(0, -300, 0)))),
        (1.12, con(VUELTA, TENDIDA, raiz=raiz(-1.6, rot=(0, -360, 0)))),
        (1.40, con(E, raiz=raiz(-1.2, rot=(0, -360, 0)))),
        (1.80, con(E, raiz=raiz(-1.0, rot=(0, -360, 0)))),
    ], golpes=(0.82,))
    # ---- el salto (desde la media vida): se agacha con la espada atras, salta (la lanza el servidor) y cae con la espada en la nieve
    AGACHA = con(GUARDIA, raiz=raiz(-4.0, 1.0), torso=(24, 10, 0), cabeza=(-14, -8, 0),
                 muslo_d=(-40, 0, 6), pierna_d=(70, 0, 0), muslo_i=(-56, 0, -6), pierna_i=(76, 0, 0),
                 _mano_d=("h", -2.4, -2.0, -4.6), _arma=(-0.1, 0.4, -1), _mano_i=("h", -1.0, -3.0, 5.4), _arma_i=(-0.2, 1, 0.2))
    VUELA = con(GUARDIA, raiz=raiz(0.6, -0.6), torso=(-14, 4, 0), cabeza=(-16, -2, 0),
                muslo_d=(-30, 0, 6), pierna_d=(50, 0, 0), muslo_i=(-10, 0, -6), pierna_i=(30, 0, 0),
                _mano_d=("h", 0.8, 8.4, -0.6), _arma=(0.0, 0.7, -0.7), brazo_i=(-60, 0, -40), antebrazo_i=(-30, 0, 0))
    CAE = con(GUARDIA, raiz=raiz(-5.6, -1.2), torso=(40, 0, 0), cabeza=(-24, 0, 0),
              muslo_d=(-70, 0, 6), pierna_d=(110, 0, 0), muslo_i=(-30, 0, -6), pierna_i=(96, 0, 0),
              _mano_d=("h", 3.6, -5.0, 7.0), _arma=(0.1, -0.75, 0.65), brazo_i=(-20, 0, -40), antebrazo_i=(-30, 0, 0))
    accion("salto", 2.0, [
        (0.00, E),
        (0.40, AGACHA),
        (0.56, con(AGACHA, raiz=raiz(-4.4, 1.2))),
        (0.72, VUELA),
        (1.04, con(VUELA, torso=(-18, 4, 0), _mano_d=("h", 0.4, 8.8, -1.2))),
        (1.20, CAE),
        (1.50, con(CAE, raiz=raiz(-5.4, -1.2))),
        (2.00, E),
    ], golpes=(1.20,))
    # ---- el grito (a media vida): se yergue y golpea dos veces el escudo con la espada ("Bien... ¡otra vez!")
    YERGUE = con(BASE, raiz=raiz(0.0), torso=(-4, 20, 0), cabeza=(-10, -14, 0),
                 _mano_i=("h", -2.0, -1.4, 6.0), _arma_i=(-0.3, 1, 0.1))
    GOLPE_E = con(YERGUE, _mano_d=("h", 5.4, 0.6, 5.4), _arma=(0.7, 0.55, 0.4))
    ATRAS_G = con(YERGUE, _mano_d=("h", -2.6, 2.4, 2.0), _arma=(-0.5, 0.8, 0.2))
    accion("grito", 1.8, [
        (0.00, E),
        (0.30, ATRAS_G),
        (0.50, GOLPE_E),
        (0.70, ATRAS_G),
        (0.90, GOLPE_E),
        (1.25, con(YERGUE, _mano_d=("h", -1.4, -3.6, 3.0), _arma=(0.0, 0.6, 1), cabeza=(-20, -14, 0))),
        (1.80, E),
    ])
    # ---- aturdido: le han parado el golpe o roto la guardia; se tambalea con los brazos abiertos
    AT = dict(raiz=raiz(-1.4, 2.4), torso=(-18, 10, 8), cabeza=(-16, -6, 12),
              muslo_d=(-12, 0, 8), pierna_d=(26, 0, 0), muslo_i=(16, 0, -8), pierna_i=(18, 0, 0),
              _mano_d=("h", -5.0, -6.6, -1.0), _arma=(-0.7, -0.6, 0.4), brazo_i=(10, 0, -34), antebrazo_i=(-30, 0, 0))
    accion("aturdido", 1.5, [
        (0.00, con(AT, raiz=raiz(-0.8, 0.6), torso=(4, 0, 0), cabeza=(0, 0, 4))),
        (0.22, AT),
        (0.70, con(AT, raiz=raiz(-1.8, 3.0), torso=(-14, -8, -8), cabeza=(-10, 8, -10))),
        (1.10, con(AT, raiz=raiz(-1.6, 2.6), torso=(-16, 6, 6), cabeza=(-14, -6, 10))),
        (1.50, E),
    ])
    # ---- rendirse: se tambalea hacia atras, baja la espada y cae de rodillas apoyandose en ella
    TAMBALEA = dict(raiz=raiz(-1.2, 2.0), torso=(-12, 8, 6), cabeza=(-14, -4, 8),
                    muslo_d=(-10, 0, 8), pierna_d=(22, 0, 0), muslo_i=(12, 0, -8), pierna_i=(16, 0, 0),
                    _mano_d=("h", -3.4, -7.0, 2.0), _arma=(-0.4, -0.8, 0.5), brazo_i=(4, 0, -24), antebrazo_i=(-26, 0, 0))
    BAJA = con(RODILLAS, raiz=raiz(-3.6, 0.8), torso=(26, 4, 0), cabeza=(20, 0, 0),
               muslo_d=(-30, 0, 6), pierna_d=(60, 0, 0), muslo_i=(-60, 0, -6), pierna_i=(60, 0, 0))
    accion("rendirse", 2.2, [
        (0.00, E),
        (0.30, TAMBALEA),
        (0.60, con(TAMBALEA, raiz=raiz(-1.6, 2.4), torso=(-6, 6, 4), cabeza=(-4, -2, 4))),
        (1.00, BAJA),
        (1.40, con(RODILLAS, torso=(26, 6, 0), cabeza=(36, -4, 0))),
        (2.20, RODILLAS),
    ])
    # ---- levantarse: de rodillas a en pie, apoyandose en la espada; acaba con la espada colgando
    EN_PIE = con(BASE, PORTE)
    accion("levantarse", 1.8, [
        (0.00, RODILLAS),
        (0.50, con(RODILLAS, raiz=raiz(-5.6, 0.4), torso=(28, 6, 0), cabeza=(10, -4, 0))),
        (1.10, con(BASE, raiz=raiz(-2.4, 0.2), torso=(20, 4, 0), cabeza=(0, 0, 0), muslo_i=(-46, 0, -5), pierna_i=(52, 0, 0),
                   _mano_d=("h", 1.4, -5.4, 4.6), _arma=(-0.05, -1.0, 0.1), brazo_i=(-10, 0, -10), antebrazo_i=(-24, 0, 0))),
        (1.80, EN_PIE),
    ])
    # ---- el saludo: la empunadura al pecho con la hoja hacia arriba, la cabeza baja; y vuelve a colgar la espada
    SALUDA = con(BASE, torso=(4, 0, 0), cabeza=(22, 0, 0),
                 _mano_d=("h", 4.2, -2.6, 3.0), _arma=(0.0, 1.0, 0.12), brazo_i=(-6, 0, -8), antebrazo_i=(-20, 0, 0))
    accion("saludo", 1.8, [
        (0.00, EN_PIE),
        (0.45, con(SALUDA, cabeza=(0, 0, 0))),
        (0.80, SALUDA),
        (1.25, SALUDA),
        (1.80, EN_PIE),
    ])
    # ---- caer: de rodillas se le escurre la espada, se sienta hacia atras y queda tendida boca arriba con la espada en el pecho
    SUELTA = con(RODILLAS, torso=(24, 4, 0), cabeza=(44, -4, 0), _mano_d=("h", 3.0, -1.0, 6.0), _arma=(0.05, -1.0, 0.1))
    SIENTA = dict(raiz=raiz(-8.0, 2.0, rot=(-24, 0, 0)), torso=(-6, 0, 0), cabeza=(30, -6, 0),
                  muslo_d=(-70, 0, 6), pierna_d=(110, 0, 0), muslo_i=(-80, 0, -6), pierna_i=(100, 0, 0),
                  _mano_d=("h", 4.0, -4.0, 5.0), _arma=(0.0, -0.6, 0.8), brazo_i=(-20, 0, -14), antebrazo_i=(-30, 0, 0))
    TUMBA = dict(raiz=raiz(-2.0, 1.4, rot=(-62, 0, 0)), torso=(0, 0, 0), cabeza=(14, -8, 0),
                 muslo_d=(-40, 0, 4), pierna_d=(60, 0, 0), muslo_i=(-46, 0, -4), pierna_i=(64, 0, 0),
                 _mano_d=("h", 5.0, 2.0, 3.6), _arma=(0.0, -0.3, 1), brazo_i=(-4, 0, -12), antebrazo_i=(-10, 0, 0))
    accion("caer", 2.6, [
        (0.00, RODILLAS),
        (0.60, SUELTA),
        (1.20, SIENTA),
        (1.80, TUMBA),
        (2.20, con(CAIDA, raiz=raiz(2.4, 0.0, rot=(-80, 0, 0)))),
        (2.60, CAIDA),
    ])
    # ---- plantar: alza la espada y la clava en la nieve delante, y se apoya (vuelve a guardar el paso)
    ALZA_P = con(BASE, raiz=raiz(0.0), torso=(-2, 0, 0), cabeza=(0, 0, 0),
                 _mano_d=("h", 3.0, 0.4, 5.0), _arma=(0.0, -1.0, 0.08), brazo_i=(-4, 0, -9), antebrazo_i=(-14, 0, 0))
    accion("plantar", 1.4, [
        (0.00, EN_PIE),
        (0.45, ALZA_P),
        (0.62, con(ALZA_P, raiz=raiz(-0.4), torso=(6, 0, 0), _mano_d=("h", 3.4, -5.0, 4.6))),
        (0.80, con(QUIETO, raiz=raiz(-0.6), torso=(6, 0, 0))),
        (1.40, QUIETO),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    """Las animaciones que el juego pone debajo de esta (para verla como se vera)."""
    if nombre == "andar":
        return [anims["porte"]]
    return []


#: lo que va ademas en SigrunAnims
TABLA_EXTRA = (
    "",
    f"    public static final float ESCALA = {ESCALA}f;",
)
