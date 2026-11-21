#!/usr/bin/env python3
"""Junta las capturas del escaparate del rey (esc_*.png) en hojas para revisarlas de un vistazo.
Uso: hoja.py <carpeta de capturas> <carpeta de salida>"""
import glob
import os
import sys

from PIL import Image, ImageDraw


def main():
    src, out = sys.argv[1], sys.argv[2]
    files = sorted(glob.glob(os.path.join(src, "esc_*.png")))
    if not files:
        print("sin capturas del escaparate")
        return
    tiles = []
    for f in files:
        im = Image.open(f).convert("RGB")
        w, h = im.size
        cw = int(h * 0.8)
        im = im.crop(((w - cw) // 2, 0, (w + cw) // 2, h)).resize((320, int(320 * h / cw)))
        d = ImageDraw.Draw(im)
        d.text((6, 6), os.path.basename(f)[4:-4], fill=(255, 255, 255))
        tiles.append(im)
    cols = 6
    per = cols * 3
    for s in range(0, len(tiles), per):
        part = tiles[s:s + per]
        rows = (len(part) + cols - 1) // cols
        tw, th = part[0].size
        sheet = Image.new("RGB", (cols * tw, rows * th), (20, 20, 24))
        for i, t in enumerate(part):
            sheet.paste(t, ((i % cols) * tw, (i // cols) * th))
        sheet.save(os.path.join(out, f"cliente_escaparate_{s // per + 1}.png"))
    print(f"{len(tiles)} capturas en {(len(tiles) + per - 1) // per} hojas")


if __name__ == "__main__":
    main()
