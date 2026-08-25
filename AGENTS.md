# Agent guide for the Draconic Evolution BlueMap add-on

This is an independent public add-on repository generated from the private
All the Mons orchestration scaffold. Read this file and `README.md` before
changing it.

## Exact baseline

- All the Mons `1.2.0`, pack commit `c7bb230f21d14d26859d0b92548f089b3a493ad9`
- Minecraft `1.21.1`
- NeoForge `21.1.248`
- Java `21`
- BlueMap `5.22-agent.backport-5.22-mc1.21.1-2`, commit `9be321df995a1103808621d529eb72773e719d4d`
- BlueMap API commit `285c9a60eff3ac2b0cab308ce1058d1565be0971`
- Exact profile `draconicevolution-3.1.4.632`

This is a standalone BlueMap add-on, not a NeoForge mod. Do not add client
classes, candidate binaries/assets/source, nested JARs, Minecraft classes,
Mixins, or world state.

## Development contract

- Preserve stock rendering while the runtime/profile is absent, duplicated,
  unsupported, malformed, disabled, or not yet implemented.
- Keep the BlueMap internal API behind `adapter/bluemap522`.
- Keep exact candidate identities and resource contracts in the profile.
- Keep state/NBT decoding, normalized data, and mesh emission separate.
- Unknown family data gets one bounded diagnostic and stock fallback.
- Use installed resources only after exact-artifact admission.
- Gallery cases and renderer facts are family-owned; do not move them back to
  the generic scaffold.

`SCAFFOLD_NOT_IMPLEMENTED` is permitted only during the fast prototype phase.
The release gate rejects it.

## Commands

Compile and test the safe seed:

```bash
gradle --no-daemon -PbluemapSourcePath=../bluemap-backport clean check build
```

Verify a prototype with exact candidate JAR properties:

- `-PdraconicEvolutionJar=/path/to/Draconic-Evolution-1.21.1-3.1.4.632.jar`

Pass those properties to Gradle and run `prototypeCheck`. Run
`verifyReleaseCandidate -PreleaseTag=v<version>` only after owner visual
acceptance and release sealing. Follow `docs/EXECUTION.md` for the reusable
prototype, acceptance, promotion and publication sequence.

Never stage or commit generated build output, candidate JARs, galleries, worlds,
credentials, logs, or research evidence.
