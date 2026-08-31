# BlueMap Draconic Evolution Add-on

A Java 21 BlueMap 5.23 feature-backport add-on for the exact
`draconicevolution-3.1.4.632` profile in All the Mons `1.2.0` / Minecraft
`1.21.1`.

Status: owner-accepted `0.1.0-alpha.2` release candidate. The exact artifact
gate and BlueMap 5.23 adapter load fourteen deterministic models from the operator-installed
Draconic Evolution JAR. BlueMap's native texture clock drives permanent
particle rings around the six relay and wireless crystals. The three direct
I/O crystals use their tier-specific stationary center glow. Draconium Chest,
Chaos Crystal, and the three reactor components remain static.

## Build

Clone with `--recurse-submodules`, or initialize an existing checkout with
the two pinned support modules:

```bash
git submodule update --init --recursive -- \
  tooling/bluemap-addon-toolkit modules/bluemap-addon-adapter-api
```

The settings preflight accepts only their committed gitlinks and rejects an
uninitialized, changed, dirty, or incorrectly pinned checkout.

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the comparison
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

## Install

Place the production JAR in BlueMap's add-on pack directory and restart the
BlueMap JVM. Removal plus one restart restores stock behavior; the add-on
creates no custom world state.

Set `-Dbluemap.draconicevolution.disabled=true` to leave the exact profile inactive.

## Scope boundary

Relay and wireless effects use eleven sampled particle poses over a condensed
88-tick orbit that remains legible at BlueMap scale. Their tier-colored energy
particles and red wireless or cyan relay particles share one native BlueMap
animation clock. Full-opacity stepped poses keep the small effects readable
without JavaScript, UI, or marker overlays. Crystal bodies remain static, as
do the direct I/O bases beneath their stationary glow. The client's procedural
shader shimmer, slower orbit timing, and dense camera-dependent particle count
stay bounded approximations.

Links, transfer beams, shields, displayed-item contents, formed Energy Core
structures, and unsupported states stay stock unless the owner expands scope.

No Draconic Evolution binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
