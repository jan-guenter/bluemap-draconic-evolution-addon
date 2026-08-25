# BlueMap Draconic Evolution Add-on

A Java 21 BlueMap add-on for the exact `draconicevolution-3.1.4.632` profile in All the Mons
`1.2.0` / Minecraft `1.21.1`.

Status: safe generated prototype. The exact artifact gate and BlueMap 5.22
adapter compile, but the family-owned renderer is intentionally absent.
BlueMap therefore retains stock rendering until the explicit
`SCAFFOLD_NOT_IMPLEMENTED` markers are replaced. A release cannot pass while
those markers remain.

## Build

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

`check` is the quick Java/checkstyle/archive gate. `prototypeCheck` additionally
requires every exact candidate JAR property and validates the placeholder
gallery. See `provenance/upstreams.json` for immutable artifact identities and
the [execution guide](docs/EXECUTION.md) for the prototype-to-release loop.

## Install

After a renderer exists, place the production JAR in BlueMap's add-on pack
directory and restart the BlueMap JVM. Removal plus one restart restores stock
behavior; the add-on creates no custom world state.

Set `-Dbluemap.draconicevolution.disabled=true` to leave the exact profile inactive.

## Scope boundary

The initial implementation must be limited to a small observed BlueMap defect.
Live contents, fill levels, activity overlays, particles, animation phase, and
unsupported states stay stock or deterministic-neutral unless the owner
explicitly expands scope.

No Draconic Evolution binary, source, class, asset, captured mesh, or gallery is
bundled in the add-on.
