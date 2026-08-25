#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Family-owned placeholder cases for the generated gallery."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "draconic_evolution_gallery"
ENVELOPE = (174, 99, 173, 178, 103, 177)


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str


# SCAFFOLD_NOT_IMPLEMENTED: replace this stock-only row with the smallest
# observed Draconic Evolution defect fixture plus one or two stock controls.
PLACEMENTS = (
    Placement(
        "stock-control",
        "stone stock rendering control",
        176,
        100,
        175,
        "minecraft:stone",
        "stock-visible",
    ),
)
