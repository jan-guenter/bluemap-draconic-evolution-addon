# Placeholder gallery

This generated gallery proves only the deterministic data-pack mechanics and a
single `minecraft:stone` stock control at `(176, 100, 175)`. It does not claim
Draconic Evolution support.

Replace `cases.py` with the smallest real defect fixture and stock controls,
then keep the stable commands:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/draconicevolution-gallery.zip
```

The release gate rejects the `SCAFFOLD_NOT_IMPLEMENTED` marker in `cases.py`.
Keep gallery generation deterministic, bounded, synthetic where practical, and
free of candidate assets or captured meshes.
