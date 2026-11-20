"""Descarga mods de Modrinth (Forge 1.20.1) con sus dependencias obligatorias."""
import json, os, sys, urllib.request, urllib.parse

OUT = sys.argv[1]
SLUGS = sys.argv[2:]
os.makedirs(OUT, exist_ok=True)
H = {"User-Agent": "ciudadela-gotica-ci/1.0"}

def get(url):
    with urllib.request.urlopen(urllib.request.Request(url, headers=H), timeout=60) as r:
        return json.load(r)

done = set()
log = []
files = []

def fetch(pid):
    if pid in done:
        return
    done.add(pid)
    q = urllib.parse.urlencode({"loaders": json.dumps(["forge"]), "game_versions": json.dumps(["1.20.1"])})
    try:
        vs = get(f"https://api.modrinth.com/v2/project/{pid}/version?{q}")
    except Exception as e:
        log.append(f"FALLO {pid}: {e}")
        return
    if not vs:
        log.append(f"SIN VERSION {pid}")
        return
    v = vs[0]
    f = next((x for x in v["files"] if x.get("primary")), v["files"][0])
    path = os.path.join(OUT, f["filename"])
    urllib.request.urlretrieve(f["url"], path)
    log.append(f"OK {pid} -> {f['filename']}")
    files.append({"path": "mods/" + f["filename"], "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]},
                  "env": {"client": "required", "server": "required"}, "downloads": [f["url"]], "fileSize": f["size"]})
    for d in v.get("dependencies", []):
        if d.get("dependency_type") == "required" and d.get("project_id"):
            fetch(d["project_id"])

for s in SLUGS:
    fetch(s)
print("\n".join(log))
open(os.path.join(OUT, "..", "mods_descargados.txt"), "w").write("\n".join(log))

json.dump(files, open(os.path.join(OUT, "..", "mrpack_files.json"), "w"), indent=1)
