/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel.InstalledMaterial;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel.Transform;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Vertex;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/** Compiles the first static Draconic gallery models from the admitted JAR. */
public final class InstalledStaticModels {

    private static final String ROOT = "assets/draconicevolution/";
    private static final InstalledMaterial CRYSTAL_BASE = material(
            "draconicevolution:models/crystal_base", 1F, 1F, 1F
    );
    private static final Transform FULL_CRYSTAL = new Transform(
            -0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F
    );
    private static final Transform HALF_CRYSTAL = new Transform(
            0.5F, 0.5F, 0.5F, 0.5F, 0F, 0.5F
    );
    private static final Transform CENTER_HALF = new Transform(
            0.5F, 0.5F, 0.5F, 0.5F, 0.5F, 0.5F
    );
    private static final Transform CENTER_THREE_QUARTERS = new Transform(
            0.75F, 0.75F, 0.75F, 0.5F, 0.5F, 0.5F
    );
    private static final Transform IDENTITY = new Transform(
            1F, 1F, 1F, 0F, 0F, 0F
    );

    private InstalledStaticModels() {
    }

    public static Map<String, InstalledStaticModel> load(Path jar) throws IOException {
        Map<String, InstalledStaticModel> result = new LinkedHashMap<>();
        try (ZipFile zip = new ZipFile(jar.toFile())) {
            WavefrontModel fullCrystal = reverse(parse(zip, "models/block/crystal.obj"));
            WavefrontModel halfCrystal = parse(zip, "models/block/crystal_half.obj");
            addCrystalTier(result, "basic", fullCrystal, halfCrystal,
                    tint(0F, 0.35F, 0.65F));
            addCrystalTier(result, "wyvern", fullCrystal, halfCrystal,
                    tint(0.55F, 0.3F, 0.9F));
            addCrystalTier(result, "draconic", fullCrystal, halfCrystal,
                    tint(1F, 0.572F, 0.172F));

            add(result, object("draconicevolution:reactor_core",
                    parse(zip, "models/block/reactor/reactor_core.obj"),
                    material("draconicevolution:block/reactor/reactor_core", 1F, 1F, 1F),
                    CENTER_HALF, false));
            add(result, object("draconicevolution:reactor_injector",
                    componentUp(backfaced(parse(
                            zip, "models/block/reactor/reactor_injector.obj"
                    ))),
                    material("draconicevolution:block/reactor/reactor_injector", 1F, 1F, 1F),
                    IDENTITY, false));
            add(result, object("draconicevolution:reactor_stabilizer",
                    componentUp(backfaced(stabilizerNeutral(parse(
                            zip, "models/block/reactor/reactor_stabilizer.obj"
                    )))),
                    material("draconicevolution:block/reactor/reactor_stabilizer", 1F, 1F, 1F),
                    IDENTITY, false));
            add(result, object("draconicevolution:chaos_crystal",
                    parse(zip, "models/block/chaos_crystal.obj"),
                    material("draconicevolution:block/chaos_crystal", 1F, 1F, 1F),
                    CENTER_THREE_QUARTERS, false));
            add(result, object("draconicevolution:draconium_chest", chest(),
                    material("draconicevolution:block/draconium_chest",
                            100F / 255F, 0F, 150F / 255F), IDENTITY, true));
        } catch (RuntimeException exception) {
            throw new IOException("invalid installed Draconic resources", exception);
        }
        return Map.copyOf(result);
    }

    private static void addCrystalTier(
            Map<String, InstalledStaticModel> result,
            String tier,
            WavefrontModel full,
            WavefrontModel half,
            InstalledMaterial crystal
    ) throws IOException {
        add(result, object("draconicevolution:" + tier + "_wireless_crystal",
                full, crystal, FULL_CRYSTAL, false));
        add(result, object("draconicevolution:" + tier + "_relay_crystal",
                full, crystal, FULL_CRYSTAL, false));
        Map<String, InstalledMaterial> groups = Map.of(
                "Base", CRYSTAL_BASE,
                "Crystal", crystal
        );
        add(result, new InstalledStaticModel(
                "draconicevolution:" + tier + "_io_crystal",
                half, groups, crystal, HALF_CRYSTAL, false
        ));
    }

    private static InstalledStaticModel object(
            String id,
            WavefrontModel model,
            InstalledMaterial material,
            Transform transform,
            boolean rotate
    ) {
        return new InstalledStaticModel(id, model, Map.of(), material, transform, rotate);
    }

    private static void add(
            Map<String, InstalledStaticModel> result,
            InstalledStaticModel model
    ) throws IOException {
        if (result.put(model.blockId(), model) != null) {
            throw new IOException("duplicate installed model " + model.blockId());
        }
    }

    private static WavefrontModel parse(ZipFile zip, String path) throws IOException {
        return WavefrontParser.parse(read(zip, ROOT + path));
    }

    private static WavefrontModel reverse(WavefrontModel model) {
        return new WavefrontModel(model.triangles().stream().map(triangle ->
                new Triangle(
                        triangle.first(), triangle.third(), triangle.second(), triangle.group()
                )
        ).toList());
    }

    private static WavefrontModel backfaced(WavefrontModel model) {
        List<Triangle> triangles = new ArrayList<>(model.triangles().size() * 2);
        for (Triangle triangle : model.triangles()) {
            triangles.add(triangle);
            triangles.add(new Triangle(
                    triangle.first(), triangle.third(), triangle.second(), triangle.group()
            ));
        }
        return new WavefrontModel(triangles);
    }

    private static WavefrontModel componentUp(WavefrontModel model) {
        return mapVertices(model, vertex -> new Vertex(
                vertex.x() + 0.5F,
                0.5F - vertex.z(),
                vertex.y(),
                vertex.u(),
                vertex.v()
        ));
    }

    private static WavefrontModel stabilizerNeutral(WavefrontModel model) {
        List<Triangle> triangles = new ArrayList<>();
        for (Triangle triangle : model.triangles()) {
            if (triangle.group().startsWith("ring")) {
                for (int turn = 0; turn < 4; turn++) {
                    triangles.add(rotateZ(triangle, turn));
                }
            } else if (triangle.group().startsWith("focus_panel")) {
                Triangle tilted = rotateXNegative45(triangle);
                for (int turn = 0; turn < 4; turn++) {
                    triangles.add(rotateZ(tilted, turn));
                }
            } else {
                triangles.add(triangle);
            }
        }
        return new WavefrontModel(triangles);
    }

    private static WavefrontModel mapVertices(
            WavefrontModel model,
            java.util.function.UnaryOperator<Vertex> operation
    ) {
        return new WavefrontModel(model.triangles().stream().map(triangle ->
                mapVertices(triangle, operation)
        ).toList());
    }

    private static Triangle mapVertices(
            Triangle triangle,
            java.util.function.UnaryOperator<Vertex> operation
    ) {
        return new Triangle(
                operation.apply(triangle.first()),
                operation.apply(triangle.second()),
                operation.apply(triangle.third()),
                triangle.group()
        );
    }

    private static Triangle rotateZ(Triangle triangle, int turns) {
        return mapVertices(triangle, vertex -> {
            float x = vertex.x();
            float y = vertex.y() - 0.5F;
            return switch (turns) {
                case 0 -> vertex;
                case 1 -> new Vertex(-y, x + 0.5F, vertex.z(), vertex.u(), vertex.v());
                case 2 -> new Vertex(-x, 0.5F - y, vertex.z(), vertex.u(), vertex.v());
                case 3 -> new Vertex(y, 0.5F - x, vertex.z(), vertex.u(), vertex.v());
                default -> throw new IllegalArgumentException("invalid quarter turn");
            };
        });
    }

    private static Triangle rotateXNegative45(Triangle triangle) {
        double radians = -Math.PI / 4D;
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        return mapVertices(triangle, vertex -> {
            float y = vertex.y() - 0.9375F;
            float z = vertex.z() + 0.59375F;
            return new Vertex(
                    vertex.x(),
                    0.9375F + y * cosine - z * sine,
                    -0.59375F + y * sine + z * cosine,
                    vertex.u(),
                    vertex.v()
            );
        });
    }

    private static byte[] read(ZipFile zip, String path) throws IOException {
        ZipEntry entry = zip.getEntry(path);
        if (entry == null || entry.isDirectory() || entry.getSize() > 2_000_000) {
            throw new IOException("missing or oversized installed resource " + path);
        }
        try (InputStream input = zip.getInputStream(entry)) {
            byte[] raw = input.readNBytes(2_000_001);
            if (raw.length > 2_000_000) {
                throw new IOException("oversized installed resource " + path);
            }
            return raw;
        }
    }

    private static InstalledMaterial tint(float red, float green, float blue) {
        return material("draconicevolution:models/crystal_no_shader", red, green, blue);
    }

    private static InstalledMaterial material(
            String texture,
            float red,
            float green,
            float blue
    ) {
        return new InstalledMaterial(Key.parse(texture), red, green, blue);
    }

    private static WavefrontModel chest() {
        List<Triangle> triangles = new ArrayList<>();
        box(triangles, 1, 0, 1, 15, 10, 15, 0, 19, 14, 10, 14);
        box(triangles, 1, 9, 1, 15, 14, 15, 0, 0, 14, 5, 14);
        box(triangles, 7, 7, 15, 9, 11, 16, 0, 0, 2, 4, 1);
        return new WavefrontModel(triangles);
    }

    private static void box(
            List<Triangle> target,
            float x0,
            float y0,
            float z0,
            float x1,
            float y1,
            float z1,
            float textureU,
            float textureV,
            float width,
            float height,
            float depth
    ) {
        x0 /= 16F;
        y0 /= 16F;
        z0 /= 16F;
        x1 /= 16F;
        y1 /= 16F;
        z1 /= 16F;
        face(target, vertex(x0, y0, z0), vertex(x0, y0, z1),
                vertex(x0, y1, z1), vertex(x0, y1, z0),
                textureU, textureV + depth, textureU + depth, textureV + depth + height);
        face(target, vertex(x1, y0, z1), vertex(x1, y0, z0),
                vertex(x1, y1, z0), vertex(x1, y1, z1),
                textureU + depth + width, textureV + depth,
                textureU + 2 * depth + width, textureV + depth + height);
        face(target, vertex(x1, y0, z0), vertex(x0, y0, z0),
                vertex(x0, y1, z0), vertex(x1, y1, z0),
                textureU + depth, textureV + depth,
                textureU + depth + width, textureV + depth + height);
        face(target, vertex(x0, y0, z1), vertex(x1, y0, z1),
                vertex(x1, y1, z1), vertex(x0, y1, z1),
                textureU + 2 * depth + width, textureV + depth,
                textureU + 2 * depth + 2 * width, textureV + depth + height);
        face(target, vertex(x0, y1, z1), vertex(x1, y1, z1),
                vertex(x1, y1, z0), vertex(x0, y1, z0),
                textureU + depth, textureV,
                textureU + depth + width, textureV + depth);
        face(target, vertex(x0, y0, z0), vertex(x1, y0, z0),
                vertex(x1, y0, z1), vertex(x0, y0, z1),
                textureU + depth + width, textureV,
                textureU + depth + 2 * width, textureV + depth);
    }

    private static Vertex vertex(float x, float y, float z) {
        return new Vertex(x, y, z, 0F, 0F);
    }

    private static void face(
            List<Triangle> target,
            Vertex first,
            Vertex second,
            Vertex third,
            Vertex fourth,
            float u0,
            float v0,
            float u1,
            float v1
    ) {
        Vertex a = uv(first, u0, v1);
        Vertex b = uv(second, u1, v1);
        Vertex c = uv(third, u1, v0);
        Vertex d = uv(fourth, u0, v0);
        target.add(new Triangle(a, b, c, "chest"));
        target.add(new Triangle(a, c, d, "chest"));
    }

    private static Vertex uv(Vertex vertex, float u, float v) {
        return new Vertex(vertex.x(), vertex.y(), vertex.z(), u / 64F, v / 64F);
    }
}
