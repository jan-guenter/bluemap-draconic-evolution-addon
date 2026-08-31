/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap523;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.draconicevolution.model.EnergyCrystalAnimation;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnimatedCrystalTexturesTest {

    @Test
    void createsOneClockedElevenPoseParticleAtlas() throws IOException {
        BufferedImage energyImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        energyImage.setRGB(0, 0, 0xff123456);
        energyImage.setRGB(31, 31, 0xffabcdef);
        BufferedImage orbImage = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
        orbImage.setRGB(0, 0, 0xff654321);
        orbImage.setRGB(31, 31, 0xfffedcba);
        Texture energy = Texture.from(Key.parse("test:energy"), energyImage);
        Texture orb = Texture.from(Key.parse("test:orb"), orbImage);

        Texture generated = AnimatedCrystalTextures.create(energy, orb);

        BufferedImage strip = generated.getTextureImage();
        int poses = EnergyCrystalAnimation.POSE_COUNT;
        assertEquals(64, strip.getWidth());
        assertEquals(64 * poses * poses, strip.getHeight());
        for (int frame = 0; frame < poses; frame++) {
            for (int pose = 0; pose < poses; pose++) {
                int slot = frame * poses + pose;
                int expectedEnergy = frame == pose ? 0xff123456 : 0;
                int expectedOrb = frame == pose ? 0xff654321 : 0;
                assertEquals(expectedEnergy, strip.getRGB(0, slot * 64));
                assertEquals(expectedOrb, strip.getRGB(32, slot * 64));
                assertEquals(frame == pose ? 0xffabcdef : 0,
                        strip.getRGB(31, slot * 64 + 31));
                assertEquals(frame == pose ? 0xfffedcba : 0,
                        strip.getRGB(63, slot * 64 + 31));
            }
        }
        assertNotNull(generated.getAnimation());
        assertFalse(generated.getAnimation().isInterpolate());
        assertEquals(8, generated.getAnimation().getFrametime());
        assertEquals(
                List.of(0, 11, 22, 33, 44, 55, 66, 77, 88, 99, 110),
                generated.getAnimation().getFrames().stream()
                        .map(frame -> frame.getIndex()).toList()
        );
        assertEquals(
                EnergyCrystalAnimation.CYCLE_TICKS,
                generated.getAnimation().getFrames().stream()
                        .mapToInt(frame -> frame.getTime()).sum()
        );
    }

    @Test
    void rejectsAChangedInstalledTextureSize() throws IOException {
        Texture changed = Texture.from(
                Key.parse("test:changed"),
                new BufferedImage(64, 64, BufferedImage.TYPE_INT_ARGB)
        );
        Texture expected = Texture.from(
                Key.parse("test:expected"),
                new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB)
        );
        assertThrows(IOException.class,
                () -> AnimatedCrystalTextures.create(changed, expected));
    }

    @Test
    void shiftsEachPoseIntoItsMaskSlot() {
        assertEquals(0.25F, StaticModelEmitter.poseV(0.25F, 0));
        assertEquals(10.25F, StaticModelEmitter.poseV(0.25F, 10));
    }
}
