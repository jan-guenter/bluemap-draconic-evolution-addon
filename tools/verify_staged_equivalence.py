#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Write or verify owner-accepted non-manifest JAR entry identities."""

from __future__ import annotations

import argparse
import hashlib
from pathlib import Path
import re
import sys
import zipfile


LINE = re.compile(r"([0-9a-f]{64})  ([^\r\n]+)")
IGNORED = {"META-INF/MANIFEST.MF"}


def load_entries(path: Path) -> dict[str, str]:
    result = {}
    for number, line in enumerate(path.read_text(encoding="utf-8").splitlines(), start=1):
        match = LINE.fullmatch(line)
        if match is None:
            raise ValueError(f"invalid entry manifest line {number}")
        digest, name = match.groups()
        if name.startswith("/") or ".." in Path(name).parts or name in result:
            raise ValueError(f"unsafe or duplicate entry on line {number}: {name}")
        result[name] = digest
    if not result:
        raise ValueError("accepted staging entry manifest is empty")
    return result


def jar_entries(archive: zipfile.ZipFile) -> dict[str, str]:
    result: dict[str, str] = {}
    for info in archive.infolist():
        name = info.filename
        if info.is_dir() or name in IGNORED:
            continue
        if name.startswith("/") or ".." in Path(name).parts or name in result:
            raise ValueError(f"unsafe or duplicate JAR entry: {name}")
        result[name] = hashlib.sha256(archive.read(info)).hexdigest()
    if not result:
        raise ValueError("JAR contains no accepted non-manifest entries")
    return result


def write_entries(jar: Path, entries: Path) -> None:
    with zipfile.ZipFile(jar) as archive:
        accepted = jar_entries(archive)
    payload = "".join(
        f"{digest}  {name}\n" for name, digest in sorted(accepted.items())
    )
    try:
        with entries.open("x", encoding="utf-8", newline="\n") as output:
            output.write(payload)
    except FileExistsError as error:
        raise ValueError(f"refusing to overwrite accepted entries: {entries}") from error
    print(f"wrote {len(accepted)} accepted non-manifest entries to {entries}")


def manifest_version(archive: zipfile.ZipFile) -> str:
    try:
        payload = archive.read("META-INF/MANIFEST.MF")
    except KeyError as error:
        raise ValueError("release JAR is missing META-INF/MANIFEST.MF") from error
    manifest = payload.decode("utf-8", errors="strict")
    for line in manifest.splitlines():
        if line.startswith("Implementation-Version: "):
            return line.removeprefix("Implementation-Version: ")
    raise ValueError("release JAR manifest lacks Implementation-Version")


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--jar", type=Path, required=True)
    parser.add_argument("--entries", type=Path, required=True)
    parser.add_argument("--write", action="store_true")
    parser.add_argument("--expected-version")
    args = parser.parse_args()

    if args.write:
        if args.expected_version is not None:
            raise ValueError("--write cannot be combined with --expected-version")
        write_entries(args.jar, args.entries)
        return 0
    if not args.expected_version:
        raise ValueError("--expected-version is required when verifying")

    expected = load_entries(args.entries)
    with zipfile.ZipFile(args.jar) as archive:
        actual = jar_entries(archive)
        if set(actual) != set(expected):
            raise ValueError(
                f"entry set differs: missing={sorted(set(expected) - set(actual))}, "
                f"extra={sorted(set(actual) - set(expected))}"
            )
        for name, accepted_sha256 in sorted(expected.items()):
            if actual[name] != accepted_sha256:
                raise ValueError(
                    f"accepted staging entry differs: {name}: {actual[name]}"
                )
        actual_version = manifest_version(archive)
    if actual_version != args.expected_version:
        raise ValueError(
            f"release manifest version differs: {actual_version!r} "
            f"!= {args.expected_version!r}"
        )
    print(f"staged equivalence passed: {len(expected)} non-manifest entries")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError, zipfile.BadZipFile) as error:
        print(f"staged equivalence failed: {error}", file=sys.stderr)
        raise SystemExit(1)
