"""Crea el modpack .mrpack (Modrinth) con Forge y Dark Craft (sin mods de decoracion desde la parte 8a).

Uso: python3 ci/make_mrpack.py mrpack_files.json darkcraft.jar salida.mrpack version_forge [shader.json]
Dark Craft va dentro (con Distant Horizons, Oculus y Embeddium en su jar); el shader (Complementary) se anade como un
fichero mas de Modrinth en shaderpacks/ (su licencia no deja subirlo directamente).
"""
import json, sys, zipfile, os

files_json, mod_jar, out, forge = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
shader = sys.argv[5] if len(sys.argv) > 5 else None
files = json.load(open(files_json))
if shader and os.path.exists(shader):
    s = json.load(open(shader))
    files.append({"path": "shaderpacks/" + s["fichero"], "hashes": s["hashes"],
                  "env": {"client": "required", "server": "unsupported"}, "downloads": [s["url"]], "fileSize": s["tamano"]})
index = {
    "formatVersion": 1,
    "game": "minecraft",
    "versionId": "1.0.0",
    "name": "Dark Craft",
    "summary": "Dark Craft: una aventura oscura sobre un mar de nubes. Lleva dentro Distant Horizons, Oculus y Embeddium; "
               "shaders: Complementary Unbound (EminGT).",
    "files": files,
    "dependencies": {"minecraft": "1.20.1", "forge": forge},
}
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("modrinth.index.json", json.dumps(index, indent=1))
    z.write(mod_jar, "overrides/mods/" + os.path.basename(mod_jar))
print("mrpack con", len(files), "ficheros + darkcraft ->", out)
