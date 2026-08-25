# Draconic Evolution gallery

This generated gallery places the nine energy crystals, Chaos Crystal,
Draconium Chest, three reactor components, and a stock stone control in a
bounded comparison grid centered near `(180, 100, 176)`.
The smooth-stone floor provides scale and contrast, with a clearance around the
tall Chaos Crystal model.

Use the stable commands:

```bash
python gallery/generate.py
python gallery/generate.py --check
python gallery/lint.py
bash gallery/package.sh /tmp/draconicevolution-gallery.zip
```

Keep gallery generation deterministic, bounded, synthetic where practical, and
free of candidate assets or captured meshes.
