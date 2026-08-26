/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import de.bluecolored.bluemap.core.util.Key;

import java.util.Map;

/** One static model compiled from resources in the admitted Draconic JAR. */
public record InstalledStaticModel(
        String blockId,
        WavefrontModel model,
        Map<String, InstalledMaterial> groupMaterials,
        InstalledMaterial defaultMaterial,
        Transform transform,
        boolean rotateWithHorizontalFacing
) {

    public InstalledStaticModel {
        groupMaterials = Map.copyOf(groupMaterials);
    }

    public InstalledMaterial material(String group) {
        return groupMaterials.getOrDefault(group, defaultMaterial);
    }

    /** Texture plus a deterministic neutral tint. */
    public record InstalledMaterial(Key texture, float red, float green, float blue) {
    }

    /** Axis-aligned scale followed by translation. */
    public record Transform(
            float scaleX,
            float scaleY,
            float scaleZ,
            float translateX,
            float translateY,
            float translateZ
    ) {
    }
}
