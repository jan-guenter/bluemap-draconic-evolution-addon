/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import java.util.ArrayList;
import java.util.List;

/** Samples the permanent client-side effects used by the nine energy crystals. */
public final class EnergyCrystalAnimation {

    public static final int POSE_COUNT = 11;
    public static final int CYCLE_TICKS = 88;
    private static final double FULL_TURN = Math.PI * 2D;

    private static final double[] PRIMARY_ANGLES = {0D, 0.91D, 2.18D, 3.69D, 5.25D};
    private static final float[] PRIMARY_RADII = {0.38F, 0.41F, 0.40F, 0.37F, 0.42F};
    private static final float[] PRIMARY_HEIGHTS = {0.52F, 0.47F, 0.58F, 0.44F, 0.54F};
    private static final float[] PRIMARY_SIZES = {0.060F, 0.052F, 0.057F, 0.050F, 0.062F};
    private static final double[] SECONDARY_ANGLES = {0.35D, 1.91D, 3.20D, 5.05D};
    private static final float[] SECONDARY_SIZES = {0.040F, 0.035F, 0.043F, 0.038F};

    private EnergyCrystalAnimation() {
    }

    public static boolean supports(String blockId) {
        return blockId.endsWith("_io_crystal")
                || blockId.endsWith("_relay_crystal")
                || blockId.endsWith("_wireless_crystal");
    }

    public static boolean hasOrbit(String blockId) {
        return blockId.endsWith("_relay_crystal")
                || blockId.endsWith("_wireless_crystal");
    }

    public static boolean hasDirectGlow(String blockId) {
        return blockId.endsWith("_io_crystal");
    }

    public static String directGlowTexture(String blockId) {
        if (!hasDirectGlow(blockId)) {
            throw new IllegalArgumentException("block does not use the direct-I/O glow");
        }
        return "draconicevolution:particle/energy_beam_" + tier(blockId);
    }

    public static List<Particle> particles(String blockId, int pose) {
        if (!hasOrbit(blockId)) {
            return List.of();
        }
        if (pose < 0 || pose >= POSE_COUNT) {
            throw new IllegalArgumentException("crystal particle pose is outside range");
        }
        float[] tierColor = tierColor(blockId);
        float[] ringColor = blockId.endsWith("_wireless_crystal")
                ? new float[]{1F, 0F, 0F}
                : new float[]{0F, 1F, 1F};
        double phase = FULL_TURN * pose / POSE_COUNT;
        List<Particle> result = new ArrayList<>(
                PRIMARY_ANGLES.length + SECONDARY_ANGLES.length
        );
        for (int index = 0; index < PRIMARY_ANGLES.length; index++) {
            result.add(around(
                    PRIMARY_ANGLES[index] + phase,
                    PRIMARY_RADII[index],
                    PRIMARY_HEIGHTS[index],
                    PRIMARY_SIZES[index],
                    tierColor,
                    false
            ));
        }
        for (int index = 0; index < SECONDARY_ANGLES.length; index++) {
            result.add(around(
                    SECONDARY_ANGLES[index] + phase,
                    0.4F,
                    0.5F,
                    SECONDARY_SIZES[index],
                    ringColor,
                    true
            ));
        }
        return List.copyOf(result);
    }

    private static Particle around(
            double angle,
            float radius,
            float y,
            float size,
            float[] color,
            boolean orb
    ) {
        return new Particle(
                0.5F + (float) Math.sin(angle) * radius,
                y,
                0.5F + (float) Math.cos(angle) * radius,
                size,
                color[0], color[1], color[2], orb
        );
    }

    private static float[] tierColor(String blockId) {
        return switch (tier(blockId)) {
            case "basic" -> new float[]{0F, 0.8F, 1F};
            case "wyvern" -> new float[]{0.8F, 0.1F, 1F};
            case "draconic" -> new float[]{1F, 0.7F, 0.2F};
            default -> throw new IllegalArgumentException("unsupported energy crystal tier");
        };
    }

    private static String tier(String blockId) {
        int namespace = blockId.indexOf(':');
        int suffix = blockId.indexOf('_', namespace + 1);
        if (suffix < 0) {
            throw new IllegalArgumentException("energy crystal tier is missing");
        }
        return blockId.substring(namespace + 1, suffix);
    }

    public record Particle(
            float x,
            float y,
            float z,
            float size,
            float red,
            float green,
            float blue,
            boolean orb
    ) {
    }
}
