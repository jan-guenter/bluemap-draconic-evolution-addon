#!/usr/bin/env python3
# SPDX-License-Identifier: MIT
"""Bounded Draconic Evolution static-rendering comparison cases."""

from __future__ import annotations

from dataclasses import dataclass


NAMESPACE = "draconic_evolution_gallery"
ENVELOPE = (170, 96, 170, 190, 106, 182)


@dataclass(frozen=True)
class Placement:
    case_id: str
    label: str
    x: int
    y: int
    z: int
    block_state: str
    expected: str


PLACEMENTS = (
    Placement("basic-io", "basic direct I/O crystal", 172, 100, 172,
              "draconicevolution:basic_io_crystal", "custom-static"),
    Placement("basic-relay", "basic relay crystal", 176, 100, 172,
              "draconicevolution:basic_relay_crystal", "custom-static"),
    Placement("basic-wireless", "basic wireless crystal", 180, 100, 172,
              "draconicevolution:basic_wireless_crystal", "custom-static"),
    Placement("wyvern-io", "wyvern direct I/O crystal", 184, 100, 172,
              "draconicevolution:wyvern_io_crystal", "custom-static"),
    Placement("wyvern-relay", "wyvern relay crystal", 188, 100, 172,
              "draconicevolution:wyvern_relay_crystal", "custom-static"),
    Placement("wyvern-wireless", "wyvern wireless crystal", 172, 100, 176,
              "draconicevolution:wyvern_wireless_crystal", "custom-static"),
    Placement("draconic-io", "draconic direct I/O crystal", 176, 100, 176,
              "draconicevolution:draconic_io_crystal", "custom-static"),
    Placement("draconic-relay", "draconic relay crystal", 180, 100, 176,
              "draconicevolution:draconic_relay_crystal", "custom-static"),
    Placement("draconic-wireless", "draconic wireless crystal", 184, 100, 176,
              "draconicevolution:draconic_wireless_crystal", "custom-static"),
    Placement("chaos-crystal", "chaos crystal static outer shell", 188, 100, 176,
              "draconicevolution:chaos_crystal", "custom-static"),
    Placement("draconium-chest", "closed Draconium chest", 172, 100, 180,
              "draconicevolution:draconium_chest", "custom-static"),
    Placement("reactor-core", "neutral reactor core", 176, 100, 180,
              "draconicevolution:reactor_core", "custom-static"),
    Placement("reactor-injector", "idle reactor injector", 180, 100, 180,
              "draconicevolution:reactor_injector", "custom-static"),
    Placement("reactor-stabilizer", "idle reactor stabilizer", 184, 100, 180,
              "draconicevolution:reactor_stabilizer", "custom-static"),
    Placement("stock-control", "stone stock rendering control", 188, 100, 180,
              "minecraft:stone", "stock-visible"),
)
