"""Los bloques nuevos de Dark Craft (la gran reforma, parte 8b y siguientes): el catalogo, sus texturas de 64 x 64 y todo
lo que necesita el juego (estados, modelos, modelos de objeto, etiquetas, nombres y la lista de Java).

Uso:
    python3 tools/bloques/bloques.py            # escribe texturas, JSON, CatalogoBloques.java y ci/historia/bloques.py
    python3 tools/bloques/bloques.py --vista /tmp/vb   # ademas, hojas para revisar (cada textura repetida y un cubo)

Cada material tiene sus formas: B bloque, E escalera, L losa, M muro, C columna (con lado y tapa), V valla, P cancela
(la puerta de la valla), D puerta, T trampilla. Los ids: <id> (o <id> la columna), <id>_escalera, <id>_losa, <id>_muro,
<id>_valla, <id>_cancela, <id>_puerta, <id>_trampilla.
"""
import json
import os
import sys
import zlib

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import pintor as P  # noqa: E402
import madera as W  # noqa: E402

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
    # maderas (parte 8c)
    "m_negra": ["#08070a", "#0f0c0b", "#17120f", "#1f1813", "#271f18", "#30261d", "#3a2e23", "#453629", "#513f30", "#5d4837"],
    "m_roble": ["#1a0f08", "#24160c", "#2f1d10", "#3b2514", "#472d19", "#54361e", "#613f24", "#6e492a", "#7c5331", "#8a5e39",
                "#986942"],
    "m_gris": ["#151413", "#1f1d1b", "#2a2724", "#36322e", "#423d38", "#4e4842", "#5b544d", "#686058", "#766d64", "#847a70",
               "#93887d"],
    "m_ceniza": ["#2e2924", "#3c362f", "#4b443b", "#5a5247", "#6a6154", "#7a7061", "#8a806f", "#9b907e", "#aca08e",
                 "#bdb19f", "#cdc2b0"],
    "m_podrida": ["#0c0d08", "#13140c", "#1b1b11", "#232315", "#2c2b1a", "#35331f", "#3f3c25", "#49452b", "#544f31", "#5f5938"],
    "m_quemada": ["#040404", "#090808", "#0f0d0c", "#161311", "#1d1916", "#251f1b", "#2e2620", "#382e26"],
    "m_caoba": ["#150605", "#200a07", "#2b0f0a", "#36140d", "#421a10", "#4f2014", "#5c2718", "#692e1c", "#773521", "#853d26"],
    "hierro": ["#0b0b0d", "#141417", "#1e1e22", "#29292e", "#35353b", "#424249", "#505058", "#5f5f68", "#707079"],
    "enlucido": ["#5b564c", "#6a6458", "#797265", "#888172", "#978f80", "#a69e8e", "#b4ac9c", "#c2baaa", "#cec7b8", "#d9d3c5"],
    "pizarra": ["#0b0c0f", "#121418", "#1a1d22", "#22262c", "#2b3037", "#353a42", "#3f454e", "#4a505a", "#565d67", "#626974"],
    "pizarra_azul": ["#0a0c12", "#10141c", "#171c26", "#1f2531", "#272e3c", "#303848", "#3a4254", "#454d60", "#50596c"],
    "teja": ["#200b06", "#2e1009", "#3d150c", "#4c1b0f", "#5b2112", "#6b2816", "#7a2f1a", "#8a371f", "#994026", "#a84a2e",
             "#b65537"],
    "liquen": ["#6e6a4c", "#7f7a57", "#908a62", "#a29b6e"],
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


# ------------------------------------------------------------------ la madera, los entramados y los tejados (parte 8c)

def t_tablas(rampa, **kw):
    """Tablones del material (las variantes casan por `borde`)."""
    return lambda s, b: RAMPAS[rampa].pintar(W.tablones(s, borde=b, **kw).v, 0.6)


def t_viga_lado(rampa, base=0.55):
    return lambda s: RAMPAS[rampa].pintar(W.viga_lado(s, base=base).v, 0.6)


def t_viga_tapa(rampa, base=0.55):
    return lambda s: RAMPAS[rampa].pintar(W.viga_tapa(s, base=base).v, 0.6)


def _herraje(rgb, hierro, s, hueco=None):
    """Pinta el hierro (con su filo de luz arriba y su sombra abajo, y los remaches) sobre la madera; el hueco, transparente."""
    h = hierro.astype(float)
    luz = 0.42 + 0.30 * np.clip(P.relieve(h), -1, 1) + 0.10 * (P.ruido(s + 51, base=16, octavas=2) - 0.5)
    rem = W.remaches(hierro)
    luz = np.where(rem, 0.95, luz)
    luz = np.where(np.roll(rem, -1, axis=0) | np.roll(rem, -1, axis=1), np.maximum(luz - 0.25, 0.1), luz)
    met = RAMPAS["hierro"].pintar(np.clip(luz, 0, 1), 0.5)
    sombra = (~hierro) & np.roll(hierro, 1, axis=0)
    rgb = np.where(sombra[..., None], rgb * 0.72, rgb)
    rgb = np.where(hierro[..., None], met, rgb)
    alfa = None
    if hueco is not None:
        alfa = np.where(hueco, 0, 255)
    return rgb, alfa


def extra_madera(rampa, base=0.55, puerta=True, trampilla=True):
    """Las texturas de la puerta (abajo, arriba y su dibujo de objeto) y de la trampilla de una madera."""
    def f(m):
        s = m["semilla"] + 500
        out = {}
        if puerta:
            mitades = W.puerta(s, base=base)
            imgs = []
            for nombre, (L, hierro, hueco) in zip(("_puerta_abajo", "_puerta_arriba"), mitades):
                rgb = RAMPAS[rampa].pintar(L.v, 0.6)
                rgb, alfa = _herraje(rgb, hierro, s, hueco if hueco.any() else None)
                img = P.imagen(rgb, alfa)
                out[m["id"] + nombre] = img
                imgs.append(img)
            # el objeto: la puerta entera reducida a la mitad, en el centro de 64 x 64
            entera = Image.new("RGBA", (64, 128))
            entera.paste(imgs[1], (0, 0))
            entera.paste(imgs[0], (0, 64))
            peq = entera.resize((32, 64), Image.BOX)
            obj = Image.new("RGBA", (64, 64))
            obj.paste(peq, (16, 0))
            out["item/" + m["id"] + "_puerta"] = obj
        if trampilla:
            L, hierro = W.trampilla(s + 7, base=base)
            rgb = RAMPAS[rampa].pintar(L.v, 0.6)
            rgb, _ = _herraje(rgb, hierro, s + 7)
            out[m["id"] + "_trampilla"] = P.imagen(rgb)
        return out
    return f


def t_entramado(figura, viga="m_negra"):
    def f(s, b):
        E, V, mascara = W.entramado(s, figura, ladrillo=1.0 if (s - b) // 1000 == 2 else 0.0)
        cal = RAMPAS["enlucido"].pintar(E.v, 0.6)
        if E.ladrillo.any():
            lad = con_junta(P.ladrillos(s + 5, alto=4, anchos=(7, 9)), "ladrillo", "cal")
            cal = np.where(E.ladrillo[..., None], lad, cal)
        madera = RAMPAS[viga].pintar(V.v, 0.6)
        return np.where(mascara[..., None], madera, cal)
    return f


def t_enlucido(s, b):
    # solo la tercera variante (la rara: pesos 4, 4, 1) deja ver el ladrillo
    E = W.enlucido(s, ladrillo=1.0 if (s - b) // 1000 == 2 else 0.0)
    cal = RAMPAS["enlucido"].pintar(E.v, 0.6)
    if E.ladrillo.any():
        lad = con_junta(P.ladrillos(s + 5, alto=4, anchos=(7, 9)), "ladrillo", "cal")
        cal = np.where(E.ladrillo[..., None], lad, cal)
    return cal


def t_pizarra(s, b):
    return con_junta(W.pizarra(s, borde=b), "pizarra")


def t_escamas(s, b):
    return con_junta(W.pizarra(s, alto=8, anchos=(8, 8), escamas=True, base=0.52, borde=b), "pizarra_azul")


def t_barro(s, b):
    L = W.barro(s, borde=b)
    rgb = con_junta(L, "teja")
    lq = np.clip((P.ruido(s + 61, base=16, octavas=2) - 0.80) * 6, 0, 1)
    return capa(rgb, "liquen", lq, 0.3 + 0.5 * P.ruido(s + 62, base=16, octavas=2))


def t_barro_musgo(s, b):
    L = W.barro(s, borde=b)
    rgb = con_junta(L, "teja")
    abajo = np.tile(((np.arange(64) % 16) >= 9).astype(float)[:, None], (1, 64))
    m = 0.55 * P.ruido(b + 63, base=4, octavas=4) + 0.25 * P.ruido(s + 64, base=16, octavas=2) + 0.25 * L.canal + 0.15 * abajo
    return capa_bultos(rgb, "musgo", np.clip((m - 0.62) * 6, 0, 1), s, base=0.42)


def t_tejamanil(s, b):
    return con_junta(W.tejamanil(s, borde=b), "m_gris")


# ------------------------------------------------------------------ el catalogo

def M(id, es, en, receta, formas="BELM", sonido="STONE", mapa="STONE", semilla=None, variantes=3, dureza=1.5,
      herramienta="pickaxe", juego="STONE", madera="OAK", extra=None, **kw):
    """Un material. sonido: una constante de SoundType; mapa: una de MapColor; juego: una de BlockSetType (puertas y
    trampillas); madera: una de WoodType (cancelas) —las comprueba el compilador de Java—; herramienta: pickaxe o axe;
    extra(m): texturas de mas (puerta, trampilla; las que empiezan por "item/" van a las de los objetos)."""
    return dict(id=id, es=es, en=en, receta=receta, formas=formas, sonido=sonido, mapa=mapa,
                semilla=semilla or zlib.crc32(id.encode()) % 10000, variantes=variantes, dureza=dureza,
                herramienta=herramienta, juego=juego, madera=madera, extra=extra, **kw)


def MADERA(id, es, en, rampa, mapa, juego, madera, semilla, base=0.55, formas="BELVPDT", **kw):
    """Una madera: tablones (con escalera, losa, valla, cancela, puerta y trampilla)."""
    return M(id, es, en, t_tablas(rampa, base=base, **kw), formas=formas, sonido="WOOD", mapa=mapa, semilla=semilla,
             dureza=2.0, herramienta="axe", juego=juego, madera=madera,
             extra=extra_madera(rampa, base=base, puerta="D" in formas, trampilla="T" in formas))


def VIGA(id, es, en, rampa, mapa, semilla, base=0.55):
    return M(id, es, en, None, formas="C", sonido="WOOD", mapa=mapa, semilla=semilla, dureza=2.0, herramienta="axe",
             lado=t_viga_lado(rampa, base), tapa=t_viga_tapa(rampa, base))


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
    # ---- parte 8c: la madera, los entramados y los tejados
    MADERA("madera_negra", "Madera negra", "Blackwood", "m_negra", "COLOR_BLACK", "DARK_OAK", "DARK_OAK", 201, base=0.58),
    MADERA("madera_roble", "Roble viejo", "Old Oak", "m_roble", "WOOD", "OAK", "OAK", 202),
    MADERA("madera_gris", "Madera gris", "Greywood", "m_gris", "COLOR_GRAY", "SPRUCE", "SPRUCE", 203, nudos=0.6),
    MADERA("madera_ceniza", "Madera de ceniza", "Ashwood", "m_ceniza", "SAND", "BIRCH", "BIRCH", 204, nudos=0.3),
    MADERA("madera_podrida", "Madera podrida", "Rotten Wood", "m_podrida", "COLOR_GREEN", "MANGROVE", "MANGROVE", 205,
           agujeros=0.5, base=0.6),
    MADERA("madera_quemada", "Madera carbonizada", "Charred Wood", "m_quemada", "COLOR_BLACK", "CRIMSON", "CRIMSON", 206,
           cuarteado=1.0, base=0.62, clavos=False, formas="BELVP"),
    MADERA("madera_caoba", "Caoba", "Mahogany", "m_caoba", "COLOR_RED", "JUNGLE", "JUNGLE", 207, base=0.6),
    VIGA("viga_negra", "Viga de madera negra", "Blackwood Beam", "m_negra", "COLOR_BLACK", 211, base=0.58),
    VIGA("viga_roble", "Viga de roble viejo", "Old Oak Beam", "m_roble", "WOOD", 212),
    VIGA("viga_gris", "Viga de madera gris", "Greywood Beam", "m_gris", "COLOR_GRAY", 213),
    VIGA("viga_podrida", "Viga podrida", "Rotten Beam", "m_podrida", "COLOR_GREEN", 214, base=0.6),
    M("entramado", "Entramado", "Timber Frame", t_entramado("marco"), formas="B", sonido="WOOD", mapa="TERRACOTTA_WHITE",
      semilla=221, herramienta="axe", pesos=[4, 4, 1]),
    M("entramado_cruz", "Entramado en aspa", "Crossed Timber Frame", t_entramado("cruz"), formas="B", sonido="WOOD",
      mapa="TERRACOTTA_WHITE", semilla=222, herramienta="axe",
      pesos=[4, 4, 1]),
    M("entramado_diagonal", "Entramado en diagonal", "Diagonal Timber Frame", t_entramado("diagonal"), formas="B",
      sonido="WOOD", mapa="TERRACOTTA_WHITE", semilla=223, herramienta="axe",
      pesos=[4, 4, 1]),
    M("entramado_contra", "Entramado en contradiagonal", "Counter-Diagonal Timber Frame", t_entramado("contra"), formas="B",
      sonido="WOOD", mapa="TERRACOTTA_WHITE", semilla=224, herramienta="axe",
      pesos=[4, 4, 1]),
    M("entramado_poste", "Entramado con poste", "Posted Timber Frame", t_entramado("poste"), formas="B", sonido="WOOD",
      mapa="TERRACOTTA_WHITE", semilla=225, herramienta="axe",
      pesos=[4, 4, 1]),
    M("entramado_travesano", "Entramado con travesano", "Railed Timber Frame", t_entramado("travesano"), formas="B",
      sonido="WOOD", mapa="TERRACOTTA_WHITE", semilla=226, herramienta="axe",
      pesos=[4, 4, 1]),
    M("enlucido", "Enlucido", "Plaster", t_enlucido, formas="BELM", sonido="STONE", mapa="TERRACOTTA_WHITE", semilla=227,
      pesos=[4, 4, 1]),
    M("tejado_pizarra", "Tejado de pizarra", "Slate Roof", t_pizarra, formas="BEL", sonido="DEEPSLATE_TILES",
      mapa="COLOR_GRAY", semilla=231),
    M("tejado_escamas", "Tejado de escamas", "Scaled Slate Roof", t_escamas, formas="BEL", sonido="DEEPSLATE_TILES",
      mapa="COLOR_BLUE", semilla=232),
    M("tejado_barro", "Tejado de barro", "Clay Tile Roof", t_barro, formas="BEL", sonido="STONE", mapa="TERRACOTTA_RED",
      semilla=233),
    M("tejado_musgo", "Tejado de barro musgoso", "Mossy Clay Tile Roof", t_barro_musgo, formas="BEL", sonido="STONE",
      mapa="COLOR_GREEN", semilla=234),
    M("tejado_tejamanil", "Tejado de tejamanil", "Wooden Shingle Roof", t_tejamanil, formas="BEL", sonido="WOOD",
      mapa="COLOR_GRAY", semilla=235, herramienta="axe"),
]

FORMAS = {"B": "", "E": "_escalera", "L": "_losa", "M": "_muro", "V": "_valla", "P": "_cancela", "D": "_puerta",
          "T": "_trampilla"}
NOMBRE_FORMA_ES = {"B": "{}", "E": "Escalera de {}", "L": "Losa de {}", "M": "Muro de {}", "V": "Valla de {}",
                   "P": "Cancela de {}", "D": "Puerta de {}", "T": "Trampilla de {}"}
NOMBRE_FORMA_EN = {"B": "{}", "E": "{} Stairs", "L": "{} Slab", "M": "{} Wall", "V": "{} Fence", "P": "{} Fence Gate",
                   "D": "{} Door", "T": "{} Trapdoor"}


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
    out = {n: P.imagen(m["receta"](s + 1000 * v, s)) for v, n in enumerate(nombres(m))}
    if m.get("extra"):
        out.update(m["extra"](m))
    return out


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


def estado_valla(id):
    partes = [{"apply": {"model": f"darkcraft:block/{id}_poste"}}]
    for lado, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270)):
        a = {"model": f"darkcraft:block/{id}_lado", "uvlock": True}
        if y:
            a["y"] = y
        partes.append({"when": {lado: "true"}, "apply": a})
    return {"multipart": partes}


def estado_cancela(id):
    giro = {"south": 0, "west": 90, "north": 180, "east": 270}
    v = {}
    for facing, y in giro.items():
        for en_muro in ("false", "true"):
            for abierta in ("false", "true"):
                md = f"darkcraft:block/{id}" + ("_muro" if en_muro == "true" else "") + ("_abierta" if abierta == "true" else "")
                e = {"model": md, "uvlock": True}
                if y:
                    e["y"] = y
                v[f"facing={facing},in_wall={en_muro},open={abierta}"] = e
    return {"variants": v}


def estado_puerta(id):
    giro = {"east": 0, "south": 90, "west": 180, "north": 270}
    v = {}
    for facing, y in giro.items():
        for half, mitad in (("lower", "abajo"), ("upper", "arriba")):
            for hinge, lado in (("left", "izq"), ("right", "der")):
                for abierta in ("false", "true"):
                    md = f"darkcraft:block/{id}_{mitad}_{lado}" + ("_abierta" if abierta == "true" else "")
                    yy = y
                    if abierta == "true":
                        yy = y + (90 if hinge == "left" else 270)
                    yy %= 360
                    e = {"model": md}
                    if yy:
                        e["y"] = yy
                    v[f"facing={facing},half={half},hinge={hinge},open={abierta}"] = e
    return {"variants": v}


def estado_trampilla(id):
    giro = {"north": 0, "east": 90, "south": 180, "west": 270}
    v = {}
    for facing, y in giro.items():
        for half, mitad in (("bottom", "abajo"), ("top", "arriba")):
            for abierta in ("false", "true"):
                if abierta == "true":
                    e = {"model": f"darkcraft:block/{id}_abierta"}
                    if y:
                        e["y"] = y
                else:
                    e = {"model": f"darkcraft:block/{id}_{mitad}"}
                v[f"facing={facing},half={half},open={abierta}"] = e
    return {"variants": v}


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
    pesos = m.get("pesos")
    enteros = [dict({"model": f"darkcraft:block/{n}"}, **({"weight": pesos[i]} if pesos else {})) for i, n in enumerate(nombres(m))]
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
    if "V" in m["formas"]:
        v = bid + "_valla"
        t = {"texture": tex}
        j(os.path.join(mb, v + "_poste.json"), {"parent": "minecraft:block/fence_post", "textures": t})
        j(os.path.join(mb, v + "_lado.json"), {"parent": "minecraft:block/fence_side", "textures": t})
        j(os.path.join(mb, v + "_inventario.json"), {"parent": "minecraft:block/fence_inventory", "textures": t})
        j(os.path.join(bs, v + ".json"), estado_valla(v))
        j(os.path.join(mi, v + ".json"), {"parent": f"darkcraft:block/{v}_inventario"})
        ids.append(v)
    if "P" in m["formas"]:
        c = bid + "_cancela"
        t = {"texture": tex}
        for suf, padre in (("", "template_fence_gate"), ("_abierta", "template_fence_gate_open"),
                           ("_muro", "template_fence_gate_wall"), ("_muro_abierta", "template_fence_gate_wall_open")):
            j(os.path.join(mb, c + suf + ".json"), {"parent": "minecraft:block/" + padre, "textures": t})
        j(os.path.join(bs, c + ".json"), estado_cancela(c))
        j(os.path.join(mi, c + ".json"), {"parent": f"darkcraft:block/{c}"})
        ids.append(c)
    if "D" in m["formas"]:
        d = bid + "_puerta"
        t = {"bottom": tex + "_puerta_abajo", "top": tex + "_puerta_arriba"}
        for mitad, en in (("abajo", "bottom"), ("arriba", "top")):
            for lado, en_l in (("izq", "left"), ("der", "right")):
                for suf in ("", "_abierta"):
                    padre = f"minecraft:block/door_{en}_{en_l}" + ("_open" if suf else "")
                    j(os.path.join(mb, f"{d}_{mitad}_{lado}{suf}.json"), {"parent": padre, "textures": t,
                                                                          "render_type": "minecraft:cutout"})
        j(os.path.join(bs, d + ".json"), estado_puerta(d))
        j(os.path.join(mi, d + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"darkcraft:item/{d}"}})
        ids.append(d)
    if "T" in m["formas"]:
        tr = bid + "_trampilla"
        t = {"texture": tex + "_trampilla"}
        for suf, padre in (("_abajo", "template_trapdoor_bottom"), ("_arriba", "template_trapdoor_top"),
                           ("_abierta", "template_trapdoor_open")):
            j(os.path.join(mb, tr + suf + ".json"), {"parent": "minecraft:block/" + padre, "textures": t,
                                                     "render_type": "minecraft:cutout"})
        j(os.path.join(bs, tr + ".json"), estado_trampilla(tr))
        j(os.path.join(mi, tr + ".json"), {"parent": f"darkcraft:block/{tr}_abajo"})
        ids.append(tr)
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
        if bid.endswith("_puerta"):
            entrada["conditions"] = [{"condition": "minecraft:block_state_property", "block": "darkcraft:" + bid,
                                      "properties": {"half": "lower"}}]
        pool = {"rolls": 1, "bonus_rolls": 0, "entries": [entrada]}
        if not bid.endswith("_losa"):
            pool["conditions"] = [{"condition": "minecraft:survives_explosion"}]
        j(os.path.join(d, bid + ".json"), {"type": "minecraft:block", "pools": [pool], "random_sequence": "darkcraft:blocks/" + bid})


def etiquetas():
    """Las etiquetas de Minecraft que necesitan: los muros se unen entre si y las vallas de madera tambien (`fences` y
    `wooden_fences`); escaleras, losas, puertas y trampillas (las de madera, en las suyas); y con que se rompen."""
    tags = {k: [] for k in ("walls", "stairs", "slabs", "fences", "wooden_fences", "fence_gates", "doors", "wooden_doors",
                            "trapdoors", "wooden_trapdoors", "wooden_stairs", "wooden_slabs", "planks", "pickaxe", "axe")}
    for m in MATERIALES:
        b = "darkcraft:" + m["id"]
        madera = m["herramienta"] == "axe"
        for f in m["formas"]:
            bid = b + ("" if f == "C" else FORMAS[f])
            tags[m["herramienta"]].append(bid)
            if f == "E":
                tags["stairs"].append(bid)
                if madera:
                    tags["wooden_stairs"].append(bid)
            elif f == "L":
                tags["slabs"].append(bid)
                if madera:
                    tags["wooden_slabs"].append(bid)
            elif f == "M":
                tags["walls"].append(bid)
            elif f == "V":
                tags["fences"].append(bid)
                tags["wooden_fences"].append(bid)
            elif f == "P":
                tags["fence_gates"].append(bid)
            elif f == "D":
                tags["doors"].append(bid)
                tags["wooden_doors"].append(bid)
            elif f == "T":
                tags["trapdoors"].append(bid)
                tags["wooden_trapdoors"].append(bid)
            elif f == "B" and "V" in m["formas"]:
                tags["planks"].append(bid)
    t = os.path.join(DATA, "minecraft", "tags", "blocks")
    ti = os.path.join(DATA, "minecraft", "tags", "items")
    for k, v in tags.items():
        if k in ("pickaxe", "axe"):
            j(os.path.join(t, "mineable", k + ".json"), {"replace": False, "values": v})
            continue
        j(os.path.join(t, k + ".json"), {"replace": False, "values": v})
        j(os.path.join(ti, k + ".json"), {"replace": False, "values": v})


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
        filas.append(f'            new Material("{m["id"]}", "{m["formas"]}", SoundType.{m["sonido"]}, MapColor.{m["mapa"]}, {m["dureza"]}F, '
                     f'BlockSetType.{m["juego"]}, WoodType.{m["madera"]}, {"true" if m["herramienta"] == "pickaxe" else "false"})')
    src = """package com.darkcraft.bloques;

import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.MapColor;

/** GENERADO por tools/bloques/bloques.py: no tocar a mano. Los materiales de los bloques nuevos y sus formas. */
public final class CatalogoBloques {
    private CatalogoBloques() {}

    /** Un material: su id, sus formas (B bloque, E escalera, L losa, M muro, C columna, V valla, P cancela, D puerta,
     *  T trampilla), su sonido, su color en el mapa, lo que cuesta romperlo, los sonidos de sus puertas y trampillas
     *  (juego) y de su cancela (madera) y si hace falta un pico para que suelte algo (la piedra; la madera, no). */
    public record Material(String id, String formas, SoundType sonido, MapColor mapa, float dureza, BlockSetType juego,
                           WoodType madera, boolean pico) {}

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


def vista(carpeta, desde=0):
    """Hojas para revisar (de cuatro en cuatro materiales, desde el numero `desde` del catalogo): un muro de 4 x 4 con las
    variantes al azar, el cubo y, si tiene, la puerta entera y la trampilla."""
    os.makedirs(carpeta, exist_ok=True)
    tarjetas = []
    for m in MATERIALES[desde:]:
        tx = texturas(m)
        if "C" in m["formas"]:
            lado, tapa = tx[m["id"] + "_lado"], tx[m["id"] + "_tapa"]
        else:
            lado = tapa = tx[m["id"]]
        rng = np.random.default_rng(7)
        lista = [tx[n] for n in (nombres(m) if "C" not in m["formas"] else [m["id"] + "_lado"])]
        rep = Image.new("RGBA", (256, 256))
        for a in range(4):
            for b in range(4):
                rep.paste(lista[int(rng.integers(0, len(lista)))], (a * 64, b * 64))
        cb = cubo(tapa, lado, s=2)
        extra = []
        if m["id"] + "_puerta_abajo" in tx:
            pu = Image.new("RGBA", (64, 128), (30, 30, 30, 255))
            pu.alpha_composite(tx[m["id"] + "_puerta_arriba"], (0, 0))
            pu.alpha_composite(tx[m["id"] + "_puerta_abajo"], (0, 64))
            extra.append(pu.resize((128, 256), Image.NEAREST))
        if m["id"] + "_trampilla" in tx:
            extra.append(tx[m["id"] + "_trampilla"].resize((128, 128), Image.NEAREST))
        ancho = 256 + 8 + cb.size[0] + sum(e.size[0] + 8 for e in extra)
        t = Image.new("RGB", (ancho, 256), (30, 30, 30))
        t.paste(rep.convert("RGB"), (0, 0))
        t.paste(cb, (264, 0))
        x = 264 + cb.size[0] + 8
        for e in extra:
            t.paste(e.convert("RGB"), (x, 0))
            x += e.size[0] + 8
        tarjetas.append((m["id"], t))
    for i in range(0, len(tarjetas), 4):
        grupo = tarjetas[i:i + 4]
        alto = sum(t.size[1] + 4 for _, t in grupo)
        ancho = max(t.size[0] for _, t in grupo)
        hoja = Image.new("RGB", (ancho, alto), (20, 20, 20))
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
            if nombre.startswith("item/"):
                idir = os.path.join(ASSETS, "textures", "item")
                os.makedirs(idir, exist_ok=True)
                img.save(os.path.join(idir, nombre[5:] + ".png"))
            else:
                img.save(os.path.join(tdir, nombre + ".png"))
        ids += escribir_material(m)
    botin(ids)
    etiquetas()
    textos()
    java()
    print(f"{len(MATERIALES)} materiales, {len(ids)} bloques")
    if "--vista" in args:
        vista(args[args.index("--vista") + 1], int(args[args.index("--desde") + 1]) if "--desde" in args else 0)


if __name__ == "__main__":
    main()
