/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Vertex;
import org.junit.jupiter.api.Test;

class EnergyCrystalAnimationTest {

    private static final double EPSILON = 1.0E-6D;

    @Test
    void coversOnlyTheNineEnergyCrystalFamilies() {
        assertTrue(EnergyCrystalAnimation.supports(
                "draconicevolution:basic_io_crystal"
        ));
        assertTrue(EnergyCrystalAnimation.supports(
                "draconicevolution:wyvern_relay_crystal"
        ));
        assertTrue(EnergyCrystalAnimation.supports(
                "draconicevolution:draconic_wireless_crystal"
        ));
        assertFalse(EnergyCrystalAnimation.supports(
                "draconicevolution:chaos_crystal"
        ));
    }

    @Test
    void leavesTheIoBaseStationary() {
        String io = "draconicevolution:basic_io_crystal";
        assertFalse(EnergyCrystalAnimation.rotates(io, "Base"));
        assertTrue(EnergyCrystalAnimation.rotates(io, "Crystal"));
        assertTrue(EnergyCrystalAnimation.rotates(
                "draconicevolution:basic_relay_crystal", "default"
        ));
    }

    @Test
    void rotatesAroundTheBlockCenterAndPreservesMetadata() {
        Vertex first = new Vertex(1F, 0.25F, 0.5F, 0.2F, 0.3F);
        Triangle source = new Triangle(
                first,
                new Vertex(0.5F, 0.75F, 1F, 0.4F, 0.5F),
                new Vertex(0F, 0.5F, 0.5F, 0.6F, 0.7F),
                "Crystal"
        );

        Triangle pose = EnergyCrystalAnimation.pose(source, 2);

        double radians = Math.toRadians(15D);
        assertEquals(0.5D + 0.5D * Math.cos(radians), pose.first().x(), EPSILON);
        assertEquals(0.5D - 0.5D * Math.sin(radians), pose.first().z(), EPSILON);
        assertEquals(first.y(), pose.first().y(), EPSILON);
        assertEquals(first.u(), pose.first().u(), EPSILON);
        assertEquals(first.v(), pose.first().v(), EPSILON);
        assertEquals(source.group(), pose.group());
        assertEquals(source, EnergyCrystalAnimation.pose(source, 0));
    }

    @Test
    void rejectsAnOutOfRangePose() {
        Triangle source = new Triangle(
                new Vertex(0F, 0F, 0F, 0F, 0F),
                new Vertex(1F, 0F, 0F, 0F, 0F),
                new Vertex(0F, 1F, 0F, 0F, 0F),
                "Crystal"
        );
        assertThrows(
                IllegalArgumentException.class,
                () -> EnergyCrystalAnimation.pose(source, EnergyCrystalAnimation.POSE_COUNT)
        );
    }
}
