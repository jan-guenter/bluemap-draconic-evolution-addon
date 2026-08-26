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
import java.util.Set;

/** Builds one shared particle-pose mask driven by BlueMap's native texture clock. */
final class AnimatedCrystalTextures {

    static final Key ENERGY_SOURCE = Key.parse("draconicevolution:particle/energy_0");
    static final Key ORB_SOURCE = Key.parse("draconicevolution:particle/white_orb");
    private static final Key KEY = Key.parse(
            "bluemap_draconic_evolution:block/energy_crystal_particle_poses"
    );
    private static final int SOURCE_EDGE = 32;
    private static final int SLOT_EDGE = 64;
    private static final int MAX_TEXTURE_HEIGHT = 8_192;

    private AnimatedCrystalTextures() {
    }

    static List<Key> keys() {
        return List.of(KEY);
    }

    static Set<Key> sourceKeys() {
        return Set.of(
                ENERGY_SOURCE,
                ORB_SOURCE,
                Key.parse("draconicevolution:particle/energy_beam_basic"),
                Key.parse("draconicevolution:particle/energy_beam_wyvern"),
                Key.parse("draconicevolution:particle/energy_beam_draconic")
        );
    }

    static List<Key> install(
            ResourcePack resourcePack,
            Texture energy,
            Texture orb
    ) throws IOException {
        if (resourcePack.getTextures().containsKey(KEY)) {
            throw new IOException("animated crystal texture key collision");
        }
        resourcePack.getTextures().put(KEY, create(energy, orb));
        return List.copyOf(Collections.nCopies(EnergyCrystalAnimation.POSE_COUNT, KEY));
    }

    static Texture create(Texture energy, Texture orb) throws IOException {
        BufferedImage energyImage = energy.getTextureImage();
        BufferedImage orbImage = orb.getTextureImage();
        if (!expectedSize(energyImage) || !expectedSize(orbImage)) {
            throw new IOException("installed crystal particle texture dimensions changed");
        }
        int poseCount = EnergyCrystalAnimation.POSE_COUNT;
        int stripHeight = Math.multiplyExact(SLOT_EDGE, poseCount * poseCount);
        if (stripHeight > MAX_TEXTURE_HEIGHT) {
            throw new IOException("animated crystal texture exceeds height budget");
        }
        int[] energyPixels = energyImage.getRGB(
                0, 0, SOURCE_EDGE, SOURCE_EDGE, null, 0, SOURCE_EDGE
        );
        int[] orbPixels = orbImage.getRGB(
                0, 0, SOURCE_EDGE, SOURCE_EDGE, null, 0, SOURCE_EDGE
        );
        BufferedImage strip = new BufferedImage(
                SLOT_EDGE, stripHeight, BufferedImage.TYPE_INT_ARGB
        );
        for (int frame = 0; frame < poseCount; frame++) {
            int activeSlot = frame * poseCount + frame;
            int y = activeSlot * SLOT_EDGE;
            strip.setRGB(
                    0, y, SOURCE_EDGE, SOURCE_EDGE,
                    energyPixels, 0, SOURCE_EDGE
            );
            strip.setRGB(
                    SOURCE_EDGE, y, SOURCE_EDGE, SOURCE_EDGE,
                    orbPixels, 0, SOURCE_EDGE
            );
        }
        return Texture.from(KEY, strip, animationMeta());
    }

    private static boolean expectedSize(BufferedImage image) {
        return image.getWidth() == SOURCE_EDGE && image.getHeight() == SOURCE_EDGE;
    }

    private static AnimationMeta animationMeta() {
        int poseCount = EnergyCrystalAnimation.POSE_COUNT;
        List<FrameMeta> frames = new ArrayList<>(poseCount);
        for (int index = 0; index < poseCount; index++) {
            int ticks = index < 8 ? 46 : 45;
            frames.add(new FrameMeta(index * poseCount, ticks));
        }
        return new AnimationMeta(
                true, SLOT_EDGE, SLOT_EDGE, 46, List.copyOf(frames)
        );
    }
}
