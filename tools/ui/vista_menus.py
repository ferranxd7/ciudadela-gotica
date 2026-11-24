"""Vista previa de los menus del mod (santuario, subir de nivel), los frascos del HUD y sus avisos, tal como los dibuja
Java (mismas medidas y formulas), sobre una captura del juego. La letra de Minecraft se saca del .jar del cliente si se
le dice donde esta (si no, una aproximada).

   python3 tools/ui/vista_menus.py fondo.png escala carpeta [client-extra.jar]
"""
import io
import json
import os
import sys
import zipfile

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import numpy as np
from PIL import Image

import arte
import fuente
import hud
import menu

AH = hud.montar()
AM = menu.montar()
FS = {n: fuente.generar(n, px) for n, px in fuente.FUENTES.items()}

CLARO, ORO, LUZ, APAGADO, VERDE = 0xEADBC4, 0xE2BF62, 0xFFF2C4, 0xB8A888, 0x9BE08A


def rgb(h):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255)


# ---------------------------------------------------------------- la letra de Minecraft
class LetraMc:
    def __init__(self, jar):
        self.glifos = {}
        if not jar or not os.path.exists(jar):
            return
        z = zipfile.ZipFile(jar)
        d = json.loads(z.read("assets/minecraft/font/include/default.json"))
        for p in d["providers"]:
            if p.get("type") != "bitmap":
                continue
            ruta = "assets/minecraft/textures/" + p["file"].split(":")[1]
            im = Image.open(io.BytesIO(z.read(ruta))).convert("RGBA")
            filas = p["chars"]
            ch = im.height // len(filas)
            alto = p.get("height", 8)
            for j, fila in enumerate(filas):
                cw = im.width // len(fila)
                for i, c in enumerate(fila):
                    if c in ("\u0000", " ") or c in self.glifos:
                        continue
                    celda = im.crop((i * cw, j * ch, (i + 1) * cw, (j + 1) * ch))
                    bb = celda.split()[3].point(lambda v: 255 if v > 0 else 0).getbbox()
                    if bb is None:
                        continue
                    ancho = bb[2]
                    esc = alto / ch
                    self.glifos[c] = (celda, int(0.5 + ancho * esc) + 1, p["ascent"], esc)

    def ancho(self, s):
        if not self.glifos:
            return 6 * len(s)
        return sum(4 if c == " " else self.glifos.get(c, (None, 6))[1] for c in s)

    def escribir(self, L, s, x, y, color, sombra=True):
        im = L.imagen()
        if not self.glifos:
            from PIL import ImageDraw, ImageFont
            d = ImageDraw.Draw(im)
            d.fontmode = "1"
            d.text((x, y - 2), s, fill=color + (255,), font=ImageFont.load_default())
            L.a[:] = np.asarray(im)
            return
        pasadas = ([((1, 1), tuple(v // 4 for v in color))] if sombra else []) + [((0, 0), color)]
        for (dx, dy), col in pasadas:
            cx = x + dx
            for c in s:
                if c == " ":
                    cx += 4
                    continue
                g = self.glifos.get(c)
                if g is None:
                    cx += 6
                    continue
                celda, av, asc, esc = g
                if esc != 1:
                    celda = celda.resize((max(1, int(celda.width * esc)), max(1, int(celda.height * esc))), 0)
                a = np.asarray(celda)
                tinta = np.zeros_like(a)
                tinta[..., 0], tinta[..., 1], tinta[..., 2] = col
                tinta[..., 3] = a[..., 3]
                im.alpha_composite(Image.fromarray(tinta), (cx, y + dy + 7 - asc))
                cx += av
        L.a[:] = np.asarray(im)

    def partir(self, s, w):
        lineas, actual = [], ""
        for palabra in s.split(" "):
            prueba = (actual + " " + palabra).strip()
            if self.ancho(prueba) <= w or not actual:
                actual = prueba
            else:
                lineas.append(actual)
                actual = palabra
        if actual:
            lineas.append(actual)
        return lineas


MC = None


# ---------------------------------------------------------------- piezas (como Pix y Panel)
def TH(n):
    x, y, w, h = AH.sitios[n]
    return AH.L.recorte(x, y, w, h)


def TM(n):
    x, y, w, h = AM.sitios[n]
    return AM.L.recorte(x, y, w, h)


def pegar(L, sp, x, y, alfa=1.0):
    s = sp.copia()
    if alfa < 1:
        s.a[..., 3] = (s.a[..., 3] * alfa).astype(np.uint8)
    L.pegar(s, x, y)


def mosaico(L, sp, x, y, w, h):
    for j in range(0, h, sp.h):
        for i in range(0, w, sp.w):
            L.pegar(sp.recorte(0, 0, min(sp.w, w - i), min(sp.h, h - j)), x + i, y + j)


def nueve(L, pre, x, y, w, h):
    ai = TM(pre + "_ai")
    e, f = ai.w, ai.h
    mosaico(L, TM(pre + "_c"), x + e, y + f, w - 2 * e, h - 2 * f)
    mosaico(L, TM(pre + "_a"), x + e, y, w - 2 * e, f)
    mosaico(L, TM(pre + "_b"), x + e, y + h - f, w - 2 * e, f)
    mosaico(L, TM(pre + "_i"), x, y + f, e, h - 2 * f)
    mosaico(L, TM(pre + "_d"), x + w - e, y + f, e, h - 2 * f)
    L.pegar(ai, x, y)
    L.pegar(TM(pre + "_ad"), x + w - e, y)
    L.pegar(TM(pre + "_bi"), x, y + h - f)
    L.pegar(TM(pre + "_bd"), x + w - e, y + h - f)


def losa(L, x, y, w, h):
    nueve(L, "panel", x, y, w, h)


def hueco(L, x, y, w, h):
    nueve(L, "hueco", x, y, w, h)


def fila(L, x, y, w):
    L.pegar(TM("fila_izq"), x, y)
    mosaico(L, TM("fila_med"), x + 10, y, w - 20, 18)
    L.pegar(TM("fila_der"), x + w - 10, y)


def fill(L, x0, y0, x1, y1, c, a=255):
    if x1 > x0 and y1 > y0:
        L.pegar(arte.Lienzo(x1 - x0, y1 - y0, rgb(c) + (a,)), x0, y0)


def franja(L, x0, x1, y0, y1, c, alfa):
    w = x1 - x0
    n = max(48, w // 2)
    for i in range(n):
        k = 1 - abs((i + 0.5) / n * 2 - 1)
        k = min(1, k * 1.9)
        k = k * k * (3 - 2 * k)
        a = int(round(255 * alfa * k))
        if a >= 3:
            fill(L, x0 + w * i // n, y0, x0 + w * (i + 1) // n, y1, c, a)


def raya(L, x0, x1, y):
    franja(L, x0, x1, y, y + 1, 0xC0963C, 1)
    franja(L, x0, x1, y + 1, y + 2, 0x4A3312, 1)


def tres(L, a, b, c, x, y, w):
    L.pegar(TH(a), x, y)
    mosaico(L, TH(b), x + TH(a).w, y, w - TH(a).w - TH(c).w, TH(b).h)
    L.pegar(TH(c), x + w - TH(c).w, y)


def tecla(L, k, texto, x, y, color):
    k = k.upper()
    wk = max(13, MC.ancho(k) + 8)
    tres(L, "tecla_izq", "tecla_med", "tecla_der", x, y, wk)
    MC.escribir(L, k, x + (wk - MC.ancho(k)) // 2 + 1, y + 2, rgb(0xF6E3A0), False)
    MC.escribir(L, texto, x + wk + 4, y + 3, rgb(color))
    return x + wk + 4 + MC.ancho(texto)


def gotica(L, n, texto, x, yArriba, color, borde=(5, 4, 8)):
    f = FS[n]
    baja = 10 if n == "gotica" else 6
    ycelda = yArriba + baja + (7 - f["ascent"])
    im = L.imagen()
    for dx, dy in ((-1, 0), (1, 0), (0, -1), (0, 1), (1, 1), (0, 2)):
        fuente.escribir(im, f, texto, x + dx, ycelda + dy, borde + (255,), 1)
    fuente.escribir(im, f, texto, x, ycelda, rgb(color) + (255,), 1)
    L.a[:] = np.asarray(im)


def anchoG(n, texto):
    return fuente.ancho(FS[n], texto, 1)


# ---------------------------------------------------------------- el HUD: frascos y avisos
def frascos(L, H, vida=(2, 3), mana=(0, 1)):
    y = H - 4 - 24
    col = 0
    for i, (quedan, total) in enumerate((vida, mana)):
        if total <= 0:
            continue
        x = 4 + col * 28
        col += 1
        hueco(L, x, y, 24, 24)
        nombre = "frasco_" + ("mana" if i else "vida") + ("" if quedan > 0 else "_vacio")
        L.pegar(TM(nombre), x + 4, y + 3)
        n = str(quedan)
        MC.escribir(L, n, x + 22 - MC.ancho(n), y + 14, rgb(0xF6E3A0 if quedan > 0 else 0xB03038))
        k = "V" if i else "Z"
        MC.escribir(L, k, x + 12 - MC.ancho(k) // 2, y - 9, rgb(0xB8A888))


def aviso_tecla(L, W, y, antes, k, despues):
    wa = MC.ancho(antes) + 5
    wk = max(13, MC.ancho(k) + 8)
    wd = MC.ancho(despues) + 5
    ancho = wa + wk + wd + 28
    x = (W - ancho) // 2
    tres(L, "aviso_izq", "aviso_med", "aviso_der", x, y, ancho)
    cx = x + 14
    MC.escribir(L, antes, cx, y + 6, rgb(CLARO))
    cx += wa
    tres(L, "tecla_izq", "tecla_med", "tecla_der", cx, y + 3, wk)
    MC.escribir(L, k, cx + (wk - MC.ancho(k)) // 2 + 1, y + 5, rgb(0xF6E3A0), False)
    cx += wk + 5
    MC.escribir(L, despues, cx, y + 6, rgb(CLARO))


def aviso_corto(L, W, H, texto):
    tw = MC.ancho(texto)
    medio = min(W // 2 - 4, tw // 2 + 60)
    y = H - 106
    franja(L, W // 2 - medio, W // 2 + medio, y, y + 17, 0x050408, 0.78)
    franja(L, W // 2 - medio, W // 2 + medio, y, y + 1, 0x96702A, 0.9)
    franja(L, W // 2 - medio, W // 2 + medio, y + 16, y + 17, 0x96702A, 0.9)
    MC.escribir(L, texto, W // 2 - tw // 2, y + 5, rgb(CLARO))


def cartel_santuario(L, W, H, titulo, sub):
    tw1 = anchoG("gotica", titulo)
    alto = 17
    cy = int(round(H * 0.30))
    arriba = cy - alto // 2
    y0, y1 = arriba - 12, arriba + alto + 26
    franja(L, 0, W, y0, y1, 0x050408, 0.72)
    franja(L, W // 8, W - W // 8, y0, y0 + 1, 0x96702A, 0.9)
    franja(L, W // 8, W - W // 8, y1 - 1, y1, 0x96702A, 0.9)
    franja(L, W // 5, W - W // 5, y0 + 2, y0 + 3, 0x4A3312, 0.8)
    franja(L, W // 5, W - W // 5, y1 - 3, y1 - 2, 0x4A3312, 0.8)
    fl = TH("floritura")
    L.pegar(fl, W // 2 - tw1 // 2 - 10 - 72, cy - 5)
    L.pegar(fl.espejo_h() if hasattr(fl, "espejo_h") else fl, W // 2 + tw1 // 2 + 10, cy - 5)
    gotica(L, "gotica", titulo, W // 2 - tw1 // 2, arriba, 0xBFE6F8)
    sw = anchoG("gotica_m", sub)
    gotica(L, "gotica_m", sub, W // 2 - sw // 2, arriba + alto + 7, CLARO)


# ---------------------------------------------------------------- el menu del santuario
def santuario(L, W, H, nombre, sel=0, nivel=12, ecos="12.438", vida=3, mana=1):
    FILA, CAB, PIE = 18, 54, 40
    ops = [("Subir de nivel", "Gasta tus Ecos de la Sombra para subir tus atributos."),
           ("Repartir frascos", "Decide cuántos frascos llevas de vida y cuántos de maná. Se rellenan al descansar."),
           ("Marcharse", "Vuelve al camino. Si mueres, reaparecerás aquí.")]
    w = max(208, min(anchoG("gotica_m", nombre) + 50, W - 16))
    h = CAB + len(ops) * FILA + PIE
    x = max(6, min(28, W // 24))
    y = max(4, (H - h) // 2 - 12)
    tope = min(W, x + w + 150)
    for i in range(30):
        a = int(round(170 * (1 - i / 30)))
        if a > 3:
            fill(L, tope * i // 30, 0, tope * (i + 1) // 30, H, 0x050408, a)
    losa(L, x, y, w, h)
    L.pegar(TM("santuario"), x + 12, y + 11)
    gotica(L, "gotica_m", nombre, x + 34, y + 12, 0xE9C766)
    MC.escribir(L, "Nivel %d" % nivel, x + 14, y + 34, rgb(CLARO))
    ew = MC.ancho(ecos)
    MC.escribir(L, ecos, x + w - 14 - ew, y + 34, rgb(ORO))
    L.pegar(TH("eco_0"), x + w - 14 - ew - 18, y + 29)
    raya(L, x + 10, x + w - 10, y + 47)
    for i, (texto, _) in enumerate(ops):
        fy = y + CAB + i * FILA
        if i == sel:
            fila(L, x + 8, fy, w - 16)
        MC.escribir(L, texto, x + 18, fy + 5, rgb(LUZ if i == sel else 0xC8B89A))
        if i == 1:
            x0 = x + w - 104
            L.pegar(TM("flecha_izq"), x0, fy + 5)
            L.pegar(TM("frasco_vida"), x0 + 11, fy + 1)
            MC.escribir(L, str(vida), x0 + 29, fy + 5, rgb(LUZ if i == sel else ORO))
            L.pegar(TM("frasco_mana"), x0 + 44, fy + 1)
            MC.escribir(L, str(mana), x0 + 62, fy + 5, rgb(LUZ if i == sel else ORO))
            L.pegar(TM("flecha_der"), x0 + 79, fy + 5)
    raya(L, x + 10, x + w - 10, y + CAB + len(ops) * FILA + 4)
    for i, linea in enumerate(MC.partir(ops[sel][1], w - 28)[:3]):
        MC.escribir(L, linea, x + 14, y + CAB + len(ops) * FILA + 10 + i * 9, rgb(APAGADO), False)
    ty = min(H - 16, y + h + 6)
    tx = x + 4
    tx = tecla(L, "↑↓", "Mover", tx, ty, APAGADO) + 10
    tx = tecla(L, "E", "Elegir", tx, ty, APAGADO) + 10
    tecla(L, "Esc", "Salir", tx, ty, APAGADO)


# ---------------------------------------------------------------- subir de nivel
ATRIBUTOS = [("vigor", "Vigor"), ("mente", "Mente"), ("aguante", "Aguante"), ("fuerza", "Fuerza"), ("destreza", "Destreza"),
             ("inteligencia", "Inteligencia"), ("fe", "Fe"), ("arcano", "Arcano")]


def nivel(L, W, H, base, nuevo, sel=0, ecos=12438, coste=1346, titulo="Subir de nivel",
          desc="Tu vida máxima. Lo primero que conviene subir."):
    ANCHO, ALTO, FILA, FILAS_Y, IZQ = 396, 224, 18, 40, 196
    x, y = (W - ANCHO) // 2, max(2, (H - ALTO) // 2)
    fill(L, 0, 0, W, H, 0x050408, 150)
    losa(L, x, y, ANCHO, ALTO)
    tw = anchoG("gotica", titulo)
    gotica(L, "gotica", titulo, x + (ANCHO - tw) // 2, y + 11, 0xE9C766)
    if tw + 2 * 82 < ANCHO - 30:
        fl = TH("floritura")
        L.pegar(fl, x + (ANCHO - tw) // 2 - 10 - 72, y + 14)
        L.pegar(fl.espejo_h() if hasattr(fl, "espejo_h") else fl, x + (ANCHO + tw) // 2 + 10, y + 14)
    raya(L, x + 12, x + ANCHO - 12, y + 33)
    fxi, fxd = x + 10 + IZQ - 58, x + 10 + IZQ - 16
    for i, (clave, nombre) in enumerate(ATRIBUTOS):
        fy = y + FILAS_Y + i * FILA
        if i == sel:
            fila(L, x + 10, fy, IZQ)
        L.pegar(TM("at_" + clave), x + 16, fy + 1)
        MC.escribir(L, nombre, x + 38, fy + 5, rgb(LUZ if i == sel else 0xC8B89A))
        L.pegar(TM("flecha_izq" + ("" if nuevo[i] > base[i] else "_gris")), fxi, fy + 5)
        L.pegar(TM("flecha_der"), fxd, fy + 5)
        v = str(nuevo[i])
        vw = anchoG("gotica_m", v)
        gotica(L, "gotica_m", v, (fxi + 7 + fxd) // 2 - vw // 2, fy + 3, LUZ if nuevo[i] > base[i] else ORO)
    dx, dw = x + 10 + IZQ + 10, ANCHO - 14 - (10 + IZQ + 10)
    hueco(L, dx, y + FILAS_Y, dw, 49)
    ly = y + FILAS_Y + 6
    nb, nn = sum(base) - 79, sum(nuevo) - 79

    def dato(yy, nombre, valor, color):
        MC.escribir(L, nombre, dx + 7, yy, rgb(CLARO))
        MC.escribir(L, valor, dx + dw - 7 - MC.ancho(valor), yy, rgb(color))

    dato(ly, "Nivel", "%d → %d" % (nb, nn) if nn > nb else str(nb), LUZ if nn > nb else ORO)
    dato(ly + 13, "Ecos", "{:,}".format(ecos).replace(",", "."), ORO)
    dato(ly + 26, "Coste", "{:,}".format(coste).replace(",", "."), LUZ)
    hueco(L, dx, y + FILAS_Y + 53, dw, 60)
    sy = y + FILAS_Y + 59

    def stat(yy, nombre, antes, despues):
        MC.escribir(L, nombre, dx + 7, yy, rgb(CLARO))
        if antes == despues:
            MC.escribir(L, antes, dx + dw - 7 - MC.ancho(antes), yy, rgb(ORO))
            return
        rx = dx + dw - 7 - MC.ancho(despues)
        MC.escribir(L, despues, rx, yy, rgb(VERDE))
        MC.escribir(L, " → ", rx - MC.ancho(" → "), yy, rgb(APAGADO))
        MC.escribir(L, antes, rx - MC.ancho(" → ") - MC.ancho(antes), yy, rgb(ORO))

    stat(sy, "Vida", "100", "120")
    stat(sy + 13, "Maná", "100", "100")
    stat(sy + 26, "Estamina", "100", "103")
    stat(sy + 39, "Daño de armas", "100 %", "102 %")
    for i, linea in enumerate(MC.partir(desc, dw - 4)[:3]):
        MC.escribir(L, linea, dx + 2, y + FILAS_Y + 119 + i * 9, rgb(APAGADO), False)
    BW = 84
    by = y + ALTO - 31
    for i, texto in enumerate(("Confirmar", "Volver")):
        bx = x + ANCHO - 14 - BW - (1 - i) * (BW + 6)
        tres(L, "boton_izq", "boton_med", "boton_der", bx, by, BW)
        MC.escribir(L, texto, bx + (BW - MC.ancho(texto)) // 2, by + 6, rgb(ORO))
    tx = tecla(L, "←→", "Cambiar", x + 14, by + 4, APAGADO) + 8
    tecla(L, "E", "Confirmar", tx, by + 4, APAGADO)


# ---------------------------------------------------------------- el menu del mod (R)
PESTANAS = ["Equipo", "Inventario", "Estado", "Sistema"]


def ranura(L, x, y, icono=None, elegida=False, alfa=1.0, equipado=False, cuenta=None):
    pegar(L, TM("ranura_luz" if elegida else "ranura"), x, y, alfa)
    if icono:
        pegar(L, TM(icono), x + 3, y + 3, alfa)
    if cuenta:
        MC.escribir(L, cuenta, x + 20 - MC.ancho(cuenta), y + 12, (255, 255, 255))
    if equipado:
        L.pegar(TM("equipado"), x + 1, y + 1)
    if elegida:
        L.pegar(TM("cursor_ai"), x - 2, y - 2)
        L.pegar(TM("cursor_ad"), x + 22 - 5, y - 2)
        L.pegar(TM("cursor_bi"), x - 2, y + 22 - 5)
        L.pegar(TM("cursor_bd"), x + 22 - 5, y + 22 - 5)


def marco_menu(L, W, H, pestana):
    fill(L, 0, 0, W, H, 0x050408, 176)
    paso = min(104, (W - 20) // 4)
    for i, n in enumerate(PESTANAS):
        cx = W // 2 + int(round((i - 1.5) * paso))
        w = anchoG("gotica_m", n)
        gotica(L, "gotica_m", n, cx - w // 2, 7, 0xF6E3A0 if i == pestana else 0x8A6A30)
        if i == pestana:
            L.pegar(TM("pestana"), cx - 20, 21)
    px, py, pw, ph = 8, 30, W - 16, H - 30 - 22
    losa(L, px, py, pw, ph)
    ty = H - 17
    tx = px + 4
    tx = tecla(L, "Tab", "Pestaña", tx, ty, APAGADO) + 10
    tx = tecla(L, "E", "Elegir", tx, ty, APAGADO) + 10
    if pestana == 0:
        tx = tecla(L, "Supr", "Quitar", tx, ty, APAGADO) + 10
    tecla(L, "Esc", "Cerrar", tx, ty, APAGADO)
    return px, py, pw, ph


def personaje(L, x, y, w, h):
    hueco(L, x, y, w, h)
    # un monigote donde ira el jugador
    esc = max(20, min((h - 26) * 10 // 21, w * 10 // 13, 78))
    alto = int(esc * 1.8)
    cx, pie = x + w // 2, y + h - 12
    fill(L, cx - esc // 4, pie - alto, cx + esc // 4, pie - alto + esc // 2, 0x8A7A66)
    fill(L, cx - esc // 4, pie - alto + esc // 2, cx + esc // 4, pie - esc * 3 // 4, 0x3A6A8A)
    fill(L, cx - esc // 4, pie - esc * 3 // 4, cx + esc // 4, pie, 0x2A2A5A)
    fill(L, cx - esc // 2, pie - alto + esc // 2, cx - esc // 4, pie - esc * 3 // 4, 0x8A7A66)
    fill(L, cx + esc // 4, pie - alto + esc // 2, cx + esc // 2, pie - esc * 3 // 4, 0x8A7A66)


def menu_equipo(L, W, H, lista=False):
    px, py, pw, ph = marco_menu(L, W, H, 0)
    x0 = px + 14
    fila_y = [py + 22 + i * 42 for i in range(3)]
    for (nombre, x, y) in (("Mano derecha", x0, fila_y[0]), ("Mano izquierda", x0 + 90, fila_y[0]), ("Armadura", x0, fila_y[1]),
                           ("Objetos rápidos", x0, fila_y[2])):
        MC.escribir(L, nombre, x, y - 11, rgb(APAGADO))
    ranura(L, x0, fila_y[0], "ico_espada", elegida=not lista)
    ranura(L, x0 + 25, fila_y[0], "ico_espada")
    ranura(L, x0 + 50, fila_y[0], "sil_espada")
    ranura(L, x0 + 90, fila_y[0], "ico_escudo")
    ranura(L, x0 + 115, fila_y[0], "sil_escudo")
    ranura(L, x0 + 140, fila_y[0], "sil_escudo")
    for i, n in enumerate(("yelmo", "pecho", "piernas", "botas")):
        ranura(L, x0 + i * 25, fila_y[1], ("ico_" if i == 1 else "sil_") + n, elegida=lista and i == 0)
    ranura(L, x0, fila_y[2], "frasco_vida", cuenta="3")
    ranura(L, x0 + 25, fila_y[2], "frasco_mana", cuenta="1")
    for i in range(4):
        ranura(L, x0 + (i + 2) * 25, fila_y[2], "ico_objeto" if i == 0 else "sil_objeto", cuenta="3" if i == 0 else None)
    cw = max(98, min(150, pw // 4))
    cx = px + pw - 12 - cw
    sx, sx1 = x0 + 180, cx - 10
    if sx1 - sx >= 84:
        sy = py + 14
        for i, (n, v) in enumerate((("Nivel", "12"), ("Vida", "115/130"), ("Maná", "100/106"), ("Estamina", "106"), ("Ataque", "7.2"), ("Defensa", "12"))):
            MC.escribir(L, n, sx, sy + i * 13, rgb(CLARO))
            MC.escribir(L, v, sx1 - MC.ancho(v), sy + i * 13, rgb(ORO))
    personaje(L, cx, py + 10, cw, ph - 20)
    dy = fila_y[2] + 22 + 9
    raya(L, x0 - 4, cx - 8, dy - 5)
    gotica(L, "gotica_m", "Espada de hierro", x0, dy, 0xE9C766)
    for i, linea in enumerate(("Al tener en la mano principal:", " 6 de daño de ataque", " 1.6 de velocidad de ataque")[:max(0, (py + ph - 8 - (dy + 17)) // 9)]):
        MC.escribir(L, linea, x0, dy + 17 + i * 9, rgb(APAGADO), False)
    if lista:
        x, y, w = x0 + 22 + 6, fila_y[1] - 6, 178
        filas = ["Yelmo de hierro", "Casco de tortuga"]
        losa(L, x, y, w, 12 + len(filas) * 18)
        for i, n in enumerate(filas):
            fy = y + 6 + i * 18
            if i == 0:
                fila(L, x + 5, fy, w - 10)
            L.pegar(TM("ico_yelmo"), x + 9, fy + 1)
            MC.escribir(L, n, x + 30, fy + 5, rgb(LUZ if i == 0 else CLARO))


def menu_bolsa(L, W, H):
    px, py, pw, ph = marco_menu(L, W, H, 1)
    gx, gy = px + 14, py + 40
    clases = ["todo", "espada", "pecho", "objeto", "material", "llave"]
    for i, n in enumerate(clases):
        pegar(L, TM("ranura_luz" if i == 1 else "ranura"), gx + i * 25, py + 10)
        pegar(L, TM("ico_" + n), gx + i * 25 + 3, py + 13, 1.0 if i == 1 else 0.6)
    gotica(L, "gotica_m", "Armas", gx + 6 * 25 + 6, py + 14, 0xE9C766)
    filas = max(1, (ph - 40 - 22) // 24)
    cosas = ["ico_espada", "ico_espada", "ico_escudo", "ico_hechizo", "ico_espada", "ico_material", "ico_objeto", "ico_llave", "ico_yelmo", "ico_pecho",
             "ico_botas", "ico_espada", "ico_escudo"]
    for r in range(filas):
        for c in range(9):
            k = r * 9 + c
            x, y = gx + c * 24, gy + r * 24
            if k < len(cosas):
                ranura(L, x, y, cosas[k], elegida=k == 3, equipado=k >= 10)
            else:
                pegar(L, TM("ranura"), x, y, 0.35)
    MC.escribir(L, "Bolsa: 12 / 27", gx, py + ph - 17, rgb(APAGADO))
    x = gx + 9 * 24 + 6
    y, w, h = py + 10, px + pw - 12 - x, ph - 20
    if w >= 90:
        hueco(L, x, y, w, h)
        big = TM("ico_hechizo").imagen().resize((32, 32), 0)
        im = L.imagen()
        im.alpha_composite(big, (x + 7, y + 7))
        L.a[:] = np.asarray(im)
        nombre = "Mandoble del Juramento"
        # el nombre, en dos lineas si hace falta
        partes, actual = [], ""
        for palabra in nombre.split(" "):
            prueba = (actual + " " + palabra).strip()
            if anchoG("gotica_m", prueba) <= w - 50 or not actual:
                actual = prueba
            else:
                partes.append(actual)
                actual = palabra
        partes.append(actual)
        for i, parte in enumerate(partes[:2]):
            gotica(L, "gotica_m", parte, x + 44, y + 8 + i * 15, 0xE9C766)
        ly = y + 44
        MC.escribir(L, "Armas", x + 8, ly, rgb(ORO))
        raya(L, x + 6, x + w - 6, ly + 11)
        lineas = []
        for t in ("A dos manos. Shift + clic: Raíces del Juramento.", "Ctrl + clic: Juicio de Espinas.", "Al tener en la mano principal:",
                  " 11 de daño de ataque", " 1 de velocidad de ataque"):
            lineas += MC.partir(t, w - 16)
        caben = max(0, (y + h - 32 - (ly + 17)) // 9)
        for i, linea in enumerate(lineas[:caben]):
            MC.escribir(L, linea, x + 8, ly + 17 + i * 9, rgb(APAGADO), False)
        ay = y + h - 29
        tecla(L, "E", "Equipar", x + 8, ay, CLARO)
        tecla(L, "Supr", "Tirar", x + 8, ay + 14, CLARO)


def cruz(L, H):
    x0, y0 = 4, H - 4 - 66
    L.pegar(TM("cruz_centro"), x0 + 22, y0 + 22)
    pegar(L, TM("ranura"), x0 + 22, y0, 0.55)
    pegar(L, TM("sil_hechizo"), x0 + 25, y0 + 3, 0.55)
    ranura(L, x0 + 44, y0 + 22, "ico_espada")
    ranura(L, x0, y0 + 22, "ico_escudo")
    ranura(L, x0 + 22, y0 + 44, "frasco_vida", cuenta="3")
    for i in range(3):
        L.pegar(TM("punto_luz" if i == 0 else "punto"), x0 + 44 + 3 + i * 6, y0 + 45)
    for i in range(3):
        L.pegar(TM("punto_luz" if i == 0 else "punto"), x0 + 46 + i * 6, y0 + 44 + 14)
    MC.escribir(L, "Z", x0 + 22 - 3 - MC.ancho("Z"), y0 + 44 + 12, rgb(APAGADO))
    MC.escribir(L, "Espada de hierro", x0 + 66 + 6, y0 + 29, rgb(CLARO))


if __name__ == "__main__":
    fondo, escala, out = sys.argv[1], int(sys.argv[2]), sys.argv[3]
    MC = LetraMc(sys.argv[4] if len(sys.argv) > 4 else None)
    os.makedirs(out, exist_ok=True)
    bg0 = Image.open(fondo).convert("RGBA")
    W, H = bg0.width // escala, bg0.height // escala

    def guardar(L, nombre):
        bg = bg0.copy()
        bg.alpha_composite(L.imagen().resize((W * escala, H * escala), Image.NEAREST), (0, 0))
        bg.save(os.path.join(out, nombre))
        print(nombre)

    L = arte.Lienzo(W, H)
    frascos(L, H)
    aviso_tecla(L, W, H - 80, "Pulsa", "E", "para descansar")
    aviso_corto(L, W, H, "No puedes descansar con enemigos cerca.")
    cartel_santuario(L, W, H, "SANTUARIO ENCENDIDO", "Santuario del Árbol de la Corona")
    guardar(L, "m_hud_%d.png" % escala)
    L = arte.Lienzo(W, H)
    frascos(L, H)
    santuario(L, W, H, "Santuario del Árbol de la Corona", sel=1)
    guardar(L, "m_santuario_%d.png" % escala)
    L = arte.Lienzo(W, H)
    nivel(L, W, H, [10] * 8, [12, 10, 11, 10, 11, 10, 10, 10], sel=0)
    guardar(L, "m_nivel_%d.png" % escala)
    L = arte.Lienzo(W, H)
    cruz(L, H)
    guardar(L, "m_cruz_%d.png" % escala)
    L = arte.Lienzo(W, H)
    menu_equipo(L, W, H)
    guardar(L, "m_equipo_%d.png" % escala)
    L = arte.Lienzo(W, H)
    menu_equipo(L, W, H, lista=True)
    guardar(L, "m_equipo_lista_%d.png" % escala)
    L = arte.Lienzo(W, H)
    menu_bolsa(L, W, H)
    guardar(L, "m_bolsa_%d.png" % escala)
