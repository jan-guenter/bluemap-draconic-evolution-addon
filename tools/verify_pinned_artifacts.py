#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Verify exact candidate bytes and NeoForge mod declarations."""

from __future__ import annotations

import argparse
import hashlib
import json
from pathlib import Path
import re
import sys
import zipfile


DESCRIPTOR = "META-INF/neoforge.mods.toml"
MAX_DESCRIPTOR_BYTES = 1024 * 1024


def parse_artifact(value: str) -> tuple[str, Path]:
    key, separator, path = value.partition("=")
    if not separator or not key or not path:
        raise ValueError(f"artifact must be KEY=PATH: {value!r}")
    return key, Path(path)


def digest(path: Path) -> str:
    value = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(64 * 1024), b""):
            value.update(chunk)
    return value.hexdigest()


def declares_mod(path: Path, expected_mod_id: str) -> bool:
    declaration = re.compile(
        r"^(?:modId|\"modId\"|'modId')\s*=\s*"
        + rf"(?:\"{re.escape(expected_mod_id)}\"|'"
        + re.escape(expected_mod_id)
        + r"')$"
    )
    with zipfile.ZipFile(path) as archive:
        try:
            info = archive.getinfo(DESCRIPTOR)
        except KeyError:
            return False
        if info.is_dir() or info.file_size > MAX_DESCRIPTOR_BYTES:
            return False
        payload = archive.read(info)
    if len(payload) > MAX_DESCRIPTOR_BYTES:
        return False
    descriptor = payload.decode("utf-8", errors="strict")
    in_mods = False
    for line in descriptor.removeprefix("\ufeff").splitlines():
        statement = line.split("#", 1)[0].strip()
        if statement.startswith("["):
            in_mods = statement in {"[[mods]]", "[[\"mods\"]]", "[['mods']]"}
        elif in_mods and declaration.fullmatch(statement):
            return True
    return False


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--manifest", type=Path, required=True)
    parser.add_argument("--artifact", action="append", default=[])
    args = parser.parse_args()

    manifest = json.loads(args.manifest.read_text(encoding="utf-8"))
    pins = {row["key"]: row for row in manifest["artifacts"]}
    supplied_pairs = [parse_artifact(value) for value in args.artifact]
    supplied = dict(supplied_pairs)
    if len(supplied) != len(supplied_pairs):
        raise ValueError("artifact keys must be unique")
    if set(supplied) != set(pins):
        raise ValueError(
            f"artifact key mismatch: supplied={sorted(supplied)}, expected={sorted(pins)}"
        )

    for key, pin in sorted(pins.items()):
        path = supplied[key]
        if not path.is_file():
            raise ValueError(f"artifact is not a file: {path}")
        actual_size = path.stat().st_size
        actual_sha256 = digest(path)
        if actual_size != pin["size"] or actual_sha256 != pin["sha256"]:
            raise ValueError(
                f"{key} byte identity mismatch: {actual_size} bytes, "
                f"SHA-256 {actual_sha256}"
            )
        if not declares_mod(path, pin["mod_id"]):
            raise ValueError(f"{key} does not declare exact mod ID {pin['mod_id']}")
        print(f"verified {key}: {actual_size} bytes, SHA-256 {actual_sha256}")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, UnicodeError, ValueError, zipfile.BadZipFile) as error:
        print(f"artifact verification failed: {error}", file=sys.stderr)
        raise SystemExit(1)
