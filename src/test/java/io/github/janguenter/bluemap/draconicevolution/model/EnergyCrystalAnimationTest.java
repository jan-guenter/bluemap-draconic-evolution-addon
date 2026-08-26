/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class EnergyCrystalAnimationTest {

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
    void matchesTheClientEffectSplit() {
        assertTrue(EnergyCrystalAnimation.hasDirectGlow(
                "draconicevolution:basic_io_crystal"
        ));
        assertFalse(EnergyCrystalAnimation.hasOrbit(
                "draconicevolution:basic_io_crystal"
        ));
        assertTrue(EnergyCrystalAnimation.hasOrbit(
                "draconicevolution:wyvern_relay_crystal"
        ));
        assertTrue(EnergyCrystalAnimation.hasOrbit(
                "draconicevolution:draconic_wireless_crystal"
        ));
        assertEquals(
                "draconicevolution:particle/energy_beam_basic",
                EnergyCrystalAnimation.directGlowTexture(
                        "draconicevolution:basic_io_crystal"
                )
        );
    }

    @Test
    void samplesTwoColoredParticleRingsWithinTheBlock() {
        var first = EnergyCrystalAnimation.particles(
                "draconicevolution:wyvern_wireless_crystal", 0
        );
        var second = EnergyCrystalAnimation.particles(
                "draconicevolution:wyvern_wireless_crystal", 1
        );

        assertEquals(9, first.size());
        assertEquals(5, first.stream().filter(particle -> !particle.orb()).count());
        assertEquals(4, first.stream().filter(EnergyCrystalAnimation.Particle::orb).count());
        assertNotEquals(first, second);
        assertTrue(first.stream().allMatch(particle ->
                particle.x() > 0F && particle.x() < 1F
                        && particle.y() > 0F && particle.y() < 1F
                        && particle.z() > 0F && particle.z() < 1F
        ));
        assertTrue(first.stream().filter(particle -> !particle.orb()).allMatch(particle ->
                particle.red() == 0.8F && particle.green() == 0.1F
                        && particle.blue() == 1F
        ));
        assertTrue(first.stream().filter(EnergyCrystalAnimation.Particle::orb)
                .allMatch(particle -> particle.red() == 1F
                        && particle.green() == 0F && particle.blue() == 0F));
    }

    @Test
    void rejectsAnOutOfRangePose() {
        assertThrows(
                IllegalArgumentException.class,
                () -> EnergyCrystalAnimation.particles(
                        "draconicevolution:basic_relay_crystal",
                        EnergyCrystalAnimation.POSE_COUNT
                )
        );
    }
}
