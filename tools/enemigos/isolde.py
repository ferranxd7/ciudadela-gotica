"""
La reina Isolde (parte 7ab): lo que la Sombra devolvio con la cara de la reina, sentado en el trono de la casa de la Sombra. Alta
(x1,5), flota un palmo sobre el suelo: el vestido de luto, negro y granate, cae hasta el suelo y se arrastra en una cola; el corpino
con su cuello alto de encaje palido abierto en abanico detras de la cabeza, las mangas acampanadas y, debajo, los brazos largos y
blancos acabados en garras negras. La corona de hierro negro de puntas altas con sus gemas rojas, el pelo negro hasta la cintura y
el velo de encaje negro que le tapa la cara y le cae por la espalda.

Dos variantes (la misma reina):
  0 velada: el velo entero.
  1 sin velo (a media vida el velo arde: "¿Quieres verme la cara?"): del velo quedan unos jirones quemados en la corona y se le ve
    la cara: de porcelana agrietada, los ojos rojos y las lagrimas negras (las grietas y los ojos lucen).
"""
from anim import Anim, con, raiz, sin  # noqa: F401
from modelo import Box, Rig, g, humanoide  # noqa: F401
from pinta import (claro, delante, dibujo, liso, mezcla, metal, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "isolde"
VARIANTES = ("velada", "sin_velo")
ESCALA = 1.5
CENTRO_Y = 1.5

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    humanoide(r, torso=(7, 12, 4), cabeza=(7, 8, 7), brazo=(3, 15, 3), pierna=(4, 12, 4), hombro=0.6)
    # ---- el vestido (en dos trozos, para que ondee) hasta el suelo y la cola que se arrastra detras
    r.pieza("vestido", "cadera", (0, -0.5, 0), boxes=[Box(-4.6, 0, -2.8, 9.2, 7.5, 5.6, "vestido")])
    r.pieza("vestido_bajo", "vestido", (0, 7.0, 0), boxes=[Box(-6.4, 0, -3.8, 12.8, 8.6, 7.6, "vestido_bajo")])
    r.pieza("cola", "cadera", (0, 0.5, 2.6), rot=(30, 0, 0), boxes=[Box(-5.4, 0, 0, 10.8, 18, 0.8, "cola")])
    # ---- el corpino (encima del torso), el cuello alto de encaje en abanico y las mangas acampanadas
    r.pieza("corpino", "torso", (0, 0, 0), boxes=[Box(-3.5, -12, -2, 7, 12, 4, "corpino", grow=g(0.35))])
    r.pieza("cuello", "torso", (0, -11.6, 1.6), rot=(-18, 0, 0), boxes=[Box(-6.2, -9.0, 0, 12.4, 9.4, 0.6, "cuello")])
    for n in ("d", "i"):
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 1.6, 0), boxes=[Box(-2.6, 0, -2.6, 5.2, 6.4, 5.2, f"manga_{n}")])
        # las garras: cinco unas largas y negras (en la textura, separadas)
        r.pieza(f"garra_{n}", f"mano_{n}", (0, 0, 0), boxes=[Box(-1.5, -0.6, -1.5, 3.0, 7.2, 3.0, f"garras_{n}")])
    # ---- el pelo negro por la espalda, la corona de puntas y el velo (delante, sobre la cara; detras, largo)
    r.pieza("pelo", "cabeza", (0, -7.4, 3.0), boxes=[Box(-4.0, 0, 0, 8.0, 18, 1.2, "pelo")])
    r.pieza("corona", "cabeza", (0, 0, 0), boxes=[
        Box(-4.1, -9.2, -4.1, 8.2, 1.6, 8.2, "corona"),
        Box(-0.6, -12.6, -4.5, 1.2, 3.6, 1.0, "punta"),
        Box(-3.6, -11.4, -4.4, 1.0, 2.4, 1.0, "punta"),
        Box(2.6, -11.4, -4.4, 1.0, 2.4, 1.0, "punta"),
        Box(-4.4, -11.0, -0.5, 1.0, 2.0, 1.0, "punta"),
        Box(3.4, -11.0, -0.5, 1.0, 2.0, 1.0, "punta"),
        Box(-0.6, -11.4, 3.4, 1.2, 2.4, 1.0, "punta"),
        Box(-0.7, -8.6, -4.6, 1.4, 1.4, 0.6, "gema"),
    ])
    r.pieza("velo_frente", "cabeza", (0, -8.0, -4.4), boxes=[Box(-4.3, -0.6, -0.5, 8.6, 14.5, 0.5, "velo_frente")])
    r.pieza("velo_espalda", "cabeza", (0, -8.0, 4.0), rot=(8, 0, 0), boxes=[Box(-4.6, -0.6, 0, 9.2, 26, 0.6, "velo_espalda")])
    r.pieza("velo_roto", "cabeza", (0, 0, 0), boxes=[Box(-4.0, -8.9, -4.0, 8.0, 2.6, 8.0, "velo_roto", grow=g(0.25))])
    r.empaquetar()
    return r


PIERNAS = ["muslo_d", "muslo_i", "pierna_d", "pierna_i"]
#: lo que se esconde en cada variante
OCULTAS = {0: PIERNAS + ["velo_roto"], 1: PIERNAS + ["velo_frente", "velo_espalda"]}

# ---------------------------------------------------------------- las texturas

LUTO = ["#0b0507", "#14080c", "#1f0b12", "#2c0f19", "#3d1422", "#521a2c"]
GRANATE = ["#3a0712", "#560b1a", "#741124", "#931a30"]
ENCAJE = ["#6e6a72", "#8f8a92", "#b3aeb5", "#d4cfd5"]
PIEL = ["#9b958f", "#b8b2ab", "#d2ccc5", "#e6e1db", "#f4f0eb"]
GRIETA = "#2b1a1e"
NEGRO = "#050304"
HIERRO = ["#0e0d10", "#1a191d", "#2a282e", "#3e3b43"]
ROJO = "#e3231c"
LAGRIMA = "#09050a"


def materiales(variante):
    luto = tela(LUTO, 901, pliegue=4, grano=0.12, luz=0.28)
    granate = tela(GRANATE, 902, pliegue=3, grano=0.12, luz=0.2)
    encaje = ruido(ENCAJE, 903, grano=0.35)
    piel = ruido(PIEL[1:5], 904, grano=0.3, base=0.6)
    hierro = metal(HIERRO, 905)

    def calado(c, x, y, semilla, cada=5):
        # el encaje: con sus agujeros (dibujo de rombos)
        if (x + y * 2 + semilla) % cada == 0 or (x * 3 + y + semilla) % (cada + 2) == 0:
            return None
        return c

    def cabeza(cara, x, y, w, h):
        c = piel(cara, x, y, w, h)
        if cara == "frente":
            # la cara: de porcelana, con las cuencas hondas, los ojos (lucen en el brillo), las lagrimas negras y las grietas
            C = {"K": "#1c1216", "s": PIEL[1], "S": PIEL[3], "b": "#4a1a24", "T": LAGRIMA, "g": GRIETA}
            cara_ = dibujo([
                "SSSgSSS",
                "SsSSgSS",
                "sKKSKKs",
                "sKTSKTs",
                "sSTSgTs",
                "sSTbbTs",
                "ssSbbSs",
                "sssSsss",
            ], C)
            v = cara_(cara, x, y, w, h)
            return v if v is not None else c
        if cara == "arriba" or (cara in ("der", "izq", "espalda") and y <= 3):
            return mezcla(NEGRO, LUTO[2], 0.25 * ((x + y) % 3))       # el pelo, recogido bajo la corona
        if cara in ("der", "izq") and delante(cara, x, w) <= 1 and y in (4, 5):
            return oscuro(c, 0.25)
        return c

    def corpino(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = luto(cara, x, y, w, h)
        if cara == "frente":
            # el peto en pico, granate, con la lazada negra
            if abs(x - 3) < (12 - y) * 0.32 and y <= 10:
                g_ = granate(cara, x, y, w, h)
                return NEGRO if x == 3 and y % 3 == 0 else g_
            if y >= 10:
                return GRANATE[0]
        if cara == "arriba":
            return piel(cara, x, y, w, h) if 2 <= x <= w - 3 else c   # el escote
        return c

    def torso(cara, x, y, w, h):
        return piel(cara, x, y, w, h) if y <= 1 else luto(cara, x, y, w, h)

    def cuello(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # el abanico de encaje: mas ancho arriba, con las varillas y el borde dentado
        if y == 0 and x % 2 == 1:
            return None
        if x in (0, w - 1) and y < 3:
            return None
        c = encaje(cara, x, y, w, h)
        if x % 4 == 1:
            return oscuro(c, 0.3)                                     # las varillas
        return calado(c, x, y, 7)

    def brazo(cara, x, y, w, h):
        return luto(cara, x, y, w, h)                                 # la manga ceñida

    def antebrazo(cara, x, y, w, h):
        return piel(cara, x, y, w, h)                                 # el brazo blanco, al aire

    def manga(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        if y == h - 1 and (x * 5 + 1) % 3 == 0:
            return None
        c = luto(cara, x, y, w, h)
        if y >= h - 2:
            return calado(encaje(cara, x, y, w, h), x, y, 3)           # el puno de encaje
        return c

    def garras(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if y <= 1:
            return piel(cara, x, y, w, h)                             # los dedos
        if cara in ("frente", "der", "izq", "espalda"):
            return NEGRO if x % 2 == 0 else None                      # las unas, largas y separadas
        return None

    def vestido(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        c = luto(cara, x, y, w, h)
        if cara == "frente" and abs(x - w / 2 + 0.5) < 1.2:
            return granate(cara, x, y, w, h)                          # la tira granate de delante
        return oscuro(c, 0.04 * y)

    def vestido_bajo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # el bajo, en jirones que flotan (y algo mas oscuro abajo)
        largo = h - 1 - ((x * 7 + 3) % 3)
        if y > largo:
            return None
        c = luto(cara, x, y, w, h)
        if cara == "frente" and abs(x - w / 2 + 0.5) < 1.6:
            c = granate(cara, x, y, w, h)
        if variante == 1 and y >= h - 2 and (x + y) % 3 == 0:
            return GRANATE[3]                                          # (las brasas del bajo, en el brillo)
        return oscuro(c, 0.06 + 0.05 * y / max(1, h - 1))

    def cola(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 5 + 2) % 4)
        if y > largo:
            return None
        return oscuro(luto(cara, x, y, w, h), 0.05 + 0.02 * y)

    def pelo(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 1) % 4)
        if y > largo:
            return None
        return mezcla(NEGRO, LUTO[2], 0.2 + 0.2 * ((x * 3 + y) % 3))

    def corona(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if cara == "arriba":
            return None if 1 <= x <= w - 2 and 1 <= y <= w - 2 else HIERRO[3]
        return HIERRO[3] if y == 0 else hierro(cara, x, y, w, h)

    def velo_frente(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 7 + 2) % 3)
        if y > largo:
            return None
        c = mezcla(NEGRO, LUTO[3], 0.35 + 0.1 * ((x + y) % 2))
        # el encaje: rombos calados y una cenefa abajo; deja ver un poco la cara (pero no los ojos)
        return calado(c, x, y, 11, cada=4) if y > 2 else c

    def velo_espalda(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        largo = h - 1 - ((x * 5 + 1) % 5)
        if y > largo:
            return None
        c = mezcla(NEGRO, LUTO[3], 0.3 + 0.1 * ((x + y) % 2))
        return calado(c, x, y, 13, cada=5)

    def velo_roto(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return None
        # lo que queda del velo: jirones quemados, con el borde rojo de las ascuas (en el brillo)
        largo = (x * 7 + 3) % 4
        if y > largo:
            return None
        return "#1a0f0c" if y < largo else "#3a1608"

    return {
        "cabeza": sombra(cabeza, abajo=0.1), "torso": torso,
        "brazo_d": brazo, "brazo_i": brazo, "antebrazo_d": sombra(antebrazo, abajo=0.15), "antebrazo_i": sombra(antebrazo, abajo=0.15),
        "muslo_d": liso(LUTO[1]), "muslo_i": liso(LUTO[1]), "pierna_d": liso(LUTO[1]), "pierna_i": liso(LUTO[1]),
        "corpino": sombra(corpino, abajo=0.15), "cuello": cuello, "manga_d": manga, "manga_i": manga,
        "garras_d": garras, "garras_i": garras,
        "vestido": vestido, "vestido_bajo": vestido_bajo, "cola": cola, "pelo": pelo,
        "corona": corona, "punta": liso(HIERRO[2]), "gema": liso("#7a0b0b"),
        "velo_frente": velo_frente, "velo_espalda": velo_espalda, "velo_roto": velo_roto,
    }


def brillo(variante):
    """Lo que brilla: las gemas de la corona; sin el velo, los ojos rojos, las grietas de la cara y las ascuas del velo y del bajo."""
    b = {"gema": liso(ROJO), "punta": lambda cara, x, y, w, h: ROJO if cara == "frente" and y == 0 else None}
    if variante == 1:
        ojos = dibujo([
            ".......",
            "....r..",
            ".......",
            ".R...R.",
            "....r..",
            ".......",
            ".......",
            ".......",
        ], {"R": ROJO, "r": "#8a1410"})
        b["cabeza"] = por_cara(liso(None), frente=ojos)
        b["velo_roto"] = lambda cara, x, y, w, h: "#ff6a1a" if cara not in ("arriba", "abajo") and y == (x * 7 + 3) % 4 and x % 2 == 0 else None
        b["vestido_bajo"] = lambda cara, x, y, w, h: "#ff4a1a" if cara not in ("arriba", "abajo") and y >= h - 2 and (x + y) % 3 == 0 else None
    return b


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va la mano respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide ~11)

ARRIBA = 3.0   # lo que flota sobre el suelo (pixeles)
BASE = dict(raiz=raiz(ARRIBA), torso=(-2, 0, 0), cabeza=(2, 0, 0), vestido=(2, 0, 0), vestido_bajo=(4, 0, 0),
            brazo_d=(-4, 0, 6), antebrazo_d=(-8, 0, 0), brazo_i=(-4, 0, -6), antebrazo_i=(-8, 0, 0))
BRAZOS = ("brazo_d", "antebrazo_d", "mano_d", "brazo_i", "antebrazo_i", "mano_i")
#: las manos juntas por delante, a la altura de la cintura (asi espera)
PORTE = dict(_mano_d=("h", 3.0, -8.2, 3.8), _mano_i=("h", -3.0, -8.2, 3.8))
#: en guardia: inclinada hacia delante, los brazos abiertos con las garras por delante
LISTO = dict(raiz=raiz(ARRIBA + 0.6, -0.6), torso=(18, 0, 0), cabeza=(-14, 0, 0), vestido=(10, 0, 0), vestido_bajo=(10, 0, 0),
             cola=(36, 0, 0), _mano_d=("h", -2.4, -2.4, 7.6), _mano_i=("h", 2.4, -2.4, 7.6))
#: sentada en el trono (flota sentada): el vestido hacia delante y luego cae, las manos en los brazos del trono
SENTADA = dict(raiz=raiz(ARRIBA - 6.0, 1.0), torso=(-6, 0, 0), cabeza=(6, 0, 10), vestido=(-86, 0, 0), vestido_bajo=(88, 0, 0),
               cola=(60, 0, 0), brazo_d=(-28, 0, 10), antebrazo_d=(-56, 0, 0), brazo_i=(-28, 0, -10), antebrazo_i=(-56, 0, 0))


def animaciones(r=None):
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

    Q = con(BASE, PORTE)
    # ---- quieta: flota, sube y baja despacio, el vestido ondea y ladea la cabeza
    bucle("quieto", 5.0, [
        (0.0, Q),
        (1.25, con(Q, raiz=raiz(ARRIBA + 0.8), torso=(-1, 4, 2), cabeza=(4, -12, 8), vestido=(-2, 0, 3), vestido_bajo=(8, 0, -3))),
        (2.5, con(Q, raiz=raiz(ARRIBA + 1.2), torso=(-2, 0, 0), cabeza=(6, 0, 0), vestido=(4, 0, 0), vestido_bajo=(2, 0, 0))),
        (3.75, con(Q, raiz=raiz(ARRIBA + 0.6), torso=(-1, -4, -2), cabeza=(4, 12, -8), vestido=(-2, 0, -3), vestido_bajo=(8, 0, 3))),
        (5.0, Q),
    ])
    # ---- andar: se desliza, el vestido y la cola por detras
    DESLIZA = con(Q, raiz=raiz(ARRIBA + 1.0), torso=(8, 0, 0), cabeza=(-6, 0, 0), vestido=(18, 0, 0), vestido_bajo=(14, 0, 0), cola=(48, 0, 0))
    bucle("andar", 1.8, [
        (0.0, DESLIZA),
        (0.45, con(DESLIZA, raiz=raiz(ARRIBA + 1.4), vestido=(22, 0, 4), vestido_bajo=(20, 0, -5))),
        (0.9, con(DESLIZA, raiz=raiz(ARRIBA + 0.8), vestido=(16, 0, 0), vestido_bajo=(10, 0, 0))),
        (1.35, con(DESLIZA, raiz=raiz(ARRIBA + 1.4), vestido=(22, 0, -4), vestido_bajo=(20, 0, 5))),
        (1.8, DESLIZA),
    ])
    # ---- en guardia
    bucle("listo", 2.4, [
        (0.0, LISTO),
        (1.2, con(LISTO, raiz=raiz(ARRIBA + 1.4, -0.6), torso=(20, 0, 0), _mano_d=("h", -2.8, -2.0, 7.8), _mano_i=("h", 2.8, -2.0, 7.8))),
        (2.4, LISTO),
    ])
    # ---- sentada en el trono, tarareando (la cabeza se mece)
    bucle("sentada", 6.0, [
        (0.0, SENTADA),
        (1.5, con(SENTADA, cabeza=(8, -10, 14))),
        (3.0, con(SENTADA, cabeza=(4, 0, 4), torso=(-4, 0, 0))),
        (4.5, con(SENTADA, cabeza=(8, 10, -4))),
        (6.0, SENTADA),
    ])

    # ================================================================ acciones
    L = con(BASE, LISTO)
    # ---- levantarse del trono (despacio: mientras habla)
    accion("levantarse", 3.0, [
        (0.00, SENTADA),
        (0.80, con(SENTADA, cabeza=(-8, 0, 0), torso=(-2, 0, 0))),
        (1.60, con(BASE, raiz=raiz(ARRIBA - 2.0, 0.6), torso=(10, 0, 0), cabeza=(-6, 0, 0), vestido=(-40, 0, 0), vestido_bajo=(44, 0, 0),
                   _mano_d=("h", -1.0, -6.0, 5.0), _mano_i=("h", 1.0, -6.0, 5.0))),
        (2.40, con(Q, raiz=raiz(ARRIBA + 1.0))),
        (3.00, L),
    ])
    # ---- las garras: dos zarpazos cruzados (la derecha y la izquierda)
    DER = con(L, raiz=raiz(ARRIBA + 1.0, 0.6), torso=(6, 30, 0), cabeza=(-10, -22, 0),
              _mano_d=("h", -6.0, 5.0, 1.0), _mano_i=("h", 2.0, -3.0, 6.0))
    DER_B = con(L, raiz=raiz(ARRIBA - 0.4, -2.4), torso=(22, -18, 0), cabeza=(-14, 14, 0),
                _mano_d=("h", 6.0, -5.0, 6.0), _mano_i=("h", 3.0, -3.0, 6.0))
    IZQ = con(L, raiz=raiz(ARRIBA + 0.6, -2.4), torso=(10, -30, 0), cabeza=(-10, 22, 0),
              _mano_d=("h", 4.0, -5.0, 4.0), _mano_i=("h", 6.0, 5.0, 1.0))
    IZQ_B = con(L, raiz=raiz(ARRIBA - 0.4, -4.0), torso=(24, 18, 0), cabeza=(-14, -14, 0),
                _mano_d=("h", 3.0, -5.0, 4.0), _mano_i=("h", -6.0, -5.0, 6.0))
    accion("garras", 1.5, [
        (0.00, L),
        (0.40, DER),
        (0.55, DER_B),
        (0.70, IZQ),
        (0.85, IZQ_B),
        (1.15, con(IZQ_B, torso=(22, 16, 0))),
        (1.50, L),
    ], golpes=(0.55, 0.85))
    # ---- el velo: gira sobre si misma con los brazos abiertos (el vestido y el velo en abanico)
    ABRE = con(L, raiz=raiz(ARRIBA + 1.6, 0.0), torso=(0, 0, 0), cabeza=(-6, 0, 0), vestido=(-24, 0, 0), vestido_bajo=(-20, 0, 0),
               cola=(70, 0, 0), _mano_d=("h", -9.0, -1.0, 2.0), _mano_i=("h", 9.0, -1.0, 2.0))
    vuelta = []
    for k in range(9):
        a = 360.0 * k / 8
        vuelta.append((0.60 + 0.46 * k / 8, con(ABRE, raiz=raiz(ARRIBA + 1.6 + 0.6 * (k % 2), 0.0, rot=(0, -a, 0)),
                                                  vestido=(-30, 0, 0), vestido_bajo=(-34, 0, 0))))
    accion("velo", 1.7, [
        (0.00, L),
        (0.45, con(ABRE, raiz=raiz(ARRIBA + 1.0, 0.0, rot=(0, 30, 0)))),
        *vuelta,
        (1.30, con(ABRE, raiz=raiz(ARRIBA + 1.0, 0.0, rot=(0, -380, 0)))),
        (1.70, con(L, raiz=raiz(ARRIBA + 0.6, -0.6, rot=(0, -360, 0)))),
    ], golpes=(0.84,))
    # ---- el abrazo: abre los brazos de par en par, se lanza (el juego la mueve) y los cierra; si pilla a alguien, lo aprieta
    ABRAZA = con(L, raiz=raiz(ARRIBA + 0.4, -3.0), torso=(26, 0, 0), cabeza=(-20, 0, 0), vestido=(30, 0, 0), vestido_bajo=(26, 0, 0),
                 cola=(70, 0, 0), _mano_d=("h", -8.0, 1.0, 5.0), _mano_i=("h", 8.0, 1.0, 5.0))
    CIERRA = con(L, raiz=raiz(ARRIBA + 0.6, -3.6), torso=(20, 0, 0), cabeza=(10, 0, 0),
                 _mano_d=("h", 4.4, -1.0, 6.4), _mano_i=("h", -4.4, -1.0, 6.4))
    accion("abrazo", 1.8, [
        (0.00, L),
        (0.50, ABRAZA),
        (0.70, con(ABRAZA, raiz=raiz(ARRIBA + 0.8, -5.0))),
        (0.85, CIERRA),
        (1.30, con(CIERRA, cabeza=(14, 0, 0))),
        (1.80, L),
    ], golpes=(0.85,))
    # ---- aprieta a quien ha cogido (la cabeza baja hacia el; le chupa la vida) y lo suelta de un empujon
    accion("aprieta", 1.6, [
        (0.00, CIERRA),
        (0.40, con(CIERRA, cabeza=(30, 0, 0), torso=(26, 0, 0))),
        (0.80, con(CIERRA, cabeza=(34, 6, 0), torso=(28, 0, 0))),
        (1.10, con(CIERRA, cabeza=(30, -6, 0), torso=(26, 0, 0))),
        (1.30, con(L, raiz=raiz(ARRIBA + 1.0, 1.0), torso=(-6, 0, 0), _mano_d=("h", -2.0, 0.0, 9.0), _mano_i=("h", 2.0, 0.0, 9.0))),
        (1.60, L),
    ], golpes=(0.40, 0.80, 1.30))
    # ---- la nana: junta las manos en el pecho, ladea la cabeza y canta meciendose (quien la mire al acabar, se duerme)
    CANTA = con(BASE, raiz=raiz(ARRIBA + 2.0), torso=(-6, 0, 0), cabeza=(-14, 0, 12), vestido=(-6, 0, 0),
                _mano_d=("h", 2.6, -3.4, 4.0), _mano_i=("h", -2.6, -3.4, 4.0))
    accion("nana", 3.2, [
        (0.00, L),
        (0.50, CANTA),
        (1.10, con(CANTA, raiz=raiz(ARRIBA + 2.6), torso=(-6, 8, 4), cabeza=(-14, -10, 16))),
        (1.80, con(CANTA, raiz=raiz(ARRIBA + 2.2), torso=(-6, -8, -4), cabeza=(-14, 10, 8))),
        (2.50, con(CANTA, raiz=raiz(ARRIBA + 2.8), cabeza=(-20, 0, 12))),
        (2.80, con(CANTA, raiz=raiz(ARRIBA + 3.0), cabeza=(-24, 0, 0), _mano_d=("h", -5.0, 2.0, 4.0), _mano_i=("h", 5.0, 2.0, 4.0))),
        (3.20, L),
    ], golpes=(2.80,))
    # ---- desvanecerse: se encoge y se apaga (el juego la lleva detras de su presa a los 0,6 s) y se abre al aparecer
    ENCOGE = con(BASE, raiz=raiz(ARRIBA - 1.0), torso=(30, 0, 0), cabeza=(30, 0, 0), vestido=(-10, 0, 0),
                 _mano_d=("h", 3.0, -3.0, 4.0), _mano_i=("h", -3.0, -3.0, 4.0))
    accion("desvanece", 1.4, [
        (0.00, L),
        (0.40, ENCOGE),
        (0.60, con(ENCOGE, raiz=raiz(ARRIBA - 1.6), torso=(36, 0, 0))),
        (0.75, con(L, raiz=raiz(ARRIBA + 2.4), torso=(-10, 0, 0), cabeza=(-14, 0, 0), _mano_d=("h", -7.0, 0.0, 5.0), _mano_i=("h", 7.0, 0.0, 5.0))),
        (1.40, L),
    ])
    # ---- las lagrimas: echa la cara al techo y abre los brazos hacia abajo; llueven lagrimas negras (a los 1,4 s)
    LLORA = con(BASE, raiz=raiz(ARRIBA + 3.0), torso=(-18, 0, 0), cabeza=(-44, 0, 0), vestido=(-12, 0, 0), vestido_bajo=(-8, 0, 0),
                _mano_d=("h", -7.0, -5.0, 1.0), _mano_i=("h", 7.0, -5.0, 1.0))
    accion("lagrimas", 2.2, [
        (0.00, L),
        (0.60, LLORA),
        (1.20, con(LLORA, raiz=raiz(ARRIBA + 3.6), cabeza=(-48, 0, 0))),
        (1.40, con(LLORA, raiz=raiz(ARRIBA + 3.4), cabeza=(-40, 0, 0), _mano_d=("h", -8.0, -3.0, 1.0), _mano_i=("h", 8.0, -3.0, 1.0))),
        (2.20, L),
    ], golpes=(1.40,))
    # ---- el grito: echa los brazos atras y grita con la cabeza hacia delante (llama a su corte; y a media vida)
    GRITA = con(BASE, raiz=raiz(ARRIBA + 2.0, -1.0), torso=(14, 0, 0), cabeza=(-30, 0, 0), vestido=(-16, 0, 0), vestido_bajo=(-14, 0, 0),
                cola=(60, 0, 0), _mano_d=("h", -6.0, -2.0, -5.0), _mano_i=("h", 6.0, -2.0, -5.0))
    accion("grito", 2.0, [
        (0.00, L),
        (0.45, con(GRITA, torso=(26, 0, 0), cabeza=(10, 0, 0))),
        (0.70, GRITA),
        (1.40, con(GRITA, raiz=raiz(ARRIBA + 2.4, -1.0), cabeza=(-34, 0, 0))),
        (2.00, L),
    ], golpes=(0.70,))
    # ---- el velo arde (a media vida): se lleva las manos a la cara, se retuerce, y se las aparta (ya sin velo)
    accion("velo_arde", 2.8, [
        (0.00, L),
        (0.40, con(BASE, raiz=raiz(ARRIBA + 1.0), torso=(20, 0, 0), cabeza=(24, 0, 0), _mano_d=("h", 2.0, 5.0, 4.0), _mano_i=("h", -2.0, 5.0, 4.0))),
        (1.00, con(BASE, raiz=raiz(ARRIBA + 0.6), torso=(28, 16, 8), cabeza=(30, -10, 10), _mano_d=("h", 2.4, 5.4, 4.0), _mano_i=("h", -1.6, 5.0, 4.4))),
        (1.60, con(BASE, raiz=raiz(ARRIBA + 0.8), torso=(26, -16, -8), cabeza=(28, 10, -10), _mano_d=("h", 1.6, 5.0, 4.4), _mano_i=("h", -2.4, 5.4, 4.0))),
        (2.10, con(BASE, raiz=raiz(ARRIBA + 2.4), torso=(-10, 0, 0), cabeza=(-16, 0, 0), _mano_d=("h", -8.0, 1.0, 3.0), _mano_i=("h", 8.0, 1.0, 3.0))),
        (2.80, L),
    ])
    # ---- aturdida
    accion("aturdido", 1.4, [
        (0.00, L),
        (0.15, con(L, raiz=raiz(ARRIBA + 1.4, 2.0), torso=(-18, 10, 0), cabeza=(-22, -14, 0))),
        (0.60, con(L, raiz=raiz(ARRIBA + 1.0, 1.5), torso=(-12, -8, 0), cabeza=(-10, 16, 0))),
        (1.00, con(L, raiz=raiz(ARRIBA + 0.6, 0.6), torso=(4, 4, 0), cabeza=(0, -6, 0))),
        (1.40, L),
    ])
    # ---- morir: baja hasta quedar de rodillas en el aire, se lleva las manos a la cara, alza la vista (oye la fuente) y se
    # deshace subiendo (el juego la va apagando)
    RODILLAS = con(BASE, raiz=raiz(ARRIBA - 7.0, 1.0), torso=(10, 0, 0), cabeza=(20, 0, 0), vestido=(-70, 0, 0), vestido_bajo=(60, 0, 0),
                   cola=(80, 0, 0), _mano_d=("h", 2.0, 4.6, 4.0), _mano_i=("h", -2.0, 4.6, 4.0))
    accion("morir", 4.4, [
        (0.00, L),
        (0.60, con(L, raiz=raiz(ARRIBA + 1.0, 1.0), torso=(-16, 0, 0), cabeza=(-24, 0, 0))),
        (1.40, RODILLAS),
        (2.40, con(RODILLAS, cabeza=(-30, 0, 0), torso=(-6, 0, 0), _mano_d=("h", -3.0, 0.0, 6.0), _mano_i=("h", 3.0, 0.0, 6.0))),
        (3.40, con(RODILLAS, raiz=raiz(ARRIBA - 3.0, 1.0), cabeza=(-34, 0, 0), torso=(-10, 0, 0),
                   _mano_d=("h", -4.0, 6.0, 3.0), _mano_i=("h", 4.0, 6.0, 3.0))),
        (4.40, con(RODILLAS, raiz=raiz(ARRIBA + 6.0, 1.0), cabeza=(-40, 0, 0), torso=(-14, 0, 0),
                   _mano_d=("h", -3.0, 9.0, 1.0), _mano_i=("h", 3.0, 9.0, 1.0))),
    ])
    return B, A


def variante_de(nombre):
    return 1 if nombre in ("lagrimas", "grito", "morir", "aprieta") else 0


def reposo(v, anims):
    return anims["quieto"].muestra(0)


def base_de(nombre, anims):
    return []


TABLA_EXTRA = ()
