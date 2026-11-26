"""
El Barquero de la laguna negra (parte 7j): el jefe del Pantano Podrido, el que la hermana Ilse oia remar bajo la capilla
(libro perdido 20: "si lees esto, no subas a su barca"). Un muerto alto y descarnado con su habito empapado hasta los
pies (roto por abajo, con algas), el manto sobre los hombros y la capa a la espalda, la capucha honda con la cara de
calavera en sombra y los ojos de fuego de almas; un cordel con monedas viejas al cinto (el peaje). En la derecha el remo
largo con un garfio de hierro atado a la pala (el bichero); en la izquierda, colgando de su cadena, la linterna de almas.

Una sola variante. Acciones: remazo (alza el remo y lo estrella delante: ola de agua negra), barrido (a media altura),
bichero (lo clava lejos con el garfio y tira de quien engancha), golpe de linterna (de cerca: quema), linterna (la alza
y el fuego de almas se abre en un anillo por el agua), grito (al empezar y a media vida: llama a sus pasajeros), salto
(de su barca al embarcadero), aturdido, morir, levantarse (esperaba de pie en la barca, con la linterna en alto).
"""
from anim import Anim, con, raiz, sin
from modelo import Box, Rig, g, humanoide
from pinta import (claro, dibujo, liso, madera, metal, mezcla, oscuro, por_cara, ruido, sombra, tela)  # noqa: F401

NOMBRE = "barquero"
VARIANTES = ("barquero",)
ESCALA = 1.55
CENTRO_Y = 1.9

# ---------------------------------------------------------------- el modelo


def rig():
    r = Rig(NOMBRE, 128)
    # largo y flaco: brazos y piernas largos, el torso estrecho
    humanoide(r, torso=(9, 14, 5), cabeza=(7, 8, 7), brazo=(3, 17, 3), pierna=(4, 15, 4), hombro=0.6)
    # la capucha honda (abierta por delante: la cara queda en sombra), con su pico atras y el borde sobre la frente
    r.pieza("capucha", "cabeza", (0, -8, 0), boxes=[
        Box(-4.3, -1.2, -4.6, 8.6, 1.2, 9.2, "capucha_arriba"),
        Box(-4.3, -1.2, -4.6, 0.9, 9.6, 9.2, "capucha_lado"),
        Box(3.4, -1.2, -4.6, 0.9, 9.6, 9.2, "capucha_lado"),
        Box(-4.3, -1.2, 3.7, 8.6, 10.6, 0.9, "capucha_nuca"),
        Box(-4.3, -1.2, -5.2, 8.6, 1.9, 0.7, "capucha_borde"),
        Box(-1.4, -2.6, 1.6, 2.8, 1.5, 3.6, "capucha_pico"),
    ])
    # el manto sobre los hombros y la capa a la espalda (hasta un palmo del suelo, rota por abajo)
    r.pieza("manto", "torso", (0, -14, 0), boxes=[Box(-5.8, -0.6, -3.3, 11.6, 5.2, 6.6, "manto", grow=g(0.3))])
    r.pieza("capa", "torso", (0, -13.6, 3.0), rot=(4, 0, 0), boxes=[Box(-5.4, 0, 0, 10.8, 26, 0.8, "capa")])
    # el habito por las piernas (cada pierna su faldon, mas ancho que ella: no se ve la raja) y el borde roto con algas
    for n in ("d", "i"):
        r.pieza(f"habito_{n}", f"muslo_{n}", (0, 0, 0), boxes=[Box(-2.75, -0.6, -2.75, 5.5, 8.4, 5.5, f"habito_{n}")])
        r.pieza(f"habito_bajo_{n}", f"pierna_{n}", (0, 0, 0), boxes=[Box(-2.65, 0, -2.65, 5.3, 7.6, 5.3, f"habito_bajo_{n}")])
    # las mangas anchas
    for n in ("d", "i"):
        r.pieza(f"manga_{n}", f"antebrazo_{n}", (0, 1.5, 0), boxes=[Box(-2.2, 0, -2.2, 4.4, 5.0, 4.4, f"manga_{n}")])
    # el cordel de la cintura con la bolsa y la sarta de monedas
    r.pieza("cordel", "cadera", (0, 0, 0), boxes=[
        Box(-4.9, -1.5, -2.9, 9.8, 1.3, 5.8, "cordel", grow=g(0.2)),
        Box(3.6, -0.6, -2.6, 2.2, 3.0, 2.4, "bolsa"),
        Box(-5.3, -0.5, -1.2, 0.6, 5.5, 0.6, "sarta"),
        Box(-5.6, 4.6, -1.6, 1.2, 1.2, 1.4, "moneda"),
    ])
    # ---- el remo (apunta a -Z con el brazo caido): la pertiga, la pala ancha, la anilla, el regaton y el garfio de hierro
    r.pieza("remo", "mano_d", (0, 0, 0), boxes=[
        Box(-0.8, -0.8, -26, 1.6, 1.6, 30, "pertiga"),
        Box(-1.0, -1.0, 3.6, 2.0, 2.0, 1.4, "regaton"),
        Box(-1.15, -1.15, -27.2, 2.3, 2.3, 1.6, "anilla"),
        Box(-3.2, -0.45, -37.5, 6.4, 0.9, 11.5, "pala"),
        Box(-0.55, -0.55, -41.5, 1.1, 1.1, 4.4, "punta"),
        Box(-0.55, -3.6, -38.6, 1.1, 3.2, 1.1, "gancho"),
        Box(-0.55, -3.6, -38.0, 1.1, 1.1, 2.6, "gancho"),
    ])
    # ---- la linterna: la cadena en el puno y el farol colgando (pieza aparte: las poses la ponen derecha)
    r.pieza("cadena", "mano_i", (0, 0, 0), boxes=[
        Box(-0.7, -0.4, -0.7, 1.4, 1.6, 1.4, "argolla"),
        Box(-0.4, 1.0, -0.4, 0.8, 3.2, 0.8, "eslabones"),
    ])
    r.pieza("farol", "cadena", (0, 4.0, 0), boxes=[
        Box(-1.2, -0.8, -1.2, 2.4, 0.9, 2.4, "farol_asa"),
        Box(-2.3, 0, -2.3, 4.6, 1.2, 4.6, "farol_tapa"),
        Box(-1.8, 1.2, -1.8, 3.6, 4.6, 3.6, "farol_vidrio"),
        Box(-2.1, 1.2, -2.1, 4.2, 4.6, 4.2, "farol_reja"),
        Box(-2.3, 5.8, -2.3, 4.6, 1.2, 4.6, "farol_tapa"),
    ])
    r.empaquetar()
    return r


#: lo que se esconde en cada variante
OCULTAS = {0: []}

#: lo que va ademas en BarqueroAnims (la escala, para el dibujante y el servidor)
TABLA_EXTRA = (
    "",
    f"    public static final float ESCALA = {ESCALA}f;",
)

# ---------------------------------------------------------------- las texturas

HABITO = ["#0b0e0d", "#121715", "#19201d", "#212a26", "#2b3631", "#36433d"]
MANTO = ["#0b0d0d", "#111514", "#171c1b", "#1e2423", "#262d2c", "#2f3836"]
HUESO = ["#262c2f", "#363e42", "#4a5459", "#616c71", "#7c878b", "#9aa4a7"]
HUECO = "#040607"
ALGA = ["#16260f", "#203817", "#2d4c1f", "#3c6127", "#4e7830"]
BARRO = ["#15110c", "#1f1912", "#2a2218"]
MADERA = ["#16100b", "#20170f", "#2b2015", "#372a1c", "#433425"]
HIERRO = ["#151518", "#24242a", "#36363e", "#4b4b55", "#63636e", "#80808b"]
OXIDO = ["#3b2216", "#5a321c", "#7a4422"]
ORO = ["#4a3a12", "#73591c", "#a07d28", "#cba43a", "#ead26a"]
CUERDA = ["#2c2318", "#3d3121", "#51422d", "#655338"]
ALMA = ["#0a3c49", "#14677a", "#2aa3b8", "#62d8ea", "#b4f4ff", "#ecfdff"]
VIDRIO = ["#0c2328", "#12343b", "#1a4851"]


def materiales(variante):
    habito_t = tela(HABITO, 911, pliegue=3, grano=0.16, luz=0.18)
    manto_t = tela(MANTO, 912, pliegue=5, grano=0.2, luz=0.14)
    hueso = ruido(HUESO[1:5], 913, grano=0.4, base=0.5)
    cuerda_t = ruido(CUERDA, 914, grano=0.5, base=0.5)

    def humedo(c, x, y, h, desde=0.45):
        """Mas oscuro y verdoso hacia abajo (empapado), con algas y barro en el borde."""
        t = y / max(1, h - 1)
        if t > desde:
            c = mezcla(c, BARRO[1], (t - desde) * 0.8)
        return c

    def roto(cara, x, y, w, h, filas=2):
        """El borde de abajo deshilachado: algun pixel de las ultimas filas, transparente."""
        if y >= h - filas:
            k = (x * 7 + len(cara) * 3 + y * 5) % 5
            if y == h - 1 and k < 3 or y == h - 2 and k == 0:
                return True
        return False

    def cabeza(cara, x, y, w, h):
        c = hueso(cara, x, y, w, h)
        if cara == "frente":
            # la calavera en sombra: las cuencas hondas (el brillo enciende los ojos), la nariz y los dientes
            C = {"K": HUECO, "s": HUESO[1], "o": HUESO[2], "c": HUESO[3], "b": "#0a0c0d", "d": HUESO[4]}
            v = dibujo([
                "ocdddco",
                "ocdcdco",
                "oKKcKKo",
                "sKKcKKs",
                "oscKcso",
                "osbbbso",
                "osdbdso",
                "oosssoo",
            ], C)(cara, x, y, w, h)
            v = v if v is not None else c
            # bajo la capucha: oscura, algo mas clara abajo (la mandibula)
            return mezcla(v, HUECO, 0.62 - 0.05 * max(0, y - 4))
        return oscuro(c, 0.45)

    def capucha(cara, x, y, w, h):
        c = manto_t(cara, x, y, w, h)
        if cara == "abajo":
            return None
        if (x * 5 + y * 3) % 23 == 0:
            return MANTO[4]
        return c

    def capucha_borde(cara, x, y, w, h):
        return MANTO[1] if (x + y) % 3 else MANTO[2]

    def capucha_pico(cara, x, y, w, h):
        return oscuro(manto_t(cara, x, y, w, h), 0.1)

    def manto(cara, x, y, w, h):
        if cara == "abajo":
            return None
        c = manto_t(cara, x, y, w, h)
        if y == h - 1 and (x * 3) % 4 == 1:
            return None                                        # los picos del borde
        if y == h - 2 and x % 5 == 0:
            return MANTO[0]
        return c

    def capa(cara, x, y, w, h):
        if cara not in ("frente", "espalda"):
            return MANTO[1]
        if roto(cara, x, y, w, h, 3):
            return None
        c = humedo(manto_t(cara, x, y, w, h), x, y, h, 0.55)
        if y >= h - 3 and (x * 3 + y) % 4 == 0:
            return ALGA[1 + (x % 3)]
        if (x * 11 + y * 7) % 29 == 0:
            return None                                        # algun agujero
        return c

    def torso(cara, x, y, w, h):
        c = habito_t(cara, x, y, w, h)
        if cara == "frente" and x == w // 2 and y > 4:
            return HABITO[0]                                   # la costura del habito
        if cara == "frente" and y <= 2 and 3 <= x <= w - 4:
            return oscuro(hueso(cara, x, y, w, h), 0.35)       # el cuello, descarnado
        return c

    def habito(cara, x, y, w, h):
        return habito_t(cara, x, y, w, h)

    def habito_bajo(cara, x, y, w, h):
        if cara == "abajo":
            return HABITO[0]
        if roto(cara, x, y, w, h, 2):
            return None
        c = humedo(habito_t(cara, x, y + 8, w, h), x, y, h, 0.25)
        if y >= h - 3 and (x * 5 + y + len(cara)) % 3 == 0:
            return ALGA[(x + y) % 5]                           # algas pegadas al borde
        return c

    def manga(cara, x, y, w, h):
        if cara == "abajo":
            return None
        if roto(cara, x, y, w, h, 2):
            return None
        return humedo(habito_t(cara, x, y, w, h), x, y, h, 0.6)

    def brazo(cara, x, y, w, h):
        return habito_t(cara, x, y, w, h)

    def antebrazo(cara, x, y, w, h):
        # la mano descarnada al final
        if y >= h - 3:
            c = hueso(cara, x, y, w, h)
            return HUESO[1] if (x + y) % 3 == 0 else c
        return habito_t(cara, x, y, w, h)

    def muslo(cara, x, y, w, h):
        return habito_t(cara, x, y, w, h)

    def pierna(cara, x, y, w, h):
        return oscuro(hueso(cara, x, y, w, h), 0.3)

    def cordel(cara, x, y, w, h):
        return cuerda_t(cara, x, y, w, h)

    def bolsa(cara, x, y, w, h):
        c = ruido(MANTO[2:6], 915, grano=0.5)(cara, x, y, w, h)
        return CUERDA[3] if y == 0 else c

    def sarta(cara, x, y, w, h):
        return ORO[2 + (y % 2)] if y % 2 == 1 else CUERDA[2]

    def moneda(cara, x, y, w, h):
        return ORO[3] if (x + y) % 2 else ORO[2]

    # la pala del remo: madera vieja con una banda de hierro y el borde comido
    pala_m = madera(MADERA, 921, "z")

    def pala(cara, x, y, w, h):
        c = pala_m(cara, x, y, w, h)
        if cara in ("arriba", "abajo"):
            if y in (0, 1):
                return HIERRO[2]                               # la banda de hierro junto a la anilla
            if (x * 3 + y) % 13 == 0:
                return MADERA[0]
            if (x == 0 or x == w - 1) and y % 3 == 1:
                return None                                    # el borde comido
        return c

    mats = {
        "cabeza": cabeza,
        "capucha_arriba": sombra(capucha, abajo=0.1),
        "capucha_lado": sombra(capucha, abajo=0.15),
        "capucha_nuca": sombra(capucha, abajo=0.2),
        "capucha_borde": capucha_borde,
        "capucha_pico": capucha_pico,
        "manto": sombra(manto, abajo=0.1),
        "capa": capa,
        "torso": sombra(torso),
        "habito_d": sombra(habito, abajo=0.12), "habito_i": sombra(habito, abajo=0.12),
        "habito_bajo_d": sombra(habito_bajo, abajo=0.05), "habito_bajo_i": sombra(habito_bajo, abajo=0.05),
        "manga_d": sombra(manga, abajo=0.1), "manga_i": sombra(manga, abajo=0.1),
        "brazo_d": brazo, "brazo_i": brazo,
        "antebrazo_d": antebrazo, "antebrazo_i": antebrazo,
        "muslo_d": muslo, "muslo_i": muslo,
        "pierna_d": pierna, "pierna_i": pierna,
        "cordel": cordel,
        "bolsa": bolsa,
        "sarta": sarta,
        "moneda": moneda,
        "pertiga": madera(MADERA, 922, "z"),
        "regaton": metal(HIERRO[1:4], 923),
        "anilla": metal(OXIDO + [HIERRO[3]], 924),
        "pala": pala,
        "punta": metal(HIERRO[2:6], 925),
        "gancho": metal(HIERRO[1:5], 926),
        "argolla": metal(HIERRO[2:5], 927),
        "eslabones": lambda cara, x, y, w, h: HIERRO[3] if y % 2 else HIERRO[1],
        "farol_asa": metal(HIERRO[1:4], 928),
        "farol_tapa": metal(HIERRO[1:5], 929),
        "farol_vidrio": lambda cara, x, y, w, h: VIDRIO[(x + y) % 3],
        # la reja del farol: los barrotes de las esquinas y uno en medio; lo demas, hueco (se ve el vidrio)
        "farol_reja": lambda cara, x, y, w, h: (HIERRO[2] if x in (0, w - 1) or x == w // 2 or y in (0, h - 1) else None)
        if cara not in ("arriba", "abajo") else None,
    }
    return mats


def brillo(variante):
    """Lo que brilla: los ojos en las cuencas y el fuego de almas de la linterna."""
    C = {"a": ALMA[2], "b": ALMA[3], "c": ALMA[4], "d": ALMA[5]}
    ojos = dibujo([
        ".......",
        ".......",
        ".bc.cb.",
        ".ab.ba.",
    ], C)

    def vidrio(cara, x, y, w, h):
        if cara in ("arriba", "abajo"):
            return ALMA[2]
        # la llama: blanca abajo en el centro, azul por los bordes
        dx = abs(x - (w - 1) / 2.0)
        if y >= h - 3 and dx < 1.0:
            return ALMA[5]
        if y >= 1 and dx < 1.5:
            return ALMA[4]
        return ALMA[3] if y >= 1 else ALMA[2]

    return {"cabeza": por_cara(liso(None), frente=ojos), "farol_vidrio": vidrio}


# ---------------------------------------------------------------- las animaciones
# ("h", x, y, z) = donde va el puno respecto a su hombro (x hacia su izquierda, y arriba, z delante; el brazo mide 17)

# de pie: alto y algo encorvado, la cabeza baja, la linterna colgando a su izquierda
BASE = dict(raiz=raiz(-0.2), torso=(10, 0, 0), cabeza=(8, 0, 0), muslo_d=(-3, 0, 4), pierna_d=(4, 0, 0), muslo_i=(2, 0, -4),
            pierna_i=(3, 0, 0), brazo_i=(-14, 0, -8), antebrazo_i=(-12, 0, 0), farol=(22, 0, 8), capa=(-4, 0, 0))
# el remo derecho como un baston, la pala arriba
PORTE = dict(_mano_d=("h", -1.5, -7.0, 4.0), _arma=(0.0, 1.0, 0.1))
GUARDIA = dict(raiz=raiz(-1.2), torso=(16, 14, 0), cabeza=(-8, -12, 0), capa=(-8, 0, 0),
               muslo_d=(10, 0, 8), pierna_d=(16, 0, 0), muslo_i=(-20, 0, -8), pierna_i=(24, 0, 0),
               brazo_i=(-36, 0, -22), antebrazo_i=(-30, 0, 0), farol=(66, 0, 22))
# en guardia: el remo delante, la pala hacia el agua; la linterna algo alzada
LISTO = con(GUARDIA, _mano_d=("h", 1.5, -8.0, 8.0), _arma=(0.15, -0.3, 1.0))
# esperando en su barca: de pie, el remo en la derecha y la linterna en alto delante, mirando el agua
VIGIA = con(BASE, PORTE, brazo_i=(-76, 0, -8), antebrazo_i=(-14, 0, 0), farol=(90, 0, 8), cabeza=(2, 4, 0), torso=(6, 0, 0))
BRAZO_D = ("brazo_d", "antebrazo_d", "mano_d")


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

    # ---- quieto: respira despacio, la linterna se mece un poco
    bucle("quieto", 5.0, [
        (0.0, BASE),
        (2.5, con(BASE, torso=(7, 0, 0), cabeza=(4, 4, 0), raiz=raiz(-0.05), brazo_i=(-12, 0, -9), farol=(18, 0, 10), capa=(-5, 0, 0))),
        (5.0, BASE),
    ])
    # ---- andar: zancadas largas y lentas; la linterna y la capa se balancean
    pasos = []
    for k, (md, mi, pd, pi_, ty, up, fa) in enumerate((
            (-26, 20, 8, 22, 6, 0.0, 30), (-6, 4, 6, 40, 0, 0.9, 14), (20, -26, 22, 8, -6, 0.0, 2), (4, -6, 40, 6, 0, 0.9, 14),
            (-26, 20, 8, 22, 6, 0.0, 30))):
        pasos.append((1.8 * k / 4, {"muslo_d": (md, 0, 0), "muslo_i": (mi, 0, 0), "pierna_d": (pd, 0, 0), "pierna_i": (pi_, 0, 0),
                                    "torso": (10, ty, -ty * 0.3), "cabeza": (6, -ty * 0.6, 0), "raiz": raiz(up),
                                    "capa": (-2 + abs(md) * 0.25, 0, 0), "brazo_i": (-14 - mi * 0.3, 0, -8), "antebrazo_i": (-12, 0, 0),
                                    "farol": (fa, 0, 8)}))
    bucle("andar", 1.8, pasos)
    q = con(BASE, PORTE)
    bucle("porte_barquero", 3.0, [(0.0, q), (3.0, q)], solo=BRAZO_D)
    bucle("listo_barquero", 2.2, [
        (0.0, LISTO),
        (1.1, con(LISTO, raiz=raiz(-1.6), torso=(LISTO["torso"][0] + 4, LISTO["torso"][1] - 3, 0), cabeza=(-10, -10, 2),
                  farol=(60, 0, 26))),
        (2.2, LISTO),
    ])
    # ---- esperando en la barca: la linterna en alto; la barca se mece (el cuerpo de lado a lado)
    bucle("vigia", 4.4, [
        (0.0, VIGIA),
        (1.1, con(VIGIA, raiz=raiz(-0.2, rot=(0, 0, 2.5)), farol=(88, 0, 4))),
        (2.2, con(VIGIA, torso=(4, 0, 0), cabeza=(0, 8, 0), raiz=raiz(-0.3))),
        (3.3, con(VIGIA, raiz=raiz(-0.2, rot=(0, 0, -2.5)), farol=(92, 0, 12))),
        (4.4, VIGIA),
    ])

    # ================================================================ acciones
    G = LISTO

    # ---- 1 remazo: alza el remo con la derecha por encima de la cabeza, lo sostiene y lo estrella delante (la pala de plano)
    R_ALTO = con(G, raiz=raiz(0.0, 1.6), torso=(-16, 8, 0), cabeza=(-20, -6, 0), muslo_d=(12, 0, 6), pierna_d=(10, 0, 0),
                 muslo_i=(-14, 0, -6), pierna_i=(10, 0, 0), capa=(22, 0, 0),
                 _mano_d=("h", -1.0, 14.2, -0.5), _arma=(0.0, 0.45, -0.89),
                 brazo_i=(-30, 0, -56), antebrazo_i=(-20, 0, 0), farol=(44, 0, 56))
    R_ABAJO = con(G, raiz=raiz(-4.0, -3.2), torso=(48, 0, 0), cabeza=(-28, 0, 0), capa=(-28, 0, 0),
                  muslo_d=(16, 0, 6), pierna_d=(30, 0, 0), muslo_i=(-54, 0, -6), pierna_i=(56, 0, 0),
                  _mano_d=("h", 1.0, -4.0, 14.0), _arma=(0.0, -0.62, 0.78),
                  brazo_i=(8, 0, -42), antebrazo_i=(-10, 0, 0), farol=(-6, 0, 42))
    accion("remazo", 2.2, [
        (0.00, G),
        (0.70, R_ALTO),
        (0.98, con(R_ALTO, raiz=raiz(0.2, 1.9), torso=(-20, 8, 0), cabeza=(-24, -6, 0))),
        (1.05, con(G, raiz=raiz(-1.6, -1.0), torso=(18, 0, 0), cabeza=(-14, 0, 0), _mano_d=("h", 0.0, 8.0, 12.0), _arma=(0.0, 0.3, 1))),
        (1.12, R_ABAJO),
        (1.65, con(R_ABAJO, torso=(46, 0, 0))),
        (2.20, G),
    ], golpes=(1.11,))
    # ---- 2 barrido: echa el remo atras a su derecha y barre a media altura de derecha a izquierda
    B_ATRAS = con(G, raiz=raiz(-1.0, 1.4), torso=(8, 62, 0), cabeza=(-6, -50, 0), muslo_d=(14, 0, 6), pierna_d=(20, 0, 0),
                  brazo_i=(-64, 0, -26), antebrazo_i=(-26, 0, 0), farol=(90, 0, 26),
                  _mano_d=("h", -7.0, -2.0, -4.0), _arma=(-1, 0.1, -0.3))
    B_DELANTE = con(G, raiz=raiz(-2.0, -1.6), torso=(18, -6, 0), cabeza=(-14, 4, 0), muslo_i=(-30, 0, -6), pierna_i=(30, 0, 0),
                    brazo_i=(-40, 0, -50), antebrazo_i=(-20, 0, 0), farol=(60, 0, 50),
                    _mano_d=("h", 2.0, -1.0, 14.5), _arma=(0.1, 0.0, 1))
    B_FIN = con(G, raiz=raiz(-1.8, -2.2), torso=(16, -62, 0), cabeza=(-10, 50, 0), muslo_i=(-34, 0, -6), pierna_i=(34, 0, 0),
                brazo_i=(-20, 0, -62), antebrazo_i=(-20, 0, 0), farol=(40, 0, 62),
                _mano_d=("h", 10.0, -1.0, 7.0), _arma=(1, 0.0, 0.2))
    accion("barrido", 1.9, [
        (0.00, G),
        (0.62, B_ATRAS),
        (0.88, con(B_ATRAS, torso=(8, 68, 0), cabeza=(-6, -56, 0))),
        (0.97, B_DELANTE),
        (1.06, B_FIN),
        (1.40, con(B_FIN, torso=(14, -66, 0))),
        (1.90, G),
    ], golpes=(0.99,))
    # ---- 3 bichero: recoge el remo con la pala delante, lo clava lejos con el garfio (el brazo entero) y tira hacia si
    BI_ATRAS = con(G, raiz=raiz(-1.2, 1.6), torso=(6, 30, 0), cabeza=(-8, -22, 0), muslo_d=(12, 0, 6), pierna_d=(18, 0, 0),
                   _mano_d=("h", -3.0, 1.0, -5.0), _arma=(0.12, 0.08, 1),
                   brazo_i=(-56, 0, -30), antebrazo_i=(-30, 0, 0), farol=(86, 0, 30))
    BI_LANZA = con(G, raiz=raiz(-2.4, -2.8), torso=(30, -14, 0), cabeza=(-24, 10, 0), capa=(-18, 0, 0),
                   muslo_d=(18, 0, 6), pierna_d=(26, 0, 0), muslo_i=(-46, 0, -6), pierna_i=(50, 0, 0),
                   _mano_d=("h", 0.5, 0.5, 14.3), _arma=(0.04, 0.0, 1),
                   brazo_i=(-20, 0, -40), antebrazo_i=(-16, 0, 0), farol=(36, 0, 40))
    BI_TIRA = con(G, raiz=raiz(-1.0, 2.4), torso=(-12, 26, 0), cabeza=(-6, -14, 0), capa=(18, 0, 0),
                  muslo_d=(20, 0, 6), pierna_d=(24, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(10, 0, 0),
                  _mano_d=("h", -4.5, 2.5, -3.5), _arma=(0.1, 0.12, 1),
                  brazo_i=(-50, 0, -36), antebrazo_i=(-40, 0, 0), farol=(90, 0, 36))
    accion("bichero", 2.1, [
        (0.00, G),
        (0.62, BI_ATRAS),
        (0.86, con(BI_ATRAS, torso=(4, 34, 0))),
        (0.93, BI_LANZA),
        (1.20, BI_LANZA),
        (1.50, BI_TIRA),
        (1.75, con(BI_TIRA, torso=(-14, 28, 0))),
        (2.10, G),
    ], golpes=(0.92,))
    # ---- 4 golpe de linterna: de cerca, la linterna en un arco de su izquierda a su derecha (quema)
    L_ATRAS = con(G, raiz=raiz(-1.0, 0.6), torso=(10, -44, 0), cabeza=(-10, 34, 0), muslo_i=(14, 0, -6), pierna_i=(20, 0, 0),
                  brazo_i=(-30, 0, -84), antebrazo_i=(-36, 0, 0), farol=(60, 0, 80))
    L_GOLPE = con(G, raiz=raiz(-2.0, -1.6), torso=(22, 26, 0), cabeza=(-16, -18, 0), muslo_d=(-30, 0, 6), pierna_d=(34, 0, 0),
                  brazo_i=(-92, 0, 6), antebrazo_i=(-4, 0, 0), farol=(30, 0, 0))
    L_FIN = con(G, raiz=raiz(-1.8, -1.4), torso=(18, 46, 0), cabeza=(-12, -30, 0), muslo_d=(-34, 0, 6), pierna_d=(38, 0, 0),
                brazo_i=(-62, 0, 42), antebrazo_i=(-10, 0, 0), farol=(40, 0, -60))
    accion("golpe_linterna", 1.5, [
        (0.00, G),
        (0.45, L_ATRAS),
        (0.62, con(L_ATRAS, torso=(10, -50, 0))),
        (0.70, L_GOLPE),
        (0.80, L_FIN),
        (1.10, con(L_FIN, torso=(16, 42, 0))),
        (1.50, G),
    ], golpes=(0.71,))
    # ---- 5 linterna: el remo de pie a su lado, alza la linterna por encima de la cabeza y el fuego estalla en un anillo
    LI_ALZA = con(BASE, PORTE, raiz=raiz(0.2), torso=(-10, 0, 0), cabeza=(-30, 0, 0), capa=(16, 0, 0),
                  brazo_i=(-168, 0, -10), antebrazo_i=(-6, 0, 0), farol=(174, 0, 10))
    LI_ESTALLA = con(LI_ALZA, raiz=raiz(0.6), torso=(-16, 0, 0), cabeza=(-38, 0, 0), brazo_i=(-174, 0, -6), farol=(178, 0, 6))
    accion("linterna", 2.4, [
        (0.00, G),
        (0.30, con(BASE, PORTE, torso=(18, 0, 0), brazo_i=(-40, 0, -16), antebrazo_i=(-50, 0, 0), farol=(90, 0, 16))),
        (0.70, LI_ALZA),
        (1.00, con(LI_ALZA, raiz=raiz(0.4), torso=(-12, 0, 0))),
        (1.05, LI_ESTALLA),
        (1.20, con(LI_ESTALLA, torso=(-14, 0, 3))),
        (1.40, con(LI_ESTALLA, torso=(-14, 0, -3))),
        (1.70, LI_ESTALLA),
        (2.40, G),
    ], golpes=(1.05,))
    # ---- 6 grito: alza el remo y abre la linterna, la cabeza atras (llama a sus pasajeros)
    GR = con(BASE, raiz=raiz(-0.4, 1.0), torso=(-16, 0, 0), cabeza=(-40, 0, 0), capa=(22, 0, 0),
             brazo_i=(-36, 0, -72), antebrazo_i=(-20, 0, 0), farol=(56, 0, 72),
             _mano_d=("h", -2.0, 13.8, 1.0), _arma=(-0.2, 1, 0.2))
    accion("grito", 2.4, [
        (0.00, G),
        (0.40, con(GR, torso=(28, 0, 0), cabeza=(10, 0, 0), raiz=raiz(-2.2))),
        (0.85, GR),
        (1.30, con(GR, torso=(-20, 0, 4), cabeza=(-44, 0, 6))),
        (1.80, con(GR, torso=(-14, 0, -4), cabeza=(-38, 0, -6))),
        (2.40, G),
    ])
    # ---- 7 salto: se agacha, salta (de la barca al embarcadero; lo mueve el servidor) y cae con el remo por delante
    S_AGACHA = con(G, raiz=raiz(-4.4), torso=(40, 0, 0), cabeza=(-24, 0, 0), capa=(-26, 0, 0),
                   muslo_d=(-70, 0, 8), pierna_d=(100, 0, 0), muslo_i=(-64, 0, -8), pierna_i=(96, 0, 0))
    S_VUELA = con(G, raiz=raiz(1.0), torso=(-6, 0, 0), cabeza=(-20, 0, 0), capa=(36, 0, 0),
                  muslo_d=(-20, 0, 8), pierna_d=(40, 0, 0), muslo_i=(10, 0, -8), pierna_i=(30, 0, 0),
                  _mano_d=("h", -1.0, 14.2, -0.5), _arma=(0.0, 0.5, -0.86), brazo_i=(-30, 0, -60), antebrazo_i=(-20, 0, 0),
                  farol=(46, 0, 60))
    accion("salto", 1.7, [
        (0.00, G),
        (0.40, S_AGACHA),
        (0.58, S_VUELA),
        (0.98, S_VUELA),
        (1.08, R_ABAJO),
        (1.40, con(R_ABAJO, torso=(46, 0, 0))),
        (1.70, G),
    ], golpes=(1.07,))
    # ---- 8 aturdido
    AT = con(G, raiz=raiz(-1.6, 2.2), torso=(-10, 0, 8), cabeza=(-22, 0, 14), capa=(14, 0, 0), muslo_d=(-8, 0, 8), pierna_d=(24, 0, 0),
             muslo_i=(12, 0, -8), pierna_i=(18, 0, 0), _mano_d=("h", -3.0, -10.0, 3.0), _arma=(-0.2, -0.4, 1),
             brazo_i=(18, 0, -30), antebrazo_i=(-16, 0, 0), farol=(-2, 0, 30))
    accion("aturdido", 1.5, [
        (0.00, con(AT, torso=(10, 0, 0), cabeza=(0, 0, 0), raiz=raiz(-1.0, 0.0))),
        (0.20, AT),
        (0.65, con(AT, torso=(-4, 6, -10), cabeza=(-16, -8, -16), raiz=raiz(-2.0, 2.8))),
        (1.05, con(AT, torso=(-8, -6, 8), cabeza=(-20, 8, 12), raiz=raiz(-1.8, 2.4))),
        (1.50, con(AT, torso=(12, 0, 0), cabeza=(4, 0, 0), raiz=raiz(-1.2, 0.6))),
    ])
    # ---- 9 morir: cae de rodillas apoyado en el remo, la linterna al suelo, y se desploma de bruces (al agua)
    RODILLAS = dict(raiz=raiz(-9.6, 0.8), muslo_d=(-10, 0, 6), pierna_d=(100, 0, 0), muslo_i=(-6, 0, -6), pierna_i=(98, 0, 0),
                    torso=(12, 0, 4), cabeza=(30, 0, 8), capa=(30, 0, 0),
                    _mano_d=("h", 3.0, -6.0, 9.0), _arma=(0.05, -1, 0.2), brazo_i=(4, 0, -26), antebrazo_i=(-10, 0, 0), farol=(6, 0, 26))
    CAIDA = dict(raiz=raiz(-6.0, 14.0, rot=(78, 0, 6)), torso=(6, 0, 0), cabeza=(-10, 16, 0), muslo_d=(-30, 0, 6), pierna_d=(70, 0, 0),
                 muslo_i=(-26, 0, -6), pierna_i=(66, 0, 0), capa=(-6, 0, 0),
                 _mano_d=("h", 4.0, 6.0, 10.0), _arma=(0.6, 0.2, 0.8), brazo_i=(-150, 0, -24), antebrazo_i=(-10, 0, 0), farol=(150, 0, 24))
    accion("morir", 3.2, [
        (0.00, con(BASE, torso=(-8, 0, 6), cabeza=(-30, 0, 10), capa=(10, 0, 0), _mano_d=("h", 4.0, -9.0, 5.0), _arma=(0.2, -1, 0.2))),
        (0.70, RODILLAS),
        (1.70, con(RODILLAS, raiz=raiz(-9.8, 0.4), torso=(20, 0, 2), cabeza=(38, 0, 6))),
        (2.50, CAIDA),
        (3.20, CAIDA),
    ])
    # ---- 10 levantarse: estaba de vigia en la barca; baja la linterna y pone el remo en guardia
    accion("levantarse", 1.4, [
        (0.00, VIGIA),
        (0.55, con(VIGIA, torso=(4, 10, 0), cabeza=(-6, 8, 0), brazo_i=(-50, 0, -16), antebrazo_i=(-30, 0, 0), farol=(80, 0, 16),
                   _mano_d=("h", 0.0, -3.0, 7.0), _arma=(0.1, 0.4, 0.9))),
        (1.40, G),
    ])
    return B, A


# ---------------------------------------------------------------- para las hojas de revision (generar.py --vistas)

def variante_de(nombre):
    return 0


def reposo(v, anims):
    import anim
    return anim.sumar(anims["quieto"].muestra(0), anims["porte_barquero"].muestra(0))


def base_de(nombre, anims):
    if nombre.startswith("porte_"):
        return [anims["quieto"]]
    if nombre == "andar":
        return [anims["quieto"], anims["porte_barquero"]]
    if nombre == "quieto":
        return [anims["porte_barquero"]]
    return []
