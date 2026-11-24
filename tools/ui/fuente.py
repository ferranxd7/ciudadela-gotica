#!/usr/bin/env python3
"""
La letra gotica en pixel art de los titulos del mod (carteles, nombres de sitios y de jefes, cabeceras de los menus).

Se saca de "New Rocker" (Pablo Impallari, Brenda Gallo y Rodrigo Fuenzalida; licencia SIL Open Font 1.1, en
tools/ui/fuentes/NewRocker-OFL.txt) dibujada sin suavizado a pocos pixeles de alto: cada letra queda en pixeles puros (y con las colas largas de la N y
la L recortadas, que en una fuente de mapa de bits separarian las letras).
Salen dos tamanos como fuentes de mapa de bits de Minecraft:

  darkcraft:gotica     letras de 22 px (carteles grandes; se pueden dibujar al doble)
  darkcraft:gotica_m   letras de 16 px (cabeceras, nombres)

Cada una es assets/darkcraft/textures/font/NOMBRE.png (una rejilla de celdas iguales, la letra pegada a la izquierda de
su celda) y assets/darkcraft/font/NOMBRE.json (que letra hay en cada celda; lo que no tenga cae en la letra normal del
juego, asi vale para todos los idiomas). Minecraft da a cada letra el ancho de su ultimo pixel no transparente mas uno:
para que cada letra avance lo que pide la fuente (y no se peguen ni se separen), en la columna donde acaba su avance va
un pixel casi transparente (alfa 1), que no se ve.

    python3 tools/ui/fuente.py [--muestra fichero.png]
"""
import json
import os
import sys

from PIL import Image, ImageDraw, ImageFont

AQUI = os.path.dirname(os.path.abspath(__file__))
REPO = os.path.abspath(os.path.join(AQUI, "..", ".."))
ASSETS = os.path.join(REPO, "src/main/resources/assets/darkcraft")
TTF = os.path.join(AQUI, "fuentes", "NewRocker-Regular.ttf")

# ASCII imprimible y Latin-1 (acentos del espanol, frances, aleman y portugues)
LETRAS = [chr(c) for c in range(0x21, 0x7F)] + [chr(c) for c in range(0xA1, 0x100) if c != 0xAD]
COLUMNAS = 16
FUENTES = {"gotica": 22, "gotica_m": 16}


def tiene(font, ch):
    """Si la fuente trae esa letra (y no el cuadro de 'no existe')."""
    try:
        m = font.getmask(ch, mode="1")
        nada = font.getmask("￿", mode="1")
        return m.getbbox() is not None and bytes(m) != bytes(nada)
    except Exception:  # noqa: BLE001
        return False


def generar(nombre, px):
    font = ImageFont.truetype(TTF, px)
    ascent, descent = font.getmetrics()
    alto = ascent + descent
    letras = [ch for ch in LETRAS if tiene(font, ch)]
    # cada letra: su dibujo de 1 bit, lo que sobresale por la izquierda y su avance
    datos = []
    ancho = 0
    for ch in letras:
        x0, y0, x1, y1 = font.getbbox(ch)
        avance = int(round(font.getlength(ch)))
        im = Image.new("L", (max(1, x1 - min(0, x0)) + 2, alto), 0)
        d = ImageDraw.Draw(im)
        d.fontmode = "1"
        d.text((-min(0, x0), 0), ch, font=font, fill=255)
        bb = im.getbbox()
        if bb is None:
            continue
        # la tinta, pegada a la izquierda; el avance cuenta desde donde empezaba la tinta
        izq = bb[0]
        tinta = im.crop((izq, 0, bb[2], alto))
        # las colas largas de la N, la Ñ y la L estan pensadas para meterse por debajo de la letra siguiente; en una
        # fuente de mapa de bits no caben (la letra siguiente se iria detras de la cola y quedaria "N_"): se cortan
        # donde acaba el avance de la letra y queda un pie corto
        if tinta.width > avance + 1:
            tinta = tinta.crop((0, 0, avance - 1, alto))
        # Minecraft suma un pixel al ancho: el ancho de la celda es el avance de la fuente menos uno
        fin = max(tinta.width, avance - 1)
        datos.append((ch, tinta, fin))
        ancho = max(ancho, fin + 1)
    celda_w, celda_h = ancho + 1, alto
    filas = (len(datos) + COLUMNAS - 1) // COLUMNAS
    hoja = Image.new("RGBA", (COLUMNAS * celda_w, filas * celda_h), (0, 0, 0, 0))
    chars = []
    for f in range(filas):
        fila = ""
        for c in range(COLUMNAS):
            i = f * COLUMNAS + c
            if i >= len(datos):
                fila += "\u0000"
                continue
            ch, tinta, fin = datos[i]
            ox, oy = c * celda_w, f * celda_h
            blanco = Image.new("RGBA", tinta.size, (255, 255, 255, 255))
            hoja.paste(blanco, (ox, oy), tinta)
            # el pixel que no se ve y marca donde acaba el avance de la letra (en la linea de base)
            if fin - 1 >= tinta.width:
                hoja.putpixel((ox + fin - 1, oy + ascent - 1), (255, 255, 255, 1))
            fila += ch
        chars.append(fila)
    os.makedirs(os.path.join(ASSETS, "textures", "font"), exist_ok=True)
    os.makedirs(os.path.join(ASSETS, "font"), exist_ok=True)
    hoja.save(os.path.join(ASSETS, "textures", "font", nombre + ".png"))
    espacio = max(3, int(round(font.getlength(" "))))
    with open(os.path.join(ASSETS, "font", nombre + ".json"), "w", encoding="utf-8") as fh:
        json.dump({"providers": [
            {"type": "space", "advances": {" ": espacio, " ": espacio}},
            {"type": "bitmap", "file": f"darkcraft:font/{nombre}.png", "height": celda_h, "ascent": ascent, "chars": chars},
            {"type": "reference", "id": "minecraft:default"},
        ]}, fh, ensure_ascii=True, indent=1)
    print(f"{nombre}: {len(datos)} letras de {px} px, celdas de {celda_w}x{celda_h}, subida {ascent}, espacio {espacio}"
          f" -> {hoja.size[0]}x{hoja.size[1]}")
    return {"hoja": hoja, "celda": (celda_w, celda_h), "ascent": ascent, "espacio": espacio,
            "indice": {d[0]: i for i, d in enumerate(datos)}}


def escribir(dest, f, texto, x, y, color, escala=1, sombra=None):
    """Escribe con la fuente generada igual que lo hara Minecraft (para las vistas previas). Devuelve el ancho."""
    cw, ch = f["celda"]
    x0 = x
    for letra in texto:
        if letra == " ":
            x += f["espacio"] * escala
            continue
        i = f["indice"].get(letra)
        if i is None:
            x += f["espacio"] * escala
            continue
        celda = f["hoja"].crop(((i % COLUMNAS) * cw, (i // COLUMNAS) * ch, (i % COLUMNAS + 1) * cw, (i // COLUMNAS + 1) * ch))
        alfa = celda.split()[3]
        bb = alfa.point(lambda v: 255 if v > 0 else 0).getbbox()
        w = bb[2] if bb else 0
        visible = alfa.point(lambda v: 255 if v > 128 else 0)
        if escala != 1:
            visible = visible.resize((cw * escala, ch * escala), Image.NEAREST)
        if sombra is not None:
            dest.paste(Image.new("RGBA", visible.size, sombra), (x + escala, y + escala), visible)
        dest.paste(Image.new("RGBA", visible.size, color), (x, y), visible)
        x += (w + 1) * escala
    return x - x0


def ancho(f, texto, escala=1):
    return escribir(Image.new("RGBA", (4, 4)), f, texto, 0, 0, (0, 0, 0, 0), escala)


def main():
    fs = {n: generar(n, px) for n, px in FUENTES.items()}
    if "--muestra" in sys.argv:
        dst = sys.argv[sys.argv.index("--muestra") + 1]
        im = Image.new("RGBA", (640, 300), (22, 18, 24, 255))
        y = 6
        for texto, n, esc, col in [("HAS MUERTO", "gotica", 2, (178, 30, 38, 255)), ("JEFE ABATIDO", "gotica", 2, (226, 190, 96, 255)),
                                   ("Reino de Valdrenn", "gotica", 1, (230, 214, 180, 255)),
                                   ("Morvain, las Tierras Marchitas", "gotica", 1, (230, 214, 180, 255)),
                                   ("Ecos de la Sombra   ¿Señor? ¡Árbol! 0123456789", "gotica_m", 1, (226, 190, 96, 255)),
                                   ("Equipo  Inventario  Estado  Hechizos  Sistema", "gotica_m", 1, (200, 196, 204, 255)),
                                   ("The quick brown fox jumps over the lazy dog", "gotica_m", 1, (200, 196, 204, 255))]:
            w = ancho(fs[n], texto, esc)
            escribir(im, fs[n], texto, (640 - w) // 2, y, col, esc, (0, 0, 0, 255))
            y += fs[n]["celda"][1] * esc + 4
        im.resize((1280, 600), Image.NEAREST).save(dst)
        print("muestra en", dst)


if __name__ == "__main__":
    main()
