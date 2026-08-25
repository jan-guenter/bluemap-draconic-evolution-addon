/*
 * SPDX-License-Identifier: MIT
 */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePackExtension;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variant;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.Variants;
import de.bluecolored.bluemap.core.util.Key;
import de.bluecolored.bluemap.core.world.BlockProperties;
import de.bluecolored.bluemap.core.world.BlockState;
import io.github.janguenter.bluemap.draconicevolution.activation.AddonRuntime;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModel;
import io.github.janguenter.bluemap.draconicevolution.model.InstalledStaticModels;
import io.github.janguenter.bluemap.draconicevolution.profile.ExactArtifactDetector;
import io.github.janguenter.bluemap.draconicevolution.profile.DraconicEvolution314632Profile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/** Exact-artifact admission hook; family routing deliberately remains stock. */
final class ProfileResourceExtension implements ResourcePackExtension {

    private static final Key SYNTHETIC =
            Key.parse("bluemap_draconic_evolution:static_model");

    private final ResourcePack resourcePack;
    private final AddonRuntime runtime;
    private Map<String, InstalledStaticModel> installed;
    private boolean ready;

    ProfileResourceExtension(ResourcePack resourcePack, AddonRuntime runtime) {
        this.resourcePack = resourcePack;
        this.runtime = runtime;
    }

    @Override
    public void loadResources(Iterable<Path> roots) {
        if (Boolean.getBoolean("bluemap.draconicevolution.disabled")) {
            runtime.inactive("operator-disabled");
            return;
        }
        Path artifact = ExactArtifactDetector.match(
                roots, DraconicEvolution314632Profile.ARTIFACTS.get(0)
        ).orElse(null);
        if (artifact == null) {
            runtime.inactive("exact-artifact-missing-or-duplicate");
            return;
        }
        try {
            installed = InstalledStaticModels.load(artifact);
            var dispatch = resourcePack.getBlockStates().get(SYNTHETIC);
            if (!validDispatch(dispatch)) {
                throw new IOException("synthetic static-model dispatch invalid");
            }
        } catch (IOException | RuntimeException exception) {
            installed = null;
            runtime.inactive("static-resource-" + exception.getClass().getSimpleName());
        }
    }

    @Override
    public Set<Key> collectUsedTextureKeys() {
        if (installed == null) {
            return Set.of();
        }
        return installed.values().stream().flatMap(model ->
                model.model().triangles().stream().map(triangle ->
                        model.material(triangle.group()).texture()
                )).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    @Override
    public void bake() {
        if (installed == null
                || installed.keySet().stream().anyMatch(block ->
                resourcePack.getBlockStates().get(Key.parse(block)) == null)
                || collectUsedTextureKeys().stream().anyMatch(texture ->
                resourcePack.getTextures().get(texture) == null)) {
            runtime.inactive("static-model-bake-incomplete");
            return;
        }
        StaticModelPackData.install(resourcePack, installed);
        ready = true;
        runtime.activate();
        System.out.println("BlueMap Draconic Evolution add-on active: "
                + installed.size() + " static models.");
    }

    @Override
    public Key getBlockStateKey(Key key) {
        return ready && installed.containsKey(key.getFormatted()) ? SYNTHETIC : key;
    }

    @Override
    public void getBlockProperties(BlockState state, BlockProperties.Builder builder) {
        if (ready && installed.containsKey(state.getId().getFormatted())) {
            builder.culling(false).occluding(false).cullingIdentical(false);
        }
    }

    private static boolean validDispatch(
            de.bluecolored.bluemap.core.resources.pack.resourcepack.blockstate.BlockState state
    ) {
        if (state == null || state.getMultipart() != null) {
            return false;
        }
        Variants variants = state.getVariants();
        if (variants == null || variants.getDefaultVariant() == null
                || variants.getDefaultVariant().getVariants().length != 1) {
            return false;
        }
        Variant variant = variants.getDefaultVariant().getVariants()[0];
        return BlueMap522Adapter.isExpectedDispatch(variant);
    }
}
