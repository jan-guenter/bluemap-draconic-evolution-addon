/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel;

import java.util.Map;
import java.util.WeakHashMap;

/** Per-resource-pack compiled Draconic static models. */
final class StaticModelPackData {

    private static final Map<ResourcePack, Map<String, InstalledStaticModel>> PACKS =
            new WeakHashMap<>();

    private StaticModelPackData() {
    }

    static synchronized void install(
            ResourcePack pack,
            Map<String, InstalledStaticModel> models
    ) {
        PACKS.put(pack, Map.copyOf(models));
    }

    static synchronized Map<String, InstalledStaticModel> get(ResourcePack pack) {
        return PACKS.get(pack);
    }
}
