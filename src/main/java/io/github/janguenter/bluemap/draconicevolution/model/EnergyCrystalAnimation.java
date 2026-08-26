/* SPDX-License-Identifier: MIT */

package io.github.janguenter.bluemap.draconicevolution.model;

import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Triangle;
import io.github.janguenter.bluemap.draconicevolution.model.WavefrontModel.Vertex;

/** Samples the client crystal spin across one representative hexagonal sector. */
public final class EnergyCrystalAnimation {

    public static final int POSE_COUNT = 8;
    public static final int CYCLE_TICKS = 419;
    private static final double SECTOR_DEGREES = 60D;

    private EnergyCrystalAnimation() {
    }

    public static boolean supports(String blockId) {
        return blockId.endsWith("_io_crystal")
                || blockId.endsWith("_relay_crystal")
                || blockId.endsWith("_wireless_crystal");
    }

    public static boolean rotates(String blockId, String group) {
        return supports(blockId)
                && (!blockId.endsWith("_io_crystal") || "Crystal".equals(group));
    }

    public static Triangle pose(Triangle input, int pose) {
        if (pose < 0 || pose >= POSE_COUNT) {
            throw new IllegalArgumentException("crystal animation pose is outside range");
        }
        double radians = Math.toRadians(SECTOR_DEGREES * pose / POSE_COUNT);
        float cosine = (float) Math.cos(radians);
        float sine = (float) Math.sin(radians);
        return new Triangle(
                rotate(input.first(), cosine, sine),
                rotate(input.second(), cosine, sine),
                rotate(input.third(), cosine, sine),
                input.group()
        );
    }

    private static Vertex rotate(Vertex input, float cosine, float sine) {
        float x = input.x() - 0.5F;
        float z = input.z() - 0.5F;
        return new Vertex(
                0.5F + x * cosine + z * sine,
                input.y(),
                0.5F - x * sine + z * cosine,
                input.u(),
                input.v()
        );
    }
}
