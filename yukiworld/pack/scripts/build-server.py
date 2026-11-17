#!/usr/bin/env python3
"""Собирает серверный архив «Claude Server»: конфиги, YukiCore и загрузчик серверных модов (с хэшами)."""
import os
import shutil
import sys
import tomllib
import urllib.parse
import zipfile
from pathlib import Path

HERE = Path(__file__).resolve().parent.parent
BUILD = HERE / "build"
PACK = BUILD / "YukiWorld"
OVR = HERE / "server"


def main():
    version = tomllib.loads((HERE / "pack.toml").read_text(encoding="utf-8"))["version"]
    root = BUILD / "server" / "Claude Server"
    shutil.rmtree(root.parent, ignore_errors=True)
    (root / "mods").mkdir(parents=True)
    shutil.copytree(PACK / "config", root / "config")
    shutil.copytree(OVR / "config", root / "config", dirs_exist_ok=True)
    for jar in (PACK / "mods").glob("*.jar"):
        shutil.copy(jar, root / "mods")
    rows, skipped = [], 0
    for f in sorted((PACK / "mods").glob("*.pw.toml")):
        t = tomllib.loads(f.read_text(encoding="utf-8"))
        if t.get("side") == "client":
            skipped += 1
            continue
        dl = t["download"]
        url = dl.get("url")
        if not url:
            fid = t["update"]["curseforge"]["file-id"]
            url = f"https://mediafilez.forgecdn.net/files/{fid // 1000}/{fid % 1000}/{urllib.parse.quote(t['filename'])}"
        rows.append(";".join((t["filename"], url, dl["hash-format"], dl["hash"])))
    (root / "mods-list.csv").write_text("file;url;algo;hash\n" + "\n".join(rows) + "\n", encoding="utf-8")
    # PowerShell 5 читает UTF-8 только с BOM, .bat и инструкция — с CRLF
    ps = (OVR / "download-mods.ps1").read_text(encoding="utf-8-sig")
    (root / "download-mods.ps1").write_text(ps, encoding="utf-8-sig", newline="\r\n")
    guide = (OVR / "ИНСТРУКЦИЯ.txt").read_text(encoding="utf-8").replace("{VERSION}", version)
    (root / "ИНСТРУКЦИЯ.txt").write_text(guide, encoding="utf-8-sig", newline="\r\n")
    for name in ("1_СКАЧАТЬ_МОДЫ.bat", "server.properties", "user_jvm_args.txt"):
        shutil.copy(OVR / name, root / name)
    out = BUILD / f"YukiWorld-Server-{version}.zip"
    with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
        for d, _, files in os.walk(root):
            for fn in files:
                p = Path(d) / fn
                z.write(p, p.relative_to(root.parent))
    print(f"🖥  Серверный архив: {out} ({len(rows)} модов, {skipped} клиентских пропущено)")


if __name__ == "__main__":
    sys.exit(main())
