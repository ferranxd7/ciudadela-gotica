#!/usr/bin/env python3
"""
Las pantallas que aun salen de Minecraft (y las de otros mods) con el estilo del mod (parte 2f): se reescriben las
texturas del juego que dibujan sus botones, deslizadores, casillas, fondos y separadores. Parte de las texturas de
verdad del juego (las que no se tocan se quedan como estan) y escribe en
src/main/resources/assets/minecraft/textures/gui/.

    python3 tools/ui/vanilla.py [client-extra.jar] [--vista carpeta]
"""
import io
import os
import sys
import zipfile

import numpy as np
from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import hud  # noqa: E402
from arte import Lienzo, ORO, PIEDRA, ROJO, TINTA, REPO, mezcla, piedra, rgba  # noqa: E402

SALIDA = os.path.join(REPO, "src/main/resources/assets/minecraft/textures/gui")


def original(jar, nombre):
    with zipfile.ZipFile(jar) as z:
        return Image.open(io.BytesIO(z.read("assets/minecraft/textures/gui/" + nombre))).convert("RGBA")


def a_lienzo(im):
    L = Lienzo(im.width, im.height)
    L.a[:] = np.asarray(im)
    return L


def boton(w, estado):
    """Un boton de w x 20 en el estilo del mod: 0 apagado, 1 normal, 2 con el raton encima."""
    luz = estado == 2
    izq, med, der = hud.boton("izq", luz), hud.boton("med", luz), hud.boton("der", luz)
    L = Lienzo(w, 20)
    L.pegar(izq, 0, 0)
    x = izq.w
    while x < w - der.w:
        L.pegar(med.recorte(0, 0, min(med.w, w - der.w - x), 20), x, 0)
        x += med.w
    L.pegar(der, w - der.w, 0)
    if estado == 0:
        # apagado: sin oro, piedra mas oscura
        a = L.a.astype(float)
        gris = a[..., :3].mean(axis=2, keepdims=True)
        a[..., :3] = gris * 0.55
        L.a[:] = np.clip(a, 0, 255).astype(np.uint8)
    return L


def deslizador(estado):
    """La guia del deslizador (0 normal, 1 con el foco): un rebaje oscuro con filo de oro gastado."""
    L = Lienzo(200, 20)
    cuerpo = piedra(200, 20, semilla=71, base=1)
    L.a[:] = cuerpo.a
    for x in range(200):
        L.px(x, 0, TINTA).px(x, 19, TINTA)
        L.px(x, 1, PIEDRA[0]).px(x, 18, ORO[2] if estado else PIEDRA[4])
    for y in range(20):
        L.px(0, y, TINTA).px(199, y, TINTA)
        L.px(1, y, PIEDRA[0]).px(198, y, ORO[2] if estado else PIEDRA[4])
    # una raya de oro por el medio, por donde corre el tirador
    for x in range(4, 196):
        L.px(x, 10, ORO[1] if x % 6 else ORO[2])
    return L


def tirador(luz):
    """El tirador del deslizador (se dibuja de 8 de ancho): piedra con filo de oro (mas claro con el raton encima)."""
    L = Lienzo(200, 20)
    b = boton(200, 2 if luz else 1)
    L.a[:] = b.a
    return L


def casilla(marcada, foco):
    """La casilla de las opciones (20 x 20): hueco oscuro con marco de oro y, marcada, una cruz de oro."""
    L = Lienzo(20, 20)
    for y in range(20):
        for x in range(20):
            borde = min(x, y, 19 - x, 19 - y)
            if borde == 0:
                c = TINTA
            elif borde == 1:
                c = ORO[5] if foco else ORO[3]
            elif borde == 2:
                c = ORO[2] if foco else ORO[1]
            else:
                c = PIEDRA[1] if (x + y) % 5 else PIEDRA[0]
            L.px(x, y, c)
    if marcada:
        for i in range(5, 15):
            for d in (0, 1):
                L.px(i, i + d - 1 if i + d - 1 >= 5 else i, ORO[5]).px(19 - i, i + d - 1 if i + d - 1 >= 5 else i, ORO[5])
        for i in range(6, 14):
            L.px(i, i, ORO[6]).px(19 - i, i, ORO[6])
    return L


def fondo(claro):
    """El fondo de las pantallas de opciones (16 x 16, se repite): piedra oscura en vez de tierra."""
    L = piedra(16, 16, semilla=91 if not claro else 92, base=1 if not claro else 2, grano=1.4)
    for x in range(16):
        if x % 8 == 3:
            L.px(x, 7, PIEDRA[0])
    return L


def separador():
    """La raya de arriba y de abajo de las listas (32 x 2): oro gastado."""
    L = Lienzo(32, 2)
    for x in range(32):
        L.px(x, 0, ORO[3] if x % 8 != 5 else ORO[5]).px(x, 1, ORO[1])
    return L


def losa_de(w, h):
    """Una losa del menu (piedra con marco de oro y tachuelas) del tamano que se quiera, de sus nueve trozos."""
    import menu
    trozos = dict(menu.nueve(menu.losa(), menu.ESQ))
    e = menu.ESQ
    L = Lienzo(w, h)

    def mosaico(t, x0, y0, ww, hh):
        for y in range(y0, y0 + hh, t.h):
            for x in range(x0, x0 + ww, t.w):
                L.pegar(t.recorte(0, 0, min(t.w, x0 + ww - x), min(t.h, y0 + hh - y)), x, y)

    mosaico(trozos["c"], e, e, w - 2 * e, h - 2 * e)
    mosaico(trozos["a"], e, 0, w - 2 * e, e)
    mosaico(trozos["b"], e, h - e, w - 2 * e, e)
    mosaico(trozos["i"], 0, e, e, h - 2 * e)
    mosaico(trozos["d"], w - e, e, e, h - 2 * e)
    L.pegar(trozos["ai"], 0, 0).pegar(trozos["ad"], w - e, 0).pegar(trozos["bi"], 0, h - e).pegar(trozos["bd"], w - e, h - e)
    return L


def avisos(jar):
    """Los avisos de Minecraft (toasts.png): las cuatro bandas de 160 x 32 (logro, desafio y receta, sistema,
    tutorial) como losas pequenas; los iconos del tutorial (a partir de x = 176) se quedan."""
    t = a_lienzo(original(jar, "toasts.png"))
    for i in range(4):
        b = losa_de(160, 32)
        if i == 1:
            # la de los desafios, con un filo rojo por dentro
            for x in range(6, 154):
                b.px(x, 5, ROJO[3]).px(x, 26, ROJO[2])
        t.a[i * 32:(i + 1) * 32, 0:160] = b.a
    return t


def montar(jar):
    out = {}
    w = a_lienzo(original(jar, "widgets.png"))
    for i in range(3):
        b = boton(200, i)
        w.a[46 + i * 20:66 + i * 20, 0:200] = b.a
    out["widgets.png"] = w
    s = a_lienzo(original(jar, "slider.png"))
    s.a[0:20, 0:200] = deslizador(1).a
    s.a[20:40, 0:200] = deslizador(0).a
    s.a[40:60, 0:200] = tirador(False).a
    s.a[60:80, 0:200] = tirador(True).a
    out["slider.png"] = s
    c = a_lienzo(original(jar, "checkbox.png"))
    for (u, v, marcada, foco) in ((0, 0, False, False), (20, 0, False, True), (0, 20, True, False), (20, 20, True, True)):
        c.a[v:v + 20, u:u + 20] = casilla(marcada, foco).a
    out["checkbox.png"] = c
    out["options_background.png"] = fondo(False)
    out["light_dirt_background.png"] = fondo(True)
    out["toasts.png"] = avisos(jar)
    out["header_separator.png"] = separador()
    out["footer_separator.png"] = separador()
    return out


if __name__ == "__main__":
    args = [a for a in sys.argv[1:] if not a.startswith("--")]
    jar = args[0] if args and args[0].endswith(".jar") else "/tmp/cp/cpjars/client-extra.jar"
    hechos = montar(jar)
    os.makedirs(SALIDA, exist_ok=True)
    for n, L in hechos.items():
        L.guardar(os.path.join(SALIDA, n))
    if "--vista" in sys.argv:
        d = sys.argv[sys.argv.index("--vista") + 1]
        os.makedirs(d, exist_ok=True)
        for n, L in hechos.items():
            im = L.imagen()
            im.resize((im.width * 3, im.height * 3), Image.NEAREST).save(os.path.join(d, "v_" + n))
    print("vanilla: hecho", ", ".join(hechos))
