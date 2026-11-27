#!/usr/bin/env python3
"""
Los herreros de Morvain (parte 7af): sus pieles (64 x 64, modelo de jugador con brazos anchos, como Dorvan) y sus retratos
para los dialogos (64 x 64), en `textures/entity/<id>.png` y `textures/gui/retratos/<id>.png`.

- **Hagen** (el Refugio de la Fragua): viejo, la frente despejada y la melena gris atada en una coleta, barba corta y bigote
  blancos, una cicatriz en la mejilla; tunica de lana parda de manga larga con brazales de cuero, delantal remendado.
- **Brenna** (junto al Faro de los Huesos): pelirroja con un panuelo verdemar en la cabeza y una trenza a la espalda, pecas,
  la sonrisa torcida; camisa sin mangas (un ancla tatuada en el brazo), cinto de cuerda con un anzuelo, botas altas.
- **Ulrik** (el Paso del Colmillo): grande, melena y barba rojizas con dos trenzas y anillas de hierro, un manto de piel sobre
  los hombros, tatuajes azules en los antebrazos, brazaletes; envueltas en las piernas y botas de piel.
- **Tobin** (la Puerta de la Capital): el aprendiz, joven, pelo negro revuelto, unas gafas de laton en la frente, la cara
  tiznada, panuelo rojo al cuello, un delantal que le queda grande y guantes enormes.

    python3 tools/personajes/herreros.py [--vista carpeta]
"""
import math
import os
import sys

import numpy as np

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from piel import Lienzo, Piel, mezcla, ruido, tela  # noqa: E402

AQUI = os.path.dirname(os.path.abspath(__file__))
ASSETS = os.path.abspath(os.path.join(AQUI, "..", "..", "src/main/resources/assets/darkcraft"))

HIERRO = ["#202024", "#34343a", "#4c4c56", "#6c6c78", "#9898a4"]
LATON = ["#4a3410", "#7a5a1c", "#a88030", "#d4ac4c", "#f0d27a"]
BRASA = ["#5e1806", "#a8320c", "#e86418", "#ffa83a", "#ffe08a"]

HAGEN = dict(
    id="hagen", semilla=41,
    piel=["#4a2e22", "#64402e", "#7e563e", "#986e52", "#b08866", "#c6a07e"],
    pelo=["#4e4a46", "#6e6a64", "#908c84", "#b4b0a8", "#d4d0c8", "#ecE8e0"],
    camisa=["#1c1812", "#28221a", "#352d22", "#433829", "#524532"],
    cuero=["#1a1009", "#2a1a0e", "#3e2614", "#54341c", "#6c4626", "#865a30"],
    pantalon=["#14140f", "#1d1d16", "#27271e", "#323227"],
    bota=["#120d0a", "#1e1611", "#2c2018", "#3a2b20"],
    ojo="#566a7c", fondo=("#080605", "#3a1608"), luz=BRASA,
)
BRENNA = dict(
    id="brenna", semilla=43,
    piel=["#6a4430", "#8a5c44", "#a8765a", "#c49274", "#d8ac8e", "#ecc6a8"],
    pelo=["#2e1006", "#4a1c0c", "#6a2c12", "#8a3c18", "#a84e22", "#c4682e"],
    camisa=["#141e20", "#1c2a2c", "#263a3c", "#30494b", "#3c5a5c"],
    cuero=["#1e130c", "#2e1d11", "#422a18", "#5a3a21", "#734c2c", "#8c5e38"],
    pantalon=["#3a3428", "#4a4334", "#5a5241", "#6a614e"],
    bota=["#140f0b", "#221912", "#30231a", "#3e2e22"],
    panuelo=["#0c2426", "#123638", "#1a4a4c", "#246062", "#327a7a"],
    ojo="#4e6e5a", fondo=("#0a0e12", "#24303a"), luz=BRASA,
)
ULRIK = dict(
    id="ulrik", semilla=47,
    piel=["#5a3020", "#7a4430", "#985a40", "#b47456", "#c88e6e", "#dca888"],
    pelo=["#3e1c0c", "#5e2c12", "#82401a", "#a65a26", "#c47a3a", "#dc9c56"],
    camisa=["#220e0c", "#341612", "#481e18", "#5c281e", "#703426"],
    cuero=["#1c120a", "#2c1c10", "#402816", "#56361e", "#6e4828", "#885a32"],
    pantalon=["#242422", "#30302c", "#3c3c37", "#4a4a44"],
    bota=["#2a2018", "#3c2e22", "#4e3e2e", "#62503c"],
    piel_manto=["#262019", "#3a3028", "#4e4236", "#665646", "#806c58", "#9a866e"],
    tatuaje="#2c4c86", ojo="#4a7ab0", fondo=("#06080e", "#1c2a3e"), luz=BRASA,
)
TOBIN = dict(
    id="tobin", semilla=53,
    piel=["#6a4a34", "#8a6248", "#a87c5e", "#c49878", "#d8b292", "#eccaaa"],
    pelo=["#0a0806", "#141010", "#211a16", "#30261f", "#40342a"],
    camisa=["#1a1e24", "#232a32", "#2e3742", "#3a4552", "#485464"],
    cuero=["#1e130c", "#2e1d11", "#422a18", "#5a3a21", "#734c2c", "#8c5e38"],
    pantalon=["#2c2016", "#3a2b1e", "#483626", "#56422e"],
    bota=["#140f0b", "#221912", "#30231a", "#3e2e22"],
    panuelo=["#4a0c0c", "#6e1414", "#922020", "#b4302a", "#cc4a3a"],
    hollin="#3a3230", ojo="#5a3a20", fondo=("#090807", "#2c2420"), luz=BRASA,
)
TODOS = [HAGEN, BRENNA, ULRIK, TOBIN]


# ---------------------------------------------------------------- la piel

def _caras(cfg, P):
    """La cara (8 x 8) de cada uno, con su paleta."""
    pi, pe = cfg["piel"], cfg["pelo"]
    # la boca: una raya de labio (bajo el bigote, la sombra de la barba; Tobin, abierta de entusiasmo)
    boca = pe[1] if cfg["id"] in ("hagen", "ulrik") else "#4a1a14" if cfg["id"] == "tobin" else mezcla(pi[1], "#7a3428", 0.5)
    C = {"s": pi[4], "S": pi[5], "o": pi[3], "O": pi[2], "n": pi[5], "W": "#d4ccc0", "I": cfg["ojo"], "m": boca,
         "h": pe[2], "H": pe[3], "p": pe[1], "P": pe[2], "K": pe[4] if cfg["id"] == "hagen" else pe[1],
         "B": pe[3], "b": pe[2], "M": pe[4], "r": "#c4846a"}
    if cfg["id"] == "hagen":
        C["H"], C["h"] = pe[3], pe[2]
        filas = ["hHsSSsHh",
                 "hssSSssh",
                 "oKKooKKo",
                 "oWIonIWo",
                 "OosnnsrO",
                 "BMMmmMMB",
                 "BBbBBbBB",
                 "OBBbbBBO"]
    elif cfg["id"] == "brenna":
        pa = cfg["panuelo"]
        C.update({"T": pa[3], "t": pa[2], "f": "#a0664a", "L": pi[2]})
        filas = ["TTtTTtTT",
                 "hTTTTTTh",
                 "hsSSSSsh",
                 "hWIssIWh",
                 "ofsnnsfo",
                 "osSsmLso",
                 "OosSSsoO",
                 "hOoooOOh"]
    elif cfg["id"] == "ulrik":
        C.update({"B": pe[3], "b": pe[2], "M": pe[4], "K": pe[2]})
        filas = ["hhhHHhhh",
                 "hhsSSshh",
                 "hKKssKKh",
                 "hWIsnIWh",
                 "bBsnnsBb",
                 "BMMmmMMB",
                 "BBbBBbBB",
                 "BbBBBBbB"]
    else:
        C.update({"X": cfg["hollin"], "t": "#e8e0d0", "p": pe[1], "P": pe[2]})
        filas = ["pPppPpPp",
                 "PpsSSsPp",
                 "psSSSSsp",
                 "oWIsSIWo",
                 "oXsnnsXo",
                 "Ossmtsso",
                 "OosSSsoO",
                 "XOooooOX"]
    P.sprite("cabeza", "frente", filas, C)


def _cabeza(cfg, P):
    pi, pe = cfg["piel"], cfg["pelo"]
    i = cfg["id"]
    piel_f = ruido(pi[2:6], cfg["semilla"], grano=0.3, base=1.3)
    pelo_f = ruido(pe[1:5], cfg["semilla"] + 1, grano=0.45, base=1.5)

    def lado(x, y, w, h):
        # x = 0 junto a la cara (el costado visto de lado: 0 delante, 7 detras)
        oreja = y in (3, 4) and x in (3, 4)
        if i == "hagen":
            if y >= 5 and x <= 2:
                return pe[3] if (x + y) % 2 else pe[2]
            if x >= 3 or y <= 1:
                return pe[2] if oreja is False and (x + y) % 3 else pe[1] if not oreja else pi[2]
            return pi[3]
        if i == "brenna":
            if y <= 1:
                return cfg["panuelo"][3] if y == 0 else cfg["panuelo"][2]
            if x >= 3:
                return pelo_f(x, y, w, h)
            return pi[2] if oreja else piel_f(x, y, w, h)
        if i == "ulrik":
            if y >= 4 and x <= 3:
                return pe[3] if (x + y) % 2 else pe[2]
            if x >= 2 or y <= 1:
                return pelo_f(x, y, w, h)
            return piel_f(x, y, w, h)
        # tobin: greñas por arriba y detras, oreja, tizne
        if y <= 1 or x >= 5:
            return pelo_f(x, y, w, h)
        if oreja:
            return pi[2]
        return cfg["hollin"] if (x, y) == (1, 5) else piel_f(x, y, w, h)

    P.pintar("cabeza", "der", lambda x, y, w, h: lado(w - 1 - x, y, w, h))
    P.pintar("cabeza", "izq", lado)

    def espalda(x, y, w, h):
        if i == "hagen" and y in (5, 6) and x in (3, 4):
            return cfg["cuero"][2]  # la cinta de la coleta
        if i == "brenna" and y <= 1:
            return cfg["panuelo"][2]
        if i == "brenna" and y >= 3 and x in (3, 4):
            return pe[3] if (x + y) % 2 else pe[4]  # nace la trenza
        return pelo_f(x, y, w, h)

    P.pintar("cabeza", "espalda", espalda)

    def arriba(x, y, w, h):
        if i == "hagen":
            # la frente despejada: delante (y alto = hacia la cara) piel, detras pelo
            return pi[4] if y >= 5 and 2 <= x <= 5 else pe[2] if (x + y) % 2 else pe[3]
        if i == "brenna":
            return cfg["panuelo"][3] if (x + y) % 3 else cfg["panuelo"][2]
        return pelo_f(x, y, w, h)

    P.pintar("cabeza", "arriba", arriba)
    P.pintar("cabeza", "abajo", lambda x, y, w, h: pe[2] if i in ("hagen", "ulrik") and y < 5 else pi[1])

    # la capa de encima: el volumen del pelo, el nudo del panuelo, las gafas de Tobin
    def capa_lado(x, y, w, h):
        if i == "hagen" and x >= 4 and y >= 2:
            return pe[3] if (x * 3 + y) % 4 else None
        if i == "ulrik" and x >= 3:
            return pe[4] if (x + 2 * y) % 5 == 0 else pe[3] if (x + y) % 3 else None
        if i == "brenna" and y == 1 and x >= 5:
            return cfg["panuelo"][1]
        if i == "tobin" and y == 1:
            return cfg["cuero"][1]  # la correa de las gafas
        if i == "tobin" and y == 0 and x % 2:
            return pe[1]
        return None

    P.pintar("cabeza_f", "der", lambda x, y, w, h: capa_lado(w - 1 - x, y, w, h))
    P.pintar("cabeza_f", "izq", capa_lado)

    def capa_espalda(x, y, w, h):
        if i == "brenna":
            if y == 1 and x in (3, 4):
                return cfg["panuelo"][4]  # el nudo
            if y in (2, 3) and x in (2, 5):
                return cfg["panuelo"][2]  # las puntas
            return None
        if i == "hagen":
            return pe[3] if y >= 6 and x in (3, 4) else None
        if i == "ulrik":
            return pe[3] if (x + y) % 2 == 0 else pe[4] if y > 5 else None
        if i == "tobin":
            return cfg["cuero"][1] if y == 1 else None
        return None

    P.pintar("cabeza_f", "espalda", capa_espalda)

    def capa_frente(x, y, w, h):
        if i == "tobin" and y == 1:
            # las gafas de laton subidas en la frente
            if x in (1, 2, 5, 6):
                return "#6a9aa8" if x in (2, 5) else LATON[3]
            return LATON[1] if x in (0, 7) else cfg["cuero"][1]
        if i == "ulrik" and y >= 6 and x in (1, 6):
            return pe[4]  # los mechones de la barba que se abren
        return None

    P.pintar("cabeza_f", "frente", capa_frente)
    P.pintar("cabeza_f", "arriba", lambda x, y, w, h: (pe[3] if (x * 7 + y) % 5 == 0 else None) if i in ("ulrik", "tobin") else None)
    P.pintar("cabeza_f", "abajo", lambda x, y, w, h: None)


def _cuerpo(cfg, P):
    i = cfg["id"]
    camisa = tela(cfg["camisa"], cfg["semilla"] + 2, pliegue=3, grano=0.10)
    cuero = ruido(cfg["cuero"][2:5], cfg["semilla"] + 3, grano=0.32, base=1.2)
    cu, pe, pi = cfg["cuero"], cfg["pelo"], cfg["piel"]

    def frente(x, y, w, h):
        if i == "tobin" and y < 2:
            return cfg["panuelo"][3] if (x + y) % 3 else cfg["panuelo"][2]
        if i == "ulrik" and y < 4 and x in (2, 3, 4, 5):
            # las dos trenzas de la barba sobre el pecho, con su anilla
            if y == 3:
                return HIERRO[3] if x in (2, 5) else HIERRO[2]
            return pe[3] if (x + y) % 2 else pe[2]
        if i == "hagen" and y < 2 and x in (3, 4):
            return pe[3]  # la barba que asoma
        if y < 2 and x in (3, 4):
            return pi[3] if y == 0 else pi[2]
        return camisa(x, y, w, h)

    P.pintar("cuerpo", "frente", frente)

    def espalda(x, y, w, h):
        if i == "brenna" and y < 8 and x in (3, 4):
            return pe[3] if (x + y) % 2 else pe[4]  # la trenza
        if i == "hagen" and y < 4 and x in (3, 4):
            return pe[3] if (x + y) % 2 else pe[2]  # la coleta
        if y < 7 and (x == 1 + y or x == 6 - y):
            return cu[3] if y % 2 == 0 else cu[2]
        if y == 7:
            return cu[2] if x not in (3, 4) else cu[4]
        return camisa(x, y, w, h)

    P.pintar("cuerpo", "espalda", espalda)
    for cara in ("der", "izq"):
        P.pintar("cuerpo", cara, lambda x, y, w, h: cu[2] if y == 7 else camisa(x, y, w, h))
    P.pintar("cuerpo", "arriba", lambda x, y, w, h: cfg["camisa"][3])
    P.pintar("cuerpo", "abajo", lambda x, y, w, h: cfg["pantalon"][1])

    # el delantal (capa de encima): peto con correas, cinto, bolsillo; quemaduras; el de Tobin, enorme y tiznado
    quemado = {(5, 9), (2, 4), (6, 5)}

    def delantal(x, y, w, h):
        if i == "ulrik" and y < 3:
            return None  # el manto lo tapa
        if y < 2:
            return cu[4] if x in (1, 6) else None
        if y == 2:
            return cu[3] if 1 <= x <= 6 else None
        if y == 7:
            if i == "brenna":
                return "#a08a62" if (x + y) % 2 else "#86704c"  # cinto de cuerda
            if x in (3, 4):
                return HIERRO[3] if x == 3 else HIERRO[2]
            return cu[1]
        if i == "brenna" and y == 8 and x == 4:
            return HIERRO[3]  # el anzuelo
        if 8 <= y <= 10 and 1 <= x <= 3:
            if y == 8:
                return HIERRO[2] if x == 2 else cu[4]
            return cu[3] if x != 3 else cu[2]
        if i == "tobin" and (x + 2 * y) % 7 == 0:
            return cfg["hollin"]
        if (x, y) in quemado:
            return cu[0]
        if x in (0, w - 1):
            return mezcla(cuero(x, y, w, h), cu[0], 0.35)
        return cuero(x, y, w, h)

    P.pintar("cuerpo_f", "frente", delantal)

    def delantal_lado(x, y, w, h):
        if i == "ulrik" and y < 3:
            return cfg["piel_manto"][3] if (x + y) % 2 else cfg["piel_manto"][2]
        if i == "tobin" and y < 2:
            return cfg["panuelo"][3]
        if y == 7:
            return cu[1]
        if y < 2 and x in (1, 2):
            return cu[4] if y == 0 else None
        return None

    P.pintar("cuerpo_f", "der", delantal_lado)
    P.pintar("cuerpo_f", "izq", delantal_lado)

    def capa_arriba(x, y, w, h):
        if i == "ulrik":
            return cfg["piel_manto"][4] if (x + y) % 3 == 0 else cfg["piel_manto"][3]
        return cu[4] if x in (1, 6) else None

    P.pintar("cuerpo_f", "arriba", capa_arriba)

    def capa_espalda(x, y, w, h):
        if i == "ulrik" and y < 6:
            m = cfg["piel_manto"]
            return m[3] if (x * 3 + y) % 4 else m[1] if y == 5 else m[4]
        return None

    P.pintar("cuerpo_f", "espalda", capa_espalda)
    P.pintar("cuerpo_f", "abajo", lambda x, y, w, h: None)
    if i == "ulrik":
        # el manto de piel por delante, sobre los hombros (deja ver las trenzas)
        def manto(x, y, w, h):
            m = cfg["piel_manto"]
            if y < 3 and x not in (2, 3, 4, 5):
                return m[4] if (x + y) % 3 == 0 else m[3]
            if y == 3 and x in (0, 7):
                return m[2]
            r = delantal(x, y, w, h) if y >= 3 else None
            return r

        P.pintar("cuerpo_f", "frente", manto)


def _brazos(cfg, P):
    i = cfg["id"]
    pi, cu, ca = cfg["piel"], cfg["cuero"], cfg["camisa"]
    camisa = tela(ca, cfg["semilla"] + 4, pliegue=3, grano=0.10)

    def brazo(derecho):
        def f(x, y, w, h):
            if i == "hagen":
                # manga larga hasta la muneca con un brazal de cuero; la izquierda con guante
                if y < 7:
                    return camisa(x, y + 2, w, h)
                if y <= 9:
                    return cu[4] if y == 7 else cu[3] if y == 8 else cu[2]
                if not derecho:
                    return cu[2] if y == 10 else cu[1]
                return pi[3] if y == 10 else pi[2]
            if i == "brenna":
                # sin mangas: el hombro con la costura y el brazo desnudo; un ancla tatuada en el derecho
                if y == 0:
                    return ca[3]
                if derecho and (x, y) in ((1, 3), (1, 4), (1, 5), (0, 5), (2, 5), (1, 2)):
                    return "#2c3c5a"
                if y in (8, 9):
                    return cu[3] if y == 8 else cu[2]  # la muneca vendada
                return pi[2] if x == 0 else pi[4] if (x == 1 and y in (2, 3, 6)) else pi[3]
            if i == "ulrik":
                if y < 3:
                    return camisa(x, y + 2, w, h)
                if y == 4:
                    return HIERRO[3] if x % 2 else HIERRO[2]  # el brazalete
                if 5 <= y <= 8 and (x + y) % 3 == 0:
                    return cfg["tatuaje"]
                if not derecho and y >= 9:
                    return cu[3] if y == 9 else cu[2]
                return pi[2] if x == 0 else pi[3]
            # tobin: remangado, tizne en el antebrazo, guantes enormes
            if y < 5:
                return camisa(x, y + 2, w, h)
            if y == 5:
                return ca[4] if (x + y) % 2 else ca[3]
            if y >= 8:
                return cu[4] if y == 8 else cu[3] if y < 11 else cu[2]
            return cfg["hollin"] if (x, y) == (2, 7) else pi[2] if x == 0 else pi[3]
        return f

    def manga(derecho):
        def f(x, y, w, h):
            if i == "hagen" and y == 6:
                return ca[4] if x % 2 else ca[3]
            if i == "ulrik" and y < 3:
                return cfg["piel_manto"][3] if (x + y) % 2 else cfg["piel_manto"][2]
            if i == "tobin" and y in (4, 5):
                return ca[4] if y == 4 else ca[3]
            if i == "tobin" and y == 8:
                return cu[4]  # el borde del guante, ancho
            return None
        return f

    for b, d in (("brazo_d", True), ("brazo_i", False)):
        for cara in ("der", "frente", "izq", "espalda"):
            P.pintar(b, cara, brazo(d))
            P.pintar(b + "_f", cara, manga(d))
        P.pintar(b, "arriba", lambda x, y, w, h: ca[3] if i != "brenna" else ca[2])
        P.pintar(b, "abajo", lambda x, y, w, h, d=d: cu[1] if (i == "tobin" or (i == "hagen" and not d)) else pi[2])
        P.pintar(b + "_f", "arriba", lambda x, y, w, h: cfg["piel_manto"][4] if i == "ulrik" else None)
        P.pintar(b + "_f", "abajo", lambda x, y, w, h: None)


def _piernas(cfg, P):
    i = cfg["id"]
    pantalon = tela(cfg["pantalon"], cfg["semilla"] + 5, pliegue=4, grano=0.10)
    cu, bo = cfg["cuero"], cfg["bota"]
    cuero = ruido(cu[2:5], cfg["semilla"] + 6, grano=0.32, base=1.2)
    alta = 6 if i == "brenna" else 7 if i == "ulrik" else 8

    def pierna(x, y, w, h):
        if i == "ulrik" and 3 <= y < 7 and (x + y) % 3 == 0:
            return cu[2]  # las envueltas cruzadas
        if y >= alta:
            if y == alta:
                return bo[3] if i != "ulrik" else cfg["piel_manto"][4]
            if i == "brenna" and y == alta + 2 and x == 1:
                return HIERRO[2]
            return bo[2] if y < 11 else bo[1]
        return pantalon(x, y, w, h)

    def faldon(centro):
        def f(x, y, w, h):
            largo = 8 if i == "tobin" else 5
            if y > largo:
                return None
            if y == largo:
                return cu[1] if (x + y) % 2 else cu[2]
            if x == centro:
                return cu[2]
            if i == "tobin" and (x + y) % 5 == 0:
                return cfg["hollin"]
            return cuero(x + 2, y + 9, 8, 16)
        return f

    for pie, centro in (("pierna_d", 3), ("pierna_i", 0)):
        for cara in ("der", "frente", "izq", "espalda"):
            P.pintar(pie, cara, pierna)
        P.pintar(pie, "arriba", lambda x, y, w, h: cfg["pantalon"][2])
        P.pintar(pie, "abajo", lambda x, y, w, h: bo[0])
        P.pintar(pie + "_f", "frente", faldon(centro))
        for cara in ("der", "izq", "espalda", "arriba", "abajo"):
            P.pintar(pie + "_f", cara, lambda x, y, w, h: None)


def piel(cfg):
    P = Piel(brazos=4)
    _caras(cfg, P)
    _cabeza(cfg, P)
    _cuerpo(cfg, P)
    _brazos(cfg, P)
    _piernas(cfg, P)
    return P


# ---------------------------------------------------------------- el retrato

def _e(cx, cy, rx, ry, x, y):
    return ((x + 0.5 - cx) / rx) ** 2 + ((y + 0.5 - cy) / ry) ** 2


def _tono(rampa, t):
    return rampa[int(np.clip(round(t), 0, len(rampa) - 1))]


def retrato(cfg):
    i = cfg["id"]
    W = H = 64
    L = Lienzo(W, H)
    r = np.random.default_rng(cfg["semilla"])
    grano = r.random((H, W))
    pi, pe, cu, ca = cfg["piel"], cfg["pelo"], cfg["cuero"], cfg["camisa"]
    luz = cfg["luz"]
    f0, f1 = cfg["fondo"]
    # ---- el fondo: oscuro con el resplandor de la fragua abajo a la izquierda (el de Ulrik, frio arriba: la nieve)
    for y in range(H):
        for x in range(W):
            d = math.hypot((x - 2) / 44, (y - 70) / 40)
            t = max(0.0, 1 - d)
            c = mezcla(f0, "#3a1406", t * 0.85)
            if i in ("ulrik", "brenna"):
                c = mezcla(c, f1, max(0.0, 1 - y / 40) * 0.55)
            if grano[y, x] < 0.07:
                c = mezcla(c, "#000000", 0.3)
            L.px(x, y, c)
    if i == "ulrik":
        for (x, y) in ((50, 6), (58, 14), (44, 18), (61, 4), (54, 26), (40, 8), (62, 30)):
            L.px(x, y, "#c8d4e4")
    cx = 32
    ancho_cabeza = {"hagen": 11.0, "brenna": 10.2, "ulrik": 12.2, "tobin": 10.4}[i]
    alto_cabeza = {"hagen": 15.6, "brenna": 15.0, "ulrik": 16.0, "tobin": 14.4}[i]
    cy = 24 if i != "tobin" else 25
    # ---- hombros y pecho
    for y in range(42, H):
        ancho = (24 if i == "ulrik" else 21 if i != "tobin" else 19) + (y - 42) * 0.75
        for x in range(W):
            dx = x + 0.5 - cx
            if abs(dx) > ancho:
                continue
            t = 2.6 - abs(dx) / ancho * 1.3 + (0.4 if dx < 0 else 0)
            if (abs(dx) * 0.7 + y * 0.3) % 6 < 1:
                t -= 0.8
            c = _tono(ca, t)
            if i == "brenna" and abs(dx) > 9:
                c = _tono(pi, t + 0.4)  # los hombros desnudos
            if dx < -ancho + 4:
                c = mezcla(c, luz[2], 0.35)
            L.px(x, y, c)
    if i == "ulrik":
        # el manto de piel sobre los hombros
        m = cfg["piel_manto"]
        for y in range(40, H):
            for x in range(W):
                dx = x + 0.5 - cx
                ancho = 26 + (y - 40) * 0.8
                if abs(dx) > ancho or abs(dx) < 9 - (y - 40) * 0.1 or y > 54 + abs(dx) * 0.2:
                    continue
                k = 2.4 + (grano[y, x] - 0.5) * 2.6 - (0.6 if (x + y) % 4 == 0 else 0)
                c = _tono(m, k)
                if dx < -ancho + 5:
                    c = mezcla(c, luz[2], 0.3)
                L.px(x, y, c)
    if i == "brenna":
        # la trenza cobriza que cae sobre el hombro de la derecha (su izquierdo), con su cinta
        for k in range(22):
            y = 30 + k
            tx = cx + 11 + int(k * 0.25)
            for x in range(tx - 1, tx + 2):
                L.px(x, y, _tono(pe, 3 + (1 if (x + y) % 2 else -0.5)))
            if k == 18:
                for x in range(tx - 1, tx + 2):
                    L.px(x, y, cfg["panuelo"][3])
        # el tatuaje del ancla en el hombro de la izquierda (su derecho)
        for (x, y) in ((14, 52), (14, 53), (14, 54), (13, 55), (15, 55), (13, 52), (15, 52), (14, 51)):
            L.px(x, y, "#2c3c5a")
    # las correas del delantal
    for y in range(44, H):
        for lado in (-1, 1):
            xc = cx + lado * (11 - (y - 44) * 0.18)
            for x in range(int(xc - 2.5), int(xc + 3)):
                c = _tono(cu, 3.4 - abs(x + 0.5 - xc) * 0.9)
                if lado < 0:
                    c = mezcla(c, luz[3], 0.18)
                L.px(x, y, c)
    if i == "tobin":
        # el panuelo rojo al cuello
        pa = cfg["panuelo"]
        for y in range(40, 49):
            for x in range(cx - 9, cx + 10):
                dx = x + 0.5 - cx
                if abs(dx) <= 9 - (y - 40) * 0.9:
                    L.px(x, y, _tono(pa, 3 - (y - 40) * 0.25 + (0.6 if (x + y) % 4 == 0 else 0)))
    # ---- el cuello
    for y in range(36, 46):
        for x in range(cx - 6, cx + 6):
            if i == "tobin" and y >= 40:
                continue
            L.px(x, y, pi[2] if x < cx - 2 else pi[1])
    # ---- el pelo de detras (melenas)
    if i in ("hagen", "ulrik", "brenna"):
        for y in range(6, 46):
            for x in range(W):
                e = _e(cx, cy + 2, ancho_cabeza + (3.5 if i == "ulrik" else 2.2), alto_cabeza + 6, x, y)
                if e > 1:
                    continue
                if i == "brenna" and y > cy + 4 and abs(x + 0.5 - cx) < ancho_cabeza + 0.5:
                    continue
                k = 2.2 + (1 if (x + int(y * 0.4)) % 3 == 0 else 0) - max(0, y - 34) * 0.05
                c = _tono(pe, k)
                if x < cx - ancho_cabeza:
                    c = mezcla(c, luz[3], 0.25)
                L.px(x, y, c)
    # ---- la cabeza
    for y in range(4, 42):
        for x in range(W):
            e = _e(cx, cy, ancho_cabeza, alto_cabeza, x, y)
            if e > 1:
                continue
            dx, dy = x + 0.5 - cx, y + 0.5 - cy
            t = 3.0 - dy / 14.0 * 0.6 + dx / 12.0 * 0.4 - max(0.0, e - 0.7) * 2.2
            c = _tono(pi, t)
            if dx < -ancho_cabeza + 2.5:
                c = mezcla(c, luz[3], 0.4)
            elif dx < -ancho_cabeza + 4.5:
                c = mezcla(c, luz[2], 0.14)
            if i == "tobin" and grano[y, x] < 0.12 and y > cy - 4:
                c = mezcla(c, cfg["hollin"], 0.45)
            if i == "brenna" and grano[y, x] < 0.06 and cy - 2 < y < cy + 8:
                c = mezcla(c, "#a0664a", 0.6)  # pecas
            L.px(x, y, c)
    # las orejas
    for lado in (-1, 1):
        ox = cx + lado * (ancho_cabeza - 0.5)
        for y in range(cy - 3, cy + 6):
            for x in range(int(ox - 2), int(ox + 2)):
                if _e(ox, cy + 1, 2.2, 4.0, x, y) <= 1:
                    L.px(x, y, pi[3] if lado > 0 else mezcla(pi[3], luz[3], 0.4))
    # ---- el pelo de arriba
    if i == "hagen":
        # la frente despejada y la melena gris peinada hacia atras
        for y in range(4, 22):
            for x in range(W):
                if _e(cx, cy, ancho_cabeza + 0.6, alto_cabeza + 0.6, x, y) > 1:
                    continue
                dx = x + 0.5 - cx
                limite = 9 + abs(dx) * 0.55
                if y > limite:
                    continue
                L.px(x, y, _tono(pe, 3 + (0.8 if (x + y) % 3 == 0 else -0.3)))
    elif i == "brenna":
        pa = cfg["panuelo"]
        for y in range(5, 17):
            for x in range(W):
                if _e(cx, cy, ancho_cabeza + 1, alto_cabeza + 1, x, y) > 1:
                    continue
                L.px(x, y, _tono(pa, 3.2 - (y - 5) * 0.12 + (0.7 if (x * 2 + y) % 7 == 0 else 0)))
        # mechones cobrizos que asoman bajo el panuelo
        for x in range(cx - 11, cx + 11):
            if abs(x + 0.5 - cx) > 6:
                for y in range(16, 22 + (x % 3)):
                    L.px(x, y, _tono(pe, 3 + (0.8 if x % 2 else 0)))
        # el nudo del panuelo, a un lado
        for (x, y) in ((cx + 10, 8), (cx + 11, 9), (cx + 12, 9), (cx + 12, 10), (cx + 13, 11), (cx + 11, 10)):
            L.px(x, y, pa[4] if (x + y) % 2 else pa[3])
    elif i == "ulrik":
        for y in range(4, 16):
            for x in range(W):
                if _e(cx, cy, ancho_cabeza + 1, alto_cabeza + 1, x, y) > 1:
                    continue
                if y > 11 + abs(x + 0.5 - cx) * 0.3:
                    continue
                L.px(x, y, _tono(pe, 3 + (0.9 if (x + 2 * y) % 5 == 0 else 0)))
    else:
        # tobin: greñas negras revueltas y las gafas de laton subidas en la frente
        for y in range(4, 19):
            for x in range(W):
                if _e(cx, cy - 1, ancho_cabeza + 1.6, alto_cabeza, x, y) > 1:
                    continue
                if y > 14 + ((x * 7) % 5) * 0.8 and abs(x + 0.5 - cx) < 8:
                    continue
                L.px(x, y, _tono(pe, 2.5 + (1 if (x * 3 + y) % 4 == 0 else 0)))
        for (x, y) in ((cx - 12, 6), (cx + 11, 5), (cx - 4, 2), (cx + 3, 3), (cx + 8, 3), (cx - 9, 4)):
            L.px(x, y, pe[2])
        for lado in (-1, 1):
            gx = cx + lado * 5
            for y in range(12, 17):
                for x in range(gx - 3, gx + 3):
                    d = math.hypot(x + 0.5 - gx, y + 0.5 - 14.5)
                    if d <= 2.9:
                        L.px(x, y, LATON[3] if d > 2.0 else "#6a9aa8" if d > 1.0 else "#bcd8e0")
        for x in range(cx - 13, cx + 13):
            if not (cx - 8 <= x <= cx - 2 or cx + 2 <= x <= cx + 8):
                L.px(x, 14, cu[1])
        L.px(cx - 1, 14, LATON[1]).px(cx, 14, LATON[1])
    # ---- las cejas, los ojos, la nariz y la boca
    ojo_y = cy - 1
    for lado in (-1, 1):
        ox = cx + lado * 4.5
        bx = int(round(ox))
        ceja = pe[4] if i == "hagen" else pe[2] if i != "tobin" else pe[1]
        for k in range(-2, 3):
            L.px(bx + k, ojo_y - 3 - (1 if i == "tobin" else 0) + (1 if (k == 2 * lado) else 0)
                 - (1 if i == "brenna" and lado > 0 and k == -2 * lado else 0), ceja)
            if i in ("hagen", "ulrik"):
                L.px(bx + k, ojo_y - 2, ceja)
        for y in range(ojo_y - 1, ojo_y + 2):
            for x in range(bx - 2, bx + 2):
                L.px(x, y, mezcla(pi[1], "#1a0e08", 0.3))
        big = i == "tobin"
        L.px(bx - 1, ojo_y, "#d8d0c4").px(bx, ojo_y, cfg["ojo"]).px(bx + 1 if lado > 0 else bx - 2, ojo_y, "#c8c0b4")
        if big:
            L.px(bx - 1, ojo_y - 1, "#d8d0c4").px(bx, ojo_y - 1, cfg["ojo"]).px(bx, ojo_y + 1, mezcla(cfg["ojo"], "#000000", 0.4))
        L.px(bx, ojo_y, cfg["ojo"])
        L.px(bx if lado > 0 else bx - 1, ojo_y, luz[4] if lado < 0 else "#e4ecf4")
    # la nariz
    for y in range(ojo_y + 1, ojo_y + 9):
        for x in range(cx - 2, cx + 3):
            w = 1.2 + (y - ojo_y) * 0.2 + (0.4 if i == "ulrik" else 0)
            if abs(x + 0.5 - (cx + 0.5)) <= w:
                L.px(x, y, pi[4] if x >= cx else pi[3])
    L.px(cx - 2, ojo_y + 8, pi[1]).px(cx + 2, ojo_y + 8, pi[1])
    # la barba (Hagen y Ulrik) o la boca a la vista
    boca_y = ojo_y + 11
    if i in ("hagen", "ulrik"):
        largo = 12 if i == "hagen" else 20
        for y in range(ojo_y + 6, min(H, boca_y + largo)):
            for x in range(W):
                dx = x + 0.5 - cx
                ancho = ancho_cabeza - 0.5 - max(0, y - (boca_y + 2)) * (0.55 if i == "hagen" else 0.25)
                if abs(dx) > ancho or ancho <= 0:
                    continue
                if y < boca_y - 1 and abs(dx) < ancho - 3:
                    continue  # las mejillas a la vista
                k = 2.6 + (1 if (int(dx * 0.9 + 100) + y // 3) % 3 == 0 else -0.4) - max(0, y - boca_y - 6) * 0.05
                c = _tono(pe, k)
                if dx < -ancho + 2.5:
                    c = mezcla(c, luz[3], 0.25)
                L.px(x, y, c)
        if i == "ulrik":
            # las dos trenzas con sus anillas
            for lado in (-1, 1):
                tx = cx + lado * 4
                for y in range(boca_y + 6, H):
                    for x in range(tx - 1, tx + 2):
                        L.px(x, y, pe[3] if (x + y) % 2 else pe[2])
                for yy in (boca_y + 12, boca_y + 18):
                    for x in range(tx - 2, tx + 3):
                        L.px(x, yy, HIERRO[4] if x == tx - 1 else HIERRO[2])
        # el bigote poblado encima de la boca y la boca entre la barba
        for x in range(cx - 6, cx + 7):
            dx = abs(x + 0.5 - cx)
            y0 = boca_y - 1 + (1 if dx > 4 else 0)
            L.px(x, y0, pe[4] if (x + y0) % 3 == 0 else pe[3])
            L.px(x, y0 + 1, pe[3])
        for x in range(cx - 2, cx + 2):
            L.px(x, boca_y + 2, "#3a1a10")
    else:
        if i == "brenna":
            # la sonrisa torcida: sube por un lado
            for k, x in enumerate(range(cx - 3, cx + 4)):
                L.px(x, boca_y - (1 if k >= 5 else 0), "#6a2a22")
            L.px(cx + 4, boca_y - 2, pi[2])
        else:
            # tobin: la sonrisa abierta de entusiasmo, con los dientes arriba y las comisuras hacia arriba
            for x in range(cx - 3, cx + 3):
                L.px(x, boca_y, "#e8e0d0" if cx - 3 < x < cx + 2 else "#4a1410")
                L.px(x, boca_y + 1, "#4a1410" if cx - 3 < x < cx + 2 else pi[2])
            for x in range(cx - 2, cx + 1):
                L.px(x, boca_y + 2, "#4a1410")
            L.px(cx - 4, boca_y - 1, "#4a1410").px(cx + 3, boca_y - 1, "#4a1410")
    if i == "hagen":
        # la cicatriz de la mejilla y las arrugas de la frente
        for (x, y) in ((cx + 7, ojo_y + 2), (cx + 7, ojo_y + 3), (cx + 6, ojo_y + 4), (cx + 6, ojo_y + 5)):
            L.px(x, y, "#c4846a")
        for x in range(cx - 6, cx + 6):
            if x % 3:
                L.px(x, ojo_y - 6, mezcla(pi[3], pi[1], 0.35))
                L.px(x + 1, ojo_y - 8, mezcla(pi[3], pi[1], 0.25))
    # brasas flotando
    for (x, y, c) in ((6, 40, luz[3]), (9, 52, luz[4]), (4, 30, luz[2]), (12, 60, luz[3]), (56, 49, luz[1]), (2, 48, luz[2])):
        L.px(x, y, c)
    return L


def guardar():
    for cfg in TODOS:
        piel(cfg).guardar(os.path.join(ASSETS, "textures", "entity", cfg["id"] + ".png"))
        retrato(cfg).guardar(os.path.join(ASSETS, "textures", "gui", "retratos", cfg["id"] + ".png"))
    print("herreros de Morvain: pieles y retratos de", ", ".join(c["id"] for c in TODOS))


if __name__ == "__main__":
    guardar()
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        for cfg in TODOS:
            p = piel(cfg)
            p.vista().save(os.path.join(d, cfg["id"] + "_piel.png"))
            retrato(cfg).imagen().resize((64 * 6, 64 * 6), 0).save(os.path.join(d, cfg["id"] + "_retrato.png"))
        print("vistas en", d)
