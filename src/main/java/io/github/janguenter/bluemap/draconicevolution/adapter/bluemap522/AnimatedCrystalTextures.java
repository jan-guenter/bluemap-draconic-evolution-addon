/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.ResourcePack;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.AnimationMeta.FrameMeta;
import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.draconicevolution.model.EnergyCrystalAnimation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/** Builds one shared pose mask driven by BlueMap's native texture clock. */
final class AnimatedCrystalTextures {

    static final Key SOURCE = Key.parse("draconicevolution:models/crystal_no_shader");
    private static final Key KEY = Key.parse(
            "bluemap_draconic_evolution:block/energy_crystal_spin_poses"
    );
    private static final int EXPECTED_EDGE = 128;
    private static final int MAX_TEXTURE_HEIGHT = 8_192;

    private AnimatedCrystalTextures() {
    }

    static List<Key> keys() {
        return List.of(KEY);
    }

    static List<Key> install(ResourcePack resourcePack, Texture source) throws IOException {
        if (resourcePack.getTextures().containsKey(KEY)) {
            throw new IOException("animated crystal texture key collision");
        }
        resourcePack.getTextures().put(KEY, create(source));
        return List.copyOf(Collections.nCopies(EnergyCrystalAnimation.POSE_COUNT, KEY));
    }

    static Texture create(Texture source) throws IOException {
        BufferedImage image = source.getTextureImage();
        if (image.getWidth() != EXPECTED_EDGE || image.getHeight() != EXPECTED_EDGE) {
            throw new IOException("installed crystal texture dimensions changed");
        }
        int poseCount = EnergyCrystalAnimation.POSE_COUNT;
        int stripHeight = Math.multiplyExact(EXPECTED_EDGE, poseCount * poseCount);
        if (stripHeight > MAX_TEXTURE_HEIGHT) {
            throw new IOException("animated crystal texture exceeds height budget");
        }
        int[] pixels = image.getRGB(
                0, 0, EXPECTED_EDGE, EXPECTED_EDGE, null, 0, EXPECTED_EDGE
        );
        BufferedImage strip = new BufferedImage(
                EXPECTED_EDGE, stripHeight, BufferedImage.TYPE_INT_ARGB
        );
        for (int frame = 0; frame < poseCount; frame++) {
            int activeSlot = frame * poseCount + frame;
            strip.setRGB(
                    0, activeSlot * EXPECTED_EDGE,
                    EXPECTED_EDGE, EXPECTED_EDGE, pixels, 0, EXPECTED_EDGE
            );
        }
        return Texture.from(KEY, strip, animationMeta());
    }

    private static AnimationMeta animationMeta() {
        int poseCount = EnergyCrystalAnimation.POSE_COUNT;
        List<FrameMeta> frames = new ArrayList<>(poseCount);
        for (int index = 0; index < poseCount; index++) {
            int ticks = index < 3 ? 53 : 52;
            frames.add(new FrameMeta(index * poseCount, ticks));
        }
        return new AnimationMeta(
                true, EXPECTED_EDGE, EXPECTED_EDGE, 53, List.copyOf(frames)
        );
    }
}
