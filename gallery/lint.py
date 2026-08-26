#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Lint the generated Draconic Evolution gallery without starting Minecraft."""

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
    minimum_x, minimum_y, minimum_z, maximum_x, maximum_y, maximum_z = (
        cases.ENVELOPE
    )
    case_ids = {placement.case_id for placement in cases.PLACEMENTS}
    positions = {(placement.x, placement.y, placement.z) for placement in cases.PLACEMENTS}
    if len(case_ids) != len(cases.PLACEMENTS) or len(positions) != len(cases.PLACEMENTS):
        raise ValueError("gallery case identifiers and positions must be unique")
    controls = [
        placement
        for placement in cases.PLACEMENTS
        if placement.block_state == "minecraft:stone"
        and placement.expected == "stock-visible"
    ]
    if len(controls) != 1:
        raise ValueError("gallery must contain exactly one stone stock control")
    for placement in cases.PLACEMENTS:
        if not (
            minimum_x <= placement.x <= maximum_x
            and minimum_y <= placement.y <= maximum_y
            and minimum_z <= placement.z <= maximum_z
        ):
            raise ValueError(f"placement escaped gallery envelope: {placement.case_id}")
        if not placement.block_state.startswith(("draconicevolution:", "minecraft:")):
            raise ValueError(f"unexpected block namespace: {placement.block_state}")

    function_root = ROOT / f"datapack/data/{cases.NAMESPACE}/function"
    functions = "\n".join(
        path.read_text(encoding="utf-8")
        for path in sorted(function_root.glob("*.mcfunction"))
    )
    if len(re.findall(r"^setblock ", functions, re.MULTILINE)) != len(
        cases.PLACEMENTS
    ):
        raise ValueError("gallery setblock count differs from the case list")
    lowered = functions.lower()
    for forbidden in ("summon ", "data merge", "op ", "deop ", "stop "):
        if forbidden in lowered:
            raise ValueError(f"forbidden gallery command: {forbidden}")
    print(f"gallery lint passed: {len(cases.PLACEMENTS)} bounded cases")
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except (OSError, ValueError) as error:
        print(f"gallery lint failed: {error}", file=sys.stderr)
        raise SystemExit(1)
