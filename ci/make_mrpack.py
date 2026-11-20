"""Crea el modpack .mrpack (Modrinth) con Forge, la Ciudadela y todos los mods de decoracion."""
import json, sys, zipfile, glob, os

files_json, mod_jar, out, forge = sys.argv[1], sys.argv[2], sys.argv[3], sys.argv[4]
files = json.load(open(files_json))
index = {
    "formatVersion": 1,
    "game": "minecraft",
    "versionId": "1.0.0",
    "name": "Ciudadela Gotica",
    "summary": "Ciudadela gotica colosal sobre una isla flotante, con mods de decoracion.",
    "files": files,
    "dependencies": {"minecraft": "1.20.1", "forge": forge},
}
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("modrinth.index.json", json.dumps(index, indent=1))
    z.write(mod_jar, "overrides/mods/" + os.path.basename(mod_jar))
print("mrpack con", len(files), "mods + ciudadela ->", out)
