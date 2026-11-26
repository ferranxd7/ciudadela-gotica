"""Mete Distant Horizons, Oculus y Embeddium dentro del jar de Dark Craft (parte 6) y baja el shader.

Uso: python3 ci/dentro/meter.py JAR ci/dentro/mods.json SALIDA

- Cada mod se baja de Modrinth (Forge, 1.20.1) en su version fijada (si ya no esta, la mas nueva con el mismo
  principio, avisando), se comprueba su sha512 y se mete SIN CAMBIOS en META-INF/jarjar/ con su entrada en
  META-INF/jarjar/metadata.json: el "jar-in-jar" de Forge, que los carga como mods normales (en el cliente y en el
  servidor; en un servidor dedicado Oculus se queda quieto: com.darkcraft.arranque.Arranque).
- Las licencias (LGPL-3.0 y la GPL-3.0 a la que remite) y los creditos van en META-INF/darkcraft/ y en
  SALIDA/creditos_mods.txt (tambien para la pagina de la release).
- El shader (Complementary) NO se puede meter en el jar (su licencia pide que se anada con Modrinth o CurseForge): se
  deja en SALIDA/shaderpacks/ para la prueba del cliente y SALIDA/shader.json (nombre, url, hashes, tamano) para el
  modpack y el instalador, que lo bajan de Modrinth.
Si algo falla, sale con error: un jar sin sus mods no se publica.
"""
import hashlib
import json
import os
import sys
import time
import urllib.parse
import urllib.request
import zipfile

JAR, LISTA, OUT = sys.argv[1], sys.argv[2], sys.argv[3]
AQUI = os.path.dirname(os.path.abspath(__file__))
H = {"User-Agent": "ferranxd7/ciudadela-gotica (Dark Craft CI)"}
os.makedirs(OUT, exist_ok=True)


def get(url, intentos=4):
    for i in range(intentos):
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers=H), timeout=90) as r:
                return r.read()
        except Exception as e:  # la red de la CI a veces falla: se reintenta
            if i == intentos - 1:
                raise
            print(f"  reintento ({e})")
            time.sleep(5 * (i + 1))


def versiones(proyecto, cargadores):
    q = urllib.parse.urlencode({"loaders": json.dumps(cargadores), "game_versions": json.dumps(["1.20.1"])})
    return json.loads(get(f"https://api.modrinth.com/v2/project/{proyecto}/version?{q}"))


def elegir(m, cargadores):
    vs = versiones(m["proyecto"], cargadores)
    if not vs:
        raise SystemExit(f"ERROR: {m['proyecto']} no tiene versiones para {cargadores} y 1.20.1")
    for v in vs:
        if v["version_number"] == m["version"]:
            return v, ""
    otras = [v for v in vs if v["version_number"].startswith(m.get("prefijo", "\0"))]
    nums = ", ".join(v["version_number"] for v in vs[:12])
    if not otras:
        raise SystemExit(f"ERROR: {m['proyecto']} {m['version']} no esta (hay: {nums})")
    return otras[0], f" (AVISO: {m['version']} no esta; hay: {nums})"


def bajar(v, carpeta):
    f = next((x for x in v["files"] if x.get("primary")), v["files"][0])
    datos = get(f["url"])
    sha = hashlib.sha512(datos).hexdigest()
    if sha != f["hashes"]["sha512"]:
        raise SystemExit(f"ERROR: el sha512 de {f['filename']} no cuadra")
    os.makedirs(carpeta, exist_ok=True)
    ruta = os.path.join(carpeta, f["filename"])
    with open(ruta, "wb") as o:
        o.write(datos)
    return f, ruta, datos


lista = json.load(open(LISTA, encoding="utf-8"))
informe, meta, creditos = [], [], []
creditos += [
    "DARK CRAFT LLEVA DENTRO ESTOS MODS (sin ningun cambio)",
    "",
    "Van en META-INF/jarjar/ y Forge los carga como mods normales (jar-in-jar). Los tres son software libre con la",
    "licencia GNU LGPL-3.0 (en META-INF/darkcraft/LGPL-3.0.txt, que remite a la GNU GPL-3.0: GPL-3.0.txt). Su codigo",
    "fuente, en las direcciones de abajo (version exacta indicada); si alguna deja de funcionar, se puede pedir en las",
    "issues de https://github.com/ferranxd7/ciudadela-gotica. Cada uno se puede cambiar por otra version: cambia su jar",
    "en META-INF/jarjar/ (un .jar es un .zip) o pon una mas nueva en la carpeta mods (Forge carga la mas nueva); Dark",
    "Craft los usa solo por su API.",
    "",
]
dentro = os.path.join(OUT, "dentro")
for m in lista["mods"]:
    v, aviso = elegir(m, ["forge"])
    f, ruta, datos = bajar(v, dentro)
    informe.append(f"OK {m['nombre']} {v['version_number']} -> {f['filename']} ({len(datos) // 1024} KiB, sha512 bien){aviso}")
    meta.append({
        "identifier": {"group": "maven.modrinth", "artifact": m["proyecto"]},
        "version": {"range": f"[{v['version_number']}]", "artifactVersion": v["version_number"]},
        "path": "META-INF/jarjar/" + f["filename"],
        "isObfuscated": False,
    })
    m["_fichero"], m["_version"], m["_ruta"] = f["filename"], v["version_number"], ruta
    creditos += [
        f"- {m['nombre']} {v['version_number']} ({f['filename']})",
        f"  De {m['autores']}. Licencia {m['licencia']}.",
        f"  Codigo fuente: {m['fuente']} (version {v['version_number']}). Pagina: {m['pagina']}",
        f"  sha512 {f['hashes']['sha512']}",
        "",
    ]

# el shader: se baja de Modrinth (no va dentro del jar)
s = lista["shader"]
try:
    v, aviso = elegir(s, ["iris"])
    f, ruta, datos = bajar(v, os.path.join(OUT, "shaderpacks"))
    json.dump({"nombre": s["nombre"], "version": v["version_number"], "fichero": f["filename"], "url": f["url"],
               "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]}, "tamano": f["size"]},
              open(os.path.join(OUT, "shader.json"), "w"), indent=1)
    informe.append(f"OK shader {s['nombre']} {v['version_number']} -> {f['filename']} ({len(datos) // 1024} KiB){aviso}")
    creditos += [
        "SHADERS (no van dentro del jar: los baja el instalador o el modpack desde Modrinth)",
        "",
        f"- {s['nombre']} {v['version_number']} ({f['filename']}), de {s['autores']}: {s['pagina']}",
        "  Con su licencia (Complementary License Agreement, dentro del zip), sin cambios. Dark Craft lo enciende por",
        "  defecto la primera vez; cualquier problema que de dentro de Dark Craft es responsabilidad de Dark Craft, no",
        "  de Complementary Development.",
        "",
    ]
except SystemExit as e:  # sin shader el mod va igual: se avisa
    informe.append(f"AVISO shader: {e}")
except Exception as e:
    informe.append(f"AVISO shader: {e}")

texto = "\n".join(creditos) + "\n"
with zipfile.ZipFile(JAR, "a") as z:
    ya = set(z.namelist())
    if "META-INF/jarjar/metadata.json" in ya:
        raise SystemExit("ERROR: el jar ya tiene META-INF/jarjar/metadata.json")
    for m in lista["mods"]:
        # sin comprimir: ya van comprimidos y asi Forge los lee directamente
        z.write(m["_ruta"], "META-INF/jarjar/" + m["_fichero"], compress_type=zipfile.ZIP_STORED)
    z.writestr("META-INF/jarjar/metadata.json", json.dumps({"jars": meta}, indent=2))
    z.write(os.path.join(AQUI, "LGPL-3.0.txt"), "META-INF/darkcraft/LGPL-3.0.txt", compress_type=zipfile.ZIP_DEFLATED)
    z.write(os.path.join(AQUI, "GPL-3.0.txt"), "META-INF/darkcraft/GPL-3.0.txt", compress_type=zipfile.ZIP_DEFLATED)
    z.writestr("META-INF/darkcraft/CREDITOS-MODS.txt", texto, compress_type=zipfile.ZIP_DEFLATED)
open(os.path.join(OUT, "creditos_mods.txt"), "w", encoding="utf-8").write(texto)
json.dump([{"modid": m["modid"], "nombre": m["nombre"], "version": m["_version"], "fichero": m["_fichero"]} for m in lista["mods"]],
          open(os.path.join(OUT, "mods_dentro.json"), "w"), indent=1)
informe.append(f"jar: {JAR} ({os.path.getsize(JAR) // 1024} KiB)")
print("\n".join(informe))
