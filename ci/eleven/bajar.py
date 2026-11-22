#!/usr/bin/env python3
"""Baja los audios de ElevenLabs (voces y efectos) que estan en descargas.json a ci/eleven/master/.
Los enlaces firmados caducan (a las 00:00 UTC del dia siguiente): por eso se bajan en cuanto se generan y
los masters se guardan en el repositorio. Los que ya estan no se vuelven a bajar."""
import json
import os
import sys
import urllib.request

AQUI = os.path.dirname(os.path.abspath(__file__))
MASTER = os.path.join(AQUI, "master")


def main():
    d = json.load(open(os.path.join(AQUI, "descargas.json"), encoding="utf-8"))
    ok = fallos = ya = 0
    for e in d["descargas"]:
        dst = os.path.join(MASTER, e["archivo"])
        if os.path.exists(dst) and os.path.getsize(dst) > 1000:
            ya += 1
            continue
        os.makedirs(os.path.dirname(dst), exist_ok=True)
        try:
            req = urllib.request.Request(e["url"], headers={"User-Agent": "Mozilla/5.0"})
            with urllib.request.urlopen(req, timeout=60) as r:
                data = r.read()
            if len(data) < 1000:
                raise ValueError(f"muy pequeno ({len(data)} bytes)")
            with open(dst, "wb") as f:
                f.write(data)
            ok += 1
            print(f"OK    {e['archivo']}  {len(data) // 1024} KB")
        except Exception as ex:  # noqa: BLE001
            fallos += 1
            print(f"FALLO {e['archivo']}: {ex}")
    print(f"bajados {ok}, ya estaban {ya}, fallos {fallos}")
    return 1 if fallos and not ok else 0


if __name__ == "__main__":
    sys.exit(main())
