/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap523;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Per-resource-pack compiled Draconic models and optional crystal animation. */
final class StaticModelPackData {

    private static final Map<ResourcePack, Data> PACKS =
            new WeakHashMap<>();

    private StaticModelPackData() {
    }

    static synchronized void install(
            ResourcePack pack,
            Map<String, InstalledStaticModel> models,
            List<Key> crystalPoseTextures
    ) {
        PACKS.put(pack, new Data(models, crystalPoseTextures));
    }

    static synchronized Data get(ResourcePack pack) {
        return PACKS.get(pack);
    }

    record Data(
            Map<String, InstalledStaticModel> models,
            List<Key> crystalPoseTextures
    ) {

        Data {
            models = Map.copyOf(models);
            crystalPoseTextures = List.copyOf(crystalPoseTextures);
        }
    }
}
