/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.draconicevolution.model.EnergyCrystalAnimation;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel.InstalledMaterial;
import io.github.janguenter.bluemap.draconicevolution.model.StaticModelTransform;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Vertex;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Emits one deterministic model using textures from the admitted resource pack. */
final class StaticModelEmitter {

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;
    private final List<Key> crystalPoseTextures;

    StaticModelEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            List<Key> crystalPoseTextures
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
        this.crystalPoseTextures = List.copyOf(crystalPoseTextures);
    }

    boolean emit(
            InstalledStaticModel installed,
            String facing,
            BlockNeighborhood block,
            TileModelView target,
            Color mapColor
    ) {
        Map<InstalledMaterial, Material> materials = resolve(installed);
        if (materials == null) {
            return false;
        }
        List<Material> poseMaterials = resolvePoseMaterials(installed);
        boolean animated = poseMaterials.size() == EnergyCrystalAnimation.POSE_COUNT;
        int start = target.getTileModel().size();
        for (Triangle source : installed.model().triangles()) {
            Triangle triangle = StaticModelTransform.triangle(
                    source, installed.transform(), installed.rotateWithHorizontalFacing(), facing
            );
            InstalledMaterial logical = installed.material(source.group());
            Material staticMaterial = materials.get(logical);
            if (staticMaterial == null) {
                return false;
            }
            if (animated && EnergyCrystalAnimation.rotates(
                    installed.blockId(), source.group()
            )) {
                for (int pose = 0; pose < EnergyCrystalAnimation.POSE_COUNT; pose++) {
                    emitTriangle(
                            EnergyCrystalAnimation.pose(triangle, pose),
                            poseMaterials.get(pose), logical, pose, block, target
                    );
                }
            } else {
                emitTriangle(triangle, staticMaterial, logical, 0, block, target);
            }
        }
        if (target.getTileModel().size() == start) {
            return false;
        }
        target.initialize(start);
        materials.values().stream().map(Material::texture).distinct().forEach(texture ->
                mapColor.add(new Color().set(texture.getColorPremultiplied()))
        );
        if (mapColor.a > 0F) {
            mapColor.flatten().straight();
        }
        return true;
    }

    private void emitTriangle(
            Triangle triangle,
            Material material,
            InstalledMaterial logical,
            int pose,
            BlockNeighborhood block,
            TileModelView target
    ) {
        Direction direction = nearestDirection(triangle);
        if (settings.isRenderTopOnly() && direction != Direction.UP) {
            return;
        }
        var normal = direction.toVector();
        LightData own = block.getLightData();
        LightData faced = block.getNeighborBlock(
                normal.getX(), normal.getY(), normal.getZ()
        ).getLightData();
        int sunlight = Math.max(own.getSkyLight(), faced.getSkyLight());
        int blocklight = Math.max(own.getBlockLight(), faced.getBlockLight());
        int visible = settings.isCaveDetectionUsesBlockLight()
                ? Math.max(sunlight, blocklight) : sunlight;
        if (block.isRemoveIfCave() && visible == 0) {
            return;
        }
        int index = target.add(1);
        TileModel mesh = target.getTileModel();
        positions(mesh, index, triangle);
        uvs(mesh, index, triangle, pose);
        mesh.setMaterialIndex(index, material.index());
        mesh.setColor(index, logical.red(), logical.green(), logical.blue());
        mesh.setAOs(index, 1F, 1F, 1F);
        mesh.setSunlight(index, sunlight);
        mesh.setBlocklight(index, blocklight);
    }

    private Map<InstalledMaterial, Material> resolve(InstalledStaticModel installed) {
        Map<InstalledMaterial, Material> result = new LinkedHashMap<>();
        installed.model().triangles().stream().map(triangle ->
                installed.material(triangle.group())).distinct().forEach(material -> {
                    Texture texture = resourcePack.getTextures().get(material.texture());
                    if (texture != null) {
                        result.put(material, new Material(textures.get(material.texture()), texture));
                    }
                });
        long expected = installed.model().triangles().stream().map(triangle ->
                installed.material(triangle.group())).distinct().count();
        return result.size() == expected ? result : null;
    }

    private List<Material> resolvePoseMaterials(InstalledStaticModel installed) {
        if (!EnergyCrystalAnimation.supports(installed.blockId())
                || crystalPoseTextures.size() != EnergyCrystalAnimation.POSE_COUNT) {
            return List.of();
        }
        java.util.ArrayList<Material> result = new java.util.ArrayList<>(
                crystalPoseTextures.size()
        );
        for (Key key : crystalPoseTextures) {
            Texture texture = resourcePack.getTextures().get(key);
            if (texture == null) {
                return List.of();
            }
            result.add(new Material(textures.get(key), texture));
        }
        return List.copyOf(result);
    }

    private static Direction nearestDirection(Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        float abx = b.x() - a.x();
        float aby = b.y() - a.y();
        float abz = b.z() - a.z();
        float acx = c.x() - a.x();
        float acy = c.y() - a.y();
        float acz = c.z() - a.z();
        float x = aby * acz - abz * acy;
        float y = abz * acx - abx * acz;
        float z = abx * acy - aby * acx;
        float ax = Math.abs(x);
        float ay = Math.abs(y);
        float az = Math.abs(z);
        if (ay >= ax && ay >= az) {
            return y >= 0 ? Direction.UP : Direction.DOWN;
        }
        if (ax >= az) {
            return x >= 0 ? Direction.EAST : Direction.WEST;
        }
        return z >= 0 ? Direction.SOUTH : Direction.NORTH;
    }

    private static void positions(TileModel mesh, int index, Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        mesh.setPositions(index, a.x(), a.y(), a.z(), b.x(), b.y(), b.z(),
                c.x(), c.y(), c.z());
    }

    private static void uvs(TileModel mesh, int index, Triangle triangle, int pose) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        mesh.setUvs(
                index,
                a.u(), poseV(a.v(), pose),
                b.u(), poseV(b.v(), pose),
                c.u(), poseV(c.v(), pose)
        );
    }

    static float poseV(float v, int pose) {
        return v + pose;
    }

    private record Material(int index, Texture texture) {
    }
}
