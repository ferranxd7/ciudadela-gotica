"""Escribe instalar_mods.bat (Windows, fin de linea CRLF): los mods de decoracion que ha probado la CI, Dark Craft
(de la ultima release: lleva dentro Distant Horizons, Oculus y Embeddium) y el shader Complementary en shaderpacks/.

Uso: python3 ci/instalador.py mrpack_files.json shader.json salida.bat
"""
import json
import os
import sys

files_json, shader_json, out = sys.argv[1], sys.argv[2], sys.argv[3]
mods = json.load(open(files_json)) if os.path.exists(files_json) else []
shader = json.load(open(shader_json)) if os.path.exists(shader_json) else None

PS = ("powershell -NoProfile -Command \"$ProgressPreference='SilentlyContinue'; "
      "[Net.ServicePointManager]::SecurityProtocol='Tls12'; Invoke-WebRequest -UseBasicParsing -Uri '{url}' -OutFile '{dest}'\"")
DARKCRAFT = "https://github.com/ferranxd7/ciudadela-gotica/releases/latest/download/darkcraft-0.1.0.jar"

lineas = [
    "@echo off",
    "chcp 65001 >nul",
    "title Instalador de mods - Dark Craft",
    "echo ==========================================================",
    "echo   Instalador de mods para Dark Craft (Forge 1.20.1)",
    "echo ==========================================================",
    "echo.",
    "echo ANTES: en TLauncher instala y abre una vez la version \"Forge 1.20.1\".",
    "echo.",
    "pause",
    "set \"MC=%APPDATA%\\.minecraft\"",
    "set \"MODS=%MC%\\mods\"",
    "if not exist \"%MODS%\" mkdir \"%MODS%\"",
    "if exist \"%MODS%\\*.jar\" (",
    "  echo Moviendo tus mods antiguos a la carpeta mods_antiguos ...",
    "  if not exist \"%MC%\\mods_antiguos\" mkdir \"%MC%\\mods_antiguos\"",
    "  move /Y \"%MODS%\\*.jar\" \"%MC%\\mods_antiguos\\\" >nul",
    ")",
    "echo.",
]
total = len(mods) + 1 + (1 if shader else 0)
n = 0
for m in mods:
    n += 1
    nombre = m["path"].split("/")[-1]
    lineas.append(f"echo [{n}/{total}] {nombre}")
    lineas.append(PS.format(url=m["downloads"][0], dest="%MODS%\\" + nombre))
n += 1
lineas.append(f"echo [{n}/{total}] darkcraft-0.1.0.jar (lleva dentro Distant Horizons, Oculus y Embeddium)")
lineas.append(PS.format(url=DARKCRAFT, dest="%MODS%\\darkcraft-0.1.0.jar"))
if shader:
    n += 1
    lineas += [
        "set \"SHADERS=%MC%\\shaderpacks\"",
        "if not exist \"%SHADERS%\" mkdir \"%SHADERS%\"",
        f"echo [{n}/{total}] {shader['fichero']} (shaders Complementary, de EminGT)",
        PS.format(url=shader["url"], dest="%SHADERS%\\" + shader["fichero"]),
    ]
lineas += [
    "echo.",
    "echo ==========================================================",
    "echo   LISTO. Abre TLauncher, elige \"Forge 1.20.1\" y juega.",
    "echo   Sube la memoria de TLauncher a 8 GB o mas (en sus ajustes):",
    "echo   la lejania de Distant Horizons y los shaders la necesitan.",
    "echo   Crea una PARTIDA NUEVA.",
    "echo ==========================================================",
    "pause",
]
with open(out, "w", encoding="utf-8", newline="\r\n") as f:
    f.write("\n".join(lineas) + "\n")
print(f"instalador: {len(mods)} mods de decoracion + Dark Craft" + (f" + {shader['fichero']}" if shader else "") + f" -> {out}")
