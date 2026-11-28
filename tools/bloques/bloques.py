"""Los bloques nuevos de Dark Craft (la gran reforma, parte 8b y siguientes): el catalogo, sus texturas de 64 x 64 y todo
lo que necesita el juego (estados, modelos, modelos de objeto, etiquetas, nombres y la lista de Java).

Uso:
    python3 tools/bloques/bloques.py            # escribe texturas, JSON, CatalogoBloques.java y ci/historia/bloques.py
    python3 tools/bloques/bloques.py --vista /tmp/vb   # ademas, hojas para revisar (cada textura repetida y un cubo)

Cada material tiene sus formas: B bloque, E escalera, L losa, M muro, C columna (con lado y tapa; un tronco lleva por
los lados las variantes de su corteza), V valla, P cancela (la puerta de la valla), D puerta, T trampilla; y sin sufijo
en el id: X planta, N capas de nieve, H hielo, R rama (Q ramita, U rama gruesa), O hojas, K racimo de hojas, G musgo que
cuelga, Y liana y A alfombra (hojarasca).
Los ids: <id>, <id>_escalera, <id>_losa, <id>_muro, <id>_valla, <id>_cancela, <id>_puerta, <id>_trampilla.
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
import terreno as T  # noqa: E402
import arboles as A  # noqa: E402
import metal as Me  # noqa: E402
import fuego as F  # noqa: E402
import muebles as Mu  # noqa: E402
import decoracion as De  # noqa: E402
import agua as Ag  # noqa: E402

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
    # terreno (parte 8d)
    "tierra": ["#130d09", "#1c140e", "#261b13", "#302218", "#3a2a1e", "#453224", "#503b2a", "#5c4431", "#684e38", "#755841"],
    "tierra_seca": ["#2c2319", "#3a2f22", "#48392b", "#574535", "#665140", "#755e4b", "#846b57", "#937963", "#a2876f"],
    "barro_t": ["#0e0b08", "#16110c", "#1e1810", "#271f15", "#30261a", "#3a2e20", "#443626", "#4f3f2d", "#a08f78"],
    "turba": ["#0b0a07", "#12100b", "#19160f", "#211d14", "#2a2419", "#332b1e", "#3d3324", "#473b2a"],
    "ceniza": ["#2a2826", "#3a3734", "#4b4743", "#5c5853", "#6e6964", "#807a75", "#928c86", "#a59f99", "#b8b2ab", "#cbc5be"],
    "grava": ["#121110", "#1e1c1a", "#2b2825", "#383430", "#46413c", "#544e48", "#625b54", "#716962", "#80776f", "#90867d"],
    "arena": ["#4a4232", "#5c523e", "#6f634b", "#827458", "#958666", "#a69774", "#b6a783", "#c5b692", "#d3c4a2"],
    "arena_gris": ["#2d2c2a", "#3c3a37", "#4b4945", "#5b5853", "#6b6862", "#7c7872", "#8d8983", "#9e9a94"],
    "arena_negra": ["#08080a", "#0f0f12", "#17171b", "#1f1f24", "#28282e", "#323239", "#3c3c44", "#47474f"],
    "nieve": ["#7c8796", "#8d98a6", "#9fa9b6", "#b1bac5", "#c3cbd4", "#d4dbe2", "#e3e8ee", "#f0f3f6", "#fafcfd"],
    "hielo": ["#3f5f7a", "#4d6f8b", "#5c809c", "#6c91ad", "#7ea2bc", "#91b3ca", "#a6c4d7", "#bbd4e3", "#d1e4ee", "#e8f2f7"],
    "musgo_s": ["#141c0c", "#1c2711", "#243216", "#2d3e1b", "#364a21", "#405727", "#4b642e", "#567136", "#62803f"],
    "h_palida": ["#2a2a1a", "#3a3923", "#4b492d", "#5c5a37", "#6d6a42", "#7e7b4d", "#8f8b59", "#a09c66", "#b1ac74", "#c1bc83"],
    "h_verde": ["#0d1a0c", "#132411", "#1a2f16", "#213a1b", "#294621", "#315227", "#3a5f2e", "#446c35", "#4e793d", "#598745"],
    "h_marchita": ["#1d190e", "#2a2413", "#372f19", "#453a1f", "#534526", "#61512d", "#6f5d35", "#7e6a3e", "#8c7747"],
    "h_pantano": ["#10140b", "#171c0f", "#1f2514", "#272e19", "#30381e", "#3a4224", "#444c2a", "#4f5731", "#5a6238"],
    "roca": ["#1a1918", "#252422", "#312f2c", "#3d3a37", "#4a4642", "#57534e", "#64605a", "#726d67", "#807a74", "#8f8982",
             "#9e9790"],
    "roca_oscura": ["#09090b", "#111114", "#19191d", "#222226", "#2b2b30", "#35353a", "#3f3f45", "#4a4a51", "#56565d"],
    "caliza_n": ["#3c362d", "#4d463b", "#5f564a", "#71675a", "#83786a", "#95897a", "#a69a8b", "#b7ab9c", "#c7bcad", "#d6ccbe"],
    "pizarra_n": ["#0e1012", "#16191c", "#1f2226", "#282c31", "#32363c", "#3c4148", "#474c54", "#525861", "#5e646e"],
    "arenisca_n": ["#2f170e", "#3f2014", "#50291a", "#623321", "#743d28", "#86482f", "#985437", "#a96040", "#b96d4a", "#c87b55"],
    "toba": ["#1e1a17", "#2b2622", "#38322d", "#463f39", "#544c45", "#635a52", "#72685f", "#81776d"],
    # plantas
    "p_palida": ["#3a3620", "#545030", "#6e6941", "#888252", "#a29b64", "#bab278", "#cfc78d"],
    "p_verde": ["#13240f", "#1d3416", "#28451e", "#345627", "#406830", "#4d7a3a", "#5a8c45"],
    "p_marchita": ["#2a2010", "#3d2f18", "#504020", "#64502a", "#786134", "#8b723f", "#9e844b"],
    "p_helecho": ["#10200e", "#183015", "#21411d", "#2b5226", "#36642f", "#42763a", "#4f8946"],
    "p_seco": ["#1f1712", "#2e231a", "#3d2f23", "#4d3c2d", "#5e4a38", "#6f5944", "#816950"],
    "f_blanca": ["#8c8a86", "#b4b1ab", "#d6d3cc", "#f0eee8"],
    "f_roja": ["#3e0a0a", "#6b1212", "#981c1a", "#c42e26"],
    "f_azul": ["#1e2a4e", "#2e4274", "#4762a0", "#6a8cc8"],
    "f_centro": ["#6b5a1c", "#a8902e", "#d8c048"],
    "hongo_rojo": ["#2a0907", "#4a120d", "#6b1c14", "#8e281c", "#b03826", "#c95436"],
    "hongo_palido": ["#4e463c", "#6a6052", "#877b6a", "#a49783", "#c0b49e"],
    "pie_hongo": ["#5a5248", "#7a7064", "#9a8f81", "#bab0a1"],
    "junco": ["#1a2410", "#263318", "#334320", "#41542a", "#4f6534", "#5e763f"],
    "espiga": ["#1e120a", "#2e1c10", "#3f2716", "#51331e"],
    # arboles (parte 8e): cortezas, maderas de dentro, hojas
    "c_roble": ["#120e0b", "#1a1510", "#231c16", "#2c241c", "#352c23", "#3f352a", "#4a3e31", "#554839", "#615241", "#6d5c4a",
                "#7a6754"],
    "c_ceniza": ["#2a2826", "#3b3835", "#4e4a46", "#625d58", "#77716b", "#8b857e", "#9e9891", "#b0aaa3", "#c1bbb4", "#d0cbc4",
                 "#ddd8d2"],
    "c_muerto": ["#1c1b1a", "#2a2826", "#393633", "#494541", "#5a5650", "#6b6660", "#7d7770", "#8f8981", "#a19a92", "#b2aba3"],
    "c_pantano": ["#0b0c08", "#12130d", "#191b12", "#212318", "#292b1e", "#323425", "#3b3d2c", "#454634", "#4f503c", "#595a44"],
    "c_sangre": ["#0d0505", "#160808", "#200b0b", "#2a0f0e", "#351312", "#401716", "#4c1c1a", "#58211e", "#652723", "#722d28"],
    "c_pino": ["#140b07", "#1d110b", "#27170f", "#311e14", "#3c2519", "#472c1e", "#533424", "#5f3c2a", "#6b4531", "#784e38",
               "#855840"],
    "m_pino": ["#2e2213", "#3d2e1a", "#4d3a21", "#5d4729", "#6d5432", "#7d613b", "#8d6f45", "#9c7d50", "#ab8b5b", "#ba9a67"],
    "o_roble": ["#0a1407", "#0f1c0b", "#15250f", "#1b2f13", "#223918", "#29431d", "#314e23", "#39592a", "#426431", "#4c7039",
                "#567c42"],
    "o_otono": ["#1f0b04", "#2e1206", "#3e1a08", "#4f230b", "#602d0e", "#713812", "#824417", "#93501d", "#a35d24", "#b26b2d",
                "#c07936"],
    "o_ceniza": ["#2a2a18", "#383820", "#474629", "#575532", "#67643c", "#777447", "#878352", "#97925e", "#a6a16b", "#b5b079",
                 "#c3be88"],
    "o_pantano": ["#0b0f07", "#10150a", "#161c0e", "#1c2312", "#232b16", "#2a331b", "#323b20", "#3a4426", "#434d2c", "#4c5633"],
    "o_sangre": ["#1a0405", "#280607", "#37090a", "#470c0d", "#580f10", "#691314", "#7a1819", "#8b1e1e", "#9c2524", "#ad2d2b"],
    "o_pino": ["#060d0b", "#0a1410", "#0e1b16", "#13231c", "#182b22", "#1e3329", "#243c30", "#2b4537", "#334e3f", "#3b5847"],
    "colgante": ["#1b2112", "#262e19", "#323b21", "#3e4829", "#4a5532", "#57623c", "#647046", "#717e51"],
    "barba": ["#2b2d29", "#3b3e39", "#4c4f49", "#5d605a", "#6f726b", "#81847d", "#93968f"],
    "t_liana": ["#141a0c", "#1d2611", "#263217", "#30401d"],
    "t_liana_seca": ["#1a120c", "#261b12", "#332419", "#402e20"],
    "pinocha": ["#2a160b", "#3c2010", "#4f2b16", "#62371c", "#754423", "#88512b"],
    # metal y vidrio (parte 8f)
    "hierro_n": ["#08090b", "#101216", "#181b20", "#21252b", "#2b3037", "#363b43", "#424851", "#4f565f", "#5e656e", "#6e757e",
                 "#80878f"],
    "oxido": ["#2a1206", "#3e1a08", "#55230b", "#6c2e0f", "#843a14", "#9a4719", "#ad5620", "#bd6629"],
    "oro": ["#2b1d08", "#40290b", "#57380f", "#6e4913", "#875b18", "#a06f1e", "#b98526", "#cf9b30", "#e2b13f", "#f0c652",
            "#f8db74"],
    "bronce": ["#180f09", "#24180e", "#322113", "#412b18", "#50361e", "#604124", "#704d2b", "#815a33", "#92673c"],
    "verdin_b": ["#2d4a3e", "#3a5e4f", "#4a7362", "#5b8875", "#6d9d89", "#82b29d", "#98c5b1"],
    "v_rojo": ["#3a0608", "#560a0c", "#741012", "#921818", "#ad2420", "#c4352a", "#d64b37"],
    "v_oro": ["#5a3006", "#7d460a", "#a05f10", "#c27a18", "#dc9626", "#eeb33c"],
    "v_blanco": ["#8a8f86", "#a8aca2", "#c4c7bd", "#dcdfd6"],
    "v_azul": ["#06103a", "#0a1a58", "#102778", "#183696", "#2447b0", "#355ac6", "#4a70d8"],
    "v_verde": ["#06280e", "#0a3a16", "#104f1f", "#18652a", "#227b36", "#2f9244", "#40a855"],
    "v_violeta": ["#22063a", "#320a54", "#441070", "#58188c", "#6c24a6", "#8135bc", "#9649ce"],
    # el fuego de siete colores (parte 8g) y las velas
    "f_natural": ["#3a0a02", "#6a1504", "#9a2606", "#c73d08", "#e85c0c", "#f8821a", "#ffa832", "#ffc957", "#ffe48a", "#fff6cf"],
    "f_almas": ["#06123a", "#0a2466", "#123a94", "#1d58bd", "#2f7ad8", "#4a9de8", "#6fbff2", "#9adcf8", "#c8f0fc", "#f0fcff"],
    "f_veneno": ["#0b2a06", "#14460a", "#1f6410", "#2c8418", "#3ca322", "#55c032", "#7ad84c", "#a6ec76", "#d2f8b0", "#f4fee8"],
    "f_arcano": ["#22063a", "#3a0a62", "#55128c", "#7420b4", "#9334d2", "#ae52e6", "#c678f2", "#dca2f8", "#eecbfc", "#faf0ff"],
    "f_sangre": ["#2a0204", "#4c0408", "#70080e", "#960e16", "#b81a1e", "#d42e26", "#e84a36", "#f47456", "#fca48a", "#ffd8cc"],
    "f_sagrado": ["#4a2a04", "#7a4806", "#a8680a", "#cf8a10", "#eaaa1e", "#f8c63a", "#ffdc62", "#ffec96", "#fff6c8", "#fffcf0"],
    "f_escarcha": ["#1a2a3a", "#2a4458", "#3e6078", "#58809a", "#78a2b8", "#9cc2d2", "#bedbe6", "#dcedf3", "#f0f8fb", "#ffffff"],
    "cera_blanca": ["#6e6656", "#8a8270", "#a69e8a", "#c0b9a4", "#d6d0bc", "#e8e3d2", "#f4f1e6"],
    "cera_negra": ["#08080a", "#111114", "#1a1a1f", "#24242a", "#2f2f36", "#3b3b43", "#484851"],
    "cera_roja": ["#2a0606", "#460a0a", "#621010", "#7e1816", "#98221e", "#b02e28", "#c43c34"],
    "trapo": ["#0a0806", "#14100b", "#1e1810", "#2a2116", "#372b1d", "#453624"],
    # telas y hueso (parte 8i)
    "t_rojo": ["#2a0606", "#460a0a", "#621010", "#7e1816", "#98221e", "#b02e28", "#c43c34"],
    "t_negro": ["#08080a", "#111114", "#1a1a1f", "#24242a", "#2f2f36", "#3b3b43"],
    "t_azul": ["#06103a", "#0a1a58", "#102778", "#183696", "#2447b0", "#355ac6"],
    "t_verde": ["#0a2214", "#12341e", "#1c4a2a", "#286238", "#347a46"],
    "t_gris": ["#2b2d29", "#3b3e39", "#4c4f49", "#5d605a", "#6f726b"],
    "hueso": ["#3e3528", "#5a4e3c", "#776a52", "#958769", "#b0a280", "#c8bb98", "#ddd2b2"],
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


# ------------------------------------------------------------------ el terreno (parte 8d)

def t_suelo(funcion, rampa, tramado=0.6, **kw):
    return lambda s, b: RAMPAS[rampa].pintar(funcion(s, **kw).v, tramado)


def t_arena(rampa):
    return lambda s, b: RAMPAS[rampa].pintar(T.arena(s, borde=b).v, 0.6)


def t_grava(s, b):
    return RAMPAS["grava"].pintar(T.grava(s, borde=b).v, 0.6)


def t_ceniza(s, b):
    L = T.ceniza(s)
    rgb = RAMPAS["ceniza"].pintar(L.v, 0.6)
    brasa = np.array([150.0, 52.0, 20.0])
    return np.where(L.brasas[..., None], brasa, rgb)


def t_hielo(s, b):
    L = T.hielo(s)
    rgb = RAMPAS["hielo"].pintar(L.v, 0.4)
    return np.dstack([rgb, L.alfa])


def t_hierba(rampa, base=0.42, tierra="tierra", nevada=False):
    """Las dos caras de un bloque de hierba: arriba (briznas o nieve) y el lado (tierra con el fleco)."""
    def f(s, b):
        if nevada:
            arriba = RAMPAS["nieve"].pintar(T.nieve(s).v, 0.6)
        else:
            arriba = RAMPAS[rampa].pintar(T.hierba_arriba(s, base=base).v, 0.5)
        tl = T.tierra(s + 3)
        G, mascara = T.hierba_lado(s, b, tl, base=0.62 if nevada else 0.55)
        lado_t = RAMPAS[tierra].pintar(tl.v, 0.6)
        lado_h = RAMPAS["nieve" if nevada else rampa].pintar(G.v, 0.5)
        lado = np.where(mascara[..., None], lado_h, lado_t)
        return {"arriba": arriba, "lado": lado}
    return f


def t_roca(rampa, **kw):
    return lambda s, b: con_junta(T.roca(s, borde=b, **kw), rampa)


def t_roca_musgosa(s, b):
    L = T.roca(s, borde=b, base=0.5)
    rgb = con_junta(L, "roca")
    m = np.clip((0.65 * P.ruido(b + 81, base=4, octavas=4) + 0.35 * P.ruido(s + 82, base=16, octavas=2) - 0.5) * 6, 0, 1)
    return capa_bultos(rgb, "musgo", m, s, base=0.42)


def t_pizarra_n(s, b):
    return RAMPAS["pizarra_n"].pintar(T.pizarra_natural(s, borde=b).v, 0.6)


def t_pedregal(s, b):
    L = P.cantos(s, cuantos=18, junta=1.2, redondez=5.0, varia=0.12, base=0.5, borde=b)
    return con_junta(L, "roca", "tierra")


def t_planta(funcion, *rampas, **kw):
    """Una planta (con transparencia): rgba de 64 x 64."""
    return lambda s, b: funcion(s, *[RAMPAS[r] for r in rampas], **kw)


# ------------------------------------------------------------------ los arboles (parte 8e)

def t_corteza(tipo, rampa, base=0.5, musgo=0.0):
    """Una corteza (las variantes casan entre si); `musgo`: cuanto musgo lleva (mas en los surcos y abajo a la
    izquierda), tambien con lo comun en los bordes."""
    def f(s, b):
        L = A.corteza(s, tipo, base=base, borde=b)
        rgb = RAMPAS[rampa].pintar(L.v, 0.6)
        if musgo:
            # como capa_bultos, pero con el ruido comun en los bordes (asi casan las variantes; 3 pixeles: el borde de
            # arriba y de abajo de cada mancha mira al pixel de al lado)
            w = A._peso(m0=3.0, m1=8.0)
            r = P.ruido(b + 61, base=4, octavas=4) * (1 - w) + P.ruido(s + 61, base=4, octavas=4) * w
            m = P.dura(np.clip((r + 0.25 * L.surco - (1 - musgo)) * 5, 0, 1), 0.8)
            bulto = P.ruido(b + 31, base=32, octavas=1) * (1 - w) + P.ruido(s + 31, base=32, octavas=1) * w
            arriba = m & ~np.roll(m, 1, axis=0)
            abajo = m & ~np.roll(m, -1, axis=0)
            luz = 0.40 + 0.34 * (bulto - 0.5) + 0.22 * arriba - 0.20 * abajo
            encima = RAMPAS["musgo"].pintar(np.clip(luz, 0, 1), 0.5)
            bajo = np.roll(m, 1, axis=0) & ~m
            rgb = np.where(bajo[..., None], rgb * 0.82, rgb)
            rgb = np.where(m[..., None], encima, rgb)
        return rgb
    return f


def t_testa(madera, corteza, base=0.55):
    def f(s):
        L = A.testa(s, base=base)
        rgb = RAMPAS[madera].pintar(L.v, 0.6)
        return np.where(L.corteza[..., None], RAMPAS[corteza].pintar(L.v, 0.6), rgb)
    return f


def t_hojas(rampa, **kw):
    return lambda s, b: A.hojas(s, RAMPAS[rampa], borde=b, **_con_nieve(kw))


def _con_nieve(kw):
    kw = dict(kw)
    if kw.pop("nevada", False):
        kw["nieve"] = RAMPAS["nieve"]
    return kw


def t_colgante(rampa):
    return lambda s, b: {"": A.colgante(s, RAMPAS[rampa]), "punta": A.colgante(s, RAMPAS[rampa], punta=True)}


def t_liana(tallo, hoja, flor=None):
    return lambda s, b: A.liana(s, RAMPAS[tallo], RAMPAS[hoja], RAMPAS[flor] if flor else None)


def t_hojarasca(*rampas, agujas=False):
    return lambda s, b: A.hojarasca(s, [RAMPAS[r] for r in rampas], agujas=agujas,
                                    cuantas=260 if agujas else 150)


# ------------------------------------------------------------------ el metal y el vidrio (parte 8f)

def t_planchas(rampa, cols=2, filas=2, base=0.5, remaches=True, capa=None, cuanto=0.45, oro=False):
    """Planchas de metal; `capa`: la rampa del oxido o del verdin que lleva encima (manchas, juntas y chorretones);
    `oro`: el oro gastado (deslustrado en los huecos del ruido)."""
    def f(s, b):
        L = Me.planchas(s, cols, filas, base=base, remaches=remaches)
        v = L.v - (0.12 * (P.ruido(s + 9, base=4, octavas=3) < 0.35) if oro else 0)
        rgb = RAMPAS[rampa].pintar(v, 0.6)
        if capa:
            m = Me.oxido(L, s, cuanto)
            encima = RAMPAS[capa].pintar(np.clip(0.3 + 0.5 * m + 0.2 * (L.v - 0.5), 0, 1), 0.6)
            rgb = np.where(P.dura(m, 0.8)[..., None], encima, rgb)
        return rgb
    return f


def t_labrado(rampa):
    return lambda s, b: RAMPAS[rampa].pintar(Me.labrado(s), 0.5)


def t_reja(rampa, **kw):
    kw = dict(kw)
    if "oxido_rampa" in kw:
        kw["oxido_rampa"] = RAMPAS[kw["oxido_rampa"]]
    return lambda s, b: Me.barrotes(s, RAMPAS[rampa], **kw)


def t_cadena(rampa):
    return lambda s, b: {"": Me.cadena(s, RAMPAS[rampa]), "item/": Me.cadena_objeto(s, RAMPAS[rampa])}


def t_vidrio(color, alfa, tipo, borde):
    """Vidrio: la textura y la del canto del panel (`borde`: (r, g, b, a))."""
    def f(s, b):
        canto = np.zeros((64, 64, 4))
        canto[:] = borde
        canto[..., :3] *= (0.9 + 0.2 * P.ruido(s + 5, base=16, octavas=1))[..., None]
        return {"": Me.vidrio(s, color, alfa, tipo), "borde": canto}
    return f


def t_vidriera(principal, acento, claro):
    def f(s, b):
        canto = np.zeros((64, 64, 4))
        canto[:] = (34, 33, 36, 255)
        return {"": Me.vidriera(s, RAMPAS[principal], RAMPAS[acento], RAMPAS[claro]), "borde": canto}
    return f


# ------------------------------------------------------------------ las luces y el fuego (parte 8g)

COLORES = ("natural", "almas", "veneno", "arcano", "sangre", "sagrado", "escarcha")
ANIM = {}   # texturas animadas: nombre -> ticks por fotograma (se escribe su .mcmeta)


def _fuego_id(base, color):
    return base if color == "natural" else f"{base}_{color}"


def sueltas():
    """Las texturas que no son de un material (las comparten varios): las llamitas, las brasas, la cera, el palo y el
    trapo de las antorchas, y los dibujos de las particulas (llama, chispa y humo; en textures/particle)."""
    out = {}
    for c in ("natural", "almas"):
        out[f"llamita_{c}"] = F.llamita(700 + COLORES.index(c), RAMPAS["f_" + c])
        ANIM[f"llamita_{c}"] = 2
    for i, c in enumerate(COLORES):
        out[f"brasas_{c}"] = F.brasas(720 + i, RAMPAS["f_" + c])
        for k in range(4):
            out[f"particle/llama_{c}_{k}"] = F.sprite_llama(740 + i, RAMPAS["f_" + c], k)
        for k in range(2):
            out[f"particle/chispa_{c}_{k}"] = F.sprite_chispa(RAMPAS["f_" + c], k)
    for k in range(4):
        out[f"particle/humo_{k}"] = F.sprite_humo(760, k)
    for cera in ("blanca", "negra", "roja"):
        out[f"cera_{cera}"] = F.cera(770 + len(cera), RAMPAS["cera_" + cera])
    out["antorcha_palo"] = F.palo(780, RAMPAS["m_roble"])
    out["antorcha_trapo"] = F.trapo(781, RAMPAS["trapo"])
    return out


def particulas_json():
    """Los ficheros de las particulas (assets/darkcraft/particles): que dibujos usa cada una."""
    d = os.path.join(ASSETS, "particles")
    for c in COLORES:
        j(os.path.join(d, f"llama_{c}.json"), {"textures": [f"darkcraft:llama_{c}_{k}" for k in range(4)]})
        j(os.path.join(d, f"chispa_{c}.json"), {"textures": [f"darkcraft:chispa_{c}_{k}" for k in range(2)]})
    j(os.path.join(d, "humo.json"), {"textures": [f"darkcraft:humo_{k}" for k in range(4)]})


def FUEGO(color, es, en, mapa, semilla):
    """El fuego de un color: el bloque de fuego (en el suelo, como el del juego: cuatro planos que se inclinan hacia
    dentro), con dos animaciones al azar."""
    return M(_fuego_id("fuego", color), es, en, None, formas="f", sonido="WOOL", mapa=mapa, semilla=semilla, dureza=0.0,
             variantes=1, color=color)


def BRASERO(color, es, en, mapa, semilla):
    return M(_fuego_id("brasero", color), es, en, None, formas="s", sonido="METAL", mapa=mapa, semilla=semilla, dureza=3.0,
             variantes=1, color=color)


def VELA(id, es, en, cera, semilla):
    return M(id, es, en, None, formas="v", sonido="CANDLE", mapa="SAND", semilla=semilla, dureza=0.1, herramienta="hoe",
             variantes=1, cera=cera, color="natural")


def CANDELABRO(id, es, en, forma, metal, cadena, mapa, semilla):
    """Un candelabro: `c` de pie (tres velas), `p` de pared (una) o `a` arana (colgada, ocho velas y cadenas)."""
    return M(id, es, en, None, formas=forma, sonido="METAL", mapa=mapa, semilla=semilla, dureza=2.0, variantes=1,
             metal=metal, cadena=cadena, color="natural")


def FAROL(id, es, en, color, semilla):
    return M(id, es, en, None, formas="l", sonido="LANTERN", mapa="METAL", semilla=semilla, dureza=2.0, variantes=1,
             color=color)


def ANTORCHA(id, es, en, color, semilla):
    """Una antorcha de pie y de pared (`<id>_pared`, sin objeto: la pone el objeto de la antorcha contra una pared)."""
    return M(id, es, en, None, formas="t", sonido="WOOD", mapa="WOOD", semilla=semilla, dureza=0.0, herramienta="axe",
             variantes=1, color=color)


# ------------------------------------------------------------------ los muebles (parte 8h)

MADERAS_MUEBLE = {"roble": ("madera_roble", "m_roble"), "negra": ("madera_negra", "m_negra"), "caoba": ("madera_caoba", "m_caoba")}


def sueltas_muebles():
    """Las texturas de los muebles que no salen de un material: las estanterias (tres de cada madera), los frentes de las
    alacenas, la paja y el lino del jergon y las tapas y hojas de los libros."""
    out = {}
    for i, (k, (tex, rp)) in enumerate(MADERAS_MUEBLE.items()):
        for v in range(3):
            out[f"estanteria_{k}" + ("" if v == 0 else f"_{v + 1}")] = Mu.libros(800 + 10 * i + v, RAMPAS[rp])
        out[f"alacena_{k}"] = Mu.alacena(830 + i, RAMPAS[rp], RAMPAS["hierro_n"])
    out["estanteria_vacia"] = Mu.libros(840, RAMPAS["m_roble"], vacia=True)
    out["estanteria_polvo"] = Mu.libros(841, RAMPAS["m_gris"], polvo=True)
    out["paja"] = Mu.paja(842)
    out["lino"] = Mu.lino(843, RAMPAS["cera_blanca"])
    out["libro_tapa"] = Mu.libro_tapa(844)
    out["libro_hojas"] = Mu.libro_hojas(845)
    return out


def MUEBLE(id, es, en, forma, madera, semilla, mapa="WOOD"):
    """Un mueble de una madera (`roble`, `negra`, `caoba`): m mesa, h silla, b banco, k taburete, o alacena, e
    estanteria; o de los de una sola: j jergon, i pila de libros, r atril."""
    return M(id, es, en, None, formas=forma, sonido="WOOD", mapa=mapa, semilla=semilla, dureza=2.0, herramienta="axe",
             variantes=1, madera_mueble=madera)


# ------------------------------------------------------------------ la decoracion (parte 8i)

ESTANDARTES = {"valdrenn": ("t_rojo", "oro", "oro", "corona", False), "sombra": ("t_negro", "t_rojo", "t_rojo", "ojo", False),
               "almas": ("t_azul", "v_blanco", "v_blanco", "llama", False), "hueso": ("hueso", "t_negro", "t_negro", "calavera", False),
               "roto": ("t_gris", "t_negro", "t_negro", "corona", True)}
TAPICES = {"rojo": ("t_rojo", "oro"), "verde": ("t_verde", "oro"), "azul": ("t_azul", "v_blanco"), "negro": ("t_negro", "t_rojo")}
ALFOMBRAS = {"roja": ("t_rojo", "t_azul", "oro"), "azul": ("t_azul", "t_rojo", "v_blanco"), "raida": ("t_gris", "t_rojo", "hueso")}


def sueltas_decoracion():
    """Las texturas de la decoracion: los panos de los estandartes (mitad de arriba y de abajo), los tapices, las alfombras,
    el hueso, la cara de la calavera, la telarana y la lapida."""
    out = {}
    for i, (k, (campo, cen, emb, tipo, roto)) in enumerate(ESTANDARTES.items()):
        a, b = De.estandarte(900 + i, RAMPAS[campo], RAMPAS[cen], RAMPAS[emb], tipo, roto)
        out[f"estandarte_{k}_arriba"], out[f"estandarte_{k}_abajo"] = a, b
    for i, (k, (fondo, dib)) in enumerate(TAPICES.items()):
        out[f"tapiz_{k}"] = De.tapiz(910 + i, RAMPAS[fondo], RAMPAS[dib])
    for i, (k, (fondo, dib, rib)) in enumerate(ALFOMBRAS.items()):
        out[f"alfombra_{k}"] = De.alfombra(920 + i, RAMPAS[fondo], RAMPAS[dib], RAMPAS[rib])
    out["hueso"] = De.hueso(930, RAMPAS["hueso"])
    out["calavera_cara"] = De.calavera_cara(931, RAMPAS["hueso"])
    out["telarana"] = De.telarana(932)
    out["lapida"] = De.lapida(933, RAMPAS["granito"], RAMPAS["liquen"])
    return out


def DECO(id, es, en, forma, semilla, mapa="STONE", sonido="STONE", herramienta="pickaxe", dureza=1.5, **kw):
    """Una pieza de decoracion: d estandarte, g tapiz, n calavera, q monton de huesos, u telarana, w lapida, x cruz de
    piedra, y ataud, z gargola, S busto y F jaula colgante."""
    return M(id, es, en, None, formas=forma, sonido=sonido, mapa=mapa, semilla=semilla, dureza=dureza, herramienta=herramienta,
             variantes=1, **kw)


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


def SUELO(id, es, en, receta, mapa, sonido, semilla, herramienta="shovel", girar=True, **kw):
    """Un suelo: bloque y losa (para suavizar el terreno), cuatro variantes (giradas al azar, si no tiene nada que siga de
    un bloque al otro)."""
    return M(id, es, en, receta, formas="BL", sonido=sonido, mapa=mapa, semilla=semilla, dureza=0.6, herramienta=herramienta,
             variantes=4, girar=girar, **kw)


def HIERBA(id, es, en, receta, mapa, semilla, sonido="GRASS"):
    """Un bloque de hierba: arriba, lado (con el fleco) y abajo la tierra; bloque y losa."""
    return M(id, es, en, receta, formas="BL", sonido=sonido, mapa=mapa, semilla=semilla, dureza=0.6, herramienta="shovel",
             variantes=4, girar=True, caras=True, abajo="tierra" if id != "hierba_pantano" else "turba",
             etiquetas=("dirt",))


def ROCA(id, es, en, receta, mapa, sonido, semilla, dureza=1.5):
    """Roca natural: bloque, escalera, losa y muro; cuatro variantes que casan entre si (no se giran: los estratos y las
    caras siguen de un bloque al otro)."""
    return M(id, es, en, receta, formas="BELM", sonido=sonido, mapa=mapa, semilla=semilla, dureza=dureza, variantes=4)


def PLANTA(id, es, en, receta, semilla, mapa="PLANT", sonido="GRASS"):
    """Una planta en cruz (con transparencia), sin choque."""
    return M(id, es, en, receta, formas="X", sonido=sonido, mapa=mapa, semilla=semilla, dureza=0.0, herramienta="hoe",
             variantes=1)


def CORTEZA(id, es, en, tipo, rampa, mapa, semilla, base=0.5, musgo=0.0):
    """La corteza de un arbol: bloque (corteza por todas las caras: los troncos gordos se hacen de piezas), escalera, losa y
    muro (para redondearlos, raices y ramas gruesas); tres variantes que casan entre si (no se giran: la veta va de arriba
    abajo)."""
    return M(id, es, en, t_corteza(tipo, rampa, base, musgo), formas="BELM", sonido="WOOD", mapa=mapa, semilla=semilla,
             dureza=2.0, herramienta="axe", etiquetas=("logs",))


def TRONCO(id, es, en, corteza, madera, rampa_c, mapa, semilla, base=0.55):
    """El tronco (con eje): por los lados la corteza (las variantes de `corteza`, al azar) y la testa arriba y abajo."""
    return M(id, es, en, None, formas="C", sonido="WOOD", mapa=mapa, semilla=semilla, dureza=2.0, herramienta="axe",
             lado_de=corteza, tapa=t_testa(madera, rampa_c, base), etiquetas=("logs",))


GRUESOS = {"Q": 2, "R": 4, "U": 6}   # medio grueso (en dieciseisavos) de cada forma de rama: 4, 8 y 12 pixeles


def RAMA(id, es, en, corteza, mapa, semilla, forma="R"):
    """Una rama: un palo (Q ramita de 4 pixeles, R rama de 8, U rama gruesa de 12) que se une a lo que tiene alrededor por
    sus seis caras, con la corteza de su arbol (la veta va a lo largo de cada brazo) y la testa en las puntas."""
    return M(id, es, en, None, formas=forma, sonido="WOOD", mapa=mapa, semilla=semilla, dureza=1.5, herramienta="axe",
             lado_de=corteza, etiquetas=("logs",))


def RACIMO(id, es, en, hojas, mapa, semilla, sonido="AZALEA_LEAVES"):
    """Un racimo de hojas: tres o cuatro bultos de hojas de distinto tamano (no un cubo) con la textura de sus hojas, para
    las puntas de las ramas y el borde de las copas."""
    return M(id, es, en, None, formas="K", sonido=sonido, mapa=mapa, semilla=semilla, dureza=0.2, herramienta="hoe",
             lado_de=hojas, etiquetas=("leaves",))


def HOJAS(id, es, en, receta, mapa, semilla, sonido="AZALEA_LEAVES"):
    """Hojas: un cubo con huecos (se ven las hojas de detras y el cielo); tres variantes. No se caen solas."""
    return M(id, es, en, receta, formas="O", sonido=sonido, mapa=mapa, semilla=semilla, dureza=0.2, herramienta="hoe",
             variantes=3, etiquetas=("leaves",))


def COLGANTE(id, es, en, rampa, mapa, semilla):
    """Musgo que cuelga (de una cara de abajo firme, de hojas, de ramas o de otro igual); el de abajo del todo, la punta."""
    return M(id, es, en, t_colgante(rampa), formas="G", sonido="HANGING_ROOTS", mapa=mapa, semilla=semilla, dureza=0.0,
             herramienta="hoe", variantes=1)


def LIANA(id, es, en, receta, mapa, semilla):
    """Una liana: se pega a las caras de los bloques de al lado (y de arriba), como la enredadera; se trepa."""
    return M(id, es, en, receta, formas="Y", sonido="VINE", mapa=mapa, semilla=semilla, dureza=0.2, herramienta="hoe",
             variantes=1, etiquetas=("climbable",))


def HOJARASCA(id, es, en, receta, mapa, semilla, sonido="AZALEA_LEAVES"):
    """Hojas caidas: una alfombra de un pixel con huecos (se ve el suelo de debajo)."""
    return M(id, es, en, receta, formas="A", sonido=sonido, mapa=mapa, semilla=semilla, dureza=0.1, herramienta="hoe",
             variantes=1)


def METAL(id, es, en, receta, mapa, semilla, sonido="METAL", variantes=3):
    """Un metal: bloque, escalera, losa y muro (con pico)."""
    return M(id, es, en, receta, formas="BELM", sonido=sonido, mapa=mapa, semilla=semilla, dureza=4.0, variantes=variantes)


def REJA(id, es, en, receta, mapa, semilla):
    """Una reja: barrotes que se unen a los de al lado y a las caras firmes (como los barrotes del juego)."""
    return M(id, es, en, receta, formas="I", sonido="METAL", mapa=mapa, semilla=semilla, dureza=5.0, variantes=1)


def CADENA(id, es, en, receta, mapa, semilla):
    """Una cadena (con eje: de pie, o tumbada a lo largo o a lo ancho)."""
    return M(id, es, en, receta, formas="Z", sonido="CHAIN", mapa=mapa, semilla=semilla, dureza=5.0, variantes=1)


def VIDRIO(id, es, en, receta, mapa, semilla):
    """Vidrio: el bloque (translucido; entre dos iguales no se ve la cara de en medio) y su panel."""
    return M(id, es, en, receta, formas="WJ", sonido="GLASS", mapa=mapa, semilla=semilla, dureza=0.3, variantes=1)


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
    M("sillar_verdin", "Sillar con verdín", "Slimed Ashlar", t_sillar_verdin, sonido="MUD_BRICKS", mapa="COLOR_GREEN", semilla=107),
    M("marmol_blanco", "Mármol blanco", "White Marble", t_marmol_blanco, sonido="CALCITE", mapa="QUARTZ", semilla=108),
    M("marmol_negro", "Mármol negro", "Black Marble", t_marmol_negro, sonido="POLISHED_DEEPSLATE", mapa="COLOR_BLACK", semilla=109, dureza=2.0),
    M("sillar_rojo", "Sillar de arenisca roja", "Red Sandstone Ashlar", t_sillar_rojo, sonido="STONE", mapa="COLOR_RED", semilla=110),
    M("ladrillo_viejo", "Ladrillo viejo", "Old Brick", t_ladrillo_viejo, sonido="MUD_BRICKS", mapa="COLOR_RED", semilla=111),
    M("ladrillo_oscuro", "Ladrillo oscuro", "Dark Brick", t_ladrillo_oscuro, sonido="NETHER_BRICKS", mapa="COLOR_BROWN", semilla=112),
    M("adoquin", "Adoquín", "Cobbled Paving", t_adoquin, sonido="STONE", mapa="STONE", semilla=113),
    M("enlosado", "Enlosado", "Flagstones", t_enlosado, sonido="STONE", mapa="STONE", semilla=114),
    M("piedra_tallada", "Piedra tallada", "Carved Stone", t_piedra_tallada, sonido="STONE", mapa="SAND", semilla=115, variantes=1),
    M("mamposteria", "Mampostería", "Rubble Masonry", t_mamposteria, sonido="STONE", mapa="STONE", semilla=116),
    M("columna_gris", "Columna de granito", "Granite Column", None, formas="C", sonido="STONE", mapa="STONE", semilla=121,
      lado=t_col_lado("granito", 0.58), tapa=t_col_tapa("granito", 0.58)),
    M("columna_palida", "Columna de caliza", "Limestone Column", None, formas="C", sonido="STONE", mapa="SAND", semilla=122,
      lado=t_col_lado("caliza", 0.62), tapa=t_col_tapa("caliza", 0.62)),
    M("columna_marmol", "Columna de mármol", "Marble Column", None, formas="C", sonido="CALCITE", mapa="QUARTZ", semilla=123,
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
    M("entramado_travesano", "Entramado con travesaño", "Railed Timber Frame", t_entramado("travesano"), formas="B",
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
    # ---- parte 8d: el terreno
    SUELO("tierra", "Tierra", "Soil", t_suelo(T.tierra, "tierra"), "DIRT", "GRAVEL", 301, etiquetas=("dirt",)),
    SUELO("tierra_seca", "Tierra cuarteada", "Cracked Earth", lambda s, b: RAMPAS["tierra_seca"].pintar(T.tierra_seca(s, borde=b).v, 0.6),
          "DIRT", "GRAVEL", 302, etiquetas=("dirt",), girar=False),
    SUELO("barro", "Barro", "Mud", t_suelo(T.barro, "barro_t"), "COLOR_BROWN", "MUD", 303, etiquetas=("dirt",)),
    SUELO("turba", "Turba", "Peat", t_suelo(T.turba, "turba"), "COLOR_BROWN", "MUD", 304, etiquetas=("dirt",)),
    SUELO("ceniza", "Ceniza", "Ash", t_ceniza, "COLOR_LIGHT_GRAY", "SAND", 305, etiquetas=("sand",)),
    SUELO("grava", "Grava", "Gravel", t_grava, "STONE", "GRAVEL", 306),
    SUELO("arena_palida", "Arena pálida", "Pale Sand", t_arena("arena"), "SAND", "SAND", 307, etiquetas=("sand",), girar=False),
    SUELO("arena_gris", "Arena gris", "Grey Sand", t_arena("arena_gris"), "COLOR_GRAY", "SAND", 308, etiquetas=("sand",),
          girar=False),
    SUELO("arena_negra", "Arena negra", "Black Sand", t_arena("arena_negra"), "COLOR_BLACK", "SAND", 309, etiquetas=("sand",),
          girar=False),
    SUELO("nieve", "Nieve", "Snow", t_suelo(T.nieve, "nieve"), "SNOW", "SNOW", 310, etiquetas=("snow",)),
    SUELO("musgo", "Manto de musgo", "Moss Mat", t_suelo(T.musgo_suelo, "musgo_s"), "COLOR_GREEN", "MOSS", 311,
          herramienta="hoe", etiquetas=("dirt",)),
    M("nieve_capa", "Capa de nieve", "Snow Layer", None, formas="N", sonido="SNOW", mapa="SNOW", semilla=312,
      herramienta="shovel", textura="nieve"),
    M("hielo", "Hielo", "Ice", t_hielo, formas="H", sonido="GLASS", mapa="ICE", semilla=313, dureza=0.5, variantes=2,
      girar=True, etiquetas=("ice",)),
    HIERBA("hierba_palida", "Hierba pálida", "Pale Grass", t_hierba("h_palida", base=0.46), "SAND", 321),
    HIERBA("hierba_verde", "Hierba verde", "Green Grass", t_hierba("h_verde"), "GRASS", 322),
    HIERBA("hierba_marchita", "Hierba marchita", "Withered Grass", t_hierba("h_marchita"), "COLOR_BROWN", 323),
    HIERBA("hierba_pantano", "Hierba de pantano", "Swamp Grass", t_hierba("h_pantano", tierra="turba"), "COLOR_GREEN", 324),
    HIERBA("hierba_nevada", "Hierba nevada", "Snowy Grass", t_hierba("nieve", nevada=True), "SNOW", 325, sonido="SNOW"),
    ROCA("roca_gris", "Roca gris", "Grey Rock", t_roca("roca", base=0.52), "STONE", "STONE", 331),
    ROCA("roca_oscura", "Roca oscura", "Dark Rock", t_roca("roca_oscura", base=0.5, poros=0.4), "COLOR_BLACK", "BASALT", 332,
         dureza=2.0),
    ROCA("roca_caliza", "Roca caliza", "Limestone Rock", t_roca("caliza_n", base=0.58, estratos=0.05), "SAND", "TUFF", 333),
    ROCA("roca_pizarra", "Roca de pizarra", "Slate Rock", t_pizarra_n, "COLOR_GRAY", "DEEPSLATE", 334, dureza=2.0),
    ROCA("roca_arenisca", "Roca arenisca", "Sandstone Rock", t_roca("arenisca_n", base=0.55, estratos=0.06), "COLOR_ORANGE",
         "STONE", 335),
    ROCA("roca_toba", "Toba volcánica", "Volcanic Tuff", t_roca("toba", base=0.5, poros=1.0, grietas=0.2), "COLOR_BROWN",
         "TUFF", 336),
    ROCA("roca_musgosa", "Roca musgosa", "Mossy Rock", t_roca_musgosa, "COLOR_GREEN", "STONE", 337),
    ROCA("pedregal", "Pedregal", "Scree", t_pedregal, "STONE", "STONE", 338),
    PLANTA("hierba_alta_palida", "Hierba alta pálida", "Tall Pale Grass", t_planta(T.hierba_alta, "p_palida"), 341),
    PLANTA("hierba_alta_verde", "Hierba alta verde", "Tall Green Grass", t_planta(T.hierba_alta, "p_verde"), 342),
    PLANTA("hierba_alta_marchita", "Hierba alta marchita", "Tall Withered Grass", t_planta(T.hierba_alta, "p_marchita"), 343),
    PLANTA("helecho", "Helecho", "Fern", t_planta(T.helecho, "p_helecho"), 344),
    PLANTA("matorral_seco", "Matorral seco", "Dry Shrub", t_planta(T.matorral, "p_seco"), 345, mapa="COLOR_BROWN"),
    PLANTA("flores_ceniza", "Flores de ceniza", "Ash Flowers", t_planta(T.flores, "p_palida", "f_blanca", "f_centro"), 346),
    PLANTA("flores_sangre", "Flores de sangre", "Blood Flowers", t_planta(T.flores, "p_marchita", "f_roja", "f_centro"), 347,
           mapa="COLOR_RED"),
    PLANTA("flores_azules", "Flores de escarcha", "Frost Flowers", t_planta(T.flores, "p_verde", "f_azul", "f_centro"), 348,
           mapa="COLOR_BLUE"),
    PLANTA("hongos_rojos", "Hongos rojos", "Red Toadstools", t_planta(T.hongos, "hongo_rojo", "pie_hongo"), 349,
           mapa="COLOR_RED", sonido="FUNGUS"),
    PLANTA("hongos_palidos", "Hongos pálidos", "Pale Toadstools", t_planta(T.hongos, "hongo_palido", "pie_hongo"), 350,
           mapa="SAND", sonido="FUNGUS"),
    PLANTA("juncos", "Juncos", "Reeds", t_planta(T.juncos, "junco", "espiga"), 351),
    # ---- parte 8e: los arboles (cortezas, troncos, ramas, hojas, musgo que cuelga, lianas y hojarasca)
    CORTEZA("corteza_roble", "Corteza de roble", "Oak Bark", "surcos", "c_roble", "COLOR_BROWN", 401, base=0.52),
    TRONCO("tronco_roble", "Tronco de roble", "Oak Log", "corteza_roble", "m_roble", "c_roble", "COLOR_BROWN", 402),
    RAMA("rama_roble", "Rama de roble", "Oak Branch", "corteza_roble", "COLOR_BROWN", 403),
    RAMA("ramita_roble", "Ramita de roble", "Oak Twig", "corteza_roble", "COLOR_BROWN", 406, forma="Q"),
    RAMA("rama_gruesa_roble", "Rama gruesa de roble", "Thick Oak Branch", "corteza_roble", "COLOR_BROWN", 407,
         forma="U"),
    HOJAS("hojas_roble", "Hojas de roble", "Oak Leaves", t_hojas("o_roble"), "PLANT", 404),
    RACIMO("racimo_roble", "Racimo de hojas de roble", "Oak Leaf Cluster", "hojas_roble", "PLANT", 408),
    HOJAS("hojas_otono", "Hojas de roble otoñal", "Autumn Oak Leaves", t_hojas("o_otono"), "COLOR_ORANGE", 405),
    RACIMO("racimo_otono", "Racimo de hojas otoñales", "Autumn Leaf Cluster", "hojas_otono", "COLOR_ORANGE", 409),
    CORTEZA("corteza_ceniza", "Corteza de fresno pálido", "Pale Ash Bark", "lisa", "c_ceniza", "QUARTZ", 411, base=0.62),
    TRONCO("tronco_ceniza", "Tronco de fresno pálido", "Pale Ash Log", "corteza_ceniza", "m_ceniza", "c_ceniza", "QUARTZ", 412),
    RAMA("rama_ceniza", "Rama de fresno pálido", "Pale Ash Branch", "corteza_ceniza", "QUARTZ", 413),
    RAMA("ramita_ceniza", "Ramita de fresno pálido", "Pale Ash Twig", "corteza_ceniza", "QUARTZ", 416, forma="Q"),
    RAMA("rama_gruesa_ceniza", "Rama gruesa de fresno pálido", "Thick Pale Ash Branch", "corteza_ceniza", "QUARTZ", 417,
         forma="U"),
    HOJAS("hojas_ceniza", "Hojas de fresno pálido", "Pale Ash Leaves", t_hojas("o_ceniza"), "SAND", 414),
    RACIMO("racimo_ceniza", "Racimo de hojas de fresno", "Pale Ash Leaf Cluster", "hojas_ceniza", "SAND", 418),
    CORTEZA("corteza_muerto", "Madera muerta", "Deadwood", "seca", "c_muerto", "COLOR_LIGHT_GRAY", 421, base=0.55),
    TRONCO("tronco_muerto", "Tronco muerto", "Dead Log", "corteza_muerto", "m_gris", "c_muerto", "COLOR_LIGHT_GRAY", 422),
    RAMA("rama_muerto", "Rama muerta", "Dead Branch", "corteza_muerto", "COLOR_LIGHT_GRAY", 423),
    RAMA("ramita_muerto", "Ramita muerta", "Dead Twig", "corteza_muerto", "COLOR_LIGHT_GRAY", 426, forma="Q"),
    RAMA("rama_gruesa_muerto", "Rama gruesa muerta", "Thick Dead Branch", "corteza_muerto", "COLOR_LIGHT_GRAY", 427,
         forma="U"),
    CORTEZA("corteza_pantano", "Corteza de ciprés musgoso", "Mossy Cypress Bark", "musgosa", "c_pantano", "COLOR_GREEN", 431,
            base=0.55, musgo=0.42),
    TRONCO("tronco_pantano", "Tronco de ciprés musgoso", "Mossy Cypress Log", "corteza_pantano", "m_podrida", "c_pantano",
           "COLOR_GREEN", 432),
    RAMA("rama_pantano", "Rama de ciprés musgoso", "Mossy Cypress Branch", "corteza_pantano", "COLOR_GREEN", 433),
    RAMA("ramita_pantano", "Ramita de ciprés musgoso", "Mossy Cypress Twig", "corteza_pantano", "COLOR_GREEN", 436,
         forma="Q"),
    RAMA("rama_gruesa_pantano", "Rama gruesa de ciprés musgoso", "Thick Mossy Cypress Branch", "corteza_pantano",
         "COLOR_GREEN", 437, forma="U"),
    HOJAS("hojas_pantano", "Hojas de ciprés musgoso", "Mossy Cypress Leaves", t_hojas("o_pantano"), "COLOR_GREEN", 434),
    RACIMO("racimo_pantano", "Racimo de hojas de ciprés", "Mossy Cypress Leaf Cluster", "hojas_pantano", "COLOR_GREEN",
           438),
    CORTEZA("corteza_sangre", "Corteza de sanguino", "Bloodwood Bark", "surcos", "c_sangre", "COLOR_RED", 441, base=0.55),
    TRONCO("tronco_sangre", "Tronco de sanguino", "Bloodwood Log", "corteza_sangre", "m_caoba", "c_sangre", "COLOR_RED", 442),
    RAMA("rama_sangre", "Rama de sanguino", "Bloodwood Branch", "corteza_sangre", "COLOR_RED", 443),
    RAMA("ramita_sangre", "Ramita de sanguino", "Bloodwood Twig", "corteza_sangre", "COLOR_RED", 446, forma="Q"),
    RAMA("rama_gruesa_sangre", "Rama gruesa de sanguino", "Thick Bloodwood Branch", "corteza_sangre", "COLOR_RED", 447,
         forma="U"),
    HOJAS("hojas_sangre", "Hojas de sanguino", "Bloodwood Leaves", t_hojas("o_sangre"), "COLOR_RED", 444),
    RACIMO("racimo_sangre", "Racimo de hojas de sanguino", "Bloodwood Leaf Cluster", "hojas_sangre", "COLOR_RED", 448),
    CORTEZA("corteza_pino", "Corteza de pino negro", "Black Pine Bark", "placas", "c_pino", "PODZOL", 451, base=0.55),
    TRONCO("tronco_pino", "Tronco de pino negro", "Black Pine Log", "corteza_pino", "m_pino", "c_pino", "PODZOL", 452),
    RAMA("rama_pino", "Rama de pino negro", "Black Pine Branch", "corteza_pino", "PODZOL", 453),
    RAMA("ramita_pino", "Ramita de pino negro", "Black Pine Twig", "corteza_pino", "PODZOL", 456, forma="Q"),
    RAMA("rama_gruesa_pino", "Rama gruesa de pino negro", "Thick Black Pine Branch", "corteza_pino", "PODZOL", 457,
         forma="U"),
    HOJAS("hojas_pino", "Agujas de pino negro", "Black Pine Needles", t_hojas("o_pino", agujas=True, huecos=0.40, cuantas=300),
          "COLOR_GREEN", 454, sonido="GRASS"),
    RACIMO("racimo_pino", "Racimo de agujas de pino", "Black Pine Needle Cluster", "hojas_pino", "COLOR_GREEN", 458,
           sonido="GRASS"),
    HOJAS("hojas_pino_nevado", "Agujas nevadas", "Snowy Pine Needles",
          t_hojas("o_pino", agujas=True, huecos=0.40, cuantas=300, nevada=True), "SNOW", 455, sonido="GRASS"),
    RACIMO("racimo_pino_nevado", "Racimo de agujas nevadas", "Snowy Pine Needle Cluster", "hojas_pino_nevado", "SNOW", 459,
           sonido="GRASS"),
    COLGANTE("musgo_colgante", "Musgo colgante", "Hanging Moss", "colgante", "COLOR_GREEN", 461),
    COLGANTE("barba_gris", "Barba de viejo", "Old Man's Beard", "barba", "COLOR_LIGHT_GRAY", 462),
    LIANA("liana", "Liana", "Liana", t_liana("t_liana", "p_verde"), "PLANT", 463),
    LIANA("liana_seca", "Liana seca", "Dry Liana", t_liana("t_liana_seca", "p_marchita"), "COLOR_BROWN", 464),
    HOJARASCA("hojarasca_roble", "Hojarasca de roble", "Oak Leaf Litter", t_hojarasca("o_otono", "p_marchita", "p_seco"),
              "COLOR_ORANGE", 471),
    HOJARASCA("hojarasca_ceniza", "Hojarasca de fresno", "Pale Ash Leaf Litter", t_hojarasca("o_ceniza", "p_palida"),
              "SAND", 472),
    HOJARASCA("hojarasca_sangre", "Hojarasca de sanguino", "Bloodwood Leaf Litter", t_hojarasca("o_sangre", "p_marchita"),
              "COLOR_RED", 473),
    HOJARASCA("pinocha", "Pinocha", "Pine Needle Litter", t_hojarasca("pinocha", agujas=True), "PODZOL", 474, sonido="GRASS"),
    # ---- parte 8f: el metal y el vidrio
    # (no "hierro_negro": ese id ya es el bloque del marco del portal de la fortaleza, parte 5e-1)
    METAL("planchas_hierro", "Planchas de hierro negro", "Black Iron Plates", t_planchas("hierro_n"), "COLOR_BLACK", 501),
    METAL("hierro_remachado", "Hierro remachado", "Riveted Iron", t_planchas("hierro_n", cols=1, filas=2), "COLOR_BLACK", 502),
    METAL("hierro_oxidado", "Hierro oxidado", "Rusted Iron", t_planchas("hierro_n", base=0.48, capa="oxido"),
          "TERRACOTTA_ORANGE", 503),
    METAL("oro_gastado", "Oro gastado", "Worn Gold", t_planchas("oro", base=0.55, remaches=False, oro=True), "GOLD", 504),
    METAL("oro_labrado", "Oro labrado", "Engraved Gold", t_labrado("oro"), "GOLD", 505, variantes=1),
    METAL("bronce", "Bronce con verdín", "Verdigris Bronze", t_planchas("bronce", capa="verdin_b", cuanto=0.38),
          "TERRACOTTA_CYAN", 506, sonido="COPPER"),
    REJA("barrotes", "Barrotes de hierro negro", "Black Iron Bars", t_reja("hierro_n"), "COLOR_BLACK", 511),
    REJA("reja_forjada", "Reja forjada", "Wrought Grille", t_reja("hierro_n", forjada=True), "COLOR_BLACK", 512),
    REJA("reja_oxidada", "Barrotes oxidados", "Rusted Bars", t_reja("hierro_n", oxido_rampa="oxido"), "TERRACOTTA_ORANGE",
         513),
    CADENA("cadena_hierro", "Cadena de hierro negro", "Black Iron Chain", t_cadena("hierro_n"), "COLOR_BLACK", 521),
    CADENA("cadena_oro", "Cadena dorada", "Gilded Chain", t_cadena("oro"), "GOLD", 522),
    CADENA("cadena_oxidada", "Cadena oxidada", "Rusted Chain", t_cadena("oxido"), "TERRACOTTA_ORANGE", 523),
    VIDRIO("vidrio_viejo", "Vidrio viejo", "Old Glass", t_vidrio((150, 175, 160), 0.38, "viejo", (130, 160, 145, 210)),
           "NONE", 531),
    VIDRIO("vidrio_sucio", "Vidrio sucio", "Grimy Glass", t_vidrio((140, 150, 135), 0.45, "sucio", (95, 98, 85, 230)),
           "NONE", 532),
    VIDRIO("vidrio_emplomado", "Vidrio emplomado", "Leaded Glass", t_vidrio((175, 190, 180), 0.40, "emplomado",
                                                                          (40, 40, 44, 255)), "NONE", 533),
    VIDRIO("vidriera_roja", "Vidriera roja", "Red Stained Glass", t_vidriera("v_rojo", "v_oro", "v_blanco"), "COLOR_RED", 534),
    VIDRIO("vidriera_azul", "Vidriera azul", "Blue Stained Glass", t_vidriera("v_azul", "v_rojo", "v_blanco"), "COLOR_BLUE",
           535),
    VIDRIO("vidriera_ambar", "Vidriera ámbar", "Amber Stained Glass", t_vidriera("v_oro", "v_verde", "v_blanco"),
           "COLOR_ORANGE", 536),
    VIDRIO("vidriera_verde", "Vidriera verde", "Green Stained Glass", t_vidriera("v_verde", "v_oro", "v_blanco"),
           "COLOR_GREEN", 537),
    VIDRIO("vidriera_violeta", "Vidriera violeta", "Violet Stained Glass", t_vidriera("v_violeta", "v_oro", "v_azul"),
           "COLOR_PURPLE", 538),
    # ---- parte 8g: las luces y el fuego
    FUEGO("natural", "Fuego", "Fire", "COLOR_ORANGE", 601),
    FUEGO("almas", "Fuego de almas", "Soul Fire", "COLOR_LIGHT_BLUE", 602),
    FUEGO("veneno", "Fuego de veneno", "Poison Fire", "COLOR_LIGHT_GREEN", 603),
    FUEGO("arcano", "Fuego arcano", "Arcane Fire", "COLOR_MAGENTA", 604),
    FUEGO("sangre", "Fuego de sangre", "Blood Fire", "COLOR_RED", 605),
    FUEGO("sagrado", "Fuego sagrado", "Holy Fire", "GOLD", 606),
    FUEGO("escarcha", "Fuego de escarcha", "Frost Fire", "SNOW", 607),
    BRASERO("natural", "Brasero", "Brazier", "COLOR_ORANGE", 611),
    BRASERO("almas", "Brasero de almas", "Soul Brazier", "COLOR_LIGHT_BLUE", 612),
    BRASERO("veneno", "Brasero de veneno", "Poison Brazier", "COLOR_LIGHT_GREEN", 613),
    BRASERO("arcano", "Brasero arcano", "Arcane Brazier", "COLOR_MAGENTA", 614),
    BRASERO("sangre", "Brasero de sangre", "Blood Brazier", "COLOR_RED", 615),
    BRASERO("sagrado", "Brasero sagrado", "Holy Brazier", "GOLD", 616),
    BRASERO("escarcha", "Brasero de escarcha", "Frost Brazier", "SNOW", 617),
    VELA("vela_blanca", "Velas blancas", "White Candles", "blanca", 621),
    VELA("vela_negra", "Velas negras", "Black Candles", "negra", 622),
    VELA("vela_roja", "Velas rojas", "Red Candles", "roja", 623),
    CANDELABRO("candelabro", "Candelabro de hierro", "Iron Candelabrum", "c", "planchas_hierro", "cadena_hierro", "COLOR_BLACK", 631),
    CANDELABRO("candelabro_oro", "Candelabro dorado", "Gilded Candelabrum", "c", "oro_gastado", "cadena_oro", "GOLD", 632),
    CANDELABRO("aplique", "Aplique de hierro", "Iron Wall Sconce", "p", "planchas_hierro", "cadena_hierro", "COLOR_BLACK", 633),
    CANDELABRO("aplique_oro", "Aplique dorado", "Gilded Wall Sconce", "p", "oro_gastado", "cadena_oro", "GOLD", 634),
    CANDELABRO("arana", "Araña de hierro", "Iron Chandelier", "a", "planchas_hierro", "cadena_hierro", "COLOR_BLACK", 635),
    CANDELABRO("arana_oro", "Araña dorada", "Gilded Chandelier", "a", "oro_gastado", "cadena_oro", "GOLD", 636),
    FAROL("farol", "Farol", "Lantern", "natural", 641),
    FAROL("farol_almas", "Farol de almas", "Soul Lantern", "almas", 642),
    ANTORCHA("antorcha", "Antorcha", "Torch", "natural", 651),
    ANTORCHA("antorcha_almas", "Antorcha de almas", "Soul Torch", "almas", 652),    # ---- parte 8h: los muebles
    MUEBLE("mesa_roble", "Mesa de roble viejo", "Old Oak Table", "m", "roble", 701, mapa="WOOD"),
    MUEBLE("silla_roble", "Silla de roble viejo", "Old Oak Chair", "h", "roble", 702, mapa="WOOD"),
    MUEBLE("banco_roble", "Banco de roble viejo", "Old Oak Bench", "b", "roble", 703, mapa="WOOD"),
    MUEBLE("taburete_roble", "Taburete de roble viejo", "Old Oak Stool", "k", "roble", 704, mapa="WOOD"),
    MUEBLE("alacena_roble", "Alacena de roble viejo", "Old Oak Cupboard", "o", "roble", 705, mapa="WOOD"),
    MUEBLE("estanteria_roble", "Estantería de roble viejo", "Old Oak Bookshelf", "e", "roble", 706, mapa="WOOD"),
    MUEBLE("mesa_negra", "Mesa de madera negra", "Blackwood Table", "m", "negra", 707, mapa="COLOR_BLACK"),
    MUEBLE("silla_negra", "Silla de madera negra", "Blackwood Chair", "h", "negra", 708, mapa="COLOR_BLACK"),
    MUEBLE("banco_negra", "Banco de madera negra", "Blackwood Bench", "b", "negra", 709, mapa="COLOR_BLACK"),
    MUEBLE("taburete_negra", "Taburete de madera negra", "Blackwood Stool", "k", "negra", 710, mapa="COLOR_BLACK"),
    MUEBLE("alacena_negra", "Alacena de madera negra", "Blackwood Cupboard", "o", "negra", 711, mapa="COLOR_BLACK"),
    MUEBLE("estanteria_negra", "Estantería de madera negra", "Blackwood Bookshelf", "e", "negra", 712, mapa="COLOR_BLACK"),
    MUEBLE("mesa_caoba", "Mesa de caoba", "Mahogany Table", "m", "caoba", 713, mapa="COLOR_RED"),
    MUEBLE("silla_caoba", "Silla de caoba", "Mahogany Chair", "h", "caoba", 714, mapa="COLOR_RED"),
    MUEBLE("banco_caoba", "Banco de caoba", "Mahogany Bench", "b", "caoba", 715, mapa="COLOR_RED"),
    MUEBLE("taburete_caoba", "Taburete de caoba", "Mahogany Stool", "k", "caoba", 716, mapa="COLOR_RED"),
    MUEBLE("alacena_caoba", "Alacena de caoba", "Mahogany Cupboard", "o", "caoba", 717, mapa="COLOR_RED"),
    MUEBLE("estanteria_caoba", "Estantería de caoba", "Mahogany Bookshelf", "e", "caoba", 718, mapa="COLOR_RED"),
    MUEBLE("estanteria_vacia", "Estantería medio vacía", "Half-Empty Bookshelf", "e", "roble", 731),
    MUEBLE("estanteria_polvo", "Estantería polvorienta", "Dusty Bookshelf", "e", "gris", 732, mapa="COLOR_GRAY"),
    M("barril", "Barril", "Barrel", None, formas="C", sonido="WOOD", mapa="WOOD", semilla=733, dureza=2.0, herramienta="axe",
      lado=lambda s: Mu.barril_lado(s, RAMPAS["m_roble"], RAMPAS["hierro_n"]),
      tapa=lambda s: Mu.barril_tapa(s, RAMPAS["m_roble"], RAMPAS["hierro_n"])),
    M("barril_viejo", "Barril viejo", "Old Barrel", None, formas="C", sonido="WOOD", mapa="COLOR_GRAY", semilla=734, dureza=2.0,
      herramienta="axe", lado=lambda s: Mu.barril_lado(s, RAMPAS["m_gris"], RAMPAS["oxido"]),
      tapa=lambda s: Mu.barril_tapa(s, RAMPAS["m_gris"], RAMPAS["oxido"])),
    M("caja", "Caja de madera", "Wooden Crate", lambda s, b: Mu.caja(s, RAMPAS["m_roble"], RAMPAS["hierro_n"]), formas="B",
      sonido="WOOD", mapa="WOOD", semilla=735, dureza=2.0, herramienta="axe", variantes=1),
    M("caja_vieja", "Caja vieja", "Old Crate", lambda s, b: Mu.caja(s, RAMPAS["m_gris"], RAMPAS["oxido"]), formas="B",
      sonido="WOOD", mapa="COLOR_GRAY", semilla=736, dureza=2.0, herramienta="axe", variantes=1),
    MUEBLE("jergon", "Jergón de paja", "Straw Pallet", "j", "roble", 737, mapa="SAND"),
    MUEBLE("libros", "Pila de libros", "Pile of Books", "i", "roble", 738, mapa="COLOR_RED"),
    MUEBLE("atril", "Atril de roble viejo", "Old Oak Lectern", "r", "roble", 739),
    # ---- parte 8i: la decoracion
    DECO("estandarte_valdrenn", "Estandarte de Valdrenn", "Valdrenn Banner", "d", 801, mapa="COLOR_RED", sonido="WOOL",
         herramienta="axe", dureza=1.0, diseno="valdrenn"),
    DECO("estandarte_sombra", "Estandarte de la Sombra", "Banner of the Shadow", "d", 802, mapa="COLOR_BLACK", sonido="WOOL",
         herramienta="axe", dureza=1.0, diseno="sombra"),
    DECO("estandarte_almas", "Estandarte de las almas", "Soul Banner", "d", 803, mapa="COLOR_BLUE", sonido="WOOL",
         herramienta="axe", dureza=1.0, diseno="almas"),
    DECO("estandarte_hueso", "Estandarte del hueso", "Bone Banner", "d", 804, mapa="SAND", sonido="WOOL", herramienta="axe",
         dureza=1.0, diseno="hueso"),
    DECO("estandarte_roto", "Estandarte desgarrado", "Tattered Banner", "d", 805, mapa="COLOR_GRAY", sonido="WOOL",
         herramienta="axe", dureza=1.0, diseno="roto"),
    DECO("tapiz_rojo", "Tapiz carmesí", "Crimson Tapestry", "g", 811, mapa="COLOR_RED", sonido="WOOL", herramienta="axe",
         dureza=0.8, diseno="rojo"),
    DECO("tapiz_verde", "Tapiz verde", "Green Tapestry", "g", 812, mapa="COLOR_GREEN", sonido="WOOL", herramienta="axe",
         dureza=0.8, diseno="verde"),
    DECO("tapiz_azul", "Tapiz azul", "Blue Tapestry", "g", 813, mapa="COLOR_BLUE", sonido="WOOL", herramienta="axe",
         dureza=0.8, diseno="azul"),
    DECO("tapiz_negro", "Tapiz negro", "Black Tapestry", "g", 814, mapa="COLOR_BLACK", sonido="WOOL", herramienta="axe",
         dureza=0.8, diseno="negro"),
    M("alfombra_roja", "Alfombra roja", "Red Carpet", None, formas="A", sonido="WOOL", mapa="COLOR_RED", semilla=821, dureza=0.1,
      herramienta="hoe", variantes=1, textura="alfombra_roja"),
    M("alfombra_azul", "Alfombra azul", "Blue Carpet", None, formas="A", sonido="WOOL", mapa="COLOR_BLUE", semilla=822, dureza=0.1,
      herramienta="hoe", variantes=1, textura="alfombra_azul"),
    M("alfombra_raida", "Alfombra raída", "Worn Carpet", None, formas="A", sonido="WOOL", mapa="COLOR_GRAY", semilla=823,
      dureza=0.1, herramienta="hoe", variantes=1, textura="alfombra_raida"),
    DECO("calavera", "Calavera", "Skull", "n", 831, mapa="SAND", sonido="BONE_BLOCK", dureza=1.0),
    DECO("monton_huesos", "Montón de huesos", "Pile of Bones", "q", 832, mapa="SAND", sonido="BONE_BLOCK", dureza=1.0),
    DECO("telarana", "Telaraña", "Cobweb", "u", 833, mapa="WOOL", sonido="WOOL", herramienta="hoe", dureza=4.0),
    DECO("lapida", "Lápida", "Gravestone", "w", 834, dureza=2.0),
    DECO("cruz_piedra", "Cruz de piedra", "Stone Cross", "x", 835, dureza=2.0),
    DECO("ataud", "Ataúd", "Coffin", "y", 836, mapa="COLOR_BLACK", sonido="WOOD", herramienta="axe", dureza=2.0),
    DECO("gargola_piedra", "Gárgola de piedra", "Stone Gargoyle", "z", 837, mapa="COLOR_BLACK", dureza=3.0),
    DECO("busto", "Busto de mármol", "Marble Bust", "S", 838, mapa="QUARTZ", sonido="CALCITE", dureza=2.0),
    DECO("jaula", "Jaula colgante", "Hanging Cage", "F", 839, mapa="COLOR_BLACK", sonido="CHAIN", dureza=3.0),
]
POR_ID = {m["id"]: m for m in MATERIALES}

FORMAS = {"B": "", "E": "_escalera", "L": "_losa", "M": "_muro", "V": "_valla", "P": "_cancela", "D": "_puerta",
          "T": "_trampilla", "J": "_panel"}
# las formas que son el material entero (sin sufijo en el id): columna, planta, capas de nieve, hielo, ramas (de 8, 4 y
# 12 pixeles), hojas, racimo, musgo que cuelga, liana, alfombra (hojarasca), vidrio, reja y cadena; y las luces (parte
# 8g): f fuego, s brasero, v velas, c candelabro, p aplique, a arana, l farol y t antorcha (con su `_pared`); y los muebles
# (parte 8h): m mesa, h silla, b banco, k taburete, o alacena, e estanteria, j jergon, i pila de libros y r atril; y la
# decoracion (parte 8i): d estandarte, g tapiz, n calavera, q monton de huesos, u telarana, w lapida, x cruz de piedra,
# y ataud, z gargola, S busto y F jaula colgante
SIN_SUFIJO = ("C", "X", "N", "H", "R", "Q", "U", "O", "K", "G", "Y", "A", "W", "I", "Z", "f", "s", "v", "c", "p", "a", "l", "t",
              "m", "h", "b", "k", "o", "e", "j", "i", "r", "d", "g", "n", "q", "u", "w", "x", "y", "z", "S", "F")
NOMBRE_FORMA_ES = {"B": "{}", "E": "Escalera de {}", "L": "Losa de {}", "M": "Muro de {}", "V": "Valla de {}",
                   "P": "Cancela de {}", "D": "Puerta de {}", "T": "Trampilla de {}", "J": "Panel de {}"}
NOMBRE_FORMA_EN = {"B": "{}", "E": "{} Stairs", "L": "{} Slab", "M": "{} Wall", "V": "{} Fence", "P": "{} Fence Gate",
                   "D": "{} Door", "T": "{} Trapdoor", "J": "{} Pane"}


def minus(es):
    return es[0].lower() + es[1:]


# ------------------------------------------------------------------ texturas

def nombres(m):
    """Los nombres de las texturas de cada variante de un material de bloque entero: id, id_2, id_3..."""
    return [m["id"] if v == 0 else f'{m["id"]}_{v + 1}' for v in range(m["variantes"])]


_CACHE = {}


def texturas(m):
    """Las texturas de un material: {nombre de fichero: imagen}."""
    if m["id"] in _CACHE:
        return _CACHE[m["id"]]
    s = m["semilla"]
    out = {}
    if "C" in m["formas"]:
        if m.get("lado_de"):
            out = {m["id"] + "_tapa": P.imagen(m["tapa"](s + 1))}
        else:
            out = {m["id"] + "_lado": P.imagen(m["lado"](s)), m["id"] + "_tapa": P.imagen(m["tapa"](s + 1))}
    elif m["formas"] == "R":
        # la corteza de su arbol tumbada (la veta de lado, para los lados de los brazos; la usan tambien la ramita y la
        # rama gruesa de su especie)
        c = POR_ID[m["lado_de"]]
        rgb = c["receta"](c["semilla"], c["semilla"])
        out = {m["id"] + "_h": _img(np.ascontiguousarray(np.rot90(rgb)))}
    elif m["formas"] in ("Q", "U", "K"):
        out = {}
    elif m["formas"] == "f":
        c = m["color"]
        for k in range(2):
            out[f"fuego_{c}_{k}"] = _img(F.llamas(s + 37 * k, RAMPAS["f_" + c]))
            ANIM[f"fuego_{c}_{k}"] = 1
    elif m["formas"] in ("s", "v", "c", "p", "a", "l", "t", "m", "h", "b", "k", "o", "e", "j", "i", "r") + DECORACION:
        out = {}
    elif m["receta"] is None and "N" not in m["formas"]:
        out = {}   # (sus texturas son de las sueltas: las alfombras de la parte 8i)
    elif "N" not in m["formas"]:
        for v, n in enumerate(nombres(m)):
            r = m["receta"](s + 1000 * v, s)
            if isinstance(r, dict):
                for cara, rgb in r.items():
                    if cara.startswith("item/"):
                        out["item/" + n + cara[5:]] = _img(rgb)
                    else:
                        out[n + ("_" + cara if cara else "")] = _img(rgb)
            else:
                out[n] = _img(r)
        if m.get("extra"):
            out.update(m["extra"](m))
    _CACHE[m["id"]] = out
    return out


def _img(r):
    """rgb (alto, ancho, 3) o rgba (alto, ancho, 4) a imagen."""
    if r.shape[2] == 4:
        return P.imagen(r[..., :3], r[..., 3])
    return P.imagen(r)


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


def losa_caras(t, y0):
    """Una losa de hierba (abajo o arriba): el lado coge la MITAD DE ARRIBA de su textura (asi se ve el fleco)."""
    lados = {c: {"uv": [0, 0, 16, 8], "texture": "#side", "cullface": c} for c in ("north", "south", "west", "east")}
    caras = dict(lados)
    caras["up"] = {"uv": [0, 0, 16, 16], "texture": "#top"} if y0 == 0 else {"uv": [0, 0, 16, 16], "texture": "#top", "cullface": "up"}
    caras["down"] = {"uv": [0, 0, 16, 16], "texture": "#bottom", "cullface": "down"} if y0 == 0 else {"uv": [0, 0, 16, 16], "texture": "#bottom"}
    return {"parent": "minecraft:block/block", "textures": dict(t, particle=t["side"]),
            "elements": [{"from": [0, y0, 0], "to": [16, y0 + 8, 16], "faces": caras}]}


def escribir_capa(m):
    """Las capas de nieve (1-8): la textura del material `textura`; la de 8 es el bloque entero."""
    bid = m["id"]
    tex = f"darkcraft:block/{m['textura']}"
    mb = os.path.join(ASSETS, "models", "block")
    v = {}
    for capas in range(1, 9):
        h = capas * 2
        if capas == 8:
            v["layers=8"] = {"model": f"darkcraft:block/{m['textura']}"}
            continue
        lados = {c: {"uv": [0, 16 - h, 16, 16], "texture": "#texture", "cullface": c} for c in ("north", "south", "west", "east")}
        caras = dict(lados, up={"uv": [0, 0, 16, 16], "texture": "#texture"},
                     down={"uv": [0, 0, 16, 16], "texture": "#texture", "cullface": "down"})
        j(os.path.join(mb, f"{bid}_{h}.json"), {"parent": "minecraft:block/thin_block" if capas == 1 else "minecraft:block/block",
                                               "textures": {"particle": tex, "texture": tex},
                                               "elements": [{"from": [0, 0, 0], "to": [16, h, 16], "faces": caras}]})
        v[f"layers={capas}"] = {"model": f"darkcraft:block/{bid}_{h}"}
    j(os.path.join(ASSETS, "blockstates", bid + ".json"), {"variants": v})
    j(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": f"darkcraft:block/{bid}_2"})
    return [bid]


def estado_liana(modelo):
    """Como la enredadera del juego: un panel en cada cara pegada (y arriba); sin ninguna, en todas."""
    nada = {"east": "false", "north": "false", "south": "false", "up": "false", "west": "false"}
    partes = []
    for lado, rot in (("north", {}), ("east", {"y": 90}), ("south", {"y": 180}), ("west", {"y": 270}), ("up", {"x": 270})):
        a = dict({"model": modelo}, **rot)
        if rot:
            a["uvlock"] = True
        partes.append({"when": {lado: "true"}, "apply": a})
        partes.append({"when": dict(nada), "apply": dict(a)})
    return {"multipart": partes}


def _caja(a, b, tex, caras=("north", "south", "east", "west", "up", "down"), uv=None, rot=None, sombra=True):
    """Un elemento de modelo: una caja de `a` a `b` con la misma textura en las caras dadas."""
    e = {"from": list(a), "to": list(b), "faces": {}}
    for c in caras:
        f = {"texture": tex}
        if uv:
            f["uv"] = uv
        e["faces"][c] = f
    if rot:
        e["rotation"] = rot
    if not sombra:
        e["shade"] = False
    return e


def _llama(cx, y0, cz, ancho, alto, tex="#llama", cruzada=True, rot=None):
    """Una llama: dos planos que se cruzan (en diagonal, o en cruz si `cruzada` es falso), con la textura entera."""
    out = []
    w = ancho / 2
    if cruzada:
        for ang in (45, -45):
            out.append({"from": [cx - w, y0, cz], "to": [cx + w, y0 + alto, cz], "shade": False,
                        "rotation": {"origin": [cx, y0, cz], "axis": "y", "angle": ang},
                        "faces": {"north": {"uv": [0, 0, 16, 16], "texture": tex}, "south": {"uv": [0, 0, 16, 16], "texture": tex}}})
    else:
        a = {"from": [cx - w, y0, cz], "to": [cx + w, y0 + alto, cz], "shade": False,
             "faces": {"north": {"uv": [0, 0, 16, 16], "texture": tex}, "south": {"uv": [0, 0, 16, 16], "texture": tex}}}
        b = {"from": [cx, y0, cz - w], "to": [cx, y0 + alto, cz + w], "shade": False,
             "faces": {"east": {"uv": [0, 0, 16, 16], "texture": tex}, "west": {"uv": [0, 0, 16, 16], "texture": tex}}}
        if rot:
            a["rotation"] = dict(rot)
            b["rotation"] = dict(rot)
        out += [a, b]
    return out


def _vela(x, z, alto, y0=0.0, grueso=2.0):
    """Una vela de pie en (x, z) (la esquina), con su mecha y su llama."""
    g = grueso
    els = [_caja((x, y0, z), (x + g, y0 + alto, z + g), "#cera", uv=[0, 0, g, alto])]
    els.append(_caja((x + g / 2 - 0.25, y0 + alto, z + g / 2 - 0.25), (x + g / 2 + 0.25, y0 + alto + 1, z + g / 2 + 0.25),
                     "#mecha", caras=("north", "south", "east", "west")))
    els += _llama(x + g / 2, y0 + alto + 0.3, z + g / 2, 3.0, 4.5)
    return els


# las velas de cada cuenta (como las del juego): esquina x, z y alto
VELAS = {1: [(7, 7, 7)], 2: [(5, 7, 7), (9, 6, 5)], 3: [(7, 4, 7), (4, 9, 5), (10, 9, 6)],
         4: [(4, 4, 7), (10, 4, 6), (4, 10, 5), (10, 10, 8)]}


def escribir_luz(m):
    """Las luces de la parte 8g: fuego, brasero, velas, candelabros (de pie, de pared y arana), farol y antorcha. Modelos
    escritos caja a caja; la llama de las velas, faroles y antorchas es la `llamita` animada (de su color) en dos planos
    cruzados."""
    bid, f = m["id"], m["formas"]
    bs = os.path.join(ASSETS, "blockstates")
    mb = os.path.join(ASSETS, "models", "block")
    mi = os.path.join(ASSETS, "models", "item")
    c = m.get("color", "natural")
    llamita = f"darkcraft:block/llamita_{'almas' if c == 'almas' else 'natural'}"
    if f == "f":
        for k in range(2):
            j(os.path.join(mb, f"{bid}_{k}.json"), {"parent": "minecraft:block/template_fire_floor",
                                                    "textures": {"fire": f"darkcraft:block/fuego_{c}_{k}"},
                                                    "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": [{"model": f"darkcraft:block/{bid}_0"},
                                                              {"model": f"darkcraft:block/{bid}_1"}]}})
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated",
                                           "textures": {"layer0": f"darkcraft:block/fuego_{c}_0"}})
        return [bid]
    if f == "s":
        t = {"hierro": "darkcraft:block/planchas_hierro", "brasas": f"darkcraft:block/brasas_{c}",
             "fuego": f"darkcraft:block/fuego_{c}_0", "particle": "darkcraft:block/planchas_hierro"}
        els = [_caja(a, b, "#hierro") for a, b in (
            ((2, 0, 2), (4, 9, 4)), ((12, 0, 2), (14, 9, 4)), ((2, 0, 12), (4, 9, 14)), ((12, 0, 12), (14, 9, 14)),
            ((2, 9, 2), (14, 10, 14)), ((1, 9, 1), (15, 13, 2)), ((1, 9, 14), (15, 13, 15)), ((1, 9, 2), (2, 13, 14)),
            ((14, 9, 2), (15, 13, 14)), ((3, 3, 3), (13, 4, 4)), ((3, 3, 12), (13, 4, 13)))]
        els.append(_caja((2, 10, 2), (14, 12, 14), "#brasas", caras=("up",)))
        # el fuego: cuatro planos que se inclinan hacia dentro (como el fuego del juego), dentro del cuenco
        for (eje, pos, ang, cara) in (("x", 9.5, -22.5, "south"), ("x", 6.5, 22.5, "north"),
                                      ("z", 9.5, -22.5, "west"), ("z", 6.5, 22.5, "east")):
            if eje == "x":
                e = {"from": [2, 11.5, pos], "to": [14, 27.5, pos]}
            else:
                e = {"from": [pos, 11.5, 2], "to": [pos, 27.5, 14]}
            e.update({"rotation": {"origin": [8, 11.5, 8], "axis": eje, "angle": ang, "rescale": True}, "shade": False,
                      "faces": {cara: {"uv": [0, 0, 16, 16], "texture": "#fuego"}}})
            els.append(e)
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/block", "textures": t, "elements": els,
                                           "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    if f == "v":
        t = {"cera": f"darkcraft:block/cera_{m['cera']}", "mecha": "darkcraft:block/cera_negra", "llama": llamita,
             "particle": f"darkcraft:block/cera_{m['cera']}"}
        v = {}
        for k, velas in VELAS.items():
            els = []
            for (x, z, h) in velas:
                els += _vela(x, z, h)
            j(os.path.join(mb, f"{bid}_{k}.json"), {"parent": "minecraft:block/block", "textures": t, "elements": els,
                                                    "render_type": "minecraft:cutout"})
            v[f"velas={k}"] = {"model": f"darkcraft:block/{bid}_{k}"}
        j(os.path.join(bs, bid + ".json"), {"variants": v})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}_1"})
        return [bid]
    t = {"metal": f"darkcraft:block/{m.get('metal', 'planchas_hierro')}", "cera": "darkcraft:block/cera_blanca",
         "mecha": "darkcraft:block/cera_negra", "llama": llamita, "cadena": f"darkcraft:block/{m.get('cadena', 'cadena_hierro')}",
         "particle": f"darkcraft:block/{m.get('metal', 'planchas_hierro')}"}
    if f == "c":
        els = [_caja(a, b, "#metal") for a, b in (
            ((5, 0, 5), (11, 1, 11)), ((6, 1, 6), (10, 2, 10)), ((7, 2, 7), (9, 11, 9)), ((6.5, 6, 6.5), (9.5, 7, 9.5)),
            ((2, 10, 7.5), (14, 11, 8.5)), ((1.5, 11, 7), (3.5, 12, 9)), ((12.5, 11, 7), (14.5, 12, 9)), ((6.5, 11, 6.5), (9.5, 12, 9.5)))]
        els += _vela(1.75, 7.25, 4, y0=12, grueso=1.5) + _vela(12.75, 7.25, 4, y0=12, grueso=1.5) + _vela(7.25, 7.25, 5, y0=12, grueso=1.5)
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/block", "textures": t, "elements": els,
                                           "render_type": "minecraft:cutout"})
        giro = {"north": 0, "east": 90, "south": 180, "west": 270}
        j(os.path.join(bs, bid + ".json"), {"variants": {f"facing={d}": dict({"model": f"darkcraft:block/{bid}"}, **({"y": y} if y else {}))
                                                         for d, y in giro.items()}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    if f == "p":
        # mirando al norte: la placa contra la pared del sur (z 16)
        els = [_caja(a, b, "#metal") for a, b in (
            ((6, 3, 15), (10, 11, 16)), ((7.5, 5, 10), (8.5, 6, 15)), ((7.5, 5, 10), (8.5, 7, 11)), ((6, 7, 8.5), (10, 8, 12.5)))]
        els += _vela(7.25, 9.75, 5, y0=8, grueso=1.5)
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/block", "textures": t, "elements": els,
                                           "render_type": "minecraft:cutout"})
        giro = {"north": 0, "east": 90, "south": 180, "west": 270}
        j(os.path.join(bs, bid + ".json"), {"variants": {f"facing={d}": dict({"model": f"darkcraft:block/{bid}"}, **({"y": y} if y else {}))
                                                         for d, y in giro.items()}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    if f == "a":
        els = [_caja(a, b, "#metal") for a, b in (
            ((2, 4, 2), (14, 5, 3)), ((2, 4, 13), (14, 5, 14)), ((2, 4, 3), (3, 5, 13)), ((13, 4, 3), (14, 5, 13)),
            ((3, 4, 7.5), (13, 5, 8.5)), ((7.5, 4, 3), (8.5, 5, 13)), ((7, 3, 7), (9, 6, 9)))]
        for (x, z) in ((2.25, 2.25), (12.25, 2.25), (2.25, 12.25), (12.25, 12.25), (7.25, 2.25), (7.25, 12.25),
                       (2.25, 7.25), (12.25, 7.25)):
            els += _vela(x, z, 3, y0=5, grueso=1.5)
        # las cadenas: dos planos cruzados del centro al techo (como la cadena del juego)
        for ang in (45, -45):
            els.append({"from": [6.5, 6, 8], "to": [9.5, 16, 8], "shade": False,
                        "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": ang},
                        "faces": {"north": {"uv": [3, 0, 0, 10], "texture": "#cadena"}, "south": {"uv": [0, 0, 3, 10], "texture": "#cadena"}}})
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/block", "textures": t, "elements": els,
                                           "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    if f == "l":
        def farol(dy):
            els = [_caja(a, b, "#metal") for a, b in (
                ((5, dy, 5), (11, dy + 1, 11)), ((5, dy + 1, 5), (6, dy + 7, 6)), ((10, dy + 1, 5), (11, dy + 7, 6)),
                ((5, dy + 1, 10), (6, dy + 7, 11)), ((10, dy + 1, 10), (11, dy + 7, 11)), ((5, dy + 7, 5), (11, dy + 8, 11)),
                ((6, dy + 8, 6), (10, dy + 9, 10)), ((7, dy + 9, 7), (9, dy + 10, 9)),
                ((7.5, dy + 1, 5), (8.5, dy + 7, 5.5)), ((7.5, dy + 1, 10.5), (8.5, dy + 7, 11)),
                ((5, dy + 1, 7.5), (5.5, dy + 7, 8.5)), ((10.5, dy + 1, 7.5), (11, dy + 7, 8.5)))]
            els.append(_caja((7, dy + 1, 7), (9, dy + 3, 9), "#cera"))
            els += _llama(8, dy + 3, 8, 3.5, 4.5)
            return els
        t["cera"] = "darkcraft:block/cera_blanca"
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/block", "textures": t, "elements": farol(0),
                                           "render_type": "minecraft:cutout"})
        colgado = farol(1)
        for ang in (45, -45):
            colgado.append({"from": [6.5, 11, 8], "to": [9.5, 16, 8], "shade": False,
                            "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": ang},
                            "faces": {"north": {"uv": [3, 0, 0, 5], "texture": "#cadena"}, "south": {"uv": [0, 0, 3, 5], "texture": "#cadena"}}})
        j(os.path.join(mb, bid + "_colgado.json"), {"parent": "minecraft:block/block", "textures": t, "elements": colgado,
                                                    "render_type": "minecraft:cutout"})
        v = {}
        for colg in ("false", "true"):
            for agua in ("false", "true"):
                v[f"hanging={colg},waterlogged={agua}"] = {"model": f"darkcraft:block/{bid}" + ("_colgado" if colg == "true" else "")}
        j(os.path.join(bs, bid + ".json"), {"variants": v})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    if f == "t":
        t = {"palo": "darkcraft:block/antorcha_palo", "trapo": "darkcraft:block/antorcha_trapo", "llama": llamita,
             "particle": "darkcraft:block/antorcha_palo"}
        de_pie = [_caja((7, 0, 7), (9, 9, 9), "#palo"), _caja((6.5, 8, 6.5), (9.5, 11, 9.5), "#trapo")]
        de_pie += _llama(8, 10.5, 8, 5, 7, cruzada=False)
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/block", "textures": t, "elements": de_pie,
                                           "render_type": "minecraft:cutout", "ambientocclusion": False})
        # de pared (mirando al este: pegada a la pared del oeste, inclinada hacia fuera como la del juego)
        rot = {"origin": [0, 3.5, 8], "axis": "z", "angle": -22.5}
        pared = [_caja((-1, 3.5, 7), (1, 12.5, 9), "#palo", rot=rot), _caja((-1.5, 11.5, 6.5), (1.5, 14.5, 9.5), "#trapo", rot=rot)]
        pared += _llama(0, 14, 8, 5, 7, cruzada=False, rot=rot)
        j(os.path.join(mb, bid + "_pared.json"), {"parent": "minecraft:block/block", "textures": t, "elements": pared,
                                                  "render_type": "minecraft:cutout", "ambientocclusion": False})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
        giro = {"east": 0, "south": 90, "west": 180, "north": 270}
        j(os.path.join(bs, bid + "_pared.json"), {"variants": {
            f"facing={d}": dict({"model": f"darkcraft:block/{bid}_pared"}, **({"y": y} if y else {})) for d, y in giro.items()}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid, bid + "_pared"]
    raise ValueError(f)


def _girado(bid, modelo, facing_y=None):
    """El estado de un mueble que mira a un lado (el modelo mira al norte)."""
    giro = facing_y or {"north": 0, "east": 90, "south": 180, "west": 270}
    return {"variants": {f"facing={d}": dict({"model": modelo}, **({"y": y} if y else {})) for d, y in giro.items()}}


# las pilas de libros: cada libro (desde, hasta, giro en y, franja de la tapa)
LIBROS = [((4, 0, 3), (12, 2, 13), 0, 0), ((5, 2, 4), (11, 4, 12), 22.5, 1), ((4, 4, 3), (12, 6, 13), 0, 2),
          ((6, 6, 5), (10, 8, 11), -22.5, 3)]


def escribir_mueble(m):
    """Los muebles de la parte 8h, caja a caja (las texturas de madera son las tablas de la parte 8c)."""
    bid, f = m["id"], m["formas"]
    bs = os.path.join(ASSETS, "blockstates")
    mb = os.path.join(ASSETS, "models", "block")
    mi = os.path.join(ASSETS, "models", "item")
    k = m.get("madera_mueble", "roble")
    tex_madera = f"darkcraft:block/{MADERAS_MUEBLE.get(k, ('madera_gris', ''))[0]}"
    t = {"madera": tex_madera, "particle": tex_madera}

    def modelo(nombre, els, textos=None):
        j(os.path.join(mb, nombre + ".json"), {"parent": "minecraft:block/block", "textures": textos or t, "elements": els})

    if f == "m":
        els = [_caja(a, b, "#madera") for a, b in (
            ((0, 13, 0), (16, 16, 16)), ((1, 0, 1), (3, 13, 3)), ((13, 0, 1), (15, 13, 3)), ((1, 0, 13), (3, 13, 15)),
            ((13, 0, 13), (15, 13, 15)), ((3, 11, 1.5), (13, 13, 2.5)), ((3, 11, 13.5), (13, 13, 14.5)),
            ((1.5, 11, 3), (2.5, 13, 13)), ((13.5, 11, 3), (14.5, 13, 13)))]
        modelo(bid, els)
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
    elif f == "h":
        els = [_caja(a, b, "#madera") for a, b in (
            ((3, 7, 3), (13, 9, 13)), ((3, 0, 3), (5, 7, 5)), ((11, 0, 3), (13, 7, 5)), ((3, 0, 11), (5, 7, 13)),
            ((11, 0, 11), (13, 7, 13)), ((3, 9, 11), (5, 22, 13)), ((11, 9, 11), (13, 22, 13)),
            ((5, 12, 11.5), (11, 14, 12.5)), ((5, 16, 11.5), (11, 18, 12.5)), ((4.5, 20, 11), (11.5, 22, 13)),
            ((5, 2, 3.5), (11, 3, 4.5)))]
        modelo(bid, els)
        j(os.path.join(bs, bid + ".json"), _girado(bid, f"darkcraft:block/{bid}"))
    elif f == "b":
        els = [_caja(a, b, "#madera") for a, b in (
            ((0, 6, 4), (16, 8, 12)), ((2, 0, 5), (4, 6, 11)), ((12, 0, 5), (14, 6, 11)), ((1.5, 0, 4.5), (4.5, 1, 11.5)),
            ((11.5, 0, 4.5), (14.5, 1, 11.5)), ((4, 2, 7), (12, 3, 9)))]
        modelo(bid, els)
        j(os.path.join(bs, bid + ".json"), _girado(bid, f"darkcraft:block/{bid}"))
    elif f == "k":
        els = [_caja(a, b, "#madera") for a, b in (
            ((4, 8, 4), (12, 10, 12)), ((4.5, 0, 4.5), (6, 8, 6)), ((10, 0, 4.5), (11.5, 8, 6)), ((4.5, 0, 10), (6, 8, 11.5)),
            ((10, 0, 10), (11.5, 8, 11.5)), ((6, 3, 5), (10, 4, 5.5)), ((6, 3, 10.5), (10, 4, 11)))]
        modelo(bid, els)
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
    elif f == "o":
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/orientable", "textures": {
            "front": f"darkcraft:block/alacena_{k}", "side": tex_madera, "top": tex_madera}})
        j(os.path.join(bs, bid + ".json"), _girado(bid, f"darkcraft:block/{bid}"))
    elif f == "e":
        lados = ["estanteria_vacia"] if bid == "estanteria_vacia" else ["estanteria_polvo"] if bid == "estanteria_polvo" else \
            [f"estanteria_{k}", f"estanteria_{k}_2", f"estanteria_{k}_3"]
        estados = []
        for i, lado in enumerate(lados):
            nm = bid if i == 0 else f"{bid}_{i + 1}"
            j(os.path.join(mb, nm + ".json"), {"parent": "minecraft:block/cube_column",
                                              "textures": {"end": tex_madera, "side": f"darkcraft:block/{lado}"}})
            estados.append({"model": f"darkcraft:block/{nm}"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": estados if len(estados) > 1 else estados[0]}})
    elif f == "j":
        tl = {"lino": "darkcraft:block/lino", "paja": "darkcraft:block/paja", "particle": "darkcraft:block/paja"}
        els = [_caja((0, 0, 1), (16, 4, 15), "#lino"), _caja((1, 4, 10.5), (15, 6, 14.5), "#lino")]
        els.append(_caja((1, 3.9, 2), (15, 4.1, 9), "#paja", caras=("up",)))
        modelo(bid, els, tl)
        j(os.path.join(bs, bid + ".json"), _girado(bid, f"darkcraft:block/{bid}"))
    elif f == "i":
        tl = {"tapa": "darkcraft:block/libro_tapa", "hojas": "darkcraft:block/libro_hojas", "particle": "darkcraft:block/libro_tapa"}
        v = {}
        for cuantos in range(1, 5):
            els = []
            for (a, b, giro, franja) in LIBROS[:cuantos]:
                uv = [0, 4 * franja, 16, 4 * franja + 4]
                e = {"from": list(a), "to": list(b), "faces": {
                    "up": {"texture": "#tapa", "uv": [0, 4 * franja, 16, 4 * franja + 4]},
                    "down": {"texture": "#tapa", "uv": [0, 4 * franja, 16, 4 * franja + 4]},
                    "west": {"texture": "#tapa", "uv": uv}, "east": {"texture": "#hojas"},
                    "north": {"texture": "#hojas"}, "south": {"texture": "#hojas"}}}
                if giro:
                    e["rotation"] = {"origin": [8, a[1] + 1, 8], "axis": "y", "angle": giro}
                els.append(e)
            modelo(f"{bid}_{cuantos}", els, tl)
            v[f"cuantos={cuantos}"] = {"model": f"darkcraft:block/{bid}_{cuantos}"}
        j(os.path.join(bs, bid + ".json"), {"variants": v})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}_2"})
        return [bid]
    elif f == "r":
        tl = dict(t, hojas="darkcraft:block/libro_hojas", tapa="darkcraft:block/libro_tapa")
        rot = {"origin": [8, 13, 8], "axis": "x", "angle": -22.5}
        els = [_caja(a, b, "#madera") for a, b in (((3, 0, 3), (13, 2, 13)), ((6, 2, 6), (10, 12, 10)), ((5, 11, 5), (11, 12, 11)))]
        els.append(_caja((2, 12, 3), (14, 13.5, 13), "#madera", rot=rot))
        els.append(_caja((1.5, 13.5, 2.5), (14.5, 14.5, 3.5), "#madera", rot=rot))
        libro = _caja((3, 13.5, 4), (13, 14.5, 12), "#hojas", rot=rot)
        libro["faces"]["up"] = {"texture": "#hojas", "uv": [0, 0, 16, 8]}
        els.append(libro)
        els.append(_caja((7.75, 14.5, 4), (8.25, 14.6, 12), "#tapa", caras=("up",), rot=rot))
        modelo(bid, els, tl)
        j(os.path.join(bs, bid + ".json"), _girado(bid, f"darkcraft:block/{bid}"))
    j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
    return [bid]


DECORACION = ("d", "g", "n", "q", "u", "w", "x", "y", "z", "S", "F")


def escribir_deco(m):
    """La decoracion de la parte 8i, caja a caja (lo que mira a un lado se escribe mirando al norte)."""
    bid, f = m["id"], m["formas"]
    bs = os.path.join(ASSETS, "blockstates")
    mb = os.path.join(ASSETS, "models", "block")
    mi = os.path.join(ASSETS, "models", "item")
    B_ = "darkcraft:block/"
    girado = True

    def modelo(els, t, render="minecraft:cutout"):
        d = {"parent": "minecraft:block/block", "textures": t, "elements": els}
        if render:
            d["render_type"] = render
        j(os.path.join(mb, bid + ".json"), d)

    if f == "d":
        k = m["diseno"]
        t = {"arriba": B_ + f"estandarte_{k}_arriba", "abajo": B_ + f"estandarte_{k}_abajo", "barra": B_ + "madera_negra",
             "metal": B_ + "oro_gastado", "particle": B_ + f"estandarte_{k}_arriba"}
        els = [_caja((0.5, 14, 13.5), (15.5, 15, 14.5), "#barra"), _caja((-0.5, 13.5, 13), (1, 15.5, 15), "#metal"),
               _caja((15, 13.5, 13), (16.5, 15.5, 15), "#metal"), _caja((7.5, 14.5, 14.5), (8.5, 16, 16), "#barra")]
        for (y0, y1, tex, uv) in ((0, 14, "#arriba", [0, 2, 16, 16]), (-16, 0, "#abajo", [0, 0, 16, 16])):
            els.append({"from": [1, y0, 14.2], "to": [15, y1, 14.2], "shade": True,
                        "faces": {"north": {"uv": uv, "texture": tex}, "south": {"uv": [uv[2], uv[1], uv[0], uv[3]], "texture": tex}}})
        modelo(els, t)
    elif f == "g":
        k = m["diseno"]
        t = {"tapiz": B_ + f"tapiz_{k}", "particle": B_ + f"tapiz_{k}"}
        els = [{"from": [0, 0, 15.2], "to": [16, 16, 16], "faces": {
            "north": {"texture": "#tapiz"}, "up": {"texture": "#tapiz", "uv": [0, 0, 16, 1]},
            "down": {"texture": "#tapiz", "uv": [0, 15, 16, 16]}, "east": {"texture": "#tapiz", "uv": [0, 0, 1, 16]},
            "west": {"texture": "#tapiz", "uv": [15, 0, 16, 16]}}}]
        modelo(els, t, None)
    elif f == "n":
        t = {"hueso": B_ + "hueso", "cara": B_ + "calavera_cara", "particle": B_ + "hueso"}
        craneo = _caja((4.5, 1.5, 5), (11.5, 7.5, 11.5), "#hueso")
        craneo["faces"]["north"] = {"texture": "#cara"}
        els = [craneo, _caja((5.5, 0, 5.2), (10.5, 1.5, 9.5), "#hueso")]
        mand = _caja((5.5, 0, 5), (10.5, 1.5, 5.6), "#cara", caras=("north",), uv=[3, 13, 13, 16])
        els.append(mand)
        modelo(els, t, None)
    elif f == "q":
        girado = False
        t = {"hueso": B_ + "hueso", "cara": B_ + "calavera_cara", "particle": B_ + "hueso"}
        els = []
        for (a, b, ang) in (((2, 0, 4), (13, 1.2, 5.2), 22.5), ((3, 0, 10), (14, 1.2, 11.2), -22.5), ((7, 1, 2), (8.2, 2.2, 14), 45),
                            ((4, 1.2, 6), (12, 2.4, 7.2), -45), ((2, 0, 7), (10, 1, 8), 0), ((9, 0.5, 3), (10.2, 1.7, 12), 22.5)):
            e = _caja(a, b, "#hueso")
            if ang:
                e["rotation"] = {"origin": [8, a[1], 8], "axis": "y", "angle": ang}
            els.append(e)
            # las cabezas del hueso (un nudo en cada punta)
        cr = _caja((9, 0, 8.5), (13, 3.6, 12.5), "#hueso")
        cr["faces"]["north"] = {"texture": "#cara"}
        cr["rotation"] = {"origin": [11, 0, 10.5], "axis": "y", "angle": 22.5}
        els.append(cr)
        modelo(els, t, None)
    elif f == "u":
        girado = False
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/cross", "textures": {"cross": B_ + "telarana"},
                                           "render_type": "minecraft:cutout"})
    elif f in ("w", "x"):
        t = {"piedra": B_ + "lapida", "base": B_ + "sillar_gris", "particle": B_ + "lapida"}
        if f == "w":
            els = [_caja((2, 0, 6), (14, 1.5, 10), "#base"), _caja((3, 1.5, 7), (13, 12, 9), "#piedra"),
                   _caja((4, 12, 7), (12, 13, 9), "#piedra"), _caja((5.5, 13, 7), (10.5, 14, 9), "#piedra")]
            els[1]["faces"]["north"] = {"texture": "#piedra", "uv": [2, 4, 14, 16]}
            els[1]["faces"]["south"] = {"texture": "#piedra", "uv": [2, 4, 14, 16]}
        else:
            els = [_caja((4, 0, 5), (12, 1.5, 11), "#base"), _caja((6.5, 1.5, 7), (9.5, 16, 9), "#piedra"),
                   _caja((3, 9, 7), (13, 12, 9), "#piedra"), _caja((5, 13, 6.5), (11, 14, 9.5), "#piedra", caras=("up", "down"))]
        modelo(els, t, None)
    elif f == "y":
        t = {"madera": B_ + "madera_negra", "hierro": B_ + "planchas_hierro", "particle": B_ + "madera_negra"}
        els = [_caja((3.5, 0, 10), (12.5, 18, 15), "#madera"), _caja((2.5, 18, 10), (13.5, 26, 15), "#madera"),
               _caja((4, 26, 10), (12, 30, 15), "#madera"), _caja((7, 6, 9.5), (9, 24, 10), "#hierro"),
               _caja((4.5, 17, 9.5), (11.5, 19, 10), "#hierro")]
        for (a, b) in (((3, 4, 9.6), (4.5, 5.5, 10)), ((11.5, 4, 9.6), (13, 5.5, 10)), ((2, 22, 9.6), (3.5, 23.5, 10)),
                       ((12.5, 22, 9.6), (14, 23.5, 10))):
            els.append(_caja(a, b, "#hierro"))
        modelo(els, t, None)
    elif f == "z":
        t = {"piedra": B_ + "roca_oscura", "ojo": B_ + "brasas_sangre", "particle": B_ + "roca_oscura"}
        els = [_caja((3, 0, 5), (13, 2, 15), "#piedra"), _caja((4, 2, 5.5), (6.5, 6.5, 9), "#piedra"),
               _caja((9.5, 2, 5.5), (12, 6.5, 9), "#piedra"),
               _caja((5, 3, 8), (11, 11, 14.5), "#piedra", rot={"origin": [8, 3, 11], "axis": "x", "angle": 22.5}),
               _caja((5.5, 8, 3), (10.5, 13, 8), "#piedra"), _caja((6.5, 8, 2), (9.5, 10.5, 3), "#piedra"),
               _caja((6, 13, 5), (7, 15, 6), "#piedra"), _caja((9, 13, 5), (10, 15, 6), "#piedra"),
               _caja((1.5, 6, 10), (5, 15, 14), "#piedra", rot={"origin": [5, 6, 12], "axis": "z", "angle": 22.5}),
               _caja((11, 6, 10), (14.5, 15, 14), "#piedra", rot={"origin": [11, 6, 12], "axis": "z", "angle": -22.5}),
               _caja((6.2, 11, 2.95), (7.4, 12, 3), "#ojo", caras=("north",)), _caja((8.6, 11, 2.95), (9.8, 12, 3), "#ojo", caras=("north",))]
        modelo(els, t, None)
    elif f == "S":
        t = {"marmol": B_ + "marmol_blanco", "base": B_ + "marmol_negro", "particle": B_ + "marmol_blanco"}
        els = [_caja((4, 0, 4), (12, 1.5, 12), "#base"), _caja((5, 1.5, 5), (11, 5, 11), "#base"), _caja((4.5, 5, 4.5), (11.5, 6, 11.5), "#base"),
               _caja((3, 6, 5.5), (13, 10, 10.5), "#marmol"), _caja((7, 10, 7), (9, 11, 9), "#marmol"),
               _caja((5.5, 11, 5.5), (10.5, 16, 10.5), "#marmol"), _caja((7.5, 12.5, 5), (8.5, 14, 5.5), "#marmol")]
        modelo(els, t, None)
    elif f == "F":
        girado = False
        t = {"barras": B_ + "barrotes", "hierro": B_ + "planchas_hierro", "cadena": B_ + "cadena_hierro", "hueso": B_ + "hueso",
             "cara": B_ + "calavera_cara", "particle": B_ + "planchas_hierro"}
        els = [_caja((3, 0.5, 3), (13, 1.5, 13), "#hierro"), _caja((3, 13, 3), (13, 14, 13), "#hierro"),
               _caja((5, 14, 5), (11, 15, 11), "#hierro")]
        for (a, b, caras) in (((3, 1.5, 3), (13, 13, 3), ("north", "south")), ((3, 1.5, 13), (13, 13, 13), ("north", "south")),
                              ((3, 1.5, 3), (3, 13, 13), ("east", "west")), ((13, 1.5, 3), (13, 13, 13), ("east", "west"))):
            els.append({"from": list(a), "to": list(b), "faces": {c: {"texture": "#barras", "uv": [0, 0, 16, 16]} for c in caras}})
        for ang in (45, -45):
            els.append({"from": [6.5, 15, 8], "to": [9.5, 16, 8], "shade": False,
                        "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": ang},
                        "faces": {"north": {"uv": [3, 0, 0, 1], "texture": "#cadena"}, "south": {"uv": [0, 0, 3, 1], "texture": "#cadena"}}})
        cr = _caja((6, 1.5, 6), (9, 4.5, 9), "#hueso")
        cr["faces"]["north"] = {"texture": "#cara"}
        els.append(cr)
        els.append(_caja((8, 1.5, 9), (12, 2.5, 10), "#hueso", rot={"origin": [10, 2, 9.5], "axis": "y", "angle": 22.5}))
        modelo(els, t)
    if girado:
        j(os.path.join(bs, bid + ".json"), _girado(bid, f"darkcraft:block/{bid}"))
    else:
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
    if f == "u":
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": B_ + "telarana"}})
    elif f == "d":
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": B_ + f"estandarte_{m['diseno']}_arriba"}})
    else:
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
    return [bid]


def escribir_vidrio(m):
    """Vidrio: el bloque (translucido) y, si lleva J, su panel (como el panel de vidrio del juego: poste, lados y sin
    lado, con el canto `<id>_borde`)."""
    bid = m["id"]
    tex = f"darkcraft:block/{bid}"
    bs = os.path.join(ASSETS, "blockstates")
    mb = os.path.join(ASSETS, "models", "block")
    mi = os.path.join(ASSETS, "models", "item")
    j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/cube_all", "textures": {"all": tex},
                                       "render_type": "minecraft:translucent"})
    j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
    j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
    ids = [bid]
    if "J" in m["formas"]:
        p = bid + "_panel"
        t = {"pane": tex, "edge": tex + "_borde"}
        for suf in ("post", "side", "side_alt", "noside", "noside_alt"):
            j(os.path.join(mb, f"{p}_{suf}.json"), {"parent": f"minecraft:block/template_glass_pane_{suf}", "textures": t,
                                                     "render_type": "minecraft:translucent"})
        md = lambda suf: f"darkcraft:block/{p}_{suf}"
        j(os.path.join(bs, p + ".json"), {"multipart": [
            {"apply": {"model": md("post")}},
            {"when": {"north": "true"}, "apply": {"model": md("side")}},
            {"when": {"east": "true"}, "apply": {"model": md("side"), "y": 90}},
            {"when": {"south": "true"}, "apply": {"model": md("side_alt")}},
            {"when": {"west": "true"}, "apply": {"model": md("side_alt"), "y": 90}},
            {"when": {"north": "false"}, "apply": {"model": md("noside")}},
            {"when": {"east": "false"}, "apply": {"model": md("noside_alt")}},
            {"when": {"south": "false"}, "apply": {"model": md("noside_alt"), "y": 90}},
            {"when": {"west": "false"}, "apply": {"model": md("noside"), "y": 270}}]})
        j(os.path.join(mi, p + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
        ids.append(p)
    return ids


def escribir_reja(m):
    """Una reja: los modelos de los barrotes del juego con nuestra textura (barras en las columnas 2-3, 7-8 y 12-13)."""
    bid = m["id"]
    tex = f"darkcraft:block/{bid}"
    mb = os.path.join(ASSETS, "models", "block")
    for suf in ("post_ends", "post", "cap", "cap_alt", "side", "side_alt"):
        j(os.path.join(mb, f"{bid}_{suf}.json"), {"parent": f"minecraft:block/iron_bars_{suf}",
                                                   "textures": {"bars": tex, "edge": tex, "particle": tex},
                                                   "render_type": "minecraft:cutout_mipped"})
    md = lambda suf: f"darkcraft:block/{bid}_{suf}"
    nada = {"east": "false", "north": "false", "south": "false", "west": "false"}
    j(os.path.join(ASSETS, "blockstates", bid + ".json"), {"multipart": [
        {"apply": {"model": md("post_ends")}},
        {"when": dict(nada), "apply": {"model": md("post")}},
        {"when": dict(nada, north="true"), "apply": {"model": md("cap")}},
        {"when": dict(nada, east="true"), "apply": {"model": md("cap"), "y": 90}},
        {"when": dict(nada, south="true"), "apply": {"model": md("cap_alt")}},
        {"when": dict(nada, west="true"), "apply": {"model": md("cap_alt"), "y": 90}},
        {"when": {"north": "true"}, "apply": {"model": md("side")}},
        {"when": {"east": "true"}, "apply": {"model": md("side"), "y": 90}},
        {"when": {"south": "true"}, "apply": {"model": md("side_alt")}},
        {"when": {"west": "true"}, "apply": {"model": md("side_alt"), "y": 90}}]})
    j(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
    return [bid]


def escribir_tronco(m):
    """Un tronco con eje: un modelo por cada variante de su corteza (de pie, girado al azar; tumbado)."""
    bid = m["id"]
    c = POR_ID[m["lado_de"]]
    tapa = f"darkcraft:block/{bid}_tapa"
    mb = os.path.join(ASSETS, "models", "block")
    de_pie, en_z, en_x = [], [], []
    for i, n in enumerate(nombres(c)):
        nm = bid if i == 0 else f"{bid}_{i + 1}"
        t = {"end": tapa, "side": f"darkcraft:block/{n}"}
        j(os.path.join(mb, nm + ".json"), {"parent": "minecraft:block/cube_column", "textures": t})
        j(os.path.join(mb, nm + "_tumbado.json"), {"parent": "minecraft:block/cube_column_horizontal", "textures": t})
        for giro in (0, 90, 180, 270):
            e = {"model": f"darkcraft:block/{nm}"}
            if giro:
                e["y"] = giro
            de_pie.append(e)
        en_z.append({"model": f"darkcraft:block/{nm}_tumbado", "x": 90})
        en_x.append({"model": f"darkcraft:block/{nm}_tumbado", "x": 90, "y": 90})
    j(os.path.join(ASSETS, "blockstates", bid + ".json"), {"variants": {"axis=y": de_pie, "axis=z": en_z, "axis=x": en_x}})
    j(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
    return [bid]


# los bultos de cada modelo de racimo (en dieciseisavos)
RACIMOS = [
    [[2, 3, 2, 12, 12, 12], [6, 7, 5, 15, 15, 14], [1, 1, 7, 9, 9, 15], [7, 1, 1, 14, 7, 8]],
    [[3, 1, 3, 13, 10, 13], [1, 6, 1, 10, 15, 10], [8, 5, 7, 15, 13, 15]],
    [[4, 4, 1, 14, 13, 10], [1, 2, 6, 10, 11, 15], [6, 8, 6, 13, 15, 13], [9, 1, 9, 15, 6, 15]],
]


def escribir_racimo(m):
    """Un racimo de hojas: tres modelos de bultos (cada uno en cuatro giros), con la textura de sus hojas."""
    bid = m["id"]
    h = POR_ID[m["lado_de"]]
    tex = f"darkcraft:block/{h['id']}"
    mb = os.path.join(ASSETS, "models", "block")
    estados = []
    for i, bultos in enumerate(RACIMOS):
        nm = bid if i == 0 else f"{bid}_{i + 1}"
        elementos = [{"from": b[:3], "to": b[3:], "faces": {c: {"texture": "#hojas"} for c in
                                                             ("north", "south", "east", "west", "up", "down")}}
                     for b in bultos]
        j(os.path.join(mb, nm + ".json"), {"parent": "minecraft:block/block", "render_type": "minecraft:cutout_mipped",
                                          "textures": {"hojas": tex, "particle": tex}, "elements": elementos})
        for giro in (0, 90, 180, 270):
            e = {"model": f"darkcraft:block/{nm}"}
            if giro:
                e["y"] = giro
            estados.append(e)
    j(os.path.join(ASSETS, "blockstates", bid + ".json"), {"variants": {"": estados}})
    j(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
    return [bid]


def escribir_rama(m, g=4):
    """Una rama (como la planta de coro): el nudo del centro (8 x 8 x 8) siempre y un brazo hacia cada cara unida. El brazo
    se escribe mirando al norte y se gira; sus lados de la izquierda y la derecha llevan la corteza tumbada (la veta a lo
    largo del brazo) y arriba y abajo la de siempre; la testa del tronco de su arbol solo en el modelo del inventario."""
    bid = m["id"]
    sp = m["lado_de"][len("corteza_"):]
    t = {"corteza": f"darkcraft:block/{m['lado_de']}", "corteza_h": f"darkcraft:block/rama_{sp}_h",
         "testa": f"darkcraft:block/tronco_{sp}_tapa", "particle": f"darkcraft:block/{m['lado_de']}"}
    mb = os.path.join(ASSETS, "models", "block")
    a, b = 8 - g, 8 + g
    caras_centro = {c: {"texture": "#corteza"} for c in ("north", "south", "east", "west", "up", "down")}
    j(os.path.join(mb, bid + "_centro.json"), {"parent": "minecraft:block/block", "textures": t,
                                               "elements": [{"from": [a, a, a], "to": [b, b, b], "faces": caras_centro}]})
    # (la punta del brazo con corteza: solo se ve donde la rama se une a otra mas fina o a hojas: un reborde de corteza)
    brazo = {"north": {"texture": "#corteza", "uv": [a, a, b, b], "cullface": "north"},
             "east": {"texture": "#corteza_h"}, "west": {"texture": "#corteza_h"},
             "up": {"texture": "#corteza"}, "down": {"texture": "#corteza"}}
    j(os.path.join(mb, bid + "_brazo.json"), {"parent": "minecraft:block/block", "textures": t,
                                              "elements": [{"from": [a, a, 0], "to": [b, b, a], "faces": brazo}]})
    inv = {"north": {"texture": "#testa", "uv": [a, a, b, b]}, "south": {"texture": "#testa", "uv": [a, a, b, b]},
           "east": {"texture": "#corteza_h"}, "west": {"texture": "#corteza_h"},
           "up": {"texture": "#corteza"}, "down": {"texture": "#corteza"}}
    j(os.path.join(mb, bid + "_inventario.json"), {"parent": "minecraft:block/block", "textures": t,
                                                   "elements": [{"from": [a, a, 0], "to": [b, b, 16], "faces": inv}]})
    partes = [{"apply": {"model": f"darkcraft:block/{bid}_centro"}}]
    for lado, rot in (("north", {}), ("east", {"y": 90}), ("south", {"y": 180}), ("west", {"y": 270}), ("up", {"x": 270}),
                      ("down", {"x": 90})):
        partes.append({"when": {lado: "true"}, "apply": dict({"model": f"darkcraft:block/{bid}_brazo"}, **rot)})
    j(os.path.join(ASSETS, "blockstates", bid + ".json"), {"multipart": partes})
    j(os.path.join(ASSETS, "models", "item", bid + ".json"), {"parent": f"darkcraft:block/{bid}_inventario"})
    return [bid]


def escribir_material(m):
    bid = m["id"]
    tex = f"darkcraft:block/{bid}"
    bs = os.path.join(ASSETS, "blockstates")
    mb = os.path.join(ASSETS, "models", "block")
    mi = os.path.join(ASSETS, "models", "item")
    ids = []
    if "C" in m["formas"] and m.get("lado_de"):
        return escribir_tronco(m)
    if m["formas"] in ("f", "s", "v", "c", "p", "a", "l", "t"):
        return escribir_luz(m)
    if m["formas"] in ("m", "h", "b", "k", "o", "e", "j", "i", "r"):
        return escribir_mueble(m)
    if m["formas"] in DECORACION:
        return escribir_deco(m)
    if "W" in m["formas"]:
        return escribir_vidrio(m)
    if m["formas"] == "I":
        return escribir_reja(m)
    if m["formas"] == "Z":
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/chain", "textures": {"all": tex, "particle": tex},
                                           "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {
            "axis=x": {"model": f"darkcraft:block/{bid}", "x": 90, "y": 90},
            "axis=y": {"model": f"darkcraft:block/{bid}"},
            "axis=z": {"model": f"darkcraft:block/{bid}", "x": 90}}})
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": f"darkcraft:item/{bid}"}})
        return [bid]
    if m["formas"] in GRUESOS:
        return escribir_rama(m, GRUESOS[m["formas"]])
    if m["formas"] == "K":
        return escribir_racimo(m)
    if "O" in m["formas"]:
        modelos = []
        for n in nombres(m):
            j(os.path.join(mb, n + ".json"), {"parent": "minecraft:block/cube_all", "textures": {"all": f"darkcraft:block/{n}"},
                                             "render_type": "minecraft:cutout_mipped"})
            modelos.append({"model": f"darkcraft:block/{n}"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": modelos}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
    if "G" in m["formas"]:
        for suf in ("", "_punta"):
            j(os.path.join(mb, bid + suf + ".json"), {"parent": "minecraft:block/cross", "textures": {"cross": tex + suf},
                                                     "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"punta=false": {"model": f"darkcraft:block/{bid}"},
                                                         "punta=true": {"model": f"darkcraft:block/{bid}_punta"}}})
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": tex + "_punta"}})
        return [bid]
    if "Y" in m["formas"]:
        # como la enredadera del juego (sin tinte)
        j(os.path.join(mb, bid + ".json"), {"ambientocclusion": False, "textures": {"particle": tex, "vine": tex},
                                           "render_type": "minecraft:cutout",
                                           "elements": [{"from": [0, 0, 0.8], "to": [16, 16, 0.8], "shade": False, "faces": {
                                               "north": {"uv": [16, 0, 0, 16], "texture": "#vine"},
                                               "south": {"uv": [0, 0, 16, 16], "texture": "#vine"}}}]})
        j(os.path.join(bs, bid + ".json"), estado_liana(f"darkcraft:block/{bid}"))
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
        return [bid]
    if "A" in m["formas"]:
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/carpet", "textures": {"wool": tex},
                                           "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": [dict({"model": f"darkcraft:block/{bid}"}, **({"y": g} if g else {}))
                                                              for g in (0, 90, 180, 270)]}})
        j(os.path.join(mi, bid + ".json"), {"parent": f"darkcraft:block/{bid}"})
        return [bid]
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
    if "N" in m["formas"]:
        return escribir_capa(m)
    if "X" in m["formas"]:
        j(os.path.join(mb, bid + ".json"), {"parent": "minecraft:block/cross", "textures": {"cross": tex},
                                           "render_type": "minecraft:cutout"})
        j(os.path.join(bs, bid + ".json"), {"variants": {"": {"model": f"darkcraft:block/{bid}"}}})
        j(os.path.join(mi, bid + ".json"), {"parent": "minecraft:item/generated", "textures": {"layer0": tex}})
        return [bid]
    pesos = m.get("pesos")
    enteros = []
    for i, n in enumerate(nombres(m)):
        for giro in ((0, 90, 180, 270) if m.get("girar") else (0,)):
            e = {"model": f"darkcraft:block/{n}"}
            if giro:
                e["y"] = giro
            if pesos:
                e["weight"] = pesos[i]
            enteros.append(e)
    if "B" in m["formas"] or "H" in m["formas"]:
        for n in nombres(m):
            if m.get("caras"):
                mod = {"parent": "minecraft:block/cube_bottom_top", "textures": {
                    "top": f"darkcraft:block/{n}_arriba", "side": f"darkcraft:block/{n}_lado",
                    "bottom": f"darkcraft:block/{m['abajo']}"}}
            else:
                mod = {"parent": "minecraft:block/cube_all", "textures": {"all": f"darkcraft:block/{n}"}}
            if "H" in m["formas"]:
                mod["render_type"] = "minecraft:translucent"
            j(os.path.join(mb, n + ".json"), mod)
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
    if "L" in m["formas"] and m.get("caras"):
        l = bid + "_losa"
        t = {"top": tex + "_arriba", "side": tex + "_lado", "bottom": f"darkcraft:block/{m['abajo']}"}
        j(os.path.join(mb, l + ".json"), losa_caras(t, 0))
        j(os.path.join(mb, l + "_arriba.json"), losa_caras(t, 8))
        j(os.path.join(bs, l + ".json"), {"variants": {
            "type=bottom": {"model": f"darkcraft:block/{l}"},
            "type=top": {"model": f"darkcraft:block/{l}_arriba"},
            "type=double": enteros if len(enteros) > 1 else enteros[0]}})
        j(os.path.join(mi, l + ".json"), {"parent": f"darkcraft:block/{l}"})
        ids.append(l)
    elif "L" in m["formas"]:
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
        suelta = bid
        if bid.endswith("_pared") and bid[:-6] in POR_ID and POR_ID[bid[:-6]]["formas"] == "t":
            suelta = bid[:-6]   # la antorcha de pared no tiene objeto: suelta la antorcha
        entrada = {"type": "minecraft:item", "name": "darkcraft:" + suelta}
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
                            "trapdoors", "wooden_trapdoors", "wooden_stairs", "wooden_slabs", "planks", "pickaxe", "axe",
                            "shovel", "hoe", "dirt", "sand", "snow", "ice", "logs", "leaves", "climbable")}
    for m in MATERIALES:
        b = "darkcraft:" + m["id"]
        madera = m["herramienta"] == "axe"
        for f in m["formas"]:
            bid = b + ("" if f in SIN_SUFIJO else FORMAS[f])
            tags[m["herramienta"]].append(bid)
            if f == "t":
                tags[m["herramienta"]].append(bid + "_pared")
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
            if f in ("B", "H", "N", "C", "R", "Q", "U", "O", "K", "Y"):
                for e in m.get("etiquetas", ()):
                    tags[e].append(bid)
    t = os.path.join(DATA, "minecraft", "tags", "blocks")
    ti = os.path.join(DATA, "minecraft", "tags", "items")
    for k, v in tags.items():
        if k in ("pickaxe", "axe", "shovel", "hoe"):
            j(os.path.join(t, "mineable", k + ".json"), {"replace": False, "values": v})
            continue
        j(os.path.join(t, k + ".json"), {"replace": False, "values": v})
        if k not in ("climbable",):
            # (trepar es cosa de bloques: no hay etiqueta de objetos)
            j(os.path.join(ti, k + ".json"), {"replace": False, "values": v})


def textos():
    es, en = {}, {}
    for m in MATERIALES:
        for f in m["formas"]:
            if f in SIN_SUFIJO:
                es["block.darkcraft." + m["id"]] = m["es"]
                en["block.darkcraft." + m["id"]] = m["en"]
                if f == "t":
                    es["block.darkcraft." + m["id"] + "_pared"] = m["es"]
                    en["block.darkcraft." + m["id"] + "_pared"] = m["en"]
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
        if "N" in m["formas"]:
            continue
        tx = texturas(m)
        if "C" in m["formas"] and m.get("lado_de"):
            c = POR_ID[m["lado_de"]]
            ct = texturas(c)
            lado, tapa = ct[c["id"]], tx[m["id"] + "_tapa"]
            lista = [ct[n] for n in nombres(c)]
        elif "C" in m["formas"]:
            lado, tapa = tx[m["id"] + "_lado"], tx[m["id"] + "_tapa"]
            lista = [lado]
        elif m["formas"] in ("Q", "U", "K"):
            continue
        elif "R" in m["formas"]:
            lado = tapa = tx[m["id"] + "_h"]
            lista = [lado]
        elif m.get("caras"):
            lado, tapa = tx[m["id"] + "_lado"], tx[m["id"] + "_arriba"]
            lista = [tx[n + "_arriba"] for n in nombres(m)]
        elif any(f in m["formas"] for f in "XOGYAWIZ"):
            color = (96, 124, 160, 255) if any(f in m["formas"] for f in "OGYWIZ") else (52, 48, 44, 255)
            lista = []
            for nombre in sorted(tx):
                if nombre.startswith("item/") or nombre.endswith("_borde"):
                    continue
                fondo = Image.new("RGBA", (64, 64), color)
                fondo.alpha_composite(tx[nombre])
                lista.append(fondo)
            lado = tapa = lista[0]
        else:
            lado = tapa = tx[m["id"]]
            lista = [tx[n] for n in nombres(m)]
        rng = np.random.default_rng(7)
        rep = Image.new("RGBA", (256, 256))
        for a in range(4):
            for b in range(4):
                pieza = lista[int(rng.integers(0, len(lista)))]
                if m.get("girar"):
                    pieza = pieza.rotate(90 * int(rng.integers(0, 4)))
                rep.paste(pieza, (a * 64, b * 64))
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


def agua():
    """El agua propia (parte 8j): las texturas del agua del juego (y de sus burbujas, salpicaduras y gotas) cambiadas por las
    del mod, en assets/minecraft (el bloque sigue siendo el agua del juego: se puede anegar todo y el terreno sigue igual)."""
    mc = os.path.join(RES, "assets", "minecraft", "textures")
    out = {
        ("block", "water_still"): (Ag.quieta(951), 2),
        ("block", "water_flow"): (Ag.fluye(952), 1),
        ("block", "water_overlay"): (Ag.encima(953), None),
        ("misc", "underwater"): (Ag.debajo(954), None),
        ("particle", "bubble"): (Ag.burbuja(), None),
    }
    for k in range(5):
        out[("particle", f"bubble_pop_{k}")] = (Ag.burbuja_rota(k), None)
    for k in range(4):
        out[("particle", f"splash_{k}")] = (Ag.salpicadura(k), None)
    for t in ("hang", "fall", "land"):
        out[("particle", f"drip_{t}")] = (Ag.gota(t), None)
    for (carpeta, nombre), (img, frametime) in out.items():
        d = os.path.join(mc, carpeta)
        os.makedirs(d, exist_ok=True)
        _img(img).save(os.path.join(d, nombre + ".png"))
        if frametime:
            j(os.path.join(d, nombre + ".png.mcmeta"), {"animation": {"frametime": frametime}})


def choques():
    """Los ids del catalogo que ya usa otro bloque u objeto del mod (registrado fuera del paquete `bloques`): en la 8f
    "hierro_negro" chocaba con el bloque del marco del portal (parte 5e-1) y el juego no arrancaba."""
    import re
    usados = set()
    raiz_java = os.path.join(RAIZ, "src", "main", "java")
    for d, _, fs in os.walk(raiz_java):
        if d.endswith(os.sep + "bloques"):
            continue
        for f in fs:
            if f.endswith(".java"):
                with open(os.path.join(d, f), encoding="utf-8") as fh:
                    usados |= set(re.findall(r'register\("([a-z0-9_]+)"', fh.read()))
    nuestros = set()
    for m in MATERIALES:
        for f in m["formas"]:
            bid = m["id"] + ("" if f in SIN_SUFIJO else FORMAS[f])
            nuestros |= {bid, bid + "_pared"} if f == "t" else {bid}
    return sorted(nuestros & usados)


def main():
    args = sys.argv[1:]
    malos = choques()
    if malos:
        sys.exit(f"estos ids ya los usa otra cosa del mod: {malos}")
    tdir = os.path.join(ASSETS, "textures", "block")
    os.makedirs(tdir, exist_ok=True)
    ids = []

    def guardar(nombre, img):
        if not hasattr(img, "save"):
            img = _img(img)
        for pre, carpeta in (("item/", "item"), ("particle/", "particle")):
            if nombre.startswith(pre):
                d = os.path.join(ASSETS, "textures", carpeta)
                os.makedirs(d, exist_ok=True)
                img.save(os.path.join(d, nombre[len(pre):] + ".png"))
                return
        img.save(os.path.join(tdir, nombre + ".png"))
        if nombre in ANIM:
            j(os.path.join(tdir, nombre + ".png.mcmeta"), {"animation": {"frametime": ANIM[nombre]}})
    for nombre, img in list(sueltas().items()) + list(sueltas_muebles().items()) + list(sueltas_decoracion().items()):
        guardar(nombre, img)
    particulas_json()
    agua()
    for m in MATERIALES:
        for nombre, img in texturas(m).items():
            guardar(nombre, img)
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
