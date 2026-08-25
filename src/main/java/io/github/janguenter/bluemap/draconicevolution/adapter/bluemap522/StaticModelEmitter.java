/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModel;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Direction;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.LightData;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel.InstalledMaterial;
import io.github.janguenter.bluemap.draconicevolution.model.StaticModelTransform;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Vertex;

import java.util.LinkedHashMap;
import java.util.Map;

/** Emits one deterministic model using textures from the admitted resource pack. */
final class StaticModelEmitter {

    private final ResourcePack resourcePack;
    private final TextureGallery textures;
    private final RenderSettings settings;

    StaticModelEmitter(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings
    ) {
        this.resourcePack = resourcePack;
        this.textures = textures;
        this.settings = settings;
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
        int start = target.getTileModel().size();
        for (Triangle source : installed.model().triangles()) {
            Triangle triangle = StaticModelTransform.triangle(
                    source, installed.transform(), installed.rotateWithHorizontalFacing(), facing
            );
            Direction direction = nearestDirection(triangle);
            if (settings.isRenderTopOnly() && direction != Direction.UP) {
                continue;
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
                continue;
            }
            InstalledMaterial logical = installed.material(source.group());
            Material material = materials.get(logical);
            if (material == null) {
                return false;
            }
            int index = target.add(1);
            TileModel mesh = target.getTileModel();
            positions(mesh, index, triangle);
            uvs(mesh, index, triangle);
            mesh.setMaterialIndex(index, material.index());
            mesh.setColor(index, logical.red(), logical.green(), logical.blue());
            mesh.setAOs(index, 1F, 1F, 1F);
            mesh.setSunlight(index, sunlight);
            mesh.setBlocklight(index, blocklight);
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

    private static void uvs(TileModel mesh, int index, Triangle triangle) {
        Vertex a = triangle.first();
        Vertex b = triangle.second();
        Vertex c = triangle.third();
        mesh.setUvs(index, a.u(), a.v(), b.u(), b.v(), c.u(), c.v());
    }

    private record Material(int index, Texture texture) {
    }
}
