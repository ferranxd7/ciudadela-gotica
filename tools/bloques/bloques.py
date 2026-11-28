"""Los bloques nuevos de Dark Craft (la gran reforma, parte 8b y siguientes): el catalogo, sus texturas de 64 x 64 y todo
lo que necesita el juego (estados, modelos, modelos de objeto, etiquetas, nombres y la lista de Java).

Uso:
    python3 tools/bloques/bloques.py            # escribe texturas, JSON, CatalogoBloques.java y ci/historia/bloques.py
    python3 tools/bloques/bloques.py --vista /tmp/vb   # ademas, hojas para revisar (cada textura repetida y un cubo)

Cada material tiene sus formas: B bloque, E escalera, L losa, M muro, C columna (con lado y tapa). Los ids:
<id> (o <id> la columna), <id>_escalera, <id>_losa, <id>_muro.
"""
import json
import os
import sys
import zlib

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pintor as P  # noqa: E402

RAIZ = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", ".."))
RES = os.path.join(RAIZ, "src", "main", "resources")
ASSETS = os.path.join(RES, "assets", "darkcraft")
DATA = os.path.join(RES, "data")
JAVA = os.path.join(RAIZ, "src", "main", "java", "com", "darkcraft", "bloques", "CatalogoBloques.java")
TEXTOS = os.path.join(RAIZ, "ci", "historia", "bloques_nombres.py")

# ------------------------------------------------------------------ las rampas (de lo oscuro a lo claro)

R = {
    "granito": ["#1f1d1c", "#2c2a28", "#3a3734", "#48443f", "#56524c", "#645f58", "#726c64", "#817a70", "#90897e", "#a0988c",
                "#b1a99c", "#c3bbae"],
    "caliza": ["#3b352b", "#4d4538", "#605745", "#736853", "#867a62", "#988c73", "#a99d84", "#b8ad95", "#c6bca5", "#d3cab5",
               "#e0d8c5", "#ece6d6"],
    "basalto": ["#0c0b0e", "#141318", "#1c1b21", "#24232a", "#2d2c34", "#36353e", "#403f48", "#4a4952", "#55545e", "#61606a",
                "#6e6d77", "#7c7b85"],
    "frio": ["#1e2228", "#2a2f36", "#363c44", "#434952", "#505760", "#5d656e", "#6b737c", "#79818a", "#878f98", "#959da6",
             "#a4acb4", "#b3bbc2"],
    "pantano": ["#151a14", "#1e241c", "#272e24", "#30382d", "#3a4336", "#444e3f", "#4f5949", "#5a6453", "#666f5e", "#727b69",
                "#7f8775", "#8c9481"],
    "marmol": ["#8e8a84", "#a19d96", "#b3afa8", "#c3bfb8", "#d1cdc6", "#dcd8d1", "#e5e1da", "#ece9e3", "#f3f0eb", "#f9f7f3"],
    "marmol_negro": ["#08070a", "#0f0e12", "#16151a", "#1d1c22", "#24232a", "#2c2b33", "#35343d", "#3f3e48", "#4a4954"],
    "arenisca": ["#2a130c", "#3a1b11", "#4b2416", "#5c2e1c", "#6e3922", "#804429", "#924f30", "#a35b38", "#b36841", "#c2764b",
                 "#cf8556", "#db9462"],
    "ladrillo": ["#2e140e", "#401c13", "#522418", "#642c1d", "#763422", "#883c28", "#9a452e", "#ab5034", "#ba5c3c", "#c86a46",
                 "#d47a54"],
    "ladrillo_oscuro": ["#140f0f", "#1d1616", "#271e1e", "#312626", "#3b2f2e", "#463837", "#514140", "#5c4b49", "#685553",
                        "#745f5c"],
    "canto": ["#1f1c19", "#2b2723", "#38332d", "#453f38", "#524b43", "#5f574e", "#6c645a", "#7a7166", "#887f73", "#978d80",
              "#a69c8e"],
    "losa": ["#24221f", "#312e2a", "#3f3b36", "#4d4943", "#5b5650", "#69645d", "#77716a", "#867f77", "#958e85", "#a59d93",
             "#b5ada2"],
    # capas de encima y juntas
    "musgo": ["#1c2410", "#283318", "#344220", "#405128", "#4d6030", "#5a6f38", "#687d42", "#778c4e"],
    "escarcha": ["#9fb3c4", "#b4c6d4", "#c8d8e3", "#dbe7ef", "#ebf3f8", "#f7fbfd"],
    "verdin": ["#18240f", "#223316", "#2c421d", "#375224", "#43632c", "#4f7334", "#5c833e", "#6a9349"],
    "vena_gris": ["#6f6b66", "#7d7974", "#8c8882", "#9b9790", "#aaa69f", "#b8b4ad"],
    "vena_clara": ["#c9c5be", "#d3cfc8", "#dcd8d2"],
    "vena_plata": ["#4d4c55", "#5c5b65", "#6c6b75", "#7d7c86"],
    "vena_oro": ["#5a4318", "#7a5c22", "#9a752e", "#b88f3c", "#d2a94e", "#e8c467"],
    "cal": ["#4a453d", "#5d574d", "#716a5e", "#857d6f", "#999181", "#ada593", "#bfb7a5"],
    "tierra": ["#1d1610", "#272016", "#31291d", "#3c3324", "#473d2c"],
    "junta_oscura": ["#0e0c0c", "#161313", "#1f1b1b", "#282323"],
}
RAMPAS = {k: P.Rampa(v) for k, v in R.items()}


def con_junta(L, piedra, junta=None, tramado=0.7):
    """Las piedras con su rampa y las juntas con la suya (o con la misma, si no hay)."""
    rgb = RAMPAS[piedra].pintar(L.v, tramado)
    if junta:
        jv = np.clip((L.v - 0.04) / 0.86, 0, 1) if junta == "cal" else np.clip(L.v * 3.0, 0, 1)
        rj = RAMPAS[junta].pintar(jv, tramado)
        m = (L.piedra < 0)[..., None]
        rgb = np.where(m, rj, rgb)
    return rgb


def capa(rgb, rampa, mascara, luz, tramado=0.8):
    """Una capa (vetas...) encima, a pixeles enteros: su rampa con la luz dada, donde dice la mascara."""
    m = P.dura(mascara, tramado)
    encima = RAMPAS[rampa].pintar(np.clip(luz, 0, 1), 0.5)
    return np.where(m[..., None], encima, rgb)


def capa_bultos(rgb, rampa, mascara, s, base=0.45, sombra=0.22, tramado=0.8):
    """Una capa con cuerpo (musgo, nieve): bultitos, el borde de arriba claro y el de abajo oscuro, y la sombra que echa
    sobre la piedra de debajo."""
    m = P.dura(mascara, tramado)
    bulto = P.ruido(s + 31, base=32, octavas=1)
    arriba = m & ~np.roll(m, 1, axis=0)
    abajo = m & ~np.roll(m, -1, axis=0)
    luz = base + 0.34 * (bulto - 0.5) + 0.22 * arriba - 0.20 * abajo
    encima = RAMPAS[rampa].pintar(np.clip(luz, 0, 1), 0.5)
    if sombra:
        bajo = np.roll(m, 1, axis=0) & ~m
        rgb = np.where(bajo[..., None], rgb * (1 - sombra), rgb)
    return np.where(m[..., None], encima, rgb)


# ------------------------------------------------------------------ las recetas de las texturas (s: la de la variante,
# b: la comun a todas las variantes del bloque)

def t_sillar_gris(s, b):
    return con_junta(P.sillares(s, granos=0.05, borde=b), "granito")


def t_sillar_palido(s, b):
    L = P.sillares(s, hiladas=(21, 21, 22), anchos=(22, 44), base=0.60, granos=0.01, grietas=0.2, borde=b)
    return con_junta(L, "caliza")


def t_sillar_oscuro(s, b):
    L = P.sillares(s, anchos=(20, 34), base=0.55, granos=0.03, desconchones=0.6, grietas=0.35, picado=0.01, borde=b)
    return con_junta(L, "basalto")


def t_sillar_musgoso(s, b):
    L = P.sillares(s, granos=0.04, desconchones=0.7, borde=b)
    rgb = con_junta(L, "granito")
    return capa_bultos(rgb, "musgo", P.musgo(L, s, cuanto=0.8, borde=b), s, base=0.42)


def t_sillar_agrietado(s, b):
    L = P.sillares(s, granos=0.04, desconchones=0.9, grietas=1.0, manchas=0.9, picado=0.012, borde=b)
    rng = np.random.default_rng(s + 5)
    for _ in range(2):
        x, y = int(rng.integers(14, 40)), int(rng.integers(14, 40))
        P.grieta(L, rng, x, y, 12, 12, largo=20, cruza=True, hondo=0.07)
    return con_junta(L, "granito")


def t_sillar_nevado(s, b):
    L = P.sillares(s, granos=0.05, desconchones=0.4, borde=b)
    rgb = con_junta(L, "frio")
    return capa_bultos(rgb, "escarcha", P.escarcha(L, s, cuanto=0.6, borde=b), s, base=0.62, sombra=0.12)


def t_sillar_verdin(s, b):
    L = P.sillares(s, anchos=(16, 32), base=0.50, desconchones=0.6, manchas=1.0, borde=b)
    rgb = con_junta(L, "pantano")
    m = P.verdin(L, s) * 0.85
    return capa(rgb, "verdin", m, 0.25 + 0.6 * P.ruido(s + 23, base=12, octavas=2))


def t_marmol_blanco(s, b):
    L = P.marmol(s, vetas=1, base=0.70)
    rgb = RAMPAS["marmol"].pintar(L.v, 0.5)
    rgb = capa(rgb, "vena_gris", L.vena, 0.25 + 0.7 * P.ruido(s + 24, base=8, octavas=2), tramado=0.2)
    return capa(rgb, "vena_clara", L.vena2, 0.2 + 0.6 * P.ruido(s + 26, base=8, octavas=2), tramado=0.2)


def t_marmol_negro(s, b):
    L = P.marmol(s, vetas=1, base=0.55)
    rgb = RAMPAS["marmol_negro"].pintar(L.v, 0.5)
    rgb = capa(rgb, "vena_oro", L.vena, 0.25 + 0.7 * P.ruido(s + 25, base=8, octavas=2), tramado=0.2)
    return capa(rgb, "vena_plata", L.vena2, 0.2 + 0.6 * P.ruido(s + 27, base=8, octavas=2), tramado=0.2)


def t_sillar_rojo(s, b):
    L = P.sillares(s, anchos=(20, 40), base=0.58, granos=0.02, grietas=0.25, borde=b)
    bandas = (0.05 * np.sin(np.arange(64) * 2 * np.pi / 8))[:, None]
    L.v = np.where(L.piedra >= 0, L.v + bandas, L.v)
    return con_junta(L, "arenisca")


def t_ladrillo_viejo(s, b):
    return con_junta(P.ladrillos(s, borde=b), "ladrillo", "cal")


def t_ladrillo_oscuro(s, b):
    L = P.ladrillos(s, base=0.52, varia=0.07, quemados=0.2, borde=b)
    L.v = np.where(L.piedra < 0, 0.12, L.v)
    return con_junta(L, "ladrillo_oscuro", "junta_oscura")


def t_adoquin(s, b):
    L = P.cantos(s, cuantos=26, junta=0.95, redondez=4.5, borde=b)
    return con_junta(L, "canto", "tierra")


def t_enlosado(s, b):
    return con_junta(P.losas(s, borde=b), "losa")


def t_piedra_tallada(s, b):
    return con_junta(P.tallado(s), "caliza")


def t_mamposteria(s, b):
    L = P.cantos(s, cuantos=14, junta=2.0, redondez=5.0, varia=0.12, base=0.54, borde=b)
    L.v = np.where(L.piedra < 0, 0.55 + 0.25 * P.ruido(b + 30, base=16, octavas=2), L.v)
    return con_junta(L, "canto", "cal")


def t_col_lado(rampa, base):
    return lambda s: RAMPAS[rampa].pintar(P.columna_lado(s, base=base).v, 0.5)


def t_col_tapa(rampa, base):
    return lambda s: RAMPAS[rampa].pintar(P.columna_tapa(s, base=base).v, 0.5)


# ------------------------------------------------------------------ el catalogo

def M(id, es, en, receta, formas="BELM", sonido="STONE", mapa="STONE", semilla=None, variantes=3, dureza=1.5, **kw):
    """Un material. sonido: una constante de SoundType; mapa: una de MapColor (las comprueba el compilador de Java)."""
    return dict(id=id, es=es, en=en, receta=receta, formas=formas, sonido=sonido, mapa=mapa,
                semilla=semilla or zlib.crc32(id.encode()) % 10000, variantes=variantes, dureza=dureza, **kw)


MATERIALES = [
    M("sillar_gris", "Sillar de granito", "Granite Ashlar", t_sillar_gris, sonido="STONE", mapa="STONE", semilla=101),
    M("sillar_palido", "Sillar de caliza", "Limestone Ashlar", t_sillar_palido, sonido="TUFF", mapa="SAND", semilla=102),
    M("sillar_oscuro", "Sillar de basalto", "Basalt Ashlar", t_sillar_oscuro, sonido="DEEPSLATE_BRICKS", mapa="COLOR_BLACK", semilla=103, dureza=2.0),
    M("sillar_musgoso", "Sillar musgoso", "Mossy Ashlar", t_sillar_musgoso, sonido="STONE", mapa="COLOR_GREEN", semilla=104),
    M("sillar_agrietado", "Sillar agrietado", "Cracked Ashlar", t_sillar_agrietado, sonido="STONE", mapa="STONE", semilla=105),
    M("sillar_nevado", "Sillar escarchado", "Frosted Ashlar", t_sillar_nevado, sonido="STONE", mapa="ICE", semilla=106),
    M("sillar_verdin", "Sillar con verdin", "Slimed Ashlar", t_sillar_verdin, sonido="MUD_BRICKS", mapa="COLOR_GREEN", semilla=107),
    M("marmol_blanco", "Marmol blanco", "White Marble", t_marmol_blanco, sonido="CALCITE", mapa="QUARTZ", semilla=108),
    M("marmol_negro", "Marmol negro", "Black Marble", t_marmol_negro, sonido="POLISHED_DEEPSLATE", mapa="COLOR_BLACK", semilla=109, dureza=2.0),
    M("sillar_rojo", "Sillar de arenisca roja", "Red Sandstone Ashlar", t_sillar_rojo, sonido="STONE", mapa="COLOR_RED", semilla=110),
    M("ladrillo_viejo", "Ladrillo viejo", "Old Brick", t_ladrillo_viejo, sonido="MUD_BRICKS", mapa="COLOR_RED", semilla=111),
    M("ladrillo_oscuro", "Ladrillo oscuro", "Dark Brick", t_ladrillo_oscuro, sonido="NETHER_BRICKS", mapa="COLOR_BROWN", semilla=112),
    M("adoquin", "Adoquin", "Cobbled Paving", t_adoquin, sonido="STONE", mapa="STONE", semilla=113),
    M("enlosado", "Enlosado", "Flagstones", t_enlosado, sonido="STONE", mapa="STONE", semilla=114),
    M("piedra_tallada", "Piedra tallada", "Carved Stone", t_piedra_tallada, sonido="STONE", mapa="SAND", semilla=115, variantes=1),
    M("mamposteria", "Mamposteria", "Rubble Masonry", t_mamposteria, sonido="STONE", mapa="STONE", semilla=116),
    M("columna_gris", "Columna de granito", "Granite Column", None, formas="C", sonido="STONE", mapa="STONE", semilla=121,
      lado=t_col_lado("granito", 0.58), tapa=t_col_tapa("granito", 0.58)),
    M("columna_palida", "Columna de caliza", "Limestone Column", None, formas="C", sonido="STONE", mapa="SAND", semilla=122,
      lado=t_col_lado("caliza", 0.62), tapa=t_col_tapa("caliza", 0.62)),
    M("columna_marmol", "Columna de marmol", "Marble Column", None, formas="C", sonido="CALCITE", mapa="QUARTZ", semilla=123,
      lado=t_col_lado("marmol", 0.66), tapa=t_col_tapa("marmol", 0.66)),
    M("columna_oscura", "Columna de basalto", "Basalt Column", None, formas="C", sonido="DEEPSLATE_BRICKS", mapa="COLOR_BLACK",
      semilla=124, lado=t_col_lado("basalto", 0.58), tapa=t_col_tapa("basalto", 0.58)),
]

FORMAS = {"B": "", "E": "_escalera", "L": "_losa", "M": "_muro"}
NOMBRE_FORMA_ES = {"B": "{}", "E": "Escalera de {}", "L": "Losa de {}", "M": "Muro de {}"}
NOMBRE_FORMA_EN = {"B": "{}", "E": "{} Stairs", "L": "{} Slab", "M": "{} Wall"}


def minus(es):
    return es[0].lower() + es[1:]


# ------------------------------------------------------------------ texturas

def nombres(m):
    """Los nombres de las texturas de cada variante de un material de bloque entero: id, id_2, id_3..."""
    return [m["id"] if v == 0 else f'{m["id"]}_{v + 1}' for v in range(m["variantes"])]


def texturas(m):
    """Las texturas de un material: {nombre de fichero: imagen}."""
    s = m["semilla"]
    if "C" in m["formas"]:
        return {m["id"] + "_lado": P.imagen(m["lado"](s)), m["id"] + "_tapa": P.imagen(m["tapa"](s + 1))}
    return {n: P.imagen(m["receta"](s + 1000 * v, s)) for v, n in enumerate(nombres(m))}


# ------------------------------------------------------------------ JSON del juego

def j(ruta, obj):
    os.makedirs(os.path.dirname(ruta), exist_ok=True)
    with open(ruta, "w", encoding="utf-8") as f:
        json.dump(obj, f, indent=2)
        f.write("\n")


def estado_escalera(modelo):
    giro = {"east": 0, "south": 90, "west": 180, "north": 270}
    v = {}
    for facing, y in giro.items():
        for half in ("bottom", "top"):
            for shape in ("straight", "inner_left", "inner_right", "outer_left", "outer_right"):
                md = modelo + ("_interior" if shape.startswith("inner") else "_exterior" if shape.startswith("outer") else "")
                yy = y
                if half == "bottom" and shape in ("inner_left", "outer_left"):
                    yy = y - 90
                if half == "top" and shape in ("inner_right", "outer_right"):
                    yy = y + 90
                yy %= 360
                e = {"model": md}
                if half == "top":
                    e["x"] = 180
                if yy:
                    e["y"] = yy
                if half == "top" or yy:
                    e["uvlock"] = True
                v[f"facing={facing},half={half},shape={shape}"] = e
    return {"variants": v}


def estado_muro(id):
    partes = [{"when": {"up": "true"}, "apply": {"model": f"darkcraft:block/{id}_poste"}}]
    for lado, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        for alto, md in (("low", "_lado"), ("tall", "_lado_alto")):
            a = {"model": f"darkcraft:block/{id}{md}", "uvlock": True}
            if y:
                a["y"] = y
            partes.append({"when": {lado: alto}, "apply": a})
    return {"multipart": partes}


def escribir_material(m):
    bid = m["id"]
    tex = f"darkcraft:block/{bid}"
    bs = os.path.join(ASSETS, "blockstates")
    mb = os.path.join(ASSETS, "models", "block")
    mi = os.path.join(ASSETS, "models", "item")
    ids = []
    if "C" in m["formas"]:
        lado, tapa = tex + "_lado", tex + "_tapa"
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/cube_column", "textures": {"end": tapa, "side": lado}})
        j(os.path.join(mb, bid + "_tumbada.json"), {"parent": "minecraft:block/cube_column_horizontal", "textures": {"end": tapa, "side": lado}})
        j(os.path.join(bs, bid + ".json"), {"variants": {
            "axis=y": {"model": f"darkcraft:block/{bid}"},
            "axis=z": {"model": f"darkcraft:block/{bid}_tumbada", "x": 90},
            "axis=x": {"model": f"darkcraft:block/{bid}_tumbada", "x": 90, "y": 90}}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    enteros = [{"model": f"darkcraft:block/{n}"} for n in nombres(m)]
    if "B" in m["formas"]:
        for n in nombres(m):
            j(os.path.join(mb, n + ".json"), {"parent": "minecraft:block/cube_all", "textures": {"all": f"darkcraft:block/{n}"}})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": enteros if len(enteros) > 1 else enteros[0]}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        ids.append(bid)
    if "E" in m["formas"]:
        e = bid + "_escalera"
        t = {"bottom": tex, "top": tex, "side": tex}
        j(os.path.join(mb, e + ".json"), {"parent": "minecraft:block/stairs", "textures": t})
        j(os.path.join(mb, e + "_interior.json"), {"parent": "minecraft:block/inner_stairs", "textures": t})
        j(os.path.join(mb, e + "_exterior.json"), {"parent": "minecraft:block/outer_stairs", "textures": t})
        j(os.path.join(bs, e + ".json"), estado_escalera(f"darkcraft:block/{e}"))
        j(os.path.join(mi, e + ".json"), {"parent": f"darkcraft:block/{e}"})
        ids.append(e)
    if "L" in m["formas"]:
        l = bid + "_losa"
        t = {"bottom": tex, "top": tex, "side": tex}
        j(os.path.join(mb, l + ".json"), {"parent": "minecraft:block/slab", "textures": t})
        j(os.path.join(mb, l + "_arriba.json"), {"parent": "minecraft:block/slab_top", "textures": t})
        j(os.path.join(bs, l + ".json"), {"variants": {
            "type=bottom": {"model": f"darkcraft:block/{l}"},
            "type=top": {"model": f"darkcraft:block/{l}_arriba"},
            "type=double": enteros if len(enteros) > 1 else enteros[0]}})
        j(os.path.join(mi, l + ".json"), {"parent": f"darkcraft:block/{l}"})
        ids.append(l)
    if "M" in m["formas"]:
        w = bid + "_muro"
        t = {"wall": tex}
        j(os.path.join(mb, w + "_poste.json"), {"parent": "minecraft:block/template_wall_post", "textures": t})
        j(os.path.join(mb, w + "_lado.json"), {"parent": "minecraft:block/template_wall_side", "textures": t})
        j(os.path.join(mb, w + "_lado_alto.json"), {"parent": "minecraft:block/template_wall_side_tall", "textures": t})
        j(os.path.join(mb, w + "_inventario.json"), {"parent": "minecraft:block/wall_inventory", "textures": t})
        j(os.path.join(bs, w + ".json"), estado_muro(w))
        j(os.path.join(mi, w + ".json"), {"parent": f"darkcraft:block/{w}_inventario"})
        ids.append(w)
    return ids


def botin(ids):
    """Lo que suelta cada bloque al romperlo (en supervivencia): el mismo; la losa doble, dos."""
    d = os.path.join(DATA, "darkcraft", "loot_tables", "blocks")
    for bid in ids:
        entrada = {"type": "minecraft:item", "name": "darkcraft:" + bid}
        if bid.endswith("_losa"):
            entrada["functions"] = [{"function": "minecraft:set_count", "count": 2, "add": False, "conditions": [
                {"condition": "minecraft:block_state_property", "block": "darkcraft:" + bid, "properties": {"type": "double"}}]},
                {"function": "minecraft:explosion_decay"}]
        pool = {"rolls": 1, "bonus_rolls": 0, "entries": [entrada]}
        if not bid.endswith("_losa"):
            pool["conditions"] = [{"condition": "minecraft:survives_explosion"}]
        j(os.path.join(d, bid + ".json"), {"type": "minecraft:block", "pools": [pool], "random_sequence": "darkcraft:blocks/" + bid})


def etiquetas():
    """Las etiquetas de Minecraft que necesitan (los muros se unen entre si; escaleras y losas; con pico se rompen)."""
    muros, escaleras, losas, todos = [], [], [], []
    for m in MATERIALES:
        b = "darkcraft:" + m["id"]
        if "B" in m["formas"] or "C" in m["formas"]:
            todos.append(b)
        if "E" in m["formas"]:
            escaleras.append(b + "_escalera")
            todos.append(b + "_escalera")
        if "L" in m["formas"]:
            losas.append(b + "_losa")
            todos.append(b + "_losa")
        if "M" in m["formas"]:
            muros.append(b + "_muro")
            todos.append(b + "_muro")
    t = os.path.join(DATA, "minecraft", "tags", "blocks")
    j(os.path.join(t, "walls.json"), {"replace": False, "values": muros})
    j(os.path.join(t, "stairs.json"), {"replace": False, "values": escaleras})
    j(os.path.join(t, "slabs.json"), {"replace": False, "values": losas})
    j(os.path.join(t, "mineable", "pickaxe.json"), {"replace": False, "values": todos})
    ti = os.path.join(DATA, "minecraft", "tags", "items")
    j(os.path.join(ti, "walls.json"), {"replace": False, "values": muros})
    j(os.path.join(ti, "stairs.json"), {"replace": False, "values": escaleras})
    j(os.path.join(ti, "slabs.json"), {"replace": False, "values": losas})


def textos():
    es, en = {}, {}
    for m in MATERIALES:
        for f in m["formas"]:
            if f == "C":
                es["block.darkcraft." + m["id"]] = m["es"]
                en["block.darkcraft." + m["id"]] = m["en"]
                continue
            bid = m["id"] + FORMAS[f]
            es["block.darkcraft." + bid] = NOMBRE_FORMA_ES[f].format(m["es"] if f == "B" else minus(m["es"]))
            en["block.darkcraft." + bid] = NOMBRE_FORMA_EN[f].format(m["en"])
    es["itemGroup.darkcraft.bloques"] = "Dark Craft · Bloques"
    en["itemGroup.darkcraft.bloques"] = "Dark Craft · Blocks"
    with open(TEXTOS, "w", encoding="utf-8") as f:
        f.write('"""GENERADO por tools/bloques/bloques.py: no tocar a mano. Los nombres de los bloques nuevos (espanol e ingles)."""\n')
        f.write("ES = " + json.dumps(es, ensure_ascii=False, indent=1) + "\n")
        f.write("EN = " + json.dumps(en, ensure_ascii=False, indent=1) + "\n")


def java():
    filas = []
    for m in MATERIALES:
        filas.append(f'            new Material("{m["id"]}", "{m["formas"]}", SoundType.{m["sonido"]}, MapColor.{m["mapa"]}, {m["dureza"]}F)')
    src = """package com.darkcraft.bloques;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;

/** GENERADO por tools/bloques/bloques.py: no tocar a mano. Los materiales de los bloques nuevos y sus formas. */
public final class CatalogoBloques {
    private CatalogoBloques() {}

    /** Un material: su id, sus formas (B bloque, E escalera, L losa, M muro, C columna), su sonido, su color en el mapa y lo
     *  que cuesta romperlo. */
    public record Material(String id, String formas, SoundType sonido, MapColor mapa, float dureza) {}

    public static final Material[] MATERIALES = {
""" + ",\n".join(filas) + """
    };
}
"""
    os.makedirs(os.path.dirname(JAVA), exist_ok=True)
    with open(JAVA, "w", encoding="utf-8") as f:
        f.write(src)


# ------------------------------------------------------------------ hojas para revisar

def cubo(top, side, s=2):
    """Un cubo en isometrica (arriba entera la luz, la cara izquierda x0,8 y la derecha x0,62), a s pixeles por texel."""
    n = top.size[0]
    t = np.asarray(top.convert("RGB"), dtype=float)
    sd = np.asarray(side.convert("RGB"), dtype=float)
    W = H = 2 * n * s
    out = np.full((H, W, 3), 30.0)
    ys, xs = np.mgrid[0:H, 0:W].astype(float) + 0.5
    u = (xs - W / 2) / s
    v = ys / s
    a, b = v + u / 2, v - u / 2
    mt = (a >= 0) & (a < n) & (b >= 0) & (b < n)
    out[mt] = t[b[mt].astype(int), a[mt].astype(int)]
    al = u + n
    cl = v - n / 2 - 0.5 * al
    ml = (al >= 0) & (al < n) & (cl >= 0) & (cl < n) & ~mt
    out[ml] = sd[cl[ml].astype(int), al[ml].astype(int)] * 0.8
    ar = u
    cr = v - n + 0.5 * ar
    mr = (ar >= 0) & (ar < n) & (cr >= 0) & (cr < n) & ~mt
    out[mr] = sd[cr[mr].astype(int), ar[mr].astype(int)] * 0.62
    return Image.fromarray(np.clip(out, 0, 255).astype(np.uint8), "RGB")


def vista(carpeta):
    os.makedirs(carpeta, exist_ok=True)
    tarjetas = []
    for m in MATERIALES:
        tx = texturas(m)
        if "C" in m["formas"]:
            lado, tapa = tx[m["id"] + "_lado"], tx[m["id"] + "_tapa"]
        else:
            lado = tapa = tx[m["id"]]
        # un muro de 4 x 4 bloques con las variantes al azar (como en el juego) y el cubo
        rng = np.random.default_rng(7)
        lista = [tx[n] for n in (nombres(m) if "C" not in m["formas"] else [m["id"] + "_lado"])]
        rep = Image.new("RGBA", (256, 256))
        for a in range(4):
            for b in range(4):
                rep.paste(lista[int(rng.integers(0, len(lista)))], (a * 64, b * 64))
        cb = cubo(tapa, lado, s=2)
        t = Image.new("RGB", (256 + cb.size[0] + 8, max(256, cb.size[1])), (30, 30, 30))
        t.paste(rep.convert("RGB"), (0, 0))
        t.paste(cb, (264, 0))
        tarjetas.append((m["id"], t))
    for i in range(0, len(tarjetas), 4):
        grupo = tarjetas[i:i + 4]
        H = sum(t.size[1] + 4 for _, t in grupo)
        W = max(t.size[0] for _, t in grupo)
        hoja = Image.new("RGB", (W, H), (20, 20, 20))
        y = 0
        for _, t in grupo:
            hoja.paste(t, (0, y))
            y += t.size[1] + 4
        hoja.save(os.path.join(carpeta, f"hoja_{i // 4 + 1}.png"))
    print("hojas en", carpeta)


def main():
    args = sys.argv[1:]
    tdir = os.path.join(ASSETS, "textures", "block")
    os.makedirs(tdir, exist_ok=True)
    ids = []
    for m in MATERIALES:
        for nombre, img in texturas(m).items():
            img.save(os.path.join(tdir, nombre + ".png"))
        ids += escribir_material(m)
    botin(ids)
    etiquetas()
    textos()
    java()
    print(f"{len(MATERIALES)} materiales, {len(ids)} bloques")
    if "--vista" in args:
        vista(args[args.index("--vista") + 1])


if __name__ == "__main__":
    main()
