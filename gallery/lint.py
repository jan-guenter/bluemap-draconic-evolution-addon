#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated placeholder gallery without starting Minecraft."""

from __future__ import annotations

import json
from pathlib import Path
import re
import sys

sys.dont_write_bytecode = True
import cases
import generate


ROOT = Path(__file__).resolve().parent


def main() -> int:
    for relative, payload in generate.generated_files().items():
        path = ROOT / relative
        if not path.is_file() or path.read_bytes() != payload:
            raise ValueError(f"generated file differs: {relative}")

    json.loads((ROOT / "datapack/pack.mcmeta").read_text(encoding="utf-8"))
    load_tag = json.loads(
        (ROOT / "datapack/data/minecraft/tags/function/load.json").read_text(
            encoding="utf-8"
        )
    )
    if load_tag != {"values": [f"{cases.NAMESPACE}:load"]}:
        raise ValueError("load tag differs from the exact namespace")
    if len(cases.PLACEMENTS) != 1:
        raise ValueError("placeholder must contain exactly one stock control")

    placement = cases.PLACEMENTS[0]
    if placement.block_state != "minecraft:stone" or placement.expected != (
        "stock-visible"
    ):
        raise ValueError("placeholder must remain an honest stone stock control")
    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = (
        cases.ENVELOPE
    )
    if not (
        minimum_x <= placement.x <= maximum_x
        and minimum_y <= placement.y <= maximum_y
        and minimum_z <= placement.z <= maximum_z
    ):
        raise ValueError("placeholder placement escaped its bounded envelope")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != 1:
        raise ValueError("placeholder must place exactly one block")
    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden placeholder command: {forbidden}")
    print("placeholder gallery lint passed: one bounded stock control")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
