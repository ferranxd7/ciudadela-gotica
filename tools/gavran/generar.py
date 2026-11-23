#!/usr/bin/env python3
"""Genera todo lo del modelo de Ser Gavran: texturas (base y brillo por fase), el codigo Java del modelo y de las
animaciones, y vistas previas en PNG. Necesita numpy, Pillow y scipy (solo para generar; el juego no).

Uso: python3 tools/gavran/generar.py [--vistas carpeta]
La cinematica inversa tarda unos minutos la primera vez; se guarda en tools/gavran/.ik_cache.pkl (no se sube).
"""
import math
import os
import sys

import numpy as np
from PIL import Image

AQUI = os.path.dirname(os.path.abspath(__file__))
sys.path.insert(0, AQUI)

import partes  # noqa: E402
from partes import PARTS, SCALE, TEX_W  # noqa: E402
import pintar  # noqa: E402
import vista  # noqa: E402

REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
TEX_DIR = os.path.join(REPO, "src/main/resources/assets/darkcraft/textures/entity/gavran")
JAVA_CLI = os.path.join(REPO, "src/main/java/com/darkcraft/client/gavran")
JAVA_COM = os.path.join(REPO, "src/main/java/com/darkcraft/gavran")
FASES = (1, 2)


def fmt(v):
    s = f"{v:.4f}".rstrip("0").rstrip(".")
    if s in ("-0", ""):
        s = "0"
    return s + "F"


def java_model(tex_h):
    out = ["package com.darkcraft.client.gavran;\n",
           "import net.minecraft.client.model.geom.PartPose;",
           "import net.minecraft.client.model.geom.builders.*;\n",
           "/** GENERADO por tools/gavran/generar.py: no tocar a mano. Piezas y cajas del modelo de Ser Gavran. */",
           "public final class GavranGeometria {",
           "    private GavranGeometria() {}\n",
           f"    public static final int TEX_W = {TEX_W}, TEX_H = {tex_h};\n",
           "    public static LayerDefinition crear() {",
           "        MeshDefinition mesh = new MeshDefinition();",
           "        PartDefinition root = mesh.getRoot();"]
    var = {"": "root"}
    for p in PARTS:
        parent = var[p.parent]
        v = "p_" + p.name
        var[p.name] = v
        cb = "CubeListBuilder.create()"
        for b in p.boxes:
            gx, gy, gz = b.grow
            cb += f".texOffs({b.u}, {b.v})"
            if gx == gy == gz == 0:
                cb += f".addBox({fmt(b.x)}, {fmt(b.y)}, {fmt(b.z)}, {fmt(b.w)}, {fmt(b.h)}, {fmt(b.d)})"
            else:
                cb += (f".addBox({fmt(b.x)}, {fmt(b.y)}, {fmt(b.z)}, {fmt(b.w)}, {fmt(b.h)}, {fmt(b.d)}, "
                       f"new CubeDeformation({fmt(gx)}, {fmt(gy)}, {fmt(gz)}))")
        rx, ry, rz = (math.radians(a) for a in p.rot)
        px, py, pz = p.pivot
        if rx == ry == rz == 0:
            pose = f"PartPose.offset({fmt(px)}, {fmt(py)}, {fmt(pz)})"
        else:
            pose = f"PartPose.offsetAndRotation({fmt(px)}, {fmt(py)}, {fmt(pz)}, {fmt(rx)}, {fmt(ry)}, {fmt(rz)})"
        out.append(f"        PartDefinition {v} = {parent}.addOrReplaceChild(\"{p.name}\", {cb}, {pose});")
    out.append("        return LayerDefinition.create(mesh, TEX_W, TEX_H);")
    out.append("    }\n")
    # caminos (de la raiz a la pieza) de las espinas de la fase 2: el modelo las esconde en la fase 1
    madre = {p.name: p.parent for p in PARTS}

    def camino(n):
        c = []
        while n:
            c.append(n)
            n = madre[n]
        return list(reversed(c))
    esp = [camino(p.name) for p in PARTS if p.name.startswith("espina2_")]
    out.append("    /** Espinas que solo se ven en la fase 2 (camino de piezas desde la raiz). */")
    out.append("    public static final String[][] ESPINAS2 = {" + ", ".join("{" + ", ".join(f'\"{x}\"' for x in c) + "}" for c in esp) + "};")
    out.append("}")
    return "\n".join(out) + "\n"


def main():
    used = partes.pack()
    tex_h = 1 << int(math.ceil(math.log2(max(used, 16))))
    os.makedirs(TEX_DIR, exist_ok=True)
    os.makedirs(JAVA_CLI, exist_ok=True)
    os.makedirs(JAVA_COM, exist_ok=True)
    texs = {}
    for ph in FASES:
        base, glow = pintar.build(ph, used)
        base = base[: tex_h * SCALE]
        glow = glow[: tex_h * SCALE]
        texs[ph] = (base, glow)
        Image.fromarray((base * 255).astype(np.uint8), "RGBA").save(os.path.join(TEX_DIR, f"gavran_{ph}.png"))
        g = glow.copy()
        g[..., :3] *= g[..., 3:4]
        g[..., 3] = np.where(g[..., 3] > 0, 1, 0)
        Image.fromarray((g * 255).astype(np.uint8), "RGBA").save(os.path.join(TEX_DIR, f"brillo_{ph}.png"))
    with open(os.path.join(JAVA_CLI, "GavranGeometria.java"), "w") as f:
        f.write(java_model(tex_h))
    import animaciones
    animaciones.densificar_todo()
    animaciones.guardar_cache()
    animaciones.write_java(os.path.join(JAVA_CLI, "GavranAnimaciones.java"), os.path.join(JAVA_COM, "GavranAnims.java"))
    peor = animaciones.peores(1)
    if "--vistas" in sys.argv:
        vdir = sys.argv[sys.argv.index("--vistas") + 1]
        os.makedirs(vdir, exist_ok=True)
        tiles = []
        for ph in FASES:
            base, glow = texs[ph]
            for yaw in (25, 145, 250):
                tiles.append(vista.render(base, glow, yaw=yaw, pitch=-8))
        rows = [np.concatenate(tiles[i * 3:(i + 1) * 3], axis=1) for i in range(len(FASES))]
        Image.fromarray(np.concatenate(rows, axis=0)).save(os.path.join(vdir, "gavran_fases.png"))
    print(f"textura {TEX_W}x{tex_h} (usado {used}), {sum(len(p.boxes) for p in PARTS)} cajas, {len(PARTS)} piezas; "
          f"peor error de la cinematica inversa: {peor[0][0]:.1f} px")


if __name__ == "__main__":
    main()
