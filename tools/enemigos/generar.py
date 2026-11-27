#!/usr/bin/env python3
"""
Genera todo lo de los enemigos con forma de persona (parte 3c): las texturas (y su capa de brillo), el codigo Java de
los modelos y de las animaciones, las tablas de acciones para el servidor y, con --vistas, hojas para revisarlo todo sin
abrir el juego (cada variante por delante y por detras, y cada animacion fotograma a fotograma).

    python3 tools/enemigos/generar.py [--vistas carpeta] [--solo hueco]

Necesita numpy y Pillow (solo para generar; el juego no).
"""
import importlib
import os
import sys

import numpy as np
from PIL import Image, ImageDraw

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)

import anim  # noqa: E402
import modelo  # noqa: E402
import vista  # noqa: E402
from pinta import Textura, brillo_de  # noqa: E402

REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
TEX_DIR = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/entity/enemigos")
JAVA_CLI = os.path.join(REPO, "src/main/java/com/darkcraft/client/enemigos")
JAVA_COM = os.path.join(REPO, "src/main/java/com/darkcraft/enemigos")
HERRAMIENTA = "tools/enemigos/generar.py"

PERSONAJES = ("hueco", "ahogado", "capitan", "guardia", "arquero", "perro", "espectro", "elite", "brandt", "corteza", "corcel", "marchito", "molinera", "ahorcado", "verdugo", "podrido", "barquero", "helado", "abad", "calcinado", "condestable", "erudito", "rectora", "salobre", "ballenero", "vieja", "penitente", "pontifice", "marcado", "aldric", "gigante", "jinete", "bufon", "dragon")


def clase(nombre):
    return nombre[0].upper() + nombre[1:]


def generar(nombre, vistas=None):
    m = importlib.import_module(nombre)
    rig = m.rig()
    os.makedirs(TEX_DIR, exist_ok=True)
    os.makedirs(JAVA_CLI, exist_ok=True)
    os.makedirs(JAVA_COM, exist_ok=True)
    texs = []
    for v, _ in enumerate(m.VARIANTES):
        t = Textura(rig).pintar(m.materiales(v))
        b = brillo_de(t, m.brillo(v))
        t.guardar(os.path.join(TEX_DIR, f"{nombre}_{v}.png"))
        b.guardar(os.path.join(TEX_DIR, f"{nombre}_{v}_brillo.png"))
        texs.append((t, b))
    if hasattr(m, "extras"):
        m.extras(TEX_DIR)
    bucles, acciones = m.animaciones(rig)
    anim.guardar_cache()
    import ik
    peor = ik.peores(6)
    if peor and peor[0][0] > 1.5:
        print(f"  {nombre}: la mano no llega en " + "; ".join(f"{e[0]:.1f} px ({e[1]}, {tuple(round(float(x), 1) for x in e[2][-3:])})" for e in peor if e[0] > 1.5))
    ik.PEORES.clear()
    ocultas = {f"OCULTAS_{v}": piezas for v, piezas in m.OCULTAS.items()}
    with open(os.path.join(JAVA_CLI, f"{clase(nombre)}Geometria.java"), "w", encoding="utf-8") as f:
        f.write(modelo.java_geometria(rig, "com.darkcraft.client.enemigos", f"{clase(nombre)}Geometria", HERRAMIENTA, ocultas))
    with open(os.path.join(JAVA_CLI, f"{clase(nombre)}Animaciones.java"), "w", encoding="utf-8") as f:
        f.write(anim.java_animaciones("com.darkcraft.client.enemigos", f"{clase(nombre)}Animaciones", HERRAMIENTA, bucles, acciones))
    with open(os.path.join(JAVA_COM, f"{clase(nombre)}Anims.java"), "w", encoding="utf-8") as f:
        f.write(anim.java_tabla("com.darkcraft.enemigos", f"{clase(nombre)}Anims", HERRAMIENTA, acciones, getattr(m, "TABLA_EXTRA", ())))
    print(f"{nombre}: textura {rig.tex_w}x{rig.tex_h}, {sum(len(p.boxes) for p in rig.partes)} cajas, {len(rig.partes)} piezas, "
          f"{len(bucles)} bucles, {len(acciones)} acciones")
    if vistas:
        hojas(m, nombre, rig, texs, bucles, acciones, vistas)
    return rig


def hojas(m, nombre, rig, texs, bucles, acciones, carpeta):
    os.makedirs(carpeta, exist_ok=True)
    por_nombre = {a.nombre: a for a in bucles + acciones}
    esc = getattr(m, "ESCALA", 1.0)
    cy = getattr(m, "CENTRO_Y", 1.0)
    # ---- cada variante: de frente, tres cuartos, de lado y de espaldas, en su postura de reposo
    filas = []
    for v, arma in enumerate(m.VARIANTES):
        t, b = texs[v]
        reposo = m.reposo(v, por_nombre) if hasattr(m, "reposo") else {}
        zv = getattr(m, "ZOOM_VARIANTES", None) or 100 / max(1.0, esc * 1.12 if esc > 1 else 1.0)
        tiles = [vista.render(rig, t.a, b.a, reposo, yaw=y, pitch=-8, size=(190, 290), zoom=zv,
                              ocultas=m.OCULTAS[v], escala_modelo=esc, centro_y=cy)
                 for y in (0, 35, 90, 180, 250)]
        filas.append(np.concatenate(tiles, axis=1))
    Image.fromarray(np.concatenate(filas, axis=0)).save(os.path.join(carpeta, f"{nombre}_variantes.png"))
    texs[0][0].imagen().resize((rig.tex_w * 6, rig.tex_h * 6), Image.NEAREST).save(os.path.join(carpeta, f"{nombre}_textura.png"))
    # ---- cada animacion, fotograma a fotograma (con la variante que le toca)
    for a in bucles + acciones:
        v = m.variante_de(a.nombre) if hasattr(m, "variante_de") else 0
        t, b = texs[v]
        n = 8 if a.bucle else max(6, min(12, int(round(a.largo / 0.14))))
        tiempos = [a.largo * k / (n - (0 if a.bucle else 1)) for k in range(n)]
        if a.golpes:
            tiempos = sorted(set(tiempos + [g / 20.0 for g in a.golpes]))
        bases = m.base_de(a.nombre, por_nombre) if hasattr(m, "base_de") else []
        poses = [anim.sumar(*[x.muestra(tt) for x in bases], a.muestra(tt)) for tt in tiempos]
        etiquetas = [f"{tt:.2f}" + (" GOLPE" if int(round(tt * 20)) in a.golpes else "") for tt in tiempos]
        for sufijo, yaw in (("", 35), ("_lado", 270)):
            lado = getattr(m, "LADO_TIRA", None) or (230 if esc <= 1 else 300)
            zt = getattr(m, "ZOOM_TIRA", None) or (56 if esc <= 1 else 62)
            im = vista.tira(rig, t.a, poses, b.a, yaw=yaw, pitch=-10, size=(lado, lado), zoom=zt, etiquetas=etiquetas,
                            ocultas=m.OCULTAS[v], escala_modelo=esc, centro_y=cy)
            im.save(os.path.join(carpeta, f"{nombre}_{a.nombre}{sufijo}.png"))


def main():
    vistas = sys.argv[sys.argv.index("--vistas") + 1] if "--vistas" in sys.argv else None
    solo = sys.argv[sys.argv.index("--solo") + 1] if "--solo" in sys.argv else None
    for nombre in PERSONAJES:
        if solo and nombre != solo:
            continue
        if not os.path.exists(os.path.join(AQUI, nombre + ".py")):
            continue
        generar(nombre, vistas)


if __name__ == "__main__":
    main()
