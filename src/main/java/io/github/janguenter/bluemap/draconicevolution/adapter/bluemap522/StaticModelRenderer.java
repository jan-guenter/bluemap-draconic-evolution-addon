/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import de.bluecolored.bluemap.core.map.TextureGallery;
import de.bluecolored.bluemap.core.map.hires.MaxCapacityReachedException;
import de.bluecolored.bluemap.core.map.hires.RenderSettings;
import de.bluecolored.bluemap.core.map.hires.TileModelView;
import de.bluecolored.bluemap.core.map.hires.block.BlockRenderer;
import de.bluecolored.bluemap.core.map.hires.block.ResourceModelRenderer;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.util.math.Color;
import de.bluecolored.bluemap.core.world.block.BlockNeighborhood;
import io.github.janguenter.bluemap.draconicevolution.activation.AddonRuntime;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel;

import java.util.Map;

/** Static neutral renderer for admitted Draconic block-entity models. */
final class StaticModelRenderer implements BlockRenderer {

    private final ResourcePack resourcePack;
    private final ResourceModelRenderer stock;
    private final StaticModelEmitter emitter;
    private final AddonRuntime runtime;
    private final Map<String, InstalledStaticModel> installed;

    StaticModelRenderer(
            ResourcePack resourcePack,
            TextureGallery textures,
            RenderSettings settings,
            AddonRuntime runtime
    ) {
        this.resourcePack = resourcePack;
        this.stock = new ResourceModelRenderer(resourcePack, textures, settings);
        this.runtime = runtime;
        StaticModelPackData.Data data = StaticModelPackData.get(resourcePack);
        this.installed = data == null ? null : data.models();
        this.emitter = new StaticModelEmitter(
                resourcePack, textures, settings,
                data == null ? java.util.List.of() : data.crystalPoseTextures()
        );
    }

    @Override
    public void render(
            BlockNeighborhood block,
            Variant ignored,
            TileModelView target,
            Color mapColor
    ) {
        InstalledStaticModel model = installed == null ? null : installed.get(
                block.getBlockState().getId().getFormatted()
        );
        if (model == null) {
            renderStock(block, target, mapColor);
            return;
        }
        String facing = block.getBlockState().getProperties().getOrDefault(
                "facing", "north"
        );
        int start = target.getStart();
        Color initial = new Color().set(mapColor);
        try {
            if (!emitter.emit(model, facing, block, target, mapColor)) {
                reset(target, start);
                mapColor.set(initial);
                renderStock(block, target, mapColor);
            }
        } catch (MaxCapacityReachedException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            reset(target, start);
            mapColor.set(initial);
            runtime.inactive("static-renderer-" + exception.getClass().getSimpleName());
            renderStock(block, target, mapColor);
        }
    }

    private void renderStock(BlockNeighborhood block, TileModelView target, Color mapColor) {
        var state = resourcePack.getBlockStates().get(block.getBlockState().getId());
        if (state == null) {
            return;
        }
        state.forEach(
                block.getBlockState(), block.getX(), block.getY(), block.getZ(),
                variant -> stock.render(block, variant, target, mapColor)
        );
    }

    private static void reset(TileModelView target, int start) {
        target.getTileModel().reset(start);
        target.initialize(start);
    }
}
