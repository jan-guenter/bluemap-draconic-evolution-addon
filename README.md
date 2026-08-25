# BlueMap Draconic Evolution Add-on

A Java 21 BlueMap add-on for the exact `draconicevolution-3.1.4.632` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: visual-review prototype. The exact artifact gate and BlueMap 5.22
adapter load fourteen deterministic static models from the operator-installed
Draconic Evolution JAR. The implementation covers nine energy crystals,
Draconium Chest, Chaos Crystal, and the three reactor components.

## Build

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

The prototype freezes animation and activity in neutral poses. Links, beams,
shields, particles, displayed-item contents, formed Energy Core structures,
and unsupported states stay stock unless the owner expands scope.

No Draconic Evolution binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
