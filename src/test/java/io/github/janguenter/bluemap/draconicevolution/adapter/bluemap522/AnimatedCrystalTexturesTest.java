/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.adapter.bluemap522;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.bluecolored.bluemap.core.resources.pack.resourcepack.texture.Texture;
import de.bluecolored.bluemap.core.util.Key;
import io.github.janguenter.bluemap.draconicevolution.model.EnergyCrystalAnimation;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import org.junit.jupiter.api.Test;

class AnimatedCrystalTexturesTest {

    @Test
    void createsOneClockedEightPoseMaskAtlas() throws IOException {
        BufferedImage sourceImage = new BufferedImage(128, 128, BufferedImage.TYPE_INT_ARGB);
        sourceImage.setRGB(0, 0, 0xff123456);
        sourceImage.setRGB(127, 127, 0xffabcdef);
        Texture source = Texture.from(Key.parse("test:crystal"), sourceImage);

        Texture generated = AnimatedCrystalTextures.create(source);

        BufferedImage strip = generated.getTextureImage();
        int poses = EnergyCrystalAnimation.POSE_COUNT;
        assertEquals(128, strip.getWidth());
        assertEquals(128 * poses * poses, strip.getHeight());
        for (int frame = 0; frame < poses; frame++) {
            for (int pose = 0; pose < poses; pose++) {
                int slot = frame * poses + pose;
                int expectedFirst = frame == pose ? 0xff123456 : 0;
                int expectedLast = frame == pose ? 0xffabcdef : 0;
                assertEquals(expectedFirst, strip.getRGB(0, slot * 128));
                assertEquals(expectedLast, strip.getRGB(127, slot * 128 + 127));
            }
        }
        assertNotNull(generated.getAnimation());
        assertTrue(generated.getAnimation().isInterpolate());
        assertEquals(53, generated.getAnimation().getFrametime());
        assertEquals(
                List.of(0, 8, 16, 24, 32, 40, 48, 56),
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
        assertThrows(IOException.class, () -> AnimatedCrystalTextures.create(changed));
    }

    @Test
    void shiftsEachPoseIntoItsMaskSlot() {
        assertEquals(0.25F, StaticModelEmitter.poseV(0.25F, 0));
        assertEquals(7.25F, StaticModelEmitter.poseV(0.25F, 7));
    }
}
